package com.quollwriter.text.rules;

import java.util.*;
import java.awt.event.*;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;
import com.quollwriter.text.*;

import com.quollwriter.ui.components.*;

import org.jdom.*;


public class WordFinder extends AbstractDialogueRule
{

    public class XMLConstants
    {

        public static final String word = "word";

    }

    private String word = null;

    private JTextField words = null;
    private List<Word> tWords = null;
    private JCheckBox ignoreInDialogueCB = null;
    private JCheckBox onlyInDialogueCB = null;
    private JComboBox whereCB = null;

    public WordFinder ()
    {

    }

    public boolean isForLanguage (String language)
    {

        return Environment.isEnglish (language);

    }

    public String toString ()
    {

        return "rule(word=" + this.word + ")";

    }

    public String getCategory ()
    {

        return Rule.WORD_CATEGORY;

    }

    public void setWord (String w)
    {

        this.word = w;

        this.tWords = TextUtilities.getAsWords (this.word.toLowerCase ());

    }

    @Override
    public String getSummary ()
    {

        if (this.word == null)
        {

            return null;

        }

        return String.format ("\"<b>%s</b>\" %s",
                              this.word,
                              this.getWhereDesc ());

    }

    private String getWhereDesc ()
    {

        String suffix = "";

        if (this.where != null)
        {

            if (this.where.equals (DialogueConstraints.ANYWHERE))
            {

                suffix = "anywhere in a sentence";

            } else
            {

                suffix = String.format ("at the %s of a sentence",
                                        this.where);

            }

            if (this.ignoreInDialogue)
            {

                suffix += ", ignore in dialogue";

            }

            if (this.onlyInDialogue)
            {

                suffix += ", but only in dialogue";

            }

        }

        return suffix;

    }

    @Override
    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        this.setWord (JDOMUtils.getAttributeValue (root,
                                                   XMLConstants.word));

    }

    public String getWord ()
    {

        return this.word;

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.setAttribute (XMLConstants.word,
                           this.word);

        return root;

    }

    @Override
    public List<Issue> getIssues (Sentence sentence)
    {

        // Check our list of words.

        List<Issue>  issues = new ArrayList ();

        Set<Integer> inds = sentence.find (this.tWords,
                                           this.getConstraints ());

        if ((inds != null)
            &&
            (inds.size () > 0)
           )
        {

            for (Integer i : inds)
            {

                Word w = sentence.getWord (i);

                int l = w.getText ().length ();

                if (this.tWords.size () > 1)
                {

                    Word nw = w.getWordsAhead (this.tWords.size () - 1);

                    l = nw.getAllTextEndOffset () - w.getAllTextStartOffset ();

                }

                String suffix = "";

                if (this.onlyInDialogue)
                {

                    suffix = " (in dialogue)";

                }

                if (!this.where.equals (DialogueConstraints.ANYWHERE))
                {

                    suffix = String.format (" (%s of sentence)",
                                            this.where);

                    if (this.onlyInDialogue)
                    {

                        suffix = String.format (" (%s of sentence, in dialogue)",
                                                this.where);

                    }

                }

                Issue iss = new Issue ("Contains: <b>" + this.word + "</b>" + suffix,
                                       sentence,
                                       w.getAllTextStartOffset (),
                                       l,
                                       w.getAllTextStartOffset () + "-" + this.word,
                                       this);
                issues.add (iss);

            }

        }

        return issues;

    }

    public void updateFromForm ()
    {

        this.setOnlyInDialogue (this.onlyInDialogueCB.isSelected ());
        this.setIgnoreInDialogue (this.ignoreInDialogueCB.isSelected ());

        int ws = this.whereCB.getSelectedIndex ();

        if (ws == 0)
        {

            this.setWhere (DialogueConstraints.ANYWHERE);

        }

        if (ws == 1)
        {

            this.setWhere (DialogueConstraints.START);

        }

        if (ws == 2)
        {

            this.setWhere (DialogueConstraints.END);

        }

        this.setWord (this.words.getText ().trim ());

    }

    @Override
    public String getDescription ()
    {

        String d = super.getDescription ();

        if (d == null)
        {

            return null;

        }

        return StringUtils.replaceString (d,
                                          "[WORD]",
                                          ((this.word == null) ? "[WORD]" : this.word));

    }

    public List<FormItem> getFormItems ()
    {

        List<FormItem> items = new ArrayList ();

        this.words = com.quollwriter.ui.UIUtils.createTextField ();

        items.add (new FormItem ("Word/Phrase",
                                 this.words));

        this.words.setText (this.word);

        Vector whereVals = new Vector ();
        whereVals.add ("Anywhere");
        whereVals.add ("Start of sentence");
        whereVals.add ("End of sentence");

        final WordFinder _this = this;

        this.whereCB = new JComboBox (whereVals);

        String loc = this.getWhere ();

        if (loc.equals (DialogueConstraints.START))
        {

            this.whereCB.setSelectedIndex (1);

        }

        if (loc.equals (DialogueConstraints.END))
        {

            this.whereCB.setSelectedIndex (2);

        }

        items.add (new FormItem ("Where",
                                 this.whereCB));

        this.ignoreInDialogueCB = new JCheckBox ("Ignore in dialogue");
        this.onlyInDialogueCB = new JCheckBox ("Only in dialogue");

        this.ignoreInDialogueCB.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (_this.ignoreInDialogueCB.isSelected ())
                {

                    _this.onlyInDialogueCB.setSelected (false);

                }

            }

        });

        this.onlyInDialogueCB.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (_this.onlyInDialogueCB.isSelected ())
                {

                    _this.ignoreInDialogueCB.setSelected (false);

                }

            }

        });

        this.ignoreInDialogueCB.setSelected (this.isIgnoreInDialogue ());
        this.onlyInDialogueCB.setSelected (this.isOnlyInDialogue ());

        items.add (new FormItem (null,
                                 this.ignoreInDialogueCB));

        items.add (new FormItem (null,
                                 this.onlyInDialogueCB));

        return items;

    }

    public String getFormError ()
    {

        String newWords = words.getText ().trim ();

        if (newWords.length () == 0)
        {

            return "Please enter at least one word or symbol.";

        }

        return null;

    }

}
