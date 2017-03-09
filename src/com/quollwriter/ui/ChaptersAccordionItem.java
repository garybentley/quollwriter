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
               pv);
            
    }
    
    @Override
    public String getId ()
    {
        
        return Chapter.OBJECT_TYPE;
        
    }
    
    @Override
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {

        m.add (UIUtils.createMenuItem ("Add New {Chapter}",
                                       Constants.ADD_ICON_NAME,
                                       this.viewer.getAction (ProjectViewer.NEW_CHAPTER_ACTION,
                                                              this.viewer.getProject ().getBooks ().get (0))));
    
    }
    
    @Override
    public void initTree ()
    {
    
        ((DefaultTreeModel) this.tree.getModel ()).setRoot (UIUtils.createChaptersTree (this.viewer.getProject (),
                                                                                        null,
                                                                                        null,
                                                                                        false));
        
    }

    @Override
    public void reloadTree ()
    {
        
        ((DefaultTreeModel) this.tree.getModel ()).setRoot (UIUtils.createChaptersTree (this.viewer.getProject (),
                                                                                        null,
                                                                                        null,
                                                                                        false));        
        
    }    
    
    public boolean showItemCountOnHeader ()
    {
        
        return true;
        
    }
    
    public int getItemCount ()
    {
        
        int c = this.viewer.getProject ().getAllNamedChildObjects (Chapter.class).size ();
        
        return c;
                
    }

    @Override
    public void fillTreePopupMenu (final JPopupMenu m,
                                   final MouseEvent ev)
    {

        final TreePath tp = this.tree.getPathForLocation (ev.getX (),
                                                          ev.getY ());
        
        final ChaptersAccordionItem _this = this;

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
                                
                                                        _this.viewer.viewObject (n);
                                
                                                    }
                                
                                               }));

                m.add (UIUtils.createMenuItem ("Edit",
                                               Constants.EDIT_ICON_NAME,
                                               _this.viewer.getAction (ProjectViewer.EDIT_NOTE_ACTION,
                                                                       n)));

                m.add (UIUtils.createTagsMenu (n,
                                               _this.viewer));
                                                                       
                m.add (UIUtils.createMenuItem ("Delete",
                                               Constants.DELETE_ICON_NAME,
                                               _this.viewer.getAction (ProjectViewer.DELETE_NOTE_ACTION,
                                                                       n)));
                                
            }
            
            if (d instanceof Chapter)
            {

                final Chapter c = (Chapter) d;

                final String chapterObjTypeName = Environment.getObjectTypeName (c);

                m.add (UIUtils.createMenuItem ("Edit {Chapter}",
                                               Constants.EDIT_ICON_NAME,
                                               _this.viewer.getAction (ProjectViewer.EDIT_CHAPTER_ACTION,
                                                                       c)));

                m.add (UIUtils.createMenuItem ("View {Chapter} Information",
                                               Constants.INFO_ICON_NAME,
                                               _this.viewer.getAction (ProjectViewer.VIEW_CHAPTER_INFO_ACTION,
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

                                _this.viewer.setChapterEditComplete (c,
                                                                     true);
                                
                            } catch (Exception e) {
                                
                                Environment.logError ("Unable to set chapter edit complete: " +
                                                      c,
                                                      e);
                                
                                UIUtils.showErrorMessage (_this.viewer,
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

                                _this.viewer.setChapterEditComplete (c,
                                                                     false);
                                
                            } catch (Exception e) {
                                
                                Environment.logError ("Unable to set chapter as not edit complete: " +
                                                      c,
                                                      e);
                                
                                UIUtils.showErrorMessage (_this.viewer,
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

                                _this.viewer.removeChapterEditPosition (c);
                                
                            } catch (Exception e) {
                                
                                Environment.logError ("Unable to remove editor position for chapter: " +
                                                      c,
                                                      e);
                                
                                UIUtils.showErrorMessage (_this.viewer,
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
                                                                                  (ProjectViewer) _this.viewer);

                        DefaultTreeModel dtm = (DefaultTreeModel) _this.tree.getModel ();

                        TreePath ntp = new TreePath (dtm.getPathToRoot (node));

                        int r = _this.tree.getRowForPath (ntp);

                        java.awt.Rectangle rect = _this.tree.getRowBounds (r);

                        Point p = SwingUtilities.convertPoint (_this.tree,
                                                               rect.x,
                                                               rect.y - rect.height + 5,
                                                               _this);

                        ah.showPopup (p);

                    }

                }));

                m.add (UIUtils.createMenuItem ("Rename {Chapter}",
                                               Constants.RENAME_ICON_NAME,
                                               _this.viewer.getAction (ProjectViewer.RENAME_CHAPTER_ACTION,
                                                                       c)));

                m.add (UIUtils.createMenuItem ("Close {Chapter}",
                                               Constants.CLOSE_ICON_NAME,
                                               new ActionAdapter ()                       
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.viewer.closePanel (c);

                    }

                }));

                m.add (UIUtils.createTagsMenu (c,
                                               _this.viewer));
                
                m.add (UIUtils.createMenuItem ("Delete {Chapter}",
                                               Constants.DELETE_ICON_NAME,
                                               _this.viewer.getAction (ProjectViewer.DELETE_CHAPTER_ACTION,
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

                        _this.viewer.viewObject (oi);

                    }

                }));

                m.add (UIUtils.createMenuItem ("Edit",
                                               Constants.EDIT_ICON_NAME,
                                               _this.viewer.getAction (ProjectViewer.EDIT_PLOT_OUTLINE_ITEM_ACTION,
                                                                       oi)));

                m.add (UIUtils.createTagsMenu (oi,
                                               _this.viewer));

                m.add (UIUtils.createMenuItem ("Delete",
                                               Constants.DELETE_ICON_NAME,
                                               _this.viewer.getAction (ProjectViewer.DELETE_PLOT_OUTLINE_ITEM_ACTION,
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

                        _this.viewer.viewObject (s);

                    }

                }));

                m.add (UIUtils.createMenuItem ("Edit",
                                               Constants.EDIT_ICON_NAME,
                                               _this.viewer.getAction (ProjectViewer.EDIT_SCENE_ACTION,
                                                                       s)));

                m.add (UIUtils.createTagsMenu (s,
                                               _this.viewer));                
                                                                       
                m.add (UIUtils.createMenuItem ("Delete",
                                               Constants.DELETE_ICON_NAME,
                                               _this.viewer.getAction (ProjectViewer.DELETE_SCENE_ACTION,
                                                                       s)));
                                
            }

        } else
        {

            m.add (UIUtils.createMenuItem ("Add New {Chapter}",
                                           Constants.ADD_ICON_NAME,
                                           _this.viewer.getAction (ProjectViewer.NEW_CHAPTER_ACTION,
                                                                   _this.viewer.getProject ().getBooks ().get (0))));        

        }

    }
        
    @Override
    public TreeCellEditor getTreeCellEditor (AbstractProjectViewer pv)
    {
        
        return new ProjectTreeCellEditor (pv,
                                          this.tree);
        
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
    
    @Override
    public boolean isTreeEditable ()
    {
        
        return false;
        
    }
    
    @Override
    public boolean isDragEnabled ()
    {
        
        return true;
        
    }
    
    @Override
    public DragActionHandler getTreeDragActionHandler (AbstractProjectViewer pv)
    {
        
        return new ChapterTreeDragActionHandler (pv,
                                                 this.tree);
        
    }
    
    public TreePath getChapterTreePathForRow (int r)
    {
        
        return this.tree.getPathForRow (r);
        
    }

    public class ChapterTreeDragActionHandler implements DragActionHandler<Chapter>
    {
    
        private AbstractProjectViewer projectViewer = null;
        private JTree tree = null;
    
        public ChapterTreeDragActionHandler (AbstractProjectViewer pv,
                                             JTree         tree)
        {
    
            this.projectViewer = pv;
            this.tree = tree;
    
        }
    
        @Override
        public boolean canImportForeignObject (NamedObject obj)
        {
            
            return false;
            
        }
        
        @Override
        public boolean importForeignObject (NamedObject obj,
                                            int         insertRow)
        {
            
            return true;
            
        }
        
        @Override
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
    
            Chapter ic = (Chapter) insertObject;
    
            Book bb = ic.getBook ();
        
            bb.moveChapter (ic,
                           insertRow);
    
            try
            {
    
                this.projectViewer.updateChapterIndexes (bb);
    
            } catch (Exception e)
            {
    
                throw new GeneralException ("Unable to update chapter indexes for book: " +
                                            bb,
                                            e);
    
            }
    
            this.projectViewer.fireProjectEvent (Chapter.OBJECT_TYPE,
                                                 ProjectEvent.MOVE);
    
            this.projectViewer.reloadTreeForObjectType (Chapter.OBJECT_TYPE);
                
            return true;
    
        }
    
        @Override
        public boolean handleMove (int     fromRow,
                                   int     toRow,
                                   Chapter chapter)
                            throws GeneralException
        {
            
            Book b = chapter.getBook ();
        
            b.moveChapter (chapter,
                           toRow);
    
            try
            {
    
                this.projectViewer.updateChapterIndexes (b);
    
            } catch (Exception e)
            {
    
                throw new GeneralException ("Unable to update chapter indexes for book: " +
                                            b,
                                            e);
    
            }
    
            this.projectViewer.fireProjectEvent (Chapter.OBJECT_TYPE,
                                                 ProjectEvent.MOVE);
    
            this.projectViewer.reloadTreeForObjectType (Chapter.OBJECT_TYPE);
                
            return true;
    
        }

    }
    
}