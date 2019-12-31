package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.geometry.*;
import javafx.css.*;
import javafx.css.converter.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.input.*;

import com.quollwriter.ui.fx.*;

public class QuollTreeView<T> extends Pane
{

    private static final CssMetaData<QuollTreeView, Number> CELL_INDENT = new CssMetaData<> ("-fx-cell-indent", SizeConverter.getInstance (), 0d)
    {

        @Override
        public boolean isSettable (QuollTreeView node)
        {

            return (node.cellIndent == null)
                    ||
                   (!node.cellIndent.isBound ());

        }

        @Override
        public StyleableProperty getStyleableProperty (QuollTreeView node)
        {

            return node.cellIndent;

        }

    };

    private static List<CssMetaData<? extends Styleable, ?>> styleables;

    static
    {

        List<CssMetaData<? extends Styleable, ?>> temp = new ArrayList<> ();

        temp.add (CELL_INDENT);

        styleables = Collections.unmodifiableList (temp);

    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData ()
    {

        return styleables;

    }

    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData ()
    {

        return QuollTreeView.<T>getClassCssMetaData ();

    }

    private StyleableDoubleProperty cellIndent = null;

    private Map<TreeItem<T>, QuollTreeCell> cells = new HashMap<> ();
    private Function<TreeItem<T>, Node> cellProvider = null;
    private TreeItem<T> root = null;

    // Make a property.
    private boolean showRoot = true;

    public QuollTreeView ()
    {

        super ();

        this.getStyleClass ().add (StyleClassNames.TREE);
        // Default the indent to 10.
        this.cellIndent = new SimpleStyleableDoubleProperty (CELL_INDENT, 10d);

    }

    public QuollTreeView (TreeItem<T> root)
    {

        this ();

        this.setRoot (root);

    }

    public void toggleOpen (TreeItem<T> item)
    {

        item.setExpanded (!item.isExpanded ());

    }

    public void insertAfter (TreeItem<T> newItem,
                             TreeItem<T> insertAfter)
    {

        if (this.root == insertAfter)
        {

            this.root.getChildren ().add (newItem);
            return;

        }

        TreeItem<T> parent = this.root;
        int ind = 0;

        if (insertAfter != null)
        {

            parent = insertAfter.getParent ();

            ind = parent.getChildren ().indexOf (insertAfter);

        }

        if (ind < 0)
        {

            // Add at the bottom.
            ind = parent.getChildren ().size ();

        }

        parent.getChildren ().add (ind,
                                   newItem);

    }

    public TreeItem<T> getTreeItemForObject (T o)
    {

        return this.getTreeItemForObject (o,
                                          null);

    }

    public TreeItem<T> getTreeItemForObject (T           o,
                                             TreeItem<T> child)
    {

        if (child == null)
        {

            child = this.root;

        }

        if (child.getValue ().equals (o))
        {

            return child;

        }

        for (TreeItem<T> item : child.getChildren ())
        {

            TreeItem<T> tti = this.getTreeItemForObject (o,
                                                         item);

            if (tti != null)
            {

                return tti;

            }

        }

        return null;

    }

    public QuollTreeCell getCellForObject (T o)
    {

        for (TreeItem<T> item : this.cells.keySet ())
        {

            if (item.getValue ().equals (o))
            {

                return this.cells.get (item);

            }

        }

        return null;

    }

    public void select (Object o)
    {

        this.clearSelection ();

        this.cells.keySet ().stream ()
            .forEach (ti ->
            {

                if (ti.getValue ().equals (o))
                {

                    QuollTreeCell c = this.cells.get (ti);

                    if (c != null)
                    {

                        c.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, true);

                        UIUtils.scrollIntoView (c,
                                                VPos.CENTER);

                    }

                }

            });

    }

    public void expandAll ()
    {

        this.walkTree (item ->
        {

            item.setExpanded (true);

        });

    }

    public void expandPathToRoot (Set<T> objs)
    {

        if (objs == null)
        {

            return;

        }

        objs.stream ()
            .forEach (o ->
            {

                TreeItem<T> ti = this.getTreeItemForObject (o);

                if (ti != null)
                {

                    this.expandPathToRoot (ti);

                }

            });

    }

    public void expandPathToRoot (TreeItem<T> item)
    {

        if (item != null)
        {

            item.setExpanded (true);

        }

        TreeItem<T> p = item.getParent ();

        if (p == null)
        {

            return;

        }

        this.expandPathToRoot (p);

    }

    public Set<TreeItem<T>> getExpandedTreeItems ()
    {

        Set<TreeItem<T>> expanded = new HashSet<> ();

        this.walkTree (item ->
        {

            if (item.isExpanded ())
            {

                expanded.add (item);

            }

        });

        return expanded;

    }

    public void walkTree (Consumer<TreeItem<T>> processor)
    {

        if (this.root == null)
        {

            return;

        }

        this.walkTree (this.root,
                       processor);

    }

    public void walkTree (TreeItem<T>           item,
                          Consumer<TreeItem<T>> processor)
    {

        processor.accept (item);

        item.getChildren ().stream ()
            .forEach (i -> this.walkTree (i, processor));

    }

    public void clearSelection ()
    {

        this.cells.values ().stream ()
            .forEach (c -> c.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, false));

    }

    public void setShowRoot (boolean v)
    {

        this.showRoot = v;

    }

    public TreeItem<T> getRoot ()
    {

        return this.root;

    }

    public void setRoot (TreeItem<T> root)
    {

        this.root = root;

        if (this.root == null)
        {

            return;

        }

        this.getChildren ().clear ();
        this.cells.clear ();

        if (this.showRoot)
        {

            QuollTreeCell c = new QuollTreeCell (this.root,
                                                 this.cellProvider.apply (this.root));

            this.cells.put (this.root,
                            c);

            this.getChildren ().add (c);

        } else {

            this.root.setExpanded (true);

        }

        // Walk the tree and add any expanded cells.
        this.createExpandedCells (this.root);

        this.root.addEventHandler (TreeItem.<T>treeNotificationEvent (),
        ev ->
        {

            if (ev.wasRemoved ())
            {

                // Remove any removed cells.
                for (TreeItem<T> ti : ev.getRemovedChildren ())
                {

                    this.removeBranch (ti);

                }

                this.requestLayout ();

                return;

            }

            if (ev.wasAdded ())
            {

                for (TreeItem<T> ti : ev.getAddedChildren ())
                {

                    this.createExpandedCells (ti);

                }

                this.requestLayout ();
                return;

            }

            if (!ev.getTreeItem ().isExpanded ())
            {

                this.hideBranch (ev.getTreeItem ());

            } else {

                this.createExpandedCells (ev.getTreeItem ());

            }

            // Do a layout pass.
            this.requestLayout ();

        });

    }

    public void removeObject (T t)
    {

        TreeItem<T> ti = this.getTreeItemForObject (t);

        TreeItem<T> parent = ti.getParent ();
        parent.getChildren ().remove (ti);
        this.requestLayout ();

    }

    public void removeBranch (TreeItem<T> ti)
    {

        this.getChildren ().remove (this.cells.remove (ti));

        for (TreeItem<T> ci : ti.getChildren ())
        {

            this.removeBranch (ci);

        }

    }

    private void hideBranch (TreeItem<T> ti)
    {

        ti.getChildren ().stream ()
            .forEach (cti ->
            {

                QuollTreeCell c = this.cells.get (cti);

                if (c != null)
                {

                    c.setVisible (false);

                }

                this.hideBranch (cti);

            });

    }

    private boolean isPathToParentExpanded (TreeItem<T> ti)
    {

        if (ti == null)
        {

            return false;

        }

        if (ti.isLeaf ())
        {

            return this.isPathToParentExpanded (ti.getParent ());

        }

        if (!ti.isExpanded ())
        {

            return false;

        }

        TreeItem<T> parent = ti.getParent ();

        if (parent == null)
        {

            return true;

        }

        return this.isPathToParentExpanded (parent);

    }

    private void createExpandedCells (TreeItem<T> ti)
    {

/* EXP */
        if (true)
        {
            return;
        }

        QuollTreeCell c = this.cells.get (ti);

        if (c != null)
        {

            c.setVisible (this.isPathToParentExpanded (ti.getParent ()));

        }

        if (ti.isExpanded ())
        {

            //QuollTreeCell c = this.cells.get (ti);

            if (c == null)
            {

                c = new QuollTreeCell (ti,
                                       this.cellProvider.apply (ti));
                c.setVisible (this.isPathToParentExpanded (ti.getParent ()));

                this.cells.put (ti,
                                c);

                this.getChildren ().add (c);

            }

            for (TreeItem<T> cti : ti.getChildren ())
            {

                c = new QuollTreeCell (cti,
                                       this.cellProvider.apply (cti));

                this.cells.put (cti,
                                c);
                c.setVisible (this.isPathToParentExpanded (cti.getParent ()));

                this.getChildren ().add (c);

            }

        }

        for (TreeItem<T> cti : ti.getChildren ())
        {

            this.createExpandedCells (cti);

        }

    }

    @Override
    protected void layoutChildren ()
    {

        if (this.root == null)
        {

            this.getChildren ().clear ();
            return;

        }

        double y = 0;

        if (this.showRoot)
        {

            this.layoutTreeItem (0,
                                 y,
                                 this.root);

            return;

        }

        QuollTreeCell rc = this.cells.get (this.root);

        if (rc != null)
        {

            rc.setVisible (false);

        }

        if (this.root.isExpanded ())
        {

            for (TreeItem ti : this.root.getChildren ())
            {

                y = this.layoutTreeItem (0,
                                          y,
                                          ti);

            }

        }

    }

    public void setCellProvider (Function<TreeItem<T>, Node> s)
    {

        this.cellProvider = s;

    }

    @Override
    public double computeMinHeight (double width)
    {

        return this.computePrefHeight (width);

    }

    @Override
    public double computePrefHeight (double width)
    {

        return this.calcPrefHeight (this.root,
                                    width,
                                    0);

    }

    private QuollTreeCell createCell (TreeItem<T> ti)
    {

        QuollTreeCell cell = new QuollTreeCell (ti,
                                                this.cellProvider.apply (ti));
        //c.setVisible (this.isPathToParentExpanded (ti.getParent ()));

        this.cells.put (ti,
                        cell);

        this.getChildren ().add (cell);

        return cell;

    }

    private double calcPrefHeight (TreeItem<T> ti,
                                   double      width,
                                   double      total)
    {

        QuollTreeCell rc = this.cells.get (ti);

        if ((ti != this.root)
            ||
            ((ti == this.root)
             &&
             (this.showRoot)
            )
           )
        {

            if (rc == null)
            {

                rc = this.createCell (ti);

            }

            total += rc.prefHeight (width);

        }

        if (ti.isExpanded ())
        {

            for (TreeItem<T> ci : ti.getChildren ())
            {

                total += this.calcPrefHeight (ci,
                                              width,
                                              0);

            }

        }

        return total;

    }

    private double layoutTreeItem (int         indent,
                                   double      yoffset,
                                   TreeItem<T> ti)
    {

        QuollTreeCell cell = this.cells.get (ti);

        if (cell == null)
        {

            /* EXP */
            cell = new QuollTreeCell (ti,
                                   this.cellProvider.apply (ti));
            //c.setVisible (this.isPathToParentExpanded (ti.getParent ()));

            this.cells.put (ti,
                            cell);

            this.getChildren ().add (cell);

            //return yoffset;

        }

        Insets insets = getInsets();
        double width = getWidth();
        double height = getHeight();
        double top = 0; //snapSpaceY(insets.getTop());
        double left = 0; //snapSpaceX(insets.getLeft());
        double bottom = 0; //snapSpaceY(insets.getBottom());
        double right = 0; //snapSpaceX(insets.getRight());
        //double space = snapSpaceY(getSpacing());

        double x = left + (indent * cellIndent.getValue ());
        double y = top + yoffset;// + computeYOffset(height - top - bottom, contentHeight, vpos);
        double contentWidth = width - left - right;

        // Is this true?
        boolean fillWidth = true;

        cell.setVisible (true);
        //cell.autosize ();
        double ch = cell.prefHeight (width);
        double cw = cell.prefWidth (ch);

        this.layoutInArea (cell,
                           x,
                           y,
                           // Check for this filling the area?
                           cw,
                           ch,
                           ch,
                           new Insets (0, 0, 0, 0),//getMargin (cell),
                           fillWidth,
                           true,
                           HPos.LEFT,
                           VPos.TOP,
                           true);

        // Have to reqeust a layout or the cell won't be redrawn.
        cell.requestLayout ();
        y += ch;

        if (ti.isExpanded ())
        {

            indent++;

            for (TreeItem<T> c : ti.getChildren ())
            {

                y = this.layoutTreeItem (indent,
                                         y,
                                         c);

            }

        }

        return y;

    }

    public static class QuollTreeCell extends HBox
    {

        private TreeItem item = null;
        private Node disclosureNode = null;
        private StackPane disclosureNodeWrapper = null;

        public QuollTreeCell (TreeItem      ti,
                              Node          content)
        {

            this.item = ti;

            this.managedProperty ().bind (this.visibleProperty ());
            this.disclosureNode = new ImageView ();
            this.disclosureNodeWrapper = new StackPane ();
            this.disclosureNodeWrapper.getChildren ().add (this.disclosureNode);
            this.disclosureNode.getStyleClass ().add ("disclosure");
            this.getStyleClass ().add (StyleClassNames.CELL);
            this.getChildren ().add (this.disclosureNodeWrapper);

            if (ti instanceof CheckBoxTreeItem)
            {

                CheckBoxTreeItem cbti = (CheckBoxTreeItem) ti;

                // Need to handle this better.
                CheckBox cb = new CheckBox ();

                cbti.selectedProperty ().addListener ((pr, oldv, newv) ->
                {

                    cb.setSelected (newv);

                });

                cb.setOnAction (ev ->
                {

                    cbti.setSelected (cb.isSelected ());

                    this.requestLayout ();

                });

                content.addEventHandler (MouseEvent.MOUSE_CLICKED,
                                         ev ->
                {

                    cbti.setSelected (!cbti.isSelected ());

                    this.requestLayout ();

                });

                if (cbti.isSelected ())
                {

                    cb.setSelected (true);

                }

                this.getChildren ().add (cb);

            }

            this.getChildren ().add (content);

            this.disclosureNodeWrapper.setOnMouseClicked (ev ->
            {

                ti.setExpanded (!ti.isExpanded ());

                this.requestLayout ();

            });

        }

        protected void handleModificationEvent (TreeItem.TreeModificationEvent ev)
        {

        }

        @Override
        public void layoutChildren ()
        {

            this.pseudoClassStateChanged (StyleClassNames.LEAF_PSEUDO_CLASS, this.item.isLeaf ());

            this.disclosureNode.pseudoClassStateChanged (StyleClassNames.EXPANDED_PSEUDO_CLASS, this.item.isExpanded ());
            this.disclosureNodeWrapper.setVisible (!this.item.isLeaf ());

            super.layoutChildren ();

        }

    }

}
