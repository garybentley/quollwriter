package com.quollwriter.achievements.rules;

import java.util.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.*;

public class WordAchievementRule extends AbstractAchievementRule
{

    public static final String RULE_TYPE = "word";
        
    public class XMLConstants
    {
        
        public static final String wordCount = "wordCount";
        public static final String count = "count";
        public static final String chapter = "chapter";
        public static final String allowRepeats = "allowRepeats";
        public static final String words = "words";
        
    }
    
    private int count = -1;
    private boolean allowRepeats = false;
    private boolean chaptersOnly = false;
    private Map<String, String> words = new HashMap ();
        
    public WordAchievementRule (Element root)
                                throws  JDOMException
    {
        
        super (root);

        this.allowRepeats = JDOMUtils.getAttributeValueAsBoolean (root,
                                                                  XMLConstants.allowRepeats,
                                                                  false);
                
        this.count = JDOMUtils.getAttributeValueAsInt (root,
                                                       XMLConstants.count,
                                                       false);
        
        this.chaptersOnly = JDOMUtils.getAttributeValueAsBoolean (root,
                                                                  XMLConstants.chapter,
                                                                  false);
        
        if ((this.count < 1)
            &&
            (this.chaptersOnly)
           )
        {
            
            this.count = 1;
            
        }        
        
        String w = JDOMUtils.getAttributeValue (root,
                                                XMLConstants.words);
        
        StringTokenizer t = new StringTokenizer (w,
                                                 ",;");
        
        while (t.hasMoreTokens ())
        {
            
            this.words.put (t.nextToken ().trim ().toLowerCase (),
                            "");
            
        }
        
    }

    public String toString ()
    {
        
        return super.toString () + "(count: " + this.count + ", words: " + this.words + ", chapters only: " + this.chaptersOnly + ", allow repeats: " + this.allowRepeats + ")";
        
    }
        
    public boolean shouldPersistState ()
    {
        
        return false;
        
    }
    
    public boolean achieved (AbstractProjectViewer viewer,
                             ProjectEvent          ev)
                             throws                Exception
    {
        
        return this.achieved (viewer);
        
    }

    public boolean achieved (AbstractProjectViewer viewer)
    {
        
        if (this.chaptersOnly)
        {
            
            Set<ChapterCounts> counts = viewer.getAllChapterCounts2 (false);
                   
            for (ChapterCounts cc : counts)
            {
                
                int c = 0;
                
                for (String w : this.words.keySet ())
                {
                    
                    if (cc.wordFrequency != null)
                    {
                    
                        Integer wc = cc.wordFrequency.get (w);
                        
                        if (wc != null)
                        {
                            
                            if (this.allowRepeats)
                            {
    
                                c += wc.intValue ();
    
                            } else {
    
                                c++;
                                                            
                            }
                                                    
                        }

                    }
                                            
                }

                if (c >= this.count)
                {
                    
                    return true;
                    
                }
                
            }
                                    
        }
                
        return false;
        
    }

    public void init (Element root)
    {
                
    }
    
    public void fillState (Element root)
    {
                
    }
     
}