package com.quollwriter.data.comparators;

import java.util.*;

import com.quollwriter.data.*;


public class ProjectSorter implements Comparator<Project>
{

    public int compare (Project o1,
                        Project o2)
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

        return -1 * o1.getLastEdited ().compareTo (o2.getLastEdited ());

    }

    public boolean equals (Object o)
    {

        return this == o;

    }

}
