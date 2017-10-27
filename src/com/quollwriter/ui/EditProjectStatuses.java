package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;

import java.net.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.events.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.db.*;

public class EditProjectStatuses extends TypesEditor<AbstractViewer, UserPropertyHandler>
{

    private java.util.List<String> prefix = new ArrayList<> ();

    public EditProjectStatuses (AbstractViewer viewer)
    {

        super (viewer);

        this.prefix.add (LanguageStrings.project);
        this.prefix.add (LanguageStrings.status);
        this.prefix.add (LanguageStrings.actions);
        this.prefix.add (LanguageStrings.manage);

    }

    @Override
    public UserPropertyHandler getTypesHandler ()
    {

        return Environment.getUserPropertyHandler (Constants.PROJECT_STATUSES_PROPERTY_NAME);

    }

    public void removePropertyChangedListener (PropertyChangedListener l)
    {

        this.getTypesHandler ().removePropertyChangedListener (l);

    }

    public void addPropertyChangedListener (PropertyChangedListener l)
    {

        this.getTypesHandler ().addPropertyChangedListener (l);

    }

    @Override
    public String getNewItemsTitle ()
    {

        return Environment.getUIString (this.prefix,
                                        LanguageStrings._new,
                                        LanguageStrings.title);
        //return "New {Project} Statuses";

    }

    @Override
    public String getExistingItemsTitle ()
    {

        return Environment.getUIString (this.prefix,
                                        LanguageStrings.table,
                                        LanguageStrings.title);
        //return "Current {Project} Statuses";

    }

    @Override
    public String getNewItemsHelp ()
    {

        return Environment.getUIString (this.prefix,
                                        LanguageStrings._new,
                                        LanguageStrings.text);
        //return "Enter the new statuses to add below, separate each status with a comma or semi-colon.";

    }

}
