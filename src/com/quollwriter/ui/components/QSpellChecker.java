package com.quollwriter.ui.components;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.*;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.WeakHashMap;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.quollwriter.text.*;
import com.quollwriter.BlankTimerTask;
import com.quollwriter.DictionaryProvider;
import com.quollwriter.synonyms.SynonymProvider;

import com.quollwriter.ui.events.DictionaryChangedEvent;
import com.quollwriter.ui.events.DictionaryChangedListener;

public class QSpellChecker implements DocumentListener,
                                      CaretListener,
                                      DictionaryChangedListener
{

    private static final char NULL_CHAR = (char) 0;

    private QTextEditor        text = null;
    private SpellChecker       checker = null;
    private LinePainter        painter = new LinePainter (Color.RED);
    private Map                elsToCheck = new WeakHashMap ();
    private boolean            enabled = false;
    private int                lastCaret = -1;
    private char               lastCharacterOver = QSpellChecker.NULL_CHAR;
    private DictionaryProvider dictProvider = null;
    private SynonymProvider    synonymProvider = null;

    public QSpellChecker(QTextEditor        text,
                         DictionaryProvider prov)
    {

        this.text = text;

        this.text.getDocument ().addDocumentListener (this);
        this.text.addCaretListener (this);

        this.setDictionaryProvider (prov);

    }

    public SynonymProvider getSynonymProvider ()
    {

        return this.synonymProvider;

    }

    public void setSynonymProvider (SynonymProvider sp)
    {

        this.synonymProvider = sp;

    }

    public void setDictionaryProvider (DictionaryProvider dp)
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

        //this.checker = new SpellChecker ();

        this.dictProvider = dp;
        this.dictProvider.addDictionaryChangedListener (this);
/*
        List dicts = dp.getDictionaries ();

        for (int i = 0; i < dicts.size (); i++)
        {

            this.checker.addDictionary ((SpellDictionaryHashMap) (dicts.get (i)));

        }

        this.checker.setUserDictionary (dp.getUserDictionary ());

        this.dictProvider = dp;
        this.dictProvider.addDictionaryChangedListener (this);
*/
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

    public void addWord (String w,
                         String type)
    {

        this.dictProvider.addWord (w,
                                   type);

    }

    public List getSuggestions (Point p)
    {

        TextIterator ti = new TextIterator (this.text.getText ());

        int start = this.text.viewToModel (p);

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

    private void clearAllHighlights ()
    {

        this.text.removeAllHighlights (this.painter);

    }

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
        /*
        int wordStart = -1;
        int wordEnd = -1;

        while ((docTok.hasMoreWords ()) &&
               (docTok.getCurrentWordPosition () <= end))
        {

            String word = docTok.nextWord ();

            wordStart = docTok.getCurrentWordPosition ();

            wordEnd = docTok.getCurrentWordEnd ();

            if (wordEnd > docLength)
            {

                wordEnd = docLength - 1;

            }

            if (wordStart >= wordEnd)
            {

                continue;

            }

            if (!this.isWordCorrect (word))
            {

                try
                {

                    this.text.addHighlight (wordStart,
                                            wordEnd,
                                            this.painter,
                                            false);

                } catch (Exception e)
                {

                }

            }

        }
        */

    }

    public boolean isEnabled ()
    {

        return this.enabled;

    }

    public void enable (boolean v)
    {

        boolean turningOn = (!this.enabled && v);

        this.enabled = v;

        if (turningOn)
        {

            this.checkAll ();

        } else
        {

            this.clearAllHighlights ();

        }

    }

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

    public void changedUpdate (DocumentEvent ev)
    {

    }

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

}
