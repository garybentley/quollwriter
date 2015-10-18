package com.quollwriter.editors.messages;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;

public class TypeEncoder
{
    
    public static Map encode (Note n)
    {
        
        if (n.getChapter () == null)
        {
            
            throw new IllegalArgumentException ("No chapter for note: " +
                                                n);
            
        }
        
        String t = (n.getDescription () != null ? n.getDescription ().getText () : null);
        
        if ((t == null)
            ||
            (t.trim ().length () == 0)
           )
        {
            
            throw new IllegalArgumentException ("No text for note: " +
                                                n);
            
        }

        Map cdata = new HashMap ();
        cdata.put (MessageFieldNames.commentid,
                   n.getId ());
        cdata.put (MessageFieldNames.chapterid,
                   n.getChapter ().getId ());
        cdata.put (MessageFieldNames.chapterversion,
                   n.getChapter ().getVersion ());
        cdata.put (MessageFieldNames.chaptername,
                   n.getChapter ().getName ());
        cdata.put (MessageFieldNames.text,
                   n.getDescriptionText ());
        cdata.put (MessageFieldNames.start,
                   n.getStartPosition ());

        if (n.getDealtWith () != null)
        {
            
            cdata.put (MessageFieldNames.date,
                       n.getDealtWith ().getTime ());

        }
                   
        if ((n.getEndPosition () > 0)
            &&
            (n.getEndPosition () > n.getStartPosition ())
           )
        {
            
            cdata.put (MessageFieldNames.end,
                       n.getEndPosition ());
                   
        }
        
        return cdata;        
        
    }
    
    // Not the right place for this, create a better structure later.   
    public static Map encode (Chapter c)
    {
        
        Map cdata = new HashMap ();
        cdata.put (MessageFieldNames.chapterid,
                   c.getId ());
        cdata.put (MessageFieldNames.name,
                   c.getName ());
        
        StringWithMarkup ct = c.getText ();
        
        String t = null;
        String m = null;
        
        if (ct != null)
        {
            
            t = ct.getText ();
            m = ct.getMarkup ().toString ();
            
        }
        
        cdata.put (MessageFieldNames.text,
                   t);

        cdata.put (MessageFieldNames.version,
                   c.getVersion ());
        
        if (m != null)
        {
        
            cdata.put (MessageFieldNames.markup,
                       m);

        }
        
        return cdata;
        
    }
    
    public static Note decodeToNote (Map     m)
                              throws GeneralException
    {
        
        String chid = TypeEncoder.getString (MessageFieldNames.chapterid,
                                             m);
        String chver = TypeEncoder.getString (MessageFieldNames.chapterversion,
                                              m);
        
        String cname = TypeEncoder.getString (MessageFieldNames.chaptername,
                                              m,
                                              false);

        // Create a fake chapter to hold the id/version.
        Chapter c = new Chapter ();
        c.setId (chid);
        c.setVersion (chver);
        c.setName (cname);
                
        String cid = TypeEncoder.getString (MessageFieldNames.commentid,
                                            m);

        String text = TypeEncoder.getString (MessageFieldNames.text,
                                             m);
                                            
        Note n = new Note ();
        
        n.setChapter (c);
        n.setDescription (new StringWithMarkup (text));
        
        // Need to also setup the summary.
        n.setSummaryFromDescription ();
        
        n.setId (cid);
        
        n.setDealtWith (TypeEncoder.getDate (MessageFieldNames.date,
                                             m,
                                             false));
        n.setPosition (TypeEncoder.getInt (MessageFieldNames.start,
                                           m));

        int end = TypeEncoder.getInt (MessageFieldNames.end,
                                      m,
                                      false);
        
        if (end > -1)
        {
            
            n.setEndPosition (end);
            
        }
                                                    
        return n;
                                            
    }
    
    public static Chapter decodeToChapter (Map m)
                                    throws GeneralException
    {
        
        String cid = TypeEncoder.getString (MessageFieldNames.chapterid,
                                            m);
        String cname = TypeEncoder.getString (MessageFieldNames.name,
                                              m);
        String ctext = TypeEncoder.getString (MessageFieldNames.text,
                                              m,
                                              false);
        String cver = TypeEncoder.getString (MessageFieldNames.version,
                                             m);
        String cm = TypeEncoder.getString (MessageFieldNames.markup,
                                           m,
                                           false);
        
        Chapter c = new Chapter ();
        c.setId (cid);
        c.setName (cname);
        
        c.setText (new StringWithMarkup (ctext, cm));
        
        c.setVersion (cver);
        
        return c;
        
    }    
    
    public static String getString (String  field,
                                    Map     data)
                             throws GeneralException
    {

        return TypeEncoder.getString (field,
                                      data,
                                      true);
    
    }
    
    public static String getString (String  field,
                                    Map     data,
                                    boolean required)
                             throws GeneralException
    {
        
        Object o = null;
        
        if (required)
        {

            o = TypeEncoder.checkTypeAndNotNull (field,
                                                 data,
                                                 String.class);
            
        } else {
                        
            o = data.get (field);

            if (o == null)
            {
                
                return null;
                
            }
            
            if (!String.class.isAssignableFrom (o.getClass ()))
            {
                
                throw new GeneralException ("Expected type for: " + field + " to be: " + String.class.getName () + ", is: " +
                                            o.getClass ().getName ());
                
            }
                        
        } 
        
        return (String) o;
        
    }
    
    public static int getInt (String  field,
                              Map     data)
                       throws GeneralException
    {

        return TypeEncoder.getInt (field,
                                   data,
                                   true);
    
    }
    
    public static int getInt (String  field,
                              Map     data,
                              boolean required)
                       throws GeneralException
    {
        
        Object o = null;
        
        if (required)
        {

            o = TypeEncoder.checkTypeAndNotNull (field,
                                                 data,
                                                 Number.class);
            
        } else {
            
            o = data.get (field);
            
            if (o == null)
            {
                
                return -1;
                
            }
            
            if (!Number.class.isAssignableFrom (o.getClass ()))
            {
                
                throw new GeneralException ("Expected type for: " + field + " to be: " + Number.class.getName () + ", is: " +
                                            o.getClass ().getName ());
                
            }
                        
        } 
        
        return ((Number) o).intValue ();
        
    }

    public static Date getDate (String  field,
                                Map     data)
                         throws GeneralException
    {

        return TypeEncoder.getDate (field,
                                    data,
                                    true);
    
    }
    
    public static Date getDate (String  field,
                                Map     data,
                                boolean required)
                         throws GeneralException
    {
        
        Object o = null;
        
        if (required)
        {

            o = TypeEncoder.checkTypeAndNotNull (field,
                                                 data,
                                                 Number.class);
            
        } else {
            
            o = data.get (field);
            
            if (o == null)
            {
                
                return null;
                
            }
            
            if (!Number.class.isAssignableFrom (o.getClass ()))
            {
                
                throw new GeneralException ("Expected type for: " + field + " to be: " + Number.class.getName () + ", is: " +
                                            o.getClass ().getName ());
                
            }
                        
        } 
        
        return new Date (((Number) o).longValue ());
        
    }

    public static boolean getBoolean (String  field,
                                      Map     data)
                               throws GeneralException
    {

        return TypeEncoder.getBoolean (field,
                                       data,
                                       true);
    
    }
    
    public static boolean getBoolean (String  field,
                                      Map     data,
                                      boolean required)
                               throws GeneralException
    {
        
        Object o = null;
        
        if (required)
        {

            o = TypeEncoder.checkTypeAndNotNull (field,
                                                 data,
                                                 Boolean.class);
            
        } else {
            
            o = data.get (field);
            
            if (o == null)
            {
                
                return false;
                
            }
            
            if (!Boolean.class.isAssignableFrom (o.getClass ()))
            {
                
                throw new GeneralException ("Expected type for: " + field + " to be: " + Number.class.getName () + ", is: " +
                                            o.getClass ().getName ());
                
            }
                        
        } 
        
        return (Boolean) o;
        
    }

    public static Object checkTypeAndNotNull (String field,
                                              Map    data,
                                              Class  expect)
                                       throws GeneralException
    {
        
        Object o = data.get (field);
        
        if (o == null)
        {
            
            throw new GeneralException ("Expected a value for: " + field);
            
        }
        
        if (expect != null)
        {
        
            if (!expect.isAssignableFrom (o.getClass ()))
            {
                
                throw new GeneralException ("Expected type for: " + field + " to be: " + expect.getClass ().getName () + ", is: " +
                                            o.getClass ().getName ());
                
            }
        
        }
        
        return o;
        
    }
    
}