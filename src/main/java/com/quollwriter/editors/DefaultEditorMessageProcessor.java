package com.quollwriter.editors;

import java.awt.event.*;
import java.awt.image.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Date;

import javafx.embed.swing.*;

import org.bouncycastle.openpgp.*;

import com.quollwriter.data.editors.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class DefaultEditorMessageProcessor implements EditorMessageProcessor
{

    public boolean processMessage (final EditorMessage mess)
                            throws Exception
    {

        boolean showPopup = EditorsEnvironment.isShowPopupWhenNewMessageReceived ();

        EditorEditor ed = mess.getEditor ();

        if (mess instanceof EditorInfoMessage)
        {

            return this.handleEditorInfoMessage ((EditorInfoMessage) mess,
                                                 ed,
                                                 showPopup);

        }

        if (mess instanceof EditorChatMessage)
        {

            return this.handleChatMessage ((EditorChatMessage) mess,
                                           ed,
                                           showPopup);

        }

        if (mess instanceof InviteMessage)
        {

            return this.handleInviteMessage ((InviteMessage) mess,
                                             ed,
                                             showPopup);

        }

        if (mess instanceof InviteResponseMessage)
        {

            return this.handleInviteResponseMessage ((InviteResponseMessage) mess,
                                                     ed,
                                                     showPopup);

        }

        if (mess instanceof NewProjectMessage)
        {

            return this.handleNewProjectMessage ((NewProjectMessage) mess,
                                                 ed,
                                                 showPopup);

        }

        if (mess instanceof NewProjectResponseMessage)
        {

            return this.handleNewProjectResponseMessage ((NewProjectResponseMessage) mess,
                                                         ed,
                                                         showPopup);

        }

        if (mess instanceof UpdateProjectMessage)
        {

            return this.handleUpdateProjectMessage ((UpdateProjectMessage) mess,
                                                    ed,
                                                    showPopup);

        }

        if (mess instanceof ProjectCommentsMessage)
        {

            return this.handleProjectCommentsMessage ((ProjectCommentsMessage) mess,
                                                      ed,
                                                      showPopup);

        }

        if (mess instanceof EditorRemovedMessage)
        {

            return this.handleEditorRemovedMessage ((EditorRemovedMessage) mess,
                                                    ed,
                                                    showPopup);

        }

        if (mess instanceof ProjectEditStopMessage)
        {

            return this.handleProjectEditStopMessage ((ProjectEditStopMessage) mess,
                                                      ed,
                                                      showPopup);

        }

        if (mess instanceof InteractionMessage)
        {

            return this.handleInteractionMessage ((InteractionMessage) mess,
                                                  ed,
                                                  showPopup);

        }

        if (mess instanceof ErrorMessage)
        {

            return this.handleErrorMessage ((ErrorMessage) mess,
                                            ed,
                                            showPopup);

        }

        return false;

    }

    private boolean handleInteractionMessage (InteractionMessage im,
                                              EditorEditor       ed,
                                              boolean            showPopup)
                                       throws Exception
    {

        EditorsEnvironment.fireEditorInteractionEvent (im.getEditor (),
                                                       im.getAction ());

        // Fire a status update.
        return false;

    }

    private boolean handleEditorRemovedMessage (EditorRemovedMessage mess,
                                                EditorEditor         ed,
                                                boolean              showPopup)
                                         throws Exception
    {

        // Unsubscribe.
        EditorsEnvironment.getMessageHandler ().unsubscribeFromEditor (ed);
/*
        if (showPopup)
        {

            AbstractViewer viewer = Environment.getFocusedViewer ();

            mess.setDealtWith (true);

            UIUtils.showMessage ((PopupsSupported) viewer,
                                 "You have been removed as {an editor}",
                                 String.format ("<b>%s</b> has removed you as {an editor}.<br /><br />Note you will no longer be able to send them messages and/or {projects}.",
                                                mess.getEditor ().getShortName ()));

        }
*/
        return true;

    }

    private boolean handleProjectEditStopMessage (ProjectEditStopMessage mess,
                                                  EditorEditor           ed,
                                                  boolean                showPopup)
                                           throws Exception
    {

        if (ed.isPending ())
        {

            // TODO: Make this nicer, maybe set new project response message to dealt with?

            // This is the rare case where someone has been invited, they accepted the project then deleted it
            // before the user could find out.

            // Get the NewProjectResponseMessage and set it as dealt with so the user doesn't have to bother with it.

        }

/*
        Project p = Environment.getProjectById (mess.getForProjectId (),
                                                Project.NORMAL_PROJECT_TYPE);

        ProjectEditor pe = EditorsEnvironment.getProjectEditor (p,
                                                                mess.getEditor ());

        pe.setCurrent (false);
        pe.setEditorTo (new Date ());
        pe.setStatusMessage (String.format ("Stopped editing: %s",
                                            Environment.formatDate (pe.getEditorTo ())));

        EditorsEnvironment.updateProjectEditor (pe);
*/
/*
        if (showPopup)
        {

            AbstractViewer viewer = Environment.getFocusedViewer ();

            UIUtils.showMessage ((PopupsSupported) viewer,
                                 "An {editor} has stopped editing your {project}",
                                 String.format ("<b>%s</b> has decided to stop editing <b>%s</b>.",
                                                mess.getEditor ().getShortName (),
                                                mess.getForProjectName ()));

            mess.setDealtWith (true);

        }
*/
        return true;

    }

    private boolean handleProjectCommentsMessage (final ProjectCommentsMessage mess,
                                                  final EditorEditor           ed,
                                                  final boolean                showPopup)
                                           throws Exception
    {

        String projId = mess.getForProjectId ();

        ProjectInfo proj = null;

        try
        {

            proj = Environment.getProjectById (projId,
                                               Project.NORMAL_PROJECT_TYPE);

        } catch (Exception e) {

            throw new GeneralException ("Unable to get project for id: " +
                                        projId,
                                        e);

        }

        ProjectEditor pe = EditorsEnvironment.getProjectEditor (proj,
                                                                ed);

        if (pe == null)
        {

            throw new IllegalArgumentException ("Editor is not a project editor for project: " + projId + ", editor: " + ed);

        }

        // It may be that we get some comments before the user acknowledged the editor's acceptance of the
        // project.  In which case we need to update the project editor to current.
        if (pe.isInvited ())
        {

            pe.setEditorFrom (mess.getWhen ());
            pe.setCurrent (true);

            pe.setStatus (ProjectEditor.Status.accepted);

        }

        int c = mess.getComments ().size ();

        pe.setStatusMessage (String.format (getUIString (editors,user,commentsreceived,editorstatus),
                                            //"Received %s {comment%s}: %s",
                                            Environment.formatNumber (c),
                                            Environment.formatDate (mess.getWhen ())));

        EditorsEnvironment.updateProjectEditor (pe);
/*
        if (showPopup)
        {

            AbstractViewer viewer = Environment.getFocusedViewer ();

            UIUtils.createQuestionPopup (viewer,
                                         "{Comments} received about a {project}",
                                         Constants.COMMENT_ICON_NAME,
                                         String.format ("<b>%s</b> has sent <b>%s</b> {comments} about version <b>%s</b> of your {project} <b>%s</b>.<br /><br />Would you like to view the {comments} now?",
                                                        mess.getEditor ().getShortName (),
                                                        mess.getForProjectName ()),
                                         "Yes, show me them",
                                         "No, I'll do it later",
                                         new ActionListener ()
                                         {

                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                AbstractViewer viewer = Environment.getFocusedViewer ();

                                                EditorsUIUtils.showProjectComments (mess,
                                                                                    viewer,
                                                                                    null);

                                            }

                                         },
                                         null,
                                         null,
                                         null);

            mess.setDealtWith (true);

        }
*/
        return true;

    }

    private boolean handleInviteMessage (final InviteMessage inv,
                                         final EditorEditor  ed,
                                         final boolean       showPopup)
                                  throws Exception
    {

        ed.setName (inv.getEditorName ());

        if (inv.getEditorAvatar () != null)
        {

            ed.setAvatar (inv.getEditorAvatar ());

        }

        // TODO: Need a nicer way of doing this.
        if (ed.getKey () != null)
        {

            EditorsEnvironment.updateEditor (ed);

        } else {

            EditorsEnvironment.addNewEditor (ed);

        }

        return true;

    }

    private boolean handleInviteResponseMessage (InviteResponseMessage rm,
                                                 EditorEditor          ed,
                                                 boolean               showPopup)
                                          throws Exception
    {

        if (ed.getEditorStatus () != EditorEditor.EditorStatus.pending)
        {

            // Actually an error, but probably ok to just ignore?
            Environment.logError ("Invalid statue, received invite response message from editor: " +
                                  ed);

            return false;

        }

        if (rm.isAccepted ())
        {

            EditorsEnvironment.getMessageHandler ().subscribeToEditor (ed);

        }

        return true;

    }

    private boolean handleErrorMessage (ErrorMessage error,
                                        EditorEditor ed,
                                        boolean      showPopup)
                                 throws Exception
    {

        // Check the type and decide what to do...
        if (error.getErrorType () == ErrorMessage.ErrorType.projectnotexists)
        {

            // Eerk!

        }

        return false;

    }

    private boolean handleEditorInfoMessage (EditorInfoMessage info,
                                             EditorEditor      ed,
                                             boolean           showPopup)
                                      throws Exception
    {

        return true;

    }

    private boolean handleChatMessage (EditorChatMessage mess,
                                       EditorEditor      ed,
                                       boolean           showPopup)
                                throws Exception
    {

        return true;

    }

    private boolean handleNewProjectResponseMessage (NewProjectResponseMessage res,
                                                     EditorEditor              ed,
                                                     boolean                   showPopup)
                                              throws Exception
    {

        if (ed.isPending ())
        {

            if (res.isAccepted ())
            {

                EditorsEnvironment.getMessageHandler ().subscribeToEditor (ed);

            }

        }

        // Set the response in the new project message we have stored.
        // Get the message.
        NewProjectMessage npmess = EditorsEnvironment.getNewProjectMessage (ed,
                                                                            res.getForProjectId (),
                                                                            true);

        if (npmess == null)
        {

            // Return an error saying that the project no longer exists.
            EditorsEnvironment.sendError (res,
                                          ErrorMessage.ErrorType.invalidstate,
                                          "No new project message found for project: " + res.getForProjectId ());

            return false;

        }

        boolean accepted = res.isAccepted ();

        npmess.setAccepted (accepted);
        npmess.setResponseMessage (res.getResponseMessage ());

        EditorsEnvironment.updateMessage (npmess);
        return true;


    }

    private boolean handleNewProjectMessage (NewProjectMessage proj,
                                             EditorEditor      ed,
                                             boolean           showPopup)
                                      throws Exception
    {

        // TODO: Need a nicer way of doing this.
        if (ed.getKey () != null)
        {

            EditorsEnvironment.updateEditor (ed);

        } else {

            // Only set the name/avatar for new editors.
            ed.setName (proj.getEditorName ());
            ed.setAvatar (proj.getEditorAvatar ());

            EditorsEnvironment.addNewEditor (ed);

        }

        NewProjectMessage npmess = EditorsEnvironment.getNewProjectMessage (ed,
                                                                            proj.getForProjectId (),
                                                                            false);

        ProjectInfo pr = Environment.getProjectById (proj.getForProjectId (),
                                                     Project.EDITOR_PROJECT_TYPE);

        ProjectEditor pe = EditorsEnvironment.getProjectEditor (pr,
                                                                ed);

        if ((npmess != null)
            &&
            (pe != null)
            &&
            (pe.isCurrent ())
           )
        {

            // Return an error saying that it can only be sent once.
            EditorsEnvironment.sendError (proj,
                                          ErrorMessage.ErrorType.invalidstate,
                                          "Already received a new project message for: " + proj.getForProjectId ());

            Environment.logError ("Received duplicate new project message from: " +
                                  ed +
                                  " for project: " +
                                  proj.getForProjectId ());

            return false;

        }

        return true;


    }

    private boolean handleUpdateProjectMessage (UpdateProjectMessage mess,
                                                EditorEditor         ed,
                                                boolean              showPopup)
                                         throws Exception
    {

        // Save the message.
        return true;

    }

}
