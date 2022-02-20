package com.quollwriter.editors.ui.sidebars;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.*;
import javafx.collections.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.text.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ProjectCommentsSideBar extends ProjectSentReceivedSideBar<ProjectCommentsMessage, ProjectCommentsViewer>
{

    private QuollHyperlink otherCommentsLabel = null;

    public ProjectCommentsSideBar (ProjectCommentsViewer  viewer,
                                   ProjectCommentsMessage message)
    {

        super (viewer,
               message);

    }

    @Override
    public StringProperty getTitle ()
    {

        return getUILanguageStringProperty (editors,projectcomments,LanguageStrings.sidebar,comments,(this.message.isSentByMe () ? sent : received),title);

        //return String.format ("{Comments} %s",
        //                      (this.message.isSentByMe () ? "to" : "from"));

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.COMMENTS;

    }

    @Override
    public StringProperty getItemsTitle ()
    {

        return getUILanguageStringProperty (objectnames,plural,comment);

    }

    @Override
    public String getStyleSheet ()
    {

        return "projectcomments";

    }

    @Override
    public Node getMessageDetails (ProjectCommentsMessage message)
    {

        List<String> prefix = Arrays.asList (editors,projectcomments,LanguageStrings.sidebar,comments,labels);

        ProjectVersion projVer = message.getProjectVersion ();

        String verName = projVer.getName ();

        final String notes = (projVer.getDescription () != null ? projVer.getDescription ().getText () : null);

        Form.Builder fb = Form.builder ()
            .layoutType (Form.LayoutType.column)
            .item (getUILanguageStringProperty (Utils.newList (prefix,(this.message.isSentByMe () ? sent : received))),
                   UILanguageStringsManager.createStringPropertyWithBinding (() ->
                   {

                       return Environment.formatDateTime (message.getWhen ());

                   }));

        if (verName != null)
        {

            fb.item (getUILanguageStringProperty (Utils.newList (prefix,version)),
                     new SimpleStringProperty (verName));

        }

        final String genComments = message.getGeneralComment ();

        if (genComments != null)
        {

            String commText = genComments;

            TextIterator ti = new TextIterator (commText);

            if (ti.getSentenceCount () > 1)
            {

                commText = ti.getFirstSentence ().getText ();

                commText += getUILanguageStringProperty (prefix,more).getValue ();
                //"<br /><a href='#'>More, click to view all.</a>";

            }

            QuollTextView notesT = QuollTextView.builder ()
                .inViewer (this.viewer)
                .text (new SimpleStringProperty (commText))
                .build ();

            notesT.setOnMouseClicked (ev ->
            {

                QuollPopup.messageBuilder ()
                    .title (editors,projectcomments,LanguageStrings.sidebar,comments,notes,popup,title)
                    .message (genComments)
                    .inViewer (this.viewer)
                    .build ();

            });

            fb.item (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.notes)),
                     notesT);

        }

        Node n = fb.build ();

        VBox content = new VBox ();

        Set<EditorMessage> messages = this.getMessage ().getEditor ().getMessages (this.getMessage ().getForProjectId (),
                                                                                   ProjectCommentsMessage.MESSAGE_TYPE);

        int otherC = 0;

        for (EditorMessage m : messages)
        {

            ProjectCommentsMessage pcm = (ProjectCommentsMessage) m;

            if (!pcm.equals (this.getMessage ()))
            {

                otherC += pcm.getComments ().size ();

            }

        }

        this.otherCommentsLabel = QuollHyperlink.builder ()
            .styleClassName (StyleClassNames.VIEW)
            .label (getUILanguageStringProperty (Arrays.asList (editors,projectcomments,LanguageStrings.sidebar,comments,labels,othercomments),
                                                 Environment.formatNumber (otherC)))
            .onAction (ev ->
            {

                this.showOtherCommentsSelector ();

            })
            .build ();

        this.otherCommentsLabel.setVisible (otherC > 0);

        content.getChildren ().addAll (n, this.otherCommentsLabel);

        return content;

    }

    private void showOtherCommentsSelector ()
    {

        String popupId = this.message.getProjectVersion ().getId () + "othercomments";

        QuollPopup qp = viewer.getPopupById (popupId);

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        final List<ProjectCommentsMessage> pcms = new ArrayList<> ();

        Set<EditorMessage> messages = this.getMessage ().getEditor ().getMessages (this.getMessage ().getForProjectId (),
                                                                                   ProjectCommentsMessage.MESSAGE_TYPE);

        for (EditorMessage m : messages)
        {

            ProjectCommentsMessage pcm = (ProjectCommentsMessage) m;

            if (pcm.equals (this.getMessage ()))
            {

                continue;

            }

            pcms.add (pcm);

        }

        if (pcms.size () == 0)
        {

            return;

        }

        if (pcms.size () == 1)
        {

            EditorsUIUtils.showProjectComments (pcms.iterator ().next (),
                                                this.viewer,
                                                null);

        } else {

            ShowObjectSelectPopup.<ProjectCommentsMessage>builder ()
                .withViewer (this.viewer)
                .title (getUILanguageStringProperty (editors,projectcomments,LanguageStrings.sidebar,comments,othercomments,popup,title))
                .styleClassName (StyleClassNames.OTHERCOMMENTS)
                .popupId (popupId)
                .objects (FXCollections.observableList (pcms))
                .cellProvider ((obj, popupContent) ->
                {

                    int c = obj.getComments ().size ();

                    VBox b = new VBox ();

                    b.setOnMouseClicked (ev ->
                    {

                        if (ev.getButton () != MouseButton.PRIMARY)
                        {

                            return;

                        }

                        EditorsUIUtils.showProjectComments (obj,
                                                            this.viewer,
                                                            null);

                        popupContent.close ();

                    });

                    UIUtils.setTooltip (b,
                                        getUILanguageStringProperty (editors,projectcomments,LanguageStrings.sidebar,comments,othercomments,item,tooltip));


                    QuollLabel h = QuollLabel.builder ()
                        .styleClassName (StyleClassNames.TITLE)
                        .label (getUILanguageStringProperty (Arrays.asList (editors,projectcomments,LanguageStrings.sidebar,comments,othercomments,item,title),
                                                             Environment.formatNumber (c)))
                        .build ();

                    b.getChildren ().add (h);

                    if (obj.getProjectVersion ().getName () != null)
                    {

                        QuollLabel l = QuollLabel.builder ()
                            .styleClassName (StyleClassNames.VERSION)
                            .label (getUILanguageStringProperty (Arrays.asList (editors,projectcomments,LanguageStrings.sidebar,comments,othercomments,item,version),
                                                                 obj.getProjectVersion ().getName ()))
                            .build ();

                        b.getChildren ().add (l);

                    }

                    // Get the first line of the notes, if provided.
                    String genComm = obj.getGeneralComment ();

                    if (genComm != null)
                    {

                        TextIterator ti = new TextIterator (genComm);

                        if (ti.getSentenceCount () > 1)
                        {

                            genComm = ti.getFirstSentence ().getText ();

                        }

                        QuollTextView notesT = QuollTextView.builder ()
                            .inViewer (this.viewer)
                            .styleClassName (StyleClassNames.COMMENTS)
                            .text (genComm)
                            .build ();

                        b.getChildren ().add (notesT);

                    }

                    QuollLabel info = QuollLabel.builder ()
                        .styleClassName (StyleClassNames.WHEN)
                        .label (getUILanguageStringProperty (Arrays.asList (editors,projectcomments,LanguageStrings.sidebar,comments,othercomments,item,(this.getMessage ().isSentByMe () ? sent : received)),
                                                                                    //"Received: %s",
                                                             Environment.formatDate (obj.getWhen ())))
                        .build ();

                    b.getChildren ().add (info);

                    return b;

                })
                .build ()
                .show ();

            }

    }

}
