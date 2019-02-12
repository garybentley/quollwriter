package com.quollwriter.ui.fx;

import javafx.scene.layout.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

/**
 A base class for content that is suitable for viewing in a viewer (in some way).
 */
 // TODO Change to use a StackPane.
public abstract class ViewerContent<E extends AbstractViewer> extends VBox
{

    protected E viewer = null;

    public ViewerContent (E viewer)
    {

        this.viewer = viewer;
        this.setFillWidth (true);

    }

    public E getViewer ()
    {

        return this.viewer;

    }

}
