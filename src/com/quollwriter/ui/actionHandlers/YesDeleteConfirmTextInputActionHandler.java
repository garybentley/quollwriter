package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;

public abstract class YesDeleteConfirmTextInputActionHandler<E extends AbstractViewer> extends TextInputActionHandler<E>
{

    private NamedObject object = null;

    public YesDeleteConfirmTextInputActionHandler (E           viewer,
                                                   NamedObject n)
    {

        super (viewer);
                
        this.object = n;

    }

    public abstract String getDeleteType ();
    
    public String getWarning ()
    {
        
        return null;
        
    }
    
    public String getIcon ()
    {
        
        return Constants.DELETE_ICON_NAME;
        
    }

    public String getTitle ()
    {
        
        return String.format (Environment.getUIString (LanguageStrings.deleteitem,
                                                       LanguageStrings.title),
                              this.getDeleteType ());
        
    }
    
    public String getHelp ()
    {
                
        String w = this.getWarning ();

        return String.format (Environment.getUIString (LanguageStrings.deleteitem,
                                                       LanguageStrings.text),
                              this.getDeleteType (),
                              this.object.getName (),
                              (w != null ? "<br /><br />" + w : ""));
                
    }
    
    public String getConfirmButtonLabel ()
    {
        
        return Environment.getUIString (LanguageStrings.deleteitem,
                                        LanguageStrings.confirm);
        
    }
    
    public String getInitialValue ()
    {
        
        return null;
        
    }
    
    public String isValid (String v)
    {

        String confirmWord = Environment.getUIString (LanguageStrings.deleteitem,
                                                      LanguageStrings.confirmword);
    
        if ((v == null)
            ||
            (!v.trim ().equalsIgnoreCase (confirmWord))
           )
        {
            
            return Environment.getUIString (LanguageStrings.deleteitem,
                                            LanguageStrings.errorlabel);
            
        }
                
        return null;
    
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
