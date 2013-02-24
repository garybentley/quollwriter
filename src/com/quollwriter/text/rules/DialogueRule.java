package com.quollwriter.text.rules;

import com.quollwriter.text.Rule;

import com.quollwriter.text.*;

public interface DialogueRule extends Rule
{
/*
    public static final String START = "start";
    public static final String ANYWHERE = "anywhere";
    public static final String END = "end";
*/
    public String getWhere ();

    public void setWhere (String w);

    public void setOnlyInDialogue (boolean d);

    public void setIgnoreInDialogue (boolean d);

    public boolean isOnlyInDialogue ();

    public boolean isIgnoreInDialogue ();

    public DialogueConstraints getConstraints ();

}
