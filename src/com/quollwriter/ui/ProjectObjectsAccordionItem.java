package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Insets;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import java.util.ArrayList;
import java.util.Enumeration;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.renderers.*;

public abstract class ProjectObjectsAccordionItem<E extends AbstractProjectViewer> extends AccordionItem
{
    
    protected JTree tree = null;
    protected String forObjType = null;
    protected E viewer = null;
    
    public ProjectObjectsAccordionItem (String title,
                                        String iconType,
                                        String forObjType,
                                        E      pv)
    {
        
        super (title,
               iconType);
        /*
        Environment.getObjectTypeNamePlural (objType),
               objType);
        */
        this.forObjType = forObjType;
        this.viewer = pv;

        this.tree = this.createTree ();
        
    }

    public E getViewer ()
    {
        
        return this.viewer;
        
    }
    
    public boolean isContentVisible ()
    {
        
        return this.tree.isVisible ();
        
    }
    
    @Override
    public String getId ()
    {
        
        return this.getForObjectType ();
        
    }
    
    public String getForObjectType ()
    {
        
        return this.forObjType;        
        
    }
    
    public JTree getTree ()
    {
        
        return this.tree;
        
    }
    
    public JComponent getContent ()
    {
        
        return this.tree;
        
    }

    public void removeObject (DataObject n)
    {
        
        UIUtils.removeNodeFromTree (this.tree,
                                    n);
        
        this.setContentVisible (true);
        
    }
            
    public void setContentVisible (boolean v)
    {

        int c = this.getItemCount ();
        
        if (c == 0)
        {
            
            v = false;
            
        }

        super.setContentVisible (v);
                
        this.updateItemCount (c);
        
        this.validate ();
        this.repaint ();
        
    }

    public void updateItemCount (int c)
    {
        
        String title = this.getTitle ();

        if (this.showItemCountOnHeader ())
        {
        
            title += " (" + c + ")";
            
        }

        // Set the title on the header directly.
        this.header.setTitle (title);
        
    }
    
    public abstract boolean showItemCountOnHeader ();
    
    public abstract int getItemCount ();
    
    public abstract void initTree ();
    
    public abstract void fillTreePopupMenu (JPopupMenu menu,
                                            MouseEvent ev);
    
    public abstract TreeCellEditor getTreeCellEditor (E     pv);
    
    public abstract int getViewObjectClickCount (Object d);
    
    public abstract boolean isTreeEditable ();
    
    public abstract boolean isDragEnabled ();
    
    public abstract boolean isAllowObjectPreview ();
    
    public abstract void reloadTree ();
    
    public abstract DragActionHandler getTreeDragActionHandler (E     pv);
    
    public void update ()
    {

        int c = this.getItemCount ();
        
        if (c == 0)
        {
            
            super.setContentVisible (false);
            
        }
                        
        this.updateItemCount (c);
        
        java.util.List<TreePath> openPaths = new ArrayList ();
        
        TreePath[] selPaths = this.tree.getSelectionPaths ();

        Enumeration<TreePath> paths = this.tree.getExpandedDescendants (new TreePath (this.tree.getModel ().getRoot ()));

        if (paths != null)
        {

            while (paths.hasMoreElements ())
            {

                TreePath el = paths.nextElement ();
            
                openPaths.add (el);
               
            }

        }        
        
        this.reloadTree ();
        
        DefaultTreeModel dtm = (DefaultTreeModel) this.tree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        for (TreePath p : openPaths)
        {

            this.tree.expandPath (UIUtils.getTreePathForUserObjects (root,
                                                                     p));
                                                                    //((DefaultMutableTreeNode) p.getLastPathComponent ()).getUserObject ()));


        }
        
        if (selPaths != null)
        {
        
            for (TreePath p : selPaths)
            {
        
                this.tree.setSelectionPath (UIUtils.getTreePathForUserObjects (root,
                                                                               p));
            
            }
            
        }
        
    }
    
    @Override
    public void init ()
    {

        super.init ();
    
        this.setContentVisible (true);
                
        this.tree.setOpaque (false);
                
        this.initTree ();
                
    }
    
    public void clearSelectedItemInTree ()
    {
        
        this.tree.clearSelection ();
        
    }
    
    public void setObjectSelectedInTree (DataObject d)
    {
        
        // Set the content visible.
        this.setContentVisible (true);
        
        // See how many children there are.
        TreePath tp = UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) this.tree.getModel ().getRoot (),
                                                        d);

        if (tp != null)
        {

            this.tree.setSelectionPath (tp);
            
        }
        
    }
    
    public void toggleTreePath (TreePath tp)
    {
        
        if (this.tree.isCollapsed (tp))
        {
            
            this.tree.expandPath (tp);
            
        } else {
            
            this.tree.collapsePath (tp);
            
        }
        
    }
    
    private JTree createTree ()
    {

        final ProjectObjectsAccordionItem _this = this;

        this.tree = UIUtils.createTree ();

        this.tree.setCellEditor (this.getTreeCellEditor (this.viewer));

        this.tree.setEditable (this.isTreeEditable ());

        PopupPreviewListener mm = new PopupPreviewListener (this);
        
        if (this.isAllowObjectPreview ())
        {
                
            this.tree.addMouseMotionListener (mm);
            this.tree.addMouseListener (mm);

        }
        
        this.tree.addMouseListener (new MouseEventHandler ()
        {
        
            @Override
            public void fillPopup (JPopupMenu menu,
                                   MouseEvent ev)
            {
                
                _this.fillTreePopupMenu (menu,
                                         ev);
                
            }
                
            @Override
            public void handlePress (MouseEvent ev)
            {
                
                // Edit the chapter.
                TreePath tp = _this.tree.getPathForLocation (ev.getX (),
                                                             ev.getY ());

                if (tp == null)
                {

                    return;

                }

                Object d = ((DefaultMutableTreeNode) tp.getLastPathComponent ()).getUserObject ();

                if (d instanceof TreeParentNode)
                {
                    
                    if (_this.tree.isCollapsed (tp))
                    {
                        
                        _this.tree.expandPath (tp);
                                                
                    } else {
                        
                        _this.tree.collapsePath (tp);
                        
                    }
                    
                    return;
                    
                }
                                
                if ((ev.getClickCount () == _this.getViewObjectClickCount (d)) &&
                    (!ev.isPopupTrigger ()))
                {

                    _this.handleViewObject (tp,
                                            d);
             
                }
                /*
                if (ev.isPopupTrigger ())
                {
                    
                    _this.showTreePopupMenu (ev);
                    
                }
                */
            }
                
        });

        if (this.isDragEnabled ())
        {

            this.tree.setDragEnabled (this.isDragEnabled ());
            /*
            this.tree.setDropTarget (new DropTarget (this.tree,
                                                     DataObjectTransferHandler.getDropHandler ()));
*/
            this.tree.setDropMode (DropMode.ON);
                        
            this.tree.setTransferHandler (new TransferHandler ()
            {
                
                final DataFlavor flavor = new DataFlavor (DataObject.class, "data object");
                
                @Override
                public int getSourceActions (JComponent c)
                {
                                      
                    JTree tree = (JTree) c;
                    
                    Point p = tree.getMousePosition ();
                    
                    TreePath tp = tree.getPathForLocation (p.x, p.y);
                                        
                    final NamedObject o = (NamedObject) UIUtils.getUserObjectForTreePath (tp);

                    JLabel l = new JLabel (o.getName ());
                    
                    this.setDragImage (UIUtils.getImageOfComponent (l,
                                                                    l.getPreferredSize ().width,
                                                                    l.getPreferredSize ().height));
            
                    return MOVE;
                
                }
                                
                @Override
                public Transferable createTransferable (JComponent c)
                {
                    
                    if (c instanceof JTree)
                    {

                        JTree tree = (JTree) c;
                        
                        TreePath tp = tree.getSelectionPath ();
                        
                        final Object o = UIUtils.getUserObjectForTreePath (tp);
                        
                        Transferable t = new Transferable ()
                        {
                            
                            @Override
                            public Object getTransferData (DataFlavor f)
                            {
                                
                                if (f == flavor)
                                {
                                    
                                    return o;
                                    
                                }
                              
                                return null;
                                
                            }
                            
                            @Override
                            public DataFlavor[] getTransferDataFlavors ()
                            {
                                
                                return new DataFlavor[] { flavor };
                                
                            }
                            
                            @Override
                            public boolean isDataFlavorSupported (DataFlavor flavor)
                            {
                                
                                return flavor == flavor;
                                
                            }
                            
                        };
                        
                        return t;
                        
                    }
                    
                    return null;
                    
                }
                                
                @Override
                public boolean canImport (TransferSupport supp)
                {
                                        
                    DataFlavor[] f = supp.getDataFlavors ();
                                
                    if (f[0] == flavor)
                    {
                        
                        supp.setShowDropLocation (true);
                        
                        return true;
                                        
                    }
                    
                    JComponent comp = (JComponent) supp.getComponent ();
                    
                    while ((comp = (JComponent) comp.getParent ()) != null)
                    {
                        
                        TransferHandler th = comp.getTransferHandler ();
                        
                        if (th != null)
                        {
                            
                            return th.canImport (supp);
                                
                        }
                    
                    }
                    
                    return false;
                                        
                }
                
                @Override
                public boolean importData (TransferSupport supp)
                {
                    
                    if (supp.getDataFlavors ()[0] != flavor)
                    {
                        
                        JComponent comp = (JComponent) supp.getComponent ();
                        
                        while ((comp = (JComponent) comp.getParent ()) != null)
                        {
                            
                            TransferHandler th = comp.getTransferHandler ();
                            
                            if (th != null)
                            {

                                return th.importData (supp);
                                    
                            }
                        
                        }                        
                        
                        return false;
                        
                    }
                    
                    Transferable t = supp.getTransferable ();
                    
                    NamedObject obj = null;
                    
                    try
                    {
                    
                        obj = (NamedObject) t.getTransferData (flavor);
                        
                    } catch (Exception e) {
                        
                        UIUtils.showErrorMessage (_this.viewer,
                                                  "Unable to move item.");

                        return false;
                        
                    }
                    
                    JTree tree = (JTree) supp.getComponent ();

                    JTree.DropLocation dl = tree.getDropLocation ();
                    
                    if (dl.getPath () == null)
                    {
                        
                        return false;
                        
                    }
                    
                    int insertRow = -1;

                    Point dp = dl.getDropPoint ();
                    
                    if (dp != null)
                    {

                        insertRow = tree.getRowForLocation (dp.x,
                                                            dp.y);

                    } 

                    DefaultTreeModel model = ((DefaultTreeModel) tree.getModel ());

                    TreePath tp = UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                                    obj);

                    int removeRow = -1;
                                      
                    removeRow = tree.getRowForPath (tp);
                                                                    
                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                    DefaultMutableTreeNode p = (DefaultMutableTreeNode) n.getParent ();

                    model.removeNodeFromParent (n);

                    model.insertNodeInto (n,
                                          p,
                                          insertRow);

                    tree.getSelectionModel ().clearSelection ();

                    tree.setSelectionRow (insertRow);
                    
                    tree.requestFocus ();
                                        
                    DragActionHandler handler = _this.getTreeDragActionHandler (_this.viewer);

                    if (handler != null)
                    {
                    
                        try
                        {
                            
                            if (!handler.handleMove (removeRow,
                                                     insertRow,
                                                     obj))
                            {
                            
                                System.out.println ("CANT");
                                
                                return false;
                                
                            }
                            
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to move: " +
                                                  obj +
                                                  " from row: " +
                                                  removeRow +
                                                  " to row: " +
                                                  insertRow,
                                                  e);

                            UIUtils.showErrorMessage (_this.viewer,
                                                      "Unable to move.");

                            return false;
                            
                        }
                        
                    }
                    
                    return true;
                    
                }
                   
                @Override
                public void exportDone (JComponent   c,
                                        Transferable t,
                                        int          action)
                {
                    
                    /*
                    Transferable t = supp.getTransferable ();
                    
                    Object obj = null;
                    
                    try
                    {
                    
                        obj = t.getTransferData (flavor);
                        
                    } catch (Exception e) {
                        
                        UIUtils.showErrorMessage (_this.viewer,
                                                  "Unable to move item.");

                        return false;
                        
                    }                    
                    
                    TreePath removeTreePath = (TreePath) data.getTransferData (DataFlavor.stringFlavor);

                    JTree tree = (JTree) source;

                    DefaultTreeModel model = ((DefaultTreeModel) tree.getModel ());

                    if (source != getDropComponent ())
                    {

                        model.removeNodeFromParent ((DefaultMutableTreeNode) removeTreePath.getLastPathComponent ());

                    } else
                    {

                        int insertRow = tree.getRowForLocation (getDropPoint ().x,
                                                                getDropPoint ().y);

                        int removeRow = tree.getRowForPath (removeTreePath);

                        TreePath insertTreePath = tree.getPathForRow (insertRow);

                        DefaultMutableTreeNode removeNode = (DefaultMutableTreeNode) removeTreePath.getLastPathComponent ();

                        DefaultMutableTreeNode insertNode = (DefaultMutableTreeNode) insertTreePath.getLastPathComponent ();

                        NamedObject removeObject = (NamedObject) removeNode.getUserObject ();

                        NamedObject insertObject = (NamedObject) insertNode.getUserObject ();

                        try
                        {

                            if (!this.dragActionHandler.performAction (removeRow,
                                                                       removeObject,
                                                                       insertRow,
                                                                       insertObject))
                            {

                                return;

                            }

                        } catch (Exception e)
                        {

                            Environment.logError ("Unable to move: " +
                                                  removeObject +
                                                  " from row: " +
                                                  removeRow +
                                                  " to row: " +
                                                  insertRow,
                                                  e);

                            UIUtils.showErrorMessage (this.projectViewer,
                                                      "Unable to move.");

                            return;

                        }

                    }
                    */
                }
                                
            });
            
            DropTargetListener dropTargetListener = new DropTargetAdapter ()
            {
                     
                @Override
                public void drop (DropTargetDropEvent ev)
                {
    
                }                 
                                
                @Override
                public void dragOver (DropTargetDragEvent ev)
                {
                    
                    JComponent comp = (JComponent) ev.getDropTargetContext ().getComponent ();
                    
                    // Get the parents of the tree until we get to a scrollpane.
                    while ((comp = (JComponent) comp.getParent ()) != null)
                    {
                        
                        if (comp instanceof JScrollPane)
                        {
                            
                            break;
                            
                        }
                        
                    }
                    
                    if (comp == null)
                    {
                        
                        return;
                        
                    }
                    
                    JScrollPane sp = (JScrollPane) comp;
                    
                    java.awt.Point mp = sp.getMousePosition ();
                    
                    if (mp == null)
                    {
                        
                        return;
                        
                    }
                    
                    int a = sp.getVerticalScrollBar ().getUnitIncrement ();
                    
                    java.awt.Point vp = sp.getViewport ().getViewPosition ();
    
                    if (mp.y <= 5 * a)
                    {
                                                
                        int newy = vp.y - (a / 2);
                        
                        if (newy < 0)
                        {
                            
                            newy = 0;
                            
                        }
                        
                        sp.getViewport ().setViewPosition (new java.awt.Point (vp.x, newy));
                        
                        return;
                        
                    }
    
                    int h = sp.getViewport ().getExtentSize ().height;
    
                    if (mp.y >= (h - (5 * a)))
                    {
                                                
                        int newy = vp.y + (a / 2);
                        
                        if (newy > (vp.y + h))
                        {
                            
                            newy = (vp.y + h);
                            
                        }
                        
                        sp.getViewport ().setViewPosition (new java.awt.Point (vp.x, newy));
                        
                        return;                        
                                        
                    }
                    
                }
                            
            };        
            
            try
            {
            
                this.tree.getDropTarget ().addDropTargetListener (dropTargetListener);
    
            } catch (Exception e) {
                
                e.printStackTrace ();
                
            }        
            
/*
            this.tree.setTransferHandler (new DataObjectTransferHandler (this.viewer,
                                                                         this.getTreeDragActionHandler (this.viewer)));
*/
        }
        
        this.tree.setBorder (new EmptyBorder (0, 7, 0, 0));
        
        return this.tree;
        
    }
    
    protected void handleViewObject (TreePath tp,
                                     Object   obj)
    {
        
        this.viewer.viewObject ((DataObject) obj);
        
    }
    
    private class PopupPreviewListener extends MouseEventHandler
    {
        
        private NamedObjectPreviewPopup popup = null;
        private NamedObject lastObject = null;
        private ProjectObjectsAccordionItem item = null;
        
        // This timer is used when the user presses a button
        private Timer showDelayTimer = null;
                  
        public PopupPreviewListener (ProjectObjectsAccordionItem item)
        {
            
            this.item = item;   
                  
            this.popup = new NamedObjectPreviewPopup (this.item.viewer);
                  
        }
        
        @Override
        public void handlePress (MouseEvent ev)
        {
            
            this.popup.hidePopup ();
            
        }
        
        @Override
        public void mouseMoved (MouseEvent ev)
        {

            // Should be an achievement for having such a long var name...
            if (!this.item.viewer.getProject ().getPropertyAsBoolean (Constants.SHOW_QUICK_OBJECT_PREVIEW_IN_PROJECT_SIDEBAR_PROPERTY_NAME))
            {
                
                return;
                
            }
        
            final PopupPreviewListener _this = this;
        
            // Edit the chapter.
            TreePath tp = this.item.tree.getPathForLocation (ev.getX (),
                                                             ev.getY ());

            if (tp == null)
            {

                return;

            }

            Object d = ((DefaultMutableTreeNode) tp.getLastPathComponent ()).getUserObject ();

            if (!(d instanceof NamedObject))
            {
                
                return;
                
            }

            if ((d instanceof TreeParentNode)
                ||
                (d instanceof Note)
               )
            {
                
                return;
                
            }
            
            if (d == this.lastObject)
            {
                
                return;
                
            }
            
            if (d != this.lastObject)
            {
                
                // Hide the popup.
                this.popup.hidePopup ();
                
            }
                            
            this.lastObject = (NamedObject) d;
            
            Point po = this.item.viewer.convertPoint (this.item.tree,
                                                      new Point (ev.getX () + 10,
                                                                 this.item.tree.getPathBounds (tp).y + this.item.tree.getPathBounds (tp).height - 5));
            
            // Show the first line of the description.
            this.popup.show ((NamedObject) d,
                             1000,
                             250,
                             po,
                             new ActionAdapter ()
                             {
                                
                                public void actionPerformed (ActionEvent ev)
                                {
                                    
                                    _this.lastObject = null;
                                    
                                }
                                
                             });
                            
        }
        
        @Override
        public void mouseExited (MouseEvent ev)
        {

            this.lastObject = null;
        
            this.popup.hidePopup ();

        }        
        
    }
    
}