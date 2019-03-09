package com.quollwriter.text.rules;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;
import com.quollwriter.text.*;

import com.quollwriter.ui.forms.*;

import org.jdom.Element;
import org.jdom.JDOMException;

public class ParagraphLengthRule extends AbstractParagraphRule
{

    public class XMLConstants
    {

        public static final String wordCount = "wordCount";
        public static final String sentenceCount = "sentenceCount";

    }

    private int sentenceCount = 0;
    private int wordCount = 0;
    private JSpinner sentCountF = null;
    private JSpinner wordCountF = null;

    public ParagraphLengthRule ()
    {

    }

    public ParagraphLengthRule (int     sentenceCount,
                                int     wordCount,
                                boolean user)
    {

        this.sentenceCount = sentenceCount;
        this.wordCount = wordCount;
        this.setUserRule (user);

    }

    public int getSentenceCount ()
    {

        return this.sentenceCount;

    }

    public void setSentenceCount (int c)
    {

        this.sentenceCount = c;

    }

    public int getWordCount ()
    {

        return this.wordCount;

    }

    public void setWordCount (int c)
    {

        this.wordCount = c;

    }

    @Override
    public String getDescription ()
    {

        String d = super.getDescription ();

        d = StringUtils.replaceString (d,
                                       "[SENTENCE_COUNT]",
                                       this.sentenceCount + "");

        d = StringUtils.replaceString (d,
                                       "[WORD_COUNT]",
                                       this.wordCount + "");

        return d;

    }

    @Override
    public String getSummary ()
    {

        String t = StringUtils.replaceString (super.getSummary (),
                                              "[SENTENCE_COUNT]",
                                              this.sentenceCount + "");

        t = StringUtils.replaceString (t,
                                       "[WORD_COUNT]",
                                       this.wordCount + "");

        return t;

    }

    @Override
    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        this.sentenceCount = JDOMUtils.getAttributeValueAsInt (root,
                                                               XMLConstants.sentenceCount);
        this.wordCount = JDOMUtils.getAttributeValueAsInt (root,
                                                           XMLConstants.wordCount);

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.setAttribute (XMLConstants.sentenceCount,
                           this.sentenceCount + "");
        root.setAttribute (XMLConstants.wordCount,
                           this.wordCount + "");

        return root;

    }

    @Override
    public List<Issue> getIssues (Paragraph paragraph)
    {

        List<String> pref = new ArrayList<> ();
        pref.add (LanguageStrings.problemfinder);
        pref.add (LanguageStrings.issues);
        pref.add (LanguageStrings.paragraphlength);

        List<Issue> issues = new ArrayList<> ();

        int wc = paragraph.getWordCount ();

        if ((this.wordCount > 0)
            &&
            (wc > this.wordCount)
           )
        {

            Issue iss = new Issue (String.format (Environment.getUIString (pref,
                                                                           LanguageStrings.wordtext),
                                                  Environment.formatNumber (wc),
                                                  Environment.formatNumber (this.wordCount)),
                                                                           //"Paragraph word count is: <b>" + Environment.formatNumber (wc) + "</b>.  (Max is: " + Environment.formatNumber (this.wordCount) + ")",
                                   paragraph,
                                   paragraph.getAllTextStartOffset () + "-wordcount-" + wc,
                                   this);

            issues.add (iss);

        }

        int sc = paragraph.getSentenceCount ();

        if ((this.sentenceCount > 0)
            &&
            (sc > this.sentenceCount)
           )
        {

            Issue iss = new Issue (String.format (Environment.getUIString (pref,
                                                                           LanguageStrings.sentencetext),
                                                  Environment.formatNumber (sc),
                                                  Environment.formatNumber (this.sentenceCount)),
                                                  //"Paragraph sentence count is: <b>" + Environment.formatNumber (sc) + "</b>.  (Max is: " + Environment.formatNumber (this.sentenceCount) + ")",
                                   paragraph,
                                   paragraph.getAllTextStartOffset () + "-sentencecount-" + sc,
                                   this);

            issues.add (iss);

        }

        return issues;

    }

    @Override
    public String getCategory ()
    {

        return Rule.PARAGRAPH_CATEGORY;

    }

    @Override
    public String getFormError ()
    {

        return null;

    }

    @Override
    public Set<FormItem> getFormItems ()
    {

        List<String> pref = new ArrayList<> ();
        pref.add (LanguageStrings.problemfinder);
        pref.add (LanguageStrings.config);
        pref.add (LanguageStrings.rules);
        pref.add (LanguageStrings.paragraphlength);
        pref.add (LanguageStrings.labels);

        Set<FormItem> items = new LinkedHashSet ();

        this.wordCountF = new JSpinner (new SpinnerNumberModel (this.wordCount,
                                                                1,
                                                                500,
                                                                1));

        Box b = new Box (BoxLayout.X_AXIS);
        b.add (this.wordCountF);
        b.add (Box.createHorizontalGlue ());

        this.wordCountF.setMaximumSize (this.wordCountF.getPreferredSize ());

        items.add (new AnyFormItem (Environment.getUIString (pref,
                                                             LanguageStrings.words),
                                    //"Words",
                                    b));

        this.sentCountF = new JSpinner (new SpinnerNumberModel (this.sentenceCount,
                                                                1,
                                                                500,
                                                                1));

        b = new Box (BoxLayout.X_AXIS);

        b.add (this.sentCountF);
        b.add (Box.createHorizontalGlue ());

        this.sentCountF.setMaximumSize (this.sentCountF.getPreferredSize ());

        items.add (new AnyFormItem (Environment.getUIString (pref,
                                                             LanguageStrings.sentences),
                                    //"Sentences",
                                    b));

        return items;

    }

    @Override
    public void updateFromForm ()
    {

        this.sentenceCount = ((SpinnerNumberModel) this.sentCountF.getModel ()).getNumber ().intValue ();
        this.wordCount = ((SpinnerNumberModel) this.wordCountF.getModel ()).getNumber ().intValue ();

    }

}
