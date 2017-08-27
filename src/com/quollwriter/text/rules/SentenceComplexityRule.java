package com.quollwriter.text.rules;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import java.text.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;
import com.quollwriter.text.*;

import com.quollwriter.ui.forms.*;

import org.jdom.Element;
import org.jdom.JDOMException;


public class SentenceComplexityRule extends AbstractSentenceRule
{

    public class XMLConstants
    {

        public static final String ratio = "ratio";
        public static final String wordCount = "wordCount";

    }

    private float      ratio = 0;
    private int wordCount = 0;
    private JSpinner ratioF = null;
    private JSpinner wordCountF = null;

    public SentenceComplexityRule ()
    {

    }

    public SentenceComplexityRule (float   syllableWordRatio,
                                   int     wordCount,
                                   boolean user)
    {

        this.ratio = syllableWordRatio;
        this.wordCount = wordCount;
        this.setUserRule (user);

    }

    @Override
    public String getDescription ()
    {

        String d = super.getDescription ();

        d = StringUtils.replaceString (d,
                                       "[RATIO]",
                                       Environment.formatNumber (this.ratio) + "");

        d = StringUtils.replaceString (d,
                                       "[COUNT]",
                                       this.wordCount + "");

        return d;

    }

    @Override
    public String getSummary ()
    {

        String t = StringUtils.replaceString (super.getSummary (),
                                              "[RATIO]",
                                              Environment.formatNumber (this.ratio) + "");

        t = StringUtils.replaceString (t,
                                       "[COUNT]",
                                       this.wordCount + "");

        return t;

    }

    @Override
    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        this.ratio = JDOMUtils.getAttributeValueAsFloat (root,
                                                         XMLConstants.ratio);
        this.wordCount = JDOMUtils.getAttributeValueAsInt (root,
                                                       XMLConstants.wordCount);

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.setAttribute (XMLConstants.ratio,
                           this.ratio + "");
        root.setAttribute (XMLConstants.wordCount,
                           this.wordCount + "");

        return root;

    }

    @Override
    public List<Issue> getIssues (Sentence sentence)
    {

        List<Issue> issues = new ArrayList ();

        float wordC = (float) sentence.getWordCount ();

        if (wordC <= this.wordCount)
        {

            return issues;

        }

        float syllC = (float) sentence.getSyllableCount ();

        float r = syllC / wordC;

        r = (float) Math.round (r * 10) / 10;

        if (r > this.ratio)
        {

            DecimalFormat df = new DecimalFormat ("##.#");

            String n = df.format (r);

            Issue iss = new Issue (String.format (Environment.getUIString (LanguageStrings.problemfinder,
                                                                           LanguageStrings.issues,
                                                                           LanguageStrings.sentencecomplexity,
                                                                           LanguageStrings.text),
                                                  //"Sentence syllable/word ratio is: <b>%s</b>.  (Max is: %s)",
                                                  n,
                                                  Environment.formatNumber (this.ratio)),
                                   sentence,
                                   sentence.getAllTextStartOffset () + "-sentencetoocomplex-" + r,
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
    public Set<FormItem> getFormItems ()
    {

        Set<String> pref = new LinkedHashSet ();
        pref.add (LanguageStrings.problemfinder);
        pref.add (LanguageStrings.config);
        pref.add (LanguageStrings.rules);
        pref.add (LanguageStrings.sentencecomplexity);
        pref.add (LanguageStrings.labels);
    
        Set<FormItem> items = new LinkedHashSet ();

        this.ratioF = new JSpinner (new SpinnerNumberModel (this.ratio,
                                                            0.1f,
                                                            3.0f,
                                                            0.1));

        Box b = new Box (BoxLayout.X_AXIS);
        b.add (this.ratioF);
        b.add (Box.createHorizontalGlue ());

        this.ratioF.setMaximumSize (this.ratioF.getPreferredSize ());

        items.add (new AnyFormItem (Environment.getUIString (pref,
                                                             LanguageStrings.ratio),
                                    b));

        this.wordCountF = new JSpinner (new SpinnerNumberModel (this.wordCount,
                                                            1,
                                                            500,
                                                            1));

        b = new Box (BoxLayout.X_AXIS);

        b.add (this.wordCountF);
        b.add (Box.createHorizontalGlue ());

        this.wordCountF.setMaximumSize (this.wordCountF.getPreferredSize ());

        items.add (new AnyFormItem (Environment.getUIString (pref,
                                                             LanguageStrings.sentencelength),
                                    //"Sentence length (words)",
                                    b));

        return items;

    }

    public String getFormError ()
    {

        return null;

    }

    public void updateFromForm ()
    {

        this.ratio = ((SpinnerNumberModel) this.ratioF.getModel ()).getNumber ().floatValue ();
        this.wordCount = ((SpinnerNumberModel) this.wordCountF.getModel ()).getNumber ().intValue ();

    }

}
