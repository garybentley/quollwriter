package com.quollwriter.ui;

import java.awt.*;
import java.io.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;

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

    public static final String INFO_HEADER_CONTROL_ID = "info";
    public static final String SUBMIT_HEADER_CONTROL_ID = "submit";

    public static int INTERNAL_SPLIT_PANE_DIVIDER_WIDTH = 2;

    private JSplitPane            splitPane = null;
    private Header                title = null;
    private Box                   notifications = null;
    private Box                   sideBar = null;
    private Box                   sideBarWrapper = null;
    private Box                   toolbarPanel = null;
    private AbstractSideBar       currentSideBar = null;
    private Map<String, AbstractSideBar> sideBars = new HashMap ();
    private Stack<AbstractSideBar>  activeSideBars = new Stack ();
    private java.util.List<SideBarListener> sideBarListeners = new ArrayList ();
    private java.util.List<MainPanelListener> mainPanelListeners = new ArrayList ();
	private JPanel                cards = null;
	private CardLayout            cardsLayout = null;
	private JLabel                importError = null;
    private ImportTransferHandlerOverlay   importOverlay = null;
    private Map<String, IdsPanel> panels = new HashMap ();
	private String currentCard = null;
    private LanguageStrings userStrings = null;
    private String toolbarLocation = null;
    private Map<String, JTree> sectionTrees = new LinkedHashMap<> ();

    public LanguageStringsEditor (LanguageStrings userStrings)
			               throws Exception
    {

		final LanguageStringsEditor _this = this;

        if (userStrings == null)
        {

            throw new IllegalArgumentException ("No strings provided.");

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

        String defSection = "General";

        // Split the defaults into sections.
        /*
        Map<String, Set<String>> sections = new HashMap<> ();

        Map<String, Object> defStrings = Environment.getDefaultUILanguageStrings ().getStrings ();

        for (String l : defStrings.keySet ())
        {

            Object o = defStrings.get (l);

            if (o instanceof String)
            {

                Set<String> sect = sections.get (defSection);

                if (sect == null)
                {

                    sect = new TreeSet<String> ();

                    sections.put (defSection,
                                  sect);

                }

                sect.add (l);

            }

            if (o instanceof Map)
            {

                Map m = (Map) o;

                String s = (String) m.get (":section");

                if (s == null)
                {

                    s = defSection;

                }

                Set<String> sect = sections.get (s);

                if (sect == null)
                {

                    sect = new TreeSet<String> ();

                    sections.put (s,
                                  sect);

                }

                sect.add (l);

            }

        }
*/
        Map<String, Set<LanguageStrings.Node>> sections = Environment.getDefaultUILanguageStrings ().getNodesInSections (defSection);

        java.util.List<AccordionItem> items = new ArrayList<> ();

        items.add (this.createSectionTree ("Basics",
                                           Constants.SETTINGS_ICON_NAME,
                                           sections.get ("Basics")));
        items.add (this.createSectionTree (defSection,
                                           "tools",
                                           sections.get (defSection)));
        items.add (this.createSectionTree ("Project",
                                           Project.OBJECT_TYPE,
                                           sections.get ("Project")));
        items.add (this.createSectionTree ("Editors",
                                           Constants.EDITORS_ICON_NAME,
                                           sections.get ("Editors")));

        // Create the sidebar.
        this.currentSideBar = new AccordionItemsSideBar (this,
                                                         items)
        {

            @Override
            public void panelShown (MainPanelEvent ev)
            {

                _this.setIdSelectedInSidebar (ev.getPanel ().getPanelId ());

            }

        };

        this.addMainPanelListener (this.currentSideBar);

        this.currentSideBar.init (null);

        //this.currentSideBar.setBorder (UIUtils.createPadding (0, 5, 5, 0));

        this.sideBarWrapper = new Box (BoxLayout.Y_AXIS);
        this.toolbarPanel = new Box (BoxLayout.Y_AXIS);
        this.sideBarWrapper.setAlignmentX (LEFT_ALIGNMENT);
        this.toolbarPanel.setAlignmentX (LEFT_ALIGNMENT);

        this.sideBar = new Box (BoxLayout.Y_AXIS);
        this.sideBar.add (this.sideBarWrapper);
        this.sideBar.add (this.toolbarPanel);

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

		this.setMinimumSize (new Dimension (500, 500));

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

    }

    private void save ()
                throws Exception
    {

        // Cycle over all the id boxes and set the values if present.
        for (IdsPanel p : this.panels.values ())
        {

            p.saveValues ();

        }

        File f = Environment.getUILanguageStringsFile (this.userStrings.getId ());

        String json = JSONEncoder.encode (this.userStrings.getAsJSON ());

        IOUtils.writeStringToFile (f,
                                   json,
                                   false);

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

    private void updateTitle ()
    {

        this.setViewerTitle ("Edit Language Strings for: " + this.userStrings.getNativeName ());

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

        this.toolbarLocation = loc;

    }

    private AccordionItem createSectionTree (String                    title,
                                             String                    iconType,
                                             Set<LanguageStrings.Node> sections)
    {

        final LanguageStringsEditor _this = this;

        final JTree tree = this.createStringsTree (sections);

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

                LanguageStrings.Node n = (LanguageStrings.Node) tn.getUserObject ();

                String id = n.getNodeId ();

                // See if there are any errors for this id.
                int errCount = _this.userStrings.getErrors (id).size ();

                String name = null;

                if (errCount > 0)
                {

                    name = String.format ("%s (%s/%s)",
                                          id,
                                          Environment.formatNumber (errCount),
                                          Environment.formatNumber (n.getAllValues ().size ()));

                    this.setForeground (Color.red);

                } else {

                    name = String.format ("%s (%s)",
                                          id,
                                          Environment.formatNumber (n.getAllValues ().size ()));

                }

                this.setText (name);

                this.setBorder (new EmptyBorder (2, 2, 2, 2));
                this.setIcon (null);

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
                                                            Environment.getDefaultUILanguageStrings ().createNode (id));

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
/*
            if (!this.strings.isEnglish ())
            {

                Object o = strs.get (k);

                if (o instanceof Map)
                {

                    Map m = (Map) o;

                    if (m.containsKey (":englishonly"))
                    {

                        continue;

                    }

                }

            }
*/
            root.add (new DefaultMutableTreeNode (k));

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

		this.setTitleHeaderControlsVisible (true);

		this.showViewer ();

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

		// Save the width/height of the split pane.

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
        ids.add (INFO_HEADER_CONTROL_ID);
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

        if (id.equals (INFO_HEADER_CONTROL_ID))
        {

            c = UIUtils.createButton (Constants.INFO_ICON_NAME,
                                      Constants.ICON_TITLE_ACTION,
                                      "Click to view/edit the information about these strings",
                                      new ActionListener ()
                                      {

                                        @Override
                                        public void actionPerformed (ActionEvent ev)
                                        {

                                            _this.showInfo ();

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

                                            _this.submit ();

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

        // Check for errors.
        if (this.userStrings.getEmail () == null)
        {

            //showError = true;

        }

        Map<LanguageStrings.Value, Set<String>> errors = this.userStrings.getErrors ();

    }

    private void showInfo ()
    {

        final LanguageStringsEditor _this = this;

        final String popupName = "stringsinfo";
        QPopup popup = this.getNamedPopup (popupName);

        if (popup == null)
        {

            popup = UIUtils.createClosablePopup ("Edit the Information",
                                                 Environment.getIcon (Constants.EDIT_ICON_NAME,
                                                                      Constants.ICON_POPUP),
                                                 null);

            final QPopup qp = popup;

            Box content = new Box (BoxLayout.Y_AXIS);

            JTextPane help = UIUtils.createHelpTextPane ("Use the form below to describe your set of strings.  All the values are required.<br /><br />When you are ready to submit the strings use the submission button on the header.",
                                                         this);

            help.setBorder (null);

            content.add (help);
            content.add (Box.createVerticalStrut (10));

            final JLabel error = UIUtils.createErrorLabel ("");
            error.setVisible (false);
            error.setBorder (UIUtils.createPadding (0, 5, 5, 5));

            content.add (error);

            Set<FormItem> items = new LinkedHashSet ();

            final TextFormItem lang = new TextFormItem ("English Name (i.e. Spanish, French, German)",
                                                         this.userStrings.getLanguageName ());

            items.add (lang);

            final TextFormItem nativelang = new TextFormItem ("Native Name (i.e. Espa\u00F1ol, Fran\u00E7ais, Deutsch)",
                                                              this.userStrings.getNativeName ());

            items.add (nativelang);

            final TextFormItem email = new TextFormItem ("Contact Email",
                                                         this.userStrings.getEmail ());

            items.add (email);

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

                    _this.userStrings.setEmail (em);
                    _this.userStrings.setLanguageName (l);
                    _this.userStrings.setNativeName (nl);

                    _this.updateTitle ();

                    qp.resize ();
                    qp.removeFromParent ();

                }

            };

            UIUtils.addDoActionOnReturnPressed (lang.getTextField (),
                                                saveAction);
            UIUtils.addDoActionOnReturnPressed (nativelang.getTextField (),
                                                saveAction);
            UIUtils.addDoActionOnReturnPressed (email.getTextField (),
                                                saveAction);

            JButton save = UIUtils.createButton ("Save",
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

	@Override
    public void fillSettingsPopup (JPopupMenu popup)
	{

        java.util.List<String> prefix = Arrays.asList (allprojects,settingsmenu,items);

		final LanguageStringsEditor _this = this;

		// Add?
        popup.add (this.createMenuItem (Environment.getUIString (prefix,
                                                                 LanguageStrings.newproject),
                                        //"New {Project}",
                                        Constants.NEW_ICON_NAME,
										new ActionListener ()
											 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

													UIUtils.showAddNewProject (_this,
																			   null,
																			   null);

												}

											 }));

        popup.add (this.createMenuItem (Environment.getUIString (prefix,
                                                                 importfile),
                                        //"Import File/{Project}",
                                        Constants.PROJECT_IMPORT_ICON_NAME,
                                        new ActionAdapter ()
                                        {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    _this.showImport ();

                                                }

                                            }));

		popup.addSeparator ();

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

		// Close and remove all sidebars.
        for (AbstractSideBar sb : new ArrayList<AbstractSideBar> (this.activeSideBars))
        {

            this.removeSideBar (sb);

        }
/*
		super.close (true,
					 null);

		Environment.closeDown ();
*/
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

	}

    @Override
    public void initKeyMappings (InputMap im)
    {

        super.initKeyMappings (im);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE,
                                        0),
                "show-main");

	}

    private IdsPanel createIdsPanel (String id)
    {

        return new IdsPanel (this,
                             id,
                             null);

    }

    public class IdsPanel extends BasicQuollPanel<LanguageStringsEditor>
    {

        private String parentId = null;
        private Set<LanguageStrings.Value> vals = null;
        private LanguageStrings.Node node = null;
        //private Object values = null;
        private Box content = null;
        private LanguageStringsEditor editor = null;

        public IdsPanel (LanguageStringsEditor ed,
                         String                id,
                         Object                values)
        {

            super (ed,
                   id,
                   null);

            this.editor = ed;

            this.setTitle (String.format ("%s (%s)",
                                          id,
                                          Environment.formatNumber (Environment.getDefaultUILanguageStrings ().getNode (id).getAllValues ().size ())));
                                          //getStringsCount (id))));

            this.parentId = id;
            //this.vals = Environment.getDefaultUILanguageStrings ().getStrings ().getAllValues (id);
            this.node = Environment.getDefaultUILanguageStrings ().getNode (id);
            //this.values = values;

            this.content = new ScrollableBox (BoxLayout.Y_AXIS);
            this.content.setAlignmentY (Component.TOP_ALIGNMENT);
            this.content.setAlignmentX (Component.LEFT_ALIGNMENT);
            //this.content.setMaximumSize (new Dimension (Short.MAX_VALUE, Short.MAX_VALUE));

        }

        @Override
        public String getPanelId ()
        {

            return this.parentId;

        }

        @Override
        public boolean isWrapContentInScrollPane ()
        {

            return true;

        }

        @Override
        public JComponent getContent ()
        {

            this.buildForm (this.parentId);
            /*
                            this.vals
                            this.values);
*/
            this.content.add (Box.createVerticalGlue ());

            return this.content;
/*
            Set<FormItem> items = new LinkedHashSet<> ();

            this.buildForm (items,
                            this.parentId,
                            null,
                            this.ids,
                            this.values);

            this.form = new Form (Form.Layout.stacked,
                                  items);

            this.form.setBorder (UIUtils.createPadding (5, 10, 5, 10));

            return this.form;
*/
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
            if (this.node.getComment () != null)
            {

                this.createComment (this.node.getComment ());

            }

            for (LanguageStrings.Value v : this.node.getAllValues ())
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
        private JLabel previewLabel = null;
        private JTextPane errors = null;
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

            Header h = UIUtils.createHeader (this.baseValue.getId (),
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
            l.setAlignmentX (Component.LEFT_ALIGNMENT);
            //l.setMinimumSize (new Dimension (200, 20));

            pb.add (l,
                    cc.xy (3, r));

            r += 2;

            pb.addLabel ("<html><i>Your Value</i></html>",
                         cc.xy (1, r));

            this.userValue = new TextArea (null,
                                           3,
                                           -1)
            {

                @Override
                public void fillPopupMenuForExtraItems (MouseEvent ev,
                                                        JPopupMenu popup,
                                                        boolean    compress)
                {

                    if (compress)
                    {

                        java.util.List<JComponent> buts = new java.util.ArrayList ();

                        buts.add (UIUtils.createButton ("eye",
                                                        Constants.ICON_MENU,
                                                        "Preview your value",
                                                        new ActionListener ()
                                                        {

                                                             public void actionPerformed (ActionEvent ev)
                                                             {

                                                                 _this.showPreview ();

                                                             }

                                                        }));

                        popup.add (UIUtils.createPopupMenuButtonBar ("Manage",
                                                                     //"Edit",
                                                                     popup,
                                                                     buts));

                    } else {

                        JMenuItem mi = null;

                        mi = UIUtils.createMenuItem ("Preview your value",
                                                     "eye",
                                                     new ActionListener ()
                                                     {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            _this.showPreview ();

                                                        }

                                                     });
                        mi.setMnemonic (KeyEvent.VK_P);
                        popup.add (mi);

                    }

                }

            };

            //this.userValue.setBorder (UIUtils.createLineBorder ());

            try
            {

                this.userValue.setDictionaryProvider (new UserDictionaryProvider ("English")
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

                                Id id = _this.getIdAtOffset (offset);

                                if (id != null)
                                {
System.out.println ("V: " + id.isIdValid () + ", " + id.fullId);
                                    return id.isIdValid ();

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

                                return null;

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
System.out.println ("FFFID: " + id.fullId);
                        _this.userValue.getEditor ().setSelectionStart (id.getPart (c).start);
                        _this.userValue.getEditor ().setSelectionEnd (id.getPart (c).end);

                    } else {

                        System.out.println ("CALLED");

                        defSelect.actionPerformed (ev);

                    }

                }

            });

            if (stringsValue != null)
            {

                this.userValue.setText (stringsValue.getRawText ());

            }

            this.userValue.setAutoGrabFocus (false);

            InputMap im = this.userValue.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);
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

                    if (ev.getKeyCode () == KeyEvent.VK_ENTER)
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

                        int c = _this.userValue.getEditor ().getCaretPosition ();

                        Id id = new Id (_this.userValue.getEditor ().getText (),
                                              c);
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

                        if (_this.selector.isVisible ())
                        {

                            ev.consume ();

                            _this.updateSelectedMatch (-1);

                            return;

                        }

                    }

                    if (ev.getKeyCode () == KeyEvent.VK_DOWN)
                    {

                        if (_this.selector.isVisible ())
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

                    Id id = new Id (t,
                                    c);

                    if (id.getFullId () == null)
                    {

                        return;

                    }
System.out.println ("XFID: " + id.getFullId ());
                    Set<String> matches = id.getPartMatches (c);

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

                        Id.Part p = id.getPart (c);

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

                        Id.Part pa = id.getPart (c);

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

            JButton insB = UIUtils.createToolBarButton (Constants.DOWN_ICON_NAME,
                                                        "Insert a reference to another string",
                                                        "insert",
                                                        new ActionListener ()
           {

               @Override
               public void actionPerformed (ActionEvent ev)
               {

                   //_this.userValue.setText (defValue);

               }

           });

            JButton useB = UIUtils.createToolBarButton (Constants.COPY_ICON_NAME,
                                                        "Use the English value",
                                                        "use",
                                                        new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    _this.userValue.setText (baseValue.getRawText ());//defValue);
                    _this.validate ();
                    _this.repaint ();

                }

            });

            JButton infB = UIUtils.createToolBarButton (Constants.FIND_ICON_NAME,
                                                        "Show the strings that use this one, i.e. those affected by any changes to this string",
                                                        "info",
                                                        new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    //_this.userValue.setText (defValue);

                }

            });

            JButton preB = UIUtils.createToolBarButton ("eye",
                                                        "Preview your string",
                                                        "preview",
                                                        new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    _this.showPreview ();

                }

            });

            java.util.List<JButton> buts = Arrays.asList (insB, useB, infB, preB);

            JToolBar bBar = UIUtils.createButtonBar (buts);

            pb.add (this.userValue,
                    cc.xy (3, r));

            r += 2;

            this.errors = UIUtils.createHelpTextPane ("",
                                                      this.editor);
            this.errors.setVisible (false);
            this.errors.setBorder (UIUtils.createPadding (6, 0, 0, 0));

            this.errorsLabel = UIUtils.createErrorLabel ("Errors");
            this.errorsLabel.setBorder (UIUtils.createPadding (6, 0, 0, 0));
            this.errorsLabel.setVisible (false);
            this.errorsLabel.setIcon (null);

            pb.add (this.errorsLabel,
                    cc.xy (1, r));
            pb.add (this.errors,
                    cc.xy (3, r));

            r += 1;

            this.preview = UIUtils.createHelpTextPane ("",
                                                       this.editor);
            this.preview.setVisible (false);
            this.preview.setBorder (UIUtils.createPadding (6, 0, 0, 0));

            this.previewLabel = UIUtils.createInformationLabel ("Preview");
            this.previewLabel.setBorder (UIUtils.createPadding (6, 0, 0, 0));
            this.previewLabel.setVisible (false);

            pb.add (this.previewLabel,
                    cc.xy (1, r));
            pb.add (this.preview,
                    cc.xy (3, r));

            r += 1;

            pb.add (bBar,
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

                    this.stringsValue.setRawText (uv);

                }

            }

        }

        public Id getIdAtOffset (int offset)
        {

            Id id = new Id (this.userValue.getEditor ().getText (),
                            offset);

            if (id.fullId == null)
            {

                return null;

            }

            return id;

        }

        public Id getIdAtCaret ()
        {

            return this.getIdAtOffset (this.userValue.getEditor ().getCaretPosition ());

        }

        public String getId ()
        {

            return this.baseValue.getId ();

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

        public void showPreview ()
        {

            String s = this.getUserValue ();

            if (s == null)
            {

                UIUtils.showErrorMessage (this.editor,
                                          "No value provided for: " + this.baseValue.getId ());

                return;

            }

            this.errorsLabel.setVisible (false);
            this.errors.setVisible (false);

            Set<String> errs = LanguageStrings.getErrors (s,
                                                          this.baseValue.getId (),
                                                          this.baseValue.getSCount (),
                                                          this.editor);

            if (errs.size () > 0)
            {

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
                this.errors.setVisible (true);

                return;

            }

            String t = this.editor.getPreviewText (s);

            this.previewLabel.setVisible (true);
            this.preview.setText (t);
            this.preview.setVisible (true);

            this.validate ();
            this.repaint ();
/*
            UIUtils.showMessage ((PopupsSupported) this.editor,
                                 this.value.getId () + " - Preview",
                                 t);
*/
        }

        private void fillMatch ()
        {

            QTextEditor editor = this.userValue.getEditor ();

            int c = editor.getCaretPosition ();

            Id id = new Id (editor.getText (),
                            c);

            String m = this.selections.getSelectedValue ();

            //m = id.getNewFullId (m);

            Id.Part part = id.getPart (c);

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
System.out.println ("APPENDING: " + m);
                    editor.replaceText (c, c, m);
                    editor.setCaretPosition (c + m.length ());

                } else {

                    part = id.getLastPart ();
System.out.println ("LAST: " + m);
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
            if (Environment.getDefaultUILanguageStrings ().getString (id.fullId) != null)
            {

                if (!id.hasClosingBrace)
                {

                    editor.replaceText (id.getEnd () + 1, id.getEnd () + 1, "}");
                    editor.setCaretPosition (c + 1);

                }

                this.hideSelector ();

                return;

            }

System.out.println ("FID: " + id.fullId);
            // Check to see if there are more matches further down the tree.
            String nid = id.fullId + ".";

            Set<String> matches = Environment.getDefaultUILanguageStrings ().getIdMatches (nid);

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
                                                //this.selector.getPreferredSize ().height));

            this.editor.showPopupAt (this.selector,
                                     SwingUtilities.convertPoint (this.userValue,
                                                                  point,
                                                                  this.editor),
                                     false);

        }

        @Override
        public Dimension getMaximumSize ()
        {

            return new Dimension (Short.MAX_VALUE,//super.getMaximumSize ().width,
                                  this.getPreferredSize ().height);

        }

        public class Id
        {

            private int _start = -1;
            private String fullId = null;
            private boolean hasClosingBrace = false;
            //private java.util.List<String> parts = null;
            //public String currentPart = null;
            //public int currentPartStart = -1;
            //public int currentPartEnd = -1;
            private java.util.List<Part> parts = new ArrayList<> ();
            public boolean hasErrors = false;

            public Id (String text,
                       int    offset)
            {

                if (text.length () < 3)
                {

                    return;

                }

                String idstart = "${";

                int ind = text.lastIndexOf (idstart, offset);

                if (ind > -1)
                {

                    ind += idstart.length ();
                    this._start = ind;

                    int idendind = text.indexOf ("}", ind);

                    if (idendind > -1)
                    {

                        this.hasClosingBrace = true;

                    } else {

                        idendind = text.indexOf (" ", ind);

                        if (idendind < 0)
                        {

                            idendind = text.indexOf ('\n', ind);

                        }

                    }

                    if (idendind < 0)
                    {

                        idendind = text.length ();

                    }

                    if (idendind < ind)
                    {

                        return;

                    }

                    if (!this.hasClosingBrace)
                    {

                        this.hasErrors = true;

                    }

                    this.fullId = text.substring (ind, idendind);

                    if (this.fullId.trim ().equals (""))
                    {

                        this.hasErrors = true;
                        this.fullId = null;

                        return;

                    }

                    if (!Character.isLetterOrDigit (this.fullId.charAt (0)))
                    {

                        this.hasErrors = true;

                    }

                    if (this.hasClosingBrace)
                    {

                        // Check to make sure there is no space between the id and the closing brace.
                        if (!Character.isLetterOrDigit (this.fullId.charAt (this.fullId.length () - 1)))
                        {

                            this.hasErrors = true;

                        }

                    }

                    int start = ind;

                    for (int i = start; i < idendind; i++)
                    {

                        if (Character.isLetterOrDigit (text.charAt (i)))
                        {

                            start = i;
                            break;

                        }

                    }

                    if (start > ind)
                    {

                        this.hasErrors = true;

                    }

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

            public boolean isIdValid ()
            {

                if (this.fullId == null)
                {

                    return false;

                }

                return Environment.getDefaultUILanguageStrings ().isIdValid (this.fullId);

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

            public Set<String> getPartMatches (int offset)
            {

                Part p = this.getPart (offset);

                if (p != null)
                {
System.out.println ("PP: " + p.getFullId ());
                    return Environment.getDefaultUILanguageStrings ().getIdMatches (p.getFullId ());

                }

                return this.getMatches ();

            }

            public Set<String> getMatches ()
            {
System.out.println ("FID: " + this.fullId);
                return Environment.getDefaultUILanguageStrings ().getIdMatches (this.fullId);

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

/*
            private int getIdPrefixLastPartEnd ()
            {

                int idPrefStart = this.getIdPrefixStart ();

                String idPref = this.getIdPrefix ();

                int lind = idPref.lastIndexOf (".");

                if (lind > 0)
                {

                    idPrefStart += lind;

                }

                return idPrefStart;

            }
*/
        }

    }

}
