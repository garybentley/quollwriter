package com.quollwriter.editors;

import java.awt.event.*;
import java.awt.image.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Date;

import org.bouncycastle.openpgp.*;

import com.quollwriter.data.editors.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.ui.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.*;

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
        
        try
        {
            
            // Uupdate the editor to be previous.
            ed.setEditorStatus (EditorEditor.EditorStatus.previous);
        
            EditorsEnvironment.updateEditor (ed);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to update editor: " +
                                  ed,
                                  e);
                                            
            return true;
            
        }
        
        if (showPopup)
        {                
                        
            AbstractProjectViewer viewer = Environment.getFocusedProjectViewer ();
                                    
            mess.setDealtWith (true);
            
            UIUtils.showMessage ((PopupsSupported) viewer,
                                 "You have been removed as {an editor}",
                                 String.format ("<b>%s</b> has removed you as {an editor}.<br /><br />Note you will no longer be able to send them messages and/or {projects}.",
                                                mess.getEditor ().getShortName ()));
            
        }
        
        return true;                    
        
    }
    
    private boolean handleProjectEditStopMessage (ProjectEditStopMessage mess,
                                                  EditorEditor           ed,
                                                  boolean                showPopup)
                                           throws Exception    
    {
        
        Project p = Environment.getProjectById (mess.getForProjectId (),
                                                Project.NORMAL_PROJECT_TYPE);
                    
        ProjectEditor pe = EditorsEnvironment.getProjectEditor (p,
                                                                mess.getEditor ());
        
        pe.setCurrent (false);
        pe.setEditorTo (new Date ());
        pe.setStatusMessage (String.format ("Stopped editing: %s",
                                            Environment.formatDate (pe.getEditorTo ())));
        
        EditorsEnvironment.updateProjectEditor (pe);

        if (showPopup)
        {
                        
            AbstractProjectViewer viewer = Environment.getFocusedProjectViewer ();
                
            UIUtils.showMessage ((PopupsSupported) viewer,
                                 "An {editor} has stopped editing your {project}",
                                 String.format ("<b>%s</b> has decided to stop editing <b>%s</b>.",
                                                mess.getEditor ().getShortName (),
                                                mess.getForProjectName ()));
            
            mess.setDealtWith (true);
                            
        }
        
        return true;        
        
    }
    
    private boolean handleProjectCommentsMessage (final ProjectCommentsMessage mess,
                                                  final EditorEditor           ed,
                                                  final boolean                showPopup)
                                           throws Exception    
    {
        
        int c = mess.getComments ().size ();
        
        // Get the project, get the project editor, set the status.
        EditorsEnvironment.setProjectEditorStatus (mess.getForProjectId (),
                                                   ed,
                                                   Environment.replaceObjectNames (String.format ("Received %s {comment%s}: %s",
                                                                                                  Environment.formatNumber (c),
                                                                                                  (c > 1 ? "s" : ""),
                                                                                                  Environment.formatDate (mess.getWhen ()))));
    
        if (showPopup)
        {
                            
            AbstractProjectViewer viewer = Environment.getFocusedProjectViewer ();
                        
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
                                                
                                                AbstractProjectViewer viewer = Environment.getFocusedProjectViewer ();
                                                
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
        
        return true;
                
    }
    
    private boolean handleInviteMessage (final InviteMessage inv,
                                         final EditorEditor  ed,
                                         final boolean       showPopup)
                                  throws Exception    
    {
        
        ed.setName (inv.getEditorName ());
        ed.setAvatar (inv.getEditorAvatar ());

        // TODO: Need a nicer way of doing this.
        if (ed.getKey () != null)
        {
                            
            EditorsEnvironment.updateEditor (ed);
            
        } else {
        
            EditorsEnvironment.addNewEditor (ed);
            
        }

        if (!showPopup)
        {
                            
            return true;
        
        }
                                                                
        ActionListener onAccept = new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                try
                {
                
                    EditorsEnvironment.addMessage (inv);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to save message for editor: " +
                                          ed,
                                          e);
                    
                    return;
                            
                }                                              

                InviteResponseMessage rm = new InviteResponseMessage (true,
                                                                      EditorsEnvironment.getUserAccount ());
                rm.setEditor (ed);
                
                EditorsEnvironment.acceptInvite (ed,
                                                 rm,
                                                 null);

            }
            
        };
    
        ActionListener onReject = new ActionListener ()
        {

            // Rejects, so update and ignore the message.
            public void actionPerformed (ActionEvent ev)
            {
                
                InviteResponseMessage rm = new InviteResponseMessage (false,
                                                                      EditorsEnvironment.getUserAccount ());
                rm.setEditor (ed);
                
                EditorsEnvironment.rejectInvite (ed,
                                                 rm,
                                                 null);

            }

        };
    
        // Pending, user needs to respond.
        EditorsUIUtils.showInviteFromEditor ((ed.getName () != null ? String.format ("%s (%s)",
                                                                                     ed.getName (),
                                                                                     ed.getEmail ()) : ed.getMainName ()),
                                             onAccept,
                                             onReject);

        return false;
                
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
        
        // Show the response.
        rm.setDealtWith (true);
        
        if (rm.isAccepted ())
        {
                            
            // Show the acceptance.
            // Set the editor as current.
            ed.setEditorStatus (EditorEditor.EditorStatus.current);
            
            EditorsEnvironment.getMessageHandler ().subscribeToEditor (ed); 
            
            if (rm.getEditorName () != null)
            {
            
                ed.setName (rm.getEditorName ());
                
            }
            
            if (rm.getEditorAvatar () != null)
            {
                
                ed.setAvatar (rm.getEditorAvatar ());
                
            }
            
            EditorsEnvironment.updateEditor (ed);
            
            // Is this response for an invite message or just out of the blue from a web service invite?
            if (!EditorsEnvironment.hasSentMessageOfTypeToEditor (ed,
                                                                  InviteMessage.MESSAGE_TYPE))
            {
                
                EditorsEnvironment.sendUserInformationToEditor (ed,
                                                                null,
                                                                null,
                                                                null);
                
            }
            
            if (showPopup)
            {
            
                EditorsUIUtils.showInviteAcceptance (ed);
                
            }
                            
        } else {
            
            ed.setEditorStatus (EditorEditor.EditorStatus.rejected);
            
            EditorsEnvironment.updateEditor (ed);
                 
            if (showPopup)
            {                                
            
                EditorsUIUtils.showInviteRejection (ed);
                
            }
            
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
        
        // Check to see if this is the first time that the editor has sent this info, if so
        // then just update don't prompt.
        // Need a better way of doing this, save me Hibernate!
        boolean hasSentInfo = false;
        
        try
        {
            
            hasSentInfo = EditorsEnvironment.hasEditorSentInfo (ed);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to check if editor has sent info: " + ed,
                                  e);                        
            
            return true;
            
        }
        
        if (hasSentInfo)
        {
        
            if (showPopup)
            {
        
                EditorsUIUtils.showEditorInfoReceived (info);
                
            }
            
        } else {
            
            // Just update the info.
            String newName = info.getName ();
            
            if (newName != null)
            {
                
                ed.setName (newName.trim ());
                
            }
            
            java.awt.image.BufferedImage newImage = info.getAvatar ();
            
            if (newImage != null)
            {
                
                ed.setAvatar (newImage);
                
            }
            
            info.setDealtWith (true);
                                    
            try
            {
            
                EditorsEnvironment.updateEditor (ed);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to update editor: " + ed,
                                      e);
                                            
            }
                                                            
        }
        
        return true;        
        
    }
    
    private boolean handleChatMessage (EditorChatMessage mess,
                                       EditorEditor      ed,
                                       boolean           showPopup)
                                throws Exception    
    {
        
        // Show a notification then inform our listeners.
        if (showPopup)
        {
            
            mess.setDealtWith (true);
                            
            EditorsUIUtils.showNewEditorChatMessageNotification ((EditorChatMessage) mess);
            
        }
                    
        return true;        
        
    }
    
    private boolean handleNewProjectResponseMessage (NewProjectResponseMessage res,
                                                     EditorEditor              ed,
                                                     boolean                   showPopup)
                                              throws Exception    
    {
        
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
                    
        if (showPopup)
        {
        
            EditorsUIUtils.showNewProjectResponseNotification (res);
            
            return true;
            
        }
        
        Project proj = null;
        
        try
        {
            
            proj = Environment.getProjectById (res.getForProjectId (),
                                               Project.NORMAL_PROJECT_TYPE);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to get project by id: " +
                                  res.getForProjectId (),
                                  e);
            
            return false;
            
        }                        
                    
        ProjectEditor pe = null;
        
        try
        {
            
            pe = EditorsEnvironment.getProjectEditor (proj,
                                                      ed);

        } catch (Exception e) {
            
            Environment.logError ("Unable to get project editor: " +
                                  proj +
                                  ", editor: " +
                                  ed,
                                  e);
            
            return false;
            
        }
                   
        if (ed.isPending ())
        {
            
            if (accepted)
            {
                
                EditorsEnvironment.getMessageHandler ().subscribeToEditor (ed);
                
            }            
            
            ed.setEditorStatus ((accepted ? EditorEditor.EditorStatus.current : EditorEditor.EditorStatus.rejected));
            
            try
            {
            
                EditorsEnvironment.updateEditor (ed);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to update editor: " +
                                      ed,
                                      e);
                
            }
            
        }
                    
        if (pe != null)
        {
        
            if (!accepted)
            {
        
                try
                {
            
                    EditorsEnvironment.removeProjectEditor (pe);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to remove project editor: " +
                                          pe,
                                          e);
                    
                }
                
            } else {
                
                try
                {
                    
                    pe.setStatus (ProjectEditor.Status.accepted);
                    
                    EditorsEnvironment.updateProjectEditor (pe);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to accept project editor: " +
                                          pe,
                                          e);
                    
                }
                
            }
            
        }
                        
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
        
        Project pr = Environment.getProjectById (proj.getForProjectId (),
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
        
        if (!showPopup)
        {
                            
            // Save the message.
            return true;
        
        }        
        
        // Check to see if the editor is already editing the project.
        EditorsUIUtils.showNewProjectReceived (proj);
        
        return true;
        
        
    }
    
    private boolean handleUpdateProjectMessage (UpdateProjectMessage mess,
                                                EditorEditor         ed,
                                                boolean              showPopup)
                                         throws Exception
    {
        
        /*
        Project pr = Environment.getProjectById (mess.getForProjectId (),
                                                 Project.EDITOR_PROJECT_TYPE);
                                                                                    
        // If we don't have the project then send an error.
        if (pr == null)
        {
            
            EditorsEnvironment.sendError (mess,
                                          ErrorMessage.ErrorType.projectnotexists,
                                          "No project with id: " + mess.getForProjectId ());
            
            Environment.logError ("Received an update project message from editor: " +
                                  ed +
                                  ", but project is unknown to user.");

            return false;

        }        
                    
        // Auto update.    
        if (!pr.isEncrypted ())
        {
            
            try
            {
                
                Environment.updateToNewVersions (pr,
                                                 mess.getProjectVersion (),
                                                 mess.getChapters (),
                                                 null);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to update project to new versions of chapters: " +
                                      pr,
                                      e);
                
                return false;
                
            }
            
        }                
        */
                
        if (showPopup)
        {
                            
            EditorsUIUtils.showProjectUpdateReceived (mess);

        }
        
        // Save the message.
        return true;
                
    }    
    
}