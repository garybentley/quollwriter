package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollRadioButtons extends VBox
{

    private ToggleGroup group = null;
    private Set<RadioButton> buttons = null;

    private QuollRadioButtons (Builder b)
    {

        this.group = new ToggleGroup ();

        this.buttons = b.buttons;

        b.buttons.stream ()
            .forEach (but ->
            {

                but.setToggleGroup (this.group);
                this.getChildren ().add (but);

            });

        this.getStyleClass ().add (StyleClassNames.RADIOBUTTONS);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

    }

    public RadioButton getSelected ()
    {

        return (RadioButton) this.group.getSelectedToggle ();

    }

    public Set<RadioButton> getButtons ()
    {

        return this.buttons;

    }

    public static QuollRadioButtons.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollRadioButtons>
    {

        private String styleName = null;
        private Set<RadioButton> buttons = new LinkedHashSet<> ();

        private Builder ()
        {

        }

        @Override
        public QuollRadioButtons build ()
        {

            return new QuollRadioButtons (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder styleClassName (String name)
        {

            this.styleName = name;
            return this;

        }

        public Builder buttons (Set<RadioButton> buts)
        {

            this.buttons = buts;
            return this;

        }

        public Builder button (RadioButton b)
        {

            this.buttons.add (b);
            return this;

        }

    }

}
