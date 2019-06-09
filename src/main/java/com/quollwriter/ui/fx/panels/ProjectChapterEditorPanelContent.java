package com.quollwriter.ui.fx.panels;

/*
import javax.swing.*;
*/
import javax.swing.event.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.geometry.*;

import java.util.*;
import java.util.concurrent.*;

import com.quollwriter.UserProperties;
import com.quollwriter.synonyms.*;
import com.quollwriter.data.*;
import com.quollwriter.Constants;
import com.quollwriter.LanguageStrings;
import com.quollwriter.Environment;
import com.quollwriter.GeneralException;
import com.quollwriter.DictionaryProvider2;
import com.quollwriter.Utils;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.swing.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ProjectChapterEditorPanelContent extends ChapterEditorPanelContent<ProjectViewer, QuollEditorPanel> implements ToolBarSupported
{

    private ScheduledFuture autoSaveTask = null;
    private QuollEditorPanel panel = null;
    private Runnable wordCountUpdate = null;
    private ToolBar toolbar = null;

    public ProjectChapterEditorPanelContent (ProjectViewer viewer,
                                             Chapter       chapter)
                                      throws GeneralException
    {

        super (viewer,
               chapter);

        final ProjectChapterEditorPanelContent _this = this;

        this.panel = new QuollEditorPanel (viewer,
                                           chapter);

        DictionaryProvider2 dp = null;

          try
          {

              dp = viewer.getDictionaryProvider ();

          } catch (Exception e)
          {

              throw new GeneralException ("Unable to get dictionary provider.",
                                          e);

          }

          SynonymProvider sp = null;

          try
          {

              sp = viewer.getSynonymProvider ();

          } catch (Exception e)
          {

              throw new GeneralException ("Unable to get synonym provider.",
                                          e);

          }

          this.panel.setSynonymProvider (sp);//Environment.getSynonymProvider ());

          viewer.spellCheckingEnabledProperty ().addListener ((v, oldv, newv) ->
          {

              _this.panel.setSpellCheckingEnabled (newv);

          });

          viewer.projectSpellCheckLanguageProperty ().addListener ((v, oldv, newv) ->
          {

              _this.panel.setDictionaryProvider (viewer.getDictionaryProvider ());

              try
              {

                  _this.panel.setSynonymProvider (viewer.getSynonymProvider ());

              } catch (Exception e) {

                  Environment.logError ("Unable to set synonym provider.",
                                        e);

                  // TODO Should error.

              }


          });

          this.panel.getEditor ().getDocument ().addDocumentListener (new DocumentListener ()
          {

              @Override
              public void insertUpdate (DocumentEvent ev)
              {

                  _this.scheduleWordCountUpdate ();

              }

              @Override
              public void changedUpdate (DocumentEvent ev)
              {

                  _this.scheduleWordCountUpdate ();

              }

              @Override
              public void removeUpdate (DocumentEvent ev)
              {

                  _this.scheduleWordCountUpdate ();

              }


          });

        UserProperties.chapterAutoSaveEnabledProperty ().addListener ((v, oldv, newv) ->
        {

           _this.tryScheduleAutoSave ();

        });

        UserProperties.chapterAutoSaveTimeProperty ().addListener ((v, oldv, newv) ->
        {

           _this.tryScheduleAutoSave ();

        });

    }

    @Override
    public ToolBar getToolBar ()
    {

        if (this.toolbar == null)
        {

            List<String> prefix = Arrays.asList (project,editorpanel,LanguageStrings.toolbar);

            ToolBar t = new ToolBar ();

            t.getItems ().add (QuollButton.builder ()
                .tooltip (Utils.newList (prefix,save,tooltip))
                .styleClassName (StyleClassNames.SAVE)
                .onAction (ev ->
                {

                    this.saveObject ();

                })
                .build ());

            t.getItems ().add (QuollMenuButton.builder ()
                .tooltip (Utils.newList (prefix,_new,tooltip))
                .styleClassName (StyleClassNames.NEW)
                .items (() ->
                {

                    List<String> mprefix = Arrays.asList (project,editorpanel,popupmenu,_new,items);

                    Set<MenuItem> items = new LinkedHashSet<> ();

                    /*
                    mi.setMnemonic ('S');
                    mi.setToolTipText (pref + "S");
                    */
                    String pref = getUILanguageStringProperty (general,shortcutprefix).getValue ();

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (mprefix, com.quollwriter.data.Scene.OBJECT_TYPE,text)))
                        .styleClassName (com.quollwriter.data.Scene.OBJECT_TYPE)
                        //.tooltip (new SimpleStringProperty (pref + "S"))
                        .onAction (eev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.newscene,
                                                    this.object);

                        })
                        .build ());

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (mprefix, OutlineItem.OBJECT_TYPE,text)))
                        .styleClassName (OutlineItem.OBJECT_TYPE)
                        //.tooltip (new SimpleStringProperty (pref + "O"))
                        .onAction (eev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.newoutlineitem,
                                                    this.object);

                        })
                        .build ());

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (mprefix, Note.OBJECT_TYPE,text)))
                        .styleClassName (Note.OBJECT_TYPE)
                        //.tooltip (new SimpleStringProperty (pref + "N"))
                        .onAction (eev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.newnote,
                                                    this.object);

                        })
                        .build ());

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (mprefix, Note.EDIT_NEEDED_OBJECT_TYPE,text)))
                        .styleClassName (StyleClassNames.EDITNEEDEDNOTE)
                        .onAction (eev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.neweditneedednote,
                                                    this.object);

                        })
                        .build ());

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (mprefix, Chapter.OBJECT_TYPE,text)))
                        .styleClassName (Chapter.OBJECT_TYPE)
                        .accelerator (new KeyCharacterCombination ("E",
                                                                   KeyCombination.SHORTCUT_DOWN,
                                                                   KeyCombination.SHIFT_DOWN))
                        .onAction (eev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.newchapter,
                                                    this.object);

                        })
                        .build ());

                    items.addAll (UIUtils.getNewAssetMenuItems (this.viewer));

                    return items;

                })
                .build ());

            t.getItems ().add (QuollButton.builder ()
                .tooltip (Utils.newList (prefix,showchapterinfo,tooltip))
                .styleClassName (StyleClassNames.INFO)
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectViewer.CommandId.showchapterinfo,
                                            this.object);

                })
                .build ());

            t.getItems ().add (QuollButton.builder ()
                .tooltip (Utils.newList (prefix,wordcount,tooltip))
                .styleClassName (StyleClassNames.WORDCOUNT)
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectViewer.CommandId.showwordcounts);

                })
                .build ());

            QuollButton sb = QuollButton.builder ()
                .tooltip (Utils.newList (prefix,this.viewer.isSpellCheckingEnabled () ? spellcheckoff : spellcheckon,tooltip))
                .styleClassName (StyleClassNames.SPELLCHECK)
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectViewer.CommandId.togglespellchecking);

                })
                .build ();

            this.viewer.spellCheckingEnabledProperty ().addListener ((pr, oldv, newv) ->
            {

                sb.pseudoClassStateChanged (StyleClassNames.ENABLED_PSEUDO_CLASS, this.viewer.isSpellCheckingEnabled ());
                sb.pseudoClassStateChanged (StyleClassNames.DISABLED_PSEUDO_CLASS, !this.viewer.isSpellCheckingEnabled ());

                UIUtils.setTooltip (sb,
                                    getUILanguageStringProperty (Utils.newList (prefix,this.viewer.isSpellCheckingEnabled () ? spellcheckoff : spellcheckon,tooltip)));

            });

            sb.pseudoClassStateChanged (StyleClassNames.ENABLED_PSEUDO_CLASS, this.viewer.isSpellCheckingEnabled ());
            sb.pseudoClassStateChanged (StyleClassNames.DISABLED_PSEUDO_CLASS, !this.viewer.isSpellCheckingEnabled ());

            t.getItems ().add (sb);

            t.getItems ().add (QuollButton.builder ()
                .tooltip (Utils.newList (prefix,delete,tooltip))
                .styleClassName (StyleClassNames.DELETE)
                .onAction (ev ->
                {

                    this.viewer.runCommand (ProjectViewer.CommandId.deletechapter,
                                            this.object);

                })
                .build ());

            t.getItems ().add (QuollMenuButton.builder ()
                .tooltip (Utils.newList (prefix,tools,tooltip))
                .styleClassName (StyleClassNames.TOOLS)
                .items (() ->
                {

                    Set<MenuItem> items = new LinkedHashSet<> ();

                    List<String> mprefix = Arrays.asList (project,editorpanel,tools);

                    if (this.viewer.isProjectLanguageEnglish ())
                    {

                        items.add (QuollMenuItem.builder ()
                            .label (getUILanguageStringProperty (Utils.newList (mprefix,problemfinder,text)))
                            .styleClassName (StyleClassNames.PROBLEMFINDER)
                            .accelerator (new KeyCharacterCombination ("P",
                                                                       KeyCombination.SHORTCUT_DOWN,
                                                                       KeyCombination.SHIFT_DOWN))
                            .onAction (ev ->
                            {

                                this.showProblemFinder ();

                            })
                            .build ());

                    }

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (mprefix,textproperties,text)))
                        .styleClassName (StyleClassNames.EDITPROPERTIES)
                        .accelerator (new KeyCharacterCombination ("E",
                                                                   KeyCombination.SHORTCUT_DOWN))
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.textproperties);

                        })
                        .build ());

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (mprefix,find,text)))
                        .styleClassName (StyleClassNames.FIND)
                        .accelerator (new KeyCharacterCombination ("F",
                                                                   KeyCombination.SHORTCUT_DOWN))
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.find);

                        })
                        .build ());

                    items.add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (mprefix,print,text)))
                        .styleClassName (StyleClassNames.PRINT)
                        .accelerator (new KeyCharacterCombination ("P",
                                                                   KeyCombination.SHORTCUT_DOWN))
                        .onAction (ev ->
                        {

                            this.viewer.runCommand (ProjectViewer.CommandId.print,
                                                    this.object);

                        })
                        .build ());

                    return items;

                })
                .build ());

            this.toolbar = t;

        }

        return this.toolbar;

    }

    private void showProblemFinder ()
    {

        this.panel.showProblemFinder ();

    }

    @Override
    public void saveObject ()
    {

        try
        {

            this.object.setText (this.panel.getEditor ().getTextWithMarkup ());

            super.saveObject ();

        } catch (Exception e) {

            Environment.logError ("Unable to save chapter: " + this.object,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (project,editorpanel,actions,save,actionerror));

        }

    }

    private void scheduleWordCountUpdate ()
    {

        final ProjectChapterEditorPanelContent _this = this;

        if (this.wordCountUpdate != null)
        {

            return;

        }

        this.wordCountUpdate = () ->
        {

            try
            {

                _this.viewer.updateChapterCounts (_this.object);

                _this.wordCountUpdate = null;

            } catch (Exception e) {

                Environment.logError ("Unable to determine word count for chapter: " +
                                      _this.object,
                                      e);

            }

        };

        this.viewer.schedule (this.wordCountUpdate,
                              1 * 1000,
                              -1);

    }

    @Override
    public QuollEditorPanel getChapterPanel ()
    {

        return this.panel;

    }

    public void showItem (ChapterItem item)
    {

        this.panel.showItem (item);

    }

    public void editItem (ChapterItem item)
    {

        // TODO

    }

    private void tryScheduleAutoSave ()
    {

        if (this.autoSaveTask != null)
        {

            this.autoSaveTask.cancel (true);

        }

        if (UserProperties.chapterAutoSaveEnabledProperty ().getValue ())
        {

            final long autoSaveInt = UserProperties.chapterAutoSaveTimeProperty ().getValue ();

            if (autoSaveInt > 0)
            {

				final ProjectChapterEditorPanelContent _this = this;

                this.autoSaveTask = this.viewer.schedule (() ->
                {

                    if (!_this.unsavedChangesProperty ().getValue ())
                    {

                        return;

                    }

                    try
                    {

                        _this.saveObject ();

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to auto save chapter: " +
                                              _this.object,
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (project,editorpanel,actions,autosave,actionerror));

                    }

                },
                autoSaveInt,
                autoSaveInt);

            }

        }

    }

}
