package com.quollwriter.uistrings;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

import com.quollwriter.*;

public class WebsiteLanguageStrings extends AbstractLanguageStrings<WebsiteLanguageStrings> implements RefValueProvider, Comparable<WebsiteLanguageStrings>
{

    public static final String ENGLISH_ID = "en";

    private String langCode = null;
    private int baseVersion = 0;

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

    public void setBaseVersion (int v)
    {

        this.baseVersion = v;

    }

    public int getPercentComplete ()
    {

        return (this.getDerivedFrom () == null ? 100 : Utils.getPercent ((float) this.getAllTextValues ().size (), (float) this.getDerivedFrom ().getAllTextValues ().size ()));

    }

    public int getBaseVersion ()
    {

        return this.baseVersion;

    }

    @Override
    public int compareTo (WebsiteLanguageStrings obj)
    {

        int i = super.compareTo (obj);

        return i;

    }

    public void setLanguageCode (String c)
    {

        this.langCode = c;

    }

    public String getLanguageCode ()
    {

        return this.langCode;

    }

    @Override
    public String getDisplayName ()
    {

        return this.getName ();

    }

    public static boolean isEnglish (String id)
    {

        return ENGLISH_ID.equals (id);

    }

    @Override
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

                this.setDerivedFrom (WebsiteLanguageStringsManager.getWebsiteLanguageStrings (did));

                this.baseVersion = this.getDerivedFrom ().getStringsVersion ();

            } catch (Exception e) {

                throw new GeneralException ("Unable to get website strings: " + did + ", version: " + this.getStringsVersion (),
                                            e);

            }

        }

        String lc = this.getString (":langcode",
                                    obj);

        this.langCode = lc;

        Number v = this.getNumber (":baseversion",
                                   obj);

        if (v != null)
        {

            this.baseVersion = v.intValue ();

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

        if (this.langCode != null)
        {

            m.put (":langcode",
                   this.langCode);

        }

        m.put (":baseversion",
               this.baseVersion);

        return m;

    }

}
