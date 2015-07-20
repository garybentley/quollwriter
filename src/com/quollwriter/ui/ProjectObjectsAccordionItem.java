package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import java.awt.dnd.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

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
    protected E projectViewer = null;
    
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
        this.projectViewer = pv;

        this.tree = this.createTree ();
    
    }

    public boolean isContentVisible ()
    {
        
        return this.tree.isVisible ();
        
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
    
    public abstract void init (JTree tree);
    
    public abstract void fillTreePopupMenu (JPopupMenu menu,
                                            MouseEvent ev);
    
    public abstract TreeCellEditor getTreeCellEditor (E     pv,
                                                      JTree tree);
    
    public abstract int getViewObjectClickCount (Object d);
    
    public abstract boolean isTreeEditable ();
    
    public abstract boolean isDragEnabled ();
    
    public abstract void reloadTree (JTree tree);
    
    public abstract DragActionHandler getTreeDragActionHandler (E     pv,
                                                                JTree tree);
    
    public void update ()
    {

        int c = this.getItemCount ();
        
        if (c == 0)
        {
            
            super.setContentVisible (false);
            
        }
                        
        this.updateItemCount (c);
        
        this.reloadTree (this.tree);
        
    }
    
    public void init ()
    {

        this.setContentVisible (true);
        
        this.init (this.tree);
        
        this.tree.setOpaque (false);
        
        super.init ();        
        
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

        this.tree.setCellEditor (this.getTreeCellEditor (this.projectViewer,
                                                         this.tree));

        this.tree.setEditable (this.isTreeEditable ());

        PopupPreviewListener mm = new PopupPreviewListener (this);
        
        this.tree.addMouseMotionListener (mm);
        this.tree.addMouseListener (mm);
        
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
            this.tree.setDropTarget (new DropTarget (this.tree,
                                                     DataObjectTransferHandler.getDropHandler ()));

            this.tree.setDropMode (DropMode.ON);
            this.tree.setTransferHandler (new DataObjectTransferHandler (this.projectViewer,
                                                                         this.getTreeDragActionHandler (this.projectViewer,
                                                                                                        this.tree)));

        }

        this.tree.setBorder (new EmptyBorder (0, 7, 0, 0));
        
        return this.tree;
        
    }
    
    protected void handleViewObject (TreePath tp,
                                     Object   obj)
    {
        
        this.projectViewer.viewObject ((DataObject) obj);
        
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
                  
            this.popup = new NamedObjectPreviewPopup (this.item.projectViewer);
                  
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
            if (!this.item.projectViewer.getProject ().getPropertyAsBoolean (Constants.SHOW_QUICK_OBJECT_PREVIEW_IN_PROJECT_SIDEBAR_PROPERTY_NAME))
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
            
            Point po = this.item.projectViewer.convertPoint (this.item.tree,
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
/*
            Point p = new Point (0,
                                 0);
*/
            this.lastObject = null;
        
            this.popup.hidePopup ();
/*
 *No idea what was going on down here... leave for now, clean up if not used
            SwingUtilities.convertPointToScreen (p,
                                                 this.item.tree);

            java.awt.Rectangle tBounds = this.item.tree.getBounds (null);

            tBounds.x = p.x;
            tBounds.y = p.y;

            if (!tBounds.contains (ev.getLocationOnScreen ()))
            {

                this.lastObject = null;
            
                this.popup.hidePopup ();

            }
            */
        }        
        
    }
    
}