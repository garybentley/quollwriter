package com.quollwriter.editors;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.awt.image.*;
import java.net.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.sql.*;

import javafx.collections.*;
import javafx.scene.input.*;
import javafx.scene.*;
import javafx.beans.property.*;
import javafx.scene.image.*;

import org.bouncycastle.openpgp.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;
import com.quollwriter.db.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.*;

import com.quollwriter.ui.fx.components.ComponentUtils;
import com.quollwriter.ui.fx.components.Notification;
import com.quollwriter.ui.fx.components.QuollPopup;
import com.quollwriter.ui.fx.StyleClassNames;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class EditorsEnvironment
{

    private static EditorsObjectManager editorsManager = null;
    private static EditorsWebServiceHandler editorsHandler = null;
    private static EditorsMessageHandler messageHandler = null;
    private static EditorAccount editorAccount = null;
    //private static boolean hasRegisteredForEditorService = false;
    public static boolean serviceAvailable = true;
    private static ObservableSet<EditorEditor> editors = FXCollections.observableSet (new HashSet<> ());
    private static ObservableList<EditorEditor> invitesForMe = FXCollections.observableList (new ArrayList<> ());
    private static ObservableList<EditorEditor> invitesIveSent = FXCollections.observableList (new ArrayList<> ());
    private static ObservableList<EditorEditor> currentEditors = FXCollections.observableList (new ArrayList<> ());
    private static ObservableList<EditorEditor> previousEditors = FXCollections.observableList (new ArrayList<> ());

    // TODO: This is *NOT* the way to handle this but is ok for now and saves faffing with dirs and files.
    protected static int    schemaVersion = 0;
    private static com.gentlyweb.properties.Properties editorsProps = new com.gentlyweb.properties.Properties ();
    //private static EditorEditor.OnlineStatus currentOnlineStatus = EditorEditor.OnlineStatus.offline;
    private static ObjectProperty<EditorEditor.OnlineStatus> userOnlineStatusProp = new SimpleObjectProperty (EditorEditor.OnlineStatus.offline);
    private static EditorEditor.OnlineStatus lastOnlineStatus = null;

    private static Map<EditorChangedListener, Object> editorChangedListeners = null;
    private static Map<ProjectEditorChangedListener, Object> projectEditorChangedListeners = null;
    private static Map<EditorMessageListener, Object> editorMessageListeners = null;
    private static Map<UserOnlineStatusListener, Object> userStatusListeners = null;
    private static Map<EditorInteractionListener, Object> editorInteractionListeners = null;

    private static boolean startupLoginTried = false;

    // Just used in the maps above as a placeholder for the listeners.
    private static final Object listenerFillObj = new Object ();

    // We create the listener containers here to allow others to listen for events even though the service
    // may not be running or ever available.  This is sort of wasteful but prevents clients having to bother
    // about checking for the service being available, they just won't receive events.
    static
    {

        // We use a synchronized weak hash map here so that we don't have to worry about all the
        // references since they will be transient compared to the potential length of the service
        // running.

        // Where possible listeners should de-register as normal but this just ensure that objects
        // that don't have a controlled pre-defined lifecycle (as opposed say to AbstractSideBar)
        // won't leak.
        EditorsEnvironment.editorChangedListeners = Collections.synchronizedMap (new WeakHashMap ());

        EditorsEnvironment.projectEditorChangedListeners = Collections.synchronizedMap (new WeakHashMap ());

        EditorsEnvironment.editorMessageListeners = Collections.synchronizedMap (new WeakHashMap ());

        EditorsEnvironment.userStatusListeners = Collections.synchronizedMap (new WeakHashMap ());

        EditorsEnvironment.editorInteractionListeners = Collections.synchronizedMap (new WeakHashMap<> ());

    }

    public static void logEditorMessages (boolean v)
    {

        EditorsEnvironment.messageHandler.logMessages (v);

    }

    public static void init (com.gentlyweb.properties.Properties props)
                      throws Exception
    {

        if (!EditorsEnvironment.serviceAvailable)
        {

            return;

        }

        if (props == null)
        {

            throw new GeneralException ("Properties must be provided.");

        }

        EditorsEnvironment.editorsProps = props;

        try
        {

            EditorsEnvironment.schemaVersion = Integer.parseInt (Utils.getResourceFileAsString (Constants.EDITORS_SCHEMA_VERSION_FILE).trim ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to read editors schema version file",
                                  e);

            EditorsEnvironment.serviceAvailable = false;

            return;

        }

        EditorsEnvironment.editorsHandler = new EditorsWebServiceHandler ();
        EditorsEnvironment.editorsHandler.init ();

        EditorsEnvironment.messageHandler = new EditorsMessageHandler ();

        //EditorsEnvironment.logEditorMessages (true);


        EditorsEnvironment.messageHandler.init ();

        String eddir = EditorsEnvironment.editorsProps.getProperty (Constants.QW_EDITORS_DB_DIR_PROPERTY_NAME);

        if (eddir != null)
        {

            File dir = new File (eddir);

            if ((dir.exists ())
                &&
                (dir.isDirectory ())
               )
            {

                EditorsEnvironment.initDB (dir);

            }

        }

        // Bit of spelunking anyone???
        Environment.startupCompleteProperty ().addListener ((p, oldv, newv) ->
        {

            if ((newv)
                &&
                (!EditorsEnvironment.startupLoginTried)
               )
            {

                if (EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME))
                {

                    Environment.scheduleImmediately (() ->
                    {

                        String pwd = EditorsEnvironment.getEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME);

                        String email = null;

                        if (EditorsEnvironment.editorAccount != null)
                        {

                            email = EditorsEnvironment.editorAccount.getEmail ();

                        }

                        if (email == null)
                        {

                            // Can't login, no account, this can happen if the user used to have an account and has
                            // deleted it with the setting enabled.
                            return;

                        }

                        // Add a notification to the project viewer saying we are logging in.
                        final AbstractViewer viewer = Environment.getFocusedViewer ();

                        Notification _n = null;

                        // We may not have a viewer if we are opening an encrypted project.
                        if (viewer != null)
                        {

                            _n = viewer.addNotification (getUILanguageStringProperty (LanguageStrings.editors,login,auto,notification),
                                                        //"Logging in to the Editors service...",
                                                         Constants.EDITORS_ICON_NAME,
                                                         30);

                        }

                        final Notification n = _n;

                        EditorsEnvironment.setLoginCredentials (email,
                                                                pwd);

                        EditorsEnvironment.startupLoginTried = true;

                        EditorsEnvironment.goOnline (null,
                                                     () ->
                        {

                            if (viewer != null)
                            {

                                viewer.removeNotification (n);

                            }

                        },
                        // On cancel
                        () ->
                        {

                            if (viewer != null)
                            {

                                viewer.removeNotification (n);

                            }

                        },
                        // On error
                        exp ->
                        {

                            EditorsEnvironment.setLoginCredentials (EditorsEnvironment.editorAccount.getEmail (),
                                                                    null);

                            if (viewer != null)
                            {

                                viewer.removeNotification (n);

                            }

                            EditorsUIUtils.showLoginError (getUILanguageStringProperty (LanguageStrings.editors,login,auto,actionerror),
                                                            //"Unable to automatically login, please check your email and password.",
                                                           () ->
                                                           {

                                                                EditorsEnvironment.goOnline (null,
                                                                                             null,
                                                                                             null,
                                                                                             null);

                                                           },
                                                           null);

                        });

                    });

                }

            }

        });

    }

    public static void removeEditorInteractionListener (EditorInteractionListener l)
    {

        EditorsEnvironment.editorInteractionListeners.remove (l);

    }

    public static void addEditorInteractionListener (EditorInteractionListener l)
    {

        EditorsEnvironment.editorInteractionListeners.put (l,
                                                           EditorsEnvironment.listenerFillObj);

    }

    public static void fireEditorInteractionEvent (final EditorEditor              ed,
                                                   final InteractionMessage.Action action)
    {

        // TODO Get rid?
        UIUtils.runLater (() ->
        {

            Set<EditorInteractionListener> ls = null;

            EditorInteractionEvent ev = new EditorInteractionEvent (ed,
                                                                    action);

            // Get a copy of the current valid listeners.
            synchronized (EditorsEnvironment.editorInteractionListeners)
            {

                ls = new LinkedHashSet<> (EditorsEnvironment.editorInteractionListeners.keySet ());

            }

            for (EditorInteractionListener l : ls)
            {

                l.handleInteraction (ev);

            }

        });

    }

    public static void removeUserOnlineStatusListener (UserOnlineStatusListener l)
    {

        EditorsEnvironment.userStatusListeners.remove (l);

    }

    public static void addUserOnlineStatusListener (UserOnlineStatusListener l)
    {

        EditorsEnvironment.userStatusListeners.put (l,
                                                    EditorsEnvironment.listenerFillObj);

    }

    private static void fireUserOnlineStatusChanged (final EditorEditor.OnlineStatus status)
    {

        // TODO Get rid?
        UIUtils.runLater (() ->
        {

            Set<UserOnlineStatusListener> ls = null;

            UserOnlineStatusEvent ev = new UserOnlineStatusEvent (status);

            // Get a copy of the current valid listeners.
            synchronized (EditorsEnvironment.userStatusListeners)
            {

                ls = new LinkedHashSet (EditorsEnvironment.userStatusListeners.keySet ());

            }

            for (UserOnlineStatusListener l : ls)
            {

                l.userOnlineStatusChanged (ev);

            }

        });

    }

    public static void setDefaultUserOnlineStatus ()
    {

        EditorEditor.OnlineStatus status = null;

        String defOnlineStatus = EditorsEnvironment.getEditorsProperty (Constants.QW_EDITORS_SERVICE_DEFAULT_ONLINE_STATUS_PROPERTY_NAME);

        if (defOnlineStatus != null)
        {

            try
            {

                status = EditorEditor.OnlineStatus.valueOf (defOnlineStatus);

            } catch (Exception e) {

                Environment.logError ("Unable to set default online status to: " +
                                      defOnlineStatus,
                                      e);

            }

        }

        if (status == null)
        {

            status = EditorEditor.OnlineStatus.online;

        }

        try
        {

            EditorsEnvironment.setUserOnlineStatus (status);

        } catch (Exception e) {

            Environment.logError ("Unable to set default online status to: " +
                                  status,
                                  e);

        }

    }

    public static void setUserOnlineStatus (EditorEditor.OnlineStatus status)
                                     throws Exception
    {

        if (!EditorsEnvironment.messageHandler.isLoggedIn ())
        {

            throw new IllegalStateException ("Cant set user online status if they are not logged in.");

        }

        if (EditorsEnvironment.userOnlineStatusProp.getValue () == status)
        {

            return;

        }

        EditorsEnvironment.userOnlineStatusProp.setValue (status);

        // Send the presence.
        EditorsEnvironment.messageHandler.setOnlineStatus (status);

        EditorsEnvironment.fireUserOnlineStatusChanged (EditorsEnvironment.userOnlineStatusProp.getValue ());

    }

    public static EditorEditor.OnlineStatus getUserOnlineStatus ()
    {

        return EditorsEnvironment.userOnlineStatusProp.getValue ();

    }

    public static ObjectProperty<EditorEditor.OnlineStatus> userOnlineStatusProperty ()
    {

        return EditorsEnvironment.userOnlineStatusProp;

    }

    public static boolean isUserLoggedIn ()
    {

        return EditorsEnvironment.messageHandler.isLoggedIn ();

    }

    public static boolean isEditorsDBDir (Path dir)
    {

        return EditorsEnvironment.isEditorsDBDir (dir.toFile ());

    }

    public static boolean isEditorsDBDir (File dir)
    {

        if (dir == null)
        {

            return false;

        }

        if (dir.isFile ())
        {

            return false;

        }

        if (!dir.exists ())
        {

            return false;

        }

        File f = new File (dir, Constants.EDITORS_DB_FILE_NAME_PREFIX + Constants.H2_DB_FILE_SUFFIX);

        if ((f.exists ())
            &&
            (f.isFile ())
           )
        {

            return true;

        }

        return false;

    }

    public static void initDB (Path dir)
                        throws Exception
    {

        EditorsEnvironment.initDB (dir.toFile ());

    }

    public static void initDB (File   dir)
                               throws Exception
    {

        if (EditorsEnvironment.editorsProps == null)
        {

            throw new IllegalStateException ("Editors properties has not yet been set, try init(Properties) first.");

        }

        // Get the username and password.
        String username = EditorsEnvironment.editorsProps.getProperty (Constants.DB_USERNAME_PROPERTY_NAME);
        String password = EditorsEnvironment.editorsProps.getProperty (Constants.DB_PASSWORD_PROPERTY_NAME);

        EditorsEnvironment.editorsManager = new EditorsObjectManager ();

        File f = EditorsEnvironment.getEditorsDBFile (dir);

        EditorsEnvironment.editorsManager.init (f,
                                                username,
                                                password,
                                                null,
                                                EditorsEnvironment.getSchemaVersion (dir));

        // Create a file that indicates that the directory can be deleted.
        Utils.createQuollWriterDirFile (dir);

        EditorsEnvironment.editorAccount = EditorsEnvironment.editorsManager.getUserAccount ();

        if (EditorsEnvironment.editorAccount != null)
        {

            // Load up the editors.
            Set<EditorEditor> eds = new LinkedHashSet<> ((List<EditorEditor>) EditorsEnvironment.editorsManager.getObjects (EditorEditor.class,
                                                                                                                            null,
                                                                                                                            null,
                                                                                                                            true));
            EditorsEnvironment.editors = FXCollections.observableSet (eds);

            for (EditorEditor ed : EditorsEnvironment.editors)
            {

                if (ed.isPrevious ())
                {

                    EditorsEnvironment.previousEditors.add (ed);
                    continue;

                }

                if (ed.isRejected ())
                {

                    continue;

                }

                if (ed.isPending ())
                {

                    if (!ed.isInvitedByMe ())
                    {

                        EditorsEnvironment.invitesForMe.add (ed);

                    } else {

                        EditorsEnvironment.invitesIveSent.add (ed);

                    }

                } else {

                    EditorsEnvironment.currentEditors.add (ed);

                }

            }

        }

    }

    public static boolean hasUserSentAProjectBefore ()
                                              throws Exception
    {

        if (EditorsEnvironment.editorsManager == null)
        {

            return false;

        }

        return EditorsEnvironment.editorsManager.hasUserSentAProjectBefore ();

    }

    public static void fireEditorMessageEvent (final EditorMessageEvent ev)
    {

        // TODO Get rid?
        UIUtils.runLater (() ->
        {

            Set<EditorMessageListener> ls = null;

            // Get a copy of the current valid listeners.
            synchronized (EditorsEnvironment.editorMessageListeners)
            {

                ls = new LinkedHashSet (EditorsEnvironment.editorMessageListeners.keySet ());

            }

            for (EditorMessageListener l : ls)
            {

                l.handleMessage (ev);

            }

        });

    }

    public static NewProjectMessage getNewProjectMessage (EditorEditor ed,
                                                          String       projectId,
                                                          boolean      sentByMe)
                                                   throws Exception
    {

        return EditorsEnvironment.editorsManager.getNewProjectMessage (ed,
                                                                       projectId,
                                                                       sentByMe);

    }

    public static boolean hasMyPublicKeyBeenSentToEditor (EditorEditor ed)
                                                   throws Exception
    {

        return EditorsEnvironment.editorsManager.hasMyPublicKeyBeenSentToEditor (ed);

    }

    public static boolean hasSentMessageOfTypeToEditor (EditorEditor ed,
                                                        String       messageType)
                                                 throws Exception
    {

        return EditorsEnvironment.editorsManager.hasSentMessageOfTypeToEditor (ed,
                                                                               messageType);

    }

    public static boolean hasEditorSentInfo (EditorEditor ed)
                                      throws Exception
    {

        if (ed.messagesLoaded ())
        {

            return ed.hasSentInfo ();

        }

        return EditorsEnvironment.editorsManager.hasEditorSentInfo (ed);

    }

    public static void removeEditorMessageListener (EditorMessageListener l)
    {

        EditorsEnvironment.editorMessageListeners.remove (l);

    }

    public static void addEditorMessageListener (EditorMessageListener l)
    {

        EditorsEnvironment.editorMessageListeners.put (l,
                                                       EditorsEnvironment.listenerFillObj);

    }

    public static void fireEditorChangedEvent (EditorEditor ed,
                                               int          changeType)
    {

        EditorsEnvironment.fireEditorChangedEvent (new EditorChangedEvent (ed,
                                                                           changeType));

    }

    public static void fireEditorChangedEvent (final EditorChangedEvent ev)
    {

        // TODO Get rid?
        UIUtils.runLater (() ->
        {

            Set<EditorChangedListener> ls = null;

            // Get a copy of the current valid listeners.
            synchronized (EditorsEnvironment.editorChangedListeners)
            {

                ls = new LinkedHashSet (EditorsEnvironment.editorChangedListeners.keySet ());

            }

            for (EditorChangedListener l : ls)
            {

                l.editorChanged (ev);

            }

        });

    }

    public static void removeEditorChangedListener (EditorChangedListener l)
    {

        EditorsEnvironment.editorChangedListeners.remove (l);

    }

    public static void addEditorChangedListener (EditorChangedListener l)
    {

        EditorsEnvironment.editorChangedListeners.put (l,
                                                       EditorsEnvironment.listenerFillObj);

    }

    public static void fireProjectEditorChangedEvent (ProjectEditor pe,
                                                      int          changeType)
    {

        EditorsEnvironment.fireProjectEditorChangedEvent (new ProjectEditorChangedEvent (pe,
                                                                                         changeType));

    }

    public static void fireProjectEditorChangedEvent (final ProjectEditorChangedEvent ev)
    {

        // TODO Get rid?
        UIUtils.runLater (() ->
        {

            Set<ProjectEditorChangedListener> ls = null;

            // Get a copy of the current valid listeners.
            synchronized (EditorsEnvironment.projectEditorChangedListeners)
            {

                ls = new LinkedHashSet (EditorsEnvironment.projectEditorChangedListeners.keySet ());

            }

            for (ProjectEditorChangedListener l : ls)
            {

                l.projectEditorChanged (ev);

            }

        });

    }

    public static void removeProjectEditorChangedListener (ProjectEditorChangedListener l)
    {

        EditorsEnvironment.projectEditorChangedListeners.remove (l);

    }

    public static void addProjectEditorChangedListener (ProjectEditorChangedListener l)
    {

        EditorsEnvironment.projectEditorChangedListeners.put (l,
                                                              EditorsEnvironment.listenerFillObj);

    }

    public static boolean isEditorsServiceAvailable ()
    {

        return EditorsEnvironment.serviceAvailable;

    }

    public static void setUserInformation (String name,
                                           Image  avatarImage)
                                    throws GeneralException
    {

        if (EditorsEnvironment.editorsManager == null)
        {

            throw new IllegalStateException ("Editor object manager not inited.");

        }
/*
TODO Remove
        if (avatarImage != null)
        {

            if (avatarImage.getWidth () > 300)
            {

                avatarImage = UIUtils.getScaledImage (avatarImage,
                                                      300);

            }

        }
*/
        EditorsEnvironment.editorAccount.setName (name);
        EditorsEnvironment.editorAccount.setAvatar (avatarImage);

        EditorsEnvironment.editorsManager.setUserInformation (EditorsEnvironment.editorAccount);

    }

    private static File getEditorsDBFile (File dir)
    {

        return new File (dir, Constants.EDITORS_DB_FILE_NAME_PREFIX);

    }

    public static int getSchemaVersion (File dir)
    {

        File dbf = new File (EditorsEnvironment.getEditorsDBFile (dir).getPath () + Constants.H2_DB_FILE_SUFFIX);

        if (!dbf.exists ())
        {

            return 0;

        }

        return EditorsEnvironment.schemaVersion;

    }

    public static EditorAccount getUserAccount ()
    {

        return EditorsEnvironment.editorAccount;

    }

    public static EditorsMessageHandler getMessageHandler ()
    {

        return EditorsEnvironment.messageHandler;

    }

        public static Set<String> getWritingGenres ()
    {

        String gt = EditorsEnvironment.editorsProps.getProperty (Constants.WRITING_GENRES_PROPERTY_NAME);

        Set<String> gitems = new LinkedHashSet ();

        StringTokenizer t = new StringTokenizer (gt,
                                                 ",");

        while (t.hasMoreTokens ())
        {

            gitems.add (t.nextToken ().trim ());

        }

        return gitems;

    }

    public static EditorsWebServiceHandler getEditorsWebServiceHandler ()
    {

        return EditorsEnvironment.editorsHandler;

    }

    public static Set<EditorMessage> getAllUndealtWithMessages ()
                                                         throws Exception
    {

        if (EditorsEnvironment.editorsManager == null)
        {

            return null;

        }

        return EditorsEnvironment.editorsManager.getAllUndealtWithMessages ();

    }

    public static Set<EditorMessage> getProjectMessages (String    projId,
                                                         String... messageTypes)
                                                  throws Exception
    {

        return EditorsEnvironment.editorsManager.getProjectMessages (projId,
                                                                     messageTypes);

    }

    /**
     * Get a count of all the messages the user hasn't dealt with.
     *
     * @return The count.
     */
    public static int getUndealtWithMessageCount ()
                                           throws Exception
    {

        if (EditorsEnvironment.editorsManager == null)
        {

            return 0;

        }

        return EditorsEnvironment.editorsManager.getUndealtWithMessageCount ();

    }

    public static File getEditorsAuthorAvatarImageFile (String suffix)
    {

        if (suffix == null)
        {

            return null;

        }

        File f = new File (Environment.getUserQuollWriterDirPath ().toFile (),
                           Constants.EDITORS_AUTHOR_AVATAR_IMAGE_FILE_NAME_PREFIX + "." + suffix);

        f.getParentFile ().mkdirs ();

        return f;

    }

    public static File getEditorsAuthorAvatarImageFile ()
    {

        if (EditorsEnvironment.editorsHandler == null)
        {

            return null;

        }

        if (EditorsEnvironment.editorAccount == null)
        {

            return null;

        }

        EditorAuthor au = EditorsEnvironment.editorAccount.getAuthor ();

        if (au == null)
        {

            return null;

        }

        String t = EditorsEnvironment.editorsProps.getProperty (Constants.EDITORS_AUTHOR_AVATAR_IMAGE_FILE_TYPE_PROPERTY_NAME);

        return EditorsEnvironment.getEditorsAuthorAvatarImageFile (t);

    }

    public static File getEditorsAuthorFile ()
    {

        File f = new File (Environment.getUserQuollWriterDirPath ().toFile (),
                           Constants.EDITORS_AUTHOR_FILE_NAME);

        f.getParentFile ().mkdirs ();

        return f;

    }

    public static File getEditorsEditorAvatarImageFile (String suffix)
    {

        if (suffix == null)
        {

            return null;

        }

        File f = new File (Environment.getUserQuollWriterDirPath ().toFile (),
                           Constants.EDITORS_EDITOR_AVATAR_IMAGE_FILE_NAME_PREFIX + "." + suffix);

        f.getParentFile ().mkdirs ();

        return f;

    }

    public static File getEditorsEditorAvatarImageFile ()
    {

        if (EditorsEnvironment.editorsHandler == null)
        {

            return null;

        }

        if (EditorsEnvironment.editorAccount == null)
        {

            return null;

        }

        EditorEditor ed = EditorsEnvironment.editorAccount.getEditor ();

        if (ed == null)
        {

            return null;

        }

        String t = EditorsEnvironment.editorsProps.getProperty (Constants.EDITORS_EDITOR_AVATAR_IMAGE_FILE_TYPE_PROPERTY_NAME);

        return EditorsEnvironment.getEditorsEditorAvatarImageFile (t);

    }

    public static File getEditorsEditorFile ()
    {

        File f = new File (Environment.getUserQuollWriterDirPath ().toFile (),
                           Constants.EDITORS_EDITOR_FILE_NAME);

        f.getParentFile ().mkdirs ();

        return f;

    }

    public static void clearUserPassword ()
    {

        if (EditorsEnvironment.editorAccount != null)
        {

            EditorsEnvironment.editorAccount.setPassword (null);

        }

    }

    public static void getInvite (EditorEditor            editor,
                                  EditorsWebServiceAction onComplete,
                                  EditorsWebServiceAction onError)
    {

        EditorsEnvironment.editorsHandler.getInvite (editor,
                                                     onComplete,
                                                     onError);

    }

    public static void getInvite (String                  fromEmail,
                                  EditorsWebServiceAction onComplete,
                                  EditorsWebServiceAction onError)
    {

        EditorsEnvironment.editorsHandler.getInvite (fromEmail,
                                                     onComplete,
                                                     onError);

    }

    public static void deleteMessages (Set<EditorMessage> messages)
                                throws Exception
    {

        // TODO: Clean up the Set/List mess.
        EditorsEnvironment.editorsManager.deleteObjects (new ArrayList<EditorMessage> (messages),
                                                         null);

    }

    public static void acceptInvite (final EditorEditor  editor,
                                     final EditorMessage message,
                                     final Runnable      onComplete)
    {

        final String email = editor.getEmail ();

        EditorsEnvironment.editorsHandler.updateInvite (email,
                                                        Invite.Status.accepted,
                                                        new EditorsWebServiceAction ()
        {

            public void processResult (EditorsWebServiceResult res)
            {

                try
                {

                    editor.setEditorStatus (EditorEditor.EditorStatus.current);

                    EditorsEnvironment.updateEditor (editor);

                } catch (Exception e) {

                    Environment.logError ("Unable to update editor: " +
                                          editor +
                                          " to accepted",
                                          e);

                    ComponentUtils.showErrorMessage (getUILanguageStringProperty (LanguageStrings.editors,LanguageStrings.editor,edit,actionerror));
                                              //"Unable to update {editor} information in local database.");

                    return;

                }

                // Send an invite response message.
                EditorsEnvironment.getMessageHandler ().subscribeToEditor (editor);

                EditorsEnvironment.sendMessageToEditor (message,
                                                        onComplete,
                                                        null,
                                                        null);

            }
        },
        null);

    }

    public static void rejectInvite (final EditorEditor  editor,
                                     final EditorMessage message,
                                     final Runnable      onComplete)
    {

        final String email = editor.getEmail ();

        EditorsEnvironment.editorsHandler.updateInvite (email,
                                                        Invite.Status.rejected,
                                                        new EditorsWebServiceAction ()
        {

            public void processResult (EditorsWebServiceResult res)
            {

                try
                {

                    editor.setEditorStatus (EditorEditor.EditorStatus.rejected);

                    EditorsEnvironment.updateEditor (editor);

                } catch (Exception e) {

                    ComponentUtils.showErrorMessage (getUILanguageStringProperty (LanguageStrings.editors,LanguageStrings.editor,edit,actionerror));
                                              //"Unable to update {editor} information in local database.");

                    Environment.logError ("Unable to update editor: " +
                                          editor +
                                          " to rejected",
                                          e);

                    return;

                }

                EditorsEnvironment.sendMessageToEditor (message,
                                                        onComplete,
                                                        null,
                                                        null);

            }
        },
        null);

    }

    public static void updateInvite (final String        email,
                                     final Invite.Status newStatus,
                                     final Runnable      onUpdateComplete)
    {

        EditorsEnvironment.editorsHandler.updateInvite (email,
                                                        newStatus,
                                                        new EditorsWebServiceAction ()
        {

            public void processResult (EditorsWebServiceResult res)
            {

                EditorEditor ed = EditorsEnvironment.getEditorByEmail (email);

                EditorEditor.EditorStatus status = null;

                if (newStatus == Invite.Status.accepted)
                {

                    status = EditorEditor.EditorStatus.current;

                } else {

                    status = EditorEditor.EditorStatus.valueOf (newStatus.getType ());

                }

                try
                {

                    if (ed != null)
                    {

                        ed.setEditorStatus (status);

                        EditorsEnvironment.updateEditor (ed);

                    } else {

                        ed = new EditorEditor ();

                        ed.setEmail (email);
                        ed.setEditorStatus (status);

                        // Add to our editors.
                        EditorsEnvironment.addNewEditor (ed);

                    }

                } catch (Exception e) {

                    Environment.logError ("Unable to update editor: " +
                                          email +
                                          " to status: " +
                                          status,
                                          e);

                    ComponentUtils.showErrorMessage (getUILanguageStringProperty (LanguageStrings.editors,editor,edit,actionerror));
                                              //"Unable to update {editor} information in local database.");

                    return;

                }

                if (onUpdateComplete != null)
                {

                    Environment.scheduleImmediately (onUpdateComplete);

                }

            }
        },
        null);

    }

    public static void closeDown ()
    {

        EditorsEnvironment.goOffline ();

        // Close all the db connections.
        if (EditorsEnvironment.editorsManager != null)
        {

            EditorsEnvironment.editorsManager.closeConnectionPool ();

        }

    }
    public static void goOffline ()
    {

        EditorsEnvironment.editorsHandler.logout ();

        EditorsEnvironment.messageHandler.logout (null);

        EditorsEnvironment.userOnlineStatusProp.setValue (EditorEditor.OnlineStatus.offline);

        EditorsEnvironment.fireUserOnlineStatusChanged (EditorsEnvironment.userOnlineStatusProp.getValue ());

        for (EditorEditor ed : EditorsEnvironment.editors)
        {

            ed.setOnlineStatus (null);

            EditorsEnvironment.fireEditorChangedEvent (new EditorChangedEvent (ed,
                                                                               EditorChangedEvent.EDITOR_CHANGED));

        }

    }

    public static boolean isMessageSendInProgress ()
    {

        return EditorsEnvironment.messageHandler.isMessageSendInProgress ();

    }

    public static void updateUserPassword (final String newPassword)
    {

        EditorsEnvironment.editorsHandler.changePassword (newPassword,
                                                          new EditorsWebServiceAction ()
        {

            @Override
            public void processResult (EditorsWebServiceResult res)
            {

                EditorsEnvironment.messageHandler.changePassword (newPassword,
                                                                  () ->
                                                                  {

                                                                      try
                                                                      {

                                                                          EditorsEnvironment.editorAccount.setPassword (newPassword);

                                                                          if (EditorsEnvironment.getEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME) != null)
                                                                          {

                                                                              EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME,
                                                                                                                     newPassword);

                                                                          }

                                                                      } catch (Exception e) {

                                                                          Environment.logError ("Unable to update password",
                                                                                                e);

                                                                      }

                                                                      UIUtils.runLater (() ->
                                                                      {

                                                                          QuollPopup.messageBuilder ()
                                                                              .inViewer (Environment.getFocusedViewer ())
                                                                              .title (LanguageStrings.editors,user,edit,password,confirmpopup,title)
                                                                              .message (LanguageStrings.editors,user,edit,password,confirmpopup,text)
                                                                              .closeButton ()
                                                                              .build ();

                                                                      });

                                                                  },
                                                                  null,
                                                                  exp ->
                                                                  {

                                                                      ComponentUtils.showErrorMessage (getUILanguageStringProperty (LanguageStrings.editors,user,edit,password,actionerror));
                                                                                                //"Unable to update your password, please contact Quoll Writer support for assistance.");

                                                                  });

            }

        },
        new EditorsWebServiceAction ()
        {

            @Override
            public void processResult (EditorsWebServiceResult res)
            {

                ComponentUtils.showErrorMessage (getUILanguageStringProperty (LanguageStrings.editors,user,edit,password,actionerror));

            }

        });

    }

    private static void checkForUndealtWithMessages ()
    {

        final AbstractViewer viewer = Environment.getFocusedViewer ();

        if (viewer == null)
        {

            return;

        }

        if (!viewer.isEditorsSideBarVisible ())
        {

            int c = 0;

            try
            {

                c = EditorsEnvironment.getUndealtWithMessageCount ();

            } catch (Exception e) {

                Environment.logError ("Unable to get undealt with message count",
                                      e);

                return;

            }

            if (c > 0)
            {

/*
                String s = "There are <b>%s</b> {Editor} messages requiring your attention.";

                if (c == 1)
                {

                    s = "There is <b>1</b> {Editor} message that requires your attention.";

                }
*/

                int _c = c;

                //xxx get notification by name.
                UIUtils.runLater (() ->
                {

                    final Notification n = viewer.addNotification (getUILanguageStringProperty (Arrays.asList (LanguageStrings.editors,messages,undealtwith,notification),
                                                                    //+ "  <a href='action:showundealtwitheditormessages'>Click here to view the message(s).</a>",
                                                                                                _c),
                                                                   StyleClassNames.EDITORS,
                                                                   60);

                    Node nn = n.getContent ();

                    nn.addEventHandler (MouseEvent.MOUSE_CLICKED,
                                        ev ->
                    {

                        viewer.removeNotification (n);

                    });

                });

            }

        }

    }

    private static void startMessageNotificationThread ()
    {

        // Start the listener for messages.
        // Display a notification is there are undealt with messages.
        // Only show if the editors side bar isn't visible.
        Environment.schedule (() ->
        {

            // TODO Remove t.setName ("editors-service-check-for-undealt-with-messages");
            //t.setPriority (Thread.MIN_PRIORITY);

            if (EditorsEnvironment.editorAccount != null)
            {

                try
                {

                    EditorsEnvironment.checkForUndealtWithMessages ();

                } catch (Exception e) {

                    Environment.logError ("Unable to get undealt with messages",
                                          e);

                }

            }

        },
        1,
        10 * Constants.MIN_IN_MILLIS);

    }

    public static void goOnline (final StringProperty loginReason,
                                 final Runnable       onLogin,
                                 final Runnable       onCancel,
                                 final Consumer<Exception> onError)
    {

        final com.quollwriter.ui.fx.viewers.AbstractViewer viewer = Environment.getFocusedViewer ();

        final StringProperty reason = (loginReason != null ? loginReason : getUILanguageStringProperty (LanguageStrings.editors,login,reasons,_default));
        //"To go online you must first login.");

        Runnable login = () ->
        {

            EditorsEnvironment.editorsHandler.login (() ->
            {

                EditorsEnvironment.messageHandler.login (() ->
                {

                    EditorsUIUtils.hideLogin ();

                    try
                    {

                        EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_HAS_LOGGED_IN_PROPERTY_NAME,
                                                               true);

                    } catch (Exception e) {

                        Environment.logError ("Unable to set property",
                                              e);

                    }

                    if (EditorsEnvironment.editorAccount.getLastLogin () == null)
                    {

                        EditorsEnvironment.getEditorsWebServiceHandler ().checkPendingInvites ();

                    }

                    EditorsEnvironment.setDefaultUserOnlineStatus ();

                            //EditorsEnvironment.currentOnlineStatus = EditorEditor.OnlineStatus.online;

                            //EditorsEnvironment.fireUserOnlineStatusChanged (EditorsEnvironment.currentOnlineStatus);

                    java.util.Date d = new java.util.Date ();

                    EditorsEnvironment.editorAccount.setLastLogin (d);

                    try
                    {

                        EditorsEnvironment.editorsManager.setLastLogin (d);

                    } catch (Exception e) {

                        Environment.logError ("Unable to set last login date",
                                              e);

                    }

                    EditorsEnvironment.startMessageNotificationThread ();

                    if (onLogin != null)
                    {

                        Environment.scheduleImmediately (onLogin);

                    }

                },
                onError);

            },
            onError);

        };

        if (EditorsEnvironment.hasLoginCredentials ())
        {

            Environment.scheduleImmediately (login);

        } else {

            EditorsUIUtils.showLogin (viewer,
                                      reason,
                                      login,
                                      onCancel);

        }

    }
/*
TODO Remove
    public static void goOnline (final String         loginReason,
                                 final ActionListener onLogin,
                                 final ActionListener onCancel,
                                 final ActionListener onError)
    {

        final AbstractViewer viewer = null; // TODO Environment.getFocusedViewer ();

        final String reason = (loginReason != null ? loginReason : getUIString (LanguageStrings.editors,login,reasons,_default));
        //"To go online you must first login.");

        ActionListener login = new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                EditorsEnvironment.editorsHandler.login (new ActionListener ()
                                                         {

                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                EditorsEnvironment.messageHandler.login (new ActionListener ()
                                                                                                         {

                                                                                                            public void actionPerformed (ActionEvent ev)
                                                                                                            {

                                                                                                                EditorsUIUtils.hideLogin ();

                                                                                                                try
                                                                                                                {

                                                                                                                    EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_HAS_LOGGED_IN_PROPERTY_NAME,
                                                                                                                                                           true);

                                                                                                                } catch (Exception e) {

                                                                                                                    Environment.logError ("Unable to set property",
                                                                                                                                          e);

                                                                                                                }

                                                                                                                //EditorsEnvironment.getEditorsWebServiceHandler ().checkPendingInvites ();

                                                                                                                if (EditorsEnvironment.editorAccount.getLastLogin () == null)
                                                                                                                {

                                                                                                                    EditorsEnvironment.getEditorsWebServiceHandler ().checkPendingInvites ();

                                                                                                                }

                                                                                                                EditorsEnvironment.setDefaultUserOnlineStatus ();

                                                                                                                //EditorsEnvironment.currentOnlineStatus = EditorEditor.OnlineStatus.online;

                                                                                                                //EditorsEnvironment.fireUserOnlineStatusChanged (EditorsEnvironment.currentOnlineStatus);

                                                                                                                java.util.Date d = new java.util.Date ();

                                                                                                                EditorsEnvironment.editorAccount.setLastLogin (d);

                                                                                                                try
                                                                                                                {

                                                                                                                    EditorsEnvironment.editorsManager.setLastLogin (d);

                                                                                                                } catch (Exception e) {

                                                                                                                    Environment.logError ("Unable to set last login date",
                                                                                                                                          e);

                                                                                                                }

                                                                                                                EditorsEnvironment.startMessageNotificationThread ();

                                                                                                                if (onLogin != null)
                                                                                                                {

                                                                                                                    onLogin.actionPerformed (ev);

                                                                                                                }

                                                                                                            }

                                                                                                        },
                                                                                                        onError);

                                                            }

                                                         },
                                                         onError);

            }

        };

        if (EditorsEnvironment.hasLoginCredentials ())
        {

            login.actionPerformed (new ActionEvent ("login", 1, "login"));

        } else {

            EditorsUIUtils.showLogin (viewer,
                                      reason,
                                      login,
                                      onCancel);

        }

    }
*/
    public static void sendMessageToAllEditors (StringProperty      loginReason,
                                                EditorMessage       mess,
                                                Runnable            onSend,
                                                Runnable            onLoginCancel,
                                                Consumer<Exception> onError)
    {

        EditorsEnvironment.sendMessageToAllEditors (new ArrayDeque (EditorsEnvironment.editors),
                                                    loginReason,
                                                    mess,
                                                    onSend,
                                                    onLoginCancel,
                                                    onError);

    }

    private static void sendMessageToAllEditors (final Deque<EditorEditor> eds,
                                                 final StringProperty      loginReason,
                                                 final EditorMessage       mess,
                                                 final Runnable            onSend,
                                                 final Runnable            onLoginCancel,
                                                 final Consumer<Exception> onError)
    {

        if (eds.size () == 0)
        {

            if (onSend != null)
            {

                Environment.scheduleImmediately (onSend);

            }

        } else {

            EditorEditor ed = eds.pop ();

            if (ed.isPrevious ())
            {

                EditorsEnvironment.sendMessageToAllEditors (eds,
                                                            loginReason,
                                                            mess,
                                                            onSend,
                                                            onLoginCancel,
                                                            onError);

                return;

            }

            Runnable onSendComplete = () ->
            {

               EditorsEnvironment.sendMessageToAllEditors (eds,
                                                           loginReason,
                                                           mess,
                                                           onSend,
                                                           onLoginCancel,
                                                           onError);

            };

            EditorsEnvironment.messageHandler.sendMessage (loginReason,
                                                           mess,
                                                           ed,
                                                           onSendComplete,
                                                           onLoginCancel,
                                                           onError);

        }

    }


    public static void sendUserInformationToAllEditors (Runnable            onSend,
                                                        Runnable            onLoginCancel,
                                                        Consumer<Exception> onError)
    {

        StringProperty loginReason = getUILanguageStringProperty (LanguageStrings.editors,login,reasons,updateinfotoall);

        EditorsEnvironment.sendMessageToAllEditors (loginReason,
                                                    new EditorInfoMessage (EditorsEnvironment.getUserAccount ()),
                                                    onSend,
                                                    onLoginCancel,
                                                    onError);

    }

    public static void sendUserInformationToEditor (EditorEditor        ed,
                                                    Runnable            onSend,
                                                    Runnable            onLoginCancel,
                                                    Consumer<Exception> onError)
    {

        EditorsEnvironment.messageHandler.sendMessage (getUILanguageStringProperty (Arrays.asList (LanguageStrings.editors,login,reasons,updateinfotocontact),
                                                                                    ed.getMainName ()),
        //"To send your information to <b>" + ed.getMainName () + "</b> you must first login to the Editors Service.",
                                                       new EditorInfoMessage (EditorsEnvironment.getUserAccount ()),
                                                       ed,
                                                       onSend,
                                                       onLoginCancel,
                                                       onError);

    }

    public static void sendInteractionMessageToEditor (InteractionMessage.Action action,
                                                       EditorEditor              ed,
                                                       Runnable                  onSend)
    {

        if (!EditorsEnvironment.messageHandler.isLoggedIn ())
        {

            return;

        }

        // Only send if editor is online.
        if (ed.isOffline ())
        {

            return;

        }

        EditorsEnvironment.sendMessageToEditor (new InteractionMessage (action,
                                                                        ed),
                                                onSend,
                                                null,
                                                null);

    }

    public static void sendMessageToEditor (final EditorMessage       mess,
                                            final Runnable            onSend,
                                            final Runnable            onLoginCancel,
                                            final Consumer<Exception> onError)
    {

        EditorsEnvironment.messageHandler.sendMessage (getUILanguageStringProperty (Arrays.asList (LanguageStrings.editors,login,reasons,sendmessagetocontact),
                                                                                    mess.getEditor ().getMainName ()),
                                                        //"To send a message to <b>" + mess.getEditor ().getMainName () + "</b> you must first login to the Editors service.",
                                                       mess,
                                                       mess.getEditor (),
                                                       onSend,
                                                       onLoginCancel,
                                                       onError);

    }

    public static void sendInvite (String         toEmail,
                                   AbstractViewer viewer)
    {

        EditorsEnvironment.editorsHandler.sendInvite (toEmail,
                                                      new EditorsWebServiceAction ()
        {

            public void processResult (EditorsWebServiceResult res)
            {

                boolean add = false;

                // See if we already have this editor and this is a resend.
                EditorEditor fed = EditorsEnvironment.getEditorByEmail (toEmail);

                if (fed == null)
                {

                    fed = new EditorEditor ();
                    add = true;

                }

                final EditorEditor ed = fed;

                // Add the invite to our local editors.
                try
                {

                    ed.setEmail (toEmail.trim ().toLowerCase ());
                    ed.setInvitedByMe (true);

                    //String resS = res.getReturnObjectAsString ();

                    Invite inv = Invite.createFrom ((Map) res.getReturnObject ());
                    /*
                    if (resS != null)
                    {

                        // Need a better way of handling this.
                        if (!res.isSuccess ())
                        {

                            // This is the public key of the editor.
                            ed.setTheirPublicKey (EditorsUtils.convertToPGPPublicKey (Base64.decode (resS)));

                        }

                    }
                    */


                    ed.setTheirPublicKey (inv.getToPublicKey ());
                    ed.setMessagingUsername (inv.getToMessagingUsername ());
                    ed.setServiceName (inv.getToServiceName ());

                    if (add)
                    {

                        // Add as new.
                        EditorsEnvironment.addNewEditor (ed);

                    }

                    // If they have a public key then send an invite message.
                    if (ed.getTheirPublicKey () != null)
                    {

                        Runnable onCancel = new Runnable ()
                        {

                            private boolean inviteSent = false;

                            @Override
                            public void run ()
                            {

                                if (this.inviteSent)
                                {

                                    return;

                                }

                                this.inviteSent = true;

                                InviteMessage invite = new InviteMessage (EditorsEnvironment.editorAccount);

                                invite.setEditor (ed);

                                // Send an invite.
                                EditorsEnvironment.sendMessageToEditor (invite,
                                                                        () ->
                                                                        {

                                                                            UIUtils.runLater (() ->
                                                                            {

                                                                                List<String> prefix = Arrays.asList (LanguageStrings.editors,user,invitesent,popup);

                                                                                AbstractViewer viewer = Environment.getFocusedViewer ();

                                                                                QuollPopup.messageBuilder ()
                                                                                    .withViewer (viewer)
                                                                                    .title (Utils.newList (prefix,title))
                                                                                    .closeButton ()
                                                                                    .message (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                                                                                           toEmail))
                                                                                    .build ();

                                                                            });

                                                                        },
                                                                        null,
                                                                        null);

                           }

                        };

                        if (viewer instanceof AbstractProjectViewer)
                        {

                            UIUtils.runLater (() ->
                            {

                                List<String> prefix = Arrays.asList (LanguageStrings.editors,user,sendprojectoninvite,popup);

                                QuollPopup p = QuollPopup.questionBuilder ()
                                    .withViewer (viewer)
                                    .styleClassName (StyleClassNames.PROJECT)
                                    .title (Utils.newList (prefix,title))
                                    .message (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                                           ed.getMainName ()))
                                    .confirmButtonLabel (Utils.newList (prefix,buttons,confirm))
                                    .cancelButtonLabel (Utils.newList (prefix,buttons,cancel))
                                    .onConfirm (ev ->
                                    {

                                        EditorsUIUtils.showSendProject ((AbstractProjectViewer) viewer,
                                                                        ed,
                                                                        null);

                                    })
                                    .onCancel (ev ->
                                    {

                                        UIUtils.runLater (onCancel);

                                    })
                                    .build ();

                            });

                            return;

                        } else {

                            Environment.scheduleImmediately (onCancel);

                            return;

                        }

                    }

                } catch (Exception e) {

                    Environment.logError ("Unable to add new editor to local",
                                          e);

                    // Show an error.
                    // Can't uninvite, what to do?
                    ComponentUtils.showErrorMessage (getUILanguageStringProperty (LanguageStrings.editors,user,sendinvite,actionerror));
                                              //"An internal error has occurred while saving the invite to local storage.  Please contact Quoll Writer support for assistance.");

                    return;

                }

                QuollPopup.messageBuilder ()
                    .withViewer (viewer)
                    .title (LanguageStrings.editors,user,invitesent,popup,title)
                    .closeButton ()
                    .message (getUILanguageStringProperty (Arrays.asList (LanguageStrings.editors,user,invitesent,popup,text),
                                                           toEmail))
                    .build ();

            }

        },
        null);

    }

    public static boolean hasLoginCredentials ()
    {

        if (EditorsEnvironment.editorAccount == null)
        {

            return false;

        }

        return ((EditorsEnvironment.editorAccount.getEmail () != null)
                &&
                (EditorsEnvironment.editorAccount.getPassword () != null));

    }

    public static void setLoginCredentials (String email,
                                            String password)
    {

        if (EditorsEnvironment.editorAccount == null)
        {

            throw new IllegalStateException ("No account available.");

        }

        EditorsEnvironment.editorAccount.setEmail (email);
        EditorsEnvironment.editorAccount.setPassword (password);

    }

    public static void initUserCredentials (String        email,
                                            String        password,
                                            String        serviceName,
                                            String        messagingUsername,
                                            PGPPublicKey  publicKey,
                                            PGPPrivateKey privateKey)
                                     throws GeneralException
    {

        if (EditorsEnvironment.editorAccount != null)
        {

            throw new GeneralException ("Already have an editor account");

        }

        EditorsEnvironment.editorAccount = new EditorAccount ();

        EditorsEnvironment.editorAccount.setEmail (email);
        EditorsEnvironment.editorAccount.setPassword (password);
        EditorsEnvironment.editorAccount.setPublicKey (publicKey);
        EditorsEnvironment.editorAccount.setPrivateKey (privateKey);
        EditorsEnvironment.editorAccount.setServiceName (serviceName);
        EditorsEnvironment.editorAccount.setMessagingUsername (messagingUsername);

        EditorsEnvironment.editorsManager.setUserInformation (EditorsEnvironment.editorAccount);

    }

    public static boolean hasRegistered ()
    {

        return EditorsEnvironment.editorAccount != null;

    }

    public synchronized static void loadMessagesForEditor (EditorEditor ed)
                                                    throws GeneralException
    {

        if (ed == null)
        {

            throw new NullPointerException ("Expected an editor");

        }

        if (ed.messagesLoaded ())
        {

            return;

        }

        synchronized (ed)
        {

            // Oh java why you so kooky...
            ed.setMessages (new LinkedHashSet<EditorMessage> ((List<EditorMessage>) EditorsEnvironment.editorsManager.getObjects (EditorMessage.class,
                                                                                                            ed,
                                                                                                            null,
                                                                                                            true)));

        }

    }
    /*
    public static void sendNewProjectResponse (final NewProjectMessage mess,
                                               final boolean           accepted,
                                               final String            responseMessage)
                                        throws GeneralException
    {

        NewProjectResponseMessage res = new NewProjectResponseMessage (mess.getForProjectId (),
                                                                       accepted,
                                                                       responseMessage,
                                                                       mess.getEditor ());

        res.setDealtWith (true);

        // For now just return rejected.
        EditorsEnvironment.sendMessageToEditor (res,
                                                new ActionListener ()
                                                {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        try
                                                        {

                                                            mess.setAccepted (accepted);
                                                            mess.setDealtWith (true);
                                                            mess.setResponseMessage (responseMessage);

                                                            // Update the original message.
                                                            EditorsEnvironment.updateMessage (mess);

                                                        } catch (Exception e) {

                                                            UIUtils.showErrorMessage (null,
                                                                                      "Unable to update {project} message, please contact Quoll Writer support for assistance.");

                                                            Environment.logError ("Unable to update new project message: " +
                                                                                  mess,
                                                                                  e);

                                                        }

                                                    }

                                                },
                                                null,
                                                null);

    }
    */
    public static void sendError (EditorMessage          mess,
                                  ErrorMessage.ErrorType errorType,
                                  String                 reason)
    {

        try
        {

            EditorsEnvironment.sendMessageToEditor (new ErrorMessage (mess,
                                                                      errorType,
                                                                      reason),
                                                    null,
                                                    null,
                                                    null);

        } catch (Exception e) {

            Environment.logError ("Unable to send error message for message: " +
                                  mess.getMessageId () +
                                  " to editor: " +
                                  mess.getEditor () +
                                  " with error type: " +
                                  errorType.getType (),
                                  e);

        }

    }

    public static Map getOriginalMessageAsMap (EditorMessage m)
                                        throws Exception
    {

        if (m.isSentByMe ())
        {

            throw new IllegalArgumentException ("Not supported for messages sent by me.");

        }

        byte[] bytes = EditorsEnvironment.editorsManager.getOriginalMessage (m);

        // Decrypt first.
        try
        {

            bytes = EditorsUtils.decrypt (bytes,
                                          EditorsEnvironment.editorAccount.getPrivateKey (),
                                          m.getEditor ().getTheirPublicKey ());

        } catch (Exception e) {

            throw new GeneralException ("Unable to decrypt message from editor: " +
                                        m.getEditor (),
                                        e);

        }

        String messageData = null;

        try
        {

            messageData = new String (bytes,
                                      "utf-8");

        } catch (Exception e) {

            throw new GeneralException ("Unable to convert decrypted message to a string from editor: " +
                                        m.getEditor (),
                                        e);

        }

        // JSON decode
        Map data = (Map) JSONDecoder.decode (messageData);

        return data;

    }

    public static ProjectEditor getProjectEditor (Project      p,
                                                  EditorEditor ed)
                                           throws Exception
    {

        return EditorsEnvironment.getProjectEditor (Environment.getProjectById (p.getId (),
                                                                                p.getType ()),
                                                    ed);

    }

    public static ProjectEditor getProjectEditor (ProjectInfo  p,
                                                  EditorEditor ed)
                                           throws GeneralException
    {

        if (EditorsEnvironment.editorAccount == null)
        {

            return null;

        }

        if (p == null)
        {

            return null;

        }

        if (ed == null)
        {

            return null;

        }

        List<ProjectEditor> pes = (List<ProjectEditor>) EditorsEnvironment.editorsManager.getObjects (ProjectEditor.class,
                                                                                                      p,
                                                                                                      null,
                                                                                                      false);

        for (ProjectEditor pe : pes)
        {

            if (pe.getEditor () == ed)
            {

                return pe;

            }

        }

        return null;

    }

    public static List<ProjectEditor> getProjectEditors (String projectId)
                                                  throws GeneralException
    {

        if (EditorsEnvironment.editorAccount == null)
        {

            return null;

        }

        ProjectInfo p = new ProjectInfo ();
        p.setId (projectId);

        return (List<ProjectEditor>) EditorsEnvironment.editorsManager.getObjects (ProjectEditor.class,
                                                                                   p,
                                                                                   null,
                                                                                   false);

    }
    /*
    public static String getUserPublicKeyBase64EncodedX ()
    {

        if (this.userPublicKey == null)
        {

            return null;

        }

        RSAPublicBCPGKey pubKey = (RSAPublicBCPGKey) this.myPublicKey.getPublicKeyPacket ().getKey ();

        return Base64.encodeBytes (pubKey.getEncoded ());

    }
    */

    public static void sendProjectEditStopMessage (final Project  p,
                                                   final Runnable onComplete)
    {

        EditorsEnvironment.sendProjectEditStopMessage (Environment.getProjectInfo (p),
                                                       onComplete);

    }

    public static void sendProjectEditStopMessage (final ProjectInfo p,
                                                   final Runnable    onComplete)
    {

        if (!p.isEditorProject ())
        {

            throw new IllegalArgumentException ("Only editor projects can be deleted.");

        }

        // Send message saying no longer editing.
        ProjectEditStopMessage message = new ProjectEditStopMessage (p,
                                                                     null,
                                                                     p.getForEditor ());

        EditorsEnvironment.sendMessageToEditor (message,
                                                onComplete,
                                                null,
                                                null);

    }

    public static void addProjectEditor (ProjectEditor pe)
                                    throws GeneralException
    {

        EditorsEnvironment.editorsManager.saveObject (pe,
                                                      null);

        // Fire an event.
        EditorsEnvironment.fireProjectEditorChangedEvent (pe,
                                                          ProjectEditorChangedEvent.PROJECT_EDITOR_ADDED);

    }

    public static void removeProjectEditors (Project p)
                                      throws GeneralException
    {

        if (EditorsEnvironment.editorsManager == null)
        {

            return;

        }

        List<ProjectEditor> pes = EditorsEnvironment.getProjectEditors (p.getId ());

        EditorsEnvironment.editorsManager.deleteObjects (pes,
                                                         null);

        for (ProjectEditor pe : pes)
        {

            // Fire an event.
            EditorsEnvironment.fireProjectEditorChangedEvent (pe,
                                                              ProjectEditorChangedEvent.PROJECT_EDITOR_DELETED);

        }

    }

    public static void removeProjectEditor (ProjectEditor pe)
                                     throws GeneralException
    {

        if (EditorsEnvironment.editorsManager == null)
        {

            return;

        }

        EditorsEnvironment.editorsManager.deleteObject (pe,
                                                        false,
                                                        null);

        // Remove from any project tied to a project viewer.
        ProjectInfo proj = null;

        try
        {

            proj = Environment.getProjectById (pe.getForProjectId (),
                                               Project.NORMAL_PROJECT_TYPE);

        } catch (Exception e) {

            Environment.logError ("Unable to get project for id: " +
                                  pe.getForProjectId (),
                                  e);

        }

        if (proj != null)
        {

            AbstractProjectViewer pv = Environment.getProjectViewer (proj);

            if (proj != null)
            {

                pv.getProject ().removeProjectEditor (pe);

            }

        }
/*
        if (pe.isAccepted ())
        {

            // Only send this if the editor has already accepted the project.
            ProjectEditStopMessage mess = new ProjectEditStopMessage (pe.getProject (),
                                                                      null,
                                                                      pe.getEditor ());

            EditorsEnvironment.sendMessageToEditor (mess);

        }
  */
        // Fire an event.
        EditorsEnvironment.fireProjectEditorChangedEvent (pe,
                                                          ProjectEditorChangedEvent.PROJECT_EDITOR_DELETED);

    }

    public static Set<ProjectInfo> getProjectsSentToEditor (EditorEditor ed)
                                                     throws Exception
    {

        if (EditorsEnvironment.editorsManager == null)
        {

            return null;

        }

        return EditorsEnvironment.editorsManager.getProjectsSentToEditor (ed);

    }

    public static Set<ProjectInfo> getProjectsForEditor (EditorEditor ed)
                                                  throws Exception
    {

        Set<ProjectInfo> projs = new LinkedHashSet ();

        for (ProjectInfo p : Environment.getAllProjectInfos ())
        {

            if (p.getForEditor () == null)
            {

                continue;

            }

            if (p.getForEditor ().getEmail ().equals (ed.getEmail ()))
            {

                projs.add (p);

            }

        }

        return projs;

    }

    public static void setProjectEditorStatus (final String       projId,
                                               final EditorEditor ed,
                                               final String       newStatus)
                                        throws Exception
    {

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

        pe.setStatusMessage (newStatus);

        EditorsEnvironment.updateProjectEditor (pe);

    }

    public static void removeEditorAsProjectEditorForAllProjects (final EditorEditor ed)
                                                           throws Exception
    {

        Set<ProjectInfo> projs = Environment.getAllProjectInfos ();

        for (ProjectInfo p : projs)
        {

            ProjectEditor pe = EditorsEnvironment.getProjectEditor (p,
                                                                    ed);

            if (pe == null)
            {

                continue;

            }

            pe.setEditorTo (new java.util.Date ());
            pe.setCurrent (false);
            pe.setStatusMessage (getUIString (LanguageStrings.editors,editor,remove,editorstatus));
            //"Removed");

            EditorsEnvironment.updateProjectEditor (pe);

        }

    }

    public static void removePendingEditor (final EditorEditor ed,
                                            final Runnable     onDeleteComplete)
    {

        if (!ed.isPending ())
        {

            throw new IllegalStateException ("Only pending editors can be deleted using this method.");

        }

        final Runnable doDelete = () ->
        {

            EditorsEnvironment.removeEditor (ed,
                                             onDeleteComplete);

        };

        EditorsEnvironment.editorsHandler.deleteInvite (ed,
                                                        new EditorsWebServiceAction ()
        {

            public void processResult (EditorsWebServiceResult res)
            {

                Environment.scheduleImmediately (doDelete);

            }
        },
        new EditorsWebServiceAction ()
        {

            public void processResult (EditorsWebServiceResult res)
            {

                // If the invite couldn't be found then just delete the editor anyway since it's
                // probably been removed from the QW end.
                if (res.isNoDataFoundError ())
                {

                    Environment.scheduleImmediately (doDelete);

                } else {

                    EditorsEnvironment.editorsHandler.getDefaultEditorsWebServiceErrorAction ().processResult (res);

                }

            }

        });

    }

    public static void removeEditor (final EditorEditor ed,
                                     final Runnable     onComplete)
    {

        if (ed.isPrevious ())
        {

            if (onComplete != null)
            {

                Environment.scheduleImmediately (onComplete);
                return;

            }

        }

        // It may be that the user deletes their account and the editor is in a pending
        // state but doesn't have an account so we can't send them a message.  Instead just
        // delete the invite.
        if ((ed.isPending ())
            &&
            (ed.getTheirPublicKey () == null)
           )
        {

            try
            {

                // Uupdate the editor to be previous.
                ed.setEditorStatus (EditorEditor.EditorStatus.previous);

                EditorsEnvironment.updateEditor (ed);

            } catch (Exception e) {

                Environment.logError ("Unable to update editor: " +
                                      ed,
                                      e);

                ComponentUtils.showErrorMessage (getUILanguageStringProperty (LanguageStrings.editors,editor,edit,actionerror));
                                          //"Unable to update {editor}, please contact Quoll Writer support for assistance.");

                return;

            }

            if (onComplete != null)
            {

                Environment.scheduleImmediately (onComplete);

            }

            return;

        }

        // Send the editor removed message
        EditorRemovedMessage mess = new EditorRemovedMessage (ed);

        EditorsEnvironment.sendMessageToEditor (mess,
                                                () ->
                                                {

                                                    UIUtils.runLater (() ->
                                                    {

                                                        try
                                                        {

                                                            // Uupdate the editor to be previous.
                                                            ed.setEditorStatus (EditorEditor.EditorStatus.previous);

                                                            EditorsEnvironment.updateEditor (ed);

                                                        } catch (Exception e) {

                                                            Environment.logError ("Unable to update editor: " +
                                                                                  ed,
                                                                                  e);

                                                            ComponentUtils.showErrorMessage (getUILanguageStringProperty (LanguageStrings.editors,editor,edit,actionerror));
                                                                                      //"Unable to update {editor}, please contact Quoll Writer support for assistance.");

                                                            return;

                                                        }

                                                        try
                                                        {

                                                            EditorsEnvironment.removeEditorAsProjectEditorForAllProjects (ed);

                                                        } catch (Exception e) {

                                                            Environment.logError ("Unable to remove editor as project editor: " +
                                                                                  ed,
                                                                                  e);

                                                            ComponentUtils.showErrorMessage (getUILanguageStringProperty (LanguageStrings.editors,editor,edit,actionerror));
                                                                                      //"Unable to update {editor}, please contact Quoll Writer support for assistance.");

                                                            return;

                                                        }

                                                    });

                                                    // Unsubscribe.
                                                    EditorsEnvironment.messageHandler.unsubscribeFromEditor (ed);

                                                    if (onComplete != null)
                                                    {

                                                        Environment.scheduleImmediately (onComplete);

                                                    }

                                                },
                                                null,
                                                null);

    }

    public static EditorMessage getMessageByKey (int key)
                                          throws GeneralException
    {

        if (EditorsEnvironment.editorsManager == null)
        {

            return null;

        }

        return (EditorMessage) (DataObject) EditorsEnvironment.editorsManager.getObjectByKey (EditorMessage.class,
                                                                                              key,
                                                                                              null, // parent object
                                                                                              null, // connection
                                                                                              true);

    }

    public static void addMessage (EditorMessage mess)
                            throws GeneralException
    {

        // TODO: This needs to be changed, ok for now.
        if (mess instanceof InteractionMessage)
        {

            return;

        }

        EditorsEnvironment.editorsManager.saveObject (mess,
                                                      null);

        mess.getEditor ().addMessage (mess);

        EditorsEnvironment.fireEditorMessageEvent (new EditorMessageEvent (mess.getEditor (),
                                                                           mess,
                                                                           EditorMessageEvent.MESSAGE_ADDED));

    }

    public static void updateMessage (EditorMessage mess)
                         throws       GeneralException
    {

        EditorsEnvironment.editorsManager.saveObject (mess,
                                                      null);

        // Fire an event.
        EditorsEnvironment.fireEditorMessageEvent (new EditorMessageEvent (mess.getEditor (),
                                                                           mess,
                                                                           EditorMessageEvent.MESSAGE_CHANGED));

    }

    public static void updateProjectEditor (ProjectEditor pe)
                               throws       GeneralException
    {

        EditorsEnvironment.editorsManager.saveObject (pe,
                                                      null);

        // Fire an event.
        EditorsEnvironment.fireProjectEditorChangedEvent (pe,
                                                          ProjectEditorChangedEvent.PROJECT_EDITOR_CHANGED);

    }

    public static void updateEditor (EditorEditor ed)
                        throws       GeneralException
    {

        EditorsEnvironment.editorsManager.saveObject (ed,
                                                      null);

        // Fire an event.
        EditorsEnvironment.fireEditorChangedEvent (ed,
                                                   EditorChangedEvent.EDITOR_CHANGED);

    }

    public static void addNewEditor (EditorEditor ed)
                              throws GeneralException
    {

        EditorsEnvironment.editorsManager.saveObject (ed,
                                                      null);

        EditorsEnvironment.editors.add (ed);

        if (ed.isInvitedByMe ())
        {

            EditorsEnvironment.invitesIveSent.add (ed);

        } else {

            EditorsEnvironment.invitesForMe.add (ed);

        }

        ed.getBinder ().addChangeListener (ed.editorStatusProperty (),
                                           (pr, oldv, newv) ->
        {

            if ((newv == EditorEditor.EditorStatus.current)
                &&
                (oldv != EditorEditor.EditorStatus.current)
               )
            {

                EditorsEnvironment.invitesForMe.remove (ed);
                EditorsEnvironment.invitesIveSent.remove (ed);
                EditorsEnvironment.currentEditors.add (ed);
                EditorsEnvironment.previousEditors.remove (ed);

            }

            if (newv == EditorEditor.EditorStatus.previous)
            {

                EditorsEnvironment.invitesForMe.remove (ed);
                EditorsEnvironment.invitesIveSent.remove (ed);
                EditorsEnvironment.currentEditors.remove (ed);
                EditorsEnvironment.previousEditors.add (ed);

            }

        });

        // Fire an event.
        EditorsEnvironment.fireEditorChangedEvent (ed,
                                                   EditorChangedEvent.EDITOR_ADDED);

    }

    public static void deleteEditor (EditorEditor ed)
                              throws GeneralException
    {

        ed.getBinder ().dispose ();

        // Delete from the project editor.
        EditorsEnvironment.editorsManager.deleteEditor (ed);

        EditorsEnvironment.editors.remove (ed);

        EditorsEnvironment.invitesForMe.remove (ed);
        EditorsEnvironment.invitesIveSent.remove (ed);
        EditorsEnvironment.currentEditors.remove (ed);
        EditorsEnvironment.previousEditors.remove (ed);

        // Fire an event.
        EditorsEnvironment.fireEditorChangedEvent (ed,
                                                   EditorChangedEvent.EDITOR_DELETED);

    }

    public static EditorEditor getEditorByEmail (String em)
    {

        if (em == null)
        {

            return null;

        }

        em = em.toLowerCase ();

        for (EditorEditor ed : EditorsEnvironment.editors)
        {

            if ((ed.getEmail () != null)
                &&
                (ed.getEmail ().equals (em))
               )
            {

                return ed;

            }

        }

        return null;

    }

    public static EditorEditor getEditorByMessagingUsername (String u)
    {

        if (u == null)
        {

            return null;

        }

        for (EditorEditor ed : EditorsEnvironment.editors)
        {

            if ((ed.getMessagingUsername () != null)
                &&
                (ed.getMessagingUsername ().equals (u))
               )
            {

                return ed;

            }

        }

        return null;

    }

    public static EditorEditor getEditorByKey (long key)
    {

        if (key < 1)
        {

            return null;

        }

        for (EditorEditor ed : EditorsEnvironment.editors)
        {

            if (ed.getKey () == key)
            {

                return ed;

            }

        }

        return null;

    }

    /**
     * Return a count of the number of editors with a status of "pending".
     *
     * @return The count.
     */
    public static int getPendingEditorsCount ()
    {

        int c = 0;

        for (EditorEditor ed : EditorsEnvironment.editors)
        {

            if (ed.getEditorStatus () == EditorEditor.EditorStatus.pending)
            {

                c++;

            }

        }

        return c;

    }

    public static ObservableList<EditorEditor> getPendingInvites ()
    {

        return EditorsEnvironment.invitesIveSent;

    }

    public static ObservableList<EditorEditor> getCurrentEditors ()
    {

        return EditorsEnvironment.currentEditors;

    }

    public static ObservableList<EditorEditor> getPreviousEditors ()
    {

        return EditorsEnvironment.previousEditors;

    }

    public static ObservableSet<EditorEditor> getEditors ()
    {

        return EditorsEnvironment.editors;

    }

    public static void fireProjectEvent (ProjectEvent ev)
    {

        // We'll see?
        // Fire to all project viewers?

    }

    public static String getNewMessageId (EditorEditor ed,
                                          String       messageType)
                                   throws Exception
    {

        return EditorsEnvironment.editorsManager.getNewMessageId (ed,
                                                                  messageType);

    }

    /*
    public static com.gentlyweb.properties.Properties getUserEditorsProperties ()
    {

        return Environment.userEditorsProperties;

    }
*/
    public static void setEditorsProperty (String           name,
                                           com.gentlyweb.properties.AbstractProperty prop)
                                    throws Exception
    {

        EditorsEnvironment.editorsProps.setProperty (name,
                                                     prop);

        EditorsEnvironment.saveEditorsProperties (null);

    }

    public static void setEditorsProperty (String name,
                                           String value)
                                    throws Exception
    {

        EditorsEnvironment.editorsProps.setProperty (name,
                                                     new com.gentlyweb.properties.StringProperty (name,
                                                                         value));

        EditorsEnvironment.saveEditorsProperties (null);

    }

    public static void setEditorsProperty (String  name,
                                           boolean value)
                                    throws Exception
    {

        EditorsEnvironment.editorsProps.setProperty (name,
                                                     new com.gentlyweb.properties.BooleanProperty (name,
                                                                          value));

        EditorsEnvironment.saveEditorsProperties (null);

    }

    public static void removeEditorsProperty (String name)
                                       throws Exception
    {

        EditorsEnvironment.editorsProps.removeProperty (name);

        EditorsEnvironment.saveEditorsProperties (null);

    }

    public static void saveEditorsProperties (com.gentlyweb.properties.Properties props)
                                           throws Exception
    {

        if (props == null)
        {

            props = EditorsEnvironment.editorsProps;

        }

        // Load the per user properties.
        DOM4JUtils.writeToFile (props.getAsElement (),
                                UserProperties.getUserEditorsPropertiesPath (),
                                true);

    }

    public static boolean getEditorsPropertyAsBoolean (String name)
    {

        return EditorsEnvironment.editorsProps.getPropertyAsBoolean (name);

    }

    public static String getEditorsProperty (String name)
    {

        return EditorsEnvironment.editorsProps.getProperty (name);

    }

    public static boolean isShowPopupWhenNewMessageReceived ()
    {

        // Never show popups in full screen mode.
        if (Environment.isInFullScreen ())
        {

            return false;

        }

        return false;
        //return EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.EDITORS_SHOW_POPUP_WHEN_NEW_MESSAGE_RECEIVED_PROPERTY_NAME);

    }
/*
    public static void setShowPopupWhenNewMessageReceived (boolean v)
                                                    throws Exception
    {

        BooleanProperty prop = new BooleanProperty (Constants.EDITORS_SHOW_POPUP_WHEN_NEW_MESSAGE_RECEIVED_PROPERTY_NAME,
                                                    v);
        EditorsEnvironment.setEditorsProperty (Constants.EDITORS_SHOW_POPUP_WHEN_NEW_MESSAGE_RECEIVED_PROPERTY_NAME,
                                               prop);

    }
*/
    public static void fullScreenEntered ()
    {

        // Get the current status.
        if (EditorsEnvironment.isUserLoggedIn ())
        {

            if (EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.QW_EDITORS_SERVICE_SET_BUSY_ON_FULL_SCREEN_ENTERED_PROPERTY_NAME))
            {

                // Get the current status, if it's not "busy" then change it to busy.
                if (EditorsEnvironment.getUserOnlineStatus () != EditorEditor.OnlineStatus.busy)
                {

                    EditorsEnvironment.lastOnlineStatus = EditorsEnvironment.getUserOnlineStatus ();

                    try
                    {

                        EditorsEnvironment.setUserOnlineStatus (EditorEditor.OnlineStatus.busy);

                    } catch (Exception e) {

                        Environment.logError ("Unable to set user online status to busy",
                                              e);

                    }

                }

            }

        }

    }

    public static void fullScreenExited ()
    {

        if (EditorsEnvironment.lastOnlineStatus != null)
        {

            try
            {

                EditorsEnvironment.setUserOnlineStatus (EditorsEnvironment.lastOnlineStatus);

            } catch (Exception e) {

                Environment.logError ("Unable to set user online status to last: " +
                                      EditorsEnvironment.lastOnlineStatus,
                                      e);

            }

            EditorsEnvironment.lastOnlineStatus = null;

        }

        // Check for new messages and show a notification if there are any.
        EditorsEnvironment.checkForUndealtWithMessages ();

    }

    /**
     * Show a warning message if the editor is offline when the user is trying to send them a message.
     *
     * @param ed The editor the user is sending a message to.
     */
    public static void showMessageSendWarningIfEditorOfflineMessage (EditorEditor ed)
    {

        if (ed.isOffline ())
        {

            // TODO: Next release change this to be more in context.
            // Has the user seen this before?
            //if (!EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.EDITORS_SEEN_OFFLINE_SEND_MESSAGE_PROPERTY_NAME))
            //{

                AbstractViewer viewer = Environment.getFocusedViewer ();

                QuollPopup.messageBuilder ()
                    .inViewer (viewer)
                    .title (LanguageStrings.editors,messages,editoroffline,popup,title)
                    .message (getUILanguageStringProperty (Arrays.asList (LanguageStrings.editors,messages,editoroffline,popup,text),
                                                           ed.mainNameProperty ()))
                    .build ();

                try
                {

                    EditorsEnvironment.setEditorsProperty (Constants.EDITORS_SEEN_OFFLINE_SEND_MESSAGE_PROPERTY_NAME,
                                                           true);

                } catch (Exception e) {

                    Environment.logError ("Unable to set property",
                                          e);

                }

            //}

        }


    }

    public static URL getReportMessageURL ()
                                    throws Exception
    {

        return new URL (Environment.getQuollWriterWebsite () + UserProperties.get (Constants.EDITORS_SERVICE_REPORT_MESSAGE_PAGE_PROPERTY_NAME));

    }

    private static void removeAllEditors (final Deque<EditorEditor> eds,
                                          final Runnable            onComplete)
    {

        if (eds.size () == 0)
        {

            if (onComplete != null)
            {

                Environment.scheduleImmediately (onComplete);

            }

        } else {

            EditorEditor ed = eds.pop ();

            Runnable onDeleteComplete = () ->
            {

               EditorsEnvironment.removeAllEditors (eds,
                                                    onComplete);

            };

            if (ed.isPending ())
            {

                EditorsEnvironment.removePendingEditor (ed,
                                                        onDeleteComplete);

            } else {

                EditorsEnvironment.removeEditor (ed,
                                                 onDeleteComplete);

            }

        }

    }

    public static void deleteUserAccount (final Runnable            onComplete,
                                          final Consumer<Exception> onError)
    {

        // Send EditorRemoved messages for all editors (but don't remove them).
        EditorsEnvironment.goOnline (getUILanguageStringProperty (LanguageStrings.editors,login,reasons,deleteaccount),
                                    //"To delete your account you must first go online.",
                                     () ->
        {

            EditorsEnvironment.removeAllEditors (new ArrayDeque (EditorsEnvironment.editors),
                                                 () ->
            {

                Runnable deleteAcc = () ->
                {

                    // Delete the account.
                    EditorsEnvironment.editorsHandler.deleteAccount (new EditorsWebServiceAction ()
                    {

                        public void processResult (EditorsWebServiceResult res)
                        {

                            // Sign out.
                            EditorsEnvironment.goOffline ();

                            // Remove saved values (if present).
                            try
                            {

                                EditorsEnvironment.removeEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME);

                            } catch (Exception e) {

                                Environment.logError ("Unable to remove editors property",
                                                      e);

                            }

                            // Close all the db connections.
                            EditorsEnvironment.editorsManager.closeConnectionPool ();

                            EditorsEnvironment.editorAccount = null;

                            try
                            {

                                EditorsEnvironment.removeEditorsProperty (Constants.QW_EDITORS_DB_DIR_PROPERTY_NAME);

                            } catch (Exception e) {

                                Environment.logError ("Unable to remove editors database location",
                                                      e);

                            }

                            if (onComplete != null)
                            {

                                Environment.scheduleImmediately (onComplete);

                            }

                        }
                    },
                    new EditorsWebServiceAction ()
                    {

                        public void processResult (EditorsWebServiceResult res)
                        {

                            Environment.logError ("Unable to delete user account: " +
                                                  res);

                            if (onError != null)
                            {

                                Environment.scheduleImmediately (() ->
                                {

                                    onError.accept (new Exception (res.getErrorMessage ()));

                                });

                            } else {

                                ComponentUtils.showErrorMessage (getUILanguageStringProperty (LanguageStrings.editors,user,deleteaccount,actionerror));
                                //"Unable to delete your account, please contact Quoll Writer support for assistance.");

                            }

                        }

                    });

                };

            });

            // Offer to remove all the editor projects.
            /*
            TODO
            EditorsUIUtils.showDeleteProjectsForAllEditors (Environment.getFocusedViewer (),
                                                            deleteAcc);
*/
        },
        null,
        onError);

    }

    public static File getEditorsMessageLogFile ()
                                          throws IOException
    {

        return Environment.getLogDir ().resolve (Constants.EDITOR_MESSAGES_LOG_NAME).toFile ();

    }

    public static ObservableList<EditorEditor> getInvitesForMe ()
    {

        return EditorsEnvironment.invitesForMe;

    }

    public static boolean isAutoLoginAtQWStart ()
    {

        return EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME);

    }

}
