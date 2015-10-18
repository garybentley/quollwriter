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

    public static final String PANEL_ID = "warmups";
    public static final int    WARMUP_SAVED = 0;
    public static final String DEFAULT_WORDS = "500 words";
    public static final String DEFAULT_MINS = "30 minutes";

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
        
        this.ownPrompt = UIUtils.createTextArea ("Enter your prompt text here...",
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
    
        this.inited = true;
    
        final WarmupPromptSelect _this = this;

        this.promptPreview.setText (UIUtils.formatPrompt (this.prompt));
        
        this.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.setOpaque (false);

        this.add (UIUtils.createBoldSubHeader ("Choose a prompt",
                                               null));

        Box ppb = new Box (BoxLayout.Y_AXIS);
        ppb.setBorder (UIUtils.createPadding (0, 5, 10, 5));
        ppb.add (this.promptPreview);
        
        this.add (ppb);
        
        Box buts = new Box (BoxLayout.X_AXIS);

        final JCheckBox doNotShow = new JCheckBox ("Do not show this prompt again");
        doNotShow.setOpaque (false);

        final JButton links = new JButton (Environment.getIcon ("website-links",
                                                                Constants.ICON_MENU));
        links.setToolTipText ("Click to see a list of websites that have writing prompts");

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

                        item.setToolTipText (String.format ("Click to go to the %s website (%s)",
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
        prev.setToolTipText ("Go back to the previous prompt");

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
                                             "You have excluded all the prompts.");

                    }

                    doNotShow.setSelected (false);

                    UIUtils.resizeParent (_this);
                    
                }

            });

        buts.add (prev);
        buts.add (Box.createHorizontalStrut (5));

        JButton next = UIUtils.createButton ("Next prompt",
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
                                             "You have excluded all the prompts.");

                    }

                    doNotShow.setSelected (false);

                    UIUtils.resizeParent (_this);
                    
                }

            });

        next.setToolTipText ("Go to the next prompt");
        next.setHorizontalTextPosition (SwingConstants.LEFT);

        buts.setAlignmentX (Component.LEFT_ALIGNMENT);
        buts.setBorder (UIUtils.createPadding (0, 10, 15, 5));

        buts.add (next);

        buts.add (Box.createHorizontalStrut (30));

        buts.add (doNotShow);
        this.add (buts);

        this.add (UIUtils.createBoldSubHeader ("OR, Enter your own prompt",
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
        this.add (UIUtils.createBoldSubHeader ("And do the {warmup} for",
                                               null));

        FormLayout fl = new FormLayout ("p, 6px, p, 6px, p, 6px, p",
                                        "p, 15px, p");

        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        builder.add (this.words,
                     cc.xy (1,
                            1));

        builder.addLabel ("and/or",
                          cc.xy (3,
                                 1));

        builder.add (this.mins,
                     cc.xy (5,
                            1));

        builder.addLabel ("(whichever is reached first)",
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

        JButton start = UIUtils.createButton ("Start Writing",
                                              null);
        
        start.addActionListener (new ActionAdapter ()
        {

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
                                              "o_O  The timer can't be unlimited for both time and words.");
                    
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
                                                  "Unable to save your prompt, please contact support for assistance.");

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

                    UIUtils.showErrorMessage (_this,
                                              "Unable to get {Warmups} {project}.");

                    Environment.logError ("Unable to get warmups projects",
                                          e);

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

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to open {Warmups} {project}.");

                        Environment.logError ("Unable to open warmups project",
                                              e);

                        return;

                    }

                } else {                        

                    // If we don't then output a message telling the user that it will be created
                    // then create it.
                    UIUtils.showMessage (null,
                                         "{Warmups} project",
                                         "A {Warmups} {project} will now be created to hold each of the {warmups} you write.",
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
                                                                              "Unable to create {Warmups} project please contact Quoll Writer support for assistance.");
                                                    
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
                                               null);

        cancel.addActionListener (new ActionAdapter ()
        {

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
                                      "o_O  The timer can't be unlimited for both time and words.");
            
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

                    UIUtils.showErrorMessage (null,
                                              "Unable to get {Warmups} {project}.");

                    Environment.logError ("Unable to get warmups projects",
                                          e);

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
                    
                    UIUtils.showErrorMessage (null,
                                              "Unable to add {warmup}.");

                    Environment.logError ("Unable to add warm-up" +
                                          w,
                                          e);
                    
                }
                
            }
            
        });
        
    }
    
    public static JComboBox getTimeOptions (int minsC)
    {

        Vector minsV = new Vector ();
        minsV.add ("Unlimited");
        minsV.add ("10 minutes");
        minsV.add ("20 minutes");
        minsV.add (WarmupPromptSelect.DEFAULT_MINS); // 30 minutes
        minsV.add ("1 hour");

        final JComboBox mins = new JComboBox (minsV);

        com.gentlyweb.properties.Properties userProps = Environment.getUserProperties ();

        if (minsC == -1)
        {

            String minsDef = userProps.getProperty (Constants.DEFAULT_WARMUP_MINS_PROPERTY_NAME);

            if (minsDef == null)
            {

                minsDef = WarmupPromptSelect.DEFAULT_MINS;

            }

            mins.setSelectedItem (minsDef);

        } else
        {

            if (minsC == 0)
            {

                mins.setSelectedItem (minsV.get (0));

            } else
            {

                if (minsC == 60)
                {

                    mins.setSelectedItem ("1 hour");

                } else
                {

                    mins.setSelectedItem (minsC + " minutes");

                }

            }

        }

        mins.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                try
                {

                    StringProperty prop = new StringProperty (Constants.DEFAULT_WARMUP_MINS_PROPERTY_NAME,
                                                              String.valueOf (WarmupPromptSelect.getMinsCount (mins)));
                    prop.setDescription ("N/A");
            
                    com.gentlyweb.properties.Properties props = Environment.getUserProperties ();                    
            
                    props.setProperty (Constants.DEFAULT_WARMUP_MINS_PROPERTY_NAME,
                                       prop);
                    
                    Environment.saveUserProperties (props);
                                    
                } catch (Exception e)
                {

                    // Not good but not the end of the world, record the error but don't tell the user.
                    Environment.logError ("Unable to save user properties",
                                          e);

                }

            }

        });        
        
        return mins;

    }

    public static JComboBox getTimeOptions ()
    {

        com.gentlyweb.properties.Properties userProps = Environment.getUserProperties ();
        
        int wc = -1;
        
        try
        {
            
            wc = Integer.parseInt (userProps.getProperty (Constants.DEFAULT_WARMUP_MINS_PROPERTY_NAME));
            
        } catch (Exception e) {
            
            // Ignore.
            
        }
        
        return WarmupPromptSelect.getTimeOptions (wc);
        
    }    
    
    public static JComboBox getWordsOptions ()
    {

        com.gentlyweb.properties.Properties userProps = Environment.getUserProperties ();
        
        int wc = -1;
        
        try
        {
            
            wc = Integer.parseInt (userProps.getProperty (Constants.DEFAULT_WARMUP_WORDS_PROPERTY_NAME));
            
        } catch (Exception e) {
            
            // Ignore.
            
        }
        
        return WarmupPromptSelect.getWordsOptions (wc);
        
    }
    
    public static JComboBox getWordsOptions (int wordsC)
    {

        Vector wordsV = new Vector ();
        wordsV.add ("Unlimited");
        wordsV.add ("100 words");
        wordsV.add ("250 words");
        wordsV.add (WarmupPromptSelect.DEFAULT_WORDS); // 500 words
        wordsV.add ("1000 words");

        final JComboBox words = new JComboBox (wordsV);

        com.gentlyweb.properties.Properties userProps = Environment.getUserProperties ();

        if (wordsC == -1)
        {

            String wordsDef = userProps.getProperty (Constants.DEFAULT_WARMUP_WORDS_PROPERTY_NAME);

            if (wordsDef == null)
            {

                wordsDef = WarmupPromptSelect.DEFAULT_WORDS;

            }

            words.setSelectedItem (wordsDef);

        } else
        {

            if (wordsC == 0)
            {

                words.setSelectedItem (wordsV.get (0));

            } else
            {

                words.setSelectedItem (wordsC + " words");

            }

        }

        words.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                try
                {

                    StringProperty prop = new StringProperty (Constants.DEFAULT_WARMUP_WORDS_PROPERTY_NAME,
                                                              String.valueOf (WarmupPromptSelect.getWordCount (words)));
                    prop.setDescription ("N/A");

                    com.gentlyweb.properties.Properties props = Environment.getUserProperties ();
            
                    props.setProperty (Constants.DEFAULT_WARMUP_WORDS_PROPERTY_NAME,
                                       prop);
                    
                    Environment.saveUserProperties (props);
                                    
                } catch (Exception e)
                {

                    // Not good but not the end of the world, record the error but don't tell the user.
                    Environment.logError ("Unable to save user properties",
                                          e);

                }

            }

        });        
        
        return words;

    }

    public static JCheckBox getDoWarmupOnStartupCheckbox ()
    {
        
        final JCheckBox doOnStartup = new JCheckBox (Environment.replaceObjectNames ("Do a {warmup} everytime Quoll Writer starts"));
        doOnStartup.setOpaque (false);
        
        doOnStartup.setSelected (Environment.getUserProperties ().getPropertyAsBoolean (Constants.DO_WARMUP_ON_STARTUP_PROPERTY_NAME));

        doOnStartup.addActionListener (new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
              
                try
                {

                    BooleanProperty prop = new BooleanProperty (Constants.DO_WARMUP_ON_STARTUP_PROPERTY_NAME,
                                                                doOnStartup.isSelected ());
                    prop.setDescription ("N/A");

                    com.gentlyweb.properties.Properties props = Environment.getUserProperties ();
            
                    props.setProperty (Constants.DO_WARMUP_ON_STARTUP_PROPERTY_NAME,
                                       prop);
                
                    Environment.saveUserProperties (props);
                                    
                } catch (Exception e)
                {

                    // Not good but not the end of the world, record the error but don't tell the user.
                    Environment.logError ("Unable to save user properties",
                                          e);

                }
                                    
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

        String v = Environment.getProperty (Constants.DEFAULT_WARMUP_WORDS_PROPERTY_NAME);

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

        String v = Environment.getProperty (Constants.DEFAULT_WARMUP_MINS_PROPERTY_NAME);

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
