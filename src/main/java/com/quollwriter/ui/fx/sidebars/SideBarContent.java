package com.quollwriter.ui.fx.sidebars;

import javafx.scene.layout.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

/**
 * A base class for content that is suitable for display within a sidebar.
 */
public abstract class SideBarContent<E extends AbstractViewer> extends ViewerContent<E> implements Stateful, SideBarCreator
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
