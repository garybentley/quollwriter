package com.quollwriter;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

import org.jdom.*;

import com.gentlyweb.utils.*;

import com.quollwriter.data.*;

public class LanguageStrings extends NamedObject implements RefValueProvider, Comparable<LanguageStrings>
{

    public static final String ENGLISH_ID = ":" + Constants.ENGLISH;
    public static final String OBJECT_TYPE = "languagestrings";

    public static String ID_PART_SEP = ".";
    public static String ID_REF_START = "${";
    public static String ID_REF_END = "}";

    private String languageName = null;
    private int stringsVersion = 0;
    private Date created = null;
    private Date lastModified = null;
    private String _email = null;
    private LanguageStrings parent = null;
    private Map<String, Node> nodes = new HashMap<> ();
    private Version qwVersion = null;
    private Set<Section> sections = null;
    private boolean isUser = false;

    private LanguageStrings ()
    {

        super (OBJECT_TYPE,
               null);

        this.qwVersion = Environment.getQuollWriterVersion ();

    }

    public LanguageStrings (LanguageStrings derivedFrom)
    {

        this ();

        // Clone the nodes in the derivedFrom.
        //this.nodes = derivedFrom.cloneNodes ();
        this.parent = derivedFrom;
        this.setId (UUID.randomUUID ().toString ());
        this.created = new Date ();

    }

    public LanguageStrings (File f)
                     throws GeneralException
    {

        this ();

        if ((f == null)
            ||
            (!f.exists ())
            ||
            (!f.isFile ())
           )
        {

            throw new IllegalArgumentException ("No file provided.");

        }

        String v = null;

        try
        {

            Reader in = new BufferedReader (new InputStreamReader (new FileInputStream (f),
                                                                   StandardCharsets.UTF_8));

            long length = f.length ();

            char[] chars = new char[(int) length];

            in.read (chars,
    		         0,
    		         (int) length);

        	in.close ();

            v = new String (chars);

        } catch (Exception e) {

            throw new GeneralException ("Unable to get contents of file: " + f,
                                        e);

        }

        this.init (v);

    }

    public LanguageStrings (String jsonData)
                     throws GeneralException
    {

        this ();

        this.init (jsonData);

    }

    public void setUser (boolean v)
    {

        this.isUser = v;

    }

    public boolean isUser ()
    {

        return this.isUser;

    }

    public void setQuollWriterVersion (Version v)
    {

        this.qwVersion = v;

    }

    public Version getQuollWriterVersion ()
    {

        return this.qwVersion;

    }

    public int getStringsVersion ()
    {

        return this.stringsVersion;

    }

    public void setStringsVersion (int v)
    {

        this.stringsVersion = v;

    }

    public static boolean isEnglish (String id)
    {

        return id.equals (ENGLISH_ID);

    }

    public boolean isEnglish ()
    {

        return isEnglish (this.getId ());

    }

    public Set<Section> getSections ()
    {

        return this.sections;

    }

    @Override
    public boolean equals (Object o)
    {

        if (!(o instanceof LanguageStrings))
        {

            return false;

        }

        LanguageStrings ls = (LanguageStrings) o;

        return this.compareTo (ls) == 0;

    }

    @Override
    public int compareTo (LanguageStrings obj)
    {

        if (obj == null)
        {

            return -1;

        }

        if (this.getId ().equals (obj.getId ()))
        {

            return this.qwVersion.compareTo (obj.qwVersion);

        }

        int v = this.qwVersion.compareTo (obj.qwVersion);

        if (v == 0)
        {

            return this.getName ().compareTo (obj.getName ());

        }

        return v;

    }

    private void init (String jsonData)
                throws GeneralException
    {

        Object obj = JSONDecoder.decode (jsonData);

        if (!(obj instanceof Map))
        {

            throw new IllegalArgumentException ("String does parse to a Map");

        }

        this.init ((Map) obj);

    }

    private void init (Map obj)
                throws GeneralException
    {

        if (obj == null)
        {

            throw new IllegalArgumentException ("No object provided.");

        }

        Map<String, Object> m = (Map<String, Object>) obj;

        this.setId (this.getString (":id",
                                    m));

        String did = this.getString (":derivedfrom",
                                     m);

        if (did != null)
        {

            // Get the parent.
            try
            {

                this.parent = Environment.getUILanguageStrings (did);

            } catch (Exception e) {

                throw new GeneralException ("Unable to find language strings for id: " +
                                            did,
                                            e);

            }

        }

        String qwv = (String) m.get (":qwversion");

        if (qwv == null)
        {

            throw new GeneralException ("Expected to find a QW version.");

        }

        Number sv = (Number) m.get (":version");

        if (sv != null)
        {

            this.stringsVersion = sv.intValue ();

        }

        this.qwVersion = new Version (qwv);

        Number n = (Number) m.get (":created");

        if (n == null)
        {

            throw new GeneralException ("Expected to find a created date.");

        }

        this.created = new Date ();
        this.created.setTime (n.longValue ());

        n = (Number) m.get (":lastmodified");

        if (n != null)
        {

            Date d = new Date ();
            d.setTime (n.longValue ());

            this.lastModified = d;

        }

        Boolean b = (Boolean) m.get (":user");

        if (b != null)
        {

            this.isUser = b.booleanValue ();

        }

        this.languageName = this.getString (":language",
                                            m);

        super.setName (this.getString (":nativename",
                                       m));

        if (this.getName () == null)
        {

            throw new IllegalArgumentException ("No native language name found.");

        }

        this._email = this.getString (":email",
                                      m);

        Collection s = (Collection) m.get (":sections");

        if (s != null)
        {

            this.sections = new LinkedHashSet<> ();

            Iterator iter = ((Collection) s).iterator ();

            while (iter.hasNext ())
            {

                Object o = iter.next ();

                if (o instanceof Map)
                {

                    this.sections.add (new Section ((Map) o));

                }

            }

        }

        // Ensure we can resolve everything.
        Iterator iter = m.keySet ().iterator ();

        while (iter.hasNext ())
        {

            String k = iter.next ().toString ();

            if (LanguageStrings.isSpecialId (k))
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
                                new Value (k,
                                           null,
                                           val,
                                           m));

            }

        }

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new HashSet<> ();

    }

    @Override
    public Date getLastModified ()
    {

        return this.lastModified;

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    /**
     * Returns a data structure that is suitable for JSON encoding.
     *
     * @return The data
     */
    public Map getAsJSON ()
    {

        Map m = new HashMap ();

        m.put (":language",
               this.languageName);
        m.put (":nativename",
               this.getName ());
        m.put (":email",
               this._email);
        m.put (":id",
               this.getId ());
        m.put (":created",
               this.created.getTime ());
        m.put (":qwversion",
               this.qwVersion.getVersion ());
        m.put (":user",
               this.isUser);
        m.put (":version",
               this.stringsVersion);

        if (this.lastModified != null)
        {

            m.put (":lastmodified",
                   this.lastModified.getTime ());

        }

        if (this.parent != null)
        {

            m.put (":derivedfrom",
                   this.parent.getId ());

        }

        if (this.sections != null)
        {

            List<Map<String, String>> sects = new ArrayList<> ();

            m.put (":sections",
                   sects);

            for (Section s : this.sections)
            {

                Map<String, String> ms = new HashMap<> ();

                ms.put ("id",
                        s.id);
                ms.put ("name",
                        s.name);
                ms.put ("icon",
                        s.icon);

                sects.add (ms);

            }

        }

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

    public static Id getId (String text,
                            int    offset)
    {

        Set<Id> ids = LanguageStrings.getIds (text);

        for (Id id : ids)
        {

            if ((id.getStart () <= offset)
                &&
                (id.getEnd () >= offset)
               )
            {

                return id;

            }

        }

        return null;

    }

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

        java.util.List<String> lines = Utils.splitString (text,
                                                          "\n");

        for (String l : lines)
        {

            int start = 0;

            while ((start = l.indexOf (LanguageStrings.ID_REF_START, start)) != -1)
            {

                String id = null;
                boolean partial = false;

                start += LanguageStrings.ID_REF_START.length ();

                int ind = start;

                int idendind = l.indexOf (LanguageStrings.ID_REF_END, start);

                if (idendind > -1)
                {

                    id = l.substring (start, idendind);

                    partial = false;
                    //hasClosingBrace = true;
                    start += id.length ();
                    start += LanguageStrings.ID_REF_END.length ();

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

    public static List<String> buildRefValsTree (String           text,
                                                 String           rootId,
                                                 RefValueProvider prov,
                                                 List<String>         ids)
    {

        if (text == null)
        {

            return null;

        }

        Set<Id> refids = LanguageStrings.getIds (text);

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

            List<String> nids = LanguageStrings.buildRefValsTree (rv,
                                                              rootId,
                                                              prov,
                                                              new ArrayList<> (ids));

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

        List<String> vals = LanguageStrings.buildRefValsTree (text,
                                                              textId,
                                                              prov,
                                                              new ArrayList<String> ());

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

        Set<Id> ids = LanguageStrings.getIds (text);

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

            }

            if (prov.getRawText (id.getId ()) == null)
            {

                errors.add (String.format ("Id: %s, referenced at location: %s does not exist.",
                            id.getId (),
                            id.getStart ()));

            }

        }
/*
        int start = 0;

        while ((start = text.indexOf (ID_REF_START,
                                      start)) > -1)
        {

            int end = text.indexOf (ID_REF_END,
                                    start);

            if (end < 0)
            {

                errors.add (String.format ("No matching %s found for opening %s at location: %s",
                                           ID_REF_END,
                                           ID_REF_START,
                                           start));

                // This breaks parsing so have to return.
                return errors;

            }

            if (end >= (start + ID_REF_START.length ()))
            {

                String sid = text.substring (start + ID_REF_START.length (),
                                             end);

                int bind = sid.indexOf ("|");
                String sub = null;

                if (bind > -1)
                {

                    sub = sid.substring (0, bind);

                    sid = sid.substring (bind + 1);

                }

                sid = sid.trim ();

                if (sid.length () == 0)
                {

                    errors.add ("No id provided at location: " + start);
                    start += end + ID_REF_END.length ();

                    continue;

                }

                if (sid.equals (textId))
                {

                    errors.add ("Id: " + ID_REF_START + sid + ID_REF_END + ", referenced at location: " + start + " refers to itself.");
                    start += end + ID_REF_END.length ();

                    continue;

                }

                if (prov.getRawText (sid) == null)
                {

                    errors.add ("Id: " + ID_REF_START + sid + ID_REF_END + ", referenced at location: " + start + " does not exist.");
                    start += end + ID_REF_END.length ();

                    continue;

                } else {

                    start += ID_REF_START.length ();

                }

            } else {

                start += ID_REF_START.length ();

            }

        }
*/
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
                                    RefValueProvider prov)
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

                String sv = prov.getString (sid);

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

                if (sv != null)
                {

                    b.replace (start,
                               end + ID_REF_END.length (),
                               sv);

                    start += sv.length ();

                } else {

                    start = end + 1;

                }

            } else {

                start += ID_REF_START.length ();

            }

        }

        String s = b.toString ();

        s = LanguageStrings.replaceSpecialValues (s);

        return s;

    }

    public static String replaceSpecialValues (String t)
    {

        if (t == null)
        {

            return t;

        }

        StringBuilder b = new StringBuilder (t);

        int start = b.indexOf ("{");

        while (start > -1)
        {

            int end = b.indexOf ("}",
                                 start);

            if (end > start)
            {

                String ot = b.substring (start + 1,
                                         end);

                String newot = ot.toLowerCase ();

                if (newot.equals ("qw"))
                {

                    newot = Constants.QUOLL_WRITER_NAME;

                }

                b.replace (start,
                           end + 1,
                           newot);

                start += newot.length ();

            } else {

                start++;

            }

            start = b.indexOf ("{",
                               start);

        }

        return b.toString ();

    }

    public Map<Value, Set<String>> getErrors ()
    {

        Map<Value, Set<String>> ret = new LinkedHashMap<> ();

        for (Value v : this.getAllValues ())
        {

            Set<String> errors = v.getErrors (this);

            if ((errors != null)
                &&
                (errors.size () > 0)
               )
            {

                ret.put (v,
                         errors);

            }

            // Special checks for the base strings.
            if ((this.isEnglish ())
                &&
                (!this.isUser ())
               )
            {

                int c = 0;
                boolean invalid = false;

                String rawText = v.getRawText ();

                // This is a cheap check to determine whether there are strings but the scount is wrong.
                for (int i = 0; i < 5; i++)
                {

                    if (rawText.indexOf ("%" + i + "$s") != -1)
                    {

                        c++;

                    }

                    if (rawText.indexOf ("%" + i + "s") != -1)
                    {

                        invalid = true;

                    }

                    if (rawText.indexOf ("$" + i + "$") != -1)
                    {

                        invalid = true;

                    }

                }

                if (rawText.indexOf ("%$") != -1)
                {

                    invalid = true;

                }

                if (invalid)
                {

                    errors.add (String.format ("Invalid %$ or %Xs value found."));

                }

                if (v.getSCount () != c)
                {

                    errors.add (String.format (":scount value is incorrect or not present, expected: %s, scount is %s.",
                                               c,
                                               v.getSCount ()));

                }

                if ((c > 0)
                    &&
                    (v.getComment () == null)
                   )
                {

                    errors.add (String.format ("Value contains one or more %s values but not have an associated comment.",
                                               "%x$s"));

                }

                if ((v.getSCount () > 0)
                    &&
                    (v.getComment () == null)
                   )
                {

                    errors.add ("S count present but no comment provided.");

                }

            }

        }

        return ret;

    }

    public Map<Value, Set<String>> getErrors (List<String> id)
    {

        Map<Value, Set<String>> ret = new LinkedHashMap<> ();

        for (Value v : this.getAllValues (id))
        {

            Set<String> errors = v.getErrors (this);

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

        return this.containsId (LanguageStrings.getIdParts (id));

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

    /**
     * Return a list of values that this set contains but the old does not or if the old
     * has the value then is the raw text value different.
     *
     * @return The set of new or raw text different values.
     */
    public Set<Value> diff (LanguageStrings old)
    {

        Set<Value> ret = new LinkedHashSet<> ();

        Set<Value> allVals = this.getAllValues ();

        for (Value v : allVals)
        {

            Value ov = old.getValue (v.getId ());

            if (ov == null)
            {
System.out.println ("NEW: " + v);
                // This is a new value.
                ret.add (v);

            } else {

                if (!v.getRawText ().equals (ov.getRawText ()))
                {
System.out.println ("CHANGED: " + v);
                    ret.add (v);

                }

            }

        }

        return ret;

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

    public Set<Value> getAllValues ()
    {

        return this.getAllValues ((Filter) null);
/*
        Set<Value> vals = new LinkedHashSet<> ();

        for (Node n : this.nodes.values ())
        {

            vals.addAll (n.getAllValues ());

        }

        return vals;
*/
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

    public Set<Value> getAllValues (List<String> idparts)
    {

        return this.getAllValues (idparts,
                                  null);
/*
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

            return n.getAllValues (idparts.subList (1, idparts.size ()));

        }

        return n.getAllValues ();
*/
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

    public void setEmail (String em)
    {

        this._email = em;

    }

    public String getEmail ()
    {

        return this._email;

    }

    public void setNativeName (String n)
    {

        super.setName (n);

    }

    public String getNativeName ()
    {

        return this.getName ();

    }

    public void setLanguageName (String n)
    {

        this.languageName = n;

    }

    public String getLanguageName ()
    {

        return this.languageName;

    }

    public static String toId (List<String> ids)
    {

        return Utils.joinStrings (ids,
                                  ID_PART_SEP);

    }

    public static List<String> getIdParts (String id)
    {

        return Utils.splitString (id,
                                  ID_PART_SEP);

    }

    public TreeSet<String> getIdMatches (String id)
    {

        if (id.endsWith ("."))
        {

            id += "*";

        }

        List<String> idparts = LanguageStrings.getIdParts (id);

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

        List<String> idparts = LanguageStrings.getIdParts (id);

        return this.getNode (idparts);

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


            return n.getChild (idparts.subList (1, idparts.size ()));

        }

        return n;

    }

    public static boolean isSpecialId (List<String> id)
    {

        for (String _id : id)
        {

            if (LanguageStrings.isSpecialId (_id))
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

    public Value getValue (List<String> idparts)
    {

        return this.getValue (idparts,
                              false);

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

                return this.parent.getValue (idparts);

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

        // See if we already have the first node.
        Node n = this.nodes.get (f);

        if (idparts.size () == 1)
        {

            if (n != null)
            {

                throw new GeneralException ("Already have a node called: " + f);

            } else {

                Value v = new Value (f,
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

        return n.insertValue (idparts.subList (1, idparts.size ()));

    }

    public String getBuiltText (String text)
    {

        return new Value (null, null, text, null, 0).getBuiltText (this);

    }

    public Value getValue (String id)
    {

        return this.getValue (LanguageStrings.getIdParts (id));

    }

    public boolean isIdValid (String id)
    {

        return this.getNode (id) != null;

    }

    @Override
    public String getRawText (String id)
    {

        return this.getRawText (LanguageStrings.getIdParts (id));

    }

    @Override
    public String getString (String id)
    {

        return this.getString (LanguageStrings.getIdParts (id));

    }

    public String getString (List<String> idparts)
    {

        if (idparts.size () < 1)
        {

            return null;

        }

        Value v = this.getValue (idparts);

        if (v != null)
        {

            return v.getBuiltText (this);

        }

        return null;

    }

    public String getRawText (List<String> idparts)
    {

        if (idparts.size () < 1)
        {

            return null;

        }

        Value v = this.getValue (idparts);

        if (v != null)
        {

            return v.getRawText ();

        }

        return null;

    }

    public interface Filter<E extends Node>
    {

        public boolean accept (E n);

    }

    public class Section
    {

        public String id = null;
        public String icon = null;
        public String name = null;

        public Section (Map data)
        {

            Object id = data.get ("id");

            if (id == null)
            {

                throw new IllegalArgumentException ("Expected to find an id.");

            }

            this.id = id.toString ();

            Object icon = data.get ("icon");

            if (icon == null)
            {

                throw new IllegalArgumentException ("Expected to find an icon.");

            }

            this.icon = icon.toString ();

            Object name = data.get ("name");

            if (name == null)
            {

                throw new IllegalArgumentException ("Expected to find a name.");

            }

            this.name = name.toString ();

        }

    }

    public class Node implements Comparable<Node>
    {

        protected Node parent = null;
        private Map<String, Node> children = null;
        protected String id = null;
        protected String comment = null;
        protected String section = null;
        private String title = null;
        private String titlex = null;

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

                    if (LanguageStrings.isSpecialId (kid))
                    {

                        if (kid.equals (":comment"))
                        {

                            this.comment = v;

                        }

                        if (kid.equals (":title"))
                        {

                            this.title = v;

                        }

                        if (kid.equals (":titlex"))
                        {

                            this.titlex = v;

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
                                 null,  /* no value */
                                 this.comment,
                                 this.scount);

            //n.title = this.title;
            //n.section = this.section;

            return n;

        }

        public void setSCount (int s)
        {

            this.scount = s;

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

        public Set<String> getErrors (RefValueProvider prov)
        {

            return LanguageStrings.getErrors (this.text,
                                              LanguageStrings.toId (this.getId ()),
                                              this.scount,
                                              prov);

/*
            Set<String> errors = new LinkedHashSet<> ();

            int start = 0;

            while ((start = this.text.indexOf (ID_REF_START,
                                               start)) > -1)
            {

                int end = this.text.indexOf (ID_REF_END,
                                             start);

                if (end < 0)
                {

                    errors.add (String.format ("No matching %s found for opening %s at location: %s",
                                               ID_REF_END,
                                               ID_REF_START,
                                               start));

                    // This breaks parsing so have to return.
                    return errors;

                }

                if (end > (start + ID_REF_START.length ()))
                {

                    String sid = this.text.substring (start + ID_REF_START.length (),
                                                      end);

                    int bind = sid.indexOf ("|");
                    String sub = null;

                    if (bind > -1)
                    {

                        sub = sid.substring (0, bind);

                        sid = sid.substring (bind + 1);

                    }

                    if (strings.getValue (sid) == null)
                    {

                        errors.add ("Id: " + sid + ", referenced at location: " + start + " does not exist.");
                        start += end + ID_REF_END.length ();

                        continue;

                    } else {

                        start += ID_REF_START.length ();

                    }

                } else {

                    start += ID_REF_START.length ();

                }

            }

            if (this.scount > 0)
            {

                for (int i = 0; i < this.scount; i++)
                {

                    String sid = "%" + (i + 1) + "$s";

                    if (this.text.indexOf (sid) < 0)
                    {

                        errors.add ("Expected to find value: " + sid);

                    }

                }

            }

            Set<String> refids = this.getRefIds ();

            if (refids.contains (this.getId ()))
            {

                errors.add ("Value references itself O_o");

            }

            Set<Value> refs = new HashSet<> ();
            Map tree = new HashMap ();
            List<Value> vals = this.buildRefValsTree (this,
                                                      strings,
                                                      new ArrayList<Value> ());

            if (vals != null)
            {

                StringBuilder b = new StringBuilder ();

                for (Value v : vals)
                {

                    if (b.length () > 0)
                    {

                        b.append (" -> ");

                    }

                    b.append (v.getId ());

                }

                errors.add ("Reference loop detected between: " + this.getId () + " and: " + b.toString ());

            }

            return errors;
*/
        }
/*
        public List<Value> buildRefValsTree (Value           root,
                                             LanguageStrings strings,
                                             List<Value>     ids)
        {

            Set<String> refids = this.getRefIds ();

            for (String rid : refids)
            {

                Value v = strings.getValue (rid);

                if (rid.equals (this.getId ()))
                {

                    return ids;

                }

                if (v == null)
                {

                    continue;

                }

                if (v.equals (root))
                {

                    return ids;

                }

                if (ids.contains (v))
                {

                    // Already have this, got a loop.
                    return ids;

                }

                ids.add (v);

                int ind = ids.size () - 1;

                List<Value> nids = v.buildRefValsTree (root,
                                                       strings,
                                                       new ArrayList (ids));

                if (nids != null)
                {

                    return nids;

                } else {

                    ids = ids.subList (0, ind);

                }

            }

            return null;

        }
*/
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

        public String getBuiltText (RefValueProvider prov)
        {

            if (this.builtText != null)
            {

                return this.builtText;

            }

            if (this.getErrors (prov).size () > 0)
            {

                return this.text;

            }

            String s = LanguageStrings.buildText (this.text,
                                                  prov);

            this.builtText = s;

            return this.builtText;

        }
/*
        public Set<String> getRefIds ()
        {

            Set<String> ids = new LinkedHashSet<> ();

            int start = 0;

            while ((start = this.text.indexOf (ID_REF_START,
                                               start)) > -1)
            {

                int end = this.text.indexOf (ID_REF_END,
                                             start);

                if (end > (start + ID_REF_START.length ()))
                {

                    String sid = this.text.substring (start + ID_REF_START.length (),
                                                      end);

                    ids.add (sid);
                    start = end + ID_REF_END.length ();

                } else {

                    start += ID_REF_START.length ();

                }

            }

            return ids;

        }
*/
    }

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

            java.util.List<String> parts = Utils.splitString (this.id,
                                                              LanguageStrings.ID_PART_SEP);

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

        public boolean isIdValid (LanguageStrings baseStrings)
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
                                           LanguageStrings baseStrings)
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

        public Set<String> getMatches (LanguageStrings baseStrings)
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

    public static final String project = "project";
    public static final String settingsmenu = "settingsmenu";
    public static final String items = "items";

    public static final String renameproject = "renameproject";
    public static final String statistics = "statistics";
    public static final String targets = "targets";
    public static final String createbackup = "createbackup";
    public static final String openproject = "openproject";
    public static final String newproject = "newproject";
    public static final String deleteproject = "deleteproject";
    public static final String closeproject = "closeproject";
    public static final String exportproject = "exportproject";
    public static final String ideaboard = "ideaboard";
    public static final String dowarmup = "dowarmup";

    public static final String actionerror = "actionerror";
    public static final String label = "label";
    public static final String filetype = "filetype";
    public static final String stages = "stages";
    public static final String wheretosave = "wheretosave";
    public static final String finder = "finder";
    public static final String tooltip = "tooltip";
    public static final String button = "button";
    public static final String title = "title";
    public static final String text = "text";
    public static final String help = "help";
    public static final String nexterror = "nexterror";
    public static final String previouserror = "previouserror";
    public static final String exportcompletepopup = "exportcompletepopup";
    public static final String sectiontitles = "sectiontitles";
    public static final String plan = "plan";
    public static final String goals = "goals";
    public static final String assets = "assets";
    public static final String notes = "notes";
    public static final String scenesoutlineitems = "scenesoutlineitems";
    public static final String chapterinfo = "chapterinfo";
    public static final String howtosave = "howtosave";
    public static final String otheritems = "otheritems";
    public static final String onefileperitemtype = "onefileperitemtype";
    public static final String types = "types";
    public static final String singlefile = "singlefile";
    public static final String onefileperchapter = "onefileperchapter";
    public static final String selectitems = "selectitems";
    public static final String importproject = "importproject";
    public static final String projectslist = "projectslist";
    public static final String selectproject = "selectproject";
    public static final String getallprojectserror = "getallprojectserror";
    public static final String moretextindicator = "moretextindicator";
    public static final String choose = "choose";
    public static final String importfromfile = "importfromfile";
    public static final String importfile = "importfile";
    public static final String options = "options";
    public static final String importfromproject = "importfromproject";
    public static final String selectfile = "selectfile";
    public static final String decide = "decide";
    public static final String textextra = "textextra";
    public static final String addtoproject = "addtoproject";
    public static final String importcompletepopup = "importcompletepopup";
    public static final String unabletosave = "unabletosave";
    public static final String supportedfiletypesdescription = "supportedfiletypesdescription";
    public static final String unabletocreateproject = "unabletocreateproject";
    public static final String nofileselected = "nofileselected";
    public static final String filenotexist = "filenotexist";
    public static final String dirselected = "dirselected";
    public static final String openfile = "openfile";
    public static final String cantopenproject = "cantopenproject";
    public static final String popup = "popup";
    public static final String wizard = "wizard";
    public static final String buttons = "buttons";
    public static final String confirm = "confirm";
    public static final String problemfinder = "problemfinder";
    public static final String config = "config";
    public static final String removerule = "removerule";
    public static final String thisproject = "thisproject";
    public static final String allprojects = "allprojects";
    public static final String rulebox = "rulebox";
    public static final String find = "find";
    public static final String info = "info";
    public static final String delete = "delete";
    public static final String edit = "edit";
    public static final String sentence = "sentence";
    public static final String paragraph = "paragraph";
    public static final String words = "words";
    public static final String addrule = "addrule";
    public static final String editrule = "editrule";
    public static final String add = "add";
    public static final String saveruleerror = "saveruleerror";
    public static final String tabtitles = "tabtitles";
    public static final String form = "form";
    public static final String labels = "labels";
    public static final String summary = "summary";
    public static final String description = "description";
    public static final String errors = "errors";
    public static final String entervalue = "entervalue";
    public static final String wordfinder = "wordfinder";
    public static final String passivesentence = "passivesentence";
    public static final String adverb = "adverb";
    public static final String wordphrase = "wordphrase";
    public static final String anywhere = "anywhere";
    public static final String where = "where";
    public static final String startofsentence = "startofsentence";
    public static final String endofsentence = "endofsentence";
    public static final String onlyindialogue = "onlyindialogue";
    public static final String ignoreindialogue = "ignoreindialogue";
    public static final String nowordserror = "nowordserror";
    public static final String rules = "rules";
    public static final String issues = "issues";
    public static final String suffixes = "suffixes";
    public static final String notindialogue = "notindialogue";
    public static final String indialogue = "indialogue";
    public static final String edittitle = "edittitle";
    public static final String insentence = "insentence";
    public static final String speechverbs = "speechverbs";
    public static final String newspeechverbs = "newspeechverbs";
    public static final String removespeechverbs = "removespeechverbs";
    public static final String separate = "separate";
    public static final String punctuationtext = "punctuationtext";
    public static final String wordtext = "wordtext";
    public static final String addtitle = "addtitle";
    public static final String doubleword = "doubleword";
    public static final String paragraphlength = "paragraphlength";
    public static final String sentencetext = "sentencetext";
    public static final String sentences = "sentences";
    public static final String paragraphreadability = "paragraphreadability";
    public static final String fr = "fr";
    public static final String fk = "fk";
    public static final String gf = "gf";
    public static final String sentencelength = "sentencelength";
    public static final String sentencecomplexity = "sentencecomplexity";
    public static final String ratio = "ratio";
    public static final String toomanyclauses = "toomanyclauses";
    public static final String clauses = "clauses";
    public static final String startup = "startup";
    public static final String cantopenlastprojecterror = "cantopenlastprojecterror";
    public static final String alreadyrunningerror = "alreadyrunningerror";
    public static final String unabletostarterror = "unabletostarterror";
    public static final String invalidemail = "invalidemail";
    public static final String functionunavailable = "functionunavailable";
    public static final String actions = "actions";
    public static final String fontunavailable = "fontunavailable";
    public static final String chaptersoverwcmaximum = "chaptersoverwcmaximum";
    public static final String multipleoverlimit = "multipleoverlimit";
    public static final String singleoverlimit = "singleoverlimit";
    public static final String multiple = "multiple";
    public static final String single = "single";
    public static final String chaptersoverreadabilitymaximum = "chaptersoverreadabilitymaximum";
    public static final String showdetail = "showdetail";
    public static final String autobackupnotification = "autobackupnotification";
    public static final String backups = "backups";
    public static final String spellchecker = "spellchecker";
    public static final String unabletosetlanguage = "unabletosetlanguage";
    public static final String achievementspanel = "achievementspanel";
    public static final String savechanges = "savechanges";
    public static final String discardchanges = "discardchanges";
    public static final String confirmpopup = "confirmpopup";
    public static final String fullscreen = "fullscreen";
    public static final String showpanelactionerror = "showpanelactionerror";
    public static final String closepanel = "closepanel";
    public static final String discard = "discard";
    public static final String save = "save";
    public static final String editors = "editors";
    public static final String vieweditorerror = "vieweditorerror";
    public static final String vieweditorserror = "vieweditorserror";
    public static final String showregistererror = "showregistererror";
    public static final String deletechapter = "deletechapter";
    public static final String toolbar = "toolbar";
    public static final String viewaddasset = "viewaddasset";
    public static final String viewchapterinformation = "viewchapterinformation";
    public static final String editchapter = "editchapter";
    public static final String viewproblemfindersidebar = "viewproblemfindersidebar";
    public static final String removetag = "removetag";
    public static final String close = "close";
    public static final String send = "send";
    public static final String email = "email";
    public static final String message = "message";
    public static final String errorlabel = "errorlabel";
    public static final String about = "about";
    public static final String keyboardshortcuts = "keyboardshortcuts";
    public static final String contactsupport = "contactsupport";
    public static final String viewuserguide = "viewuserguide";
    public static final String reportbug = "reportbug";
    public static final String whatsnew = "whatsnew";
    public static final String achievements = "achievements";
    public static final String projectmenu = "projectmenu";
    public static final String type = "type";
    public static final String vieweditors = "vieweditors";
    public static final String editorsserviceregister = "editorsserviceregister";
    public static final String showcontacts = "showcontacts";
    public static final String betabug = "betabug";
    public static final String bug = "bug";
    public static final String warmup = "warmup";
    public static final String reportproblem = "reportproblem";
    public static final String sendlogfiles = "sendlogfiles";
    public static final String sendscreenshot = "sendscreenshot";
    public static final String cancel = "cancel";
    public static final String debugmode = "debugmode";
    public static final String enabled = "enabled";
    public static final String disabled = "disabled";
    public static final String dowarmuponstart = "dowarmuponstart";
    public static final String times = "times";
    public static final String unlimited = "unlimited";
    public static final String words100 = "words100";
    public static final String words250 = "words250";
    public static final String words500 = "words500";
    public static final String words1000 = "words1000";
    public static final String hour1 = "hour1";
    public static final String mins5 = "mins5";
    public static final String mins10 = "mins10";
    public static final String mins20 = "mins20";
    public static final String mins30 = "mins30";
    public static final String week1 = "week1";
    public static final String days2 = "days2";
    public static final String days5 = "days5";
    public static final String hours12 = "hours12";
    public static final String hours24 = "hours24";
    public static final String addwarmuperror = "addwarmuperror";
    public static final String getwarmupsprojecterror = "getwarmupsprojecterror";
    public static final String starterror = "starterror";
    public static final String createwarmupsproject = "createwarmupsproject";
    public static final String openwarmupsprojecterror = "openwarmupsprojecterror";
    public static final String startwriting = "startwriting";
    public static final String saveerror = "saveerror";
    public static final String whicheverfirst = "whicheverfirst";
    public static final String andor = "andor";
    public static final String dofor = "dofor";
    public static final String ownprompt = "ownprompt";
    public static final String nextprompt = "nextprompt";
    public static final String previousprompt = "previousprompt";
    public static final String allpromptsexcluded = "allpromptsexcluded";
    public static final String visitlink = "visitlink";
    public static final String weblinks = "weblinks";
    public static final String noshowpromptagain = "noshowpromptagain";
    public static final String chooseprompt = "chooseprompt";
    public static final String acknowledgments = "acknowledgments";
    public static final String makeadonation = "makeadonation";
    public static final String releasenotes = "releasenotes";
    public static final String sourcecode = "sourcecode";
    public static final String website = "website";
    public static final String copyright = "copyright";
    public static final String qwversion = "qwversion";
    public static final String stop = "stop";
    public static final String tipspanel = "tipspanel";
    public static final String next = "next";
    public static final String previous = "previous";
    public static final String name = "name";
    public static final String tips = "tips";
    public static final String sidebar = "sidebar";
    public static final String section = "section";
    public static final String appearsinchapters = "appearsinchapters";
    public static final String showinsidebar = "showinsidebar";
    public static final String activetitle = "activetitle";
    public static final String headerpopupmenu = "headerpopupmenu";
    public static final String treepopupmenu = "treepopupmenu";
    public static final String tree = "tree";
    public static final String view = "view";
    public static final String sort = "sort";
    public static final String _new = "new";
    public static final String aboutpanel = "aboutpanel";
    public static final String linkedto = "linkedto";
    public static final String editobjecttypeinfo = "editobjecttypeinfo";
    public static final String addfileordocument = "addfileordocument";
    public static final String popupmenu = "popupmenu";
    public static final String user = "user";
    public static final String playsound = "playsound";
    public static final String playsoundinfullscreen = "playsoundinfullscreen";
    public static final String enable = "enable";
    public static final String legacyfields = "legacyfields";
    public static final String aliases = "aliases";
    public static final String webpage = "webpage";
    public static final String textalignments = "textalignments";
    public static final String left = "left";
    public static final String right = "right";
    public static final String justified = "justified";
    public static final String sidebars = "sidebars";
    public static final String othersidebarselect = "othersidebarselect";
    public static final String cantfindeditor = "cantfindeditor";
    public static final String invalidprojectdir = "invalidprojectdir";
    public static final String projectdirisfile = "projectdirisfile";
    public static final String projectdirnotexist = "projectdirnotexist";
    public static final String projectnotexist = "projectnotexist";
    public static final String openerrors = "openerrors";
    public static final String general = "general";
    public static final String writingtargetreachedpopup = "writingtargetreachedpopup";
    public static final String monthly = "monthly";
    public static final String weekly = "weekly";
    public static final String daily = "daily";
    public static final String session = "session";
    public static final String enterpasswordpopup = "enterpasswordpopup";
    public static final String novalue = "novalue";
    public static final String invalidvalue = "invalidvalue";
    public static final String projectalreadyopen = "projectalreadyopen";
    public static final String invalidpassword = "invalidpassword";
    public static final String invalidstate = "invalidstate";
    public static final String open = "open";
    public static final String changestatus = "changestatus";
    public static final String restore = "restore";
    public static final String showerror = "showerror";
    public static final String viewbackupsdir = "viewbackupsdir";
    public static final String nobackups = "nobackups";
    public static final String show = "show";
    public static final String deleteitem = "deleteitem";
    public static final String confirmword = "confirmword";
    public static final String startat = "startat";
    public static final String endat = "endat";
    public static final String splitchapter = "splitchapter";
    public static final String newchaptername = "newchaptername";
    public static final String cantreopenproject = "cantreopenproject";
    //public static final String projectexists = "projectexists";
    public static final String renamechapter = "renamechapter";
    //public static final String chapterexists = "chapterexists";
    public static final String newchapter = "newchapter";
    public static final String deletetype = "deletetype";
    public static final String warning = "warning";
    public static final String valueexists = "valueexists";
    public static final String warmups = "warmups";
    public static final String deletewarmup = "deletewarmup";
    public static final String notifications = "notifications";
    public static final String remove = "remove";
    public static final String filefinder = "filefinder";
    public static final String errormessage = "errormessage";
    public static final String generalmessage = "generalmessage";
    public static final String iconcolumn = "iconcolumn";
    public static final String distractionfreemode = "distractionfreemode";
    public static final String viewitem = "viewitem";
    public static final String edititem = "edititem";
    public static final String moveitem = "moveitem";
    public static final String linkitem = "linkitem";
    public static final String manage = "manage";
    public static final String rename = "rename";
    public static final String colorchooser = "colorchooser";
    public static final String hex = "hex";
    public static final String red = "red";
    public static final String blue = "blue";
    public static final String green = "green";
    public static final String color = "color";
    public static final String swatch = "swatch";
    public static final String reset = "reset";
    public static final String objectfinder = "objectfinder";
    public static final String renamewarmup = "renamewarmup";
    public static final String editorproject = "editorproject";
    public static final String normal = "normal";
    public static final String editor = "editor";
    public static final String plural = "plural";
    public static final String headercontrols = "headercontrols";
    public static final String loading = "loading";
    public static final String notfound = "notfound";
    public static final String results = "results";
    public static final String findall = "findall";
    public static final String ignored = "ignored";
    public static final String unignore = "unignore";
    public static final String ignore = "ignore";
    public static final String checkbox = "checkbox";
    public static final String overlay = "overlay";
    public static final String deleteall = "deleteall";
    public static final String addsection = "addsection";
    public static final String newobject = "newobject";
    public static final String hidesection = "hidesection";
    public static final String addtag = "addtag";
    public static final String below = "below";
    public static final String tags = "tags";
    public static final String newtag = "newtag";
    public static final String table = "table";
    public static final String finish = "finish";
    public static final String loadallerror = "loadallerror";
    public static final String converttoproject = "converttoproject";
    public static final String entersummaryerror = "entersummaryerror";
    public static final String maxchars = "maxchars";
    public static final String dictionary = "dictionary";
    public static final String synonyms = "synonyms";
    public static final String formatting = "formatting";
    public static final String format = "format";
    public static final String bold = "bold";
    public static final String italic = "italic";
    public static final String underline = "underline";
    public static final String spellcheck = "spellcheck";
    public static final String nosuggestions = "nosuggestions";
    public static final String nosynonyms = "nosynonyms";
    public static final String more = "more";
    public static final String redo = "redo";
    public static final String undo = "undo";
    public static final String cut = "cut";
    public static final String paste = "paste";
    public static final String copy = "copy";
    public static final String textarea = "textarea";
    public static final String charsremaining = "charsremaining";
    public static final String charsover = "charsover";
    public static final String deletetag = "deletetag";
    public static final String prompt = "prompt";
    public static final String create = "create";
    public static final String newprojectpanel = "newprojectpanel";
    public static final String encrypt = "encrypt";
    public static final String password = "password";
    public static final String confirmpassword = "confirmpassword";
    public static final String nopassword = "nopassword";
    public static final String nomatch = "nomatch";
    public static final String savein = "savein";
    public static final String newwords = "newwords";
    public static final String nodirselected = "nodirselected";
    public static final String createexporter = "createexporter";
    public static final String objectnames = "objectnames";
    public static final String singular = "singular";
    public static final String convertwarmup = "convertwarmup";
    public static final String editwarmup = "editwarmup";
    public static final String enter = "enter";
    public static final String exit = "exit";
    public static final String firsttimepopup = "firsttimepopup";
    public static final String editproperties = "editproperties";
    public static final String sessionwordcount = "sessionwordcount";
    public static final String chapterwordcount = "chapterwordcount";
    public static final String word = "word";
    public static final String distractionfreemodeenter = "distractionfreemodeenter";
    public static final String distractionfreemodeexit = "distractionfreemodeexit";
    public static final String showproperties = "showproperties";
    public static final String achievementreached = "achievementreached";
    public static final String fullscreenexit = "fullscreenexit";
    public static final String changer = "changer";
    public static final String resetchange = "resetchange";
    public static final String confirmchange = "confirmchange";
    public static final String panel = "panel";
    public static final String nolinksedit = "nolinksedit";
    public static final String file = "file";
    public static final String othernames = "othernames";
    public static final String allowmulti = "allowmulti";
    public static final String multi = "multi";
    public static final String valueseparator = "valueseparator";
    public static final String addedit = "addedit";
    public static final String bulletpoints = "bulletpoints";
    public static final String max = "max";
    public static final String min = "min";
    public static final String greaterthanmin = "greaterthanmin";
    public static final String _default = "default";
    public static final String lessthanmax = "lessthanmax";
    public static final String smallicon = "smallicon";
    public static final String bigicon = "bigicon";
    public static final String warnings = "warnings";
    public static final String basic = "basic";
    public static final String userobjects = "userobjects";
    public static final String fields = "fields";
    public static final String move = "move";
    public static final String layout = "layout";
    public static final String layouts = "layouts";
    public static final String layout0 = "0";
    public static final String layout1 = "1";
    public static final String layout2 = "2";
    public static final String layout3 = "3";
    public static final String layout4 = "4";
    public static final String layout5 = "5";
    public static final String layout6 = "6";
    public static final String layout7 = "7";
    public static final String layout8 = "8";
    public static final String spellcheckon = "spellcheckon";
    public static final String spellcheckoff = "spellcheckoff";
    public static final String tools = "tools";
    public static final String wordcount = "wordcount";
    public static final String editorpanel = "editorpanel";
    public static final String textproperties = "textproperties";
    public static final String print = "print";
    public static final String convert = "convert";
    public static final String notetypes = "notetypes";
    public static final String removeeditposition = "removeeditposition";
    public static final String seteditcomplete = "seteditcomplete";
    public static final String showchapterinfo = "showchapterinfo";
    public static final String seteditposition = "seteditposition";
    public static final String compresstext = "compresstext";
    public static final String shortcutprefix = "shortcutprefix";
    public static final String doubleclickmenu = "doubleclickmenu";
    public static final String ideatypes = "ideatypes";
    public static final String ideas = "ideas";
    public static final String on = "on";
    public static final String off = "off";
    public static final String objectpreview = "objectpreview";
    public static final String newbelow = "newbelow";
    public static final String edittextproperties = "edittextproperties";
    public static final String chapters = "chapters";
    public static final String scenes = "scenes";
    public static final String outlineitems = "outlineitems";
    public static final String seteditneeded = "seteditneeded";
    public static final String ignoreall = "ignoreall";
    public static final String manageitems = "manageitems";
    public static final String newitems = "newitems";
    public static final String separators = "separators";
    public static final String status = "status";
    public static final String clicktoclose = "clicktoclose";
    public static final String clicktoview = "clicktoview";
    public static final String clicktohide = "clicktohide";
    public static final String doubleclicktoview = "doubleclicktoview";
    public static final String readability = "readability";
    public static final String projectwords = "projectwords";
    public static final String totalwords = "totalwords";
    public static final String selected = "selected";
    public static final String edited = "edited";
    public static final String allchapters = "allchapters";
    public static final String viewdetaillink = "viewdetaillink";
    public static final String helplink = "helplink";
    public static final String sparkline = "sparkline";
    public static final String valuepercent = "valuepercent";
    public static final String a4pages = "a4pages";
    public static final String documents = "documents";
    public static final String showfolder = "showfolder";
    public static final String noobjectselectedpanel = "noobjectselectedpanel";
    public static final String upgrade = "upgrade";
    public static final String download = "download";
    public static final String start = "start";
    public static final String cantcreatetemporaryfile = "cantcreatetemporaryfile";
    public static final String newversionavailable = "newversionavailable";
    public static final String exitnow = "exitnow";
    public static final String exitlater = "exitlater";
    public static final String unabletodownload = "unabletodownload";
    public static final String later = "later";
    public static final String restart = "restart";
    public static final String complete = "complete";
    public static final String digestinvalid = "digestinvalid";
    public static final String inprogress = "inprogress";
    public static final String checkingfile = "checkingfile";
    public static final String changedisplay = "changedisplay";
    public static final String managestatuses = "managestatuses";
    public static final String sortwordcount = "sortwordcount";
    public static final String sortstatus = "sortstatus";
    public static final String sortname = "sortname";
    public static final String sortlastedited = "sortlastedited";
    public static final String sortprojects = "sortprojects";
    public static final String findprojects = "findprojects";
    public static final String importfileorproject = "importfileorproject";
    public static final String noprojects = "noprojects";
    public static final String example = "example";
    public static final String selectbackground = "selectbackground";
    public static final String removeproject = "removeproject";
    public static final String projectopen = "projectopen";
    public static final String unspecified = "unspecified";
    public static final String error = "error";
    public static final String encrypted = "encrypted";
    public static final String tooltips = "tooltips";
    public static final String managebackups = "managebackups";
    public static final String newstatus = "newstatus";
    public static final String setstatus = "setstatus";
    public static final String editcomplete = "editcomplete";
    public static final String notedited = "notedited";
    public static final String lastedited = "lastedited";
    public static final String details = "details";
    public static final String nomoreproblems = "nomoreproblems";
    public static final String limit = "limit";
    public static final String unignoreall = "unignoreall";
    public static final String noproblemsfound = "noproblemsfound";
    public static final String end = "end";
    public static final String unignoreissues = "unignoreissues";
    public static final String wordtypes = "wordtypes";
    public static final String adverbs = "adverbs";
    public static final String other = "other";
    public static final String tabs = "tabs";
    public static final String verbs = "verbs";
    public static final String adjectives = "adjectives";
    public static final String nouns = "nouns";
    public static final String notfirst = "notfirst";
    public static final String firstusewizard = "firstusewizard";
    public static final String selectprojectdb = "selectprojectdb";
    public static final String existing = "existing";
    public static final String manual = "manual";
    public static final String imageselector = "imageselector";
    public static final String viewonlypanel = "viewonlypanel";
    public static final String testboolbarbuttonmessage = "testboolbarbuttonmessage";
    public static final String test = "test";
    public static final String bulletedtext = "bulletedtext";
    public static final String charts = "charts";
    public static final String allwordcounts = "allwordcounts";
    public static final String _for = "for";
    public static final String xaxis = "xaxis";
    public static final String yaxis = "yaxis";
    public static final String lastmonth = "lastmonth";
    public static final String thismonth = "thismonth";
    public static final String alltime = "alltime";
    public static final String lastweek = "lastweek";
    public static final String thisweek = "thisweek";
    public static final String perchapter = "perchapter";
    public static final String history = "history";
    public static final String showaverage = "showaverage";
    public static final String showtarget = "showtarget";
    public static final String markers = "markers";
    public static final String average = "average";
    public static final String target = "target";
    public static final String chaptersovertarget = "chaptersovertarget";
    public static final String averagesuffix = "averagesuffix";
    public static final String now = "now";
    public static final String forchapters = "forchapters";
    public static final String notarget = "notarget";
    public static final String detail = "detail";
    public static final String timeseries = "timeseries";
    public static final String added = "added";
    public static final String removed = "removed";
    public static final String showgf = "showgf";
    public static final String showfk = "showfk";
    public static final String showtargets = "showtargets";
    public static final String averagegf = "averagegf";
    public static final String averagegfsuffix = "averagegfsuffix";
    public static final String averagefk = "averagefk";
    public static final String averagefksuffix = "averagefksuffix";
    public static final String targetfk = "targetfk";
    public static final String targetgf = "targetgf";
    public static final String overtargetfk = "overtargetfk";
    public static final String overtargetgf = "overtargetgf";
    public static final String excluded = "excluded";
    public static final String notargets = "notargets";
    public static final String sessionlength = "sessionlength";
    public static final String sessions = "sessions";
    public static final String sessionsover1hr = "sessionsover1hr";
    public static final String sessionsovertarget = "sessionsovertarget";
    public static final String sessionmostwords = "sessionmostwords";
    public static final String numsessions = "numsessions";
    public static final String totalsessiontime = "totalsessiontime";
    public static final String numzerowordsessions = "numzerowordsessions";
    public static final String longestsession = "longestsession";
    public static final String averagesessionlength = "averagesessionlength";
    public static final String showzero = "showzero";
    public static final String averagesession = "averagesession";
    public static final String hide = "hide";
    public static final String rating = "rating";
    public static final String shorttext = "shorttext";
    public static final String sortrating = "sortrating";
    public static final String sortdate = "sortdate";
    public static final String sortalpha = "sortalpha";
    public static final String newidea = "newidea";
    public static final String noideas = "noideas";
    public static final String header = "header";
    public static final String defaulttypes = "defaulttypes";
    public static final String dialogue = "dialogue";
    public static final String none = "none";
    public static final String clear = "clear";
    public static final String image = "image";
    public static final String newtypes = "newtypes";
    public static final String newnotetype = "newnotetype";
    public static final String selectitem = "selectitem";
    public static final String unavailable = "unavailable";
    public static final String affirmativevalue = "affirmativevalue";
    public static final String notification = "notification";
    public static final String preview = "preview";
    public static final String nodescription = "nodescription";
    public static final String problemcount = "problemcount";
    public static final String spellingcount = "spellingcount";
    public static final String emptychapter = "emptychapter";
    public static final String notapplicable = "notapplicable";
    public static final String apply = "apply";
    public static final String defaults = "defaults";
    public static final String names = "names";
    public static final String unabletoopenfile = "unabletoopenfile";
    public static final String unabletoopenwebpage = "unabletoopenwebpage";
    public static final String appendix = "appendix";
    public static final String bookdetails = "bookdetails";
    public static final String authorname = "authorname";
    public static final String id = "id";
    public static final String addor = "andor";
    public static final String time = "time";
    public static final String timer = "timer";
    public static final String remaining = "remaining";
    public static final String less1min = "less1min";
    public static final String over1min = "over1min";
    public static final String noname = "noname";
    public static final String adddesctochapter = "adddesctochapter";
    public static final String chapteritems = "chapteritems";
    public static final String mywriting = "mywriting";
    public static final String showmessagewhentargetreached = "showmessagewhentargetreached";
    public static final String showwarningwhenchapterexceedsmax = "showwarningwhenchapterexceedsmax";
    public static final String chaptersovermaxtarget = "chaptersovermaxtarget";
    public static final String chaptersoverreadabilitytarget = "chaptersoverreadabilitytarget";
    public static final String overlimit = "overlimit";
    public static final String maximum = "maximum";
    public static final String unabletoperformaction = "unabletoperformaction";
    public static final String fullscreentitle = "fullscreentitle";
    public static final String deletesceneoutlineitems = "deletesceneoutlineitems";
    public static final String fontsize = "fontsize";
    public static final String alignment = "alignment";
    public static final String linespacing = "linespacing";
    public static final String textborder = "textborder";
    public static final String indentfirstline = "indentfirstline";
    public static final String highlightwritingline = "highlightwritingline";
    public static final String highlightlinecolor = "highlightlinecolor";
    public static final String textcolor = "textcolor";
    public static final String bgcolor = "bgcolor";
    public static final String bgopacity = "bgopacity";
    public static final String bgimagewebsites = "bgimagewebsites";
    public static final String fullscreenproperties = "fullscreenproperties";
    public static final String areasize = "areasize";
    public static final String showtimewordcount = "showtimewordcount";
    public static final String versions = "versions";
    public static final String qwstart = "qwstart";
    public static final String showtips = "showtips";
    public static final String showlastedited = "showlastedited";
    public static final String showprojectswindow = "showprojectswindow";
    public static final String betas = "betas";
    public static final String optin = "optin";
    public static final String itemsandrules = "itemsandrules";
    public static final String autologin = "autologin";
    public static final String defaultstatus = "defaultstatus";
    public static final String statuses = "statuses";
    public static final String online = "online";
    public static final String offline = "offline";
    public static final String busy = "busy";
    public static final String away = "away";
    public static final String snooze = "snooze";
    public static final String fullscreenbusystatus = "fullscreenbusystatus";
    public static final String logmessages = "logmessages";
    public static final String problemfinderrules = "problemfinderrules";
    public static final String editingchapters = "editingchapters";
    public static final String haseditposition = "haseditposition";
    public static final String showicon = "showicon";
    public static final String autosave = "autosave";
    public static final String autosavewhen = "autosavewhen";
    public static final String viewexample = "viewexample";
    public static final String showeditposition = "showeditposition";
    public static final String seteditpositioncolor = "seteditpositioncolor";
    public static final String seteditcompleteatchapterend = "seteditcompleteatchapterend";
    public static final String compressrightclickmenu = "compressrightclickmenu";
    public static final String setspellcheckerlanguage = "setspellcheckerlanguage";
    public static final String downloadlanguagefiles = "downloadlanguagefiles";
    public static final String setasdefaultlanguage = "setasdefaultlanguage";
    public static final String nonenglishwarning = "nonenglishwarning";
    public static final String downloaddictionaryfiles = "downloaddictionaryfiles";
    public static final String managedictionary = "managedictionary";
    public static final String naming = "naming";
    public static final String changenames = "changenames";
    public static final String keepprojectswindowsopen = "keepprojectswindowsopen";
    public static final String showprojectswindownoopenproject = "showprojectswindownoopenproject";
    public static final String showpreview = "showpreview";
    public static final String shownotes = "shownotes";
    public static final String lookandsound = "lookandsound";
    public static final String interfacelayout = "interfacelayout";
    public static final String interfacelayouts = "interfacelayouts";
    public static final String showtoolbar = "showtoolbar";
    public static final String belowsidebar = "belowsidebar";
    public static final String abovesidebar = "abovesidebar";
    public static final String showtabs = "showtabs";
    public static final String showtabstop = "showtabstop";
    public static final String showtabsbottom = "showtabsbottom";
    public static final String whenfind = "whenfind";
    public static final String expandall = "expandall";
    public static final String justchapter = "justchapter";
    public static final String playtypewritersound = "playtypewritersound";
    public static final String usesound = "usesound";
    public static final String selectownwavfile = "selectownwavfile";
    public static final String highlightdividers = "highlightdividers";
    public static final String filter = "filter";
    public static final String newasset = "newasset";
    public static final String alwayspopup = "alwayspopup";
    public static final String popupifpossible = "popupifpossible";
    public static final String owntab = "owntab";
    public static final String editassetconfig = "editassetconfig";
    public static final String addtype = "addtype";
    public static final String projectandbackup = "projectandbackup";
    public static final String selectprojectdir = "selectprojectdir";
    public static final String all = "all";
    public static final String autobackup = "autobackup";
    public static final String createbackupafter = "createbackupafter";
    public static final String nobackupstokeep = "nobackupstokeep";
    public static final String selectbackupdir = "selectbackupdir";
    public static final String link = "link";
    public static final String savepropertyerror = "savepropertyerror";
    public static final String changeprojectdir = "changeprojectdir";
    public static final String backupdirchangewarning = "backupdirchangewarning";
    public static final String reopenproject = "reopenproject";
    public static final String dirnotempty = "dirnotempty";
    public static final String backupdirnotempty = "backupdirnotempty";
    public static final String changebackupdir = "changebackupdir";
    public static final String usedefault = "usedefault";
    public static final String examplechapter = "examplechapter";
    public static final String uilanguage = "uilanguage";
    //public static final String select = "select";
    public static final String downloading = "downloading";
    public static final String restartwarning = "restartwarning";
    public static final String set = "set";
    public static final String createtranslation = "createtranslation";
    public static final String edittranslation = "edittranslation";
    public static final String feedback = "feedback";
    public static final String switchtoversion = "switchtoversion";
    public static final String comment = "comment";
    public static final String unsentcommentspopup = "unsentcommentspopup";
    public static final String unsentcomments = "unsentcomments";
    public static final String editcomment = "editcomment";
    public static final String viewertitle = "viewertitle";
    public static final String viewertitleversionwrapper = "viewertitleversionwrapper";
    public static final String previouscontact = "previouscontact";
    public static final String viewcomment = "viewcomment";
    public static final String auto = "auto";
    public static final String login = "login";
    public static final String messages = "messages";
    public static final String undealtwith = "undealtwith";
    public static final String reasons = "reasons";
    public static final String updateinfotoall = "updateinfotoall";
    public static final String updateinfotocontact = "updateinfotocontact";
    public static final String sendmessagetocontact = "sendmessagetocontact";
    public static final String invitesent = "invitesent";
    public static final String sendprojectoninvite = "sendprojectoninvite";
    public static final String sendinvite = "sendinvite";
    public static final String editoroffline = "editoroffline";
    public static final String deleteaccount = "deleteaccount";
    public static final String deletealleditorprojects = "deletealleditorprojects";
    public static final String deleteprojectsforeditor = "deleteprojectsforeditor";
    public static final String avatar = "avatar";
    public static final String accept = "accept";
    public static final String reject = "reject";
    public static final String updated = "updated";
    public static final String sendproject = "sendproject";
    public static final String updateproject = "updateproject";
    public static final String firstupdatewithversion = "firstupdatewithversion";
    public static final String firstupdate = "firstupdate";
    public static final String lastupdatewithversion = "lastupdatewithversion";
    public static final String lastupdate = "lastupdate";
    public static final String sendorupdateproject = "sendorupdateproject";
    public static final String nochapters = "nochapters";
    public static final String unsavedchanges = "unsavedchanges";
    public static final String dueby = "dueby";
    public static final String updatesuffix = "updatesuffix";
    public static final String version = "version";
    public static final String editorstatus = "editorstatus";
    public static final String sendunsentcomments = "sendunsentcomments";
    public static final String comments = "comments";
    public static final String reportmessage = "reportmessage";
    public static final String from = "from";
    public static final String reason = "reason";
    public static final String changepassword = "changepassword";
    public static final String newpassword = "newpassword";
    public static final String register = "register";
    public static final String exists = "exists";
    public static final String savepasswordwarningpopup = "savepasswordwarningpopup";
    public static final String prefix = "prefix";
    public static final String invalidcredentials = "invalidcredentials";
    public static final String alreadyregistered = "alreadyregistered";
    public static final String agreetandc = "agreetandc";
    public static final String viewtandc = "viewtandc";
    public static final String youremail = "youremail";
    public static final String alreadyinvited = "alreadyinvited";
    public static final String previousrejected = "previousrejected";
    public static final String inviteeditor = "inviteeditor";
    public static final String invite = "invite";
    public static final String noemail = "noemail";
    public static final String self = "self";
    public static final String showprojectscontactisediting = "showprojectscontactisediting";
    public static final String showprojectseditingforcontact = "showprojectseditingforcontact";
    public static final String commentssent = "commentssent";
    public static final String commentsreceived = "commentsreceived";
    public static final String important = "important";
    public static final String suffix = "suffix";
    public static final String projectupdated = "projectupdated";
    public static final String inactiveaccount = "inactiveaccount";
    public static final String maxloginattempts = "maxloginattempts";
    public static final String deleteinvite = "deleteinvite";
    public static final String updateinvite = "updateinvite";
    public static final String sendchat = "sendchat";
    public static final String contactistyping = "contactistyping";
    public static final String sending = "sending";
    public static final String box = "box";
    public static final String received = "received";
    public static final String response = "response";
    public static final String update = "update";
    public static final String sent = "sent";
    public static final String report = "report";
    public static final String attention = "attention";
    public static final String resendinvite = "resendinvite";
    public static final String allmessages = "allmessages";
    public static final String removecontact = "removecontact";
    public static final String updatecontactinfo = "updatecontactinfo";
    public static final String projectscontactediting = "projectscontactediting";
    public static final String sendupdateproject = "sendupdateproject";
    public static final String viewcommentserror = "viewcommentserror";
    public static final String lastcommentssent = "lastcommentssent";
    public static final String lastcommentsreceived = "lastcommentsreceived";
    public static final String projectsuserediting = "projectsuserediting";
    public static final String projectupdates = "projectupdates";
    public static final String importantmessages = "importantmessages";
    public static final String sendmessage = "sendmessage";
    public static final String unreadchatmessages = "unreadchatmessages";
    public static final String noprojectcomments = "noprojectcomments";
    public static final String projectcomments = "projectcomments";
    public static final String undealtwithmessagecount = "undealtwithmessagecount";
    public static final String projecteditor = "projecteditor";
    public static final String onlinestatus = "onlinestatus";
    public static final String previouseditor = "previouseditor";
    public static final String pendingeditor = "pendingeditor";
    public static final String invitereceived = "invitereceived";
    public static final String currenteditor = "currenteditor";
    public static final String contactinfo = "contactinfo";
    public static final String inviteresponse = "inviteresponse";
    public static final String dealtwith = "dealtwith";
    public static final String rejected = "rejected";
    public static final String accepted = "accepted";
    public static final String contactremoved = "contactremoved";
    public static final String chatmessages = "chatmessages";
    public static final String today = "today";
    public static final String yesterday = "yesterday";
    public static final String sentbyme = "sentbyme";
    public static final String redownload = "redownload";
    public static final String editneededtitle = "editneededtitle";
    public static final String editneedednote = "editneedednote";
    public static final String font = "font";
    public static final String shortcut = "shortcut";
    public static final String projecteditstop = "projecteditstop";
    public static final String newprojectresponse = "newprojectresponse";
    public static final String extra = "extra";
    public static final String clicktoviewproject = "clicktoviewproject";
    public static final String clicktoviewcomments = "clicktoviewcomments";
    public static final String othercomments = "othercomments";
    public static final String item = "item";
    public static final String displaypassword = "displaypassword";
    public static final String resetpassword = "resetpassword";
    public static final String resendaccountconfirmationemail = "resendaccountconfirmationemail";
    public static final String updatenameavatar = "updatenameavatar";
    public static final String previouscontacts = "previouscontacts";
    public static final String preferences = "preferences";
    public static final String logout = "logout";
    public static final String invitesfromothers = "invitesfromothers";
    public static final String allcontacts = "allcontacts";
    public static final String pendinginvites = "pendinginvites";
    public static final String nocontacts = "nocontacts";
    public static final String firstlogin = "firstlogin";
    public static final String savepassword = "savepassword";
    public static final String otherversions = "otherversions";
    public static final String noversion = "noversion";
    public static final String unsent = "unsent";
    public static final String projectsent = "projectsent";
    public static final String viewchapter = "viewchapter";
    public static final String commentspanel = "commentspanel";
    public static final String logindetails = "logindetails";
    public static final String minlength = "minlength";
    public static final String reminderpopup = "reminderpopup";
    public static final String check = "check";
    public static final String selectfolder = "selectfolder";
    public static final String saving = "saving";
    public static final String newcomment = "newcomment";
    public static final String due = "due";
    public static final String latest = "latest";
    public static final String newupdateproject = "newupdateproject";
    public static final String notspecified = "notspecified";
    public static final String projectdeleted = "projectdeleted";
    public static final String previouseditors = "previouseditors";
    public static final String hidepreviouseditors = "hidepreviouseditors";
    public static final String unknownproject = "unknownproject";
    public static final String notinlist = "notinlist";

}
