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
import com.quollwriter.ui.components.Form;
import com.quollwriter.ui.components.FormAdapter;
import com.quollwriter.ui.components.FormEvent;
import com.quollwriter.ui.components.FormItem;


public class AddNewItemTypeActionHandler extends TextInputActionHandler
{

    public AddNewItemTypeActionHandler (ProjectViewer pv)
    {

        super (pv);

    }

    public String getIcon ()
    {
        
        return Constants.ADD_ICON_NAME;
        
    }
    
    public String getTitle ()
    {
        
        return "Add New {Object} Type";
        
    }
    
    public String getHelp ()
    {
        
        return "Add the name of the new {object} type below.";
        
    }
    
    public String getConfirmButtonLabel ()
    {
        
        return "Add";
        
    }
    
    public String getInitialValue ()
    {
        
        return null;
        
    }
    
    public String isValid (String v)
    {
        
        if ((v == null)
            ||
            (v.trim ().length () == 0)
           )
        {
            
            return "Please enter a name.";
            
        }
        
        return null;
    
    }
    
    public boolean onConfirm (String v)
                              throws Exception
    {
        
        this.projectViewer.getObjectTypesHandler (QObject.OBJECT_TYPE).addType (v,
                                                                                false);
        
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
