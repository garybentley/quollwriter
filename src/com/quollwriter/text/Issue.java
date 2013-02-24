package com.quollwriter.text;

import javax.swing.text.*;


public class Issue
{

    private String   desc = null;
    private Position sentenceStartPos = null;
    private Position sentenceEndPos = null;
    private int      startWordPosition = -1;
    private int      length = -1;
    private String   ruleId = null;
    private Rule     rule = null;

    public Issue(String desc,
                 int    startWordPosition,
                 int    length,
                 Rule   rule)
    {

        this.desc = desc;
        this.startWordPosition = startWordPosition;
        this.length = length;
        this.rule = rule;
        this.ruleId = rule.getId ();

    }

    public String getRuleId ()
    {

        return this.ruleId;

    }

    public Rule getRule ()
    {

        return this.rule;

    }

    public boolean equals (Issue iss)
    {

        if ((this.sentenceStartPos.getOffset () == iss.sentenceStartPos.getOffset ()) &&
            (this.startWordPosition == iss.startWordPosition) &&
            (this.ruleId.equals (iss.ruleId)))
        {

            return true;

        }

        return false;

    }

    public boolean equals (Object o)
    {

        if (o instanceof Issue)
        {
            
            return this.equals ((Issue) o);
            
        }

        return false;

    }

    public String getDescription ()
    {

        return this.desc;

    }

    public int getStartWordPosition ()
    {

        return this.startWordPosition;

    }

    public int getLength ()
    {

        return this.length;

    }

    public Position getSentenceStartPosition ()
    {

        return this.sentenceStartPos;

    }

    public Position getSentenceEndPosition ()
    {

        return this.sentenceEndPos;

    }

    public void setSentenceStartPosition (Position p)
    {

        this.sentenceStartPos = p;

    }

    public void setSentenceEndPosition (Position p)
    {

        this.sentenceEndPos = p;

    }

    public String toString ()
    {

        return this.desc + ": " + this.sentenceStartPos + " - " + this.sentenceEndPos + "(" + this.startWordPosition + " - " + this.length + ")";

    }

}
