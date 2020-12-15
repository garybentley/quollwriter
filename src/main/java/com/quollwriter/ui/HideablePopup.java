package com.quollwriter.ui;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Rectangle;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.components.ActionAdapter;

public abstract class HideablePopup<E extends AbstractViewer> extends Box
{

    protected E viewer = null;
    private Timer showTimer = null;
    private Timer hideTimer = null;
    private boolean hidden = false;

    public HideablePopup (E viewer)
    {

        super (BoxLayout.Y_AXIS);

        this.viewer = viewer;

        this.setOpaque (true);
        this.setBackground (UIUtils.getComponentColor ());
        this.setBorder (new CompoundBorder (com.quollwriter.ui.components.UIUtils.internalPanelDropShadow,
                                            UIUtils.createLineBorder ()));

        final HideablePopup _this = this;

        this.addMouseListener (new MouseAdapter ()
        {

            public void mouseEntered (MouseEvent ev)
            {

                if (_this.hideTimer != null)
                {

                    _this.hideTimer.stop ();

                }

            }

            public void mouseExited (MouseEvent ev)
            {

                if (_this.hideTimer != null)
                {

                    Point p = new Point (0,
                                         0);

                    SwingUtilities.convertPointToScreen (p,
                                                         _this);

                    Rectangle tBounds = _this.getBounds (null);

                    tBounds.x = p.x;
                    tBounds.y = p.y;

                    if (!tBounds.contains (ev.getLocationOnScreen ()))
                    {

                        _this.hideTimer.start ();

                    }

                }

            }

        });

    }

    /**
     * Called to get the content of the popup.
     *
     * @return The content of the popup.
     */
    public abstract JComponent getContent ();

    /**
     * Use if you want to show the popup after a specific delay.  The hideDelay indicates how long to wait
     * until the user moves the mouse out of the popup before closing.  The hide delay is restarted if the user
     * quickly moves out of the popup and then back in.
     *
     * @param showDelay How long to wait until the popup is shown.
     * @param hideDelay How long to wait until the popup is hidden after the user has moved the mouse out of the popup.
     * @param po Where to show the popup.
     * @param onHide Called after the popup has been hidden.
     */
    public void show (final int            showDelay,
                      final int            hideDelay,
                      final Point          po,
                      final ActionListener onHide)
    {

        final HideablePopup _this = this;

        if (showDelay > 0)
        {

            if (this.showTimer != null)
            {

                this.showTimer.stop ();

            }

            this.showTimer = new Timer (showDelay,
                                        new ActionListener ()
                                        {

                                            @Override
                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                _this.show (po);

                                            }

                                        });

            this.showTimer.setRepeats (false);
            this.showTimer.start ();

            if (hideDelay > 0)
            {

                if (this.hideTimer == null)
                {

                    this.hideTimer = new Timer (hideDelay,
                                                new ActionAdapter ()
                                                {

                                                     public void actionPerformed (ActionEvent ev)
                                                     {

                                                         _this.hidePopup ();

                                                         if (onHide != null)
                                                         {

                                                            onHide.actionPerformed (ev);

                                                         }

                                                     }

                                                 });

                    this.hideTimer.setRepeats (false);

                } else {

                    this.hideTimer.stop ();

                    this.hideTimer = null;

                }

            }

        }

    }

    /**
     * Immediately hides the popup, all timers are stopped.
     */
    public void hidePopup ()
    {

        if (this.showTimer != null)
        {

            this.showTimer.stop ();
        }

        if (this.hideTimer != null)
        {

            this.hideTimer.stop ();

        }

        this.setVisible (false);

        if (this.getParent () != null)
        {

            this.viewer.removePopup (this);

        }

        this.showTimer = null;
        this.hideTimer = null;

    }

    /**
     * Immediately shows the popup at the specified point.  {@link getContent()} is called to get the content of the
     * popup.
     *
     * @param showAt Show at the specified point.
     */
    public void show (Point showAt)
    {

        this.removeAll ();

        Box content = new Box (BoxLayout.Y_AXIS);
        UIUtils.setPadding (content, 5, 5, 5, 5);
        content.add (this.getContent ());

        this.add (content);

        // For some reason we need to set the size manually.
        content.setPreferredSize (new Dimension (310,
                                                 content.getPreferredSize ().height));

        this.viewer.showPopupAt (this,
                                 showAt,
                                 true);

    }

}
