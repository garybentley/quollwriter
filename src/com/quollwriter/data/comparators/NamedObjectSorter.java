package com.quollwriter.data.comparators;

import java.util.*;

import com.quollwriter.data.*;


public class NamedObjectSorter implements Comparator<NamedObject>
{

    private static Map<String, Integer> objectTypeOrder = new HashMap ();

    static
    {

        Map<String, Integer> m = NamedObjectSorter.objectTypeOrder;

        int ind = 1;

        m.put (Scene.OBJECT_TYPE,
               ind++);
        m.put (OutlineItem.OBJECT_TYPE,
               ind++);
        m.put (Chapter.OBJECT_TYPE,
               ind++);
        m.put (Note.OBJECT_TYPE,
               ind++);
        m.put (QCharacter.OBJECT_TYPE,
               ind++);
        m.put (Location.OBJECT_TYPE,
               ind++);
        m.put (QObject.OBJECT_TYPE,
               ind++);
        m.put (ResearchItem.OBJECT_TYPE,
               ind++);
        m.put (IdeaType.OBJECT_TYPE,
               ind++);
        m.put (Idea.OBJECT_TYPE,
               ind++);
        m.put (Book.OBJECT_TYPE,
               ind++);
        m.put (Project.OBJECT_TYPE,
               ind++);

    }

    private static OutlineItemSorter outlineItemComp = new OutlineItemSorter ();

    public NamedObjectSorter()
    {

    }

    public int compare (NamedObject o1,
                        NamedObject o2)
    {

        if ((o1 == null)
            &&
            (o2 == null)
           )
        {
            
            return 0;
            
        }

        if ((o1 == null)
            &&
            (o2 != null)
           )
        {
            
            return 1;
            
        }

        if ((o1 != null)
            &&
            (o2 == null)
           )
        {
            
            return -1;
            
        }

        if (!o1.getObjectType ().equals (o2.getObjectType ()))
        {

            int o1k = NamedObjectSorter.objectTypeOrder.get (o1.getObjectType ());
            int o2k = NamedObjectSorter.objectTypeOrder.get (o2.getObjectType ());

            return o1k - o2k;

        }

        if ((o1 instanceof OutlineItem) &&
            (o2 instanceof OutlineItem))
        {

            return NamedObjectSorter.outlineItemComp.compare ((OutlineItem) o1,
                                                              (OutlineItem) o2);

        }

        if ((o1 instanceof Chapter) &&
            (o2 instanceof Chapter))
        {

            Chapter c1 = (Chapter) o1;
            Chapter c2 = (Chapter) o2;

            // Compare books.
            // If not equal fall through to a name sort.
            if (c1.getBook () == c2.getBook ())
            {

                return c1.getBook ().getChapterIndex (c1) - c2.getBook ().getChapterIndex (c2);

            }

        }

        return o1.getName ().toLowerCase ().compareTo (o2.getName ().toLowerCase ());

    }

    public boolean equals (Object o)
    {

        return this == o;

    }

}
