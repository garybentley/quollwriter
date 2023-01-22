package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
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

        if (b.itemSupplier != null)
        {

            this.getItems ().addAll (b.itemSupplier.get ());

        }

        HBox h = new HBox ();
        h.getStyleClass ().add (StyleClassNames.ICONBOX);
        Pane p = new Pane ();
        p.getStyleClass ().add (b.styleName + "-" + StyleClassNames.ICON);
        p.getStyleClass ().add (StyleClassNames.ICON);
        h.getChildren ().add (p);
        h.managedProperty ().bind (h.visibleProperty ());

        this.setGraphic (h);

    }

    public static QuollMenu.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollMenu>
    {

        private StringProperty text = null;
        private String styleName = null;
        private Supplier<Set<MenuItem>> itemSupplier = null;

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
