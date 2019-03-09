package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollTextField extends TextField
{

    private QuollTextField (Builder b)
    {

        this.setEditable (true);

        if (b.text != null)
        {

            this.setText (b.text);

        }

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

    }

    public static QuollTextField.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollTextField>
    {

        private String text = null;
        private String styleName = null;
        private StringProperty placeHolder = null;

        private Builder ()
        {

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

        public Builder styleClassName (String n)
        {

            this.styleName = n;
            return this;

        }

        @Override
        public QuollTextField build ()
        {

            return new QuollTextField (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
