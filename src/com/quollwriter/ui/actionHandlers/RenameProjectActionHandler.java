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

public class RenameProjectActionHandler extends TextInputActionHandler
{

    private Project               project = null;

    public RenameProjectActionHandler (AbstractProjectViewer pv)
    {

        super (pv);
        this.project = pv.getProject ();

    }

    public String getIcon ()
    {
        
        return Constants.EDIT_ICON_NAME;
        
    }
    
    public String getTitle ()
    {
        
        return "Rename {Project}";
        
    }
    
    public String getHelp ()
    {
        
        return "Enter the new {project} name below.  The {project} will then be closed and re-opened.";
        
    }
    
    public String getConfirmButtonLabel ()
    {
        
        return "Change";
        
    }
    
    public String getInitialValue ()
    {
        
        return this.projectViewer.getProject ().getName ();
        
    }
    
    public String isValid (String v)
    {

        if ((v == null)
            ||
            (v.trim ().length () == 0)
           )
        {
            
            return "Please enter a new name.";
            
        }

        v = v.trim ();
        
        if (!v.equalsIgnoreCase (this.projectViewer.getProject ().getName ()))
        {
                                                                        
            File newDir = new File (this.projectViewer.getProject ().getProjectDirectory ().getParentFile () + "/" + Utils.sanitizeForFilename (v));

            if (newDir.exists ())
            {

                return "A {project} with that name already exists.";
            
            }

        }
                
        return null;
    
    }
    
    public boolean onConfirm (String v)
                              throws Exception
    {
        
        final String newName = v;
        final String oldName = this.projectViewer.getProject ().getName ();
        
        if (!newName.equals (oldName))
        {
            
            final Project proj = this.projectViewer.getProject ();
            
            proj.setName (newName);

            // Save the project.
            try
            {

                this.projectViewer.saveProject ();

            } catch (Exception e)
            {

                Environment.logError ("Unable to save project",
                                      e);

                throw e;

            }

            // See how many books are in the project, if there is just one then change the name of it to be the same
            // as the project.
            if (proj.getBooks ().size () == 1)
            {

                Book b = proj.getBooks ().get (0);

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

                    throw e;
                                          
                }

            }

            final File newDir = new File (this.projectViewer.getProject ().getProjectDirectory ().getParentFile () + "/" + Utils.sanitizeForFilename (newName));                                                    

            // Close the viewer.
            this.projectViewer.close (true,
                                      new ActionListener ()
            {                                                    
            
                public void actionPerformed (ActionEvent ev)
                {
                    
                    // Rename the dir.
                    if (!proj.getProjectDirectory ().renameTo (newDir))
                    {
        
                        Environment.logError ("Unable to rename project directory: " +
                                              proj.getProjectDirectory () +
                                              " to: " +
                                              newDir);

                        UIUtils.showErrorMessage (null,
                                                  "Unable to rename project directory, please contact Quoll Writer support for assistance.");
        
                        return;
        
                    }
        /*
                    // Change the name in the projects file.
                    try
                    {
        
                        Environment.renameProject (oldName,
                                                   newName);
        
                    } catch (Exception e)
                    {
        
                        Environment.logError ("Unable to rename project (probably an error with the projects file): " +
                                              proj,
                                              e);

                        UIUtils.showErrorMessage (null,
                                                  "Unable to rename project, please contact Quoll Writer support for assistance.");
        
                        return;
                                                      
                    }
        */
                    proj.setProjectDirectory (newDir);
        
                    // Open the project again.
                    try
                    {
        
                        Environment.openProject (proj);
        
                    } catch (Exception e)
                    {
        
                        Environment.logError ("Unable to reopen project: " +
                                              proj,
                                              e);
        
                        UIUtils.showErrorMessage (null,
                                                  "Unable to reopen project, please contact Quoll Writer support for assistance.");

                        return;
                                              
                    }
        
                    Environment.getProjectViewer (proj).fireProjectEventLater (proj.getObjectType (),
                                                                               ProjectEvent.RENAME);

                }
                
            });
            
        }
        
        return true;
        
    }
    
    public boolean onCancel ()
                             throws Exception
    {
        
        return true;
        
    }
    
    public Point getShowAt ()
    {
        
        return null;
        
    }
    
}
