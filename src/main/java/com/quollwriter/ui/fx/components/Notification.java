package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.concurrent.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class Notification extends HBox
{

    private int duration = 0;
    private Button close = null;
    private Runnable onRemove = null;
    private Node content = null;
    private AbstractViewer viewer = null;
    private ScheduledFuture timerId = null;

    private Notification (Builder builder)
    {

        if (builder.viewer == null)
        {

            throw new IllegalArgumentException ("No viewer provided.");

        }

        UIUtils.addStyleSheet (this,
                               Constants.COMPONENT_STYLESHEET_TYPE,
                               StyleClassNames.NOTIFICATION);
        if (builder.styleSheet != null)
        {

            this.getStylesheets ().add (builder.styleSheet);

        }

        this.getStyleClass ().add (StyleClassNames.NOTIFICATION);
        this.getStyleClass ().add (builder.styleName);

        this.duration = builder.duration;
        this.onRemove = builder.onRemove;
        this.content = builder.content;
        this.viewer = builder.viewer;

        HBox h = new HBox ();
        h.getStyleClass ().add (StyleClassNames.ICONBOX);
        Pane p = new Pane ();
        HBox.setHgrow (p,
                       Priority.NEVER);
        p.getStyleClass ().add (builder.styleName + StyleClassNames.ICON_SUFFIX);
        p.getStyleClass ().add (StyleClassNames.ICON);
        h.getChildren ().add (p);
        h.managedProperty ().bind (h.visibleProperty ());
        HBox.setHgrow (h,
                       Priority.NEVER);

        builder.content.getStyleClass ().add (StyleClassNames.CONTENT);
        HBox.setHgrow (builder.content,
                       Priority.ALWAYS);

       QuollToolBar toolbar = QuollToolBar.builder ()
            .controls (builder.controls)
            .build ();

        this.close = QuollButton.builder ()
            .styleClassName (StyleClassNames.CLOSE)
            .tooltip (getUILanguageStringProperty (notifications,remove,tooltip))
            .onAction (ev ->
            {

                this.removeNotification ();

            })
            .build ();

        toolbar.getItems ().add (this.close);

        this.getChildren ().addAll (h, builder.content, toolbar);

    }

    /**
     * Get a builder to create a new header.
     *
     * Usage: Header.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static Notification.Builder builder ()
    {

        return new Builder ();

    }

    public void init ()
    {

        final Notification _this = this;

        if (this.timerId != null)
        {

            this.restartTimer ();

            return;

        }

        if (this.duration > 0)
        {

            this.restartTimer ();

        }

    }

    public void restartTimer ()
    {

        final Notification _this = this;

        if (this.timerId != null)
        {

            this.timerId.cancel (false);

        }

        this.timerId = this.viewer.schedule (UIUtils.createRunLater (() -> { System.out.println ("HERE"); _this.removeNotification ();}),
                                             this.duration * Constants.SEC_IN_MILLIS,
                                             -1);

    }

    public void removeNotification ()
    {

        if (this.timerId != null)
        {

            this.timerId.cancel (false);

        }

        this.viewer.removeNotification (this);

        if (this.onRemove != null)
        {

            UIUtils.runLater (this.onRemove);

        }

    }

    public static class Builder implements IBuilder<Builder, Notification>
    {

        private Node content = null;
        private String styleName = null;
        private Set<Node> controls = new LinkedHashSet<> ();
        private AbstractViewer viewer = null;
        private int duration = 0;
        private Runnable onRemove = null;
        private String styleSheet = null;

        private Builder ()
        {

        }

        @Override
        public Notification build ()
        {

            return new Notification (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder styleSheet (String u)
        {

            this.styleSheet = u;
            return this;

        }

        public Builder withControl (Node c)
        {

            this.controls.add (c);

            return this;

        }

        public Builder controls (Set<? extends Node> c)
        {

            if (c != null)
            {

                this.controls.addAll (c);

            }

            return this;

        }

        public Builder styleName (String n)
        {

            this.styleName = n;

            return this;

        }

        public Builder content (Node c)
        {

            this.content = c;

            return this;

        }

        public Builder duration (int secs)
        {

            this.duration = secs;

            return this;

        }

        public Builder onRemove (Runnable r)
        {

            this.onRemove = r;

            return this;

        }

        public Builder inViewer (AbstractViewer viewer)
        {

            this.viewer = viewer;

            return this;

        }

    }

}
