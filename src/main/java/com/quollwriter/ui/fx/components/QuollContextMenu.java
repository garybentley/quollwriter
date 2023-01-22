package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

public class QuollContextMenu extends ContextMenu
{

    private QuollContextMenu (Builder b)
    {
/*
        UIUtils.addStyleSheet (this.getScene (),
                               Constants.COMPONENT_STYLESHEET_TYPE,
                               "menu");

        if (b.styleSheet != null)
        {

            UIUtils.addStyleSheet (this.getScene (),
                                   Constants.COMPONENT_STYLESHEET_TYPE,
                                   b.styleSheet);

        }
*/
        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.itemSupplier != null)
        {

            this.getItems ().addAll (b.itemSupplier.get ());

        }

    }

    public static QuollContextMenu.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollContextMenu>
    {

        private String styleName = null;
        private String styleSheet = null;
        private Supplier<Set<MenuItem>> itemSupplier = null;

        private Builder ()
        {

        }

        @Override
        public QuollContextMenu build ()
        {

            return new QuollContextMenu (this);

        }

        @Override
        public Builder _this ()
        {

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

        public Builder styleClassName (String n)
        {

            this.styleName = n;

            return this;

        }

    }

}
