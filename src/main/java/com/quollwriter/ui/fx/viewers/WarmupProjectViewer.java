package com.quollwriter.ui.fx.viewers;

import java.util.*;
import java.util.function.*;

import javafx.scene.*;
import javafx.scene.control.*;

import com.quollwriter.data.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.components.*;

public class WarmupProjectViewer extends AbstractProjectViewer
{

    public WarmupProjectViewer ()
    {

        super ();

    }

    @Override
    public SideBar getMainSideBar ()
    {

        return null;

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.WARMUP;

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
    public void openPanelForId (String id)
                         throws GeneralException
    {

        super.openPanelForId (id);

    }

    public void addNewWarmup (Warmup w)
                       throws Exception
    {

        // TODO

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
    public void showOptions (String sect)
                      throws GeneralException
    {

    }

    @Override
    public Set<FindResultsBox> findText (String t)
    {

        // TODO
        return null;

    }

}
