package com.quollwriter;

import java.io.*;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Set;

import java.awt.event.*;

import com.gentlyweb.properties.*;

import com.quollwriter.ui.*;
import com.quollwriter.events.*;

public class UserProperties
{
    
    private static com.gentlyweb.properties.Properties props = new com.gentlyweb.properties.Properties ();
    private static Map<UserPropertyListener, Object> listeners = null;    
    
    // Just used in the map above as a placeholder for the listeners.
    private static final Object listenerFillObj = new Object ();    
    
    static
    {
        
        UserProperties.listeners = Collections.synchronizedMap (new WeakHashMap ());        
        
    }
    
    private UserProperties ()
    {
        
    }
    
    public static void init (com.gentlyweb.properties.Properties props)
    {
        
        if (props == null)
        {
            
            throw new NullPointerException ("Properties must be specified");
            
        }
        
        UserProperties.props = props;
        
    }
    
    public static void removeListener (UserPropertyListener l)
    {
        
        UserProperties.listeners.remove (l);
        
    }

    /**
     * Adds a listener for property events.  Warning!  This will be a soft reference that can
     * disappear so make sure you have a strong reference to your listener.
     *
     * @param l The listener.
     */
    public static void addListener (UserPropertyListener l)
    {
        
        UserProperties.listeners.put (l,
                                      UserProperties.listenerFillObj);
        
    }

    public static void fireUserPropertyEvent (Object                 source,
                                              String                 name,
                                              AbstractProperty       prop,
                                              UserPropertyEvent.Type action)
    {
        
        UserProperties.fireUserPropertyEvent (new UserPropertyEvent (source,
                                                                     name,
                                                                     prop,
                                                                     action));  
        
    }

    public static void fireUserPropertyEvent (final UserPropertyEvent ev)
    {
                
        UIUtils.doActionLater (new ActionListener ()
        {
        
            public void actionPerformed (ActionEvent aev)
            {
                                
                Set<UserPropertyListener> ls = null;
                                
                // Get a copy of the current valid listeners.
                synchronized (UserProperties.listeners)
                {
                                    
                    ls = new LinkedHashSet (UserProperties.listeners.keySet ());
                    
                }
                    
                for (UserPropertyListener l : ls)
                {
                    
                    l.propertyChanged (ev);

                }

            }
            
        });
        
    }    
    
    public static void remove (String name)
    {
        
        UserProperties.props.removeProperty (name);

        UserProperties.fireUserPropertyEvent (UserProperties.listenerFillObj,
                                              name,
                                              null,
                                              UserPropertyEvent.Type.removed);

        UserProperties.save ();
        
    }

    public static void set (String           name,
                            AbstractProperty prop)
    {
        
        UserProperties.props.setProperty (name,
                                          prop);
        
        UserProperties.fireUserPropertyEvent (UserProperties.listenerFillObj,
                                              name,
                                              prop,
                                              UserPropertyEvent.Type.changed);
        
        UserProperties.save ();
        
    }

    public static void set (String name,
                            String value)
    {
        
        UserProperties.set (name,
                            new StringProperty (name,
                                                value));
        
        UserProperties.save ();
        
    }

    public static void set (String  name,
                            boolean value)
    {
        
        UserProperties.set (name,
                            new BooleanProperty (name,
                                                 value));
        
        UserProperties.save ();
        
    }
    
    public static void set (String  name,
                            float   value)
    {
        
        UserProperties.set (name,
                            new FloatProperty (name,
                                               value));
        
        UserProperties.save ();
        
    }
    
    public static void set (String  name,
                            int     value)
    {
        
        UserProperties.set (name,
                            new IntegerProperty (name,
                                                 value));
        
        UserProperties.save ();
        
    }

    public static boolean getAsBoolean (String name,
                                        String defOnNull)
    {
        
        AbstractProperty a = UserProperties.props.getPropertyObj (name);
        
        if (a == null)
        {
            
            return UserProperties.getAsBoolean (defOnNull);
            
        }
        
        return UserProperties.props.getPropertyAsBoolean (name);
        
    }
    
    public static boolean getAsBoolean (String name)
    {
        
        return UserProperties.props.getPropertyAsBoolean (name);
        
    }
    
    public static int getAsInt (String name,
                                String defOnNull)
    {
        
        AbstractProperty a = UserProperties.props.getPropertyObj (name);
        
        if (a == null)
        {
            
            return UserProperties.getAsInt (defOnNull);
            
        }
        
        return UserProperties.props.getPropertyAsInt (name);
        
    }

    public static int getAsInt (String name)
    {
        
        return UserProperties.props.getPropertyAsInt (name);
        
    }

    public static File getAsFile (String name)
    {
        
        return UserProperties.props.getPropertyAsFile (name);
        
    }

    public static float getAsFloat (String name,
                                    String defOnNull)
    {
        
        AbstractProperty a = UserProperties.props.getPropertyObj (name);
        
        if (a == null)
        {
            
            return UserProperties.getAsFloat (defOnNull);
            
        }
        
        return UserProperties.props.getPropertyAsFloat (name);
        
    }

    public static float getAsFloat (String name)
    {
        
        return UserProperties.props.getPropertyAsFloat (name);
        
    }
    
    public static String get (String name,
                              String defOnNull)
    {
        
        AbstractProperty a = UserProperties.props.getPropertyObj (name);
        
        if (a == null)
        {
            
            return UserProperties.get (defOnNull);
            
        }
        
        return UserProperties.props.getProperty (name);
        
    }

    public static String get (String name)
    {
        
        return UserProperties.props.getProperty (name);
        
    }
    
    public static AbstractProperty getProperty (String name)
    {
        
        return UserProperties.props.getPropertyObj (name);
        
    }
    
    public static Properties getProperties ()
    {
        
        return UserProperties.props;
        
    }
    
    private static void save ()
    {

        try
        {
    
            Environment.saveUserProperties ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to set user properties",
                                  e);
            
        }

    }
    
}