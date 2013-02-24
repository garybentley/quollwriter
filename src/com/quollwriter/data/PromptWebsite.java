package com.quollwriter.data;

import com.gentlyweb.xml.*;

import org.jdom.*;


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

    public PromptWebsite(Element root)
                  throws JDOMException
    {

        this.count = JDOMUtils.getAttributeValue (root,
                                                  XMLConstants.count);
        this.name = JDOMUtils.getAttributeValue (root,
                                                 XMLConstants.name);
        this.url = JDOMUtils.getChildElementContent (root,
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
