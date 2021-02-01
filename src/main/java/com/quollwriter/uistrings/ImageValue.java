package com.quollwriter.uistrings;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import javafx.beans.property.*;

public class ImageValue extends Value<ImageValue>
{

    private String id = null;
    private String url = null;
    private ObjectProperty<Path> pathProp = null;

    public ImageValue (String id,
                       Node   parent,
                       Map    imgData)
    {

        super (id,
               parent,
               imgData);

        this.pathProp = new SimpleObjectProperty<> ();

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

                this.pathProp.setValue (Paths.get (f));

            }

        }

    }

    public ObjectProperty<Path> pathProperty ()
    {

        return this.pathProp;

    }

    public void setImageFile (Path f)
    {

        this.pathProp.setValue (f);

    }

    public Path getImageFile ()
    {

        return this.pathProp.getValue ();

    }

    @Override
    public Set<String> getErrors (RefValueProvider prov)
    {

        Set<String> errs = new LinkedHashSet<> ();

        Path p = this.pathProp.getValue ();

        if (p == null)
        {

            errs.add ("No image file provided.");

            return errs;

        } else {

            if (!Files.exists (p))
            {

                errs.add ("File: <b>" + p + "</b>, does not exist.");

                return errs;

            }

        }

        try
        {

            if (Files.size (p) > (300 * 1024))
            {

                errs.add ("Image must be no larger than 300Kb in size.");

            }

        } catch (Exception e) {

            errs.add ("Unable to determine image file size.");

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

        if (this.pathProp.getValue () != null)
        {

            m.put (":file",
                   this.pathProp.getValue ().toString ());

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
