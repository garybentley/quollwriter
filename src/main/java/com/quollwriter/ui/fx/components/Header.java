package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

/**
 * A "header" used for the heading of a particular section or area.
 * It has a title, an icon and a set of controls.   The controls will always be shown on the far right of the box.
 *
 * The header has a default style class of "header".
 */
public class Header extends HBox
{

    private ImageView image = null;
    private Label title = null;
    private ToolBar toolbar = null;
    private ObjectProperty<Label> titleLabelProp = null;

    private Header (Builder b)
    {

        final Header _this = this;

        // TODO Is this needed?
        if (b.icon != null)
        {

            this.title.setGraphic (b.icon);

        }

        this.image = new ImageView ();
        this.image.getStyleClass ().add (StyleClassNames.ICON);
        this.image.managedProperty ().bind (this.image.visibleProperty ());

        this.title = new Label ();
        this.title.getStyleClass ().add (StyleClassNames.TITLE);
        this.titleLabelProp = new SimpleObjectProperty<> (this.title);

        HBox.setHgrow (this.title,
                       Priority.ALWAYS);

        this.getChildren ().addAll (this.image, this.title);
        this.getStyleClass ().add (StyleClassNames.HEADER);

        if (b.contextMenuItemSupplier != null)
        {

            this.title.setOnContextMenuRequested (ev ->
            {

                ContextMenu m = new ContextMenu ();

                Set<MenuItem> items = b.contextMenuItemSupplier.get ();

                if (items != null)
                {

                    m.getItems ().addAll (items);

                    _this.title.setContextMenu (m);

                }

            });

        }

        if (b.title != null)
        {

            this.title.textProperty ().bind (b.title);

        }

        if (b.tooltip != null)
        {

            Tooltip t = new Tooltip ();
            t.textProperty ().bind (b.tooltip);

            this.title.setTooltip (t);

        }

        this.toolbar = new ToolBar ();
        this.toolbar.managedProperty ().bind (this.toolbar.visibleProperty ());
        this.getChildren ().add (this.toolbar);
        this.toolbar.getStyleClass ().add (StyleClassNames.CONTROLS);

        ContextMenu cm = new ContextMenu ();
        MenuItem conf = new MenuItem ("Configure");
        conf.setOnAction (ev ->
        {

        });
        cm.getItems ().add (conf);

        this.toolbar.setContextMenu (cm);

        if (b.controls != null)
        {

            for (Node n : b.controls)
            {

                if (n == null)
                {

                    continue;

                }

                this.toolbar.getItems ().add (n);

            }

        }

        if (b.cursor != null)
        {

            //this.title.setCursor (b.cursor);

        }

        if (b.styleName != null)
        {

            if (!this.getStyleClass ().contains (b.styleName))
            {

                this.getStyleClass ().add (b.styleName);

            }

        }

    }

    public ObjectProperty<Node> iconProperty ()
    {

        return this.title.graphicProperty ();

    }

    public StringProperty titleProperty ()
    {

        return this.title.textProperty ();

    }

    public ObjectProperty<Label> titleLabelProperty ()
    {

        return this.titleLabelProp;

    }

    /**
     * Get a builder to create a new header.
     *
     * Usage: Header.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static Header.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, Header>
    {

        private StringProperty tooltip = null;
        private Cursor cursor = null;
        private StringProperty title = null;
        private Node icon = null;
        private String styleName = null;
        private Set<Node> controls = null;
        private Supplier<Set<MenuItem>> contextMenuItemSupplier = null;

        private Builder ()
        {

        }

        @Override
        public Header build ()
        {

            return new Header (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder contextMenu (final Set<MenuItem> items)
        {

            this.contextMenuItemSupplier = new Supplier<Set<MenuItem>> ()
            {

                @Override
                public Set<MenuItem> get ()
                {

                    return items;

                }

            };

            return this;

        }

        public Builder contextMenu (Supplier<Set<MenuItem>> items)
        {

            this.contextMenuItemSupplier = items;
            return this;

        }
/*
TODO Remove
        public Builder cursor (Cursor c)
        {

            this.cursor = c;
            return this;

        }
*/
        public Builder controls (Set<Node> c)
        {

            this.controls = c;

            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;

            return this;

        }

        public Builder icon (Node n)
        {

            this.icon = n;

            return this;

        }

        public Builder title (StringProperty prop)
        {

            this.title = prop;
            return this;

        }

        public Builder title (List<String> prefix,
                              String...    ids)
        {

            return this.title (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder title (String... ids)
        {

            return this.title (getUILanguageStringProperty (ids));

        }

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

    }

}
