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

public class WarmupsAccordionItem extends ProjectObjectsAccordionItem<WarmupsViewer>
{
        
    public WarmupsAccordionItem (WarmupsViewer pv)
    {
        
        super (Environment.getObjectTypeNamePlural (Warmup.OBJECT_TYPE),
               Warmup.OBJECT_TYPE,
               Warmup.OBJECT_TYPE,
               pv);
                        
    }
    
    @Override
    public void reloadTree ()
    {

        ((DefaultTreeModel) this.tree.getModel ()).setRoot (UIUtils.createChaptersTree (this.projectViewer.getProject (),
                                                                                        null,
                                                                                        null,
                                                                                        false));        
        
    }
    
    @Override
    public void initTree ()
    {

        ((DefaultTreeModel) this.tree.getModel ()).setRoot (UIUtils.createChaptersTree (this.projectViewer.getProject (),
                                                                                        null,
                                                                                        null,
                                                                                        false));        
    }

    @Override
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {

        m.add (UIUtils.createMenuItem ("Add New " + Environment.getObjectTypeName (Warmup.OBJECT_TYPE),
                                       "add",
                                       this.projectViewer.getAction (WarmupsViewer.NEW_WARMUP_ACTION,
                                                                     this.projectViewer.getProject ().getBooks ().get (0))));
    
    }    
    
    public boolean showItemCountOnHeader ()
    {
        
        return true;
        
    }
    
    public int getItemCount ()
    {
        
        int c = this.projectViewer.getProject ().getAllNamedChildObjects (Chapter.class).size ();
        
        return c;
                
    }

    @Override
    public void fillTreePopupMenu (JPopupMenu m,
                                   MouseEvent ev)
    {

        final WarmupsAccordionItem _this = this;

        final AbstractProjectViewer pv = this.projectViewer;
        
        final TreePath tp = this.tree.getPathForLocation (ev.getX (),
                                                          ev.getY ());

        JMenuItem mi = null;

        if (tp != null)
        {

            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            final DataObject d = (DataObject) node.getUserObject ();

            if (d instanceof Chapter)
            {

                final Chapter c = (Chapter) d;

                m.add (UIUtils.createMenuItem ("Edit {Warmup}",
                                               Constants.EDIT_ICON_NAME,
                                               pv.getAction (WarmupsViewer.EDIT_WARMUP_ACTION,
                                                             c)));

                m.add (UIUtils.createMenuItem ("Convert to a {Project}",
                                               Constants.CONVERT_ICON_NAME,
                                               pv.getAction (WarmupsViewer.CONVERT_TO_PROJECT_ACTION,
                                                             c)));

                m.add (UIUtils.createMenuItem ("Rename {Warmup}",
                                               Constants.RENAME_ICON_NAME,
                                               pv.getAction (WarmupsViewer.RENAME_WARMUP_ACTION,
                                                             c)));
                                                             
                m.add (UIUtils.createMenuItem ("Close {Warmup}",
                                               Constants.CANCEL_ICON_NAME,
                                               new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        pv.removePanel (c);

                    }

                }));

                m.add (UIUtils.createMenuItem ("Delete {Warmup}",
                                               Constants.DELETE_ICON_NAME,
                                               pv.getAction (WarmupsViewer.DELETE_WARMUP_ACTION,
                                                             c)));
                
            }

        } 
        
    }
    
    @Override
    public TreeCellEditor getTreeCellEditor (WarmupsViewer pv)
    {
        
        return new WarmupsProjectTreeCellEditor (pv,
                                                 tree);
                
    }
    
    public int getViewObjectClickCount (Object d)
    {
        
        return 1;
        
    }
    
    public boolean isTreeEditable ()
    {
        
        return true;
        
    }
    
    public boolean isDragEnabled ()
    {
        
        return true;
        
    }
    
    @Override
    public DragActionHandler getTreeDragActionHandler (WarmupsViewer pv)
    {
        
        return null;
/*
        return new ChapterTreeDragActionHandler ((ProjectViewer) pv,
                                                 tree);
  */      
    }
        
}