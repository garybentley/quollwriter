package com.quollwriter.ui.panels;

import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.event.*;

import java.io.*;
import java.net.*;
import java.beans.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;
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

import com.quollwriter.editors.messages.*;
import java.util.Date;
import com.quollwriter.editors.ui.*;

public class OptionsPanel extends QuollPanel
{
    
    public static final String PANEL_ID = "options";
    
    private Accordion accordion = null;
    private JScrollPane scrollPane = null;
    private JCheckBox sendErrorsToSupport = null;
    
    private Map<String, Accordion.Item> sections = new HashMap ();
    
    public OptionsPanel (AbstractProjectViewer pv)
                  throws GeneralException
    {

        super (pv,
               pv.getProject ());

    }
    
    public void showSection (String name)
    {
        
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
                                                          
            Timer cycle = UIUtils.createCyclicAnimator (l,
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
            
            // Scroll to it and open.
            item.scrollRectToVisible (item.getBounds ());
            
            cycle.start ();
            
        }
        
    }
    
    public void init ()
    {
               
        Header h = UIUtils.createHeader ("Options",
                                         Constants.PANEL_TITLE,
                                         "options",
                                         null);

        this.add (h);
                                         
        Box b = new Box (BoxLayout.Y_AXIS);

        final OptionsPanel _this = this;
        
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

    private void addWarmupsSection ()
    {

        final OptionsPanel _this = this;
        
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
        
        this.setContentBorder (box);
        
        this.accordion.add (this.createHeader (UIUtils.formatForUser ("{Warmups}"),
                                               Warmup.OBJECT_TYPE),
                            null,
                            box,
                            UIUtils.createHelpTextPane ("Want to flex your writing muscles everytime Quoll Writer starts?  You'd better click the title above and get things set up while you still have time.",
                                                        this.projectViewer));
                
    }
    
    private void setContentBorder (Box box)
    {
        
        box.setBorder (new EmptyBorder (7, 0, 10, 0));
        
    }
    
    private void addAchievementsSection ()
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

        this.setContentBorder (box);
        
        this.accordion.add (this.createHeader (UIUtils.formatForUser ("Achievements"),
                                               Constants.ACHIEVEMENT_ICON_NAME),
                            null,
                            box,
                            UIUtils.createHelpTextPane ("Are the achievements annoying you?  Use this section to switch them off and they will bug you no more you underachiever.",
                                                        this.projectViewer));
                
    }

    private void addProblemsSection ()
    {
                
        final OptionsPanel _this = this;
                
        //box.add (this.createHeader (UIUtils.formatForUser ("When something goes wrong")));
        //box.add (Box.createVerticalStrut (5));

        Box box = new Box (BoxLayout.Y_AXIS);
        
        this.sendErrorsToSupport = UIUtils.createCheckBox ("Send errors to Quoll Writer support");
        this.sendErrorsToSupport.setSelected (Environment.getUserProperties ().getPropertyAsBoolean (Constants.AUTO_SEND_ERRORS_TO_SUPPORT_PROPERTY_NAME));
        
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

        this.setContentBorder (box);
        
        this.accordion.add (this.createHeader (UIUtils.formatForUser ("When something goes wrong"),
                                               Constants.BUG_ICON_NAME),
                            null,
                            box,
                            UIUtils.createHelpTextPane ("Quoll Writer isn't perfect and it would be good to know when things cluck up.  If you open this section you'll find a single setting that will let you send errors back to the magical worker elfs at Quoll Writer Headquarters located in deepest, darkest suburban Australia.  Did you know that every error sent will prevent a Drop Bear attack.  It's a serious and very real threat to our native elves.",
                                                        this.projectViewer));
        
    }

    private void addBetasSection ()
    {
                
        final OptionsPanel _this = this;
                
        //box.add (this.createHeader (UIUtils.formatForUser ("When something goes wrong")));
        //box.add (Box.createVerticalStrut (5));

        Box box = new Box (BoxLayout.Y_AXIS);
        
        final JCheckBox optinToBetas = UIUtils.createCheckBox ("Opt-in to beta versions (enables auto-send of errors to Quoll Writer support)");
        optinToBetas.setSelected (Environment.getUserProperties ().getPropertyAsBoolean (Constants.OPTIN_TO_BETA_VERSIONS_PROPERTY_NAME));
        
        optinToBetas.addActionListener (new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {

                if (optinToBetas.isSelected ())
                {
                    
                    UIUtils.showMessage ((PopupsSupported) _this.projectViewer,
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

        this.setContentBorder (box);
        
        this.accordion.add (this.createHeader (UIUtils.formatForUser ("Beta versions"),
                                               Constants.ABOUT_ICON_NAME),
                            null,
                            box,
                            UIUtils.createHelpTextPane ("Want to get ahead of the crowd?  Or maybe help improve Quoll Writer?  This section lets you opt-in to beta versions.  But be warned, betas aren't perfect.",
                                                        this.projectViewer));
        
    }

    private void addEditorsSection ()
    {
        
        if (EditorsEnvironment.getUserAccount () == null)
        {
            
            return;
            
        }
        
        final OptionsPanel _this = this;
                
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

        this.setContentBorder (box);
        
        Accordion.Item item = this.accordion.add (this.createHeader (UIUtils.formatForUser ("{Editors} Service"),
                                                  Constants.EDITORS_ICON_NAME),
                                                  null,
                                                  box,
                                                  UIUtils.createHelpTextPane ("Options related to the Editors service and how you interact with your {contacts}.",
                                                                              this.projectViewer));
        
        this.sections.put ("editors",
                           item);
        
    }
    
    private void addEditingChaptersSection ()
    {
        
        final OptionsPanel _this = this;
        
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

        final Properties userProps = Environment.getUserProperties ();
  
        c = this.createHelpText ("Show an icon against a {chapter} in the {chapter} list when");
        this.setAsMainItem (c);
  
        box.add (c);
  
        final JCheckBox showEditPos = new JCheckBox (Environment.replaceObjectNames ("it has an edit position"));
        showEditPos.setSelected (userProps.getPropertyAsBoolean (Constants.SHOW_EDIT_POSITION_ICON_IN_CHAPTER_LIST_PROPERTY_NAME));
        showEditPos.setOpaque (false);
        showEditPos.setAlignmentX (Component.LEFT_ALIGNMENT);

        showEditPos.addItemListener (new ItemAdapter ()
        {
           
            public void itemStateChanged (ItemEvent ev)
            {
                
                _this.updateUserProperty (Constants.SHOW_EDIT_POSITION_ICON_IN_CHAPTER_LIST_PROPERTY_NAME,
                                          showEditPos.isSelected ());
                
                // Redraw the chapter list.
                if (_this.projectViewer instanceof ProjectViewer)
                {
                    
                    ((ProjectViewer) _this.projectViewer).reloadChapterTree ();
                    
                }
                
            }
            
        });
        
        c = this.createWrapper (showEditPos);
        this.setAsSubItem (c);
        
        box.add (c);
        
        final JCheckBox showEdited = new JCheckBox (Environment.replaceObjectNames ("it is set as edit complete"));
        showEdited.setSelected (userProps.getPropertyAsBoolean (Constants.SHOW_EDIT_COMPLETE_ICON_IN_CHAPTER_LIST_PROPERTY_NAME));
        showEdited.setOpaque (false);
        showEdited.setAlignmentX (Component.LEFT_ALIGNMENT);

        showEdited.addItemListener (new ItemAdapter ()
        {
           
            public void itemStateChanged (ItemEvent ev)
            {
                
                _this.updateUserProperty (Constants.SHOW_EDIT_COMPLETE_ICON_IN_CHAPTER_LIST_PROPERTY_NAME,
                                          showEdited.isSelected ());
                
                // Redraw the chapter list.
                if (_this.projectViewer instanceof ProjectViewer)
                {
                    
                    ((ProjectViewer) _this.projectViewer).reloadChapterTree ();
                    
                }
                
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
                
                QPopup popup = _this.projectViewer.getPopupByName ("edit-complete-example-popup");
                
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
                
                _this.projectViewer.showPopupAt (popup,
                                                 label,
                                                 true);
                
            }
            
        });
        
        c = this.createWrapper (label);
        this.setAsSubItem (c);
        
        box.add (c);
        
        box.add (Box.createVerticalStrut (15));

        final JCheckBox showEditMarker = new JCheckBox (Environment.replaceObjectNames ("Show the edit position in a {chapter}"));
        showEditMarker.setSelected (userProps.getPropertyAsBoolean (Constants.SHOW_EDIT_MARKER_IN_CHAPTER_PROPERTY_NAME));
        showEditMarker.setOpaque (false);
        showEditMarker.setAlignmentX (Component.LEFT_ALIGNMENT);

        showEditMarker.addItemListener (new ItemAdapter ()
        {
           
            public void itemStateChanged (ItemEvent ev)
            {
                
                _this.updateUserProperty (Constants.SHOW_EDIT_MARKER_IN_CHAPTER_PROPERTY_NAME,
                                          showEditMarker.isSelected ());

                _this.projectViewer.doForPanels (QuollEditorPanel.class,
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
        
        c = this.createWrapper (showEditMarker);
        this.setAsMainItem (c);
        
        box.add (c);

        box.add (Box.createVerticalStrut (5));

        c = this.createHelpText ("Use the following color for the edit position indicator");
        this.setAsSubItem (c);

        box.add (c);

        final Color col = UIUtils.getColor (userProps.getProperty (Constants.EDIT_MARKER_COLOR_PROPERTY_NAME));
        
        final JPanel cSwatch = QColorChooser.getSwatch (col);

        UIUtils.setAsButton (cSwatch);
        
        cSwatch.addMouseListener (new MouseAdapter ()
        {

            public void mouseReleased (MouseEvent ev)
            {

                String colors = _this.projectViewer.getProject ().getProperty (Constants.COLOR_SWATCHES_PROPERTY_NAME);
                            
                QPopup popup = QColorChooser.getColorChooserPopup (colors,
                                                                   col,
                                                                   new ChangeAdapter ()
                                                                   {
                         
                                                                     public void stateChanged (ChangeEvent ev)
                                                                     {
                         
                                                                         Color c = (Color) ev.getSource ();

                                                                        _this.updateUserProperty (Constants.EDIT_MARKER_COLOR_PROPERTY_NAME,
                                                                                                  UIUtils.colorToHex (c));
                                                        
                                                                        _this.projectViewer.doForPanels (QuollEditorPanel.class,
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

                popup.setDraggable (_this.projectViewer);
                                                                   
                _this.projectViewer.showPopupAt (popup,
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

                QPopup popup = _this.projectViewer.getPopupByName ("edit-position-example-popup");
                
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
                                                                                                  
                _this.projectViewer.showPopupAt (popup,
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
        markEdited.setSelected (userProps.getPropertyAsBoolean (Constants.SET_CHAPTER_AS_EDIT_COMPLETE_WHEN_EDIT_POSITION_IS_AT_END_OF_CHAPTER_PROPERTY_NAME));
        markEdited.setAlignmentX (Component.LEFT_ALIGNMENT);

        markEdited.addItemListener (new ItemAdapter ()
        {
           
            public void itemStateChanged (ItemEvent ev)
            {
                
                _this.updateUserProperty (Constants.SET_CHAPTER_AS_EDIT_COMPLETE_WHEN_EDIT_POSITION_IS_AT_END_OF_CHAPTER_PROPERTY_NAME,
                                          markEdited.isSelected ());

                _this.projectViewer.doForPanels (AbstractEditorPanel.class,
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
                                                            
                                                                _this.projectViewer.setChapterEditPosition (c,
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
        
        c = this.createWrapper (markEdited);
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        final JCheckBox compressMenu = new JCheckBox (Environment.replaceObjectNames ("Compress the {chapter} right click menu"));
        compressMenu.setOpaque (false);
        compressMenu.setSelected (userProps.getPropertyAsBoolean (Constants.COMPRESS_CHAPTER_CONTEXT_MENU_PROPERTY_NAME));
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

                QPopup popup = _this.projectViewer.getPopupByName (name);
                
                if (popup == null)
                {

                    popup = UIUtils.createClosablePopup ("Example",
                                                         null,
                                                         null);
                                                                                      
                    ImagePanel ip = new ImagePanel (Environment.getImage (img),
                                                    null);
                                
                    popup.setName (name);                                                                                                                                          
                    popup.setContent (ip);                                                                      
                         
                    popup.setDraggable (_this.projectViewer);
                         
                }
                                                                                              
                _this.projectViewer.showPopupAt (popup,
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

        try
        {                
        
            downloadFiles.setVisible (!DictionaryProvider.isLanguageInstalled (this.projectViewer.getSpellCheckLanguage ()));
            
        } catch (Exception e) {
            
            // Ignore.
                
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
                
                SwingUtilities.invokeLater (new Runnable ()
                {
                    
                    public void run ()
                    {
                
                        spellcheckLang.setModel (new DefaultComboBoxModel (langs));
                        spellcheckLang.setSelectedItem (_this.projectViewer.getProject ().getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME));
                        spellcheckLang.setEnabled (true);

                        boolean isDef = Environment.getUserProperties ().getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME).equals (spellcheckLang.getSelectedItem ().toString ());
                        
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

                _this.updateProjectProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME,
                                             lang);
                
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

                if (!lang.equals (_this.projectViewer.getSpellCheckLanguage ()))
                {
                    
                    _this.projectViewer.fireProjectEventLater (ProjectEvent.SPELL_CHECK,
                                                               ProjectEvent.CHANGE_LANGUAGE);
                    
                }
                
            }
            
        };
        
        downloadFiles.addMouseListener (new MouseEventHandler ()
        {
            
            @Override
            public void handlePress (MouseEvent ev)
            {
                
                final String lang = spellcheckLang.getSelectedItem ().toString ();
                
                _this.projectViewer.downloadDictionaryFiles (lang,
                                                             new ActionAdapter ()
                                                             {
                                                                
                                                                public void actionPerformed (ActionEvent ev)
                                                                {
                                                                    
                                                                    setLang.actionPerformed (new ActionEvent (_this, 0, lang));
                                                                
                                                                    // Add a notification saying that the language has been set.
                                                                    // We do this "later" so that the previous notification is removed
                                                                    // and we don't get an unwanted border.
                                                                    SwingUtilities.invokeLater (new Runnable ()
                                                                    {
                                                                        
                                                                        public void run ()
                                                                        {
                                                                            
                                                                            _this.projectViewer.addNotification (String.format ("The language files for <b>%s</b> have been downloaded and the project language set.",
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

                String def = Environment.getUserProperties ().getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME);

                final String currLang = def;
                                
                if (Environment.isEnglish (def))
                {
                    
                    def = Constants.ENGLISH;
                                
                }
                
                defLang.setSelected (def.equals (lang));
                
                if ((!Environment.isEnglish (lang))
                    &&
                    (!_this.projectViewer.getSpellCheckLanguage ().equals (lang))
                   )
                {

                    UIUtils.showMessage (_this,
                                         "Please note: when changing the spell check language to something other\nthan English the following features will be disabled:\n\n  * synonym lookups\n  * the problem finder\n  * readability indices");
                    
                }
                        
                downloadFiles.setVisible (false);
            
                // Check to see if the files are available.
                try
                {
                    
                    if (!DictionaryProvider.isLanguageInstalled (lang))
                    {
    
                        downloadFiles.setVisible (true);
    
                        UIUtils.createQuestionPopup (_this.projectViewer,
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
                                                            
                                                            _this.projectViewer.downloadDictionaryFiles (lang,
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
                    
                    UIUtils.showErrorMessage (_this,
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

                _this.projectViewer.showDictionaryManager ();
                
            }

        });

        c = this.createWrapper (b);
        this.setAsMainItem (c);
        
        box.add (c);

        this.setContentBorder (box);
        
        this.accordion.add (this.createHeader (UIUtils.formatForUser ("Editing {Chapters}"),
                                               Chapter.OBJECT_TYPE),
                            null,
                            box,
                            UIUtils.createHelpTextPane ("Everything to do with editing {chapters}.  Manage your personal dictionary, the language for the project, set up auto save and how edit positions behave.  All this and more for the low, low price of a few clicks.",
                                                        this.projectViewer));
                
    }
    
    private void addWhatThingsAreCalledSection ()
    {
        
        final OptionsPanel _this = this;    
    
        final Properties userProps = Environment.getUserProperties ();
    
        Box box = new Box (BoxLayout.Y_AXIS);
        
        final JButton b = UIUtils.createButton ("Change names",
                                                null);
        
        b.addActionListener (new ActionAdapter ()
                             {
                               
                                public void actionPerformed (ActionEvent ev)
                                {
                                    
                                    _this.projectViewer.showObjectTypeNameChanger ();
                                                                        
                                }
                                
                            });

        JComponent c = this.createWrapper (b);

        this.setAsMainItem (c);
        
        box.add (c);

        this.setContentBorder (box);
        
        this.accordion.add (this.createHeader (UIUtils.formatForUser ("What things are called"),
                                               Constants.CONFIG_ICON_NAME),
                            null,
                            box,
                            UIUtils.createHelpTextPane ("Not happy with what things are called?  Want to change {chapter} to <i>sausage</i>?  What are you waiting for crack this section open and get changing.  Yes that's a phrase now.",
                                                        this.projectViewer));
        
    }
    
    private void addHowThingsLookSection ()
    {

        final OptionsPanel _this = this;    
    
        final Properties userProps = Environment.getUserProperties ();
    
        //box.add (this.createHeader ("How things look and sound"));
        //box.add (Box.createVerticalStrut (5));
        
        Box box = new Box (BoxLayout.Y_AXIS);
        
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

        final JCheckBox showPrev = new JCheckBox (Environment.replaceObjectNames ("Show a brief preview of the object when you mouse over its name in the {project} sidebar"));
        showPrev.setSelected (userProps.getPropertyAsBoolean (Constants.SHOW_QUICK_OBJECT_PREVIEW_IN_PROJECT_SIDEBAR_PROPERTY_NAME));
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
        
        box.add (Box.createVerticalStrut (15));

        final JCheckBox showNotesInChapterList = new JCheckBox (Environment.replaceObjectNames ("Show {Notes} in the {Chapter} list"));
        showNotesInChapterList.setSelected (userProps.getPropertyAsBoolean (Constants.SHOW_NOTES_IN_CHAPTER_LIST_PROPERTY_NAME));
        showNotesInChapterList.setOpaque (false);
        
        showNotesInChapterList.addItemListener (new ItemAdapter ()
        {
           
            public void itemStateChanged (ItemEvent ev)
            {
                
                _this.updateUserProperty (Constants.SHOW_NOTES_IN_CHAPTER_LIST_PROPERTY_NAME,
                                          showNotesInChapterList.isSelected ());

                // Redraw the chapter list.
                if (_this.projectViewer instanceof ProjectViewer)
                {
                    
                    ((ProjectViewer) _this.projectViewer).reloadChapterTree ();
                    
                }
                
            }
            
        });
        
        c = this.createWrapper (showNotesInChapterList);
        this.setAsMainItem (c);
        
        box.add (c);
        
        box.add (Box.createVerticalStrut (15));
        
        c = this.createHelpText ("How should the interface be laid out? (Click on the image below to change)");
        this.setAsMainItem (c);
        
        box.add (c);
                
        String selLayout = Environment.getUserProperties ().getProperty (Constants.UI_LAYOUT_PROPERTY_NAME);

        final JLabel layoutSel = new JLabel (new ImageIcon (Environment.getImage (Constants.DATA_DIR + selLayout + ".png")));
        
        UIUtils.setAsButton (layoutSel);
                                
        layoutSel.addMouseListener (new MouseEventHandler ()
        {
           
            @Override
            public void handlePress (MouseEvent ev)
            {
                
                String selLayout = Environment.getUserProperties ().getProperty (Constants.UI_LAYOUT_PROPERTY_NAME);

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
                        
                        String layout = layoutL.getSelectedValue ();
                        
                        layoutSel.setIcon (new ImageIcon (Environment.getImage (Constants.DATA_DIR + layout + ".png")));
                        
                        qp.removeFromParent ();
                        
                        _this.projectViewer.setUILayout (layout);

                        try
                        {
                    
                            Environment.setUserProperty (Constants.UI_LAYOUT_PROPERTY_NAME,
                                                         layout);
                
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to set layout to: " +
                                                  layout,
                                                  e);
                            
                        }        
                        
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
        
                _this.projectViewer.showPopupAt (qp,
                                                 UIUtils.getCenterShowPosition (_this.projectViewer,
                                                                                qp),
                                                 false);
                
                qp.setDraggable (_this.projectViewer);
                
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

        box.add (Box.createVerticalStrut (15));
        
        /*
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
                
                _this.projectViewer.setUILayout (loc);
                
                _this.updateUserProperty (Constants.SIDEBAR_LOCATION_PROPERTY_NAME,
                                          loc);

            }
            
        });        
        
        c = this.createWrapper (sidebarLoc);
        this.setAsSubItem (c);
        
        box.add (c);
        */
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
        f.setMaximumSize (new Dimension (400,
                                         f.getPreferredSize ().height));
        
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
        
        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.LEFT_ALIGNMENT); //ButtonBarFactory.buildLeftAlignedBar (buts);
        bp.setOpaque (false);

        c = this.createWrapper (bp);
        
        box.add (Box.createVerticalStrut (5));

        this.setAsSubItem (c);
        
        box.add (c);
        
        this.setContentBorder (box);
        
        Accordion.Item item = this.accordion.add (this.createHeader (UIUtils.formatForUser ("How things look and sound"),
                                                                     "eye"),
                                                  null,
                                                  box,
                                                  UIUtils.createHelpTextPane ("Want a sound to play whenever a key is pressed?  Want to move the tabs or sidebar around?  Want to show useful tips when Quoll Writer starts?  This is the section for you.",
                                                                              this.projectViewer));

        this.sections.put ("look",
                           item);
                                                        
                                                        
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
    
    private void addProjectAndSnapshotsSection ()
    {

        final OptionsPanel _this = this;
                
        //box.add (this.createHeader (Environment.replaceObjectNames ("{Project} & Snapshots")));
        //box.add (Box.createVerticalStrut (5));

        Box box = new Box (BoxLayout.Y_AXIS);
        
        Vector snapshotA = new Vector ();
        snapshotA.add (Constants.HOURS_12);
        snapshotA.add (Constants.HOURS_24);
        snapshotA.add (Constants.DAYS_2);
        snapshotA.add (Constants.DAYS_5);
        snapshotA.add (Constants.WEEK_1);

        final JComboBox snapshotAmount = new JComboBox (snapshotA);        

        final JCheckBox enableSnapshots = new JCheckBox (Environment.replaceObjectNames ("Automatically create snapshots of the {project}"));
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
        
        box.add (Box.createVerticalStrut (15));
        
        c = this.createHelpText ("Select the directory where your {project} is stored");
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

        this.setContentBorder (box);
                
        this.accordion.add (this.createHeader (Environment.replaceObjectNames ("{Project} & Snapshots"),
                                               Constants.PROJECT_ICON_NAME),
                            null,
                            box,
                            UIUtils.createHelpTextPane ("Click the title above to open this section.  You can then change where your {project} is stored and how often snapshots are taken.",
                                                        this.projectViewer));

    }
    
    private boolean handleProjectDirChange (final FileFinder f)
    {
        
        final OptionsPanel _this = this;
        
        final Project proj = this.projectViewer.getProject ();
        
        final File oldProjDir = proj.getProjectDirectory ();
        
        final File newDir = f.getSelectedFile ();
        
        final File newProjDir = new File (newDir.getPath () + "/" + Utils.sanitizeForFilename (proj.getName ()));

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
            Point p = SwingUtilities.convertPoint (f,
                                                   0,
                                                   0,
                                                   this.projectViewer);

            // Inception!  Don't you just love asynchronous programming!
            ActionListener confirmAction = new ActionListener ()
            {
                           
               public void actionPerformed (ActionEvent ev)
               {

                   _this.projectViewer.close (true,
                                              new ActionListener ()
                   {
   
                       public void actionPerformed (ActionEvent ev)
                       {
               
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
                
                                //f.setFile (oldProjDir);
         
                                UIUtils.showErrorMessage (null,
                                                          "Unable to change project directory, please contact Quoll Writer support for assistance.");                            
                
                                return;
                
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
                
                                //f.setFile (oldProjDir);
                
                                UIUtils.showErrorMessage (null,
                                                          "Unable to change project directory, please contact Quoll Writer support for assistance.");                            
         
                                return;
                
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
                                
                                UIUtils.showErrorMessage (null,
                                                          "Unable to reopen project, please contact Quoll Writer support for assistance.");                            
                                
                                return;
                
                            }
                
                            // Finally, delete the old project directory.
                            Utils.deleteDir (oldDir);
                
                            Environment.getProjectViewer (proj).fireProjectEventLater (proj.getObjectType (),
                                                                                       ProjectEvent.CHANGED_DIRECTORY);
                                                               
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
                                                   
            UIUtils.createQuestionPopup (this.projectViewer,
                                         "Confirm change to {project} directory?",
                                         Project.OBJECT_TYPE,
                                         "Warning!  To change the directory of a {project} it must first be saved and closed.  Once the directory has been changed the {project} will be reopened.<br /><br />Do you wish to continue?",
                                         "Yes, change it",
                                         null,
                                         confirmAction,
                                         onCancel,
                                         onCancel,
                                         p);
            
        }

        return false;
        
    }
    
    private void addItemsAndRulesSection ()
    {

        final OptionsPanel _this = this;
            
        //box.add (this.createHeader ("Manage Items & Rules"));
        //box.add (Box.createVerticalStrut (10));

        Box box = new Box (BoxLayout.Y_AXIS);
        
        JButton[] buts = new JButton[ 3 ];
        
        JButton b = new JButton (Environment.replaceObjectNames ("{Note} Types"));

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
                
                _this.projectViewer.showProblemFinderRuleConfig ();
                
            }
            
        });
        
        buts[2] = b;
        
        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.LEFT_ALIGNMENT);//ButtonBarFactory.buildLeftAlignedBar (buts);
        bp.setOpaque (false);

        JComponent c = this.createWrapper (bp);
        
        this.setAsMainItem (c);
        
        box.add (c);

        this.setContentBorder (box);
        
        this.accordion.add (this.createHeader (UIUtils.formatForUser ("Manage Items & Rules"),
                                               Constants.EDIT_ICON_NAME),
                            null,
                            box,
                            UIUtils.createHelpTextPane ("Set up the problem finder rules and manage the note and item types.  A dull description but it does exactly what it says on the tin, well screen.",
                                                        this.projectViewer));
                
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
                                                  this.projectViewer);
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

    public void setState (final Map<String, String> s,
                          boolean                   hasFocus)
    {

        final OptionsPanel _this = this;
    
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

                _this.setReadyForUse (true);
                
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