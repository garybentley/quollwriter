package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.*;
import javafx.scene.image.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.events.*;

public class StarBar extends HBox
{

    //public static final String RATING = StarBar.class.getName () + "/rating";

    private IntegerProperty valueProp = null;

    private StarBar (Builder b)
    {

        this.getStyleClass ().add (StyleClassNames.STARBAR);
        this.valueProp = new SimpleIntegerProperty (0);

        for (int i = 0; i < b.maxlvl; i++)
        {

            this.getChildren ().add (this.createButton (i,
                                                        b));

        }

        this.valueProp.addListener ((pr, oldv, newv) ->
        {

            if (oldv != newv)
            {

                this.setValue (newv.intValue ());

            }

        });

        this.setValue (b.value);

    }

    private Node createButton (int     i,
                               Builder b)
    {

        IconBox h = IconBox.builder ()
            .iconName (StyleClassNames.STAR)
            .build ();
        Pane but = h.getPane ();
        but.getStyleClass ().add ((i + 1) + "");

        UIUtils.setTooltip (but,
                            b.tooltip);
        h.setUserData (i + 1);

        but.addEventHandler (MouseEvent.MOUSE_ENTERED,
                             ev ->
        {

            this.setTempValue (i + 1);

        });

        but.addEventHandler (MouseEvent.MOUSE_EXITED,
                             ev ->
        {

            this.setTempValue (0);
            this.setValue (this.valueProp.getValue ());

        });

        but.addEventHandler (MouseEvent.MOUSE_PRESSED,
                             ev ->
        {

            if ((i + 1) == this.valueProp.getValue ())
            {

                // This is a toggle, switch off rating.
                this.setValue (0);
                this.setTempValue (0);
                return;

            }

            this.setValue (i + 1);

        });

        return h;

    }

    private void setTempValue (int v)
    {

        this.getChildren ().stream ()
            .forEach (it ->
            {

                Integer in = (Integer) it.getUserData ();

                it.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, false);

                if (in <= v)
                {

                    it.pseudoClassStateChanged (StyleClassNames.TEMP_SELECTED_PSEUDO_CLASS, true);

                } else {

                    it.pseudoClassStateChanged (StyleClassNames.TEMP_SELECTED_PSEUDO_CLASS, false);

                }

            });

    }

    public IntegerProperty valueProperty ()
    {

        return this.valueProp;

    }

    public void setValue (int v)
    {

        this.valueProp.setValue (v);

        this.getChildren ().stream ()
            .forEach (it ->
            {

                Integer in = (Integer) it.getUserData ();

                if (in.intValue () <= v)
                {

                    it.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, true);

                } else {

                    it.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, false);

                }

            });

    }

    /**
     * Get a builder to create a new StarBar.
     *
     * @returns A new builder.
     */
    public static Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, StarBar>
    {

        private int maxlvl = 5;
        private int value = 0;
        private StringProperty tooltip = null;

        private Builder ()
        {

        }

        public Builder tooltip (StringProperty t)
        {

            this.tooltip = t;
            return this;

        }

        public Builder value (int v)
        {

            this.value = v;
            return this;

        }

        public Builder maxLevel (int v)
        {

            this.maxlvl = v;
            return this;

        }

        @Override
        public StarBar build ()
        {

            return new StarBar (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
