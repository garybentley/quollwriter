package com.quollwriter.text.rules;

import java.io.*;

import java.util.*;

import com.gentlyweb.properties.StringProperty;

import com.gentlyweb.xml.*;

import com.quollwriter.*;

import com.quollwriter.text.*;

import org.jdom.*;


public class RuleFactory
{

    public static final int USER = 1;
    public static final int PROJECT = 2;
    public static final int ALL = 4;

    private static Map<String, Map<String, Rule>> rules = new HashMap ();

    private static int lastUserRuleIndex = 0;

    public static void init ()
                      throws Exception
    {

        // Load the standard rules.
        String xml = Environment.getResourceFileAsString (Constants.PROBLEM_FINDER_RULES_FILE);

        Element root = JDOMUtils.getStringAsElement (xml);

        List els = JDOMUtils.getChildElements (root,
                                               AbstractRule.XMLConstants.root,
                                               false);

        for (int i = 0; i < els.size (); i++)
        {

            Element el = (Element) els.get (i);

            Rule r = null;
            
            try
            {
                
                r = RuleFactory.createRule (el,
                                            false);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to create rule",
                                      new GeneralException ("Unable to create rule from element: " +
                                                            JDOMUtils.getPath (el),
                                                            e));
                
                continue;
                
            }

            Map<String, Rule> l = RuleFactory.rules.get (r.getCategory ());

            if (l == null)
            {

                l = new HashMap ();
                RuleFactory.rules.put (r.getCategory (),
                                       l);

            }

            l.put (r.getId (),
                   r);

        }

        // Load the user defined rules.
        File dir = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.USER_PROBLEM_FINDER_RULES_DIR);

        File[] files = dir.listFiles (new FilenameFilterAdapter ()
            {

                public boolean accept (File   dir,
                                       String name)
                {

                    if (name.endsWith (".xml"))
                    {

                        return true;

                    }

                    return false;

                }

            });

        if (files != null)
        {

            for (int i = 0; i < files.length; i++)
            {

                try
                {

                    RuleFactory.loadUserRule (files[i]);

                } catch (Exception e)
                {

                    // Delete the rule.
                    files[i].delete ();
                
                    Environment.logError ("Unable to load user rule: " +
                                          files[i],
                                          e);

                }

            }

        }

    }

    public static void removeUserRule (Rule r)
                                throws Exception
    {

        File f = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.USER_PROBLEM_FINDER_RULES_DIR + r.getId () + ".xml");

        if (f.exists ())
        {

            if (!f.delete ())
            {

                throw new GeneralException ("Unable to delete user rule file: " + f);

            }

            RuleFactory.rules.get (Rule.WORD_CATEGORY).remove (r.getId ());

        }

    }

    public static Map<String, String> getIgnores (int                                 type,
                                                  com.gentlyweb.properties.Properties projProps)
    {

        String toIgnore = "";

        if ((type == USER) ||
            (type == ALL))
        {

            String ignores = UserProperties.get (Constants.PROBLEM_FINDER_RULES_TO_IGNORE_PROPERTY_NAME);

            if (ignores != null)
            {

                toIgnore = ignores;

            }

        }

        if (toIgnore == null)
        {

            toIgnore = "";

        }

        // Get the to ignore from the project properties.
        if ((type == PROJECT) ||
            (type == ALL))
        {

            String ignores = projProps.getProperty (Constants.PROBLEM_FINDER_RULES_TO_IGNORE_PROPERTY_NAME);

            if (ignores != null)
            {

                toIgnore = toIgnore + ";" + ignores;

            }

        }

        Map<String, String> rulesToIgnore = new HashMap ();

        StringTokenizer t = new StringTokenizer (toIgnore,
                                                 ";");

        while (t.hasMoreTokens ())
        {

            String tok = t.nextToken ().trim ();

            if ((tok.equals (null + "")) ||
                (tok.equals ("")))
            {

                continue;

            }

            rulesToIgnore.put (tok,
                               "");

        }

        return rulesToIgnore;

    }

    public static void removeIgnore (Rule                                r,
                                     int                                 type,
                                     com.gentlyweb.properties.Properties projProps)
    {

        Map<String, String> ignores = RuleFactory.getIgnores (type,
                                                              projProps);

        ignores.remove (r.getId ());

        RuleFactory.saveIgnores (ignores,
                                 type,
                                 projProps);

    }

    public static void addIgnore (Rule                                r,
                                  int                                 type,
                                  com.gentlyweb.properties.Properties projProps)
    {

        // If this is a user rule then if we remove from all projects just delete it.
        if ((type == USER) &&
            (r.isUserRule ()))
        {

            try
            {

                RuleFactory.removeUserRule (r);

            } catch (Exception e)
            {

                Environment.logError ("Unable to remove user rule: " +
                                      r,
                                      e);

            }

            return;

        }

        Map<String, String> ignores = RuleFactory.getIgnores (type,
                                                              projProps);

        ignores.put (r.getId (),
                     "");

        RuleFactory.saveIgnores (ignores,
                                 type,
                                 projProps);

    }

    private static void saveIgnores (Map<String, String>                 ignores,
                                     int                                 type,
                                     com.gentlyweb.properties.Properties projProps)
    {

        StringBuilder b = new StringBuilder ();

        Iterator<String> iter = ignores.keySet ().iterator ();

        while (iter.hasNext ())
        {

            b.append (iter.next ());

            if (iter.hasNext ())
            {

                b.append (";");

            }

        }

        if (type == PROJECT)
        {

            StringProperty p = new StringProperty (Constants.PROBLEM_FINDER_RULES_TO_IGNORE_PROPERTY_NAME,
                                                   b.toString ());
            p.setDescription ("N/A");
    
            projProps.setProperty (Constants.PROBLEM_FINDER_RULES_TO_IGNORE_PROPERTY_NAME,
                                   p);
            
        }


        if (type == USER)
        {

            UserProperties.set (Constants.PROBLEM_FINDER_RULES_TO_IGNORE_PROPERTY_NAME,
                                b.toString ());

        }

    }

    private static Rule loadUserRule (File f)
                               throws Exception
    {

        Element el = JDOMUtils.getFileAsElement (f,
                                                 Environment.GZIP_EXTENSION);

        Rule r = RuleFactory.createRule (el,
                                         false);

        Map<String, Rule> l = RuleFactory.rules.get (r.getCategory ());

        if (l == null)
        {

            l = new HashMap ();
            RuleFactory.rules.put (r.getCategory (),
                                   l);

        }

        l.put (r.getId (),
               r);

        String fn = f.getName ();

        if (fn.startsWith ("user-"))
        {
            
            int ind = fn.lastIndexOf ('.');
            
            if (ind > 1)
            {
                
                try
                {
                    
                    RuleFactory.lastUserRuleIndex = Integer.parseInt (fn.substring (5,
                                                                      fn.length () - 4));
                    
                } catch (Exception e) {
                    
                    
                }

            }
            
        }

        return r;

    }

    public static void saveUserRule (Rule r)
                              throws Exception
    {

        if ((r.getId () == null) &&
            (r.isUserRule ()))
        {

            RuleFactory.lastUserRuleIndex++;

            r.setId ("user-" + RuleFactory.lastUserRuleIndex);

        }

        File f = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.USER_PROBLEM_FINDER_RULES_DIR + r.getId ().toLowerCase () + ".xml");

        if (!f.exists ())
        {

            if (!f.getParentFile ().exists ())
            {

                if (!f.getParentFile ().mkdirs ())
                {

                    throw new GeneralException ("Unable to create parent dirs for: " +
                                                f);

                }

            }

        }

        JDOMUtils.writeElementToFile (r.getAsElement (),
                                      f,
                                      true);

        RuleFactory.loadUserRule (f);

    }

    public static Rule getRuleById (String id)
    {

        Iterator<Map<String, Rule>> iter = RuleFactory.rules.values ().iterator ();

        while (iter.hasNext ())
        {

            Map<String, Rule> rules = iter.next ();

            Rule r = rules.get (id);

            if (r != null)
            {

                return r;

            }

        }

        return null;

    }

    public static Rule getRuleById (String id,
                                    String category)
    {

        Map<String, Rule> rs = RuleFactory.rules.get (category);

        if (rs == null)
        {

            return null;

        }

        return rs.get (id);

    }

    public static List<Issue> getParagraphIssues (Paragraph                           para,
                                                  com.gentlyweb.properties.Properties projProps)
    {

        List<Issue> issues = new ArrayList ();
    
        Map<String, Rule> rules = RuleFactory.rules.get (Rule.PARAGRAPH_CATEGORY);
        
        if (rules == null)
        {
            
            return issues;
            
        }
    
        Collection<Rule> iss = rules.values ();

        // Get the ignores.
        Map<String, String> ignores = RuleFactory.getIgnores (ALL,
                                                              projProps);

        for (Rule r : iss)
        {

            if (ignores.containsKey (r.getId ()))
            {

                continue;

            }

            ParagraphRule pr = (ParagraphRule) r;
            
            issues.addAll (r.getIssues (para));

        }
        
        return issues;
        
    }

    public static List<Issue> getSentenceIssues (Sentence                            sentence,
                                                 com.gentlyweb.properties.Properties projProps)
    {

        List<Issue> issues = new ArrayList ();
    
        // Get the ignores.
        Map<String, String> ignores = RuleFactory.getIgnores (ALL,
                                                              projProps);

        Map<String, Rule> rules = RuleFactory.rules.get (Rule.WORD_CATEGORY);
    
        if (rules != null)
        {
    
            Collection<Rule> wordIss = rules.values ();
            
            for (Rule r : wordIss)
            {
    
                if (ignores.containsKey (r.getId ()))
                {
    
                    continue;
    
                }
    
                SentenceRule sr = (SentenceRule) r;
    
                issues.addAll (sr.getIssues (sentence));
    
            }

        }
            
        rules = RuleFactory.rules.get (Rule.SENTENCE_CATEGORY);
        
        if (rules != null)
        {
            
            Collection<Rule> sRules = rules.values ();
    
            for (Rule r : sRules)
            {
    
                if (ignores.containsKey (r.getId ()))
                {
    
                    continue;
    
                }
    
                SentenceRule sr = (SentenceRule) r;
                
                issues.addAll (r.getIssues (sentence));
    
            }

        }
            
        return issues;

    }

    private static Rule createRule (Element root,
                                    boolean userRule)
                             throws JDOMException
    {

        String type = JDOMUtils.getAttributeValue (root,
                                                   AbstractRule.XMLConstants.createType);

        Rule r = null;

        if (type.equals (WordFinder.CREATE_TYPE))
        {

            r = new WordFinder (userRule);

        }

        if (type.equals (AdverbRule.CREATE_TYPE))
        {

            r = new AdverbRule (userRule);

        }

        if (type.equals (PassiveSentenceRule.CREATE_TYPE))
        {

            r = new PassiveSentenceRule (userRule);

        }

        if (type.equals (TooManyClausesRule.CREATE_TYPE))
        {

            r = new TooManyClausesRule (userRule);

        }

        if (type.equals (SentenceLengthRule.CREATE_TYPE))
        {

            r = new SentenceLengthRule (userRule);

        }

        if (type.equals (SentenceComplexityRule.CREATE_TYPE))
        {

            r = new SentenceComplexityRule (userRule);

        }

        if (type.equals (ParagraphLengthRule.CREATE_TYPE))
        {

            r = new ParagraphLengthRule (userRule);

        }

        if (type.equals (ParagraphReadabilityRule.CREATE_TYPE))
        {

            r = new ParagraphReadabilityRule (userRule);

        }
        
        r.init (root);

        return r;

    }

    public static List<Rule> getWordRules ()
    {

        return RuleFactory.getRules (Rule.WORD_CATEGORY);

    }

    public static List<Rule> getSentenceRules ()
    {

        return RuleFactory.getRules (Rule.SENTENCE_CATEGORY);

    }

    public static List<Rule> getParagraphRules ()
    {

        return RuleFactory.getRules (Rule.PARAGRAPH_CATEGORY);

    }
    
    public static List<Rule> getRules (String category)
    {

        return new ArrayList (RuleFactory.rules.get (category).values ());

    }

    public static Map<String, Map<String, Rule>> getRules ()
    {

        return RuleFactory.rules;

    }

}
