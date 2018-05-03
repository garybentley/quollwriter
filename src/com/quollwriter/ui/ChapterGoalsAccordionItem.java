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

public class ChapterGoalsAccordionItem extends ChapterFieldAccordionItem
{

    public ChapterGoalsAccordionItem (ProjectViewer pv,
                                      Chapter       c)
    {

        super (pv,
               c);

    }

    @Override
    public StringWithMarkup getFieldValue (Chapter c)
    {

        return c.getGoals ();

    }

    @Override
    public String getFieldName ()
    {

        // TODO: For now we get the ui string, but later we'll need to set it appropriately.
        return Environment.getUIString (LanguageStrings.chapters,
                                        LanguageStrings.fields,
                                        LanguageStrings.goals);

        //return this.chapter.getLegacyTypeField (Chapter.GOALS_LEGACY_FIELD_ID).getFormName ();
        //return "Goal";

    }

    @Override
    public void setFieldValue (StringWithMarkup v,
                               Chapter          c)
    {

        c.setGoals (v);

    }

    @Override
    public String getFieldIconType ()
    {

        return "goals";

    }

    @Override
    public boolean isBulleted ()
    {

        // TODO: Change to use field...
        return true;

    }

}
