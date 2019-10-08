package com.quollwriter.ui.fx.components;

import javafx.scene.layout.*;
import javafx.scene.input.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.viewers.*;

public class BackgroundPane extends Pane implements Stateful
{

    private String BG_ID = "bgid";
    private String BG_OPACITY = "bgopacity";

    private AbstractViewer viewer = null;
    private BackgroundObject backgroundObject = null;

    public BackgroundPane (AbstractViewer viewer)
    {

        this.viewer = viewer;
        this.getStyleClass ().add (StyleClassNames.BACKGROUND);
        this.backgroundObject = new BackgroundObject ();
        this.backgroundObject.backgroundProperty ().addListener ((p, oldv, newv) ->
        {

            this.updateBackground ();

        });

        this.addEventHandler (ScrollEvent.SCROLL,
                              ev ->
        {

            if ((ev.isShiftDown ())
                &&
                // We have to break encapsulation here because we know that AbstractView uses this same type of event for ui font scaling.
                (!ev.isControlDown ())
               )
            {

                double o = this.getOpacity ();

                if (ev.getDeltaX () > 0)
                {

                    o += 0.05;

                } else {

                    o -= 0.05;

                }

                if (o < 0)
                {

                    o = 0.05;

                }

                if (o > 1)
                {

                    o = 1;

                }

                this.setOpacity (o);
                ev.consume ();

            }

        });

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        if (s == null)
        {

            return;

        }

        try
        {

            Object obj = BackgroundObject.createBackgroundObjectForId (s.getAsString (BG_ID));

            if (obj != null)
            {

                this.setBackgroundObject (obj);

            }

            this.setOpacity (s.getAsFloat (BG_OPACITY, 1f));

        } catch (Exception e) {

            throw new GeneralException ("Unable to init background from state: " + s,
                                        e);

        }

    }

    @Override
    public State getState ()
    {

        State s = new State ();
        s.set (BG_ID,
               this.backgroundObject.getAsString ());
        s.set (BG_OPACITY,
               this.getOpacity ());
        return s;

    }

    public BackgroundObject getBackgroundObject ()
    {

        return this.backgroundObject;

    }

    public void showBackgroundSelector ()
    {

        try
        {

            QuollPopup qp = this.viewer.getPopupById (SelectBGPopup.POPUP_ID);

            if (qp != null)
            {

                qp.show ();

                return;

            }

            SelectBGPopup p = new SelectBGPopup (this.viewer,
                                                 this.backgroundObject.getBackgroundObject ());
            p.show ();

            p.selectedProperty ().addListener ((pr, oldv, newv) ->
            {

                try
                {

                    this.backgroundObject.update (newv);

                } catch (Exception e) {

                    // TODO Show error to user?
                    Environment.logError ("Unable to update background for value: " + newv,
                                          e);

                }

            });

        } catch (Exception e) {

            Environment.logError ("Unable to show background selector",
                                  e);

        }

    }

    public void setBackgroundObject (Object v)
                              throws Exception
    {

        this.backgroundObject.update (v);

    }

    private void updateBackground ()
    {

        this.setBackground (this.backgroundObject.getBackground ());
        this.pseudoClassStateChanged (StyleClassNames.USERIMAGE_PSEUDO_CLASS, this.backgroundObject.isUserPath ());

        boolean isImage = this.backgroundObject.isImage ();

        this.pseudoClassStateChanged (StyleClassNames.BGCOLOR_PSEUDO_CLASS, !isImage);
        this.pseudoClassStateChanged (StyleClassNames.BGIMAGE_PSEUDO_CLASS, isImage);

    }

}
