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
import java.util.Map;
import java.util.Enumeration;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.panels.*;

public abstract class ProjectObjectsAccordionItem<E extends AbstractProjectViewer> extends AccordionItem
{
    
    private final DataFlavor flavor = new DataFlavor (NamedObject.class, "named object");                                
    
    protected JTree tree = null;
    protected E viewer = null;
    private boolean inited = false;
    
    public ProjectObjectsAccordionItem (String title,
                                        String iconType,
                                        E      pv)
    {
        
        super (title,
               iconType);
        
        this.viewer = pv;
        
        this.tree = this.createTree ();
        
    }
    
    public ProjectObjectsAccordionItem (String    title,
                                        ImageIcon icon,
                                        E         pv)
    {
        
        super (title,
               icon);

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
    public abstract String getId ();
        
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

        if (this.inited)
        {
            
            return;
            
        }
    
        this.inited = true;
    
        super.init ();
    
        this.setContentVisible (true);
                
        this.tree.setOpaque (false);
                
        this.initTree ();

        final ProjectObjectsAccordionItem _this = this;
        
        final TransferHandler th = this.getTransferHandler ();
        
        // Allow for foreign object import.
        this.setTransferHandler (new TransferHandler ()
        {
            
            @Override
            public int getSourceActions (JComponent c)
            {
                                    
                if (th != null)
                {
                    
                    return th.getSourceActions (c);
                    
                }
                
                return -1;
            
            }
            
            @Override
            public Transferable createTransferable (final JComponent c)
            {
            
                return new Transferable ()
                {
                    
                    @Override
                    public Object getTransferData (DataFlavor flavor)
                    {
                        
                        Component[] components = new Component[1];
                        components[0] = c;
                        return components;
                    
                    }
        
                    @Override
                    public DataFlavor[] getTransferDataFlavors()
                    {
                        
                        DataFlavor[] flavors = new DataFlavor[1];
                        flavors[0] = com.quollwriter.ui.sidebars.ProjectSideBar.COMPONENT_FLAVOR;
                        return flavors;
                    
                    }
        
                    @Override
                    public boolean isDataFlavorSupported (DataFlavor flavor)
                    {
                        
                        return flavor.equals (com.quollwriter.ui.sidebars.ProjectSideBar.COMPONENT_FLAVOR);
                    
                    }
                    
                };
/*            
                if (th != null)
                {
                    
                    return th.createTransferable (c);
                    
                }
  */          
                //return null;
            
            }
            
            @Override
            public boolean importData (TransferSupport supp)
            {
                
                Transferable t = supp.getTransferable ();
                
                Object o = null;
                
                try
                {

                    o = t.getTransferData (t.getTransferDataFlavors ()[0]);
                    
                } catch (Exception e) {
                    
                    return false;
                    
                }

                JComponent comp = (JComponent) supp.getComponent ();
                
                if (!(o instanceof NamedObject))
                {
                    
                    if (th != null)
                    {
                        
                        return th.importData (supp);
                        
                    }
                    /*
                    while ((comp = (JComponent) comp.getParent ()) != null)
                    {
                        
                        TransferHandler th = comp.getTransferHandler ();
                        
                        if (th != null)
                        {
                            
                            return th.importData (supp);
                                
                        }
                    
                    }                        
                    */
                    return false;
                    
                }
                
                NamedObject obj = (NamedObject) o;
                                
                // Are we in our tree?
                DataFlavor[] f = supp.getDataFlavors ();
                            
                if (f[0] != flavor)
                {                    

                    DragActionHandler handler = _this.getTreeDragActionHandler (_this.viewer);

                    if (handler != null)
                    {
                    
                        try
                        {

                            if (!handler.importForeignObject (obj,
                                                              0))
                            {
                                
                                UIUtils.showErrorMessage (_this.viewer,
                                                          "Unable to move.");
                                
                                return false;
                                
                            }

                            QuollPanel qp = _this.viewer.getCurrentlyVisibleTab ();
                            
                            if (qp instanceof ProjectObjectQuollPanel)
                            {
                                
                                _this.viewer.viewObject (((ProjectObjectQuollPanel) qp).getForObject ());
                                
                            }                            
                            
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to import foreign object: " +
                                                  obj,
                                                  e);
                            
                            return false;
                            
                        }
                        
                    }

                    return true;
                
                }

                return false;

            }
                
            @Override
            public boolean canImport (TransferSupport supp)
            {
                
                Transferable t = supp.getTransferable ();
                
                Object o = null;
                
                try
                {

                    o = t.getTransferData (t.getTransferDataFlavors ()[0]);
                    
                } catch (Exception e) {
                    
                    return false;
                    
                }

                JComponent comp = (JComponent) supp.getComponent ();
                
                if (!(o instanceof NamedObject))
                {
                    
                    if (th != null)
                    {
                    
                        return th.canImport (supp);
                        
                    }
                    /*
                    while ((comp = (JComponent) comp.getParent ()) != null)
                    {
                        
                        TransferHandler th = comp.getTransferHandler ();
                        
                        if (th != null)
                        {
                            
                            return th.canImport (supp);
                                
                        }
                    
                    }                        
                    */
                    
                    return false;
                    
                }
                
                // Are we in our tree?
                DataFlavor[] f = supp.getDataFlavors ();
                            
                if (f[0] == flavor)
                {
                                       
                    return false;
                                    
                }
                                                
                NamedObject obj = (NamedObject) o;
                
                // Are we importing from a different tree/object.
                DragActionHandler handler = _this.getTreeDragActionHandler (_this.viewer);

                if (handler != null)
                {
                
                    try
                    {

                        if (handler.canImportForeignObject (obj))
                        {
                        
                            supp.setShowDropLocation (true);
    
                            return true;
                        
                        }

                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to check if foreign object can be imported: " +
                                              obj,
                                              e);
                        
                        return false;
                        
                    }
                    
                }
                
                return false;
                                    
            }
            
        });
               
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

            this.tree.setDropMode (DropMode.ON);
                        
            this.tree.setTransferHandler (new TransferHandler ()
            {
                          
                private java.awt.image.BufferedImage dragImage = null;
                                                                
                @Override
                public int getSourceActions (JComponent c)
                {
                                      
                    JTree tree = (JTree) c;
                    
                    Point p = tree.getMousePosition ();
                    
                    TreePath tp = tree.getPathForLocation (p.x, p.y);
                                        
                    final NamedObject o = (NamedObject) UIUtils.getUserObjectForTreePath (tp);

                    if (o == null)
                    {
                        
                        return -1;
                        
                    }
                    
                    JLabel l = new JLabel (o.getName ());
                    
                    this.dragImage = UIUtils.getImageOfComponent (l,
                                                                  l.getPreferredSize ().width,
                                                                  l.getPreferredSize ().height);
                    
                    this.setDragImage (this.dragImage);
            
                    return MOVE;
                
                }
                  
                @Override
                public java.awt.Image getDragImage ()
                {
                    
                    Point p = _this.tree.getMousePosition ();
                    
                    TreePath tp = _this.tree.getPathForLocation (p.x, p.y);
                                        
                    final NamedObject o = (NamedObject) UIUtils.getUserObjectForTreePath (tp);

                    if (o == null)
                    {
                        
                        return null;
                        
                    }
                    
                    JLabel l = new JLabel (o.getName ());
                    
                    this.dragImage = UIUtils.getImageOfComponent (l,
                                                                  l.getPreferredSize ().width,
                                                                  l.getPreferredSize ().height);                    
                                
                    return this.dragImage;
                                
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
                                    
                    if (!supp.isDrop ())
                    {
                    
                        return false;
                    }                                        
                                        
                    JComponent comp = (JComponent) supp.getComponent ();

                    // Are we in our tree?
                    DataFlavor[] f = supp.getDataFlavors ();
                                
                    if (f[0] == flavor)
                    {
                        
                        supp.setShowDropLocation (true);
                    
                        return true;
                                        
                    }
                    
                    Transferable t = supp.getTransferable ();
                                        
                    Object o = null;
                    
                    try
                    {

                        o = t.getTransferData (t.getTransferDataFlavors ()[0]);
                        
                    } catch (Exception e) {
                        
                        return false;
                        
                    }

                    if (!(o instanceof NamedObject))
                    {
                        
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
                    
                    NamedObject obj = (NamedObject) o;
                    
                    // Are we importing from a different tree/object.
                    DragActionHandler handler = _this.getTreeDragActionHandler (_this.viewer);

                    if (handler != null)
                    {
                    
                        try
                        {

                            if (handler.canImportForeignObject (obj))
                            {
                            
                                supp.setShowDropLocation (true);
        
                                return true;
                            
                            }

                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to check if foreign object can be imported: " +
                                                  obj,
                                                  e);
                            
                            return false;
                            
                        }
                        
                    }

                    return false;
                                        
                }
                
                @Override
                public boolean importData (TransferSupport supp)
                {
                    
                    Transferable t = supp.getTransferable ();
                    
                    Object o = null;
                                        
                    try
                    {
                    
                        o = t.getTransferData (t.getTransferDataFlavors ()[0]);
                        
                    } catch (Exception e) {
                        
                        UIUtils.showErrorMessage (_this.viewer,
                                                  "Unable to move item.");

                        return false;
                        
                    }
                    
                    if (!(o instanceof NamedObject))
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

                    NamedObject obj = (NamedObject) o;
                    
                    // Are we in our tree?
                    DataFlavor[] f = supp.getDataFlavors ();
                                
                    if (f[0] != flavor)
                    {                    
                    
                        JTree.DropLocation dl = _this.tree.getDropLocation ();
                        
                        if (dl.getPath () == null)
                        {
                            
                            return false;
                            
                        }

                        int insertRow = -1;
    
                        Point dp = dl.getDropPoint ();
                        
                        if (dp != null)
                        {
    
                            insertRow = _this.tree.getRowForPath (dl.getPath ());
    
                        }
                        
                        DragActionHandler handler = _this.getTreeDragActionHandler (_this.viewer);
    
                        if (handler != null)
                        {
                                                
                            _this.tree.getSelectionModel ().clearSelection ();    
                        
                            try
                            {
                        
                                if (!handler.importForeignObject (obj,
                                                                  insertRow))
                                {
                                    
                                    UIUtils.showErrorMessage (_this.viewer,
                                                              "Unable to move.");
                                    
                                    return false;
                                    
                                }

                                QuollPanel qp = _this.viewer.getCurrentlyVisibleTab ();
                                
                                if (qp instanceof ProjectObjectQuollPanel)
                                {
                                    
                                    _this.viewer.viewObject (((ProjectObjectQuollPanel) qp).getForObject ());
                                    
                                }
                                
                            } catch (Exception e) {
                                
                                Environment.logError ("Unable to import foreign object: " +
                                                      obj,
                                                      e);
                                
                                return false;
                                
                            }
                            
                        }
                        
                        return true;
                    
                    }
                                        
                    //JTree tree = (JTree) supp.getComponent ();

                    JTree.DropLocation dl = _this.tree.getDropLocation ();
                    
                    if (dl.getPath () == null)
                    {
                        
                        return false;
                        
                    }
                    
                    int insertRow = -1;

                    Point dp = dl.getDropPoint ();
                    
                    if (dp != null)
                    {

                        insertRow = _this.tree.getRowForPath (dl.getPath ());

                    } 

                    DefaultTreeModel model = ((DefaultTreeModel) _this.tree.getModel ());

                    TreePath tp = UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                                    obj);

                    int removeRow = -1;
                                      
                    removeRow = _this.tree.getRowForPath (tp);
                                                                    
                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                    DefaultMutableTreeNode p = (DefaultMutableTreeNode) n.getParent ();

                    model.removeNodeFromParent (n);

                    model.insertNodeInto (n,
                                          p,
                                          insertRow);

                    _this.tree.getSelectionModel ().clearSelection ();

                    _this.tree.setSelectionRow (insertRow);
                    
                    _this.tree.requestFocus ();
                                        
                    DragActionHandler handler = _this.getTreeDragActionHandler (_this.viewer);

                    if (handler != null)
                    {
                    
                        try
                        {
                            
                            if (!handler.handleMove (removeRow,
                                                     insertRow,
                                                     obj))
                            {
                                                            
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
            
                //this.tree.getDropTarget ().addDropTargetListener (dropTargetListener);
    
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