package com.quollwriter.editors.ui;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Arrays;

import java.io.*;

import java.awt.Point;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;
import com.quollwriter.text.*;
import com.quollwriter.events.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class NewProjectMessageBox extends MessageBox<NewProjectMessage> implements EditorChangedListener
{

    private Box responseBox = null;
    private ProjectSentReceivedViewer sentViewer = null;
    private JLabel previousLabel = null;

    public NewProjectMessageBox (NewProjectMessage mess,
                                 AbstractViewer    viewer)
    {

        super (mess,
               viewer);

    }

    @Override
    public void editorChanged (EditorChangedEvent ev)
    {

        if (ev.getEditor () == this.message.getEditor ())
        {

            this.updateForEditor ();

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

    private void updateForEditor ()
    {

        this.previousLabel.setVisible (false);

        if ((!this.message.isDealtWith ())
            &&
            (this.message.getEditor ().isPrevious ())
           )
        {

            this.previousLabel.setText (String.format (getUIString (editors,messages,newproject,received,undealtwith,previouseditor),
                                                        //"<b>%s</b> is a previous {contact}.  This message can no longer be acted upon.",
                                                       this.message.getEditor ().getShortName ()));

            this.previousLabel.setVisible (true);

            this.responseBox.setVisible (false);

        }

    }

    public void doUpdate ()
    {

        this.responseBox.setVisible (!this.message.isDealtWith ());

    }

    public void doInit ()
    {

        EditorsEnvironment.addEditorChangedListener (this);

        final NewProjectMessageBox _this = this;

        String t = null;
        //"Sent {project}";

        if (!this.message.isSentByMe ())
        {

            t = getUIString (editors,messages,newproject,received,title);
            //"Received an invitation to edit a {project}";

        } else {

            t = getUIString (editors,messages,newproject,sent,title);

        }

        JComponent h = UIUtils.createBoldSubHeader (t,
                                                    Constants.PROJECT_ICON_NAME);

        this.add (h);

        JComponent bp = EditorsUIUtils.getProjectMessageDetails (this.message,
                                                                 this.viewer,
                                                                 this);

        bp.setBorder (UIUtils.createPadding (0, 5, 0, 5));

        this.add (bp);

        this.responseBox = new Box (BoxLayout.Y_AXIS);

        this.responseBox.setVisible (false);

        this.add (this.responseBox);

        this.previousLabel = UIUtils.createInformationLabel ("");

        this.add (this.previousLabel);

        this.updateForEditor ();

        if ((!this.message.isDealtWith ())
            &&
            (!this.message.isSentByMe ())
            &&
            (!this.message.getEditor ().isPrevious ())
           )
        {

            this.responseBox.setVisible (true);

            JComponent l = UIUtils.createBoldSubHeader (getUIString (editors,messages,newproject,received,undealtwith,text),
                                                        //"Select your response below",
                                                        null);

            this.responseBox.add (l);

            JButton accept = UIUtils.createButton (getUIString (editors,messages,newproject,received,undealtwith,buttons,LanguageStrings.accept,text),
                                                  //"Accept",
                                                   new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    // Show a message box.
                    _this.showResponseMessagePopup (true);

                }

            });

            accept.setToolTipText (getUIString (editors,messages,newproject,received,undealtwith,buttons,LanguageStrings.accept,title));
            //"Click to accept the invitation");

            JButton reject = UIUtils.createButton (getUIString (editors,messages,newproject,received,undealtwith,buttons,LanguageStrings.reject,text),
                                                    //"Reject",
                                                   new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    _this.showResponseMessagePopup (false);

                }

            });

            reject.setToolTipText (getUIString (editors,messages,newproject,received,undealtwith,buttons,LanguageStrings.reject,title));
            //"Click to reject the invitation");

            JButton[] buts = new JButton[] { accept, reject };

            JComponent bb = UIUtils.createButtonBar2 (buts,
                                                      Component.LEFT_ALIGNMENT);

            bb.setAlignmentX (Component.LEFT_ALIGNMENT);

            this.responseBox.add (bb);

        }

    }

    private void showResponseMessagePopup (final boolean accepted)
    {

        this.handleNewProjectResponse (this.viewer,
                                       this.message,
                                       accepted);

    }

    public void handleNewProjectResponse (      AbstractViewer    viewer,
                                          final NewProjectMessage mess,
                                          final boolean           accepted)
    {

        if (viewer == null)
        {

            viewer = Environment.getFocusedViewer ();

        }

        if (accepted)
        {

            java.util.List<String> prefix = Arrays.asList (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,popup);

            final QPopup qp = UIUtils.createClosablePopup (getUIString (prefix,title),
                                                            //"Accept the invitation",
                                                           Environment.getIcon (Constants.PROJECT_ICON_NAME,
                                                                                Constants.ICON_POPUP),
                                                           null);

            String rows = "p, 6px, p, 6px, p, 6px, p";

            int row = 1;

            Box content = new Box (BoxLayout.Y_AXIS);

            content.add (UIUtils.createBoldSubHeader (getUIString (prefix,labels,where),
                                                      //"Where should the {project} be saved?",
                                                      null));

            content.add (Box.createVerticalStrut (5));

            File defDir = Environment.getDefaultSaveProjectDir ();

            defDir.mkdirs ();

            File nf = new File (defDir, "editor-projects/" + mess.getEditor ().getEmail ());

            final FileFinder saveField = new FileFinder ();

            saveField.setFile (nf);

            saveField.setApproveButtonText (getUIString (prefix,finder,button));
            //"Select");
            saveField.setFinderSelectionMode (JFileChooser.DIRECTORIES_ONLY);
            saveField.setFinderTitle (getUIString (prefix,finder,title));
            //"Select a directory to save to");

            saveField.setFindButtonToolTip (getUIString (prefix,finder,tooltip));
            //"Click to find a directory");
            saveField.setClearOnCancel (true);
            saveField.init ();

            final String projName = mess.getForProjectName ();

            final JCheckBox encryptField = UIUtils.createCheckBox (getUIString (prefix,labels,encrypt));
            //"Encrypt this {project}?  You will be prompted for a password.");

            Set<FormItem> items = new LinkedHashSet<> ();

            items.add (new AnyFormItem (getUIString (prefix,labels,project),
                                        //"{Project}",
                                        UIUtils.createLabel (projName)));

            items.add (new AnyFormItem (getUIString (prefix,labels,save),
                                        //"Save In",
                                        saveField));

            items.add (new AnyFormItem (null,
                                        encryptField));

            FormLayout pfl = new FormLayout ("right:p, 6px, 100px, 20px, p, 6px, fill:100px",
                                             "p, 6px");
            pfl.setHonorsVisibility (true);
            PanelBuilder pbuilder = new PanelBuilder (pfl);

            CellConstraints cc = new CellConstraints ();

            final JPasswordField passwordField = new JPasswordField ();

            pbuilder.addLabel (getUIString (prefix,labels,password),
                            //"Password",
                               cc.xy (1,
                                      1));

            pbuilder.add (passwordField,
                          cc.xy (3,
                                 1));

            final JPasswordField passwordField2 = new JPasswordField ();

            pbuilder.addLabel (getUIString (prefix,labels,confirmpassword),
                                //"Confirm",
                               cc.xy (5,
                                      1));

            pbuilder.add (passwordField2,
                          cc.xy (7,
                                 1));

            final JPanel ppanel = pbuilder.getPanel ();
            ppanel.setVisible (false);
            ppanel.setOpaque (false);

            items.add (new AnyFormItem (null,
                                        ppanel));

            encryptField.addActionListener (new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    ppanel.setVisible (encryptField.isSelected ());

                    qp.resize ();

                }

            });

            final JLabel error = UIUtils.createErrorLabel ("");
            error.setVisible (false);
            error.setBorder (UIUtils.createPadding (0, 0, 5, 0));

            Form f = UIUtils.createForm (items);

            content.add (error);
            content.add (f);

            content.add (Box.createVerticalStrut (10));
            content.add (UIUtils.createBoldSubHeader (getUIString (prefix,labels,sendmessage,text),
                                                    //"Send a message back",
                                                      null));

            final TextArea res = new TextArea (String.format (getUIString (prefix,labels,sendmessage,tooltip),
                                                                //"You can optionally return a message to %s using this box.",
                                                              mess.getEditor ().getMainName ()),
                                               5,
                                               5000);

            content.setBorder (UIUtils.createPadding (5, 0, 0, 0));
            content.add (res);

            ActionListener doSave = new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    error.setVisible (false);

                    java.util.List<String> prefix = Arrays.asList (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,popup);

                    // See if the project already exists.
                    File pf = new File (saveField.getSelectedFile (), Utils.sanitizeForFilename (projName));

                    if (pf.exists ())
                    {

                        error.setText (String.format (getUIString (prefix,errors,valueexists),
                                                    //"A {project} called: %s already exists.",
                                                      projName,
                                                      pf.getPath ()));

                        error.setVisible (true);

                        qp.resize ();

                        return;

                    }

                    String pwd = null;

                    if (encryptField.isSelected ())
                    {

                        // Make sure a password has been provided.
                        pwd = new String (passwordField.getPassword ()).trim ();

                        String pwd2 = new String (passwordField2.getPassword ()).trim ();

                        if (pwd.equals (""))
                        {

                            error.setText (getUIString (prefix,errors,nopassword));
                            //"Please provide a password for securing the {project}."));

                            error.setVisible (true);

                            qp.resize ();

                            return;

                        }

                        if (pwd2.equals (""))
                        {

                            error.setText (getUIString (prefix,errors,confirmpassword));
                            //"Please confirm your password.");

                            error.setVisible (true);

                            qp.resize ();

                            return;

                        }

                        if (!pwd.equals (pwd2))
                        {

                            error.setText (getUIString (prefix,errors,nomatch));
                            //"The passwords do not match.");

                            error.setVisible (true);

                            qp.resize ();

                            return;

                        }

                    }

                    final String responseMessage = (res.getText ().trim ().length () == 0 ? null : res.getText ().trim ());

                    mess.setResponseMessage (responseMessage);

                    // Create the project.
                    Project p = null;

                    try
                    {

                        p = mess.createProject ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to create project from message: " +
                                              mess,
                                              e);

                        UIUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                  getUIString (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,actionerror));
                                                  //"Unable to save {project}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                    final Project fproj = p;

                    // Put it in the user's directory.
                    try
                    {

                        // We create the project but then close the connection pool since the user
                        // may not want to open the project yet.
                        Environment.createProject (saveField.getSelectedFile (),
                                                   p,
                                                   pwd).closeConnectionPool ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to save editor project to: " +
                                              saveField.getSelectedFile () +
                                              ", message: " +
                                              mess,
                                              e);

                        UIUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                  getUIString (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,actionerror));
                                                  //"Unable to save {project}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                    mess.setDealtWith (true);

                    ActionListener onComplete = new ActionListener ()
                    {

                       public void actionPerformed (ActionEvent ev)
                       {

                            qp.removeFromParent ();

                            mess.setAccepted (accepted);
                            mess.setDealtWith (true);
                            mess.setResponseMessage (responseMessage);

                            try
                            {

                                // Update the original message.
                                EditorsEnvironment.updateMessage (mess);

                            } catch (Exception e) {

                                Environment.logError ("Unable to update message: " +
                                                      mess,
                                                      e);

                                UIUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                          getUIString (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,actionerror));
                                                          //"Unable to update message, please contact Quoll Writer support for assistance.");

                                // Should really carry on... maybe...

                            }

                            // Ask if they want to open the project now.
                            UIUtils.createQuestionPopup (Environment.getFocusedViewer (),
                                                         getUIString (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,openproject,popup,title),
                                                         //"Open the {project}?",
                                                         Constants.OPEN_PROJECT_ICON_NAME,
                                                         getUIString (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,openproject,popup,text),
                                                         //"Open the {project} now?",
                                                         getUIString (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,openproject,popup,buttons,confirm),
                                                         //"Yes, open it",
                                                         getUIString (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,openproject,popup,buttons,cancel),
                                                         //"Not now",
                                                         new ActionListener ()
                                                         {

                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                try
                                                                {

                                                                    Environment.openProject (fproj,
                                                                                            new ActionListener ()
                                                                                             {

                                                                                                public void actionPerformed (ActionEvent ev)
                                                                                                {

                                                                                                    // Show the first chapter.
                                                                                                    AbstractProjectViewer pv = Environment.getProjectViewer (fproj);

                                                                                                    if (pv != null)
                                                                                                    {

                                                                                                        pv.viewObject (pv.getProject ().getBook (0).getChapters ().get (0));

                                                                                                    }

                                                                                                }

                                                                                             });

                                                                } catch (Exception e) {

                                                                    Environment.logError ("Unable to open project: " +
                                                                                          fproj,
                                                                                          e);

                                                                    UIUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                                                              getUIString (editors,messages,newproject,received,undealtwith,LanguageStrings.accepted,openproject,actionerror));
                                                                                              //"Unable to open {project}: " + fproj.getName () + " please contact Quoll Support for assistance.");

                                                                }

                                                            }

                                                         },
                                                         null,
                                                         null,
                                                         null);

                       }

                    };

                    NewProjectResponseMessage res = new NewProjectResponseMessage (mess.getForProjectId (),
                                                                                   true,
                                                                                   responseMessage,
                                                                                   mess.getEditor (),
                                                                                   EditorsEnvironment.getUserAccount ());

                    if (mess.getEditor ().isPending ())
                    {

                        EditorsEnvironment.acceptInvite (mess.getEditor (),
                                                         res,
                                                         onComplete);

                    } else {

                        EditorsEnvironment.sendMessageToEditor (res,
                                                                onComplete,
                                                                null,
                                                                null);

                    }

               }

            };

            JButton save = UIUtils.createButton (getUIString (prefix,buttons,LanguageStrings.save),
                                                //"Save {project}",
                                                 doSave);

            UIUtils.addDoActionOnReturnPressed (res,
                                                doSave);

            JButton cancel = UIUtils.createButton (getUIString (prefix,buttons,LanguageStrings.cancel),
                                                    //Environment.getButtonLabel (Constants.CANCEL_BUTTON_LABEL_ID),
                                                   new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                     qp.removeFromParent ();

                }

            });

            JButton[] buts = new JButton[] { save, cancel };

            JComponent bs = UIUtils.createButtonBar2 (buts,
                                                      Component.LEFT_ALIGNMENT);
            bs.setAlignmentX (Component.LEFT_ALIGNMENT);

            content.add (Box.createVerticalStrut (10));
            content.add (bs);
            content.setBorder (new EmptyBorder (10, 10, 10, 10));

            qp.setContent (content);

            content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                     content.getPreferredSize ().height));

            Point showAt = UIUtils.getCenterShowPosition (viewer,
                                                          qp);

            viewer.showPopupAt (qp,
                                showAt,
                                false);

            qp.setDraggable (viewer);

        } else {

            // Ask for a response?
            final String responseMessage = null;

            mess.setDealtWith (true);

            ActionListener onComplete = new ActionListener ()
            {

               public void actionPerformed (ActionEvent ev)
               {

                    mess.setAccepted (accepted);
                    mess.setDealtWith (true);
                    mess.setResponseMessage (responseMessage);

                    try
                    {

                        // Update the original message.
                        EditorsEnvironment.updateMessage (mess);

                    } catch (Exception e) {

                        Environment.logError ("Unable to update message: " +
                                              mess,
                                              e);

                        UIUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                  getUIString (editors,messages,update,actionerror));
                                                  //"Unable to update message, please contact Quoll Writer support for assistance.");

                        // Should really carry on... maybe...

                    }

               }

            };

            NewProjectResponseMessage res = new NewProjectResponseMessage (mess.getForProjectId (),
                                                                           false,
                                                                           responseMessage,
                                                                           mess.getEditor (),
                                                                           EditorsEnvironment.getUserAccount ());

            if (mess.getEditor ().isPending ())
            {

                EditorsEnvironment.rejectInvite (mess.getEditor (),
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
