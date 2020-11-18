package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.scene.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class Wizard extends VBox
{

    public static final String NEXT_BUTTON_ID = "next";
    public static final String PREVIOUS_BUTTON_ID = "previous";
    public static final String FINISH_BUTTON_ID = "finish";
    public static final String CANCEL_BUTTON_ID = "cancel";
    private String                 currentStepId = null;
    private Button                 cancelBut = null;
    private Button                 nextBut = null;
    private Button                 prevBut = null;
    private Header                 title = null;
    private VBox                   stepWrapper = null;
    private Function<String, StringProperty> nextButtonLabelProv = null;
    private Function<String, StringProperty> prevButtonLabelProv = null;
    private Function<String, String> nextStepIdProvider = null;
    private Function<String, String> prevStepIdProvider = null;
    private Function<String, Step> stepProv = null;
    private BiPredicate<String, String> nextStepCheck = null;
    private BiPredicate<String, String> prevStepCheck = null;

    private Wizard (Builder b)
    {

        if (b.stepProv == null)
        {

            throw new IllegalArgumentException ("Step provider must be provided.");

        }

        if (b.nextStepIdProvider == null)
        {

            throw new IllegalArgumentException ("Next step id provider must be provided.");

        }

        if (b.prevStepIdProvider == null)
        {

            throw new IllegalArgumentException ("Previous step id provider must be provided.");

        }

        if (b.startStepId == null)
        {

            throw new IllegalArgumentException ("Start step id must be provided.");

        }

        final Wizard _this = this;

        this.stepProv = b.stepProv;
        this.nextStepIdProvider = b.nextStepIdProvider;
        this.prevStepIdProvider = b.prevStepIdProvider;

        this.nextButtonLabelProv = b.nextButtonLabelProv;
        this.prevButtonLabelProv = b.prevButtonLabelProv;

        this.getStyleClass ().add (StyleClassNames.WIZARD);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.onCancel != null)
        {

            this.addEventHandler (WizardEvent.CANCEL_EVENT,
                                  b.onCancel);

        }

        if (b.onFinish != null)
        {

            this.addEventHandler (WizardEvent.FINISH_EVENT,
                                  b.onFinish);

        }

        this.nextStepCheck = b.nextStepCheck;
        this.prevStepCheck = b.prevStepCheck;
/*
        if (b.onNextStep != null)
        {

            this.addEventHandler (WizardEvent.NEXT_STEP_SHOW_EVENT,
                                  b.onNextStep);

        }

        if (b.onPrevStep != null)
        {

            this.addEventHandler (WizardEvent.PREVIOUS_STEP_SHOW_EVENT,
                                  b.onPrevStep);

        }
*/
        if (b.onStepShow != null)
        {

            this.addEventHandler (WizardEvent.STEP_SHOW_EVENT,
                                  b.onStepShow);

        }

        this.title = Header.builder ()
            .build ();

        this.stepWrapper = new VBox ();
        this.stepWrapper.getStyleClass ().add (StyleClassNames.CONTENT);

        this.nextBut = QuollButton.builder ()
            .buttonType (ButtonBar.ButtonData.NEXT_FORWARD)
            .onAction (ev ->
            {

                String nid = _this.nextStepIdProvider.apply (_this.currentStepId);

                if (_this.nextStepCheck != null)
                {

                    if (!_this.nextStepCheck.test (_this.currentStepId,
                                                   nid))
                    {

                        return;

                    }

                }

                if (nid == null)
                {

                    // Fire the finish event.
                    WizardEvent wev = new WizardEvent (_this,
                                                      _this.currentStepId,
                                                      nid,
                                                      _this.prevStepIdProvider.apply (_this.currentStepId),
                                                      WizardEvent.FINISH_EVENT);

                    _this.fireEvent (wev);

                    return;

                }

                String pid = _this.prevStepIdProvider.apply (_this.currentStepId);

                String oldcurr = _this.currentStepId;

                _this.currentStepId = nid;
                _this.update ();

                WizardEvent wev = new WizardEvent (_this,
                                       _this.currentStepId,
                                       oldcurr,
                                       pid,
                                       WizardEvent.STEP_SHOW_EVENT);

                _this.fireEvent (wev);

            })
            .build ();

        this.prevBut = QuollButton.builder ()
            .buttonType (ButtonBar.ButtonData.BACK_PREVIOUS)
            .onAction (ev ->
            {

                String pid = _this.prevStepIdProvider.apply (_this.currentStepId);

                if (_this.prevStepCheck != null)
                {

                    if (!_this.prevStepCheck.test (_this.currentStepId,
                                                   pid))
                    {

                        return;

                    }

                }

                if (pid == null)
                {

                    return;

                }

                String nid = _this.nextStepIdProvider.apply (_this.currentStepId);

                String oldcurr = _this.currentStepId;

                _this.currentStepId = pid;
                _this.update ();

                WizardEvent wev = new WizardEvent (_this,
                                       _this.currentStepId,
                                       nid,
                                       oldcurr,
                                       WizardEvent.STEP_SHOW_EVENT);

                _this.fireEvent (wev);

            })
            .build ();

        this.cancelBut = QuollButton.builder ()
            .label (getUILanguageStringProperty (wizard,buttons,cancel))
            .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
            .onAction (ev ->
            {

                String pid = _this.prevStepIdProvider.apply (_this.currentStepId);
                String nid = _this.nextStepIdProvider.apply (_this.currentStepId);

                WizardEvent wev = new WizardEvent (_this,
                                                   _this.currentStepId,
                                                   nid,
                                                   pid,
                                                   WizardEvent.CANCEL_EVENT);

                _this.fireEvent (wev);

            })
            .build ();

        QuollButtonBar bb = QuollButtonBar.builder ()
            .button (this.nextBut)
            .button (this.prevBut)
            .button (this.cancelBut)
            .build ();

        this.getChildren ().addAll (this.title, this.stepWrapper, bb);

        this.showStep (b.startStepId);

    }

    public String getCurrentStepId ()
    {

        return this.currentStepId;

    }

    public boolean showStep (String stepId)
    {

        WizardEvent wev = new WizardEvent (this,
                                           this.currentStepId,
                                           stepId,
                                           null,
                                           WizardEvent.NEXT_STEP_SHOW_EVENT);

        this.fireEvent (wev);

        if (wev.isConsumed ())
        {

            // A handler/filter doesn't want the previous step shown.
            return false;

        }

        String oldid = this.currentStepId;

        this.currentStepId = stepId;
        this.update ();

        wev = new WizardEvent (this,
                               this.currentStepId,
                               oldid,
                               null,
                               WizardEvent.STEP_SHOW_EVENT);

        this.fireEvent (wev);

        return true;

    }

    private void update ()
    {

        Step currStep = this.stepProv.apply (this.currentStepId);

        if (currStep == null)
        {

            throw new IllegalStateException ("Unable to get step for id: " + this.currentStepId);

        }

        this.title.titleProperty ().unbind ();
        this.title.titleProperty ().bind (currStep.title);

        VBox.setVgrow (currStep.content, Priority.ALWAYS);
        this.stepWrapper.getChildren ().clear ();
        this.stepWrapper.getChildren ().add (currStep.content);
        VBox.setVgrow (this.stepWrapper, Priority.ALWAYS);

        this.updateButtons ();

    }

    public StringProperty getNextButtonLabel (String nid)
    {

        return (nid == null ? getUILanguageStringProperty (wizard,buttons,finish) : getUILanguageStringProperty (wizard,buttons,next));

    }

    public StringProperty getPreviousButtonLabel (String pid)
    {

        return getUILanguageStringProperty (wizard,buttons,previous);

    }

    private void updateButtons ()
    {

        String nid = this.nextStepIdProvider.apply (this.currentStepId);

        StringProperty np = null;

        if (this.nextButtonLabelProv == null)
        {

            np = (nid == null ? getUILanguageStringProperty (wizard,buttons,finish) : getUILanguageStringProperty (wizard,buttons,next));

        } else {

            np = this.nextButtonLabelProv.apply (nid);

        }

        this.nextBut.textProperty ().unbind ();
        this.nextBut.textProperty ().bind (np);

        String pid = this.prevStepIdProvider.apply (this.currentStepId);

        StringProperty pp = null;

        if (this.prevButtonLabelProv == null)
        {

            pp = getUILanguageStringProperty (wizard,buttons,previous);

        } else {

            pp = this.prevButtonLabelProv.apply (pid);

        }

        this.prevBut.textProperty ().unbind ();
        this.prevBut.textProperty ().bind (pp);

        this.prevBut.setDisable (pid == null);
        this.nextBut.setDisable (nid == null);

    }

    public void enableButtons ()
    {

        this.nextBut.setDisable (false);
        this.prevBut.setDisable (false);
        this.cancelBut.setDisable (false);

    }

    public void enableButton (String  name,
                              boolean enable)
    {

        if ((name.equals (NEXT_BUTTON_ID)) ||
            (name.equals (FINISH_BUTTON_ID)))
        {

            this.nextBut.setDisable (!enable);

        }

        if (name.equals (PREVIOUS_BUTTON_ID))
        {

            this.prevBut.setDisable (!enable);

        }

        if (name.equals (CANCEL_BUTTON_ID))
        {

            this.cancelBut.setDisable (!enable);

        }

    }

    /**
     * Get a builder to create a new wizard.
     *
     * @returns A new builder.
     */
    public static Wizard.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, Wizard>
    {

        private Function<String, Step> stepProv = null;
        private Function<String, String> nextStepIdProvider = null;
        private Function<String, String> prevStepIdProvider = null;
        private Function<String, StringProperty> nextButtonLabelProv = null;
        private Function<String, StringProperty> prevButtonLabelProv = null;
        private String styleName = null;
        private String startStepId = null;
        private EventHandler<WizardEvent> onCancel = null;
        private EventHandler<WizardEvent> onFinish = null;
        private EventHandler<WizardEvent> onNextStep = null;
        private EventHandler<WizardEvent> onPrevStep = null;
        private EventHandler<WizardEvent> onStepShow = null;
        private AbstractViewer viewer = null;
        private BiPredicate<String, String> nextStepCheck = null;
        private BiPredicate<String, String> prevStepCheck = null;

        private Builder ()
        {

        }

        @Override
        public Wizard build ()
        {

            return new Wizard (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder onCancel (EventHandler<WizardEvent> h)
        {

            this.onCancel = h;
            return this;

        }

        public Builder onFinish (EventHandler<WizardEvent> h)
        {

            this.onFinish = h;
            return this;

        }

        public Builder nextStepCheck (BiPredicate<String, String> ch)
        {

            this.nextStepCheck = ch;
            return this;

        }

        public Builder previousStepCheck (BiPredicate<String, String> ch)
        {

            this.prevStepCheck = ch;
            return this;

        }

        public Builder onNextStepShow (EventHandler<WizardEvent> h)
        {

            this.onNextStep = h;
            return this;

        }

        public Builder onPreviousStepShow (EventHandler<WizardEvent> h)
        {

            this.onPrevStep = h;
            return this;

        }

        public Builder onStepShow (EventHandler<WizardEvent> h)
        {

            this.onStepShow = h;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;

            return this;

        }

        public Builder startStepId (String id)
        {

            this.startStepId = id;
            return this;

        }

        /**
         * @param prov The first arg to the function is the next step id.
         */
        public Builder nextButtonLabelProvider (Function<String, StringProperty> prov)
        {

            this.nextButtonLabelProv = prov;
            return this;

        }

        /**
         * @param prov The first arg to the function is the previous step id.
         */
        public Builder previousButtonLabelProvider (Function<String, StringProperty> prov)
        {

            this.prevButtonLabelProv = prov;
            return this;

        }

        /**
         * @param prov The first arg to the function is the step id.
         */
        public Builder stepProvider (Function<String, Step> prov)
        {

            this.stepProv = prov;
            return this;

        }

        public Builder nextStepIdProvider (Function<String, String> prov)
        {

            this.nextStepIdProvider = prov;
            return this;

        }

        public Builder previousStepIdProvider (Function<String, String> prov)
        {

            this.prevStepIdProvider = prov;
            return this;

        }

        public Builder withViewer (AbstractViewer viewer)
        {

            this.viewer = viewer;
            return this;

        }

    }

    public static class Step
    {

        public String id = null;
        public StringProperty title = null;
        public Node           content = null;

        public Step ()
        {

        }

        public Step (String         stepId,
                     StringProperty title,
                     Node           content)
        {

            this.id = stepId;
            this.title = title;
            this.content = content;

        }

    }

    public static class WizardEvent extends Event
    {

        public static final EventType<WizardEvent> FINISH_EVENT = new EventType<> ("wizard.finish");
        public static final EventType<WizardEvent> CANCEL_EVENT = new EventType<> ("wizard.cancel");
        public static final EventType<WizardEvent> PREVIOUS_STEP_SHOW_EVENT = new EventType<> ("wizard.step.prevshow");
        public static final EventType<WizardEvent> NEXT_STEP_SHOW_EVENT = new EventType<> ("wizard.step.nextshow");
        public static final EventType<WizardEvent> STEP_SHOW_EVENT = new EventType<> ("wizard.step.show");

        private Wizard wizard = null;
        private String curr = null;
        private String next = null;
        private String prev = null;

        public WizardEvent (Wizard                 wiz,
                            String                 currentId,
                            String                 nextId,
                            String                 prevId,
                            EventType<WizardEvent> type)
        {

            super (type);

            this.wizard = wiz;
            this.curr = currentId;
            this.next = nextId;
            this.prev = prevId;

        }

        public String getCurrentStepId ()
        {

            return this.curr;

        }

        public String getPreviousStepId ()
        {

            return this.prev;

        }

        public String getNextStepId ()
        {

            return this.next;

        }

        public Wizard getWizard ()
        {

            return this.wizard;

        }

    }

}
