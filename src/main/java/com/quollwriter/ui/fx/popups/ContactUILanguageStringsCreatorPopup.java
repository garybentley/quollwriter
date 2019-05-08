package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;

import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ContactUILanguageStringsCreatorPopup extends PopupContent
{

    public static final String POPUP_ID = "contactuilangstringscreator";

    private QuollTextArea desc = null;
    private QuollTextField email = null;

    public ContactUILanguageStringsCreatorPopup (AbstractViewer viewer)
    {

        super (viewer);

        final ContactUILanguageStringsCreatorPopup _this = this;

        List<String> prefix = Arrays.asList (uilanguage,feedback);

        this.desc = QuollTextArea.builder ()
            .placeholder (getUILanguageStringProperty (Utils.newList (prefix, LanguageStrings.popup,message,tooltip),
                                                       UILanguageStringsManager.getCurrentUILanguageStrings ().getNativeName ()))
            .maxChars (10000)
            //.autoGrabFocus (true)
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        VBox.setVgrow (this.desc,
                       Priority.ALWAYS);

        this.email = QuollTextField.builder ()
            .styleClassName (StyleClassNames.EMAIL)
            .build ();

        Form f = Form.builder ()
            .description (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.popup,text),
                                                       UILanguageStringsManager.getCurrentUILanguageStrings ().getNativeName ()))
            .confirmButton (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.popup,buttons,send)))
            .cancelButton (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.popup,buttons,cancel)))
            .item (getUILanguageStringProperty (Utils.newList (prefix, LanguageStrings.popup,LanguageStrings.message,text)),
                   this.desc)
            .item (getUILanguageStringProperty (Utils.newList (prefix, LanguageStrings.popup,LanguageStrings.email,text)),
                   this.email)
            .withViewer (this.viewer)
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

            Map details = new HashMap ();
            details.put ("details",
                         desc.getText ());
            details.put ("email",
                         email.getText ());
            details.put ("uilanguageid",
                         UILanguageStringsManager.getCurrentUILanguageStrings ().getId ());

            try
            {

                Environment.sendMessageToSupport ("uilanguage",
                                                  details,
                                                  () ->
                {

                    _this.getPopup ().close ();

                    desc.setText ("");

                    ComponentUtils.showMessage (_this.getViewer (),
                                                getUILanguageStringProperty (Utils.newList (prefix,confirmpopup, LanguageStrings.title)),
                                                getUILanguageStringProperty (Utils.newList (prefix,confirmpopup,text)));

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

    public static String getPopupId ()
    {

        return POPUP_ID + UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME);

    }

    @Override
    public QuollPopup createPopup ()
    {

        List<String> prefix = Arrays.asList (uilanguage,feedback);

        QuollPopup p = QuollPopup.builder ()
            .title (Utils.newList (prefix,LanguageStrings.popup,title))
            .styleClassName (StyleClassNames.CONTACTUILANGCREATOR)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID + UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME))
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.requestFocus ();

        return p;

    }

}
