package com.quollwriter.text;

import java.util.*;
import javax.swing.text.*;

import com.quollwriter.*;

public class Issue
{

    private String   desc = null;
    private Position startPos = null;
    private Position endPos = null;
    private int      startIssuePosition = -1;
    private int      length = -1;
    private String   ruleId = null;
    private Rule     rule = null;
    private String   issueId = null;

    public Issue (String    desc,
                  TextBlock text,
                  String    issueId,
                  Rule      rule)
    {

        this (desc,
              text.getAllTextStartOffset (),
              text.getText ().length (),
              issueId,
              rule);

    }

    public Issue (String    desc,
                  int       startIssuePosition,
                  int       length,
                  String    issueId,
                  Rule      rule)
    {

        this.desc = desc;
        this.startIssuePosition = startIssuePosition;
        this.length = length;
        this.rule = rule;
        this.ruleId = rule.getId ();
        this.issueId = issueId;

    }
    
    @Override
    public String toString ()
    {
                
        Map<String, Object> props = new LinkedHashMap ();
        
        props.put ("ruleId",
                   this.ruleId);
        props.put ("ruleSummary",
                   this.rule.getSummary ());
        props.put ("ruleCategory",
                   this.rule.getCategory ());
        props.put ("issueId",
                   this.issueId);
        props.put ("description",
                   this.desc);
        props.put ("startIssuePosition",
                   this.startIssuePosition);
        props.put ("startPosition",
                   (this.startPos == null ? "null" : this.startPos.getOffset ()));
        props.put ("endPosition",
                   (this.endPos == null ? "null" : this.endPos.getOffset ()));
        props.put ("length",
                   this.length);
                
        return Environment.formatObjectToStringProperties (props);        
        
    }
    
    public String getIssueId ()
    {
        
        return this.issueId;
        
    }
    
    public String getRuleId ()
    {

        return this.ruleId;

    }

    public Rule getRule ()
    {

        return this.rule;

    }

    @Override
    public int hashCode ()
    {
        
        int hash = 7;
        hash = (31 * hash) + ((null == this.issueId) ? 0 : this.issueId.hashCode ());
        hash = (31 * hash) + ((null == this.ruleId) ? 0 : this.ruleId.hashCode ());
        hash = (31 * hash) + this.startIssuePosition;

        return hash;
    
    }
    
    public boolean equals (Issue iss)
    {

        if ((this.startPos.getOffset () == iss.startPos.getOffset ())
            &&
            (this.startIssuePosition == iss.startIssuePosition)
            &&
            (this.ruleId.equals (iss.ruleId))
            &&
            (this.issueId.equals (iss.issueId))
           )
        {

            return true;

        }

        return false;

    }

    @Override
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

}
