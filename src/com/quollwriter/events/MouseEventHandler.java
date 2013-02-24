package com.quollwriter.events;

import java.awt.event.*;

import com.quollwriter.*;

public class MouseEventHandler extends MouseAdapter
{
    
    public void handlePress (MouseEvent ev)
    {
        
    }
    
    public void mouseReleased (MouseEvent ev)
    {

        this.handlePress (ev);
                
    }

    public void mousePressed (MouseEvent ev)
    {
                    
        if (!Environment.isWindows)
        {

            this.handlePress (ev);

        }

    }
    
    
}