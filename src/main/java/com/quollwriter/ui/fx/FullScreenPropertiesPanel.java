package com.quollwriter.ui.fx;

import java.util.*;

import javafx.beans.value.*;
import javafx.scene.*;
import javafx.geometry.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class FullScreenPropertiesPanel extends VBox
{

    private BackgroundPane bgsel = null;
    private Pane bgsize = null;
    private Slider bgopacity = null;
    private QuollCheckBox bgshowwctime = null;
    private IPropertyBinder binder = null;
    private boolean ignoreChanges = false;

    public FullScreenPropertiesPanel (AbstractProjectViewer viewer,
                                      IPropertyBinder       binder)
    {

        this.getStyleClass ().add (StyleClassNames.FULLSCREEN);

        Form.Builder fb = Form.builder ();

        this.bgsel = new BackgroundPane (viewer);

        try
        {

            this.bgsel.setBackgroundObject (UserProperties.getFullScreenBackground ());

        } catch (Exception e) {

            Environment.logError ("Unable to set full screen background to: " +
                                  UserProperties.getFullScreenBackground (),
                                  e);

        }

        this.bgsel.setOnMouseClicked (ev ->
        {

            this.bgsel.showBackgroundSelector ();

        });

        this.bgsel.getBackgroundObject ().backgroundProperty ().addListener ((pr, oldv, newv) ->
        {

            try
            {

                this.ignoreChanges = true;

                UserProperties.setFullScreenBackground (this.bgsel.getBackgroundObject ().getBackgroundObject ());

            } finally {

                this.ignoreChanges = false;

            }

        });

        binder.addChangeListener (UserProperties.fullScreenBackgroundProperty (),
                                  (pr, oldv, newv) ->
        {

            if (this.ignoreChanges)
            {

                return;

            }

            try
            {

                this.bgsel.setBackgroundObject (newv);

            } catch (Exception e) {

                Environment.logError ("Unable to set full screen background to: " +
                                      newv,
                                      e);

            }

        });

        fb.item (getUILanguageStringProperty (project,sidebar,fullscreenproperties,selectbackground,text),
                 this.bgsel);

        fb.item (QuollHyperlink.builder ()
            .label (getUILanguageStringProperty (project,sidebar,fullscreenproperties,bgimagewebsites,text))
            .styleClassName (StyleClassNames.SELECTBG)
            .onAction (ev ->
            {

                 // Open the url.
                 UIUtils.openURL (viewer,
                                  Constants.QUOLLWRITER_PROTOCOL + ":resources.html#bgimages");

            })
            .build ());

        this.bgopacity = new Slider (0, 1, UserProperties.getFullScreenOpacity ());
        UIUtils.setTooltip (this.bgopacity,
                            getUILanguageStringProperty (project,sidebar,fullscreenproperties,LanguageStrings.bgopacity,tooltip));
        this.bgopacity.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            try
            {

                this.ignoreChanges = true;

                UserProperties.setFullScreenOpacity (newv.doubleValue ());

            } finally {


                this.ignoreChanges = false;

            }

        });

        binder.addChangeListener (UserProperties.fullScreenOpacityProperty (),
                                  (pr, oldv, newv) ->
        {

            if (this.ignoreChanges)
            {

                return;

            }

            this.bgopacity.setValue (newv.doubleValue ());

        });

        fb.item (getUILanguageStringProperty (project,sidebar,fullscreenproperties,LanguageStrings.bgopacity,text),
                 this.bgopacity);

        int border = 2;

        this.bgsize = new Pane ();
        this.bgsize.getStyleClass ().add (StyleClassNames.INNER);
        Pane bgsizeWrapper = new Pane ();
        bgsizeWrapper.getChildren ().add (this.bgsize);
        bgsizeWrapper.getStyleClass ().add (StyleClassNames.BACKGROUNDSIZEBOX);
        UIUtils.setTooltip (this.bgsize,
                            getUILanguageStringProperty (project,sidebar,fullscreenproperties,areasize,tooltip));

        ChangeListener<Number> resizeRelocate = (pr, oldv, newv) ->
        {

            double w = bgsizeWrapper.getWidth ();
            double h = bgsizeWrapper.getHeight ();

            double xbw = UserProperties.getFullScreenXBorderWidth ();
            double ybw = UserProperties.getFullScreenYBorderWidth ();

            double cx = w * xbw;
            double cy = h * ybw;

            this.bgsize.setPrefSize ((double) Math.round (w - (2 * w * xbw) - (2 * border)),
                                     (double) Math.round (h - (2 * h * ybw) - (2 * border)));
            this.bgsize.relocate (Math.round (cx), Math.round (cy));

        };

        binder.addChangeListener (UserProperties.fullScreenXBorderWidthProperty (),
                                  resizeRelocate);
        binder.addChangeListener (UserProperties.fullScreenYBorderWidthProperty (),
                                  resizeRelocate);

        this.sceneProperty ().addListener ((pr, oldv, newv) ->
        {

            if ((oldv == null)
                &&
                (newv != null)
               )
            {

                UIUtils.runLater (() ->
                {

                    resizeRelocate.changed (UserProperties.fullScreenXBorderWidthProperty (),
                                            0,
                                            0);

                });

            }

        });

        fb.item (getUILanguageStringProperty (project,sidebar,fullscreenproperties,areasize,text),
                 bgsizeWrapper);

        DragResizer mdr = DragResizer.makeResizable (this.bgsize,
                                   border,
                                   Side.TOP,
                                   Side.BOTTOM,
                                   Side.LEFT,
                                   Side.RIGHT);

        mdr.setOnDraggingLeft (() ->
        {

           if (mdr.getDragZone () == Side.RIGHT)
           {

               UserProperties.incrementFullScreenXBorderWidth ();

           }

           if (mdr.getDragZone () == Side.LEFT)
           {

               UserProperties.decrementFullScreenXBorderWidth ();

           }

       });

       mdr.setOnDraggingRight (() ->
       {

           if (mdr.getDragZone () == Side.RIGHT)
           {

               UserProperties.decrementFullScreenXBorderWidth ();

           }

           if (mdr.getDragZone () == Side.LEFT)
           {

               UserProperties.incrementFullScreenXBorderWidth ();

           }

       });

       mdr.setOnDraggingUp (() ->
       {

           if (mdr.getDragZone () == Side.TOP)
           {

               UserProperties.decrementFullScreenYBorderWidth ();

           }

           if (mdr.getDragZone () == Side.BOTTOM)
           {

               UserProperties.incrementFullScreenYBorderWidth ();

           }

       });

       mdr.setOnDraggingDown (() ->
       {

           if (mdr.getDragZone () == Side.TOP)
           {

               UserProperties.incrementFullScreenYBorderWidth ();

           }

           if (mdr.getDragZone () == Side.BOTTOM)
           {

               UserProperties.decrementFullScreenYBorderWidth ();

           }

       });

        this.bgshowwctime = QuollCheckBox.builder ()
            .label (project,sidebar,fullscreenproperties,showtimewordcount,text)
            .userProperty (Constants.FULL_SCREEN_SHOW_TIME_WORD_COUNT_PROPERTY_NAME)
            .build ();

        fb.item (this.bgshowwctime);

        this.getChildren ().add (fb.build ());

    }

}
