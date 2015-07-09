package com.quollwriter.editors;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import javax.net.ssl.*;

import java.awt.event.*;
import javax.swing.*;

import org.jxmpp.util.*;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat.*;
import org.jivesoftware.smack.roster.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.provider.*;
import org.jivesoftware.smack.util.*;
import org.jivesoftware.smack.tcp.*;

import org.jivesoftware.smackx.iqregister.*;
import org.jivesoftware.smackx.offline.*;

import com.gentlyweb.logging.Logger;

//import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.messages.*;

public class EditorsMessageHandler implements ChatMessageListener
{
    
    private static String SERVER_SUFFIX = null;
    
    private XMPPTCPConnection conn = null;

    private String userJID = null;
    private boolean loggedIn = false;
    private EditorMessageProcessor messageProcessor = null;
    private boolean logMessages = false;
    private Logger messageLog = null;
    
    // We use an atomic integer here (rather than volatile which has problems with value increment/decrement)
    // to keep track of how many messages we are in the process of sending.  This is most useful when sending a
    // large message and the user has closed down the last project viewer.  For example if they send their project
    // then close QW straight after.
    private AtomicInteger messageSendInProgressCount = new AtomicInteger (0);
    
    private volatile boolean logoutRequested = false;
    
    public EditorsMessageHandler ()
    {
        
        this.messageProcessor = new DefaultEditorMessageProcessor ();
        
    }
    
    public void init ()
               throws Exception
    {
        
        if (EditorsMessageHandler.SERVER_SUFFIX == null)
        {
            
            EditorsMessageHandler.SERVER_SUFFIX = EditorsEnvironment.getEditorsProperty (Constants.EDITORS_JID_SUFFIX_PROPERTY_NAME);
            
            if (EditorsMessageHandler.SERVER_SUFFIX == null)
            {
                
                EditorsMessageHandler.SERVER_SUFFIX = "@www.quollwriter.com";
                
            }
            
        }
        
        File lf = new File (Environment.getLogDir (),
                            Constants.EDITOR_MESSAGES_LOG_NAME);
        
        lf.delete ();
        
        this.messageLog = new Logger ();
        this.messageLog.initLogFile (lf);        
        
    }
        
    private void logMessage (String text)
    {
        
        this.logMessage (text,
                         null);
        
    }

    private void logMessage (String        text,
                             EditorMessage mess)
    {
        
        if ((this.logMessages)
            ||
            (Environment.isDebugModeEnabled ())
           )
        {
            
            String t = text + (mess != null ? ": " + mess : "");
            
            if (this.messageLog != null)
            {
            
                this.messageLog.logInformationMessage (t);
                
            } else {
                
                Environment.logMessage (t);
                        
            }
            
        }
        
    }

    public void logMessages (boolean v)
    {
        
        this.logMessages = v;        
        
    }
    
    public boolean isMessageSendInProgress ()
    {
        
        return this.messageSendInProgressCount.intValue () > 0;
        
    }
    
    public boolean isLoggedIn ()
    {
        
        return this.loggedIn;
        
    }
    
    public EditorEditor.OnlineStatus getOnlineStatus (EditorEditor ed)
    {
        
        if ((this.conn == null)
            ||
            (ed == null)
            ||
            (!this.loggedIn)
           )
        {
            
            return null;
            
        }
                                
        Roster r = Roster.getInstanceFor (this.conn);
        
        if (r == null)
        {
            
            return null;
        
        }
         
        return this.getOnlineStatus (r.getPresence (this.getJID (ed)));
        
    }
    
    public EditorEditor.OnlineStatus getOnlineStatus (Presence p)
    {
                
        EditorEditor.OnlineStatus status = EditorEditor.OnlineStatus.offline;
        
        if (p == null)
        {
            
            return status;
            
        }
        
        if (!p.isAvailable ())
        {
        
            return status;

        }        
        
        switch (p.getMode ())
        {
            
            case available :
            {
                
                return EditorEditor.OnlineStatus.online;
            
            }
            
            case away :
            {
                
                return EditorEditor.OnlineStatus.away;
                
            }
            
            case dnd :
            {
                
                return EditorEditor.OnlineStatus.busy;
                
            }
            
            case xa :
            {
                
                return EditorEditor.OnlineStatus.snooze;
                
            }
            
        }
        
        return null;
        
    }
    
    public void setOnlineStatus (EditorEditor.OnlineStatus status)
                          throws Exception
    {
        
        Presence.Type type = Presence.Type.available;
        Presence.Mode mode = Presence.Mode.available;
        
        switch (status)
        {
            
            case online :
            {
                
                break;
            
            }
            
            case away :
            {
                
                mode = Presence.Mode.away;
                break;
                
            }
            
            case busy :
            {
                
                mode = Presence.Mode.dnd;
                break;
                
            }
            
            case snooze :
            {
                
                mode = Presence.Mode.xa;
                break;
                
            }
            
        }
        
        Presence presence = new Presence (type);
        presence.setMode (mode);
        this.conn.sendStanza (presence);        
        
    }
    
    /**
     * Set the object that will deal with the message once it has been received and built.
     * We only allow a single instance, as opposed to a listener to prevent handling/displaying
     * the message multiple times.  The processor is basically responsible for updating the UI and/or
     * bringing the message to the user's attention, usually via event firing or displaying popups.
     * The processor should NOT save the message, this class handles that.
     *
     * Where a processor is not specified then an instance of {@link DefaultEditorMessageProcessor}
     * will be used.
     *
     * @param p The processor.
     */
    public void setMessageProcessor (EditorMessageProcessor p)
    {
        
        this.messageProcessor = p;
        
    }
    
    private void handleMessageForUnknownEditor (final String  fromUsername,
                                                final Message message)
    {
        
        final EditorsMessageHandler _this = this;
        
        this.logMessage ("Processing message for unknown editor: " + fromUsername);
        
        EditorsEnvironment.getInvite (fromUsername,
                                      new EditorsWebServiceAction ()
                                      {
                                                
                                        @Override
                                        public void processResult (EditorsWebServiceResult res)
                                        {
                                                        
                                            Map m = (Map) res.getReturnObject ();
                                        
                                            if (m == null)
                                            {
                                                
                                                _this.logMessage ("No invite from unknown editor: " + fromUsername);
                                                
                                                // No invite.
                                                return;
                                                
                                            }
                                        
                                            Invite invite = null;
                                            
                                            try
                                            {
                                                
                                                invite = Invite.createFrom (m);
                                                
                                            } catch (Exception e) {
                                                
                                                Environment.logError ("Unable to create invite from: " +
                                                                      m +
                                                                      " from username: " +
                                                                      fromUsername,
                                                                      e);
                                                
                                                return;
                                                
                                            }                                                                

                                            // Did I send the invite or receive it?
                                            if (invite.getFromEmail ().equals (EditorsEnvironment.getUserAccount ().getEmail ()))
                                            {
                                                
                                                // Send by me.
                                                // Get the editor by their email.
                                                EditorEditor ed = EditorsEnvironment.getEditorByEmail (invite.getToEmail ());
                                                
                                                if (ed == null)
                                                {
                                                    
                                                    Environment.logError ("Unable to find editor with email: " +
                                                                          invite.getToEmail ());
                                                    
                                                    return;
                                                    
                                                }
                                                
                                                // Update the editor.
                                                ed.setTheirPublicKey (invite.getToPublicKey ());
                                                
                                                ed.setMessagingUsername (invite.getToMessagingUsername ());
                                                ed.setServiceName (invite.getToServiceName ());
                                                                                                                                                                                                
                                                try
                                                {
                                                
                                                    _this.processMessageForEditor (ed,
                                                                                   message);

                                                } catch (Exception e) {
                                                    
                                                    Environment.logError ("Unable to process message for editor: " +
                                                                          ed,
                                                                          e);
                                                    
                                                }
                                                
                                            } else {
                                                
                                                // Invite I've received.                                            
                                                if ((invite.getStatus () == Invite.Status.rejected)
                                                    ||
                                                    (invite.getStatus () == Invite.Status.accepted)
                                                   )
                                                {
                                                    
                                                    Environment.logError ("Illegal state, received invite from: " +
                                                                          fromUsername +
                                                                          " but invite status is: " +
                                                                          invite.getStatus ().getType ());
                                                    
                                                    // Already accepted/rejected the invite.
                                                    // Need to report.
                                                    return;
                                                    
                                                }
    
                                                final Invite finvite = invite;
                                                
                                                if (invite.getStatus () == Invite.Status.pending)
                                                {
                                                
                                                    // Create a new holding editor.
                                                    EditorEditor ed = new EditorEditor ();
                                                    ed.setEmail (invite.getFromEmail ());
                                                    ed.setTheirPublicKey (invite.getFromPublicKey ());
                                                    
                                                    ed.setMessagingUsername (invite.getFromMessagingUsername ());
                                                    ed.setServiceName (invite.getFromServiceName ());
                                                                                                                                                                                                    
                                                    try
                                                    {
                                                    
                                                        _this.processMessageForEditor (ed,
                                                                                       message);
    
                                                    } catch (Exception e) {
                                                        
                                                        Environment.logError ("Unable to process message for editor: " +
                                                                              ed,
                                                                              e);
                                                        
                                                    }
                                                                                                        
                                                }
                                                
                                            }
        
                                        }
                                        
                                      },
                                      null);
        
    }
    
    private void processMessageForEditor (EditorEditor ed,
                                          Message      message)
                                   throws Exception
    {
        
        // All messages should be encrypted.
        String body = message.getBody ();
                    
        EditorMessage mess = null;
        
        try
        {
            
            mess = MessageFactory.getMessage (body,
                                              ed,
                                              true);
            
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to convert message",
                                        e);
            
        }

        this.logMessage ("<<< Received",
                         mess);
        
        boolean saveMessage = false;
        
        try
        {
        
            if (this.messageProcessor == null)
            {
                
                this.messageProcessor = new DefaultEditorMessageProcessor ();
                
            }
        
            try
            {
        
                saveMessage = this.messageProcessor.processMessage (mess);
                
            } catch (Exception e) {
                
                throw new GeneralException ("Unable to process message: " +
                                            mess +
                                            ", using: " +
                                            this.messageProcessor,
                                            e);
                
            }
        
        } finally {
        
            if ((saveMessage)
                &&
                // May have a "fake" editor so don't save.
                (ed.getKey () > 0)
               )
            {
        
                try
                {
                
                    EditorsEnvironment.addMessage (mess);
                    
                } catch (Exception e) {
                    
                    throw new GeneralException ("Unable to save message: " +
                                                mess,
                                                e);
                            
                }              

            }
                
        }
        
    }
    
    public void processMessage (final Chat    chat,
                                final Message message)
    {

        // Check to make sure we know who this is.
        final String username = this.getUsernameFromJID (message.getFrom ());
        
        this.logMessage ("Received message from user: " + username);
        
        final EditorEditor ed = EditorsEnvironment.getEditorByMessagingUsername (username);
               
        final EditorsMessageHandler _this = this;
                        
        if (ed == null)
        {

            // We don't know about this editor, get any invite from them.
            this.handleMessageForUnknownEditor (username,
                                                message);
            
            return;
                                                            
        }
        
        if (ed.isRejected ())
        {
            
            // Send an unsubscribed, this user shouldn't be contacting us.
            this.sendUnsubscribed (ed);
            return;
            
        }
        
        if (ed.isPrevious ())
        {
            
            // They are a previous editor, this shouldn't be contacting us.
            this.sendUnsubscribed (ed);
            return;
            
        }
        
        if (ed.getTheirPublicKey () == null)
        {

            // Get the invite and their public key.
            EditorsEnvironment.getInvite (ed,
                                          new EditorsWebServiceAction ()
                                          {
                                            
                                            @Override
                                            public void processResult (EditorsWebServiceResult res)
                                            {
                                                            
                                                Map m = (Map) res.getReturnObject ();
                                            
                                                if (m == null)
                                                {
                                                    
                                                    Environment.logError ("Unable to get invite for editor: " +
                                                                          ed);
                                                    
                                                    // No invite.
                                                    return;
                                                    
                                                }
                                            
                                                Invite invite = null;
                                                
                                                try
                                                {
                                                    
                                                    invite = Invite.createFrom (m);
                                                    
                                                } catch (Exception e) {
                                                    
                                                    Environment.logError ("Unable to create invite from: " +
                                                                          m +
                                                                          " from editor: " +
                                                                          ed,
                                                                          e);
                                                    
                                                    return;
                                                    
                                                }                                                                
    
                                                if (invite.getStatus () == Invite.Status.rejected)
                                                {
                                                    
                                                    Environment.logError ("Received message from: " +
                                                                          ed +
                                                                          " even though associated invite is rejected.");
                                                    
                                                    return;
                                                    
                                                }

                                                if (invite.getStatus () == Invite.Status.pending)
                                                {
                                                    
                                                    Environment.logError ("Received message from: " +
                                                                          ed +
                                                                          " even though invite is pending.");

                                                    return;
                                                    
                                                }
                                                
                                                // We are interested in the to public key here since "we" sent the
                                                // invite.
                                                ed.setTheirPublicKey (invite.getToPublicKey ());
                                            
                                                try
                                                {
                                            
                                                    EditorsEnvironment.updateEditor (ed);
                                                    
                                                } catch (Exception e)
                                                {
                                                    
                                                    Environment.logError ("Unable to update editor: " +
                                                                          ed,
                                                                          e);
                                                    
                                                    return;
                                                    
                                                }
                                                
                                                // Now process the message.
                                                try
                                                {
                                                    
                                                    _this.processMessageForEditor (ed,
                                                                                   message);
                                                    
                                                } catch (Exception e) {
                                                    
                                                    Environment.logError ("Unable to process message for editor: " +
                                                                          ed,
                                                                          e);
                                                    
                                                }
                                                
                                            }
                                            
                                          },
                                          null);
            
            return;
            
        }
        
        try
        {
        
            this.processMessageForEditor (ed,
                                          message);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to process message for editor: " +
                                  ed,
                                  e);
            
        }

/*        
        // All messages should be encrypted.
        String body = message.getBody ();
                    
        EditorMessage mess = null;
        
        try
        {
            
            mess = MessageFactory.getMessage (body,
                                              ed,
                                              true);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to convert message",
                                  e);
            
            return;
            
        }

        Environment.logDebugMessage ("<<< Received " + mess + " message from editor: " + ed);
        
        boolean saveMessage = false;
        
        try
        {
        
            if (this.messageProcessor == null)
            {
                
                this.messageProcessor = new DefaultEditorMessageProcessor ();
                
            }
        
            try
            {
        
                saveMessage = this.messageProcessor.processMessage (mess);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to process message: " +
                                      mess +
                                      ", using: " +
                                      this.messageProcessor,
                                      e);
                
                return;
                
            }
        
        } finally {
        
            if (saveMessage)
            {
        
                try
                {
                
                    EditorsEnvironment.addMessage (mess);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to save message: " +
                                          mess,
                                          e);
                    
                    return;
                            
                }              

            }
                
        }
        
        if (true)
        {
            
            return;
            
        }

        // TODO: ALL THIS BELOW CAN GO

/*
        
        if (ed.isPending ())
        {
        
            // Check the invite.
        
            // The message type must be "publickey".
            String body = message.getBody ();
            
            if (body == null)
            {
                
                // Not good...
                Environment.logError ("Illegal state, pending editor: " +
                                      email +
                                      " sent a null body message");
                
                // Unsubscribe?
                
                return;
                
            }
        
            // Check the message.
            // This is the only time we don't check for encryption.
                        
            // Should be json.
            
            EditorMessage mess = null;
            
            try
            {
                
                mess = MessageFactory.getMessage (body,
                                                  ed,
                                                  false);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to convert message",
                                      e);
                
                return;
                
            }
            
            if (!(mess instanceof PublicKeyMessage))
            {
                
                Environment.logError ("Invalid state, expected public key message, got: " +
                                      mess.getMessageType () +
                                      " from editor: " +
                                      ed);
                
                return;
                
            }
            
            Environment.logDebugMessage ("<<< Received public key from editor: " + ed);

            ed.setTheirPublicKey (((PublicKeyMessage) mess).getPublicKey ());
            
            try
            {
            
                EditorsEnvironment.addMessage (mess);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to save message: " +
                                      mess,
                                      e);
                
                return;
                
            }
            
            boolean showAcceptance = false;
            
            boolean publicKeySent = false;
            
            try
            {
                
                publicKeySent = EditorsEnvironment.hasMyPublicKeyBeenSentToEditor (ed);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to check whether editor has recevied public key: " +
                                      ed,
                                      e);
                
                return;
                
            }
            
            // If the editor invited me then set them as current.
            if ((!ed.isInvitedByMe ())
                ||
                // Have we already sent the public key?
                (publicKeySent)
               )
            {
                
                ed.setEditorStatus (EditorEditor.EditorStatus.current);
                
                showAcceptance = true;
                
            } 
            
            try
            {
                                                        
                EditorsEnvironment.updateEditor (ed);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to update editor: " +
                                      ed,
                                      e);
                
                return;
                                                            
            }
            
            if (publicKeySent)
            {
                
                // Send the editor info.
                this.sendMessage (null,
                                  new EditorInfoMessage (EditorsEnvironment.getUserAccount ()),
                                  ed,
                                  null,
                                  null);                                
                
            }
            
            if (showAcceptance)
            {
                                
                EditorsUIUtils.showAcceptance (ed);
                                    
            }
            
            return;
            
        }
        
        if (ed.isCurrent ())
        {            
          
            // All messages should be encrypted.
            // Devlier the message to our listeners.
            
            String body = message.getBody ();
                        
            EditorMessage mess = null;
            
            try
            {
                
                mess = MessageFactory.getMessage (body,
                                                  ed,
                                                  true);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to convert message",
                                      e);
                
                return;
                
            }

            Environment.logDebugMessage ("<<< Received " + mess + " message from editor: " + ed);
            
            boolean saveMessage = false;
            
            try
            {
            
                if (this.messageProcessor == null)
                {
                    
                    this.messageProcessor = new DefaultEditorMessageProcessor ();
                    
                }
            
                try
                {
            
                    saveMessage = this.messageProcessor.processMessage (mess);
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to process message: " +
                                          mess +
                                          ", using: " +
                                          this.messageProcessor,
                                          e);
                    
                    return;
                    
                }
            
            } finally {
            
                if (saveMessage)
                {
            
                    try
                    {
                    
                        EditorsEnvironment.addMessage (mess);
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to save message: " +
                                              mess,
                                              e);
                        
                        return;
                                
                    }              

                }
                    
            }
                        
        }
         
         */
                                                 
    }
        
    private boolean shouldShowLogin ()
    {
        
        return (EditorsEnvironment.getUserAccount () == null)
                ||
               (EditorsEnvironment.getUserAccount ().getPassword () == null);
                
    }    
    
    private void doLogin (final String                loginReason,
                          final ActionListener        onLogin,
                          final ActionListener        onCancel)
    {
        
        final EditorsMessageHandler _this = this;
        
        if (!this.shouldShowLogin ())
        {
            
            this.login (onLogin,
                        null);

            return;
            
        }
        
        EditorsEnvironment.goOnline (loginReason,
                                     onLogin,
                                     onCancel,
                                     null);
        
    }
    
    public void logout (final ActionListener onLogout)
    {
        
        if (this.isMessageSendInProgress ())
        {
            
            this.logoutRequested = true;
            
            return;
            
        }
        
        this.logoutRequested = false;
        
        this.userJID = null;
        this.loggedIn = false;
        
        final EditorsMessageHandler _this = this;
        
        new Thread (new Runnable ()
        {
            
            public void run ()
            {
        
                if (_this.conn != null)
                {
                    
                    try
                    {
                    
                        _this.conn.disconnect ();
                        
                        _this.conn = null;
                 
                    } catch (Exception e) { }
                    
                    if (onLogout != null)
                    {
                        
                        onLogout.actionPerformed (new ActionEvent ("logout", 1, "logout"));
                        
                    }
                    
                }
                
            }
            
        }).start ();
        
    }
    
    public void changePassword (final String         newPassword,
                                final ActionListener onComplete,
                                final ActionListener onError)
    {

        final EditorsMessageHandler _this = this;
    
        this.doLogin ("To change your password you must first login to the Editors service.",
                      new ActionListener ()
                      {
                        
                          public void actionPerformed (ActionEvent ev)
                          {
                            
                              try
                              {
                            
                                AccountManager.getInstance (_this.conn).changePassword (newPassword);
                                
                                if (onComplete != null)
                                {
                                    
                                    onComplete.actionPerformed (new ActionEvent ("complete", 1, ""));
                                    
                                }
                                
                              } catch (Exception e) {
                                
                                Environment.logError ("Unable to change password",
                                                      e);
                                
                                if (onError != null)
                                {
                                    
                                    onError.actionPerformed (new ActionEvent (e, 1, "error"));
                                    
                                    return;
                                    
                                }
                                                                
                                UIUtils.showErrorMessage (Environment.getFocusedProjectViewer (),
                                                          "Unable to change password, please contact Quoll Writer support for assistance.");                                
                                
                              }

                            
                          }
                        
                      },
                      onError);
        
    }
    
    public void login (final ActionListener onLogin,
                       final ActionListener onError)
    {
        
        final EditorsMessageHandler _this = this;

        this.logoutRequested = false;
        
        if (this.loggedIn)
        {
            
            if (onLogin != null)
            {
                
                onLogin.actionPerformed (new ActionEvent ("login", 1, "login"));
                
            }
            
            return;
            
        }
        
        new Thread (new Runnable ()
        {
            
            public void run ()
            {
        
                try
                {
        
                    EditorAccount acc = EditorsEnvironment.getUserAccount ();
                    
                    int port = 5222;
                    
                    try
                    {
                    
                        port = Integer.parseInt (EditorsEnvironment.getEditorsProperty (Constants.EDITORS_SERVICE_PORT_PROPERTY_NAME));
                        
                    } catch (Exception e) {
                        
                        // ???
                        
                    }

                    XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder ()
                                                                .setUsernameAndPassword (acc.getMessagingUsername (),
                                                                                         acc.getPassword ())
                                                                .setServiceName (acc.getServiceName ())
                                                                .setConnectTimeout (10 * 1000)
                                                                .setCompressionEnabled (true)
                                                                //.setDebuggerEnabled (true)
                                                                .setPort (port)
                                                                .build ();
                    
                    _this.conn = new XMPPTCPConnection (config);
                    
                    _this.conn.setUseStreamManagement (true);
                    _this.conn.setPacketReplyTimeout (30 * 3000);                    
                    
                    try
                    {
                    
                        _this.conn.connect ();
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to connect to Editors service",
                                              e);
                        
                        if (onError != null)
                        {
                            
                            UIUtils.doLater (onError);
                            
                        } else {
                                                
                            EditorsUIUtils.showLoginError ("Unable to connect to the Editors service, please contact Quoll Writer support for assistance.");
                            
                        }
                                                
                        return;
                                       
                    }
                    
                    _this.userJID = _this.conn.getUser ();
/*
                    // Get all the offline messages.
                    OfflineMessageManager mm = new OfflineMessageManager (_this.conn);
                    
                    List<Message> messages = mm.getMessages ();
                    
                    if (messages != null)
                    {
                        
                        // Switch off showing popups for messages.
                        boolean showPopup = EditorsEnvironment.isShowPopupWhenNewMessageReceived ();
                        
                        try
                        {
                        
                            EditorsEnvironment.setShowPopupWhenNewMessageReceived (false);
                            
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to update property to",
                                                  e);
                            
                        }
                        
                        for (Message m : messages)
                        {
                            
                            _this.processMessage (null,
                                                  m);
                            
                        }
                        
                        try
                        {
                        
                            EditorsEnvironment.setShowPopupWhenNewMessageReceived (showPopup);
                            
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to update property to: " + showPopup,
                                                  e);
                            
                        }
                        
                    }
*/
                    final Roster roster = Roster.getInstanceFor (_this.conn);

                    roster.setSubscriptionMode (Roster.SubscriptionMode.manual);
                                        
                    ChatManager chatMan = ChatManager.getInstanceFor (_this.conn);
                    
                    chatMan.addChatListener (new ChatManagerListener ()
                    {
                   
                        public void chatCreated (Chat chat, boolean createdLocally)
                        {
                    
                            if (!createdLocally)
                            {
                        
                                chat.addMessageListener (_this);
            
                            }
                            
                        }
                    
                    });
                    
                    // Add a check for subscriptions.
                    _this.conn.addAsyncStanzaListener (new StanzaListener ()
                    {
                        
                        public void processPacket (Stanza p)
                        {
                            
                            if (p instanceof Presence)
                            {
                                
                                Presence pp = (Presence) p;

                                final Presence pre = (Presence) p;
                                
                                final String username = EditorsMessageHandler.getUsernameFromJID (pre.getFrom ());

                                final String jid = pre.getFrom ();
                                
                                final EditorEditor ed = EditorsEnvironment.getEditorByMessagingUsername (username);
                                
                                if (pre.getType () == Presence.Type.unsubscribe)
                                {
                                                                        
                                    // Send unsubscribe.
                                    _this.sendPresence (Presence.Type.unsubscribe,
                                                        pre.getFrom ());
                                    _this.sendPresence (Presence.Type.unsubscribed,
                                                        pre.getFrom ());
                                    
                                    return;
                                    
                                    
                                }

                                if (pre.getType () == Presence.Type.subscribed)
                                {

                                }
                                
                                if (pre.getType () == Presence.Type.subscribe)
                                {
                                                                        
                                    if ((ed == null)
                                        ||
                                        (ed.getEditorStatus () == EditorEditor.EditorStatus.rejected)
                                       )
                                    {
                                        
                                        // No idea who this is, just return unsubscribed.
                                        _this.sendPresence (Presence.Type.unsubscribed,
                                                            pre.getFrom ());
                                        
                                        return;

                                    }

                                    _this.sendSubscribedToEditor (ed); 
                                    

                                }
                                                                        
                            }
                            
                        }
                        
                    },
                    new StanzaFilter ()
                    {
                        
                        public boolean accept (Stanza p)
                        {

                            if (p instanceof Presence)
                            {
                                
                                return true;
                                
                            }
                        /*
                            if (super.accept (p))
                            {
                                
                                Presence pp = (Presence) p;
                                
                                return true;

                            }
                          */  
                            return false;
                            
                        }
                        
                    });
                    
                    // Enable automatic reconnection.
                    ReconnectionManager.getInstanceFor (_this.conn).enableAutomaticReconnection ();
                    
                    try
                    {

                        _this.conn.login ();

                    } catch (Exception e) {
                        
                        Environment.logError ("User: " +
                                              acc.getEmail () +
                                              " is unable to login to editors service.",
                                              e);

                        EditorsUIUtils.showLoginError ("Unable to login.  Please check your email and password.");

                        if (onError != null)
                        {
                            
                            UIUtils.doLater (onError);
                            
                        }
                                                                   
                        return;
                                           
                    }
                    
                    for (EditorEditor ed : EditorsEnvironment.getEditors ())
                    {
                                                
                        Presence p = roster.getPresence (_this.getJID (ed));
                        
                        EditorEditor.OnlineStatus status = _this.getOnlineStatus (p);
                        
                        ed.setOnlineStatus (status);

                        EditorsEnvironment.fireEditorChangedEvent (new EditorChangedEvent (ed,
                                                                                           EditorChangedEvent.EDITOR_CHANGED));
                        
                    }
                    
                    roster.addRosterListener (new RosterListener ()
                    {
                       
                        public void entriesAdded (Collection<String> addrs)
                        {
                                                                                 
                        }
                        
                        public void entriesDeleted (Collection<String> addrs)
                        {
                                                      
                        }
                       
                        public void entriesUpdated (Collection<String> addrs)
                        {

                        }
                        
                        public void presenceChanged (Presence p)
                        {
                           
                            // Get the editor associated with the presence.
                            String jid = p.getFrom ();
                            
                            String username = EditorsMessageHandler.getUsernameFromJID (jid);
                        
                            EditorEditor ed = EditorsEnvironment.getEditorByMessagingUsername (username);
                            
                            if (ed != null)
                            {
                                
                                ed.setOnlineStatus (_this.getOnlineStatus (p));
                                
                                EditorsEnvironment.fireEditorChangedEvent (new EditorChangedEvent (ed,
                                                                                                   EditorChangedEvent.EDITOR_CHANGED));
                                
                            }

                        }
                       
                    });
                    
                    _this.loggedIn = true;
                     
                    if (onLogin != null)
                    {
                        
                        UIUtils.doLater (onLogin);
                        
                    }                    
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to login",
                                          e);

                    if (onError != null)
                    {

                        UIUtils.doLater (onError);
                        
                    } else {
                    
                        EditorsUIUtils.showLoginError ("Unable to login to the Editors service, please contact Quoll Writer support for assistance.");
                        
                    }
                                        
                }
                
            }
            
        }).start ();
        
    }
        
    private static String getUsernameFromJID (String jid)
    {
        
        return XmppStringUtils.unescapeLocalpart (XmppStringUtils.parseLocalpart (jid));
        
    }
    
    private static String getJID (EditorEditor ed)
    {
        
        return ed.getMessagingUsername () + "@" + ed.getServiceName ();
        
    }
        
    private void sendUnsubscribed (EditorEditor ed)
    {
        
        // Just in case.
        String jid = this.getJID (ed);        
        
        try
        {
        
            Presence ret = new Presence (Presence.Type.unsubscribed);
            ret.setTo (jid);
            this.conn.sendStanza (ret);        

        } catch (Exception e) {
            
            Environment.logError ("Unable to send unsubscribed presence to: " +
                                  jid,
                                  e);
            
        }        
        
    }
    
    private void sendPresence (Presence.Type type,
                               String        to)
    {
        
        try
        {
        
            Presence ret = new Presence (type);
            ret.setTo (to);
            this.conn.sendStanza (ret);        

        } catch (Exception e) {
            
            Environment.logError ("Unable to send presence: " +
                                  type +
                                  " to: " +
                                  to,
                                  e);
            
        }
        
    }
    
    private void sendSubscribedToEditor (final EditorEditor ed)
    {
                
        try
        {

            String jid = this.getJID (ed);
        
            this.sendPresence (Presence.Type.subscribed,
                               jid);
                                
        } catch (Exception e) {

            Environment.logError ("Unable to send subscribed to editor: " + ed,
                                  e);
            
        }
                
    }
    
    public void unsubscribeFromEditor (final EditorEditor ed)
    {
        
        final EditorsMessageHandler _this = this;
        
        new Thread (new Runnable ()
        {
            
            public void run ()
            {
                
                String jid = _this.getJID (ed);
                
                _this.sendPresence (Presence.Type.unsubscribe,
                                    jid);
                _this.sendPresence (Presence.Type.unsubscribed,
                                    jid);
                                
            }
            
        }).start ();

    }
    
    public void subscribeToEditor (final EditorEditor          ed)
    {
        
        final EditorsMessageHandler _this = this;
        
        new Thread (new Runnable ()
        {
            
            public void run ()
            {
                
                try
                {
        
                    String jid = _this.getJID (ed);
                
                    Roster.getInstanceFor (_this.conn).createEntry (jid,
                                                                    null,
                                                                    null);
            
                    _this.sendPresence (Presence.Type.subscribe,
                                        jid);

                                  
                    //_this.sendMyPublicKeyToEditor (ed);
                                                     
                } catch (Exception e) {

                /*
                    AbstractProjectViewer viewer = Environment.getFocusedProjectViewer ();
                
                    UIUtils.showErrorMessage (viewer,
                                              "Unable to subscribe to editor: " + ed.getEmail ());
*/
                    Environment.logError ("Unable to subscribe to editor: " + ed,
                                          e);
                    
                }
                
            }
            
        }).start ();
        
    }
    /*
    private void sendMyPublicKeyToEditor (final EditorEditor toEditor)
                                   throws Exception
    {
        
        final EditorsMessageHandler _this = this;
                                                               
        EditorAccount acc = EditorsEnvironment.getUserAccount ();
        
        PublicKeyMessage mess = new PublicKeyMessage (EditorsEnvironment.getUserAccount ().getPublicKey ());
        
        this.sendMessage (null,
                          mess,
                          toEditor,
                          new ActionListener ()
                          {
                            
                              public void actionPerformed (ActionEvent ev)
                              {
                                
                                    if (toEditor.getTheirPublicKey () != null)
                                    {
                                        
                                        // Send the editor info.
                                        _this.sendMessage (null,
                                                           new EditorInfoMessage (EditorsEnvironment.getUserAccount ()),
                                                           toEditor,
                                                           null,
                                                           null);                                                        
                                                        
                                    }
                                
                              }
                            
                          },
                          null);        
        
    }
        */
    public void sendMessage (final String                loginReason,
                             final EditorMessage         mess,
                             final EditorEditor          to,
                             final ActionListener        onSend,
                             final ActionListener        onLoginCancel)
    {

        this.sendMessage (loginReason,
                          mess,
                          to,
                          onSend,
                          onLoginCancel,
                          null);
    
    }
    
    public void sendMessage (final String                loginReason,
                             final EditorMessage         mess,
                             final EditorEditor          to,
                             final ActionListener        onSend,
                             final ActionListener        onLoginCancel,
                             final ActionListener        onError)
    {

        final EditorsMessageHandler _this = this;
    
        this.logoutRequested = false;
    
        this.doLogin (loginReason,
                      new ActionListener ()
                      {
                        
                        public void actionPerformed (ActionEvent ev)
                        {
                                                        
                            // TODO: Next release change this to be more in context.
                            //EditorsEnvironment.showMessageSendWarningIfEditorOfflineMessage (to);
                                                        
                            if (_this.messageSendInProgressCount.intValue () < 0)
                            {
                                
                                _this.messageSendInProgressCount.set (0);
                                
                            }
                            
                            _this.messageSendInProgressCount.incrementAndGet ();
                            
                            Thread t = new Thread (new Runnable ()
                            {
                                
                                public void run ()
                                {
                            
                                    try
                                    {
                                    
                                        _this.logMessage (">>> Sending",
                                                          mess);
                                    
                                        mess.setEditor (to);
                                        mess.setSentByMe (true);
                                        mess.setWhen (new Date ());
                                        mess.setDealtWith (true);
                                        
                                        Map data = mess.toMap ();
                                        
                                        String dmess = JSONEncoder.encode (data);
                                        
                                        byte[] bmess = dmess.getBytes ();
                                        
                                        if (mess.isEncrypted ())
                                        {
                                            
                                            if (to.getTheirPublicKey () == null)
                                            {
                                                
                                                throw new GeneralException ("Invalid state, no public key available for editor: " +
                                                                            to +
                                                                            ", unable to send message: " +
                                                                            mess);
                                                
                                            }
                                            
                                            bmess = EditorsUtils.encrypt (dmess,
                                                                          EditorsEnvironment.getUserAccount ().getPrivateKey (),
                                                                          to.getTheirPublicKey ());
                                
                                            mess.setOriginalMessage (new String (bmess, "utf-8"));
                                
                                        } else {
                                            
                                            mess.setOriginalMessage (dmess);
                                            
                                        }
                                        
                                        Chat c = ChatManager.getInstanceFor (_this.conn).createChat (_this.getJID (to),
                                                                                                     _this);
                                
                                        Message m = new Message ();
                                        m.setBody (new String (bmess, "utf-8"));
                                        m.setType (Message.Type.normal);

                                        c.sendMessage (m);

                                        // Save the message away.
                                        EditorsEnvironment.addMessage (mess);
                                        
                                        if (onSend != null)
                                        {
                                            
                                            UIUtils.doLater (onSend);
                                            
                                        }
                                        
                                    } catch (Exception e) {
                                        
                                        if (onError != null)
                                        {
                                            
                                            UIUtils.doLater (onError,
                                                             new Exception ("Unable to send message to " + to,
                                                                            e),
                                                             null);

                                        } else {
                                        
                                            AbstractProjectViewer viewer = Environment.getFocusedProjectViewer ();
                                            
                                            UIUtils.showErrorMessage (viewer,
                                                                      "Unable to send message to <b>" + to.getName () + "</b>.  Please contact Quoll Writer support for assistance.");
                                            
                                            Environment.logError ("Unable to send message to: " +
                                                                  to,
                                                                  e);

                                        }
                                                                  
                                    } finally {
                                        
                                        _this.messageSendInProgressCount.decrementAndGet ();
                                        
                                        if ((_this.messageSendInProgressCount.intValue () <= 0)
                                            &&
                                            (_this.logoutRequested)
                                           )
                                        {
                                            
                                            _this.logout (null);
                                                                                        
                                        }
                                        
                                    }
                                    
                                }
                                
                            });
                            
                            t.setDaemon (false);
                            
                            t.start ();
                            
                        }
                        
                      },
                      onLoginCancel);

    }    
            
}