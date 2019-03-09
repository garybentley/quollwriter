package com.quollwriter.ui.fx.swing;

public class TextRange
{

    public int start = -1;
    public int end = -1;

    public TextRange (int start,
                      int end)
    {

        this.start = start;
        this.end = end;

    }

    public TextRange (QTextEditor ed)
    {

        if (this.start < 0)
        {

            this.start = ed.getSelectionStart ();

            if (this.start < 0)
            {

                this.start = ed.getCaret ().getDot ();

            }

        }

        if (this.end < 0)
        {

            this.end = ed.getSelectionEnd ();

            if (this.end < 0)
            {

                this.end = this.start;

            }

        }

    }

}
