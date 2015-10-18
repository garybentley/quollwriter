package com.quollwriter.data;

import java.text.*;

import com.quollwriter.*;
import com.quollwriter.text.*;

public class OutlineItem extends ChapterItem
{

    public static final String OBJECT_TYPE = "outlineitem";

    // private Scene scene = null;

    public OutlineItem()
    {

        super (OutlineItem.OBJECT_TYPE);

    }

    public OutlineItem(int     at,
                       Chapter c)
    {

        super (OutlineItem.OBJECT_TYPE,
               at,
               c);

    }

    public OutlineItem(int   at,
                       Scene s)
    {

        super (OutlineItem.OBJECT_TYPE,
               at,
               s.getChapter ());

        this.scene = s;

    }

    public void getChanges (NamedObject      old,
                            org.jdom.Element root)
    {

    }

/*
    public Scene getScene ()
    {

        return this.scene;

    }
*/

    public void setScene (Scene s)
    {

        if (s == null)
        {
            
            if (this.scene != null)
            {
                
                this.scene.removeOutlineItem (this);
                
            }
            
        }
    
        super.setScene (s);
        
        this.setParent (s);

    }

    @Override
    public void setDescription (StringWithMarkup d)
    {

        // Legacy data check, can't control the order of calls that hibernate makes so ensure that it
        // doesn't overwrite the description.
        if ((this.getName () != null) &&
            (d == null))
        {

            return;

        }

        if (d != null)
        {

            String t = d.getText ();
        
            Paragraph p = new Paragraph (t,
                                         0);
        
            if (p.getSentenceCount () > 0)
            {

                this.setName (p.getFirstSentence ().getText ());

            }
        
        }

        super.setDescription (d);

    }

}
