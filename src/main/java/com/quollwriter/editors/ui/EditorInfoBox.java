package com.quollwriter.editors.ui;

import java.awt.image.*;

import java.util.*;
import java.io.*;

import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.embed.swing.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.events.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.*;
//import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class EditorInfoBox extends VBox
{

    private EditorEditor editor = null;
    private AbstractViewer viewer = null;
    private Pane avatar = null;
    private HBox avatarBox = null;
    private QuollLabel mainName = null;
    private IconBox onlineStatus = null;
    private QuollLabel other = null;
    private VBox details = null;
    private HBox editorInfo = null;
    private QuollButton projectMessages = null;
    private QuollButton importantMessages = null;
    private QuollButton comments = null;
    private QuollButton chat = null;
    private boolean showProjectInfo = false;
    private ProjectEditor projEditor = null;
    private Project proj = null;
    private boolean editorProject = false;

    public EditorInfoBox (EditorEditor   ed,
                          AbstractViewer viewer,
                          boolean        showProjectInfo,
                          IPropertyBinder binder)
                   //throws GeneralException
    {

        final EditorInfoBox _this = this;

        this.editor = ed;

        UIUtils.addStyleSheet (this,
                               Constants.COMPONENT_STYLESHEET_TYPE,
                               StyleClassNames.CONTACT);

        this.setFillWidth (true);
        this.getStyleClass ().add (StyleClassNames.CONTACT);
        this.showProjectInfo = showProjectInfo;

        if ((this.showProjectInfo)
            &&
            (!(viewer instanceof AbstractProjectViewer))
           )
        {

           throw new IllegalArgumentException ("To show project information a project viewer must be provided.");

        }

        if (viewer instanceof AbstractProjectViewer)
        {

            this.proj = ((AbstractProjectViewer) viewer).getProject ();

            this.editorProject = this.proj.isEditorProject ();

        }

        this.loadMessagesForEditor ();

        // We add ourselves as a listener for editor change events however we don't ever
        // remove ourselves since, as a standard component, we don't have a fixed lifecycle.
        //EditorsEnvironment.addEditorChangedListener (this);

        //EditorsEnvironment.addEditorMessageListener (this);

        binder.addSetChangeListener (this.editor.getMessages (),
                                     ev ->
        {

            this.updateButtons ();

        });

        binder.addChangeListener (this.editor.messagesUpdatedProperty (),
                                  (pr, oldv, newv) ->
        {

            this.updateButtons ();

        });

        this.viewer = viewer;

        if (this.viewer instanceof AbstractProjectViewer)
        {

            this.projEditor = ((AbstractProjectViewer) this.viewer).getProject ().getProjectEditor (this.editor);

        }

        binder.addChangeListener (ed.editorStatusProperty (),
                                  (pr, oldv, newv) ->
        {

            this.updateForEditorStatus ();

        });

        this.editorInfo = new HBox ();
        /*
        HBox.setHgrow (this.editorInfo,
                       Priority.ALWAYS);
*/
        this.setOnMouseReleased (ev ->
        {

            if (this.getProperties ().get ("context-menu") != null)
            {

                ((ContextMenu) this.getProperties ().get ("context-menu")).hide ();

            }

            if ((ev.isPopupTrigger ())
                ||
                (this.editor.isPending ())
               )
            {

                return;

            }

            // Show the editor.
            try
            {

                this.viewer.sendMessageToEditor (this.editor);

            } catch (Exception e) {

                Environment.logError ("Unable to show editor: " +
                                      this.editor,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (editors,LanguageStrings.editor,view,actionerror));
                                          //"Unable to show {editor}.");

            }

        });

        this.avatarBox = IconBox.builder ()
            .styleClassName ("avatar-box")
            .image (this.editor.mainAvatarProperty ())
            .onNoImage (b ->
            {

                if (this.avatarBox != null)
                {

                    this.avatarBox.pseudoClassStateChanged (StyleClassNames.NOAVATAR_PSEUDO_CLASS, true);

                }

            })
            .onImagePresent (b ->
            {

                if (this.avatarBox != null)
                {

                    this.avatarBox.pseudoClassStateChanged (StyleClassNames.NOAVATAR_PSEUDO_CLASS, false);

                }

            })
            .build ();

        this.avatarBox.pseudoClassStateChanged (StyleClassNames.NOAVATAR_PSEUDO_CLASS, this.editor.getMainAvatar () == null);

/*
        this.avatar = new ImageView ();
        this.avatar.setPreserveRatio (true);
        this.avatar.setSmooth (true);
        this.avatar.getStyleClass ().add (StyleClassNames.AVATAR);
*/
        //this.updateAvatar ();
/*
        VBox p = new VBox ();
        p.getStyleClass ().add ("avatar-box");
        p.getChildren ().add (this.avatar);

        p.widthProperty ().addListener ((pr, oldv, newv) ->
        {

            this.avatar.setFitWidth (Math.round (newv.doubleValue ()) - p.getInsets ().getLeft () - p.getInsets ().getRight ());

            UIUtils.runLater (() ->
            {

                this.requestLayout ();

            });

        });
*/
/*
        binder.addChangeListener (ed.mainAvatarProperty (),
                                  (pr, oldv, newv) ->
        {

            this.updateAvatar ();

        });
*/
        final boolean pending = ed.isPending ();

        this.details = new VBox ();

        StringProperty nameProp = new SimpleStringProperty ();

        this.mainName = QuollLabel.builder ()
            .label (nameProp)
            .styleClassName (StyleClassNames.NAME)
            .build ();

        nameProp.setValue (ed.getMainName ());

        binder.addChangeListener (ed.mainNameProperty (),
                                  (pr, oldv, newv) ->
        {

            nameProp.setValue (ed.getMainName ());

        });

        this.other = QuollLabel.builder ()
            .styleClassName (StyleClassNames.OTHER)
            .build ();

        this.onlineStatus = IconBox.builder ()
            .styleClassName (StyleClassNames.ONLINESTATUS)
            .build ();

        binder.addChangeListener (ed.onlineStatusProperty (),
                                  (pr, oldv, newv) ->
        {

            this.updateForOnlineStatus ();

        });

        this.editorInfo.getChildren ().addAll (this.avatarBox, this.details);
        this.updateForEditorStatus ();
        this.updateForOnlineStatus ();

        // Create the buttons.
        this.projectMessages = QuollButton.builder ()
            .iconName (Project.OBJECT_TYPE)
            .onAction (ev ->
            {

                try
                {

                    EditorsUIUtils.showProjectMessagesForEditor (this.editor,
                                                                 (AbstractProjectViewer) this.viewer,
                                                                 this.projectMessages);

                } catch (Exception e) {

                    Environment.logError ("Unable to show project messages for editor: " +
                                          this.editor,
                                          e);

                    ComponentUtils.showErrorMessage (viewer,
                                                     getUILanguageStringProperty (editors,messages,show,project,actionerror));
                                              //"Unable to show {project} messages for {editor}.");

                }

            })
            .build ();

        this.importantMessages = QuollButton.builder ()
            .iconName (StyleClassNames.IMPORTANT)
            .onAction (ev ->
            {

                try
                {

                    EditorsUIUtils.showImportantMessagesForEditor (this.editor,
                                                                   this.viewer,
                                                                   this.importantMessages);

                } catch (Exception e) {

                    Environment.logError ("Unable to show important messages for editor: " +
                                          this.editor,
                                          e);

                    ComponentUtils.showErrorMessage (this.viewer,
                                                     getUILanguageStringProperty (editors,messages,show,important,actionerror));
                                              //"Unable to show important messages for {editor}.");

                }

            })
            .build ();

        this.comments = QuollButton.builder ()
            .iconName (StyleClassNames.COMMENT)
            .onAction (ev ->
            {

                try
                {

                    EditorsUIUtils.showAllCommentsForEditor (this.editor,
                                                             (AbstractProjectViewer) this.viewer,
                                                             this.comments);

                } catch (Exception e) {

                    Environment.logError ("Unable to show comments for editor: " +
                                          this.editor,
                                          e);

                    ComponentUtils.showErrorMessage (this.viewer,
                                                     getUILanguageStringProperty (editors,LanguageStrings.editor,view,actionerror));
                                              //"Unable to show {comments} for {editor}.");

                }

            })
            .build ();

        this.chat = QuollButton.builder ()
            .iconName (StyleClassNames.MESSAGE)
            .onAction (ev ->
            {

                try
                {

                    this.viewer.sendMessageToEditor (this.editor);

                } catch (Exception e) {

                    Environment.logError ("Unable to show editor: " +
                                          this.editor,
                                          e);

                    ComponentUtils.showErrorMessage (this.viewer,
                                                     getUILanguageStringProperty (editors,LanguageStrings.editor,view,actionerror));
                                              //"Unable to show {editor}.");

                }

            })
            .build ();

        QuollToolBar tb = QuollToolBar.builder ()
            .styleClassName (StyleClassNames.STATUSBAR)
            .controls (Arrays.asList (this.onlineStatus,
                                      this.importantMessages,
                                      this.comments,
                                      this.projectMessages,
                                      this.chat))
            .build ();

        this.details.getChildren ().addAll (this.mainName, this.other, tb);

        this.getChildren ().add (this.editorInfo);

        this.updateButtons ();

        this.addFullPopupListener ();

    }
/*
    private void updateAvatar ()
    {

        if (this.editor.getMainAvatar () != null)
        {

            this.avatar.setBackground (new Background (new BackgroundImage (this.editor.getMainAvatar (), //SwingFXUtils.toFXImage (this.editor.getMainAvatar (), null),
                                                                   BackgroundRepeat.NO_REPEAT,
                                                                   BackgroundRepeat.NO_REPEAT,
                                                                   BackgroundPosition.CENTER,
                                                                   new BackgroundSize (100, 100, true, true, true, false))));

            this.avatarBox.pseudoClassStateChanged (StyleClassNames.NOAVATAR_PSEUDO_CLASS, false);

        } else {

            this.avatar.setBackground (null);
            this.avatarBox.pseudoClassStateChanged (StyleClassNames.NOAVATAR_PSEUDO_CLASS, true);

        }

    }
*/

    private void updateButtons ()
    {

        UIUtils.runLater (() ->
        {

            this.updateButtons_int ();

        });

    }

    private void updateButtons_int ()
    {

        Set<EditorMessage> mess = this.getImportantMessages ();

        this.importantMessages.setVisible (false);
        int ms = mess.size ();

        if (ms > 0)
        {

            this.projectMessages.pseudoClassStateChanged (StyleClassNames.UNDEALTWITH_PSEUDO_CLASS, true);

            UIUtils.setTooltip (this.importantMessages,
                                getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,importantmessages,tooltip),
                                                                  //"%s new/important message%s requiring your attention, click to view them",
                                                             Environment.formatNumber (ms)));
                                                                  //(ms == 1 ? "" : "s"),
                                                                  //(ms == 1 ? "s" : "")));

            this.importantMessages.setText (String.format ("%s",
                                                           Environment.formatNumber (ms)));

            this.importantMessages.setVisible (true);

        }

        this.projectMessages.setVisible (false);
        if ((this.showProjectInfo)
            &&
            ((this.projEditor != null)
             ||
             (this.editorProject)
            )
           )
        {

            int undealtWithCount = 0;

            // Get undealt with messages that are not chat.
            // If there is just one then show it, otherwise show a link that will display a popup of them.
            Set<EditorMessage> projMess = this.getProjectMessages ();

            for (EditorMessage em : projMess)
            {

                if (!em.isDealtWith ())
                {

                    undealtWithCount++;

                }

            }

            int ps = projMess.size ();

            this.projectMessages.pseudoClassStateChanged (StyleClassNames.UNDEALTWITH_PSEUDO_CLASS, (undealtWithCount > 0));

            if (undealtWithCount > 0)
            {

                UIUtils.setTooltip (this.projectMessages,
                                    getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,projecteditor,undealtwithmessagecount,tooltip),
                                                                //"%s {project} message%s requiring your attention, click to view them",
                                                                 Environment.formatNumber (undealtWithCount)));
                                                                    //(undealtWithCount == 1 ? "" : "s"),
                                                                    //(undealtWithCount == 1 ? "s" : "")));

            } else {

                UIUtils.setTooltip (this.projectMessages,
                                    getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,projecteditor,undealtwithmessagecount,tooltip),
                                                                //"%s {project} message%s, click to view them",
                                                                Environment.formatNumber (ps)));
                                                                    //(projMess.size () == 1 ? "" : "s"),
                                                                    //(projMess.size () == 1 ? "s" : "")));

            }

            this.projectMessages.setText (String.format ("%s",
                                                         Environment.formatNumber (ps)));

            this.projectMessages.setVisible (true);

        }

        this.comments.setVisible (false);

        if (this.showProjectInfo)
        {

            AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

            int commCount = 0;

            if (!this.editor.isPending ())
            {

                this.comments.setVisible (true);

                // Get undealt with messages that are not chat.
                // If there is just one then show it, otherwise show a link that will display a popup of them.
                Set<EditorMessage> comments = this.editor.getMessages (new DefaultEditorMessageFilter (pv.getProject (),
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

                    this.projectMessages.pseudoClassStateChanged (StyleClassNames.UNDEALTWITH_PSEUDO_CLASS, (undealtWithCount > 0));

                    UIUtils.setTooltip (this.comments,
                                        getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,projectcomments,(this.projEditor != null ? received : sent),tooltip),
                                                                                                //"%s {comment%s} %s %s",
                                                                     Environment.formatNumber (commCount),
                                                                                                 //(commCount == 1 ? "" : "s"),
                                                                                                 //(this.projEditor != null ? "from" : "sent to"),
                                                                     this.editor.mainNameProperty ()));

                } else {

                    if (this.projEditor != null)
                    {

                        UIUtils.setTooltip (this.comments,
                                            getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,noprojectcomments,received,tooltip),
                                                                                                    //"%s has not sent you any {comments} yet.",
                                                                         this.editor.mainNameProperty ()));

                    } else {

                        UIUtils.setTooltip (this.comments,
                                            getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,noprojectcomments,sent,tooltip),
                                                                                                    //"You have not sent any {comments} to %s yet.",
                                                                         this.editor.mainNameProperty ()));

                    }

                }

                this.comments.setText (Environment.formatNumber (commCount));

                this.comments.setDisable (commCount == 0);

            }

        }

        this.chat.setVisible (false);

        Set<EditorMessage> chatMessages = this.getChatMessages ();

        int chatMessagesSize = chatMessages.size ();

        if (chatMessagesSize > 0)
        {

            this.chat.pseudoClassStateChanged (StyleClassNames.UNDEALTWITH_PSEUDO_CLASS, true);

            UIUtils.setTooltip (this.chat,
                                getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,unreadchatmessages,tooltip),
                                                                                     //"%s unread chat message%s",
                                                             Environment.formatNumber (chatMessagesSize)));

            this.chat.setText (Environment.formatNumber (chatMessagesSize));

            this.chat.setVisible (true);

        }

    }

    private void updateForOnlineStatus ()
    {

        if (this.editor.isPending ())
        {

            this.onlineStatus.setVisible (false);
            return;

        }

        this.onlineStatus.setVisible (true);

        if (this.editor.getOnlineStatus () != null)
        {

            //this.onlineStatus.getStyleClass ().addAll (this.editor.getOnlineStatus ().getType () + "-icon");
            UIUtils.setTooltip (this.onlineStatus,
                                this.editor.getOnlineStatus ().nameProperty ());
            this.onlineStatus.setIconName (this.editor.getOnlineStatus ().getType ());

        } else {

            //this.onlineStatus.getStyleClass ().add (EditorEditor.OnlineStatus.offline.getType () + "-icon");
            UIUtils.setTooltip (this.onlineStatus,
                                EditorEditor.OnlineStatus.offline.nameProperty ());
            this.onlineStatus.setIconName (EditorEditor.OnlineStatus.offline.getType ());

        }

    }

    private void updateForEditorStatus ()
    {

        this.other.setVisible (false);
        UIUtils.setTooltip (this.editorInfo,
                            null);

        if ((this.showProjectInfo)
            &&
            (this.projEditor != null)
           )
        {

            this.other.setVisible (true);
            this.other.textProperty ().unbind ();
            this.other.textProperty ().bind (this.projEditor.statusMessageProperty ());

        }

        if (!this.editor.isPending ())
        {

            if (!this.editor.isPrevious ())
            {

                UIUtils.setTooltip (this.editorInfo,
                                    getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,info,tooltip,currenteditor),
                                                               //"Click to send a message to %s, right click to see the menu",
                                                                     this.editor.getMainName ()));

            } else {

                UIUtils.setTooltip (this.editorInfo,
                                    getUILanguageStringProperty (editors,LanguageStrings.editor,view,info,tooltip,previouseditor));

            }

        } else {

            if (!this.editor.isInvitedByMe ())
            {

                this.other.textProperty ().unbind ();
                this.other.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,LanguageStrings.other,pendingeditor,invitereceived),
                                                   //"Received: %s",
                                                                              Environment.formatDate (this.editor.getDateCreated ())));

            } else {

                this.other.textProperty ().unbind ();
                this.other.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,LanguageStrings.other,pendingeditor,invitesent),
                                                   //"Invited: %s",
                                                                              Environment.formatDate (this.editor.getDateCreated ())));

            }

            this.other.setVisible (true);

        }

    }

    public boolean isShowProjectInfo ()
    {

        return this.showProjectInfo;

    }

    private boolean isShowAttentionBorder ()
    {

        final EditorInfoBox _this = this;

        // TODO: Investigate why this is needed, this is being called on closedown of QW.
        // Probably from close of link to message server.
        if (this.viewer instanceof AbstractProjectViewer)
        {

            if (((AbstractProjectViewer) this.viewer).getProject () == null)
            {

                return false;

            }

        }

        if (this.editor.isPrevious ())
        {

            return false;

        }

        return this.editor.getMessages (EditorsUIUtils.getImportantMessageFilter ()).size () > 0;

    }

    private Set<EditorMessage> getProjectComments ()
    {

        return this.editor.getMessages (new DefaultEditorMessageFilter (this.proj,
                                                                        ProjectCommentsMessage.MESSAGE_TYPE));

    }

    private Set<EditorMessage> getChatMessages ()
    {

        return this.editor.getMessages (new EditorMessageFilter ()
        {

            @Override
            public boolean accept (EditorMessage m)
            {

                if ((m.getMessageType ().equals (EditorChatMessage.MESSAGE_TYPE))
                    &&
                    (!m.isDealtWith ())
                   )
                {

                    return true;

                }

                return false;

            }

        });

    }

    private Set<EditorMessage> getProjectMessages ()
    {

        final EditorInfoBox _this = this;

        final String projId = this.proj.getId ();

        return this.editor.getMessages (new EditorMessageFilter ()
        {

            @Override
            public boolean accept (EditorMessage m)
            {

                if (!projId.equals (m.getForProjectId ()))
                {

                    return false;

                }

                if ((m.getMessageType ().equals (NewProjectMessage.MESSAGE_TYPE))
                    ||
                    (m.getMessageType ().equals (NewProjectResponseMessage.MESSAGE_TYPE))
                    ||
                    (m.getMessageType ().equals (UpdateProjectMessage.MESSAGE_TYPE))
                    ||
                    (m.getMessageType ().equals (ProjectEditStopMessage.MESSAGE_TYPE))
                   )
                {

                    return true;

                }

                return false;

            }

        });

    }

    private Set<EditorMessage> getImportantMessages ()
    {

        if (this.editor.isPrevious ())
        {

            return new HashSet ();

        }

        final EditorInfoBox _this = this;

        String _projId = "";

        if (this.proj != null)
        {

            _projId = this.proj.getId ();

        }

        final String projId = _projId;

        Set<EditorMessage> mess = this.editor.getMessages (m ->
        {

            if (!EditorsUIUtils.getImportantMessageFilter ().accept (m))
            {

                return false;

            }

            if (m.getMessageType ().equals (EditorChatMessage.MESSAGE_TYPE))
            {

                return false;

            }

            if (_this.showProjectInfo)
            {

                if (projId.equals (m.getForProjectId ()))
                {

                    return false;

                }

            }

            return true;

        });

        return mess;

    }
    public ProjectEditor getProjectEditor ()
    {

        return this.projEditor;


    }
    public EditorEditor getEditor ()
    {

        return this.editor;

    }

/*
    private void update ()
    {

        if (this.proj != null)
        {

            // TODO: Fix this.
            if (((AbstractProjectViewer) this.viewer).getProject () == null)
            {

                // We are closing down.
                return;

            }

        }

        this.onlineStatus.setVisible (false);
        this.other.setVisible (false);
        this.projectMessages.setVisible (false);
        this.importantMessages.setVisible (false);

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
            //this.onlineStatus.setText (this.editor.getOnlineStatus ().getName ());
            this.onlineStatus.setText ("");
            this.onlineStatus.setVisible (true);
            this.onlineStatus.setMaximumSize (this.onlineStatus.getPreferredSize ());

        }

        if (!this.editor.isPending ())
        {

            UIUtils.setAsButton (this.editorInfo);

            if (!this.editor.isPrevious ())
            {

                this.editorInfo.setToolTipText (String.format (getUIString (editors,LanguageStrings.editor,view,info,tooltip,currenteditor),
                                                               //"Click to send a message to %s, right click to see the menu",
                                                               this.editor.getMainName ()));

            } else {

                this.editorInfo.setToolTipText (getUIString (editors,LanguageStrings.editor,view,info,tooltip,previouseditor));
                //"Right click to see the menu");

            }

        } else {

            if (!this.editor.isInvitedByMe ())
            {

                this.other.setText (String.format (getUIString (editors,LanguageStrings.editor,view,LanguageStrings.other,pendingeditor,invitereceived),
                                                   //"Received: %s",
                                                   Environment.formatDate (this.editor.getDateCreated ())));

            } else {

                this.other.setText (String.format (getUIString (editors,LanguageStrings.editor,view,LanguageStrings.other,pendingeditor,invitesent),
                                                   //"Invited: %s",
                                                   Environment.formatDate (this.editor.getDateCreated ())));

            }

            this.other.setVisible (true);

        }

        //final String projId = this.projectViewer.getProject ().getId ();

        Set<EditorMessage> mess = this.getImportantMessages ();

        int ms = mess.size ();

        this.importantMessages.setForeground (java.awt.Color.black);

        if (ms > 0)
        {

            this.importantMessages.setForeground (java.awt.Color.red);

            this.importantMessages.setToolTipText (String.format (getUIString (editors,LanguageStrings.editor,view,importantmessages,tooltip),
                                                                  //"%s new/important message%s requiring your attention, click to view them",
                                                                  Environment.formatNumber (ms)));
                                                                  //(ms == 1 ? "" : "s"),
                                                                  //(ms == 1 ? "s" : "")));

            this.importantMessages.setText (String.format ("%s",
                                                           Environment.formatNumber (ms)));

            this.importantMessages.setVisible (true);

        }

        //if (this.editor.isPending ())
        //{

        //    this.importantMessages.setVisible (false);

        //}


        if (this.editor.isPrevious ())
        {

            this.onlineStatus.setIcon (Environment.getIcon (Constants.ERROR_RED_ICON_NAME,
                                                            Constants.ICON_MENU_INNER));
            this.onlineStatus.setToolTipText (getUIString (editors,LanguageStrings.editor,view,previouseditor,onlinestatus,tooltip));
            //"This is a previous {contact}."));
            this.onlineStatus.setText ("");
            this.onlineStatus.setMaximumSize (this.onlineStatus.getPreferredSize ());

            this.onlineStatus.setVisible (true);

        }

        if ((this.showProjectInfo)
            &&
            ((this.projEditor != null)
             ||
             (this.editorProject)
            )
           )
        {

            if (this.projEditor != null)
            {

                this.other.setVisible (true);
                xxx do this
                this.other.setText (Environment.replaceObjectNames (this.projEditor.getStatusMessage ()));

            }

            int undealtWithCount = 0;

            // Get undealt with messages that are not chat.
            // If there is just one then show it, otherwise show a link that will display a popup of them.
            Set<EditorMessage> projMess = this.getProjectMessages ();

            for (EditorMessage em : projMess)
            {

                if (!em.isDealtWith ())
                {

                    undealtWithCount++;

                }

            }

            int ps = projMess.size ();

            this.projectMessages.setForeground (java.awt.Color.black);

            if (undealtWithCount > 0)
            {

                this.projectMessages.setForeground (java.awt.Color.red);

                this.projectMessages.setToolTipText (String.format (getUIString (editors,LanguageStrings.editor,view,projecteditor,undealtwithmessagecount,tooltip),
                                                                    //"%s {project} message%s requiring your attention, click to view them",
                                                                    Environment.formatNumber (undealtWithCount)));
                                                                    //(undealtWithCount == 1 ? "" : "s"),
                                                                    //(undealtWithCount == 1 ? "s" : "")));

            } else {

                this.projectMessages.setToolTipText (String.format (getUIString (editors,LanguageStrings.editor,view,projecteditor,undealtwithmessagecount,tooltip),
                                                                    //"%s {project} message%s, click to view them",
                                                                    Environment.formatNumber (ps)));
                                                                    //(projMess.size () == 1 ? "" : "s"),
                                                                    //(projMess.size () == 1 ? "s" : "")));

            }

            this.projectMessages.setText (String.format ("%s",
                                                         Environment.formatNumber (ps)));

            this.projectMessages.setVisible (true);

        }

        this.comments.setVisible (false);

        if (this.showProjectInfo)
        {

            AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

            int commCount = 0;

            if (!this.editor.isPending ())
            {

                this.comments.setVisible (true);
                this.comments.setForeground (java.awt.Color.black);

                // Get undealt with messages that are not chat.
                // If there is just one then show it, otherwise show a link that will display a popup of them.
                Set<EditorMessage> comments = this.editor.getMessages (new DefaultEditorMessageFilter (pv.getProject (),
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

                    this.comments.setToolTipText (Environment.replaceObjectNames (String.format (getUIString (editors,LanguageStrings.editor,view,projectcomments,(this.projEditor != null ? received : sent),tooltip),
                                                                                                //"%s {comment%s} %s %s",
                                                                                                 Environment.formatNumber (commCount),
                                                                                                 //(commCount == 1 ? "" : "s"),
                                                                                                 //(this.projEditor != null ? "from" : "sent to"),
                                                                                                 this.editor.getShortName ())));

                } else {

                    if (this.projEditor != null)
                    {

                        this.comments.setToolTipText (Environment.replaceObjectNames (String.format (getUIString (editors,LanguageStrings.editor,view,noprojectcomments,received,tooltip),
                                                                                                    //"%s has not sent you any {comments} yet.",
                                                                                                     this.editor.getShortName ())));

                    } else {

                        this.comments.setToolTipText (Environment.replaceObjectNames (String.format (getUIString (editors,LanguageStrings.editor,view,noprojectcomments,sent,tooltip),
                                                                                                    //"You have not sent any {comments} to %s yet.",
                                                                                                     this.editor.getShortName ())));

                    }

                }

                this.comments.setText (Environment.formatNumber (commCount));

                this.comments.setEnabled (commCount > 0);

            }

        }

        this.chat.setVisible (false);

        Set<EditorMessage> chatMessages = this.getChatMessages ();

        int chatMessagesSize = chatMessages.size ();

        if (chatMessagesSize > 0)
        {

            this.chat.setForeground (java.awt.Color.red);

            this.chat.setToolTipText (Environment.replaceObjectNames (String.format (getUIString (editors,LanguageStrings.editor,view,unreadchatmessages,tooltip),
                                                                                     //"%s unread chat message%s",
                                                                                     Environment.formatNumber (chatMessagesSize))));

            this.chat.setText (Environment.formatNumber (chatMessagesSize));

            this.chat.setVisible (true);

        }

        if (this.isShowAttentionBorder ())
        {

            this.editorInfo.setBorder (new CompoundBorder (new MatteBorder (0, 2, 0, 0, UIUtils.getColor ("#ff0000")),
                                                           UIUtils.createPadding (0, 5, 0, 0)));

        } else {

            this.editorInfo.setBorder (null);

        }

        this.validate ();
        this.repaint ();

    }
*/

    public void addDeleteAllMessagesMenuItem (ContextMenu menu)
    {

        final EditorInfoBox _this = this;

        if (Environment.isDebugModeEnabled ())
        {

            if ((this.proj != null)
                &&
                (this.proj.getProjectEditor (_this.editor) != null)
               )
            {

                menu.getItems ().add (QuollMenuItem.builder ()
                    .label (new SimpleStringProperty ("Remove {project} editor [Debug option]"))
                    .iconName (StyleClassNames.DELETE)
                    .onAction (ev ->
                    {

                        QuollPopup.yesConfirmTextEntryBuilder ()
                            .inViewer (this.viewer)
                            .title (new SimpleStringProperty ("Remove {project} editor?"))
                            .description (new SimpleStringProperty (String.format ("To remove <b>%s</b> as a {project} editor please enter <b>Yes</b> in the box below.  Note: this will also remove all {project} related message types for this {project} (project-new, project-new-response, project-update, project-edit-stop, project-comments)",
                                                                                   this.editor.getMainName ())))
                            .confirmButtonLabel (new SimpleStringProperty ("Yes, delete them"))
                            .cancelButtonLabel (new SimpleStringProperty ("No, keep them"))
                            .onConfirm (eev ->
                            {

                                this.loadMessagesForEditor ();

                                final Set<EditorMessage> messages = _this.editor.getMessages (new DefaultEditorMessageFilter (_this.proj,
                                                            NewProjectMessage.MESSAGE_TYPE,
                                                            NewProjectResponseMessage.MESSAGE_TYPE,
                                                            UpdateProjectMessage.MESSAGE_TYPE,
                                                            ProjectEditStopMessage.MESSAGE_TYPE,
                                                            ProjectCommentsMessage.MESSAGE_TYPE));

                                try
                                {

                                    EditorsEnvironment.deleteMessages (messages);

                                    EditorsEnvironment.removeProjectEditor (_this.proj.getProjectEditor (_this.editor));

                                } catch (Exception e) {

                                    Environment.logError ("Unable to delete messages for editor: " +
                                                          _this.editor,
                                                          e);

                                    ComponentUtils.showErrorMessage (_this.viewer,
                                                                     new SimpleStringProperty ("Unable to delete messages for editor."));

                                    return;

                                }

                                QuollPopup.messageBuilder ()
                                    .inViewer (_this.viewer)
                                    .title (new SimpleStringProperty ("{Project} editor removed"))
                                    .message (new SimpleStringProperty ("All associated {project} messages have been deleted."))
                                    .closeButton ()
                                    .build ();

                            })
                            .build ();

                    })
                    .build ());

            }

            menu.getItems ().add (QuollMenuItem.builder ()
                .label (new SimpleStringProperty ("Delete Contact completely [debug option]"))
                .iconName (StyleClassNames.DELETE)
                .onAction (ev ->
                {

                    QuollPopup.yesConfirmTextEntryBuilder ()
                        .description (new SimpleStringProperty ("Enter <b>Yes</b> to completely delete the Contact."))
                        .onConfirm (eev ->
                        {

                            try
                            {

                                EditorsEnvironment.deleteEditor (this.editor);

                            } catch (Exception e) {

                                Environment.logError ("Unable to delete editor: " + this.editor,
                                                      e);

                                ComponentUtils.showErrorMessage (new SimpleStringProperty ("Unable to delete editor."));

                            }

                        })
                        .build ();

                })
                .build ());

            menu.getItems ().add (QuollMenuItem.builder ()
                .label (new SimpleStringProperty ("Delete all messages for types [Debug option]"))
                .iconName (StyleClassNames.DELETE)
                .onAction (ev ->
                {

                    VBox b = new VBox ();

                    Set<String> types = new LinkedHashSet<> ();

                    types.add (NewProjectMessage.MESSAGE_TYPE);
                    types.add (UpdateProjectMessage.MESSAGE_TYPE);
                    types.add (NewProjectResponseMessage.MESSAGE_TYPE);
                    types.add (ProjectEditStopMessage.MESSAGE_TYPE);
                    types.add (ProjectCommentsMessage.MESSAGE_TYPE);
                    types.add (InviteMessage.MESSAGE_TYPE);
                    types.add (InviteResponseMessage.MESSAGE_TYPE);
                    types.add (EditorChatMessage.MESSAGE_TYPE);
                    types.add (EditorInfoMessage.MESSAGE_TYPE);
                    types.add (EditorRemovedMessage.MESSAGE_TYPE);

                    final Map<String, CheckBox> cbs = new HashMap<> ();

                    for (String t : types)
                    {

                        CheckBox cb = QuollCheckBox.builder ()
                            .label (new SimpleStringProperty (t))
                            .build ();

                        cbs.put (t,
                                 cb);

                        b.getChildren ().add (cb);

                    }

                    QuollPopup.questionBuilder ()
                        .inViewer (_this.viewer)
                        .title (new SimpleStringProperty ("Delete types of message"))
                        .message (b)
                        .confirmButtonLabel (new SimpleStringProperty ("Yes, delete them"))
                        .onConfirm (eev ->
                        {

                            _this.loadMessagesForEditor ();

                            Set<String> selTypes = new LinkedHashSet ();

                            for (String t : cbs.keySet ())
                            {

                                if (cbs.get (t).isSelected ())
                                {

                                    selTypes.add (t);

                                }

                            }

                            Set<EditorMessage> toDel = _this.editor.getMessages (null,
                                                                                 selTypes.toArray (new String[selTypes.size ()]));

                            try
                            {

                                EditorsEnvironment.deleteMessages (toDel);

                            } catch (Exception e) {

                                Environment.logError ("Unable to delete messages for editor: " +
                                                      _this.editor,
                                                      e);

                                ComponentUtils.showErrorMessage (_this.viewer,
                                                                 new SimpleStringProperty ("Unable to delete messages for editor."));

                                return;

                            }

                            for (EditorMessage m : toDel)
                            {

                                _this.editor.removeMessage (m);

                            }

                            QuollPopup.messageBuilder ()
                                .inViewer (_this.viewer)
                                .title (new SimpleStringProperty ("Selected message types deleted"))
                                .closeButton ()
                                .message (new SimpleStringProperty ("All message for selected types have been deleted."))
                                .build ();

                        })
                        .build ();

                })
                .build ());

        }

    }

    private void loadMessagesForEditor ()
    {

        if (this.editor.messagesLoaded ())
        {

            return;

        }

        try
        {

            EditorsEnvironment.loadMessagesForEditor (this.editor);

        } catch (Exception e) {

            Environment.logError ("Unable to load messages for editor: " +
                                  this.editor,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (editors,LanguageStrings.editor,view,actionerror));
                                      //"Unable to load messages for editor.");

            return;

        }

    }

    public void addSendMessageMenuItem (ContextMenu menu)
    {

        final EditorInfoBox _this = this;

        if (this.editor.isPrevious ())
        {

            return;

        }

        final boolean pending = this.editor.isPending ();

        if (!pending)
        {

            menu.getItems ().add (QuollMenuItem.builder ()
                .label (editors,LanguageStrings.editor,view,popupmenu,items,sendmessage)
                .iconName (StyleClassNames.MESSAGE)
                .onAction (ev ->
                {

                    try
                    {

                        this.viewer.sendMessageToEditor (this.editor);

                    } catch (Exception e) {

                        Environment.logError ("Unable to show editor: " +
                                              this.editor,
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (editors,LanguageStrings.editor,view,actionerror));
                                                  //"Unable to show {editor}.");

                    }

                })
                .build ());

        }

    }

    public void addShowImportantMessagesMenuItem (ContextMenu menu)
    {

        if (this.editor.isPrevious ())
        {

            return;

        }

        final EditorInfoBox _this = this;

        final boolean pending = this.editor.isPending ();

        if (!pending)
        {

            final Set<EditorMessage> messages = this.editor.getMessages (m ->
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

            });

            if (messages.size () > 0)
            {

                menu.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,popupmenu,items,importantmessages),
                                                         Environment.formatNumber (messages.size ())))
                    .iconName (StyleClassNames.IMPORTANT)
                    .onAction (ev ->
                    {

                        try
                        {

                          EditorsUIUtils.showImportantMessagesForEditor (this.editor,
                                                                         this.viewer,
                                                                         null);

                        } catch (Exception e) {

                            Environment.logError ("Unable to show project messages for editor: " +
                                                  this.editor,
                                                  e);

                            ComponentUtils.showErrorMessage (this.viewer,
                                                             getUILanguageStringProperty (editors,LanguageStrings.messages,show,important,actionerror));
                                                      //"Unable to {project} messages for editor.");

                            return;

                        }

                    })
                    .build ());

            }

        }

    }

    public void addProjectSentAndUpdatesMenuItem (ContextMenu menu)
    {

        final EditorInfoBox _this = this;

        final boolean pending = this.editor.isPending ();

        //boolean isEditorProject = this.projectViewer.getProject ().isEditorProject ();

        if ((!pending)
            &&
            (this.showProjectInfo)
            &&
            (this.proj != null)
           )
        {

            final Set<EditorMessage> messages = this.editor.getMessages (new DefaultEditorMessageFilter (this.proj,
                                                                                                         NewProjectMessage.MESSAGE_TYPE,
                                                                                                         NewProjectResponseMessage.MESSAGE_TYPE,
                                                                                                         UpdateProjectMessage.MESSAGE_TYPE,
                                                                                                         ProjectEditStopMessage.MESSAGE_TYPE));

            if (messages.size () > 0)
            {

                menu.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,popupmenu,items,projectupdates),
                                                         Environment.formatNumber (messages.size ())))
                    .iconName (Project.OBJECT_TYPE)
                    .onAction (ev ->
                    {

                        try
                        {

                            EditorsUIUtils.showProjectMessagesForEditor (this.editor,
                                                                         (AbstractProjectViewer) this.viewer,
                                                                         null);

                        } catch (Exception e) {

                            Environment.logError ("Unable to show project messages for editor: " +
                                                  this.editor,
                                                  e);

                            ComponentUtils.showErrorMessage (this.viewer,
                                                             getUILanguageStringProperty (editors,LanguageStrings.messages,show,project,actionerror));
                                                      //"Unable to {project} messages for editor.")

                            return;

                        }

                    })
                    .build ());

            }

        }

    }

    public void addProjectsInvolvedWithMenuItem (ContextMenu menu)
    {

        final boolean pending = this.editor.isPending ();

        if (!pending)
        {

            // Get all the projects.
            int projCount = 0;

            try
            {

                projCount = Environment.getAllProjectInfos (Project.EDITOR_PROJECT_TYPE).size ();

            } catch (Exception e) {

                Environment.logError ("Unable to get all projects",
                                      e);

            }

            if (projCount > 0)
            {

                menu.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,popupmenu,items,projectsuserediting),
                                                         this.editor.mainNameProperty (),
                                                         Environment.formatNumber (projCount)))
                    .iconName (Project.OBJECT_TYPE)
                    .onAction (ev ->
                    {

                        try
                        {

                            EditorsUIUtils.showProjectsUserIsEditingForEditor (this.editor,
                                                                               this.viewer);

                        } catch (Exception e) {

                            Environment.logError ("Unable to show projects user is editing for editor: " +
                                                  this.editor,
                                                  e);

                            ComponentUtils.showErrorMessage (this.viewer,
                                                             getUILanguageStringProperty (editors,LanguageStrings.editor,showprojectscontactisediting,actionerror));
                                                      //String.format ("Unable to show {projects} you are editing for %s.",
                                                        //             _this.editor.getShortName ()));

                            return;

                        }

                    })
                    .build ());

            }

            final Set<EditorMessage> messages = this.editor.getMessages (m ->
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

                return true;

            });

            if (messages.size () > 0)
            {

                menu.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.editor,view,popupmenu,items,projectscontactediting),
                                                         this.editor.mainNameProperty (),
                                                         Environment.formatNumber (messages.size ())))
                    .iconName (Project.OBJECT_TYPE)
                    .onAction (ev ->
                    {

                        try
                        {

                            EditorsUIUtils.showProjectsEditorIsEditingForUser (this.editor,
                                                                               this.viewer);

                        } catch (Exception e) {

                            Environment.logError ("Unable to show projects for editor: " +
                                                  this.editor,
                                                  e);

                            ComponentUtils.showErrorMessage (this.viewer,
                                                             getUILanguageStringProperty (editors,LanguageStrings.editor,showprojectscontactisediting,actionerror));
                                                      //String.format ("Unable to show {projects} %s is editing for you.",
                                                        //             _this.editor.getShortName ()));

                            return;

                        }

                    })
                    .build ());

            }

        }

    }

    public void addShowCommentsMenuItem (ContextMenu menu)
    {

        final EditorInfoBox _this = this;

        if (this.editor.isPending ())
        {

            return;

        }

        if (this.proj == null)
        {

            return;

        }

        boolean isEditorProject = this.proj.isEditorProject ();

        final Set<EditorMessage> messages = this.editor.getMessages (m ->
        {

            return ((m.getMessageType ().equals (ProjectCommentsMessage.MESSAGE_TYPE))
                    &&
                    (_this.proj.getId ().equals (m.getForProjectId ())));

        });

        String suffix = (this.projEditor != null ? "received" : "sent");

        if ((isEditorProject)
            &&
            (messages.size () > 0)
           )
        {

            menu.getItems ().add (QuollMenuItem.builder ()
                .label (editors,LanguageStrings.editor,view,popupmenu,items,commentssent)
                .iconName (StyleClassNames.COMMENT)
                .onAction (ev ->
                {

                    try
                    {

                        EditorsUIUtils.showAllCommentsForEditor (this.editor,
                                                                 (AbstractProjectViewer) this.viewer,
                                                                 null);

                    } catch (Exception e) {

                        Environment.logError ("Unable to show comments from editor: " +
                                              this.editor,
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (editors,viewcommentserror));
                                                  //"Unable to show {comments} from editor.");

                        return;

                    }

                })
                .build ());

        } else {

            Iterator<EditorMessage> iter = messages.iterator ();

            if (messages.size () > 0)
            {

                final ProjectCommentsMessage message = (ProjectCommentsMessage) messages.iterator ().next ();

                menu.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (editors,LanguageStrings.editor,view,popupmenu,items,(this.projEditor != null ? lastcommentsreceived : lastcommentssent),
                                                         Environment.formatNumber (message.getComments ().size ())))
                    .iconName (StyleClassNames.FIND)
                    .onAction (ev ->
                    {

                        EditorsUIUtils.showProjectComments (message,
                                                            (AbstractProjectViewer) this.viewer,
                                                            null);

                    })
                    .build ());

            }

            if (messages.size () > 1)
            {

                menu.getItems ().add (QuollMenuItem.builder ()
                    .label (editors,LanguageStrings.editor,view,popupmenu,items,(this.projEditor != null ? commentsreceived : commentssent))
                    .iconName (StyleClassNames.COMMENT)
                    .onAction (ev ->
                    {

                        try
                        {

                            EditorsUIUtils.showAllCommentsForEditor (this.editor,
                                                                     (AbstractProjectViewer) this.viewer,
                                                                     null);

                        } catch (Exception e) {

                            Environment.logError ("Unable to show comments from editor: " +
                                                  this.editor,
                                                  e);

                            ComponentUtils.showErrorMessage (this.viewer,
                                                             getUILanguageStringProperty (editors,viewcommentserror));
                                                      //"Unable to show {comments} from editor.");

                            return;

                        }

                    })
                    .build ());

            }

        }

    }

    public void addSendOrUpdateProjectMenuItem (ContextMenu menu)
    {

        if ((this.editor.isPrevious ())
            ||
            (this.editor.isPending ())
           )
        {

            return;

        }

        if (this.proj == null)
        {

            return;

        }

        final boolean pending = this.editor.isPending ();

        boolean isEditorProject = this.proj.isEditorProject ();

        if ((!pending)
            &&
            (!isEditorProject)
           )
        {

            this.loadMessagesForEditor ();

            // Find out what was the last project message sent.
            Set<EditorMessage> messages = this.editor.getMessages (new DefaultEditorMessageFilter (this.proj,
                                                                                                   NewProjectMessage.MESSAGE_TYPE,
                                                                                                   NewProjectResponseMessage.MESSAGE_TYPE,
                                                                                                   ProjectEditStopMessage.MESSAGE_TYPE,
                                                                                                   UpdateProjectMessage.MESSAGE_TYPE));

            EditorMessage last = null;

            for (EditorMessage m : messages)
            {

                last = m;

            }

            boolean addSend = false;
            boolean addUpdate = false;

            if ((last == null)
                ||
                (last instanceof ProjectEditStopMessage)
               )
            {

                addSend = true;

            }

            if (last instanceof NewProjectMessage)
            {

                // Sent the project.  Do nothing since we have no response.
                addSend = true;
                //return;

            }

            if (last instanceof NewProjectResponseMessage)
            {

                NewProjectResponseMessage npr = (NewProjectResponseMessage) last;

                if (!npr.isAccepted ())
                {

                    addSend = true;

                } else {

                    addUpdate = true;

                }

            }

            if (last instanceof UpdateProjectMessage)
            {

                addUpdate = true;

            }

            if (addSend)
            {

                menu.getItems ().add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.SEND)
                    .label (editors,LanguageStrings.editor,view,popupmenu,items,sendproject)
                    .onAction (ev ->
                    {

                        EditorsUIUtils.showSendProject ((AbstractProjectViewer) this.viewer,
                                                        this.editor,
                                                        null);

                    })
                    .build ());

                return;

            }

            if (addUpdate)
            {

                menu.getItems ().add (QuollMenuItem.builder ()
                    .label (editors,LanguageStrings.editor,view,popupmenu,items,sendupdateproject)
                    .iconName (StyleClassNames.SEND)
                    .onAction (ev ->
                    {

                        EditorsUIUtils.showUpdateProject ((AbstractProjectViewer) this.viewer,
                                                          this.editor,
                                                          null);

                    })
                    .build ());

            } else {

                menu.getItems ().add (QuollMenuItem.builder ()
                    .label (editors,LanguageStrings.editor,view,popupmenu,items,sendproject)
                    .iconName (StyleClassNames.SEND)
                    .onAction (ev ->
                    {

                        EditorsUIUtils.showSendProject ((AbstractProjectViewer) this.viewer,
                                                        this.editor,
                                                        null);

                    })
                    .build ());

            }

        }

    }

    public void addUpdateEditorInfoMenuItem (ContextMenu menu)
    {

        final boolean pending = this.editor.isPending ();

        if (!pending)
        {

            menu.getItems ().add (QuollMenuItem.builder ()
                .label (editors,LanguageStrings.editor,view,popupmenu,items,updatecontactinfo)
                .iconName (StyleClassNames.EDIT)
                .onAction (ev ->
                {

                    EditorsUIUtils.updateEditorInfo (this.viewer,
                                                     this.editor);

                })
                .build ());

        }

    }

    public void addRemoveEditorMenuItem (ContextMenu menu)
    {

        final EditorInfoBox _this = this;

        if (this.editor.isPrevious ())
        {

            return;

        }

        menu.getItems ().add (QuollMenuItem.builder ()
            .label (editors,LanguageStrings.editor,view,popupmenu,items,removecontact)
            .iconName (StyleClassNames.DELETE)
            .onAction (ev ->
            {

                EditorsUIUtils.showRemoveEditor (this.viewer,
                                                 this.editor,
                                                 null);

            })
            .build ());
    }

    public void addShowAllMessagesMenuItem (final ContextMenu menu)
    {

        menu.getItems ().add (QuollMenuItem.builder ()
            .label (editors,LanguageStrings.editor,view,popupmenu,items,allmessages)
            .iconName (StyleClassNames.FIND)
            .onAction (ev ->
            {

                EditorsUIUtils.showAllMessagesForEditor (this.editor,
                                                         this.viewer,
                                                         null);

            })
            .build ());

    }

/*
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
*/
    public void addFullPopupListener ()
    {

        final EditorInfoBox _this = this;

        this.setOnContextMenuRequested (ev ->
        {

            ContextMenu cm = new ContextMenu ();

            this.addSendMessageMenuItem (cm);
            this.addSendOrUpdateProjectMenuItem (cm);
            this.addShowImportantMessagesMenuItem (cm);

            if (this.editor.isPending ())
            {

                cm.getItems ().add (QuollMenuItem.builder ()
                    .label (editors,LanguageStrings.editor,view,popupmenu,items,resendinvite)
                    .iconName (StyleClassNames.NOTIFY)
                    .onAction (eev ->
                    {

                        EditorsEnvironment.sendInvite (this.editor.getEmail (),
                                                       this.viewer);

                    })
                    .build ());

            }

            this.addShowAllMessagesMenuItem (cm);

            this.addUpdateEditorInfoMenuItem (cm);

            this.addRemoveEditorMenuItem (cm);

            this.addDeleteAllMessagesMenuItem (cm);

            ev.consume ();

            this.getProperties ().put ("context-menu", cm);

            cm.setAutoFix (true);
            cm.setAutoHide (true);
            cm.setHideOnEscape (true);
            cm.show (this,
                    ev.getScreenX (),
                    ev.getScreenY ());

        });

    }

    public void addBasicPopupListener ()
    {

        this.setOnContextMenuRequested (ev ->
        {

            ContextMenu cm = new ContextMenu ();

            this.addDeleteAllMessagesMenuItem (cm);

            this.addSendMessageMenuItem (cm);

            this.addUpdateEditorInfoMenuItem (cm);

            ev.consume ();

            this.editorInfo.getProperties ().put ("context-menu", cm);

            cm.setAutoFix (true);
            cm.setAutoHide (true);
            cm.setHideOnEscape (true);
            cm.show (this.editorInfo,
                    ev.getScreenX (),
                    ev.getScreenY ());

        });

    }

}
