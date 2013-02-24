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


public class WarmupPromptSelect extends PopupWindow
{

    public static final String PANEL_ID = "warmups";
    public static final int    WARMUP_SAVED = 0;
    public static final String DEFAULT_WORDS = "500 words";
    public static final String DEFAULT_MINS = "30 minutes";

    private JTextArea ownPrompt = null;
    private Prompt    prompt = null;
    private JComboBox mins = null;
    private JComboBox words = null;
    private JCheckBox doOnStartup = null;

    public WarmupPromptSelect(AbstractProjectViewer v)
    {

        super (v);

    }


    public String getWindowTitle ()
    {

        return "Do a Warm-up Exercise";

    }

    public String getHeaderTitle ()
    {

        return "Do a Warm-up Exercise";

    }

    public String getHeaderIconType ()
    {

        return "warmups";

    }

    public String getHelpText ()
    {

        return null;

    }

    public JComponent getContentPanel ()
    {

        final WarmupPromptSelect _this = this;

        Box b = new Box (BoxLayout.Y_AXIS);

        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.setOpaque (true);
        b.setBackground (null);

        b.add (UIUtils.createBoldSubHeader ("Choose a prompt",
                                            null));

        Prompts.shuffle ();

        this.prompt = Prompts.next ();

        HTMLEditorKit kit = new HTMLEditorKit ();
        HTMLDocument  doc = (HTMLDocument) kit.createDefaultDocument ();

        final JTextPane promptPreview = new JTextPane (doc);

        promptPreview.setEditorKit (kit);
        promptPreview.setEditable (false);

        promptPreview.setBackground (Color.WHITE);

        promptPreview.setText (UIUtils.getWithHTMLStyleSheet (promptPreview,
                                                              UIUtils.formatPrompt (prompt)));

        promptPreview.addHyperlinkListener (new HyperlinkAdapter ()
            {

                public void hyperlinkUpdate (HyperlinkEvent ev)
                {

                    if (ev.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
                    {

                        URL url = ev.getURL ();

                        UIUtils.openURL (promptPreview,
                                         url);

                    }

                }

            });

        final JScrollPane ppsp = UIUtils.createScrollPane (promptPreview);
        ppsp.setBorder (null);

        ppsp.setPreferredSize (new Dimension (500,
                                              75));

        b.add (ppsp);
        b.add (Box.createVerticalStrut (10));

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

                        JMenuItem item = new JMenuItem (w.getName () + " (" + w.getCount () + ")");

                        item.setToolTipText ("Click to go to the " + w.getName () + " website (" + w.getURL () + ")");
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

                        promptPreview.setText (UIUtils.getWithHTMLStyleSheet (promptPreview,
                                                                              UIUtils.formatPrompt (p)));

                        promptPreview.setCaretPosition (0);

                    } else
                    {

                        promptPreview.setText ("");

                        UIUtils.showMessage (_this,
                                             "You have excluded all the prompts.");

                    }

                    doNotShow.setSelected (false);

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

                        promptPreview.setText (UIUtils.getWithHTMLStyleSheet (promptPreview,
                                                                              UIUtils.formatPrompt (p)));

                        promptPreview.setCaretPosition (0);

                    } else
                    {

                        promptPreview.setText ("");

                        UIUtils.showMessage (_this,
                                             "You have excluded all the prompts.");

                    }

                    doNotShow.setSelected (false);

                }

            });

        next.setToolTipText ("Go to the next prompt");
        next.setHorizontalTextPosition (SwingConstants.LEFT);

        buts.setAlignmentX (Component.LEFT_ALIGNMENT);
        buts.setBorder (new EmptyBorder (0,
                                         5,
                                         20,
                                         5));

        buts.add (next);

        buts.add (Box.createHorizontalStrut (30));

        buts.add (doNotShow);
        b.add (buts);

        b.add (UIUtils.createBoldSubHeader ("OR, Enter your own prompt",
                                            null));

        this.ownPrompt = UIUtils.createTextArea (3);
/*
        this.ownPrompt.setPreferredSize (new Dimension (500,
                                                        this.ownPrompt.getPreferredSize ().height));
*/
        JScrollPane sp = UIUtils.createScrollPane (this.ownPrompt);

        sp.setPreferredSize (new Dimension (500,
                                            sp.getPreferredSize ().height));

        b.add (sp);

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

        JButton clear = UIUtils.createButton ("Clear",
                                              null);
        clear.setToolTipText ("Click to clear the text above");

        clear.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.ownPrompt.setText ("");

                }

            });
/*
        buts.add (share);
        buts.add (Box.createHorizontalStrut (5));
*/

        buts.add (clear);

        buts.setAlignmentX (Component.LEFT_ALIGNMENT);
        buts.setBorder (new EmptyBorder (0,
                                         5,
                                         20,
                                         5));
/*
        buts.setPreferredSize (new Dimension (Short.MAX_VALUE,
                                              buts.getPreferredSize ().height));
        buts.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                              buts.getPreferredSize ().height));
*/
        // b.add (buts);

        b.add (Box.createVerticalStrut (20));

        b.add (UIUtils.createBoldSubHeader ("And do the warm-up for",
                                            null));

        FormLayout fl = new FormLayout ("p, 6px, p, 6px, p, 6px, p",
                                        "p, 15px, p");

        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        this.words = WarmupPromptSelect.getWordsOptions ();

        builder.add (this.words,
                     cc.xy (1,
                            1));

        builder.addLabel ("and/or",
                          cc.xy (3,
                                 1));

        this.mins = WarmupPromptSelect.getTimeOptions ();

        builder.add (this.mins,
                     cc.xy (5,
                            1));

        builder.addLabel ("(whichever is reached first)",
                          cc.xy (7,
                                 1));

        this.doOnStartup = WarmupPromptSelect.getDoWarmupOnStartupCheckbox ();
        
        builder.add (this.doOnStartup,
                     cc.xyw (1,
                             3,
                             7));

        JPanel p = builder.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);
        p.setBorder (new EmptyBorder (5,
                                      5,
                                      0,
                                      5));
/*
        p.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         p.getPreferredSize ().height));
*/
        b.add (p);

        SwingUtilities.invokeLater (new Runner ()
            {

                public void run ()
                {

                    ppsp.getVerticalScrollBar ().setValue (0);

                }

            });

        return b;

    }

    public JButton[] getButtons ()
    {

        final WarmupPromptSelect _this = this;

        JButton b = new JButton ("Start Writing");

        b.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

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

                    // See if we already have the warmups project, if so then just open it.
                    Project p = null;

                    try
                    {

                        p = Environment.getWarmupsProject ();

                    } catch (Exception e)
                    {

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to get Warm-ups project.");

                        Environment.logError ("Unable to get all projects",
                                              e);

                        return;

                    }

                    WarmupsViewer v = null;

                    try
                    {

                        if (p != null)
                        {

                            v = (WarmupsViewer) Environment.openProject (p);

                        } else
                        {

                            v = new WarmupsViewer ();

                            // If we don't then output a message telling the user that it will be created
                            // then create it.
                            UIUtils.showMessage (_this,
                                                 "A Warm-ups project will now be created to hold each of the warm-ups you write.");

                            p = new Project (Constants.DEFAULT_WARMUPS_PROJECT_NAME);
                            p.setType (Project.WARMUPS_PROJECT_TYPE);

                            // Put it in the user's directory.
                            v.newProject (Environment.getUserQuollWriterDir (),
                                          p,
                                          null);

                        }

                        final WarmupsViewer vv = v;
                        final Warmup w = new Warmup ();
                        w.setPrompt (prompt);
                        w.setWords (WarmupPromptSelect.getWordCount (_this.words));
                        w.setMins (WarmupPromptSelect.getMinsCount (_this.mins));

                        SwingUtilities.invokeLater (new Runner ()
                        {

                            public void run ()
                            {
                                
                                try
                                {
                                
                                    vv.addNewWarmup (w);
                                    
                                    if (!ownPrompt.equals (""))
                                    {
                                        
                                        vv.fireProjectEvent (Warmup.OBJECT_TYPE,
                                                             ProjectEvent.CREATE_OWN_PROMPT,
                                                             w);
                                        
                                    }
                                    
                                } catch (Exception e) {
                                    
                                    UIUtils.showErrorMessage (_this,
                                                              "Unable to add warm-up.");
            
                                    Environment.logError ("Unable to add warm-up" +
                                                          w,
                                                          e);
                                    
                                    
                                }
                                
                            }
                            
                        });

                    } catch (Exception e)
                    {

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to open Warm-ups project.");

                        Environment.logError ("Unable to open warmups project",
                                              e);

                        return;

                    }

                    _this.close ();

                }

            });

        JButton c = new JButton ("Cancel");

        c.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.close ();

                }

            });

        JButton[] buts = new JButton[2];
        buts[0] = b;
        buts[1] = c;

        return buts;

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
        
        final JCheckBox doOnStartup = new JCheckBox ("Do a warm-up everytime Quoll Writer starts");
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
