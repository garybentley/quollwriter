package com.quollwriter.data.comparators;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;

public class NamedObjectSorter implements Comparator<NamedObject>, ProjectEventListener
{

    private static NamedObjectSorter sorter = null;

    private Map<String, Integer> objectTypeOrder = new HashMap ();

    private ChapterItemSorter chapterItemComp = new ChapterItemSorter ();
        
    public static NamedObjectSorter getInstance ()
    {
                
        if (NamedObjectSorter.sorter == null)
        {
        
            Object o = new Object ();
            
            synchronized (o)
            {
                
                NamedObjectSorter.sorter = new NamedObjectSorter ();
                
                Environment.addUserProjectEventListener (NamedObjectSorter.sorter);
                
            }
            
        }
        
        return NamedObjectSorter.sorter;
        
    }
    
    protected NamedObjectSorter()
    {

        this.initOrder ();
            
    }
    
    private void initOrder ()
    {
                 
        // We lock here on the object we will change, we don't want to be in a situation where
        // the ordering is being used and then it changes underneath whoever is using it.
        synchronized (this.objectTypeOrder)
        {
        
            Map<String, Integer> m = new HashMap ();
    
            int ind = 1;
    
            m.put (Scene.OBJECT_TYPE,
                   ind++);
            m.put (OutlineItem.OBJECT_TYPE,
                   ind++);
            m.put (Chapter.OBJECT_TYPE,
                   ind++);
            m.put (Note.OBJECT_TYPE,
                   ind++);
            
            Set<UserConfigurableObjectType> assetObjTypes = Environment.getAssetUserConfigurableObjectTypes (true);
    
            for (UserConfigurableObjectType type : assetObjTypes)
            {
                    
                m.put (type.getObjectTypeId (),
                       ind++);
    
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

        if (ev.getType ().equals (ProjectEvent.USER_OBJECT_TYPE))
        {

            this.initOrder ();
        
        }
        
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

        // Since the sorter are often used for contains and equality tests for Sets/Maps etc we need
        // to do a check here for the same object being added twice.
        if (o1.getObjectType ().equals (o2.getObjectType ()))
        {

            if (o1.getKey () == o2.getKey ())
            {
                
                return 0;
                
            }

        }

        if (!o1.getObjectType ().equals (o2.getObjectType ()))
        {

            int o1k = this.objectTypeOrder.get (o1.getObjectType ());
            int o2k = this.objectTypeOrder.get (o2.getObjectType ());

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
