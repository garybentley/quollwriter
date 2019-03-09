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

    }

    @Override
    public State getState ()
    {

        return new State ();

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

        Scene s = new Scene (box);//, 790, 500);

		this.setScene (s);
        this.setMinWidth (300);
        this.setMinHeight (300);

        this.addStyleSheet (UserProperties.getUserStyleSheetURL ());

        // Listen to the night mode property, add a psuedo class when it is enabled.
        Environment.nightModeProperty ().addListener ((val, oldv, newv) ->
        {

            box.pseudoClassStateChanged (StyleClassNames.NIGHT_MODE_PSEUDO_CLASS, newv);

        });

        this.getIcons ().addAll (Environment.getWindowIcons ());

        this.initStyle (StageStyle.UNIFIED);
        this.setMinHeight (300);
		this.setMinWidth (300);

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
