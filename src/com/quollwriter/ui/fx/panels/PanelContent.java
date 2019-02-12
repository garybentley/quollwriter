package com.quollwriter.ui.fx.panels;

import javafx.beans.property.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

/**
 * A base class for content that is suitable for display within a sidebar.
 */
public abstract class PanelContent<E extends AbstractViewer> extends ViewerContent<E> implements Stateful, PanelCreator
{

    protected Panel panel = null;
    private BooleanProperty readyForUseProp = null;

    public PanelContent (E viewer)
    {

        super (viewer);

        this.readyForUseProp = new SimpleBooleanProperty (false);

    }

    @Override
    public State getState ()
    {

        if (this.panel != null)
        {

            return this.panel.getState ();

        }

        return new State ();

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        if (this.panel != null)
        {

            this.panel.init (s);

        }

    }

    public Panel getPanel ()
    {

        if (this.panel == null)
        {

            this.panel = this.createPanel ();

        }

        return this.panel;

    }

    public ReadOnlyBooleanProperty readyForUseProperty ()
    {

        // This will be read only.
        return this.readyForUseProp.readOnlyBooleanProperty (this.readyForUseProp);

    }

    public void setReadyForUse ()
    {

        if (this.readyForUseProp.getValue ())
        {

            return;

        }

        this.readyForUseProp.setValue (true);

    }

}
