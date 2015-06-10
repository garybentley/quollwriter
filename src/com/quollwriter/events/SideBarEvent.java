package com.quollwriter.events;

import java.util.EventObject;

import com.quollwriter.ui.sidebars.AbstractSideBar;

public class SideBarEvent extends EventObject
{

    private AbstractSideBar sb = null;

    public SideBarEvent (Object          source,
                         AbstractSideBar sb)
    {
        
        super (source);
        
        this.sb = sb;
        
    }

    public AbstractSideBar getSideBar ()
    {
        
        return this.sb;
        
    }
    
}