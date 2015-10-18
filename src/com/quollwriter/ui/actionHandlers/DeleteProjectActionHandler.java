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
        
                Environment.deleteProject (pr);
                        
            }
            
        });

        return true;
        
    }
    
}
