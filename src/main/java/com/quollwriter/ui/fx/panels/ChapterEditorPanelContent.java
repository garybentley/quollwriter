package com.quollwriter.ui.fx.panels;

import java.util.*;
import java.util.concurrent.*;

import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import org.fxmisc.flowless.*;
import org.fxmisc.wellbehaved.event.*;

import com.quollwriter.synonyms.*;
import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.swing.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

/**
 * A base class for content that is suitable for display within a panel for a specific named object.
 */
//public abstract class ChapterEditorPanelContent<E extends AbstractProjectViewer, P extends javax.swing.JComponent> extends NamedObjectPanelContent<E, Chapter>
public abstract class ChapterEditorPanelContent<E extends AbstractProjectViewer> extends NamedObjectPanelContent<E, Chapter>
{

    private ScheduledFuture autoSaveTask = null;
    //private QuollEditorPanel panel = null;
    private Runnable wordCountUpdate = null;
    private ScheduledFuture a4PageCountUpdater = null;

    protected List<QuollPopup> popupsToCloseOnClick = new ArrayList<> ();
    private Map<String, ContextMenu> contextMenus = new HashMap<> ();

    //private P chapterPanel = null;
    protected TextEditor editor = null;
    private VirtualizedScrollPane<TextEditor> scrollPane = null;
    protected javafx.beans.property.StringProperty selectedTextProp = null;
    private long textLastModifiedTime = 0;

    public ChapterEditorPanelContent (E              viewer,
                                      Chapter        chapter,
                                      TextProperties props)
                               throws GeneralException
    {

        super (viewer,
               chapter);

        this.textLastModifiedTime = System.currentTimeMillis ();

        this.selectedTextProp = new SimpleStringProperty ();

        props.backgroundColorProperty ().addListener ((pr, oldv, newv) ->
        {

            try
            {

                this.getBackgroundPane ().setBackgroundObject (newv);

            } catch (Exception e) {

                Environment.logError ("Unable to set background object to: " + newv + " for panel: " + this.getPanel ().getPanelId (),
                                      e);

            }

        });

        try
        {

            this.editor = TextEditor.builder ()
                //.text (chapter.getText ())
                .textProperties (props)
                .dictionaryProvider (viewer.getDictionaryProvider ())
                .synonymProvider (viewer.getSynonymProvider ())
                .formattingEnabled (true)
                .build ();

        } catch (Exception e) {

            throw new GeneralException ("Unable to create editor panel for chapter: " +
                                        chapter,
                                        e);

        }

        Nodes.addInputMap (this.editor,
                           InputMap.process (EventPattern.mousePressed (),
                                             ev ->
                                             {

                                                this.hidePopups ();

                                                // TODO Handle right click on chapter items in margin.
                                                if (ev.isPopupTrigger ())
                                                {

                                                    this.setContextMenu ();

                                                    return InputHandler.Result.PROCEED;

                                                }

                                                return InputHandler.Result.PROCEED;

                                            }));

         Nodes.addInputMap (this.editor,
                            InputMap.process (EventPattern.mouseReleased (),
                                               ev ->
                                               {

                                                   if (ev.isPopupTrigger ())
                                                   {

                                                       this.setContextMenu ();

                                                       return InputHandler.Result.PROCEED;

                                                   }

                                                   return InputHandler.Result.PROCEED;

                                               }));

        Nodes.addInputMap (this.editor,
                           InputMap.process (EventPattern.keyPressed (),
                                             ev ->
                                             {

                                                 this.hidePopups ();

                                                 return InputHandler.Result.PROCEED;

                                             }));

        this.editor.readyForUseProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv)
            {

                this.setReadyForUse ();

            }

        });

        this.editor.getContent ().multiRichChanges ().subscribe (changes ->
        {

            if (this.editor.isIgnoreDocumentChanges ())
            {

                return;

            }

            this.textLastModifiedTime = System.currentTimeMillis ();

            this.setHasUnsavedChanges (true);

            this.viewer.scheduleUpdateChapterCounts (this.object);

        });

        this.editor.selectedTextProperty ().addListener ((pr, oldv, newv) ->
        {

            this.selectedTextProp.setValue (this.editor.getSelectedText ());

        });

        this.addChangeListener (this.viewer.projectSpellCheckLanguageProperty (),
                                (v, oldv, newv) ->
        {

           this.editor.setDictionaryProvider (this.viewer.getDictionaryProvider ());

           try
           {

              this.editor.setSynonymProvider (viewer.getSynonymProvider ());

           } catch (Exception e) {

               Environment.logError ("Unable to set synonym provider.",
                                     e);

              // TODO Should error.

           }

        });

        this.addChangeListener (this.viewer.spellCheckingEnabledProperty (),
                                (pr, oldv, newv) ->
        {

            this.editor.setSpellCheckEnabled (newv);

        });

        Nodes.addInputMap (this.editor,
                           InputMap.consume (EventPattern.keyPressed (KeyCode.S, KeyCombination.SHORTCUT_DOWN),
                                             ev ->
                                             {

                                                 try
                                                 {

                                                     this.saveObject ();

                                                 } catch (Exception e)
                                                 {

                                                     Environment.logError ("Unable to save chapter: " + this.object,
                                                                           e);

                                                     ComponentUtils.showErrorMessage (this.viewer,
                                                                                      getUILanguageStringProperty (editorpanel,actions,LanguageStrings.save,actionerror));

                                                 }

                                             }));

       Nodes.addInputMap (this.editor,
                          InputMap.process (EventPattern.keyPressed (),
                                            ev ->
                                            {

                                                // TODO Maybe whitelist this...

                                                if (!UserProperties.isPlaySoundOnKeyStroke ())
                                                {

                                                    return InputHandler.Result.PROCEED;

                                                }

                                                KeyCode kc = ev.getCode ();

                                                if ((kc == KeyCode.ESCAPE)
                                                    ||
                                                    (kc.isFunctionKey ())
                                                    ||
                                                    (kc.isNavigationKey ())
                                                    ||
                                                    (kc.isModifierKey ())
                                                    ||
                                                    (kc.isArrowKey ())
                                                    ||
                                                    (kc.isMediaKey ())
                                                   )
                                                {

                                                    return InputHandler.Result.PROCEED;

                                                }

                                                if ((ev.isShortcutDown ())
                                                    ||
                                                    (ev.isMetaDown ())
                                                    ||
                                                    (ev.isControlDown ())
                                                    ||
                                                    (ev.isAltDown ())
                                                   )
                                                {

                                                    return InputHandler.Result.PROCEED;

                                                }

                                                UserProperties.playKeyStrokeSound ();

                                                return InputHandler.Result.PROCEED;

                                            }));

        Nodes.addInputMap (this.editor,
                           InputMap.process (EventPattern.mouseEntered (),
                                             ev ->
                                             {

                                                  //this.editor.requestFocus ();

                                                  return InputHandler.Result.PROCEED;

                                             }));

        this.setText (chapter.getText ());

        this.addChangeListener (UserProperties.chapterAutoSaveEnabledProperty (),
                                (v, oldv, newv) ->
        {

           this.tryScheduleAutoSave ();

        });

        this.addChangeListener (UserProperties.chapterAutoSaveTimeProperty (),
                                (v, oldv, newv) ->
        {

           this.tryScheduleAutoSave ();

        });

        this.tryScheduleAutoSave ();

    }

    private void setContextMenu ()
    {

        if (this.editor.getProperties ().get ("context-menu") != null)
        {

            ((ContextMenu) this.editor.getProperties ().get ("context-menu")).hide ();

        }

        ContextMenu cm = new ContextMenu ();

        boolean compress = UserProperties.getAsBoolean (Constants.COMPRESS_CHAPTER_CONTEXT_MENU_PROPERTY_NAME);

        cm.getItems ().addAll (this.getContextMenuItems (compress));

        this.editor.setContextMenu (cm);

        this.editor.getProperties ().put ("context-menu", cm);
        cm.setAutoFix (true);
        cm.setAutoHide (true);
        cm.setHideOnEscape (true);

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

				final ChapterEditorPanelContent _this = this;

                this.autoSaveTask = this.viewer.schedule (() ->
                {

                    UIUtils.runLater (() ->
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

                    });

                },
                autoSaveInt,
                autoSaveInt);

            }

        }

    }

    public long getTextLastModifiedTime ()
    {

        return this.textLastModifiedTime;

    }

    public void hideContextMenu ()
    {

        this.editor.hideContextMenu ();

    }

    public abstract Set<MenuItem> getContextMenuItems (boolean    compress);

    public abstract Map<KeyCombination, Runnable> getActionMappings ();

    public void setText (StringWithMarkup t)
    {

        this.editor.setText (t);
/*
        if (this.viewer.getViewer ().isShowing ())
        {

            this.editor.setText (t);

        } else {

            this.viewer.getViewer ().addEventHandler (WindowEvent.WINDOW_SHOWN,
                                                      ev ->
            {

                this.editor.setText (t);

            });

        }
*/
    }

    public IndexRange getSelection ()
    {

        return this.editor.getSelection ();

    }

    public void scrollToTextPosition (int      pos,
                                      Runnable afterScroll)
    {

        Bounds b = this.editor.getBoundsForPosition (pos);

        if (b == null)
        {

            int p = this.editor.getParagraphForOffset (pos);

            this.editor.showParagraphInViewport (p);

            UIUtils.forceRunLater (() ->
            {

                this.scrollToTextPosition (pos,
                                           afterScroll);

            });

            return;

            //throw new IllegalArgumentException ("Position: " + pos + ", is not valid.");

        }

        Bounds eb = this.editor.localToScreen (this.editor.getBoundsInLocal ());

        double diff = b.getMinY () - eb.getMinY () - (eb.getHeight () / 2);
        this.editor.scrollYBy (diff);

        UIUtils.forceRunLater (afterScroll);

    }

    @Override
    public void dispose ()
    {

        this.editor.dispose ();
        super.dispose ();

    }

    @Override
    public void requestFocus ()
    {

        //this.editor.requestFocus ();

    }

    public boolean isPositionAtTextEnd (int p)
    {

        return this.editor.isPositionAtTextEnd (p);

    }

    public void insertText (int    pos,
                            String text)
    {

        this.editor.insertText (pos,
                                text);

    }

    public String getSelectedText ()
    {

        return this.editor.getSelectedText ();

    }

    public StringProperty selectedTextProperty ()
    {

        return this.selectedTextProp;

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        final ChapterEditorPanelContent _this = this;

        Runnable r = () ->
        {

            if (s != null)
            {

                Float f = s.getAsFloat (Constants.LAST_EDITOR_SCROLL_POSITION_PROPERTY_NAME,
                                        0f);
                Float vc = s.getAsFloat (Constants.LAST_EDITOR_SCROLL_OFFSET_PROPERTY_NAME, 0f);
                Integer p = s.getAsInt (Constants.LAST_EDITOR_VISIBLE_PARAGRAPH_PROPERTY_NAME);

                if (p != null)
                {

                    this.editor.showParagraphAtTop (p);

                    UIUtils.forceRunLater (() ->
                    {

                        this.editor.scrollYBy (vc);

                    });

                }

                Integer i = s.getAsInt (Constants.LAST_EDITOR_CARET_POSITION_PROPERTY_NAME,
                                        0);

                //this.editor.getCaretSelectionBind ().moveTo (i);

                //this.editor.requestFollowCaret ();

            }

            //this.editor.requestFollowCaret ();

            this.editor.setSpellCheckEnabled (this.viewer.isSpellCheckingEnabled ());

            // TODO This needs to be done later otherwise will cause an error.
            //this.editor.requestFocus ();

        };

        this.editor.runOnReady (r);
/*
        if (this.viewer.getViewer ().isShowing ())
        {

            if (this.getScene () == null)
            {

                this.sceneProperty ().addListener ((pr, oldv, newv) ->
                {

                    r.run ();

                });

            } else {

                r.run ();

            }

        } else {

            this.viewer.getViewer ().addEventHandler (WindowEvent.WINDOW_SHOWN,
                                                      ev ->
            {

                r.run ();

            });

        }
*/
        super.init (s);

    }

    @Override
    public State getState ()
    {

        State s = super.getState ();

        int ai = this.editor.visibleParToAllParIndex (0);
        Bounds vpb = this.editor.getVisibleParagraphBoundsOnScreen (0);
        IndexRange ir = this.editor.getParagraphTextRange (ai);
        this.editor.selectRange (ir.getStart (), ir.getEnd ());
        Optional<Bounds> bb = this.editor.getSelectionBounds ();

        if ((bb != null)
            &&
            (bb.orElse (null) != null)
           )
        {

            s.set (Constants.LAST_EDITOR_VISIBLE_PARAGRAPH_PROPERTY_NAME,
                   ai);
            s.set (Constants.LAST_EDITOR_SCROLL_OFFSET_PROPERTY_NAME,
                   vpb.getMinY () - bb.get ().getMinY ());

        }

        s.set (Constants.LAST_EDITOR_SCROLL_POSITION_PROPERTY_NAME,
               this.editor.estimatedScrollYProperty ().getValue ().floatValue ());
        s.set (Constants.LAST_EDITOR_CARET_POSITION_PROPERTY_NAME,
               this.editor.getCaretSelectionBind ().getPosition ());

        return s;

    }

    public TextEditor getEditor ()
    {

        return this.editor;

    }

    public Node getEditorWrapper (VirtualizedScrollPane<TextEditor> scrollPane)
    {

        return scrollPane;

    }

    @Override
    public Panel createPanel ()
    {

        this.scrollPane = new VirtualizedScrollPane<> (this.editor);
        VBox.setVgrow (this.scrollPane, Priority.ALWAYS);

        this.getChildren ().add (this.getEditorWrapper (this.scrollPane));

        Panel panel = Panel.builder ()
            // TODO .title (this.object.nameProperty ())
            .title (this.object.nameProperty ())
            .content (this)
            .styleClassName (StyleClassNames.CHAPTER)
            .panelId (getPanelIdForChapter (this.object))
            .actionMappings (this.getActionMappings ())
            .build ();

        panel.addEventHandler (Panel.PanelEvent.SHOW_EVENT,
                               ev ->
        {

            //this.editor.requestFocus ();

        });

        return panel;

    }

/*
TODO
    //@Override
    public Panel createPanel_old ()
    {

        SwingNode n = new SwingNode ();

        SwingUIUtils.doLater (() ->
        {

            try
            {

                this.chapterPanel = this.getChapterPanel ();
                //this.chapterPanel.init (null);
                n.setContent (this.chapterPanel);

                this.init (null);

            } catch (Exception e) {

                // TODO Improve.
                Environment.logError ("HERE",
                                      e);

            }

        });

        VBox.setVgrow (n, Priority.ALWAYS);

        this.getChildren ().add (n);

        Panel panel = Panel.builder ()
            // TODO .title (this.object.nameProperty ())
            .title (this.object.nameProperty ())
            .content (this)
            .styleClassName (StyleClassNames.CHAPTER)
            .panelId (getPanelIdForChapter (this.object))
            // TODO .headerControls ()
            .build ();

        return panel;

    }
*/
/*
TODO ? Remove?
    @Override
    public void saveObject ()
                     throws Exception
    {

        this.object.setText (this.getChapterPanel ().getEditor ().getTextWithMarkup ());

        super.saveObject ();

    }
*/

    @Override
    public void saveObject ()
                     throws Exception
    {

        this.object.setText (this.editor.getTextWithMarkup ());

        super.saveObject ();

    }

    public static String getPanelIdForChapter (Chapter c)
    {

        return "chapter-" + c.getKey ();

    }

    public ReadabilityIndices getReadabilityIndices ()
    {

        // TODO return this.chapterPanel.getReadabilityIndices ();
        return null;

    }

    public StringWithMarkup getText ()
    {

        return this.editor.getTextWithMarkup ();

    }

    public TextEditor.Position createTextPosition (int pos)
    {

        return this.editor.createTextPosition (pos);

    }

    public void bindTextPropertiesTo (TextProperties p)
    {

        this.editor.bindTo (p);

    }

    public void setUseTypewriterScrolling (boolean v)
    {
/*
TODO

        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.setUseTypewriterScrolling (v);

            });

            return;

        }

        if (!v)
        {

            this.scrollPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            // Reset the margin.
            this.editor.setMargin (this.origEditorMargin);
            this.showIconColumn (true);

        } else {

            this.scrollPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            this.showIconColumn (false);

        }

        this.scrollCaretIntoView (null);

        this.scrollPane.getViewport ().setViewSize (this.editor.getPreferredSize ());

        this.editor.requestFocus ();
*/
    }

    public void scrollCaretIntoView (final Runnable runAfterScroll)
    {
/*
TODO Remove
        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.scrollCaretIntoView (runAfterScroll);

            });

            return;

        }

        final ProjectChapterEditorPanelContent _this = this;
*/
/*
TODO
        try
        {

            int c = _this.editor.getCaret ().getDot ();

            if (c > -1)
            {

                _this.scrollToPosition (c);

            }

            _this.updateViewportPositionForTypewriterScrolling ();

            if (runAfterScroll != null)
            {

                SwingUIUtils.doLater (runAfterScroll);

            }

        } catch (Exception e)
        {

            // Ignore it.

        }
*/
    }

    private void scheduleWordCountUpdate ()
    {

        final ChapterEditorPanelContent _this = this;

        if (this.wordCountUpdate != null)
        {

            return;

        }

        this.wordCountUpdate = () ->
        {

            try
            {

                ChapterCounts cc = this.viewer.getChapterCounts (this.object);

                if (this.getText () == null)
                {

                    _this.wordCountUpdate = null;
                    this.scheduleWordCountUpdate ();

                    return;

                }

                final String t = this.getText ().getText ();

                final ChapterCounts ncc = new ChapterCounts (t);

                cc.setWordCount (ncc.getWordCount ());
                cc.setSentenceCount (ncc.getSentenceCount ());

                _this.wordCountUpdate = null;

                this.scheduleA4PageCountUpdate ();

            } catch (Exception e) {

                Environment.logError ("Unable to determine word count for chapter: " +
                                      _this.object,
                                      e);

            }

        };

        this.viewer.schedule (this.wordCountUpdate,
                              1 * Constants.SEC_IN_MILLIS,
                              -1);

    }

    private void scheduleA4PageCountUpdate ()
    {

        this.viewer.unschedule (this.a4PageCountUpdater);

        this.a4PageCountUpdater = this.viewer.schedule (() ->
        {

            try
            {

                ChapterCounts cc = this.viewer.getChapterCounts (this.object);

                cc.setStandardPageCount (UIUtils.getA4PageCountForChapter (this.object,
                                                                           this.getText ().getText ()));

            } catch (Exception e) {

                Environment.logError ("Unable to get a4 page count for chapter: " +
                                      this.object,
                                      e);

            }

        },
        // Start in 2 seconds
        2 * Constants.SEC_IN_MILLIS,
        // Do it once.
        0);

    }

    public void insertSectionBreak ()
    {
/*
TODO
        if (!SwingUtilities.isEventDispatchThread ())
        {

            SwingUIUtils.doLater (() ->
            {

                this.insertSectionBreak ();

            });

            return;

        }
*/
/*
TODO
        final DefaultStyledDocument doc = (DefaultStyledDocument) this.editor.getDocument ();

        final int offset = this.editor.getCaret ().getDot ();

        try
        {

            this.editor.startCompoundEdit ();

            String ins = String.valueOf ('\n') + String.valueOf ('\n') + Constants.SECTION_BREAK + String.valueOf ('\n') + String.valueOf ('\n');

            doc.insertString (offset,
                              ins,
                              this.editor.sectionBreakStyle);

            doc.setParagraphAttributes (offset + 2,
                                        Constants.SECTION_BREAK.length (),
                                        this.editor.sectionBreakStyle,
                                        false);

            doc.setLogicalStyle (offset + 2,
                                 this.editor.sectionBreakStyle);

            this.editor.endCompoundEdit ();

        } catch (Exception e)
        {

        }
*/
    }

    public void hidePopups ()
    {

        Set<QuollPopup> popups = new HashSet<> (this.popupsToCloseOnClick);

        popups.stream ()
           .forEach (p -> p.close ());

        this.popupsToCloseOnClick.clear ();

    }

    private void hideAllContextMenus ()
    {

        UIUtils.runLater (() ->
        {

            this.contextMenus.values ().stream ()
                .forEach (cm -> cm.hide ());

        });

    }

}
