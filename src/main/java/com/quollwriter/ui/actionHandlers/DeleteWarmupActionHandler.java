package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ActionAdapter;


public class DeleteWarmupActionHandler extends YesDeleteConfirmTextInputActionHandler<WarmupsViewer>
{

    private Chapter               chapter = null;

    public DeleteWarmupActionHandler (Chapter       c,
                                      WarmupsViewer pv)
    {

        super (pv,
               c);
        this.chapter = c;

    }

    public String getDeleteType ()
    {
        
        return Environment.getUIString (LanguageStrings.warmups,
                                        LanguageStrings.actions,
                                        LanguageStrings.deletewarmup,
                                        LanguageStrings.deletetype);
        //"{Warmup}";
        
    }
        
    public String getWarning ()
    {
        
        return Environment.getUIString (LanguageStrings.warmups,
                                        LanguageStrings.actions,
                                        LanguageStrings.deletewarmup,
                                        LanguageStrings.warning);
        //"Warning!  Once deleted a {warmup} cannot be restored.";
        
    }
            
    public boolean onConfirm (String v)
                              throws Exception
    {

        this.viewer.deleteChapter (this.chapter);

        return true;
        
    }
    
}
