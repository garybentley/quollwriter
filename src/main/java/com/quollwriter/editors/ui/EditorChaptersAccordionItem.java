package com.quollwriter.editors.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import java.awt.dnd.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Arrays;

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

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

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

    @Override
    public void reloadTree ()
    {

        java.util.List<TreePath> openPaths = new ArrayList ();

        Enumeration<TreePath> paths = this.tree.getExpandedDescendants (new TreePath (this.tree.getModel ().getRoot ()));

        if (paths != null)
        {

            while (paths.hasMoreElements ())
            {

                openPaths.add (paths.nextElement ());

            }

        }

        ((DefaultTreeModel) this.tree.getModel ()).setRoot (EditorsUIUtils.createTree (this.viewer.getProject (),
                                                                                       null,
                                                                                       null,
                                                                                       false));

        DefaultTreeModel dtm = (DefaultTreeModel) this.tree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        for (TreePath p : openPaths)
        {

            this.tree.expandPath (UIUtils.getTreePathForUserObject (root,
                                                                    ((DefaultMutableTreeNode) p.getLastPathComponent ()).getUserObject ()));

        }

    }

    @Override
    public void initTree ()
    {

        ((DefaultTreeModel) this.tree.getModel ()).setRoot (EditorsUIUtils.createTree (this.viewer.getProject (),
                                                                                       null,
                                                                                       null,
                                                                                       false));

        this.tree.setCellRenderer (new ProjectTreeCellRenderer (true)
        {

            public Component getTreeCellRendererComponent (JTree   tree,
                                                           Object  value,
                                                           boolean sel,
                                                           boolean expanded,
                                                           boolean leaf,
                                                           int     row,
                                                           boolean hasFocus)
            {

                Component co= super.getTreeCellRendererComponent (tree,
                                                    value,
                                                    sel,
                                                    expanded,
                                                    leaf,
                                                    row,
                                                    hasFocus);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

                value = node.getUserObject ();

                if (value instanceof Chapter)
                {

                    Chapter c = (Chapter) value;

                    String n = c.getName ();

                    int s = c.getNotes ().size ();

                    if (s > 0)
                    {

                        n += " (" + c.getNotes ().size () + ")";

                    }

                    this.setText (n);

                }

                return this;

            }

            @Override
            public String getIconType (DataObject             d,
                                       DefaultMutableTreeNode par)
            {

                if (d instanceof Note)
                {

                    return Constants.COMMENT_ICON_NAME;

                }

                return super.getIconType (d,
                                          par);

            }

        });

    }

    @Override
    public void fillTreePopupMenu (JPopupMenu m,
                                   MouseEvent ev)
    {

        final EditorChaptersAccordionItem _this = this;

        final AbstractProjectViewer pv = this.viewer;

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

                java.util.List<String> prefix = Arrays.asList (editors,project,sidebar,comments,treepopupmenu,comments,items);

                m.add (UIUtils.createMenuItem (getUIString (prefix,view),
                                            //"View",
                                               Constants.VIEW_ICON_NAME,
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        pv.viewObject (n);

                                                    }

                                               }));

                m.add (UIUtils.createMenuItem (getUIString (prefix,edit),
                                            //"Edit",
                                               Constants.EDIT_ICON_NAME,
                                               pv.getAction (EditorProjectViewer.EDIT_COMMENT_ACTION,
                                                             n)));

                m.add (UIUtils.createMenuItem (getUIString (prefix,delete),
                                                //"Delete",
                                               Constants.DELETE_ICON_NAME,
                                               pv.getAction (EditorProjectViewer.DELETE_COMMENT_ACTION,
                                                             n)));

            }

            if (d instanceof Chapter)
            {

                java.util.List<String> prefix = Arrays.asList (editors,project,sidebar,comments,treepopupmenu,chapters,items);

                final Chapter c = (Chapter) d;

                final String chapterObjTypeName = Environment.getObjectTypeName (c).getValue ();

                m.add (UIUtils.createMenuItem (getUIString (prefix,view),
                                                //"Edit {Chapter}",
                                               Constants.VIEW_ICON_NAME,
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

                    m.add (UIUtils.createMenuItem (getUIString (prefix,seteditcomplete),
                                                    //"Set as Edit Complete",
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

                    m.add (UIUtils.createMenuItem (getUIString (prefix,seteditneeded),
                                                //"Set as Edit Needed",
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

                    m.add (UIUtils.createMenuItem (getUIString (prefix,removeeditposition),
                                                    //"Remove Edit Point",
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

                m.add (UIUtils.createMenuItem (getUIString (prefix,close),
                                                //"Close {Chapter}",
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
