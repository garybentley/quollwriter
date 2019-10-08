package com.quollwriter.ui.fx;

import javafx.beans.property.*;
import javafx.scene.Node;

public interface ChapterItemFormatter
{

    String getStyleClassName ();
    StringProperty getPopupTitle ();
    Node format ();

}
