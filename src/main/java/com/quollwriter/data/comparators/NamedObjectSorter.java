package com.quollwriter.data.comparators;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.data.*;

public class NamedObjectSorter implements Comparator<NamedObject>, ProjectEventListener
{

    private Map<String, Integer> objectTypeOrder = new HashMap<> ();

    private ChapterItemSorter chapterItemComp = new ChapterItemSorter ();

    public NamedObjectSorter (Project proj)
    {

        this.initOrder (proj);

    }

    private void initOrder (Project proj)
    {

        // We lock here on the object we will change, we don't want to be in a situation where
        // the ordering is being used and then it changes underneath whoever is using it.
        synchronized (this.objectTypeOrder)
        {

            Map<String, Integer> m = new HashMap<> ();

            int ind = 1;

            m.put (Scene.OBJECT_TYPE,
                   ind++);
            m.put (OutlineItem.OBJECT_TYPE,
                   ind++);
            m.put (Chapter.OBJECT_TYPE,
                   ind++);
            m.put (Note.OBJECT_TYPE,
                   ind++);

            if (proj != null)
            {

                Set<UserConfigurableObjectType> assetObjTypes = proj.getAssetUserConfigurableObjectTypes (true);

                for (UserConfigurableObjectType type : assetObjTypes)
                {

                    m.put (type.getObjectTypeId (),
                           ind++);

                }

            }

            m.put (IdeaType.OBJECT_TYPE,
                   ind++);
            m.put (Idea.OBJECT_TYPE,
                   ind++);
            m.put (Book.OBJECT_TYPE,
                   ind++);
            m.put (Project.OBJECT_TYPE,
                   ind++);

            this.objectTypeOrder = m;

        }

    }

    @Override
    public void eventOccurred (ProjectEvent ev)
    {

    }

    @Override
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

            Integer _o1k = this.objectTypeOrder.get (o1.getObjectType ());
            Integer _o2k = this.objectTypeOrder.get (o2.getObjectType ());

            int o1k = 0;
            int o2k = 0;

            if (_o1k != null)
            {

                o1k = _o1k.intValue ();

            }

            if (_o2k != null)
            {

                o2k = _o2k.intValue ();

            }

            return o1k - o2k;

        }

        if ((o1 instanceof ChapterItem) &&
            (o2 instanceof ChapterItem))
        {

            return this.chapterItemComp.compare ((ChapterItem) o1,
                                                 (ChapterItem) o2);

        }

        if ((o1 instanceof Chapter) &&
            (o2 instanceof Chapter))
        {

            Chapter c1 = (Chapter) o1;
            Chapter c2 = (Chapter) o2;

            // Compare books.
            // If not equal fall through to a name sort.
            if ((c1.getBook () == null)
                ||
                (c2.getBook () == null)
               )
            {

                // Not entirely sure how this can happen.
                return 0;

            }

            if (c1.getBook () == c2.getBook ())
            {

                return c1.getBook ().getChapterIndex (c1) - c2.getBook ().getChapterIndex (c2);

            }

        }

        if (o1.getName () == null)
        {

            return 1;

        }

        if (o2.getName () == null)
        {

            return -1;

        }

        return o1.getName ().toLowerCase ().compareTo (o2.getName ().toLowerCase ());

    }

    public boolean equals (Object o)
    {

        return this == o;

    }

}
