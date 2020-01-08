package com.quollwriter.ui.fx.components;

import java.net.*;
import java.util.*;
import java.util.function.*;

import javafx.css.*;
import javafx.collections.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.value.*;
import javafx.scene.text.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.data.PropertyBinder;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class Viewer extends Stage implements Stateful
{

    public static final int DEFAULT_WINDOW_WIDTH = 800;
	public static final int DEFAULT_WINDOW_HEIGHT = 800;

    private Node content = null;
    private Header header = null;
    private String styleName = null;
    private StringProperty titleProp = null;
    private Supplier<Set<Node>> headerControlsSupplier = null;
    private IPropertyBinder binder = new PropertyBinder ();

    private Viewer (Builder b)
    {

        this.binder.addSetChangeListener (Environment.getStyleSheets (),
                                          ev ->
        {

            if (ev.wasAdded ())
            {

                this.addStyleSheet (ev.getElementAdded ());

            }

            if (ev.wasRemoved ())
            {

                this.removeStyleSheet (ev.getElementRemoved ());

            }

        });

        this.addEventHandler (Viewer.ViewerEvent.CLOSE_EVENT,
                              ev ->
        {

            this.binder.dispose ();

        });

        this.addEventHandler (WindowEvent.WINDOW_HIDDEN,
                              ev ->
        {

            this.fireEvent (new Viewer.ViewerEvent (this,
                                                    Viewer.ViewerEvent.CLOSE_EVENT));

        });

        this._init (b);

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        if (s != null)
        {

            Integer wh = s.getAsInt (Constants.WINDOW_HEIGHT_PROPERTY_NAME);

            if (wh == null)
            {

                wh = DEFAULT_WINDOW_HEIGHT;

            }

            Integer ww = s.getAsInt (Constants.WINDOW_WIDTH_PROPERTY_NAME);

            if (ww == null)
            {

                ww = DEFAULT_WINDOW_WIDTH;

            }

            this.setHeight (wh);
            this.setWidth (ww);

            Integer y = s.getAsInt (Constants.WINDOW_TOP_LOCATION_PROPERTY_NAME);

            if ((y != null)
                &&
                (y > 0)
               )
            {

                this.setY (y);

            }

            Integer x = s.getAsInt (Constants.WINDOW_LEFT_LOCATION_PROPERTY_NAME);

            if ((x != null)
                &&
                (x > 0)
               )
            {

                this.setX (x);

            }

            this.setMaximized (s.getAsBoolean (Constants.WINDOW_MAXIMIZED_PROPERTY_NAME));

        }

    }

    private void updateUIBaseFontAndSize ()
    {

        Font f = UserProperties.getUIBaseFont ();

        if (f == null)
        {

            f = Font.getDefault ();

        }

        this.getScene ().getRoot ().setStyle (String.format ("-fx-font-size: %1$spt; -fx-font-family: \"%2$s\"",
                                                             UserProperties.getUIBaseFontSize (),
                                                             f.getName ()));

    }

    @Override
    public State getState ()
    {

        State s = new State ();
        s.set (Constants.WINDOW_HEIGHT_PROPERTY_NAME,
               this.getHeight ());
        s.set (Constants.WINDOW_WIDTH_PROPERTY_NAME,
               this.getWidth ());
        s.set (Constants.WINDOW_TOP_LOCATION_PROPERTY_NAME,
               this.getY ());
        s.set (Constants.WINDOW_LEFT_LOCATION_PROPERTY_NAME,
               this.getX ());
        s.set (Constants.WINDOW_MAXIMIZED_PROPERTY_NAME,
               this.isMaximized ());

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
            //.controls (b.headerControlsSupplier.get ())
            .toolbar (b.headerToolbar)
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

        Environment.getStyleSheets ().stream ()
            .forEach (u ->
            {

                this.addStyleSheet (u);

            });

        this.binder.addChangeListener (UserProperties.uiBaseFontSizeProperty (),
                                       (pr, oldv, newv) ->
        {

            this.updateUIBaseFontAndSize ();

        });

        this.binder.addChangeListener (UserProperties.uiBaseFontProperty (),
                                       (pr, oldv, newv) ->
        {

            this.updateUIBaseFontAndSize ();

        });

        this.updateUIBaseFontAndSize ();

        // Listen to the night mode property, add a psuedo class when it is enabled.
        this.binder.addChangeListener (Environment.nightModeProperty (),
                                       (val, oldv, newv) ->
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
        private ToolBar headerToolbar = null;

        public Builder headerToolbar (ToolBar tb)
        {

            this.headerToolbar = tb;
            return this;

        }

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
        public static final EventType<ViewerEvent> FULL_SCREEN_ENTERED_EVENT = new EventType<> ("viewer.fullscreen.entered");
        public static final EventType<ViewerEvent> FULL_SCREEN_EXITED_EVENT = new EventType<> ("viewer.fullscreen.exited");

        private Viewer viewer = null;

        public ViewerEvent (Viewer                 panel,
                            EventType<ViewerEvent> type)
        {

            super (type);

            this.viewer = viewer;

        }

    }


}
