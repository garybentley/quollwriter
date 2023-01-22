package com.quollwriter.events;

import java.util.EventObject;

import com.quollwriter.ui.sidebars.AbstractSideBar;
import com.quollwriter.ui.*;

public class FullScreenEvent extends EventObject
{

    private AbstractProjectViewer viewer = null;
    
    public FullScreenEvent (AbstractProjectViewer source)
    {
        
        super (source);
        
        this.viewer = source;
        
    }

    public AbstractProjectViewer getViewer ()
    {
        
        return this.viewer;
        
    }
        
}