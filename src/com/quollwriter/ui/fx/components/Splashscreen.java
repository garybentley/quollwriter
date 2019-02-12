package com.quollwriter.ui.fx.components;

import java.net.*;

import javafx.application.*;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.animation.*;
import javafx.util.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

public class Splashscreen extends Stage
{

    public static final String STYLE_CLASS_NAME = "qw-splashscreen";
    public static final String IMAGE_CLASS_NAME = "qw-image";

    private ProgressBar progress = null;
    private ImageView image = null;
    private Group root = null;

    private Splashscreen (Builder b)
    {

        super (StageStyle.TRANSPARENT);

        this.root = new Group ();

        VBox content = new VBox ();
        content.getStyleClass ().add (STYLE_CLASS_NAME);

        this.root.getChildren ().addAll (content);

        Scene s = new Scene (this.root, 500, 500, javafx.scene.paint.Color.BLACK);
        //Environment.createScene (this.root);
        this.setScene (s);

        this.init ();

        if (b.imageView != null)
        {

            this.image = b.imageView;

            this.image.getStyleClass ().add (IMAGE_CLASS_NAME);
            content.getChildren ().add (this.image);

        }

        if (b.styleName != null)
        {

            content.getStyleClass ().add (b.styleName);

        }

        content.getChildren ().add (progress);

    }

    public Splashscreen ()
    {

        this.init ();

    }

    public void finish ()
    {

        this.updateProgress (100);
        this.fadeOut (1000);

    }

    public void updateProgress (double v)
    {

        final Splashscreen _this = this;

        UIUtils.runLater (() ->
        {

            double n = _this.progress.getProgress () + v;

            if (n > 100)
            {

                n = 100;

            }

            _this.progress.setProgress (n);

        });

    }

    public ObjectProperty<Image> imageProperty ()
    {

        return this.image.imageProperty ();

    }

    public DoubleProperty progressProperty ()
    {

        return this.progress.progressProperty ();

    }

    /**
     * Get a builder to create a new splashscreen.
     *
     * Usage: Splashscreen.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static Splashscreen.Builder builder ()
    {

        return new Builder ();

    }

    // GTODO Move to a util.
    public void fadeOut (int time)
    {

        final Splashscreen _this = this;

        UIUtils.runLater (() ->
        {

            FadeTransition ft = new FadeTransition (Duration.millis (time), this.root);
            ft.setFromValue (1.0);
            ft.setToValue (0);
            ft.play ();
            _this.close ();

        });

    }

    private void init ()
    {

        //this.getStyleClass ().add (STYLE_CLASS_NAME);

        this.progress = new ProgressBar (0);
        HBox.setHgrow (this.progress, Priority.ALWAYS);
        this.progress.setMaxWidth (Double.MAX_VALUE);

    }

    public static class Builder implements IBuilder<Builder, Splashscreen>
    {

        private ImageView imageView = null;
        private URL url = null;
        private Image image = null;
        private String styleName = null;

        private Builder ()
        {

        }

        @Override
        public Splashscreen build ()
                            throws Exception
        {

            if (this.image == null)
            {

                Image im = this.image;

                if (im == null)
                {

                    if (this.url != null)
                    {

                        im = new Image (url.toURI ().toString ());

                    }

                }

                this.imageView = new ImageView (im);

            }

            return new Splashscreen (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder image (Image im)
        {

            this.image = im;

            return this;

        }

        public Builder image (URL url)
        {

            this.url = url;

            return this;

        }

        public Builder image (ImageView iv)
        {

            this.imageView = iv;

            return this;

        }

        public Builder styleClassName (String style)
        {

            this.styleName = style;
            return this;

        }

    }

}
