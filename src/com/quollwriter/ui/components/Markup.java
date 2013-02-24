package com.quollwriter.ui.components;

import java.util.*;

import javax.swing.*;
import javax.swing.text.*;


public class Markup
{

    public static final String B = "b";
    public static final String U = "u";
    public static final String I = "i";

    public class MarkupIterator implements Iterator<MarkupItem>
    {

        private List<MarkupItem> items = null;
        private MarkupItem       current = null;
        private int              currInd = -1;

        public MarkupIterator(List<MarkupItem> items)
        {

            this.items = items;

        }

        public void remove ()
        {

            throw new UnsupportedOperationException ("Not supported.");

        }

        public boolean hasNext ()
        {

            return (this.currInd < this.items.size ());

        }

        public MarkupItem next ()
        {

            if ((this.items == null) ||
                (this.items.size () == 0))
            {

                return null;

            }

            if (this.currInd == this.items.size ())
            {

                // Can't progress any further.
                return null;

            }

            if (this.current == null)
            {

                // See is this is a real item or a pseudo item.
                MarkupItem it = this.items.get (0);

                if (it.start > 0)
                {

                    this.current = new MarkupItem (0,
                                                   it.start - 1,
                                                   false,
                                                   false,
                                                   false);

                } else
                {

                    this.current = it;

                }

                this.currInd = 0;

                return this.current;

            }

            int ind = ((this.currInd == -1) ? 0 : this.currInd);

            MarkupItem it = this.items.get (ind);

            if (it.start > (this.current.end + 1))
            {

                // Create a psuedo item.
                this.current = new MarkupItem (this.current.end,
                                               it.start,
                                               false,
                                               false,
                                               false);

            } else
            {

                this.current = it;

                this.currInd++;

            }

            return this.current;

            // P: 0-29
            // 30-50
            // P: 51-74
            // 75-80
            // P: 81-109
            // 110-200

        }

    }

    public class MarkupItem
    {

        public int     start = -1;
        public int     end = -1;
        public String  types = null;
        public boolean bold = false;
        public boolean italic = false;
        public boolean underline = false;

        public MarkupItem(String types,
                          String startEnd)
                   throws Exception
        {

            int ind = startEnd.indexOf ("-");

            this.start = Integer.parseInt (startEnd.substring (0,
                                                               ind));

            this.end = Integer.parseInt (startEnd.substring (ind + 1));

            if (types.indexOf (Markup.B) != -1)
            {

                this.bold = true;

            }

            if (types.indexOf (Markup.I) != -1)
            {

                this.italic = true;

            }

            if (types.indexOf (Markup.U) != -1)
            {

                this.underline = true;

            }

            this.types = types;

        }

        public MarkupItem(int     start,
                          int     end,
                          boolean bold,
                          boolean italic,
                          boolean underline)
        {

            this.start = start;
            this.end = end;
            this.bold = bold;
            this.italic = italic;
            this.underline = underline;

            this.types = (bold ? Markup.B : "") + (italic ? Markup.I : "") + (underline ? Markup.U : "");

        }

        public String toString ()
        {

            return (this.start + "-" + this.end + "[" + this.getStyles ("") + "]");

        }

        public String getStyles (String sep)
        {

            return (this.bold ? Markup.B : "") + sep + (this.italic ? Markup.I : "") + sep + (this.underline ? Markup.U : "");

        }

        public boolean isStyled ()
        {

            return this.bold || this.italic || this.underline;

        }

    }

    public List<MarkupItem> items = new ArrayList ();

    public Markup(String m)
    {

        if (m == null)
        {

            return;

        }

        StringTokenizer tt = new StringTokenizer (m,
                                                  ",");

        while (tt.hasMoreTokens ())
        {

            StringTokenizer nt = new StringTokenizer (tt.nextToken ().trim (),
                                                      "=");

            try
            {

                this.items.add (new MarkupItem (nt.nextToken (),
                                                nt.nextToken ()));

            } catch (Exception e)
            {

                // Ignore the item.

            }

        }

    }

    public Markup(Document doc)
    {

        Element[] roots = doc.getRootElements ();

        for (int i = 0; i < roots.length; i++)
        {

            this.traverseElement (roots[i]);

        }

    }

    public void apply (QTextEditor ed)
    {

        for (MarkupItem i : items)
        {

            try
            {

                if (i.bold)
                {

                    ed.applyStyle (StyleConstants.Bold,
                                   i.start,
                                   i.end);

                }

                if (i.italic)
                {

                    ed.applyStyle (StyleConstants.Italic,
                                   i.start,
                                   i.end);

                }

                if (i.underline)
                {

                    ed.applyStyle (StyleConstants.Underline,
                                   i.start,
                                   i.end);

                }

            } catch (Exception e)
            {

            }

        }

    }

    public Iterator iterator ()
    {

        return new MarkupIterator (this.items);

    }

    private void traverseElement (Element el)
    {

        AttributeSet as = el.getAttributes ();

        

        boolean b = StyleConstants.isBold (as);//as.getAttribute (StyleConstants.Bold) != null;
        boolean i = StyleConstants.isItalic (as); //as.getAttribute (StyleConstants.Italic) != null;
        boolean u = StyleConstants.isUnderline (as); //as.getAttribute (StyleConstants.Underline) != null;

        if ((b) ||
            (i) ||
            (u))
        {

            this.items.add (new MarkupItem (el.getStartOffset (),
                                            el.getEndOffset (),
                                            b,
                                            i,
                                            u));

        }

        int c = el.getElementCount ();

        for (int ind = 0; ind < c; ind++)
        {

            this.traverseElement (el.getElement (ind));

        }

    }

    public List<MarkupItem> getItems (int from,
                                      int to)
    {

        List<MarkupItem> its = new ArrayList ();

        for (MarkupItem it : this.items)
        {

            if (it.start >= from)
            {

                its.add (it);

            }

            if (it.end <= to)
            {

                its.add (it);

            }

        }

        return its;

    }

    private String formatItems ()
    {

        if ((this.items == null) ||
            (this.items.size () == 0))
        {

            return null;

        }

        StringBuilder b = new StringBuilder ();

        for (MarkupItem m : this.items)
        {

            if (b.length () > 0)
            {

                b.append (",");

            }

            b.append (m.types + "=");

            b.append (m.start + "-" + m.end);

        }

        return b.toString ();

    }

    public String toString ()
    {

        return this.formatItems ();

    }

}
