package com.quollwriter.editors.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import java.awt.dnd.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.tree.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.editors.ui.panels.*;

public class EditorChaptersAccordionItem extends ChaptersAccordionItem
{
        
    public EditorChaptersAccordionItem (AbstractProjectViewer pv)
    {
        
        super (pv);
            
    }
        
    @Override
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {
    
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

        ((DefaultTreeModel) tree.getModel ()).setRoot (EditorsUIUtils.createTree (this.projectViewer.getProject (),
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
    
    public void init (JTree tree)
    {

        ((DefaultTreeModel) tree.getModel ()).setRoot (EditorsUIUtils.createTree (this.projectViewer.getProject (),
                                                                                  null,
                                                                                  null,
                                                                                  false));
    }

    @Override
    public void fillTreePopupMenu (JPopupMenu m,
                                   MouseEvent ev)
    {

        final EditorChaptersAccordionItem _this = this;

        final AbstractProjectViewer pv = this.projectViewer;

        final TreePath tp = this.tree.getPathForLocation (ev.getX (),
                                                          ev.getY ());
        
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
                                               pv.getAction (EditorProjectViewer.EDIT_COMMENT_ACTION,
                                                             n)));

                m.add (UIUtils.createMenuItem ("Delete",
                                               Constants.DELETE_ICON_NAME,
                                               pv.getAction (EditorProjectViewer.DELETE_COMMENT_ACTION,
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
/*
                m.add (UIUtils.createMenuItem ("View {Chapter} Information",
                                               Constants.INFO_ICON_NAME,
                                               pv.getAction (ProjectViewer.VIEW_CHAPTER_INFO_ACTION,
                                                             c)));
*/
                if (!c.isEditComplete ())
                {                                                             
                
                    m.add (UIUtils.createMenuItem ("Set as Edit Complete",
                                                   Constants.EDIT_COMPLETE_ICON_NAME,
                                                   new ActionAdapter ()
                    {
                        
                        public void actionPerformed (ActionEvent ev)
                        {
                            
                            EditorChapterPanel qp = (EditorChapterPanel) pv.getEditorForChapter (c);
                            
                            qp.setEditComplete (true);
                            
                        }
                        
                    }));
                
                } else {
                    
                    m.add (UIUtils.createMenuItem ("Set as Edit Needed",
                                                   Constants.EDIT_ICON_NAME,
                                                   new ActionAdapter ()
                    {
                        
                        public void actionPerformed (ActionEvent ev)
                        {
                            
                            EditorChapterPanel qp = (EditorChapterPanel) pv.getEditorForChapter (c);
                            
                            qp.setEditComplete (false);
                            
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
                            
                            EditorChapterPanel qp = (EditorChapterPanel) pv.getEditorForChapter (c);
                            
                            qp.removeEditPosition ();
                            
                        }
                        
                    }));
                    
                }
                
                m.add (UIUtils.createMenuItem ("Close {Chapter}",
                                               Constants.CLOSE_ICON_NAME,
                                               new ActionAdapter ()                       
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        pv.closePanel (c);

                    }

                }));

            }

        } 
        
    }
            
    public boolean isTreeEditable ()
    {
        
        return false;
        
    }
    
    public boolean isDragEnabled ()
    {
        
        return false;
        
    }
            
}