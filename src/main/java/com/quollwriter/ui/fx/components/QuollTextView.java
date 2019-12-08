package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.event.*;
import javafx.beans.value.*;
import javafx.beans.binding.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.web.*;

import org.fxmisc.flowless.*;
import org.fxmisc.wellbehaved.event.*;

import org.w3c.dom.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollTextView extends VBox
{

    //private TextArea text = null;
    private TextEditor text = null;

    private QuollTextView (Builder b)
    {

        this.getStyleClass ().add (StyleClassNames.QTEXTVIEW);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.viewer == null)
        {

            throw new IllegalArgumentException ("Viewer must be provided.");

        }

        this.text = new TextEditor (null,
                                    null,
                                    null);
        this.text.setEditable (false);

        //this.text = new TextArea ();
        //this.text.setWrapText (true);
        VBox.setVgrow (this.text,
                       Priority.ALWAYS);

        if (b.text != null)
        {

            this.text.setText (b.text);

        }

        this.getChildren ().addAll (this.text);

    }

    public TextEditor getTextEditor ()
    {

        return this.text;

    }

    public void setText (StringWithMarkup text)
    {

        if (text == null)
        {

            this.setText ("");

        } else {

            this.setText (text);

        }

    }

    public void setText (String text)
    {

        this.text.setText (new StringWithMarkup (text));

    }

    public String getText ()
    {

        return this.text.getText ();

    }

    public StringWithMarkup getTextWithMarkup ()
    {

        return this.text.getTextWithMarkup ();

    }

    /**
     *
     * @returns A new builder.
     */
    public static QuollTextView.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollTextView>
    {

        private StringWithMarkup text = null;
        private String styleName = null;
        private AbstractViewer viewer = null;

        private Builder ()
        {

        }

        public Builder withViewer (AbstractViewer viewer)
        {

            this.viewer = viewer;
            return this;

        }

        public Builder text (StringWithMarkup text)
        {

            this.text = text;
            return this;

        }

        public Builder text (String text)
        {

            this.text = new StringWithMarkup (text);
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;
            return this;

        }

        @Override
        public QuollTextView build ()
        {

            return new QuollTextView (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
