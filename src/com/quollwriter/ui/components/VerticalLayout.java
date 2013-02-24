package com.quollwriter.ui.components;

import java.awt.*;

import java.util.*;


public class VerticalLayout implements LayoutManager
{

    private int vgap;
    private int hgap;
    private int width;

//Constructors
    /**
    * Constructs an instance of VerticalLayout with a vertical vgap of 5 pixels, horizontal centering and anchored to
    * the top of the display area.
    */
    public VerticalLayout(int width)
    {

        this (5,
              5,
              width);
    }

    /**
    * Constructs a VerticalLayout instance with horizontal centering, anchored to the top with the specified vgap
    *
    * @param vgap An int value indicating the vertical seperation of the components
    */
    public VerticalLayout(int vgap,
                          int width)
    {

        this (vgap,
              5,
              width);

    }

    public VerticalLayout(int vgap,
                          int hgap,
                          int width)
    {

        this.vgap = vgap;
        this.hgap = hgap;
        this.width = width;

    }

    private Dimension getParentPreferredSize (Container parent,
                                              boolean   min)
    {

        Insets insets = parent.getInsets ();

        synchronized (parent.getTreeLock ())
        {

            java.util.List<Component> visComps = new ArrayList ();

            int n = parent.getComponentCount ();

            for (int i = 0; i < n; i++)
            {

                Component c = parent.getComponent (i);

                if (!c.isVisible ())
                {

                    continue;

                }

                visComps.add (c);

            }

            Dimension pd = parent.getSize ();

            int w = 0;

            int ccount = 0;

            int pdw = pd.width - insets.left - insets.right;

            while (true)
            {

                if (((w + this.width) >= pdw) ||
                    (ccount >= visComps.size ()))
                {

                    break;

                }

                w += this.width;

                ccount++;

                if ((w + this.hgap + this.width) < pdw)
                {

                    w += this.hgap;

                }

            }

            if (ccount == 0)
            {

                ccount = 1;

            }

            n = visComps.size ();

            int xs = ((pdw - w) / 2) + insets.left;

            int x = xs;

            int y = insets.top;

            if (ccount == 1)
            {

                for (int i = 0; i < n; i++)
                {

                    Component c = visComps.get (i);

                    Dimension d = (min ? c.getMinimumSize () : c.getPreferredSize ());

                    c.setBounds (x,
                                 y,
                                 this.width,
                                 d.height);

                    y += d.height + this.vgap;

                }

                return new Dimension (this.width,
                                      (y - this.vgap));

            }

            int row = 0;

            int col = 1;

            int maxHeight = 0;
            int maxWidth = 0;

            // Split into rows.
            java.util.List<Integer> rowHeights = new ArrayList (ccount);

            for (int i = 0; i < n; i++)
            {

                Component c = visComps.get (i);

                Dimension d = (min ? c.getMinimumSize () : c.getPreferredSize ());

                y = this.getColHeight (col - 1,
                                       rowHeights,
                                       insets.top);

                if ((col > 1) &&
                    (col < (ccount + 1)))
                {

                    x += this.hgap;

                }

                x += this.width;
                y += d.height + this.vgap;

                maxWidth = Math.max (maxWidth,
                                     x);
                maxHeight = Math.max (maxHeight,
                                      (y - this.vgap));

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

            return new Dimension (maxWidth,
                                  maxHeight);

        }

    }

    private Dimension layoutSize (Container parent,
                                  boolean   minimum)
    {

        Dimension size = parent.getSize ();

        if ((size != null) &&
            (size.height > 0) &&
            (size.width > 0))
        {

            return this.getParentPreferredSize (parent,
                                                minimum);

        }

        java.util.List<Component> visComps = new ArrayList ();

        int y = 0;

        int n = parent.getComponentCount ();

        for (int i = 0; i < n; i++)
        {

            Component c = parent.getComponent (i);

            if (!c.isVisible ())
            {

                continue;

            }

            Dimension d = minimum ? c.getMinimumSize () : c.getPreferredSize ();

            y += d.height + this.vgap;

        }

        Insets insets = parent.getInsets ();

        y += insets.top + insets.bottom;

        return new Dimension (this.width + insets.left + insets.right,
                              y);

    }

    /**
     * Lays out the container.
     */
    public void layoutContainer (Container parent)
    {

        Insets insets = parent.getInsets ();

        synchronized (parent.getTreeLock ())
        {

            java.util.List<Component> visComps = new ArrayList ();

            int n = parent.getComponentCount ();

            for (int i = 0; i < n; i++)
            {

                Component c = parent.getComponent (i);

                if (!c.isVisible ())
                {

                    continue;

                }

                visComps.add (c);

            }

            Dimension pd = parent.getSize ();

            int w = 0;

            int ccount = 0;

            int pdw = pd.width - insets.left - insets.right;

            while (true)
            {

                if (((w + this.width) >= pdw) ||
                    (ccount >= visComps.size ()))
                {

                    break;

                }

                w += this.width;

                ccount++;

                if ((w + this.hgap + this.width) < pdw)
                {

                    w += this.hgap;

                }

            }

            if (ccount == 0)
            {

                ccount = 1;

            }

            n = visComps.size ();

            int xs = ((pdw - w) / 2) + insets.left;

            int x = xs;

            int y = insets.top;

            if (ccount == 1)
            {

                for (int i = 0; i < n; i++)
                {

                    Component c = visComps.get (i);

                    Dimension d = c.getPreferredSize ();

                    c.setBounds (x,
                                 y,
                                 this.width,
                                 d.height);

                    y += d.height + this.vgap;

                }

                return;

            }

            int row = 0;

            int col = 1;

            // Split into rows.
            java.util.List<Integer> rowHeights = new ArrayList (ccount);

            for (int i = 0; i < n; i++)
            {

                Component c = visComps.get (i);

                Dimension d = c.getPreferredSize ();

                y = this.getColHeight (col - 1,
                                       rowHeights,
                                       insets.top);

                if ((col > 1) &&
                    (col < (ccount + 1)))
                {

                    x += this.hgap;

                }

                c.setBounds (x,
                             y,
                             this.width,
                             d.height);

                x += this.width;
                y += d.height + this.vgap;

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

    private int getColHeight (int                     ind,
                              java.util.List<Integer> l,
                              int                     def)
    {

        if (ind > (l.size () - 1))
        {

            return def;

        }

        return l.get (ind);

    }

    private void addUpdateList (int            ind,
                                int            v,
                                java.util.List l)
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

//-----------------------------------------------------------------------------
    public Dimension minimumLayoutSize (Container parent)
    {
        return layoutSize (parent,
                           true);
    }
//-----------------------------------------------------------------------------
    public Dimension preferredLayoutSize (Container parent)
    {
        return layoutSize (parent,
                           false);
    }
//----------------------------------------------------------------------------

    /**
     * Not used by this class
     */
    public void addLayoutComponent (String    name,
                                    Component comp)
    {
    }
//-----------------------------------------------------------------------------

    /**
     * Not used by this class
     */
    public void removeLayoutComponent (Component comp)
    {
    }
//-----------------------------------------------------------------------------
    public String toString ()
    {
        return getClass ().getName () + "[vgap=" + vgap + " hgap=" + this.hgap + " width=" + this.width + "]";
    }
}
