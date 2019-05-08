package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.text.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ShowObjectSelectPopup<T> extends PopupContent
{

    private Builder<T> builder = null;

    private ShowObjectSelectPopup (Builder<T> b)
    {

        super (b.viewer);

        this.builder = b;

        final ShowObjectSelectPopup _this = this;

        // We don't need to have this reactive to changes, maybe add later.
        VBox vb = new VBox ();
        vb.getStyleClass ().add (StyleClassNames.ITEMS);

        vb.getChildren ().addAll (b.objs.stream ()
            .map (obj -> b.cellProvider.apply (obj, this))
            .collect (Collectors.toList ()));

        this.getChildren ().add (new ScrollPane (vb));

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (builder.title)
            .styleClassName (builder.styleName)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (builder.popupId)
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.requestFocus ();

        return p;

    }

    /**
     * Get a builder.
     *
     * @returns A new builder.
     */
    public static <T> ShowObjectSelectPopup.Builder<T> builder ()
    {

        return new Builder<> ();

    }

    public static class Builder<T> implements IBuilder<Builder<T>, ShowObjectSelectPopup<T>>
    {

        private StringProperty title = null;
        private StringProperty desc = null;
        private String styleName = null;
        private BiFunction<T, ShowObjectSelectPopup, Node> cellProvider = null;
        private String popupId = null;
        private AbstractViewer viewer = null;
        private Set<T> objs = null;

        private Builder ()
        {

        }

        @Override
        public ShowObjectSelectPopup<T> build ()
        {

            return new ShowObjectSelectPopup<> (this);

        }

        @Override
        public Builder<T> _this ()
        {

            return this;

        }

        public Builder<T> objects (Set<T> objs)
        {

            this.objs = objs;
            return this;

        }

        public Builder<T> withViewer (AbstractViewer v)
        {

            this.viewer = v;
            return this;

        }

        public Builder<T> popupId (String id)
        {

            this.popupId = id;
            return this;

        }

        public Builder<T> cellProvider (BiFunction<T, ShowObjectSelectPopup, Node> prov)
        {

            this.cellProvider = prov;
            return this;

        }

        public Builder<T> styleClassName (String n)
        {

            this.styleName = n;
            return this;

        }

        public Builder<T> title (StringProperty prop)
        {

            this.title = prop;
            return this;

        }

        public Builder<T> title (List<String> prefix,
                                 String...    ids)
        {

            return this.title (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder<T> title (String... ids)
        {

            return this.title (getUILanguageStringProperty (ids));

        }

        public Builder<T> description (StringProperty prop)
        {

            this.desc = prop;
            return this;

        }

        public Builder<T> description (List<String> prefix,
                                       String...    ids)
        {

            return this.description (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder<T> description (String... ids)
        {

            return this.description (getUILanguageStringProperty (ids));

        }

    }

}
