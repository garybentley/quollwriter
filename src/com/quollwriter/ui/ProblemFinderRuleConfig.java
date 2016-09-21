package com.quollwriter.ui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;

import java.net.*;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.text.*;
import com.quollwriter.text.rules.*;

import com.quollwriter.ui.components.*;

import org.josql.*;


public class ProblemFinderRuleConfig extends ScrollableBox implements ProjectEventListener
{

    private class RuleWrapper
    {

        public Rule rule = null;

        public RuleWrapper(Rule r)
        {

            this.rule = r;

        }

        public String toString ()
        {

            return this.rule.getSummary ();

        }
    }

    private class RuleBox extends Box
    {

        public Rule rule = null;

        private JTextPane desc = null;
        private JTextPane info = null;
        private JToolBar buttons = null;

        public RuleBox(final Rule                    rule)
        {

            super (BoxLayout.Y_AXIS);

            this.rule = rule;

        }

        public void init (final ProblemFinderRuleConfig conf)
        {

            final RuleBox _this = this;

            final Box main = new Box (BoxLayout.X_AXIS);

            this.add (main);

            this.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.setOpaque (false);
            main.setAlignmentX (Component.LEFT_ALIGNMENT);
            main.setOpaque (false);

            this.setBorder (new CompoundBorder (new MatteBorder (0,
                                                                 0,
                                                                 1,
                                                                 0,
                                                                 UIUtils.getInnerBorderColor ()),
                                                new EmptyBorder (5,
                                                                 5,
                                                                 5,
                                                                 5)));

            this.desc = UIUtils.createHelpTextPane (this.rule.getDescription (),
                                                    ProblemFinderRuleConfig.this.projectViewer);
            this.desc.setBorder (null);

            this.info = UIUtils.createHelpTextPane (this.rule.getSummary (),
                                                    ProblemFinderRuleConfig.this.projectViewer);

            main.add (this.info);
            main.add (Box.createHorizontalGlue ());

            this.desc.setVisible (false);
            this.desc.setAlignmentX (Component.LEFT_ALIGNMENT);

            // We use this here so that the "desc" doesn't need a border, if we have a border then it screws
            // up the height when rendered.
            Box descWrap = new Box (BoxLayout.Y_AXIS);
            descWrap.setAlignmentX (Component.LEFT_ALIGNMENT);
            descWrap.add (this.desc);

            descWrap.setBorder (UIUtils.createPadding (0, 10, 0, 10));

            this.add (descWrap);

            java.util.List<JButton> buttons = new ArrayList ();

            buttons.add (UIUtils.createButton (Constants.FIND_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Click to find all problems for this rule",
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        conf.getProjectViewer ().showProblemFinderRuleSideBar (_this.rule);

                                                    }

                                               }));

            buttons.add (UIUtils.createButton (Constants.EDIT_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Click to edit this rule",
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        conf.editRule (_this.rule,
                                                                       false);

                                                    }

                                               }));

            buttons.add (UIUtils.createButton (Constants.INFO_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Click to show/hide the description of this rule",
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.desc.setVisible (!_this.desc.isVisible ());

                                                    }

                                               }));

            buttons.add (UIUtils.createButton (Constants.DELETE_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Click to remove this rule",
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        ProblemFinderRuleConfig.confirmRuleRemoval (conf.getProjectViewer (),
                                                                                                    _this.rule,
                                                                                                    conf.getProjectViewer ().getProject ().getProperties (),
                                                                                                    new ActionListener ()
                                                        {

                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                 conf.removeRuleBox (_this);

                                                                 if (_this.rule.getCategory ().equals (Rule.SENTENCE_CATEGORY))
                                                                 {

                                                                     conf.sentenceControl.setVisible (true);

                                                                 }

                                                                 if (_this.rule.getCategory ().equals (Rule.PARAGRAPH_CATEGORY))
                                                                 {

                                                                     conf.paragraphControl.setVisible (true);

                                                                 }

                                                                 Environment.fireUserProjectEvent (conf,
                                                                                                   ProjectEvent.PROBLEM_FINDER,
                                                                                                   ProjectEvent.REMOVE_RULE,
                                                                                                   _this.rule);

                                                            }

                                                        });

                                                    }

                                               }));

            this.buttons = UIUtils.createButtonBar (buttons);

            //this.buttons.setVisible (false);
            this.buttons.setAlignmentX (Component.LEFT_ALIGNMENT);

            main.add (this.buttons);

        }

        public void update ()
        {

            this.desc.setText (this.rule.getDescription ());
            this.info.setText (this.rule.getSummary ());

        }

    }

    //private QuollEditorPanel editor = null;
    private Box              wordsBox = null;
    private JScrollPane      wordsBoxSP = null;
    private Box              wordsEditBox = null;
    private JPanel           wordsWrapper = null;
    private Box              sentenceBox = null;
    private JScrollPane      sentenceBoxSP = null;
    private Box              sentenceEditBox = null;
    private JPanel           sentenceWrapper = null;
    private Box              sentenceControl = null;
    private Box              paragraphBox = null;
    private JScrollPane      paragraphBoxSP = null;
    private Box              paragraphEditBox = null;
    private JPanel           paragraphWrapper = null;
    private Box              paragraphControl = null;
    private DnDTabbedPane    tabs = null;
    private ProjectViewer projectViewer = null;
    private boolean inited = false;

     public ProblemFinderRuleConfig (ProjectViewer pv)
     {

         super (BoxLayout.Y_AXIS);

         this.projectViewer = pv;

         Environment.addUserProjectEventListener (this);

     }

     public void eventOccurred (ProjectEvent ev)
     {

          if (!ev.getType ().equals (ProjectEvent.PROBLEM_FINDER))
          {

               return;

          }

          if ((!ev.getAction ().equals (ProjectEvent.NEW_RULE))
              &&
              (!ev.getAction ().equals (ProjectEvent.EDIT_RULE))
              &&
              (!ev.getAction ().equals (ProjectEvent.REMOVE_RULE))
             )
          {

              return;

          }

          this.addWordRules ();
          this.addSentenceRules ();
          this.addParagraphRules ();

     }

    private void createSentenceWrapper ()
    {

        final ProblemFinderRuleConfig _this = this;

        this.sentenceBox = new ScrollableBox (BoxLayout.Y_AXIS);
        this.sentenceBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.sentenceBox.setOpaque (false);

        this.sentenceBoxSP = new JScrollPane (this.sentenceBox);
        this.sentenceBoxSP.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.sentenceBoxSP.getViewport ().setOpaque (false);
        this.sentenceBoxSP.setBorder (null);
        this.sentenceBoxSP.setOpaque (false);
        this.sentenceBoxSP.getVerticalScrollBar ().setUnitIncrement (20);
        this.sentenceBoxSP.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.sentenceBoxSP.getViewport ().setPreferredSize (new Dimension (450,
                                                                           400));


        this.sentenceWrapper = new JPanel ();
        this.sentenceWrapper.setLayout (new CardLayout ());
        this.sentenceWrapper.setOpaque (false);

        Box sentenceAll = new Box (BoxLayout.Y_AXIS);
        sentenceAll.setAlignmentX (Component.LEFT_ALIGNMENT);
        sentenceAll.setOpaque (false);

        this.sentenceControl = new Box (BoxLayout.X_AXIS);
        this.sentenceControl.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.sentenceControl.setBorder (new CompoundBorder (new MatteBorder (0,
                                                                             0,
                                                                             1,
                                                                             0,
                                                                             Environment.getBorderColor ()),
                                                            new EmptyBorder (3,
                                                                             3,
                                                                             3,
                                                                             3)));

        List<JButton> buts = new ArrayList ();

        buts.add (UIUtils.createButton (Constants.ADD_ICON_NAME,
                                        Constants.ICON_MENU,
                                        "Click to add a new Sentence Structure rule",
                                        new ActionAdapter ()
                                        {

                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                final JPopupMenu popup = new JPopupMenu ();

                                                List<Rule> igs = _this.getSentenceIgnores ();

                                                for (Rule r : igs)
                                                {

                                                    JMenuItem mi = new JMenuItem ("<html>" + r.getSummary () + "</html>");
                                                    mi.setActionCommand (r.getId ());
                                                    mi.setToolTipText ("Click to add this rule");

                                                    mi.addActionListener (new ActionAdapter ()
                                                        {

                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                Rule r = RuleFactory.getRuleById (ev.getActionCommand ());

                                                                RuleBox rb = new RuleBox (r);

                                                                _this.sentenceBox.add (rb,
                                                                                       0);

                                                                rb.init (_this);

                                                                _this.removeIgnore (r,
                                                                                    (_this.isProjectIgnore (r) ? RuleFactory.PROJECT : RuleFactory.USER));

                                                                _this.repaint ();

                                                            }

                                                        });

                                                    popup.add (mi);

                                                }

                                                Point p = _this.sentenceControl.getMousePosition ();

                                                popup.show (_this.sentenceControl,
                                                            p.x,
                                                            p.y);

                                            }

                                        }));

        this.sentenceControl.add (UIUtils.createButtonBar (buts));

        this.sentenceControl.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                            this.sentenceControl.getPreferredSize ().height));

        sentenceAll.add (this.sentenceControl);

        this.sentenceControl.setVisible (this.getSentenceIgnores ().size () != 0);

        sentenceAll.add (this.sentenceBoxSP);

        this.sentenceWrapper.add (sentenceAll,
                                  "view");

        this.sentenceEditBox = new Box (BoxLayout.Y_AXIS);
        this.sentenceEditBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.sentenceEditBox.setOpaque (false);

        this.sentenceWrapper.add (this.sentenceEditBox,
                                  "edit");

        ((CardLayout) this.sentenceWrapper.getLayout ()).show (this.sentenceWrapper,
                                                               "view");

          this.addSentenceRules ();

    }

    private void addSentenceRules ()
    {

          final int val = this.sentenceBoxSP.getVerticalScrollBar ().getValue ();

          this.sentenceBox.removeAll ();

          List<Rule> senRules = RuleFactory.getSentenceRules ();

          if (senRules != null)
          {

              Map<String, String> ignores = RuleFactory.getIgnores (RuleFactory.ALL,
                                                                    this.projectViewer.getProject ().getProperties ());

              for (Rule r : senRules)
              {

                  if (ignores.containsKey (r.getId ()))
                  {

                      continue;

                  }

                  RuleBox rb = new RuleBox (r);

                  this.sentenceBox.add (rb);

                  rb.init (this);

              }

          }

          this.sentenceBox.add (Box.createVerticalGlue ());

          final ProblemFinderRuleConfig _this = this;

          UIUtils.doLater (new ActionListener ()
          {

              public void actionPerformed (ActionEvent ev)
              {

                  _this.sentenceBoxSP.getVerticalScrollBar ().setValue (val);

              }

          });

    }

    private void createParagraphWrapper ()
    {

        final ProblemFinderRuleConfig _this = this;

        this.paragraphBox = new ScrollableBox (BoxLayout.Y_AXIS);
        this.paragraphBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.paragraphBox.setOpaque (false);

        this.paragraphBoxSP = new JScrollPane (this.paragraphBox);
        this.paragraphBoxSP.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.paragraphBoxSP.getViewport ().setOpaque (false);
        this.paragraphBoxSP.setBorder (null);
        this.paragraphBoxSP.setOpaque (false);
        this.paragraphBoxSP.getVerticalScrollBar ().setUnitIncrement (20);
        this.paragraphBoxSP.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.paragraphBoxSP.getViewport ().setPreferredSize (new Dimension (450,
                                                                            400));


        this.paragraphWrapper = new JPanel ();
        this.paragraphWrapper.setLayout (new CardLayout ());
        this.paragraphWrapper.setOpaque (false);

        Box paragraphAll = new Box (BoxLayout.Y_AXIS);
        paragraphAll.setAlignmentX (Component.LEFT_ALIGNMENT);
        paragraphAll.setOpaque (false);

        this.paragraphControl = new Box (BoxLayout.X_AXIS);
        this.paragraphControl.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.paragraphControl.setBorder (new CompoundBorder (new MatteBorder (0,
                                                                             0,
                                                                             1,
                                                                             0,
                                                                             Environment.getBorderColor ()),
                                                            new EmptyBorder (3,
                                                                             3,
                                                                             3,
                                                                             3)));

        List<JButton> buts = new ArrayList ();

        buts.add (UIUtils.createButton (Constants.ADD_ICON_NAME,
                                        Constants.ICON_MENU,
                                        "Click to add a new Paragraph Structure rule",
                                        new ActionAdapter ()
                                        {

                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                final JPopupMenu popup = new JPopupMenu ();

                                                List<Rule> igs = _this.getParagraphIgnores ();

                                                for (Rule r : igs)
                                                {

                                                    JMenuItem mi = new JMenuItem ("<html>" + r.getSummary () + "</html>");
                                                    mi.setActionCommand (r.getId ());
                                                    mi.setToolTipText ("Click to add this rule");

                                                    mi.addActionListener (new ActionAdapter ()
                                                        {

                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                Rule r = RuleFactory.getRuleById (ev.getActionCommand ());

                                                                RuleBox rb = new RuleBox (r);

                                                                _this.paragraphBox.add (rb,
                                                                                        0);

                                                                rb.init (_this);

                                                                _this.removeIgnore (r,
                                                                                    (_this.isProjectIgnore (r) ? RuleFactory.PROJECT : RuleFactory.USER));

                                                                _this.repaint ();

                                                            }

                                                        });

                                                    popup.add (mi);

                                                }

                                                Point p = _this.paragraphControl.getMousePosition ();

                                                popup.show (_this.paragraphControl,
                                                            p.x,
                                                            p.y);

                                            }

                                        }));

        this.paragraphControl.add (UIUtils.createButtonBar (buts));

        this.paragraphControl.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                             this.paragraphControl.getPreferredSize ().height));

        paragraphAll.add (this.paragraphControl);

        this.paragraphControl.setVisible (this.getParagraphIgnores ().size () != 0);

        paragraphAll.add (this.paragraphBoxSP);

        this.paragraphWrapper.add (paragraphAll,
                                   "view");

        this.paragraphEditBox = new Box (BoxLayout.Y_AXIS);
        this.paragraphEditBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.paragraphEditBox.setOpaque (false);

        this.paragraphWrapper.add (this.paragraphEditBox,
                                   "edit");

        ((CardLayout) this.paragraphWrapper.getLayout ()).show (this.paragraphWrapper,
                                                                "view");

          this.addParagraphRules ();

     }

     private void addParagraphRules ()
     {

          final int val = this.paragraphBoxSP.getVerticalScrollBar ().getValue ();

          this.paragraphBox.removeAll ();

          List<Rule> paraRules = RuleFactory.getParagraphRules ();

          if (paraRules != null)
          {

              Map<String, String> ignores = RuleFactory.getIgnores (RuleFactory.ALL,
                                                                    this.projectViewer.getProject ().getProperties ());

              for (Rule r : paraRules)
              {

                  if (ignores.containsKey (r.getId ()))
                  {

                      continue;

                  }

                  RuleBox rb = new RuleBox (r);
                  this.paragraphBox.add (rb);

                  rb.init (this);

              }

          }

          this.paragraphBox.add (Box.createVerticalGlue ());

          final ProblemFinderRuleConfig _this = this;

          UIUtils.doLater (new ActionListener ()
          {

               @Override
               public void actionPerformed (ActionEvent ev)
               {

                    _this.paragraphBoxSP.getVerticalScrollBar ().setValue (val);
               }

          });

     }

    private void createWordsWrapper ()
    {

        final ProblemFinderRuleConfig _this = this;

        this.wordsBox = new ScrollableBox (BoxLayout.Y_AXIS);
        this.wordsBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.wordsBox.setOpaque (false);
        this.wordsBoxSP = new JScrollPane (this.wordsBox);
        this.wordsBoxSP.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.wordsBoxSP.getViewport ().setOpaque (false);
        this.wordsBoxSP.setBorder (null);
        this.wordsBoxSP.setOpaque (false);
        this.wordsBoxSP.getVerticalScrollBar ().setUnitIncrement (20);
        this.wordsBoxSP.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.wordsWrapper = new JPanel ();
        this.wordsWrapper.setLayout (new CardLayout ());
        this.wordsWrapper.setOpaque (false);
        this.wordsBoxSP.getViewport ().setPreferredSize (new Dimension (450,
                                                                        400));

        Box wordsAll = new Box (BoxLayout.Y_AXIS);
        wordsAll.setAlignmentX (Component.LEFT_ALIGNMENT);
        wordsAll.setOpaque (false);

        Box wordsControl = new Box (BoxLayout.X_AXIS);
        wordsControl.setAlignmentX (Component.LEFT_ALIGNMENT);
        wordsControl.setBorder (new CompoundBorder (new MatteBorder (0,
                                                                     0,
                                                                     1,
                                                                     0,
                                                                     Environment.getBorderColor ()),
                                                    new EmptyBorder (3,
                                                                     3,
                                                                     3,
                                                                     3)));

        List<JButton> buts = new ArrayList ();

        buts.add (UIUtils.createButton (Constants.ADD_ICON_NAME,
                                        Constants.ICON_MENU,
                                        "Click to add a new Words/Phrases rule",
                                        new ActionAdapter ()
                                        {

                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                _this.wordsEditBox.removeAll ();

                                                WordFinder wf = new WordFinder ();
                                                wf.setUserRule (true);

                                                _this.editRule (wf,
                                                                true);

                                                _this.repaint ();

                                            }

                                        }));

        wordsControl.add (UIUtils.createButtonBar (buts));

        wordsControl.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                    wordsControl.getPreferredSize ().height));

        wordsAll.add (wordsControl);

        wordsAll.add (this.wordsBoxSP);

        this.wordsWrapper.add (wordsAll,
                               "view");

        this.wordsEditBox = new Box (BoxLayout.Y_AXIS);
        this.wordsEditBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.wordsEditBox.setOpaque (false);

        this.wordsWrapper.add (this.wordsEditBox,
                               "edit");

        ((CardLayout) this.wordsWrapper.getLayout ()).show (this.wordsWrapper,
                                                            "view");

          this.addWordRules ();

    }

    private void addWordRules ()
    {

          final int val = this.wordsBoxSP.getVerticalScrollBar ().getValue ();

          this.wordsBox.removeAll ();

          // Get all the "word" rules.
          List<Rule> wordRules = RuleFactory.getWordRules ();

          Query q = new Query ();

          try
          {

              q.parse ("SELECT * FROM com.quollwriter.text.rules.WordFinder ORDER BY word.toLowerCase");

              QueryResults qr = q.execute (wordRules);

              wordRules = (List<Rule>) qr.getResults ();

          } catch (Exception e)
          {

              Environment.logError ("Unable to sort word rules",
                                    e);

              // Just carry on.

          }

          if (wordRules != null)
          {

              Map<String, String> ignores = RuleFactory.getIgnores (RuleFactory.ALL,
                                                                    this.projectViewer.getProject ().getProperties ());

              for (Rule r : wordRules)
              {

                  if (ignores.containsKey (r.getId ()))
                  {

                      continue;

                  }

                  RuleBox rb = new RuleBox (r);

                  this.wordsBox.add (rb);

                  rb.init (this);

              }

          }

          this.wordsBox.add (Box.createVerticalGlue ());

          final ProblemFinderRuleConfig _this = this;

          UIUtils.doLater (new ActionListener ()
          {

              public void actionPerformed (ActionEvent ev)
              {

                  _this.wordsBoxSP.getVerticalScrollBar ().setValue (val);

              }

          });

    }

    public void init ()
    {

        if (this.inited)
        {

           return;

        }

        final ProblemFinderRuleConfig _this = this;

        this.tabs = new DnDTabbedPane ();
        // Load the "rules to ignore".

        this.tabs.setAlignmentX (Component.LEFT_ALIGNMENT);
        // Get the to ignore from the user properties.

        this.tabs.setTabLayoutPolicy (JTabbedPane.SCROLL_TAB_LAYOUT);

        this.createWordsWrapper ();

        this.tabs.add ("Words/Phrases",
                       this.wordsWrapper);

        this.createSentenceWrapper ();

        this.tabs.add ("Sentence Structure",
                       this.sentenceWrapper);

        this.createParagraphWrapper ();

        this.tabs.add ("Paragraph Structure",
                       this.paragraphWrapper);

        this.tabs.setBorder (new EmptyBorder (10,
                                              5,
                                              0,
                                              5));

        this.add (this.tabs);

        JButton finish = new JButton ("Finish");

        finish.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.closePopupParent (_this.getParent ());

            }

        });

        JButton[] buts = new JButton[] { finish };

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.CENTER_ALIGNMENT);
        bp.setOpaque (false);

        bp.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.add (Box.createVerticalStrut (10));

        this.add (bp);

        this.inited = true;

    }

    private List<Rule> getParagraphIgnores ()
    {

        List<Rule> rules = new ArrayList ();

        Iterator<String> iter = RuleFactory.getIgnores (RuleFactory.ALL,
                                                        this.projectViewer.getProject ().getProperties ()).keySet ().iterator ();

        while (iter.hasNext ())
        {

            Rule r = RuleFactory.getRuleById (iter.next ());

            if (r == null)
            {

                continue;

            }

            if (!r.getCategory ().equals (Rule.PARAGRAPH_CATEGORY))
            {

                continue;

            }

            rules.add (r);

        }

        return rules;

    }

    private List<Rule> getSentenceIgnores ()
    {

        List<Rule> rules = new ArrayList ();

        Iterator<String> iter = RuleFactory.getIgnores (RuleFactory.ALL,
                                                        this.projectViewer.getProject ().getProperties ()).keySet ().iterator ();

        while (iter.hasNext ())
        {

            Rule r = RuleFactory.getRuleById (iter.next ());

            if (r == null)
            {

                continue;

            }

            if (!r.getCategory ().equals (Rule.SENTENCE_CATEGORY))
            {

                continue;

            }

            rules.add (r);

        }

        return rules;

    }

    private boolean isProjectIgnore (Rule r)
    {

        return RuleFactory.getIgnores (RuleFactory.PROJECT,
                                       this.projectViewer.getProject ().getProperties ()).containsKey (r.getId ());

    }

    private void removeIgnore (Rule r,
                               int  type)
    {

        RuleFactory.removeIgnore (r,
                                  type,
                                  this.projectViewer.getProject ().getProperties ());

        // Don't like this but there aren't many other ways to do it.
        this.sentenceControl.setVisible (this.getSentenceIgnores ().size () != 0);

        this.paragraphControl.setVisible (this.getParagraphIgnores ().size () != 0);

    }

    public void editRule (final Rule    r,
                          final boolean add)
    {

        final ProblemFinderRuleConfig _this = this;

        Box editBox = null;

        if (r instanceof SentenceRule)
        {

            editBox = this.sentenceEditBox;

        }

        if (r instanceof ParagraphRule)
        {

            editBox = this.paragraphEditBox;

        }

        if (r instanceof WordFinder)
        {

            editBox = this.wordsEditBox;

        }

        editBox.removeAll ();

        final Box _editBox = editBox;

          Form f = r.getEditForm (new ActionListener ()
          {

               @Override
               public void actionPerformed (ActionEvent ev)
               {

                    r.updateFromForm ();

                    try
                    {

                        RuleFactory.saveUserRule (r);

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to save user rule: " +
                                              r,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to save rule");

                    }

                    _this.restoreToView (_editBox,
                                         r,
                                         add);

                    Environment.fireUserProjectEvent (_this,
                                                      ProjectEvent.PROBLEM_FINDER,
                                                      (add ? ProjectEvent.NEW_RULE : ProjectEvent.EDIT_RULE),
                                                      r);

               }

          },
          new ActionListener ()
          {

               @Override
               public void actionPerformed (ActionEvent ev)
               {

                    _this.restoreToView (_editBox,
                                         r,
                                         add);

               }

          },
          add);

        f.addFormListener (new FormAdapter ()
        {

            public void actionPerformed (FormEvent ev)
            {

                if (ev.getID () == FormEvent.CANCEL)
                {

                    _this.restoreToView (_editBox,
                                         null,
                                         add);

                    return;

                }

            }

        });

        f.setAlignmentX (Component.LEFT_ALIGNMENT);
        f.setBorder (null);
        editBox.add (f);

        f.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         f.getPreferredSize ().height));

        editBox.add (Box.createVerticalGlue ());

        if (r instanceof WordFinder)
        {

            this.tabs.setSelectedIndex (0);

            ((CardLayout) this.wordsWrapper.getLayout ()).show (this.wordsWrapper,
                                                                "edit");

            this.wordsWrapper.validate ();
            this.wordsWrapper.repaint ();

        } else {

            if (r instanceof SentenceRule)
            {

                this.tabs.setSelectedIndex (1);

                ((CardLayout) this.sentenceWrapper.getLayout ()).show (this.sentenceWrapper,
                                                                       "edit");

                this.sentenceWrapper.validate ();
                this.sentenceWrapper.repaint ();

            }

            if (r instanceof ParagraphRule)
            {

                this.tabs.setSelectedIndex (2);

                ((CardLayout) this.paragraphWrapper.getLayout ()).show (this.paragraphWrapper,
                                                                        "edit");

                this.paragraphWrapper.validate ();
                this.paragraphWrapper.repaint ();

            }

        }

        this.validate ();
        this.repaint ();

    }

    private void restoreToView (Box     editBox,
                                Rule    r,
                                boolean add)
    {

        editBox.removeAll ();

        if (r != null)
        {

            RuleBox rb = null;

            Container parent = null;

            Component[] comps = null;

            if (r instanceof SentenceRule)
            {

                parent = this.sentenceBox;

                comps = this.sentenceBox.getComponents ();

            }

            if (r instanceof ParagraphRule)
            {

                parent = this.paragraphBox;

                comps = this.paragraphBox.getComponents ();

            }

            if (r instanceof WordFinder)
            {

                parent = this.wordsBox;

                comps = this.wordsBox.getComponents ();

            }

            for (int i = 0; i < comps.length; i++)
            {

                if (comps[i] instanceof RuleBox)
                {

                    RuleBox _rb = (RuleBox) comps[i];

                    if (_rb.rule == r)
                    {

                        rb = _rb;

                        break;

                    }

                }

            }

            if (rb != null)
            {

                rb.update ();

            } else
            {

                // Add a new one at the top.
                rb = new RuleBox (r);

                parent.add (rb,
                            0);

                rb.init (this);

            }

        }

        if (editBox == this.wordsEditBox)
        {

            ((CardLayout) this.wordsWrapper.getLayout ()).show (this.wordsWrapper,
                                                                "view");

        }

        if (editBox == this.sentenceEditBox)
        {

            ((CardLayout) this.sentenceWrapper.getLayout ()).show (this.sentenceWrapper,
                                                                   "view");

        }

        if (editBox == this.paragraphEditBox)
        {

            ((CardLayout) this.paragraphWrapper.getLayout ()).show (this.paragraphWrapper,
                                                                    "view");

        }

        this.validate ();
        this.repaint ();

    }

    private void removeRuleBox (RuleBox r)
    {

        if (r.rule.getCategory ().equals (Rule.WORD_CATEGORY))
        {

            this.wordsBox.remove (r);

        }

        if (r.rule.getCategory ().equals (Rule.SENTENCE_CATEGORY))
        {

            this.sentenceBox.remove (r);

        }

        if (r.rule.getCategory ().equals (Rule.PARAGRAPH_CATEGORY))
        {

            this.paragraphBox.remove (r);

        }

        this.validate ();
        this.repaint ();

    }

    public static void confirmRuleRemoval (final AbstractProjectViewer               parent,
                                           final Rule                                r,
                                           final com.gentlyweb.properties.Properties projProps,
                                           final ActionListener                      onRemove)
    {

        final Map<String, ActionListener> buttons = new LinkedHashMap ();

        buttons.put ("From this {project} only",
                     new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                RuleFactory.addIgnore (r,
                                       RuleFactory.PROJECT,
                                       projProps);

                if (onRemove != null)
                {

                    onRemove.actionPerformed (new ActionEvent (ev.getSource (),
                                                               RuleFactory.PROJECT,
                                                               "project"));

                }



            }

        });

        buttons.put ("All {projects}",
                     new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                RuleFactory.addIgnore (r,
                                       RuleFactory.USER,
                                       projProps);
                if (onRemove != null)
                {

                    onRemove.actionPerformed (new ActionEvent (ev.getSource (),
                                                               RuleFactory.USER,
                                                               "user"));

                }

            }

        });

        buttons.put (Environment.getButtonLabel (null,
                                                 Constants.CANCEL_BUTTON_LABEL_ID),
                     null);

          UIUtils.createQuestionPopup (parent,
                                       "Confirm rule removal",
                                       Constants.DELETE_ICON_NAME,
                                       "Please confirm you wish to remove this rule.",
                                       buttons,
                                       null,
                                       null);

    }

    public ProjectViewer getProjectViewer ()
    {

        return this.projectViewer;

    }

}
