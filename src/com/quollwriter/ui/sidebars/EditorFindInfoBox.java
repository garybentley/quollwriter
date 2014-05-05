package com.quollwriter.ui.sidebars;

import java.awt.Component;
import java.awt.event.*;
import java.awt.image.*;

import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.ui.*;
import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ActionAdapter;

public class EditorFindInfoBox extends Box
{
    
    private EditorEditor editor = null;
    
    public EditorFindInfoBox (EditorEditor ed)
    {
        
        super (BoxLayout.X_AXIS);
        
        this.editor = ed;
        
        if (ed.getAvatar () != null)
        {
        
            BufferedImage bi = UIUtils.getScaledImage (ed.getAvatar (),
                                                       50,
                                                       50);
                    
            JLabel il = new JLabel (new ImageIcon (bi));
            il.setAlignmentY (Component.TOP_ALIGNMENT);
            
            this.add (il);
            il.setOpaque (false);
            
            il.setBorder (new MatteBorder (1,
                                           1,
                                           1,
                                           1,
                                           Environment.getBorderColor ()));
            this.add (Box.createHorizontalStrut (5));

        }
        
        Box details = new Box (BoxLayout.Y_AXIS);
        details.setAlignmentY (Component.TOP_ALIGNMENT);
        
        Box nameBox = new Box (BoxLayout.X_AXIS);
        details.add (nameBox);
        nameBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        nameBox.add (this.getNameLabel (ed.getName ()));
        
        //details.add (Box.createVerticalStrut (3));
                        
        this.add (details);

        final EditorFindInfoBox _this = this;
        
        this.addMouseListener (new MouseEventHandler ()
        {
            
            public void handlePress (MouseEvent ev)
            {
                
                if (ev.isPopupTrigger ())
                {
                    
                    // Show the menu.

                    JPopupMenu m = new JPopupMenu ();

                    JMenuItem mi = UIUtils.createMenuItem ("Information",
                                                           Constants.INFO_ICON_NAME,
                                                           null,
                                                           null,
                                                           null);

                    m.add (mi);
    
                    m.add (mi);

                    mi = UIUtils.createMenuItem ("Send invite",
                                                 "message",
                                                 null,
                                                 null,
                                                 null);

                    m.add (mi);
                    
                    Component c = (Component) ev.getSource ();
    
                    m.show (c,
                            ev.getX (),
                            ev.getY ());
                                
                    return;
                    
                }
                                
            }
            
        });
                
    }
    
    private JLabel getNameLabel (String name)
    {
        
        JLabel l = new JLabel (name);
        l.setFont (l.getFont ().deriveFont (16f));
        l.setAlignmentY (Component.TOP_ALIGNMENT);
        l.setVerticalAlignment (SwingConstants.TOP);
        
        return l;
        
    }
    
    public void init ()
    {
        
        
        
    }
    
}