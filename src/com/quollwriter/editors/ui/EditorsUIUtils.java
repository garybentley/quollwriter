package com.quollwriter.editors.ui;

import java.util.*;
import java.net.*;
import java.io.*;

import java.awt.Color;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import org.josql.*;

import com.toedter.calendar.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.ScrollableBox;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.text.*;

public class EditorsUIUtils
{
    
    private static EditorLogin editorLogin = null;
    private static EditorMessageFilter defaultViewableMessageFilter = null;
    private static EditorMessageFilter importantMessageFilter = null;
    
    static
    {
        
        EditorsUIUtils.editorLogin = new EditorLogin ();

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

        String s = "To delete your account please enter the word <b>Yes</b> in the box below.<br /><br />Note: Your {contacts} database will not be deleted allowing you to keep a record of what you have sent and received.  If you create another account you will not be able to use your current database.<br /><br /><p class='error'>Warning: deleting your account means you will no longer be able to send messages to any of your {contacts} or receive messages from them.  A message will be sent to each of them telling them you have removed them.</p>";
        
        UIUtils.createTextInputPopup (viewer,
                                     "Delete your account",
                                     Constants.DELETE_ICON_NAME,
                                     s,
                                     "Yes, delete it",
                                     "No, keep it",
                                     null,
                                     UIUtils.getYesValueValidator (),
                                     new ActionListener ()
                                     {
                                        
                                        public void actionPerformed (ActionEvent ev)
                                        {
                                            
                                            final String dbDir = EditorsEnvironment.getEditorsProperty (Constants.QW_EDITORS_DB_DIR_PROPERTY_NAME);
                                            
                                            final Notification notify = viewer.addNotification ("Deleting your Editors Service account, please wait.  This sometimes takes a little while...",
                                                                                                Constants.LOADING_GIF_NAME,
                                                                                                -1);

                                            EditorsEnvironment.deleteUserAccount (new ActionListener ()
                                                                                  {
                                                                                    
                                                                                    public void actionPerformed (ActionEvent ev)
                                                                                    {

                                                                                        // Remove the editors sidebar.
                                                                                        Environment.removeSideBarFromAllProjectViewers (EditorsSideBar.NAME);
                                                                                    
                                                                                        viewer.removeNotification (notify);
                                                                                    
                                                                                        AbstractViewer viewer = Environment.getFocusedViewer ();
                                                                                        
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
                                                                                        
                                                                                        UIUtils.showMessage ((PopupsSupported) viewer,
                                                                                                             "Account deleted",
                                                                                                             String.format ("Your Editors Service account has been deleted.<br /><br />Your {contacts} database has <b>not</b> been deleted.<br /><a href='%s'>Click to view the folder containing the database</a>",
                                                                                                                            url));
                                                                                        
                                                                                    }
                                                                                    
                                                                                  },
                                                                                  new ActionListener ()
                                                                                  {
                                                                                    
                                                                                        public void actionPerformed (ActionEvent ev)
                                                                                        {
                                                                                            
                                                                                            AbstractViewer viewer = Environment.getFocusedViewer ();
                                                                                            
                                                                                            UIUtils.showErrorMessage (viewer,
                                                                                                                      "Unable to delete your account, please contact Quoll Writer support for assistance.");                                                                                            
                                                                                            
                                                                                        }
                                                                                    
                                                                                  });
                                            
                                        }
                                        
                                     },
                                     null,
                                     null);
        
    }

    /**
     * If we are editing any projects for any editors then show a popup offering to delete
     * the projects.  Once the delete is complete (or there are no projects) call <b>onRemoveComplete</b>.
     * If any of the projects are open then they are force closed first.
     *
     */
    public static void showDeleteProjectsForAllEditors (final AbstractViewer viewer,
                                                        final ActionListener onRemoveComplete)
    {
        
        Set<EditorEditor> eds = new HashSet ();
        
        Set<ProjectInfo> edProjs = null;
                
        try
        {
            
            edProjs = Environment.getAllProjectInfos (Project.EDITOR_PROJECT_TYPE);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to get all editor projects",
                                  e);
            
            UIUtils.showErrorMessage (viewer,
                                      "Unable to get all {projects}, please contact Quoll Support for assistance.");
            
            if (onRemoveComplete != null)
            {
                
                onRemoveComplete.actionPerformed (new ActionEvent (viewer, 1, "complete"));                
                
            }
            
            return;
            
        }
        
        for (ProjectInfo p : edProjs)
        {

            eds.add (p.getForEditor ());
            
        }
                                                                                            
        if (edProjs.size () > 0)
        {
            
            final ActionListener deleteEditorProjs = new ActionListener ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
                    
                    Set<ProjectInfo> edProjs = new LinkedHashSet ();

                    try
                    {
                        
                        edProjs = Environment.getAllProjectInfos (Project.EDITOR_PROJECT_TYPE);
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to get all editor projects",
                                              e);
                        
                        UIUtils.showErrorMessage (viewer,
                                                  "Unable to get all {projects}, please contact Quoll Support for assistance.");
                        
                        if (onRemoveComplete != null)
                        {
                            
                            onRemoveComplete.actionPerformed (new ActionEvent (viewer, 1, "complete"));                
                            
                        }

                        return;
                        
                    }
                                        
                    for (ProjectInfo p : edProjs)
                    {
                        
                        try
                        {                                                                        
                        
                            Environment.deleteProject (p,
                                                       null);
                            
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to delete project: " +
                                                  p,
                                                  e);
                            
                            UIUtils.showErrorMessage (viewer,
                                                      String.format ("Unable to get delete {project}: <b>%s</b>, please contact Quoll Support for assistance.",
                                                                     p.getName ()));
                            
                        }
                        
                    }
                                                                
                    if (onRemoveComplete != null)
                    {
                        
                        onRemoveComplete.actionPerformed (ev);
                        
                    }
                    
                }
                
            };
            
            String sb = String.format ("You are currently editing <b>%s</b> {project%s} for <b>%s</b> {contact%s}.  To remove them enter the word <b>Yes</b> below.",
                                       Environment.formatNumber (edProjs.size ()),
                                       edProjs.size () > 1 ? "s" : "",
                                       Environment.formatNumber (eds.size ()),
                                       eds.size () > 1 ? "s" : "");
            
            UIUtils.createTextInputPopup (viewer,
                                         "Delete all {projects} for {contact}",
                                         Constants.DELETE_ICON_NAME,
                                         sb,
                                         "Yes, delete them",
                                         "No, keep them",
                                         null,
                                         UIUtils.getYesValueValidator (),
                                         deleteEditorProjs,
                                         onRemoveComplete,
                                         null);
            
        } else {
         
            if (onRemoveComplete != null)
            {
            
                onRemoveComplete.actionPerformed (new ActionEvent (viewer, 1, "complete"));
                
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
                                                    final ActionListener onRemoveComplete)
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
            
            UIUtils.showErrorMessage (viewer,
                                      "Unable to get {projects} for {editor}, please contact Quoll Writer support for assistance.");
            
            return;
            
        }
                                                                                            
        if (edProjs.size () > 0)
        {
            
            final ActionListener deleteEditorProjs = new ActionListener ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
                    
                    Set<ProjectInfo> edProjs = null;
            
                    try
                    {
                        
                        edProjs = EditorsEnvironment.getProjectsForEditor (ed);
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to get projects for editor: " +
                                              ed,
                                              e);
                        
                        UIUtils.showErrorMessage (viewer,
                                                  "Unable to get {projects} for {contact}, please contact Quoll Writer support for assistance.");
                        
                        return;
                        
                    }
                                                                                                                                
                    for (ProjectInfo p : edProjs)
                    {
                        
                        // Just to be sure.
                        if (!p.getType ().equals (Project.EDITOR_PROJECT_TYPE))
                        {
                            
                            continue;
                            
                        }
                                                
                        Environment.deleteProject (p,
                                                   null);                                                            
                        
                    }
                                                                
                    if (onRemoveComplete != null)
                    {
                        
                        onRemoveComplete.actionPerformed (ev);
                        
                    }
                    
                }
                
            };
            
            String sb = String.format ("You are currently editing <b>%s</b> {project%s} for <b>%s</b>.  To remove them enter the word <b>Yes</b> below.",
                                       Environment.formatNumber (edProjs.size ()),
                                       edProjs.size () > 1 ? "s" : "",
                                       ed.getShortName ());
            
            UIUtils.createTextInputPopup (viewer,
                                         "Delete {projects} for {contact}",
                                         Constants.DELETE_ICON_NAME,
                                         sb,
                                         "Yes, delete them",
                                         "No, keep them",
                                         null,
                                         UIUtils.getYesValueValidator (),
                                         deleteEditorProjs,
                                         onRemoveComplete,
                                         null);
            
        } else {
         
            if (onRemoveComplete != null)
            {
            
                onRemoveComplete.actionPerformed (new ActionEvent (ed, 1, "complete"));
                
            }
         
        }
        
    }
    
    public static void showRemoveEditor (final AbstractViewer viewer,
                                         final EditorEditor   ed,
                                         final ActionListener onRemoveComplete)
    {
        
        // If the editor is pending then just remove them and remove the invite.
        if (ed.isPending ())
        {
            
            UIUtils.createTextInputPopup (viewer,
                                         "Remove {contact}",
                                         Constants.DELETE_ICON_NAME,
                                         String.format ("To confirm removal of <b>%s</b> as {a contact} please enter the word <b>Yes</b> in the box below.",
                                                        ed.getShortName ()),
                                         "Yes, remove them",
                                         "No, keep them",
                                         null,
                                         UIUtils.getYesValueValidator (),
                                         new ActionListener ()
                                         {
                                            
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                                
                                                EditorsEnvironment.removePendingEditor (ed,
                                                                                        onRemoveComplete);
                                                
                                            }
                                            
                                         },
                                         null,
                                         null);
            
            return;            
            
        }
        
        final ActionListener onComplete = new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                AbstractViewer viewer = Environment.getFocusedViewer ();
                
                UIUtils.showMessage ((PopupsSupported) viewer,
                                     "{Contact} removed",
                                     String.format ("<b>%s</b> has been removed as a {contact}.",
                                                    ed.getMainName ()));
                
            }
            
        };
                
        final ActionListener removeEditor = new ActionListener ()
        {
           
           public void actionPerformed (ActionEvent ev)
           {
               
               EditorsEnvironment.removeEditor (ed,
                                                new ActionListener ()
                                                {
                                                   
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                       
                                                        EditorsUIUtils.showDeleteProjectsForEditor (Environment.getFocusedViewer (),
                                                                                                    ed,
                                                                                                    onComplete);
                                                   
                                                    }
                                                   
                                                });
               
           }
           
        };
        
        String sb = String.format ("Please confirm you wish to remove <b>%s</b> as {a contact}.<br /><br />A message will be sent to <b>%s</b> informing them of your decision.<br /><br />Enter <b>Yes</b> below to remove them.<br /><br /><p class='error'>Warning: you will no longer be able to receive messages from <b>%s</b> or send messages to them.</p>",
                                    ed.getMainName (),
                                    ed.getMainName (),
                                    ed.getMainName ());
                                
        UIUtils.createTextInputPopup (viewer,
                                     "Remove {contact}",
                                     Constants.DELETE_ICON_NAME,
                                     sb,
                                     "Yes, remove them",
                                     "No, keep them",
                                     null,
                                     UIUtils.getYesValueValidator (),
                                     removeEditor,
                                     null,
                                     null);
        
    }

    /*
     * Maybe move this to be a Form.
     */
    public static void updateYourInfo (final AbstractViewer viewer)
    {

        final QPopup qp = UIUtils.createClosablePopup ("Update your name/avatar",
                                                       Environment.getIcon (Constants.EDIT_ICON_NAME,
                                                                            Constants.ICON_POPUP),
                                                       null);
    
        Box content = new Box (BoxLayout.Y_AXIS);
        
        JTextPane desc = UIUtils.createHelpTextPane ("Change your name/avatar below.  Note: clicking <b>Save</b> below will send any changes to your {editors}.",
                                                     viewer);        
                            
        content.add (desc);
        desc.setBorder (null);
        desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     desc.getPreferredSize ().height));                
                  
        final JLabel error = UIUtils.createErrorLabel ("");
                  
        error.setBorder (new EmptyBorder (10, 0, 0, 0));
        error.setVisible (false);
                  
        content.add (error);
                  
        FormLayout fl = new FormLayout ("6px, right:p, 6px, fill:200px:grow",
                                        "10px, p, 6px, top:p, 10px, p");

        fl.setHonorsVisibility (true);
        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int row = 2;
                
        builder.addLabel ("Your Name",
                          cc.xy (2,
                                 row));
    
        EditorAccount acc = EditorsEnvironment.getUserAccount ();
        
        final String accName = (acc.getName ());
    
        final JTextField nameF = new JTextField (accName);
        
        Box nameB = new Box (BoxLayout.X_AXIS);
        nameB.add (nameF);
        nameB.add (Box.createHorizontalStrut (3));
        
        builder.add (nameB,
                     cc.xy (4,
                            row));

        row += 2;
                
        builder.addLabel ("Your Avatar",
                          cc.xy (2,
                                 row));
                    
        java.util.List<String> fileTypes = new java.util.ArrayList ();
        fileTypes.add ("jpg");
        fileTypes.add ("jpeg");
        fileTypes.add ("png");
        fileTypes.add ("gif");
                
        Box b = new Box (BoxLayout.X_AXIS);
        
        BufferedImage im = acc.getAvatar ();
        
        final ImageSelector avatar = new ImageSelector (im,
                                                        fileTypes,
                                                        new Dimension (75, 75));
        avatar.setBorder (UIUtils.createLineBorder ());
            
        avatar.addChangeListener (new ChangeListener ()
        {
        
            public void stateChanged (ChangeEvent ev)
            {
                
                qp.resize ();
                
            }
            
        });
        
        b.add (avatar);
    
        builder.add (b,
                     cc.xy (4,
                            row));
                                        
        final JButton updateB = UIUtils.createButton (Environment.getButtonLabel (Constants.UPDATE_BUTTON_LABEL_ID),
                                                      null);
        final JButton cancelB = UIUtils.createButton (Environment.getButtonLabel (Constants.CANCEL_BUTTON_LABEL_ID),
                                                      null);                
        
        updateB.addActionListener (new ActionListener ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                error.setVisible (false);
                
                // Update the user data.
                String newName = nameF.getText ().trim ();
                
                if (newName.length () == 0)
                {
                    
                    error.setText ("Please provide your name.");
                    error.setVisible (true);
                    
                    qp.resize ();
                    
                    return;
                    
                }
                                
                try
                {
                
                    EditorsEnvironment.setUserInformation (newName,
                                                           avatar.getImage ());
                    
                    EditorsEnvironment.sendUserInformationToAllEditors (new ActionListener ()
                    {
                        
                        public void actionPerformed (ActionEvent ev)
                        {
                        
                            if (EditorsEnvironment.getEditors ().size () > 0)
                            {
                                                            
                                UIUtils.showMessage ((PopupsSupported) viewer,
                                                     "Details sent",
                                                     "Your updated details have been sent to your {editors}");
                                
                            }
                            
                        }
                        
                    },
                    null,
                    null);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to update user information",
                                          e);
                    
                    UIUtils.showErrorMessage (viewer,
                                              "Unable to update user information, please contact Quoll Writer support for assistance.");
                    
                }
                
                qp.removeFromParent ();
                
            }
            
        });
        
        cancelB.addActionListener (qp.getCloseAction ());
        
        row += 2;        
        
        JComponent bs = UIUtils.createButtonBar2 (new JButton[] {updateB, cancelB},
                                                  Component.LEFT_ALIGNMENT);

        builder.add (bs,
                     cc.xy (4,
                            row));

        JPanel p = builder.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        content.add (p);
                                                       
        content.setBorder (new EmptyBorder (10, 10, 10, 10));
        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                 content.getPreferredSize ().height));

        qp.setContent (content);
        
        viewer.showPopupAt (qp,
                            UIUtils.getCenterShowPosition (viewer,
                                                           qp),
                            false);
        
        qp.setDraggable (viewer);
        qp.resize ();

    }
    
    /*
     * Maybe move this to be a Form.
     */
    public static void updateEditorInfo (final AbstractViewer viewer,
                                         final EditorEditor   ed)
    {

        final QPopup qp = UIUtils.createClosablePopup ("Update the {contact} information",
                                                       Environment.getIcon (Constants.EDIT_ICON_NAME,
                                                                            Constants.ICON_POPUP),
                                                       null);
    
        Box content = new Box (BoxLayout.Y_AXIS);
        
        JTextPane desc = UIUtils.createHelpTextPane ("Change the name/avatar for <b>" + ed.getMainName () + "</b> below.",
                                                     viewer);        
                            
        content.add (desc);
        desc.setBorder (null);
        desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     desc.getPreferredSize ().height));                
                  
        final JLabel error = UIUtils.createErrorLabel ("");
                  
        error.setBorder (new EmptyBorder (10, 0, 0, 0));
        error.setVisible (false);
                  
        content.add (error);
                  
        FormLayout fl = new FormLayout ("6px, right:p, 6px, fill:200px:grow",
                                        "10px, p, 6px, top:p, 10px, p");

        fl.setHonorsVisibility (true);
        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int row = 2;
                
        builder.addLabel ("Name",
                          cc.xy (2,
                                 row));
    
        String name = ed.getMyNameForEditor ();
        
        if (name == null)
        {
            
            name = ed.getMainName ();
            
        }
    
        final String edOrigName = (ed.getName () != null ? ed.getName () : ed.getEmail ());
    
        final JTextField nameF = new JTextField (name);
        
        Box nameB = new Box (BoxLayout.X_AXIS);
        nameB.add (nameF);
        nameB.add (Box.createHorizontalStrut (3));
        nameB.add (UIUtils.createButton (Constants.RESET_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Click to reset the name to " + edOrigName,
                                         new ActionListener ()
                                         {
                                            
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                                
                                                nameF.setText (edOrigName);
                                                
                                            }
                                            
                                         }));
        
        builder.add (nameB,
                     cc.xy (4,
                            row));

        row += 2;
                
        builder.addLabel ("Avatar",
                          cc.xy (2,
                                 row));
                    
        java.util.List<String> fileTypes = new java.util.ArrayList ();
        fileTypes.add ("jpg");
        fileTypes.add ("jpeg");
        fileTypes.add ("png");
        fileTypes.add ("gif");
                
        Box b = new Box (BoxLayout.X_AXIS);
        
        BufferedImage im = ed.getMyAvatarForEditor ();
        
        if (im == null)
        {
            
            im = ed.getAvatar ();
            
        }
        
        final ImageSelector avatar = new ImageSelector (im,
                                                        fileTypes,
                                                        new Dimension (75, 75));
        avatar.setBorder (UIUtils.createLineBorder ());
            
        avatar.addChangeListener (new ChangeListener ()
        {
        
            public void stateChanged (ChangeEvent ev)
            {
                
                qp.resize ();
                
            }
            
        });
        
        b.add (avatar);
    
        builder.add (b,
                     cc.xy (4,
                            row));
                                        
        final JButton updateB = UIUtils.createButton (Environment.getButtonLabel (Constants.UPDATE_BUTTON_LABEL_ID),
                                                      null);
        final JButton cancelB = UIUtils.createButton (Environment.getButtonLabel (Constants.CANCEL_BUTTON_LABEL_ID),
                                                      null);                
        
        updateB.addActionListener (new ActionListener ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                error.setVisible (false);
                
                // Update "my" data for the editor.
                String newName = nameF.getText ().trim ();
                
                if (newName.length () == 0)
                {
                    
                    error.setText ("A name must be specified.");
                    error.setVisible (true);
                    
                    qp.resize ();
                    
                    return;
                    
                }
                
                BufferedImage avIm = UIUtils.getScaledImage (avatar.getImage (),
                                                             300);
                
                ed.setMyAvatarForEditor (avIm);
                ed.setMyNameForEditor (nameF.getText ().trim ());
                
                try
                {
                
                    EditorsEnvironment.updateEditor (ed);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to update editor: " + ed,
                                          e);
                    
                    UIUtils.showErrorMessage (viewer,
                                              "Unable to update {editor}, please contact Quoll Writer support for assistance.");
                    
                }
                                                
                qp.removeFromParent ();
                
            }
            
        });
        
        cancelB.addActionListener (qp.getCloseAction ());
        
        row += 2;        
        
        JComponent bs = UIUtils.createButtonBar2 (new JButton[] {updateB, cancelB},
                                                  Component.LEFT_ALIGNMENT);

        builder.add (bs,
                     cc.xy (4,
                            row));

        JPanel p = builder.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        content.add (p);
                                                       
        content.setBorder (new EmptyBorder (10, 10, 10, 10));
        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                 content.getPreferredSize ().height));

        qp.setContent (content);
        
        viewer.showPopupAt (qp,
                            UIUtils.getCenterShowPosition (viewer,
                                                           qp),
                            false);
        
        qp.setDraggable (viewer);
        qp.resize ();
    }
    
    public static void showEditorInfoReceived (final EditorInfoMessage info)
    {

        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
    
                final EditorEditor ed = info.getEditor ();
            
                Box content = new Box (BoxLayout.Y_AXIS);
                
                AbstractViewer viewer = Environment.getFocusedViewer ();
                
                JTextPane desc = UIUtils.createHelpTextPane ("<b>" + ed.getMainName () + "</b> has updated their information.  To accept the changes click on the <b>Update</b> button below, to keep the current name and avatar click <b>Reject</b>.",
                                                             viewer);        
                                    
                content.add (desc);
                desc.setBorder (null);
                desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                             desc.getPreferredSize ().height));                
                    
                content.add (Box.createVerticalStrut (10));
                
                FormLayout fl = new FormLayout ("6px, right:p, 6px, fill:200px:grow",
                                                "10px, p, 6px, p");
        
                fl.setHonorsVisibility (true);
                PanelBuilder builder = new PanelBuilder (fl);
        
                CellConstraints cc = new CellConstraints ();
        
                int row = 2;
                        
                builder.addLabel ("Their New Name",
                                  cc.xy (2,
                                         row));
            
                builder.addLabel (info.getName (),
                                  cc.xy (4,
                                         row));
                
                row += 2;
                        
                builder.addLabel ("Their New Avatar",
                                  cc.xy (2,
                                         row));
                                    
                Box b = new Box (BoxLayout.X_AXIS);
                
                ImagePanel avatar = new ImagePanel (UIUtils.getScaledImage (info.getAvatar (),
                                                                            100),
                                                    null);
                avatar.setBorder (UIUtils.createLineBorder ());
                    
                b.add (avatar);
            
                builder.add (b,
                             cc.xy (4,
                                    row));
                            
                JPanel p = builder.getPanel ();
                p.setOpaque (false);
                p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        
                content.add (p);
                    
                Map<String, ActionListener> buttons = new LinkedHashMap ();
                
                buttons.put ("Update",
                             new ActionListener ()
                             {
                                
                                public void actionPerformed (ActionEvent ev)
                                {
                                    
                                    try
                                    {
                                        
                                        BufferedImage avIm = UIUtils.getScaledImage (info.getAvatar (),
                                                                                     300);
                                        
                                        ed.setMyAvatarForEditor (avIm);
                                        ed.setMyNameForEditor (info.getName ());
                                        ed.setAvatar (avIm);
                                        ed.setName (info.getName ());
                                                    
                                        EditorsEnvironment.updateEditor (ed);
        
                                        info.setDealtWith (true);
                                        
                                        EditorsEnvironment.updateMessage (info);
                                        
                                    } catch (Exception e) {
                                        
                                        // We reget the viewer here since there is no guarantee that the old one is still
                                        // valid.
                                        AbstractViewer viewer = Environment.getFocusedViewer ();
                                        
                                        UIUtils.showErrorMessage (viewer,
                                                                  "Unable to update {editor} information, please contact Quoll Writer support for assistance.");
                                        
                                        Environment.logError ("Unable to update editor: " +
                                                              ed +
                                                              " with information: " +
                                                              info,
                                                              e);
                                        
                                    }
                                    
                                }
                                
                             });
                
                buttons.put ("Reject",
                             new ActionListener ()
                             {
                                
                                public void actionPerformed (ActionEvent ev)
                                {
                                    
                                    try
                                    {
                                        
                                        info.setDealtWith (true);
                                        
                                        EditorsEnvironment.updateMessage (info);
                                        
                                    } catch (Exception e) {
                                        
                                        // We reget the viewer here since there is no guarantee that the old one is still
                                        // valid.
                                        AbstractViewer viewer = Environment.getFocusedViewer ();
                                        
                                        UIUtils.showErrorMessage (viewer,
                                                                  "Unable to update {editor} information, please contact Quoll Writer support for assistance.");
                                        
                                        Environment.logError ("Unable to save editor info message for editor: " +
                                                              ed +
                                                              " with information: " +
                                                              info,
                                                              e);
                                        
                                    }
                                    
                                }
                                
                             });
        
                buttons.put ("I'll decide later",
                             new ActionListener ()
                             {
                                
                                public void actionPerformed (ActionEvent ev)
                                {
                                                                        
                                }
                                
                             });

                UIUtils.createQuestionPopup (viewer,
                                             "{An editor} has updated their information",
                                             "edit",
                                             content,
                                             buttons,
                                             null,
                                             null);
                
            }
            
        });
        
    }
    
    public static JComponent createSendProjectPanel (final AbstractProjectViewer viewer,
                                                     final EditorEditor          ed,
                                                     final ActionListener        onSend)
    {

        // See what the last send/update message was, if any.
        try
        {
            
            EditorsEnvironment.loadMessagesForEditor (ed);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to load messages for editor: " +
                                  ed,
                                  e);
            
            UIUtils.showErrorMessage (viewer,
                                      "Unable to show send {project} window, please contact Quoll Writer support for assistance.");
            
            return null;
                
        }
        
        Project p = viewer.getProject ();        
        
        String updateText = "";
        
        SortedSet<EditorMessage> messages = ed.getMessages (p.getId (),
                                                            NewProjectMessage.MESSAGE_TYPE,
                                                            UpdateProjectMessage.MESSAGE_TYPE);
        
        boolean update = false;
        
        Set<String> chapterIds = new HashSet ();
        
        if ((messages != null)
            &&
            (messages.size () > 0)
           )
        {
            
            // Get the last one.
            EditorMessage last = messages.last ();

            update = true;
                        
            AbstractProjectMessage pm = (AbstractProjectMessage) last;

            Set<Chapter> chaps = pm.getChapters ();
            String verName = pm.getProjectVersion ().getName ();
            
            if (last instanceof NewProjectMessage)
            {
                
                if (verName != null)
                {
                
                    updateText = "You sent version <b>%s</b> of the {project} on <b>%s</b>.";
                    
                } else {
                    
                    updateText = "You sent the {project} on <b>%s</b>.";
                    
                }
                
                // TODO: Add link to view what was sent.
                
            }
        
            if (last instanceof UpdateProjectMessage)
            {
                                
                if (verName != null)
                {
                
                    updateText = "You last sent version <b>%s</b> of the {project} on <b>%s</b>.";
                    
                } else {
                    
                    updateText = "You last sent the {project} on <b>%s</b>.";
                    
                }
                
            }
            
            updateText = "<br /><br />" + updateText + "  The {chapters} you previously sent have been pre-selected.";
            
            if (verName != null)
            {
            
                updateText = String.format (updateText,
                                            verName,
                                            Environment.formatDate (last.getWhen ()));

            } else {
                
                updateText = String.format (updateText,
                                            Environment.formatDate (last.getWhen ()));
                
            }
            
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
    
        final boolean fupdate = update;
    
        final Box content = new Box (BoxLayout.Y_AXIS);
                            
        JTextPane desc = UIUtils.createHelpTextPane (String.format ("Select the {chapters} you wish to send to <b>" + ed.getMainName () + "</b>.  You can also select a date indicating when you would like the editing to be completed, remember to be reasonable, {editors} have lives too!<br /><br />It is recommended that you provide a <i>Version</i> such as <b>1st Draft</b> or <b>Final edit</b> so that when you get comments back you know what they relate to.%s",
                                                                    updateText),
                                                     viewer);        
        desc.setBorder (null);
        
        content.add (desc);
                    
        final JLabel error = UIUtils.createErrorLabel ("");
        error.setBorder (new EmptyBorder (10, 10, 0, 10));
        error.setVisible (false);
        
        content.add (error);
                    
        FormLayout fl = new FormLayout ("6px, right:p, 6px, fill:200px:grow",
                                        "10px, p, 6px, p, 6px, top:p, 6px, p, 6px, top:p, 0px, p, 10px, p");

        fl.setHonorsVisibility (true);
        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int row = 2;
                
        builder.addLabel (Environment.replaceObjectNames ("{Project}"),
                          cc.xy (2,
                                 row));

        builder.addLabel (p.getName (),
                          cc.xy (4,
                                 row));
                                 
        row += 2;
                                 
        builder.addLabel ("Version",
                          cc.xy (2,
                                 row));
        
        final JTextField version = new JTextField ();
        
        builder.add (version,
                     cc.xy (4,
                            row));
        
        row += 2;
                
        builder.addLabel (Environment.replaceObjectNames ("Notes"),
                          cc.xy (2,
                                 row));
        
        final TextArea notes = new TextArea (Environment.replaceObjectNames (String.format ("Tell %s about your {project}/book/story here.  Also add instructions/hints on what you are wanting them to do, what to look at specifically and so on.",
                                                                                            ed.getMainName ())),
                                             4,
                                             5000);
        notes.setAutoGrabFocus (false);
                                             
        builder.add (notes,
                     cc.xy (4,
                            row));

        row += 2;
                                        
        builder.addLabel (Environment.replaceObjectNames ("Due by"),
                          cc.xy (2,
                                 row));
        
        Calendar cal = new GregorianCalendar ();
        cal.add (Calendar.DATE,
                 7);
        
        final JDateChooser jcal = new JDateChooser (cal.getTime ());
        
        jcal.setMinSelectableDate (new Date ());        
        jcal.getCalendarButton ().setMargin (new java.awt.Insets (3, 3, 3, 3));
        jcal.setIcon (Environment.getIcon ("date",
                                           16));
        
        Box calBox = new Box (BoxLayout.X_AXIS);
        calBox.add (jcal);
        jcal.setMaximumSize (jcal.getPreferredSize ());
        builder.add (calBox,
                     cc.xy (4,
                            row));        

        row += 2;

        builder.addLabel (Environment.replaceObjectNames ("{Chapters}"),
                          cc.xy (2,
                                 row));
                                                
        JTree tree = UIUtils.createSelectableTree ();
                
        final DefaultMutableTreeNode root = UIUtils.createTreeNode (p,
                                                                    null,
                                                                    null,
                                                                    true);
                                
        Book b = (Book) p.getBooks ().get (0);

        DefaultMutableTreeNode broot = UIUtils.createTreeNode (b,
                                                               null,
                                                               null,
                                                               true);
        
        //root.add (broot);
        
        // Get the chapters.
        List<Chapter> chaps = b.getChapters ();

        for (Chapter c : chaps)
        {

            DefaultMutableTreeNode node = UIUtils.createTreeNode (c,
                                                                  null,
                                                                  null,
                                                                  true);

            SelectableDataObject s = (SelectableDataObject) node.getUserObject ();

            s.selected = chapterIds.contains (c.getId ());
                                                                  
            if (node == null)
            {

                continue;

            }

            root.add (node);

        }

        tree.setModel (new DefaultTreeModel (root));
                   
        UIUtils.expandAllNodesWithChildren (tree);                   
        
        JComponent t = tree;
                    
        if (tree.getPreferredSize ().height > 200)
        {

            JScrollPane sp = UIUtils.createScrollPane (tree);
            sp.setOpaque (false);
            //sp.setBorder (null);

            sp.setPreferredSize (new Dimension (500,
                                                200));
            
            t = sp;
                    
        }
        
        builder.add (t,
                     cc.xy (4,
                            row));                
             
        row += 2;
                                                     
        // See if we have sent this editor anything before, if so offer to show what was sent.

        JLabel sending = UIUtils.createLoadingLabel ("Sending {project}");
        sending.setText (Environment.replaceObjectNames ("Sending {project}..."));
        sending.setVisible (true);
        
        final Box sendBox = new Box (BoxLayout.X_AXIS);
        sendBox.setBorder (new EmptyBorder (10, 0, 0, 0));
        sendBox.add (sending);
        sendBox.setVisible (false);
        
        builder.add (sendBox,
                     cc.xy (4,
                            row));                
             
        row += 2;        
        
        final JButton send = UIUtils.createButton ("Send",
                                                   null);

        ActionListener sendAction = new ActionListener ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                Set<Chapter> chapters = new LinkedHashSet ();
                
                UIUtils.getSelectedObjects (root,
                                            chapters);
                    
                String err = null;
                                
                if (chapters.size () == 0)
                {
                    
                    // Show the error.
                    err = "Please select at least 1 {chapter}.";
                    
                }
                
                for (Chapter c : chapters)
                {
                    
                    AbstractEditorPanel qp = viewer.getEditorForChapter (c);
                    
                    if (qp != null)
                    {
                        
                        if (qp.hasUnsavedChanges ())
                        {
                            
                            err = "One of the selected {chapters} has unsaved changes, please save your work before sending the {project}.";
                            
                            break;
                        
                        }
                        
                    }
                    
                }
                
                if (notes.getText ().trim ().length () > 5000)
                {
                    
                    err = "Notes can be a maximum of 5000 characters.";
                                
                }
                
                if (err != null)
                {
                    
                    error.setText (err);
                    error.setVisible (true);
                    
                    UIUtils.doLater (new ActionListener ()
                    {
                        
                        public void actionPerformed (ActionEvent ev)
                        {
                            
                            UIUtils.resizeParent (content);
                            
                        }
                        
                    });
                    
                    UIUtils.resizeParent (content);
                    
                    return;
                    
                }
                
                send.setEnabled (false);
                error.setVisible (false);
                
                // Show the sending.
                sendBox.setVisible (true);
                
                UIUtils.resizeParent (content);
                
                String n = null;
                
                if (!notes.getForeground ().equals (UIUtils.getHintTextColor ()))
                {
                    
                    n = notes.getText ().trim ();
                    
                }
                
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
                pv.setDueDate (jcal.getDate ());
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
                    
                    UIUtils.showErrorMessage (viewer,
                                              "Unable to send new project to {editor}, please contact Quoll Writer support for assistance.");
                    
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
                                                                
                EditorsEnvironment.sendMessageToEditor (mess,
                                                        // On send.
                                                        new ActionListener ()
                                                        {
                                                            
                                                            public void actionPerformed (ActionEvent ev)
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
                                                                                                                                        
                                                                    pe.setStatusMessage (String.format ("{Project} sent: %s",
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
                                                                        
                                                                        UIUtils.showErrorMessage (viewer,
                                                                                                  "Unable to add {editor} " + ed.getMainName () + " to the {project}.  Please contact Quoll Writer support for assistance.");
                                                                        
                                                                    }
                                                                        
                                                                } else {
                                                                    
                                                                    try
                                                                    {

                                                                        // Update them to be current.
                                                                        pe.setCurrent (true);
                                                                        pe.setEditorFrom (new Date ());
                                                                        pe.setEditorTo (null);
                                                                        
                                                                        pe.setStatusMessage (String.format ("{Project} updated: %s",
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
                                                                        
                                                                        UIUtils.showErrorMessage (viewer,
                                                                                                  "Unable to add {editor} " + ed.getMainName () + " to the {project}.  Please contact Quoll Writer support for assistance.");
                                                                        
                                                                    }
                                                                    
                                                                }
                                                                
                                                                UIUtils.showMessage ((PopupsSupported) viewer,
                                                                                     "Your {project} has been sent",
                                                                                     String.format ("Your {project} <b>%s</b> has been sent to <b>%s</b>",
                                                                                                    viewer.getProject ().getName (),
                                                                                                    ed.getMainName ()));
                                                                
                                                                UIUtils.closePopupParent (content);
                                                                
                                                            }
                                                            
                                                        },
                                                        // On cancel of login.
                                                        null,
                                                        null);
                
            }
            
        };

        send.addActionListener (sendAction);
        
        UIUtils.addDoActionOnReturnPressed (version,
                                            sendAction);
        UIUtils.addDoActionOnReturnPressed (notes,
                                            sendAction);
                
        JButton cancel = UIUtils.createButton (Environment.getButtonLabel (Constants.CANCEL_BUTTON_LABEL_ID),
                                               null);
        
        cancel.addActionListener (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
            
                // Check to see if we are sending to a pending editor.
                if (ed.isPending ())
                {
                    
                    InviteMessage invite = new InviteMessage (EditorsEnvironment.getUserAccount ());
                    
                    invite.setEditor (ed);
                    
                    // Send an invite.
                    EditorsEnvironment.sendMessageToEditor (invite,
                                                            new ActionListener ()
                                                            {
                                                                
                                                                public void actionPerformed (ActionEvent ev)
                                                                {
                                                                    
                                                                    AbstractViewer viewer = Environment.getFocusedViewer ();
                                                                    
                                                                    UIUtils.showMessage ((PopupsSupported) viewer,
                                                                                         "Invite sent",
                                                                                         String.format ("An invite has been sent to: <b>%s</b>.",
                                                                                                        ed.getEmail ()),
                                                                                         "Ok, got it",
                                                                                         null);                                                                                                            
                                                                    
                                                                }
                                                                
                                                            },
                                                            null,
                                                            null);
                    
                }

                UIUtils.closePopupParent (content);

            }
                
        });
        
        JButton[] buts = new JButton[] { send, cancel };
        
        JComponent buttons = UIUtils.createButtonBar2 (buts,
                                                       Component.LEFT_ALIGNMENT); 
        buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        builder.add (buttons,
                     cc.xy (4,
                            row));        

        JPanel bp = builder.getPanel ();
        bp.setOpaque (false);
        bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        content.add (bp);             

        content.addAncestorListener (new AncestorListener ()
        {
           
            @Override
            public void ancestorAdded (AncestorEvent ev)
            {
                
                if (version.isShowing ())
                {
                    
                    version.grabFocus ();
                    
                }
                
            }
            
            @Override
            public void ancestorMoved (AncestorEvent ev)
            {
                                
            }

            @Override
            public void ancestorRemoved (AncestorEvent ev)
            {
                                
            }
            
        });
        
        return content;
        
    }
    
    public static void showSendProject (final AbstractProjectViewer viewer,
                                        final EditorEditor          ed,
                                        final ActionListener        onSend)
    {
        
        final QPopup popup = UIUtils.createClosablePopup ("Send {project}",
                                                          Environment.getIcon (Constants.SEND_ICON_NAME,
                                                                               Constants.ICON_POPUP),
                                                          null);

        if (ed.isPending ())
        {
            
            // If the editor is not pending then we make the popup non-closable since we have to send a message and
            // don't want it being sent multiple times.
            popup.getHeader ().getControls ().setVisible (false);
                                                          
        }

        JComponent content = EditorsUIUtils.createSendProjectPanel (viewer,
                                                                    ed,
                                                                    onSend);
        content.setBorder (UIUtils.createPadding (10, 10, 10, 10));
                         
        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                     content.getPreferredSize ().height + 20));

        popup.setContent (content);
                                                               
        viewer.showPopupAt (popup,
                            UIUtils.getCenterShowPosition (viewer,
                                                           popup),
                            false);
        
        popup.resize ();
        
        popup.setDraggable (viewer);
                
        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                popup.resize ();
                
            }
            
        });
        
    }
    
    public static void showUpdateProject (final AbstractProjectViewer viewer,
                                          final EditorEditor          ed,
                                          final ActionListener        onSend)
    {
        
        if (ed.isPending ())
        {
            
            throw new IllegalStateException ("Can only update a project for a non-pending editor.");
            
        }
        
        final QPopup popup = UIUtils.createClosablePopup ("Update {project}",
                                                          Environment.getIcon (Constants.SEND_ICON_NAME,
                                                                               Constants.ICON_POPUP),
                                                          null);

        JComponent content = EditorsUIUtils.createSendProjectPanel (viewer,
                                                                    ed,
                                                                    onSend);
        content.setBorder (UIUtils.createPadding (10, 10, 10, 10));
                         
        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                     content.getPreferredSize ().height + 20));

        popup.setContent (content);
                                                               
        viewer.showPopupAt (popup,
                            UIUtils.getCenterShowPosition (viewer,
                                                           popup),
                            false);
        popup.setDraggable (viewer);

        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                popup.resize ();
                
            }
            
        });

    }
    
    public static void showNewProjectResponseNotification (final NewProjectResponseMessage mess)
    {
        
        UIUtils.doLater (new ActionListener ()
        {
        
            public void actionPerformed (ActionEvent ev)
            {

                ProjectInfo proj = null;
                
                try
                {
                    
                    proj = Environment.getProjectById (mess.getForProjectId (),
                                                       Project.NORMAL_PROJECT_TYPE);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to get project by id: " +
                                          mess.getForProjectId (),
                                          e);
                    
                    return;
                    
                }
                
                if (proj == null)
                {
                    
                    // Should probably send an error message back.
                    EditorsEnvironment.sendError (mess,
                                                  ErrorMessage.ErrorType.projectnotexists,
                                                  "Unknown project : " +
                                                  mess.getForProjectId ());
                    
                    Environment.logError ("Unable to find project by id: " +
                                          mess.getForProjectId () +
                                          ", project doesn't exist.");
                    
                    return;
                    
                }
                
                final EditorEditor ed = mess.getEditor ();
                
                String title = "Editing your {project}";
            
                final boolean accepted = mess.isAccepted ();
            
                String acceptedText = (accepted ? "accepted" : "rejected");
            
                Box content = new Box (BoxLayout.Y_AXIS);
                
                final AbstractViewer viewer = Environment.getFocusedViewer ();
                
                JTextPane desc = UIUtils.createHelpTextPane (String.format ("<b>%s</b> has <b>%s</b> your offer to edit your {project} %s.",
                                                                            ed.getMainName (),
                                                                            acceptedText,
                                                                            proj.getName ()),
                                                             viewer);        
                                    
                content.add (desc);
                desc.setBorder (null);
                desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                             desc.getPreferredSize ().height));                
                    
                content.add (Box.createVerticalStrut (10));
            
                if (mess.getResponseMessage () != null)
                {
                    
                    content.add (UIUtils.createBoldSubHeader ("Response from " + ed.getMainName (),
                                                              null));
                    
                    JTextPane res = UIUtils.createHelpTextPane (mess.getResponseMessage (),
                                                                viewer);        
                                        
                    content.add (res);
                    res.setBorder (new EmptyBorder (5, 5, 0, 5));
                    res.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                                res.getPreferredSize ().height));                
                        
                    content.add (Box.createVerticalStrut (10));
                    
                }
            
                final ProjectInfo fproj = proj;
            
                UIUtils.showMessage ((PopupsSupported) viewer,
                                     title,
                                     content,
                                     Environment.getButtonLabel (Constants.CONFIRM_BUTTON_LABEL_ID),
                                     new ActionListener ()
                                     {
                                        
                                        public void actionPerformed (ActionEvent ev)
                                        {
                                            
                                            mess.setDealtWith (true);
                                            
                                            try
                                            {
                                            
                                                EditorsEnvironment.updateMessage (mess);
                                                
                                            } catch (Exception e) {
                                                
                                                Environment.logError ("Unable to update message: " +
                                                                      mess,
                                                                      e);
                                                
                                            }

                                            if (ed.isPending ())
                                            {
                                                
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
                                            
                                            // Remove them as a project editor.
                                            ProjectEditor pe = null;
                                            
                                            try
                                            {
                                                
                                                pe = EditorsEnvironment.getProjectEditor (fproj,
                                                                                          ed);
                                                
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
                                                            
                                                        } catch (Exception e) {
                                                            
                                                            Environment.logError ("Unable to accept project editor: " + 
                                                                                  pe,
                                                                                  e);
                                                            
                                                        }
                                                        
                                                    }
                                                    
                                                }
                                                
                                            } catch (Exception e) {
                                                
                                                Environment.logError ("Unable to get project editor for project: " +
                                                                      fproj +
                                                                      ", and editor: " +
                                                                      ed,
                                                                      e);
                                                
                                            }
                                                                                                
                                        }
                                        
                                     });
            
            }
            
        });
            
    }
    
    public static void handleNewProjectResponse (      AbstractViewer    viewer,
                                                 final NewProjectMessage mess,
                                                 final boolean           accepted)
    {

        if (viewer == null)
        {
            
            viewer = Environment.getFocusedViewer ();
            
        }
    
        if (accepted)
        {
    
            final QPopup qp = UIUtils.createClosablePopup ("Accept the invitation",
                                                           Environment.getIcon (Constants.PROJECT_ICON_NAME,
                                                                                Constants.ICON_POPUP),
                                                           null);
                                                
            String rows = "p, 6px, p, 6px, p, 6px, p";
                        
            int row = 1;
            
            Box content = new Box (BoxLayout.Y_AXIS);
            
            content.add (UIUtils.createBoldSubHeader ("Where should the {project} be saved?",
                                                      null));
            
            content.add (Box.createVerticalStrut (5));
            
            final FormLayout fl = new FormLayout ("right:p, 6px, fill:100px:grow, 2px, p",
                                                  rows);
            fl.setHonorsVisibility (true);
            final PanelBuilder builder = new PanelBuilder (fl);
    
            final CellConstraints cc = new CellConstraints ();
    
            final String projName = mess.getForProjectName ();
    
            builder.addLabel (Environment.replaceObjectNames ("{Project}"),
                              cc.xy (1,
                                     row));
            builder.addLabel (projName,
                              cc.xy (3,
                                     row));
    
            row += 2;
                                
            builder.addLabel ("Save In",
                              cc.xy (1,
                                     row));
    
            File defDir = Environment.getDefaultSaveProjectDir ();
            
            defDir.mkdirs ();
    
            File nf = new File (defDir, "editor-projects/" + mess.getEditor ().getEmail ());
    
            final FileFinder saveField = new FileFinder ();
        
            saveField.setFile (nf);
    
            saveField.setApproveButtonText ("Select");
            saveField.setFinderSelectionMode (JFileChooser.DIRECTORIES_ONLY);
            saveField.setFinderTitle ("Select a directory to save to");
                        
            saveField.setFindButtonToolTip ("Click to find a directory");
            saveField.setClearOnCancel (true);
            saveField.init ();
    
            builder.add (saveField,
                         cc.xy (3,
                                row));
            
            row += 2;
                                
            final JCheckBox encryptField = UIUtils.createCheckBox ("Encrypt this {project}?  You will be prompted for a password.");
            encryptField.setBackground (Color.WHITE);
    
            builder.add (encryptField,
                         cc.xyw (3,
                                 row,
                                 2));
    
            row += 2;
    
            FormLayout pfl = new FormLayout ("right:p, 6px, 100px, 20px, p, 6px, fill:100px",
                                             "p, 6px");
            pfl.setHonorsVisibility (true);
            PanelBuilder pbuilder = new PanelBuilder (pfl);
    
            final JPasswordField passwordField = new JPasswordField ();
    
            pbuilder.addLabel ("Password",
                               cc.xy (1,
                                      1));
    
            pbuilder.add (passwordField,
                          cc.xy (3,
                                 1));
    
            final JPasswordField passwordField2 = new JPasswordField ();
    
            pbuilder.addLabel ("Confirm",
                               cc.xy (5,
                                      1));
    
            pbuilder.add (passwordField2,
                          cc.xy (7,
                                 1));
                                     
            final JPanel ppanel = pbuilder.getPanel ();
            ppanel.setVisible (false);
            ppanel.setOpaque (false);
    
            builder.add (ppanel,
                         cc.xyw (3,
                                 row,
                                 2));
    
            encryptField.addActionListener (new ActionListener ()
            {
    
                public void actionPerformed (ActionEvent ev)
                {
    
                    ppanel.setVisible (encryptField.isSelected ());
                        
                    qp.resize ();
                    
                }
    
            });
                                
            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            p.setBorder (new EmptyBorder (0, 5, 0, 5));
            /*
            if (createOnReturn)
            {
                        
                UIUtils.addDoActionOnReturnPressed (this.nameField,
                                                    createProjectAction);        
                UIUtils.addDoActionOnReturnPressed (this.saveField,
                                                    createProjectAction);        
                UIUtils.addDoActionOnReturnPressed (this.passwordField,
                                                    createProjectAction);        
                UIUtils.addDoActionOnReturnPressed (this.passwordField2,
                                                    createProjectAction);        
    
            }
    */
            final JLabel error = UIUtils.createErrorLabel ("");
            error.setVisible (false);
            error.setBorder (new EmptyBorder (0,
                                              0,
                                              5,
                                              0));
                        
            content.add (error);
            content.add (p);
                            
            content.add (Box.createVerticalStrut (10));
            content.add (UIUtils.createBoldSubHeader ("Send a message back",
                                                      null));
                            
            final TextArea res = new TextArea ("You can optionally return a message to " + mess.getEditor ().getMainName () + " using this box.",
                                               3,
                                               5000);
            
            //res.setBorder (new EmptyBorder (5, 5, 0, 5));
            
            content.add (res);
                          
            JButton save = UIUtils.createButton ("Save {project}",
                                                 null);
            
            ActionListener doSave = new ActionListener ()
            {
               
                public void actionPerformed (ActionEvent ev)
                {
                               
                    error.setVisible (false);
                               
                    // See if the project already exists.
                    File pf = new File (saveField.getSelectedFile (), Utils.sanitizeForFilename (projName));
            
                    if (pf.exists ())
                    {
            
                        error.setText (Environment.replaceObjectNames (String.format ("A {project} called: %s already exists.",
                                                                                      projName)));
            
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
            
                            error.setText (Environment.replaceObjectNames ("Please provide a password for securing the {project}."));
                
                            error.setVisible (true);
                
                            qp.resize ();
                
                            return;
            
                        }
            
                        if (pwd2.equals (""))
                        {
            
                            error.setText ("Please confirm your password.");

                            error.setVisible (true);
                
                            qp.resize ();
                
                            return;
            
                        }
            
                        if (!pwd.equals (pwd2))
                        {
            
                            error.setText ("The passwords do not match.");

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
                                                  "Unable to save {project}, please contact Quoll Writer support for assistance.");
                        
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
                                                  "Unable to save {project}, please contact Quoll Writer support for assistance.");
                        
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
                                                          "Unable to update message, please contact Quoll Writer support for assistance.");
                                
                                // Should really carry on... maybe...
                                
                            }
                                                      
                            // Ask if they want to open the project now.
                            UIUtils.createQuestionPopup (Environment.getFocusedViewer (),
                                                         "Open the {project}?",
                                                         Constants.OPEN_PROJECT_ICON_NAME,
                                                         "Open the {project} now?",
                                                         "Yes, open it",
                                                         "Not now",
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
                                                                                                    AbstractProjectViewer pv = (AbstractProjectViewer) ev.getSource ();
                                                                                                    
                                                                                                    pv.viewObject (pv.getProject ().getBook (0).getChapters ().get (0));
                                                                                                    
                                                                                                }
                                                                                                
                                                                                             });
                                                                    
                                                                } catch (Exception e) {
                                                                    
                                                                    Environment.logError ("Unable to open project: " +
                                                                                          fproj,
                                                                                          e);
                                                                    
                                                                    UIUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                                                              "Unable to open {project}: " + fproj.getName () + " please contact Quoll Support for assistance.");
                                                                    
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
            
            save.addActionListener (doSave);
            
            UIUtils.addDoActionOnReturnPressed (res,
                                                doSave);
            
            JButton cancel = UIUtils.createButton (Environment.getButtonLabel (Constants.CANCEL_BUTTON_LABEL_ID),
                                                   null);
            
            cancel.addActionListener (new ActionListener ()
            {
               
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
                                                  "Unable to update message, please contact Quoll Writer support for assistance.");
                        
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
    
    public static void showSendUnsentComments (final AbstractProjectViewer viewer,
                                               final ActionListener        onSend)
    {
        
        UIUtils.doLater (new ActionListener ()
        {
        
            public void actionPerformed (ActionEvent ev)
            {

                final EditorEditor ed = viewer.getProject ().getForEditor ();
            
                final QPopup popup = UIUtils.createClosablePopup ("Send {comments} to {editor}",
                                                                  Environment.getIcon (Constants.SEND_ICON_NAME,
                                                                                       Constants.ICON_POPUP),
                                                                  null);
            
                Box content = new Box (BoxLayout.Y_AXIS);

                JTextPane desc = UIUtils.createHelpTextPane (String.format ("Select the {comments} you wish to send to <b>%s</b> below.",
                                                                            ed.getMainName ()),
                                                             viewer);        
                                    
                content.add (desc);
                desc.setBorder (null);
                desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                             desc.getPreferredSize ().height));                
                    
                final JLabel error = UIUtils.createErrorLabel ("Please select at least 1 {comment}.");        
                error.setBorder (UIUtils.createPadding (10, 0, 0, 0));
                error.setVisible (false);
                content.add (error);                
                
                FormLayout fl = new FormLayout ("6px, right:p, 6px, fill:200px:grow",
                                                "10px, top:p, 6px, top:p, 0px, p, 10px, p");
        
                fl.setHonorsVisibility (true);
                PanelBuilder builder = new PanelBuilder (fl);
        
                CellConstraints cc = new CellConstraints ();
        
                int row = 2;
                        
                builder.addLabel (Environment.replaceObjectNames ("Notes"),
                                  cc.xy (2,
                                         row));
                        
                final TextArea genComments = new TextArea ("Optionally, you can provide some general thoughts/comments about the {project} here.",
                                                           3,
                                                           5000);

                builder.add (genComments,
                             cc.xy (4,
                                    row));
                
                row += 2;
                
                Set exclude = new HashSet ();
                Set init = new HashSet ();
                
                Set<NamedObject> notes = viewer.getProject ().getAllNamedChildObjects (Note.class);
                
                for (NamedObject d : notes)
                {
                    
                    if (!(d instanceof Note))
                    {
                        
                        Environment.logError ("Found an object of type: " +
                                              d.getClass ().getName () +
                                              " when looking for: " +
                                              Note.class.getName ());
                        
                        continue;
                        
                    }
                    
                    Note n = (Note) d;
                    
                    if (n.isDealtWith ())
                    {
                        
                        exclude.add (n);
                        
                    } else {
                        
                        init.add (n);
                        
                        // Also add the chapter.
                        init.add (n.getChapter ());
                                                
                    }
                                        
                }
                
                Set<NamedObject> chaps = viewer.getProject ().getAllNamedChildObjects (Chapter.class);
                
                for (NamedObject d : chaps)
                {
                    
                    if (init.contains (d))
                    {
                        
                        continue;
                        
                    }
                    
                    exclude.add (d);
                    
                }
                
                JTree tree = UIUtils.createSelectableTree ();
                
                SelectableProjectTreeCellRenderer cr = (SelectableProjectTreeCellRenderer) tree.getCellRenderer ();
                
                cr.setIconType (Note.OBJECT_TYPE,
                                Constants.COMMENT_ICON_NAME);
                
                final DefaultMutableTreeNode root = EditorsUIUtils.createTree (viewer.getProject (),
                                                                               exclude,
                                                                               init,
                                                                               true);
                
                ((DefaultTreeModel) tree.getModel ()).setRoot (root);

                for (Object o : init)
                {
        
                    tree.expandPath (UIUtils.getTreePathForUserObject (root,
                                                                       o));
        
                }                        
                
                Dimension pref = tree.getPreferredSize ();
                
                if (pref.height > 400)
                {
                    
                    pref.height = 400;
                    
                } else {
                    
                    pref.height += 3;
                                
                }
                
                JScrollPane sp = UIUtils.createScrollPane (tree);
                                                                 
                sp.setBorder (UIUtils.createLineBorder ());
                sp.setPreferredSize (pref);
                
                builder.addLabel (Environment.replaceObjectNames ("{Comments}"),
                                  cc.xy (2,
                                         row));
                                                        
                builder.add (sp,
                             cc.xy (4,
                                    row));                                
                
                row += 2;

                JLabel sending = UIUtils.createLoadingLabel ("Sending {comments}...");
                sending.setVisible (true);
                        
                final Box sendBox = new Box (BoxLayout.X_AXIS);
                sendBox.setBorder (new EmptyBorder (10, 0, 0, 0));
                sendBox.add (sending);
                sendBox.setVisible (false);
                
                builder.add (sendBox,
                             cc.xy (4,
                                    row));                
                 
                row += 2;
                                                                
                ActionListener onShow = null;
        
                final JButton send = UIUtils.createButton ("Send",
                                                           null);
        
                ActionListener sendAction = new ActionListener ()
                {
                
                    public void actionPerformed (ActionEvent ev)
                    {
                             
                        // Get a count.
                        error.setVisible (false);

                        Set<NamedObject> selected = new LinkedHashSet ();
                        
                        UIUtils.getSelectedObjects (root,
                                                    selected);
                            
                        final Set<Note> comments = new LinkedHashSet ();
                            
                        for (NamedObject n : selected)
                        {
                            
                            if (n instanceof Note)
                            {
                                
                                comments.add ((Note) n);
                                
                            }
                            
                        }
                        
                        String err = null;
                                        
                        if (comments.size () == 0)
                        {
                            
                            error.setVisible (true);

                            popup.resize ();
                            
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
                                                                new ActionListener ()
                                                                {
                                                                    
                                                                    public void actionPerformed (ActionEvent ev)
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
                                                                            
                                                                            UIUtils.showErrorMessage (viewer,
                                                                                                      "Your comments have been sent but Quoll Writer is unable to update the comments in your local db, please contact Quoll Writer support for assistance.");
                                                                            
                                                                            return;
                                                                            
                                                                        }
                                                                        
                                                                        // Fire an event for each note.
                                                                        for (Note n : comments)
                                                                        {
                                                                            
                                                                            viewer.fireProjectEvent (n.getObjectType (),
                                                                                                     ProjectEvent.EDIT,
                                                                                                     n);
                                                                            
                                                                        }
                                                                        
                                                                        String title = "Your {comments} have been sent";
                                                                        String desc = "Your {comments} have been sent to <b>%s</b>";
                                                                        
                                                                        if (comments.size () == 1)
                                                                        {
                                                                            
                                                                            title = "Your {comment} has been sent";
                                                                            desc = "Your {comment} has been sent to <b>%s</b>";
                                                                            
                                                                        }
                                                                        
                                                                        UIUtils.showMessage ((PopupsSupported) viewer,
                                                                                             title,
                                                                                             String.format (desc,
                                                                                                            ed.getMainName ()));
                                                                                                                                                
                                                                        popup.removeFromParent ();

                                                                        if (onSend != null)
                                                                        {
                                                                            
                                                                            onSend.actionPerformed (new ActionEvent ("sent", 1, "sent"));
                                                                            
                                                                        }
                                                                        
                                                                    }
                                                                    
                                                                },
                                                                // On cancel of login.
                                                                popup.getCloseAction (),
                                                                null);
                        
                        
                        sendBox.setVisible (true);
                        
                        popup.resize ();
                                    
                    }
                    
                 };
                
                send.addActionListener (sendAction);
                UIUtils.addDoActionOnReturnPressed (genComments,
                                                    sendAction);
                
                final JButton cancel = UIUtils.createButton ("Cancel",
                                                             null);
        
                cancel.addActionListener (popup.getCloseAction ());
                
                JButton[] buts = new JButton[] { send, cancel };
                
                JComponent buttons = UIUtils.createButtonBar2 (buts,
                                                               Component.LEFT_ALIGNMENT); 
                buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
        
                builder.add (buttons,
                             cc.xy (4,
                                    row));        

                JPanel bp = builder.getPanel ();
                bp.setOpaque (false);
                bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
                
                content.add (bp);
                        
                //content.add (buttons);
                content.setBorder (UIUtils.createPadding (10, 10, 10, 10));
                                 
                content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                             content.getPreferredSize ().height));
        
                popup.setContent (content);
                                                                       
                viewer.showPopupAt (popup,
                                    UIUtils.getCenterShowPosition (viewer,
                                                                   popup),
                                    false);
                popup.setDraggable (viewer);
                
            }
            
        });        
        
    }
    
    public static void showNewProjectReceived (final NewProjectMessage mess)
    {
        
        UIUtils.doLater (new ActionListener ()
        {
        
            public void actionPerformed (ActionEvent ev)
            {

                final EditorEditor ed = mess.getEditor ();
            
                Box content = new Box (BoxLayout.Y_AXIS);
                
                final AbstractViewer viewer = Environment.getFocusedViewer ();
                
                JTextPane desc = UIUtils.createHelpTextPane (String.format ("<b>%s</b> has sent you the following {project} to edit.  Please respond below.",
                                                                            mess.getEditor ().getMainName ()),
                                                             viewer);        
                                    
                content.add (desc);
                desc.setBorder (null);
                desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                             desc.getPreferredSize ().height));                
                    
                content.add (Box.createVerticalStrut (10));

                String rows = "p, 6px, top:p, 6px, p, 6px, p";
                String cols = "right:p, 6px, fill:200px:grow";                
                
                FormLayout   fl = new FormLayout (cols,
                                                  rows);
                PanelBuilder b = new PanelBuilder (fl);
            
                Project _proj = null;
                
                try
                {
                    
                    _proj = mess.createProject ();
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to create project from message: " +
                                          mess,
                                          e);
                    
                    UIUtils.showErrorMessage (viewer,
                                              "Unable to send {project}, please contact Quoll Writer support for assistance.");
                    
                    return;                        
                    
                }
            
                final Project proj = _proj;
            
                ProjectVersion projVer = proj.getProjectVersion ();    
            
                CellConstraints cc = new CellConstraints ();        
                            
                int row = 1;
                                           
                b.addLabel (EditorsUIUtils.createFormLabel ("{Project}"),
                            cc.xy (1, row));
        
                b.addLabel (proj.getName (),
                            cc.xy (3, row));
        
                row += 2;

                b.addLabel (Environment.formatNumber (proj.getWordCount ()) + " words, " + Environment.replaceObjectNames (proj.getBook (0).getChapters ().size () + " {chapters}"),
                            cc.xy (3, row));

                row += 2;
                
                b.addLabel (EditorsUIUtils.createFormLabel ("Due by"),
                            cc.xy (1, row));
        
                b.addLabel ((projVer.getDueDate () != null ? Environment.formatDate (projVer.getDueDate ()) : "Not specified."),
                            cc.xy (3, row));

                row += 2;
        
                b.addLabel (EditorsUIUtils.createFormLabel ("Notes"),
                            cc.xy (1, row));
        
                ActionListener onShow = null;
        
                JComponent notes = null;
                
                StringWithMarkup verNotes = projVer.getDescription ();
                
                if (verNotes != null)
                {
                    
                    JComponent t = UIUtils.createHelpTextPane (verNotes,
                                                               viewer);
                    
                    t.setBorder (null);
                    t.setOpaque (false);

                    if (t.getPreferredSize ().height > 100)
                    {                    
                    
                        final JScrollPane sp = UIUtils.createScrollPane (t);
    
                        onShow = new ActionListener ()
                        {
                            
                            public void actionPerformed (ActionEvent ev)
                            {
    
                                sp.getVerticalScrollBar ().setValue (0);
                                
                            }
                            
                        };
                        
                        notes = sp;

                        notes.setPreferredSize (new Dimension (500,
                                                               100));
                        
                    } else {
                        
                        notes = t;
                        
                    }
                    
                    notes.setOpaque (false);
                    notes.setBorder (null);
                    
                } else {
                    
                    notes = new JLabel ("No notes provided.");
                    
                }
        
                b.add (notes,
                       cc.xy (3, row));

                row += 2;
                        
                JPanel p = b.getPanel ();
                p.setOpaque (false);
                p.setAlignmentX (Component.LEFT_ALIGNMENT);
                p.setBorder (new EmptyBorder (0, 5, 10, 5));
                                
                content.add (p);
                                                                
                Map<String, ActionListener> buttons = new LinkedHashMap ();
                
                buttons.put ("Yes, I'll edit the {project}",
                             new ActionListener ()
                             {
                                
                                public void actionPerformed (ActionEvent ev)
                                {
                                    
                                    //final String fromEmail = mess.getEditor ().getEmail ();
                                    
                                    // We may not have the editor here, this could be the first message from them.
                                    // If not then we accept the invite first.
                                    //EditorEditor ed = EditorsEnvironment.getEditorByEmail (fromEmail);
                                    
                                    if (ed.isPending ())
                                    {
                                                                                
                                        // Accepts, so update and then process the message.
                                        EditorsEnvironment.updateInvite (ed.getEmail (),
                                                                         Invite.Status.accepted,
                                        new ActionListener ()
                                        {
                                           
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                                
                                                ed.setName (mess.getEditorName ());
                                                ed.setAvatar (mess.getEditorAvatar ());
                                                
                                                try
                                                {
                                                
                                                    EditorsEnvironment.updateEditor (ed);
                                                    
                                                } catch (Exception e) {
                                                    
                                                    Environment.logError ("Unable to update editor: " +
                                                                          ed,
                                                                          e);
                    
                                                    return;                                                                    
                                                    
                                                }
                                               
                                                EditorsEnvironment.getMessageHandler ().subscribeToEditor (ed);
                                               
                                                try
                                                {
                                                
                                                    EditorsEnvironment.addMessage (mess);
                                                    
                                                } catch (Exception e) {
                                                    
                                                    Environment.logError ("Unable to save message for editor: " +
                                                                          ed,
                                                                          e);
                                                    
                                                    return;
                                                            
                                                }                                              
                                        
                                                EditorsUIUtils.handleNewProjectResponse (null,
                                                                                         mess,
                                                                                         true);
                                                
                                            }
                                            
                                        });
                                        
                                        return;
                                                
                                    } else {
                                    
                                        try
                                        {
                                        
                                            EditorsEnvironment.addMessage (mess);
                                            
                                        } catch (Exception e) {
                                            
                                            UIUtils.showErrorMessage (null,
                                                                      "Unable to save message from {editor}, please contact Quoll Writer support for assistance.");
                                            
                                            Environment.logError ("Unable to save message for editor: " +
                                                                  ed,
                                                                  e);
                                            
                                            return;
                                                    
                                        }                                              

                                        EditorsUIUtils.handleNewProjectResponse (null,
                                                                                 mess,
                                                                                 true);

                                    }
                                                                                 
                                }
                                
                             });
                
                buttons.put ("No thanks",
                             new ActionListener ()
                             {
                                
                                public void actionPerformed (ActionEvent ev)
                                {
                                                                        
                                    if (ed.isPending ())
                                    {
                                        
                                        // Accepts, so update and then process the message.
                                        EditorsEnvironment.updateInvite (ed.getEmail (),
                                                                         Invite.Status.rejected,
                                        new ActionListener ()
                                        {
                                           
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                                                                                
                                                EditorsUIUtils.handleNewProjectResponse (null,
                                                                                         mess,
                                                                                         false);
                                                
                                            }
                                            
                                        });
                                        
                                    } else {
                                        
                                        try
                                        {
                                        
                                            EditorsEnvironment.addMessage (mess);
                                            
                                        } catch (Exception e) {
                                            
                                            UIUtils.showErrorMessage (null,
                                                                      "Unable to save message from {editor}, please contact Quoll Writer support for assistance.");
                                            
                                            Environment.logError ("Unable to save message for editor: " +
                                                                  ed,
                                                                  e);
                                            
                                            return;
                                                    
                                        }                                              

                                        EditorsUIUtils.handleNewProjectResponse (null,
                                                                                 mess,
                                                                                 false);                                        
                                                                            
                                    }
                                    
                                }
                                
                             });
    
                buttons.put ("I'll decide later",
                             new ActionListener ()
                             {
                                
                                public void actionPerformed (ActionEvent ev)
                                {
                                                                        
                                    try
                                    {
                                    
                                        EditorsEnvironment.addMessage (mess);
                                        
                                    } catch (Exception e) {
                                        
                                        UIUtils.showErrorMessage (null,
                                                                  "Unable to save message from {editor}, please contact Quoll Writer support for assistance.");
                                        
                                        Environment.logError ("Unable to save message for editor: " +
                                                              ed,
                                                              e);
                                        
                                        return;
                                                
                                    }                                              
                                                                        
                                }
                                
                             });

                UIUtils.createQuestionPopup (viewer,
                                             "You've been sent a {project} to edit",
                                             "edit",
                                             content,
                                             buttons,
                                             null,
                                             null);
            
                if (onShow != null)
                {
                    
                    // And this is why I hate you Swing...
                    UIUtils.doLater (onShow);
                    
                }
            
            }
            
        });
        
    }

    public static void showProjectUpdateReceived (final UpdateProjectMessage mess)
    {
        
        UIUtils.doLater (new ActionListener ()
        {
        
            public void actionPerformed (ActionEvent ev)
            {

                ProjectInfo _proj = null;
                
                try
                {
                    
                    _proj = Environment.getProjectById (mess.getForProjectId (),
                                                        Project.EDITOR_PROJECT_TYPE);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to get project for id: " +
                                          mess.getForProjectId (),
                                          e);
                    
                    return;
                        
                }
            
                if (_proj == null)
                {
                    
                    return;
                    
                }
            
                final ProjectInfo proj = _proj;
            
                final EditorEditor ed = mess.getEditor ();
            
                final Box content = new Box (BoxLayout.Y_AXIS);
                
                final AbstractViewer viewer = Environment.getFocusedViewer ();
                
                JTextPane desc = UIUtils.createHelpTextPane (String.format ("<b>%s</b> has sent an update for the following {project}.",
                                                                            mess.getEditor ().getMainName ()),
                                                             viewer);        
                                    
                content.add (desc);
                desc.setBorder (null);
                desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                             desc.getPreferredSize ().height));                
                    
                content.add (Box.createVerticalStrut (10));

                String rows = "p, 6px, top:p, 6px, p, 6px, p";
                String cols = "right:p, 6px, fill:200px:grow";                
                
                FormLayout   fl = new FormLayout (cols,
                                                  rows);
                PanelBuilder b = new PanelBuilder (fl);
                            
                CellConstraints cc = new CellConstraints ();        
                            
                int row = 1;
                                           
                b.addLabel (EditorsUIUtils.createFormLabel ("{Project}"),
                            cc.xy (1, row));
        
                b.addLabel (proj.getName (),
                            cc.xy (3, row));
        
                row += 2;

                b.addLabel (Environment.replaceObjectNames (String.format ("%s words, %s {chapters}",
                                                                           Environment.formatNumber (proj.getChapterCount ()),
                                                                           Environment.formatNumber (proj.getWordCount ()))),
                            cc.xy (3, row));

                row += 2;
                
                b.addLabel (EditorsUIUtils.createFormLabel ("Due by"),
                            cc.xy (1, row));
        
                ProjectVersion projVer = mess.getProjectVersion ();
        
                b.addLabel ((projVer.getDueDate () != null ? Environment.formatDate (projVer.getDueDate ()) : "Not specified."),
                            cc.xy (3, row));

                row += 2;
        
                b.addLabel (EditorsUIUtils.createFormLabel ("Notes"),
                            cc.xy (1, row));
        
                ActionListener onShow = null;
        
                JComponent notes = null;
                
                StringWithMarkup verNotes = projVer.getDescription ();
                
                if (verNotes != null)
                {
                    
                    JComponent t = UIUtils.createHelpTextPane (verNotes,
                                                               viewer);
                    
                    t.setBorder (null);
                    t.setOpaque (false);

                    if (t.getPreferredSize ().height > 100)
                    {                    
                    
                        final JScrollPane sp = UIUtils.createScrollPane (t);
    
                        onShow = new ActionListener ()
                        {
                            
                            public void actionPerformed (ActionEvent ev)
                            {
    
                                sp.getVerticalScrollBar ().setValue (0);
                                
                            }
                            
                        };
                        
                        notes = sp;

                        notes.setPreferredSize (new Dimension (500,
                                                               100));
                        
                    } else {
                        
                        notes = t;
                        
                    }
                    
                    notes.setOpaque (false);
                    notes.setBorder (null);
                    
                } else {
                    
                    notes = new JLabel ("No notes provided.");
                    
                }
        
                b.add (notes,
                       cc.xy (3, row));

                row += 2;
                        
                JPanel p = b.getPanel ();
                p.setOpaque (false);
                p.setAlignmentX (Component.LEFT_ALIGNMENT);
                p.setBorder (new EmptyBorder (0, 5, 10, 5));
                                
                content.add (p);
                                                                
                Map<String, ActionListener> buttons = new LinkedHashMap ();
                
                buttons.put ("View the update",
                             new ActionListener ()
                             {
                                
                                public void actionPerformed (ActionEvent ev)
                                {
                                    
                                    EditorsUIUtils.showProjectUpdate (mess,
                                                                      viewer,
                                                                      null);
                                                                                 
                                }
                                
                             });
                
                buttons.put ("I'll check the update later",
                             new ActionListener ()
                             {
                                
                                public void actionPerformed (ActionEvent ev)
                                {
                                                                        
                                }
                                
                             });
    
                UIUtils.createQuestionPopup (viewer,
                                             "A {project} you're editing has been updated",
                                             "edit",
                                             content,
                                             buttons,
                                             null,
                                             null);
            
                if (onShow != null)
                {
                    
                    // And this is why I hate you Swing...
                    UIUtils.doLater (onShow);
                    
                }
            
            }
            
        });
        
    }

    public static void showInviteAcceptance (final EditorEditor ed)
    {
        
        UIUtils.doLater (new ActionListener ()
        {
        
            public void actionPerformed (ActionEvent ev)
            {
                               
                AbstractViewer viewer = Environment.getFocusedViewer ();
                               
                Box content = new Box (BoxLayout.Y_AXIS);

                String title = "Invitation accepted";                
                String message = "Your invite to <b>" + ed.getMainName () + "</b> to become {an editor} has been accepted.  You can now send messages and/or your {project}(s) to them.";
                
                JTextPane desc = UIUtils.createHelpTextPane (message,
                                                             viewer);        
                desc.setBorder (null);
                desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                             desc.getPreferredSize ().height));
                
                content.add (desc);
                content.add (Box.createVerticalStrut (10));
                                
                UIUtils.showMessage (viewer,
                                     title,
                                     content,
                                     Environment.getButtonLabel (Constants.CONFIRM_BUTTON_LABEL_ID),
                                     null);
                
            }
                                    
        });

    }
    
    public static void showInviteRejection (final EditorEditor ed)
    {
                
        UIUtils.doLater (new ActionListener ()
        {
        
            public void actionPerformed (ActionEvent ev)
            {
                               
                AbstractViewer viewer = Environment.getFocusedViewer ();

                Box content = new Box (BoxLayout.Y_AXIS);

                String title = "Invitation rejected";
                String message = "Your invitation to <b>" + ed.getMainName () + "</b> has been rejected.";
                
                JTextPane desc = UIUtils.createHelpTextPane (message,
                                                             viewer);        
                desc.setBorder (null);
                desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                             desc.getPreferredSize ().height));
                
                content.add (desc);
                content.add (Box.createVerticalStrut (10));

                UIUtils.showMessage (viewer,
                                     title,
                                     content,
                                     Environment.getButtonLabel (Constants.CONFIRM_BUTTON_LABEL_ID),
                                     null);
                
            }
                                    
        });

    }
            
    public static void showAcceptance (final EditorEditor ed)
    {
                
        UIUtils.doLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {
                                
                AbstractViewer viewer = Environment.getFocusedViewer ();
                
                if (viewer == null)
                {
                    
                    return;
                    
                }

                Box content = new Box (BoxLayout.Y_AXIS);
                
                String text = "<b>" + ed.getMainName () + "</b> has accepted your invitation.  You can now send messages and/or your project(s) to them.";
                
                if (!ed.isInvitedByMe ())
                {
                    
                    text = "Your acceptance of the invitation from <b>" + ed.getMainName () + "</b> has been acknowledged.  You can now send messages and/or your project(s) to them.";
                    
                }
                                                        
                UIUtils.showMessage ((PopupsSupported) viewer,
                                     "Your invites",
                                     text,
                                     Environment.getButtonLabel (Constants.CONFIRM_BUTTON_LABEL_ID),
                                     new ActionListener ()
                                     {
                                        
                                        public void actionPerformed (ActionEvent ev)
                                        {
                                            
                                            EditorsEnvironment.fireEditorChangedEvent (ed,
                                                                                       EditorChangedEvent.EDITOR_CHANGED);
                                                                                            
                                        }
                                        
                                     });

            }
                                    
        });
        
    }

    public static void showReportMessage (final MessageBox     mess,
                                          final AbstractViewer viewer)
    {

        URL url = null;
        
        try
        {
            
            url = EditorsEnvironment.getReportMessageURL ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to get report message url",
                                  e);
            
            UIUtils.showErrorMessage (viewer,
                                      "Unable to show report message popup, please contact Quoll Writer support for assistance.");
            
            return;
            
        }
    
        final URL reportURL = url;
    
        final QPopup qp = UIUtils.createClosablePopup ("Report a message",
                                                       Environment.getIcon (Constants.ERROR_ICON_NAME,
                                                                            Constants.ICON_POPUP),
                                                       null);
             
        Box content = new Box (BoxLayout.Y_AXIS);

        JTextPane desc = UIUtils.createHelpTextPane ("Please describe why you are reporting the message.<br /><br />Note: when reporting a message all information known about the message, such as the text received by you, will be sent to the Quoll Writer server.<br /><br /><p class='error'>Warning: falsely reporting messages can lead to you being banned from the Editors Service.</p>",
                                                     viewer);        
                            
        content.add (desc);
        desc.setBorder (null);
        desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     desc.getPreferredSize ().height));                

        content.add (Box.createVerticalStrut (10));
                                     
        content.add (UIUtils.createBoldSubHeader ("Message you are reporting",
                                                  null));
              
        BufferedImage im = null;
        
        try
        {
                        
            mess.setOpaque (true);
            mess.setBackground (UIUtils.getComponentColor ());
                        
            im = UIUtils.getImageOfComponent (mess,
                                              UIUtils.getPopupWidth () - 40,
                                              0);
            
            mess.setOpaque (false);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to create message box for: " +
                                  mess,
                                  e);
            
            UIUtils.showErrorMessage (viewer,
                                      "Unable to show report message popup, please contact Quoll Writer support for assistance.");
            
            return;
            
        }

        JLabel imL = new JLabel (new ImageIcon (im));

        imL.setBorder (new CompoundBorder (UIUtils.createPadding (5, 5, 0, 5),
                                           new CompoundBorder (new LineBorder (UIUtils.getInnerBorderColor (),
                                                                               1),//UIUtils.createLineBorder (),
                                                               UIUtils.createPadding (5, 5, 5, 5))));
        
        content.add (imL);
        
        content.add (Box.createVerticalStrut (10));

        content.add (UIUtils.createBoldSubHeader ("From {Editor}",
                                                  null));

        EditorInfoBox edB = null;
        
        try
        {
            
            edB = new EditorInfoBox (mess.getMessage ().getEditor (),
                                     viewer,
                                     false);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to create editor box for: " +
                                  mess.getMessage ().getEditor (),
                                  e);
            
            UIUtils.showErrorMessage (viewer,
                                      "Unable to show report message popup, please contact Quoll Writer support for assistance.");
            
            return;
            
        }
        
        edB.init ();
        
        edB.setBorder (UIUtils.createPadding (0, 5, 0, 5));
        
        content.add (edB);
        
        content.add (Box.createVerticalStrut (10));
        
        content.add (UIUtils.createBoldSubHeader ("Reason",
                                                  null));

        final JLabel error = UIUtils.createErrorLabel ("");
                  
        error.setBorder (UIUtils.createPadding (5, 5, 5, 5));
        error.setVisible (false);
                  
        content.add (error);
        
        final TextArea notes = new TextArea (Environment.replaceObjectNames ("Please describe why you are reporting the message, for example does it contain inappropriate language or suggestions?"),
                                             4,
                                             5000);
                                                  
        notes.setBorder (UIUtils.createPadding (0, 5, 0, 5));

        content.add (notes);
        
        final JLabel sending = UIUtils.createLoadingLabel ("Sending report...");
        sending.setVisible (false);        
        sending.setBorder (UIUtils.createPadding (10, 5, 0, 5));
        
        content.add (sending);
        
        final JButton reportB = UIUtils.createButton ("Report",
                                                      null);
        final JButton cancelB = UIUtils.createButton (Environment.getButtonLabel (Constants.CANCEL_BUTTON_LABEL_ID),
                                                      null);                
        
        reportB.addActionListener (new ActionListener ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                error.setVisible (false);
                
                notes.setAutoGrabFocus (false);
                
                if (notes.getText ().trim ().length () == 0)
                {

                    error.setText ("Please provide a reason for the report.");
                    error.setVisible (true);
                    
                    qp.resize ();
                    
                    return;
                    
                }
                
                sending.setVisible (true);
                qp.resize ();
                
                EditorsEnvironment.goOnline ("To report a message you must first login.",
                                             new ActionListener ()
                                             {
                                                
                                                public void actionPerformed (ActionEvent ev)
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
                                                                         new ActionListener ()
                                                                         {
                                                                            
                                                                            public void actionPerformed (ActionEvent ev)
                                                                            {
                                                                                
                                                                                qp.removeFromParent ();
                                                                                
                                                                                UIUtils.showMessage ((PopupsSupported) viewer,
                                                                                                     "Message reported",
                                                                                                     "The message has been reported to Quoll Writer support.  Please note that you may be contacted to get other information about the message, for example if you have received an inappropriate image or text in a {chapter}.");
                                                                                
                                                                            }
                                                                            
                                                                         },
                                                                         new ActionListener ()
                                                                         {
                                                                            
                                                                            public void actionPerformed (ActionEvent ev)
                                                                            {
    
                                                                                qp.removeFromParent ();
                                                                                
                                                                                Environment.logError ("Unable to report message:" +
                                                                                                      ev);
                                                                                
                                                                                UIUtils.showErrorMessage (viewer,
                                                                                                          "Unable to report the message, please contact Quoll Writer support for assistance.");
                                                                                
                                                                            }
                                                                            
                                                                         },
                                                                         new ActionListener ()
                                                                         {
                                                                            
                                                                            public void actionPerformed (ActionEvent ev)
                                                                            {
                                                                                
                                                                                qp.removeFromParent ();
                                                                                
                                                                                Environment.logError ("Unable to report message, got fail",
                                                                                                      (Exception) ev.getSource ());
                                                                                
                                                                                UIUtils.showErrorMessage (viewer,
                                                                                                          "Unable to report the message, please contact Quoll Writer support for assistance.");
                                                                                
                                                                            }
                                                                            
                                                                         });
                                                        
                                                    } catch (Exception e) {
                                                                                                                
                                                        qp.removeFromParent ();
                                                        
                                                        Environment.logError ("Unable to report message",
                                                                              e);
                                                        
                                                        UIUtils.showErrorMessage (viewer,
                                                                                  "Unable to report the message, please contact Quoll Writer support for assistance.");

                                                    }
                                                    
                                                }
                                                
                                             },
                                             null,
                                             null);
                                
            }
            
        });
        
        cancelB.addActionListener (qp.getCloseAction ());
                
        JComponent bs = UIUtils.createButtonBar2 (new JButton[] {reportB, cancelB},
                                                  Component.LEFT_ALIGNMENT);
        
        bs.setBorder (UIUtils.createPadding (5, 5, 0, 5));
        
        content.add (bs);
        
        content.setBorder (UIUtils.createPadding (10, 10, 10, 10));
        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                 content.getPreferredSize ().height));
        
        qp.setContent (content);        
        
        qp.setDraggable (viewer);
                          
        qp.resize ();
        
        viewer.showPopupAt (qp,
                            UIUtils.getCenterShowPosition (viewer,
                                                           qp),
                            false);
        
    }
    
    public static void showRegister (final AbstractViewer viewer)
                              throws Exception
    {
        
        final QPopup popup = UIUtils.createWizardPopup ("Register for the Editors service (beta)",
                                                  Constants.EDITORS_ICON_NAME,
                                                  null,
                                                  new EditorRegister (viewer));

        popup.setDraggable (viewer);
                          
        popup.resize ();
        
        viewer.showPopupAt (popup,
                            UIUtils.getCenterShowPosition (viewer,
                                                           popup),
                            false);
        
    }    
    
    public static void hideLogin ()
    {
        
        EditorsUIUtils.editorLogin.removeFromParent ();
        
    }

    public static void showChangePassword (final AbstractViewer viewer)
    {
        
        final QPopup qp = UIUtils.createClosablePopup ("Change your password",
                                                       Environment.getIcon (Constants.EDIT_ICON_NAME,
                                                                            Constants.ICON_POPUP),
                                                       null);
    
        Box content = new Box (BoxLayout.Y_AXIS);
        
        JTextPane desc = UIUtils.createHelpTextPane ("Enter your new password below.",
                                                     viewer);        
                            
        content.add (desc);
        desc.setBorder (null);
        desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     desc.getPreferredSize ().height));                
                  
        final JLabel error = UIUtils.createErrorLabel ("");
                  
        error.setBorder (new EmptyBorder (10, 0, 0, 0));
        error.setVisible (false);
                  
        content.add (error);
                  
        FormLayout fl = new FormLayout ("6px, right:p, 6px, fill:200px:grow",
                                        "10px, p, 6px, top:p, 10px, p");

        fl.setHonorsVisibility (true);
        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int row = 2;
                
        builder.addLabel ("New Password",
                          cc.xy (2,
                                 row));
    
        final JPasswordField pass1 = new JPasswordField ();
                
        builder.add (pass1,
                     cc.xy (4,
                            row));

        row += 2;

        builder.addLabel ("Confirm Password",
                          cc.xy (2,
                                 row));

        final JPasswordField pass2 = new JPasswordField ();
                
        builder.add (pass2,
                     cc.xy (4,
                            row));

        final JButton updateB = UIUtils.createButton (Environment.getButtonLabel (Constants.UPDATE_BUTTON_LABEL_ID),
                                                      null);
        final JButton cancelB = UIUtils.createButton (Environment.getButtonLabel (Constants.CANCEL_BUTTON_LABEL_ID),
                                                      null);                
        
        updateB.addActionListener (new ActionListener ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                error.setVisible (false);
                
                // Update the user data.
                String pwd = new String (pass1.getPassword ());
                
                if (pwd.length () == 0)
                {
                    
                    error.setText ("Please provide a new password.");
                    error.setVisible (true);
                    
                    qp.resize ();
                    
                    return;
                    
                }

                String pwd2 = new String (pass2.getPassword ());
                                
                if (pwd2.length () == 0)
                {
                    
                    error.setText ("Please confirm your new password.");
                    error.setVisible (true);
                    
                    qp.resize ();
                    
                    return;
                    
                }

                if (!pwd.equals (pwd2))
                {
                    
                    error.setText ("The passwords do not match.");
                    error.setVisible (true);
                    
                    qp.resize ();
                    
                    return;
                    
                }

                EditorsEnvironment.updateUserPassword (pwd);
                
                qp.removeFromParent ();
                
            }
            
        });
        
        cancelB.addActionListener (qp.getCloseAction ());
        
        row += 2;        
        
        JComponent bs = UIUtils.createButtonBar2 (new JButton[] {updateB, cancelB},
                                                  Component.LEFT_ALIGNMENT);

        builder.add (bs,
                     cc.xy (4,
                            row));

        JPanel p = builder.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        content.add (p);
                                                       
        content.setBorder (UIUtils.createPadding (10, 10, 10, 10));
        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                 content.getPreferredSize ().height));

        qp.setContent (content);
        
        viewer.showPopupAt (qp,
                            UIUtils.getCenterShowPosition (viewer,
                                                           qp),
                            false);
        
        qp.setDraggable (viewer);
        qp.resize ();
                
    }

    public static void showLogin (final AbstractViewer viewer,
                                  final String         loginReason,
                                  final ActionListener onLogin,
                                  final ActionListener onCancel)
    {
        
        UIUtils.doActionLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
        
                EditorsUIUtils.editorLogin.setLoginReason (loginReason);
                EditorsUIUtils.editorLogin.setOnLogin (onLogin);
                
                EditorsUIUtils.editorLogin.setOnCancel (onCancel);
                EditorsUIUtils.editorLogin.setOnClose (onCancel);
                
                EditorsUIUtils.editorLogin.show (viewer);
                
            }
            
        });
                
    }
        
    public static void showLoginError (EditorsWebServiceResult res)
    {
        
        String reason = "Please contact Quoll Writer support for assistance, quoting error code: " + res.getReturnCode () + (res.getReturnCode () == 401 ? ("/" + res.getErrorType ()) : "");
        
        if (res.getReturnCode () == 401)
        {
                            
            String errType = res.getErrorType ();
            
            if (errType != null)
            {
            
                if (errType.equals ("InvalidCredentials"))
                {
                    
                    reason = "Please check your email/password and try again.";
                    
                }
                
            }
                            
        }
        
        EditorsUIUtils.showLoginError ("Unable to login to the Editors service.<br />" + reason);
        
    }

    public static void showLoginError (String                error)
    {

        EditorsUIUtils.showLoginError (error,
                                       null,
                                       null);
    
    }
    
    public static void showLoginError (String                error,
                                       ActionListener        onLogin,
                                       ActionListener        onCancel)
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
        AbstractViewer viewer = Environment.getFocusedViewer ();

        EditorsUIUtils.editorLogin.show (viewer);
        
        EditorsUIUtils.editorLogin.showError (error);

        if (onLogin != null)
        {
            
            EditorsUIUtils.editorLogin.setOnLogin (onLogin);
            
        }
        
        if (onCancel != null)
        {
            
            EditorsUIUtils.editorLogin.setOnCancel (onCancel);
            EditorsUIUtils.editorLogin.setOnClose (onCancel);

        }
        
        EditorsUIUtils.editorLogin.resize ();
        
    }
    
    public static void showInviteEditor (final AbstractViewer viewer)
    {
        
        UIUtils.doActionLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
        
                UIUtils.createTextInputPopup (viewer,
                                              "Send an invite", //Invite {an editor}",
                                              Constants.NEW_ICON_NAME,
                                              "Enter the email address of the person to invite.",
                                              "Invite",
                                              null,
                                              null,
                                              new ValueValidator<String> ()
                                              {
                                                
                                                  public String isValid (String v)
                                                  {
                                                    
                                                      if ((v == null)
                                                          ||
                                                          (v.trim ().equals (""))
                                                         )
                                                      {
                                                        
                                                          return "The email address must be specified.";
                                                        
                                                      }
                                                    
                                                      if (v.indexOf ("@") < 0)
                                                      {
                                                        
                                                          return "Please provide a valid email address.";
                                                        
                                                      }
                                                    
                                                      if (v.equals (EditorsEnvironment.getUserAccount ().getEmail ()))
                                                      {
                                                        
                                                          return "Inviting yourself?  O_o";
                                                        
                                                      }
                                                    
                                                      EditorEditor ed = EditorsEnvironment.getEditorByEmail (v);
                                                    
                                                      // Check to see if we already have the editor.
                                                      if (ed != null)
                                                      {
                                                        
                                                          String other = "You have already invited <b>%s (%s)</b>.";
                                                        
                                                          if (ed.getEditorStatus () == EditorEditor.EditorStatus.rejected)
                                                          {
                                                            
                                                              other = "You have already invited: <b>%s (%s)</b>.  Your invitation was rejected.";
                                                            
                                                          }
                                                        
                                                          if (ed.isPrevious ())
                                                          {
                                                            
                                                              other = "<b>%s (%s)</b> is a previous {contact}.";
                                                            
                                                          }
                                                        
                                                          return String.format (other,
                                                                                ed.getShortName (),
                                                                                ed.getEmail ());
                                                        
                                                      }
                                                    
                                                      return null;
                                                    
                                                  }
                                                
                                              },
                                              new ActionListener ()
                                              {
                                                
                                                  public void actionPerformed (final ActionEvent ev)
                                                  {
                                                  
                                                        // Send the invite.                                                
                                                        EditorsEnvironment.sendInvite (ev.getActionCommand ());
        
                                                  }
                                                
                                              },
                                              null,
                                              null);        

            }
            
        });
        
    }
    
    public static void showNewEditorChatMessageNotification (final EditorChatMessage mess)
    {

        UIUtils.doLater (new ActionListener ()
        {
        
            public void actionPerformed (ActionEvent ev)
            {
                
                final EditorEditor ed = mess.getEditor ();
                
                String title = "New chat message";

                AbstractViewer viewer = Environment.getFocusedViewer ();
                
                Map<String, ActionListener> buttons = new LinkedHashMap ();
                
                buttons.put ("Reply",
                             new ActionListener ()
                             {
                                
                                @Override
                                public void actionPerformed (ActionEvent ev)
                                {
                                    
                                    AbstractViewer viewer = Environment.getFocusedViewer ();

                                    try
                                    {

                                        viewer.sendMessageToEditor (ed);
                                        
                                    } catch (Exception e) {
                                        
                                        Environment.logError ("Unable to show editors side bar for editor: " +
                                                              ed,
                                                              e);
                                        
                                        UIUtils.showErrorMessage (viewer,
                                                                  "Unable to display {editors} side bar, please contact Quoll Writer support for assistance.");
                                        
                                    }
                                    
                                }
                                
                             });
                
                buttons.put ("Close",
                             new ActionListener ()
                             {
                                
                                @Override
                                public void actionPerformed (ActionEvent ev)
                                {
                                    
                                }
                                
                             });
                
                Box content = new Box (BoxLayout.Y_AXIS);
                                
                JTextPane desc = UIUtils.createHelpTextPane (String.format ("Message received from <b>%s</b>.",
                                                                            ed.getMainName ()),
                                                             viewer);        
                                    
                content.add (desc);
                desc.setBorder (null);
                desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                             desc.getPreferredSize ().height));                
                    
                content.add (Box.createVerticalStrut (10));

                ChatMessageBox mb = new ChatMessageBox (mess,
                                                        viewer);
                
                mb.setShowAttentionBorder (false);
                
                try
                {
                
                    mb.init ();
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to init chat message box for message: " +
                                          mess,
                                          e);
                    
                    return;
                    
                }                
                
                mess.setDealtWith (true);
                
                try
                {
                    
                    EditorsEnvironment.updateMessage (mess);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to update message: " +
                                          mess,
                                          e);
                    
                }
                                
                content.add (mb);
                
                UIUtils.createQuestionPopup (viewer,
                                             "New chat message",
                                             Constants.MESSAGE_ICON_NAME,
                                             content,
                                             buttons,
                                             null,
                                             null);
            
                try
                {
                    
                    EditorsEnvironment.updateMessage (mess);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to update message: " +
                                          mess,
                                          e);
                    
                }
            
            }
            
        });
        
    }
    
    public static void showInviteFromEditor (final String                from,
                                             final ActionListener        onAccept,
                                             final ActionListener        onReject)
    {
        
        UIUtils.doActionLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
        
                AbstractViewer viewer = Environment.getFocusedViewer ();        
        
                UIUtils.createQuestionPopup (viewer,
                                             "Invitation to be {an editor}",
                                             "invite",
                                             "You have been invited to be {an editor} for:<br /><br /><b>" + from + "</b><br /><br />Would you like to accept this invitation?",
                                             "Yes, accept",
                                             "No, decline",
                                             onAccept,
                                             onReject,
                                             null,
                                             null);
                
            }
            
        });

    }
    
    public static void showResultError (final EditorsWebServiceResult res)
    {
        
        AbstractViewer viewer = Environment.getFocusedViewer ();
        
        UIUtils.showErrorMessage (viewer,
                                  res.getErrorMessage ());
        
    }
    
    public static void showCommentPopup (final Note      note,
                                         final Component showAt)
    {
        
    }
 
    /**
     * Create a tree + model that is suitable for viewing a set of chapter.
     * It adds a listener that will open the associated object.
     */
    public static JTree createViewTree (final Set<Chapter>          chapters,
                                        final AbstractProjectViewer viewer)
    {
        
        final JTree tree = UIUtils.createTree ();

        tree.setCellRenderer (new ProjectTreeCellRenderer (true)
        {
           
            @Override
            public String getIconType (DataObject d,
                                       DefaultMutableTreeNode par)
            {
                
                if (d instanceof Note)
                {
                    
                    return Constants.COMMENT_ICON_NAME;
                    
                }
                
                return super.getIconType (d,
                                          par);
                
            }
            
        });
        
        tree.setEditable (false);
        tree.setToolTipText ("Click to view");
        tree.addMouseListener (new MouseEventHandler ()
        {
        
            @Override
            public void handlePress (MouseEvent ev)
            {
                
                // Edit the chapter.
                TreePath tp = tree.getPathForLocation (ev.getX (),
                                                       ev.getY ());

                if (tp == null)
                {

                    return;

                }

                Object d = ((DefaultMutableTreeNode) tp.getLastPathComponent ()).getUserObject ();

                if (d instanceof TreeParentNode)
                {
                    
                    if (tree.isCollapsed (tp))
                    {
                        
                        tree.expandPath (tp);
                        
                    } else {
                        
                        tree.collapsePath (tp);
                        
                    }
                    
                    return;
                    
                }
                
                if ((ev.getClickCount () == 1)
                    &&
                    (!ev.isPopupTrigger ())
                   )
                {

                    // Check to see if the key is null, if so then this a "fake" object
                    // and we need to get the real one.
                    DataObject obj = (DataObject) d;
                    
                    if (obj.getKey () == null)
                    {
                        
                        DataObject oobj = viewer.getProject ().getObjectById (obj.getClass (),
                                                                              obj.getId ());
                                                      
                        if (oobj == null)
                        {
                                
                            // The object isn't in this project.                 
                            Chapter c = null;
                                                       
                            // If a note, just show a standard popup with the message.
                            if (obj instanceof Note)
                            {
                                
                                obj = ((Note) obj);
                                
                                c = ((Note) obj).getChapter ();
                                
                            }
                            
                            if (obj instanceof Chapter)
                            {
                                
                                obj = ((Chapter) obj);
                                
                                c = ((Chapter) obj);
                                                                
                            }

                            final DataObject dobj = obj;
                            
                            if (c.getBook () == null)
                            {
                                
                                return;
                                                        
                            }
                            
                            String projId = c.getBook ().getProject ().getId ();

                            ProjectInfo p = null;
                            
                            try
                            {
                                
                                p = Environment.getProjectById (projId,
                                                                Project.EDITOR_PROJECT_TYPE);
                                
                            } catch (Exception e) {
                                
                                Environment.logError ("Unable to get project for id: " +
                                                      projId,
                                                      e);
                                
                                UIUtils.showErrorMessage (viewer,
                                                          "Unable to open project.");
                                
                                return;
                                
                            }
                            
                            if (p != null)
                            {
                                
                                // We potentially get the project twice here but that's ok.
                                try
                                {
                                
                                    Environment.openObjectInProject (p,
                                                                     c);                                

                                } catch (Exception e) {
                                    
                                    Environment.logError ("Unable to open project: " +
                                                          p,
                                                          e);
                                    
                                    UIUtils.showErrorMessage (viewer,
                                                              "Unable to open {project}.");
                                    
                                    return;
                                    
                                }
                                
                                return;
                                
                            }
                            
                            // If this is a chapter and the project no longer exists then
                            // output a message to that effect.
                            
                            // If this is a note and the project no longer exists, output
                            // a message with the note but also saying the project doesn't exist.
                            
                            return;
                                                
                        } else {
                            
                            viewer.viewObject (oobj);
                            
                        }
                        
                    }
                
                    return;
                
                }
                                
            }
                
        });
        
        Project p = new Project ();
        p.setName ("___bogus");
        
        DefaultMutableTreeNode root = UIUtils.createTreeNode (p,
                                                              null,
                                                              null,
                                                              false);

        for (Chapter c : chapters)
        {

            DefaultMutableTreeNode cnode = EditorsUIUtils.createTree (c,
                                                                      null,
                                                                      null,
                                                                      false);

            if (cnode == null)
            {

                continue;

            }

            root.add (cnode);
            
        }
    
        ((DefaultTreeModel) tree.getModel ()).setRoot (root);
        
        return tree;
                
    }
 
    public static DefaultMutableTreeNode createTree (Chapter    c,
                                                     Collection exclude,
                                                     Collection init,
                                                     boolean    selectable)

    {
        
        DefaultMutableTreeNode root = UIUtils.createTreeNode (c,
                                                              exclude,
                                                              init,
                                                              selectable);

        if (root == null)
        {
            
            return null;
                                                              
        }
        
        for (Note n : c.getNotes ())
        {

            DefaultMutableTreeNode node = UIUtils.createTreeNode (n,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

            if (node == null)
            {

                continue;

            }

            root.add (node);

        }
        
        return root;
        
    }
 
    public static DefaultMutableTreeNode createTree (Project    p,
                                                     Collection exclude,
                                                     Collection init,
                                                     boolean    selectable)

    {
        
        DefaultMutableTreeNode root = UIUtils.createTreeNode (p,
                                                              exclude,
                                                              init,
                                                              selectable);

        if (p.getBooks ().size () == 1)
        {

            Book b = (Book) p.getBooks ().get (0);

            // Get the chapters.
            List<Chapter> chaps = b.getChapters ();

            for (Chapter c : chaps)
            {

                DefaultMutableTreeNode node = EditorsUIUtils.createTree (c,
                                                                         exclude,
                                                                         init,
                                                                         selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }

        }
            
        return root;                
        
    }
    
    public static void showProjectComments (final ProjectCommentsMessage message,
                                            final AbstractViewer         parentViewer,
                                            final ActionListener         onShow)
    {

        // Load up the project with the specific text.
        // See if we have a project viewer for the project.
        ProjectInfo proj = null;
        
        try
        {
            
            proj = Environment.getProjectById (message.getForProjectId (),
                                               Project.NORMAL_PROJECT_TYPE);
        
        } catch (Exception e) {
            
            Environment.logError ("Unable to get project for: " +
                                  message.getForProjectId (),
                                  e);
            
            UIUtils.showErrorMessage (parentViewer,
                                      "Unable to show {comments}, please contact Quoll Writer support for assistance.");

            return;                        
            
        }        
        
        if (proj == null)
        {
            
            Environment.logError ("No project for: " +
                                  message.getForProjectId ());
            
            UIUtils.showErrorMessage (parentViewer,
                                      "Unable to show {comments}, please contact Quoll Writer support for assistance.");

            return;                                    
            
        }
        
        final ProjectInfo _proj = proj;
        
        ActionListener open = new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                String pwd = ev.getActionCommand ();
                
                if (pwd.equals (""))
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
                    
                    UIUtils.showErrorMessage (parentViewer,
                                              "Unable to show {comments}, please contact Quoll Writer support for assistance.");
                    
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
                    
                    UIUtils.showErrorMessage (parentViewer,
                                              "Unable to show {comments}, please contact Quoll Writer support for assistance.");
                    
                    return;
                    
                }
                
                if (pv == null)
                {
                    
                    Environment.logError ("Unable to find project version: " +
                                          message.getProjectVersion ().getId () +
                                          " for project: " +
                                          _proj);
                    
                    UIUtils.showErrorMessage (parentViewer,
                                              "Unable to show {comments}, please contact Quoll Writer support for assistance.");
                    
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
                    
                    Map<String, Chapter> kchaps = new HashMap ();
                    
                    for (Chapter c : chaps)
                    {
                        
                        b.addChapter (c);
                        
                        kchaps.put (c.getId (),
                                    c);
                        
                    }
                    
                    for (Note n : message.getComments ())
                    {
                        
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
                    
                    pcv.init ();
                    
                    pcv.showViewer ();                    
                    
                    if (onShow != null)
                    {
                        
                        onShow.actionPerformed (new ActionEvent (pcv, 1, "show"));
                                                            
                    }
                                        
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to view comments for project: " +
                                          _proj,
                                          e);
                    
                    UIUtils.showErrorMessage (parentViewer,
                                              "Unable to show {comments}, please contact Quoll Writer support for assistance.");
                    
                    return;
                    
                }
                
            }
            
        };

        UIUtils.openProjectAndDoAction (proj,
                                        open,
                                        parentViewer);
        
    }
 
    public static void showProjectUpdate (final UpdateProjectMessage message,
                                          final AbstractViewer       parentViewer,
                                          final ActionListener       onShow)
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
            
            UIUtils.showErrorMessage (parentViewer,
                                      "Unable to show {project} update, please contact Quoll Writer support for assistance.");

            return;                        
            
        }
        
        final ProjectInfo _proj = proj;
        
        if (proj == null)
        {
            
            UIUtils.showMessage (parentViewer,
                                 "The {project} for this update no longer exists.");
            
            return;
            
        }
        
        ActionListener open = new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                String pwd = ev.getActionCommand ();
                
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
                    
                    UIUtils.showErrorMessage (parentViewer,
                                              "Unable to update {project}, please contact Quoll Writer support for assistance.");
                    
                    return;
                    
                }
                
                try
                {

                    Environment.openProject (_proj);
                    
                    AbstractProjectViewer pv = Environment.getProjectViewer (_proj);
                    
                    if (!(pv instanceof EditorProjectViewer))
                    {
                        
                        Environment.logError ("Unable to open project at version: " +
                                              message.getProjectVersion () +
                                              ", project: " +
                                              _proj);
                        
                        UIUtils.showErrorMessage (parentViewer,
                                                  "Unable to view updated {project}, please contact Quoll Writer support for assistance.");
                        
                        return;                        
                        
                    }
                    
                    EditorProjectViewer epv = (EditorProjectViewer) pv;
                    
                    epv.switchToProjectVersion (message.getProjectVersion ());
                                        
                    if (onShow != null)
                    {
                        
                        onShow.actionPerformed (new ActionEvent (_proj, 1, "show"));
                                                            
                    }
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to for project: " +
                                          _proj,
                                          e);
                    
                    UIUtils.showErrorMessage (parentViewer,
                                              "Unable to show {comments}, please contact Quoll Writer support for assistance.");
                    
                    return;
                    
                }
                
            }
            
        };

        UIUtils.openProjectAndDoAction (proj,
                                        open,
                                        parentViewer);
                
    }

    public static String createFormLabel (String l)
    {
        
        return Environment.replaceObjectNames ("<html><i>Due by</i></html>");
        
    }
 
    public static JComponent getProjectVersionPanel (final ProjectVersion        pv,
                                                     final AbstractProjectViewer viewer)
    {
        
        if (pv == null)
        {
            
            return null;
            
        }
        
        String rows = "";

        String ver = pv.getName ();
        final String genComments = (pv.getDescription () != null ? pv.getDescription ().getText () : null);
        Date due = pv.getDueDate ();
        
        if (ver != null)
        {
            
            rows += "p";
            
        }
        
        if (due != null)
        {
            
            if (rows.length () > 0)
            {
                
                rows += ", 6px, ";
                
            }
            
            rows += "p";
                
        }

        if (genComments != null)
        {
            
            if (rows.length () > 0)
            {
                
                rows += ", 6px, ";
                
            }

            rows += "top:p";
                
        }
        
        FormLayout fl = new FormLayout ("right:p, 6px, fill:100px:grow",
                                        rows);

        fl.setHonorsVisibility (true);
        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int row = 1;
             
        if (ver != null)
        {
            
            builder.addLabel (Environment.replaceObjectNames ("<html><i>{Version}</i></html>"),
                              cc.xy (1,
                                     row));
            
            String latest = "";
            
            if (pv.isLatest ())
            {
                
                latest = " (latest)";
                
            }
            
            builder.addLabel (String.format ("<html>%s%s</html>",
                                             ver,
                                             latest),
                              cc.xy (3,
                                     row));        

            row += 2;
                
        }
        
        if (due != null)
        {                
        
            builder.addLabel (Environment.replaceObjectNames ("<html><i>{Due}</i></html>"),
                              cc.xy (1,
                                     row));
            
            builder.addLabel (String.format ("<html>%s</html>",
                                             Environment.formatDate (due)),
                              cc.xy (3,
                                     row));        

            row += 2;
                                     
        }
                
        if (genComments != null)
        {
        
            builder.addLabel (Environment.replaceObjectNames ("<html><i>{Notes}</i></html>"),
                              cc.xy (1,
                                     row));
    
            String commText = genComments;
                                     
            TextIterator ti = new TextIterator (commText);
            
            if (ti.getSentenceCount () > 1)
            {
                
                commText = ti.getFirstSentence ().getText ();
            
                commText += "<br /><a href='#'>More, click to view all.</a>";
                                     
            }
            
            JComponent mess = UIUtils.createHelpTextPane (commText,
                                                          viewer);
            
            mess.addMouseListener (new MouseEventHandler ()
            {
               
                public void handlePress (MouseEvent ev)
                {
    
                    UIUtils.showMessage ((PopupsSupported) viewer,
                                         "Notes",
                                         genComments);
                    
                }
                
            });
            
            mess.setBorder (null);
            
            builder.add (mess,
                         cc.xy (3,
                                row));

        }
                                 
        JPanel bp = builder.getPanel ();
        
        bp.setAlignmentX (Component.LEFT_ALIGNMENT);
        bp.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                          bp.getPreferredSize ().height));
        
        return bp;        
        
    }
 
    public static void showMessagesInPopup (String             title,
                                            String             iconType,
                                            String             help,
                                            Set<EditorMessage> messages,
                                            boolean            showAttentionBorder,
                                            AbstractViewer     viewer,
                                            Component          showAt)
    {

        final QPopup qp = UIUtils.createClosablePopup (title,
                                                       Environment.getIcon (iconType,
                                                                            Constants.ICON_POPUP),
                                                       null);
    
        Box content = new Box (BoxLayout.Y_AXIS);
        
        JTextPane desc = UIUtils.createHelpTextPane (help + "<br /><br />Messages with a red border require an acknowledgement or action from you.",
                                                     viewer);        
                            
        content.add (desc);
        desc.setBorder (null);
        desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     desc.getPreferredSize ().height));                
    
        content.add (Box.createVerticalStrut (5));
    
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
        
        Box b = new ScrollableBox (BoxLayout.Y_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        Box lastB = null;
        
        for (EditorMessage m : messages)
        {
            
            MessageBox mb = null;
            
            try
            {
            
                mb = MessageBoxFactory.getMessageBoxInstance (m,
                                                              viewer);
                mb.setShowAttentionBorder (true);//showAttentionBorder);
    
                mb.init ();
    
            } catch (Exception e) {
                
                Environment.logError ("Unable to get message box for message: " +
                                      m,
                                      e);
                                
            }
            
            mb.setAlignmentX (Component.LEFT_ALIGNMENT);
            
            Box wb = new Box (BoxLayout.Y_AXIS);
            wb.setAlignmentX (Component.LEFT_ALIGNMENT);
            
            wb.setBorder (UIUtils.createBottomLineWithPadding (5, 0, 10, 0));
            wb.add (mb);
            
            b.add (wb);
            
            lastB = wb;
            
        }

        if (lastB != null)
        {
        
            //lastB.setBorder (UIUtils.createPadding (5, 0, 0, 0));
            
        }
                
        final JScrollPane sp = UIUtils.createScrollPane (b);
        
        if (b.getPreferredSize ().height < 350)
        {
            
            sp.setPreferredSize (new Dimension (500, b.getPreferredSize ().height + 1));
            
        } else {
            
            sp.setPreferredSize (new Dimension (500, 350));            
            
        }
        
        sp.setBorder (null);
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.setBorder (new EmptyBorder (1, 0, 0, 0));
        
        sp.getVerticalScrollBar ().addAdjustmentListener (new AdjustmentListener ()
        {
           
            public void adjustmentValueChanged (AdjustmentEvent ev)
            {
                
                if (sp.getVerticalScrollBar ().getValue () > 0)
                {
                
                    sp.setBorder (new MatteBorder (1, 0, 0, 0,
                                                   UIUtils.getInnerBorderColor ()));

                } else {
                    
                    sp.setBorder (new EmptyBorder (1, 0, 0, 0));
                    
                }
                    
            }
            
        });        

        content.add (sp);
                                                       
        content.setBorder (new EmptyBorder (10, 10, 10, 10));

        JButton finish = new JButton ("Close");

        finish.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                qp.removeFromParent ();

            }

        });

        JButton[] buts = new JButton[] { finish };

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.CENTER_ALIGNMENT); 
        bp.setOpaque (false);

        bp.setAlignmentX (Component.LEFT_ALIGNMENT);
        bp.setBorder (UIUtils.createPadding (10, 0, 0, 0));
        
        content.add (bp);                                                 
                                                 
        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                 content.getPreferredSize ().height));

        qp.setContent (content);
        
        if (showAt != null)
        {
        
            viewer.showPopupAt (qp,
                                showAt,
                                false);

        } else {
            
            viewer.showPopupAt (qp,
                                UIUtils.getCenterShowPosition (viewer,
                                                               qp),
                                false);            
            
        }
        
        qp.setDraggable (viewer);
        qp.resize ();
                        
        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                sp.getVerticalScrollBar ().setValue (0);        
                
            }
            
        });
        
    }
    
    public static void showAllMessagesForEditor (EditorEditor   ed,
                                                 AbstractViewer viewer,
                                                 Component      showAt)
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
        
        EditorsUIUtils.showMessagesInPopup ("All messages",
                                            Constants.ERROR_ICON_NAME,
                                            String.format ("All messages you've sent to and/or received from <b>%s</b>.",
                                                           ed.getShortName ()),
                                            messages,
                                            false,
                                            viewer,
                                            showAt);
        
    }    

    public static void showImportantMessagesForEditor (EditorEditor   ed,
                                                       AbstractViewer viewer,
                                                       Component      showAt)
    {
                
        // Get undealt with messages that are not chat.
        // If there is just one then show it, otherwise show a link that will display a popup of them.
        Set<EditorMessage> undealtWith = ed.getMessages (EditorsUIUtils.importantMessageFilter);
        
        EditorsUIUtils.showMessagesInPopup ("Important messages",
                                            Constants.ERROR_ICON_NAME,
                                            String.format ("New and important messages from <b>%s</b> that require your attention.",
                                                           ed.getShortName ()),
                                            undealtWith,
                                            false,
                                            viewer,
                                            showAt);
        
    }    
    
    public static void showProjectMessagesForEditor (EditorEditor          ed,
                                                     AbstractProjectViewer viewer,
                                                     Component             showAt)
    {
        
        Set<EditorMessage> messages = ed.getMessages (new DefaultEditorMessageFilter (viewer.getProject (),
                                                                                      NewProjectMessage.MESSAGE_TYPE,
                                                                                      UpdateProjectMessage.MESSAGE_TYPE,
                                                                                      NewProjectResponseMessage.MESSAGE_TYPE,
                                                                                      ProjectEditStopMessage.MESSAGE_TYPE));
        
        ProjectEditor pe = viewer.getProject ().getProjectEditor (ed);
                
        EditorsUIUtils.showMessagesInPopup ("{Project} updates sent/received",
                                            Project.OBJECT_TYPE,
                                            String.format ("All {project} updates you have sent to or received from <b>%s</b> for {project} <b>%s</b>.  The latest update is shown first.",
                                                           ed.getShortName (),
                                                           viewer.getProject ().getName ()),
                                            messages,
                                            true,
                                            viewer,
                                            showAt);

    }
    
    public static void showAllCommentsForEditor (EditorEditor          ed,
                                                 AbstractProjectViewer viewer,
                                                 Component             showAt)
    {
        
        Set<EditorMessage> comments = ed.getMessages (new DefaultEditorMessageFilter (viewer.getProject (),
                                                                                      ProjectCommentsMessage.MESSAGE_TYPE));
            
        if (comments.size () == 0)
        {
            
            UIUtils.showMessage ((PopupsSupported) viewer,
                                 "No comments sent/received",
                                 "No comments have been sent or received yet.");

            return;
                
        }
        
        boolean sentByMe = comments.iterator ().next ().isSentByMe ();
        
        String suffix = (sentByMe ? "sent" : "received");
        String suffix2 = (sentByMe ? "sent to" : "received from");
                                                
        EditorsUIUtils.showMessagesInPopup (String.format ("{Comments} %s",
                                                suffix),
                                 Constants.COMMENT_ICON_NAME,
                                 String.format ("All {comments} you have %s <b>%s</b> for {project} <b>%s</b>.  The latest {comments} are shown first.",
                                                suffix2,
                                                ed.getShortName (),
                                                viewer.getProject ().getName ()),                                 
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
            
            UIUtils.showErrorMessage (viewer,
                                      String.format ("Unable to show {projects} you are editing for %s.",
                                                     editor.getShortName ()));            
            
            return;
            
        }
        
        final QPopup qp = UIUtils.createClosablePopup ("{Projects} you are editing",
                                                       Environment.getIcon (Constants.EDIT_ICON_NAME,
                                                                            Constants.ICON_POPUP),
                                                       null);
    
        Box content = new Box (BoxLayout.Y_AXIS);
        
        JTextPane desc = UIUtils.createHelpTextPane (String.format ("All {projects} you are editing for <b>%s</b>.",
                                                                    editor.getShortName ()),
                                                     viewer);        
                            
        content.add (desc);
        desc.setBorder (null);
        desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     desc.getPreferredSize ().height));                
    
        content.add (Box.createVerticalStrut (5));
            
        Box b = new ScrollableBox (BoxLayout.Y_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        Box lastB = null;
        
        for (ProjectInfo p : projs)
        {
            
            Box pb = new Box (BoxLayout.Y_AXIS);
            pb.setAlignmentX (Component.LEFT_ALIGNMENT);
            
            JComponent h = UIUtils.createBoldSubHeader (p.getName (),
                                                        null);
            pb.add (h);
            
            // TODO: Finish this.
            
            b.add (pb);
            
            lastB = pb;
                        
        }

        if (lastB != null)
        {
        
            lastB.setBorder (UIUtils.createPadding (0, 0, 0, 0));
            
        }
                
        final JScrollPane sp = UIUtils.createScrollPane (b);
        
        if (b.getPreferredSize ().height < 350)
        {
            
            sp.setPreferredSize (new Dimension (500, b.getPreferredSize ().height + 1));
            
        } else {
            
            sp.setPreferredSize (new Dimension (500, 350));            
            
        }
        
        sp.setBorder (null);
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.setBorder (new EmptyBorder (1, 0, 0, 0));
        
        sp.getVerticalScrollBar ().addAdjustmentListener (new AdjustmentListener ()
        {
           
            public void adjustmentValueChanged (AdjustmentEvent ev)
            {
                
                if (sp.getVerticalScrollBar ().getValue () > 0)
                {
                
                    sp.setBorder (new MatteBorder (1, 0, 0, 0,
                                                   UIUtils.getInnerBorderColor ()));

                } else {
                    
                    sp.setBorder (new EmptyBorder (1, 0, 0, 0));
                    
                }
                    
            }
            
        });        

        content.add (sp);
                                                       
        content.setBorder (new EmptyBorder (10, 10, 10, 10));

        JButton finish = new JButton ("Close");

        finish.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                qp.removeFromParent ();

            }

        });

        JButton[] buts = new JButton[] { finish };

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.CENTER_ALIGNMENT); 
        bp.setOpaque (false);

        bp.setAlignmentX (Component.LEFT_ALIGNMENT);
        bp.setBorder (UIUtils.createPadding (10, 0, 0, 0));
        
        content.add (bp);                                                 
                                                 
        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                 content.getPreferredSize ().height));

        qp.setContent (content);
        
        viewer.showPopupAt (qp,
                            UIUtils.getCenterShowPosition (viewer,
                                                           qp),
                            false);
        
        qp.setDraggable (viewer);
        qp.resize ();
                        
        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                sp.getVerticalScrollBar ().setValue (0);        
                
            }
            
        });
        
    }
    
    public static void showProjectsEditorIsEditingForUser (EditorEditor   editor,
                                                           AbstractViewer viewer)
    {
        
        final Set<ProjectInfo> projs = new LinkedHashSet ();
        
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
                
        final QPopup qp = UIUtils.createClosablePopup ("{Projects} being edited",
                                                       Environment.getIcon (Constants.EDIT_ICON_NAME,
                                                                            Constants.ICON_POPUP),
                                                       null);
    
        Box content = new Box (BoxLayout.Y_AXIS);
        
        JTextPane desc = UIUtils.createHelpTextPane (String.format ("All {projects} <b>%s</b> is editing for you.",
                                                                    editor.getShortName ()),
                                                     viewer);        
                            
        content.add (desc);
        desc.setBorder (null);
        desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     desc.getPreferredSize ().height));                
    
        content.add (Box.createVerticalStrut (5));
            
        Box b = new ScrollableBox (BoxLayout.Y_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        Box lastB = null;
        
        for (ProjectInfo p : projs)
        {
                        
        }

        if (lastB != null)
        {
        
            lastB.setBorder (UIUtils.createPadding (0, 0, 0, 0));
            
        }
                
        final JScrollPane sp = UIUtils.createScrollPane (b);
        
        if (b.getPreferredSize ().height < 350)
        {
            
            sp.setPreferredSize (new Dimension (500, b.getPreferredSize ().height + 1));
            
        } else {
            
            sp.setPreferredSize (new Dimension (500, 350));            
            
        }
        
        sp.setBorder (null);
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.setBorder (new EmptyBorder (1, 0, 0, 0));
        
        sp.getVerticalScrollBar ().addAdjustmentListener (new AdjustmentListener ()
        {
           
            public void adjustmentValueChanged (AdjustmentEvent ev)
            {
                
                if (sp.getVerticalScrollBar ().getValue () > 0)
                {
                
                    sp.setBorder (new MatteBorder (1, 0, 0, 0,
                                                   UIUtils.getInnerBorderColor ()));

                } else {
                    
                    sp.setBorder (new EmptyBorder (1, 0, 0, 0));
                    
                }
                    
            }
            
        });        

        content.add (sp);
                                                       
        content.setBorder (new EmptyBorder (10, 10, 10, 10));

        JButton finish = new JButton ("Close");

        finish.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                qp.removeFromParent ();

            }

        });

        JButton[] buts = new JButton[] { finish };

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.CENTER_ALIGNMENT); 
        bp.setOpaque (false);

        bp.setAlignmentX (Component.LEFT_ALIGNMENT);
        bp.setBorder (UIUtils.createPadding (10, 0, 0, 0));
        
        content.add (bp);                                                 
                                                 
        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                 content.getPreferredSize ().height));

        qp.setContent (content);
        
        viewer.showPopupAt (qp,
                            UIUtils.getCenterShowPosition (viewer,
                                                           qp),
                            false);
        
        qp.setDraggable (viewer);
        qp.resize ();
                        
        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                sp.getVerticalScrollBar ().setValue (0);        
                
            }
            
        });
        
    }

}