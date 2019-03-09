package com.quollwriter.ui.fx.panels;

import javafx.beans.property.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

/**
 * A base class for content that is suitable for display within a panel for a specific named object.
 */
public abstract class NamedObjectPanelContent<E extends AbstractProjectViewer, O extends NamedObject> extends PanelContent<E>
{

    protected O object = null;
    private BooleanProperty unsavedChangesProp = null;

    public NamedObjectPanelContent (E viewer,
                                    O object)
    {

        super (viewer);

        this.object = object;
        this.unsavedChangesProp = new SimpleBooleanProperty (false);

    }

    public O getObject ()
    {

        return this.object;

    }

    public ReadOnlyBooleanProperty unsavedChangesProperty ()
    {

        return this.unsavedChangesProp.readOnlyBooleanProperty (this.unsavedChangesProp);

    }

    public void saveObject ()
                     throws Exception
    {

        this.viewer.saveObject (this.object,
                                true);

        this.unsavedChangesProp.setValue (false);

        // Fire an event to interested parties.
        this.fireEvent (new Panel.PanelEvent (this.getPanel (),
                                              Panel.PanelEvent.SAVED_EVENT));

    }

}
