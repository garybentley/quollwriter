package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.binding.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollTextArea extends VBox
{

    private Label maxlabel = null;
    private TextArea text = null;
    private Boolean autoGrabFocus = false;

    private QuollTextArea (Builder b)
    {

        final QuollTextArea _this = this;

        this.getStyleClass ().add (StyleClassNames.QTEXTAREA);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        this.text = new TextArea ();
        this.text.setWrapText (true);
        VBox.setVgrow (this.text,
                       Priority.ALWAYS);

        if (b.placeHolder != null)
        {

            this.text.promptTextProperty ().bind (b.placeHolder);

        }

        this.text.setEditable (true);

        if (b.text != null)
        {

            this.text.setText (b.text);

        }

        this.autoGrabFocus = b.autoGrabFocus;

        this.text.addEventHandler (MouseEvent.MOUSE_ENTERED,
                                   ev ->
        {

            if (_this.autoGrabFocus)
            {

                _this.text.requestFocus ();

            }

        });

        this.maxlabel = new Label ();
        this.maxlabel.getStyleClass ().add (StyleClassNames.MAX);

        this.maxlabel.setVisible (b.maxChars > 0);

        this.maxlabel.textProperty ().bind (Bindings.createStringBinding (() ->
        {

            String t = String.format (getUIString (textarea,maxchars),
                                      Environment.formatNumber (b.maxChars));

            int l = _this.text.getText ().length ();

            if (l > 0)
            {

                if (l > b.maxChars)
                {

                    t += String.format (getUIString (textarea,charsover),
                                        Environment.formatNumber (l - b.maxChars));
                    _this.maxlabel.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, true);

                } else {

                    t += String.format (getUIString (textarea,charsremaining),
                                        Environment.formatNumber ((b.maxChars - l)));
                    _this.maxlabel.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, false);

                }

            }

            return t;

        },
        Environment.uilangProperty (),
        this.text.textProperty ()));

        // TODO Add spell checker/synonym support.

        // TODO Add menu, copy/paste etc.

        this.getChildren ().addAll (this.text, this.maxlabel);

    }

    public void setText (String text)
    {

        this.text.setText (text);

    }

    public String getText ()
    {

        return this.text.getText ();

    }

    /**
     * Get a builder to create a new QuollPopup.
     *
     * Usage: QuollPopup.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static QuollTextArea.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollTextArea>
    {

        private StringProperty placeHolder = null;
        private String text = null;
        private int maxChars = -1;
        private String styleName = null;
        private Boolean autoGrabFocus = false;

        private Builder ()
        {

        }

        public Builder autoGrabFocus (Boolean v)
        {

            this.autoGrabFocus = v;
            return this;

        }

        public Builder text (String text)
        {

            this.text = text;
            return this;

        }

        public Builder placeholder (List<String> prefix,
                                    String...    ids)
        {

            return this.placeholder (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder placeholder (String... ids)
        {

            return this.placeholder (getUILanguageStringProperty (ids));

        }

        public Builder placeholder (StringProperty prop)
        {

            this.placeHolder = prop;
            return this;

        }

        public Builder maxChars (int c)
        {

            this.maxChars = c;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;
            return this;

        }

        @Override
        public QuollTextArea build ()
        {

            return new QuollTextArea (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
