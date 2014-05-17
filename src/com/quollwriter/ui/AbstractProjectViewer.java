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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.sound.sampled.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

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

import com.quollwriter.achievements.rules.*;

public abstract class AbstractProjectViewer extends JFrame implements PropertyChangedListener,
                                                                      SpellCheckSupported,
                                                                      PopupsSupported,
                                                                      HTMLPanelActionHandler
{

    public static final String TAB_OBJECT_TYPE = "tab";

    // public static final int CLOSE_WARMUPS_ACTION = 12; //"closeWarmups";
    public static final int CREATE_PROJECT_SNAPSHOT_ACTION = 0;
    public static final int NEW_PROJECT_ACTION = 1;
    public static final int EDIT_CHAPTER_ACTION = 4; // "editChapter";
    public static final int DELETE_CHAPTER_ACTION = 5;
    public static final int RENAME_CHAPTER_ACTION = 7; // "renameChapter";
    public static final int ABOUT_ACTION = 9; // "about";
    public static final int REPORT_BUG_ACTION = 10; // "reportBug";
    public static final int OPEN_PROJECT_ACTION = 11; // "openProject";
    public static final int CLOSE_PROJECT_ACTION = 12; // "closeProject";
    public static final int DELETE_PROJECT_ACTION = 13; // "deleteProject";
    public static final int RENAME_PROJECT_ACTION = 14; // "renameProject";
    public static final int EDIT_PROJECT_PROPERTIES_ACTION = 15; // "editProjectProperties";
    public static final int WARMUP_EXERCISE_ACTION = 26;
    public static final int SHOW_STATISTICS_ACTION = 27;
    public static final int CONTACT_SUPPORT_ACTION = 28;

    public static final String NAME_CHANGED = "nameChanged";

    protected Project             proj = null;
    private DnDTabbedPane         tabs = null;
    private DictionaryProvider    dictProv = null;
    private SynonymProvider       synProv = null;
    private JSplitPane            splitPane = null;
    protected ObjectManager       dBMan = null;
    private Header                title = null;
    private Map<String, JToolBar> toolbars = new HashMap ();
    private Box                   toolbarPanel = null;
    private Accordion             acc = null;
    private boolean               spellCheckingEnabled = false;
    private String                spellCheckLanguage = null;
    private Date                  sessionStart = new Date ();
    private Box                   notifications = null;
    private boolean               playSoundOnKeyStroke = false;
    private Clip                  keyStrokeSound = null;
    private QPopup                achievementsPopup = null;
    private ChapterCounts         startWordCounts = new ChapterCounts ();
    private Map<Chapter, ChapterCounts> noEditorChapterCounts = new WeakHashMap ();
    private Map<Chapter, ReadabilityIndices> noEditorReadabilityIndices = new WeakHashMap ();
    //private Box                   itemsBox = null;
    private Box                   sideBar = null;
    private Box                   sideBarWrapper = null;
    private Map<String, AbstractSideBar> sideBars = new HashMap ();
    private AbstractSideBar       currentSideBar = null;
    private Finder                finder = null;
    private WordCountsSideBar      wordCounts = null;
    private Map<String, QuollPanel> panels = new HashMap ();
    private int                   lastDividerLocation = -1;
    private Map<String, Integer>  sideBarWidths = new HashMap ();
    //private String                currentSideBar = null;
    private java.util.List<SideBarListener> sideBarListeners = new ArrayList ();
    private java.util.List<MainPanelListener> mainPanelListeners = new ArrayList ();

    private Timer achievementsHideTimer = null;

    private JTree chapterTree = null;

    private FullScreenFrame fsf = null;
    private FullScreenOverlay fullScreenOverlay = null;

    private String toolbarLocation = Constants.BOTTOM;

    private String sidebarLocation = Constants.LEFT;    
    
    private Map tempOptions = new HashMap ();

    private Set<ProjectEventListener> projectEventListeners = new HashSet ();
    private boolean ignoreProjectEvents = false;

    private Tips tips = null;

    private DictionaryManager dictMan = null;
    
    private WordCountTimer wordCountTimer = null;

    public AbstractProjectViewer()
    {

        final AbstractProjectViewer _this = this;

        this.addWindowListener (new WindowAdapter ()
            {

                public void windowClosing (WindowEvent ev)
                {

                    // Save the open tabs.
                    _this.close (false,
                                 null);

                }

            });

        this.wordCountTimer = new WordCountTimer (this,
                                                  -1,
                                                  -1);            
        
        this.getContentPane ().setBackground (UIUtils.getComponentColor ());
            
        this.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);

        // Create a split pane.
        this.splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
                                         false);
        this.splitPane.setDividerSize (2);//UIUtils.getSplitPaneDividerSize ());
        this.splitPane.setBorder (null);

        javax.swing.plaf.basic.BasicSplitPaneDivider div = ((javax.swing.plaf.basic.BasicSplitPaneUI) this.splitPane.getUI ()).getDivider ();
        div.setBorder (new MatteBorder (0, 0, 0, 1, Environment.getBorderColor ()));
        this.splitPane.setOpaque (false);
        this.splitPane.setBackground (UIUtils.getComponentColor ());
        
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
                
                /*
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F1,
                                        0),
                Constants.SHOW_FIND_ACTION);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F3,
                                        0),
                "show-options");

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F4,
                                        0),
                "close-current-tab");

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F5,
                                        0),
                "fullscreen");                

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F10,
                                        0),
                "do-warmup");
                
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE,
                                        0),
                "show-main-sidebar");
                                
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                        Event.CTRL_MASK),
                Constants.SHOW_FIND_ACTION);
                */
                                        
        this.tabs = this.createTabbedPane ();
        
        this.tabs.putClientProperty(com.jgoodies.looks.Options.NO_CONTENT_BORDER_KEY, Boolean.TRUE);
        //this.tabs.putClientProperty(com.jgoodies.looks.Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
        
        this.setTabsLocation (null);

        this.splitPane.setRightComponent (this.tabs);

        this.sideBar = new Box (BoxLayout.PAGE_AXIS);        
        this.sideBar.setOpaque (false);
        this.sideBar.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                    Short.MAX_VALUE));
        
        this.toolbarPanel = new Box (BoxLayout.X_AXIS);
        
        this.toolbarPanel.setOpaque (false);

        this.toolbarPanel.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        this.toolbarPanel.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                         100));
                                                        
        this.sideBarWrapper = new Box (BoxLayout.X_AXIS);
        this.sideBarWrapper.setOpaque (false);
        this.sideBarWrapper.setAlignmentX (Component.LEFT_ALIGNMENT);
                                                 
        this.sideBar.add (this.sideBarWrapper);
        this.sideBar.add (this.toolbarPanel);
                                
        this.splitPane.setLeftComponent (this.sideBar);
        this.splitPane.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        Box b = new Box (BoxLayout.Y_AXIS);

        this.title = new Header ();
        this.title.setFont (this.title.getFont ().deriveFont (22f));
        this.title.setAlignmentX (Component.LEFT_ALIGNMENT);

        // new
        Header h = this.title;
        h.setFont (h.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (22d)).deriveFont (Font.PLAIN));
        h.setPaintProvider (null);
        h.setTitleColor (UIUtils.getTitleColor ());
        h.setIcon (null);
        h.setPadding (new Insets (3, 3, 3, 7));
        
        JToolBar titleC = UIUtils.createButtonBar (new ArrayList ());

        this.fillTitleToolbar (titleC);                                        

        titleC.add (UIUtils.createButton (Constants.FIND_ICON_NAME,
                                          Constants.ICON_TITLE_ACTION,
                                          "Click to open the find",
                                          new ActionAdapter ()
                                          {
                                            
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                
                                                  _this.showFind (null);
                                                
                                              }
                                            
                                          }));
                    
        titleC.add (UIUtils.createButton (Constants.FULLSCREEN_ICON_NAME,
                                          Constants.ICON_TITLE_ACTION,
                                          "Click to work in full screen mode",
                                          new ActionAdapter ()
                                          {
                                                
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                                    
                                                  _this.enterFullScreen ();
                                                    
                                              }
                                                
                                          }));
                
        titleC.add (UIUtils.createButton (Constants.SETTINGS_ICON_NAME,
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
                                    
                                                    titlePopup.add (_this.createMenuItem ("Find",
                                                                                          Constants.FIND_ICON_NAME,
                                                                                          new ActionAdapter ()
                                                                                          {
                                                                                
                                                                                            public void actionPerformed (ActionEvent ev)
                                                                                            {
                                                                                
                                                                                                _this.showFind (null);
                                                                                
                                                                                            }
                                                                                
                                                                                         }));
                                    
                                                    titlePopup.add (_this.createMenuItem ("Options",
                                                                                          Constants.OPTIONS_ICON_NAME,
                                                                                          ProjectViewer.EDIT_PROJECT_PROPERTIES_ACTION));
                                                    
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
                                                                                                         "help:index.html");
                                                        
                                                                                    }
                                                        
                                                                                 }));
                                    
                                                    m.add (_this.createMenuItem ("Keyboard Shortcuts",
                                                                                 null,
                                                                                 new ActionAdapter ()
                                                                                 {
                                                        
                                                                                    public void actionPerformed (ActionEvent ev)
                                                                                    {
                                                        
                                                                                        UIUtils.openURL (_this,
                                                                                                         "help:main-window/keyboard-shortcuts.html");
                                                        
                                                                                    }
                                                        
                                                                                 }));
                                    
                                                    // About Quoll Writer
                                                    titlePopup.add (_this.createMenuItem ("About Quoll Writer",
                                                                                          Constants.ABOUT_ICON_NAME,
                                                                                          AbstractProjectViewer.ABOUT_ACTION));
                                    
                                                    if (Environment.debugMode)
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
                                        
                                           }));
                                        
        
        this.title.setControls (titleC);

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
        
        b.add (this.splitPane);

        try
        {
            
            this.tips = new Tips (this);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to init tips",
                                  e);
            
        }
        
        this.addTabChangeListener (new ChangeAdapter ()
        {
           
            public void stateChanged (ChangeEvent ev)
            {
                
                QuollPanel qp = _this.getCurrentlyVisibleTab ();
                
                if (qp != null)
                {
                
                    _this.fireMainPanelShownEvent (qp);
                    
                }
                
            }
            
        });
                
        this.getContentPane ().add (b);

    }

    public void showReportProblem ()
    {
        
        ReportAProblem rp = new ReportAProblem (this);

        rp.init ();        
        
    }
    
    public void showObjectTypeNameChanger ()
    {
        
        ObjectTypeNameChanger c = new ObjectTypeNameChanger (this);
        
        c.init ();        
        
    }
    
    public void showContactSupport ()
    {
        
        ContactSupport sp = new ContactSupport (this);

        sp.init ();        
        
    }
    
    public void initActionMappings (ActionMap am)
    {
        
        final AbstractProjectViewer _this = this;
        
        am.put ("show-options",
                new ActionAdapter ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.showOptions ();
                        
                    }
                    
                });
        
        am.put ("close-current-tab",
                new ActionAdapter ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                    
                        QuollPanel qp = _this.getCurrentlyVisibleTab ();

                        if (qp != null)
                        {
                            
                            _this.closePanel (qp);
                            
                        }
                            
                    }
        
                });        
        
        am.put ("do-warmup",
                new ActionAdapter ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                    
                        _this.getAction (AbstractProjectViewer.WARMUP_EXERCISE_ACTION).actionPerformed (ev);
        
                    }
        
                });        
        
        am.put ("show-main-sidebar",
                new ActionAdapter ()
                {
                   
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        _this.showMainSideBar ();
                        
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

                        Environment.debugMode = !Environment.debugMode;
                        
                        // Add a notification.
                        _this.addNotification (Notification.createMessageNotification (_this,
                                                                                       "Debug mode is now <b>" + (Environment.debugMode ? "ENabled" : "DISabled") + "</b>",
                                                                                       5));

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
                
        am.put (Constants.SHOW_FIND_ACTION,
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.showFind (null);

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
                
        am.put ("fullscreen",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                       _this.enterFullScreen ();
                    
                    }

                });                        
        
    }
    
    public void initKeyMappings (InputMap im)
    {
        
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F1,
                                        0),
                Constants.SHOW_FIND_ACTION);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F3,
                                        0),
                "show-options");

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F4,
                                        0),
                "close-current-tab");

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F5,
                                        0),
                "fullscreen");                

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F10,
                                        0),
                "do-warmup");
                
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE,
                                        0),
                "show-main-sidebar");
                                
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                        Event.CTRL_MASK),
                Constants.SHOW_FIND_ACTION);        
        
    }
    
    public WordCountTimer getWordCountTimer ()
    {
        
        return this.wordCountTimer;
        
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
    
    private void initSideBars ()
    {
        
        this.finder = new Finder (this);
        
        this.addSideBar ("find",
                         this.finder);

        this.wordCounts = new WordCountsSideBar (this);

        this.addSideBar ("wordcounts",
                         this.wordCounts);        

        this.addSideBar ("project",
                         this.getMainSideBar ());        

        this.showMainSideBar ();
        
    }
    
    public JComponent getSideBarPanel ()
    {
        
        if (this.splitPane.getLeftComponent ().equals (this.sideBar))
        {
            
            return this.sideBar;
            
        }
        
        return (JComponent) this.splitPane.getRightComponent ();
        
    }

    private void fireMainPanelShownEvent (QuollPanel p)
    {
        
        MainPanelEvent ev = new MainPanelEvent (this,
                                                p);
        
        for (MainPanelListener l : this.mainPanelListeners)
        {

            l.panelShown (ev);
            
        }
        
    }
    
    public void removeMainPanelListener (MainPanelListener l)
    {

        this.mainPanelListeners.remove (l);

    }
    
    public void addMainPanelListener (MainPanelListener l)
    {
        
        this.mainPanelListeners.add (l);
        
    }
    
    private void fireSideBarShownEvent (String name)
    {
        
        SideBarEvent ev = new SideBarEvent (this,
                                            name);
        
        for (SideBarListener l : this.sideBarListeners)
        {
            
            l.sideBarShown (ev);
            
        }
        
    }
    
    public void removeSideBarListener (SideBarListener l)
    {
        
        this.sideBarListeners.remove (l);
        
    }
    
    public void addSideBarListener (SideBarListener l)
    {
        
        this.sideBarListeners.add (l);
        
    }
    
    public void removeSideBar (AbstractSideBar panel)
    {
        
        this.sideBars.remove (panel.getName ());
        
        if (panel == this.currentSideBar)
        {
            
            this.sideBarWrapper.removeAll ();
            
        }
                
        this.showMainSideBar ();
        
    }
    
    public void addSideBar (String          name,
                            AbstractSideBar panel)
    {
        
        if (panel == null)
        {
            
            throw new IllegalArgumentException ("No panel provided for: " + name);
            
        }
        
        panel.init ();
        
        panel.setName (name);
        
        this.sideBars.put (name,
                           panel);
        
    }
    
    public boolean isDistractionFreeModeEnabled ()
    {
        
        if (this.fsf != null)
        {
            
            return this.fsf.isDistractionFreeModeEnabled ();
            
        }
        
        return false;
        
    }
    
    public void exitFullScreen ()
    {
        
        if (this.fsf != null)
        {
            
            this.fsf.close ();

            this.tabs.setVisible (true);     
            
            this.fullScreenOverlay.setVisible (false);
            
        }
        
    }
    
    public void closeFind ()
    {
        
        this.showMainSideBar ();
        
    }

    public void viewWordCounts ()
    {
        
        this.showSideBar ("wordcounts");

        this.fireProjectEvent (ProjectEvent.WORD_COUNTS,
                               ProjectEvent.SHOW);

        this.fireProjectEvent (ProjectEvent.READABILITY,
                               ProjectEvent.SHOW);
        
    }
    
    public void showFind (String text)
    {
        
        this.showSideBar ("find");
        
        if (text == null)
        {
            
            QuollPanel qp = this.getCurrentlyVisibleTab ();
            
            if (qp instanceof AbstractEditorPanel)
            {
                
                text = ((AbstractEditorPanel) qp).getEditor ().getSelectedText ();
                
            }
            
        }
        
        this.finder.onShow (text);
                
    }
    
    public boolean isMainSideBarName (String n)
    {
        
        return (n.equals ("project"));
        
    }
    
    public void showMainSideBar ()
    {
        
        this.showSideBar ("project");
        
    }
    
    public Component getCurrentSideBarCard ()
    {
        
        return this.currentSideBar;
                
    }
        
    public void showSideBar (String name)
    {

        if ((this.currentSideBar != null)
            &&
            (this.currentSideBar.getName ().equals (name))
           )
        {

            if (this.fsf != null)
            {
                
                this.fsf.showSideBar ();
                
            }
            
            return;
            
        }
    
        AbstractSideBar b = this.sideBars.get (name);
        
        if (b == null)
        {
            
            throw new IllegalArgumentException ("Unable to show sidebar: " +
                                                name +
                                                ", no sidebar found with that name.");
            
        }
        
        this.finder.clearHighlight ();
                
        // Get the current sidebar.
        if (this.currentSideBar != null)
        {
        
            this.sideBarWidths.put (this.currentSideBar.getName (),
                                    this.currentSideBar.getSize ().width);

        }
                                    
        this.sideBarWrapper.removeAll ();
        this.sideBarWrapper.add (b);
                           
        this.sideBar.validate ();
        this.sideBar.repaint ();

        // See if we have a width for the sidebar.
        Integer w = this.sideBarWidths.get (name);
        
        if ((w == null)
            ||
            (w == 0)
           )
        {
            
            w = b.getPreferredSize ().width;

        }

        int divLoc = -1;
        
        if (w != null)
        {
            
            if (this.splitPane.getLeftComponent () == this.sideBar)
            {
                
                divLoc = w;
                                
            } else {
                
                divLoc = this.splitPane.getSize ().width - w;
                                
            }
            
        } else {
            
            divLoc = b.getPreferredSize ().width;
            
        }

        int sw = b.getPreferredSize ().width;
        
        if (divLoc < sw)
        {
            
            sw = divLoc;
            
        }
        
        int tbw = this.toolbarPanel.getPreferredSize ().width;
        
        if (divLoc < tbw)
        {
            
            divLoc = tbw;
            
        }
        
        this.splitPane.setDividerLocation (divLoc);            
        
        this.currentSideBar = b;
                      
        this.fireSideBarShownEvent (name);

        if (this.fsf != null)
        {
            
            this.fsf.showSideBar ();
            
        }
        
    }
    
    public Map getTempOptions ()
    {
        
        return this.tempOptions;
        
    }

    public boolean hasTempOption (String name)
    {
        
        return this.getTempOption (name) != null;
        
    }

    public void setSplitPaneColor (Color c)
    {

    
        ((javax.swing.plaf.basic.BasicSplitPaneUI) this.splitPane.getUI ()).getDivider ().setBackground (Environment.getBorderColor ());
        
        this.validate ();
        this.repaint ();
        
    }
    
    public void setSplitPaneColor ()
    {

        this.setSplitPaneColor (UIUtils.getComponentColor ());
                
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

    public void addTabChangeListener (ChangeListener cl)
    {
        
        this.tabs.addChangeListener (cl);
        
    }
    
    private void updateToolbarForPanel (QuollPanel qp)
    {
        
        if (qp != null)
        {

            this.toolbarPanel.removeAll ();

            JToolBar tb = qp.getToolBar ((this.fsf != null));
                
            if (tb != null)
            {

                this.toolbarPanel.add (tb);

                if (tb.getComponentCount () == 0)
                {

                    this.setToolbarVisible (false);

                } else
                {

                    this.setToolbarVisible (true);

                }

            }

            this.toolbarPanel.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                             this.toolbarPanel.getPreferredSize ().height));

            this.toolbarPanel.revalidate ();
            this.toolbarPanel.repaint ();
            
        } else
        {

            this.setToolbarVisible (false);

        }        
        
    }
    
    private DnDTabbedPane createTabbedPane ()
    {
        
        final AbstractProjectViewer _this = this;
        
        final DnDTabbedPane p = new DnDTabbedPane ();

        p.setTabLayoutPolicy (JTabbedPane.SCROLL_TAB_LAYOUT);
        
        p.addChangeListener (new ChangeAdapter ()
        {
           
            public void stateChanged (ChangeEvent ev)
            {
                
                _this.updateToolbarForPanel (_this.getCurrentlyVisibleTab ());
                
            }
            
        });
        
        return p;
        
    }
        
    public abstract TypesHandler getObjectTypesHandler (String objType);
        
    public abstract void reloadTreeForObjectType (String objType);
        
    public abstract void reloadTreeForObjectType (NamedObject obj);

    public abstract void showObjectInTree (String      treeObjType,
                                           NamedObject obj);
    
    public abstract AbstractSideBar getMainSideBar ();
        
    public abstract String getViewerTitle ();

    public abstract String getViewerIcon ();

    public abstract String getChapterObjectName ();

    public abstract void fillFullScreenTitleToolbar (JToolBar toolbar);
    
    public abstract void fillTitleToolbar (JToolBar toolbar);
    
    public abstract void fillSettingsPopup (JPopupMenu popup);

    public abstract void doSaveState ();

    public abstract void handleOpenProject ()
                                     throws Exception;

    public abstract void handleNewProject ()
                                    throws Exception;

    public abstract boolean viewObject (DataObject d);

    public abstract boolean viewObject (DataObject     d,
                                        ActionListener doAfterView);

    public abstract boolean openPanel (String id);

    public abstract void deleteChapter (Chapter c);

    public abstract void deleteObject (NamedObject o)
                                       throws      GeneralException;
    
    public abstract void deleteObject (NamedObject o,
                                       boolean     deleteChildObjects)
                                       throws GeneralException;
    
    public abstract void updateChapterIndexes (Book   b)
                                               throws GeneralException;
    
    public abstract void handleItemChangedEvent (ItemChangedEvent ev);

    public ActionMap getActionMap ()
    {

        return this.splitPane.getActionMap ();

    }

    public InputMap getInputMap (int m)
    {

        return this.splitPane.getInputMap (m);

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

        Notification n = new Notification (this,
                                           comp,
                                           iconType,
                                           duration,
                                           buttons);

        this.addNotification (n);

        return n;

    }

    public void removeNotification (Notification n)
    {

        // Remove the box.
        this.notifications.remove (n);

        if (this.notifications.getComponentCount () == 0)
        {

            this.notifications.setVisible (false);

        }

        this.notifications.getParent ().validate ();
        this.notifications.getParent ().repaint ();                

    }

    public void addNotification (Notification n)
    {
        
        if (this.notifications.getComponentCount () > 0)
        {
            
            n.setBorder (new CompoundBorder (new MatteBorder (0,
                                                              0,
                                                              1,
                                                              0,
                                                              UIUtils.getBorderColor ()),
                                             n.getBorder ()));
            
        }
        
        this.notifications.add (n,
                                0);

        this.notifications.setVisible (true);

        n.init ();
        
        this.validate ();
        this.repaint ();        
        
    }

    public Notification addNotification (String text,
                                         String type,
                                         int    duration)
    {

        JTextPane p = UIUtils.createHelpTextPane (text,
                                                  this);

        p.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         Short.MAX_VALUE));
        p.setBorder (null);

        return this.addNotification (p,
                                     type,
                                     duration);

    }

    public Action getAction (int name)
    {

        return this.getAction (name,
                               null);

    }
    
    public void reinitAllChapterEditors ()
    {

        this.doForPanels (AbstractEditorPanel.class,
                          new DefaultQuollPanelAction ()
                          {
                            
                                public void doAction (QuollPanel qp)
                                {
                                    
                                    ((AbstractEditorPanel) qp).initEditor ();
                                    
                                }
                            
                          });

    }

    public void scrollTo (final Chapter c,
                          final int     pos)
    {

        final AbstractProjectViewer _this = this;

        this.viewObject (c);

        SwingUtilities.invokeLater (new Runner ()
        {

            public void run ()
            {

                try
                {

                    _this.getEditorForChapter (c).scrollToPosition (pos);
                    _this.getEditorForChapter (c).scrollToPosition (pos);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to scroll to position: " +
                                          pos,
                                          e);

                }

            }

        });

    }
    
    public AbstractEditorPanel getEditorForChapter (Chapter c)
    {
    
        return (AbstractEditorPanel) this.getQuollPanelForObject (c);

    }

    public Action getAction (int               name,
                             final NamedObject other)
    {

        final AbstractProjectViewer pv = this;

        if (name == AbstractProjectViewer.EDIT_CHAPTER_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pv.viewObject ((Chapter) other);

                }

            };

        }

        if (name == AbstractProjectViewer.SHOW_STATISTICS_ACTION)
        {
            
            return new ActionAdapter ()
            {            

                public void actionPerformed (ActionEvent ev)
                {
                    
                    pv.viewWordCountHistory ();
                    
                }
                
            };
            
        }
        
        if (name == AbstractProjectViewer.RENAME_CHAPTER_ACTION)
        {

            return new RenameChapterActionHandler ((Chapter) other,
                                                   pv);

        }

        if (name == AbstractProjectViewer.WARMUP_EXERCISE_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (final ActionEvent ev)
                {

                    pv.showWarmupPromptSelect ();

                }

            };

        }

        if (name == AbstractProjectViewer.DELETE_CHAPTER_ACTION)
        {

            return new DeleteChapterActionHandler ((Chapter) other,
                                                   pv);

        }

        if (name == AbstractProjectViewer.CREATE_PROJECT_SNAPSHOT_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    UIUtils.createQuestionPopup (pv,
                                                 "Create Snapshot",
                                                 Constants.SNAPSHOT_ICON_NAME,
                                                 "Please confirm you wish to create a snapshot of this {project}.",
                                                 "Yes, create it",
                                                 null,
                                                 new ActionAdapter ()
                                                 {
                                                    
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        try
                                                        {
                                
                                                            File f = pv.dBMan.createBackup ();
                                
                                                            UIUtils.showMessage (pv,
                                                                                 "A snapshot has been created and written to:\n\n" + f.getPath ());
                                
                                                        } catch (Exception e)
                                                        {
                                
                                                            Environment.logError ("Unable to create snapshot of project: " +
                                                                                  pv.getProject (),
                                                                                  e);
                                
                                                            UIUtils.showErrorMessage (pv,
                                                                                      "Unable to create snapshot.");
                                
                                                        }
                                                        
                                                    }
                                                    
                                                },
                                                null,
                                                null);

                }

            };

        }

        if (name == AbstractProjectViewer.RENAME_PROJECT_ACTION)
        {

            return new RenameProjectActionHandler (this);

        }

        if (name == AbstractProjectViewer.OPEN_PROJECT_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    FindOrOpen f = new FindOrOpen (FindOrOpen.SHOW_OPEN);

                    f.pack ();

                    UIUtils.setCenterOfScreenLocation (f);

                    f.setVisible (true);

                }

            };

        }

        if (name == AbstractProjectViewer.NEW_PROJECT_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    FindOrOpen f = new FindOrOpen (FindOrOpen.SHOW_NEW);

                    f.pack ();

                    UIUtils.setCenterOfScreenLocation (f);

                    f.setVisible (true);

                }

            };

        }

        if (name == AbstractProjectViewer.CLOSE_PROJECT_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pv.close (false,
                              null);

                }

            };

        }

        if (name == AbstractProjectViewer.EDIT_PROJECT_PROPERTIES_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pv.showOptions ();

                }

            };

        }

        if (name == AbstractProjectViewer.DELETE_PROJECT_ACTION)
        {

            return new DeleteProjectActionHandler (this);

        }

        if (name == AbstractProjectViewer.REPORT_BUG_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pv.showReportProblem ();

                }

            };

        }

        if (name == AbstractProjectViewer.CONTACT_SUPPORT_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pv.showContactSupport ();

                }

            };

        }

        if (name == AbstractProjectViewer.ABOUT_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    new About (pv).init ();

                    pv.fireProjectEvent (ProjectEvent.ABOUT,
                                         ProjectEvent.SHOW);

                }

            };

        }

        return null;

    }

    public void updateProjectDictionaryForNames (Set<String> oldNames,
                                                 NamedObject object)
    {
        
        if (!(object instanceof Asset))
        {
            
            return;
            
        }

        for (String nn : oldNames)
        {

            if (nn == null)
            {
                
                continue;
                
            }

            if (nn.trim ().length () == 0)
            {
                
                continue;
                
            }

            try
            {

                this.removeWordFromDictionary (nn,
                                               "project");
                this.removeWordFromDictionary (nn + "'s",
                                               "project");

            } catch (Exception e) {}

        }
    
        for (String nn : object.getAllNames ())
        {
    
            // Add the name.
            try
            {

                this.addWordToDictionary (nn,
                                          "project");
                this.addWordToDictionary (nn + "'s",
                                          "project");

            } catch (Exception e) {}

        }
        
    }

    public void removeWordFromDictionary (String w,
                                          String type)
    {

        if (this.dictProv != null)
        {

            this.dictProv.removeWord (w,
                                      type);

        }

    }

    public void addWordToDictionary (String w,
                                     String type)
    {

        if (this.dictProv != null)
        {

            this.dictProv.addWord (w,
                                   type);

        }

    }

    public void setDictionaryProvider (DictionaryProvider dp)
    {

        throw new UnsupportedOperationException ("Not supported.");

    }

    public void checkSpelling ()
    {

        throw new UnsupportedOperationException ("Not supported.");

    }

    public void showWarmupPromptSelect ()
    {
        
        WarmupPromptSelect w = new WarmupPromptSelect (this);

        w.init ();
              
    }

    public void doForPanels (Class            type,
                             QuollPanelAction act)
    {
        
        for (QuollPanel p : this.panels.values ())
        {
            
            if (type.isAssignableFrom (p.getClass ()))
            {
                
                act.doAction (p);
                
            }
            
        }
        
    }
    
    public void scheduleAutoSaveForAllEditors ()
    {

        this.doForPanels (AbstractEditorPanel.class,
                          new DefaultQuollPanelAction ()
                          {
                            
                              public void doAction (QuollPanel p)
                              {

                                  ((AbstractEditorPanel) p).scheduleAutoSave ();
                                
                              }
                            
                          });

    }
    
    public boolean isHighlightWritingLine ()
    {
        
        return this.proj.getPropertyAsBoolean (Constants.EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME);
        
    }
        
    public void setHighlightWritingLine (final boolean s)
    {
                
        this.doForPanels (AbstractEditorPanel.class,
                          new DefaultQuollPanelAction ()
                          {
                            
                                public void doAction (QuollPanel p)
                                {
                                    
                                    ((AbstractEditorPanel) p).getEditor ().setHighlightWritingLine (s);
                                    
                                }
                            
                          });
                
    }
    
    public void setKeyStrokeSoundFile (File    f,
                                       boolean play)
                                throws Exception
    {

        this.playSoundOnKeyStroke = play;

        this.fireProjectEvent (ProjectEvent.TYPE_WRITER_SOUND,
                               (play ? ProjectEvent.ON : ProjectEvent.OFF));

        // See if the user has specified a sound.
        if (this.playSoundOnKeyStroke)
        {

            try
            {

                InputStream is = null;

                if ((f != null) &&
                    (f.exists ()))
                {

                    is = new BufferedInputStream (new FileInputStream (f));

                }

                if (is == null)
                {

                    // Play the default.
                    is = new BufferedInputStream (Environment.getResourceStream (Constants.DEFAULT_KEY_STROKE_SOUND_FILE));

                }

                // Get the clip.
                AudioInputStream ais = AudioSystem.getAudioInputStream (is);

                this.keyStrokeSound = AudioSystem.getClip ();

                this.keyStrokeSound.open (ais);

            } catch (Exception e)
            {

                Environment.logError ("Unable to get sound file to play on key stroke",
                                      e);

            }

        }

    }

    /**
     * Done here so that we don't have multiple clips taking up memory, the user can
     * only type in one editor at once.
     */
    public void playKeyStrokeSound ()
    {

        if (!this.playSoundOnKeyStroke)
        {

            return;

        }

        try
        {

            if (this.keyStrokeSound.isRunning ())
            {

                this.keyStrokeSound.stop ();

            }

            this.keyStrokeSound.setFramePosition (0);

            this.keyStrokeSound.start ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to play key stroke sound",
                                  e);

            this.playSoundOnKeyStroke = false;

        }

    }

    private String getProjectDictionary ()
    {

        return null;

    }

    public boolean hasDictionaryFiles (String lang)
                                       throws IOException
    {
        
        return this.getDictionaryFiles (lang) != null;
        
    }
    
    public java.util.List<InputStream> getDictionaryFiles (String lang)
                                                           throws IOException
    {
        
        java.util.List<InputStream> dictFiles = new ArrayList<InputStream> ();

        if (lang == null)
        {
            
            return null;
            
        }
        
        String dFiles = this.proj.getProperty (lang + "DictionaryFiles");

        if (dFiles != null)
        {

            StringTokenizer t = new StringTokenizer (dFiles,
                                                     ",");

            while (t.hasMoreTokens ())
            {

                String tok = t.nextToken ().trim ();

                InputStream in = Environment.class.getResourceAsStream (Constants.DICTIONARIES_DIR + tok);
                
                if (in != null)
                {
                    
                    dictFiles.add (in);
                    
                }

            }

        }
                
        // Add all files from the dictionary directory (if it exists)
        File dir = Environment.getDictionaryDirectory (lang);
        
        if (dir.exists ())
        {
            
            // Load all files.
            File[] files = dir.listFiles ();
            
            for (int i = 0; i < files.length; i++)
            {
                
                if (files[i].isFile ())
                {
                    
                    dictFiles.add (new FileInputStream (files[i]));
                    
                }
                
            }
            
        }
        
        if (dictFiles.size () == 0)
        {
            
            return null;
            
        }

        if (Environment.isEnglish (lang))
        {
            
            if (!lang.equals (Constants.ENGLISH))
            {
            
                java.util.List<InputStream> enDictFiles = this.getDictionaryFiles (Constants.ENGLISH);
            
                if (enDictFiles != null)
                {
            
                    dictFiles.addAll (enDictFiles);
                    
                }
                
            }
            
        }
        
        return dictFiles;
        
    }
    
    public void setSpellCheckLanguage (String  lang,
                                       boolean updateEditors)
                                throws GeneralException,
                                       IOException
    {

    
        if ((this.spellCheckLanguage != null) &&
            (this.spellCheckLanguage.equals (lang)))
        {

            // Not changing.
            return;

        }

        if (lang == null)
        {
            
            // Basically turning off spellchecking.
            this.dictProv = null;

            this.doForPanels (SpellCheckSupported.class,
                              new DefaultQuollPanelAction ()
                              {
                                
                                    public void doAction (QuollPanel panel)
                                    {
                                        
                                        AbstractEditorPanel s = (AbstractEditorPanel) panel;
                                        
                                        s.setDictionaryProvider (null);
                                        
                                        s.setSynonymProvider (null);
                                        
                                    }
                                
                              });
            
            return;
            
        }
        
        java.util.List<InputStream> dictFiles = this.getDictionaryFiles (lang);

        File userDict = Environment.getUserDictionaryFile ();

        if (!userDict.exists ())
        {

            userDict.createNewFile ();

        }

        java.util.List<String> names = new ArrayList ();

        boolean isEnglish = Environment.isEnglish (lang);
        
        // Get the names from the assets.
        Set<NamedObject> objs = this.proj.getAllNamedChildObjects (Asset.class);

        for (NamedObject o : objs)
        {

            Set<String> onames = o.getAllNames ();
            
            for (String n : onames)
            {

                // Get the name, add it.
                names.add (n);
                
                if (isEnglish)
                {
                
                    names.add (n + "'s");
                    
                }

            }

        }

        this.dictProv = new DictionaryProvider (dictFiles,
                                                names,
                                                userDict);

        final AbstractProjectViewer _this = this;
                                                
        if (updateEditors)
        {

            this.doForPanels (AbstractEditorPanel.class,
                              new DefaultQuollPanelAction ()
                              {
                                
                                    public void doAction (QuollPanel panel)
                                    {
                                        
                                        AbstractEditorPanel s = (AbstractEditorPanel) panel;
                                        
                                        s.setDictionaryProvider (_this.dictProv);
                                        
                                        try
                                        {
                                        
                                            s.setSynonymProvider (_this.getSynonymProvider ());
                                            
                                        } catch (Exception e) {
                                            
                                            Environment.logError ("Unable to set synonym provider",
                                                                  e);
                                            
                                        }
                                        
                                    }
                                
                              });

        }

    }

    public ReadabilityIndices getReadabilityIndices (String  text)
    {
                
        ReadabilityIndices ri = new ReadabilityIndices ();

        if (text != null)
        {
        
            ri.add (text);            
            
        }

        return ri;
        
        
    }    

    public String getWordTypes (String word)
                                throws GeneralException
    {

        if (this.synProv != null)
        {
            
            return this.synProv.getWordTypes (word);
            
        }
        
        return null;

    }
    
    public Synonyms getSynonyms (String word)
                                 throws GeneralException
    {
        
        if (this.synProv != null)
        {
            
            return this.synProv.getSynonyms (word);
            
        }
        
        return null;
        
    }

    public boolean synonymLookupsSupported ()
    {

        return this.synProv != null;

    }
    
    public SynonymProvider getSynonymProvider ()
                                               throws Exception
    {
        
        if (this.synProv != null)
        {
            
            return this.synProv;
            
        }
        
        this.synProv = Environment.getSynonymProvider (this.getSpellCheckLanguage ());
        
        return this.synProv;
        
    }

    public boolean isLanguageFunctionAvailable ()
    {
        
        if (!this.isLanguageEnglish ())
        {
            
            this.showNotificationPopup ("Function unavailable",
                                        "Sorry, this function is only available when your spellchecker language is English.<br /><br /><a href='action:contact'>Click here to contact me to help add support for your language</a>",
                                        20);
            
            return false;
            
        }
        
        return true;
        
    }
    
    public boolean isLanguageEnglish ()
    {
        
        return Environment.isEnglish (this.getSpellCheckLanguage ());
        
    }
    
    public String getSpellCheckLanguage ()
    {
        
        String c = this.proj.getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME);
        
        // Get the property.
        if (c == null)
        {

            c = Environment.getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME);

        }

        return c;        
        
    }
    
    public DictionaryProvider getDictionaryProvider ()
                                              throws Exception
    {

        if (this.dictProv != null)
        {

            return this.dictProv;

        }

        this.setSpellCheckLanguage (this.getSpellCheckLanguage (),
                                    false);

        return this.dictProv;

    }

    /*
    public void setDictionaryProvider (DictionaryProvider prov)
    {

    this.dictProv = prov;

    }
     */
/*
    public void createProject (String name)
                               throws Exception
    {

        Project p = new Project (name);

        File projDir = new File (dir.getPath () + "/" + Utils.sanitizeForFilename (p.getName ()));

        // Get the username and password.
        String username = Environment.getProperty (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = Environment.getProperty (Constants.DB_PASSWORD_PROPERTY_NAME);

        this.dBMan = new ObjectManager ();
        this.dBMan.init (new File (projDir.getPath () + "/projectdb"),
                         username,
                         password,
                         filePassword);

        // Create a file that indicates that the directory can be deleted.
        Utils.createQuollWriterDirFile (projDir);

        // Create one in the "projectdb.lobs.db" dir as well (if present).
        Utils.createQuollWriterDirFile (new File (projDir.getPath () + "/projectdb.lobs.db"));

        Book b = new Book (p,
                           p.getName ());

        p.addBook (b);

        p.setProjectDirectory (projDir);
        p.setEncrypted (false);

        try
        {

            this.saveObject (p,
                             true);

        } catch (Exception e) {

            Environment.logError ("Unable to create new project: " +
                                  p,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to create " + name + " project");

            return;

        }

        this.proj = p;

    }
*/
/*
    public void setTabsLocation (String loc)
    {

        if (loc == null)
        {

            loc = Constants.BOTTOM;

        }

        if (loc.equals (Constants.TOP))
        {
        
            this.tabs.setTabPlacement(SwingConstants.TOP);
            
        } else {
            
            this.tabs.setTabPlacement(SwingConstants.BOTTOM);
            
        }
        
    }
*/
    public void setToolbarLocation (String loc)
    {

        if (loc == null)
        {

            loc = Constants.BOTTOM;

        }

        if (loc.equals (Constants.TOP))
        {

            this.toolbarPanel.setBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getBorderColor ()),
                                                             new EmptyBorder (0,
                                                                              5,
                                                                              0,
                                                                              5)));

            this.sideBar.add (this.toolbarPanel,
                              0);

        } else
        {

            this.toolbarPanel.setBorder (new CompoundBorder (new MatteBorder (1, 0, 0, 0, UIUtils.getBorderColor ()),
                                                             new EmptyBorder (0,
                                                                              5,
                                                                              0,
                                                                              5)));
/*
            this.toolbarPanel.setBorder (new EmptyBorder (7,
                                                          7,
                                                          0,
                                                          0));
*/
            this.sideBar.add (this.toolbarPanel);

        }

        this.toolbarLocation = loc;

    }

    public void setTabsLocation (String loc)
    {
        
        if (loc == null)
        {
            
            loc = Constants.TOP;
            
        }
        
        int p = -1;
        
        Border b = null;
        
        if (loc.equals (Constants.TOP))
        {
            
            p = JTabbedPane.TOP;

            b = new EmptyBorder (7, 0, 0, 0);
                        
        }

        if (loc.equals (Constants.BOTTOM))
        {
            
            p = JTabbedPane.BOTTOM;

            b = new EmptyBorder (0, 0, 7, 0);
            
        }

        if (loc.equals (Constants.LEFT))
        {
            
            p = JTabbedPane.LEFT;
            
        }
        
        if (loc.equals (Constants.RIGHT))
        {
            
            p = JTabbedPane.RIGHT;
            
        }

        this.tabs.setBorder (b);
        
        this.tabs.setTabPlacement (p);
        
    }
    
    public void setSidebarLocation (String loc)
    {

        if (loc == null)
        {

            loc = Constants.LEFT;

        }

        boolean swap = false;
        
        if (loc.equals (Constants.LEFT))
        {

            if (!this.splitPane.getLeftComponent ().equals (this.sideBar))
            {
                
                swap = true;
                                
            }

        } else
        {

            if (!this.splitPane.getRightComponent ().equals (this.sideBar))
            {
                
                swap = true;
                                
            }
        
        }

        if (swap)
        {

            // Swap them.
            int dloc = this.splitPane.getDividerLocation ();
            
            Component left = this.splitPane.getLeftComponent ();
            
            Component right = this.splitPane.getRightComponent ();

            this.splitPane.setLeftComponent (null);
            this.splitPane.setRightComponent (null);
            
            this.splitPane.setLeftComponent (right);
            this.splitPane.setRightComponent (left);
            
            this.splitPane.setDividerLocation (this.splitPane.getSize ().width - dloc);            
            
        }
        
        this.sidebarLocation = loc;

    }    
    
    private void initWindow ()
    {
    
        this.setSpellCheckingEnabled (this.proj.getPropertyAsBoolean (Constants.SPELL_CHECKING_ENABLED_PROPERTY_NAME));

        // Check the height and width and ensure they aren't too big.
        // If they overrun the current screen size then reduce them down to 80% either way.
        Dimension ss = Toolkit.getDefaultToolkit ().getScreenSize ();

        int wWidth = this.proj.getPropertyAsInt (Constants.WINDOW_WIDTH_PROPERTY_NAME);

        if (wWidth > ss.width)
        {

            wWidth = (int) ((float) ss.width * 0.8f);

        }

        int wHeight = this.proj.getPropertyAsInt (Constants.WINDOW_HEIGHT_PROPERTY_NAME);

        if (wHeight > ss.height)
        {

            wHeight = (int) ((float) ss.height * 0.8f);

        }

        this.splitPane.setPreferredSize (new Dimension (wWidth,
                                                        wHeight));

        this.setSplitPaneColor ();
        
        this.setToolbarLocation (this.proj.getProperty (Constants.TOOLBAR_LOCATION_PROPERTY_NAME));

        this.setSidebarLocation (Environment.getUserProperties ().getProperty (Constants.SIDEBAR_LOCATION_PROPERTY_NAME));        

        this.setTabsLocation (this.proj.getProperty (Constants.TABS_LOCATION_PROPERTY_NAME));        

        this.splitPane.setDividerLocation (this.proj.getPropertyAsInt (Constants.SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME));
        
        UIUtils.setFrameTitle (this,
                               Environment.replaceObjectNames (this.getViewerTitle ()));

        this.setIconImage (Environment.getWindowIcon ().getImage ());

        this.pack ();

        UIUtils.setCenterOfScreenLocation (this);

        this.setVisible (true);

        Environment.doVersionCheck (this);

        if (this.isSpellCheckingEnabled ())
        {

            this.doForPanels (SpellCheckSupported.class,
                              new DefaultQuollPanelAction ()
                              {
                                
                                    public void doAction (QuollPanel p)
                                    {
                                        
                                        SpellCheckSupported s = (SpellCheckSupported) p;
                                        
                                        s.checkSpelling ();
                                        
                                    }
                                
                              });

        }

        this.playSoundOnKeyStroke = this.proj.getPropertyAsBoolean (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME);

        this.initKeyStrokeSound ();

    }

    private void initKeyStrokeSound ()
    {
        
        // See if the user has specified a sound.
        if (this.playSoundOnKeyStroke)
        {

            try
            {

                String sf = this.proj.getProperty (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);

                InputStream is = null;

                if (sf != null)
                {

                    File f = new File (sf);

                    if (f.exists ())
                    {

                        is = new BufferedInputStream (new FileInputStream (f));

                    }

                }

                if (is == null)
                {

                    // Play the default.
                    is = new BufferedInputStream (Environment.getResourceStream (Constants.DEFAULT_KEY_STROKE_SOUND_FILE));

                }

                // Get the clip.
                AudioInputStream ais = AudioSystem.getAudioInputStream (is);

                this.keyStrokeSound = AudioSystem.getClip ();

                this.keyStrokeSound.open (ais);

            } catch (Exception e)
            {

                Environment.logError ("Unable to get sound file to play on key stroke",
                                      e);

            }

        }        
        
    }

    public void newProject (File    dir,
                            Project p,
                            String  filePassword)
                     throws Exception
    {

        if (p == null)
        {

            throw new GeneralException ("No project provided.");

        }

        if (p.getName () == null)
        {

            throw new GeneralException ("No project name provided.");

        }

        this.setIgnoreProjectEvents (true);

        File projDir = new File (dir.getPath () + "/" + Utils.sanitizeForFilename (p.getName ()));

        // Get the username and password.
        String username = Environment.getProperty (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = Environment.getProperty (Constants.DB_PASSWORD_PROPERTY_NAME);

        this.dBMan = new ObjectManager ();
        this.dBMan.init (new File (projDir.getPath () + "/projectdb"),
                         username,
                         password,
                         filePassword);

        this.dBMan.setProject (p);

        // Create a file that indicates that the directory can be deleted.
        Utils.createQuollWriterDirFile (projDir);

        // Create one in the "projectdb.lobs.db" dir as well (if present).
        Utils.createQuollWriterDirFile (new File (projDir.getPath () + "/projectdb.lobs.db"));

        p.setProjectDirectory (projDir);
        p.setEncrypted (filePassword != null);

        this.proj = p;

        if ((this.proj.getBooks () == null) ||
            (this.proj.getBooks ().size () == 0) ||
            (this.proj.getBooks ().get (0).getChapters ().size () == 0))
        {

            Book b = null;

            if ((this.proj.getBooks () != null)
                &&
                (this.proj.getBooks ().size () > 0)
               )
            {

                b = this.proj.getBooks ().get (0);
                               
            } else {

                b = new Book (this.proj,
                              this.proj.getName ());
                
                this.proj.addBook (b);

            }

        }        
                
        Environment.addToAchievementsManager (this);

        this.initSideBars ();

        this.handleNewProject ();
        
        this.title.setTitle (Environment.replaceObjectNames (this.getViewerTitle ()));
        /* old
        this.title.setIcon (Environment.getIcon (this.getViewerIcon (),
                                                 true));
        */
        try
        {

            this.saveObject (p,
                             true);

        } catch (Exception e)
        {

            Environment.logError ("Unable to create new project: " +
                                  p,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to create new project: " + p.getName ());

            return;

        }

        // Register ourselves with the environment.
        try
        {

            Environment.addOpenedProject (this);

        } catch (Exception e)
        {

            Environment.logError ("Unable to add opened project (probably an error with the projects file): " +
                                  p,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to update projects list.  No action is required but this project may not appear in the projects list when you next open a project.<br /><br />More details can be found in the error log.");

        }

        this.setSpellCheckingEnabled (this.proj.getPropertyAsBoolean (Constants.SPELL_CHECKING_ENABLED_PROPERTY_NAME));

        this.initWindow ();

        this.setSplitPaneColor ();
        
        this.handleWhatsNew ();
        
        this.handleShowTips ();

        this.checkForDictionaryFiles ();        
        
        this.setIgnoreProjectEvents (false);

        this.fireProjectEvent (this.proj.getObjectType (),
                               ProjectEvent.NEW);

    }

    public void setToolbarVisible (boolean v)
    {

        this.toolbarPanel.setVisible (v);

    }

    public void newProject (File   dir,
                            String name,
                            String filePassword)
                     throws Exception
    {

        Project p = new Project (name);

        this.newProject (dir,
                         p,
                         filePassword);

    }


    public void openProject (Project p,
                             String  filePassword)
                      throws Exception
    {

        this.setIgnoreProjectEvents (true);

        // Get the username and password.
        String username = Environment.getProperty (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = Environment.getProperty (Constants.DB_PASSWORD_PROPERTY_NAME);

        if (p.isNoCredentials ())
        {

            username = null;
            password = null;
            filePassword = null;

        }

        if (!p.isEncrypted ())
        {

            filePassword = null;

        }

        this.dBMan = new ObjectManager ();

        this.dBMan.init (new File (p.getProjectDirectory ().getPath () + "/projectdb"),
                         username,
                         password,
                         filePassword);

        Startup.ss.incr (5);

        // Get the project.
        this.proj = this.dBMan.getProject ();

        Startup.ss.incr (5);

        this.proj.setProjectDirectory (p.getProjectDirectory ());
        this.proj.setFilePassword (filePassword);
        this.proj.setEncrypted (p.isEncrypted ());
        this.proj.setNoCredentials (p.isNoCredentials ());

        this.proj.addPropertyChangedListener (this,
                                              null);

        Startup.ss.incr (5);

        Environment.addToAchievementsManager (this);

        this.initSideBars ();
                
        this.handleOpenProject ();

        Startup.ss.incr (5);

        // this.title.setTitle (this.proj.getName ());
        this.title.setTitle (Environment.replaceObjectNames (this.getViewerTitle ()));
        
        
        this.title.setIcon (Environment.getIcon (this.getViewerIcon (),
                                                 Constants.ICON_TITLE));

                                                 
        final java.util.List<Runner> inits = new ArrayList ();

        // Setup the tabs.
        String openTabs = this.proj.getProperty (Constants.OPEN_TABS_PROPERTY_NAME);

        if (openTabs != null)
        {

            java.util.List<String> objIds = new ArrayList ();
        
            // Split on :
            StringTokenizer t = new StringTokenizer (openTabs,
                                                     ",");

            while (t.hasMoreTokens ())
            {

                String tok = t.nextToken ().trim ();

                objIds.add (tok);
                
            }
                
            Collections.reverse (objIds);
                
            for (String panelId : objIds)
            {
                
                ObjectReference r = null;

                try
                {

                    r = ObjectReference.parseObjectReference (panelId);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to parse: " +
                                          panelId,
                                          e);

                    continue;

                }

                // Pass it to the project.
                final DataObject d = this.proj.getObjectForReference (r);

                if (d == null)
                {

                    if (!this.openPanelInt (panelId))
                    {
                        
                        Environment.logError ("Unable to open panel for id: " +
                                              panelId);

                        continue;
                                                
                    }

                } else
                {

                    if (!this.viewObject (d))
                    {

                        continue;

                    }

                }

            }

        }

        Startup.ss.incr (5);

        this.initWindow ();
        
        String lastOpen = this.proj.getProperty (Constants.LAST_OPEN_TAB_PROPERTY_NAME);

        if (lastOpen != null)
        {

            final QuollPanel qp = this.getQuollPanel (lastOpen);

            if (qp != null)
            {
            
                //final String state = this.proj.getProperty (lastOpen + "-state");

                final AbstractProjectViewer _this = this;

                inits.add (new Runner ()
                {

                    public void run ()
                    {

                        _this.setPanelVisible (qp);
                        
                        //qp.validate ();
                        //qp.repaint ();

                    }

                });
                
            }
            
            this.setSplitPaneColor ();

        }

        // Init all the panels.
        for (QuollPanel qp : this.panels.values ())
        {
            
            Runner r = this.getInitPanelStateRunner (qp,
                                                     lastOpen.equals (qp.getPanelId ()));
            
            if (r != null)
            {
            
                inits.add (r);
                
            }
            
        }
        
        SwingUtilities.invokeLater (new Runner ()
            {

                public void run ()
                {

                    for (Runner r : inits)
                    {

                        r.run ();

                    }

                }

            });

        Startup.ss.incr (5);

        // See if the properties say that we should produce a snapshot.
        if ((this.proj.getPropertyAsBoolean (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME)) &&
            (this.proj.getLastEdited () != null))
        {

            // Check to see how long it has been since the last update.
            long t = System.currentTimeMillis ();

            if ((t - this.proj.getLastEdited ().getTime ()) > Utils.getTimeAsMillis (this.proj.getProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME)))
            {

                // Create a snapshot.
                File bf = null;

                try
                {

                    bf = this.dBMan.createBackup ();

                    this.addNotification ("An automatic snapshot has been created.  <a href='" + bf.getParentFile ().toURI ().toURL () + "'>Click to view the backup directory.</a>",
                                          "information",
                                          30);

                    /*
                    UIUtils.showMessage (this,
                                 "An automatic snapshot has been created of the Project and written to:\n\n" + bf);
                     */

                } catch (Exception e)
                {

                    Environment.logError ("Unable to create backup for project: " +
                                          this.proj,
                                          e);

                    UIUtils.showErrorMessage (this,
                                              "Unable to create snapshot.");

                }

            }

        }

        Startup.ss.incr (5);

        this.dBMan.createActionLogEntry (this.proj,
                                         "Opened project",
                                         null,
                                         null);

        final AbstractProjectViewer _this = this;

        SwingUtilities.invokeLater (new Runner ()
            {

                public void run ()
                {

                    // Get the word counts, don't calc the a4 pages count.
                    _this.startWordCounts = _this.getAllChapterCounts (false);

                }

            });

        // Check the font used for the project and inform the user if we don't have it.
        // We do it here so it's done once.
        String f = this.proj.getProperty (Constants.EDITOR_FONT_PROPERTY_NAME);

        if (f != null)
        {

            Font ft = new Font (f,
                                Font.PLAIN,
                                12);

            if (!ft.getFamily ().equalsIgnoreCase (f))
            {

                UIUtils.showMessage (this,
                                     "The font <b>" + f +
                                     "</b> selected for use in {chapters} is not available on this computer.<br /><br />To select a new font, switch to a chapter tab then <a href='action:textproperties'>click here to change the text properties</a>.");

            }

        }

        this.handleWhatsNew ();
        
        this.handleShowTips ();

        // Check to see if we have the dictionary files, if not (this may be a project shared for a dropbox user)
        // download them automatically.
        this.checkForDictionaryFiles ();
                
        this.setIgnoreProjectEvents (false);

        this.fireProjectEvent (this.proj.getObjectType (),
                               ProjectEvent.OPEN);
                               
    }
    
    private void checkForDictionaryFiles ()
    {
        
        try
        {
        
            if (!this.hasDictionaryFiles (this.getSpellCheckLanguage ()))
            {
                
                final AbstractProjectViewer _this = this;
                
                final String lang = this.getSpellCheckLanguage ();
                
                // Download them.
                this.downloadDictionaryFiles (this.getSpellCheckLanguage (),
                                              new ActionAdapter ()
                                              {
                                                
                                                  public void actionPerformed (ActionEvent ev)
                                                  {
                                                    
                                                      try
                                                      {
                                                    
                                                            _this.setSpellCheckLanguage (null,
                                                                                         true);
                                                            
                                                            _this.setSpellCheckLanguage (lang,
                                                                                         true);
                                                            
                                                      } catch (Exception e) {
                                                        
                                                          Environment.logError ("Unable to set spell check language to: " +
                                                                                lang,
                                                                                e);
                                                        
                                                          UIUtils.showErrorMessage (_this,
                                                                                    "Unable to set spell check language to <b>" +
                                                                                    lang +
                                                                                    "</b>.<br /><br />Please contact Quoll Writer support for assistance.");
                                                        
                                                      }
                                                    
                                                  }
                                                
                                              });
                
            }        

        } catch (Exception e) {
            
            Environment.logError ("Unable to check for spell check language",
                                  e);
            
        }
        
    }
    
    public void downloadDictionaryFiles (String         lang,
                                         final ActionListener onComplete)
    {

        if (Environment.isEnglish (lang))
        {
            
            // See if we already have a set of english language files.
            if (Environment.hasEnglishDictionaryFiles ())
            {
                
                // So we only need the country specific files.
                lang += "-only";
                
            }
            
        }

        final String langOrig = lang;
        final String language = lang;
        
        URL url = null;
        
        try
        {
            
            url = new URL (Environment.getQuollWriterWebsite () + "/" + StringUtils.replaceString (Environment.getProperty (Constants.QUOLL_WRITER_LANGUAGE_FILES_URL_PROPERTY_NAME),
                                                                                                   "[[LANG]]",
                                                                                                   StringUtils.replaceString (language,
                                                                                                                              " ",
                                                                                                                              "%20")));
            
        } catch (Exception e) {
            
            UIUtils.showErrorMessage (this,
                                      "Unable to download language files");
            
            Environment.logError ("Unable to download language files, cant create url",
                                  e);

            return;            
            
        }
    
        // Create a temp file for it.
        File _file = null;
        
        try
        {

            _file = File.createTempFile ("quollwriter-language-" + language,
                                         null);
            
        } catch (Exception e) {
            
            UIUtils.showErrorMessage (this,
                                      "Unable to download language files");
            
            Environment.logError ("Unable to download language files, cant create temp file",
                                  e);

            return;
            
        }

        _file.deleteOnExit ();
                
        final File file = _file;
        
        Box b = new Box (BoxLayout.Y_AXIS);

        final JTextPane htmlP = UIUtils.createHelpTextPane ("The language files for <b>" + language + "</b> are now being downloaded.",
                                                            this);
        htmlP.setBorder (null);
        htmlP.setBackground (null);
        htmlP.setOpaque (false);
        htmlP.setAlignmentX (Component.LEFT_ALIGNMENT);

        b.add (htmlP);
        b.add (Box.createVerticalStrut (10));
        
        final JProgressBar prog = new JProgressBar (0, 100);
        
        prog.setPreferredSize (new Dimension (500, 25));
        prog.setMaximumSize (new Dimension (500, 25));
        prog.setAlignmentX (Component.LEFT_ALIGNMENT);

        b.add (prog);
                
        file.deleteOnExit ();

        final Notification n = this.addNotification (b,
                                                     Constants.DOWNLOAD_ICON_NAME,
                                                     -1,
                                                     null);
                
        final AbstractProjectViewer _this = this;
                
        final Runnable removeNotification = new Runnable ()
        {
        
            public void run ()
            {
                
               n.removeNotification ();
                
            }
        };
        
        final UrlDownloader downloader = new UrlDownloader (url,
                                                            file,
                                                            new DownloadListener ()
                                                            {
                                                        
                                                                public void handleError (Exception e)
                                                                {
                                                                    
                                                                    UIUtils.showErrorMessage (_this,
                                                                                              "A problem has occurred while downloading the language files for <b>" + langOrig + "</b>.<br /><br />Please contact Quoll Writer support for assistance.");
                                                                    
                                                                    Environment.logError ("Unable to download language files",
                                                                                          e);
                                                                    
                                                                    SwingUtilities.invokeLater (removeNotification);
                                                                    
                                                                }
                                                        
                                                                public void progress (final int downloaded,
                                                                                      final int total)
                                                                {

                                                                    SwingUtilities.invokeLater (new Runner ()
                                                                    {
                                                            
                                                                        public void run ()
                                                                        {
                                                                            
                                                                            int val = (int) (((double) downloaded / (double) total) * 100);
                                                                            
                                                                            prog.setValue (val);
                                                                            
                                                                        }
                                                                        
                                                                    });
                                                                    
                                                                }
                                                                
                                                                public void finished (int total)
                                                                {
                                                                                                                                        
                                                                    prog.setValue (100);
                                                                    prog.setIndeterminate (true);
                                                                    
                                                                    new Thread (new Runner ()
                                                                    {
                                                                    
                                                                        public void run ()
                                                                        {
                                                                    
                                                                            // Now extract the file into the relevant directory.
                                                                            try
                                                                            {

                                                                                Utils.extractZipFile (file,
                                                                                                      Environment.getUserQuollWriterDir ());
                                                                                
                                                                            } catch (Exception e) {
                                                                                
                                                                                Environment.logError ("Unable to extract language zip file: " +
                                                                                                      file +
                                                                                                      " to: " +
                                                                                                      Environment.getUserQuollWriterDir (),
                                                                                                      e);
                                                                                
                                                                                return;
                                                                                
                                                                            } finally {
        
                                                                                file.delete ();
                                                                                
                                                                            }
                                                                            
                                                                            if (onComplete != null)
                                                                            {
                                                                                                                                                    
                                                                                SwingUtilities.invokeLater (new Runner ()
                                                                                {
                                                                        
                                                                                    public void run ()
                                                                                    {
                                                                        
                                                                                        prog.setIndeterminate (false);                                                                    
                                                                                       
                                                                                        onComplete.actionPerformed (new ActionEvent (_this, 0, langOrig));
                                                                                        
                                                                                    }
                                                                                    
                                                                                });
                                                                                
                                                                            }

                                                                            SwingUtilities.invokeLater (removeNotification);
                                                                                                                                                        
                                                                        }
                                                                        
                                                                    }).start ();
                                                                    
                                                                }
                                                        
                                                            });
        
        downloader.start ();
                        
        n.addCancelListener (new ActionAdapter ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                downloader.stop ();
                
                file.delete ();

            }
            
        });
        
    }
    
    private void initPanelState (QuollPanel qp)
    {
        
        Runner r = this.getInitPanelStateRunner (qp,
                                                 true);
        
        if (r != null)
        {
            
            r.run ();
            
        }
        
    }
    
    private Runner getInitPanelStateRunner (final QuollPanel qp,
                                            final boolean    visible)
    {
                
        final String panelId = qp.getPanelId ();
        
        final String state = this.proj.getProperty (panelId + "-state");

        if (state != null)
        {

            final AbstractProjectViewer _this = this;

            // Need to "do later" since it won't always set the state correctly for
            // none visible tabs.
            return new Runner ()
            {

                public void run ()
                {

                    qp.setState (UIUtils.parseState (state),
                                                     visible);
                    
                }

            };

        }
        
        return null;
        
    }
    
    private void handleWhatsNew ()
    {
        
        boolean showWhatsNew = false;

        String whatsNewVersion = Environment.getProperty (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME);
    
        if (whatsNewVersion != null)
        {

            if (Environment.isNewVersionGreater (whatsNewVersion,
                                                 Environment.getQuollWriterVersion ()))
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
    
    public void showWhatsNew (boolean onlyShowCurrentVersion)
    {

        try
        {
        
            WhatsNew wn = new WhatsNew (this,
                                        onlyShowCurrentVersion);
            
            wn.init ();

            wn.toFront ();
            
        } catch (Exception e) {
            
            // Not good but not the end of the world but shouldn't stop things from going on.
            Environment.logError ("Unable to init whats new",
                                  e);
            
        }
   
    }
    
    private void handleShowTips ()
    {
        
        if ((this.tips != null)
            &&
            (Environment.getUserProperties ().getPropertyAsBoolean (Constants.SHOW_TIPS_PROPERTY_NAME))
           )
        {

            final AbstractProjectViewer _this = this;

            try
            {

                String tipText = this.tips.getNextTip ();
        
                final HTMLPanel htmlP = new HTMLPanel (tipText,
                                                       this);

                htmlP.setBackground (null);
                htmlP.setOpaque (false);
        
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
                            
                            htmlP.getParent ().validate ();
                            htmlP.getParent ().repaint ();

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
                                                     p);
                        
                    }
                    
                });
/*
                htmlP.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                     htmlP.getPreferredSize ().height));
                                                     */
                htmlP.setMinimumSize (htmlP.getPreferredSize ());
                
                htmlP.getParent ().validate ();
                htmlP.getParent ().repaint ();
/*
                n.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                 n.getPreferredSize ().height));                
  */              
                n.setMinimumSize (n.getPreferredSize ());
            } catch (Exception e) {
                
                Environment.logError ("Unable to show tips",
                                      e);
                
            }

        }        
        
    }
    
    public void showDictionaryManager ()
    {
        
        if (this.dictMan == null)
        {

            this.dictMan = new DictionaryManager (this);

            this.dictMan.init ();

            this.fireProjectEvent (ProjectEvent.PERSONAL_DICTIONARY,
                                   ProjectEvent.SHOW);

        }

        this.dictMan.setVisible (true);
        
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

            if (v.equals ("textproperties"))
            {
                
                QuollPanel qp = this.getCurrentlyVisibleTab ();

                if (qp instanceof AbstractEditorPanel)
                {

                    ((AbstractEditorPanel) qp).showTextProperties ();
                    
                }
            
                return;
                
            }

            if (v.equals ("whatsnew"))
            {
                
                this.showWhatsNew (true);

                return;
                
            }
            
            if (v.equals ("find"))
            {
                
                this.showFind (null);
                
                return;
                
            }

            if (v.equals ("achievements"))
            {
                
                this.viewAchievements ();
                
                return;
                
            }

            if (v.equals ("spellcheckoff"))
            {
                
                this.setSpellCheckingEnabled (false);
                
                return;
                                
            }

            if (v.equals ("spellcheckon"))
            {
                
                this.setSpellCheckingEnabled (true);
                
                return;
                                
            }

            if (v.equals ("warmup"))
            {
                
                this.showWarmupPromptSelect ();
                
                return;
                
            }

            if (v.equals ("statistics"))
            {
                
                this.viewWordCountHistory ();
                
                return;
                
            }
    
            if (v.equals ("wordcounts"))
            {
                                
                this.viewWordCounts ();
        
                return;
                
            }

            if (v.equals ("wordcounthistory"))
            {

                this.viewWordCountHistory ();
                
                return;
                                
            }
    
            if (v.equals ("projectoptions"))
            {
                
                this.showOptions ();
    
                return;
                
            }
    
            if (v.equals ("dictionarymanager"))
            {
                
                this.showDictionaryManager ();
    
                return;
                
            }
            
            if (v.equals ("enabletypewritersound"))
            {
                
                this.playSoundOnKeyStroke = !this.playSoundOnKeyStroke;
                
                this.initKeyStrokeSound ();
    
                return;
                
            }
            
            if (v.equals ("fullscreen"))
            {
    
                try
                {

                    this.enterFullScreen ();
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to show in full screen mode",
                                          e);
                    
                }
                
                return;
                
            }
            
            if (v.equals ("projectsidebar"))
            {
                
                this.showMainSideBar ();
                
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

        } catch (Exception e) {
            
            Environment.logError ("Unable to perform action: " +
                                  v,
                                  e);
            
        }
        
    }
    
    public boolean viewAchievements ()
    {

        // Check our tabs to see if we are already viewing the word counts, if so then just switch to it instead.
        AchievementsPanel ap = (AchievementsPanel) this.getQuollPanel (AchievementsPanel.PANEL_ID);

        if (ap != null)
        {

            this.setPanelVisible (ap);

            this.fireProjectEvent (ProjectEvent.ACHIEVEMENTS,
                                   ProjectEvent.SHOW);

            return true;

        }

        try
        {

            ap = new AchievementsPanel (this,
                                        this.proj);

            ap.init ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to view achievements: " +
                                  this.proj,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to view achievements");

            return false;

        }

        this.addPanel (ap);

        // Open the tab :)
        return this.viewAchievements ();

    }    

    private boolean openPanelInt (String id)
    {

        if (id.equals (OptionsPanel.PANEL_ID))
        {

            return this.showOptions ();
            
        }    
    
        if (id.equals (WordCountPanel.PANEL_ID))
        {

            return this.viewWordCountHistory ();
            
        }

        if (id.equals (AchievementsPanel.PANEL_ID))
        {

            return this.viewAchievements ();
            
        }

        return this.openPanel (id);
        
    }

    public Project getProject ()
    {

        return this.proj;

    }

    protected JScrollPane createTreeScroll (JTree tree)
    {

        return UIUtils.createTreeScroll (tree);

    }

    protected JTree createTree ()
    {

        return UIUtils.createTree ();

    }

    private boolean closeInternal (boolean        saveUnsavedChanges,
                                   ActionListener afterClose)
    {
        
        if (saveUnsavedChanges)
        {

            // Save all.
            for (QuollPanel qp : this.panels.values ())
            {

                if (qp.hasUnsavedChanges ())
                {

                    boolean showError = true;

                    try
                    {

                        showError = !qp.saveUnsavedChanges ();

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to save unsaved changes for: " +
                                              qp.getForObject (),
                                              e);

                    }

                    if (showError)
                    {

                        UIUtils.showErrorMessage (this,
                                                  "Unable to save: " +
                                                  qp.getForObject ().getName () +
                                                  ", aborting exit.");

                        // Switch to the tab.
                        this.viewObject (qp.getForObject ());

                        return false;

                    }

                }

            }            

        }

        try
        {

            this.saveState ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to save viewer state",
                                  e);

            return false;
                                  
        }        
        
        // Regardless of whether it should be saved call the close method
        // for the panel to allow it to close itself nicely.
        // Close after state so we can keep track of what is open.
        
        // Duplicate the values so we don't get a modification error for this.panels.
        Set<QuollPanel> qpps = new LinkedHashSet (this.panels.values ());
        
        for (QuollPanel qp : qpps)
        {
            
            this.closePanel (qp);
                        
        }
        
        this.proj.setLastEdited (new Date ());

        try
        {

            this.saveProject ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to save project: " +
                                  this.proj,
                                  e);

            return false;
                                  
        }

        this.dBMan.saveWordCounts (this.sessionStart,
                                   new Date ());

        this.dBMan.createActionLogEntry (this.proj,
                                         "Closed project",
                                         null,
                                         null);

        // Fire our last event.
        this.fireProjectEvent (this.proj.getObjectType (),
                               ProjectEvent.CLOSE);

        Environment.removeFromAchievementsManager (this);

        // Close all the db connections.
        this.dBMan.closeConnectionPool ();

        try
        {

            Environment.projectClosed (this);

        } catch (Exception e)
        {

            Environment.logError ("Unable to close project",
                                  e);

            return false;
                                  
        }

        this.dispose ();

        this.proj = null;

        this.dBMan = null;
        
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
    
        final StringBuilder b = new StringBuilder ();

        this.exitFullScreen ();
                 
        boolean save = false;

        if (noConfirm)
        {

            save = true;

        }

        if (!noConfirm)
        {

            boolean hasChanges = false;
        
            for (QuollPanel qp : this.panels.values ())
            {
                
                if (qp.hasUnsavedChanges ())
                {
    
                    hasChanges = true;
        
                    if (qp.getForObject () instanceof NamedObject)
                    {
        
                        b.append ("<li>" + UIUtils.getObjectALink ((NamedObject) qp.getForObject ()) + "</li>");
                        
                    }
    
                }                                  
                                    
            }
        
            if (hasChanges)
            {

                final AbstractProjectViewer _this = this;
            
                UIUtils.createQuestionPopup (this,
                                             "Save changes before exiting?",
                                             Constants.SAVE_ICON_NAME,
                                             String.format ("The following items have unsaved changes (click on an item to go to the relevant tab):<ul>%s</ul>Do you wish to save the changes before exiting?",
                                                            b.toString ()),
                                             "Yes, save the changes",
                                             null,
                                             new ActionListener ()
                                             {
                                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
        
                                                    _this.closeInternal (true,
                                                                         afterClose);
                                                
                                                }
                                                
                                             },
                                             null,
                                             null);

                return false;

            }
            
        }
        
        return this.closeInternal (true,
                                   afterClose);
                            
    }

    public FullScreenFrame getFullScreenFrame ()
    {
        
        return this.fsf;
        
    }
    
    /*
    private Book getBookCurrentlyEdited ()
    {

    Chapter c = this.getChapterCurrentlyEdited ();

    if (c != null)
    {

        return c.getBook ();

    }

    return this.proj.getBook (0);

    }
     */
    
    public boolean isCurrentSideBarTextProperties ()
    {
        
        return this.currentSideBar.getName ().equals ("textproperties");
        
    }
    
    public void showTextProperties ()
    {

        if (!(this.getCurrentlyVisibleTab () instanceof AbstractEditorPanel))
        {
            
            return;
            
        }
    
        // Due to the way that key bindings work it's difficult to override the Ctrl+E binding
        // for showing the text properties in full screen.  Instead do a check here if in full screen.
        if (this.isInFullScreen ())
        {
            
            this.fsf.showProperties ();
            
            return;
            
        }
    
        this.addSideBar ("textproperties",
                         new TextPropertiesSideBar (this,
                                                    this,
                                                    new ProjectTextProperties (this)));
        
        this.showSideBar ("textproperties");
        
    }    
    
    public QuollPanel getCurrentlyVisibleTab ()
    {

        if (this.isInFullScreen ())
        {
            
            // Return the panel being displayed.
            return this.fsf.getPanel ().getChild ();
            
        }

        Component sel = this.tabs.getSelectedComponent ();

        if (sel instanceof QuollPanel)
        {

            return (QuollPanel) sel;

        }

        return null;

    }

    private void saveState ()
                     throws GeneralException
    {

        if (this.proj == null)
        {

            return;

        }

        // Set the open tab ids.

        QuollPanel vqp = this.getCurrentlyVisibleTab ();

        String panelId = null;
        
        if (vqp != null)
        {
            
            panelId = vqp.getPanelId ();
            
        }
        
        // Save it.
        try
        {

            this.proj.setProperty (Constants.LAST_OPEN_TAB_PROPERTY_NAME,
                                   panelId);
            
        } catch (Exception e)
        {

            throw new GeneralException ("Unable to save open tab id for project: " +
                                        this.proj,
                                        e);

        }

        StringBuilder openTabs = new StringBuilder ();

        for (QuollPanel qp : this.panels.values ())
        {

            panelId = qp.getPanelId ();

            if (openTabs.length () > 0)
            {

                openTabs.append (",");

            }

            openTabs.append (panelId);

        }

        try
        {

            this.proj.setProperty (Constants.OPEN_TABS_PROPERTY_NAME,
                                   openTabs.toString ());

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to save open tab ids for project: " +
                                        this.proj,
                                        e);

        }

        // Save the size of the window (more specifically the size of the split pane.
        try
        {

            this.proj.setProperty (Constants.WINDOW_HEIGHT_PROPERTY_NAME,
                                   this.splitPane.getSize ().height);

            this.proj.setProperty (Constants.WINDOW_WIDTH_PROPERTY_NAME,
                                   this.splitPane.getSize ().width);

            this.proj.setProperty (Constants.SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME,
                                   this.splitPane.getDividerLocation ());

            this.proj.setProperty (Constants.SPELL_CHECKING_ENABLED_PROPERTY_NAME,
                                   this.spellCheckingEnabled);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to save open tab ids for project: " +
                                        this.proj,
                                        e);

        }

        this.doSaveState ();

        this.saveProject ();

    }

    public java.util.List<DataObject> getObjectsForReferences (String objRefs)
    {

        java.util.List<DataObject> objs = new ArrayList ();

        StringTokenizer t = new StringTokenizer (objRefs,
                                                 ",");

        while (t.hasMoreTokens ())
        {

            String tok = t.nextToken ().trim ();

            ObjectReference r = null;

            try
            {

                r = ObjectReference.parseObjectReference (tok);

            } catch (Exception e)
            {

                Environment.logError ("Unable to parse: " +
                                      tok,
                                      e);

                continue;

            }

            // Pass it to the project.
            DataObject d = this.proj.getObjectForReference (r);

            if (d == null)
            {

                continue;

            }

            objs.add (d);

        }

        return objs;

    }

    public Component getVisiblePanel ()
    {

        return this.tabs.getSelectedComponent ();

    }

    public void enterFullScreen ()
    {
        
        try
        {
        
            this.showInFullScreen (this.getCurrentlyVisibleTab ());
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to show in full screen",
                                  e);
            
            UIUtils.showErrorMessage (this,
                                      "Unable to enter full screen mode");
            
        }
        
    }
    
    public void fullScreenClosed (QuollPanel vis)
    {

        this.fsf = null;

        this.fullScreenOverlay.setVisible (false);
        
        DnDTabbedPane p = this.createTabbedPane ();
        
        // Have to rebuild the tabbed pane because otherwise it behaves weirdly.
        for (int i = this.tabs.getTabCount () - 1; i > -1; i--)
        {
            
            Component title = this.tabs.getTabComponentAt (i);
            Component body = this.tabs.getComponentAt (i);
            
            this.tabs.remove (i);
            
            this.tabs.add (body,
                   i);
            this.tabs.setTabComponentAt (i,
                                 title);
            
        }
        
        try
        {
        
            this.tabs.setSelectedComponent (vis);
            
        } catch (Exception e) {
            
            // May not exist, don't care.
            
        }

        this.tabs.setVisible (true);
        
        if (vis instanceof AbstractEditorPanel)
        {

            AbstractEditorPanel edPanel = (AbstractEditorPanel) vis;
    
            QTextEditor ed = edPanel.getEditor ();

            edPanel.scrollCaretIntoView ();
        
            ed.grabFocus ();

        }
        
        if (this.splitPane.getLeftComponent () == this.tabs)
        {
            
            this.splitPane.setRightComponent (this.sideBar);
            
        } else {

            this.splitPane.setLeftComponent (this.sideBar);
            
        }

        this.splitPane.setDividerLocation (this.lastDividerLocation);

        this.setVisible (true);                                                  
        
        this.validate ();
        this.repaint ();
        
    }

    public void restoreFromFullScreen (FullScreenQuollPanel qp)
    {

        for (int i = 0; i < this.tabs.getTabCount (); i++)
        {

            Component comp = this.tabs.getComponentAt (i);

            if (comp == qp)
            {

                this.tabs.setComponentAt (i,
                                          qp.getChild ());

                qp.getChild ().setVisible (true);

                this.tabs.revalidate ();
                this.tabs.repaint ();
                this.validate ();
                this.repaint ();

                return;

            }

        }

        this.tabs.setVisible (true);
        
    }

    public boolean isInFullScreen ()
    {
        
        return this.fsf != null;
        
    }

    public void showInFullScreen (QuollPanel qep)
                           throws GeneralException
    {

        if (this.fsf == null)
        {
            
            // Need to get the divider location.
            this.lastDividerLocation = this.splitPane.getDividerLocation ();        
            
        }
                
        if (this.fullScreenOverlay == null)
        {
        
            this.fullScreenOverlay = new FullScreenOverlay (this);
                    
            this.setGlassPane (this.fullScreenOverlay);

        }
        
        this.fullScreenOverlay.setVisible (true);

        if (this.fsf != null)
        {

            if (this.fsf.getPanel ().getChild () == qep)
            {

                // Nothing to do, it's already showing, maybe bring to front.
                this.fsf.toFront ();

                return;

            } 

        }

        if (qep == null)
        {
            
            qep = new BlankQuollPanel (this);
            
            qep.init ();
            
        }
        
        if (qep != null)
        {

            FullScreenQuollPanel fs = new FullScreenQuollPanel (qep);

            int tabInd = this.getTabIndexForPanelId (qep.getPanelId ());
            
            if (tabInd > -1)
            {
            
                // Need to set the component, otherwise it will be removed.
                this.tabs.setComponentAt (tabInd,
                                          fs);

            }
                                          
            if (this.fsf != null)
            {

                this.fsf.switchTo (fs);

            } else
            {
            
                this.fsf = new FullScreenFrame (fs);

                this.fsf.init ();

                // Need to set the tabs hidden otherwise the parent qw window will flash in front
                // or show up in front of the fsf.
                this.tabs.setVisible (false);                
                
            }

            //this.fsf.toFront ();

            this.tabs.revalidate ();
            this.tabs.repaint ();
            this.validate ();
            this.repaint ();            
            
        } 
        
    }
    
    public void showInFullScreen (DataObject n)
                           throws GeneralException
    {

        // Are we already in fs mode?
        if (this.fsf != null)
        {

            if (this.fsf.getPanel ().getChild ().getForObject () == n)
            {

                // Nothing to do, it's already showing, maybe bring to front.
                this.fsf.toFront ();

                return;

            } else
            {

                this.viewObject (n);

            }

        }

        if (this.fullScreenOverlay == null)
        {
        
            this.fullScreenOverlay = new FullScreenOverlay (this);
                    
            this.setGlassPane (this.fullScreenOverlay);

        }
        
        this.fullScreenOverlay.setVisible (true);        
        
        this.lastDividerLocation = this.splitPane.getDividerLocation ();     
        
        AbstractEditorPanel qep = this.getEditorForChapter ((Chapter) n);

        if (qep != null)
        {
            
            FullScreenQuollPanel fs = new FullScreenQuollPanel (qep);

            // Need to set the component, otherwise it will be removed.
            this.tabs.setComponentAt (this.getTabIndexForPanelId (qep.getPanelId ()),
                                      fs);

            if (this.fsf != null)
            {

                this.fsf.switchTo (fs);

            } else
            {

                this.fsf = new FullScreenFrame (fs);

                this.fsf.init ();

            }

            //this.fsf.toFront ();

            this.tabs.revalidate ();
            this.tabs.repaint ();
            this.validate ();
            this.repaint ();            
            
        }

    }

    public QuollPanel getQuollPanelForObject (DataObject n)
    {

        for (QuollPanel qp : this.panels.values ())
        {
            
            if (qp.getForObject () == n)
            {

                if (qp instanceof FullScreenQuollPanel)
                {
                    
                    return ((FullScreenQuollPanel) qp).getChild ();
                    
                } else {

                    return qp;
                
                }

            }
            
        }
/*
        for (int i = 0; i < this.tabs.getTabCount (); i++)
        {

            Component comp = this.tabs.getComponentAt (i);

            if (comp instanceof QuollPanel)
            {

                QuollPanel qp = (QuollPanel) comp;

                if (qp.getForObject () == n)
                {

                    if (qp instanceof FullScreenQuollPanel)
                    {
                        
                        return ((FullScreenQuollPanel) qp).getChild ();
                        
                    } else {

                        return qp;
                    
                    }

                }

            }

        }
*/
        return null;

    }

    public java.util.List<QuollPanel> getAllQuollPanelsForObject (DataObject n)
    {

        java.util.List<QuollPanel> ret = new ArrayList ();

        for (QuollPanel qp : this.panels.values ())
        {
            
            if (qp.getForObject () == n)
            {

                ret.add (qp);

            }            
            
        }

        return ret;

    }

    public QuollPanel getQuollPanel (String panelId)
    {

        return this.panels.get (panelId);

    }

    protected void setPanelVisible (QuollPanel qp)
    {

        this.updateToolbarForPanel (qp);
    
        if (!this.inFullScreen ())
        {
    
            this.tabs.setSelectedComponent (qp);
            this.tabs.revalidate ();
            this.tabs.repaint ();

        } else {

            try
            {
            
                this.showInFullScreen (qp);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to show: " + qp.getForObject () +
                                      " in full screen",
                                      e);
                
                UIUtils.showErrorMessage (this,
                                          "Unable to show in full screen");
                
            }
            
        }

        this.fireMainPanelShownEvent (qp);
  
    }

    protected int getTabIndexForObject (NamedObject n)
    {

        return this.getTabIndexForPanelId (n.getObjectReference ().asString ());

    }

    protected int getTabIndexForPanelId (String panelId)
    {

        for (int i = 0; i < this.tabs.getTabCount (); i++)
        {

            Component comp = this.tabs.getComponentAt (i);

            if (comp instanceof QuollPanel)
            {

                QuollPanel qp = (QuollPanel) comp;

                if (qp.getPanelId ().equals (panelId))
                {

                    return i;

                }

            }

        }

        return -1;

    }

    public void setTabHeaderTitle (QuollPanel qp,
                                   String     title)
    {
        
        TabHeader th = (TabHeader) this.tabs.getTabComponentAt (getTabIndexForPanelId (qp.getPanelId ()));
        
        if (th != null)
        {
        
            th.setTitle (title);
            
        }
        
    }
    
    public TabHeader addPanel (final QuollPanel qp)
    {

        final AbstractProjectViewer _this = this;
        
        final TabHeader th = new TabHeader (this.tabs,
                                            Environment.getIcon (Constants.CANCEL_ICON_NAME,
                                                                 Constants.ICON_TAB_HEADER),
                                            Environment.getTransparentImage (),
                                            qp.getTitle ());

        th.setIcon (Environment.getIcon (qp.getIconType (),
                                         Constants.ICON_TAB_HEADER));

        final String panelId = qp.getPanelId ();

        this.panels.put (panelId,
                         qp);
        
        th.addMouseListener (new MouseAdapter ()
        {
           
            public void mousePressed (MouseEvent ev)
            {
                
                if (SwingUtilities.isMiddleMouseButton (ev))
                {
                    /*
                    if (!_this.removePanel (panelId))
                    {

                        return;

                    }
*/
                    if (!_this.closePanel (qp))
                    {
                        

                    }
                    
                }
                
            }
            
        });
        
        th.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (ev.getID () == TabHeader.TAB_CLOSING)
                {
/*
                    if (!_this.removePanel (panelId))
                    {

                        VetoableActionEvent vev = (VetoableActionEvent) ev;

                        vev.cancel ();

                        return;

                    }
*/
                    if (!_this.closePanel (qp))
                    {
                        
                        VetoableActionEvent vev = (VetoableActionEvent) ev;

                        vev.cancel ();                        
                        
                    }
                    
                }

            }

        });

        qp.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    if (ev.getID () == QuollPanel.UNSAVED_CHANGES_ACTION_EVENT)
                    {

                        if (ev.getActionCommand ().equals (QuollPanel.HAS_CHANGES_COMMAND))
                        {

                            th.setComponentChanged (true);

                        }

                        if (ev.getActionCommand ().equals (QuollPanel.NO_CHANGES_COMMAND))
                        {

                            th.setComponentChanged (false);

                        }

                    }

                    if (ev.getID () == QuollPanel.SAVED)
                    {

                        th.setComponentChanged (false);

                    }

                }

            });

        this.tabs.add (qp,
                       0);
        this.tabs.setTabComponentAt (0,
                                     th);

        this.initPanelState (qp);                                     
                                     
        return th;

    }
    
    public boolean closePanel (NamedObject n)
    {
        
        QuollPanel qp = this.getQuollPanel (n.getObjectReference ().asString ());
        
        if (qp == null)
        {

            return false;
            
        }
        
        return this.closePanel (qp);
        
    }
    
    public boolean closePanel (QuollPanel qp)
    {

        if (!this.removePanel (qp))
        {

            return false;
        
        }
    
        // Get the state.
        Map m = new LinkedHashMap ();

        qp.getState (m);

        String panelId = qp.getPanelId ();                
        
        if (m.size () > 0)
        {
        
            try
            {

                this.proj.setProperty (panelId + "-state",
                                       UIUtils.createState (m));

            } catch (Exception e)
            {

                Environment.logError ("Unable to save state for panel: " +
                                      panelId,
                                      e);

            }

        }

        qp.close ();

        this.panels.remove (panelId);

        return true;
        
    }
        
    protected void informTreeOfNodeChange (NamedObject n,
                                           JTree       tree)
    {

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                                                                 n).getLastPathComponent ();

        model.nodeChanged (node);
        
        tree.validate ();
        tree.repaint ();

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

        if (this.fsf != null)
        {
            
            this.fsf.addPopup (c,
                               hideOnClick,
                               hideViaVisibility);
            
            return;
            
        }
    
        this.getLayeredPane ().add (c,
                                    JLayeredPane.POPUP_LAYER);

        this.getLayeredPane ().moveToFront (c);

//      this.getLayeredPane ().repaint ();

    }

    public void removePopup (Component c)
    {

        if (this.fsf != null)
        {
            
            this.fsf.removePopup (c);
            
            return;
            
        }
    
        this.getLayeredPane ().remove (c);

        this.getLayeredPane ().validate ();

        this.getLayeredPane ().repaint ();

    }

    public void showAchievement (AchievementRule ar)
    {

        try
        {
            
            this._showAchievement (ar);
            
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
            
            m.setSize (new Dimension (350,
                                      500));
            m.setMaximumSize (new Dimension (350,
                                             Short.MAX_VALUE));
                                             
            Box b = new Box (BoxLayout.Y_AXIS);
            b.setBackground (UIUtils.getComponentColor ());
            b.setOpaque (true);            
            b.add (m);
            b.setPreferredSize (new Dimension (350,
                                               b.getPreferredSize ().height));
            c = b;
            
        }

        final QPopup popup = UIUtils.createPopup (title,
                                                  Constants.INFO_ICON_NAME,
                                                  c,
                                                  true,
                                                  null);
                
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
                          new Point (10, 10));
        
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
    
    private void _showAchievement (AchievementRule ar)
    {

        Box b = null;

        if (this.isDistractionFreeModeEnabled ())
        {
            
            return;
            
        }
        
        if (this.achievementsPopup == null)
        {

            b = new Box (BoxLayout.Y_AXIS);
            b.setBackground (UIUtils.getComponentColor ());
            b.setOpaque (true);            
            
            this.achievementsPopup = UIUtils.createPopup ("You've got an Achievement",
                                                          null,
                                                          b,
                                                          true,
                                                          null);
            
            this.achievementsPopup.getHeader ().setPreferredSize (new Dimension (250,
                                                                  this.achievementsPopup.getHeader ().getPreferredSize ().height));

            final AbstractProjectViewer _this = this;
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

        // Full screen?
        if (this.fsf != null)
        {

            this.fsf.showAchievementsPopup (this.achievementsPopup,
                                              new Point (10, 10));

        } else {

            this.showPopupAt (this.achievementsPopup,
                              new Point (10, 10));

        }
       
        this.achievementsPopup.setVisible (true);
        
        final AbstractProjectViewer _this = this;
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
        
    }

    public QPopup getPopupByName (String name)
    {
        
        if (name == null)
        {
            
            return null;
            
        }
        
        if (this.fsf != null)
        {
            
            return this.fsf.getPopupByName (name);
            
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
    
    public void showPopupAt (Component popup,
                             Component showAt)
    {

        if (this.fsf != null)
        {
            
            this.fsf.showPopupAt (popup,
                                  showAt);
            
            return;
            
        }
    
        Point po = SwingUtilities.convertPoint (showAt,
                                                0,
                                                0,
                                                this.getContentPane ());

        this.showPopupAt (popup,
                          po);


    }

    public void showPopupAt (Component c,
                             Point     p)
    {

        if (this.fsf != null)
        {
            
            this.fsf.showPopupAt (c,
                                  p);
            
            return;
            
        }    
    
        Insets ins = this.getInsets ();

        if ((c.getParent () == null)
            &&
            (c.getParent () != this.getLayeredPane ())
           )
        {

            this.addPopup (c,
                           true,
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

    public void showPopup (Component c)
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
                          p);

    }

    public boolean removePanel (NamedObject n)
    {

        return this.removePanel (n.getObjectReference ().asString ());

    }

    public void removeAllPanelsForObject (NamedObject n)
    {

        java.util.List<QuollPanel> panels = this.getAllQuollPanelsForObject (n);

        for (QuollPanel p : panels)
        {

            this.removePanel (p.getPanelId ());

        }

    }

    private boolean removePanel (String panelId)
    {

        QuollPanel p = this.getQuollPanel (panelId);

        if (p == null)
        {

            return false;

        }
        
        return this.removePanel (p);
        
    }
    
    private boolean removePanel (final QuollPanel p)
    {
        
        final AbstractProjectViewer _this = this;
        
        final ActionListener remove = new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                NamedObject n = p.getForObject ();
        
                if (p != null)
                {
        
                    if ((_this.fsf != null)
                        &&
                        (p == _this.fsf.getPanel ().getChild ())
                       )
                    {
                        
                        // In full screen, restore first.
                        _this.restoreFromFullScreen (_this.fsf.getPanel ());
                        
                        // Add a blank instead.
                        _this.fsf.showBlankPanel ();
                        
                    }        
        
                    String panelId = p.getPanelId ();
                    
                    int tInd = _this.getTabIndexForPanelId (panelId);
                    
                    if (tInd > -1)
                    {
        
                        _this.tabs.removeTabAt (tInd);
        
                    }
        
                    _this.panels.remove (panelId);
        
                    // Remove all the property changed listeners.
                    java.util.List<PropertyChangedListener> l = p.getObjectPropertyChangedListeners ();
        
                    if (l != null)
                    {
        
                        for (PropertyChangedListener c : l)
                        {
        
                            n.removePropertyChangedListener (c);
        
                        }
        
                    }
        
                }
                
            }
            
        };
        
        // Object already deleted, don't ask the question.
        if (!this.proj.hasObject (p.getForObject ()))
        {
            
            remove.actionPerformed (new ActionEvent (this,
                                                     0,
                                                     "deleted"));
            
            return true;
            
        }

        if (p.hasUnsavedChanges ())
        {
        
            UIUtils.createQuestionPopup (this,
                                         "Save before closing?",
                                         Constants.SAVE_ICON_NAME,
                                         String.format ("The %s has unsaved changes.  Save before closing?",
                                                        p.getForObject ().getObjectType ()),
                                         "Yes, save the changes",
                                         "No, discard the changes",
                                         new ActionListener ()
                                         {
                                            
                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                try
                                                {
                                
                                                    p.saveObject ();
                                
                                                } catch (Exception e)
                                                {
                                
                                                    // What the hell to do here???
                                                    Environment.logError ("Unable to save: " +
                                                                          p.getForObject (),
                                                                          e);
                                
                                                    UIUtils.showErrorMessage (_this,
                                                                              "Unable to save " +
                                                                              Environment.getObjectTypeName (p.getForObject ()).toLowerCase () +
                                                                              ": " +
                                                                              p.getForObject ().getName ());
                                                                
                                                    return;
                                                                
                                                }    
                                            
                                                remove.actionPerformed (ev);
                                            
                                            }
                                            
                                         },
                                         new ActionListener ()
                                         {
                                            
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                                
                                                remove.actionPerformed (ev);
                                                
                                            }
                                            
                                         },
                                         null);
            
            return false;

        }

        remove.actionPerformed (new ActionEvent (this,
                                                 0,
                                                 "deleted"));
  
        return true;

    }

    public DataHandler getDataHandler (String objType)
    {

        return this.dBMan.getHandler (objType);

    }

    public void refreshViewPanel (NamedObject n)
    {

        QuollPanel p = this.getQuollPanelForObject (n);

        if (p != null)
        {

            p.refresh (n);

        }

    }

    public void refreshObjectPanels (final Set<NamedObject> objs)
    {

        final AbstractProjectViewer _this = this;

        if ((objs != null) &&
            (objs.size () > 0))
        {

            SwingUtilities.invokeLater (new Runner ()
                {

                    public void run ()
                    {

                        // For each one determine if it is visible.
                        for (NamedObject n : objs)
                        {

                            QuollPanel qp = _this.getQuollPanelForObject (n);

                            if (qp != null)
                            {

                                qp.refresh (n);

                            }

                        }

                    }

                });

        }

    }

    public void deleteLinks (NamedObject o,
                             boolean     doInTransaction)
                      throws GeneralException
    {

        java.util.Set<NamedObject> otherObjects = o.getOtherObjectsInLinks ();

        this.dBMan.deleteLinks (o,
                                null);

        this.refreshObjectPanels (otherObjects);

    }

    public void saveLinks (NamedObject o,
                           Set<Link>   newLinks)
                    throws GeneralException
    {

        // Do it here to get all the links that may be removed.
        java.util.Set<NamedObject> otherObjects = o.getOtherObjectsInLinks ();

        this.dBMan.updateLinks (o,
                                newLinks);

        // Do it here to get all the links that may be added.
        otherObjects.addAll (o.getOtherObjectsInLinks ());

        this.refreshObjectPanels (otherObjects);

    }

    public void saveObject (NamedObject o,
                            boolean     doInTransaction)
                     throws GeneralException
    {

        java.util.Set<NamedObject> otherObjects = o.getOtherObjectsInLinks ();

        this.dBMan.saveObject (o,
                               null);

        this.refreshObjectPanels (otherObjects);

    }

    public void saveObjects (java.util.List objs,
                             boolean        doInTransaction)
                      throws GeneralException
    {

        this.dBMan.saveObjects (objs,
                                null);

    }
/*
    public void deleteObject (NamedObject o)
                       throws GeneralException
    {

        this.dBMan.deleteObject (o,
                                 false,
                                 null);

    }
*/
    public void saveProject ()
                      throws GeneralException
    {

        this.dBMan.saveObject (this.proj,
                               null);

    }

    public void setLinks (NamedObject o)
    {

        try
        {

            this.dBMan.getLinks (o,
                                 this.proj);

        } catch (Exception e)
        {

            Environment.logError ("Unable to set links for: " +
                                  o,
                                  e);

        }

    }

    public void propertyChanged (PropertyChangedEvent ev)
    {

        DataObject d = (DataObject) ev.getSource ();

        if (d instanceof Project)
        {

            Project p = (Project) d;

            try
            {

                this.saveObject (p,
                                 true);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save project: " +
                                      p,
                                      e);

            }

        }

    }

    public boolean isSpellCheckingEnabled ()
    {

        return this.spellCheckingEnabled;

    }

    public void setSpellCheckingEnabled (final boolean v)
    {

        this.spellCheckingEnabled = v;

        this.doForPanels (SpellCheckSupported.class,
                          new DefaultQuollPanelAction ()
                          {
                                
                               public void doAction (QuollPanel qp)
                               {
                                    
                                   SpellCheckSupported s = (SpellCheckSupported) qp;
                                      
                                   s.setSpellCheckingEnabled (v);
                                    
                               }
                                
                          });

        this.fireProjectEvent (ProjectEvent.SPELL_CHECK,
                               (v ? ProjectEvent.ON : ProjectEvent.OFF));

    }

    public void createActionLogEntry (NamedObject n,
                                      String      m)
    {

        this.dBMan.createActionLogEntry (n,
                                         m,
                                         null,
                                         null);

    }

    public Point convertPoint (Component c,
                               Point     p)
    {

        Component o = this.getContentPane ();
        
        if (this.inFullScreen ())
        {
            
            o = this.fsf;
            
        }
        
        return SwingUtilities.convertPoint (c,
                                            p,
                                            o);
        
    }
    
    public boolean inFullScreen ()
    {

        return this.fsf != null;

    }

    public ReadabilityIndices getReadabilityIndices (Chapter c)
    {
        
        AbstractEditorPanel qep = this.getEditorForChapter (c);

        ReadabilityIndices ri = null;

        if (qep != null)
        {

            ri = qep.getReadabilityIndices ();
            
            if (ri != null)
            {

                this.noEditorReadabilityIndices.remove (c);

                return ri;
                
            }
        
        } 
            
        ri = this.noEditorReadabilityIndices.get (c);
        
        if (ri == null)
        {
            
            // Cache the value.
            ri = this.getReadabilityIndices (c.getText ());
        
            this.noEditorReadabilityIndices.put (c,
                                                 ri);

        }
        
        return ri;
        
    }
    
    public ChapterCounts getChapterCounts (Chapter c,
                                           boolean calcA4PagesCount)
    {
        
        AbstractEditorPanel qep = this.getEditorForChapter (c);

        ChapterCounts qcc = null;

        if (qep != null)
        {

            qcc = qep.getChapterCounts ();
            
            if (qcc != null)
            {

                this.noEditorChapterCounts.remove (c);

                return qcc;
                
            }
        
        } 
            
        qcc = this.noEditorChapterCounts.get (c);
        
        if (qcc == null)
        {
            
            // Cache the value.
            qcc = UIUtils.getChapterCounts (c.getText ());
        
            if (calcA4PagesCount)
            {
                
                qcc.a4PageCount = UIUtils.getA4PageCountForChapter (c,
                                                                    c.getText ());
                
            }
        
            this.noEditorChapterCounts.put (c,
                                            qcc);

        }
        
        return qcc;
        
    }

    public Set<ChapterCounts> getAllChapterCounts2 (boolean calcA4PagesCount)
    {

        Set<ChapterCounts> ccs = new LinkedHashSet ();
        
        Book b = this.proj.getBooks ().get (0);

        java.util.List<Chapter> chapters = b.getChapters ();

        for (Chapter c : chapters)
        {

            ccs.add (this.getChapterCounts (c,
                                            calcA4PagesCount));

        }        
        
        return ccs;
        
    }
        
    public ReadabilityIndices getAllReadabilityIndices ()
    {
        
        if (this.proj == null)
        {
            
            return null;
            
        }
    
        Book b = this.proj.getBooks ().get (0);

        java.util.List<Chapter> chapters = b.getChapters ();

        ReadabilityIndices ri = new ReadabilityIndices ();

        for (Chapter c : chapters)
        {

            ri.add (this.getReadabilityIndices (c));
        
        }

        return ri;        
        
    }
    
    public ChapterCounts getAllChapterCounts (boolean calcA4PagesCount)
    {

        if (this.proj == null)
        {
            
            return null;
            
        }
    
        if (this.proj.getBooks ().size () == 0)
        {
            
            return null;
            
        }
    
        Book b = this.proj.getBooks ().get (0);
        
        java.util.List<Chapter> chapters = b.getChapters ();

        ChapterCounts achc = new ChapterCounts ();

        for (Chapter c : chapters)
        {

            achc.add (this.getChapterCounts (c,
                                             calcA4PagesCount));
        
        }

        return achc;

    }

    private Book getBookCurrentlyEdited ()
    {

        Chapter c = this.getChapterCurrentlyEdited ();

        if (c != null)
        {

            return c.getBook ();

        }

        return this.proj.getBook (0);

    }

    public Chapter getChapterCurrentlyEdited ()
    {

        Component sel = this.getCurrentlyVisibleTab ();

        if (sel instanceof AbstractEditorPanel)
        {

            // Get the id.
            AbstractEditorPanel qep = (AbstractEditorPanel) sel;

            // Get the chapter.
            return qep.getChapter ();

        }

        return null;

    }

    public boolean showOptions ()
    {

        OptionsPanel p = (OptionsPanel) this.getQuollPanel (OptionsPanel.PANEL_ID);

        if (p == null)
        {

            try
            {

                p = new OptionsPanel (this);

                p.init ();

            } catch (Exception e)
            {

                Environment.logError ("Unable to view the options",
                                      e);

                UIUtils.showErrorMessage (this,
                                          "Unable to view the options");

                return false;

            }

            this.addPanel (p);

        }

        this.setPanelVisible (p);

        return true;

    }    

    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the word count history is viewed.
     */
    public boolean viewWordCountHistory ()
    {

        // Check our tabs to see if we are already viewing the word counts, if so then just switch to it instead.
        WordCountPanel wcp = (WordCountPanel) this.getQuollPanel (WordCountPanel.PANEL_ID);

        if (wcp != null)
        {

            this.setPanelVisible (wcp);

            this.fireProjectEvent (ProjectEvent.WORD_COUNT_HISTORY,
                                   ProjectEvent.SHOW);

            return true;

        }

        try
        {

            wcp = new WordCountPanel (this,
                                      this.proj);

            wcp.init ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to view word counts: " +
                                  this.proj,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to view word count history");

            return false;

        }

        this.addPanel (wcp);

        // Open the tab :)
        return this.viewWordCountHistory ();

    }

    public ChapterCounts getStartWordCounts ()
    {
        
        return this.startWordCounts;
        
    }
    
    public int getSessionWordCount ()
    {
        
        ChapterCounts achc = this.getAllChapterCounts (false);
        
        return achc.wordCount - this.startWordCounts.wordCount;
        
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
        
        final AbstractProjectViewer _this = this;
        
        SwingUtilities.invokeLater (new Runnable ()
        {
            
            public void run ()
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

}
