package com.quollwriter.editors.ui;

import java.util.*;
import java.util.concurrent.*;

import javafx.scene.layout.*;
import javafx.scene.control.*;

import com.quollwriter.data.editors.*;
import com.quollwriter.events.*;
import com.quollwriter.editors.*;
import com.quollwriter.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class EditorChatBox extends VBox implements EditorInteractionListener
{

    private EditorEditor editor = null;
    private AbstractViewer viewer = null;
    private boolean typingStartedSent = false;
    private Label notification = null;
    private QuollTextArea message = null;
    private ScheduledFuture typingStop = null;
    private ScheduledFuture typingStart = null;

    public EditorChatBox (EditorEditor   ed,
                          AbstractViewer viewer)
    {

        EditorChatBox _this = this;

        this.editor = ed;
        this.viewer = viewer;
        this.getStyleClass ().add (StyleClassNames.CHATBOX);

        EditorsEnvironment.addEditorInteractionListener (this);

        this.message = QuollTextArea.builder ()
            .placeholder (getUILanguageStringProperty (editors,LanguageStrings.editor,sendchat,box,tooltip))
            .build ();

        this.message.setOnTextKeyReleased (ev ->
        {

            if (this.typingStop != null)
            {

                this.typingStop.cancel (true);

            }

            if (this.typingStart != null)
            {

                this.typingStart.cancel (true);

            }

            this.typingStart = Environment.schedule (() ->
            {

                if (!_this.typingStartedSent)
                {

                    _this.typingStartedSent = true;

                    // Send the message.
                    _this.sendStartedTyping ();

                }

            },
            50,
            -1);

            this.typingStop = Environment.schedule (() ->
            {

                _this.typingStartedSent = false;

                // Send stopped message.
                _this.sendStoppedTyping ();

            },
            750,
            -1);

        });

        this.notification = QuollLabel.builder ()
            .styleClassName (StyleClassNames.LOADING)
            .label (editors,LanguageStrings.editor,sendchat,sending)
            .build ();
        HBox.setHgrow (this.notification,
                       Priority.ALWAYS);

        Runnable sendMessage = () ->
        {

            String text = _this.message.getText ().trim ();

            if (text.length () == 0)
            {

                return;

            }

            this.notification.textProperty ().unbind ();
            this.notification.textProperty ().bind (getUILanguageStringProperty (editors,LanguageStrings.editor,sendchat,sending));
            this.notification.setVisible (true);
            HBox.setHgrow (this.notification,
                           Priority.ALWAYS);

            // Add the message to the today list.
            final Date when = new Date ();

            final Date w = Utils.zeroTimeFields (when);

            final EditorChatMessage m = new EditorChatMessage (text,
                                                               true,
                                                               _this.editor,
                                                               when);

            m.setDealtWith (true);

            this.sendStoppedTyping ();

            EditorsEnvironment.sendMessageToEditor (m,
                                                    () ->
            {

                UIUtils.runLater (() ->
                {

                    _this.notification.setVisible (false);

                    _this.message.setText ("");
                    _this.message.requestFocus ();

                });

            },
            () ->
            {

                _this.notification.setVisible (false);

            },
            null);

        };

        UIUtils.addDoOnReturnPressed (this.message,
                                      sendMessage);

        Button save = QuollButton.builder ()
            .iconName (StyleClassNames.SEND)
            .tooltip (editors,LanguageStrings.editor,sendchat,box,buttons,send,tooltip)
            .onAction (ev ->
            {

                UIUtils.runLater (sendMessage);

            })
            .build ();

        Button cancel = QuollButton.builder ()
            .iconName (StyleClassNames.CANCEL)
            .tooltip (editors,LanguageStrings.editor,sendchat,box,buttons,LanguageStrings.cancel,tooltip)
            .onAction (ev ->
            {

                this.message.setText ("");

            })
            .build ();

        HBox buts = new HBox ();
        buts.getStyleClass ().addAll (StyleClassNames.BUTTONS, "controls", "tool-bar");
        buts.getChildren ().addAll (this.notification, save, cancel);

        this.getChildren ().addAll (this.message, buts);

    }

    @Override
    public void handleInteraction (EditorInteractionEvent ev)
    {

        if (this.editor != ev.getEditor ())
        {

            return;

        }

        if (ev.getAction () == InteractionMessage.Action.typing)
        {

            this.showTyping ();

        }

        if (ev.getAction () == InteractionMessage.Action.normal)
        {

            this.notification.setVisible (false);

        }

    }

    public void requestFocus ()
    {

        this.message.requestFocus ();

    }

    private void showTyping ()
    {

        this.notification.textProperty ().unbind ();
        this.notification.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,sendchat,contactistyping),
                                                                             this.editor.mainNameProperty ()));
                                                 //this.editor.getShortName () + " is typing...");
        this.notification.getStyleClass ().clear ();
        this.notification.getStyleClass ().addAll (StyleClassNames.NOTIFICATION, StyleClassNames.TYPING);
        this.notification.setVisible (true);

    }

    private void showSending ()
    {

        this.notification.textProperty ().unbind ();
        this.notification.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,sendchat,sending)));
        this.notification.getStyleClass ().clear ();
        this.notification.getStyleClass ().addAll (StyleClassNames.NOTIFICATION, StyleClassNames.SENDING);
        this.notification.setVisible (true);

    }

    private void sendStartedTyping ()
    {

        EditorsEnvironment.sendInteractionMessageToEditor (InteractionMessage.Action.typing,
                                                           this.editor,
                                                           null);

    }

    private void sendStoppedTyping ()
    {

        EditorsEnvironment.sendInteractionMessageToEditor (InteractionMessage.Action.normal,
                                                      this.editor,
                                                      null);

    }

}
