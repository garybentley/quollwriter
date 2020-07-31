package com.quollwriter.ui.fx.components;

import javafx.util.*;
import java.util.*;
import java.util.function.*;

import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

public class NamedObjectTree extends QuollTreeView<NamedObject>
{

    public static final DataFormat PROJECT_OBJECT_DATA_FORMAT = new DataFormat ("project/object");

    private NamedObjectTree (Builder b)
    {

        if ((b.viewObjectOnClick)
            &&
            (b.viewer == null)
           )
        {

            throw new IllegalArgumentException ("Viewer must be provided when viewObjectOnClick is set to true.");

        }

        if ((b.canImport != null)
            &&
            (b.project == null)
           )
        {

            throw new IllegalArgumentException ("Project must be provided when canImport is provided.");

        }

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        this.setShowRoot (false);
        this.setRoot (b.root);
        this.setCellProvider (b.cellProvider != null ? b.cellProvider : treeItem ->
        {

            NamedObject n = treeItem.getValue ();

            Node l = null;

            if (b.labelProvider != null)
            {

                l = b.labelProvider.apply (treeItem);

            } else {

                if (n instanceof UserConfigurableObject)
                {

                    UserConfigurableObject nu = (UserConfigurableObject) n;

                    UserConfigurableObjectType uc = nu.getUserConfigurableObjectType ();

                    QuollLabel nl = QuollLabel.builder ()
                        .label (n.nameProperty ())
                        .build ();

                    ImageView iv = new ImageView ();
                    //iv.imageProperty ().bind (uc.icon16x16Property ());

                    nl.setGraphic (iv);

                    l = nl;

                } else {

                    l = QuollLabel.builder ()
                    .label (n.nameProperty ())
                    .styleClassName (n.getObjectType ())
                    .build ();

                }

            }

            Node _l = l;

            l.setOnMouseClicked (ev ->
            {

                if (ev.getButton () != MouseButton.PRIMARY)
                {

                    return;

                }

                if (_l.getProperties ().get ("context-menu") != null)
                {

                    ((ContextMenu) _l.getProperties ().get ("context-menu")).hide ();

                }

                if (b.onClick != null)
                {

                    b.onClick.accept (n,
                                      ev);
                    ev.consume ();

                } else {

                    if (b.viewObjectOnClick)
                    {

                        b.viewer.viewObject (n);
                        ev.consume ();

                    }

                }

            });

            l.setOnContextMenuRequested (ev ->
            {

                if (_l.getProperties ().get ("context-menu") != null)
                {

                    ((ContextMenu) _l.getProperties ().get ("context-menu")).hide ();

                }

                if (ev.getSource () != _l)
                {

                    return;

                }

                ContextMenu cm = new ContextMenu ();

                Set<MenuItem> its = new LinkedHashSet<> ();

                if (b.contextMenuItemSupplier != null)
                {

                    its.addAll (b.contextMenuItemSupplier.apply (n));

                } else {

                    // TODO Add own items?

                }

                if (its.size () > 0)
                {

                    cm.getItems ().addAll (its);

                    //_l.setContextMenu (cm);

                }

                _l.getProperties ().put ("context-menu", cm);
                cm.setAutoFix (true);
                cm.setAutoHide (true);
                cm.setHideOnEscape (true);
                cm.show (_l,
                         ev.getScreenX (),
                         ev.getScreenY ());
                ev.consume ();

            });

            l.setOnDragDetected (ev ->
            {

                if (b.canExport != null)
                {

                    if (!b.canExport.apply (n))
                    {

                        return;

                    }

                } else {

                    return;

                }

                Dragboard db = _l.startDragAndDrop (TransferMode.MOVE);

                ClipboardContent c = new ClipboardContent ();
                c.put (PROJECT_OBJECT_DATA_FORMAT,
                       n.getObjectReference ().asString ());

                db.setContent (c);
                db.setDragView (UIUtils.getImageOfNode (_l));
                _l.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, true);
                ev.consume ();

            });

            l.setOnDragExited (ev ->
            {

                _l.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            });

            l.setOnDragDone (ev ->
            {

                _l.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);
                _l.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);

            });

            l.setOnDragDropped (ev ->
            {

                _l.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

                Dragboard db = ev.getDragboard ();

                Object o = db.getContent (PROJECT_OBJECT_DATA_FORMAT);

                if (o != null)
                {

                    int ind = treeItem.getParent ().getChildren ().indexOf (treeItem);

                    NamedObject on = (NamedObject) b.project.getObjectForReference (ObjectReference.parseObjectReference (o.toString ()));

                    // See if we are importing from another place.
                    TreeItem<NamedObject> oitem = this.getTreeItemForObject (on);

                    if (oitem == null)
                    {

                        // We are adding to the tree.
                        oitem = new TreeItem<> (on);

                    } else {

                        oitem.getParent ().getChildren ().remove (oitem);

                    }

                    treeItem.getParent ().getChildren ().add (ind,
                                                              oitem);

                    if (b.onDragDropped != null)
                    {

                        b.onDragDropped.accept (on);

                        ev.setDropCompleted (true);
                        ev.consume ();

                    }

                }

            });

            l.setOnDragOver (ev ->
            {

                _l.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

                Dragboard db = ev.getDragboard ();

                Object o = db.getContent (PROJECT_OBJECT_DATA_FORMAT);

                if (o != null)
                {

                    NamedObject on = (NamedObject) b.project.getObjectForReference (ObjectReference.parseObjectReference (o.toString ()));

                    if (b.canImport != null)
                    {

                        if (!b.canImport.apply (n,
                                                on))
                        {

                            return;

                        }

                    } else {

                        return;

                    }

                    if (!on.equals (n))
                    {

                        _l.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

                        ev.acceptTransferModes (TransferMode.MOVE, TransferMode.COPY);

                        return;

                    }

                }

            });

            return l;

        });

    }

    public static NamedObjectTree.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, NamedObjectTree>
    {

        private String styleName = null;
        private TreeItem<NamedObject> root = null;
        private Function<TreeItem<NamedObject>, Node> cellProvider = null;
        private BiFunction<NamedObject, NamedObject, Boolean> canImport = null;
        private Function<NamedObject, Boolean> canExport = null;
        private Function<NamedObject, Set<MenuItem>> contextMenuItemSupplier = null;
        private Project project = null;
        private Consumer<NamedObject> onDragDropped = null;
        private boolean viewObjectOnClick = false;
        private BiConsumer<NamedObject, MouseEvent> onClick = null;
        private AbstractProjectViewer viewer = null;
        private Function<TreeItem<NamedObject>, Node> labelProvider = null;

        private Builder ()
        {

        }

        public Builder labelProvider (Function<TreeItem<NamedObject>, Node> labelProvider)
        {

            this.labelProvider = labelProvider;
            return this;

        }

        public Builder withViewer (AbstractProjectViewer viewer)
        {

            this.viewer = viewer;
            return this;

        }

        public Builder onClick (BiConsumer<NamedObject, MouseEvent> on)
        {

            this.onClick = on;
            return this;

        }

        public Builder viewObjectOnClick (boolean v)
        {

            this.viewObjectOnClick = v;
            return this;

        }

        public Builder root (TreeItem<NamedObject> root)
        {

            this.root = root;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;
            return this;

        }

        /*
         * First arg to function is the object of the item that we are over.
         * The second arg to the function is the object we wish to import to the item that we are over.
         */
        public Builder canImport (BiFunction<NamedObject, NamedObject, Boolean> im)
        {

            this.canImport = im;
            return this;

        }

        public Builder canExport (Function<NamedObject, Boolean> ex)
        {

            this.canExport = ex;
            return this;

        }

        public Builder cellProvider (Function<TreeItem<NamedObject>, Node> prov)
        {

            this.cellProvider = prov;
            return this;

        }

        public Builder contextMenuItemSupplier (Function<NamedObject, Set<MenuItem>> supp)
        {

            this.contextMenuItemSupplier = supp;
            return this;

        }

        public Builder project (Project p)
        {

            this.project = p;
            return this;

        }

        public Builder onDragDropped (Consumer<NamedObject> on)
        {

            this.onDragDropped = on;
            return this;

        }

        @Override
        public NamedObjectTree build ()
        {

            return new NamedObjectTree (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
