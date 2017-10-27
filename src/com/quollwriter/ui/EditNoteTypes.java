package com.quollwriter.ui;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;


public class EditNoteTypes extends TypesEditor<AbstractViewer, UserPropertyHandler>
{

    public EditNoteTypes (AbstractViewer pv)
    {

        super (pv);

    }

    @Override
    public UserPropertyHandler getTypesHandler ()
    {

        return Environment.getUserPropertyHandler (Constants.NOTE_TYPES_PROPERTY_NAME);

    }

    @Override
    public String getNewItemsHelp ()
    {

        return "Enter the new types to add below, separate each type with a comma or semi-colon.";

    }

    @Override
    public String getExistingItemsHelp ()
    {

        return "Note: removing a type will only remove it from the list of types to select when adding/editing a note.  You can also change the type name by editing the values, double click on a type to edit it.";

    }

    @Override
    public String getNewItemsTitle ()
    {

        return "New Note Types";

    }

    @Override
    public String getExistingItemsTitle ()
    {

        return "Current Note Types";

    }

}
