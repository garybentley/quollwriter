package com.quollwriter.text.rules;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.text.*;

import com.quollwriter.ui.components.*;

import org.jdom.Element;
import org.jdom.JDOMException;


public class SentenceLengthRule extends AbstractSentenceRule
{

    public class XMLConstants
    {

        public static final String wordCount = "wordCount";

    }

    private int      wordCount = 0;
    private JSpinner count = null;

    public SentenceLengthRule ()
    {

    }

    public SentenceLengthRule (int     wordCount,
                               boolean user)
    {

        this.wordCount = wordCount;
        this.setUserRule (user);

    }

    @Override
    public String getDescription ()
    {

        String d = super.getDescription ();

        return StringUtils.replaceString (d,
                                          "[LIMIT]",
                                          this.wordCount + "");

    }

    @Override
    public String getSummary ()
    {

        return StringUtils.replaceString (super.getSummary (),
                                          "[LIMIT]",
                                          this.wordCount + "");

    }

    @Override
    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        this.wordCount = JDOMUtils.getAttributeValueAsInt (root,
                                                           XMLConstants.wordCount);

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.setAttribute (XMLConstants.wordCount,
                           this.wordCount + "");

        return root;

    }

    @Override
    public List<Issue> getIssues (Sentence sentence)
    {

        // Check each word to make sure it's not punctuation.
        List<Issue> issues = new ArrayList ();

        int wc = sentence.getWordCount ();

        if (wc > this.wordCount)
        {

            Issue iss = new Issue ("Sentence contains: <b>" + wc + "</b> words.",
                                   sentence,
                                   sentence.getAllTextStartOffset (),
                                   sentence.getLastWord ().getAllTextEndOffset () - sentence.getAllTextStartOffset (),
                                   sentence.getAllTextStartOffset () + "-toomanywords-" + wc,
                                   this);

            issues.add (iss);

        }

        return issues;

    }

    public String getCategory ()
    {

        return Rule.SENTENCE_CATEGORY;

    }

    public List<FormItem> getFormItems ()
    {

        List<FormItem> items = new ArrayList ();

        this.count = new JSpinner (new SpinnerNumberModel (this.wordCount,
                                                           1,
                                                           200,
                                                           1));

        Box b = new Box (BoxLayout.X_AXIS);
        b.add (this.count);
        b.add (Box.createHorizontalGlue ());

        this.count.setMaximumSize (this.count.getPreferredSize ());

        items.add (new FormItem ("No of Words",
                                 b));

        return items;

    }

    public String getFormError ()
    {

        return null;

    }

    public void updateFromForm ()
    {

        this.wordCount = ((SpinnerNumberModel) this.count.getModel ()).getNumber ().intValue ();

    }

}
