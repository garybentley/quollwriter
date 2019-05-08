package com.quollwriter.ui.fx.components;

import java.net.*;
import java.util.*;
import java.util.function.*;

import javafx.css.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class Viewer extends Stage implements Stateful
{

    private Node content = null;
    private Header header = null;
    private String styleName = null;
    private StringProperty titleProp = null;
    private Supplier<Set<Node>> headerControlsSupplier = null;

    private Viewer (Builder b)
    {

        this._init (b);

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        if (s != null)
        {

            this.setHeight (s.getAsInt (Constants.WINDOW_HEIGHT_PROPERTY_NAME));
            this.setWidth (s.getAsInt (Constants.WINDOW_WIDTH_PROPERTY_NAME));

            int y = s.getAsInt (Constants.WINDOW_TOP_LOCATION_PROPERTY_NAME);

            if (y > 0)
            {

                this.setY (y);

            }

            int x = s.getAsInt (Constants.WINDOW_LEFT_LOCATION_PROPERTY_NAME);

            if (x > 0)
            {

                this.setX (x);

            }

        }

    }

    private void updateUIBaseFontSize ()
    {

        this.getScene ().getRoot ().setStyle (String.format ("-fx-font-size: %1$spt;",
                                                             UserProperties.getUIBaseFontSize ()));

    }

    @Override
    public State getState ()
    {

        State s = new State ();
        s.set (Constants.WINDOW_HEIGHT_PROPERTY_NAME,
               this.getScene ().getHeight ());
        s.set (Constants.WINDOW_WIDTH_PROPERTY_NAME,
               this.getScene ().getWidth ());
        s.set (Constants.WINDOW_TOP_LOCATION_PROPERTY_NAME,
               this.getY ());
        s.set (Constants.WINDOW_LEFT_LOCATION_PROPERTY_NAME,
               this.getX ());

        return s;

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

    private void _init (Builder b)
    {

        if (b.content == null)
        {

            throw new IllegalArgumentException ("Content must be provided.");

        }

        if (b.title == null)
        {

            throw new IllegalArgumentException ("Title must be provided.");

        }

        this.header = Header.builder ()
            .controls (b.headerControlsSupplier.get ())
            .styleClassName (StyleClassNames.HEADER)
            .build ();
        this.header.titleProperty ().bind (b.title);
        this.titleProp = b.title;

        this.titleProperty ().bind (b.title);

        VBox box = new VBox ();
        box.getStyleClass ().add (StyleClassNames.VIEWER);
        box.getStyleClass ().add (b.styleName);
        VBox.setVgrow (this.header,
                       Priority.NEVER);
        VBox.setVgrow (b.content,
                       Priority.ALWAYS);
        box.getChildren ().addAll (this.header, b.content);
        this.content = b.content;

        Scene s = new Scene (box);

		this.setScene (s);
        this.setMinWidth (300);
        this.setMinHeight (300);

        this.addStyleSheet (UserProperties.getUserStyleSheetURL ());

        UserProperties.uiBaseFontSizeProperty ().addListener ((pr, oldv, newv) ->
        {

            this.updateUIBaseFontSize ();

        });

        this.updateUIBaseFontSize ();

        // Listen to the night mode property, add a psuedo class when it is enabled.
        Environment.nightModeProperty ().addListener ((val, oldv, newv) ->
        {

            box.pseudoClassStateChanged (StyleClassNames.NIGHT_MODE_PSEUDO_CLASS, newv);

        });

        this.getIcons ().addAll (Environment.getWindowIcons ());

        this.initStyle (StageStyle.UNIFIED);

        // Hide the window by default, this allows subclasses to set things up.
        this.hide ();

    }

    public static class Builder implements IBuilder<Builder, Viewer>
    {

        private Node content = null;
        private String styleName = null;
        private String panelId = null;
        private StringProperty title = null;
        private Supplier<Set<Node>> headerControlsSupplier = null;

        public Builder headerControls (final Set<Node> items)
        {

            this.headerControlsSupplier = new Supplier<Set<Node>> ()
            {

                @Override
                public Set<Node> get ()
                {

                    return items;

                }

            };

            return this;

        }

        public Builder headerControls (Supplier<Set<Node>> items)
        {

            this.headerControlsSupplier = items;
            return this;

        }

        public Builder content (Node c)
        {

            this.content = c;
            return this;

        }

        public Builder styleClassName (String name)
        {

            this.styleName = name;
            return this;

        }

        public Builder title (String... ids)
        {

            return this.title (getUILanguageStringProperty (ids));

        }

        public Builder title (List<String> prefix,
                              String... ids)
        {

            return this.title (getUILanguageStringProperty (Utils.newList (prefix,ids)));

        }

        public Builder title (StringProperty prop)
        {

            this.title = prop;
            return this;

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        @Override
        public Viewer build ()
        {

            return new Viewer (this);

        }

    }

    public void removeStyleSheet (URL url)
    {

        this.getScene ().getStylesheets ().remove (url.toExternalForm ());

    }

    public void addStyleSheet (URL url)
    {

        this.getScene ().getStylesheets ().add (url.toExternalForm ());

    }

    public Node getContent ()
    {

        return this.content;

    }

    public String getStyleClassName ()
    {

        return this.styleName;

    }

    public Header getHeader ()
    {

        return this.header;

    }

    public static class ViewerEvent extends Event
    {

        public static final EventType<ViewerEvent> CLOSE_EVENT = new EventType<> ("viewer.close");
        public static final EventType<ViewerEvent> SHOW_EVENT = new EventType<> ("viewer.show");

        private Viewer viewer = null;

        public ViewerEvent (Viewer                 panel,
                            EventType<ViewerEvent> type)
        {

            super (type);

            this.viewer = viewer;

        }

    }


}
