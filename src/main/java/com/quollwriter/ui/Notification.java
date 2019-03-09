package com.quollwriter.ui;

import java.util.*;

import java.awt.Container;
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

    private javax.swing.Timer timer = null;
    private int duration = 0;
    private JButton cancel = null;
    private JComponent content = null;
    private ActionListener onRemove = null;
    private JComponent contentBox = null;

    public Notification (JComponent              comp,
                         String                  iconType,
                         int                     duration,
                         java.util.List<JButton> buttons,
                         ActionListener          onRemove)
    {

        super (BoxLayout.X_AXIS);

        this.content = comp;

        this.duration = duration;

        final Notification _this = this;

        this.onRemove = onRemove;

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

        this.contentBox = new Box (BoxLayout.X_AXIS);
        this.contentBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.contentBox.setAlignmentY (Component.TOP_ALIGNMENT);

        b.add (this.contentBox);

        this.contentBox.add (comp);

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
                                            Environment.getUIString (LanguageStrings.notifications,
                                                                     LanguageStrings.remove,
                                                                     LanguageStrings.tooltip),
                                            //"Click to remove this notification",
                                            removeNotification);

        buts.add (this.cancel);

        JToolBar butBar = UIUtils.createButtonBar (buts);

        butBar.setAlignmentY (Component.TOP_ALIGNMENT);

        b.add (butBar);

        //b.setBackground (UIUtils.getComponentColor ());

        b.setOpaque (false);

    }

    public void setOnRemove (ActionListener onRemove)
    {

        this.onRemove = onRemove;

    }

    public void setContent (JComponent c)
    {

        this.contentBox.removeAll ();
        this.contentBox.add (c);

        this.content = c;

    }

    public JComponent getContent ()
    {

        return this.content;

    }

    public static Notification createHelpNotification (AbstractProjectViewer viewer,
                                                       String                message,
                                                       int                   duration,
                                                       HyperlinkListener     clickListener,
                                                       ActionListener        onRemove)
    {

        JTextPane htmlP = UIUtils.createHelpTextPane (message,
                                                      viewer);

        htmlP.setBorder (null);

        if (clickListener != null)
        {

            htmlP.addHyperlinkListener (clickListener);

        }

        return new Notification (htmlP,
                                 Constants.HELP_ICON_NAME,
                                 duration,
                                 null,
                                 onRemove);

    }

    public static Notification createMessageNotification (AbstractProjectViewer viewer,
                                                          String                message,
                                                          int                   duration,
                                                          HyperlinkListener     clickListener,
                                                          ActionListener        onRemove)
    {

        JTextPane htmlP = UIUtils.createHelpTextPane (message,
                                                      viewer);

        htmlP.setBorder (null);

        if (clickListener != null)
        {

            htmlP.addHyperlinkListener (clickListener);

        }

        return new Notification (htmlP,
                                 Constants.INFO_ICON_NAME,
                                 duration,
                                 null,
                                 onRemove);

    }

    public static Notification createMessageNotification (AbstractProjectViewer viewer,
                                                          String                message,
                                                          int                   duration,
                                                          ActionListener        onRemove)
    {

        return Notification.createMessageNotification (viewer,
                                                       message,
                                                       duration,
                                                       null,
                                                       onRemove);

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

        Container p = this.getParent ();

        if (p != null)
        {

            p.remove (this);

            p.revalidate ();
            p.repaint ();

        }

        if (this.onRemove != null)
        {

            this.onRemove.actionPerformed (new ActionEvent (this, 1, "removed"));

        }

    }

    public void actionPerformed (ActionEvent ev)
    {

        this.removeNotification ();

    }

}
