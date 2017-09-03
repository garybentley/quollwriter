package com.quollwriter.text;

import java.awt.*;
import java.awt.event.*;

import java.util.List;
import java.util.ArrayList;

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

            JEditorPane desc = UIUtils.createHelpTextPane (d + Environment.getUIString (LanguageStrings.problemfinder,
                                                                                        LanguageStrings.ignore,
                                                                                        LanguageStrings.checkbox),
                                                           //"Check the box to ignore this problem.",
                                                           null);
            desc.setBorder (UIUtils.createPadding (0, 10, 0, 10));
            desc.setSize (new Dimension (380,
                                         Short.MAX_VALUE));

            desc.setAlignmentX (Component.LEFT_ALIGNMENT);

            b.add (desc);
            b.setBorder (UIUtils.createPadding (5, 5, 5, 5));

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
    private ProjectViewer viewer = null;

    public IgnoreCheckbox (final String           s,
                           final Issue            i,
                           final QuollEditorPanel qp,
                           final ProjectViewer    viewer)
    {

        super (s);

        this.issue = i;

        this.setText (s);

        this.qp = qp;
        this.viewer = viewer;

        final IgnoreCheckbox _this = this;

        final IgnoreCheckboxToolTip t = new IgnoreCheckboxToolTip (_this.issue);

        final Timer show = new Timer (ToolTipManager.sharedInstance ().getInitialDelay () * 2,
                                      new ActionAdapter ()
                                      {

                                          public void actionPerformed (ActionEvent ev)
                                          {

                                              qp.getViewer ().addPopup (t,
                                                                        true,
                                                                        false);

                                              Point po = SwingUtilities.convertPoint (_this,
                                                                                      30,
                                                                                      0 - _this.getPreferredSize ().height - t.getPreferredSize ().height - 10,
                                                                                      qp.getViewer ());

                                              qp.getViewer ().showPopupAt (t,
                                                                           po,
                                                                           true);

                                          }

                                      });
        show.setRepeats (false);

        final Timer hide = new Timer (250,//ToolTipManager.sharedInstance ().getInitialDelay (),
                                      new ActionAdapter ()
                                      {

                                          public void actionPerformed (ActionEvent ev)
                                          {

                                              t.setVisible (false);

                                              qp.getViewer ().removePopup (t);

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

            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {
/*
                m.add (UIUtils.createMenuItem ("Find next problem of this type",
                                               Constants.NEXT_ICON_NAME,
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                       _this.qp.getViewer ().showProblemFinderRuleSideBar (_this.issue.getRule ());

                                                    }

                                               }));
*/

                List<String> prefix = new ArrayList ();
                prefix.add (LanguageStrings.problemfinder);
                prefix.add (LanguageStrings.ignore);
                prefix.add (LanguageStrings.popupmenu);
                prefix.add (LanguageStrings.items);

                m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                        LanguageStrings.find),
                                               //"Find all problems of this type",
                                               Constants.FIND_ICON_NAME,
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                       _this.viewer.showProblemFinderRuleSideBar (_this.issue.getRule ());

                                                    }

                                               }));

                m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                        LanguageStrings.ignore),
                                                                        //"Ignore this type of problem",
                                               Constants.ERROR_ICON_NAME,
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        ProblemFinderRuleConfig.confirmRuleRemoval (qp.getViewer (),
                                                                                                    _this.issue.getRule (),
                                                                                                    _this.viewer.getProject ().getProperties (),
                                                                                                    new ActionListener ()
                                                        {

                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                _this.qp.removeIgnoreCheckboxesForRule (_this.issue.getRule ());

                                                            }

                                                        });

                                                    }

                                               }));

                m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                        LanguageStrings.edit),
                                               //"Edit this rule",
                                               Constants.EDIT_ICON_NAME,
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.qp.showProblemFinderRuleConfig ();

                                                        _this.viewer.getProblemFinderRuleConfig ().editRule (_this.issue.getRule (),
                                                                                                             false);

                                                    }

                                               }));

            }

            @Override
            public void mouseExited (MouseEvent ev)
            {

                show.stop ();
                hide.start ();

            }

            @Override
            public void mouseEntered (MouseEvent ev)
            {

                show.start ();
                hide.stop ();

            }

        });

    }

    @Override
    public void setText (String t)
    {

        super.setText (String.format ("<html>%s</html>",
                                      Environment.replaceObjectNames (t)));

    }

}
