package com.quollwriter.ui.fx;

import java.nio.file.*;
import java.net.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.time.*;
import java.time.format.*;
import java.text.*;

import javafx.collections.*;
import javafx.beans.value.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.util.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.paint.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.input.*;

import org.apache.commons.io.file.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.achievements.*;
import com.quollwriter.uistrings.UILanguageStringsManager;
import com.quollwriter.uistrings.UILanguageStrings;
import com.quollwriter.uistrings.UILanguageStringsInfo;
import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class Options extends VBox implements Stateful
{

    private static final String PROJECTITEMPREVIEW_POPUP_ID = "projectitempreview";
    private static final String LAYOUT_POPUP_ID = "layoutselector";

    private AbstractViewer viewer = null;
    private IPropertyBinder propertyBinder = null;
    private VBox contentWrapper = null;
    private VBox sections = null;
    private Section currSect = null;
    private Map<Section.Id, Section> sects = new HashMap<> ();
    private Set<Node> headerControls = null;

    public void dispose ()
    {

        this.propertyBinder.dispose ();

    }

    public Options (AbstractViewer  viewer,
                    IPropertyBinder binder,
                    Set<Node>             headerControls,
                    Section.Id...   sects)
    {

        this.propertyBinder = binder;
        this.viewer = viewer;
        this.headerControls = headerControls;

        HBox sp = new HBox ();
        this.getChildren ().add (sp);

        VBox left = new VBox ();
        left.getStyleClass ().add ("left");

        Header h_ = Header.builder ()
            .title (getUILanguageStringProperty (LanguageStrings.options,title))
            .styleClassName (StyleClassNames.MAIN)
            .iconClassName (StyleClassNames.OPTIONS)
            //.controls (headerControls)
            .build ();

        left.getChildren ().add (h_);

        this.sections = new VBox ();
        this.sections.getStyleClass ().add (StyleClassNames.SECTIONS);
        left.getChildren ().add (new QScrollPane (this.sections));
        VBox.setVgrow (this.sections,
                       Priority.ALWAYS);

        this.contentWrapper = new VBox ();
        this.contentWrapper.getStyleClass ().add (StyleClassNames.ITEMS);

        sp.getChildren ().addAll (left, this.contentWrapper);
        HBox.setHgrow (this.contentWrapper,
                       Priority.ALWAYS);

        VBox.setVgrow (sp,
                       Priority.ALWAYS);

        for (Section.Id id : sects)
        {

            Header h = this.createSectionLabel (id,
                                                viewer);
            if (h == null)
            {

                continue;

            }

            h.setUserData (id);

            h.setOnMouseClicked (ev ->
            {

                this.showSection (id);

            });

            this.sections.getChildren ().add (h);

        }

        UIUtils.setFirstLastPseudoClasses (this.sections);

    }

    public void showSection (Section.Id id)
    {

        this.contentWrapper.getChildren ().clear ();

        Section s = this.sects.get (id);

        if (s == null)
        {

            s = this.createSection (id,
                                    this.viewer);

            if (s == null)
            {

                return;

            }

            this.sects.put (id,
                            s);

        }

        if (this.currSect != null)
        {

            this.currSect.getHeader ().getControls ().getItems ().clear ();

        }

        Node c = s.getContent ();
        Header ch = s.getHeader ();
        ch.managedProperty ().bind (ch.visibleProperty ());

        if (headerControls != null)
        {

            ch.getControls ().getItems ().addAll (headerControls);

        }

        VBox.setVgrow (c,
                       Priority.ALWAYS);
        ScrollPane csp = new QScrollPane (c);
        VBox.setVgrow (csp,
                       Priority.ALWAYS);
        this.contentWrapper.getChildren ().addAll (ch, csp);
        this.currSect = s;
        UIUtils.setSelected (sections,
                             id);

        UIUtils.setFirstLastPseudoClasses ((Parent) c,
                                           "." + StyleClassNames.SUBTITLE);

    }

    @Override
    public State getState ()
    {

        State s = new State ();

        s.set ("selected",
               this.currSect.getSectionId ().getType ());

        return s;

    }

    @Override
    public void init (State s)
    {

        if (s == null)
        {

            this.showSection ((Section.Id) this.sections.getChildren ().get (0).getUserData ());
            return;

        }

        String sel = s.getAsString ("selected");

        if (sel == null)
        {

            this.showSection ((Section.Id) this.sections.getChildren ().get (0).getUserData ());
            return;

        }

        Section.Id id = Section.Id.valueOf (sel);

        if (id != null)
        {

            this.showSection (id);

        }

    }

    private Header createSectionLabel (Section.Id     id,
                                       AbstractViewer viewer)
    {

        String idp = "";
        String scn = "";


        if (Section.Id.assets == id)
        {

            idp = assets;
            scn = StyleClassNames.ASSETS;

        }

        if (Section.Id.look == id)
        {

            idp = lookandsound;
            scn = StyleClassNames.LOOKS;

        }

/*
TODO Remove, rolled into the looks section.
        if (Section.Id.naming == id)
        {

            idp = naming;
            scn = StyleClassNames.NAMING;

        }
*/
        if (Section.Id.editing == id)
        {

            idp = editingchapters;
            scn = StyleClassNames.EDITING;

        }

        if (Section.Id.project == id)
        {

            scn = StyleClassNames.PROJECT;
            idp = projectandbackup;

        }

        if (Section.Id.start == id)
        {

            scn = StyleClassNames.START;
            idp = qwstart;

        }

/*
TODO Remove, rolled into start section
        if (Section.Id.warmups == id)
        {

            scn = Warmup.OBJECT_TYPE;
            idp = warmups;

        }
*/
        if (Section.Id.editors == id)
        {

            scn = StyleClassNames.CONTACTS;
            idp = editors;

        }

/*
 TODO Remove, rolled into edit section
        if (Section.Id.itemsAndRules == id)
        {

            scn = StyleClassNames.EDIT;
            idp = itemsandrules;

        }
*/
        if (Section.Id.achievements == id)
        {

            scn = StyleClassNames.ACHIEVEMENTS;
            idp = achievements;

        }

        if (Section.Id.problems == id)
        {

            scn = StyleClassNames.BUG;
            idp = errors;

        }

        if (idp.equals (""))
        {

            return null;

        }

        return Header.builder ()
            .styleClassName (scn)
            .title (options,idp,title)
            //.tooltip (options,idp,text)
            .build ();

    }

    private Section createSection (Section.Id     id,
                                   AbstractViewer viewer)
    {

        if (Section.Id.assets == id)
        {

            if (!(viewer instanceof AbstractProjectViewer))
            {

                throw new IllegalArgumentException ("Can only show assets when the AbstractProjectViewer is provided.");

            }

            return this.createAssetsSection ((AbstractProjectViewer) viewer);

        }

        if (Section.Id.look == id)
        {

            return this.createLookSection (viewer);

        }

        if (Section.Id.naming == id)
        {

            return this.createNamingSection (viewer);

        }

        if (Section.Id.editing == id)
        {

            if (!(viewer instanceof AbstractProjectViewer))
            {

                throw new IllegalArgumentException ("Can only show assets when the AbstractProjectViewer is provided.");

            }

            return this.createEditingSection ((AbstractProjectViewer) viewer);

        }

        if (Section.Id.project == id)
        {

            return this.createProjectSection (viewer);

        }

        if (Section.Id.start == id)
        {

            return this.createStartSection (viewer);

        }

        if (Section.Id.warmups == id)
        {

            return this.createWarmupsSection (viewer);

        }

        if (Section.Id.editors == id)
        {

            return this.createEditorsSection (viewer);

        }

        if (Section.Id.itemsAndRules == id)
        {

            return this.createItemsAndRulesSection (viewer);

        }

        if (Section.Id.achievements == id)
        {

            return this.createAchievementsSection (viewer);

        }

        if (Section.Id.problems == id)
        {

            return this.createProblemsSection (viewer);

        }

        return null;

    }

    private Node createLayoutSelector ()
    {

        final Options _this = this;

        //Label l = new Label ();
        //l.setGraphic (this.getLayoutPanel (UserProperties.uiLayoutProperty ().getValue ()));
        VBox l = new VBox ();
        l.getChildren ().add (this.getLayoutPanel (UserProperties.uiLayoutProperty ().getValue ()));
        l.maxWidthProperty ().bind (((Region) l.getChildren ().get (0)).widthProperty ());

        this.propertyBinder.addChangeListener (UserProperties.uiLayoutProperty (),
                                         (pr, oldv, newv) ->
                                         {

                                             l.getChildren ().clear ();
                                             l.getChildren ().add (this.getLayoutPanel (newv));

                                         });

        l.setOnMouseClicked (ev ->
        {

            if (ev.getButton () != MouseButton.PRIMARY)
            {

                return;

            }

            QuollPopup qp = this.viewer.getPopupById (LAYOUT_POPUP_ID);

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            VBox b = new VBox ();
            b.getStyleClass ().add (StyleClassNames.ITEMS);

            qp = QuollPopup.builder ()
                .title (options,lookandsound,labels,interfacelayout,popup,title)
                .styleClassName (StyleClassNames.LAYOUTSELECTOR)
                .headerIconClassName (StyleClassNames.EDIT)
                .popupId (LAYOUT_POPUP_ID)
                .content (b)
                .withClose (true)
                .withViewer (_this.viewer)
                .hideOnEscape (true)
                .removeOnClose (true)
                .show ()
                .build ();

            UIUtils.addStyleSheet (qp,
                                   Constants.POPUP_STYLESHEET_TYPE,
                                   StyleClassNames.LAYOUTSELECTOR);

            QuollPopup _qp = qp;

            List<String> layoutTypes = new ArrayList<> ();
            layoutTypes.add (Constants.LAYOUT_PS_CH);
            layoutTypes.add (Constants.LAYOUT_CH_PS);
            layoutTypes.add (Constants.LAYOUT_PS_CH_OS);
            layoutTypes.add (Constants.LAYOUT_OS_CH_PS);
            layoutTypes.add (Constants.LAYOUT_PS_OS_CH);
            layoutTypes.add (Constants.LAYOUT_CH_OS_PS);

            for (String lt : layoutTypes)
            {

                HBox hb = new HBox ();
                hb.getStyleClass ().add (StyleClassNames.ITEM);
                hb.getChildren ().add (this.getLayoutPanel (lt));

                hb.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, UserProperties.uiLayoutProperty ().getValue ().equals (lt));

                UIUtils.setTooltip (hb,
                                    getUILanguageStringProperty (options,lookandsound,labels,interfacelayout,popup,tooltip));

                //BasicHtmlTextFlow text = BasicHtmlTextFlow.builder ()
                QuollTextView text = QuollTextView.builder ()
                    .styleClassName (StyleClassNames.DESCRIPTION)
                    .text (getUILanguageStringProperty (options,lookandsound,interfacelayouts,lt))
                    .inViewer (_this.viewer)
                    .build ();
                HBox.setHgrow (text,
                               Priority.ALWAYS);

                hb.getChildren ().add (text);
                hb.setUserData (lt);

                hb.setOnMouseClicked (eev ->
                {

                    if (eev.getButton () != MouseButton.PRIMARY)
                    {

                        return;

                    }

                    UIUtils.setSelected (b,
                                         lt);
                    UserProperties.setUILayout (lt);

                    Environment.fireUserProjectEvent (_this,
                                                      ProjectEvent.Type.layout,
                                                      ProjectEvent.Action.changed);

                    _qp.close ();

                });

                b.getChildren ().add (hb);

            }

            UIUtils.runLater (() ->
            {

                _qp.toFront ();

            });

        });

        return l;

    }

    private Node getLayoutPanel (String type)
    {

        if (type.startsWith ("layout-"))
        {

            type = type.substring ("layout-".length ());

        }

        HBox content = new HBox ();
        content.getStyleClass ().add (StyleClassNames.LAYOUT);

        StringTokenizer t = new StringTokenizer (type,
                                                 "-");

        while (t.hasMoreTokens ())
        {

            String tok = t.nextToken ();

            if (tok.equals ("os"))
            {

                HBox p = new HBox ();
                p.getStyleClass ().add (StyleClassNames.OTHER);

                QuollLabel l = QuollLabel.builder ()
                    .label (options,lookandsound,labels,other)
                    .styleClassName (StyleClassNames.TEXT)
                    .build ();

                p.getChildren ().add (l);
                content.getChildren ().add (p);

            }

            if (tok.equals ("ps"))
            {

                HBox p = new HBox ();
                p.getStyleClass ().add (StyleClassNames.PROJECT);

                QuollLabel l = QuollLabel.builder ()
                    .label (objectnames,singular, Project.OBJECT_TYPE)
                    .styleClassName (StyleClassNames.TEXT)
                    .build ();

                p.getChildren ().add (l);
                content.getChildren ().add (p);

            }

            if (tok.equals ("ch"))
            {

                HBox p = new HBox ();
                p.getStyleClass ().add (StyleClassNames.TABS);
                HBox.setHgrow (p, Priority.ALWAYS);

                QuollLabel l = QuollLabel.builder ()
                    // TODO Change to "content"
                    .label (options,lookandsound,labels,tabs)
                    .styleClassName (StyleClassNames.TEXT)
                    .build ();

                p.getChildren ().add (l);
                content.getChildren ().add (p);

            }

        }

        return content;

    }

    private Section createWarmupsSection (AbstractViewer viewer)
    {

        HBox hcb = new HBox ();
        hcb.getStyleClass ().add (StyleClassNames.ITEM);
        hcb.getChildren ().addAll (DoWarmupExercisePopup.createWordsOptions (),
                                   QuollLabel.builder ()
                                        .label (options,warmups,labels,andor)
                                        .build (),
                                   DoWarmupExercisePopup.createTimeOptions (),
                                   QuollLabel.builder ()
                                        .label (options,warmups,labels,whicheverfirst)
                                        .build ());

        Section s = Section.builder ()
            .styleClassName (Warmup.OBJECT_TYPE)
            .title (options,warmups,title)
            .sectionId (Section.Id.warmups)
            .description (options,warmups,text)
            .mainItem (DoWarmupExercisePopup.createDoWarmupOnStartupCheckbox ())
            .subItem (getUILanguageStringProperty (dowarmup,dofor,title),
                      hcb)
            .build ();

        return s;

    }

    private Section createWebsiteSection (AbstractViewer viewer)
    {

        HBox b = new HBox ();

        b.getChildren ().add (QuollButton.builder ()
            .label (options,website,labels,createtranslation)
            .onAction (ev ->
            {

                UIUtils.showAddNewWebsiteLanguageStringsPopup (this.viewer);

            })
            .build ());

        b.getChildren ().add (QuollButton.builder ()
            .label (options,website,labels,edittranslation)
            .onAction (ev ->
            {

                UIUtils.showEditWebsiteLanguageStringsSelectorPopup (this.viewer);

            })
            .build ());

        Section s = Section.builder ()
            .styleClassName (StyleClassNames.WEBSITE)
            .title (options,website,title)
            .description (options,website,text)
            .sectionId (Section.Id.website)
            .mainItem (b)
            .build ();

        return s;

    }

    private Section createStartSection (AbstractViewer viewer)
    {

        final Options _this = this;

        QuollCheckBox showTips = QuollCheckBox.builder ()
            .label (options,qwstart,labels,showtips)
            .userProperty (Constants.SHOW_TIPS_PROPERTY_NAME)
            .build ();
        //("Show useful tips"));

        QuollCheckBox lastCB = QuollCheckBox.builder ()
            .label (options,qwstart,labels,showlastedited)
            .userProperty (Constants.OPEN_LAST_EDITED_PROJECT_PROPERTY_NAME)
            .build ();
        // ("Open the last edited {project}"));

        QuollCheckBox showCB = QuollCheckBox.builder ()
            .label (options,qwstart,labels,showprojectswindow)
            .userProperty (Constants.SHOW_LANDING_ON_START_PROPERY_NAME)
            .build ();

        lastCB.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            if (!newv)
            {

                showCB.setSelected (true);

            }

        });

        showCB.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            if (!newv)
            {

                lastCB.setSelected (true);

            }

        });

        // ("Show the {Projects} window"));
/*
        showTips.setSelected (UserProperties.getAsBoolean (Constants.SHOW_TIPS_PROPERTY_NAME));
		lastCB.setSelected (UserProperties.getAsBoolean (Constants.OPEN_LAST_EDITED_PROJECT_PROPERTY_NAME));
		showCB.setSelected (UserProperties.getAsBoolean (Constants.SHOW_LANDING_ON_START_PROPERY_NAME));

        showTips.addItemListener (new ItemAdapter ()
        {

            public void itemStateChanged (ItemEvent ev)
            {

                _this.updateUserProperty (Constants.SHOW_TIPS_PROPERTY_NAME,
                                          showTips.isSelected ());

            }

        });

        JComponent c = this.createWrapper (showTips);
        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        lastCB.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

				if (!lastCB.isSelected ())
				{

					showCB.setSelected (true);

                    UserProperties.set (Constants.SHOW_LANDING_ON_START_PROPERY_NAME,
										showCB.isSelected ());

				}

				UserProperties.set (Constants.OPEN_LAST_EDITED_PROJECT_PROPERTY_NAME,
									lastCB.isSelected ());

            }

        });

        c = this.createWrapper (lastCB);

        this.setAsMainItem (c);

        box.add (c);

        box.add (Box.createVerticalStrut (15));

        showCB.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

				if (!showCB.isSelected ())
				{

					lastCB.setSelected (true);

                    UserProperties.set (Constants.OPEN_LAST_EDITED_PROJECT_PROPERTY_NAME,
                                        lastCB.isSelected ());

				}

                UserProperties.set (Constants.SHOW_LANDING_ON_START_PROPERY_NAME,
                                    showCB.isSelected ());

            }

        });

        c = this.createWrapper (showCB);

        this.setAsMainItem (c);

        box.add (c);
*/

        HBox hcb = new HBox ();
        hcb.getStyleClass ().add (StyleClassNames.ITEM);
        hcb.getChildren ().addAll (DoWarmupExercisePopup.createWordsOptions (),
                                   QuollLabel.builder ()
                                        .label (options,warmups,labels,andor)
                                        .build (),
                                   DoWarmupExercisePopup.createTimeOptions (),
                                   QuollLabel.builder ()
                                        .label (options,warmups,labels,whicheverfirst)
                                        .build ());

        Section s = Section.builder ()
            .sectionId (Section.Id.start)
            .styleClassName (StyleClassNames.START)
            .title (options,qwstart,title)
            .description (options,qwstart,text)
            .mainItem (showTips)
            .mainItem (lastCB)
            .mainItem (showCB)
            .mainItem (DoWarmupExercisePopup.createDoWarmupOnStartupCheckbox ())
            .subItem (getUILanguageStringProperty (dowarmup,dofor,title),
                      hcb)
            .build ();

        return s;

    }

    private Section createEditingSection (AbstractProjectViewer viewer)
    {

        //final Properties props = Environment.getDefaultProperties (Project.OBJECT_TYPE);

        Map<Integer, StringProperty> timeMap = new LinkedHashMap<> ();

        timeMap.put (5,
                     getUILanguageStringProperty (times,mins5));
        timeMap.put (10,
                     getUILanguageStringProperty (times,mins10));
        timeMap.put (20,
                     getUILanguageStringProperty (times,mins20));
        timeMap.put (30,
                     getUILanguageStringProperty (times,mins30));
        timeMap.put (60,
                     getUILanguageStringProperty (times,hour1));

        final ComboBox<Integer> autosaveAmount = new ComboBox<> (FXCollections.observableList (new ArrayList (timeMap.keySet ())));

        int v = UserProperties.chapterAutoSaveTimeProperty ().getValue () / Constants.MIN_IN_MILLIS;

        autosaveAmount.getSelectionModel ().select (Integer.valueOf (UserProperties.chapterAutoSaveTimeProperty ().getValue () / Constants.MIN_IN_MILLIS));
        autosaveAmount.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            int selVal = autosaveAmount.getSelectionModel ().getSelectedItem ();

            UserProperties.setChapterAutoSaveTime (newv * Constants.MIN_IN_MILLIS);

        });

        Callback<ListView<Integer>, ListCell<Integer>> cellFactory = (lv ->
        {

            return new ListCell<Integer> ()
            {

                @Override
                protected void updateItem (Integer item,
                                           boolean empty)
                {

                    super.updateItem (item,
                                      empty);

                    if (empty || item == null)
                    {

                        this.textProperty ().unbind ();
                        setText ("");

                    } else {

                        this.textProperty ().bind (timeMap.get (item));

                    }

                }

            };

        });

        autosaveAmount.setCellFactory (cellFactory);
        autosaveAmount.setButtonCell (cellFactory.call (null));

        // TODO Change to use user properties.

        final QuollCheckBox enableAutosave = QuollCheckBox.builder ()
            .label (options,editingchapters,labels,autosave)
            .build ();
        boolean autosaveEnabled = UserProperties.chapterAutoSaveEnabledProperty ().getValue ();
        enableAutosave.setSelected (autosaveEnabled);

        enableAutosave.selectedProperty ().addListener ((pv, oldv, newv) ->
        {

            UserProperties.setChapterAutoSaveEnabled (enableAutosave.isSelected ());
            autosaveAmount.setDisable (!enableAutosave.isSelected ());
            Environment.fireUserProjectEvent (this.viewer,
                                              ProjectEvent.Type.autosave,
                                              (enableAutosave.isSelected () ? ProjectEvent.Action.on : ProjectEvent.Action.off));

        });

        autosaveAmount.setDisable (!autosaveEnabled);

        ComboBox<String> spellcheckLang = new ComboBox<> ();
        spellcheckLang.setDisable (true);

        final QuollCheckBox defLang = QuollCheckBox.builder ()
            .label (options,editingchapters,labels,setasdefaultlanguage)
            .build ();
        //"Set as default language"));

        defLang.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            if (defLang.isSelected ())
            {

                UserProperties.setDefaultSpellCheckLanguage (spellcheckLang.valueProperty ().getValue ());

            }

        });

        Consumer<String> downloadDictFiles = lang ->
        {

            DownloadPanel langDownload = DownloadPanel.builder ()
                .title (getUILanguageStringProperty (Arrays.asList (dictionary,download,notification),
                                                     getUILanguageStringProperty (languagenames,lang)))
                .styleClassName (StyleClassNames.DOWNLOAD)
                .showStop (true)
                .build ();
            langDownload.managedProperty ().bind (langDownload.visibleProperty ());

            Set<Node> controls = new LinkedHashSet<> ();
            controls.add (langDownload.getStopButton ());

            Notification n = this.viewer.addNotification (langDownload,
                                                          StyleClassNames.DOWNLOAD,
                                                          -1,
                                                          controls);

            UrlDownloader dl = UIUtils.downloadDictionaryFiles (lang,
                                             this.viewer,
                                             // On progress
                                             p ->
                                             {

                                                 langDownload.setProgress (p);

                                             },
                                             // On complete
                                             () ->
                                             {

                                                 this.viewer.removeNotification (n);

                                                 if (this.viewer instanceof AbstractProjectViewer)
                                                 {

                                                     AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

                                                     try
                                                     {

                                                         pv.setProjectSpellCheckLanguage (lang);

                                                     } catch (Exception e) {

                                                         Environment.logError ("Unable to set project spell check language to: " + lang,
                                                                               e);

                                                         ComponentUtils.showErrorMessage (this.viewer,
                                                                                          getUILanguageStringProperty (Arrays.asList (spellchecker,unabletosetlanguage),
                                                                                                                       getUILanguageStringProperty (languagenames,lang)));

                                                         return;

                                                     }

                                                     pv.fireProjectEventLater (ProjectEvent.Type.spellcheck,
                                                                               ProjectEvent.Action.changelanguage);

                                                     this.viewer.addNotification (getUILanguageStringProperty (Arrays.asList (options,editingchapters,downloaddictionaryfiles,notification,text),
                                                                                 //"The language files for <b>%s</b> have been downloaded and the project language set.",
                                                                                                               lang),
                                                                                  StyleClassNames.INFORMATION,
                                                                                  30);

                                                 } else {

                                                     // Add a notification that the files have been downloaded.
                                                     this.viewer.addNotification (getUILanguageStringProperty (Arrays.asList (options,editingchapters,downloaddictionaryfiles,notification,text),
                                                                                 //"The language files for <b>%s</b> have been downloaded and the project language set.",
                                                                                                               lang),
                                                                                  StyleClassNames.INFORMATION,
                                                                                  -1);

                                                 }

                                             },
                                             // On error
                                             ex ->
                                             {

                                                 this.viewer.removeNotification (n);

                                                 ComponentUtils.showErrorMessage (this.viewer,
                                                                                  getUILanguageStringProperty (Arrays.asList (dictionary,download,actionerror),
                                                                                                               getUILanguageStringProperty (languagenames,spellcheckLang.valueProperty ().getValue ())));

                                             });

        };

        Node downloadFiles = QuollHyperlink.builder ()
            .label (options,editingchapters,labels,downloadlanguagefiles)
            .styleClassName (StyleClassNames.DOWNLOAD)
            .onAction (ev ->
            {

                String lang = spellcheckLang.valueProperty ().getValue ();

                downloadDictFiles.accept (lang);

            })
            .build ();
        downloadFiles.managedProperty ().bind (downloadFiles.visibleProperty ());

        if (this.viewer instanceof AbstractProjectViewer)
        {

            AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

            try
            {

                downloadFiles.setVisible (!DictionaryProvider.isLanguageInstalled (pv.getProjectSpellCheckLanguage ()));

            } catch (Exception e) {

                // Ignore.

            }

        } else {

            downloadFiles.setVisible (!DictionaryProvider.isLanguageInstalled (UserProperties.getDefaultSpellCheckLanguage ()));

        }

        spellcheckLang.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            final String lang = newv;

            String def = UserProperties.getDefaultSpellCheckLanguage ();

            if (def == null)
            {

                def = Constants.ENGLISH;

            }

            final String currLang = def;

            if (UILanguageStrings.isEnglish (def))
            {

                def = Constants.ENGLISH;

            }

            //defLang.setSelected (def.equals (lang));

            if (this.viewer instanceof AbstractProjectViewer)
            {

                AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

                def = pv.getProjectSpellCheckLanguage ();

            }

            if (!def.equals (lang))
            {

                defLang.setSelected (false);

            } else {

                defLang.setSelected (true);

            }

            if ((!UILanguageStrings.isEnglish (lang))
                &&
                (!def.equals (lang))
               )
            {

                QuollPopup.messageBuilder ()
                    .message (options,editingchapters,labels,nonenglishwarning)
                    .withViewer (this.viewer)
                    .closeButton ()
                    .build ();

            }

            downloadFiles.setVisible (false);

            // Check to see if the files are available.
            try
            {

                if (!DictionaryProvider.isLanguageInstalled (lang))
                {

                    downloadFiles.setVisible (true);

                    List<String> prefix = Arrays.asList (options,editingchapters,downloaddictionaryfiles,popup);

                    QuollPopup.questionBuilder ()
                        .withViewer (this.viewer)
                        .styleClassName (StyleClassNames.DOWNLOAD)
                        .title (getUILanguageStringProperty (Utils.newList (prefix,title)))
                        .message (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                               lang))
                        .confirmButtonLabel (getUILanguageStringProperty (Utils.newList (prefix,buttons,confirm)))
                        .cancelButtonLabel (getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)))
                        .onConfirm (ev ->
                        {

                            downloadDictFiles.accept (lang);

                        })
                        .onCancel (ev ->
                        {

                            spellcheckLang.getSelectionModel ().select (currLang);

                        })
                        .build ();

                    return;

                } else {

                    if (this.viewer instanceof AbstractProjectViewer)
                    {

                        AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

                        try
                        {

                            pv.setProjectSpellCheckLanguage (lang);

                        } catch (Exception e) {

                            Environment.logError ("Unable to set spell check language to: " + lang,
                                                  e);

                            ComponentUtils.showErrorMessage (this.viewer,
                                                             getUILanguageStringProperty (Arrays.asList (spellchecker,unabletosetlanguage),
                                                                                          getUILanguageStringProperty (languagenames,lang)));

                        }

                        pv.fireProjectEventLater (ProjectEvent.Type.spellcheck,
                                                  ProjectEvent.Action.changelanguage);

                    }

                }

            } catch (Exception e) {

                Environment.logError ("Unable to get language files for: " +
                                      lang,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (options,editingchapters,downloaddictionaryfiles,actionerror));
                                          //"Unable to check for dictionary files, please contact Quoll Writer support.");

                return;

            }

        });

        Callback<ListView<String>, ListCell<String>> langCellFactory = (lv ->
        {

            return new ListCell<String> ()
            {

                @Override
                protected void updateItem (String  item,
                                           boolean empty)
                {

                    super.updateItem (item,
                                      empty);

                    if (empty || item == null)
                    {

                        this.textProperty ().unbind ();
                        setText ("");

                    } else {

                        StringProperty textProp = getUILanguageStringProperty (Arrays.asList (languagenames,item));

                        this.textProperty ().bind (textProp);

                    }

                }

            };

        });

        spellcheckLang.setCellFactory (langCellFactory);
        spellcheckLang.setButtonCell (langCellFactory.call (null));

        // Get the languages supported by the spellchecker.
        Environment.schedule (() ->
        {

            String l = null;

            try
            {

                l = Utils.getUrlFileAsString (new URL (Environment.getQuollWriterWebsite () + "/" + UserProperties.get (Constants.QUOLL_WRITER_SUPPORTED_LANGUAGES_URL_PROPERTY_NAME)));

            } catch (Exception e) {

                // Something gone wrong, so just add english.
                l = Constants.ENGLISH;

                Environment.logError ("Unable to get language files url",
                                      e);

            }

            StringTokenizer t = new StringTokenizer (l,
                                                     String.valueOf ('\n'));

            final List<String> langs = new ArrayList<> ();

            while (t.hasMoreTokens ())
            {

                String lang = t.nextToken ().trim ();

                if (lang.equals (""))
                {

                    continue;

                }

                langs.add (lang);

            }

            UIUtils.runLater (() ->
            {

                spellcheckLang.getItems ().addAll (langs);

                String def = UserProperties.get (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME);

                if (this.viewer instanceof AbstractProjectViewer)
                {

                    AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

                    spellcheckLang.getSelectionModel ().select (pv.getProjectSpellCheckLanguage ());

                } else {

                    spellcheckLang.getSelectionModel ().select (def);

                }

                spellcheckLang.setDisable (false);

                boolean isDef = def.equals (spellcheckLang.valueProperty ().getValue ());

                //defLang.setSelected (isDef);

            });

        },
        1,
        -1);

        Region editPosColorSwatch = new Region ();
        editPosColorSwatch.setBackground (new Background (new BackgroundFill (UserProperties.getEditMarkerColor (), null, null)));
        editPosColorSwatch.getStyleClass ().add (StyleClassNames.EDITPOSITIONCOLORSWATCH);
        editPosColorSwatch.getStyleClass ().add (StyleClassNames.COLORSWATCH);

        editPosColorSwatch.setOnMouseClicked (ev ->
        {

            if (ev.getButton () != MouseButton.PRIMARY)
            {

                return;

            }

            String pid = "edit-position-color-chooser";

            QuollPopup qp = viewer.getPopupById (pid);

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            ColorChooserPopup p = new ColorChooserPopup (viewer,
                                                         UserProperties.getEditMarkerColor (),
                                                         true);
            p.getPopup ().setPopupId (pid);
            p.getChooser ().setOnColorSelected (eev ->
            {

                UserProperties.setEditMarkerColor (p.getChooser ().colorProperty ().getValue ());
                editPosColorSwatch.setBackground (new Background (new BackgroundFill (UserProperties.getEditMarkerColor (), null, null)));
                p.close ();

            });

            p.show ();


        });

        QuollCheckBox compressChapterContextMenu = QuollCheckBox.builder ()
            .label (options,editingchapters,labels,compressrightclickmenu)
            .userProperty (Constants.COMPRESS_CHAPTER_CONTEXT_MENU_PROPERTY_NAME)
            .build ();

        boolean soundSel = UserProperties.getAsBoolean (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME);

        QuollButton playSoundB = QuollButton.builder ()
            .label (options,editingchapters,labels,playtypewritersound,buttons,playsound)
            .onAction (ev ->
            {

                UserProperties.playKeyStrokeSound ();

            })
            .build ();

        QuollCheckBox playSoundCB = QuollCheckBox.builder ()
            .selected (soundSel)
            .label (options,editingchapters,labels,playtypewritersound,text)
            .build ();

        playSoundCB.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            UserProperties.setPlaySoundOnKeyStroke (playSoundCB.isSelected ());

        });

        Path kp = null;

        String kf = UserProperties.get (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME);

        if (kf != null)
        {

            kp = Paths.get (kf);

        }

        QuollFileField ownSoundF = QuollFileField.builder ()
            .showClear (true)
            .limitTo (QuollFileField.Type.file)
            .styleClassName (StyleClassNames.OWNSOUND)
            .withViewer (this.viewer)
            .fileExtensionFilter (getUILanguageStringProperty (options,editingchapters,labels,playtypewritersound,finder,filter),
                                  "wav")
            .chooserTitle (getUILanguageStringProperty (options,editingchapters,labels,playtypewritersound,finder,title))
            .findButtonTooltip (getUILanguageStringProperty (options,editingchapters,labels,playtypewritersound,finder,tooltip))
            .initialFile (kp)
            .build ();

        ownSoundF.fileProperty ().addListener ((pr, oldv, newv) ->
        {

            UserProperties.setKeyStrokeSoundFilePath (newv);

        });

        playSoundB.setDisable (!soundSel);
        ownSoundF.setDisable (!soundSel);

        playSoundCB.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            playSoundB.setDisable (!playSoundCB.isSelected ());
            ownSoundF.setDisable (!playSoundCB.isSelected ());

        });

        QuollCheckBox showPreviewCB = QuollCheckBox.builder ()
            .label (options,editingchapters,labels,showpreview)
            .userProperty (Constants.SHOW_QUICK_OBJECT_PREVIEW_IN_PROJECT_SIDEBAR_PROPERTY_NAME)
            .build ();

        QuollButton changeDisplayBut = QuollButton.builder ()
            .label (options,editingchapters,labels,changedisplay)
            .onAction (ev ->
            {

                QuollPopup qp = this.viewer.getPopupById (ChangeProjectItemPreviewDisplayPopup.POPUP_ID);

                if (qp != null)
                {

                    qp.toFront ();
                    return;

                }

                new ChangeProjectItemPreviewDisplayPopup (viewer).show ();

            })
            .build ();

        changeDisplayBut.setDisable (!showPreviewCB.isSelected ());

        showPreviewCB.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            changeDisplayBut.setDisable (!newv);

        });

        Section.Builder s = Section.builder ()
            .styleClassName (StyleClassNames.EDITING)
            .sectionId (Section.Id.editing)
            .title (options,editingchapters,title)
            .description (options,editingchapters,text)
            .subtitle (getUILanguageStringProperty (options,editingchapters,labels,subtitles,autosave))
            .mainItem (enableAutosave)
            .subItem (getUILanguageStringProperty (options,editingchapters,labels,autosavewhen),
                      autosaveAmount)
            .subtitle (getUILanguageStringProperty (options,editingchapters,labels,subtitles,editposition))
            .mainItem (getUILanguageStringProperty (options,editingchapters,labels,showicon),
                       QuollCheckBox.builder ()
                            .label (options,editingchapters,labels,haseditposition)
                            .userProperty (Constants.SHOW_EDIT_POSITION_ICON_IN_CHAPTER_LIST_PROPERTY_NAME)
                            .build (),
                       QuollCheckBox.builder ()
                            .label (options,editingchapters,labels,editcomplete)
                            .userProperty (Constants.SHOW_EDIT_COMPLETE_ICON_IN_CHAPTER_LIST_PROPERTY_NAME)
                            .build (),
                       QuollHyperlink.builder ()
                            .label (actions,viewexample)
                            .styleClassName (StyleClassNames.VIEW)
                           .onAction (ev ->
                           {

                               String pid = "chapter-icons-example-popup";

                               QuollPopup qp = this.viewer.getPopupById (pid);

                               if (qp != null)
                               {

                                   qp.toFront ();
                                   return;

                               }

                               QuollPopup.messageBuilder ()
                                    .title (names,example)
                                    .styleClassName (StyleClassNames.CHAPTERICONSEXAMPLE)
                                    .message (new ImageView ())
                                    .withViewer (this.viewer)
                                    .popupId (pid)
                                    .build ();

                           })
                           .build ())
            .mainItem (QuollCheckBox.builder ()
                .label (options,editingchapters,labels,showeditposition)
                .userProperty (Constants.SHOW_EDIT_MARKER_IN_CHAPTER_PROPERTY_NAME)
                .build ())
            .subItem (getUILanguageStringProperty (options,editingchapters,labels,seteditpositioncolor,text),
                      editPosColorSwatch,
                      QuollHyperlink.builder ()
                          .label (actions,viewexample)
                          .styleClassName (StyleClassNames.VIEW)
                          .onAction (ev ->
                          {

                              String pid = "edit-position-example-popup";

                              QuollPopup qp = this.viewer.getPopupById (pid);

                              if (qp != null)
                              {

                                  qp.toFront ();
                                  return;

                              }

                              QuollPopup.messageBuilder ()
                                .title (names,example)
                                .styleClassName (StyleClassNames.EDITPOSITIONEXAMPLE)
                                .message (new ImageView ())
                                .withViewer (this.viewer)
                                .popupId (pid)
                                .build ();

                          })
                          .build ())
            .mainItem (QuollCheckBox.builder ()
                .label (options,editingchapters,labels,seteditcompleteatchapterend)
                .userProperty (Constants.SET_CHAPTER_AS_EDIT_COMPLETE_WHEN_EDIT_POSITION_IS_AT_END_OF_CHAPTER_PROPERTY_NAME)
                .build ())
            .subtitle (getUILanguageStringProperty (options,editingchapters,labels,subtitles,sidebar))
            .mainItem (showPreviewCB)
            .subItem (changeDisplayBut)
            .mainItem (QuollCheckBox.builder ()
                .label (options,editingchapters,labels,shownotes)
                .userProperty (Constants.SHOW_NOTES_IN_CHAPTER_LIST_PROPERTY_NAME)
                .build ())
            .mainItem (compressChapterContextMenu)
            .subItem (QuollHyperlink.builder ()
                .label (actions,viewexample)
                .styleClassName (StyleClassNames.VIEW)
                .onAction (ev ->
                {

                    String pid = "compress-chapter-menu-example-popup-" + compressChapterContextMenu.isSelected ();

                    QuollPopup qp = this.viewer.getPopupById (pid);

                    if (qp != null)
                    {

                        qp.toFront ();
                        return;

                    }

                    ImageView iv = new ImageView ();
                    iv.pseudoClassStateChanged (StyleClassNames.COMPRESSED_PSEUDO_CLASS, compressChapterContextMenu.isSelected ());

                    QuollPopup.messageBuilder ()
                        .styleClassName (StyleClassNames.COMPRESSCHAPTERCONTEXTEXAMPLE)
                        .message (iv)
                        .title (names,example)
                        .withViewer (this.viewer)
                        .popupId (pid)
                        .build ();

                })
                .build ());

        boolean isAPV = (viewer instanceof ProjectViewer);

        if (isAPV)
        {

            s.subtitle (getUILanguageStringProperty (options,editingchapters,labels,subtitles,problemfinder))
                .mainItem (QuollButton.builder ()
                    .label (options,editingchapters,labels,problemfinderrules)
                    .onAction (ev ->
                    {

                        ((ProjectViewer) viewer).showProblemFinderRuleConfig ();

                    })
                    .build ());

        }

        s.subtitle (getUILanguageStringProperty (options,editingchapters,labels,subtitles,notetypes))
            .mainItem (QuollButton.builder ()
                .label (options,editingchapters,labels,editnotetypes)
                .onAction (ev ->
                {

                    viewer.showManageNoteTypes ();

                })
                .build ());

        s.subtitle (getUILanguageStringProperty (options,editingchapters,labels,subtitles,spellcheck))
            .mainItem (getUILanguageStringProperty (options,editingchapters,labels,setspellcheckerlanguage),
                       spellcheckLang)
            .subItem (defLang)
            .mainItem (QuollButton.builder ()
                .label (getUILanguageStringProperty (options,editingchapters,labels,managedictionary))
                .onAction (ev ->
                {

                    this.viewer.showDictionaryManager ();

                })
                .build ())
            // TODO Is this still needed? .subItem (downloadFiles)
            .subtitle (getUILanguageStringProperty (options,editingchapters,labels,subtitles,keypress))
            .mainItem (playSoundCB)
            .subItem (getUILanguageStringProperty (options,editingchapters,labels,playtypewritersound,selectownwavfile),
                      ownSoundF)
            .subItem (playSoundB);

        return s.build ();

    }

    private Section createNamingSection (AbstractViewer viewer)
    {

        Section s = Section.builder ()
            .sectionId (Section.Id.naming)
            .styleClassName (StyleClassNames.NAMING)
            .title (options,naming,title)
            .description (options,naming,text)
            .mainItem (QuollButton.builder ()
                        .label (options,naming,labels,changenames)
                        .onAction (ev ->
                        {

                            QuollPopup qp = this.viewer.getPopupById (ObjectTypeNameChangePopup.POPUP_ID);

                            if (qp != null)
                            {

                                qp.toFront ();
                                return;

                            }

                            new ObjectTypeNameChangePopup (this.viewer).show ();

                        })
                        .build ())
            .build ();

        return s;

    }

    private Section createProjectSection (AbstractViewer viewer)
    {

        if (!(this.viewer instanceof AbstractProjectViewer))
        {

            return null;

        }

        AbstractProjectViewer pv = (AbstractProjectViewer) viewer;

        final Options _this = this;

        final Project proj = pv.getProject ();

        VBox b = new VBox ();

        ErrorBox projDirErr = ErrorBox.builder ()
            .build ();

        QuollFileField projDirF = QuollFileField.builder ()
            .chooserTitle (getUILanguageStringProperty (options,projectandbackup,labels,selectprojectdir,finder,title))
            .limitTo (QuollFileField.Type.directory)
            .initialFile (proj.getProjectDirectory ().getParentFile ().toPath ())
            .withViewer (viewer)
            .build ();

        b.getChildren ().addAll (projDirErr, projDirF);

        QuollButton projDirChangeB = QuollButton.builder ()
            .label (options,projectandbackup,labels,selectprojectdir,finder,label)
            .build ();
        projDirChangeB.setOnAction (ev ->
        {

            this.handleProjectDirChange (projDirF.getFile (),
                                         pv,
                                         err ->
            {

                if (err != null)
                {

                    projDirErr.setErrors (err);
                    projDirErr.setVisible (true);

                }

                // Reset the project dir, something went wrong.
                projDirF.setFile (proj.getProjectDirectory ().getParentFile ().toPath ());

            });

            projDirChangeB.setDisable (true);

        });
        projDirChangeB.setDisable (true);

        projDirF.fileProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv == null)
            {

                return;

            }

            projDirChangeB.setDisable (newv.equals (proj.getProjectDirectory ().getParentFile ().toPath ()));

        });

        Set<StringProperty> backupsA = new LinkedHashSet<> ();
        backupsA.add (getUILanguageStringProperty (times,hours12)); //Constants.HOURS_12);
        backupsA.add (getUILanguageStringProperty (times,hours24)); //Constants.HOURS_24);
        backupsA.add (getUILanguageStringProperty (times,days2)); //Constants.DAYS_2);
        backupsA.add (getUILanguageStringProperty (times,days5)); //Constants.DAYS_5);
        backupsA.add (getUILanguageStringProperty (times,week1)); //Constants.WEEK_1);

        long backupsTime = proj.getAutoBackupsTime ();
        //Utils.getTimeAsMillis (proj.getProperty (Constants.AUTO_SNAPSHOTS_TIME_PROPERTY_NAME));

        int btInd = 0; // 12 hours

        if (backupsTime == (24 * Constants.HOUR_IN_MILLIS))
        {

            btInd = 1;

        }

        if (backupsTime == (2 * Constants.DAY_IN_MILLIS))
        {

            btInd = 2;

        }

        if (backupsTime == (5 * Constants.DAY_IN_MILLIS))
        {

            btInd = 3;

        }

        if (backupsTime == (7 * Constants.DAY_IN_MILLIS))
        {

            btInd = 4;

        }

        QuollChoiceBox backupTimeB = QuollChoiceBox.builder ()
            .items (backupsA)
            .onSelected (ev ->
            {

                QuollChoiceBox cb = (QuollChoiceBox) ev.getSource ();

                int selInd = cb.getSelectionModel ().getSelectedIndex ();

                long time = 0;

                if (selInd == 0)
                {

                    time = 12 * Constants.HOUR_IN_MILLIS;

                }

                if (selInd == 1)
                {

                    time = 24 * Constants.HOUR_IN_MILLIS;

                }

                if (selInd == 2)
                {

                    time = 2 * Constants.DAY_IN_MILLIS;

                }

                if (selInd == 3)
                {

                    time = 5 * Constants.DAY_IN_MILLIS;

                }

                if (selInd == 4)
                {

                    time = 7 * Constants.DAY_IN_MILLIS;

                }

                proj.setAutoBackupsTime (time);
                UserProperties.setAutoBackupsTime (time);

            })
            .selectedIndex (btInd)
            .build ();

        QuollCheckBox autoBackupEnb = QuollCheckBox.builder ()
            .label (options,projectandbackup,labels,autobackup)
            .selected (proj.isAutoBackupsEnabled ())
            .build ();

        autoBackupEnb.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            backupTimeB.setDisable (!newv);
            proj.setAutoBackupsEnabled (newv);
            UserProperties.setAutoBackupsEnabled (newv);

        });

        backupTimeB.setDisable (!autoBackupEnb.isSelected ());

        Set<StringProperty> countA = new LinkedHashSet<> ();
        countA.add (new SimpleStringProperty (Environment.formatNumber (10)));
        countA.add (new SimpleStringProperty (Environment.formatNumber (20)));
        countA.add (new SimpleStringProperty (Environment.formatNumber (50)));
        countA.add (getUILanguageStringProperty (options,projectandbackup,labels,all));

        int count = proj.getBackupsToKeepCount ();

        int selInd = 3;

        if (count == 10)
        {

            selInd = 0;

        }

        if (count == 20)
        {

            selInd = 1;

        }

        if (count == 50)
        {

            selInd = 2;

        }

        QuollChoiceBox backupsCountB = QuollChoiceBox.builder ()
            .items (countA)
            .selectedIndex (selInd)
            .onSelected (ev ->
            {

                QuollChoiceBox cb = (QuollChoiceBox) ev.getSource ();

                int cselInd = cb.getSelectionModel ().getSelectedIndex ();

                int ccount = 10;

                if (cselInd == 1)
                {

                    ccount = 20;

                }

                if (cselInd == 2)
                {

                    ccount = 50;

                }

                if (cselInd == 3)
                {

                    ccount = -1;

                }

                proj.setBackupsToKeepCount (ccount);
                UserProperties.setBackupsToKeepCount (ccount);

                if (ccount > -1)
                {

                    int _ccount = ccount;

                    Runnable prune = () ->
                    {

                        try
                        {

                            pv.getObjectManager ().pruneBackups (pv.getProject (),
                                                                 _ccount);

                            QuollPopup.messageBuilder ()
                                .title (options,projectandbackup,prunebackups,confirmpopup,title)
                                .message (options,projectandbackup,prunebackups,confirmpopup,text)
                                .closeButton ()
                                .styleClassName (StyleClassNames.BACKUPS)
                                .withViewer (viewer)
                                .build ();

                        } catch (Exception e) {

                            Environment.logError ("Unable to prune backups for project: " +
                                                  proj,
                                                  e);

                            ComponentUtils.showErrorMessage (viewer,
                                                             getUILanguageStringProperty (options,projectandbackup,prunebackups,actionerror));

                        }

                    };

                    int pc = -1;

                    try
                    {

                        pc = pv.getObjectManager ().getBackupFilesCount (pv.getProject ());

                    } catch (Exception e) {

                        Environment.logError ("Unable to prune backups for project: " +
                                              proj,
                                              e);

                        ComponentUtils.showErrorMessage (viewer,
                                                         getUILanguageStringProperty (options,projectandbackup,prunebackups,actionerror));
                        return;

                    }

                    if (pc > ccount)
                    {

                        QuollPopup.yesConfirmTextEntryBuilder ()
                            .withViewer (viewer)
                            .styleClassName (StyleClassNames.BACKUPS)
                            .title (options,projectandbackup,prunebackups,confirmpopup,title)
                            .description (getUILanguageStringProperty (Arrays.asList (options,projectandbackup,prunebackups,popup,text),
                                                                   Environment.formatNumber (pc - ccount)))
                            .confirmButtonLabel (options,projectandbackup,prunebackups,popup,buttons,confirm)
                            .cancelButtonLabel (options,projectandbackup,prunebackups,popup,buttons,cancel)
                            .onConfirm (eev ->
                            {

                                prune.run ();

                            })
                            .build ();

                    }

                }

            })
            .build ();

        VBox bb = new VBox ();

        ErrorBox projBackupDirErr = ErrorBox.builder ()
            .build ();

        Path backupDir = proj.getBackupDirectory ().toPath ();

        if (Files.notExists (backupDir))
        {

            try
            {

                Files.createDirectory (backupDir);

                Utils.createQuollWriterDirFile (backupDir);

            } catch (Exception e) {

                Environment.logError ("Unable to create backup dir: " + backupDir,
                                      e);

            }

        }

        QuollFileField projBackupDirF = QuollFileField.builder ()
            .chooserTitle (getUILanguageStringProperty (options,projectandbackup,labels,selectbackupdir,finder,title))
            .limitTo (QuollFileField.Type.directory)
            .initialFile (backupDir)
            .withViewer (viewer)
            .build ();

        bb.getChildren ().addAll (projBackupDirErr, projBackupDirF);

        QuollButton projBackupDirChangeB = QuollButton.builder ()
            .label (options,projectandbackup,labels,selectbackupdir,finder,label)
            .onAction (ev ->
            {

                projBackupDirErr.setVisible (false);

                this.handleBackupsDirChange (projBackupDirF.getFile (),
                                             pv,
                                             err ->
                {

                    if (err != null)
                    {

                        projBackupDirErr.setErrors (err);
                        projBackupDirErr.setVisible (true);

                    }

                    // Reset the project dir, something went wrong.
                    //projBackupDirF.setFile (proj.getBackupDirectory ().toPath ());

                });

            })
            .build ();
        projBackupDirChangeB.setDisable (true);

        projBackupDirF.fileProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv == null)
            {

                return;

            }

            projBackupDirChangeB.setDisable (newv.equals (proj.getBackupDirectory ().toPath ()));

        });

        this.propertyBinder.addChangeListener (proj.backupDirectoryProperty (),
                                               (pr, oldv, newv) ->
        {

            if (newv == null)
            {

                return;

            }

            projBackupDirF.setFile (newv.toPath ());

            projBackupDirChangeB.setDisable (newv.toPath ().equals (projBackupDirF.fileProperty ().getValue ()));

        });

        QuollButton createBackupB = QuollButton.builder ()
            .label (options,projectandbackup,labels,createbackup)
            .onAction (ev ->
            {

                BackupsManager.showCreateBackup (proj,
                                                 proj.getFilePassword (),
                                                 this.viewer);

            })
            .build ();

        QuollButton manageBackupsB = QuollButton.builder ()
            .label (options,projectandbackup,labels,managebackups)
            .onAction (ev ->
            {

                BackupsManager.showForProject (Environment.getProjectInfo (proj),
                                               this.viewer);

            })
            .build ();

        HBox buts = new HBox ();
        buts.getStyleClass ().add (StyleClassNames.ITEM);
        buts.getChildren ().addAll (createBackupB, manageBackupsB);

        Section s = Section.builder ()
            .sectionId (Section.Id.project)
            .styleClassName (StyleClassNames.PROJECT)
            .title (options,projectandbackup,title)
            .description (options,projectandbackup,text)
            .subtitle (getUILanguageStringProperty (options,projectandbackup,labels,subtitles,projectdir))
            .mainItem (getUILanguageStringProperty (options,projectandbackup,labels,selectprojectdir,text),
                       b)
            .subItem (projDirChangeB)
            .subtitle (getUILanguageStringProperty (options,projectandbackup,labels,subtitles,backups))
            .mainItem (autoBackupEnb)
            .subItem (getUILanguageStringProperty (options,projectandbackup,labels,createbackupafter),
                      backupTimeB)
            .mainItem (getUILanguageStringProperty (options,projectandbackup,labels,nobackupstokeep),
                       backupsCountB)
            .mainItem (getUILanguageStringProperty (options,projectandbackup,labels,selectbackupdir,text),
                       bb)
            .subItem (projBackupDirChangeB)
            .mainItem (buts)
            .build ();

        return s;

    }

    private Section createAssetsSection (AbstractProjectViewer viewer)
    {

        String addAsset = UserProperties.get (Constants.ADD_ASSETS_PROPERTY_NAME);

        if (addAsset == null)
        {

            addAsset = Constants.ADD_ASSETS_POPUP;

        }

        QuollRadioButtons rbuts = QuollRadioButtons.builder ()
            .button (QuollRadioButton.builder ()
                        .label (options,assets,labels,newasset,options,alwayspopup,text)
                        .tooltip (options,assets,labels,newasset,options,alwayspopup,tooltip)
                        .selected (addAsset.equals (Constants.ADD_ASSETS_POPUP))
                        .onAction (ev ->
                        {

                            UserProperties.set (Constants.ADD_ASSETS_PROPERTY_NAME,
                                                Constants.ADD_ASSETS_POPUP);

                        })
                        .build ())
            .button (QuollRadioButton.builder ()
                        .label (options,assets,labels,newasset,options,popupifpossible,text)
                        .tooltip (options,assets,labels,newasset,options,popupifpossible,tooltip)
                        .selected (addAsset.equals (Constants.ADD_ASSETS_TRY_POPUP))
                        .onAction (ev ->
                        {

                            UserProperties.set (Constants.ADD_ASSETS_PROPERTY_NAME,
                                                Constants.ADD_ASSETS_TRY_POPUP);

                        })
                        .build ())
            .button (QuollRadioButton.builder ()
                        .label (options,assets,labels,newasset,options,owntab,text)
                        .tooltip (options,assets,labels,newasset,options,owntab,tooltip)
                        .selected (addAsset.equals (Constants.ADD_ASSETS_TAB))
                        .onAction (ev ->
                        {

                            UserProperties.set (Constants.ADD_ASSETS_PROPERTY_NAME,
                                                Constants.ADD_ASSETS_TAB);

                        })
                        .build ())
            .build ();

        ObservableList<UserConfigurableObjectType> types = FXCollections.observableList (new ArrayList<> ());

        this.propertyBinder.addSetChangeListener (viewer.getProject ().getUserConfigurableObjectTypes (),
                                                  ch ->
        {

            if (ch.wasAdded ())
            {

                if (ch.getElementAdded ().isAssetObjectType ())
                {

                    types.add (ch.getElementAdded ());

                }

            }

            if (ch.wasRemoved ())
            {

                types.remove (ch.getElementRemoved ());

            }

        });

        for (UserConfigurableObjectType t : viewer.getProject ().getAssetUserConfigurableObjectTypes (true))
        {

            types.add (t);

        }

        ComboBox<UserConfigurableObjectType> editTypes = new ComboBox<> (types);

        Callback<ListView<UserConfigurableObjectType>, ListCell<UserConfigurableObjectType>> cellFactory = listView ->
        {

            return new ListCell<UserConfigurableObjectType> ()
            {

                // This is dumb...just sayin...
                @Override
                protected void updateItem (UserConfigurableObjectType type,
                                           boolean                    empty)
                {

                    super.updateItem (type,
                                      empty);

                    if (empty || item == null)
                    {

                        this.textProperty ().unbind ();
                        setText ("");

                    } else {

                        ImageView iv = new ImageView ();
                        iv.imageProperty ().bind (type.icon16x16Property ());

                        this.textProperty ().bind (type.nameProperty ());
                        this.setGraphic (iv);

                    }

                }

            };

        };
        editTypes.setCellFactory (cellFactory);
        editTypes.setButtonCell (cellFactory.call (null));
        editTypes.getSelectionModel ().select (types.get (0));

        QuollButton editType = QuollButton.builder ()
            .label (buttons,edit)
            .onAction (ev ->
            {

                viewer.showEditUserConfigurableType (editTypes.getSelectionModel ().getSelectedItem ());

            })
            .build ();

        QuollButton deleteType = QuollButton.builder ()
            .label (buttons,delete)
            .onAction (ev ->
            {

                viewer.showDeleteUserConfigurableType (editTypes.getSelectionModel ().getSelectedItem ());

            })
            .build ();

        HBox b = new HBox ();
        b.getChildren ().addAll (editTypes, editType, deleteType);
        b.getStyleClass ().add (StyleClassNames.ITEM);

        Section s = Section.builder ()
            .sectionId (Section.Id.assets)
            .styleClassName (StyleClassNames.ASSETS)
            .title (options,assets,title)
            .description (options,assets,text)
            .subtitle (getUILanguageStringProperty (options,assets,labels,subtitles,newasset))
            .mainItem (getUILanguageStringProperty (options,assets,labels,newasset,text),
                       rbuts)
            .subtitle (getUILanguageStringProperty (options,assets,labels,subtitles,existing))
            .mainItem (getUILanguageStringProperty (options,assets,labels,editassetconfig),
                       b)
            .subtitle (getUILanguageStringProperty (options,assets,labels,subtitles,newtype))
            .mainItem (QuollButton.builder ()
                        .label (options,assets,labels,addtype)
                        .onAction (ev ->
                        {

                            viewer.showAddNewUserConfigurableType ();

                        })
                        .build ())
             .subtitle (getUILanguageStringProperty (options,assets,labels,subtitles,importtypes))
             .mainItem (getUILanguageStringProperty (options,assets,labels,importtypes,text),
                        QuollButton.builder ()
                            .label (options,assets,labels,importtypes,label)
                            .onAction (ev ->
                            {

                                viewer.showImportUserConfigurableTypes ();

                            })
                            .build ())
            .build ();

        return s;

    }

    private Section createLookSection (AbstractViewer viewer)
    {

        QuollButton feedbackB = QuollButton.builder ()
            .label (options,lookandsound,labels,feedback)
            .onAction (ev ->
            {

                QuollPopup qp = this.viewer.getPopupById (ContactUILanguageStringsCreatorPopup.getPopupId ());

                if (qp != null)
                {

                    qp.toFront ();
                    return;

                }

                new ContactUILanguageStringsCreatorPopup (this.viewer).show ();

            })
            .build ();

        boolean showFeedbackB = true;

        UILanguageStrings currUIL = UILanguageStringsManager.getCurrentUILanguageStrings ();

        if ((currUIL.isEnglish ())
            ||
            (currUIL.isUser ())
           )
        {

            showFeedbackB = false;

        }

        feedbackB.setVisible (showFeedbackB);

        ComboBox<UILanguageStringsInfo> uilangSel = new ComboBox<> ();
        uilangSel.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            // TODO Fix this!
            String uid = (newv.user ? "user-" : "") + newv.id;

            feedbackB.setVisible ((!UILanguageStrings.isEnglish (uid)) && (!newv.user));

            if (uid.equals (UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME)))
            {

                return;

            }

            UILanguageStrings ls = null;

            try
            {

                ls = UILanguageStringsManager.getUILanguageStrings (newv);//uid);

            } catch (Exception e) {

                Environment.logError ("Unable to get ui language for: " + uid,
                                      e);

                ComponentUtils.showErrorMessage (viewer,
                                                 getUILanguageStringProperty (uilanguage,set,actionerror));

                return;

            }

            Runnable setLang = () ->
            {

                try
                {

                    Environment.setUILanguage (uid);

                } catch (Exception e) {

                    Environment.logError ("Unable to set ui language to: " + uid,
                                          e);

                    ComponentUtils.showErrorMessage (viewer,
                                                     getUILanguageStringProperty (uilanguage,set,actionerror));

                }

            };

            if (ls == null)
            {

                QuollPopup.messageBuilder ()
                    .withViewer (viewer)
                    .title (uilanguage,set,downloading,title)
                    .message (uilanguage,set,downloading,text)
                    .closeButton ()
                    .build ();

                UILanguageStringsManager.downloadUILanguageFile (uid,
                                                                 setLang,
                                                                 () ->
                                                                 {

                                                                     ComponentUtils.showErrorMessage (viewer,
                                                                                                      getUILanguageStringProperty (uilanguage,set,actionerror));

                                                                });

            } else {

                UIUtils.runLater (setLang);

            }

        });

        Callback<ListView<UILanguageStringsInfo>, ListCell<UILanguageStringsInfo>> cellFactory = (lv ->
        {

            return new ListCell<UILanguageStringsInfo> ()
            {

                @Override
                protected void updateItem (UILanguageStringsInfo item,
                                           boolean               empty)
                {

                    super.updateItem (item,
                                      empty);

                    if (empty || item == null)
                    {

                        this.textProperty ().unbind ();
                        setText ("");

                    } else {

                        StringProperty textProp = null;//item.nativeName;

                        if (!UILanguageStrings.isEnglish (item.id))
                        {

                            textProp = getUILanguageStringProperty (Arrays.asList (uilanguage,set,LanguageStrings.item),
                                                                    item.nativeName,
                                                                    (item.languageName != null ? item.languageName : "< >"),
                                                                    Environment.formatNumber (item.percentComplete),
                                                                    item.user ? getUILanguageStringProperty (uilanguage,set,createdbyyou) : "");
                                                                    /*
                            text = String.format ("%1$s (%2$s, %3$s%% %4$s%5$s)",
                                                  item.nativeName,
                                                  item.languageName,
                                                  Environment.formatNumber (item.percentComplete),
                                                  "complete",
                                                  item.user ? ", created by you" : "");
                                                        */
                        } else {

                            textProp = new SimpleStringProperty (item.nativeName);

                        }

                        this.textProperty ().bind (textProp);

                    }

                }

            };

        });

        uilangSel.setCellFactory (cellFactory);
        uilangSel.setButtonCell (cellFactory.call (null));
        uilangSel.setDisable (true);

        Environment.schedule (() ->
        {

            Set<UILanguageStringsInfo> _uilangs = Environment.getAvailableUILanguageStrings ();

            Set<UILanguageStringsInfo> uilangs = _uilangs.stream ()
                .sorted ((o1, o2) ->
                {

                    if (UILanguageStrings.isEnglish (o1.id))
                    {

                        return -1 * Integer.MAX_VALUE;

                    }

                    if (UILanguageStrings.isEnglish (o2.id))
                    {

                        return -1 * Integer.MAX_VALUE;

                    }

                    if (o1.nativeName.equals (o2.nativeName))
                    {

                        return Integer.compare (o1.percentComplete, o2.percentComplete);

                    }

                    return o1.nativeName.compareTo (o2.nativeName);

                })
                .collect (Collectors.toSet ());

            UIUtils.runLater (() ->
            {

                uilangSel.getItems ().addAll (uilangs);

                uilangSel.setDisable (false);

                String sel = UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME);

                uilangs.stream ()
                    .forEach (in ->
                    {

                        if ((in.id.equals (sel))
                            ||
                            // Is it user strings?
                            ((in.user)
                             &&
                             (("user-" + in.id).equals (sel))
                            )
                           )
                        {

                            uilangSel.getSelectionModel ().select (in);

                        }

                    });

            });

        },
        0,
        -1);

        HBox hb = new HBox ();
        hb.getStyleClass ().add (StyleClassNames.ITEM);
        hb.getChildren ().addAll (uilangSel, feedbackB);

        QuollButton createTransB = QuollButton.builder ()
            .label (options,lookandsound,labels,createtranslation)
            .onAction (ev ->
            {

                UIUtils.showAddNewUILanguageStringsPopup (this.viewer);

            })
            .build ();

        QuollButton editTransB = QuollButton.builder ()
            .label (options,lookandsound,labels,edittranslation)
            .onAction (ev ->
            {

                UIUtils.showEditUILanguageStringsSelectorPopup (this.viewer);

            })
            .build ();

        HBox bb = new HBox ();
        bb.getStyleClass ().add (StyleClassNames.ITEM);
        bb.getChildren ().addAll (createTransB, editTransB);

        VBox uilangb = new VBox ();
        uilangb.getChildren ().addAll (hb, bb);

        Slider textSize = new Slider (5, 30, 1);
        textSize.setBlockIncrement (1);
        textSize.setMajorTickUnit (5);
        textSize.setMinorTickCount (4);

        HBox thb = new HBox ();
        thb.getStyleClass ().add (StyleClassNames.TEXTSIZE);

        Label less = QuollLabel.builder ()
            .label (getUILanguageStringProperty (options,lookandsound,labels,basefontsizeless))
            .styleClassName (StyleClassNames.LESS)
            .build ();
        Label more = QuollLabel.builder ()
            .label (getUILanguageStringProperty (options,lookandsound,labels,basefontsizemore))
            .styleClassName (StyleClassNames.MORE)
            .build ();
        thb.getChildren ().addAll (less, textSize, more);

        double f = UserProperties.getUIBaseFontSize ();

        textSize.setValue (f);
        textSize.valueChangingProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv)
            {

                return;

            }

            UserProperties.setUIBaseFontSize (textSize.valueProperty ().floatValue ());

        });

        textSize.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            if (textSize.isValueChanging ())
            {

                return;

            }

            UserProperties.setUIBaseFontSize (textSize.valueProperty ().floatValue ());

        });

        this.propertyBinder.addChangeListener (UserProperties.uiBaseFontSizeProperty (),
                                               (pr, oldv, newv) ->
        {

            if (newv.floatValue () != textSize.valueProperty ().floatValue ())
            {

                textSize.setValue (newv.floatValue ());

            }

        });

        ComboBox<Font> uiFont = UIUtils.getFontSelector (this.viewer,
                                                         UserProperties.getUIBaseFont ());

        uiFont.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            UserProperties.setUIBaseFont (uiFont.valueProperty ().getValue ());

        });

        QuollFileField styleSheet = QuollFileField.builder ()
            .initialFile (UserProperties.getUserStyleSheet ())
            .limitTo (QuollFileField.Type.file)
            .withViewer (this.viewer)
            .chooserTitle (options,lookandsound,stylesheet,finder,title)
            .showClear (true)
            .viewButtonTooltip (options,lookandsound,stylesheet,finder,view,tooltip)
            .findButtonTooltip (options,lookandsound,stylesheet,finder,find,tooltip)
            .clearButtonTooltip (options,lookandsound,stylesheet,finder,clear,tooltip)
            .fileExtensionFilter (getUILanguageStringProperty (options,lookandsound,stylesheet,finder,extensionfilter),
                                  "css")
            .build ();

        styleSheet.fileProperty ().addListener ((pr, oldv, newv) ->
        {

            UserProperties.setUserStyleSheet (newv);

        });

        QuollTimeRangeSelector timeRange = QuollTimeRangeSelector.builder ()
            .now ()
            .from (UserProperties.getAutoNightModeFromTime ())
            .to (UserProperties.getAutoNightModeToTime ())
            .build ();

        QuollCheckBox autoNightMode = QuollCheckBox.builder ()
            .label (options,lookandsound,labels,autonightmode)
            .selected (UserProperties.autoNightModeEnabledProperty ().getValue ())
            .build ();

        timeRange.setDisable (!autoNightMode.isSelected ());

        autoNightMode.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            timeRange.setDisable (!autoNightMode.isSelected ());

            UserProperties.setAutoNightModeEnabled (autoNightMode.isSelected ());

        });

        timeRange.fromProperty ().addListener ((pr, oldv, newv) ->
        {

            UserProperties.setAutoNightModeFromTime (newv);

        });

        timeRange.toProperty ().addListener ((pr, oldv, newv) ->
        {

            UserProperties.setAutoNightModeToTime (newv);

        });

        NumberSelector nightModeBGColor = NumberSelector.builder ()
            .styleClassName (StyleClassNames.NIGHTMODEBGCOLOR)
            .min (0)
            .max (150)
            .initialValue ((int) (UserProperties.nightModeBGColorProperty ().getValue ().getRed () * 255))
            .onValueChanged ((oldv, newv) ->
            {

                // Get as hex, repeat for r, g, b to get grey value.
                UserProperties.setNightModeBGColor (UIUtils.hexToColor (UIUtils.rgbToHex (newv.intValue (),
                                                                                          newv.intValue (),
                                                                                          newv.intValue ())));
            })
            .build ();

        QuollCheckBox permNightMode = QuollCheckBox.builder ()
            .label (options,lookandsound,labels,permanentnightmode)
            .selected (UserProperties.permanentNightModeEnabledProperty ().getValue ())
            .build ();

        permNightMode.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            if (!newv)
            {

                timeRange.setDisable (false);
                autoNightMode.setDisable (false);

            } else {

                timeRange.setDisable (true);
                autoNightMode.setDisable (true);
                Environment.setNightModeEnabled (true);

            }

            UserProperties.setPermanentlyEnableNightMode (newv);

        });

        Section s = Section.builder ()
            .sectionId (Section.Id.look)
            .styleClassName (StyleClassNames.LOOKS)
            .title (options,lookandsound,title)
            .description (options,lookandsound,text)
            .subtitle (getUILanguageStringProperty (options,lookandsound,labels,subtitles,language))
            .mainItem (getUILanguageStringProperty (options,lookandsound,labels,uilanguage),
                       uilangb)
            .subtitle (getUILanguageStringProperty (options,lookandsound,labels,subtitles,font))
            .mainItem (getUILanguageStringProperty (options,lookandsound,labels,basefont),
                       uiFont)
            .mainItem (getUILanguageStringProperty (options,lookandsound,labels,basefontsize),
                       thb)
                       /*
                       TODO Removed for now...
            .mainItem (getUILanguageStringProperty (options,lookandsound,labels,stylesheet,text),
                       styleSheet)
                       */
            .subtitle (getUILanguageStringProperty (options,lookandsound,labels,subtitles,layout))
            .mainItem (getUILanguageStringProperty (options,lookandsound,labels,interfacelayout,text),
                       this.createLayoutSelector ())
            .mainItem (getUILanguageStringProperty (options,lookandsound,labels,showtoolbar),
                       QuollComboBox.builder ()
                         .items (getUILanguageStringProperty (options,lookandsound,labels,abovesidebar),
                                 getUILanguageStringProperty (options,lookandsound,labels,belowsidebar))
                         .selectedIndex (Constants.TOP.equals (UserProperties.get (Constants.TOOLBAR_LOCATION_PROPERTY_NAME)) ? 0 : 1)
                         .onSelected (ev ->
                         {

                             ComboBox cb = (ComboBox) ev.getSource ();

                             int ind = cb.getSelectionModel ().getSelectedIndex ();

                             UserProperties.setToolbarLocation (ind == 0 ? Constants.TOP : Constants.BOTTOM);

                         })
                         .build ())
            .mainItem (getUILanguageStringProperty (options,lookandsound,labels,showtabs),
                       QuollComboBox.builder ()
                          .items (getUILanguageStringProperty (options,lookandsound,labels,showtabstop),
                                  getUILanguageStringProperty (options,lookandsound,labels,showtabsbottom))
                          .selectedIndex (Constants.TOP.equals (UserProperties.get (Constants.TABS_LOCATION_PROPERTY_NAME)) ? 0 : 1)
                          .onSelected (ev ->
                          {

                              ComboBox cb = (ComboBox) ev.getSource ();

                              int ind = cb.getSelectionModel ().getSelectedIndex ();

                              UserProperties.set (Constants.TABS_LOCATION_PROPERTY_NAME,
                                                  ind == 0 ? Constants.TOP : Constants.BOTTOM);

                          })
                          .build ())
            .subtitle (getUILanguageStringProperty (options,lookandsound,labels,subtitles,naming))
            .mainItem (QuollButton.builder ()
                        .label (options,lookandsound,labels,changenames)
                        .onAction (ev ->
                        {

                            viewer.runCommand (AbstractViewer.CommandId.editobjectnames);

                        })
                        .build ())
            .subtitle (getUILanguageStringProperty (options,lookandsound,labels,subtitles,tags))
            .mainItem (QuollButton.builder ()
                .label (options,lookandsound,labels,tags)
                .onAction (ev ->
                {

                    viewer.runCommand (AbstractViewer.CommandId.edittags);

                })
                .build ())
            .subtitle (getUILanguageStringProperty (options,lookandsound,labels,subtitles,nightmode))
            .mainItem (permNightMode)
            .mainItem (autoNightMode)
            .subItem (timeRange)
            .mainItem (getUILanguageStringProperty (options,lookandsound,labels,nightmodebgcolor),
                       nightModeBGColor)
            .subtitle (getUILanguageStringProperty (options,lookandsound,labels,subtitles,projectswindow))
            .mainItem (QuollCheckBox.builder ()
                .label (options,lookandsound,labels,keepprojectswindowsopen)
                .userProperty (Constants.KEEP_PROJECTS_WINDOW_WHEN_PROJECT_OPENED_PROPERTY_NAME)
                .build ())
            .mainItem (QuollCheckBox.builder ()
                .label (options,lookandsound,labels,showprojectswindownoopenproject)
                .userProperty (Constants.SHOW_PROJECTS_WINDOW_WHEN_NO_OPEN_PROJECTS_PROPERTY_NAME)
                .build ())
            .subtitle (getUILanguageStringProperty (options,lookandsound,labels,subtitles,find))
            .mainItem (getUILanguageStringProperty (options,lookandsound,labels,whenfind),
                       QuollRadioButtons.builder ()
                            .button (QuollRadioButton.builder ()
                                        .label (options,lookandsound,labels,expandall)
                                        .onAction (ev ->
                                        {

                                            UserProperties.set (Constants.SHOW_EACH_CHAPTER_FIND_RESULT_PROPERTY_NAME,
                                                                true);

                                        })
                                        .selected (UserProperties.getAsBoolean (Constants.SHOW_EACH_CHAPTER_FIND_RESULT_PROPERTY_NAME))
                                        .build ())
                            .button (QuollRadioButton.builder ()
                                        .label (options,lookandsound,labels,justchapter)
                                        .onAction (ev ->
                                        {

                                            UserProperties.set (Constants.SHOW_EACH_CHAPTER_FIND_RESULT_PROPERTY_NAME,
                                                                false);

                                        })
                                        .selected (!UserProperties.getAsBoolean (Constants.SHOW_EACH_CHAPTER_FIND_RESULT_PROPERTY_NAME))
                                        .build ())
                            .build ())
            .build ();

        return s;

    }

    private Section createProblemsSection (AbstractViewer viewer)
    {

        QuollCheckBox sendErrorsToSupport = QuollCheckBox.builder ()
            .label (options,errors,labels,send)
            .userProperty (Constants.AUTO_SEND_ERRORS_TO_SUPPORT_PROPERTY_NAME)
            .build ();

        Section s = Section.builder ()
            .styleClassName (StyleClassNames.BUG)
            .sectionId (Section.Id.problems)
            .title (options,errors,title)
            .description (options,errors,text)
            .mainItem (sendErrorsToSupport)
            .build ();

        return s;

    }

    private Section createAchievementsSection (AbstractViewer viewer)
    {

        final AchievementsManager man = Environment.getAchievementsManager ();

        QuollCheckBox achievementsOn = QuollCheckBox.builder ()
            .label (options,achievements,labels,enable)
            .build ();
        achievementsOn.setSelected (man.isAchievementsEnabled ());

        achievementsOn.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            man.setAchievementsEnabled (achievementsOn.isSelected ());

        });

        QuollCheckBox achievementSounds = QuollCheckBox.builder ()
            .label (options,achievements,labels,playsound)
            .build ();
        achievementSounds.setSelected (man.isSoundEnabled ());

        QuollCheckBox fullScreenSoundsOn = QuollCheckBox.builder ()
            .label (options,achievements,labels,playsoundinfullscreen)
            .build ();

        fullScreenSoundsOn.setSelected (man.isSoundsInFullScreenEnabled ());

        achievementSounds.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            man.setSoundEnabled (achievementSounds.isSelected ());

            fullScreenSoundsOn.setDisable (!achievementSounds.isSelected ());

        });

        fullScreenSoundsOn.setSelected (man.isSoundsInFullScreenEnabled ());

        fullScreenSoundsOn.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            man.setSoundsInFullScreenEnabled (fullScreenSoundsOn.isSelected ());

        });

        Section s = Section.builder ()
            .sectionId (Section.Id.achievements)
            .styleClassName (StyleClassNames.ACHIEVEMENTS)
            .title (options,achievements,title)
            .description (options,achievements,text)
            .mainItem (achievementsOn)
            .mainItem (achievementSounds)
            .subItem (fullScreenSoundsOn)
            .build ();

        return s;

    }

    private Section createItemsAndRulesSection (AbstractViewer viewer)
    {

        // Gaaah....
        boolean isAPV = (viewer instanceof ProjectViewer);

        HBox b = new HBox ();
        b.getStyleClass ().add (StyleClassNames.ITEM);
        b.getChildren ().add (QuollButton.builder ()
            .label (objectnames,plural, Note.NOTE_TYPE_OBJECT_TYPE)
            .onAction (ev ->
            {

                viewer.showManageNoteTypes ();

            })
            .build ());

        b.getChildren ().add (QuollButton.builder ()
            .label (objectnames,plural, Tag.OBJECT_TYPE)
            .onAction (ev ->
            {

                viewer.runCommand (AbstractViewer.CommandId.edittags);

            })
            .build ());

        if (isAPV)
        {

            b.getChildren ().add (QuollButton.builder ()
                .label (options,itemsandrules,labels,problemfinderrules)
                .onAction (ev ->
                {

                    ((ProjectViewer) viewer).showProblemFinderRuleConfig ();

                })
                .build ());

        }

        Section s = Section.builder ()
            .sectionId (Section.Id.itemsAndRules)
            .styleClassName (StyleClassNames.EDIT)
            .title (options,itemsandrules,title)
            .description (options,itemsandrules,text)
            .mainItem (b)
            .build ();

        return s;

    }

    private Section createEditorsSection (AbstractViewer viewer)
    {

        QuollCheckBox autoLogin = QuollCheckBox.builder ()
            .label (options,editors,labels,autologin)
            .build ();
        //"Automatically login/go online whenever Quoll Writer starts");

        autoLogin.setSelected (EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME));

        autoLogin.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            try
            {

                EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_LOGIN_AT_QW_START_PROPERTY_NAME,
                                                       autoLogin.isSelected ());

            } catch (Exception e) {

                Environment.logError ("Unable to set to login at start",
                                      e);

            }

        });

        List<EditorEditor.OnlineStatus> statuses = Arrays.asList (EditorEditor.OnlineStatus.online,
            EditorEditor.OnlineStatus.busy,
            EditorEditor.OnlineStatus.away,
            EditorEditor.OnlineStatus.snooze);

        final ComboBox<EditorEditor.OnlineStatus> defStatus = new ComboBox<> (FXCollections.observableList (statuses));

        defStatus.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            EditorEditor.OnlineStatus selVal = defStatus.getSelectionModel ().getSelectedItem ();

            try
            {

                EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_DEFAULT_ONLINE_STATUS_PROPERTY_NAME,
                                                       selVal.getType ());

            } catch (Exception e) {

                Environment.logError ("Unable to set default online status",
                                      e);

            }

        });

        Callback<ListView<EditorEditor.OnlineStatus>, ListCell<EditorEditor.OnlineStatus>> cellFactory = (lv ->
        {

            return new ListCell<EditorEditor.OnlineStatus> ()
            {

                @Override
                protected void updateItem (EditorEditor.OnlineStatus item,
                                           boolean                   empty)
                {

                    super.updateItem (item,
                                      empty);

                    if (empty || item == null)
                    {

                        this.textProperty ().unbind ();
                        setText ("");

                    } else {

                        String iconName = item.getType ();

                        IconBox ib = IconBox.builder ()
                            .iconName (iconName)
                            .build ();
                        this.setGraphic (ib);

                        this.textProperty ().bind (getUILanguageStringProperty (editors,LanguageStrings.statuses,item.getType ()));
                        //this.getStyleClass ().add (Constants.ONLINE_STATUS_ICON_NAME_PREFIX + iconName);

                    }

                }

            };

        });

        defStatus.setCellFactory (cellFactory);
        defStatus.setButtonCell (cellFactory.call (null));

        String defOnlineStatus = EditorsEnvironment.getEditorsProperty (Constants.QW_EDITORS_SERVICE_DEFAULT_ONLINE_STATUS_PROPERTY_NAME);

        if (defOnlineStatus != null)
        {

            defStatus.getSelectionModel ().select (EditorEditor.OnlineStatus.valueOf (defOnlineStatus));

        }

        QuollCheckBox fullScreenBusy = QuollCheckBox.builder ()
            .label (options,editors,labels,fullscreenbusystatus)
            .build ();
        //"Set my status to <b>Busy</b> when I enter full screen mode");

        fullScreenBusy.setSelected (EditorsEnvironment.getEditorsPropertyAsBoolean (Constants.QW_EDITORS_SERVICE_SET_BUSY_ON_FULL_SCREEN_ENTERED_PROPERTY_NAME));

        fullScreenBusy.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            try
            {

                EditorsEnvironment.setEditorsProperty (Constants.QW_EDITORS_SERVICE_SET_BUSY_ON_FULL_SCREEN_ENTERED_PROPERTY_NAME,
                                                       fullScreenBusy.isSelected ());

                if (fullScreenBusy.isSelected ())
                {

                    if (Environment.isInFullScreen ())
                    {

                        EditorsEnvironment.fullScreenEntered ();

                    }

                }

            } catch (Exception e) {

                Environment.logError ("Unable to set to busy on full screen entered",
                                      e);

            }

        });

        QuollCheckBox logMessages = QuollCheckBox.builder ()
            .label (options,editors,labels,logmessages,text)
            .build ();
        //"Log messages I send/receive (debug only)");

        logMessages.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            EditorsEnvironment.logEditorMessages (logMessages.isSelected ());

        });

        Section s = Section.builder ()
            .sectionId (Section.Id.editors)
            .styleClassName (StyleClassNames.CONTACTS)
            .title (options,editors,title)
            .description (options,editors,text)
            .mainItem (autoLogin)
            .mainItem (getUILanguageStringProperty (options,editors,labels,defaultstatus),
                       defStatus)
            .mainItem (fullScreenBusy)
            .mainItem (logMessages)
            .subItem (getUILanguageStringProperty (options,editors,labels,logmessages,help))
            .build ();

        return s;

    }

    public static class Section extends VBox implements Stateful
    {

        public enum Id
        {

            warmups ("warmups"),
            look ("look"),
            naming ("naming"),
            editing ("editing"),
            editors ("editors"),
            itemsAndRules ("itemsAndRules"),
            achievements ("achievements"),
            problems ("problems"),
            betas ("betas"),
            start ("start"),
            assets ("assets"),
            project ("project"),
            website ("website");

            private String type = null;

            Id (String type)
            {

                this.type = type;

            }

            public String getType ()
            {

                return this.type;

            }

        }

        private AccordionItem acc = null;
        private Section.Id id = null;
        private VBox openContent = null;
        private Header header = null;

        private Section (Builder b)
        {

            if (b.sectId == null)
            {

                throw new IllegalArgumentException ("Section id must be provided.");

            }

            if (b.description == null)
            {

                throw new IllegalArgumentException ("Description must be provided.");

            }

            if (b.title == null)
            {

                throw new IllegalArgumentException ("Title must be provided.");

            }

            if (b.items.size () == 0)
            {

                throw new IllegalArgumentException ("At least 1 item must be provided.");

            }

            this.id = b.sectId;

            BasicHtmlTextFlow desc = BasicHtmlTextFlow.builder ()
                .text (b.description)
                .build ();
            desc.managedProperty ().bind (desc.visibleProperty ());

            VBox c = new VBox ();
            c.managedProperty ().bind (c.visibleProperty ());
            c.getStyleClass ().add (StyleClassNames.CONTENT);

            this.openContent = c;

            // Build the section.
            for (Item it : b.items)
            {

                if (it.type == Item.Type.subtitle)
                {

                    QuollLabel l = QuollLabel.builder ()
                        .label (it.description)
                        .styleClassName (StyleClassNames.SUBTITLE)
                        .build ();
                    c.getChildren ().add (l);
                    continue;

                }

                String style = it.type == Item.Type.main ? StyleClassNames.MAIN : StyleClassNames.SUB;

                if (it.description != null)
                {

                    VBox bb = new VBox ();

                    Node n = BasicHtmlTextFlow.builder ()
                                .styleClassName (StyleClassNames.DESCRIPTION)
                                .text (it.description)
                                .build ();

                    bb.getStyleClass ().add (style);
                    bb.getChildren ().add (n);
                    style = StyleClassNames.SUB;
                    c.getChildren ().add (bb);

                }

                String _style = style;

                it.controls.stream ()
                    .forEach (con ->
                    {

                        VBox bb = new VBox ();
                        bb.getStyleClass ().add (_style);
                        bb.getChildren ().add (con);
                        c.getChildren ().add (bb);

                    });

            }
/*
            this.acc = AccordionItem.builder ()
                .title (b.title)
                .openContent (c)
                .open (true)
                .closedContent (desc)
                .styleClassName (b.styleName)
                .build ();
*/
            VBox.setVgrow (c,
                           Priority.ALWAYS);

            this.header = Header.builder ()
                .title (b.title)
                .styleClassName (b.styleName)
                .build ();

        }

        public Node getContent ()
        {

            return this.openContent;

        }

        public Header getHeader ()
        {

            return this.header;

        }

        public Node getOpenContent ()
        {

            return this.openContent;

        }

        @Override
        public void init (State state)
        {

            this.acc.init (state);

        }

        @Override
        public State getState ()
        {

            return this.acc.getState ();

        }

        public Section.Id getSectionId ()
        {

            return this.id;

        }

        /**
         * Get a builder to create a new Section.
         *
         * @returns A new builder.
         */
        public static Section.Builder builder ()
        {

            return new Builder ();

        }

        private static class Item
        {

            public enum Type
            {
                main,
                sub,
                subtitle
            }

            public StringProperty description = null;
            public Set<Node> controls = new LinkedHashSet<> ();
            public Type type = Type.main;

            public Item (StringProperty desc,
                         Set<Node>      controls,
                         Type           type)
            {

                this.description = desc;
                this.controls.addAll (controls);
                this.type = type;

            }

            public Item (StringProperty desc,
                         Node           control,
                         Type           type)
            {

                this.description = desc;
                this.controls.add (control);
                this.type = type;

            }

        }

        public static class Builder implements IBuilder<Builder, Section>
        {

            private StringProperty description = null;
            private StringProperty title = null;
            private String styleName = null;
            private Set<Item> items = new LinkedHashSet<> ();
            private AbstractViewer viewer = null;
            public Section.Id sectId = null;

            private Builder ()
            {

            }

            @Override
            public Section build ()
            {

                return new Section (this);

            }

            @Override
            public Builder _this ()
            {

                return this;

            }

            public Builder sectionId (Section.Id id)
            {

                this.sectId = id;
                return this;

            }

            public Builder withViewer (AbstractViewer viewer)
            {

                this.viewer = viewer;
                return this;

            }

            public Builder styleClassName (String name)
            {

                this.styleName = name;
                return this;

            }

            public Builder subtitle (StringProperty p)
            {

                return this.item (p,
                                  (Node) null,
                                  Item.Type.subtitle);

            }

            public Builder mainItem (Node control)
            {

                return this.item (null,
                                  control,
                                  Item.Type.main);

            }

            public Builder mainItem (Set<Node> controls)
            {

                return this.item (null,
                                  controls,
                                  Item.Type.main);

            }

            public Builder mainItem (StringProperty desc,
                                     Node           control)
            {

                return this.item (desc,
                                  control,
                                  Item.Type.main);

            }

            public Builder mainItem (StringProperty desc,
                                     Node...        controls)
            {

                return this.item (desc,
                                  (controls != null ? new LinkedHashSet<> (Arrays.asList (controls)) : null),
                                  Item.Type.main);

            }

            public Builder mainItem (StringProperty desc,
                                     Set<Node>      control)
            {

                return this.item (desc,
                                  control,
                                  Item.Type.main);

            }

            public Builder subItem (Node control)
            {

                return this.item (null,
                                  control,
                                  Item.Type.sub);

            }

            public Builder subItem (Set<Node> controls)
            {

                return this.item (null,
                                  controls,
                                  Item.Type.sub);

            }

            public Builder subItem (StringProperty desc,
                                    Node...        controls)
            {

                return this.item (desc,
                                  (controls != null ? new LinkedHashSet<> (Arrays.asList (controls)) : null),
                                  Item.Type.sub);

            }

            public Builder subItem (StringProperty desc,
                                    Node           control)
            {

                return this.item (desc,
                                  control,
                                  Item.Type.sub);

            }

            public Builder subItem (StringProperty desc,
                                    Set<Node>      controls)
            {

                return this.item (desc,
                                  controls,
                                  Item.Type.sub);

            }

            public Builder item (StringProperty desc,
                                 Node           control,
                                 Item.Type      type)
            {

                this.items.add (new Item (desc,
                                          control,
                                          type));
                return this;

            }

            public Builder item (StringProperty desc,
                                 Set<Node>      controls,
                                 Item.Type      type)
            {

                this.items.add (new Item (desc,
                                          controls,
                                          type));
                return this;

            }

            public Builder title (List<String> prefix,
                                  String...    ids)
            {

                return this.title (getUILanguageStringProperty (Utils.newList (prefix, ids)));

            }

            public Builder title (String... ids)
            {

                return this.title (getUILanguageStringProperty (ids));

            }

            public Builder title (StringProperty title)
            {

                this.title = title;
                return this;

            }

            public Builder description (List<String> prefix,
                                        String...    ids)
            {

                return this.description (getUILanguageStringProperty (Utils.newList (prefix, ids)));

            }

            public Builder description (String... ids)
            {

                return this.description (getUILanguageStringProperty (ids));

            }

            public Builder description (StringProperty desc)
            {

                this.description = desc;
                return this;

            }

        }

    }

    private void handleBackupsDirChange (Path                     newDir,
                                         AbstractProjectViewer    viewer,
                                         Consumer<StringProperty> error)
    {

        Project proj = viewer.getProject ();

        final Path oldDir = proj.getBackupDirectory ().toPath ();

        // See if the project directory is changing.
        if (!newDir.equals (oldDir))
        {

            if (!Utils.isDirectoryEmpty (newDir))
            {

                error.accept (getUILanguageStringProperty (project,actions,changebackupdir,errors,dirnotempty));
                return;

            }

            try
            {

                if (Files.exists (oldDir))
                {

                    PathUtils.copyDirectory (oldDir,
                                             newDir,
                                             StandardCopyOption.COPY_ATTRIBUTES);

                    Utils.deleteDir (oldDir);

                }

            } catch (Exception e) {

                Environment.logError ("Unable to move backup dir from: " +
                                      oldDir +
                                      "to: " +
                                      newDir,
                                      e);

                ComponentUtils.showErrorMessage (viewer,
                                                 getUILanguageStringProperty (project,actions,changebackupdir,actionerror));

            }

            proj.setBackupDirectory (newDir.toFile ());

            ProjectInfo pi = Environment.getProjectInfo (proj);

            if (pi != null)
            {

                pi.setBackupDirPath (newDir);

            }

            String pid = "backupdirchange";

            QuollPopup.messageBuilder ()
                .removeOnClose (true)
                .popupId (pid)
                .withViewer (viewer)
                .title (project,actions,changebackupdir,confirmpopup,title)
                .message (getUILanguageStringProperty (Arrays.asList (project,actions,changebackupdir,confirmpopup,text),
                                                       newDir.toString ()))
                .button (QuollButton.builder ()
                    .label (buttons,confirm)
                    .buttonType (ButtonBar.ButtonData.APPLY)
                    .onAction (eev ->
                    {

                       this.viewer.getPopupById (pid).close ();

                    })
                    .build ())
                .build ();

        }

    }

    private void handleProjectDirChange (Path                     newParentDir,
                                         AbstractProjectViewer    viewer,
                                         Consumer<StringProperty> error)
    {

        Project proj = viewer.getProject ();

        Path oldProjDir = proj.getProjectDirectory ().toPath ();

        Path newProjDir = newParentDir.resolve (Utils.sanitizeForFilename (proj.getName ()));

        boolean backupIsSubDir = false;

        Path _newBackupDir = null;

        if (Utils.isSubDir (proj.getProjectDirectory (),
                            proj.getBackupDirectory ()))
        {

            backupIsSubDir = true;
            _newBackupDir = newProjDir.resolve (proj.getBackupDirectory ().toPath ().toString ().substring (proj.getProjectDirectory ().toPath ().toString ().length () + 1));

            if (!Utils.isDirectoryEmpty (_newBackupDir))
            {

                error.accept (getUILanguageStringProperty (Arrays.asList (project,actions,changeprojectdir,errors,backupdirnotempty),
                                                           _newBackupDir.toUri ().toString (),
                                                           _newBackupDir.toString ()));

                return;

            }

        }

        Path newBackupDir = _newBackupDir;

        // See if the project directory is changing.
        if (!newProjDir.equals (oldProjDir))
        {

            if (Files.exists (newProjDir))
            {

                error.accept (getUILanguageStringProperty (project,actions,changeprojectdir,errors,dirnotempty));
                return;

            }

            try
            {

                viewer.changeProjectDirectory (newProjDir);

            } catch (Exception e) {

                Environment.logError ("Unable to rename project directory: " +
                                      proj.getProjectDirectory () +
                                      " to: " +
                                      newProjDir,
                                      e);

                ComponentUtils.showErrorMessage (viewer,
                                                 getUILanguageStringProperty (project,actions,changeprojectdir,actionerror));

                //Environment.showAllProjectsViewer ();

            }

            viewer.fireProjectEvent (ProjectEvent.Type.projectobject,
                                     ProjectEvent.Action.changeddirectory,
                                     proj.getObjectType ());

            String pid = "projectdirchange";

            QuollPopup.messageBuilder ()
                .removeOnClose (true)
                .popupId (pid)
                .withViewer (viewer)
                .title (project,actions,changeprojectdir,confirmpopup,title)
                .message (getUILanguageStringProperty (Arrays.asList (project,actions,changeprojectdir,confirmpopup,text),
                                                       newProjDir.toString ()))
                .button (QuollButton.builder ()
                    .label (buttons,confirm)
                    .buttonType (ButtonBar.ButtonData.APPLY)
                    .onAction (eev ->
                    {

                       this.viewer.getPopupById (pid).close ();

                    })
                    .build ())
                .build ();

            if (true)
            {
                return;
            }

            StringProperty extra = new SimpleStringProperty ("");

            if (backupIsSubDir)
            {

                extra = getUILanguageStringProperty (project,actions,changeprojectdir,confirmpopup,backupdirchangewarning);
                //"<span class='error'>Warning!  The backups directory for this {project} will also be changed.</span><br /><br />";

            }

            QuollPopup.questionBuilder ()
                .withViewer (viewer)
                .styleClassName (StyleClassNames.PROJECT)
                .title (project,actions,changeprojectdir,confirmpopup,title)
                .message (getUILanguageStringProperty (Arrays.asList (project,actions,changeprojectdir,confirmpopup,text),
                                                       extra))
                .confirmButtonLabel (project,actions,changeprojectdir,confirmpopup,buttons,confirm)
                .cancelButtonLabel (project,actions,changeprojectdir,confirmpopup,buttons,cancel)
                .onConfirm (ev ->
                {

                    try
                    {

                        viewer.changeProjectDirectory (newProjDir);

                    } catch (Exception e) {

                        Environment.logError ("Unable to rename project directory: " +
                                              proj.getProjectDirectory () +
                                              " to: " +
                                              newProjDir,
                                              e);

                        ComponentUtils.showErrorMessage (viewer,
                                                         getUILanguageStringProperty (project,actions,changeprojectdir,actionerror));

                        //Environment.showAllProjectsViewer ();

                    }

                    Environment.getProjectViewer (proj).fireProjectEvent (ProjectEvent.Type.projectobject,
                                                                          ProjectEvent.Action.changeddirectory,
                                                                          proj.getObjectType ());



/*
                    if (newBackupDir != null)
                    {

                        // Need to change the backup dir first.
                        proj.setBackupDirectory (newBackupDir.toFile ());

                    }

                    viewer.close (true,
                                  () ->
                    {

                        try
                        {

                            Path _newProjDir = Files.move (proj.getProjectDirectory ().toPath (),
                                                           newProjDir);

                            proj.setProjectDirectory (_newProjDir.toFile ());

                        } catch (Exception e) {

                            Environment.logError ("Unable to rename project directory: " +
                                                  proj.getProjectDirectory () +
                                                  " to: " +
                                                  newProjDir,
                                                  e);

                            ComponentUtils.showErrorMessage (getUILanguageStringProperty (project,actions,changeprojectdir,actionerror));
                                                      //"Unable to change project directory, please contact Quoll Writer support for assistance.");

                        }

                        // Open the project again.
                        try
                        {

                            Environment.openProject (proj,
                                                     () ->
                                                     {

                                                        Environment.getProjectViewer (proj).fireProjectEvent (ProjectEvent.Type.projectobject,
                                                                                                              ProjectEvent.Action.changeddirectory,
                                                                                                              proj.getObjectType ());

                                                     });

                        } catch (Exception e)
                        {

                            // Show the projects window.
                            Environment.showAllProjectsViewer ();

                            Environment.logError ("Unable to reopen project: " +
                                                  proj,
                                                  e);

                            ComponentUtils.showErrorMessage (null,
                                                             getUILanguageStringProperty (project,actions,changeprojectdir,errors,reopenproject));
                                                      //"Unable to reopen project, please contact Quoll Writer support for assistance.");

                            return;

                        }

                   });
*/
                })
                .onCancel (ev ->
                {

                    error.accept (null);

                })
                .build ();

        }

    }

}
