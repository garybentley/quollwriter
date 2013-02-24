package com.quollwriter.synonyms.providers;

import java.io.*;

import java.net.*;

import java.util.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;

import com.quollwriter.synonyms.*;


public class BigHugeLabsSynonymProvider implements SynonymProvider
{

    public static final String URL_PROPERTY_NAME = "bighugelabsURL";

    public static final String WORD_TAG = "[[WORD]]";

    public BigHugeLabsSynonymProvider()
    {

    }

    public void setUseCache (boolean v)
    {

        // It needs one!

    }

    public String getWordTypes (String word)
                         throws GeneralException
    {

        throw new UnsupportedOperationException ("Not supported by this synonym provider.");

    }

    public boolean hasSynonym (String word)
                               throws GeneralException
    {
    
        return this.getSynonyms (word) != null;
        
    }
    
    public Synonyms getSynonyms (String word)
                          throws GeneralException
    {

        if ((word == null) ||
            (word.trim ().equals ("")))
        {

            return null;

        }

        // Encode the word.
        try
        {

            word = URLEncoder.encode (word,
                                      "utf-8");

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to url encode word: " +
                                        word,
                                        e);

        }

        String url = Environment.getProperty (BigHugeLabsSynonymProvider.URL_PROPERTY_NAME);

        url = StringUtils.replaceString (url,
                                         BigHugeLabsSynonymProvider.WORD_TAG,
                                         word);

        URL u = null;

        try
        {

            u = new URL (url);

        } catch (Exception e)
        {

            throw new GeneralException ("URL: " +
                                        url +
                                        " from property: " +
                                        BigHugeLabsSynonymProvider.URL_PROPERTY_NAME +
                                        " with word: " +
                                        word +
                                        " is not valid.",
                                        e);

        }

        try
        {

            return this.doRequest (u);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get synonyms using url: " +
                                        url,
                                        e);

        }

    }

    private Synonyms doRequest (URL url)
                         throws Exception
    {

        Synonyms ret = new Synonyms ();

        HttpURLConnection conn = (HttpURLConnection) url.openConnection ();

        // Handle the 303 case automatically.
        conn.setInstanceFollowRedirects (true);

        conn.setDoInput (true);
        conn.setDoOutput (true);

        // Connect.
        conn.connect ();

        // Check the return code.
        if (conn.getResponseCode () == HttpURLConnection.HTTP_NOT_FOUND)
        {

            return ret;

        }

        if (conn.getResponseCode () == HttpURLConnection.HTTP_INTERNAL_ERROR)
        {

            throw new GeneralException ("Got 500 error whilst trying to use url: " +
                                        url +
                                        ", server said: " +
                                        conn.getResponseMessage ());

        }

        if (conn.getResponseCode () != HttpURLConnection.HTTP_OK)
        {

            throw new GeneralException ("Got unexpected response code: " +
                                        conn.getResponseCode () +
                                        " whilst trying to use url: " +
                                        url +
                                        ", response message: " +
                                        conn.getResponseMessage ());

        }

        // Get the input stream.
        BufferedReader r = new BufferedReader (new InputStreamReader (conn.getInputStream (),
                                                                      "utf-8"));

        // Read everything in.
        StringBuilder b = new StringBuilder ();

        String line = r.readLine ();

        while (line != null)
        {

            if (b.length () > 0)
            {

                b.append ("\n");

            }

            b.append (line);

            line = r.readLine ();

        }

        r.close ();
        conn.disconnect ();

        if (url.toExternalForm ().endsWith ("/"))
        {

            // There will be a result per line.
            StringTokenizer t = new StringTokenizer (b.toString (),
                                                     String.valueOf ('\n'));

            while (t.hasMoreTokens ())
            {

                ret.verbs.add (t.nextToken ().trim ());

            }

            return ret;

        }

        if (url.toExternalForm ().endsWith ("/json"))
        {

            // JSON format.
            Map data = (Map) JSONDecoder.decode (b.toString ());

            Map verbs = (Map) data.get ("verb");

            if (verbs != null)
            {

                // Should have a "syn" entry and potentially a "sim".
                ret.addVerbs ((List<String>) verbs.get ("syn"));
                ret.addVerbs ((List) verbs.get ("sim"));

            }

            Map adjs = (Map) data.get ("adjectives");

            if (adjs != null)
            {

                // Should have a "syn" entry and potentially a "sim".
                ret.addAdjectives ((List<String>) adjs.get ("syn"));
                ret.addAdjectives ((List) adjs.get ("sim"));

            }

            Map advs = (Map) data.get ("adverb");

            if (advs != null)
            {

                // Should have a "syn" entry and potentially a "sim".
                ret.addAdverbs ((List<String>) advs.get ("syn"));
                ret.addAdverbs ((List) advs.get ("sim"));

            }

            // Can contain "noun" and "verb" entries.
            Map nouns = (Map) data.get ("noun");

            if (nouns != null)
            {

                // Should have a "syn" entry and potentially a "sim".
                ret.addNouns ((List<String>) nouns.get ("syn"));

                ret.addNouns ((List) nouns.get ("sim"));

            }

        }

        return ret;

    }

}
