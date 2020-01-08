package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.concurrent.*;

import javafx.geometry.*;
import javafx.scene.control.*;

import org.reactfx.*;

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
    private Deque<Range> rangesToCheck = new ConcurrentLinkedDeque<> ();

    public QSpellChecker(TextEditor          text,
                         DictionaryProvider2 prov)
    {

        this.text = text;

        this.setDictionaryProvider (prov);

        Environment.schedule (() ->
        {

            if (text.isBeingUpdated ())
            {

                return;

            }
UIUtils.runLater (() ->
{
            IndexRange r = null;

            while (!this.rangesToCheck.isEmpty ())
            {

                this.checkRange (this.rangesToCheck.pop ());

            }
});
        },
        100,
        100);

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
    //System.out.println ("W: " + w.getAllTextStartOffset () + ", " + ir.getStart () + ", " + w.getAllTextEndOffset () + ", " + ir.getEnd ());
                        int wordStart = w.getAllTextStartOffset () + start; //ir.getStart ();
                        int wordEnd = w.getAllTextEndOffset () + start; //ir.getStart ();
    //System.out.println ("S: " + wordStart + ", " + w.getText ().length ());
                        errors.add (new IndexRange (wordStart, wordStart + w.getText ().length ()));
                        //this.text.addSpellingError (new IndexRange (wordStart, wordStart + w.getText ().length ()));

                    } catch (Exception e)
                    {
    e.printStackTrace ();
                    }

                } else {

                    try
                    {

                        int wordStart = w.getAllTextStartOffset () + start;//ir.getStart ();
                        int wordEnd = w.getAllTextEndOffset () + start; //ir.getStart ();

                        //this.text.removeSpellingError (new IndexRange (wordStart, wordStart + w.getText ().length ()));

                    } catch (Exception e)
                    {
    e.printStackTrace ();
                    }

                }

            }

            this.text.setSpellingErrors (start,
                                         pr.getEnd (),
                                         errors);

        },
        -1,
        -1);

    }

    public void checkAll ()
    {

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

        if (w.isPunctuation ())
        {

            this.text.removeSpellingError (new IndexRange (wordStart, wordStart + w.getText ().length ()));

            this.checkWordInParagraph (paraStartOffset,
                                       w.getPrevious ());

        } else {

            if (!this.isWordCorrect (w))
            {

                this.text.addSpellingError (new IndexRange (wordStart, wordStart + w.getText ().length ()));

            } else {

                this.text.removeSpellingError (new IndexRange (wordStart, wordStart + w.getText ().length ()));
            }

        }

    }

    private void checkRange (Range rr)
    {

        int op = this.text.getParagraphForOffset (rr.from);
        int np = this.text.getParagraphForOffset (rr.to);

        int diff = rr.from - rr.to;

        if ((op != np)
            &&
            (rr.textChange)
            &&
            (Math.abs (rr.from - rr.to) > 1)
           )
        {

            // Check all the affected paragraphs.
            int min = Math.min (op, np);
            int max = Math.max (op, np);

            for (int i = min; i < max + 1; i++)
            {

                this.checkParagraph (i);

            }

        } else {

            IndexRange or = this.text.getParagraphTextRange (op);

            this.checkWordInParagraph (op,
                                       rr.from - or.getStart ());

           IndexRange nr = this.text.getParagraphTextRange (np);

           this.checkWordInParagraph (np,
                                      rr.to - nr.getStart ());

        }

        if (true) {return;}

        int oldp = this.text.getParagraphForOffset (rr.from);
        int newp = this.text.getParagraphForOffset (rr.to);
System.out.println ("CHECKING RANGE: " + rr);
        if (oldp != newp)
        {

            // Check the old word.
            this.checkWordInParagraph (op,
                                       rr.from);
            return;

        } else {

            TextIterator ti = new TextIterator (this.text.getParagraphs ().get (newp).getText ());

            IndexRange r = this.text.getParagraphTextRange (newp);

            Word ow = ti.getWordAt (rr.from - r.getStart ());
            Word nw = ti.getWordAt (rr.to - r.getStart ());

            if (ow == nw)
            {
System.out.println ("RET");
                return;

            }
System.out.println ("OW: " + ow + ", " + nw);
            char nc = this.text.getText ().charAt (rr.to - 1);

            if ((Character.isWhitespace (nc))
                ||
                (!Character.isLetterOrDigit (nc))
               )
            {

                if (ow != null)
                {

                    this.checkWordInParagraph (r.getStart (),
                                               ow);

                }

                return;

            }

            if ((ow == null)
                &&
                (nw == null)
               )
            {

                return;

            }

            if (ow == nw)
            {

                this.text.removeSpellingError (new IndexRange (r.getStart (), r.getStart () + nw.getText ().length ()));
                return;

            }

            if (ow != null)
            {

                IndexRange rrr = this.text.getParagraphTextRange (oldp);

                if ((rr.from < r.getStart () + ow.getAllTextStartOffset ())
                    ||
                    ((rr.to > r.getStart () + ow.getAllTextEndOffset ()))
                   )
                {

                    this.checkWordInParagraph (r.getStart (),
                                               ow);
                    return;

                }

            }

            if ((nw != null)
                &&
                (nw.isPunctuation ())
               )
            {

                this.checkWordInParagraph (r.getStart (),
                                           nw);
                return;

            }

            if ((ow != null)
                &&
                (ow.isPunctuation ())
               )
            {

                this.checkWordInParagraph (r.getStart (),
                                           ow);
                return;

            }

        }

    }

    public void enable (boolean v)
    {

        boolean turningOn = (!this.enabled && v);

        this.enabled = v;

        if (turningOn)
        {

            this.text.caretPositionProperty ().addListener ((pr, oldv, newv) ->
            {

                this.rangesToCheck.add (new Range (oldv, newv));
/*
                int oldp = this.text.getParagraphForOffset (oldv);
                int newp = this.text.getParagraphForOffset (newv);
System.out.println ("CARET: " + oldv + ", " + newv);
                if (oldp != newp)
                {

                    // Check the old word.
                    this.checkWordInParagraph (oldp,
                                               oldv);
                    return;

                } else {

                    TextIterator ti = new TextIterator (this.text.getParagraphs ().get (newp).getText ());

                    IndexRange r = this.text.getParagraphTextRange (newp);

                    Word ow = ti.getWordAt (oldv - r.getStart ());
                    Word nw = ti.getWordAt (newv - r.getStart ());

                    char nc = this.text.getText ().charAt (newv - 1);

                    if ((Character.isWhitespace (nc))
                        ||
                        (!Character.isLetterOrDigit (nc))
                       )
                    {

                        if (ow != null)
                        {

                            this.checkWordInParagraph (r.getStart (),
                                                       ow);

                        }

                        return;

                    }

                    if ((ow == null)
                        &&
                        (nw == null)
                       )
                    {

                        return;

                    }

                    if (ow == nw)
                    {

                        this.text.removeSpellingError (new IndexRange (r.getStart (), r.getStart () + nw.getText ().length ()));
                        return;

                    }

                    if (ow != null)
                    {

                        if ((newv < r.getStart () + ow.getAllTextStartOffset ())
                            ||
                            ((newv > r.getStart () + ow.getAllTextEndOffset ()))
                           )
                        {

                            this.checkWordInParagraph (r.getStart (),
                                                       ow);
                            return;

                        }

                    }

                    if ((nw != null)
                        &&
                        (nw.isPunctuation ())
                       )
                    {

                        this.checkWordInParagraph (r.getStart (),
                                                   nw);
                        return;

                    }

                    if ((ow != null)
                        &&
                        (ow.isPunctuation ())
                       )
                    {

                        this.checkWordInParagraph (r.getStart (),
                                                   ow);
                        return;

                    }

                }
*/
            });

            this.subscription = this.text.plainTextChanges ().subscribe (change ->
            {

                this.rangesToCheck.add (new Range (change.getPosition (),
                                                   change.getPosition () + change.getNetLength (),
                                                   true));

                if (true) {return;}

                if (change.getNetLength () == 1)
                {
/*
                    // Typed a single character.
                    if (Character.isWhitespace (this.text.getText ().charAt (change.getPosition ())))
                    {

                        // Added a word.
                        this.text.removeSpellingError (new IndexRange (change.getPosition (), change.getPosition () + 1));

                    }

                    int pi = text.getParagraphForOffset (change.getPosition ());

                    // Check previous word.
                    TextIterator ti = new TextIterator (this.text.getParagraphs ().get (pi).getText ());

                    IndexRange r = this.text.getParagraphTextRange (pi);

                    Word w = ti.getWordAt (change.getPosition () - r.getStart () - 1);

                    this.checkWordInParagraph (r.getStart (),
                                               w);
*/
                } else {

                    // Get previous word.
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

                if (true) {return;}

                // Check the range.
                int pi = text.getParagraphForOffset (change.getPosition ());

                // Get the end paragraph.
                int epi = text.getParagraphForOffset (change.getRemovalEnd ());
                int e2pi = text.getParagraphForOffset (change.getInsertionEnd ());

                epi = Math.max (epi, e2pi);

                if (Character.isWhitespace (this.text.getText ().charAt (change.getPosition ())))
                {

                    try
                    {

                        this.text.removeSpellingError (new IndexRange (change.getPosition (), change.getPosition () + 1));

                    } catch (Exception e)
                    {

                    }

                    for (int i = pi; i < epi + 1; i++)
                    {

                        this.checkParagraph (i);

                    }

                }

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
/*
    public void insertUpdate (DocumentEvent ev)
    {

        if (!this.enabled)
        {

            return;

        }

        if (ev.getLength () == 1)
        {

            this.lastCharacterOver = QSpellChecker.NULL_CHAR;

        }

        if (ev.getLength () == 1)
        {

            String t = null;

            try
            {

                t = ev.getDocument ().getText (ev.getOffset (),
                                               ev.getLength ());

            } catch (Exception e) {

                // Wtf...
                return;

            }

            if (t.trim ().length () == 0)
            {

                // Check the previous element.
                Element element;

                try
                {

                    element = ((AbstractDocument) ev.getDocument ()).getParagraphElement (ev.getOffset ());

                } catch (Exception ex)
                {

                    return;

                }

                this.checkElement (element);

            }

        } else {

            this.checkElements (ev.getOffset (),
                                ev.getLength ());

        }

    }
*/
/*
    public void changedUpdate (DocumentEvent ev)
    {

    }
*/
/*
    public void removeUpdate (DocumentEvent ev)
    {

        if (!this.enabled)
        {

            return;

        }

        if (ev.getOffset () == 0)
        {

            return;

        }

        if (ev.getLength () == 1)
        {

            this.lastCharacterOver = QSpellChecker.NULL_CHAR;

        }

        if (ev.getLength () == 1)
        {

            // Check the previous char, if there is one then don't check, if it's whitespace then check.
            if (ev.getOffset () > 0)
            {

                String t = null;

                try
                {

                    t = ev.getDocument ().getText (ev.getOffset () - 1,
                                                   ev.getLength ());

                } catch (Exception e) {

                    // Wtf...
                    return;

                }

                // Check the previous element.
                Element element;

                try
                {

                    element = ((AbstractDocument) ev.getDocument ()).getParagraphElement (ev.getOffset ());

                } catch (Exception ex)
                {

                    return;

                }

                this.checkElement (element);

                return;

            }

        } else {

            this.checkElements (ev.getOffset (),
                                ev.getLength ());

        }

    }
*/
/*
    public void caretUpdate (CaretEvent ev)
    {

        if (!this.enabled)
        {

            return;

        }

        if (this.lastCaret == -1)
        {

            this.lastCaret = ev.getDot ();

            return;

        }

        Document document = this.text.getDocument ();

        try
        {

            Element oldEl = ((AbstractDocument) document).getParagraphElement (this.lastCaret);
            Element newEl = ((AbstractDocument) document).getParagraphElement (ev.getDot ());

            if (oldEl != newEl)
            {

                this.checkElements (this.lastCaret);

            } else
            {

                boolean d = false;

                if ((Math.max (this.lastCaret,
                               ev.getDot ()) - Math.min (this.lastCaret,
                                                         ev.getDot ())) > 1)
                {

                    this.checkElement (newEl);

                } else
                {

                    if (this.lastCharacterOver != QSpellChecker.NULL_CHAR)
                    {

                        String text = this.text.getText ();

                        if ((text.length () == 0) ||
                            (ev.getDot () > (text.length () - 1)))
                        {

                            return;

                        }

                        if ((Character.isWhitespace (this.lastCharacterOver)) &&
                            (Character.isWhitespace (text.charAt (ev.getDot ()))))
                        {

                            d = true;

                        }

                        try
                        {

                            if (ev.getDot () < this.lastCaret)
                            {

                                if ((!Character.isWhitespace (this.lastCharacterOver)) &&
                                    (Character.isWhitespace (text.charAt (ev.getDot ()))))
                                {

                                    d = true;

                                }

                            } else
                            {

                                if (Character.isWhitespace (this.lastCharacterOver))
                                {

                                    d = true;

                                }

                            }

                        } catch (Exception e)
                        {

                            com.quollwriter.Environment.logError ("HERE: ",
                                                                  e);

                        }

                    }

                }

                if (d)
                {

                    this.checkElement (newEl);

                }

            }

        } catch (Exception e)
        {

            return;

        }

        this.lastCaret = ev.getDot ();

        String t = this.text.getText ();

        if ((this.lastCaret >= t.length ()) ||
            (this.lastCaret < 0))
        {

            return;

        }

        this.lastCharacterOver = this.text.getText ().charAt (this.lastCaret);

    }
*/

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
