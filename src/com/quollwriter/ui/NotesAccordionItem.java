package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import java.awt.dnd.*;

import javax.swing.*;
import javax.swing.tree.*;

import java.util.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.renderers.*;

public class NotesAccordionItem extends ProjectObjectsAccordionItem<ProjectViewer>
{

    public NotesAccordionItem (ProjectViewer pv)
    {

        super (Environment.getObjectTypeNamePlural (Note.OBJECT_TYPE),
               Note.OBJECT_TYPE,
               pv);

    }

    @Override
    public String getTitle ()
    {

        return Environment.getObjectTypeNamePlural (Note.OBJECT_TYPE);

    }

    @Override
    public String getId ()
    {

        return Note.OBJECT_TYPE;

    }

    @Override
    public void reloadTree ()
    {

        ((DefaultTreeModel) this.tree.getModel ()).setRoot (UIUtils.createNoteTree (this.viewer));

    }

    @Override
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {

        List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.sidebar);
        prefix.add (LanguageStrings.notes);
        prefix.add (LanguageStrings.headerpopupmenu);
        prefix.add (LanguageStrings.items);

        m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                LanguageStrings._new),
                                       //"Add New Type",
                                       Constants.ADD_ICON_NAME,
                                       this.viewer.getAction (ProjectViewer.NEW_NOTE_TYPE_ACTION)));

        m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                LanguageStrings.manage),
                                       //"Manage Types",
                                       Constants.EDIT_ICON_NAME,
                                       this.viewer.getAction (ProjectViewer.MANAGE_NOTE_TYPES_ACTION)));

    }

    @Override
    public void initTree ()
    {

        ((DefaultTreeModel) this.tree.getModel ()).setRoot (UIUtils.createNoteTree (this.viewer));

    }

    public boolean showItemCountOnHeader ()
    {

        return true;

    }

    public int getItemCount ()
    {

        int c = this.viewer.getProject ().getAllNamedChildObjects (Note.class).size ();

        return c;

    }

    @Override
    public void fillTreePopupMenu (JPopupMenu m,
                                   MouseEvent ev)
    {

        final NotesAccordionItem _this = this;

        List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.sidebar);
        prefix.add (LanguageStrings.notes);
        prefix.add (LanguageStrings.treepopupmenu);
        prefix.add (LanguageStrings.items);

        final TreePath tp = this.tree.getPathForLocation (ev.getX (),
                                                          ev.getY ());

        JMenuItem mi = null;

        if (tp != null)
        {

            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            final NamedObject d = (NamedObject) node.getUserObject ();

            if (d instanceof TreeParentNode)
            {

                if (!d.getName ().equals (Note.EDIT_NEEDED_NOTE_TYPE))
                {

                    m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                            LanguageStrings.rename),
                                                   //"Rename",
                                                   Constants.EDIT_ICON_NAME,
                                                   new ActionAdapter ()
                                                   {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            DefaultTreeModel dtm = (DefaultTreeModel) _this.tree.getModel ();

                                                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                                                            NamedObject nt = (NamedObject) n.getUserObject ();

                                                            new RenameNoteTypeActionHandler (nt.getName (),
                                                                                             _this.viewer).actionPerformed (ev);

                                                        }

                                                   }));

                }

                if (node.getChildCount () == 0)
                {

                    m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                            LanguageStrings.delete),
                                                   //"Delete",
                                                   Constants.DELETE_ICON_NAME,
                                                   new ActionAdapter ()
                                                   {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            DefaultTreeModel dtm = (DefaultTreeModel) _this.tree.getModel ();

                                                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                                                            NamedObject nt = (NamedObject) n.getUserObject ();

                                                            Environment.getUserPropertyHandler (Constants.NOTE_TYPES_PROPERTY_NAME).removeType (nt.getName (),
                                                                                                                                                false);

                                                            dtm.removeNodeFromParent (n);

                                                        }

                                                   }));

                }

            } else
            {

                m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                        LanguageStrings.view),
                                               //"View",
                                               Constants.VIEW_ICON_NAME,
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.viewer.viewObject (d);

                                                    }

                                               }));

                m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                        LanguageStrings.edit),
                                               //"Edit",
                                               Constants.EDIT_ICON_NAME,
                                               _this.viewer.getAction (ProjectViewer.EDIT_NOTE_ACTION,
                                                                       d)));

                m.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                        LanguageStrings.delete),
                                               //"Delete",
                                               Constants.DELETE_ICON_NAME,
                                               _this.viewer.getAction (ProjectViewer.DELETE_NOTE_ACTION,
                                                                       d)));

            }

        }

    }

    @Override
    public TreeCellEditor getTreeCellEditor (ProjectViewer pv)
    {

        return new ProjectTreeCellEditor (pv,
                                          tree);

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

        return false;

    }

    @Override
    public DragActionHandler getTreeDragActionHandler (ProjectViewer pv)
    {

        return null;

    }

}
