package com.quollwriter.ui.fx.panels;

import java.io.*;

import java.text.*;

import java.util.*;

import org.josql.*;

import com.gentlyweb.utils.*;
//import com.gentlyweb.properties.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.popups.*;

import com.quollwriter.uistrings.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.application.*;

import com.quollwriter.db.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;

public class ProjectsPanel<E extends AbstractViewer> extends PanelContent<E>
{

    public static final String PANEL_ID = "allprojects";

    public interface CommandIds extends PanelContent.CommandIds
    {

        String changedisplay = "changedisplay";
        String newprojectstatus = "newproejctstatus";

    }

    private SimpleStringProperty titleProp = null;

    private FlowPane tiles = null;

    public ProjectsPanel (E viewer)
    {

        super (viewer);

        final ProjectsPanel _this = this;

        this.titleProp = new SimpleStringProperty (this, "title");

        this.titleProp.bind (Bindings.createStringBinding (() ->
        {

            return String.format (getUIString (LanguageStrings.allprojects, LanguageStrings.title), Environment.formatNumber (Environment.allProjectsProperty ().size ()));

        },
        Environment.uilangProperty (),
        Environment.allProjectsProperty ()));

        this.tiles = new FlowPane ();
        this.tiles.getStyleClass ().add (StyleClassNames.PROJECTSLIST);

        this.fillProjects ();

        this.addChangeListener (UserProperties.getMappedStringProperty (Constants.SORT_PROJECTS_BY_PROPERTY_NAME),
                                (p, oldv, newv) ->
        {

            _this.fillProjects ();

        });

        ScrollPane tsp = new ScrollPane ();
		tsp.setContent (this.tiles);

        this.addActionMapping (() ->
        {

            QuollPopup qp = _this.viewer.getPopupById (ChangeProjectDisplayPopup.POPUP_ID);

            if (qp != null)
            {

                qp.show ();

                return;

            }

            ChangeProjectDisplayPopup p = new ChangeProjectDisplayPopup (_this.viewer);
            p.show ();

        },
        CommandIds.changedisplay);

        this.getChildren ().add (tsp);

    }

    @Override
    public Panel createPanel ()
    {

        final ProjectsPanel _this = this;

        StringProperty t = new SimpleStringProperty (this, "title");

        t.bind (Bindings.createStringBinding (() ->
        {

            return String.format (getUIString (allprojects,title), Environment.formatNumber (Environment.allProjectsProperty ().size ()));

        },
        UILanguageStringsManager.uilangProperty (),
        Environment.allProjectsProperty ()));

        Panel panel = Panel.builder ()
            .title (t)
            .content (this)
            .styleClassName (StyleClassNames.ALLPROJECTS)
            .panelId (PANEL_ID)
            .contextMenu (() ->
            {

                Set<MenuItem> items = new LinkedHashSet<> ();

                List<String> prefix = Arrays.asList (allprojects,popupmenu,LanguageStrings.items);

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,_new))
                    .styleClassName (StyleClassNames.ADD)
                    .onAction (ev ->
                    {

                        _this.viewer.runCommand (AbstractViewer.CommandId.newproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,importfileorproject))
                    .styleClassName (StyleClassNames.IMPORT)
                    .onAction (ev ->
                    {

                        _this.viewer.runCommand (AbstractViewer.CommandId.importfile);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,findprojects))
                    .styleClassName (StyleClassNames.FIND)
                    .onAction (ev ->
                    {

                        _this.viewer.runCommand (AllProjectsViewer.CommandId.findprojects);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenu.builder ()
                    .label (Utils.newList (prefix,sortprojects))
                    .styleClassName (StyleClassNames.SORTMENU)
                    .items (() ->
                    {

                        Set<MenuItem> sitems = new LinkedHashSet<> ();

                        sitems.add (QuollMenuItem.builder ()
                            .label (Utils.newList (prefix,sortlastedited))
                            .styleClassName (StyleClassNames.LASTEDITED)
                            .onAction (ev ->
                            {

                                // TODO Use an enum.
                                _this.updateSortProjectsBy ("lastEdited");

                            })
                            .build ());

                        sitems.add (QuollMenuItem.builder ()
                            .label (Utils.newList (prefix,sortname))
                            .styleClassName (StyleClassNames.NAME)
                            .onAction (ev ->
                            {

                                // TODO Use an enum.
                                _this.updateSortProjectsBy ("name");

                            })
                            .build ());

                        sitems.add (QuollMenuItem.builder ()
                            .label (Utils.newList (prefix,sortstatus))
                            .styleClassName (StyleClassNames.STATUS)
                            .onAction (ev ->
                            {

                                // TODO Use an enum.
                                _this.updateSortProjectsBy ("status");

                            })
                            .build ());

                        sitems.add (QuollMenuItem.builder ()
                            .label (Utils.newList (prefix,sortwordcount))
                            .styleClassName (StyleClassNames.WORDCOUNT)
                            .onAction (ev ->
                            {

                                // TODO Use an enum.
                                _this.updateSortProjectsBy ("wordCount");

                            })
                            .build ());

                        return sitems;

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,managestatuses))
                    .styleClassName (StyleClassNames.MANAGESTATUSES)
                    .onAction (ev ->
                    {

                        _this.viewer.runCommand (AbstractViewer.CommandId.manageprojectstatuses);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,changedisplay))
                    .styleClassName (StyleClassNames.CHANGEDISPLAY)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandIds.changedisplay);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,selectbackground))
                    .styleClassName (StyleClassNames.SELECTBG)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandIds.selectbackground);

                    })
                    .build ());

                return items;

            })
            .build ();

        panel.setOnMouseClicked (ev ->
        {

            if (ev.getClickCount () == 2)
            {

                _this.viewer.runCommand (AbstractViewer.CommandId.newproject);

            }

        });

        return panel;

    }

    private String getLegacyPreV3Background ()
    {

        try
		{

			String bgs = UserProperties.get ("landing-background-state");

			if (bgs != null)
			{

				State s = new State (bgs);

				return s.getAsString ("background");

            }

		} catch (Exception e) {

			Environment.logError ("Unable to init landing background",
								  e);

		}

        return null;

    }

    private Float getLegacyPreV3Opacity ()
    {

        try
		{

			String bgs = UserProperties.get ("landing-background-state");

			if (bgs != null)
			{

				State s = new State (bgs);

				String op = s.getAsString ("opacity");

				try
				{

					return Float.parseFloat (op);

				} catch (Exception e) {

					// Ignore.

				}

			}

		} catch (Exception e) {

			Environment.logError ("Unable to init landing background",
								  e);

		}

        return null;

    }

    @Override
    public void init (State state)
               throws GeneralException
    {

        final ProjectsPanel _this = this;

        // Legacy, pref v3.
        String bgv = this.getLegacyPreV3Background ();
        Float opacity = this.getLegacyPreV3Opacity ();

        if (bgv == null)
        {

            try
            {

                this.setBackgroundObject (BackgroundObject.createBackgroundObjectForId (bgv));

            } catch (Exception e) {

                Environment.logError ("Unable to set legacy background for: " + bgv,
                                      e);

            }

            // TODO Remove the old settings.

        }

        super.init (state);

    }

    private void showDeleteProject (final ProjectInfo    p)
    {

        // TODO
/*
        final Landing _this = this;

        final AbstractProjectViewer viewer = null; // TODO Environment.getProjectViewer (p);

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
*/
    }

    private void showRemoveProject (final ProjectInfo p)
    {

        List<String> prefix = Arrays.asList (allprojects,actions,removeproject);

        final ProjectsPanel _this = this;

        QuollPopup.questionBuilder ()
            .title (prefix,popup,title)
            .styleClassName (StyleClassNames.WARNING)
            .message (getUILanguageStringProperty (Utils.newList (prefix,popup,text),
                                                   p.getName (),
                                                   Environment.canOpenProject (p)))
            .confirmButtonLabel (prefix,popup,buttons,confirm)
            .cancelButtonLabel (prefix,popup,buttons,cancel)
            .withViewer (this.getViewer ())
            .withHandler (this.getViewer ())
            .onConfirm (ev ->
            {

                try
                {

                    Environment.deleteProject (p);

                } catch (Exception e) {

                    Environment.logError ("Unable to remove project: " +
                                          p.getName (),
                                          e);

                    ComponentUtils.showErrorMessage (_this.getViewer (),
                                                     getUILanguageStringProperty (prefix,actionerror));
                                              //"Unable to remove project, please contact Quoll Writer support for assistance.");

                    return;

                }


            })
            .build ();

    }

    private boolean handleOpenProject (final ProjectInfo p,
									   final Runnable    onOpen)
    {

        StringProperty reason = Environment.canOpenProject (p);

        if (reason != null)
        {

            this.showRemoveProject (p);

            return false;

        }

		if (p.isOpening ())
		{

			return false;

		}

		// Is the project already open?
		AbstractViewer pv = Environment.getProjectViewer (p);

		if (pv != null)
		{

            pv.toFront ();
            pv.setIconified (false);

			return true;

		}

		final ProjectsPanel _this = this;

		Runnable _onOpen = new Runnable ()
		{

			@Override
			public void run ()
			{

				if (UserProperties.getAsBoolean (Constants.CLOSE_PROJECTS_WINDOW_WHEN_PROJECT_OPENED_PROPERTY_NAME))
				{

                    // TODO _this.close (true, null);

				}

				if (onOpen != null)
				{

                    Platform.runLater (onOpen);

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

                  ComponentUtils.showErrorMessage (this.viewer,
                                                   getUILanguageStringProperty (Utils.newList (prefix,general),
                                                                                            p.getName (),
                                                                                            getUILanguageStringProperty (prefix,unspecified)));
/*
TODO Remove
                                                   String.format (Environment.getUIString (prefix,
                                                                                    LanguageStrings.general),
                                                                     //"Unable to open project: " +
                                                           p.getName (),
                                                           Environment.getUIString (prefix,
                                                                                    LanguageStrings.unspecified)));
*/
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

          ComponentUtils.showErrorMessage (this.viewer,
                                           getUILanguageStringProperty (Utils.newList (prefix,general),
                                                                        p.getName (),
                                                                        getUILanguageStringProperty (prefix,unspecified)));

        }

        return false;

    }

    private void updateSortProjectsBy (String sortBy)
	{

		UserProperties.set (Constants.SORT_PROJECTS_BY_PROPERTY_NAME,
							sortBy);

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

			infos = new ArrayList<> (Environment.allProjectsProperty ().getValue ());

		} catch (Exception e) {

			Environment.logError ("Unable to get all project infos",
								  e);

			ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (allprojects,actionerror));
									         //"Unable to get {project} information, please contact Quoll Writer support for assistance.");

			return;

		}

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

		this.tiles.getChildren ().clear ();

		for (ProjectInfo p : infos)
		{

			ProjectBox pb = new ProjectBox (p,
											this);

			//pb.setMaxHeight (Control.USE_PREF_SIZE);
            //pb.getStyleClass ().add (p.getType ());
			pb.getStyleClass ().add ("project");

			this.tiles.getChildren ().add (pb);

		}
		/*
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
		*/
	}

    @Override
    public State getState ()
    {

        return super.getState ();

    }

    public void fillToolBar (ToolBar toolBar,
                             boolean  fullScreen)
    {

    }

  public class ProjectBox extends VBox
  {

        private Header name = null;
        private BasicHtmlTextFlow info = null;
        private StringProperty infoProp = null;

		private ProjectInfo project = null;
		private ProjectsPanel parent = null;
        private ContextMenu contextMenu = null;

		public ProjectInfo getProjectInfo ()
		{

			return this.project;

		}

      public ProjectBox (ProjectInfo   p,
						 ProjectsPanel parent)
      {

            this.setFocusTraversable (true);

			this.parent = parent;

			final ProjectBox _this = this;

			this.project = p;

            this.getStyleClass ().add (this.getStyleName ());

            this.name = Header.builder ()
                .title (this.project.nameProperty ())
                .build ();

            this.infoProp = new SimpleStringProperty ();
            this.infoProp.bind (Bindings.createStringBinding (() ->
            {

                return UIUtils.getFormattedProjectInfo (_this.project);

            },
            UILanguageStringsManager.uilangProperty (),
            UserProperties.projectInfoFormatProperty (),
            this.project.statusProperty (),
            this.project.lastEditedProperty ()));

            this.info = BasicHtmlTextFlow.builder ()
                .text (this.infoProp)
                .styleClassName (StyleClassNames.INFO)
                .withHandler (this.parent.getViewer ())
                .build ();

            this.getChildren ().addAll (this.name, this.info);

            this.setOnKeyPressed (ev ->
            {

                if (ev.getCode () == KeyCode.ENTER)
                {

                    _this.parent.handleOpenProject (_this.project,
                                                    null);

                }

            });

            this.setOnMouseEntered (ev ->
			{

				StringProperty tip = null;

                List<String> prefix = Arrays.asList (allprojects,actions,openproject,tooltips);

                javafx.beans.property.StringProperty _tip = Environment.canOpenProject (_this.project);

                if (_tip != null)
                {

                    tip = getUILanguageStringProperty (Utils.newList (prefix,error),
                                                       _tip);
                    //"This {project} cannot be opened for the following reason:<br /><br />" + tip + "<br /><br />Right click to remove this from your list of {projects}.";

                }

                if ((tip == null)
                    &&
                    (_this.project.isEncrypted ())
                   )
                {

                    tip = getUILanguageStringProperty (Utils.newList (prefix,encrypted));
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

                        // TODO Change to use a property.
                        tip = getUILanguageStringProperty (Utils.newList (prefix,editor),
                                                           name);

                    }

                }

                if ((tip == null)
                    &&
                    (_this.project.isWarmupsProject ())
                   )
                {

                    tip = getUILanguageStringProperty (Utils.newList (prefix,warmups));
                                                //"This is your {warmups} {project}.  Click to open it.";

                }

                if (tip == null)
                {

                    tip = getUILanguageStringProperty (Utils.newList (prefix,general));
                                                   //"Click to open the {project}.";

                }

                UIUtils.setTooltip (_this,
                                    tip);

/*
TODO
				tip = Environment.canOpenProject (_this.project);

				if (tip != null)
				{

					tip = "This {project} cannot be opened for the following reason:<br /><br />" + tip + "<br /><br />Right click to remove this from your list of {projects}.";

				}

				if ((tip == null)
					&&
					(_this.project.isEncrypted ())
				   )
				{

					tip = "This {project} is encrypted and needs a password to access it.  Click to open it.";

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

						tip = String.format ("You are editing this {project} for <b>%s</b>.  Click to open it.",
											 name);

					}

				}

				if ((tip == null)
					&&
					(_this.project.isWarmupsProject ())
				   )
				{

					tip = "This is your {warmups} {project}.  Click to open it.";

				}

				if (tip == null)
				{

					tip = "Click to open the {project}.";

				}

				Tooltip.install (_this,
								 new Tooltip (String.format ("<html>%s</html>",
															 tip)));//Environment.replaceObjectNames (tip))));
*/
			});

            this.setOnContextMenuRequested (evv ->
            {

                List<String> prefix = Arrays.asList (allprojects,LanguageStrings.project,popupmenu,items);

				ContextMenu cm = new ContextMenu ();

                Set<MenuItem> items = new LinkedHashSet<> ();

				StringProperty reason = Environment.canOpenProject (_this.project);

				if (reason != null)
				{

                    items.add (QuollMenuItem.builder ()
                        .label (Utils.newList (prefix,remove))
                        .styleClassName (StyleClassNames.REMOVE)
                        .onAction (ev ->
                        {

                            _this.parent.showRemoveProject (_this.project);

                        })
                        .build ());

				} else {

                    items.add (QuollMenuItem.builder ()
                        .label (Utils.newList (prefix,open))
                        .styleClassName (StyleClassNames.OPEN)
                        .onAction (ev ->
                        {

                            // TODO Change to use a command?
                            _this.parent.handleOpenProject (_this.project,
															null);

                        })
                        .build ());

                    items.add (QuollMenu.builder ()
                        .label (Utils.newList (prefix,setstatus))
                        .styleClassName (StyleClassNames.SETSTATUS)
                        .items (() ->
                        {

                            Set<MenuItem> sitems = new LinkedHashSet<> ();

                            UserProperties.getProjectStatuses ().stream ()
                                .forEach (s -> sitems.add (_this.createStatusMenuItem (s)));

                            if (sitems.size () > 0)
                            {

                                sitems.add (new SeparatorMenuItem ());

                            }

                            // No value.
    						sitems.add (_this.createStatusMenuItem (UserProperties.noProjectStatusProperty ()));

                            sitems.add (QuollMenuItem.builder ()
                                .label (Utils.newList (prefix,newstatus))
                                .styleClassName (StyleClassNames.NEWSTATUS)
                                .onAction (ev ->
                                {

                                    _this.showAddNewProjectStatus ();

                                })
                                .build ());

                            return sitems;

                        })
                        .build ());

                    items.add (new SeparatorMenuItem ());

					if (!_this.project.isEncrypted ())
					{

                        items.add (QuollMenuItem.builder ()
                            .label (Utils.newList (prefix,createbackup))
                            .styleClassName (StyleClassNames.CREATEBACKUP)
                            .onAction (ev ->
                            {

                                BackupsManager.showCreateBackup (_this.project,
                                                                 null,
                                                                 _this.parent.getViewer ());

                            })
                            .build ());

					}

                    items.add (QuollMenuItem.builder ()
                        .label (Utils.newList (prefix,managebackups))
                        .styleClassName (StyleClassNames.MANAGEBACKUPS)
                        .onAction (ev ->
                        {

                            BackupsManager.showForProject (_this.project,
                                                           this.parent.getViewer ());

                        })
                        .build ());

                    items.add (new SeparatorMenuItem ());

                    items.add (QuollMenuItem.builder ()
                        .label (Utils.newList (prefix,showfolder))
                        .styleClassName (StyleClassNames.SHOWFOLDER)
                        .onAction (ev ->
                        {

                            _this.showDirectory ();

                        })
                        .build ());

                    items.add (QuollMenuItem.builder ()
                        .label (Utils.newList (prefix,delete))
                        .styleClassName (StyleClassNames.DELETE)
                        .onAction (ev ->
                        {

                            UIUtils.showDeleteProjectPopup (_this.project,
                                                            null,
                                                            _this.parent.getViewer ());

                        })
                        .build ());

					}

                    if (this.contextMenu != null)
                    {

                        this.contextMenu.hide ();

                    }

                    evv.consume ();

                    cm.setAutoHide (true);
                    cm.getItems ().addAll (items);

					cm.show (_this, evv.getScreenX (), evv.getScreenY ());

                    this.contextMenu = cm;

            });

            this.setOnMouseClicked (ev ->
            {

                if (ev.isPopupTrigger ())
                {

                    return;

                }

				try
				{

					_this.parent.handleOpenProject (_this.project,
													null);

				} catch (Exception e) {

				   Environment.logError ("Unable to open project: " +
					   				     _this.project,
										 e);

                  List<String> prefix = Arrays.asList (LanguageStrings.project,actions,openproject,openerrors);

                  ComponentUtils.showErrorMessage (_this.parent.viewer,
                                                   getUILanguageStringProperty (Utils.newList (prefix,general),
                                                                                _this.project.getName (),
                                                                                getUIString (Utils.newList (prefix,unspecified))));

				}

			});
			/*
			this.add (new JLayer<JComponent> (this.content, new LayerUI<JComponent> ()
			{

				public String getToolTipText()
				{

					if (!_this.interactive)
					{

						return null;

					}

					//final ProjectBox _this = this;

					String tip = null;

					tip = Environment.canOpenProject (_this.project);

					if (tip != null)
					{

						tip = "This {project} cannot be opened for the following reason:<br /><br />" + tip + "<br /><br />Right click to remove this from your list of {projects}.";

					}

					if ((tip == null)
						&&
						(_this.project.isEncrypted ())
					   )
					{

						tip = "This {project} is encrypted and needs a password to access it.  Click to open it.";

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

							tip = String.format ("You are editing this {project} for <b>%s</b>.  Click to open it.",
												 name);

						}

					}

					if ((tip == null)
						&&
						(_this.project.isWarmupsProject ())
					   )
					{

						tip = "This is your {warmups} {project}.  Click to open it.";

					}

					if (tip == null)
					{

						tip = "Click to open the {project}.";

					}

					return String.format ("<html>%s</html>",
										  Environment.replaceObjectNames (tip));

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

								ev.consume ();

								JPopupMenu popup = new JPopupMenu ();

								String reason = Environment.canOpenProject (_this.project);

								if (reason != null)
								{

									popup.add (UIUtils.createMenuItem ("Remove {project}",
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

									popup.add (UIUtils.createMenuItem ("Open",
																	   Constants.OPEN_PROJECT_ICON_NAME,
																	   new ActionListener ()
																	   {

																			public void actionPerformed (ActionEvent ev)
																			{

																				_this.parent.handleOpenProject (_this.project,
																												null);

																			}

																		}));

									JMenu m = new JMenu ("Set Status");

									popup.add (m);

									// Get the project statuses.
									Set<String> statuses = Environment.getUserPropertyHandler (Constants.PROJECT_STATUSES_PROPERTY_NAME).getTypes ();

									for (String s : statuses)
									{

										m.add (_this.createStatusMenuItem (s));

									}

									m.add (_this.createStatusMenuItem (null));

									m.add (UIUtils.createMenuItem ("Add a new Status",
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

										popup.add (UIUtils.createMenuItem ("Create a Backup",
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

									popup.add (UIUtils.createMenuItem ("Manage Backups",
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

									popup.add (UIUtils.createMenuItem ("Show Folder",
																	   Constants.FOLDER_ICON_NAME,
																	   new ActionListener ()
																	   {

																			public void actionPerformed (ActionEvent ev)
																			{

																				UIUtils.showFile (null,
																								  _this.project.getProjectDirectory ());

																			}

																		}));

									popup.add (UIUtils.createMenuItem ("Delete",
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

									UIUtils.showErrorMessage (_this.parent,
															  "Unable to open {project}, please contact Quoll Writer support for assistance.");

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

				}

			}));
			*/
			/*
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
			*/

      }

      private void showAddNewProjectStatus ()
      {

          List<String> prefix = Arrays.asList (LanguageStrings.project,status,actions,add);

          final ProjectBox _this = this;

          QuollPopup.textEntryBuilder ()
            .withViewer (this.parent.getViewer ())
            .withHandler (this.parent.getViewer ())
            .title (prefix,popup,title)
            .description (prefix,popup,text)
            .confirmButtonLabel (prefix,popup,buttons,save)
            .cancelButtonLabel (prefix,popup,buttons,cancel)
            .styleClassName (StyleClassNames.ADD)
            .validator (v ->
            {

                if ((v == null)
                    ||
                    (v.trim ().length () == 0)
                   )
                {

                    return getUILanguageStringProperty (Utils.newList (prefix,popup,errors,novalue));
                    //"Please enter the new status.";

                }

                v = v.trim ();

                for (StringProperty pp : UserProperties.getProjectStatuses ())
                {

                    if (pp.getValue ().equalsIgnoreCase (v))
                    {

                        return getUILanguageStringProperty (Utils.newList (prefix,popup,errors,valueexists));

                    }

                }

                return null;

            })
            .onConfirm (ev ->
            {

                javafx.scene.Node n = ev.getForm ().lookup ("#text");

                if (n != null)
                {

                    TextField tf = (TextField) n;

                    StringProperty pr = UserProperties.addProjectStatus (tf.getText ());

                    _this.project.setStatus (pr.getValue ());

                }

            })
            .build ();
            /*
            TODO Remove
          ComponentUtils.createTextEntryPopup (getUILanguageStringProperty (Utils.newList (prefix,popup,title)),
                                          //"Add a new {Project} Status",
                                        StyleClassNames.ADD,
                                        getUILanguageStringProperty (Utils.newList (prefix,popup,text)),
                                        //"Enter a new status below, it will be applied to the {project} once added.",
                                        null,
                                        v ->
                                        {

                                            if ((v == null)
                                                ||
                                                (v.trim ().length () == 0)
                                               )
                                            {

                                                return getUILanguageStringProperty (Utils.newList (prefix,popup,errors,novalue));
                                                //"Please enter the new status.";

                                            }

                                            v = v.trim ();

                                            for (StringProperty pp : UserProperties.getProjectStatuses ())
                                            {

                                                if (pp.getValue ().equalsIgnoreCase (v))
                                                {

                                                    return getUILanguageStringProperty (Utils.newList (prefix,popup,errors,valueexists));

                                                }

                                            }

                                            return null;

                                        },
                                        getUILanguageStringProperty (Utils.newList (prefix,popup,buttons,save)),
                                        getUILanguageStringProperty (Utils.newList (prefix,popup,buttons,cancel)),
                                        // On save.
                                        ev ->
                                        {

                                            javafx.scene.Node n = ev.getForm ().lookup ("#text");

                                            if (n != null)
                                            {

                                                TextField tf = (TextField) n;

                                                StringProperty pr = UserProperties.addProjectStatus (tf.getText ());

                                                _this.project.setStatus (pr.getValue ());

                                            }

                                        },
                                        // On cancel
                                        null,
                                        null,
                                        _this.parent.getViewer ());
*/
      }

      private void showDirectory ()
      {

          UIUtils.showFile (this.parent.viewer,
                            this.project.getProjectDirectory ().toPath ());

      }

      private MenuItem createStatusMenuItem (StringProperty status)
      {

          final ProjectBox _this = this;

          return QuollMenuItem.builder ()
                    .label ((status.getValue () == null ? getUILanguageStringProperty (Arrays.asList (LanguageStrings.project,LanguageStrings.status,novalue)) : status))
                    .styleClassName (status.getValue () == null ? StyleClassNames.NOVALUE : StyleClassNames.NOIMAGE)
                    .onAction (ev ->
                    {

                        try
                        {

                            _this.project.setStatus (status.getValue ());

                        } catch (Exception e) {

                            Environment.logError ("Unable to update status for project: " +
                                                  _this.project,
                                                  e);

                            ComponentUtils.showErrorMessage (_this.parent.viewer,
                                                             getUILanguageStringProperty (LanguageStrings.project,LanguageStrings.status,actionerror));
                                                             //"Unable to update status");

                        }

                    })
                    .build ();

      }

      private String getStyleName ()
      {

          String n = StyleClassNames.NORMAL;
          //Project.OBJECT_TYPE;

          if (!this.project.getProjectDirectory ().exists ())
          {

              // Return a problem icon.
              n = StyleClassNames.ERROR;
              return n;

          }

          if (this.project.isEncrypted ())
          {

              // Return the lock icon.
              n = StyleClassNames.ENCRYPTED;
              return n;

          }

          if (this.project.isEditorProject ())
          {

              // Return an editor icon.
              n = StyleClassNames.EDITOR;
              return n;

          }

          if (this.project.isWarmupsProject ())
          {

              // Return an editor icon.
              n = Warmup.OBJECT_TYPE;
              return n;

          }

          return n;

      }

    }

}
