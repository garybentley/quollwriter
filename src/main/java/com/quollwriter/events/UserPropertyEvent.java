package com.quollwriter.events;

import java.util.EventObject;

import com.gentlyweb.properties.*;

public class UserPropertyEvent extends EventObject
{

    public enum Type
    {

        added ("added"),
        removed ("removed"),
        changed ("changed");
    
        private String type = null;
    
        Type (String t)
        {
            
            this.type = t;
            
        }
    
    }

    private AbstractProperty prop = null;
    private String name = null;
    private Type action = null;
    
    public UserPropertyEvent (Object           source,
                              String           name,
                              AbstractProperty prop,
                              Type             action)
    {
        
        super (source);
        
        this.name = name;
        this.prop = prop;
        this.action = action;
        
    }

    public Type getAction ()
    {
        
        return this.action;
        
    }
    
    public AbstractProperty getProperty ()
    {
        
        return this.prop;
        
    }
    
    public String getName ()
    {
        
        return this.name;
        
    }
        
}