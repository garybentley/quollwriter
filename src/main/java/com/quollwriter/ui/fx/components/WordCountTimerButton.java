package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.geometry.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class WordCountTimerButton extends HBox
{

    private static final String POPUPID = "wctimer";

    private QuollButton timerBut = null;
    private ControllableProgressBar timerProgress = null;
    private WordCountProgressTimer wctimer = null;
    private AbstractProjectViewer viewer = null;
    private Runnable onMinutesComplete = null;
    private Runnable onWordsComplete = null;
    private int words = 0;
    private int mins = 0;

    private WordCountTimerButton (Builder b)
    {

        this.viewer = b.viewer;
        this.onMinutesComplete = b.onMinutesComplete;
        this.onWordsComplete = b.onWordsComplete;

        if (b.styleClass != null)
        {

            this.getStyleClass ().add (b.styleClass);

        }

        this.timerProgress = ControllableProgressBar.builder ()
            .allowStop (false)
            .allowPause (true)
            .allowReset (false)
            .allowPlay (true)
            .styleClassName (StyleClassNames.PROGRESS)
            .build ();
        this.timerProgress.managedProperty ().bind (this.timerProgress.visibleProperty ());
        this.timerProgress.setVisible (false);

        this.timerProgress.progressProperty ().addListener ((pr, oldv, newv) ->
        {

          if (newv.doubleValue () >= 1)
          {

               if (this.wctimer.getMinutesRemaining () <= 0)
               {

                   UIUtils.runLater (() ->
                   {

                       this.viewer.showNotificationPopup (getUILanguageStringProperty (timer,complete,time,popup,title),
                                                          getUILanguageStringProperty (Arrays.asList (timer,complete,time,popup,text),
                                                                                       Environment.formatNumber (mins)),
                                                          30);

                       UIUtils.runLater (this.onMinutesComplete);

                       this.reset ();

                   });

               }

               if (this.wctimer.getWordsRemaining () <= 0)
               {

                   UIUtils.runLater (() ->
                   {

                       this.viewer.showNotificationPopup (getUILanguageStringProperty (timer,complete,LanguageStrings.words,popup,title),
                                                          getUILanguageStringProperty (Arrays.asList (timer,complete,LanguageStrings.words,popup,text),
                                                                                       Environment.formatNumber (words)),
                                                          30);

                       UIUtils.runLater (this.onWordsComplete);

                       this.reset ();

                   });

               }

          }

        });

        this.timerBut = QuollButton.builder ()
            .tooltip (b.buttonTooltip)
            .iconName (StyleClassNames.TIMER)
            .build ();
        this.timerBut.managedProperty ().bind (this.timerBut.visibleProperty ());
        this.timerBut.setOnAction (ev ->
        {

            QuollPopup qp = b.viewer.getPopupById (POPUPID);

            if (qp != null)
            {

                qp.show (this.timerBut,
                         Side.BOTTOM);

                if (b.onPopupShow != null)
                {

                    UIUtils.runLater (() ->
                    {

                        b.onPopupShow.accept (qp);

                    });

                }

                return;

            }

            WordCountTimerSelectPopup p = new WordCountTimerSelectPopup (b.viewer,
                                                                         (mins, words) ->
            {

                this.start (mins,
                            words);

                b.viewer.getPopupById (POPUPID).close ();

            });
            p.getPopup ().setOnClose (() ->
            {

                if (b.onPopupClose != null)
                {

                    UIUtils.runLater (() ->
                    {

                        b.onPopupClose.accept (p.getPopup ());

                    });

                }

            });
            p.getPopup ().setPopupId (POPUPID);
            p.getPopup ().show (this.timerBut,
                                Side.BOTTOM);

        });

        this.getChildren ().addAll (this.timerBut, this.timerProgress);

    }

    public void reset ()
    {

        this.timerProgress.setVisible (false);
        this.timerBut.setVisible (true);

    }

    public void start (int mins,
                       int words)
    {

        this.timerProgress.setVisible (true);
        this.timerBut.setVisible (false);
        this.mins = mins;
        this.words = words;

        if (this.wctimer != null)
        {

            this.wctimer.stop ();

        }

        this.timerProgress.stateProperty ().setValue (ControllableProgressBar.State.playing);
        this.timerProgress.setProgress (0.1d);

        this.wctimer = new WordCountProgressTimer (this.viewer,
                                                   2, //mins,
                                                   2, //words,
                                                   this.timerProgress.getProgressBar ());

/*
        this.timerProgress.getProgressBar ().addEventHandler (ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                                                              evv ->
        {

            this.allowHeaderHide = false;

            Environment.schedule (() ->
            {

                this.allowHeaderHide = true;

            },
            2000,
            -1);

        });
*/
        this.timerProgress.stateProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv == ControllableProgressBar.State.canceled)
            {

                this.wctimer.stopTimer ();
                this.timerBut.setVisible (true);
                this.timerProgress.setVisible (false);
                return;

            }

            if (newv == ControllableProgressBar.State.reset)
            {

                this.wctimer.stopTimer ();
                this.viewer.getPopupById (POPUPID).show (this.timerBut,
                                                         Side.BOTTOM);
                return;

            }

            if (newv == ControllableProgressBar.State.stopped)
            {

                this.wctimer.stopTimer ();
                return;

            }

            if (newv == ControllableProgressBar.State.paused)
            {

                this.wctimer.pauseTimer ();
                return;

            }

            if (newv == ControllableProgressBar.State.playing)
            {

                this.wctimer.unpauseTimer ();
                return;

            }

        });

    }

    public static Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, WordCountTimerButton>
    {

        private Consumer<QuollPopup> onPopupShow = null;
        private Consumer<QuollPopup> onPopupClose = null;
        private String styleClass = null;
        private AbstractProjectViewer viewer = null;
        private Runnable onMinutesComplete = null;
        private Runnable onWordsComplete = null;
        private StringProperty buttonTooltip = null;

        private Builder ()
        {

        }

        @Override
        public WordCountTimerButton build ()
        {

            return new WordCountTimerButton (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder styleClassName (String s)
        {

            this.styleClass = s;
            return _this ();

        }

        public Builder onPopupShow (Consumer<QuollPopup> s)
        {

            this.onPopupShow = s;
            return _this ();

        }

        public Builder onPopupClose (Consumer<QuollPopup> s)
        {

            this.onPopupClose = s;
            return _this ();

        }

        public Builder inViewer (AbstractProjectViewer v)
        {

            this.viewer = v;
            return _this ();

        }

        public Builder onWordsComplete (Runnable r)
        {

            this.onWordsComplete = r;
            return _this ();

        }

        public Builder onMinutesComplete (Runnable r)
        {

            this.onMinutesComplete = r;
            return _this ();

        }

        public Builder buttonTooltip (StringProperty p)
        {

            this.buttonTooltip = p;
            return _this ();

        }

    }

}
