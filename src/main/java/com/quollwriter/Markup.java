package com.quollwriter;

import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import javafx.scene.control.IndexRange;

import org.fxmisc.richtext.model.*;

import com.quollwriter.ui.fx.components.TextEditor;

import com.gentlyweb.utils.*;

public class Markup
{

    public static String DEFAULT_SEPARATOR = "";
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

                this.currInd = 1;

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

        public void shiftBy (int i)
        {

            this.start += i;
            this.end += i;

            if (this.start < 0)
            {

                this.start = 0;

            }

            if (this.end < 0)
            {

                this.end = 0;

            }

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

    public List<MarkupItem> items = new ArrayList<> ();

    public Markup ()
    {

    }

    public Markup (Markup fromMarkup,
                   int    from,
                   int    to)
    {

        if (fromMarkup == null)
        {

            return;

        }

        for (MarkupItem mi : fromMarkup.getItems (from,
                                                  to))
        {

            this.items.add (this.createItem (mi));

        }

    }

    public Markup(String m)
    {

        if (m == null)
        {

            return;

        }

        int lastInd = 0;

        StringTokenizer tt = new StringTokenizer (m,
                                                  ",");

        while (tt.hasMoreTokens ())
        {

            StringTokenizer nt = new StringTokenizer (tt.nextToken ().trim (),
                                                      "=");

            try
            {

                MarkupItem it = new MarkupItem (nt.nextToken (),
                                                nt.nextToken ());

                if (it.start > lastInd)
                {
                    /*
                    // Create the dummy item.
                    MarkupItem dit = new MarkupItem (lastInd,
                                                     it.start,
                                                     false,
                                                     false,
                                                     false);

                    this.items.add (dit);
                    */
                }

                lastInd = it.end;

                this.items.add (it);

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

    public Markup (TextEditor ed)
    {

        StyleSpans<TextEditor.TextStyle> ss = ed.getStyleSpans (0, ed.getText ().length ());

        int from = 0;

        for (StyleSpan<TextEditor.TextStyle> s : ss)
        {

            TextEditor.TextStyle t = s.getStyle ();

            if ((t.isBold ())
                ||
                (t.isUnderline ())
                ||
                (t.isItalic ())
               )
            {

                this.items.add (new MarkupItem (from,
                                                from + s.getLength (),
                                                t.isBold (),
                                                t.isItalic (),
                                                t.isUnderline ()));

            }

            from += s.getLength ();

        }

    }

    public void addItem (int start,
                         int end,
                         boolean isBold,
                         boolean isItalic,
                         boolean isUnderline)
    {

        this.items.add (new MarkupItem (start,
                                        end,
                                        isBold,
                                        isItalic,
                                        isUnderline));

    }

    public void shiftBy (int i)
    {

        for (MarkupItem it : items)
        {

            it.shiftBy (i);

        }

    }

    public String markupAsHTML (String text)
    {

        return this.markupAsHTML (text,
                                  DEFAULT_SEPARATOR);

    }

    public String markupAsHTML (String text,
                                String sep)
    {

        if (text == null)
        {

            return "";

        }

        if (this.items.size () == 0)
        {

            return text;

        }

        StringBuilder ct = new StringBuilder ();

        Markup.MarkupItem last = null;

        for (MarkupItem item : this.items)
        {

            if ((last == null)
                &&
                (item.start > 0)
               )
            {

                // Got some unstyled text at the start.
                ct.append (text.substring (0,
                                           item.start));

            }

            if ((last != null)
                &&
                (item.start > last.end)
               )
            {

                // Got some unstyled text in the middle of two items.
                ct.append (text.substring (last.end,
                                           item.start));

            }

            int end = Math.min (item.end, text.length ());

            String st = text.substring (item.start,
                                        end);

            boolean styled = item.isStyled ();

            if (styled)
            {

                ct.append ("<span class=\"");
                ct.append (item.getStyles (sep));
                ct.append ("\">");

                ct.append (StringUtils.replaceString (st,
                                                      String.valueOf ('\n'),
                                                      "<br />"));

                ct.append ("</span>");

            } else {

                ct.append (st);

            }

            last = item;

        }

        if ((last != null)
            &&
            (last.end < text.length ())
           )
        {

            // Got some unstyled text at the end.
            ct.append (text.substring (last.end));

        }

        return ct.toString ();

    }

    private void applyStyle (DefaultStyledDocument doc,
                             Object                style,
                             int                   start,
                             int                   end)
    {

        MutableAttributeSet attrs = new SimpleAttributeSet ();
        attrs.addAttribute (style,
                            true);

        doc.setCharacterAttributes (start,
                                    end - start,
                                    attrs,
                                    false);

    }

    public void apply (TextEditor ed)
    {

        for (MarkupItem i : this.items)
        {

            // TODO Put all 3 calls together?
            if (i.bold)
            {

                ed.setBold (new IndexRange (i.start,
                                            i.end));

            }

            if (i.italic)
            {

                ed.setItalic (new IndexRange (i.start,
                                              i.end));

            }

            if (i.underline)
            {

                ed.setUnderline (new IndexRange (i.start,
                                                 i.end));

            }

        }

    }

    public void apply (DefaultStyledDocument doc)
    {

        for (MarkupItem i : items)
        {

            try
            {

                if (i.bold)
                {

                    this.applyStyle (doc,
                                     StyleConstants.Bold,
                                     i.start,
                                     i.end);

                }

                if (i.italic)
                {

                    this.applyStyle (doc,
                                     StyleConstants.Italic,
                                     i.start,
                                     i.end);

                }

                if (i.underline)
                {

                    this.applyStyle (doc,
                                     StyleConstants.Underline,
                                     i.start,
                                     i.end);

                }

            } catch (Exception e)
            {

            }

        }

    }

    public void apply (com.quollwriter.ui.components.QTextEditor ed)
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

    public void apply (com.quollwriter.ui.fx.swing.QTextEditor ed)
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
/*
    public Iterator iterator ()
    {

        return new MarkupIterator (this.items);

    }
  */
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

    public List<MarkupItem> getMarkupBetween (int from,
                                              int to)
    {

        return this.getItems (from,
                              to);

    }

    public List<MarkupItem> getItems (int from,
                                      int to)
    {

        List<MarkupItem> its = new ArrayList ();

        for (MarkupItem it : this.items)
        {

            // start between from and to.
            if ((it.start >= from)
                &&
                (it.start <= to)
               )
            {

                its.add (it);

                continue;

            }

            // anything that covers the whole range.
            if ((it.start <= from)
                &&
                (it.end >= from)
               )
            {

                its.add (it);

                continue;

            }

            // end between from and to.
            if ((it.end >= from)
                &&
                (it.end <= to)
               )
            {

                its.add (it);

                continue;

            }

        }

        return its;

    }

    public MarkupItem createItem (MarkupItem it)
    {

        return this.createItem (it.start,
                                it.end,
                                it.bold,
                                it.italic,
                                it.underline);

    }

    public MarkupItem createItem (int     start,
                                  int     end,
                                  boolean bold,
                                  boolean italic,
                                  boolean underline)
    {

        return new MarkupItem (start,
                               end,
                               bold,
                               italic,
                               underline);

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

            if (m.types.equals (""))
            {

                continue;

            }

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
