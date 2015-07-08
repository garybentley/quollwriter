package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import java.awt.dnd.*;

import javax.swing.*;
import javax.swing.tree.*;

import java.util.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.renderers.*;

public class NotesAccordionItem extends ProjectObjectsAccordionItem<AbstractProjectViewer>
{
        
    public NotesAccordionItem (AbstractProjectViewer pv)
    {
        
        super (Environment.getObjectTypeNamePlural (Note.OBJECT_TYPE),
               Note.OBJECT_TYPE,
               Note.OBJECT_TYPE,
               pv);
            
    }
    
    public void reloadTree (JTree tree)
    {
        
        java.util.List<TreePath> openPaths = new ArrayList ();
        
        Enumeration<TreePath> paths = tree.getExpandedDescendants (new TreePath (tree.getModel ().getRoot ()));

        if (paths != null)
        {

            while (paths.hasMoreElements ())
            {

                openPaths.add (paths.nextElement ());

            }

        }

        ((DefaultTreeModel) tree.getModel ()).setRoot (UIUtils.createNoteTree (this.projectViewer));

        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        for (TreePath p : openPaths)
        {

            tree.expandPath (UIUtils.getTreePathForUserObject (root,
                                                               ((DefaultMutableTreeNode) p.getLastPathComponent ()).getUserObject ()));

        }        
        
    }

    private void initTree ()
    {
        
        
        
    }
    
    @Override
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {
                
        m.add (UIUtils.createMenuItem ("Add New Type",
                                       Constants.ADD_ICON_NAME,
                                       this.projectViewer.getAction (ProjectViewer.NEW_NOTE_TYPE_ACTION)));

        m.add (UIUtils.createMenuItem ("Manage Types",
                                       Constants.EDIT_ICON_NAME,
                                       this.projectViewer.getAction (ProjectViewer.MANAGE_NOTE_TYPES_ACTION)));

    }    
    
    public void init (JTree tree)
    {

        ((DefaultTreeModel) tree.getModel ()).setRoot (UIUtils.createNoteTree (this.projectViewer));

    }

    public boolean showItemCountOnHeader ()
    {
        
        return true;
        
    }
    
    public int getItemCount ()
    {
        
        int c = this.projectViewer.getProject ().getAllNamedChildObjects (Note.class).size ();
        
        return c;
                
    }

    @Override
    public void fillTreePopupMenu (JPopupMenu m,
                                   MouseEvent ev)
    {

        final NotesAccordionItem _this = this;

        final TreePath tp = this.tree.getPathForLocation (ev.getX (),
                                                          ev.getY ());

        JMenuItem mi = null;

        if (tp != null)
        {

            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            final NamedObject d = (NamedObject) node.getUserObject ();

            if (d instanceof TreeParentNode)
            {

                if (!d.getName ().equals (Note.EDIT_NEEDED_NOTE_TYPE))
                {
    
                    m.add (UIUtils.createMenuItem ("Rename",
                                                   Constants.EDIT_ICON_NAME,
                                                   new ActionAdapter ()
                                                   {
                                
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                
                                                            _this.tree.setSelectionPath (tp);
                                                            _this.tree.startEditingAtPath (tp);
                                
                                                        }
                                
                                                   }));

                }

                if (node.getChildCount () == 0)
                {

                    m.add (UIUtils.createMenuItem ("Delete",
                                                   Constants.DELETE_ICON_NAME,
                                                   new ActionAdapter ()
                                                   {
                                
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                
                                                            DefaultTreeModel dtm = (DefaultTreeModel) _this.tree.getModel ();
                                
                                                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();
                                
                                                            NamedObject nt = (NamedObject) n.getUserObject ();
                                
                                                            _this.projectViewer.getObjectTypesHandler (Note.OBJECT_TYPE).removeType (nt.getName (),
                                                                                                                                     false);
                                
                                                            dtm.removeNodeFromParent (n);
                                
                                                        }
                                
                                                   }));

                }

            } else
            {

                m.add (UIUtils.createMenuItem ("View",
                                               Constants.VIEW_ICON_NAME,
                                               new ActionAdapter ()
                                               {
                                
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                
                                                        _this.projectViewer.viewObject (d);
                                
                                                    }
                                
                                               }));

                m.add (UIUtils.createMenuItem ("Edit",
                                               Constants.EDIT_ICON_NAME,
                                               _this.projectViewer.getAction (ProjectViewer.EDIT_NOTE_ACTION,
                                                                              d)));

                m.add (UIUtils.createMenuItem ("Delete",
                                               Constants.DELETE_ICON_NAME,
                                               _this.projectViewer.getAction (ProjectViewer.DELETE_NOTE_ACTION,
                                                                              d)));

            }

        } 
        
    }
    
    public TreeCellEditor getTreeCellEditor (AbstractProjectViewer pv,
                                             JTree                 tree)
    {
        
        return new ProjectTreeCellEditor (pv,
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
        
        return false;
        
    }
    
    public DragActionHandler getTreeDragActionHandler (AbstractProjectViewer pv,
                                                       JTree                 tree)
    {
        
        return null;
        
    }
        
}