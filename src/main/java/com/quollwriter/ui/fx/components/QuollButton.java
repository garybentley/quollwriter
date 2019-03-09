package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.control.*;
import javafx.event.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class QuollButton extends Button
{

    private QuollButton (Builder b)
    {

        if (b.label != null)
        {

            this.textProperty ().bind (b.label);

        }

        if (b.tooltip != null)
        {

            Tooltip t = new Tooltip ();
            t.textProperty ().bind (b.tooltip);

            this.setTooltip (t);

        }

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.onAction != null)
        {

            this.setOnAction (b.onAction);

        }

        if (b.type != null)
        {

            ButtonBar.setButtonData (this,
                                     b.type);

        }

    }

    /**
     * Get a builder to create a new button.
     *
     * Usage: QuollButton.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static QuollButton.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollButton>
    {

        private StringProperty label = null;
        private String styleName = null;
        private StringProperty tooltip = null;
        private EventHandler<ActionEvent> onAction = null;
        private ButtonBar.ButtonData type = ButtonBar.ButtonData.APPLY;

        private Builder ()
        {

        }

        @Override
        public QuollButton build ()
        {

            return new QuollButton (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder onAction (EventHandler<ActionEvent> onAction)
        {

            this.onAction = onAction;
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

        public Builder buttonType (ButtonBar.ButtonData type)
        {

            this.type = type;

            return this;

        }

        public Builder label (StringProperty prop)
        {

            this.label = prop;
            return this;

        }

        public Builder label (List<String> prefix,
                              String...    ids)
        {

            return this.label (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder label (String... ids)
        {

            return this.label (getUILanguageStringProperty (ids));

        }

    }

}
