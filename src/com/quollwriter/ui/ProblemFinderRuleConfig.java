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


public class ProblemFinderRuleConfig extends PopupWindow
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
            this.desc.setBorder (new EmptyBorder (0,
                                                  15,
                                                  0,
                                                  10));
            this.desc.setSize (new Dimension (420, 500));
            
            this.desc.setPreferredSize (new Dimension (420,
                                                       this.desc.getPreferredSize ().height + 10));
                                                     
            this.desc.setMaximumSize (new Dimension (400,
                                                     Short.MAX_VALUE));
                                                  
            this.info = UIUtils.createHelpTextPane (this.rule.getSummary (),
                                                    ProblemFinderRuleConfig.this.projectViewer);
        
            main.add (this.info);
            main.add (Box.createHorizontalGlue ());

            this.desc.setVisible (false);
            this.desc.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.add (this.desc);
            
            java.util.List<JButton> buttons = new ArrayList ();
            
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

                                                        _this.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                                                             _this.getPreferredSize ().height));
                                                        
                                                        _this.validate ();
                                                        _this.repaint ();

                                                    }
                                                    
                                               }));
                                               
            buttons.add (UIUtils.createButton (Constants.DELETE_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Click to remove this rule",
                                               new ActionAdapter ()
                                               {
                                                
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        ProblemFinderRuleConfig.confirmRuleRemoval (conf,
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

            this.desc.setText (UIUtils.getWithHTMLStyleSheet (this.desc,
                                                              this.rule.getDescription ()));
            this.info.setText (UIUtils.getWithHTMLStyleSheet (this.info,
                                                              this.rule.getSummary ()));

        }

    }

    private QuollEditorPanel editor = null;
    private Box              wordsBox = null;
    private Box              wordsEditBox = null;
    private JPanel           wordsWrapper = null;
    private Box              sentenceBox = null;
    private Box              sentenceEditBox = null;
    private JPanel           sentenceWrapper = null;
    private Box              sentenceControl = null;
    private Box              paragraphBox = null;
    private Box              paragraphEditBox = null;
    private JPanel           paragraphWrapper = null;
    private Box              paragraphControl = null;
    private DnDTabbedPane    tabs = null;

    public ProblemFinderRuleConfig (AbstractProjectViewer pv)
    {

        super (pv,
               Component.CENTER_ALIGNMENT);
                
    }
    
    public String getWindowTitle ()
    {

        return "Configure the Problem Finder Rules";

    }

    public String getHeaderTitle ()
    {

        return this.getWindowTitle ();

    }

    public String getHeaderIconType ()
    {

        return "config";

    }

    public String getHelpText ()
    {

        return "";

    }

    private void createSentenceWrapper ()
    {

        final ProblemFinderRuleConfig _this = this;
     
        this.sentenceBox = new Box (BoxLayout.Y_AXIS);
        this.sentenceBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.sentenceBox.setOpaque (false);

        final JScrollPane spsp = new JScrollPane (this.sentenceBox);
        spsp.setAlignmentX (Component.LEFT_ALIGNMENT);
        spsp.getViewport ().setOpaque (false);
        spsp.setBorder (null);
        spsp.setOpaque (false);
        spsp.getVerticalScrollBar ().setUnitIncrement (20);
        spsp.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spsp.getViewport ().setPreferredSize (new Dimension (450,
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

        sentenceAll.add (spsp);

        this.sentenceWrapper.add (sentenceAll,
                                  "view");

        this.sentenceEditBox = new Box (BoxLayout.Y_AXIS);
        this.sentenceEditBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.sentenceEditBox.setOpaque (false);

        this.sentenceWrapper.add (this.sentenceEditBox,
                                  "edit");

        ((CardLayout) this.sentenceWrapper.getLayout ()).show (this.sentenceWrapper,
                                                               "view");

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

        SwingUtilities.invokeLater (new Runner ()
        {

            public void run ()
            {
                
                spsp.getVerticalScrollBar ().setValue (0);
                
            }
            
        });
          
    }

    private void createParagraphWrapper ()
    {

        final ProblemFinderRuleConfig _this = this;
     
        this.paragraphBox = new Box (BoxLayout.Y_AXIS);
        this.paragraphBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.paragraphBox.setOpaque (false);

        final JScrollPane spsp = new JScrollPane (this.paragraphBox);
        spsp.setAlignmentX (Component.LEFT_ALIGNMENT);
        spsp.getViewport ().setOpaque (false);
        spsp.setBorder (null);
        spsp.setOpaque (false);
        spsp.getVerticalScrollBar ().setUnitIncrement (20);
        spsp.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spsp.getViewport ().setPreferredSize (new Dimension (450,
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

        paragraphAll.add (spsp);

        this.paragraphWrapper.add (paragraphAll,
                                   "view");

        this.paragraphEditBox = new Box (BoxLayout.Y_AXIS);
        this.paragraphEditBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.paragraphEditBox.setOpaque (false);

        this.paragraphWrapper.add (this.paragraphEditBox,
                                   "edit");

        ((CardLayout) this.paragraphWrapper.getLayout ()).show (this.paragraphWrapper,
                                                                "view");

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

        SwingUtilities.invokeLater (new Runner ()
        {

            public void run ()
            {
                
                spsp.getVerticalScrollBar ().setValue (0);
                
            }
            
        });
          
    }
    
    private void createWordsWrapper ()
    {

        final ProblemFinderRuleConfig _this = this;
     
        this.wordsBox = new Box (BoxLayout.Y_AXIS);
        this.wordsBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.wordsBox.setOpaque (false);
        final JScrollPane ppsp = new JScrollPane (this.wordsBox);
        ppsp.setAlignmentX (Component.LEFT_ALIGNMENT);
        ppsp.getViewport ().setOpaque (false);
        ppsp.setBorder (null);
        ppsp.setOpaque (false);
        ppsp.getVerticalScrollBar ().setUnitIncrement (20);
        ppsp.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.wordsWrapper = new JPanel ();
        this.wordsWrapper.setLayout (new CardLayout ());
        this.wordsWrapper.setOpaque (false);
        ppsp.getViewport ().setPreferredSize (new Dimension (450,
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
                            
                                                WordFinder wf = new WordFinder (true);
                            
                                                _this.editRule (wf,
                                                                true);
                            
                                                _this.repaint ();
                                                
                                            }
                                            
                                        }));
        
        wordsControl.add (UIUtils.createButtonBar (buts));

        wordsControl.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                    wordsControl.getPreferredSize ().height));

        wordsAll.add (wordsControl);

        wordsAll.add (ppsp);

        this.wordsWrapper.add (wordsAll,
                               "view");

        this.wordsEditBox = new Box (BoxLayout.Y_AXIS);
        this.wordsEditBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.wordsEditBox.setOpaque (false);

        this.wordsWrapper.add (this.wordsEditBox,
                               "edit");

        ((CardLayout) this.wordsWrapper.getLayout ()).show (this.wordsWrapper,
                                                            "view");     

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

        SwingUtilities.invokeLater (new Runner ()
        {

            public void run ()
            {
                
                ppsp.getVerticalScrollBar ().setValue (0);
                
            }
            
        });
     
    }
    
    public JComponent getContentPanel ()
    {

        final ProblemFinderRuleConfig _this = this;

        this.tabs = new DnDTabbedPane ();
        // Load the "rules to ignore".

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
                                       
        return tabs;

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

        List<FormItem> items = new ArrayList ();

        final JTextField summary = UIUtils.createTextField ();

        summary.setText (r.getSummary ());

        if (r instanceof SentenceRule)
        {

            if (r instanceof AbstractSentenceRule)
            {

                summary.setText (((AbstractSentenceRule) r).getEditSummary ());

            }

            items.add (new FormItem ("Summary",
                                     summary));

        }

        if (r instanceof ParagraphRule)
        {

            if (r instanceof AbstractParagraphRule)
            {

                summary.setText (((AbstractParagraphRule) r).getEditSummary ());

            }

            items.add (new FormItem ("Summary",
                                     summary));

        }
        
        items.addAll (r.getFormItems ());

        final JCheckBox ignoreInDialogue = new JCheckBox ("Ignore in dialogue");
        final JCheckBox onlyInDialogue = new JCheckBox ("Only in dialogue");

        ignoreInDialogue.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    if (ignoreInDialogue.isSelected ())
                    {

                        onlyInDialogue.setSelected (false);

                    }

                }

            });

        onlyInDialogue.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    if (onlyInDialogue.isSelected ())
                    {

                        ignoreInDialogue.setSelected (false);

                    }

                }

            });

        Vector whereVals = new Vector ();
        whereVals.add ("Anywhere");
        whereVals.add ("Start of sentence");
        whereVals.add ("End of sentence");

        final JComboBox where = new JComboBox (whereVals);

        if (r instanceof DialogueRule)
        {

            DialogueRule dr = (DialogueRule) r;

            String loc = dr.getWhere ();

            if (loc.equals (DialogueConstraints.START))
            {

                where.setSelectedIndex (1);

            }

            if (loc.equals (DialogueConstraints.END))
            {

                where.setSelectedIndex (2);

            }

            items.add (new FormItem ("Where",
                                     where));

            ignoreInDialogue.setSelected (dr.isIgnoreInDialogue ());
            onlyInDialogue.setSelected (dr.isOnlyInDialogue ());

            items.add (new FormItem (null,
                                     ignoreInDialogue));

            items.add (new FormItem (null,
                                     onlyInDialogue));

        }

        final JTextArea desc = UIUtils.createTextArea (3);

        if (!add)
        {

            if (r instanceof AbstractSentenceRule)
            {

                desc.setText (((AbstractSentenceRule) r).getEditDescription ());

            } else if (r instanceof AbstractParagraphRule) {
               
               desc.setText (((AbstractParagraphRule) r).getEditDescription ());
            
            } else {

                desc.setText (r.getDescription ());

            }

        }

        items.add (new FormItem ("Description",
                                 desc));

        // Create a new form.
        String title = null;

        if (add)
        {

            if (r instanceof SentenceRule)
            {

                title = "Add new Sentence Structure rule";

            }

            if (r instanceof ParagraphRule)
            {

                title = "Add new Paragraph Structure rule";

            }

            if (r instanceof WordFinder)
            {

                title = "Add new Word/Phrase rule";

            }
            
        } else
        {

            if ((r instanceof SentenceRule)
                ||
                (r instanceof ParagraphRule)
               )
            {

                title = "Edit Rule";

            } 

            if (r instanceof WordFinder)
            {
               
               title = "Edit: " + r.getSummary ();
               
            }
            
        }

        Form f = new Form (title,
                           null,
                           items,
                           null,
                           Form.SAVE_CANCEL_BUTTONS);
        f.setAlignmentX (Component.LEFT_ALIGNMENT);
        f.setBorder (null);
        editBox.add (f);

        f.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         f.getPreferredSize ().height));
        
        editBox.add (Box.createVerticalGlue ());
        
        final Box _editBox = editBox;

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

                r.setSummary (summary.getText ());

                r.setDescription (desc.getText ().trim ());

                if (r instanceof DialogueRule)
                {

                    DialogueRule dr = (DialogueRule) r;

                    dr.setOnlyInDialogue (onlyInDialogue.isSelected ());
                    dr.setIgnoreInDialogue (ignoreInDialogue.isSelected ());

                    int ws = where.getSelectedIndex ();

                    if (ws == 0)
                    {

                        dr.setWhere (DialogueConstraints.ANYWHERE);

                    }

                    if (ws == 1)
                    {

                        dr.setWhere (DialogueConstraints.START);

                    }

                    if (ws == 2)
                    {

                        dr.setWhere (DialogueConstraints.END);

                    }

                }

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

                _this.projectViewer.fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                                      (add ? ProjectEvent.NEW_RULE : ProjectEvent.EDIT_RULE),
                                                      r);

            }

        });

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

    public static void confirmRuleRemoval (final Component                           parent,
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

        // Need a nicer way of doing this, ok for now, improve in a later version.
        if (parent instanceof PopupWindow)
        {
    
            UIUtils.createQuestionPopup ((PopupWindow) parent,
                                         "Confirm rule removal",
                                         null,
                                         "Please confirm you wish to remove this rule.",
                                         buttons,
                                         null,
                                         null);

            return;
            
        } 
        
        if (parent instanceof QuollPanel)
        {
            
            UIUtils.createQuestionPopup (((QuollPanel) parent).getProjectViewer (),
                                         "Confirm rule removal",
                                         Constants.DELETE_ICON_NAME,
                                         "Please confirm you wish to remove this rule.",
                                         buttons,
                                         null,
                                         null);
            
            
        }
        
    }

    public JButton[] getButtons ()
    {

        final ProblemFinderRuleConfig _this = this;

        JButton c = new JButton ("Finish");

        c.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.close ();

                }

            });

        JButton[] buts = new JButton[1];
        buts[0] = c;

        return buts;

    }

}
