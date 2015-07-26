package com.quollwriter.text.rules;

import com.quollwriter.text.*;

public abstract class AbstractSentenceRule extends AbstractRule<Sentence> implements SentenceRule
{

    public AbstractSentenceRule(boolean userRule)
    {

        super (userRule);

    }

    public String getEditSummary ()
    {

        return this.summary;

    }

    public String getEditDescription ()
    {

        return this.desc;

    }

    @Override
    public String getEditFormTitle (boolean add)
    {
        
        return (add ? "Add new Sentence Structure rule" : null);
        
    }
    
}
