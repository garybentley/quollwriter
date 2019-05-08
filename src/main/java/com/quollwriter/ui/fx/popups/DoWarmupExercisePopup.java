package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.text.*;

import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class DoWarmupExercisePopup extends PopupContent
{

    public static final String POPUP_ID = "dowarmup";

    private ObjectProperty<Prompt> promptProp = null;
    private Prompt    prompt = null;
    private QuollTextArea ownprompt = null;
    private ChoiceBox mins = null;
    private ChoiceBox words = null;
    private CheckBox doOnStartup = null;
    private CheckBox doNotShow = null;

    public DoWarmupExercisePopup (AbstractViewer viewer)
    {

        super (viewer);

        final DoWarmupExercisePopup _this = this;

        Prompts.shuffle ();

        VBox b = new VBox ();

        VBox pb = new VBox ();
        pb.getStyleClass ().add (StyleClassNames.CHOOSEPROMPT);

        pb.getChildren ().add (Header.builder ()
            .title (getUILanguageStringProperty (dowarmup,chooseprompt,title))
            .build ());

        BasicHtmlTextFlow promptPreview = BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.TEXT)
            .build ();

        this.promptProp = new SimpleObjectProperty<Prompt> ();

        this.promptProp.addListener ((pr, oldv, newv) ->
        {

            promptPreview.textProperty ().unbind ();
            promptPreview.textProperty ().bind (UIUtils.formatPrompt (this.prompt));

        });

        this.prompt = Prompts.next ();
        this.promptProp.setValue (this.prompt);

        ScrollPane sp = new ScrollPane (promptPreview);

        pb.getChildren ().add (sp);

        QuollButtonBar bb = QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .styleClassName (StyleClassNames.WEBLINKS)
                        .buttonType (ButtonBar.ButtonData.OTHER)
                        .tooltip (getUILanguageStringProperty (dowarmup,weblinks,tooltip))
                        .onAction (ev ->
                        {

                            UIUtils.openURL (this.viewer,
                                             UserProperties.get (Constants.QUOLL_WRITER_WEBSITE_PROMPTS_LINK_PROPERTY_NAME));

                        })
                        .build ())
            .button (QuollButton.builder ()
                        .styleClassName (StyleClassNames.PREVIOUS)
                        .buttonType (ButtonBar.ButtonData.BACK_PREVIOUS)
                        .tooltip (getUILanguageStringProperty (dowarmup,previousprompt,tooltip))
                        .onAction (ev ->
                        {

                            if (this.doNotShow.isSelected ())
                            {

                                // Remove the prompt.
                                Prompts.excludeCurrent ();

                            }

                            Prompt p = Prompts.previous ();

                            if (p != null)
                            {

                                this.prompt = p;
                                this.promptProp.setValue (p);

                            } else
                            {

                                this.prompt = null;
                                this.promptProp.setValue (null);

                                ComponentUtils.showMessage (this.viewer,
                                                            getUILanguageStringProperty (dowarmup,allpromptsexcluded));
                                                     //"You have excluded all the prompts.");

                            }

                            this.doNotShow.setSelected (false);

                        })
                        .build ())
            .button (QuollButton.builder ()
                        .styleClassName (StyleClassNames.NEXT)
                        .buttonType (ButtonBar.ButtonData.NEXT_FORWARD)
                        .label (getUILanguageStringProperty (dowarmup,nextprompt,text))
                        .tooltip (getUILanguageStringProperty (dowarmup,nextprompt,tooltip))
                        .onAction (ev ->
                        {

                            if (this.doNotShow.isSelected ())
                            {

                                // Remove the prompt.
                                Prompts.excludeCurrent ();

                            }

                            Prompt p = Prompts.next ();

                            if (p != null)
                            {

                                this.prompt = p;
                                this.promptProp.setValue (p);

                            } else
                            {

                                this.prompt = null;
                                this.promptProp.setValue (null);

                                ComponentUtils.showMessage (this.viewer,
                                                            getUILanguageStringProperty (dowarmup,allpromptsexcluded));
                                                     //"You have excluded all the prompts.");

                            }

                            this.doNotShow.setSelected (false);

                        })
                        .build ())
            .build ();

        this.doNotShow = QuollCheckBox.builder ()
            .label (dowarmup,noshowpromptagain)
            .build ();
        ButtonBar.setButtonData (this.doNotShow, ButtonBar.ButtonData.OTHER);

        bb.getButtons ().add (this.doNotShow);

        bb.getButtons ().stream ()
            .forEach (but ->
            {

                ButtonBar.setButtonUniformSize (but, false);

            });

        pb.getChildren ().add (bb);

        VBox ob = new VBox ();
        ob.getStyleClass ().add (StyleClassNames.OWNPROMPT);

        ob.getChildren ().add (Header.builder ()
            .title (getUILanguageStringProperty (dowarmup,LanguageStrings.ownprompt,title))
            .build ());

        this.ownprompt = QuollTextArea.builder ()
            .placeholder (getUILanguageStringProperty (dowarmup,LanguageStrings.ownprompt,tooltip))
            .styleClassName (StyleClassNames.TEXT)
            .build ();

        ob.getChildren ().add (ownprompt);

        VBox cb = new VBox ();
        cb.getStyleClass ().add (StyleClassNames.CONFIG);

        cb.getChildren ().add (Header.builder ()
            .title (getUILanguageStringProperty (dowarmup,dofor,title))
            .build ());

        HBox hcb = new HBox ();

        this.mins = UIUtils.getTimeOptions (() ->
        {

            String minsDef = UserProperties.get (Constants.DEFAULT_WARMUP_MINS_PROPERTY_NAME);

            int minsC = Constants.DEFAULT_MINS;

            if (minsDef != null)
            {

                minsC = Integer.parseInt (minsDef);

            }

            return minsC;

        },
        // Called when the value changes.
        val ->
        {

            UserProperties.set (Constants.DEFAULT_WARMUP_MINS_PROPERTY_NAME,
                                String.valueOf (val));

        });

        this.words = UIUtils.getWordsOptions (() ->
        {

            String v = UserProperties.get (Constants.DEFAULT_WARMUP_WORDS_PROPERTY_NAME);

            try
            {

                return Integer.parseInt (v);

            } catch (Exception e)
            {

                try
                {

                    return Integer.parseInt (v.substring (0,
                                                          v.indexOf (" ") - 1));

                } catch (Exception ee)
                {

                    return Constants.DEFAULT_WORDS;

                }

            }

        },
        val ->
        {

            UserProperties.set (Constants.DEFAULT_WARMUP_WORDS_PROPERTY_NAME,
                                String.valueOf (val));

        });

        hcb.getChildren ().addAll (this.mins,
                                   QuollLabel.builder ()
                                        .label (dowarmup,andor)
                                        .build (),
                                   this.words,
                                   QuollLabel.builder ()
                                        .label (dowarmup,whicheverfirst)
                                        .build ());

        this.doOnStartup = QuollCheckBox.builder ()
            .label (dowarmup,dowarmuponstart)
            .selected (UserProperties.getAsBoolean (Constants.DO_WARMUP_ON_STARTUP_PROPERTY_NAME))
            .onAction (ev ->
            {

                UserProperties.set (Constants.DO_WARMUP_ON_STARTUP_PROPERTY_NAME,
                                    doOnStartup.isSelected ());

            })
            .build ();

        cb.getChildren ().addAll (hcb, this.doOnStartup);

        bb = QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.OK_DONE)
                        .label (getUILanguageStringProperty (dowarmup,startwriting))
                        .onAction (ev ->
                        {

                            this.addWarmupToProject ();
                            this.close ();

                        })
                        .build ())
            .button (QuollButton.builder ()
                        .label (getUILanguageStringProperty (buttons, cancel))
                        .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
                        .onAction (ev ->
                        {

                            this.close ();

                        })
                        .build ())
            .build ();

        b.getChildren ().addAll (pb, ob, cb, bb);

        this.getChildren ().add (b);

    }

    private void addWarmupToProject ()
    {

/*
        final int mins = WarmupPromptSelect.getMinsCount (this.mins);
        final int words = WarmupPromptSelect.getWordCount (this.words);

        if ((mins == 0)
            &&
            (words == 0)
           )
        {

            ComponentUtils.showErrorMessage (_this.viewer,
                                      getUIString (dowarmup,starterror));
                                      //"o_O  The timer can't be unlimited for both time and words.");

            return;

        }
*/
        final int mins = UserProperties.getAsInt (Constants.DEFAULT_WARMUP_MINS_PROPERTY_NAME);
        final int words = UserProperties.getAsInt (Constants.DEFAULT_WARMUP_WORDS_PROPERTY_NAME);

        if ((mins == 0)
            &&
            (words == 0)
           )
        {

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (prefix,starterror));
                                      //"o_O  The timer can't be unlimited for both time and words.");

            return;

        }

        Prompt prompt = this.prompt;

        final String ownPromptT = this.ownprompt.getText ().trim ();

        if (!ownPromptT.equals (""))
        {

            // Create a user prompt.
            try
            {

                prompt = Prompts.addUserPrompt (ownPromptT);

            } catch (Exception e)
            {

                Environment.logError ("Unable to create user prompt with text: " +
                                      ownPromptT,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (prefix,saveerror));
                                          //"Unable to save your prompt, please contact support for assistance.");

                return;

            }

        }

        Prompt _prompt = prompt;

        WarmupProjectViewer v = null;

        try
        {

            v = Environment.getWarmupProjectViewer ();

        } catch (Exception e) {

            Environment.logError ("Unable to add warm-up",
                                  e);

            ComponentUtils.showErrorMessage (null,
                                             getUILanguageStringProperty (dowarmup,addwarmuperror));

            return;

        }

        if (v == null)
        {

            return;

        }

        final Warmup w = new Warmup ();
        w.setPrompt (_prompt);
        w.setWords (words);
        w.setMins (mins);

        try
        {

            v.addNewWarmup (w);

            if (!prompt.isUserPrompt ())
            {

                v.fireProjectEvent (ProjectEvent.Type.projectobject,
                                    ProjectEvent.Action.createownprompt,
                                    w);

            }

        } catch (Exception e) {

            Environment.logError ("Unable to add warm-up" +
                                  w,
                                  e);

            ComponentUtils.showErrorMessage (null,
                                             getUILanguageStringProperty (dowarmup,addwarmuperror));
                                      //"Unable to add {warmup}.");

            return;

        }
/*
        // See if we already have the warmups project, if so then just open it.
        ProjectInfo p = null;

        try
        {

            p = Environment.getWarmupsProject ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to get warmups projects",
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (prefix,getwarmupsprojecterror));
                                      //"Unable to get {Warmups} {project}.");

            return;

        }

        if (p != null)
        {

            try
            {

                Environment.openProject (p,
                                         addToWarmupsProject);

            } catch (Exception e)
            {

                Environment.logError ("Unable to open warmups project",
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (prefix,openwarmupsprojecterror));
                                          //"Unable to open {Warmups} {project}.");

                return;

            }

        } else {

            // If we don't then output a message telling the user that it will be created
            // then create it.
            WarmupProjectViewer v = new WarmupProjectViewer ();

            Project pr = new Project (Constants.DEFAULT_WARMUPS_PROJECT_NAME);
            pr.setType (Project.WARMUPS_PROJECT_TYPE);

            try
            {

                v.init (null);

                // Put it in the user's directory.
                v.newProject (Environment.getUserQuollWriterDirPath (),
                              pr,
                              null);

            } catch (Exception e) {

                Environment.logError ("Unable to create warmups project",
                                      e);

                ComponentUtils.showErrorMessage (null,
                                                 getUILanguageStringProperty (prefix,createwarmupsproject,actionerror));
                                          //"Unable to create {Warmups} project please contact Quoll Writer support for assistance.");

                return;

            }

            UIUtils.runLater (addToWarmupsProject);

            ComponentUtils.showMessage (this.viewer,
                                        getUILanguageStringProperty (prefix,createwarmupsproject,title),
                                        //"{Warmups} project",
                                        getUILanguageStringProperty (prefix,createwarmupsproject,text));
                                        //"A {Warmups} {project} will now be created to hold your {warmups}.",

        }
*/
    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (Arrays.asList (dowarmup,LanguageStrings.popup,title))
            .styleClassName (StyleClassNames.DOWARMUP)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.requestFocus ();

        return p;

    }

}
