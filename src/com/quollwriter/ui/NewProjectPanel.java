package com.quollwriter.ui;

import java.awt.Color;
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
    
    public NewProjectPanel()
    {
    
    }

    public JTextField getNameField ()
    {
        
        return this.nameField;
        
    }
        
    public JComponent createPanel (final Component      parent,
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

        this.nameField = UIUtils.createTextField ();

        builder.addLabel ("Name",
                          cc.xy (1,
                                 row));
        builder.add (this.nameField,
                     cc.xy (3,
                            row));

        row += 2;
                            
        builder.addLabel ("Save In",
                          cc.xy (1,
                                 row));

        String defDir = null;

        java.util.List pss = new ArrayList ();

        try
        {

            pss.addAll (Environment.getAllProjects ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to get all project stubs",
                                  e);

        }

        final java.util.List projs = pss;

        Collections.sort (projs,
                          new ProjectSorter ());

        if (projs.size () > 0)
        {

            Project p = (Project) projs.get (0);

            defDir = p.getProjectDirectory ().getParentFile ().getPath ();

        } else
        {

            File projsDir = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.DEFAULT_PROJECTS_DIR_NAME);

            projsDir.mkdirs ();

            defDir = projsDir.getPath ();

        }

        this.saveField = UIUtils.createTextField ();
        this.saveField.setText (defDir);

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
                    f.setDialogTitle ("Select a Directory");
                    f.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
                    f.setApproveButtonText ("Select");
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
                            
        this.encryptField = UIUtils.createCheckBox ("Encrypt this {project}?  You will be prompted for a password.");
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

        pbuilder.addLabel ("Password",
                           cc.xy (1,
                                  1));

        pbuilder.add (this.passwordField,
                      cc.xy (3,
                             1));

        this.passwordField2 = new JPasswordField ();

        pbuilder.addLabel ("Confirm",
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

                parent.repaint ();
                
                if (parent instanceof PopupWindow)
                {
                    
                    ((PopupWindow) parent).resize ();
                    
                }
                
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
                
            }
            
        };                    
            
        if (addButtons)
        {
            
            row += 2;
            
            JButton createBut = new JButton ();
            createBut.setText ("Create");

            createBut.addActionListener (createProjectAction);
                
            JButton cancelBut = new JButton ();
            cancelBut.setText ("Cancel");

            if (onCancel != null)
            {
            
                cancelBut.addActionListener (onCancel);
                
            }

            if (parent instanceof PopupWindow)
            {
                
                cancelBut.addActionListener (((PopupWindow) parent).getCloseAction ());
                
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

    public boolean createProject (Component parent)
    {
        
        if (!this.checkForm (parent))
        {
            
            return false;
            
        }

        Project proj = new Project (this.getName ());

        AbstractProjectViewer pj = null;

        try
        {

            pj = Environment.getProjectViewerForType (proj);

        } catch (Exception e)
        {

            Environment.logError ("Unable to create new project: " +
                                  proj,
                                  e);

            UIUtils.showErrorMessage (parent,
                                      "Unable to create new project: " + proj.getName ());

            return false;

        }

        try
        {

            pj.newProject (this.getSaveDirectory (),
                           proj.getName (),
                           this.getPassword ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to create new project: " +
                                  proj,
                                  e);

            UIUtils.showErrorMessage (parent,
                                      "Unable to create new project: " + proj.getName ());

            return false;

        }                
        
        return true;
        
    }
    
    private boolean showError (Component parent,
                               String    text)
    {
        
        this.error.setText (Environment.replaceObjectNames (text));
        
        this.error.setVisible (true);

        parent.repaint ();
        
        if (parent instanceof PopupWindow)
        {
            
            ((PopupWindow) parent).resize ();
            
        }
        
        return false;
        
    }
    
    public boolean checkForm (Component parent)
    {

        String n = this.nameField.getText ().trim ();

        if (n.equals (""))
        {

            return this.showError (parent,
                                   "Please provide a name for the {project}.");
                
        }

        // See if the project already exists.
        File pf = new File (saveField.getText () + "/" + Utils.sanitizeForFilename (n));

        if (pf.exists ())
        {

            return this.showError (parent,
                                   "A {project} called: " +
                                   n +
                                   " already exists.");

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
                                       "Please provide a password for securing the {project}.");

            }

            if (pwd2.equals (""))
            {

                return this.showError (parent,
                                       "Please confirm your password.");

            }

            if (!pwd.equals (pwd2))
            {

                return this.showError (parent,
                                       "The passwords do not match.");

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

        String pwd = new String (this.passwordField.getPassword ());

        if (pwd.trim ().equals (""))
        {

            pwd = null;

        }

        return pwd;

    }

    public String getName ()
    {

        return this.nameField.getText ();

    }
    
}
