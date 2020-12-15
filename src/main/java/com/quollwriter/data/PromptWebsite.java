package com.quollwriter.data;

import org.dom4j.*;

import com.quollwriter.*;

public class PromptWebsite
{

    public class XMLConstants
    {

        public static final String root = "site";
        public static final String count = "count";
        public static final String name = "name";
        public static final String url = "url";

    }

    private String count = null;
    private String name = null;
    private String url = null;

    public PromptWebsite (Element root)
                   throws GeneralException
    {

        this.count = DOM4JUtils.attributeValue (root,
                                                  XMLConstants.count);
        this.name = DOM4JUtils.attributeValue (root,
                                               XMLConstants.name);
        this.url = DOM4JUtils.childElementContent (root,
                                                   XMLConstants.url);

    }

    public String getURL ()
    {

        return this.url;

    }

    public String getCount ()
    {

        return this.count;

    }

    public String getName ()
    {

        return this.name;

    }

}
