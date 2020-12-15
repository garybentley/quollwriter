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
import javafx.geometry.*;

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

        if (vb.getChildren ().size () > 0)
        {

            vb.getChildren ().get (0).pseudoClassStateChanged (StyleClassNames.FIRST_PSEUDO_CLASS, true);
            vb.getChildren ().get (vb.getChildren ().size () - 1).pseudoClassStateChanged (StyleClassNames.LAST_PSEUDO_CLASS, true);

        }

        VBox content = new VBox ();
        this.getChildren ().add (content);

        if (b.above != null)
        {

            content.getChildren ().add (b.above);

        }

        content.getChildren ().add (new ScrollPane (vb));

        if (b.below != null)
        {

            content.getChildren ().add (b.below);

        }

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (builder.title)
            .styleClassName (builder.styleName)
            .styleSheet (StyleClassNames.OBJECTSELECT)
            .headerIconClassName (builder.headerIconClassName)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (builder.popupId)
            .removeOnClose (true)
            .withViewer (this.viewer)
            .showAt (builder.showAt,
                     builder.showWhere)
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
        private String headerIconClassName = null;
        private Node above = null;
        private Node below = null;
        private Node showAt = null;
        private Side showWhere = null;

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

        public Builder<T> showAt (Node n,
                                  Side where)
        {

            this.showAt = n;
            this.showWhere = where;
            return _this ();

        }

        public Builder<T> showAboveObjects (Node n)
        {

            this.above = n;
            return _this ();

        }

        public Builder<T> showBelowObjects (Node n)
        {

            this.below = n;
            return _this ();

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

        public Builder<T> headerIconClassName (String c)
        {

            this.headerIconClassName = c;
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
