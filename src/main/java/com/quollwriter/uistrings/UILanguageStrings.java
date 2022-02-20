package com.quollwriter.uistrings;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

import com.quollwriter.data.*;
import com.quollwriter.*;

public class UILanguageStrings extends AbstractLanguageStrings<UILanguageStrings> implements RefValueProvider, Comparable<UILanguageStrings>
{

    public static final String ENGLISH_ID = ":" + Constants.ENGLISH;
    public static final String OBJECT_TYPE = "languagestrings";

    //public static String ID_PART_SEP = ".";
    //public static String ID_REF_START = "${";
    //public static String ID_REF_END = "}";

    private String languageName = null;
    private Version qwVersion = null;
    //private Set<Section> sections = null;

    private UILanguageStrings ()
    {

        super ();

        this.qwVersion = Environment.getQuollWriterVersion ();

    }

    public UILanguageStrings (BaseStrings strs)
    {

        super (strs);

    }

    public UILanguageStrings (UILanguageStrings derivedFrom)
    {

        super (derivedFrom);

    }

    public UILanguageStrings (File f)
                       throws GeneralException
    {

        this ();

        if ((f == null)
            ||
            (!f.exists ())
            ||
            (!f.isFile ())
           )
        {

            throw new IllegalArgumentException ("No file provided.");

        }

        String v = null;

        try
        {

            Reader in = new BufferedReader (new InputStreamReader (new FileInputStream (f),
                                                                   StandardCharsets.UTF_8));

            long length = f.length ();

            char[] chars = new char[(int) length];

            in.read (chars,
    		         0,
    		         (int) length);

        	in.close ();

            v = new String (chars);

        } catch (Exception e) {

            throw new GeneralException ("Unable to get contents of file: " + f,
                                        e);

        }

        this.init (v);

    }

    public UILanguageStrings (String jsonData)
                       throws GeneralException
    {

        this ();

        this.init (jsonData);

    }

    public UILanguageStringsInfo getInfo ()
    {

        return new UILanguageStringsInfo (this.getId (),
                                          this.getNativeName (),
                                          this.languageName,
                                          this.getPercentComplete (),
                                          this.qwVersion,
                                          this.isUser ());

    }

    public int getPercentComplete ()
    {

        return (this.getDerivedFrom () == null ? 100 : Utils.getPercent ((float) this.getAllTextValues ().size (), (float) this.getDerivedFrom ().getAllTextValues ().size ()));

    }

    public void setLanguageName (String n)
    {

        this.languageName = n;

    }

    public String getLanguageName ()
    {

        return this.languageName;

    }

    public void setQuollWriterVersion (Version v)
    {

        this.qwVersion = v;

    }

    public Version getQuollWriterVersion ()
    {

        return this.qwVersion;

    }

    public static boolean isEnglish (String id)
    {

        return (id.equals (ENGLISH_ID))
                ||
                "UK English".equals (id)
                ||
                "US English".equals (id)
                ||
               ((":" + id).equals (ENGLISH_ID));

    }

    public boolean isEnglish ()
    {

        return this.isEnglish (this.getId ());

    }

    @Override
    public boolean equals (Object o)
    {

        if (!(o instanceof UILanguageStrings))
        {

            return false;

        }

        UILanguageStrings ls = (UILanguageStrings) o;

        return this.compareTo (ls) == 0;

    }

    @Override
    public int compareTo (UILanguageStrings obj)
    {

        int i = super.compareTo (obj);

        if (i != 0)
        {

            return i;

        }

        return this.qwVersion.compareTo (obj.qwVersion);

    }

    @Override
    public String getDisplayName ()
    {

        return this.getName () + " (" + this.getQuollWriterVersion ().toString () + ")";

    }

    private void init (String jsonData)
                throws GeneralException
    {

        Object obj = JSONDecoder.decode (jsonData);

        if (!(obj instanceof Map))
        {

            throw new IllegalArgumentException ("String does parse to a Map, is: " + obj.getClass ().getName ());

        }

        this.init ((Map) obj);

    }

    @Override
    public void init (Map<String, Object> obj)
                throws GeneralException
    {

        super.init (obj);

        this.languageName = this.getString (":language",
                                            obj);

        String qwv = (String) obj.get (":qwversion");

        if (qwv == null)
        {

            throw new GeneralException ("Expected to find a QW version.");

        }

        this.qwVersion = new Version (qwv);

        String did = this.getString (":derivedfrom",
                                     obj);

        if (did != null)
        {

            try
            {

                this.setDerivedFrom (UILanguageStringsManager.getUILanguageStrings (did,
                                                                                    this.qwVersion));

            } catch (Exception e) {

                throw new GeneralException ("Unable to parent strings for: " + did + ", " + this.qwVersion,
                                            e);

            }

        }

/*
        // Ensure we can resolve everything.
        Iterator iter = m.keySet ().iterator ();

        while (iter.hasNext ())
        {

            String k = iter.next ().toString ();

            if (BaseStrings.isSpecialId (k))
            {

                continue;

            }

            List ids = new ArrayList ();

            ids.add (k);

            Object v = m.get (k);

            if (v instanceof Map)
            {

                Map om = (Map) v;

                this.nodes.put (k,
                                new Node (k,
                                          null,
                                          om));

                continue;

            }

            if (v instanceof String)
            {

                String val = v.toString ();

                this.nodes.put (k,
                                new TextValue (k,
                                               null,
                                               val,
                                               m));

            }

        }
*/
    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "language",
                                    this.languageName);

        this.addToStringProperties (props,
                                    "qwversion",
                                    this.qwVersion.getVersion ());
                                    
    }

    /**
     * Returns a data structure that is suitable for JSON encoding.
     *
     * @return The data
     */
    @Override
    public Map getAsJSON ()
    {

        Map m = super.getAsJSON ();
        m.put (":language",
               this.languageName);
        m.put (":qwversion",
               this.qwVersion.getVersion ());
        return m;

    }

/*
    public static Set<String> getRefIds (String text)
    {

        Set<String> ids = new LinkedHashSet<> ();

        int start = 0;

        while ((start = text.indexOf (ID_REF_START,
                                      start)) > -1)
        {

            int end = text.indexOf (ID_REF_END,
                                    start);

            if (end > (start + ID_REF_START.length ()))
            {

                String sid = text.substring (start + ID_REF_START.length (),
                                             end);

                sid = sid.trim ();

                if (sid.length () > 0)
                {

                    ids.add (sid);

                }

                start = end + ID_REF_END.length ();

            } else {

                start += ID_REF_START.length ();

            }

        }

        return ids;

    }
*/
/*
    public static List<String> buildRefValsTree (String           text,
                                                 String           rootId,
                                                 RefValueProvider prov,
                                                 List<String>         ids)
    {

        if (text == null)
        {

            return null;

        }

        Set<Id> refids = LanguageStrings.getIds (text);

        for (Id rid : refids)
        {

            if (rid.isPartial ())
            {

                continue;

            }
            //Value v = strings.getValue (rid);

            if (rid.getId ().equals (rootId))
            {

                return ids;

            }

            if (ids.contains (rid.getId ().trim ()))
            {

                // Already have this, got a loop.
                return ids;

            }

            ids.add (rid.getId ().trim ());

            int ind = ids.size () - 1;

            String rv = prov.getRawText (rid.getId ().trim ());//getString (rid);

            if (rv == null)
            {

                return null;

            }

            List<String> nids = LanguageStrings.buildRefValsTree (rv,
                                                              rootId,
                                                              prov,
                                                              new ArrayList<> (ids));

            if (nids != null)
            {

                return nids;

            } else {

                ids = ids.subList (0, ind);

            }

        }

        return null;

    }
*/

/*
    public static String buildText (String           text,
                                    RefValueProvider prov)
    {

        StringBuilder b = new StringBuilder (text);

        int start = 0;

        while ((start = b.indexOf (ID_REF_START,
                                   start)) > -1)
        {

            int end = b.indexOf (ID_REF_END,
                                 start);

            if (end > (start + ID_REF_START.length ()))
            {

                String sid = b.substring (start + ID_REF_START.length (),
                                          end);

                int bind = sid.indexOf ("|");
                String sub = null;

                if (bind > -1)
                {

                    sub = sid.substring (0, bind);

                    sid = sid.substring (bind + 1);

                }

                String sv = prov.getString (sid);

                if (sub != null)
                {

                    if (sub.equalsIgnoreCase ("u"))
                    {

                        // Uppercase the first letter.

                    }

                    if (sub.equalsIgnoreCase ("l"))
                    {

                        // Lowercase the first letter.
                        //char c[] = idv.toCharArray ();

                        //c[0] = Character.toLowerCase (c[0]);

                        //idv = new String (c);

                    }

                    if (sub.equalsIgnoreCase ("ua"))
                    {

                        // Uppercase the first letter of each word.

                    }

                    if (sub.equalsIgnoreCase ("la"))
                    {

                        // Lowercase the first letter of each word.

                    }

                }

                if (sv != null)
                {

                    b.replace (start,
                               end + ID_REF_END.length (),
                               sv);

                    start += sv.length ();

                } else {

                    start = end + 1;

                }

            } else {

                start += ID_REF_START.length ();

            }

        }

        String s = b.toString ();

        s = LanguageStrings.replaceSpecialValues (s);

        return s;

    }
*/

}
