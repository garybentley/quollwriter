package com.quollwriter.ui.fx.components;

import javafx.util.*;
import java.util.*;
import java.util.function.*;

import javafx.scene.control.*;
import javafx.scene.control.cell.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;

public class NamedObjectTree extends TreeView<NamedObject>
{

    private NamedObjectTree (Builder b)
    {

        this.setCellFactory (b.cellFactory != null ? b.cellFactory : new ProjectTreeCellFactory ());

        if (b.root != null)
        {

            this.setRoot (b.root);

        }

    }

/*
    public static QuollTree createSelectableTree ()
    {

        NamedObjectTree t = NamedObjectTree.builder ();
            .cellFactory (new ProjectTreeCellFactory ()
            {

                @Override
                public TreeCell<NamedObject> createCell ()
                {

                    return new CheckBoxTreeCell<> ()
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
                                //c.getStyleClass ().add (this.getStyle (value));
                                return;

                            }

                            this.textProperty ().bind (n.nameProperty ());
                            this.getStyleClass ().add (this.getStyle (n));

                        }

                    };

                }

            })
            .build ();

    }
*/

    public static TreeItem<NamedObject> createChapterTreeModel (Project                proj,
                                                                Predicate<NamedObject> objectFilter)
    {

        TreeItem<NamedObject> root = new TreeItem<> (proj);

        proj.getBooks ().get (0).getChapters ().stream ()
            .forEach (c ->
            {

                if ((objectFilter != null)
                    &&
                    (!objectFilter.test (c))
                   )
                {

                    return;

                }

                TreeItem<NamedObject> ch = new TreeItem<> (c);
                root.getChildren ().add (ch);

                // Get the outline items and scenes.

            });

        return root;

    }

    public static NamedObjectTree createChapterTree (Project                              proj,
                                                     Predicate<NamedObject>               objectFilter,
                                                     Function<NamedObject, Set<MenuItem>> contextMenuItemSupplier)
    {


        NamedObjectTree t = NamedObjectTree.builder ()
            .cellFactory (new ProjectTreeCellFactory ()
            {

                @Override
                public void initCell (NamedObject           obj,
                                      TreeCell<NamedObject> cell)
                {

                    // Set up the context menu.
                    if (contextMenuItemSupplier != null)
                    {

                        cell.setOnContextMenuRequested (ev ->
                        {

                            Set<MenuItem> its = contextMenuItemSupplier.apply (obj);

                            if ((its != null)
                                &&
                                (its.size () > 0)
                               )
                            {

                                ContextMenu m = new ContextMenu ();
                                m.getItems ().addAll (its);
                                cell.setContextMenu (m);

                            }

                        });

                    }

                }

            })
            .root (NamedObjectTree.createChapterTreeModel (proj,
                                                           objectFilter))
            .build ();

        t.setShowRoot (false);

        return t;

    }

    public static NamedObjectTree.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, NamedObjectTree>
    {

        private String styleName = null;
        private TreeItem<NamedObject> root = null;
        private Callback<TreeView<NamedObject>, TreeCell<NamedObject>> cellFactory = null;

        private Builder ()
        {

        }

        public Builder cellFactory (Callback<TreeView<NamedObject>, TreeCell<NamedObject>> f)
        {

            this.cellFactory = f;
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

        public Builder draggable (boolean v)
        {

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
