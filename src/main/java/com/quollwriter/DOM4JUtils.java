package com.quollwriter;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;

import org.dom4j.*;
import org.dom4j.tree.*;
import org.dom4j.io.*;

public class DOM4JUtils
{

    public static String childElementContent (Element el,
                                              String  elName)
                                       throws GeneralException
    {

        return DOM4JUtils.childElementContent (el,
                                               elName,
                                               true,
                                               null);

    }

    public static String childElementContent (Element el,
                                              String  elName,
                                              boolean required,
                                              String  onNotProvidedNotRequired)
                                       throws GeneralException
    {

        Element cel = el.element (elName);

        if (cel == null)
        {

            if (required)
            {

                DOM4JUtils.raiseException ("Expected: %1$s to have a child element: %2$s that has content.",
                                           el,
                                           elName);
                return null;

            } else {

                return onNotProvidedNotRequired;

            }

        }

        String v = cel.getText ();

        if ("".equals (v))
        {

            return onNotProvidedNotRequired;

        }

        return v;

    }

    public static String elementAsString (Element el)
                                  throws  GeneralException
    {

        try
        {

            Document doc = DocumentHelper.createDocument ();
            doc.setRootElement (el);
            return el.asXML ();

        } catch (Exception e) {

            DOM4JUtils.raiseException ("Unable to get %1$s as string.",
                                       el);
            return null;

        }

    }

    public static String attributeValue (Element el,
                                         String  attrName)
                                  throws GeneralException
    {

        return DOM4JUtils.attributeValue (el,
                                          attrName,
                                          true);

    }

    public static String attributeValue (Element el,
                                         String  attrName,
                                         boolean required)
                                  throws GeneralException
    {

        String val = el.attributeValue (attrName);

        if ((val == null)
            &&
            (required)
           )
        {

            DOM4JUtils.raiseException ("Expected: %1$s to have an attribute called: %2$s",
                                       DOM4JUtils.getPath (el),
                                       attrName);

        }

        return val;

    }

    public static Boolean attributeValueAsBoolean (Element el,
                                                   String  attrName)
                                            throws GeneralException
    {

        return DOM4JUtils.attributeValueAsBoolean (el,
                                                   attrName,
                                                   true);

    }

    public static Boolean attributeValueAsBoolean (Element el,
                                                   String  attrName,
                                                   boolean required)
                                            throws GeneralException
    {

        String val = DOM4JUtils.attributeValue (el,
                                                attrName,
                                                required);

        if (val == null)
        {

            return false;

        }

        try
        {

            return Boolean.valueOf (val);

        } catch (Exception e) {

            DOM4JUtils.raiseException ("Unable to convert value: %1$s to boolean, attribute: %2$s",
                                       val,
                                       el.attribute (attrName),
                                       e);
            return false;

        }

    }

    public static Integer attributeValueAsInt (Element el,
                                               String  attrName)
                                        throws GeneralException
    {

        return DOM4JUtils.attributeValueAsInt (el,
                                               attrName,
                                               true);

    }

    public static Integer attributeValueAsInt (Element el,
                                               String  attrName,
                                               boolean required)
                                        throws GeneralException
    {

        String val = DOM4JUtils.attributeValue (el,
                                                attrName,
                                                required);

        if (val == null)
        {

            return 0;

        }

        try
        {

            return Integer.valueOf (val);

        } catch (Exception e) {

            DOM4JUtils.raiseException ("Unable to convert value: %1$s to integer, attribute: %2$s",
                                       val,
                                       el.attribute (attrName),
                                       e);
            return 0;

        }

    }

    public static Float attributeValueAsFloat (Element el,
                                               String  attrName)
                                        throws GeneralException
    {

        return DOM4JUtils.attributeValueAsFloat (el,
                                                 attrName,
                                                 true);

    }

    public static Float attributeValueAsFloat (Element el,
                                               String  attrName,
                                               boolean required)
                                        throws GeneralException
    {

        String val = DOM4JUtils.attributeValue (el,
                                                attrName,
                                                required);

        if (val == null)
        {

            return 0f;

        }

        try
        {

            return Float.valueOf (val);

        } catch (Exception e) {

            DOM4JUtils.raiseException ("Unable to convert value: %1$s to float, attribute: %2$s",
                                       val,
                                       el.attribute (attrName),
                                       e);
            return 0f;

        }

    }

    public static Element stringAsElement (String v)
                                    throws GeneralException
    {

        try
        {

            Document doc = DocumentHelper.parseText (v);
            return doc.getRootElement ();

        } catch (Exception e) {

            throw new GeneralException ("Unable to convert text to an element: " + v,
                                        e);

        }

    }

    public static String getElementPath (Element       el,
					                     StringBuilder buf)
    {

        String b = '/' + el.getQualifiedName ();

    	// Now just check to see if element is part of group at this level.
    	// Get the parent.
    	Element parent = el.getParent ();

    	if (parent != null)
    	{

            for (Element cel : parent.elements (el.getName ()))
            {

                int i = 1;

    			if (cel == el)
    			{

    			    // They are the same, add a [X] to the buffer.
    			    b += '[';

    			    if (cel.attribute ("id") != null)
    			    {

        				b += cel.attributeValue ("id");

                    } else {

			            b += i + "";

		            }

		            b += ']';

                }

                i++;

            }

            buf.insert (0,
    			        b);

    	    return DOM4JUtils.getElementPath (parent,
    					                      buf);

        }

	    buf.insert (0,
		            b);

	    return buf.toString ();

    }

    public static String getPath (Element el)
    {

        return DOM4JUtils.getElementPath (el);

    }

    public static String getElementPath (Element el)
    {

        return DOM4JUtils.getElementPath (el,
                                          new StringBuilder ());

    }

    public static String getPath (Attribute attr)
    {

	    String path = DOM4JUtils.getElementPath (attr.getParent (),
						                         new StringBuilder ());
	    return path + "/@" + attr.getQualifiedName ();

    }

    public static Element fileAsElement (Path p)
                                  throws GeneralException
    {

        return DOM4JUtils.fileAsElement (p.toFile ());

    }

    public static Element fileAsElement (File f)
                                  throws GeneralException
    {

        try
        {

            String s = new String (Files.readAllBytes (f.toPath ()),
                                   StandardCharsets.UTF_8);

            Document doc = DocumentHelper.parseText (s);

            return doc.getRootElement ();

        } catch (Exception e) {

            throw new GeneralException ("Unable to convert file: " + f + " to xml",
                                        e);

        }

    }

    public static void raiseException (String    message,
                                       Object... args)
                                throws GeneralException
    {

        Object[] vals = new Object[args.length];

        for (int i = 0; i < vals.length; i++)
        {

            Object o = args[i];

            if (o instanceof Attribute)
            {

                vals[i] = DOM4JUtils.getPath ((Attribute) o);
                continue;

            }

            if (o instanceof Element)
            {

                vals[i] = DOM4JUtils.getPath ((Element) o);
                continue;

            }

            vals[i] = o;

        }

        throw new GeneralException (String.format (message,
                                                   vals));

    }

    public static void writeToFile (Element el,
                                    Path    path,
                                    boolean overwrite)
                             throws GeneralException
    {

        try
        {

            try (BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (path.toFile (), !overwrite), StandardCharsets.UTF_8)))
            {

                XMLWriter xmlw = new XMLWriter (bw);
                xmlw.write (el);
                bw.flush ();
                bw.close ();

            }

        } catch (Exception e) {

            throw new GeneralException ("Unable to write element to path: " + path,
                                        e);

        }

    }

}
