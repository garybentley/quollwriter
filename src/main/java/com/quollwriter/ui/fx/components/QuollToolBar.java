package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.collections.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.*;
import javafx.geometry.*;
import javafx.scene.input.*;
import javafx.event.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class QuollToolBar extends HBox implements Stateful
{

    private FlowPane otherItems = null;
    private AbstractViewer viewer = null;
    private String popupId = null;
    private Map<String, Node> controls = new LinkedHashMap<> ();
    private DataFormat dataFormat = null;
    private boolean configurable = false;
    private EventHandler<QuollPopup.PopupEvent> onConfigurePopupShown = null;
    private EventHandler<QuollPopup.PopupEvent> onConfigurePopupClosed = null;

    private QuollToolBar (Builder b)
    {

        this.viewer = b.viewer;

        if (b.tooltip != null)
        {

            Tooltip t = new Tooltip ();
            t.textProperty ().bind (b.tooltip);

            UIUtils.setTooltip (this,
                                b.tooltip);

        }

        this.getStyleClass ().add (StyleClassNames.QTOOLBAR);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.isControls)
        {

            this.getStyleClass ().add (StyleClassNames.CONTROLS);

        }

        this.managedProperty ().bind (this.visibleProperty ());

        this.maxWidthProperty ().bind (this.prefWidthProperty ());
        this.minWidthProperty ().bind (this.prefWidthProperty ());
        //this.prefWidthProperty ().bind (this.widthProperty ());

        HBox.setHgrow (this,
                       Priority.NEVER);

        if (b.configurable)
        {

            this.configurable = b.configurable;

            this.popupId = UUID.randomUUID ().toString ();
            this.dataFormat = new DataFormat ("qw/buttonId/" + this.popupId);

            this.setOnDragExited (ev ->
            {

                this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            });

            this.setOnDragOver (ev ->
            {

                this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

                Dragboard db = ev.getDragboard ();

                Object o = db.getContent (dataFormat);

                if (o != null)
                {

                    this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

                    ev.acceptTransferModes (TransferMode.ANY);

                }

                ev.consume ();

            });

            this.setOnDragDropped (ev ->
            {

                this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

                Dragboard db = ev.getDragboard ();

                Object o = db.getContent (dataFormat);

                if (o != null)
                {

                    Node on = this.controls.get (o.toString ());

                    Pane p = (Pane) on.getParent ();

                    int li = this.getChildren ().size ();

                    if (li > 0)
                    {

                        li--;

                    } else {

                        p.getChildren ().remove (on);

                        this.getChildren ().add (on);

                        return;

                    }

                    Node fn = this.getChildren ().get (li);

                    if (ev.getScreenX () > fn.localToScreen (fn.getBoundsInLocal ()).getMaxX ())
                    {

                        p.getChildren ().remove (on);

                        this.getChildren ().add (on);

                    } else {

                        p.getChildren ().remove (on);

                        this.getChildren ().add (0,
                                                 on);

                    }

                }

                ev.setDropCompleted (true);
                ev.consume ();

            });

            this.setOnDragDone (ev ->
            {

                this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);
                this.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);

            });

            this.setOnDragEntered (ev ->
            {

                ev.consume ();

            });

            this.otherItems = new FlowPane ();
            this.otherItems.getStyleClass ().add (b.styleName);
            this.otherItems.getStyleClass ().add ("controls");

            this.otherItems.setOnDragDropped (ev ->
            {

                this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

                Dragboard db = ev.getDragboard ();

                Object o = db.getContent (dataFormat);

                if (o != null)
                {

                    Node on = this.controls.get (o.toString ());

                    Pane p = (Pane) on.getParent ();

                    int li = this.otherItems.getChildren ().size ();

                    if (li > 0)
                    {

                        li--;

                    } else {

                        p.getChildren ().remove (on);

                        this.otherItems.getChildren ().add (on);

                        return;

                    }

                    Node fn = this.otherItems.getChildren ().get (li);

                    if (ev.getScreenX () > fn.localToScreen (fn.getBoundsInLocal ()).getMaxX ())
                    {

                        p.getChildren ().remove (on);

                        this.otherItems.getChildren ().add (on);

                    } else {

                        p.getChildren ().remove (on);

                        this.otherItems.getChildren ().add (0,
                                                 on);

                    }

                }

                ev.setDropCompleted (true);
                ev.consume ();

            });

            this.otherItems.setOnDragEntered (ev ->
            {

                ev.consume ();

            });

            this.otherItems.setOnDragOver (ev ->
            {

                this.otherItems.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

                Dragboard db = ev.getDragboard ();

                Object o = db.getContent (dataFormat);

                if (o != null)
                {

                    this.otherItems.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

                    ev.acceptTransferModes (TransferMode.ANY);

                    return;

                }

                ev.consume ();

            });

            if (b.controls != null)
            {

                Map<String, Node> cons = new LinkedHashMap<> ();

                for (Node n : b.controls)
                {

                    String bid = UIUtils.getButtonId (n);

                    if (bid == null)
                    {

                        throw new IllegalStateException ("Controls must have a buttonId property if the toolbar is configurable: " + n);

                    }

                    this.makeDraggable (n);

                    this.controls.put (bid,
                                       n);

                }

                if (b.visibleItems != null)
                {

                    for (String id : b.visibleItems)
                    {

                        Node cn = this.controls.get (id);

                        if (id == null)
                        {

                            throw new IllegalArgumentException ("No control provided with buttonId: " + id);

                        }

                        this.getChildren ().add (cn);

                    }

                } else {

                    this.getChildren ().addAll (b.controls);

                }

            }

            this.setOnMousePressed (ev ->
            {

                if (this.getProperties ().get ("context-menu") != null)
                {

                    ((ContextMenu) this.getProperties ().get ("context-menu")).hide ();

                }

            });

            this.setOnContextMenuRequested (ev ->
            {

                if (!this.configurable)
                {

                    return;

                }

                ContextMenu cm = new ContextMenu ();

                QuollMenuItem mi = QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (actions,configure))
                    .iconName (StyleClassNames.CONFIG)
                    .onAction (eev ->
                    {

                        this.showConfigurePopup ();

                    })
                    .build ();

                cm.getItems ().add (mi);

                ev.consume ();

                this.getProperties ().put ("context-menu", cm);

                cm.setAutoFix (true);
                cm.setAutoHide (true);
                cm.setHideOnEscape (true);
                cm.show (this,
                         ev.getScreenX (),
                         ev.getScreenY ());

            });

        } else {

            if (b.controls != null)
            {

                this.getChildren ().addAll (b.controls);

            }

        }

    }

    public Set<String> getVisibleItemIds ()
    {

        Set<String> ids = new LinkedHashSet<> ();

        for (Node n : this.getChildren ())
        {

            String id = UIUtils.getButtonId (n);

            if (id != null)
            {

                ids.add (id);

            }

        }

        return ids;

    }

    public void setVisibleItems (Set<String> ids)
    {

        this.getChildren ().clear ();

        for (String id : ids)
        {

            Node cn = this.controls.get (id);

            if (cn == null)
            {

                throw new IllegalArgumentException ("No control provided with buttonId: " + id);

            }

            this.getChildren ().add (cn);

        }

    }

    @Override
    public void init (State s)
    {

    }

    @Override
    public State getState ()
    {

        State s = new State ();
        return s;

    }

    private void makeDraggable (Node n)
    {

        String bid = UIUtils.getButtonId (n);

        n.addEventFilter (MouseEvent.MOUSE_PRESSED,
                           ev ->
        {

            if (this.viewer.getPopupById (this.popupId) != null)
            {

                if (this.viewer.getPopupById (this.popupId).isVisible ())
                {

                    ev.consume ();
                    return;

                }

            }

        });

        n.addEventFilter (MouseEvent.MOUSE_CLICKED,
                           ev ->
        {

            if (this.viewer.getPopupById (this.popupId) != null)
            {

                if (this.viewer.getPopupById (this.popupId).isVisible ())
                {

                    ev.consume ();
                    return;

                }

            }

        });

        n.setOnDragDetected (ev ->
        {

            if (this.viewer.getPopupById (this.popupId) != null)
            {

                Dragboard db = n.startDragAndDrop (TransferMode.MOVE);
                db.setDragView (n.snapshot (null, null));
                ClipboardContent c = new ClipboardContent ();
                c.put (dataFormat,
                       bid);
                //c.putString ("hello");
                db.setContent (c);
                n.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, true);
                ev.consume ();

            }

        });

        n.setOnDragExited (ev ->
        {

            n.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

        });

        n.setOnDragDone (ev ->
        {

            n.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);
            n.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);

        });

        n.setOnDragDropped (ev ->
        {

            n.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            Dragboard db = ev.getDragboard ();

            Object o = db.getContent (dataFormat);

            if (o != null)
            {

                Pane p = ((Pane) n.getParent ());
                int ind = p.getChildren ().indexOf (n);
                Node rn = this.controls.get (o.toString ());
                Pane rnp = (Pane) rn.getParent ();
                rnp.getChildren ().remove (rn);

                if (p == rnp)
                {

                    p.getChildren ().add (ind,
                                          rn);

                } else {

                    ind++;

                    if (ind >= p.getChildren ().size ())
                    {

                        p.getChildren ().add (rn);

                    } else {

                        p.getChildren ().add (ind,
                                              rn);

                    }

                }

            }

            ev.setDropCompleted (true);
            ev.consume ();

        });

        n.setOnDragOver (ev ->
        {

            n.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            Dragboard db = ev.getDragboard ();

            Object o = db.getContent (dataFormat);

            if (o != null)
            {

                n.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

                ev.acceptTransferModes (TransferMode.ANY);
                ev.consume ();

                return;

            }

        });

    }

    public void setOnConfigurePopupShown (EventHandler<QuollPopup.PopupEvent> ev)
    {

        this.onConfigurePopupShown = ev;

    }

    public void setOnConfigurePopupClosed (EventHandler<QuollPopup.PopupEvent> ev)
    {

        this.onConfigurePopupClosed = ev;

    }

    private void showConfigurePopup ()
    {

        QuollPopup qp = this.viewer.getPopupById (this.popupId);

        if (qp != null)
        {

            this.minWidthProperty ().unbind ();
            //this.minWidthProperty ().bind (qp.getContent ().widthProperty ());

            this.getStyleClass ().add ("configuring");

            qp.setVisible (true);
            qp.toFront ();
            return;

        }

        Set<String> visible = new HashSet<> ();

        for (Node n : this.getChildren ())
        {

            visible.add (UIUtils.getButtonId (n));

        }

        this.otherItems.getChildren ().clear ();

        for (String bid : this.controls.keySet ())
        {

            if (visible.contains (bid))
            {

                continue;

            }

            this.otherItems.getChildren ().add (this.controls.get (bid));

        }

        VBox c = new VBox ();

        //this.minWidthProperty ().bind (c.widthProperty ());
        //c.prefWidthProperty ().bind (this.widthProperty ());

        qp = QuollPopup.builder ()
            .withClose (true)
            .content (c)
            .withViewer (this.viewer)
            .styleClassName ("headerconfigure")
            .styleSheet ("headerconfigure")
            .removeOnClose (false)
            .popupId (this.popupId)
            .onClose (() ->
            {

                this.getStyleClass ().remove ("configuring");

            })
            .build ();

        if (this.onConfigurePopupShown != null)
        {

            qp.addEventHandler (QuollPopup.PopupEvent.SHOWN_EVENT,
                                this.onConfigurePopupShown);

        }

        if (this.onConfigurePopupClosed != null)
        {

            qp.addEventHandler (QuollPopup.PopupEvent.CLOSED_EVENT,
                                this.onConfigurePopupClosed);

        }

        QuollPopup _qp = qp;

        QuollTextView desc = QuollTextView.builder ()
            .text (new SimpleStringProperty ("To remove items from the toolbar, drag and drop items from the toolbar into the area above.  To add items to the toolbar, drag and drop items from the area above into the toolbar."))
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        QuollButtonBar butBar = QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .label (new SimpleStringProperty ("Finish"))
                        .onAction (eev ->
                        {

                            this.minWidthProperty ().unbind ();
                            _qp.close ();

                        })
                        .build ())
            .build ();

        c.getChildren ().addAll (this.otherItems, desc, butBar);

        this.viewer.addPopup (qp);
        qp.setVisible (false);
        qp.applyCss ();
        qp.requestLayout ();

        c.widthProperty ().addListener ((pr, oldv, newv) ->
        {

            _qp.applyCss ();
            _qp.requestLayout ();

            Bounds b = this.localToScene (this.getLayoutBounds ());
            Bounds qb = _qp.getLayoutBounds ();
            double x = b.getMaxX () - qb.getWidth ();
            double y = b.getMaxY ();

            this.viewer.showPopup (_qp,
                                   x,
                                   y);

        });

        UIUtils.forceRunLater (() ->
        {

            Bounds b = this.localToScene (this.getLayoutBounds ());
            Bounds qb = _qp.getLayoutBounds ();
            double x = b.getMaxX () - qb.getWidth ();
            double y = b.getMaxY ();

            this.viewer.showPopup (_qp,
                                   x,
                                   y);

        });

        this.getStyleClass ().add ("configuring");

    }

    public Set<String> getHeaderControlIds ()
    {

        Set<String> ids = new LinkedHashSet<> ();

        for (Node n : this.getChildren ())
        {

            ids.add (UIUtils.getButtonId (n));

        }

        return ids;

    }

    public ObservableList<Node> getItems ()
    {

        return this.getChildren ();

    }

    public void setConfigurable (boolean v)
    {

        this.configurable = v;

    }

    public static QuollToolBar.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollToolBar>
    {

        private StringProperty label = null;
        private String styleName = null;
        private StringProperty tooltip = null;
        private Collection<? extends Node> controls = null;
        private boolean isControls = true;
        private boolean configurable = false;
        private Set<String> visibleItems = null;
        private AbstractViewer viewer = null;

        private Builder ()
        {

        }

        @Override
        public QuollToolBar build ()
        {

            return new QuollToolBar (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder configurable (boolean v)
        {

            this.configurable = v;
            return this;

        }

        public Builder inViewer (AbstractViewer v)
        {

            this.viewer = v;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;

            return this;

        }

        public Builder tooltip (StringProperty prop)
        {

            this.tooltip = prop;

            return this;

        }

        public Builder tooltip (List<String> prefix,
                                String...    ids)
        {

            this.tooltip = getUILanguageStringProperty (Utils.newList (prefix, ids));
            return this;

        }

        public Builder tooltip (String... ids)
        {

            this.tooltip = getUILanguageStringProperty (ids);
            return this;

        }

        public Builder visibleItems (Set<String> items)
        {

            this.visibleItems = new LinkedHashSet<> (items);
            return this;

        }

        public Builder controls (Collection<? extends Node> c)
        {

            this.controls = c;
            return this;

        }

        public Builder isControls (boolean v)
        {

            this.isControls = v;
            return this;

        }

    }

}
