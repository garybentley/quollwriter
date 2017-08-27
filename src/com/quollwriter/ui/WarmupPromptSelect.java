package com.quollwriter.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import java.net.*;

import java.util.*;

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

import com.quollwriter.ui.components.*;


public class WarmupPromptSelect extends Box
{

    public static final int    WARMUP_SAVED = 0;
    public static final int DEFAULT_WORDS = 500;
    public static final int DEFAULT_MINS = 30;

    private TextArea ownPrompt = null;
    private Prompt    prompt = null;
    private JComboBox mins = null;
    private JComboBox words = null;
    private JCheckBox doOnStartup = null;
    private AbstractViewer viewer = null;
    private JTextPane promptPreview = null;
    private boolean inited = false;

    public WarmupPromptSelect(AbstractViewer v)
    {

        super (BoxLayout.Y_AXIS);

        this.viewer = v;
        
        this.ownPrompt = UIUtils.createTextArea (Environment.getUIString (LanguageStrings.dowarmup,
                                                                          LanguageStrings.ownprompt,
                                                                          LanguageStrings.tooltip),
                                                 //"Enter your prompt text here...",
                                                 3,
                                                 -1);
        
        Prompts.shuffle ();

        this.prompt = Prompts.next ();
        
        this.mins = WarmupPromptSelect.getTimeOptions ();
        
        this.words = WarmupPromptSelect.getWordsOptions ();

        this.promptPreview = UIUtils.createHelpTextPane (v);

        this.doOnStartup = WarmupPromptSelect.getDoWarmupOnStartupCheckbox ();
        
    }

    public void init ()
    {

        if (this.inited)
        {
            
            return;
            
        }
    
        Set<String> prefix = new LinkedHashSet ();
        prefix.add (LanguageStrings.dowarmup);
    
        this.inited = true;
    
        final WarmupPromptSelect _this = this;

        this.promptPreview.setText (UIUtils.formatPrompt (this.prompt));
        
        this.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.setOpaque (false);

        this.add (UIUtils.createBoldSubHeader (Environment.getUIString (prefix,
                                                                        LanguageStrings.chooseprompt,
                                                                        LanguageStrings.title),
                                               //"Choose a prompt",
                                               null));

        Box ppb = new Box (BoxLayout.Y_AXIS);
        ppb.setBorder (UIUtils.createPadding (0, 5, 10, 5));
        ppb.add (this.promptPreview);
        
        this.add (ppb);
        
        Box buts = new Box (BoxLayout.X_AXIS);

        final JCheckBox doNotShow = new JCheckBox (Environment.getUIString (prefix,
                                                                            LanguageStrings.noshowpromptagain));
                                                   //"Do not show this prompt again");
        doNotShow.setOpaque (false);

        final JButton links = new JButton (Environment.getIcon ("website-links",
                                                                Constants.ICON_MENU));
        links.setToolTipText (Environment.getUIString (prefix,
                                                       LanguageStrings.weblinks,
                                                       LanguageStrings.tooltip));
        //"Click to see a list of websites that have writing prompts");

        buts.add (links);

        links.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    JPopupMenu menu = new JPopupMenu ();

                    List<PromptWebsite> ws = Prompts.getPromptWebsites ();

                    for (PromptWebsite w : ws)
                    {

                        JMenuItem item = new JMenuItem (String.format ("%s (%s)",
                                                                       w.getName (),
                                                                       w.getCount ()));

                        item.setToolTipText (String.format (Environment.getUIString (prefix,
                                                                                     LanguageStrings.weblinks,
                                                                                     LanguageStrings.visitlink),
                                                            //"Click to go to the %s website (%s)",
                                                            w.getName (),
                                                            w.getURL ()));
                        item.setActionCommand (w.getURL ());

                        item.addActionListener (new ActionAdapter ()
                            {

                                public void actionPerformed (ActionEvent ev)
                                {

                                    UIUtils.openURL (_this,
                                                     ev.getActionCommand ());

                                }

                            });

                        menu.add (item);

                    }

                    if (ws.size () > 0)
                    {

                        menu.show (links,
                                   10,
                                   10);

                    }

                }

            });

        buts.add (Box.createHorizontalStrut (5));

        JButton prev = new JButton (Environment.getIcon ("previous",
                                                         Constants.ICON_MENU));
        prev.setToolTipText (Environment.getUIString (prefix,
                                                      LanguageStrings.previousprompt,
                                                      LanguageStrings.tooltip));
        //"Go back to the previous prompt");

        prev.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    if (doNotShow.isSelected ())
                    {

                        // Remove the prompt.
                        Prompts.excludeCurrent ();

                    }

                    Prompt p = Prompts.previous ();

                    if (p != null)
                    {

                        _this.prompt = p;

                        _this.promptPreview.setText (UIUtils.formatPrompt (p));

                        _this.promptPreview.setCaretPosition (0);

                    } else
                    {

                        _this.promptPreview.setText ("");

                        UIUtils.showMessage (_this,
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.allpromptsexcluded));
                                             //"You have excluded all the prompts.");

                    }

                    doNotShow.setSelected (false);

                    UIUtils.resizeParent (_this);
                    
                }

            });

        buts.add (prev);
        buts.add (Box.createHorizontalStrut (5));

        JButton next = UIUtils.createButton (Environment.getUIString (prefix,
                                                                      LanguageStrings.nextprompt,
                                                                      LanguageStrings.text),
                                             //"Next prompt",
                                             "next");

        next.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    if (doNotShow.isSelected ())
                    {

                        // Remove the prompt.
                        Prompts.excludeCurrent ();

                    }

                    Prompt p = Prompts.next ();

                    if (p != null)
                    {

                        _this.prompt = p;

                        _this.promptPreview.setText (UIUtils.formatPrompt (p));

                        _this.promptPreview.setCaretPosition (0);

                    } else
                    {

                        _this.promptPreview.setText ("");

                        UIUtils.showMessage (_this,
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.allpromptsexcluded));                                             
                                             //"You have excluded all the prompts.");

                    }

                    doNotShow.setSelected (false);

                    UIUtils.resizeParent (_this);
                    
                }

            });

        next.setToolTipText (Environment.getUIString (prefix,
                                                      LanguageStrings.nextprompt,
                                                      LanguageStrings.tooltip));
        //"Go to the next prompt");
        next.setHorizontalTextPosition (SwingConstants.LEFT);

        buts.setAlignmentX (Component.LEFT_ALIGNMENT);
        buts.setBorder (UIUtils.createPadding (0, 10, 15, 5));

        buts.add (next);

        buts.add (Box.createHorizontalStrut (30));

        buts.add (doNotShow);
        this.add (buts);

        this.add (UIUtils.createBoldSubHeader (Environment.getUIString (prefix,
                                                                        LanguageStrings.ownprompt,
                                                                        LanguageStrings.title),
                                               //"OR, Enter your own prompt",
                                               null));
        
        Box b = new Box (BoxLayout.Y_AXIS);
        
        b.setBorder (UIUtils.createPadding (5, 5, 15, 5));
        b.add (this.ownPrompt);
        
        this.add (b);
/*
        buts = new Box (BoxLayout.X_AXIS);

        JButton share = UIUtils.createButton ("Share your prompt",
                                              "share");
        share.setToolTipText ("Click to share your prompt with other Quoll Writer users");

        share.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    if (_this.ownPrompt.getText ().trim ().equals (""))
                    {

                        UIUtils.showMessage (_this,
                                             "Please enter a prompt.");

                        return;

                    }

                    ShareWritingPrompt p = new ShareWritingPrompt (_this.projectViewer,
                                                                   _this.ownPrompt.getText ().trim ());

                    p.init ();
                    p.setVisible (true);

                }

            });
*/
        this.add (UIUtils.createBoldSubHeader (Environment.getUIString (prefix,
                                                                        LanguageStrings.dofor,
                                                                        LanguageStrings.title),
                                               //"And do the {warmup} for",
                                               null));

        FormLayout fl = new FormLayout ("p, 6px, p, 6px, p, 6px, p",
                                        "p, 15px, p");

        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        builder.add (this.words,
                     cc.xy (1,
                            1));

        builder.addLabel (Environment.getUIString (prefix,
                                                   LanguageStrings.andor),
                          //"and/or",
                          cc.xy (3,
                                 1));

        builder.add (this.mins,
                     cc.xy (5,
                            1));

        builder.addLabel (Environment.getUIString (prefix,
                                                   LanguageStrings.whicheverfirst),
                          //"(whichever is reached first)",
                          cc.xy (7,
                                 1));
        
        builder.add (this.doOnStartup,
                     cc.xyw (1,
                             3,
                             7));

        JPanel p = builder.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);
        p.setBorder (UIUtils.createPadding (5, 5, 0, 5));
/*
        p.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         p.getPreferredSize ().height));
*/
        this.add (p);

        JButton start = UIUtils.createButton (Environment.getUIString (prefix,
                                                                       LanguageStrings.startwriting),
                                              //"Start Writing",
                                              new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                final int mins = WarmupPromptSelect.getMinsCount (_this.mins);
                final int words = WarmupPromptSelect.getWordCount (_this.words);
            
                if ((mins == 0)
                    &&
                    (words == 0)
                   )
                {
                    
                    UIUtils.showErrorMessage (_this.viewer,
                                              Environment.getUIString (prefix,
                                                                       LanguageStrings.starterror));
                                              //"o_O  The timer can't be unlimited for both time and words.");
                    
                    return;
                    
                }        
            
                Prompt prompt = _this.prompt;

                final String ownPromptT = _this.ownPrompt.getText ().trim ();

                if (!ownPromptT.equals (""))
                {

                    // Create a user prompt.
                    try
                    {

                        prompt = Prompts.addUserPrompt (ownPromptT);

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to create user prompt with text: " +
                                              ownPromptT,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  Environment.getUIString (prefix,
                                                                           LanguageStrings.saveerror));
                                                  //"Unable to save your prompt, please contact support for assistance.");

                        return;

                    }

                }

                final Prompt _prompt = prompt;
                
                // See if we already have the warmups project, if so then just open it.
                ProjectInfo p = null;

                try
                {

                    p = Environment.getWarmupsProject ();

                } catch (Exception e)
                {

                    Environment.logError ("Unable to get warmups projects",
                                          e);

                    UIUtils.showErrorMessage (_this,
                                              Environment.getUIString (prefix,
                                                                       LanguageStrings.getwarmupsprojecterror));
                                              //"Unable to get {Warmups} {project}.");

                    return;

                }

                if (p != null)
                {
                
                    try
                    {

                        Environment.openProject (p);

                        _this.addWarmupToProject (_prompt);                                                        
                        
                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to open warmups project",
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  Environment.getUIString (prefix,
                                                                           LanguageStrings.openwarmupsprojecterror));
                                                  //"Unable to open {Warmups} {project}.");

                        return;

                    }

                } else {                        

                    // If we don't then output a message telling the user that it will be created
                    // then create it.
                    UIUtils.showMessage (null,
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.createwarmupsproject,
                                                                  LanguageStrings.title),
                                         //"{Warmups} project",
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.createwarmupsproject,
                                                                  LanguageStrings.text),
                                         //"A {Warmups} {project} will now be created to hold your {warmups}.",
                                         null,
                                         new ActionListener ()
                                         {
                                            
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                                
                                                WarmupsViewer v = new WarmupsViewer ();
                                                                                                
                                                Project p = new Project (Constants.DEFAULT_WARMUPS_PROJECT_NAME);
                                                p.setType (Project.WARMUPS_PROJECT_TYPE);
                    
                                                try
                                                {
                    
                                                    v.init ();
                    
                                                    // Put it in the user's directory.
                                                    v.newProject (Environment.getUserQuollWriterDir (),
                                                                  p,
                                                                  null);

                                                } catch (Exception e) {
                                                    
                                                    Environment.logError ("Unable to create warmups project",
                                                                          e);
                                                    
                                                    UIUtils.showErrorMessage (null,
                                                                              Environment.getUIString (prefix,
                                                                                                       LanguageStrings.createwarmupsproject,
                                                                                                       LanguageStrings.actionerror));
                                                                              //"Unable to create {Warmups} project please contact Quoll Writer support for assistance.");
                                                    
                                                    return;
                                                    
                                                }
                                                
                                                _this.addWarmupToProject (_prompt);
                                                
                                            }
                                            
                                         });

                }

                UIUtils.closePopupParent (_this);

            }

        });

        JButton cancel = UIUtils.createButton (Constants.CANCEL_BUTTON_LABEL_ID,
                                               new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.closePopupParent (_this);

            }

        });        

        JButton[] buts2 = {start, cancel};
        //buts[0] = b;
        //buts[1] = c;
            
            
        JPanel bp = UIUtils.createButtonBar2 (buts2,
                                              Component.LEFT_ALIGNMENT);

        bp.setBorder (UIUtils.createPadding (10, 0, 0, 0));
                                              
        this.add (bp);
        
    }

    private void addWarmupToProject (final Prompt prompt)
    {        
        
        final WarmupPromptSelect _this = this;
        
        final int mins = WarmupPromptSelect.getMinsCount (this.mins);
        final int words = WarmupPromptSelect.getWordCount (this.words);
    
        if ((mins == 0)
            &&
            (words == 0)
           )
        {
            
            UIUtils.showErrorMessage (_this.viewer,
                                      Environment.getUIString (LanguageStrings.dowarmup,
                                                               LanguageStrings.starterror));
                                      //"o_O  The timer can't be unlimited for both time and words.");
            
            return;
            
        }        
        
        UIUtils.doLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {                                
                
                ProjectInfo p = null;
                
                try
                {

                    p = Environment.getWarmupsProject ();

                } catch (Exception e)
                {

                    Environment.logError ("Unable to get warmups projects",
                                          e);

                    UIUtils.showErrorMessage (null,
                                              Environment.getUIString (LanguageStrings.dowarmup,
                                                                       LanguageStrings.getwarmupsprojecterror));
                                              //"Unable to get {Warmups} {project}.");

                    return;

                }
                
                final Warmup w = new Warmup ();
                w.setPrompt (prompt);
                w.setWords (words);
                w.setMins (mins);                
                
                try
                {
                    
                    WarmupsViewer v = (WarmupsViewer) Environment.getProjectViewer (p);
                
                    v.addNewWarmup (w);
                    
                    if (!prompt.isUserPrompt ())
                    {
                        
                        v.fireProjectEvent (Warmup.OBJECT_TYPE,
                                            ProjectEvent.CREATE_OWN_PROMPT,
                                            w);
                        
                    }
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to add warm-up" +
                                          w,
                                          e);

                    UIUtils.showErrorMessage (null,
                                              Environment.getUIString (LanguageStrings.dowarmup,
                                                                       LanguageStrings.addwarmuperror));
                                              //"Unable to add {warmup}.");
                    
                }
                
            }
            
        });
        
    }
    
    public static JComboBox getTimeOptions (int minsC)
    {

        Vector minsV = new Vector ();
        minsV.add (Environment.getUIString (LanguageStrings.times,
                                            LanguageStrings.unlimited));
        //"Unlimited");
        minsV.add (Environment.getUIString (LanguageStrings.times,
                                            LanguageStrings.mins10));
        //minsV.add ("10 minutes");
        minsV.add (Environment.getUIString (LanguageStrings.times,
                                            LanguageStrings.mins20));
        //minsV.add ("20 minutes");
        minsV.add (Environment.getUIString (LanguageStrings.times,
                                            LanguageStrings.mins30));        
        //minsV.add (WarmupPromptSelect.DEFAULT_MINS); // 30 minutes
        minsV.add (Environment.getUIString (LanguageStrings.times,
                                            LanguageStrings.hour1));        
        //minsV.add ("1 hour");

        final JComboBox mins = new JComboBox (minsV);

        String minsDef = UserProperties.get (Constants.DEFAULT_WARMUP_MINS_PROPERTY_NAME);

        if (minsDef == null)
        {

            minsC = WarmupPromptSelect.DEFAULT_MINS;

        } else {
            
            minsC = Integer.parseInt (minsDef);
            
        }

        // Unlimited.
        int sel = 0;
    
        if (minsC == 60)
        {

            sel = 4;
        
        }
        
        if (minsC == 30)
        {
            
            sel = 3;
            
        }
        
        if (minsC == 20)
        {
            
            sel = 2;
            
        }
        
        if (minsC == 10)
        {
            
            sel = 1;
            
        }
        
        mins.setSelectedItem (minsV.get (sel));

        mins.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UserProperties.set (Constants.DEFAULT_WARMUP_MINS_PROPERTY_NAME,
                                    String.valueOf (WarmupPromptSelect.getMinsCount (mins)));
                                    
            }

        });        
        
        return mins;

    }

    public static JComboBox getTimeOptions ()
    {

        int wc = -1;
        
        try
        {
            
            wc = Integer.parseInt (UserProperties.get (Constants.DEFAULT_WARMUP_MINS_PROPERTY_NAME));
            
        } catch (Exception e) {
            
            // Ignore.
            
        }
        
        return WarmupPromptSelect.getTimeOptions (wc);
        
    }    
    
    public static JComboBox getWordsOptions ()
    {

        int wc = -1;
        
        try
        {
            
            wc = Integer.parseInt (UserProperties.get (Constants.DEFAULT_WARMUP_WORDS_PROPERTY_NAME));
            
        } catch (Exception e) {
            
            // Ignore.
            
        }
        
        return WarmupPromptSelect.getWordsOptions (wc);
        
    }
    
    public static JComboBox getWordsOptions (int wordsC)
    {

        Vector wordsV = new Vector ();
        wordsV.add (Environment.getUIString (LanguageStrings.words,
                                             LanguageStrings.unlimited));
        //wordsV.add ("Unlimited");
        wordsV.add (Environment.getUIString (LanguageStrings.words,
                                             LanguageStrings.words100));
        //wordsV.add ("100 words");
        wordsV.add (Environment.getUIString (LanguageStrings.words,
                                             LanguageStrings.words250));
        //wordsV.add ("250 words");
        wordsV.add (Environment.getUIString (LanguageStrings.words,
                                             LanguageStrings.words500));
        //wordsV.add (WarmupPromptSelect.DEFAULT_WORDS); // 500 words
        wordsV.add (Environment.getUIString (LanguageStrings.words,
                                             LanguageStrings.words1000));
        //wordsV.add ("1000 words");

        final JComboBox words = new JComboBox (wordsV);

        String wordsDef = UserProperties.get (Constants.DEFAULT_WARMUP_WORDS_PROPERTY_NAME);

        if (wordsDef == null)
        {

            wordsC = WarmupPromptSelect.DEFAULT_WORDS;

        } else {
            
            wordsC = Integer.parseInt (wordsDef);
            
        }

        int sel = 0;
        
        if (wordsC == -1)
        {
            
            sel = 3;
            
        }
        
        if (wordsC == 100)
        {
            
            sel = 1;
            
        }
    
        if (wordsC == 250)
        {
            
            sel = 2;
            
        }

        if (wordsC == 500)
        {
            
            sel = 3;
            
        }
        
        if (wordsC == 1000)
        {
            
            sel = 4;
            
        }
        
        words.setSelectedItem (wordsV.get (sel));

        words.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UserProperties.set (Constants.DEFAULT_WARMUP_WORDS_PROPERTY_NAME,
                                    String.valueOf (WarmupPromptSelect.getWordCount (words)));
                                    
            }

        });        
        
        return words;

    }

    public static JCheckBox getDoWarmupOnStartupCheckbox ()
    {
        
        final JCheckBox doOnStartup = new JCheckBox (Environment.getUIString (LanguageStrings.dowarmup,
                                                                              LanguageStrings.dowarmuponstart));
        //"Do a {warmup} everytime Quoll Writer starts"));
        doOnStartup.setOpaque (false);
        
        doOnStartup.setSelected (UserProperties.getAsBoolean (Constants.DO_WARMUP_ON_STARTUP_PROPERTY_NAME));

        doOnStartup.addActionListener (new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
              
                UserProperties.set (Constants.DO_WARMUP_ON_STARTUP_PROPERTY_NAME,
                                    doOnStartup.isSelected ());
                                                                        
            }
            
        });
        
        return doOnStartup;
        
    }
    
    public static int getWordCount (JComboBox c)
    {

        int ws = c.getSelectedIndex ();
        int wordsC = 0;

        if (ws > 0)
        {

            if (ws == 1)
            {

                wordsC = 100;

            }

            if (ws == 2)
            {

                wordsC = 250;

            }

            if (ws == 3)
            {

                wordsC = 500;

            }

            if (ws == 4)
            {

                wordsC = 1000;

            }

        }

        return wordsC;

    }

    public static int getMinsCount (JComboBox c)
    {

        int ms = c.getSelectedIndex ();
        int minsC = 0;

        if (ms > 0)
        {

            if (ms == 1)
            {

                minsC = 10;

            }

            if (ms == 2)
            {

                minsC = 20;

            }

            if (ms == 3)
            {

                minsC = 30;

            }

            if (ms == 4)
            {

                minsC = 60;

            }

        }

        return minsC;

    }
    
    public static int getDefaultWarmupWords ()
    {

        String v = UserProperties.get (Constants.DEFAULT_WARMUP_WORDS_PROPERTY_NAME);

        try
        {

            return Integer.parseInt (v.substring (0,
                                                  v.indexOf (" ") - 1));

        } catch (Exception e)
        {

            return 500;

        }

    }

    public static int getDefaultWarmupMins ()
    {

        String v = UserProperties.get (Constants.DEFAULT_WARMUP_MINS_PROPERTY_NAME);

        try
        {

            return Integer.parseInt (v.substring (0,
                                                  v.indexOf (" ") - 1));

        } catch (Exception e)
        {

            return 30;

        }

    }

}
