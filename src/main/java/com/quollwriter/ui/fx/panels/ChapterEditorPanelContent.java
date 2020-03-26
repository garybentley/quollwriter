package com.quollwriter.ui.fx.panels;

import java.util.*;

import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.embed.swing.*;

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

    //private P chapterPanel = null;
    protected TextEditor editor = null;
    private VirtualizedScrollPane<TextEditor> scrollPane = null;
    protected javafx.beans.property.StringProperty selectedTextProp = null;

    public ChapterEditorPanelContent (E              viewer,
                                      Chapter        chapter,
                                      TextProperties props)
                               throws GeneralException
    {

        super (viewer,
               chapter);

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

            this.setHasUnsavedChanges (true);

        });

        this.editor.selectedTextProperty ().addListener ((pr, oldv, newv) ->
        {

            this.selectedTextProp.setValue (this.editor.getSelectedText ());

        });

        this.viewer.spellCheckingEnabledProperty ().addListener ((pr, oldv, newv) ->
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

    }

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

    public void scrollToTextPosition (int      pos,
                                      Runnable afterScroll)
    {

        Bounds b = this.editor.getBoundsForPosition (pos);

        if (b == null)
        {

            int p = this.editor.getParagraphForOffset (pos);

            this.editor.showParagraphInViewport (p);

            UIUtils.runLater (() ->
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

        UIUtils.runLater (afterScroll);

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

                this.scrollPane.scrollYBy (f);

                Integer i = s.getAsInt (Constants.LAST_EDITOR_CARET_POSITION_PROPERTY_NAME,
                                        0);

                //this.editor.getCaretSelectionBind ().moveTo (i);

            }

            this.editor.requestFollowCaret ();

            this.editor.setSpellCheckEnabled (this.viewer.isSpellCheckingEnabled ());

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

        s.set (Constants.LAST_EDITOR_SCROLL_POSITION_PROPERTY_NAME,
               this.scrollPane.estimatedScrollYProperty ().getValue ().floatValue ());
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

}
