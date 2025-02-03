package com.quollwriter.ui.fx.userobjects;

import java.util.*;
import java.util.stream.*;

import javafx.event.*;
import javafx.geometry.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.popups.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class LayoutColumn extends VBox
{

    public static final EventType<Event> EDIT_COLUMN_EVENT = new EventType<> ("layoutcolumn.editcolumn");

    private UserConfigurableObject object = null;
    private IPropertyBinder binder = null;
    private VBox fieldsBox = null;
    private ProjectViewer viewer = null;
    private UserConfigurableObjectType.FieldsColumn fieldsColumn = null;
    private SortableColumnsPanel panel = null;
    private Header title = null;

    public LayoutColumn (UserConfigurableObjectType.FieldsColumn col,
                         UserConfigurableObject                  object,
                         SortableColumnsPanel                    panel,
                         ProjectViewer                           viewer)
    {

        this.object = object;
        this.viewer = viewer;
        this.panel = panel;
        this.binder = new PropertyBinder ();
        this.fieldsBox = new VBox ();
        this.fieldsBox.getStyleClass ().add (StyleClassNames.FIELDS);
        this.fieldsBox.pseudoClassStateChanged (StyleClassNames.HIDELABELS_PSUEDO_CLASS, !col.isShowFieldLabels ());

        VBox.setVgrow (this.fieldsBox,
                       Priority.ALWAYS);

        VBox.setVgrow (this,
                       Priority.ALWAYS);

        this.title = Header.builder ()
           .styleClassName (StyleClassNames.TITLE)
           .title (col.titleProperty ())
           .build ();
        this.title.managedProperty ().bind (this.title.visibleProperty ());
        this.title.setVisible ((col.titleProperty ().getValue () != null && !col.titleProperty ().getValue ().trim ().equals ("")));

        this.binder.addChangeListener (col.showFieldLabelsProperty (),
                                       (pr, oldv, newv) ->
        {

            this.fieldsBox.pseudoClassStateChanged (StyleClassNames.HIDELABELS_PSUEDO_CLASS, !col.isShowFieldLabels ());

        });

        this.binder.addChangeListener (col.titleProperty (),
                                       (pr, oldv, newv) ->
        {

           this.title.setVisible ((newv != null) && (!newv.trim ().equals ("")));

        });

        final LayoutColumn _this = this;

        this.getStyleClass ().add (StyleClassNames.COLUMN);

        this.getChildren ().addAll (this.title, this.fieldsBox);

        this.setOnMousePressed (ev ->
        {

            if (this.getProperties ().get ("context-menu") != null)
            {

                ((ContextMenu) this.getProperties ().get ("context-menu")).hide ();

            }

        });

        this.setOnContextMenuRequested (ev ->
        {

           ContextMenu cm = new ContextMenu ();

           UIUtils.addShowCSSViewerFilter (cm);

           cm.getItems ().add (QuollMenuItem.builder ()
               .iconName (StyleClassNames.EDIT)
               .label (assets,view,column,popupmenu,LanguageStrings.items,editall)
               .onAction (eev ->
               {

                   this.editAllFields ();

               })
               .build ());

           cm.getItems ().add (QuollMenuItem.builder ()
               .iconName (StyleClassNames.ADD)
               .label (assets,view,column,popupmenu,LanguageStrings.items,newfield)
               .onAction (eev ->
               {

                   this.showAddNewField (null);

               })
               .build ());

           cm.getItems ().add (new SeparatorMenuItem ());

           cm.getItems ().add (QuollMenuItem.builder ()
               .iconName (StyleClassNames.EDIT)
               .label (assets,view,column,popupmenu,LanguageStrings.items,edit)
               .onAction (eev ->
               {

                   this.showEditColumnTitle ();

               })
               .build ());

           boolean show = !this.fieldsColumn.isShowFieldLabels ();

           cm.getItems ().add (QuollMenuItem.builder ()
               .iconName (show ? StyleClassNames.SHOW : StyleClassNames.HIDE)
               .label (assets,view,column,popupmenu,LanguageStrings.items,(show ? LanguageStrings.show : hide))
               .onAction (eev ->
               {

                   this.fieldsColumn.setShowFieldLabels (show);

               })
               .build ());

           cm.getItems ().add (QuollMenuItem.builder ()
               .iconName (StyleClassNames.ADD)
               .label (assets,view,column,popupmenu,LanguageStrings.items,addcolumn)
               .onAction (eev ->
               {

                   this.showAddNewLayoutColumn (this,
                                                null);

               })
               .build ());

           if (this.panel.getLayoutColumns ().size () > 1)
           {

               cm.getItems ().add (QuollMenuItem.builder ()
                   .iconName (StyleClassNames.DELETE)
                   .label (assets,view,column,popupmenu,LanguageStrings.items,delete)
                   .onAction (eev ->
                   {

                       this.showDeleteColumn ();

                   })
                   .build ());

           }

           this.getProperties ().put ("context-menu", cm);

           cm.setAutoFix (true);
           cm.setAutoHide (true);
           cm.setHideOnEscape (true);
           cm.show (this,
                   ev.getScreenX (),
                   ev.getScreenY ());

        });

        this.fieldsColumn = col;

        this.binder.addListChangeListener (col.fields (),
                                           ch ->
        {

            while (ch.next ())
            {

                if (ch.wasRemoved ())
                {

                    FieldBox fb = (FieldBox) this.fieldsBox.getChildren ().get (ch.getFrom ());

                    fb.dispose ();

                    this.fieldsBox.getChildren ().remove (ch.getFrom ());

                }

                if (ch.wasAdded ())
                {

                    for (int i = ch.getFrom (); i < ch.getTo (); i++)
                    {

                        try
                        {

                            this.addField (col.fields ().get (i),
                                           i);

                        } catch (Exception e) {

                            Environment.logError ("Unable to add field: " + col.fields ().get (i),
                                                  e);

                            ComponentUtils.showErrorMessage (this.viewer,
                                                             getUILanguageStringProperty (assets,fields,add,actionerror));

                        }

                    }

                }

            }

        });

        for (UserConfigurableObjectTypeField f : col.fields ())
        {

            if (f == null)
            {

                continue;

            }

            try
            {

                this.addField (f,
                               -1);

            } catch (Exception e) {

                Environment.logError ("Unable to add field: " + f,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (assets,fields,add,actionerror));

            }

        }

    }

    public void showDeleteColumn ()
    {

        this.panel.showDeleteColumn (this.fieldsColumn);

    }

    public void editAllFields ()
    {

        this.getFieldBoxes ().stream ()
         .forEach (n -> n.showEditFull ());

        this.fireEvent (new Event (this, this, EDIT_COLUMN_EVENT));

    }

    public Header getTitle ()
    {

        return this.title;

    }

    public void showEdit ()
    {

        this.getFieldBoxes ().stream ()
            .forEach (b -> b.showEditFull ());

    }

    public void showView ()
    {

        for (FieldBox fb : this.getFieldBoxes ())
        {

            try
            {

                fb.showView ();

            } catch (Exception e) {

                Environment.logError ("Unable to view field: " + fb.getTypeField (),
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (assets,fields,view,actionerror));

            }

        }

    }

    public boolean updateFields ()
    {

        boolean v = true;

        for (FieldBox b : this.getFieldBoxes ())
        {

            if (!b.save ())
            {

                v = false;

            }

        }

        return v;

    }

    public boolean areFieldsBeingEdited ()
    {

        return this.getFieldBoxes ().stream ()
            .map (b -> b.isEditing ())
            .findFirst ()
            .orElse (false);

    }

    public void setDragItem (FieldBox fb)
    {

        this.panel.setDragItem (fb);

    }

    public FieldBox getDragItem ()
    {

        return this.panel.getDragItem ();

    }

    public SortableColumnsPanel getPanel ()
    {

        return this.panel;

    }

    public void showAddNewLayoutColumn (LayoutColumn addAfter,
                                        Runnable     onAdd)
    {

        this.panel.showAddNewLayoutColumn (this,
                                           onAdd);

    }

    public void dispose ()
    {

        this.binder.dispose ();

    }

    public UserConfigurableObjectType.FieldsColumn getFieldsColumn ()
    {

        return this.fieldsColumn;

    }

    public List<FieldBox> getFieldBoxes ()
    {

        return this.fieldsBox.getChildren ().stream ()
            .filter (n -> n instanceof FieldBox)
            .map (n -> (FieldBox) n)
            .collect (Collectors.toList ());

    }

    public void showEditColumnTitle ()
    {

        final LayoutColumn _this = this;

        QuollPopup.textEntryBuilder ()
         .withViewer (this.viewer)
         .styleClassName (StyleClassNames.EDIT)
         .title (assets,view,column,LanguageStrings.title,edit,popup,LanguageStrings.title)
         .description (assets,view,column,LanguageStrings.title,edit,popup,text)
         .confirmButtonLabel (assets,view,column,LanguageStrings.title,edit,popup,buttons,confirm)
         .cancelButtonLabel (assets,view,column,LanguageStrings.title,edit,popup,buttons,cancel)
         .text (this.fieldsColumn.getTitle ())
         .validator (s ->
         {

             return null;

         })
         .onConfirm (fev ->
         {

             // TODO Ugh this is so yucky...
             String t = ((TextField) fev.getForm ().lookup ("#text")).getText ().trim ();

             _this.fieldsColumn.setTitle (t);

         })
         .build ();

    }

    public FieldBox addField (UserConfigurableObjectTypeField field,
                              FieldBox                        addBelow)
                       throws GeneralException
    {

        int ind = -1;

        if (addBelow != null)
        {

            ind = this.fieldsBox.getChildren ().indexOf (addBelow) + 1;

        }

        return this.addField (field,
                              ind);

    }

    public FieldBox addField (UserConfigurableObjectTypeField field,
                              int                             addAt)
                       throws GeneralException
    {

        FieldBox vb = new FieldBox (field,
                                    this.object,
                                    this,
                                    this.binder,
                                    this.viewer);
        vb.showView ();

        if (addAt == -1)
        {

            addAt = this.fieldsBox.getChildren ().size ();

        }

        this.fieldsBox.getChildren ().add (addAt,
                                           vb);
        vb.getLabel ().setOnDragDetected (ev ->
        {

            this.setDragItem (vb);

            Dragboard db = vb.startDragAndDrop (TransferMode.MOVE);

            ClipboardContent c = new ClipboardContent ();
            c.put (DataFormat.PLAIN_TEXT,
                   "dragging");

            db.setContent (c);
            db.setDragView (UIUtils.getImageOfNode (vb));
            vb.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, true);
            ev.consume ();

        });

        vb.setOnDragOver (ev ->
        {

            Dragboard db = ev.getDragboard ();

            Object o = db.getContent (DataFormat.PLAIN_TEXT);

            if ((o != null)
                &&
                (o.toString ().equals ("dragging"))
               )
            {

                ev.acceptTransferModes (TransferMode.MOVE);
                vb.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);
                return;

            }

            vb.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

        });

        vb.setOnDragExited (ev ->
        {

            vb.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

        });

        vb.setOnDragDropped (ev ->
        {

            vb.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            FieldBox dvb = this.getDragItem ();

            if (dvb == null)
            {

                return;

            }

            dvb.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);

            LayoutColumn p = vb.getLayoutColumn ();
            LayoutColumn tp = dvb.getLayoutColumn ();

            int ti = p.getFieldsColumn ().fields ().indexOf (vb.getTypeField ());
            tp.getFieldsColumn ().fields ().remove (dvb.getTypeField ());
            p.getFieldsColumn ().fields ().add (ti,
                                                dvb.getTypeField ());

            dvb.setLayoutColumn (p);

            this.setDragItem (null);

            ev.setDropCompleted (true);

            p.reflow ();
            tp.reflow ();

            ev.consume ();
            this.requestLayout ();

        });

        this.reflow ();

        if (this.areFieldsBeingEdited ())
        {

            vb.showEditFull ();

        }

        return vb;

    }

    private void reflow ()
    {

        FieldBox last = null;
/*
        for (FieldBox f : this.getFieldBoxes ())
        {

            VBox.setVgrow (f,
                           Priority.NEVER);
            last = f;

        }

        if (last != null)
        {

            VBox.setVgrow (last,
                           Priority.ALWAYS);

        }

        UIUtils.forceRunLater (() ->
        {

            this.requestLayout ();

        });
*/
    }

    public FieldBox getFieldBoxAt (double x,
                                   double y)
    {

        for (FieldBox fb : this.getFieldBoxes ())
        {

            if (fb.getBoundsInParent ().contains (x, y))
            {

                return fb;

            }

        }

        return null;

    }

    public void showAddNewField (FieldBox addBelow)
    {

        final LayoutColumn _this = this;

        UserConfigurableObjectType type = this.object.getUserConfigurableObjectType ();

        AddUserConfigurableTypeFieldPopup qp = new AddUserConfigurableTypeFieldPopup (this.viewer,
                                                                                      type,
                                                                                      field ->
        {

            // Need to save first so the hashcode gets set correctly.
            try
            {

                this.viewer.saveObject (field,
                                        true);
                //Environment.updateUserConfigurableObjectTypeField (field);

            } catch (Exception e) {

                Environment.logError ("Unable to create user config object type field: " +
                                      field,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (userobjects,LanguageStrings.fields,add,actionerror));
                                          //"Unable to create the field.");

                return;

            }

            FieldBox fb = null;

            if (addBelow != null)
            {

                this.fieldsColumn.fields ().add (this.fieldsColumn.fields ().indexOf (addBelow.getTypeField ()) + 1,
                                                 field);

            } else {

                this.fieldsColumn.fields ().add (field);

            }

            try
            {

                this.viewer.saveObject (type,
                                        true);
                //Environment.updateUserConfigurableObjectTypeField (field);

            } catch (Exception e) {

                Environment.logError ("Unable to update user config object type: " +
                                      type,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (userobjects,LanguageStrings.fields,add,actionerror));
                                          //"Unable to create the field.");

                return;

            }

            UIUtils.runLater (() ->
            {

                this.getFieldBox (field).showEditSingle ();

            });

        });

        if (addBelow != null)
        {

            qp.show (addBelow.getLabel (),
                     Side.BOTTOM);

        } else {

            qp.show ();

        }

    }

    public FieldBox getFieldBox (UserConfigurableObjectTypeField field)
    {

        for (Node n : this.fieldsBox.getChildren ())
        {

            if (n instanceof FieldBox)
            {

                FieldBox b = (FieldBox) n;

                if (b.getTypeField ().equals (field))
                {

                    return b;

                }

            }

        }

        return null;

    }

}
