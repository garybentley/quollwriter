package com.quollwriter.editors.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import java.net.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.UIUtils;
import com.quollwriter.ui.PopupWindow;
import com.quollwriter.ui.AbstractProjectViewer;
import com.quollwriter.ui.ValueValidator;
import com.quollwriter.ui.TextInputWindow;

import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;

public class EditorLogin extends QPopup implements UserOnlineStatusListener
{

    private ActionListener onLogin = null;
    private ActionListener onCancel = null;
    private ActionListener onError = null;
    private ActionListener onClose = null;
    private JTextPane loginReason = null;
    private JLabel error = null;
    private JLabel logginIn = null;
    private JTextField emailField = null;
    private JPasswordField passwordField = null;
    private JCheckBox autoLogin = null;
    private JCheckBox savePwd = null;
    private boolean inited = false;
    
    public EditorLogin ()
    {

        super ("Login to the Editors service",
               Environment.getIcon ("editors",
                                    Constants.ICON_POPUP),
               null);
        
        final EditorLogin _this = this;
        
        JButton close = UIUtils.createButton (Constants.CLOSE_ICON_NAME,
                                              Constants.ICON_MENU,
                                              "Click to close",
                                              new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {
        
                _this.setVisible (false);
                
                if (_this.onClose != null)
                {
                    
                    _this.onClose.actionPerformed (ev);
                    
                }

                _this.removeFromParent ();
                
            }
            
        });

        List<JButton> buts = new ArrayList ();
        buts.add (close);
        
        this.getHeader ().setControls (UIUtils.createButtonBar (buts));

        this.loginReason = UIUtils.createHelpTextPane (null,
                                                       null);        
        
        this.error = UIUtils.createErrorLabel ("Please enter a value.");

        this.error.setVerticalAlignment (SwingConstants.TOP);
        this.error.setVerticalTextPosition (SwingConstants.TOP);
        this.error.setVisible (false);
        this.error.setBorder (UIUtils.createPadding (0, 5, 6, 0));

        this.logginIn = UIUtils.createLoadingLabel ("Logging in... please wait...");
        this.logginIn.setBorder (UIUtils.createPadding (0, 5, 6, 0));
        
        this.logginIn.setVisible (false);
        
        this.savePwd = UIUtils.createCheckBox ("Save password");                        
        
        EditorsEnvironment.addUserOnlineStatusListener (this);        
        
    }

    public void userOnlineStatusChanged (UserOnlineStatusEvent ev)
    {
        
        if (ev.getStatus () != EditorEditor.OnlineStatus.online)
        {
            
            return;
            
        }
        
        try
        {
        
            if (this.savePwd.isSelected ())
            {
                
                EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME,
                                                       new String (this.passwordField.getPassword ()));
                
            }

        } catch (Exception e) {
            
            Environment.logError ("Unable to save editors service password",
                                  e);
                
        }
        
    }
    
    public void setOnLogin (ActionListener onLogin)
    {
        
        this.onLogin = onLogin;
        
    }
    
    public void setOnCancel (ActionListener onCancel)
    {
        
        this.onCancel = onCancel;
        
    }

    public void setOnClose (ActionListener onClose)
    {
        
        this.onClose = onClose;
        
    }

    public void showError (String err)
    {
        
        this.error.setText (err);

        this.error.setVisible (true);
        
        this.logginIn.setVisible (false);
        
        this.resize ();
        
    }
    
    public void setLoginReason (String r)
    {
                
        this.loginReason.setText (r);
                
        this.loginReason.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                                 this.loginReason.getPreferredSize ().height));

    }
    
    public void init ()
    {
        
        final EditorLogin _this = this;
        
        Box b = new Box (BoxLayout.Y_AXIS);

        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.setOpaque (true);
        b.setBackground (null);
        b.setBorder (new EmptyBorder (5, 5, 10, 10));

        b.add (this.loginReason);
                
        b.add (this.error);

        b.add (logginIn);
            
        int row = 2;
                            
        final JButton loginBut = UIUtils.createButton ("Login",
                                                          null);                
                                            
        FormLayout fl = new FormLayout ("6px, right:p, 6px, fill:200px:grow",
                                        "10px, p, 6px, p, 6px, p, 6px, p, 6px, p");

        fl.setHonorsVisibility (true);
        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        builder.addLabel ("Your Email",
                          cc.xy (2,
                                 row));
        
        final EditorAccount acc = EditorsEnvironment.getUserAccount ();        
        
        String email = null;
                
        // Get the email address.
        if (acc != null)
        {
            
            email = acc.getEmail ();
            
            builder.addLabel (acc.getEmail (),
                         cc.xy (4,
                                row));

        } else {
            
            this.emailField = UIUtils.createTextField ();

            builder.add (this.emailField,
                         cc.xy (4,
                                row));
            
        }
        
        row += 2;
    
        builder.addLabel ("Password",
                          cc.xy (2,
                                 row));
        
        String pwd = EditorsEnvironment.getEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME);
        
        this.passwordField = new JPasswordField ();

        this.passwordField.setText (pwd);
        
        builder.add (this.passwordField,
                     cc.xy (4,
                            row));

        KeyListener loginFieldCheck = new KeyAdapter ()
        {
            
            public void keyReleased (KeyEvent ev)
            {
                
                loginBut.setEnabled (true);

                if (_this.emailField != null)
                {
                
                    String email = _this.emailField.getText ().trim ();
                    
                    int atInd = email.indexOf ('@');
                    
                    int dotInd = email.indexOf ('.',
                                                atInd);
                    
                    if ((email.length () == 0)
                        ||
                        (atInd == -1)
                        ||
                        (email.length () - 1 == dotInd)
                        ||
                        (dotInd == -1)
                       )
                    {
                        
                        loginBut.setEnabled (false);
                                
                    }

                }
                
                char[] pwd = passwordField.getPassword ();
                
                if ((pwd.length == 0)
                    ||
                    (pwd.length < 8)
                   )
                {
                    
                    loginBut.setEnabled (false);
                    
                }
                
            }
            
        };
        
        if (this.emailField != null)
        {
            
            this.emailField.addKeyListener (loginFieldCheck);
            
        }
        
        passwordField.addKeyListener (loginFieldCheck);
                    
        row += 2;
                 
        if (pwd != null)
        {
            
            this.savePwd.setSelected (true);
            
        }
        
        this.autoLogin = UIUtils.createCheckBox ("Automatically login whenever Quoll Writer starts");                

        this.autoLogin.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                try
                {
            
                    EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME,
                                                           _this.autoLogin.isSelected ());

                } catch (Exception e) {
                    
                    Environment.logError ("Unable to set to login at start",
                                          e);                    
                                                           
                }
                
            }
            
        });        
        
        //this.autoLogin.setSelected (EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME));
        
        this.savePwd.addActionListener (new ActionListener ()
        {
           
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                                
                if (_this.savePwd.isSelected ())
                {
                    
                    _this.savePwd.setSelected (false);
                                            
                    // Show the warning.
                    UIUtils.createTextInputPopup (Environment.getFocusedProjectViewer (),
                                                   "Warning!",
                                                   Constants.WARN_ICON_NAME,
                                                   "It is not recommended that your password is saved.<br /><br />This is because there is no way to securely store the password on your local computer, encrypting the password would have no effect/benefit, because of this your password will be saved in clear text in your editor properties file.<br /><br />If you understand the risks and are happy for the password to be stored in an insecure manner then please enter the word <b>Yes</b> in the box below.<br /><br />Note: if you select <b>No, don't save it</b> then you will have to enter the password each time you use login to the Editors service.<br /><br /><a href='help:editor-mode/saving-password'>Click here to find out more.</a>",
                                                   "Yes, save the password",
                                                   "No, don't save it",
                                                   null,
												   UIUtils.getYesValueValidator (),
                                                    new ActionAdapter ()
                                                    {
                                                      
                                                          public void actionPerformed (ActionEvent ev)
                                                          {                                                      
                                                              
                                                              _this.savePwd.setSelected (true);
                
                                                          }
                                                      
                                                    },
                                                    new ActionAdapter ()
                                                    {
                                                      
                                                          public void actionPerformed (ActionEvent ev)
                                                          {
                                                              
                                                              _this.savePwd.setSelected (false);
                                                              
                                                          }
                                                      
                                                    },
                                                    null).resize ();
                    
                } else {
                    
                    try
                    {
                    
                        EditorsEnvironment.removeEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME);
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to remove editors property: " +
                                              Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME,
                                              e);
                        
                    }

                }
                
            }
            
        });         
        
        ActionListener doLogin = new ActionAdapter ()
        {
          
            public void actionPerformed (ActionEvent ev)
            {
                
                if (!loginBut.isEnabled ())
                {
                    
                    return;
                    
                }
                
                try
                {
                
                    if (!_this.savePwd.isSelected ())
                    {
                        
                        EditorsEnvironment.removeEditorsProperty (Constants.QW_EDITORS_SERVICE_PASSWORD_PROPERTY_NAME);
                        
                    }
                    
                    BooleanProperty bp = new BooleanProperty (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME,
                                                              _this.autoLogin.isSelected ());
                    
                    EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME,
                                                           bp);

                } catch (Exception e) {
                    
                    Environment.logError ("Unable to update properties",
                                          e);
                                                
                }
                
                EditorsEnvironment.setLoginCredentials ((_this.emailField != null ? emailField.getText ().trim ().toLowerCase () : acc.getEmail ()),
                                                        new String (passwordField.getPassword ()));
                    
                _this.logginIn.setVisible (true);
                _this.error.setVisible (false);
                _this.resize ();
                                        
                if (_this.onLogin != null)
                {
                    
                    _this.onLogin.actionPerformed (new ActionEvent (_this, 1, "login"));
                                        
                }
                                
            }
            
        };

        if (this.emailField != null)
        {            
        
            UIUtils.addDoActionOnReturnPressed (this.emailField,
                                                doLogin);

        }
        
        UIUtils.addDoActionOnReturnPressed (passwordField,
                                            doLogin);
        
        loginBut.addActionListener (doLogin);
        
        builder.add (savePwd,
                     cc.xy (4,
                            row));
        
        row += 2;
                                                                      
        builder.add (autoLogin,
                     cc.xy (4,
                            row));
        
        row += 2;

        loginBut.setEnabled (pwd != null);
                
        JButton cancelBut = UIUtils.createButton ("Cancel",
                                          null);
        
        cancelBut.addActionListener (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.removeFromParent ();
                
                if (_this.onCancel != null)
                {
                    
                    _this.onCancel.actionPerformed (ev);
                    
                }
                
            }
            
        });        
            
        JButton[] buts = { loginBut, cancelBut };

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.LEFT_ALIGNMENT); 
        bp.setOpaque (false);
        bp.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        builder.add (bp,
                     cc.xyw (4,
                             row,
                             1));
        
        JPanel p = builder.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        b.add (p);
    
        _this.setContent (b);
                     
        b.setPreferredSize (new Dimension (UIUtils.DEFAULT_POPUP_WIDTH,
                                             b.getPreferredSize ().height));
        
        this.setContent (b);
        
        this.inited = true;
        
    }

    public void show (AbstractProjectViewer viewer)
    {
        
        if (!this.inited)
        {
            
            this.init ();
            
        }
        
        this.error.setVisible (false);
        
        this.logginIn.setVisible (false);        
        
        this.resize ();
        
        viewer.showPopupAt (this,
                            UIUtils.getCenterShowPosition (viewer,
                                                           this),
                            false);
    
        this.resize ();
    
        this.setDraggable (viewer);    
    
        if (this.emailField != null)
        {
    
            if (!emailField.getText ().equals (""))
            {
                
                this.passwordField.grabFocus ();
                
            } else {
        
                this.emailField.grabFocus ();
                
            }        

        } else {
          
            this.passwordField.grabFocus ();
            
        }
        
    }

    @Override
    public void setVisible (boolean v)
    {
        
        if (!this.inited)
        {
            
            return;
            
        }
        
        this.autoLogin.setSelected (EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME));
        
        super.setVisible (v);
        
    }
    
}
