package com.quollwriter.events;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

public class MouseEventHandler extends MouseAdapter
{
    
    public void fillPopup (JPopupMenu m,
                           MouseEvent ev)
    {
        
    }
    
    public void handleMiddlePress (MouseEvent ev)
    {
        
    }
        
    public void handlePress (MouseEvent ev)
    {
                
    }
    
    private void _handlePress (MouseEvent ev)
    {
        
        if (SwingUtilities.isMiddleMouseButton (ev))
        {
            
            this.handleMiddlePress (ev);
            
            return;
            
        }

        if (ev.isPopupTrigger ())
        {
            
            JPopupMenu m = new JPopupMenu ();

            this.fillPopup (m,
                            ev);
            
            if (m.getComponentCount () == 0)
            {
                
                return;
                
            }
            
            Component c = (Component) ev.getSource ();

            m.show (c,
                    ev.getX (),
                    ev.getY ());
                        
            return;            
            
        }

        this.handlePress (ev);
        
    }
    
    public void mouseReleased (MouseEvent ev)
    {

        this._handlePress (ev);
                
    }

    public void mousePressed (MouseEvent ev)
    {
                    
        if (!Environment.isWindows)
        {

            this._handlePress (ev);

        }

    }
    
    
}