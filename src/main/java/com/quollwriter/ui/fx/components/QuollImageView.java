package com.quollwriter.ui.fx.components;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.geometry.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.text.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class QuollImageView extends StackPane
{

    //private ImageView iv = null;

    private IconBox ib = null;
    private VBox overlay = null;
    private Label dropLabel = null;

    private ObjectProperty<Image> imageProp = null;
    private ObjectProperty<Path> imagePathProp = null;

    public QuollImageView ()
    {

        this.imagePathProp = new SimpleObjectProperty<> ();
        this.imageProp = new SimpleObjectProperty<> ();

        this.setOnDragDetected (ev ->
        {

            if (this.imagePathProp.getValue () != null)
            {

                Dragboard db = this.startDragAndDrop (TransferMode.MOVE);

                ClipboardContent c = new ClipboardContent ();
                List<File> files = new ArrayList<> ();
                files.add (this.imagePathProp.getValue ().toFile ());
                c.putFiles (files);

                db.setContent (c);
                db.setDragView (UIUtils.getImageOfNode (this));
                this.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, true);
                ev.consume ();

            }

        });

        this.setOnDragDone (ev ->
        {

            this.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);
            ev.consume ();

        });


        this.setOnDragExited (ev ->
        {

            this.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);
            ev.consume ();

        });

        this.getStyleClass ().add (StyleClassNames.IMAGE);

        final QuollImageView _this = this;

        this.ib = IconBox.builder ()
            .build ();
        VBox bb = new VBox ();

        VBox.setVgrow (bb,
                       Priority.ALWAYS);

        bb.getChildren ().add (this.ib);

        this.getChildren ().add (bb);//this.ib);

        this.overlay = new VBox ();
        this.overlay.getStyleClass ().add (StyleClassNames.OVERLAY);
        this.overlay.managedProperty ().bind (this.overlay.visibleProperty ());
        this.dropLabel = new Label ();
        this.overlay.getChildren ().add (this.dropLabel);
        this.overlay.setVisible (false);
        this.getChildren ().add (this.overlay);
        this.pseudoClassStateChanged (StyleClassNames.NOIMAGE_PSEUDO_CLASS, false);
        this.ib.setImage (null);
        this.imageProp.setValue (null);

        /*
        VBox.setVgrow (this.overlay,
                       Priority.ALWAYS);
*/
        this.setOnDragOver (ev ->
        {

            if (this.canHandleDragEvent (ev))
            {

                File f = this.getFileFromDragEvent (ev);

                if (!f.exists ())
                {

                    return;

                }

                if ((this.imagePathProp.getValue () != null)
                    &&
                    (this.imagePathProp.getValue ().toFile ().equals (f))
                   )
                {

                    return;

                }

                ev.acceptTransferModes (TransferMode.COPY_OR_MOVE);

            }

        });

        this.setOnDragDropped (ev ->
        {

            this.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);

            if (this.canHandleDragEvent (ev))
            {

                File f = this.getFileFromDragEvent (ev);

                if (!f.exists ())
                {

                    return;

                }

                try
                {

                    this.overlay.setVisible (false);
                    this.setImage (f.toPath ());
                    //this.imagePathProp.setValue (f.toPath ());
                    this.requestLayout ();
                    ev.consume ();

                } catch (Exception e) {

                    // TODO Handle user error.

                    Environment.logError ("Unable to set image to: " + f,
                                          e);

                }

            }

        });

        this.setOnDragExited (ev ->
        {

            this.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);
            this.overlay.setVisible (false);
            ev.consume ();

            try
            {

                this.setImage (this.imagePathProp.getValue ());

            } catch (Exception e) {

                Environment.logError ("Unable to reset image",
                                      e);

            }

        });

        this.setOnDragDetected (ev ->
        {

            if (this.imagePathProp.getValue () != null)
            {

                Dragboard db = this.startDragAndDrop (TransferMode.COPY);

                ClipboardContent c = new ClipboardContent ();
                c.putFiles (Arrays.asList (this.imagePathProp.getValue ().toFile ()));
                db.setContent (c);
                ev.consume ();

            }

        });

        this.setOnDragEntered (ev ->
        {

            if (this.canHandleDragEvent (ev))
            {

                File f = this.getFileFromDragEvent (ev);

                if (!f.exists ())
                {

                    return;

                }

                if ((this.imagePathProp.getValue () != null)
                    &&
                    (this.imagePathProp.getValue ().toFile ().equals (f))
                   )
                {

                    return;

                }

                try
                {

                    Image im = null;

                    Path p = f.toPath ();

                    if ((p != null)
                        &&
                        (Files.exists (p))
                       )
                    {

                        try (InputStream is = Files.newInputStream (p))
                        {

                            im = new Image (is);

                        } catch (Exception e) {

                            // Ignore...

                        }

                    }

                    ev.acceptTransferModes (TransferMode.COPY_OR_MOVE);

                    this.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, true);
                    this.overlay.setVisible (true);
                    this.setImage (im);
                    ev.consume ();
                    this.requestLayout ();

                } catch (Exception e) {

                    Environment.logError ("Unable to set preview image to: " + f,
                                          e);

                }

            }

        });

    }

    public void setDragDropLabelText (StringProperty p)
    {

        this.dropLabel.textProperty ().unbind ();
        this.dropLabel.textProperty ().bind (p);

    }

    public Path getImagePath ()
    {

        return this.imagePathProp.getValue ();

    }

    public void setImage (Path p)
                   throws IOException
    {

        Image im = null;

        if ((p != null)
            &&
            (Files.exists (p))
           )
        {

            try (InputStream is = Files.newInputStream (p))
            {

                im = new Image (is);

            }

        }

        this.imagePathProp.setValue (p);

        this.setImage (im);

    }

    public ObjectProperty<Path> imagePathProperty ()
    {

        return this.imagePathProp;

    }

    public void setImage (Image im)
    {

        this.pseudoClassStateChanged (StyleClassNames.NOIMAGE_PSEUDO_CLASS, false);

        if (im == null)
        {

            this.ib.setImage (null);
            this.imageProp.setValue (null);
            this.pseudoClassStateChanged (StyleClassNames.NOIMAGE_PSEUDO_CLASS, true);
            return;

        }

        this.imageProp.setValue (im);
        this.ib.setImage (im);
        this.setPrefHeight (im.getHeight ());
        this.setMinHeight (im.getHeight ());
        /*
        UIUtils.runLater (() ->
        {

            this.requestLayout ();

        });
*/
    }

    public ObjectProperty<Image> imageProperty ()
    {

        return this.imageProp;

    }

    private File getFileFromDragEvent (DragEvent ev)
    {

        List<File> files = ev.getDragboard ().getFiles ();

        if (files == null)
        {

            return null;

        }

        if (files.size () > 1)
        {

            return null;

        }

        File f = files.get (0);

        if (UIUtils.isImageFile (f))
        {

            return f;

        }

        if (ev.getGestureSource () != ev.getGestureTarget ())
        {


        }

        return null;

    }

    private boolean canHandleDragEvent (DragEvent ev)
    {

        List<File> files = ev.getDragboard ().getFiles ();

        if ((files == null)
            ||
            (files.size () == 0)
            ||
            (files.size () > 1)
           )
        {

            return false;

        }

        File f = files.get (0);

        if (UIUtils.isImageFile (f))
        {

            return true;

        }

        if (ev.getGestureSource () != ev.getGestureTarget ())
        {


        }

        return false;

    }

}
