package com.quollwriter.ui.fx.panels;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.popups.*;

/**
 * A base class for content that is suitable for display within a sidebar.
 */
public abstract class PanelContent<E extends AbstractViewer> extends ViewerContent<E> implements Stateful, PanelCreator
{

    public static final String BG_STATE_ID = "bg";

    public interface CommandIds
    {
        String selectbackground = "selectbackground";

    }

    protected Panel panel = null;
    private BooleanProperty readyForUseProp = null;
    private Map<String, Command> actionMap = new HashMap<> ();
    private Pane background = null;

    private BackgroundObject backgroundObject = null;

    public PanelContent (E viewer)
    {

        super (viewer);

        final PanelContent _this = this;

        this.readyForUseProp = new SimpleBooleanProperty (false);
        this.background = new Pane ();
        this.background.getStyleClass ().add (StyleClassNames.BACKGROUND);
        this.backgroundObject = new BackgroundObject ();
        this.backgroundObject.backgroundProperty ().addListener ((p, oldv, newv) ->
        {

            _this.updateBackground ();

        });

        this.getChildren ().add (this.background);

        this.addEventHandler (ScrollEvent.SCROLL,
                              ev ->
        {

            if (ev.isShiftDown ())
            {

                double o = _this.background.getOpacity ();

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

                _this.background.setOpacity (o);
                ev.consume ();

            }

        });

        this.addActionMapping (() ->
        {

            try
            {

                QuollPopup qp = _this.viewer.getPopupById (SelectBGPopup.POPUP_ID);

                if (qp != null)
                {

                    qp.show ();

                    return;

                }

                SelectBGPopup p = new SelectBGPopup (_this.viewer,
                                                     _this.backgroundObject.getBackgroundObject ());
                p.show ();

                p.selectedProperty ().addListener ((pr, oldv, newv) ->
                {

                    try
                    {

                        _this.backgroundObject.update (newv);

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

        },
        CommandIds.selectbackground);

    }

    public void setBackgroundObject (Object v)
                              throws Exception
    {

        this.backgroundObject.update (v);

    }

    public BackgroundObject getBackgroundObject ()
    {

        return this.backgroundObject;

    }

    @Override
    public State getState ()
    {

        State s = null;

        if (this.panel != null)
        {

            s = this.panel.getState ();

        }

        if (s == null)
        {

            s = new State ();

        }

        s.set (BG_STATE_ID,
               this.backgroundObject.getAsString ());

        return s;

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        final PanelContent _this = this;

        if (this.panel != null)
        {

            this.panel.init (s);

        }

        if (s == null)
        {

            return;

        }

        String bgid = s.getAsString (BG_STATE_ID);

        try
        {

            this.backgroundObject.update (BackgroundObject.createBackgroundObjectForId (bgid));

        } catch (Exception e) {

            // TODO Show message?
            Environment.logError ("Unable to set background object for id: " + bgid,
                                  e);

        }

    }

    private void updateBackground ()
    {

        this.background.setBackground (this.backgroundObject.getBackground ());
        this.getPanel ().pseudoClassStateChanged (StyleClassNames.USERIMAGE_PSEUDO_CLASS, this.backgroundObject.isUserPath ());

        boolean isImage = this.backgroundObject.isImage ();

        this.getPanel ().pseudoClassStateChanged (StyleClassNames.BGCOLOR_PSEUDO_CLASS, isImage);
        this.getPanel ().pseudoClassStateChanged (StyleClassNames.BGIMAGE_PSEUDO_CLASS, !isImage);

    }

    public Panel getPanel ()
    {

        if (this.panel == null)
        {

            this.panel = this.createPanel ();

        }

        return this.panel;

    }

    public ReadOnlyBooleanProperty readyForUseProperty ()
    {

        // This will be read only.
        return this.readyForUseProp.readOnlyBooleanProperty (this.readyForUseProp);

    }

    protected void addActionMapping (Command command)
    {

        for (String c : command.commandIds ())
        {

            this.actionMap.put (c,
                                command);

        }

    }

    protected void addActionMapping (Runnable  action,
                                     String... commands)
    {

        this.addActionMapping (Command.create (action,
                                               commands));

    }

    /**
     * Run the specified command, the command must map to one of the enum values.
     *
     * @param command The command to run.
     */
    public void runCommand (String   command)
    {

        this.runCommand (command,
                         null);

    }

    /**
     * Run the specified command.
     *
     * @param command The command to run.
     * @param doAfter An optional runnable to execute after the command has completed.
     */
    public void runCommand (String   command,
                            Runnable doAfter)
    {

        Command r = this.actionMap.get (command);

        if (r == null)
        {

            throw new IllegalArgumentException (String.format ("Command %1$s is not supported.", command));

        }

        // TODO: Check error handling, maybe wrap in a try/catch.
        r.run (doAfter);

    }

    public void setReadyForUse ()
    {

        if (this.readyForUseProp.getValue ())
        {

            return;

        }

        this.readyForUseProp.setValue (true);

    }

}
