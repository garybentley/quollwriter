package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.event.*;

import java.io.*;
import java.net.*;
import java.beans.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import javax.sound.sampled.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.filechooser.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ItemAdapter;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.Accordion;
import com.quollwriter.achievements.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.text.*;

import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.ui.*;

public class Options extends Box
{

    public enum Section
    {

        warmups ("warmups"),
        look ("look"),
        naming ("naming"),
        editing ("editing"),
        editors ("editors"),
        itemsAndRules ("itemsAndRules"),
        achievements ("achievements"),
        problems ("problems"),
        betas ("betas"),
        start ("start"),
        landing ("landing"),
        project ("project");

        private String type = null;

        Section (String type)
        {

            this.type = type;

        }

        public String getType ()
        {

            return this.type;

        }

    }

    private Header header = null;
    private AbstractViewer viewer = null;
    private Accordion accordion = null;
    private JScrollPane scrollPane = null;
    private JCheckBox sendErrorsToSupport = null;

    private Map<Section, Accordion.Item> sections = new HashMap ();

    private Set<Section> sectIds = null;

    public Options (AbstractViewer viewer,
                    Section...     sectIds)
             throws GeneralException
    {

        super (BoxLayout.Y_AXIS);

        this.viewer = viewer;

        this.sectIds = new LinkedHashSet (new ArrayList ((List<Section>) Arrays.asList (sectIds)));

        this.header = UIUtils.createHeader ("Options",
                                            Constants.PANEL_TITLE,
                                            Constants.OPTIONS_ICON_NAME,
                                             null);

    }

    public class SectionInfo
    {

        public String title = null;
        public String help = null;
        public JComponent content = null;
        public String iconType = null;

        public SectionInfo (String     title,
                            String     iconType,
                            String     help,
                            JComponent content)
        {

            this.title = title;
            this.iconType = iconType;
            this.help = help;
            this.content = content;

        }

    }

    public Header getHeader ()
    {

        return this.header;

    }

    public void init ()
    {

        this.add (this.header);

        Box b = new Box (BoxLayout.Y_AXIS);

        final Options _this = this;

        this.accordion = new Accordion (BoxLayout.Y_AXIS);

        this.accordion.setBorder (new EmptyBorder (0,
                                                   10,
                                                   10,
                                                   10));

        this.scrollPane = new JScrollPane (this.accordion);
        this.scrollPane.setBorder (new EmptyBorder (1, 0, 0, 0));
        this.scrollPane.setOpaque (false);
        this.scrollPane.getViewport ().setBorder (null);
        this.scrollPane.getViewport ().setOpaque (false);
        this.scrollPane.getVerticalScrollBar ().setUnitIncrement (50);
        this.scrollPane.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.scrollPane.getVerticalScrollBar ().addAdjustmentListener (new AdjustmentListener ()
        {

            public void adjustmentValueChanged (AdjustmentEvent ev)
            {

                if (_this.scrollPane.getVerticalScrollBar ().getValue () > 0)
                {

                    _this.scrollPane.setBorder (new MatteBorder (1, 0, 0, 0,
                                                                 UIUtils.getInnerBorderColor ()));

                } else {

                    _this.scrollPane.setBorder (new EmptyBorder (1, 0, 0, 0));

                }

            }

        });

        this.add (this.scrollPane);

        this.accordion.setOpaque (false);

        for (Section sect : this.sectIds)
        {

            SectionInfo inf = this.getSectionInfo (sect);

            if (inf == null)
            {

                continue;

            }

            this.setContentBorder (inf.content);

            Accordion.Item item = this.accordion.add (this.createHeader (UIUtils.formatForUser (inf.title),
                                                                         inf.iconType),
                                                      null,
                                                      inf.content,
                                                      UIUtils.createHelpTextPane (inf.help,
                                                                                  this.viewer));

            this.sections.put (sect,
                               item);

        }
        /*
        this.addProjectAndSnapshotsSection ();

        this.addHowThingsLookSection ();

        this.addWhatThingsAreCalledSection ();

        this.addEditingChaptersSection ();

        this.addEditorsSection ();

        this.addItemsAndRulesSection ();

        this.addWarmupsSection ();

        this.addAchievementsSection ();

        this.addProblemsSection ();

        this.addBetasSection ();
*/
        this.accordion.add (Box.createVerticalGlue ());

        this.accordion.setAllSectionsOpen (false);

        SwingUtilities.invokeLater (new Runnable ()
        {

            public void run ()
            {

                _this.scrollPane.getVerticalScrollBar ().setValue (0);

            }

        });

    }

    public void showSection (String name)
    {

        if (name == null)
        {

            return;

        }

        this.showSection (Section.valueOf (name));

    }

    public void showSection (Section name)
    {

        final Options _this = this;
        final Accordion.Item item = this.sections.get (name);

        if (item != null)
        {

            item.setOpenContentVisible (true);

            this.validate ();
            this.repaint ();

            final Border origBorder = item.getBorder ();

            final Color col = UIUtils.getBorderHighlightColor ();

            final int r = col.getRed ();
            final int g = col.getGreen ();
            final int b = col.getBlue ();

            PropertyChangeListener l = new PropertyChangeListener ()
            {

                @Override
                public void propertyChange (PropertyChangeEvent ev)
                {

                    Color c = new Color (r,
                                         g,
                                         b,
                                        ((Number) ev.getNewValue ()).intValue ());

                    item.setBorder (new CompoundBorder (new MatteBorder (3, 3, 3, 3, c),
                                                        UIUtils.createPadding (3, 3, 3, 3)));

                }

            };

            final Timer cycle = UIUtils.createCyclicAnimator (l,
                                                        l,
                                                        60,
                                                        1500,
                                                        0,
                                                        255,
                                                        2,
                                                        new ActionListener ()
                                                        {

                                                           @Override
                                                           public void actionPerformed (ActionEvent ev)
                                                           {

                                                               item.setBorder (origBorder);

                                                           }

                                                        });

            UIUtils.doLater (new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    _this.scrollPane.scrollRectToVisible (SwingUtilities.convertRectangle (item,
                                                                                           item.getBounds (),
                                                                                           _this.scrollPane));

                    cycle.start ();

                }

            });

        }

    }

    public SectionInfo getSectionInfo (Section sect)
    {

        if (sect == Section.start)
        {

            return this.createStartSection ();

        }

        if (sect == Section.warmups)
        {

            return this.createWarmupsSection ();

        }

        if (sect == Section.project)
        {

            return this.createProjectSection ();

        }

        if (sect == Section.look)
        {

            return this.createLookSection ();

        }

        if (sect == Section.naming)
        {

            return this.createNamingSection ();

        }

        if (sect == Section.editing)
        {

            return this.createEditingSection ();

        }

        if (sect == Section.editors)
        {

            return this.createEditorsSection ();

        }

        if (sect == Section.itemsAndRules)
        {

            return this.createItemsAndRulesSection ();

        }

        if (sect == Section.achievements)
        {

            return this.createAchievementsSection ();

        }

        if (sect == Section.problems)
        {

            return this.createProblemsSection ();

        }

        if (sect == Section.betas)
        {

            return this.createBetasSection ();

        }

        return null;

    }

    private SectionInfo createStartSection ()
    {

        final Options _this = this;

        Box box = new Box (BoxLayout.Y_AXIS);

        final JCheckBox showTips = new JCheckBox (Environment.replaceObjectNames ("Show useful tips"));
        showTips.setOpaque (false);

		final JCheckBox lastCB = new JCheckBox (Environment.replaceObjectNames ("Open the last edited {project}"));
		lastCB.setOpaque (false);

		final JCheckBox showCB = new JCheckBox (Environment.replaceObjectNames ("Show the {Projects} window"));
		showCB.setOpaque (false);

        showTips.setSelected (UserProperties.getAsBoolean (Constants.SHOW_TIPS_PROPERTY_NAME));
		lastCB.setSelected (UserProperties.getAsBoolean (Constants.OPEN_LAST_EDITED_PROJECT_PROPERTY_NAME));
		showCB.setSelected (UserProperties.getAsBoolean (Constants.SHOW_LANDING_ON_START_PROPERY_NAME));

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

        lastCB.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

				if (!lastCB.isSelected ())
				{

					showCB.setSelected (true);

                    UserProperties.set (Constants.SHOW_LANDING_ON_START_PROPERY_NAME,
										showCB.isSelected ());

				}

				UserProperties.set (Constants.OPEN_LAST_EDITED_PROJECT_PROPERTY_NAME,
									lastCB.isSelected ());

            }

        });

        c = this.createWrapper (lastCB);

        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        showCB.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

				if (!showCB.isSelected ())
				{

					lastCB.setSelected (true);

                    UserProperties.set (Constants.OPEN_LAST_EDITED_PROJECT_PROPERTY_NAME,
                                        lastCB.isSelected ());

				}

                UserProperties.set (Constants.SHOW_LANDING_ON_START_PROPERY_NAME,
                                    showCB.isSelected ());

            }

        });

        c = this.createWrapper (showCB);

        this.setAsMainItem (c);

        box.add (c);

        return new SectionInfo ("When {QW} starts",
                                "start",
                                "Want to see some tips when {QW} starts?  Or maybe open the {project} you last edited?  Look no further!",
                                box);

    }

    private SectionInfo createBetasSection ()
    {

        final Options _this = this;

        //box.add (this.createHeader (UIUtils.formatForUser ("When something goes wrong")));
        //box.add (Box.createVerticalStrut (5));

        Box box = new Box (BoxLayout.Y_AXIS);

        final JCheckBox optinToBetas = UIUtils.createCheckBox ("Opt-in to beta versions (enables auto-send of errors to Quoll Writer support)");
        optinToBetas.setSelected (UserProperties.getAsBoolean (Constants.OPTIN_TO_BETA_VERSIONS_PROPERTY_NAME));

        optinToBetas.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (optinToBetas.isSelected ())
                {

                    UIUtils.showMessage ((PopupsSupported) _this.viewer,
                                         "About Betas",
                                         "Quoll Writer betas are opt-in and are designed to elicit feedback from users.  They will be functionally complete and as bug free as possible.  However they won't be without issues, so please beware and report any problems you find.<br /><br />You can opt out at any time although this will not revert back to a previous version (that is potentially dangerous).<br /><br />Note: opting into betas will enable the <b>Send errors to Quoll Writer support</b> option, you can switch this off if you like to prevent errors being sent.");

                    _this.sendErrorsToSupport.setSelected (true);
                    _this.updateUserProperty (Constants.AUTO_SEND_ERRORS_TO_SUPPORT_PROPERTY_NAME,
                                              true);


                }

                _this.updateUserProperty (Constants.OPTIN_TO_BETA_VERSIONS_PROPERTY_NAME,
                                          optinToBetas.isSelected ());

            }

        });

        JComponent c = this.createWrapper (optinToBetas);

        this.setAsMainItem (c);

        box.add (c);

        return new SectionInfo ("Beta versions",
                                Constants.ABOUT_ICON_NAME,
                                "Want to get ahead of the crowd?  Or maybe help improve Quoll Writer?  This section lets you opt-in to beta versions.  But be warned, betas aren't perfect.",
                                box);

    }

    private SectionInfo createProblemsSection ()
    {

        final Options _this = this;

        //box.add (this.createHeader (UIUtils.formatForUser ("When something goes wrong")));
        //box.add (Box.createVerticalStrut (5));

        Box box = new Box (BoxLayout.Y_AXIS);

        this.sendErrorsToSupport = UIUtils.createCheckBox ("Send errors to Quoll Writer support");
        this.sendErrorsToSupport.setSelected (UserProperties.getAsBoolean (Constants.AUTO_SEND_ERRORS_TO_SUPPORT_PROPERTY_NAME));

        this.sendErrorsToSupport.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.updateUserProperty (Constants.AUTO_SEND_ERRORS_TO_SUPPORT_PROPERTY_NAME,
                                          sendErrorsToSupport.isSelected ());

            }

        });

        JComponent c = this.createWrapper (this.sendErrorsToSupport);

        this.setAsMainItem (c);

        box.add (c);

        return new SectionInfo ("When something goes wrong",
                                Constants.BUG_ICON_NAME,
                                "Quoll Writer isn't perfect and it would be good to know when things cluck up.  If you open this section you'll find a single setting that will let you send errors back to the magical worker elfs at Quoll Writer Headquarters located in deepest, darkest suburban Australia.  Did you know that every error sent will prevent a Drop Bear attack.  It's a serious and very real threat to our native elves.",
                                box);

    }

    private SectionInfo createAchievementsSection ()
    {

        Box box = new Box (BoxLayout.Y_AXIS);

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

        return new SectionInfo ("Achievements",
                                Constants.ACHIEVEMENT_ICON_NAME,
                                "Are the achievements annoying you?  Use this section to switch them off and they will bug you no more you underachiever.",
                                box);

    }

    private SectionInfo createItemsAndRulesSection ()
    {

        final Options _this = this;

        //box.add (this.createHeader ("Manage Items & Rules"));
        //box.add (Box.createVerticalStrut (10));

        Box box = new Box (BoxLayout.Y_AXIS);

        // Gaaah....
        boolean isAPV = (this.viewer instanceof AbstractProjectViewer);

        JButton[] buts = new JButton[ (isAPV ? 3 : 2) ];

        JButton b = new JButton (Environment.replaceObjectNames ("{Note} Types"));

        b.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.viewer.showEditNoteTypes ();

            }

        });

        buts[0] = b;

        b = new JButton ("Item Types");

        b.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.viewer.showEditItemTypes ();

            }

        });

        buts[1] = b;

        if (isAPV)
        {

            b = new JButton ("Problem Finder Rules");

            b.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    ((ProjectViewer) _this.viewer).showProblemFinderRuleConfig ();

                }

            });

            buts[2] = b;

        }

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.LEFT_ALIGNMENT);//ButtonBarFactory.buildLeftAlignedBar (buts);
        bp.setOpaque (false);

        JComponent c = this.createWrapper (bp);

        this.setAsMainItem (c);

        box.add (c);

        return new SectionInfo ("Manage Items & Rules",
                                Constants.EDIT_ICON_NAME,
                                "Set up the problem finder rules and manage the note and item types.  A dull description but it does exactly what it says on the tin, well screen.",
                                box);

    }

    private SectionInfo createEditorsSection ()
    {

        if (EditorsEnvironment.getUserAccount () == null)
        {

            return null;

        }

        final Options _this = this;

        Box box = new Box (BoxLayout.Y_AXIS);

        final JCheckBox autoLogin = UIUtils.createCheckBox ("Automatically login/go online whenever Quoll Writer starts");

        autoLogin.setSelected (EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME));

        autoLogin.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                try
                {

                    EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME,
                                                           autoLogin.isSelected ());

                } catch (Exception e) {

                    Environment.logError ("Unable to set to login at start",
                                          e);

                }

            }

        });

        JComponent c = this.createWrapper (autoLogin);
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        c = this.createHelpText ("My default status when I go online is");
        this.setAsMainItem (c);

        box.add (c);

        Vector statuses = new Vector ();

        statuses.add (EditorEditor.OnlineStatus.online);
        statuses.add (EditorEditor.OnlineStatus.busy);
        statuses.add (EditorEditor.OnlineStatus.away);
        statuses.add (EditorEditor.OnlineStatus.snooze);

        final JComboBox defStatus = new JComboBox (statuses);

        String defOnlineStatus = EditorsEnvironment.getEditorsProperty (Constants.QW_EDITORS_SERVICE_DEFAULT_ONLINE_STATUS_PROPERTY_NAME);

        if (defOnlineStatus != null)
        {

            defStatus.setSelectedItem (EditorEditor.OnlineStatus.valueOf (defOnlineStatus));

        }

        defStatus.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                if (ev.getStateChange () != ItemEvent.SELECTED)
                {

                    return;

                }

                try
                {

                    EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_DEFAULT_ONLINE_STATUS_PROPERTY_NAME,
                                                           ((EditorEditor.OnlineStatus) defStatus.getSelectedItem ()).getType ());

                } catch (Exception e) {

                    Environment.logError ("Unable to set default online status",
                                          e);

                }

            }

        });

        defStatus.setRenderer (new DefaultListCellRenderer ()
        {

            public Component getListCellRendererComponent (JList   list,
                                                           Object  value,
                                                           int     index,
                                                           boolean isSelected,
                                                           boolean cellHasFocus)
            {

                JLabel l = (JLabel) super.getListCellRendererComponent (list,
                                                                        value,
                                                                        index,
                                                                        isSelected,
                                                                        cellHasFocus);

                EditorEditor.OnlineStatus status = (EditorEditor.OnlineStatus) value;

                String iconName = status.getType ();

                l.setText (status.getName ());
                l.setBorder (UIUtils.createPadding (3, 3, 3, 3));
                l.setIcon (Environment.getIcon (Constants.ONLINE_STATUS_ICON_NAME_PREFIX + iconName,
                                                Constants.ICON_POPUP));

                return l;

            }

        });

        c = this.createWrapper (defStatus);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        final JCheckBox fullScreen = UIUtils.createCheckBox ("Set my status to <b>Busy</b> when I enter full screen mode");

        fullScreen.setSelected (EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.QW_EDITORS_SERVICE_SET_BUSY_ON_FULL_SCREEN_ENTERED_PROPERTY_NAME));

        fullScreen.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                try
                {

                    EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_SET_BUSY_ON_FULL_SCREEN_ENTERED_PROPERTY_NAME,
                                                           fullScreen.isSelected ());

                    if (fullScreen.isSelected ())
                    {

                        if (Environment.isInFullScreen ())
                        {

                            EditorsEnvironment.fullScreenEntered ();

                        }

                    }

                } catch (Exception e) {

                    Environment.logError ("Unable to set to busy on full screen entered",
                                          e);

                }

            }

        });

        c = this.createWrapper (fullScreen);
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));
        /*
        final JCheckBox showPopups = UIUtils.createCheckBox ("Display a popup when I receive a new message from {an editor} (popups are never shown in full screen mode)");

        showPopups.setSelected (EditorsEnvironment.isShowPopupWhenNewMessageReceived ());

        c = this.createWrapper (showPopups);
        this.setAsMainItem (c);

        box.add (c);

        final JComponent label = UIUtils.createClickableLabel ("View an example",
                                                               null);

        label.addMouseListener (new MouseAdapter ()
        {

            public void mousePressed (MouseEvent ev)
            {

                QPopup popup = _this.projectViewer.getPopupByName ("editors-popup-message-example-popup");

                if (popup == null)
                {

                    popup = UIUtils.createClosablePopup ("Example",
                                                         null,
                                                         null);

                    popup.setName ("editors-popup-message-example-popup");
                    ImagePanel ip = new ImagePanel (Environment.getImage (Constants.QW_EDITORS_MESSAGE_POPUP_TEST_IMAGE),
                                                    null);

                    popup.setContent (ip);

                }

                _this.projectViewer.showPopupAt (popup,
                                                 label,
                                                 true);

            }

        });

        c = this.createWrapper (label);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));
        */
        final JCheckBox logMessages = UIUtils.createCheckBox ("Log messages I send/receive (debug only)");

        logMessages.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                EditorsEnvironment.logEditorMessages (logMessages.isSelected ());

            }

        });

        c = this.createWrapper (logMessages);
        this.setAsMainItem (c);

        box.add (c);

        c = this.createHelpText ("Save messages sent/received to a log file.  You should only check this box if Quoll Writer support asks you to.  Note: this value is not saved and must be activated everytime you run Quoll Writer.");
        this.setAsSubItem (c);

        box.add (c);

        return new SectionInfo ("{Editors} Service",
                                Constants.EDITORS_ICON_NAME,
                                "Options related to the Editors service and how you interact with your {contacts}.",
                                box);

    }

    private SectionInfo createEditingSection ()
    {

        final Options _this = this;

        //final AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

        //box.add (this.createHeader (UIUtils.formatForUser ("Editing {Chapters}")));
        //box.add (Box.createVerticalStrut (5));

        Box box = new Box (BoxLayout.Y_AXIS);

        Vector chapterA = new Vector ();
        chapterA.add (Constants.MINS_5);
        chapterA.add (Constants.MINS_10);
        chapterA.add (Constants.MINS_20);
        chapterA.add (Constants.MINS_30);
        chapterA.add (Constants.HOUR_1);

        final JComboBox autosaveAmount = new JComboBox (chapterA);

        final JCheckBox enableAutosave = new JCheckBox (Environment.replaceObjectNames ("Enable {Chapter} Auto-save"));
        enableAutosave.setOpaque (false);
        enableAutosave.setAlignmentX (Component.LEFT_ALIGNMENT);

        JComponent c = this.createWrapper (enableAutosave);
        this.setAsMainItem (c);

        box.add (c);

        final Properties props = Environment.getDefaultProperties (Project.OBJECT_TYPE);

        boolean autosaveEnabled = props.getPropertyAsBoolean (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME);
        enableAutosave.setSelected (autosaveEnabled);

        autosaveAmount.setSelectedItem (props.getProperty (Constants.CHAPTER_AUTO_SAVE_INTERVAL_PROPERTY_NAME));
        autosaveAmount.setEnabled (enableAutosave.isSelected ());

        enableAutosave.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                autosaveAmount.setEnabled (enableAutosave.isSelected ());

                props.setProperty (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME,
                                   new BooleanProperty (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME,
                                                        enableAutosave.isSelected ()));

                // For all the ProjectViewers (where text is editable) update the auto save settings.
                Environment.doForOpenProjects (Project.NORMAL_PROJECT_TYPE,
                                               new ProjectViewerAction<ProjectViewer> ()
                                               {

                                                    public void doAction (ProjectViewer pv)
                                                    {

                                                        pv.scheduleAutoSaveForAllEditors ();

                                                    }

                                               });

                _this.updateDefaultProjectProperty (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME,
                                                    enableAutosave.isSelected ());

                Environment.fireUserProjectEvent (_this,
                                                  ProjectEvent.AUTO_SAVE,
                                                  (enableAutosave.isSelected () ? ProjectEvent.ON : ProjectEvent.OFF));

            }

        });

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

                // For all the ProjectViewers (where text is editable) update the auto save settings.
                Environment.doForOpenProjects (Project.NORMAL_PROJECT_TYPE,
                                               new ProjectViewerAction<ProjectViewer> ()
                                               {

                                                    public void doAction (ProjectViewer pv)
                                                    {

                                                        pv.scheduleAutoSaveForAllEditors ();

                                                    }

                                               });

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

        c = this.createHelpText ("Show an icon against a {chapter} in the {chapter} list when");
        this.setAsMainItem (c);

        box.add (c);

        final JCheckBox showEditPos = new JCheckBox (Environment.replaceObjectNames ("it has an edit position"));
        showEditPos.setSelected (UserProperties.getAsBoolean (Constants.SHOW_EDIT_POSITION_ICON_IN_CHAPTER_LIST_PROPERTY_NAME));
        showEditPos.setOpaque (false);
        showEditPos.setAlignmentX (Component.LEFT_ALIGNMENT);

        showEditPos.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                Environment.doForOpenProjects (Project.NORMAL_PROJECT_TYPE,
                                               new ProjectViewerAction<ProjectViewer> ()
                                               {

                                                    public void doAction (ProjectViewer pv)
                                                    {

                                                        pv.reloadChapterTree ();

                                                    }

                                               });

                _this.updateUserProperty (Constants.SHOW_EDIT_POSITION_ICON_IN_CHAPTER_LIST_PROPERTY_NAME,
                                          showEditPos.isSelected ());

            }

        });

        c = this.createWrapper (showEditPos);
        this.setAsSubItem (c);

        box.add (c);

        final JCheckBox showEdited = new JCheckBox (Environment.replaceObjectNames ("it is set as edit complete"));
        showEdited.setSelected (UserProperties.getAsBoolean (Constants.SHOW_EDIT_COMPLETE_ICON_IN_CHAPTER_LIST_PROPERTY_NAME));
        showEdited.setOpaque (false);
        showEdited.setAlignmentX (Component.LEFT_ALIGNMENT);

        showEdited.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                Environment.doForOpenProjects (Project.NORMAL_PROJECT_TYPE,
                                               new ProjectViewerAction<ProjectViewer> ()
                                               {

                                                    public void doAction (ProjectViewer pv)
                                                    {

                                                        pv.reloadChapterTree ();

                                                    }

                                               });

                _this.updateUserProperty (Constants.SHOW_EDIT_COMPLETE_ICON_IN_CHAPTER_LIST_PROPERTY_NAME,
                                          showEdited.isSelected ());

            }

        });

        c = this.createWrapper (showEdited);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (5));

        final JComponent label = UIUtils.createClickableLabel ("View an example",
                                                               null);

        label.addMouseListener (new MouseEventHandler ()
        {

            public void handlePress (MouseEvent ev)
            {

                QPopup popup = _this.viewer.getPopupByName ("edit-complete-example-popup");

                if (popup == null)
                {

                    // Create a fake chapter tree.
                    JTree tree = UIUtils.createTree ();

                    tree.setCellRenderer (new ProjectTreeCellRenderer (false)
                    {

                        public boolean showEditPositionIcon ()
                        {

                            return true;

                        }

                        public boolean showEditCompleteIcon ()
                        {

                            return true;

                        }

                    });

                    Book testBook = null;

                    try
                    {

                        testBook = Environment.createTestBook ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to create test book",
                                              e);

                        return;

                    }

                    for (Chapter c : testBook.getChapters ())
                    {

                        if (testBook.getChapterIndex (c) % 2 == 0)
                        {

                            c.setEditComplete (true);

                        } else {

                            c.setEditPosition (1);

                        }

                    }

                    tree.setModel (new DefaultTreeModel (UIUtils.createTree (testBook,
                                                                             null,
                                                                             null,
                                                                             false)));

                    popup = UIUtils.createClosablePopup ("Example",
                                                         null,
                                                         null);

                    popup.setName ("edit-complete-example-popup");
                    tree.setBorder (new EmptyBorder (10, 10, 10, 10));

                    popup.setContent (tree);

                    popup.setPreferredSize (new Dimension (250,
                                                           popup.getPreferredSize ().height));

                }

                _this.viewer.showPopupAt (popup,
                                          label,
                                          true);

            }

        });

        c = this.createWrapper (label);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        final JCheckBox showEditMarker = new JCheckBox (Environment.replaceObjectNames ("Show the edit position in a {chapter}"));
        showEditMarker.setSelected (UserProperties.getAsBoolean (Constants.SHOW_EDIT_MARKER_IN_CHAPTER_PROPERTY_NAME));
        showEditMarker.setOpaque (false);
        showEditMarker.setAlignmentX (Component.LEFT_ALIGNMENT);

        showEditMarker.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                _this.updateUserProperty (Constants.SHOW_EDIT_MARKER_IN_CHAPTER_PROPERTY_NAME,
                                          showEditMarker.isSelected ());

                Environment.doForOpenProjectViewers (AbstractProjectViewer.class,
                                                     new ProjectViewerAction<AbstractProjectViewer> ()
                                                     {

                                                        public void doAction (AbstractProjectViewer pv)
                                                        {

                                                            pv.doForPanels (QuollEditorPanel.class,
                                                                            new DefaultQuollPanelAction ()
                                                                            {

                                                                                public void doAction (QuollPanel qp)
                                                                                {

                                                                                try
                                                                                {

                                                                                    ((QuollEditorPanel) qp).reinitIconColumn ();

                                                                                } catch (Exception e) {

                                                                                    Environment.logError ("Unable to reinit icon column for panel",
                                                                                                          e);

                                                                                }

                                                                            }

                                                                         });

                                                        }

                                                     });

            }

        });

        c = this.createWrapper (showEditMarker);
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (5));

        c = this.createHelpText ("Use the following color for the edit position indicator");
        this.setAsSubItem (c);

        box.add (c);

        final Color col = UIUtils.getColor (UserProperties.get (Constants.EDIT_MARKER_COLOR_PROPERTY_NAME));

        final JPanel cSwatch = QColorChooser.getSwatch (col);

        UIUtils.setAsButton (cSwatch);

        cSwatch.addMouseListener (new MouseAdapter ()
        {

            public void mouseReleased (MouseEvent ev)
            {

                QPopup popup = QColorChooser.getColorChooserPopup ("Select the edit indicator color",
                                                                   col,
                                                                   new ChangeAdapter ()
                                                                   {

                                                                     public void stateChanged (ChangeEvent ev)
                                                                     {

                                                                         Color c = (Color) ev.getSource ();

                                                                        _this.updateUserProperty (Constants.EDIT_MARKER_COLOR_PROPERTY_NAME,
                                                                                                  UIUtils.colorToHex (c));

                                                                        Environment.doForOpenProjectViewers (AbstractProjectViewer.class,
                                                                                                             new ProjectViewerAction<AbstractProjectViewer> ()
                                                                                                             {

                                                                                                                public void doAction (AbstractProjectViewer pv)
                                                                                                                {

                                                                                                                    pv.doForPanels (QuollEditorPanel.class,
                                                                                                                                    new DefaultQuollPanelAction ()
                                                                                                                                    {

                                                                                                                                       public void doAction (QuollPanel qp)
                                                                                                                                       {

                                                                                                                                           try
                                                                                                                                           {

                                                                                                                                               ((QuollEditorPanel) qp).reinitIconColumn ();

                                                                                                                                           } catch (Exception e) {

                                                                                                                                               Environment.logError ("Unable to reinit icon column for panel",
                                                                                                                                                                     e);

                                                                                                                                           }

                                                                                                                                       }

                                                                                                                                    });

                                                                                                                }

                                                                                                             });

                                                                        cSwatch.setBackground (c);

                                                                     }

                                                                   },
                                                                   new ActionAdapter ()
                                                                   {

                                                                        public void actionPerformed (ActionEvent ev)
                                                                        {
/*
                                                                            QPopup p = _this.popups.remove ("textcolor");

                                                                            p.removeFromParent ();
  */
                                                                        }

                                                                   });

                popup.setDraggable (_this.viewer);

                _this.viewer.showPopupAt (popup,
                                          cSwatch,
                                          true);

            }

        });

        c = this.createWrapper (cSwatch);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (5));

        final JComponent label2 = UIUtils.createClickableLabel ("View an example",
                                                                null);

        label2.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handlePress (MouseEvent ev)
            {

                QPopup popup = _this.viewer.getPopupByName ("edit-position-example-popup");

                if (popup == null)
                {

                    popup = UIUtils.createClosablePopup ("Example",
                                                         null,
                                                         null);

                    popup.setName ("edit-position-example-popup");
                    ImagePanel ip = new ImagePanel (Environment.getImage (Constants.EDIT_POSITION_TEST_IMAGE),
                                                    null);

                    popup.setContent (ip);

                }

                _this.viewer.showPopupAt (popup,
                                          label2,
                                          true);

            }

        });

        c = this.createWrapper (label2);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        final JCheckBox markEdited = new JCheckBox (Environment.replaceObjectNames ("Set a {chapter} as edit complete when the edit position is at the end of the {chapter}"));
        markEdited.setOpaque (false);
        markEdited.setSelected (UserProperties.getAsBoolean (Constants.SET_CHAPTER_AS_EDIT_COMPLETE_WHEN_EDIT_POSITION_IS_AT_END_OF_CHAPTER_PROPERTY_NAME));
        markEdited.setAlignmentX (Component.LEFT_ALIGNMENT);

        markEdited.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                _this.updateUserProperty (Constants.SET_CHAPTER_AS_EDIT_COMPLETE_WHEN_EDIT_POSITION_IS_AT_END_OF_CHAPTER_PROPERTY_NAME,
                                          markEdited.isSelected ());

                Environment.doForOpenProjectViewers (AbstractProjectViewer.class,
                                                     new ProjectViewerAction<AbstractProjectViewer> ()
                                                     {

                                                        public void doAction (final AbstractProjectViewer pv)
                                                        {

                                                            pv.doForPanels (AbstractEditorPanel.class,
                                                                            new DefaultQuollPanelAction ()
                                                                            {

                                                                               public void doAction (QuollPanel qp)
                                                                               {

                                                                                   AbstractEditorPanel p = (AbstractEditorPanel) qp;

                                                                                   if (p == null)
                                                                                   {

                                                                                       return;

                                                                                   }

                                                                                   try
                                                                                   {

                                                                                       Chapter c = p.getChapter ();

                                                                                       if (c.getEditPosition () > 0)
                                                                                       {

                                                                                           pv.setChapterEditPosition (c,
                                                                                                                      c.getEditPosition ());

                                                                                       }

                                                                                   } catch (Exception e) {

                                                                                       Environment.logError ("Unable to set edit position for chapter: " +
                                                                                                             p.getChapter (),
                                                                                                             e);

                                                                                   }

                                                                               }

                                                                            });

                                                        }

                                                     });

            }

        });

        c = this.createWrapper (markEdited);
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        final JCheckBox compressMenu = new JCheckBox (Environment.replaceObjectNames ("Compress the {chapter} right click menu"));
        compressMenu.setOpaque (false);
        compressMenu.setSelected (UserProperties.getAsBoolean (Constants.COMPRESS_CHAPTER_CONTEXT_MENU_PROPERTY_NAME));
        compressMenu.setAlignmentX (Component.LEFT_ALIGNMENT);

        compressMenu.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                _this.updateUserProperty (Constants.COMPRESS_CHAPTER_CONTEXT_MENU_PROPERTY_NAME,
                                          compressMenu.isSelected ());

            }

        });

        c = this.createWrapper (compressMenu);
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (5));

        final JLabel label3 = UIUtils.createClickableLabel ("View an example",
                                                            null);

        label3.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handlePress (MouseEvent ev)
            {

                String img = Constants.COMPRESSED_CHAPTER_CONTEXT_MENU_IMAGE;
                String name = "compress-chapter-menu-example-popup";

                if (!compressMenu.isSelected ())
                {

                    img = Constants.NONE_COMPRESSED_CHAPTER_CONTEXT_MENU_IMAGE;

                    name = "no-compress-chapter-menu-example-popup";

                }

                QPopup popup = _this.viewer.getPopupByName (name);

                if (popup == null)
                {

                    popup = UIUtils.createClosablePopup ("Example",
                                                         null,
                                                         null);

                    ImagePanel ip = new ImagePanel (Environment.getImage (img),
                                                    null);

                    popup.setName (name);
                    popup.setContent (ip);

                    popup.setDraggable (_this.viewer);

                }

                _this.viewer.showPopupAt (popup,
                                          label3,
                                          true);

            }

        });

        c = this.createWrapper (label3);

        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        c = this.createHelpText ("Use the following language for the spellchecker");
        this.setAsMainItem (c);

        box.add (c);

        final JComponent downloadFiles = UIUtils.createClickableLabel ("Download the language files",
                                                                       null);

        if (this.viewer instanceof AbstractProjectViewer)
        {

            AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

            try
            {

                downloadFiles.setVisible (!DictionaryProvider.isLanguageInstalled (pv.getSpellCheckLanguage ()));

            } catch (Exception e) {

                // Ignore.

            }

        } else {

            downloadFiles.setVisible (!DictionaryProvider.isLanguageInstalled (UserProperties.get (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME)));

        }

        final JCheckBox defLang = new JCheckBox (Environment.replaceObjectNames ("Set as default language"));

        final JComboBox spellcheckLang = new JComboBox ();

        // Get the languages supported by the spellchecker.
        new Thread (new Runnable ()
        {

            public void run ()
            {

                String l = null;

                try
                {

                    l = Environment.getUrlFileAsString (new URL (Environment.getQuollWriterWebsite () + "/" + Environment.getProperty (Constants.QUOLL_WRITER_SUPPORTED_LANGUAGES_URL_PROPERTY_NAME)));

                } catch (Exception e) {

                    // Something gone wrong, so just add english.
                    l = Constants.ENGLISH;

                    Environment.logError ("Unable to get language files url",
                                          e);

                }

                StringTokenizer t = new StringTokenizer (l,
                                                         String.valueOf ('\n'));

                final Vector langs = new Vector ();

                while (t.hasMoreTokens ())
                {

                    String lang = t.nextToken ().trim ();

                    if (lang.equals (""))
                    {

                        continue;

                    }

                    langs.add (lang);

                }

                UIUtils.doLater (new ActionListener ()
                {

                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {

                        spellcheckLang.setModel (new DefaultComboBoxModel (langs));

                        String def = UserProperties.get (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME);

                        if (_this.viewer instanceof AbstractProjectViewer)
                        {

                            AbstractProjectViewer pv = (AbstractProjectViewer) _this.viewer;

                            spellcheckLang.setSelectedItem (pv.getProject ().getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME));

                        } else {

                            spellcheckLang.setSelectedItem (def);

                        }

                        spellcheckLang.setEnabled (true);

                        boolean isDef = def.equals (spellcheckLang.getSelectedItem ().toString ());

                        defLang.setSelected (isDef);

                    }

                });

            }

        }).start ();

        final ActionListener setLang = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                String lang = ev.getActionCommand ();

                if (_this.viewer instanceof AbstractProjectViewer)
                {

                    AbstractProjectViewer pv = (AbstractProjectViewer) _this.viewer;

                    _this.updateProjectProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME,
                                                 lang);

                    try
                    {

                        pv.setSpellCheckLanguage (lang,
                                                  true);

                    } catch (Exception e)
                    {

                        // Not good but not fatal either.
                        Environment.logError ("Unable to set spell check language to: " +
                                              lang,
                                              e);

                        return;

                    }

                    if (!lang.equals (pv.getSpellCheckLanguage ()))
                    {

                        pv.fireProjectEventLater (ProjectEvent.SPELL_CHECK,
                                                  ProjectEvent.CHANGE_LANGUAGE);

                    }

                }

            }

        };

        downloadFiles.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handlePress (MouseEvent ev)
            {

                final String lang = spellcheckLang.getSelectedItem ().toString ();

                UIUtils.downloadDictionaryFiles (lang,
                                                 _this.viewer,
                                                new ActionListener ()
                                                {

                                                   @Override
                                                   public void actionPerformed (ActionEvent ev)
                                                   {

                                                       setLang.actionPerformed (new ActionEvent (_this, 0, lang));

                                                       // Add a notification saying that the language has been set.
                                                       // We do this "later" so that the previous notification is removed
                                                       // and we don't get an unwanted border.
                                                       UIUtils.doLater (new ActionListener ()
                                                       {

                                                           @Override
                                                           public void actionPerformed (ActionEvent ev)
                                                           {

                                                               _this.viewer.addNotification (String.format ("The language files for <b>%s</b> have been downloaded and the project language set.",
                                                                                                            lang),
                                                                                             Constants.INFO_ICON_NAME,
                                                                                             30);

                                                           }

                                                       });

                                                   }

                                                });

            }

        });

        spellcheckLang.addItemListener (new ItemAdapter ()
        {

           public void itemStateChanged (ItemEvent ev)
           {

                if (ev.getStateChange () != ItemEvent.SELECTED)
                {

                    return;

                }

                final String lang = spellcheckLang.getSelectedItem ().toString ();

                String def = UserProperties.get (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME);

                final String currLang = def;

                if (Environment.isEnglish (def))
                {

                    def = Constants.ENGLISH;

                }

                defLang.setSelected (def.equals (lang));

                if (_this.viewer instanceof AbstractProjectViewer)
                {

                    AbstractProjectViewer pv = (AbstractProjectViewer) _this.viewer;

                    def = pv.getSpellCheckLanguage ();

                }

                if ((!Environment.isEnglish (lang))
                    &&
                    (!def.equals (lang))
                   )
                {

                    UIUtils.showMessage (_this.viewer,
                                         "Please note: when changing the spell check language to something other<br />than English the following features will be disabled:<ul><li>Synonym lookups</li><li>The Problem Finder</li><li>Readability Indices</li></ul>");

                }

                downloadFiles.setVisible (false);

                // Check to see if the files are available.
                try
                {

                    if (!DictionaryProvider.isLanguageInstalled (lang))
                    {

                        downloadFiles.setVisible (true);

                        UIUtils.createQuestionPopup (_this.viewer,
                                                     "Download dictionary files?",
                                                     Constants.DOWNLOAD_ICON_NAME,
                                                     "The dictionary files for <b>" +
                                                     lang +
                                                     "</b> need to be downloaded from the Quoll Writer server.<br /><br />Would you like to download them now?",
                                                     "Yes, download them",
                                                     null,
                                                     new ActionListener ()
                                                     {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            UIUtils.downloadDictionaryFiles (lang,
                                                                                             _this.viewer,
                                                                                             setLang);

                                                        }

                                                     },
                                                     new ActionListener ()
                                                     {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            spellcheckLang.setSelectedItem (currLang);

                                                        }

                                                     },
                                                     null,
                                                     null);

                        return;

                    }

                } catch (Exception e) {

                    Environment.logError ("Unable to get language files for: " +
                                          lang,
                                          e);

                    UIUtils.showErrorMessage (_this.viewer,
                                              "Unable to check for dictionary files, please contact Quoll Writer support.");

                    return;

                }

                setLang.actionPerformed (new ActionEvent (this, 0, lang));

           }

        });

        c = this.createWrapper (spellcheckLang);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (5));

        defLang.setOpaque (false);
        defLang.setAlignmentX (Component.LEFT_ALIGNMENT);

        defLang.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                if (!defLang.isSelected ())
                {

                    return;

                }

                String lang = spellcheckLang.getSelectedItem ().toString ();

                _this.updateUserProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME,
                                          lang);

            }

        });

        c = this.createWrapper (defLang);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (5));

        c = this.createWrapper (downloadFiles);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        JButton b = new JButton ("Manage your Personal Dictionary");

        b.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.viewer.showDictionaryManager ();

            }

        });

        c = this.createWrapper (b);
        this.setAsMainItem (c);

        box.add (c);

        return new SectionInfo ("Editing {Chapters}",
                                Chapter.OBJECT_TYPE,
                                "Everything to do with editing {chapters}.  Manage your personal dictionary, the language for the project, set up auto save and how edit positions behave.  All this and more for the low, low price of a few clicks.",
                                box);

    }

    private SectionInfo createNamingSection ()
    {

        final Options _this = this;

        Box box = new Box (BoxLayout.Y_AXIS);

        final JButton b = UIUtils.createButton ("Change names",
                                                null);

        b.addActionListener (new ActionAdapter ()
                             {

                                public void actionPerformed (ActionEvent ev)
                                {

                                    _this.viewer.showObjectTypeNameChanger ();

                                }

                            });

        JComponent c = this.createWrapper (b);

        this.setAsMainItem (c);

        box.add (c);

        return new SectionInfo ("What things are called",
                                Constants.CONFIG_ICON_NAME,
                                "Not happy with what things are called?  Want to change {chapter} to <i>sausage</i>?  What are you waiting for crack this section open and get changing.  Yes that's a phrase now.",
                                box);

    }

    private SectionInfo createLookSection ()
    {

        final Options _this = this;

        //box.add (this.createHeader ("How things look and sound"));
        //box.add (Box.createVerticalStrut (5));

        Box box = new Box (BoxLayout.Y_AXIS);

		final JCheckBox keepCB = new JCheckBox (Environment.replaceObjectNames ("Keep the {Projects} window open when a {project} is opened"));
		keepCB.setOpaque (false);

        keepCB.setSelected (!UserProperties.getAsBoolean (Constants.CLOSE_PROJECTS_WINDOW_WHEN_PROJECT_OPENED_PROPERTY_NAME));

        keepCB.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                _this.updateUserProperty (Constants.CLOSE_PROJECTS_WINDOW_WHEN_PROJECT_OPENED_PROPERTY_NAME,
                                          !keepCB.isSelected ());

            }

        });

        JComponent c = this.createWrapper (keepCB);
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

		final JCheckBox openPWCB = new JCheckBox (Environment.replaceObjectNames ("Show the {Projects} window when I have no open {projects}"));
		openPWCB.setOpaque (false);

        openPWCB.setSelected (UserProperties.getAsBoolean (Constants.SHOW_PROJECTS_WINDOW_WHEN_NO_OPEN_PROJECTS_PROPERTY_NAME));

        openPWCB.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                _this.updateUserProperty (Constants.SHOW_PROJECTS_WINDOW_WHEN_NO_OPEN_PROJECTS_PROPERTY_NAME,
                                          openPWCB.isSelected ());

            }

        });

        c = this.createWrapper (openPWCB);
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        final JCheckBox showPrev = new JCheckBox (Environment.replaceObjectNames ("Show a brief preview of the object when you mouse over its name in the {project} sidebar"));
        showPrev.setSelected (UserProperties.getAsBoolean (Constants.SHOW_QUICK_OBJECT_PREVIEW_IN_PROJECT_SIDEBAR_PROPERTY_NAME));
        showPrev.setOpaque (false);

        showPrev.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                _this.updateUserProperty (Constants.SHOW_QUICK_OBJECT_PREVIEW_IN_PROJECT_SIDEBAR_PROPERTY_NAME,
                                          showPrev.isSelected ());

            }

        });

        c = this.createWrapper (showPrev);
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (5));

        final JComponent label = UIUtils.createClickableLabel ("Change what is displayed",
                                                               null);

        label.addMouseListener (new MouseEventHandler ()
        {

            public void handlePress (MouseEvent ev)
            {

                _this.showEditChapterInfo ();

            }

        });

        c = this.createWrapper (label);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        final JCheckBox showNotesInChapterList = new JCheckBox (Environment.replaceObjectNames ("Show {Notes} in the {Chapter} list"));
        showNotesInChapterList.setSelected (UserProperties.getAsBoolean (Constants.SHOW_NOTES_IN_CHAPTER_LIST_PROPERTY_NAME));
        showNotesInChapterList.setOpaque (false);

        showNotesInChapterList.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                _this.updateUserProperty (Constants.SHOW_NOTES_IN_CHAPTER_LIST_PROPERTY_NAME,
                                          showNotesInChapterList.isSelected ());

                Environment.doForOpenProjects (Project.NORMAL_PROJECT_TYPE,
                                               new ProjectViewerAction<ProjectViewer> ()
                                               {

                                                    public void doAction (ProjectViewer pv)
                                                    {

                                                        pv.reloadChapterTree ();

                                                    }

                                               });

            }

        });

        c = this.createWrapper (showNotesInChapterList);
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        c = this.createHelpText ("How should the interface be laid out? (Click on the image below to change)");
        this.setAsMainItem (c);

        box.add (c);

        String selLayout = UserProperties.get (Constants.UI_LAYOUT_PROPERTY_NAME);

        final JLabel layoutSel = new JLabel (new ImageIcon (Environment.getImage (Constants.DATA_DIR + selLayout + ".png")));

        UIUtils.setAsButton (layoutSel);

        layoutSel.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handlePress (MouseEvent ev)
            {

                String selLayout = UserProperties.get (Constants.UI_LAYOUT_PROPERTY_NAME);

                final QPopup qp = UIUtils.createClosablePopup ("Select a layout",
                                                               Environment.getIcon (Constants.EDIT_ICON_NAME,
                                                                                    Constants.ICON_POPUP),
                                                               null);

                Box content = new Box (BoxLayout.Y_AXIS);

                Vector<String> layoutTypes = new Vector ();
                layoutTypes.add (Constants.LAYOUT_PS_CH);
                layoutTypes.add (Constants.LAYOUT_CH_PS);
                layoutTypes.add (Constants.LAYOUT_PS_CH_OS);
                layoutTypes.add (Constants.LAYOUT_OS_CH_PS);
                layoutTypes.add (Constants.LAYOUT_PS_OS_CH);
                layoutTypes.add (Constants.LAYOUT_CH_OS_PS);

                final JList<String> layoutL = new JList (layoutTypes);

                layoutL.setSelectedValue (selLayout,
                                          false);

                layoutL.setToolTipText ("Click to select a layout");
                layoutL.addListSelectionListener (new ListSelectionListener ()
                {

                    public void valueChanged (ListSelectionEvent ev)
                    {

                        final String layout = layoutL.getSelectedValue ();

                        layoutSel.setIcon (new ImageIcon (Environment.getImage (Constants.DATA_DIR + layout + ".png")));

                        qp.removeFromParent ();

                        _this.updateUserProperty (Constants.UI_LAYOUT_PROPERTY_NAME,
                                                  layout);

                        Environment.doForOpenProjectViewers (AbstractProjectViewer.class,
                                                             new ProjectViewerAction<AbstractProjectViewer> ()
                                                             {

                                                                public void doAction (AbstractProjectViewer pv)
                                                                {

                                                                    pv.setUILayout (layout);

                                                                }

                                                             });

                        Environment.fireUserProjectEvent (_this,
                                                          ProjectEvent.LAYOUT,
                                                          ProjectEvent.CHANGED);

                    }

                });

                layoutL.setCellRenderer (new DefaultListCellRenderer ()
                {

                    private Map<String, ImageIcon> images = new HashMap ();

                    public Component getListCellRendererComponent (JList   list,
                                                                   Object  value,
                                                                   int     index,
                                                                   boolean isSelected,
                                                                   boolean cellHasFocus)
                    {

                        super.getListCellRendererComponent (list,
                                                            value,
                                                            index,
                                                            isSelected,
                                                            cellHasFocus);

                        String imName = value.toString ();

                        ImageIcon icon = this.images.get (imName);

                        if (icon == null)
                        {

                            icon = new ImageIcon (Environment.getImage (Constants.DATA_DIR + imName + ".png"));

                            this.images.put (imName,
                                             icon);

                        }

                        this.setIcon (icon);
                        String text = "";

                        if (imName.equals (Constants.LAYOUT_PS_CH))
                        {

                            text = "Only show one sidebar, always on the left. Other sidebars will display where the {project} sidebar is.  (Default)";

                        }

                        if (imName.equals (Constants.LAYOUT_CH_PS))
                        {

                            text = "Only show one sidebar, always on the right.  Other sidebars will display where the {project} sidebar is.";

                        }

                        if (imName.equals (Constants.LAYOUT_PS_CH_OS))
                        {

                            text = "The {project} sidebar is shown on the left.  Other sidebars, such as word counts, are shown on the right.";

                        }

                        if (imName.equals (Constants.LAYOUT_OS_CH_PS))
                        {

                            text = "The {project} sidebar is shown on the right.  Other sidebars, such as {chapter} information, are shown on the left.";

                        }

                        if (imName.equals (Constants.LAYOUT_PS_OS_CH))
                        {

                            text = "The {project} sidebar is shown on the left with other sidebars next to it.";

                        }

                        if (imName.equals (Constants.LAYOUT_CH_OS_PS))
                        {

                            text = "The {project} sidebar is shown on the far right with other sidebars next to it (to the left).";

                        }

                        this.setText (String.format ("<html>%s</html>",
                                                     Environment.replaceObjectNames (text)));
                        this.setBorder (UIUtils.createPadding (5, 3, 5, 3));
                        this.setVerticalTextPosition (SwingConstants.TOP);

                        if (isSelected)
                        {

                            this.setBorder (new CompoundBorder (UIUtils.createLineBorder (),
                                                                this.getBorder ()));

                        }

                        return this;

                    }

                });

                UIUtils.setAsButton (layoutL);

                content.add (layoutL);
                content.setBorder (UIUtils.createPadding (10, 10, 10, 10));
                qp.setContent (content);

                content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                         content.getPreferredSize ().height));

                _this.viewer.showPopupAt (qp,
                                          UIUtils.getCenterShowPosition (_this.viewer,
                                                                         qp),
                                                 false);

                qp.setDraggable (_this.viewer);

            }

        });

        c = this.createWrapper (layoutSel);

        box.add (Box.createVerticalStrut (5));

        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        c = this.createHelpText ("Show the toolbar");
        this.setAsMainItem (c);

        box.add (c);

        Vector v = new Vector ();
        v.add (Environment.replaceObjectNames ("Above {Chapters}"));
        v.add (Environment.replaceObjectNames ("Below {Notes}"));

        final JComboBox toolbarLoc = new JComboBox (v);

        String loc = UserProperties.get (Constants.TOOLBAR_LOCATION_PROPERTY_NAME);

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

                if (loc.equals (UserProperties.get (Constants.TOOLBAR_LOCATION_PROPERTY_NAME)))
                {

                    return;

                }

                final String _loc = loc;

                _this.updateUserProperty (Constants.TOOLBAR_LOCATION_PROPERTY_NAME,
                                          loc);

                Environment.doForOpenProjectViewers (AbstractProjectViewer.class,
                                                     new ProjectViewerAction<AbstractProjectViewer> ()
                                                     {

                                                        public void doAction (AbstractProjectViewer pv)
                                                        {

                                                            pv.setToolbarLocation (_loc);

                                                        }

                                                     });

                Environment.fireUserProjectEvent (_this,
                                                  ProjectEvent.TOOLBAR,
                                                  ProjectEvent.MOVE);

            }

        });

        c = this.createWrapper (toolbarLoc);
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

        loc = UserProperties.get (Constants.TABS_LOCATION_PROPERTY_NAME);

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

                if (loc.equals (UserProperties.get (Constants.TABS_LOCATION_PROPERTY_NAME)))
                {

                    return;

                }

                final String _loc = loc;

                _this.updateUserProperty (Constants.TABS_LOCATION_PROPERTY_NAME,
                                          loc);

                Environment.doForOpenProjectViewers (AbstractProjectViewer.class,
                                                     new ProjectViewerAction<AbstractProjectViewer> ()
                                                     {

                                                        public void doAction (AbstractProjectViewer pv)
                                                        {

                                                            pv.setTabsLocation (_loc);

                                                        }

                                                     });

                Environment.fireUserProjectEvent (_this,
                                                  ProjectEvent.TABS,
                                                  ProjectEvent.MOVE);

            }

        });

        c = this.createWrapper (tabsLoc);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        c = this.createHelpText ("When I do a find");
        this.setAsMainItem (c);

        box.add (c);

        final JRadioButton eachChap = UIUtils.createRadioButton ("Expand all {chapters} to show all results");
        final JRadioButton justChap = UIUtils.createRadioButton ("Just show the {chapter}");

        boolean showEachChapResult = UserProperties.getAsBoolean (Constants.SHOW_EACH_CHAPTER_FIND_RESULT_PROPERTY_NAME);

        eachChap.setSelected (showEachChapResult);
        justChap.setSelected (!showEachChapResult);

        ButtonGroup g = new ButtonGroup ();
        g.add (eachChap);
        g.add (justChap);

        eachChap.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                UserProperties.set (Constants.SHOW_EACH_CHAPTER_FIND_RESULT_PROPERTY_NAME,
                                    true);

            }

        });

        justChap.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                UserProperties.set (Constants.SHOW_EACH_CHAPTER_FIND_RESULT_PROPERTY_NAME,
                                    false);

            }

        });

        c = this.createWrapper (eachChap);
        this.setAsSubItem (c);

        box.add (c);

        c = this.createWrapper (justChap);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (10));

        final JCheckBox playSound = new JCheckBox (UIUtils.formatForUser ("Play a typewriter sound when editing a {chapter}."));

        boolean playSoundEnabled = UserProperties.getAsBoolean (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME);

        final FileFinder f = new FileFinder ();
        final JButton useB = new JButton ("Use Sound");
        final JButton testB = new JButton ("Play Sound");

        f.setEnabled (playSoundEnabled);
        playSound.setOpaque (false);
        playSound.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                boolean sel = playSound.isSelected ();

                f.setEnabled (sel);

                if ((sel)
                    &&
                    (f.getSelectedFile () != null)
                    &&
                    (f.getSelectedFile ().exists ())
                   )
                {

                    useB.setEnabled (true);

                } else {

                    useB.setEnabled (false);

                }

                Environment.setPlaySoundOnKeyStroke (sel);

                _this.updateUserProperty (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME,
                                          sel);

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

        String sf = UserProperties.get (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);

        if (sf == null)
        {

            sf = "";

        }

        final String sfv = sf;

        f.setFile (new File (sfv));

        f.setApproveButtonText ("Select");
        f.setFinderSelectionMode (JFileChooser.FILES_ONLY);
        f.setFinderTitle ("Select a File");

        f.setFileFilter (new FileNameExtensionFilter ("Supported Files (wav)",
                                                      "wav"));

        f.setFindButtonToolTip ("Click to find a wav file");
        f.setClearOnCancel (true);
        f.setMaximumSize (new Dimension (400,
                                         f.getPreferredSize ().height));

        f.setOnSelectHandler (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                useB.setEnabled (true);
                testB.setEnabled (true);

            }

        });

        f.showCancel (true,
                      new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UserProperties.remove (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);

                useB.setEnabled (false);
                testB.setEnabled (false);

            }

        });

        f.init ();

        boolean sel = (f.getSelectedFile ().exists () && playSoundEnabled);
        useB.setEnabled (sel);
        testB.setEnabled (sel);

        c = this.createWrapper (f);
        this.setAsSubItem (c);

        box.add (c);

        useB.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                File file = f.getSelectedFile ();

                if ((file != null)
                    &&
                    (file.exists ())
                   )
                {

                    try
                    {

                        Environment.setKeyStrokeSoundFile (file);

                    } catch (Exception e)
                    {

                        UIUtils.showErrorMessage (_this,
                                                  file.getName () + " is not a valid .wav file.");

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
                                                      file.getName () + " is not a valid .wav file.");

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

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.LEFT_ALIGNMENT); //ButtonBarFactory.buildLeftAlignedBar (buts);
        bp.setOpaque (false);

        c = this.createWrapper (bp);

        box.add (Box.createVerticalStrut (5));

        this.setAsSubItem (c);

        box.add (c);
        
        box.add (Box.createVerticalStrut (10));

        c = this.createHelpText ("Edit Assets ({Characters}, {Locations}, etc) in");
        this.setAsMainItem (c);

        box.add (c);
        
        final JRadioButton rbAssetInPopup = UIUtils.createRadioButton ("A Popup");
        final JRadioButton rbAssetInTab = UIUtils.createRadioButton ("Their own tab");

        boolean editAssetInPopup = UserProperties.getAsBoolean (Constants.EDIT_ASSETS_IN_POPUP_PROPERTY_NAME);

        rbAssetInPopup.setSelected (editAssetInPopup);
        rbAssetInTab.setSelected (!editAssetInPopup);

        g = new ButtonGroup ();
        g.add (rbAssetInPopup);
        g.add (rbAssetInTab);
        
        rbAssetInPopup.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                UserProperties.set (Constants.EDIT_ASSETS_IN_POPUP_PROPERTY_NAME,
                                    true);

            }

        });        
        
        rbAssetInTab.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                UserProperties.set (Constants.EDIT_ASSETS_IN_POPUP_PROPERTY_NAME,
                                    false);

            }

        });        

        c = this.createWrapper (rbAssetInPopup);
        this.setAsSubItem (c);

        box.add (c);

        c = this.createWrapper (rbAssetInTab);
        this.setAsSubItem (c);

        box.add (c);        
/*
        eachChap.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                UserProperties.set (Constants.SHOW_EACH_CHAPTER_FIND_RESULT_PROPERTY_NAME,
                                    true);

            }

        });
  */      
        
        return new SectionInfo ("How things look and sound",
                                "eye",
                                "Want a sound to play whenever a key is pressed?  Want to move the tabs or sidebar around?  Want to show useful tips when Quoll Writer starts?  This is the section for you.",
                                box);

    }

    private SectionInfo createProjectSection ()
    {

        if (!(this.viewer instanceof AbstractProjectViewer))
        {

            return null;

        }

        final Options _this = this;

        final Project proj = ((AbstractProjectViewer) this.viewer).getProject ();

        Box box = new Box (BoxLayout.Y_AXIS);

        final JButton b = new JButton ("Change");

        final FileFinder f = UIUtils.createFileFind (proj.getProjectDirectory ().getParentFile ().getPath (),
                                                     "Select a Directory",
                                                     JFileChooser.DIRECTORIES_ONLY,
                                                     "Select",
                                                     null);
        f.setFindButtonToolTip (Environment.replaceObjectNames ("Click to find a new {project} directory"));
        f.setMaximumSize (new Dimension (400,
                                         f.getPreferredSize ().height));

        f.setOnSelectHandler (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                b.setEnabled (!f.getSelectedFile ().getPath ().equals (proj.getProjectDirectory ().getParentFile ().getPath ()));

            }

        });

        JComponent c = this.createHelpText ("Select the directory where your {project} is stored");
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

        box.add (Box.createVerticalStrut (15));

        Vector backupsA = new Vector ();
        backupsA.add (Constants.HOURS_12);
        backupsA.add (Constants.HOURS_24);
        backupsA.add (Constants.DAYS_2);
        backupsA.add (Constants.DAYS_5);
        backupsA.add (Constants.WEEK_1);

        final JComboBox backupsAmount = new JComboBox (backupsA);

        Vector vals = new Vector ();
        vals.add (Constants.COUNT_10);
        vals.add (Constants.COUNT_20);
        vals.add (Constants.COUNT_50);
        vals.add (Constants.COUNT_ALL);

        final JComboBox backupsCount = new JComboBox (vals);
        backupsCount.setSelectedItem (proj.getProperty (Constants.BACKUPS_TO_KEEP_COUNT_PROPERTY_NAME));

        backupsCount.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                if (ev.getStateChange () != ItemEvent.SELECTED)
                {

                    return;

                }

                _this.updateDefaultProjectProperty (Constants.BACKUPS_TO_KEEP_COUNT_PROPERTY_NAME,
                                                    (String) backupsCount.getSelectedItem ());

                try
                {

                    AbstractProjectViewer pv = (AbstractProjectViewer) _this.viewer;

                    pv.getObjectManager ().pruneBackups (pv.getProject (),
                                                         Utils.getCountAsInt ((String) backupsCount.getSelectedItem ()));

                } catch (Exception e) {

                    Environment.logError ("Unable to prune backups for project: " +
                                          proj,
                                          e);

                }

            }

        });

        final JCheckBox enableBackups = new JCheckBox (Environment.replaceObjectNames ("Automatically create backups of the {project}"));
        enableBackups.setOpaque (false);

        c = this.createWrapper (enableBackups);
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (5));

        boolean backupsEnabled = proj.getPropertyAsBoolean (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME);
        enableBackups.setSelected (backupsEnabled);

        backupsAmount.setSelectedItem (proj.getProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME));
        backupsAmount.setEnabled (backupsEnabled);

        backupsAmount.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                if (ev.getStateChange () != ItemEvent.SELECTED)
                {

                    return;

                }

                _this.updateDefaultProjectProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME,
                                                    (String) backupsAmount.getSelectedItem ());

            }

        });

        c = this.createHelpText ("Create a new backup after the following time between sessions or during a session");
        this.setAsSubItem (c);

        box.add (c);

        c = this.createWrapper (backupsAmount);
        this.setAsSubItem2 (c);

        box.add (c);

        box.add (Box.createVerticalStrut (10));

        c = this.createHelpText ("Number of backups to keep (oldest are deleted first)");
        this.setAsMainItem (c);

        box.add (c);

        c = this.createWrapper (backupsCount);
        this.setAsSubItem (c);

        box.add (c);

        final JButton bb = new JButton ("Change");

        final FileFinder bf = UIUtils.createFileFind (proj.getBackupDirectory ().getPath (),
                                                     "Select a Directory",
                                                     JFileChooser.DIRECTORIES_ONLY,
                                                     "Select",
                                                     null);
        bf.setFindButtonToolTip (Environment.replaceObjectNames ("Click to find a new backup directory"));
        bf.setMaximumSize (new Dimension (400,
                                          bf.getPreferredSize ().height));

        bf.setOnSelectHandler (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                bb.setEnabled (!bf.getSelectedFile ().getPath ().equals (proj.getBackupDirectory ().getPath ()));

            }

        });

        enableBackups.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                boolean sel = enableBackups.isSelected ();

                backupsAmount.setEnabled (sel);

                _this.updateDefaultProjectProperty (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME,
                                                    sel);

            }

        });

        box.add (Box.createVerticalStrut (10));

        c = this.createHelpText ("Select the directory where {project} backups are stored");
        this.setAsMainItem (c);

        box.add (c);

        c = this.createWrapper (bf);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (5));

        bb.setEnabled (false);

        bb.addActionListener (new ActionAdapter ()
        {

             public void actionPerformed (ActionEvent ev)
             {

                 _this.handleBackupsDirChange (bf);

             }

        });

        c = this.createWrapper (bb);
        this.setAsSubItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (10));

        JButton create = UIUtils.createButton (Constants.CREATE_BACKUP_BUTTON_LABEL_ID,
                                               null);

        create.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.showCreateBackup (proj,
                                          null,
                                          _this.viewer);

            }

        });

        JButton manage = new JButton ("Manage Backups");

        manage.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.showManageBackups (Environment.getProjectInfo (proj),
                                           _this.viewer);

            }

        });

        JButton buts[] = new JButton[] { create, manage };

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.LEFT_ALIGNMENT);
        bp.setOpaque (false);

        c = this.createWrapper (bp);

        //c = this.createWrapper (mb);
        this.setAsMainItem (c);

        box.add (c);

        return new SectionInfo ("{Project} & Backups",
                                Constants.PROJECT_ICON_NAME,
                                "Click the title above to open this section.  You can then change where your {project} is stored and how often backups are created and where they are stored.",
                                box);

    }

    private SectionInfo createWarmupsSection ()
    {

        final Options _this = this;

        //box.add (this.createHeader (UIUtils.formatForUser ("{Warmups}")));
        //box.add (Box.createVerticalStrut (5));

        Box box = new Box (BoxLayout.Y_AXIS);

        JComponent c = this.createWrapper (WarmupPromptSelect.getDoWarmupOnStartupCheckbox ());
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (5));

        c = this.createHelpText ("And do the {warmup} for");
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

        //this.setContentBorder (box);

        SectionInfo info = new SectionInfo ("{Warmups}",
                                            Warmup.OBJECT_TYPE,
                                            "Want to flex your writing muscles everytime Quoll Writer starts?  You'd better click the title above and get things set up while you still have time.",
                                            box);

        return info;

    }

    private void setContentBorder (JComponent box)
    {

        box.setBorder (UIUtils.createPadding (7, 0, 10, 0));

    }

    private void updateUserProperty (String  name,
                                     boolean value)
    {

        UserProperties.set (name,
                            value);

        if (this.viewer instanceof AbstractProjectViewer)
        {

            ((AbstractProjectViewer) this.viewer).getProject ().getProperties ().removeProperty (name);

        }

    }

    private void updateUserProperty (String name,
                                     String value)
    {

        UserProperties.set (name,
                            value);

        if (this.viewer instanceof AbstractProjectViewer)
        {

            ((AbstractProjectViewer) this.viewer).getProject ().getProperties ().removeProperty (name);

        }

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

        AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

        Properties props = pv.getProject ().getProperties ();

        props.setProperty (name,
                           new StringProperty (name,
                                               value));

        try
        {

            pv.saveProject ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to save project",
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to save.");

            return;

        }

    }

    /*
    private void addProjectAndSnapshotsSection ()
    {

        final OptionsPanel _this = this;

        Box box = new Box (BoxLayout.Y_AXIS);

        final JButton b = new JButton ("Change");

        final FileFinder f = UIUtils.createFileFind (this.projectViewer.getProject ().getProjectDirectory ().getParentFile ().getPath (),
                                                     "Select a Directory",
                                                     JFileChooser.DIRECTORIES_ONLY,
                                                     "Select",
                                                     null);
        f.setFindButtonToolTip (Environment.replaceObjectNames ("Click to find a new {project} directory"));
        f.setMaximumSize (new Dimension (400,
                                         f.getPreferredSize ().height));

        f.setOnSelectHandler (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                b.setEnabled (!f.getSelectedFile ().getPath ().equals (_this.projectViewer.getProject ().getProjectDirectory ().getParentFile ().getPath ()));

            }

        });

        JComponent c = this.createHelpText ("Select the directory where your {project} is stored");
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

        box.add (Box.createVerticalStrut (15));

        Vector backupsA = new Vector ();
        backupsA.add (Constants.HOURS_12);
        backupsA.add (Constants.HOURS_24);
        backupsA.add (Constants.DAYS_2);
        backupsA.add (Constants.DAYS_5);
        backupsA.add (Constants.WEEK_1);

        final JComboBox backupsAmount = new JComboBox (backupsA);

        Vector vals = new Vector ();
        vals.add (Constants.COUNT_10);
        vals.add (Constants.COUNT_20);
        vals.add (Constants.COUNT_50);
        vals.add (Constants.COUNT_ALL);

        final JComboBox backupsCount = new JComboBox (vals);
        backupsCount.setSelectedItem (this.projectViewer.getProject ().getProperty (Constants.BACKUPS_TO_KEEP_COUNT_PROPERTY_NAME));

        backupsCount.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                if (ev.getStateChange () != ItemEvent.SELECTED)
                {

                    return;

                }

                _this.updateDefaultProjectProperty (Constants.BACKUPS_TO_KEEP_COUNT_PROPERTY_NAME,
                                                    (String) backupsCount.getSelectedItem ());

                try
                {

                    _this.projectViewer.getObjectManager ().pruneBackups (_this.projectViewer.getProject (),
                                                                          Utils.getCountAsInt ((String) backupsCount.getSelectedItem ()));

                } catch (Exception e) {

                    Environment.logError ("Unable to prune backups for project: " +
                                          _this.projectViewer.getProject (),
                                          e);

                }

            }

        });

        final JCheckBox enableBackups = new JCheckBox (Environment.replaceObjectNames ("Automatically create backups of the {project}"));
        enableBackups.setOpaque (false);

        c = this.createWrapper (enableBackups);
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (5));

        boolean backupsEnabled = this.projectViewer.getProject ().getPropertyAsBoolean (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME);
        enableBackups.setSelected (backupsEnabled);

        backupsCount.setEnabled (backupsEnabled);

        backupsAmount.setSelectedItem (this.projectViewer.getProject ().getProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME));
        backupsAmount.setEnabled (backupsEnabled);

        backupsAmount.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                if (ev.getStateChange () != ItemEvent.SELECTED)
                {

                    return;

                }

                _this.updateDefaultProjectProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME,
                                                    (String) backupsAmount.getSelectedItem ());

            }

        });

        backupsCount.setEnabled (enableBackups.isSelected ());

        c = this.createHelpText ("Create a new backup after the following time between sessions or during a session");
        this.setAsSubItem (c);

        box.add (c);

        c = this.createWrapper (backupsAmount);
        this.setAsSubItem2 (c);

        box.add (c);

        box.add (Box.createVerticalStrut (10));

        c = this.createHelpText ("Number of backups to keep (oldest are deleted first)");
        this.setAsSubItem (c);

        box.add (c);

        c = this.createWrapper (backupsCount);
        this.setAsSubItem2 (c);

        box.add (c);

        final JButton bb = new JButton ("Change");

        final FileFinder bf = UIUtils.createFileFind (this.projectViewer.getProject ().getBackupDirectory ().getPath (),
                                                     "Select a Directory",
                                                     JFileChooser.DIRECTORIES_ONLY,
                                                     "Select",
                                                     null);
        bf.setFindButtonToolTip (Environment.replaceObjectNames ("Click to find a new backup directory"));
        bf.setMaximumSize (new Dimension (400,
                                          bf.getPreferredSize ().height));

        bf.setOnSelectHandler (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                bb.setEnabled (!bf.getSelectedFile ().getPath ().equals (_this.projectViewer.getProject ().getBackupDirectory ().getPath ()));

            }

        });

        enableBackups.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                boolean sel = enableBackups.isSelected ();

                backupsAmount.setEnabled (sel);

                backupsCount.setEnabled (sel);

                bf.setEnabled (sel);

                _this.updateDefaultProjectProperty (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME,
                                                    sel);

            }

        });

        box.add (Box.createVerticalStrut (10));

        c = this.createHelpText ("Select the directory where {project} backups are stored");
        this.setAsSubItem (c);

        box.add (c);

        c = this.createWrapper (bf);
        this.setAsSubItem2 (c);

        box.add (c);

        box.add (Box.createVerticalStrut (5));

        bb.setEnabled (false);

        bb.addActionListener (new ActionAdapter ()
        {

             public void actionPerformed (ActionEvent ev)
             {

                 _this.handleBackupsDirChange (bf);

             }

        });

        c = this.createWrapper (bb);
        this.setAsSubItem2 (c);

        box.add (c);

        box.add (Box.createVerticalStrut (10));

        JButton create = new JButton ("Create a Backup");

        create.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.showCreateBackup (_this.projectViewer.getProject (),
                                          null,
                                          _this.projectViewer);

            }

        });

        JButton manage = new JButton ("Manage Backups");

        manage.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.showManageBackups (Environment.getProjectInfo (_this.projectViewer.getProject ()),
                                           _this.projectViewer);

            }

        });

        JButton buts[] = new JButton[] { create, manage };

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.LEFT_ALIGNMENT);
        bp.setOpaque (false);

        c = this.createWrapper (bp);

        //c = this.createWrapper (mb);
        this.setAsMainItem (c);

        box.add (c);

        this.setContentBorder (box);

        Accordion.Item item = this.accordion.add (this.createHeader (Environment.replaceObjectNames ("{Project} & Backups"),
                                                                     Constants.PROJECT_ICON_NAME),
                                                  null,
                                                  box,
                                                  UIUtils.createHelpTextPane ("Click the title above to open this section.  You can then change where your {project} is stored and how often backups are created and where they are stored.",
                                                                              this.viewer));

        this.sections.put (Section.Project,
                           item);


    }
    */
    private boolean handleProjectDirChange (final FileFinder f)
    {

        final Options _this = this;

        AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

        final Project proj = pv.getProject ();

        final File oldProjDir = proj.getProjectDirectory ();

        final File newDir = f.getSelectedFile ();

        final File newProjDir = new File (newDir,
                                          Utils.sanitizeForFilename (proj.getName ()));

        boolean backupIsSubDir = false;

        File _newBackupDir = null;

        if (Utils.isSubDir (proj.getProjectDirectory (),
                            proj.getBackupDirectory ()))
        {

            backupIsSubDir = true;

            try
            {

                _newBackupDir = new File (newProjDir.getPath () + proj.getBackupDirectory ().getCanonicalPath ().substring (proj.getProjectDirectory ().getCanonicalPath ().length ()));

            } catch (Exception e) {

                Environment.logError ("Unable to determine new backups directory for: " +
                                      proj.getBackupDirectory () +
                                      " to: " +
                                      newProjDir);

                UIUtils.showErrorMessage (null,
                                          "Unable to change project directory, please contact Quoll Writer support for assistance.");

                return false;

            }

            if (!Utils.isDirectoryEmpty (_newBackupDir))
            {

                UIUtils.showErrorMessage (this.viewer,
                                          "New backups directory: " +
                                          _newBackupDir +
                                          " is not empty.");

                f.setFile (oldProjDir);

                return false;

            }

        }

        final File newBackupDir = _newBackupDir;

        // See if the project directory is changing.
        if (!newProjDir.equals (oldProjDir))
        {

            if (!Utils.isDirectoryEmpty (newProjDir))
            {

                UIUtils.showErrorMessage (this.viewer,
                                          "Unable to change {project} directory to: " +
                                          newProjDir +
                                          " directory is not empty.");

                f.setFile (oldProjDir);

                return false;

            }

            // Is changing so need to close the project.
            Point p = SwingUtilities.convertPoint (f,
                                                   0,
                                                   0,
                                                   this.viewer);

            // Inception!  Don't you just love asynchronous programming!
            ActionListener confirmAction = new ActionListener ()
            {

               public void actionPerformed (ActionEvent ev)
               {

                    if (newBackupDir != null)
                    {

                        // Need to change the backup dir first.
                        proj.setBackupDirectory (newBackupDir);

                    }

                    _this.viewer.close (true,
                                        new ActionListener ()
                    {

                       public void actionPerformed (ActionEvent ev)
                       {

                            if (!proj.getProjectDirectory ().renameTo (newProjDir))
                            {

                                Environment.logError ("Unable to rename project directory: " +
                                                      proj.getProjectDirectory () +
                                                      " to: " +
                                                      newProjDir);

                                UIUtils.showErrorMessage (null,
                                                          "Unable to change project directory, please contact Quoll Writer support for assistance.");

                            } else {

                                proj.setProjectDirectory (newProjDir);

                            }

                            // Open the project again.
                            try
                            {

                                Environment.openProject (proj,
                                                         new ActionListener ()
                                                         {

                                                            @Override
                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                Environment.getProjectViewer (proj).fireProjectEventLater (proj.getObjectType (),
                                                                                                                           ProjectEvent.CHANGED_DIRECTORY);


                                                            }

                                                         });

                            } catch (Exception e)
                            {

                                // Show the projects window.
                                Environment.showLanding ();

                                Environment.logError ("Unable to reopen project: " +
                                                      proj,
                                                      e);

                                UIUtils.showErrorMessage (null,
                                                          "Unable to reopen project, please contact Quoll Writer support for assistance.");

                                return;

                            }

                            // Finally, delete the old project directory.
                            //Utils.deleteDir (oldDir);

                        }

                   });

                }

            };

            ActionListener onCancel = new ActionListener ()
            {

               public void actionPerformed (ActionEvent ev)
               {

                   // Reset the file.
                   f.setFile (oldProjDir.getParentFile ());

               }

            };

            String extra = "";

            if (backupIsSubDir)
            {

                extra = "<span class='error'>Warning!  The backups directory for this {project} will also be changed.</span><br /><br />";

            }

            UIUtils.createQuestionPopup (this.viewer,
                                         "Confirm change to {project} directory?",
                                         Project.OBJECT_TYPE,
                                         "To change the directory of a {project} it must first be saved and closed.  Once the directory has been changed the {project} will be reopened.<br /><br />" + extra + "Do you wish to continue?",
                                         "Yes, change it",
                                         null,
                                         confirmAction,
                                         onCancel,
                                         onCancel,
                                         p);

        }

        return false;

    }

    private void handleBackupsDirChange (final FileFinder f)
    {

        final Options _this = this;

        AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

        final Project proj = pv.getProject ();

        final File oldDir = proj.getBackupDirectory ();

        final File newDir = f.getSelectedFile ();

        // See if the project directory is changing.
        if (!newDir.equals (oldDir))
        {

            if (!Utils.isDirectoryEmpty (newDir))
            {

                UIUtils.showErrorMessage (this.viewer,
                                          "Unable to change backup directory to: " +
                                          newDir +
                                          ", directory is not empty.");

                return;

            }

            // Do a trick, delete the directory then rename to it.
            if (newDir.exists ())
            {

                newDir.delete ();

            }

            // Just rename it.
            if (!oldDir.renameTo (newDir))
            {

                UIUtils.showErrorMessage (this.viewer,
                                          "Unable to change backup directory to: " +
                                          newDir);

                return;

            }

            proj.setBackupDirectory (newDir);

            UIUtils.showMessage ((PopupsSupported) this.viewer,
                                 "Backups directory changed",
                                 String.format ("The backups directory for the {project} has been changed to <b>%s</b>.",
                                                newDir.getPath ()));

        }

    }

    private Header createHeader (String title)
    {

        return this.createHeader (title,
                                  null);

    }

    private Header createHeader (String title,
                                 String iconType)
    {

        Header h = UIUtils.createHeader (title,
                                         Constants.SUB_PANEL_TITLE,
                                         iconType,
                                         null);

        h.setBorder (UIUtils.createBottomLineWithPadding (0, 0, 3, 0));

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

    private void setAsSubItem2 (JComponent c)
    {

        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        c.setAlignmentY (Component.TOP_ALIGNMENT);

        c.setBorder (new CompoundBorder (new EmptyBorder (0, 20, 0, 10),
                                         c.getBorder ()));

    }

    private JComponent createWrapper (JComponent c)
    {

        if ((c instanceof JComboBox)
            ||
            (c instanceof JCheckBox)
           )
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

        JTextPane t = UIUtils.createHelpTextPane (text,
                                                  this.viewer);
        t.setBorder (new EmptyBorder (0,
                                      5,
                                      5,
                                      5));

        return t;

    }

    public void setState (final Map<String, String> s)
    {

        final Options _this = this;

        this.accordion.setState (s.get ("sections"));

        SwingUtilities.invokeLater (new Runnable ()
        {

            public void run ()
            {

                int o = 0;

                try
                {

                    o = Integer.parseInt (s.get ("scroll"));

                } catch (Exception e) {

                    return;

                }

                _this.scrollPane.getVerticalScrollBar ().setValue (o);

                //_this.setReadyForUse (true);

            }

        });

    }

    public void getState (Map<String, Object> m)
    {

        m.put ("sections",
               this.accordion.getState ());
        m.put ("scroll",
               this.scrollPane.getVerticalScrollBar ().getValue ());

    }

    public void showEditChapterInfo ()
    {

		final Options _this = this;

        String popupName = "editchapterinfo";
        final QPopup popup = UIUtils.createClosablePopup ("Change what is displayed",
												    Environment.getIcon (Constants.EDIT_ICON_NAME,
																	     Constants.ICON_POPUP),
												    null);

        popup.setPopupName (popupName);

		OptionsBox bb = new OptionsBox (this.viewer);

		final TextArea status = new TextArea ("Enter the format here...",
											  3,
											  -1);
		status.setText (UserProperties.get (Constants.CHAPTER_INFO_PREVIEW_FORMAT,
                                            Constants.DEFAULT_CHAPTER_INFO_PREVIEW_FORMAT));

        final Chapter _bogus = new Chapter ();
        _bogus.setKey (1L);
        _bogus.setDescription (new StringWithMarkup ("This chapter will be really, really good once I actually start to write it."));
        _bogus.setText (new StringWithMarkup ("Once upon a time there was a little chapter that wanted to be written."));

        final ProjectViewer _bogusPV = new ProjectViewer ()
        {

           public ChapterCounts getChapterCounts (Chapter c)
           {

               ChapterCounts cc = new ChapterCounts ();
               cc.wordCount = new TextIterator (_bogus.getChapterText ()).getWords ().size ();

               return cc;

           }

           public Set<Word> getSpellingErrors (Chapter c)
           {

               Set<Word> ret = new HashSet ();
               return ret;

           }

           public Set<Issue> getProblems (Chapter c)
           {

               Set<Issue> ret = new HashSet ();
               return ret;

           }

       };

       final JComponent pb = UIUtils.getChapterInfoPreview (_bogus,
                                                            null,
                                                            _bogusPV);

		bb.addMain ("Use the following format for {chapter} information.  <a href='help:chapters/landing'>Click here</a> for help on the format/tags that can be used.",
					status);

		Box pbb = new Box (BoxLayout.X_AXIS);

        int BOX_WIDTH = 380;

		pbb.setMaximumSize (new Dimension (BOX_WIDTH,
										   Short.MAX_VALUE));

        final Box previewWrapper = new Box (BoxLayout.Y_AXIS);

        previewWrapper.add (pb);
		pbb.add (previewWrapper);

        status.addCaretListener (new CaretListener ()
		{

			@Override
			public void caretUpdate (CaretEvent ev)
			{

				UIUtils.doLater (new ActionListener ()
				{

					@Override
					public void actionPerformed (ActionEvent ev)
					{

                        previewWrapper.removeAll ();

                        previewWrapper.add (UIUtils.getChapterInfoPreview (_bogus,
                                                                           status.getText (),
                                                                           _bogusPV));

						popup.resize ();

					}

				});

			}

		});

		bb.addMain (UIUtils.createBoldSubHeader ("Example",
												null),
				   pbb);

        Box content = new Box (BoxLayout.Y_AXIS);

        content.add (bb);
        content.add (pbb);
		content.add (Box.createVerticalStrut (15));

		//content.add (Box.createVerticalGlue ());

		JButton save = UIUtils.createButton (Environment.getButtonLabel (Constants.SAVE_BUTTON_LABEL_ID),
											 null);

		save.addActionListener (new ActionAdapter ()
		{

			public void actionPerformed (ActionEvent ev)
			{

				UserProperties.set (Constants.CHAPTER_INFO_PREVIEW_FORMAT,
									status.getText ());

                previewWrapper.removeAll ();

                previewWrapper.add (UIUtils.getChapterInfoPreview (_bogus,
                                                                   status.getText (),
                                                                   _bogusPV));

                popup.resize ();

			}

		});

		JButton cancel = UIUtils.createButton (Environment.getButtonLabel (Constants.CANCEL_BUTTON_LABEL_ID),
											   null);

		cancel.addActionListener (new ActionAdapter ()
		{

			public void actionPerformed (ActionEvent ev)
			{

				popup.removeFromParent ();

			}

		});

		JButton reset = new JButton ("Use default");

		reset.addActionListener (new ActionAdapter ()
		{

			public void actionPerformed (ActionEvent ev)
			{

				status.setText (UserProperties.get (Constants.DEFAULT_CHAPTER_INFO_PREVIEW_FORMAT));

                previewWrapper.removeAll ();

                previewWrapper.add (UIUtils.getChapterInfoPreview (_bogus,
                                                                   status.getText (),
                                                                   _bogusPV));

				popup.resize ();

				UserProperties.set (Constants.CHAPTER_INFO_PREVIEW_FORMAT,
									status.getText ());

			}

		});

		JButton[] buts = { save, reset, cancel };

		JPanel bp = UIUtils.createButtonBar2 (buts,
											  Component.CENTER_ALIGNMENT);
		bp.setOpaque (false);
		bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
		content.add (bp);

        Box _content = new Box (BoxLayout.Y_AXIS);
        _content.add (content);

		content.setBorder (UIUtils.createPadding (10, 10, 10, 10));

		_content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
									 _content.getPreferredSize ().height));

		popup.setContent (_content);

		this.viewer.showPopupAt (popup,
							UIUtils.getCenterShowPosition (this,
														   popup),
							false);

		popup.setDraggable (this.viewer);

		// TODO: Why Swing??? Why, why, why???
		UIUtils.doLater (new ActionListener ()
		{

			@Override
			public void actionPerformed (ActionEvent ev)
			{

				//popup.resize ();

			}

		});

    }

}
