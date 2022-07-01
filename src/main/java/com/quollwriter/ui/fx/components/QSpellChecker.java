package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.concurrent.*;

import javafx.geometry.*;
import javafx.scene.control.*;

import org.reactfx.*;

import org.fxmisc.richtext.model.PlainTextChange;

import com.quollwriter.text.*;
import com.quollwriter.DictionaryProvider2;
import com.quollwriter.synonyms.SynonymProvider;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import com.quollwriter.ui.events.DictionaryChangedEvent;
import com.quollwriter.ui.events.DictionaryChangedListener;

public class QSpellChecker implements DictionaryChangedListener
{

    private static final char NULL_CHAR = (char) 0;

    private TextEditor        text = null;
    private SpellChecker       checker = null;
    private Map                elsToCheck = new WeakHashMap ();
    private boolean            enabled = false;
    private int                lastCaret = -1;
    private char               lastCharacterOver = QSpellChecker.NULL_CHAR;
    private DictionaryProvider2 dictProvider = null;
    private SynonymProvider    synonymProvider = null;
    private Subscription subscription = null;
    private int lastCaretPos = -1;

    public QSpellChecker(TextEditor          text,
                         DictionaryProvider2 prov)
    {

        this.text = text;

        this.setDictionaryProvider (prov);

    }

    public void checkParagraph (int pi)
    {

        Environment.schedule (() ->
        {

            List<IndexRange> errors = new ArrayList<> ();

            TextIterator ti = new TextIterator (this.text.getParagraphs ().get (pi).getText ());
            IndexRange pr = this.text.getParagraphTextRange (pi);

            int start = pr.getStart ();

            List<Word> words = ti.getWords ();

            for (Word w : words)
            {

                if (w.isPunctuation ())
                {

                    continue;

                }

                if (!this.isWordCorrect (w))
                {

                    try
                    {

                        int wordStart = w.getAllTextStartOffset () + start;
                        int wordEnd = w.getAllTextEndOffset () + start;
                        errors.add (new IndexRange (wordStart, wordStart + w.getText ().length ()));

                    } catch (Exception e)
                    {

                        e.printStackTrace ();

                    }

                }

            }

            UIUtils.runLater (() ->
            {

                this.text.setSpellingErrors (start,
                                             pr.getEnd (),
                                             errors);

            });

        },
        -1,
        -1);

    }

    public void checkAll ()
    {

        if (!this.isEnabled ())
        {

            return;

        }

        Environment.schedule (() ->
        {

            List<IndexRange> errors = new ArrayList<> ();

            TextIterator ti = new TextIterator (this.text.getText ().substring (0,
                                                                                this.text.getText ().length ()));

            List<Word> words = ti.getWords ();

            for (Word w : words)
            {

                if (w.isPunctuation ())
                {

                    continue;

                }

                if (!this.isWordCorrect (w))
                {

                    try
                    {

                        int wordStart = w.getAllTextStartOffset ();
                        int wordEnd = w.getAllTextEndOffset ();

                        errors.add (new IndexRange (wordStart, wordStart + w.getText ().length ()));

                    } catch (Exception e)
                    {
    e.printStackTrace ();
                    }

                }

            }

            UIUtils.runLater (() ->
            {

                this.text.setSpellingErrors (0,
                                             this.text.getText ().length (),
                                             errors);

            });

        },
        -1,
        -1);

    }

    public SynonymProvider getSynonymProvider ()
    {

        return this.synonymProvider;

    }

    public void setSynonymProvider (SynonymProvider sp)
    {

        this.synonymProvider = sp;

    }

    public void setDictionaryProvider (DictionaryProvider2 dp)
    {

        if (this.dictProvider != null)
        {

            this.dictProvider.removeDictionaryChangedListener (this);

        }

        if (dp == null)
        {

            this.checker = null;
            this.dictProvider = null;

            return;

        }

        this.checker = dp.getSpellChecker ();

        this.dictProvider = dp;
        this.dictProvider.addDictionaryChangedListener (this);

    }

    public void dictionaryChanged (DictionaryChangedEvent ev)
    {

        if (ev.getType () == DictionaryChangedEvent.WORD_ADDED)
        {

            // Recheck the document.
            this.checkAll ();

        }

        if (ev.getType () == DictionaryChangedEvent.WORD_REMOVED)
        {

            // Recheck the document.
            this.checkAll ();

        }

    }

    public boolean isWordCorrect (Word word)
    {

        if (this.checker == null)
        {

            return false;

        }

        return this.checker.isCorrect (word);

/*
        if (w.isPunctuation ())
        {

            continue;

        }

        // See if the word is a number.
        try
        {

            Double.parseDouble (word);

            return true;

        } catch (Exception e) {

            // Not a number.

        }

        boolean v = this.checker.isCorrect (word)
                    ||
                    this.checker.isIgnored (word);

        if (!v)
        {

            // See if we have a synonym provider, if so check to see if the word ends with
            // "s", "ed" or "er", if it does strip the ending then look for the synonym, if it has
            // one then allow it through.
            if (((word.endsWith ("s"))
                 ||
                 (word.endsWith ("ed"))
                 ||
                 (word.endsWith ("er"))
                )
                &&
                (this.synonymProvider != null)
               )
            {

                if (word.endsWith ("s"))
                {

                    word = word.substring (0,
                                           word.length () - 1);

                } else {

                    word = word.substring (0,
                                           word.length () - 2);

                }

                try
                {

                    v = this.synonymProvider.hasSynonym (word);

                } catch (Exception e) {

                    // Ignore it.

                }

            }

        }

        return v;
*/
    }

    public void addWord (String w)
    {

        this.dictProvider.addWord (w);

    }

    public List getSuggestions (Point2D p)
    {

        TextIterator ti = new TextIterator (this.text.getText ());

        int start = this.text.getTextPositionForMousePosition (p.getX (),
                                                               p.getY ());

        Word word = null;

        if (start > -1)
        {

            word = ti.getWordAt (start);

        }

        return this.getSuggestions (word);

    }

    public List getSuggestions (Word word)
    {

        if (word == null)
        {

            return null;

        }

        if (this.checker == null)
        {

            return null;

        }

        return this.checker.getSuggestions (word);

    }
/*
TODO Remove?
    public void checkElements (final int start)
    {

        // Get the element for the offset.
        final Element el;

        try
        {

            el = ((AbstractDocument) this.text.getDocument ()).getParagraphElement (start);

        } catch (Exception ex)
        {

            return;

        }

        // See if we have it scheduled for checking.
        if (this.elsToCheck.containsKey (el))
        {

            // Already scheduled.
            return;

        }

        this.elsToCheck.put (el,
                             el);

        final QSpellChecker _this = this;

                    SwingUtilities.invokeLater (new Runnable ()
                        {

                            public void run ()
                            {

                                _this.checkElement (el);

                                final Object o = new Object ();

                                synchronized (o)
                                {

                                    _this.elsToCheck.remove (el);

                                }

                            }

                        });

    }
*/
/*
    private void checkElements (int start,
                                int length)
    {

        int end = start + length;

        Document document = this.text.getDocument ();

        Element element;

        do
        {

            try
            {

                element = ((AbstractDocument) document).getParagraphElement (start);

            } catch (Exception ex)
            {

                return;

            }

            this.checkElement (element);

            start = element.getEndOffset ();

        } while ((start <= end) && (start < document.getLength ()));

    }
*/
/*
    public void checkAll ()
    {

        Document document = this.text.getDocument ();

        for (int i = 0; i < document.getLength ();)
        {

            Element el = null;

            try
            {

                el = ((AbstractDocument) document).getParagraphElement (i);

            } catch (Exception e)
            {

                // Need to funnel this back somewhere!

            }

            this.checkElements (i);

            i = el.getEndOffset ();

        }

    }
*/
/*
    private void clearAllHighlights ()
    {

        this.text.removeAllHighlights (this.painter);

    }
*/
/*
    public void checkElement (Element el)
    {

        this.text.removeHighlightsForElement (el,
                                              this.painter);

        if (!this.enabled)
        {

            return;

        }

        int start = el.getStartOffset ();

        int docLength = this.text.getDocument ().getLength ();

        int end = el.getEndOffset ();

        if (start == end)
        {

            return;

        }

        TextIterator ti = new TextIterator (this.text.getText ().substring (start,
                                                                            end - 1));

        List<Word> words = ti.getWords ();

        for (Word w : words)
        {

            if (w.isPunctuation ())
            {

                continue;

            }

            //String word = w.getText ();

            if (!this.isWordCorrect (w))
            {

                try
                {

                    int wordStart = w.getAllTextStartOffset () + start;
                    int wordEnd = w.getAllTextEndOffset () + start;

                    this.text.addHighlight (wordStart,
                                            wordEnd,
                                            this.painter,
                                            false);

                } catch (Exception e)
                {

                }

            }

        }

    }
*/
    public boolean isEnabled ()
    {

        return this.enabled;

    }

    private void checkWordInParagraph (int paraInd,
                                       int wordPos)
    {

        TextIterator ti = new TextIterator (this.text.getParagraphs ().get (paraInd).getText ());

        IndexRange r = this.text.getParagraphTextRange (paraInd);

        Word w = ti.getWordAt (wordPos);

        this.checkWordInParagraph (r.getStart (),
                                   w);

    }

    private void checkWordInParagraph (int  paraStartOffset,
                                       Word w)
    {

        if (w == null)
        {

            return;

        }

        int wordStart = w.getAllTextStartOffset () + paraStartOffset;

        int l = this.text.getText ().length ();

        if ((wordStart >= l)
            ||
            (wordStart + w.getText ().length () >= l)
           )
        {

            return;

        }

        if (w.isPunctuation ())
        {

            this.removeSpellingError (new IndexRange (wordStart, wordStart + w.getText ().length ()));

            this.checkWordInParagraph (paraStartOffset,
                                       w.getPrevious ());

        } else {

            if (!this.isWordCorrect (w))
            {

                this.addSpellingError (new IndexRange (wordStart, wordStart + w.getText ().length ()));

            } else {

                this.removeSpellingError (new IndexRange (wordStart, wordStart + w.getText ().length ()));
            }

        }

    }

    private void removeSpellingError (IndexRange r)
    {

        UIUtils.runLater (() ->
        {

            this.text.removeSpellingError (r);

        });

    }

    private void addSpellingError (IndexRange r)
    {

        UIUtils.runLater (() ->
        {

            this.text.addSpellingError (r);

        });

    }

    public void enable (boolean v)
    {

        boolean turningOn = (!this.enabled && v);

        this.enabled = v;

        // Conditions
        //   1. Leave word - move caret
        //   2. type punctuation
        //   3. space
        //   4. newline

        if (turningOn)
        {

            this.text.caretPositionProperty ().addListener ((pr, oldv, newv) ->
            {

                if (!this.isEnabled ())
                {

                    return;

                }

                Environment.schedule (() ->
                {

                    int oldp = this.text.getParagraphForOffset (oldv);
                    int newp = this.text.getParagraphForOffset (newv);
                    int olds = 0;

                    Word ow = null;
                    Word nw = null;

                    if (newp != -1)
                    {

                        TextIterator nti = new TextIterator (this.text.getParagraphs ().get (newp).getText ());
                        IndexRange r = this.text.getParagraphTextRange (newp);
                        nw = nti.getWordAt (newv - r.getStart ());

                        if (nw == null)
                        {

                            if (newv - r.getStart () > -1)
                            {

                                nw = nti.getWordAt (newv - r.getStart () - 1);

                            }

                        }

                    }

                    if (oldp != -1)
                    {

                        TextIterator oti = new TextIterator (this.text.getParagraphs ().get (oldp).getText ());
                        IndexRange r = this.text.getParagraphTextRange (oldp);
                        olds = r.getStart ();
                        ow = oti.getWordAt (oldv - olds);

                        if (ow == null)
                        {

                            if (oldv - r.getStart () > -1)
                            {

                                ow = oti.getWordAt (oldv - r.getStart () - 1);

                            }

                        }

                    }

                    if (ow != null)
                    {

                        if (!ow.equals (nw))
                        {

                            this.checkWordInParagraph (olds,
                                                       ow);
                            return;

                        }

                    }

                },
                -1,
                -1);

            });

            this.subscription = this.text.plainTextChanges ().subscribe (change ->
            {

                Environment.schedule (() ->
                {

                    this.handleTextChange (change);

                },
                -1,
                -1);

            });

            this.checkAll ();

        } else
        {

            if (this.subscription != null)
            {

                this.subscription.unsubscribe ();

            }

            this.text.clearAllSpellingErrors ();

        }

    }

    private void handleTextChange (PlainTextChange change)
    {

        String ins = change.getInserted ();

        if ((ins != null)
            &&
            (!"".equals (ins))
           )
        {

            if (ins.length () == 1)
            {

                if (!Character.isLetterOrDigit (ins.charAt (0)))
                {

                    // A new line, space or punctuation character has been entered.  Check the previous word.
                    int pos = change.getPosition ();

                    while (pos > 0)
                    {

                        // Find the first previous word.
                        if (!Character.isLetterOrDigit (this.text.getText ().charAt (pos)))
                        {

                            this.removeSpellingError (new IndexRange (pos, pos + 1));
                            pos--;
                            continue;

                        }

                        // Found a letter or digit.
                        int pi = this.text.getParagraphForOffset (pos);

                        TextIterator ti = new TextIterator (this.text.getParagraphs ().get (pi).getText ());

                        IndexRange r = this.text.getParagraphTextRange (pi);

                        Word w = ti.getWordAt (pos - r.getStart ());

                        this.checkWordInParagraph (r.getStart (),
                                                   w);

                        return;

                    }

                }

            } else {

                // Inserted a lot of text.  Check affected paragraphs.
                int pi = text.getParagraphForOffset (change.getPosition ());

                // Get the end paragraph.
                int epi = text.getParagraphForOffset (change.getRemovalEnd ());
                int e2pi = text.getParagraphForOffset (change.getInsertionEnd ());

                epi = Math.max (epi, e2pi);

                for (int i = pi; i < epi + 1; i++)
                {

                    this.checkParagraph (i);

                }

            }

        }

    }

    private static class Range
    {

        public int from = -1;
        public int to = -1;
        public boolean textChange = false;

        public Range (int from,
                      int to,
                      boolean textChange)
        {

            this (from,
                  to);

            this.textChange = textChange;

        }

        public Range (int from,
                      int to)
        {

            this.from = from;
            this.to = to;

        }

        public String toString ()
        {

            return "range: " + this.from + " : " + this.to;

        }

    }

}
