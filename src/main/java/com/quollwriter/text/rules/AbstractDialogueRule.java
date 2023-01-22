package com.quollwriter.text.rules;

import com.quollwriter.text.*;
import com.quollwriter.*;

import org.dom4j.*;


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
               throws GeneralException
    {

        super.init (root);

        this.where = DOM4JUtils.attributeValue (root,
                                                  XMLConstants.location,
                                                  false);

        if (this.where.equals (""))
        {

            this.where = DialogueConstraints.ANYWHERE;

        }

        this.ignoreInDialogue = DOM4JUtils.attributeValueAsBoolean (root,
                                                                      XMLConstants.ignoreInDialogue,
                                                                      false);
        this.onlyInDialogue = DOM4JUtils.attributeValueAsBoolean (root,
                                                                    XMLConstants.onlyInDialogue,
                                                                    false);

    }

    @Override
    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        if (this.ignoreInDialogue)
        {

            root.addAttribute (XMLConstants.ignoreInDialogue,
                               Boolean.toString (this.ignoreInDialogue));

        }

        if (this.onlyInDialogue)
        {

            root.addAttribute (XMLConstants.onlyInDialogue,
                               Boolean.toString (this.onlyInDialogue));

        }

        if (this.where != null)
        {

            root.addAttribute (XMLConstants.location,
                               this.where);

        }

        return root;

    }

}
