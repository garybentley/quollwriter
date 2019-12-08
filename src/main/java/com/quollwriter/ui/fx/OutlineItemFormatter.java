package com.quollwriter.ui.fx;

import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

public class OutlineItemFormatter extends AbstractChapterItemFormatter<OutlineItem>
{

    public OutlineItemFormatter (ProjectViewer   viewer,
                                 IPropertyBinder binder,
                                 OutlineItem     item,
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

        String desc = this.item.getDescription ().getMarkedUpText ();

        // TODO Change to use a QuollTextView.
        BasicHtmlTextFlow t = BasicHtmlTextFlow.builder ()
            .text (desc)
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        v.getChildren ().add (t);

        if (this.item.getScene () != null)
        {

            v.getChildren ().add (QuollHyperlink.builder ()
                .label (this.item.getScene ().getName ())
                .styleClassName (StyleClassNames.SCENE)
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectViewer.CommandId.viewobject,
                                            item.getScene ());

                })
                .build ());

        }

        return v;

    }

    public String getStyleClassName ()
    {

        return StyleClassNames.OUTLINEITEM;

    }

    public StringProperty getPopupTitle ()
    {

        return Environment.getObjectTypeName (this.item);

    }

}
