package com.quollwriter;

import javafx.beans.property.*;

public class StringWithMarkup
{

    private String text = null;
    private Markup markup = null;
    private StringProperty textProp = new SimpleStringProperty ();
    private ObjectProperty<Markup> markupProp = new SimpleObjectProperty<> ();

    public StringWithMarkup (String text,
                             Markup markup)
    {

        this.text = text;
        this.markup = markup;

        this.markupProp.setValue (this.markup);
        this.textProp.setValue (this.text);

    }

    public StringWithMarkup (String text,
                             String markup)
    {

        this.text = text;

        if (markup != null)
        {

            this.markup = new Markup (markup);

        }

        this.markupProp.setValue (this.markup);
        this.textProp.setValue (this.text);

    }

    public StringWithMarkup (String text)
    {

        this.text = text;

        this.textProp.setValue (this.text);

    }

    public StringProperty textProperty ()
    {

        return this.textProp;

    }

    public ObjectProperty<Markup> markupProperty ()
    {

        return this.markupProp;

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
            this.textProp.setValue (this.text);

        }

        if (t.getMarkup () != null)
        {

            this.markup = new Markup (t.getMarkup ().toString ());
            this.markupProp.setValue (this.markup);

        }

    }

    public void update (String text,
                        Markup markup)
    {

        this.text = text;
        this.markup = markup;
        this.textProp.setValue (text);
        this.markupProp.setValue (markup);

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
