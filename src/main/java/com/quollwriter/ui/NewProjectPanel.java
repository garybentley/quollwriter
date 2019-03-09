package com.quollwriter.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Component;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.components.*;


public class NewProjectPanel
{

    private JTextField     nameField = null;
    private JTextField     saveField = null;
    private JPasswordField passwordField2 = null;
    private JPasswordField passwordField = null;
    private JCheckBox      encryptField = null;
    private JLabel         error = null;
    private Project        project = null;

    public NewProjectPanel()
    {

        this.nameField = UIUtils.createTextField ();

    }

    public void setProject (Project p)
    {

        this.project = p;

    }

    public JTextField getNameField ()
    {

        return this.nameField;

    }

    public JComponent createPanel (final Container      parent,
                                   final ActionListener onCreate,
                                         boolean        createOnReturn,
                                   final ActionListener onCancel,
                                         boolean        addButtons)
    {

        final NewProjectPanel _this = this;

        String rows = "p, 6px, p, 6px, p, 6px, p";

        if (addButtons)
        {

            rows = rows + ", 6px, p";

        }

        int row = 1;

        final FormLayout fl = new FormLayout ("right:p, 6px, fill:200px:grow, 2px, p",
                                              rows);
        fl.setHonorsVisibility (true);
        final PanelBuilder builder = new PanelBuilder (fl);

        final CellConstraints cc = new CellConstraints ();

        builder.addLabel (Environment.getUIString (LanguageStrings.newprojectpanel,
                                                   LanguageStrings.labels,
                                                   LanguageStrings.name),
                          //"Name",
                          cc.xy (1,
                                 row));
        builder.add (this.nameField,
                     cc.xy (3,
                            row));

        row += 2;

        builder.addLabel (Environment.getUIString (LanguageStrings.newprojectpanel,
                                                   LanguageStrings.labels,
                                                   LanguageStrings.savein),
                          //"Save In",
                          cc.xy (1,
                                 row));

        File defDir = Environment.getDefaultSaveProjectDirPath ().toFile ();

        this.saveField = UIUtils.createTextField ();
        this.saveField.setText (defDir.getPath ());

        // this.nameField.addKeyListener (k);
        // this.saveField.addKeyListener (k);

        builder.add (this.saveField,
                     cc.xy (3,
                            row));

        JButton findBut = new JButton (Environment.getIcon ("find",
                                                            Constants.ICON_MENU));

        findBut.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    JFileChooser f = new JFileChooser ();
                    f.setDialogTitle (Environment.getUIString (LanguageStrings.newprojectpanel,
                                                               LanguageStrings.find,
                                                               LanguageStrings.title));
                                      //"Select a Directory");
                    f.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
                    f.setApproveButtonText (Environment.getUIString (LanguageStrings.newprojectpanel,
                                                                     LanguageStrings.find,
                                                                     LanguageStrings.confirm));
                                            //"Select");
                    f.setCurrentDirectory (new File (_this.saveField.getText ()));

                    // Need to run: attrib -r "%USERPROFILE%\My Documents" on XP to allow a new directory
                    // to be created in My Documents.

                    if (f.showOpenDialog (parent) == JFileChooser.APPROVE_OPTION)
                    {

                        _this.saveField.setText (f.getSelectedFile ().getPath ());

                    }

                }

            });

        builder.add (findBut,
                     cc.xy (5,
                            row));

        row += 2;

        this.encryptField = UIUtils.createCheckBox (Environment.getUIString (LanguageStrings.newprojectpanel,
                                                                             LanguageStrings.labels,
                                                                             LanguageStrings.encrypt));
                                                    //"Encrypt this {project}?  You will be prompted for a password.");
        this.encryptField.setBackground (Color.WHITE);

        builder.add (this.encryptField,
                     cc.xyw (3,
                             row,
                             2));

        FormLayout pfl = new FormLayout ("right:p, 6px, 100px, 20px, p, 6px, fill:100px",
                                         "p, 6px");
        pfl.setHonorsVisibility (true);
        PanelBuilder pbuilder = new PanelBuilder (pfl);

        this.passwordField = new JPasswordField ();

        pbuilder.addLabel (Environment.getUIString (LanguageStrings.newprojectpanel,
                                                    LanguageStrings.labels,
                                                    LanguageStrings.password),
                           //"Password",
                           cc.xy (1,
                                  1));

        pbuilder.add (this.passwordField,
                      cc.xy (3,
                             1));

        this.passwordField2 = new JPasswordField ();

        pbuilder.addLabel (Environment.getUIString (LanguageStrings.newprojectpanel,
                                                    LanguageStrings.labels,
                                                    LanguageStrings.confirmpassword),
                           //"Confirm",
                           cc.xy (5,
                                  1));

        pbuilder.add (this.passwordField2,
                      cc.xy (7,
                             1));

        row += 2;

        final JPanel ppanel = pbuilder.getPanel ();
        ppanel.setVisible (false);
        ppanel.setOpaque (false);

        builder.add (ppanel,
                     cc.xyw (3,
                             row,
                             2));

        this.encryptField.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                ppanel.setVisible (_this.encryptField.isSelected ());

                UIUtils.resizeParent (parent);

            }

        });

        ActionListener createProjectAction = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (!_this.createProject (parent))
                {

                    return;

                }

                if (onCreate != null)
                {

                    onCreate.actionPerformed (new ActionEvent (_this,
                                                               0,
                                                               _this.nameField.getText ()));

                }

                if (parent instanceof PopupWindow)
                {

                    ((PopupWindow) parent).close ();

                }

                if (parent instanceof QPopup)
                {

                    ((QPopup) parent).removeFromParent ();

                }

            }

        };

        if (addButtons)
        {

            row += 2;

            JButton createBut = new JButton ();
            createBut.setText (Environment.getUIString (LanguageStrings.newprojectpanel,
                                                        LanguageStrings.buttons,
                                                        LanguageStrings.create));
                               //"Create");

            createBut.addActionListener (createProjectAction);

            JButton cancelBut = new JButton ();
            cancelBut.setText (Environment.getUIString (LanguageStrings.newprojectpanel,
                                                        LanguageStrings.buttons,
                                                        LanguageStrings.cancel));
            //"Cancel");

            if (onCancel != null)
            {

                cancelBut.addActionListener (onCancel);

            }

            if (parent instanceof PopupWindow)
            {

                cancelBut.addActionListener (((PopupWindow) parent).getCloseAction ());

            }

            if (parent instanceof QPopup)
            {

                cancelBut.addActionListener (((QPopup) parent).getCloseAction ());

            }

            JButton[] buts = { createBut, cancelBut };

            JPanel bp = UIUtils.createButtonBar2 (buts,
                                                  Component.LEFT_ALIGNMENT); //ButtonBarFactory.buildLeftAlignedBar (buts);
            bp.setOpaque (false);
            bp.setAlignmentX (Component.LEFT_ALIGNMENT);

            builder.add (bp,
                         cc.xyw (3,
                                 row,
                                 3));

        }

        JPanel p = builder.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        if (createOnReturn)
        {

            UIUtils.addDoActionOnReturnPressed (this.nameField,
                                                createProjectAction);
            UIUtils.addDoActionOnReturnPressed (this.saveField,
                                                createProjectAction);
            UIUtils.addDoActionOnReturnPressed (this.passwordField,
                                                createProjectAction);
            UIUtils.addDoActionOnReturnPressed (this.passwordField2,
                                                createProjectAction);

        }

        this.error = UIUtils.createErrorLabel ("");
        this.error.setVisible (false);
        this.error.setBorder (new EmptyBorder (0,
                                               0,
                                               5,
                                               0));

        Box b = new Box (BoxLayout.Y_AXIS);

        b.add (this.error);
        b.add (p);

        return b;

    }

    public boolean createProject (Container parent)
    {

        if (!this.checkForm (parent))
        {

            return false;

        }

        Project proj = this.project;

        if (proj == null)
        {

            proj = new Project (this.getName ());

        } else {

            proj.setName (this.getName ());

        }

        AbstractProjectViewer pj = null;

        try
        {

            // TODO pj = Environment.getProjectViewerForType (proj);

        } catch (Exception e)
        {

            Environment.logError ("Unable to create new project: " +
                                  proj,
                                  e);

            UIUtils.showErrorMessage (parent,
                                      Environment.getUIString (LanguageStrings.newprojectpanel,
                                                               LanguageStrings.actionerror));
                                      //"Unable to create new project: " + proj.getName ());

            return false;

        }

        try
        {

            pj.newProject (this.getSaveDirectory (),
                           proj,
                           this.getPassword ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to create new project: " +
                                  proj,
                                  e);

            UIUtils.showErrorMessage (parent,
                                      Environment.getUIString (LanguageStrings.newprojectpanel,
                                                               LanguageStrings.actionerror));
                                      //"Unable to create new project: " + proj.getName ());

            return false;

        }

        return true;

    }

    private boolean showError (Container parent,
                               String    text)
    {

        this.error.setText (Environment.replaceObjectNames (text));

        this.error.setVisible (true);

        parent.repaint ();

        UIUtils.resizeParent (parent);

        return false;

    }

    private boolean hideError (Container parent)
    {

        this.error.setVisible (false);

        parent.repaint ();

        UIUtils.resizeParent (parent);

        return false;

    }

    public boolean checkForm (Container parent)
    {

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.newprojectpanel);
        prefix.add (LanguageStrings.errors);

        this.hideError (parent);

        String n = this.nameField.getText ().trim ();

        if (n.equals (""))
        {

            return this.showError (parent,
                                   Environment.getUIString (prefix,
                                                            LanguageStrings.novalue));
                                   //"Please provide a name for the {project}.");

        }

        // See if the project already exists.
        File pf = new File (saveField.getText (), Utils.sanitizeForFilename (n));

        if (pf.exists ())
        {

            return this.showError (parent,
                                   Environment.getUIString (prefix,
                                                            LanguageStrings.valueexists));

        }

        String pwd = null;

        if (this.encryptField.isSelected ())
        {

            // Make sure a password has been provided.
            pwd = new String (this.passwordField.getPassword ()).trim ();

            String pwd2 = new String (this.passwordField2.getPassword ()).trim ();

            if (pwd.equals (""))
            {

                return this.showError (parent,
                                       Environment.getUIString (prefix,
                                                                LanguageStrings.nopassword));
                                       //"Please provide a password for securing the {project}.");

            }

            if (pwd2.equals (""))
            {

                return this.showError (parent,
                                       Environment.getUIString (prefix,
                                                                LanguageStrings.confirmpassword));
                                       //"Please confirm your password.");

            }

            if (!pwd.equals (pwd2))
            {

                return this.showError (parent,
                                       Environment.getUIString (prefix,
                                                                LanguageStrings.nomatch));
                                       //"The passwords do not match.");

            }

        }

        return true;

    }

    public File getSaveDirectory ()
    {

        return new File (this.saveField.getText ());

    }

    public String getPassword ()
    {

        if (!this.encryptField.isSelected ())
        {

            return null;

        }

        String pwd = new String (this.passwordField.getPassword ());

        if (pwd.trim ().equals (""))
        {

            pwd = null;

        }

        return pwd;

    }

    public void setName (String n)
    {

        this.nameField.setText (n);

    }

    public String getName ()
    {

        return this.nameField.getText ();

    }

}
