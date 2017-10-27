package com.quollwriter.ui.renderers;

import java.awt.*;

import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.text.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;

import com.quollwriter.ui.components.ImagePanel;


public class ChapterSnippetsTreeCellRenderer extends DefaultTreeCellRenderer
{

    private Map icons = new HashMap ();

    public ChapterSnippetsTreeCellRenderer()
    {

    }

    public Component getTreeCellRendererComponent (JTree   tree,
                                                   Object  value,
                                                   boolean sel,
                                                   boolean expanded,
                                                   boolean leaf,
                                                   int     row,
                                                   boolean hasFocus)
    {

        super.getTreeCellRendererComponent (tree,
                                            value,
                                            sel,
                                            expanded,
                                            leaf,
                                            row,
                                            hasFocus);

        this.setBorder (new EmptyBorder (2, 2, 2, 2));
        this.setIcon (null);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        value = node.getUserObject ();

        if (value instanceof Chapter)
        {

            Chapter c = (Chapter) value;

            this.setText (c.getName () + " (" + node.getChildCount () + ")");

        } else {

            this.setIcon (Environment.getIcon (Chapter.OBJECT_TYPE,
                                               Constants.ICON_TREE));

        }

        return this;

    }

}
