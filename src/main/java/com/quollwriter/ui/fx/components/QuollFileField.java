package com.quollwriter.ui.fx.components;

import java.util.*;
import java.io.*;

import javafx.stage.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollFileField extends HBox
{

    public enum Type
    {
        file,
        directory;
    }

    private QuollTextField text = null;
    private ObjectProperty<File> fileProp = null;
    private StringProperty chooserTitle = null;
    private AbstractViewer viewer = null;
    private Type limitTo = Type.file;

    private QuollFileField (Builder b)
    {

        if (b.viewer == null)
        {

            throw new IllegalArgumentException ("Viewer must be provided.");

        }

        final QuollFileField _this = this;

        this.fileProp = new SimpleObjectProperty<> ();

        this.viewer = b.viewer;
        this.chooserTitle = b.chooserTitle;
        this.limitTo = b.limitTo;

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        this.getStyleClass ().add (StyleClassNames.FILEFIND);

        this.text = QuollTextField.builder ()
            .placeholder (b.placeholder)
            .build ();

        HBox.setHgrow (this.text,
                       Priority.ALWAYS);
        this.text.setEditable (false);

        this.fileProp.addListener ((v, oldv, newv) ->
        {

            if ((newv == null)
                &&
                (b.initialFile == null)
               )
            {

                _this.text.setText ("");

            } else {

                _this.text.setText (newv.getPath ());

            }

        });

        this.fileProp.setValue (b.initialFile);

        this.text.setOnMouseClicked (ev ->
        {

            ev.consume ();

            _this.showChooser ();

        });

        this.getChildren ().add (this.text);

        this.getChildren ().add (QuollButton.builder ()
            .tooltip (b.buttonTooltip)
            .onAction (ev ->
            {

                ev.consume ();
                _this.showChooser ();

            })
            .build ());

    }

    private void showChooser ()
    {

        final QuollFileField _this = this;

        if (this.limitTo == Type.file)
        {

            // TODO

        }

        if (this.limitTo == Type.directory)
        {

            DirectoryChooser d = new DirectoryChooser ();
            d.titleProperty ().bind (this.chooserTitle);
            d.setInitialDirectory (this.fileProp.getValue ());
            File f = d.showDialog (this.viewer.getViewer ());

            _this.fileProp.setValue (f);

        }

    }

    public File getFile ()
    {

        return this.fileProp.getValue ();

    }

    public ObjectProperty<File> fileProperty ()
    {

        return this.fileProp;

    }

    public static QuollFileField.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollFileField>
    {

        private String styleName = null;
        private StringProperty placeholder = null;
        private StringProperty buttonTooltip = null;
        private File initialFile = null;
        private AbstractViewer viewer = null;
        private StringProperty chooserTitle = null;
        private Type limitTo = null;

        private Builder ()
        {

        }

        public Builder limitTo (Type t)
        {

            this.limitTo = t;
            return this;

        }

        public Builder withViewer (AbstractViewer viewer)
        {

            this.viewer = viewer;
            return this;

        }

        public Builder initialFile (File f)
        {

            this.initialFile = f;
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

            this.placeholder = prop;
            return this;

        }

        public Builder chooserTitle (List<String> prefix,
                                     String...    ids)
        {

            return this.chooserTitle (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder chooserTitle (String... ids)
        {

            return this.chooserTitle (getUILanguageStringProperty (ids));

        }

        public Builder chooserTitle (StringProperty prop)
        {

            this.chooserTitle = prop;
            return this;

        }

        public Builder buttonTooltip (List<String> prefix,
                                      String...    ids)
        {

            return this.buttonTooltip (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder buttonTooltip (String... ids)
        {

            return this.buttonTooltip (getUILanguageStringProperty (ids));

        }

        public Builder buttonTooltip (StringProperty prop)
        {

            this.buttonTooltip = prop;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;
            return this;

        }

        @Override
        public QuollFileField build ()
        {

            return new QuollFileField (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
