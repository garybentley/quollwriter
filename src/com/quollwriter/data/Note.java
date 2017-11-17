package com.quollwriter.data;

import java.util.*;

import org.jdom.*;

import com.quollwriter.text.*;

public class Note extends ChapterItem
{

    public static final String DEALT_WITH = "dealtWith";
    public static final String EDIT_NEEDED_NOTE_TYPE = "Edit Needed";
    public static final String OBJECT_TYPE = "note";
    public static final String EDIT_NEEDED_OBJECT_TYPE = "editneedednote";
    public static final String NOTE_TYPE_OBJECT_TYPE = "notetype";

    private Date        due = null;
    private String      type = null;
    private NamedObject object = null;
    private Date        dealtWith = null;

    public Note()
    {

        super (Note.OBJECT_TYPE);

    }

    public Note(int     at,
                Chapter c)
    {

        super (Note.OBJECT_TYPE,
               at,
               c);

    }

    public Note(int     from,
                int     to,
                Chapter c)
    {

        super (Note.OBJECT_TYPE,
               from,
               to,
               c);

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "due",
                                    this.due);
        this.addToStringProperties (props,
                                    "type",
                                    this.type);
        this.addToStringProperties (props,
                                    "object",
                                    this.object);
        this.addToStringProperties (props,
                                    "dealtWith",
                                    this.dealtWith);

    }

    public boolean isDealtWith ()
    {

        return this.dealtWith != null;

    }

    public Date getDealtWith ()
    {

        return this.dealtWith;

    }

    public void setDealtWith (Date d)
    {

        Date oldd = this.dealtWith;

        this.dealtWith = d;

        this.firePropertyChangedEvent (Note.DEALT_WITH,
                                       oldd,
                                       this.dealtWith);

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

        Note n = (Note) old;

        this.addFieldChangeElement (root,
                                    "due",
                                    ((old != null) ? n.getDue () : null),
                                    this.due);

        this.addFieldChangeElement (root,
                                    "type",
                                    ((old != null) ? n.getType () : null),
                                    this.type);

    }

    public void setSummaryFromDescription ()
    {

        String text = this.getDescriptionText ();

        if (text == null)
        {

            return;

        }

        Paragraph p = new Paragraph (text,
                                     0);

        this.setSummary (p.getFirstSentence ().getText ());

    }

    public void setObject (NamedObject o)
    {

        this.object = o;

    }

    public void setChapter (Chapter c)
    {

        this.setObject (c);

        super.setChapter (c);

    }

    public Chapter getChapter ()
    {

        if (this.object instanceof Chapter)
        {

            return (Chapter) this.object;

        }

        return super.getChapter ();

    }

    public NamedObject getObject ()
    {

        if (this.getChapter () != null)
        {

            return this.getChapter ();

        }

        return this.object;

    }

    public String getType ()
    {

        return this.type;

    }

    public void setType (String t)
    {

        this.type = t;

    }

    public Date getDue ()
    {

        return this.due;

    }

    public void setDue (Date d)
    {

        this.due = d;

    }

    public boolean isEditNeeded ()
    {

        return Note.EDIT_NEEDED_NOTE_TYPE.equals (this.type);

    }

}
