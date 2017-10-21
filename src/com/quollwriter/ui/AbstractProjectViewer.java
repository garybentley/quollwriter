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
import java.util.TimerTask;
import java.util.concurrent.*;

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
import com.quollwriter.text.*;
import com.quollwriter.text.rules.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.charts.*;

import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.ui.sidebars.*;

import com.quollwriter.achievements.rules.*;

public abstract class AbstractProjectViewer extends AbstractViewer implements PropertyChangedListener,
                                                                              SpellCheckSupported,
                                                                              HTMLPanelActionHandler
{

    public static final String IDEA_BOARD_HEADER_CONTROL_ID = "ideaBoard";
	public static final String FIND_HEADER_CONTROL_ID = "find";
    public static final String FULL_SCREEN_HEADER_CONTROL_ID = "fullScreen";
    public static final String CLOSE_HEADER_CONTROL_ID = "close";

    public static final String TAB_OBJECT_TYPE = "tab";

    public static int INTERNAL_SPLIT_PANE_DIVIDER_WIDTH = 2;

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
    public static final int CONTACT_SUPPORT_ACTION = 28;

    public static final String NAME_CHANGED = "nameChanged";

    protected Project             proj = null;
    private DnDTabbedPane         tabs = null;
    private DictionaryProvider    dictProv = null;
    private SynonymProvider       synProv = null;
    private JSplitPane            splitPane = null;
    private JSplitPane            splitPane2 = null;
    protected ObjectManager       dBMan = null;
    //private Header                title = null;
    private Map<String, JToolBar> toolbars = new HashMap ();
    private Box                   toolbarPanel = null;
    private Accordion             acc = null;
    private boolean               spellCheckingEnabled = false;
    private Date                  sessionStart = new Date ();
    //private Box                   notifications = null;
    private boolean               playSoundOnKeyStroke = false;
    private Clip                  keyStrokeSound = null;
    //private QPopup                achievementsPopup = null;
    private ChapterCounts         startWordCounts = new ChapterCounts (null);
    private Map<Chapter, ReadabilityIndices> noEditorReadabilityIndices = new WeakHashMap ();
    private Map<Chapter, ChapterCounts> chapterCounts = new WeakHashMap ();
	//private Box                   itemsBox = null;
    private Box                   sideBar = null;
    private Box                   sideBarWrapper = null;
    private Map<String, AbstractSideBar> sideBars = new HashMap ();
    private Stack<AbstractSideBar>  activeSideBars = new Stack ();
    private AbstractSideBar       currentOtherSideBar = null;
    private AbstractSideBar       mainSideBar = null;
    private Finder                finder = null;
    private WordCountsSideBar      wordCounts = null;
    private Map<String, QuollPanel> panels = new HashMap ();
    private int                   lastDividerLocation = -1;
    private Map<String, Integer>  sideBarWidths = new HashMap ();
    private java.util.List<SideBarListener> sideBarListeners = new ArrayList ();
    private java.util.List<MainPanelListener> mainPanelListeners = new ArrayList ();
    private java.util.List<FullScreenListener> fullScreenListeners = new ArrayList ();
    //private Map<String, QPopup> popups = new HashMap ();

    private Timer achievementsHideTimer = null;

    private JTree chapterTree = null;

    private FullScreenFrame fsf = null;
    private FullScreenOverlay fullScreenOverlay = null;

    private String toolbarLocation = Constants.BOTTOM;

    private String layout = Constants.LAYOUT_PS_CH;

    private Map tempOptions = new HashMap ();

    //private Set<ProjectEventListener> projectEventListeners = new HashSet ();
    //private boolean ignoreProjectEvents = false;

    //private Tips tips = null;

    private DictionaryManager dictMan = null;

    private WordCountTimer wordCountTimer = null;

    private int savedOtherSideBarWidth = 0;
    private int savedSideBarWidth = 0;

	private JPanel                cards = null;
	private CardLayout            cardsLayout = null;

	private ScheduledFuture autoSaveTask = null;
    private ScheduledFuture chapterCountsUpdater = null;
	private TargetsData targets = null;
	private Map<Chapter, Date> chapterWordCountTargetWarned = new HashMap ();

    public AbstractProjectViewer()
    {

        final AbstractProjectViewer _this = this;

        this.wordCountTimer = new WordCountTimer (this,
                                                  -1,
                                                  -1);

        this.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);

        // Create a split pane.
        this.splitPane = UIUtils.createSplitPane (JSplitPane.HORIZONTAL_SPLIT);

        this.splitPane.setBorder (null);

        javax.swing.plaf.basic.BasicSplitPaneDivider div = ((javax.swing.plaf.basic.BasicSplitPaneUI) this.splitPane.getUI ()).getDivider ();
        div.setBorder (new MatteBorder (0, 0, 0, 1, Environment.getBorderColor ()));
        this.splitPane.setOpaque (false);
        this.splitPane.setBackground (UIUtils.getComponentColor ());

        this.splitPane2 = UIUtils.createSplitPane (JSplitPane.HORIZONTAL_SPLIT);
        this.splitPane2.setBorder (null);

        div = ((javax.swing.plaf.basic.BasicSplitPaneUI) this.splitPane2.getUI ()).getDivider ();
        div.setBorder (new MatteBorder (0, 0, 0, 1, Environment.getBorderColor ()));
        this.splitPane2.setOpaque (false);
        this.splitPane2.setBackground (UIUtils.getComponentColor ());

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

        this.tabs = this.createTabbedPane ();

        this.tabs.addChangeListener (new ChangeListener ()
        {

            public void stateChanged (ChangeEvent ev)
            {

                if (_this.tabs.getTabCount () == 0)
                {

                    _this.tabs.setVisible (false);

                } else {

                    _this.tabs.setVisible (true);

                }
            }

        });

        this.tabs.setMinimumSize (new Dimension (300, 200));
        this.tabs.putClientProperty(com.jgoodies.looks.Options.NO_CONTENT_BORDER_KEY, Boolean.TRUE);
        //this.tabs.putClientProperty(com.jgoodies.looks.Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
        this.tabs.setBorder (null);

        this.setTabsLocation (null);

        this.splitPane2.setLeftComponent (this.tabs);

        this.splitPane.setRightComponent (this.splitPane2);

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

		this.cardsLayout = new CardLayout (0, 0);

		this.cards = new JPanel (this.cardsLayout);
        this.cards.setBackground (UIUtils.getComponentColor ());
		this.cards.setAlignmentX (Component.LEFT_ALIGNMENT);

		this.cards.add (this.splitPane, "main");

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

        this.setContent (this.cards);

    }

    public void addCard (String     name,
                         JComponent c)
    {

        this.cards.add (c,
                        name);

    }

    public void showMainCard ()
    {

        this.showCard ("main");

    }

    public void showCard (String name)
    {

        this.cardsLayout.show (this.cards,
                               name);

        this.validate ();
        this.repaint ();

    }

    @Override
    public void initActionMappings (ActionMap am)
    {

        final AbstractProjectViewer _this = this;

        super.initActionMappings (am);

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

        am.put ("show-main-sidebar",
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.closeSideBar ();

                    }

                });

        am.put ("new-project",
                this.getAction (AbstractProjectViewer.NEW_PROJECT_ACTION,
                                null));

        am.put ("open-project",
                this.getAction (AbstractProjectViewer.OPEN_PROJECT_ACTION,
                                null));

        am.put (Constants.SHOW_FIND_ACTION,
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.showFind (null);

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

    @Override
    public void initKeyMappings (InputMap im)
    {

        super.initKeyMappings (im);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F1,
                                        0),
                Constants.SHOW_FIND_ACTION);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F4,
                                        0),
                "close-current-tab");

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F5,
                                        0),
                "fullscreen");

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE,
                                        0),
                "show-main-sidebar");

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                        Event.CTRL_MASK),
                Constants.SHOW_FIND_ACTION);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_N,
                                        Event.CTRL_MASK),
                "new-project");
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_O,
                                        Event.CTRL_MASK),
                "open-project");

    }

    public WordCountTimer getWordCountTimer ()
    {

        return this.wordCountTimer;

    }

    protected void initSideBars ()
                          throws GeneralException
    {

        this.finder = new Finder (this);

        this.addSideBar (this.finder);

        this.wordCounts = new WordCountsSideBar (this);

        this.addSideBar (this.wordCounts);

        this.mainSideBar = this.getMainSideBar ();

        this.addSideBar (this.mainSideBar);

    }

    public JComponent getSideBarForFullScreen ()
    {

        if (this.currentOtherSideBar != null)
        {

            this.sideBarWrapper.removeAll ();
            this.sideBarWrapper.add (this.currentOtherSideBar);

        } else {

            if (this.mainSideBar != null)
            {

                this.sideBarWrapper.removeAll ();
                this.sideBarWrapper.add (this.mainSideBar);

            }

        }

        return this.sideBar;

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

    private void fireFullScreenEnteredEvent ()
    {

        FullScreenEvent ev = new FullScreenEvent (this);

        for (FullScreenListener l : this.fullScreenListeners)
        {

            l.fullScreenEntered (ev);

        }

    }

    private void fireFullScreenExitedEvent ()
    {

        FullScreenEvent ev = new FullScreenEvent (this);

        for (FullScreenListener l : this.fullScreenListeners)
        {

            l.fullScreenExited (ev);

        }

    }

    public void removeFullScreenListener (FullScreenListener l)
    {

        this.fullScreenListeners.remove (l);

    }

    public void addFullScreenListener (FullScreenListener l)
    {

        this.fullScreenListeners.add (l);

    }

    protected void fireSideBarShownEvent (AbstractSideBar sb)
    {

        SideBarEvent ev = new SideBarEvent (this,
                                            sb);

        for (SideBarListener l : this.sideBarListeners)
        {

            l.sideBarShown (ev);

        }

    }

    protected void fireSideBarHiddenEvent (AbstractSideBar sb)
    {

        SideBarEvent ev = new SideBarEvent (this,
                                            sb);

        for (SideBarListener l : this.sideBarListeners)
        {

            l.sideBarHidden (ev);

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

    private JComponent createLayoutFiller ()
    {

        Box l = new Box (BoxLayout.X_AXIS);
        l.setMinimumSize (new Dimension (0, 0));
        l.setPreferredSize (new Dimension (0, 0));
        l.setVisible (false);

        return l;

    }

    public void closeSideBar ()
    {

        if (this.currentOtherSideBar != null)
        {

            this.currentOtherSideBar.onHide ();

            this.fireSideBarHiddenEvent (this.currentOtherSideBar);

        }

        this.currentOtherSideBar = null;

        this.showMainSideBar ();

    }

    public AbstractSideBar getSideBar (String id)
    {

        return this.sideBars.get (id);

    }

    public void removeSideBar (String id)
    {

        this.removeSideBar (this.getSideBar (id));

    }

    public void removeSideBar (AbstractSideBar sb)
    {

        if (sb == null)
        {

            return;

        }

        this.sideBars.remove (sb.getId ());

        this.activeSideBars.remove (sb);

        if (this.currentOtherSideBar == sb)
        {

            this.currentOtherSideBar = null;

        }

        try
        {

            // Close the sidebar down gracefully.
            sb.onHide ();

            this.fireSideBarHiddenEvent (sb);

            sb.onClose ();

        } catch (Exception e) {

            Environment.logError ("Unable to close sidebar: " + sb.getId (),
                                  e);

        }

        this.removeSideBarListener (sb);

        this.removeMainPanelListener (sb);

        AbstractSideBar _sb = (this.activeSideBars.size () > 0 ? this.activeSideBars.peek () : null);

        if (_sb != null)
        {

            this.showSideBar (_sb.getId ());

        } else {

            if (this.fsf == null)
            {

                this.setUILayout (this.layout);

            }

        }

    }

    public void addSideBar (AbstractSideBar sb)
                     throws GeneralException
    {

        if (sb == null)
        {

            throw new IllegalArgumentException ("No sidebar provided.");

        }

        String state = null;

        String id = sb.getId ();

        if (id != null)
        {

            state = this.proj.getProperty ("sidebarState-" + id);

        }

        sb.init (state);

        //sb.setName (name);

        this.sideBars.put (id,
                           sb);

        this.addMainPanelListener (sb);

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

        this.setUILayout (this.layout);

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

        this.finder.setFindText (text);

        this.finder.onShow ();

    }

    public boolean isMainSideBar (AbstractSideBar sb)
    {

        return this.mainSideBar == sb;

    }

    public boolean isMainSideBarId (String n)
    {

        return (n.equals (this.getMainSideBarId ()));

    }

    protected void setMainSideBar (AbstractSideBar sb)
    {

        this.mainSideBar = sb;

        this.sideBars.put (this.getMainSideBarId (),
                           sb);

        this.showMainSideBar ();

    }

    public void showMainSideBar ()
    {

        this.showSideBar (this.getMainSideBarId ());

    }

    public String getMainSideBarId ()
    {

        return this.mainSideBar.getId ();

    }

    public int getActiveSideBarCount ()
    {

        int c = this.activeSideBars.size ();

        if ((this.layout.equals (Constants.LEFT))
            ||
            (this.layout.equals (Constants.RIGHT))
            ||
            (this.layout.equals (Constants.LAYOUT_PS_CH))
            ||
            (this.layout.equals (Constants.LAYOUT_CH_PS))
           )
        {

            c++;

        }

        return c;

    }

    public JPopupMenu getShowOtherSideBarsPopupSelector ()
    {

        final AbstractProjectViewer _this = this;

        JPopupMenu m = new JPopupMenu ();

        if (this.currentOtherSideBar != null)
        {

            if ((this.layout.equals (Constants.LEFT))
                ||
                (this.layout.equals (Constants.RIGHT))
                ||
                (this.layout.equals (Constants.LAYOUT_PS_CH))
                ||
                (this.layout.equals (Constants.LAYOUT_CH_PS))
               )
            {

                JMenuItem mi = UIUtils.createMenuItem (this.mainSideBar.getActiveTitle (),
                                                       this.mainSideBar.getActiveIconType (),
                                                       new ActionListener ()
                                                       {

                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                _this.showMainSideBar ();

                                                            }

                                                       });

                m.add (mi);

            }

        }

        // Means we are showing the main sidebar and the other sidebar.
        // Exclude those from the list.
        for (AbstractSideBar sb : this.activeSideBars)
        {

            if ((this.currentOtherSideBar != null)
                &&
                (this.currentOtherSideBar == sb)
               )
            {

                continue;

            }

            final AbstractSideBar _sb = sb;

            JMenuItem mi = UIUtils.createMenuItem (sb.getActiveTitle (),
                                                   sb.getActiveIconType (),
                                                   new ActionListener ()
                                                   {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            _this.showSideBar (_sb.getId ());

                                                        }

                                                   });

            m.add (mi);

        }

        return m;

    }

    public void showSideBar (String id)
    {

        if ((this.currentOtherSideBar != null)
            &&
            (this.currentOtherSideBar.getId ().equals (id))
           )
        {

            return;

        }

        AbstractSideBar b = this.sideBars.get (id);

        if (b == null)
        {

            throw new IllegalArgumentException ("Unable to show sidebar: " +
                                                id +
                                                ", no sidebar found with that id.");

        }

        if (this.currentOtherSideBar != null)
        {

            // Will be hiding this one.
            this.currentOtherSideBar.onHide ();

            this.fireSideBarHiddenEvent (this.currentOtherSideBar);

        }

        if (id.equals (this.getMainSideBarId ()))
        {

            // Need to check the layout.  If we are only showing one sidebar then set the current other
            // to null.
            if (this.isUILayoutShowSingleSidebar ())
            {

                this.currentOtherSideBar = null;

            }

        } else {

            this.currentOtherSideBar = b;

            this.activeSideBars.remove (b);

            this.activeSideBars.push (b);

        }

        if (this.fsf != null)
        {

            if (this.currentOtherSideBar != null)
            {

                this.sideBarWrapper.removeAll ();
                this.sideBarWrapper.add (this.currentOtherSideBar);

            } else {

                this.sideBarWrapper.removeAll ();
                this.sideBarWrapper.add (this.mainSideBar);

            }

            this.fsf.showSideBar ();

        } else {

			this.setUILayout (this.layout);

		}

        try
        {

            b.onShow ();

        } catch (Exception e) {

            Environment.logError ("Unable to call onShow for sidebar: " +
                                  id +
                                  ", instance: " +
                                  b,
                                  e);

        }

        this.fireSideBarShownEvent (b);

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

    public abstract String getViewerTitle ();

    public abstract void reloadTreeForObjectType (String objType);

    public abstract void reloadTreeForObjectType (NamedObject obj);

    public abstract void showObjectInTree (String      treeObjType,
                                           NamedObject obj);

    public abstract AbstractSideBar getMainSideBar ();

    public abstract String getChapterObjectName ();

    public abstract void fillFullScreenTitleToolbar (JToolBar toolbar);

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

    public abstract Set<FindResultsBox> findText (String t);

    public Action getAction (int name)
    {

        return this.getAction (name,
                               null);

    }

    /**
     * Get the correct text properties depending on whether the viewer is in full screen or not.
     *
     * @returns Normal project text properties if we are in normal mode or full screen text properties
     *          if we are in full screen mode.
     */
    public TextProperties getTextProperties ()
    {

        return (this.fsf != null ? Environment.getFullScreenTextProperties () : Environment.getProjectTextProperties ());

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

					UIUtils.showCreateBackup (pv.getProject (),
											  null,
											  pv);

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

                    try
                    {

                        Environment.showLanding ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to show landing",
                                              e);

                        UIUtils.showErrorMessage (pv,
                                                  Environment.getUIString (LanguageStrings.project,
                                                                           LanguageStrings.actions,
                                                                           LanguageStrings.openproject));
                                                  //"Unable to show the {projects} window, please contact Quoll Writer support for assistance.");

                    }

                }

            };

        }

        if (name == AbstractProjectViewer.NEW_PROJECT_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    UIUtils.showAddNewProject (pv,
                                               null,
                                               null);

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

            return new DeleteProjectActionHandler (this,
                                                   this.getProject (),
                                                   null);

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

                    pv.showAbout ();

                }

            };

        }

        return super.getAction (name);

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

    public void doForSideBars (final Class              type,
                               final QuollSideBarAction act,
							   final boolean            doOnEventThread)
    {

		Set<AbstractSideBar> ps = new LinkedHashSet (this.sideBars.values ());

        for (AbstractSideBar sb : ps)
        {

            if (type.isAssignableFrom (sb.getClass ()))
            {

				final AbstractSideBar _sb = sb;

				if (doOnEventThread)
				{

					UIUtils.doLater (new ActionListener ()
					{

						@Override
						public void actionPerformed (ActionEvent ev)
						{

							act.doAction (_sb);

						}

					});

				} else {

					try
					{

						act.doAction (sb);

					} catch (Exception e) {

						Environment.logError ("Unable to perform action: " +
											  act,
											  e);

					}

				}

            }

        }

    }

    public void doForSideBars (final Class              type,
                               final QuollSideBarAction act)
    {

		this.doForSideBars (type,
						    act,
						    true);

    }

    public void doForPanels (final Class            type,
                             final QuollPanelAction act,
							 final boolean          doOnEventThread)
    {

		Set<QuollPanel> ps = new LinkedHashSet (this.panels.values ());

        for (QuollPanel p : ps)
        {

            if (type.isAssignableFrom (p.getClass ()))
            {

				final QuollPanel _p = p;

				if (doOnEventThread)
				{

					UIUtils.doLater (new ActionListener ()
					{

						@Override
						public void actionPerformed (ActionEvent ev)
						{

							act.doAction (_p);

						}

					});

				} else {

					try
					{

						act.doAction (p);

					} catch (Exception e) {

						Environment.logError ("Unable to perform action: " +
											  act,
											  e);

					}

				}

            }

        }

    }

    public void doForPanels (final Class            type,
                             final QuollPanelAction act)
    {

		this.doForPanels (type,
						  act,
						  true);

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

    /**
     * Set the spell check language for the project.  Note: this DOES NOT affect the project property:
     * {@link Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME}.  Also this does not affect the spell check enabled
     * flag.
     *
     * @param lang The language to use, set to <b>null</b> to switch off spell checking.
     * @param updateEditors Set to <b>true</b> to tell all the editor panels about the change.
     * @throws Exception If something goes wrong.
     */
    public void setSpellCheckLanguage (String  lang,
                                       boolean updateEditors)
                                throws Exception
    {

        if ((this.dictProv != null) &&
            (this.dictProv.getLanguage ().equals (lang))
           )
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

        this.dictProv = new DictionaryProvider (lang,
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

            this.showNotificationPopup (Environment.getUIString (LanguageStrings.functionunavailable,
                                                                 LanguageStrings.title),
                                                                 //"Function unavailable",
                                        Environment.getUIString (LanguageStrings.functionunavailable,
                                                                 LanguageStrings.text),
                                        //"Sorry, this function is only available when your spellchecker language is English.<br /><br /><a href='action:contact'>Click here to contact me to help add support for your language</a>",
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

        if (Environment.isEnglish (c))
        {

            c = Constants.ENGLISH;

        }

        return c;

    }

    public DictionaryProvider getDictionaryProvider ()
    {

        return this.dictProv;

    }

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

    @Override
    public AbstractSideBar getActiveOtherSideBar ()
    {

        if (this.activeSideBars.size () > 0)
        {

            return this.activeSideBars.peek ();

        }

        return null;

    }

    private boolean isUILayoutShowSingleSidebar ()
    {

        String layout = this.getUILayout ();

        if (layout.equals (Constants.LEFT))
        {

            layout = Constants.LAYOUT_PS_CH;

        }

        if (layout.equals (Constants.RIGHT))
        {

            layout = Constants.LAYOUT_CH_PS;

        }

        return (layout.equals (Constants.LAYOUT_PS_CH)
                ||
                layout.equals (Constants.LAYOUT_CH_PS));

    }

    /**
     * The layout is done in terms of preferred sizes and resize weights.
     * We then call resetToPreferredSizes on the split panes to resize.
     * The resize weights ensure that the tabs get all the extra space (without it one of the sidebars
     * could get the space).  Weights of 0.98f and 0.02f are used to ensure that the sidebars aren't shrunk below
     * their current size (can't work out why it does that...)
     *
     * @{link splitpane2} is nested within the right half of {@link splitpane}.
     *
     * A zero sized filler is used to when a "half" isn't used to ensure that the relevant splitpane
     * doesn't freak out. See {@link createLayoutFiller()}.
     */
    public void setUILayout (String layout)
    {

        final AbstractProjectViewer _this = this;
        final AbstractSideBar other = this.currentOtherSideBar;

        final Dimension min = this.sideBar.getMinimumSize ();

        int ww = this.splitPane.getSize ().width;

        if (ww == 0)
        {

            ww = this.proj.getPropertyAsInt (Constants.WINDOW_WIDTH_PROPERTY_NAME);

        }

        int sbw = this.savedSideBarWidth;

        if (sbw == 0)
        {

            sbw = this.sideBar.getSize ().width;

        } else {

            this.savedSideBarWidth = 0;

        }

        if (sbw == 0)
        {

            sbw = this.proj.getPropertyAsInt (Constants.PROJECT_SIDE_BAR_WIDTH_PROPERTY_NAME);

            if (sbw <= 0)
            {

                // Legacy, pre-v2.3
                int spd = this.proj.getPropertyAsInt (Constants.SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME);

                if (spd > 0)
                {

                    sbw = spd;

                    this.proj.removeProperty (Constants.PROJECT_SIDE_BAR_WIDTH_PROPERTY_NAME);

                } else {

                    sbw = this.proj.getPropertyAsInt (Constants.PROJECT_SIDE_BAR_WIDTH_PROPERTY_NAME);

                }

            }

            if (sbw == 0)
            {

                sbw = min.width;

            }

        }

        if (min.width > sbw)
        {

            sbw = min.width;

        }

        int h = this.splitPane.getSize ().height;

        if (h == 0)
        {

            h = 200;

        }

        int w = ww - sbw - INTERNAL_SPLIT_PANE_DIVIDER_WIDTH;
        int ow = 0;

        if (other != null)
        {

            ow = this.savedOtherSideBarWidth;

            if (ow == 0)
            {

                ow = other.getMinimumSize ().width - INTERNAL_SPLIT_PANE_DIVIDER_WIDTH;

            } else {

                this.savedOtherSideBarWidth = 0;

            }

            w -= ow;

            other.setPreferredSize (new Dimension (ow,
                                                   h));

        }

        //this.tabs.setPreferredSize (new Dimension (w, 100));

        //this.sideBar.setPreferredSize (new Dimension (sbw, 200));

        final int fw = w;
        final int fow = ow;
        final int fsbw = sbw;

        // Legacy, pre-2.3
        if (layout.equals (Constants.LEFT))
        {

            layout = Constants.LAYOUT_PS_CH;

        }

        if (layout.equals (Constants.RIGHT))
        {

            layout = Constants.LAYOUT_CH_PS;

        }

        if (layout.equals (Constants.LAYOUT_PS_CH_OS))
        {

            this.splitPane.setLeftComponent (this.sideBar);
            this.splitPane.setDividerSize (INTERNAL_SPLIT_PANE_DIVIDER_WIDTH);
            this.splitPane.setResizeWeight (0f);
            this.splitPane.setDividerLocation (this.sideBar.getPreferredSize ().width);

            this.splitPane2.setLeftComponent (this.tabs);
            this.splitPane2.setRightComponent (this.createLayoutFiller ());
            this.splitPane2.setDividerSize (0);

            this.sideBarWrapper.removeAll ();
            this.sideBarWrapper.add (this.mainSideBar);

            this.splitPane.setDividerLocation (fsbw);//this.sideBar.getPreferredSize ().width);

            // Is a non-main sidebar visible?
            if (other != null)
            {

                this.splitPane2.setResizeWeight (1f);
                this.splitPane2.setRightComponent (other);
                this.splitPane2.setDividerSize (INTERNAL_SPLIT_PANE_DIVIDER_WIDTH);
                this.splitPane2.setDividerLocation (fw);

                UIUtils.doLater (new ActionListener ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        // For reasons best known to swing this has to be done separately otherwise when
                        // you exit full screen it will have the other sidebar as taking up all the space.
                        _this.splitPane.setDividerLocation (fsbw);
                        _this.splitPane2.setDividerLocation (fw);

						_this.validate ();
						_this.repaint ();

                    }

                });

            }

        }

        if (layout.equals (Constants.LAYOUT_OS_CH_PS))
        {

            this.splitPane.setLeftComponent (this.tabs);
            this.splitPane.setResizeWeight (1f);
            this.splitPane.setDividerSize (INTERNAL_SPLIT_PANE_DIVIDER_WIDTH);

            this.splitPane2.setLeftComponent (this.createLayoutFiller ());
            this.splitPane2.setRightComponent (this.sideBar);
            this.splitPane2.setDividerSize (0);

            this.sideBarWrapper.removeAll ();
            this.sideBarWrapper.add (this.mainSideBar);

            this.splitPane.setDividerLocation (fw);

            // Is a non-main sidebar visible?
            if (other != null)
            {

                this.splitPane.setResizeWeight (0);
                this.splitPane2.setResizeWeight (1f);

                this.splitPane.setLeftComponent (other);
                this.splitPane.setResizeWeight (0);

                this.splitPane2.setLeftComponent (this.tabs);
                this.splitPane2.setDividerSize (INTERNAL_SPLIT_PANE_DIVIDER_WIDTH);
                this.splitPane.setDividerLocation (fow);
                this.splitPane2.setDividerLocation (fw);

                UIUtils.doLater (new ActionListener ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        // For reasons best known to swing this has to be done separately otherwise when
                        // you exit full screen it will have the other sidebar as taking up all the space.
                        _this.splitPane.setDividerLocation (fow);
                        _this.splitPane2.setDividerLocation (fw);

						_this.validate ();
						_this.repaint ();

                    }

                });

            }

        }

        if (layout.equals (Constants.LAYOUT_PS_OS_CH))
        {

            this.splitPane.setLeftComponent (this.sideBar);
            this.splitPane.setDividerSize (INTERNAL_SPLIT_PANE_DIVIDER_WIDTH);
            this.splitPane.setResizeWeight (0f);

            this.splitPane2.setLeftComponent (this.createLayoutFiller ());
            this.splitPane2.setRightComponent (this.tabs);
            //this.splitPane2.setResizeWeight (0.02f);
            this.splitPane2.setDividerSize (0);

            this.sideBarWrapper.removeAll ();
            this.sideBarWrapper.add (this.mainSideBar);

            this.splitPane.setDividerLocation (fsbw);

            // Is a non-main sidebar visible?
            if (other != null)
            {

                //this.splitPane.setResizeWeight (0.5f);

                this.splitPane2.setLeftComponent (other);
                this.splitPane2.setDividerSize (INTERNAL_SPLIT_PANE_DIVIDER_WIDTH);
                this.splitPane2.setResizeWeight (0f);

                this.splitPane2.setDividerLocation (fow);
                this.splitPane.setDividerLocation (fsbw);

                UIUtils.doLater (new ActionListener ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        // For reasons best known to swing this has to be done separately otherwise when
                        // you exit full screen it will have the other sidebar as taking up all the space.
                        _this.splitPane2.setDividerLocation (fow);
                        _this.splitPane.setDividerLocation (fsbw);

						_this.validate ();
						_this.repaint ();

                    }

                });

            }

        }

        if (layout.equals (Constants.LAYOUT_CH_OS_PS))
        {

            this.splitPane.setLeftComponent (this.tabs);
            this.splitPane.setDividerSize (INTERNAL_SPLIT_PANE_DIVIDER_WIDTH);

            this.splitPane2.setLeftComponent (this.createLayoutFiller ());
            this.splitPane2.setRightComponent (this.sideBar);
            this.splitPane2.setDividerSize (0);

            this.sideBarWrapper.removeAll ();
            this.sideBarWrapper.add (this.mainSideBar);

            this.splitPane.setDividerLocation (fw);

            // Is a non-main sidebar visible?
            if (other != null)
            {

                this.splitPane.setResizeWeight (1f);

                this.splitPane2.setLeftComponent (other);
                this.splitPane2.setDividerSize (INTERNAL_SPLIT_PANE_DIVIDER_WIDTH);

                this.splitPane2.setDividerLocation (fow);
                this.splitPane.setDividerLocation (fw);

                UIUtils.doLater (new ActionListener ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        // For reasons best known to swing this has to be done separately otherwise when
                        // you exit full screen it will have the other sidebar as taking up all the space.
                        _this.splitPane2.setDividerLocation (fow);
                        _this.splitPane.setDividerLocation (fw);

						_this.validate ();
						_this.repaint ();

                    }

                });

            }

        }

        if (layout.equals (Constants.LAYOUT_PS_CH))
        {

            this.splitPane.setLeftComponent (this.sideBar);
            this.splitPane.setDividerSize (INTERNAL_SPLIT_PANE_DIVIDER_WIDTH);
            this.splitPane.setResizeWeight (0f);

            this.splitPane2.setLeftComponent (this.tabs);
            this.splitPane2.setResizeWeight (1f);
            this.splitPane2.setRightComponent (this.createLayoutFiller ());
            this.splitPane2.setDividerSize (0);

            if (other != null)
            {

                if (other.getMinimumSize ().width > sbw)
                {

                    sbw = other.getMinimumSize ().width;

                }

                this.sideBarWrapper.removeAll ();
                this.sideBarWrapper.add (other);

            } else {

                if (this.mainSideBar != null)
                {

                    if (this.mainSideBar.getMinimumSize ().width > sbw)
                    {

                        sbw = this.mainSideBar.getMinimumSize ().width;

                    }

                    this.sideBarWrapper.removeAll ();
                    this.sideBarWrapper.add (this.mainSideBar);

                }

            }

            final int fsbw2 = sbw;

            this.splitPane.setDividerLocation (sbw);

            UIUtils.doLater (new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    // For reasons best known to swing this has to be done separately otherwise when
                    // you exit full screen it will have the other sidebar as taking up all the space.
                    _this.splitPane.setDividerLocation (fsbw2);

					_this.validate ();
					_this.repaint ();

                }

            });

        }

        if (layout.equals (Constants.LAYOUT_CH_PS))
        {

            w += ow;

            this.splitPane.setLeftComponent (this.tabs);
            this.splitPane.setDividerSize (INTERNAL_SPLIT_PANE_DIVIDER_WIDTH);
            this.splitPane.setResizeWeight (1f);

            this.splitPane2.setLeftComponent (this.sideBar);
            this.splitPane2.setRightComponent (this.createLayoutFiller ());
            this.splitPane2.setDividerSize (0);

            if (other != null)
            {

                if (other.getMinimumSize ().width > sbw)
                {

                    w = this.splitPane.getSize ().width - other.getMinimumSize ().width;

                }

                this.sideBarWrapper.removeAll ();
                this.sideBarWrapper.add (other);

            } else {

                if (this.mainSideBar != null)
                {

                    if (this.mainSideBar.getMinimumSize ().width > sbw)
                    {

                        w = this.splitPane.getSize ().width - this.mainSideBar.getMinimumSize ().width;

                    }

                    this.sideBarWrapper.removeAll ();
                    this.sideBarWrapper.add (this.mainSideBar);

                }

            }

            this.splitPane.setDividerLocation (w);

            final int fw2 = w;

            UIUtils.doLater (new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    // For reasons best known to swing this has to be done separately otherwise when
                    // you exit full screen it will have the other sidebar as taking up all the space.
                    _this.splitPane.setDividerLocation (fw2);

					_this.validate ();
					_this.repaint ();

                }

            });

        }

        this.layout = layout;

    }

    private String getUILayout ()
    {

        String sidebarLoc = UserProperties.get (Constants.SIDEBAR_LOCATION_PROPERTY_NAME);

        String uiLayout = Constants.LAYOUT_CH_PS;

        // Legacy, pre-v2.5
        if (sidebarLoc != null)
        {

            if (sidebarLoc.equals (Constants.RIGHT))
            {

                uiLayout = Constants.LAYOUT_CH_PS;

            }

            UserProperties.remove (Constants.SIDEBAR_LOCATION_PROPERTY_NAME);

        } else {

            uiLayout = UserProperties.get (Constants.UI_LAYOUT_PROPERTY_NAME);

        }

        // Legacy, pre-2.5
        if (uiLayout.equals (Constants.LEFT))
        {

            uiLayout = Constants.LAYOUT_PS_CH;

        }

        if (uiLayout.equals (Constants.RIGHT))
        {

            uiLayout = Constants.LAYOUT_CH_PS;

        }

        return uiLayout;

    }

    protected void initWindow ()
    {

        if (this.proj == null)
        {

            return;

        }

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

        String sidebarLoc = UserProperties.get (Constants.SIDEBAR_LOCATION_PROPERTY_NAME);

        String uiLayout = this.getUILayout ();

        this.setUILayout (uiLayout);

        this.setTabsLocation (this.proj.getProperty (Constants.TABS_LOCATION_PROPERTY_NAME));

        this.setViewerTitle (this.getViewerTitle ());

        this.setIconImage (Environment.getWindowIcon ().getImage ());

        //this.updateForDebugMode ();

        //this.pack ();

        // Allow the underlying Windowing manager determine where to put the window.
        //this.setLocationByPlatform (true);

        //this.setVisible (true);

        //Environment.doNewsAndVersionCheck (this);

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

    }

    public void newProject (File    saveDir,
                            Project p,
                            String  filePassword)
                     throws Exception
    {

        this.newProject (saveDir,
                         p,
                         filePassword,
                         null);

    }

    public void newProject (File           saveDir,
                            Project        p,
                            String         filePassword,
                            ActionListener onOpen)
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

        this.dBMan = Environment.createProject (saveDir,
                                                p,
                                                filePassword);

        this.proj = this.dBMan.getProject ();
		this.proj.setFilePassword (filePassword);

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

                this.saveObject (b,
                                 true);

            }

        }

        this.proj.addPropertyChangedListener (this);

		this.targets = new TargetsData (this.proj.getProperties ());

        Environment.addToAchievementsManager (this);

        this.initSideBars ();

        this.initDictionaryProvider ();

        this.handleNewProject ();

		this.initChapterCounts ();

        this.setSpellCheckingEnabled (this.proj.getPropertyAsBoolean (Constants.SPELL_CHECKING_ENABLED_PROPERTY_NAME));

        this.setSplitPaneColor ();

		this.startAutoBackups ();

        this.initWindow ();

        this.showMainSideBar ();

        //this.handleWhatsNew ();

        //this.handleShowTips ();

        this.setIgnoreProjectEvents (false);

        this.fireProjectEvent (this.proj.getObjectType (),
                               ProjectEvent.NEW);

        this.showViewer ();

        // Register ourselves with the environment.
        try
        {

            Environment.addOpenedProject (this);

        } catch (Exception e)
        {

            Environment.logError ("Unable to add opened project (probably an error with the projects file): " +
                                  this.proj,
                                  e);

        }

        this.scheduleA4PageCountUpdate ();

		UIUtils.doLater (onOpen);

    }

    private void scheduleA4PageCountUpdate ()
    {

        final AbstractProjectViewer _this = this;

        // Generate the A4 page counts.
        this.schedule (new Runnable ()
        {

            @Override
            public void run ()
            {

                Book b = _this.proj.getBooks ().get (0);

                java.util.List<Chapter> chapters = b.getChapters ();

                for (Chapter c : chapters)
                {

                    String t = _this.getCurrentChapterText (c);

					ChapterCounts cc = _this.getChapterCounts (c);

                    try
                    {

                        cc.standardPageCount = UIUtils.getA4PageCountForChapter (c,
                                                                                 t);

                    } catch (Exception e) {

                        // Just ignore any errors, it's next to impossible to stop them.

                    }

                }

            }

        },
        1,
        0);

    }

    public void setToolbarVisible (boolean v)
    {

        this.toolbarPanel.setVisible (v);

    }

    public void openProject (ProjectInfo    p,
                             String         filePassword)
                      throws Exception
	{

		this.openProject (p,
						  filePassword,
						  null);

	}

    public void openProject (ProjectInfo    p,
                             String         filePassword,
							 ActionListener onOpen)
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

        this.dBMan.init (new File (p.getProjectDirectory ().getPath (), Constants.PROJECT_DB_FILE_NAME_PREFIX),
                         username,
                         password,
                         filePassword,
                         Environment.getSchemaVersion ());

        Environment.incrStartupProgress ();

        // Get the project.

        try
        {

            this.proj = this.dBMan.getProject ();

        } catch (Exception e) {

            // This means we can't open the project and something is wrong, close the dbman to prevent
            // it locking the project open but unusable.
            this.dBMan.closeConnectionPool ();

            throw e;

        }

        Environment.incrStartupProgress ();

		this.proj.setFilePassword (filePassword);
        this.proj.setProjectDirectory (p.getProjectDirectory ());
		this.proj.setBackupDirectory (p.getBackupDirectory ());
        //this.proj.setFilePassword (filePassword);
        this.proj.setEncrypted (p.isEncrypted ());
        this.proj.setNoCredentials (p.isNoCredentials ());

        this.proj.addPropertyChangedListener (this);

		this.initChapterCounts ();

		this.targets = new TargetsData (this.proj.getProperties ());

        Environment.incrStartupProgress ();

        Environment.addToAchievementsManager (this);

        this.initSideBars ();

        this.initDictionaryProvider ();

        this.handleOpenProject ();

        Environment.incrStartupProgress ();

        final java.util.List<Runner> inits = new ArrayList ();

        this.restoreTabs ();

        Environment.incrStartupProgress ();

        this.initWindow ();

        this.setSplitPaneColor ();

        // Init all the panels.
        for (QuollPanel qp : this.panels.values ())
        {

            Runner r = this.getInitPanelStateRunner (qp,
                                                     (qp == this.getCurrentlyVisibleTab ()));
                                                     //lastOpen.equals (qp.getPanelId ()));

            // The init for the panel should have set it as ready for use so switch that off.
            qp.setReadyForUse (false);

            if (r != null)
            {

                inits.add (r);

            }

        }

        UIUtils.doLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                for (Runner r : inits)
                {

                    r.run ();

                }

            }

        });

        Environment.incrStartupProgress ();

		this.startAutoBackups ();

		this.scheduleAutoSaveForAllEditors ();

        Environment.incrStartupProgress ();

        this.dBMan.createActionLogEntry (this.proj,
                                         "Opened project",
                                         null,
                                         null);

        final AbstractProjectViewer _this = this;

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
                                     String.format (Environment.getUIString (LanguageStrings.fontunavailable,
                                                                             LanguageStrings.text),
                                                    f));
                //"The font <b>" + f + "</b> selected for use in {chapters} is not available on this computer.<br /><br />To select a new font, switch to a chapter tab then <a href='action:textproperties'>click here to change the text properties</a>.");

            }

        }

        //this.handleWhatsNew ();

        //this.handleShowTips ();

        this.setIgnoreProjectEvents (false);

        this.fireProjectEvent (this.proj.getObjectType (),
                               ProjectEvent.OPEN);

        this.showViewer ();

        // Register ourselves with the environment.
        try
        {

            Environment.addOpenedProject (this);

        } catch (Exception e)
        {

            Environment.logError ("Unable to add opened project: " +
                                  this.proj,
                                  e);

        }

        this.scheduleA4PageCountUpdate ();

		UIUtils.doLater (onOpen);

		// Check to see if any chapters have overrun the target.
		UIUtils.doLater (new ActionListener ()
		{

			@Override
			public void actionPerformed (ActionEvent ev)
			{

				try
				{

					int wc = _this.getProjectTargets ().getMaxChapterCount ();

					Set<Chapter> chaps = _this.getChaptersOverWordTarget ();

					int s = chaps.size ();

					if ((wc > 0)
						&&
						(s > 0)
					   )
					{

						for (Chapter c : chaps)
						{

							_this.chapterWordCountTargetWarned.put (c,
																	new Date ());

						}

						final JLabel l = UIUtils.createLabel (null,
															  null,
															  null);

                        String t = LanguageStrings.single;

                        if (s > 1)
                        {

                            t = LanguageStrings.multiple;

                        }

                        String text = String.format (Environment.getUIString (LanguageStrings.targets,
                                                                              LanguageStrings.chaptersoverwcmaximum,
                                                                              t),
                                                     Environment.formatNumber (chaps.size ()),
                                                                    //"%s {chapter}%s over the word count maximum of <b>%s</b> words, click to view the {chapter}%s.",
  													 Environment.formatNumber (wc));

  						l.setText (text);

						final Notification n = _this.addNotification (l,
																	  Constants.WORDCOUNT_ICON_NAME,
																	  90);

						UIUtils.makeClickable (l,
											   new ActionListener ()
											   {

													@Override
													public void actionPerformed (ActionEvent ev)
													{

														Targets.showChaptersOverWordTarget (_this,
																							n);

														n.removeNotification ();

													}

											   });

					}

					chaps = _this.getChaptersOverReadabilityTarget ();

					s = chaps.size ();

					if (s > 0)
					{

                        String t = LanguageStrings.single;

                        if (s > 1)
                        {

                            t = LanguageStrings.multiple;

                        }

						final JLabel l = UIUtils.createLabel (null,
															  null,
															  null);

                        String text = String.format (Environment.getUIString (LanguageStrings.targets,
                                                                              LanguageStrings.chaptersoverreadabilitymaximum,
                                                                              t),
                                                     //"%s {chapter}%s over one of the readability targets, click to view the {chapter}%s.",
  													 Environment.formatNumber (chaps.size ()));
  													 //(s == 1 ? " is" : "s are"),
  													 //(s == 1 ? "" : "s"));

  						l.setText (text);

						final Notification n = _this.addNotification (l,
																	  Constants.READABILITY_ICON_NAME,
																	  90);

						UIUtils.makeClickable (l,
											   new ActionListener ()
											   {

													@Override
													public void actionPerformed (ActionEvent ev)
													{

														Targets.showChaptersOverReadabilityTarget (_this,
																								   n);

														n.removeNotification ();

													}

											   });

					}

				} catch (Exception e) {

					Environment.logError ("Unable to display chapters over target notification",
										  e);

				}

			}

		});

		this.schedule (new Runnable ()
		{

			@Override
			public void run ()
			{

				try
				{

					int wc = _this.getProjectTargets ().getMaxChapterCount ();

					if ((wc > 0)
						&&
						(_this.getProjectTargets ().isShowMessageWhenMaxChapterCountExceeded ())
					   )
					{

						Book b = _this.proj.getBooks ().get (0);

						final Set<Chapter> over = new LinkedHashSet ();

						java.util.List<Chapter> chapters = b.getChapters ();

						Date d = new Date ();

						// 15 minutes ago.
						long last = System.currentTimeMillis () - 15 * 60 * 1000;

						for (Chapter c : chapters)
						{

						    ChapterCounts cc = _this.getChapterCounts (c);

							final Chapter _c = c;

							if (cc.wordCount > wc)
							{

								if (!_this.chapterWordCountTargetWarned.containsKey (c))
								{

									_this.chapterWordCountTargetWarned.put (c,
																			new Date ());

									over.add (c);

								}

							} else {

								Date od = _this.chapterWordCountTargetWarned.get (c);

								// Only remove if it's been 15 minutes since we last warned the user.
								// This provides a buffer so that they aren't constantly nagged about
								// it going over, for example if they've deleted/edited a sentence, removed
								// a word or two to go below the target then added some back in.
								if ((od != null)
									&&
									(od.getTime () < last)
								   )
								{

									_this.chapterWordCountTargetWarned.remove (c);

								}

							}

						}

						if (over.size () > 0)
						{

							// Show a message.
							UIUtils.doLater (new ActionListener ()
							{

								@Override
								public void actionPerformed (ActionEvent ev)
								{

									final int s = over.size ();

									final QPopup popup = UIUtils.createClosablePopup (Environment.getUIString (LanguageStrings.targets,
                                                                                                               LanguageStrings.popup,
                                                                                                               LanguageStrings.title),
                                                                                      //String.format ("{Chapter}%s over word count target",
																									 //(s == 1 ? "" : "s")),
																					  Environment.getIcon (Constants.WORDCOUNT_ICON_NAME,
																										   Constants.ICON_POPUP),
																					  null);
/*
									String text = String.format ("%s {chapter}%s over the target word count, click to view them.",
																 Environment.formatNumber (s),
																 (s == 1 ? " is" : "s are"));
*/
                                    String text = null;

									if (s == 1)
									{

										Chapter c = over.iterator ().next ();

                                        text = String.format (Environment.getUIString (LanguageStrings.targets,
                                                                                       LanguageStrings.popup,
                                                                                       LanguageStrings.singleoverlimit),
                                                              c.getName ());

										//text = String.format ("{Chapter} <b>%s</b> is over the target word count, click to view it.",
											//				  c.getName ());

									} else {

                                        text = String.format (Environment.getUIString (LanguageStrings.targets,
                                                                                       LanguageStrings.popup,
                                                                                       LanguageStrings.multipleoverlimit),
                                                              Environment.formatNumber (s));

                                    }

									Box content = new Box (BoxLayout.Y_AXIS);

									ActionListener showChapter = new ActionListener ()
									{

										@Override
										public void actionPerformed (ActionEvent ev)
										{

											Targets.showChaptersOverWordTarget (_this,
																				null);

											popup.removeFromParent ();

										}

									};

									JLabel l = UIUtils.createLabel (text,
																	null,
																	showChapter);

									l.setBorder (UIUtils.createPadding (0, 0, 10, 0));

									content.setBorder (UIUtils.createPadding (10, 10, 10, 10));

									content.add (l);

									JButton cb = UIUtils.createButton (Constants.CLOSE_BUTTON_LABEL_ID);

									JButton sb = UIUtils.createButton (Environment.getUIString (LanguageStrings.buttons,
                                                                                                LanguageStrings.showdetail),
                                                                       //"Show detail",
																	   showChapter);

									JButton[] buts = { sb, cb };

									JComponent bs = UIUtils.createButtonBar2 (buts,
																			  Component.CENTER_ALIGNMENT);
									bs.setAlignmentX (Component.LEFT_ALIGNMENT);

									content.add (bs);

									cb.addActionListener (popup.getCloseAction ());
									popup.setRemoveOnClose (true);

									popup.setContent (content);

									popup.setDraggable (_this);

									popup.resize ();

									_this.showPopupAt (popup,
													   new Point (10, 10),
													   false);

								}

							});

						}

					}

				} catch (Exception e) {

					Environment.logError ("Unable to determine chapters that are over target",
										  e);

				}

			}

		},
		30 * 1000,
		5 * 1000);

    }

	public void saveProjectTargets ()
	{

		try
		{

			this.saveObject (this.proj,
							 false);

		} catch (Exception e) {

			Environment.logError ("Unable to update project targets",
								  e);

		}

	}

	public TargetsData getProjectTargets ()
	{

		return this.targets;

	}

	private void startAutoBackups ()
	{

		final AbstractProjectViewer _this = this;

		this.schedule (new Runnable ()
		{

			public void run ()
			{

				try
				{

					// Get references first so that they can't change further on.
					Project proj = _this.proj;
					ObjectManager dBMan = _this.dBMan;

					if ((proj == null)
						||
						(dBMan == null)
					   )
					{

						return;

					}

					if (proj.getPropertyAsBoolean (Constants.AUTO_SNAPSHOTS_ENABLED_PROPERTY_NAME))
					{

						// Get the last backup.
						File last = dBMan.getLastBackupFile (proj);

						long lastDate = (last != null ? last.lastModified () : proj.getDateCreated ().getTime ());

						if ((System.currentTimeMillis () - lastDate) > Utils.getTimeAsMillis (proj.getProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME)))
						{

                            Environment.createBackupForProject (proj,
                                                                false);

							UIUtils.doLater (new ActionListener ()
							{

								@Override
								public void actionPerformed (ActionEvent ev)
								{

									try
									{

										_this.addNotification (String.format (Environment.getUIString (LanguageStrings.backups,
                                                                                                       LanguageStrings.autobackupnotification),
                                                                              //"An automatic backup has been created.  <a href='%s:%s'>Click to view the backups.</a>",
                                                                              Constants.ACTION_PROTOCOL,
                                                                              Constants.BACKUPS_HTML_PANEL_ACTION),
															  Constants.INFO_ICON_NAME,
															  30);

									} catch (Exception e) {

										// Sigh...

									}

								}

							});

						}

					}

				} catch (Exception e) {

					if (_this.proj == null)
					{

						// Means we have shutdown.
						return;

					}

					Environment.logError ("Unable to create backup for project: " +
										  _this.proj,
										  e);

				}

			}

		},
		// Start straight away.
		0,
		// Run every 10 mins.
		10 * 60 * 1000);

	}

    /**
     * Given a list of comma separated object ids, open the panels for the objects (if available).
     * So an example <b>ids</b> might be: chapter-1,character-2,chapter-3.
     * This will be parsed into 3 values and then the relevant objects searched for in the project.
     * If there is an associated object then it will be opened for viewing.
     *
     * @param ids The object ids, comma separated.
     */
    public void openPanelsFromObjectIdList (String ids)
    {

        if ((ids == null)
            ||
            (ids.trim ().equals (""))
           )
        {

            return;

        }

        java.util.List<String> objIds = new ArrayList ();

        // Split on :
        StringTokenizer t = new StringTokenizer (ids,
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

    public String getOpenTabsProperty ()
    {

        ProjectVersion pv = this.proj.getProjectVersion ();

        String suffix = "";

        // See if we have a project version.
        if (pv != null)
        {

            suffix = ":" + pv.getKey ();

        }

        // Setup the tabs.
        String openTabs = this.proj.getProperty (Constants.OPEN_TABS_PROPERTY_NAME + suffix);

        return openTabs;

    }

    /**
     * Responsible for setting up the panels that should be open when the project is opened.
     * If you override this method ensure that you do:
     *   * Open the necessary panels.
     *   * Call this.initWindow to restore the window state.
     *   * Set the last opened tab (after opening it).
     */
    protected void restoreTabs ()
    {

        ProjectVersion pv = this.proj.getProjectVersion ();

        String suffix = "";

        // See if we have a project version.
        if (pv != null)
        {

            suffix = ":" + pv.getKey ();

        }

        // Setup the tabs.
        String openTabs = this.getOpenTabsProperty ();

        this.openPanelsFromObjectIdList (openTabs);

        String lastOpen = this.proj.getProperty (Constants.LAST_OPEN_TAB_PROPERTY_NAME + suffix);

        if (lastOpen != null)
        {

            final QuollPanel qp = this.getQuollPanel (lastOpen);

            if (qp != null)
            {

                this.setPanelVisible (qp);

            }

        }

    }

    private void initDictionaryProvider ()
    {

        final String lang = this.getSpellCheckLanguage ();

        try
        {

            if (!DictionaryProvider.isLanguageInstalled (lang))
            {

                final AbstractProjectViewer _this = this;

                // Turn off spell checking until the download is complete.
                this.setSpellCheckLanguage (null,
                                            true);

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
                                                                                    String.format (Environment.getUIString (LanguageStrings.spellchecker,
                                                                                                                            LanguageStrings.unabletosetlanguage),
                                                                                                   lang));
                                                                                    //"Unable to set spell check language to <b>" +
                                                                                    //lang +
                                                                                    //"</b>.<br /><br />Please contact Quoll Writer support for assistance.");

                                                      }

                                                  }

                                              });

            } else {

                this.setSpellCheckLanguage (lang,
                                            true);

            }

        } catch (Exception e) {

            Environment.logError ("Unable to check for spell check language",
                                  e);

        }

    }

    public void downloadDictionaryFiles (String         lang,
                                         final ActionListener onComplete)
    {

		UIUtils.downloadDictionaryFiles (lang,
										 this,
										 onComplete);

    }

    private void initPanelState (QuollPanel qp)
    {

        Runner r = this.getInitPanelStateRunner (qp,
                                                 true);

        if (r != null)
        {

			try
			{

				r.run ();

			} catch (Exception e) {

				Environment.logError ("Unable to init panel: " +
									  qp.getPanelId (),
									  e);

			}

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

                    qp.setReadyForUse (true);

                }

            };

        }

        return null;

    }

    public void handleHTMLPanelAction (String v)
    {

        try
        {

            if (v.equals (Constants.BACKUPS_HTML_PANEL_ACTION))
            {

                UIUtils.showManageBackups (Environment.getProjectInfo (this.proj),
                                           this);

                return;

            }

            if (v.equals ("textproperties"))
            {

                QuollPanel qp = this.getCurrentlyVisibleTab ();

                if (qp instanceof AbstractEditorPanel)
                {

                    ((AbstractEditorPanel) qp).showTextProperties ();

                }

                return;

            }

            if (v.equals ("find"))
            {

                this.showFind (null);

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

            if (v.equals ("statistics"))
            {

                this.viewStatistics ();

                return;

            }

            if (v.equals ("wordcounts"))
            {

                this.viewWordCounts ();

                return;

            }

            if (v.equals ("wordcounthistory"))
            {

                this.viewStatistics ();

                return;

            }

            if (v.equals ("projectoptions"))
            {

                this.showOptions ("project");

                return;

            }

            if (v.equals ("dictionarymanager"))
            {

                this.showDictionaryManager ();

                return;

            }

            if (v.equals ("enabletypewritersound"))
            {

				boolean old = Environment.isPlaySoundOnKeyStroke ();

				Environment.setPlaySoundOnKeyStroke (true);

				Environment.playKeyStrokeSound ();

				Environment.setPlaySoundOnKeyStroke (old);

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

            super.handleHTMLPanelAction (v);

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

            ap = new AchievementsPanel (this);

            ap.init ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to view achievements: " +
                                  this.proj,
                                  e);

            UIUtils.showErrorMessage (this,
                                      Environment.getUIString (LanguageStrings.achievementspanel,
                                                               LanguageStrings.actionerror));
            //"Unable to view achievements");

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

        if (id.equals (StatisticsPanel.PANEL_ID))
        {

            return this.viewStatistics ();

        }

        if (id.equals (StatisticsPanel.OLD_WORD_COUNT_PANEL_ID))
        {

            return this.viewStatistics ();

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

    protected void closeAllTabs (boolean saveState)
    {

        if (saveState)
        {

            try
            {

                this.saveState ();

            } catch (Exception e) {

                Environment.logError ("Unable to save state",
                                      e);

            }

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

    }

    private boolean closeInternal (boolean        saveUnsavedChanges,
                                   ActionListener afterClose)
    {

        if (saveUnsavedChanges)
        {

            // Save all.
            for (QuollPanel qp : this.panels.values ())
            {

				if (qp instanceof ProjectObjectQuollPanel)
				{

					ProjectObjectQuollPanel pqp = (ProjectObjectQuollPanel) qp;

					if (pqp.hasUnsavedChanges ())
					{

						boolean showError = true;

						try
						{

							showError = !pqp.saveUnsavedChanges ();

						} catch (Exception e)
						{

							Environment.logError ("Unable to save unsaved changes for: " +
												  pqp.getForObject (),
												  e);

						}

						if (showError)
						{

							UIUtils.showErrorMessage (this,
													  String.format (Environment.getUIString (LanguageStrings.closeproject,
                                                                                              LanguageStrings.actionerror),
                                                                     pqp.getTitle ()));
                                                                     //"Unable to save: <b>" +
													  //pqp.getTitle () +
													  //"</b>, aborting exit.");

							// Switch to the tab.
							this.viewObject (pqp.getForObject ());

							return false;

						}

					}

				}

            }

        }

        // Close all sidebars.
        // TODO: Fix this up
        for (AbstractSideBar sb : new ArrayList<AbstractSideBar> (this.activeSideBars))
        {

            this.removeSideBar (sb);

        }

        //this.notifications.setVisible (false);

        this.removeAllNotifications ();

        this.closeAllTabs (true);

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

        this.proj.removePropertyChangedListener (this);

        ChapterDataHandler ch = (ChapterDataHandler) this.dBMan.getHandler (Chapter.class);

        ch.saveWordCounts (this.proj,
                           this.sessionStart,
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

        super.close (false,
                     null);

        try
        {

            Environment.projectClosed (this,
                                       afterClose);

            return true;

        } catch (Exception e)
        {

            Environment.logError ("Unable to close project",
                                  e);

            Environment.showLanding ();

            return false;

        }

    }

    @Override
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

				if (qp instanceof ProjectObjectQuollPanel)
				{

					ProjectObjectQuollPanel pqp = (ProjectObjectQuollPanel) qp;

					if (pqp.hasUnsavedChanges ())
					{

						hasChanges = true;

						if (pqp.getForObject () instanceof NamedObject)
						{

							b.append ("<li>" + pqp.getTitle () + "</li>");

						}

					}

				}

            }

            if (hasChanges)
            {

                final AbstractProjectViewer _this = this;

                Map<String, ActionListener> buttons = new LinkedHashMap ();

                buttons.put (Environment.getUIString (LanguageStrings.buttons,
                                                      LanguageStrings.savechanges),
                             //"Yes, save the changes",
                            new ActionListener ()
                            {

                               public void actionPerformed (ActionEvent ev)
                               {

                                   _this.closeInternal (true,
                                                        afterClose);

                               }

                            });

                buttons.put (Environment.getUIString (LanguageStrings.buttons,
                                                      LanguageStrings.discardchanges),
                             //"No, discard the changes",
                            new ActionListener ()
                            {

                               public void actionPerformed (ActionEvent ev)
                               {

                                   _this.closeInternal (false,
                                                        afterClose);

                               }

                            });

                buttons.put (Environment.getButtonLabel (Constants.CANCEL_BUTTON_LABEL_ID),
                             null);

                UIUtils.createQuestionPopup (this,
                                             Environment.getUIString (LanguageStrings.closeproject,
                                                                      LanguageStrings.confirmpopup,
                                                                      LanguageStrings.title),
                                             //"Save changes before exiting?",
                                             Constants.SAVE_ICON_NAME,
                                             String.format (Environment.getUIString (LanguageStrings.closeproject,
                                                                                     LanguageStrings.confirmpopup,
                                                                                     LanguageStrings.text),
                                                            //"The following items have unsaved changes (click on an item to go to the relevant tab):<ul>%s</ul>Do you wish to save the changes before exiting?",
                                                            b.toString ()),
                                             buttons,
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

    public boolean isCurrentSideBarTextProperties ()
    {

        if (this.currentOtherSideBar != null)
        {

            return this.currentOtherSideBar.getId ().equals (TextPropertiesSideBar.ID);

        }

        return false;

    }

    public void showTextProperties ()
                             throws GeneralException
    {

        if (!this.sideBars.containsKey (TextPropertiesSideBar.ID))
        {

            this.addSideBar (new TextPropertiesSideBar (this,
                                                        this,
                                                        Environment.getProjectTextProperties ()));

        }

        this.showSideBar (TextPropertiesSideBar.ID);

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

    /**
     * Return the name of the property that is used to save the open tabs, this is needed because is the project
     * has a project version object then the name of the property will change.
     *
     * @return The open tabs property name in the form {@link Constants.OPENS_TAB_PROPERTY_NAME} + [":" + project version key].
     */
    public String getOpenTabsPropertyName ()
    {

        // See if we have a project version.
        ProjectVersion pv = this.proj.getProjectVersion ();

        String suffix = "";

        if (pv != null)
        {

            suffix = ":" + pv.getKey ();

        }

        return Constants.OPEN_TABS_PROPERTY_NAME + suffix;

    }

    private void saveState ()
                     throws GeneralException
    {

        if (this.proj == null)
        {

            return;

        }

        // Get the state from the sidebars.
        for (AbstractSideBar sb : this.sideBars.values ())
        {

            String id = sb.getId ();

            if (id == null)
            {

                continue;

            }

            try
            {

                this.proj.setProperty ("sidebarState-" + id,
                                       sb.getSaveState ());

            } catch (Exception e) {

                Environment.logError ("Unable to save state for sidebar: " +
                                      id,
                                      e);

            }

        }

        // Set the open tab ids.

        QuollPanel vqp = this.getCurrentlyVisibleTab ();

        String panelId = null;

        if (vqp != null)
        {

            panelId = vqp.getPanelId ();

        }

        // See if we have a project version.
        ProjectVersion pv = this.proj.getProjectVersion ();

        String suffix = "";

        if (pv != null)
        {

            suffix = ":" + pv.getKey ();

        }

        // Save it.
        try
        {

            this.proj.setProperty (Constants.LAST_OPEN_TAB_PROPERTY_NAME + suffix,
                                   panelId);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to save open tab id for project: " +
                                        this.proj,
                                        e);

        }

        StringBuilder openTabs = new StringBuilder ();

        for (int i = 0; i < this.tabs.getTabCount (); i++)
        {

            Component p = this.tabs.getComponentAt (i);

            if (p instanceof QuollPanel)
            {

                // TODO: Change to add a flag in the QuollPanel of whether it should be persisted.
                if (p instanceof HelpTextPanel)
                {

                    continue;

                }

                QuollPanel qp = (QuollPanel) p;

                panelId = qp.getPanelId ();

                if (openTabs.length () > 0)
                {

                    openTabs.append (",");

                }

                openTabs.append (panelId);

            }

        }

        try
        {

            this.proj.setProperty (this.getOpenTabsPropertyName (),
                                   openTabs.toString ());

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to save open tab ids for project: " +
                                        this.proj,
                                        e);

        }

        // We've drastically changed the size of things due to closing tabs and potentially the
        // notifications so validate to ensure the sizes have changed.
        this.validate ();

        // Save the size of the window (more specifically the size of the split pane.
        try
        {

            this.proj.setProperty (Constants.WINDOW_HEIGHT_PROPERTY_NAME,
                                   this.splitPane.getSize ().height);

            this.proj.setProperty (Constants.WINDOW_WIDTH_PROPERTY_NAME,
                                   this.splitPane.getSize ().width);

            this.proj.setProperty (Constants.PROJECT_SIDE_BAR_WIDTH_PROPERTY_NAME,
                                   this.sideBar.getSize ().width);
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
                                      Environment.getUIString (LanguageStrings.fullscreen,
                                                               LanguageStrings.actionerror));
                                      //"Unable to enter full screen mode");

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

        this.setVisible (true);
        this.setUILayout (this.layout);
        this.validate ();
        this.repaint ();

        this.fireFullScreenExitedEvent ();

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

            qep = new BlankQuollPanel (this,
									   "fullscreen-blank");

            qep.init ();

        }

        if (this.currentOtherSideBar != null)
        {

            this.savedOtherSideBarWidth = this.currentOtherSideBar.getSize ().width;

        }

        if (this.mainSideBar != null)
        {

            this.savedSideBarWidth = this.mainSideBar.getSize ().width;

        }

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

            this.fsf = new FullScreenFrame (fs,
											this);

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

        this.fireFullScreenEnteredEvent ();

		final AbstractProjectViewer _this = this;

		UIUtils.doLater (new ActionListener ()
		{

			@Override
			public void actionPerformed (ActionEvent ev)
			{

				if (_this.fsf != null)
				{

					_this.fsf.toFront ();

				}

			}

		});

    }

    public void showInFullScreen (DataObject n)
                           throws GeneralException
    {

        // Are we already in fs mode?
        if (this.fsf != null)
        {

            if (this.fsf.getCurrentForObject () == n)
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

                this.fsf = new FullScreenFrame (fs,
												this);

                this.fsf.init ();

            }

            //this.fsf.toFront ();

            this.tabs.revalidate ();
            this.tabs.repaint ();
            this.validate ();
            this.repaint ();

        }

        this.fireFullScreenEnteredEvent ();

    }

    public ProjectObjectQuollPanel getQuollPanelForObject (DataObject n)
    {

        for (QuollPanel qp : this.panels.values ())
        {

			ProjectObjectQuollPanel pqp = null;

			if (qp instanceof FullScreenQuollPanel)
			{

				FullScreenQuollPanel fqp = (FullScreenQuollPanel) qp;

				if (fqp.getChild () instanceof ProjectObjectQuollPanel)
				{

					pqp = (ProjectObjectQuollPanel) fqp.getChild ();

				}

			}

			// This is getting silly...
			// TODO: Fix this up.
			if (qp instanceof ProjectObjectQuollPanel)
			{

				pqp = (ProjectObjectQuollPanel) qp;

			}

			if ((pqp != null)
				&&
				(pqp.getForObject () == n)
			   )
			{

				return pqp;

			}

        }

        return null;

    }

    public java.util.List<ProjectObjectQuollPanel> getAllQuollPanelsForObject (DataObject n)
    {

        java.util.List<ProjectObjectQuollPanel> ret = new ArrayList ();

        for (QuollPanel qp : this.panels.values ())
        {

			if (qp instanceof ProjectObjectQuollPanel)
			{

				ProjectObjectQuollPanel pqp = (ProjectObjectQuollPanel) qp;

				if (pqp.getForObject () == n)
				{

					ret.add (pqp);

				}

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

                Environment.logError ("Unable to show panel: " + qp +
                                      " in full screen",
                                      e);

                UIUtils.showErrorMessage (this,
                                          Environment.getUIString (LanguageStrings.fullscreen,
                                                                   LanguageStrings.showpanelactionerror));
                                          //"Unable to show in full screen");

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

        int ind = this.getTabIndexForPanelId (qp.getPanelId ());

        if (ind < 0)
        {

            // No current tab for this panel.
            return;

        }

        TabHeader th = (TabHeader) this.tabs.getTabComponentAt (ind);

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

        th.setIcon (qp.getIcon (Constants.ICON_TAB_HEADER));

        final String panelId = qp.getPanelId ();

        this.panels.put (panelId,
                         qp);

        th.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (ev.getID () == TabHeader.TAB_CLOSING)
                {

                    if (!_this.closePanel (qp))
                    {

                        VetoableActionEvent vev = (VetoableActionEvent) ev;

                        vev.cancel ();

                    }

                }

            }

        });

        if (qp instanceof ProjectObjectQuollPanel)
        {

            qp.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    if (ev.getID () == ProjectObjectQuollPanel.UNSAVED_CHANGES_ACTION_EVENT)
                    {

                        if (ev.getActionCommand ().equals (ProjectObjectQuollPanel.HAS_CHANGES_COMMAND))
                        {

                            th.setComponentChanged (true);

                        }

                        if (ev.getActionCommand ().equals (ProjectObjectQuollPanel.NO_CHANGES_COMMAND))
                        {

                            th.setComponentChanged (false);

                        }

                    }

                    if (ev.getID () == ProjectObjectQuollPanel.SAVED)
                    {

                        th.setComponentChanged (false);

                    }

                }

            });

        }

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

        final AbstractProjectViewer _this = this;

        if (qp instanceof ProjectObjectQuollPanel)
        {

            final ProjectObjectQuollPanel p = (ProjectObjectQuollPanel) qp;

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

                        _this.removePanel (p);

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
            if ((p.getForObject ().getKey () != null)
                &&
                (!this.proj.hasObject (p.getForObject ()))
               )
            {

                remove.actionPerformed (new ActionEvent (this,
                                                         0,
                                                         "deleted"));

                return true;

            }

            if (p.hasUnsavedChanges ())
            {

                UIUtils.createQuestionPopup (this,
                                             Environment.getUIString (LanguageStrings.closepanel,
                                                                      LanguageStrings.confirmpopup,
                                                                      LanguageStrings.title),
                                             //"Save before closing?",
                                             Constants.SAVE_ICON_NAME,
                                             String.format (Environment.getUIString (LanguageStrings.closepanel,
                                                                                     LanguageStrings.confirmpopup,
                                                                                     LanguageStrings.text),
                                                            p.getTitle ()),
                                                            //"The %s has unsaved changes.  Save before closing?",
                                                            //Environment.getObjectTypeName (p.getForObject ())),
                                             Environment.getUIString (LanguageStrings.closepanel,
                                                                      LanguageStrings.confirmpopup,
                                                                      LanguageStrings.buttons,
                                                                      LanguageStrings.save),
                                             //"Yes, save the changes",
                                             Environment.getUIString (LanguageStrings.closepanel,
                                                                      LanguageStrings.confirmpopup,
                                                                      LanguageStrings.buttons,
                                                                      LanguageStrings.discard),
                                             //"No, discard the changes",
                                             new ActionListener ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    try
                                                    {

                                                        if (!p.saveUnsavedChanges ())
                                                        {

                                                            return;

                                                        }

                                                    } catch (Exception e)
                                                    {

                                                        // What the hell to do here???
                                                        Environment.logError ("Unable to save: " +
                                                                              p.getForObject (),
                                                                              e);

                                                        UIUtils.showErrorMessage (_this,
                                                                                  Environment.getUIString (LanguageStrings.closepanel,
                                                                                                           LanguageStrings.actionerror));
                                                                                  //"Unable to save " +
                                                                                  //Environment.getObjectTypeName (p.getForObject ()));

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
                                             new ActionListener ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    if (!p.hasUnsavedChanges ())
                                                    {

                                                        remove.actionPerformed (ev);

                                                    }

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

        return this.removePanel (qp);

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

    @Override
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

        super.addPopup (c,
                        hideOnClick,
                        hideViaVisibility);

    }

    @Override
    public void removePopup (Component c)
    {

        if (this.fsf != null)
        {

            this.fsf.removePopup (c);

            return;

        }

        super.removePopup (c);

    }

    @Override
    public void showAchievement (AchievementRule ar)
    {

        if (this.fsf != null)
        {

            this.fsf.showAchievement (ar);

            return;

        }

        super.showAchievement (ar);

    }

    @Override
    public QPopup getPopupByName (String name)
    {

        if (this.fsf != null)
        {

            return this.fsf.getPopupByName (name);

        }

        return super.getPopupByName (name);

    }

    @Override
    public void showPopupAt (Component popup,
                             Component showAt,
                             boolean   hideOnParentClick)
    {

        if (this.fsf != null)
        {

            this.fsf.showPopupAt (popup,
                                  showAt,
                                  hideOnParentClick);

            return;

        }

        super.showPopupAt (popup,
                           showAt,
                           hideOnParentClick);

    }

    @Override
    public void showPopupAt (Component c,
                             Point     p,
                             boolean   hideOnParentClick)
    {

        if (this.fsf != null)
        {

            this.fsf.showPopupAt (c,
                                  p,
                                  hideOnParentClick);

            return;

        }

        super.showPopupAt (c,
                           p,
                           hideOnParentClick);

    }

    public boolean removePanel (NamedObject n)
    {

        return this.removePanel (n.getObjectReference ().asString ());

    }

    public void removeAllSideBarsForObject (NamedObject n)
    {

        for (AbstractSideBar s : this.sideBars.values ())
        {

            if (n == s.getForObject ())
            {

                this.removeSideBar (s);

            }

        }

    }

    public void removeAllPanelsForObject (NamedObject n)
    {

        for (ProjectObjectQuollPanel p : this.getAllQuollPanelsForObject (n))
        {

            this.removePanel (p);

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

    public boolean removePanel (final QuollPanel p)
    {

		p.close ();

		String panelId = p.getPanelId ();

		int tInd = this.getTabIndexForPanelId (panelId);

		if (tInd > -1)
		{

			this.tabs.removeTabAt (tInd);

		}

		this.panels.remove (panelId);

		return true;

	}

    public ObjectManager getObjectManager ()
    {

        return this.dBMan;

    }

    public DataHandler getDataHandler (Class clazz)
                                throws GeneralException
    {

        if (this.dBMan == null)
        {

            return null;

        }

        return this.dBMan.getHandler (clazz);

    }

    public void refreshViewPanel (NamedObject n)
    {

        ProjectObjectQuollPanel p = this.getQuollPanelForObject (n);

        if (p != null)
        {

            p.refresh ();

        }

    }

    public void refreshObjectPanels (final Set<NamedObject> objs)
    {

        final AbstractProjectViewer _this = this;

        if ((objs != null) &&
            (objs.size () > 0))
        {

			UIUtils.doLater (new ActionListener ()
			{

                @Override
				public void actionPerformed (ActionEvent ev)
				{

					// For each one determine if it is visible.
					for (NamedObject n : objs)
					{

						ProjectObjectQuollPanel qp = _this.getQuollPanelForObject (n);

						if (qp != null)
						{

							qp.refresh ();

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

		if (o == null)
		{

			return;

		}

        java.util.Set<NamedObject> otherObjects = o.getOtherObjectsInLinks ();

        if (this.dBMan == null)
        {

            throw new IllegalStateException ("No object manager available.");

        }

        this.dBMan.saveObject (o,
                               null);

        this.refreshObjectPanels (otherObjects);

    }

    public void saveObjects (java.util.List<? extends NamedObject> objs,
                             boolean        doInTransaction)
                      throws GeneralException
    {

        this.dBMan.saveObjects (objs,
                                null);

    }

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

            this.dBMan.getLinks (o);

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
            ri = this.getReadabilityIndices (c.getChapterText ());

            this.noEditorReadabilityIndices.put (c,
                                                 ri);

        }

        return ri;

    }

    public ChapterCounts getChapterCounts (Chapter c)
    {

		return this.chapterCounts.get (c);

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

	private void initChapterCounts ()
	{

		if (this.proj == null)
		{

			return;

		}

        if (this.proj.getBooks ().size () == 0)
        {

            return;

        }

        Book b = this.proj.getBooks ().get (0);

        java.util.List<Chapter> chapters = b.getChapters ();

		for (Chapter c : chapters)
		{

			this.updateChapterCounts (c);

		}

		this.startWordCounts = this.getAllChapterCounts ();

	}

	public String getCurrentChapterText (Chapter c)
	{

        AbstractEditorPanel qep = this.getEditorForChapter (c);

		String t = null;

        if (qep != null)
        {

			t = qep.getEditor ().getText ();

        } else {

			t = (c.getText () != null ? c.getText ().getText () : null);

        }

		return t;

	}

	public void updateChapterCounts (final Chapter c)
	{

        final AbstractProjectViewer _this = this;

		final String t = this.getCurrentChapterText (c);

		final ChapterCounts cc = new ChapterCounts (t);

		this.chapterCounts.put (c,
								cc);

        if (!Environment.isStartupComplete ())
        {

            return;

        }

        // Don't try and calculate the a4 page count before the window is ready otherwise
        // strange errors result.  The initChapterCounts and scheduleA4PageCountUpdate will handle the initial counts.
        this.unschedule (this.chapterCountsUpdater);

        Runnable r = new Runnable ()
        {

            @Override
            public void run ()
            {

                try
                {

                    cc.standardPageCount = UIUtils.getA4PageCountForChapter (c,
                                                                             t);

                } catch (Exception e) {

                    Environment.logError ("Unable to get a4 page count for chapter: " +
                                          c,
                                          e);

                }

            }

        };

        this.chapterCountsUpdater = _this.schedule (r,
                                                    // Start in 2 seconds
                                                    2 * Constants.SEC_IN_MILLIS,
                                                    // Do it once.
                                                    0);

	}

	public int getChapterA4PageCount (Chapter c)
	{

		return UIUtils.getA4PageCountForChapter (c,
												 this.getCurrentChapterText (c));

	}

    public ChapterCounts getAllChapterCounts ()
    {

		ChapterCounts all = new ChapterCounts ();

		for (ChapterCounts cc : this.chapterCounts.values ())
		{

			all.add (cc);

		}

        return all;

    }

	public Set<ChapterCounts> getAllChapterCountsAsSet ()
	{

		return new LinkedHashSet<ChapterCounts> (this.chapterCounts.values ());

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

        return this.showOptions (null);

    }

    public boolean showOptions (final String section)
    {

        OptionsPanel p = (OptionsPanel) this.getQuollPanel (OptionsPanel.PANEL_ID);

        if (p == null)
        {

            try
            {

                p = new OptionsPanel (this,
									  Options.Section.project,
									  Options.Section.look,
									  Options.Section.naming,
									  Options.Section.editing,
                                      Options.Section.assets,
									  Options.Section.start,
									  Options.Section.editors,
									  Options.Section.itemsAndRules,
									  Options.Section.warmups,
									  Options.Section.achievements,
									  Options.Section.problems,
									  Options.Section.betas);

                p.init ();

            } catch (Exception e)
            {

                Environment.logError ("Unable to view the options",
                                      e);

                UIUtils.showErrorMessage (this,
                                          Environment.getUIString (LanguageStrings.options,
                                                                   LanguageStrings.actionerror));
                                          //"Unable to view the options");

                return false;

            }

            this.addPanel (p);

        }

        this.setPanelVisible (p);

        final OptionsPanel pp = p;

        if (section != null)
        {

            UIUtils.doActionWhenPanelIsReady (p,
                                              new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pp.showSection (section);

                }

            },
            null,
            null);

        }

        return true;

    }

	@Override
	public boolean showChart (String chartType)
					   throws GeneralException
	{

		if (this.viewStatistics ())
		{

			StatisticsPanel sp = (StatisticsPanel) this.getQuollPanel (StatisticsPanel.PANEL_ID);

			sp.showChart (chartType);

			return true;

		}

		return false;

	}

    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the word count history is viewed.
     */
    public boolean viewStatistics ()
    {

        // Check our tabs to see if we are already viewing the word counts, if so then just switch to it instead.
        StatisticsPanel wcp = (StatisticsPanel) this.getQuollPanel (StatisticsPanel.PANEL_ID);

        if (wcp != null)
        {

            this.setPanelVisible (wcp);

            this.fireProjectEvent (ProjectEvent.STATISTICS,
                                   ProjectEvent.SHOW);

            return true;

        }

        try
        {

            wcp = new StatisticsPanel (this,
									   new PerChapterWordCountsChart (this),
									   new AllWordCountsChart (this),
									   new ReadabilityIndicesChart (this),
									   new SessionWordCountChart (this),
									   new SessionTimeChart (this));

            wcp.init ();

			wcp.showChart (PerChapterWordCountsChart.CHART_TYPE);

        } catch (Exception e)
        {

            Environment.logError ("Unable to view word counts: " +
                                  this.proj,
                                  e);

            UIUtils.showErrorMessage (this,
                                      Environment.getUIString (LanguageStrings.statistics,
                                                               LanguageStrings.actionerror));
                                      //"Unable to view statistics panel.");

            return false;

        }

        this.addPanel (wcp);

        // Open the tab :)
        return this.viewStatistics ();

    }

	/**
	 * Determine the number of words written for this project during this session.
	 *
	 * @return The current word count - the start word count.
	 */
	public int getSessionWordCount ()
	{

		return this.getAllChapterCounts ().wordCount - this.startWordCounts.wordCount;

	}

    public ChapterCounts getStartWordCounts ()
    {

        return this.startWordCounts;

    }

    public Set<Chapter> snapshotChapters (Set<Chapter>   chapters,
                                          ProjectVersion pv)
                                   throws Exception
    {

        ChapterDataHandler ch = (ChapterDataHandler) this.dBMan.getHandler (Chapter.class);

        return ch.snapshot (chapters,
                            pv);

    }

    @Override
    public void sendMessageToEditor (final EditorEditor ed)
                              throws GeneralException
    {

        this.viewEditors ();

        final AbstractProjectViewer _this = this;

        UIUtils.doLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                EditorsSideBar sb = (EditorsSideBar) _this.sideBars.get (EditorsSideBar.ID);

                if (sb == null)
                {

                    Environment.logError ("Cant get editors sidebar?");

                    return;

                }

                try
                {

                    sb.showChatBox (ed);

                } catch (Exception e) {

                    Environment.logError ("Unable to show editor: " +
                                          ed,
                                          e);

                    UIUtils.showErrorMessage (_this,
                                              Environment.getUIString (LanguageStrings.editors,
                                                                       LanguageStrings.vieweditorerror));
                                              //"Unable to show Editor");

                }

            }

        });

    }

    @Override
    public void viewEditor (final EditorEditor ed)
                     throws GeneralException
    {

        this.viewEditors ();

        final AbstractProjectViewer _this = this;

        UIUtils.doLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                EditorsSideBar sb = (EditorsSideBar) _this.sideBars.get (EditorsSideBar.ID);

                if (sb == null)
                {

                    Environment.logError ("Cant get editors sidebar?");

                    return;

                }

                try
                {

                    sb.showEditor (ed);

                } catch (Exception e) {

                    Environment.logError ("Unable to show editor: " +
                                          ed,
                                          e);

                    UIUtils.showErrorMessage (_this,
                                              Environment.getUIString (LanguageStrings.editors,
                                                                       LanguageStrings.vieweditorerror));
                                              //"Unable to show Editor");

                }

            }

        });

    }

    @Override
    public boolean viewEditors ()
                         throws GeneralException
    {

        // See if the user has an account or has already registered, if so show the sidebar
        // otherwise show the register.
        if (!EditorsEnvironment.hasRegistered ())
        {

            try
            {

                EditorsUIUtils.showRegister (this);

            } catch (Exception e) {

                Environment.logError ("Unable to show editor register",
                                      e);

                UIUtils.showErrorMessage (this,
                                          Environment.getUIString (LanguageStrings.editors,
                                                                   LanguageStrings.showregistererror));
                                          //"Unable to show the editors register panel, please contact Quoll Writer support for assistance.");

                return false;

            }

            return true;

        }

        AbstractSideBar sb = this.sideBars.get (EditorsSideBar.ID);

        if (sb == null)
        {

            sb = new EditorsSideBar (this);

            this.addSideBar (sb);

        }

        this.showSideBar (EditorsSideBar.ID);

        return true;

    }

    /**
     * Set a chapter as edit complete.
     *
     * Note: this is NOT the right place for this method however there isn't currently a better
     * place.  It is here because the editor panel may need to use the function and the chapter
     * tree might, however the editor panel may not actually exist and putting it in the chapter
     * tree means that the chapter tree needs knowledge of panels which isn't correct either (but is worse).
     *
     * @param chapter The chapter to mark as edit complete.
     * @param editComplete Whether the chapter is edit complete or not.
     * @throws Exception If something goes wrong like not being able to save the chapter.
     */
    public void setChapterEditComplete (Chapter chapter,
                                        boolean editComplete)
                                 throws Exception
    {

        chapter.setEditComplete (editComplete);

        AbstractEditorPanel p = (AbstractEditorPanel) this.getEditorForChapter (chapter);

        int pos = 0;

        if (p != null)
        {

            pos = Utils.stripEnd (p.getEditor ().getText ()).length ();

        } else {

            String t = (chapter.getText () != null ? chapter.getText ().getText () : "");

            pos = Utils.stripEnd (t).length ();

        }

        chapter.setEditPosition (pos);

        this.saveObject (chapter,
                         false);

        this.reloadTreeForObjectType (Chapter.OBJECT_TYPE);

    }

    public void setChapterEditPosition (Chapter chapter,
                                        int     textPos)
                                 throws Exception
    {

        AbstractEditorPanel p = (AbstractEditorPanel) this.getEditorForChapter (chapter);

        int l = 0;

        if (p != null)
        {

            l = Utils.stripEnd (p.getEditor ().getText ()).length ();

            textPos = Math.min (textPos, l);

            // See if we are on the last line (it may be that the user is in the icon
            // column).
            Rectangle pp = p.getEditor ().modelToView (textPos);

            if (UserProperties.getAsBoolean (Constants.SET_CHAPTER_AS_EDIT_COMPLETE_WHEN_EDIT_POSITION_IS_AT_END_OF_CHAPTER_PROPERTY_NAME))
            {

                if (textPos <= l)
                {

                    Rectangle ep = p.getEditor ().modelToView (l);

                    chapter.setEditComplete ((ep.y == pp.y));

                }

            }

        } else {

            String t = (chapter.getText () != null ? chapter.getText ().getText () : "");

            l = Utils.stripEnd (t).length ();

        }

        textPos = Math.min (textPos, l);

        chapter.setEditPosition (textPos);

        this.saveObject (chapter,
                         false);

        this.reloadTreeForObjectType (Chapter.OBJECT_TYPE);

    }

    public void removeChapterEditPosition (Chapter chapter)
                                    throws Exception
    {

        chapter.setEditComplete (false);
        chapter.setEditPosition (-1);

        this.saveObject (chapter,
                         false);

        this.reloadTreeForObjectType (Chapter.OBJECT_TYPE);

    }

    @Override
    public boolean isEditorsVisible ()
    {

        return this.isEditorsSideBarVisible ();

    }

    public boolean isEditorsSideBarVisible ()
    {

        EditorsSideBar sb = (EditorsSideBar) this.sideBars.get (EditorsSideBar.ID);

        if (sb != null)
        {

            return sb.isShowing ();

        }

        return false;

    }

    public ProjectVersion getProjectVersion (String name)
                                      throws GeneralException
    {

        java.util.List<ProjectVersion> pvs = (java.util.List<ProjectVersion>) this.dBMan.getObjects (ProjectVersion.class,
                                                                                 null,
                                                                                 null,
                                                                                 false);

        for (ProjectVersion pv : pvs)
        {

            if (pv.getName ().equalsIgnoreCase (name))
            {

                return pv;

            }

        }

        return null;

    }

    public Set<Note> getNotesForVersion (ProjectVersion pv)
                                  throws GeneralException
    {

        NoteDataHandler ndh = (NoteDataHandler) this.dBMan.getHandler (Note.class);

        return ndh.getNotesForVersion (pv,
                                       null);

    }

    public Set<Note> getDealtWithNotes (ProjectVersion pv,
                                        boolean        isDealtWith)
                                 throws GeneralException
    {

        NoteDataHandler ndh = (NoteDataHandler) this.dBMan.getHandler (Note.class);

        return ndh.getDealtWith (pv,
                                 isDealtWith,
                                 null);

    }

    @Override
    public void showHelpText (String title,
                              String text,
                              String iconType,
                              String helpTextId)
    {

        this.addHelpTextTab (title,
                             text,
                             iconType,
                             helpTextId);

    }

    public void addHelpTextTab (String title,
                                String text,
                                String iconType,
                                String panelId)
    {

        HelpTextPanel p = new HelpTextPanel (this,
                                             title,
                                             text,
                                             iconType,
                                             panelId);

        p.init ();

        this.addPanel (p);

        this.setPanelVisible (p);

    }

    @Override
    public JComponent getTitleHeaderControl (String id)
    {

        if (id == null)
        {

            return null;

        }

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.title);
        prefix.add (LanguageStrings.toolbar);
        prefix.add (LanguageStrings.buttons);

        final AbstractProjectViewer _this = this;

        JComponent c = null;

        if (id.equals (FIND_HEADER_CONTROL_ID))
        {

            return UIUtils.createButton (Constants.FIND_ICON_NAME,
                                         Constants.ICON_TITLE_ACTION,
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.find,
                                                                  LanguageStrings.tooltip),
                                              //"Click to open the find",
                                              new ActionAdapter ()
                                              {

                                                  public void actionPerformed (ActionEvent ev)
                                                  {

                                                      _this.showFind (null);

                                                  }

                                              });

        }

        if (id.equals (CLOSE_HEADER_CONTROL_ID))
        {

            return UIUtils.createButton (Constants.CLOSE_ICON_NAME,
                                         Constants.ICON_TITLE_ACTION,
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.close,
                                                                  LanguageStrings.tooltip),
                                          //"Click to close",
                                          new ActionAdapter ()
                                          {

                                              public void actionPerformed (ActionEvent ev)
                                              {

                                                  _this.close (false,
                                                               null);

                                              }

                                          });

        }

        if (id.equals (FULL_SCREEN_HEADER_CONTROL_ID))
        {

            return UIUtils.createButton (Constants.FULLSCREEN_ICON_NAME,
                                         Constants.ICON_TITLE_ACTION,
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.fullscreen,
                                                                  LanguageStrings.tooltip),
                                          //"Click to work in full screen mode",
                                          new ActionAdapter ()
                                          {

                                              public void actionPerformed (ActionEvent ev)
                                              {

                                                  _this.enterFullScreen ();

                                              }

                                          });

        }

        return super.getTitleHeaderControl (id);

    }

    @Override
    public Set<String> getTitleHeaderControlIds ()
	{

		Set<String> ids = new LinkedHashSet ();

		ids.add (CONTACTS_HEADER_CONTROL_ID);
		ids.add (FIND_HEADER_CONTROL_ID);
		ids.add (FULL_SCREEN_HEADER_CONTROL_ID);
        ids.add (SETTINGS_HEADER_CONTROL_ID);

		return ids;

	}

    public void scheduleAutoSaveForAllEditors ()
    {

		this.unschedule (this.autoSaveTask);

		this.autoSaveTask = null;

        if (this.proj.getPropertyAsBoolean (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME))
        {

            final long autoSaveInt = Utils.getTimeAsMillis (this.proj.getProperty (Constants.CHAPTER_AUTO_SAVE_INTERVAL_PROPERTY_NAME));

            if (autoSaveInt > 0)
            {

				final AbstractProjectViewer _this = this;

                // Create our auto save
                Runnable r = new Runnable ()
                {

					@Override
                    public void run ()
                    {

                        try
                        {

                            _this.doForPanels (AbstractEditableEditorPanel.class,
                                              new QuollPanelAction<AbstractEditableEditorPanel> ()
                                              {

                                                  @Override
                                                  public void doAction (AbstractEditableEditorPanel p)
                                                  {

                                                    if (!p.hasUnsavedChanges ())
                                                    {

                                                        return;

                                                    }

                                                    try
                                                    {

                                                        p.saveChapter ();

                                                    } catch (Exception e)
                                                    {

                                                        Environment.logError ("Unable to auto save chapter: " +
                                                                              p.getChapter (),
                                                                              e);

                                                    }

                                                  }

                                              });

                        } catch (Exception e) {

                            Environment.logError ("Unable update panels",
                                                  e);

                        }

                    }

                };

				this.autoSaveTask = this.schedule (r,
                                                   autoSaveInt,
                                                   autoSaveInt);

            }

        }

    }

	/**
	 * Display the targets for the project.
	 *
	 */
	public void viewTargets ()
                      throws GeneralException
	{

        TargetsSideBar t = new TargetsSideBar (this);

        this.addSideBar (t);

        this.showSideBar (TargetsSideBar.ID);

	}

	public boolean hasUnsavedChapters ()
	{

        for (QuollPanel qp : this.panels.values ())
        {

			if (qp instanceof AbstractEditorPanel)
			{

				AbstractEditorPanel ep = (AbstractEditorPanel) qp;

				if (ep.hasUnsavedChanges ())
				{

					return true;

				}

			}

		}

		return false;

	}

    public Set<Chapter> getChaptersOverReadabilityTarget ()
    {

        Set<Chapter> chaps = new LinkedHashSet ();

        if (this.getProject () == null)
        {

            // Closing down.
            return chaps;

        }

        TargetsData projTargets = this.getProjectTargets ();

        int tFK = projTargets.getReadabilityFK ();
        int tGF = projTargets.getReadabilityGF ();

        if ((tFK > 0)
            ||
            (tGF > 0)
           )
        {

            for (Book book : this.getProject ().getBooks ())
            {

                for (Chapter c : book.getChapters ())
                {

                    ReadabilityIndices ri = this.getReadabilityIndices (c);

                    ChapterCounts cc = this.getChapterCounts (c);

                    if (cc.wordCount < Constants.MIN_READABILITY_WORD_COUNT)
                    {

                        continue;

                    }

                    float fk = ri.getFleschKincaidGradeLevel ();
                    float gf = ri.getGunningFogIndex ();

                    if ((tFK > 0)
                        &&
                        (ri.getFleschKincaidGradeLevel () > tFK)
                       )
                    {

                        chaps.add (c);

                        continue;

                    }

                    if ((tGF > 0)
                        &&
                        (ri.getGunningFogIndex () > tGF)
                       )
                    {

                        chaps.add (c);

                        continue;

                    }

                }

            }

        }

        return chaps;

    }

    public Set<Chapter> getChaptersOverWordTarget ()
    {

        Set<Chapter> chaps = new LinkedHashSet ();

        if (this.getProject () == null)
        {

            // Closing down.
            return chaps;

        }

        TargetsData projTargets = this.getProjectTargets ();

        int tcc = projTargets.getMaxChapterCount ();

        if (tcc > 0)
        {

            for (Book book : this.getProject ().getBooks ())
            {

                for (Chapter c : book.getChapters ())
                {

                    ChapterCounts count = this.getChapterCounts (c);

                    if (count.wordCount > tcc)
                    {

                        chaps.add (c);

                    }

                }

            }

        }

        return chaps;

    }

    public Map<Chapter, Set<Issue>> getProblemsForAllChapters ()
    {

        return this.getProblemsForAllChapters (null);

    }

    public Map<Chapter, Set<Issue>> getProblemsForAllChapters (Rule limitToRule)
    {

        Map<Chapter, Set<Issue>> probs = new LinkedHashMap ();

        if (this.getProject () == null)
        {

            // Closing down.
            return probs;

        }

        for (Book book : this.getProject ().getBooks ())
        {

            for (Chapter c : book.getChapters ())
            {

                Set<Issue> issues = null;

                if (limitToRule != null)
                {

                    issues = this.getProblems (c,
                                               limitToRule);

                } else {

                    issues = this.getProblems (c);

                }

                if (issues.size () > 0)
                {

                    probs.put (c, issues);

                }

            }

        }

        return probs;

    }

    public Set<Issue> getProblems (Chapter c,
                                   Rule    r)
    {

        Set<Issue> ret = new LinkedHashSet ();

        String ct = this.getCurrentChapterText (c);

        if (ct != null)
        {

            TextBlockIterator ti = new TextBlockIterator (ct);

            TextBlock b = null;

            while ((b = ti.next ()) != null)
            {

                java.util.List<Issue> issues = RuleFactory.getIssues (b,
                                                                      r,
                                                                      this.proj.getProperties ());

                for (Issue i : issues)
                {

                    ret.add (i);

                    i.setChapter (c);

                }

            }

        }

        return ret;

    }

    public Set<Issue> getProblems (Chapter c)
    {

        Set<Issue> ret = new LinkedHashSet ();

        String ct = this.getCurrentChapterText (c);

        if (ct != null)
        {

            TextBlockIterator ti = new TextBlockIterator (ct);

            TextBlock b = null;

            while ((b = ti.next ()) != null)
            {

                if (b instanceof Paragraph)
                {

                    ret.addAll (RuleFactory.getParagraphIssues ((Paragraph) b,
                                                                this.proj.getProperties ()));

                }

                if (b instanceof Sentence)
                {

                    ret.addAll (RuleFactory.getSentenceIssues ((Sentence) b,
                                                                this.proj.getProperties ()));

                }

            }

        }

        return ret;

    }

    public Set<Word> getSpellingErrors (Chapter c)
    {

        Set<Word> ret = new LinkedHashSet ();

        String ct = this.getCurrentChapterText (c);

        if (ct != null)
        {

            DictionaryProvider dp = this.getDictionaryProvider ();

            if (dp != null)
            {

                SpellChecker sc = dp.getSpellChecker ();

                if (sc != null)
                {

                    TextIterator ti = new TextIterator (ct);

                    for (Word w : ti.getWords ())
                    {

                        if (!sc.isCorrect (w))
                        {

                            ret.add (w);

                        }

                    }

                }

            }

        }

        return ret;

    }

    public File getProjectFile (String fileName)
    {

        if (fileName == null)
        {

            return null;

        }

        return this.proj.getFile (fileName);

    }

    public File getProjectFilesDirectory ()
    {

        return this.proj.getFilesDirectory ();

    }

    public void saveToProjectFilesDirectory (File   file,
                                             String fileName)
                                      throws IOException
    {

        this.proj.saveToFilesDirectory (file,
                                        fileName);

    }

}
