package com.quollwriter.ui;

import java.util.*;

import javax.swing.*;

import java.awt.event.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;


public class LocationDetailsEditPanel extends AssetDetailsEditPanel<Location>
{

    public LocationDetailsEditPanel (Location      l,
                                     ProjectViewer pv)
    {

        super (l,
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

    @Override
    public boolean hasChanges ()
    {

        return false;

    }

    public void fillForSave ()
    {

    }

    public void fillForEdit ()
    {

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
