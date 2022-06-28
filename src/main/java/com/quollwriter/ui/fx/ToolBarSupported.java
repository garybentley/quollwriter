package com.quollwriter.ui.fx;

import java.util.*;
import javafx.scene.*;

import com.quollwriter.ui.fx.components.*;

public interface ToolBarSupported
{

    default QuollToolBar getToolBar ()
    {

        return null;

    }

    default Set<Node> getToolBarItems ()
    {

        return new HashSet<> ();

    }

    default boolean isToolBarConfigurable ()
    {

        return false;

    }

}
