package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollCheckBox extends CheckBox
{

    private QuollCheckBox (Builder b)
    {

        if (b.label != null)
        {

            this.textProperty ().bind (b.label);

        }

        if (b.tooltip != null)
        {

            UIUtils.setTooltip (this,
                                b.tooltip);

        }

        this.setSelected (b.selected);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

    }

    public static QuollCheckBox.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollCheckBox>
    {

        private StringProperty label = null;
        private StringProperty tooltip = null;
        private String styleName = null;
        private Boolean selected = false;

        private Builder ()
        {

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

        public Builder label (StringProperty prop)
        {

            this.label = prop;
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

        public Builder tooltip (StringProperty prop)
        {

            this.tooltip = prop;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;
            return this;

        }

        public Builder selected (Boolean v)
        {

            this.selected = v;
            return this;

        }

        @Override
        public QuollCheckBox build ()
        {

            return new QuollCheckBox (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
