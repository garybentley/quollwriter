package com.quollwriter.ui.fx;

import javafx.scene.*;
import javafx.scene.layout.*;

import com.quollwriter.ui.fx.viewers.*;

public class DebugConsole extends Region
{

    private AbstractViewer viewer = null;

    public DebugConsole (AbstractViewer viewer)
    {

        this.viewer = viewer;

    }

}
