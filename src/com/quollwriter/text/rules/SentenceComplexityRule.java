package com.quollwriter.text.rules;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;
import com.quollwriter.text.*;

import com.quollwriter.ui.components.*;

import org.jdom.Element;
import org.jdom.JDOMException;


public class SentenceComplexityRule extends AbstractSentenceRule
{

    public static final String CREATE_TYPE = "sentencecomplexity";

    public class XMLConstants
    {

        public static final String ratio = "ratio";
        public static final String wordCount = "wordCount";

    }

    private float      ratio = 0;
    private int wordCount = 0;
    private JSpinner ratioF = null;
    private JSpinner wordCountF = null;

    public SentenceComplexityRule (boolean user)
    {

        super (user);

    }

    public SentenceComplexityRule (float   syllableWordRatio,
                                   int     wordCount,
                                   boolean user)
    {

        this (user);
        
        this.ratio = syllableWordRatio;
        this.wordCount = wordCount;

    }

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

    public String getCreateType ()
    {

        return SentenceComplexityRule.CREATE_TYPE;

    }

    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        this.ratio = JDOMUtils.getAttributeValueAsFloat (root,
                                                         XMLConstants.ratio);
        this.wordCount = JDOMUtils.getAttributeValueAsInt (root,
                                                       XMLConstants.wordCount);

    }

    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.setAttribute (XMLConstants.ratio,
                           this.ratio + "");
        root.setAttribute (XMLConstants.wordCount,
                           this.wordCount + "");

        return root;

    }
/*
    public List<Issue> getIssues (String  sentence,
                                  boolean inDialogue)
    {

        List<Issue> issues = new ArrayList ();
    
        // Check our list of words.
        sentence = sentence.toLowerCase ();

        float wordC = (float) TextUtilities.getWordCount (sentence);

        if (wordC <= this.wordCount)
        {
            
            return issues;
            
        }
        
        float syllC = (float) TextUtilities.getSyllableCount (sentence);

        float r = syllC / wordC;
        
        r = (float) Math.round (r * 10) / 10;
        
        if (r > this.ratio)
        {
            
            Issue iss = new Issue ("Sentence syllable/word ratio is: <b>" + Environment.formatNumber (r) + "</b>.  (Max is: " + Environment.formatNumber (this.ratio) + ")",
                                   -1,
                                   -1,
                                   this);

            issues.add (iss);

        }

        return issues;

    }
*/
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
            
            Issue iss = new Issue ("Sentence syllable/word ratio is: <b>" + Environment.formatNumber (r) + "</b>.  (Max is: " + Environment.formatNumber (this.ratio) + ")",
                                   sentence,
                                   sentence.getAllTextStartOffset () + "-sentencetoocomplex-" + r,
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

        this.ratioF = new JSpinner (new SpinnerNumberModel (this.ratio,
                                                            0.1f,
                                                            3.0f,
                                                            0.1));

        Box b = new Box (BoxLayout.X_AXIS);
        b.add (this.ratioF);
        b.add (Box.createHorizontalGlue ());

        this.ratioF.setMaximumSize (this.ratioF.getPreferredSize ());

        items.add (new FormItem ("Ratio",
                                 b));

        this.wordCountF = new JSpinner (new SpinnerNumberModel (this.wordCount,
                                                            1,
                                                            500,
                                                            1));

        b = new Box (BoxLayout.X_AXIS);
                                                            
        b.add (this.wordCountF);
        b.add (Box.createHorizontalGlue ());

        this.wordCountF.setMaximumSize (this.wordCountF.getPreferredSize ());

        items.add (new FormItem ("Sentence length (words)",
                                 b));
                                 
        return items;

    }

    public void updateFromForm ()
    {

        this.ratio = ((SpinnerNumberModel) this.ratioF.getModel ()).getNumber ().floatValue ();
        this.wordCount = ((SpinnerNumberModel) this.wordCountF.getModel ()).getNumber ().intValue ();

    }

}
