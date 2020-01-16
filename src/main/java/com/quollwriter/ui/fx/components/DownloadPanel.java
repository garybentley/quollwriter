package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class DownloadPanel extends VBox
{

    private ProgressBar prog = null;
    private QuollButton stop = null;
    private Runnable onStop = null;

    private DownloadPanel (Builder b)
    {

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.title != null)
        {

            this.getChildren ().add (QuollTextView.builder ()
            .withViewer (b.viewer)
            .text (b.title)
            .build ());

        }

        HBox h = new HBox ();
        h.getStyleClass ().add (StyleClassNames.PROGRESS);
        this.getChildren ().add (h);

        this.prog = new ProgressBar ();
        HBox.setHgrow (this.prog,
                       Priority.ALWAYS);
        h.getChildren ().add (prog);

        if (b.showStop)
        {

            this.onStop = b.onStop;

            this.stop = QuollButton.builder ()
                .styleClassName (StyleClassNames.STOP)
                .onAction (ev ->
                {

                    if (this.onStop != null)
                    {

                        this.onStop.run ();

                    }

                })
                .build ();
            h.getChildren ().add (this.stop);

        }

    }

    public void setOnStop (Runnable r)
    {

        this.onStop = r;

    }

    public void setProgress (double d)
    {

        this.prog.setProgress (d);

    }

    public Button getStopButton ()
    {

        return this.stop;

    }

    public static DownloadPanel.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, DownloadPanel>
    {

        private boolean showStop = false;
        private String styleName = null;
        private StringProperty title = null;
        private Runnable onStop = null;
        private AbstractViewer viewer = null;

        private Builder ()
        {

        }

        @Override
        public DownloadPanel build ()
        {

            return new DownloadPanel (this);

        }

        public Builder onStop (Runnable r)
        {

            this.onStop = r;
            return this;

        }

        public Builder title (StringProperty prop)
        {

            this.title = prop;
            return this;

        }

        public Builder title (List<String> prefix,
                              String...    ids)
        {

            return this.title (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder title (String... ids)
        {

            return this.title (getUILanguageStringProperty (ids));

        }

        public Builder withViewer (AbstractViewer v)
        {

            this.viewer = v;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;

            return this;

        }

        public Builder showStop (boolean v)
        {

            this.showStop = v;
            return this;

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
