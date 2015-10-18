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

public class EditProjectStatuses extends TypesEditor
{

    public EditProjectStatuses (AbstractViewer viewer)
    {

        super (viewer,
               Environment.getUserPropertyHandler (Constants.PROJECT_STATUSES_PROPERTY_NAME));

    }

    public void removePropertyChangedListener (PropertyChangedListener l)
    {
        
        ((UserPropertyHandler) this.getTypesHandler ()).removePropertyChangedListener (l);
        
    }
    
    public void addPropertyChangedListener (PropertyChangedListener l)
    {

        ((UserPropertyHandler) this.getTypesHandler ()).addPropertyChangedListener (l);
        
    }
    
    public String getWindowTitle ()
    {

        return "Manage the {Project} Statuses";

    }

    public String getHeaderTitle ()
    {

        return "Manage the {Project} Statuses";

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
        
        return null;
        
    }

    public String getTypesName ()
    {
        
        return "{Project} Statuses";
        
    }

}
