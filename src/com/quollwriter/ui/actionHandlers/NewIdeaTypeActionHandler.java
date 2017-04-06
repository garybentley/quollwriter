package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.events.*;

public class NewIdeaTypeActionHandler extends TextInputActionHandler
{

    private IdeaBoard  ideaBoard = null;

    public NewIdeaTypeActionHandler (IdeaBoard ib)
    {

        super (ib.getViewer ());
    
        this.ideaBoard = ib;

    }

    public String getIcon ()
    {
        
        return Constants.ADD_ICON_NAME;
        
    }
    
    public String getTitle ()
    {
        
        return "Add New Idea Type";
        
    }
    
    public String getHelp ()
    {
        
        return "Enter the new Idea type below.";
        
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

        if (this.ideaBoard.hasTypeWithName (v))
        {

            return "A type called: " + v + " already exists.";

        }
        
        return null;
    
    }
    
    public boolean onConfirm (String v)
                              throws Exception
    {

        try
        {

            this.ideaBoard.addNewType (v,
                                       null,
                                       true);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to add new idea type with name: " +
                                        v,
                                        e);

        }
        
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
