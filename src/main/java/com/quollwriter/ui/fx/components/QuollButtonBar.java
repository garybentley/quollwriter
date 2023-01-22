package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.collections.*;

import com.quollwriter.ui.fx.*;

public class QuollButtonBar extends HBox
{

    public static final String DEFAULT_BUTTON_LAYOUT = "BXUIAYNOC";
    //public static final String APPLY_BUTTON_LAYOUT = "A";

    private ButtonBar buttonBar = null;

    private QuollButtonBar (Builder b)
    {

        b.buttons.stream ()
            .forEach (but -> ButtonBar.setButtonUniformSize (but, false));

        this.buttonBar = new ButtonBar (b.layout != null ? b.layout : DEFAULT_BUTTON_LAYOUT);
        this.buttonBar.getButtons ().addAll (b.buttons);

        this.getChildren ().add (this.buttonBar);
        this.getStyleClass ().add (StyleClassNames.BUTTONS);
        this.setSnapToPixel (true);

    }

    public ObservableList<Node> getButtons ()
    {

        return this.buttonBar.getButtons ();

    }

    public Button getCancelButton ()
    {

        if (this.buttonBar == null)
        {

            return null;

        }

        for (Node n : this.buttonBar.getButtons ())
        {

            if (!(n instanceof Button))
            {

                continue;

            }

            Button b = (Button) n;

            if (ButtonBar.getButtonData (b) == ButtonBar.ButtonData.CANCEL_CLOSE)
            {

                return b;

            }

        }

        return null;

    }

    public Button getConfirmButton ()
    {

        if (this.buttonBar == null)
        {

            return null;

        }

        for (Node n : this.buttonBar.getButtons ())
        {

            if (!(n instanceof Button))
            {

                continue;

            }

            Button b = (Button) n;

            if (ButtonBar.getButtonData (b) == ButtonBar.ButtonData.OK_DONE)
            {

                return b;

            }

        }

        return null;

    }

    public static QuollButtonBar.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollButtonBar>
    {

        private String styleName = null;
        private Set<Button> buttons = new LinkedHashSet<> ();
        private String layout = null;

        private Builder ()
        {

        }

        @Override
        public QuollButtonBar build ()
        {

            return new QuollButtonBar (this);

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

        public Builder layout (String layout)
        {

            this.layout = layout;
            return this;

        }

        public Builder buttons (Set<Button> buts)
        {

            this.buttons = buts;
            return this;

        }

        public Builder button (Button b)
        {

            this.buttons.add (b);
            return this;

        }

    }

}
