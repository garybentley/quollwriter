package com.quollwriter.text;

import javax.swing.text.*;


public class Issue
{

    private String   desc = null;
    private Position startPos = null;
    private Position endPos = null;
    private int      startIssuePosition = -1;
    private int      length = -1;
    private String   ruleId = null;
    private Rule     rule = null;

    public Issue (String    desc,
                  TextBlock text,
                  Rule      rule)
    {

        this (desc,
              text.getAllTextStartOffset (),
              text.getText ().length (),
              rule);

    }

    public Issue (String    desc,
                  int       startIssuePosition,
                  int       length,
                  Rule      rule)
    {

        this.desc = desc;
        this.startIssuePosition = startIssuePosition;
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

        if ((this.startPos.getOffset () == iss.startPos.getOffset ()) &&
            (this.startIssuePosition == iss.startIssuePosition) &&
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

    public int getStartIssuePosition ()
    {

        return this.startIssuePosition;

    }

    public int getEndIssuePosition ()
    {
        
        return this.startIssuePosition + this.getLength ();
        
    }
    
    public int getLength ()
    {

        return this.length;

    }

    public Position getStartPosition ()
    {

        return this.startPos;

    }

    public Position getEndPosition ()
    {

        return this.endPos;

    }

    public void setStartPosition (Position p)
    {

        this.startPos = p;

    }

    public void setEndPosition (Position p)
    {

        this.endPos = p;

    }

    public String toString ()
    {

        return this.desc + ": " + this.startPos + " - " + this.endPos + "(" + this.startIssuePosition + " - " + this.length + ")";

    }

}
