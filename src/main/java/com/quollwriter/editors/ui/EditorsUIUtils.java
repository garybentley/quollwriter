package com.quollwriter.editors.ui;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.net.*;
import java.io.*;
import java.awt.image.*;
import java.time.*;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.embed.swing.*;

import org.josql.*;

import com.toedter.calendar.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.text.*;

import com.quollwriter.ui.fx.components.ComponentUtils;
import com.quollwriter.ui.fx.components.QuollPopup;
import com.quollwriter.ui.fx.components.Form;
import com.quollwriter.ui.fx.components.Viewer;
import com.quollwriter.ui.fx.components.QuollHyperlink;
import com.quollwriter.ui.fx.components.QuollLabel;
import com.quollwriter.ui.fx.components.QuollTextView;
import com.quollwriter.ui.fx.components.QuollButtonBar;
import com.quollwriter.ui.fx.components.QuollButton;
import com.quollwriter.ui.fx.components.QScrollPane;
import com.quollwriter.ui.fx.components.QuollTextArea;
import com.quollwriter.ui.fx.components.ImageSelector;
import com.quollwriter.ui.fx.components.NamedObjectTree;
import com.quollwriter.ui.fx.components.QuollTreeView;
import com.quollwriter.ui.fx.components.QuollTextField;
import com.quollwriter.ui.fx.components.Notification;
import com.quollwriter.ui.fx.StyleClassNames;
import com.quollwriter.ui.fx.State;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.UIUtils;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class EditorsUIUtils
{

    private static EditorLoginPopup editorLogin = null;
    private static EditorMessageFilter defaultViewableMessageFilter = null;
    private static EditorMessageFilter importantMessageFilter = null;

    static
    {

        EditorsUIUtils.editorLogin = new EditorLoginPopup ();

        // Defines the messages that can be "viewed" by the user.
        Project np = null;

        EditorsUIUtils.defaultViewableMessageFilter = new DefaultEditorMessageFilter (np,
                                                      EditorChatMessage.MESSAGE_TYPE,
                                                      EditorInfoMessage.MESSAGE_TYPE,
                                                      NewProjectMessage.MESSAGE_TYPE,
                                                      NewProjectResponseMessage.MESSAGE_TYPE,
                                                      ProjectCommentsMessage.MESSAGE_TYPE,
                                                      InviteMessage.MESSAGE_TYPE,
                                                      InviteResponseMessage.MESSAGE_TYPE,
                                                      ProjectEditStopMessage.MESSAGE_TYPE,
                                                      UpdateProjectMessage.MESSAGE_TYPE,
                                                      EditorRemovedMessage.MESSAGE_TYPE);

        EditorsUIUtils.importantMessageFilter = new EditorMessageFilter ()
        {

            @Override
            public boolean accept (EditorMessage m)
            {

                if (!EditorsUIUtils.getDefaultViewableMessageFilter ().accept (m))
                {

                    return false;

                }

                if (m.isDealtWith ())
                {

                    return false;

                }
              /*
                if (m.getMessageType ().equals (EditorChatMessage.MESSAGE_TYPE))
                {

                    return false;

                }
                */
                /*
                if (m.getMessageType ().equals (ProjectCommentsMessage.MESSAGE_TYPE))
                {

                    return false;

                }
*/
                return true;

            }

        };

    };

    public static EditorMessageFilter getImportantMessageFilter ()
    {

        return EditorsUIUtils.importantMessageFilter;

    }

    public static EditorMessageFilter getDefaultViewableMessageFilter ()
    {

        return EditorsUIUtils.defaultViewableMessageFilter;

    }

    public static void showDeleteAccount (final AbstractViewer viewer)
    {

        // Remove all editors.

        // Send project edit stop for all projects they are editing.

        // Remove account.

        java.util.List<String> prefix = Arrays.asList (editors,user,deleteaccount,popup);

        //String s = "To delete your account please enter the word <b>Yes</b> in the box below.<br /><br />Note: Your {contacts} database will not be deleted allowing you to keep a record of what you have sent and received.  If you create another account you will not be able to use your current database.<br /><br /><p class='error'>Warning: deleting your account means you will no longer be able to send messages to any of your {contacts} or receive messages from them.  A message will be sent to each of them telling them you have removed them.</p>";

        QuollPopup.yesConfirmTextEntryBuilder ()
            .styleClassName (StyleClassNames.DELETE)
            .title (Utils.newList (prefix,title))
            .description (Utils.newList (prefix,text))
            .confirmButtonLabel (Utils.newList (prefix,buttons,confirm))
            .cancelButtonLabel (Utils.newList (prefix,buttons,cancel))
            .onConfirm (ev ->
            {

                final String dbDir = EditorsEnvironment.getEditorsProperty (Constants.QW_EDITORS_DB_DIR_PROPERTY_NAME);

                final Notification notify = viewer.addNotification (getUILanguageStringProperty (editors,user,deleteaccount,notification),
                                                                    //"Deleting your Editors Service account, please wait.  This sometimes takes a little while...",
                                                                    Constants.LOADING_GIF_NAME,
                                                                    -1);

                EditorsEnvironment.deleteUserAccount (() ->
                {

                    // Remove the editors sidebar.
                    Environment.removeSideBarFromAllProjectViewers (EditorsSideBar.SIDEBAR_ID);

                    viewer.removeNotification (notify);

                    String url = "";

                    try
                    {

                        url = new File (dbDir).toURI ().toURL ().toString ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to convert file: " +
                                              dbDir +
                                              " to a url",
                                              e);

                    }

                    QuollPopup.messageBuilder ()
                        .inViewer (viewer)
                        .title (editors,user,deleteaccount,confirmpopup,title)
                        .message (getUILanguageStringProperty (Arrays.asList (editors,user,deleteaccount,confirmpopup,text),
                                                               url))
                        .build ();

                },
                // On error
                (exp) ->
                {

                    ComponentUtils.showErrorMessage (getUILanguageStringProperty (editors,user,deleteaccount,actionerror));
                                              //"Unable to delete your account, please contact Quoll Writer support for assistance.");

                });

            })
            .build ();

    }

    /**
     * If we are editing any projects for any editors then show a popup offering to delete
     * the projects.  Once the delete is complete (or there are no projects) call <b>onRemoveComplete</b>.
     * If any of the projects are open then they are force closed first.
     *
     */
    public static void showDeleteProjectsForAllEditors (final AbstractViewer viewer,
                                                        final Runnable       onRemoveComplete)
    {

        Set<EditorEditor> eds = new HashSet ();

        Set<ProjectInfo> edProjs = null;

        try
        {

            edProjs = Environment.getAllProjectInfos (Project.EDITOR_PROJECT_TYPE);

        } catch (Exception e) {

            Environment.logError ("Unable to get all editor projects",
                                  e);

            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (editors,user,deletealleditorprojects,actionerror));
                                      //"Unable to get all {projects}, please contact Quoll Support for assistance.");

            if (onRemoveComplete != null)
            {

                Environment.scheduleImmediately (onRemoveComplete);

            }

            return;

        }

        for (ProjectInfo p : edProjs)
        {

            eds.add (p.getForEditor ());

        }

        if (edProjs.size () > 0)
        {

            final Runnable deleteEditorProjs = () ->
            {

                Set<ProjectInfo> _edProjs = null;

                try
                {

                    _edProjs = Environment.getAllProjectInfos (Project.EDITOR_PROJECT_TYPE);

                } catch (Exception e) {

                    Environment.logError ("Unable to get all editor projects",
                                          e);

                    ComponentUtils.showErrorMessage (viewer,
                                                     getUILanguageStringProperty (editors,user,deletealleditorprojects,actionerror));
                                              //"Unable to get all {projects}, please contact Quoll Support for assistance.");

                    if (onRemoveComplete != null)
                    {

                        Environment.scheduleImmediately (onRemoveComplete);

                    }

                    return;

                }

                for (ProjectInfo p : _edProjs)
                {

                    try
                    {

                        Environment.deleteProject (p,
                                                   (Runnable) null);

                    } catch (Exception e) {

                        Environment.logError ("Unable to delete project: " +
                                              p,
                                              e);

                        ComponentUtils.showErrorMessage (viewer,
                                                         getUILanguageStringProperty (editors,user,deletealleditorprojects,actionerror));

                    }

                }

                if (onRemoveComplete != null)
                {

                    Environment.scheduleImmediately (onRemoveComplete);

                }

            };

            java.util.List<String> prefix = Arrays.asList (editors,user,deletealleditorprojects,popup);

            QuollPopup.yesConfirmTextEntryBuilder ()
                .title (Utils.newList (prefix,title))
                .styleClassName (StyleClassNames.DELETE)
                .description (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                           Environment.formatNumber (edProjs.size ()),
                                                           Environment.formatNumber (eds.size ())))
                .confirmButtonLabel (Utils.newList (prefix,buttons,confirm))
                .cancelButtonLabel (Utils.newList (prefix,buttons,cancel))
                .onConfirm (ev ->
                {

                    Environment.scheduleImmediately (deleteEditorProjs);

                })
                .onCancel (ev ->
                {

                    Environment.scheduleImmediately (onRemoveComplete);

                })
                .build ();

        } else {

            if (onRemoveComplete != null)
            {

                Environment.scheduleImmediately (onRemoveComplete);

            }

        }

    }

    /**
     * If we are editing any projects for the specified editor then show a popup offering to delete
     * the projects.  Once the delete is complete (or there are no projects) call <b>onRemoveComplete</b>.
     * If any of the projects are open then they are force closed first.
     *
     */
    public static void showDeleteProjectsForEditor (final AbstractViewer viewer,
                                                    final EditorEditor   ed,
                                                    final Runnable       onRemoveComplete)
    {

        // Remove all projects for the editor.
        Set<ProjectInfo> edProjs = null;

        try
        {

            edProjs = EditorsEnvironment.getProjectsForEditor (ed);

        } catch (Exception e) {

            Environment.logError ("Unable to get projects for editor: " +
                                  ed,
                                  e);

            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (editors,user,deleteprojectsforeditor,actionerror));
                                      //"Unable to get {projects} for {editor}, please contact Quoll Writer support for assistance.");

            return;

        }

        if (edProjs.size () > 0)
        {

            final Runnable deleteEditorProjs = () ->
            {

                Set<ProjectInfo> _edProjs = null;

                try
                {

                    _edProjs = EditorsEnvironment.getProjectsForEditor (ed);

                } catch (Exception e) {

                    Environment.logError ("Unable to get projects for editor: " +
                                          ed,
                                          e);

                    ComponentUtils.showErrorMessage (viewer,
                                                     getUILanguageStringProperty (editors,user,deleteprojectsforeditor,actionerror));
                                              //"Unable to get {projects} for {contact}, please contact Quoll Writer support for assistance.");

                    return;

                }

                for (ProjectInfo p : _edProjs)
                {

                    // Just to be sure.
                    if (!p.getType ().equals (Project.EDITOR_PROJECT_TYPE))
                    {

                        continue;

                    }

                    Environment.deleteProject (p);

                }

                if (onRemoveComplete != null)
                {

                    Environment.scheduleImmediately (onRemoveComplete);

                }

            };

            List<String> prefix = Arrays.asList (editors,user,deleteprojectsforeditor,popup);

            QuollPopup.yesConfirmTextEntryBuilder ()
                .title (Utils.newList (prefix,title))
                .styleClassName (StyleClassNames.DELETE)
                .description (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                           Environment.formatNumber (edProjs.size ()),
                                                           ed.mainNameProperty ()))
                .confirmButtonLabel (Utils.newList (prefix,buttons,confirm))
                .cancelButtonLabel (Utils.newList (prefix,buttons,cancel))
                .onConfirm (ev ->
                {

                    Environment.scheduleImmediately (deleteEditorProjs);

                })
                .onCancel (ev ->
                {

                    Environment.scheduleImmediately (onRemoveComplete);

                })
                .build ();

        } else {

            if (onRemoveComplete != null)
            {

                Environment.scheduleImmediately (onRemoveComplete);

            }

        }

    }

    public static void showRemoveEditor (final AbstractViewer viewer,
                                         final EditorEditor   ed,
                                         final Runnable       onRemoveComplete)
    {

        // If the editor is pending then just remove them and remove the invite.
        if (ed.isPending ())
        {

            List<String> prefix = Arrays.asList (editors,editor,remove,popup);

            QuollPopup.yesConfirmTextEntryBuilder ()
                .styleClassName (StyleClassNames.DELETE)
                .title (Utils.newList (prefix,title))
                .description (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                           ed.mainNameProperty ()))
                .confirmButtonLabel (Utils.newList (prefix,buttons,confirm))
                .cancelButtonLabel (Utils.newList (prefix,buttons,cancel))
                .onConfirm (ev ->
                {

                    EditorsEnvironment.removePendingEditor (ed,
                                                            onRemoveComplete);

                })
                .build ();

            return;

        }

        final Runnable onComplete = () ->
        {

            List<String> prefix = Arrays.asList (editors,editor,remove,confirmpopup);

            QuollPopup.messageBuilder ()
                .withViewer (viewer)
                .styleClassName (StyleClassNames.DELETE)
                .title (Utils.newList (prefix,title))
                .message (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                       ed.getMainName ()))
                .build ();

        };

        java.util.List<String> prefix = Arrays.asList (editors,editor,remove,popup);

        QuollPopup.yesConfirmTextEntryBuilder ()
            .title (Utils.newList (prefix,title))
            .styleClassName (StyleClassNames.DELETE)
            .description (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                       ed.getMainName ()))
            .confirmButtonLabel (Utils.newList (prefix,buttons,confirm))
            .cancelButtonLabel (Utils.newList (prefix,buttons,cancel))
            .onConfirm (ev ->
            {

                EditorsEnvironment.removeEditor (ed,
                                                 () ->
                                                 {

                                                     EditorsUIUtils.showDeleteProjectsForEditor (Environment.getFocusedViewer (),
                                                                                                 ed,
                                                                                                 onComplete);

                                                 });

            })
            .build ();

    }

    /*
     * Maybe move this to be a Form.
     */
    public static void updateYourInfo (final AbstractViewer viewer)
    {

        java.util.List<String> prefix = Arrays.asList (editors,user,edit,info,popup);

        Form.Builder fb = Form.builder ()
            .inViewer (viewer)
            .confirmButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,confirm)))
            .cancelButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)))
            .description (Utils.newList (prefix,text));

        EditorAccount acc = EditorsEnvironment.getUserAccount ();

        final String accName = (acc.getName ());

        QuollTextField nameF = QuollTextField.builder ()
            .text (accName)
            .build ();

        fb.item (getUILanguageStringProperty (Utils.newList (prefix,labels,name)),
                 nameF);

        Image im = acc.getAvatar ();

        ImageSelector avatarSel = ImageSelector.builder ()
            .image (im)
            .styleClassName (StyleClassNames.AVATAR)
            .withViewer (viewer)
            .build ();

        fb.item (getUILanguageStringProperty (Utils.newList (prefix,labels,LanguageStrings.avatar)),
                 avatarSel);

        Form f = fb.build ();

        QuollPopup qp = QuollPopup.formBuilder ()
            .title (Utils.newList (prefix,title))
            .styleClassName (StyleClassNames.EDIT)
            .form (f)
            .build ();

        f.addEventHandler (Form.FormEvent.CANCEL_EVENT,
                           ev ->
        {

            qp.close ();

        });

        f.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
                           ev ->
        {

            f.hideError ();

            // Update the user data.
            String newName = nameF.getText ().trim ();

            if (newName.length () == 0)
            {

                f.showError (getUILanguageStringProperty (Utils.newList (prefix,errors,noname)));//"Please provide your name.");
                return;

            }

            try
            {

                EditorsEnvironment.setUserInformation (newName,
                                                       avatarSel.getImage ());

                EditorsEnvironment.sendUserInformationToAllEditors (() ->
                {

                    if (EditorsEnvironment.getEditors ().size () > 0)
                    {

                        List<String> prefix2 = Arrays.asList (editors,user,edit,info,confirmpopup);

                        QuollPopup.messageBuilder ()
                            .title (Utils.newList (prefix2,title))
                            .message (Utils.newList (prefix2,text))
                            .closeButton ()
                            .build ();

                        qp.close ();

                    }

                },
                null,
                null);

            } catch (Exception e) {

                Environment.logError ("Unable to update user information",
                                      e);

                ComponentUtils.showErrorMessage (viewer,
                                                 getUILanguageStringProperty (editors,user,edit,info,actionerror));
                                          //"Unable to update user information, please contact Quoll Writer support for assistance.");

            }

        });

    }

    /*
     * Maybe move this to be a Form.
     */
    public static void updateEditorInfo (final AbstractViewer viewer,
                                         final EditorEditor   ed)
    {

        List<String> prefix = Arrays.asList (editors,editor,edit,popup);

        Form.Builder fb = Form.builder ()
            .inViewer (viewer)
            .confirmButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,confirm)))
            .cancelButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)))
            .description (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                       ed.getMainName ()));

        String name = ed.getMyNameForEditor ();

        if (name == null)
        {

            name = ed.getMainName ();

        }

        QuollTextField nameF = QuollTextField.builder ()
            .text (name)
            .build ();

        String edOrigName = (ed.getName () != null ? ed.getName () : ed.getEmail ());

        HBox nb = new HBox ();
        nb.getChildren ().add (nameF);
        HBox.setHgrow (nameF,
                       Priority.ALWAYS);
        nb.getChildren ().add (QuollButton.builder ()
            .iconName (StyleClassNames.RESET)
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,labels,reset,tooltip),
                                                   edOrigName))
            .onAction (ev ->
            {

                nameF.setText (edOrigName);

            })
            .build ());

        fb.item (getUILanguageStringProperty (Utils.newList (prefix,labels,LanguageStrings.name)),
                 nb);

        Image im = ed.getMyAvatarForEditor ();

        if (im == null)
        {

            im = ed.getAvatar ();

        }

        ImageSelector avatarSel = ImageSelector.builder ()
            .image (im) //(im != null) ? SwingFXUtils.toFXImage (im, null) : null)
            .styleClassName (StyleClassNames.AVATAR)
            .withViewer (viewer)
            .build ();

        fb.item (getUILanguageStringProperty (Utils.newList (prefix,labels,LanguageStrings.avatar)),
                 avatarSel);

        Form f = fb.build ();

        QuollPopup qp = QuollPopup.formBuilder ()
            .title (Utils.newList (prefix,title))
            .headerIconClassName (StyleClassNames.EDIT)
            .styleSheet ("updateeditorcontactinfo")
            .form (f)
            .build ();

        f.addEventHandler (Form.FormEvent.CANCEL_EVENT,
                           ev ->
        {

            qp.close ();

        });

        f.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
                           ev ->
        {

            f.hideError ();

            // Update the user data.
            String newName = nameF.getText ().trim ();

            if (newName.length () == 0)
            {

                f.showError (getUILanguageStringProperty (editors,editor,edit,popup,errors,noname));//"Please provide your name.");
                return;

            }

            ed.setMyAvatarForEditor (avatarSel.getImage ());
            ed.setMyNameForEditor (nameF.getText ().trim ());

            try
            {

                EditorsEnvironment.updateEditor (ed);

                qp.close ();

            } catch (Exception e) {

                Environment.logError ("Unable to update editor: " + ed,
                                      e);

                ComponentUtils.showErrorMessage (viewer,
                                                 getUILanguageStringProperty (editors,editor,edit,actionerror));
                                          //"Unable to update {editor}, please contact Quoll Writer support for assistance.");

            }

        });

    }

    public static Form createSendProjectPanel (final AbstractProjectViewer viewer,
                                               final EditorEditor          ed,
                                               final Runnable              onSend,
                                               final Runnable              onCancel)
    {

        // See what the last send/update message was, if any.
        try
        {

            EditorsEnvironment.loadMessagesForEditor (ed);

        } catch (Exception e) {

            Environment.logError ("Unable to load messages for editor: " +
                                  ed,
                                  e);

            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (editors,user,sendorupdateproject,actionerror));
                                      //"Unable to show send {project} window, please contact Quoll Writer support for assistance.");

            return null;

        }

        Project p = viewer.getProject ();

        StringProperty updateText = new SimpleStringProperty ("");

        SortedSet<EditorMessage> messages = ed.getMessages (p.getId (),
                                                            NewProjectMessage.MESSAGE_TYPE,
                                                            UpdateProjectMessage.MESSAGE_TYPE);

        SortedSet<EditorMessage> projRes = ed.getMessages (p.getId (),
                                                           NewProjectResponseMessage.MESSAGE_TYPE);

        boolean update = false;

        Set<String> chapterIds = new HashSet<> ();

        if ((messages != null)
            &&
            (messages.size () > 0)
           )
        {

            java.util.List<String> prefix2 = Arrays.asList (editors,user,updateproject,popup,labels);

            // Get the last one.
            EditorMessage last = messages.last ();

            update = (projRes.size () > 0);

            AbstractProjectMessage pm = (AbstractProjectMessage) last;

            Set<Chapter> chaps = pm.getChapters ();
            String verName = pm.getProjectVersion ().getName ();

            updateText = UILanguageStringsManager.createStringPropertyWithBinding (() ->
            {

                String ut = "";

                if (last instanceof NewProjectMessage)
                {

                    if (verName != null)
                    {

                        ut = getUILanguageStringProperty (Utils.newList (prefix2,firstupdatewithversion),
                                                          verName,
                                                          Environment.formatDate (last.getWhen ())).getValue ();
                        //"You sent version <b>%s</b> of the {project} on <b>%s</b>.";

                    } else {

                        ut = getUILanguageStringProperty (Utils.newList (prefix2,firstupdate),
                                                          Environment.formatDate (last.getWhen ())).getValue ();
                                                    //)"You sent the {project} on <b>%s</b>.";

                    }

                    // TODO: Add link to view what was sent.

                }

                if (last instanceof UpdateProjectMessage)
                {

                    if (verName != null)
                    {

                        ut = getUILanguageStringProperty (Utils.newList (prefix2,lastupdatewithversion),
                                                          verName,
                                                          Environment.formatDate (last.getWhen ())).getValue ();
                                                    //"You last sent version <b>%s</b> of the {project} on <b>%s</b>.";

                    } else {

                        ut = getUILanguageStringProperty (Utils.newList (prefix2,lastupdate),
                                                          Environment.formatDate (last.getWhen ())).getValue ();
                                                    //"You last sent the {project} on <b>%s</b>.";

                    }

                }

                return "<br /><br />" + ut + getUILanguageStringProperty (Utils.newList (prefix2,updatesuffix)).getValue ();

            });
            //"  The {chapters} you previously sent have been pre-selected.";

            for (Chapter c : chaps)
            {

                chapterIds.add (c.getId ());

            }

        } else {

            // Add all chapters, first send.
            Book b = (Book) p.getBooks ().get (0);

            List<Chapter> chaps = b.getChapters ();

            for (Chapter c : chaps)
            {

                chapterIds.add (c.getId ());

            }

        }

        // Check to see if we should be sending the project as new.
        if (update)
        {

            messages = ed.getMessages (p.getId (),
                                       ProjectEditStopMessage.MESSAGE_TYPE);

            if ((messages != null)
                &&
                (messages.size () > 0)
               )
            {

                // Send a new project.  Even though the editor has stopped editing this project
                // in the past.
                update = false;

            }

        }

        java.util.List<String> prefix = Arrays.asList (editors,user,sendorupdateproject,popup);

        final boolean fupdate = update;

        Form.Builder f = Form.builder ()
            .inViewer (viewer)
            .confirmButton (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.buttons,LanguageStrings.send)))
            .cancelButton (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.buttons,LanguageStrings.cancel)))
            .description (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                       ed.getMainName (),
                                                       updateText));

        // TODO Add pervious version?
        QuollTextField version = QuollTextField.builder ()
            .build ();

        QuollTextArea notes = QuollTextArea.builder ()
            .maxChars (5000)
            .placeholder (getUILanguageStringProperty (Utils.newList (prefix,labels,LanguageStrings.notes,tooltip),
                                                       ed.getMainName ()))
            .build ();

        ZonedDateTime cal = ZonedDateTime.now ();
        cal = cal.plusDays (7);

        DatePicker date = new DatePicker (LocalDate.from (cal));

        List<Chapter> objs = new ArrayList<> (viewer.getProject ().getBooks ().get (0).getChapters ());

        TreeItem<NamedObject> root = new TreeItem<> (viewer.getProject ());

        for (Chapter c : objs)
        {

            CheckBoxTreeItem<NamedObject> ci = new CheckBoxTreeItem<> (c);
            ci.setSelected (chapterIds.contains (c.getId ()));
            root.getChildren ().add (ci);

        }

        QuollTreeView<NamedObject> chapterTree = NamedObjectTree.builder ()
            .project (viewer.getProject ())
            .root (root)
            .labelProvider (treeItem ->
            {

                NamedObject n = treeItem.getValue ();

                if (n instanceof Project)
                {

                    return new Label ();

                }

                QuollLabel l = QuollLabel.builder ()
                    .label (n.nameProperty ())
                    .styleClassName (n.getObjectType ())
                    .build ();

                l.setOnMouseClicked (ev ->
                {

                    if (ev.getButton () != MouseButton.PRIMARY)
                    {

                        return;

                    }

                    l.requestFocus ();

                    viewer.viewObject (n);

                });

                return l;

            })
            .build ();

        VBox b = new VBox ();
        VBox.setVgrow (chapterTree,
                       Priority.ALWAYS);
        QuollCheckBox cb = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (actions,selectall))
            .styleClassName (StyleClassNames.SELECT)
            .build ();
        cb.setOnAction (ev ->
        {

            chapterTree.walkTree (ti ->
            {

                if (ti instanceof CheckBoxTreeItem)
                {

                    CheckBoxTreeItem<NamedObject> cti = (CheckBoxTreeItem<NamedObject>) ti;

                    cti.setSelected (cb.isSelected ());

                }

            });

        });
        b.getChildren ().addAll (cb, new QScrollPane (chapterTree));

        f.item (getUILanguageStringProperty (Utils.newList (prefix,labels,project)),
                QuollLabel.builder ()
                    .label (new SimpleStringProperty (p.getName ()))
                    .build ());

        f.item (getUILanguageStringProperty (Utils.newList (prefix,labels,LanguageStrings.version)),
                version);

        f.item (getUILanguageStringProperty (Utils.newList (prefix,labels,LanguageStrings.notes,text)),
                notes);

        f.item (getUILanguageStringProperty (Utils.newList (prefix,labels,dueby)),
                date);

        f.item (getUILanguageStringProperty (Utils.newList (prefix,labels,chapters)),
                b);

        Form form = f.build ();

        form.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
                              ev ->
        {

            form.hideError ();

            Set<Chapter> chapters = new LinkedHashSet<> ();

            Set<StringProperty> errors = new LinkedHashSet<> ();

            chapterTree.walkTree (item ->
            {

                if (item instanceof CheckBoxTreeItem)
                {

                    CheckBoxTreeItem<NamedObject> cti = (CheckBoxTreeItem<NamedObject>) item;

                    if (!cti.isSelected ())
                    {

                        return;

                    }

                    if (cti.getValue () instanceof Chapter)
                    {

                        chapters.add ((Chapter) cti.getValue ());

                    }

                }

            });

            if (chapters.size () == 0)
            {

                // Show the error.
                errors.add (getUILanguageStringProperty (editors,user,sendorupdateproject,popup,LanguageStrings.errors,nochapters));
                //"Please select at least 1 {chapter}.";

            } else {

                for (Chapter c : chapters)
                {

                    ChapterEditorPanelContent qp = viewer.getEditorForChapter (c);

                    if (qp != null)
                    {

                        if (qp.hasUnsavedChanges ())
                        {

                            errors.add (getUILanguageStringProperty (editors,user,sendorupdateproject,popup,LanguageStrings.errors,unsavedchanges));
                            //"One of the selected {chapters} has unsaved changes, please save your work before sending the {project}.";

                            break;

                        }

                    }

                }

            }

            if (notes.getText ().trim ().length () > 5000)
            {

                errors.add (getUILanguageStringProperty (editors,user,sendorupdateproject,popup,LanguageStrings.errors,maxchars));
                //"Notes can be a maximum of 5000 characters.";

            }

            if (errors.size () > 0)
            {

                form.showErrors (errors);
                return;

            }

            form.getConfirmButton ().setDisable (true);

            String n = notes.getText ().trim ();

            if (n.length () == 0)
            {

                n = null;

            }

            String verName = version.getText ().trim ();

            if (verName.length () == 0)
            {

                verName = null;

            }

            ProjectVersion pv = new ProjectVersion ();
            pv.setName (verName);
            pv.setDueDate (Utils.localDateToDate (date.getValue ()));
            pv.setDescription (new StringWithMarkup (n));

            // Need to snapshot the chapters.
            Set<Chapter> nchapters = null;

            try
            {

                nchapters = viewer.snapshotChapters (chapters,
                                                     pv);

            } catch (Exception e) {

                Environment.logError ("Unable to snapshot chapters: " +
                                      chapters,
                                      e);

                ComponentUtils.showErrorMessage (viewer,
                                                 getUILanguageStringProperty (editors,user,sendorupdateproject,actionerror));
                                          //"Unable to send new project to {editor}, please contact Quoll Writer support for assistance.");

                return;

            }

            EditorMessage mess = null;

            if (fupdate)
            {

                mess = new UpdateProjectMessage (viewer.getProject (),
                                                 nchapters,
                                                 pv,
                                                 ed);

            } else {

                mess = new NewProjectMessage (viewer.getProject (),
                                              nchapters,
                                              pv,
                                              ed,
                                              ed.getEditorStatus () == EditorEditor.EditorStatus.pending ? EditorsEnvironment.getUserAccount () : null);

            }

            // Since we are sending the message we have dealt with it.
            mess.setDealtWith (true);

            form.showLoading (getUILanguageStringProperty (Utils.newList (prefix,loading)));

            EditorsEnvironment.sendMessageToEditor (mess,
                                                    // On send.
                                                    () ->
                                                    {

                                                        // See if we already have the project editor, this can happen if we have previously
                                                        // sent the project but they deleted it and we are re-sending.
                                                        ProjectEditor pe = null;

                                                        try
                                                        {

                                                            pe = EditorsEnvironment.getProjectEditor (viewer.getProject (),
                                                                                                      ed);

                                                        } catch (Exception e) {

                                                            Environment.logError ("Unable to get project editor for project: " +
                                                                                  viewer.getProject () +
                                                                                  " and editor: " +
                                                                                  ed,
                                                                                  e);

                                                            // Oh bugger...

                                                        }

                                                        if (pe == null)
                                                        {

                                                            pe = new ProjectEditor (viewer.getProject (),
                                                                                    ed);

                                                            pe.setStatus (ProjectEditor.Status.invited);

                                                            pe.statusMessageProperty ().unbind ();
                                                            pe.statusMessageProperty ().bind (getUILanguageStringProperty (Arrays.asList (editors,user,sendproject,editorstatus),
                                                                                                //"{Project} sent: %s",
                                                                                                                           Environment.formatDate (new Date ())));

                                                            // Add the editor to the list of editors
                                                            // for the project.  A little dangerous to do it here
                                                            // since it's not in the same transaction as the message.
                                                            // TODO: Maybe have the message have a "side-effect" or "after save"
                                                            // TODO: that will add the editor to the project in the same transaction.
                                                            try
                                                            {

                                                                EditorsEnvironment.addProjectEditor (pe);

                                                                viewer.getProject ().addProjectEditor (pe);

                                                            } catch (Exception e) {

                                                                // Goddamn it!
                                                                // Nothing worse than having to show an error and success at the same time.
                                                                Environment.logError ("Unable to add editor: " +
                                                                                      ed +
                                                                                      " to project: " +
                                                                                      viewer.getProject (),
                                                                                      e);

                                                                ComponentUtils.showErrorMessage (viewer,
                                                                                                 getUILanguageStringProperty (editors,user,sendorupdateproject,actionerror));
                                                                                          //"Unable to add {editor} " + ed.getMainName () + " to the {project}.  Please contact Quoll Writer support for assistance.");

                                                            }

                                                        } else {

                                                            try
                                                            {

                                                                // Update them to be current.
                                                                pe.setCurrent (true);
                                                                pe.setEditorFrom (new Date ());
                                                                pe.setEditorTo (null);

                                                                pe.statusMessageProperty ().unbind ();
                                                                pe.statusMessageProperty ().bind (getUILanguageStringProperty (Arrays.asList (editors,user,updateproject,editorstatus),
                                                                                                    //"{Project} updated: %s",
                                                                                                                               Environment.formatDate (new Date ())));

                                                                EditorsEnvironment.updateProjectEditor (pe);

                                                            } catch (Exception e) {

                                                                // Goddamn it!
                                                                // Nothing worse than having to show an error and success at the same time.
                                                                Environment.logError ("Unable to add editor: " +
                                                                                      ed +
                                                                                      " to project: " +
                                                                                      viewer.getProject (),
                                                                                      e);

                                                                ComponentUtils.showErrorMessage (viewer,
                                                                                                 getUILanguageStringProperty (editors,user,sendorupdateproject,actionerror));
                                                                                          //"Unable to add {editor} " + ed.getMainName () + " to the {project}.  Please contact Quoll Writer support for assistance.");

                                                            }

                                                        }

                                                        form.hideLoading ();

                                                        QuollPopup.messageBuilder ()
                                                            .inViewer (viewer)
                                                            .title (editors,user,sendorupdateproject,confirmpopup,title)
                                                            .message (getUILanguageStringProperty (Arrays.asList (editors,user,sendorupdateproject,confirmpopup,text),
                                                                                                   viewer.getProject ().getName (),
                                                                                                   ed.getMainName ()))
                                                            .closeButton ()
                                                            .build ();

                                                        if (onSend != null)
                                                        {

                                                            Environment.scheduleImmediately (onSend);

                                                        }

                                                    },
                                                    // On cancel of login.
                                                    () ->
                                                    {

                                                        form.getConfirmButton ().setDisable (false);
                                                        form.hideLoading ();

                                                    },
                                                    null);

        });

        form.addEventHandler (Form.FormEvent.CANCEL_EVENT,
                              ev ->
        {

            if (ed.isPending ())
            {

                InviteMessage invite = new InviteMessage (EditorsEnvironment.getUserAccount ());

                invite.setEditor (ed);

                form.showLoading (getUILanguageStringProperty (Utils.newList (prefix,loading)));

                // Send an invite.
                EditorsEnvironment.sendMessageToEditor (invite,
                                                        () ->
                                                        {

                                                            form.hideLoading ();
                                                            form.getConfirmButton ().setDisable (false);

                                                            QuollPopup.messageBuilder ()
                                                                .inViewer (viewer)
                                                                .title (editors,user,invitesent,popup,title)
                                                                .message (getUILanguageStringProperty (Arrays.asList (editors,user,invitesent,popup,text),
                                                                                                       ed.getEmail ()))
                                                                .closeButton ()
                                                                .build ();

                                                        },
                                                        onCancel,
                                                        null);

            }

        });

        return form;

    }

    public static void showSendProject (final AbstractProjectViewer viewer,
                                        final EditorEditor          ed,
                                        final Runnable              onSend)
    {

        QuollPopup qp = QuollPopup.formBuilder ()
            .title (editors,user,sendproject,popup,title)
            .styleClassName (StyleClassNames.SEND)
            .styleSheet ("sendproject")
            .form (EditorsUIUtils.createSendProjectPanel (viewer,
                                                          ed,
                                                          () ->
                                                          {

                                                              // Need to close the popup.

                                                              if (onSend != null)
                                                              {

                                                                  Environment.scheduleImmediately (onSend);

                                                              }

                                                          },
                                                          null))
            .build ();

    }

    public static void showUpdateProject (final AbstractProjectViewer viewer,
                                          final EditorEditor          ed,
                                          final Runnable              onSend)
    {

        if (ed.isPending ())
        {

            throw new IllegalStateException ("Can only update a project for a non-pending editor.");

        }

        QuollPopup qp = QuollPopup.formBuilder ()
            .title (editors,user,updateproject,popup,title)
            .styleClassName (StyleClassNames.SEND)
            .styleSheet ("sendproject")
            .form (EditorsUIUtils.createSendProjectPanel (viewer,
                                                          ed,
                                                          () ->
                                                          {

                                                              // Need to close the popup.

                                                              if (onSend != null)
                                                              {

                                                                  Environment.scheduleImmediately (onSend);

                                                              }

                                                          },
                                                          null))
            .build ();

    }

    public static void showSendUnsentComments (final AbstractProjectViewer viewer,
                                               final Runnable              onSend)
    {

        List<String> prefix = Arrays.asList (editors,user,sendunsentcomments,popup);

        final EditorEditor ed = viewer.getProject ().getForEditor ();

        Form.Builder fb = Form.builder ()
            .inViewer (viewer)
            .description (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                       ed.getMainName ()))
            .confirmButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,LanguageStrings.send)))
            .cancelButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,LanguageStrings.cancel)));

        QuollTextArea genComments = QuollTextArea.builder ()
            .placeholder (Utils.newList (prefix,labels,notes,tooltip))
            .maxChars (5000)
            .build ();

        List<Chapter> objs = new ArrayList<> (viewer.getProject ().getBooks ().get (0).getChapters ());

        TreeItem<NamedObject> root = new TreeItem<> (viewer.getProject ());

        for (Chapter c : objs)
        {

            CheckBoxTreeItem<NamedObject> ci = new CheckBoxTreeItem<> (c);
            root.getChildren ().add (ci);

            for (Note n : c.getNotes ())
            {

                if (n.isDealtWith ())
                {

                    continue;

                }

                CheckBoxTreeItem<NamedObject> nci = new CheckBoxTreeItem<> (n);
                ci.getChildren ().add (nci);

            }

        }

        QuollTreeView<NamedObject> tree = NamedObjectTree.builder ()
            .project (viewer.getProject ())
            .root (root)
            .labelProvider (treeItem ->
            {

                NamedObject n = treeItem.getValue ();

                if (n instanceof Project)
                {

                    return new Label ();

                }

                QuollLabel l = QuollLabel.builder ()
                    .label (n.nameProperty ())
                    .styleClassName (n.getObjectType ())
                    .build ();

                l.setOnMouseClicked (ev ->
                {

                    if (ev.getButton () != MouseButton.PRIMARY)
                    {

                        return;

                    }

                    l.requestFocus ();

                    viewer.viewObject (n);

                });

                return l;

            })
            .build ();

        ScrollPane sp = new ScrollPane (tree);

        fb.item (getUILanguageStringProperty (Utils.newList (prefix,labels,LanguageStrings.notes,text)),
                 genComments);

        fb.item (getUILanguageStringProperty (Utils.newList (prefix,labels,comments)),
                 sp);

        Form f = fb.build ();

        QuollPopup qp = QuollPopup.formBuilder ()
            .inViewer (viewer)
            .styleClassName (StyleClassNames.SEND)
            .title (Utils.newList (prefix,title))
            .form (f)
            .build ();

        f.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
                           ev ->
        {

            // Get a count.
            f.hideError ();

            Set<NamedObject> selected = new LinkedHashSet<> ();

            tree.walkTree (item ->
            {

                if (item instanceof CheckBoxTreeItem)
                {

                    CheckBoxTreeItem<NamedObject> cti = (CheckBoxTreeItem<NamedObject>) item;

                    if (!cti.isSelected ())
                    {

                        return;

                    }

                    if (cti.getValue () instanceof Note)
                    {

                        selected.add (cti.getValue ());

                    }

                }

            });

            final Set<Note> comments = new LinkedHashSet<> ();

            for (NamedObject n : selected)
            {

                if (n instanceof Note)
                {

                    comments.add ((Note) n);

                }

            }

            if (comments.size () == 0)
            {

                f.showError (getUILanguageStringProperty (Utils.newList (prefix,errors,novalue)));
                return;

            }

            String gc = genComments.getText ().trim ();

            if (gc.length () == 0)
            {

                gc = null;

            }

            ProjectCommentsMessage mess = new ProjectCommentsMessage (viewer.getProject (),
                                                                      gc,
                                                                      comments,
                                                                      viewer.getProject ().getProjectVersion (),
                                                                      ed);

            // Since we are sending the message we have dealt with it.
            mess.setDealtWith (true);

            // Do this here because we don't know how long it will take to actually send.
            final Date sentDate = new Date ();

            EditorsEnvironment.sendMessageToEditor (mess,
                                                    // On send.
                                                    () ->
                                                    {

                                                        // Update the comments to be dealt with.
                                                        for (Note n : comments)
                                                        {

                                                            n.setDealtWith (sentDate);

                                                        }

                                                        try
                                                        {

                                                            // Should really change the underlying method
                                                            // but can't be bothered at the moment!
                                                            // TODO
                                                            viewer.saveObjects (new ArrayList (comments),
                                                                                true);

                                                        } catch (Exception e) {

                                                            Environment.logError ("Unable to update comments",
                                                                                  e);

                                                            ComponentUtils.showErrorMessage (viewer,
                                                                                             getUILanguageStringProperty (editors,user,sendunsentcomments,actionerror));
                                                                                      //"Your comments have been sent but Quoll Writer is unable to update the comments in your local db, please contact Quoll Writer support for assistance.");

                                                            return;

                                                        }

                                                        // Fire an event for each note.
                                                        for (Note n : comments)
                                                        {

                                                            viewer.fireProjectEvent (ProjectEvent.Type.note,
                                                                                     ProjectEvent.Action.edit,
                                                                                     n);

                                                        }

                                                        QuollPopup.messageBuilder ()
                                                            .title (editors,user,sendunsentcomments,confirmpopup,title)
                                                            .message (getUILanguageStringProperty (Arrays.asList (editors,user,sendunsentcomments,confirmpopup,text),
                                                                                                   ed.getMainName ()))
                                                            .inViewer (viewer)
                                                            .build ();

                                                        qp.close ();

                                                        if (onSend != null)
                                                        {

                                                            Environment.scheduleImmediately (onSend);

                                                        }

                                                    },
                                                    // On cancel of login.
                                                    null,
                                                    null);


        });

        f.addEventHandler (Form.FormEvent.CANCEL_EVENT,
                           ev ->
        {

            qp.close ();

        });

    }

    public static void showReportMessage (final MessageBox     mess,
                                          final AbstractViewer viewer,
                                          final IPropertyBinder binder)
    {

        URL url = null;

        try
        {

            url = EditorsEnvironment.getReportMessageURL ();

        } catch (Exception e) {

            Environment.logError ("Unable to get report message url",
                                  e);

            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (editors,user,reportmessage,actionerror));
                                      //"Unable to show report message popup, please contact Quoll Writer support for assistance.");

            return;

        }

        final URL reportURL = url;

        List<String> prefix = Arrays.asList (editors,user,reportmessage,popup);

        Form.Builder fb = Form.builder ()
            .inViewer (viewer)
            .description (Utils.newList (prefix,text))
            .confirmButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,send)))
            .cancelButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)));

        fb.sectionTitle (Utils.newList (prefix,sectiontitles,message));

        Image im = null;

        try
        {

            im = UIUtils.getImageOfNode (mess);

        } catch (Exception e) {

            Environment.logError ("Unable to create message box for: " +
                                  mess,
                                  e);

            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (editors,user,reportmessage,actionerror));
                                      //"Unable to show report message popup, please contact Quoll Writer support for assistance.");

            return;

        }

        ImageView iv = new ImageView (im);

        fb.item (iv);
        fb.sectionTitle (Utils.newList (prefix,sectiontitles,from));

        EditorInfoBox edB = null;

        try
        {

            edB = new EditorInfoBox (mess.getMessage ().getEditor (),
                                     viewer,
                                     false,
                                     binder);

        } catch (Exception e) {

            Environment.logError ("Unable to create editor box for: " +
                                  mess.getMessage ().getEditor (),
                                  e);

            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (editors,user,reportmessage,actionerror));
                                      //"Unable to show report message popup, please contact Quoll Writer support for assistance.");

            return;

        }

        fb.item (edB);

        fb.sectionTitle (Utils.newList (prefix,sectiontitles,reason));

        QuollTextArea notes = QuollTextArea.builder ()
            .styleClassName (StyleClassNames.NOTES)
            .placeholder (Utils.newList (prefix,labels,reason,tooltip))
            .maxChars (5000)
            .build ();

        fb.item (notes);

        Form f = fb.build ();

        QuollPopup qp = QuollPopup.formBuilder ()
            .withViewer (viewer)
            .form (f)
            .styleClassName (StyleClassNames.REPORTMESSAGE)
            .title (Utils.newList (prefix,title))
            .build ();

        f.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
                           ev ->
        {

            f.hideError ();

            if (notes.getText ().trim ().length () == 0)
            {

                f.showError (getUILanguageStringProperty (prefix,errors,novalue));
                return;

            }

            f.showLoading (getUILanguageStringProperty (Utils.newList (prefix,loading)));

            EditorsEnvironment.goOnline (getUILanguageStringProperty (editors,login,reasons,reportmessage),
                                         //"To report a message you must first login.",
                                         () ->
                                         {

                                            Map data = new HashMap ();
                                            data.put ("message",
                                                      mess.getMessage ());
                                            data.put ("editor",
                                                      mess.getMessage ().getEditor ());
                                            data.put ("user",
                                                      EditorsEnvironment.getUserAccount ());
                                            data.put ("reason",
                                                      notes.getText ().trim ());

                                            Map<String, String> headers = new HashMap ();
                                            headers.put ("Authorization",
                                                         EditorsEnvironment.getUserAccount ().getWebServiceSessionId ());

                                            try
                                            {

                                                Utils.postToURL (reportURL,
                                                                 headers,
                                                                 JSONEncoder.encode (data,
                                                                                     true,
                                                                                     ""),
                                                                 (ret, resCode) ->
                                                                 {

                                                                     qp.close ();

                                                                     QuollPopup.messageBuilder ()
                                                                        .withViewer (viewer)
                                                                        .title (editors,user,reportmessage,confirmpopup,title)
                                                                        .message (editors,user,reportmessage,confirmpopup,text)
                                                                        .build ();

                                                                 },
                                                                 (ret, resCode) ->
                                                                 {

                                                                    qp.close ();

                                                                    Environment.logError ("Unable to report message:" +
                                                                                          ev);

                                                                    ComponentUtils.showErrorMessage (viewer,
                                                                                                     getUILanguageStringProperty (editors,user,reportmessage,actionerror));
                                                                                                  //"Unable to report the message, please contact Quoll Writer support for assistance.");

                                                                 },
                                                                 (exp) ->
                                                                 {

                                                                    qp.close ();

                                                                    Environment.logError ("Unable to report message, got fail",
                                                                                          exp);

                                                                    ComponentUtils.showErrorMessage (viewer,
                                                                                                     getUILanguageStringProperty (editors,user,reportmessage,actionerror));
                                                                                                  //"Unable to report the message, please contact Quoll Writer support for assistance.");

                                                                },
                                                                null);

                                            } catch (Exception e) {

                                                qp.close ();

                                                Environment.logError ("Unable to report message",
                                                                      e);

                                                ComponentUtils.showErrorMessage (viewer,
                                                                                 getUILanguageStringProperty (editors,user,reportmessage,actionerror));
                                                                          //"Unable to report the message, please contact Quoll Writer support for assistance.");

                                            }

                                         },
                                         null,
                                         null);

        });

        f.addEventHandler (Form.FormEvent.CANCEL_EVENT,
                           ev ->
        {

            qp.close ();

        });

    }

    public static void showRegister (final AbstractViewer viewer)
                              throws Exception
    {

        QuollPopup qp = viewer.getPopupById (EditorRegister.POPUP_ID);

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        new EditorRegister (viewer).show ();

    }

    public static void hideLogin ()
    {

        if (EditorsUIUtils.editorLogin == null)
        {

            return;

        }

        EditorsUIUtils.editorLogin.close ();

    }

    public static void showChangePassword (final AbstractViewer viewer)
    {

        java.util.List<String> prefix = Arrays.asList (editors,user,changepassword,popup);

        Form.Builder fb = Form.builder ()
            .description (prefix,text)
            .confirmButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,confirm)))
            .cancelButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)));

        PasswordField pass1 = new PasswordField ();
        PasswordField pass2 = new PasswordField ();

        fb.item (getUILanguageStringProperty (Utils.newList (prefix,labels,newpassword)),
                 pass1);

        fb.item (getUILanguageStringProperty (Utils.newList (prefix,labels,confirmpassword)),
                 pass2);

        Form form = fb.build ();

        QuollPopup qp = QuollPopup.formBuilder ()
            .styleClassName (StyleClassNames.CHANGEPASSWORD)
            .headerIconClassName (StyleClassNames.EDIT)
            .title (Utils.newList (prefix,title))
            .inViewer (viewer)
            .form (form)
            .build ();

        form.addEventHandler (Form.FormEvent.CANCEL_EVENT,
                              ev ->
        {

            qp.close ();

        });

        form.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
                              ev ->
        {

            List<String> prefix2 = Arrays.asList (editors,user,changepassword,popup,errors);

            form.hideError ();

            // Update the user data.
            String pwd = pass1.getText ();

            if (pwd.length () == 0)
            {

                form.showError (getUILanguageStringProperty (Utils.newList (prefix2,nopassword)));
                return;

            }

            String pwd2 = pass2.getText ();

            if (pwd2.length () == 0)
            {

                form.showError (getUILanguageStringProperty (Utils.newList (prefix2,confirmpassword)));
                return;

            }

            if (!pwd.equals (pwd2))
            {

                form.showError (getUILanguageStringProperty (Utils.newList (prefix2,nomatch)));
                return;

            }

            EditorsEnvironment.updateUserPassword (pwd);

            qp.close ();

        });

    }

    public static void showLogin (final AbstractViewer viewer,
                                  final StringProperty loginReason,
                                  final Runnable       onLogin,
                                  final Runnable       onCancel)
    {

        UIUtils.runLater (() ->
        {

            EditorsUIUtils.editorLogin.setVisible (true);

            EditorsUIUtils.editorLogin.show (viewer,
                                             loginReason,
                                             onLogin,
                                             onCancel);

        });

    }

    public static void showLoginError (EditorsWebServiceResult res)
    {

        StringProperty reason = null;
        //"Please contact Quoll Writer support for assistance, quoting error code: " + res.getReturnCode () + (res.getReturnCode () == 401 ? ("/" + res.getErrorType ()) : "");

        if (res.getReturnCode () == 401)
        {

            String errType = res.getErrorType ();

            if (errType != null)
            {

                if (errType.equals ("InvalidCredentials"))
                {

                    reason = getUILanguageStringProperty (editors,login,errors,invalidcredentials);
                    //"Please check your email/password and try again.";

                }

                if (errType.equals ("AccountNotActive"))
                {

                    reason = getUILanguageStringProperty (editors,login,errors,inactiveaccount);

                }

            }

        } else {

            reason = getUILanguageStringProperty (Arrays.asList (editors,login,errors,general),
                                                  res.getReturnCode () + "/" + res.getErrorType ());

        }

        EditorsUIUtils.showLoginError (reason);
        //"Unable to login to the Editors service.<br />" + reason);

    }

    public static void showLoginError (StringProperty                error)
    {

        EditorsUIUtils.showLoginError (error,
                                       null,
                                       null);

    }

    public static void showLoginError (StringProperty  error,
                                       Runnable        onLogin,
                                       Runnable        onCancel)
    {

        if (error == null)
        {

            throw new NullPointerException ("Error must be provided.");

        }

        UIUtils.runLater (() ->
        {

        /*
        if (EditorsUIUtils.editorLogin.getParent () == null)
        {

            AbstractProjectViewer viewer = Environment.getFocusedProjectViewer ();

            UIUtils.showErrorMessage (viewer,
                                      "Unable to show login form, please contact Quoll Writer support for assistance.");

            return;

        }
        */

            EditorsUIUtils.editorLogin.show ();

            EditorsUIUtils.editorLogin.showError (error);

            if (onLogin != null)
            {

                EditorsUIUtils.editorLogin.setOnLogin (onLogin);

            }

            if (onCancel != null)
            {

                EditorsUIUtils.editorLogin.setOnCancel (onCancel);
                //EditorsUIUtils.editorLogin.setOnClose (onCancel);

            }

        });

        //EditorsUIUtils.editorLogin.resize ();

    }

    public static void showContacts (      Set<EditorEditor>     editors,
                                           StringProperty        title,
                                     final AbstractProjectViewer viewer,
                                           Consumer<EditorEditor> onSelect,
                                           Node                   extra)
    {

        ShowObjectSelectPopup.<EditorEditor>builder ()
            .withViewer (viewer)
            .title (title)
            .styleClassName (StyleClassNames.OBJECTSELECT)
            .headerIconClassName (StyleClassNames.CONTACTS)
            //.styleSheet ("selectcontact")
            .popupId ("showcontacts")
            .objects (editors)
            .showBelowObjects (extra)
            .cellProvider ((obj, popupContent) ->
            {

               QuollLabel l = QuollLabel.builder ()
                    .label (obj.mainNameProperty ())
                    .styleClassName (StyleClassNames.CONTACT)
                    .build ();
               IconBox ib = IconBox.builder ()
                    .image (obj.mainAvatarProperty ())
                    .build ();

                ib.pseudoClassStateChanged (StyleClassNames.NOAVATAR_PSEUDO_CLASS, (obj.mainAvatarProperty ().getValue () == null));
                l.setGraphic (ib);

                return l;

            })
            .build ()
            .show ();

    }

    public static void showInviteEditor (final AbstractViewer viewer)
    {

        final java.util.List<String> prefix = Arrays.asList (editors,user,inviteeditor,popup);

        String popupId = "inviteeditor";

        QuollPopup.textEntryBuilder ()
            .popupId (popupId)
            .styleClassName (StyleClassNames.INVITEEDITOR)
            .headerIconClassName (StyleClassNames.ADD)
            .title (Utils.newList (prefix,title))
            .description (Utils.newList (prefix,text))
            .removeOnClose (true)
            .hideOnEscape (true)
            .withClose (true)
            .withViewer (viewer)
            .confirmButtonLabel (Utils.newList (prefix,buttons,invite))
            .cancelButtonLabel (Utils.newList (prefix,buttons,cancel))
            .onConfirm (ev ->
            {

                TextField tf = (TextField) ev.getForm ().lookup ("#text");

                String v = tf.getText ().trim ();

                EditorsEnvironment.sendInvite (v,
                                               viewer);

                viewer.getPopupById (popupId).close ();

            })
            .onCancel (ev ->
            {

                //viewer.getPopupById (popupId).close ();

            })
            .validator (v ->
            {

                if ((v == null)
                    ||
                    (v.trim ().equals (""))
                   )
                {

                    return getUILanguageStringProperty (Utils.newList (prefix,errors,noemail));
                    //"The email address must be specified.";

                }

                if (v.indexOf ("@") < 0)
                {

                    return getUILanguageStringProperty (Utils.newList (prefix,errors,invalidemail));
                    //"Please provide a valid email address.";

                }

                if (v.equals (EditorsEnvironment.getUserAccount ().getEmail ()))
                {

                    return getUILanguageStringProperty (Utils.newList (prefix,errors,self));
                    //"Inviting yourself?  O_o";

                }

                EditorEditor ed = EditorsEnvironment.getEditorByEmail (v);

                // Check to see if we already have the editor.
                if (ed != null)
                {

                    String type = alreadyinvited;
                    //"You have already invited <b>%s (%s)</b>.";

                    if (ed.getEditorStatus () == EditorEditor.EditorStatus.rejected)
                    {

                        type = previousrejected;
                        //"You have already invited: <b>%s (%s)</b>.  Your invitation was rejected.";

                    }

                    if (ed.isPrevious ())
                    {

                        type = previous;
                        //"<b>%s (%s)</b> is a previous {contact}.";

                    }

                    return getUILanguageStringProperty (Utils.newList (prefix,errors,type),
                                                        ed.mainNameProperty (),
                                                        ed.emailProperty ());

                }

                return null;

            })
            .build ();

    }

    public static void showResultError (final EditorsWebServiceResult res)
    {

        ComponentUtils.showErrorMessage (new SimpleStringProperty (res.getErrorMessage ()));

    }

    public static TreeItem<NamedObject> createChapterTreeItem (Chapter c)
    {

        TreeItem<NamedObject> cii = new TreeItem<> (c);

        cii.getChildren ().addAll (c.getNotes ().stream ()
            .map (n -> new TreeItem<NamedObject> (n))
            .collect (Collectors.toList ()));

        return cii;

    }

    public static TreeItem<NamedObject> createChaptersTree (Project proj)
    {

        TreeItem<NamedObject> root = new TreeItem<> (proj);

        Book b = (Book) proj.getBooks ().get (0);

        root.getChildren ().addAll (b.getChapters ().stream ()
            .map (c -> EditorsUIUtils.createChapterTreeItem (c))
            .collect (Collectors.toList ()));

        return root;

    }

    public static void showProjectComments (final ProjectCommentsMessage message,
                                            final AbstractViewer         parentViewer,
                                            final Consumer<ProjectCommentsViewer> onShow)
    {

        // Load up the project with the specific text.
        // See if we have a project viewer for the project.
        ProjectInfo proj = null;

        try
        {

            proj = Environment.getProjectById (message.getForProjectId (),
                                               (message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE));

        } catch (Exception e) {

            Environment.logError ("Unable to get project for: " +
                                  message.getForProjectId (),
                                  e);

            ComponentUtils.showErrorMessage (parentViewer,
                                             getUILanguageStringProperty (project,actions,openproject,openerrors,comments));
                                      //"Unable to show {comments}, please contact Quoll Writer support for assistance.");

            return;

        }

        if (proj == null)
        {

            Environment.logError ("No project for: " +
                                  message.getForProjectId ());

            ComponentUtils.showErrorMessage (parentViewer,
                                             getUILanguageStringProperty (project,actions,openproject,openerrors,comments));
                                      //"Unable to show {comments}, please contact Quoll Writer support for assistance.");

            return;

        }

        final ProjectInfo _proj = proj;

        Consumer<String> open = pwd ->
        {

            //String pwd = _proj.getFilePassword ();

            if ((pwd != null)
                &&
                (pwd.equals (""))
               )
            {

                pwd = null;

            }

            Set<Chapter> chaps = null;

            try
            {

                chaps = Environment.getVersionedChapters (_proj,
                                                          message.getChapters (),
                                                          pwd);

            } catch (Exception e) {

                Environment.logError ("Unable to get versioned chapters for project: " +
                                      _proj,
                                      e);

                ComponentUtils.showErrorMessage (parentViewer,
                                                 getUILanguageStringProperty (project,actions,openproject,openerrors,comments));
                                          //"Unable to show {comments}, please contact Quoll Writer support for assistance.");

                return;

            }

            ProjectVersion pv = null;

            try
            {

                pv = Environment.getProjectVersionById (_proj,
                                                        message.getProjectVersion ().getId (),
                                                        pwd);

            } catch (Exception e) {

                Environment.logError ("Unable to get project version: " +
                                      message.getProjectVersion ().getId () +
                                      " for project: " +
                                      _proj,
                                      e);

                ComponentUtils.showErrorMessage (parentViewer,
                                                 getUILanguageStringProperty (project,actions,openproject,openerrors,comments));
                                          //"Unable to show {comments}, please contact Quoll Writer support for assistance.");

                return;

            }

            if (pv == null)
            {

                Environment.logError ("Unable to find project version: " +
                                      message.getProjectVersion ().getId () +
                                      " for project: " +
                                      _proj);

                ComponentUtils.showErrorMessage (parentViewer,
                                                 getUILanguageStringProperty (project,actions,openproject,openerrors,comments));
                                          //"Unable to show {comments}, please contact Quoll Writer support for assistance.");

                return;


            }

            try
            {

                // Need to "fill up" the project with the chapters and comments.
                // Create a new project object just to be safe (in case the getProjectById call changes in the future).
                Project np = new Project ();
                np.setName (_proj.getName ());
                np.setProjectVersion (pv);

                np.setType (Project.EDITOR_PROJECT_TYPE);
                np.setId (message.getForProjectId ());
                np.setName (message.getForProjectName ());

                Book b = new Book (np,
                                   np.getName ());

                np.addBook (b);

                // Need to prevent an O^n performance hit here.

                Map<String, Chapter> kchaps = new HashMap<> ();

                for (Chapter c : chaps)
                {

                    b.addChapter (c);

                    kchaps.put (c.getId (),
                                c);

                    c.setEditPosition (-1);
                    c.setEditComplete (false);

                }

                long k = 1;

                for (Note n : message.getComments ())
                {

                    // Need to give it a fake key.
                    n.setKey (k++);
                    n.setType (Note.EDIT_NEEDED_OBJECT_TYPE);

                    // Get the fake chapter.
                    Chapter fakec = n.getChapter ();

                    // Get the real chapter.
                    Chapter realc = kchaps.get (fakec.getId ());

                    if (realc == null)
                    {

                        // God damnit...
                        // TODO: Handle when a chapter no longer exists but have comments for it.

                    }

                    realc.addNote (n);

                }

                ProjectCommentsViewer pcv = new ProjectCommentsViewer (np,
                                                                       message);

                pcv.createViewer ();
                pcv.init (null);

                if (onShow != null)
                {

                    UIUtils.runLater (() ->
                    {

                        onShow.accept (pcv);

                    });

                }

            } catch (Exception e) {

                Environment.logError ("Unable to view comments for project: " +
                                      _proj,
                                      e);

                ComponentUtils.showErrorMessage (parentViewer,
                                                 getUILanguageStringProperty (project,actions,openproject,openerrors,comments));
                                          //"Unable to show {comments}, please contact Quoll Writer support for assistance.");

                return;

            }

        };

        UIUtils.askForPasswordForProject (proj,
                                          null,
                                          open,
                                          null,
                                          parentViewer);

    }

    public static void showProjectUpdate (final UpdateProjectMessage message,
                                          final AbstractViewer       parentViewer,
                                          final Runnable             onShow)
    {

        // Load up the project with the specific text.
        // See if we have a project viewer for the project.
        ProjectInfo proj = null;

        try
        {

            proj = Environment.getProjectById (message.getForProjectId (),
                                               Project.EDITOR_PROJECT_TYPE);

        } catch (Exception e) {

            Environment.logError ("Unable to get project for: " +
                                  message.getForProjectId (),
                                  e);

            ComponentUtils.showErrorMessage (parentViewer,
                                             getUILanguageStringProperty (editors,messages,projectupdated,actionerror));
                                      //"Unable to show {project} update, please contact Quoll Writer support for assistance.");

            return;

        }

        final ProjectInfo _proj = proj;

        if (proj == null)
        {

            QuollPopup.messageBuilder ()
                .message (editors,messages,projectupdated,errors,novalue)
                .inViewer (parentViewer)
                .build ();

            message.setDealtWith (true);

            try
            {

                EditorsEnvironment.updateMessage (message);

            } catch (Exception e) {

                Environment.logError ("Unable to update project message: " +
                                      message,
                                      e);

            }

            return;

        }

        Consumer<String> open = (pwd) ->
        {

            if (pwd.equals (""))
            {

                pwd = null;

            }

            try
            {

                Environment.updateToNewVersions (_proj,
                                                 message.getProjectVersion (),
                                                 message.getChapters (),
                                                 pwd);

                message.setDealtWith (true);

                EditorsEnvironment.updateMessage (message);

            } catch (Exception e) {

                Environment.logError ("Unable to update project to new versions of chapters: " +
                                      _proj,
                                      e);

                ComponentUtils.showErrorMessage (parentViewer,
                                                 getUILanguageStringProperty (editors,messages,projectupdated,actionerror));
                                          //"Unable to update {project}, please contact Quoll Writer support for assistance.");

                return;

            }

            try
            {

                Environment.openProject (_proj);

                AbstractProjectViewer pv = null; // TODO Environment.getProjectViewer (_proj);

                if (!(pv instanceof EditorProjectViewer))
                {

                    Environment.logError ("Unable to open project at version: " +
                                          message.getProjectVersion () +
                                          ", project: " +
                                          _proj);

                    ComponentUtils.showErrorMessage (parentViewer,
                                                     getUILanguageStringProperty (editors,messages,projectupdated,actionerror));
                                              //"Unable to view updated {project}, please contact Quoll Writer support for assistance.");

                    return;

                }

                EditorProjectViewer epv = (EditorProjectViewer) pv;

                epv.switchToProjectVersion (message.getProjectVersion ());

                UIUtils.runLater (onShow);

            } catch (Exception e) {

                Environment.logError ("Unable to for project: " +
                                      _proj,
                                      e);

                ComponentUtils.showErrorMessage (parentViewer,
                                                 getUILanguageStringProperty (editors,messages,projectupdated,actionerror));
                                          //"Unable to show {comments}, please contact Quoll Writer support for assistance.");

                return;

            }

        };

        UIUtils.askForPasswordForProject (proj,
                                          null,
                                          open,
                                          null,
                                          parentViewer);

    }

    public static Node getProjectVersionPanel (final ProjectVersion        pv,
                                               final AbstractProjectViewer viewer)
    {

        if (pv == null)
        {

            return null;

        }

        String ver = pv.getName ();
        final String genComments = (pv.getDescription () != null ? pv.getDescription ().getText () : null);
        Date due = pv.getDueDate ();

        List<String> prefix = Arrays.asList (editors,project,sidebar,comments,labels);

        Form.Builder f = Form.builder ()
            .layoutType (Form.LayoutType.column)
            .styleClassName (StyleClassNames.PROJECTVERSIONINFO);

        if (ver != null)
        {

            StringProperty l = new SimpleStringProperty ();
            l.bind (UILanguageStringsManager.createStringBinding (() ->
            {

                String v = ver;

                if (pv.isLatest ())
                {

                    v += getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.latest)).getValue ();

                }

                return v;

            }));

            f.item (getUILanguageStringProperty (Utils.newList (prefix,version)),
                    QuollLabel.builder ()
                        .label (l)
                        .build ());

        }

        if (due != null)
        {

            f.item (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.due)),
                    QuollLabel.builder ()
                        .label (new SimpleStringProperty (Environment.formatDate (due)))
                        .build ());

        }

        if (genComments != null)
        {

            String commText = genComments;

            TextIterator ti = new TextIterator (commText);

            if (ti.getSentenceCount () > 1)
            {

                commText = ti.getFirstSentence ().getText ();

                commText += getUILanguageStringProperty (Utils.newList (prefix,more));
                //"<br /><a href='#'>More, click to view all.</a>";

            }

            QuollTextView v = QuollTextView.builder ()
                .text (commText)
                .inViewer (viewer)
                .build ();

            f.item (getUILanguageStringProperty (Utils.newList (prefix,notes)),
                    v);

            v.setOnMouseClicked (ev ->
            {

                QuollPopup.messageBuilder ()
                    .withViewer (viewer)
                    .title (editors,project,sidebar,comments,notes,popup,title)
                    .message (new SimpleStringProperty (genComments))
                    .closeButton ()
                    .build ();

            });

        }

        Form form = f.build ();
        VBox.setVgrow (form,
                       Priority.NEVER);

        return form;

    }

    public static void showMessagesInPopup (StringProperty     title,
                                            String             className,
                                            StringProperty     help,
                                            Set<EditorMessage> messages,
                                            boolean            showAttentionBorder,
                                            AbstractViewer     viewer,
                                            Node               showAt)
    {

        try
        {

            // Sort the messages in descending when order or newest first.
            Query q = new Query ();
            q.parse (String.format ("SELECT * FROM %s ORDER BY when DESC",
                                    EditorMessage.class.getName ()));

            QueryResults qr = q.execute (messages);

            messages = new LinkedHashSet (qr.getResults ());

        } catch (Exception e) {

            Environment.logError ("Unable to sort messages",
                                  e);

        }

        VBox content = new VBox ();
        content.getChildren ().add (QuollTextView.builder ()
            .text (UILanguageStringsManager.createStringPropertyWithBinding (() ->
            {

                return help.getValue () + getUILanguageStringProperty (editors,LanguageStrings.messages,show,suffix).getValue ();

            },
            help))
            .styleClassName (StyleClassNames.DESCRIPTION)
            .inViewer (viewer)
            .build ());

        VBox projBox = new VBox ();
        QScrollPane sp = new QScrollPane (projBox);
        VBox.setVgrow (sp,
                       Priority.ALWAYS);

         for (EditorMessage m : messages)
         {

             MessageBox mb = null;

             try
             {

                 mb = MessageBoxFactory.getMessageBoxInstance (m,
                                                               viewer);
                 mb.setShowAttentionBorder (true);//showAttentionBorder);

             } catch (Exception e) {

                 Environment.logError ("Unable to get message box for message: " +
                                       m,
                                       e);

             }

             projBox.getChildren ().add (mb);

       }

        QuollPopup qp = QuollPopup.builder ()
            .title (title)
            .styleClassName (className != null ? className : StyleClassNames.EDIT)
            .withClose (true)
            .styleSheet (StyleClassNames.EDITORMESSAGES)
            .withViewer (viewer)
            .hideOnEscape (true)
            .removeOnClose (true)
            .content (content)
            .show ()
            .build ();

        QuollButtonBar bb = QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.FINISH)
                        .label (buttons,close)
                        .onAction (ev ->
                        {

                            qp.close ();

                        })
                        .build ())
            .build ();

        content.getChildren ().addAll (sp, bb);

    }

    public static void showAllMessagesForEditor (EditorEditor   ed,
                                                 AbstractViewer viewer,
                                                 Node           showAt)
    {

        Project np = null;

        Set<EditorMessage> messages = ed.getMessages (new DefaultEditorMessageFilter (np,
                                                      EditorInfoMessage.MESSAGE_TYPE,
                                                      NewProjectMessage.MESSAGE_TYPE,
                                                      NewProjectResponseMessage.MESSAGE_TYPE,
                                                      ProjectCommentsMessage.MESSAGE_TYPE,
                                                      InviteResponseMessage.MESSAGE_TYPE,
                                                      ProjectEditStopMessage.MESSAGE_TYPE,
                                                      UpdateProjectMessage.MESSAGE_TYPE,
                                                      EditorRemovedMessage.MESSAGE_TYPE));

        java.util.List<String> prefix = Arrays.asList (editors,LanguageStrings.messages,show,all,popup);

        EditorsUIUtils.showMessagesInPopup (getUILanguageStringProperty (Utils.newList (prefix,title)),
                                            //"All messages",
                                            StyleClassNames.FIND,
                                            getUILanguageStringProperty (Utils.newList (prefix,text),
                                                           //"All messages you've sent to and/or received from <b>%s</b>.",
                                                                         ed.mainNameProperty ()),
                                            messages,
                                            false,
                                            viewer,
                                            showAt);

    }

    public static void showImportantMessagesForEditor (EditorEditor   ed,
                                                       AbstractViewer viewer,
                                                       Node           showAt)
    {

        // Get undealt with messages that are not chat.
        // If there is just one then show it, otherwise show a link that will display a popup of them.
        Set<EditorMessage> undealtWith = ed.getMessages (EditorsUIUtils.importantMessageFilter);

        java.util.List<String> prefix = Arrays.asList (editors,messages,show,important,popup);

        EditorsUIUtils.showMessagesInPopup (getUILanguageStringProperty (Utils.newList (prefix,title)),
                                            //"Important messages",
                                            StyleClassNames.IMPORTANT,
                                            getUILanguageStringProperty (Utils.newList (prefix,text),
                                                            //"New and important messages from <b>%s</b> that require your attention.",
                                                                         ed.mainNameProperty ()),
                                            undealtWith,
                                            false,
                                            viewer,
                                            showAt);

    }

    public static void showProjectMessagesForEditor (EditorEditor          ed,
                                                     AbstractProjectViewer viewer,
                                                     Node                  showAt)
    {

        Set<EditorMessage> messages = ed.getMessages (new DefaultEditorMessageFilter (viewer.getProject (),
                                                                                      NewProjectMessage.MESSAGE_TYPE,
                                                                                      UpdateProjectMessage.MESSAGE_TYPE,
                                                                                      NewProjectResponseMessage.MESSAGE_TYPE,
                                                                                      ProjectEditStopMessage.MESSAGE_TYPE));

        ProjectEditor pe = viewer.getProject ().getProjectEditor (ed);

        java.util.List<String> prefix = Arrays.asList (editors,LanguageStrings.messages,show,project,popup);

        EditorsUIUtils.showMessagesInPopup (getUILanguageStringProperty (Utils.newList (prefix,title)),
                                            //"{Project} updates sent/received",
                                            StyleClassNames.PROJECTMESSAGES,
                                            getUILanguageStringProperty (Utils.newList (prefix,text),
                                                          //"All {project} updates you have sent to or received from <b>%s</b> for {project} <b>%s</b>.  The latest update is shown first.",
                                                                         ed.mainNameProperty (),
                                                                         viewer.getProject ().nameProperty ()),
                                            messages,
                                            true,
                                            viewer,
                                            showAt);

    }

    public static void showAllCommentsForEditor (EditorEditor          ed,
                                                 AbstractProjectViewer viewer,
                                                 Node                  showAt)
    {

        Set<EditorMessage> comments = ed.getMessages (new DefaultEditorMessageFilter (viewer.getProject (),
                                                                                      ProjectCommentsMessage.MESSAGE_TYPE));
/*
        if (comments.size () == 0)
        {

            UIUtils.showMessage ((PopupsSupported) viewer,
                                 "No comments sent/received",
                                 "No comments have been sent or received yet.");

            return;

        }
*/
        boolean sentByMe = comments.iterator ().next ().isSentByMe ();

        java.util.List<String> prefix = Arrays.asList (editors,messages,show,(sentByMe ? commentssent : commentsreceived),popup);

        //String suffix = (sentByMe ? "sent" : "received");
        //String suffix2 = (sentByMe ? "sent to" : "received from");

        EditorsUIUtils.showMessagesInPopup (getUILanguageStringProperty (Utils.newList (prefix,title)),
                                            //String.format ("{Comments} %s",
                                            //    suffix),
                                            StyleClassNames.COMMENTS,
                                            getUILanguageStringProperty (Utils.newList (prefix,text),
                                            //"All {comments} you have %s <b>%s</b> for {project} <b>%s</b>.  The latest {comments} are shown first.",
                                            //    suffix2,
                                                                         ed.mainNameProperty (),
                                                                         viewer.getProject ().nameProperty ()),
                                            comments,
                                            true,
                                            viewer,
                                            showAt);

    }

    public static void showProjectsUserIsEditingForEditor (EditorEditor   editor,
                                                           AbstractViewer viewer)
    {

        Set<ProjectInfo> projs = new LinkedHashSet ();

        try
        {

            Set<ProjectInfo> allProjs = Environment.getAllProjectInfos ();

            for (ProjectInfo p : allProjs)
            {

                if (p.isEditorProject ())
                {

                    EditorEditor ed = EditorsEnvironment.getEditorByEmail (p.getForEditor ().getEmail ());

                    if (ed == editor)
                    {

                        projs.add (p);

                    }

                }

            }

        } catch (Exception e) {

            Environment.logError ("Unable to get all projects",
                                  e);

            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (editors,LanguageStrings.editor,showprojectseditingforcontact,actionerror));
                                      //String.format ("Unable to show {projects} you are editing for %s.",
                                      //             editor.getShortName ()));

            return;

        }

        List<String> prefix = Arrays.asList (editors,LanguageStrings.editor,showprojectseditingforcontact,popup);

        VBox content = new VBox ();
        content.getChildren ().add (QuollTextView.builder ()
            .text (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                editor.mainNameProperty ()))
            .styleClassName (StyleClassNames.DESCRIPTION)
            .inViewer (viewer)
            .build ());

        VBox projBox = new VBox ();
        QScrollPane sp = new QScrollPane (projBox);
        VBox.setVgrow (sp,
                       Priority.ALWAYS);

        for (ProjectInfo p : projs)
        {

            QuollLabel l = QuollLabel.builder ()
                .label (p.nameProperty ())
                .build ();
            projBox.getChildren ().add (l);

            // TODO Allow click to open...

        }

        QuollPopup qp = QuollPopup.builder ()
            .title (Utils.newList (prefix,title))
            .styleClassName (StyleClassNames.EDIT)
            .withClose (true)
            .withViewer (viewer)
            .hideOnEscape (true)
            .removeOnClose (true)
            .content (content)
            .show ()
            .build ();

        QuollButtonBar bb = QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.FINISH)
                        .label (buttons,close)
                        .onAction (ev ->
                        {

                            qp.close ();

                        })
                        .build ())
            .build ();

        content.getChildren ().addAll (sp, bb);

    }

    public static void showProjectsEditorIsEditingForUser (EditorEditor   editor,
                                                           AbstractViewer viewer)
    {

        final Set<ProjectInfo> projs = new LinkedHashSet<> ();

        Set<EditorMessage> messages = editor.getMessages (new EditorMessageFilter ()
        {

            public boolean accept (EditorMessage m)
            {

                if (m.isSentByMe ())
                {

                    return false;

                }

                if (!m.getMessageType ().equals (NewProjectResponseMessage.MESSAGE_TYPE))
                {

                    return false;

                }

                NewProjectResponseMessage nprm = (NewProjectResponseMessage) m;

                if (!nprm.isAccepted ())
                {

                    return false;

                }

                try
                {

                    projs.add (Environment.getProjectById (nprm.getForProjectId (),
                                                           Project.NORMAL_PROJECT_TYPE));

                } catch (Exception e) {

                    Environment.logError ("Unable to get normal project with id: " +
                                          nprm.getForProjectId (),
                                          e);

                }

                return true;

            }

        });

        List<String> prefix = Arrays.asList (editors,LanguageStrings.editor,showprojectscontactisediting,popup);

        VBox content = new VBox ();
        content.getChildren ().add (QuollTextView.builder ()
            .text (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                editor.mainNameProperty ()))
            .styleClassName (StyleClassNames.DESCRIPTION)
            .inViewer (viewer)
            .build ());

        VBox projBox = new VBox ();
        QScrollPane sp = new QScrollPane (projBox);
        VBox.setVgrow (sp,
                       Priority.ALWAYS);

        for (ProjectInfo p : projs)
        {

            // TODO

        }

        QuollPopup qp = QuollPopup.builder ()
            .title (Utils.newList (prefix,title))
            .styleClassName (StyleClassNames.EDIT)
            .withClose (true)
            .withViewer (viewer)
            .hideOnEscape (true)
            .removeOnClose (true)
            .content (content)
            .show ()
            .build ();

        QuollButtonBar bb = QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.FINISH)
                        .label (buttons,close)
                        .onAction (ev ->
                        {

                            qp.close ();

                        })
                        .build ())
            .build ();

        content.getChildren ().addAll (sp, bb);

    }


    public static Node getProjectMessageDetails (final AbstractProjectMessage message,
                                                 final AbstractViewer         viewer,
                                                 final MessageBox             parentMessageBox)
    {

        String plural = "";

        List<String> prefix = Arrays.asList (editors,messages,newupdateproject,labels);

        if (message.getChapters ().size () > 1)
        {

            plural = "s";

        }

        ProjectVersion projVer = message.getProjectVersion ();
        Date dueDate = projVer.getDueDate ();

        String notes = ((projVer.getDescription () != null) ? projVer.getDescription ().getText () : null);
        String verName = projVer.getName ();

        ProjectInfo proj = null;

        try
        {

            proj = Environment.getProjectById (message.getForProjectId (),
                                               (message.isSentByMe () ? Project.NORMAL_PROJECT_TYPE : Project.EDITOR_PROJECT_TYPE));

        } catch (Exception e) {

            Environment.logError ("Unable to get project for id: " +
                                  message.getForProjectId (),
                                  e);

        }

        final ProjectInfo fproj = proj;

        // Show:
        //   * Project
        //   * Sent
        //   * Version (optional)
        //   * Word/chapter count
        //   * Due by
        //   * Notes (optional)
        //   * View link

        Form.Builder f = Form.builder ()
            .layoutType (Form.LayoutType.column);

        Node projLabel = null;

        if (proj != null)
        {

            projLabel = QuollHyperlink.builder ()
                .label (new SimpleStringProperty (message.getForProjectName ()))
                .tooltip (Utils.newList (prefix,open))
                .styleClassName (StyleClassNames.VIEWPROJECT)
                .onAction (ev ->
                {

                    if (fproj != null)
                    {

                        try
                        {

                            Environment.openProject (fproj);

                        } catch (Exception e) {

                            Environment.logError ("Unable to open project: " +
                                                  fproj,
                                                  e);

                        }

                    }

                })
                .build ();

        } else {

            projLabel = QuollLabel.builder ()
                .label (new SimpleStringProperty (message.getForProjectName ()))
                .build ();

        }

        f.item (getUILanguageStringProperty (Utils.newList (prefix,Project.OBJECT_TYPE)),
                projLabel);

        f.item (getUILanguageStringProperty (Utils.newList (prefix, (message.isSentByMe () ? sent : received))),
                QuollLabel.builder ()
                    .label (new SimpleStringProperty (Environment.formatDateTime (message.getWhen ())))
                    .build ());

        if (verName != null)
        {

            f.item (getUILanguageStringProperty (Utils.newList (prefix,version)),
                    QuollLabel.builder ()
                        .label (new SimpleStringProperty (verName))
                        .build ());

        }

        f.item (QuollLabel.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,detail),
                                             Environment.formatNumber (message.getWordCount ()),
                                             Environment.formatNumber (message.getChapters ().size ())))
            .build ());
                        //Environment.replaceObjectNames (String.format ("%s words, %s {chapter%s}",
                        //                                                 Environment.formatNumber (message.getWordCount ()),
                        //                                                 message.getChapters ().size (),
                        //                                                 plural)),

        f.item (getUILanguageStringProperty (Utils.newList (prefix,dueby)),
                QuollLabel.builder ()
                    .label ((dueDate != null ? new SimpleStringProperty (Environment.formatDate (dueDate)) : getUILanguageStringProperty (Utils.newList (prefix,notspecified))))
                    .build ());

        if (notes != null)
        {

            f.item (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.notes)),
                    QuollTextView.builder ()
                        .text (notes)
                        .build ());

        }

        if (message.isSentByMe ())
        {

            Node viewProj = null;

            if (proj == null)
            {

                viewProj = QuollLabel.builder ()
                    .styleClassName (StyleClassNames.ERROR)
                    .label (editors,messages,newupdateproject,sent,labels,projectdeleted)
                    .build ();
                //"{Project} has been deleted");

            } else {

                viewProj = QuollHyperlink.builder ()
                    .label (editors,messages,newupdateproject,sent,labels,clicktoview)
                    .styleClassName (StyleClassNames.VIEW)
                    .onAction (ev ->
                    {

                        AbstractViewer childViewer = parentMessageBox.getChildViewer ();

                        if (childViewer != null)
                        {

                            childViewer.getViewer ().setIconified (false);
                            childViewer.getViewer ().toFront ();

                            return;

                        }

                        // Load up the project with the specific text.
                        // See if we have a project viewer for the project.
                        ProjectInfo nproj = null;

                        try
                        {

                            nproj = Environment.getProjectById (message.getForProjectId (),
                                                                Project.NORMAL_PROJECT_TYPE);

                        } catch (Exception e) {

                            Environment.logError ("Unable to get project for: " +
                                                  message.getForProjectId (),
                                                  e);

                            ComponentUtils.showErrorMessage (viewer,
                                                             getUILanguageStringProperty (editors,projectsent,actions,openproject,actionerror));
                                                      //"Unable to show the {project}, please contact Quoll Writer support for assistance.");

                            return;

                        }

                        final ProjectInfo _proj = nproj;

                        Consumer<String> open = (_pwd) ->
                        {

                            String pwd = _proj.getFilePassword ();

/*
                            ev.getActionCommand ();

                            if (pwd.equals (""))
                            {

                                pwd = null;

                            }
*/
                            Set<Chapter> chaps = null;

                            try
                            {

                                chaps = Environment.getVersionedChapters (_proj,
                                                                          message.getChapters (),
                                                                          pwd);

                            } catch (Exception e) {

                                Environment.logError ("Unable to get versioned chapters for project: " +
                                                      _proj,
                                                      e);

                                ComponentUtils.showErrorMessage (viewer,
                                                                 getUILanguageStringProperty (editors,projectsent,actions,openproject,actionerror));
                                                          //"Unable to show {comments}, please contact Quoll Writer support for assistance.");

                                return;

                            }

                            try
                            {

                                // Need to "fill up" the project with the chapters and comments.
                                // Create a new project object just to be safe (in case the getProjectById call changes in the future).
                                Project np = new Project ();
                                np.setName (_proj.getName ());

                                np.setType (Project.EDITOR_PROJECT_TYPE);
                                np.setId (message.getForProjectId ());
                                np.setName (message.getForProjectName ());

                                Book b = new Book (np,
                                                   np.getName ());

                                np.addBook (b);

                                for (Chapter c : chaps)
                                {

                                    b.addChapter (c);

                                }

                                final int chapterCount = chaps.size ();

                                ProjectSentReceivedViewer pcv = new ProjectSentReceivedViewer<AbstractProjectMessage> (np,
                                                                                                                       message)
                                {

                                    @Override
                                    public void init (State s)
                                               throws GeneralException
                                    {

                                        super.init (s);

                                        this.setMainSideBar (new ProjectSentReceivedSideBar<> (this,
                                                                                               this.message)
                                        {

                                            @Override
                                            public String getStyleSheet ()
                                            {

                                                return "projectsentreceived";

                                            }

                                            @Override
                                            public String getStyleClassName ()
                                            {

                                                return StyleClassNames.COMMENTS;

                                            }

                                            @Override
                                            public StringProperty getTitle ()
                                            {

                                                return getUILanguageStringProperty (editors,projectsent,LanguageStrings.sidebar,title);

                                            }

                                            @Override
                                            public StringProperty getItemsTitle ()
                                            {

                                                return getUILanguageStringProperty (editors,projectsent,LanguageStrings.sidebar,chapters,title);

                                            }

                                            @Override
                                            public Node getMessageDetails (AbstractProjectMessage message)
                                            {

                                                List<String> prefix = Arrays.asList (editors,projectsent,LanguageStrings.sidebar,labels);

                                                ProjectVersion projVer = message.getProjectVersion ();

                                                String verName = projVer.getName ();

                                                final String notes = (projVer.getDescription () != null ? projVer.getDescription ().getText () : null);

                                                Form.Builder fb = Form.builder ()
                                                    .item (getUILanguageStringProperty (Utils.newList (prefix,sent)),
                                                           UILanguageStringsManager.createStringPropertyWithBinding (() ->
                                                           {

                                                               return Environment.formatDateTime (message.getWhen ());

                                                           }));

                                                if (verName != null)
                                                {

                                                    fb.item (getUILanguageStringProperty (Utils.newList (prefix,version)),
                                                             new SimpleStringProperty (verName));

                                                }

                                                if (notes != null)
                                                {

                                                    QuollTextView notesT = QuollTextView.builder ()
                                                        .inViewer (this.viewer)
                                                        .text (UILanguageStringsManager.createStringPropertyWithBinding (() ->
                                                        {

                                                            String commText = notes;

                                                            TextIterator ti = new TextIterator (commText);

                                                            if (ti.getSentenceCount () > 1)
                                                            {

                                                                commText = ti.getFirstSentence ().getText ();

                                                                commText += getUILanguageStringProperty (prefix,more).getValue ();
                                                                //"<br /><a href='#'>More, click to view all.</a>";

                                                            }

                                                            return commText;

                                                        }))
                                                        .build ();

                                                    notesT.setOnMouseClicked (ev ->
                                                    {

                                                        QuollPopup.messageBuilder ()
                                                            .withViewer (this.viewer)
                                                            .title (getUILanguageStringProperty (editors,projectsent,LanguageStrings.sidebar,notes,popup,title))
                                                            .message (new SimpleStringProperty (notes))
                                                            .build ();

                                                    });

                                                    fb.item (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.notes)),
                                                             notesT);

                                                }

                                                return fb.build ();

                                            }

                                        });

                                    }

                                    @Override
                                    public String getStyleClassName ()
                                    {

                                        return StyleClassNames.COMMENTS;

                                    }

                                };

                                pcv.createViewer ();
                                pcv.init (null);

                                parentMessageBox.setChildViewer (pcv);

                                pcv.getViewer ().addEventHandler (Viewer.ViewerEvent.CLOSE_EVENT,
                                (eev ->
                                {

                                    parentMessageBox.setChildViewer (null);

                                }));

                            } catch (Exception e) {

                                Environment.logError ("Unable to view project: " +
                                                      _proj,
                                                      e);

                                ComponentUtils.showErrorMessage (viewer,
                                                                 getUILanguageStringProperty (editors,projectsent,actions,openproject,actionerror));
                                                          //"Unable to show {project}, please contact Quoll Writer support for assistance.");

                                return;

                            }

                        };

                        UIUtils.askForPasswordForProject (_proj,
                                                          null,
                                                          open,
                                                          null,
                                                          viewer);

                    })
                    .build ();

            }

            f.item (viewProj);

        }

        return f.build ();

    }

    public static Node getNewProjectMessageDetails (final NewProjectMessage     mess,
                                                    final AbstractProjectViewer viewer,
                                                    final MessageBox            parentMessageBox)
    {

        return EditorsUIUtils.getProjectMessageDetails (mess,
                                                        viewer,
                                                        parentMessageBox);

    }

}
