package com.quollwriter.ui.fx.components;

import java.util.*;
import javafx.util.*;

import javafx.scene.control.*;
import javafx.scene.control.cell.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;

public class ProjectTreeCellFactory implements Callback<TreeView<NamedObject>, TreeCell<NamedObject>>
{

    public static boolean showEditPositionIcon ()
    {

        return UserProperties.getAsBoolean (Constants.SHOW_EDIT_POSITION_ICON_IN_CHAPTER_LIST_PROPERTY_NAME);

    }

    public static boolean showEditCompleteIcon ()
    {

        return UserProperties.getAsBoolean (Constants.SHOW_EDIT_COMPLETE_ICON_IN_CHAPTER_LIST_PROPERTY_NAME);

    }

    public static String getStyle (NamedObject o)
    {

        String ot = null;

        if (o == null)
        {

            return null;

        }

        if (o instanceof Note)
        {

            Note n = (Note) o;

            if (o instanceof TreeParentNode)
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

        if ((o instanceof OutlineItem)
            ||
            (o instanceof Scene)
           )
        {

            ot = o.getObjectType ();

            return ot;

        }

        if (o instanceof Chapter)
        {

            Chapter c = (Chapter) o;

            if ((ProjectTreeCellFactory.showEditPositionIcon ())
                &&
                (c.getEditPosition () > 0)
               )
            {

                ot = Constants.EDIT_IN_PROGRESS_ICON_NAME;

            }

            if ((ProjectTreeCellFactory.showEditCompleteIcon ())
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

            return ot;

        }

        if (o instanceof TreeParentNode)
        {

            TreeParentNode tpn = (TreeParentNode) o;

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

            return ot;

        }

        if (!(o instanceof BlankNamedObject))
        {

            return o.getObjectType ();

        }

        return ot;

    }

    public void initCell (NamedObject           n,
                          TreeCell<NamedObject> cell)
    {

    }

    public TreeCell<NamedObject> createCell ()
    {

        final ProjectTreeCellFactory _this = this;

        TreeCell<NamedObject> c = new TreeCell<> ()
        {

            @Override
            protected void updateItem (NamedObject n,
                                       boolean     empty)
            {

                super.updateItem (n,
                                  empty);

                if ((empty)
                    ||
                    (n == null)
                   )
                {

                    // Remove styles?
                    this.textProperty ().unbind ();
                    this.getStyleClass ().remove (_this.getStyle (n));
                    //c.getStyleClass ().add (this.getStyle (value));
                    return;

                }

                this.textProperty ().bind (n.nameProperty ());
                this.getStyleClass ().add (_this.getStyle (n));
                _this.initCell (n,
                                this);

            }

        };

        return c;

    }

    @Override
    public TreeCell<NamedObject> call (TreeView<NamedObject> v)
    {

        TreeCell<NamedObject> c = this.createCell ();

        return c;
/*
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
*/

    }

}
