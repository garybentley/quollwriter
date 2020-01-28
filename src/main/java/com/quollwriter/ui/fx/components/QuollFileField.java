package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.nio.file.*;

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
    private ObjectProperty<Path> fileProp = null;
    private StringProperty chooserTitle = null;
    private Type limitTo = Type.file;
    private FileChooser.ExtensionFilter fileExtFilter = null;
    private AbstractViewer viewer = null;

    private QuollFileField (Builder b)
    {

        final QuollFileField _this = this;

        this.viewer = b.viewer;
        this.fileProp = new SimpleObjectProperty<> ();

        this.chooserTitle = b.chooserTitle;
        this.limitTo = b.limitTo;
        this.fileExtFilter = b.fileExtFilter;

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

            if (newv == null)
            {

                _this.text.setText ("");

            } else {

                _this.text.setText (newv.toString ());

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
            .styleClassName (StyleClassNames.FIND)
            .tooltip (b.findButtonTooltip)
            .onAction (ev ->
            {

                ev.consume ();
                _this.showChooser ();

            })
            .build ());

        this.getChildren ().add (QuollButton.builder ()
            .styleClassName (StyleClassNames.VIEW)
            .tooltip (b.viewButtonTooltip)
            .onAction (ev ->
            {

                ev.consume ();

                UIUtils.showFile (this.viewer,
                                  this.fileProp.getValue ());

            })
            .build ());

        if (b.showClear)
        {

            this.getChildren ().add (QuollButton.builder ()
                .styleClassName (StyleClassNames.CLEAR)
                .tooltip (b.clearButtonTooltip)
                .onAction (ev ->
                {

                    ev.consume ();
                    this.setFile (null);

                })
                .build ());

        }

        this.disableProperty ().addListener ((pr, oldv, newv) ->
        {

            this.getChildren ().stream ()
                .forEach (n -> n.setDisable (newv));

        });

    }

    private void showChooser ()
    {

        final QuollFileField _this = this;

        if (this.limitTo == Type.file)
        {

            FileChooser f = new FileChooser ();

            if (this.chooserTitle != null)
            {

                f.titleProperty ().bind (this.chooserTitle);

            }

            if (this.fileExtFilter != null)
            {

                f.getExtensionFilters ().add (this.fileExtFilter);

            }

            File _f = f.showOpenDialog (this.getScene ().getWindow ());
            _this.fileProp.setValue ((_f == null ? null : _f.toPath ()));

            return;

        }

        if (this.limitTo == Type.directory)
        {

            DirectoryChooser d = new DirectoryChooser ();
            d.titleProperty ().bind (this.chooserTitle);

            if (this.fileProp.getValue () != null)
            {

                if (Files.exists (this.fileProp.getValue ()))
                {

                    d.setInitialDirectory (this.fileProp.getValue ().toFile ());

                }

            }

            File f = d.showDialog (this.getScene ().getWindow ());

            _this.fileProp.setValue ((f != null ? f.toPath () : this.fileProp.getValue ()));

        }

    }

    public void setFile (Path f)
    {

        this.fileProp.setValue (f);

    }

    public Path getFile ()
    {

        return this.fileProp.getValue ();

    }

    public ObjectProperty<Path> fileProperty ()
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
        private StringProperty findButtonTooltip = null;
        private StringProperty clearButtonTooltip = null;
        private StringProperty viewButtonTooltip = null;
        private Path initialFile = null;
        private Stage stage = null;
        private StringProperty chooserTitle = null;
        private Type limitTo = null;
        private FileChooser.ExtensionFilter fileExtFilter = null;
        private boolean showClear = false;
        private AbstractViewer viewer = null;

        private Builder ()
        {

        }

        public Builder showClear (boolean v)
        {

            this.showClear = v;
            return this;

        }

        public Builder limitTo (Type t)
        {

            this.limitTo = t;
            return this;

        }

        public Builder inStage (Stage s)
        {

            this.stage = s;
            return this;

        }

        public Builder withViewer (AbstractViewer viewer)
        {

            this.viewer = viewer;
            return this;

        }

        public Builder initialFile (Path f)
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

        public Builder viewButtonTooltip (List<String> prefix,
                                          String...    ids)
        {

            return this.viewButtonTooltip (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder viewButtonTooltip (String... ids)
        {

            return this.viewButtonTooltip (getUILanguageStringProperty (ids));

        }

        public Builder viewButtonTooltip (StringProperty prop)
        {

            this.viewButtonTooltip = prop;
            return this;

        }

        public Builder findButtonTooltip (List<String> prefix,
                                          String...    ids)
        {

            return this.findButtonTooltip (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder findButtonTooltip (String... ids)
        {

            return this.findButtonTooltip (getUILanguageStringProperty (ids));

        }

        public Builder findButtonTooltip (StringProperty prop)
        {

            this.findButtonTooltip = prop;
            return this;

        }

        public Builder clearButtonTooltip (List<String> prefix,
                                           String...    ids)
        {

            return this.clearButtonTooltip (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder clearButtonTooltip (String... ids)
        {

            return this.clearButtonTooltip (getUILanguageStringProperty (ids));

        }

        public Builder clearButtonTooltip (StringProperty prop)
        {

            this.clearButtonTooltip = prop;
            return this;

        }

        public Builder fileExtensionFilter (StringProperty filterDesc,
                                            String...      exts)
        {

            if ((exts == null)
                ||
                (exts.length == 0)
               )
            {

                throw new IllegalArgumentException ("At least one file extension must be provided.");

            }

            String v = filterDesc.getValue ();

            List<String> lexts = Arrays.asList (exts);

            StringBuilder b = new StringBuilder (v);

            b.append (" (");
            b.append (lexts.stream ()
                .collect (Collectors.joining (", ")));
            b.append (")");

            this.fileExtFilter = new FileChooser.ExtensionFilter (b.toString (),
                                                                  lexts.stream ()
                                                                    .map (e ->
                                                                    {

                                                                        if (e.startsWith ("*."))
                                                                        {

                                                                            return e;

                                                                        }

                                                                        if (e.startsWith ("."))
                                                                        {

                                                                            return "*" + e;

                                                                        }

                                                                        return "*." + e;

                                                                    })
                                                                    .collect (Collectors.toList ()));

            return this;

        }

        public Builder fileExtensionFilter (FileChooser.ExtensionFilter fil)
        {

            this.fileExtFilter = fil;
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
