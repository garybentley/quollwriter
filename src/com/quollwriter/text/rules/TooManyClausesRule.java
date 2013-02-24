package com.quollwriter.text.rules;

import java.util.*;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.text.*;

import com.quollwriter.ui.components.*;

import org.jdom.*;


public class TooManyClausesRule extends AbstractSentenceRule
{

    public static final String CREATE_TYPE = "toomanyclauses";

    public class XMLConstants
    {

        public static final String clauseCount = "clauseCount";

    }

    private int                 clauseCount = 0;
    private JSpinner            count = null;
    private Map<String, String> separators = new HashMap ();

    public TooManyClausesRule(boolean user)
    {

        super (user);

        this.separators.put (",",
                             ",");
        this.separators.put ("-",
                             "-");

    }

    public String getDescription ()
    {

        String d = super.getDescription ();

        return StringUtils.replaceString (d,
                                          "[LIMIT]",
                                          this.clauseCount + "");

    }

    public String getSummary ()
    {

        return StringUtils.replaceString (super.getSummary (),
                                          "[LIMIT]",
                                          this.clauseCount + "");

    }

    public String getCreateType ()
    {

        return TooManyClausesRule.CREATE_TYPE;

    }

    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        this.clauseCount = JDOMUtils.getAttributeValueAsInt (root,
                                                             XMLConstants.clauseCount);

    }

    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.setAttribute (XMLConstants.clauseCount,
                           this.clauseCount + "");

        return root;

    }

    public List<Issue> getIssues (String  sentence,
                                  boolean inDialogue)
    {

        // Check our list of words.
        sentence = sentence.toLowerCase ();

        List<String> swords = TextUtilities.getAsWords (sentence);

        int c = 1;

        for (int i = 0; i < swords.size (); i++)
        {

            if (this.separators.containsKey (swords.get (i)))
            {

                if (i > 0)
                {
                    
                    c++;
                    
                }

            }

        }

        // Look for , ; or -.
        List<Issue> issues = new ArrayList ();

        if (c > this.clauseCount)
        {

            Issue iss = new Issue ("Sentence contains: <b>" + c + "</b> clauses.",
                                   -1,
                                   -1,
                                   this);

            issues.add (iss);

        }

        return issues;

    }

    public List<FormItem> getFormItems ()
    {

        List<FormItem> items = new ArrayList ();

        this.count = new JSpinner (new SpinnerNumberModel (this.clauseCount,
                                                           1,
                                                           200,
                                                           1));

        Box b = new Box (BoxLayout.X_AXIS);
        b.add (this.count);
        b.add (Box.createHorizontalGlue ());

        this.count.setMaximumSize (this.count.getPreferredSize ());

        items.add (new FormItem ("No of Clauses",
                                 b));

        return items;

    }

    public void updateFromForm ()
    {

        this.clauseCount = ((SpinnerNumberModel) this.count.getModel ()).getNumber ().intValue ();

    }

    public String getCategory ()
    {

        return Rule.SENTENCE_CATEGORY;

    }

}
