package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.collections.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class QuollToolBar extends HBox //ToolBar
{

    private QuollToolBar (Builder b)
    {

        if (b.tooltip != null)
        {

            Tooltip t = new Tooltip ();
            t.textProperty ().bind (b.tooltip);

            UIUtils.setTooltip (this,
                                b.tooltip);

        }

        this.getStyleClass ().add (StyleClassNames.QTOOLBAR);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.isControls)
        {

            this.getStyleClass ().add (StyleClassNames.CONTROLS);

        }

        this.managedProperty ().bind (this.visibleProperty ());

        this.maxWidthProperty ().bind (this.prefWidthProperty ());
        this.minWidthProperty ().bind (this.prefWidthProperty ());
        //this.prefWidthProperty ().bind (this.widthProperty ());

        HBox.setHgrow (this,
                       Priority.NEVER);

        if (b.controls != null)
        {

            this.getChildren ().addAll (b.controls);

        }

    }

    public ObservableList<Node> getItems ()
    {

        return this.getChildren ();

    }

    public static QuollToolBar.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollToolBar>
    {

        private StringProperty label = null;
        private String styleName = null;
        private StringProperty tooltip = null;
        private Collection<? extends Node> controls = null;
        private boolean isControls = true;

        private Builder ()
        {

        }

        @Override
        public QuollToolBar build ()
        {

            return new QuollToolBar (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;

            return this;

        }

        public Builder tooltip (StringProperty prop)
        {

            this.tooltip = prop;

            return this;

        }

        public Builder tooltip (List<String> prefix,
                                String...    ids)
        {

            this.tooltip = getUILanguageStringProperty (Utils.newList (prefix, ids));
            return this;

        }

        public Builder tooltip (String... ids)
        {

            this.tooltip = getUILanguageStringProperty (ids);
            return this;

        }

        public Builder controls (Collection<? extends Node> c)
        {

            this.controls = c;
            return this;

        }

        public Builder isControls (boolean v)
        {

            this.isControls = v;
            return this;

        }

    }

}
