package com.quollwriter.text.rules;

import java.awt.Component;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;

import com.quollwriter.synonyms.*;

import com.quollwriter.text.*;

import com.quollwriter.ui.components.*;

import org.jdom.*;


public class AdverbRule extends AbstractSentenceRule
{

    public static final String CREATE_TYPE = "adverb";

    public class XMLConstants
    {

        public static final String speechVerbs = "speechVerbs";

    }

    private Map<String, String> speechVerbs = new HashMap ();
    private Map<String, String> excludeAdverbs = new HashMap ();
    private JTextField          newVerbs = null;
    private DefaultListModel    listModel = null;

    public AdverbRule(boolean user)
    {

        super (user);

    }

    public String getCategory ()
    {

        return Rule.SENTENCE_CATEGORY;

    }

    public String getCreateType ()
    {

        return AdverbRule.CREATE_TYPE;

    }

    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        String sw = JDOMUtils.getAttributeValue (root,
                                                 XMLConstants.speechVerbs);

        StringTokenizer t = new StringTokenizer (sw,
                                                 ",");

        while (t.hasMoreTokens ())
        {

            this.speechVerbs.put (t.nextToken ().trim ().toLowerCase (),
                                  "");


        }

    }

    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        StringBuilder b = new StringBuilder ();

        Iterator<String> iter = this.speechVerbs.keySet ().iterator ();

        while (iter.hasNext ())
        {

            b.append (iter.next ());

            if (iter.hasNext ())
            {

                b.append (",");

            }

        }

        root.setAttribute (XMLConstants.speechVerbs,
                           b.toString ());

        return root;

    }

    public List<Issue> getIssues (String  sentence,
                                  boolean inDialogue)
    {

        // Check our list of words.
        sentence = sentence.toLowerCase ();

        List<String> swords = TextUtilities.getAsWords (sentence);
        List<Issue>  issues = new ArrayList ();

        int sws = swords.size ();

        String adverbWT = String.valueOf (Synonyms.ADVERB);

        for (int i = 0; i < sws; i++)
        {

            String w = swords.get (i);

            inDialogue = TextUtilities.stillInDialogue (w,
                                                        inDialogue);

            if (inDialogue)
            {

                continue;

            }

            if (this.speechVerbs.containsKey (w))
            {

                // It does contain it, so now check the next word.
                if (i < (sws - 1))
                {

                    String nw = swords.get (i + 1);

                    try
                    {

                        String wt = Environment.getWordTypes (nw);

                        if (wt != null)
                        {

                            // We are only interested in those that are purely adverbs (no other word types)
                            if (wt.equals (adverbWT))
                            {

                                // Maybe check to see if it's after a "

                                // Add an issue.
                                Issue iss = new Issue ("Use of adverb: <b>" + nw +
                                                       "</b> to modify speech verb: <b>" + w + "</b>",
                                                       i,
                                                       w.length () + nw.length () + 1,
                                                       this);
                                issues.add (iss);

                            }

                        }


                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to check for word: " +
                                              nw +
                                              " being an adverb.");

                    }

                }

            }


        }

        return issues;

    }

    public List<FormItem> getFormItems ()
    {

        final AdverbRule _this = this;

        List<FormItem> items = new ArrayList ();

        Box b = new Box (BoxLayout.Y_AXIS);

        this.newVerbs = com.quollwriter.ui.UIUtils.createTextField ();

        b.add (newVerbs);

        JLabel label = new JLabel ("(separate with , or ;)");
        label.setBorder (new EmptyBorder (0,
                                          5,
                                          0,
                                          0));

        b.add (label);

        items.add (new FormItem ("New Speech Verbs",
                                 b));

        Vector v = new Vector (this.speechVerbs.keySet ());

        Collections.sort (v);

        this.listModel = new DefaultListModel ();

        for (int i = 0; i < v.size (); i++)
        {

            this.listModel.addElement (v.get (i));

        }

        b = new Box (BoxLayout.X_AXIS);

        final JList verbs = new JList (this.listModel);

        verbs.setVisibleRowCount (5);
        verbs.setMaximumSize (verbs.getPreferredSize ());

        b.add (new JScrollPane (verbs));

        b.add (Box.createHorizontalStrut (5));

        Box bb = new Box (BoxLayout.Y_AXIS);

        ImagePanel del = new ImagePanel (Environment.getIcon ("delete",
                                                              Constants.ICON_MENU),
                                         null);
        del.setBorder (null);
        del.setOpaque (false);
        del.setToolTipText ("Click to remove the selected Speech Verbs");
        com.quollwriter.ui.UIUtils.setAsButton (del);
        del.setAlignmentY (Component.TOP_ALIGNMENT);

        del.addMouseListener (new MouseAdapter ()
            {

                public void mouseReleased (MouseEvent ev)
                {

                    // Get the selected items, remove them from the model.
                    int[] inds = verbs.getSelectedIndices ();

                    if (inds != null)
                    {

                        for (int i = inds.length - 1; i > -1; i--)
                        {

                            _this.listModel.remove (inds[i]);

                        }

                    }

                }

            });

        bb.add (del);
        bb.add (Box.createVerticalGlue ());

        b.add (bb);
        b.add (Box.createHorizontalGlue ());

        items.add (new FormItem ("Speech Verbs",
                                 b));

        return items;

    }

    public void updateFromForm ()
    {

        // Reset the speech verbs.
        Map<String, String> verbs = new HashMap ();

        String n = this.newVerbs.getText ();

        if (n != null)
        {

            StringTokenizer t = new StringTokenizer (n,
                                                     ";,");

            while (t.hasMoreTokens ())
            {

                verbs.put (t.nextToken ().trim ().toLowerCase (),
                           "");

            }

        }

        Enumeration en = this.listModel.elements ();

        while (en.hasMoreElements ())
        {

            verbs.put ((String) en.nextElement (),
                       "");

        }

        this.speechVerbs = verbs;

    }

}
