package com.quollwriter.editors.ui;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class EditorRemovedMessageBox extends MessageBox<EditorRemovedMessage>
{

    private VBox responseBox = null;

    public EditorRemovedMessageBox (EditorRemovedMessage mess,
                                    AbstractViewer       viewer)
    {

        super (mess,
               viewer);

        this.getStyleClass ().add (StyleClassNames.EDITORREMOVED);

        this.binder.addChangeListener (mess.dealtWithProperty (),
                                       (pr, oldv, newv) ->
        {

            if (this.message.isDealtWith ())
            {

                if (this.responseBox != null)
                {

                    this.responseBox.setVisible (false);

                }

            }

        });

        StringProperty title = getUILanguageStringProperty (editors,messages,contactremoved,(this.message.isSentByMe () ? sent : received),LanguageStrings.title);
        StringProperty text = getUILanguageStringProperty (Arrays.asList (editors,messages,contactremoved,(this.message.isSentByMe () ? sent : received),LanguageStrings.text),
                                                           this.message.getEditor ().mainNameProperty ());

        this.getChildren ().add (Header.builder ()
            .title (title)
            .iconClassName (StyleClassNames.DELETE)
            .styleClassName (StyleClassNames.SUBTITLE)
            .build ());

        this.getChildren ().add (QuollTextView.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .text (text)
            .inViewer (this.viewer)
            .build ());

        if (!this.message.isDealtWith ())
        {

            // Show the response.
            this.responseBox = new VBox ();

            this.getChildren ().add (this.responseBox);

            this.responseBox.getChildren ().add (QuollTextView.builder ()
                .text (getUILanguageStringProperty (Arrays.asList (editors,messages,contactremoved,received,undealtwith,LanguageStrings.text),
                                                                            //"Clicking on the button below will remove <b>%s</b> from your list of current {contacts}.  You can still get access to them in the options menu of the {Contacts} sidebar via the <b>View the previous {contacts}</b> item.",
                                                                   this.message.getEditor ().mainNameProperty ()))
                .inViewer (this.viewer)
                .styleClassName (StyleClassNames.DESCRIPTION)
                .build ());

            final EditorEditor ed = this.message.getEditor ();

            this.getChildren ().add (QuollButton.builder ()
                .label (editors,messages,contactremoved,received,undealtwith,buttons,confirm)
                .onAction (ev ->
                {

                    try
                    {

                        // Unsubscribe.
                        EditorsEnvironment.getMessageHandler ().unsubscribeFromEditor (ed);

                        // For all projects, if they are a project editor then set them as previous.
                        EditorsEnvironment.removeEditorAsProjectEditorForAllProjects (ed);

                        // Uupdate the editor to be previous.
                        ed.setEditorStatus (EditorEditor.EditorStatus.previous);

                        EditorsEnvironment.updateEditor (ed);

                        this.responseBox.setVisible (false);

                        this.message.setDealtWith (true);

                        EditorsEnvironment.updateMessage (this.message);

                        // Offer to remove any projects we are editing for them.
                        EditorsUIUtils.showDeleteProjectsForEditor (this.viewer,
                                                                    this.message.getEditor (),
                                                                    null);

                    } catch (Exception e) {

                        Environment.logError ("Unable to update editor: " +
                                              ed,
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (editors,messages,update,actionerror));
                                                  //"Unable to update {contact}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                })
                .build ());

        }

    }

    public boolean isAutoDealtWith ()
    {

        return false;

    }

}
