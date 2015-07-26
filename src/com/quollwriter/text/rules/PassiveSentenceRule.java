package com.quollwriter.text.rules;

import java.util.*;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;

import com.quollwriter.synonyms.*;

import com.quollwriter.text.*;

import com.quollwriter.ui.components.*;

import org.jdom.*;


public class PassiveSentenceRule extends AbstractSentenceRule
{

    public static final String CREATE_TYPE = "passivesentence";

    public class XMLConstants
    {

        public static final String beWords = "beWords";
        public static final String irregularForms = "irregularForms";
        public static final String ignoreInDialogue = "ignoreInDialogue";        

    }

    private Set<String> beWords = new HashSet ();
    private Set<String> irregularForms = new HashSet ();
    private boolean ignoreInDialogue = false;
    
    private JCheckBox ignoreDialogueF = null;

    public PassiveSentenceRule(boolean user)
    {

        super (user);

    }

    public void setIrregularForms (Set<String> forms)
    {
        
        this.irregularForms = new HashSet (forms);
        
    }
    
    public void setBeWords (Set<String> words)
    {
        
        this.beWords = new HashSet (words);
        
    }
    
    public boolean isIgnoreInDialogue ()
    {
        
        return this.ignoreInDialogue;
        
    }
    
    public void setIgnoreInDialogue (boolean v)
    {
    
        this.ignoreInDialogue = v;
        
    }
    
    public String getDescription ()
    {

        String d = super.getDescription ();

        return d;

    }

    public String getSummary ()
    {

        return super.getSummary ();

    }

    public String getCreateType ()
    {

        return PassiveSentenceRule.CREATE_TYPE;

    }

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
/*
    public List<Issue> getIssues (String  sentence,
                                  boolean inDialogue)
    {

        List<Issue> issues = new ArrayList ();

        // Check our list of words.
        sentence = sentence.toLowerCase ();

        List<String> swords = TextUtilities.getAsWords (sentence);

        int c = 0;
        int lastWord = -1;

        for (int i = 0; i < swords.size (); i++)
        {

            String a = swords.get (i);

            inDialogue = TextUtilities.stillInDialogue (a,
                                                        inDialogue);            
            
            if ((inDialogue)
                &&
                (this.ignoreInDialogue)
               )
            {
                
                continue;
                
            }
            
            if (this.beWords.containsKey (a))
            {

                // Check the next word.
                if (i < (swords.size () - 1))
                {

                    String b = swords.get (i + 1);

                    if (this.isPastTenseVerb (b))
                    {
                    
                        Issue iss = new Issue ("Passive voice, contains: <b>" + a + " " + b + "</b>",
                                               i,
                                               a.length () + b.length () + 1,
                                               this);

                        issues.add (iss);


                    }

                }

            }

        }

        return issues;

    }
*/
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
                    
                        Issue iss = new Issue ("Passive voice, contains: <b>" + w.getText () + " " + nw.getText () + "</b>",
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

            if (Environment.getWordTypes (w,
                                          // Assume english for now
                                          null) == null)
            {

                w += "e";

            }

        } catch (Exception e)
        {


        }

        String t = null;

        try
        {

            t = Environment.getWordTypes (w,
                                          // Assume english for now
                                          null);

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
    public List<FormItem> getFormItems ()
    {

        List<FormItem> items = new ArrayList ();

        this.ignoreDialogueF = new JCheckBox ("Ignore in dialogue");

        this.ignoreDialogueF.setSelected (this.ignoreInDialogue);
        
        items.add (new FormItem ("",
                                 this.ignoreDialogueF));
        
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
