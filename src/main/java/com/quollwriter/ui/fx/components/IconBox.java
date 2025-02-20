package com.quollwriter.ui.fx.components;

import java.util.function.*;

import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;

public class IconBox extends HBox
{

    private HBox pane = null;
    private Consumer<IconBox> onNoImage = null;
    private Consumer<IconBox> onImagePresent = null;

    private IconBox (Builder b)
    {

        this.onNoImage = b.onNoImage;
        this.onImagePresent = b.onImagePresent;
        this.getStyleClass ().add (StyleClassNames.ICONBOX);
        this.pane = new HBox ();
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

        //this.pane.prefHeightProperty ().bind (this.heightProperty ());
        //this.pane.prefWidthProperty ().bind (this.widthProperty ());
        //this.prefHeightProperty ().bind (this.pane.prefHeightProperty ());
        //this.prefWidthProperty ().bind (this.pane.prefWidthProperty ());
        //this.maxWidthProperty ().bind (this.pane.prefWidthProperty ());
        //this.maxHeightProperty ().bind (this.pane.prefHeightProperty ());
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

        this.pane.pseudoClassStateChanged (StyleClassNames.IMAGE_PSEUDO_CLASS, false);
        this.pane.getChildren ().clear ();
        this.pane.prefWidthProperty ().unbind ();
        this.pane.prefHeightProperty ().unbind ();
        this.pane.maxWidthProperty ().unbind ();
        this.pane.maxHeightProperty ().unbind ();

        if (im == null)
        {

            this.pane.setPrefWidth (Region.USE_PREF_SIZE);
            this.pane.setPrefHeight (Region.USE_PREF_SIZE);
            this.pane.setBackground (null);

            if (this.onNoImage != null)
            {

                this.onNoImage.accept (this);

            }

            return;

        }

        BackgroundImage bim = new BackgroundImage (im,
                                                  BackgroundRepeat.NO_REPEAT,
                                                  BackgroundRepeat.NO_REPEAT,
                                                  null,
                                                  null);
                                                  /*
                                                  new BackgroundSize (BackgroundSize.AUTO,
                                                                      BackgroundSize.AUTO,
                                                                      false,
                                                                      false,
                                                                      false, //true,
                                                                      true ));*/
                                                  //BackgroundPosition.DEFAULT,
                                                  //new BackgroundSize (100, 100, true, true, true, false));
/*
        this.pane.setBackground (new Background (bim));
        UIUtils.forceRunLater (() ->
        {
            UIUtils.printCSSProperties (this.pane);
            System.out.println ("EXCEP: " + bim.getImage ().isError ());
            System.out.println ("WIDTH: " + bim.getImage ().getRequestedHeight ());
        });
        this.pane.setPrefHeight (im.getHeight ());
        this.pane.setPrefWidth (im.getWidth ());
        this.pane.prefWidthProperty ().bind (this.widthProperty ());
        this.pane.prefHeightProperty ().bind (this.heightProperty ());
*/

        //OLD THIS WORKED!
        ImageView iv = new ImageView ();
        iv.setImage (im);
        iv.setPreserveRatio (true);
        iv.setSmooth (true);
        iv.setCache (true);

        this.pane.pseudoClassStateChanged (StyleClassNames.IMAGE_PSEUDO_CLASS, true);
        //iv.fitWidthProperty ().bind (this.widthProperty ());
        //this.pane.prefWidthProperty ().bind (iv.fitWidthProperty ());
        //this.pane.prefHeightProperty ().bind (iv.fitHeightProperty ());
        this.pane.getChildren ().add (iv);


        if (this.onImagePresent != null)
        {

            this.onImagePresent.accept (this);

        }

    }

    public HBox getPane ()
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
