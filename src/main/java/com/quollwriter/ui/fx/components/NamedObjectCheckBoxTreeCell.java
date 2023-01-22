package com.quollwriter.ui.fx.components;

import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.util.*;

import javafx.scene.control.*;
import javafx.scene.control.cell.*;

import com.quollwriter.data.*;

public class NamedObjectCheckBoxTreeCell extends CheckBoxTreeCell<NamedObject>
{

    public NamedObjectCheckBoxTreeCell (Callback<TreeItem<NamedObject>, ObservableValue<Boolean>> getSelProp)
    {

        super (getSelProp);

    }

    @Override
    public void updateItem (NamedObject n,
                            boolean     empty)
    {

        this.textProperty ().unbind ();

        super.updateItem (n,
                          empty);

        this.setWrapText (true);

        if ((empty)
            ||
            (n == null)
           )
        {

            // Remove styles?
            this.textProperty ().unbind ();
            this.getStyleClass ().remove (ProjectTreeCellFactory.getStyle (n));
            //c.getStyleClass ().add (this.getStyle (value));
            return;

        }

        if (!(this.getTreeItem () instanceof CheckBoxTreeItem))
        {

            this.setGraphic (null);

        } else {

            this.getStyleClass ().add (ProjectTreeCellFactory.getStyle (n));

        }

        this.textProperty ().unbind ();
        this.textProperty ().bind (n.nameProperty ());

    }

}
