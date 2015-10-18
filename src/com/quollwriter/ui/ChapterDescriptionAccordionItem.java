package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.dnd.*;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.renderers.*;

public class ChapterDescriptionAccordionItem extends ChapterFieldAccordionItem
{
                
    public ChapterDescriptionAccordionItem (ProjectViewer pv,
                                            Chapter       c)
    {
        
        super (pv,
               c);
                    
    }

    public boolean isBulleted ()
    {
        
        return false;
        
    }
    
    public StringWithMarkup getFieldValue (Chapter c)
    {
        
        return c.getDescription ();
        
    }
    
    public String getFieldName ()
    {
        
        return "Description";
        
    }

    public String getFieldNamePlural ()
    {
        
        return this.getFieldName ();
        
    }
    
    public void setFieldValue (StringWithMarkup v,
                               Chapter c)
    {
        
        c.setDescription (v);
        
    }
    
    public String getFieldIconType ()
    {
        
        return Constants.INFO_ICON_NAME;
        
    }
        
}