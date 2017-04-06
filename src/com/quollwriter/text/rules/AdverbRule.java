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
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;

import org.jdom.*;


public class AdverbRule extends AbstractSentenceRule
{

    public class XMLConstants
    {

        public static final String speechVerbs = "speechVerbs";

    }

    private Set<String> speechVerbs = new HashSet ();
    private JTextField          newVerbs = null;
    private DefaultListModel    listModel = null;

    public AdverbRule ()
    {

    }

    @Override
    public String getCategory ()
    {

        return Rule.SENTENCE_CATEGORY;

    }

    @Override
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

            this.speechVerbs.add (t.nextToken ().trim ().toLowerCase ());

        }

    }

    public void setSpeechVerbs (Set<String> verbs)
    {

        this.speechVerbs = verbs;

    }

    public boolean isSpeechVerb (String w)
    {

        if (w == null)
        {

            return false;

        }

        return this.speechVerbs.contains (w.toLowerCase ());

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        StringBuilder b = new StringBuilder ();

        for (String w : this.speechVerbs)
        {

            if (b.length () > 0)
            {

                b.append (",");

            }

            b.append (w);

        }

        root.setAttribute (XMLConstants.speechVerbs,
                           b.toString ());

        return root;

    }

    @Override
    public List<Issue> getIssues (Sentence sentence)
    {

        List<Issue>  issues = new ArrayList ();

        String adverbWT = String.valueOf (Synonyms.ADVERB);

        List<Word> swords = sentence.getWords ();

        for (Word w : swords)
        {

            if (w.isInDialogue ())
            {

                continue;

            }

            if (this.isSpeechVerb (w.getText ()))
            {

                Word nw = w.getNext ();

                if (nw != null)
                {

                    try
                    {

                        String wt = Environment.getWordTypes (nw.getText (),
                                                              // We assume english for now
                                                              null);

                        if (wt != null)
                        {

                            // We are only interested in those that are purely adverbs (no other word types)
                            if (wt.equals (adverbWT))
                            {

                                // Maybe check to see if it's after a "

                                // Add an issue.
                                Issue iss = new Issue ("Use of adverb: <b>" + nw.getText () +
                                                       "</b> to modify speech verb: <b>" + w.getText () + "</b>",
                                                       sentence,
                                                       sentence.getAllTextStartOffset () + "-" + nw.getText () + "-" + w.getText (),
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

    @Override
    public Set<FormItem> getFormItems ()
    {

        final AdverbRule _this = this;

        Set<FormItem> items = new LinkedHashSet ();

        Box b = new Box (BoxLayout.Y_AXIS);

        this.newVerbs = com.quollwriter.ui.UIUtils.createTextField ();

        b.add (newVerbs);

        JLabel label = new JLabel ("(separate with , or ;)");
        label.setBorder (new EmptyBorder (0,
                                          5,
                                          0,
                                          0));

        b.add (label);

        items.add (new AnyFormItem ("New Speech Verbs",
                                    b));

        Vector v = new Vector (this.speechVerbs);

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

        List<JComponent> buts = new ArrayList ();
        
        buts.add (UIUtils.createButton (Constants.DELETE_ICON_NAME,
                                        Constants.ICON_MENU,
                                        "Click to remove the selected Speech Verbs",
                                        new ActionListener ()
        {
            
            @Override
            public void actionPerformed (ActionEvent ev)
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
            
        }));

/*        

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
*/
        bb.add (UIUtils.createButtonBar (buts));
        bb.add (Box.createVerticalGlue ());

        b.add (bb);
        b.add (Box.createHorizontalGlue ());

        items.add (new AnyFormItem ("Speech Verbs",
                                    b));

        return items;

    }

    @Override
    public String getFormError ()
    {

        return null;

    }

    public void updateFromForm ()
    {

        // Reset the speech verbs.
        Set<String> verbs = new HashSet ();

        String n = this.newVerbs.getText ();

        if (n != null)
        {

            StringTokenizer t = new StringTokenizer (n,
                                                     ";,");

            while (t.hasMoreTokens ())
            {

                verbs.add (t.nextToken ().trim ().toLowerCase ());

            }

        }

        Enumeration en = this.listModel.elements ();

        while (en.hasMoreElements ())
        {

            verbs.add ((String) en.nextElement ());

        }

        this.speechVerbs = verbs;

    }

}
