package com.quollwriter.text.rules;

public abstract class AbstractSentenceRule extends AbstractRule implements SentenceRule
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

}
