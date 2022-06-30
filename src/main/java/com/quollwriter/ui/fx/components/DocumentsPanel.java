package com.quollwriter.ui.fx.components;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import javafx.collections.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.geometry.*;
import javafx.scene.image.*;
import javafx.scene.text.*;
import javafx.beans.property.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class DocumentsPanel extends StackPane
{

    private AbstractProjectViewer viewer = null;
    private VBox fileBoxes = null;
    private VBox overlay = null;
    private Path lastFile = null;
    private Label addLabel = null;
    private ObservableSet<Path> paths = null;

    public DocumentsPanel (ObservableSet<Path>   opaths,
                           IPropertyBinder       binder,
                           AbstractProjectViewer viewer)
    {

        this.paths = FXCollections.observableSet ();

        this.viewer = viewer;
        this.getStyleClass ().add (StyleClassNames.DOCUMENTS);

        VBox b = new VBox ();
        b.managedProperty ().bind (b.visibleProperty ());
        b.setFillWidth (true);
        this.fileBoxes = new VBox ();
        this.fileBoxes.setFillWidth (true);
        VBox.setVgrow (this.fileBoxes,
                       Priority.ALWAYS);

        this.addLabel = QuollLabel.builder ()
            .label (getUILanguageStringProperty (form,addedit,types,documents,add,label))
            .styleClassName (StyleClassNames.ADD)
            .build ();

        this.addLabel.setOnMouseClicked (ev ->
        {

            if (ev.isPopupTrigger ())
            {

                return;

            }

            FileChooser fs = new FileChooser ();

            Path dir = null;

            if (this.lastFile != null)
            {

                dir = this.lastFile.getParent ();

            } else {

                dir = Paths.get (System.getProperty ("user.home"));

            }

            fs.setInitialDirectory (dir.toFile ());
            List<File> files = fs.showOpenMultipleDialog (this.viewer.getViewer ());

            if (files != null)
            {

                for (File f : files)
                {

                    this.paths.add (f.toPath ());
                    this.lastFile = f.toPath ();

                }

            }

        });

        b.getChildren ().addAll (this.addLabel, fileBoxes);

        this.fileBoxes.getStyleClass ().add (StyleClassNames.FILES);
        this.getChildren ().add (b);

        this.overlay = new VBox ();
        this.overlay.prefWidthProperty ().bind (this.widthProperty ());
        this.overlay.prefHeightProperty ().bind (this.heightProperty ());
        this.overlay.getStyleClass ().add (StyleClassNames.OVERLAY);
        this.overlay.managedProperty ().bind (this.overlay.visibleProperty ());
        this.overlay.getChildren ().add (QuollLabel.builder ()
            .label (assets,documents,add,drop)
            .styleClassName (StyleClassNames.DOWNLOAD)
            .build ());
        this.overlay.setVisible (false);
        this.getChildren ().add (this.overlay);
        VBox.setVgrow (this.overlay,
                       Priority.ALWAYS);
/*
        List<Path> files = new ArrayList<> (this.paths);
        Collections.sort (files);

        for (Path f : files)
        {

            this.addFileBox (f,
                             -1);

        }
*/
        binder.addSetChangeListener (this.paths,
                                     ch ->
        {

            if (ch.wasAdded ())
            {

                List<Path> paths = new ArrayList<> (this.paths);

                Collections.sort (paths);

                Path f = ch.getElementAdded ();

                int addAt = paths.indexOf (f);

                this.addFileBox (f,
                                 addAt);

            }

            if (ch.wasRemoved ())
            {

                this.removeFile (ch.getElementRemoved ());

            }

        });

        this.setPaths (opaths);

        this.setOnDragDropped (ev ->
        {

            this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            if (this.canHandleDragEvent (ev))
            {

                List<Path> pfs = this.getFilesFromDragEvent (ev);

                if (pfs.size () == 0)
                {

                    return;

                }

                try
                {

                    b.setVisible (true);
                    this.overlay.setVisible (false);

                    for (Path f : pfs)
                    {

                        this.paths.add (f);
                        this.lastFile = f;

                    }

                    this.requestLayout ();
                    ev.consume ();

                } catch (Exception e) {

                    // TODO Handle user error.

                    Environment.logError ("Unable to add files: " + pfs,
                                          e);

                }

            }

        });

        this.setOnDragExited (ev ->
        {

            Node n = (Node) ev.getSource ();

            if (this.localToScreen (this.getLayoutBounds ()).contains (n.localToScreen (ev.getX (), ev.getY ())))
            {

                return;

            }

            this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);
            this.overlay.setVisible (false);
            b.setVisible (true);
            ev.consume ();

        });

        this.setOnDragEntered (ev ->
        {

            this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

            if (this.canHandleDragEvent (ev))
            {

                b.setVisible (false);

                List<Path> pfs = this.getFilesFromDragEvent (ev);

                if (pfs.size () == 0)
                {

                    return;

                }

                this.overlay.setVisible (true);

                ev.acceptTransferModes (TransferMode.COPY_OR_MOVE);

            }

        });

        this.setOnDragOver (ev ->
        {

            this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

            if (this.canHandleDragEvent (ev))
            {

                b.setVisible (false);
                List<Path> pfs = this.getFilesFromDragEvent (ev);

                if (pfs.size () == 0)
                {

                    return;

                }

                this.overlay.setVisible (true);

                ev.acceptTransferModes (TransferMode.COPY_OR_MOVE);

            }

        });

    }

    public void setPaths (Set<Path> paths)
    {

        this.paths.clear ();

        this.paths.addAll (paths);

    }

    public ObservableSet<Path> pathsProperty ()
    {

        return this.paths;

    }

    private boolean canHandleDragEvent (DragEvent ev)
    {

        List<File> files = ev.getDragboard ().getFiles ();

        if ((files == null)
            ||
            (files.size () == 0)
           )
        {

            return false;

        }

        for (File f : files)
        {

            if (Files.isDirectory (f.toPath ()))
            {

                return false;

            }

            Path p = f.toPath ();

            if (this.paths.contains (p))
            {

                return false;

            }

        }

        return true;

    }

    private List<Path> getFilesFromDragEvent (DragEvent ev)
    {

        List<File> files = ev.getDragboard ().getFiles ();

        if (files == null)
        {

            return new ArrayList<> ();

        }

        List<Path> pfs = new ArrayList<> ();

        for (File f : files)
        {

            Path p = f.toPath ();

            if ((Files.exists (p))
                &&
                (!Files.isDirectory (p))
               )
            {

                pfs.add (p);

            }

        }

        return pfs;

    }

    private void removeFile (Path p)
    {

        Node fbn = this.fileBoxes.getChildren ().stream ()
            .filter (n ->
            {

                if (n instanceof FileBox)
                {

                    FileBox fb = (FileBox) n;

                    if (fb.getFile ().equals (p))
                    {

                        return true;

                    }

                }

                return false;

            })
            .findFirst ()
            .orElse (null);

        if (fbn != null)
        {

            this.fileBoxes.getChildren ().remove (fbn);

        }

        this.fileBoxes.setVisible (this.fileBoxes.getChildren ().size () != 0);

    }

    private void addFileBox (Path f,
                             int  addAt)
    {

        FileBox fb = new FileBox (f,
                                  this,
                                  this.viewer);

        if (addAt == -1)
        {

            this.fileBoxes.getChildren ().add (fb);

        } else {

            this.fileBoxes.getChildren ().add (addAt,
                                               fb);

        }

        this.fileBoxes.setVisible (this.fileBoxes.getChildren ().size () != 0);

    }

    private class FileBox extends HBox
    {

        private Path file = null;
        private AbstractProjectViewer viewer = null;
        private DocumentsPanel docPanel = null;

        public FileBox (Path                     f,
                        DocumentsPanel           docPanel,
                        AbstractProjectViewer    viewer)
        {

            this.docPanel = docPanel;
            List<String> prefix = Arrays.asList (assets,documents,viewitem);

            this.file = f;
            this.viewer = viewer;
            this.getStyleClass ().add (StyleClassNames.FILE);

            this.setOnDragDetected (ev ->
            {

                Dragboard db = this.startDragAndDrop (TransferMode.MOVE);

                ClipboardContent c = new ClipboardContent ();
                List<File> files = new ArrayList<> ();
                files.add (f.toFile ());
                c.putFiles (files);

                db.setContent (c);
                db.setDragView (UIUtils.getImageOfNode (this));
                this.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, true);
                ev.consume ();

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
            if (!this.isValidFile ())
            {

                this.pseudoClassStateChanged (StyleClassNames.INVALID_PSUEDO_CLASS, true);

                UIUtils.setTooltip (this,
                                    getUILanguageStringProperty (assets,documents,viewitem,errortooltip));

            } else {

                UIUtils.setTooltip (this,
                                    getUILanguageStringProperty (assets,documents,viewitem,tooltip));

            }

            String ext = "...";
            String name = f.getFileName ().toString ();

            int ind = name.lastIndexOf (".");

            if ((ind > 0)
                &&
                (ind < name.length ())
               )
            {

                ext = name.substring (ind + 1);
                name = name.substring (0, ind);

            }

            StringProperty fileName = new SimpleStringProperty (f.getFileName ().toString ());

            QuollLabel l = QuollLabel.builder ()
                .label (fileName)
                .build ();
            HBox.setHgrow (l,
                           Priority.ALWAYS);

            if (UIUtils.isImageFile (f))
            {

                this.pseudoClassStateChanged (StyleClassNames.USERIMAGE_PSEUDO_CLASS, true);

                try
                {

                    IconBox ic = IconBox.builder ()
                        .image (new SimpleObjectProperty<Image> (UIUtils.getImage (f)))
                        .build ();

                    //ImageView iv = new ImageView (UIUtils.getImage (f));

                    //iv.setFitWidth (48);
                    //iv.setFitHeight (48);

                    l.setGraphic (ic);
                    //this.getChildren ().add (iv);

                } catch (Exception e) {

                    this.pseudoClassStateChanged (StyleClassNames.INVALID_PSUEDO_CLASS, true);

                    Environment.logError ("Unable to get image: " + f,
                                          e);

                }

            } else {

                this.pseudoClassStateChanged (StyleClassNames.NORMAL_PSUEDO_CLASS, true);
                IconBox ic = IconBox.builder ()
                    .iconName (StyleClassNames.NORMAL)
                    .build ();

                //ImageView iv = new ImageView (UIUtils.getImage (f));

                //iv.setFitWidth (48);
                //iv.setFitHeight (48);

                l.setGraphic (ic);

            }

            this.getChildren ().add (l);

            this.addEventHandler (MouseEvent.MOUSE_CLICKED,
                                  ev ->
            {

                if (ev.isPopupTrigger ())
                {

                    return;

                }

                if (this.isValidFile ())
                {

                    UIUtils.showFile (this.viewer,
                                      this.file);

                }

                if (this.getProperties ().get ("context-menu") != null)
                {

                    ((ContextMenu) this.getProperties ().get ("context-menu")).hide ();

                }

            });

            this.setOnContextMenuRequested (ev ->
            {

                ContextMenu cm = new ContextMenu ();

                if (this.isValidFile ())
                {

                    cm.getItems ().add (QuollMenuItem.builder ()
                        .label (assets,documents,viewitem,popupmenu,items,open)
                        .iconName (StyleClassNames.VIEW)
                        .onAction (eev ->
                        {

                            UIUtils.showFile (this.viewer,
                                              this.file);

                        })
                        .build ());

                }

                if (Files.exists (this.file.getParent ()))
                {

                    cm.getItems ().add (QuollMenuItem.builder ()
                        .label (assets,documents,viewitem,popupmenu,items,showfolder)
                        .iconName (StyleClassNames.SHOWFOLDER)
                        .onAction (eev ->
                        {

                            UIUtils.showFile (this.viewer,
                                              this.file.getParent ());

                        })
                        .build ());

                }

                cm.getItems ().add (QuollMenuItem.builder ()
                    .label (assets,documents,viewitem,popupmenu,items,remove)
                    .iconName (StyleClassNames.CLEAR)
                    .onAction (eev ->
                    {

                        this.docPanel.paths.remove (this.file);

                    })
                    .build ());

                this.getProperties ().put ("context-menu", cm);
                cm.setAutoFix (true);
                cm.setAutoHide (true);
                cm.setHideOnEscape (true);
                cm.show (this,
                         ev.getScreenX (),
                         ev.getScreenY ());
                ev.consume ();

            });

        }

        public Path getFile ()
        {

            return this.file;

        }

        private boolean isValidFile ()
        {

            if ((Files.notExists (this.file))
                ||
                (Files.isDirectory (this.file))
               )
            {

                return false;

            }

            return true;

        }

    }

}
