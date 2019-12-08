package com.quollwriter.ui.fx;

import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

public class NoteItemFormatter extends AbstractChapterItemFormatter<Note>
{

    public NoteItemFormatter (ProjectViewer   viewer,
                              IPropertyBinder binder,
                              Note            item,
                              Runnable        onNewPopupShown)
    {

        super (viewer,
               binder,
               item,
               onNewPopupShown);

    }

    @Override
    public Node getContent ()
    {

        VBox v = new VBox ();

        String summ = this.item.getSummary ();

        String desc = null;

        if ((!this.item.getType ().equals (""))
            &&
            (!this.item.isEditNeeded ())
           )
        {

            summ = "<b>" + this.item.getType () + "</b>: " + summ;

            if (this.item.getDescription () != null)
            {

                summ += "<br />" + this.item.getDescription ().getMarkedUpText ();

            }

            desc = summ;

        } else {

            desc = item.getDescription ().getMarkedUpText ();

        }

        BasicHtmlTextFlow t = BasicHtmlTextFlow.builder ()
            .text (desc)
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        v.getChildren ().add (t);

        return v;

    }

    public String getStyleClassName ()
    {

        if (this.item.isEditNeeded ())
        {

            return StyleClassNames.EDITNEEDEDNOTE;

        }

        return StyleClassNames.NOTE;

    }

    public StringProperty getPopupTitle ()
    {

        return Environment.getObjectTypeName (this.item);

    }

}
