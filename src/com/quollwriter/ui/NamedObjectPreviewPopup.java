package com.quollwriter.ui;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Rectangle;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.components.ActionAdapter;

public class NamedObjectPreviewPopup extends Box
{

    private AbstractProjectViewer viewer = null;
    private Timer showTimer = null;
    private Timer hideTimer = null;
    private boolean hidden = false;

    public NamedObjectPreviewPopup (AbstractProjectViewer viewer)
    {

        super (BoxLayout.Y_AXIS);

        this.viewer = viewer;

        //this.setOpaque (false);

        this.setMaximumSize (new Dimension (380, Short.MAX_VALUE));

        this.setOpaque (true);
        this.setBackground (UIUtils.getComponentColor ());
        this.setBorder (new CompoundBorder (com.quollwriter.ui.components.UIUtils.internalPanelDropShadow,
                                            UIUtils.createLineBorder ()));

        final NamedObjectPreviewPopup _this = this;

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

    public void show (final NamedObject obj,
                      final int         showDelay,
                      final int         hideDelay,
                      final Point       po,
                      final ActionListener onHide)
    {

        if (obj == null)
        {

            return;

        }

        final NamedObjectPreviewPopup _this = this;

        if (showDelay > 0)
        {

            if (this.showTimer != null)
            {

                this.showTimer.stop ();

            }

            this.showTimer = new Timer (showDelay,
                            new ActionAdapter ()
                            {

                                 public void actionPerformed (ActionEvent ev)
                                 {

                                     _this.show (obj,
                                                 po);

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

    public void show (NamedObject obj,
                      Point       showAt)
    {

        this.removeAll ();

        Box content = new Box (BoxLayout.Y_AXIS);
        UIUtils.setPadding (content, 5, 5, 5, 5);
        this.add (content);

        // TODO: Make this nicer later.
        if (obj instanceof Chapter)
        {

            Chapter c = (Chapter) obj;

            content.add (UIUtils.getChapterInfoPreview (c,
                                                        null,
                                                        this.viewer));

        } else {

            String firstLine = "<b><i>No description.</i></b>";

            String t = (obj.getDescription () != null ? obj.getDescription ().getText () : null);

            if ((t != null)
                &&
                (t.length () > 0)
               )
            {

                firstLine = new Paragraph (t, 0).getFirstSentence ().getText ();

            }

            JEditorPane desc = UIUtils.createHelpTextPane (t,
                                                           null);

            FormLayout fl = new FormLayout ("380px",
                                            "p");

            PanelBuilder pb = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            pb.add (desc, cc.xy (1, 1));

            desc.setAlignmentX (Component.LEFT_ALIGNMENT);

            JPanel p = pb.getPanel ();
            p.setOpaque (true);
            p.setBackground (UIUtils.getComponentColor ());

            p.setBorder (new EmptyBorder (3,
                                          3,
                                          3,
                                          3));

            content.add (p);

        }

        this.viewer.showPopupAt (this,
                                 showAt,
                                 true);

    }

    public static JComponent getObjectPreviewPanel (NamedObject obj)
    {

        if (obj instanceof Chapter)
        {

            Chapter c = (Chapter) obj;



        }

        return null;

    }

}
