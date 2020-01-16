package com.quollwriter.ui.fx.components;

import java.io.*;
import java.util.*;

import javafx.scene.layout.*;
import javafx.scene.input.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.viewers.*;

public class BackgroundPane extends Pane implements Stateful
{

    public static final String BG_ID = "bgid";
    public static final String BG_OPACITY = "bgopacity";

    private AbstractViewer viewer = null;
    private BackgroundObject backgroundObject = null;
    private Object origBgObject = null;
    private boolean dragImportAllowed = false;
    private String bgselpid = null;

    public BackgroundPane (AbstractViewer viewer)
    {

        this.viewer = viewer;
        this.getStyleClass ().add (StyleClassNames.BACKGROUND);
        this.backgroundObject = new BackgroundObject ();
        this.backgroundObject.backgroundProperty ().addListener ((p, oldv, newv) ->
        {

            this.updateBackground ();

        });

        this.setOnDragDropped (ev ->
        {

            if (this.dragImportAllowed)
            {

                List<File> files = ev.getDragboard ().getFiles ();

                if (files != null)
                {

                    if (files.size () > 1)
                    {

                        return;

                    }

                    File f = files.get (0);

                    if (UIUtils.isImageFile (f))
                    {

                        try
                        {

                            this.origBgObject = null;
                            this.backgroundObject.update (f.toPath ());

                        } catch (Exception e) {

                            Environment.logError ("Unable to set background to path: " + f,
                                                  e);

                        }

                        ev.consume ();

                    }

                }

            }

        });

        this.setOnDragExited (ev ->
        {

            try
            {

                if (this.origBgObject != null)
                {

                    this.backgroundObject.update (this.origBgObject);
                    this.origBgObject = null;

                }

            } catch (Exception e) {

                Environment.logError ("Unable to update background to original object: " +
                                      this.origBgObject,
                                      e);

            }

        });

        this.setOnDragEntered (ev ->
        {

            if (this.dragImportAllowed)
            {

                List<File> files = ev.getDragboard ().getFiles ();

                if (files != null)
                {

                    if (files.size () > 1)
                    {

                        return;

                    }

                    File f = files.get (0);

                    if (UIUtils.isImageFile (f))
                    {

                        ev.acceptTransferModes (TransferMode.COPY_OR_MOVE);

                        try
                        {

                            this.origBgObject = this.backgroundObject.getBackgroundObject ();
                            this.backgroundObject.updateForUserPath (f.toPath ());

                        } catch (Exception e) {

                            Environment.logError ("Unable to set background to path: " + f,
                                                  e);

                        }

                        ev.consume ();

                    }

                }

            }

        });

        this.setOnDragOver (ev ->
        {

            if (this.dragImportAllowed)
            {

                List<File> files = ev.getDragboard ().getFiles ();

                if (files != null)
                {

                    if (files.size () > 1)
                    {

                        return;

                    }

                    File f = files.get (0);

                    if (UIUtils.isImageFile (f))
                    {

                        ev.acceptTransferModes (TransferMode.COPY_OR_MOVE);
                        ev.consume ();

                    }

                }

            }

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

    public void setDragImportAllowed (boolean v)
    {

        this.dragImportAllowed = v;

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

            if (this.bgselpid == null)
            {

                this.bgselpid = "selectbg" + UUID.randomUUID ().toString ();

            }

            QuollPopup qp = this.viewer.getPopupById (this.bgselpid);

            if (qp != null)
            {

                qp.show ();

                return;

            }

            SelectBGPopup p = new SelectBGPopup (this.viewer,
                                                 this.backgroundObject.getBackgroundObject ());
            p.getPopup ().setPopupId (this.bgselpid);
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
