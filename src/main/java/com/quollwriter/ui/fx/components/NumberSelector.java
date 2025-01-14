package com.quollwriter.ui.fx.components;

import java.util.function.*;

import javafx.scene.layout.*;
import javafx.scene.control.*;

import com.quollwriter.ui.fx.*;
import javafx.beans.property.*;

public class NumberSelector extends HBox
{

    private IntegerProperty valueProp = null;
    private Slider slider = null;
    private boolean ignoreUpdates = false;

    public NumberSelector (Builder b)
    {

        final NumberSelector _this = this;

        this.getStyleClass ().add (StyleClassNames.TYPE);

        if (b.styleClassName != null)
        {

            this.getStyleClass ().add (b.styleClassName);

        }

        this.valueProp = new SimpleIntegerProperty ();

        this.slider = new Slider (b.min,
                                  b.max,
                                  b.init);

        Spinner<Integer> spinner = new Spinner<> (new SpinnerValueFactory.IntegerSpinnerValueFactory (b.min, b.max, b.init));

        spinner.getStyleClass ().add (Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);
        spinner.setEditable (true);

        HBox.setHgrow (slider, Priority.ALWAYS);
        this.getChildren ().addAll (slider, spinner);

        slider.valueProperty ().addListener ((p, oldv, newv) ->
        {

            if (_this.ignoreUpdates)
            {

                return;

            }

            _this.valueProp.setValue (newv);

        });

        spinner.valueProperty ().addListener ((p, oldv, newv) ->
        {

            if (_this.ignoreUpdates)
            {

                return;

            }

            _this.valueProp.setValue (newv);

        });

        this.valueProp.addListener ((p, oldv, newv) ->
        {

            try
            {

                _this.ignoreUpdates = true;

                slider.setValue (newv.doubleValue ());
                spinner.valueFactoryProperty ().get ().setValue (newv.intValue ());

                if (b.onValueChanged != null)
                {

                    b.onValueChanged.accept (oldv.intValue (),
                                             newv.intValue ());

                }

            } finally {

                _this.ignoreUpdates = false;

            }

        });

    }

    public void setValue (int v)
    {

        this.valueProp.setValue (v);

    }

    public IntegerProperty valueProperty ()
    {

        return this.valueProp;

    }

    public static NumberSelector.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, NumberSelector>
    {

        private String styleClassName = null;
        private int min = 0;
        private int max = Integer.MAX_VALUE;
        private int init = 0;
        private BiConsumer<Integer, Integer> onValueChanged = null;

        private Builder ()
        {

        }

        @Override
        public NumberSelector build ()
        {

            return new NumberSelector (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder styleClassName (String s)
        {

            this.styleClassName = s;
            return _this ();

        }

        public Builder min (int v)
        {

            this.min = v;
            return _this ();

        }

        public Builder max (int v)
        {

            this.max = v;
            return _this ();

        }

        public Builder initialValue (int v)
        {

            this.init = v;
            return _this ();

        }

        // Old, New values in the consumer
        public Builder onValueChanged (BiConsumer<Integer, Integer> l)
        {

            this.onValueChanged = l;
            return _this ();

        }

    }

}
