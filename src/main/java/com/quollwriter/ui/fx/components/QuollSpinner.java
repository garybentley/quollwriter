package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.event.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.util.converter.*;
import java.text.*;
import java.util.function.*;

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

        //this.getStyleClass ().add (Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);
        this.getStyleClass ().add ("arrows-on-right-vertical");
        this.setEditable (b.editable);

        // Taken from: https://stackoverflow.com/a/29561005/1784362
        NumberFormat format = NumberFormat.getIntegerInstance();
        UnaryOperator<TextFormatter.Change> filter = c ->
        {
            if (c.isContentChange())
            {
                if (c.getControlNewText ().equals (""))
                {

                    return c;

                }
                ParsePosition parsePosition = new ParsePosition(0);
                // NumberFormat evaluates the beginning of the text
                format.parse(c.getControlNewText(), parsePosition);
                if (parsePosition.getIndex() == 0 ||
                        parsePosition.getIndex() < c.getControlNewText().length()) {
                    // reject parsing the complete text failed
                    return null;
                }
            }
            return c;
        };
        TextFormatter<Integer> formatter = new TextFormatter<Integer>(
                new IntegerStringConverter(), 0, filter);

        this.getEditor().setTextFormatter(formatter);

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
