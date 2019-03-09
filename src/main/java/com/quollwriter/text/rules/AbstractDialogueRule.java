package com.quollwriter.text.rules;

import com.gentlyweb.xml.*;

import com.quollwriter.text.*;

import org.jdom.*;


public abstract class AbstractDialogueRule extends AbstractRule<Sentence> implements DialogueRule
{

    public class XMLConstants
    {

        public static final String location = "location";
        public static final String ignoreInDialogue = "ignoreInDialogue";
        public static final String onlyInDialogue = "onlyInDialogue";

    }

    protected String  where = DialogueConstraints.ANYWHERE;
    protected boolean ignoreInDialogue = true;
    protected boolean onlyInDialogue = false;

    public AbstractDialogueRule ()
    {

    }

    public DialogueConstraints getConstraints ()
    {

        return new DialogueConstraints (this.onlyInDialogue,
                                        this.ignoreInDialogue,
                                        this.where);

    }

    public void setWhere (String w)
    {

        this.where = w;

    }

    public String getWhere ()
    {

        return this.where;

    }

    public void setOnlyInDialogue (boolean d)
    {

        this.onlyInDialogue = d;

    }

    public void setIgnoreInDialogue (boolean d)
    {

        this.ignoreInDialogue = d;

    }

    public boolean isOnlyInDialogue ()
    {

        return this.onlyInDialogue;

    }

    public boolean isIgnoreInDialogue ()
    {

        return this.ignoreInDialogue;

    }

    @Override
    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        this.where = JDOMUtils.getAttributeValue (root,
                                                  XMLConstants.location,
                                                  false);

        if (this.where.equals (""))
        {

            this.where = DialogueConstraints.ANYWHERE;

        }

        this.ignoreInDialogue = JDOMUtils.getAttributeValueAsBoolean (root,
                                                                      XMLConstants.ignoreInDialogue,
                                                                      false);
        this.onlyInDialogue = JDOMUtils.getAttributeValueAsBoolean (root,
                                                                    XMLConstants.onlyInDialogue,
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

        if (this.onlyInDialogue)
        {

            root.setAttribute (XMLConstants.onlyInDialogue,
                               Boolean.toString (this.onlyInDialogue));

        }

        if (this.where != null)
        {

            root.setAttribute (XMLConstants.location,
                               this.where);

        }

        return root;

    }

}
