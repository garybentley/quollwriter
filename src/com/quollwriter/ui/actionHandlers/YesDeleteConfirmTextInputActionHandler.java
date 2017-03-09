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
        
        /*
        if (n == null)
        {
            
            throw new IllegalArgumentException ("Object cannot be null");
            
        }
        */
        
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
        
        return "Delete " + this.getDeleteType ();
        
    }
    
    public String getHelp ()
    {
        
        String h = "To delete " + this.getDeleteType () + " <b>" + this.object.getName () + "</b> please enter the word <b>Yes</b> into the box below.";
        
        String w = this.getWarning ();
        
        if (w != null)
        {
            
            h = h + "<br /><br />" + w;
        
        }
        
        return h;
        
    }
    
    public String getConfirmButtonLabel ()
    {
        
        return "Yes, delete it";
        
    }
    
    public String getInitialValue ()
    {
        
        return null;
        
    }
    
    public String isValid (String v)
    {

        if ((v == null)
            ||
            (!v.trim ().equalsIgnoreCase ("yes"))
           )
        {
            
            return "Please enter yes to confirm deletion.";
            
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
