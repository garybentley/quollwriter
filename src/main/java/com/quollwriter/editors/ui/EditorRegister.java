package com.quollwriter.editors.ui;

import java.io.File;
import java.nio.file.*;

import java.net.*;

import java.text.*;

import java.util.*;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.beans.property.*;

import org.bouncycastle.openpgp.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.editors.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class EditorRegister extends PopupContent<AbstractViewer>
{

    public static final String POPUP_ID = "editorregister";

    private CheckBox tcAgreeField = null;
    private QuollLabel tcError = null;
    private QuollLabel loginError = null;
    private QuollFileField finder = null;
    private QuollTextField emailField = null;
    private QuollPasswordField passwordField = null;
    private QuollTextField nameField = null;
    private ImageSelector avatar = null;
    private QuollLabel saving = null;
    private PGPKeyPair keyPair = null;
    private String messagingUsername = null;
    private String serviceName = null;
    private boolean createCalled = false;
    private boolean login = false;
    private boolean tcsClicked = false;
    private Wizard wizard = null;

    private Map<String, Wizard.Step> steps = new HashMap<> ();

    public EditorRegister (AbstractViewer viewer)
                    throws Exception
    {

        super (viewer);

        final EditorRegister _this = this;

        Thread t = new Thread (new Runnable ()
        {

            public void run ()
            {

                // Create our public/private key.
                try
                {

                    _this.keyPair = EditorsUtils.generateKeyPair ();

                } catch (Exception e) {

                    Environment.logError ("Unable to generate key pair",
                                          e);

                }

            }

        });

        t.setPriority (Thread.MIN_PRIORITY);
        t.start ();

        Wizard w = Wizard.builder ()
            .startStepId (this.getStartStepId ())
            .nextStepIdProvider (currId ->
            {

                return getNextStepId (currId);

            })
            .previousStepIdProvider (currId ->
            {

                return getPreviousStepId (currId);

            })
            .nextButtonLabelProvider (currId ->
            {

                if ((currId != null)
                    &&
                    (currId.equals ("existing"))
                   )
                {

                    return getUILanguageStringProperty (LanguageStrings.wizard,buttons,Wizard.FINISH_BUTTON_ID);
                    //"Finish";

                }

                if ((currId != null)
                    &&
                    (currId.equals ("login-details"))
                   )
                {

                    return getUILanguageStringProperty (editors,user,register,buttons,register);
                    //"Register";

                }

                return this.wizard.getNextButtonLabel (currId);

            })
            .stepProvider (currId ->
            {

                return getStep (currId);

            })
            .onCancel (ev ->
            {

                this.close ();

            })
            .onFinish (ev ->
            {

                this.handleFinish ();

                this.close ();

            })
            .nextStepCheck ((currId, nextId) ->
            {

                return this.handleStepChange (currId,
                                              nextId);

            })
            .previousStepCheck ((currId, prevId) ->
            {

                return this.handleStepChange (currId, prevId);

            })
            .onStepShow (ev ->
            {

                this.enableButtons (ev.getWizard (),
                                    ev.getCurrentStepId ());

            })
            .build ();

        VBox b = new VBox ();
        VBox.setVgrow (this.wizard, Priority.ALWAYS);
        b.getChildren ().addAll (w);

        this.getChildren ().addAll (b);

    }
/*
    public String getWindowTitle ()
    {

        return "Register for the Editor Service";

    }

    public String getHeaderTitle ()
    {

        return this.getWindowTitle ();

    }

    public String getHeaderIconType ()
    {

        return "editors";

    }
*/
    public String getFirstHelpText ()
    {

        return "Help!";

    }

    public boolean handleFinish ()
    {

        // Init the editors db.
        try
        {

            EditorsEnvironment.initDB (this.finder.getFile ());

        } catch (Exception e) {

            Environment.logError ("Unable to init editors database",
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (editors,user,register,actionerror));
                                      //"Unable to init editors database");

            // Clean up db files?

            return true;

        }

        // Save the directory away for the editors db. (user property)
        try
        {

            EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_DB_DIR_PROPERTY_NAME,
                                                   this.finder.getFile ().toString ());

        } catch (Exception e) {

            Environment.logError ("Unable to save editors database location",
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (editors,user,register,actionerror));
                                      //"Unable to save editors database location");

            return true;

        }

        // Set the credentials.
        if (!this.login)
        {

            try
            {

                EditorsEnvironment.initUserCredentials (this.emailField.getText ().trim ().toLowerCase (),
                                                        null, //new String (this.passwordField.getPassword ()),
                                                        this.serviceName,
                                                        this.messagingUsername,
                                                        this.keyPair.getPublicKey (),
                                                        this.keyPair.getPrivateKey ());

                // Save the name/picture away.
                String n = this.nameField.getText ().trim ();

                if (n.length () == 0)
                {

                    n = null;

                }

                EditorsEnvironment.setUserInformation (n,
                                                       this.avatar.getImage ());

            } catch (Exception e) {

                Environment.logError ("Unable to save user information/credentials",
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (editors,user,register,actionerror));
                                          //"Unable to save your details");

                return true;

            }

        } else {

            EditorsEnvironment.goOnline (null,
                                         null,
                                         null,
                                         null);

        }

        try
        {

            this.viewer.viewEditors ();

        } catch (Exception e) {

            Environment.logError ("Unable to show editors",
                                  e);

        }

        return true;

    }
/*
TODO Remove
    public String getNextButtonLabel (String currStage)
    {

        if ((currStage != null)
            &&
            (currStage.equals ("existing"))
           )
        {

            return getUIString (wizard,buttons,FINISH_BUTTON_ID);
            //"Finish";

        }

        if ((currStage != null)
            &&
            (currStage.equals ("login-details"))
           )
        {

            return getUIString (editors,user,register,buttons,register);
            //"Register";

        }

        return super.getNextButtonLabel (currStage);

    }
*/

    public String getNextStepId (String currStage)
    {

        if (currStage == null)
        {

            return "start";

        }

        if (currStage.equals ("start"))
        {

            return "dir";

        }

        if (currStage.equals ("dir"))
        {

            return "about";

        }

        if (currStage.equals ("login-details"))
        {

            return "finish";

        }

        if (currStage.equals ("about"))
        {

            return "login-details";

        }

        if (currStage.equals ("finish"))
        {

        }

        return null;

    }

    public String getPreviousStepId (String currStage)
    {

        if (currStage == null)
        {

            return null;

        }

        if (currStage.equals ("existing"))
        {

            return "start";

        }

        if (currStage.equals ("dir"))
        {

            return "start";

        }

        if (currStage.equals ("start"))
        {

            return null;

        }

        if (currStage.equals ("login-details"))
        {

            return "about";

        }

        if (currStage.equals ("about"))
        {

            return "dir";

        }

        if (currStage.equals ("finish"))
        {

            return null;

        }

        return null;

    }

    public boolean handleStepChange (String oldStage,
                                     String newStage)
    {

        if (oldStage == null)
        {

            return true;

        }

        if ((newStage.equals ("finish"))
            &&
            (this.createCalled)
           )
        {

            return true;

        }

        if ((oldStage.equals ("start"))
            &&
            (newStage.equals ("existing"))
           )
        {

            return true;

        }

        if (oldStage.equals ("start"))
        {

            if (!this.tcAgreeField.isSelected ())
            {

                this.tcError.setVisible (true);

                return false;

            }

            if (!this.tcsClicked)
            {

                QuollPopup.messageBuilder ()
                    .title (editors,user,register,stages,start,reminderpopup,title)
                    .message (editors,user,register,stages,start,reminderpopup,text)
                    .inViewer (this.viewer)
                    .build ();

            }

        }

        if (oldStage.equals ("dir"))
        {

            if (newStage.equals ("start"))
            {

                return true;

            }

            if (Files.exists (Utils.getQuollWriterDirFile (this.finder.getFile ())))
            {

                return false;

            }

        }

        if ((oldStage.equals ("login-details"))
            &&
            (newStage.equals ("finish"))
           )
        {

            final EditorRegister _this = this;

            String email = this.emailField.getText ().trim ();

            int atInd = email.indexOf ('@');

            int dotInd = email.indexOf ('.',
                                        atInd);

            if ((email.length () == 0)
                ||
                (atInd == -1)
                ||
                (dotInd == -1)
                ||
                (email.length () - 1 == dotInd)
               )
            {

                this.loginError.setText (getUILanguageStringProperty (editors,user,register,stages,logindetails,errors,invalidemail));
                //"Please provide a valid email address.");
                this.loginError.setVisible (true);

                return false;

            }

            String pwd = this.passwordField.getPassword1 ();
            String pwd2 = this.passwordField.getPassword2 ();

            if (pwd.length () == 0)
            {

                this.loginError.setText (getUILanguageStringProperty (editors,user,register,stages,logindetails,errors,nopassword));
                //"Please provide a password.");
                this.loginError.setVisible (true);

                return false;

            }

            if (pwd2.length () == 0)
            {

                this.loginError.setText (getUILanguageStringProperty (editors,user,register,stages,logindetails,errors,confirmpassword));
                //"Please confirm your password.");
                this.loginError.setVisible (true);

                return false;

            }

            if (!pwd.equals (pwd2))
            {

                this.loginError.setText (getUILanguageStringProperty (editors,user,register,stages,logindetails,errors,nomatch));
                //"Your passwords do not match.");
                this.loginError.setVisible (true);

                return false;

            }

            if (pwd.length () < 8)
            {

                this.loginError.setText (getUILanguageStringProperty (editors,user,register,stages,logindetails,errors,minlength));
                //"Your password must be at least 8 characters long.");
                this.loginError.setVisible (true);

                return false;

            }

            this.loginError.setVisible (false);

            // Create the account, show the saving.
            this.wizard.enableButton (Wizard.FINISH_BUTTON_ID,
                                      false);
            this.wizard.enableButton (Wizard.PREVIOUS_BUTTON_ID,
                                      false);
            this.wizard.enableButton (Wizard.CANCEL_BUTTON_ID,
                                      false);

            this.saving.setVisible (true);

            this.emailField.setDisable (true);
            this.passwordField.setFieldsDisable (true);

            try
            {

                EditorsEnvironment.getEditorsWebServiceHandler ().createAccount (email,
                                                                                 pwd,
                                                                                 this.keyPair.getPublicKey (),
                                                                                 res ->
                {

                    Map d = (Map) res.getReturnObject ();

                    this.messagingUsername = (String) d.get ("username");
                    this.serviceName = (String) d.get ("servicename");

                    this.createCalled = true;

                    this.wizard.showStep ("finish");

                },
                res ->
                {

                    this.wizard.enableButton (Wizard.FINISH_BUTTON_ID,
                                              true);
                    this.wizard.enableButton (Wizard.PREVIOUS_BUTTON_ID,
                                              true);
                    this.wizard.enableButton (Wizard.CANCEL_BUTTON_ID,
                                              true);

                    _this.saving.setVisible (false);

                    // Handle parameter errors, then other error types.

                    _this.emailField.setDisable (false);
                    _this.passwordField.setFieldsDisable (false);
                    _this.loginError.setText (new SimpleStringProperty (getUILanguageStringProperty (editors,user,register,actionerror).getValue () + "  " + res.getErrorMessage ()));
                    //"Unable to create account: " + res.getErrorMessage ());
                    _this.loginError.setVisible (true);

                });

            } catch (Exception e) {

                Environment.logError ("Unable to create account",
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (editors,user,register,actionerror));
                                          //"Unable to create account, please contact Quoll Writer support for assistance.");

            }

            return false;

        }

        return true;

    }

    public String getStartStepId ()
    {

        return "start";

    }

    private Wizard.Step createStartStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        List<String> prefix = Arrays.asList (editors,user,register,stages,start);

        ws.title = getUILanguageStringProperty (Utils.newList (prefix,title));
        //"Getting started";

        QuollTextView desc = QuollTextView.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .inViewer (this.viewer)
            .text (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                Environment.getQuollWriterHelpLink ("editor-mode/overview",
                                                                                    null)))
            .build ();

        QuollHyperlink tc = QuollHyperlink.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,labels,viewtandc)))
            .styleClassName (StyleClassNames.INFORMATION)
            .onAction (ev ->
            {

                this.tcsClicked = true;
                UIUtils.openURL (this.viewer,
                                 Environment.getQuollWriterHelpLink ("editor-mode/terms-and-conditions",
                                                                     null));

            })
            .build ();

        this.tcError = QuollLabel.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,errors,agreetandc)))
            .styleClassName (StyleClassNames.ERROR)
            .build ();

        this.tcError.setVisible (false);

        this.tcAgreeField = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,labels,agreetandc)))
            .onAction (ev ->
            {

                if (this.tcAgreeField.isSelected ())
                {

                    this.tcError.setVisible (false);

                }

            })
            .build ();

        QuollHyperlink reg = QuollHyperlink.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,labels,alreadyregistered)))
            .styleClassName (StyleClassNames.EDITORS)
            .onAction (ev ->
            {

                this.wizard.showStep ("existing");

            })
            .build ();

        VBox b = new VBox ();

        b.getChildren ().addAll (desc, tc, this.tcError, this.tcAgreeField, reg);

        ws.content = b;

        return ws;

    }

    private Wizard.Step createExistingStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        List<String> prefix = Arrays.asList (editors,user,register,stages,exists);

        this.login = true;

        ws.title = getUILanguageStringProperty (Utils.newList (prefix,title));
        //"Find an existing {editors} database";

        QuollTextView desc = QuollTextView.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .inViewer (this.viewer)
            .text (getUILanguageStringProperty (Utils.newList (prefix,text)))
            .build ();

        QuollLabel message = QuollLabel.builder ()
            .styleClassName (StyleClassNames.INFORMATION)
            .build ();

        message.setVisible (false);

        this.finder = QuollFileField.builder ()
            .chooserTitle (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.finder,title)))
            .limitTo (QuollFileField.Type.directory)
            .initialFile (Environment.getUserQuollWriterDirPath ())
            .withViewer (viewer)
            .findButtonTooltip (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.finder,tooltip)))
            .build ();

        this.finder.fileProperty ().addListener ((pr, oldv, newv) ->
        {

            // See if it's an existing editors db, if so ask.

            if (!EditorsEnvironment.isEditorsDBDir (this.finder.getFile ()))
            {

                // Show an error
                message.setText (getUILanguageStringProperty (Arrays.asList (editors,user,register,stages,exists,errors,invalidvalue),
                                                              Constants.EDITORS_DB_FILE_NAME_PREFIX));
                message.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, true);

                this.wizard.enableButton (Wizard.FINISH_BUTTON_ID,
                                          false);

            } else {

                // See if the project is already in their project list.

                message.setText (getUILanguageStringProperty (editors,user,register,stages,exists,labels,confirm));
                message.pseudoClassStateChanged (StyleClassNames.OK_PSEUDO_CLASS, true);

                // Set the seen sidebar property to ensure the welcome tab doesn't display.
                try
                {

                    EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_EDITORS_SIDEBAR_SEEN_PROPERTY_NAME,
                                                           true);

                } catch (Exception e) {

                    Environment.logError ("Unable to set editors sidebar seen property",
                                          e);

                }

                this.wizard.enableButton (Wizard.FINISH_BUTTON_ID,
                                          true);

            }

            message.setVisible (true);

            // Open the database and show the information?

        });

        VBox b = new VBox ();

        b.getChildren ().addAll (desc, message, this.finder);

        ws.content = b;

        return ws;

    }

    private Wizard.Step createDirStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        List<String> prefix = Arrays.asList (editors,user,register,stages,selectfolder);

        ws.title = getUILanguageStringProperty (Utils.newList (prefix,title));
        //"Where to store editor information?";

        QuollTextView desc = QuollTextView.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .inViewer (this.viewer)
            .text (getUILanguageStringProperty (Utils.newList (prefix,text)))
            .build ();

        QuollLabel finderError = QuollLabel.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,errors,invalidvalue)))
            .styleClassName (StyleClassNames.ERROR)
            .build ();

        finderError.setVisible (false);

        this.finder = QuollFileField.builder ()
            .chooserTitle (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.finder,title)))
            .limitTo (QuollFileField.Type.directory)
            .initialFile (Environment.getUserPath ("editors"))
            .withViewer (viewer)
            .findButtonTooltip (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.finder,tooltip)))
            .build ();

        this.finder.fileProperty ().addListener ((pr, oldv, newv) ->
        {

            finderError.setVisible (false);

            // See if it's an existing editors db, if so ask.

            if (Files.exists (Utils.getQuollWriterDirFile (this.finder.getFile ())))
            {

                finderError.setVisible (true);

            }

        });

        // Support encryption?

        VBox b = new VBox ();
        b.getChildren ().addAll (desc, finderError, this.finder);

        ws.content = b;

        return ws;

    }

    private Wizard.Step createFinishStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        List<String> prefix = Arrays.asList (editors,user,register,stages,finish);

        ws.title = getUILanguageStringProperty (Utils.newList (prefix,title));
        //"Account created, just one more step";

        QuollTextView desc = QuollTextView.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .inViewer (this.viewer)
            .text (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                this.emailField.getText ().trim ()))
            .build ();

        QuollLabel l = QuollLabel.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,labels,check)))
            .build ();

        VBox b = new VBox ();
        b.getChildren ().addAll (desc, l);

        ws.content = l;

        return ws;

    }

    private Wizard.Step createAboutStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        List<String> prefix = Arrays.asList (editors,user,register,stages,about);

        ws.title = getUILanguageStringProperty (Utils.newList (prefix,title));
        //"About you";

        QuollTextView desc = QuollTextView.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .inViewer (this.viewer)
            .text (getUILanguageStringProperty (Utils.newList (prefix,text)))
            .build ();

        this.nameField = QuollTextField.builder ()
            .build ();

        this.avatar = ImageSelector.builder ()
            .build ();

        Form f = Form.builder ()
            .item (getUILanguageStringProperty (Utils.newList (prefix,labels,name)),
                   this.nameField)
            .item (getUILanguageStringProperty (Utils.newList (prefix,labels,LanguageStrings.avatar)),
                   this.avatar)
            .build ();

        VBox b = new VBox ();
        b.getChildren ().addAll (desc, f);
        ws.content = b;

        return ws;

    }

    private Wizard.Step createLoginDetailsStep ()
    {

        Wizard.Step ws = new Wizard.Step ();
        List<String> prefix = Arrays.asList (editors,user,register,stages,logindetails);

        ws.title = getUILanguageStringProperty (Utils.newList (prefix,title));
        //"Your login details";

        QuollTextView desc = QuollTextView.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .inViewer (this.viewer)
            .text (getUILanguageStringProperty (Utils.newList (prefix,text)))
            .build ();

        this.saving = QuollLabel.builder ()
            .styleClassName (StyleClassNames.LOADING)
            .label (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.saving)))
            .build ();
        this.saving.setVisible (false);

        this.loginError = QuollLabel.builder ()
            .styleClassName (StyleClassNames.ERROR)
            .build ();
        this.loginError.setVisible (false);

        this.emailField = QuollTextField.builder ()
            .build ();
        this.passwordField = QuollPasswordField.builder ()
            .passwordLabel (getUILanguageStringProperty (Utils.newList (prefix,labels,password)))
            .confirmLabel (getUILanguageStringProperty (Utils.newList (prefix,labels,confirmpassword)))
            .build ();

        Form f = Form.builder ()
            .item (getUILanguageStringProperty (Utils.newList (prefix,labels,email)),
                   this.emailField)
            .item (this.passwordField)
            .build ();

        // TODO Use form error/loading?
        VBox b = new VBox ();
        b.getChildren ().addAll (this.saving, this.loginError, f);

        ws.content = b;

        return ws;

    }

    public Wizard.Step getStep (String stepId)
    {

        final EditorRegister _this = this;

        Wizard.Step ws = this.steps.get (stepId);

        if (ws != null)
        {

            return ws;

        }

        if (stepId.equals ("start"))
        {

            return this.createStartStep ();

        }

        if (stepId.equals ("existing"))
        {

            return this.createExistingStep ();

        }

        if (stepId.equals ("dir"))
        {

            return this.createDirStep ();

        }

        if (stepId.equals ("finish"))
        {

            return this.createFinishStep ();

        }

        if (stepId.equals ("about"))
        {

            return this.createAboutStep ();

        }

        if (stepId.equals ("login-details"))
        {

            return this.createLoginDetailsStep ();

        }

        return ws;

    }

    private void enableButtons (Wizard wiz,
                                String currentStage)
    {

        wiz.enableButton (Wizard.NEXT_BUTTON_ID,
                          true);

        if (currentStage.equals ("existing"))
        {

            wiz.enableButton (Wizard.FINISH_BUTTON_ID,
                              false);

        }

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (getUILanguageStringProperty (editors,user,register,LanguageStrings.popup,title))
            .styleClassName (StyleClassNames.EDITORREGISTER)
            .styleSheet (StyleClassNames.EDITORREGISTER)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();
        p.requestFocus ();

        return p;

    }

}
