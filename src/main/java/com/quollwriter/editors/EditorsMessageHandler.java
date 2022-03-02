package com.quollwriter.editors;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;

import javax.net.ssl.*;

import javafx.beans.property.*;

import org.jxmpp.jid.*;
import org.jxmpp.jid.impl.*;
import org.jxmpp.util.*;
import org.jxmpp.stringprep.XmppStringprepException;
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

//import com.gentlyweb.utils.*;

import com.quollwriter.*;
//import com.quollwriter.ui.*;
//import com.quollwriter.ui.components.QPopup;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.ComponentUtils;
import com.quollwriter.editors.messages.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class EditorsMessageHandler implements ChatMessageListener
{

    private static String SERVER_SUFFIX = null;

    private XMPPTCPConnection conn = null;

    private EntityFullJid userJID = null;
    private boolean loggedIn = false;
    private EditorMessageProcessor messageProcessor = null;
    private boolean logMessages = false;

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

            Environment.logMessage (t);

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
                                               throws XmppStringprepException
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

                                            // Is the invite deleted?  If so just ignore the message.
                                            if (invite.getStatus () == Invite.Status.deleted)
                                            {

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

    }

    private boolean shouldShowLogin ()
    {

        return (EditorsEnvironment.getUserAccount () == null)
                ||
               (EditorsEnvironment.getUserAccount ().getPassword () == null);

    }

    private void doLogin (final StringProperty  loginReason,
                          final Runnable        onLogin,
                          final Runnable        onCancel)
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

    public void logout (final Runnable onLogout)
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

        Environment.scheduleImmediately (() ->
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

                    Environment.scheduleImmediately (onLogout);

                }

            }

        });

    }

    public void changePassword (final String              newPassword,
                                final Runnable onComplete,
                                final Runnable onCancel,
                                final java.util.function.Consumer<Exception> onError)
    {

        final EditorsMessageHandler _this = this;

        this.doLogin (getUILanguageStringProperty (editors,login,reasons,changepassword),
                      //"To change your password you must first login to the Editors service.",
                      () ->
                      {

                          try
                          {

                            AccountManager.getInstance (_this.conn).changePassword (newPassword);

                            if (onComplete != null)
                            {

                                Environment.scheduleImmediately (onComplete);

                            }

                          } catch (Exception e) {

                            Environment.logError ("Unable to change password",
                                                  e);

                            if (onError != null)
                            {

                                Environment.scheduleImmediately (() ->
                                {

                                    onError.accept (e);

                                });

                                return;

                            }

                            ComponentUtils.showErrorMessage (getUILanguageStringProperty (editors,user,changepassword,actionerror));
                                                      //"Unable to change password, please contact Quoll Writer support for assistance.");

                          }

                      },
                      onCancel);

    }

    public void login (final Runnable            onLogin,
                       final java.util.function.Consumer<Exception> onError)
    {

        final EditorsMessageHandler _this = this;

        this.logoutRequested = false;

        if ((this.conn != null)
            &&
            (this.conn.isConnected ())//this.loggedIn)
           )
        {

            if (onLogin != null)
            {

                Environment.scheduleImmediately (onLogin);

            }

            return;

        }

        Environment.scheduleImmediately (() ->
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
                                                            .setServiceName (JidCreate.domainBareFrom (acc.getServiceName ()))
                                                            .setConnectTimeout (10 * 1000)
                                                            .setCompressionEnabled (true)
                                                            .setSendPresence (true)
                                                            //.setDebuggerEnabled (true)
                                                            .setPort (port)
                                                            .build ();

                _this.conn = new XMPPTCPConnection (config);

                _this.conn.setUseStreamManagement (true);
                _this.conn.setReplyTimeout (30 * 3000);

                try
                {

                    _this.conn.connect ();

                } catch (Exception e) {

                    Environment.logError ("Unable to connect to Editors service",
                                          e);

                    if (onError != null)
                    {

                        Environment.scheduleImmediately (() ->
                        {

                            onError.accept (e);

                        });

                    } else {

                        EditorsUIUtils.showLoginError (getUILanguageStringProperty (editors,login,errors,other));
                        //"Unable to connect to the Editors service, please contact Quoll Writer support for assistance.");

                    }

                    return;

                }

                _this.userJID = _this.conn.getUser ();

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

                    public void processStanza (Stanza p)
                    {

                        if (p instanceof Presence)
                        {

                            Presence pp = (Presence) p;

                            final Presence pre = (Presence) p;

                            final String username = EditorsMessageHandler.getUsernameFromJID (pre.getFrom ());

                            final EditorEditor ed = EditorsEnvironment.getEditorByMessagingUsername (username);

                            if (pre.getType () == Presence.Type.unsubscribe)
                            {

                                // Send unsubscribe.
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

                    EditorsUIUtils.showLoginError (getUILanguageStringProperty (editors,login,errors,invalidcredentials));
                    //"Unable to login.  Please check your email and password.");

                    if (onError != null)
                    {

                        Environment.scheduleImmediately (() ->
                        {

                            onError.accept (e);

                        });

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

                    public void entriesAdded (Collection<Jid> addrs)
                    {

                    }

                    public void entriesDeleted (Collection<Jid> addrs)
                    {

                    }

                    public void entriesUpdated (Collection<Jid> addrs)
                    {

                    }

                    public void presenceChanged (Presence p)
                    {

                        try
                        {

                            // Get the editor associated with the presence.
                            Jid jid = p.getFrom ();

                            String username = EditorsMessageHandler.getUsernameFromJID (jid);

                            EditorEditor ed = EditorsEnvironment.getEditorByMessagingUsername (username);

                            if (ed != null)
                            {

                                ed.setOnlineStatus (_this.getOnlineStatus (p));

                                EditorsEnvironment.fireEditorChangedEvent (new EditorChangedEvent (ed,
                                                                                                   EditorChangedEvent.EDITOR_CHANGED));

                            }

                        } catch (Exception e) {

                            Environment.logError ("Unable to update editor",
                                                  e);

                        }

                    }

                });

                _this.loggedIn = true;

                if (onLogin != null)
                {

                    Environment.scheduleImmediately (onLogin);

                }

            } catch (Exception e) {

                Environment.logError ("Unable to login",
                                      e);

                if (onError != null)
                {

                    Environment.scheduleImmediately (() ->
                    {

                        onError.accept (e);

                    });

                } else {

                    EditorsUIUtils.showLoginError (getUILanguageStringProperty (editors,login,errors,other));
                    //"Unable to login to the Editors service, please contact Quoll Writer support for assistance.");

                }

            }

        });

    }
/*
TODO Remove
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
                                                                .setServiceName (JidCreate.domainBareFrom (acc.getServiceName ()))
                                                                .setConnectTimeout (10 * 1000)
                                                                .setCompressionEnabled (true)
                                                                //.setDebuggerEnabled (true)
                                                                .setPort (port)
                                                                .build ();

                    _this.conn = new XMPPTCPConnection (config);

                    _this.conn.setUseStreamManagement (true);
                    _this.conn.setReplyTimeout (30 * 3000);

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

                            EditorsUIUtils.showLoginError (getUIString (editors,login,errors,other));
                            //"Unable to connect to the Editors service, please contact Quoll Writer support for assistance.");

                        }

                        return;

                    }

                    _this.userJID = _this.conn.getUser ();

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

                        public void processStanza (Stanza p)
                        {

                            if (p instanceof Presence)
                            {

                                Presence pp = (Presence) p;

                                final Presence pre = (Presence) p;

                                final String username = EditorsMessageHandler.getUsernameFromJID (pre.getFrom ());

                                final EditorEditor ed = EditorsEnvironment.getEditorByMessagingUsername (username);

                                if (pre.getType () == Presence.Type.unsubscribe)
                                {

                                    // Send unsubscribe.
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

                            //if (super.accept (p))
                            //{

                            //    Presence pp = (Presence) p;

                            //    return true;

                            //}

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

                        EditorsUIUtils.showLoginError (getUIString (editors,login,errors,invalidcredentials));
                        //"Unable to login.  Please check your email and password.");

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

                        public void entriesAdded (Collection<Jid> addrs)
                        {

                        }

                        public void entriesDeleted (Collection<Jid> addrs)
                        {

                        }

                        public void entriesUpdated (Collection<Jid> addrs)
                        {

                        }

                        public void presenceChanged (Presence p)
                        {

                            try
                            {

                                // Get the editor associated with the presence.
                                Jid jid = p.getFrom ();

                                String username = EditorsMessageHandler.getUsernameFromJID (jid);

                                EditorEditor ed = EditorsEnvironment.getEditorByMessagingUsername (username);

                                if (ed != null)
                                {

                                    ed.setOnlineStatus (_this.getOnlineStatus (p));

                                    EditorsEnvironment.fireEditorChangedEvent (new EditorChangedEvent (ed,
                                                                                                       EditorChangedEvent.EDITOR_CHANGED));

                                }

                            } catch (Exception e) {

                                Environment.logError ("Unable to update editor",
                                                      e);

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

                        EditorsUIUtils.showLoginError (getUIString (editors,login,errors,other));
                        //"Unable to login to the Editors service, please contact Quoll Writer support for assistance.");

                    }

                }

            }

        }).start ();

    }
*/
    private static String getUsernameFromJID (String jid)
    {

        return XmppStringUtils.unescapeLocalpart (XmppStringUtils.parseLocalpart (jid));

    }

    private static String getUsernameFromJID (Jid jid)
    {

        return jid.getLocalpartOrThrow ().asUnescapedString ();

    }

/*
TODO OLD Remove?
    private static String getJID (EditorEditor ed)
    {

        return ed.getMessagingUsername () + "@" + ed.getServiceName ();

    }
*/

    private static EntityBareJid getJID (EditorEditor ed)
                                  throws XmppStringprepException
    {

        return JidCreate.entityBareFrom (ed.getMessagingUsername () + "@" + ed.getServiceName ());

    }

    private void sendUnsubscribed (EditorEditor ed)
    {

        try
        {

            BareJid jid = this.getJID (ed);

            Presence ret = new Presence (Presence.Type.unsubscribed);
            ret.setTo (jid);
            this.conn.sendStanza (ret);

        } catch (Exception e) {

            Environment.logError ("Unable to send unsubscribed presence to: " +
                                  ed,
                                  e);

        }

    }

    private void sendPresence (Presence.Type type,
                               Jid           to)
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

            BareJid jid = this.getJID (ed);

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

        new Thread (() ->
        {

            try
            {

                BareJid jid = _this.getJID (ed);

                _this.sendPresence (Presence.Type.unsubscribe,
                                    jid);
                _this.sendPresence (Presence.Type.unsubscribed,
                                    jid);

            } catch (Exception e) {

                Environment.logError ("Unable to unsubscribe from editor: " + ed,
                                      e);

            }

        }).start ();

    }

    public void subscribeToEditor (final EditorEditor          ed)
    {

        final EditorsMessageHandler _this = this;

        new Thread (() ->
        {

            try
            {

                BareJid jid = _this.getJID (ed);

                Roster.getInstanceFor (_this.conn).createEntry (jid,
                                                                null,
                                                                null);

                _this.sendPresence (Presence.Type.subscribe,
                                    jid);


                //_this.sendMyPublicKeyToEditor (ed);

            } catch (Exception e) {

                ComponentUtils.showErrorMessage (new SimpleStringProperty ("Unable to subscribe to editor: " + ed.getEmail ()));

                Environment.logError ("Unable to subscribe to editor: " + ed,
                                      e);

            }

        }).start ();

    }

    public void sendMessage (final StringProperty loginReason,
                             final EditorMessage  mess,
                             final EditorEditor   to,
                             final Runnable       onSend,
                             final Runnable       onLoginCancel)
    {

        this.sendMessage (loginReason,
                          mess,
                          to,
                          onSend,
                          onLoginCancel,
                          null);

    }

    public void sendMessage (final StringProperty        loginReason,
                             final EditorMessage         mess,
                             final EditorEditor          to,
                             final Runnable              onSend,
                             final Runnable              onLoginCancel,
                             final java.util.function.Consumer<Exception>   onError)
    {

        final EditorsMessageHandler _this = this;

        this.logoutRequested = false;

        if (to.isPrevious ())
        {

            Environment.logError ("Trying to send message to previous editor: " + to + ", message is: " + mess);

            if (onError != null)
            {

                Environment.scheduleImmediately (() ->
                {

                    onError.accept (new Exception ("Trying to send message to: " + to));

                });

            }

            return;

        }

        this.doLogin (loginReason,
                      () ->
                      {

                        // TODO: Next release change this to be more in context.
                        //EditorsEnvironment.showMessageSendWarningIfEditorOfflineMessage (to);

                        if (_this.messageSendInProgressCount.intValue () < 0)
                        {

                            _this.messageSendInProgressCount.set (0);

                        }

                        _this.messageSendInProgressCount.incrementAndGet ();

                        Environment.scheduleImmediately (() ->
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

                                if (dmess.length () > Constants.EDITORS_SERVICE_MAX_MESSAGE_SIZE)
                                {

                                    throw new GeneralException ("Message is too large, size is: " +
                                                                dmess.length () +
                                                                ", max is: " + Constants.EDITORS_SERVICE_MAX_MESSAGE_SIZE);

                                }

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

                                    Environment.scheduleImmediately (onSend);

                                }

                            } catch (Exception e) {

                                if (onError != null)
                                {

                                    Environment.scheduleImmediately (() ->
                                    {

                                        onError.accept (new Exception ("Unable to send message to " + to,
                                                                       e));

                                    });

                                } else {

                                    ComponentUtils.showErrorMessage (getUILanguageStringProperty (Arrays.asList (editors,messages,send,actionerror),
                                                                                                  (to.getName () != null ? to.getName () : to.getEmail ())));
                                        //"Unable to send message to <b>" + to.getName () + "</b>.  Please contact Quoll Writer support for assistance.");

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

                        });

                      },
                      onLoginCancel);

    }

}
