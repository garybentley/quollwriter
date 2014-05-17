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
import com.quollwriter.ui.components.Form;
import com.quollwriter.ui.components.FormAdapter;
import com.quollwriter.ui.components.FormEvent;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.events.*;


public class RenameAssetActionHandler extends TextInputActionHandler
{

    private Asset asset = null;

    public RenameAssetActionHandler (Asset                 a,
                                     AbstractProjectViewer pv)
    {

        super (pv);
        this.asset = a;

    }

    public String getIcon ()
    {
        
        return Constants.EDIT_ICON_NAME;
        
    }
    
    public String getTitle ()
    {
        
        return "Rename " + Environment.getObjectTypeName (this.asset);
        
    }
    
    public String getHelp ()
    {
        
        return String.format ("Enter the new %s name below.",
                              Environment.getObjectTypeName (this.asset).toLowerCase ());
        
    }

    public String getConfirmButtonLabel ()
    {
        
        return "Change";
        
    }
    
    public String getInitialValue ()
    {
        
        return this.asset.getName ();
        
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
        
        Set<? extends Asset> as = this.projectViewer.getProject ().getAllAssetsByName (v,
                                                                                       this.asset.getObjectType ());
        
        if (as.size () > 0)
        {
            
            for (Asset a : as)
            {
                
                if (a.getKey () != this.asset.getKey ())
                {
                    
                    exists = true;
                    
                }
                
            }
            
        }
        
        if (exists)
        {
                                                                        
            return String.format ("Another %s with that name already exists.",
                                  Environment.getObjectTypeName (this.asset.getObjectType ()));

        }

        return null;
    
    }
    
    public boolean onConfirm (String v)
                              throws Exception
    {
    
        try
        {

            this.asset.setName (v);

            this.projectViewer.saveObject (this.asset,
                                           true);

            // Inform the chapter tree that something has changed.
            this.projectViewer.handleItemChangedEvent (new ItemChangedEvent (this,
                                                                             this.asset,
                                                                             AbstractProjectViewer.NAME_CHANGED));
    
            this.projectViewer.fireProjectEventLater (this.asset.getObjectType (),
                                                      ProjectEvent.RENAME);

            return true;
                                                      
        } catch (Exception e)
        {

            Environment.logError ("Unable to change name of asset: " +
                                  this.asset +
                                  " to: " +
                                  v,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      String.format ("An internal error has occurred.\n\nUnable to change name of %s.",
                                                     Environment.getObjectTypeName (this.asset)));

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
