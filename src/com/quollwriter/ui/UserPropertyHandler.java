package com.quollwriter.ui;

import java.util.*;

import java.awt.event.*;
import javax.swing.event.*;

import com.gentlyweb.properties.StringProperty;

import com.quollwriter.*;
import com.quollwriter.events.*;

import com.quollwriter.data.*;
import com.quollwriter.db.*;


public class UserPropertyHandler implements TypesHandler
{

    public static final String VALUE_CHANGED = "valueChanged";
    public static final String VALUE_ADDED = "valueAdded";
    public static final String VALUE_DELETED = "valueDeleted";

    public static final String DEFAULT_SEPARATOR = "|";

    private String propName = null;
    private String sep = null;
    private Set<String> types = new TreeSet ();
    private ObjectProvider<QObject> objectProvider = null;
    private Map<PropertyChangedListener, Object> listeners = null;    
    // Just used in the maps above as a placeholder for the listeners.
    private final Object listenerFillObj = new Object ();
    
    public UserPropertyHandler (String propName,
                                String sep)

    {

        this.propName = propName;
        this.sep = (sep != null ? sep : DEFAULT_SEPARATOR);
    
        this.listeners = Collections.synchronizedMap (new WeakHashMap ());        
        
        String nt = Environment.getUserProperties ().getProperty (this.propName);

        if (nt != null)
        {

            StringTokenizer t = new StringTokenizer (nt,
                                                     this.sep);
    
            while (t.hasMoreTokens ())
            {
    
                String tok = t.nextToken ().trim ();
                
                this.types.add (tok);
    
            }

        }

    }

    public boolean hasType (String t)
    {
        
        for (String type : this.types)
        {
            
            if (t.equalsIgnoreCase (type))
            {
                
                return true;
                
            }
            
        }
        
        return false;
        
    }
    
    protected void firePropertyChangedEvent (final PropertyChangedEvent ev)
    {
                
        final UserPropertyHandler _this = this;
                
        UIUtils.doActionLater (new ActionListener ()
        {
        
            public void actionPerformed (ActionEvent aev)
            {
                
                Set<PropertyChangedListener> ls = null;
                                
                // Get a copy of the current valid listeners.
                synchronized (_this.listeners)
                {
                                    
                    ls = new LinkedHashSet (_this.listeners.keySet ());
                    
                }
                    
                for (PropertyChangedListener l : ls)
                {
                    
                    try
                    {
                    
                        l.propertyChanged (ev);
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to update listener: " +
                                              l +
                                              " with change to user property type: " +
                                              _this.propName,
                                              e);
                        
                    }

                }

            }
            
        });
                        
    }
    
    public void removePropertyChangedListener (PropertyChangedListener l)
    {
        
        this.listeners.remove (l);
        
    }
    
    public void addPropertyChangedListener (PropertyChangedListener l)
    {
        
        this.listeners.put (l,
                            this.listenerFillObj);
        
    }        
    
    public boolean typesEditable ()
    {
        
        return true;
        
    }
    
    public boolean removeType (String  type,
                               boolean reload)
    {

        this.types.remove (type);

        this.saveTypes ();

        PropertyChangedEvent ev = new PropertyChangedEvent (this,
                                                            VALUE_DELETED,
                                                            type,
                                                            null);
        
        this.firePropertyChangedEvent (ev);
        
        return true;

    }

    public void addType (String  t,
                         boolean reload)
    {

        if (this.types.contains (t))
        {

            return;

        }

        this.types.add (t);

        this.saveTypes ();

        PropertyChangedEvent ev = new PropertyChangedEvent (this,
                                                            VALUE_ADDED,
                                                            null,
                                                            t);
        
        this.firePropertyChangedEvent (ev);

    }

    public Set<String> getTypes ()
    {

        return new TreeSet<String> (this.types);

    }

    private void saveTypes ()
    {

        StringBuilder sb = new StringBuilder ();

        for (String s : this.types)
        {

            if (sb.length () > 0)
            {

                sb.append (this.sep);

            }

            sb.append (s);

        }

        com.gentlyweb.properties.Properties props = Environment.getUserProperties ();

        StringProperty p = new StringProperty (this.propName,
                                               sb.toString ());
        p.setDescription ("N/A");

        props.setProperty (this.propName,
                           p);

        try
        {

            Environment.saveUserProperties (props);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save user properties for: " +
                                  this.propName +
                                  " property with value: " +
                                  sb.toString (),
                                  e);

        }

    }

    public boolean renameType (String  oldType,
                               String  newType,
                               boolean reload)
    {

        PropertyChangedEvent ev = new PropertyChangedEvent (this,
                                                            VALUE_CHANGED,
                                                            oldType,
                                                            newType);
        
        this.firePropertyChangedEvent (ev);

        this.removeType (oldType,
                         false);
        this.addType (newType,
                      false);

        return true;

    }

}
