package com.quollwriter.data.comparators;

import java.util.*;

import com.quollwriter.data.*;


public class ProjectInfoSorter implements Comparator<ProjectInfo>
{

    public int compare (ProjectInfo o1,
                        ProjectInfo o2)
    {

        if (o1.getLastEdited () == o2.getLastEdited ())
        {

            return 0;

        }

        if (o2.getLastEdited () == null)
        {

            return -1;

        }

        if (o1.getLastEdited () == null)
        {

            return 1;

        }

        if (o1.getLastEdited ().compareTo (o2.getLastEdited ()) == 0)
        {
            
            return o1.getName ().compareTo (o2.getName ());
            
        }
        
        return -1 * o1.getLastEdited ().compareTo (o2.getLastEdited ());

    }

    public boolean equals (Object o)
    {

        return this == o;

    }

}
