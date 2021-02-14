package com.quollwriter.ui.fx;

import java.util.*;
import javafx.scene.*;

public interface ToolBarSupported
{

    Set<Node> getToolBarItems ();
    default boolean isToolBarConfigurable ()
    {

        return false;

    }

}
