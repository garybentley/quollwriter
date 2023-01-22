package com.quollwriter.data;

import org.dom4j.*;
import org.dom4j.tree.*;

import com.quollwriter.*;

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
           throws GeneralException
    {

        this.id = root.attributeValue (XMLConstants.id);

        this.text = root.element (XMLConstants.text).getTextTrim ();

        this.author = DOM4JUtils.childElementContent (root,
                                                         XMLConstants.author,
                                                         false,
                                                         null);

        this.storyName = DOM4JUtils.childElementContent (root,
                                                            XMLConstants.storyName,
                                                            false,
                                                            null);

        this.url = DOM4JUtils.childElementContent (root,
                                                      XMLConstants.url,
                                                      false,
                                                      null);

    }

    @Override
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

        Element root = new DefaultElement (XMLConstants.root);

        if (this.id != null)
        {

            root.addAttribute (XMLConstants.id,
                               this.id);

        }

        if (this.author != null)
        {

            Element auth = new DefaultElement (XMLConstants.author);
            root.add (auth);
            auth.add (new DefaultCDATA (this.author));

        }

        if (this.storyName != null)
        {

            Element st = new DefaultElement (XMLConstants.storyName);
            root.add (st);
            st.add (new DefaultCDATA (this.storyName));

        }

        if (this.url != null)
        {

            Element url = new DefaultElement (XMLConstants.url);
            root.add (url);
            url.add (new DefaultCDATA (this.url));

        }

        Element text = new DefaultElement (XMLConstants.text);
        root.add (text);
        text.add (new DefaultCDATA (this.text));

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
