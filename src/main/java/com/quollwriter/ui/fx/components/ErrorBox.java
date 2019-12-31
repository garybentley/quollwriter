package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.stream.*;

import javafx.beans.property.*;
import javafx.scene.layout.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

public class ErrorBox extends VBox
{

    private AbstractViewer viewer = null;

    private ErrorBox (Builder b)
    {

        this.getStyleClass ().add (StyleClassNames.ERRORS);
        this.managedProperty ().bind (this.visibleProperty ());
        this.setVisible (false);
        this.viewer = b.viewer;

        if (b.errors != null)
        {

            this.setErrors (b.errors);

        }

    }

    public void setErrors (Set<StringProperty> errs)
    {

        this.getChildren ().clear ();

        if (errs == null)
        {

            return;

        }

        QuollTextView tv = QuollTextView.builder ()
            .withViewer (this.viewer)
            .text (errs.stream ()
                    .map (p ->
                    {

                        return String.format ("<li>%1$s</li>",
                                              p.getValue ());

                    })
                    .collect (Collectors.joining ("", "<ul class='errors'>", "</ul>")))
            .build ();

        this.getChildren ().add (tv);

    }

    public static ErrorBox.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, ErrorBox>
    {

        private Set<StringProperty> errors = null;
        private AbstractViewer viewer = null;

        private Builder ()
        {

        }

        public Builder withViewer (AbstractViewer viewer)
        {

            this.viewer = viewer;
            return _this();

        }

        public Builder errors (Set<StringProperty> errors)
        {

            this.errors = errors;
            return _this ();

        }

        @Override
        public ErrorBox build ()
        {

            return new ErrorBox (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
