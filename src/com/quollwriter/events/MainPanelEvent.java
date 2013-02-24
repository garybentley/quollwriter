package com.quollwriter.events;

import java.util.EventObject;

import com.quollwriter.ui.*;

public class MainPanelEvent extends EventObject
{

    private QuollPanel panel = null;

    public MainPanelEvent (Object     source,
                           QuollPanel panel)
    {
        
        super (source);
        
        this.panel = panel;
        
    }

    public QuollPanel getPanel ()
    {
        
        return this.panel;
        
    }
    
}