package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.event.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class QuollMenuItem extends MenuItem
{

    private QuollMenuItem (Builder b)
    {

        if (b.text != null)
        {

            this.textProperty ().bind (b.text);

        }

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }
/*
        if (b.tooltip != null)
        {

            UIUtils.setTooltip (this,
                                b.tooltip);

        }
*/

        if (b.accelerator != null)
        {

            this.setAccelerator (b.accelerator);

        }

        if (b.onAction != null)
        {

            this.setOnAction (b.onAction);

        }

        this.setGraphic (new ImageView ());

    }

    /**
     * Get a builder to create a new menu item.
     *
     * Usage: QuollMenuItem.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollMenuItem>
    {

        private StringProperty text = null;
        private KeyCombination accelerator = null;
        private String styleName = null;
        private EventHandler<ActionEvent> onAction = null;

        private Builder ()
        {

        }

        @Override
        public QuollMenuItem build ()
        {

            return new QuollMenuItem (this);

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

        public Builder label (StringProperty prop)
        {

            this.text = prop;
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

        public Builder accelerator (KeyCombination kc)
        {

            this.accelerator = kc;
            return this;

        }

/*
        public Builder tooltip (StringProperty prop)
        {

            this.tooltip = prop;
            return this;

        }

        public Builder tooltip (List<String> prefix,
                                String...    ids)
        {

            return this.tooltip (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder tooltip (String... ids)
        {

            return this.tooltip (getUILanguageStringProperty (ids));

        }
*/
    }

}
