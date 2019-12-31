package com.quollwriter.ui.fx.components;

import java.awt.Desktop;

import java.util.*;

import java.io.*;
import java.nio.file.*;

import javafx.beans.property.*;

import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ImageSelector extends QuollImageView
{

    private Path origFile = null;
    private ObjectProperty<Path> fileProp = new SimpleObjectProperty<> ();
    private AbstractViewer viewer = null;

    private ImageSelector (Builder b)
    {

        if (b.viewer == null)
        {

            throw new IllegalArgumentException ("Viewer must be provided.");

        }

        final ImageSelector _this = this;

        this.viewer = b.viewer;
        this.origFile = b.initFile;

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        UIUtils.setTooltip (this,
                            b.tooltip != null ? b.tooltip : getUILanguageStringProperty (imageselector,tooltip));

        this.setOnContextMenuRequested (ev ->
        {

            ContextMenu cm = new ContextMenu ();

            if (this.getImagePath () != null)
            {

                cm.getItems ().add (QuollMenuItem.builder ()
                    .label (imageselector,popupmenu,items,remove)
                    .styleClassName (StyleClassNames.DELETE)
                    .onAction (eev ->
                    {

                        _this.fileProp.setValue (null);

                    })
                    .build ());

                cm.getItems ().add (QuollMenuItem.builder ()
                    .label (imageselector,popupmenu,items,view)
                    .styleClassName (StyleClassNames.VIEW)
                    .onAction (eev ->
                    {

                        try
                        {

                            Desktop.getDesktop ().open (_this.getImagePath ().toFile ());

                        } catch (Exception e) {

                            Environment.logError ("Unable to show file: " +
                                                  _this.getImagePath (),
                                                  e);

                            ComponentUtils.showErrorMessage (_this.viewer,
                                                             getUILanguageStringProperty (imageselector,view,actionerror));
                                                      //"Unable to show file");

                        }

                    })
                    .build ());

                cm.getItems ().add (QuollMenuItem.builder ()
                    .label (imageselector,popupmenu,items,showfolder)
                    .styleClassName (StyleClassNames.FOLDER)
                    .onAction (eev ->
                    {

                        try
                        {

                            Desktop.getDesktop ().open (_this.getImagePath ().getParent ().toFile ());

                        } catch (Exception e) {

                            Environment.logError ("Unable to show file in folder: " +
                                                  _this.fileProp.getValue (),
                                                  e);

                            ComponentUtils.showErrorMessage (_this.viewer,
                                                             getUILanguageStringProperty (imageselector,view,actionerror));
                                                      //"Unable to show file in folder");

                        }

                    })
                    .build ());

                }

                if (this.origFile != null)
                {

                    cm.getItems ().add (QuollMenuItem.builder ()
                        .label (imageselector,popupmenu,items,restore)
                        .styleClassName (StyleClassNames.RESTORE)
                        .onAction (eev ->
                        {

                            try
                            {

                                _this.setImage (_this.origFile);

                            } catch (Exception e) {

                                Environment.logError ("Unable to show file in folder: " +
                                                      _this.fileProp.getValue (),
                                                      e);

                                ComponentUtils.showErrorMessage (_this.viewer,
                                                                 getUILanguageStringProperty (imageselector,restore,actionerror));

                            }

                        })
                        .build ());

                }

                cm.show (this.viewer.getViewer (), ev.getScreenX (), ev.getScreenY ());
                cm.setAutoHide (true);

        });

        this.setOnMouseClicked (ev ->
        {

            if (ev.isPopupTrigger ())
            {

                return;

            }

            FileChooser fc = new FileChooser ();
            fc.titleProperty ().bind (getUILanguageStringProperty (imageselector,title));
            fc.getExtensionFilters ().add (UIUtils.imageFileFilter);

            if (this.getImagePath () != null)
            {

                fc.setInitialDirectory (this.getImagePath ().getParent ().toFile ());
                fc.setInitialFileName (this.getImagePath ().getFileName ().toString ());

            } else {

                // TODO Set default dir?
                fc.setInitialDirectory (Environment.getUserQuollWriterDirPath ().toFile ());
            }

            File sel = fc.showOpenDialog (this.viewer.getViewer ());

            if (sel != null)
            {

                try
                {

                    this.setImage (sel.toPath ());

                } catch (Exception e) {

                    Environment.logError ("Unable to show file in folder: " +
                                          _this.fileProp.getValue (),
                                          e);

                    ComponentUtils.showErrorMessage (_this.viewer,
                                                     getUILanguageStringProperty (imageselector,update,actionerror));

                }

            }

        });


        try
        {

            this.setImage (b.initFile);

        } catch (Exception e) {

            Environment.logError ("Unable to init file: " +
                                  b.initFile,
                                  e);

            ComponentUtils.showErrorMessage (_this.viewer,
                                             getUILanguageStringProperty (imageselector,init,actionerror));

        }

    }

    public static Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, ImageSelector>
    {

        private StringProperty tooltip = null;
        private StringProperty title = null;
        private Path initFile = null;
        private String styleName = null;
        private AbstractViewer viewer = null;

        @Override
        public ImageSelector build ()
        {

            return new ImageSelector (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder withViewer (AbstractViewer viewer)
        {

            this.viewer = viewer;
            return this;

        }

        public Builder file (Path p)
        {

            this.initFile = p;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;

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

        public Builder tooltip (StringProperty prop)
        {

            this.tooltip = prop;
            return this;

        }

        public Builder tooltip (List<String> prefix,
                                String...    ids)
        {

            return this.tooltip (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder tooltip (String... ids)
        {

            return this.tooltip (getUILanguageStringProperty (ids));

        }

    }

}
