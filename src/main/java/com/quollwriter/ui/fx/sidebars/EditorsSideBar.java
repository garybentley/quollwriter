package com.quollwriter.ui.fx.sidebars;

import java.util.*;

import javafx.embed.swing.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.scene.image.*;
import javafx.collections.*;

import com.quollwriter.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.editors.EditorsEnvironment;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.ui.sidebars.EditorPanel;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

/**
 * The editors/contacts a user has.
 */
public class EditorsSideBar<E extends AbstractViewer> extends SideBarContent
{

    public static final String SIDEBAR_ID = "editors";

    private VBox content = null;
    private TabPane tabs = null;

    private VBox noEditors = null;
    private VBox firstLogin = null;

    private Notification notification = null;

    private Map<String, Tab> specialTabs = new HashMap<> ();
    private Tab editorsTab = null;

    private QuollMenuButton statusBut = null;
    private QuollButton loginBut = null;

    private Map<EditorEditor, IPropertyBinder> editorBinders = new HashMap<> ();

    public EditorsSideBar (E viewer)
    {

        super (viewer);

        this.getBinder ().addSetChangeListener (EditorsEnvironment.getEditors (),
                                                ev ->
        {

            if (ev.wasAdded ())
            {

                // Add...

            }

            if (ev.wasRemoved ())
            {

                IPropertyBinder b = this.editorBinders.get (ev.getElementRemoved ());

                if (b != null)
                {

                    b.dispose ();

                }

                this.editorBinders.remove (ev.getElementRemoved ());

                Tab rem = null;

                // Remove any tab.
                for (Tab t : this.tabs.getTabs ())
                {

                    if (ev.getElementRemoved ().equals (t.getUserData ()))
                    {

                        rem = t;
                        break;

                    }

                }

                Tab _rem = rem;

                UIUtils.runLater (() ->
                {

                    this.tabs.getTabs ().remove (_rem);

                });

            }

        });

        this.tabs = new TabPane ();
        this.tabs.setTabClosingPolicy (TabPane.TabClosingPolicy.ALL_TABS);
        this.tabs.setSide (UserProperties.tabsLocationProperty ().getValue ().equals (Constants.TOP) ? Side.TOP : Side.BOTTOM);
        this.tabs.setTabDragPolicy (TabPane.TabDragPolicy.REORDER);
        VBox.setVgrow (this.tabs,
                       Priority.ALWAYS);

        this.content = new VBox ();
        VBox.setVgrow (this.content,
                       Priority.ALWAYS);
        this.content.getChildren ().add (this.tabs);
        this.getChildren ().add (this.content);
        VBox.setVgrow (this,
                       Priority.ALWAYS);

        this.createEditorList ();

    }

    private void createEditorList ()
    {

        VBox edBox = new VBox ();
        edBox.getStyleClass ().add (StyleClassNames.CONTENT);
        VBox.setVgrow (edBox,
                       Priority.ALWAYS);
        HBox.setHgrow (edBox,
                       Priority.ALWAYS);
        ScrollPane sp = new ScrollPane (edBox);

        this.firstLogin = new VBox ();
        this.firstLogin.getStyleClass ().add (StyleClassNames.FIRSTLOGIN);
        this.firstLogin.managedProperty ().bind (this.firstLogin.visibleProperty ());

        this.firstLogin.getChildren ().add (QuollLabel.builder ()
            .styleClassName (StyleClassNames.TITLE)
            .label (LanguageStrings.editors,LanguageStrings.sidebar,firstlogin,title)
            .build ());
        this.firstLogin.getChildren ().add (QuollTextView.builder ()
            .styleClassName (StyleClassNames.TEXT)
            .text (getUILanguageStringProperty (LanguageStrings.editors,LanguageStrings.sidebar,firstlogin,text))
            .inViewer (this.viewer)
            .build ());

        QuollButton loginBut = QuollButton.builder ()
            .tooltip (LanguageStrings.editors,LanguageStrings.sidebar,firstlogin,buttons,login,tooltip)
            .label (LanguageStrings.editors,LanguageStrings.sidebar,firstlogin,buttons,login,text)
            .iconName (this.getStatusStyleClassName (null))
            .onAction (ev ->
            {

                EditorsEnvironment.goOnline (null,
                                             () ->
                                             {

                                                // TODO? this.updateView ();

                                             },
                                             // On cancel
                                             null,
                                             // On error
                                             null);

            })
            .build ();

        this.firstLogin.getChildren ().add (loginBut);
        this.firstLogin.managedProperty ().bind (this.firstLogin.visibleProperty ());
        this.firstLogin.setVisible (false);

        edBox.getChildren ().add (this.firstLogin);

        this.noEditors = new VBox ();
        this.noEditors.getStyleClass ().add (StyleClassNames.NOEDITORS);
        this.noEditors.managedProperty ().bind (this.noEditors.visibleProperty ());

        this.noEditors.getChildren ().add (Header.builder ()
            .styleClassName (StyleClassNames.TITLE)
            .title (LanguageStrings.editors,LanguageStrings.sidebar,nocontacts,title)
            .build ());
        this.noEditors.getChildren ().add (QuollTextView.builder ()
            .styleClassName (StyleClassNames.TEXT)
            .text (getUILanguageStringProperty (LanguageStrings.editors,LanguageStrings.sidebar,nocontacts,text))
            .inViewer (this.viewer)
            .build ());

        this.noEditors.getChildren ().add (QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                .tooltip (LanguageStrings.editors,LanguageStrings.sidebar,nocontacts,buttons,sendinvite,tooltip)
                .label (LanguageStrings.editors,LanguageStrings.sidebar,nocontacts,buttons,sendinvite,text)
                .onAction (ev ->
                {

                    EditorsUIUtils.showInviteEditor (this.viewer);

                })
                .build ())
            .build ());

        edBox.getChildren ().add (this.noEditors);

        this.getBinder ().addListChangeListener (EditorsEnvironment.getCurrentEditors (),
                                                 ev ->
        {

            this.noEditors.setVisible (EditorsEnvironment.getCurrentEditors ().size () == 0);

        });

        this.noEditors.setVisible (EditorsEnvironment.getCurrentEditors ().size () == 0);

        edBox.getChildren ().add (this.createEditorsSection (invitesfromothers,
                                                             StyleClassNames.INVITESFORME,
                                                             EditorsEnvironment.getInvitesForMe ()));

        edBox.getChildren ().add (this.createEditorsSection (pendinginvites,
                                                             StyleClassNames.PENDINGINVITES,
                                                             EditorsEnvironment.getPendingInvites ()));

        edBox.getChildren ().add (this.createEditorsSection (allcontacts,
                                                             StyleClassNames.ALLCONTACTS,
                                                             EditorsEnvironment.getCurrentEditors ()));

        Tab t = new Tab ();
        t.getStyleClass ().add (StyleClassNames.CONTACTS);
        t.setContent (sp);
        t.setClosable (false);

        IconBox b = IconBox.builder ()
            .iconName (StyleClassNames.CONTACTS)
            .build ();
        t.setGraphic (b);

        this.editorsTab = t;

        this.tabs.getTabs ().add (t);

    }

    private Node createEditorsSection (String         type,
                                       String         className,
                                       ObservableList<EditorEditor> eds)
    {

        VBox box = new VBox ();

        QuollTextView hp = QuollTextView.builder ()
            .text (getUILanguageStringProperty (LanguageStrings.editors,LanguageStrings.sidebar,type,text))
            .inViewer (this.viewer)
            .build ();

        box.getChildren ().add (hp);

        for (EditorEditor ed : eds)
        {

            try
            {

                box.getChildren ().add (new EditorInfoBox (ed,
                                                           this.viewer,
                                                           false,
                                                           this.getBinder ()));

            } catch (Exception e) {

                Environment.logError ("Unable to create editor box for: " +
                                      ed,
                                      e);

            }

        }

        this.getBinder ().addListChangeListener (eds,
                                                 ev ->
        {

            hp.setVisible (eds.size () == 0);

            while (ev.next ())
            {

                if (ev.wasRemoved ())
                {

                    for (EditorEditor ed : ev.getRemoved ())
                    {

                        Node rem = null;

                        for (Node n : box.getChildren ())
                        {

                            if (n instanceof EditorInfoBox)
                            {

                                EditorInfoBox b = (EditorInfoBox) n;

                                if (b.getEditor ().equals (ed))
                                {

                                    rem = n;
                                    break;

                                }

                            }

                        }

                        if (rem != null)
                        {

                            Node _rem = rem;

                            UIUtils.runLater (() ->
                            {

                                box.getChildren ().remove (_rem);

                            });

                        }

                    }

                }

                if (ev.wasAdded ())
                {

                    UIUtils.runLater (() ->
                    {

                        for (EditorEditor ed : ev.getAddedSubList ())
                        {

                            try
                            {

                                box.getChildren ().add (new EditorInfoBox (ed,
                                                                           this.viewer,
                                                                           false,
                                                                           this.getBinder ()));

                            } catch (Exception e) {

                                Environment.logError ("Unable to create editor box for: " +
                                                      ed,
                                                      e);

                            }

                        }

                    });

                }

            }

        });

        AccordionItem acc = AccordionItem.builder ()
            .styleClassName (className)
            .title (UILanguageStringsManager.createStringPropertyWithBinding (() ->
            {

                return getUILanguageStringProperty (Arrays.asList (LanguageStrings.editors,LanguageStrings.sidebar,type,title),
                                                    Environment.formatNumber (eds.size ())).getValue ();


            },
            eds))
            .openContent (box)
            .build ();

        acc.managedProperty ().bind (acc.visibleProperty ());
        acc.setVisible (eds.size () > 0);

        return acc;

    }

    @Override
    public SideBar createSideBar ()
    {

        StringProperty title = getUILanguageStringProperty (editors,LanguageStrings.sidebar,LanguageStrings.title);

        SideBar sb = SideBar.builder ()
            .title (title)
            .activeTitle (title)
            //.contextMenu ()?
            .headerIconClassName (StyleClassNames.CONTACTS)
            .styleClassName (StyleClassNames.CONTACTS)
            .styleSheet (StyleClassNames.CONTACTS)
            .withScrollPane (false)
            .canClose (true)
            .headerControls (this.getHeaderControls ())
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (SIDEBAR_ID)
            .build ();

        sb.addEventHandler (SideBar.SideBarEvent.CLOSE_EVENT,
                            ev ->
        {

            // TODO?
            //EditorsEnvironment.removeEditorChangedListener (this);
            //EditorsEnvironment.removeUserOnlineStatusListener (this);

        });

        return sb;

    }

    private void updateStatusButton ()
    {

        EditorEditor.OnlineStatus status = EditorsEnvironment.getUserOnlineStatus ();

        if (status == null)
        {

            status = EditorEditor.OnlineStatus.offline;

        }

        StringProperty toolTip = null;
        StringProperty info = null;

        if (status == EditorEditor.OnlineStatus.offline)
        {

            toolTip = getUILanguageStringProperty (editors,LanguageStrings.sidebar,headercontrols,items,onlinestatus,online,button,tooltip);
            //"Click to go online";

            info = getUILanguageStringProperty (editors,LanguageStrings.sidebar,headercontrols,items,onlinestatus,offline,LanguageStrings.notification);
            //"You have been logged out.";

        } else {

            toolTip = getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.sidebar,headercontrols,items,onlinestatus,button,tooltip),
                                                   status.getName ());
            //status.getName () + ", click to change your status";

            info = getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.sidebar,headercontrols,items,onlinestatus,update,LanguageStrings.notification),
                                //"Your status is now <b>%s</b>.",
                                                status.getName ());

        }

        UIUtils.setTooltip (this.statusBut,
                            toolTip);
        this.statusBut.setIconName (this.getStatusStyleClassName (status));
        this.loginBut.setVisible (!EditorsEnvironment.isUserLoggedIn ());
        this.statusBut.setVisible (!this.loginBut.isVisible ());

    }

    public Set<Node> getHeaderControls ()
    {

        final EditorsSideBar _this = this;

        Set<Node> buts = new LinkedHashSet<> ();

        this.loginBut = QuollButton.builder ()
            .iconName (this.getStatusStyleClassName (EditorEditor.OnlineStatus.offline))
            .tooltip (editors,LanguageStrings.sidebar,headercontrols,items,onlinestatus,online,button,tooltip)
            .onAction (ev ->
            {

                QuollPopup np = null;

                if (EditorsEnvironment.hasLoginCredentials ())
                {
System.out.println ("HERE");
                    np = QuollPopup.notificationBuilder ()
                        .message (editors,LanguageStrings.sidebar,headercontrols,items,onlinestatus,online,LanguageStrings.notification)
                        .styleClassName (StyleClassNames.LOADING)
                        .inViewer (this.viewer)
                        .build ();

                    this.viewer.showPopup (np,
                                           this.loginBut,
                                           Side.BOTTOM);

                }

                QuollPopup _np = np;

                EditorsEnvironment.goOnline (null,
                                             () ->
                                             {

                                                 UIUtils.runLater (() ->
                                                 {

                                                     if (_np != null)
                                                     {

                                                         _np.close ();

                                                     }

                                                     QuollPopup qp = QuollPopup.notificationBuilder ()
                                                         .message (getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.sidebar,headercontrols,items,onlinestatus,update,LanguageStrings.notification),
                                                                                                EditorsEnvironment.getUserOnlineStatus ().getName ()))
                                                         .styleClassName (StyleClassNames.LOADING)
                                                         .inViewer (this.viewer)
                                                         .hideIn (3 * Constants.SEC_IN_MILLIS)
                                                         .build ();

                                                     this.loginBut.setDisable (false);
                                                     this.loginBut.setVisible (false);
                                                     this.statusBut.setVisible (true);

                                                     this.viewer.showPopup (qp,
                                                                            this.statusBut,
                                                                            Side.BOTTOM);

                                                });

                                             },
                                             // On cancel
                                             null,
                                             // On error
                                             exp ->
                                             {

                                                 Environment.logError ("Unable to go online",
                                                                       exp);

                                                 UIUtils.runLater (() ->
                                                 {

                                                     this.loginBut.setDisable (true);

                                                     if (_np != null)
                                                     {

                                                         _np.close ();

                                                     }

                                                    EditorsUIUtils.showLoginError (getUILanguageStringProperty (editors,login,errors,invalidcredentials),
                                                                                    //"Unable to login, please check your email and password.",
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

            })
            .build ();

        this.loginBut.managedProperty ().bind (this.loginBut.visibleProperty ());
        this.loginBut.setVisible (!EditorsEnvironment.isUserLoggedIn ());

        this.statusBut = QuollMenuButton.builder ()
            .iconName (this.getStatusStyleClassName (EditorEditor.OnlineStatus.offline))
            .items (() ->
            {

                Set<MenuItem> its = new LinkedHashSet<> ();

                if (EditorsEnvironment.isUserLoggedIn ())
                {

                    Set<EditorEditor.OnlineStatus> statuses = new LinkedHashSet<> ();
                    statuses.add (EditorEditor.OnlineStatus.online);
                    statuses.add (EditorEditor.OnlineStatus.busy);
                    statuses.add (EditorEditor.OnlineStatus.away);
                    statuses.add (EditorEditor.OnlineStatus.snooze);

                    for (EditorEditor.OnlineStatus v : statuses)
                    {

                        its.add (QuollMenuItem.builder ()
                            .iconName (this.getStatusStyleClassName (v))
                            .label (v.getName ())
                            .onAction (ev ->
                            {

                                try
                                {

                                    EditorsEnvironment.setUserOnlineStatus (v);

                                } catch (Exception e) {

                                    Environment.logError ("Unable to set user status to: " +
                                                          v,
                                                          e);

                                    ComponentUtils.showErrorMessage (_this.viewer,
                                                                     getUILanguageStringProperty (editors,LanguageStrings.sidebar,headercontrols,items,onlinestatus,update,actionerror));
                                                              //"Unable to change your status, please contact Quoll Writer support for assistance.");

                                }

                            })
                            .build ());

                    }

                    its.add (QuollMenuItem.builder ()
                        .iconName (this.getStatusStyleClassName (EditorEditor.OnlineStatus.offline))
                        .label (editors,LanguageStrings.sidebar,headercontrols,items,onlinestatus,popupmenu,items,logout)
                        .onAction (ev ->
                        {

                            EditorsEnvironment.goOffline ();
                            this.statusBut.setVisible (false);
                            this.loginBut.setVisible (true);

                            UIUtils.runLater (() ->
                            {

                                QuollPopup qp = QuollPopup.notificationBuilder ()
                                    .message (getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.sidebar,headercontrols,items,onlinestatus,offline,LanguageStrings.notification),
                                                                           EditorsEnvironment.getUserOnlineStatus ().getName ()))
                                    .styleClassName (StyleClassNames.MESSAGE)
                                    .inViewer (this.viewer)
                                    .hideIn (3 * Constants.SEC_IN_MILLIS)
                                    .build ();

                                this.viewer.showPopup (qp,
                                                       this.loginBut,
                                                       Side.BOTTOM);

                            });

                        })
                        .build ());

                }

                return its;

            })
            .build ();
        this.statusBut.managedProperty ().bind (this.statusBut.visibleProperty ());

        this.loginBut.visibleProperty ().addListener ((pr, oldv, newv) ->
        {

            this.statusBut.setVisible (!this.loginBut.isVisible ());

        });

        buts.add (this.loginBut);
        buts.add (this.statusBut);

        this.updateStatusButton ();

        this.getBinder ().addChangeListener (EditorsEnvironment.userOnlineStatusProperty (),
                                             (pr, oldv, newv) ->
        {

            UIUtils.runLater (() ->
            {

                this.updateStatusButton ();

            });

        });

        buts.add (QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (editors,LanguageStrings.sidebar,headercontrols,items,sendinvite,tooltip))
            .iconName (StyleClassNames.NEW)
            .onAction (ev ->
            {

                EditorsUIUtils.showInviteEditor (this.viewer);

            })
            .build ());

        buts.add (QuollMenuButton.builder ()
            .iconName (StyleClassNames.OPTIONS)
            .tooltip (getUILanguageStringProperty (editors,LanguageStrings.sidebar,headercontrols,items,config,tooltip))
            .items (() ->
            {

                Set<MenuItem> its = new LinkedHashSet<> ();

                // Get all previous editors.
                int prevCount = 0;

                for (EditorEditor ed : EditorsEnvironment.getEditors ())
                {

                    if (ed.isPrevious ())
                    {

                        prevCount++;

                    }

                }

                if (prevCount > 0)
                {

                    its.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.sidebar,headercontrols,items,config,popupmenu,items,previouscontacts,text),
                                                             Environment.formatNumber (prevCount)))
                        .iconName (StyleClassNames.PREVIOUSCONTACTS)
                        .onAction (ev ->
                        {

                            this.showPreviousEditors ();

                        })
                        .build ());
                }

                its.add (QuollMenuItem.builder ()
                    .label (editors,LanguageStrings.sidebar,headercontrols,items,config,popupmenu,items,updatenameavatar,text)
                    .iconName (StyleClassNames.UPDATEINFO)
                    .onAction (ev ->
                    {

                        EditorsUIUtils.updateYourInfo (this.viewer);

                    })
                    .build ());

                its.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.CHANGEPASSWORD)
                    .label (editors,LanguageStrings.sidebar,headercontrols,items,config,popupmenu,items,changepassword,text)
                    .onAction (ev ->
                    {

                        EditorsUIUtils.showChangePassword (this.viewer);

                    })
                    .build ());

                if (!EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.QW_EDITORS_SERVICE_HAS_LOGGED_IN_PROPERTY_NAME))
                {

                    its.add (QuollMenuItem.builder ()
                        .label (editors,LanguageStrings.sidebar,headercontrols,items,config,popupmenu,items,resendaccountconfirmationemail,text)
                        .iconName (StyleClassNames.RESENDCONFIRMEMAIL)
                        .onAction (ev ->
                        {

                            UIUtils.openURL (this.viewer,
                                             String.format (Environment.getQuollWriterWebsiteLink ("editor-mode/send-account-confirmation-email?email=%s",
                                                                                                   EditorsEnvironment.getUserAccount ().getEmail ())));

                        })
                        .build ());

                } else {

                      // If they have their password stored then display it.
                      final String edPass = EditorsEnvironment.getEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME);

                      if (edPass != null)
                      {

                          its.add (QuollMenuItem.builder ()
                            .iconName (StyleClassNames.SHOWPASSWORD)
                            .label (editors,LanguageStrings.sidebar,headercontrols,items,config,popupmenu,items,displaypassword,text)
                            .onAction (ev ->
                            {

                                String extra = "";

                                if (Environment.isDebugModeEnabled ())
                                {

                                    extra = String.format ("<br />Messaging Username: <b>%s</b>",
                                                           EditorsEnvironment.getUserAccount ().getMessagingUsername ());

                                }

                                QuollPopup.messageBuilder ()
                                    .inViewer (_this.viewer)
                                    .headerIconClassName (StyleClassNames.VIEW)
                                    .title (editors,user,displaypassword,LanguageStrings.popup,title)
                                    .message (getUILanguageStringProperty (Arrays.asList (editors,user,displaypassword,LanguageStrings.popup,text),
                                                                    //"Note: your password is being displayed because you have checked the <i>Save password</i> box for logging into the Editors service.<br /><br />Your login details are:<br /><br />Email address: <b>%s</b><br />Password: <b>%s</b>%s",
                                                                           EditorsEnvironment.getUserAccount ().getEmail (),
                                                                           edPass))
                                    .closeButton ()
                                    .build ();
                                                                    //extra));

                            })
                            .build ());

                      } else {

                          its.add (QuollMenuItem.builder ()
                            .iconName (StyleClassNames.RESETPASSWORD)
                            .label (editors,LanguageStrings.sidebar,headercontrols,items,config,popupmenu,items,resetpassword,text)
                            .onAction (ev ->
                            {

                                UIUtils.openURL (this.getViewer (),
                                                 String.format (Environment.getQuollWriterWebsiteLink ("editor-mode/send-password-reset-email?email=%s",
                                                                                                       null),
                                                                EditorsEnvironment.getUserAccount ().getEmail ()));

                            })
                            .build ());

                      }

                }

                its.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.HELP)
                    .label (editors,LanguageStrings.sidebar,headercontrols,items,config,popupmenu,items,help,text)
                    .onAction (ev ->
                    {

                        UIUtils.openURL (this.getViewer (),
                                         Environment.getQuollWriterHelpLink ("editor-mode/overview",
                                                                             null));

                    })
                    .build ());

                its.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.DELETEACCOUNT)
                    .label (editors,LanguageStrings.sidebar,headercontrols,items,config,popupmenu,items,deleteaccount,text)
                    .onAction (ev ->
                    {

                        EditorsUIUtils.showDeleteAccount (this.viewer);

                    })
                    .build ());

                return its;

            })
            .build ());

        return buts;

    }

    @Override
    public void init (State s)
    {

        super.init (s);

    }
/*
    @Override
    public void editorChanged (EditorChangedEvent ev)
    {

        EditorEditor ed = ev.getEditor ();

        this.updateView ();

        for (int i = 0; i < this.tabs.getTabCount (); i++)
        {

            Component comp = this.tabs.getComponentAt (i);

            if (comp instanceof EditorPanel)
            {

                if (ed == ((EditorPanel) comp).getEditor ())
                {

                    JLabel th = (JLabel) this.tabs.getTabComponentAt (i);

                    BufferedImage av = ed.getDisplayAvatar ();

                    if (av != null)
                    {

                        BufferedImage nim = UIUtils.getScaledImage (av,
                                                                    28);

                        if (nim.getHeight () > 28)
                        {

                            nim = UIUtils.getScaledImage (av,
                                                          28,
                                                          28);

                        }

                        th.setIcon (new ImageIcon (nim));
                        th.setText (null);

                    } else {

                        th.setIcon (null);
                        th.setText (ed.getShortName ());
                    }

                    String tt = ed.getShortName ();

                    if (ed.getOnlineStatus () != null)
                    {

                        tt += ", " + ed.getOnlineStatus ().getName ();

                    }

                    th.setToolTipText (String.format ("<html>%s</html>",
                                                      tt));

                }

            }

        }

    }
*/
/*
    private JMenuItem createStatusMenuItem (final EditorEditor.OnlineStatus status)
    {

        final EditorsSideBar _this = this;

        JMenuItem mi = UIUtils.createMenuItem (status.getName (),
                                               this.getStatusIconName (status),
                                               new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                try
                {

                    EditorsEnvironment.setUserOnlineStatus (status);

                } catch (Exception e) {

                    Environment.logError ("Unable to set user status to: " +
                                          status,
                                          e);

                    UIUtils.showErrorMessage (_this.viewer,
                                              getUIString (editors,sidebar,headercontrols,items,onlinestatus,update,actionerror));
                                              //"Unable to change your status, please contact Quoll Writer support for assistance.");

                }

            }

        });

        return mi;

    }
*/
/*
    private void updateUserOnlineStatus (EditorEditor.OnlineStatus status)
    {

        if (status == null)
        {

            status = EditorEditor.OnlineStatus.offline;

        }

        String iconName = this.getStatusIconName (status);
        String toolTip = null;
        String info = null;

        if (status == EditorEditor.OnlineStatus.offline)
        {

            toolTip = getUIString (editors,sidebar,headercontrols,items,onlinestatus,online,button,tooltip);
            //"Click to go online";

            info = getUIString (editors,sidebar,headercontrols,items,onlinestatus,offline,LanguageStrings.notification);
            //"You have been logged out.";

        } else {

            toolTip = String.format (getUIString (editors,sidebar,headercontrols,items,onlinestatus,button,tooltip),
                                     status.getName ());
            //status.getName () + ", click to change your status";

            info = String.format (getUIString (editors,sidebar,headercontrols,items,onlinestatus,update,LanguageStrings.notification),
                                //"Your status is now <b>%s</b>.",
                                  status.getName ());

        }

        this.statusButton.setIcon (Environment.getIcon (iconName,
                                                        Constants.ICON_SIDEBAR));
        this.statusButton.setToolTipText (toolTip);

        if (this.statusButton.isShowing ())
        {

            this.showNotification (iconName,
                                   info,
                                   2,
                                   this.statusButton);

        }

    }
*/
/*
    public void userOnlineStatusChanged (UserOnlineStatusEvent ev)
    {

        this.updateUserOnlineStatus (ev.getStatus ());

    }
*/
    @Override
    public State getState ()
    {

        return super.getState ();

    }

    public void showChatBox (final EditorEditor ed)
                      throws GeneralException
    {

        this.showEditor (ed);

        // TODO
/*
        final EditorsSideBar _this = this;

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                EditorPanel edPanel = _this.getEditorPanel (ed);

                if (edPanel != null)
                {

                    edPanel.showChatBox ();

                }

            }

        });
*/
    }
/*
    private void updateView ()
    {

        Set<EditorEditor> invitesForMe = new LinkedHashSet ();
        Set<ProjectEditor> projEds = new LinkedHashSet ();
        Set<EditorEditor> others = new LinkedHashSet ();
        Set<EditorEditor> invitesIveSent = new LinkedHashSet ();

        int edsSize = 0;

        for (EditorEditor ed : EditorsEnvironment.getEditors ())
        {

            if (ed.isPrevious ())
            {

                continue;

            }

            if (ed.isRejected ())
            {

                continue;

            }

            edsSize++;

            if (ed.isPending ())
            {

                if (!ed.isInvitedByMe ())
                {

                    invitesForMe.add (ed);

                } else {

                    invitesIveSent.add (ed);

                }

            } else {

                    others.add (ed);

//                ProjectEditor pe = this.projectViewer.getProject ().getProjectEditor (ed);

//                if (pe != null)
//                {

//                    projEds.add (pe);

//                } else {

//                    others.add (ed);

//                }

            }

        }

        this.otherEditors.setVisible (others.size () > 0);

        try
        {

            this.otherEditors.update (others);

        } catch (Exception e) {

            Environment.logError ("Unable to update other editors section with editors: " +
                                  others,
                                  e);

            this.otherEditors.setVisible (false);

            UIUtils.showErrorMessage (this.viewer,
                                      getUIString (editors,vieweditorserror));
                                      //"Unable to display others section, please contact Quoll Writer support for assistance.");

        }

        this.invitesForMe.setVisible (invitesForMe.size () > 0);

        try
        {

            this.invitesForMe.update (invitesForMe);

        } catch (Exception e) {

            Environment.logError ("Unable to update invites for me editors section with editors: " +
                                  invitesForMe,
                                  e);

            this.invitesForMe.setVisible (false);

            UIUtils.showErrorMessage (this.viewer,
                                      getUIString (editors,vieweditorserror));
                                      //"Unable to display invites from others section, please contact Quoll Writer support for assistance.");

        }

        this.invitesIveSent.setVisible (invitesIveSent.size () > 0);

        try
        {

            this.invitesIveSent.update (invitesIveSent);

        } catch (Exception e) {

            Environment.logError ("Unable to update invites ive sent section with editors: " +
                                  invitesIveSent,
                                  e);

            this.invitesIveSent.setVisible (false);

            UIUtils.showErrorMessage (this.viewer,
                                      getUIString (editors,vieweditorserror));
                                      //"Unable to display invites I've sent section, please contact Quoll Writer support for assistance.");

        }

        this.noEditors.setVisible (edsSize == 0);

        this.firstLogin.setVisible (false);

        // May not have an account anymore.
        if ((EditorsEnvironment.getUserAccount () != null)
            &&
            (EditorsEnvironment.getUserAccount ().getLastLogin () == null)
           )
        {

            this.firstLogin.setVisible (true);
            this.noEditors.setVisible (false);

        }

        this.validate ();
        this.repaint ();

    }
*/
    public void showPreviousEditors ()
    {

        Tab t = this.specialTabs.get ("previous-editors");

        if (t == null)
        {

            Node prev = this.createEditorsSection (previouscontacts,
                                                   StyleClassNames.PREVIOUSEDITORS,
                                                   EditorsEnvironment.getPreviousEditors ());

            ScrollPane sp = new ScrollPane (prev);

            t = new Tab ();
            t.setContent (sp);
            IconBox b = IconBox.builder ()
                .iconName (StyleClassNames.PREVIOUSEDITORS)
                .build ();
            t.setGraphic (b);
            this.specialTabs.put ("previous-editors",
                                  t);

            this.tabs.getTabs ().add (t);

        }

        this.tabs.getSelectionModel ().select (t);

    }

    public void showEditor (EditorEditor ed)
                     throws GeneralException
    {

        final EditorsSideBar _this = this;

        for (Tab t : this.tabs.getTabs ())
        {

            if (ed.equals (t.getUserData ()))
            {

                this.tabs.getSelectionModel ().select (t);
                return;

            }

        }

        if (!ed.messagesLoaded ())
        {

            try
            {

                EditorsEnvironment.loadMessagesForEditor (ed);

            } catch (Exception e) {

                throw new GeneralException ("Unable to load messages for editor: " +
                                            ed,
                                            e);

            }

        }

        EditorPanel ep = new EditorPanel (this.getViewer (),
                                          ed,
                                          this.getBinder ());

        Tab t = new Tab ();
        t.getStyleClass ().add (StyleClassNames.EDITOR);
        t.setContent (ep);
        t.setUserData (ed);
        StringProperty nameProp = new SimpleStringProperty ();

        PropertyBinder edBinder = new PropertyBinder ();

        edBinder.addChangeListener (ed.mainNameProperty (),
                                    (pr, oldv, newv) ->
        {

            nameProp.setValue (newv);

            if (ed.getMainAvatar () != null)
            {

                nameProp.setValue (null);

            }

        });
        nameProp.setValue (ed.mainNameProperty ().getValue ());
        t.textProperty ().bind (nameProp);

        IconBox h = IconBox.builder ()
            .iconName (StyleClassNames.AVATAR)
            .image (ed.mainAvatarProperty ())
            .binder (edBinder)
            .build ();
        t.setGraphic (h);

        h.pseudoClassStateChanged (StyleClassNames.NOAVATAR_PSEUDO_CLASS, ed.mainAvatarProperty ().getValue () == null);

        edBinder.addChangeListener (ed.mainAvatarProperty (),
                                    (pr, oldv, newv) ->
        {

            h.pseudoClassStateChanged (StyleClassNames.NOAVATAR_PSEUDO_CLASS, newv == null);

        });

        edBinder.addChangeListener (ed.onlineStatusProperty (),
                                    (pr, oldv, newv) ->
        {

            String tt = ed.getMainName ();

            if (ed.getOnlineStatus () != null)
            {

                tt += ", " + ed.getOnlineStatus ().getName ();

            }

            t.setTooltip (UIUtils.createTooltip (new SimpleStringProperty (tt)));

        });

        String tt = ed.getMainName ();

        if (ed.getOnlineStatus () != null)
        {

            tt += ", " + ed.getOnlineStatus ().getName ();

        }

        t.setTooltip (UIUtils.createTooltip (new SimpleStringProperty (tt)));

        this.editorBinders.put (ed,
                                edBinder);

        this.tabs.getTabs ().add (t);
        this.tabs.getSelectionModel ().select (t);

        ContextMenu cm = new ContextMenu ();
        cm.getItems ().add (QuollMenuItem.builder ()
            .label (buttons,close)
            .iconName (StyleClassNames.CLOSE)
            .onAction (ev ->
            {

                this.tabs.getTabs ().remove (t);

            })
            .build ());

        t.setContextMenu (cm);

    }
/*
    private class EditorsSection extends AccordionItem
    {

        private Box editorsListWrapper = null;
        private AbstractViewer viewer = null;
        private StringProperty title = null;
        private JLabel help = null;
        private JLabel noEditorsHelp = null;
        private ComponentListener listener = null;
        private JComponent content = null;

        public EditorsSection (StringProperty title,
                               StringProperty help,
                               StringProperty noEditorsHelp,
                               AbstractViewer viewer)
        {

            super ("");

            this.title = title;
            this.viewer = viewer;

            final EditorsSection _this = this;

            Box content = new Box (BoxLayout.Y_AXIS);

            if (help != null)
            {

                this.help = UIUtils.createInformationLabel (help);
                this.help.setBorder (UIUtils.createPadding (0, 0, 5, 5));

                content.add (this.help);

            }

            if (noEditorsHelp != null)
            {

                this.noEditorsHelp = UIUtils.createInformationLabel (noEditorsHelp);
                this.noEditorsHelp.setBorder (UIUtils.createPadding (0, 0, 5, 5));

                content.add (this.noEditorsHelp);

            }

            this.editorsListWrapper = new Box (BoxLayout.Y_AXIS);

            content.add (this.editorsListWrapper);

            this.content = content;

        }

        @Override
        public JComponent getContent ()
        {

            return this.content;

        }

        private EditorInfoBox getEditorBox (EditorEditor ed)
                                     throws GeneralException
        {

            EditorInfoBox b = new EditorInfoBox (ed,
                                                 this.viewer,
                                                 false);

            b.setAlignmentX (Component.LEFT_ALIGNMENT);

            b.addFullPopupListener ();

            b.init ();

            b.addComponentListener (this.listener);

            return b;

        }

        public void updateForEditor (EditorEditor ed)
        {

            int count = this.editorsListWrapper.getComponentCount ();

            for (int i = 0; i < count; i++)
            {

                EditorInfoBox infBox = (EditorInfoBox) this.editorsListWrapper.getComponent (i);

                if (infBox.getEditor () == ed)
                {

                    this.setBorder (infBox,
                                    i == (count - 1));

                }

            }

        }

        public void update (Set<EditorEditor> eds)
                     throws GeneralException
        {

            this.setTitle (String.format (//"%s (%s)",
                                          this.title,
                                          Environment.formatNumber (eds.size ())));

            if (this.help != null)
            {

                this.help.setVisible (eds.size () > 0);

            }

            if (this.noEditorsHelp != null)
            {

                this.noEditorsHelp.setVisible (eds.size () == 0);

            }

            this.editorsListWrapper.removeAll ();

            EditorInfoBox last = null;

            for (EditorEditor ed : eds)
            {

                EditorInfoBox infBox = this.getEditorBox (ed);

                last = infBox;

                this.editorsListWrapper.add (infBox);

                this.setBorder (infBox,
                                false);

            }

            if (last != null)
            {

                this.setBorder (last,
                                true);

            }

        }

        public void updateForProjectEditors (Set<ProjectEditor> pes)
                     throws GeneralException
        {

            Set<EditorEditor> eds = new LinkedHashSet ();

            for (ProjectEditor pe : pes)
            {

                eds.add (pe.getEditor ());

            }

            this.update (eds);

        }

    }
*/
    public String getStatusStyleClassName (EditorEditor.OnlineStatus status)
    {

        String type = "offline";

        if (status != null)
        {

            type = status.getType ();

        }

        return type;
        //return Constants.ONLINE_STATUS_ICON_NAME_PREFIX + type;

    }

}
