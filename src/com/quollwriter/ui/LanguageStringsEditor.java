package com.quollwriter.ui;

import java.awt.*;
import java.io.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;

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

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class LanguageStringsEditor extends AbstractViewer
{

	public static final int DEFAULT_WINDOW_WIDTH = 800;
	public static final int DEFAULT_WINDOW_HEIGHT = 500;
	public static final int PROJECT_BOX_WIDTH = 250;

	public static final String MAIN_CARD = "main";
	public static final String OPTIONS_CARD = "options";

    public static final String INFO_HEADER_CONTROL_ID = "info";

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
	private JPanel                cards = null;
	private CardLayout            cardsLayout = null;
	private JLabel                importError = null;
    private ImportTransferHandlerOverlay   importOverlay = null;
    private Map<String, IdsPanel> panels = new HashMap ();
	private String currentCard = null;
    private LanguageStrings strings = null;
    private String toolbarLocation = null;

    public LanguageStringsEditor (LanguageStrings strings)
			               throws Exception
    {

		final LanguageStringsEditor _this = this;

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

        this.strings = this.strings;

        this.strings = new LanguageStrings ();
        this.strings.setNativeName ("English");
        this.strings.setLanguageName (Constants.ENGLISH);
        this.strings.setParent (Environment.getDefaultUILanguageStrings ());

        this.setViewerTitle ("Edit Language Strings for: " + this.strings.getNativeName ());

        String defSection = "General";

        // Split the defaults into sections.
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
                                                         items);

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

    private AccordionItem createSectionTree (String      title,
                                             String      iconType,
                                             Set<String> sections)
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

                String id = value.toString ();

                String name = String.format ("%s (%s)",
                                             id,
                                             Environment.formatNumber (_this.getStringsCount (id)));

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

                    String id = n.getUserObject ().toString ();

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

        return acc;

    }

    private int getStringsCount (String id)
    {

        Object o = Environment.getDefaultUILanguageStrings ().getStrings ().get (id);

        return this.getStringsCount (o);

    }

    private boolean isIdCountable (String id)
    {

        return !((id.startsWith ("_"))
                 ||
                 (id.startsWith (":"))
                );

    }

    private int getStringsCount (Object o)
    {

        if (o instanceof String)
        {

            if (this.isIdCountable ((String) o))
            {

                return 1;

            }

        }

        if (o instanceof Collection)
        {

            int c = 0;

            Collection col = (Collection) o;

            for (Object co : col)
            {

                c+= this.getStringsCount (co);

            }

            return c;

        }

        if (o instanceof Map)
        {

            int c = 0;

            Map m = (Map) o;

            Iterator iter = m.keySet ().iterator ();

            while (iter.hasNext ())
            {

                String k = (String) iter.next ();

                if (!this.isIdCountable (k))
                {

                    continue;

                }

                Object v = m.get (k);

                c += this.getStringsCount (v);

            }

            return c;

        }

        return 0;

    }

    private JTree createStringsTree (Set<String> sections)
    {

        final LanguageStringsEditor _this = this;

        DefaultMutableTreeNode root = new DefaultMutableTreeNode ("_strings");

        for (String k : sections)
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
            if (!this.isIdCountable (k))
            {

                continue;

            }

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

                                            //_this.showWarmupPromptSelect ();

                                        }

                                      });

        }

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
                             //this.strings.get (id));

    }

    public class IdsPanel extends BasicQuollPanel<LanguageStringsEditor>
    {

        private String parentId = null;
        private Object ids = null;
        private Object values = null;
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
                                          Environment.formatNumber (ed.getStringsCount (id))));

            this.parentId = id;
            this.ids = Environment.getDefaultUILanguageStrings ().getStrings ().get (id);
            this.values = values;

            this.content = new ScrollableBox (BoxLayout.Y_AXIS);
            this.content.setAlignmentY (Component.TOP_ALIGNMENT);
            this.content.setAlignmentX (Component.LEFT_ALIGNMENT);
            //this.content.setMaximumSize (new Dimension (Short.MAX_VALUE, Short.MAX_VALUE));

        }

        @Override
        public String getPanelId ()
        {

            return "ids-" + this.parentId;

        }

        @Override
        public boolean isWrapContentInScrollPane ()
        {

            return true;

        }

        @Override
        public JComponent getContent ()
        {

            this.buildForm (this.parentId,
                            this.ids,
                            this.values);

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

        private void createComment (String comment)
        {

            JComponent c = UIUtils.createLabel (comment);
            c.setAlignmentX (LEFT_ALIGNMENT);
            c.setBorder (UIUtils.createPadding (0, 15, 5, 5));

            this.content.add (c);

        }

        private void addComment (Object        ids,
                                 String        forId)
        {

            if (ids instanceof Map)
            {

                Map m = (Map) ids;

                Object v = m.get (":comment");

                if (v != null)
                {

                    this.createComment (v.toString ());

                }

            }

        }

        private void buildForm (String        idPrefix,
                                Object        ids,
                                Object        values)
        {

            // Check for the section comment.
            this.addComment (ids,
                             null);

            if (ids instanceof Map)
            {

                Map m = (Map) ids;

                Iterator iter = m.keySet ().iterator ();

                while (iter.hasNext ())
                {

                    String k = iter.next ().toString ();

                    if (!(this.viewer.isIdCountable (k)))
                    {

                        continue;

                    }

                    Object v = m.get (k);

                    String _idPrefix = ((idPrefix != null) ? idPrefix + "." : "") + k;

                    if (v instanceof Map)
                    {

                        this.buildForm (_idPrefix,
                                        v,
                                        null);

                    }

                    if (v instanceof String)
                    {

                        this.content.add (new IdBox (_idPrefix, // full id
                                                     (String) m.get (":comment." + k),
                                                     null, // value
                                                     (String) v, // defValue
                                                     (Number) m.get (":scount." + k),
                                                     this.editor)); // scount

                    }

                }

            }

        }

    }

    private class IdBox extends Box
    {

        private String id = null;
        private TextArea userValue = null;
        private LanguageStringsEditor editor = null;

        public IdBox (final String id,
                      final String comment,
                      final String value,
                      final String defValue,
                      final Number scount,
                      LanguageStringsEditor editor)
        {

            super (BoxLayout.Y_AXIS);

            final IdBox _this = this;

            this.editor = editor;
            this.id = id;

            Header h = UIUtils.createHeader (id,
                                             Constants.SUB_PANEL_TITLE);

            h.setBorder (UIUtils.createBottomLineWithPadding (0, 0, 3, 0));
            h.setAlignmentX (Component.LEFT_ALIGNMENT);

            this.add (h);

            Box b = new Box (BoxLayout.Y_AXIS);
            FormLayout   fl = new FormLayout ("right:60px, 5px, min(150px;p):grow",
                                              "top:p, 6px, top:p" + (comment != null ? ", 6px, top:p" : ""));
            PanelBuilder pb = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            int r = 1;

            if (comment != null)
            {

                pb.addLabel ("<html><i>Comment</i></html>",
                             cc.xy (1, r));

                String c = "";

                if (scount != null)
                {

                    for (int i = 0; i < scount.intValue (); i++)
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

            JTextArea l = new JTextArea (defValue);
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
                                           -1);

            JButton insB = UIUtils.createToolBarButton (Constants.DOWN_ICON_NAME,
                                                        "Insert a reference to another string",
                                                        "insert",
                                                        new ActionListener ()
           {

               @Override
               public void actionPerformed (ActionEvent ev)
               {

                   _this.userValue.setText (defValue);

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

                    _this.userValue.setText (defValue);

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

                    StringWithMarkup sm = _this.userValue.getEditor ().getTextWithMarkup ();

                    if (!sm.hasText ())
                    {

                        UIUtils.showErrorMessage (_this.editor,
                                                  "No value provided for: " + _this.id);

                        return;

                    }

                    UIUtils.showMessage ((PopupsSupported) _this.editor,
                                         _this.id + " - Preview",
                                         _this.editor.strings.doReplacements (sm.getMarkedUpText ()));

                }

            });

            java.util.List<JButton> buts = Arrays.asList (insB, useB, infB, preB);

            JToolBar bBar = UIUtils.createButtonBar (buts);

            Box uvBox = new Box (BoxLayout.X_AXIS);
            this.userValue.setAlignmentY (TOP_ALIGNMENT);
            this.userValue.setAlignmentX (LEFT_ALIGNMENT);
            bBar.setAlignmentY (TOP_ALIGNMENT);
            bBar.setAlignmentX (LEFT_ALIGNMENT);
            uvBox.add (this.userValue);
            uvBox.add (Box.createHorizontalStrut (5));
            uvBox.add (bBar);

            pb.add (uvBox,
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

        @Override
        public Dimension getMaximumSize ()
        {

            return new Dimension (Short.MAX_VALUE,//super.getMaximumSize ().width,
                                  this.getPreferredSize ().height);

        }

    }

}
