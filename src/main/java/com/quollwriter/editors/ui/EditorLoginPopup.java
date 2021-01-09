package com.quollwriter.editors.ui;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.event.*;
import javafx.scene.input.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.popups.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class EditorLoginPopup extends PopupContent<AbstractViewer>
{

    public static final String POPUP_ID = "editorlogin";

    private Runnable onLogin = null;
    private Runnable onCancel = null;
    private Consumer<Exception> onError = null;
    private Runnable onClose = null;
    private QuollTextField emailField = null;
    private PasswordField passwordField = null;
    private QuollCheckBox autoLogin = null;
    private QuollCheckBox savePwd = null;
    private boolean inited = false;
    private Form form = null;

    public EditorLoginPopup ()
    {

        super (null);

        Form.Builder fb = Form.builder ()
            .confirmButton (editors,login,LanguageStrings.popup,buttons,login)
            .cancelButton (editors,login,LanguageStrings.popup,buttons,cancel);

        this.emailField = QuollTextField.builder ()
            //.text (acc.getEmail ())
            .build ();

        String upwd = EditorsEnvironment.getEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME);

        this.passwordField = new PasswordField ();
        //passwordField.setText (upwd);

        this.savePwd = QuollCheckBox.builder ()
            .label (editors,login,LanguageStrings.popup,labels,savepassword)
            .selected (upwd != null)
            .onAction (ev ->
            {

                if (this.savePwd.isSelected ())
                {

                    this.savePwd.setSelected (false);

                    java.util.List<String> prefix = Arrays.asList (editors,login,savepasswordwarningpopup);

                    // Show the warning.
                    QuollPopup qp = QuollPopup.yesConfirmTextEntryBuilder ()
                        .styleClassName (StyleClassNames.WARNING)
                        .title (Utils.newList (prefix,title))
                        .description (Utils.newList (prefix,text))
                        .confirmButtonLabel (Utils.newList (prefix,buttons,confirm))
                        .cancelButtonLabel (Utils.newList (prefix,buttons,cancel))
                        .onConfirm (eev ->
                        {

                            this.savePwd.setSelected (true);

                        })
                        .onCancel (eev ->
                        {

                            this.savePwd.setSelected (false);

                        })
                        .build ();

                } else {

                    try
                    {

                        EditorsEnvironment.removeEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME);

                    } catch (Exception e) {

                        Environment.logError ("Unable to remove editors property: " +
                                              Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME,
                                              e);

                    }

                }

            })
            .build ();

        this.autoLogin = QuollCheckBox.builder ()
            .label (editors,login,LanguageStrings.popup,labels,autologin)
            .selected (EditorsEnvironment.isAutoLoginAtQWStart ())
            .onAction (ev ->
            {

                try
                {

                    EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME,
                                                           this.autoLogin.isSelected ());

                } catch (Exception e) {

                    Environment.logError ("Unable to set to login at start",
                                          e);

                }

            })
            .build ();

        fb.item (getUILanguageStringProperty (Arrays.asList (editors,login,LanguageStrings.popup,labels,youremail)),
                 this.emailField);

        fb.item (getUILanguageStringProperty (Arrays.asList (editors,login,LanguageStrings.popup,labels,password)),
                 this.passwordField);

        fb.item (savePwd);
        fb.item (autoLogin);

        this.form = fb.build ();

        this.checkButtons ();

        EventHandler<KeyEvent> onKeyPressed = ev ->
        {

            this.checkButtons ();

        };

        this.emailField.addEventHandler (KeyEvent.KEY_PRESSED,
                                         onKeyPressed);
        this.passwordField.addEventHandler (KeyEvent.KEY_PRESSED,
                                            onKeyPressed);

        this.form.addEventHandler (Form.FormEvent.CANCEL_EVENT,
                                   ev ->
        {

            this.close ();

            if (this.onCancel != null)
            {

                Environment.scheduleImmediately (this.onCancel);

            }

        });

        this.form.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
                                   ev ->
        {

            if (this.form.getConfirmButton ().isDisabled ())
            {

                return;

            }

            try
            {

                if (!this.savePwd.isSelected ())
                {

                    EditorsEnvironment.removeEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME);

                }

                EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME,
                                                       new com.gentlyweb.properties.BooleanProperty (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME,
                                                                                                     this.autoLogin.isSelected ()));

            } catch (Exception e) {

                Environment.logError ("Unable to update properties",
                                      e);

            }

            EditorsEnvironment.setLoginCredentials (this.emailField.getText ().trim ().toLowerCase (),
                                                    this.passwordField.getText ());

            this.form.showLoading (getUILanguageStringProperty (editors,login,LanguageStrings.popup,loading));
            this.form.hideError ();

            if (this.onLogin != null)
            {

                Environment.scheduleImmediately (onLogin);

            }

        });

        this.getChildren ().add (this.form);

    }

    private void checkButtons ()
    {

        this.form.getConfirmButton ().setDisable (false);

        if (this.emailField != null)
        {

            String email = this.emailField.getText ().trim ();

            int atInd = email.indexOf ('@');

            int dotInd = email.indexOf ('.',
                                        atInd);

            if ((email.length () == 0)
                ||
                (atInd == -1)
                ||
                (email.length () - 1 == dotInd)
                ||
                (dotInd == -1)
               )
            {

                this.form.getConfirmButton ().setDisable (true);

            }

        }

        String pwd = this.passwordField.getText ();

        if ((pwd.length () == 0)
            ||
            (pwd.length () < 8)
           )
        {

            this.form.getConfirmButton ().setDisable (true);

        }

    }

    public void showError (StringProperty v)
    {

        this.form.hideLoading ();
        this.form.showError (v);

    }

    private void initFields ()
    {

        final EditorAccount acc = EditorsEnvironment.getUserAccount ();

        if (acc != null)
        {

            this.emailField.setText (acc.getEmail ());

        }

        String upwd = EditorsEnvironment.getEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME);

        this.passwordField.setText (upwd);

        this.checkButtons ();

    }

    @Override
    public void show ()
    {

        this.initFields ();

        super.show ();

    }

    public void show (AbstractViewer viewer,
                      StringProperty loginReason,
                      Runnable       onLogin,
                      Runnable       onCancel)
    {

        this.initFields ();

        this.onLogin = onLogin;
        this.onCancel = onCancel;

        this.form.hideError ();

        this.form.setDescription (loginReason);

        if (viewer == null)
        {

            viewer = Environment.getFocusedViewer ();

        }

        viewer.showPopup (this.getPopup ());

    }

    public void setOnLogin (Runnable r)
    {

        this.onLogin = r;

    }

    public void setOnCancel (Runnable c)
    {

        this.onCancel = c;
        this.getPopup ().setOnClose (c);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (editors,login,LanguageStrings.popup,title)
            .styleClassName (StyleClassNames.EDITORLOGIN)
            .headerIconClassName (StyleClassNames.CONTACTS)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .removeOnClose (false)
            .build ();

        p.addEventHandler (QuollPopup.PopupEvent.SHOWN_EVENT,
                           ev ->
        {

            UIUtils.forceRunLater (() ->
            {

                this.emailField.requestFocus ();

            });

        });

        return p;

    }

}
