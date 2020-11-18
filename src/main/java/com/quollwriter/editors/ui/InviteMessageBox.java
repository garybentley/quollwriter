package com.quollwriter.editors.ui;

import java.util.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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

public class InviteMessageBox extends MessageBox<InviteMessage>
{

    private VBox responseBox = null;

    public InviteMessageBox (InviteMessage  mess,
                             AbstractViewer viewer)
    {

        super (mess,
               viewer);

        StringProperty title = getUILanguageStringProperty (editors,messages,invite,sent,LanguageStrings.title);
        //"Sent invitation";

        if (!this.message.isSentByMe ())
        {

            title = getUILanguageStringProperty (editors,messages,invite,received,LanguageStrings.title);

        }

        this.binder.addChangeListener (this.message.dealtWithProperty (),
                                       (pr, oldv, newv) ->
        {

            this.responseBox.setVisible (!this.message.isDealtWith ());

        });

        this.getStyleClass ().add (StyleClassNames.INVITEMESSAGE);

        this.getChildren ().add (Header.builder ()
            .title (title)
            .build ());

        if ((!this.message.isDealtWith ())
            &&
            (!this.message.isSentByMe ())
           )
        {

           this.responseBox = new VBox ();

           this.getChildren ().add (this.responseBox);

           this.responseBox.getChildren ().add (QuollLabel.builder ()
            .label (editors,messages,invite,received,response)
            .styleClassName (StyleClassNames.SUBTITLE)
            .build ());

           QuollButtonBar bb = QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                .label (editors,messages,invite,received,buttons,LanguageStrings.confirm,text)
                .tooltip (editors,messages,invite,received,buttons,LanguageStrings.confirm,tooltip)
                .buttonType (ButtonBar.ButtonData.YES)
                .onAction (ev ->
                {

                    this.handleResponse (true);

                })
                .build ())
            .button (QuollButton.builder ()
                .label (editors,messages,invite,received,buttons,LanguageStrings.reject,text)
                .tooltip (editors,messages,invite,received,buttons,LanguageStrings.reject,text)
                .buttonType (ButtonBar.ButtonData.NO)
                .onAction (ev ->
                {

                    this.handleResponse (false);

                })
                .build ())
            .build ();

           this.responseBox.getChildren ().add (bb);

       }

    }

    @Override
    public boolean isShowAttentionBorder ()
    {

        return false;

    }

    @Override
    public boolean isAutoDealtWith ()
    {

        return false;

    }

    private void handleResponse (boolean accepted)
    {

        EditorEditor ed = this.message.getEditor ();

        InviteResponseMessage rm = new InviteResponseMessage (accepted,
                                                              EditorsEnvironment.getUserAccount ());
        rm.setEditor (ed);

        Runnable onComplete = () ->
        {

            this.message.setDealtWith (true);

            try
            {

                EditorsEnvironment.updateMessage (this.message);

            } catch (Exception e) {

                Environment.logError ("Unable to update message: " +
                                      this.message,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (editors,messages,update,actionerror));
                                          //"Unable to update invite, please contact Quoll Writer support for assistance.");

            }

        };

        if (accepted)
        {

            EditorsEnvironment.acceptInvite (ed,
                                             rm,
                                             onComplete);

        } else {

            EditorsEnvironment.rejectInvite (ed,
                                             rm,
                                             onComplete);

        }

    }
}
