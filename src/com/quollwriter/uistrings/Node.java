package com.quollwriter.uistrings;

import java.util.*;

import com.quollwriter.*;

public class Node<E extends Value> implements Comparable<Node>
{

    protected Node parent = null;
    private Map<String, Node> children = null;
    protected String id = null;
    protected String comment = null;
    protected String section = null;
    private String title = null;
    private Set<ImageValue> imgs = null;
    private Set<String> toplevelNodes = null;

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

        if (data == null)
        {

            return;

        }

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

            if (o instanceof Collection)
            {

                Collection c = (Collection) o;

                if (kid.equals (":toplevelnodes"))
                {

                    this.toplevelNodes = new LinkedHashSet<> ();

                    for (Object v : c)
                    {

                        this.toplevelNodes.add (v.toString ());

                    }

                }

            }

            if (o instanceof Map)
            {

                Map m = (Map) o;
/*
                if (kid.equals (":imgs"))
                {

                    this.imgs = new LinkedHashSet<> ();

                    for (Object mk : m.keySet ())
                    {

                        Map mm = (Map) m.get (mk);

                        String mkk = mk.toString ();

                        if (this.children == null)
                        {

                            this.children = new LinkedHashMap<> ();

                        }

                        this.children.put (mkk,
                                           new ImageValue (mkk,
                                                           this,
                                                           mm));

                    }

                }
*/
                if (this.children == null)
                {

                    this.children = new LinkedHashMap<> ();

                }

                String type = (String) m.get (":type");

                if (type != null)
                {

                    if (type.equals ("img"))
                    {

                        ImageValue img = new ImageValue (kid,
                                                         this,
                                                         m);

                        this.children.put (kid,
                                           img);

                        continue;

                    }

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
                                   new TextValue (kid,
                                                  this,
                                                  v,
                                                  data));

            }

        }

    }

    public Set<String> getTopLevelNodes ()
    {

        return this.toplevelNodes;

    }

    public Node getRoot ()
    {

        if (this.parent == null)
        {

            return this;

        }

        return this.parent.getRoot ();

    }

    public Set<Node> getNodes (List<String>  idparts,
                               Filter<Node> filter)
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

            return n.getNodes (idparts.subList (1, idparts.size ()),
                                  filter);

        } else {

            return this.getNodes (filter);

        }

    }

    public Set<Node> getAllNodes ()
    {

        return this.getNodes (null);

    }

    public Set<Value> getValues (Filter<Node> filter)
    {

        Set<Value> ret = new LinkedHashSet<> ();

        if (filter != null)
        {

            if (!filter.accept (this))
            {

                return ret;

            }

        }

        if (this instanceof Value)
        {

            ret.add ((Value) this);

        }

        if (this.children != null)
        {

            for (Node n : this.children.values ())
            {

                ret.addAll (n.getValues (filter));

            }

        }

        return ret;

    }

    public Set<Node> getNodes (Filter<Node> filter)
    {

        Set<Node> ret = new LinkedHashSet<> ();

        if (filter != null)
        {

            if (!filter.accept (this))
            {

                return ret;

            }

        }

        ret.add (this);

        if (this.children != null)
        {

            for (Node n : this.children.values ())
            {

                ret.addAll (n.getNodes (filter));

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

        Node n = this.getChild (f);

        if (idparts.size () == 1)
        {

            if (n == null)
            {

                TextValue v = new TextValue (f,
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

        return n.insertTextValue (idparts.subList (1, idparts.size ()));

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

        Node n = this.getChild (f);

        if (idparts.size () == 1)
        {

            if (n == null)
            {

                ImageValue v = new ImageValue (f,
                                               n,
                                               null);

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

        return n.insertImageValue (idparts.subList (1, idparts.size ()));

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

        Node n = this.getChild (f);

        if (n == null)
        {

            return null;

        }

        if (idparts.size () == 1)
        {

            this.children.remove (idparts.get (0));

            return n;

        }

        return n.removeNode (idparts.subList (1, idparts.size ()));

    }

    public Map getAsJSON ()
    {

        Map m = new LinkedHashMap ();

        if (this.toplevelNodes != null)
        {

            m.put (":toplevelnodes",
                   this.toplevelNodes);

        }

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

                    m.putAll (v.getAsJSON ());
/*
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
*/
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

        return (this.getId () + "(node,:section=" + this.section + ",:title=" + this.title + ",:comment=" + this.comment + ",children=" + (this.children != null ? this.children.size () : 0) + ")");

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

    public Set<TextValue> getAllTextValues (List<String> idparts)
    {

        return this.getAllTextValues (idparts,
                                      null);

    }

    public Set<TextValue> getAllTextValues (List<String>      idparts,
                                            Filter<TextValue> filter)
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

            return n.getAllTextValues (idparts.subList (1, idparts.size ()),
                                       filter);

        } else {

            return this.getAllTextValues (filter);

        }

    }

    public Set<ImageValue> getAllImageValues (List<String> idparts)
    {

        return this.getAllImageValues (idparts,
                                       null);

    }

    public Set<ImageValue> getAllImageValues (List<String>       idparts,
                                              Filter<ImageValue> filter)
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

            return n.getAllImageValues (idparts.subList (1, idparts.size ()),
                                        filter);

        } else {

            return this.getAllImageValues (filter);

        }

    }

    public Set<ImageValue> getAllImageValues (Filter<ImageValue> filter)
    {

        Set<ImageValue> vals = new LinkedHashSet<> ();

        if (this instanceof ImageValue)
        {

            ImageValue v = (ImageValue) this;

            if (filter != null)
            {

                if (!filter.accept (v))
                {

                    return vals;

                }

            }

            vals.add (v);

            return vals;

        }

        if (this.children != null)
        {

            for (Node n : this.children.values ())
            {

                if (n instanceof ImageValue)
                {

                    ImageValue v = (ImageValue) n;

                    if (filter != null)
                    {

                        if (!filter.accept (v))
                        {

                            continue;

                        }

                    }

                    vals.add (v);

                    continue;

                }

                vals.addAll (n.getAllImageValues (filter));

            }

        }

        return vals;

    }

    public Set<Value> getAllValues (List<String> idparts)
    {

        return this.getAllValues (idparts,
                                  null);

    }

    public Set<Value> getAllValues (List<String>  idparts,
                                    Filter<Value> filter)
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

            return n.getAllValues (idparts.subList (1, idparts.size ()),
                                   filter);

        } else {

            return this.getAllValues (filter);

        }

    }

    public Set<Value> getAllValues ()
    {

        return this.getAllValues ((Filter) null);

    }

    public Set<TextValue> getAllTextValues ()
    {

        return this.getAllTextValues ((Filter) null);

    }

    public Set<Value> getAllValues (Filter<Value> filter)
    {

        Set<Value> vals = new LinkedHashSet<> ();

        if (this instanceof Value)
        {

            Value v = (Value) this;

            if (filter != null)
            {

                if (!filter.accept (v))
                {

                    return vals;

                }

            }

            vals.add (v);

            return vals;

        }

        if (this.children != null)
        {

            for (Node n : this.children.values ())
            {

                if (n instanceof Value)
                {

                    Value v = (Value) n;

                    if (filter != null)
                    {

                        if (!filter.accept (v))
                        {

                            continue;

                        }

                    }

                    vals.add (v);

                    continue;

                }

                vals.addAll (n.getAllValues (filter));

            }

        }

        return vals;

    }

    public Set<TextValue> getAllTextValues (Filter<TextValue> filter)
    {

        Set<TextValue> vals = new LinkedHashSet<> ();

        if (this instanceof TextValue)
        {

            TextValue v = (TextValue) this;

            if (filter != null)
            {

                if (!filter.accept (v))
                {

                    return vals;

                }

            }

            vals.add (v);

            return vals;

        }

        if (this.children != null)
        {

            for (Node n : this.children.values ())
            {

                if (n instanceof TextValue)
                {

                    TextValue v = (TextValue) n;

                    if (filter != null)
                    {

                        if (!filter.accept (v))
                        {

                            continue;

                        }

                    }

                    vals.add (v);

                    continue;

                }

                vals.addAll (n.getAllTextValues (filter));

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

    public boolean isDifferent (E v)
    {

        if ((this.comment != null)
            &&
            (v.getComment () == null)
           )
        {

            return true;

        }

        if ((this.comment == null)
            &&
            (v.getComment () != null)
           )
        {

            return true;

        }

        return !this.equals (v);

    }

}
