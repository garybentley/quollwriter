package com.quollwriter.ui.fx.userobjects;

import java.util.*;
import java.util.stream.*;

import javafx.collections.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.input.*;
import javafx.event.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class SortableColumnsPanel extends SplitPane implements Stateful
{

    private UserConfigurableObject object = null;
    private ProjectViewer          viewer = null;
    private LayoutColumn           dragColumn = null;
    private FieldBox               dragItem = null;
    private EventHandler<MouseEvent> handler = null;

    public SortableColumnsPanel (UserConfigurableObject  obj,
                                 IPropertyBinder         binder,
                                 ProjectViewer           viewer)
    {

        this.object = obj;
        this.viewer = viewer;

        this.handler = ev ->
        {

            Node last = null;

            double[] pos = this.getDividerPositions ();

            for (Node n : this.getItems ())
            {

                last = n;

                SplitPane.setResizableWithParent (n, false);
                Region r = (Region) n;

                r.setMinWidth (USE_COMPUTED_SIZE);
                r.applyCss ();
                r.getMinWidth ();

            }

            this.setDividerPositions (pos);

            if (last != null)
            {

                SplitPane.setResizableWithParent (last, true);

            }

            this.removeEventHandler (MouseEvent.MOUSE_MOVED,
                                     this.handler);
            this.requestLayout ();

        };

        this.addEventHandler (MouseEvent.MOUSE_MOVED,
                              handler);

        ObservableList<UserConfigurableObjectType.FieldsColumn> sortableColumns = obj.getUserConfigurableObjectType ().getSortableFieldsColumns ();

        binder.addListChangeListener (sortableColumns,
                                      ch ->
        {

            while (ch.next ())
            {

                if (ch.wasRemoved ())
                {

                    LayoutColumn lc = (LayoutColumn) this.getItems ().get (ch.getFrom ());

                    lc.dispose ();

                    this.getItems ().remove (ch.getFrom ());

                }

                if (ch.wasAdded ())
                {

                    // Add a new column.
                    for (int i = ch.getFrom (); i < ch.getTo (); i++)
                    {

                        this.addColumn (sortableColumns.get (i),
                                        i);

                    }

                }

                this.requestLayout ();

            }

        });

        for (UserConfigurableObjectType.FieldsColumn col : sortableColumns)
        {

            this.addColumn (col,
                            -1);

        }

        if (sortableColumns.size () == 0)
        {

            UserConfigurableObjectType.FieldsColumn col = obj.getUserConfigurableObjectType ().addNewColumn (null);

            this.addColumn (col,
                            -1);

        }

    }

    public void showView ()
    {

        this.getLayoutColumns ().stream ()
            .forEach (v -> v.showView ());

    }

    public void showEdit ()
    {

        this.getLayoutColumns ().stream ()
            .forEach (v -> v.showEdit ());

    }

    public List<LayoutColumn> getLayoutColumns ()
    {

        return this.getItems ().stream ()
            .map (b ->
            {

                if (b instanceof LayoutColumn)
                {

                    LayoutColumn lc = (LayoutColumn) b;

                    return lc;

                } else {

                    return null;

                }

            })
            .filter (b -> b != null)
            .collect (Collectors.toList ());

    }

    public boolean areFieldsBeingEdited ()
    {

        return this.getLayoutColumns ().stream ()
            .map (b ->
            {

                return b.areFieldsBeingEdited ();

            })
            .filter (b -> b)
            .findFirst ()
            .orElse (false);

    }

    public boolean updateFields ()
    {

        boolean noErrors = true;

        for (LayoutColumn lc : this.getLayoutColumns ())
        {

            if (!lc.updateFields ())
            {

                noErrors = false;

            }

        }

        return noErrors;

    }

    @Override
    public State getState ()
    {

        State s = new State ();

        s.set ("widths",
               this.getItems ().stream ()
                .map (n -> ((Region) n).getWidth ())
                .collect (Collectors.toList ()));

        return s;

    }

    @Override
    public void init (State s)
    {

        if (s == null)
        {

            return;

        }
        Runnable r = () ->
        {

            List<Number> widths = s.getAsList ("widths",
                                               Number.class);

            if (widths != null)
            {

                for (int i = 0; i < widths.size (); i++)
                {

                    if (i >= this.getItems ().size ())
                    {

                        continue;

                    }

                    ((Region) this.getItems ().get (i)).setMinWidth (widths.get (i).doubleValue () - 1);
                    ((Region) this.getItems ().get (i)).setPrefWidth (widths.get (i).doubleValue () - 1);

                }

            }

        };

//        UIUtils.runLater (r);

        List<Number> widths = s.getAsList ("widths",
                                           Number.class);

        if (widths != null)
        {

            for (int i = 0; i < widths.size (); i++)
            {

                if (i >= this.getItems ().size ())
                {

                    continue;

                }

                ((Region) this.getItems ().get (i)).setMinWidth (widths.get (i).doubleValue () - 1);
                ((Region) this.getItems ().get (i)).setPrefWidth (widths.get (i).doubleValue () - 1);

            }

        }

        if (this.getScene () != null)
        {

            if (this.viewer.getViewer ().isShowing ())
            {

                UIUtils.forceRunLater (r);

            } else {

                this.viewer.getViewer ().showingProperty ().addListener ((xpr, xoldv, xnewv) ->
                {

                    UIUtils.forceRunLater (r);

                });

            }

            return;

        }


        this.sceneProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv == null)
            {
                return;

            }

            if (this.viewer.getViewer ().isShowing ())
            {

                UIUtils.forceRunLater (r);

            } else {

                this.viewer.getViewer ().showingProperty ().addListener ((xpr, xoldv, xnewv) ->
                {

                    UIUtils.forceRunLater (r);

                });

            }

        });

    }

    public void showDeleteColumn (UserConfigurableObjectType.FieldsColumn col)
    {

        if (col.fields ().size () == 0)
        {

            this.object.getUserConfigurableObjectType ().getSortableFieldsColumns ().remove (col);
            return;

        }

        String pid = this.object.getObjectReference ().asString () + "deletecolumn";

        if (this.viewer.getPopupById (pid) != null)
        {

            return;

        }

        QuollPopup.messageBuilder ()
            .withViewer (this.viewer)
            .popupId (pid)
            .styleClassName (StyleClassNames.DELETE)
            .title (assets,view,column,delete,popup,title)
            .message (assets,view,column,delete,popup,text)
            .button (QuollButton.builder ()
                        .label (assets,view,column,delete,popup,buttons,confirm)
                        .onAction (ev ->
                        {

                            this.viewer.getPopupById (pid).close ();

                        })
                        .build ())
            .build ();

    }

    public void showAddNewLayoutColumn (LayoutColumn addAfter,
                                        Runnable     onColumnAdded)
    {

        QuollPopup.textEntryBuilder ()
         .withViewer (this.viewer)
         .headerIconClassName (StyleClassNames.ADD)
         .title (assets,view,column,title,edit,popup,title)
         .description (assets,view,column,title,edit,popup,text)
         .confirmButtonLabel (assets,view,column,title,edit,popup,buttons,confirm)
         .cancelButtonLabel (assets,view,column,title,edit,popup,buttons,cancel)
         .validator (s ->
         {

             return null;

         })
         .onConfirm (fev ->
         {

             // TODO Ugh this is so yucky...
             String t = ((TextField) fev.getForm ().lookup ("#text")).getText ().trim ();

             UserConfigurableObjectType.FieldsColumn col = new UserConfigurableObjectType.FieldsColumn (t);

             List<UserConfigurableObjectType.FieldsColumn> cols = this.object.getUserConfigurableObjectType ().getSortableFieldsColumns ();

             int ind = -1;

             if (addAfter != null)
             {

                 ind = cols.indexOf (addAfter.getFieldsColumn ()) + 1;

             }

             cols.add (ind > -1 ? ind : cols.size (),
                       col);

             UIUtils.runLater (onColumnAdded);

         })
         .build ();

    }

    public LayoutColumn addColumn (UserConfigurableObjectType.FieldsColumn col,
                                   int                                     addAt)
    {

        LayoutColumn lc = new LayoutColumn (col,
                                            this.object,
                                            this,
                                            this.viewer);

        //lc.prefHeightProperty ().bind (this.heightProperty ());

        this.getItems ().add ((addAt == -1 ? this.getItems ().size () : addAt),
                              lc);

        SplitPane.setResizableWithParent (lc, false);

        Node last = null;

        for (Node n : this.getItems ())
        {

            SplitPane.setResizableWithParent (n, false);

            last = n;

        }

        SplitPane.setResizableWithParent (last, true);

        lc.getTitle ().setOnDragDetected (ev ->
        {
/*
            if (this.areFieldsBeingEdited ())
            {

                ev.consume ();
                return;

            }
*/
            this.dragColumn = lc;

            Dragboard db = lc.startDragAndDrop (TransferMode.MOVE);

            ClipboardContent c = new ClipboardContent ();
            c.put (DataFormat.PLAIN_TEXT,
                   "dragging-column");

            db.setContent (c);
            db.setDragView (UIUtils.getImageOfNode (lc));
            lc.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, true);
            ev.consume ();

        });

        lc.setOnDragExited (ev ->
        {

            lc.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

        });

        lc.setOnDragDropped (ev ->
        {

            lc.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            LayoutColumn dlc = this.dragColumn;

            if (dlc != null)
            {

                dlc.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);

                int ind2 = this.getItems ().indexOf (lc);

                double[] pos = this.getDividerPositions ();

                this.object.getUserConfigurableObjectType ().getSortableFieldsColumns ().remove (dlc.getFieldsColumn ());
                this.object.getUserConfigurableObjectType ().getSortableFieldsColumns ().add (ind2, dlc.getFieldsColumn ());
                //this.layoutSplitPane.getItems ().remove (dlc);
                //this.layoutSplitPane.getItems ().add (ind2, dlc);

                this.setDividerPositions (pos);

                this.dragColumn = null;
                this.requestLayout ();

            } else {

                FieldBox dvb = this.getDragItem ();

                if (dvb == null)
                {

                    return;

                }

                dvb.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);

                UserConfigurableObjectType.FieldsColumn tp = dvb.getLayoutColumn ().getFieldsColumn ();

                tp.fields ().remove (dvb.getTypeField ());
                lc.getFieldsColumn ().fields ().add (dvb.getTypeField ());
                this.setDragItem (null);
                this.requestLayout ();

            }

            ev.setDropCompleted (true);
            ev.consume ();

        });

        lc.setOnDragOver (ev ->
        {

            lc.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            Dragboard db = ev.getDragboard ();

            Object o = db.getContent (DataFormat.PLAIN_TEXT);

            if ((o != null)
                &&
                (o.toString ().equals ("dragging"))
               )
            {

                if (lc.getFieldBoxAt (ev.getX (),
                                      ev.getY ()) == null)
                {

                    lc.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

                }

                ev.acceptTransferModes (TransferMode.MOVE);

                return;

            }

            if ((o != null)
                &&
                (o.toString ().equals ("dragging-column"))
               )
            {

                ev.acceptTransferModes (TransferMode.MOVE);
                lc.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

                return;

            }

        });

        return lc;

    }

    public void setDragItem (FieldBox fb)
    {

        this.dragItem = fb;

    }

    public FieldBox getDragItem ()
    {

        return this.dragItem;

    }

}
