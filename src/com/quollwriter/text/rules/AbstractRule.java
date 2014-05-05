package com.quollwriter.text.rules;

import com.gentlyweb.xml.*;

import com.quollwriter.text.*;

import org.jdom.*;


public abstract class AbstractRule<E extends TextBlock> implements Rule<E>
{

    public class XMLConstants
    {

        public static final String root = "rule";
        public static final String description = "description";
        public static final String id = "id";
        public static final String user = "user";
        public static final String summary = "summary";
        public static final String createType = "createType";

    }

    protected String  summary = null;
    protected String  desc = null;
    protected String  id = null;
    protected boolean userRule = false;

    public AbstractRule(boolean userRule)
    {

        this.userRule = userRule;

    }

    public void setSummary (String s)
    {

        this.summary = s;

    }

    public String getSummary ()
    {

        return this.summary;

    }

    public boolean isUserRule ()
    {

        return this.userRule;

    }

    public void setDescription (String d)
    {

        this.desc = d;

    }

    public void setId (String id)
    {

        this.id = id;

    }

    public String getId ()
    {

        return this.id;

    }

    public String getDescription ()
    {

        return this.desc;

    }

    public void init (Element root)
               throws JDOMException
    {

        this.id = JDOMUtils.getAttributeValue (root,
                                               XMLConstants.id);

        this.userRule = JDOMUtils.getAttributeValueAsBoolean (root,
                                                              XMLConstants.user,
                                                              false);

        this.desc = JDOMUtils.getChildElementContent (root,
                                                      XMLConstants.description,
                                                      !this.userRule);

        this.summary = JDOMUtils.getChildElementContent (root,
                                                         XMLConstants.summary,
                                                         true);

    }

    public Element getAsElement ()
    {

        Element root = new Element (XMLConstants.root);

        root.setAttribute (XMLConstants.id,
                           this.id);
        root.setAttribute (XMLConstants.createType,
                           this.getCreateType ());

        if (this.userRule)
        {

            root.setAttribute (XMLConstants.user,
                               Boolean.toString (this.userRule));

        }

        Element summ = new Element (XMLConstants.summary);

        root.addContent (summ);

        summ.addContent (this.summary);

        if (this.desc != null)
        {

            Element desc = new Element (XMLConstants.description);

            root.addContent (desc);

            desc.addContent (this.desc);

        }

        return root;

    }

}
