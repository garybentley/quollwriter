package com.quollwriter.data.comparators;

import java.util.*;

import com.quollwriter.data.*;


public class ChapterItemSorter implements Comparator<ChapterItem>
{

    public int compare (ChapterItem o1,
                        ChapterItem o2)
    {
/*
        if (o1.getKey () == null)
        {
            
            return 1;
            
        }

        if (o2.getKey () == null)
        {
            
            return 1;
            
        }
  */  
        if (o1.getPosition () == o2.getPosition ())
        {
            
            if ((o1.getKey () == null)
                ||
                (o1.getName () == null)
               )
            {
                
                return 1;
                
            }
    
            if ((o2.getKey () == null)
                ||
                (o2.getName () == null)
               )
            {
                
                return 1;
                
            }
            
            if ((o1 instanceof Scene)
                &&
                (o2 instanceof OutlineItem)
               )
            {
                
                return -1;
                        
            }

            if ((o2 instanceof Scene)
                &&
                (o1 instanceof OutlineItem)
               )
            {
                
                return 1;
                        
            }
                        
            int nc = o1.getName ().compareTo (o2.getName ());
            
            if (nc == 0)
            {
                
                // Return the one created first.
                return o1.getDateCreated ().compareTo (o2.getDateCreated ());
                    
            } 
                
            return nc;
            
            //return (int) (o1.getKey () - o2.getKey ());
            
        }

        return o1.getPosition () - o2.getPosition ();

    }

    public boolean equals (Object o)
    {

        return this == o;

    }

}
