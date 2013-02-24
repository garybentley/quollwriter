package com.quollwriter;

import java.util.*;

import javax.swing.*;

import com.quollwriter.ui.components.*;


public class DefaultIconProvider implements IconProvider
{

    private Map iconOverrides = new HashMap ();

    public void putIcon (String name,
                         String realName)
    {

        this.iconOverrides.put (name,
                                realName);

    }

    public ImageIcon getIcon (String  name,
                              int     type)
    {

        if (name == null)
        {

            return null;

        }

        String n = (String) this.iconOverrides.get (name);

        if (n != null)
        {

            name = n;

        }

        return Environment.getIcon (name,
                                    type);

    }

}
