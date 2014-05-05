package com.quollwriter.ui;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.events.*;

import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ActionAdapter;

public class FullScreenOverlay extends Box
{
    
    private AbstractProjectViewer projectViewer = null;
    
    public FullScreenOverlay (AbstractProjectViewer pv)
    {
        
        super (BoxLayout.Y_AXIS);
        
        this.projectViewer = pv;
        
        this.setBackground (UIUtils.getComponentColor ());
        this.setOpaque (true);
        
        Box bb = new Box (BoxLayout.X_AXIS);
        
        bb.add (Box.createHorizontalGlue ());

        Header h = UIUtils.createHeader ("Quoll Writer is currently in full screen mode, click to exit",
                                         Constants.PANEL_TITLE,
                                         Constants.FULLSCREEN_EXIT_ICON_NAME,
                                         null);
        //h.setBorder (UIUtils.createTestLineBorder ());
        h.setMaximumSize (h.getPreferredSize ());
        h.setAlignmentX (Component.CENTER_ALIGNMENT);
        
        bb.setAlignmentX (Component.CENTER_ALIGNMENT);
        final FullScreenOverlay _this = this;

        final ActionListener closeAction = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.projectViewer.exitFullScreen ();
        
            }
            
        };
        
        this.addMouseListener (new MouseEventHandler ()
        {
            
            public void handlePress (MouseEvent ev)
            {
                
                closeAction.actionPerformed (null);
             
            }
            
        });

        InputMap im = this.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE,
                                        0),
                closeAction);
                
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F9,
                                        0),
                closeAction);
        
        bb.add (h);
        
        this.add (Box.createVerticalGlue ());
        
        this.add (bb);

        this.add (Box.createVerticalGlue ());
                
        UIUtils.setAsButton (this);
        
    }
    
}