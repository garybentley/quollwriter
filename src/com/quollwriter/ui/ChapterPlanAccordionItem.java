package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import java.awt.dnd.*;

import javax.swing.*;
import javax.swing.tree.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.renderers.*;

public class ChapterPlanAccordionItem extends ChapterFieldAccordionItem
{
                
    public ChapterPlanAccordionItem (ProjectViewer pv,
                                     Chapter       c)
    {
        
        super (pv,
               c);
        
    }

    public StringWithMarkup getFieldValue (Chapter c)
    {
        
        return c.getPlan ();
        
    }
    
    public String getFieldName ()
    {
        
        return "Plan";
        
    }

    public String getFieldNamePlural ()
    {
        
        return "Plan";
        
    }
    
    public void setFieldValue (StringWithMarkup v,
                               Chapter          c)
    {
        
        c.setPlan (v);
        
    }
    
    public String getFieldIconType ()
    {
        
        return "plan";
        
    }

    public boolean isBulleted ()
    {
        
        return true;
        
    }
        
}