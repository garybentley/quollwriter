package com.quollwriter.ui.fx.sidebars;

import javafx.scene.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

/**
 * A base class for content that is suitable for display within a sidebar for a specific named object.
 */
public abstract class NamedObjectSideBarContent<E extends AbstractViewer, O extends NamedObject> extends SideBarContent<E>
{

    protected O object = null;

    public NamedObjectSideBarContent (E viewer,
                                      O object)
    {

        super (viewer);

        this.object = object;

    }

    public O getObject ()
    {

        return this.object;

    }

}
