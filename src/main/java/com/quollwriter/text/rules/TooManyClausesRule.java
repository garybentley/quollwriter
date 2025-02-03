package com.quollwriter.text.rules;

import java.util.*;

import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.*;

import com.quollwriter.*;
import com.quollwriter.text.*;

import org.dom4j.*;

import com.quollwriter.ui.fx.components.Form;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class TooManyClausesRule extends AbstractSentenceRule
{

    public class XMLConstants
    {

        public static final String clauseCount = "clauseCount";

    }

    private int                 clauseCount = 0;
    private Map<String, String> separators = new HashMap<> ();

    private Spinner<Integer> count2 = null;

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

        this ();

        this.clauseCount = clauseCount;
        this.setUserRule (user);

    }

    public String getDescription ()
    {

        String d = super.getDescription ();

        return Utils.replaceString (d,
                                          "[LIMIT]",
                                          this.clauseCount + "");

    }

    @Override
    public String getSummary ()
    {

        return Utils.replaceString (super.getSummary (),
                                          "[LIMIT]",
                                          this.clauseCount + "");

    }

    @Override
    public void init (Element root)
               throws GeneralException
    {

        super.init (root);

        this.clauseCount = DOM4JUtils.attributeValueAsInt (root,
                                                             XMLConstants.clauseCount);

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        root.addAttribute (XMLConstants.clauseCount,
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
        List<Issue> issues = new ArrayList<> ();

        if (c > this.clauseCount)
        {

            Issue iss = new Issue (String.format (Environment.getUIString (LanguageStrings.problemfinder,
                                                                           LanguageStrings.issues,
                                                                           LanguageStrings.toomanyclauses,
                                                                           LanguageStrings.text),
                                                  Environment.formatNumber (c),
                                                  Environment.formatNumber (this.clauseCount)),
                                                  //"Sentence contains: <b>" + c + "</b> clauses.",
                                   sentence,
                                   sentence.getAllTextStartOffset () + "-toomanyclauses-" + c,
                                   this);

            issues.add (iss);

        }

        return issues;

    }

    @Override
    public Set<Form.Item> getFormItems ()
    {

        List<String> pref = Arrays.asList (problemfinder,config,rules,toomanyclauses,labels);

        Set<Form.Item> items = new LinkedHashSet<> ();

        this.count2 = new Spinner<> (new IntegerSpinnerValueFactory (1, 200, this.clauseCount, 1));

        items.add (new Form.Item (getUILanguageStringProperty (Utils.newList (pref,clauses)),
                                  //"No of Clauses",
                                  this.count2));

        return items;

    }

    public String getCategory ()
    {

        return Rule.SENTENCE_CATEGORY;

    }

    @Override
    public void updateFromForm ()
    {

        this.clauseCount = this.count2.getValue ();

    }

}
