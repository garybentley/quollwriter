package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.scene.*;
import javafx.geometry.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.control.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ConfigurableToolbar extends ToolBar
{

    private static final DataFormat JAVA_FORMAT = new DataFormat("application/x-java-serialized-object");

    private ToolBar otherItems = null;
    private Node moving = null;

    private ConfigurableToolbar (Builder b)
    {

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        this.otherItems = new ToolBar ();
        this.otherItems.getStyleClass ().add (b.styleName);
        this.otherItems.getStyleClass ().add ("controls");

        this.otherItems.setOnDragDetected (ev ->
        {

            if (this.otherItems.getItems ().contains (this.moving))
            {

                ev.consume ();
                return;

            }

            Dragboard db = this.otherItems.startDragAndDrop (TransferMode.ANY);
            db.setDragView (this.moving.snapshot (null, null));
            ev.consume ();

        });

        this.otherItems.setOnDragDropped (ev ->
        {

            this.moving = null;
            ev.setDropCompleted (true);
            ev.consume ();

        });

        this.otherItems.setOnDragEntered (ev ->
        {

            ev.acceptTransferModes (TransferMode.MOVE);

            ev.consume ();

        });

        this.otherItems.setOnDragOver (ev ->
        {

            if (this.moving != null)
            {

                if (this.otherItems.getItems ().contains (this.moving))
                {

                    return;

                }

                ev.acceptTransferModes (TransferMode.MOVE);

                Point2D p = this.otherItems.sceneToLocal (ev.getSceneX (),
                                                          ev.getSceneY ());

                int ind = 0;

                Node prev = null;

                for (Node n : this.otherItems.getItems ())
                {

                    Bounds ib = n.getBoundsInParent ();

                    if (ib.getMinX () < p.getX ())
                    {

                        if (prev == null)
                        {

                            // Insert at front.
                            ind = 0;

                        } else {

                            ind = this.otherItems.getItems ().indexOf (n);

                        }

                    }

                    prev = n;

                }

                this.getItems ().remove (this.moving);
                this.otherItems.getItems ().add (ind,
                                                 this.moving);
                this.moving = null;

                ev.consume ();

            }

        });

        ContextMenu cm = new ContextMenu ();
        MenuItem conf = new MenuItem ("Configure");

        String pid = UUID.randomUUID ().toString ();

        conf.setOnAction (ev ->
        {

            QuollPopup qp = b.viewer.getPopupById (pid);

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            VBox c = new VBox ();

            qp = QuollPopup.builder ()
                .withClose (true)
                .content (c)
                .withViewer (b.viewer)
                .styleClassName ("headerconfigure")
                .removeOnClose (true)
                .popupId (pid)
                .onClose (() ->
                {

                    this.getStyleClass ().remove ("configuring");

                })
                .build ();

            QuollPopup _qp = qp;

            BasicHtmlTextFlow desc = BasicHtmlTextFlow.builder ()
                .text (new SimpleStringProperty ("To remove items from the toolbar, drag and drop items from the toolbar into the area above.  To add items to the toolbar, drag and drop items from the area above into the toolbar."))
                .styleClassName (StyleClassNames.DESCRIPTION)
                .build ();

            QuollButtonBar butBar = QuollButtonBar.builder ()
                .button (QuollButton.builder ()
                            .label (new SimpleStringProperty ("Finish"))
                            .onAction (eev ->
                            {

                                _qp.close ();

                            })
                            .build ())
                .build ();

            c.getChildren ().addAll (this.otherItems, desc, butBar);

            qp.headerProperty ().getValue ().setVisible (false);

            b.viewer.showPopup (qp,
                                this,
                                Side.BOTTOM);

            this.getStyleClass ().add ("configuring");

        });

        MenuItem remove = new MenuItem ("Remove");
        cm.getItems ().addAll (conf, remove);

        this.setContextMenu (cm);

        if (b.visibleItems != null)
        {

            this.getItems ().addAll (b.visibleItems);

        }

        this.setOnDragDetected (ev ->
        {

            if (this.getItems ().contains (this.moving))
            {

                ev.consume ();
                return;

            }

            Dragboard db = this.startDragAndDrop (TransferMode.ANY);
            db.setDragView (this.moving.snapshot (null, null));
            ev.consume ();

        });

        this.setOnDragDropped (ev ->
        {

            this.moving = null;
            ev.setDropCompleted (true);
            ev.consume ();

        });

        this.setOnDragEntered (ev ->
        {

            ev.acceptTransferModes (TransferMode.MOVE);

            ev.consume ();

        });

        this.setOnDragOver (ev ->
        {

            if (this.moving != null)
            {

                if (this.getItems ().contains (this.moving))
                {

                    return;

                }

                ev.acceptTransferModes (TransferMode.MOVE);

                Point2D p = this.sceneToLocal (ev.getSceneX (),
                                               ev.getSceneY ());

                int ind = 0;

                Node prev = null;

                for (Node n : this.getItems ())
                {

                    Bounds ib = n.getBoundsInParent ();

                    if (ib.getMinX () < p.getX ())
                    {

                        if (prev == null)
                        {

                            // Insert at front.
                            ind = 0;

                        } else {

                            ind = this.getItems ().indexOf (n);

                        }

                    }

                    prev = n;

                }

                this.otherItems.getItems ().remove (this.moving);
                this.getItems ().add (ind,
                                      this.moving);
                this.moving = null;

                ev.consume ();

            }

        });

        for (Node n : b.items)
        {

            if (n == null)
            {

                continue;

            }

            if (b.visibleItems != null)
            {

                if (!b.visibleItems.contains (n))
                {

                    this.otherItems.getItems ().add (n);

                }

            }

            n.setOnDragDetected (ev ->
            {

                Dragboard db = n.startDragAndDrop (TransferMode.ANY);

                n.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, true);

                if (this.otherItems.getItems ().contains (n))
                {

                    db.setDragView (n.snapshot (null, null));

                }

                // Need to add something otherwise the drag isn't registered.
                ClipboardContent c = new ClipboardContent ();
                c.putString ("HERE");
                //c.put (JAVA_FORMAT, "HERE");
                db.setContent (c);
                this.moving = n;

                ev.consume ();

            });

            n.setOnDragEntered (ev ->
            {

                ev.acceptTransferModes (TransferMode.MOVE);

                if ((ev.getGestureSource () != n)
                    &&
                    (ev.getDragboard ().getContent (DataFormat.PLAIN_TEXT) != null)
                   )
                {

                    n.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

                }

                ev.consume ();

            });

            n.setOnDragOver (ev ->
            {

                ev.acceptTransferModes (TransferMode.MOVE);

                // Is this node in the visible toolbar?
                int indexOf = this.getItems ().indexOf (n);

                if (indexOf == -1)
                {
/*
                    this.otherItems.getItems ().remove (ev.getGestureSource ());

                    // Insert at the location of what we are currently over.
                    indexOf = this.getItems ().indexOf (n);

                    this.getItems ().add (indexOf,
                                          (Node) ev.getGestureSource ());
*/
                } else {

                    // This is for moving an item with the visible toolbar.
                    int indexOfOther = this.getItems ().indexOf (ev.getGestureSource ());

                    if (indexOf != indexOfOther)
                    {

                        this.getItems ().remove ((Node) ev.getGestureSource ());
                        this.getItems ().add (indexOf,
                                                      (Node) ev.getGestureSource ());

                    }

                }

                ev.consume ();

            });

            n.setOnDragExited (ev ->
            {

                n.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);
                ev.consume ();

            });

            n.setOnDragDropped (ev ->
            {

                this.moving = null;
                ev.setDropCompleted (true);
                n.setCursor (Cursor.DEFAULT);
                n.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);
                n.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);
                ev.consume ();

            });

        }

    }

    /**
     * Get a builder to create a new header.
     *
     * Usage: Header.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static ConfigurableToolbar.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, ConfigurableToolbar>
    {

        private String styleName = null;
        private Set<Node> items = null;
        private Set<Node> visibleItems = null;
        private AbstractViewer viewer = null;

        private Builder ()
        {

        }

        @Override
        public ConfigurableToolbar build ()
        {

            return new ConfigurableToolbar (this);

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

        public Builder items (Set<Node> items)
        {

            this.items = items;
            return this;

        }

        public Builder visibleItems (Set<Node> items)
        {

            this.visibleItems = items;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;

            return this;

        }

    }

}
