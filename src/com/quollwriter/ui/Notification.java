package com.quollwriter.ui;

import java.util.*;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.quollwriter.*;

import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ImagePanel;

public class Notification extends Box implements ActionListener
{

    private AbstractProjectViewer viewer = null;
    private javax.swing.Timer timer = null;
    private int duration = 0;
    private JButton cancel = null;

    public Notification (AbstractProjectViewer   viewer,
                         JComponent              comp,
                         String                  iconType,
                         int                     duration,
                         java.util.List<JButton> buttons)
    {

        super (BoxLayout.X_AXIS);

        this.viewer = viewer;

        this.duration = duration;

        final Notification _this = this;

        this.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.setBorder (new EmptyBorder (5,
                                         7,
                                         5,
                                         7));

        Box b = this;
        
        ImagePanel ip = new ImagePanel (Environment.getIcon (iconType,
                                                             Constants.ICON_NOTIFICATION),
                                        null);
        ip.setAlignmentY (Component.TOP_ALIGNMENT);
        comp.setAlignmentY (Component.TOP_ALIGNMENT);
        b.add (ip);
        b.add (Box.createHorizontalStrut (10));

        b.add (comp);

        final ActionAdapter removeNotification = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.removeNotification ();

            }

        };

        java.util.List<JButton> buts = new ArrayList();

        if (buttons != null)
        {
            
            buts.addAll (buttons);
            
        }

        this.cancel = UIUtils.createButton ("cancel",
                                            Constants.ICON_MENU,
                                            "Click to remove this notification",
                                            removeNotification);
        
        buts.add (this.cancel);

        JToolBar butBar = UIUtils.createButtonBar (buts);

        butBar.setAlignmentY (Component.TOP_ALIGNMENT);

        b.add (butBar);

        b.setBackground (UIUtils.getColor ("FFFFFF"));

        b.setOpaque (true);
        
    }
        
    public static Notification createMessageNotification (AbstractProjectViewer viewer,
                                                          String                message,
                                                          int                   duration)
    {
    
        final HTMLPanel htmlP = new HTMLPanel (message,
                                               null);

        htmlP.setBackground (null);
        htmlP.setOpaque (false);
        htmlP.setAlignmentX (Component.LEFT_ALIGNMENT);

        return new Notification (viewer,
                                 htmlP,
                                 Constants.INFO_ICON_NAME,
                                 duration,
                                 null);
        
    }
    
    public void addCancelListener (ActionListener l)
    {
        
        this.cancel.addActionListener (l);
        
    }
    
    public void removeCancelListener (ActionListener l)
    {
        
        this.cancel.removeActionListener (l);
        
    }
    
    public void init ()
    {
        
        final Notification _this = this;
        
        if (this.timer != null)
        {
            
            this.timer.stop ();
            this.timer.start ();
            
            return;
            
        }
        
        if (this.duration > 0)
        {

            this.timer = new javax.swing.Timer (this.duration * 1000,
                                                new ActionAdapter ()
                                                {
                                                    
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        _this.removeNotification ();
                                                        
                                                    }
                                                    
                                                });

            this.timer.setRepeats (false);
            this.timer.start ();

        }        
        
    }
    
    public void restartTimer ()
    {
        
        this.timer.restart ();
        
    }
    
    public void removeNotification ()
    {

        if (this.timer != null)
        {
    
            this.timer.stop ();
            
        }
        
        this.viewer.removeNotification (this);

    }
 
    public void actionPerformed (ActionEvent ev)
    {
        
        this.removeNotification ();
        
    }
    
}