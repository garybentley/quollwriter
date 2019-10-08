package com.quollwriter.ui.fx;

import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.ui.fx.viewers.*;

public class SceneItemFormatter extends AbstractChapterItemFormatter<com.quollwriter.data.Scene>
{

    public SceneItemFormatter (ProjectViewer              viewer,
                               IPropertyBinder            binder,
                               com.quollwriter.data.Scene scene)
    {

        super (viewer,
               binder,
               scene);

    }

    @Override
    public Node getContent ()
    {

        String desc = item.getDescription ().getMarkedUpText ();

        BasicHtmlTextFlow t = BasicHtmlTextFlow.builder ()
            .text (desc)
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        return t;

    }

    public String getStyleClassName ()
    {

        return StyleClassNames.SCENE;

    }

    public StringProperty getPopupTitle ()
    {

        return Environment.getObjectTypeName (this.item);

    }

}
