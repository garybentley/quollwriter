package com.quollwriter.text.rules;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.text.*;

import com.quollwriter.ui.forms.*;

import org.jdom.Element;
import org.jdom.JDOMException;


public class DoubleWordRule extends AbstractSentenceRule
{

    public DoubleWordRule ()
    {

    }

    public String getDescription ()
    {

        String d = super.getDescription ();

        return d;

    }

    @Override
    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        return root;

    }

    private boolean hasThree (Word w,
                              String find)
    {

        if (w.textEquals (find))
        {

            Word n = w.getNext ();

            if (n != null)
            {

                if (n.textEquals (find))
                {

                    Word nn = n.getNext ();

                    if (nn != null)
                    {

                        if (nn.textEquals (find))
                        {

                            return true;

                        }

                    }

                }

            }

        }

        return false;

    }

    @Override
    public List<Issue> getIssues (Sentence sentence)
    {

        List<Issue> issues = new ArrayList ();

        if (sentence == null)
        {

            return issues;

        }

        if (sentence.getWords ().size () == 0)
        {

            return issues;

        }

        int dotC = 0;

        Iterator<Word> iter = sentence.getWords ().iterator ();

        Word curr = iter.next ();

        while (curr != null)
        {

            if ((this.hasThree (curr,
                                "."))
                ||
                (this.hasThree (curr,
                                "*"))
               )
            {

                // This is an ellipsis or * * *.
                curr = iter.next ();
                curr = iter.next ();

                // Push the iterator onto the word beyond the last .
                if (iter.hasNext ())
                {

                    curr = iter.next ();
                    continue;

                } else {

                    // No more words.
                    break;

                }

            }

            // We check on the previous word.
            Word prev = curr.getPrevious ();

            if (prev != null)
            {

                if (curr.textEquals (prev))
                {

                    Issue iss = null;

                    if (curr.isPunctuation ())
                    {

                        iss = new Issue (String.format ("Double punctuation: <b>%s%s</b>",
                                                        curr.getText (),
                                                        curr.getText ()),
                                         sentence,
                                         prev.getAllTextStartOffset (),
                                         curr.getAllTextEndOffset () - prev.getAllTextStartOffset (),
                                         curr.getAllTextStartOffset () + "-doubleword",
                                         this);

                    } else {

                        iss = new Issue (String.format ("Double word: <b>%s %s</b>.",
                                                        curr.getText (),
                                                        curr.getText ()),
                                         sentence,
                                         prev.getAllTextStartOffset (),
                                         curr.getAllTextEndOffset () - prev.getAllTextStartOffset (),
                                         curr.getAllTextStartOffset () + "-doubleword",
                                         this);

                    }

                    issues.add (iss);

                }

            }

            if (iter.hasNext ())
            {

                curr = iter.next ();

            } else {

                curr = null;

            }

        }

        return issues;

    }

    @Override
    public String getCategory ()
    {

        return Rule.SENTENCE_CATEGORY;

    }

    @Override
    public Set<FormItem> getFormItems ()
    {

        Set<FormItem> items = new LinkedHashSet ();

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

    }

}
