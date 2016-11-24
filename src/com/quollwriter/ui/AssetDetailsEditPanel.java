package com.quollwriter.ui;

import java.awt.Color;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.*;

public abstract class AssetDetailsEditPanel<E extends Asset> extends DetailsEditPanel<ProjectViewer, E>
{

    public AssetDetailsEditPanel (E             a,
                                  ProjectViewer pv)
    {

        super (a,
               pv);

    }
    
    @Override
    public void doEdit ()
    {
        
        ActionListener a = AssetViewPanel.getEditAssetAction (this.viewer,
                                                              this.object);
        
        if (a == null)
        {
            
            Environment.logError ("Unable to get edit asset action for: " +
                                  this.object,
                                  null);
            
            UIUtils.showErrorMessage (this.viewer,
                                      String.format ("Unable to edit the {%s}",
                                                     Environment.getObjectTypeName (this.object)));
            
            return;
            
        }

        UIUtils.doLater (a);        
        
    }
    
    @Override
    public boolean canSave ()
    {

        return true;

    }

    @Override
    public List<FormItem> getExtraViewItems ()
    {

        List<FormItem> items = new ArrayList ();

        return items;

    }

}
