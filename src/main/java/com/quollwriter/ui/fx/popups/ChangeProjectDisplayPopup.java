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

        QuollTextArea format = QuollTextArea.builder ()
            .styleClassName (StyleClassNames.FORMAT)
            .text (UserProperties.getProjectInfoFormat ())
            .withViewer (viewer)
            .formattingEnabled (false)
            .build ();
        b.getChildren ().add (format);

        b.getChildren ().add (Header.builder ()
            .styleClassName (StyleClassNames.EXAMPLE)
            .title (getUILanguageStringProperty (Utils.newList (prefix,example,title)))
            .build ());

        // TODO Wrap in another box to allow for centering/padding.
        VBox exb = new VBox ();

        exb.getStyleClass ().addAll (StyleClassNames.PROJECT, StyleClassNames.NORMAL, StyleClassNames.EXAMPLE);

        ProjectInfo exp = new ProjectInfo ();

        exp.setProjectDirectory (javax.swing.filechooser.FileSystemView.getFileSystemView ().getDefaultDirectory ().toPath ());
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
            .iconClassName (StyleClassNames.PROJECT)
            .build ();

        SimpleStringProperty infoProp = new SimpleStringProperty ();
        infoProp.bind (UILanguageStringsManager.createStringBinding (() ->
        {

            return UIUtils.getFormattedProjectInfo (exp,
                                                    format.getText ());
                                                    //UserProperties.getProjectInfoFormat ());

        }));
        //UserProperties.projectInfoFormatProperty ()));

        BasicHtmlTextFlow info = BasicHtmlTextFlow.builder ()
            .text (infoProp)
            .styleClassName (StyleClassNames.INFO)
            .withHandler (this.getViewer ())
            .build ();

        format.getTextEditor ().multiRichChanges ().subscribe (ch ->
        {

            info.setText (UIUtils.getFormattedProjectInfo (exp,
                                                           format.getText ()));

        });

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

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (allprojects,changedisplay,LanguageStrings.popup,title)
            .styleClassName (StyleClassNames.CHANGEPROJECTDISPLAY)
            .styleSheet (StyleClassNames.CHANGEPROJECTDISPLAY)
            .headerIconClassName (StyleClassNames.EDIT)
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
