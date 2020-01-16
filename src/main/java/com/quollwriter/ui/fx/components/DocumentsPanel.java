package com.quollwriter.ui.fx.components;

import java.util.*;
import java.io.*;
import java.nio.file.*;

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
    private NamedObject obj = null;
    private VBox fileBoxes = null;
    private VBox overlay = null;
    private Path lastFile = null;
    private Label addLabel = null;

    public DocumentsPanel (NamedObject           obj,
                           IPropertyBinder       binder,
                           AbstractProjectViewer viewer)
    {

        this.obj = obj;
        this.viewer = viewer;
        this.getStyleClass ().add (StyleClassNames.DOCUMENTS);

        VBox b = new VBox ();
        b.setFillWidth (true);
        this.fileBoxes = new VBox ();
        this.fileBoxes.setFillWidth (true);
        VBox.setVgrow (this.fileBoxes,
                       Priority.ALWAYS);

        this.addLabel = QuollLabel.builder ()
            .label (new SimpleStringProperty ("Click to find a file or files to add"))
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

                    this.obj.addFile (f.toPath ());
                    this.lastFile = f.toPath ();

                }

            }

        });

        b.getChildren ().addAll (this.addLabel, fileBoxes);

        this.fileBoxes.getStyleClass ().add (StyleClassNames.FILES);
        this.getChildren ().add (b);

        this.overlay = new VBox ();
        this.overlay.getStyleClass ().add (StyleClassNames.OVERLAY);
        this.overlay.managedProperty ().bind (this.overlay.visibleProperty ());
        this.overlay.getChildren ().add (QuollLabel.builder ()
            .label (assets,documents,add,drop)
            .build ());
        this.overlay.setVisible (false);
        this.getChildren ().add (this.overlay);
        VBox.setVgrow (this.overlay,
                       Priority.ALWAYS);

        List<Path> files = new ArrayList<> (this.obj.getFiles ());
        Collections.sort (files);

        for (Path f : files)
        {

            this.addFileBox (f,
                             -1);

        }

        binder.addSetChangeListener (this.obj.getFiles (),
                                     ch ->
        {

            if (ch.wasAdded ())
            {

                List<Path> paths = new ArrayList<> (this.obj.getFiles ());

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

                    this.overlay.setVisible (false);

                    for (Path f : pfs)
                    {

                        this.obj.addFile (f);
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

            this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);
            this.overlay.setVisible (false);
            ev.consume ();

        });

        this.setOnDragEntered (ev ->
        {

            this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

            if (this.canHandleDragEvent (ev))
            {

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
/*
    @Override
    protected double computeMinHeight (double w)
    {

        double h = 0;

        if (this.addLabel.isVisible ())
        {

            h += this.addLabel.prefHeight (w);

        }

        List<Node> ns = null;

        if (this.fileBoxes.getChildren ().size () > 5)
        {

            ns = this.fileBoxes.getChildren ().subList (0, 5);

        } else {

            ns = this.fileBoxes.getChildren ();

        }

        for (Node n : ns)
        {

            Region r = (Region) n;

            h += r.prefHeight (w);

        }

        return h;

    }
*/
/*
    @Override

    public void layoutChildren ()
    {

        super.layoutChildren ();

        Insets ins = this.getInsets ();

        for (Node n : this.getManagedChildren ())
        {

            Region r = (Region) n;
System.out.println ("REG: " + r);
            r.resizeRelocate (ins.getLeft (),
                              ins.getTop (),
                              this.getWidth () - ins.getLeft () - ins.getRight (),
                              this.getHeight () - ins.getTop () - ins.getBottom ());

        }

    }
*/
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
                                  this.obj,
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
/*
    private void addFile (final Path    f,
                          int addAt,
                          final boolean noCheck)
    {

        if (addAt == -1)
        {

            List<Path> paths = new ArrayList<> (this.obj.getFiles ());

            Collections.sort (paths);

            addAt = paths.indexOf (f);

        }

        if (!noCheck)
        {

            if (this.obj.getFiles ().contains (f))
            {

                return;

            }

            try
            {

                this.obj.addFile (f);

                this.viewer.saveObject (this.obj,
                                        false);

            } catch (Exception e) {

                this.obj.removeFile (f);

                //this.obj.getFiles ().remove (f);

                Environment.logError ("Unable to add file to: " +
                                      this.obj,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (assets,document,add,actionerror));

                return;

            }

        }
*/

    private class FileBox extends HBox
    {

        private Path file = null;
        private AbstractProjectViewer viewer = null;
        private NamedObject obj = null;

        public FileBox (Path                     f,
                        NamedObject              obj,
                        AbstractProjectViewer    viewer)
        {

            List<String> prefix = Arrays.asList (assets,documents,viewitem);

            this.file = f;
            this.viewer = viewer;
            this.obj = obj;
            this.getStyleClass ().add (StyleClassNames.FILE);

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

                    ImageView iv = new ImageView (UIUtils.getImage (f));
                    iv.setFitWidth (48);
                    iv.setFitHeight (48);

                    l.setGraphic (iv);
                    //this.getChildren ().add (iv);

                } catch (Exception e) {

                    this.pseudoClassStateChanged (StyleClassNames.INVALID_PSUEDO_CLASS, true);

                    Environment.logError ("Unable to get image: " + f,
                                          e);

                }

            } else {

                this.pseudoClassStateChanged (StyleClassNames.NORMAL_PSUEDO_CLASS, true);

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
                        .styleClassName (StyleClassNames.VIEW)
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
                        .styleClassName (StyleClassNames.FOLDER)
                        .onAction (eev ->
                        {

                            UIUtils.showFile (this.viewer,
                                              this.file.getParent ());

                        })
                        .build ());

                }

                cm.getItems ().add (QuollMenuItem.builder ()
                    .label (assets,documents,viewitem,popupmenu,items,remove)
                    .styleClassName (StyleClassNames.REMOVE)
                    .onAction (eev ->
                    {

                        this.obj.removeFile (this.file);

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
