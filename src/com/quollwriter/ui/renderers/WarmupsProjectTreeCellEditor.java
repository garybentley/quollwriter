package com.quollwriter.ui.renderers;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;


public class WarmupsProjectTreeCellEditor extends DefaultTreeCellEditor implements CellEditorListener
{

    private WarmupsViewer projectViewer = null;
    private NamedObject   editing = null;
    private Chapter       lastChapter = null;

    public WarmupsProjectTreeCellEditor(WarmupsViewer pv,
                                        JTree         tree)
    {

        super (tree,
               new ProjectTreeCellRenderer (false));

        this.projectViewer = pv;

        this.addCellEditorListener (this);

    }

    public void setLastChapter (Chapter c)
    {

        this.lastChapter = c;

    }

    public void editingCanceled (ChangeEvent ev)
    {

        ((DefaultMutableTreeNode) this.lastPath.getLastPathComponent ()).setUserObject (this.editing);

        this.editing = null;

    }

    public void editingStopped (ChangeEvent ev)
    {

        String n = this.getCellEditorValue ().toString ().trim ();

        ((DefaultMutableTreeNode) this.lastPath.getLastPathComponent ()).setUserObject (this.editing);

        // See if there is another chapter with that name.
        if (n.equals (""))
        {

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Please select a name.");

            this.tree.startEditingAtPath (this.lastPath);

            return;

        }

        String oldName = this.editing.getName ();

        boolean result = false;

        if (this.editing instanceof Chapter)
        {

            result = this.handleChapter ((Chapter) this.editing,
                                         n);

        } else if (!(this.editing instanceof TreeParentNode))
        {

            try
            {

                this.editing.setName (n);

                this.projectViewer.saveObject (this.editing,
                                               true);

            } catch (Exception e)
            {

                Environment.logError ("Unable to change name of: " +
                                      this.editing +
                                      " to: " +
                                      n,
                                      e);

                UIUtils.showErrorMessage (this.projectViewer,
                                          "An internal error has occurred.\n\nUnable to change name.");

                result = false;

            }

        } else if (this.editing instanceof TreeParentNode)
        {

            result = this.handleTreeParentNode ((TreeParentNode) this.editing,
                                                n);

        } else
        {

            Environment.logError ("Type: " +
                                  this.editing +
                                  " not supported yet.");

            result = false;

        }

        if (!result)
        {

            this.editing.setName (oldName);

            this.tree.startEditingAtPath (this.lastPath);

            return;

        }

        this.editing = null;

    }

    private boolean handleTreeParentNode (TreeParentNode n,
                                          String         newName)
    {

        return true;

    }

    private boolean handleChapter (Chapter c,
                                   String  newName)
    {

        try
        {

            c.setName (newName);

            // Inform the chapter tree that something has changed.
            this.projectViewer.reloadWarmupsTree ();

            this.projectViewer.saveObject (c,
                                           true);

        } catch (Exception e)
        {

            Environment.logError ("Unable to change name of: " +
                                  c +
                                  " to: " +
                                  newName,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to change {chapter} name.");

            return false;

        }

        return true;

    }

    protected void prepareForEditing ()
    {

        if (this.editingComponent instanceof JTextField)
        {

            JTextField j = (JTextField) this.editingComponent;

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.lastPath.getLastPathComponent ();

            NamedObject n = (NamedObject) node.getUserObject ();

            this.editing = n;

            j.setText (n.getName ());

        }

        super.prepareForEditing ();

    }

    protected void startEditingTimer ()
    {

        // Don't do anything.

    }

    protected boolean shouldStartEditingTimer (EventObject ev)
    {

        return false;

    }

    protected boolean canEditImmediately (EventObject ev)
    {

        if (ev == null)
        {

            return true;

        }

        // No you can't.
        return false;

    }

}
;
