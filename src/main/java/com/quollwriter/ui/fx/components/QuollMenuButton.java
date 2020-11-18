package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class QuollMenuButton extends MenuButton
{

    private IconBox icon = null;

    private QuollMenuButton (Builder b)
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
/*
        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }
*/
        if (b.onAction != null)
        {

            this.setOnAction (b.onAction);

        }

        if (b.type != null)
        {

            ButtonBar.setButtonData (this,
                                     b.type);

        }

        this.setOnShowing (ev ->
        {

            this.getItems ().clear ();

            if (b.itemSupplier != null)
            {

                this.getItems ().addAll (b.itemSupplier.get ());

            }

        });

        this.icon = IconBox.builder ()
            .iconName (b.iconName)
            .build ();
        this.icon.setVisible (b.iconName != null);
        this.setGraphic (this.icon);

    }

    public void setIconName (String s)
    {

        this.icon.setIconName (s);
        if (s != null)
        {

            this.icon.setVisible (true);

        }

    }

    public static QuollMenuButton.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollMenuButton>
    {

        private StringProperty label = null;
        private String iconName = null;
        private StringProperty tooltip = null;
        private EventHandler<ActionEvent> onAction = null;
        private ButtonBar.ButtonData type = ButtonBar.ButtonData.APPLY;
        private Supplier<Set<MenuItem>> itemSupplier = null;

        private Builder ()
        {

        }

        @Override
        public QuollMenuButton build ()
        {

            return new QuollMenuButton (this);

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

        public Builder iconName (String n)
        {

            this.iconName = n;

            return this;

        }

        public Builder items (Set<MenuItem> items)
        {

            this.itemSupplier = new Supplier<Set<MenuItem>> ()
            {

                @Override
                public Set<MenuItem> get ()
                {

                    return items;

                }

            };

            return this;

        }

        public Builder items (Supplier<Set<MenuItem>> items)
        {

            this.itemSupplier = items;
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
