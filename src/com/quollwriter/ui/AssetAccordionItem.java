package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

import java.util.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.renderers.*;

public class AssetAccordionItem extends ProjectObjectsAccordionItem<ProjectViewer>
{
                
    public AssetAccordionItem (String        objType,
                               ProjectViewer pv)
    {
        
        super (Environment.getObjectTypeNamePlural (objType),
               objType,
               objType,
               pv);
                                                
    }
        
    public void init (final JTree tree)
    {

        ((DefaultTreeModel) tree.getModel ()).setRoot (UIUtils.createAssetTree (this.forObjType,
                                                                                this.projectViewer.getProject ()));
        
    }

    @Override
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {
    
        final AssetAccordionItem _this = this;

        ActionListener addNewItem = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                Asset a = null;

                try
                {

                    a = Asset.createSubType (_this.forObjType);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to create asset sub type for: " +
                                          _this.forObjType,
                                          e);

                    return;

                }

                AssetActionHandler aah = new AssetActionHandler (a,
                                                                 (ProjectViewer) _this.projectViewer,
                                                                 AbstractActionHandler.ADD);

                aah.setShowPopupAt (_this.getHeader (),
                                         "below");

                aah.actionPerformed (ev);

            }

        };
    
        m.add (UIUtils.createMenuItem ("Add New " + Environment.getObjectTypeName (this.forObjType),
                                       Constants.ADD_ICON_NAME,
                                       addNewItem));

        if (this.forObjType.equals (QObject.OBJECT_TYPE))
        {
                    
            // Add New Type
            m.add (UIUtils.createMenuItem (Environment.replaceObjectNames ("New {Object} Type"),
                                           Constants.ADD_ICON_NAME,
                                           this.projectViewer.getAction (ProjectViewer.NEW_ITEM_TYPE_ACTION)));
    
            m.add (UIUtils.createMenuItem (Environment.replaceObjectNames ("Manage {Object} Types"),
                                           Constants.EDIT_ICON_NAME,
                                           this.projectViewer.getAction (ProjectViewer.MANAGE_ITEM_TYPES_ACTION)));

        }       

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

        ((DefaultTreeModel) tree.getModel ()).setRoot (UIUtils.createAssetTree (this.forObjType,
                                                                                this.projectViewer.getProject ()));

        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        for (TreePath p : openPaths)
        {

            tree.expandPath (UIUtils.getTreePathForUserObject (root,
                                                               ((DefaultMutableTreeNode) p.getLastPathComponent ()).getUserObject ()));

        }        
        
    }
        
    public boolean showItemCountOnHeader ()
    {
        
        return true;
        
    }
    
    public int getItemCount ()
    {
        
        int c = this.projectViewer.getProject ().getAllNamedChildObjects (Asset.getAssetClass (this.forObjType)).size ();
        
        return c;
                
    }
    
    public DragActionHandler getTreeDragActionHandler (ProjectViewer pv,
                                                       JTree         tree)
    {
        
        return null;
        
    }

    @Override
    public void fillTreePopupMenu (JPopupMenu m,
                                   MouseEvent ev)
    {

        final AssetAccordionItem _this = this;

        final TreePath tp = _this.tree.getPathForLocation (ev.getX (),
                                                           ev.getY ());

        JMenuItem mi = null;

        if (tp != null)
        {

            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            final DataObject d = (DataObject) node.getUserObject ();

            mi = new JMenuItem ("View",
                                Environment.getIcon ("view",
                                                     Constants.ICON_MENU));

            mi.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.projectViewer.viewObject (d);

                }

            });

            m.add (mi);

            m.add (UIUtils.createMenuItem ("Rename",
                                           Constants.EDIT_ICON_NAME,
                                           AssetViewPanel.getRenameAssetAction (this.projectViewer,
                                                                                (Asset) d)));
            /*
            mi = new JMenuItem ("Rename",
                                Environment.getIcon ("edit",
                                                     Constants.ICON_MENU));

            m.add (mi);

            mi.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.tree.setSelectionPath (tp);
                    _this.tree.startEditingAtPath (tp);

                }

            });
*/
            mi = new JMenuItem ("Delete",
                                Environment.getIcon ("delete",
                                                     Constants.ICON_MENU));

            m.add (mi);

            mi.addActionListener (AssetViewPanel.getDeleteAssetAction ((ProjectViewer) _this.projectViewer,
                                                                       (Asset) d));

        }

    }

    public TreeCellEditor getTreeCellEditor (ProjectViewer pv,
                                             JTree         tree)
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
        
}