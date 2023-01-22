package com.quollwriter.ui.fx;

import java.util.concurrent.*;

import java.util.*;

import org.reactfx.*;
import org.reactfx.util.*;
import org.fxmisc.wellbehaved.event.*;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.events.*;

public class LanguageStringsTextIdBox extends LanguageStringsIdBox<TextValue, String>
{

    private String popupId = null;

    private QuollTextArea userValue = null;
    //privte TextArea userValue = null;

    private QuollTextView preview = null;
    private VBox previewWrapper = null;

    private DictionaryProvider2 dictProv = null;

    private ScheduledFuture errorCheck = null;

    private ObservableList<String> matches = null;

    private ShowObjectSelectPopup<String> matchesView = null;

    public LanguageStringsTextIdBox (final TextValue               baseValue,
                                     final TextValue               stringsValue,
                                     final LanguageStringsIdsPanel panel)
    {

        super (baseValue,
               stringsValue,
               panel);

        this.matches = FXCollections.observableList (new ArrayList<> ());

        this.popupId = BaseStrings.toId (this.baseValue.getId ()) + "stringsmatch";

        LanguageStringsTextIdBox _this = this;
        try
        {

            this.dictProv = new DictionaryProvider2 ()
            {

                @Override
                public void addDictionaryChangedListener (DictionaryChangedListener l)
                {

                }

                @Override
                public void removeDictionaryChangedListener (DictionaryChangedListener l)
                {

                }

                @Override
                public void removeWord (String word)
                {

                }

                @Override
                public void addWord (String word)
                {

                }

                @Override
                public com.quollwriter.ui.fx.SpellChecker getSpellChecker ()
                {

                    return new com.quollwriter.ui.fx.SpellChecker ()
                    {

                        @Override
                        public boolean isCorrect (Word word)
                        {
//if(true){return true;}
                            if (_this.panel.getEditor ().getBaseStrings ().isIdValid (word.getText ()))
                            {

                                return true;

                            }

                            com.quollwriter.ui.fx.SpellChecker sp = _this.panel.getEditor ().getSpellChecker ();

                            if (sp != null)
                            {

                                return sp.isCorrect (word);

                            } else {

                                return true;

                            }

                        }

                        @Override
                        public boolean isIgnored (Word word)
                        {

                            return false;

                        }

                        @Override
                        public java.util.List<String> getSuggestions (Word word)
                        {

                            com.quollwriter.ui.fx.SpellChecker sp = _this.panel.getEditor ().getSpellChecker ();

                            if (sp != null)
                            {

                                return sp.getSuggestions (word);

                            } else {

                                return null;

                            }

                        }

                    };

                }

            };

        } catch (Exception e) {

            Environment.logError ("Unable to create user dictionary",
                                  e);

        }

    }

    @Override
    public void requestFocus ()
    {

        this.userValue.requestFocus ();

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.STRING;

    }

    @Override
    public Set<Form.Item> getFormItems ()
    {

        LanguageStringsTextIdBox _this = this;

        Set<Form.Item> items = new LinkedHashSet<> ();

        items.add (new Form.Item (new SimpleStringProperty ("English"),
                                  QuollTextView.builder ()
                                    .text (this.baseValue.getRawText ())
                                    .formatter (s ->
                                    {

                                        s = Utils.replaceString (s,
                                                                 "<",
                                                                 "&lt;");
                                        return s;

                                    })
                                    .build ()));

        this.previewWrapper = new VBox ();
        previewWrapper.getStyleClass ().add (StyleClassNames.PREVIEW);
        previewWrapper.managedProperty ().bind (previewWrapper.visibleProperty ());
        previewWrapper.setVisible (false);

        this.preview = QuollTextView.builder ()
            .inViewer (this.panel.getEditor ())
            .build ();
        this.preview.setFocusTraversable (false);

        this.previewWrapper.getChildren ().add (QuollLabel.builder ()
            .label (new SimpleStringProperty ("Preview"))
            .build ());
        this.previewWrapper.getChildren ().add (this.preview);
        this.userValue = QuollTextArea.builder ()
                            .text ((this.stringsValue != null ? this.stringsValue.getRawText () : null))
                            .contextMenu (() ->
                            {

                                Set<MenuItem> its = new LinkedHashSet<> ();

                                final Id id = this.getIdAtOffset (this.userValue.getTextEditor ().getTextPositionForCurrentMousePosition ());

                                if (id != null)
                                {

                                    its.add (QuollMenuItem.builder ()
                                        .iconName (StyleClassNames.VIEW)
                                        .label (new SimpleStringProperty ("Go to Id definition"))
                                        .onAction (ev ->
                                        {

                                            this.getEditor ().showId (id);

                                        })
                                        .build ());

                                }

                                its.add (QuollMenuItem.builder ()
                                    .iconName (StyleClassNames.COPY)
                                    .label (new SimpleStringProperty ("Use the English value"))
                                    .onAction (ev ->
                                    {

                                        this.useEnglishValue ();

                                    })
                                    .build ());

                                return its;

                            })
                            .dictionaryProvider (this.dictProv)
                            .spellCheckEnabled (true)
                            .formattingEnabled (false)
                            .autoGrabFocus (false)
                            .withViewer (this.panel.getEditor ())
                            .build ();

        // TODO Mouse motion - _this.userValue.setToolTipText (_this.getEditor ().getString (id.getId ()));

        // TODO _this.updatePreviews ();
        //_this.showPreview ();

/*
TODO ?
        final Action defSelect = this.userValue.getEditor ().getActionMap ().get (DefaultEditorKit.selectWordAction);

        this.userValue.getEditor ().getActionMap ().put (DefaultEditorKit.selectWordAction,
                                                         new TextAction (DefaultEditorKit.selectWordAction)
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                int c = _this.userValue.getEditor ().getCaretPosition ();

                Id id = _this.getIdAtCaret ();

                if (id != null)
                {

                    _this.userValue.getEditor ().setSelectionStart (id.getPart (c).start);
                    _this.userValue.getEditor ().setSelectionEnd (id.getPart (c).end);

                } else {

                    defSelect.actionPerformed (ev);

                }

            }

        });
*/

        this.userValue.getTextEditor ().caretPositionProperty ().addListener ((pr, oldv, newv) ->
        {

            if ((newv > 0)
                &&
                (this.userValue.getTextEditor ().isFocused ())
               )
            {

                this.showMatches (false);

            }

        });

        Nodes.addInputMap (this.userValue.getTextEditor (),
                           InputMap.process (EventPattern.keyPressed (KeyCode.P, KeyCombination.SHORTCUT_DOWN),
                                             ev ->
                                             {

                                                 this.showPreview ();

                                                 return InputHandler.Result.CONSUME;

                                             }));

        Nodes.addInputMap (this.userValue.getTextEditor (),
                           InputMap.process (EventPattern.keyPressed (),
                                             ev ->
                                             {

                                                 if ((ev.getCode () == KeyCode.DELETE)
                                                     ||
                                                     (ev.getCode () == KeyCode.BACK_SPACE)
                                                    )
                                                 {

                                                     UIUtils.forceRunLater (() ->
                                                     {

                                                         this.showMatches (false);

                                                     });

                                                     return InputHandler.Result.PROCEED;

                                                 }

                                                  if ((ev.getCode () == KeyCode.CLOSE_BRACKET)
                                                      &&
                                                      (ev.isShiftDown ())
                                                     )
                                                  {

                                                      this.hideSelector ();

                                                      return InputHandler.Result.CONSUME;

                                                  }

                                                  if (ev.getCode () == KeyCode.ENTER)
                                                  {

                                                      if (this.isSelectorVisible ())
                                                      {

                                                          ev.consume ();

                                                          this.matchesView.getSelectionModel ().select (this.matchesView.getFocusModel ().getFocusedIndex ());

                                                          //this.fillMatch ();

                                                          return InputHandler.Result.CONSUME;

                                                      }

                                                  }

                                                  if (ev.getCode () == KeyCode.UP)
                                                  {

                                                      if (this.isSelectorVisible ())
                                                      {

                                                          ev.consume ();

                                                          this.updateSelectedMatch (-1);

                                                          return InputHandler.Result.CONSUME;

                                                      }

                                                  }

                                                  if (ev.getCode () == KeyCode.DOWN)
                                                  {

                                                      if (this.isSelectorVisible ())
                                                      {

                                                          ev.consume ();

                                                          this.updateSelectedMatch (1);

                                                          return InputHandler.Result.CONSUME;

                                                      }

                                                  }

                                                  if (ev.getCode () == KeyCode.ESCAPE)
                                                  {

                                                      ev.consume ();

                                                      this.hideSelector ();

                                                      return InputHandler.Result.CONSUME;

                                                  }

                                                  if ((ev.getCode () == KeyCode.ENTER)
                                                      ||
                                                      (ev.getCode () == KeyCode.UP)
                                                      ||
                                                      (ev.getCode () == KeyCode.DOWN)
                                                     )
                                                  {

                                                      if (this.isSelectorVisible ())
                                                      {

                                                          ev.consume ();

                                                          return InputHandler.Result.CONSUME;

                                                      }

                                                  }

                                                  if (ev.getCode () == KeyCode.TAB)
                                                  {

                                                      if (this.isSelectorVisible ())
                                                      {

                                                          this.matchesView.getSelectionModel ().select (this.matchesView.getFocusModel ().getFocusedIndex ());

                                                          return InputHandler.Result.CONSUME;

                                                          //this.fillMatch ();

                                                      } else {

                                                          if (this.showMatches (false))
                                                          {

                                                              return InputHandler.Result.CONSUME;

                                                          }

                                                      }

                                                      if (ev.isShiftDown ())
                                                      {

                                                          panel.moveToPreviousBox (this.baseValue);

                                                      } else {

                                                          panel.moveToNextBox (this.baseValue);

                                                      }

                                                      return InputHandler.Result.CONSUME;

                                                  }

                                                  UIUtils.forceRunLater (() ->
                                                  {

                                                      this.showMatches (false);

                                                  });

                                                  return InputHandler.Result.PROCEED;

                                             }));

        items.add (new Form.Item (new SimpleStringProperty ("Your Value"),
                                  this.userValue));

        items.add (new Form.Item (this.previewWrapper));

        this.userValue.getTextEditor ().focusedProperty ().addListener ((pr, oldv, newv) ->
        {

            this.hideSelector ();

            if (!newv)
            {

                if (this.isSelectorVisible ())
                {

                    return;

                }

                UIUtils.runLater (() ->
                {

                    if (this.showErrors (false))
                    {

                        return;

                    }

                    this.showPreview ();

                    this.updateSideBar (this.baseValue);

                });

            }

        });

        this.userValue.getTextEditor ().plainTextChanges ().subscribe (ev ->
        {

            this.showPreview ();
/*
            if (this.errorCheck != null)
            {

                this.errorCheck.cancel (true);

            }

            this.errorCheck = this.panel.getEditor ().schedule (() ->
            {

                UIUtils.runLater (() ->
                {

                    this.showErrors (false);

                });

            },
            750,
            -1);
*/
        });

        UIUtils.forceRunLater (() ->
        {

            this.showPreview ();

        });

        return items;

    }

    private boolean showMatches (boolean fillOnSingleMatch)
    {

        int c = this.userValue.getTextEditor ().getCaretPosition ();

        String t = this.userValue.getTextEditor ().getText ();
        Id id = BaseStrings.getId (t, c);

        if (id == null)
        {

            this.hideSelector ();
            return false;

        }

        Set<String> matches = id.getPartMatches (c,
                                                 this.panel.getEditor ().getBaseStrings ().getStrings ());

        if ((matches == null)
            ||
            (matches.size () == 0)
           )
        {

            this.hideSelector ();

        }

        if (matches.size () == 1)
        {

            Id.Part p = id.getPart (c);

            if (p == null)
            {

                p = id.getLastPart ();

            }

            if (p.part.equals (matches.iterator ().next ()))
            {

                if (fillOnSingleMatch)
                {

                    this.fillMatch ();

                    this.hideSelector ();
                    return true;

                }

            }

        }

        try
        {

            int ind = c;

            Id.Part pa = id.getPart (c);

            if (pa != null)
            {

                ind = pa.start;

            }

            Bounds b = this.userValue.getTextEditor ().getBoundsForPosition (ind);

            if (b == null)
            {

                return false;

            }

            b = this.userValue.getTextEditor ().screenToLocal (b);
            //b = this.userValue.getTextEditor ().localToScene (b);

            this.showSelectionPopup (matches,
                                      b.getMinX (),
                                      b.getMaxY () - this.userValue.getTextEditor ().getLayoutBounds ().getHeight ());

            return true;

        } catch (Exception e) {

            Environment.logError ("Unable to show selection popup",
                                  e);

            return false;

        }

    }

    @Override
    public void showPreview ()
    {

        String s = this.getUserValue ();

        if (s == null)
        {

            this.previewWrapper.setVisible (false);

            return;

        }

        this.previewWrapper.setVisible (true);

        String t = this.getEditor ().getPreviewText (s);

        t = Utils.replaceString (t,
                                       "[",
                                       "&#91;");

        t = Utils.replaceString (t,
                                       "]",
                                       "&#93;");

        this.preview.setText (new SimpleStringProperty (t));

    }

    public void saveValue ()
                    throws GeneralException
    {

        String uv = this.getUserValue ();

        if (uv != null)
        {

            if (this.stringsValue != null)
            {

                this.stringsValue.setRawText (uv);

            } else {

                this.stringsValue = this.getEditor ().getUserStrings ().insertTextValue (this.baseValue.getId ());

                //this.stringsValue.setSCount (this.baseValue.getSCount ());
                this.stringsValue.setRawText (uv);

            }

        } else {

            this.getEditor ().getUserStrings ().removeNode (this.baseValue.getId ());

        }

    }

    public Id getIdAtPoint (Point2D p)
    {

        try
        {

            return BaseStrings.getId (this.userValue.getTextEditor ().getText (),
                                      this.userValue.getTextEditor ().getTextPositionForMousePosition (p.getX (),
                                                                                                       p.getY ()));

        } catch (Exception e) {

            // Can ignore.

        }

        return null;

    }

    public Id getIdAtOffset (int offset)
    {

        return BaseStrings.getId (this.userValue.getTextEditor ().getText (),
                                  offset);

    }

    public Id getIdAtCaret ()
    {

        return this.getIdAtOffset (this.userValue.getTextEditor ().getCaretPosition ());

    }

    @Override
    public String getUserValue ()
    {

        if (this.userValue.getTextEditor ().isReadyForUse ())
        {

            StringWithMarkup sm = this.userValue.getTextEditor ().getTextWithMarkup ();

            if (sm != null)
            {

                if (!sm.hasText ())
                {

                    return null;

                }

                return sm.getMarkedUpText ();

            }

        } else {

            if (this.stringsValue != null)
            {

                return this.stringsValue.getRawText ();

            }

        }

        return null;

    }

    public void useEnglishValue ()
    {

        this.userValue.getTextEditor ().setText (new StringWithMarkup (this.baseValue.getRawText ()));

    }

    public int getErrorCount ()
    {

        String s = this.getUserValue ();

        if (s == null)
        {

            return 0;

        }

        return BaseStrings.getErrors (s,
                                      BaseStrings.toId (this.baseValue.getId ()),
                                      this.baseValue.getSCount (),
                                      this.getEditor ()).size ();

    }

    public boolean hasErrors ()
    {

        return this.getErrorCount () > 0;


    }

    public boolean showErrors (boolean requireUserValue)
    {

        String s = this.getUserValue ();

        if ((s == null)
            &&
            (!requireUserValue)
           )
        {

            return false;

        }

        Set<String> errs = null;

        if (s == null)
        {

            errs = new LinkedHashSet<> ();

            errs.add ("Cannot show a preview, no value provided.");

        } else {

            errs = BaseStrings.getErrors (s,
                                          BaseStrings.toId (this.baseValue.getId ()),
                                          this.baseValue.getSCount (),
                                          this.getEditor ());

        }

        return this.showErrors (errs);

    }

    private void fillMatch ()
    {

        String m = this.matchesView.getFocusModel ().getFocusedItem ();

        if (m == null)
        {

            m = this.matches.get (0);

        }

        this.fillMatch (m);

    }

    private void fillMatch (String m)
    {

        TextEditor editor = this.userValue.getTextEditor ();

        int c = editor.getCaretPosition ();

        Id id = BaseStrings.getId (editor.getText (),
                                   c);

        if (id == null)
        {

            return;

        }

        Id.Part part = id.getPart (c);

        if (part != null)
        {

            editor.replaceText (part.start, part.end, m);
            editor.setCaretPosition (part.start + m.length ());

        } else {

            // Is the previous character a . if so append.
            if ((editor.getText ().substring (c - 1, c).equals ("."))
                ||
                (c == id.getEnd ())
               )
            {

                editor.replaceText (c, c, m);
                editor.setCaretPosition (c + m.length ());

            } else {

                part = id.getLastPart ();

                editor.replaceText (part.start, part.start + part.end, m);
                editor.setCaretPosition (part.start + m.length ());

            }

        }

        c = editor.getCaretPosition ();

        id = this.getIdAtCaret ();

        // We may be inserting into the middle of an id, check to see if it's valid.
/*
        if (id.hasErrors ())
        {

            this.hideSelector ();

            // Update the view to "spell check" the ids.
            return;

        }
*/
        // Check to see if the id maps to a string.
        if (this.getEditor ().getBaseStrings ().getString (id.getId ()) != null)
        {

            if (id.isPartial ())
            {

                editor.replaceText (id.getEnd (), id.getEnd (), "}");
                editor.setCaretPosition (c + 1);

            }

            this.hideSelector ();

            return;

        }

        // Check to see if there are more matches further down the tree.
        String nid = id.getId () + ".";

        Set<String> matches = this.getEditor ().getBaseStrings ().getIdMatches (nid);

        if (matches.size () > 0)
        {

            c = editor.getCaretPosition ();

            editor.replaceText (c, c, ".");
            editor.setCaretPosition (c + 1);

            try
            {

                Bounds b = this.userValue.getTextEditor ().getBoundsForPosition (editor.getCaretPosition ());

                b = this.userValue.getTextEditor ().screenToLocal (b);
                //b = this.userValue.getTextEditor ().localToScene (b);

                this.showSelectionPopup (matches,
                                          b.getMinX (),
                                          b.getMaxY () - this.userValue.getTextEditor ().getLayoutBounds ().getHeight ());

            } catch (Exception e) {

                e.printStackTrace ();

            }

            return;

        } else {

            c = editor.getCaretPosition ();

            editor.replaceText (c, c, "}");
            editor.setCaretPosition (c + 1);

            this.hideSelector ();

            return;

        }

    }

    private String updateSelectedMatch (int incr)
    {

        int i = this.matchesView.getFocusModel ().getFocusedIndex ();

        i += incr;

        int s = this.matches.size ();

        if (i < 0)
        {

            i = s + i;

        }

        if (i > s - 1)
        {

            i -= s;

        }

        this.matchesView.getFocusModel ().focus (i);

        return this.matchesView.getFocusModel ().getFocusedItem ();

    }

    public boolean isSelectorVisible ()
    {

        return this.panel.getEditor ().getPopupById (this.popupId) != null;

    }

    public void hideSelector ()
    {

        QuollPopup qp = this.panel.getEditor ().getPopupById (this.popupId);

        if (qp != null)
        {

            qp.close ();

        }

        this.userValue.getTextEditor ().setFocusTraversable (true);

        if (this.matchesView != null)
        {

            //this.matchesView.getFocusModel ().focus (-1);

        }

    }

    public void showSelectionPopup (Set<String> matches,
                                    Bounds      b)
    {

        this.showSelectionPopup (matches,
                                 b.getMinX (),
                                 b.getMinY ());

    }

    public void showSelectionPopup (Set<String> matches,
                                    Point2D     point)
    {

        this.showSelectionPopup (matches,
                                 point.getX (),
                                 point.getY ());

    }

    public void showSelectionPopup (Set<String> matches,
                                    double      x,
                                    double      y)
    {

        if ((matches == null)
            ||
            (matches.size () == 0)
           )
        {

            QuollPopup qp = this.panel.getEditor ().getPopupById (this.popupId);

            if (qp != null)
            {

                qp.close ();

            }

            return;

        }

        this.matches.clear ();
        this.matches.addAll (matches);

        this.userValue.getTextEditor ().setFocusTraversable (false);

        QuollPopup qp = this.panel.getEditor ().getPopupById (this.popupId);

        if (qp == null)
        {

            this.matchesView = ShowObjectSelectPopup.<String>builder ()
                .withViewer (this.panel.getEditor ())
                .styleClassName (StyleClassNames.STRING)
                .title ((StringProperty) null)
                .styleSheet (StyleClassNames.OBJECTSELECT, "languagestringmatches")
                .popupId (this.popupId)
                .objects (this.matches)
                .withClose (false)
                .visibleCount (7)
                .allowNavigationByKeys (true)
                .cellProvider ((obj, popupContent) ->
                {

                   QuollLabel l = QuollLabel.builder ()
                        .label (obj)
                        .build ();

                    l.setOnMousePressed (ev ->
                    {

                        if (ev.getButton () != MouseButton.PRIMARY)
                        {

                            return;

                        }

                        this.fillMatch (obj);

                    });

                    return l;

                })
                .build ();

            qp = this.matchesView.createPopup ();

            this.matchesView.getSelectionModel ().selectedItemProperty ().addListener ((pr, oldv, newv) ->
            {

                if (newv != null)
                {

                    this.fillMatch ();

                }

            });

        }

        QuollPopup _qp = qp;

        UIUtils.forceRunLater (() ->
        {

            this.matchesView.getFocusModel ().focus (-1);
            this.matchesView.getFocusModel ().focus (0);

        });

        this.panel.getEditor ().showPopup (_qp,
                                           this.userValue.getTextEditor (),
                                           x,
                                           y,
                                           null);

    }

}
