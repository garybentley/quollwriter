package com.quollwriter.uistrings;

import java.util.*;
import com.quollwriter.text.*;

public class TextValue extends Value<TextValue>
{

    private int scount = 0;
    private String text = null;
    private String builtText = null;

    public TextValue (String id,
                      Node   parent,
                      String text,
                      Map    parentData)
    {

        super (id,
               parent);

        this.text = text;

        Object co = parentData.get (":comment." + id);

        String comm = null;

        if (co != null)
        {

           this.comment = co.toString ();

        }

        Object so = parentData.get (":scount." + id);

        if ((so != null)
            &&
            (so instanceof Number)
           )
        {

           this.scount = ((Number) so).intValue ();

        }

    }

    public TextValue (String id,
                      Node   parent,
                      String text,
                      String comment,
                      int    scount)
    {

        super (id,
               parent,
               null);

        this.text = text;
        this.comment = comment;
        this.scount = scount;

    }

    @Override
    public boolean match (String find)
    {

        if (super.match (find))
        {

            return true;

        }

        if (this.text != null)
        {

            List<Word> ftext = new Paragraph (find,
                                              0).getWords ();

            List<Word> words = new Paragraph (this.text,
                                              0).getWords ();

            if (TextUtilities.find (words,
                                    ftext,
                                    true).size () > 0)
            {

                return true;

            }

        }

        return false;

    }

    @Override
    public Map getAsJSON ()
    {

        Map m = super.getAsJSON ();

        String id = this.getNodeId ();

        m.put (id,
               this.getRawText ());

        if (this.comment != null)
        {

            m.put (":comment." + id,
                   this.comment);

        }

        if (this.scount > 0)
        {

            m.put (":scount." + id,
                   this.scount);

        }

        return m;

    }

    @Override
    public Node cloneNode ()
    {

        TextValue n = new TextValue (this.id,
                                     null,
                                     null,  /* no value */
                                     this.comment,
                                     this.scount);

        //n.title = this.title;
        //n.section = this.section;

        return n;

    }

    @Override
    public boolean isDifferent (TextValue v)
    {

        TextValue ov = (TextValue) v;

        if (!this.getRawText ().equals (ov.getRawText ()))
        {

            return true;

        }

        if (this.scount != ov.getSCount ())
        {

            return true;

        }

        return super.isDifferent (v);

    }

    public void setSCount (int s)
    {

        this.scount = s;

    }

    public int getSCount ()
    {

        return this.scount;

    }

    @Override
    public String toString ()
    {

        return (this.getId () + "(value,scount=" + this.scount + ",text=" + this.text + ",comment=" + this.comment + ")");

    }

    @Override
    public Set<Value> getAllValues ()
    {

        return new LinkedHashSet<> ();

    }

    @Override
    public Node getChild (List<String> idparts)
    {

        return null;

    }

    @Override
    public Set<String> getErrors (RefValueProvider prov)
    {

        return BaseStrings.getErrors (this.text,
                                      BaseStrings.toId (this.getId ()),
                                      prov.getSCount (BaseStrings.toId (this.getId ())),
                                      //this.scount,
                                      prov);

    }

    public void clearBuiltText ()
    {

        this.builtText = null;

    }

    public void setRawText (String t)
    {

        this.text = t;
        this.clearBuiltText ();

    }

    public String getRawText ()
    {

        return this.text;

    }

    public String getBuiltText (RefValueProvider prov)
    {
/*
        if (this.builtText != null)
        {

            return this.builtText;

        }
*/
        if (this.getErrors (prov).size () > 0)
        {

            return this.text;

        }

        String s = BaseStrings.buildText (this.text,
                                          prov);

        this.builtText = s;

        return this.builtText;

    }

}
