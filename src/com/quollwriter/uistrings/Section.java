package com.quollwriter.uistrings;

import java.util.*;

public class Section
{

    public String id = null;
    public String icon = null;
    public String name = null;

    public Section (Map data)
    {

        Object id = data.get ("id");

        if (id == null)
        {

            throw new IllegalArgumentException ("Expected to find an id.");

        }

        this.id = id.toString ();

        Object icon = data.get ("icon");

        if (icon == null)
        {

            throw new IllegalArgumentException ("Expected to find an icon.");

        }

        this.icon = icon.toString ();

        Object name = data.get ("name");

        if (name == null)
        {

            throw new IllegalArgumentException ("Expected to find a name.");

        }

        this.name = name.toString ();

    }

}
