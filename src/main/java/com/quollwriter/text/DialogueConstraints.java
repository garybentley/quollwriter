package com.quollwriter.text;

public class DialogueConstraints
{

    public static final String START = "start";
    public static final String ANYWHERE = "anywhere";
    public static final String END = "end";

    public boolean onlyInDialogue = false;
    public boolean ignoreInDialogue = true;
    public String  where = DialogueConstraints.ANYWHERE;

    public DialogueConstraints()
    {

    }

    public DialogueConstraints (boolean onlyInDialogue,
                                boolean ignoreInDialogue,
                                String  where)
    {
        
        this.onlyInDialogue = onlyInDialogue;
        this.ignoreInDialogue = ignoreInDialogue;
        this.where = where;
        
        if (this.where == null)
        {
            
            this.where = DialogueConstraints.ANYWHERE;
            
        }
        
    }

    public String toString ()
    {

        return ("onlyInDialogue: " + this.onlyInDialogue + ", ignoreInDialogue: " + this.ignoreInDialogue + ", where: " + this.where);

    }

}
