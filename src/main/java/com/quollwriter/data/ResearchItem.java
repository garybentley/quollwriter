package com.quollwriter.data;

import org.dom4j.*;


public class ResearchItem extends LegacyAsset
{

    public static final String WEB_PAGE_LEGACY_FIELD_ID = "webpage";
    public static final String WEB_PAGE_LEGACY_FIELD_FORM_NAME = "Web Page";

    public static final String OBJECT_TYPE = "researchitem";
    public static final String URL = "url";

    private String url = null;

    public ResearchItem (UserConfigurableObjectType objType)
    {

        super (objType);

    }

    public String getUrl ()
    {

        return this.url;

    }

    public void setUrl (String url)
    {

        String oldUrl = this.url;

        UserConfigurableObjectField f = this.getLegacyField (WEB_PAGE_LEGACY_FIELD_ID);

        if (f == null)
        {

            UserConfigurableObjectTypeField type = this.getLegacyTypeField (WEB_PAGE_LEGACY_FIELD_ID);

            if (type == null)
            {

                return;

            }

            f = new UserConfigurableObjectField (type);

            this.addField (f);

        }

        f.setValue (url);

        this.url = url;

        this.firePropertyChangedEvent (URL,
                                       oldUrl,
                                       this.url);

    }

    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

        ResearchItem ri = (ResearchItem) old;

        this.addFieldChangeElement (root,
                                    "url",
                                    ((old != null) ? ri.getUrl () : null),
                                    this.url);

    }

}
