package com.quollwriter.ui.fx.popups;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import javafx.scene.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.*;

import com.quollwriter.*;
import com.quollwriter.db.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class BackupsManager extends PopupContent
{

    public static final String POPUP_ID = "managebackups";

    private VBox backupsBox = null;
    private Node noBackups = null;
    private Hyperlink viewBackupsDir = null;
    private ProjectInfo proj = null;
    private ScrollPane backupsScroll = null;

    public BackupsManager (AbstractViewer viewer,
                           ProjectInfo    project)
                    throws GeneralException
    {

        super (viewer);

        this.proj = project;

        VBox b = new VBox ();
        this.getChildren ().add (b);

        final BackupsManager _this = this;

        BasicHtmlTextFlow desc = BasicHtmlTextFlow.builder ()
            .text (getUILanguageStringProperty (Arrays.asList (backups,text),
                                                proj.nameProperty ()))
            .withHandler (viewer)
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        this.viewBackupsDir = QuollHyperlink.builder ()
            .label (backups,viewbackupsdir)
            .styleClassName (StyleClassNames.VIEW)
            .onAction (ev ->
            {

                UIUtils.showFile (viewer,
                                  _this.proj.getBackupDirPath ());

            })
            .build ();

        this.noBackups = BasicHtmlTextFlow.builder ()
            .text (getUILanguageStringProperty (LanguageStrings.backups,nobackups))
            .withHandler (viewer)
            .styleClassName (StyleClassNames.NOBACKUPS)
            .build ();
        this.noBackups.managedProperty ().bind (this.noBackups.visibleProperty ());

        this.backupsBox = new VBox ();
        this.backupsBox.managedProperty ().bind (this.backupsBox.visibleProperty ());
        this.backupsBox.getStyleClass ().add (StyleClassNames.ITEMS);

        this.backupsScroll = new ScrollPane (this.backupsBox);
        this.backupsScroll.managedProperty ().bind (this.backupsScroll.visibleProperty ());

        QuollButtonBar bb = QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.OTHER)
                        .label (backups,show,buttons,createbackup)
                        .onAction (ev ->
                        {

                            BackupsManager.showCreateBackup (proj,
                                                             null,
                                                             _this.viewer);

                        })
                        .build ())
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.FINISH)
                        .label (backups,show,buttons,finish)
                        .onAction (ev ->
                        {

                            _this.close ();

                        })
                        .build ())
            .build ();

        b.getChildren ().addAll (desc, this.viewBackupsDir, this.noBackups, this.backupsScroll, bb);

        this.addSetChangeListener (proj.getBackupPaths (),
                                   ev ->
        {
System.out.println ("GOT CHANGE: " + ev);
            if (ev.wasRemoved ())
            {
System.out.println ("BM REMOVED: " + ev.getElementRemoved ());
                Optional<Node> n = this.backupsBox.getChildren ().stream ()
                    .filter (bn -> bn.getUserData ().equals (ev.getElementRemoved ()))
                    .findFirst ();

                if (n.isPresent ())
                {

                    this.backupsBox.getChildren ().remove (n.get ());
                    this.viewer.fireProjectEventLater (ProjectEvent.Type.backups,
                                                       ProjectEvent.Action.delete);

                }

            }

            // We only care about add events, remove is already handled below.
            if (ev.wasAdded ())
            {

                try
                {

                    Header h = _this.createBackupItem (ev.getElementAdded ());

                    _this.backupsBox.getChildren ().add (0,
                                                         h);

                } catch (Exception e) {

                    Environment.logError ("Unable to add backup path to list: " +
                                          ev.getElementAdded (),
                                          e);

                    ComponentUtils.showErrorMessage (_this.viewer,
                                                     // Not really the right error to show but this will happen so infrequently that it
                                                     // won't make a difference.
                                                     getUILanguageStringProperty (backups,show,actionerror));

                }

            }

        });

        this.update ();

    }

    private Header createBackupItem (Path backupPath)
                              throws IOException
    {

        final BackupsManager _this = this;

        Set<Node> cons = new LinkedHashSet<> ();
        cons.add (QuollButton.builder ()
            .tooltip (backups,restore,tooltip)
            .styleClassName (StyleClassNames.RESTORE)
            .onAction (ev ->
            {

                List<String> prefix = Arrays.asList (LanguageStrings.backups,restore,LanguageStrings.popup);

                QuollPopup.questionBuilder ()
                    .title (prefix,title)
                    .withViewer (this.viewer)
                    .styleClassName (StyleClassNames.RESTORE)
                    .message (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                           this.proj.nameProperty (),
                                                           backupPath.getParent ().toUri ().toString (),
                                                           backupPath.toString ()))
                    .confirmButtonLabel (prefix,buttons,confirm)
                    .cancelButtonLabel (prefix,buttons,cancel)
                    .onConfirm (fv ->
                    {

                        final AbstractProjectViewer pv = Environment.getProjectViewer (this.proj);

                        Runnable doRestore = () ->
                        {

                            Runnable _doRestore = () ->
                            {

                                try
                                {

                                    // Create a backup.
                                    Path f = BackupsManager.createBackupForProject (_this.proj,
                                                                                    true);

                                } catch (Exception e) {

                                    Environment.logError ("Unable to create backup for project: " +
                                                          _this.proj,
                                                          e);

                                    ComponentUtils.showErrorMessage (_this.viewer,
                                                                     getUILanguageStringProperty (backups,_new,actionerror));
                                                              //"Unable to create a backup of the {project} in its current state.");

                                    return;

                                }

                                // Restore using our file.
                                try
                                {

                                    BackupsManager.restoreBackupForProject (_this.proj,
                                                                            backupPath);

                                } catch (Exception e) {

                                    Environment.logError ("Unable to restore project with file: " +
                                                          backupPath +
                                                          ", project: " +
                                                          _this.proj,
                                                          e);

                                    ComponentUtils.showErrorMessage (_this.viewer,
                                                                     getUILanguageStringProperty (backups,restore,actionerror));
                                                                     //"Unable to restore backup");

                                    return;

                                }

                                List<String> prefix2 = Arrays.asList (backups,restore,confirmpopup);

                                if (pv != null)
                                {

                                    try
                                    {

                                        // Reopen the project.
                                        Environment.openProject (_this.proj);

                                        AbstractProjectViewer p = Environment.getProjectViewer (_this.proj);

                                        // Show confirmation.
                                        QuollPopup.messageBuilder ()
                                            .withViewer (p)
                                            .title (prefix2, title)
                                            .styleClassName (StyleClassNames.PROJECT)
                                            .closeButton ()
                                            .message (getUILanguageStringProperty (Utils.newList (prefix2,text),
                                                                         //"The {project} has been restored from file <b>%s</b>.",
                                                                                   backupPath.getFileName ().toString ()))
                                            .build ();

                                        p.fireProjectEventLater (ProjectEvent.Type.backups,
                                                                 ProjectEvent.Action.restore);

                                    } catch (Exception e) {

                                        Environment.logError ("Unable to reopen project: " +
                                                              _this.proj,
                                                              e);

                                        ComponentUtils.showErrorMessage (_this.viewer,
                                                                         getUILanguageStringProperty (Utils.newList (prefix2,actionerror)));
                                                                         //"Unable to re-open backup");

                                        return;

                                    }

                                    return;

                                }

                                // Show confirmation.
                                QuollPopup.messageBuilder ()
                                    .withViewer (_this.viewer)
                                    .title (prefix2, title)
                                    .message (getUILanguageStringProperty (Utils.newList (prefix2,text),
                                                                 //"{Project} <b>%s</b> has been restored using file <b>%s</b>.",
                                                                           _this.proj.nameProperty (),
                                                                           backupPath.getFileName ().toString ()))
                                    .build ();
                                    /*
                                    TODO remove
                                ComponentUtils.showMessage (_this.viewer,
                                                            getUILanguageStringProperty (Utils.newList (prefix2,title)),
                                                            //"{Project} restored",
                                                            getUILanguageStringProperty (Utils.newList (prefix2,text),
                                                                                         //"{Project} <b>%s</b> has been restored using file <b>%s</b>.",
                                                                                         _this.proj.nameProperty (),
                                                                                         backupPath.getFileName ().toString ()));
*/
                            };

                            UIUtils.askForPasswordForProject (_this.proj,
                                                              null,
                                                              password ->
                                                              {

                                                                  _this.proj.setFilePassword (password);

                                                                  UIUtils.runLater (_doRestore);

                                                              },
                                                              () ->
                                                              {

                                                                  // On cancel reopen the project.
                                                                  try
                                                                  {

                                                                      Environment.openProject (_this.proj);

                                                                  } catch (Exception e) {

                                                                      Environment.logError ("Unable to reopen project: " +
                                                                                            _this.proj,
                                                                                            e);

                                                                      ComponentUtils.showErrorMessage (null,
                                                                                                       getUILanguageStringProperty (backups,restore,confirmpopup,actionerror));
                                                                                                       //"Unable to re-open backup");

                                                                      return;

                                                                  }

                                                              },
                                                              _this.viewer);

                        };

                        if (pv != null)
                        {

                            // Close the project.
                            pv.close (true,
                                      doRestore);

                        } else {

                            UIUtils.runLater (doRestore);

                        }

                    })
                    .build ();
/*
TODO Remove
                ComponentUtils.createQuestionPopup (getUILanguageStringProperty (Utils.newList (prefix,title)),
                                                    StyleClassNames.RESTORE,
                                                    ,
                                                    getUILanguageStringProperty (Utils.newList (prefix,buttons,confirm)),
                                                    getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)),
                                                    fv ->
                                                    {

                                                        final AbstractProjectViewer pv = Environment.getProjectViewer (_this.proj);

                                                        Runnable doRestore = () ->
                                                        {

                                                            Runnable _doRestore = () ->
                                                            {

                                                                try
                                                                {

                                                                    // Create a backup.
                                                                    Path f = BackupsManager.createBackupForProject (_this.proj,
                                                                                                                    true);

                                                                } catch (Exception e) {

                                                                    Environment.logError ("Unable to create backup for project: " +
                                                                                          _this.proj,
                                                                                          e);

                                                                    ComponentUtils.showErrorMessage (_this.viewer,
                                                                                                     getUILanguageStringProperty (backups,_new,actionerror));
                                                                                              //"Unable to create a backup of the {project} in its current state.");

                                                                    return;

                                                                }

                                                                // Restore using our file.
                                                                try
                                                                {

                                                                    BackupsManager.restoreBackupForProject (_this.proj,
                                                                                                            backupPath);

                                                                } catch (Exception e) {

                                                                    Environment.logError ("Unable to restore project with file: " +
                                                                                          backupPath +
                                                                                          ", project: " +
                                                                                          _this.proj,
                                                                                          e);

                                                                    ComponentUtils.showErrorMessage (_this.viewer,
                                                                                                     getUILanguageStringProperty (backups,restore,actionerror));
                                                                                                     //"Unable to restore backup");

                                                                    return;

                                                                }

                                                                List<String> prefix2 = Arrays.asList (backups,restore,confirmpopup);

                                                                if (pv != null)
                                                                {

                                                                    try
                                                                    {

                                                                        // Reopen the project.
                                                                        Environment.openProject (_this.proj);

                                                                        AbstractProjectViewer p = Environment.getProjectViewer (_this.proj);

                                                                        // Show confirmation.
                                                                        ComponentUtils.showMessage (p,
                                                                                                    getUILanguageStringProperty (Utils.newList (prefix2,title)),
                                                                                                    //"{Project} restored",
                                                                                                    getUILanguageStringProperty (Utils.newList (prefix2,text),
                                                                                                                                 //"The {project} has been restored from file <b>%s</b>.",
                                                                                                                                 backupPath.getFileName ().toString ()));


                                                                        p.fireProjectEventLater (ProjectEvent.Type.backups,
                                                                                                 ProjectEvent.Action.restore);

                                                                    } catch (Exception e) {

                                                                        Environment.logError ("Unable to reopen project: " +
                                                                                              _this.proj,
                                                                                              e);

                                                                        ComponentUtils.showErrorMessage (_this.viewer,
                                                                                                         getUILanguageStringProperty (Utils.newList (prefix2,actionerror)));
                                                                                                         //"Unable to re-open backup");

                                                                        return;

                                                                    }

                                                                    return;

                                                                }

                                                                // Show confirmation.
                                                                ComponentUtils.showMessage (_this.viewer,
                                                                                            getUILanguageStringProperty (Utils.newList (prefix2,title)),
                                                                                            //"{Project} restored",
                                                                                            getUILanguageStringProperty (Utils.newList (prefix2,text),
                                                                                                                         //"{Project} <b>%s</b> has been restored using file <b>%s</b>.",
                                                                                                                         _this.proj.nameProperty (),
                                                                                                                         backupPath.getFileName ().toString ()));

                                                            };

                                                            UIUtils.askForPasswordForProject (_this.proj,
                                                                                              null,
                                                                                              password ->
                                                                                              {

                                                                                                  _this.proj.setFilePassword (password);

                                                                                                  UIUtils.runLater (_doRestore);

                                                                                              },
                                                                                              () ->
                                                                                              {

                                                                                                  // On cancel reopen the project.
                                                                                                  try
                                                                                                  {

                                                                                                      Environment.openProject (_this.proj);

                                                                                                  } catch (Exception e) {

                                                                                                      Environment.logError ("Unable to reopen project: " +
                                                                                                                            _this.proj,
                                                                                                                            e);

                                                                                                      ComponentUtils.showErrorMessage (null,
                                                                                                                                       getUILanguageStringProperty (backups,restore,confirmpopup,actionerror));
                                                                                                                                       //"Unable to re-open backup");

                                                                                                      return;

                                                                                                  }

                                                                                              },
                                                                                              _this.viewer);

                                                        };

                                                        if (pv != null)
                                                        {

                                                            // Close the project.
                                                            pv.close (true,
                                                                      doRestore);

                                                        } else {

                                                            UIUtils.runLater (doRestore);

                                                        }

                                                    },
                                                    _this.viewer);
*/
            })
            .build ());

        cons.add (QuollButton.builder ()
            .tooltip (backups,delete,tooltip)
            .styleClassName (StyleClassNames.DELETE)
            .onAction (ev ->
            {

                String pid = "deletebackup" + backupPath.toString ();

                if (this.viewer.getPopupById (pid) != null)
                {

                    return;

                }

                List<String> prefix = Arrays.asList (backups,delete,confirmpopup);

                QuollPopup.questionBuilder ()
                    .title (prefix,title)
                    .withViewer (this.viewer)
                    .popupId (pid)
                    .styleClassName (StyleClassNames.DELETE)
                    .message (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                           backupPath.toString ()))
                    .confirmButtonLabel (prefix,buttons,confirm)
                    .cancelButtonLabel (prefix,buttons,cancel)
                    .onConfirm (fv ->
                    {

                        try
                        {

                            _this.removeBackup (backupPath);

                        } catch (Exception e) {

                            Environment.logError ("Unable to remove backup: " + backupPath,
                                                  e);

                            ComponentUtils.showErrorMessage (_this.viewer,
                                                             backups,delete,actionerror);

                        }

                        this.viewer.getPopupById (pid).close ();

                    })
                    .build ();
/*
                ComponentUtils.createQuestionPopup (getUILanguageStringProperty (Utils.newList (prefix,title)),
                                                    StyleClassNames.DELETE,
                                                    getUILanguageStringProperty (Utils.newList (prefix,text),
                                                                                 backupPath.toString ()),
                                                    getUILanguageStringProperty (Utils.newList (prefix,buttons,confirm)),
                                                    getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)),
                                                    fv ->
                                                    {

                                                        try
                                                        {

                                                            _this.removeBackup (backupPath);

                                                        } catch (Exception e) {

                                                            Environment.logError ("Unable to remove backup: " + backupPath,
                                                                                  e);

                                                            ComponentUtils.showErrorMessage (_this.viewer,
                                                                                             getUILanguageStringProperty (backups,delete,actionerror));

                                                        }

                                                    },
                                                    _this.viewer);
*/
            })
            .build ());

        Header h = Header.builder ()
            .title (String.format ("%s (%s)",
                                   Environment.formatDateTime (new Date (Files.getLastModifiedTime (backupPath).toMillis ())),
                                   backupPath.getFileName ()))
            .styleClassName (StyleClassNames.ITEM)
            .controls (cons)
            .build ();

        h.setUserData (backupPath);

        return h;

    }

    private void removeBackup (Path backupPath)
                        throws GeneralException
    {

        try
        {
System.out.println ("REMOVING BACKUP: " + backupPath + ", " + this.proj.getBackupPaths ());
            this.proj.removeBackupPath (backupPath);

            Files.delete (backupPath);

        } catch (Exception e) {

            throw new GeneralException ("Unable to remove backup: " + backupPath,
                                        e);

        }

    }

    private void update ()
    {

        try
        {

            final BackupsManager _this = this;

            this.noBackups.setVisible (true);

            this.viewBackupsDir.setVisible (Files.exists (this.proj.getBackupDirPath ()));

            this.backupsBox.getChildren ().removeAll ();

            this.backupsBox.getChildren ().addAll (this.proj.getBackupPaths ().stream ()
                // Convert the .zip file to a visual element.
                .map (p ->
                {

                    try
                    {

                        return this.createBackupItem (p);

                    } catch (Exception e) {

                        throw new RuntimeException (e);

                    }

                })
                .collect (Collectors.toList ()));

            this.backupsScroll.setVisible (true);

            if (this.backupsBox.getChildren ().size () > 0)
            {

                this.noBackups.setVisible (false);

                UIUtils.runLater (() ->
                {

                    UIUtils.scrollIntoView (this.backupsBox.getChildren ().get (0),
                                            VPos.TOP);

                });

            } else {

                this.backupsScroll.setVisible (false);

            }

        } catch (Exception e) {

            Environment.logError ("Unable to build list of backups",
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (backups,show,actionerror));
                                             //"Unable to show list of backups, please contact Quoll Writer support for assistance.");

        }

    }

    public static void showForProject (ProjectInfo    proj,
                                       AbstractViewer viewer)
    {

        try
        {

            String pid = POPUP_ID + proj.getId ();

            QuollPopup qp = viewer.getPopupById (pid);

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            BackupsManager m = new BackupsManager (viewer,
                                                   proj);

            m.show ();

        } catch (Exception e) {

            Environment.logError ("Unable to show backups manager for: " +
                                  proj,
                                  e);

            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (backups,show,actionerror));

        }

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (backups,show, LanguageStrings.popup,title)
            .styleClassName (StyleClassNames.BACKUPS)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID + this.proj.getId ())
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();
        p.requestFocus ();

        Environment.fireUserProjectEvent (this.viewer,
                                          ProjectEvent.Type.backups,
                                          ProjectEvent.Action.show);

        return p;

    }

    public static void restoreBackupForProject (ProjectInfo p,
                                                Path        restorePath)
                                         throws Exception
    {

        Path dbDir = p.getProjectDirectory ();

        // Get the project db file.
        Path dbFile = dbDir.resolve (Constants.FULL_PROJECT_DB_FILE_NAME);

        if (Files.notExists (dbFile))
        {

            throw new GeneralException ("No project database file found at: " +
                                        dbFile +
                                        ", for project: " +
                                        p);

        }

        Path oldDbFile = dbFile.resolveSibling (Constants.FULL_OLD_PROJECT_DB_FILE_NAME);

        // Rename to .old
        try
        {

            Files.move (dbFile, oldDbFile, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {

            throw new GeneralException ("Unable to rename project database file to: " +
                                        oldDbFile +
                                        ", for project: " +
                                        p,
                                        e);

        }

        try
        {

            Utils.extractZipFile (restorePath.toFile (),
                                  p.getProjectDirectory ().toFile ());

            // See if there is a project db file in there now.
            if (Files.notExists (dbFile))
            {

                throw new GeneralException ("Backup file does not contain a valid project db file");

            }

            Files.delete (oldDbFile);

        } catch (Exception e) {

            // Try and rename back.
            Files.move (oldDbFile, dbFile, StandardCopyOption.REPLACE_EXISTING);

            throw e;

        }

    }

    public static void showCreateBackup (final Project        proj,
                                         final String         filePassword,
                                         final AbstractViewer viewer)
    {

        BackupsManager.showCreateBackup (Environment.getProjectInfo (proj),
                                         filePassword,
                                         viewer);

    }

    public static void showCreateBackup (final ProjectInfo    proj,
                                         final String         filePassword,
                                         final AbstractViewer viewer)
    {

        String pid = "createbackup" + proj.getObjectReference ().asString ();

        if (viewer.getPopupById (pid) != null)
        {

            return;

        }

        QuollPopup.questionBuilder ()
            .popupId (pid)
            .title (backups,_new,LanguageStrings.popup,title)
            .styleClassName (StyleClassNames.CREATEBACKUP)
            .message (getUILanguageStringProperty (Arrays.asList (backups,_new,LanguageStrings.popup,text),
                        //"Please confirm you wish to create a backup of {project} <b>%s</b>.",
                                                   proj.getName ()))
            .confirmButtonLabel (backups,_new,LanguageStrings.popup,buttons,confirm)
            .cancelButtonLabel (backups,_new,LanguageStrings.popup,buttons,cancel)
            .withViewer (viewer)
            .onConfirm (fev ->
            {

                try
                {

                    Path p = BackupsManager.createBackupForProject (proj,
                                                                    false);

                    VBox b = new VBox ();

                    QuollTextView m = QuollTextView.builder ()
                    //BasicHtmlTextFlow m = BasicHtmlTextFlow.builder ()
                        .styleClassName (StyleClassNames.MESSAGE)
                        .text (getUILanguageStringProperty (Arrays.asList (backups,_new,confirmpopup,text),
                                                            p.getParent ().toUri ().toString (),
                                                            p.toString ()))
                        .withViewer (viewer)
                        .build ();

                    QuollHyperlink l = QuollHyperlink.builder ()
                        .styleClassName (StyleClassNames.VIEW)
                        .label (getUILanguageStringProperty (backups,_new,confirmpopup,labels,view))
                        .onAction (ev ->
                        {

                            BackupsManager.showForProject (proj,
                                                           viewer);

                        })
                        .build ();

                    b.getChildren ().addAll (m, l);

                    QuollPopup.messageBuilder ()
                        .message (b)
                        .title (backups,_new,confirmpopup,title)
                        .styleClassName (StyleClassNames.BACKUPCREATED)
                        .withViewer (viewer)
                        .closeButton ()
                        .build ();

                    viewer.getPopupById (pid).close ();
/*
TODO REmove
                    ComponentUtils.showMessage (viewer,
                                                StyleClassNames.BACKUPCREATED,
                                                getUILanguageStringProperty (backups,_new,confirmpopup,title),
                                                //"Backup created",
                                                b);
*/
                } catch (Exception e)
                {

                    Environment.logError ("Unable to create backup of project: " +
                                          proj,
                                          e);

                    ComponentUtils.showErrorMessage (viewer,
                                                     getUILanguageStringProperty (backups,_new,actionerror));
                                              //"Unable to create backup.");

                }

            })
            .build ();
/*
TODO Remove
        ComponentUtils.createQuestionPopup (getUILanguageStringProperty (backups,_new,LanguageStrings.popup,title),
                                            StyleClassNames.CREATEBACKUP,
                                            getUILanguageStringProperty (Arrays.asList (backups,_new,LanguageStrings.popup,text),
                                                        //"Please confirm you wish to create a backup of {project} <b>%s</b>.",
                                                                         proj.getName ()),
                                            getUILanguageStringProperty (backups,_new,LanguageStrings.popup,buttons,confirm),
                                            //"Yes, create it",
                                            getUILanguageStringProperty (backups,_new,LanguageStrings.popup,buttons,cancel),
                                            //null,
                                            fev ->
                                            {

                                                try
                                                {

                                                    Path p = BackupsManager.createBackupForProject (proj,
                                                                                                    false);

                                                    VBox b = new VBox ();

                                                    BasicHtmlTextFlow m = BasicHtmlTextFlow.builder ()
                                                        .styleClassName (StyleClassNames.MESSAGE)
                                                        .text (getUILanguageStringProperty (Arrays.asList (backups,_new,confirmpopup,text),
                                                                                            p.getParent ().toUri ().toString (),
                                                                                            p.toString ()))
                                                        .withHandler (viewer)
                                                        .build ();

                                                    QuollHyperlink l = QuollHyperlink.builder ()
                                                        .styleClassName (StyleClassNames.VIEW)
                                                        .label (getUILanguageStringProperty (backups,_new,confirmpopup,labels,view))
                                                        .onAction (ev ->
                                                        {

                                                            BackupsManager.showForProject (proj,
                                                                                           viewer);

                                                        })
                                                        .build ();

                                                    b.getChildren ().addAll (m, l);

                                                    ComponentUtils.showMessage (viewer,
                                                                                StyleClassNames.BACKUPCREATED,
                                                                                getUILanguageStringProperty (backups,_new,confirmpopup,title),
                                                                                //"Backup created",
                                                                                b);

                                                } catch (Exception e)
                                                {

                                                    Environment.logError ("Unable to create backup of project: " +
                                                                          proj,
                                                                          e);

                                                    ComponentUtils.showErrorMessage (viewer,
                                                                                     getUILanguageStringProperty (backups,_new,actionerror));
                                                                              //"Unable to create backup.");

                                                }

                                            },
                                            viewer);
*/
    }

    public static Path createBackupForProject (Project p,
                                               boolean noPrune)
                                        throws Exception
    {

        return BackupsManager.createBackupForProject (Environment.getProjectInfo (p),
                                                      noPrune);

    }

    public static Path createBackupForProject (ProjectInfo p,
                                               boolean     noPrune)
                                        throws Exception
    {

        boolean closePool = false;

        AbstractProjectViewer pv = Environment.getProjectViewer (p);

        ObjectManager om = null;
        Project proj = null;

        if (pv != null)
        {

            // Load up the chapters.
            om = pv.getObjectManager ();

            proj = pv.getProject ();

        } else {

            if ((p.isEncrypted ())
                &&
                (p.getFilePassword () == null)
               )
            {

                throw new IllegalArgumentException ("The file password must be specified for encrypted projects when the project is not already open.");

            }

            // Open the project.
            try
            {

                om = Environment.getProjectObjectManager (p,
                                                          p.getFilePassword ());

                proj = om.getProject ();

            } catch (Exception e) {

                // Can't open the project.
                if (om != null)
                {

                    om.closeConnectionPool ();

                }

                throw e;

            }

            proj.setBackupDirectory (p.getBackupDirPath ().toFile ());

            closePool = true;

        }

        int count = proj.getBackupsToKeepCount ();

        try
        {

            Path f = om.createBackup (proj,
                                      (noPrune ? -1 : count));

            p.addBackupPath (f);

            Environment.fireUserProjectEvent (proj,
                                              ProjectEvent.Type.backups,
                                              ProjectEvent.Action._new,
                                              proj);

            return f;

        } finally {

            if (closePool)
            {

                if (om != null)
                {

                    om.closeConnectionPool ();

                }

            }

        }

    }

}
