package com.quollwriter.ui.components;

import java.util.regex.*;

import javax.swing.*;
import javax.swing.text.*;


public class FormattedTextField extends JTextField
{

    public class FormattedDocument extends PlainDocument
    {

        private Pattern limitToChars = null;

        public FormattedDocument(String limitToChars)
        {

            this.limitToChars = Pattern.compile (limitToChars);

        }

        public void insertString (int          offs,
                                  String       str,
                                  AttributeSet a)
                           throws BadLocationException
        {

            if (str == null)
            {

                return;

            }

            StringBuilder b = new StringBuilder ();

            char[] ch = str.toCharArray ();

            for (int i = 0; i < ch.length; i++)
            {

                if (this.limitToChars.matcher (String.valueOf (ch[i])).matches ())
                {

                    b.append (ch[i]);

                }

            }

            super.insertString (offs,
                                b.toString (),
                                a);

        }

    }

    private String chars = null;

    public FormattedTextField(String limitToChars)
    {

        this.chars = limitToChars;

        this.setDocument (new FormattedDocument (this.chars));

    }

}
