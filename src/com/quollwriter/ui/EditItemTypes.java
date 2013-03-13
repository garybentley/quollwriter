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

import com.quollwriter.exporter.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;

public class EditItemTypes extends TypesEditor
{

    public EditItemTypes (ProjectViewer pv)
    {

        super (pv,
               pv.getItemTypeHandler ());

    }

    public String getWindowTitle ()
    {

        return Environment.replaceObjectNames ("Manage the {Object} Types");

    }

    public String getHeaderTitle ()
    {

        return Environment.replaceObjectNames ("Manage the {Object} Types");

    }

    public String getHeaderIconType ()
    {

        return Constants.EDIT_ICON_NAME;

    }

    public String getHelpText ()
    {

        return null;

    }

    public String getNewTypeHelp ()
    {
        
        return "Note: removing a type will only remove it from the list of types to select when adding/editing an {object}.  You can also change the type name by editing the values, double click on a type to begin.";
        
    }

    public String getTypesName ()
    {
        
        return "{Object} Types";
        
    }

}