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

public class ChaptersAccordionItem extends ProjectObjectsAccordionItem<ProjectViewer>
{
        
    public ChaptersAccordionItem (ProjectViewer pv)
    {
        
        super (Chapter.OBJECT_TYPE,
               pv);
            
    }
    
    public void init (JTree tree)
    {

        this.addHeaderPopupMenuItem ("Add New " + Environment.getObjectTypeName (Chapter.OBJECT_TYPE),
                                     "add",
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

    public void showTreePopupMenu (MouseEvent ev)
    {

        final ChaptersAccordionItem _this = this;

        final AbstractProjectViewer pv = this.projectViewer;
        
        final TreePath tp = this.tree.getPathForLocation (ev.getX (),
                                                          ev.getY ());

        final JPopupMenu m = new JPopupMenu ();

        JMenuItem mi = null;

        if (tp != null)
        {

            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            final DataObject d = (DataObject) node.getUserObject ();

            if (d instanceof Chapter)
            {

                final Chapter c = (Chapter) d;

                final String chapterObjTypeName = Environment.getObjectTypeName (c);

                mi = new JMenuItem ("Edit " + chapterObjTypeName,
                                    Environment.getIcon ("edit",
                                                         Constants.ICON_MENU));
                mi.addActionListener (pv.getAction (ProjectViewer.EDIT_CHAPTER_ACTION,
                                                    c));
                m.add (mi);

                mi = new JMenuItem ("View " + chapterObjTypeName + " Information",
                                    Environment.getIcon ("information",
                                                         Constants.ICON_MENU));
                mi.addActionListener (pv.getAction (ProjectViewer.VIEW_CHAPTER_INFO_ACTION,
                                                    c));
                m.add (mi);

                mi = new JMenuItem ("Add New " + chapterObjTypeName + " Below",
                                    Environment.getIcon (Chapter.OBJECT_TYPE + "-add",
                                                         Constants.ICON_MENU));

                ActionListener h = new ActionAdapter ()
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

                };

                mi.addActionListener (h);

                m.add (mi);

                mi = new JMenuItem ("Rename " + chapterObjTypeName,
                                    Environment.getIcon (Chapter.OBJECT_TYPE + "-edit",
                                                         Constants.ICON_MENU));

                mi.addActionListener (new ActionAdapter ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.tree.setSelectionPath (tp);
                            _this.tree.startEditingAtPath (tp);

                        }

                    });

                m.add (mi);

                mi = new JMenuItem ("Close " + chapterObjTypeName,
                                    Environment.getIcon (Chapter.OBJECT_TYPE + "-delete",
                                                         Constants.ICON_MENU));

                mi.addActionListener (new ActionAdapter ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            pv.closePanel (c);

                        }

                    });

                m.add (mi);

                mi = new JMenuItem ("Delete " + chapterObjTypeName,
                                    Environment.getIcon ("delete",
                                                         Constants.ICON_MENU));
                mi.addActionListener (pv.getAction (ProjectViewer.DELETE_CHAPTER_ACTION,
                                                    c));
                m.add (mi);

            }

            if (d instanceof OutlineItem)
            {

                final OutlineItem oi = (OutlineItem) d;

                mi = new JMenuItem ("View",
                                    Environment.getIcon ("view",
                                                         Constants.ICON_MENU));
                mi.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        pv.viewObject (oi);

                    }

                });

                m.add (mi);

                mi = new JMenuItem ("Edit",
                                    Environment.getIcon ("edit",
                                                         Constants.ICON_MENU));
                mi.addActionListener (pv.getAction (ProjectViewer.EDIT_PLOT_OUTLINE_ITEM_ACTION,
                                                    oi));
                m.add (mi);

                mi = new JMenuItem ("Delete",
                                    Environment.getIcon ("delete",
                                                         Constants.ICON_MENU));
                mi.addActionListener (pv.getAction (ProjectViewer.DELETE_PLOT_OUTLINE_ITEM_ACTION,
                                                    oi));
                m.add (mi);

            }

            if (d instanceof Scene)
            {

                final Scene s = (Scene) d;

                mi = new JMenuItem ("View",
                                    Environment.getIcon ("view",
                                                         Constants.ICON_MENU));
                mi.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        pv.viewObject (s);

                    }

                });

                m.add (mi);

                mi = new JMenuItem ("Edit",
                                    Environment.getIcon ("edit",
                                                         Constants.ICON_MENU));
                mi.addActionListener (pv.getAction (ProjectViewer.EDIT_SCENE_ACTION,
                                                    s));
                m.add (mi);

                mi = new JMenuItem ("Delete",
                                    Environment.getIcon ("delete",
                                                         Constants.ICON_MENU));
                mi.addActionListener (pv.getAction (ProjectViewer.DELETE_SCENE_ACTION,
                                                    s));
                m.add (mi);

            }

        } else
        {

            mi = new JMenuItem ("Add New " + Environment.getObjectTypeName (Chapter.OBJECT_TYPE),
                                Environment.getIcon (Chapter.OBJECT_TYPE + "-add",
                                                     Constants.ICON_MENU));
            mi.addActionListener (pv.getAction (ProjectViewer.NEW_CHAPTER_ACTION,
                                                pv.getProject ().getBooks ().get (0)));
            m.add (mi);

        }

        m.show ((Component) ev.getSource (),
                ev.getX (),
                ev.getY ());
        
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
        
        return true;
        
    }
    
    public DragActionHandler getTreeDragActionHandler (ProjectViewer pv,
                                                       JTree         tree)
    {
        
        return new ChapterTreeDragActionHandler ((ProjectViewer) pv,
                                                 tree);
        
    }
    
    public TreePath getChapterTreePathForRow (int r)
    {
        
        return this.tree.getPathForRow (r);
        
    }

    public class ChapterTreeDragActionHandler implements DragActionHandler
    {
    
        private ProjectViewer projectViewer = null;
        private JTree tree = null;
    
        public ChapterTreeDragActionHandler(ProjectViewer pv,
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
    
            ((ProjectViewer) this.projectViewer).reloadChapterTree ();
    
            return true;
    
        }
    
    }
    
}