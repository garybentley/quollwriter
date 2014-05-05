package com.quollwriter;

import java.io.*;

import java.text.*;

import java.util.*;

public class JSONEncoder
{

    public static final String DATE_FORMAT = "dd MMM yyyy kk:mm";

    private static Map builtInEncoders = null;

    static
    {

        Map m = new HashMap ();
        JSONEncoder.builtInEncoders = m;

        m.put (List.class.getName (),
               "");
        m.put (ArrayList.class.getName (),
               "");
        m.put (Map.class.getName (),
               "");
        m.put (HashMap.class.getName (),
               "");

    }

    /**
     * Singleton, there can be only 1!
     */
    private JSONEncoder()
    {

    }

    public static String encode (String s)
    {

        char[] chars = s.toCharArray ();

        int l = chars.length;

        StringBuilder sb = new StringBuilder (l + 4);
        sb.append ('"');

        char   b = 0;
        char   c = 0;
        String t;

        for (int i = 0; i < l; i += 1)
        {

            b = c;
            c = s.charAt (i);

            switch (c)
            {

            case '\\':
            case '"':
                sb.append ('\\');
                sb.append (c);

                break;

            case '/':

                if (b == '<')
                {
                    sb.append ('\\');
                }

                sb.append (c);

                break;

            case '\b':
                sb.append ("\\b");

                break;

            case '\t':
                sb.append ("\\t");

                break;

            case '\n':
                sb.append ("\\n");

                break;

            case '\f':
                sb.append ("\\f");

                break;

            case '\r':
                sb.append ("\\r");

                break;

            default:

                if ((c < ' ') || ((c >= '\u0080') && (c < '\u00a0')) ||
                    ((c >= '\u2000') && (c < '\u2100')))
                {
                    t = "000" + Integer.toHexString (c);
                    sb.append ("\\u" + t.substring (t.length () - 4));
                } else
                {
                    sb.append (c);
                }
            }
        }

        /*
        for (int i = 0; i < l; i++)
        {

            char c = chars[i];

            if ((c == '"')
                ||
                (c == '\\')
                ||
                (c == '\n')
                ||
                (c == '\r')
                ||
                (c == '\f')
                ||
                (c == '\t')
                ||
                (c == '/')
               )
            {

                if (p != '\\')
                {

                    b.append ('\\');

                }

            }

            b.append (c);

            p = c;

        }
         */
        sb.append ('"');

/*
        // This was BELOW the \\\\ replacement.

        s = s.replace ("\\",
                       "\\\\");

        s = s.replace ("\"",
                       "\\\"");
        s = s.replace (String.valueOf ('\n'),
                       "\\n");

        s = s.replace (String.valueOf ('\r'),
                       "\\r");

        s = s.replace (String.valueOf ('\t'),
                       "\\t");

        s = s.replace ("'",
                       "\\'");

        s = s.replace ("/",
                       "\\/");
*/
        return sb.toString ();

    }

    public static String encode (Object             o)
                          throws GeneralException
    {

        if (o == null)
        {

            return o + "";

        }

        if (o instanceof Date)
        {

            // Format as dd MMM yyyy hh:mm.
            SimpleDateFormat sdf = new SimpleDateFormat (JSONEncoder.DATE_FORMAT);

            return JSONEncoder.encode (sdf.format ((Date) o));

        }

        if (o instanceof Boolean)
        {

            return ((Boolean) o).toString ();

        }

        if (o instanceof Number)
        {

            return String.valueOf (((Number) o).doubleValue ());

        }

        if (o instanceof String)
        {

            return JSONEncoder.encode (o.toString ());

        }

        if (o instanceof Collection)
        {

            return JSONEncoder.encodeCollection ((Collection) o);

        }

        if (o instanceof Map)
        {

            return JSONEncoder.encodeMap ((Map) o);

        }
/*
        DataObjectFormatter f = Environment.getFormatter (o.getClass ());
        
        if ((f != null)
            &&
            (o instanceof DataObject)
           )
        {
            
            try
            {
            
                return JSONEncoder.encodeMap (f.encode ((DataObject) o));
            
            } catch (Exception e) {
                
                throw new GeneralException ("Unable to encode object of type: " +
                                            o.getClass ().getName () +
                                            " using formatter: " +
                                            f.getClass ().getName (),
                                            e);
                
            }
            
        }
  */      
        throw new GeneralException ("Object: " + o.getClass ().getName () + " is not supported.");

    }

    public static Map createErrorObject (String type,
                                         String message)
    {

        List l = new ArrayList ();
        l.add (message);

        return JSONEncoder.createErrorObject (type,
                                              l);

    }

    public static Map createErrorObject (String type,
                                         List   errors)
    {

        Map m = new HashMap ();
        Map errM = new HashMap ();
        m.put ("error",
               errM);

        errM.put ("type",
                  type);
        errM.put ("messages",
                  errors);

        return m;

    }

    public static String encodeErrors (String             type,
                                       List               errors)
                                       throws GeneralException
    {

        Map errM = JSONEncoder.createErrorObject (type,
                                                  errors);

        try
        {

            return JSONEncoder.encode (errM);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to encode errors");

        }

    }

    public static String createMapWrapper (Map m)
    {

        StringBuilder b = new StringBuilder ("{");

        Iterator iter = m.keySet ().iterator ();

        while (iter.hasNext ())
        {

            Object k = iter.next ();

            b.append (JSONEncoder.encode (k.toString ()));

            b.append (':');
            b.append (m.get (k));

            if (iter.hasNext ())
            {

                b.append (",");

            }

        }

        b.append ("}");

        return b.toString ();

    }

    public static String encodeMap (Map                m)
                             throws GeneralException
    {

        StringBuilder b = new StringBuilder ("{");

        Iterator iter = m.keySet ().iterator ();

        while (iter.hasNext ())
        {

            Object k = iter.next ();

            b.append (JSONEncoder.encode (k.toString ()));

            b.append (':');

            String v = null;

            v = JSONEncoder.encode (m.get (k));

            b.append (v);

            if (iter.hasNext ())
            {

                b.append (",");

            }

        }

        b.append ("}");

        return b.toString ();

    }

    public static String encodeCollection (Collection         o)
                                    throws GeneralException
    {

        StringBuilder b = new StringBuilder ("[");

        if (o instanceof List)
        {

            List l = (List) o;

            int s = l.size ();

            for (int i = 0; i < s; i++)
            {

                String v = null;

                v = JSONEncoder.encode (l.get (i));

                b.append (v);

                if (i < (s - 1))
                {

                    b.append (",");

                }

                ;

            }

        } else
        {

            if (o instanceof Collection)
            {

                Collection l = (Collection) o;

                Iterator iter = l.iterator ();

                while (iter.hasNext ())
                {

                    String v = null;

                    v = JSONEncoder.encode (iter.next ());

                    b.append (v);

                    if (iter.hasNext ())
                    {

                        b.append (",");

                    }

                }

            }

        }

        b.append ("]");

        return b.toString ();

    }

    public static String createCollectionWrapper (Collection o)
    {

        StringBuilder b = new StringBuilder ("[");

        if (o instanceof List)
        {

            List l = (List) o;

            for (int i = 0; i < l.size (); i++)
            {

                // Don't encode.
                b.append (l.get (i));

                if (i < (l.size () - 1))
                {

                    b.append (",");

                }

            }

        } else
        {

            if (o instanceof Collection)
            {

                Collection l = (Collection) o;

                Iterator iter = l.iterator ();

                while (iter.hasNext ())
                {

                    b.append (iter.next ());

                    if (iter.hasNext ())
                    {

                        b.append (",");

                    }

                }

            }

        }

        b.append ("]");

        return b.toString ();

    }

}
