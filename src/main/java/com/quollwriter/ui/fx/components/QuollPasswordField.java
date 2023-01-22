package com.quollwriter.ui.fx.components;

import java.util.*;
import java.io.*;

import javafx.stage.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollPasswordField extends HBox
{

    private PasswordField password1 = null;
    private PasswordField password2 = null;

    private QuollPasswordField (Builder b)
    {

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        this.getStyleClass ().add (StyleClassNames.PASSWORDS);

        Label pl1 = new Label ();
        pl1.textProperty ().bind (b.passwordLabel != null ? b.passwordLabel : getUILanguageStringProperty (newprojectpanel,labels,password));

        this.getChildren ().add (pl1);

        this.password1 = new PasswordField ();

        this.getChildren ().add (this.password1);

        Label pl2 = new Label ();
        pl2.textProperty ().bind (b.confirmLabel != null ? b.confirmLabel : getUILanguageStringProperty (newprojectpanel,labels,confirmpassword));

        this.getChildren ().add (pl2);

        this.password2 = new PasswordField ();

        this.getChildren ().add (this.password2);

    }

    public PasswordField getPasswordField1 ()
    {

        return this.password1;

    }

    public PasswordField getPasswordField2 ()
    {

        return this.password2;

    }

    public void setFieldsDisable (boolean v)
    {

        this.password1.setDisable (v);
        this.password2.setDisable (v);

    }

    public String getPassword1 ()
    {

        return this.password1.getText ();

    }

    public String getPassword2 ()
    {

        return this.password2.getText ();

    }

    public static QuollPasswordField.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollPasswordField>
    {

        private String styleName = null;
        private StringProperty passwordLabel = null;
        private StringProperty confirmLabel = null;

        private Builder ()
        {

        }

        public Builder confirmLabel (StringProperty l)
        {

            this.confirmLabel = l;
            return this;

        }

        public Builder passwordLabel (StringProperty l)
        {

            this.passwordLabel = l;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;
            return this;

        }

        @Override
        public QuollPasswordField build ()
        {

            return new QuollPasswordField (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
