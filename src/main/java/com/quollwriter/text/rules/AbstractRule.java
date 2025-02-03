package com.quollwriter.text.rules;

import java.util.*;

import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.text.*;

import com.quollwriter.ui.fx.components.Form;

// TODO import com.quollwriter.ui.fx.components.Form;

import org.dom4j.*;
import org.dom4j.tree.*;


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
    private String defaultSummary = null;

    public AbstractRule ()
    {

    }

    //public abstract String getEditFormTitle (boolean add);

    public Set<Form.Item> getFormItems ()
    {

        return null;

    }

    public StringProperty getFormError ()
    {

        return null;

    }

    public String getDefaultSummary ()
    {

        return this.defaultSummary;

    }

    @Override
    public void setSummary (String s)
    {

        this.summary = s;

    }

    @Override
    public String getSummary ()
    {

        return this.summary;

    }

    @Override
    public void setUserRule (boolean u)
    {

        this.userRule = u;

    }

    @Override
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
               throws GeneralException
    {

        this.id = DOM4JUtils.attributeValue (root,
                                               XMLConstants.id);

        this.userRule = DOM4JUtils.attributeValueAsBoolean (root,
                                                              XMLConstants.user,
                                                              false);

        this.desc = DOM4JUtils.childElementContent (root,
                                                      XMLConstants.description,
                                                      false,
                                                      null);

        this.summary = DOM4JUtils.childElementContent (root,
                                                         XMLConstants.summary);

        if (!this.userRule)
        {

            this.defaultSummary = this.summary;

        }

    }

    public Element getAsElement ()
    {

        Element root = new DefaultElement (XMLConstants.root);

        root.addAttribute (XMLConstants.id,
                           this.id);
        root.addAttribute (XMLConstants.createType,
                           this.getClass ().getName ());

        if (this.userRule)
        {

            root.addAttribute (XMLConstants.user,
                               Boolean.toString (this.userRule));

        }

        Element summ = new DefaultElement (XMLConstants.summary);

        root.add (summ);

        summ.add (new DefaultCDATA (this.summary));

        if ((this.desc != null)
            &&
            (this.desc.length () > 0)
           )
        {

            Element desc = new DefaultElement (XMLConstants.description);

            root.add (desc);

            desc.add (new DefaultCDATA (this.desc));

        }

        return root;

    }

    @Override
    public int hashCode ()
    {

        return this.id.hashCode ();

    }

    @Override
    public boolean equals (Object o)
    {

        if ((o == null) || (!(o instanceof AbstractRule)))
        {

            return false;

        }

        AbstractRule r = (AbstractRule) o;

        return this.id.equals (r.id);

    }

}
