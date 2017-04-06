package com.quollwriter.text.rules;

import java.util.*;
import java.awt.event.*;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.text.*;

import com.quollwriter.ui.forms.*;

import org.jdom.*;

public class TooManyClausesRule extends AbstractSentenceRule
{

    public class XMLConstants
    {

        public static final String clauseCount = "clauseCount";

    }

    private int                 clauseCount = 0;
    private JSpinner            count = null;
    private Map<String, String> separators = new HashMap ();

    public TooManyClausesRule ()
    {

        this.separators.put (",",
                             ",");
        this.separators.put ("-",
                             "-");

    }

    public TooManyClausesRule (int     clauseCount,
                               boolean user)
    {

        this.clauseCount = clauseCount;
        this.setUserRule (user);

    }

    public String getDescription ()
    {

        String d = super.getDescription ();

        return StringUtils.replaceString (d,
                                          "[LIMIT]",
                                          this.clauseCount + "");

    }

    @Override
    public String getSummary ()
    {

        return StringUtils.replaceString (super.getSummary (),
                                          "[LIMIT]",
                                          this.clauseCount + "");

    }

    @Override
    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        this.clauseCount = JDOMUtils.getAttributeValueAsInt (root,
                                                             XMLConstants.clauseCount);

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.setAttribute (XMLConstants.clauseCount,
                           this.clauseCount + "");

        return root;

    }

    @Override
    public List<Issue> getIssues (Sentence sentence)
    {

        int c = 1;
        int i = 0;

        for (Word w : sentence.getWords ())
        {

            if (this.separators.containsKey (w.getText ()))
            {

                if (i > 0)
                {

                    c++;

                }

            }

            i++;

        }

        // Look for , ; or -.
        List<Issue> issues = new ArrayList ();

        if (c > this.clauseCount)
        {

            Issue iss = new Issue ("Sentence contains: <b>" + c + "</b> clauses.",
                                   sentence,
                                   sentence.getAllTextStartOffset () + "-toomanyclauses-" + c,
                                   this);

            issues.add (iss);

        }

        return issues;

    }

    @Override
    public Set<FormItem> getFormItems ()
    {

        Set<FormItem> items = new LinkedHashSet ();

        this.count = new JSpinner (new SpinnerNumberModel (this.clauseCount,
                                                           1,
                                                           200,
                                                           1));

        Box b = new Box (BoxLayout.X_AXIS);
        b.add (this.count);
        b.add (Box.createHorizontalGlue ());

        this.count.setMaximumSize (this.count.getPreferredSize ());

        items.add (new AnyFormItem ("No of Clauses",
                                    b));

        return items;

    }

    @Override
    public String getFormError ()
    {

        return null;

    }

    @Override
    public void updateFromForm ()
    {

        this.clauseCount = ((SpinnerNumberModel) this.count.getModel ()).getNumber ().intValue ();

    }

    public String getCategory ()
    {

        return Rule.SENTENCE_CATEGORY;

    }

}
