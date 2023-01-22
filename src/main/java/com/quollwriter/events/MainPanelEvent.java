package com.quollwriter.events;

import java.util.EventObject;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.data.*;

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
    
    public DataObject getForObject ()
    {
        
        if (this.panel instanceof ProjectObjectQuollPanel)
        {
            
            ProjectObjectQuollPanel p = (ProjectObjectQuollPanel) this.panel;
            
            return p.getForObject ();
            
        }
        
        return null;
        
    }
    
}