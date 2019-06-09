package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;

import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.event.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class QuollPopup extends ViewerContent
{

    private SimpleObjectProperty<Header> headerProp = null;
    private Runnable onClose = null;
    private double dx = 0;
    private double dy = 0;
    private double mx = 0;
    private double my = 0;
    private String popupId = null;
    private Boolean removeOnClose = false;
    private Set<QuollPopup> childPopups = null;
    private boolean moving = false;

    private QuollPopup (Builder b)
    {

        super (b.viewer);

        this.childPopups = new HashSet<> ();

        //this.setPrefSize (javafx.scene.layout.Region.USE__SIZE, javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        //this.setMaxSize (javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        this.removeOnClose = b.removeOnClose;

        final QuollPopup _this = this;

        this.popupId = b.popupId;

        Set<Node> controls = new LinkedHashSet<> ();

        if (b.controls != null)
        {

            controls.addAll (b.controls);

        }

        if (b.withClose)
        {

            QuollButton close = QuollButton.builder ()
                .styleClassName (StyleClassNames.CLOSE)
                .tooltip (actions,clicktoclose)
                .onAction (ev ->
                {
                    ev.consume ();
                    _this.close ();
                    //this.viewer.removePopup (this);
                })
                .build ();

            controls.add (close);

        }

        if (b.hideOnEscape)
        {

            // TODO
            //this.setHideOnEscape (true);

        }

        this.onClose = b.onClose;
        // TODO
        //this.setOnCloseRequest (ev -> this._close ());

        Header h = Header.builder ()
            .title (b.title)
            .controls (controls)
            .build ();
        h.managedProperty ().bind (h.visibleProperty ());

        VBox vb = new VBox ();
        this.getStyleClass ().add (StyleClassNames.QPOPUP);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        VBox.setVgrow (b.content, Priority.ALWAYS);
        vb.getChildren ().addAll (h, b.content);

        b.content.getStyleClass ().add (StyleClassNames.CONTENT);

        this.headerProp = new SimpleObjectProperty<> (h);

        this.getChildren ().add (vb);

        this.setOnMousePressed (ev ->
        {

            _this.toFront ();

        });

        h.setOnMousePressed(ev ->
        {
            // record a delta distance for the drag and drop operation.
            _this.dx = ev.getSceneX();
            _this.dy = ev.getSceneY();
            _this.mx = _this.getLayoutX ();
            _this.my = _this.getLayoutY ();
            h.setCursor (Cursor.MOVE);

        });
        h.setOnMouseReleased(ev -> h.setCursor (Cursor.DEFAULT));
        h.setOnMouseDragged(ev ->
        {

            ev.consume ();

            double diffx = ev.getSceneX () - _this.dx;
            double diffy = ev.getSceneY () - _this.dy;

            _this.mx = _this.mx + diffx;
            _this.my = _this.my + diffy;

            Point2D p = _this.localToScene (ev.getX (), ev.getY ());

            Bounds _b = vb.localToScene (vb.getBoundsInLocal ());
/*
            if ((diffx < 1)
                &&
                ((_b.getMinX () <= 0)
                 ||
                 (_b.getMinY () <= 0)
                )
               )
            {

                _this.mx = _this.getLayoutX ();
                _this.my = _this.getLayoutY ();

                _this.dx = ev.getSceneX();
                _this.dy = ev.getSceneY();

                _this.relocate (0, _this.my);

                return;

            }

            if ((diffx > 0)
                &&
                (_b.getWidth () + _b.getMinX () >= _this.getScene ().getWidth ())
               )
            {

                _this.dx = ev.getSceneX ();
                _this.dy = ev.getSceneY ();

                return;

            }
*/

            _this.moving = true;

            _this.relocate (_this.mx, _this.my);

            _this.moving = false;

            _this.dx = ev.getSceneX ();
            _this.dy = ev.getSceneY ();

        });
        h.setOnMouseEntered(ev ->
        {
            if (!ev.isPrimaryButtonDown ())
            {

                h.setCursor (Cursor.MOVE);

            }
        });
        h.setOnMouseExited(ev ->
        {
            if (!ev.isPrimaryButtonDown())
            {
                h.setCursor(Cursor.DEFAULT);
            }
        });

        UIUtils.doOnKeyReleased (this,
                                 KeyCode.F4,
                                 () ->
                                 {

                                     this.close ();

                                 });

        if (b.show)
        {

            this.show ();

        }
/*
        this.boundsInParentProperty ().addListener ((pr, oldv, newv) ->
        {
            if ((!_this.isVisible ())
                ||
                (_this.moving)
               )
            {
System.out.println ("MOVING: " + _this.moving);
                return;

            }

            if (oldv.getMinY () != newv.getMinY ())
            {
System.out.println ("HERE: " + oldv.getMinY () + ", " + newv.getMinY () + ", " + (oldv.getMinY () - newv.getMinY ()));
                double diff = oldv.getMinY () - newv.getMinY ();
                _this.moving = true;

                int nh = (int) _this.prefHeight (_this.getWidth ());

                if (oldv.getHeight () > 0)
                {

                    diff = (nh - oldv.getHeight ()) - diff;

                }

                //oldv = _this.localToScene (oldv);
                System.out.println ("HERE2: " + oldv.getMinY () + ", " + newv.getMinY () + ", " + (oldv.getMinY () - newv.getMinY ()));

                _this.resizeRelocate (oldv.getMinX (),
                                      oldv.getMinY () + diff,
                                      _this.getWidth (),
                                      _this.prefHeight (_this.getWidth ()));

_this.moving = false;
            }

        });
*/
    }

    public void addChildPopup (QuollPopup qp)
    {

        this.childPopups.add (qp);

    }

    public ObjectProperty<Header> headerProperty ()
    {

        return this.headerProp;

    }

    /**
     * Get a builder to create a new QuollPopup.
     *
     * Usage: QuollPopup.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static QuollPopup.Builder builder ()
    {

        return new Builder ();

    }

    private void _close ()
    {

        this.childPopups.stream ()
            .forEach (p -> p.close ());

        if (this.onClose != null)
        {

            UIUtils.runLater (this.onClose);

        }

    }

    public void close ()
    {

        this.setVisible (false);

        this._close ();

        if (this.removeOnClose)
        {

            this.viewer.removePopup (this);

        }

        this.fireEvent (new PopupEvent (this,
                                        PopupEvent.CLOSED_EVENT));

    }

    public void show ()
    {

        this.show (-1, -1);

    }

    public void show (int x,
                      int y)
    {

        this.viewer.showPopup (this,
                               x,
                               y);

        this.fireEvent (new PopupEvent (this,
                                        PopupEvent.SHOWN_EVENT));

    }

    public void setPopupId (String id)
    {

        this.popupId = id;

    }

    public String getPopupId ()
    {

        return this.popupId;

    }

    public static class Builder implements IBuilder<Builder, QuollPopup>
    {

        private StringProperty title = null;
        private String styleName = null;
        private Set<Node> controls = null;
        private Node content = null;
        private boolean withClose = false;
        private boolean hideOnEscape = false;
        private Runnable onClose = null;
        private String popupId = null;
        private AbstractViewer viewer = null;
        private boolean show = false;
        private boolean removeOnClose = false;

        private Builder ()
        {

        }

        @Override
        public QuollPopup build ()
        {

            return new QuollPopup (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder show ()
        {

            this.show = true;
            return this;

        }

        public Builder withViewer (AbstractViewer viewer)
        {

            this.viewer = viewer;
            return this;

        }

        public Builder popupId (String id)
        {

            this.popupId = id;
            return this;

        }

        public Builder removeOnClose (Boolean b)
        {

            this.removeOnClose = b;
            return this;

        }

        public Builder onClose (Runnable r)
        {

            this.onClose = r;
            return this;

        }

        public Builder hideOnEscape (boolean v)
        {

            this.hideOnEscape = v;
            return this;

        }

        public Builder withClose (boolean v)
        {

            this.withClose = v;
            return this;

        }

        public Builder content (Node c)
        {

            this.content = c;
            return this;

        }

        public Builder controls (Set<Node> c)
        {

            this.controls = c;

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

    }

    public static class PopupEvent extends Event
    {

        public static final EventType<PopupEvent> CLOSED_EVENT = new EventType<> ("popup.closed");
        public static final EventType<PopupEvent> SHOWN_EVENT = new EventType<> ("popup.shown");

        private QuollPopup popup = null;

        public PopupEvent (QuollPopup            popup,
                           EventType<PopupEvent> type)
        {

            super (type);

            this.popup = popup;

        }

        public QuollPopup getPopup ()
        {

            return this.popup;

        }

    }

}
