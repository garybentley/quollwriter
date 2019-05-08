package com.quollwriter.ui.fx.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

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

import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.LanguageStrings.*;

public class ProjectChapterEditorPanelContent extends ChapterEditorPanelContent<ProjectViewer, QuollEditorPanel>
{

    private ScheduledFuture autoSaveTask = null;
    private QuollEditorPanel panel = null;
    private Runnable wordCountUpdate = null;

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

                        _this.viewer.saveObject (_this.object,
                                                 true);

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to auto save chapter: " +
                                              _this.object,
                                              e);

                    }

                },
                autoSaveInt,
                autoSaveInt);

            }

        }

    }

}
