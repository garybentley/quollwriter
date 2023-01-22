package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.event.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollRadioButton extends RadioButton
{

    private QuollRadioButton (Builder b)
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

        if (b.userProp != null)
        {

            this.setSelected (UserProperties.getAsBoolean (b.userProp));

            this.selectedProperty ().addListener ((pr, oldv, newv) ->
            {

                UserProperties.set (b.userProp,
                                    this.isSelected ());

            });

        } else {

            this.setSelected (b.selected);

        }

        if (b.onAction != null)
        {

            this.addEventHandler (ActionEvent.ANY,
                                  b.onAction);

        }

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

    }

    public static QuollRadioButton.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollRadioButton>
    {

        private String styleName = null;
        private StringProperty label = null;
        private StringProperty tooltip = null;
        private boolean selected = false;
        private EventHandler<ActionEvent> onAction = null;
        private String userProp = null;

        private Builder ()
        {

        }

        @Override
        public QuollRadioButton build ()
        {

            return new QuollRadioButton (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder userProperty (String name)
        {

            this.userProp = name;
            return this;

        }

        public Builder styleClassName (String name)
        {

            this.styleName = name;
            return this;

        }

        public Builder onAction (EventHandler<ActionEvent> ev)
        {

            this.onAction = ev;
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

        public Builder selected (boolean v)
        {

            this.selected = v;
            return this;

        }

    }

}
