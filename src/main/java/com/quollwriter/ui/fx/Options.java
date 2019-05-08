package com.quollwriter.ui.fx;

import java.net.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

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

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.uistrings.UILanguageStringsManager;
import com.quollwriter.uistrings.UILanguageStrings;
import com.quollwriter.uistrings.UILanguageStringsInfo;
import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class Options extends VBox
{

    private static final String PROJECTITEMPREVIEW_POPUP_ID = "projectitempreview";
    private static final String LAYOUT_POPUP_ID = "layoutselector";

    private AbstractViewer viewer = null;

    public Options (AbstractViewer viewer,
                    Section.Id...  sects)
    {

        this.viewer = viewer;

        for (Section.Id id : sects)
        {

            Section s = this.createSection (id,
                                            viewer);

            if (s != null)
            {

                this.getChildren ().add (s);

            }

        }

    }

    private Section createSection (Section.Id     id,
                                   AbstractViewer viewer)
    {

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

            return this.createEditingSection (viewer);

        }

        return null;

    }

    private <T> void addChangeListener (String propName,
                                        ObservableValue<T> value)
    {

    }

    private Node createLayoutSelector ()
    {

        final Options _this = this;

        String currLayout = UserProperties.get (Constants.UI_LAYOUT_PROPERTY_NAME);

        Label l = new Label ();
        l.setGraphic (this.getLayoutPanel (currLayout));

        UserProperties.uiLayoutProperty ().addListener ((pr, oldv, newv) ->
        {

            l.setGraphic (this.getLayoutPanel (newv));

        });

        l.setOnMouseClicked (ev ->
        {

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
                .popupId (LAYOUT_POPUP_ID)
                .content (b)
                .withClose (true)
                .withViewer (_this.viewer)
                .hideOnEscape (true)
                .removeOnClose (true)
                .show ()
                .build ();

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

                hb.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, currLayout.equals (lt));

                UIUtils.setTooltip (hb,
                                    getUILanguageStringProperty (options,lookandsound,labels,interfacelayout,popup,tooltip));

                BasicHtmlTextFlow text = BasicHtmlTextFlow.builder ()
                    .styleClassName (StyleClassNames.DESCRIPTION)
                    .text (getUILanguageStringProperty (options,lookandsound,interfacelayouts,lt))
                    .withViewer (_this.viewer)
                    .build ();
                HBox.setHgrow (text,
                               Priority.ALWAYS);

                hb.getChildren ().add (text);
                hb.setUserData (lt);

                hb.setOnMouseClicked (eev ->
                {

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

    private Section createEditingSection (AbstractViewer viewer)
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

        autosaveAmount.getSelectionModel ().select (new Integer (UserProperties.chapterAutoSaveTimeProperty ().getValue () / Constants.MIN_IN_MILLIS));
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

            UIUtils.downloadDictionaryFiles (lang,
                                             this.viewer,
                                             () ->
                                             {

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

                                                     // TODO
                                                     // Add a notification that the files have been downloaded.
                                                     this.viewer.addNotification (getUILanguageStringProperty (Arrays.asList (options,editingchapters,downloaddictionaryfiles,notification,text),
                                                                                 //"The language files for <b>%s</b> have been downloaded and the project language set.",
                                                                                                               lang),
                                                                                  StyleClassNames.INFORMATION,
                                                                                  -1);

                                                 }

                                              });

        };

        Node downloadFiles = QuollHyperlink.builder ()
            .label (options,editingchapters,labels,downloadlanguagefiles)
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

            final String currLang = def;

            if (UILanguageStrings.isEnglish (def))
            {

                def = Constants.ENGLISH;

            }

            defLang.setSelected (def.equals (lang));

            if (this.viewer instanceof AbstractProjectViewer)
            {

                AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

                def = pv.getProjectSpellCheckLanguage ();

            }

            if ((!UILanguageStrings.isEnglish (lang))
                &&
                (!def.equals (lang))
               )
            {

                ComponentUtils.showMessage (this.viewer,
                                            getUILanguageStringProperty (options,editingchapters,labels,nonenglishwarning));
                                     //"Please note: when changing the spell check language to something other<br />than English the following features will be disabled:<ul><li>Synonym lookups</li><li>The Problem Finder</li><li>Readability Indices</li></ul>");

            }

            downloadFiles.setVisible (false);

            // Check to see if the files are available.
            try
            {

                if (!DictionaryProvider.isLanguageInstalled (lang))
                {

                    downloadFiles.setVisible (true);

                    List<String> prefix = Arrays.asList (options,editingchapters,downloaddictionaryfiles,popup);

                    ComponentUtils.createQuestionPopup (getUILanguageStringProperty (Utils.newList (prefix,title)),
                                                        StyleClassNames.DOWNLOAD,
                                                        //"Download dictionary files?",
                                                        getUILanguageStringProperty (Utils.newList (prefix,text),
                                                                                     lang),
                                                        getUILanguageStringProperty (Utils.newList (prefix,buttons,confirm)),
                                                        getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)),
                                                        // On confirm
                                                        ev ->
                                                        {

                                                            downloadDictFiles.accept (lang);

                                                        },
                                                        // On cancel
                                                        ev ->
                                                        {

                                                            spellcheckLang.getSelectionModel ().select (currLang);

                                                        },
                                                        null,
                                                        this.viewer);

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

                    spellcheckLang.getSelectionModel ().select (pv.getProject ().getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME));

                } else {

                    spellcheckLang.getSelectionModel ().select (def);

                }

                spellcheckLang.setDisable (false);

                boolean isDef = def.equals (spellcheckLang.valueProperty ().getValue ());

                defLang.setSelected (isDef);

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

        Section s = Section.builder ()
            .styleClassName (StyleClassNames.EDITING)
            .title (options,editingchapters,title)
            .description (options,editingchapters,text)
            .mainItem (enableAutosave)
            .mainItem (getUILanguageStringProperty (options,editingchapters,labels,autosavewhen),
                       autosaveAmount)
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
                           .onAction (ev ->
                           {

                               String pid = "chapter-icons-example-popup";

                               QuollPopup qp = this.viewer.getPopupById (pid);

                               if (qp != null)
                               {

                                   qp.toFront ();
                                   return;

                               }

                               qp = ComponentUtils.showMessage (this.viewer,
                                                                StyleClassNames.CHAPTERICONSEXAMPLE,
                                                                getUILanguageStringProperty (names,example),
                                                                new ImageView ());
                               qp.setPopupId (pid);

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
                          .onAction (ev ->
                          {

                              String pid = "edit-position-example-popup";

                              QuollPopup qp = this.viewer.getPopupById (pid);

                              if (qp != null)
                              {

                                  qp.toFront ();
                                  return;

                              }

                              qp = ComponentUtils.showMessage (this.viewer,
                                                               StyleClassNames.EDITPOSITIONEXAMPLE,
                                                               getUILanguageStringProperty (names,example),
                                                               new ImageView ());
                              qp.setPopupId (pid);

                          })
                          .build ())
            .mainItem (QuollCheckBox.builder ()
                .label (options,editingchapters,labels,seteditcompleteatchapterend)
                .userProperty (Constants.SET_CHAPTER_AS_EDIT_COMPLETE_WHEN_EDIT_POSITION_IS_AT_END_OF_CHAPTER_PROPERTY_NAME)
                .build ())
            .mainItem (compressChapterContextMenu)
            .subItem (QuollHyperlink.builder ()
                .label (actions,viewexample)
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

                    qp = ComponentUtils.showMessage (this.viewer,
                                                     StyleClassNames.COMPRESSCHAPTERCONTEXTEXAMPLE,
                                                     getUILanguageStringProperty (names,example),
                                                     iv);
                    qp.setPopupId (pid);

                })
                .build ())
            .mainItem (getUILanguageStringProperty (options,editingchapters,labels,setspellcheckerlanguage),
                       spellcheckLang)
            .subItem (defLang)
            // TODO Is this still needed? .subItem (downloadFiles)
            .mainItem (QuollButton.builder ()
                .label (getUILanguageStringProperty (options,editingchapters,labels,managedictionary))
                .onAction (ev ->
                {

                    QuollPopup qp = this.viewer.getPopupById (DictionaryManager.POPUP_ID);

                    if (qp != null)
                    {

                        qp.toFront ();
                        return;

                    }

                    try
                    {

                        new DictionaryManager (this.viewer,
                                               new UserDictionaryProvider ()).show ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to show dictionary manager",
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (dictionary,manage,actionerror));

                    }

                })
                .build ())
            .build ();

        return s;

    }

    private Section createNamingSection (AbstractViewer viewer)
    {

        Section s = Section.builder ()
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

            String uid = newv.id;

            feedbackB.setVisible ((!UILanguageStrings.isEnglish (uid)) && (!uid.startsWith ("user-")));

            if (uid.equals (UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME)))
            {

                return;

            }

            UILanguageStrings ls = null;

            try
            {

                ls = UILanguageStringsManager.getUILanguageStrings (uid);

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
/*
TODO Remove NO longer needed.
                    ComponentUtils.showMessage (viewer,
                                                getUILanguageStringProperty (uilanguage,set,restartwarning,title),
                                                getUILanguageStringProperty (uilanguage,set,restartwarning,text));
*/
                } catch (Exception e) {

                    Environment.logError ("Unable to set ui language to: " + uid,
                                          e);

                    ComponentUtils.showErrorMessage (viewer,
                                                     getUILanguageStringProperty (uilanguage,set,actionerror));

                }

            };

            if (ls == null)
            {

                ComponentUtils.showMessage (viewer,
                                            getUILanguageStringProperty (uilanguage,set,downloading,title),
                                            getUILanguageStringProperty (uilanguage,set,downloading,text));

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
                                                                    item.languageName,
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

                        if (in.id.equals (sel))
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

        QuollCheckBox showPreviewCB = QuollCheckBox.builder ()
            .label (options,lookandsound,labels,showpreview)
            .userProperty (Constants.SHOW_QUICK_OBJECT_PREVIEW_IN_PROJECT_SIDEBAR_PROPERTY_NAME)
            .build ();

        QuollButton changeDisplayBut = QuollButton.builder ()
            .label (options,lookandsound,labels,changedisplay)
            .onAction (ev ->
            {

                QuollPopup qp = this.viewer.getPopupById (ChangeProjectItemPreviewDisplayPopup.POPUP_ID);

                if (qp != null)
                {

                    qp.toFront ();
                    return;

                }

                new ChangeProjectItemPreviewDisplayPopup (this.viewer).show ();

            })
            .build ();

        changeDisplayBut.setDisable (!showPreviewCB.isSelected ());

        showPreviewCB.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            changeDisplayBut.setDisable (!newv);

        });

        boolean soundSel = UserProperties.getAsBoolean (Constants.PLAY_SOUND_ON_KEY_STROKE_PROPERTY_NAME);

        QuollButton playSoundB = QuollButton.builder ()
            .label (options,lookandsound,labels,playtypewritersound,buttons,playsound)
            .onAction (ev ->
            {

                Environment.playKeyStrokeSound ();

            })
            .build ();

        QuollCheckBox playSoundCB = QuollCheckBox.builder ()
            .selected (soundSel)
            .label (options,lookandsound,labels,playtypewritersound,text)
            .build ();

        QuollFileField ownSoundF = QuollFileField.builder ()
            .showClear (true)
            .limitTo (QuollFileField.Type.file)
            .styleClassName (StyleClassNames.OWNSOUND)
            .withViewer (this.viewer)
            .fileExtensionFilter (getUILanguageStringProperty (options,lookandsound,labels,playtypewritersound,finder,filter),
                                  "wav")
            .chooserTitle (getUILanguageStringProperty (options,lookandsound,labels,playtypewritersound,finder,title))
            .findButtonTooltip (getUILanguageStringProperty (options,lookandsound,labels,playtypewritersound,finder,tooltip))
            .initialFile (UserProperties.getAsFile (Constants.KEY_STROKE_SOUND_FILE_PROPERTY_NAME))
            .build ();

        ownSoundF.fileProperty ().addListener ((pr, oldv, newv) ->
        {

            Environment.setKeyStrokeSoundFilePath ((newv != null ? newv.toPath () : null));

        });

        playSoundB.setDisable (!soundSel);
        ownSoundF.setDisable (!soundSel);

        playSoundCB.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            playSoundB.setDisable (!playSoundCB.isSelected ());
            ownSoundF.setDisable (!playSoundCB.isSelected ());

        });

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

        textSize.setValue (UserProperties.getUIBaseFontSize ());
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

        UserProperties.uiBaseFontSizeProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv.floatValue () != textSize.valueProperty ().floatValue ())
            {

                textSize.setValue (newv.floatValue ());

            }

        });

        Section s = Section.builder ()
            .styleClassName (StyleClassNames.LOOKS)
            .title (options,lookandsound,title)
            .description (options,lookandsound,text)
            .mainItem (getUILanguageStringProperty (options,lookandsound,labels,uilanguage),
                       uilangb)
            .mainItem (QuollCheckBox.builder ()
                .label (options,lookandsound,labels,keepprojectswindowsopen)
                .userProperty (Constants.KEEP_PROJECTS_WINDOW_WHEN_PROJECT_OPENED_PROPERTY_NAME)
                .build ())
            .mainItem (QuollCheckBox.builder ()
                .label (options,lookandsound,labels,showprojectswindownoopenproject)
                .userProperty (Constants.SHOW_PROJECTS_WINDOW_WHEN_NO_OPEN_PROJECTS_PROPERTY_NAME)
                .build ())
            .mainItem (showPreviewCB)
            .subItem (changeDisplayBut)
            .mainItem (QuollCheckBox.builder ()
                .label (options,lookandsound,labels,shownotes)
                .userProperty (Constants.SHOW_NOTES_IN_CHAPTER_LIST_PROPERTY_NAME)
                .build ())
            .mainItem (getUILanguageStringProperty (options,lookandsound,labels,basefontsize),
                       thb)
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

                              UserProperties.set (Constants.TOOLBAR_LOCATION_PROPERTY_NAME,
                                                  ind == 0 ? Constants.TOP : Constants.BOTTOM);

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
                                        .selected (UserProperties.getAsBoolean (Constants.SHOW_EACH_CHAPTER_FIND_RESULT_PROPERTY_NAME))
                                        .build ())
                            .build ())
            .mainItem (playSoundCB)
            .subItem (getUILanguageStringProperty (options,lookandsound,labels,playtypewritersound,selectownwavfile),
                      ownSoundF)
            .subItem (playSoundB)
            .mainItem (QuollCheckBox.builder ()
                .label (options,lookandsound,labels,highlightdividers,text)
                .userProperty (Constants.HIGHLIGHT_SPLITPANE_DIVIDERS_PROPERTY_NAME)
                .build ())
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
            landing ("landing"),
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

        private Section (Builder b)
        {

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

            BasicHtmlTextFlow desc = BasicHtmlTextFlow.builder ()
                .text (b.description)
                .build ();
            desc.managedProperty ().bind (desc.visibleProperty ());

            VBox c = new VBox ();
            c.managedProperty ().bind (c.visibleProperty ());

            // Build the section.
            for (Item it : b.items)
            {

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

            this.acc = AccordionItem.builder ()
                .title (b.title)
                .openContent (c)
                .closedContent (desc)
                .styleClassName (b.styleName)
                .build ();

            this.getChildren ().add (this.acc);

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
                sub
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

}
