package com.quollwriter.ui.renderers;

import java.awt.*;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;


public class ProjectTreeCellRenderer extends DefaultTreeCellRenderer
{

    private Map icons = new HashMap ();
    private boolean showObjectIcons = false;

    public ProjectTreeCellRenderer (boolean showObjectIcons)
    {

        this.showObjectIcons = showObjectIcons;

    }

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

        this.setBorder (new EmptyBorder (2, 2, 2, 2));

        this.setOpaque (false);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        value = node.getUserObject ();

        if (value instanceof DataObject)
        {

            DataObject d = (DataObject) value;

            DefaultMutableTreeNode par = (DefaultMutableTreeNode) node.getParent ();

            Icon ic = this.getIcon (d,
                                    par);

            this.setIcon (ic);

        } else {

            this.setIcon (null);

        }

        if (value instanceof NamedObject)
        {

            String n = ((NamedObject) value).getName ();

            if (n.equals (Note.EDIT_NEEDED_NOTE_TYPE))
            {

                n = Environment.getUIString (LanguageStrings.notetypes,
                                             LanguageStrings.editneededtype);

            }

            if (value instanceof TreeParentNode)
            {

                TreeParentNode t = (TreeParentNode) value;

                if (t.getCount () > -1)
                {

                    n += " (" + t.getCount () + ")";

                }

            }

            this.setText (n);

        }

        this.setToolTipText (Environment.getUIString (LanguageStrings.actions,
                                                      LanguageStrings.clicktoview));
        //"Click to view");
        this.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

        return this;

    }

    public Icon getIcon (DataObject             d,
                         DefaultMutableTreeNode par)
    {

        if ((d instanceof Asset)
            &&
            (this.showObjectIcons)
           )
        {

            return ((Asset) d).getUserConfigurableObjectType ().getIcon16x16 ();

        }

        String ot = this.getIconType (d,
                                      par);

        Icon ic = null;

        if (ot != null)
        {

            ic = (Icon) this.icons.get (ot);

            if (ic == null)
            {

                ic = Environment.getIcon (ot,
                                          Constants.ICON_TREE);

                this.icons.put (ot,
                                ic);

            }

        }

        return ic;

    }

    public String getIconType (DataObject             d,
                               DefaultMutableTreeNode par)
    {

        String ot = null;

        if (d instanceof Note)
        {

            Note n = (Note) d;

            //DefaultMutableTreeNode par = (DefaultMutableTreeNode) node.getParent ();

            if (par.getUserObject () instanceof TreeParentNode)
            {

                if (n.isEditNeeded ())
                {

                    ot = Constants.EDIT_NEEDED_NOTE_ICON_NAME;

                } else {

                    ot = Constants.BULLET_BLACK_ICON_NAME;//n.getObjectType ();

                }

            } else {

                ot = Constants.BULLET_BLACK_ICON_NAME;

            }

            if (n.isDealtWith ())
            {

                ot = Constants.SET_DEALT_WITH_ICON_NAME;

            }

            return ot;

        }

        if ((d instanceof OutlineItem)
            ||
            (d instanceof Scene)
           )
        {

            ot = d.getObjectType ();

            return ot;

        }

        if (d instanceof Chapter)
        {

            Chapter c = (Chapter) d;

            if ((this.showEditPositionIcon ())
                &&
                (c.getEditPosition () > 0)
               )
            {

                ot = Constants.EDIT_IN_PROGRESS_ICON_NAME;

            }

            if ((this.showEditCompleteIcon ())
                &&
                (c.isEditComplete ())
               )
            {

                ot = Constants.EDIT_COMPLETE_ICON_NAME;

            }

            boolean allDealtWith = true;

            Set<Note> notes = c.getNotes ();

            for (Note n : notes)
            {

                if (!n.isDealtWith ())
                {

                    allDealtWith = false;

                    break;

                }

            }

            if (notes.size () == 0)
            {

                allDealtWith = false;

            }

            if (allDealtWith)
            {

                ot = Constants.SET_DEALT_WITH_ICON_NAME;

            }

        }

        if (d instanceof TreeParentNode)
        {

            TreeParentNode tpn = (TreeParentNode) d;

            ot = tpn.getForObjectType ();

            if (ot != null)
            {

                if ((ot.equals (Note.OBJECT_TYPE)) &&
                    (Note.EDIT_NEEDED_NOTE_TYPE.equals (tpn.getName ())))
                {

                    ot = "edit-needed-note";

                } else
                {

                    if (ot.equals (QCharacter.OBJECT_TYPE))
                    {

                        ot = ot + "-multi";

                    }

                }

            }

        }

        if ((this.showObjectIcons)
            &&
            (!(d instanceof BlankNamedObject))
           )
        {

            return d.getObjectType ();

        }

        return ot;

    }

    protected boolean showEditPositionIcon ()
    {

        return UserProperties.getAsBoolean (Constants.SHOW_EDIT_POSITION_ICON_IN_CHAPTER_LIST_PROPERTY_NAME);

    }

    protected boolean showEditCompleteIcon ()
    {

        return UserProperties.getAsBoolean (Constants.SHOW_EDIT_COMPLETE_ICON_IN_CHAPTER_LIST_PROPERTY_NAME);

    }

}
