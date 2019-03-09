package com.quollwriter.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.text.*;
import javax.swing.event.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.ActionAdapter;

public class ProjectFinder extends Finder<AbstractProjectViewer, FindResultsBox> implements TreeSelectionListener
{

    public ProjectFinder (AbstractProjectViewer v)
    {

        super (v);

    }

    @Override
    public Set<FindResultsBox> search (String text)
    {

        Set<FindResultsBox> results = this.viewer.findText (text);

        boolean expandSearchResults = UserProperties.getAsBoolean (Constants.SHOW_EACH_CHAPTER_FIND_RESULT_PROPERTY_NAME);

        for (FindResultsBox r : results)
        {

            r.getTree ().addTreeSelectionListener (this);

            r.init ();

            if (expandSearchResults)
            {

                r.exapndAllResultsInTree ();

            }

        }

        return results;

    }

    @Override
    public String getTitle ()
    {

        return Environment.getUIString (LanguageStrings.objectfinder,
                                        LanguageStrings.sidebar,
                                        LanguageStrings.title) + (this.getFindText () != null ? ": " + this.getFindText () : "");

    }

    @Override
    public void onClose ()
    {

        this.removeListeners ();

        super.onClose ();

    }

    private void removeListeners ()
    {

        // Remove the listeners.
        if (this.results != null)
        {

            for (FindResultsBox b : this.results)
            {

                b.getTree ().removeTreeSelectionListener (this);

            }

        }

    }

    @Override
    public void valueChanged (TreeSelectionEvent ev)
    {

        if (this.results == null)
        {

            return;

        }

        if (!ev.isAddedPath ())
        {

            return;

        }

        for (FindResultsBox b : this.results)
        {

            if (ev.getSource () == b.getTree ())
            {

                continue;

            }

            b.clearSelectedItemInTree ();

        }

    }
/*
    public void clearHighlight ()
    {

    }
*/
}
