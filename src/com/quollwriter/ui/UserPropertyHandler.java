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
    private String[] defaultUIString = null;
    private Set<String> types = null;
    private ObjectProvider<QObject> objectProvider = null;
    private Map<PropertyChangedListener, Object> listeners = null;
    // Just used in the maps above as a placeholder for the listeners.
    private final Object listenerFillObj = new Object ();

    public UserPropertyHandler (String    propName,
                                String    sep,
                                String... def)

    {

        this.propName = propName;
        this.sep = (sep != null ? sep : DEFAULT_SEPARATOR);
        this.defaultUIString = def;

        this.listeners = Collections.synchronizedMap (new WeakHashMap ());

    }

    private void initTypes ()
    {

        if (this.types != null)
        {

            return;

        }

        this.types = new TreeSet ();

        String nt = UserProperties.get (this.propName);

        if (nt == null)
        {

            if (this.defaultUIString != null)
            {}

            nt = Environment.getUIString (this.defaultUIString);

        }

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

        this.initTypes ();

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

        this.initTypes ();

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

        this.initTypes ();

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

        this.initTypes ();

        return new TreeSet<String> (this.types);

    }

    private void saveTypes ()
    {

        this.initTypes ();

        StringBuilder sb = new StringBuilder ();

        for (String s : this.types)
        {

            if (sb.length () > 0)
            {

                sb.append (this.sep);

            }

            sb.append (s);

        }

        UserProperties.set (this.propName,
                            sb.toString ());

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
