package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ActionAdapter;


public class DeleteWarmupActionHandler extends YesDeleteConfirmTextInputActionHandler
{

    private Chapter               chapter = null;

    public DeleteWarmupActionHandler (Chapter               c,
                                      AbstractProjectViewer pv)
    {

        super (pv,
               c);
        this.chapter = c;

    }

    public String getDeleteType ()
    {
        
        return "{Warmup}";
        
    }
        
    public String getWarning ()
    {
        
        return "Warning!  Once deleted a {warmup} cannot be restored.";
        
    }
            
    public boolean onConfirm (String v)
                              throws Exception
    {

        this.projectViewer.deleteChapter (this.chapter);

        return true;
        
    }
    
}
