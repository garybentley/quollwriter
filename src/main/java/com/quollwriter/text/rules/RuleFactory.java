package com.quollwriter.text.rules;

import java.io.*;
import java.nio.file.*;
import java.util.stream.*;

import java.util.*;

import com.gentlyweb.properties.StringProperty;

import com.quollwriter.*;

import com.quollwriter.text.*;

import org.dom4j.*;

public class RuleFactory
{

    public static final int USER = 1;
    public static final int PROJECT = 2;
    public static final int ALL = 4;

    private static Map<String, Map<String, Rule>> rules = new HashMap<> ();
    private static Map<String, Class> ruleTypes = new HashMap<> ();

    private static int lastUserRuleIndex = 0;

    public static void init ()
                      throws Exception
    {

        // Load the standard rules.
        String xml = Utils.getResourceFileAsString (Constants.PROBLEM_FINDER_RULES_FILE);

        Element root = DOM4JUtils.stringAsElement (xml);

        for (Element el : root.elements (AbstractRule.XMLConstants.root))
        {

            Rule r = null;

            try
            {

                r = RuleFactory.createRule (el,
                                            false);

            } catch (Exception e) {

                Environment.logError ("Unable to create rule",
                                      new GeneralException ("Unable to create rule from element: " +
                                                            DOM4JUtils.getPath (el),
                                                            e));

                continue;

            }

            Map<String, Rule> l = RuleFactory.rules.get (r.getCategory ());

            if (l == null)
            {

                l = new HashMap<> ();
                RuleFactory.rules.put (r.getCategory (),
                                       l);

            }

            l.put (r.getId (),
                   r);

        }

        // Load the user defined rules.
        Path dir = Environment.getUserPath (Constants.USER_PROBLEM_FINDER_RULES_DIR);

        if (Files.exists (dir))
        {

            try (Stream<Path> ls = Files.walk (dir))
            {

                ls.filter (f -> f.getFileName ().toString ().endsWith (".xml"))
                .forEach (f ->
                {

                    try
                    {

                        RuleFactory.loadUserRule (f);

                    } catch (Exception e)
                    {

                        // Delete the rule.
                        try
                        {

                            Files.delete (f);

                        } catch (Exception ee) {

                            Environment.logError ("Unable to delete user rule file: " +
                                                  f,
                                                  ee);

                        }

                        Environment.logError ("Unable to load user rule: " +
                                              f,
                                              e);

                    }

                });

            }

        }

    }

    public static Path getUserRuleFilePath (Rule r)
    {

        return RuleFactory.getUserRulesDirPath ().resolve (r.getId () + ".xml");

    }

    public static Path getUserRulesDirPath ()
    {

        return Environment.getUserPath (Constants.USER_PROBLEM_FINDER_RULES_DIR);

    }

    public static void removeUserRule (Rule r)
                                throws Exception
    {

        Path f = RuleFactory.getUserRuleFilePath (r);

        if (Files.deleteIfExists (f))
        {

            RuleFactory.rules.get (Rule.WORD_CATEGORY).remove (r.getId ());

        } else {

            throw new GeneralException ("Unable to delete user rule file: " + f);

        }
/*
TODO
        File f = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + Constants.USER_PROBLEM_FINDER_RULES_DIR + r.getId () + ".xml");

        if (f.exists ())
        {

            if (!f.delete ())
            {

                throw new GeneralException ("Unable to delete user rule file: " + f);

            }

            RuleFactory.rules.get (Rule.WORD_CATEGORY).remove (r.getId ());

        }
*/
    }

    public static Set<Rule> getProjectRules (com.gentlyweb.properties.Properties projProps)
    {

        Map<String, String> ignores = RuleFactory.getIgnores (ALL,
                                                              projProps);

        Set<String> iids = ignores.keySet ();

        Set<Rule> rs = new HashSet<> ();

        for (String t : RuleFactory.rules.keySet ())
        {

            Map<String, Rule> rrs = RuleFactory.rules.get (t);

            for (Rule r : rrs.values ())
            {

                if (iids.contains (r.getId ()))
                {

                    continue;

                }

                rs.add (r);

            }

        }

        return rs;

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

        Map<String, String> rulesToIgnore = new HashMap<> ();

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

    private static Rule loadUserRule (Path f)
                               throws Exception
    {

        Element el = DOM4JUtils.fileAsElement (f);

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

        String fn = f.getFileName ().toString ();

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

        Path f = RuleFactory.getUserRuleFilePath (r);

        Files.createDirectories (f.getParent ());
/*
TODO Clean up
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
*/
        DOM4JUtils.writeToFile (r.getAsElement (),
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

    public static List<Issue> getIssues (TextBlock                           block,
                                         Rule                                rule,
                                         com.gentlyweb.properties.Properties projProps)
    {

        List<Issue> issues = new ArrayList<> ();

        if ((rule == null)
            ||
            (block == null)
           )
        {

            return issues;

        }

        if ((block instanceof Paragraph)
            &&
            (!(rule instanceof ParagraphRule))
           )
        {

            return issues;

        }

        if ((block instanceof Sentence)
            &&
            (!(rule instanceof SentenceRule))
           )
        {

            return issues;

        }

        // Get the ignores.
        Map<String, String> ignores = RuleFactory.getIgnores (ALL,
                                                              projProps);

        if (ignores.containsKey (rule.getId ()))
        {

            return issues;

        }

        return rule.getIssues (block);

    }

    public static List<Issue> getParagraphIssues (Paragraph                           para,
                                                  com.gentlyweb.properties.Properties projProps)
    {

        List<Issue> issues = new ArrayList<> ();

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

        List<Issue> issues = new ArrayList<> ();

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
                             throws GeneralException
    {

        String type = DOM4JUtils.attributeValue (root,
                                                   AbstractRule.XMLConstants.createType);

        Class c = null;

        try
        {

            c = Class.forName (type);

        } catch (Exception e) {

            // Search for an existing type with the same id.
            Rule r = RuleFactory.createLegacyRule (root,
                                                   userRule);

            if (r != null)
            {

                return r;

            }

            throw new GeneralException (String.format ("Unable to load class: %s, referenced by: %s",
                                                    type,
                                                    DOM4JUtils.getPath (root.attribute (AbstractRule.XMLConstants.createType))),
                                     e);

        }

        Rule r = null;

        try
        {

            r = (Rule) c.getDeclaredConstructor ().newInstance ();

        } catch (Exception e) {

            throw new GeneralException (String.format ("Unable to create new instance of rule for class: %s, referenced by: %s",
                                                    type,
                                                    DOM4JUtils.getPath (root.attribute (AbstractRule.XMLConstants.createType))),
                                     e);

        }

        r.setUserRule (userRule);

        r.init (root);

        return r;

    }

    /**
     * Creates rules based on the old "createType" values that were used prior to version 2.5.4.
     * The values are now hard-coded since they have been removed from the classes themselves.
     *
     * @param root The root element to use to create the rule.
     * @param userRule Is this a user rule.
     * @returns The created rule.
     */
    private static Rule createLegacyRule (Element root,
                                          boolean userRule)
                                   throws GeneralException
    {

        Rule r = null;

        String type = DOM4JUtils.attributeValue (root,
                                                   AbstractRule.XMLConstants.createType);

        if (type.equals ("wordFinder"))
        {

            r = new WordFinder ();

        }

        if (type.equals ("adverb"))
        {

            r = new AdverbRule ();

        }

        if (type.equals ("passivesentence"))
        {

            r = new PassiveSentenceRule ();

        }

        if (type.equals ("toomanyclauses"))
        {

            r = new TooManyClausesRule ();

        }

        if (type.equals ("sentencelength"))
        {

            r = new SentenceLengthRule ();

        }

        if (type.equals ("sentencecomplexity"))
        {

            r = new SentenceComplexityRule ();

        }

        if (type.equals ("paragraphlength"))
        {

            r = new ParagraphLengthRule ();

        }

        if (type.equals ("paragraphreadability"))
        {

            r = new ParagraphReadabilityRule ();

        }

        if (r == null)
        {

            return null;

        }

        r.setUserRule (userRule);

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

        return new ArrayList<> (RuleFactory.rules.get (category).values ());

    }

    public static Map<String, Map<String, Rule>> getRules ()
    {

        return RuleFactory.rules;

    }

}
