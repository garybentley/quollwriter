package com.quollwriter.exporter;

import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import com.quollwriter.data.*;


public class ExportUtils
{

    public static Project getSelectedItems (JTree   tree,
                                            Project proj)
    {

        Project p = new Project (proj.getName ());

        Book b = new Book (p,
                           null);

        p.addBook (b);
        b.setName (proj.getName ());

        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        Enumeration en = root.depthFirstEnumeration ();

        while (en.hasMoreElements ())
        {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement ();

            Object o = node.getUserObject ();

            if (o instanceof SelectableDataObject)
            {

                SelectableDataObject so = (SelectableDataObject) o;

                if (so.selected)
                {

                    if (so.obj instanceof Asset)
                    {

                        p.addAsset ((Asset) so.obj);

                    }

                    if (so.obj instanceof Chapter)
                    {

                        p.getBooks ().get (0).addChapter ((Chapter) so.obj);

                    }

                }

            }

        }

        return p;

    }

}
