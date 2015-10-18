package com.quollwriter.ui;

import java.util.*;

import javax.swing.*;

import java.awt.event.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;


public class LocationDetailsEditPanel extends DetailsEditPanel
{

    public LocationDetailsEditPanel (Asset                 a,
                                     AbstractProjectViewer pv)
    {

        super (a,
               pv);

    }

    public Set<String> getObjectChangeEventTypes ()
    {
        
        return null;
        
    }    
    
    public String getEditHelpText ()
    {

        return null;

    }

    public void fillForSave ()
    {

    }

    public void fillForEdit ()
    {

    }

    public boolean canSave ()
    {

        return true;

    }

    public List<FormItem> getExtraEditItems (ActionListener onSave)
    {

        List<FormItem> items = new ArrayList ();

        return items;

    }

    public List<FormItem> getExtraViewItems ()
    {

        List<FormItem> items = new ArrayList ();

        return items;

    }

}
