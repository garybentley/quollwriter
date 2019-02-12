package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.control.*;
import javafx.event.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class QuollMenu extends Menu
{

    private QuollMenu (Builder b)
    {

        if (b.text != null)
        {

            this.textProperty ().bind (b.text);

        }

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.items != null)
        {

            this.getItems ().addAll (b.items);

        }

    }

    /**
     * Get a builder to create a new menu item.
     *
     * Usage: QuollMenuItem.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static QuollMenu.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollMenu>
    {

        private StringProperty text = null;
        private String styleName = null;
        private Set<MenuItem> items = null;

        private Builder ()
        {

        }

        @Override
        public QuollMenu build ()
        {

            return new QuollMenu (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder items (Set<MenuItem> items)
        {

            this.items = items;
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

            return this.label (getUILanguageStringProperty (Utils.newList (prefix,ids)));

        }

        public Builder label (String... ids)
        {

            return this.label (getUILanguageStringProperty (ids));

        }

    }

}
