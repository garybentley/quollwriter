package com.quollwriter.editors.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.*;
import java.awt.image.*;

import java.util.Set;
import java.util.Iterator;

import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.ui.*;
import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.events.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.components.ActionAdapter;

public class EditorInfoBox extends Box implements EditorChangedListener
{
    
    private EditorEditor editor = null;
    private AbstractProjectViewer projectViewer = null;
    private JLabel avatar = null;
    private JLabel mainName = null;
    private JLabel onlineStatus = null;
    private JLabel other = null;
    private Box details = null;
    
    public EditorInfoBox (EditorEditor          ed,
                          AbstractProjectViewer viewer)
    {
        
        super (BoxLayout.X_AXIS);
        
        // We add ourselves as a listener for editor change events however we don't ever
        // remove ourselves since, as a standard component, we don't have a fixed lifecycle.
        EditorsEnvironment.addEditorChangedListener (this);
        
        this.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.projectViewer = viewer;
        this.editor = ed;
                
        this.setOpaque (false);
                
        this.avatar = new JLabel ();
                
        this.avatar.setAlignmentY (Component.TOP_ALIGNMENT);
        
        this.add (this.avatar);
        this.avatar.setOpaque (false);
        
        this.avatar.setBorder (new CompoundBorder (UIUtils.createPadding (0, 0, 0, 5),
                                                   UIUtils.createLineBorder ()));
        
        final boolean pending = ed.isPending ();
        
        this.details = new Box (BoxLayout.Y_AXIS);
        details.setAlignmentY (Component.TOP_ALIGNMENT);

        JLabel l = UIUtils.createInformationLabel (null);
        l.setFont (l.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (12)).deriveFont (Font.PLAIN));
        l.setAlignmentY (Component.TOP_ALIGNMENT);
        l.setVerticalAlignment (SwingConstants.TOP);
        details.add (l);
        this.mainName = l;
                
        l = UIUtils.createInformationLabel (null);
        l.setVisible (false);
        l.setAlignmentY (Component.TOP_ALIGNMENT);
        l.setVerticalAlignment (SwingConstants.TOP);
        UIUtils.setPadding (l, 5, 3, 0, 5);
        details.add (l);
        
        this.onlineStatus = l;
                      
        l = UIUtils.createInformationLabel (null);
        l.setVisible (false);
        l.setFont (l.getFont ().deriveFont (java.awt.Font.ITALIC));
        UIUtils.setPadding (l, 5, 3, 0, 5);
        details.add (l);        
                        
        this.other = l;
                        
        this.add (details);

        final EditorInfoBox _this = this;
        /*
        this.addMouseListener (new MouseEventHandler ()
        {
            
            public void handlePress (MouseEvent ev)
            {
                
                boolean isEditorProject = _this.projectViewer.getProject ().isEditorProject ();
                
                if (ev.isPopupTrigger ())
                {
                                        
                    // Show the menu.

                    JPopupMenu m = new JPopupMenu ();

                    JMenuItem mi = null;
                        
                    if (Environment.isDebugModeEnabled ())
                    {
                        
                        mi = UIUtils.createMenuItem ("Delete all messages [Debug option]",
                                                     Constants.DELETE_ICON_NAME,
                                                     new ActionListener ()
                                                     {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            UIUtils.createTextInputPopup (_this.projectViewer,
                                                                                          "Delete all messages?",
                                                                                          Constants.DELETE_ICON_NAME,
                                                                                          String.format ("To delete all messages from <b>%s</b> please enter <b>Yes</b> in the box below.",
                                                                                                         _this.editor.getMainName ()),
                                                                                          "Yes, delete them",
                                                                                          Constants.CANCEL_BUTTON_LABEL_ID,
                                                                                          null,
                                                                                          UIUtils.getYesValueValidator (),
                                                                                          new ActionListener ()
                                                                                          {
                                                                                            
                                                                                              public void actionPerformed (ActionEvent ev)
                                                                                              {
                                                                                                
                                                                                                if (!_this.editor.messagesLoaded ())
                                                                                                {
                                                                                                    
                                                                                                    try
                                                                                                    {
                                                                                                    
                                                                                                        EditorsEnvironment.loadMessagesForEditor (_this.editor);
                                                                                                        
                                                                                                    } catch (Exception e) {
                                                                                                        
                                                                                                        Environment.logError ("Unable to load messages for editor: " +
                                                                                                                              _this.editor,
                                                                                                                              e);
                                                                                                        
                                                                                                        UIUtils.showErrorMessage (_this.projectViewer,
                                                                                                                                  "Unable to load messages for editor.");
                                                                                                        
                                                                                                        return;
                                                                                                        
                                                                                                    }
                                                                                                    
                                                                                                }
                                                                                                    
                                                                                                try
                                                                                                {
                                                                                                
                                                                                                    EditorsEnvironment.deleteMessages (_this.editor.getMessages ());
                                                                                                    
                                                                                                } catch (Exception e) {
                                                                                                                                                                                                            
                                                                                                    Environment.logError ("Unable to delete messages for editor: " +
                                                                                                                          _this.editor,
                                                                                                                          e);
                                                                                                    
                                                                                                    UIUtils.showErrorMessage (_this.projectViewer,
                                                                                                                              "Unable to delete messages for editor.");
                                                                                                    
                                                                                                    return;
                                                                                                    
                                                                                                }
                                                                                                
                                                                                                _this.editor.setMessages (null);

                                                                                                UIUtils.showMessage ((PopupsSupported) _this.projectViewer,
                                                                                                                     "All messages deleted",
                                                                                                                     String.format ("All messages (sent and received) for <b>%s</b> have been deleted.",
                                                                                                                                    _this.editor.getMainName ()));
                                                                                                
                                                                                              }
                                                                                            
                                                                                          },
                                                                                          null,
                                                                                          null);
                                                                                                                    
                                                        }
                                                        
                                                     });
    
                        m.add (mi);                            
                        
                    }
                    
                    if (!pending)
                    {                        
                    
                        mi = UIUtils.createMenuItem ("Send message",
                                                     Constants.MESSAGE_ICON_NAME,
                                                     new ActionListener ()
                                                     {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            try
                                                            {
                                                            
                                                                _this.projectViewer.sendMessageToEditor (_this.editor);
                                                                
                                                            } catch (Exception e) {
                                                                
                                                                UIUtils.showErrorMessage (_this,
                                                                                          "Unable to show {editor}.");
                                                                
                                                                Environment.logError ("Unable to show editor: " +
                                                                                      _this.editor,
                                                                                      e);                                                            
                                                                
                                                            }
                                                            
                                                        }
                                                        
                                                     });
    
                        m.add (mi);    

                    }
    
                    if ((!pending)
                        &&
                        (!isEditorProject)
                       )
                    {
    
                        mi = UIUtils.createMenuItem ("Show all {comments}",
                                                     Constants.FIND_ICON_NAME,
                                                     new ActionListener ()
                                                     {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            //EditorsUIUtils.
                                                            
                                                        }
                                                        
                                                     });
    
                        m.add (mi);

                        mi = UIUtils.createMenuItem ("Send {project}/{chapters}",
                                                     Constants.SEND_ICON_NAME,
                                                     new ActionListener ()
                                                     {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            EditorsUIUtils.showSendProject (_this.projectViewer,
                                                                                            _this.editor,
                                                                                            null);
                                                            
                                                        }
                                                        
                                                     });
                        
                        m.add (mi);

                    }

                    if (!pending)
                    {
                    
                        mi = UIUtils.createMenuItem ("Update the {editor} information",
                                                     Constants.EDIT_ICON_NAME,
                                                     new ActionListener ()
                                                     {
                                                                
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                                        
                                                            EditorsUIUtils.updateEditorInfo (_this.projectViewer,
                                                                                             _this.editor);
                                                                        
                                                        }
                                                                
                                                     },
                                                     null,
                                                     null);
    
                        m.add (mi);

                    }
                    
                    if (!isEditorProject)
                    {
                    
                        mi = UIUtils.createMenuItem ("Remove {editor}",
                                                     Constants.DELETE_ICON_NAME,
                                                     new ActionListener ()
                                                     {
                                                       
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                           
                                                            EditorsUIUtils.showRemoveEditor (_this.projectViewer,
                                                                                             _this.editor,
                                                                                             null);
                                                           
                                                        }
                                                       
                                                    });
    
                        m.add (mi);

                    }
                        
                    Component c = (Component) ev.getSource ();
    
                    m.show (c,
                            ev.getX (),
                            ev.getY ());
                                
                    return;
                    
                }
                                
            }
            
        });
        */
        this.update ();
                
    }
        
    public void editorChanged (EditorChangedEvent ev)
    {
        
        if (ev.getEditor () == this.editor)
        {
            
            this.update ();
            
        }
        
    }
    
    private void update ()
    {
        
        this.onlineStatus.setVisible (false);
        this.other.setVisible (false);
        
        this.mainName.setText (this.editor.getMainName ());
        
        BufferedImage bi = null;
        
        if (this.editor.getMainAvatar () != null)
        {
        
            bi = UIUtils.getScaledImage (this.editor.getMainAvatar (),
                                         50);
            
        } else {
            
            bi = Environment.getNoEditorAvatarImage ();
                
        }
        
        this.avatar.setIcon (new ImageIcon (bi));
        
        
        if (this.editor.getOnlineStatus () != null)
        {
            
            String type = Constants.ONLINE_STATUS_ICON_NAME_PREFIX + this.editor.getOnlineStatus ().getType ();
        
            this.onlineStatus.setIcon (Environment.getIcon (type,
                                                            Constants.ICON_MENU_INNER));
            this.onlineStatus.setToolTipText (this.editor.getOnlineStatus ().getName ());
            this.onlineStatus.setText (this.editor.getOnlineStatus ().getName ());
            this.onlineStatus.setVisible (true);

        }
        
        if (this.editor.isPending ())
        {

            this.other.setText ((this.editor.isInvitedByMe () ? "Invited: " : "Received: ") + Environment.formatDate (this.editor.getDateCreated ()));

            this.other.setVisible (true);
            
            final EditorInfoBox _this = this;
            
            if (!this.editor.isInvitedByMe ())
            {
                
                // See if they have sent a new project message as the invite.
                try
                {
                
                    EditorsEnvironment.loadMessagesForEditor (this.editor);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to load messages for editor: " +
                                          this.editor,
                                          e);
                    
                    return;
                    
                }
                
                // Show an accept/reject.
                EditorMessage m = null;
                
                if ((this.editor.getMessages () != null)
                    &&
                    (this.editor.getMessages ().size () > 0)
                   )
                {
                    
                    m = this.editor.getMessages ().iterator ().next ();
                    
                }
                
                if ((m == null)
                    ||
                    (m instanceof InviteMessage)
                   )
                {                                
                
                    final InviteMessage im = (InviteMessage) m;
                
                    JButton accept = UIUtils.createButton (Environment.getIcon (Constants.ACCEPTED_ICON_NAME,
                                                                                Constants.ICON_EDITOR_MESSAGE),
                                                           "Click to accept the invitation",
                                                           new ActionListener ()
                                                           {
                                                                
                                                                public void actionPerformed (ActionEvent ev)
                                                                {

                                                                    InviteResponseMessage rm = new InviteResponseMessage (true,
                                                                                                                          EditorsEnvironment.getUserAccount ());
                                                                    rm.setEditor (editor);
                                                                    rm.setDealtWith (true);
                                                                                        /*                                                
                                                                    EditorsEnvironment.sendMessageToEditor (rm,
                                                                                                            null,
                                                                                                            null,
                                                                                                            null);
                                                                    */
                                                                    EditorsEnvironment.acceptInvite (_this.editor,
                                                                                                     rm,
                                                                                                     null);

                                                                    im.setDealtWith (true);
                                                                    
                                                                    try
                                                                    {
                                                                        
                                                                        EditorsEnvironment.updateMessage (im);
                                                                        
                                                                    } catch (Exception e) {
                                                                        
                                                                        Environment.logError ("Unable to update invite message to be dealt with: " +
                                                                                              im,
                                                                                              e);
                                                                        
                                                                    }
                                                                    
                                                                }
                                                                
                                                           });
                    
                    JButton reject = UIUtils.createButton (Environment.getIcon (Constants.REJECTED_ICON_NAME,
                                                                                Constants.ICON_EDITOR_MESSAGE),
                                                           "Click to reject the invitation",
                                                           new ActionListener ()
                                                           {
                                                                
                                                                public void actionPerformed (ActionEvent ev)
                                                                {
                                                                           
                                                                    InviteResponseMessage rm = new InviteResponseMessage (false,
                                                                                                                          EditorsEnvironment.getUserAccount ());
                                                                    rm.setEditor (editor);
                                                                    rm.setDealtWith (true);
                                                                    
                                                                    EditorsEnvironment.sendMessageToEditor (rm,
                                                                                                            null,
                                                                                                            null,
                                                                                                            null);

                                                                    EditorsEnvironment.rejectInvite (_this.editor,
                                                                                                     rm,
                                                                                                     null);

                                                                    im.setDealtWith (true);
                                                                    
                                                                    try
                                                                    {
                                                                        
                                                                        EditorsEnvironment.updateMessage (im);
                                                                        
                                                                    } catch (Exception e) {
                                                                        
                                                                        Environment.logError ("Unable to update invite message to be dealt with: " +
                                                                                              im,
                                                                                              e);
                                                                        
                                                                    }
                                                                    
                                                                }
                                                                
                                                           });
        
                    java.util.List<JButton> buts = new java.util.ArrayList ();
                    buts.add (accept);
                    buts.add (reject);
                                    
                    JComponent bb = UIUtils.createButtonBar (buts);
                                
                    bb.setAlignmentX (Component.LEFT_ALIGNMENT);
                    bb.setAlignmentY (Component.CENTER_ALIGNMENT);

                    this.details.add (bb);
                    
                }
                
                if ((m != null)
                    &&
                    (m instanceof NewProjectMessage)
                   )
                {
                
                    // Show the project details.
                    final NewProjectMessage mess = (NewProjectMessage) m;
                    
                    JComponent p = NewProjectMessageBox.getNewProjectMessageDetails (mess,
                                                                                     null);

                    p.setBorder (UIUtils.createPadding (5, 10, 5, 5));
                    
                    this.details.add (p);
                                                                                     
                    JButton accept = UIUtils.createButton (Environment.getIcon (Constants.ACCEPTED_ICON_NAME,
                                                                                Constants.ICON_EDITOR_MESSAGE),
                                                           "Click to accept the invitation",
                                                           new ActionListener ()
                                                           {
                                                                
                                                                public void actionPerformed (ActionEvent ev)
                                                                {

                                                                    EditorsUIUtils.handleNewProjectResponse (null,
                                                                                                             mess,
                                                                                                             true);
                                                                    
                                                                }
                                                                
                                                           });
                    
                    JButton reject = UIUtils.createButton (Environment.getIcon (Constants.REJECTED_ICON_NAME,
                                                                                Constants.ICON_EDITOR_MESSAGE),
                                                           "Click to reject the invitation",
                                                           new ActionListener ()
                                                           {
                                                                
                                                                public void actionPerformed (ActionEvent ev)
                                                                {
                                                                                                                                                      
                                                                    EditorsUIUtils.handleNewProjectResponse (null,
                                                                                                             mess,
                                                                                                             false);
                                                                    
                                                                }
                                                                
                                                           });
        
                    java.util.List<JButton> buts = new java.util.ArrayList ();
                    buts.add (accept);
                    buts.add (reject);
                                    
                    JComponent bb = UIUtils.createButtonBar (buts);
                                
                    bb.setAlignmentX (Component.LEFT_ALIGNMENT);
                    bb.setAlignmentY (Component.CENTER_ALIGNMENT);

                    this.details.add (bb);
                                
                }
                
            }
            
        } else {
        
            // Not convinced about this, may be better to store a reference?
            ProjectEditor pe = null;
            
            try
            {
            
                pe = EditorsEnvironment.getProjectEditor (this.projectViewer.getProject (),
                                                          this.editor);
    
            } catch (Exception e) {
            
                Environment.logError ("Unable to get project editor for editor: " +
                                      this.editor +
                                      " and project: " +
                                      this.projectViewer.getProject (),
                                      e);
                
            }
            
            if (pe != null)
            {
                
                this.other.setVisible (true);
                this.other.setText (Environment.replaceObjectNames (pe.getStatusMessage ()));
                        
            }
            
        }
        
    }
        
    public EditorInfoBox init ()
    {
        
        this.update ();
        
        return this;
        
    }

    public void addDeleteAllMessagesMenuItem (JPopupMenu menu)
    {
        
        final EditorInfoBox _this = this;
        
        JMenuItem mi = null;
            
        if (Environment.isDebugModeEnabled ())
        {
            
            menu.add (UIUtils.createMenuItem ("Delete all messages [Debug option]",
                                         Constants.DELETE_ICON_NAME,
                                         new ActionListener ()
                                         {
                                            
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                                
                                                UIUtils.createTextInputPopup (_this.projectViewer,
                                                                              "Delete all messages?",
                                                                              Constants.DELETE_ICON_NAME,
                                                                              String.format ("To delete all messages from <b>%s</b> please enter <b>Yes</b> in the box below.",
                                                                                             _this.editor.getMainName ()),
                                                                              "Yes, delete them",
                                                                              Constants.CANCEL_BUTTON_LABEL_ID,
                                                                              null,
                                                                              UIUtils.getYesValueValidator (),
                                                                              new ActionListener ()
                                                                              {
                                                                                
                                                                                  public void actionPerformed (ActionEvent ev)
                                                                                  {
                                                                                    
                                                                                    if (!_this.editor.messagesLoaded ())
                                                                                    {
                                                                                        
                                                                                        try
                                                                                        {
                                                                                        
                                                                                            EditorsEnvironment.loadMessagesForEditor (_this.editor);
                                                                                            
                                                                                        } catch (Exception e) {
                                                                                            
                                                                                            Environment.logError ("Unable to load messages for editor: " +
                                                                                                                  _this.editor,
                                                                                                                  e);
                                                                                            
                                                                                            UIUtils.showErrorMessage (_this.projectViewer,
                                                                                                                      "Unable to load messages for editor.");
                                                                                            
                                                                                            return;
                                                                                            
                                                                                        }
                                                                                        
                                                                                    }
                                                                                        
                                                                                    try
                                                                                    {
                                                                                    
                                                                                        EditorsEnvironment.deleteMessages (_this.editor.getMessages ());
                                                                                        
                                                                                    } catch (Exception e) {
                                                                                                                                                                                                
                                                                                        Environment.logError ("Unable to delete messages for editor: " +
                                                                                                              _this.editor,
                                                                                                              e);
                                                                                        
                                                                                        UIUtils.showErrorMessage (_this.projectViewer,
                                                                                                                  "Unable to delete messages for editor.");
                                                                                        
                                                                                        return;
                                                                                        
                                                                                    }
                                                                                    
                                                                                    _this.editor.setMessages (null);

                                                                                    UIUtils.showMessage ((PopupsSupported) _this.projectViewer,
                                                                                                         "All messages deleted",
                                                                                                         String.format ("All messages (sent and received) for <b>%s</b> have been deleted.",
                                                                                                                        _this.editor.getMainName ()));
                                                                                    
                                                                                  }
                                                                                
                                                                              },
                                                                              null,
                                                                              null);
                                                                                                        
                                            }
                                            
                                         }));
            
        }        
        
    }
    
    public void addSendMessageMenuItem (JPopupMenu menu)
    {
        
        final EditorInfoBox _this = this;        
        
        if (this.editor.isPrevious ())
        {
            
            return;
            
        }
        
        final boolean pending = this.editor.isPending ();        
        
        if (!pending)
        {                        
        
            menu.add (UIUtils.createMenuItem ("Send message",
                                                Constants.MESSAGE_ICON_NAME,
                                                new ActionListener ()
                                                {
                                                   
                                                   public void actionPerformed (ActionEvent ev)
                                                   {
                                                       
                                                       try
                                                       {
                                                       
                                                           _this.projectViewer.sendMessageToEditor (_this.editor);
                                                           
                                                       } catch (Exception e) {
                                                           
                                                           UIUtils.showErrorMessage (_this,
                                                                                     "Unable to show {editor}.");
                                                           
                                                           Environment.logError ("Unable to show editor: " +
                                                                                 _this.editor,
                                                                                 e);                                                            
                                                           
                                                       }
                                                       
                                                   }
                                                   
                                                }));

        }
                
    }
    
    public void addShowCommentsMenuItem (JPopupMenu menu)
    {

        final EditorInfoBox _this = this;                
        
        final boolean pending = this.editor.isPending ();        

        boolean isEditorProject = this.projectViewer.getProject ().isEditorProject ();
        
        if ((!pending)
            &&
            (!isEditorProject)
           )
        {

            final Set<EditorMessage> messages = this.editor.getMessages (new EditorMessageFilter ()
            {
                
                public boolean accept (EditorMessage m)
                {
                    
                    return ((m.getMessageType ().equals (ProjectCommentsMessage.MESSAGE_TYPE))
                            &&
                            (_this.projectViewer.getProject ().getId ().equals (m.getForProjectId ())));
                
                }
                
            });
        
            Iterator<EditorMessage> iter = messages.iterator ();
        
            if (messages.size () > 0)
            {
                
                menu.addSeparator ();                
                
                final ProjectCommentsMessage message = (ProjectCommentsMessage) messages.iterator ().next ();
                
                menu.add (UIUtils.createMenuItem (String.format ("View last {comments} (%s) received",
                                                                 message.getComments ().size ()),
                                                  Constants.FIND_ICON_NAME,
                                                  new ActionListener ()
                                                  {
                                                                                                
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                                                                                                                            
                                                            EditorsUIUtils.showProjectComments (message,
                                                                                                _this.projectViewer,
                                                                                                null);

                                                        }
                                                
                                                  }));

            }
            
            if (messages.size () > 1)
            {
                                
                JMenu mm = new JMenu (Environment.replaceObjectNames (String.format ("Older {Comments} (%s sets)",
                                                                                     messages.size () -1)));
                
                menu.add (mm);
                
                while (iter.hasNext ())
                {
                
                    final ProjectCommentsMessage mess = (ProjectCommentsMessage) iter.next ();
                
                    mm.add (UIUtils.createMenuItem (String.format ("%s - %s {comments}",
                                                                   Environment.formatDate (mess.getWhen ()),
                                                                   mess.getComments ().size ()),
                                                    null,
                                                    new ActionListener ()
                                                    {
                                                  
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                      
                                                            EditorsUIUtils.showProjectComments (mess,
                                                                                                _this.projectViewer,
                                                                                                null);
                                                      
                                                        }
                                                  
                                                    }));
                    
                }

            }
            
        }        
        
    }
    
    public void addSendOrUpdateProjectMenuItem (JPopupMenu menu)
    {
        
        final EditorInfoBox _this = this;        
        
        if (this.editor.isPrevious ())
        {
            
            return;
            
        }
        
        final boolean pending = this.editor.isPending ();        

        boolean isEditorProject = this.projectViewer.getProject ().isEditorProject ();
        
        if ((!pending)
            &&
            (!isEditorProject)
           )
        {

            if (!this.editor.messagesLoaded ())
            {
                
                try
                {
                
                    EditorsEnvironment.loadMessagesForEditor (_this.editor);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to load messages for editor: " +
                                          _this.editor,
                                          e);
                    
                    UIUtils.showErrorMessage (_this.projectViewer,
                                              "Unable to load messages for editor.");
                    
                    return;
                    
                }
                
            }
        
            NewProjectMessage npm = (NewProjectMessage) this.editor.getMessage (NewProjectMessage.MESSAGE_TYPE,
                                                                                this.projectViewer.getProject ());
        
            ProjectEditStopMessage psm = (ProjectEditStopMessage) this.editor.getMessage (ProjectEditStopMessage.MESSAGE_TYPE,
                                                                                          this.projectViewer.getProject ());

            if ((npm != null)
                &&
                (psm == null)
               )
            {

                if (npm.isAccepted ())
                {
            
                    menu.add (UIUtils.createMenuItem ("Update {project}/{chapters}",
                                                      Constants.SEND_ICON_NAME,
                                                      new ActionListener ()
                                                      {
                                                    
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            EditorsUIUtils.showUpdateProject (_this.projectViewer,
                                                                                              _this.editor,
                                                                                              null);
                                                            
                                                        }
                                                        
                                                      }));

                }
                
            } else {
                
                menu.add (UIUtils.createMenuItem ("Send {project}/{chapters}",
                                                  Constants.SEND_ICON_NAME,
                                                  new ActionListener ()
                                                  {
                                                
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        EditorsUIUtils.showSendProject (_this.projectViewer,
                                                                                        _this.editor,
                                                                                        null);

                                                    }
                                                    
                                                  }));
                
            }

        }
        
    }
    
    public void addUpdateEditorInfoMenuItem (JPopupMenu menu)
    {
        
        final EditorInfoBox _this = this;        
        
        final boolean pending = this.editor.isPending ();        
        
        if (!pending)
        {
        
            menu.add (UIUtils.createMenuItem ("Update the {editor} information",
                                              Constants.EDIT_ICON_NAME,
                                              new ActionListener ()
                                              {
                                                    
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                                
                                                    EditorsUIUtils.updateEditorInfo (_this.projectViewer,
                                                                                     _this.editor);
                                                                
                                                }
                                                        
                                              },
                                              null,
                                              null));
                                              
        }
        
    }
    
    public void addRemoveEditorMenuItem (JPopupMenu menu)
    {
        
        final EditorInfoBox _this = this;        
        
        if (this.editor.isPrevious ())
        {
            
            return;
            
        }
        
        boolean isEditorProject = this.projectViewer.getProject ().isEditorProject ();
        
        if (!isEditorProject)
        {
        
            menu.add (UIUtils.createMenuItem ("Remove {editor}",
                                                Constants.DELETE_ICON_NAME,
                                                new ActionListener ()
                                                {
                                                  
                                                   public void actionPerformed (ActionEvent ev)
                                                   {
                                                      
                                                       EditorsUIUtils.showRemoveEditor (_this.projectViewer,
                                                                                        _this.editor,
                                                                                        null);
                                                      
                                                   }
                                                  
                                               }));

        }        
        
    }
    
    public void addSearchMessagesMenuItem (final JPopupMenu  menu,
                                           final EditorPanel panel)
    {
        
        final EditorInfoBox _this = this;        
        
        final boolean pending = this.editor.isPending ();        
        
        if (!pending)
        {                        
        
            menu.add (UIUtils.createMenuItem ("Search messages",
                                                Constants.FIND_ICON_NAME,
                                                new ActionListener ()
                                                {
                                                   
                                                   public void actionPerformed (ActionEvent ev)
                                                   {
                                                       
                                                       panel.showSearch ();
                                                       
                                                   }
                                                   
                                                }));

        }
                
    }    
    
    public void addFullPopupListener ()
    {
        
        final EditorInfoBox _this = this;

        this.addMouseListener (new MouseEventHandler ()
        {
            
            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {
                
                _this.addDeleteAllMessagesMenuItem (m);
                        
                _this.addSendMessageMenuItem (m);
                    
                _this.addSendOrUpdateProjectMenuItem (m);
                                        
                _this.addShowCommentsMenuItem (m);
/*
                infBox.addSearchMessagesMenuItem (m,
                                                  _this);
  */              
                    
                _this.addUpdateEditorInfoMenuItem (m);
                
                _this.addRemoveEditorMenuItem (m);                
                
            }
            
        });                
        
    }
    
    public void addBasicPopupListener ()
    {
        
        final EditorInfoBox _this = this;
                
        this.addMouseListener (new MouseEventHandler ()
        {
            
            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {
                                
                _this.addDeleteAllMessagesMenuItem (m);
                    
                _this.addSendMessageMenuItem (m);
                
                _this.addUpdateEditorInfoMenuItem (m);
                                                                            
            }
            
        });        
        
    }
    
}