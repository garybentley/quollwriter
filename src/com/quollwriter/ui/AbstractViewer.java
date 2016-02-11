package com.quollwriter.ui;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;

import java.io.*;

import java.net.*;

import java.security.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.WeakHashMap;
import java.util.Collections;
import java.util.Stack;
import java.util.SortedSet;
import java.util.TimerTask;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.sound.sampled.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.text.*;

import com.gentlyweb.properties.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.db.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.events.*;
import com.quollwriter.synonyms.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.renderers.*;

import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.ui.sidebars.*;

import com.quollwriter.achievements.rules.*;
import com.quollwriter.achievements.ui.*;

public abstract class AbstractViewer extends JFrame implements PopupsSupported,
                                                               SideBarsSupported,
                                                               HTMLPanelActionHandler
{
    
    public static final int NEW_PROJECT_ACTION = 1;
    public static final int ABOUT_ACTION = 9; // "about";
    public static final int REPORT_BUG_ACTION = 10; // "reportBug";
    public static final int CLOSE_PROJECT_ACTION = 12; // "closeProject";
    public static final int DELETE_PROJECT_ACTION = 13; // "deleteProject";
    public static final int WARMUP_EXERCISE_ACTION = 26;
    public static final int CONTACT_SUPPORT_ACTION = 28;

    public static final String REPORT_BUG_HEADER_CONTROL_ID = "reportBug";
    public static final String CONTACTS_HEADER_CONTROL_ID = "contacts";
    public static final String SETTINGS_HEADER_CONTROL_ID = "settings";
    public static final String DO_WARMUP_HEADER_CONTROL_ID = "doWarmup";
    public static final String REPORT_BETA_BUG_HEADER_CONTROL_ID = "reportBetaBug";
    
    private Header                title = null;
    private DropTarget titleDT = null;
    private Box                   content = null;
    private Box                   notifications = null;
    private QPopup                achievementsPopup = null;
    private Map<String, QPopup> popups = new HashMap ();

    private java.util.Timer generalTimer = null;
    
    private Timer achievementsHideTimer = null;

    private Tips tips = null;
                
    private Map tempOptions = new HashMap ();                
                
    private Set<ProjectEventListener> projectEventListeners = new HashSet ();
    private boolean ignoreProjectEvents = false;                
                
    public AbstractViewer()
    {

        final AbstractViewer _this = this;

        this.generalTimer = new java.util.Timer (true);
        
        this.addWindowListener (new WindowAdapter ()
            {

                public void windowClosing (WindowEvent ev)
                {

                    _this.close (false,
                                 null);

                }

            });
                    
        this.getContentPane ().setBackground (UIUtils.getComponentColor ());
            
        this.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
                                                                
        Box b = new Box (BoxLayout.Y_AXIS);

        this.title = new Header ();
        this.title.setAlignmentX (Component.LEFT_ALIGNMENT);

        // new
        Header h = this.title;
        h.setFont (h.getFont ().deriveFont ((float) UIUtils.getScaledFontSize (22)).deriveFont (Font.PLAIN));
        h.setPaintProvider (null);
        h.setTitleColor (UIUtils.getTitleColor ());
        h.setIcon (null);
        h.setPadding (new Insets (3, 3, 3, 7));
                
        Box tb = new Box (BoxLayout.X_AXIS);
        tb.setAlignmentX (Component.LEFT_ALIGNMENT);
        tb.add (this.title);

        tb.setBorder (new CompoundBorder (new MatteBorder (0,
                                                           0,
                                                           1,
                                                           0,
                                                           Environment.getBorderColor ()),
                                          new EmptyBorder (0,
                                                           5,
                                                           0,
                                                           0)
                                          )); 
                                                           
        b.add (tb);

        // Create the "notifications" area.
        this.notifications = new Box (BoxLayout.Y_AXIS);

        this.notifications.setBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getBorderColor ()));

        this.notifications.setVisible (false);
        b.add (this.notifications);

        this.content = new Box (BoxLayout.Y_AXIS);
        
        b.add (this.content);

        try
        {
            
            this.tips = new Tips (this);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to init tips",
                                  e);
            
        }
                                        
        this.getContentPane ().add (b);

    }

    public void init ()
               throws Exception
    {
        
        final AbstractViewer _this = this;
        
        InputMap im = this.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F12,
                                        InputEvent.CTRL_MASK | InputEvent.ALT_MASK),
                "debug");
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F1,
                                        InputEvent.CTRL_MASK | InputEvent.ALT_MASK),
                "debug-mode");

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F11,
                                        0),
                "whatsnew");   
                
        ActionMap am = this.getActionMap ();
                
        this.initKeyMappings (im);
        
        this.initActionMappings (am);
        
        final JToolBar titleC = UIUtils.createButtonBar (new ArrayList ());

        Set<String> tids = this.getTitleHeaderControlIds ();
        
        if (tids == null)
        {
            
            tids = new LinkedHashSet ();
            
        }

        if (Environment.getQuollWriterVersion ().isBeta ())
        {
                        
            Set<String> ntids = new LinkedHashSet ();
            
            ntids.add (REPORT_BETA_BUG_HEADER_CONTROL_ID);
            
            ntids.addAll (tids);
            
            tids = ntids;

        }
        
        final Set<String> ftids = tids;
        
        for (String s : tids)
        {
            
            JComponent c = this.getTitleHeaderControl (s);
            
            if (c != null)
            {
                
                c.setName (s);
                /*
                c.addMouseListener (new MouseEventHandler ()
                {

                    public void fillPopup (JPopupMenu m,
                                           MouseEvent ev)
                    {
                        
                        //titleC.setEditable (true);
                                                
                        final QPopup ep = UIUtils.createClosablePopup ("Configure",
                                                                       null,
                                                                       null);
                
                        Box b = new Box (BoxLayout.Y_AXIS);
                        
                        b.setBorder (UIUtils.createPadding (10, 10, 10, 10));
                        
                        final DraggableToolbar tb = new DraggableToolbar ();//UIUtils.createButtonBar (new ArrayList ());
                        //tb.setConnectTo (titleC);
                        //titleC.setConnectTo (tb);
                        tb.setMinimumSize (new Dimension (250, 30));
                        tb.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                          Short.MAX_VALUE));
                        tb.setEditable (true);
                        tb.setBorder (UIUtils.createTestLineBorder ());
                        
                        b.add (tb);
                                                
                        for (String s : ftids)
                        {
                            
                            JComponent c = _this.getTitleHeaderControl (s);
                            
                            if (c != null)
                            {
                                
                                c.setName (s);
                        
                                tb.add (c);
                                
                            }
                            
                        }
                        
                        ep.setContent (b);

                        _this.showPopupAt (ep,
                                           titleC,
                                           true);
                        
                    }

                });
                */
                
                titleC.add (c);
                
            }

        }
                     
        this.title.setControls (titleC);
                
        this.setIconImage (Environment.getWindowIcon ().getImage ());
                
        this.updateForDebugMode ();        
                
    }
    
    public void setTitleHeaderControlsVisible (boolean v)
    {
        
        this.title.getControls ().setVisible (v);
        
        this.validate ();
        this.repaint ();
        
    }
    
    public void showViewer ()
    {
        
        this.pack ();

        // Allow the underlying Windowing manager determine where to put the window.
        this.setLocationByPlatform (true);

        this.setVisible (true);

        Environment.doNewsAndVersionCheck (this);

        this.handleWhatsNew ();
        
        this.handleShowTips ();                
        
    }
    
    public JComponent getTitleHeaderControl (String id)
    {
        
        if (id == null)
        {
            
            return null;
            
        }
        
        final AbstractViewer _this = this;
        
        JComponent c = null;
        
        if (id.equals (DO_WARMUP_HEADER_CONTROL_ID))
        {
            
            c = UIUtils.createButton (Constants.WARMUPS_ICON_NAME,
                                      Constants.ICON_TITLE_ACTION,
                                      "Click to do a new {Warmup} exercise",
                                      new ActionAdapter ()
                                      {
                                                
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                    
                                                _this.showWarmupPromptSelect ();
                                                    
                                            }
                                                
                                      });                        
            
        }
        
        if (id.equals (REPORT_BUG_HEADER_CONTROL_ID))
        {
            
            c = UIUtils.createButton (Constants.BUG_ICON_NAME,
                                              Constants.ICON_TITLE_ACTION,
                                              "Click to report a bug/problem",
                                              new ActionAdapter ()
                                              {
                                                
                                                  public void actionPerformed (ActionEvent ev)
                                                  {
                                    
                                                      _this.showReportProblem ();
                                                    
                                                  }
                                                
                                              });                        
            
        }
        
        if (id.equals (REPORT_BETA_BUG_HEADER_CONTROL_ID))
        {
            
            c = UIUtils.createButton (Constants.BUG_ICON_NAME,
                                              Constants.ICON_TITLE_ACTION,
                                              "Click to report a bug/problem with the beta",
                                              new ActionAdapter ()
                                              {
                                                
                                                  public void actionPerformed (ActionEvent ev)
                                                  {
                                    
                                                      _this.showReportProblem ();
                                                    
                                                  }
                                                
                                              });                        
            
        }

        if (id.equals (CONTACTS_HEADER_CONTROL_ID))
        {
            
            if (EditorsEnvironment.isEditorsServiceAvailable ())
            {
            
                String toolTip = (EditorsEnvironment.hasRegistered () ? "Click to show my {contacts}" : "Click to register for the Editors Service.");
            
                c = UIUtils.createButton (Constants.EDITORS_ICON_NAME,
                                             Constants.ICON_TITLE_ACTION,
                                             toolTip,
                                             new ActionAdapter ()
                                             {
                                                 
                                                 public void actionPerformed (ActionEvent ev)
                                                 {
                                                     
                                                     if ((_this.isEditorsVisible ())
                                                         &&
                                                         (!EditorsEnvironment.isUserLoggedIn ())
                                                        )
                                                     {
                     
                                                         EditorsEnvironment.goOnline (null,
                                                                                      null,
                                                                                      null,
                                                                                      null);
                                                                                                                     
                                                         return;
                                                         
                                                     }
                                                     
                                                     try
                                                     {
                                                     
                                                         _this.viewEditors ();
                                                         
                                                     } catch (Exception e) {
                                                         
                                                         Environment.logError ("Unable to view editors",
                                                                               e);
                                                         
                                                         UIUtils.showErrorMessage (_this,
                                                                                   "Unable to show the {editors}.");
                                                         
                                                     }

                                                 }
                                                 
                                             });

            }
                                             
        }
        
        if (id.equals (SETTINGS_HEADER_CONTROL_ID))
        {

            c = UIUtils.createButton (Constants.SETTINGS_ICON_NAME,
                                           Constants.ICON_TITLE_ACTION,
                                           "Click to view the {Project} menu",
                                           new ActionAdapter ()
                                           {
                                            
                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    final JPopupMenu titlePopup = new JPopupMenu ();
                                    
                                                    try
                                                    {
                                    
                                                        _this.fillSettingsPopup (titlePopup);
                                    
                                                    } catch (Exception e)
                                                    {
                                    
                                                        Environment.logError ("Unable to fill popup",
                                                                              e);
                                    
                                                    }
                                    
                                                    titlePopup.addSeparator ();
                                    
                                                    JMenuItem mi = null;
/*                                    
                                                    titlePopup.add (_this.createMenuItem ("Find",
                                                                                          Constants.FIND_ICON_NAME,
                                                                                          new ActionAdapter ()
                                                                                          {
                                                                                
                                                                                            public void actionPerformed (ActionEvent ev)
                                                                                            {
                                                                                
                                                                                                _this.showFind (null);
                                                                                
                                                                                            }
                                                                                
                                                                                         }));
  */                                  
                                                    titlePopup.add (_this.createMenuItem ("Options",
                                                                                          Constants.OPTIONS_ICON_NAME,
                                                                                          new ActionAdapter ()
                                                                                          {
                                                                                            
                                                                                              @Override
                                                                                              public void actionPerformed (ActionEvent ev)
                                                                                              {
                                                                                                
                                                                                                  _this.showOptions (null);
                                                                                                
                                                                                              }
                                                                                            
                                                                                          }));
                                                    
                                                    titlePopup.add (_this.createMenuItem ("Achievements",
                                                                                          Constants.ACHIEVEMENT_ICON_NAME,
                                                                                          new ActionAdapter ()
                                                                                          {
                                                                                                
                                                                                            public void actionPerformed (ActionEvent ev)
                                                                                            {
                                                                                                
                                                                                                _this.viewAchievements ();
                                                                                                
                                                                                            }
                                                                                            
                                                                                         }));
                                    
                                                    titlePopup.addSeparator ();

                                                    titlePopup.add (_this.createMenuItem ("What's New in this version",
                                                                                          Constants.WHATS_NEW_ICON_NAME,
                                                                                          new ActionAdapter ()
                                                                                          {
                                                                                                
                                                                                            public void actionPerformed (ActionEvent ev)
                                                                                            {
                                                                                                
                                                                                                _this.showWhatsNew (true);
                                                                                                
                                                                                            }
                                                                                            
                                                                                         }));

                                                    // Help
                                                    JMenu m = new JMenu ("Help");
                                                    m.setIcon (Environment.getIcon (Constants.HELP_ICON_NAME,
                                                                                    Constants.ICON_MENU));
                                    
                                                    titlePopup.add (m);
                                                        
                                                    // Report Bug/Problem
                                                    m.add (_this.createMenuItem ("Report Bug/Problem",
                                                                                 Constants.BUG_ICON_NAME,
                                                                                 AbstractProjectViewer.REPORT_BUG_ACTION));
                                    
                                                    // Contact Support
                                                    m.add (_this.createMenuItem ("Contact Support",
                                                                                 Constants.EMAIL_ICON_NAME,
                                                                                 AbstractProjectViewer.CONTACT_SUPPORT_ACTION));                                                                                 
                                                                                 
                                                    // View the User Guide
                                                    m.add (_this.createMenuItem ("View the User Guide",
                                                                                 Constants.HELP_ICON_NAME,
                                                                                 new ActionAdapter ()
                                                                                 {
                                                        
                                                                                    public void actionPerformed (ActionEvent ev)
                                                                                    {
                                                        
                                                                                        UIUtils.openURL (_this,
                                                                                                         "help:index");
                                                        
                                                                                    }
                                                        
                                                                                 }));
                                    
                                                    m.add (_this.createMenuItem ("Keyboard Shortcuts",
                                                                                 null,
                                                                                 new ActionAdapter ()
                                                                                 {
                                                        
                                                                                    public void actionPerformed (ActionEvent ev)
                                                                                    {
                                                        
                                                                                        UIUtils.openURL (_this,
                                                                                                         "help:main-window/keyboard-shortcuts");
                                                        
                                                                                    }
                                                        
                                                                                 }));
                                    
                                                    // About Quoll Writer
                                                    titlePopup.add (_this.createMenuItem ("About Quoll Writer",
                                                                                          Constants.ABOUT_ICON_NAME,
                                                                                          AbstractProjectViewer.ABOUT_ACTION));
                                    
                                                    if (Environment.isDebugModeEnabled ())
                                                    {
                                    
                                                        // Debug Console
                                                        titlePopup.add (_this.createMenuItem ("Debug Console",
                                                                                              Constants.CONSOLE_ICON_NAME,
                                                                                              new ActionAdapter ()
                                                                                              {
                                                                    
                                                                                                    public void actionPerformed (ActionEvent ev)
                                                                                                    {
                                                                    
                                                                                                        new DebugConsole (_this);
                                                                    
                                                                                                    }
                                                                    
                                                                                              }));
                                    
                                                    }
                                    
                                                    JComponent s = (JComponent) ev.getSource ();
                                    
                                                    titlePopup.show (s,
                                                                     s.getWidth () / 2,
                                                                     s.getHeight ());

                                                    
                                                }
                                        
                                           });
                        
        }
                
        return c;
                
    }
        
    public abstract boolean showOptions (String section);
    
    public abstract Set<String> getTitleHeaderControlIds ();
        
    public abstract JPopupMenu getShowOtherSideBarsPopupSelector ();

    public abstract int getActiveSideBarCount ();
    
    public abstract AbstractSideBar getActiveOtherSideBar ();
    
    public abstract void addSideBarListener (SideBarListener l);
    
    public abstract void removeSideBarListener (SideBarListener l);
    
    public abstract void closeSideBar ();
    
    public abstract boolean viewAchievements ();             
             
    public abstract boolean isEditorsVisible ();
                    
    public abstract void viewEditor (EditorEditor ed)
                              throws GeneralException; 

    public abstract boolean viewEditors ()
                                  throws GeneralException;    

    public abstract void sendMessageToEditor (EditorEditor ed)
                                       throws GeneralException;    
    
    public void setContent (JComponent content)
    {
        
        content.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        this.content.add (content);
        
    }
    
    public void showContactSupport ()
    {
        
        final AbstractViewer _this = this;
        
        String popupName = "contactsupport";
        QPopup popup = this.getNamedPopup (popupName);
        
        if (popup == null)
        {
        
            popup = UIUtils.createClosablePopup ("Contact Support",
                                                 Environment.getIcon (Constants.EMAIL_ICON_NAME,
                                                                      Constants.ICON_POPUP),
                                                 null);
        
            popup.setPopupName (popupName);
            
            this.addNamedPopup (popupName,
                                popup);
        
            Box content = new Box (BoxLayout.Y_AXIS);
        
            JTextPane help = UIUtils.createHelpTextPane ("Use the form below to contact Quoll Writer support.  If you wish to receive a response then please provide an email address.", 
                                                         this);
    
            help.setBorder (null);
                
            content.add (help);
            content.add (Box.createVerticalStrut (10));
                  
            final JLabel error = UIUtils.createErrorLabel ("Please enter a message.");
            error.setVisible (false);
            error.setBorder (UIUtils.createPadding (0, 5, 5, 5));
            
            content.add (error);
                  
            final TextArea desc = new TextArea ("Enter your message here.",
                                                5,
                                                10000);
            
            FormLayout fl = new FormLayout ("10px, right:p, 6px, fill:200px:grow, 10px",
                                            "top:p, 6px, p, 6px, p");
                                            
            PanelBuilder builder = new PanelBuilder (fl);
    
            CellConstraints cc = new CellConstraints ();
            
            builder.addLabel ("Message",
                              cc.xy (2,
                                     1));
                    
            builder.add (desc, 
                         cc.xy (4,
                                1));
                            
            builder.addLabel ("Your Email",
                              cc.xy (2,
                                     3));
    
            final QPopup qp = popup;
    
            final JTextField email = new JTextField ();
            
            builder.add (email,
                         cc.xy (4, 3));
                                                                       
            ActionListener sendAction = new ActionListener ()
            {
    
                public void actionPerformed (ActionEvent ev)
                {
    
                    error.setVisible (false);
                
                    String emErr = Utils.checkEmail (email.getText ());
                    
                    if (emErr != null)
                    {
                        
                        error.setText (emErr);
                        
                        error.setVisible (true);
                        
                        qp.resize ();
                        
                        return;
                        
                    }
                
                    if (desc.getText ().trim ().equals (""))
                    {
    
                        error.setText ("Please enter a message.");
                        error.setVisible (true);
                        
                        qp.resize ();
                    
                        return;
    
                    }
    
                    qp.resize ();
                    
                    // Send the message.
                    Map details = new HashMap ();
                    details.put ("details",
                                 "Email: " + email.getText () + "\nDetails: " + desc.getText ());
                    details.put ("email",
                                 email.getText ());
    
                    try
                    {
    
                        Environment.sendMessageToSupport ("contact",
                                                          details,
                                                          new ActionAdapter ()
                        {
    
                            public void actionPerformed (ActionEvent ev)
                            {
                        
                                UIUtils.showMessage ((PopupsSupported) _this,
                                                     "Message sent",
                                                     "Your request has been logged with Quoll Writer support.  If you provided an email address then you should get a response within 1-2 days.  If not feel then free to send the message again.");
    
                                _this.fireProjectEvent (ProjectEvent.CONTACT,
                                                        ProjectEvent.SUBMIT);
    
                            }
                            
                        });                        
                                                                                          
                    } catch (Exception e)
                    {
    
                        Environment.logError ("Unable to send message to support",
                                              e);
    
                        UIUtils.showErrorMessage (_this,
                                                  "Unable to send message.");
    
                    }
    
                    qp.removeFromParent ();
    
                }
    
            };
    
            UIUtils.addDoActionOnReturnPressed (desc,
                                                sendAction);        
            UIUtils.addDoActionOnReturnPressed (email,
                                                sendAction);        
            
            JButton send = new JButton ("Send");
            JButton cancel = new JButton ("Cancel");
    
            send.addActionListener (sendAction);
    
            cancel.addActionListener (new ActionAdapter ()
            {
    
                public void actionPerformed (ActionEvent ev)
                {
    
                    qp.removeFromParent ();
    
                }
    
            });
    
            JButton[] buts = { send, cancel };
    
            JPanel bp = UIUtils.createButtonBar2 (buts,
                                                  Component.LEFT_ALIGNMENT); 
            bp.setOpaque (false);
    
            builder.add (bp,
                         cc.xy (4, 5));        
            
            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
    
            content.add (p);
            
            content.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                             content.getPreferredSize ().height));
            content.setBorder (UIUtils.createPadding (10, 10, 10, 10));        
        
            popup.setContent (content);
            
            popup.setDraggable (this);
                              
            popup.resize ();
            this.showPopupAt (popup,
                              UIUtils.getCenterShowPosition (this,
                                                             popup),
                              false);

        } else {
            
            popup.setVisible (true);
            
                
        }
    }
    
    public void showReportProblem ()
    {
        
        final AbstractViewer _this = this;
        
        String popupName = "bugreport";
        QPopup popup = this.getNamedPopup (popupName);
        
        if (popup == null)
        {
                
            popup = UIUtils.createClosablePopup ("Report a Bug/Problem",
                                                 Environment.getIcon (Constants.BUG_ICON_NAME,
                                                                      Constants.ICON_POPUP),
                                                 null);
              
            Box content = new Box (BoxLayout.Y_AXIS);
            
            JTextPane help = UIUtils.createHelpTextPane ("Complete the form below to report a bug/problem.  The email address is optional, only provide it if you would like a response.<br /><br />The operating system you are using and the Java version will also be sent (it helps with debugging).  No personal information will be sent.", 
                                                         this);
    
            help.setBorder (null);
                
            content.add (help);
            content.add (Box.createVerticalStrut (10));
                  
            final JLabel error = UIUtils.createErrorLabel ("Please enter a description.");
            error.setVisible (false);
            error.setBorder (UIUtils.createPadding (0, 5, 5, 5));
            
            content.add (error);
                  
            final TextArea desc = new TextArea ("Enter the bug/problem description here.  More information is usually better.",
                                                5,
                                                10000);
            
            FormLayout fl = new FormLayout ("10px, right:p, 6px, fill:200px:grow, 10px",
                                            "top:p, 6px, p, 6px, p, 6px, p");
                                            
            PanelBuilder builder = new PanelBuilder (fl);
    
            CellConstraints cc = new CellConstraints ();
            
            builder.addLabel ("Description",
                              cc.xy (2,
                                     1));
                                                                     
            builder.add (desc,
                         cc.xy (4,
                                1));
                            
            builder.addLabel ("Your Email",
                              cc.xy (2,
                                     3));
    
            final JTextField email = new JTextField ();
            
            builder.add (email,
                         cc.xy (4, 3));
                                                               
            final JCheckBox sendLogFiles = new JCheckBox ("Send the log files");
    
            if (Environment.getQuollWriterVersion ().isBeta ())
            {
                
                sendLogFiles.setEnabled (false);
                sendLogFiles.setToolTipText ("Log files are always sent for beta versions, it really helps with debugging.");
                
            }
    
            sendLogFiles.setSelected (true);
            sendLogFiles.setOpaque (false);
            sendLogFiles.setAlignmentX (java.awt.Component.LEFT_ALIGNMENT);
    
            builder.add (sendLogFiles,
                         cc.xy (4,
                                5));        
            
            final QPopup qp = popup;
            
            ActionListener sendAction = new ActionListener ()
            {
    
                public void actionPerformed (ActionEvent ev)
                {
    
                    error.setVisible (false);
                
                    String emErr = Utils.checkEmail (email.getText ());
                    
                    if (emErr != null)
                    {
                        
                        error.setText (emErr);
                        
                        error.setVisible (true);
                        
                        qp.resize ();
                        
                        return;
                        
                    }
                
                    if (desc.getText ().trim ().equals (""))
                    {
    
                        error.setText ("Please enter a description of the problem/bug.");
                        error.setVisible (true);
                        
                        qp.resize ();
                    
                        return;
    
                    }
    
                    qp.resize ();
                    
                    StringBuilder dets = new StringBuilder ("Email: " + email.getText () + "\nDetails: " + desc.getText ());
                    
                    // TODO: Fix this, have a toString on project viewer instead.
                    if (_this instanceof AbstractProjectViewer)
                    {
                        
                        Project proj = ((AbstractProjectViewer) _this).getProject ();
                        
                        dets.append ("\nCurrent project id: " + proj.getId ());
                        
                    }
                    
                    // Send the message.
                    Map details = new HashMap ();
                    details.put ("details",
                                 dets.toString ());

                    details.put ("email",
                                 email.getText ());
    
                    try
                    {
    
                        // Get the log files?
                        if (sendLogFiles.isSelected ())
                        {
    
                            details.put ("errorLog",
                                         IOUtils.getFile (Environment.getErrorLogFile ()));
                            details.put ("generalLog",
                                         IOUtils.getFile (Environment.getGeneralLogFile ()));
                            details.put ("editorsMessageLog",
                                         IOUtils.getFile (EditorsEnvironment.getEditorsMessageLogFile ()));
    
                        }
    
                        Environment.sendMessageToSupport ("bug",
                                                          details,
                                                          new ActionAdapter ()
                        {
    
                            public void actionPerformed (ActionEvent ev)
                            {
                        
                                UIUtils.showMessage ((PopupsSupported) _this,
                                                     "Problem/Bug reported",
                                                     "Thank you, the problem has been logged with Quoll Writer support.  If you provided an email address then you should get a response within 1-2 days.  If not feel then free to send the message again.");
        
                                _this.fireProjectEvent (ProjectEvent.BUG_REPORT,
                                                        ProjectEvent.SUBMIT);
    
                            }
                            
                        });
                                                                      
                    } catch (Exception e)
                    {
    
                        Environment.logError ("Unable to send message to support",
                                              e);
    
                        UIUtils.showErrorMessage (_this,
                                                  "Unable to send message.");
    
                    }
    
                    qp.removeFromParent ();
    
                }
    
            };
    
            UIUtils.addDoActionOnReturnPressed (desc,
                                                sendAction);        
            UIUtils.addDoActionOnReturnPressed (email,
                                                sendAction);        
            
            JButton send = new JButton ("Send");
            JButton cancel = new JButton ("Cancel");
    
            send.addActionListener (sendAction);
    
            cancel.addActionListener (new ActionAdapter ()
            {
    
                public void actionPerformed (ActionEvent ev)
                {
    
                    qp.removeFromParent ();
    
                }
    
            });
    
            JButton[] buts = { send, cancel };
    
            JPanel bp = UIUtils.createButtonBar2 (buts,
                                                  Component.LEFT_ALIGNMENT); 
            bp.setOpaque (false);
    
            builder.add (bp,
                         cc.xy (4, 7));        
            
            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
    
            content.add (p);
            
            content.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                             content.getPreferredSize ().height));
            content.setBorder (UIUtils.createPadding (10, 10, 10, 10));        
            
            popup.setContent (content);
            
            popup.setDraggable (this);
                              
            popup.setPopupName (popupName);
            
            this.addNamedPopup (popupName,
                                popup);
                              
            popup.resize ();
            this.showPopupAt (popup,
                              UIUtils.getCenterShowPosition (this,
                                                             popup),
                              false);
            
        } else {
            
            popup.setVisible (true);
            
        }
        
    }
            
    public void initActionMappings (ActionMap am)
    {
        
        final AbstractViewer _this = this;
        
        am.put ("do-warmup",
                new ActionAdapter ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                    
                        _this.getAction (AbstractViewer.WARMUP_EXERCISE_ACTION).actionPerformed (ev);
        
                    }
        
                });        
                
        am.put ("debug",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        new DebugConsole (_this);

                    }

                });

        am.put ("debug-mode",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        Environment.setDebugModeEnabled (!Environment.isDebugModeEnabled ());                    
                                        
                        _this.updateForDebugMode ();

                        // Add a notification.
                        _this.addNotification (String.format ("Debug mode is now <b>%s</b>",
                                                              (Environment.isDebugModeEnabled () ? "ENabled" : "DISabled")),
                                               Constants.BUG_ICON_NAME,
                                               10);
                                            
                    }

                });

        am.put ("whatsnew",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        try
                        {
                    
                            Environment.setUserProperty (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME,
                                                         new StringProperty (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME,
                                                                             "0"));
                
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to set the whats new version viewed property",
                                                  e);
                            
                        }
                    
                        _this.showWhatsNew (true);

                    }

                });
                
        am.put ("contact",
                new ActionAdapter ()
                {
                   
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.showContactSupport ();
                        
                    }
                    
                });
                
        am.put ("editobjectnames",
                new ActionAdapter ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.showObjectTypeNameChanger ();
                        
                    }
                    
                });
                        
        am.put ("vieweditors",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        try
                        {
                    
                            _this.viewEditors ();
                            
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to show editors",
                                                  e);
                            
                            UIUtils.showErrorMessage (_this,
                                                      "Unable to show editors.");
                            
                        }
                    
                    }

                });                        

    }
    
    private void updateForDebugMode ()
    {
                
        String iconName = this.getViewerIcon ();
                                                                      
        if (Environment.isDebugModeEnabled ())
        {                                                                                       
        
            iconName = Constants.BUG_ICON_NAME;

        }
        
        this.title.setIcon (Environment.getIcon (iconName,
                                                 Constants.ICON_TITLE));
        
    }
    
    public void initKeyMappings (InputMap im)
    {
        
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F7,
                                        0),
                "vieweditors");                
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F10,
                                        0),
                "do-warmup");
                
    }
        
    protected JMenuItem createMenuItem (String label,
                                        String icon,
                                        int    action)
    {
        
        return UIUtils.createMenuItem (label,
                                       icon,
                                       this.getAction (action));
        
    }

    protected JMenuItem createMenuItem (String label,
                                        String icon,
                                        ActionListener action)
    {
        
        return UIUtils.createMenuItem (label,
                                       icon,
                                       action);
        
    }
        
    public Map getTempOptions ()
    {
        
        return this.tempOptions;
        
    }

    public boolean hasTempOption (String name)
    {
        
        return this.getTempOption (name) != null;
        
    }
    
    public boolean isTempOption (String name)
    {
        
        Object o = this.getTempOption (name);
        
        if (o == null)
        {
            
            return false;
            
        }
        
        if (o instanceof Boolean)
        {
            
            return ((Boolean) o).booleanValue ();
            
        }
        
        return false;
        
    }

    public Object getTempOption (String name)
    {
        
        return this.tempOptions.get (name);
        
    }

    public void setTempOption (String name,
                               Object value)
    {
        
        this.tempOptions.put (name,
                              value);
        
    }

    public abstract void showHelpText (String title,
                                       String text,
                                       String iconType,
                                       String helpTextId);
                
    //public abstract String getViewerTitle ();

    public abstract String getViewerIcon ();
    
    //public abstract void fillTitleToolbar (JToolBar toolbar);
    
    public abstract void fillSettingsPopup (JPopupMenu popup);

    public abstract void doSaveState ();
        
    public ActionMap getActionMap ()
    {

        return this.content.getActionMap ();

    }

    public InputMap getInputMap (int m)
    {

        return this.content.getInputMap (m);

    }

    public Notification addNotification (JComponent comp,
                                         String     iconType,
                                         int        duration)
    {
        
        return this.addNotification (comp,
                                     iconType,
                                     duration,
                                     null);
        
    }

    /**
     * Adds a notification to the notification area, the action listener can be used
     * to remove the notification, it can be safely called with a null event.
     *
     * @param comp The component to add to the notification.
     * @param iconType The type of notification, supported values are "information" and "notify".
     * @param duration The time, in seconds, that the notification should be shown for, if less than 1 then
     *                 the notification won't be auto removed.
     * @return An action listener that can be called to remove the notification.
     */
    public Notification addNotification (JComponent comp,
                                         String     iconType,
                                         int        duration,
                                         java.util.List<JButton> buttons)
    {

        final AbstractViewer _this = this;
    
        Notification n = new Notification (comp,
                                           iconType,
                                           duration,
                                           buttons,
                                           new ActionListener ()
                                           {
                                            
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    Notification n = (Notification) ev.getSource ();
                                                    
                                                    _this.removeNotification (n);
                                                    
                                                }
                                            
                                           });

        this.addNotification (n);

        return n;

    }

    public void removeAllNotifications ()
    {
        
        for (int i = 0; i < this.notifications.getComponentCount (); i++)
        {
            
            Notification n = (Notification) this.notifications.getComponent (i);
            
            this.removeNotification (n);
            
        }        
        
    }
    
    public void removeNotification (Notification n)
    {

        // Remove the box.
        this.notifications.remove (n);

        if (this.notifications.getComponentCount () == 0)
        {

            this.notifications.setVisible (false);

        } else {
            
            int c = this.notifications.getComponentCount ();

            JComponent jc = (JComponent) this.notifications.getComponent (c - 1);
            
            Border b = jc.getBorder ();
            
            // Eek, not good but ok for now.
            // TODO: Fix this nasty.
            if (b instanceof CompoundBorder)
            {
                
                CompoundBorder cb = (CompoundBorder) b;
                
                jc.setBorder (cb.getInsideBorder ());
                
            }

        }

        this.notifications.getParent ().validate ();
        this.notifications.getParent ().repaint ();                

    }

    public void addNotification (Notification n)
    {
        
        if (this.notifications.getComponentCount () > 0)
        {
                        
            n.setBorder (new CompoundBorder (UIUtils.createBottomLineWithPadding (0, 0, 1, 0),
                                             n.getBorder ()));
            
        }
        
        this.notifications.add (n,
                                0);

        this.notifications.setVisible (true);

        n.init ();
        
        this.notifications.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                          this.notifications.getPreferredSize ().height));
        
        this.validate ();
        this.repaint ();        
        
    }
    
    public Notification addNotification (String text,
                                         String type,
                                         int    duration)
    {

        return this.addNotification (text,
                                     type,
                                     duration,
                                     null);
    
    }
    
    public Notification addNotification (String            text,
                                         String            type,
                                         int               duration,
                                         HyperlinkListener clickListener)
    {

        JTextPane p = UIUtils.createHelpTextPane (text,
                                                  this);

        p.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         Short.MAX_VALUE));
        p.setBorder (null);

        if (clickListener != null)
        {
            
            p.addHyperlinkListener (clickListener);
            
        }
                
        return this.addNotification (p,
                                     type,
                                     duration);

    }

    public Action getAction (int name)
    {

        final AbstractViewer _this = this;
        
        if (name == AbstractViewer.WARMUP_EXERCISE_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (final ActionEvent ev)
                {

                    _this.showWarmupPromptSelect ();

                }

            };

        }

        if (name == AbstractViewer.REPORT_BUG_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.showReportProblem ();

                }

            };

        }

        if (name == AbstractViewer.CONTACT_SUPPORT_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.showContactSupport ();

                }

            };

        }

        if (name == AbstractViewer.ABOUT_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.showAbout ();

                }

            };

        }

        return null;

    }

    public void showWarmupPromptSelect ()
    {
        
        final QPopup qp = UIUtils.createClosablePopup ("Do a {Warmup} Exercise",
                                                       Environment.getIcon (Warmup.OBJECT_TYPE,
                                                                            Constants.ICON_POPUP),
                                                       null);
    
        WarmupPromptSelect w = new WarmupPromptSelect (this);

        w.init ();

        w.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                  qp.getPreferredSize ().height));
                                  
        w.setBorder (UIUtils.createPadding (10, 10, 10, 10));        
        
        qp.setContent (w);
        
        qp.setDraggable (this);

        qp.resize ();
        
        this.addNamedPopup ("warmup",
                            qp);
        
        this.showPopupAt (qp,
                          UIUtils.getCenterShowPosition (this,
                                                         qp),
                           false);

        UIUtils.doLater (new ActionListener ()
        {                           
        
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                qp.resize ();
                
            }
            
        });
                              
    }
        
    public void setViewerTitle (String t)
    {
                
        t = Environment.replaceObjectNames (t);
                
        this.title.setTitle (t);
        
        UIUtils.setFrameTitle (this,
                               t);        
        
    }
                    
    private void handleWhatsNew ()
    {
        
        boolean showWhatsNew = false;

        String whatsNewVersion = Environment.getProperty (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME);
    
        if (whatsNewVersion != null)
        {

            Version lastViewed = new Version (whatsNewVersion);

            if (lastViewed.isNewer (Environment.getQuollWriterVersion ()))
            {
        
                showWhatsNew = true;
                
            }
            
        } else {
            
            showWhatsNew = true;
            
        }

        if (showWhatsNew)
        {
            
            this.showWhatsNew (false);
            
        }        
        
    }
    
    public void addNamedPopup (String name,
                               QPopup popup)
    {
        
        QPopup qp = this.popups.get (name);
        
        if (qp != null)
        {
            
            qp.removeFromParent ();
            
        }
        
        this.popups.put (name,
                         popup);
        
    }
    
    public void removeNamedPopup (String name)
    {
        
        QPopup qp = this.popups.remove (name);
        
        if (qp != null)
        {
            
            qp.removeFromParent ();
            
        }
        
    }
    
    public QPopup getNamedPopup (String name)
    {
        
        return this.popups.get (name);
        
    }
    
    public void showAbout ()
    {
        
        final AbstractViewer _this = this;
        
        String popupName = "about";
        
        QPopup popup = this.getNamedPopup (popupName);
        
        if (popup == null)
        {
                    
            final QPopup qp = UIUtils.createClosablePopup ("About",
                                                           Environment.getIcon (Constants.ABOUT_ICON_NAME,
                                                                                Constants.ICON_POPUP),
                                                           null);
                
            qp.addPopupListener (new PopupAdapter ()
            {
               
                @Override
                public void popupHidden (PopupEvent ev)
                {
                    
                    _this.removeNamedPopup ("about");
                    
                }
                
            });
                  
            Box content = new Box (BoxLayout.Y_AXIS);
    
            FormLayout pfl = new FormLayout ("5px, right:p, 6px, fill:p:grow",
                                             "p, 6px, p, 6px, p, 6px, p, 6px, p, 6px, p, 6px, p, 6px, p, 6px, p");
    
            PanelBuilder pbuilder = new PanelBuilder (pfl);
    
            CellConstraints cc = new CellConstraints ();
    
            int y = 1;
    
            pbuilder.addLabel ("Version",
                               cc.xy (2,
                                      y));
    
            pbuilder.addLabel (Environment.getQuollWriterVersion ().getVersion (),
                               cc.xy (4,
                                      y));
    
            y += 2;
    
            pbuilder.addLabel ("Copyright",
                               cc.xy (2,
                                      y));
    
            Date d = new Date ();
    
            SimpleDateFormat sdf = new SimpleDateFormat ("yyyy");
    
            String year = sdf.format (d);
    
            pbuilder.addLabel (String.format ("\u00A9 2009-%s Gary Bentley",
                                              year),
                               cc.xy (4,
                                      y));
    
            y += 2;
    
            pbuilder.addLabel ("Website",
                               cc.xy (2,
                                      y));
    
            pbuilder.add (UIUtils.createWebsiteLabel (Environment.getQuollWriterWebsite (),
                                                      null,
                                                      false),
                          cc.xy (4,
                                 y));
    
            y += 2;
    
            pbuilder.addLabel ("Source Code",
                               cc.xy (2,
                                      y));
    
            pbuilder.add (UIUtils.createWebsiteLabel ("https://github.com/garybentley/quollwriter",
                                                      null,
                                                      false),
                          cc.xy (4,
                                 y));
    
            y += 2;
    
            String relNotesUrl = Environment.getProperty (Constants.QUOLL_WRITER_RELEASE_NOTES_URL_PROPERTY_NAME);
    
            relNotesUrl = StringUtils.replaceString (relNotesUrl,
                                                     "[[VERSION]]",
                                                     Environment.getQuollWriterVersion ().getVersion ().replace ('.',
                                                                                                                 '_'));
    
            pbuilder.add (UIUtils.createWebsiteLabel (relNotesUrl,
                                                      "Release Notes",
                                                      false),
                          cc.xy (4,
                                 y));
    
            y += 2;
    
            pbuilder.add (UIUtils.createWebsiteLabel ("https://www.patreon.com/quollwriter?ty=h",
                                                      "Patreon",
                                                      false),
                          cc.xy (4,
                                 y));
    
            y += 2;

            pbuilder.add (UIUtils.createWebsiteLabel (Environment.getProperty (Constants.QUOLL_WRITER_ACKNOWLEDGMENTS_URL_PROPERTY_NAME),
                                                      "Acknowledgments",
                                                      false),
                          cc.xy (4,
                                 y));
    
            y += 2;
            
            JButton closeBut = new JButton ();
            closeBut.setText ("Close");
    
            closeBut.addActionListener (new ActionListener ()
            {
    
                public void actionPerformed (ActionEvent ev)
                {
    
                    qp.removeFromParent ();
                        
                }
    
            });
            
            JButton[] buts = { closeBut };
    
            JPanel bp = UIUtils.createButtonBar2 (buts,
                                                  Component.LEFT_ALIGNMENT); 
            bp.setOpaque (false);
            
            JPanel p = pbuilder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
    
            content.add (p);
            
            content.add (Box.createVerticalStrut (10));
            content.add (bp);
            
            content.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                             content.getPreferredSize ().height));
            content.setBorder (UIUtils.createPadding (10, 10, 10, 10));        
            
            qp.setContent (content);
            
            qp.setDraggable (this);
                              
            qp.resize ();

            popup = qp;
            
            this.addNamedPopup (popupName,
                                popup);

            this.showPopupAt (popup,
                              UIUtils.getCenterShowPosition (this,
                                                             popup),
                              false);
            
        } else {

            popup.setVisible (true);
            
        }
            
        this.fireProjectEvent (ProjectEvent.ABOUT,
                               ProjectEvent.SHOW);
        
    }
    
    public void showWhatsNew (boolean onlyShowCurrentVersion)
    {

        String popupName = "whatsnew";
        
        QPopup popup = this.getNamedPopup (popupName);
                
        try
        {

            popup = UIUtils.createWizardPopup ("What's new in version " + Environment.getQuollWriterVersion (),
                                               Constants.WHATS_NEW_ICON_NAME,
                                               null,
                                               new WhatsNew (this,
                                                             onlyShowCurrentVersion));
    
            popup.setPopupName (popupName); 
    
        } catch (Exception e) {
            
            // Not good but not the end of the world but shouldn't stop things from going on.
            Environment.logError ("Unable to init whats new",
                                  e);
            
            UIUtils.showErrorMessage (this,
                                      "Unable to show What's New, please contact Quoll Writer support for assitance.");
            
            return;
            
        }
            
        this.addNamedPopup (popupName,
                            popup);            
                        
        popup.setDraggable (this);
                          
        popup.resize ();
        this.showPopupAt (popup,
                          UIUtils.getCenterShowPosition (this,
                                                         popup),
                          false);
                        
        this.fireProjectEvent (ProjectEvent.WHATS_NEW,
                               ProjectEvent.SHOW);
   
    }
    
    private void handleShowTips ()
    {
        
        if (Environment.isFirstUse ())
        {
            
            return;
            
        }
        
        if ((this.tips != null)
            &&
            (Environment.getUserProperties ().getPropertyAsBoolean (Constants.SHOW_TIPS_PROPERTY_NAME))
           )
        {

            final AbstractViewer _this = this;

            try
            {

                String tipText = this.tips.getNextTip ();
        
                final JTextPane htmlP = UIUtils.createHelpTextPane (tipText,
                                                                    this);
                
                htmlP.setBorder (null);
                htmlP.setSize (new Dimension (500,
                                              500));

                JButton nextBut = UIUtils.createButton ("next",
                                                        Constants.ICON_MENU,
                                                        "Click to view the next tip",
                                                        null);
    
                java.util.List<JButton> buts = new ArrayList ();
                buts.add (nextBut);
                
                JButton offBut = UIUtils.createButton (Constants.STOP_ICON_NAME,
                                                       Constants.ICON_MENU,
                                                       "Click to stop showing tips when Quoll Writer starts",
                                                       null);
                
                buts.add (offBut);
                                                
                // Show a tip.
                final Notification n = this.addNotification (htmlP,
                                                             Constants.HELP_ICON_NAME,
                                                             90,
                                                             buts);

                nextBut.addActionListener (new ActionAdapter ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        String t = _this.tips.getNextTip ();
                        
                        if (t != null)
                        {

                            htmlP.setText (t);
                            
                            htmlP.validate ();
                            //htmlP.getParent ().validate ();
                            //htmlP.getParent ().repaint ();

                            n.setMinimumSize (n.getPreferredSize ());
                            
                            _this.repaint ();

                            n.restartTimer ();

                            _this.fireProjectEvent (ProjectEvent.TIPS,
                                                    ProjectEvent.SHOW);

                        }
                        
                    }
                    
                });

                offBut.addActionListener (new ActionAdapter ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        JButton but = (JButton) ev.getSource ();
                        
                        Point p = SwingUtilities.convertPoint (but,
                                                               0,
                                                               0,
                                                               _this);

                        UIUtils.createQuestionPopup (_this,
                                                     "Stop showing tips?",
                                                     Constants.STOP_ICON_NAME,
                                                     "Stop showing tips when Quoll Writer starts?<br /><br />They can enabled at any time in the <a href='action:projectoptions'>Options panel</a>.",
                                                     "Yes, stop showing them",
                                                     "No, keep them",
                                                     new ActionListener ()
                                                     {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                        
                                                            _this.fireProjectEvent (ProjectEvent.TIPS,
                                                                                    ProjectEvent.OFF);
                                                        
                                                            try
                                                            {
                                                        
                                                                Environment.setUserProperty (Constants.SHOW_TIPS_PROPERTY_NAME,
                                                                                             new BooleanProperty (Constants.SHOW_TIPS_PROPERTY_NAME,
                                                                                                                  false));
                                
                                                            } catch (Exception e) {
                                                                
                                                                Environment.logError ("Unable to turn off tips",
                                                                                      e);
                                                                
                                                            }
                                
                                                            n.removeNotification ();
                                                            
                                                        }

                                                     },
                                                     null,
                                                     null,
                                                     p);
                        
                    }
                    
                });
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to show tips",
                                      e);
                
            }

        }        
        
    }
            
    public void handleHTMLPanelAction (String v)
    {

        StringTokenizer t = new StringTokenizer (v,
                                                 ",;");
        
        if (t.countTokens () > 1)
        {
        
            while (t.hasMoreTokens ())
            {
                
                this.handleHTMLPanelAction (t.nextToken ().trim ());
                
            }

            return;

        }

        try
        {

            if (v.equals ("showundealtwitheditormessages"))
            {
                
                this.viewEditors ();
                
                return;
                                
            }

            if (v.startsWith ("options"))
            {
                
                String section = null;
                
                int dot = v.indexOf ('.');
                
                if (dot > 1)
                {
                    
                    section = v.substring (dot + 1);
                    
                }
                
                this.showOptions (section);

                return;
                
            }
            
            if (v.equals ("whatsnew"))
            {
                
                this.showWhatsNew (true);

                return;
                
            }
            
            if (v.equals ("achievements"))
            {
                
                this.viewAchievements ();
                
                return;
                
            }

            if (v.equals ("warmup"))
            {
                
                this.showWarmupPromptSelect ();
                
                return;
                
            }
                
            if (v.equals ("showinviteeditor"))
            {
                
                EditorsUIUtils.showInviteEditor (this);
                
                return;                
                
            }
                        
            if (v.equals ("editobjectnames"))
            {
                
                this.showObjectTypeNameChanger ();
                
                return;
                        
            }
            
            if (v.equals ("contact"))
            {
                
                this.showContactSupport ();
                
                return;
                
            }

            if (v.equals ("reportbug"))
            {

                this.showReportProblem ();
                
                return;
                
            }

            if (v.equals ("dictionarymanager"))
            {
                
                this.showDictionaryManager ();
                
                return;
                
            }
            
            if (v.equals ("editors"))
            {

                this.viewEditors ();
                
                return;
                
            }
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to perform action: " +
                                  v,
                                  e);
            
        }
        
    }
    
    public void showObjectTypeNameChanger ()
    {
        
        String popupName = "editobjectnames";
        QPopup popup = this.getNamedPopup (popupName);
        
        if (popup == null)
        {
                
            popup = UIUtils.createClosablePopup ("Edit Object Names",
                                                 Environment.getIcon (Constants.CONFIG_ICON_NAME,
                                                                      Constants.ICON_POPUP),
                                                 null);
            
            popup.setPopupName (popupName);
            
            ObjectTypeNameChanger c = new ObjectTypeNameChanger (this);
            
            c.init ();        
    
            popup.setContent (c);
    
            c.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                       c.getPreferredSize ().height));
            c.setBorder (UIUtils.createPadding (10, 10, 10, 10));        
            
            popup.setContent (c);
            
            popup.setDraggable (this);
                              
            popup.resize ();

            this.addNamedPopup (popupName,
                                popup);            
            
            this.showPopupAt (popup,
                              UIUtils.getCenterShowPosition (this,
                                                             popup),
                              false);
            
        } else {
            
            popup.setVisible (true);            
            
        }
                
    }    
    
    private boolean closeInternal (boolean        saveUnsavedChanges,
                                   ActionListener afterClose)
    {
                
        this.notifications.setVisible (false);        
                
        this.dispose ();

        if (afterClose != null)
        {

            afterClose.actionPerformed (new ActionEvent (this,
                                                         0,
                                                         "closed"));
            
        }
        
        return true;
        
    }
    
    public boolean close (boolean              noConfirm,
                          final ActionListener afterClose)
    {
            
        return this.closeInternal (true,
                                   afterClose);
                            
    }
    
    public void addPopup (Component c)
    {

        this.addPopup (c,
                       false,
                       false);

    }

    public void addPopup (Component c,
                          boolean   hideOnClick,
                          boolean   hideViaVisibility)
    {

        this.getLayeredPane ().add (c,
                                    JLayeredPane.POPUP_LAYER);

        this.getLayeredPane ().moveToFront (c);

//      this.getLayeredPane ().repaint ();

    }

    @Override
    public void removePopup (Component c)
    {
    
        this.getLayeredPane ().remove (c);

        this.getLayeredPane ().validate ();

        this.getLayeredPane ().repaint ();

    }
    
    public void showAchievement (AchievementRule ar)
    {

        try
        {
            
            Box b = null;
            
            if (this.achievementsPopup == null)
            {
    
                b = new Box (BoxLayout.Y_AXIS);
                b.setBackground (UIUtils.getComponentColor ());
                b.setOpaque (true);            
                
                this.achievementsPopup = UIUtils.createPopup ("You've got an Achievement",
                                                              Constants.ACHIEVEMENT_ICON_NAME,
                                                              b,
                                                              true,
                                                              null);
                
                this.achievementsPopup.getHeader ().setPreferredSize (new Dimension (250,
                                                                      this.achievementsPopup.getHeader ().getPreferredSize ().height));
    
                final AbstractViewer _this = this;
                final Box content = b;
        
                this.achievementsPopup.getHeader ().addMouseListener (new MouseAdapter ()
                {
        
                    public void mouseReleased (MouseEvent ev)
                    {
        
                        _this.achievementsPopup.setVisible (false);
        
                        content.removeAll ();                    
        
                    }
        
                });
    
                this.achievementsPopup.addMouseListener (new ComponentShowHide (new ActionAdapter ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.achievementsHideTimer.stop ();
                        
                    }
                    
                },
                new ActionAdapter ()
                {
    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.achievementsHideTimer.start ();
                        
                    }
                    
                }));
    
            } else {
                
                b = (Box) this.achievementsPopup.getContent ();
                            
            }
    
            JComponent arBox = new AchievementBox (ar,
                                                   false,
                                                   true);
    
            if (b.getComponentCount () > 0)
            {
                
                arBox.setBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, Environment.getBorderColor ()),
                                                     arBox.getBorder ()));
                
            }
    
            b.add (arBox,
                   0);
    
            if (this.achievementsPopup.getParent () != null)
            {
                
                this.achievementsPopup.getParent ().remove (this.achievementsPopup);
                
            }
    
            this.showPopupAt (this.achievementsPopup,
                              new Point (10, 10),
                              true);
    
            this.achievementsPopup.setVisible (true);
            
            final AbstractViewer _this = this;
            final Box content = b;
            
            if (this.achievementsHideTimer == null)
            {
            
                this.achievementsHideTimer = new Timer (10000,
                                                        new ActionAdapter ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.achievementsPopup.setVisible (false);
        
                        content.removeAll ();
                                        
                    }
                    
                });
    
                this.achievementsHideTimer.setRepeats (false);
    
            }
    
            this.achievementsHideTimer.stop ();
                    
            this.achievementsHideTimer.start ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to display achievement: " +
                                  ar,
                                  e);
            
        }

    }
    
    public void showNotificationPopup (String title,
                                       String message,
                                       int    showFor)
    {
        
        JComponent c = null;
        JTextPane m = null;
        
        if (message != null)
        {
            
            m = UIUtils.createHelpTextPane (message,
                                            this);

            m.setSize (new Dimension (350 - 20,
                                         m.getPreferredSize ().height));  
  
            Box b = new Box (BoxLayout.Y_AXIS);
            b.setBackground (UIUtils.getComponentColor ());
            b.setOpaque (true);            
            b.add (m);
            b.setBorder (UIUtils.createPadding (10, 10, 10, 10));
            b.setPreferredSize (new Dimension (350,
                                               b.getPreferredSize ().height));

            c = b;
            
        }

        final QPopup popup = UIUtils.createPopup (title,
                                                  Constants.INFO_ICON_NAME,
                                                  null,
                                                  true,
                                                  null);
              
        popup.setContent (c);
                
        if (m != null)
        {
            
            m.addHyperlinkListener (new HyperlinkAdapter ()
            {

                public void hyperlinkUpdate (HyperlinkEvent ev)
                {

                    if (ev.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
                    {
               
                        popup.removeFromParent ();
                        
                    }
                    
                }
                
            });
                                                  
        }

        this.showPopupAt (popup,
                          new Point (10, 10),
                          true);
        
        if (showFor > 0)
        {
                        
            final Timer t = new Timer (showFor * 1000,
                                       new ActionAdapter ()
                                       {
                            
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                    
                                                popup.removeFromParent ();
                                    
                                            }
                                            
                                       });

            popup.addMouseListener (new ComponentShowHide (new ActionAdapter ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
                    
                    t.stop ();
                    
                }
                
            },
            new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {
                    
                    t.start ();
                    
                }
                
            }));

            t.setRepeats (false);

            t.start ();
            
        }
        
    }
    
    public QPopup getPopupByName (String name)
    {
        
        if (name == null)
        {
            
            return null;
            
        }
        
        Component[] children = this.getLayeredPane ().getComponentsInLayer (JLayeredPane.POPUP_LAYER);
        
        if (children == null)
        {
            
            return null;
            
        }
        
        for (int i = 0; i < children.length; i++)
        {
            
            Component c = children[i];
            
            if (name.equals (c.getName ()))
            {
                
                if (c instanceof QPopup)
                {
                    
                    return (QPopup) c;
                    
                }
                
            }
                        
        }
        
        return null;
        
    }
    
    public void showPopupAt (JComponent c,
                             Rectangle r,
                             String    where,
                             boolean   hideOnParentClick)
    {

        Dimension s = c.getPreferredSize ();

        int x = r.x;
        int y = r.y - c.getInsets ().top;// - c.getInsets ().bottom;

        if (where == null)
        {

            where = "below";

        }

        if (where.equals ("below"))
        {

            y = y + r.height;

        }

        if (where.equals ("above"))
        {

            y = y - s.height - c.getInsets ().bottom;

        }

        if (y < 0)
        {

            y = r.y; // + r.height;

        }

        if ((x + s.width) > (this.getWidth ()))
        {

            x = this.getWidth () - 20 - s.width;

        }

        if (x < 0)
        {

            x = 5;

        }

        this.showPopupAt (c,
                          new Point (x,
                                     y),
                          hideOnParentClick);

    }
    
    @Override
    public void showPopupAt (Component popup,
                             Component showAt,
                             boolean   hideOnParentClick)
    {
    
        Point po = SwingUtilities.convertPoint (showAt,
                                                0,
                                                0,
                                                this.getContentPane ());

        this.showPopupAt (popup,
                          po,
                          hideOnParentClick);


    }

    @Override
    public void showPopupAt (Component c,
                             Point     p,
                             boolean   hideOnParentClick)
    {
    
        Insets ins = this.getInsets ();

        if ((c.getParent () == null)
            &&
            (c.getParent () != this.getLayeredPane ())
           )
        {

            this.addPopup (c,
                           hideOnParentClick,
                           false);

        }

        Dimension cp = c.getPreferredSize ();

        if ((p.y + cp.height) > (this.getBounds ().height - ins.top - ins.bottom))
        {

            p = new Point (p.x,
                           p.y);

            // See if the child is changing height.
            if (c.getBounds ().height != cp.height)
            {
            
                p.y = p.y - (cp.height - c.getBounds ().height);
        
            } else {
                
                p.y = p.y - cp.height;

            }
                
        }

        if (p.y < 0)
        {

            p = new Point (p.x,
                           p.y);

            p.y = 10;

        }

        if ((p.x + cp.width) > (this.getBounds ().width - ins.left - ins.right))
        {

            p = new Point (p.x,
                           p.y);

            p.x = p.x - cp.width;

        }

        if (p.x < 0)
        {

            p = new Point (p.x,
                           p.y);

            p.x = 10;

        }

        c.setBounds (p.x,
                     p.y,
                     c.getPreferredSize ().width,
                     c.getPreferredSize ().height);

        c.setVisible (true);
        this.validate ();
        this.repaint ();

    }

    public void showPopup (Component c,
                           boolean   hideOnParentClick)
    {

        Point p = this.getMousePosition ();

        if (p != null)
        {

            SwingUtilities.convertPointToScreen (p,
                                                 this);

        } else
        {

            p = new Point (300,
                           300);

        }

        this.showPopupAt (c,
                          p,
                          hideOnParentClick);

    }

    public Point convertPoint (Component c,
                               Point     p)
    {

        Component o = this.getContentPane ();
                
        return SwingUtilities.convertPoint (c,
                                            p,
                                            o);
        
    }
             
    public void setViewerControls (JComponent c)
    {
        
        this.title.setControls (c);
        
    }

    public void removeProjectEventListener (ProjectEventListener l)
    {
        
        this.projectEventListeners.remove (l);
        
    }

    public void addProjectEventListener (ProjectEventListener l)
    {
        
        this.projectEventListeners.add (l);
        
    }

    public void fireProjectEventLater (final String type,
                                       final String action)
    {
        
        final AbstractViewer _this = this;
        
        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.fireProjectEvent (type,
                                        action);
                
            }
            
        });
        
    }

    public void fireProjectEvent (String type,
                                  String action,
                                  Object contextObject)
    {

        this.fireProjectEvent (new ProjectEvent (this,
                                                 type,
                                                 action,
                                                 contextObject));

    }

    public void fireProjectEvent (String type,
                                  String action)
    {
        
        this.fireProjectEvent (new ProjectEvent (this,
                                                 type,
                                                 action));
        
    }

    public void setIgnoreProjectEvents (boolean v)
    {
        
        this.ignoreProjectEvents = v;
        
    }

    public void fireProjectEvent (ProjectEvent ev)
    {
        
        if (this.ignoreProjectEvents)
        {
            
            return;
            
        }
        
        for (ProjectEventListener l : this.projectEventListeners)
        {
            
            l.eventOccurred (ev);
            
        }
        
    }

    public void showDictionaryManager ()
    {
                    
        String popupName = "dictman";
        QPopup popup = this.getNamedPopup (popupName);
        
        if (popup == null)
        {
        
            DictionaryManager dictMan = new DictionaryManager (this);
                
            dictMan.init ();
        
            popup = UIUtils.createClosablePopup ("Manage your personal Dictionary",
                                                 Environment.getIcon (Constants.DICTIONARY_ICON_NAME,
                                                                      Constants.ICON_POPUP),
                                                 null);
        
            popup.setRemoveOnClose (false);        
        
            popup.setPopupName (popupName);
            
            this.addNamedPopup (popupName,
                                popup);
        
            dictMan.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                             dictMan.getPreferredSize ().height));
            dictMan.setBorder (UIUtils.createPadding (10, 10, 10, 10));        
        
            popup.setContent (dictMan);
            
            popup.setDraggable (this);
                              
            popup.resize ();
            this.showPopupAt (popup,
                              UIUtils.getCenterShowPosition (this,
                                                             popup),
                              false);

        } else {
            
            popup.setVisible (true);
            
        }
            
        this.fireProjectEvent (ProjectEvent.PERSONAL_DICTIONARY,
                               ProjectEvent.SHOW);

    }

    public void showEditNoteTypes ()
    {
        
        String popupName = "editnotetypes";
        QPopup popup = this.getNamedPopup (popupName);
        
        if (popup == null)
        {
        
            popup = UIUtils.createClosablePopup ("Manage the {Note} Types",
                                                 Environment.getIcon (Constants.EDIT_ICON_NAME,
                                                                      Constants.ICON_POPUP),
                                                 null);
        
            popup.setRemoveOnClose (false);
        
            popup.setPopupName (popupName);
            
            this.addNamedPopup (popupName,
                                popup);
        
            EditNoteTypes content = new EditNoteTypes (this);
            content.init ();
        
            content.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                             content.getPreferredSize ().height));
            content.setBorder (UIUtils.createPadding (10, 10, 10, 10));        
        
            popup.setContent (content);
            
            popup.setDraggable (this);
                              
            popup.resize ();
            this.showPopupAt (popup,
                              UIUtils.getCenterShowPosition (this,
                                                             popup),
                              false);

        } else {
            
            popup.setVisible (true);
            
        }

        this.fireProjectEvent (ProjectEvent.NOTE_TYPES,
                               ProjectEvent.SHOW);
        
    }

    public void showEditItemTypes ()
    {
        
        String popupName = "edititemtypes";
        QPopup popup = this.getNamedPopup (popupName);
        
        if (popup == null)
        {
        
            popup = UIUtils.createClosablePopup ("Manage the {Object} Types",
                                                 Environment.getIcon (Constants.EDIT_ICON_NAME,
                                                                      Constants.ICON_POPUP),
                                                 null);
        
            popup.setRemoveOnClose (false);
        
            popup.setPopupName (popupName);
            
            this.addNamedPopup (popupName,
                                popup);
        
            EditItemTypes content = new EditItemTypes (this);
            content.init ();
        
            content.setBorder (UIUtils.createPadding (10, 10, 10, 10));
            
            content.setPreferredSize (new Dimension (UIUtils.getPopupWidth () - 20,
                             content.getPreferredSize ().height));
        
            popup.setContent (content);
            
            popup.setDraggable (this);
                              
            popup.resize ();
            this.showPopupAt (popup,
                              UIUtils.getCenterShowPosition (this,
                                                             popup),
                              false);

        } else {
            
            popup.setVisible (true);

            
        }

        this.fireProjectEvent (ProjectEvent.ITEM_TYPES,
                               ProjectEvent.SHOW);
        
    }

    /**
     * Schedule the task to run after delay and repeat (use -1 or 0 for no repeat).
     *
     * @param t The task to run.
     * @param delay The delay, in millis.
     * @param repeat The repeat time, in millis.
     */
    public void schedule (TimerTask t,
                          long      delay,
                          long      repeat)
    {
       
        if (repeat < 1)
        {
            
            this.generalTimer.schedule (t,
                                        delay);
            
        } else {
        
            this.generalTimer.schedule (t,
                                        delay,
                                        repeat);

        }
        
    }
    
}
