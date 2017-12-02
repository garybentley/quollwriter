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

import com.gentlyweb.properties.*;
import com.gentlyweb.utils.*;

import org.josql.*;

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

// TODO: Create a PopupFrame that supports popups.

public class Landing extends AbstractViewer implements ProjectInfoChangedListener
{

	public static final int DEFAULT_WINDOW_WIDTH = 800;
	public static final int DEFAULT_WINDOW_HEIGHT = 500;
	public static final int PROJECT_BOX_WIDTH = 250;

	public static final String MAIN_CARD = "main";
	public static final String OPTIONS_CARD = "options";
	public static final String STATS_CARD = "stats";
	public static final String NO_PROJECTS_CARD = "noprojects";
	public static final String ACHIEVEMENTS_CARD = "achievements";

    public static final String NEW_PROJECT_HEADER_CONTROL_ID = "newProject";
	public static final String IMPORT_PROJECT_HEADER_CONTROL_ID = "importProject";

	public static final String STATUS_TAG = "{s}";
	public static final String WORDS_TAG = "{wc}";
	public static final String CHAPTERS_TAG = "{ch}";
	public static final String LAST_EDITED_TAG = "{le}";
	public static final String EDIT_COMPLETE_TAG = "{ec}";
	public static final String READABILITY_TAG = "{r}";
	public static final String EDITOR_TAG = "{ed}";

    public static int INTERNAL_SPLIT_PANE_DIVIDER_WIDTH = 2;

    private JSplitPane            splitPane = null;
    private Header                title = null;
    private Box                   notifications = null;
    private BackgroundImagePanel backgroundPanel = null;
    private Object               backgroundObject = null;
    private QPopup               backgroundSelectorPopup = null;
    private Map<String, AbstractSideBar> sideBars = new HashMap ();
    private Stack<AbstractSideBar>  activeSideBars = new Stack ();
    private java.util.List<SideBarListener> sideBarListeners = new ArrayList ();
    private AbstractSideBar       currentSideBar = null;
	private JPanel                cards = null;
	private CardLayout            cardsLayout = null;
	private JLabel                importError = null;
    private ImportTransferHandlerOverlay   importOverlay = null;
	private Notification findProjectsNotification = null;
	private Options options = null;
	private StatisticsPanel statsPanel = null;
	private String currentCard = null;
	private PropertyChangedListener statusesChangedListener = null;

    public Landing ()
			 throws Exception
    {

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.allprojects);

		final Landing _this = this;

        // Create a split pane.
        this.splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
                                         false);
        this.splitPane.setDividerSize (0);//UIUtils.getSplitPaneDividerSize ());
        this.splitPane.setBorder (null);

        javax.swing.plaf.basic.BasicSplitPaneDivider div = ((javax.swing.plaf.basic.BasicSplitPaneUI) this.splitPane.getUI ()).getDivider ();
        div.setBorder (new MatteBorder (0, 0, 0, 1, Environment.getBorderColor ()));
        this.splitPane.setOpaque (false);
        this.splitPane.setBackground (UIUtils.getComponentColor ());

		this.cardsLayout = new CardLayout (0, 0);

		this.cards = new JPanel (this.cardsLayout);
        this.cards.setBackground (UIUtils.getComponentColor ());

		this.splitPane.setLeftComponent (this.cards);

		Box noprojs = new Box (BoxLayout.Y_AXIS);

        noprojs.add (Box.createVerticalStrut (20));

        Box bb = new Box (BoxLayout.X_AXIS);

        bb.add (Box.createHorizontalGlue ());

        Header h = UIUtils.createHeader (Environment.getUIString (prefix,
                                                                  LanguageStrings.noprojects),
                                            //"You currently have no {projects}",
                                         Constants.PANEL_TITLE);
        h.setMaximumSize (h.getPreferredSize ());
        h.setAlignmentX (Component.CENTER_ALIGNMENT);

		bb.add (h);

        bb.setAlignmentX (Component.CENTER_ALIGNMENT);

        noprojs.add (bb);

		noprojs.add (Box.createVerticalStrut (10));

        noprojs.add (Box.createVerticalGlue ());

		noprojs.setToolTipText (Environment.getUIString (prefix,
                                                         LanguageStrings.tooltip));
                            //Environment.replaceObjectNames ("Double click to add a new {project}."));

        noprojs.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handleDoublePress (MouseEvent ev)
            {

				UIUtils.showAddNewProject (_this,
										   null,
										   null);

            }

            @Override
            public void fillPopup (JPopupMenu p,
                                   MouseEvent ev)
            {

                java.util.List<String> prefix = new ArrayList<> ();
                prefix.add (LanguageStrings.allprojects);
                prefix.add (LanguageStrings.popupmenu);
                prefix.add (LanguageStrings.items);

				p.add (this.createMenuItem (Environment.getUIString (prefix,
                                                                     LanguageStrings._new),
                                            //"New {Project}",
											Constants.NEW_ICON_NAME,
											"newproject",
											null,
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

				p.add (this.createMenuItem (Environment.getUIString (prefix,
                                                                     LanguageStrings.importfile),
                                            //"Import file",
											Constants.PROJECT_IMPORT_ICON_NAME,
											"import",
											null,
											new ActionListener ()
											{

												@Override
												public void actionPerformed (ActionEvent ev)
												{

													_this.showImportProject ();

												}

											}));

				p.add (this.createMenuItem (Environment.getUIString (prefix,
                                                                     LanguageStrings.findprojects),
                                                                     //"Find your {Projects}",
											Constants.FIND_ICON_NAME,
											"findprojects",
											null,
											new ActionListener ()
											{

												@Override
												public void actionPerformed (ActionEvent ev)
												{

                                                    _this.showFindProjects ();

												}

											}));

            }

		});

		this.cards.add (noprojs, NO_PROJECTS_CARD);

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

        VerticalLayout layout = new VerticalLayout (7,
                                                    7,
                                                    PROJECT_BOX_WIDTH,
													VerticalLayout.LEFT);
													//(Environment.getAllProjectInfos ().size () > 2 ? VerticalLayout.LEFT : VerticalLayout.CENTER));

		this.backgroundPanel = new BackgroundImagePanel (new BorderLayout ());//layout);

        this.backgroundPanel.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.backgroundPanel.setBorder (UIUtils.createPadding (10, 10, 10, 10));

        this.backgroundPanel.setOpaque (false);

        final JScrollPane cscroll = new JScrollPane (this.backgroundPanel);

		this.cards.add (cscroll, MAIN_CARD);

        cscroll.setBorder (null);

        cscroll.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        cscroll.getVerticalScrollBar ().setUnitIncrement (30);
        cscroll.setOpaque (false);
        cscroll.setBackground (null);
		cscroll.getViewport ().setOpaque (false);

        this.backgroundPanel.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handleDoublePress (MouseEvent ev)
            {

				UIUtils.showAddNewProject (_this,
										   null,
										   null);

            }

            @Override
            public void fillPopup (JPopupMenu p,
                                   MouseEvent ev)
            {

                java.util.List<String> prefix = new ArrayList<> ();
                prefix.add (LanguageStrings.allprojects);
                prefix.add (LanguageStrings.popupmenu);
                prefix.add (LanguageStrings.items);

				p.add (this.createMenuItem (Environment.getUIString (prefix,
                                                                     LanguageStrings._new),
                                            //"New {Project}",
											Constants.NEW_ICON_NAME,
											"newproject",
											null,
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

				p.add (this.createMenuItem (Environment.getUIString (prefix,
                                                                     LanguageStrings.importfileorproject),
                                            //"Import File/{Project}",
											Constants.PROJECT_IMPORT_ICON_NAME,
											"import",
											null,
											new ActionListener ()
											{

												@Override
												public void actionPerformed (ActionEvent ev)
												{

													_this.showImportProject ();

												}

											}));

				p.add (this.createMenuItem (Environment.getUIString (prefix,
                                                                     LanguageStrings.findprojects),
                                            //"Find your {Projects}",
											Constants.FIND_ICON_NAME,
											"findprojects",
											null,
											new ActionListener ()
											{

												@Override
												public void actionPerformed (ActionEvent ev)
												{

                                                    _this.showFindProjects ();

												}

											}));

				p.addSeparator ();

				JMenu m = new JMenu (Environment.getUIString (prefix,
                                                              LanguageStrings.sortprojects));
                                                              // ("Sort {Projects} by"));
				m.setIcon (Environment.getIcon (Constants.SORT_ICON_NAME,
												Constants.ICON_MENU));

				p.add (m);

				m.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                                      LanguageStrings.sortlastedited),
                                            //"Last edited",
											 Constants.DATE_ICON_NAME,
											 new ActionListener ()
											 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

													_this.sortProjects ("lastEdited");

												}

											 }));

				m.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                                      LanguageStrings.sortname),
                                            //"Name",
											 Constants.SPELLCHECKER_ICON_NAME,
											 new ActionListener ()
											 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

													_this.sortProjects ("name");

												}

											 }));

				m.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                                      LanguageStrings.sortstatus),
                                            //"Status",
											 Constants.STAR_ICON_NAME,
											 new ActionListener ()
											 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

													_this.sortProjects ("status");

												}

											 }));

				m.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                                      LanguageStrings.sortwordcount),
                                            //"Word Count",
											 Constants.WORDCOUNT_ICON_NAME,
											 new ActionListener ()
											 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

													_this.sortProjects ("wordCount");

												}

											 }));

				p.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                                      LanguageStrings.managestatuses),
                                            //"Manage Statuses",
											 Constants.EDIT_ICON_NAME,
											 new ActionListener ()
											 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

													_this.showEditProjectStatuses ();

												}

											 }));

				p.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                                      LanguageStrings.changedisplay),
                                            //"Change what is displayed",
											 Constants.EDIT_ICON_NAME,
											 new ActionListener ()
											 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

													_this.showEditProjectInfo ();

												}

											 }));

                p.add (_this.getSelectBackgroundButton (true));

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

                _this.showImportProject (f);

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

                return ImportProject.isSupportedFileType (f);

			}

		}));

    }

	private void showEditProjectInfo ()
	{

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.allprojects);
        prefix.add (LanguageStrings.changedisplay);
        prefix.add (LanguageStrings.popup);

		final Landing _this = this;

        String popupName = "editprojectinfo";
        QPopup popup = this.getNamedPopup (popupName);

        if (popup == null)
        {

			popup = UIUtils.createClosablePopup (Environment.getUIString (prefix,
                                                                          LanguageStrings.title),
                                                //"Change what is displayed",
												 Environment.getIcon (Constants.EDIT_ICON_NAME,
																	  Constants.ICON_POPUP),
												 null);

			final QPopup _popup = popup;

            popup.setPopupName (popupName);

            this.addNamedPopup (popupName,
                                popup);

			Box content = new Box (BoxLayout.Y_AXIS);

			OptionsBox bb = new OptionsBox (this);

			content.add (bb);

			ProjectInfo _bogus = new ProjectInfo ();

			_bogus.setProjectDirectory (FileSystemView.getFileSystemView ().getDefaultDirectory ());
			_bogus.setName (Environment.getUIString (prefix,
                                                     LanguageStrings.example,
                                                     LanguageStrings.name));
                                                     //Environment.replaceObjectNames ("My {Project}"));
			_bogus.setStatus (Environment.getUIString (prefix,
                                                       LanguageStrings.example,
                                                       LanguageStrings.status));
                                                     //"In Progress");
			_bogus.setLastEdited (new Date (System.currentTimeMillis () - (7 * Constants.DAY_IN_MILLIS)));
			_bogus.addStatistic (ProjectInfo.Statistic.chapterCount,
								 5);
			_bogus.addStatistic (ProjectInfo.Statistic.wordCount,
								 12123);

			_bogus.addStatistic (ProjectInfo.Statistic.gunningFogIndex,
								 3);
			_bogus.addStatistic (ProjectInfo.Statistic.fleschReadingEase,
								 73);
			_bogus.addStatistic (ProjectInfo.Statistic.fleschKincaidGradeLevel,
								 8);
			_bogus.addStatistic (ProjectInfo.Statistic.editedWordCount,
								 5398);

			//final JTextField status = new JTextField (this.getProjectInfoFormat ());

			final TextArea status = new TextArea (Environment.getUIString (prefix,
                                                                           LanguageStrings.format,
                                                                           LanguageStrings.tooltip),
                                                //"Enter the format here...",
												  3,
												  -1);
			status.setText (this.getProjectInfoFormat ());

			final ProjectBox pb = new ProjectBox (_bogus,
												  this.getProjectInfoFormat (),
												  _this);

			pb.setInteractive (false);

			status.addCaretListener (new CaretListener ()
			{

				@Override
				public void caretUpdate (CaretEvent ev)
				{

					pb.setProjectInfoFormat (status.getText ());

					UIUtils.doLater (new ActionListener ()
					{

						@Override
						public void actionPerformed (ActionEvent ev)
						{

							_popup.resize ();

						}

					});

				}

			});

			bb.addMain (Environment.getUIString (prefix,
                                                 LanguageStrings.text),
                        //"Use the following format for {project} information.  <a href='help:projects/landing'>Click here</a> for help on the format/tags that can be used.",
						status);

			Box pbb = new Box (BoxLayout.X_AXIS);
            /*
			pbb.setMaximumSize (new Dimension (PROJECT_BOX_WIDTH,
											   Short.MAX_VALUE));
                                               */
			pbb.add (pb);

			bb.addMain (UIUtils.createBoldSubHeader (Environment.getUIString (prefix,
                                                                              LanguageStrings.example,
                                                                              LanguageStrings.title),
                                                    //"Example",
													 null),
					    pbb);

			content.add (Box.createVerticalStrut (15));

			content.add (Box.createVerticalGlue ());

			JButton save = UIUtils.createButton (Environment.getUIString (prefix,
                                                                          LanguageStrings.buttons,
                                                                          LanguageStrings.save),
                                                //Environment.getButtonLabel (Constants.SAVE_BUTTON_LABEL_ID),
												 new ActionListener ()
			{

                @Override
				public void actionPerformed (ActionEvent ev)
				{

					UserProperties.set (Constants.PROJECT_INFO_FORMAT,
										status.getText ());

					try
					{

						_this.showProjects ();

					} catch (Exception e) {

						Environment.logError ("Unable to update projects with format: " +
											  status.getText (),
											  e);

						UIUtils.showErrorMessage (_this,
                                                  Environment.getUIString (LanguageStrings.allprojects,
                                                                           LanguageStrings.actionerror));
												  //"Unable to update {projects} with the new format.");

						return;

					}

				}

			});

			JButton cancel = UIUtils.createButton (Environment.getUIString (prefix,
                                                                            LanguageStrings.buttons,
                                                                            LanguageStrings.cancel),
                                                //Environment.getButtonLabel (Constants.CANCEL_BUTTON_LABEL_ID),
												   new ActionListener ()
			{

                @Override
				public void actionPerformed (ActionEvent ev)
				{

					_popup.removeFromParent ();

				}

			});

			JButton reset = UIUtils.createButton (Environment.getUIString (prefix,
                                                                           LanguageStrings.buttons,
                                                                           LanguageStrings._default),
                                                //"Use default",
                                                  new ActionListener ()
			{

                @Override
				public void actionPerformed (ActionEvent ev)
				{

					status.setText (UserProperties.get (Constants.DEFAULT_PROJECT_INFO_FORMAT));

					pb.setProjectInfoFormat (status.getText ());

					_popup.resize ();

					UserProperties.set (Constants.PROJECT_INFO_FORMAT,
										status.getText ());

					try
					{

						_this.showProjects ();

					} catch (Exception e) {

						Environment.logError ("Unable to update projects with format: " +
											  status.getText (),
											  e);

						UIUtils.showErrorMessage (_this,
                                                  Environment.getUIString (LanguageStrings.allprojects,
                                                                           LanguageStrings.actionerror));
												  //"Unable to update {projects} with the new format.");

						return;

					}

				}

			});

			JButton[] buts = { save, reset, cancel };

			JPanel bp = UIUtils.createButtonBar2 (buts,
												  Component.CENTER_ALIGNMENT);
			bp.setOpaque (false);
			bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
			content.add (bp);

			content.setBorder (UIUtils.createPadding (10, 10, 10, 10));

			content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
										 content.getPreferredSize ().height));

			popup.setContent (content);

			this.showPopupAt (popup,
								UIUtils.getCenterShowPosition (this,
															   popup),
								false);

			popup.setDraggable (this);

			// TODO: Why Swing??? Why, why, why???
			UIUtils.doLater (new ActionListener ()
			{

				@Override
				public void actionPerformed (ActionEvent ev)
				{

					_popup.resize ();

				}

			});

		} else {

			popup.setVisible (true);

		}

	}

	private void showEditProjectStatuses ()
	{

        String popupName = "projectstatuses";
        QPopup popup = this.getNamedPopup (popupName);

        if (popup == null)
        {

            popup = UIUtils.createClosablePopup (Environment.getUIString (LanguageStrings.project,
                                                                          LanguageStrings.status,
                                                                          LanguageStrings.actions,
                                                                          LanguageStrings.manage,
                                                                          LanguageStrings.title),
                                                //"Manage the {Project} Statuses",
                                                 Environment.getIcon (Constants.EDIT_ICON_NAME,
                                                                      Constants.ICON_POPUP),
                                                 null);

            popup.setPopupName (popupName);

            this.addNamedPopup (popupName,
                                popup);

            EditProjectStatuses content = new EditProjectStatuses (this);
            content.init ();

			// Need a hard reference to this since a weak map is used for the property listeners
			// in the types handler.
			this.statusesChangedListener = new PropertyChangedListener ()
			{

				@Override
				public void propertyChanged (PropertyChangedEvent ev)
				{

					if (ev.getChangeType ().equals (UserPropertyHandler.VALUE_ADDED))
					{

						return;

					}

					Set<ProjectInfo> all = null;

					try
					{

						all = Environment.getAllProjectInfos ();

					} catch (Exception e) {

						Environment.logError ("Unable to get all projects",
											  e);

						return;

					}

					for (ProjectInfo pi : all)
					{

						if (((String) ev.getOldValue ()).equals (pi.getStatus ()))
						{

							pi.setStatus ((String) ev.getNewValue ());

							try
							{

								Environment.updateProjectInfo (pi);

							} catch (Exception e) {

								Environment.logError ("Unable to set new status: " +
													  ev.getNewValue () +
													  " on project: " +
													  pi,
													  e);

							}

						}

					}

				}

			};

			content.addPropertyChangedListener (this.statusesChangedListener);

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

	private void showNoProjects ()
	{

		this.setViewerTitle (Environment.getUIString (LanguageStrings.allprojects,
                                                      LanguageStrings.noprojects));
                                                    //"No {Projects}");

        this.showCard (NO_PROJECTS_CARD);

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

    private AbstractButton getSelectBackgroundButton (boolean showText)
    {

        final Landing _this = this;

        AbstractButton mi = null;

        if (showText)
        {
            java.util.List<String> prefix = new ArrayList<> ();
            prefix.add (LanguageStrings.allprojects);
            prefix.add (LanguageStrings.popupmenu);
            prefix.add (LanguageStrings.items);

            mi = new JMenuItem (Environment.getUIString (LanguageStrings.allprojects,
                                                         LanguageStrings.popupmenu,
                                                         LanguageStrings.items,
                                                         LanguageStrings.selectbackground));
                                                         //"Select a background image/color");

        } else
        {

            mi = new JButton ();
            UIUtils.setAsButton (mi);
            mi.setOpaque (false);
            mi.setToolTipText ("Click to show/hide the background image/color selector");

        }

        mi.setIcon (Environment.getIcon ("bg-select",
                                         (showText ? Constants.ICON_MENU : Constants.ICON_TOOLBAR)));

        mi.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.showBackgroundSelector ();

                }

            });

        return mi;

    }

    public void showBackgroundSelector ()
    {

        final Landing _this = this;

        if (this.backgroundSelectorPopup == null)
        {

            this.backgroundSelectorPopup = BackgroundSelector.getBackgroundSelectorPopup (new ChangeAdapter ()
            {

                public void stateChanged (ChangeEvent ev)
                {

                    BackgroundChangeEvent bce = (BackgroundChangeEvent) ev;

                    if (!bce.getValue ().equals (_this.backgroundPanel.getBackgroundObject ()))
                    {

                    }

                    _this.backgroundPanel.setBackgroundObject (bce.getValue ());

                }

            },
            new Dimension (75,
                           75),
            this.backgroundObject);

            this.backgroundSelectorPopup.setDraggable (this);

            this.addPopup (this.backgroundSelectorPopup,
                           true,
                           false);

        } else
        {

            if (this.backgroundSelectorPopup.isVisible ())
            {

                this.backgroundSelectorPopup.setVisible (false);

                return;

            }

        }

        this.showPopupAt (this.backgroundSelectorPopup,
                          new Point (100,
                                     100),
                                     true);

        ((BackgroundSelector) this.backgroundSelectorPopup.getContent ()).setSelected (this.backgroundObject);

    }

    private void showRemoveProject (final ProjectInfo    p,
									final ActionListener onRemove)
    {

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.allprojects);
        prefix.add (LanguageStrings.actions);
        prefix.add (LanguageStrings.removeproject);

        final Landing _this = this;

        Map<String, ActionListener> buts = new LinkedHashMap ();
        buts.put (Environment.getUIString (prefix,
                                           LanguageStrings.popup,
                                           LanguageStrings.buttons,
                                           LanguageStrings.confirm),
                //"Yes, remove it",
                  new ActionListener ()
                  {

                     public void actionPerformed (ActionEvent ev)
                     {

                        try
                        {

							Environment.deleteProject (p,
													   onRemove);

                        } catch (Exception e) {

                            Environment.logError ("Unable to remove project: " +
                                                  p.getName (),
                                                  e);

                            UIUtils.showErrorMessage (_this,
                                                      Environment.getUIString (prefix,
                                                                               LanguageStrings.actionerror));
                                                      //"Unable to remove project, please contact Quoll Writer support for assistance.");

                            return;

                        }

                     }

                  });

        buts.put (Environment.getUIString (prefix,
                                           LanguageStrings.popup,
                                           LanguageStrings.buttons,
                                           LanguageStrings.cancel),
                //"No, keep it",
                  new ActionListener ()
                  {

                     public void actionPerformed (ActionEvent ev)
                     {

                        // Don't do anything...

                     }

                  });

        String reason = Environment.canOpenProject (p);

		String message = String.format (Environment.getUIString (prefix,
                                                                 LanguageStrings.popup,
                                                                 LanguageStrings.text),
                                        //"Sorry, {project} <b>%s</b> cannot be opened for the following reason:<br /><br /><b>%s</b><br /><br />This can happen if your projects file gets out of sync with your hard drive, for example if you have re-installed your machine or if you are using a file syncing service.<br /><br />Do you want to remove it from your list of {projects}?",
                                        p.getName (),
                                        reason);

        //message = message + "<br /><br />Note: this will <b>only</b> remove the {project} from the list it will not remove any other data.";

        JComponent mess = UIUtils.createHelpTextPane (message,
                                                      null);
        mess.setBorder (null);
        mess.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     500));

        UIUtils.createQuestionPopup (this,
                                     Environment.getUIString (prefix,
                                                              LanguageStrings.popup,
                                                              LanguageStrings.title),
                                    //"Unable to open {project}",
                                     Constants.ERROR_ICON_NAME,
                                     mess,
                                     buts,
                                     null,
                                     null);

    }

    private void showDeleteProject (final ProjectInfo    p,
									final ActionListener onDelete)
    {

        final Landing _this = this;

        final AbstractProjectViewer viewer = Environment.getProjectViewer (p);

        if (viewer != null)
        {

            java.util.List<String> prefix = new ArrayList<> ();
            prefix.add (LanguageStrings.project);
            prefix.add (LanguageStrings.actions);
            prefix.add (LanguageStrings.deleteproject);
            prefix.add (LanguageStrings.projectopen);

			UIUtils.showMessage (this,
								 Environment.getUIString (prefix,
                                                          LanguageStrings.title),
                                //"{Project} already open",
								 String.format (Environment.getUIString (prefix,
                                                                         LanguageStrings.text),
                                                //"<b>%s</b> is currently already open in a window.  Please delete the {project} from that window or close the {project} then delete it from here.",
												p.getName ()),
								 null,
								 new ActionListener ()
								 {

									@Override
									public void actionPerformed (ActionEvent ev)
									{

										viewer.setExtendedState (JFrame.NORMAL);
										viewer.toFront ();

									}

								 });

			return;

        }

		new DeleteProjectActionHandler (this,
										p,
										onDelete).actionPerformed (new ActionEvent (this, 0, "delete"));

    }

    public void showImportProject ()
	{

		this.showImportProject (null);

	}

    public void showImportProject (File f)
    {

		this.removeNamedPopup ("import-project");

        try
        {

			ImportProject im = new ImportProject ();
			im.setFile (f);
			im.setNewProjectOnly (true);

			QPopup popup = UIUtils.createWizardPopup (Environment.getUIString (LanguageStrings.importproject,
                                                                               LanguageStrings.title),
                                                    //"Import a File or {Project}",
													  Constants.PROJECT_IMPORT_ICON_NAME,
													  null,
													  im);

			popup.setDraggable (this);

			popup.resize ();
			this.showPopupAt (popup,
							  UIUtils.getCenterShowPosition (this,
															 popup),
							  false);

			this.addNamedPopup ("import-project",
								popup);

        } catch (Exception e) {

            Environment.logError ("Unable to show import project for file: " +
                                  f,
                                  e);

            UIUtils.showErrorMessage (this,
                                      Environment.getUIString (LanguageStrings.importproject,
                                                               LanguageStrings.actionerror));
                                      //"Unable to show import project wizard, please contact Quoll Writer support for assistance.");

        }

    }

    private boolean handleOpenProject (final ProjectInfo    p,
									   final ActionListener onOpen)
    {

        String reason = Environment.canOpenProject (p);

        if (reason != null)
        {

            this.showRemoveProject (p,
									null);

            return false;

        }

		if (p.isOpening ())
		{

			return false;

		}

		// Is the project already open?
		AbstractProjectViewer pv = Environment.getProjectViewer (p);

		if (pv != null)
		{

			pv.setExtendedState (JFrame.NORMAL);
			pv.toFront ();

			return true;

		}

		final Landing _this = this;

		ActionListener _onOpen = new ActionListener ()
		{

			@Override
			public void actionPerformed (ActionEvent ev)
			{

				if (UserProperties.getAsBoolean (Constants.CLOSE_PROJECTS_WINDOW_WHEN_PROJECT_OPENED_PROPERTY_NAME))
				{

					_this.setVisible (false);

				}

				if (onOpen != null)
				{

					onOpen.actionPerformed (ev);

				}

			}

		};

        try
        {

            Environment.openProject (p,
									 _onOpen);

            return true;

        } catch (Exception e)
        {

            // Check for encryption.
            if ((ObjectManager.isEncryptionException (e))
                &&
                (!p.isEncrypted ())
               )
            {

                // Try with no credentials.
                try
                {

                    p.setNoCredentials (true);

					this.handleOpenProject (p,
											_onOpen);

                    return true;

                } catch (Exception ee) {

                    p.setNoCredentials (false);

                    // Check for encryption.
                    if (ObjectManager.isEncryptionException (e))
                    {

                        p.setEncrypted (true);

                        this.handleOpenProject (p,
												_onOpen);

                        return true;

                    }

                    Environment.logError ("Unable to open project: " +
                                          p.getName (),
                                          ee);

                    java.util.List<String> prefix = new ArrayList<> ();
                    prefix.add (LanguageStrings.project);
                    prefix.add (LanguageStrings.actions);
                    prefix.add (LanguageStrings.openproject);
                    prefix.add (LanguageStrings.openerrors);

                    UIUtils.showErrorMessage (this,
                                              String.format (Environment.getUIString (prefix,
                                                                                      LanguageStrings.general),
                                                                       //"Unable to open project: " +
                                                             p.getName (),
                                                             Environment.getUIString (prefix,
                                                                                      LanguageStrings.unspecified)));

                    return false;

                }

            }

            Environment.logError ("Unable to open project: " +
                                  p.getName (),
                                  e);

            java.util.List<String> prefix = new ArrayList ();
            prefix.add (LanguageStrings.project);
            prefix.add (LanguageStrings.actions);
            prefix.add (LanguageStrings.openproject);
            prefix.add (LanguageStrings.openerrors);

            UIUtils.showErrorMessage (this,
                                      String.format (Environment.getUIString (prefix,
                                                                              LanguageStrings.general),
                                                               //"Unable to open project: " +
                                                     p.getName (),
                                                     Environment.getUIString (prefix,
                                                                              LanguageStrings.unspecified)));

        }

        return false;

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

		try
		{

			String bgs = UserProperties.get ("landing-background-state");

			if (bgs != null)
			{

				Map<String, String> state = UIUtils.parseState (bgs);

				String bg = state.get ("background");

				if (bg != null)
				{

					try
					{

						this.backgroundPanel.setBackgroundObject (bg);

					} catch (Exception e) {

						Environment.logError ("Unable to set board background to: " +
											  bg,
											  e);

					}

				}

				String op = state.get ("opacity");

				float opv = 0.7f;

				try
				{

					opv = Float.parseFloat (op);

				} catch (Exception e) {

					// Ignore.

				}

				this.backgroundPanel.setBackgroundOpacity (opv);

			}

		} catch (Exception e) {

			Environment.logError ("Unable to init landing background",
								  e);

		}

		int width = 0;
		int height = 0;

		try
		{

			width = UserProperties.getAsInt ("landing-window-width");

			height = UserProperties.getAsInt ("landing-window-height");

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

		this.showProjects ();

		this.setTitleHeaderControlsVisible (true);

		this.showViewer ();

    }

	public void showProjects ()
					   throws Exception
	{

		this.currentCard = null;

		try
		{

			java.util.List<ProjectInfo> projs = new ArrayList (Environment.getAllProjectInfos ());

			Collections.sort (projs,
							  new ProjectInfoSorter ());

			if (projs.size () == 0)
			{

				this.showNoProjects ();

			} else {

				this.fillProjects ();

			}

		} catch (Exception e) {

			throw new GeneralException ("Unable to get projects",
										e);

		}

	}

	private void sortProjects (String sortBy)
	{

		UserProperties.set (Constants.SORT_PROJECTS_BY_PROPERTY_NAME,
							sortBy);

		this.fillProjects ();

	}

	@Override
	public void projectInfoChanged (ProjectInfoChangedEvent ev)
	{

		this.fillProjects ();

	}

	private void fillProjects ()
	{

		// Get how we should sort.
		String sortBy = UserProperties.get (Constants.SORT_PROJECTS_BY_PROPERTY_NAME);

		if (sortBy == null)
		{

			sortBy = "ifThenElse (lastEdited = null, 0, lastEdited) DESC, name";

		}

		if (sortBy.equals ("lastEdited"))
		{

			sortBy = "ifThenElse (lastEdited = null, 0, lastEdited) DESC, name, status";

		}

		if (sortBy.equals ("status"))
		{

			sortBy = "status, ifThenElse (lastEdited = null, 0, lastEdited) DESC, name";

		}

		if (sortBy.equals ("name"))
		{

			sortBy = "name, ifThenElse (lastEdited = null, 0, lastEdited) DESC, status";

		}

		if (sortBy.equals ("wordCount"))
		{

			sortBy = "wordCount DESC, ifThenElse (lastEdited = null, 0, lastEdited) DESC, name, status";

		}

		java.util.List<ProjectInfo> infos = null;

		try
		{

			infos = new ArrayList (Environment.getAllProjectInfos ());

		} catch (Exception e) {

			Environment.logError ("Unable to get all project infos",
								  e);

			UIUtils.showErrorMessage (this,
                                      Environment.getUIString (LanguageStrings.allprojects,
                                                               LanguageStrings.actionerror));
									  //"Unable to get {project} information, please contact Quoll Writer support for assistance.");

			return;

		}

        VerticalLayout layout = new VerticalLayout (7,
                                                    7,
                                                    PROJECT_BOX_WIDTH,
													VerticalLayout.LEFT);

		this.backgroundPanel.setLayout (layout);

		this.setViewerTitle (String.format (Environment.getUIString (LanguageStrings.allprojects,
                                                                     LanguageStrings.title),
                                            //"Your {Projects} (%s)",
											Environment.formatNumber (infos.size ())));

		try
		{

			String sql = String.format ("SELECT * FROM %s ORDER BY %s",
										ProjectInfo.class.getName (),
										sortBy);

			Query q = new Query ();

			q.parse (sql);

			QueryResults qr = q.execute (infos);

			infos = (java.util.List<ProjectInfo>) qr.getResults ();

		} catch (Exception e) {

			Collections.sort (infos,
							  new ProjectInfoSorter ());

		}

		this.backgroundPanel.removeAll ();

		for (ProjectInfo p : infos)
		{

			java.util.List<JComponent> l = new ArrayList ();

			l.add (new ProjectBox (p,
								   this.getProjectInfoFormat (),
								   this));

			this.backgroundPanel.add (UIUtils.createButtonBar (l));

		}

		if (this.currentCard != null)
		{

			if ((!NO_PROJECTS_CARD.equals (this.currentCard))
				&&
				(!MAIN_CARD.equals (this.currentCard))
			   )
			{

				return;

			}

		}

		if (infos.size () == 0)
		{

			this.showCard (NO_PROJECTS_CARD);

		} else {

			this.showMainCard ();

		}

	}

	public String getProjectInfoFormat ()
	{

		String f = UserProperties.get (Constants.PROJECT_INFO_FORMAT);

		if (f == null)
		{

			f = UserProperties.get (Constants.DEFAULT_PROJECT_INFO_FORMAT);

		}

		return f;

	}

    public class ProjectBox extends Box implements PropertyChangedListener
    {

		private Box content = null;
		private ProjectInfo project = null;
		private boolean showBackground = false;
		private Landing parent = null;
		private JLabel name = null;
		private JLabel icon = null;
		private JLabel info = null;
		private String format = null;
		private boolean interactive = true;

		public ProjectInfo getProjectInfo ()
		{

			return this.project;

		}

        @Override
        public int getBaseline (int w,
                                int h)
        {

            return 0;

        }

		public void setInteractive (boolean v)
		{

			this.interactive = v;

		}

        public ProjectBox (ProjectInfo p,
						   String      format,
						   Landing     parent)
        {

            super (BoxLayout.Y_AXIS);

			this.parent = parent;
			this.format = format;

			final ProjectBox _this = this;

			this.project = p;

			this.setOpaque (true);
			this.setBackground (UIUtils.getComponentColor ());

			this.name = new JLabel ("")
			{

				@Override
				public void setText (String t)
				{

					super.setText (String.format ("<html>%s</html>",
												  t));

				}

			};

			this.info = new JLabel ("")
			{

				@Override
				public void setText (String t)
				{

					super.setText (String.format ("<html>%s</html>",
												  t));

				}

			};

			this.icon = new JLabel ("");

			this.content = new Box (BoxLayout.X_AXIS)
			{

				public void paintComponent (Graphics g)
				{

					if (_this.showBackground)
					{

						Graphics2D g2d = (Graphics2D) g;

						Dimension s = this.getSize ();

						GradientPaint gp = new GradientPaint (0, 0,
															  UIUtils.getColor ("#ffffff"),
															  0, (s.height * 1f),
															  UIUtils.getColor ("#dddddd"),
															  false);

						g2d.setPaint (gp);
						g2d.fill (g2d.getClip ());

						g2d.setPaint (new Color (0,
												 0,
												 0,
												 1));
						g2d.fill (g2d.getClip ());

					}

				}

			};

			this.add (new JLayer<JComponent> (this.content, new LayerUI<JComponent> ()
			{

				public String getToolTipText()
				{

                    java.util.List<String> prefix = new ArrayList<> ();
                    prefix.add (LanguageStrings.allprojects);
                    prefix.add (LanguageStrings.actions);
                    prefix.add (LanguageStrings.openproject);
                    prefix.add (LanguageStrings.tooltips);

					if (!_this.interactive)
					{

						return null;

					}

					//final ProjectBox _this = this;

					String tip = null;

					tip = Environment.canOpenProject (_this.project);

					if (tip != null)
					{

						tip = String.format (Environment.getUIString (prefix,
                                                                      LanguageStrings.error),
                                             tip);
                        //"This {project} cannot be opened for the following reason:<br /><br />" + tip + "<br /><br />Right click to remove this from your list of {projects}.";

					}

					if ((tip == null)
						&&
						(_this.project.isEncrypted ())
					   )
					{

						tip = Environment.getUIString (prefix,
                                                       LanguageStrings.encrypted);
                        //"This {project} is encrypted and needs a password to access it.  Click to open it.";

					}

					if ((tip == null)
						&&
						(_this.project.isEditorProject ())
					   )
					{

						EditorEditor ed = _this.project.getForEditor ();

						if (ed != null)
						{

							String name = ed.getMainName ();

							ed = EditorsEnvironment.getEditorByEmail (ed.getEmail ());

							if (ed != null)
							{

								name = ed.getShortName ();

							}

							tip = String.format (Environment.getUIString (prefix,
                                                                          LanguageStrings.editor),
                                                //"You are editing this {project} for <b>%s</b>.  Click to open it.",
												 name);

						}

					}

					if ((tip == null)
						&&
						(_this.project.isWarmupsProject ())
					   )
					{

						tip = Environment.getUIString (prefix,
                                                       LanguageStrings.warmups);
                                                    //"This is your {warmups} {project}.  Click to open it.";

					}

					if (tip == null)
					{

						tip = Environment.getUIString (prefix,
                                                       LanguageStrings.general);
                                                       //"Click to open the {project}.";

					}

					return "<html>%s</html>";

				}

				@Override
				public void installUI(JComponent c) {
					super.installUI(c);
					// enable mouse motion events for the layer's subcomponents
					((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
				}

				@Override
				public void uninstallUI(JComponent c) {
					super.uninstallUI(c);
					// reset the layer event mask
					((JLayer) c).setLayerEventMask(0);
				}

				@Override
				public void processMouseEvent (MouseEvent                   ev,
											   JLayer<? extends JComponent> l)
				{

					if (_this.interactive)
					{

						l.getView ().setToolTipText (this.getToolTipText ());

						if (ev.getID () == MouseEvent.MOUSE_RELEASED)
						{

							if (ev.isPopupTrigger ())
							{

                                java.util.List<String> prefix = new ArrayList<> ();
                                prefix.add (LanguageStrings.allprojects);
                                prefix.add (LanguageStrings.project);
                                prefix.add (LanguageStrings.popupmenu);
                                prefix.add (LanguageStrings.items);

								ev.consume ();

								JPopupMenu popup = new JPopupMenu ();

								String reason = Environment.canOpenProject (_this.project);

								if (reason != null)
								{

									popup.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                                                LanguageStrings.remove),
                                                                    //"Remove {project}",
																	   Constants.ERROR_ICON_NAME,
																	   new ActionListener ()
																	   {

																			public void actionPerformed (ActionEvent ev)
																			{

																				_this.parent.showRemoveProject (_this.project,
																												new ActionListener ()
																				{

																					public void actionPerformed (ActionEvent ev)
																					{

																						_this.parent.remove (_this);

																						_this.parent.validate ();

																						_this.parent.repaint ();

																					}

																				});

																			}

																		}));

								} else {

									popup.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                                                LanguageStrings.open),
                                                                        //"Open",
																	   Constants.OPEN_PROJECT_ICON_NAME,
																	   new ActionListener ()
																	   {

																			public void actionPerformed (ActionEvent ev)
																			{

																				_this.parent.handleOpenProject (_this.project,
																												null);

																			}

																		}));

									JMenu m = new JMenu (Environment.getUIString (prefix,
                                                                                  LanguageStrings.setstatus));
                                                        //"Set Status");

									popup.add (m);

									// Get the project statuses.
									Set<String> statuses = Environment.getUserPropertyHandler (Constants.PROJECT_STATUSES_PROPERTY_NAME).getTypes ();

									for (String s : statuses)
									{

										m.add (_this.createStatusMenuItem (s));

									}

									m.add (_this.createStatusMenuItem (null));

									m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                                            LanguageStrings.newstatus),
                                                                    //"Add a new Status",
																   Constants.EDIT_ICON_NAME,
																   new ActionListener ()
																   {

																		public void actionPerformed (ActionEvent ev)
																		{

																			_this.showAddNewProjectStatus ();

																		}

																	}));

									popup.addSeparator ();

									if (!_this.project.isEncrypted ())
									{

										popup.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                                                    LanguageStrings.createbackup),
                                                                    //"Create a Backup",
																		   Constants.SNAPSHOT_ICON_NAME,
																		   new ActionListener ()
																		   {

																				public void actionPerformed (ActionEvent ev)
																				{

																					UIUtils.showCreateBackup (_this.project,
																											  null,
																											  _this.parent);

																				}

																			}));

									}

									popup.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                                                LanguageStrings.managebackups),
                                                                    //"Manage Backups",
																	   Constants.EDIT_ICON_NAME,
																	   new ActionListener ()
																	   {

																			public void actionPerformed (ActionEvent ev)
																			{

																				UIUtils.showManageBackups (_this.project,
																										   _this.parent);

																			}

																		}));

									popup.addSeparator ();

									popup.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                                                LanguageStrings.showfolder),
                                                                        //"Show Folder",
																	   Constants.FOLDER_ICON_NAME,
																	   new ActionListener ()
																	   {

																			public void actionPerformed (ActionEvent ev)
																			{

																				UIUtils.showFile (null,
																								  _this.project.getProjectDirectory ());

																			}

																		}));

									popup.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                                                LanguageStrings.delete),
                                                                    //"Delete",
																	   Constants.DELETE_ICON_NAME,
																	   new ActionListener ()
																	   {

																			public void actionPerformed (ActionEvent ev)
																			{

																				_this.parent.showDeleteProject (_this.project,
																												new ActionListener ()
																				{

																					public void actionPerformed (ActionEvent ev)
																					{

																						_this.parent.remove (_this);

																						_this.parent.validate ();

																						_this.parent.repaint ();

																					}

																				});

																			}

																		}));

								}

								popup.show (_this,
											ev.getX (),
											ev.getY ());

								return;

							} else {

								try
								{

									_this.parent.handleOpenProject (_this.project,
																	null);

								} catch (Exception e) {

									Environment.logError ("Unable to open project: " +
														  _this.project,
														  e);

                                    java.util.List<String> prefix = new ArrayList<> ();
                                    prefix.add (LanguageStrings.project);
                                    prefix.add (LanguageStrings.actions);
                                    prefix.add (LanguageStrings.openproject);
                                    prefix.add (LanguageStrings.openerrors);

                                    UIUtils.showErrorMessage (this,
                                                              String.format (Environment.getUIString (prefix,
                                                                                                      LanguageStrings.general),
                                                                                      //"Unable to open project: " +
                                                                             _this.project.getName (),
                                                                             Environment.getUIString (prefix,
                                                                                                      LanguageStrings.unspecified)));
									//UIUtils.showErrorMessage (_this.parent,
										//					  "Unable to open {project}, please contact Quoll Writer support for assistance.");

								}

							}

							return;

						}

					}

					if (ev.getID () == MouseEvent.MOUSE_EXITED)
					{

						_this.setBorder (UIUtils.createPadding (1, 1, 1, 1));

						_this.showBackground = false;

						_this.validate ();

						_this.repaint ();

						return;

					}

					if (ev.getID () == MouseEvent.MOUSE_ENTERED)
					{

						_this.setBorder (new LineBorder (UIUtils.getBorderColor (), 1, false));
						_this.showBackground = true;

						_this.validate ();

						_this.repaint ();

						return;

					}
						/*
					if (ev.getID () == MouseEvent.MOUSE_CLICKED)
					{

						try
						{

							_this.parent.handleOpenProject (_this.project,
															new ActionListener ()
							{

								@Override
								public void actionPerformed (ActionEvent ev)
								{

									_this.parent.dispose ();

								}

							});

						} catch (Exception e) {

							Environment.logError ("Unable to open project: " +
												  _this.project,
												  e);

							UIUtils.showErrorMessage (_this.parent,
													  "Unable to open {project}, please contact Quoll Writer support for assistance.");

						}

						return;

					}
				 */
				}

			}));

			this.setBorder (UIUtils.createPadding (1, 1, 1, 1));

			this.icon.setAlignmentX (Component.LEFT_ALIGNMENT);
			this.icon.setAlignmentY (Component.TOP_ALIGNMENT);
			this.icon.setBorder (UIUtils.createPadding (5, 0, 0, 0));

			Box detail = new Box (BoxLayout.Y_AXIS);
			detail.setAlignmentX (Component.LEFT_ALIGNMENT);
			detail.setAlignmentY (Component.TOP_ALIGNMENT);

			this.content.add (this.icon);
			this.content.add (Box.createHorizontalStrut (5));
			this.content.add (detail);

            //Header h = new Header ();
            //h.setTitle (p.getName ());
            this.name.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.name.setAlignmentY (Component.TOP_ALIGNMENT);
			this.name.setFont (this.name.getFont ().deriveFont (UIUtils.getScaledFontSize (14)).deriveFont (Font.PLAIN));
            //h.setPaintProvider (null);
            this.name.setForeground (UIUtils.getTitleColor ());
			this.name.setBorder (UIUtils.createPadding (0, 0, 0, 0));
			detail.add (this.name);

			this.content.setBorder (UIUtils.createPadding (3, 5, 5, 5));

			this.info.setBorder (UIUtils.createPadding (2, 0, 0, 5));
			this.info.setAlignmentX (Component.LEFT_ALIGNMENT);
			this.info.setAlignmentY (Component.TOP_ALIGNMENT);

			detail.add (this.info);

			this.update ();

			this.project.addPropertyChangedListener (this);

        }

		public void setProjectInfoFormat (String t)
		{

			this.format = t;

			this.update ();

		}

		public String getFormattedProjectInfo ()
		{

            java.util.List<String> prefix = new ArrayList ();
            prefix.add (LanguageStrings.allprojects);
            prefix.add (LanguageStrings.project);
            prefix.add (LanguageStrings.view);
            prefix.add (LanguageStrings.labels);

			String lastEd = "";

			if (project.getLastEdited () != null)
			{

				lastEd = String.format (Environment.getUIString (prefix,
                                                                 LanguageStrings.lastedited),
                                        //"Last edited: %s",
										Environment.formatDate (project.getLastEdited ()));

			} else {

				lastEd = Environment.getUIString (prefix,
                                                  LanguageStrings.notedited);
                                                //"Not yet edited.";

			}

			String text = this.format;

			String nl = String.valueOf ('\n');

			while (text.endsWith (nl))
			{

				text = text.substring (0,
									   text.length () - 1);

			}

			text = text.toLowerCase ();

			text = StringUtils.replaceString (text,
											  " ",
											  "&nbsp;");
			text = StringUtils.replaceString (text,
											  nl,
											  "<br />");

			text = StringUtils.replaceString (text,
											  STATUS_TAG,
											  (this.project.getStatus () != null ? this.project.getStatus () : "No status"));

			text = StringUtils.replaceString (text,
											  WORDS_TAG,
											  String.format (Environment.getUIString (prefix,
                                                                                      LanguageStrings.words),
                                                            //"%s words",
															 Environment.formatNumber (this.project.getWordCount ())));

            text = StringUtils.replaceString (text,
                                              CHAPTERS_TAG,
                                              String.format (Environment.getUIString (prefix,
                                                                                      LanguageStrings.chapters),
                                                //"%s ${objectnames.%s.chapter}",
                                                             Environment.formatNumber (this.project.getChapterCount ())));

			text = StringUtils.replaceString (text,
											  LAST_EDITED_TAG,
											  lastEd);
			text = StringUtils.replaceString (text,
											  EDIT_COMPLETE_TAG,
											  String.format (Environment.getUIString (prefix,
                                                                                      LanguageStrings.editcomplete),
                                                            //"%s%% complete",
															 Environment.formatNumber (Environment.getPercent (this.project.getEditedWordCount (), project.getWordCount ()))));
			text = StringUtils.replaceString (text,
											  READABILITY_TAG,
											  String.format (Environment.getUIString (prefix,
                                                                                      LanguageStrings.readability),
                                                            //"GL: %s, RE: %s, GF: %s",
															 Environment.formatNumber (Math.round (this.project.getFleschKincaidGradeLevel ())),
															 Environment.formatNumber (Math.round (this.project.getFleschReadingEase ())),
															 Environment.formatNumber (Math.round (this.project.getGunningFogIndex ()))));

			return text;

		}

		private void showAddNewProjectStatus ()
		{

            final java.util.List<String> prefix = new ArrayList<> ();
            prefix.add (LanguageStrings.project);
            prefix.add (LanguageStrings.status);
            prefix.add (LanguageStrings.actions);
            prefix.add (LanguageStrings.add);

			final ProjectBox _this = this;

			UIUtils.createTextInputPopup (this.parent,
										  Environment.getUIString (prefix,
                                                                   LanguageStrings.popup,
                                                                   LanguageStrings.title),
                                            //"Add a new {Project} Status",
										  null,
                                          Environment.getUIString (prefix,
                                                                   LanguageStrings.popup,
                                                                   LanguageStrings.text),
										  //"Enter a new status below, it will be applied to the {project} once added.",
                                          Environment.getUIString (prefix,
                                                                   LanguageStrings.popup,
                                                                   LanguageStrings.buttons,
                                                                   LanguageStrings.save),
										  //"Add",
                                          Environment.getUIString (prefix,
                                                                   LanguageStrings.popup,
                                                                   LanguageStrings.buttons,
                                                                   LanguageStrings.cancel),
										  //null,
										  null,
										  new ValueValidator<String> ()
										  {

											public String isValid (String v)
											{

												if ((v == null)
													||
													(v.trim ().length () == 0)
												   )
												{

													return Environment.getUIString (prefix,
                                                                                    LanguageStrings.popup,
                                                                                    LanguageStrings.errors,
                                                                                    LanguageStrings.novalue);
                                                    //"Please enter the new status.";

												}

												v = v.trim ();

												Set<String> statuses = Environment.getUserPropertyHandler (Constants.PROJECT_STATUSES_PROPERTY_NAME).getTypes ();

												for (String s : statuses)
												{

													if (s.equalsIgnoreCase (v))
													{

                                                        return Environment.getUIString (prefix,
                                                                                        LanguageStrings.popup,
                                                                                        LanguageStrings.errors,
                                                                                        LanguageStrings.valueexists);
														//return "Already have that status.";

													}

												}

												return null;

											}

										},
										// On confirm
										new ActionListener ()
										{

											@Override
											public void actionPerformed (ActionEvent ev)
											{

												_this.project.setStatus (ev.getActionCommand ());

												try
												{

													Environment.updateProjectInfo (_this.project);

												} catch (Exception e) {

													Environment.logError ("Unable to update project info: " +
																		  _this.project,
																		  e);

													UIUtils.showErrorMessage (_this.parent,
                                                                              Environment.getUIString (LanguageStrings.project,
                                                                                                       LanguageStrings.status,
                                                                                                       LanguageStrings.actionerror));
																			  //"Unable to update {project}.");

													return;

												}

												Environment.getUserPropertyHandler (Constants.PROJECT_STATUSES_PROPERTY_NAME).addType (ev.getActionCommand (),
																																	   true);

											}

										},
										null,
										null);

		}

		private JMenuItem createStatusMenuItem (final String status)
		{

			final ProjectBox _this = this;

            return UIUtils.createMenuItem ((status == null ? Environment.getUIString (LanguageStrings.project,
                                                                                      LanguageStrings.status,
                                                                                      LanguageStrings.novalue)
                                                           : status),
                                           (String) null,
                                           new ActionListener ()
                                           {

                                                public void actionPerformed (ActionEvent ev)
                                                {

													try
													{

														_this.project.setStatus (status);

														Environment.updateProjectInfo (_this.project);

													} catch (Exception e) {

														Environment.logError ("Unable to update status for project: " +
																			  _this.project,
																			  e);

														UIUtils.showErrorMessage (_this.parent,
                                                                                  Environment.getUIString (LanguageStrings.project,
                                                                                                           LanguageStrings.status,
                                                                                                           LanguageStrings.actionerror));
																				  //"Unable to update status");

													}

                                                }

                                           });

		}

		private void setSize (JLabel l,
							  String text)
		{

			// Taken from: http://blog.nobel-joergensen.com/2009/01/18/changing-preferred-size-of-a-html-jlabel/
			JLabel resizer = new JLabel();

			resizer.setText("<html>" + text + "</html>");

			View view = (View) resizer.getClientProperty(
					javax.swing.plaf.basic.BasicHTML.propertyKey);

			view.setSize(201, 0);

			float w = view.getPreferredSpan(View.X_AXIS);
			float h = view.getPreferredSpan(View.Y_AXIS);

			l.setPreferredSize (new java.awt.Dimension((int) Math.ceil(w),
                (int) Math.ceil(h)));

		}

		private void update ()
		{

			String inf = this.getFormattedProjectInfo ();

			if (inf.trim ().length () == 0)
			{

				this.info.setVisible (false);

			} else {

				this.info.setText (inf);

				this.info.setVisible (true);

			}

			this.name.setText (this.project.getName ());

			this.icon.setIcon (Environment.getIcon (this.getIconName (),
													Constants.ICON_TOOLBAR));

			this.validate ();
			this.repaint ();

		}

		@Override
		public void propertyChanged (PropertyChangedEvent ev)
		{

			this.update ();

		}

		private String getIconName ()
		{

			String n = Project.OBJECT_TYPE;

			if (!this.project.getProjectDirectory ().exists ())
			{

				// Return a problem icon.
				n = Constants.ERROR_ICON_NAME;

				return n;

			}

			if (this.project.isEncrypted ())
			{

				// Return the lock icon.
				n = Constants.LOCK_ICON_NAME;

			}

			if (this.project.isEditorProject ())
			{

				// Return an editor icon.
				n = Constants.EDITORS_ICON_NAME;

			}

			if (this.project.isWarmupsProject ())
			{

				// Return an editor icon.
				n = Constants.WARMUPS_ICON_NAME;

			}

			return n;

		}

    }

	public void showManageBackups (final ProjectInfo proj)
	{

		UIUtils.showManageBackups (proj,
								   this);

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

        final Landing _this = this;

		//this.splitPane.setLeftComponent (this.backgroundPanel);
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

			//sb.init ();

            this.addSideBar (sb);

        }

        this.showSideBar (EditorsSideBar.ID);

        return true;

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
    public void viewEditor (final EditorEditor ed)
                     throws GeneralException
    {

        this.viewEditors ();

        final Landing _this = this;

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

                    UIUtils.showErrorMessage (_this,
                                              "Unable to show Editor");

                    Environment.logError ("Unable to show editor: " +
                                          ed,
                                          e);

                }

            }

        });

    }

	@Override
    public boolean isEditorsVisible ()
    {

        EditorsSideBar sb = (EditorsSideBar) this.sideBars.get (EditorsSideBar.ID);

        if (sb != null)
        {

            return sb.isShowing ();

        }

        return false;

    }

	@Override
    public Set<String> getTitleHeaderControlIds ()
	{

		Set<String> ids = new LinkedHashSet ();

        ids.add ("strings");
		ids.add (NEW_PROJECT_HEADER_CONTROL_ID);
		ids.add (IMPORT_PROJECT_HEADER_CONTROL_ID);
		ids.add (CONTACTS_HEADER_CONTROL_ID);
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

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.allprojects);
        prefix.add (LanguageStrings.headercontrols);
        prefix.add (LanguageStrings.items);

		final Landing _this = this;

		JComponent c = null;

        if (id.equals ("strings"))
        {

            c = UIUtils.createButton (Constants.ADD_ICON_NAME,
                                      Constants.ICON_TITLE_ACTION,
                                      "Click to add a new set of langauge strings",
                                    // ??? "Click to provide feedback/report a problem with the beta",
                                          new ActionAdapter ()
                                          {

                                              public void actionPerformed (ActionEvent ev)
                                              {

                                                  LanguageStrings ls = new LanguageStrings (Environment.getDefaultUILanguageStrings ());
                                                  ls.setNativeName ("French");

                                                  try
                                                  {

                                                      new LanguageStringsEditor (ls).init ();

                                                  } catch (Exception e) {

                                                      Environment.logError ("Unable to create language strings editor",
                                                                            e);

                                                      UIUtils.showErrorMessage (_this,
                                                                                "Unable to create strings editor.");

                                                  }

                                              }

                                          });

        }

		if (id.equals (NEW_PROJECT_HEADER_CONTROL_ID))
		{

			c = UIUtils.createButton (Constants.ADD_ICON_NAME,
                                      Constants.ICON_TITLE_ACTION,
                                      Environment.getUIString (prefix,
                                                               LanguageStrings.add,
                                                               LanguageStrings.tooltip),
                                    // ??? "Click to provide feedback/report a problem with the beta",
                                          new ActionAdapter ()
                                          {

                                              public void actionPerformed (ActionEvent ev)
                                              {

                                                  UIUtils.showAddNewProject (_this,
																			 null,
																			 null);

                                              }

                                          });

		}

		if (id.equals (IMPORT_PROJECT_HEADER_CONTROL_ID))
		{

			c = UIUtils.createButton (Constants.PROJECT_IMPORT_ICON_NAME,
                                      Constants.ICON_TITLE_ACTION,
                                      Environment.getUIString (prefix,
                                                               LanguageStrings.add,
                                                               LanguageStrings.tooltip),
                                    //"Click to create a new {project} by importing a file",
                                          new ActionAdapter ()
                                          {

                                              public void actionPerformed (ActionEvent ev)
                                              {

                                                  _this.showImportProject ();

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

        return Project.OBJECT_TYPE;

    }

	@Override
    public void fillSettingsPopup (JPopupMenu popup)
	{

        java.util.List<String> prefix = new ArrayList ();

        prefix.add (LanguageStrings.allprojects);
        prefix.add (LanguageStrings.settingsmenu);
        prefix.add (LanguageStrings.items);

		final Landing _this = this;

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
                                                                 LanguageStrings.importfileorproject),
                                        //"Import File/{Project}",
                                        Constants.PROJECT_IMPORT_ICON_NAME,
                                        new ActionAdapter ()
                                        {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    _this.showImportProject ();

                                                }

                                            }));

        popup.add (this.createMenuItem (Environment.getUIString (prefix,
                                                                 LanguageStrings.findprojects),
                                        //"Find your {Projects}",
                                             Constants.FIND_ICON_NAME,
                                             new ActionAdapter ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    _this.showFindProjects ();

                                                }

                                            }));

		popup.addSeparator ();

        popup.add (this.createMenuItem (Environment.getUIString (prefix,
                                                                 LanguageStrings.statistics),
                                        //"Statistics",
                                        Constants.CHART_ICON_NAME,
                                        AbstractProjectViewer.SHOW_STATISTICS_ACTION));

        popup.add (this.createMenuItem (Environment.getUIString (prefix,
                                                                 LanguageStrings.targets),
                                        //"Targets",
										Constants.TARGET_ICON_NAME,
										ProjectViewer.SHOW_TARGETS_ACTION));

		popup.addSeparator ();

		JMenu m = new JMenu (Environment.getUIString (prefix,
                                                      LanguageStrings.sortprojects));
                            //Environment.replaceObjectNames ("Sort {Projects} by"));
		m.setIcon (Environment.getIcon (Constants.SORT_ICON_NAME,
										Constants.ICON_MENU));

		popup.add (m);

		m.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                              LanguageStrings.sortlastedited),
                                    //"Last edited",
									 Constants.DATE_ICON_NAME,
									 new ActionListener ()
									 {

										@Override
										public void actionPerformed (ActionEvent ev)
										{

											_this.sortProjects ("lastEdited");

										}

									 }));

		m.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                              LanguageStrings.sortname),
                                    //"Name",
									 Constants.SPELLCHECKER_ICON_NAME,
									 new ActionListener ()
									 {

										@Override
										public void actionPerformed (ActionEvent ev)
										{

											_this.sortProjects ("name");

										}

									 }));

		m.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                              LanguageStrings.sortstatus),
                                    //"Status",
									 Constants.STAR_ICON_NAME,
									 new ActionListener ()
									 {

										@Override
										public void actionPerformed (ActionEvent ev)
										{

											_this.sortProjects ("status");

										}

									 }));

		m.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                              LanguageStrings.sortwordcount),
                                    //"Word Count",
									 Constants.WORDCOUNT_ICON_NAME,
									 new ActionListener ()
									 {

										@Override
										public void actionPerformed (ActionEvent ev)
										{

											_this.sortProjects ("wordCount");

										}

									 }));

		popup.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                                  LanguageStrings.managestatuses),
                                        //"Manage Statuses",
									 Constants.EDIT_ICON_NAME,
									 new ActionListener ()
									 {

										@Override
										public void actionPerformed (ActionEvent ev)
										{

											_this.showEditProjectStatuses ();

										}

									 }));

		popup.add (_this.createMenuItem (Environment.getUIString (prefix,
                                                                  LanguageStrings.changedisplay),
                                        //"Change what is displayed",
										 Constants.EDIT_ICON_NAME,
										 new ActionListener ()
										 {

												@Override
												public void actionPerformed (ActionEvent ev)
												{

													_this.showEditProjectInfo ();

												}

										 }));


		popup.add (_this.getSelectBackgroundButton (true));

        popup.addSeparator ();

        // Do a Warm-up Exercise
        popup.add (this.createMenuItem (Environment.getUIString (prefix,
                                                                 LanguageStrings.dowarmup),
                                        //"Do a {Warmup} Exercise",
                                        Constants.WARMUPS_ICON_NAME,
										new ActionAdapter ()
										{

											@Override
											public void actionPerformed (ActionEvent ev)
											{

												_this.showWarmupPromptSelect ();

											}

										}));

	}

	@Override
	public boolean viewStatistics ()
							throws GeneralException
	{

		final Landing _this = this;

		if (this.statsPanel != null)
		{

			this.showCard (STATS_CARD);

			return true;

		}

		try
		{

			this.statsPanel = new StatisticsPanel (this,
												   new SessionWordCountChart (this),
												   new SessionTimeChart (this));

			this.statsPanel.init ();

			JButton close = UIUtils.createButton (Constants.CLOSE_ICON_NAME,
												  Constants.ICON_MENU,
												  Environment.getUIString (LanguageStrings.actions,
                                                                           LanguageStrings.clicktoclose),
                                                //"Click to close",
												  new ActionAdapter ()
			{

				public void actionPerformed (ActionEvent ev)
				{

					try
					{

						_this.statsPanel = null;

						_this.showProjects ();

					} catch (Exception e) {

						Environment.logError ("Unable to show projects",
											  e);

						_this.showMainCard ();

					}

				}

			});

			java.util.List<JButton> buts = new ArrayList ();
			buts.add (close);

			this.statsPanel.getHeader ().setControls (UIUtils.createButtonBar (buts));

			this.cards.add (this.statsPanel, STATS_CARD);

			this.statsPanel.showChart (SessionWordCountChart.CHART_TYPE);

			return this.viewStatistics ();

		} catch (Exception e) {

			Environment.logError ("Unable to view the statistics",
								  e);

			UIUtils.showErrorMessage (this,
                                      Environment.getUIString (LanguageStrings.statistics,
                                                               LanguageStrings.actionerror));
									  //"Unable to view the statistics");

			return false;

		}

	}

	@Override
	public boolean showChart (String chartType)
					   throws GeneralException
	{

		if (this.viewStatistics ())
		{

			this.statsPanel.showChart (chartType);

			return true;

		}

		return false;

	}

	@Override
	public boolean showOptions (String section)
	{

		final Landing _this = this;

		if (this.options != null)
		{

			this.showCard (OPTIONS_CARD);

			this.options.showSection (section);

			return true;

		}

		try
		{

			this.options = new Options (this,
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

			this.options.init ();

			String state = UserProperties.get ("landing-options-state");

			if (state != null)
			{

				this.options.setState (UIUtils.parseState (state));

			}

			JButton close = UIUtils.createButton (Constants.CLOSE_ICON_NAME,
												  Constants.ICON_MENU,
												  Environment.getUIString (LanguageStrings.actions,
                                                                           LanguageStrings.clicktoclose),
                                                //"Click to close",
												  new ActionAdapter ()
			{

				public void actionPerformed (ActionEvent ev)
				{

					try
					{

						_this.options = null;

						_this.showProjects ();

					} catch (Exception e) {

						Environment.logError ("Unable to show projects",
											  e);

						_this.showMainCard ();

					}

				}

			});

			java.util.List<JButton> buts = new ArrayList ();
			buts.add (close);

			this.options.getHeader ().setControls (UIUtils.createButtonBar (buts));

			this.cards.add (this.options, OPTIONS_CARD);

			return this.showOptions (section);

		} catch (Exception e) {

			Environment.logError ("Unable to view the options",
								  e);

            UIUtils.showErrorMessage (this,
                                      Environment.getUIString (LanguageStrings.options,
                                                               LanguageStrings.actionerror));
									  //"Unable to view the options");

			return false;

		}

	}

	private void findProjectsInDir (final File   dir,
									final JLabel details)
	{

		if ((dir == null)
			||
			(!dir.exists ())
			||
			(dir.isFile ())
		   )
		{

			return;

		}

		UIUtils.doLater (new ActionListener ()
		{

			@Override
			public void actionPerformed (ActionEvent ev)
			{

				details.setText ("<html>"
                                 + String.format (Environment.getUIString (LanguageStrings.allprojects,
                                                                          LanguageStrings.actions,
                                                                          LanguageStrings.findprojects,
                                                                          LanguageStrings.details),
                                                  dir.getPath ())
                                 + "</html>");

			}

		});

		File pf = new File (dir,
							Constants.PROJECT_DB_FILE_NAME_PREFIX + Constants.H2_DB_FILE_SUFFIX);

		if ((pf.exists ())
			&&
			(pf.isFile ())
		   )
		{

			// Do we already have this project.
			ProjectInfo pi = null;

			try
			{

				pi = Environment.getProjectByDirectory (dir);

			} catch (Exception e) {

				Environment.logError ("Unable to check if project exists with directory: " +
									  dir,
									  e);

			}

			if (pi == null)
			{

				// Try and open it.

				ObjectManager om = null;

				try
				{

					pi = new ProjectInfo ();
					pi.setProjectDirectory (dir);

					om = Environment.getProjectObjectManager (pi,
															  null);

					Project proj = om.getProject ();

					pi = new ProjectInfo (proj);

				} catch (Exception e) {

					if (ObjectManager.isEncryptionException (e))
					{

						// Try again but with no credentials.
						pi.setNoCredentials (true);

						try
						{

							om = Environment.getProjectObjectManager (pi,
																	  null);

							pi = new ProjectInfo (om.getProject ());
							pi.setNoCredentials (true);

						} catch (Exception ee) {

							if (ObjectManager.isEncryptionException (ee))
							{

								pi.setNoCredentials (false);
								// Get the file name.
								pi.setEncrypted (true);
								pi.setName (WordsCapitalizer.capitalizeEveryWord (dir.getName ()));
								pi.setProjectDirectory (dir);

								Project p = new Project ();
								p.setProjectDirectory (dir);
								pi.setBackupDirectory (p.getBackupDirectory ());

							} else {

								pi = null;

							}

						}

					} else {

						pi = null;

					}

				} finally {

					try
					{

						if (om != null)
						{

							om.closeConnectionPool ();

						}

					} catch (Exception e) {

						// Ignore.

					}

				}

				if (pi != null)
				{

					try
					{

						Environment.updateProjectInfo (pi);

					} catch (Exception e) {

						Environment.logError ("Unable to add project info for project: " +
											  pi,
											  e);

					}

				}

			}

		}

		File[] files = dir.listFiles ();

		if (files != null)
		{

			for (int i = 0; i < files.length; i++)
			{

				File f = files[i];

				if (f.isDirectory ())
				{

					this.findProjectsInDir (f,
											details);

				}

			}

		}

	}

	public void showFindProjects ()
	{

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.allprojects);
        prefix.add (LanguageStrings.actions);
        prefix.add (LanguageStrings.findprojects);

		if (this.findProjectsNotification != null)
		{

			UIUtils.showMessage ((PopupsSupported) this,
								 Environment.getUIString (prefix,
                                                          LanguageStrings.inprogress,
                                                          LanguageStrings.popup,
                                                          LanguageStrings.title),
                                //"Find in progress",
                                Environment.getUIString (prefix,
                                                         LanguageStrings.inprogress,
                                                         LanguageStrings.popup,
                                                         LanguageStrings.text));
								 //"A search for {projects} is currently in progress.");

			return;

		}

		final Landing _this = this;

        prefix.add (LanguageStrings.popup);

        final QPopup popup = UIUtils.createClosablePopup (Environment.getUIString (prefix,
                                                                                   LanguageStrings.title),
                                                        //"Find your {Projects}",
                                                          Environment.getIcon (Constants.FIND_ICON_NAME,
                                                                               Constants.ICON_POPUP),
                                                          null);

		Box content = new Box (BoxLayout.Y_AXIS);

		content.add (UIUtils.createHelpTextPane (Environment.getUIString (prefix,
                                                                          LanguageStrings.text),
                                                //"This function allows you to find your {projects}.  It is useful if you are using Dropbox or another file syncing service or just need to rebuild your {projects} database.<br /><br />Use the box below to select a starting directory then press <b>Find</b>, Quoll Writer will then search your file system for {projects} and it will add them automatically.",
												 null));

        content.add (Box.createVerticalStrut (5));

		final JLabel message = new JLabel ("");

		message.setBorder (UIUtils.createPadding (0, 5, 5, 0));
		message.setVisible (false);
		content.add (message);

		final JButton findBut = UIUtils.createButton (Environment.getUIString (prefix,
                                                                               LanguageStrings.buttons,
                                                                               LanguageStrings.find));
                                                                               //"Find");

		final FileFinder finder = UIUtils.createFileFind (Environment.getUserQuollWriterDir ().getPath (),
														  Environment.getUIString (prefix,
                                                                                   LanguageStrings.finder,
                                                                                   LanguageStrings.title),
                                                            //"Select a Directory",
														  JFileChooser.DIRECTORIES_ONLY,
														  Environment.getUIString (prefix,
                                                                                   LanguageStrings.finder,
                                                                                   LanguageStrings.button),
                                                            //"Select",
														  null);
		finder.setFindButtonToolTip (Environment.getUIString (prefix,
                                                              LanguageStrings.finder,
                                                              LanguageStrings.tooltip));
                                    //"Click to find a starting directory");

		finder.setBorder (UIUtils.createPadding (0,
										   5,
										   0,
										   0));

		content.add (finder);
		content.add (Box.createVerticalStrut (5));

		findBut.addActionListener (new ActionAdapter ()
		{

			public void actionPerformed (ActionEvent ev)
			{

				popup.removeFromParent ();

				final JLabel details = new JLabel ("");

				_this.findProjectsNotification = _this.addNotification (details,
																		Constants.LOADING_GIF_NAME,
																		-1);

				new Thread (new Runnable ()
				{

					public void run ()
					{

						// Search all directories for the projects.
						File startDir = finder.getSelectedFile ();

						try
						{

							_this.findProjectsInDir (startDir,
													 details);

						} catch (Exception e) {

							Environment.logError ("Unable to find projects in directory: " +
												  startDir,
												  e);

						} finally {

							UIUtils.doLater (new ActionListener ()
							{

								@Override
								public void actionPerformed (ActionEvent ev)
								{

									_this.removeNotification (_this.findProjectsNotification);

									_this.findProjectsNotification = null;

								}

							});

						}

					}

				}).start ();

			}

		});

		JButton cancelBut = UIUtils.createButton (Environment.getUIString (prefix,
                                                                           LanguageStrings.buttons,
                                                                           LanguageStrings.cancel),
                                                //Environment.getButtonLabel (Constants.CANCEL_BUTTON_LABEL_ID),
				 								  popup.getCloseAction ());

		JButton[] buts = new JButton[] { findBut, cancelBut };

		JPanel bp = UIUtils.createButtonBar2 (buts,
											  Component.LEFT_ALIGNMENT);
		bp.setOpaque (false);
		bp.setBorder (UIUtils.createPadding (0, 5, 0, 0));
		bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
		bp.setMaximumSize (bp.getPreferredSize ());
		content.add (bp);

        content.setBorder (UIUtils.createPadding (10, 10, 10, 10));

        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                 content.getPreferredSize ().height + 20));

        popup.setContent (content);

        this.showPopupAt (popup,
                            UIUtils.getCenterShowPosition (this,
                                                           popup),
                            false);

        popup.setDraggable (this);

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

		final Landing _this = this;

		try
		{

			Achievements achs = new Achievements (this);

			achs.init ();

			JButton close = UIUtils.createButton (Constants.CLOSE_ICON_NAME,
												  Constants.ICON_MENU,
												  Environment.getUIString (LanguageStrings.actions,
                                                                           LanguageStrings.clicktoclose),
                                                //"Click to close",
												  new ActionAdapter ()
			{

				public void actionPerformed (ActionEvent ev)
				{

					try
					{

						_this.showProjects ();

					} catch (Exception e) {

						Environment.logError ("Unable to show projects",
											  e);

						_this.showMainCard ();

					}

				}

			});

			java.util.List<JButton> buts = new ArrayList ();
			buts.add (close);

			achs.getHeader ().setControls (UIUtils.createButtonBar (buts));

			this.cards.add (achs, ACHIEVEMENTS_CARD);

			this.showCard (ACHIEVEMENTS_CARD);

		} catch (Exception e) {

			Environment.logError ("Unable to view the achievements",
								  e);

			UIUtils.showErrorMessage (this,
                                      Environment.getUIString (LanguageStrings.achievementspanel,
                                                               LanguageStrings.actionerror));
									  //"Unable to view the achievements");

			return false;

		}

		return true;

	}

	@Override
    public void sendMessageToEditor (final EditorEditor ed)
                              throws GeneralException
    {

        this.viewEditors ();

        final Landing _this = this;

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

                }

            }

        });

    }

	@Override
    public boolean close (boolean              noConfirm,
                          final ActionListener afterClose)
	{

		this.setVisible (false);

		if (Environment.getOpenProjects ().size () > 0)
		{

			return true;

		}

		// No more projects open and we have been closed so exit properly.
		if (this.notifications != null)
		{

			this.notifications.setVisible (false);

		}

		// Save state.
		Map m = new LinkedHashMap ();

		if (this.options != null)
		{

			this.options.getState (m);

			if (m.size () > 0)
			{

				UserProperties.set ("landing-options-state",
									UIUtils.createState (m));

			}

		}

		m = new LinkedHashMap ();

        String bg = this.backgroundPanel.getBackgroundObjectAsString ();

        m.put ("background",
               bg);

        m.put ("opacity",
               String.valueOf (this.backgroundPanel.getBackgroundOpacity ()));

		// TODO: Have Constants for these.

		UserProperties.set ("landing-background-state",
							UIUtils.createState (m));

		UserProperties.set ("landing-window-height",
							this.splitPane.getSize ().height);

		UserProperties.set ("landing-window-width",
							this.splitPane.getSize ().width);

		// Close and remove all sidebars.
        for (AbstractSideBar sb : new ArrayList<AbstractSideBar> (this.activeSideBars))
        {

            this.removeSideBar (sb);

        }

		super.close (true,
					 null);

		Environment.closeDown ();

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

        final Landing _this = this;

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

				this.showImportProject ();

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

        TargetsSideBar t = new TargetsSideBar (this);

        this.addSideBar (t);

        this.showSideBar (TargetsSideBar.ID);

	}

	@Override
    public void initActionMappings (ActionMap am)
    {

		super.initActionMappings (am);

        final Landing _this = this;

        am.put ("new-project",
				new ActionAdapter ()
				{

					@Override
					public void actionPerformed (ActionEvent ev)
					{

						UIUtils.showAddNewProject (_this,
												   null,
												   null);

					}

				});

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

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_N,
                                        InputEvent.CTRL_MASK),
                "new-project");

	}

}
