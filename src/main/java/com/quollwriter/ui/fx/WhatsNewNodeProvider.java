package com.quollwriter.ui.fx;

import javafx.scene.*;

import com.quollwriter.ui.fx.viewers.*;

public interface WhatsNewNodeProvider
{

    public Node getNode (AbstractViewer pv,
                         String         id);

}
