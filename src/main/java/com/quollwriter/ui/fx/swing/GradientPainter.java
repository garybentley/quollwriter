package com.quollwriter.ui.fx.swing;

import java.awt.*;


public class GradientPainter implements PaintProvider
{

    protected Color left = null;
    protected Color right = null;

    public GradientPainter(Color left,
                           Color right)
    {

        this.left = left;
        this.right = right;

    }

    public void setColors (Color left,
                           Color right)
    {

        this.left = left;
        this.right = right;

    }

    public PaintProvider getClone ()
    {

        return new GradientPainter (this.left,
                                    this.right);

    }

    public Paint getPaint (Component c)
    {

        Color l = this.left;

        if (l == null)
        {

            l = c.getBackground ();

        }

        Color r = this.right;

        if (r == null)
        {

            r = c.getBackground ();

        }

        int w = c.getWidth ();
        int h = c.getHeight ();

        return new GradientPaint (new Point (0,
                                             h),
                                  l,
                                  new Point (w,
                                             h),
                                  r);

    }

}
