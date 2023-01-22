package com.quollwriter.achievements.rules;

import java.util.*;

import org.dom4j.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

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
                                throws  GeneralException
    {

        super (root);

        this.allowRepeats = DOM4JUtils.attributeValueAsBoolean (root,
                                                                  XMLConstants.allowRepeats,
                                                                  false);

        this.count = DOM4JUtils.attributeValueAsInt (root,
                                                       XMLConstants.count,
                                                       false);

        this.chaptersOnly = DOM4JUtils.attributeValueAsBoolean (root,
                                                                  XMLConstants.chapter,
                                                                  false);

        if ((this.count < 1)
            &&
            (this.chaptersOnly)
           )
        {

            this.count = 1;

        }

        String w = root.attributeValue (XMLConstants.words);

        if (w == null)
        {

            DOM4JUtils.raiseException ("Expected: %1$s to have an attribute: %2$s",
                                       root,
                                       XMLConstants.words);

        }

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

    @Override
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

        if (this.chaptersOnly)
        {

            Set<ChapterCounts> counts = viewer.getAllChapterCountsAsSet ();

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

    @Override
    public void init (Element root)
    {

    }

    @Override
    public void fillState (Element root)
    {

    }

}
