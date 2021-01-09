package com.quollwriter.ui.fx.components;

import java.util.function.*;

import javafx.embed.swing.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;

public class IconBox extends HBox
{

    private Pane pane = null;
    private Consumer<IconBox> onNoImage = null;
    private Consumer<IconBox> onImagePresent = null;

    private IconBox (Builder b)
    {

        this.onNoImage = b.onNoImage;
        this.onImagePresent = b.onImagePresent;
        this.getStyleClass ().add (StyleClassNames.ICONBOX);
        this.pane = new Pane ();
        this.pane.getStyleClass ().add (StyleClassNames.ICON);

        if (b.styleClassName != null)
        {

            this.getStyleClass ().add (b.styleClassName);

        }

        if (b.iconName != null)
        {

            this.pane.getStyleClass ().addAll (b.iconName + StyleClassNames.ICON_SUFFIX);

        }
        this.getChildren ().add (this.pane);

        this.managedProperty ().bind (this.visibleProperty ());

        if ((b.image != null)
            &&
            (b.binder != null)
           )
        {

            b.binder.addChangeListener (b.image,
                                        (pr, oldv, newv) ->
            {

                this.setBackgroundImage (newv);

            });

        }

        if (b.image != null)
        {

            this.setBackgroundImage (b.image.getValue ());

        }

    }

    public void setImage (Image i)
    {

        if (i == null)
        {

            this.pane.setBackground (null);
            return;

        }

        this.setBackgroundImage (i);

    }

    public void setImage (ObjectProperty<Image> i,
                          IPropertyBinder       binder)
    {

        UIUtils.setBackgroundImage (this.pane,
                                    i,
                                    binder);

    }

    public void setIconName (String c)
    {

        String r = null;

        for (String s : this.pane.getStyleClass ())
        {

            if (s.endsWith (StyleClassNames.ICON_SUFFIX))
            {

                r = s;
                break;

            }

        }

        if (r != null)
        {

            this.pane.getStyleClass ().remove (r);

        }

        if (!c.endsWith (StyleClassNames.ICON_SUFFIX))
        {

            c += StyleClassNames.ICON_SUFFIX;

        }

        this.pane.getStyleClass ().add (c);

    }

    private void setBackgroundImage (Image im)
    {

        if (im == null)
        {

            this.pane.setBackground (null);

            if (this.onNoImage != null)
            {

                this.onNoImage.accept (this);

            }

            return;

        }
System.out.println ("HERE: " + im);
        this.pane.setBackground (new Background (new BackgroundImage (im, //SwingFXUtils.toFXImage (im, null),
                                                               BackgroundRepeat.NO_REPEAT,
                                                               BackgroundRepeat.NO_REPEAT,
                                                               BackgroundPosition.CENTER,
                                                               new BackgroundSize (100, 100, true, true, false, true))));

        if (this.onImagePresent != null)
        {

            this.onImagePresent.accept (this);

        }

    }

    public Pane getPane ()
    {

        return this.pane;

    }

    public static IconBox.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, IconBox>
    {

        private String styleClassName = null;
        private String iconName = null;
        private ObjectProperty<Image> image = null;
        private IPropertyBinder binder = null;
        private Consumer<IconBox> onNoImage = null;
        private Consumer<IconBox> onImagePresent = null;

        private Builder ()
        {

        }

        @Override
        public IconBox build ()
        {

            return new IconBox (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder onNoImage (Consumer<IconBox> o)
        {

            this.onNoImage = o;
            return _this ();

        }

        public Builder onImagePresent (Consumer<IconBox> o)
        {

            this.onImagePresent = o;
            return _this ();

        }

        public Builder binder (IPropertyBinder b)
        {

            this.binder = b;
            return _this ();

        }

        public Builder image (ObjectProperty<Image> im)
        {

            this.image = im;
            return _this ();

        }

        public Builder iconName (String s)
        {

            this.iconName = s;
            return _this ();

        }

        public Builder styleClassName (String s)
        {

            this.styleClassName = s;
            return _this ();

        }

    }

}
