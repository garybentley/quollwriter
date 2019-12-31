package com.quollwriter.ui.fx.popups;

import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.scene.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.IPropertyBinder;

/**
 * A base class for content that is suitable for display within a sidebar.
 */
public abstract class PopupContent<E extends AbstractViewer> extends ViewerContent<E> implements PopupCreator, IPropertyBinder
{

    protected QuollPopup popup = null;

    public PopupContent (E viewer)
    {

        super (viewer);

    }

    @Override
    public IPropertyBinder getBinder ()
    {

        return this.getPopup ().getBinder ();

    }

    public void addChildPopup (PopupContent c)
    {

        this.getPopup ().addChildPopup (c.getPopup ());

    }

    public void close ()
    {

        this.getPopup ().close ();

    }

    public void show (Node n,
                      Side s)
    {

        this.getPopup ().show (n,
                               s);

    }

    public void show ()
    {

        this.getPopup ().show ();

    }

    public void show (double x,
                      double y)
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
