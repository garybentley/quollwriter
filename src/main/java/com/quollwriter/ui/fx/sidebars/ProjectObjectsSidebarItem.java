package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.function.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.input.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.uistrings.UILanguageStringsManager;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

public abstract class ProjectObjectsSidebarItem<E extends AbstractProjectViewer> implements Stateful, IPropertyBinder
{

    protected E viewer = null;
    protected AccordionItem item = null;
    private IPropertyBinder binder = null;

    public ProjectObjectsSidebarItem (E               pv,
                                      IPropertyBinder binder)
    {

        this.viewer = pv;
        this.binder = binder;

    }

    @Override
    public IPropertyBinder getBinder ()
    {

        return this.binder;

    }

    public abstract Node getContent ();

    public abstract String getId ();

    public abstract StringProperty getTitle ();

    public abstract IntegerProperty getItemCount ();

    public abstract Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ();

    public abstract List<String> getStyleClassNames ();

    public abstract BooleanProperty showItemCountOnHeader ();

    public abstract boolean canImport (NamedObject o);

    public abstract void importObject (NamedObject o);

    @Override
    public State getState ()
    {

        if (this.item == null)
        {

            return new State ();

        }

        return this.item.getState ();

    }

    @Override
    public void init (State s)
    {

        this.getAccordionItem ().init (s);

    }

    public AccordionItem getAccordionItem ()
    {

        if (this.item == null)
        {

            String tt = "%1$s (%2$s)";

            StringProperty title = this.getTitle ();
            IntegerProperty itemCount = this.getItemCount ();

            SimpleStringProperty t = new SimpleStringProperty ();
            t.bind (UILanguageStringsManager.createStringBinding (() ->
            {

                return String.format (tt,
                                      title.getValue (),
                                      Environment.formatNumber (itemCount.getValue ()));

            },
            title,
            itemCount,
            UILanguageStringsManager.uilangProperty ()));

            this.item = AccordionItem.builder ()
                .title (t)
                .accordionId (this.getId ())
                .openContent (this.getContent ())
                .styleClassNames (this.getStyleClassNames ())
                .contextMenu (this.getHeaderContextMenuItemSupplier ())
                .build ();

            Header h = this.item.getHeader ();

            h.setOnDragOver (ev ->
            {

                h.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

                Dragboard db = ev.getDragboard ();

                Object o = db.getContent (NamedObjectTree.PROJECT_OBJECT_DATA_FORMAT);

                if (o != null)
                {

                    NamedObject on = (NamedObject) this.viewer.getProject ().getObjectForReference (ObjectReference.parseObjectReference (o.toString ()));

                    if (!this.canImport (on))
                    {

                        return;

                    }

                    h.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

                    ev.acceptTransferModes (TransferMode.MOVE, TransferMode.COPY);

                }

            });

            h.setOnDragExited (ev ->
            {

                h.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            });

            h.setOnDragDone (ev ->
            {

                h.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);
                h.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);

            });

            h.setOnDragDropped (ev ->
            {

                h.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

                Dragboard db = ev.getDragboard ();

                Object o = db.getContent (NamedObjectTree.PROJECT_OBJECT_DATA_FORMAT);

                if (o != null)
                {

                    NamedObject on = (NamedObject) this.viewer.getProject ().getObjectForReference (ObjectReference.parseObjectReference (o.toString ()));

                    this.importObject (on);

                }

            });

        }

        return this.item;

    }

    public E getViewer ()
    {

        return this.viewer;

    }

}
