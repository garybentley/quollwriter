package com.quollwriter.data;

import com.gentlyweb.xml.*;

import org.jdom.*;


public class Prompt
{

    public static final String USER_PROMPT_ID_PREFIX = "user-";

    public class XMLConstants
    {

        public static final String root = "prompt";
        public static final String id = "id";
        public static final String text = "text";
        public static final String author = "author";
        public static final String url = "url";
        public static final String storyName = "storyName";

    }

    private String id = null;
    private String text = null;
    private String author = null;
    private String url = null;
    private String storyName = null;

    public Prompt(String text)
    {

        this.text = text;
        this.id = Prompt.USER_PROMPT_ID_PREFIX + System.currentTimeMillis ();

    }

    public Prompt(String id,
                  String author,
                  String storyName,
                  String url,
                  String text)
    {

        this.id = id;
        this.author = author;
        this.url = url;
        this.text = text;
        this.storyName = storyName;

    }

    public Prompt(Element root)
           throws JDOMException
    {

        this.id = JDOMUtils.getAttributeValue (root,
                                               XMLConstants.id,
                                               false);

        if (this.id.equals (""))
        {

            this.id = null;

        }

        this.text = JDOMUtils.getChildElementContent (root,
                                                      XMLConstants.text);
        this.author = JDOMUtils.getChildElementContent (root,
                                                        XMLConstants.author,
                                                        false);

        if (this.author.equals (""))
        {

            this.author = null;

        }

        this.storyName = JDOMUtils.getChildElementContent (root,
                                                           XMLConstants.storyName,
                                                           false);

        if (this.storyName.equals (""))
        {

            this.storyName = null;

        }

        this.url = JDOMUtils.getChildElementContent (root,
                                                     XMLConstants.url,
                                                     false);

        if (this.url.equals (""))
        {

            this.url = null;

        }

    }

    public String toString ()
    {

        return "prompt(id: " + this.id + ", author: " + author + ", " + url + ", storyName: " + this.storyName + ")";

    }

    public boolean isUserPrompt ()
    {

        return Prompt.isUserPrompt (this.id);

    }

    public static boolean isUserPrompt (String id)
    {

        return id.startsWith (Prompt.USER_PROMPT_ID_PREFIX);

    }

    public Element getAsElement ()
    {

        Element root = new Element (XMLConstants.root);

        if (this.id != null)
        {

            root.setAttribute (XMLConstants.id,
                               this.id);

        }

        if (this.author != null)
        {

            Element auth = new Element (XMLConstants.author);
            root.addContent (auth);
            auth.addContent (this.author);

        }

        if (this.storyName != null)
        {

            Element st = new Element (XMLConstants.storyName);
            root.addContent (st);
            st.addContent (this.storyName);

        }

        if (this.url != null)
        {

            Element url = new Element (XMLConstants.url);
            root.addContent (url);
            url.addContent (this.url);

        }

        Element text = new Element (XMLConstants.text);
        root.addContent (text);
        text.addContent (this.text);

        return root;

    }

    public void setAuthor (String a)
    {

        this.author = a;

    }

    public void setURL (String u)
    {

        this.url = u;

    }

    public void setStoryName (String n)
    {

        this.storyName = n;

    }

    public String getURL ()
    {

        return this.url;

    }

    public String getAuthor ()
    {

        return this.author;

    }

    public String getStoryName ()
    {

        return this.storyName;

    }

    public void setText (String t)
    {

        this.text = t;

    }

    public String getText ()
    {

        return this.text;

    }

    public String getId ()
    {

        return this.id;

    }
}
