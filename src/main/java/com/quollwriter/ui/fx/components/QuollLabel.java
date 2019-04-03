package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.control.*;
import javafx.event.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class QuollLabel extends Label
{

    private QuollLabel (Builder b)
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

    }

    /**
     * Get a builder to create a new label.
     *
     * Usage: QuollLabel.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static QuollLabel.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollLabel>
    {

        private StringProperty label = null;
        private String styleName = null;
        private StringProperty tooltip = null;

        private Builder ()
        {

        }

        @Override
        public QuollLabel build ()
        {

            return new QuollLabel (this);

        }

        @Override
        public Builder _this ()
        {

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
