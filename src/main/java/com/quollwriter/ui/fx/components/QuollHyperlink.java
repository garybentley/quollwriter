package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.control.*;
import javafx.event.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class QuollHyperlink extends Hyperlink
{

    private QuollHyperlink (Builder b)
    {

        if (b.label != null)
        {

            this.textProperty ().bind (b.label);

        }

        if (b.tooltip != null)
        {

            Tooltip t = new Tooltip ();
            t.textProperty ().bind (b.tooltip);

            this.setTooltip (t);

        }

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.onAction != null)
        {

            this.setOnAction (b.onAction);

        }

    }

    /**
     * Get a builder to create a new hyperlink.
     *
     * Usage: QuollHyperlink.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static QuollHyperlink.Builder builder ()
    {

        return new Builder ();

    }

    public static QuollHyperlink createLink (AbstractViewer viewer,
                                             StringProperty label,
                                             String         url)
    {

        return QuollHyperlink.builder ()
            .label (label)
            .onAction (ev ->
            {

                UIUtils.openURL (viewer,
                                 viewer,
                                 url);

            })
            .build ();

    }

    public static class Builder implements IBuilder<Builder, QuollHyperlink>
    {

        private StringProperty label = null;
        private String styleName = null;
        private StringProperty tooltip = null;
        private EventHandler<ActionEvent> onAction = null;

        private Builder ()
        {

        }

        @Override
        public QuollHyperlink build ()
        {

            return new QuollHyperlink (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder onAction (EventHandler<ActionEvent> onAction)
        {

            this.onAction = onAction;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;

            return this;

        }

        public Builder tooltip (StringProperty prop)
        {

            this.tooltip = prop;

            return this;

        }

        public Builder tooltip (List<String> prefix,
                                String...    ids)
        {

            this.tooltip = getUILanguageStringProperty (Utils.newList (prefix, ids));
            return this;

        }

        public Builder tooltip (String... ids)
        {

            this.tooltip = getUILanguageStringProperty (ids);
            return this;

        }

        public Builder label (StringProperty prop)
        {

            this.label = prop;
            return this;

        }

        public Builder label (List<String> prefix,
                              String...    ids)
        {

            return this.label (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder label (String... ids)
        {

            return this.label (getUILanguageStringProperty (ids));

        }

    }

}
