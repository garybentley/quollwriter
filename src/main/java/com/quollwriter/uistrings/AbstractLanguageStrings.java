package com.quollwriter.uistrings;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

import org.jdom.*;

import com.gentlyweb.utils.*;

import com.quollwriter.data.*;
import com.quollwriter.*;

public abstract class AbstractLanguageStrings<E extends AbstractLanguageStrings> extends NamedObject implements RefValueProvider, Comparable<E>
{

    public static final String ENGLISH_ID = ":" + Constants.ENGLISH;
    public static final String OBJECT_TYPE = "languagestrings";

    //public static String ID_PART_SEP = ".";
    //public static String ID_REF_START = "${";
    //public static String ID_REF_END = "}";

    private int stringsVersion = 0;
    private Date created = null;
    private Date lastModified = null;
    private String _email = null;
    private E derivedFrom = null;
    //private Map<String, Node> nodes = new HashMap<> ();
    private Set<Section> sections = null;
    private boolean isUser = false;
    private BaseStrings strings = null;

    protected AbstractLanguageStrings ()
    {

        super (OBJECT_TYPE);

        this.strings = new BaseStrings (null);

    }

    public AbstractLanguageStrings (BaseStrings strs)
    {

        this ();

        this.strings = strs;

    }

    public AbstractLanguageStrings (E derivedFrom)
    {

        this ();

        this.setDerivedFrom (derivedFrom);
        // Clone the nodes in the derivedFrom.
        //this.nodes = derivedFrom.cloneNodes ();
        this.setId (UUID.randomUUID ().toString ());
        this.created = new Date ();

    }

    public abstract String getDisplayName ();

/*
    public AbstractLanguageStrings (File f)
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
*/

    public Set<TextValue> getAllTextValues ()
    {

        return this.strings.getAllTextValues ();

    }

    public Set<Value> find (String text)
    {

        return this.strings.find (text);

    }

    public E getDerivedFrom ()
    {

        return this.derivedFrom;

    }

    public void setDerivedFrom (E derivedFrom)
    {

        this.derivedFrom = derivedFrom;

        if (this.derivedFrom != null)
        {

            this.strings.setParent (this.derivedFrom.getStrings ());

        }

    }

    public AbstractLanguageStrings (String jsonData)
                             throws GeneralException
    {

        this ();

        this.init (jsonData);

    }

    public BaseStrings getStrings ()
    {

        return this.strings;

    }

    public void setUser (boolean v)
    {

        this.isUser = v;

    }

    public boolean isUser ()
    {

        return this.isUser;

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

        if (!(o instanceof AbstractLanguageStrings))
        {

            return false;

        }

        E ls = (E) o;

        return this.compareTo (ls) == 0;

    }

    @Override
    public int compareTo (E obj)
    {

        if (obj == null)
        {

            return -1;

        }

        if (this.getId ().equals (obj.getId ()))
        {

            return this.stringsVersion - obj.getStringsVersion ();

        }

/*
        if (this.getId ().equals (obj.getId ()))
        {

            return this.qwVersion.compareTo (obj.qwVersion);

        }

        int v = this.qwVersion.compareTo (obj.qwVersion);
*/
        return this.getName ().compareTo (obj.getName ());

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

    public void init (Map<String, Object> m)
               throws GeneralException
    {

        if (m == null)
        {

            throw new IllegalArgumentException ("No object provided.");

        }

        this.setId (this.getString (":id",
                                    m));

        Number sv = (Number) m.get (":version");

        if (sv != null)
        {

            this.stringsVersion = sv.intValue ();

        }

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

        this.strings = new BaseStrings ((this.derivedFrom != null ? this.derivedFrom.getStrings () : null));
        this.strings.init (m);

/*
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
*/
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

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "nativename",
                                    this.getName ());
        this.addToStringProperties (props,
                                    "email",
                                    this._email);
        this.addToStringProperties (props,
                                    "created",
                                    this.created);
        this.addToStringProperties (props,
                                    "stringsversion",
                                    this.stringsVersion);

        if (this.parent != null)
        {

            this.addToStringProperties (props,
                                        "derivedfrom",
                                        this.parent.getId ());

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

        m.put (":nativename",
               this.getName ());
        m.put (":email",
               this._email);
        m.put (":id",
               this.getId ());
        m.put (":created",
               this.created.getTime ());
        m.put (":user",
               this.isUser);
        m.put (":version",
               this.stringsVersion);

        if (this.lastModified != null)
        {

            m.put (":lastmodified",
                   this.lastModified.getTime ());

        }

        if (this.derivedFrom != null)
        {

            m.put (":derivedfrom",
                   this.derivedFrom.getId ());

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

        m.putAll (this.strings.getAsJSON ());

        return m;

    }

    public Node createNode (String id)
    {

        return new Node (id,
                         null);

    }

/*
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
*/
/*
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
*/
    public static Set<String> getErrors (String           text,
                                         String           textId,
                                         int              scount,
                                         RefValueProvider prov)
    {

        return BaseStrings.getErrors (text,
                                      textId,
                                      scount,
                                      prov);

    }
/*
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
*/
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

    public Map<TextValue, Set<String>> getErrors ()
    {

        Map<TextValue, Set<String>> ret = new LinkedHashMap<> ();

        for (TextValue v : this.strings.getAllTextValues ())
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
                &&
                (v instanceof TextValue)
               )
            {

                TextValue tv = (TextValue) v;

                int c = 0;
                boolean invalid = false;

                String rawText = tv.getRawText ();

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

                if (tv.getSCount () != c)
                {

                    errors.add (String.format (":scount value is incorrect or not present, expected: %s, scount is %s.",
                                               c,
                                               tv.getSCount ()));

                }

                if ((c > 0)
                    &&
                    (tv.getComment () == null)
                   )
                {

                    errors.add (String.format ("Value contains one or more %s values but not have an associated comment.",
                                               "%x$s"));

                }

                if ((tv.getSCount () > 0)
                    &&
                    (tv.getComment () == null)
                   )
                {

                    errors.add ("S count present but no comment provided.");

                }

            }

        }

        return ret;

    }

    public Map<TextValue, Set<String>> getErrors (List<String> id)
    {

        Map<TextValue, Set<String>> ret = new LinkedHashMap<> ();

        for (TextValue v : this.strings.getAllTextValues (id))
        {

            Set<String> errors = null;

            if (v instanceof TextValue)
            {

                errors = ((TextValue) v).getErrors (this);

            }

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

    public String getString (String id,
                             Map    from)
    {

        Object o = from.get (id);

        if (o == null)
        {

            return null;

        }

        return o.toString ();

    }

    public Number getNumber (String id,
                             Map    from)
    {

        Object o = from.get (id);

        if (o == null)
        {

            return null;

        }

        if (o instanceof Number)
        {

            return (Number) o;

        }

        return null;

    }

    public Map<String, Node> cloneNodes ()
    {

        return this.strings.cloneNodes ();

    }

    public boolean containsId (String id)
    {

        return this.containsId (BaseStrings.getIdParts (id));

    }

    public boolean containsId (List<String> idparts)
    {

        return this.strings.containsId (idparts);

    }

    /**
     * Return a list of values that this set contains but the old does not or if the old
     * has the value then is the raw text value different.
     *
     * @return The set of new or raw text different values.
     */
    public Set<Value> diff (UILanguageStrings old)
    {

        return this.strings.diff (old.getStrings ());
/*
        Set<Value> ret = new LinkedHashSet<> ();

        Set<Value> allVals = this.getAllValues ();

        for (Value v : allVals)
        {

            Value ov = old.getValue (v.getId ());

            if (ov == null)
            {

                // This is a new value.
                ret.add (v);

            } else {

                if (v.isDifferent (ov))
                {

                    ret.add (v);

                }

            }

        }

        return ret;
*/
    }

    public Map<String, Set<Node>> getNodesInSections (String defSection)
    {

        return this.strings.getNodesInSections (defSection);

    }

    public Set<Node> getNodes (List<String> idparts,
                               Filter<Node> filter)
    {

        return this.strings.getNodes (idparts,
                                      filter);

    }

    public Set<Node> getNodes (List<String> idParts)
    {

        return this.getNodes (idParts,
                              null);

    }

    public Set<Node> getNodes (Filter<Node> filter)
    {

        return this.strings.getNodes (filter);

    }

    public Set<ImageValue> getAllImageValues ()
    {

        return this.strings.getAllImageValues ();

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

        return this.strings.getAllValues (filter);

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

        return this.strings.getAllValues (idparts,
                                          filter);

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

/*
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
*/

    public Node removeNode (List<String> idparts)
                     throws GeneralException
    {

        return this.strings.removeNode (idparts);

    }

    public TreeSet<String> getIdMatches (String id)
    {

        return this.strings.getIdMatches (id);

    }

    public Node getNode (String id)
    {

        List<String> idparts = BaseStrings.getIdParts (id);

        return this.getNode (idparts);

    }

    public Node getNode (List<String> idparts)
    {

        return this.strings.getNode (idparts);

    }

    public static boolean isSpecialId (List<String> id)
    {

        return BaseStrings.isSpecialId (id);

    }

    public TextValue getTextValue (List<String> idparts)
    {

        Value v = this.getValue (idparts);

        if (v instanceof TextValue)
        {

            return (TextValue) v;

        }

        return null;

    }

    public ImageValue getImageValue (List<String> idparts)
    {

        Value v = this.getValue (idparts);

        if (v instanceof ImageValue)
        {

            return (ImageValue) v;

        }

        return null;

    }

    public Value getValue (List<String> idparts)
    {

        return this.strings.getValue (idparts,
                                      false);

    }

    public Value getValue (List<String> idparts,
                           boolean      thisOnly)
    {

        return this.strings.getValue (idparts,
                                      thisOnly);

    }

    public TextValue insertTextValue (List<String> idparts)
                               throws GeneralException
    {

        return this.strings.insertTextValue (idparts);

    }

    public ImageValue insertImageValue (List<String> idparts)
                                 throws GeneralException
    {

        return this.strings.insertImageValue (idparts);

    }

    public String getBuiltText (String text)
    {

        return new TextValue (null, null, text, null, 0).getBuiltText (this);

    }

    public Value getValue (String id)
    {

        return this.getValue (BaseStrings.getIdParts (id));

    }

    public boolean isIdValid (String id)
    {

        return this.getNode (id) != null;

    }

    @Override
    public int getSCount (String id)
    {

        return this.getSCount (BaseStrings.getIdParts (id));

    }

    @Override
    public String getRawText (String id)
    {

        return this.getRawText (BaseStrings.getIdParts (id));

    }

    @Override
    public String getString (String id)
    {

        return this.getString (BaseStrings.getIdParts (id));

    }

    public int getSCount (List<String> idparts)
    {

        if (this.derivedFrom != null)
        {

            // Defer to our parent.
            return this.derivedFrom.getSCount (idparts);

        }

        if (idparts.size () < 1)
        {

            return 0;

        }

        TextValue v = this.getTextValue (idparts);

        if (v != null)
        {

            return v.getSCount ();

        }

        return 0;

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

/*
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
    public static final String fkfull = "fkfull";
    public static final String frfull = "frfull";
    public static final String gffull = "gffull";
    public static final String defaultchaptername = "defaultchaptername";
    public static final String defaultstatuses = "defaultstatuses";
    public static final String editneededtype = "editneededtype";
    public static final String websiteuilanguage = "websiteuilanguage";
*/
}
