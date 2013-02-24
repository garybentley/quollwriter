package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Vector;

import javax.sound.sampled.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;

import com.gentlyweb.properties.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.components.*;

import org.jdom.*;


public class ProjectOptions extends JDialog
{

    private JComboBox         snapshotAmount = null;
    private JCheckBox         enableAutosave = null;
    private JCheckBox         def = null;
    private JComboBox         autosaveAmount = null;
    private JCheckBox         enableSnapshots = null;
    private JComboBox         spellcheckLang = null;
    private JCheckBox         playSound = null;
    private JTextField        projDir = null;
    private JTextField        playSoundFile = null;
    private JComboBox         toolbarLoc = null;
    private JCheckBox         showTips = null;

    private AbstractProjectViewer projectViewer = null;

    public ProjectOptions(AbstractProjectViewer pv)
    {

        super (pv,
               UIUtils.getFrameTitle (pv.getProject ().getName () + ": " + Environment.getObjectTypeName (Project.OBJECT_TYPE) + " Options"));

        this.projectViewer = pv;

        this.setMinimumSize (new Dimension (500,
                                            0));

        this.setIconImage (Environment.getWindowIcon ().getImage ());

        final ProjectOptions _this = this;

        this.setDefaultCloseOperation (WindowConstants.HIDE_ON_CLOSE);

        JPanel content = new JPanel (new BorderLayout (),
                                     true);

        Box b = new Box (BoxLayout.PAGE_AXIS);
        content.add (b);

        b.setOpaque (false);
        b.setBorder (new EmptyBorder (5,
                                      5,
                                      5,
                                      5));

        // Add a tabbed pane.
        JTabbedPane tp = new JTabbedPane ();
        b.add (tp);

        tp.add (Environment.getObjectTypeName (Project.OBJECT_TYPE),
                this.createProjectOptions ());

        tp.add (Environment.getObjectTypeName (Chapter.OBJECT_TYPE),
                this.createChapterOptions ());

        b.add (Box.createVerticalStrut (5));

        java.util.List bs = new ArrayList ();

        JButton but = new JButton (Form.SAVE_BUTTON_LABEL);

        but.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.saveOptions ();

                    _this.setVisible (false);
                    _this.dispose ();

                }

            });

        bs.add (but);

        but = new JButton (Form.CANCEL_BUTTON_LABEL);

        but.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.setVisible (false);
                    _this.dispose ();

                }

            });

        bs.add (but);

        b.add (ButtonBarFactory.buildRightAlignedBar ((JButton[]) bs.toArray (new JButton[bs.size ()])));

        this.getContentPane ().add (content);

        this.setResizable (false);

        this.pack ();

        this.setPreferredSize (new Dimension (520,
                                              this.getPreferredSize ().height));

        this.pack ();

    }

    private void saveOptions ()
    {

        // Check the sound file.
        if (this.playSound.isSelected ())
        {

            if (this.playSoundFile.getText ().trim ().length () > 0)
            {

                File f = new File (this.playSoundFile.getText ());

                if (!f.exists ())
                {

                    UIUtils.showErrorMessage (this.projectViewer,
                                              "Sound file: " +
                                              this.playSoundFile.getText () +
                                              " does not exist.");

                    return;

                }

                if (!f.getName ().toLowerCase ().endsWith ("wav"))
                {

                    UIUtils.showErrorMessage (this.projectViewer,
                                              "Sound file: " +
                                              this.playSoundFile.getText () +
                                              " is not a .wav file.");

                    return;

                }

            }

        }

        boolean changeDir = false;

        // Save the project options.
        Project proj = this.projectViewer.getProject ();

        try
        {

            Utils.createQuollWriterDirFile (new File (proj.getProjectDirectory ().getPath () + "/projectdb.lobs.db"));

        } catch (Exception e)
        {

            // Ignore for now.
            Environment.logError ("Unable to add can delete dir file to: " +
                                  proj.getProjectDirectory ().getPath () + "/projectdb.lobs.db",
                                  e);

        }

        File newDir = new File (this.projDir.getText () + "/" + Utils.sanitizeForFilename (proj.getName ()));

        // See if the project directory is changing.
        if (!newDir.equals (proj.getProjectDirectory ()))
        {

            if (newDir.exists ())
            {

                UIUtils.showErrorMessage (this.projectViewer,
                                          "Unable to change directory to: " +
                                          this.projDir.getText () +
                                          " a directory with path:\n\n    " +
                                          newDir.getPath () +
                                          "\n\nalready exists.");

                return;

            }

            // Is changing so need to close the project.
            if (JOptionPane.showConfirmDialog (this.projectViewer,
                                               "Warning!  To change the directory of a project it must first be saved and closed.\n\nOnce the directory has been changed the project will be reopened.\n\nDo you wish to continue?",
                                               UIUtils.getFrameTitle ("Confirm project directory change?"),
                                               JOptionPane.YES_NO_OPTION,
                                               JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
            {

                changeDir = true;

            }

        }

        Properties props = null;

        if (this.def.isSelected ())
        {

            props = Environment.getDefaultProperties (Project.OBJECT_TYPE);

        } else
        {

            props = proj.getProperties ();

        }

        props.setProperty (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME,
                           new BooleanProperty (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME,
                                                this.enableSnapshots.isSelected ()));
        props.setProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME,
                           new StringProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME,
                                               (String) this.snapshotAmount.getSelectedItem ()));

        props.setProperty (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME,
                           new BooleanProperty (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME,
                                                this.enableAutosave.isSelected ()));
props.removeProperty (Constants.SHOW_TIPS_PROPERTY_NAME);
        try
        {

            Environment.setUserProperty (Constants.SHOW_TIPS_PROPERTY_NAME,
                                         new BooleanProperty (Constants.SHOW_TIPS_PROPERTY_NAME,
                                                              this.showTips.isSelected ()));

        } catch (Exception e) {
            
            Environment.logError ("Unable to set show tips",
                                  e);
            
        }
        
        Environment.getProjectViewer (proj).fireProjectEventLater (ProjectEvent.AUTO_SAVE,
                                                                   (this.enableAutosave.isSelected () ? ProjectEvent.ON : ProjectEvent.OFF));
        
        props.setProperty (Constants.CHAPTER_AUTO_SAVE_INTERVAL_PROPERTY_NAME,
                           new StringProperty (Constants.CHAPTER_AUTO_SAVE_INTERVAL_PROPERTY_NAME,
                                               (String) this.autosaveAmount.getSelectedItem ()));
        
        if (!((String) this.spellcheckLang.getSelectedItem ()).equals (proj.getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME)))
        {
            
            Environment.getProjectViewer (proj).fireProjectEventLater (ProjectEvent.SPELL_CHECK,
                                                                       ProjectEvent.CHANGE_LANGUAGE);
            
        }
        
        props.setProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME,
                           new StringProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME,
                                               (String) this.spellcheckLang.getSelectedItem ()));

        props.setProperty (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME,
                           new BooleanProperty (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME,
                                                this.playSound.isSelected ()));
        props.setProperty (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME,
                           new StringProperty (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME,
                                               this.playSoundFile.getText ()));

        String loc = Constants.TOP;

        if (this.toolbarLoc.getSelectedIndex () == 1)
        {

            loc = Constants.BOTTOM;

        }

        if (!loc.equals (proj.getProperty (Constants.TOOLBAR_LOCATION_PROPERTY_NAME)))
        {
            
            Environment.getProjectViewer (proj).fireProjectEventLater (ProjectEvent.TOOLBAR,
                                                                       ProjectEvent.MOVE);            
            
        }

        props.setProperty (Constants.TOOLBAR_LOCATION_PROPERTY_NAME,
                           new StringProperty (Constants.TOOLBAR_LOCATION_PROPERTY_NAME,
                                               loc));

        // See if we need to set these as the defaults.
        if (this.def.isSelected ())
        {

            // Save the user options.
            try
            {

                Environment.saveDefaultProperties (Project.OBJECT_TYPE,
                                                   props);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save user properties",
                                      e);

                UIUtils.showErrorMessage (this,
                                          "Unable to save user properties");

            }

            props = this.projectViewer.getProject ().getProperties ();

            // Unset the properties from the project.
            props.removeProperty (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME);
            props.removeProperty (Constants.CHAPTER_AUTO_SAVE_INTERVAL_PROPERTY_NAME);
            props.removeProperty (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME);
            props.removeProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME);
            props.removeProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME);
            props.removeProperty (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME);
            props.removeProperty (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);
            props.removeProperty (Constants.TOOLBAR_LOCATION_PROPERTY_NAME);

        }

        try
        {

            File f = null;

            if (this.playSoundFile.getText ().trim ().length () > 0)
            {

                f = new File (this.playSoundFile.getText ());

            }

            this.projectViewer.setKeyStrokeSoundFile (f,
                                                      this.playSound.isSelected ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to set key stroke sound file to: " +
                                  this.playSoundFile.getText (),
                                  e);

        }

        try
        {

            this.projectViewer.setSpellCheckLanguage (this.spellcheckLang.getSelectedItem ().toString (),
                                                      true);

        } catch (Exception e)
        {

            // Not good but not fatal either.
            Environment.logError ("Unable to set spell check language to: " +
                                  this.spellcheckLang.getSelectedItem (),
                                  e);

        }

        this.projectViewer.setToolbarLocation (loc);

        this.projectViewer.scheduleAutoSaveForAllEditors ();

        // Save the general options.

        // Save the project.
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

        // See if the project directory is changing.
        if (changeDir)
        {

            this.projectViewer.close (true);

            try
            {

                Utils.copyDir (proj.getProjectDirectory (),
                               newDir);

            } catch (Exception e)
            {

                Environment.logError ("Unable to copy directory: " +
                                      proj.getProjectDirectory ().getParentFile () +
                                      " to: " +
                                      newDir.getParentFile (),
                                      e);

                return;

            }

            try
            {

                Environment.changeProjectDir (proj,
                                              newDir);

            } catch (Exception e)
            {

                Environment.logError ("Unable to change project directory (probably an error with the projects file): " +
                                      proj,
                                      e);

            }

            File oldDir = proj.getProjectDirectory ();

            proj.setProjectDirectory (newDir);

            // Open the project again.
            try
            {

                Environment.openProject (proj);

            } catch (Exception e)
            {

                Environment.logError ("Unable to reopen project: " +
                                      proj,
                                      e);

            }

            // Finally, delete the old project directory.
            Utils.deleteDir (oldDir);

            Environment.getProjectViewer (proj).fireProjectEventLater (proj.getObjectType (),
                                                                       ProjectEvent.CHANGED_DIRECTORY);

        }

    }

    private Box createProjectOptions ()
    {

        final ProjectOptions _this = this;

        // Create the project options.
        Box projOptsBox = new Box (BoxLayout.PAGE_AXIS);
        projOptsBox.setBorder (new EmptyBorder (3,
                                                3,
                                                3,
                                                3));

        java.util.List its = new ArrayList ();

        this.enableSnapshots = new JCheckBox ("Enable Auto-snapshots");
        this.enableSnapshots.setOpaque (false);
        this.enableSnapshots.addItemListener (new ItemAdapter ()
            {

                public void itemStateChanged (ItemEvent ev)
                {

                    _this.snapshotAmount.setEnabled (_this.enableSnapshots.isSelected ());

                }

            });

        its.add (this.enableSnapshots);
        its.add ("Create a new snapshot after the following time between sessions:");

        Vector snapshotA = new Vector ();
        snapshotA.add (Constants.HOURS_12);
        snapshotA.add (Constants.HOURS_24);
        snapshotA.add (Constants.DAYS_2);
        snapshotA.add (Constants.DAYS_5);
        snapshotA.add (Constants.WEEK_1);

        this.snapshotAmount = new JComboBox (snapshotA);

        this.snapshotAmount.setSelectedItem (this.projectViewer.getProject ().getProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME));
        this.snapshotAmount.setEnabled (this.enableSnapshots.isSelected ());

        boolean snapshotsEnabled = this.projectViewer.getProject ().getPropertyAsBoolean (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME);
        this.enableSnapshots.setSelected (snapshotsEnabled);

        its.add (this.snapshotAmount);

        this.addSection (projOptsBox,
                         "Snapshots",
                         "When enabled snapshots of the project will be automatically created if the specified amount of time has past since the last edit.",
                         its);

        its = new ArrayList ();

        JButton b = new JButton ("Manage Note Types");

        b.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                ((ProjectViewer) _this.projectViewer).showEditNoteTypes ();

            }

        });

        its.add (b);

        this.addSection (projOptsBox,
                         "Note Types",
                         "Click on the button below to manage the types of notes.",
                         its);

        its = new ArrayList ();

        Box db = new Box (BoxLayout.X_AXIS);
        db.setAlignmentX (Component.LEFT_ALIGNMENT);
        its.add (db);

        this.projDir = UIUtils.createTextField ();
        this.projDir.setText (this.projectViewer.getProject ().getProjectDirectory ().getParentFile ().getPath ());
        this.projDir.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.projDir.setPreferredSize (new Dimension (350,
                                                      this.projDir.getPreferredSize ().height));

        db.add (this.projDir);
        db.add (Box.createHorizontalStrut (2));

        JButton dbut = new JButton (Environment.getIcon ("find",
                                                         Constants.ICON_MENU));
        db.add (dbut);

        dbut.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    JFileChooser f = new JFileChooser ();
                    f.setDialogTitle ("Select a Directory");
                    f.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
                    f.setApproveButtonText ("Select");
                    f.setCurrentDirectory (new File (_this.projDir.getText ()));

                    // Need to run: attrib -r "%USERPROFILE%\My Documents" on XP to allow a new directory
                    // to be created in My Documents.

                    if (f.showOpenDialog (_this) == JFileChooser.APPROVE_OPTION)
                    {

                        _this.projDir.setText (f.getSelectedFile ().getPath ());

                    }

                }

            });

        db.setMaximumSize (db.getPreferredSize ());

        this.addSection (projOptsBox,
                         "Project Directory",
                         "The field below allows you to specify which directory the project files should be placed in.",
                         its);

        Vector v = new Vector ();
        v.add (Environment.replaceObjectNames ("Above {Chapters}"));
        v.add (Environment.replaceObjectNames ("Below {Notes}"));

        this.toolbarLoc = new JComboBox (v);

        String loc = this.projectViewer.getProject ().getProperty (Constants.TOOLBAR_LOCATION_PROPERTY_NAME);

        int ind = 0;

        if (loc.equals (Constants.BOTTOM))
        {

            ind = 1;

        }

        this.toolbarLoc.setSelectedIndex (ind);

        its = new ArrayList ();
        its.add (this.toolbarLoc);

        this.addSection (projOptsBox,
                         "Toolbar Location",
                         "Select where the toolbar for {Chapters}/{Assets} is placed.",
                         its);

        this.showTips = new JCheckBox ("Show tips when Quoll Writer starts");
        this.showTips.setSelected (this.projectViewer.getProject ().getPropertyAsBoolean (Constants.SHOW_TIPS_PROPERTY_NAME));
        this.showTips.setOpaque (false);
        
        its = new ArrayList ();
        its.add (this.showTips);

        this.addSection (projOptsBox,
                         "Tips",
                         null,
                         its);

        this.def = new JCheckBox ("Set as defaults");
        this.def.setOpaque (false);

        its = new ArrayList ();
        its.add (this.def);

        this.addSection (projOptsBox,
                         "Defaults",
                         "Use the checkbox below to set these options as the defaults for all projects (including projects where you have not yet explicitly set the options).",
                         its);

        JComponent cc = (JComponent) Box.createVerticalGlue ();
/*
        cc.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                          Short.MAX_VALUE));
*/
        projOptsBox.add (cc);

        return projOptsBox;

    }

    private Box createChapterOptions ()
    {

        final ProjectOptions _this = this;

        // Create the chapter options.
        Box projOptsBox = new Box (BoxLayout.PAGE_AXIS);
        projOptsBox.setBorder (new EmptyBorder (3,
                                                3,
                                                3,
                                                3));

        java.util.List its = new ArrayList ();

        Vector chapterA = new Vector ();
        chapterA.add (Constants.MINS_5);
        chapterA.add (Constants.MINS_10);
        chapterA.add (Constants.MINS_20);
        chapterA.add (Constants.MINS_30);
        chapterA.add (Constants.HOUR_1);

        this.autosaveAmount = new JComboBox (chapterA);

        this.enableAutosave = new JCheckBox ("Enable Auto-save");
        this.enableAutosave.setOpaque (false);
        this.enableAutosave.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.enableAutosave.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                _this.autosaveAmount.setEnabled (_this.enableAutosave.isSelected ());

            }

        });

        boolean autosaveEnabled = this.projectViewer.getProject ().getPropertyAsBoolean (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME);
        this.enableAutosave.setSelected (autosaveEnabled);

        its.add (this.enableAutosave);
        its.add ("Auto-save after:");

        this.autosaveAmount.setSelectedItem (this.projectViewer.getProject ().getProperty (Constants.CHAPTER_AUTO_SAVE_INTERVAL_PROPERTY_NAME));
        this.autosaveAmount.setEnabled (this.enableAutosave.isSelected ());

        its.add (this.autosaveAmount);

        this.addSection (projOptsBox,
                         "Chapter Auto-save",
                         "When enabled chapters will be automatically saved after the specified intervals (applies when project is next opened).",
                         its);

        Vector langs = new Vector ();
        langs.add (Constants.US_ENGLISH);
        langs.add (Constants.BRITISH_ENGLISH);

        this.spellcheckLang = new JComboBox (langs);
        this.spellcheckLang.setSelectedItem (this.projectViewer.getProject ().getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME));

        its = new ArrayList ();
        its.add (this.spellcheckLang);

        its.add ("Use the button below to manage the words in your personal dictionary.");

        JButton b = new JButton ("Manage Personal Dictionary");

        b.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.projectViewer.showDictionaryManager ();
                    
                }

            });

        its.add (b);

        this.addSection (projOptsBox,
                         "Spellchecker",
                         "Select the language used for the spellcheck.",
                         its);

        its = new ArrayList ();

        this.playSound = new JCheckBox ("Play a typewriter sound whenever a key is pressed");

        boolean playSoundEnabled = this.projectViewer.getProject ().getPropertyAsBoolean (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME);

        this.playSound.setOpaque (false);
        this.playSound.addItemListener (new ItemAdapter ()
            {

                public void itemStateChanged (ItemEvent ev)
                {

                    _this.playSoundFile.setEnabled (_this.playSound.isSelected ());

                }

            });

        its.add (this.playSound);

        its.add ("Alternatively, select a WAV file that will be played instead. (Note: only .wav files are supported.)");

        Box db = new Box (BoxLayout.X_AXIS);
        db.setAlignmentX (Component.LEFT_ALIGNMENT);
        its.add (db);

        String sf = this.projectViewer.getProject ().getProperty (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);

        if (sf == null)
        {

            sf = "";

        }

        final String sfv = sf;

        this.playSoundFile = UIUtils.createTextField ();
        this.playSoundFile.setText (sfv);
        this.playSoundFile.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.playSoundFile.setMaximumSize (new Dimension (350,
                                                          this.playSoundFile.getPreferredSize ().height));

        db.add (this.playSoundFile);
        db.add (Box.createHorizontalStrut (2));

        JButton dbut = new JButton (Environment.getIcon ("find",
                                                         Constants.ICON_MENU));
        db.add (dbut);

        dbut.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    JFileChooser f = new JFileChooser ();
                    f.setDialogTitle ("Select a File");
                    f.setFileSelectionMode (JFileChooser.FILES_ONLY);
                    f.setApproveButtonText ("Select");

                    if (!sfv.equals (""))
                    {

                        f.setCurrentDirectory (new File (sfv));

                    }

                    f.setFileFilter (new FileNameExtensionFilter ("Supported Files (wav)",
                                                                  "wav"));

                    if (f.showOpenDialog (_this) == JFileChooser.APPROVE_OPTION)
                    {

                        _this.playSoundFile.setText (f.getSelectedFile ().getPath ());

                    }

                }

            });

        this.playSound.setSelected (playSoundEnabled);

        its.add ("Use the button below to play/test the sound, if you select a file above then that will be played instead of the default.");

        JButton testB = new JButton ("Test Sound");

        testB.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    String sf = _this.playSoundFile.getText ();

                    try
                    {

                        InputStream is = null;

                        if ((sf != null) &&
                            (sf.trim ().length () > 0))
                        {

                            File f = new File (sf);

                            if ((f.exists ()) &&
                                (f.getName ().toLowerCase ().endsWith (".wav")))
                            {

                                is = new BufferedInputStream (new FileInputStream (f));

                            } else
                            {

                                UIUtils.showErrorMessage (_this,
                                                          sf + " is not a valid .wav file.");

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
                                              sf,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to play sound file.");

                    }

                }

            });

        its.add (testB);

        this.addSection (projOptsBox,
                         "Sounds",
                         null,
                         its);

        JComponent cc = (JComponent) Box.createVerticalGlue ();

        projOptsBox.add (cc);

        return projOptsBox;

    }

    private void addSection (Box            box,
                             String         title,
                             String         desc,
                             java.util.List items)
    {

        Border b = new EmptyBorder (5,
                                    5,
                                    5,
                                    5);

        Border ib = new EmptyBorder (0,
                                     10,
                                     5,
                                     5);

        Header ti = UIUtils.createBoldSubHeader (title,
                                                 null);
        ti.setBackground (Color.white);
        ti.setBorder (new CompoundBorder (b,
                                          ti.getBorder ()));

        box.add (ti);

        if (desc != null)
        {

            box.add (this.createHelpText (desc));

        }

        for (int i = 0; i < items.size (); i++)
        {

            Object o = items.get (i);

            if (o instanceof String)
            {

                box.add (this.createHelpText ((String) o));

                continue;

            }

            if (o instanceof JComboBox)
            {

                JComboBox c = (JComboBox) o;

                c.setMaximumSize (c.getPreferredSize ());

            }

            if (o instanceof JComponent)
            {

                JComponent c = (JComponent) o;

                c.setAlignmentX (Component.LEFT_ALIGNMENT);
                c.setAlignmentY (Component.TOP_ALIGNMENT);

                Box _b = null;

                if (!(o instanceof Box))
                {

                    _b = new Box (BoxLayout.X_AXIS);
                    _b.add (Box.createHorizontalStrut (5));
                    _b.add ((Component) o);
                    _b.add (Box.createHorizontalGlue ());
                    _b.setAlignmentX (Component.LEFT_ALIGNMENT);
                    _b.setAlignmentY (Component.TOP_ALIGNMENT);

                } else
                {

                    _b = (Box) o;

                }

                box.add (_b);

                _b.setBorder (ib);

            }

        }

        box.add (Box.createVerticalStrut (5));

    }

    private JTextPane createHelpText (String text)
    {

        JTextPane t = UIUtils.createHelpTextPane (text);
        t.setBorder (new EmptyBorder (0,
                                      10,
                                      5,
                                      5));

        return t;

    }
/*
    public static JTextPane createHelpTextPane (String    text)

        JTextArea t = new JTextArea ();
        t.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        t.setText (text);
        t.setEditable (false);
        t.setOpaque (false);
        t.setLineWrap (true);
        t.setWrapStyleWord (true);
        t.setBorder (new EmptyBorder (0, 10, 5, 5));

        t.setSize (400,
                   Short.MAX_VALUE);

        t.setAlignmentY (Component.TOP_ALIGNMENT);

        return t;

    }
*/
}
