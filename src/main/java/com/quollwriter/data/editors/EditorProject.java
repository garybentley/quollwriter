package com.quollwriter.data.editors;

import java.util.*;

import com.quollwriter.data.*;

public class EditorProject extends AbstractEditorObject
{

    public static final String OBJECT_TYPE = "editorproject";
    
    public enum WordCountLength
    {
        
        shortStory  ("shortStory", "Short story (less than 7,500 words)"),
        novella  ("novella", "Novella (17,500 to 40,000 words"),
        novelette  ("novelette", "Novelette (7,500 to 17,5000 words"),
        novel  ("novel", "Novel (over 40,000 words");
        
        private final String type;
        private final String desc;
        
        WordCountLength (String type,
                         String desc)
        {
            
            this.type = type;
            this.desc = desc;
            
        }
        
        public static Set<WordCountLength> convert (Set<String> types)
        {
            
            Set<WordCountLength> ret = new LinkedHashSet ();
            
            for (String s : types)
            {
                
                WordCountLength wcl = WordCountLength.getWordCountLengthByType (s);
                
                if (wcl == null)
                {
                    
                    wcl = WordCountLength.getWordCountLengthByDescription (s);
                    
                }
                
                if (wcl == null)
                {
                    
                    continue;
                    
                }
                
                ret.add (wcl);
                
            }
            
            return ret;
            
        }
        
        public static Set<String> getTypes (Set<WordCountLength> wcls)
        {
            
            Set<String> ret = new LinkedHashSet ();
            
            if (wcls == null)
            {
                
                return ret;
                
            }
            
            for (WordCountLength wcl : wcls)
            {
                
                if (wcl == null)
                {
                    
                    continue;
                    
                }
                
                ret.add (wcl.getType ());
                
            }
            
            return ret;
            
        }

        public static Set<String> getDescriptions (Set<WordCountLength> wcls)
        {
            
            Set<String> ret = new LinkedHashSet ();
            
            if (wcls == null)
            {
                
                return ret;
                
            }
            
            for (WordCountLength wcl : wcls)
            {
                
                if (wcl == null)
                {
                    
                    continue;
                    
                }
                
                ret.add (wcl.getDescription ());
                
            }
            
            return ret;
            
        }

        public static WordCountLength getWordCountLengthByType (String type)
        {
            
            if (type.equals (WordCountLength.shortStory.getType ()))
            {
            
                return WordCountLength.shortStory;
            
            }
            
            if (type.equals (WordCountLength.novella.getType ()))
            {
            
                return WordCountLength.novella;
            
            }

            if (type.equals (WordCountLength.novelette.getType ()))
            {
            
                return WordCountLength.novelette;
            
            }

            if (type.equals (WordCountLength.novel.getType ()))
            {
            
                return WordCountLength.novel;
            
            }
            
            return null;
            
        }
        
        public static WordCountLength getWordCountLengthByDescription (String d)
        {
            
            if (d.equals (WordCountLength.shortStory.getDescription ()))
            {
            
                return WordCountLength.shortStory;
            
            }
            
            if (d.equals (WordCountLength.novella.getDescription ()))
            {
            
                return WordCountLength.novella;
            
            }

            if (d.equals (WordCountLength.novelette.getDescription ()))
            {
            
                return WordCountLength.novelette;
            
            }

            if (d.equals (WordCountLength.novel.getDescription ()))
            {
            
                return WordCountLength.novel;
            
            }
            
            return null;
            
        }

        public String getDescription ()
        {
            
            return this.desc;
            
        }
        
        public String getType ()
        {
            
            return this.type;
            
        }
        
        public String toString ()
        {
            
            return this.getType ();
            
        }
        
    }
    
    private Project proj = null;
        
    private Set<String> genres = new HashSet ();    
    private WordCountLength wcLength = null;
    
    private String expectations = null;
    
    public EditorProject ()
    {
        
        super (OBJECT_TYPE);
        
    }
        
    public void setProject (Project p)
    {
        
        this.proj = p;
        
    }
    
    public Project getProject ()
    {
    
        return this.proj;
        
    }
    
    public void setWordCountLength (WordCountLength wc)
    {
        
        this.wcLength = wc;
        
    }
    
    public WordCountLength getWordCountLength ()
    {
        
        return this.wcLength;
        
    }
    
    public Set<String> getGenres ()
    {
        
        return this.genres;
        
    }
        
    public void setGenres (Set<String> s)
    {
        
        this.genres = s;
        
    }
    
    public void setExpectations (String e)
    {
        
        this.expectations = e;
        
    }
    
    public String getExpectations ()
    {
        
        return this.expectations;
        
    }
            
}