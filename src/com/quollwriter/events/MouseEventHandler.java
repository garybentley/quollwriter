package com.quollwriter.events;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;

public class MouseEventHandler extends MouseAdapter
{

    //public boolean redispatchToParent = false;

    public void redispatch (MouseEvent e)
    {

    }
    /*
        if (this.redispatchToParent)
        {
            
            Component c = e.getComponent ();
            
            if (c.getParent () != null)
            {
    
                e = SwingUtilities.convertMouseEvent (c,
                                                      e,
                                                      c.getParent ());
        
                c.getParent ().dispatchEvent (e);

            }
            
        }

    }
*/
    public void performAction (ActionEvent ev)
    {
        
        // Blank.
        
    }

    public JButton createButton (String         icon,
                                 int            iconType,
                                 String         toolTipText,
                                 String actionCommand)
    {
        
        final MouseEventHandler _this = this;
        
        JButton but = UIUtils.createButton (icon,
                                            iconType,
                                            toolTipText,
                                            new ActionListener ()
                                            {
                                             
                                                 public void actionPerformed (ActionEvent ev)
                                                 {
                                                     
                                                     _this.performAction (ev);
                                                     
                                                 }
                                             
                                            });
        
        but.setActionCommand (actionCommand);

        return but;
        
    }
    
    public JMenuItem createMenuItem (String label,
                                     String icon,
                                     String         actionCommand,
                                     KeyStroke      accel)
    {
        
        JMenuItem mi = this.createMenuItem (label,
                                            icon,
                                            actionCommand);
        
        mi.setAccelerator (accel);
        
        return mi;

        
    }

    public JMenuItem createMenuItem (String label,
                                     String icon,
                                     String actionCommand)
    {
        
        final MouseEventHandler _this = this;
        
        JMenuItem mi = UIUtils.createMenuItem (label,
                                               icon,
                                               new ActionListener ()
                                               {
                                                
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        _this.performAction (ev);
                                                        
                                                    }
                                                
                                               });
                                     
        mi.setActionCommand (actionCommand);
                                                       
        return mi;
        
    }        
   
    public JMenuItem createMenuItem (String label,
                                     String icon,
                                     String         actionCommand,
                                     KeyStroke      accel,
                                     ActionListener list)
    {
        
        JMenuItem mi = UIUtils.createMenuItem (label,
                                               icon,
                                               list);
                                     
        mi.setActionCommand (actionCommand);

        mi.setAccelerator (accel);
        
        return mi;

        
    }           
    
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
    
    public void handleDoublePress (MouseEvent ev)
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

        if (ev.getClickCount () == 2)
        {
            
            this.handleDoublePress (ev);
            
            return;
            
        }
        
        this.handlePress (ev);
        
    }
    
    public void mouseReleased (MouseEvent ev)
    {

        this._handlePress (ev);
                
        this.redispatch (ev);
                
    }

    public void mousePressed (MouseEvent ev)
    {
                    
        if (!Environment.isWindows)
        {

            this._handlePress (ev);

            return;
            
        }

        this.redispatch (ev);
        
    }
    
    public void mouseMoved (MouseEvent e)
    {

        this.redispatch (e);

    }

    public void mouseDragged (MouseEvent e)
    {

        this.redispatch (e);

    }

    public void mouseEntered (MouseEvent e)
    {

        this.redispatch (e);

    }

    public void mouseExited (MouseEvent e)
    {

        this.redispatch (e);

    }
    
    @Override
    public void mouseClicked (MouseEvent e)
    {

        this.redispatch (e);

    }

}