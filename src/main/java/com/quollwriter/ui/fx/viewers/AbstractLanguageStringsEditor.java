package com.quollwriter.ui.fx.viewers;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.concurrent.*;
import java.text.*;
import java.net.*;

import javafx.collections.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import org.josql.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.text.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.achievements.ui.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.uistrings.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public abstract class AbstractLanguageStringsEditor<B extends AbstractLanguageStrings, U extends AbstractLanguageStrings> extends AbstractViewer implements RefValueProvider, PanelViewer
{

    public interface CommandId extends AbstractViewer.CommandId
    {

        String showMain = "showMain";
        String openproject = "openproject";
        String textproperties = "textproperties";

    }

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

    private WindowedContent windowedContent = null;

    private Header                title = null;
    private LanguageStringsSideBar mainSideBar = null;
    private FindSideBar                findSideBar = null;
	private StackPane                cards = null;
    protected Map<Id, LanguageStringsIdsPanel> panels = new HashMap<> ();
	private Id currentCard = null;
    ///private Finder                finder = null;
    protected U userStrings = null;
    protected B baseStrings = null;
    protected Filter<Node> nodeFilter = null;
    private Map<String, Set<Value>> valuesCache = null;
    private ObservableMap<Node, Number> errCounts = FXCollections.observableHashMap ();
    private ObservableMap<Node, Number> userCounts = FXCollections.observableHashMap ();
    private boolean inited = false;
    private boolean updatingPreviews = false;
    private ObjectProperty<Panel> currentPanelProp = null;
    private DictionaryProvider2 dictionaryProvider = null;

    public AbstractLanguageStringsEditor (U userStrings)
    {

        this (userStrings,
              (B) userStrings.getDerivedFrom ());

/*
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
*/

        this.initActionMappings ();

    }

    public AbstractLanguageStringsEditor (U userStrings,
                                          B baseStrings)
    {

        this.userStrings = userStrings;
        this.baseStrings = baseStrings;

        this.baseStrings = (B) this.userStrings.getDerivedFrom ();

        this.currentPanelProp = new SimpleObjectProperty<> ();

        this.initValuesCache ();

        this.mainSideBar = new LanguageStringsSideBar (this,
                                                       this.baseStrings);

        this.cards = new StackPane ();

        this.initActionMappings ();

    }

    private void initActionMappings ()
    {

        this.addActionMapping (() ->
        {

            // Just ignore...

        },
        CommandId.fullscreen);

        this.addActionMapping (() ->
        {

            // Just ignore...

        },
        CommandId.textproperties);

    }

    public U getUserStrings ()
    {

        return this.userStrings;

    }

    public B getBaseStrings ()
    {

        return this.baseStrings;

    }

    public abstract void tryOut ();

    public abstract void save ()
                        throws Exception;

    public abstract void submit (Runnable onSuccess,
                                 Runnable onFailure);

    public abstract void delete ()
                          throws Exception;

    public abstract void showReportProblemForId (String id);

    public abstract void onForwardLabelClicked ()
                                         throws Exception;

    @Override
    public ProjectFullScreenContent getFullScreenContent ()
    {

        return null;

    }

    public void showForwardLabel (StringProperty text)
    {

        this.mainSideBar.showForwardLabel (text);

    }

    public int getErrorCount (Node n)
    {

        int c = 0;

        // Get the card.
        Number num = this.errCounts.get (n);
/*
        if (num != null)
        {

            return num.intValue ();

        }
*/

        LanguageStringsIdsPanel panel = this.panels.get (new Id (n.getNodeId ()));

        if (panel != null)
        {

            c = panel.getErrorCount ();

        } else {

            for (Value nv : this.valuesCache.get (n.getNodeId ()))
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

    public int getUserValueCount (Node n)
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

            for (Value nv : this.valuesCache.get (n.getNodeId ()))
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

    public Set<Value> getValuesForNode (Node n)
    {

        return this.valuesCache.get (n.getNodeId ());

    }

    private void initValuesCache ()
    {

        this.valuesCache = new HashMap<> ();

        String defSection = "General";

        Map<String, Set<Node>> sections = this.baseStrings.getNodesInSections (defSection);

        for (Section sect : (Set<Section>) this.baseStrings.getSections ())
        {

            for (Node k : sections.get (sect.id))
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

                        this.valuesCache.put (n.getNodeId (), vals);

                    }

                    continue;

                }

                vals = k.getValues (this.nodeFilter);

                if (vals.size () == 0)
                {

                    continue;

                }

                this.valuesCache.put (k.getNodeId (), vals);

            }

        }

    }

    public void setNodeFilter (Filter<Node> filter)
    {

        this.nodeFilter = filter;

        // Re-init the values cache.
        this.initValuesCache ();

        this.initSideBar ();

    }

    private void initSideBar ()
    {

        if (this.baseStrings == null)
        {

            throw new IllegalStateException ("No base strings set.");

        }
/*
        String state = UserProperties.get ("sidebarState-" + this.mainSideBar.getSideBar ().getSideBarId ());

        if (state != null)
        {

            this.mainSideBar.init (new State (state));

        }
*/
    }

    public void showAllStrings ()
    {

        try
        {

            this.save ();

        } catch (Exception e) {

            Environment.logError ("Unable to save",
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             new SimpleStringProperty ("Unable to update view."));

            return;

        }

        // Clear out the panel cache.
        this.panels = new HashMap<> ();

        this.currentCard = null;
        this.cards.getChildren ().clear ();

        this.nodeFilter = null;
        this.initSideBar ();

    }

    public void updateSideBar (final Node n)
    {

        //this.errCounts.remove (n);
        this.userCounts.remove (n);

        this.userCounts.put (n,
                             this.getUserValueCount (n));

        this.errCounts.put (n,
                            this.getErrorCount (n));

    }

    public String getPreviewText (String t)
    {

        return BaseStrings.buildText (t,
                                      this);

    }

    public void updatePreviews ()
    {

        final AbstractLanguageStringsEditor<B, U> _this = this;

        UIUtils.runLater (() ->
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

        });

    }

    @Override
    public String getString (String id)
    {

        List<String> idparts = BaseStrings.getIdParts (id);

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

    public void showIds (Id id)
    {

        this.showIds (id,
                      null);

    }

    public void showIds (Id       id,
                         Runnable onShow)
    {

        if (!this.valuesCache.containsKey (id.getId ()))
        {

            return;

        }

        //String id = node.getNodeId ();
        LanguageStringsIdsPanel p = this.panels.get (id);

        if (p == null)
        {

            p = this.createIdsPanel (id);

            try
            {

                p.init (null);

            } catch (Exception e) {

                Environment.logError ("Unable to show ids for id: " +
                                      id,
                                      e);

                ComponentUtils.showErrorMessage (this,
                                                 "Unable to show panel.");

                return;

            }

            this.panels.put (id,
                             p);

            this.cards.getChildren ().add (p.getPanel ());

        }

        this.showCard (id);

        UIUtils.runLater (onShow);

    }

    public void showId (Id id)
    {

        final AbstractLanguageStringsEditor _this = this;
/*
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
*/
        this.showIds (id,
                      () ->
        {

            this.scrollToNode (id);

        });

    }

    public void scrollToNode (Id id)
    {

        LanguageStringsIdsPanel c = this.panels.get (this.currentCard);

        //c.scrollToNode (id);

    }

    public void showMainCard ()
    {

        this.showCard (new Id (0, MAIN_CARD, false));

    }

    public void showCard (Id id)
    {

        Panel p = this.panels.get (id).getPanel ();
        p.toFront ();

		this.currentCard = id;

        this.currentPanelProp.setValue (p);

    }

    public ObjectProperty<Panel> currentPanelProperty ()
    {

        return this.currentPanelProp;

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
    public void init (State s)
			   throws GeneralException
    {

		super.init (s);

        this.initSideBar ();

        super.setMainSideBar (this.mainSideBar.getSideBar ());

        final AbstractLanguageStringsEditor<B, U> _this = this;

        if (this.baseStrings != null)
        {

            this.initSideBar ();

        }

        //this.initSideBar ();
/*
        this.finder = new Finder (this);

        this.addSideBar (this.finder);
*/

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

        String lastId = UserProperties.get ("languagestringseditor-stringsid-lasteditingid-" + this.userStrings.getId ());

        if (lastId != null)
        {

            Id id = new Id (0, lastId, false);

            this.showIds (id);

        }

        float scroll = 0;

        try
        {

            scroll = UserProperties.getAsFloat ("languagestringseditor-stringsid-lasteditingscroll");

        } catch (Exception e) {

            // Ignore.

        }

        if (scroll > 0)
        {

            final float _scroll = scroll;

            UIUtils.runLater (() ->
            {

                //this.panels.get (_this.currentCard).getScrollPane ().getVerticalScrollBar ().setValue (_scroll);

            });

        }

        this.addKeyMapping (CommandId.find,
                            KeyCode.F1);
        this.addKeyMapping (CommandId.find,
                            KeyCode.F, KeyCombination.SHORTCUT_DOWN);
        this.addKeyMapping (CommandId.newproject,
                            KeyCode.N, KeyCombination.SHORTCUT_DOWN);
        this.addKeyMapping (CommandId.openproject,
                            KeyCode.O, KeyCombination.SHORTCUT_DOWN);
        this.addActionMapping (() ->
        {

            this.showFind ();

        },
        CommandId.find);

        this.addActionMapping (() ->
        {

            this.showMainCard ();

        },
        CommandId.showMain);

    }

    @Override
    public void removeSideBar (SideBar sb)
    {

        if (sb != null)
        {

            try
            {

                State ss = sb.getState ();

                UserProperties.set ("sidebarState-" + id,
                                    ss.asString ());

            } catch (Exception e) {

                Environment.logError ("Unable to save state for sidebar: " +
                                      id,
                                      e);

            }

        }

        super.removeSideBar (sb);

    }

    @Override
    public Supplier<Set<javafx.scene.Node>> getTitleHeaderControlsSupplier ()
    {

        return () ->
        {

            Set<javafx.scene.Node> controls = new LinkedHashSet<> ();

            controls.add (this.getTitleHeaderControl (HeaderControl.submit));
            controls.add (this.getTitleHeaderControl (HeaderControl.find));
            controls.add (this.getTitleHeaderControl (HeaderControl.tryout));
            controls.add (this.getTitleHeaderControl (HeaderControl.help));

            return controls;

        };

    }

	@Override
    public javafx.scene.Node getTitleHeaderControl (HeaderControl control)
	{

		if (control == null)
		{

			return null;

		}

        if (control == HeaderControl.submit)
        {

            return QuollButton.builder ()
                .tooltip ("Click to submit the strings")
                .iconName (StyleClassNames.SUBMIT)
                .buttonId ("submitstrings")
                .onAction (ev ->
                {

                    this.submit (null,
                                 null);

                })
                .build ();

        }

        if (control == HeaderControl.tryout)
        {

            return QuollButton.builder ()
                .tooltip ("Click to try out your strings")
                .iconName (StyleClassNames.TRYOUT)
                .buttonId ("tryoutstrings")
                .onAction (ev ->
                {

                    this.tryOut ();

                })
                .build ();

        }

		return super.getTitleHeaderControl (control);

	}

    public U getUserLanguageStrings ()
    {

        return this.userStrings;

    }

	@Override
	public void showOptions (String section)
	{

        throw new UnsupportedOperationException ("Not supported.");

	}

	@Override
    public void close (Runnable afterClose)
    {

        this.close (true,
                    afterClose);

    }

    public void close (boolean save,
                       Runnable afterClose)
	{

        if (save)
        {

            try
            {

                this.save ();

            } catch (Exception e) {

                Environment.logError ("Unable to save language strings: " +
                                      this.userStrings,
                                      e);

                ComponentUtils.showErrorMessage (this,
                                                 "Unable to save language strings.");

                return;

            }

        }

        try
        {

            State s = super.getState ();
            UserProperties.set ("languagestringeditor",
                                s.asString ());

            UserProperties.set ("languagestringseditor-sidebar",
                                this.mainSideBar.getState ().asString ());

        } catch (Exception e) {

            Environment.logError ("Unable to set state",
                                  e);

        }

        if (this.currentCard != null)
        {

            UserProperties.set ("languagestringseditor-stringsid-lasteditingid-" + this.userStrings.getId (),
                                this.currentCard.getId ());

        }

		super.close (afterClose);

	}

    @Override
    public void handleURLAction (String     v,
                                 MouseEvent ev)
    {

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

		super.handleURLAction (v,
                               ev);

	}

    public void showFind (String v)
    {

        if (this.findSideBar == null)
        {

            this.findSideBar = new FindSideBar (this);

            this.addSideBar (this.findSideBar);

        }

        this.showSideBar (this.findSideBar.getSideBar ().getSideBarId ());

        this.findSideBar.find (v);

    }

    public void showFind ()
    {

        this.showFind (null);

    }

    private LanguageStringsIdsPanel createIdsPanel (Id id)
    {

        final AbstractLanguageStringsEditor _this = this;

        return new LanguageStringsIdsPanel (this,
                                            this.baseStrings.getNode (id),
                                            this.valuesCache.get (id.getId ()));

    }

    @Override
    public WindowedContent getWindowedContent ()
    {

        if (this.windowedContent == null)
        {

            Supplier<Set<javafx.scene.Node>> hcsupp = this.getTitleHeaderControlsSupplier ();

            Set<javafx.scene.Node> headerCons = new LinkedHashSet<> ();

            if (hcsupp != null)
            {

                headerCons.addAll (hcsupp.get ());

            }

            this.windowedContent = new WindowedContent (this,
                                                        this.getStyleClassName (),
                                                        StyleClassNames.EDIT,
                                                        headerCons,
                                                        this.cards);

            this.windowedContent.setTitle (this.titleProperty ());

            this.windowedContent.getHeader ().getControls ().setVisibleItems (UserProperties.languageStringsEditorHeaderControlButtonIds ());

            this.windowedContent.getHeader ().getControls ().setOnConfigurePopupClosed (ev ->
            {

                UserProperties.setLanguageStringsEditorHeaderControlButtonIds (this.windowedContent.getHeader ().getControls ().getVisibleItemIds ());

            });

        }

        return this.windowedContent;

    }

    public FindSideBar getFindSideBar ()
    {

        return this.findSideBar;

    }

    protected void showMessage (String title,
                                String message)
    {

        QuollPopup.messageBuilder ()
            .inViewer (this.isClosed () ? null : this)
            .title (new SimpleStringProperty (title))
            .message (new SimpleStringProperty (message))
            .closeButton ()
            .build ()
            .show ();

    }

    @Override
    public void deleteAllObjectsForType (UserConfigurableObjectType t)
                                  throws GeneralException
    {

        // Do nothing.

    }

    @Override
    public Panel getCurrentPanel ()
    {

        return this.currentPanelProp.getValue ();

    }

    protected QuollPopup showProgressPopup (String title,
                                            String headerIconClassName,
                                            String message)
    {

        VBox b = new VBox ();
        b.getChildren ().add (QuollTextView.builder ()
            .text (new SimpleStringProperty (message))
            .build ());
        ProgressBar pb = new ProgressBar ();
        pb.setProgress (ProgressBar.INDETERMINATE_PROGRESS);

        QuollPopup qp = QuollPopup.messageBuilder ()
            .message (b)
            .headerIconClassName (headerIconClassName)
            .title (new SimpleStringProperty (title))
            .inViewer (this)
            .removeOnClose (true)
            .hideOnEscape (true)
            .build ();

        qp.getProperties ().put ("progress-bar",
                                 pb);

        qp.show ();

        return qp;

    }

    @Override
    public Set<FindResultsBox> findText (String t)
    {

        Set<FindResultsBox> res = new LinkedHashSet<> ();

        Map<String, Section> sects = new HashMap<> ();

        for (Section sect : (Set<Section>) this.baseStrings.getSections ())
        {

            sects.put (sect.id,
                       sect);

        }

        Set<Value> results = this.baseStrings.find (t);

        Set<Value> uresults = this.userStrings.find (t);

        for (Value v : uresults)
        {

            results.add (this.baseStrings.getValue (v.getId ()));

        }

        Map<Section, Map<Node, List<Value>>> vals = new HashMap<> ();

        for (Value v : results)
        {

            Node r = v.getRoot ();

            Section s = sects.get (r.getSection ());

            Map<Node, List<Value>> svs = vals.get (s);

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

                    Node tlnn = this.baseStrings.getNode (tlnid);

                    if (BaseStrings.toId (v.getId ()).startsWith (tlnid))
                    {

                        List<Value> l = svs.get (tlnn);

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

                List<Value> l = svs.get (r);

                if (l == null)
                {

                    l = new ArrayList<> ();

                    svs.put (r,
                             l);

                }

                l.add (v);

            }

        }

        res.add (new LanguageStringsResultsBox (this,
                                                vals));

        return res;

    }

    public ObservableMap<Node, Number> errorCountsProperty ()
    {

        return this.errCounts;

    }

    public ObservableMap<Node, Number> userCountsProperty ()
    {

        return this.userCounts;

    }

}
