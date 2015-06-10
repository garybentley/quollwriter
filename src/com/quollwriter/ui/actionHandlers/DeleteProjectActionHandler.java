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
        
                Environment.deleteProject (pr);
                        
            }
            
        });

        return true;
        
    }
    
}
