package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import javafx.beans.property.*;
import javafx.scene.*;

import com.quollwriter.*;
import com.quollwriter.db.*;
import com.quollwriter.text.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class FindProjectsPopup extends PopupContent
{

    public static final String POPUP_ID = "findprojects";
    private Notification notification = null;
    private ScheduledFuture task = null;

    public FindProjectsPopup (AbstractViewer viewer)
    {

        super (viewer);

        final FindProjectsPopup _this = this;

        List<String> prefix = Arrays.asList (allprojects,actions,findprojects,LanguageStrings.popup);

        QuollFileField find = QuollFileField.builder ()
            .initialFile (Environment.getUserQuollWriterDirPath ())
            .withViewer (viewer)
            .limitTo (QuollFileField.Type.directory)
            .findButtonTooltip (getUILanguageStringProperty (Utils.newList (prefix,finder,tooltip)))
            .chooserTitle (getUILanguageStringProperty (Utils.newList (prefix,finder,title)))
            .build ();

        Form f = Form.builder ()
            .description (getUILanguageStringProperty (Utils.newList (prefix,text)))
            .item (getUILanguageStringProperty (Utils.newList (prefix,labels,startingfolder)),
                   find)
            .confirmButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,LanguageStrings.find)))
            .cancelButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)))
            .inViewer (viewer)
            .build ();

/*
		if (this.notification != null)
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
*/
        f.setOnConfirm (ev ->
        {

            _this.getPopup ().close ();

            BasicHtmlTextFlow details = BasicHtmlTextFlow.builder ()
                .build ();

            Set<Node> controls = new LinkedHashSet<> ();
            controls.add (QuollButton.builder ()
                .styleClassName (StyleClassNames.STOP)
                .tooltip (getUILanguageStringProperty (allprojects,actions,findprojects,LanguageStrings.notification,buttons,stop,tooltip))
                .onAction (aev ->
                {

                    _this.task.cancel (true);
                    _this.viewer.unschedule (_this.task);
                    _this.notification.removeNotification ();

                })
                .build ());

            _this.notification = viewer.addNotification (details,
                                                         StyleClassNames.FINDPROJECTS,
                                                         -1,
                                                         controls);

            _this.task = _this.viewer.schedule (() ->
            {

                try
                {

                    try (Stream<Path> ls = Files.walk (find.getFile ()))
                    {

                        ls.forEach (p ->
                        {

                            if (!Files.isDirectory (p))
                            {

                                return;

                            }

                            UIUtils.runLater (() ->
                            {

                                details.setText (getUILanguageStringProperty (Arrays.asList (allprojects,actions,findprojects,LanguageStrings.notification,text),
                                                                              p.toString ()));

                            });

                            // TODO
                            //_this.handleDir (p);

                        });

                        _this.notification.removeNotification ();

                    }

                } catch (Exception e) {

                    _this.task.cancel (true);
                    _this.viewer.unschedule (_this.task);
                    _this.notification.removeNotification ();

                    Environment.logError ("Unable to walk dir: " + find.getFile (),
                                          e);

                    // TODO Show the user that there is a problem.

                }

            },
            0,
            -1);

        });
        f.setOnCancel (ev -> _this.getPopup ().close ());

        this.getChildren ().add (f);

    }

    private void handleDir (Path dir)
    {

        if (dir == null)
        {

            return;

        }

        Path file = dir.resolve (Constants.PROJECT_DB_FILE_NAME_PREFIX + Constants.H2_DB_FILE_SUFFIX);

        if ((Files.exists (file))
            &&
            (Files.isRegularFile (file))
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
								pi.setName (WordsCapitalizer.capitalizeEveryWord (dir.getFileName ().toString ()));
								pi.setProjectDirectory (dir);

								Project p = new Project ();
								p.setProjectDirectory (dir.toFile ());
								pi.setBackupDirPath (p.getBackupDirectory ().toPath ());

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

                        Environment.addProjectInfo (pi);

					} catch (Exception e) {

						Environment.logError ("Unable to add project info for project: " +
											  pi,
											  e);

					}

				}

			}

		}

	}

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (allprojects,actions,findprojects,LanguageStrings.popup,title)
            .styleClassName (StyleClassNames.FINDPROJECTS)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();

        p.requestFocus ();

        return p;

    }

}
