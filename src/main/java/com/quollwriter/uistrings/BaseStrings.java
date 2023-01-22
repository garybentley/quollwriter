package com.quollwriter.uistrings;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

import org.dom4j.*;

import com.quollwriter.*;

public class BaseStrings implements RefValueProvider
{

    public static final String ID_PART_SEP = ".";
    public static final String ID_REF_START = "${";
    public static final String ID_REF_END = "}";

    private BaseStrings parent = null;
    private Map<String, Node> nodes = new HashMap<> ();

    public BaseStrings (BaseStrings derivedFrom)
    {

        this.parent = derivedFrom;

    }

    public BaseStrings getParent ()
    {

        return this.parent;

    }

    @Override
    public int getSCount (String id)
    {

        return this.getSCount (BaseStrings.getIdParts (id));

    }

    public int getSCount (List<String> idparts)
    {

        Node n = this.getNode (idparts);

        if (n == null)
        {

            return 0;

        }

        if (n instanceof TextValue)
        {

            TextValue tv = (TextValue) n;

            return tv.getSCount ();

        }

        return 0;

    }

    public void setParent (BaseStrings parent)
    {

        this.parent = parent;

    }

    public void init (Map<String, Object> m)
    {

        // Ensure we can resolve everything.
        Iterator iter = m.keySet ().iterator ();

        while (iter.hasNext ())
        {

            String k = iter.next ().toString ();

            if (BaseStrings.isSpecialId (k))
            {

                continue;

            }

            List ids = new ArrayList ();

            ids.add (k);

            Object v = m.get (k);

            if (v instanceof Map)
            {

                Map om = (Map) v;

                this.nodes.put (k,
                                new Node (k,
                                          null,
                                          om));

                continue;

            }

            if (v instanceof String)
            {

                String val = v.toString ();

                this.nodes.put (k,
                                new TextValue (k,
                                               null,
                                               val,
                                               m));

            }

        }

    }

    /**
     * Returns a data structure that is suitable for JSON encoding.
     *
     * @return The data
     */
    public Map getAsJSON ()
    {

        Map m = new HashMap ();

        for (String idPrefix : this.nodes.keySet ())
        {

            m.put (idPrefix,
                   this.nodes.get (idPrefix).getAsJSON ());

        }

        return m;

    }

    public Node createNode (String id)
    {

        return new Node (id,
                         null);

    }

    public static Set<String> getRefIds (String text)
    {

        Set<String> ids = new LinkedHashSet<> ();

        int start = 0;

        while ((start = text.indexOf (ID_REF_START,
                                      start)) > -1)
        {

            int end = text.indexOf (ID_REF_END,
                                    start);

            if (end > (start + ID_REF_START.length ()))
            {

                String sid = text.substring (start + ID_REF_START.length (),
                                             end);

                sid = sid.trim ();

                if (sid.length () > 0)
                {

                    ids.add (sid);

                }

                start = end + ID_REF_END.length ();

            } else {

                start += ID_REF_START.length ();

            }

        }

        return ids;

    }

    public Node removeNode (List<String> idparts)
                     throws GeneralException
    {

        if ((idparts == null)
            ||
            (idparts.size () == 0)
           )
        {

            throw new IllegalArgumentException ("No id provided.");

        }

        String f = idparts.get (0);

        // See if we already have the first node.
        Node n = this.nodes.get (f);

        if (n == null)
        {

            return null;

        }

        if (idparts.size () == 1)
        {

            this.nodes.remove (n);

            return n;

        }

        return n.removeNode (idparts.subList (1, idparts.size ()));

    }

    public ImageValue insertImageValue (List<String> idparts)
                                 throws GeneralException
    {

        if ((idparts == null)
            ||
            (idparts.size () == 0)
           )
        {

            throw new IllegalArgumentException ("No id provided.");

        }

        String f = idparts.get (0);

        // See if we already have the first node.
        Node n = this.nodes.get (f);

        if (idparts.size () == 1)
        {

            if (n != null)
            {

                throw new GeneralException ("Already have a node called: " + f);

            } else {

                ImageValue v = new ImageValue (f,
                                               n,
                                               null);

                this.nodes.put (f, v);

            }

        } else {

            if (n == null)
            {

                n = new Node (f,
                              null);

                this.nodes.put (f, n);

            }

        }

        return n.insertImageValue (idparts.subList (1, idparts.size ()));

    }

    public Set<Value> find (String text)
    {

        Set<Value> ret = new LinkedHashSet<> ();

        for (Value v : this.getAllValues ())
        {

            if (v.match (text))
            {

                ret.add (v);

            }

        }

        return ret;

    }

    public TextValue insertTextValue (List<String> idparts)
                               throws GeneralException
    {

        if ((idparts == null)
            ||
            (idparts.size () == 0)
           )
        {

            throw new IllegalArgumentException ("No id provided.");

        }

        String f = idparts.get (0);

        // See if we already have the first node.
        Node n = this.nodes.get (f);

        if (idparts.size () == 1)
        {

            if (n != null)
            {

                throw new GeneralException ("Already have a node called: " + f);

            } else {

                TextValue v = new TextValue (f,
                                             n,
                                             null,
                                             null,
                                             0);

                this.nodes.put (f, v);

            }

        } else {

            if (n == null)
            {

                n = new Node (f,
                              null);

                this.nodes.put (f, n);

            }

        }

        return n.insertTextValue (idparts.subList (1, idparts.size ()));

    }

    public static Id getId (String text,
                            int    offset)
    {

        Set<Id> ids = BaseStrings.getIds (text);

        for (Id id : ids)
        {

            if ((offset >= id.getStart ())
                &&
                (offset <= id.getEnd ())
               )
            {

                return id;

            }

        }

        return null;

    }
/*
copy?
    public static Set<Id> getIds (String text)
    {

        Set<Id> ret = new LinkedHashSet<> ();

        if ((text == null)
            ||
            (text.trim ().length () == 0)
           )
        {

            return ret;

        }

        java.util.List<String> lines = Utilities.splitString (text,
                                                              "\n");

        for (String l : lines)
        {

            int start = 0;

            while ((start = l.indexOf (ID_REF_START, start)) != -1)
            {

                String id = null;
                boolean partial = false;

                start += ID_REF_START.length ();

                int ind = start;

                int idendind = l.indexOf (ID_REF_END, start);

                if (idendind > -1)
                {

                    id = l.substring (start, idendind);

                    partial = false;
                    //hasClosingBrace = true;
                    start += id.length ();
                    start += ID_REF_END.length ();

                } else {

                    StringBuilder b = new StringBuilder ();

                    for (int i = start; i < l.length (); i++)
                    {

                        char c = text.charAt (i);

                        if (Character.isWhitespace (c))
                        {

                            break;

                        }

                        b.append (c);

                    }

                    id = b.toString ();
                    start += id.length ();
                    partial = true;

                }

                ret.add (new Id (ind, id, partial));

            }

        }

        return ret;

    }
*/
    public static Set<Id> getIds (String text)
    {

        Set<Id> ret = new LinkedHashSet<> ();

        if ((text == null)
            ||
            (text.trim ().length () == 0)
           )
        {

            return ret;

        }

        int len = 0;

        int start = 0;

        while ((start = text.indexOf (ID_REF_START, start)) != -1)
        {

            String id = null;
            boolean partial = false;

            start += ID_REF_START.length ();

            int ind = start;

            int idendind = text.indexOf (ID_REF_END, start);

            if (idendind > -1)
            {

                id = text.substring (start, idendind);

                partial = false;
                //hasClosingBrace = true;
                start += id.length ();
                start += ID_REF_END.length ();

            } else {

                StringBuilder b = new StringBuilder ();

                for (int i = start; i < text.length (); i++)
                {

                    char c = text.charAt (i);

                    if (Character.isWhitespace (c))
                    {

                        break;

                    }

                    b.append (c);

                }

                id = b.toString ();
                start += id.length ();
                partial = true;

            }

            ret.add (new Id (ind, id, partial));

        }

            return ret;
/*
        java.util.List<String> lines = Utils.splitString (text,
                                                          "\n");

        for (String l : lines)
        {

            int start = 0;

            while ((start = l.indexOf (ID_REF_START, start)) != -1)
            {

                String id = null;
                boolean partial = false;

                start += ID_REF_START.length ();

                int ind = start;

                int idendind = l.indexOf (ID_REF_END, start);

                if (idendind > -1)
                {

                    id = l.substring (start, idendind);

                    partial = false;
                    //hasClosingBrace = true;
                    start += id.length ();
                    start += ID_REF_END.length ();

                } else {

                    StringBuilder b = new StringBuilder ();

                    for (int i = start; i < l.length (); i++)
                    {

                        char c = l.charAt (i);

                        if (Character.isWhitespace (c))
                        {

                            break;

                        }

                        b.append (c);

                    }

                    id = b.toString ();
                    start += id.length ();
                    partial = true;

                }

                ret.add (new Id (ind + len, id, partial));

            }

            len += l.length () + 1;

        }

        return ret;
*/
    }

    public static List<String> buildRefValsTree (String           text,
                                                 String           rootId,
                                                 List<String>     ids,
                                                 RefValueProvider prov)
    {

        if (text == null)
        {

            return null;

        }

        Set<Id> refids = BaseStrings.getIds (text);

        for (Id rid : refids)
        {

            if (rid.isPartial ())
            {

                continue;

            }
            //Value v = strings.getValue (rid);

            if (rid.getId ().equals (rootId))
            {

                return ids;

            }

            if (ids.contains (rid.getId ().trim ()))
            {

                // Already have this, got a loop.
                return ids;

            }

            ids.add (rid.getId ().trim ());

            int ind = ids.size () - 1;

            String rv = prov.getRawText (rid.getId ().trim ());//getString (rid);

            if (rv == null)
            {

                return null;

            }

            List<String> nids = BaseStrings.buildRefValsTree (rv,
                                                              rootId,
                                                              new ArrayList<> (ids),
                                                              prov);

            if (nids != null)
            {

                return nids;

            } else {

                ids = ids.subList (0, ind);

            }

        }

        return null;

    }

    public static Set<String> getErrors (String           text,
                                         String           textId,
                                         int              scount,
                                         RefValueProvider prov)
    {

        Set<String> errors = new LinkedHashSet<> ();

        if ((text == null)
            ||
            ("".equals (text))
           )
        {

            return errors;

        }

        List<String> vals = BaseStrings.buildRefValsTree (text,
                                                          textId,
                                                          new ArrayList<String> (),
                                                          prov);

        if (vals != null)
        {

            StringBuilder b = new StringBuilder ();

            for (String v : vals)
            {

                if (b.length () > 0)
                {

                    b.append (" -> ");

                }

                b.append (v);

            }

            errors.add ("Reference loop detected between: " + textId + " and: " + b.toString ());

            return errors;

        }

        Set<Id> ids = BaseStrings.getIds (text);

        for (Id id : ids)
        {

            if (id.isPartial ())
            {

                errors.add (String.format ("No matching %s found for opening %s at location: %s",
                                           ID_REF_END,
                                           ID_REF_START,
                                           id.getStart ()));

            }

            if (!id.getId ().trim ().equals (id.getId ()))
            {

                errors.add (String.format ("Id %s at location: %s contains invalid characters",
                                           id.getId (),
                                           id.getStart ()));

            }

            if (id.getId ().trim ().equals (""))
            {

                errors.add (String.format ("No id provided at location: %s",
                                           id.getStart ()));

            } else {

                if (!prov.isIdValid (id.getId ()))
                {

                    errors.add (String.format ("Id: %s, referenced at location: %s does not exist.",
                                id.getId (),
                                id.getStart ()));

                }

            }

        }

        scount = prov.getSCount (textId);

        if (scount > 0)
        {

            for (int i = 0; i < scount; i++)
            {

                String sid = "%" + (i + 1) + "$s";

                if (text.indexOf (sid) < 0)
                {

                    errors.add ("Expected to find value: " + sid);

                }

            }


        }

        // Check for values beyond the scount.
        for (int i = 0; i < 10; i++)
        {

            if ((scount < 1)
                ||
                (i > scount)
               )
            {

                String sid = "%" + (i + 1) + "$s";

                if (text.indexOf (sid) > -1)
                {

                    errors.add ("Invalid value used: " + sid + ", scount: " + scount);

                }

            }

        }

        try
        {

            if (scount > 0)
            {

                Object[] test = new String[scount];

                for (int i = 0; i < scount; i++)
                {

                    test[i] = "test";

                }

                String.format (text,
                               test);

            } else {

                //String.format (text);

            }

        } catch (Exception e) {

            errors.add ("Invalid value used: " + e.getMessage ());

        }

/*
        Set<String> refids = LanguageStrings.getRefIds (text);

        if (refids.contains (textId))
        {

            errors.add ("Value uses itself O_o");

        }
*/

        return errors;

    }

    public static String buildText (String           text,
                                    RefValueProvider strings)
    {

        StringBuilder b = new StringBuilder (text);

        int start = 0;

        while ((start = b.indexOf (ID_REF_START,
                                   start)) > -1)
        {

            int end = b.indexOf (ID_REF_END,
                                 start);

            if (end > (start + ID_REF_START.length ()))
            {

                String sid = b.substring (start + ID_REF_START.length (),
                                          end);

                int bind = sid.indexOf ("|");
                String sub = null;

                if (bind > -1)
                {

                    sub = sid.substring (0, bind);

                    sid = sid.substring (bind + 1);

                }

                String sv = strings.getString (sid);

                if (sv == null)
                {

                    sv = ID_REF_START + sid + ID_REF_END;

                }

                if (sub != null)
                {

                    if (sub.equalsIgnoreCase ("u"))
                    {

                        // Uppercase the first letter.

                    }

                    if (sub.equalsIgnoreCase ("l"))
                    {

                        // Lowercase the first letter.
                        //char c[] = idv.toCharArray ();

                        //c[0] = Character.toLowerCase (c[0]);

                        //idv = new String (c);

                    }

                    if (sub.equalsIgnoreCase ("ua"))
                    {

                        // Uppercase the first letter of each word.

                    }

                    if (sub.equalsIgnoreCase ("la"))
                    {

                        // Lowercase the first letter of each word.

                    }

                }

                b.replace (start,
                           end + ID_REF_END.length (),
                           sv);

                start += sv.length ();

            } else {

                start += ID_REF_START.length ();

            }

        }

        String s = b.toString ();

        s = BaseStrings.replaceSpecialValues (s);

        return s;

    }

    public static String replaceSpecialValues (String t)
    {

        if (t == null)
        {

            return t;

        }

        t = Utils.replaceString (t,
                                       "{QW}",
                                       Constants.QUOLL_WRITER_NAME);

        return t;

    }

    public Set<Node> getNodes (List<String> idparts,
                               Filter<Node> filter)
    {

        Set<Node> vals = new LinkedHashSet<> ();

        if (idparts.size () < 1)
        {

            return vals;

        }

        Node n = this.nodes.get (idparts.get (0));

        if (n == null)
        {

            return vals;

        }

        if (idparts.size () > 1)
        {

            return n.getNodes (idparts.subList (1, idparts.size ()),
                               filter);

        }

        return n.getNodes (filter);

    }

    public Set<Node> getNodes (List<String> idParts)
    {

        return this.getNodes (idParts,
                              null);

    }

    public Set<Node> getNodes (Filter<Node> filter)
    {

        Set<Node> vals = new LinkedHashSet<> ();

        for (Node n : this.nodes.values ())
        {

            vals.addAll (n.getNodes (filter));

        }

        return vals;

    }

    public Set<Value> getAllValues (List<String>  idparts,
                                    Filter<Value> filter)
    {

        Set<Value> vals = new LinkedHashSet<> ();

        if (idparts.size () < 1)
        {

            return vals;

        }

        Node n = this.nodes.get (idparts.get (0));

        if (n == null)
        {

            return vals;

        }

        if (idparts.size () > 1)
        {

            return n.getAllValues (idparts.subList (1, idparts.size ()),
                                   filter);

        }

        return n.getAllValues (filter);

    }

    public Set<Value> getAllValues (Filter<Value> filter)
    {

        Set<Value> vals = new LinkedHashSet<> ();

        for (Node n : this.nodes.values ())
        {

            vals.addAll (n.getAllValues (filter));

        }

        return vals;

    }

    public Set<TextValue> getAllTextValues ()
    {

        return this.getAllTextValues ((Filter<TextValue>) null);

    }

    public Set<TextValue> getAllTextValues (Filter<TextValue> filter)
    {

        Set<TextValue> vals = new LinkedHashSet<> ();

        for (Node n : this.nodes.values ())
        {

            vals.addAll (n.getAllTextValues (filter));

        }

        return vals;

    }

    public Set<ImageValue> getAllImageValues ()
    {

        return this.getAllImageValues ((Filter<ImageValue>) null);

    }

    public Set<ImageValue> getAllImageValues (Filter<ImageValue> filter)
    {

        Set<ImageValue> vals = new LinkedHashSet<> ();

        for (Node n : this.nodes.values ())
        {

            vals.addAll (n.getAllImageValues (filter));

        }

        return vals;

    }

    public Map<Value, Set<String>> getErrors ()
    {

        Map<Value, Set<String>> ret = new LinkedHashMap<> ();

        for (Value v : this.getAllValues ())
        {

            Set<String> errors = v.getErrors (this);
                                              //(this.parent != null ? this.parent.getValue (v.getId ()) : null));

            if ((errors != null)
                &&
                (errors.size () > 0)
               )
            {

                ret.put (v,
                         errors);

            }

        }

        return ret;

    }

    public Map<Value, Set<String>> getErrors (String id)
    {

        Map<Value, Set<String>> ret = new LinkedHashMap<> ();

        for (Value v : this.getAllValues (id))
        {

            Set<String> errors = v.getErrors (this);
                                              //(this.parent != null ? this.parent.getValue (v.getId ()) : null));

            if ((errors != null)
                &&
                (errors.size () > 0)
               )
            {

                ret.put (v,
                         errors);

            }

        }

        return ret;

    }

    private String getString (String id,
                              Map    from)
    {

        Object o = from.get (id);

        if (o == null)
        {

            return null;

        }

        return o.toString ();

    }

    public Map<String, Set<Node>> getNodesInSections (String defSection)
    {

        Map<String, Set<Node>> sects = new LinkedHashMap<> ();

        for (Node n : this.nodes.values ())
        {

            String s = n.getSection ();

            if (s == null)
            {

                s = defSection;

            }

            Set<Node> nns = sects.get (s);

            if (nns == null)
            {

                nns = new TreeSet<> ();

                sects.put (s,
                           nns);

            }

            nns.add (n);

        }

        return sects;

    }

    public Map<String, Node> cloneNodes ()
    {

        Map<String, Node> ret = new LinkedHashMap<> ();

        for (String id : this.nodes.keySet ())
        {

            ret.put (id,
                     this.nodes.get (id).cloneNode ());

        }

        return ret;

    }

    public boolean containsId (String id)
    {

        return this.containsId (BaseStrings.getIdParts (id));

    }

    public boolean containsId (List<String> idparts)
    {

        if (idparts.size () < 1)
        {

            return false;

        }

        Node n = this.nodes.get (idparts.get (0));

        if (n != null)
        {

            return n.getChild (idparts.subList (1, idparts.size ())) != null;

        }

        return false;

    }

    public Set<Value> getAllValues ()
    {

        Set<Value> vals = new LinkedHashSet<> ();

        for (Node n : this.nodes.values ())
        {

            vals.addAll (n.getAllValues ());

        }

        return vals;

    }

    public Set<Value> getAllValues (String id)
    {

        List<String> idparts = BaseStrings.getIdParts (id);

        Set<Value> vals = new LinkedHashSet<> ();

        if (idparts.size () < 1)
        {

            return vals;

        }

        Node n = this.nodes.get (idparts.get (0));

        if (idparts.size () > 1)
        {

            return n.getAllValues (idparts.subList (1, idparts.size ()));

        }

        if (n == null)
        {

            return vals;

        }

        return n.getAllValues ();

    }

    public Set<TextValue> getAllTextValues (String id)
    {

        List<String> idparts = BaseStrings.getIdParts (id);

        return this.getAllTextValues (idparts);

    }

    public Set<TextValue> getAllTextValues (List<String> idparts)
    {

        Set<TextValue> vals = new LinkedHashSet<> ();

        if (idparts.size () < 1)
        {

            return vals;

        }

        Node n = this.nodes.get (idparts.get (0));

        if (idparts.size () > 1)
        {

            return n.getAllTextValues (idparts.subList (1, idparts.size ()));

        }

        if (n == null)
        {

            return vals;

        }

        return n.getAllTextValues ();

    }

    public static String toId (List<String> ids)
    {

        return BaseStrings.joinStrings (ids,
                                        ID_PART_SEP);

    }

    public static List<String> getIdParts (String id)
    {

        return BaseStrings.splitString (id,
                                        ID_PART_SEP);

    }

    public TreeSet<String> getIdMatches (String id)
    {

        if (id.endsWith ("."))
        {

            id += "*";

        }

        List<String> idparts = BaseStrings.getIdParts (id);

        TreeSet<String> matches = new TreeSet<> ();

        if (idparts.size () > 1)
        {

            // Get the first node.
            Node n = this.nodes.get (idparts.get (0));

            if (n == null)
            {

                return matches;

            }

            return n.getIdMatches (idparts.subList (1, idparts.size ()));

        } else {

            for (String nid : this.nodes.keySet ())
            {

                if (nid.startsWith (id))
                {

                    matches.add (nid);

                }

            }

            return matches;

        }

    }

    public Node getNode (String id)
    {

        return this.getNode (BaseStrings.getIdParts (id));

    }

    public Node getNode (List<String> idparts)
    {

        Node n = null;

        if (idparts.size () > 0)
        {

            n = this.nodes.get (idparts.get (0));

        }

        if ((idparts.size () > 1)
            &&
            (n != null)
           )
        {

            n = n.getChild (idparts.subList (1, idparts.size ()));

        }

        if ((n == null)
            &&
            (this.parent != null)
           )
        {

            n = this.parent.getNode (idparts);

        }

        return n;

    }

    public static boolean isSpecialId (List<String> id)
    {

        for (String _id : id)
        {

            if (BaseStrings.isSpecialId (_id))
            {

                return true;

            }

        }

        return false;

    }

    public static boolean isSpecialId (String id)
    {

        return id.startsWith (":");

    }

    public Value getValue (List<String> idparts,
                           boolean      thisOnly)
    {

        if (idparts.size () < 1)
        {

            return null;

        }

        if (!this.containsId (idparts))
        {

            if ((this.parent != null)
                &&
                (!thisOnly)
               )
            {

                return this.parent.getValue (idparts,
                                             false);

            }

            return null;

        }

        Node n = this.nodes.get (idparts.get (0));

        if (n == null)
        {

            return null;

        }

        if (idparts.size () > 1)
        {

            n = n.getChild (idparts.subList (1, idparts.size ()));

        }

        if (n instanceof Value)
        {

            return (Value) n;

        }

        return null;

    }

    public TextValue getTextValue (List<String> idparts,
                                   boolean      thisOnly)
    {

        Value v = this.getValue (idparts,
                                 thisOnly);

        if (v instanceof TextValue)
        {

            return (TextValue) v;

        }

        return null;

    }

    public TextValue insertTextValue (String id)
                               throws GeneralException
    {

        if ((id == null)
            ||
            (id.trim ().length () == 0)
           )
        {

            throw new IllegalArgumentException ("No id provided.");

        }

        List<String> idparts = this.getIdParts (id);

        if (idparts.size () == 0)
        {

            throw new IllegalArgumentException ("No id provided.");

        }

        String f = idparts.get (0);

        // See if we already have the first node.
        Node n = this.nodes.get (f);

        if (idparts.size () == 1)
        {

            if (n != null)
            {

                throw new GeneralException ("Already have a node called: " + f);

            } else {

                TextValue v = new TextValue (f,
                                             null,
                                             null,
                                             null,
                                             0);

                this.nodes.put (f, v);

            }

        } else {

            if (n == null)
            {

                n = new Node (f,
                              null);

                this.nodes.put (f, n);

            }

        }

        return n.insertTextValue (idparts.subList (1, idparts.size ()));

    }

    public String getBuiltText (String text)
    {

        return new TextValue (null, null, text, null, 0).getBuiltText (this);

    }

    public TextValue getTextValue (List<String> idparts)
    {

        return this.getTextValue (idparts,
                                  false);

    }

    public TextValue getTextValue (String id)
    {

        return this.getTextValue (BaseStrings.getIdParts (id),
                                  false);

    }

    public boolean isIdValid (String id)
    {

        return this.getNode (id) != null;

    }

    public String getRawText (List<String> idparts)
    {

        if (idparts.size () < 1)
        {

            return null;

        }

        TextValue v = this.getTextValue (idparts);

        if (v != null)
        {

            return v.getRawText ();

        }

        return null;

    }

    public String getRawText (String id)
    {

        return this.getRawText (BaseStrings.getIdParts (id));

    }

    public String getString (String id)
    {

        return this.getString (BaseStrings.getIdParts (id));

    }

    public String getString (List<String> idparts)
    {

        if (idparts.size () < 1)
        {

            return null;

        }

        TextValue v = this.getTextValue (idparts);

        if (v != null)
        {

            return v.getBuiltText (this);

        }

        return null;

    }

    /**
     * Return a list of values that this set contains but the old does not or if the old
     * has the value then is the raw text value different.
     *
     * @return The set of new or raw text different values.
     */
     public Set<Value> diff (BaseStrings old)
     {

         Set<Value> ret = new LinkedHashSet<> ();

         Set<Value> allVals = this.getAllValues ();

         for (Value v : allVals)
         {

             Value ov = old.getValue (v.getId (),
                                      true);

             if (ov == null)
             {

                 // This is a new value.
                 ret.add (v);

             } else {

                 if (v.isDifferent (ov))
                 {

                     ret.add (v);

                 }
 /*
                 if (!v.getRawText ().equals (ov.getRawText ()))
                 {

                     ret.add (v);

                 }
 */
             }

         }

         return ret;

     }
/*
    public class Node implements Comparable<Node>
    {

        protected Node parent = null;
        private Map<String, Node> children = null;
        protected String id = null;
        protected String comment = null;
        protected String section = null;
        protected String title = null;

        public Node (String id,
                     Node   parent)
        {

            this.id = id;
            this.parent = parent;

        }

        public Node (String id,
                     Node   parent,
                     Map    data)
        {

            this (id,
                  parent);

            Iterator iter = data.keySet ().iterator ();

            while (iter.hasNext ())
            {

                Object ko = iter.next ();

                if (!(ko instanceof String))
                {

                    continue;

                }

                String kid = ko.toString ();

                Object o = data.get (kid);

                if (o instanceof Map)
                {

                    Map m = (Map) o;

                    if (this.children == null)
                    {

                        this.children = new LinkedHashMap<> ();

                    }

                    this.children.put (kid,
                                       new Node (kid,
                                                 this,
                                                 m));

                }

                if (o instanceof String)
                {

                    String v = o.toString ();

                    if (BaseStrings.isSpecialId (kid))
                    {

                        if (kid.equals (":comment"))
                        {

                            this.comment = v;

                        }

                        if (kid.equals (":title"))
                        {

                            this.title = v;

                        }

                        if (kid.equals (":section"))
                        {

                            this.section = v;

                        }

                        continue;

                    }

                    if (this.children == null)
                    {

                        this.children = new LinkedHashMap<> ();

                    }

                    this.children.put (kid,
                                       new Value (kid,
                                                  this,
                                                  v,
                                                  data));

                }

            }

        }

        public Value insertValue (List<String> idparts)
                           throws GeneralException
        {

            if ((idparts == null)
                ||
                (idparts.size () == 0)
               )
            {

                throw new IllegalArgumentException ("No id provided.");

            }

            String f = idparts.get (0);

            Node n = this.getChild (f);

            if (idparts.size () == 1)
            {

                if (n == null)
                {

                    Value v = new Value (f,
                                         null,
                                         null,
                                         null,
                                         0);

                    this.addNode (v);

                    return v;

                } else {

                    throw new GeneralException ("Already have value: " + n + " with id: " + f);

                }

            }

            if (n == null)
            {

                n = new Node (f,
                              null);

                this.addNode (n);

            }

            return n.insertValue (idparts.subList (1, idparts.size ()));

        }

        public Map getAsJSON ()
        {

            Map m = new LinkedHashMap ();

            if (this.comment != null)
            {

                m.put (":comment",
                       this.comment);

            }

            if (this.title != null)
            {

                m.put (":title",
                       this.title);

            }

            if (this.section != null)
            {

                m.put (":section",
                       this.section);

            }

            if (this.children != null)
            {

                for (String idPrefix : this.children.keySet ())
                {

                    Node n = this.children.get (idPrefix);

                    if (n instanceof Value)
                    {

                        Value v = (Value) n;

                        m.put (idPrefix,
                               v.getRawText ());

                        if (v.getComment () != null)
                        {

                            m.put (":comment." + idPrefix,
                                   v.getComment ());

                        }

                        if (v.getSCount () > 0)
                        {

                            m.put (":scount." + idPrefix,
                                   v.getSCount ());

                        }

                        continue;

                    }

                    m.put (idPrefix,
                           n.getAsJSON ());

                }

            }

            return m;

        }

        @Override
        public int compareTo (Node n)
        {

            return this.id.compareTo (n.id);

        }

        public void addNode (Node n)
        {

            if (this.children == null)
            {

                this.children = new LinkedHashMap<> ();

            }

            if (this.children.containsKey (n.id))
            {

                throw new IllegalArgumentException ("Node already contains child with id: " + n.id);

            }

            n.parent = this;

            this.children.put (n.id,
                               n);

        }

        public String getSection ()
        {

            return this.section;

        }

        public String getTitle ()
        {

            return this.title;

        }

        public Node cloneNode ()
        {

            Node n = new Node (this.id,
                               null);

            n.comment = this.comment;
            n.title = this.title;
            n.section = this.section;

            if (this.children != null)
            {

                for (String cid : this.children.keySet ())
                {

                    Node cn = this.children.get (cid);

                    Node nn = cn.cloneNode ();

                    n.addNode (nn);

                }

            }

            return n;

        }

        public String getComment ()
        {

            return this.comment;

        }

        @Override
        public String toString ()
        {

            return (this.getId () + "(node,children=" + (this.children != null ? this.children.size () : 0));

        }

        public Map<String, Node> getChildren ()
        {

            return this.children;

        }

        public Node getParent ()
        {

            return this.parent;

        }

        public Node getChild (String id)
        {

            if (this.children == null)
            {

                return null;

            }

            return this.children.get (id);

        }

        public Node getChild (List<String> ids)
        {

            if (ids.size () < 1)
            {

                return null;

            }

            Node c = this.getChild (ids.get (0));

            if (c == null)
            {

                return null;

            }

            if (ids.size () > 1)
            {

                return c.getChild (ids.subList (1, ids.size ()));

            }

            return c;

        }

        public String getNodeId ()
        {

            return this.id;

        }

        public List<String> getId ()
        {

            if (this.parent == null)
            {

                List<String> r = new ArrayList<> ();
                r.add (this.id);

                return r;

            }

            List<String> r = this.parent.getId ();

            r.add (this.id);

            return r;

        }

        public TreeSet<String> getIdMatches (List<String> idparts)
        {

            TreeSet<String> matches = new TreeSet<> ();

            if (this.children == null)
            {

                return matches;

            }

            if (idparts.size () > 1)
            {

                Node n = this.children.get (idparts.get (0));

                if (n == null)
                {

                    return matches;

                }

                return n.getIdMatches (idparts.subList (1, idparts.size ()));

            }

            // Should only be one left here...
            String id = idparts.get (0);

            for (String nid : this.children.keySet ())
            {

                if ((nid.startsWith (id))
                    ||
                    (id.equals ("*"))
                   )
                {

                    matches.add (nid);

                }

            }

            return matches;

        }

        public Set<Value> getAllValues (List<String> idparts)
        {

            if (this.children == null)
            {

                return new LinkedHashSet<> ();

            }

            if (idparts.size () > 0)
            {

                Node n = this.children.get (idparts.get (0));

                if (n == null)
                {

                    return new LinkedHashSet<> ();

                }

                return n.getAllValues (idparts.subList (1, idparts.size ()));

            } else {

                return this.getAllValues ();

            }

        }

        public Set<Value> getAllValues ()
        {

            Set<Value> vals = new LinkedHashSet<> ();

            if (this instanceof Value)
            {

                vals.add ((Value) this);

                return vals;

            }

            if (this.children != null)
            {

                for (Node n : this.children.values ())
                {

                    if (n instanceof Value)
                    {

                        vals.add ((Value) n);
                        continue;

                    }

                    vals.addAll (n.getAllValues ());

                }

            }

            return vals;

        }

        @Override
        public boolean equals (Object o)
        {

            if (!(o instanceof Node))
            {

                return false;

            }

            Node n = (Node) o;

            if ((this.parent != null)
                &&
                (n.parent != null)
               )
            {

                if (this.parent.equals (n.parent))
                {

                    return this.id.equals (n.id);

                }

            }

            if ((this.parent == null)
                &&
                (n.parent == null)
               )
            {

                return this.id.equals (n.id);

            }

            return false;

        }

    }
*/
/*
    public class Value extends Node
    {

        private int scount = 0;
        private String text = null;
        private String builtText = null;

        public Value (String id,
                      Node   parent,
                      String text,
                      Map    parentData)
        {

            super (id,
                   parent);

            this.text = text;

            Object co = parentData.get (":comment." + id);

            String comm = null;

            if (co != null)
            {

               this.comment = co.toString ();

            }

            Object so = parentData.get (":scount." + id);

            if ((so != null)
                &&
                (so instanceof Number)
               )
            {

               this.scount = ((Number) so).intValue ();

            }

        }

        public Value (String id,
                      Node   parent,
                      String text,
                      String comment,
                      int    scount)
        {

            super (id,
                   parent);

            this.text = text;
            this.comment = comment;
            this.scount = scount;

        }

        @Override
        public Node cloneNode ()
        {

            Value n = new Value (this.id,
                                 null,
                                 null,
                                 this.comment,
                                 this.scount);

            n.title = this.title;
            n.section = this.section;

            return n;

        }

        public int getSCount ()
        {

            return this.scount;

        }

        @Override
        public String toString ()
        {

            return (this.getId () + "(value,scount=" + this.scount + ",text=" + this.text + ",comment=" + this.comment + ")");

        }

        @Override
        public Set<Value> getAllValues ()
        {

            return new LinkedHashSet<> ();

        }

        @Override
        public Node getChild (List<String> idparts)
        {

            return null;

        }

        public Set<String> getErrors (BaseStrings strings,
                                      Value       baseValue)
        {

            return BaseStrings.getErrors (this.text,
                                              BaseStrings.toId (this.getId ()),
                                              //this.getId (),
                                              (baseValue != null ? baseValue.getSCount () : this.scount),
                                              strings);

        }

        public void clearBuiltText ()
        {

            this.builtText = null;

        }

        public void setRawText (String t)
        {

            this.text = t;
            this.clearBuiltText ();

        }

        public String getRawText ()
        {

            return this.text;

        }

        public String getBuiltText (BaseStrings strings)
        {

            if (this.builtText != null)
            {

                return this.builtText;

            }

            String s = BaseStrings.buildText (this.text,
                                              strings);

            this.builtText = s;

            return this.builtText;

        }
    }
*/
    public static List<String> splitString (String str,
                                            String separator)
    {

        List<String> ret = new ArrayList ();

        if (str == null)
        {

            return ret;

        }

        StringTokenizer t = new StringTokenizer (str,
                                                 separator);

        while (t.hasMoreTokens ())
        {

            ret.add (t.nextToken ());

        }

        return ret;

    }

    public static String joinStrings (Collection<String> items,
                                      String             separator)
    {

        if ((items == null)
            ||
            (items.size () == 0)
           )
        {

            return null;

        }

        StringBuilder b = new StringBuilder ();

        Iterator<String> iter = items.iterator ();

        while (iter.hasNext ())
        {

            b.append (iter.next ());

            if (iter.hasNext ())
            {

                b.append (separator != null ? separator : ", ");

            }

        }

        return b.toString ();

    }
/*
    public static class Id
    {

        private int start = -1;
        private boolean partial = false;
        private String id = null;
        private List<Part> parts = null;

        public Id (int     start,
                   String  id,
                   boolean partial)
        {

            this.start = start;
            this.id = id;
            this.partial = partial;

            java.util.List<String> parts = Utilities.splitString (this.id,
                                                                  ID_PART_SEP);

            this.parts = new ArrayList<> ();

            int cind = start;

            Part prevp = null;

            for (int i = 0; i < parts.size (); i++)
            {

                if (i > 0)
                {

                    cind++;

                }

                String ps = parts.get (i);

                if (ps.trim ().length () != ps.length ())
                {

                    //this.hasErrors = true;

                }

                Part p = new Part (this,
                                   cind,
                                   ps,
                                   prevp);

                prevp = p;
                cind += ps.length ();

                this.parts.add (p);

            }

        }

        public String getId ()
        {

            return this.id;

        }

        public boolean isIdValid (BaseStrings baseStrings)
        {

            return baseStrings.isIdValid (this.id);

        }

        public boolean isPartial ()
        {

            return this.partial;

        }

        public int getStart ()
        {

            return this.start;

        }

        public int getEnd ()
        {

            return this.getStart () + this.id.length ();

        }

        public Part getLastPart ()
        {

            if (this.parts.size () == 0)
            {

                return null;

            }

            return this.parts.get (this.parts.size () - 1);

        }

        public Set<String> getPartMatches (int             offset,
                                           BaseStrings baseStrings)
        {

            Part p = this.getPart (offset);

            if (p != null)
            {

                return baseStrings.getIdMatches (p.getFullId ());

            }

            return this.getMatches (baseStrings);

        }

        public String toString ()
        {

            return "id[start=" + this.start + ",text=" + this.id + ",partial=" + this.partial + "]";

        }

        public Set<String> getMatches (BaseStrings baseStrings)
        {

            return baseStrings.getIdMatches (this.id);

        }

        public Part getPart (int offset)
        {

            for (int i = 0; i < this.parts.size (); i++)
            {

                Part p = this.parts.get (i);

                if ((offset >= p.start)
                    &&
                    (offset <= p.end)
                   )
                {

                    return p;

                }

            }

            return null;

        }

        public class Part
        {

            public int start = -1;
            public int end = -1;
            public String part = null;
            public Id parent = null;
            public Part previous = null;

            public Part (Id     parent,
                         int    start,
                         String part,
                         Part   prev)
            {

                this.start = start;
                this.end = this.start + part.length ();
                this.parent = parent;
                this.part = part;
                this.previous = prev;

            }

            public String getFullId ()
            {

                StringBuilder b = new StringBuilder (this.part);

                Part prev = this.previous;

                while (prev != null)
                {

                    b.insert (0, prev.part + ".");
                    prev = prev.previous;

                }

                return b.toString ();

            }

        }

    }
*/
}
