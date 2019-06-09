package com.quollwriter.editors.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import java.awt.dnd.*;

import java.util.Date;
import java.util.Set;
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

// TODO: Make abstract ProjectSentReceivedChaptersAccordionItem, maybe move into ProjectSentReceivedSideBar.
public class ProjectCommentsChaptersAccordionItem extends ProjectObjectsAccordionItem<ProjectSentReceivedViewer>
{

    public ProjectCommentsChaptersAccordionItem (ProjectSentReceivedViewer pv)
    {

        super ("",
               Constants.COMMENT_ICON_NAME,
               pv);

    }

    @Override
    public String getId ()
    {

        return null;

    }

    @Override
    public DragActionHandler getTreeDragActionHandler (ProjectSentReceivedViewer pv)
    {

        return null;

    }

    protected void handleViewObject (TreePath tp,
                                     Object   obj)
    {

        if (obj instanceof Chapter)
        {

            Chapter c = (Chapter) obj;

            if (c.getNotes ().size () > 0)
            {

                Note n = c.getNotes ().iterator ().next ();

                this.viewer.viewObject (n);

                this.tree.expandPath (UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) this.tree.getModel ().getRoot (),
                                                                        c));

                return;

            }

        }

        this.viewer.viewObject ((DataObject) obj);

    }

    @Override
    public void reloadTree ()
    {

        ((DefaultTreeModel) tree.getModel ()).setRoot (EditorsUIUtils.createTree (this.viewer.getProject (),
                                                                                  null,
                                                                                  null,
                                                                                  false));

    }

    @Override
    public void initTree ()
    {

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) EditorsUIUtils.createTree (this.viewer.getProject (),
                                                                                          null,
                                                                                          null,
                                                                                          false);

        ((DefaultTreeModel) this.tree.getModel ()).setRoot (root);

        this.tree.expandPath (UIUtils.getTreePathForUserObject (root,
                                                                this.viewer.getProject ().getBook (0).getChapters ().get (0)));

    }

    public boolean showItemCountOnHeader ()
    {

        return true;

    }

    @Override
    public void fillTreePopupMenu (JPopupMenu m,
                                   MouseEvent ev)
    {

        java.util.List<String> prefix = Arrays.asList (editors,projectcomments,sidebar,comments,treepopupmenu);

        final TreePath tp = this.tree.getPathForLocation (ev.getX (),
                                                          ev.getY ());

        final ProjectCommentsChaptersAccordionItem _this = this;

        final AbstractProjectViewer pv = this.viewer;

        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

        JMenuItem mi = null;

        if (tp != null)
        {

            final DataObject d = (DataObject) node.getUserObject ();

            if (d instanceof Note)
            {

                final Note n = (Note) d;

                // Is this a project we have sent?
                if (!this.viewer.getMessage ().isSentByMe ())
                {

                    m.add (UIUtils.createMenuItem (getUIString (prefix,comment,items,(n.isDealtWith () ? undealtwith : dealtwith)),
                                                    //String.format ("Set %s with",
                                                    //              (n.isDealtWith () ? "undealt" : "dealt")),
                                                   Constants.VIEW_ICON_NAME,
                                                   new ActionAdapter ()
                                                   {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            n.setDealtWith (n.isDealtWith () ? null : new Date ());

                                                            _this.update ();

                                                        }

                                                   }));

                }

                m.add (UIUtils.createMenuItem (getUIString (prefix,comment,items,view),
                                                //"View",
                                               Constants.VIEW_ICON_NAME,
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        pv.viewObject (n);

                                                    }

                                               }));

            }

            if (d instanceof Chapter)
            {

                final Chapter c = (Chapter) d;

                final String chapterObjTypeName = Environment.getObjectTypeName (c).getValue ();

                // Is this a project we have sent?
                if (!this.viewer.getMessage ().isSentByMe ())
                {

                    int nc = 0;

                    final Set<Note> notes = c.getNotes ();

                    for (Note n : notes)
                    {

                        if (n.isDealtWith ())
                        {

                            nc++;

                        }

                    }

                    if (nc > 0)
                    {

                        m.add (UIUtils.createMenuItem (getUIString (prefix,Chapter.OBJECT_TYPE,items,undealtwith),
                                                        //"Set all undealt with",
                                                       Constants.SET_UNDEALT_WITH_ICON_NAME,
                                                       new ActionListener ()
                                                       {

                                                            @Override
                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                for (Note n : notes)
                                                                {

                                                                    n.setDealtWith (null);

                                                                }

                                                                _this.update ();

                                                            }

                                                       }));

                    }

                    if (nc != notes.size ())
                    {

                        m.add (UIUtils.createMenuItem (getUIString (prefix,Chapter.OBJECT_TYPE,items,dealtwith),
                                                        //"Set all dealt with",
                                                       Constants.SET_DEALT_WITH_ICON_NAME,
                                                       new ActionListener ()
                                                       {

                                                            @Override
                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                Date d = new Date ();

                                                                for (Note n : notes)
                                                                {

                                                                    n.setDealtWith (d);

                                                                }

                                                                _this.update ();

                                                            }

                                                       }));

                    }

                }

                m.add (UIUtils.createMenuItem (getUIString (prefix,Chapter.OBJECT_TYPE,items,view),
                                                //"View {Chapter}",
                                               Constants.EDIT_ICON_NAME,
                                               pv.getAction (ProjectViewer.EDIT_CHAPTER_ACTION,
                                                             c)));

                m.add (UIUtils.createMenuItem (getUIString (prefix,Chapter.OBJECT_TYPE,items,close),
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

    @Override
    public TreeCellEditor getTreeCellEditor (ProjectSentReceivedViewer pv)
    {

        return null;

    }

    @Override
    public int getItemCount ()
    {

        return 0;

    }

    @Override
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

        return false;

    }

}
