package com.quollwriter.achievements.rules;

import java.util.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.*;

public class WordCountAchievementRule extends AbstractAchievementRule
{

    public static final String RULE_TYPE = "wordcount";
        
    public class XMLConstants
    {
        
        public static final String wordCount = "wordCount";
        public static final String count = "count";
        public static final String chapter = "chapter";
        
    }
    
    private int count = -1;
    private int wordCount = 0;
    private boolean chaptersOnly = false;
        
    public WordCountAchievementRule (Element root)
                                     throws  JDOMException
    {
        
        super (root);
                
        this.count = JDOMUtils.getAttributeValueAsInt (root,
                                                       XMLConstants.count,
                                                       false);

        this.wordCount = JDOMUtils.getAttributeValueAsInt (root,
                                                           XMLConstants.wordCount);
        
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
        
    }

    public String toString ()
    {
        
        return super.toString () + "(count: " + this.count + ", wordCount: " + this.wordCount + ", chapters only: " + this.chaptersOnly + ")";
        
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
        
        if (viewer == null)
        {
            
            return false;
            
        }
        
        if (this.chaptersOnly)
        {
            
            int c = 0;
               
            Set<ChapterCounts> counts = viewer.getAllChapterCounts2 (false);
            
            for (ChapterCounts cc : counts)
            {
                
                if (cc.wordCount >= this.wordCount)
                {
                    
                    c++;
                        
                }
                
            }
            
            if (c >= this.count)
            {
                
                return true;
                
            }
            
        }
        
        if (viewer.getAllChapterCounts (false).wordCount >= this.wordCount)
        {
            
            return true;
            
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