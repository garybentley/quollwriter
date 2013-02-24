package com.quollwriter.ui.panels;

import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;

import java.io.*;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.ArrayList;

import javax.sound.sampled.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ItemAdapter;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.achievements.*;

public class OptionsPanel extends QuollPanel
{
    
    public static final String PANEL_ID = "options";
    
    public OptionsPanel (AbstractProjectViewer pv,
                         Project               p)
                  throws GeneralException
    {

        super (pv,
               p,
               false);

    }
    
    public void init ()
    {
               
        Header h = UIUtils.createHeader ("Options",
                                         Constants.PANEL_TITLE,
                                         "options",
                                         null);

        Box b = new Box (BoxLayout.Y_AXIS);

        final JScrollPane lscroll = new JScrollPane (b);
        lscroll.setBorder (null);
        lscroll.setOpaque (false);
        lscroll.getViewport ().setBorder (null);
        lscroll.getViewport ().setOpaque (false);
        lscroll.getVerticalScrollBar ().setUnitIncrement (20);
        lscroll.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.add (lscroll);
        
        b.setOpaque (false);

        b.add (h);

        b.add (Box.createVerticalStrut (5));        

        Box bb = new Box (BoxLayout.Y_AXIS);
        
        bb.setBorder (new EmptyBorder (0, 10, 10, 10));
           
        b.add (bb);
           
        this.addProjectAndSnapshotsSection (bb);

        bb.add (Box.createVerticalStrut (20));
                                   
        this.addHowThingsLookSection (bb);
                
        bb.add (Box.createVerticalStrut (20));
                
        this.addEditingChaptersSection (bb);
        
        bb.add (Box.createVerticalStrut (20));

        this.addItemsAndRulesSection (bb);

        bb.add (Box.createVerticalStrut (20));

        this.addWarmupsSection (bb);
        
        bb.add (Box.createVerticalStrut (20));

        this.addAchievementsSection (bb);

        bb.add (Box.createVerticalStrut (20));
        
        b.add (Box.createVerticalGlue ());
               
        SwingUtilities.invokeLater (new Runnable ()
        {
            
            public void run ()
            {
                
                lscroll.getVerticalScrollBar ().setValue (0);
                
            }
            
        });
               
    }

    private void addWarmupsSection (Box box)
    {

        final OptionsPanel _this = this;
        
        box.add (this.createHeader (UIUtils.formatForUser ("Warm-ups")));
        box.add (Box.createVerticalStrut (5));
        
        JComponent c = this.createWrapper (WarmupPromptSelect.getDoWarmupOnStartupCheckbox ());
        this.setAsMainItem (c);
        
        box.add (c);

        box.add (Box.createVerticalStrut (5));
        
        c = this.createHelpText ("And do the warm-up for");
        this.setAsSubItem (c);
        
        box.add (c);
        
        FormLayout fl = new FormLayout ("p, 6px, p, 6px, p, 6px, p",
                                        "p");

        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        final JComboBox words = WarmupPromptSelect.getWordsOptions ();

        builder.add (words,
                     cc.xy (1,
                            1));

        builder.addLabel ("and/or",
                          cc.xy (3,
                                 1));

        final JComboBox mins = WarmupPromptSelect.getTimeOptions ();

        builder.add (mins,
                     cc.xy (5,
                            1));
        
        builder.addLabel ("(whichever is reached first)",
                          cc.xy (7,
                                 1));

        JPanel p = builder.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        c = this.createWrapper (p);
        this.setAsSubItem (c);
        
        box.add (c);
        
    }
    
    private void addAchievementsSection (Box box)
    {
                
        box.add (this.createHeader (UIUtils.formatForUser ("Achievements")));
        box.add (Box.createVerticalStrut (5));

        final AchievementsManager man = Environment.getAchievementsManager ();
        
        final JCheckBox achievementsOn = UIUtils.createCheckBox ("Enable achievements");
        achievementsOn.setSelected (man.isAchievementsEnabled ());
        
        achievementsOn.addActionListener (new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                man.setAchievementsEnabled (achievementsOn.isSelected ());
                
            }
            
        });
        
        JComponent c = this.createWrapper (achievementsOn);
        
        this.setAsMainItem (c);
        
        box.add (c);
        
        final JCheckBox achievementSounds = UIUtils.createCheckBox ("Play a sound when an achievement is reached");
        achievementSounds.setSelected (man.isSoundEnabled ());        

        final JCheckBox fullScreenSoundsOn = UIUtils.createCheckBox ("Play the sound in full screen mode");
        
        achievementSounds.addActionListener (new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                man.setSoundEnabled (achievementSounds.isSelected ());
                
                fullScreenSoundsOn.setEnabled (achievementSounds.isSelected ());
                
            }
            
        });

        box.add (Box.createVerticalStrut (15));
        
        c = this.createWrapper (achievementSounds);
        
        this.setAsMainItem (c);
        
        box.add (c);

        fullScreenSoundsOn.setSelected (man.isSoundsInFullScreenEnabled ());
        
        fullScreenSoundsOn.addActionListener (new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                man.setSoundsInFullScreenEnabled (fullScreenSoundsOn.isSelected ());
                
            }
            
        });

        //box.add (Box.createVerticalStrut (5));
        
        c = this.createWrapper (fullScreenSoundsOn);
        
        this.setAsSubItem (c);
        
        box.add (c);        
        
    }
    
    private void addEditingChaptersSection (Box box)
    {
        
        final OptionsPanel _this = this;
        
        box.add (this.createHeader (UIUtils.formatForUser ("Editing {Chapters}")));
        box.add (Box.createVerticalStrut (5));
        
        Vector chapterA = new Vector ();
        chapterA.add (Constants.MINS_5);
        chapterA.add (Constants.MINS_10);
        chapterA.add (Constants.MINS_20);
        chapterA.add (Constants.MINS_30);
        chapterA.add (Constants.HOUR_1);

        final JComboBox autosaveAmount = new JComboBox (chapterA);

        final JCheckBox enableAutosave = new JCheckBox ("Enable Chapter Auto-save");
        enableAutosave.setOpaque (false);
        enableAutosave.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        JComponent c = this.createWrapper (enableAutosave);
        this.setAsMainItem (c);
        
        box.add (c);
                
        boolean autosaveEnabled = this.projectViewer.getProject ().getPropertyAsBoolean (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME);
        enableAutosave.setSelected (autosaveEnabled);

        autosaveAmount.setSelectedItem (this.projectViewer.getProject ().getProperty (Constants.CHAPTER_AUTO_SAVE_INTERVAL_PROPERTY_NAME));
        autosaveAmount.setEnabled (enableAutosave.isSelected ());
        
        enableAutosave.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                autosaveAmount.setEnabled (enableAutosave.isSelected ());

                Properties props = Environment.getDefaultProperties (Project.OBJECT_TYPE);                
                
                props.setProperty (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME,
                                   new BooleanProperty (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME,
                                                        enableAutosave.isSelected ()));
                
                _this.projectViewer.fireProjectEventLater (ProjectEvent.AUTO_SAVE,
                                                          (enableAutosave.isSelected () ? ProjectEvent.ON : ProjectEvent.OFF));
                
                _this.projectViewer.scheduleAutoSaveForAllEditors ();
                
                _this.updateDefaultProjectProperty (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME,
                                                    enableAutosave.isSelected ());

            }

        });
/*
        autoSaveAmount.addActionListener (new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                
                
            }
            
        });
  */      
        autosaveAmount.addItemListener (new ItemAdapter ()
        {
           
            public void itemStateChanged (ItemEvent ev)
            {

                if (ev.getStateChange () != ItemEvent.SELECTED)
                {
                    
                    return;
                    
                }

                _this.updateDefaultProjectProperty (Constants.CHAPTER_AUTO_SAVE_INTERVAL_PROPERTY_NAME,
                                                    (String) autosaveAmount.getSelectedItem ());
                
                _this.projectViewer.scheduleAutoSaveForAllEditors ();
                
            }
            
        });

        box.add (Box.createVerticalStrut (15));
        
        c = this.createHelpText ("Auto-save every");
        this.setAsMainItem (c);
        
        box.add (c);
        
        c = this.createWrapper (autosaveAmount);
        this.setAsSubItem (c);
        
        box.add (c);

        box.add (Box.createVerticalStrut (15));
        
        c = this.createHelpText ("Use the following language for the spellchecker");
        this.setAsMainItem (c);
        
        box.add (c);
        
        Vector langs = new Vector ();
        langs.add (Constants.US_ENGLISH);
        langs.add (Constants.BRITISH_ENGLISH);

        final JComboBox spellcheckLang = new JComboBox (langs);
        spellcheckLang.setSelectedItem (this.projectViewer.getProject ().getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME));
        
        spellcheckLang.addItemListener (new ItemAdapter ()
        {
           
           public void itemStateChanged (ItemEvent ev)
           {
            
                if (ev.getStateChange () != ItemEvent.SELECTED)
                {
                    
                    return;
                    
                }
            
                String lang = spellcheckLang.getSelectedItem ().toString ();
            
                try
                {
        
                    _this.projectViewer.setSpellCheckLanguage (lang,
                                                               true);
        
                } catch (Exception e)
                {
        
                    // Not good but not fatal either.
                    Environment.logError ("Unable to set spell check language to: " +
                                          lang,
                                          e);
        
                    return;
        
                }                

                if (!lang.equals (_this.projectViewer.getProject ().getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME)))
                {
                    
                    _this.projectViewer.fireProjectEventLater (ProjectEvent.SPELL_CHECK,
                                                               ProjectEvent.CHANGE_LANGUAGE);
                    
                }
                
                _this.updateProjectProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME,
                                             lang);
           
                // Ask if they want to set it for all projects not just this one.
                if (JOptionPane.showConfirmDialog (_this.projectViewer,
                                                   "Would you like to set " + lang + " as the default spellchecker language for new projects?",
                                                   UIUtils.getFrameTitle ("Set as default language?"),
                                                   JOptionPane.YES_NO_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
                {
                    
                    _this.updateDefaultProjectProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME,
                                                        lang);
                    
                }
                            
           }
           
        });
        
        c = this.createWrapper (spellcheckLang);
        this.setAsSubItem (c);
        
        box.add (c);
        
        box.add (Box.createVerticalStrut (15));

        JButton b = new JButton ("Manage your Personal Dictionary");

        b.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.projectViewer.showDictionaryManager ();
                
            }

        });

        c = this.createWrapper (b);
        this.setAsMainItem (c);
        
        box.add (c);
                
    }
    
    private void addHowThingsLookSection (Box box)
    {

        final OptionsPanel _this = this;    
    
        final Properties userProps = Environment.getUserProperties ();
    
        box.add (this.createHeader ("How things look and sound"));
        box.add (Box.createVerticalStrut (5));
        
        final JCheckBox showTips = new JCheckBox ("Show useful tips when Quoll Writer starts");
        showTips.setSelected (userProps.getPropertyAsBoolean (Constants.SHOW_TIPS_PROPERTY_NAME));
        showTips.setOpaque (false);
        
        showTips.addItemListener (new ItemAdapter ()
        {
           
            public void itemStateChanged (ItemEvent ev)
            {
                
                _this.updateUserProperty (Constants.SHOW_TIPS_PROPERTY_NAME,
                                          showTips.isSelected ());
                
            }
            
        });
        
        JComponent c = this.createWrapper (showTips);
        this.setAsMainItem (c);
        
        box.add (c);
        
        box.add (Box.createVerticalStrut (15));
        
        c = this.createHelpText ("Show the toolbar");
        this.setAsMainItem (c);
        
        box.add (c);
        
        Vector v = new Vector ();
        v.add (Environment.replaceObjectNames ("Above {Chapters}"));
        v.add (Environment.replaceObjectNames ("Below {Notes}"));

        final JComboBox toolbarLoc = new JComboBox (v);

        String loc = userProps.getProperty (Constants.TOOLBAR_LOCATION_PROPERTY_NAME);

        int ind = 0;

        if (loc.equals (Constants.BOTTOM))
        {

            ind = 1;

        }

        toolbarLoc.setSelectedIndex (ind);        
        
        toolbarLoc.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                if (ev.getStateChange () != ItemEvent.SELECTED)
                {
                    
                    return;
                    
                }

                String loc = Constants.TOP;
        
                if (toolbarLoc.getSelectedIndex () == 1)
                {
        
                    loc = Constants.BOTTOM;
        
                }
        
                if (!loc.equals (_this.projectViewer.getProject ().getProperty (Constants.TOOLBAR_LOCATION_PROPERTY_NAME)))
                {
                    
                    _this.projectViewer.fireProjectEventLater (ProjectEvent.TOOLBAR,
                                                               ProjectEvent.MOVE);            
                    
                }

                _this.projectViewer.setToolbarLocation (loc);
                
                _this.updateUserProperty (Constants.TOOLBAR_LOCATION_PROPERTY_NAME,
                                          loc);
                
            }
            
        });        
        
        c = this.createWrapper (toolbarLoc);
        this.setAsSubItem (c);
        
        box.add (c);

        // Sidebar location
        box.add (Box.createVerticalStrut (15));
        
        c = this.createHelpText ("Show the sidebar");
        this.setAsMainItem (c);
        
        box.add (c);
        
        v = new Vector ();
        v.add (Environment.replaceObjectNames ("On the left"));
        v.add (Environment.replaceObjectNames ("On the right"));

        final JComboBox sidebarLoc = new JComboBox (v);

        loc = userProps.getProperty (Constants.SIDEBAR_LOCATION_PROPERTY_NAME);

        ind = 0;

        if (loc.equals (Constants.RIGHT))
        {

            ind = 1;

        }

        sidebarLoc.setSelectedIndex (ind);        
        
        sidebarLoc.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                if (ev.getStateChange () != ItemEvent.SELECTED)
                {
                    
                    return;
                    
                }

                String loc = Constants.LEFT;
        
                if (sidebarLoc.getSelectedIndex () == 1)
                {
        
                    loc = Constants.RIGHT;
        
                }
        
                if (!loc.equals (userProps.getProperty (Constants.SIDEBAR_LOCATION_PROPERTY_NAME)))
                {
                    
                    _this.projectViewer.fireProjectEventLater (ProjectEvent.SIDEBAR,
                                                               ProjectEvent.MOVE);            
                    
                }
                
                _this.projectViewer.setSidebarLocation (loc);
                
                _this.updateUserProperty (Constants.SIDEBAR_LOCATION_PROPERTY_NAME,
                                          loc);

            }
            
        });        
        
        c = this.createWrapper (sidebarLoc);
        this.setAsSubItem (c);
        
        box.add (c);
        
        box.add (Box.createVerticalStrut (15));

        // Sidebar location        
        c = this.createHelpText ("Show the tabs");
        this.setAsMainItem (c);
        
        box.add (c);        
        
        v = new Vector ();
        v.add (Environment.replaceObjectNames ("At the top"));
        v.add (Environment.replaceObjectNames ("At the bottom"));

        final JComboBox tabsLoc = new JComboBox (v);

        loc = userProps.getProperty (Constants.TABS_LOCATION_PROPERTY_NAME);

        ind = 0;

        if (loc.equals (Constants.BOTTOM))
        {

            ind = 1;

        }

        tabsLoc.setSelectedIndex (ind);        
        
        tabsLoc.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                if (ev.getStateChange () != ItemEvent.SELECTED)
                {
                    
                    return;
                    
                }

                String loc = Constants.TOP;
        
                if (tabsLoc.getSelectedIndex () == 1)
                {
        
                    loc = Constants.BOTTOM;
        
                }

                if (tabsLoc.getSelectedIndex () == 2)
                {
        
                    loc = Constants.LEFT;
        
                }

                if (tabsLoc.getSelectedIndex () == 3)
                {
        
                    loc = Constants.RIGHT;
        
                }
        
                if (!loc.equals (userProps.getProperty (Constants.TABS_LOCATION_PROPERTY_NAME)))
                {
                    
                    _this.projectViewer.fireProjectEventLater (ProjectEvent.TABS,
                                                               ProjectEvent.MOVE);            
                    
                }
                
                _this.projectViewer.setTabsLocation (loc);
                
                _this.updateUserProperty (Constants.TABS_LOCATION_PROPERTY_NAME,
                                          loc);

            }
            
        });        
        
        c = this.createWrapper (tabsLoc);
        this.setAsSubItem (c);
        
        box.add (c);
        
        box.add (Box.createVerticalStrut (15));
                
        final JCheckBox playSound = new JCheckBox (UIUtils.formatForUser ("Play a typewriter sound when editing a {chapter}."));

        boolean playSoundEnabled = userProps.getPropertyAsBoolean (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME);

        playSound.setOpaque (false);
        playSound.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                try
                {
                    
                    _this.projectViewer.setKeyStrokeSoundFile (null,
                                                               playSound.isSelected ());
                    
                    _this.updateUserProperty (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME,
                                              playSound.isSelected ());

                } catch (Exception e)
                {
        
                    Environment.logError ("Unable to set key stroke sound file to the default",
                                          e);
        
                }                
                
            }

        });

        playSound.setSelected (playSoundEnabled);
        
        box.add (Box.createVerticalStrut (5));
        
        c = this.createWrapper (playSound);
        this.setAsMainItem (c);
        
        box.add (c);
        
        box.add (Box.createVerticalStrut (5));
        
        c = this.createHelpText ("Or, select your own WAV file that will be played instead. (Note: only .wav files are supported.)");
        this.setAsSubItem (c);
        
        box.add (c);
        
        String sf = userProps.getProperty (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);

        if (sf == null)
        {

            sf = "";

        }

        final String sfv = sf;

        final JButton useB = new JButton ("Use Sound");
        final JButton testB = new JButton ("Play Sound");                

        final FileFinder f = new FileFinder ();
        
        f.setFile (new File (sfv));
        f.setApproveButtonText ("Select");
        f.setFinderSelectionMode (JFileChooser.FILES_ONLY);
        f.setFinderTitle ("Select a File");
        
        f.setFileFilter (new FileNameExtensionFilter ("Supported Files (wav)",
                                                      "wav"));
        
        f.setFindButtonToolTip ("Click to find a wav file");
        f.setClearOnCancel (true);
        
        f.setOnSelectHandler (new ActionAdapter ()
        {
                                                        
            public void actionPerformed (ActionEvent ev)
            {

                useB.setEnabled (true);
                
            }
            
        });
        
        f.showCancel (true,
                      new ActionAdapter ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                Properties props = Environment.getDefaultProperties (Project.OBJECT_TYPE);                                        
                
                props.removeProperty (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);
                
                useB.setEnabled (false);                
                
            }
            
        });

        f.init ();
        
        c = this.createWrapper (f);
        this.setAsSubItem (c);
        
        box.add (c);
        
        useB.setEnabled (false);
        
        if ((sfv != null)
            &&
            (sfv.trim ().length () > 0)
           )
        {
            
            useB.setEnabled (true);
            
        }
        
        useB.addActionListener (new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {

                File file = f.getSelectedFile ();
            
                if (file != null)
                {
    
                    try
                    {
                
                        _this.projectViewer.setKeyStrokeSoundFile (file,
                                                                   playSound.isSelected ());
    
                        _this.updateUserProperty (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME,
                                                  file.getPath ());
    
                    } catch (Exception e)
                    {
            
                        Environment.logError ("Unable to set key stroke sound file to: " +
                                              file.getPath (),
                                              e);
            
                    }

                }
                                
            }
            
        });
        
        testB.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                File file = f.getSelectedFile ();
System.out.println ("FILE: " + file);
                try
                {

                    InputStream is = null;

                    if (file != null)
                    {
                    
                        if ((file.exists ()) &&
                            (file.getName ().toLowerCase ().endsWith (".wav")))
                        {
    
                            is = new BufferedInputStream (new FileInputStream (file));
    
                        } else
                        {
    
                            UIUtils.showErrorMessage (_this,
                                                      file + " is not a valid .wav file.");
    
                            return;
    
                        }

                    }
                        
                    if (is == null)
                    {

                        // Play the default.
                        is = Environment.getResourceStream (Constants.DEFAULT_KEY_STROKE_SOUND_FILE);

                    }

                    // Get the clip.
                    AudioInputStream ais = AudioSystem.getAudioInputStream (is);

                    Clip c = AudioSystem.getClip ();

                    c.open (ais);

                    c.start ();

                } catch (Exception e)
                {

                    Environment.logError ("Unable to play sound file: " +
                                          file,
                                          e);

                    UIUtils.showErrorMessage (_this,
                                              "Unable to play sound file.");

                }

            }

        });

        JButton buts[] = new JButton[2];
        
        buts[0] = useB;
        buts[1] = testB;
        
        JPanel bp = ButtonBarFactory.buildLeftAlignedBar (buts);
        bp.setOpaque (false);

        c = this.createWrapper (bp);
        
        box.add (Box.createVerticalStrut (5));

        this.setAsSubItem (c);
        
        box.add (c);

    }

    private void updateUserProperty (String  name,
                                     boolean value)
    {
        
        try
        {
    
            Environment.setUserProperty (name,
                                         new BooleanProperty (name,
                                                              value));
    
        } catch (Exception e)
        {
    
            Environment.logError ("Unable to save user properties",
                                  e);
    
            UIUtils.showErrorMessage (this,
                                      "Unable to save user properties");
    
        }

        this.projectViewer.getProject ().getProperties ().removeProperty (name);        
        
    }
    
    private void updateUserProperty (String name,
                                     String value)
    {
        
        try
        {
    
            Environment.setUserProperty (name,
                                         new StringProperty (name,
                                                             value));
    
        } catch (Exception e)
        {
    
            Environment.logError ("Unable to save user properties",
                                  e);
    
            UIUtils.showErrorMessage (this,
                                      "Unable to save user properties");
    
        }

        this.projectViewer.getProject ().getProperties ().removeProperty (name);        

    }

    private void updateDefaultProjectProperty (String name,
                                               String value)
    {
        
        Properties props = Environment.getDefaultProperties (Project.OBJECT_TYPE);
        
        props.setProperty (name,
                           new StringProperty (name,
                                               value));

        try
        {
    
            Environment.saveDefaultProperties (Project.OBJECT_TYPE,
                                               props);
    
        } catch (Exception e)
        {
    
            Environment.logError ("Unable to save default project properties",
                                  e);
    
            UIUtils.showErrorMessage (this,
                                      "Unable to save default project properties");
    
        }
                
    }

    private void updateDefaultProjectProperty (String  name,
                                               boolean value)
    {
        
        Properties props = Environment.getDefaultProperties (Project.OBJECT_TYPE);
        
        props.setProperty (name,
                           new BooleanProperty (name,
                                                value));

        try
        {
    
            Environment.saveDefaultProperties (Project.OBJECT_TYPE,
                                               props);
    
        } catch (Exception e)
        {
    
            Environment.logError ("Unable to save default project properties",
                                  e);
    
            UIUtils.showErrorMessage (this,
                                      "Unable to save default project properties");
    
        }
                
    }
    
    private void updateProjectProperty (String name,
                                        String value)
    {
     
        Properties props = this.projectViewer.getProject ().getProperties ();

        props.setProperty (name,
                           new StringProperty (name,
                                               value));
        
        try
        {

            this.projectViewer.saveProject ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to save project",
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to save.");

            return;

        }        
        
    }
    
    private void addProjectAndSnapshotsSection (Box box)
    {

        final OptionsPanel _this = this;
        
        box.add (this.createHeader (Environment.replaceObjectNames ("Project & Snapshots")));
        box.add (Box.createVerticalStrut (5));

        Vector snapshotA = new Vector ();
        snapshotA.add (Constants.HOURS_12);
        snapshotA.add (Constants.HOURS_24);
        snapshotA.add (Constants.DAYS_2);
        snapshotA.add (Constants.DAYS_5);
        snapshotA.add (Constants.WEEK_1);

        final JComboBox snapshotAmount = new JComboBox (snapshotA);        

        final JCheckBox enableSnapshots = new JCheckBox ("Automatically create snapshots of the project");
        enableSnapshots.setOpaque (false);        
        
        JComponent c = this.createWrapper (enableSnapshots);
        this.setAsMainItem (c);
        
        box.add (c);
        
        box.add (Box.createVerticalStrut (5));
        
        boolean snapshotsEnabled = this.projectViewer.getProject ().getPropertyAsBoolean (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME);
        enableSnapshots.setSelected (snapshotsEnabled);        

        snapshotAmount.setSelectedItem (this.projectViewer.getProject ().getProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME));
        snapshotAmount.setEnabled (enableSnapshots.isSelected ());
        
        snapshotAmount.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                if (ev.getStateChange () != ItemEvent.SELECTED)
                {
                    
                    return;
                    
                }
                
                _this.updateDefaultProjectProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME,
                                                    (String) snapshotAmount.getSelectedItem ());
                
            }

        });
        
        enableSnapshots.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                snapshotAmount.setEnabled (enableSnapshots.isSelected ());

                _this.updateDefaultProjectProperty (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME,
                                                    enableSnapshots.isSelected ());
                
            }

        });

        c = this.createHelpText ("Create a new snapshot after the following time between sessions");
        this.setAsSubItem (c);
        
        box.add (c);
        
        c = this.createWrapper (snapshotAmount);
        this.setAsSubItem (c);
        
        box.add (c);

        final JButton b = new JButton ("Change");
                
        final FileFinder f = UIUtils.createFileFind (this.projectViewer.getProject ().getProjectDirectory ().getParentFile ().getPath (),
                                                     "Select a Directory",
                                                     JFileChooser.DIRECTORIES_ONLY,
                                                     "Select",
                                                     null);
        f.setFindButtonToolTip ("Click to find a new project directory");
        
        f.setOnSelectHandler (new ActionAdapter ()
        {
                                                        
            public void actionPerformed (ActionEvent ev)
            {

                b.setEnabled (!f.getSelectedFile ().getPath ().equals (_this.projectViewer.getProject ().getProjectDirectory ().getParentFile ().getPath ()));
                
            }
            
        });
        
        box.add (Box.createVerticalStrut (15));
        
        c = this.createHelpText ("Select the directory where your project is stored");
        this.setAsMainItem (c);

        box.add (c);
        
        c = this.createWrapper (f);
        this.setAsSubItem (c);        

        box.add (c);
        
        box.add (Box.createVerticalStrut (5));
                
        b.setEnabled (false);
        
        b.addActionListener (new ActionAdapter ()
        {
         
             public void actionPerformed (ActionEvent ev)
             {
                 
                 _this.handleProjectDirChange (f);

             }
         
        });

        c = this.createWrapper (b);
        this.setAsSubItem (c);
        
        box.add (c);
                
    }
    
    private boolean handleProjectDirChange (FileFinder f)
    {
        
        Project proj = this.projectViewer.getProject ();
        
        File oldProjDir = proj.getProjectDirectory ();
        
        File newDir = f.getSelectedFile ();
        
        File newProjDir = new File (newDir.getPath () + "/" + Utils.sanitizeForFilename (proj.getName ()));

        // See if the project directory is changing.
        if (!newDir.equals (oldProjDir))
        {

            if (Utils.isDirectoryEmpty (newDir))
            {

                UIUtils.showErrorMessage (this.projectViewer,
                                          "Unable to change directory to: " +
                                          newProjDir +
                                          " directory is not empty.");

                f.setFile (oldProjDir);
                                          
                return false;

            }

            // Is changing so need to close the project.
            if (JOptionPane.showConfirmDialog (this.projectViewer,
                                               "Warning!  To change the directory of a project it must first be saved and closed.\n\nOnce the directory has been changed the project will be reopened.\n\nDo you wish to continue?",
                                               UIUtils.getFrameTitle ("Confirm project directory change?"),
                                               JOptionPane.YES_NO_OPTION,
                                               JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
            {

                this.projectViewer.close (true);
    
                // Past here we can't stop the event anyway.
    
                try
                {
    
                    Utils.copyDir (proj.getProjectDirectory (),
                                   newProjDir);
    
                } catch (Exception e)
                {
    
                    Environment.logError ("Unable to copy directory: " +
                                          proj.getProjectDirectory ().getParentFile () +
                                          " to: " +
                                          newProjDir.getParentFile (),
                                          e);
    
                    f.setFile (oldProjDir);
    
                    return true;
    
                }
    
                try
                {
    
                    Environment.changeProjectDir (proj,
                                                  newProjDir);
    
                } catch (Exception e)
                {
    
                    Environment.logError ("Unable to change project directory (probably an error with the projects file): " +
                                          proj,
                                          e);
    
                    f.setFile (oldProjDir);
    
                    return true;
    
                }
    
                File oldDir = proj.getProjectDirectory ();
    
                proj.setProjectDirectory (newProjDir);
    
                // Open the project again.
                try
                {
    
                    Environment.openProject (proj);
    
                } catch (Exception e)
                {
    
                    Environment.logError ("Unable to reopen project: " +
                                          proj,
                                          e);
                    
                    return true;
    
                }
    
                // Finally, delete the old project directory.
                Utils.deleteDir (oldDir);
    
                Environment.getProjectViewer (proj).fireProjectEventLater (proj.getObjectType (),
                                                                           ProjectEvent.CHANGED_DIRECTORY);
            
                return true;
            
            }

            // Reset the file.
            f.setFile (oldProjDir);

            
        }

        return false;
        
    }
    
    private void addItemsAndRulesSection (Box box)
    {

        final OptionsPanel _this = this;
            
        box.add (this.createHeader ("Manage Items & Rules"));
        box.add (Box.createVerticalStrut (10));

        JButton[] buts = new JButton[ 3 ];
        
        JButton b = new JButton ("Note Types");

        b.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                ((ProjectViewer) _this.projectViewer).showEditNoteTypes ();

            }

        });

        buts[0] = b;
        
        b = new JButton ("Item Types");

        b.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                ((ProjectViewer) _this.projectViewer).showEditItemTypes ();

            }

        });
        
        buts[1] = b;

        b = new JButton ("Problem Finder Rules");
        
        b.addActionListener (new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                ProblemFinderRuleConfig conf = new ProblemFinderRuleConfig (_this.projectViewer);

                conf.init ();

                conf.setVisible (true);
                
            }
            
        });
        
        buts[2] = b;
        
        JPanel bp = ButtonBarFactory.buildLeftAlignedBar (buts);
        bp.setOpaque (false);

        JComponent c = this.createWrapper (bp);
        
        this.setAsMainItem (c);
        
        box.add (c);
                
    }
        
    private Header createHeader (String title)
    {
        
        Header h = UIUtils.createHeader (title,
                                         Constants.SUB_PANEL_TITLE,
                                         null,
                                         null);
        
        h.setBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, Environment.getBorderColor ()),
                                                             new EmptyBorder (0, 0, 3, 0)));
        
        return h;
        
    }
    
    private void setAsMainItem (JComponent c)
    {

        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        c.setAlignmentY (Component.TOP_ALIGNMENT);    
    
        c.setBorder (new CompoundBorder (new EmptyBorder (0, 5, 0, 10),
                                         c.getBorder ()));
        
    }

    private void setAsSubItem (JComponent c)
    {

        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        c.setAlignmentY (Component.TOP_ALIGNMENT);    
    
        c.setBorder (new CompoundBorder (new EmptyBorder (0, 15, 0, 10),
                                         c.getBorder ()));
        
    }
    
    private JComponent createWrapper (JComponent c)
    {
        
        if (c instanceof JComboBox)
        {

            c.setMaximumSize (c.getPreferredSize ());

        }

        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        c.setAlignmentY (Component.TOP_ALIGNMENT);

        if (!(c instanceof Box))
        {

            Box _b = new Box (BoxLayout.X_AXIS);
            _b.add (Box.createHorizontalStrut (5));
            _b.add (c);
            _b.add (Box.createHorizontalGlue ());
            _b.setAlignmentX (Component.LEFT_ALIGNMENT);
            _b.setAlignmentY (Component.TOP_ALIGNMENT);

            c = _b;
                        
        } else {
            
            c.setBorder (new EmptyBorder (0, 5, 0, 0));
            
        }
        
        return c;
    
    }
    
    private JTextPane createHelpText (String text)
    {

        JTextPane t = UIUtils.createHelpTextPane (text);
        t.setBorder (new EmptyBorder (0,
                                      5,
                                      5,
                                      5));

        return t;

    }

    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {

    }

    public List<Component> getTopLevelComponents ()
    {

        return null;

    }

    public <T extends NamedObject> void refresh (T n)
    {


    }

    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {

        final OptionsPanel _this = this;
    
        JButton b = UIUtils.createToolBarButton ("options",
                                                       Environment.replaceObjectNames ("This is just a test item so you can see how the toolbar looks when you change it's location."),
                                                       null,
                                                       new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.showMessage (_this,
                                     "This button is here as a test so you can see what the toolbar looks like when you move it.");
            
            }

        });

        toolBar.add (b);
    
        b = UIUtils.createToolBarButton ("options",
                                                       Environment.replaceObjectNames ("This is just a test item so you can see how the toolbar looks when you change it's location."),
                                                       null,
                                                       new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.showMessage (_this,
                                     "This button is here as a test so you can see what the toolbar looks like when you move it.");
            
            }

        });

        toolBar.add (b);

        b = UIUtils.createToolBarButton ("options",
                                                       Environment.replaceObjectNames ("This is just a test item so you can see how the toolbar looks when you change it's location."),
                                                       null,
                                                       new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.showMessage (_this,
                                     "This button is here as a test so you can see what the toolbar looks like when you move it.");
            
            }

        });

        toolBar.add (b);
    
    }

    public boolean saveUnsavedChanges ()
                                throws Exception
    {

        return true;

    }

    public String getPanelId ()
    {

        // Gonna regret this...
        return OptionsPanel.PANEL_ID;

    }

    public void setState (Map<String, String> s,
                          boolean             hasFocus)
    {

    }

    public void getState (Map<String, Object> m)
    {

    }

    public String getTitle ()
    {
        
        return "Options";
        
    }
    
    public String getIconType ()
    {

        return "options";

    }

    public void close ()
    {


    }    
}