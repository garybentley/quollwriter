package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.popups.*;

public class ColorSelectorSwatch extends Region
{

    private String popupId = UUID.randomUUID ().toString ();
    private ColorChooserPopup chooser = null;

    private ColorSelectorSwatch (Builder b)
    {

        if (b.initialColor != null)
        {

            this.setThisColor (b.initialColor);

        }

        this.getStyleClass ().add (StyleClassNames.COLORSWATCH);

        if (b.styleClass != null)
        {

            this.getStyleClass ().add (b.styleClass);

        }

        this.setOnMouseClicked (ev ->
        {

            if (ev.getButton () != MouseButton.PRIMARY)
            {

                return;

            }

            if (this.chooser == null)
            {

                this.chooser = new ColorChooserPopup (b.viewer,
                                                      b.initialColor,
                                                      true);
                this.chooser.getPopup ().setTitle (b.popupTitle);
                this.chooser.getPopup ().setPopupId (this.popupId);
                this.chooser.getChooser ().setOnColorSelected (eev ->
                {

                    Color c = this.chooser.getChooser ().colorProperty ().getValue ();

                    this.setThisColor (c);
                    this.chooser.close ();

                    if (b.onColorSelected != null)
                    {

                        b.onColorSelected.accept (c);

                    }

                });

            }

            this.chooser.setVisible (true);
            this.chooser.toFront ();
            this.chooser.show ();

        });

    }

    public void setColor (Color c)
    {

        this.setThisColor (c);

        if (this.chooser != null)
        {

            this.chooser.getChooser ().setColor (c);

        }

    }

    private void setThisColor (Color c)
    {

        this.setBackground (new Background (new BackgroundFill (c, null, null)));

    }

    public static ColorSelectorSwatch.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, ColorSelectorSwatch>
    {

        private String styleClass = null;
        private Color initialColor = null;
        private Consumer<Color> onColorSelected = null;
        private StringProperty popupTitle = null;
        private AbstractViewer viewer = null;

        private Builder ()
        {

        }

        @Override
        public ColorSelectorSwatch build ()
        {

            return new ColorSelectorSwatch (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder inViewer (AbstractViewer v)
        {

            this.viewer = v;
            return _this ();

        }

        public Builder styleClassName (String s)
        {

            this.styleClass = s;
            return _this ();

        }

        public Builder onColorSelected (Consumer<Color> col)
        {

            this.onColorSelected = col;
            return _this ();

        }

        public Builder initialColor (Color c)
        {

            this.initialColor = c;
            return _this ();

        }

        public Builder popupTitle (StringProperty t)
        {

            this.popupTitle = t;
            return _this ();

        }

    }

}
