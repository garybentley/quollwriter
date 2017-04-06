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
        
        return "Enter the new {object} type below.";
        
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
                
        if (Environment.getUserPropertyHandler (Constants.OBJECT_TYPES_PROPERTY_NAME).hasType (v))
        {

            return "A type called: " + v + " already exists.";

        }
        
        return null;
    
    }
    
    public boolean onConfirm (String v)
                              throws Exception
    {
        
        Environment.getUserPropertyHandler (Constants.OBJECT_TYPES_PROPERTY_NAME).addType (v,
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
