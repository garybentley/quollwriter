package com.quollwriter.uistrings;

import java.util.*;

import com.quollwriter.*;

public class UILanguageStringsInfo
{

    public String id = null;
    public String nativeName = null;
    public String languageName = null;
    public int percentComplete = 0;
    public boolean user = false;
    public Version version = null;

    public UILanguageStringsInfo (String  id,
                                  String  nativeName,
                                  String  langName,
                                  int     percentComplete,
                                  Version version,
                                  boolean user)
    {

        this.id = id;
        this.nativeName = nativeName;
        this.languageName = langName;
        this.percentComplete = percentComplete;
        this.version = version;
        this.user = user;

    }

    @Override
    public int hashCode ()
    {

        return Objects.hash (id, this.version);

    }

    @Override
    public boolean equals (Object o)
    {

        if ((o == null) || (!(o instanceof UILanguageStringsInfo)))
        {

            return false;

        }

        UILanguageStringsInfo inf = (UILanguageStringsInfo) o;

        if (this.id.equals (inf.id))
        {

            return this.version.equals (inf.version);

        }

        return false;

    }

}
