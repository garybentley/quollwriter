package com.quollwriter.ui.fx.swing;

import java.awt.Point;
import java.awt.Component;
import java.awt.Dimension;

import java.util.*;

public class DragEvent extends EventObject
{

    public static final int STARTED = 0;
    public static final int FINISHED = 1;
    public static final int IN_PROGRESS = 2;

    private Point start = null;
    private Point last = null;

    public DragEvent (Component source,
                      Point     start,
                      Point     last)
    {

        super (source);

        this.start = start;
        this.last = last;

    }

    public Dimension getDifference ()
    {

        Point s = this.start;
        Point l = this.last;

        if (l == null)
        {

            l = new Point (0, 0);

        }

        return new Dimension (l.x - s.x,
                              l.y - s.y);

    }

    public Point getLast ()
    {

        return this.last;

    }

    public Point getStart ()
    {

        return this.start;

    }

}
