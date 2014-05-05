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

public class ParagraphLengthRule extends AbstractParagraphRule
{
    
    public static final String CREATE_TYPE = "paragraphlength";

    public class XMLConstants
    {

        public static final String wordCount = "wordCount";
        public static final String sentenceCount = "sentenceCount";

    }

    private int sentenceCount = 0;
    private int wordCount = 0;
    private JSpinner sentCountF = null;
    private JSpinner wordCountF = null;

    public ParagraphLengthRule (boolean user)
    {

        super (user);

    }

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

    public String getCreateType ()
    {

        return ParagraphLengthRule.CREATE_TYPE;

    }

    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        this.sentenceCount = JDOMUtils.getAttributeValueAsInt (root,
                                                               XMLConstants.sentenceCount);
        this.wordCount = JDOMUtils.getAttributeValueAsInt (root,
                                                           XMLConstants.wordCount);

    }

    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.setAttribute (XMLConstants.sentenceCount,
                           this.sentenceCount + "");
        root.setAttribute (XMLConstants.wordCount,
                           this.wordCount + "");

        return root;

    }
/*
    public List<Issue> getIssues (String  paragraph,
                                  boolean inDialogue)
    {

        List<Issue> issues = new ArrayList ();
    
        int wc = TextUtilities.getWordCount (paragraph);
        
        if (wc > this.wordCount)
        {
            
            Issue iss = new Issue ("Paragraph word count is: <b>" + Environment.formatNumber (wc) + "</b>.  (Max is: " + Environment.formatNumber (this.wordCount) + ")",
                                   -1,
                                   -1,
                                   this);

            issues.add (iss);

        }

        int sc = TextUtilities.getSentenceCount (paragraph);
        
        if (sc > this.sentenceCount)
        {
            
            Issue iss = new Issue ("Paragraph sentence count is: <b>" + Environment.formatNumber (sc) + "</b>.  (Max is: " + Environment.formatNumber (this.sentenceCount) + ")",
                                   -1,
                                   -1,
                                   this);

            issues.add (iss);

        }
        
        return issues;

    }
*/
    public List<Issue> getIssues (Paragraph paragraph)
    {

        List<Issue> issues = new ArrayList ();
            
        int wc = paragraph.getWordCount ();
            
        if (wc > this.wordCount)
        {
            
            Issue iss = new Issue ("Paragraph word count is: <b>" + Environment.formatNumber (wc) + "</b>.  (Max is: " + Environment.formatNumber (this.wordCount) + ")",
                                   paragraph,
                                   this);

            issues.add (iss);

        }

        int sc = paragraph.getSentenceCount ();
        
        if (sc > this.sentenceCount)
        {
            
            Issue iss = new Issue ("Paragraph sentence count is: <b>" + Environment.formatNumber (sc) + "</b>.  (Max is: " + Environment.formatNumber (this.sentenceCount) + ")",
                                   paragraph,
                                   this);

            issues.add (iss);

        }
        
        return issues;

    }

    public String getCategory ()
    {

        return Rule.PARAGRAPH_CATEGORY;

    }

    public List<FormItem> getFormItems ()
    {

        List<FormItem> items = new ArrayList ();

        this.wordCountF = new JSpinner (new SpinnerNumberModel (this.wordCount,
                                                                1,
                                                                500,
                                                                1));

        Box b = new Box (BoxLayout.X_AXIS);
        b.add (this.wordCountF);
        b.add (Box.createHorizontalGlue ());

        this.wordCountF.setMaximumSize (this.wordCountF.getPreferredSize ());

        items.add (new FormItem ("Words",
                                 b));

        this.sentCountF = new JSpinner (new SpinnerNumberModel (this.sentenceCount,
                                                                1,
                                                                500,
                                                                1));

        b = new Box (BoxLayout.X_AXIS);
                                                            
        b.add (this.sentCountF);
        b.add (Box.createHorizontalGlue ());

        this.sentCountF.setMaximumSize (this.sentCountF.getPreferredSize ());

        items.add (new FormItem ("Sentences",
                                 b));
                                 
        return items;

    }

    public void updateFromForm ()
    {

        this.sentenceCount = ((SpinnerNumberModel) this.sentCountF.getModel ()).getNumber ().intValue ();
        this.wordCount = ((SpinnerNumberModel) this.wordCountF.getModel ()).getNumber ().intValue ();

    }    
    
}