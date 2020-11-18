package com.quollwriter.ui.fx;

import javafx.beans.property.*;
import javafx.scene.Node;

import com.quollwriter.data.*;
import com.quollwriter.*;

public interface ChapterItemFormatter<E extends ChapterItem>
{

    String getStyleClassName ();
    StringProperty getPopupTitle ();
    Node format ();
    void deleteItem (E i);
    void editItem (E i);
    void saveItem (E i) throws GeneralException;

}
