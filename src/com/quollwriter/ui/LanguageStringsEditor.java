package com.quollwriter.ui;

import java.awt.*;
import java.io.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;
import java.net.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.LayerUI;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;
import com.gentlyweb.utils.*;

import org.josql.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.text.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.achievements.ui.*;
import com.quollwriter.ui.charts.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.components.ScrollableBox;
import com.quollwriter.ui.components.SpellChecker;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class LanguageStringsEditor extends AbstractViewer implements RefValueProvider
{

	public static final int DEFAULT_WINDOW_WIDTH = 800;
	public static final int DEFAULT_WINDOW_HEIGHT = 500;
	public static final int PROJECT_BOX_WIDTH = 250;

	public static final String MAIN_CARD = "main";
	public static final String OPTIONS_CARD = "options";

    public static final String SUBMIT_HEADER_CONTROL_ID = "submit";
    public static final String USE_HEADER_CONTROL_ID = "use";
    public static final String HELP_HEADER_CONTROL_ID = "help";
    public static final String FIND_HEADER_CONTROL_ID = "find";

    public static int INTERNAL_SPLIT_PANE_DIVIDER_WIDTH = 2;

    private JSplitPane            splitPane = null;
    private Header                title = null;
    private Box                   notifications = null;
    private Box                   sideBar = null;
    private Box                   sideBarWrapper = null;
    private Box                   toolbarPanel = null;
    private AbstractSideBar       currentSideBar = null;
    private AccordionItemsSideBar mainSideBar = null;
    private Map<String, AbstractSideBar> sideBars = new HashMap ();
    private Stack<AbstractSideBar>  activeSideBars = new Stack ();
    private java.util.List<SideBarListener> sideBarListeners = new ArrayList ();
    private java.util.List<MainPanelListener> mainPanelListeners = new ArrayList ();
	private JPanel                cards = null;
	private CardLayout            cardsLayout = null;
	private JLabel                importError = null;
    private JLabel                forwardLabel = null;
    private ImportTransferHandlerOverlay   importOverlay = null;
    private Map<String, IdsPanel> panels = new HashMap ();
	private String currentCard = null;
    ///private Finder                finder = null;
    private LanguageStrings userStrings = null;
    private String toolbarLocation = null;
    private Map<String, JTree> sectionTrees = new LinkedHashMap<> ();
    private LanguageStrings baseStrings = null;
    private LanguageStrings.Filter<LanguageStrings.Node> nodeFilter = null;
    private Map<LanguageStrings.Node, Set<LanguageStrings.Value>> valuesCache = new HashMap<> ();

    public LanguageStringsEditor (LanguageStrings userStrings)
			               throws Exception
    {

        this (userStrings,
              Environment.getQuollWriterVersion ());

    }

    public LanguageStringsEditor (LanguageStrings userStrings,
                                  Version         baseQWVersion)
			               throws Exception
    {

        if (baseQWVersion == null)
        {

            baseQWVersion = Environment.getQuollWriterVersion ();

        }

		final LanguageStringsEditor _this = this;

        if (userStrings == null)
        {

            throw new IllegalArgumentException ("No strings provided.");

        }

        this.baseStrings = Environment.getUserUIEnglishLanguageStrings (baseQWVersion);

        if (this.baseStrings == null)
        {

            throw new IllegalArgumentException ("Unable to find English strings for version: " + baseQWVersion);

        }

        // Create a split pane.
        this.splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
                                         false);
        this.splitPane.setDividerSize (UIUtils.getSplitPaneDividerSize ());
        this.splitPane.setBorder (null);

        javax.swing.plaf.basic.BasicSplitPaneDivider div = ((javax.swing.plaf.basic.BasicSplitPaneUI) this.splitPane.getUI ()).getDivider ();
        div.setBorder (new MatteBorder (0, 0, 0, 1, Environment.getBorderColor ()));
        this.splitPane.setOpaque (false);
        this.splitPane.setBackground (UIUtils.getComponentColor ());

		this.cardsLayout = new CardLayout (0, 0);

		this.cards = new JPanel (this.cardsLayout);
        this.cards.setBackground (UIUtils.getComponentColor ());

		this.splitPane.setRightComponent (this.cards);

        this.userStrings = userStrings;

        this.updateTitle ();

        this.forwardLabel = UIUtils.createClickableLabel ("",
                                                          Environment.getIcon (Constants.INFO_ICON_NAME,
                                                                               Constants.ICON_CLICKABLE_LABEL));
        this.forwardLabel.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));

        UIUtils.makeClickable (this.forwardLabel,
                               new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                try
                {

                    if (_this.nodeFilter != null)
                    {

                        _this.showAllStrings ();

                    } else {

                        _this.limitViewToPreviousVersionDiff ();

                    }

                } catch (Exception e) {

                    Environment.logError ("Unable to update view",
                                          e);

                    UIUtils.showErrorMessage (_this,
                                              "Unable to update view.");

                }

            }

        });

        // Create the sidebar.
        this.mainSideBar = new AccordionItemsSideBar (this,
                                                      null)//items)
        {

            @Override
            public JComponent getContent ()
            {

                Box b = new Box (BoxLayout.Y_AXIS);

                b.add (_this.forwardLabel);

                _this.forwardLabel.setVisible (Environment.getQuollWriterVersion ().isNewer (_this.userStrings.getQuollWriterVersion ()));

                b.add (super.getContent ());

                b.setBorder (UIUtils.createPadding (0, 0, 0, 0));

                b.setPreferredSize (new Dimension (200, Short.MAX_VALUE));

                return b;

            }

            @Override
            public Dimension getMinimumSize ()
            {

                return new Dimension (200,
                                      250);
            }

            @Override
            public void panelShown (MainPanelEvent ev)
            {

                _this.setIdSelectedInSidebar (ev.getPanel ().getPanelId ());

            }

        };

        this.initSideBar ();
/*
        this.finder = new Finder (this);

        this.addSideBar (this.finder);
*/
        this.currentSideBar = this.mainSideBar;

        this.addMainPanelListener (this.currentSideBar);

        this.currentSideBar.init (null);

        this.sideBarWrapper = new Box (BoxLayout.Y_AXIS);
        this.toolbarPanel = new Box (BoxLayout.Y_AXIS);
        this.sideBarWrapper.setAlignmentX (LEFT_ALIGNMENT);
        this.toolbarPanel.setAlignmentX (LEFT_ALIGNMENT);

        this.sideBar = new Box (BoxLayout.Y_AXIS);
        this.sideBar.add (this.sideBarWrapper);
        //this.sideBar.add (this.toolbarPanel);

        this.setToolbarLocation (null);

        JButton saveB = UIUtils.createToolBarButton (Constants.SAVE_ICON_NAME,
                                                    "Save all the strings",
                                                    "save",
                                                    new ActionListener ()
        {

           @Override
           public void actionPerformed (ActionEvent ev)
           {

               //_this.userValue.setText (defValue);

           }

        });

        JButton nextB = UIUtils.createToolBarButton (Constants.NEXT_ICON_NAME,
                                                     "Go to the next section",
                                                     "next",
                                                     new ActionListener ()
        {

           @Override
           public void actionPerformed (ActionEvent ev)
           {

               //_this.userValue.setText (defValue);

           }

        });

        this.toolbarPanel.add (UIUtils.createButtonBar (Arrays.asList (saveB, nextB)));

        this.toolbarPanel.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                         this.toolbarPanel.getPreferredSize ().height));

        this.splitPane.setLeftComponent (this.sideBar);

        this.sideBarWrapper.add (this.currentSideBar);

		this.setContent (this.splitPane);

		this.importOverlay = new ImportTransferHandlerOverlay ();

        this.importOverlay.addMouseListener (new MouseEventHandler ()
        {

            public void handlePress (MouseEvent ev)
            {

                _this.importOverlay.setVisible (false);

                _this.validate ();
                _this.repaint ();

            }

        });

		this.setTransferHandler (new ImportTransferHandler (new ActionListener ()
		{

			public void actionPerformed (ActionEvent ev)
			{

				File f = (File) ev.getSource ();

                _this.importOverlay.setFile (f);

                _this.setGlassPane (_this.importOverlay);

                _this.importOverlay.setVisible (true);
                _this.validate ();
                _this.repaint ();

			}

		},
		new ActionListener ()
		{

			public void actionPerformed (ActionEvent ev)
			{

                _this.importOverlay.setVisible (false);
                _this.validate ();
                _this.repaint ();

				File f = (File) ev.getSource ();

			}

		},
		new ActionListener ()
		{

			public void actionPerformed (ActionEvent ev)
			{

                _this.importOverlay.setVisible (false);
                _this.validate ();
                _this.repaint ();

			}

		},
		new java.io.FileFilter ()
		{

			@Override
			public boolean accept (File f)
			{

                return f.getName ().endsWith (".json");

			}

		}));

        this.setMinimumSize (new Dimension (500, 500));

        // Check to see if a new version of the default strings is available.
        Environment.schedule (new Runnable ()
        {

            @Override
            public void run ()
            {

                String url = Environment.getProperty (Constants.QUOLL_WRITER_GET_UI_LANGUAGE_STRINGS_URL_PROPERTY_NAME);

                url = StringUtils.replaceString (url,
                                                 Constants.VERSION_TAG,
                                                 _this.baseStrings.getQuollWriterVersion ().toString ());
                url = StringUtils.replaceString (url,
                                                 Constants.ID_TAG,
                                                 _this.baseStrings.getId ());

                url = StringUtils.replaceString (url,
                                                 Constants.LAST_MOD_TAG,
                                                 "0");

                url += "&newer=true";

                try
                {

                     String data = Environment.getUrlFileAsString (new URL (Environment.getQuollWriterWebsite () + "/" + url));

                     if (data.startsWith (Constants.JSON_RETURN_PREFIX))
                     {

                         data = data.substring (Constants.JSON_RETURN_PREFIX.length ());

                     }

                     Object obj = JSONDecoder.decode (data);

                     if (obj == null)
                     {

                         return;

                     }

                     final LanguageStrings newls = new LanguageStrings (data);

                     Box content = new Box (BoxLayout.Y_AXIS);

                     content.add (UIUtils.createHelpTextPane (String.format ("A new version of the <b>%s</b> language strings is available.  This is for version <b>%s</b>, of {QW}.<br />You can view the changes and submit an update to your strings.",
                                                                             newls.getNativeName (),
                                                                             newls.getQuollWriterVersion ().toString ()),
                                                              _this));

                     content.add (Box.createVerticalStrut (5));

                     JButton b = UIUtils.createButton ("View the changes");

                     content.add (b);

                     // Add a notification.
                     final Notification n = _this.addNotification (content,
                                                                   Constants.INFO_ICON_NAME,
                                                                   -1);

                      b.addActionListener (new ActionListener ()
                      {

                           @Override
                           public void actionPerformed (ActionEvent ev)
                           {

                               LanguageStrings uls = null;

                               try
                               {

                                   uls = Environment.getUserUILanguageStrings (newls.getQuollWriterVersion (),
                                                                               _this.userStrings.getId ());

                               } catch (Exception e) {

                                   Environment.logError ("Unable to get user strings for version: " + newls.getQuollWriterVersion () + ", " + _this.userStrings.getId (),
                                                         e);

                                   UIUtils.showErrorMessage (null,
                                                             getUIString (uilanguage,edit,actionerror));

                                   return;

                               }

                               if (uls != null)
                               {

                                   // Open these instead.
                                   LanguageStringsEditor lse = Environment.editUILanguageStrings (uls,
                                                                                                  uls.getQuollWriterVersion ());

                                   try
                                   {

                                       lse.limitViewToPreviousVersionDiff ();

                                   } catch (Exception e) {

                                       Environment.logError ("Unable to update view",
                                                             e);

                                       UIUtils.showErrorMessage (_this,
                                                                 "Unable to update view");

                                       return;

                                   }

                                   _this.removeNotification (n);

                                   return;

                               }

                               _this.showChanges (newls);

                               _this.removeNotification (n);

                           }

                      });

                 } catch (Exception e) {

                     Environment.logError ("Unable to get new user interface strings",
                                           e);

                }

            }

        },
        5,
        -1);

    }

    private int getErrorCount (LanguageStrings.Node n)
    {

        int c = 0;

        // Get the card.
        IdsPanel p = this.panels.get (n.getNodeId ());

        if (p != null)
        {

            return p.getErrorCount ();

        }

        for (LanguageStrings.Value nv : this.valuesCache.get (n))
        {

            LanguageStrings.Value uv = this.userStrings.getValue (nv.getId (),
                                                                  true);

            if (uv != null)
            {

                if (LanguageStrings.getErrors (uv.getRawText (),
                                                  LanguageStrings.toId (nv.getId ()),
                                                  nv.getSCount (),
                                                  this).size () > 0)
                {

                    c++;

                }

            }

        }

        return c;

    }

    private int getUserValueCount (LanguageStrings.Node n)
    {

        int c = 0;

        // Get the card.
        IdsPanel p = this.panels.get (n.getNodeId ());

        if (p != null)
        {

            return p.getUserValueCount ();

        }

        for (LanguageStrings.Value nv : this.valuesCache.get (n))
        {

            LanguageStrings.Value uv = this.userStrings.getValue (nv.getId (),
                                                                  true);

            if (uv != null)
            {

                c++;

            }

        }

        return c;

    }

    private void initSideBar ()
    {

        String defSection = "General";

        Map<String, Set<LanguageStrings.Node>> sections = this.baseStrings.getNodesInSections (defSection);

        java.util.List<AccordionItem> items = new ArrayList<> ();

        this.valuesCache = new HashMap<> ();

        for (LanguageStrings.Section sect : this.baseStrings.getSections ())
        {

            AccordionItem it = this.createSectionTree (sect.name,
                                                       sect.icon,
                                                       sections.get (sect.id));

            if (it == null)
            {

                continue;

            }

            items.add (it);

        }

        this.mainSideBar.setItems (items);

        if (this.nodeFilter == null)
        {

            this.forwardLabel.setText (String.format ("Click to show what's changed/new between version <b>%s</b> and <b>%s</b>.",
                                                      Environment.getQuollWriterVersion ().toString (),
                                                      this.userStrings.getQuollWriterVersion ().toString ()));

        } else {

            this.forwardLabel.setText (String.format ("Click to show all the strings for version <b>%s</b>.",
                                                      this.userStrings.getQuollWriterVersion ().toString ()));

        }

    }

    private void showAllStrings ()
    {

        try
        {

            this.save ();

        } catch (Exception e) {

            Environment.logError ("Unable to save",
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to update view.");

            return;

        }

        // Clear out the panel cache.
        this.panels = new HashMap<> ();

        this.currentCard = null;
        this.cards.removeAll ();

        this.nodeFilter = null;
        this.initSideBar ();

        this.validate ();
        this.repaint ();

    }

    private void showChanges (LanguageStrings newls)
    {

        Version v = this.userStrings.getQuollWriterVersion ();

        try
        {

            Environment.saveUserUILanguageStrings (newls);

            this.userStrings.setQuollWriterVersion (newls.getQuollWriterVersion ());

            Environment.saveUserUILanguageStrings (this.userStrings);

            this.userStrings.setQuollWriterVersion (v);

            LanguageStrings uls = Environment.getUserUILanguageStrings (newls.getQuollWriterVersion (),
                                                                        this.userStrings.getId ());

            // Get a diff of the default to this new.
            LanguageStringsEditor lse = Environment.editUILanguageStrings (uls,
                                                                           newls.getQuollWriterVersion ());
            lse.limitViewToPreviousVersionDiff ();

        } catch (Exception e) {

            Environment.logError ("Unable to show strings editor for: " +
                                  newls,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to show strings.");

        }

    }

    private void limitViewToPreviousVersionDiff ()
                                          throws Exception
    {

        try
        {

            this.save ();

        } catch (Exception e) {

            Environment.logError ("Unable to save",
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to update view.");

            return;

        }

        final LanguageStrings basels = this.baseStrings;

        // Get the previous version (which will be the current QW version).
        final LanguageStrings prevbasels = Environment.getUserUIEnglishLanguageStrings (Environment.getQuollWriterVersion ());

        if (prevbasels == null)
        {

            // No strings.
            return;

        }

        this.nodeFilter = new LanguageStrings.Filter<LanguageStrings.Node> ()
        {

            @Override
            public boolean accept (LanguageStrings.Node n)
            {

                LanguageStrings.Node pn = prevbasels.getNode (n.getId ());

                // Does the node exist in the current base strings but not the previous?
                if (pn == null)
                {

                    // This is a new node.
                    return true;

                }

                // It exists, but has it changed?
                if ((n instanceof LanguageStrings.Value)
                    &&
                    (!(pn instanceof LanguageStrings.Value))
                   )
                {

                    // Node type changed.
                    return true;

                }

                if ((pn instanceof LanguageStrings.Value)
                    &&
                    (!(n instanceof LanguageStrings.Value))
                   )
                {

                    // Node type changed.
                    return true;

                }

                if ((pn instanceof LanguageStrings.Value)
                    &&
                    (n instanceof LanguageStrings.Value)
                   )
                {

                    LanguageStrings.Value pnv = (LanguageStrings.Value) pn;
                    LanguageStrings.Value nv = (LanguageStrings.Value) n;

                    // Value changed?
                    if (pnv.getRawText ().equals (nv.getRawText ()))
                    {

                        return false;

                    }

                }

                return true;

            }

        };

        this.initSideBar ();

        // Clear out the panel cache.
        this.panels = new HashMap<> ();

        this.currentCard = null;
        this.cards.removeAll ();

        this.validate ();
        this.repaint ();

    }

    private void updateSideBar (LanguageStrings.Node n)
    {

        for (JTree t : this.sectionTrees.values ())
        {

            // See how many children there are.
            TreePath tp = UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) t.getModel ().getRoot (),
                                                            n.getRoot ());

            if (tp != null)
            {

                ((DefaultTreeModel) t.getModel ()).nodeChanged ((DefaultMutableTreeNode) tp.getLastPathComponent ());

                t.validate ();
                t.repaint ();

                break;

            }

        }

        this.sideBar.validate ();
        this.sideBar.repaint ();

    }

    private void updateSideBar ()
    {

        this.sideBar.validate ();
        this.sideBar.repaint ();

    }

    private void saveToFile ()
                      throws Exception
    {

        Environment.saveUserUILanguageStrings (this.userStrings);

    }

    private void save ()
                throws Exception
    {

        // Cycle over all the id boxes and set the values if present.
        for (IdsPanel p : this.panels.values ())
        {

            p.saveValues ();

        }

        this.userStrings.setQuollWriterVersion (this.baseStrings.getQuollWriterVersion ());

        this.saveToFile ();

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

    public String getPreviewText (String t)
    {

        return LanguageStrings.buildText (t,
                                          this);

    }

    @Override
    public String getString (String id)
    {

        java.util.List<String> idparts = LanguageStrings.getIdParts (id);

        // See if we have a panel.
        if (idparts.size () > 0)
        {

            IdsPanel p = this.panels.get (idparts.get (0));

            if (p != null)
            {

                String t = p.getIdValue (id);

                if (t != null)
                {

                    return this.getPreviewText (t);

                }

            }

        }

        return this.userStrings.getString (id);

    }

    @Override
    public int getSCount (String id)
    {

        return this.baseStrings.getSCount (id);

    }

    @Override
    public String getRawText (String id)
    {

        java.util.List<String> idparts = LanguageStrings.getIdParts (id);

        // See if we have a panel.
        if (idparts.size () > 0)
        {

            IdsPanel p = this.panels.get (idparts.get (0));

            if (p != null)
            {

                String t = p.getIdValue (id);

                if (t != null)
                {

                    return t;

                }

            }

        }

        return this.userStrings.getRawText (id);

    }

    private void updateTitle ()
    {

        this.setViewerTitle (String.format ("Edit Language Strings for: %s (%s)",
                                            this.userStrings.getNativeName (),
                                            this.baseStrings.getQuollWriterVersion ().toString ()));

    }

    public void setToolbarLocation (String loc)
    {

        if (loc == null)
        {

            loc = Constants.BOTTOM;

        }
/*
        if (loc.equals (Constants.TOP))
        {

            this.toolbarPanel.setBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getBorderColor ()),
                                                             new EmptyBorder (0,
                                                                              0,
                                                                              0,
                                                                              0)));

            this.sideBar.add (this.toolbarPanel,
                              0);

        } else
        {

            this.toolbarPanel.setBorder (new CompoundBorder (new MatteBorder (1, 0, 0, 0, UIUtils.getBorderColor ()),
                                                             new EmptyBorder (0,
                                                                              0,
                                                                              0,
                                                                              0)));

            this.sideBar.add (this.toolbarPanel);

        }
*/
        this.toolbarLocation = loc;

    }

    private AccordionItem createSectionTree (String                    title,
                                             String                    iconType,
                                             Set<LanguageStrings.Node> sections)
    {

        final LanguageStringsEditor _this = this;

        final JTree tree = this.createStringsTree (sections);

        if (tree == null)
        {

            return null;

        }

        tree.setCellRenderer (new DefaultTreeCellRenderer ()
        {

            public Component getTreeCellRendererComponent (JTree   tree,
                                                           Object  value,
                                                           boolean sel,
                                                           boolean expanded,
                                                           boolean leaf,
                                                           int     row,
                                                           boolean hasFocus)
            {

                super.getTreeCellRendererComponent (tree,
                                                    value,
                                                    sel,
                                                    expanded,
                                                    leaf,
                                                    row,
                                                    hasFocus);

                DefaultMutableTreeNode tn = (DefaultMutableTreeNode) value;

                if (tn.getUserObject () instanceof String)
                {

                    return this;

                }

                this.setIcon (null);

                LanguageStrings.Node n = (LanguageStrings.Node) tn.getUserObject ();

                final java.util.List<String> id = n.getId ();

                int c = _this.getUserValueCount (n);
                int alls = 0;
                int errCount = _this.getErrorCount (n);

                Set<LanguageStrings.Value> vals = _this.valuesCache.get (n);

                if (vals != null)
                {

                    alls = vals.size ();

                }

                String title = (n.getTitle () != null ? n.getTitle () : n.getNodeId ());

                String name = null;

                if (errCount > 0)
                {

                    name = String.format ("%s (%s/%s) [%s errors]",
                                          title,
                                          Environment.formatNumber (c),
                                          Environment.formatNumber (alls),
                                          Environment.formatNumber (errCount));

                    this.setIcon (Environment.getIcon (Constants.ERROR_ICON_NAME,
                                                       Constants.ICON_SIDEBAR));

                } else {

                    name = String.format ("%s (%s/%s)",
                                          title,
                                          Environment.formatNumber (c),
                                          Environment.formatNumber (alls));

                    if (alls == c)
                    {

                        this.setIcon (Environment.getIcon (Constants.SAVE_ICON_NAME,
                                                           Constants.ICON_SIDEBAR));

                    } else {

                        if (c > 0)
                        {

                            this.setIcon (Environment.getIcon (Constants.NEXT_ICON_NAME,
                                                               Constants.ICON_SIDEBAR));

                        }

                    }

                }

                this.setText (name);

                this.setBorder (new EmptyBorder (2, 2, 2, 2));

                return this;

            }

        });

        tree.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handlePress (MouseEvent ev)
            {

                TreePath tp = tree.getPathForLocation (ev.getX (),
                                                       ev.getY ());

                if (tp != null)
                {

                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                    LanguageStrings.Node node  = (LanguageStrings.Node) n.getUserObject ();

                    String id = node.getNodeId ();

                    _this.showIds (id);
                    /*

                    IdsPanel p = _this.panels.get (id);

                    if (p == null)
                    {

                        p = _this.createIdsPanel (id);

                        try
                        {

                            p.init ();

                        } catch (Exception e) {

                            Environment.logError ("Unable to show ids for id: " +
                                                  id,
                                                  e);

                            UIUtils.showErrorMessage (_this,
                                                      "Unable to show panel.");

                            return;

                        }

                        _this.panels.put (id,
                                          p);

                        _this.cards.add (p, id);

                    }

                    _this.showCard (id);
*/
                }

            }

        });

        AccordionItem acc = new AccordionItem (title,
                                               iconType)
        {

            @Override
            public JComponent getContent ()
            {

                return tree;

            }

        };

        this.sectionTrees.put (title,
                               tree);

        return acc;

    }

    public void showIds (String idPrefix)
    {

        if (!this.valuesCache.containsKey (this.baseStrings.getNode (idPrefix)))
        {

            return;

        }

        //String id = node.getNodeId ();
        IdsPanel p = this.panels.get (idPrefix);

        if (p == null)
        {

            p = this.createIdsPanel (idPrefix);

            try
            {

                p.init ();

            } catch (Exception e) {

                Environment.logError ("Unable to show ids for id: " +
                                      idPrefix,
                                      e);

                UIUtils.showErrorMessage (this,
                                          "Unable to show panel.");

                return;

            }

            this.panels.put (idPrefix,
                             p);

            this.cards.add (p, idPrefix);

        }

        this.showCard (idPrefix);

        this.fireMainPanelShownEvent (p);

    }

    public void setIdSelectedInSidebar (String id)
    {

        for (JTree t : this.sectionTrees.values ())
        {

            t.clearSelection ();

            // See how many children there are.
            TreePath tp = UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) t.getModel ().getRoot (),
                                                            this.baseStrings.createNode (id));

            if (tp != null)
            {

                t.setSelectionPath (tp);

            }

        }

    }

    private JTree createStringsTree (Set<LanguageStrings.Node> sections)
    {

        final LanguageStringsEditor _this = this;

        DefaultMutableTreeNode root = new DefaultMutableTreeNode ("_strings");

        for (LanguageStrings.Node k : sections)
        {

            Set<LanguageStrings.Value> vals = k.getValues (this.nodeFilter);

            if (vals.size () == 0)
            {

                continue;

            }

            this.valuesCache.put (k, vals);

            root.add (new DefaultMutableTreeNode (k));

        }

        if (root.getChildCount () == 0)
        {

            // Filtered out everything.
            return null;

        }

        final JTree tree = UIUtils.createTree ();

        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel ();

        dtm.setRoot (root);

        return tree;

    }

    public void showMainCard ()
    {

        this.showCard (MAIN_CARD);

    }

    public void showCard (String name)
    {

        this.cardsLayout.show (this.cards,
                               name);

		this.currentCard = name;

        this.validate ();
        this.repaint ();

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

    public void addPopup (Component c)
    {

        this.addPopup (c,
                       false,
                       false);

    }

	@Override
    public void addPopup (Component c,
                          boolean   hideOnClick,
                          boolean   hideViaVisibility)
    {

        this.getLayeredPane ().add (c,
                                    JLayeredPane.POPUP_LAYER);

        this.getLayeredPane ().moveToFront (c);

    }

	@Override
    public void removePopup (Component c)
    {

        this.getLayeredPane ().remove (c);

        this.getLayeredPane ().validate ();

        this.getLayeredPane ().repaint ();

    }

	@Override
    public void init ()
			   throws Exception
    {

		super.init ();

        final LanguageStringsEditor _this = this;

		int width = 0;
		int height = 0;

		try
		{

			width = UserProperties.getAsInt ("languagestringseditor-window-width");

			height = UserProperties.getAsInt ("languagestringseditor-window-height");

		} catch (Exception e) {

			// Ignore.

		}

		if (width < 1)
		{

			width = DEFAULT_WINDOW_WIDTH;

		}

		if (height < 1)
		{

			height = DEFAULT_WINDOW_HEIGHT;

		}

        this.splitPane.setPreferredSize (new Dimension (width,
                                                        height));

        int scroll = 0;

        try
		{

			scroll = UserProperties.getAsInt ("languagestringseditor-sidebar-scroll");

		} catch (Exception e) {

			// Ignore.

		}

        if (scroll > 0)
        {

            this.mainSideBar.scrollVerticalTo (scroll);

        }

		this.setTitleHeaderControlsVisible (true);

		this.showViewer ();

        String lastId = UserProperties.get ("languagestringseditor-stringsid-lasteditingid-" + this.userStrings.getId ());

        if (lastId != null)
        {

            this.showIds (lastId);

        }

        scroll = 0;

        try
        {

            scroll = UserProperties.getAsInt ("languagestringseditor-stringsid-lasteditingscroll");

        } catch (Exception e) {

            // Ignore.

        }

        if (scroll > 0)
        {

            final int _scroll = scroll;

            UIUtils.doLater (new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    _this.panels.get (_this.currentCard).getScrollPane ().getVerticalScrollBar ().setValue (_scroll);

                }

            });

        }

        if (!UserProperties.getAsBoolean (Constants.LANGUAGE_STRINGS_FIRST_USE_SEEN_PROPERTY_NAME))
        {

            UIUtils.showMessage ((PopupsSupported) this,
                                 "Welcome to the (beta) editor!",
                                 "Welcome to the strings editor.  This window is where you'll create your own strings for a language for the {QW} User Interface.<br /><br />It is recommended that you read the <a href='help://uilanguages/overview'>strings editor guide</a> before starting out.  It describes how the editor works and the concepts used.<br /><br />Note: this is a beta release and I need your help to let me know what features you would like to see to help you create the strings.  So please <a href='action:contact'>all feedback and ideas are welcome</a>.");

            UserProperties.set (Constants.LANGUAGE_STRINGS_FIRST_USE_SEEN_PROPERTY_NAME,
                                true);

        }

    }

    public void showSideBar (String name)
    {

        if ((this.currentSideBar != null)
            &&
            (this.currentSideBar.getName ().equals (name))
           )
        {

            return;

        }

        AbstractSideBar b = this.sideBars.get (name);

        if (b == null)
        {

            throw new IllegalArgumentException ("Unable to show sidebar: " +
                                                name +
                                                ", no sidebar found with that name.");

        }

        if (this.currentSideBar != null)
        {

            // Will be hiding this one.
            this.currentSideBar.onHide ();

        }

		this.currentSideBar = b;

		this.activeSideBars.remove (b);

		this.activeSideBars.push (b);

		this.currentSideBar.setVisible (true);

        this.setUILayout ();

        try
        {

            b.onShow ();

        } catch (Exception e) {

            Environment.logError ("Unable to call onShow for sidebar: " +
                                  name +
                                  ", instance: " +
                                  b,
                                  e);

        }

    }

    public void setUILayout ()
    {

        final LanguageStringsEditor _this = this;

		this.splitPane.setResizeWeight (1f);

		if (this.currentSideBar != null)
		{

			this.splitPane.setDividerSize (INTERNAL_SPLIT_PANE_DIVIDER_WIDTH);
			this.splitPane.setRightComponent (this.currentSideBar);

			final Dimension min = this.currentSideBar.getMinimumSize ();

			int ww = this.splitPane.getSize ().width;

			int sbw = this.currentSideBar.getSize ().width;

			if (sbw == 0)
			{

				sbw = min.width;

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

			this.splitPane.setDividerLocation (w);

			final int fw2 = w;

			UIUtils.doLater (new ActionListener ()
			{

				public void actionPerformed (ActionEvent ev)
				{

					// For reasons best known to swing this has to be done separately otherwise when
					// you exit full screen it will have the other sidebar as taking up all the space.
					_this.splitPane.setDividerLocation (fw2);

				}

			});

		} else {

			this.splitPane.setDividerSize (0);
			this.splitPane.setRightComponent (this.createLayoutFiller ());

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

    public void addSideBar (AbstractSideBar sb)
                     throws GeneralException
    {

        if (sb == null)
        {

            throw new IllegalArgumentException ("No sidebar provided.");

        }

        String id = sb.getId ();

        String state = null;

        if (id != null)
        {

            state = UserProperties.get ("sidebarState-" + id);

        }

        sb.init (state);

        this.sideBars.put (id,
                           sb);

    }

	@Override
	public void doSaveState ()
	{

        // Get the state from the sidebars.
        for (AbstractSideBar sb : this.sideBars.values ())
        {

            String id = sb.getId ();

            if (id == null)
            {

                continue;

            }

            UserProperties.set ("sidebarState-" + id,
                                sb.getSaveState ());

        }

	}

    @Override
    public boolean viewEditors ()
						 throws GeneralException
    {

        throw new UnsupportedOperationException ("Not supported.");

    }

	@Override
    public void viewEditor (final EditorEditor ed)
                     throws GeneralException
    {

        throw new UnsupportedOperationException ("Not supported.");

    }

	@Override
    public boolean isEditorsVisible ()
    {

        return false;

    }

	@Override
    public Set<String> getTitleHeaderControlIds ()
	{

		Set<String> ids = new LinkedHashSet ();

        ids.add (SUBMIT_HEADER_CONTROL_ID);
        ids.add (FIND_HEADER_CONTROL_ID);
        ids.add (USE_HEADER_CONTROL_ID);
        ids.add (REPORT_BUG_HEADER_CONTROL_ID);
        ids.add (HELP_HEADER_CONTROL_ID);
        ids.add (SETTINGS_HEADER_CONTROL_ID);

		return ids;

	}

	@Override
    public JComponent getTitleHeaderControl (String id)
	{

		if (id == null)
		{

			return null;

		}

        java.util.List<String> prefix = Arrays.asList (allprojects,headercontrols,items);

		final LanguageStringsEditor _this = this;

		JComponent c = null;
/*
        if (id.equals (FIND_HEADER_CONTROL_ID))
        {

            return UIUtils.createButton (Constants.FIND_ICON_NAME,
                                         Constants.ICON_TITLE_ACTION,
                                         "Find",
                                          new ActionAdapter ()
                                          {

                                              public void actionPerformed (ActionEvent ev)
                                              {

                                                  _this.showFind ();

                                              }

                                          });

        }
*/
        if (id.equals (SUBMIT_HEADER_CONTROL_ID))
        {

            c = UIUtils.createButton (Constants.UP_ICON_NAME,
                                      Constants.ICON_TITLE_ACTION,
                                      "Click to submit the strings",
                                      new ActionListener ()
                                      {

                                        @Override
                                        public void actionPerformed (ActionEvent ev)
                                        {

                                            _this.submit ();

                                        }

                                      });

        }

        if (id.equals (USE_HEADER_CONTROL_ID))
        {

            c = UIUtils.createButton (Constants.PLAY_ICON_NAME,
                                      Constants.ICON_TITLE_ACTION,
                                      "Click to try out your strings",
                                      new ActionListener ()
                                      {

                                        @Override
                                        public void actionPerformed (ActionEvent ev)
                                        {

                                            if (!_this.userStrings.getQuollWriterVersion ().equals (Environment.getQuollWriterVersion ()))
                                            {

                                                UIUtils.showMessage ((PopupsSupported) _this,
                                                                     "Unable to try out",
                                                                     "Sorry, you can only try out a set of strings if the version matches the currently installed {QW} version.");

                                                return;

                                            }

                                            _this.tryOut ();

                                        }

                                      });

        }

        if (id.equals (HELP_HEADER_CONTROL_ID))
        {

            c = UIUtils.createButton (Constants.HELP_ICON_NAME,
                                      Constants.ICON_TITLE_ACTION,
                                      "Click to view the help about editing your strings",
                                      new ActionListener ()
                                      {

                                        @Override
                                        public void actionPerformed (ActionEvent ev)
                                        {

                                            UIUtils.openURL (_this,
                                                             "help://uilanguages/overview");

                                        }

                                      });

        }

		if (c != null)
		{

			return c;

		}

		return super.getTitleHeaderControl (id);

	}

    private void submit ()
    {

        final LanguageStringsEditor _this = this;

        try
        {

            this.save ();

        } catch (Exception e) {

            Environment.logError ("Unable to save: " + this.userStrings,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to save strings.");

            return;

        }

        Set<LanguageStrings.Value> vals = this.userStrings.getAllValues ();

        if (vals.size () == 0)
        {

            UIUtils.showMessage ((PopupsSupported) this,
                                 "No strings provided",
                                 "Sorry, you must provide at least 1 string.");

            return;

        }

        int c = 0;

        for (LanguageStrings.Value uv : vals)
        {

            LanguageStrings.Value nv = this.baseStrings.getValue (uv.getId ());

            c += LanguageStrings.getErrors (uv.getRawText (),
                                            LanguageStrings.toId (nv.getId ()),
                                            nv.getSCount (),
                                            this).size ();

        }

        if (c > 0)
        {

            UIUtils.showMessage ((PopupsSupported) this,
                                 "Errors found in strings",
                                 String.format ("Sorry, there are <b>%s</b> errors that must be corrected before you can submit the strings.",
                                                Environment.formatNumber (c)));

            return;

        }

        final String popupName = "submit";
        QPopup popup = this.getNamedPopup (popupName);

        if (popup == null)
        {

            popup = UIUtils.createClosablePopup ("Submit your strings",
                                                 Environment.getIcon (Constants.UP_ICON_NAME,
                                                                      Constants.ICON_POPUP),
                                                 null);

            final QPopup qp = popup;

            Box content = new Box (BoxLayout.Y_AXIS);

            JTextPane help = UIUtils.createHelpTextPane ("Complete the form below to submit your strings.  All the values are required.",
                                                         this);

            help.setBorder (null);

            content.add (help);
            content.add (Box.createVerticalStrut (10));

            final JLabel error = UIUtils.createErrorLabel ("");
            error.setVisible (false);
            error.setBorder (UIUtils.createPadding (0, 5, 5, 5));

            content.add (error);

            Set<FormItem> items = new LinkedHashSet ();

            final TextFormItem nativelang = new TextFormItem ("Native Name (i.e. Espa\u00F1ol, Fran\u00E7ais, Deutsch)",
                                                             this.userStrings.getNativeName ());

            items.add (nativelang);

            final TextFormItem lang = new TextFormItem ("English Name (i.e. Spanish, French, German)",
                                                         this.userStrings.getLanguageName ());

            items.add (lang);

            final TextFormItem email = new TextFormItem ("Contact Email",
                                                         this.userStrings.getEmail ());

            items.add (email);

            final JLabel tc = UIUtils.createClickableLabel ("View the Terms and Conditions for creating a translation",
                                                            Environment.getIcon (Constants.INFO_ICON_NAME,
                                                                                 Constants.ICON_CLICKABLE_LABEL),
                                                            Environment.getQuollWriterHelpLink ("uilanguages/terms-and-conditions",
                                                                                                null));

            final CheckboxFormItem tandc = new CheckboxFormItem (null, "I have read and agree to the Terms and Conditions");

            if (this.userStrings.getStringsVersion () == 0)
            {

                items.add (new AnyFormItem (null, tc));
                items.add (tandc);

            }

            ActionListener saveAction = new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    String em = email.getValue ();

                    if (em == null)
                    {

                        error.setText ("Please enter a valid email.");
                        error.setVisible (true);
                        qp.resize ();
                        qp.resize ();

                        return;

                    }

                    String l = lang.getValue ();

                    if (l == null)
                    {

                        error.setText ("Please enter the English Language name.");
                        error.setVisible (true);
                        qp.resize ();

                        return;

                    }

                    String nl = nativelang.getValue ();

                    if (nl == null)
                    {

                        error.setText ("Please enter the Name Language name.");
                        error.setVisible (true);
                        qp.resize ();

                        return;

                    }

                    if (_this.userStrings.getAllValues ().size () == 0)
                    {

                        error.setText ("No strings provided.  Please provide at least 1 string for your translation.");
                        error.setVisible (true);
                        qp.resize ();

                        return;

                    }

                    if ((_this.userStrings.getStringsVersion () == 0)
                        &&
                        (!tandc.isSelected ())
                       )
                    {

                        error.setText ("To submit your strings you must agree to the Terms & Conditions, and please give them a quick read ;)");
                        error.setVisible (true);
                        qp.resize ();

                        return;

                    }

                    _this.userStrings.setEmail (em);
                    _this.userStrings.setLanguageName (l);
                    _this.userStrings.setNativeName (nl);

                    _this.updateTitle ();

                    try
                    {

                        _this.save ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to save strings: " + _this.userStrings,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to save strings.");

                        return;

                    }

                    // Get the file, then send.
                    String t = null;

                    try
                    {

                        t = JSONEncoder.encode (_this.userStrings.getAsJSON ());

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to upload strings: " + _this.userStrings,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to upload strings.");

                        return;

                    }

                    Map<String, String> headers = new HashMap<> ();

                    String submitterid = UserProperties.get (Constants.UI_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME);

                    if (submitterid != null)
                    {

                        headers.put (Constants.UI_LANGUAGE_STRINGS_SUBMITTER_ID_HEADER_NAME,
                                     submitterid);

                    }

                    URL u = null;

                    try
                    {

                        u = new URL (Environment.getQuollWriterWebsite () + Environment.getProperty (Constants.SUBMIT_UI_LANGUAGE_STRINGS_URL_PROPERTY_NAME));

                    } catch (Exception e) {

                        Environment.logError ("Unable to construct the url for submitting the ui language strings.",
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to upload strings.");

                        return;

                    }

                    Utils.postToURL (u,
                                     headers,
                                     t,
                                     // On success
                                     new ActionListener ()
                                     {

                                         @Override
                                         public void actionPerformed (ActionEvent ev)
                                         {

                                             Map m = (Map) JSONDecoder.decode ((String) ev.getSource ());

                                             String res = (String) m.get ("result");

                                             String sid = (String) m.get ("submitterid");

                                             UserProperties.set (Constants.UI_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME,
                                                                 sid);

                                             //_this.userStrings.setSubmitterId (sid);
                                             _this.userStrings.setStringsVersion (((Number) m.get ("version")).intValue ());

                                             try
                                             {

                                                 _this.saveToFile ();

                                             } catch (Exception e) {

                                                 Environment.logError ("Unable to save strings file: " +
                                                                       _this.userStrings,
                                                                       e);

                                                 UIUtils.showErrorMessage (_this,
                                                                           "Your strings have been submitted to Quoll Writer support for review.  However the associated local file, where the strings are kept on your machine, could not be updated.");

                                                 return;

                                             }

                                             if (_this.userStrings.getStringsVersion () == 1)
                                             {

                                                 UIUtils.showMessage ((PopupsSupported) _this,
                                                                      "Strings submitted",
                                                                      String.format ("Your strings have been submitted to Quoll Writer support for review.<br /><br />A confirmation email has been sent to <b>%s</b>.  Please click on the link in that email to confirm your email address.<br /><br />Thank you for taking the time and the effort to create the strings, it is much appreciated!",
                                                                                     _this.userStrings.getEmail ()));

                                             } else {

                                                 UIUtils.showMessage ((PopupsSupported) _this,
                                                                      "Strings submitted",
                                                                      String.format ("Thank you!  Your strings have been updated to version <b>%s</b> and will be made available to Quoll Writer users.<br /><br />Thank you for taking the time and effort to update the strings, it is much appreciated!",
                                                                                     Environment.formatNumber (_this.userStrings.getStringsVersion ())));

                                             }

                                             qp.resize ();
                                             qp.removeFromParent ();

                                         }

                                     },
                                     // On error
                                     new ActionListener ()
                                     {

                                         @Override
                                         public void actionPerformed (ActionEvent ev)
                                         {

                                             Map m = (Map) JSONDecoder.decode ((String) ev.getSource ());

                                             String res = (String) m.get ("reason");

                                             // Get the errors.
                                             UIUtils.showErrorMessage (_this,
                                                                       "Unable to submit the strings, reason:<ul class='error'><li>" + res + "</li></ul>");

                                         }

                                     },
                                     new ActionListener ()
                                     {

                                         @Override
                                         public void actionPerformed (ActionEvent ev)
                                         {

                                             Map m = (Map) JSONDecoder.decode ((String) ev.getSource ());

                                             String res = (String) m.get ("reason");

                                             // Get the errors.
                                             UIUtils.showErrorMessage (_this,
                                                                       "Unable to submit the strings, reason:<ul class='error'><li>" + res + "</li></ul>");

                                         }

                                     });

                }

            };

            UIUtils.addDoActionOnReturnPressed (lang.getTextField (),
                                                saveAction);
            UIUtils.addDoActionOnReturnPressed (nativelang.getTextField (),
                                                saveAction);
            UIUtils.addDoActionOnReturnPressed (email.getTextField (),
                                                saveAction);

            JButton save = UIUtils.createButton ("Submit",
                                                 saveAction);
            JButton cancel = UIUtils.createButton ("Cancel",
                                                   new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    qp.removeFromParent ();
                    _this.removeNamedPopup (popupName);

                }

            });

            Set<JButton> buttons = new LinkedHashSet ();
            buttons.add (save);
            buttons.add (cancel);

            Form f = new Form (Form.Layout.stacked,
                               items,
                               buttons);

            content.add (f);

            content.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                             content.getPreferredSize ().height));
            content.setBorder (UIUtils.createPadding (10, 10, 10, 10));

            popup.setContent (content);

            popup.setDraggable (this);

            popup.setPopupName (popupName);

            this.addNamedPopup (popupName,
                                popup);

            //popup.resize ();
            this.showPopupAt (popup,
                              UIUtils.getCenterShowPosition (this,
                                                             popup),
                              false);

            UIUtils.doLater (new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    //desc.grabFocus ();
                    qp.resize ();

                }

            });

        } else {

            popup.setVisible (true);
            popup.resize ();

        }

    }

	@Override
    public String getViewerIcon ()
    {

        return Constants.EDIT_ICON_NAME;

    }

    private void tryOut ()
    {

        try
        {

            this.save ();

            Environment.setUILanguage ("user-" + this.userStrings.getId ());

        } catch (Exception e) {

            Environment.logError ("Unable to update ui language for: " + this.userStrings.getId (),
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to set strings.");

            return;

        }

        UIUtils.showMessage ((PopupsSupported) this,
                             "Restart recommended",
                             "{QW} has been updated to make use of your strings.  To make full use of them it is recommended that you restart {QW}.");

    }

	@Override
    public void fillSettingsPopup (JPopupMenu popup)
	{

        //java.util.List<String> prefix = Arrays.asList (allprojects,settingsmenu,items);

		final LanguageStringsEditor _this = this;

        popup.add (this.createMenuItem ("Submit your strings",
                                        Constants.UP_ICON_NAME,
                                        new ActionAdapter ()
                                        {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    _this.submit ();

                                                }

                                            }));

        popup.add (this.createMenuItem ("Create a new translation",
                                        Constants.NEW_ICON_NAME,
										new ActionListener ()
											 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

                                                    UIUtils.showAddNewLanguageStringsPopup (_this);

												}

											 }));

        popup.add (this.createMenuItem ("Edit a translation",
                                        Constants.EDIT_ICON_NAME,
 			    						new ActionListener ()
 										{

											@Override
											public void actionPerformed (ActionEvent ev)
											{

                                                UIUtils.showEditLanguageStringsSelectorPopup (_this);

											}

										}));

        if (this.baseStrings.getQuollWriterVersion ().equals (Environment.getQuollWriterVersion ()))
        {

            popup.add (this.createMenuItem ("Try out your strings",
                                            Constants.PLAY_ICON_NAME,
    										new ActionListener ()
    											 {

    												@Override
    												public void actionPerformed (ActionEvent ev)
    												{

                                                        _this.tryOut ();

    												}

    											 }));

        }

        popup.add (this.createMenuItem ("Delete",
                                     Constants.DELETE_ICON_NAME,
										new ActionListener ()
											 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

                                                 _this.showDelete ();

												}

											 }));

         popup.add (this.createMenuItem ("Close",
                                      Constants.CLOSE_ICON_NAME,
 										new ActionListener ()
 											 {

 												@Override
 												public void actionPerformed (ActionEvent ev)
 												{

                                                  _this.close (true,
                                                               null);

 												}

 											 }));

        popup.addSeparator ();

        popup.add (this.createMenuItem ("Open Project",
                                        Constants.UP_ICON_NAME,
                                        new ActionAdapter ()
                                        {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    Environment.showLanding ();

                                                }

                                            }));


	}

    public LanguageStrings getUserLanguageStrings ()
    {

        return this.userStrings;

    }

    public void showDelete ()
    {

        final LanguageStringsEditor _this = this;

        QPopup qp = UIUtils.createPopup ("Delete the strings",
                                         Constants.DELETE_ICON_NAME,
                                         null,
                                         true,
                                         null);

        final Box content = new Box (BoxLayout.Y_AXIS);

        JComponent mess = UIUtils.createHelpTextPane ("Please confirm you wish to delete your strings.  Enter <b>Yes</b> in the box below to confirm deletion.<br /><br />To delete <b>all</b> versions of the strings please check the box below.<br /><br /><span class='warning'>Warning!  This is an irreverisble operation and cannot be undone.  This will make your strings unavailable to {QW} users but will not remove it from anyone who has already downloaded the strings.",
                                                      this);
        mess.setBorder (null);
        mess.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     mess.getPreferredSize ().height));

        content.add (mess);

        content.add (Box.createVerticalStrut (10));

        final JCheckBox delAll = UIUtils.createCheckBox ("Delete all versions");

        content.add (delAll);

        content.add (Box.createVerticalStrut (10));

        final JLabel error = UIUtils.createErrorLabel ("Please enter the word Yes.");
        //"Please enter a value.");

        error.setVisible (false);
        error.setBorder (UIUtils.createPadding (0, 0, 5, 0));

        final JTextField text = UIUtils.createTextField ();

        text.setMinimumSize (new Dimension (300,
                                            text.getPreferredSize ().height));
        text.setPreferredSize (new Dimension (300,
                                              text.getPreferredSize ().height));
        text.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            text.getPreferredSize ().height));
        text.setAlignmentX (Component.LEFT_ALIGNMENT);

        error.setAlignmentX (Component.LEFT_ALIGNMENT);

        content.add (error);
        content.add (text);

        content.add (Box.createVerticalStrut (10));

        JButton confirm = null;
        JButton cancel = UIUtils.createButton ("No, keep them",
                                               new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                qp.removeFromParent ();

            }

        });

        ActionListener confirmAction = new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                String mess = UIUtils.getYesValueValidator ().isValid (text.getText ().trim ());

                if (mess != null)
                {

                    // Should probably wrap this in a
                    error.setText (mess);

                    error.setVisible (true);

                    // Got to be an easier way of doing this.
                    content.setPreferredSize (null);

                    content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                             content.getPreferredSize ().height));

                    _this.showPopupAt (qp,
                                       qp.getLocation (),
                                       false);

                    return;

                }

                String submitterid = UserProperties.get (Constants.UI_LANGUAGE_STRINGS_SUBMITTER_ID_PROPERTY_NAME);

                if ((submitterid != null)
                    &&
                    // Has they been submitted?
                    (_this.userStrings.getStringsVersion () > 0)
                   )
                {

                    URL u = null;

                    try
                    {

                        String p = Environment.getProperty (Constants.DELETE_UI_LANGUAGE_STRINGS_URL_PROPERTY_NAME);

                        p = StringUtils.replaceString (p,
                                                       Constants.ID_TAG,
                                                       _this.userStrings.getId ());

                        p = StringUtils.replaceString (p,
                                                       Constants.VERSION_TAG,
                                                       _this.userStrings.getQuollWriterVersion ().toString ());

                        p = StringUtils.replaceString (p,
                                                       Constants.ALL_TAG,
                                                       (delAll.isSelected () ? "true" : ""));

                        u = new URL (Environment.getQuollWriterWebsite () + p);

                    } catch (Exception e) {

                        Environment.logError ("Unable to construct the url for submitting the ui language strings.",
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to upload strings.");

                        return;

                    }

                    Map<String, String> headers = new HashMap<> ();

                    headers.put (Constants.UI_LANGUAGE_STRINGS_SUBMITTER_ID_HEADER_NAME,
                                 submitterid);

                    Utils.postToURL (u,
                                     headers,
                                     "bogus",
                                     // On success
                                     new ActionListener ()
                                     {

                                         @Override
                                         public void actionPerformed (ActionEvent ev)
                                         {

                                             String r = (String) JSONDecoder.decode ((String) ev.getSource ());

                                             // Delete our local versions.
                                             try
                                             {

                                                 Environment.deleteUserUILanguageStrings (_this.userStrings,
                                                                                          delAll.isSelected ());

                                             } catch (Exception e) {

                                                 Environment.logError ("Unable to delete user strings: " + _this.userStrings,
                                                                       e);

                                                 UIUtils.showErrorMessage (_this,
                                                                           "Unable to delete the strings.");

                                                  qp.removeFromParent ();

                                                  return;

                                             }

                                             LanguageStringsEditor.super.close (true,
                                                                                new ActionListener ()
                                             {

                                                 @Override
                                                 public void actionPerformed (ActionEvent ev)
                                                 {

                                                     UIUtils.showMessage ((Component) null,
                                                                          "Strings deleted",
                                                                          "Your strings have been deleted.<br /><br />Thank you for the time and effort you put in to create the strings, it is much appreciated!");

                                                 }

                                             });

                                         }

                                     },
                                     // On error
                                     new ActionListener ()
                                     {

                                         @Override
                                         public void actionPerformed (ActionEvent ev)
                                         {

                                             Map m = (Map) JSONDecoder.decode ((String) ev.getSource ());

                                             String res = (String) m.get ("reason");

                                             // Get the errors.
                                             UIUtils.showErrorMessage (_this,
                                                                       "Unable to submit the strings, reason:<ul class='error'><li>" + res + "</li></ul>");

                                         }

                                     },
                                     new ActionListener ()
                                     {

                                         @Override
                                         public void actionPerformed (ActionEvent ev)
                                         {

                                             Map m = (Map) JSONDecoder.decode ((String) ev.getSource ());

                                             String res = (String) m.get ("reason");

                                             // Get the errors.
                                             UIUtils.showErrorMessage (_this,
                                                                       "Unable to delete the strings, reason:<ul class='error'><li>" + res + "</li></ul>");

                                         }

                                     });

                } else {

                    // Not been submitted.
                    // Delete our local versions.
                    try
                    {

                        Environment.deleteUserUILanguageStrings (_this.userStrings,
                                                                 delAll.isSelected ());

                    } catch (Exception e) {

                        Environment.logError ("Unable to delete user strings: " + _this.userStrings,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to delete the strings.");

                         qp.removeFromParent ();

                         return;

                    }

                    // Close without saving.
                    LanguageStringsEditor.super.close (true,
                                                       new ActionListener ()
                    {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            UIUtils.showMessage ((Component) null,
                                                 "Strings deleted",
                                                 "Your strings have been deleted.");

                        }

                    });

                }

            }

        };

        confirm = UIUtils.createButton ("Yes, delete them",
                                        confirmAction);

        UIUtils.addDoActionOnReturnPressed (text,
                                            confirmAction);

        JButton[] buts = null;

        if (confirm != null)
        {

            buts = new JButton[] { confirm, cancel };

        } else {

            buts = new JButton[] { cancel };

        }

        JComponent buttons = UIUtils.createButtonBar2 (buts,
                                                       Component.LEFT_ALIGNMENT); //ButtonBarFactory.buildLeftAlignedBar (buts);
        buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
        content.add (buttons);
        content.setBorder (UIUtils.createPadding (10, 10, 10, 10));

        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                 content.getPreferredSize ().height));

        qp.setContent (content);
        qp.setDraggable (this);

        this.showPopupAt (qp,
                          UIUtils.getCenterShowPosition (this,
                                                         qp),
                          false);

    }

    public void showImport ()
    {

    }

	@Override
	public boolean viewStatistics ()
							throws GeneralException
	{

        throw new UnsupportedOperationException ("Not supported.");

	}

	@Override
	public boolean showChart (String chartType)
					   throws GeneralException
	{

        throw new UnsupportedOperationException ("Not supported.");

	}

	@Override
	public boolean showOptions (String section)
	{

        throw new UnsupportedOperationException ("Not supported.");

	}

    @Override
    public void showHelpText (String title,
                              String text,
                              String iconType,
                              String helpTextId)
    {

		// TODO

    }

	@Override
    public boolean viewAchievements ()
	{

		return true;

	}

	@Override
    public void sendMessageToEditor (final EditorEditor ed)
                              throws GeneralException
    {

        throw new UnsupportedOperationException ("Not supported.");

    }

	@Override
    public boolean close (boolean              noConfirm,
                          final ActionListener afterClose)
	{

        try
        {

            this.save ();

        } catch (Exception e) {

            Environment.logError ("Unable to save language strings: " +
                                  this.userStrings,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to save language strings.");

            return false;

        }

		this.setVisible (false);

		// Save state.
		Map m = new LinkedHashMap ();

		UserProperties.set ("languagestringseditor-window-height",
							this.splitPane.getSize ().height);

		UserProperties.set ("languagestringseditor-window-width",
							this.splitPane.getSize ().width);

        if (this.currentCard != null)
        {

            UserProperties.set ("languagestringseditor-stringsid-lasteditingid-" + this.userStrings.getId (),
                                this.currentCard);

            UserProperties.set ("languagestringseditor-stringsid-lasteditingscroll",
                                this.panels.get (this.currentCard).getScrollPane ().getVerticalScrollBar ().getValue ());

        }

        UserProperties.set ("languagestringseditor-sidebar-scroll",
                            this.mainSideBar.getScrollPane ().getVerticalScrollBar ().getValue ());

		// Close and remove all sidebars.
        for (AbstractSideBar sb : new ArrayList<AbstractSideBar> (this.activeSideBars))
        {

            this.removeSideBar (sb);

        }

		super.close (true,
					 afterClose);

		return true;

	}

    public void removeSideBar (AbstractSideBar sb)
    {

        if (sb == null)
        {

            return;

        }

        try
        {

            // Close the sidebar down gracefully.
            sb.onHide ();

            sb.onClose ();

        } catch (Exception e) {

            Environment.logError ("Unable to close sidebar: " + sb.getName (),
                                  e);

        }

        this.sideBars.remove (sb.getName ());

        this.activeSideBars.remove (sb);

        this.removeSideBarListener (sb);

        AbstractSideBar _sb = (this.activeSideBars.size () > 0 ? this.activeSideBars.peek () : null);

        if (_sb != null)
        {

            this.showSideBar (_sb.getName ());

        } else {

            this.setUILayout ();

        }

    }

	@Override
	public void closeSideBar ()
    {

        this.currentSideBar = null;

		this.setUILayout ();

    }

	@Override
	public AbstractSideBar getActiveOtherSideBar ()
	{

		return null;

	}

	@Override
    public int getActiveSideBarCount ()
    {

        return this.activeSideBars.size ();

	}

	@Override
    public JPopupMenu getShowOtherSideBarsPopupSelector ()
    {

        final LanguageStringsEditor _this = this;

        JPopupMenu m = new JPopupMenu ();

        // Means we are showing the main sidebar and the other sidebar.
        // Exclude those from the list.
        for (AbstractSideBar sb : this.activeSideBars)
        {

            if ((this.currentSideBar != null)
                &&
                (this.currentSideBar == sb)
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

                                                            _this.showSideBar (_sb.getName ());

                                                        }

                                                   });

            m.add (mi);

        }

        return m;

    }

    private JComponent createLayoutFiller ()
    {

        Box l = new Box (BoxLayout.X_AXIS);
        l.setMinimumSize (new Dimension (0, 0));
        l.setPreferredSize (new Dimension (0, 0));
        l.setVisible (false);

        return l;

    }

	@Override
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

            if (v.equals ("import"))
            {

				this.showImport ();

			}

        } catch (Exception e) {

            Environment.logError ("Unable to perform action: " +
                                  v,
                                  e);

        }

		super.handleHTMLPanelAction (v);

	}

	/**
	 * Display the targets for the project.
	 *
	 */
	public void viewTargets ()
                      throws GeneralException
	{

        throw new UnsupportedOperationException ("Not supported.");

	}

	@Override
    public void initActionMappings (ActionMap am)
    {

		super.initActionMappings (am);

        final LanguageStringsEditor _this = this;

        am.put ("show-main",
				new ActionAdapter ()
				{

					@Override
					public void actionPerformed (ActionEvent ev)
					{

						_this.showMainCard ();

					}

				});
/*
        am.put (Constants.SHOW_FIND_ACTION,
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.showFind ();

                    }

                });
*/
	}

    @Override
    public void initKeyMappings (InputMap im)
    {

        super.initKeyMappings (im);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE,
                                        0),
                "show-main");

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F1,
                                        0),
                Constants.SHOW_FIND_ACTION);

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
/*
    public void showFind ()
    {

        this.showSideBar ("find");

        this.finder.onShow ();

    }
*/
    private IdsPanel createIdsPanel (String id)
    {

        final LanguageStringsEditor _this = this;

        return new IdsPanel (this,
                             this.baseStrings.getNode (id),
                             this.valuesCache.get (this.baseStrings.getNode (id)));

    }

    public class IdsPanel extends BasicQuollPanel<LanguageStringsEditor>
    {

        private String parentId = null;
        private Set<LanguageStrings.Value> vals = null;
        private LanguageStrings.Node parent = null;
        private Set<LanguageStrings.Value> values = null;
        private Box content = null;
        private LanguageStringsEditor editor = null;

        public IdsPanel (LanguageStringsEditor      ed,
                         LanguageStrings.Node       parent,
                         Set<LanguageStrings.Value> values)
        {

            super (ed,
                   parent.getNodeId (),
                   null);

            this.editor = ed;

            this.parent = parent;
            this.values = values;

            //this.node = this.editor.baseStrings.getNode (id);

            String title = (this.parent.getTitle () != null ? this.parent.getTitle () : this.parent.getNodeId ());

            this.setTitle (String.format ("%s (%s)",
                                          title,
                                          Environment.formatNumber (this.values.size ())));

            this.content = new ScrollableBox (BoxLayout.Y_AXIS);
            this.content.setAlignmentY (Component.TOP_ALIGNMENT);
            this.content.setAlignmentX (Component.LEFT_ALIGNMENT);

        }

        @Override
        public String getPanelId ()
        {

            return this.parent.getNodeId ();

        }

        @Override
        public boolean isWrapContentInScrollPane ()
        {

            return true;

        }

        public void updatePreviews ()
        {

            for (int i = 0; i < this.content.getComponentCount (); i++)
            {

                Component c = this.content.getComponent (i);

                if (c instanceof IdBox)
                {

                    IdBox b = (IdBox) c;

                    b.showPreview ();

                }

            }

        }

        @Override
        public JComponent getContent ()
        {

            final IdsPanel _this = this;

            this.buildForm (this.parent.getNodeId ());

            this.content.add (Box.createVerticalGlue ());

            this.updatePreviews ();

            return this.content;

        }

        public void saveValues ()
                         throws GeneralException
        {

            for (int i = 0; i < this.content.getComponentCount (); i++)
            {

                Component c = this.content.getComponent (i);

                if (c instanceof IdBox)
                {

                    IdBox b = (IdBox) c;

                    b.saveValue ();

                }

            }

        }

        public String getIdValue (String id)
        {

            for (int i = 0; i < this.content.getComponentCount (); i++)
            {

                Component c = this.content.getComponent (i);

                if (c instanceof IdBox)
                {

                    IdBox b = (IdBox) c;

                    if (b.getId ().equals (id))
                    {

                        return b.getUserValue ();

                    }

                }

            }

            return null;

        }

        public int getErrorCount ()
        {

            int c = 0;

            for (int i = 0; i < this.content.getComponentCount (); i++)
            {

                Component co = this.content.getComponent (i);

                if (co instanceof IdBox)
                {

                    IdBox b = (IdBox) co;

                    if (b.hasErrors ())
                    {

                        c++;

                    }

                }

            }

            return c;

        }

        public int getUserValueCount ()
        {

            int c = 0;

            for (int i = 0; i < this.content.getComponentCount (); i++)
            {

                Component co = this.content.getComponent (i);

                if (co instanceof IdBox)
                {

                    IdBox b = (IdBox) co;

                    if (b.hasUserValue ())
                    {

                        c++;

                    }

                }

            }

            return c;

        }

        private void createComment (String comment)
        {

            JComponent c = UIUtils.createLabel (comment);
            c.setAlignmentX (LEFT_ALIGNMENT);
            c.setBorder (UIUtils.createPadding (0, 15, 5, 5));

            this.content.add (c);

        }

        private void buildForm (String idPrefix)
        {

            // Check for the section comment.
            if (this.parent.getComment () != null)
            {

                this.createComment (this.parent.getComment ());

            }

            for (LanguageStrings.Value v : this.values)
            {

                this.content.add (new IdBox (v,
                                             (this.editor.userStrings.containsId (v.getId ()) ? this.editor.userStrings.getValue (v.getId ()) : null),
                                             this.editor)); // scount

            }

        }

    }

    private class IdBox extends Box
    {

        private TextArea userValue = null;
        private LanguageStringsEditor editor = null;
        private Box selector = null;
        private JList<String> selections = null;
        private LanguageStrings.Value baseValue = null;
        private LanguageStrings.Value stringsValue = null;

        private JTextPane preview = null;
        private Box previewWrapper = null;
        private JLabel previewLabel = null;
        private JTextPane errors = null;
        private Box errorsWrapper = null;
        private JLabel errorsLabel = null;

        public IdBox (final LanguageStrings.Value baseValue,
                      final LanguageStrings.Value stringsValue,
                      final LanguageStringsEditor editor)
        {

            super (BoxLayout.Y_AXIS);

            final IdBox _this = this;

            this.editor = editor;
            this.baseValue = baseValue;
            this.stringsValue = stringsValue;

            Header h = UIUtils.createHeader (LanguageStrings.toId (this.baseValue.getId ()),
                                             Constants.SUB_PANEL_TITLE);

            h.setBorder (UIUtils.createBottomLineWithPadding (0, 0, 3, 0));
            h.setAlignmentX (Component.LEFT_ALIGNMENT);

            this.add (h);

            String comment = this.baseValue.getComment ();

            Box b = new Box (BoxLayout.Y_AXIS);
            FormLayout   fl = new FormLayout ("right:60px, 5px, min(150px;p):grow",
                                              (comment != null ? "top:p, 6px," : "") + "top:p, 6px, top:p:grow, 6px, top:p, top:p, top:p");
            fl.setHonorsVisibility (true);
            PanelBuilder pb = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            int r = 1;

            if (comment != null)
            {

                pb.addLabel ("<html><i>Comment</i></html>",
                             cc.xy (1, r));

                String c = "";

                if (this.baseValue.getSCount () > 0)
                {

                    for (int i = 0; i < this.baseValue.getSCount (); i++)
                    {

                        if (c.length () > 0)
                        {

                            c += ", ";

                        }

                        c += "%" + (i + 1) + "$s";

                    }

                    c = "<br /><i>Requires values: " + c + " to be present in your value.</i>";

                }

                pb.addLabel ("<html>" + comment + c + "</html>",
                             cc.xy (3, r));

                r += 2;

            }

            pb.addLabel ("<html><i>English</i></html>",
                         cc.xy (1,
                                r));

            JTextArea l = new JTextArea (baseValue.getRawText ()); //defValue);
            l.setLineWrap (true);
            l.setWrapStyleWord (true);
            l.setEditable (false);
            l.setBackground (UIUtils.getComponentColor ());
            l.setAlignmentX (Component.LEFT_ALIGNMENT);

            //l.setMinimumSize (new Dimension (200, 20));

            pb.add (l,
                    cc.xy (3, r));

            r += 2;

            pb.addLabel ("<html><i>Your Value</i></html>",
                         cc.xy (1, r));

            this.userValue = new TextArea (null,
                                           3,
                                           -1,
                                           false)
            {

                @Override
                public void fillPopupMenuForExtraItems (MouseEvent ev,
                                                        JPopupMenu popup,
                                                        boolean    compress)
                {

                    if (compress)
                    {

                        java.util.List<JComponent> buts = new java.util.ArrayList ();

                        buts.add (UIUtils.createButton (Constants.COPY_ICON_NAME,
                                                        Constants.ICON_MENU,
                                                        "Use the English value",
                                                        new ActionListener ()
                                                        {

                                                             public void actionPerformed (ActionEvent ev)
                                                             {

                                                                 _this.useEnglishValue ();

                                                             }

                                                        }));

                        popup.add (UIUtils.createPopupMenuButtonBar ("Manage",
                                                                     //"Edit",
                                                                     popup,
                                                                     buts));

                    } else {

                        JMenuItem mi = null;

                        mi = UIUtils.createMenuItem ("Use the English value",
                                                     Constants.COPY_ICON_NAME,
                                                     new ActionListener ()
                                                     {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            _this.useEnglishValue ();

                                                        }

                                                     });
                        popup.add (mi);

                    }

                }

            };

            this.userValue.addKeyListener (new KeyAdapter ()
            {

                private ScheduledFuture task = null;

                private void update ()
                {

                    if (this.task != null)
                    {

                        this.task.cancel (false);

                    }

                    this.task = _this.editor.schedule (new Runnable ()
                    {

                        @Override
                        public void run ()
                        {

                            UIUtils.doLater (new ActionListener ()
                            {

                                @Override
                                public void actionPerformed (ActionEvent ev)
                                {

                                    _this.showPreview ();

                                }

                            });

                        }

                    },
                    750,
                    0);

                }

                @Override
                public void keyPressed (KeyEvent ev)
                {

                    this.update ();

                }

                @Override
                public void keyReleased (KeyEvent ev)
                {

                    this.update ();

                }

            });

            //this.userValue.setBorder (UIUtils.createLineBorder ());

            try
            {

                this.userValue.setDictionaryProvider (new UserDictionaryProvider (UserProperties.get (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME))
                {

                    @Override
                    public SpellChecker getSpellChecker ()
                    {

                        final SpellChecker sp = super.getSpellChecker ();

                        return new SpellChecker ()
                        {

                            @Override
                            public boolean isCorrect (Word word)
                            {

                                int offset = word.getAllTextStartOffset ();

                                LanguageStrings.Id id = _this.getIdAtOffset (offset);

                                if (id != null)
                                {

                                    return id.isIdValid (_this.editor.baseStrings);

                                }

                                return sp.isCorrect (word);

                            }

                            @Override
                            public boolean isIgnored (Word word)
                            {

                                return false;

                            }

                            @Override
                            public java.util.List<String> getSuggestions (Word word)
                            {

                                return sp.getSuggestions (word);

                            }

                        };

                    }

                });

                this.userValue.setSpellCheckEnabled (true);

            } catch (Exception e) {

                e.printStackTrace ();

            }

            final Action defSelect = this.userValue.getEditor ().getActionMap ().get (DefaultEditorKit.selectWordAction);

            this.userValue.getEditor ().getActionMap ().put (DefaultEditorKit.selectWordAction,
                                                             new TextAction (DefaultEditorKit.selectWordAction)
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    int c = _this.userValue.getEditor ().getCaretPosition ();

                    Id id = _this.getIdAtCaret ();

                    if (id != null)
                    {

                        _this.userValue.getEditor ().setSelectionStart (id.getPart (c).start);
                        _this.userValue.getEditor ().setSelectionEnd (id.getPart (c).end);

                    } else {

                        defSelect.actionPerformed (ev);

                    }

                }

            });

            if (stringsValue != null)
            {

                this.userValue.setText (stringsValue.getRawText ());

            }

            this.userValue.setAutoGrabFocus (false);

            InputMap im = this.userValue.getInputMap (JComponent.WHEN_FOCUSED);
            ActionMap am = this.userValue.getActionMap ();

            im.put (KeyStroke.getKeyStroke (KeyEvent.VK_P,
                                            InputEvent.CTRL_MASK),
                    "preview");

            am.put ("preview",
                    new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.showPreview ();

                }

            });

            this.userValue.addKeyListener (new KeyAdapter ()
            {

                @Override
                public void keyPressed (KeyEvent ev)
                {

                    if ((ev.getKeyCode () == KeyEvent.VK_ENTER)
                        ||
                        (ev.getKeyCode () == KeyEvent.VK_UP)
                        ||
                        (ev.getKeyCode () == KeyEvent.VK_DOWN)
                       )
                    {

                        if (_this.isSelectorVisible ())
                        {

                            ev.consume ();

                            return;

                        }

                    }

                    if (ev.getKeyCode () == KeyEvent.VK_TAB)
                    {

                        ev.consume ();

/*
                        if (id.hasErrors ())
                        {

                            _this.hideSelector ();

                            return;

                        }
*/
                        _this.fillMatch ();//m);

                    }

                }

                @Override
                public void keyReleased (KeyEvent ev)
                {

                    if ((ev.getKeyCode () == KeyEvent.VK_CLOSE_BRACKET)
                        &&
                        (ev.isShiftDown ())
                       )
                    {

                        _this.hideSelector ();

                        return;

                    }

                    if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                    {

                        if (_this.isSelectorVisible ())
                        {

                            ev.consume ();

                            _this.fillMatch ();

                            return;

                        }

                    }

                    if (ev.getKeyCode () == KeyEvent.VK_UP)
                    {

                        if (_this.isSelectorVisible ())
                        {

                            ev.consume ();

                            _this.updateSelectedMatch (-1);

                            return;

                        }

                    }

                    if (ev.getKeyCode () == KeyEvent.VK_DOWN)
                    {

                        if (_this.isSelectorVisible ())
                        {

                            ev.consume ();

                            _this.updateSelectedMatch (1);

                            return;

                        }

                    }

                    if (ev.getKeyCode () == KeyEvent.VK_ESCAPE)
                    {

                        ev.consume ();

                        _this.hideSelector ();

                        return;

                    }

                    int c = _this.userValue.getEditor ().getCaretPosition ();

                    String t = _this.userValue.getEditor ().getText ();

                    LanguageStrings.Id id = LanguageStrings.getId (t, c);

                    if (id == null)
                    {

                        _this.hideSelector ();

                        return;

                    }

                    Set<String> matches = id.getPartMatches (c,
                                                             _this.editor.baseStrings);

                    if ((matches == null)
                        ||
                        (matches.size () == 0)
                       )
                    {

                        _this.hideSelector ();

                        return;

                    }

                    if (matches.size () == 1)
                    {

                        LanguageStrings.Id.Part p = id.getPart (c);

                        if (p == null)
                        {

                            p = id.getLastPart ();

                        }

                        if (p.part.equals (matches.iterator ().next ()))
                        {

                            _this.hideSelector ();
                            return;

                        }

                    }

                    try
                    {

                        int ind = c;

                        LanguageStrings.Id.Part pa = id.getPart (c);

                        if (pa != null)
                        {

                            ind = pa.start;

                        }

                        Rectangle r = _this.userValue.getEditor ().modelToView (ind);

                        //Point p = r.getLocation ();
                        //p.y -= 10;
                        //p.x -= 10;
                        //p.y += r.height;.

                        _this.showSelectionPopup (matches,
                                                  r.getLocation ());

                    } catch (Exception e) {

                        e.printStackTrace ();

                    }

                }

            });

            pb.add (this.userValue,
                    cc.xy (3, r));

            r += 2;

            // Needed to prevent the performance hit
            this.errorsWrapper = new Box (BoxLayout.Y_AXIS);

            this.errorsLabel = UIUtils.createErrorLabel ("Errors");
            this.errorsLabel.setBorder (UIUtils.createPadding (6, 0, 0, 0));
            this.errorsLabel.setVisible (false);
            this.errorsLabel.setIcon (null);
            this.errorsLabel.setFocusable (false);

            pb.add (this.errorsLabel,
                    cc.xy (1, r));
            pb.add (this.errorsWrapper,
                    cc.xy (3, r));

            r += 1;

            // Needed to prevent the performance hit
            this.previewWrapper = new Box (BoxLayout.Y_AXIS);

            this.previewLabel = UIUtils.createInformationLabel ("Preview");
            this.previewLabel.setBorder (UIUtils.createPadding (6, 0, 0, 0));
            this.previewLabel.setVisible (false);

            pb.add (this.previewLabel,
                    cc.xy (1, r));
            pb.add (this.previewWrapper,
                    cc.xy (3, r));

            JPanel p = pb.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (Component.LEFT_ALIGNMENT);
            p.setBorder (UIUtils.createPadding (5, 5, 0, 0));

            this.add (p);

            this.setBorder (UIUtils.createPadding (0, 10, 20, 10));
            this.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.setAlignmentY (Component.TOP_ALIGNMENT);

        }

        public void saveValue ()
                        throws GeneralException
        {

            String uv = this.getUserValue ();

            if (uv != null)
            {

                if (this.stringsValue != null)
                {

                    this.stringsValue.setRawText (uv);

                } else {

                    this.stringsValue = this.editor.userStrings.insertValue (this.baseValue.getId ());

                    //this.stringsValue.setSCount (this.baseValue.getSCount ());
                    this.stringsValue.setRawText (uv);

                }

            } else {

                this.editor.userStrings.removeNode (this.baseValue.getId ());

            }

        }

        public LanguageStrings.Id getIdAtOffset (int offset)
        {

            return LanguageStrings.getId (this.userValue.getEditor ().getText (),
                                          offset);
/*
            Id id = new Id (this.userValue.getEditor ().getText (),
                            offset);

            if (id.fullId == null)
            {

                return null;

            }

            return id;
*/
        }

        public LanguageStrings.Id getIdAtCaret ()
        {

            return this.getIdAtOffset (this.userValue.getEditor ().getCaretPosition ());

        }

        public String getId ()
        {

            return LanguageStrings.toId (this.baseValue.getId ());

        }

        public boolean hasUserValue ()
        {

            return this.getUserValue () != null;

        }

        public String getUserValue ()
        {

            StringWithMarkup sm = this.userValue.getTextWithMarkup ();

            if (sm != null)
            {

                if (!sm.hasText ())
                {

                    return null;

                }

                return sm.getMarkedUpText ();

            }

            return null;

        }

        public void useEnglishValue ()
        {

            this.userValue.updateText (this.baseValue.getRawText ());
            this.showPreview ();
            this.validate ();
            this.repaint ();

        }

        public boolean hasErrors ()
        {

            String s = this.getUserValue ();

            if (s == null)
            {

                return false;

            }

            return LanguageStrings.getErrors (s,
                                              LanguageStrings.toId (this.baseValue.getId ()),
                                              this.baseValue.getSCount (),
                                              this.editor).size () > 0;


        }

        public boolean showErrors (boolean requireUserValue)
        {

            String s = this.getUserValue ();

            if ((s == null)
                &&
                (!requireUserValue)
               )
            {

                this.errorsLabel.setVisible (false);
                this.errorsWrapper.setVisible (false);

                return false;

            }

            Set<String> errs = null;

            if (s == null)
            {

                errs = new LinkedHashSet<> ();

                errs.add ("Cannot show a preview, no value provided.");

            } else {

                errs = LanguageStrings.getErrors (s,
                                                  LanguageStrings.toId (this.baseValue.getId ()),
                                                  this.baseValue.getSCount (),
                                                  this.editor);

            }

            LanguageStrings.Node root = this.baseValue.getRoot ();

            this.editor.updateSideBar (this.baseValue);

            if (errs.size () > 0)
            {

                if (this.errors == null)
                {

                    this.errors = UIUtils.createHelpTextPane ("",
                                                              this.editor);
                    this.errors.setBorder (UIUtils.createPadding (6, 0, 0, 0));
                    this.errors.setFocusable (false);
                    this.errorsWrapper.add (this.errors);

                }

                StringBuilder b = new StringBuilder ();

                for (String e : errs)
                {

                    if (b.length () > 0)
                    {

                        b.append ("<br />");

                    }

                    b.append ("- " + e);

                }

                this.errors.setText ("<span class='error'>" + b.toString () + "</span>");
                this.errorsLabel.setVisible (true);
                this.errorsWrapper.setVisible (true);

                this.editor.updateSideBar (this.baseValue);

                return true;

            } else {

                this.errorsLabel.setVisible (false);
                this.errorsWrapper.setVisible (false);

            }

            return false;

        }

        public void showPreview ()
        {

            if (this.showErrors (false))
            {

                return;

            }

            String s = this.getUserValue ();

            if (s == null)
            {

                if (this.preview != null)
                {

                    this.preview.setText ("");

                }

                this.previewWrapper.setVisible (false);
                this.previewLabel.setVisible (false);

                this.editor.updateSideBar (this.baseValue);

                return;

            }

            if (this.preview == null)
            {

                this.preview = UIUtils.createHelpTextPane ("",
                                                           this.editor);
                this.preview.setBorder (UIUtils.createPadding (6, 0, 0, 0));
                this.preview.setFocusable (false);

                this.previewWrapper.add (this.preview);

            }

            String t = this.editor.getPreviewText (s);

            this.previewLabel.setVisible (true);
            this.preview.setText (t);
            this.previewWrapper.setVisible (true);

            this.editor.updateSideBar (this.baseValue);

            this.validate ();
            this.repaint ();

        }

        private void fillMatch ()
        {

            QTextEditor editor = this.userValue.getEditor ();

            int c = editor.getCaretPosition ();

            LanguageStrings.Id id = LanguageStrings.getId (editor.getText (),
                                                           c);

            if (id == null)
            {

                return;

            }

            String m = this.selections.getSelectedValue ();

            //m = id.getNewFullId (m);

            LanguageStrings.Id.Part part = id.getPart (c);

            if (part != null)
            {

                editor.replaceText (part.start, part.end, m);
                editor.setCaretPosition (part.start + m.length ());

            } else {

                // Is the previous character a . if so append.
                if ((editor.getText ().substring (c - 1, c).equals ("."))
                    ||
                    (c == id.getEnd ())
                   )
                {

                    editor.replaceText (c, c, m);
                    editor.setCaretPosition (c + m.length ());

                } else {

                    part = id.getLastPart ();

                    editor.replaceText (part.start, part.start + part.end, m);
                    editor.setCaretPosition (part.start + m.length ());

                }

            }

            c = editor.getCaretPosition ();

            id = this.getIdAtCaret ();

            // We may be inserting into the middle of an id, check to see if it's valid.
/*
            if (id.hasErrors ())
            {

                this.hideSelector ();

                // Update the view to "spell check" the ids.
                return;

            }
*/
            // Check to see if the id maps to a string.
            if (this.editor.baseStrings.getString (id.getId ()) != null)
            {

                if (id.isPartial ())
                {

                    editor.replaceText (id.getEnd (), id.getEnd (), "}");
                    editor.setCaretPosition (c + 1);

                }

                this.hideSelector ();

                return;

            }

            // Check to see if there are more matches further down the tree.
            String nid = id.getId () + ".";

            Set<String> matches = this.editor.baseStrings.getIdMatches (nid);

            if (matches.size () > 0)
            {

                c = editor.getCaretPosition ();

                editor.replaceText (c, c, ".");
                editor.setCaretPosition (c + 1);

                try
                {

                    this.showSelectionPopup (matches,
                                             editor.modelToView (id.getPart (c).start).getLocation ());

                } catch (Exception e) {

                    e.printStackTrace ();

                }

                return;

            } else {

                c = editor.getCaretPosition ();

                editor.replaceText (c, c, "}");
                editor.setCaretPosition (c + 1);

                this.hideSelector ();

                return;

            }

        }

        private String updateSelectedMatch (int incr)
        {

            int i = this.selections.getSelectedIndex ();

            i += incr;

            int s = this.selections.getModel ().getSize ();

            if (i < 0)
            {

                i = s + i;

            }

            if (i > s - 1)
            {

                i -= s;

            }

            this.selections.setSelectedIndex (i);

            return this.selections.getSelectedValue ();

        }

        public boolean isSelectorVisible ()
        {

            if (this.selector != null)
            {

                return this.selector.isVisible ();

            }

            return false;

        }

        public void hideSelector ()
        {

            if (this.selector != null)
            {

                this.selector.setVisible (false);

            }

            this.userValue.getEditor ().setFocusTraversalKeysEnabled (true);

        }

        public void showSelectionPopup (Set<String> matches,
                                        Point       point)
        {

            if (this.selector == null)
            {

                this.selector = new Box (BoxLayout.Y_AXIS);

                this.selector.setOpaque (true);
                this.selector.setBackground (UIUtils.getComponentColor ());
                this.selector.setBorder (UIUtils.createLineBorder ());

            }

            if ((matches == null)
                ||
                (matches.size () == 0)
               )
            {

                this.hideSelector ();

                return;

            }

            this.userValue.getEditor ().setFocusTraversalKeysEnabled (false);

            this.selector.removeAll ();

            DefaultListModel<String> m = new DefaultListModel<> ();

            for (String o : matches)
            {

                m.addElement (o);

            }

            this.selections = new JList<String> ();
            final JList<String> l = this.selections;
            l.setModel (m);
            l.setLayoutOrientation (JList.VERTICAL);
            l.setVisibleRowCount (0);
            l.setOpaque (true);
            l.setBackground (UIUtils.getComponentColor ());
            l.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                             Short.MAX_VALUE));
            UIUtils.setAsButton (l);

            l.setCellRenderer (new DefaultListCellRenderer ()
            {

                public Component getListCellRendererComponent (JList   list,
                                                               Object  value,
                                                               int     index,
                                                               boolean isSelected,
                                                               boolean cellHasFocus)
                {

                    String obj = (String) value;

                    JLabel l = (JLabel) super.getListCellRendererComponent (list,
                                                                            value,
                                                                            index,
                                                                            isSelected,
                                                                            cellHasFocus);

                    l.setText (obj);//.getName ());

                    l.setFont (l.getFont ().deriveFont (UIUtils.getScaledFontSize (10)).deriveFont (Font.PLAIN));
/*
                    l.setIcon (Environment.getObjectIcon (obj,
                                                          Constants.ICON_NOTIFICATION));
*/
                    l.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));
                    l.setPreferredSize (new Dimension (l.getPreferredSize ().width, 29));
/*
                    if (cellHasFocus)
                    {

                        l.setBackground (Environment.getHighlightColor ());

                    }
*/
                    return l;

                }

            });

            l.setSelectedIndex (0);

            int rowHeight = 30;

            l.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            JScrollPane sp = new JScrollPane (l);

            sp.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sp.getVerticalScrollBar ().setUnitIncrement (rowHeight);
            sp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            sp.setOpaque (false);
/*
            sp.getViewport ().setPreferredSize (new Dimension (300,
                                                               rowHeight * (matches.size () > 10 ? 10 : matches.size ())));
*/
            sp.setBorder (null);

            this.selector.add (sp);

            l.addListSelectionListener (new ListSelectionListener ()
            {

                @Override
                public void valueChanged (ListSelectionEvent ev)
                {
/*
                    if (onSelect != null)
                    {

                        NamedObject obj = (NamedObject) l.getSelectedValue ();

                        onSelect.actionPerformed (new ActionEvent (l,
                                                                   0,
                                                                   obj.getObjectReference ().asString ()));

                        if (closeOnSelect)
                        {

                            ep.removeFromParent ();

                        }

                    }
*/
                }

            });

            this.selector.setPreferredSize (new Dimension (300,
                                                           rowHeight * (matches.size () > 10 ? 10 : matches.size ())));

            this.editor.showPopupAt (this.selector,
                                     SwingUtilities.convertPoint (this.userValue,
                                                                  point,
                                                                  this.editor),
                                     false);

        }

        @Override
        public Dimension getMaximumSize ()
        {

            return new Dimension (Short.MAX_VALUE,
                                  this.getPreferredSize ().height);

        }
/*
        public class Id
        {

            private int _start = -1;
            private String fullId = null;
            private boolean hasClosingBrace = false;
            private java.util.List<Part> parts = new ArrayList<> ();
            public boolean hasErrors = false;

            public Id (String text,
                       int    offset)
            {

                if (text.length () < 3)
                {

                    return;

                }

                String idstart = LanguageStrings.ID_REF_START;

                int ind = text.lastIndexOf (idstart, offset);

                if (ind > -1)
                {

                    ind += idstart.length ();
                    this._start = ind;

                    int idendind = text.indexOf (LanguageStrings.ID_REF_END, ind);

                    if (idendind > -1)
                    {

                        if (idendind < offset)
                        {

                            return;

                        }

                        // We have an end, see if it's on the same line.
                        int leind = text.indexOf ("\n", ind);

                        if (leind < 0)
                        {

                            leind = text.length ();

                        }

                        if (idendind < leind)
                        {

                            this.hasClosingBrace = true;
                            this.fullId = text.substring (ind, leind - 1);

                        }

                    } else {
System.out.println ("ELSE");
                        StringBuilder b = new StringBuilder ();

                        for (int i = ind; i < text.length (); i++)
                        {

                            char c = text.charAt (i);

                            if (Character.isWhitespace (c))
                            {

                                break;

                            }

                            b.append (c);

                        }

                        this.fullId = b.toString ();

                    }
System.out.println ("ID: " + this.fullId);
                    if (this.fullId.equals (""))
                    {

                        this.fullId = null;

                    }

                    int start = ind;

                    java.util.List<String> parts = Utils.splitString (this.fullId,
                                                                      ".");

                    int cind = start;

                    Part prevp = null;

                    for (int i = 0; i < parts.size (); i++)
                    {

                        if (i > 0)
                        {

                            cind++;

                        }

                        String ps = parts.get (i);

                        if (ps.trim ().length () != ps.length ())
                        {

                            this.hasErrors = true;

                        }

                        Part p = new Part (this,
                                           cind,
                                           ps,
                                           prevp);

                        prevp = p;
                        cind += ps.length ();

                        this.parts.add (p);

                    }

                }

            }

            public int getEnd ()
            {

                if (this.parts.size () == 0)
                {

                    return this._start;

                }

                return this.parts.get (this.parts.size () - 1).end;

            }

            public Part getPart (int offset)
            {

                for (int i = 0; i < this.parts.size (); i++)
                {

                    Part p = this.parts.get (i);

                    if ((offset >= p.start)
                        &&
                        (offset <= p.end)
                       )
                    {

                        return p;

                    }

                }

                return null;

            }

            public String getFullId ()
            {

                return this.fullId;

            }

            public boolean isIdValid (LanguageStrings baseStrings)
            {

                if (this.fullId == null)
                {

                    return false;

                }

                return baseStrings.isIdValid (this.fullId);

            }

            public boolean hasErrors ()
            {

                return this.hasErrors;

            }

            public String getNewFullId (String suffix)
            {

                if (this.fullId.endsWith ("."))
                {

                    return this.fullId + suffix;

                }

                String pref = this.getIdPrefix ();

                if (pref == null)
                {

                    return suffix;

                }

                return pref + "." + suffix;

            }

            public String getIdPrefix ()
            {

                StringBuilder b = new StringBuilder ();

                for (int i = 0; i < this.parts.size () - 1; i++)
                {

                    if (i > 0)
                    {

                        b.append (".");

                    }

                    b.append (this.parts.get (i).part);

                }

                if (b.length () == 0)
                {

                    return null;

                }

                return b.toString ();

            }

            public Part getLastPart ()
            {

                if (this.parts.size () == 0)
                {

                    return null;

                }

                return this.parts.get (this.parts.size () - 1);

            }

            public Set<String> getPartMatches (int             offset,
                                               LanguageStrings baseStrings)
            {

                Part p = this.getPart (offset);

                if (p != null)
                {

                    return baseStrings.getIdMatches (p.getFullId ());

                }

                return this.getMatches (baseStrings);

            }

            public Set<String> getMatches (LanguageStrings baseStrings)
            {

                return baseStrings.getIdMatches (this.fullId);

            }

            public class Part
            {

                public int start = -1;
                public int end = -1;
                public String part = null;
                public Id parent = null;
                public Part previous = null;

                public Part (Id     parent,
                             int    start,
                             String part,
                             Part   prev)
                {

                    this.start = start;
                    this.end = this.start + part.length ();
                    this.parent = parent;
                    this.part = part;
                    this.previous = prev;

                }

                public String getFullId ()
                {

                    StringBuilder b = new StringBuilder (this.part);

                    Part prev = this.previous;

                    while (prev != null)
                    {

                        b.insert (0, prev.part + ".");
                        prev = prev.previous;

                    }

                    return b.toString ();

                }

            }

        }
*/

    }

}
