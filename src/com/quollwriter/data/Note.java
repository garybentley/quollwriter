package com.quollwriter.data;

import java.util.Date;

import org.jdom.*;


public class Note extends ChapterItem
{

    public static final String EDIT_NEEDED_NOTE_TYPE = "Edit Needed";
    public static final String OBJECT_TYPE = "note";

    private Date        due = null;
    private String      type = null;
    private NamedObject object = null;

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

    public String toString ()
    {

        return Note.OBJECT_TYPE + "(summary: " + this.name + ", id: " + this.getKey () + ", due: " + this.due + ", position: " + this.getPosition () + ", end: " + this.getEndPosition () + ", hash: " + this.hashCode () + ")";

    }

    public boolean isEditNeeded ()
    {

        return Note.EDIT_NEEDED_NOTE_TYPE.equals (this.type);

    }

}
