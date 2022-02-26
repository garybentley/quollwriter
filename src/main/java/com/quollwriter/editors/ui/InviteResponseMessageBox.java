package com.quollwriter.editors.ui;

import java.util.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.embed.swing.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

// Use an annotation?
//@MessageBox(messageClass=InviteResponseMessage)
public class InviteResponseMessageBox extends MessageBox<InviteResponseMessage>
{

    private VBox responseBox = null;

    public InviteResponseMessageBox (InviteResponseMessage mess,
                                     AbstractViewer        viewer)
    {

        super (mess,
               viewer);

        if (!this.message.isDealtWith ())
        {

            // Show the response.
            this.responseBox = new VBox ();

            this.getChildren ().add (this.responseBox);
            this.getStyleClass ().add (StyleClassNames.INVITEMESSAGE);

            this.getChildren ().add (Header.builder ()
                .title (editors,messages,inviteresponse,undealtwith,(this.message.isAccepted () ? accepted : rejected),title)
                .iconClassName (this.message.isAccepted () ? StyleClassNames.ACCEPTED : StyleClassNames.REJECTED)
                .build ());

            if (this.message.isAccepted ())
            {

                if ((this.message.getEditorName () != null)
                    ||
                    (this.message.getEditorAvatar () != null)
                   )
                {

                    this.responseBox.getChildren ().add (QuollTextView.builder ()
                        .text (getUILanguageStringProperty (editors,messages,inviteresponse,undealtwith,accepted,text))
                        .inViewer (this.viewer)
                        .build ());

                    HBox editorInfo = new HBox ();

                    this.responseBox.getChildren ().add (editorInfo);

                    if (this.message.getEditorAvatar () != null)
                    {

                        editorInfo.getChildren ().add (UIUtils.getImageView (this.message.getEditorAvatar ()));

                    }

                    if (this.message.getEditorName () != null)
                    {

                        editorInfo.getChildren ().add (QuollLabel.builder ()
                            .label (this.message.getEditorName ())
                            .build ());

                    }

                }

            }

            final EditorEditor ed = this.message.getEditor ();

            this.responseBox.getChildren ().add (QuollButtonBar.builder ()
                .button (QuollButton.builder ()
                    .label (editors,messages,inviteresponse,undealtwith,buttons,confirm)
                    .onAction (ev ->
                    {

                        try
                        {

                            if (this.message.isAccepted ())
                            {

                                ed.setEditorStatus (EditorEditor.EditorStatus.current);

                                if (this.message.getEditorName () != null)
                                {

                                    ed.setName (this.message.getEditorName ());

                                }

                                if (this.message.getEditorAvatar () != null)
                                {

                                    ed.setAvatar (this.message.getEditorAvatar ());

                                }

                                EditorsEnvironment.updateEditor (ed);

                                // Is this response for an invite message or just out of the blue from a web service invite?
                                if (!EditorsEnvironment.hasSentMessageOfTypeToEditor (ed,
                                                                                      InviteMessage.MESSAGE_TYPE))
                                {

                                    EditorsEnvironment.sendUserInformationToEditor (ed,
                                                                                    null,
                                                                                    null,
                                                                                    null);

                                }

                            } else {

                                ed.setEditorStatus (EditorEditor.EditorStatus.rejected);

                                EditorsEnvironment.updateEditor (ed);

                            }

                            this.message.setDealtWith (true);

                            EditorsEnvironment.updateMessage (this.message);

                            this.responseBox.setVisible (false);

                        } catch (Exception e) {

                            Environment.logError ("Unable to update editor: " +
                                                  ed,
                                                  e);

                            ComponentUtils.showErrorMessage (this.viewer,
                                                             getUILanguageStringProperty (editors,editor,edit,actionerror));
                                                      //"Unable to update {editor}, please contact Quoll Writer support for assistance.");

                            return;

                        }

                    })
                    .build ())
                .build ());

            return;

        }

        this.getChildren ().add (Header.builder ()
            .iconClassName (this.message.isAccepted () ? StyleClassNames.ACCEPTED : StyleClassNames.REJECTED)
            .title (editors,messages,inviteresponse,dealtwith,(this.message.isAccepted () ? LanguageStrings.accepted : rejected),title)
            .build ());

    }

    public boolean isAutoDealtWith ()
    {

        return false;

    }

}
