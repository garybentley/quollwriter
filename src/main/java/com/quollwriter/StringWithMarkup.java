package com.quollwriter;

public class StringWithMarkup
{

    private String text = null;
    private Markup markup = null;

    public StringWithMarkup (String text,
                             Markup markup)
    {

        this.text = text;
        this.markup = markup;

    }

    public StringWithMarkup (String text,
                             String markup)
    {

        this.text = text;

        if (markup != null)
        {

            this.markup = new Markup (markup);

        }

    }

    public StringWithMarkup (String text)
    {

        this.text = text;

    }

    public boolean hasText ()
    {

        if (this.text == null)
        {

            return false;

        }

        return this.text.trim ().length () > 0;

    }

    public void update (StringWithMarkup t)
    {

        if (t.getText () != null)
        {

            this.text = new String (t.getText ());

        }

        if (t.getMarkup () != null)
        {

            this.markup = new Markup (t.getMarkup ().toString ());

        }

    }

    public void update (String text,
                        Markup markup)
    {

        this.text = text;
        this.markup = markup;

    }

    public String getMarkedUpText ()
    {

        if (this.text == null)
        {

            return null;

        }

        if (this.markup == null)
        {

            return this.text;

        }

        return this.markup.markupAsHTML (this.text);

    }

    public String getText ()
    {

        return this.text;

    }

    public Markup getMarkup ()
    {

        return this.markup;

    }

}
