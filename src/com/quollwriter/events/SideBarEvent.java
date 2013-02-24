package com.quollwriter.events;

import java.util.EventObject;


public class SideBarEvent extends EventObject
{

    private String name = null;

    public SideBarEvent (Object source,
                         String name)
    {
        
        super (source);
        
        this.name = name;
        
    }

    public String getName ()
    {
        
        return this.name;
        
    }
    
}