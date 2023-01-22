package com.quollwriter.achievements.rules;

import java.util.*;

import org.dom4j.*;
import org.dom4j.tree.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

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
                                    throws  GeneralException
    {

        super (root);

        this.sentenceCount = DOM4JUtils.attributeValueAsInt (root,
                                                             XMLConstants.sentenceCount,
                                                             false);

        this.chapterCount = DOM4JUtils.attributeValueAsInt (root,
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

    @Override
    public boolean achieved (AbstractProjectViewer viewer,
                             ProjectEvent          ev)
                             throws                Exception
    {

        return this.achieved (viewer);

    }

    @Override
    public boolean achieved (AbstractProjectViewer viewer)
    {

        if (viewer.getProject () == null)
        {

            throw new IllegalArgumentException ("No project found.");

        }

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

    @Override
    public void init (Element root)
    {

    }

    @Override
    public void fillState (Element root)
    {

    }

}
