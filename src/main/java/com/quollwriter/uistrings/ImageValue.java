package com.quollwriter.uistrings;

import java.util.*;
import java.io.*;

public class ImageValue extends Value<ImageValue>
{

    private String id = null;
    private String url = null;
    private File file = null;

    public ImageValue (String id,
                       Node   parent,
                       Map    imgData)
    {

        super (id,
               parent,
               imgData);

        if (imgData != null)
        {

            String _id = (String) imgData.get (":id");

            if (_id != null)
            {

                this.id = _id;

            }

            String _url = (String) imgData.get (":url");

            if (_url != null)
            {

                this.url = _url;

            }

            String f = (String) imgData.get (":file");

            if (f != null)
            {

                this.file = new File (f);

            }

        }

    }

    public void setImageFile (File f)
    {

        this.file = f;

    }

    public File getImageFile ()
    {

        return this.file;

    }

    @Override
    public Set<String> getErrors (RefValueProvider prov)
    {

        Set<String> errs = new LinkedHashSet<> ();

        if (this.file == null)
        {

            errs.add ("No image file provided.");

            return errs;

        } else {

            if (!this.file.exists ())
            {

                errs.add ("File: <b>" + this.file + "</b>, does not exist.");

                return errs;

            }

        }

        if (this.file.length () > (300 * 1024))
        {

            errs.add ("Image must be no larger than 300Kb in size.");

        }

        return errs;

    }

    @Override
    public Node cloneNode ()
    {

        ImageValue n = new ImageValue (this.id,
                                       null,
                                       null);

        return n;

    }

    @Override
    public Map getAsJSON ()
    {

        Map m = super.getAsJSON ();

        Map n = new LinkedHashMap ();

        n.put (this.getNodeId (),
               m);

        m.put (":id",
               this.id);

        m.put (":url",
               this.url);

        m.put (":type",
               "img");

        if (this.file != null)
        {

            m.put (":file",
                   this.file.getPath ());

        }

        return n;

    }

    public String getUrl ()
    {

        return this.url;

    }

    @Override
    public String toString ()
    {

        return (this.getId () + "(image,id=" + this.id + ",url=" + this.url + ")");

    }

    @Override
    public Set<Value> getAllValues ()
    {

        return new LinkedHashSet<> ();

    }

}
