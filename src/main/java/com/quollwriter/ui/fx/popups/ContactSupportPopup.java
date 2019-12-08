package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;

import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ContactSupportPopup extends PopupContent
{

    public static final String POPUP_ID = "contactsupport";

    private QuollTextArea desc = null;
    private QuollTextField email = null;

    public ContactSupportPopup (AbstractViewer viewer)
    {

        super (viewer);

        final ContactSupportPopup _this = this;

        List<String> prefix = Arrays.asList (project,actions,contactsupport);

        this.desc = QuollTextArea.builder ()
            .placeholder (getUILanguageStringProperty (Utils.newList (prefix, LanguageStrings.popup,message,tooltip)))
            .maxChars (10000)
            .withViewer (viewer)
            //.autoGrabFocus (true)
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        javafx.scene.layout.VBox.setVgrow (this.desc,
        javafx.scene.layout.Priority.ALWAYS);

        this.email = QuollTextField.builder ()
            .styleClassName (StyleClassNames.EMAIL)
            .build ();

        Form f = Form.builder ()
            .description (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.popup,text)))
            .confirmButton (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.popup,buttons,send)))
            .cancelButton (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.popup,buttons,cancel)))
            .item (getUILanguageStringProperty (Utils.newList (prefix, LanguageStrings.popup,LanguageStrings.message,text)),
                   this.desc)
            .item (getUILanguageStringProperty (Utils.newList (prefix, LanguageStrings.popup,LanguageStrings.email,text)),
                   this.email)
            .withHandler (this.viewer)
            .build ();

        f.setOnCancel (ev -> _this.getPopup ().close ());

        f.setOnConfirm (ev ->
        {

            ev.getForm ().hideError ();

            StringProperty emErr = Utils.checkEmail (email.getText ());

            if (emErr != null)
            {

                ev.getForm ().showError (emErr);

                return;

            }

            if (desc.getText ().trim ().equals (""))
            {

                ev.getForm ().showError (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.popup,errorlabel)));

                return;

            }

            _this.getPopup ().close ();

            // Send the message.
            Map details = new HashMap ();
            details.put ("details",
                         "Email: " + email.getText () + "\nDetails: " + desc.getText ());
            details.put ("email",
                         email.getText ());

            try
            {

                Environment.sendMessageToSupport ("contact",
                                                  details,
                                                  () ->
                {

                    desc.setText ("");

                    QuollPopup.messageBuilder ()
                        .withViewer (_this.getViewer ())
                        .title (prefix,confirmpopup,title)
                        .message (prefix,confirmpopup,text)
                        .build ();

                    _this.getViewer ().fireProjectEvent (ProjectEvent.Type.contact,
                                                         ProjectEvent.Action.submit);

                });

            } catch (Exception e)
            {

                Environment.logError ("Unable to send message to support",
                                      e);

                ComponentUtils.showErrorMessage (_this.getViewer (),
                                                 getUILanguageStringProperty (prefix,actionerror));
                                          //"Unable to send message.");

            }

        });

        this.getChildren ().add (f);

    }

    @Override
    public QuollPopup createPopup ()
    {

        List<String> prefix = Arrays.asList (project,actions,contactsupport);

        QuollPopup p = QuollPopup.builder ()
            .title (Utils.newList (prefix,LanguageStrings.popup,title))
            .styleClassName (StyleClassNames.CONTACTSUPPORT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.requestFocus ();

        return p;

    }

}
