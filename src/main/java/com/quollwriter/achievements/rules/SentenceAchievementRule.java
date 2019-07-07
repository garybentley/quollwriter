package com.quollwriter.achievements.rules;

import java.util.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.*;

public class SentenceAchievementRule extends AbstractAchievementRule
{

    public static final String RULE_TYPE = "sentence";

    public class XMLConstants
    {

        public static final String sentenceCount = "sentenceCount";
        public static final String chapterCount = "chapterCount";

    }

    private int sentenceCount = -1;
    private int chapterCount = -1;

    public SentenceAchievementRule (Element root)
                                    throws  JDOMException
    {

        super (root);

        this.sentenceCount = JDOMUtils.getAttributeValueAsInt (root,
                                                               XMLConstants.sentenceCount,
                                                               false);

        this.chapterCount = JDOMUtils.getAttributeValueAsInt (root,
                                                              XMLConstants.chapterCount,
                                                              false);

    }

    public String toString ()
    {

        return super.toString () + "(sentence count: " + this.sentenceCount + ", chapter count: " + this.chapterCount + ")";

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

        Set<NamedObject> chapters = viewer.getProject ().getAllNamedChildObjects (Chapter.class);

        if (this.chapterCount > 0)
        {

            if (this.chapterCount > chapters.size ())
            {

                return false;

            }

        }

        for (NamedObject c : chapters)
        {

            ChapterCounts cc = viewer.getChapterCounts ((Chapter) c);

            if (cc == null)
            {

                return false;

            }

            if (cc.getSentenceCount () >= this.sentenceCount)
            {

                return true;

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
