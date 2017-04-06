package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.events.*;

public class RenameChapterActionHandler extends TextInputActionHandler<AbstractProjectViewer>
{

    private Chapter               chapter = null;

    public RenameChapterActionHandler (Chapter               c,
                                       AbstractProjectViewer pv)
    {

        super (pv);
        this.chapter = c;

    }

    public String getIcon ()
    {
        
        return Constants.EDIT_ICON_NAME;
        
    }
    
    public String getTitle ()
    {
        
        return "Rename {Chapter}";
        
    }
    
    public String getHelp ()
    {
        
        return "Enter the new {chapter} name below.";
        
    }

    public String getConfirmButtonLabel ()
    {
        
        return "Change";
        
    }
    
    public String getInitialValue ()
    {
        
        return this.chapter.getName ();
        
    }
    
    public String isValid (String v)
    {

        if ((v == null)
            ||
            (v.trim ().length () == 0)
           )
        {
            
            return "Please enter a new name.";
            
        }

        v = v.trim ();
        
        boolean exists = false;
        
        Set<Chapter> cs = this.chapter.getBook ().getAllChaptersWithName (v);
        
        if (cs.size () > 0)
        {
            
            for (Chapter c : cs)
            {
                
                if (c.getKey () != this.chapter.getKey ())
                {
                    
                    exists = true;
                    
                }
                
            }
            
        }
        
        if (exists)
        {
                                                                        
            return "Another {chapter} with that name already exists.";

        }
                
        return null;
    
    }
    
    public boolean onConfirm (String v)
                              throws Exception
    {
    
        try
        {

            this.chapter.setName (v);

            this.viewer.saveObject (this.chapter,
                                    true);

            // Inform the chapter tree that something has changed.
            this.viewer.handleItemChangedEvent (new ItemChangedEvent (this,
                                                                      this.chapter,
                                                                      AbstractProjectViewer.NAME_CHANGED));
    
            this.viewer.fireProjectEventLater (this.chapter.getObjectType (),
                                               ProjectEvent.RENAME);

            return true;
                                                      
        } catch (Exception e)
        {

            Environment.logError ("Unable to change name of chapter: " +
                                  this.chapter +
                                  " to: " +
                                  v,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      "An internal error has occurred.\n\nUnable to change name of {chapter}.");

        }
        
        return false;
    
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
