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

public class BasicPopupsViewer extends Stage
{

    private IPropertyBinder binder = new PropertyBinder ();

    public BasicPopupsViewer (QuollPopup qp)
    {

        //super (StageStyle.TRANSPARENT);

        this.titleProperty ().bind (qp.getHeader ().titleProperty ());

        VBox popupPane = new VBox ();
        popupPane.getStyleClass ().add (StyleClassNames.POPUPVIEWER);
        popupPane.getChildren ().add (qp);

        Scene s = new Scene (popupPane);
        this.setScene (s);

        this.binder.addSetChangeListener (Environment.getStyleSheets (),
                                          ev ->
        {

            if (ev.wasAdded ())
            {

                this.addStyleSheet (ev.getElementAdded ());

            }

            if (ev.wasRemoved ())
            {

                this.removeStyleSheet (ev.getElementRemoved ());

            }

        });

        qp.addEventHandler (QuollPopup.PopupEvent.CLOSED_EVENT,
                            ev ->
        {

            this.hide ();

        });

        this.addEventHandler (Viewer.ViewerEvent.CLOSE_EVENT,
                              ev ->
        {

            this.binder.dispose ();

        });

        Environment.getStyleSheets ().stream ()
            .forEach (u ->
            {

                this.addStyleSheet (u);

            });

        this.getIcons ().addAll (Environment.getWindowIcons ());

        this.setResizable (false);
        this.initStyle (StageStyle.UNIFIED);

        //this.sizeToScene ();

        this.show ();

        popupPane.boundsInParentProperty ().addListener ((pr, oldv, newv) ->
        {

            //this.setHeight (popupPane.getHeight ());
            this.sizeToScene ();

        });

        popupPane.prefHeightProperty ().addListener ((pr, oldv, newv) ->
        {

            //this.setHeight (popupPane.getHeight ());
            this.sizeToScene ();

        });

        popupPane.prefWidthProperty ().addListener ((pr, oldv, newv) ->
        {

            //this.setWidth (popupPane.getWidth ());
            this.sizeToScene ();

        });

        UIUtils.runLater (() ->
        {

            this.sizeToScene ();

        });

    }

    public void removeStyleSheet (URL url)
    {

        this.getScene ().getStylesheets ().remove (url.toExternalForm ());

    }

    public void addStyleSheet (URL url)
    {

        this.getScene ().getStylesheets ().add (url.toExternalForm ());

    }

}
