package com.quollwriter.editors.messages;

import java.util.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.text.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;

public abstract class AbstractProjectMessage extends EditorMessage
{

    protected Set<Chapter> chapters = null;
    //protected String notes = null;
    //protected Date dueBy = null;
    protected int wordCount = 0;
    protected ProjectVersion projVer = null;
    //protected String versionName = null;
    //protected String versionId = null;

    public AbstractProjectMessage ()
    {

    }

    public AbstractProjectMessage (Project       project,
                                   Set<Chapter>  chapters,
                                   ProjectVersion pv,
                                   //Date          dueBy,
                                   //String        notes,
                                   EditorEditor  editor)
    {

        if ((chapters == null)
            ||
            (chapters.size () == 0)
           )
        {

            throw new IllegalArgumentException ("Expected at least 1 chapter.");

        }

        if (pv == null)
        {

            throw new IllegalArgumentException ("Expected project version.");

        }

        if (pv.getId () == null)
        {

            throw new IllegalArgumentException ("Project version must have an id.");

        }

        this.setEditor (editor);
        this.setForProjectName (project.getName ());
        this.setForProjectId (project.getId ());
        this.projVer = pv;
        //this.versionName = versionName;
        this.chapters = chapters;
        //this.notes = notes;
        //this.dueBy = dueBy;

        int wc = 0;

        for (Chapter c : chapters)
        {

            wc += TextUtilities.getWordCount (c.getChapterText ());

        }

        this.wordCount = wc;

    }

    public abstract Project createProject ()
                                    throws Exception;


    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "chapters",
                                    this.chapters.size ());
        this.addToStringProperties (props,
                                    "wordCount",
                                    this.wordCount);
        this.addToStringProperties (props,
                                    "dueBy",
                                    this.projVer.getDueDate ());
        this.addToStringProperties (props,
                                    "versionName",
                                    this.projVer.getName ());
        this.addToStringProperties (props,
                                    "versionId",
                                    this.projVer.getId ());
        this.addToStringProperties (props,
                                    "notes",
                                    this.projVer.getDescription ());

    }

    /**
     * Will fill up the <b>data</b> map with the information in this object that is encoded and sent in the message to the editor.
     *
     * @param data The data.
     * @throws Exception If there is an encoding issue or if there is no project id or no chapters.
     */
    protected void fillMap (Map data)
                     throws Exception
    {

        if (this.getForProjectId () == null)
        {

            throw new GeneralException ("No project id set");

        }

        if ((this.chapters == null)
            ||
            (this.chapters.size () == 0)
           )
        {

            throw new GeneralException ("No chapters set");

        }

        data.put (MessageFieldNames.name,
                  this.getForProjectName ());

        if ((this.projVer.getDescriptionText () != null)
            &&
            (this.projVer.getDescriptionText ().trim ().length () > 0)
           )
        {

            data.put (MessageFieldNames.notes,
                      this.projVer.getDescriptionText ().trim ());

        }

        if (this.projVer.getDueDate () != null)
        {

            data.put (MessageFieldNames.dueby,
                      this.projVer.getDueDate ().getTime ());

        }

        if (this.projVer.getName () != null)
        {

            data.put (MessageFieldNames.versionname,
                      this.projVer.getName ());

        }

        data.put (MessageFieldNames.versionid,
                  this.projVer.getId ());

        List<Map> clist = new ArrayList<> ();

        data.put (MessageFieldNames.chapters,
                  clist);

        // Add the chapters.
        for (Chapter c : this.chapters)
        {

            clist.add (TypeEncoder.encode (c));

        }

    }

    public String getMessage ()
                       throws GeneralException
    {

        // Return a summary, should be json.
        Map<String, Object> m = new HashMap<> ();
        m.put (MessageFieldNames.name,
               this.getForProjectName ());
        m.put (MessageFieldNames.projectid,
               this.getForProjectId ());
        m.put (MessageFieldNames.chaptercount,
               this.chapters.size ());

        if (this.projVer.getDescriptionText () != null)
        {

            m.put (MessageFieldNames.notes,
                   this.projVer.getDescriptionText ());

        }

        if (this.projVer.getDueDate () != null)
        {

            m.put (MessageFieldNames.dueby,
                   this.projVer.getDueDate ().getTime ());

        }

        m.put (MessageFieldNames.wordcount,
               this.wordCount);

        if (this.projVer.getName () != null)
        {

            m.put (MessageFieldNames.versionname,
                   this.projVer.getName ());

        }

        m.put (MessageFieldNames.versionid,
               this.projVer.getId ());

        // Get a summary of each chapter.
        List chs = new ArrayList ();

        for (Chapter c : this.chapters)
        {

            Map cm = new HashMap ();
            chs.add (cm);

            cm.put (MessageFieldNames.name,
                    c.getName ());
            cm.put (MessageFieldNames.chapterid,
                    c.getId ());
            cm.put (MessageFieldNames.version,
                    c.getVersion ());

        }

        m.put (MessageFieldNames.chapters,
               chs);

        this.fillMessageMap (m);

        try
        {

            return JSONEncoder.encode (m);

        } catch (Exception e) {

            throw new GeneralException ("Unable to json encode message: " +
                                        m,
                                        e);

        }

    }

    public void setMessage (String m)
                     throws GeneralException
    {

        // This is a summary of what was sent/received.
        Map data = (Map) JSONDecoder.decode (m);

        this.fillFromMessageMap (data);

        this.setForProjectName (this.getString (MessageFieldNames.name,
                                                data));
        this.setForProjectId (this.getString (MessageFieldNames.projectid,
                                              data));

        ProjectVersion pv = new ProjectVersion ();
        pv.setDescription (new StringWithMarkup (this.getString (MessageFieldNames.notes,
                                                                 data,
                                                                 false)));
        pv.setName (this.getString (MessageFieldNames.versionname,
                                    data,
                                    false));
        pv.setDueDate (this.getDate (MessageFieldNames.dueby,
                                     data,
                                     false));

        pv.setId (this.getString (MessageFieldNames.versionid,
                                  data));

        this.projVer = pv;

        this.wordCount = this.getInt (MessageFieldNames.wordcount,
                                      data);

        Set<Chapter> chaps = null;

        List chs = (List) data.get (MessageFieldNames.chapters);

        if (chs != null)
        {

            chaps = new LinkedHashSet ();

            for (int i = 0; i < chs.size (); i++)
            {

                Map cm = (Map) chs.get (i);

                Chapter c = TypeEncoder.decodeToChapter (cm);

                chaps.add (c);

            }

        }

        this.chapters = chaps;

    }

    /**
     * Used when converting the data in the message into a summary string.
     * Called from: {@link getMessage()}.
     *
     * @param m The map to fill.
     */
    protected abstract void fillMessageMap (Map<String, Object> m)
                                     throws GeneralException;

    /**
     * Used to extract data from the passed in map and init this object.  The data has been taken
     * from a summary string generated by {@link getMessage()}.
     * Called from: {@link setMessage(String)}
     *
     * @param m The map to extract data from.
     */
    protected abstract void fillFromMessageMap (Map m)
                                         throws GeneralException;

    public ProjectVersion getProjectVersion ()
    {

        return this.projVer;

    }

    public Set<Chapter> getChaptersWithText ()
                                      throws Exception
    {

        Map data = EditorsEnvironment.getOriginalMessageAsMap (this);

        Object chaps = this.checkTypeAndNotNull (MessageFieldNames.chapters,
                                                 data,
                                                 List.class);

        List chapsL = (List) chaps;

        if (chapsL.size () == 0)
        {

            throw new GeneralException ("Expected to find at least 1 chapter.");

        }

        int wc = 0;

        Set<Chapter> chapsS = new LinkedHashSet ();

        for (int i = 0; i < chapsL.size (); i++)
        {

            Object co = chapsL.get (i);

            if (!(co instanceof Map))
            {

                throw new GeneralException ("Expected chapter data for index: " +
                                            i +
                                            " to by a map, has type: " +
                                            co.getClass ().getName ());

            }

            Map cm = (Map) co;

            Chapter c = TypeEncoder.decodeToChapter (cm);

            chapsS.add (c);

        }

        return chapsS;

    }

    public Set<Chapter> getChapters ()
    {

        return this.chapters;

    }

    public int getWordCount ()
    {

        return this.wordCount;

    }

    public boolean isEncrypted ()
    {

        return true;

    }

    protected void doInit (Map          data,
                           EditorEditor from)
                    throws Exception
    {

        this.setForProjectName (this.getString (MessageFieldNames.name,
                                                data));

        String notes = this.getString (MessageFieldNames.notes,
                                       data,
                                       false);

        ProjectVersion pv = new ProjectVersion ();

        if (notes != null)
        {

            notes = notes.trim ();

            if (notes.length () > 5000)
            {

                throw new GeneralException ("Notes is too long, max length is: 5000, is: " + notes.length ());

            }

            pv.setDescription (new StringWithMarkup (notes));

        }

        pv.setId (this.getString (MessageFieldNames.versionid,
                                  data));
        pv.setName (this.getString (MessageFieldNames.versionname,
                                    data,
                                    false));
        pv.setDueDate (this.getDate (MessageFieldNames.dueby,
                                     data,
                                     false));

        this.projVer = pv;

        Object chaps = this.checkTypeAndNotNull (MessageFieldNames.chapters,
                                                 data,
                                                 List.class);

        List chapsL = (List) chaps;

        if (chapsL.size () == 0)
        {

            throw new GeneralException ("Expected to find at least 1 chapter.");

        }

        int wc = 0;

        Set<Chapter> chapsS = new LinkedHashSet ();

        for (int i = 0; i < chapsL.size (); i++)
        {

            Object co = chapsL.get (i);

            if (!(co instanceof Map))
            {

                throw new GeneralException ("Expected chapter data for index: " +
                                            i +
                                            " to by a map, has type: " +
                                            co.getClass ().getName ());

            }

            Map cm = (Map) co;

            Chapter c = TypeEncoder.decodeToChapter (cm);

            wc += TextUtilities.getWordCount (c.getChapterText ());

            chapsS.add (c);

        }

        this.wordCount = wc;
        this.chapters = chapsS;

    }

}
