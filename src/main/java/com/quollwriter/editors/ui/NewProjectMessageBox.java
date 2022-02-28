package com.quollwriter.editors.ui;

import java.util.*;

import java.io.*;
import java.nio.file.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;
import com.quollwriter.text.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class NewProjectMessageBox extends MessageBox<NewProjectMessage>
{

    private VBox responseBox = null;
    private ProjectSentReceivedViewer sentViewer = null;
    private Label previousLabel = null;
    private QuollButtonBar responseButs = null;

    public NewProjectMessageBox (NewProjectMessage mess,
                                 AbstractViewer    viewer)
    {

        super (mess,
               viewer);

        this.getStyleClass ().add (StyleClassNames.NEWPROJECT);

        final NewProjectMessageBox _this = this;

        StringProperty text = getUILanguageStringProperty (editors,messages,newproject,sent,title);
        //"Sent {project}";

        if (!this.message.isSentByMe ())
        {

           text = getUILanguageStringProperty (editors,messages,newproject,received,title);
           //"Received an invitation to edit a {project}";

       }

       this.getStyleClass ().add (StyleClassNames.NEWPROJECTMESSAGE);

       this.getChildren ().add (Header.builder ()
        .title (text)
        .styleClassName (StyleClassNames.HEADER)
        .iconClassName (StyleClassNames.PROJECT)
        .build ());

       this.binder.addChangeListener (this.message.dealtWithProperty (),
                                      (pr, oldv, newv) ->
       {

           this.update ();

       });

       this.binder.addChangeListener (this.message.getEditor ().editorStatusProperty (),
                                      (pr, oldv, newv) ->
       {

           this.update ();

       });

       Node details = EditorsUIUtils.getProjectMessageDetails (this.message,
                                                               this.viewer,
                                                               this);

       details.getStyleClass ().add (StyleClassNames.MESSAGEDETAILS);
       this.getChildren ().add (details);

       this.responseBox = new VBox ();
       this.responseBox.getStyleClass ().add (StyleClassNames.RESPONSE);
       this.responseBox.managedProperty ().bind (this.responseBox.visibleProperty ());
       this.responseBox.setVisible (false);
       this.getChildren ().add (this.responseBox);
/*
       if (!this.message.isSentByMe ())
       {

           // Not sent by me.
           if ((!this.message.isDealtWith ())
               &&
               (!this.message.getEditor ().isPrevious ())
              )
           {

               Button update = QuollButton.builder ()
                .label (editors,messages,updateproject,received,undealtwith,buttons,LanguageStrings.update)
                .onAction (ev ->
                {

                    EditorsUIUtils.showProjectUpdate (this.message,
                                                      this.viewer,
                                                      null);

                })
                .build ();

               this.responseBox.getChildren ().add (update);
               this.responseBox.setVisible (true);

           }

       }
*/
       this.previousLabel = QuollLabel.builder ()
        .styleClassName (StyleClassNames.PREVIOUS)
        .build ();

        if ((!this.message.isDealtWith ())
            &&
            (!this.message.isSentByMe ())
            &&
            (!this.message.getEditor ().isPrevious ())
           )
        {

            this.responseBox.setVisible (true);

            this.responseBox.getChildren ().add (Header.builder ()
                .title (editors,messages,newproject,received,undealtwith,LanguageStrings.text)
                .build ());

            this.responseButs = QuollButtonBar.builder ()
                .button (QuollButton.builder ()
                    .label (editors,messages,newproject,received,undealtwith,buttons,LanguageStrings.accept,LanguageStrings.text)
                    .buttonType (ButtonBar.ButtonData.YES)
                    .onAction (ev ->
                    {

                        this.handleNewProjectResponse (true);

                    })
                    .tooltip (editors,messages,newproject,received,undealtwith,buttons,LanguageStrings.accept,title)
                    .build ())
                .button (QuollButton.builder ()
                    .label (editors,messages,newproject,received,undealtwith,buttons,LanguageStrings.reject,LanguageStrings.text)
                    .buttonType (ButtonBar.ButtonData.NO)
                    .onAction (ev ->
                    {

                        this.handleNewProjectResponse (false);

                    })
                    .tooltip (editors,messages,newproject,received,undealtwith,buttons,LanguageStrings.reject,title)
                    .build ())
                .build ();

            this.responseBox.getChildren ().add (this.responseButs);

        }

    }

    @Override
    public boolean isAutoDealtWith ()
    {

        return false;

    }

    @Override
    public boolean isShowAttentionBorder ()
    {

        if (this.message.getEditor ().isPrevious ())
        {

            return false;

        }

        return super.isShowAttentionBorder ();

    }

    private void update ()
    {

        this.previousLabel.setVisible (false);

        if (this.message.isDealtWith ())
        {

            this.responseBox.setVisible (false);

        }

        if ((!this.message.isDealtWith ())
            &&
            (this.message.getEditor ().isPrevious ())
           )
        {

            this.previousLabel.textProperty ().unbind ();
            this.previousLabel.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (editors,messages,newproject,received,undealtwith,previouseditor),
                                                        //"<b>%s</b> is a previous {contact}.  This message can no longer be acted upon.",
                                                                                  this.message.getEditor ().getMainName ()));
            this.previousLabel.setVisible (true);
            this.responseBox.setVisible (false);

        }

    }

    private void handleNewProjectAccept ()
    {

        List<String> prefix = Arrays.asList (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,popup);

        VBox content = new VBox ();

        QuollPopup qp = QuollPopup.builder ()
            .headerIconClassName (StyleClassNames.PROJECT)
            .styleClassName (StyleClassNames.ACCEPT)
            .styleSheet ("editoracceptproject")
            .title (Utils.newList (prefix,title))
            .content (content)
            .inViewer (this.viewer)
            .hideOnEscape (true)
            .removeOnClose (true)
            .withClose (true)
            .build ();
/*
TODO Remove
        content.getChildren ().add (Header.builder ()
            .title (Utils.newList (prefix,labels,where))
            .build ());
*/
/*
TODO Is this needed?
        Path defDir = Environment.getDefaultSaveProjectDirPath ();

        try
        {

            Files.createDirectories (defDir);

        } catch (Exception e) {

            Environment.logError ("Unable to create directories for: " + defDir,
                                  e);

            // TODO Hanlde this error.

        }
*/

        Path defDir = Environment.getDefaultSaveProjectDirPath ();
        Path nf = defDir.resolve ("editor-projects/").resolve (this.message.getEditor ().getEmail ());

        try
        {

            Files.createDirectories (nf);

        } catch (Exception e) {

            Environment.logError ("Unable to create directories for: " + defDir,
                                  e);

            // TODO Hanlde this error.

        }

        QuollFileField saveDir = QuollFileField.builder ()
            .styleClassName (StyleClassNames.SAVE)
            .initialFile (nf)
            .limitTo (QuollFileField.Type.directory)
            .chooserTitle (Utils.newList (prefix,finder,tooltip))
            .withViewer (viewer)
            .build ();

        QuollCheckBox encrypt = QuollCheckBox.builder ()
            .selected (false)
            .label (Utils.newList (prefix,labels,LanguageStrings.encrypt))
            .build ();

        QuollPasswordField passwords = QuollPasswordField.builder ()
            .passwordLabel (getUILanguageStringProperty (Utils.newList (prefix,labels,password)))
            .confirmLabel (getUILanguageStringProperty (Utils.newList (prefix,labels,confirmpassword)))
            .build ();

        passwords.managedProperty ().bind (passwords.visibleProperty ());
        passwords.setVisible (false);
        encrypt.selectedProperty ().addListener ((v, oldv, newv) ->
        {

            passwords.setVisible (newv);

        });

        QuollTextArea mess = QuollTextArea.builder ()
            .placeholder (getUILanguageStringProperty (Utils.newList (prefix,labels,sendmessage,tooltip),
                                                            //"You can optionally return a message to %s using this box.",
                                                       this.message.getEditor ().getMainName ()))
            .maxChars (5000)
            .build ();

        Form form = Form.builder ()
            //.description (desc)
            .layoutType (Form.LayoutType.stacked)
            .item (getUILanguageStringProperty (Utils.newList (prefix,labels,project)),
                   QuollLabel.builder ()
                    .label (this.message.forProjectNameProperty ())
                    .build ())
            .item (getUILanguageStringProperty (Utils.newList (prefix,labels,save)),
                   saveDir)
            .item (encrypt)
            .item (passwords)
            .item (Header.builder ()
                .title (Utils.newList (prefix,labels,sendmessage,text))
                .styleClassName (StyleClassNames.SUBTITLE)
                .build ())
            .item (mess)
            .confirmButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,LanguageStrings.save)))
            .cancelButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,LanguageStrings.cancel)))
            .build ();

        content.getChildren ().add (form);

        form.setOnCancel (ev ->
        {

            qp.close ();

        });

        form.setOnConfirm (ev ->
        {

            form.hideError ();

            List<String> prefix2 = Arrays.asList (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,popup);

            // See if the project already exists.
            Path pf = saveDir.getFile ().resolve (Utils.sanitizeForFilename (this.message.getForProjectName ()));

            if (Files.exists (pf))
            {

                form.showError (getUILanguageStringProperty (Utils.newList (prefix2,errors,valueexists),
                                                            //"A {project} called: %s already exists.",
                                                             this.message.forProjectNameProperty (),
                                                             pf));

                return;

            }

            String pwd = passwords.getPassword1 ();

            if (encrypt.isSelected ())
            {

                // Make sure a password has been provided.
                String pwd2 = passwords.getPassword2 ();

                if (pwd.equals (""))
                {

                    form.showError (getUILanguageStringProperty (Utils.newList (prefix2,errors,nopassword)));
                                           //"Please provide a password for securing the {project}.");
                    return;

                }

                if (pwd2.equals (""))
                {

                    form.showError (getUILanguageStringProperty (Utils.newList (prefix2,errors,confirmpassword)));
                                           //"Please confirm your password.");
                    return;

                }

                if (!pwd.equals (pwd2))
                {

                    form.showError (getUILanguageStringProperty (Utils.newList (prefix2,errors,nomatch)));
                                           //"The passwords do not match.");
                    return;

                }

            }

            final String responseMessage = (mess.getText ().trim ().length () == 0 ? null : mess.getText ().trim ());

            this.message.setResponseMessage (responseMessage);

            // Create the project.
            Project p = null;

            try
            {

                p = this.message.createProject ();

            } catch (Exception e) {

                Environment.logError ("Unable to create project from message: " +
                                      this.message,
                                      e);

                form.showError (getUILanguageStringProperty (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,actionerror));
                                          //"Unable to save {project}, please contact Quoll Writer support for assistance.");

                return;

            }

            final Project fproj = p;

            // Put it in the user's directory.
            try
            {

                if (pwd.length () > 0)
                {

                    p.setFilePassword (pwd);

                }

                // We create the project but then close the connection pool since the user
                // may not want to open the project yet.
                Environment.createProject (saveDir.getFile (),
                                           p).closeConnectionPool ();

            } catch (Exception e) {

                Environment.logError ("Unable to save editor project to: " +
                                      saveDir +
                                      ", message: " +
                                      this.message,
                                      e);

                form.showError (getUILanguageStringProperty (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,actionerror));
                                          //"Unable to save {project}, please contact Quoll Writer support for assistance.");

                return;

            }

            this.message.setDealtWith (true);

            Runnable onComplete = () ->
            {

                String popupId = "open-editor-proj" + fproj.getId ();

                if (this.viewer.getPopupById (popupId) != null)
                {

                    return;

                }

                UIUtils.runLater (() ->
                {

                    qp.close ();

                });

                this.message.setAccepted (true);
                this.message.setDealtWith (true);
                this.message.setResponseMessage (responseMessage);

                try
                {

                    // Update the original message.
                    EditorsEnvironment.updateMessage (this.message);

                } catch (Exception e) {

                    Environment.logError ("Unable to update message: " +
                                          this.message,
                                          e);

                    ComponentUtils.showErrorMessage (getUILanguageStringProperty (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,actionerror));
                                              //"Unable to update message, please contact Quoll Writer support for assistance.");

                    // Should really carry on... maybe...

                }

                UIUtils.runLater (() ->
                {

                    QuollPopup.questionBuilder ()
                        .styleClassName (StyleClassNames.OPEN)
                        .popupId (popupId)
                        .title (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,openproject,popup,title)
                        .message (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,openproject,popup,text)
                        .confirmButtonLabel (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,openproject,popup,buttons,confirm)
                        .cancelButtonLabel (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,openproject,popup,buttons,cancel)
                        .onConfirm (eev ->
                        {

                            this.viewer.getPopupById (popupId).close ();

                            try
                            {

                                Environment.openProject (fproj,
                                                         () ->
                                                         {

                                                            // Show the first chapter.
                                                            AbstractProjectViewer pv = Environment.getProjectViewer (fproj);

                                                            if (pv != null)
                                                            {

                                                                pv.viewObject (pv.getProject ().getBook (0).getChapters ().get (0));

                                                            }

                                                         });

                            } catch (Exception e) {

                                Environment.logError ("Unable to open project: " +
                                                      fproj,
                                                      e);

                                ComponentUtils.showErrorMessage (getUILanguageStringProperty (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,openproject,actionerror));
                                                          //"Unable to open {project}: " + fproj.getName () + " please contact Quoll Support for assistance.");

                            }



                        })
                        .build ();

                });

            };

            NewProjectResponseMessage res = new NewProjectResponseMessage (this.message.getForProjectId (),
                                                                           true,
                                                                           responseMessage,
                                                                           this.message.getEditor (),
                                                                           EditorsEnvironment.getUserAccount ());

            if (this.message.getEditor ().isPending ())
            {

                EditorsEnvironment.acceptInvite (this.message.getEditor (),
                                                 res,
                                                 onComplete);

            } else {

                EditorsEnvironment.sendMessageToEditor (res,
                                                        onComplete,
                                                        null,
                                                        null);

            }

        });

        qp.show ();

    }

    public void handleNewProjectResponse (boolean accepted)
    {

        if (accepted)
        {

            this.handleNewProjectAccept ();

        } else {

            // Ask for a response?
            final String responseMessage = null;

            this.message.setDealtWith (true);

            Runnable onComplete = () ->
            {

                this.message.setAccepted (accepted);
                this.message.setDealtWith (true);
                this.message.setResponseMessage (responseMessage);

                try
                {

                    // Update the original message.
                    EditorsEnvironment.updateMessage (this.message);

                } catch (Exception e) {

                    Environment.logError ("Unable to update message: " +
                                          this.message,
                                          e);

                    ComponentUtils.showErrorMessage (getUILanguageStringProperty (editors,messages,update,actionerror));
                                              //"Unable to update message, please contact Quoll Writer support for assistance.");

                    // Should really carry on... maybe...

                }

            };

            NewProjectResponseMessage res = new NewProjectResponseMessage (this.message.getForProjectId (),
                                                                           false,
                                                                           responseMessage,
                                                                           this.message.getEditor (),
                                                                           EditorsEnvironment.getUserAccount ());

            if (this.message.getEditor ().isPending ())
            {

                EditorsEnvironment.rejectInvite (this.message.getEditor (),
                                                 res,
                                                 onComplete);

            } else {

                EditorsEnvironment.sendMessageToEditor (res,
                                                        onComplete,
                                                        null,
                                                        null);

            }

        }

    }

}
