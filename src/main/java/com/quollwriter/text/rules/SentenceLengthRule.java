package com.quollwriter.text.rules;

import java.util.*;

import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.*;

import com.quollwriter.*;
import com.quollwriter.text.*;

import org.dom4j.*;

import com.quollwriter.ui.fx.components.Form;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class SentenceLengthRule extends AbstractSentenceRule
{

    public class XMLConstants
    {

        public static final String wordCount = "wordCount";

    }

    private int      wordCount = 0;

    private Spinner<Integer> count2 = null;

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

        return Utils.replaceString (d,
                                          "[LIMIT]",
                                          this.wordCount + "");

    }

    @Override
    public String getSummary ()
    {

        return Utils.replaceString (super.getSummary (),
                                          "[LIMIT]",
                                          this.wordCount + "");

    }

    @Override
    public void init (Element root)
               throws GeneralException
    {

        super.init (root);

        this.wordCount = DOM4JUtils.attributeValueAsInt (root,
                                                           XMLConstants.wordCount);

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.addAttribute (XMLConstants.wordCount,
                           this.wordCount + "");

        return root;

    }

    @Override
    public List<Issue> getIssues (Sentence sentence)
    {

        // Check each word to make sure it's not punctuation.
        List<Issue> issues = new ArrayList<> ();

        int wc = sentence.getWordCount ();

        if (wc > this.wordCount)
        {

            Issue iss = new Issue (String.format (Environment.getUIString (LanguageStrings.problemfinder,
                                                                           LanguageStrings.issues,
                                                                           LanguageStrings.sentencelength,
                                                                           LanguageStrings.text),
                                                  Environment.formatNumber (wc),
                                                  Environment.formatNumber (this.wordCount)),
                                                  //"Sentence contains: <b>" + wc + "</b> words.",
                                   sentence,
                                   sentence.getAllTextStartOffset (),
                                   sentence.getLastWord ().getAllTextEndOffset () - sentence.getAllTextStartOffset (),
                                   sentence.getAllTextStartOffset () + "-toomanywords-" + wc,
                                   this);

            issues.add (iss);

        }

        return issues;

    }

    @Override
    public String getCategory ()
    {

        return Rule.SENTENCE_CATEGORY;

    }

    @Override
    public Set<Form.Item> getFormItems ()
    {

        List<String> pref = Arrays.asList (problemfinder,config,rules,sentencelength,labels);

        Set<Form.Item> items = new LinkedHashSet<> ();

        this.count2 = new Spinner (new IntegerSpinnerValueFactory (1, 200, this.wordCount, 1));

        items.add (new Form.Item (getUILanguageStringProperty (Utils.newList (pref,words)),
                                    //"No of Words",
                                  this.count2));

        return items;

    }

    @Override
    public void updateFromForm ()
    {

        this.wordCount = this.count2.getValue ();

    }

}
