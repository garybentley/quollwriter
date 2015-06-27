package com.quollwriter.editors.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.AWTEvent;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.plaf.LayerUI;

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

public class EditorInfoBox extends Box implements EditorChangedListener, EditorMessageListener
{
    
    private EditorEditor editor = null;
    private AbstractProjectViewer projectViewer = null;
    private JLabel avatar = null;
    private JLabel mainName = null;
    private JLabel onlineStatus = null;
    private JLabel other = null;
    private Box details = null;
    private Box editorInfo = null;
    private JButton importantMessages = null;
    private JButton comments = null;
    private JButton chat = null;
    private boolean showImportantMessages = false;
    private ProjectEditor projEditor = null;
    private MessageBox pendingMessageBox = null;
    
    public EditorInfoBox (EditorEditor          ed,
                          AbstractProjectViewer viewer,
                          boolean               showImportantMessages)
                   throws GeneralException
    {
        
        super (BoxLayout.Y_AXIS);
        
        final EditorInfoBox _this = this;

        this.editor = ed;
        
        this.showImportantMessages = showImportantMessages;
        
        // Load the messages.        
        EditorsEnvironment.loadMessagesForEditor (this.editor);        
        
        // We add ourselves as a listener for editor change events however we don't ever
        // remove ourselves since, as a standard component, we don't have a fixed lifecycle.
        EditorsEnvironment.addEditorChangedListener (this);

        EditorsEnvironment.addEditorMessageListener (this);
        
        this.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.projectViewer = viewer;
        
        this.projEditor = viewer.getProject ().getProjectEditor (this.editor);
                
        this.editorInfo = new Box (BoxLayout.X_AXIS);                
        this.editorInfo.setAlignmentX (Component.LEFT_ALIGNMENT);
                
        JLayer infoWrapper = new JLayer<JComponent> (this.editorInfo, new LayerUI<JComponent> ()
        {
            
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                // enable mouse motion events for the layer's subcomponents
                ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
            }

            @Override
            public void uninstallUI(JComponent c) {
                super.uninstallUI(c);
                // reset the layer event mask
                ((JLayer) c).setLayerEventMask(0);
            }
             
            @Override
            public void processMouseEvent (MouseEvent                   ev,
                                           JLayer<? extends JComponent> l)
            {

                // TODO: Check for multi-platform compatibility.
                if (ev.getID () != MouseEvent.MOUSE_RELEASED)
                {
                    
                    return;
                    
                }

                if (ev.getSource () instanceof JButton)
                {
                    
                    return;
                    
                }
        
                if (_this.editor.getEditorStatus () == EditorEditor.EditorStatus.pending)
                {
                    
                    return;
                    
                }
                
                if (ev.getClickCount () != 1)
                {
                    
                    return;
                                
                }

                if (ev.isPopupTrigger ())
                {
                    
                    return;
                    
                }
                
                // Show the editor.
                try
                {
                                        
                    _this.projectViewer.sendMessageToEditor (_this.editor);
                    
                } catch (Exception e) {
                    
                    UIUtils.showErrorMessage (_this.projectViewer,
                                              "Unable to show {editor}.");
                    
                    Environment.logError ("Unable to show editor: " +
                                          _this.editor,
                                          e);
                    
                }
                
            }
            
        });
            
        infoWrapper.setAlignmentX (Component.LEFT_ALIGNMENT);
            
        this.add (infoWrapper);
                
        this.setOpaque (false);
                
        this.avatar = new JLabel ();
                
        this.avatar.setAlignmentY (Component.TOP_ALIGNMENT);
        
        this.editorInfo.add (this.avatar);
        this.avatar.setOpaque (false);
        
        this.avatar.setBorder (new CompoundBorder (UIUtils.createPadding (0, 0, 0, 5),
                                                   UIUtils.createLineBorder ()));
        
        final boolean pending = ed.isPending ();
        
        this.details = new Box (BoxLayout.Y_AXIS);
        details.setAlignmentY (Component.TOP_ALIGNMENT);

        JLabel l = new JLabel ("");
        l.setAlignmentX (Component.LEFT_ALIGNMENT);
        l.setFont (l.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (14d)).deriveFont (java.awt.Font.PLAIN));
        
        //l.setFont (l.getFont ().deriveFont ((float) 16).deriveFont (Font.PLAIN));
        l.setAlignmentY (Component.TOP_ALIGNMENT);
        l.setVerticalAlignment (SwingConstants.TOP);
        this.details.add (l);
        this.mainName = l;
                        
        l = UIUtils.createInformationLabel (null);
        this.onlineStatus = l;

        l.setVisible (false);
        //l.setAlignmentY (Component.TOP_ALIGNMENT);
        //l.setVerticalAlignment (SwingConstants.TOP);
        UIUtils.setPadding (l, 0, 3, 0, 5);
        
        // Create a horizontal box to hold:
        //  Online status | Important messages button | Chat messages button
        Box statusBox = new Box (BoxLayout.X_AXIS);

        statusBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.details.add (statusBox);

        this.importantMessages = UIUtils.createButton (Constants.ERROR_ICON_NAME,
                                                             Constants.ICON_MENU,
                                                             "",
                                                             new ActionListener ()
                                                             {
                                            
                                                                public void actionPerformed (ActionEvent ev)
                                                                {
                                                                 
                                                                     try
                                                                     {
                                                                 
                                                                        EditorsUIUtils.showImportantMessagesForEditor (_this.editor,
                                                                                                                       _this.projectViewer);
                                                                         
                                                                     } catch (Exception e) {
                                                                         
                                                                         UIUtils.showErrorMessage (_this.projectViewer,
                                                                                                   "Unable to show new/important messages for {editor}.");
                                                                         
                                                                         Environment.logError ("Unable to show important messages for editor: " +
                                                                                               _this.editor,
                                                                                               e);
                                                                         
                                                                     }                                                        
                                                                     
                                                                }
                                            
                                                             });
        
        this.importantMessages.setIconTextGap (2);
        this.importantMessages.setFont (this.importantMessages.getFont ().deriveFont (Font.BOLD,
                                                                                      14));
        this.importantMessages.setForeground (java.awt.Color.red);

        this.comments = UIUtils.createButton (Constants.COMMENT_ICON_NAME,
                                              Constants.ICON_MENU,
                                              "",
                                              new ActionListener ()
                                              {
                                            
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                                 
                                                    try
                                                    {
                                                                 
                                                        EditorsUIUtils.showAllCommentsForEditor (_this.editor,
                                                                                                 _this.projectViewer);
                                                                         
                                                    } catch (Exception e) {
                                                        
                                                        UIUtils.showErrorMessage (_this.projectViewer,
                                                                                  "Unable to show {comments} for {editor}.");
                                                        
                                                        Environment.logError ("Unable to show comments for editor: " +
                                                                              _this.editor,
                                                                              e);
                                                        
                                                    }                                                        
                                                                     
                                                }
                                            
                                             });
        
        this.comments.setIconTextGap (2);
        this.comments.setFont (this.importantMessages.getFont ().deriveFont (Font.BOLD,
                                                                             14));

        this.chat = UIUtils.createButton (Constants.MESSAGE_ICON_NAME,
                                          Constants.ICON_MENU,
                                          "",
                                          new ActionListener ()
                                          {
                                            
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                                 
                                                    try
                                                    {
                                                                 
                                                        _this.projectViewer.sendMessageToEditor (_this.editor);
                                                                         
                                                    } catch (Exception e) {
                                                        
                                                        UIUtils.showErrorMessage (_this.projectViewer,
                                                                                  "Unable to show {editor}.");
                                                        
                                                        Environment.logError ("Unable to show editor: " +
                                                                              _this.editor,
                                                                              e);
                                                        
                                                    }                                                        
                                                                     
                                                }
                                            
                                             });
        
        this.chat.setIconTextGap (2);
        this.chat.setFont (this.importantMessages.getFont ().deriveFont (Font.BOLD,
                                                                         14));

        java.util.List<JComponent> buts = new java.util.ArrayList ();
        buts.add (this.onlineStatus);
        buts.add (this.importantMessages);
        buts.add (this.comments);
        buts.add (this.chat);
        
        statusBox.add (UIUtils.createButtonBar (buts));
        statusBox.add (Box.createHorizontalGlue ());

        l = UIUtils.createInformationLabel (null);
        l.setVisible (false);
        l.setFont (l.getFont ().deriveFont (java.awt.Font.ITALIC));
        UIUtils.setPadding (l, 5, 3, 0, 5);
        this.details.add (l);        
                        
        this.other = l;
                        
        this.editorInfo.add (this.details);
                                
    }
        
    public void setShowImportantMessages (boolean v)
    {
        
        this.showImportantMessages = v;
        
        this.update ();
                
    }
    
    public boolean isShowImportantMessages ()
    {
        
        return this.showImportantMessages;
        
    }
    
    public void handleMessage (EditorMessageEvent ev)
    {
    
        if (ev.getEditor () == this.editor)
        {
            
            this.update ();
            
        }
        
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
            this.onlineStatus.setMaximumSize (this.onlineStatus.getPreferredSize ());

        }        
        
        if ((this.editor.isPending ())
            &&
            (this.pendingMessageBox == null)
            &&
            (!this.editor.isInvitedByMe ())
           )
        {

            if (!this.editor.isInvitedByMe ())
            {
        
                // Show an accept/reject.
                EditorMessage m = null;
                
                if ((this.editor.getMessages () != null)
                    &&
                    (this.editor.getMessages ().size () > 0)
                   )
                {
                    
                    m = this.editor.getMessages ().iterator ().next ();
                    
                }
                
                MessageBox mb = null;
                
                try
                {
                
                    mb = MessageBoxFactory.getMessageBoxInstance (m,
                                                                  this.projectViewer);
                    mb.setShowAttentionBorder (false);
        
                    mb.init ();
        
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to get message box for message: " +
                                          m,
                                          e);
                                    
                    UIUtils.showErrorMessage (this.projectViewer,
                                              "Unable to show invite from {editor}, please contact Quoll Writer support for assistance.");
    
                    return;
                    
                }
                
                mb.setAlignmentX (Component.LEFT_ALIGNMENT);
    
                this.pendingMessageBox = mb;
                this.add (mb);
                
                this.other.setText (String.format ("Received: %s",
                                                   Environment.formatDate (this.editor.getDateCreated ())));
                    
            } else {
                
                this.other.setText (String.format ("Invited: %s",
                                                   Environment.formatDate (this.editor.getDateCreated ())));
    
                this.other.setVisible (true);
                
            }

            this.other.setVisible (true);

            /*
            final EditorInfoBox _this = this;
            
                // Show an accept/reject.
                EditorMessage m = null;
                
                if ((this.editor.getMessages () != null)
                    &&
                    (this.editor.getMessages ().size () > 0)
                   )
                {
                    
                    m = this.editor.getMessages ().iterator ().next ();
                    
                }
                
                MessageBox mb = null;
                
                try
                {
                
                    mb = MessageBoxFactory.getMessageBoxInstance (m,
                                                                  this.projectViewer);
                    mb.setShowAttentionBorder (false);
        
                    mb.init ();
        
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to get message box for message: " +
                                          m,
                                          e);
                                    
                    UIUtils.showErrorMessage 
                                    
                }
                
                mb.setAlignmentX (Component.LEFT_ALIGNMENT);
                
                
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

                    this.add (bb);
                    
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
                    
                    this.add (p);
                                                                                     
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

                    this.add (bb);
                                
                }
                
            }
            */
        } else {
        
            if (this.pendingMessageBox != null)
            {
        
                this.pendingMessageBox.setVisible (false);
                this.remove (this.pendingMessageBox);
                this.pendingMessageBox = null;
                
            }
        
            UIUtils.setAsButton (this.editorInfo);
        
            if (!this.editor.isPrevious ())
            {
        
                this.editorInfo.setToolTipText (String.format ("Click to send a message to %s, right click to see the menu",
                                                               this.editor.getMainName ()));
                
            } else {
                
                this.editorInfo.setToolTipText ("Right click to see the menu");
                
            }
                    
            if (this.projEditor != null)
            {
                
                this.other.setVisible (true);
                this.other.setText (Environment.replaceObjectNames (this.projEditor.getStatusMessage ()));
                        
            }
            
        }
        
        this.importantMessages.setVisible (false);
        
        // Get undealt with messages that are not chat.
        // If there is just one then show it, otherwise show a link that will display a popup of them.
        Set<EditorMessage> undealtWith = this.editor.getMessages (new EditorMessageFilter ()
                                                                  {
                                                                    
                                                                      @Override
                                                                      public boolean accept (EditorMessage m)
                                                                      {
                                                                        
                                                                          if (m.isDealtWith ())
                                                                          {
                                                                            
                                                                              return false;
                                                                            
                                                                          }
                                                                        
                                                                          if (m.getMessageType ().equals (EditorChatMessage.MESSAGE_TYPE))
                                                                          {
                                                                            
                                                                              return false;
                                                                            
                                                                          }
                                                                          
                                                                          return true;
                                                                        
                                                                      }
                                                                    
                                                                  });
        
        if ((undealtWith.size () > 0)
            &&
            (!this.editor.isPending ())
           )
        {
        
            this.importantMessages.setToolTipText (String.format ("%s important message%s require%s your attention",
                                                                  Environment.formatNumber (undealtWith.size ()),
                                                                  (undealtWith.size () == 1 ? "" : "s"),
                                                                  (undealtWith.size () == 1 ? "s" : "")));
            this.importantMessages.setText (String.format ("%s",
                                                           Environment.formatNumber (undealtWith.size ())));
    
            this.importantMessages.setVisible (true);
            
        }

        this.comments.setVisible (false);
        
        int commCount = 0;
    
        if (this.projEditor != null)
        {
            
            this.comments.setVisible (true);
            this.comments.setForeground (java.awt.Color.black);
            
            // Get undealt with messages that are not chat.
            // If there is just one then show it, otherwise show a link that will display a popup of them.
            Set<EditorMessage> comments = this.editor.getMessages (new DefaultEditorMessageFilter (this.projectViewer.getProject (),
                                                                                                   ProjectCommentsMessage.MESSAGE_TYPE));
            
            if (comments.size () > 0)
            {
                
                int sets = comments.size ();
                int undealtWithCount = 0;
                
                for (EditorMessage m : comments)
                {
                    
                    if (!m.isDealtWith ())
                    {
                        
                        undealtWithCount++;
                        
                            
                    }
                    
                    ProjectCommentsMessage pcm = (ProjectCommentsMessage) m;
                    
                    commCount += pcm.getComments ().size ();
                    
                }
                
                if (undealtWithCount > 0)
                {
                    
                    this.comments.setForeground (java.awt.Color.red);
                    
                }
                
                this.comments.setToolTipText (Environment.replaceObjectNames (String.format ("%s {comment%s} %s {editor}",
                                                                              (this.projEditor != null ? "from this" : "sent to"),
                                                                              Environment.formatNumber (commCount),
                                                                              (commCount == 1 ? "" : "s"))));
        
                this.comments.setVisible (true);
                
            }
        
            this.comments.setText (Environment.formatNumber (commCount));
            
            this.comments.setEnabled (commCount > 0);
            
        }
        
        this.chat.setVisible (false);

        Set<EditorMessage> chatMessages = this.editor.getMessages (new EditorMessageFilter ()
                                                                   {
                                                                    
                                                                        public boolean accept (EditorMessage m)
                                                                        {
                                                                            
                                                                            if (m.isDealtWith ())
                                                                            {
                                                                                
                                                                                return false;
                                                                                
                                                                            }
                                                                            
                                                                            if (!m.getMessageType ().equals (EditorChatMessage.MESSAGE_TYPE))
                                                                            {
                                                                                
                                                                                return false;
                                                                                
                                                                            }
                                                                            
                                                                            return true;
                                                                            
                                                                        }
                                                                    
                                                                   });
        
        if (chatMessages.size () > 0)
        {
                        
            this.chat.setForeground (java.awt.Color.red);
                
            this.chat.setToolTipText (Environment.replaceObjectNames (String.format ("%s unread chat message%s",
                                                                                     Environment.formatNumber (chatMessages.size ()),
                                                                                     (chatMessages.size () == 1 ? "" : "s"))));
            this.chat.setText (Environment.formatNumber (chatMessages.size ()));
    
            this.chat.setVisible (true);
            
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
    
    public void addShowImportantMessagesMenuItem (JPopupMenu menu)
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
                    
                    if (!EditorsUIUtils.getDefaultViewableMessageFilter ().accept (m))
                    {
                        
                        return false;
                        
                    }
                    
                    if (m.isDealtWith ())
                    {
                        
                        return false;
                        
                    }
                    
                    if (m.getMessageType ().equals (EditorChatMessage.MESSAGE_TYPE))
                    {
                      
                        return false;
                      
                    }
                    
                    return true;
                
                }
                
            });
                
            if (messages.size () > 0)
            {
                
                menu.addSeparator ();                
                                
                menu.add (UIUtils.createMenuItem (String.format ("View new/important messages (%s)",
                                                                 Environment.formatNumber (messages.size ())),
                                                  Constants.ERROR_ICON_NAME,
                                                  new ActionListener ()
                                                  {
                                                                                                
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            try
                                                            {                                                            
                                                            
                                                              EditorsUIUtils.showImportantMessagesForEditor (_this.editor,
                                                                                                             _this.projectViewer);
    
                                                            } catch (Exception e) {
                                                                
                                                                Environment.logError ("Unable to show project messages for editor: " +
                                                                                      _this.editor,
                                                                                      e);
                                                                
                                                                UIUtils.showErrorMessage (_this.projectViewer,
                                                                                          "Unable to {project} messages for editor.");
                                                                
                                                                return;
                                                                
                                                            }                                                                                                                        
                                                                                                                                                            
                                                        }
                                                
                                                  }));

            }
                        
        }        
        
    }
    
    public void addProjectSentAndUpdatesMenuItem (JPopupMenu menu)
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
                    
                    return (((m.getMessageType ().equals (UpdateProjectMessage.MESSAGE_TYPE))
                             ||
                             (m.getMessageType ().equals (NewProjectMessage.MESSAGE_TYPE))
                            )
                            &&
                            (_this.projectViewer.getProject ().getId ().equals (m.getForProjectId ()))
                           );
                
                }
                
            });
                
            if (messages.size () > 0)
            {
                
                menu.addSeparator ();                
                
                // Check to see if editor is a project editor.
                String suffix = (this.projectViewer.getProject ().getProjectEditor (this.editor) != null ? "sent" : "received");
                                
                menu.add (UIUtils.createMenuItem ("View what {project} updates you have " + suffix,
                                                  Constants.FIND_ICON_NAME,
                                                  new ActionListener ()
                                                  {
                                                                                                
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            try
                                                            {                                                            
                                                            
                                                                EditorsUIUtils.showProjectMessagesForEditor (_this.editor,
                                                                                                             _this.projectViewer);

                                                            } catch (Exception e) {
                                                                
                                                                Environment.logError ("Unable to show project messages for editor: " +
                                                                                      _this.editor,
                                                                                      e);
                                                                
                                                                UIUtils.showErrorMessage (_this.projectViewer,
                                                                                          "Unable to {project} messages for editor.");
                                                                
                                                                return;
                                                                
                                                            }                                                            
                                                                                                                                                            
                                                        }
                                                
                                                  }));

            }
                        
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
        
            String suffix = (this.projectViewer.getProject ().getProjectEditor (this.editor) != null ? "received" : "sent");
        
            Iterator<EditorMessage> iter = messages.iterator ();
        
            if (messages.size () > 0)
            {
                
                menu.addSeparator ();                
                
                final ProjectCommentsMessage message = (ProjectCommentsMessage) messages.iterator ().next ();
                
                menu.add (UIUtils.createMenuItem (String.format ("View last {comments} (%s) %s",
                                                                 message.getComments ().size (),
                                                                 suffix),
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
                
                menu.add (UIUtils.createMenuItem (String.format ("View all {comments} %s",
                                                                 suffix),
                                                  Constants.COMMENT_ICON_NAME,
                                                  new ActionListener ()
                                                  {
                                                    
                                                      public void actionPerformed (ActionEvent ev)
                                                      {
                                                        
                                                        try
                                                        {                                                            
                                                        
                                                            EditorsUIUtils.showAllCommentsForEditor (_this.editor,
                                                                                                     _this.projectViewer);
                                                            
                                                        } catch (Exception e) {
                                                            
                                                            Environment.logError ("Unable to show comments from editor: " +
                                                                                  _this.editor,
                                                                                  e);
                                                            
                                                            UIUtils.showErrorMessage (_this.projectViewer,
                                                                                      "Unable to show {comments} from editor.");
                                                            
                                                            return;
                                                            
                                                        }
                                                        
                                                      }
                                                    
                                                  }));
                
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
    
    /**
     * Add a mouse listener to the content, because the JLayer intercepts the mouse events we need to channel the
     * listener add to the actual content component.
     *
     * TODO: Make this nicer somehow, and add removeMouseListener.
     */
    @Override
    public void addMouseListener (MouseListener m)
    {
        
        this.editorInfo.addMouseListener (m);
        
    }
    
    public void addRemoveEditorMenuItem (JPopupMenu menu)
    {
        
        final EditorInfoBox _this = this;        
        
        if (this.editor.isPrevious ())
        {
            
            return;
            
        }
        
        if (this.editor.isPending ())
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

        this.editorInfo.addMouseListener (new MouseEventHandler ()
        {
            
            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {
                
                _this.addDeleteAllMessagesMenuItem (m);
                        
                _this.addSendMessageMenuItem (m);

                _this.addSendOrUpdateProjectMenuItem (m);
                    
                _this.addShowImportantMessagesMenuItem (m);
                                        
                _this.addShowCommentsMenuItem (m);

                _this.addProjectSentAndUpdatesMenuItem (m);

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
                
        this.editorInfo.addMouseListener (new MouseEventHandler ()
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