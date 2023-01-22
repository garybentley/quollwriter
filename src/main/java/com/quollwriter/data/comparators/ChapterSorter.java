package com.quollwriter.data.comparators;

import java.util.*;

import com.quollwriter.data.*;


public class ChapterSorter implements Comparator<Chapter>
{

    public static final int INDEX = 0;
    public static final int DATE_CREATED = 1;
    public static final int DATE_LAST_MODIFIED = 2;

    public static final int ASC = 1;
    public static final int DESC = -1;

    private int sortType = ChapterSorter.INDEX;
    private int dir = ChapterSorter.ASC;

    public ChapterSorter()
    {

    }

    public ChapterSorter(int sortType,
                         int dir)
    {

        this.sortType = sortType;

        this.dir = dir;

    }

    public int compare (Chapter o1,
                        Chapter o2)
    {

        if (this.sortType == ChapterSorter.DATE_CREATED)
        {

            return this.dir * o1.getDateCreated ().compareTo (o2.getDateCreated ());

        }

        if (this.sortType == ChapterSorter.DATE_LAST_MODIFIED)
        {

            return this.dir * o1.getLastModified ().compareTo (o2.getLastModified ());

        }

        return (this.dir * o1.getBook ().getChapterIndex (o1)) - o2.getBook ().getChapterIndex (o2);

    }

    public boolean equals (Object o)
    {

        return this == o;

    }

}
