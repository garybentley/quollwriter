package com.quollwriter.ui.fx.components;

import javafx.beans.property.*;
import javafx.event.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ControllableProgressBar extends HBox
{

    public enum State
    {
        paused,
        stopped,
        playing,
        reset,
        canceled
    }

    private QuollButton playBut = null;
    private QuollButton stopBut = null;
    private QuollButton pauseBut = null;
    private QuollButton resetBut = null;
    private ProgressBar progressBar = null;
    private ObjectProperty<State> stateProp = null;

    private ControllableProgressBar (Builder b)
    {

        this.getStyleClass ().add (b.styleClassName);

        this.stateProp = new SimpleObjectProperty<> (State.stopped);

        this.progressBar = new ProgressBar ();

        this.progressBar.setOnContextMenuRequested (ev ->
        {

            QuollContextMenu m = QuollContextMenu.builder ()
                .build ();

            if (this.stateProp.getValue () == State.playing)
            {

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (timer,popupmenu,items,pause)
                    .styleClassName (StyleClassNames.PAUSE)
                    .onAction (eev ->
                    {

                        this.stateProp.setValue (State.paused);

                    })
                    .build ());

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (timer,popupmenu,items,stop)
                    .styleClassName (StyleClassNames.STOP)
                    .onAction (eev ->
                    {

                        this.stateProp.setValue (State.stopped);

                    })
                    .build ());

            }

            if (this.stateProp.getValue () == State.stopped)
            {

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (timer,popupmenu,items,play)
                    .styleClassName (StyleClassNames.PLAY)
                    .onAction (eev ->
                    {

                        this.stateProp.setValue (State.playing);

                    })
                    .build ());

            }

            if (this.stateProp.getValue () == State.paused)
            {

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (timer,popupmenu,items,play)
                    .styleClassName (StyleClassNames.PLAY)
                    .onAction (eev ->
                    {

                        this.stateProp.setValue (State.playing);

                    })
                    .build ());

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (timer,popupmenu,items,stop)
                    .styleClassName (StyleClassNames.STOP)
                    .onAction (eev ->
                    {

                        this.stateProp.setValue (State.stopped);

                    })
                    .build ());

            }

            m.getItems ().add (QuollMenuItem.builder ()
                .label (timer,popupmenu,items,reset)
                .styleClassName (StyleClassNames.RESET)
                .onAction (eev ->
                {

                    this.stateProp.setValue (State.stopped);
                    this.stateProp.setValue (State.reset);

                })
                .build ());

            m.getItems ().add (QuollMenuItem.builder ()
                .label (timer,popupmenu,items,cancel)
                .styleClassName (StyleClassNames.CANCEL)
                .onAction (eev ->
                {

                    this.stateProp.setValue (State.canceled);

                })
                .build ());

            this.progressBar.setContextMenu (m);
/*
            this.progressBar.getProperties ().put ("context-menu", m);

            m.setAutoFix (true);
            m.setAutoHide (true);
            m.setHideOnEscape (true);
            m.show (this,
                    ev.getScreenX (),
                    ev.getScreenY ());
*/
        });

        this.playBut = QuollButton.builder ()
            .styleClassName (StyleClassNames.PLAY)
            .onAction (ev ->
            {

                this.stateProp.setValue (State.playing);
                /*
                this.stopBut.setDisable (false);
                this.stopBut.setVisible (b.allowStop);
                this.playBut.setVisible (false);
                this.pauseBut.setVisible (b.allowPause);
                this.fireEvent (new ProgressButtonEvent (this.progressBar,
                                                         ProgressButtonEvent.PLAY_EVENT));
*/
            })
            .build ();
        this.playBut.managedProperty ().bind (this.playBut.visibleProperty ());
        this.playBut.setVisible (b.allowPlay);

        this.stopBut = QuollButton.builder ()
            .styleClassName (StyleClassNames.STOP)
            .onAction (ev ->
            {

                this.stateProp.setValue (State.stopped);
                /*
                this.stopBut.setDisable (true);
                this.playBut.setVisible (b.allowPlay);
                this.resetBut.setVisible (false);
                this.pauseBut.setVisible (false);
                this.fireEvent (new ProgressButtonEvent (this.progressBar,
                                                         ProgressButtonEvent.STOP_EVENT));
*/
            })
            .build ();
        this.stopBut.managedProperty ().bind (this.stopBut.visibleProperty ());
        this.stopBut.setVisible (b.allowStop);
        this.stopBut.setDisable (true);

        this.pauseBut = QuollButton.builder ()
            .styleClassName (StyleClassNames.PAUSE)
            .onAction (ev ->
            {

                this.stateProp.setValue (State.paused);
                /*
                this.playBut.setVisible (b.allowPlay);
                this.pauseBut.setVisible (false);
                this.resetBut.setVisible (false);
                this.fireEvent (new ProgressButtonEvent (this.progressBar,
                                                         ProgressButtonEvent.PAUSE_EVENT));
*/
            })
            .build ();
        this.pauseBut.managedProperty ().bind (this.pauseBut.visibleProperty ());
        this.pauseBut.setVisible (false);

        this.resetBut = QuollButton.builder ()
            .styleClassName (StyleClassNames.RESET)
            .onAction (ev ->
            {
/*
                this.stateProp.setValue (State.stopped);
                this.playBut.setVisible (b.allowPlay);
                this.resetBut.setVisible (false);
                this.pauseBut.setVisible (false);
*/
                this.progressBar.setProgress (0);
                this.fireEvent (new ProgressButtonEvent (this.progressBar,
                                                         ProgressButtonEvent.RESET_EVENT));

            })
            .build ();
        this.resetBut.managedProperty ().bind (this.resetBut.visibleProperty ());
        this.resetBut.setVisible (false);

        this.getChildren ().addAll (this.progressBar, this.playBut, this.pauseBut, this.resetBut, this.stopBut);

        this.progressBar.progressProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv.doubleValue () >= 1)
            {

                this.resetBut.setVisible (b.allowReset);
                this.stopBut.setVisible (false);
                return;

            }

            if (newv.doubleValue () <= 0)
            {

                this.playBut.setVisible (b.allowPlay);
                this.stopBut.setVisible (false);
                this.resetBut.setVisible (false);
                return;

            }

            this.stopBut.setVisible (b.allowStop);
            this.pauseBut.setVisible (b.allowPause);
            this.playBut.setVisible (false);
            this.resetBut.setVisible (false);

        });

        this.stateProp.addListener ((pr, oldv, newv) ->
        {

            if (newv == State.playing)
            {

                this.fireEvent (new ProgressButtonEvent (this.progressBar,
                                                         ProgressButtonEvent.PLAY_EVENT));

                this.stopBut.setVisible (b.allowStop);
                this.stopBut.setDisable (false);
                this.resetBut.setVisible (false);
                this.pauseBut.setVisible (b.allowPause);
                this.playBut.setVisible (false);

            }

            if (newv == State.stopped)
            {

                this.fireEvent (new ProgressButtonEvent (this.progressBar,
                                                         ProgressButtonEvent.STOP_EVENT));

                this.stopBut.setDisable (true);
                this.resetBut.setVisible (b.allowReset);
                this.playBut.setVisible (false);
                this.pauseBut.setVisible (false);

            }

            if (newv == State.paused)
            {

                this.fireEvent (new ProgressButtonEvent (this.progressBar,
                                                         ProgressButtonEvent.PAUSE_EVENT));

                this.playBut.setVisible (b.allowPlay);
                this.stopBut.setVisible (b.allowStop);
                this.stopBut.setDisable (false);
                this.resetBut.setVisible (false);
                this.pauseBut.setVisible (false);

            }

        });

        this.stateProp.setValue (State.playing);

    }

    public ObjectProperty<State> stateProperty ()
    {

        return this.stateProp;

    }

    public double getProgress ()
    {

        return this.progressBar.getProgress ();

    }

    public void setProgress (double v)
    {

        this.progressBar.setProgress (v);

    }

    public ProgressBar getProgressBar ()
    {

        return this.progressBar;

    }

    public DoubleProperty progressProperty ()
    {

        return this.progressBar.progressProperty ();

    }

    public static ControllableProgressBar.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, ControllableProgressBar>
    {

        private boolean allowPause = false;
        private boolean allowStop = false;
        private boolean allowPlay = false;
        private boolean allowReset = false;
        private String styleClassName = null;

        private Builder ()
        {

        }

        @Override
        public ControllableProgressBar build ()
        {

            return new ControllableProgressBar (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder styleClassName (String v)
        {

            this.styleClassName = v;
            return _this ();

        }

        public Builder allowStop (boolean v)
        {

            this.allowStop = v;
            return _this ();

        }

        public Builder allowReset (boolean v)
        {

            this.allowReset = v;
            return _this ();

        }

        public Builder allowPlay (boolean v)
        {

            this.allowPlay = v;
            return _this ();

        }

        public Builder allowPause (boolean v)
        {

            this.allowPause = v;
            return _this ();

        }

    }

    public static class ProgressButtonEvent extends Event
    {

        public static final EventType<ProgressButtonEvent> PAUSE_EVENT = new EventType<> ("progressbutton.pause");
        public static final EventType<ProgressButtonEvent> PLAY_EVENT = new EventType<> ("progressbutton.play");
        public static final EventType<ProgressButtonEvent> RESET_EVENT = new EventType<> ("progressbutton.reset");
        public static final EventType<ProgressButtonEvent> STOP_EVENT = new EventType<> ("progressbutton.stop");

        private ProgressBar pb = null;

        public ProgressButtonEvent (ProgressBar           pb,
                                    EventType<ProgressButtonEvent> type)
        {

            super (type);
            this.pb = pb;

        }

        public ProgressBar getProgressBar ()
        {

            return this.pb;

        }

    }

}
