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

public class EditorsSideBar extends AbstractSideBar<AbstractViewer> implements EditorChangedListener,
                                                                               EditorMessageListener,
                                                                               UserOnlineStatusListener
{
    
    public static final String NAME = "editors";
    
    private DnDTabbedPane tabs = null;
    private EditorFindPanel editorFindPanel = null;
    private JComponent noEditors = null;
    private JComponent firstLogin = null;
    private JButton statusButton = null;
        
    private JComponent notification = null;
    
    private EditorsSection otherEditors = null;
    private EditorsSection invitesForMe = null;
    private EditorsSection invitesIveSent = null;
        
    private Map<String, JComponent> specialTabs = new HashMap ();

    public EditorsSideBar (AbstractViewer v)
    {
        
        super (v);
                
        EditorsEnvironment.addEditorChangedListener (this);
        EditorsEnvironment.addEditorMessageListener (this);        
        EditorsEnvironment.addUserOnlineStatusListener (this);
        
    }                
             
    @Override
    public void handleMessage (EditorMessageEvent ev)
    {

        this.updateView ();
    
    }
                
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
        EditorsEnvironment.removeUserOnlineStatusListener (this);
        
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
        
        return "{Contacts}";  // {Editors}
        
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
                    
                    UIUtils.showErrorMessage (_this.viewer,
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
        String info = null;
        
        if (status == EditorEditor.OnlineStatus.offline)
        {
            
            toolTip = "Click to go online";
            
            info = "You have been logged out.";
            
        } else {
            
            toolTip = status.getName () + ", click to change your status";
            
            info = String.format ("Your status is now <b>%s</b>.",
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
                            
                            EditorsEnvironment.goOffline ();                            

                        }
                        
                    });

                    m.add (mi);
                    
                    m.show (_this.statusButton,
                            10,
                            10);
                    
                } else {
              
                    QPopup np = null;
              
                    if (EditorsEnvironment.hasLoginCredentials ())
                    {
                        
                        np = _this.showNotification (Constants.LOADING_GIF_NAME,
                                                     "Logging in...",
                                                     -1,
                                                     _this.statusButton);
                        
                    }
              
                    // TODO: Very nasty, fix.
                    final QPopup fnp = np;
              
                    EditorsEnvironment.goOnline (null,
                                                 new ActionListener ()
                                                 {
                                                  
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        if (fnp != null)
                                                        {
                                                            
                                                            fnp.removeFromParent ();
                                                            
                                                        }
                                                        
                                                        _this.updateView ();

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
                                                        
                                                        if (fnp != null)
                                                        {
                                                            
                                                            fnp.removeFromParent ();
                                                            
                                                        }                                                        
                                                        
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
                                          "Click to send an invite",
                                          new ActionAdapter ()
                                          {
                                            
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                                
                                                  EditorsUIUtils.showInviteEditor (_this.viewer);
                                                
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

                                                popup.add (UIUtils.createMenuItem (String.format ("View the previous {contacts} (%s)",
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
                                                                                    
                                                                                    EditorsUIUtils.updateYourInfo (_this.viewer);
                                                                                                                                                                        
                                                                                }
                                                                             
                                                                             }));
                                                     
                                          popup.add (UIUtils.createMenuItem ("Change your password",
                                                                             Constants.EDIT_ICON_NAME,
                                                                             new ActionListener ()
                                          {
                                            
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    EditorsUIUtils.showChangePassword (_this.viewer);
                                                    
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
                                                      
                                                        UIUtils.openURL (_this.viewer,
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
                                                        
                                                        UIUtils.showMessage ((PopupsSupported) _this.viewer,
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
                                                                                                                
                                                        UIUtils.openURL (_this.viewer,
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
                                                    
                                                    AbstractViewer viewer = Environment.getFocusedViewer ();
                                                    
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
                                                                     Environment.getQuollWriterHelpLink ("editor-mode/overview",
                                                                                                         null));
                                                    
                                                }
                                            
                                          }));

                                          popup.add (UIUtils.createMenuItem ("Delete your account",
                                                                             Constants.DELETE_ICON_NAME,
                                                                             new ActionListener ()
                                          {
                                            
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    EditorsUIUtils.showDeleteAccount (_this.viewer);
                                                                                                        
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
        
            this.viewer.showHelpText ("Welcome to the Editors Service",
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

        this.firstLogin = new Box (BoxLayout.Y_AXIS);

        this.firstLogin.add (UIUtils.createBoldSubHeader ("<i>Checked your email?</i>",
                                                         null));
        
        JComponent firstLoginHelp = UIUtils.createHelpTextPane ("Once you've validated your email address click on the button below to login.",
                                                                this.viewer);
        firstLoginHelp.setBorder (null);
        this.firstLogin.setBorder (new EmptyBorder (5, 5, 5, 5));
        
        Box bfirstLoginHelp = new Box (BoxLayout.Y_AXIS);
        bfirstLoginHelp.setAlignmentX (Component.LEFT_ALIGNMENT);
        bfirstLoginHelp.setBorder (UIUtils.createPadding (0, 5, 0, 5));
        bfirstLoginHelp.add (firstLoginHelp);
        
        this.firstLogin.add (bfirstLoginHelp);
        
        this.firstLogin.add (Box.createVerticalStrut (10));
                                
        JButton but = UIUtils.createToolBarButton (this.getStatusIconName (null),
                                                   "Click to go online",
                                                   null,
                                                   new ActionListener ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                EditorsEnvironment.goOnline (null,
                                             new ActionListener ()
                                             {
                                              
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    _this.updateView ();
                                                        
                                                }
                                              
                                             },
                                             // On cancel
                                             null,
                                             // On error
                                             null);
                
            }
            
        });

        but.setFont (but.getFont ().deriveFont (UIUtils.getScaledFontSize (12)));

        but.setText ("Click to Login");
        List<JButton> buts = new ArrayList ();
        buts.add (but);
        
        Box bbar = new Box (BoxLayout.X_AXIS);
        bbar.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        bbar.add (Box.createHorizontalGlue ());
        bbar.add (UIUtils.createButtonBar (buts));
        bbar.add (Box.createHorizontalGlue ());
        
        this.firstLogin.add (bbar);
        
        edBox.add (this.firstLogin);
                        
        this.noEditors = new Box (BoxLayout.Y_AXIS);        
        
        this.noEditors.add (UIUtils.createBoldSubHeader ("<i>No current {contacts}</i>",
                                                         null));
        
        JComponent noedsHelp = UIUtils.createHelpTextPane ("You currently have no {contacts}.  Click on the button below to invite someone to be {an editor} for your {project}.",
                                                           this.viewer);
        noedsHelp.setBorder (null);
        this.noEditors.setBorder (new EmptyBorder (5, 5, 5, 5));
        
        Box bnoedsHelp = new Box (BoxLayout.Y_AXIS);
        bnoedsHelp.setAlignmentX (Component.LEFT_ALIGNMENT);
        bnoedsHelp.setBorder (UIUtils.createPadding (0, 5, 0, 5));
        bnoedsHelp.add (noedsHelp);
        
        this.noEditors.add (bnoedsHelp);
        
        this.noEditors.add (Box.createVerticalStrut (10));
                                
        but = UIUtils.createToolBarButton (Constants.NEW_ICON_NAME,
                                           "Click to invite someone to be {an editor} for your {project}",
                                           null,
                                           new ActionListener ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                EditorsUIUtils.showInviteEditor (_this.viewer);                
                
            }
            
        });

        but.setFont (but.getFont ().deriveFont (UIUtils.getScaledFontSize (12)));

        but.setText ("Send an invite");
        buts = new ArrayList ();
        buts.add (but);
        
        bbar = new Box (BoxLayout.X_AXIS);
        bbar.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        bbar.add (Box.createHorizontalGlue ());
        bbar.add (UIUtils.createButtonBar (buts));
        bbar.add (Box.createHorizontalGlue ());
        
        this.noEditors.add (bbar);

        edBox.add (this.noEditors);
                                
        this.invitesForMe = new EditorsSection ("Invites from others",
                                                "Invites I've received from other people.",
                                                null,
                                                this.viewer);
                
        edBox.add (this.invitesForMe);
        
        this.invitesIveSent = new EditorsSection ("Pending invites",
                                                  "Invites I've sent to other people.",
                                                  null,
                                                  this.viewer);
    
        edBox.add (this.invitesIveSent);

        this.otherEditors = new EditorsSection ("All {Contacts}", //Editors
                                                null,
                                                null,
                                                this.viewer);
                
        edBox.add (this.otherEditors);

        final JScrollPane sp = UIUtils.createScrollPane (edBox);
        sp.setBorder (null);
                
        this.tabs.add (sp,
                       0);
        this.tabs.setIconAt (0,
                             Environment.getIcon ("editors-list",
                                                  Constants.ICON_EDITORS_LIST_TAB_HEADER));
        
        this.updateView ();
        
        UIUtils.doLater (new ActionListener ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                sp.getHorizontalScrollBar ().setValue (0);
                sp.getVerticalScrollBar ().setValue (0);
                
            }
            
        });
        
    }
        
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
/*
                ProjectEditor pe = this.projectViewer.getProject ().getProjectEditor (ed);
                
                if (pe != null)
                {
                    
                    projEds.add (pe);
                    
                } else {
                    
                    others.add (ed);
                    
                }
  */              
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
                                      "Unable to display others section, please contact Quoll Writer support for assistance.");            
            
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
                                      "Unable to display invites from others section, please contact Quoll Writer support for assistance.");            
            
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
                                      "Unable to display invites I've sent section, please contact Quoll Writer support for assistance.");            
            
        }
        
        this.noEditors.setVisible (edsSize == 0);        
        
        this.firstLogin.setVisible (false);

        if (EditorsEnvironment.getUserAccount ().getLastLogin () == null)
        {

            this.firstLogin.setVisible (true);
            this.noEditors.setVisible (false);
        
        }
        
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
    
    private QPopup showNotification (String iconType,
                                     String message,
                                     int    duration,
                                     Component showAt)
    {

        final EditorsSideBar _this = this;
    
        final QPopup p = new QPopup (message,
                                     Environment.getIcon (iconType,
                                                          Constants.ICON_EDITOR_MESSAGE),
                                     null);
        
        p.getHeader ().setFont (p.getHeader ().getFont ().deriveFont (UIUtils.getScaledFontSize (12)));

        p.getHeader ().setBorder (UIUtils.createPadding (10, 10, 10, 10));
        
        this.viewer.showPopupAt (p,
                                 showAt,
                                 true);
                        
        if (duration > 0)
        {
            
            javax.swing.Timer timer = new javax.swing.Timer (duration * 1000,
                                                             new ActionAdapter ()
                                                             {
                                                                             
                                                                public void actionPerformed (ActionEvent ev)
                                                                {
                                                                            
                                                                    p.removeFromParent ();
                                                                                 
                                                                }
                                                                             
                                                             });

            timer.setRepeats (false);
            timer.start ();            
                        
        }
        
        return p;
        
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
        
        return box; 
                    
    }

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

        JComponent nc = UIUtils.createInformationLabel ("{Contacts} you have removed or have removed you in the past.");
            
        nc.setBorder (UIUtils.createPadding (0, 0, 5, 0));
        
        edBox.add (nc);
        
        int prevCount = 0;
                                                                                                   
        List<EditorEditor> eds = EditorsEnvironment.getEditors (); 
        
        try
        {
        
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

        } catch (Exception e) {
            
            Environment.logError ("Unable to show editors: " +
                                  eds,
                                  e);
            
            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to build list of previous {contacts}, please contact Quoll Writer support for assistance.");
            
            return;
            
        }
        
        if (prevCount == 0)
        {
            
            return;
            
        }
        
        edBox.add (Box.createVerticalGlue ());
        
        AccordionItem prev = new AccordionItem (String.format ("Previous {contacts} (%s)",
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
        
        l.setToolTipText (Environment.replaceObjectNames ("Previous {contacts}"));
        
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
      
    private void showMessagesInSpecialTab (Set<EditorMessage> messages,
                                           String             iconName,
                                           String             toolTipText,
                                           String             tabName,
                                           String             desc,
                                           ActionListener     onDescClose)
                                    throws GeneralException
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
        
        if (desc != null)
        {
            
            JComponent nc = UIUtils.createInformationLabel (desc);
            
            nc.setBorder (UIUtils.createPadding (5, 5, 5, 5));
            
            content.add (nc);
            
        }
        
        for (EditorEditor ed : edmessages.keySet ())
        {
        
            Header h = UIUtils.createBoldSubHeader (String.format ("%s messages from",
                                                                   Environment.formatNumber (edmessages.get (ed).size ())),
                                                    null);
        
            h.setBorder (UIUtils.createPadding (0, 5, 0, 0));
            
            content.add (h);
        
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
                                
        this.tabs.setSelectedComponent (sp);
        this.tabs.revalidate ();
        this.tabs.repaint ();

        this.specialTabs.put (tabName,
                              sp);
        
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

    private boolean isPendingInviteForMe (EditorEditor ed)
    {
        
        return ed.isPending () && !ed.isInvitedByMe ();
        
    }

    /**
     * Always 250, 200.
     */
    @Override
    public Dimension getMinimumSize ()
    {
        
        return new Dimension (UIUtils.getScreenScaledWidth (250),
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
                                 throws GeneralException
    {
        
        EditorInfoBox b = new EditorInfoBox (ed,
                                             this.viewer,
                                             false);
                                             
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
                
        b.addFullPopupListener ();
        
        b.init ();
        
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
                    
                    UIUtils.showErrorMessage (_this.viewer,
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
 
    private class EditorsSection extends AccordionItem
    {

        private Box editorsListWrapper = null;
        private AbstractViewer viewer = null;
        private String title = null;
        private JLabel help = null;
        private JLabel noEditorsHelp = null;
        private ComponentListener listener = null;
    
        public EditorsSection (String         title,
                               String         help,
                               String         noEditorsHelp,
                               AbstractViewer viewer)
        {
            
            super ("",
                   null);
            
            this.title = title;
            this.viewer = viewer;
            
            final EditorsSection _this = this;
            
            this.listener = new ComponentAdapter ()
            {
                
                @Override
                public void componentResized (ComponentEvent ev)
                {
                    
                    int count = _this.editorsListWrapper.getComponentCount ();
                    
                    for (int i = 0; i < count; i++)
                    {
                        
                        EditorInfoBox infBox = (EditorInfoBox) _this.editorsListWrapper.getComponent (i);
                        
                        if (infBox == ev.getSource ())
                        {
                        
                            _this.setBorder (infBox,
                                             i == (count - 1));
                            
                        }
                        
                    }                    
                    
                }
                
            };
            
            Box content = new Box (BoxLayout.Y_AXIS);
            content.setAlignmentX (Component.LEFT_ALIGNMENT);
            content.setBorder (UIUtils.createPadding (0, 10, 0, 5));
        
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
        
            this.setContent (content);
            
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
        /*
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
        */
        public void update (Set<EditorEditor> eds)
                     throws GeneralException
        {
            
            this.setTitle (String.format ("%s (%s)",
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
        
        private void setBorder (EditorInfoBox b,
                                boolean       isLast)
        {

            b.setBorder (isLast ? UIUtils.createPadding (5, 0, 5, 0) : UIUtils.createBottomLineWithPadding (5, 0, 5, 0));
/*
            if (b.isShowAttentionBorder ())
            {
        
                b.setBorder (new CompoundBorder (new CompoundBorder (new MatteBorder (0, 2, 0, 0, UIUtils.getColor ("#ff0000")),
                                                                     UIUtils.createPadding (0, 5, 0, 0)),
                                                 isLast ? UIUtils.createPadding (5, 0, 5, 0) : UIUtils.createBottomLineWithPadding (5, 0, 5, 0)));
                
            } else {
                
                b.setBorder (isLast ? UIUtils.createPadding (5, 0, 5, 0) : UIUtils.createBottomLineWithPadding (5, 0, 5, 0));
                
            }
  */          
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
    
}