package com.quollwriter.ui;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

import com.quollwriter.data.*;

public abstract class FindResultsBox<E extends AbstractProjectViewer> extends ProjectObjectsAccordionItem<E>
{

    protected int count = 0;

    private String forObjType = null;

    public FindResultsBox (String    title,
                           ImageIcon icon,
                           E         viewer)
    {

        super (title,
               icon,
               viewer);

        this.getTree ().getSelectionModel().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);

    }

    public FindResultsBox (String title,
                           String iconType,
                           String forObjType,
                           E      viewer)
    {

        super (title,
               iconType,
               viewer);

        this.forObjType = forObjType;

        this.getTree ().getSelectionModel().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);

    }

    @Override
    public String getId ()
    {

        return this.forObjType;

    }

    @Override
    public boolean showItemCountOnHeader ()
    {

        return true;

    }

    public void exapndAllResultsInTree ()
    {

        UIUtils.expandAllNodesWithChildren (this.tree);

    }

    public int getItemCount ()
    {

        return this.count;

    }

    @Override
    public void reloadTree ()
    {

    }

    public void fillTreePopupMenu (JPopupMenu m,
                                   MouseEvent ev)
    {

    }

    @Override
    public TreeCellEditor getTreeCellEditor (AbstractProjectViewer pv)
    {

        return null;

    }

    public int getViewObjectClickCount (Object d)
    {

        return 1;

    }

    @Override
    public boolean isAllowObjectPreview ()
    {

        return true;

    }

    public boolean isTreeEditable ()
    {

        return false;

    }

    public boolean isDragEnabled ()
    {

        return false;

    }

    @Override
    public DragActionHandler getTreeDragActionHandler (AbstractProjectViewer pv)
    {

        return null;

    }
/*
    public void clearResults ()
    {

        ((DefaultTreeModel) this.tree.getModel ()).setRoot (null);

        this.setVisible (false);

    }
*/
}
