package com.quollwriter.editors.ui.sidebars;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.*;
import java.awt.Image;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.util.Collection;
import java.util.Set;
import java.util.Vector;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.gentlyweb.utils.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.db.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.DnDTabbedPane;
import com.quollwriter.ui.components.TabHeader;
import com.quollwriter.ui.components.ScrollableBox;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.ui.events.*;

public class EditorsSideBar extends AbstractSideBar implements EditorChangedListener,
                                                               EditorMessageListener,
                                                               UserOnlineStatusListener
{
    
    public static final String NAME = "editors";
    
    private DnDTabbedPane tabs = null;
    private EditorFindPanel editorFindPanel = null;
    private JComponent noEditors = null;
    private JButton statusButton = null;
    private AccordionItem currentEditors = null;
    private JLabel noCurrentEditors = null;
    private JComponent currentEditorsWrapper = null;
    private JLabel currentEditorsHelp = null;

    private AccordionItem otherEditors = null;
    private JComponent otherEditorsWrapper = null;
    
    private AccordionItem invitesForMe = null;
    private JComponent invitesForMeWrapper = null;
    private JComponent notification = null;
    private JLabel undealtWithMessages = null;
    
    private JLabel projectCommentsMessage = null;
    
    private Map<String, JComponent> specialTabs = new HashMap ();

    public EditorsSideBar (AbstractProjectViewer v)
    {
        
        super (v);
                
        EditorsEnvironment.addEditorChangedListener (this);
        EditorsEnvironment.addEditorMessageListener (this);
        EditorsEnvironment.addUserOnlineStatusListener (this);
        
    }
    
    private void rebuildEditorList (EditorEditor.EditorStatus status)
    {
        
/*
        List<EditorEditor> eds = this.editors.get (status);

        AccordionItem ai = this.editorSections.get (status);

        // Just rebuild the list
        ai.setContent (this.createEditorsList (eds));

        ai.setVisible (eds.size () > 0);

        this.updateView ();
        this.validate ();
        this.repaint ();
*/
    }
    
    public void handleMessage (EditorMessageEvent ev)
    {
        
        EditorEditor ed = ev.getEditor ();
        
        if (ed.getEditorStatus () == EditorEditor.EditorStatus.pending)
        {
            
            // Don't need to do anything.
            return;
            
        }
        
        this.updateUndealtWithMessageCount ();        
        
        // If the editor is not visible then do nothing (may change).
        
        // If the editor is in a tab then add the message.
        
        // If the editor is not the current tab then flash the header.

        EditorPanel panel = this.getEditorPanel (ed);
        
        if (panel != null)
        {
            
            panel.handleEditorMessageEvent (ev);
            
            //panel.addMessage (ev.getMessage ());
            
        }           

    }
    
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
    
    public boolean canClose ()
    {
        
        return true;
        
    }
    
    @Override
    public void onShow ()
    {
        
        // TODO
        
    }
    
    @Override
    public void onHide ()
    {
        
    }
    
    @Override
    public void onClose ()
    {
        
        EditorsEnvironment.removeEditorChangedListener (this);
        EditorsEnvironment.removeEditorMessageListener (this);
        EditorsEnvironment.removeUserOnlineStatusListener (this);

        EditorsEnvironment.goOffline ();                            
        
    }
    
    public boolean removeOnClose ()
    {
        
        return false;
        
    }
    
    public String getIconType ()
    {
        
        return Constants.EDITORS_ICON_NAME;
        
    }
    
    public String getTitle ()
    {
        
        return "{Editors}";
        
    }
    
    public void panelShown (MainPanelEvent ev)
    {
                
    }
        
    private void update ()
    {
        
        if (!this.isVisible ())
        {
            
            return;
            
        }
                
    }
    
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
                    
                    UIUtils.showErrorMessage (_this.projectViewer,
                                              "Unable to change your status, please contact Quoll Writer support for assistance.");
                    
                }
                
            }
            
        });

        return mi;        
        
    }
    
    private void updateUserOnlineStatus (EditorEditor.OnlineStatus status)
    {
        
        if (status == null)
        {
            
            status = EditorEditor.OnlineStatus.offline;
            
        }
        
        String iconName = this.getStatusIconName (status);
        String toolTip = null;
        
        if (status == EditorEditor.OnlineStatus.offline)
        {
            
            toolTip = "Click to go online";
            
        } else {
            
            toolTip = status.getName () + ", click to change your status";
            
        }
            
        this.statusButton.setIcon (Environment.getIcon (iconName,
                                                        Constants.ICON_SIDEBAR));
        this.statusButton.setToolTipText (toolTip);
        
        this.hideNotification ();        
        
    }
    
    public void userOnlineStatusChanged (UserOnlineStatusEvent ev)
    {

        this.updateUserOnlineStatus (ev.getStatus ());
                
    }
    
    @Override
    public List<JComponent> getHeaderControls ()
    {

        final EditorsSideBar _this = this;
        
        List<JComponent> buts = new ArrayList ();        
                        
        this.statusButton = UIUtils.createButton (this.getStatusIconName (EditorEditor.OnlineStatus.offline),
                                                  Constants.ICON_SIDEBAR,
                                                  "Click to go online",
                                                  null);

        this.userOnlineStatusChanged (new UserOnlineStatusEvent (EditorsEnvironment.getUserOnlineStatus ()));
        
        this.statusButton.addActionListener (new ActionListener ()
        {
          
            public void actionPerformed (ActionEvent ev)
            {
              
                if (EditorsEnvironment.isUserLoggedIn ())
                {
                    
                    JPopupMenu m = new JPopupMenu ();

                    Set<EditorEditor.OnlineStatus> statuses = new LinkedHashSet ();
                    statuses.add (EditorEditor.OnlineStatus.online);
                    statuses.add (EditorEditor.OnlineStatus.busy);
                    statuses.add (EditorEditor.OnlineStatus.away);
                    statuses.add (EditorEditor.OnlineStatus.snooze);
                    
                    for (EditorEditor.OnlineStatus v : statuses)
                    {
                        
                        m.add (_this.createStatusMenuItem (v));
                        
                    }
                    
                    JMenuItem mi = UIUtils.createMenuItem ("Logout",
                                                           _this.getStatusIconName (null),
                                                           new ActionListener ()
                    {
                        
                        public void actionPerformed (ActionEvent ev)
                        {
                            
                            JLabel l = UIUtils.createLoadingLabel ("Logging out...");
                            l.setVisible (true);
                            _this.showNotification (l);

                            EditorsEnvironment.goOffline ();                            
                            
                        }
                        
                    });

                    m.add (mi);
                    
                    m.show (_this.statusButton,
                            10,
                            10);
                    
                } else {
              
                    final JLabel l = UIUtils.createLoadingLabel ("Logging in...");

                    if (EditorsEnvironment.hasLoginCredentials ())
                    {
                        
                        l.setVisible (true);
                        _this.showNotification (l);
                        
                    }
              
                    EditorsEnvironment.goOnline (null,
                                                 new ActionListener ()
                                                 {
                                                  
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        _this.hideNotification ();

                                                    }
                                                  
                                                 },
                                                 // On cancel
                                                 null,
                                                 // On error
                                                 new ActionListener ()
                                                 {
                                                    
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        _this.hideNotification ();
                                                        
                                                        EditorsUIUtils.showLoginError ("Unable to login, please check your email and password.",
                                                                                       new ActionListener ()
                                                                                       {
                                                                                        
                                                                                            public void actionPerformed (ActionEvent ev)
                                                                                            {
                                                                                                
                                                                                                EditorsEnvironment.goOnline (null,
                                                                                                                             null,
                                                                                                                             null,
                                                                                                                             null);
                                                                                                
                                                                                            }
                                                                                        
                                                                                       },
                                                                                       null);                                                        
                                                        
                                                    }
                                                    
                                                 });

                }
                                                                                                
            }
          
        });

        //this.updateUserOnlineStatus (EditorsEnvironment.getUserOnlineStatus ());
        
        buts.add (this.statusButton);
/*
        b = UIUtils.createButton (Constants.FIND_ICON_NAME,
                                          Constants.ICON_SIDEBAR,
                                          "Click to find editors",
                                          new ActionAdapter ()
                                          {
                                            
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                                
                                                  _this.toggleFindEditorsTab ();  
                                                
                                              }
                                            
                                          });

        buts.add (b);
  */                                        
        JButton b = UIUtils.createButton (Constants.NEW_ICON_NAME,
                                          Constants.ICON_SIDEBAR,
                                          "Click to invite a new {editor}",
                                          new ActionAdapter ()
                                          {
                                            
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                                
                                                  _this.showInvitePopup ();
                                                
                                              }
                                            
                                          });

        buts.add (b);        
        
        b = UIUtils.createButton (Constants.OPTIONS_ICON_NAME,
                                  Constants.ICON_SIDEBAR,
                                  "Click to view the config options",
                                  new ActionAdapter ()
                                  {
                                            
                                        public void actionPerformed (ActionEvent ev)
                                        {
                                                            
                                            JMenuItem mi = null;
  
                                            JPopupMenu popup = new JPopupMenu ();

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

                                                popup.add (UIUtils.createMenuItem (String.format ("View the previous {editors} (%s)",
                                                                                                  Environment.formatNumber (prevCount)),
                                                                                   Constants.STOP_ICON_NAME,
                                                                                   new ActionListener ()
                                                                                   {
                                                                                   
                                                                                        public void actionPerformed (ActionEvent ev)
                                                                                        {
                                                                                            
                                                                                            _this.showPreviousEditors ();
                                                                                                                                                                                
                                                                                        }
                                                                                   
                                                                                   }));
                                                
                                            }
                                          
                                          popup.add (UIUtils.createMenuItem ("Update your name/avatar",
                                                                             Constants.EDIT_ICON_NAME,
                                                                             new ActionListener ()
                                                                             {
                                                                             
                                                                                public void actionPerformed (ActionEvent ev)
                                                                                {
                                                                                    
                                                                                    EditorsUIUtils.updateYourInfo (_this.projectViewer);
                                                                                                                                                                        
                                                                                }
                                                                             
                                                                             }));
                                                     
                                          popup.add (UIUtils.createMenuItem ("Change your password",
                                                                             Constants.EDIT_ICON_NAME,
                                                                             new ActionListener ()
                                          {
                                            
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    EditorsUIUtils.showChangePassword (_this.projectViewer);
                                                    
                                                }
                                            
                                          }));

                                          if (!EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.QW_EDITORS_SERVICE_HAS_LOGGED_IN_PROPERTY_NAME))
                                          {
                                            
                                            popup.add (UIUtils.createMenuItem ("Resend confirmation email",
                                                                               Constants.SEND_ICON_NAME,
                                                                               new ActionListener ()
                                            {
                                              
                                                  public void actionPerformed (ActionEvent ev)
                                                  {
                                                      
                                                        UIUtils.openURL (_this.projectViewer,
                                                                         String.format (Environment.getQuollWriterWebsiteLink ("editor-mode/send-account-confirmation-email?email=%s",
                                                                                                                               EditorsEnvironment.getUserAccount ().getEmail ())));
                                                      
                                                  }
                                              
                                            }));                                            
                                            
                                          } else {
                                          
                                            // If they have their password stored then display it.
                                            final String edPass = EditorsEnvironment.getEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME);
                                            
                                            if (edPass != null)
                                            {
  
                                                popup.add (UIUtils.createMenuItem ("Display your password",
                                                                                   Constants.VIEW_ICON_NAME,
                                                                                   new ActionListener ()
                                                {
                                                
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        String extra = "";
                                                        
                                                        if (Environment.isDebugModeEnabled ())
                                                        {
                                                            
                                                            extra = String.format ("<br />Messaging Username: <b>%s</b>",
                                                                                   EditorsEnvironment.getUserAccount ().getMessagingUsername ());
                                                            
                                                        }
                                                        
                                                        UIUtils.showMessage ((PopupsSupported) _this.projectViewer,
                                                                             "Your Editors service password",
                                                                             String.format ("Note: your password is being displayed because you have checked the <i>Save password</i> box for logging into the Editors service.<br /><br />Your login details are:<br /><br />Email address: <b>%s</b><br />Password: <b>%s</b>%s",
                                                                                            EditorsEnvironment.getUserAccount ().getEmail (),
                                                                                            edPass,
                                                                                            extra));
                                                        
                                                    }
                                                
                                                }));
                                              
                                            } else {
                                              
                                              popup.add (UIUtils.createMenuItem ("Reset your password",
                                                                                 Constants.CANCEL_ICON_NAME,
                                                                                 new ActionListener ()
                                              {
                                                
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                                                                                
                                                        UIUtils.openURL (_this.projectViewer,
                                                                         String.format (Environment.getQuollWriterWebsiteLink ("editor-mode/send-password-reset-email?email=%s",
                                                                                                                               null),
                                                                                        EditorsEnvironment.getUserAccount ().getEmail ()));
                                                        
                                                    }
                                                
                                              }));                                            
                                              
                                            }
                                            
                                          }
                                          
                                          popup.add (UIUtils.createMenuItem ("Preferences",
                                                                             Constants.CONFIG_ICON_NAME,
                                                                             new ActionListener ()
                                          {
                                            
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    AbstractProjectViewer viewer = Environment.getFocusedProjectViewer ();
                                                    
                                                    viewer.showOptions ("editors");
                                                    
                                                }
                                            
                                          }));

                                          popup.add (UIUtils.createMenuItem ("Help",
                                                                             Constants.HELP_ICON_NAME,
                                                                             new ActionListener ()
                                          {
                                            
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    UIUtils.openURL (_this,
                                                                     Environment.getQuollWriterHelpLink ("editor-mode/sidebar",
                                                                                                         null));
                                                    
                                                }
                                            
                                          }));

                                          popup.add (UIUtils.createMenuItem ("Delete my account",
                                                                             Constants.DELETE_ICON_NAME,
                                                                             new ActionListener ()
                                          {
                                            
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    EditorsUIUtils.showDeleteAccount (_this.projectViewer);
                                                                                                        
                                                }
                                            
                                          }));

                                          JComponent s = (JComponent) ev.getSource ();
                            
                                          popup.show (s,
                                                      s.getWidth () / 2,
                                                      s.getHeight ());
                                             
                                      }
                                            
                                  });

        buts.add (b);

        return buts;        
        
    }

    private void createWelcomeTab ()
    {
        
        try
        {
        
            this.projectViewer.addHelpTextTab ("Welcome to the Editors Service",
                                               StringUtils.replaceString (Environment.getResourceFileAsString (Constants.EDITORS_SIDEBAR_FIRST_USE_HELP_FILE),
                                                                          "\n",
                                                                          ""),
                                               Constants.EDITORS_ICON_NAME,
                                               "editors-service-first-help");
                    
        } catch (Exception e) {
            
            Environment.logError ("Unable to get editors sidebar first use help file",
                                  e);
            
            return;
            
        }
                
    }
    
    private void createEditorList ()
    {
        
        final EditorsSideBar _this = this;
        
        // Get all our editors.
        List<EditorEditor> editors = EditorsEnvironment.getEditors ();        

        Box edBox = new ScrollableBox (BoxLayout.Y_AXIS);

        edBox.setOpaque (true);
        edBox.setBackground (UIUtils.getComponentColor ());

        edBox.setBorder (UIUtils.createPadding (0, 0, 0, 0));

        this.noEditors = new Box (BoxLayout.Y_AXIS);        
        
        this.noEditors.add (UIUtils.createBoldSubHeader ("<i>No current {editors}</i>",
                                                         null));
        
        JComponent noedsHelp = UIUtils.createHelpTextPane ("You currently have no {editors}.  Click on the button below to invite someone to be {an editor} for your {project}.",
                                                           this.projectViewer);
        noedsHelp.setBorder (null);
        this.noEditors.setBorder (new EmptyBorder (5, 5, 5, 5));
        
        Box bnoedsHelp = new Box (BoxLayout.Y_AXIS);
        bnoedsHelp.setAlignmentX (Component.LEFT_ALIGNMENT);
        bnoedsHelp.setBorder (UIUtils.createPadding (0, 5, 0, 5));
        bnoedsHelp.add (noedsHelp);
        
        this.noEditors.add (bnoedsHelp);
        
        this.noEditors.add (Box.createVerticalStrut (10));
                                
        JButton but = UIUtils.createToolBarButton (Constants.NEW_ICON_NAME,
                                                   "Click to invite someone to be {an editor} for your {project}",
                                                   null,
                                                   new ActionListener ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                EditorsUIUtils.showInviteEditor (_this.projectViewer);                
                
            }
            
        });

        but.setFont (but.getFont ().deriveFont ((float) 16));

        but.setText ("Invite {an Editor}");
        List<JButton> buts = new ArrayList ();
        buts.add (but);
        
        Box bbar = new Box (BoxLayout.X_AXIS);
        bbar.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        bbar.add (Box.createHorizontalGlue ());
        bbar.add (UIUtils.createButtonBar (buts));
        bbar.add (Box.createHorizontalGlue ());
        
        this.noEditors.add (bbar);

        edBox.add (this.noEditors);
                
        Box b = new Box (BoxLayout.Y_AXIS);
    
        this.currentEditorsHelp = UIUtils.createInformationLabel ("{Editors} you have sent this {project} to.");        
        this.currentEditorsHelp.setBorder (UIUtils.createPadding (0, 10, 5, 5));
        b.add (this.currentEditorsHelp);    
    
        this.noCurrentEditors = UIUtils.createInformationLabel ("You have no current {editors} for this {project}.  Right click on one of the {editors} in the <b>Other Editors</b> section below and select <b>Send {project}/{chapters}</b> to send them your {project}.");        
        this.noCurrentEditors.setBorder (UIUtils.createPadding (0, 10, 5, 5));        
        b.add (this.noCurrentEditors);                

        this.currentEditorsWrapper = new Box (BoxLayout.Y_AXIS);
        this.currentEditorsWrapper.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.currentEditorsWrapper.setBorder (UIUtils.createPadding (0, 10, 0, 5));
        
        b.add (this.currentEditorsWrapper);
        
        this.currentEditors = new AccordionItem ("",
                                                 null,
                                                 b);        
                
        this.currentEditors.init ();
  
        edBox.add (this.currentEditors);

        this.otherEditorsWrapper = new Box (BoxLayout.Y_AXIS);
        this.otherEditorsWrapper.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.otherEditorsWrapper.setBorder (UIUtils.createPadding (0, 10, 0, 5));
        
        AccordionItem otherEdsAi = new AccordionItem ("",
                                                      null,
                                                      this.otherEditorsWrapper);
                
        this.otherEditors = otherEdsAi;
                
        otherEdsAi.init ();        
        
        edBox.add (otherEdsAi);

        this.invitesForMeWrapper = new Box (BoxLayout.Y_AXIS);
        this.invitesForMeWrapper.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.invitesForMeWrapper.setBorder (UIUtils.createPadding (0, 10, 0, 5));

        AccordionItem invitesAi = new AccordionItem ("",
                                                     null,
                                                     this.invitesForMeWrapper);
                
        this.invitesForMe = invitesAi;
                
        invitesAi.init ();        
        
        edBox.add (invitesAi);

        JScrollPane sp = UIUtils.createScrollPane (edBox);
        sp.setBorder (null);
                
        this.tabs.add (sp,
                       0);
        this.tabs.setIconAt (0,
                             Environment.getIcon ("editors-list",
                                                  Constants.ICON_EDITORS_LIST_TAB_HEADER));
        
        this.updateView ();
        
    }
        
    private void updateView ()
    {
                
        Set<EditorEditor> invites = new LinkedHashSet ();
        Set<ProjectEditor> projEds = new LinkedHashSet ();
        Set<EditorEditor> others = new LinkedHashSet ();
                
        int edsSize = EditorsEnvironment.getEditors ().size ();
        int invitesCount = 0;
        
        int otherEdsCount = 0;
        
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
            
            if (ed.isPending ())
            {
                
                invites.add (ed);
                
                //invitesCount++;
                
            }
            
            ProjectEditor pe = this.projectViewer.getProject ().getProjectEditor (ed);
            
            if (pe != null)
            {
                
                projEds.add (pe);
                
            } else {
                
                others.add (ed);
                
            }
            
/*            
            if (!this.projectViewer.getProject ().isProjectEditor (ed))
            {
                
                if ((!ed.isInvitedByMe ())
                    &&
                    (ed.isPending ())
                   )
                {
                    
                    invitesCount++;
                    
                } else {
                
                    if (ed.isRejected ())
                    {
                
                        edsSize--;
                        
                    } else {
                        
                        otherEdsCount++;
                        
                    }
                    
                }
                
            }
  */          
        }
        
        int currProjEdsCount = 0;
        
        if (this.projectViewer.getProject ().getProjectEditors () != null)
        {
            
            currProjEdsCount = this.projectViewer.getProject ().getProjectEditors ().size ();
        
        }
        
        this.currentEditors.setTitle (String.format ("Current {Editors} (%s)",
                                                     currProjEdsCount));
        this.currentEditors.setVisible (currProjEdsCount > 0);
        this.currentEditorsWrapper.setVisible (currProjEdsCount > 0);
        this.noCurrentEditors.setVisible (currProjEdsCount == 0);
        this.currentEditorsHelp.setVisible (currProjEdsCount > 0);
        
        this.otherEditors.setVisible (others.size () > 0);
        this.otherEditors.setTitle (String.format ("Other {Editors} (%s)",
                                                   others.size ()));

        this.noEditors.setVisible (edsSize == 0);        
        
        this.invitesForMe.setVisible (invitesCount > 0);
        this.invitesForMe.setTitle (String.format ("Invites from others (%s)",
                                                   invitesCount));
        
        this.fillOtherEditorsList (others);
        this.fillCurrentEditorsList (projEds);
        this.fillInvitesEditorsList (invites);
        
        this.validate ();
        this.repaint ();
        
    }

    private void hideNotification ()
    {
        
        if (this.notification != null)
        {
            
            this.notification.setVisible (false);
            
        }
        
    }
    
    private void showNotification (JComponent c)
    {
        
        c.setVisible (true);
        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.notification.removeAll ();
        
        this.notification.add (c);
        this.notification.setVisible (true);
        
    }

    private void showNotification (String iconType,
                                   String message)
    {
        
        JLabel l = new JLabel (Environment.getIcon (iconType,
                                                    Constants.ICON_SIDEBAR_NOTIFICATION),
                               SwingConstants.LEFT);
                
        l.setText (String.format ("<html>%s</html>",
                                  Environment.replaceObjectNames ((message))));
        
        this.showNotification (l);
        
    }
    
    public JComponent getContent ()
    {

        final EditorsSideBar _this = this;    
    
        Box box = new Box (BoxLayout.Y_AXIS);
        box.setAlignmentX (Component.LEFT_ALIGNMENT);
        // Turn off the auto border.
        box.setBorder (UIUtils.createPadding (0, 0, 0, 0));
        box.setOpaque (true);
        box.setBackground (UIUtils.getComponentColor ());
        
        this.notification = new Box (BoxLayout.Y_AXIS);
        this.notification.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.notification.setVisible (false);
        this.notification.setBorder (UIUtils.createPadding (5, 5, 5, 5));
        
        box.add (this.notification);
        
        // Get a count of how many undealt with messages there are.
        this.undealtWithMessages = UIUtils.createClickableLabel ("",
                                                                 Environment.getIcon (Constants.ERROR_ICON_NAME,
                                                                                      Constants.ICON_TAB_HEADER),
                                                                 new ActionListener ()
                                                                 {
                                                        
                                                                    public void actionPerformed (ActionEvent ev)
                                                                    {
                                                            
                                                                        _this.showUndealtWithMessages ();
                                                            
                                                                    }
                                                        
                                                                 });
                
        this.undealtWithMessages.setBorder (UIUtils.createPadding (5, 5, 5, 5));

        this.updateUndealtWithMessageCount ();
                
        box.add (this.undealtWithMessages);
                        
        this.projectCommentsMessage = UIUtils.createClickableLabel ("",
                                                             Environment.getIcon (Constants.COMMENT_ICON_NAME,
                                                                                  Constants.ICON_TAB_HEADER),
                                                             new ActionListener ()
                                                             {
                                                                
                                                                public void actionPerformed (ActionEvent ev)
                                                                {
                                                                    
                                                                    _this.showProjectCommentsMessages ();
                                                                    
                                                                }
                                                                
                                                             });
                        
        this.projectCommentsMessage.setBorder (UIUtils.createPadding (5, 5, 5, 5));

        this.updateProjectCommentsMessage ();
                
        box.add (this.projectCommentsMessage);

        this.tabs = new DnDTabbedPane ();
        
        this.tabs.putClientProperty(com.jgoodies.looks.Options.NO_CONTENT_BORDER_KEY, Boolean.TRUE);
        //this.tabs.putClientProperty(com.jgoodies.looks.Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
        this.tabs.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.tabs.setTabLayoutPolicy (JTabbedPane.SCROLL_TAB_LAYOUT);
        this.tabs.setBorder (UIUtils.createPadding (5, 2, 0, 0));
        this.tabs.addChangeListener (new ChangeAdapter ()
        {
           
            public void stateChanged (ChangeEvent ev)
            {
                
            }
            
        });
                           
        box.add (this.tabs);

        this.createEditorList ();
                
        // See if this is the first time the user has seen the side bar.
        if (!EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.QW_EDITORS_SERVICE_EDITORS_SIDEBAR_SEEN_PROPERTY_NAME))
        {
            
            try
            {

                EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_EDITORS_SIDEBAR_SEEN_PROPERTY_NAME,
                                                       true);

            } catch (Exception e) {
                
                Environment.logError ("Unable to set editors sidebar seen property",
                                      e);
                
            }
            
            this.createWelcomeTab ();
            
        }
           
    this.createWelcomeTab ();
           
                
        return box;
                    
    }
/*
    public Dimension getPreferredSize ()
    {
        
        return new Dimension (300,
                              500);
        
    }
  */

    private void addTabCloseListener (final JComponent header,
                                      final JComponent content)
    {
        
        final EditorsSideBar _this = this;
        
        header.addMouseListener (new MouseEventHandler ()
        {
           
            @Override
            public void handlePress (MouseEvent ev)
            {
                
                _this.tabs.setSelectedComponent (content);
                
            }
           
            @Override
            public void handleMiddlePress (MouseEvent ev)
            {
                
                _this.tabs.remove (content);
                     
            }
            
            @Override
            public void fillPopup (JPopupMenu menu,
                                   MouseEvent ev)
            {
                
                menu.add (UIUtils.createMenuItem ("Close",
                                                  Constants.CLOSE_ICON_NAME,
                                                  new ActionListener ()
                                                  {
                                                       
                                                      public void actionPerformed (ActionEvent ev)
                                                      {
                                                           
                                                           _this.tabs.remove (content);
                                                           
                                                      }
                                                       
                                                  }));
                
            }
            
        });                
        
    }
  
    public void showPreviousEditors ()
    {
        
        JComponent c = this.specialTabs.get ("previous-editors");

        if (c != null)
        {
            
            this.tabs.remove (c);
                
        }
        
        final Box edBox = new ScrollableBox (BoxLayout.Y_AXIS);

        edBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        edBox.setBorder (UIUtils.createPadding (0, 10, 0, 5));
        edBox.setOpaque (true);
        edBox.setBackground (UIUtils.getComponentColor ());

        final Notification n = Notification.createHelpNotification (this.projectViewer,
                                                                    "{Editors} you have removed or have removed you in the past.",
                                                                    30,
                                                                    null,
                                                                    null);

        n.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         100));
                                                                    
        n.init ();
        
        edBox.add (n);
        
        int prevCount = 0;
                                                                                                   
        List<EditorEditor> eds = EditorsEnvironment.getEditors (); 
        
        for (int i = 0; i < eds.size (); i++)
        {
                 
            EditorEditor ed = eds.get (i);
                 
            if (!ed.isPrevious ())
            {
                
                continue;
                    
            }
            
            prevCount++;
            
            EditorInfoBox infBox = this.getEditorBox (ed);
                
            if (i < eds.size () - 1)
            {
            
                infBox.setBorder (UIUtils.createBottomLineWithPadding (5, 0, 5, 0));

            } else {
                
                infBox.setBorder (UIUtils.createPadding (5, 0, 5, 0));
                                             
            }
    
            edBox.add (infBox);
            
            i++;
            
        }
        
        if (prevCount == 0)
        {
            
            return;
            
        }
        
        edBox.add (Box.createVerticalGlue ());
        
        AccordionItem prev = new AccordionItem (String.format ("Previous {editors} (%s)",
                                                               Environment.formatNumber (prevCount)),
                                                null,
                                                edBox);        
        
        prev.setBorder (UIUtils.createPadding (0, 0, 0, 0));
        prev.init ();        
                    
        Box wrapper = new ScrollableBox (BoxLayout.Y_AXIS);
        wrapper.setAlignmentX (Component.LEFT_ALIGNMENT);        
        wrapper.add (prev);            
                            
        JScrollPane sp = UIUtils.createScrollPane (wrapper);
        sp.setBorder (null);        
        
        this.tabs.add (sp,
                       1);
        
        JLabel l = new JLabel (Environment.getIcon (Constants.STOP_ICON_NAME,
                                                    Constants.ICON_EDITORS_LIST_TAB_HEADER));
        
        l.setToolTipText (Environment.replaceObjectNames ("Previous {editors}"));
        
        this.tabs.setTabComponentAt (1,
                                     l);
                
        this.addTabCloseListener (l,
                                  sp);
                
        this.tabs.setSelectedComponent (sp);
        this.tabs.revalidate ();
        this.tabs.repaint ();

        this.specialTabs.put ("previous-editors",
                              prev);
        
    }
  
    public void showProjectCommentsMessages ()
    {

        JComponent c = this.specialTabs.get ("project-comments");

        if (c != null)
        {
            
            this.tabs.remove (c);
                
        }
        
        final EditorsSideBar _this = this;
        
        Set<EditorMessage> messages = null;
        
        try
        {
            
            messages = EditorsEnvironment.getProjectMessages (this.projectViewer.getProject ().getId (),
                                                              ProjectCommentsMessage.MESSAGE_TYPE);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to get all project comments messages.",
                                  e);
                        
            return;
            
        }
                
        if ((messages == null)
            ||
            (messages.size () == 0)
           )
        {
            
            return;
                
        }

        this.showMessagesInSpecialTab (messages,
                                       Constants.COMMENT_ICON_NAME,
                                       "All {comments} for this {project}",
                                       "project-comments",
                                       "Below are all the {comments} you have received for this {project} from <b>all</b> your {editors}.",
                                       null);
                
    }
  
    private void updateProjectCommentsMessage ()
    {
        
        int count = 0;
        
        try
        {
            
            Set<EditorMessage> messages = EditorsEnvironment.getProjectMessages (this.projectViewer.getProject ().getId (),
                                                                                 ProjectCommentsMessage.MESSAGE_TYPE);

            for (EditorMessage m : messages)
            {
                
                ProjectCommentsMessage pcm = (ProjectCommentsMessage) m;
                
                count += pcm.getComments ().size ();
                
            }
                        
        } catch (Exception e) {
            
            Environment.logError ("Unable to get project comments for project: " +
                                  this.projectViewer.getProject ().getId (),
                                  e);
            
            this.projectCommentsMessage.setVisible (false);
            
            return;
            
        }
                
        String l = "";
        
        if (count == 1)
        {
            
            l = "You have <b>1</b> {comment} for this {project}.  Click to view it.";
                        
        } else {
            
            l = String.format ("You have <b>%s</b> {comments} for this {project}.  Click to view them.",
                               Environment.formatNumber (count));
            
        }
        
        this.projectCommentsMessage.setText (l);

        this.projectCommentsMessage.setVisible (count > 0);
        
    }
  
    private void showMessagesInSpecialTab (Set<EditorMessage> messages,
                                           String             iconName,
                                           String             toolTipText,
                                           String             tabName,
                                           String             desc,
                                           ActionListener     onDescClose)
    {

        final EditorsSideBar _this = this;
    
        // Split the messages into Editor->Messages
        Map<EditorEditor, Set<EditorMessage>> edmessages = new LinkedHashMap ();
        
        for (EditorMessage m : messages)
        {
        
            EditorEditor ed = m.getEditor ();
            
            Set<EditorMessage> edm = edmessages.get (ed);
            
            if (edm == null)
            {
                
                edm = new LinkedHashSet ();
                
                edmessages.put (ed,
                                edm);
                
            }
            
            edm.add (m);
            
        }
        
        final Box content = new ScrollableBox (BoxLayout.Y_AXIS);
        
        if (desc != null)
        {
            
            final Notification n = Notification.createHelpNotification (this.projectViewer,
                                                                        desc,
                                                                        30,
                                                                        null,
                                                                        onDescClose);

            n.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                             100));
                                                                              
            
            content.add (n);            

            n.init ();
            
        }
        
        for (EditorEditor ed : edmessages.keySet ())
        {
        
            EditorPanel ep = new EditorPanel (this,
                                              ed,
                                              edmessages.get (ed));
            ep.setShowChatBox (false);
            ep.init ();
                                
            ep.setOpaque (true);
            ep.setBackground (UIUtils.getComponentColor ());
            ep.setAlignmentX (Component.LEFT_ALIGNMENT);
            ep.setBorder (UIUtils.createPadding (5, 5, 5, 0));

            content.add (ep);
            
        }        
        
        JScrollPane sp = UIUtils.createScrollPane (content);
        sp.setBorder (null);                
        
        this.tabs.add (sp,
                       1);
        
        JLabel l = new JLabel (Environment.getIcon (iconName,
                                                    Constants.ICON_EDITORS_LIST_TAB_HEADER));
        
        l.setToolTipText (Environment.replaceObjectNames (toolTipText));
        
        this.tabs.setTabComponentAt (1,
                                     l);
        
        this.addTabCloseListener (l,
                                  sp);        
                
        this.tabs.setSelectedComponent (sp);
        this.tabs.revalidate ();
        this.tabs.repaint ();

        this.specialTabs.put (tabName,
                              content);
        
    }
  
    public void showUndealtWithMessages ()
    {
                
        JComponent c = this.specialTabs.get ("undealt");

        if (c != null)
        {
            
            this.tabs.remove (c);
                
        }
        
        Set<EditorMessage> messages = null;
        
        try
        {
            
            messages = EditorsEnvironment.getAllUndealtWithMessages ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to display undealt with messages",
                                  e);
            
            UIUtils.showErrorMessage (this.projectViewer,
                                      "Unable to display undealt with messages.");
            
            return;
            
        }

        if (messages.size () == 0)
        {
            
            return;
            
        }
        
        this.undealtWithMessages.setVisible (false);
        
        this.showMessagesInSpecialTab (messages,
                                       Constants.ERROR_ICON_NAME,
                                       "All messages requiring your attention",
                                       "undealt",
                                       "Below are messages that require your attention in some way",
                                       null);
    
    }
  
    private void updateUndealtWithMessageCount ()
    {
        
        int undealtWithCount = 0;
        
        try
        {
            
            undealtWithCount = EditorsEnvironment.getUndealtWithMessageCount ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to get undealt with message count",
                                  e);
            
            this.undealtWithMessages.setVisible (false);
            
            return;
            
        }

        if (undealtWithCount == 0)
        {
            
            this.undealtWithMessages.setVisible (false);
            
            return;
            
        }
        
        String l = "";
        
        if (undealtWithCount == 1)
        {
            
            l = "<b>1</b> message requires your attention.  Click to view it.";
                        
        } else {
            
            l = String.format ("<b>%s</b> messages require your attention.  Click to view them.",
                               Environment.formatNumber (undealtWithCount));
            
        }
        
        this.undealtWithMessages.setText (l);

        this.undealtWithMessages.setVisible (true);
        
    }
  
    public void toggleFindEditorsTab ()
    {
        
        if ((this.editorFindPanel != null)
            &&
            (this.editorFindPanel.getParent () != null)
           )
        {
            
            this.hideFindEditorsTab ();
            
            return;
            
        }
        
        this.showFindEditorsTab ();
        
    }
    
    public void hideFindEditorsTab ()
    {
        
        if (this.editorFindPanel == null)
        {
            
            return;
            
        }
        
        this.tabs.remove (this.editorFindPanel);
        
    }
    
    public void showFindEditorsTab ()
    {

        if ((this.editorFindPanel != null)
            &&
            (this.editorFindPanel.getParent () != null)
           )
        {
            
            this.tabs.setSelectedComponent (this.editorFindPanel);
            this.tabs.revalidate ();
            this.tabs.repaint ();
            
            return;
            
        }
        
        if (this.editorFindPanel == null)
        {
        
            // Add the editor find panel.
            this.editorFindPanel = new EditorFindPanel (this);

            this.editorFindPanel.init ();

            this.editorFindPanel.setOpaque (true);
            this.editorFindPanel.setBackground (UIUtils.getComponentColor ());
            this.editorFindPanel.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.editorFindPanel.setBorder (new EmptyBorder (5, 5, 5, 5));            
            
        } 
        
        JLabel add = new JLabel (Environment.getIcon (Constants.FIND_ICON_NAME,
                                                      Constants.ICON_TAB_HEADER));
        
        this.tabs.add (this.editorFindPanel,
                       1);
        this.tabs.setTabComponentAt (1,
                                     add);        
        
        this.showFindEditorsTab ();        
        
    }
        
    private void showInvitePopup ()
    {
                
        final EditorsSideBar _this = this;
                
        EditorsUIUtils.showInviteEditor (this.projectViewer);
                                
    }
    
    public EditorPanel getEditorPanel (EditorEditor ed)
    {
        
        for (int i = 0; i < this.tabs.getTabCount (); i++)
        {

            Component comp = this.tabs.getComponentAt (i);

            if (comp instanceof EditorPanel)
            {

                if (ed == ((EditorPanel) comp).getEditor ())
                {

                    return (EditorPanel) comp;
                
                }
                
            }
            
        }
        
        return null;
        
    }
    
    public void showChatBox (final EditorEditor ed)
                      throws GeneralException
    {
        
        this.showEditor (ed);
        
        final EditorsSideBar _this = this;
        
        SwingUtilities.invokeLater (new Runnable ()
        {
        
            public void run ()
            {
                
                EditorPanel edPanel = _this.getEditorPanel (ed);
            
                if (edPanel != null)
                {
                    
                    edPanel.showChatBox ();
                    
                }

            }
                
        });
        
    }
    
    public void showEditor (EditorEditor ed)
                     throws GeneralException
    {
        
        final EditorsSideBar _this = this;
        
        EditorPanel edPanel = this.getEditorPanel (ed);
        
        if (edPanel != null)
        {
     
            this.editorChanged (new EditorChangedEvent (ed,
                                                        EditorChangedEvent.EDITOR_CHANGED));        
        
            this.tabs.setSelectedComponent (edPanel);
            this.tabs.revalidate ();
            this.tabs.repaint ();
       
            return;
                                
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

        EditorPanel ep = new EditorPanel (this,
                                          ed);
        ep.init ();
                            
        ep.setOpaque (true);
        ep.setBackground (UIUtils.getComponentColor ());
        ep.setAlignmentX (Component.LEFT_ALIGNMENT);
        ep.setBorder (UIUtils.createPadding (5, 5, 5, 0));
                       
        int ind = this.tabs.getTabCount ();
                        
        this.tabs.add (ep);
                     
        final JLabel th = new JLabel ();
        th.setBorder (new CompoundBorder (UIUtils.createPadding (2, 0, 0, 0),
                                          UIUtils.createLineBorder ()));

        th.setMaximumSize (new Dimension (100, 100));
                             
        this.tabs.setTabComponentAt (ind,
                                     th);        
                         
        this.addTabHeaderMouseHandler (th,
                                       ep);

        this.showEditor (ed);
        
    }
    
    public void init ()
               throws GeneralException
    {

        super.init ();
        
    }    
    
    private void addTabHeaderMouseHandler (final JComponent tabHeader,
                                           final JComponent tab)
    {
        
        final EditorsSideBar _this = this;
        
        tabHeader.addMouseListener (new MouseEventHandler ()
        {
           
            @Override
            public void handlePress (MouseEvent ev)
            {
                
                _this.tabs.setSelectedComponent (tab);
                
            }
           
            @Override
            public void handleMiddlePress (MouseEvent ev)
            {
                
                _this.tabs.remove (tab);
                     
            }
            
            @Override
            public void fillPopup (JPopupMenu menu,
                                   MouseEvent ev)
            {
                
                menu.add (UIUtils.createMenuItem ("Close",
                                                  Constants.CLOSE_ICON_NAME,
                                                  new ActionListener ()
                                                  {
                                                       
                                                      public void actionPerformed (ActionEvent ev)
                                                      {
                                                           
                                                           _this.tabs.remove (tab);
                                                           
                                                      }
                                                       
                                                  }));
                
            }
            
        });
        
    }
    
    public String getStatusIconName (EditorEditor.OnlineStatus status)
    {
        
        String type = "offline";
        
        if (status != null)
        {
            
            type = status.getType ();
        
        }

        return Constants.ONLINE_STATUS_ICON_NAME_PREFIX + type;
        
    }
 
    public void fillInvitesEditorsList (Set<EditorEditor> eds)
    {
        
        this.invitesForMeWrapper.removeAll ();

        int c = 0;
                  
        for (EditorEditor ed : eds)
        {
            
            EditorInfoBox infBox = this.getEditorBox (ed);
                
            if (c < eds.size () - 1)
            {
            
                infBox.setBorder (UIUtils.createBottomLineWithPadding (5, 0, 5, 0));

            } else {
                
                infBox.setBorder (UIUtils.createPadding (5, 0, 5, 0));
                                             
            }
    
            this.invitesForMeWrapper.add (infBox);
            
            c++;
            
        }
        
    }
 
    private boolean isPendingInviteForMe (EditorEditor ed)
    {
        
        return ed.isPending () && !ed.isInvitedByMe ();
        
    }
 
    public void fillOtherEditorsList (Set<EditorEditor> eds)
    {
/*
        List<EditorEditor> eds = new ArrayList ();
    
        // Maybe use a filter here?
        for (EditorEditor ed : EditorsEnvironment.getEditors ())
        {
            
            if (ed.getEditorStatus () == EditorEditor.EditorStatus.rejected)
            {
                
                continue;
                
            }            
            
            if ((!this.projectViewer.getProject ().isProjectEditor (ed))
                &&
                (!this.isPendingInviteForMe (ed))
               )
            {
                
                eds.add (ed);
                
            }
            
            if (ed.isPrevious ())
            {
                
                System.out.println ("ED: " + ed);
                
            }
            
        }
        */
        this.otherEditorsWrapper.removeAll ();
        
        int c = 0;
        
        for (EditorEditor ed : eds)
        {
                    
            EditorInfoBox infBox = this.getEditorBox (ed);
                
            if (c < eds.size () - 1)
            {
            
                infBox.setBorder (UIUtils.createBottomLineWithPadding (5, 0, 5, 0));

            } else {
                
                infBox.setBorder (UIUtils.createPadding (5, 0, 5, 0));
                                             
            }
    
            this.otherEditorsWrapper.add (infBox);
            
            c++;
            
        }
                        
    }
        
    public void fillCurrentEditorsList (Set<ProjectEditor> currEds)
    {
                        
        this.currentEditorsWrapper.removeAll ();
        
        if (currEds != null)
        {
        
            List<ProjectEditor> eds = new ArrayList (currEds);
        
            int c = 0;
            
            for (ProjectEditor pe : currEds)
            {
        
                EditorInfoBox infBox = this.getEditorBox (pe.getEditor ());
                
                if (c < eds.size () - 1)
                {
                
                    infBox.setBorder (UIUtils.createBottomLineWithPadding (5, 0, 5, 0));
    
                } else {
                    
                    infBox.setBorder (UIUtils.createPadding (5, 0, 5, 0));
                                                 
                }
    
                this.currentEditorsWrapper.add (infBox);
    
                c++;
    
            }
            
        }
                        
    }

    /**
     * Always 300, 200.
     */
    @Override
    public Dimension getMinimumSize ()
    {
        
        return new Dimension (250,
                              200);        
    }

    public JComponent createEditorsFindList (List<EditorEditor> editors)
    {
        
        Box b = new Box (BoxLayout.Y_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);

        for (int i = 0; i < editors.size (); i++)
        {
            
            EditorEditor ed = editors.get (i);
            
            EditorFindInfoBox infBox = this.getEditorFindBox (ed);
            
            if (i < editors.size () - 1)
            {
            
                infBox.setBorder (new CompoundBorder (new MatteBorder (0,
                                                                       0,
                                                                       1,
                                                                       0,
                                                                       UIUtils.getBorderColor ()),
                                                      new EmptyBorder (5, 0, 5, 0)));

            } else {
                
                infBox.setBorder (new EmptyBorder (5, 0, 5, 0));
                                             
            }

            infBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                  infBox.getPreferredSize ().height + 10));
                          /*                  
            infBox.setMinimumSize (new Dimension (300,
                                                  infBox.getPreferredSize ().height));
            */
            b.add (infBox);

        }
        
        b.setBorder (new EmptyBorder (0, 10, 0, 5));
      
        return b;        
        
    }

    private EditorInfoBox getEditorBox (final EditorEditor ed)
    {
        
        EditorInfoBox b = new EditorInfoBox (ed,
                                             this.projectViewer);

        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        b.setToolTipText ("Click to send a message to " + ed.getMainName () + ", right click to see the menu");
        
        b.addFullPopupListener ();
        
        final EditorsSideBar _this = this;
        
        b.addMouseListener (new MouseEventHandler ()
        {
            
            @Override
            public void handlePress (MouseEvent ev)
            {
                
                if (ed.getEditorStatus () == EditorEditor.EditorStatus.pending)
                {
                    
                    return;
                    
                }
                
                // Show the editor.
                try
                {
                    
                    _this.showChatBox (ed);
                    
                } catch (Exception e) {
                    
                    UIUtils.showErrorMessage (_this.projectViewer,
                                              "Unable to show {editor}.");
                    
                    Environment.logError ("Unable to show editor: " +
                                          ed,
                                          e);
                    
                }
                
            }
            
        });
        
        if (ed.getEditorStatus () != EditorEditor.EditorStatus.pending)
        {
                    
            UIUtils.setAsButton (b);

        }

        return b;
        
    }

    private EditorFindInfoBox getEditorFindBox (final EditorEditor ed)
    {
        
        EditorFindInfoBox b = new EditorFindInfoBox (ed);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        final EditorsSideBar _this = this;
        
        b.addMouseListener (new MouseEventHandler ()
        {
            
            @Override
            public void handlePress (MouseEvent ev)
            {
                
                // Show the editor.
                try
                {
                    
                    _this.showChatBox (ed);
                    
                } catch (Exception e) {
                    
                    UIUtils.showErrorMessage (_this.projectViewer,
                                              "Unable to show {editor}.");
                    
                    Environment.logError ("Unable to show editor: " +
                                          ed,
                                          e);
                    
                }
                
            }
            
        });
        
        UIUtils.setAsButton (b);
        
        return b;
        
    }
    
}