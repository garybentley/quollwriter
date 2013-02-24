package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.Form;
import com.quollwriter.ui.components.FormAdapter;
import com.quollwriter.ui.components.FormEvent;
import com.quollwriter.ui.components.FormItem;


public class RenameProjectActionHandler extends FormAdapter
{

    private JTextField            nameField = UIUtils.createTextField ();
    private Form                  f = null;
    private Project               project = null;
    private AbstractProjectViewer projectViewer = null;

    public RenameProjectActionHandler(AbstractProjectViewer pv)
    {

        this.project = pv.getProject ();
        this.projectViewer = pv;

    }

    private void initForm ()
    {

        List items = new ArrayList ();
        items.add (new FormItem ("Name",
                                 this.nameField));

        this.f = new Form ("Rename Project",
                           Environment.getIcon (Project.OBJECT_TYPE,
                                                Constants.ICON_POPUP),
                           items,
                           this.projectViewer,
                           Form.SAVE_CANCEL_BUTTONS);

        f.addFormListener (this);

        final Form form = this.f;

        this.nameField.addKeyListener (new KeyAdapter ()
            {

                public void keyPressed (KeyEvent ev)
                {

                    if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                    {

                        // This is the same as save for the form.
                        form.fireFormEvent (FormEvent.SAVE,
                                            FormEvent.SAVE_ACTION_NAME);

                    }

                }

            });

    }

    public void actionPerformed (ActionEvent ev)
    {

        this.initForm ();

        this.f.setBounds (200,
                          200,
                          this.f.getPreferredSize ().width,
                          this.f.getPreferredSize ().height);

        this.projectViewer.addPopup (this.f);

        this.f.setVisible (true);

        this.nameField.setText (this.project.getName ());

        this.nameField.grabFocus ();

        this.nameField.selectAll ();

    }

    public void actionPerformed (FormEvent ev)
    {

        if (ev.getID () != FormEvent.SAVE)
        {

            return;

        }

        String newName = this.nameField.getText ().trim ();

        if (newName.equals (""))
        {

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Please select a name.");

            return;

        }

        // Check to see if we can move it, basically make sure that the directory doesn't already exist.
        if (!newName.equalsIgnoreCase (this.project.getName ()))
        {

            File newDir = new File (this.project.getProjectDirectory ().getParentFile () + "/" + Utils.sanitizeForFilename (newName));

            if (newDir.exists ())
            {

                UIUtils.showErrorMessage (this.projectViewer,
                                          "Unable to change name to: " +
                                          newName +
                                          " a directory with path:\n\n    " +
                                          newDir.getPath () +
                                          "\n\nalready exists.");

            }

            // Show a warning.
            if (JOptionPane.showConfirmDialog (this.projectViewer,
                                               "Warning!  To change the name of a project it must first be saved and closed.\n\nOnce the name has been changed the project will be reopened.\n\nDo you wish to continue?",
                                               UIUtils.getFrameTitle ("Confirm project name change?"),
                                               JOptionPane.YES_NO_OPTION,
                                               JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
            {

                this.f.hideForm ();

                return;

            }

            String oldName = this.project.getName ();

            this.project.setName (newName);

            // Save the project.
            try
            {

                this.projectViewer.saveProject ();

            } catch (Exception e)
            {

                Environment.logError ("Unable to save project",
                                      e);

                com.quollwriter.ui.UIUtils.showErrorMessage (this.projectViewer,
                                                             "Unable to save.");

                return;

            }

            // See how many books are in the project, if there is just one then change the name of it to be the same
            // as the project.
            if (this.project.getBooks ().size () == 1)
            {

                Book b = this.project.getBooks ().get (0);

                b.setName (newName);

                try
                {

                    this.projectViewer.saveObject (b,
                                                   true);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to save book: " +
                                          b,
                                          e);

                    com.quollwriter.ui.UIUtils.showErrorMessage (this.projectViewer,
                                                                 "Unable to save.");

                    return;

                }

            }

            // Close the viewer.
            this.projectViewer.close (true);

            // Rename the dir.
            if (!this.project.getProjectDirectory ().renameTo (newDir))
            {

                Environment.logError ("Unable to rename project directory: " +
                                      this.project.getProjectDirectory () +
                                      " to: " +
                                      newDir);

                this.f.hideForm ();

                return;

            }

            // Change the name in the projects file.
            try
            {

                Environment.renameProject (oldName,
                                           newName);

            } catch (Exception e)
            {

                Environment.logError ("Unable to rename project (probably an error with the projects file): " +
                                      this.project,
                                      e);

            }

            this.project.setProjectDirectory (newDir);

            // Open the project again.
            try
            {

                Environment.openProject (this.project);

            } catch (Exception e)
            {

                Environment.logError ("Unable to reopen project: " +
                                      this.project,
                                      e);

            }

            Environment.getProjectViewer (this.project).fireProjectEventLater (this.project.getObjectType (),
                                                                               ProjectEvent.RENAME);

            this.projectViewer = null;
            this.project = null;

        }

        this.f.hideForm ();

    }

}
