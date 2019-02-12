package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;

import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class QuollPopup extends Popup
{

    private SimpleObjectProperty<Header> headerProp = null;
    private Runnable onClose = null;

    private QuollPopup (Builder b)
    {

        this.setAutoFix (true);

        Set<Node> controls = new HashSet<> ();

        if (b.controls != null)
        {

            controls.addAll (b.controls);

        }

        if (b.withClose)
        {

            QuollButton close = QuollButton.builder ()
                .styleClassName (StyleClassNames.CLOSE)
                .tooltip (actions,clicktoclose)
                .onAction (ev -> this.hide ())
                .build ();

            controls.add (close);

        }

        if (b.hideOnEscape)
        {

            this.setHideOnEscape (true);

        }

        this.onClose = b.onClose;
        this.setOnCloseRequest (ev -> this._close ());

        Header h = Header.builder ()
            .title (b.title)
            .styleClassName (b.styleName)
            .controls (controls)
            .build ();

        this.headerProp = new SimpleObjectProperty<> (h);

        this.getContent ().add (h);
        this.getContent ().add (b.content);

    }

    public ObjectProperty<Header> headerProperty ()
    {

        return this.headerProp;

    }

    /**
     * Get a builder to create a new QuollPopup.
     *
     * Usage: QuollPopup.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static QuollPopup.Builder builder ()
    {

        return new Builder ();

    }

    private void _close ()
    {

        if (this.onClose != null)
        {

            UIUtils.runLater (this.onClose);

        }

    }

    public void close ()
    {

        this.hide ();

        this._close ();

    }

    public static class Builder implements IBuilder<Builder, QuollPopup>
    {

        private StringProperty title = null;
        private String styleName = null;
        private Set<Node> controls = null;
        private Node content = null;
        private boolean withClose = false;
        private boolean hideOnEscape = false;
        private Runnable onClose = null;

        private Builder ()
        {

        }

        @Override
        public QuollPopup build ()
        {

            return new QuollPopup (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder onClose (Runnable r)
        {

            this.onClose = r;
            return this;

        }

        public Builder hideOnEscape (boolean v)
        {

            this.hideOnEscape = v;
            return this;

        }

        public Builder withClose (boolean v)
        {

            this.withClose = v;
            return this;

        }

        public Builder content (Node c)
        {

            this.content = c;
            return this;

        }

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

    }


}
