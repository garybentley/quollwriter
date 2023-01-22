package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.geometry.*;
import javafx.css.*;
import javafx.css.converter.*;
import javafx.scene.*;
import javafx.scene.layout.*;

public class VerticalLayout extends Pane
{

    private static final CssMetaData<VerticalLayout, Number> VGAP = new CssMetaData<> ("-fx-vgap", SizeConverter.getInstance (), 0d)
    {

        @Override
        public boolean isSettable (VerticalLayout node)
        {

            return (node.vGap == null)
                    ||
                   (!node.vGap.isBound ());

        }

        @Override
        public StyleableProperty getStyleableProperty (VerticalLayout node)
        {

            return node.vGap;

        }

    };

    private static final CssMetaData<VerticalLayout, Number> HGAP = new CssMetaData<> ("-fx-hgap", SizeConverter.getInstance (), 0d)
    {

        @Override
        public boolean isSettable (VerticalLayout node)
        {

            return (node.hGap == null)
                    ||
                   (!node.hGap.isBound ());

        }

        @Override
        public StyleableProperty getStyleableProperty (VerticalLayout node)
        {

            return node.hGap;

        }

    };

    private static final CssMetaData<VerticalLayout, Number> ITEM_WIDTH = new CssMetaData<> ("-qw-item-width", SizeConverter.getInstance (), 0d)
    {

        @Override
        public boolean isSettable (VerticalLayout node)
        {

            return (node.itemWidth == null)
                    ||
                   (!node.itemWidth.isBound ());

        }

        @Override
        public StyleableProperty getStyleableProperty (VerticalLayout node)
        {

            return node.itemWidth;

        }

    };

    private static List<CssMetaData<? extends Styleable, ?>> styleables;

    static
    {

        List<CssMetaData<? extends Styleable, ?>> temp = new ArrayList<> ();

        temp.add (HGAP);
        temp.add (VGAP);
        temp.add (ITEM_WIDTH);

        temp.addAll (Pane.getClassCssMetaData ());

        styleables = Collections.unmodifiableList (temp);

    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData ()
    {

        return styleables;

    }

    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData ()
    {

        return VerticalLayout.getClassCssMetaData ();

    }

    private StyleableDoubleProperty vGap = null;
    private StyleableDoubleProperty hGap = null;
    private StyleableDoubleProperty itemWidth = null;

    public VerticalLayout()
    {

        this.vGap = new SimpleStyleableDoubleProperty (VGAP, 0d);
        this.hGap = new SimpleStyleableDoubleProperty (HGAP, 0d);
        this.itemWidth = new SimpleStyleableDoubleProperty (ITEM_WIDTH, 200d);

    }

/*
    @Override
    protected double computePrefWidth (double h)
    {

    }
*/
/*
    @Override
    protected double computePrefHeight (double v)
    {

        double lw = this.getParent ().getWidth () - insets.getLeft () - insets.getRight ();

        double rw = 0;

        int col = 0;

        Map<Integer, Set<Node>> cols = new HashSet<> ();

        for (Node n : this.getChildren ())
        {

            if (!n.isManaged ())
            {

                continue;

            }

            if (rw < lw)
            {

                Set<Node> colNodes = cols.get (col);

            }

            visComps.add (n);

        }

    }
*/

    @Override
    protected double computeMinWidth (double _h)
    {

        Insets insets = this.getInsets ();

        return Math.round (this.itemWidth.getValue () - insets.getLeft () - insets.getRight ());

    }

    @Override
    protected double computePrefWidth (double _h)
    {

        return this.computeMinWidth (_h);

    }

    @Override
    protected double computeMinHeight (double _w)
    {
        return 200;
    }

    @Override
    protected double computePrefHeight (double _w)
    {

        if (_w == -1)
        {

            _w = ((Region) this.getParent ()).getWidth ();

        }

        if (_w == 0)
        {

            _w = this.computePrefWidth (-1);

        }

        double vg = this.vGap.getValue ();
        double hg = this.hGap.getValue ();
        double iw = this.itemWidth.getValue ();

        List<Node> visComps = new ArrayList<> ();

        for (Node n : this.getChildren ())
        {

            if (!n.isManaged ())
            {

                continue;

            }

            visComps.add (n);

        }

        double w = 0;

        int ccount = 0;

        Insets insets = this.getInsets ();

        double pdw = _w - insets.getLeft () - insets.getRight ();

        while (true)
        {

            if (((w + iw) >= pdw) ||
                (ccount >= visComps.size ()))
            {

                break;

            }

            w += iw;

            ccount++;

            if ((w + hg + iw) < pdw)
            {

                w += hg;

            }

        }

        if (ccount == 0)
        {

            ccount = 1;

        }

        double xs = insets.getLeft () + ((pdw - w) / 2);
        //(this.align == CENTER ? ((pdw - w) / 2) : 0);

        double x = xs;

        double y = insets.getTop ();
        boolean min = false;

        if (ccount == 1)
        {

            for (Node c : visComps)
            {

                double d = (min ? c.minHeight (iw) : c.prefHeight (iw));
                y += d + vg;

            }

            return Math.round (y - vg + insets.getBottom ());

        }

        int row = 0;

        int col = 1;

        double maxHeight = 0;
        double maxWidth = 0;

        // Split into rows.
        List<Double> rowHeights = new ArrayList<> (ccount);

        for (Node c : visComps)
        {

            double d = (min ? c.minHeight (iw) : c.prefHeight (iw));

            y = this.getColHeight (col - 1,
                                   rowHeights,
                                   insets.getTop () + insets.getBottom ());

            if ((col > 1) &&
                (col < (ccount + 1)))
            {

                x += hg;

            }

            x += iw;
            y += d + vg;

            maxWidth = Math.max (maxWidth,
                                 x);
            maxHeight = Math.max (maxHeight,
                                  (y - vg));

            this.addUpdateList (col - 1,
                                y,
                                rowHeights);

            if ((col > 1) &&
                (ccount > 1) &&
                ((col % ccount) == 0))
            {

                col = 1;
                x = xs;
                row++;

            } else
            {

                col++;

            }

        }

        return Math.round (maxHeight);

    }

    private double getColHeight (int                     ind,
                              List<Double> l,
                              double                     def)
    {

        if (ind > (l.size () - 1))
        {

            return def;

        }

        return l.get (ind);

    }

    private void addUpdateList (int            ind,
                                double            v,
                                List<Double> l)
    {

        if (ind > (l.size () - 1))
        {

            l.add (ind,
                   v);

        } else
        {

            l.set (ind,
                   v);

        }

    }

    /**
     * Lays out the container.
     */
    @Override
    protected void layoutChildren ()
    {

        double vg = this.vGap.getValue ();
        double hg = this.hGap.getValue ();
        double iw = this.itemWidth.getValue ();

        List<Node> visComps = new ArrayList ();

        for (Node c : this.getChildren ())
        {

            if (!c.isManaged ())
            {

                continue;

            }

            visComps.add (c);

        }

        Insets insets = this.getInsets ();

        double pdw = this.getLayoutBounds ().getWidth () - insets.getLeft () - insets.getRight ();

        double w = 0;

        int ccount = 0;

        while (true)
        {

            if (((w + iw) >= pdw) ||
                (ccount >= visComps.size ()))
            {

                break;

            }

            w += iw;

            ccount++;

            if ((w + hg + iw) < pdw)
            {

                w += hg;

            }

        }

        if (ccount == 0)
        {

            ccount = 1;

        }

        double xs = insets.getLeft () + ((pdw - w) / 2);
        //(this.align == CENTER ? ((pdw - w) / 2) : 0);

        double x = xs;

        double y = insets.getTop ();

        if (ccount == 1)
        {

            for (Node c : visComps)
            {

                double h = c.prefHeight (iw);

                c.resizeRelocate (Math.round (x),
                                  Math.round (y),
                                  Math.round (iw),
                                  Math.round (h));

                y += h + vg;

            }

            return;

        }

        int row = 0;

        int col = 1;

        // Split into rows.
        List<Double> rowHeights = new ArrayList<> (ccount);

        for (Node c : visComps)
        {

            double h = c.prefHeight (iw);

            y = this.getColHeight (col - 1,
                                   rowHeights,
                                   insets.getTop ());

            if ((col > 1) &&
                (col < (ccount + 1)))
            {

                x += hg;

            }

            c.resizeRelocate (Math.round (x),
                              Math.round (y),
                              Math.round (iw),
                              Math.round (h));

            x += iw;
            y += h + vg;

            this.addUpdateList (col - 1,
                                y,
                                rowHeights);

            if ((col > 1) &&
                (ccount > 1) &&
                ((col % ccount) == 0))
            {

                col = 1;
                x = xs;
                row++;

            } else
            {

                col++;

            }

        }

    }

}
