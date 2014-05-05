package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;

public class DeleteProjectActionHandler extends YesDeleteConfirmTextInputActionHandler
{

    public DeleteProjectActionHandler (AbstractProjectViewer pv)
    {

        super (pv,
               pv.getProject ());

    }

    public String getDeleteType ()
    {
        
        return "{Project}";
        
    }
        
    public String getWarning ()
    {
        
        return "Warning!  All information/chapters associated with the {project} will be deleted. Once deleted a {project} cannot be restored.";
        
    }
            
    public boolean onConfirm (String v)
                              throws Exception
    {

        // Need a ref for later.
        final Project pr = this.projectViewer.getProject ();

        this.projectViewer.close (true,
                                  new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {

                // There is probably now (because of h2) a "projectdb.lobs.db" directory.
                // Add a can delete file to it.
                try
                {
        
                    Utils.createQuollWriterDirFile (new File (pr.getProjectDirectory ().getPath () + "/projectdb.lobs.db"));
        
                } catch (Exception e)
                {
        
                    // Ignore for now.
                    Environment.logError ("Unable to add can delete dir file to: " +
                                          pr.getProjectDirectory ().getPath () + "/projectdb.lobs.db",
                                          e);
        
                }
        
                // Delete the directory.
                Utils.deleteDir (pr.getProjectDirectory ());
        
                // Remove the project from the list.
                try
                {
        
                    Environment.removeProject (pr);
        
                } catch (Exception e)
                {
        
                    Environment.logError ("Unable to remove project: " +
                                          pr +
                                          " from list of projects.",
                                          e);
        
                }
        
                // Show the welcome screen if there are no projects open.
                if (Environment.getOpenProjects ().size () == 0)
                {
        
                    FindOrOpen f = new FindOrOpen (FindOrOpen.SHOW_OPEN | FindOrOpen.SHOW_NEW);
        
                    f.pack ();
        
                    UIUtils.setCenterOfScreenLocation (f);
        
                    f.setVisible (true);
        
                }
                
            }
            
        });

        return true;
        
    }
    
}
