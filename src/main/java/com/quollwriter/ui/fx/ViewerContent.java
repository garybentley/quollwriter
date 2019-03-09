package com.quollwriter.ui.fx;

import javafx.scene.layout.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

/**
 A base class for content that is suitable for viewing in a viewer (in some way).
 */
public abstract class ViewerContent<E extends AbstractViewer> extends StackPane
{

    protected E viewer = null;

    public ViewerContent (E viewer)
    {

        if (viewer == null)
        {

            throw new IllegalArgumentException ("Viewer must be specified.");

        }

        this.viewer = viewer;
        //this.setFillWidth (true);

    }

    public E getViewer ()
    {

        return this.viewer;

    }

}
