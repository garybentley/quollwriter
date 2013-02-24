package com.quollwriter.text;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;

import com.quollwriter.ui.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.panels.*;

public class IgnoreCheckbox extends JCheckBox
{

    public class IgnoreCheckboxToolTip extends Box
    {

        public IgnoreCheckboxToolTip(Issue i)
        {

            super (BoxLayout.Y_AXIS);

            this.setMaximumSize (new java.awt.Dimension (400,
                                                         Short.MAX_VALUE));

            Box b = new Box (BoxLayout.Y_AXIS);

            this.setOpaque (false);
            this.add (b);

            String d = i.getRule ().getDescription ();

            if (d.length () > 0)
            {
                
                d = d + "<br /><br />";
                
            }

            JEditorPane desc = UIUtils.createHelpTextPane (d + "Check the box to ignore this problem.");
            desc.setBorder (new EmptyBorder (0,
                                             10,
                                             0,
                                             10));
            desc.setSize (new Dimension (380,
                                         Short.MAX_VALUE));

            desc.setAlignmentX (Component.LEFT_ALIGNMENT);

            b.add (desc);
            b.setBorder (new EmptyBorder (5,
                                          5,
                                          5,
                                          5));

            this.setBorder (new CompoundBorder (com.quollwriter.ui.components.UIUtils.internalPanelDropShadow,
                                                UIUtils.createLineBorder ()));
            b.setOpaque (true);
            b.setBackground (UIUtils.getColor ("f5fcfe"));

            this.setPreferredSize (new java.awt.Dimension (400,
                                                           this.getPreferredSize ().height));

        }

    }

    public Issue             issue = null;
    private QuollEditorPanel qp = null;

    public IgnoreCheckbox(String                 s,
                          Issue                  i,
                          final QuollEditorPanel qp)
    {

        super (s);

        this.issue = i;

        this.qp = qp;

        final IgnoreCheckbox _this = this;

        final IgnoreCheckboxToolTip t = new IgnoreCheckboxToolTip (_this.issue);

        final Timer show = new Timer (ToolTipManager.sharedInstance ().getInitialDelay () * 2,
                                      new ActionAdapter ()
                                      {

                                          public void actionPerformed (ActionEvent ev)
                                          {

                                              qp.getProjectViewer ().addPopup (t,
                                                                               true,
                                                                               false);

                                              Point po = SwingUtilities.convertPoint (_this,
                                                                                      30,
                                                                                      0 - _this.getPreferredSize ().height - t.getPreferredSize ().height - 10,
                                                                                      qp.getProjectViewer ());

                                              qp.getProjectViewer ().showPopupAt (t,
                                                                                  po);

                                          }

                                      });
        show.setRepeats (false);

        final Timer hide = new Timer (ToolTipManager.sharedInstance ().getInitialDelay (),
                                      new ActionAdapter ()
                                      {

                                          public void actionPerformed (ActionEvent ev)
                                          {

                                              t.setVisible (false);

                                              qp.getProjectViewer ().removePopup (t);

                                          }

                                      });
        hide.setRepeats (false);

        t.addMouseListener (new MouseAdapter ()
            {

                public void mouseEntered (MouseEvent ev)
                {

                    hide.stop ();

                }

                public void mouseExited (MouseEvent ev)
                {

                    Point p = new Point (0,
                                         0);

                    SwingUtilities.convertPointToScreen (p,
                                                         t);

                    Rectangle tBounds = t.getBounds (null);

                    tBounds.x = p.x;
                    tBounds.y = p.y;

                    if (!tBounds.contains (ev.getLocationOnScreen ()))
                    {

                        hide.start ();

                    }

                }

            });

        this.addMouseListener (new MouseEventHandler ()
            {

                public void handlePress (MouseEvent ev)
                {

                    if (ev.isPopupTrigger ())
                    {

                        final JPopupMenu m = new JPopupMenu ();

                        m.add (UIUtils.createMenuItem ("Ignore this type of problem",
                                                       null,
                                                       new ActionAdapter ()
                                                       {
                            
                                                            public void actionPerformed (ActionEvent ev)
                                                            {
                            
                                                                if (ProblemFinderRuleConfig.confirmRuleRemoval (qp,
                                                                                                                _this.issue.getRule (),
                                                                                                                _this.qp.getProjectViewer ().getProject ().getProperties ()))
                                                                {
                            
                                                                    _this.qp.removeIgnoreCheckboxesForRule (_this.issue.getRule ());
                            
                                                                }
                            
                                                            }
                            
                                                       }));

                        m.add (UIUtils.createMenuItem ("Edit this rule",
                                                       null,
                                                       new ActionAdapter ()
                                                       {
                            
                                                            public void actionPerformed (ActionEvent ev)
                                                            {
                            
                                                                _this.qp.showProblemFinderRuleConfig ();
                            
                                                                _this.qp.getProblemFinderRuleConfig ().editRule (_this.issue.getRule (),
                                                                                                                 false);
                            
                                                            }
                            
                                                       }));

                        m.show ((Component) ev.getSource (),
                                ev.getX (),
                                ev.getY ());

                    }

                }

                public void mouseExited (MouseEvent ev)
                {

                    show.stop ();
                    hide.start ();

                }

                public void mouseEntered (MouseEvent ev)
                {

                    show.start ();
                    hide.stop ();

                }

            });

    }

}
