package com.quollwriter.uistrings;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

import org.jdom.*;

import com.gentlyweb.utils.*;
import com.quollwriter.*;

public class WebsiteLanguageStrings extends AbstractLanguageStrings<WebsiteLanguageStrings> implements RefValueProvider, Comparable<WebsiteLanguageStrings>
{

    public static final String ENGLISH_ID = "en";

    public WebsiteLanguageStrings (WebsiteLanguageStrings derivedFrom)
    {

        super (derivedFrom);

    }

    public WebsiteLanguageStrings (File f)
                            throws GeneralException
    {

        if ((f == null)
            ||
            (!f.exists ())
            ||
            (!f.isFile ())
           )
        {

            throw new IllegalArgumentException ("No file provided: " + f);

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

    public WebsiteLanguageStrings (String jsonData)
                            throws GeneralException
    {

        this.init (jsonData);

    }

    @Override
    public int compareTo (WebsiteLanguageStrings obj)
    {

        int i = super.compareTo (obj);

        return i;

    }

    public boolean isEnglish ()
    {

        return this.getId ().equals (ENGLISH_ID);

    }

    private void init (String jsonData)
                throws GeneralException
    {

        Object obj = JSONDecoder.decode (jsonData);

        if (!(obj instanceof Map))
        {

            throw new IllegalArgumentException ("String does parse to a Map");

        }

        this.init ((Map<String, Object>) obj);

    }

    @Override
    public void init (Map<String, Object> obj)
                throws GeneralException
    {

        super.init (obj);

        String did = this.getString (":derivedfrom",
                                     obj);

        if (did != null)
        {

            try
            {

                this.setParent (Environment.getWebsiteLanguageStrings (this.getStringsVersion (),
                                                                       did));

            } catch (Exception e) {

                throw new GeneralException ("Unable to get website strings: " + did + ", version: " + this.getStringsVersion (),
                                            e);

            }

        }

    }

    /**
     * Returns a data structure that is suitable for JSON encoding.
     *
     * @return The data
     */
    public Map getAsJSON ()
    {

        Map m = super.getAsJSON ();

        return m;

    }

/*
keep?
    public Map<TextValue, Set<String>> getErrors ()
    {

        Map<TextValue, Set<String>> ret = new LinkedHashMap<> ();

        for (TextValue v : this.strings.getAllTextValues ())
        {

            Set<String> errors = v.getErrors (this);

            if ((errors != null)
                &&
                (errors.size () > 0)
               )
            {

                ret.put (v,
                         errors);

            }

            // Special checks for the base strings.
            if (this.isEnglish ())
            {

                int c = 0;
                boolean invalid = false;

                String rawText = v.getRawText ();

                // This is a cheap check to determine whether there are strings but the scount is wrong.
                for (int i = 0; i < 5; i++)
                {

                    if (rawText.indexOf ("%" + i + "$s") != -1)
                    {

                        c++;

                    }

                    if (rawText.indexOf ("%" + i + "s") != -1)
                    {

                        invalid = true;

                    }

                    if (rawText.indexOf ("$" + i + "$") != -1)
                    {

                        invalid = true;

                    }

                }

                if (rawText.indexOf ("%$") != -1)
                {

                    invalid = true;

                }

                if (invalid)
                {

                    errors.add (String.format ("Invalid %$ or %Xs value found."));

                }

                if (v.getSCount () != c)
                {

                    errors.add (String.format (":scount value is incorrect or not present, expected: %s, scount is %s.",
                                               c,
                                               v.getSCount ()));

                }

                if ((c > 0)
                    &&
                    (v.getComment () == null)
                   )
                {

                    errors.add (String.format ("Value contains one or more %s values but not have an associated comment.",
                                               "%x$s"));

                }

                if ((v.getSCount () > 0)
                    &&
                    (v.getComment () == null)
                   )
                {

                    errors.add ("S count present but no comment provided.");

                }

            }

        }

        return ret;

    }
*/
/*
keep?
    public Map<TextValue, Set<String>> getErrors (String id)
    {

        Map<TextValue, Set<String>> ret = new LinkedHashMap<> ();

        for (TextValue v : this.strings.getAllTextValues (id))
        {

            Set<String> errors = v.getErrors (this);

            if ((errors != null)
                &&
                (errors.size () > 0)
               )
            {

                ret.put (v,
                         errors);

            }

        }

        return ret;

    }
*/

}
