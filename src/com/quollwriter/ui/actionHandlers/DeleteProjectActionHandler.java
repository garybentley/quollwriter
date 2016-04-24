package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;

public class DeleteProjectActionHandler extends YesDeleteConfirmTextInputActionHandler<AbstractViewer>
{

    private ProjectInfo projInfo = null;
    private ActionListener onDelete = null;
    
    public DeleteProjectActionHandler (AbstractViewer viewer,
                                       Project        proj,
                                       ActionListener onDelete)
    {

        this (viewer,
              Environment.getProjectInfo (proj),
              onDelete);
               
    }

    public DeleteProjectActionHandler (AbstractViewer viewer,
                                       ProjectInfo    pi,
                                       ActionListener onDelete)
    {

        super (viewer,
               pi);

        this.projInfo = pi;
        this.onDelete = onDelete;
               
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

        Environment.deleteProject (this.projInfo,
                                   this.onDelete);

        return true;
        
    }
    
}
