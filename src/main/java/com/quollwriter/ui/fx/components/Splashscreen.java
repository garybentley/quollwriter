package com.quollwriter.ui.fx.components;

import java.net.*;
import java.util.stream.*;

import javafx.application.*;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.animation.*;
import javafx.util.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

public class Splashscreen extends Stage
{

    private ProgressBar progress = null;
    private ImageView image = null;

    private Splashscreen (Builder b)
    {

        super (StageStyle.TRANSPARENT);

        VBox content = new VBox ();
        content.getStyleClass ().add (StyleClassNames.SPLASHSCREEN);

        UIUtils.addStyleSheet (content,
                               Constants.COMPONENT_STYLESHEET_TYPE,
                               StyleClassNames.SPLASHSCREEN);

        Scene s = new Scene (content);
        s.setFill (Color.TRANSPARENT);

        s.getStylesheets ().add (UserProperties.getDefaultStyleSheetURL ().toExternalForm ());
        this.setScene (s);

        ImageView image = new ImageView ();
        image.getStyleClass ().add (StyleClassNames.IMAGE);
        content.getChildren ().add (image);

        if (b.styleName != null)
        {

            content.getStyleClass ().add (b.styleName);

        }

        this.progress = new ProgressBar (0);
        HBox.setHgrow (this.progress, Priority.ALWAYS);
        //this.progress.setMaxWidth (Double.MAX_VALUE);

        content.getChildren ().add (progress);

        this.getIcons ().addAll (Environment.getWindowIcons ());
        this.setTitle (Constants.QUOLL_WRITER_NAME);

        this.sizeToScene ();

        UIUtils.runLater (() ->
        {

            this.toFront ();

        });

        content.setOnMouseClicked (ev ->
        {

            //this.close ();

        });

    }

    public void finish ()
    {

        this.updateProgress (100);
        this.fadeOut (1000);

    }

    public void updateProgress (double v)
    {

        UIUtils.runLater (() ->
        {

            this.progress.setProgress (v);
/*
            double n = _this.progress.getProgress () + v;

            if (n > 100)
            {

                n = 100;

            }

            _this.progress.setProgress (n);
*/
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

            FadeTransition ft = new FadeTransition (Duration.millis (time), this.getScene ().getRoot ());
            ft.setFromValue (1.0);
            ft.setToValue (0);
            ft.setOnFinished (ev ->
            {

                _this.close ();

            });
            ft.play ();

        });

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
