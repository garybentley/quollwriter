package com.quollwriter.ui;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.charts.*;

import com.quollwriter.ui.components.Accordion;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.QPopup;

public class Targets<E extends AbstractViewer> extends Accordion
{

    private E viewer = null;
    private JLabel chaptersOverDisplay = null;
    private JLabel readabilityOverDisplay = null;

    public Targets (E v)
    {

        super (BoxLayout.Y_AXIS);

        this.viewer = v;

    }

    public void init ()
    {

        final Targets _this = this;

        final TargetsData userTargets = Environment.getUserTargets ();

        FormLayout   fl = new FormLayout ("right:55px, 5px, [p,80px], 5px, p:grow",
                                          "p, 6px, p, 6px, p, 6px, p");
        PanelBuilder pb = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int r = 1;

        pb.addLabel ("Session",
                    cc.xy (1,
                           r));

        int t = userTargets.getMySessionWriting ();

        SpinnerNumberModel ssm = new SpinnerNumberModel (t,
                                                         0,
                                                         100000,
                                                         10);

        final JSpinner ssp = new JSpinner (ssm);

        ssp.addChangeListener (new ChangeListener ()
        {

            @Override
            public void stateChanged (ChangeEvent ev)
            {

                Number n = (Number) ssp.getValue ();

                userTargets.setMySessionWriting (n.intValue ());

                Environment.saveUserTargets ();

            }

        });

        pb.add (ssp,
               cc.xy (3,
                      r));
        pb.addLabel ("words",
                     cc.xy (5,
                            r));

        r += 2;

        pb.addLabel ("Daily",
                    cc.xy (1,
                           r));

        t = userTargets.getMyDailyWriting ();

        SpinnerNumberModel dsm = new SpinnerNumberModel (t,
                                                         0,
                                                         100000,
                                                         10);

        final JSpinner dsp = new JSpinner (dsm);

        dsp.addChangeListener (new ChangeListener ()
        {

            @Override
            public void stateChanged (ChangeEvent ev)
            {

                Number n = (Number) dsp.getValue ();

                userTargets.setMyDailyWriting (n.intValue ());

                Environment.saveUserTargets ();

            }

        });

        pb.add (dsp,
               cc.xy (3,
                      r));
        pb.addLabel ("words",
                     cc.xy (5,
                            r));

        r += 2;

        pb.addLabel ("Weekly",
                    cc.xy (1,
                           r));

        t = userTargets.getMyWeeklyWriting ();

        SpinnerNumberModel wsm = new SpinnerNumberModel (t,
                                                         0,
                                                         100000,
                                                         50);

        final JSpinner wsp = new JSpinner (wsm);

        wsp.addChangeListener (new ChangeListener ()
        {

            @Override
            public void stateChanged (ChangeEvent ev)
            {

                Number n = (Number) wsp.getValue ();

                userTargets.setMyWeeklyWriting (n.intValue ());

                Environment.saveUserTargets ();

            }

        });

        pb.add (wsp,
                cc.xy (3,
                       r));
        pb.addLabel ("words",
                     cc.xy (5,
                            r));

        r += 2;

        pb.addLabel ("Monthly",
                    cc.xy (1,
                           r));

        t = userTargets.getMyMonthlyWriting ();

        SpinnerNumberModel msm = new SpinnerNumberModel (t,
                                                         0,
                                                         100000,
                                                         100);

        final JSpinner msp = new JSpinner (msm);

        msp.addChangeListener (new ChangeListener ()
        {

            @Override
            public void stateChanged (ChangeEvent ev)
            {

                Number n = (Number) msp.getValue ();

                userTargets.setMyMonthlyWriting (n.intValue ());

                Environment.saveUserTargets ();

            }

        });

        pb.add (msp,
                cc.xy (3,
                       r));
        pb.addLabel ("words",
                     cc.xy (5,
                            r));

        JPanel p = pb.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        OptionsBox wcb = new OptionsBox (this.viewer);
        wcb.setBorder (UIUtils.createPadding (10, 5, 10, 0));

        wcb.addMain (p);

        final JCheckBox sessWarn = UIUtils.createCheckBox ("Show a message when a target is reached");

        wcb.addMain (sessWarn);

        sessWarn.setSelected (userTargets.isShowMessageWhenSessionTargetReached ());

        sessWarn.addActionListener (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                userTargets.setShowMessageWhenSessionTargetReached (sessWarn.isSelected ());

                Environment.saveUserTargets ();

            }

        });

        final JLabel history = UIUtils.createClickableLabel ("View Detail",
                                                             Environment.getIcon ("chart",
                                                                                  Constants.ICON_MENU),
                                                             new ActionListener ()
                                                             {

                                                                @Override
                                                                public void actionPerformed (ActionEvent ev)
                                                                {

                                                                    try
                                                                    {

                                                                        _this.viewer.showChart (SessionWordCountChart.CHART_TYPE);

                                                                    } catch (Exception e) {

                                                                        Environment.logError ("Unable to show session word counts",
                                                                                              e);

                                                                        UIUtils.showErrorMessage (_this.viewer,
                                                                                                  "Unable to show chart.");

                                                                    }

                                                                }

                                                             });

        wcb.addMain (history);

        Accordion.Item wo = this.add (this.createHeader ("My Writing",
                                                        Constants.EDIT_ICON_NAME),
                                       null,
                                       wcb,
                                       UIUtils.createHelpTextPane ("Set yourself daily/weekly/session writing targets.",
                                                                   this.viewer));

        // Chapter Word Counts

        if (this.viewer instanceof AbstractProjectViewer)
        {

            final AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

            final TargetsData projTargets = pv.getProjectTargets ();

            fl = new FormLayout ("right:55px, 5px, [p,80px], 5px, p:grow",
                                 "p");

            pb = new PanelBuilder (fl);

            cc = new CellConstraints ();

            r = 1;

            pb.addLabel ("Maximum",
                         cc.xy (1,
                                r));

            SpinnerNumberModel mwc = new SpinnerNumberModel (projTargets.getMaxChapterCount (),
                                                             0,
                                                             1000000,
                                                             50);

            final JSpinner mwsp = new JSpinner (mwc);

            mwsp.addChangeListener (new ChangeListener ()
            {

                @Override
                public void stateChanged (ChangeEvent ev)
                {

                    Number n = (Number) mwsp.getValue ();

                    projTargets.setMaxChapterCount (n.intValue ());

                    pv.saveProjectTargets ();

                    _this.update ();

                }

            });

            pb.add (mwsp,
                   cc.xy (3,
                          r));
            pb.addLabel ("words",
                         cc.xy (5,
                                r));

            p = pb.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (Component.LEFT_ALIGNMENT);

            wcb = new OptionsBox (this.viewer);
            wcb.setBorder (UIUtils.createPadding (10, 5, 10, 0));

            final JCheckBox warn = UIUtils.createCheckBox ("Show a warning when a {chapter} exceeds the maximum");

            warn.setSelected (projTargets.isShowMessageWhenMaxChapterCountExceeded ());

            warn.addActionListener (new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    projTargets.setShowMessageWhenMaxChapterCountExceeded (warn.isSelected ());

                    pv.saveProjectTargets ();

                }

            });

            wcb.addMain (p);

            wcb.addMain (warn);

            this.chaptersOverDisplay = UIUtils.createClickableLabel (null,
                                                                        Environment.getIcon (Constants.ERROR_ICON_NAME,
                                                                                             Constants.ICON_CLICKABLE_LABEL),
                                                     new ActionListener ()
                                                     {

                                                        @Override
                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            Targets.showChaptersOverWordTarget ((AbstractProjectViewer) _this.viewer,
                                                                                                _this.chaptersOverDisplay);

                                                        }

                                                     });


            wcb.addMain (this.chaptersOverDisplay);

            final JLabel wchistory = UIUtils.createClickableLabel ("View Detail",
                                                                   Environment.getIcon ("chart",
                                                                                        Constants.ICON_MENU),
                                                                   new ActionListener ()
                                                                   {

                                                                        @Override
                                                                        public void actionPerformed (ActionEvent ev)
                                                                        {

                                                                            try
                                                                            {

                                                                                _this.viewer.showChart (PerChapterWordCountsChart.CHART_TYPE);

                                                                            } catch (Exception e) {

                                                                                Environment.logError ("Unable to show per chapter word counts",
                                                                                                      e);

                                                                                UIUtils.showErrorMessage (_this.viewer,
                                                                                                          "Unable to show chart.");

                                                                            }

                                                                        }

                                                                   });

            wcb.addMain (wchistory);

            Accordion.Item wc = this.add (this.createHeader ("{Chapter} Word Count",
                                                            Chapter.OBJECT_TYPE),
                                           null,
                                           wcb,
                                           UIUtils.createHelpTextPane ("Specify the maximum number of words a chapter should have.",
                                                                       this.viewer));

            fl = new FormLayout ("right:80px, 5px, [p,80px], 5px, p:grow",
                                 "p, 6px, p");

            pb = new PanelBuilder (fl);

            cc = new CellConstraints ();

            r = 1;

            pb.addLabel ("Flesch-Kincaid",
                         cc.xy (1,
                                r));

            SpinnerNumberModel fkm = new SpinnerNumberModel (projTargets.getReadabilityFK (),
                                                             0,
                                                             30,
                                                             1);

            final JSpinner fksp = new JSpinner (fkm);

            fksp.addChangeListener (new ChangeListener ()
            {

                @Override
                public void stateChanged (ChangeEvent ev)
                {

                    Number n = (Number) fksp.getValue ();

                    projTargets.setReadabilityFK (n.intValue ());

                    pv.saveProjectTargets ();

                    _this.update ();

                }

            });

            pb.add (fksp,
                    cc.xy (3,
                           r));

            r += 2;

            pb.addLabel ("Gunning Fog",
                         cc.xy (1,
                                r));

            SpinnerNumberModel gfm = new SpinnerNumberModel (projTargets.getReadabilityGF (),
                                                             0,
                                                             20,
                                                             1);

            final JSpinner gfsp = new JSpinner (gfm);

            gfsp.addChangeListener (new ChangeListener ()
            {

                @Override
                public void stateChanged (ChangeEvent ev)
                {

                    Number n = (Number) gfsp.getValue ();

                    projTargets.setReadabilityGF (n.intValue ());

                    pv.saveProjectTargets ();

                    _this.update ();

                }

            });

            pb.add (gfsp,
                    cc.xy (3,
                           r));

            p = pb.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (Component.LEFT_ALIGNMENT);

            OptionsBox rb = new OptionsBox (this.viewer);
            rb.setBorder (UIUtils.createPadding (10, 5, 10, 0));

            rb.addMain (p);

            this.readabilityOverDisplay = UIUtils.createClickableLabel (null,
                                                                     Environment.getIcon (Constants.ERROR_ICON_NAME,
                                                                                          Constants.ICON_CLICKABLE_LABEL),
                                                     new ActionListener ()
                                                     {

                                                        @Override
                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            Targets.showChaptersOverReadabilityTarget ((AbstractProjectViewer) _this.viewer,
                                                                                                       _this.readabilityOverDisplay);

                                                        }

                                                     });

            rb.addMain (this.readabilityOverDisplay);

            final JLabel rhistory = UIUtils.createClickableLabel ("View Detail",
                                                                  Environment.getIcon ("chart",
                                                                                       Constants.ICON_MENU),
                                                                   new ActionListener ()
                                                                   {

                                                                        @Override
                                                                        public void actionPerformed (ActionEvent ev)
                                                                        {

                                                                            try
                                                                            {

                                                                                _this.viewer.showChart (ReadabilityIndicesChart.CHART_TYPE);

                                                                            } catch (Exception e) {

                                                                                Environment.logError ("Unable to show readability chart",
                                                                                                      e);

                                                                                UIUtils.showErrorMessage (_this.viewer,
                                                                                                          "Unable to show chart.");

                                                                            }

                                                                        }

                                                                   });

            rb.addMain (rhistory);

            Accordion.Item ri = this.add (this.createHeader ("Readability",
                                                            Constants.PROBLEM_FINDER_ICON_NAME),
                                          null,
                                          rb,
                                          UIUtils.createHelpTextPane ("Specify the maximum readability {chapters} should have.",
                                                                      this.viewer));

            this.checkReadability ();
            this.checkChapters ();

            this.viewer.schedule (new Runnable ()
            {

                @Override
                public void run ()
                {

                    _this.update ();

                }

            },
            1 * 1000,
            2 * 1000);

        }

    }

    public static void showChaptersOverWordTarget (final AbstractProjectViewer viewer,
                                                   final JComponent            showPopupAt)
    {

        Set<Chapter> chaps = viewer.getChaptersOverWordTarget ();

        if (chaps.size () == 0)
        {

            return;

        }

        TargetsData projTargets = viewer.getProjectTargets ();

        int tcc = projTargets.getMaxChapterCount ();

        String cols = "max(150px;p), 6px, right:max(40px;p), 6px, right:max(40px;p)";

        StringBuilder rb = new StringBuilder ();

        for (int i = 0; i < chaps.size (); i++)
        {

            rb.append ("top:p");

            if (i < chaps.size () - 1)
            {

                rb.append (", 6px, ");

            }

        }

        String rows = rb.toString ();

        FormLayout   fl = new FormLayout (cols,
                                          rows);
        PanelBuilder b = new PanelBuilder (fl);
        b.border (Borders.DIALOG);

        CellConstraints cc = new CellConstraints ();

        int r = 1;

        boolean hasOver25 = false;

        for (Chapter c : chaps)
        {

            final Chapter _c = c;

            JLabel l = UIUtils.createClickableLabel (c.getName (),
                                                     null,
                                                     new ActionListener ()
                                                     {

                                                        @Override
                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            viewer.viewObject (_c);

                                                        }

                                                     });

            l.setToolTipText (String.format ("Click to view the {%s}",
                                             Chapter.OBJECT_TYPE));

            ChapterCounts count = viewer.getChapterCounts (c);

            int diff = count.wordCount - tcc;

            int perc = Environment.getPercent (diff, tcc);

            JLabel diffl = null;

            if (perc >= 25)
            {

                diffl = UIUtils.createErrorLabel (String.format ("+%s",
                                                                 Environment.formatNumber (count.wordCount - tcc)));

                diffl.setIcon (null);

                hasOver25 = true;

            } else {

                diffl = UIUtils.createLabel (String.format ("+%s",
                                                            Environment.formatNumber (count.wordCount - tcc)));

            }

            b.add (diffl,
                   cc.xy (5, r));

            b.addLabel (String.format ("%s",
                                       Environment.formatNumber (count.wordCount)),
                        cc.xy (3, r));

            b.add (l,
                   cc.xy (1, r));

            r += 2;

        }

        JPanel p = b.getPanel ();
        p.setOpaque (false);
        p.setBorder (UIUtils.createPadding (5, 5, 10, 10));
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        Box bb = new Box (BoxLayout.Y_AXIS);

        JTextPane t = UIUtils.createHelpTextPane (String.format ("Current maximum {chapter} word count <b>%s words</b>.",
                                                                 Environment.formatNumber (tcc)),
                                                  viewer);

        bb.add (t);

        if (hasOver25)
        {

            JTextPane helpT = UIUtils.createHelpTextPane ("{Chapters} in red are 25% over the target, consider splitting them into two.",
                                                          viewer);

            bb.add (helpT);

        }

        bb.add (p);

        JButton cb = UIUtils.createButton ("Close",
                                           null);

        JButton[] buts = { cb };

        JComponent bs = UIUtils.createButtonBar2 (buts,
                                                  Component.CENTER_ALIGNMENT);
        bs.setAlignmentX (Component.LEFT_ALIGNMENT);

        bb.add (bs);

        bb.setBorder (UIUtils.createPadding (10, 10, 10, 5));

        QPopup popup = UIUtils.createPopup ("{Chapters} over target word count",
                                            Constants.WORDCOUNT_ICON_NAME,
                                            bb,
                                            true,
                                            null);

        cb.addActionListener (popup.getCloseAction ());
        popup.setRemoveOnClose (true);

        viewer.showPopupAt (popup,
                            showPopupAt,
                            true);

        popup.setDraggable (viewer);

    }

    public static void showChaptersOverReadabilityTarget (final AbstractProjectViewer viewer,
                                                          final JComponent            showPopupAt)
    {

        Set<Chapter> chaps = viewer.getChaptersOverReadabilityTarget ();

        if (chaps.size () == 0)
        {

            return;

        }

        TargetsData projTargets = viewer.getProjectTargets ();

        int tcc = projTargets.getMaxChapterCount ();
        int tfk = projTargets.getReadabilityFK ();
        int tgf = projTargets.getReadabilityGF ();

        // FK over, FK, GF over, GF, chapter
        String cols = "max(150px;p), 6px, right:max(40px;p), 6px, right:max(40px;p), 6px, right:max(40px;p), 6px, right:max(40px;p)";

        StringBuilder rb = new StringBuilder ("top:p, 6px, ");

        for (int i = 0; i < chaps.size (); i++)
        {

            rb.append ("top:p");

            if (i < chaps.size () - 1)
            {

                rb.append (", 6px, ");

            }

        }

        String rows = rb.toString ();

        FormLayout   fl = new FormLayout (cols,
                                          rows);
        PanelBuilder b = new PanelBuilder (fl);
        b.border (Borders.DIALOG);

        CellConstraints cc = new CellConstraints ();

        int r = 1;

        b.addLabel ("FK",
                    cc.xy (3, r));

        b.addLabel ("GF",
                    cc.xy (7, r));

        r += 2;

        for (Chapter c : chaps)
        {

            final Chapter _c = c;

            JLabel l = UIUtils.createClickableLabel (c.getName (),
                                                     null,
                                                     new ActionListener ()
                                                     {

                                                        @Override
                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            viewer.viewObject (_c);

                                                        }

                                                     });

            l.setToolTipText (String.format ("Click to view the {%s}",
                                             Chapter.OBJECT_TYPE));

            ReadabilityIndices ri = viewer.getReadabilityIndices (c);

            float fk = ri.getFleschKincaidGradeLevel ();
            float gf = ri.getGunningFogIndex ();

            float diffFK = fk - tfk;
            float diffGF = gf - tgf;

            JLabel diffFKl = null;

            if (diffFK > 0)
            {

                diffFKl = UIUtils.createLabel (String.format ("+%s",
                                                              Environment.formatNumber (diffFK)));

            } else {

                diffFKl = UIUtils.createLabel ("-");


            }

            JLabel diffGFl = null;

            if (diffGF > 0)
            {

                diffGFl = UIUtils.createLabel (String.format ("+%s",
                                                              Environment.formatNumber (diffGF)));

            } else {

                diffGFl = UIUtils.createLabel ("-");


            }

            b.add (diffFKl,
                   cc.xy (5, r));

            b.addLabel (String.format ("%s",
                                       Environment.formatNumber (fk)),
                        cc.xy (3, r));

            b.add (diffGFl,
                   cc.xy (9, r));

            b.addLabel (String.format ("%s",
                                       Environment.formatNumber (gf)),
                        cc.xy (7, r));

            b.add (l,
                   cc.xy (1, r));

            r += 2;

        }

        JPanel p = b.getPanel ();
        p.setOpaque (false);
        p.setBorder (UIUtils.createPadding (10, 15, 15, 10));
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        Box bb = new Box (BoxLayout.Y_AXIS);

        bb.add (p);

        JButton cb = UIUtils.createButton ("Close",
                                           null);

        JButton[] buts = { cb };

        JComponent bs = UIUtils.createButtonBar2 (buts,
                                                  Component.CENTER_ALIGNMENT);
        bs.setAlignmentX (Component.LEFT_ALIGNMENT);

        bb.add (bs);

        bb.setBorder (UIUtils.createPadding (10, 10, 10, 5));

        QPopup popup = UIUtils.createPopup ("{Chapters} over readability target",
                                            Constants.PROBLEM_FINDER_ICON_NAME,
                                            bb,
                                            true,
                                            null);

        cb.addActionListener (popup.getCloseAction ());
        popup.setRemoveOnClose (true);

        viewer.showPopupAt (popup,
                            showPopupAt,
                            true);

        popup.setDraggable (viewer);

    }

    private void update ()
    {

        this.checkChapters ();
        this.checkReadability ();

    }

    private void checkReadability ()
    {

        final AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

        if (pv.getProject () == null)
        {

            // Closing down.
            return;

        }

        final TargetsData projTargets = pv.getProjectTargets ();

        Set<Chapter> chaps = pv.getChaptersOverReadabilityTarget ();

        final Targets _this = this;

        final int _cc = chaps.size ();

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                _this.readabilityOverDisplay.setVisible (_cc > 0);

                if (_cc > 0)
                {

                    _this.readabilityOverDisplay.setText (String.format ("%s {%s%s} exceed%s the target, click to view %s.",
                                                                         Environment.formatNumber (_cc),
                                                                         Chapter.OBJECT_TYPE,
                                                                         (_cc > 1 ? "s" : ""),
                                                                         (_cc > 1 ? "" : "s"),
                                                                         (_cc > 1 ? "them" : "it")));


                }

            }

        });

    }

    private void checkChapters ()
    {

        final AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

        final TargetsData projTargets = pv.getProjectTargets ();

        if (projTargets == null)
        {

            return;

        }

        final Targets _this = this;

        final int cc = pv.getChaptersOverWordTarget ().size ();

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                _this.chaptersOverDisplay.setVisible (cc > 0);

                if (cc > 0)
                {

                    _this.chaptersOverDisplay.setText (String.format ("%s {%s%s} exceed%s the target, click to view %s.",
                                                                      Environment.formatNumber (cc),
                                                                      Chapter.OBJECT_TYPE,
                                                                      (cc > 1 ? "s" : ""),
                                                                      (cc > 1 ? "" : "s"),
                                                                      (cc > 1 ? "them" : "it")));


                }

            }

        });

    }

    private Header createHeader (String title,
                                 String iconType)
    {

        Header h = UIUtils.createHeader (Environment.replaceObjectNames (title),
                                         Constants.SUB_PANEL_TITLE,
                                         iconType,
                                         null);

        h.setBorder (UIUtils.createBottomLineWithPadding (0, 0, 3, 0));

        return h;

    }

}
