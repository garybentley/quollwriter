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

    @Override
    public StringWithMarkup getFieldValue (Chapter c)
    {

        return c.getPlan ();

    }

    @Override
    public String getFieldName ()
    {

        return this.chapter.getLegacyTypeField (Chapter.PLAN_LEGACY_FIELD_ID).getFormName ();
        //return "Plan";

    }

    @Override
    public void setFieldValue (StringWithMarkup v,
                               Chapter          c)
    {

        c.setPlan (v);

    }

    @Override
    public String getFieldIconType ()
    {

        return "plan";

    }

    @Override
    public boolean isBulleted ()
    {

        return true;

    }

}
