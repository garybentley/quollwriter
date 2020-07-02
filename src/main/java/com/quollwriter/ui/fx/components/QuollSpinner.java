package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.event.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollSpinner extends Spinner<Integer>
{

    private QuollSpinner (Builder b)
    {

        if (b.tooltip != null)
        {

            UIUtils.setTooltip (this,
                                b.tooltip);

        }

        this.getStyleClass ().add (Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);
        this.setEditable (b.editable);

        this.setValueFactory (new SpinnerValueFactory.IntegerSpinnerValueFactory (b.min,
                                                              b.max,
                                                              b.initialValue,
                                                              b.stepBy));

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

    }

    public static QuollSpinner.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollSpinner>
    {

        private StringProperty tooltip = null;
        private String styleName = null;
        private int min = 0;
        private int max = Integer.MAX_VALUE;
        private int initialValue = 0;
        private int stepBy = 1;
        private boolean editable = true;

        private Builder ()
        {

        }

        public Builder stepBy (int s)
        {

            this.stepBy = s;
            return this;

        }

        public Builder initialValue (int v)
        {

            this.initialValue = v;
            return this;

        }

        public Builder min (int m)
        {

            this.min = m;
            return this;

        }

        public Builder max (int m)
        {

            this.max = m;
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

        public Builder editable (boolean v)
        {

            this.editable = v;
            return this;

        }

        @Override
        public QuollSpinner build ()
        {

            return new QuollSpinner (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
