package com.quollwriter.text.rules;

import java.util.*;

import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.*;

import com.quollwriter.*;
import com.quollwriter.text.*;

import com.quollwriter.ui.fx.components.Form;
import com.quollwriter.ui.fx.components.*;

import org.dom4j.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ParagraphLengthRule extends AbstractParagraphRule
{

    public class XMLConstants
    {

        public static final String wordCount = "wordCount";
        public static final String sentenceCount = "sentenceCount";

    }

    private int sentenceCount = 0;
    private int wordCount = 0;

    private Spinner<Integer> sentCountF2 = null;
    private Spinner<Integer> wordCountF2 = null;

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

        d = Utils.replaceString (d,
                                       "[SENTENCE_COUNT]",
                                       this.sentenceCount + "");

        d = Utils.replaceString (d,
                                       "[WORD_COUNT]",
                                       this.wordCount + "");

        return d;

    }

    @Override
    public String getSummary ()
    {

        String t = Utils.replaceString (super.getSummary (),
                                              "[SENTENCE_COUNT]",
                                              this.sentenceCount + "");

        t = Utils.replaceString (t,
                                       "[WORD_COUNT]",
                                       this.wordCount + "");

        return t;

    }

    @Override
    public void init (Element root)
               throws GeneralException
    {

        super.init (root);

        this.sentenceCount = DOM4JUtils.attributeValueAsInt (root,
                                                               XMLConstants.sentenceCount);
        this.wordCount = DOM4JUtils.attributeValueAsInt (root,
                                                           XMLConstants.wordCount);

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.addAttribute (XMLConstants.sentenceCount,
                           this.sentenceCount + "");
        root.addAttribute (XMLConstants.wordCount,
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
    public Set<Form.Item> getFormItems ()
    {

        List<String> pref = Arrays.asList (problemfinder,config,rules,paragraphlength,labels);

        Set<Form.Item> items = new LinkedHashSet<> ();

        this.wordCountF2 = new Spinner<> (new IntegerSpinnerValueFactory (1, 500, this.wordCount, 1));

        items.add (new Form.Item (getUILanguageStringProperty (Utils.newList (pref,words)),
                                  this.wordCountF2));

        this.sentCountF2 = new Spinner<> (new IntegerSpinnerValueFactory (1, 500, this.sentenceCount, 1));

        items.add (new Form.Item (getUILanguageStringProperty (Utils.newList (pref,sentences)),
                                  this.sentCountF2));

        return items;

    }

    @Override
    public void updateFromForm ()
    {

        this.sentenceCount = this.sentCountF2.getValue ();
        this.wordCount = this.wordCountF2.getValue ();

    }

}
