package com.quollwriter.ui.fx.components;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.geometry.*;

public class QScrollPane extends ScrollPane
{

    public enum PartiallyVisiblePolicy
    {

        USE_POSITION_POLICY,
        BRING_INTO_VIEW;

    }

    private PartiallyVisiblePolicy partiallyVisiblePolicy = PartiallyVisiblePolicy.BRING_INTO_VIEW;
    private VPos verticalPositionPolicy  = VPos.CENTER;

    public QScrollPane ()
    {

        super ();

    }

    public QScrollPane (Node content)
    {

        super (content);

    }

    public void setPartiallyVisiblePolicy (PartiallyVisiblePolicy policy)
    {

        this.partiallyVisiblePolicy = policy;

    }

    public void setOutsideViewportVerticalPositionPolicy (VPos pos)
    {

        this.verticalPositionPolicy = pos;

    }

    public void scrollIntoView (Node node)
    {

        Bounds vb = this.getBoundsInLocal ();
        Bounds nb = this.getChildBounds (node);

        // Is the node already fully visible?
        if (vb.contains (nb))
        {

            return;

        }

        VPos pos = this.verticalPositionPolicy;

        double vh = this.getViewportBounds ().getHeight ();
        double sph = this.getContent ().getBoundsInLocal ().getHeight ();

        // Node is below or partially below the viewport bounds.
        if (nb.getMaxY () > vb.getMaxY ())
        {

            // Is the node somewhere in the viewport?
            if ((nb.getMinY () > vb.getMinY ())
                &&
                (nb.getMinY () < vb.getMaxY ())
                &&
                (this.partiallyVisiblePolicy == PartiallyVisiblePolicy.BRING_INTO_VIEW)
               )
            {

                // Partially visible.
                // Same as bottom position.
                pos = VPos.BOTTOM;

            }

            // Get the amount outside the viewport.
            double diff = nb.getMaxY () - vb.getMaxY ();

            if (pos == VPos.TOP)
            {

                diff = vh - (nb.getHeight () - diff);

            }

            if (pos == VPos.CENTER)
            {

                diff = (vh / 2) + ((nb.getHeight () / 2) - (nb.getHeight () - diff));

            }

            if (pos == VPos.BOTTOM)
            {

                // Don't need to handle, is just the amount off screen.

            }

            this.setVvalue (this.getVvalue () + ((diff / (sph - vh))));

            return;

        }

        if (nb.getMinY () < vb.getMinY ())
        {

            // Is the node somewhere in the viewport?
            if ((nb.getMaxY () > vb.getMinY ())
                &&
                (nb.getMaxY () < vb.getMaxY ())
                &&
                (this.partiallyVisiblePolicy == PartiallyVisiblePolicy.BRING_INTO_VIEW)
               )
            {

                // Partially visible.
                // Same as top position.
                pos = VPos.TOP;

            }

            // Get the amount outside the viewport.
            double diff = nb.getMinY () - vb.getMinY ();

            if (pos == VPos.TOP)
            {

                // Don't need to handle, is just the amount off screen.

            }

            if (pos == VPos.CENTER)
            {

                diff = (vh / -2) - ((nb.getHeight () / 2) - (nb.getHeight () + diff));

            }

            if (pos == VPos.BOTTOM)
            {

                diff = - vh + (nb.getHeight () + diff);

            }

            this.setVvalue (this.getVvalue () + ((diff / (sph - vh))));

            return;

        }

    }

    private Bounds getChildBounds (Node node)
    {

        Node p = node.getParent ();
        Bounds b = node.getBoundsInParent ();

        while (p != this)
        {

            if (p == null)
            {

                return null;

            }

            b = p.localToParent (b);
            p = p.getParent ();

        }

        return b;

    }

}
