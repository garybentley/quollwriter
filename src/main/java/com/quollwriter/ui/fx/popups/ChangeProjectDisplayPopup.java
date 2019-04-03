package com.quollwriter.ui.fx.popups;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import javafx.event.*;
import javafx.scene.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.application.*;
import javafx.collections.*;
import javafx.collections.transformation.*;

import com.quollwriter.*;
import com.quollwriter.db.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.uistrings.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ChangeProjectDisplayPopup extends PopupContent
{

    public static final String POPUP_ID = "changeprojectdisplay";

    public ChangeProjectDisplayPopup (AbstractViewer viewer)
    {

        super (viewer);

        final ChangeProjectDisplayPopup _this = this;

        VBox b = new VBox ();
        this.getChildren ().add (b);

        List<String> prefix = Arrays.asList (allprojects,changedisplay,LanguageStrings.popup);

        b.getChildren ().add (BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .text (getUILanguageStringProperty (Utils.newList (prefix,text)))
            .build ());

        SimpleStringProperty formatProp = new SimpleStringProperty ();
        formatProp.setValue (UserProperties.getProjectInfoFormat ());

        // TODO Add controls for bold/italic?  Add menu?  Add tag menu?
        QuollTextArea format = QuollTextArea.builder ()
            .styleClassName (StyleClassNames.FORMAT)
            .text (UserProperties.getProjectInfoFormat ())
            .build ();
        b.getChildren ().add (format);
        formatProp.bind (format.textProperty ());

        b.getChildren ().add (Header.builder ()
            .styleClassName (StyleClassNames.EXAMPLE)
            .title (getUILanguageStringProperty (Utils.newList (prefix,example,title)))
            .build ());

        // TODO Wrap in another box to allow for centering/padding.
        VBox exb = new VBox ();

        exb.getStyleClass ().addAll (StyleClassNames.PROJECT, StyleClassNames.NORMAL, StyleClassNames.EXAMPLE);

        ProjectInfo exp = new ProjectInfo ();

        exp.setProjectDirectory (javax.swing.filechooser.FileSystemView.getFileSystemView ().getDefaultDirectory ());
        exp.setName (Environment.getUIString (prefix,
                                              LanguageStrings.example,
                                              LanguageStrings.name));
                                                 //Environment.replaceObjectNames ("My {Project}"));
        exp.setStatus (Environment.getUIString (prefix,
                                                LanguageStrings.example,
                                                LanguageStrings.status));
                                                 //"In Progress");
        exp.setLastEdited (new Date (System.currentTimeMillis () - (7 * Constants.DAY_IN_MILLIS)));
        exp.addStatistic (ProjectInfo.Statistic.chapterCount,
                          5);
        exp.addStatistic (ProjectInfo.Statistic.wordCount,
                          12123);

        exp.addStatistic (ProjectInfo.Statistic.gunningFogIndex,
                          3);
        exp.addStatistic (ProjectInfo.Statistic.fleschReadingEase,
                          73);
        exp.addStatistic (ProjectInfo.Statistic.fleschKincaidGradeLevel,
                          8);
        exp.addStatistic (ProjectInfo.Statistic.editedWordCount,
                          5398);

        Header name = Header.builder ()
            .title (exp.nameProperty ())
            .build ();

        SimpleStringProperty infoProp = new SimpleStringProperty ();
        infoProp.bind (Bindings.createStringBinding (() ->
        {

            return UIUtils.getFormattedProjectInfo (exp,
                                                    formatProp.getValue ());

        },
        UILanguageStringsManager.uilangProperty (),
        formatProp));

        BasicHtmlTextFlow info = BasicHtmlTextFlow.builder ()
            .text (infoProp)
            .styleClassName (StyleClassNames.INFO)
            .withViewer (this.getViewer ())
            .build ();

        exb.getChildren ().addAll (name, info);

        b.getChildren ().add (exb);

        QuollButtonBar bb = QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.APPLY)
                        .label (getUILanguageStringProperty (Utils.newList (prefix,buttons,save)))
                        .onAction (ev ->
                        {

                            UserProperties.setProjectInfoFormat (format.getText ());

                            _this.close ();

                        })
                        .build ())
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.OTHER)
                        .label (getUILanguageStringProperty (Utils.newList (prefix,buttons,_default)))
                        .onAction (ev ->
                        {

                            format.setText (UserProperties.get (Constants.DEFAULT_PROJECT_INFO_FORMAT));

                        })
                        .build ())
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
                        .label (getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)))
                        .onAction (ev ->
                        {

                            _this.close ();

                        })
                        .build ())
            .build ();

        b.getChildren ().add (bb);
/*
        final BackupsManager _this = this;

        BasicHtmlTextFlow desc = BasicHtmlTextFlow.builder ()
            .text (getUILanguageStringProperty (Arrays.asList (backups,text),
                                                proj.nameProperty ()))
            .withViewer (viewer)
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
            .withViewer (viewer)
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

        // Ugh, the compiler needs more help here.
        proj.backupPathsProperty ().addListener ((SetChangeListener<Path>) (ev ->
        {

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

        }));

        this.update ();
*/
    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (allprojects,changedisplay,LanguageStrings.popup,title)
            .styleClassName (StyleClassNames.CHANGEPROJECTDISPLAY)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();
        p.requestFocus ();

        // TODO Fire an event?
        /*
        Environment.fireUserProjectEvent (this.viewer,
                                          ProjectEvent.Type.?,
                                          ProjectEvent.Action.show);
*/
        return p;

    }

}
