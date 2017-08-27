package com.quollwriter.synonyms.providers;

import java.io.*;

import java.net.*;

import java.util.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;

import com.quollwriter.synonyms.*;

public class BuiltInSynonymProvider implements SynonymProvider
{

    public static final String THESAURUS_FILE_NAME_PROPERTY_NAME = "thesaurusFileName";

    public static final String THESAURUS_FILE_INDEX_NAME_PROPERTY_NAME = "thesaurusIndexFileName";

    private List<SynonymIndex>  synInd = new ArrayList<SynonymIndex> ();
    private RandomAccessFile    thesaurusFile = null;
    private File                tFile = null;
    private Map<String, String> synCache = new HashMap ();
    private boolean             useCache = false;

    public BuiltInSynonymProvider()
    {

    }

    private File getIndexFile (String lang)
    {
        
        String thesaurusDir = Environment.getUserQuollWriterDir ().getPath () + "/thesaurus/" + lang;

        return new File (thesaurusDir + "/index.txt");
        
    }
    
    private File getWordsFile (String lang)
    {
        
        String thesaurusDir = Environment.getUserQuollWriterDir ().getPath () + "/thesaurus/" + lang;
            
        return new File (thesaurusDir + "/words.txt");
        
    }
    
    public boolean isLanguageSupported (String lang)
    {
        
        if (this.hasThesaurusJarResourceFile ())
        {
            
            return true;
            
        }
            
        // Look in the thesaurus directory.
        if (Environment.isEnglish (lang))
        {
            
            lang = Constants.ENGLISH;
            //"English";
            
        }
        
        File wordsFile = this.getWordsFile (lang);
                
        if (!wordsFile.exists ())
        {
            
            return false;
                
        }
            
        File indexFile = this.getIndexFile (lang);
        
        if (!indexFile.exists ())
        {
            
            return false;
        
        }
        
        return true;
        
    }
    
    private boolean hasThesaurusJarResourceFile ()
    {
        
        String thesaurusIndexFileName = Environment.getProperty (BuiltInSynonymProvider.THESAURUS_FILE_INDEX_NAME_PROPERTY_NAME);

        if (thesaurusIndexFileName == null)
        {

            return false;

        }

        String thesaurusIndexFileResourceName = Constants.DICTIONARIES_DIR + thesaurusIndexFileName;

        // Read the index file.
        return Environment.getResourceStream (thesaurusIndexFileResourceName) != null;        
        
    }

    
    public void init (String language)
                      throws GeneralException
    {
        
        BufferedReader indexReader = null;
        File wordsFile = null;
        
        // Check for legacy, pre v2.2 or if the user still has the dictionaries jar.
        if (this.hasThesaurusJarResourceFile ())
        {
        
            String thesaurusFileName = Environment.getProperty (BuiltInSynonymProvider.THESAURUS_FILE_NAME_PROPERTY_NAME);
    
            if (thesaurusFileName == null)
            {
    
                throw new GeneralException ("No: " +
                                            BuiltInSynonymProvider.THESAURUS_FILE_NAME_PROPERTY_NAME +
                                            " property found.");
    
            }
    
            String thesaurusIndexFileName = Environment.getProperty (BuiltInSynonymProvider.THESAURUS_FILE_INDEX_NAME_PROPERTY_NAME);
    
            if (thesaurusIndexFileName == null)
            {
    
                throw new GeneralException ("No: " +
                                            BuiltInSynonymProvider.THESAURUS_FILE_INDEX_NAME_PROPERTY_NAME +
                                            " property found.");
    
            }
    
            String thesaurusFileResourceName = Constants.DICTIONARIES_DIR + thesaurusFileName;
    
            File thesaurusFile = new File (Environment.getUserQuollWriterDir ().getPath () + "/" + thesaurusFileName);
    
            if (!thesaurusFile.exists ())
            {
    
                // Extract the thesaurus file to the Quoll Writer directory.
                Environment.extractResourceToFile (thesaurusFileResourceName,
                                                   thesaurusFile);
    
            }
    
            this.tFile = thesaurusFile;
    
            String thesaurusIndexFileResourceName = Constants.DICTIONARIES_DIR + thesaurusIndexFileName;
    
            // Read the index file.
            indexReader = new BufferedReader (new InputStreamReader (Environment.getResourceStream (thesaurusIndexFileResourceName)));

        } else {
            
            String lang = language;
            
            // Look in the thesaurus directory.
            if (Environment.isEnglish (language))
            {
                
                lang = Constants.ENGLISH;
                //"English";
                
            }
                        
            File thesaurusFile = this.getWordsFile (lang);
                
            if (!thesaurusFile.exists ())
            {
                
                throw new GeneralException ("Unable to find thesaurus words file for language: " +
                                            lang +
                                            " at: " +
                                            thesaurusFile);
                
            }
            
            this.tFile = thesaurusFile;
            
            File indexFile = this.getIndexFile (lang);
            
            if (!indexFile.exists ())
            {
                
                throw new GeneralException ("Unable to find thesaurus index file for language: " +
                                            lang +
                                            " at: " +
                                            indexFile);
                
            }
            
            try
            {
            
                indexReader = new BufferedReader (new FileReader (indexFile));
                
            } catch (Exception e) {
                
                throw new GeneralException ("Unable to read thesaurus index file for language: " +
                                            lang +
                                            " at: " +
                                            indexFile);
                
            }
            
        }
        
        if (indexReader == null)
        {
            
            throw new GeneralException ("Unable to find thesaurus index file for language: " +
                                        language);
            
        }
        
        String l = null;

        try
        {

            while ((l = indexReader.readLine ()) != null)
            {

                StringTokenizer t = new StringTokenizer (l,
                                                         "@");

                SynonymIndex si = new SynonymIndex ();
                si.word = t.nextToken ();

                try
                {

                    si.index = Long.parseLong (t.nextToken ());

                } catch (Exception e)
                {

                    Environment.logError ("Bad word lookup index for word: " +
                                          si.word,
                                          e);

                }

                this.synInd.add (si);

            }

            indexReader.close ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to read thesaurus index file",
                                        e);

        }

        try
        {

            this.thesaurusFile = new RandomAccessFile (this.tFile,
                                                       "r");

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get access to thesaurus file: " +
                                        this.tFile,
                                        e);

        }

    }

    public String getWordTypes (String word)
                         throws GeneralException
    {

        if (word == null)
        {

            return null;

        }

        word = word.trim ();

        if (word.equals (""))
        {

            return null;

        }

        if (this.useCache)
        {

            String v = this.synCache.get (word);

            if (v != null)
            {

                return v;

            }

        }

        SynonymIndex si = new SynonymIndex ();
        si.word = word.toLowerCase ().trim ();

        int ind = Collections.binarySearch (this.synInd,
                                            si);

        if (ind < 0)
        {

            return null;

        }

        si = (SynonymIndex) this.synInd.get (ind);

        try
        {

            this.thesaurusFile.seek (si.index);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to seek to: " +
                                        si.index +
                                        " to find word: " +
                                        word +
                                        " in thesaurus file: " +
                                        this.tFile,
                                        e);

        }

        // Get the line.
        String line = null;

        try
        {

            line = this.thesaurusFile.readLine ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to read line from: " +
                                        si.index +
                                        " for word: " +
                                        word +
                                        " in thesaurus file: " +
                                        this.tFile,
                                        e);

        }

        if (line == null)
        {

            throw new GeneralException ("Unable to find line for index: " +
                                        si.index +
                                        " and word: " +
                                        word +
                                        " in thesaurus file: " +
                                        this.tFile);

        }

        // Convert the line back, the format is:
        // word@<parts>,synonym@<parts>,...

        // There will be a result per line.
        StringTokenizer t = new StringTokenizer (line,
                                                 ",");

        // First token is the word.
        String w = t.nextToken ();

        // Get the word and it's parts.
        StringTokenizer wt = new StringTokenizer (w,
                                                  "@");

        if (wt.countTokens () == 2)
        {

            wt.nextToken ();

            String tok = wt.nextToken ();

            if (this.useCache)
            {

                this.synCache.put (word,
                                   tok);

            }

            return tok;

        }

        return null;

    }

    public void setUseCache (boolean v)
    {

        if (!v)
        {

            this.synCache = new HashMap ();

        }

        this.useCache = v;

    }

    public boolean hasSynonym (String word)
    {

        if ((word == null) ||
            (word.trim ().equals ("")))
        {

            return false;

        }

        SynonymIndex si = new SynonymIndex ();
        si.word = word.toLowerCase ().trim ();

        int ind = Collections.binarySearch (this.synInd,
                                            si);

        return ind > -1;

    }

    public Synonyms getSynonyms (String word)
                          throws GeneralException
    {

        Synonyms ret = new Synonyms ();
        ret.word = word;

        if ((word == null) ||
            (word.trim ().equals ("")))
        {

            return ret;

        }

        SynonymIndex si = new SynonymIndex ();
        si.word = word.toLowerCase ().trim ();

        int ind = Collections.binarySearch (this.synInd,
                                            si);

        if (ind < 0)
        {

            return ret;

        }

        si = (SynonymIndex) this.synInd.get (ind);

        try
        {

            this.thesaurusFile.seek (si.index);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to seek to: " +
                                        si.index +
                                        " to find word: " +
                                        word +
                                        " in thesaurus file: " +
                                        this.tFile,
                                        e);

        }

        // Get the line.
        String line = null;

        try
        {

            line = this.thesaurusFile.readLine ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to read line from: " +
                                        si.index +
                                        " for word: " +
                                        word +
                                        " in thesaurus file: " +
                                        this.tFile,
                                        e);

        }

        if (line == null)
        {

            throw new GeneralException ("Unable to find line for index: " +
                                        si.index +
                                        " and word: " +
                                        word +
                                        " in thesaurus file: " +
                                        this.tFile);

        }

        // Convert the line back, the format is:
        // word@<parts>,synonym@<parts>,...

        // There will be a result per line.
        StringTokenizer t = new StringTokenizer (line,
                                                 ",");

        // First token is the word.
        String w = t.nextToken ();

        // Get the word and it's parts.
        StringTokenizer wt = new StringTokenizer (w,
                                                  "@");

        if (wt.countTokens () == 2)
        {

            ret.word = wt.nextToken ();
            ret.setParts (wt.nextToken ());

        }

        if (!word.equalsIgnoreCase (ret.word))
        {

            // Bad...
            throw new GeneralException ("Corrupted thesaurus and/or index file, unable to find word: " +
                                        word +
                                        " in thesarus file but it is in the index, found: " +
                                        w +
                                        " instead.  Index file has location: " +
                                        si.index);

        }

        while (t.hasMoreTokens ())
        {

            String tok = t.nextToken ();

            // Group the words.

            // Format will be: <word>@<parts>
            // For parts see: http://icon.shef.ac.uk/Moby/mpos.html, also O is possible for OTHER.
            StringTokenizer tt = new StringTokenizer (tok,
                                                      "@");

            if (tt.countTokens () == 2)
            {

                String sw = tt.nextToken ();
                String parts = tt.nextToken ();

                boolean added = false;

                if ((parts.indexOf (Synonyms.VERB) != -1) ||
                    (parts.indexOf (Synonyms.VERB_T) != -1) ||
                    (parts.indexOf (Synonyms.VERB_I) != -1))
                {

                    ret.verbs.add (sw);
                    added = true;

                }

                if ((parts.indexOf (Synonyms.NOUN) != -1) ||
                    (parts.indexOf (Synonyms.PLURAL) != -1) ||
                    (parts.indexOf (Synonyms.NOUN_PHRASE) != -1))
                {

                    ret.nouns.add (sw);
                    added = true;

                }

                if ((parts.indexOf (Synonyms.ADJECTIVE) != -1))
                {

                    ret.adjectives.add (sw);
                    added = true;

                }

                if ((parts.indexOf (Synonyms.ADVERB) != -1))
                {

                    ret.adverbs.add (sw);
                    added = true;

                }

                if (!added)
                {

                    ret.other.add (sw);

                }

            }

        }

        // Cycle over the word parts in ret and add the synonyms in the order for the word.
        for (int i = 0; i < ret.getParts ().length (); i++)
        {

            char c = ret.getParts ().charAt (i);

            ret.addToWords (c);

        }

        return ret;

    }

}
