package com.quollwriter.text.rules;

import java.util.*;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.text.*;

import com.quollwriter.ui.components.*;

import org.jdom.*;


public class WordFinder extends AbstractDialogueRule
{

    public static final String CREATE_TYPE = "wordFinder";

    public class XMLConstants
    {

        public static final String word = "word";

    }

    // private List words = null;

    private String word = null;

    private JTextField words = null;

    public WordFinder(boolean user)
    {

        super (user);
        /*
          this.words = new ArrayList ();
          this.words.add ("that");
          this.words.add (RuleUtilities.getAsWords ("there are"));
          this.words.add (RuleUtilities.getAsWords ("there were"));
          this.words.add (RuleUtilities.getAsWords ("there was"));
          this.words.add ("feeling");
          this.words.add ("feel");
          this.words.add ("seem");
          this.words.add ("seemed");
         */
        // That
        // There are
        // There was
        // There were
        // It is
        // It was
        // Feel
        // Feeling
        // Seem
        // Seemed

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

    }

    public String getSummary ()
    {

        StringBuilder b = new StringBuilder ("\"" + this.word + "\" ");

        if (this.where != null)
        {

            if (this.where.equals (DialogueConstraints.ANYWHERE))
            {

                b.append ("anywhere in a sentence");

            } else
            {

                b.append ("at the " + this.where + " of a sentence");

            }

        }

        return b.toString ();

    }

    public String getCreateType ()
    {

        return WordFinder.CREATE_TYPE;

    }

    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        this.word = JDOMUtils.getAttributeValue (root,
                                                 XMLConstants.word);

    }

    public String getWord ()
    {

        return this.word;

    }

    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.setAttribute (XMLConstants.word,
                           this.word);

        return root;

    }

    public List<Issue> getIssues (String  sentence,
                                  boolean inDialogue)
    {

        // Check our list of words.
        sentence = sentence.toLowerCase ();

        List<String> swords = TextUtilities.getAsWords (sentence);
        List<Issue>  issues = new ArrayList ();

        List<Integer> inds = TextUtilities.indexOf (swords,
                                                    TextUtilities.getAsWords (this.word.toLowerCase ()),
                                                    inDialogue,
                                                    this.getConstraints ());

        if (inds != null)
        {

            for (Integer i : inds)
            {

                Issue iss = new Issue ("Contains: <b>" + this.word + "</b>",
                                       i,
                                       (this.word + "").length (),
                                       this);
                issues.add (iss);

            }

        }

/*
There is an obvious distinction between the two.
Obviously the distinction between one and the other is significant.
There is a(1) distinction between them both.
There is an obvious difference(1) between the two of(2) them(3).
*/
        return issues;

    }

    public void updateFromForm ()
    {

        if (this.words == null)
        {

            return;

        }

        this.word = this.words.getText ();

    }

    public String getDescription ()
    {

        String d = super.getDescription ();

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

        return items;

    }

}
