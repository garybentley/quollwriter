package com.quollwriter.editors.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;

import java.net.*;

import java.text.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.tree.*;

import org.bouncycastle.openpgp.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.editors.*;

public class EditorRegister extends Wizard 
{

    private JCheckBox tcAgreeField = null;
    private JLabel tcError = null;
    private JLabel loginError = null;
    private FileFinder finder = null;
    private JTextField emailField = null;
    private JPasswordField passwordField = null;
    private JPasswordField password2Field = null;
    private JTextField nameField = null;
    private ImageSelector avatar = null;
    private JLabel saving = null;
    private PGPKeyPair keyPair = null;
    private String messagingUsername = null;
    private String serviceName = null;
    private boolean createCalled = false;
    private boolean login = false;
    private boolean tcsClicked = false;

    public EditorRegister (AbstractViewer viewer)
                    throws Exception
    {

        super (viewer);
        
        final EditorRegister _this = this;
        
        Thread t = new Thread (new Runnable ()
        {
            
            public void run ()
            {
        
                // Create our public/private key.
                try
                {
                    
                    _this.keyPair = EditorsUtils.generateKeyPair ();
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to generate key pair",
                                          e);
                    
                }
                
            }
            
        });
        
        t.setPriority (Thread.MIN_PRIORITY);
        t.start ();

    }
/*
    public String getWindowTitle ()
    {

        return "Register for the Editor Service";

    }

    public String getHeaderTitle ()
    {

        return this.getWindowTitle ();

    }

    public String getHeaderIconType ()
    {

        return "editors";

    }
*/
    public String getFirstHelpText ()
    {

        return "Help!";

    }

    public boolean handleFinish ()
    {
        
        // Init the editors db.
        try
        {
            
            EditorsEnvironment.initDB (this.finder.getSelectedFile ());
            
        } catch (Exception e) {
            
            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to init editors database");
            
            Environment.logError ("Unable to init editors database",
                                  e);
                        
            // Clean up db files?
                        
            return true;
                        
        }
        
        // Save the directory away for the editors db. (user property)
        try
        {
            
            EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_DB_DIR_PROPERTY_NAME,
                                                   this.finder.getSelectedFile ().getPath ());

        } catch (Exception e) {
            
            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to save editors database location");
            
            Environment.logError ("Unable to save editors database location",
                                  e);
            
            return true;
                        
        }
        
        // Set the credentials.
        if (!this.login)
        {
            
            try
            {
    
                EditorsEnvironment.initUserCredentials (this.emailField.getText ().trim ().toLowerCase (),
                                                        null, //new String (this.passwordField.getPassword ()),
                                                        this.serviceName,
                                                        this.messagingUsername,                                                    
                                                        this.keyPair.getPublicKey (),
                                                        this.keyPair.getPrivateKey ());
                    
                // Save the name/picture away.
                String n = this.nameField.getText ().trim ();
                
                if (n.length () == 0)
                {
                    
                    n = null;
                    
                }
                
                EditorsEnvironment.setUserInformation (n,
                                                       this.avatar.getImage ());
    
            } catch (Exception e) {
                
                UIUtils.showErrorMessage (this.viewer,
                                          "Unable to save your details");
                
                Environment.logError ("Unable to save user information/credentials",
                                      e);
                
                return true;
                
            }
            
        } else {
            
            EditorsEnvironment.goOnline (null,
                                         null,
                                         null,
                                         null);
            
        }
        
        try
        {
        
            this.viewer.viewEditors ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to show editors",
                                  e);
            
        }
        
        return true;

    }

    public String getNextButtonLabel (String currStage)
    {
        
        if ((currStage != null)
            &&
            (currStage.equals ("existing"))
           )
        {
            
            return "Finish";
            
        }
        
        if ((currStage != null)
            &&
            (currStage.equals ("login-details"))
           )
        {
        
            return "Register";
        
        }
        
        return super.getNextButtonLabel (currStage);
        
    }
    
    public void handleCancel ()
    {

    }

    public String getNextStage (String currStage)
    {

        if (currStage == null)
        {

            return "start";

        }

        if (currStage.equals ("start"))
        {

            return "dir";

        }

        if (currStage.equals ("dir"))
        {

            return "about";

        }

        if (currStage.equals ("login-details"))
        {

            return "finish";

        }

        if (currStage.equals ("about"))
        {

            return "login-details";

        }

        if (currStage.equals ("finish"))
        {

        }

        return null;

    }

    public String getPreviousStage (String currStage)
    {

        if (currStage == null)
        {

            return null;

        }

        if (currStage.equals ("dir"))
        {

            return "start";

        }
        
        if (currStage.equals ("start"))
        {

            return null;

        }

        if (currStage.equals ("login-details"))
        {

            return "about";

        }

        if (currStage.equals ("about"))
        {

            return "dir";

        }

        if (currStage.equals ("finish"))
        {

            return null;

        }
        
        return null;

    }

    public boolean handleStageChange (String oldStage,
                                      String newStage)
    {
    
        if (oldStage == null)
        {

            return true;
                               
        }
        
        if ((newStage.equals ("finish"))
            &&
            (this.createCalled)
           )
        {
            
            return true;
            
        }
        
        if ((oldStage.equals ("start"))
            &&
            (newStage.equals ("existing"))
           )
        {
            
            return true;
            
        }
        
        if (oldStage.equals ("start"))
        {
            
            if (!this.tcAgreeField.isSelected ())
            {
                
                this.tcError.setVisible (true);
                
                this.resize ();
                
                return false;
                
            }
            
            if (!this.tcsClicked)
            {
                
                UIUtils.showMessage ((PopupsSupported) this.viewer,
                                     "A gentle nudge",
                                     String.format ("The Terms and Conditions only take a couple of minutes to read.  There is even a tl;dr of the impotant bits.<br /><br />It really is in your interests to give it a once over.  Spare a couple of minutes and save yourself some hassle later.%s",
                                                    "<br /><br />" + Environment.getQuollWriterHelpLink ("editor-mode/terms-and-conditions",
                                                                                                         "Click to view the Terms & Conditions")));
                                
            }
            
        }
        
        if (oldStage.equals ("dir"))
        {
            
            if (newStage.equals ("start"))
            {
                
                return true;
                        
            }
            
            if (Utils.getQuollWriterDirFile (this.finder.getSelectedFile ()).exists ())
            {
            
                return false;
            
            }
            
        }
                                
        if ((oldStage.equals ("login-details"))
            &&
            (newStage.equals ("finish"))
           )
        {

            final EditorRegister _this = this;
            
            String email = this.emailField.getText ().trim ();
            
            int atInd = email.indexOf ('@');
            
            int dotInd = email.indexOf ('.',
                                        atInd);
            
            if ((email.length () == 0)
                ||
                (atInd == -1)
                ||
                (dotInd == -1)
                ||
                (email.length () - 1 == dotInd)
               )
            {
    
                this.loginError.setText ("Please provide a valid email address.");                
                this.loginError.setVisible (true);
    
                this.resize ();
                
                return false;
                        
            }
            
            String pwd = new String (this.passwordField.getPassword ());
            String pwd2 = new String (this.password2Field.getPassword ());
    
            if (pwd.length () == 0)
            {
    
                this.loginError.setText ("Please provide a password.");                
                this.loginError.setVisible (true);
    
                this.resize ();
                
                return false;
                
            }
                
            if (pwd2.length () == 0)
            {
                
                this.loginError.setText ("Please confirm your password.");                
                this.loginError.setVisible (true);
    
                this.resize ();
                
                return false;
                
            }
            
            if (!pwd.equals (pwd2))
            {
    
                this.loginError.setText ("Your passwords do not match.");                
                this.loginError.setVisible (true);
    
                this.resize ();
                
                return false;
                
            }
                
            if (pwd.length () < 8)
            {
                
                this.loginError.setText ("Your password must be at least 8 characters long.");                
                this.loginError.setVisible (true);
    
                this.resize ();
                
                return false;
                
            }
        
            this.loginError.setVisible (false);
        
            // Create the account, show the saving.
            this.enableButton ("finish",
                               false);
            this.enableButton ("previous",
                               false);
            this.enableButton ("cancel",
                               false);
            
            this.saving.setVisible (true);
            
            this.emailField.setEnabled (false);
            this.passwordField.setEnabled (false);
            this.password2Field.setEnabled (false);
            
            this.resize ();            
        
            try
            {
        
                EditorsEnvironment.getEditorsWebServiceHandler ().createAccount (email,
                                                                                 pwd,
                                                                                 this.keyPair.getPublicKey (),
                                                                                 new EditorsWebServiceAction ()
                {
        
                    public void processResult (EditorsWebServiceResult res)
                    {

                        Map d = (Map) res.getReturnObject ();
                        
                        _this.messagingUsername = (String) d.get ("username");
                        _this.serviceName = (String) d.get ("servicename");
    
                        _this.createCalled = true;
    
                        _this.showStage ("finish");
                    
                    }
                    
                },
                new EditorsWebServiceAction ()
                {
        
                    public void processResult (EditorsWebServiceResult res)
                    {
                            
                        _this.enableButton ("finish",
                                            true);
                        _this.enableButton ("previous",
                                            true);
                        _this.enableButton ("cancel",
                                            true);
                        
                        _this.saving.setVisible (false);
                        
                        // Handle parameter errors, then other error types.
                        
                        _this.emailField.setEnabled (true);
                        _this.passwordField.setEnabled (true);
                        _this.password2Field.setEnabled (true);
                        _this.loginError.setText ("Unable to create account: " + res.getErrorMessage ());
                        _this.loginError.setVisible (true);
                        
                        _this.resize ();
                                                                                                                   
                    }
                                    
                });

            } catch (Exception e) {
                
                Environment.logError ("Unable to create account",
                                      e);
                
                UIUtils.showErrorMessage (this.viewer,
                                          "Unable to create account, please contact Quoll Writer support for assistance.");
                
            }
            
            return false;
            
        }
        
        return true;

    }

    public int getMaximumContentHeight ()
    {

        return 200;

    }

    public String getStartStage ()
    {

        return "start";

    }

    public int getContentPreferredHeight ()
    {
        
        return 200;
        
    }
    
    public WizardStep getStage (String stage)
    {

        final EditorRegister _this = this;

        WizardStep ws = new WizardStep ();

        if (stage.equals ("start"))
        {

            ws.title = "Getting started";

            ws.helpText = String.format ("Welcome to the Quoll Writer Editors Service.  The registration process only takes a minute and the service is free to use (no really).<br /><br />If you've already registered don't worry just use the link at the bottom to find your {editors} database.<br /><br />%s",
                                         Environment.getQuollWriterHelpLink ("editor-mode/overview",
                                                                             "Find out more about the editor service."));
            
            Box b = new Box (BoxLayout.Y_AXIS);
            
            JLabel tc = UIUtils.createClickableLabel ("View the Terms and Conditions for using the Editors service.",
                                                      Environment.getIcon (Constants.INFO_ICON_NAME,
                                                                           Constants.ICON_CLICKABLE_LABEL),
                                                      Environment.getQuollWriterHelpLink ("editor-mode/terms-and-conditions",
                                                                                             null));
            
            tc.addMouseListener (new MouseEventHandler ()
            {
               
                @Override
                public void handlePress (MouseEvent ev)
                {
                    
                    _this.tcsClicked = true;
                    
                }
                
            });
            
            tc.setBorder (UIUtils.createPadding (0, 0, 10, 5));
            
            b.add (tc);
            
            this.tcError = UIUtils.createErrorLabel ("You must agree to the Terms and Conditions to continue.");
            
            this.tcError.setVisible (false);
            this.tcError.setBorder (new EmptyBorder (0, 0, 5, 0));
            
            b.add (this.tcError);
            
            this.tcAgreeField = UIUtils.createCheckBox ("I have read and agree to the Terms and Conditions of use");                        

            this.tcAgreeField.addActionListener (new ActionListener ()
            {
               
                public void actionPerformed (ActionEvent ev)
                {
                    
                    if (_this.tcAgreeField.isSelected ())
                    {
                        
                        _this.tcError.setVisible (false);
                        _this.resize ();
                        
                    }
                    
                }
                
            });
            
            this.tcAgreeField.setMaximumSize (this.tcAgreeField.getPreferredSize ());
                        
            b.add (this.tcAgreeField);
            
            b.add (Box.createVerticalGlue ());
            
            JLabel reg = UIUtils.createClickableLabel ("Already registered?  Click here to find your {editors} database directory.",
                                                       Environment.getIcon (Constants.EDITORS_ICON_NAME,
                                                                            Constants.ICON_CLICKABLE_LABEL),
                                                       new ActionListener ()
                                                       {
                                                        
                                                            public void actionPerformed (ActionEvent ev)
                                                            {
                                                                
                                                                _this.showStage ("existing");
                                                                
                                                            }
                                                        
                                                       });
            
            reg.setBorder (UIUtils.createPadding (0, 5, 5, 5));
            
            b.add (reg);
            
            b.setBorder (UIUtils.createPadding (0, 5, 0, 5));
            
            ws.panel = b;
            
        }
            
        if (stage.equals ("existing"))
        {
            
            this.login = true;
            
            ws.title = "Find an existing {editors} database";
            
            ws.helpText = "Use the finder below to find your existing {editors} database.";
            
            final JLabel message = new JLabel ("");
            
            message.setVisible (false);
                        
            message.setBorder (UIUtils.createPadding (0, 0, 5, 0));
                        
            this.finder = UIUtils.createFileFind (Environment.getUserQuollWriterDir ().getPath (),
                                                  "Select a Directory",
                                                  JFileChooser.DIRECTORIES_ONLY,
                                                  "Select",
                                                  null);
            this.finder.setFindButtonToolTip ("Click to find a directory");
                        
            this.finder.setOnSelectHandler (new ActionListener ()
            {
                                                            
                public void actionPerformed (ActionEvent ev)
                {
                                
                    // See if it's an existing editors db, if so ask.
                
                    if (!EditorsEnvironment.isEditorsDBDir (_this.finder.getSelectedFile ()))
                    {
                        
                        // Show an error
                        message.setText ("<html>" + Environment.replaceObjectNames ("Sorry, that doesn't look like {an editors} directory.  There should be a file called: <b>" + Constants.EDITORS_DB_FILE_NAME_PREFIX + ".h2.db</b> in the directory.") + "</html>");
                        message.setForeground (UIUtils.getColor (Constants.ERROR_TEXT_COLOR));
                        message.setIcon (Environment.getIcon (Constants.ERROR_RED_ICON_NAME,
                                                              Constants.ICON_MENU));

                        _this.enableButton ("finish",
                                            false);
                                                        
                    } else {
                    
                        // See if the project is already in their project list.
                    
                        message.setText (Environment.replaceObjectNames ("That looks like {an editors} directory."));
                        message.setForeground (UIUtils.getColor ("#558631"));
                        message.setIcon (Environment.getIcon ("ok-green",
                                                              Constants.ICON_MENU));

                        // Set the seen sidebar property to ensure the welcome tab doesn't display.
                        try
                        {                                                              
                        
                            EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_EDITORS_SIDEBAR_SEEN_PROPERTY_NAME,
                                                                   true);                                                              

                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to set editors sidebar seen property",
                                                  e);
                                                              
                        }
                        
                        _this.enableButton ("finish",
                                            true);

                    }
                        
                    message.setVisible (true);
                                            
                    // Open the database and show the information?
                    
                    _this.resize ();
                                                                                                  
                }
                
            });    
                        
            Box b = new Box (BoxLayout.Y_AXIS);

            b.add (message);
            b.add (this.finder);
            b.setBorder (UIUtils.createPadding (0, 5, 0, 5));
            
            ws.panel = b;
            
        }
        
        if (stage.equals ("dir"))
        {

            ws.title = "Where to store editor information?";

            ws.helpText = "Information about your editors and chat messages is stored in a database on your local machine.  Please select the directory where the information should be stored.";
            
            final JLabel finderError = UIUtils.createErrorLabel ("Sorry, that looks like an existing Quoll Writer project directory.");
            
            finderError.setVisible (false);
            finderError.setBorder (new EmptyBorder (0, 0, 5, 0));
                        
            this.finder = UIUtils.createFileFind (Environment.getUserQuollWriterDir ().getPath () + "/editors",
                                                  "Select a Directory",
                                                  JFileChooser.DIRECTORIES_ONLY,
                                                  "Select",
                                                  null);
            this.finder.setFindButtonToolTip ("Click to find a directory");
                        
            this.finder.setOnSelectHandler (new ActionListener ()
            {
                                                            
                public void actionPerformed (ActionEvent ev)
                {
                                
                    finderError.setVisible (false);

                    // See if it's an existing editors db, if so ask.
                
                    if (Utils.getQuollWriterDirFile (_this.finder.getSelectedFile ()).exists ())
                    {
                        
                        finderError.setVisible (true);
                                            
                    }
                    
                    _this.resize ();
                                                                                                  
                }
                
            });    

            // Support encryption?
                        
            Box b = new Box (BoxLayout.Y_AXIS);

            b.add (finderError);
            b.add (this.finder);
            b.setBorder (UIUtils.createPadding (0, 5, 0, 5));
            
            ws.panel = b;

        }

        if (stage.equals ("finish"))
        {
            
            ws.title = "Account created, just one more step";
            
            ws.helpText = "Your account has been created.  But there is one final step to complete the process.  A confirmation email has been sent to <b>" + this.emailField.getText ().trim () + "</b>.  Please click on the link to complete the process.";
            
            ws.panel = new JLabel ("Check your email and have fun!");
            
        }
        
        if (stage.equals ("about"))
        {

            ws.title = "About you";

            ws.helpText = "Here you can provide some information about yourself.  This will be sent to editors to let them know about you.  The information is optional.";
            
            FormLayout fl = new FormLayout ("6px, right:p, 6px, fill:200px:grow",
                                            "p, 6px, top:p, 6px, p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();
            
            int row = 1;
            
            builder.addLabel ("Your Name",
                              cc.xy (2,
                                     row));

            this.nameField = UIUtils.createTextField ();
            this.nameField.setPreferredSize (new Dimension (200,
                                                            this.nameField.getPreferredSize ().height));
            
            builder.add (this.nameField,
                         cc.xy (4,
                                row));
    
            row += 2;

            java.util.List<String> fileTypes = new java.util.ArrayList ();
            fileTypes.add ("jpg");
            fileTypes.add ("jpeg");
            fileTypes.add ("png");
            fileTypes.add ("gif");
            
            builder.addLabel ("Your picture/Avatar",
                              cc.xy (2,
                                     row));
            
            Box b = new Box (BoxLayout.X_AXIS);
            
            java.awt.image.BufferedImage noImage = null;
            
            this.avatar = new ImageSelector (noImage,
                                             fileTypes,
                                             new Dimension (75, 75));
            this.avatar.setBorder (UIUtils.createLineBorder ());
            
            b.add (this.avatar);
            
            builder.add (b,
                         cc.xy (4,
                                row));
                        
            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            
            ws.panel = p;

        }

        if (stage.equals ("login-details"))
        {

            ws.title = "Your login details";

            ws.helpText = "And finally.  To access the Editors service you will need to login.  Please provide an email address/password below.  Note: it is recommended that you create a separate email account for use with this service.";

            this.saving= new JLabel (Environment.getLoadingIcon ());
            this.saving.setText ("Creating account, please wait...");
            this.saving.setBorder (new EmptyBorder (5, 10, 5, 5));
            
            this.saving.setVisible (false);
            
            this.loginError = UIUtils.createErrorLabel ("___bogus");
            this.loginError.setVisible (false);
            this.loginError.setBorder (new EmptyBorder (0, 0, 5, 0));
            
            int row = 1;
                                                                
            FormLayout fl = new FormLayout ("6px, right:p, 6px, fill:200px:grow",
                                            "p, 6px, p, 6px, p");
    
            fl.setHonorsVisibility (true);
            PanelBuilder builder = new PanelBuilder (fl);
    
            CellConstraints cc = new CellConstraints ();
                
            builder.addLabel ("Email",
                              cc.xy (2,
                                     row));
            
            this.emailField = UIUtils.createTextField ();
            
            builder.add (this.emailField,
                         cc.xy (4,
                                row));
    
            row += 2;
    
            builder.addLabel ("Password",
                              cc.xy (2,
                                     row));
            
            this.passwordField = new JPasswordField ();
            
            builder.add (this.passwordField,
                         cc.xy (4,
                                row));
    
            row += 2;
            
            builder.addLabel ("Confirm Password",
                              cc.xy (2,
                                     row));
            
            this.password2Field = new JPasswordField ();
            
            builder.add (this.password2Field,
                         cc.xy (4,
                                row));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            Box b = new Box (BoxLayout.Y_AXIS);

            b.add (this.saving);
            b.add (this.loginError);
            b.add (p);
            
            ws.panel = b;

        }

        return ws;

    }

    @Override
    protected void enableButtons ()
    {
        
        super.enableButtons ();
        
        if (this.getCurrentStage ().equals ("existing"))
        {
            
            this.enableButton ("finish",
                               false);
            
        }
        
    }
    
}


