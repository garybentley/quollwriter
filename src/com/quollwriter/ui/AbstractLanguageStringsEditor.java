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
import com.quollwriter.uistrings.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public abstract class AbstractLanguageStringsEditor<B extends AbstractLanguageStrings, U extends AbstractLanguageStrings> extends AbstractViewer implements RefValueProvider
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
    private Finder                finder = null;
    private Map<String, AbstractSideBar> sideBars = new HashMap ();
    private Stack<AbstractSideBar>  activeSideBars = new Stack ();
    private java.util.List<SideBarListener> sideBarListeners = new ArrayList ();
    private java.util.List<MainPanelListener> mainPanelListeners = new ArrayList ();
	private JPanel                cards = null;
	private CardLayout            cardsLayout = null;
	private JLabel                importError = null;
    private JLabel                forwardLabel = null;
    private ImportTransferHandlerOverlay   importOverlay = null;
    protected Map<String, LanguageStringsIdsPanel> panels = new HashMap<> ();
	private String currentCard = null;
    ///private Finder                finder = null;
    protected U userStrings = null;
    private String toolbarLocation = null;
    private Map<String, JTree> sectionTrees = new LinkedHashMap<> ();
    protected B baseStrings = null;
    protected Filter<Node> nodeFilter = null;
    private Map<Node, Set<Value>> valuesCache = new HashMap<> ();
    private Map<Node, Number> errCounts = new HashMap<> ();
    private Map<Node, Number> userCounts = new HashMap<> ();
    private boolean inited = false;
    private TreeCellRenderer treeCellRenderer = null;
    private boolean updatingPreviews = false;

    public AbstractLanguageStringsEditor (U userStrings)
    {

        this.userStrings = userStrings;

        this.baseStrings = (B) this.userStrings.getDerivedFrom ();

    }

    public AbstractLanguageStringsEditor (U userStrings,
                                          B baseStrings)
    {

        this.userStrings = userStrings;
        this.baseStrings = baseStrings;

    }

    public abstract void tryOut ();

    public abstract void save ()
                        throws Exception;

    public abstract void submit (ActionListener onSuccess,
                                 ActionListener onFailure);

    public abstract void delete ()
                          throws Exception;

    public abstract void onForwardLabelClicked ()
                                         throws Exception;

    public abstract void showReportProblemForId (String id);

    public void showForwardLabel (String text)
    {

        this.forwardLabel.setText (text);
        this.forwardLabel.setVisible (true);

    }

    public TreeCellRenderer getTreeCellRenderer ()
    {

        return this.treeCellRenderer;

    }

    private int getErrorCount (Node n)
    {

        int c = 0;

        // Get the card.
        Number num = this.errCounts.get (n);

        if (num != null)
        {

            return num.intValue ();

        }

        LanguageStringsIdsPanel panel = this.panels.get (BaseStrings.toId (n.getId ()));

        if (panel != null)
        {

            c = panel.getErrorCount ();

        } else {

            for (Value nv : this.valuesCache.get (n))
            {

                Value uv = this.userStrings.getValue (nv.getId (),
                                                      true);

                if (uv instanceof TextValue)
                {

                    TextValue _nv = this.baseStrings.getTextValue (uv.getId ());

                    if (nv == null)
                    {

                      // The string is present in the user strings but not the base!
                      Environment.logError ("Found string: " + uv.getId () + " present in user strings but not base.");

                      continue;

                    }

                    if (BaseStrings.getErrors (((TextValue) uv).getRawText (),
                                                 BaseStrings.toId (nv.getId ()),
                                                 _nv.getSCount (),
                                                 this).size () > 0)
                    {

                        c++;

                    };

                }

            }

        }

        this.errCounts.put (n,
                            c);

        return c;

    }

    private int getUserValueCount (Node n)
    {

        int c = 0;

        Number num = this.userCounts.get (n);

        if (num != null)
        {

            return num.intValue ();

        }

        LanguageStringsIdsPanel panel = this.panels.get (BaseStrings.toId (n.getId ()));

        if (panel != null)
        {

            c = panel.getUserValueCount ();

        } else {

            for (Value nv : this.valuesCache.get (n))
            {

                Value uv = this.userStrings.getValue (nv.getId (),
                                                      true);

                if (uv != null)
                {

                    c++;

                }

            }

        }

        this.userCounts.put (n,
                             c);

        return c;

    }

    public void setNodeFilter (Filter<Node> filter)
    {

        this.nodeFilter = filter;

        this.initSideBar ();

    }

    private void initSideBar ()
    {

        if (this.baseStrings == null)
        {

            throw new IllegalStateException ("No base strings set.");

        }

        String defSection = "General";

        Map<String, Set<Node>> sections = this.baseStrings.getNodesInSections (defSection);

        java.util.List<AccordionItem> items = new ArrayList<> ();

        this.valuesCache = new HashMap<> ();

        for (Section sect : (Set<Section>) this.baseStrings.getSections ())
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

    }

    public void showAllStrings ()
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

/*
    public void showChanges (LanguageStrings newls)
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
*/
/*
    public void limitViewToPreviousVersionDiff ()
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

                if ((pn instanceof LanguageStrings.TextValue)
                    &&
                    (n instanceof LanguageStrings.TextValue)
                   )
                {

                    LanguageStrings.TextValue pnv = (LanguageStrings.TextValue) pn;
                    LanguageStrings.TextValue nv = (LanguageStrings.TextValue) n;

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
*/
    public void updateSideBar (final Node n)
    {

        final AbstractLanguageStringsEditor<B, U> _this = this;

        this.errCounts.remove (n);
        this.userCounts.remove (n);

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                for (JTree t : _this.sectionTrees.values ())
                {

                    // See how many children there are.
                    TreePath tp = UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) t.getModel ().getRoot (),
                                                                    n);

                    if (tp != null)
                    {

                        ((DefaultTreeModel) t.getModel ()).nodeChanged ((DefaultMutableTreeNode) tp.getLastPathComponent ());

                        t.validate ();
                        t.repaint ();

                        break;

                    }

                }

                _this.sideBar.validate ();
                _this.sideBar.repaint ();

            }

        });

    }
/*
    private void updateSideBar ()
    {

        this.sideBar.validate ();
        this.sideBar.repaint ();

    }
*/
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

        return BaseStrings.buildText (t,
                                      this);

    }

    public void updatePreviews ()
    {

        // XXX TODO
        if (true)
        {

            return;

        }

        final AbstractLanguageStringsEditor<B, U> _this = this;

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                if (_this.updatingPreviews)
                {

                    return;

                }

                try
                {

                    _this.updatingPreviews = true;

                    for (LanguageStringsIdsPanel p : _this.panels.values ())
                    {

                        p.updatePreviews ();

                    }

                } catch (Exception e) {

                    Environment.logError ("Unable to update previews.",
                                          e);

                } finally {

                    _this.updatingPreviews = false;

                }

            }

        });

    }

    @Override
    public String getString (String id)
    {

        java.util.List<String> idparts = BaseStrings.getIdParts (id);

        // See if we have a panel.
        if (idparts.size () > 0)
        {

            for (LanguageStringsIdsPanel p : this.panels.values ())
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

        for (LanguageStringsIdsPanel p : this.panels.values ())
        {

            String t = p.getIdValue (id);

            if (t != null)
            {

                return t;

            }

        }

        return this.userStrings.getRawText (id);

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

    private AccordionItem createSectionTree (String    title,
                                             String    iconType,
                                             Set<Node> sections)
    {

        final AbstractLanguageStringsEditor<B, U> _this = this;

        final JTree tree = this.createStringsTree (sections);

        if (tree == null)
        {

            return null;

        }

        tree.setCellRenderer (this.treeCellRenderer);

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

                    Node node  = (Node) n.getUserObject ();

                    String id = BaseStrings.toId (node.getId ());

                    _this.showIds (id);

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

        this.showIds (idPrefix,
                      null);

    }

    public void showIds (String         idPrefix,
                         ActionListener onShow)
    {

        if (!this.valuesCache.containsKey (this.baseStrings.getNode (idPrefix)))
        {

            return;

        }

        //String id = node.getNodeId ();
        LanguageStringsIdsPanel p = this.panels.get (idPrefix);

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

        if (onShow != null)
        {

            UIUtils.doLater (onShow);

        }

    }

    public void showId (final String id)
    {

        final AbstractLanguageStringsEditor _this = this;

        String idPrefix = null;

        // Get the relevant prefix.
        for (JTree t : this.sectionTrees.values ())
        {

            Enumeration en = ((DefaultMutableTreeNode) t.getModel ().getRoot ()).children ();

            while (en.hasMoreElements ())
            {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement ();

                Node n = (Node) node.getUserObject ();

                if (id.startsWith (BaseStrings.toId (n.getId ())))
                {

                    idPrefix = BaseStrings.toId (n.getId ());

                    break;

                }

            }

            if (idPrefix != null)
            {

                break;

            }

        }

        if (idPrefix == null)
        {

            return;

        }

        this.showIds (idPrefix,
                      new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                _this.scrollToNode (id);

            }

        });

    }

    public void scrollToNode (String n)
    {

        LanguageStringsIdsPanel c = this.panels.get (this.currentCard);

        c.scrollToNode (n);

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

    private JTree createStringsTree (Set<Node> sections)
    {

        final AbstractLanguageStringsEditor _this = this;

        DefaultMutableTreeNode root = new DefaultMutableTreeNode ("_strings");

        for (Node k : sections)
        {

            // Find all the top level nodes.
            Set<String> tlNodes = k.getTopLevelNodes ();

            Set<Value> vals = null;

            Node n = null;

            if ((tlNodes != null)
                &&
                (tlNodes.size () > 0)
               )
            {

                // Get the nodes, pass through the filter as well.
                vals = new LinkedHashSet<> ();

                for (String tln : tlNodes)
                {

                    Node x = k.getChild (BaseStrings.getIdParts (tln));

                    if (x != null)
                    {

                        if (this.nodeFilter != null)
                        {

                            if (this.nodeFilter.accept (x))
                            {

                                n = x;

                            }

                        } else {

                            n = x;

                        }

                    }

                    vals = n.getValues (this.nodeFilter);

                    if (vals.size () == 0)
                    {

                        continue;

                    }

                    this.valuesCache.put (n, vals);

                    root.add (new DefaultMutableTreeNode (n));

                }

                continue;

            }

            vals = k.getValues (this.nodeFilter);

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

        this.updateToolbarForPanel (this.panels.get (this.currentCard));

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

    protected void setBaseStrings (B ls)
    {

        this.baseStrings = ls;

        if (this.inited)
        {

            this.initSideBar ();

        }

    }

	@Override
    public void init ()
			   throws Exception
    {

        if (this.inited)
        {

            return;

        }

        this.inited = true;

		super.init ();

        final AbstractLanguageStringsEditor<B, U> _this = this;

        this.treeCellRenderer = new DefaultTreeCellRenderer ()
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

                Node n = (Node) tn.getUserObject ();

                final java.util.List<String> id = n.getId ();

                int c = _this.getUserValueCount (n);
                int alls = 0;
                int errCount = _this.getErrorCount (n);

                Set<Value> vals = _this.valuesCache.get (n);

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

                this.setText ("<html>" + name + "</html>");

                this.setBorder (new EmptyBorder (2, 2, 2, 2));

                return this;

            }

        };

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

        this.forwardLabel = UIUtils.createClickableLabel ("",
                                                          Environment.getIcon (Constants.INFO_ICON_NAME,
                                                                               Constants.ICON_CLICKABLE_LABEL));
        this.forwardLabel.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));
        this.forwardLabel.setVisible (false);

        UIUtils.makeClickable (this.forwardLabel,
                               new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                try
                {

                    _this.onForwardLabelClicked ();

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
            public String getId ()
            {

                return "main";

            }

            @Override
            public JComponent getContent ()
            {

                Box b = new Box (BoxLayout.Y_AXIS);

                b.add (_this.forwardLabel);

                //_this.forwardLabel.setVisible (Environment.getQuollWriterVersion ().isNewer (_this.userStrings.getQuollWriterVersion ()));

                b.add (super.getContent ());

                b.setBorder (UIUtils.createPadding (0, 0, 0, 0));

                b.setPreferredSize (new Dimension (200, Short.MAX_VALUE));

                return b;

            }

            @Override
            public Dimension getMinimumSize ()
            {

                return new Dimension (250,
                                      250);
            }

            @Override
            public void panelShown (MainPanelEvent ev)
            {

                _this.setIdSelectedInSidebar (ev.getPanel ().getPanelId ());

            }

        };

        this.addSideBar (this.mainSideBar);

        this.finder = new Finder<AbstractLanguageStringsEditor, AccordionItem> (this)
        {

            @Override
            public String getTitle ()
            {

                return "Find";

            }

            @Override
            public Set<AccordionItem> search (String t)
            {

                Set<AccordionItem> res = new LinkedHashSet<> ();

                Map<String, Section> sects = new HashMap<> ();

                for (Section sect : (Set<Section>) _this.baseStrings.getSections ())
                {

                    sects.put (sect.id,
                               sect);

                }

                Set<Value> results = _this.baseStrings.find (t);

                Set<Value> uresults = _this.userStrings.find (t);

                for (Value v : uresults)
                {

                    results.add (_this.baseStrings.getValue (v.getId ()));

                }

                Map<Section, Map<Node, java.util.List<Value>>> vals = new HashMap<> ();

                for (Value v : results)
                {

                    Node r = v.getRoot ();

                    Section s = sects.get (r.getSection ());

                    Map<Node, java.util.List<Value>> svs = vals.get (s);

                    if (svs == null)
                    {

                        svs = new LinkedHashMap<> ();

                        vals.put (s,
                                  svs);

                    }

                    Set<String> tlns = r.getTopLevelNodes ();

                    if (tlns != null)
                    {

                        for (String tln : tlns)
                        {

                            String tlnid = r.getNodeId () + "." + tln;

                            Node tlnn = _this.baseStrings.getNode (tlnid);

                            if (BaseStrings.toId (v.getId ()).startsWith (tlnid))
                            {

                                java.util.List<Value> l = svs.get (tlnn);

                                if (l == null)
                                {

                                    l = new ArrayList<> ();

                                    svs.put (tlnn,
                                             l);

                                }

                                l.add (v);

                            }

                        }

                    } else {

                        java.util.List<Value> l = svs.get (r);

                        if (l == null)
                        {

                            l = new ArrayList<> ();

                            svs.put (r,
                                     l);

                        }

                        l.add (v);

                    }

                }

                for (Section sect : vals.keySet ())
                {

                    final Map<Node, java.util.List<Value>> vs = vals.get (sect);

                    // Build the tree.
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode ("_strings");

                    for (Node n : vs.keySet ())
                    {

                        DefaultMutableTreeNode tn = new DefaultMutableTreeNode (n);

                        root.add (tn);

                        for (Value v : vs.get (n))
                        {

                            tn.add (new DefaultMutableTreeNode (BaseStrings.toId (v.getId ())));

                        }

                    }

                    final JTree tree = UIUtils.createTree ();

                    tree.setBorder (new EmptyBorder (0, 7, 0, 0));

                    DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel ();

                    dtm.setRoot (root);

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

                                this.setIcon (null);

                                return this;

                            }

                            this.setIcon (null);

                            DefaultMutableTreeNode pn = (DefaultMutableTreeNode) tn.getParent ();

                            Node n = (Node) tn.getUserObject ();

                            final java.util.List<String> id = n.getId ();

                            int c = tn.getChildCount ();

                            String title = (n.getTitle () != null ? n.getTitle () : n.getNodeId ());

                            String name = null;

                            name = String.format ("%s (%s)",
                                                  title,
                                                  Environment.formatNumber (c));

                            this.setText ("<html>" + name + "</html>");

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

                                Object o = n.getUserObject ();

                                DefaultMutableTreeNode p = (DefaultMutableTreeNode) n.getParent ();

                                Node pnode  = (Node) p.getUserObject ();

                                String id = BaseStrings.toId (pnode.getId ());

                                _this.showIds (id,
                                               new ActionListener ()
                                {

                                    @Override
                                    public void actionPerformed (ActionEvent ev)
                                    {

                                        _this.scrollToNode (o.toString ());

                                    }

                                });

                            }

                        }

                    });

                    AccordionItem it = new AccordionItem (sect.name,
                                                          sect.icon)
                    {

                        @Override
                        public JComponent getContent ()
                        {

                            return tree;

                        }

                    };

                    it.init ();

                    res.add (it);

                }

                return res;

            }

        };

        this.addSideBar (this.finder);

        if (this.baseStrings != null)
        {

            this.initSideBar ();

        }

        //this.initSideBar ();
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
        this.sideBar.add (this.toolbarPanel);

        this.setToolbarLocation (null);

        this.toolbarPanel.setBorder (new CompoundBorder (new MatteBorder (1, 0, 0, 0, UIUtils.getBorderColor ()),
                                                         new EmptyBorder (0,
                                                                          5,
                                                                          0,
                                                                          5)));

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

    }

    private void updateToolbarForPanel (QuollPanel qp)
    {

        if (qp != null)
        {

            this.toolbarPanel.removeAll ();

            JToolBar tb = qp.getToolBar (false);

            this.toolbarPanel.add (tb);

            this.toolbarPanel.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                             this.toolbarPanel.getPreferredSize ().height));

            this.toolbarPanel.revalidate ();
            this.toolbarPanel.repaint ();

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

        this.sideBarWrapper.removeAll ();

        this.sideBarWrapper.add (this.currentSideBar);

		this.activeSideBars.remove (b);

		this.activeSideBars.push (b);

		this.currentSideBar.setVisible (true);

        this.validate ();
        this.repaint ();

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

        if (true)
        {

            return;

        }

        final AbstractLanguageStringsEditor _this = this;

		this.splitPane.setResizeWeight (1f);

		if (this.currentSideBar != null)
		{

			this.splitPane.setDividerSize (INTERNAL_SPLIT_PANE_DIVIDER_WIDTH);
			this.splitPane.setLeftComponent (this.currentSideBar);

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
    public JComponent getTitleHeaderControl (String id)
	{

		if (id == null)
		{

			return null;

		}

        java.util.List<String> prefix = Arrays.asList (allprojects,headercontrols,items);

		final AbstractLanguageStringsEditor _this = this;

		JComponent c = null;

        if (id.equals (FIND_HEADER_CONTROL_ID))
        {

            return UIUtils.createButton (Constants.FIND_ICON_NAME,
                                         Constants.ICON_TITLE_ACTION,
                                         "Find",
                                          new ActionAdapter ()
                                          {

                                              public void actionPerformed (ActionEvent ev)
                                              {

                                                  _this.showFind (null);

                                              }

                                          });

        }

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

                                            _this.submit (null,
                                                          null);

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

                                            _this.tryOut ();

                                        }

                                      });

        }
/*
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
*/
		if (c != null)
		{

			return c;

		}

		return super.getTitleHeaderControl (id);

	}

	@Override
    public String getViewerIcon ()
    {

        return Constants.EDIT_ICON_NAME;

    }

    public U getUserLanguageStrings ()
    {

        return this.userStrings;

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

        this.showMainSideBar ();

		this.setUILayout ();

    }

    public void showMainSideBar ()
    {

        this.showSideBar (this.getMainSideBarId ());

    }

    public String getMainSideBarId ()
    {

        return this.mainSideBar.getId ();

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

        final AbstractLanguageStringsEditor _this = this;

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

				//this.showImport ();

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

        final AbstractLanguageStringsEditor _this = this;

        am.put ("show-main",
				new ActionAdapter ()
				{

					@Override
					public void actionPerformed (ActionEvent ev)
					{

						_this.showMainCard ();

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

	}

    public void showFind (String text)
    {

        this.showSideBar ("find");

        this.finder.setFindText (text);

        this.finder.onShow ();

        this.setUILayout ();

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

    private LanguageStringsIdsPanel createIdsPanel (String id)
    {

        final AbstractLanguageStringsEditor _this = this;

        return new LanguageStringsIdsPanel (this,
                                            this.baseStrings.getNode (id),
                                            this.valuesCache.get (this.baseStrings.getNode (id)));

    }

}
