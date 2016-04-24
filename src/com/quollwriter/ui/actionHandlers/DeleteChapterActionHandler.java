package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ActionAdapter;


public class DeleteChapterActionHandler extends YesDeleteConfirmTextInputActionHandler<AbstractProjectViewer>
{

    private Chapter               chapter = null;

    public DeleteChapterActionHandler(Chapter               c,
                                      AbstractProjectViewer pv)
    {

        super (pv,
               c);
        this.chapter = c;

    }

    public String getDeleteType ()
    {
        
        return "{Chapter}";
        
    }
        
    public String getWarning ()
    {
        
        return "Warning!  Once deleted a {chapter} cannot be restored.";
        
    }
            
    public boolean onConfirm (String v)
                              throws Exception
    {

        this.viewer.deleteChapter (this.chapter);

        return true;
        
    }
    
}
