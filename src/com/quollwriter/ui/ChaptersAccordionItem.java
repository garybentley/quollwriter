package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import java.awt.dnd.*;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.tree.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.panels.*;

public class ChaptersAccordionItem extends ProjectObjectsAccordionItem<AbstractProjectViewer>
{
        
    public ChaptersAccordionItem (AbstractProjectViewer pv)
    {
        
        super (Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE),
               Chapter.OBJECT_TYPE,
               Chapter.OBJECT_TYPE,
               pv);
            
    }
    
    public void init (JTree tree)
    {

        this.addHeaderPopupMenuItem ("Add New {Chapter}",
                                     Constants.ADD_ICON_NAME,
                                     this.projectViewer.getAction (ProjectViewer.NEW_CHAPTER_ACTION,
                                                                   this.projectViewer.getProject ().getBooks ().get (0)));

        ((DefaultTreeModel) tree.getModel ()).setRoot (UIUtils.createChaptersTree (this.projectViewer.getProject (),
                                                                                   null,
                                                                                   null,
                                                                                   false));        
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

        ((DefaultTreeModel) tree.getModel ()).setRoot (UIUtils.createChaptersTree (this.projectViewer.getProject (),
                                                                                   null,
                                                                                   null,
                                                                                   false));        

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
        
        int c = this.projectViewer.getProject ().getAllNamedChildObjects (Chapter.class).size ();
        
        return c;
                
    }

    @Override
    public void fillTreePopupMenu (final JPopupMenu m,
                                   final MouseEvent ev)
    {

        final TreePath tp = this.tree.getPathForLocation (ev.getX (),
                                                          ev.getY ());
        
        final ChaptersAccordionItem _this = this;

        final AbstractProjectViewer pv = this.projectViewer;
        
        JMenuItem mi = null;

        if (tp != null)
        {

            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            final DataObject d = (DataObject) node.getUserObject ();

            if (d instanceof Note)
            {
                
                final Note n = (Note) d;
                
                m.add (UIUtils.createMenuItem ("View",
                                               Constants.VIEW_ICON_NAME,
                                               new ActionAdapter ()
                                               {
                                
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                
                                                        pv.viewObject (n);
                                
                                                    }
                                
                                               }));

                m.add (UIUtils.createMenuItem ("Edit",
                                               Constants.EDIT_ICON_NAME,
                                               pv.getAction (ProjectViewer.EDIT_NOTE_ACTION,
                                                             n)));

                m.add (UIUtils.createMenuItem ("Delete",
                                               Constants.DELETE_ICON_NAME,
                                               pv.getAction (ProjectViewer.DELETE_NOTE_ACTION,
                                                             n)));
                
            }
            
            if (d instanceof Chapter)
            {

                final Chapter c = (Chapter) d;

                final String chapterObjTypeName = Environment.getObjectTypeName (c);

                m.add (UIUtils.createMenuItem ("Edit {Chapter}",
                                               Constants.EDIT_ICON_NAME,
                                               pv.getAction (ProjectViewer.EDIT_CHAPTER_ACTION,
                                                             c)));

                m.add (UIUtils.createMenuItem ("View {Chapter} Information",
                                               Constants.INFO_ICON_NAME,
                                               pv.getAction (ProjectViewer.VIEW_CHAPTER_INFO_ACTION,
                                                             c)));

                if (!c.isEditComplete ())
                {                                                             
                
                    m.add (UIUtils.createMenuItem ("Set as Edit Complete",
                                                   Constants.EDIT_COMPLETE_ICON_NAME,
                                                   new ActionAdapter ()
                    {
                        
                        public void actionPerformed (ActionEvent ev)
                        {
                            
                            try
                            {

                                pv.setChapterEditComplete (c,
                                                           true);
                                
                            } catch (Exception e) {
                                
                                Environment.logError ("Unable to set chapter edit complete: " +
                                                      c,
                                                      e);
                                
                                UIUtils.showErrorMessage (pv,
                                                          "Unable to set {chapter} as edit complete.");
                                
                            }
                            /*
                             *
                            QuollEditorPanel qp = (QuollEditorPanel) pv.getEditorForChapter (c);
                            
                            if (qp == null)
                            {
                                
                                // No panel.
                                return;
                                
                            }
                            
                            qp.setEditComplete (true);
                            */
                        }
                        
                    }));
                
                } else {
                    
                    m.add (UIUtils.createMenuItem ("Set as Edit Needed",
                                                   Constants.EDIT_ICON_NAME,
                                                   new ActionAdapter ()
                    {
                        
                        public void actionPerformed (ActionEvent ev)
                        {
                            
                            try
                            {

                                pv.setChapterEditComplete (c,
                                                           false);
                                
                            } catch (Exception e) {
                                
                                Environment.logError ("Unable to set chapter as not edit complete: " +
                                                      c,
                                                      e);
                                
                                UIUtils.showErrorMessage (pv,
                                                          "Unable to set {chapter} as not edit complete.");
                                
                            }

                        }
                        
                    }));
                    
                }
                
                if (c.getEditPosition () > 0)
                {
                    
                    m.add (UIUtils.createMenuItem ("Remove Edit Point",
                                                   Constants.CANCEL_ICON_NAME,
                                                   new ActionAdapter ()
                    {
                        
                        public void actionPerformed (ActionEvent ev)
                        {
                            
                            try
                            {

                                pv.removeChapterEditPosition (c);
                                
                            } catch (Exception e) {
                                
                                Environment.logError ("Unable to remove editor position for chapter: " +
                                                      c,
                                                      e);
                                
                                UIUtils.showErrorMessage (pv,
                                                          "Unable to remove {chapter} edit position.");
                                
                            }
                                                        
                        }
                        
                    }));
                    
                }
                
                m.add (UIUtils.createMenuItem ("Add New {Chapter} Below",
                                               Constants.ADD_ICON_NAME,
                                               new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        AddChapterActionHandler ah = new AddChapterActionHandler (c.getBook (),
                                                                                  c,
                                                                                  (ProjectViewer) pv);


                        DefaultTreeModel dtm = (DefaultTreeModel) _this.tree.getModel ();

                        TreePath ntp = new TreePath (dtm.getPathToRoot (node));

                        int r = _this.tree.getRowForPath (ntp);

                        java.awt.Rectangle rect = _this.tree.getRowBounds (r);

                        Point p = SwingUtilities.convertPoint (_this.tree,
                                                               rect.x,
                                                               rect.y - rect.height + 5,
                                                               _this);

                        ah.setShowPopupAtPoint (p,
                                                "below");

                        ah.actionPerformed (ev);

                    }

                }));

                m.add (UIUtils.createMenuItem ("Rename {Chapter}",
                                               Constants.RENAME_ICON_NAME,
                                               pv.getAction (ProjectViewer.RENAME_CHAPTER_ACTION,
                                                             c)));

                m.add (UIUtils.createMenuItem ("Close {Chapter}",
                                               Constants.CLOSE_ICON_NAME,
                                               new ActionAdapter ()                       
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        pv.closePanel (c);

                    }

                }));

                m.add (UIUtils.createMenuItem ("Delete {Chapter}",
                                               Constants.DELETE_ICON_NAME,
                                               pv.getAction (ProjectViewer.DELETE_CHAPTER_ACTION,
                                                             c)));

            }

            if (d instanceof OutlineItem)
            {

                final OutlineItem oi = (OutlineItem) d;

                m.add (UIUtils.createMenuItem ("View",
                                               Constants.VIEW_ICON_NAME,
                                               new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        pv.viewObject (oi);

                    }

                }));

                m.add (UIUtils.createMenuItem ("Edit",
                                               Constants.EDIT_ICON_NAME,
                                               pv.getAction (ProjectViewer.EDIT_PLOT_OUTLINE_ITEM_ACTION,
                                                             oi)));

                m.add (UIUtils.createMenuItem ("Delete",
                                               Constants.DELETE_ICON_NAME,
                                               pv.getAction (ProjectViewer.DELETE_PLOT_OUTLINE_ITEM_ACTION,
                                                             oi)));

            }

            if (d instanceof Scene)
            {

                final Scene s = (Scene) d;

                m.add (UIUtils.createMenuItem ("View",
                                               Constants.VIEW_ICON_NAME,
                                               new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        pv.viewObject (s);

                    }

                }));

                m.add (UIUtils.createMenuItem ("Edit",
                                               Constants.EDIT_ICON_NAME,
                                               pv.getAction (ProjectViewer.EDIT_SCENE_ACTION,
                                                             s)));

                m.add (UIUtils.createMenuItem ("Delete",
                                               Constants.DELETE_ICON_NAME,
                                               pv.getAction (ProjectViewer.DELETE_SCENE_ACTION,
                                                             s)));
                
            }

        } else
        {

            m.add (UIUtils.createMenuItem ("Add New {Chapter}",
                                           Constants.ADD_ICON_NAME,
                                           pv.getAction (ProjectViewer.NEW_CHAPTER_ACTION,
                                                         pv.getProject ().getBooks ().get (0))));        

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
        
        return true;
        
    }
    
    public DragActionHandler getTreeDragActionHandler (AbstractProjectViewer pv,
                                                       JTree                 tree)
    {
        
        return new ChapterTreeDragActionHandler (pv,
                                                 tree);
        
    }
    
    public TreePath getChapterTreePathForRow (int r)
    {
        
        return this.tree.getPathForRow (r);
        
    }

    public class ChapterTreeDragActionHandler implements DragActionHandler
    {
    
        private AbstractProjectViewer projectViewer = null;
        private JTree tree = null;
    
        public ChapterTreeDragActionHandler (AbstractProjectViewer pv,
                                             JTree         tree)
        {
    
            this.projectViewer = pv;
            this.tree = tree;
    
        }
    
        public boolean performAction (int         removeRow,
                                      NamedObject removeObject,
                                      int         insertRow,
                                      NamedObject insertObject)
                               throws GeneralException
        {
    
            if ((!(removeObject instanceof Chapter)) ||
                (!(insertObject instanceof Chapter)))
            {
    
                return false;
    
            }
    
            // Get the path for the insert row.
            TreePath insertTp = this.tree.getPathForRow (insertRow);
            
            if (insertTp == null)
            {
                
                return false;
                
            }
    
            Chapter c = (Chapter) removeObject;
    
            Book b = c.getBook ();
    
            Chapter insertC = (Chapter) ((DefaultMutableTreeNode) insertTp.getPathComponent (1)).getUserObject ();
    
            b.moveChapter (c,
                           b.getChapterIndex (insertC) - 1);
    
            try
            {
    
                this.projectViewer.updateChapterIndexes (c.getBook ());
    
            } catch (Exception e)
            {
    
                throw new GeneralException ("Unable to update chapter indexes for book: " +
                                            c.getBook (),
                                            e);
    
            }
    
            this.projectViewer.fireProjectEvent (Chapter.OBJECT_TYPE,
                                                 ProjectEvent.MOVE);
    
            this.projectViewer.reloadTreeForObjectType (Chapter.OBJECT_TYPE);
    
            return true;
    
        }
    
    }
    
}