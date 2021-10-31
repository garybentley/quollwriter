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

public class LanguageStringsTextIdBox extends LanguageStringsIdBox<TextValue, String>
{

    //public static final String POPUP_ID = "stringsmatch";

    private String popupId = null;

    private QuollTextArea userValue = null;
    //privte TextArea userValue = null;

    private QuollTextView preview = null;
    private VBox previewWrapper = null;

    private DictionaryProvider2 dictProv = null;

    private ListView<String> matchesView = null;

    private ScheduledFuture errorCheck = null;

    private VBox matchesWrapper = null;

    //private ObservableList<String> matches = FXCollections.observableList (new ArrayList<> ());

    public LanguageStringsTextIdBox (final TextValue               baseValue,
                                     final TextValue               stringsValue,
                                     final LanguageStringsIdsPanel panel)
    {

        super (baseValue,
               stringsValue,
               panel);

        this.matchesWrapper = new VBox ();

        this.popupId = BaseStrings.toId (this.baseValue.getId ()) + "stringsmatch";

        LanguageStringsTextIdBox _this = this;

        try
        {

            this.dictProv = new UserDictionaryProvider (dictProv)
            {

                @Override
                public com.quollwriter.ui.fx.SpellChecker getSpellChecker ()
                {

                    final com.quollwriter.ui.fx.SpellChecker sp = super.getSpellChecker ();

                    return new com.quollwriter.ui.fx.SpellChecker ()
                    {

                        @Override
                        public boolean isCorrect (Word word)
                        {

                            int offset = word.getAllTextStartOffset ();

                            Id id = _this.getIdAtOffset (offset);

                            if (id != null)
                            {

                                return _this.panel.getEditor ().getBaseStrings ().isIdValid (id.getId ());

                            }

                            return sp.isCorrect (word);

                        }

                        @Override
                        public boolean isIgnored (Word word)
                        {

                            return false;

                        }

                        @Override
                        public java.util.List<String> getSuggestions (Word word)
                        {

                            return sp.getSuggestions (word);

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

        items.add (new Form.Item (this.previewWrapper));

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

                                                          //this.fillMatch ();

                                                      } else {

                                                          this.showMatches (false);

                                                      }

                                                      if (ev.isShiftDown ())
                                                      {

                                                          panel.moveToPreviousBox (stringsValue);

                                                      } else {

                                                          panel.moveToNextBox (stringsValue);

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

        this.userValue.getTextEditor ().focusedProperty ().addListener ((pr, oldv, newv) ->
        {
/*
            if (this.isSelectorVisible ())
            {

                return;

            }
*/
            if (!newv)
            {

                this.hideSelector ();

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
/*
        this.userValue.getTextEditor ().plainTextChanges ().subscribe (ev ->
        {

            if (this.errorCheck != null)
            {

                this.errorCheck.cancel (true);

            }

            this.errorCheck = this.panel.getEditor ().schedule (() ->
            {


            },
            750,
            -1);

        });
*/

        UIUtils.forceRunLater (() ->
        {

            this.showPreview ();

        });

        return items;

    }

    private void showMatches (boolean fillOnSingleMatch)
    {

        int c = this.userValue.getTextEditor ().getCaretPosition ();

        String t = this.userValue.getTextEditor ().getText ();

        Id id = BaseStrings.getId (t, c);

        if (id == null)
        {

            this.hideSelector ();
            return;

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
                    return;

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

            b = this.userValue.getTextEditor ().screenToLocal (b);
            //b = this.userValue.getTextEditor ().localToScene (b);

            this.showSelectionPopup (matches,
                                      b.getMinX (),
                                      b.getMaxY () - this.userValue.getTextEditor ().getLayoutBounds ().getHeight ());

        } catch (Exception e) {

            Environment.logError ("Unable show selection popup",
                                  e);

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

    public boolean hasErrors ()
    {

        String s = this.getUserValue ();

        if (s == null)
        {

            return false;

        }

        return BaseStrings.getErrors (s,
                                      BaseStrings.toId (this.baseValue.getId ()),
                                      this.baseValue.getSCount (),
                                      this.getEditor ()).size () > 0;


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

        TextEditor editor = this.userValue.getTextEditor ();

        int c = editor.getCaretPosition ();

        Id id = BaseStrings.getId (editor.getText (),
                                   c);

        if (id == null)
        {

            return;

        }

        String m = this.matchesView.getFocusModel ().getFocusedItem ();

        if (m == null)
        {

            m = this.matchesView.getItems ().get (0);

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

        int s = this.matchesView.getItems ().size ();

        if (i < 0)
        {

            i = s + i;

        }

        if (i > s - 1)
        {

            i -= s;

        }

        this.matchesView.getFocusModel ().focus (i);

        this.matchesView.scrollTo (i);

        return this.matchesView.getItems ().get (i);

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

            this.matchesView.getFocusModel ().focus (-1);

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

        this.matchesWrapper.getChildren ().clear ();

        this.matchesView = new ListView<> ();
        this.matchesWrapper.getChildren ().add (this.matchesView);
        this.matchesView.setItems (FXCollections.observableList (new ArrayList<> (matches)));
        VBox.setVgrow (this.matchesView,
                       Priority.ALWAYS);

        this.matchesView.getSelectionModel ().setSelectionMode (SelectionMode.SINGLE);
        /*
        this.matchesView.setOnMouseClicked (ev ->
        {

            this.fillMatch ();

        });
        */
        this.matchesView.getSelectionModel ().selectedItemProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv != null)
            {

                this.fillMatch ();

            }

        });

        this.userValue.getTextEditor ().setFocusTraversable (false);

        QuollPopup qp = this.panel.getEditor ().getPopupById (this.popupId);

        if (qp == null)
        {

            qp = QuollPopup.builder ()
                .styleClassName (StyleClassNames.STRING)
                .styleSheet (StyleClassNames.OBJECTSELECT, "languagestringmatches")
                .hideOnEscape (true)
                .withClose (false)
                .content (this.matchesWrapper)
                .popupId (this.popupId)
                .removeOnClose (false)
                .withViewer (this.panel.getEditor ())
                .build ();

        }

        UIUtils.forceRunLater (() ->
        {
            try
            {

                this.matchesView.setPrefHeight (this.matchesView.getItems ().size () * (this.matchesView.getFixedCellSize () + 1));
                //this.matchesView.setMinHeight (this.matchesView.getPrefHeight ());
                this.matchesView.setMaxHeight (this.matchesView.getPrefHeight ());

                if (this.matchesView.getItems ().size () > 5)
                {

                    this.matchesView.setPrefHeight (5 * (this.matchesView.getFixedCellSize () + 1));
                    this.matchesView.setMaxHeight (5 * (this.matchesView.getFixedCellSize () + 1));
                    //this.matchesView.setMinHeight (this.matchesView.getPrefHeight ());

                }

                this.matchesWrapper.setPrefHeight (this.matchesView.getPrefHeight ());
                this.matchesWrapper.setMinHeight (this.matchesView.getMinHeight ());
                this.matchesWrapper.setMaxHeight (this.matchesView.getMaxHeight ());

                int ind = this.matchesView.getFocusModel ().getFocusedIndex ();

                if (ind < 0)
                {

                    ind = 0;

                }

                this.matchesView.getFocusModel ().focus (ind);

            } catch (Exception e) {

                // Just ignore, sometimes the LC can be null.

            }

        });

        this.panel.getEditor ().showPopup (qp,
                                           this.userValue.getTextEditor (),
                                           x,
                                           y,
                                           null);

    }

}
