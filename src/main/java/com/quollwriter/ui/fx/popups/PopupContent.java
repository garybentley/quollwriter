package com.quollwriter.ui.fx.popups;

import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.geometry.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

/**
 * A base class for content that is suitable for display within a sidebar.
 */
public abstract class PopupContent<E extends AbstractViewer> extends ViewerContent<E> implements PopupCreator
{

    protected QuollPopup popup = null;

    public PopupContent (E viewer)
    {

        super (viewer);

    }

    public void addChildPopup (PopupContent c)
    {

        this.getPopup ().addChildPopup (c.getPopup ());

    }

    public void close ()
    {

        this.getPopup ().close ();

    }

    public void show ()
    {

        this.getPopup ().show ();

    }

    public void show (int x,
                      int y)
    {

        this.getPopup ().show (x,
                               y);

    }

    public QuollPopup getPopup ()
    {

        if (this.popup == null)
        {

            this.popup = this.createPopup ();

        }

        return this.popup;

    }

}
