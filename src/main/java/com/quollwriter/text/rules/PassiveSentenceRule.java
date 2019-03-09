package com.quollwriter.text.rules;

import java.util.*;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;

import com.quollwriter.synonyms.*;

import com.quollwriter.text.*;

import com.quollwriter.ui.forms.*;

import org.jdom.*;


public class PassiveSentenceRule extends AbstractSentenceRule
{

    public class XMLConstants
    {

        public static final String beWords = "beWords";
        public static final String irregularForms = "irregularForms";
        public static final String ignoreInDialogue = "ignoreInDialogue";

    }

    private Set<String> beWords = new HashSet<> ();
    private Set<String> irregularForms = new HashSet<> ();
    private boolean ignoreInDialogue = false;

    private CheckboxFormItem ignoreDialogueF = null;

    public PassiveSentenceRule ()
    {

    }

    public void setIrregularForms (Set<String> forms)
    {

        this.irregularForms = new HashSet<> (forms);

    }

    public void setBeWords (Set<String> words)
    {

        this.beWords = new HashSet<> (words);

    }

    public boolean isIgnoreInDialogue ()
    {

        return this.ignoreInDialogue;

    }

    public void setIgnoreInDialogue (boolean v)
    {

        this.ignoreInDialogue = v;

    }

    @Override
    public String getDescription ()
    {

        String d = super.getDescription ();

        return d;

    }

    @Override
    public String getSummary ()
    {

        return super.getSummary ();

    }

    @Override
    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        String sw = JDOMUtils.getAttributeValue (root,
                                                 XMLConstants.beWords);

        StringTokenizer t = new StringTokenizer (sw,
                                                 ",");

        while (t.hasMoreTokens ())
        {

            this.beWords.add (t.nextToken ().trim ().toLowerCase ());

        }

        String w = JDOMUtils.getAttributeValue (root,
                                                XMLConstants.irregularForms);

        t = new StringTokenizer (w,
                                 ",");

        while (t.hasMoreTokens ())
        {

            this.irregularForms.add (t.nextToken ().trim ().toLowerCase ());

        }

        this.ignoreInDialogue = JDOMUtils.getAttributeValueAsBoolean (root,
                                                                      XMLConstants.ignoreInDialogue,
                                                                      false);

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        if (this.ignoreInDialogue)
        {

            root.setAttribute (XMLConstants.ignoreInDialogue,
                               Boolean.toString (this.ignoreInDialogue));

        }

        StringBuilder b = new StringBuilder ();

        for (String s : this.beWords)
        {

            if (b.length () > 0)
            {

                b.append (",");

            }

            b.append (s);

        }

        root.setAttribute (XMLConstants.beWords,
                           b.toString ());

        b = new StringBuilder ();

        for (String s : this.irregularForms)
        {

            if (b.length () > 0)
            {

                b.append (",");

            }

            b.append (s);

        }

        root.setAttribute (XMLConstants.irregularForms,
                           b.toString ());

        return root;

    }

    @Override
    public List<Issue> getIssues (Sentence sentence)
    {

        List<Issue> issues = new ArrayList ();

        List<Word> swords = sentence.getWords ();

        int c = 0;
        int lastWord = -1;

        for (Word w : swords)
        {

            if ((w.isInDialogue ())
                &&
                (this.ignoreInDialogue)
               )
            {

                continue;

            }

            if (this.isBeWord (w.getText ()))
            {

                Word nw = w.getNext ();

                if (nw != null)
                {

                    if (this.isPastTenseVerb (nw.getText ()))
                    {

                        int s = w.getAllTextStartOffset ();

                        Issue iss = new Issue (String.format (Environment.getUIString (LanguageStrings.problemfinder,
                                                                                       LanguageStrings.issues,
                                                                                       LanguageStrings.passivesentence,
                                                                                       LanguageStrings.text),
                                                              w.getText () + " " + nw.getText ()),
                                               //"Passive voice, contains: <b>" + w.getText () + " " + nw.getText () + "</b>",
                                               sentence,
                                               s,
                                               nw.getAllTextEndOffset () - s,
                                               s + "-passivevoice-" + w.getText () + "-" + nw.getText (),
                                               this);

                        issues.add (iss);


                    }

                }

            }

        }

        return issues;

    }

    public boolean isBeWord (String w)
    {

        if (w == null)
        {

            return false;

        }

        return this.beWords.contains (w.toLowerCase ());

    }

    public boolean isPastTenseVerb (String w)
    {

        if (w == null)
        {

            return false;

        }

        w = w.toLowerCase ();

        // See if the word is:
        // 1. A verb
        // 2. Ends with "ed" OR:
        // 3. Is an irregular form.
        if (this.irregularForms.contains (w))
        {

            return true;

        }

        // At this point we only consider words that end with "ed".
        if (!w.endsWith ("ed"))
        {

            return false;

        }

        w = w.substring (0,
                         w.length () - 2);

        // Check to see if the word is valid, if not add the e back. (i.e. damaged -> damage)
        try
        {

            // Assume english for now
            SynonymProvider sp = Environment.getSynonymProvider (Constants.ENGLISH);

            if (sp != null)
            {

                if (sp.getWordTypes (w) == null)
                {

                    w += "e";

                }

            }

        } catch (Exception e)
        {


        }

        String t = null;

        try
        {

            SynonymProvider sp = Environment.getSynonymProvider (Constants.ENGLISH);

            if (sp != null)
            {

                t = sp.getWordTypes (w);

            }

        } catch (Exception e)
        {

            Environment.logError ("Unable to get word types for word: " +
                                  w,
                                  e);

            return false;

        }

        if (t == null)
        {

            return false;

        }

        if ((t.indexOf (Synonyms.VERB) != -1) ||
            (t.indexOf (Synonyms.VERB_T) != -1) ||
            (t.indexOf (Synonyms.VERB_I) != -1))
        {

            return true;

        }

        return false;

    }

    @Override
    public Set<FormItem> getFormItems ()
    {

        Set<FormItem> items = new LinkedHashSet ();

        List<String> pref = Arrays.asList (LanguageStrings.problemfinder,LanguageStrings.config,LanguageStrings.rules,LanguageStrings.passivesentence,LanguageStrings.labels);

        this.ignoreDialogueF = new CheckboxFormItem (null,
                                                     Environment.getUIString (pref,
                                                                              LanguageStrings.ignoreindialogue));
                                                     //"Ignore in dialogue");

        this.ignoreDialogueF.setSelected (this.ignoreInDialogue);

        items.add (this.ignoreDialogueF);

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

        this.ignoreInDialogue = this.ignoreDialogueF.isSelected ();

    }

    public String getCategory ()
    {

        return Rule.SENTENCE_CATEGORY;

    }

}
