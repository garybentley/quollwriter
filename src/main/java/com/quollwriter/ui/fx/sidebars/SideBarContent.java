package com.quollwriter.ui.fx.sidebars;

import javafx.scene.layout.*;
import javafx.beans.value.*;
import javafx.collections.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.IPropertyBinder;

/**
 * A base class for content that is suitable for display within a sidebar.
 */
public abstract class SideBarContent<E extends AbstractViewer> extends ViewerContent<E> implements Stateful, SideBarCreator, IPropertyBinder
{

    protected SideBar sidebar = null;

    public SideBarContent (E viewer)
    {

        super (viewer);

    }

    @Override
    public State getState ()
    {

        if (this.sidebar != null)
        {

            return this.sidebar.getState ();

        }

        return new State ();

    }

    @Override
    public IPropertyBinder getBinder ()
    {

        return this.getSideBar ().getBinder ();

    }

    @Override
    public void init (State s)
    {

        if (this.sidebar != null)
        {

            this.sidebar.init (s);

        }

    }

    public SideBar getSideBar ()
    {

        if (this.sidebar == null)
        {

            this.sidebar = this.createSideBar ();

        }

        return this.sidebar;

    }

}
