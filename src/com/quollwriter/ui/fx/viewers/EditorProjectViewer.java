package com.quollwriter.ui.fx.viewers;

import java.util.*;
import java.util.function.*;

import javafx.scene.*;
import javafx.scene.control.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.components.*;

public class EditorProjectViewer extends AbstractProjectViewer
{

    public EditorProjectViewer ()
    {

        super ();

    }

    @Override
    public SideBar getMainSideBar ()
    {

        return null;

    }

    @Override
    public Supplier<Set<MenuItem>> getSettingsMenuSupplier ()
    {

        // TODO
        return null;

    }

    @Override
    public Supplier<Set<Node>> getTitleHeaderControlsSupplier ()
    {

        // TODO
        return null;

    }

    @Override
    public void viewObject (DataObject d,
                            Runnable   doAfterView)
    {

    }

    @Override
    public Panel getPanelForId (String id)
    {

        return null;

    }

    @Override
    public void handleNewProject ()
                           throws Exception
    {

    }

    @Override
    public void handleOpenProject ()
                            throws Exception
    {

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.EDITOR;

    }

}
