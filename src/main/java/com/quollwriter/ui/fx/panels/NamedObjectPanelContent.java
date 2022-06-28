package com.quollwriter.ui.fx.panels;

import javafx.beans.property.*;
import javafx.scene.image.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

/**
 * A base class for content that is suitable for display within a panel for a specific named object.
 */
public abstract class NamedObjectPanelContent<E extends AbstractProjectViewer, O extends NamedObject> extends PanelContent<E> implements ToolBarSupported
{

    protected O object = null;
    private BooleanProperty unsavedChangesProp = null;
    private boolean unsavedChanges = false;
    private QuollToolBar tb = null;

    public NamedObjectPanelContent (E viewer,
                                    O object)
    {

        super (viewer);

        this.object = object;
        this.unsavedChangesProp = new SimpleBooleanProperty (false);

    }

    @Override
    public QuollToolBar getToolBar ()
    {

        if (this.tb == null)
        {

            this.tb = QuollToolBar.builder ()
                .styleClassName (StyleClassNames.TOOLBAR)
                .controls (this.getToolBarItems ())
            //.configurable (tbs.isToolBarConfigurable ())
            //.inViewer (this.viewer)
                .build ();

        }

        return this.tb;

    }

    public ObjectProperty<Image> iconProperty ()
    {

        return null;

    }

    public O getObject ()
    {

        return this.object;

    }

    public void setHasUnsavedChanges (boolean v)
    {

        this.unsavedChanges = v;

        this.unsavedChangesProp.setValue (v);

    }

    public boolean hasUnsavedChanges ()
    {

        return this.unsavedChanges;

    }

    public BooleanProperty unsavedChangesProperty ()
    {

        return this.unsavedChangesProp;//.readOnlyBooleanProperty (this.unsavedChangesProp);

    }

    public void saveObject ()
                        throws Exception
    {

        this.viewer.saveObject (this.object,
                                true);

        this.setHasUnsavedChanges (false);

        // Fire an event to interested parties.
        this.fireEvent (new Panel.PanelEvent (this.getPanel (),
                                              Panel.PanelEvent.SAVED_EVENT));

    }

}
