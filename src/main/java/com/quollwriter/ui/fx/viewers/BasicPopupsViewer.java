package com.quollwriter.ui.fx.viewers;

import java.net.*;

import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.data.PropertyBinder;
import com.quollwriter.ui.fx.components.*;

public class BasicPopupsViewer extends VBox implements ViewerCreator,
                                                       Stateful
{

    private IPropertyBinder binder = new PropertyBinder ();
    private Viewer viewer = null;
    private QuollPopup qp = null;

    public BasicPopupsViewer (QuollPopup qp)
    {

        //super (StageStyle.TRANSPARENT);
        this.qp = qp;

        VBox.setVgrow (this,
                       Priority.ALWAYS);

        VBox popupPane = new VBox ();
        popupPane.getStyleClass ().add (StyleClassNames.POPUPVIEWER);
        popupPane.getChildren ().add (qp);
        VBox.setVgrow (popupPane,
                       Priority.ALWAYS);

        this.getChildren ().add (popupPane);

        qp.addEventHandler (QuollPopup.PopupEvent.CLOSED_EVENT,
                            ev ->
        {

            this.getViewer ().hide ();

        });

        this.addEventHandler (Viewer.ViewerEvent.CLOSE_EVENT,
                              ev ->
        {

            this.binder.dispose ();

        });

        popupPane.prefHeightProperty ().addListener ((pr, oldv, newv) ->
        {

            this.getViewer ().setHeight (popupPane.getHeight ());
            //this.getViewer ().sizeToScene ();

        });

    }
/*
    public void removeStyleSheet (URL url)
    {

        this.getScene ().getStylesheets ().remove (url.toExternalForm ());

    }

    public void addStyleSheet (URL url)
    {

        this.getScene ().getStylesheets ().add (url.toExternalForm ());

    }
*/

    @Override
    public State getState ()
    {

        State s = this.getViewer ().getState ();

        if (s == null)
        {

            s = new State ();

        }

        return s;

    }


    @Override
    public void init (State s)
               throws GeneralException
    {

        this.getViewer ().sizeToScene ();

        //this.getViewer ().init (s);

        UIUtils.forceRunLater (() ->
        {

            this.getViewer ().show ();
            //this.getViewer ().setResizable (false);

        });

    }

    public Viewer getViewer ()
    {

        if (this.viewer == null)
        {

            throw new IllegalStateException ("Viewer has not been created.");

        }

        return this.viewer;

    }

    @Override
    public Viewer createViewer ()
    {

        if (this.viewer != null)
        {

            return this.viewer;

        }

        Viewer v = Viewer.builder ()
            //.headerControls (headerCons)
            //.headerToolbar (ctb)
            //.styleClassName (this.getStyleClassName ())
            .styleSheet ("popup")
            .content (this)
            .title (this.qp.getHeader ().titleProperty ())
            .build ();

        v.setResizable (true);

        this.viewer = v;

        // Best place for this?
        v.getScene ().addEventFilter (javafx.scene.input.MouseEvent.MOUSE_RELEASED,
                                      ev ->
        {

            if (!Environment.isDebugModeEnabled ())
            {

                return;

            }

            if (!ev.isShortcutDown ())
            {

                return;

            }

            try
            {

                ev.consume ();

                Environment.showCSSViewer (this.getViewer (),
                                           (Node) ev.getTarget ());

            } catch (Exception e) {

                Environment.logError ("Unable to show css viewer for node: " + ev.getTarget (),
                                      e);

            }

        });

        return v;

    }

}
