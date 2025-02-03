package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import javax.swing.text.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.text.*;
import com.quollwriter.text.rules.*;

public class ChapterDataHandler implements DataHandler<Chapter, Book>
{

    private final String STD_SELECT_PREFIX = "SELECT bookdbkey, dbkey, userobjecttypedbkey, name, description, descriptionmarkup, files, lastmodified, datecreated, properties, text, markup, goals, goalsmarkup, plan, planmarkup, editposition, editcomplete, id, version, latest FROM chapter_v ";

    private ObjectManager objectManager = null;

    public ChapterDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    private void deleteProblemFinderIgnores (Chapter    c,
                                             Connection conn)
                                      throws GeneralException
    {

        List<Object> params = new ArrayList<> ();
        params.add (c.getKey ());

        this.objectManager.executeStatement ("DELETE FROM problemfinderignore WHERE chapterdbkey = ?",
                                             params,
                                             conn);

    }

    public void saveProblemFinderIgnores (Chapter    c,
                                          Connection conn)
                                   throws GeneralException
    {

        boolean releaseConn = false;

        if (conn == null)
        {

            conn = this.objectManager.getConnection ();
            releaseConn = true;

        }

        try
        {

            // Delete everything for this chapter first.
            this.deleteProblemFinderIgnores (c,
                                             conn);

            for (Issue iss : c.getProblemFinderIgnores ())
            {

                List<Object> params = new ArrayList<> ();
                params.add (c.getKey ());
                params.add (iss.getRuleId ());
                params.add (iss.getIssueId ());
                params.add (iss.getStartPosition ().getOffset ());
                params.add (iss.getStartIssuePosition ());

                this.objectManager.executeStatement ("INSERT INTO problemfinderignore (chapterdbkey, ruleid, issueid, startposition, wordposition) VALUES (?, ?, ?, ?, ?)",
                                                     params,
                                                     conn);

            }

        } catch (Exception e)
        {

            this.objectManager.throwException (conn,
                                               "Unable to update problem finder ignores for chapter: " +
                                               c,
                                               e);

        } finally {

            if (releaseConn)
            {

                this.objectManager.releaseConnection (conn);

            }

        }

    }

    private Set<Issue> getProblemFinderIgnores (Chapter  c,
                                                Document doc)
                                         throws GeneralException
    {

        return this.getProblemFinderIgnores (c,
                                             doc,
                                             null);

    }

    private Set<Issue> getProblemFinderIgnores (Chapter    c,
                                                Document   doc,
                                                Connection conn)
                                         throws GeneralException
    {

        Set<Issue> ret = new HashSet<> (); //new TreeSet (new IssueSorter ());

        boolean releaseConn = false;

        if (conn == null)
        {

            conn = this.objectManager.getConnection ();
            releaseConn = true;

        }

        try
        {

            List<Object> params = new ArrayList<> ();
            params.add (c.getKey ());

            ResultSet rs = this.objectManager.executeQuery ("SELECT ruleid, startposition, wordposition, issueid FROM problemfinderignore WHERE chapterdbkey = ? ORDER BY startposition, wordposition",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                int ind = 1;

                String ruleId = rs.getString (ind++);

                Rule r = RuleFactory.getRuleById (ruleId);

                if (r == null)
                {

                    continue;

                }

                int startPos = rs.getInt (ind++);
                int wordPos = rs.getInt (ind++);

                // Legacy, pre v2.3
                String issueId = rs.getString (ind++);

                if (issueId == null)
                {

                    issueId = startPos + "";

                }

                Issue iss = new Issue (null,
                                       null,
                                       wordPos,
                                       -1,
                                       issueId,
                                       r);

                if (doc != null)
                {

                    iss.setStartPosition (doc.createPosition (startPos));

                }

                iss.setChapter (c);

                ret.add (iss);

            }

            return ret;

        } catch (Exception e)
        {

            this.objectManager.throwException (conn,
                                               "Unable to load problem finder ignores for chapter: " +
                                               c,
                                               e);

        } finally {

            if (releaseConn)
            {

                this.objectManager.releaseConnection (conn);

            }

        }

        return null;

    }

    private Chapter getChapter (ResultSet rs,
                                Book      book,
                                boolean   loadChildObjects)
                         throws GeneralException
    {

        try
        {

            int ind = 1;

            int  bookKey = rs.getInt (ind++);
            long key = rs.getInt (ind++);

            long userObjTypeKey = rs.getLong (ind++);

            Chapter c = new Chapter (book);
            c.setKey (key);

            // Load the object fields.
            this.objectManager.setUserConfigurableObjectFields (c,
                                                                rs.getStatement ().getConnection ());

            c.setName (rs.getString (ind++));

            c.setDescription (new StringWithMarkup (rs.getString (ind++),
                                                    rs.getString (ind++)));
            c.setFiles (Utils.getFilesFromXML (rs.getString (ind++)));

            c.setLastModified (rs.getTimestamp (ind++));
            c.setDateCreated (rs.getTimestamp (ind++));
            c.setPropertiesAsString (rs.getString (ind++));

            String t = rs.getString (ind++);

            c.setText (new StringWithMarkup (t,
                                             rs.getString (ind++)));

            c.setGoals (new StringWithMarkup (rs.getString (ind++),
                                              rs.getString (ind++)));

            c.setPlan (new StringWithMarkup (rs.getString (ind++),
                                             rs.getString (ind++)));
            c.setEditPosition (rs.getInt (ind++));

            // Ensure that the edit position is valid (not sure why this can happen).
            // If it's not then set it to the end of the text.
            if ((c.getEditPosition () > 0)
                &&
                (c.getText ().hasText ())
                &&
                (c.getEditPosition () > c.getText ().getText ().length ())
               )
            {

                c.setEditPosition (c.getText ().getText ().length ());

            }

            c.setEditComplete (rs.getBoolean (ind++));
            c.setId (rs.getString (ind++));
            c.setVersion (rs.getString (ind++));

            if (book != null)
            {

                // Need to check the index?
                book.addChapter (c);

            }

            c.setProblemFinderIgnores (this.getProblemFinderIgnores (c,
                                                                     null,
                                                                     rs.getStatement ().getConnection ()));

            /*
            long pvkey = rs.getInt (ind++);

            if (pvkey > 0)
            {

                ProjectVersion pv = (ProjectVersion) this.objectManager.getObjectByKey (ProjectVersion.class,
                                                                   bookKey,
                                                                   rs.getStatement ().getConnection (),
                                                                   false);

                c.setProjectVersion (pv);

            }
            */
            c.setLatest (rs.getBoolean (ind++));

            if (loadChildObjects)
            {

                Connection conn = rs.getStatement ().getConnection ();

                this.objectManager.getObjects (Scene.class,
                                               c,
                                               conn,
                                               loadChildObjects);

                // Get all the notes.
                this.objectManager.loadNotes (c,
                                              conn);

                this.objectManager.getObjects (OutlineItem.class,
                                               c,
                                               conn,
                                               loadChildObjects);

            }

            return c;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load chapter",
                                        e);

        }

    }

    public Set<Chapter> getChaptersForVersion (ProjectVersion pv,
                                               Book           parent,
                                               Connection     conn,
                                               boolean        loadChildObjects)
                                        throws GeneralException
    {

        Set<Chapter> ret = new LinkedHashSet<> ();

        boolean closeConn = false;

        if (conn == null)
        {

            conn = this.objectManager.getConnection ();

            closeConn = true;

        }

        try
        {

            List<Object> params = new ArrayList<> ();
            params.add (pv.getKey ());

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE projectversiondbkey = ? ORDER BY index",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getChapter (rs,
                                          parent,
                                          loadChildObjects));

            }

            rs.close ();

            return ret;

        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to get chapters with version: " +
                                               pv.getKey (),
                                               e);

        } finally {

            if (closeConn)
            {

                this.objectManager.releaseConnection (conn);

            }

        }

        return null;

    }

    public Set<Chapter> getVersionedChapters (Collection<Chapter> chaps)
                                        throws Exception
    {

        List<Object> params = new ArrayList<> ();

        StringBuilder b = new StringBuilder ();

        for (Chapter c : chaps)
        {

            if (b.length () > 0)
            {

                b.append (" OR ");

            }

            b.append ("(id = ? AND version = ?)");

            params.add (c.getId ());
            params.add (c.getVersion ());

        }

        Set<Chapter> ret = new LinkedHashSet<> ();

        Connection conn = null;

        try
        {

            conn = this.objectManager.getConnection ();

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + "WHERE " + b + " ORDER BY index",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getChapter (rs,
                                          null,
                                          false));

            }

            return ret;

        } catch (Exception e) {

            this.objectManager.throwException (conn,
                                               "Unable to get versioned chapters for: " +
                                               chaps,
                                               e);

        } finally {

            this.objectManager.releaseConnection (conn);

        }

        return null;

    }

    @Override
    public List<Chapter> getObjects (Book        book,
                                     Connection  conn,
                                     boolean     loadChildObjects)
                              throws GeneralException
    {

        ProjectVersion pv = book.getProject ().getProjectVersion ();

        if ((pv != null)
            &&
            (!pv.isLatest ())
           )
        {

            // Get the chapters at the specific version.
            return new ArrayList<Chapter> (this.getChaptersForVersion (pv,
                                                                       book,
                                                                       conn,
                                                                       loadChildObjects));

        }

        try
        {

            List<Chapter> ret = new ArrayList<> ();

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + "WHERE latest = TRUE ORDER BY index",
                                                            null,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getChapter (rs,
                                          book,
                                          loadChildObjects));

            }

            rs.close ();

            return ret;

        } catch (Exception e) {

            throw new GeneralException ("Unable to get latest chapters for book: " +
                                        book,
                                        e);

        }

    }

    public Chapter getObjectByKey (long       key,
                                   Book       book,
                                   Connection conn,
                                   boolean    loadChildObjects)
                            throws GeneralException
    {

        try
        {

            List<Object> params = new ArrayList<> ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + "WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                return this.getChapter (rs,
                                        book,
                                        loadChildObjects);

            }

            return null;

        } catch (Exception e) {

            throw new GeneralException ("Unable to get chapter with key: " +
                                        key,
                                        e);

        }

    }

    // TODO: Pass in a list of pre-calculated ChapterCounts.
    public void saveWordCounts (Project        project,
                                java.util.Date start,
                                java.util.Date end)
    {

        if ((project == null) ||
            (project.getKey () == null))
        {

            return;

        }

        Connection conn = null;

        try
        {

            List<Object> wcs = new ArrayList<> ();

            conn = this.objectManager.getConnection ();

            start = Utils.zeroTimeFieldsForDate (start);
            end = Utils.zeroTimeFieldsForDate (end);

            List<Object> params = new ArrayList<> ();
            params.add (start);
            params.add (project.getKey ());

            this.objectManager.executeStatement ("DELETE FROM wordcount WHERE start = ? AND projectdbkey = ?",
                                                 params,
                                                 conn);

            for (Book book : project.getBooks ())
            {

                for (Chapter c : book.getChapters ())
                {

                    ChapterCounts cc = new ChapterCounts (c.getChapterText ());

                    params = new ArrayList<> ();
                    params.add (project.getKey ());

                    params.add (c.getKey ());

                    params.add (cc.getWordCount ());
                    params.add (start);
                    params.add (end);

                    this.objectManager.executeStatement ("INSERT INTO wordcount (projectdbkey, chapterdbkey, count, start, end) VALUES (?, ?, ?, ?, ?)",
                                                         params,
                                                         conn);

                }

            }

        } catch (Exception e)
        {

            try
            {

                this.objectManager.throwException (conn,
                                                   "Unable to save session word counts for project: " +
                                                   project,
                                                   e);

            } catch (Exception ee) {

                Environment.logError ("Unable to save session word counts, see cause for details",
                                      ee);

            }

        } finally
        {

            this.objectManager.releaseConnection (conn);

        }

    }

    public List<WordCount> getWordCounts (Chapter ch,
                                          int     daysPast)
                                   throws GeneralException
    {

        Connection conn = null;

        try
        {

            conn = this.objectManager.getConnection ();

            List<Object> params = new ArrayList<> ();
            params.add (ch.getKey ());
            params.add (ch.getBook ().getProject ().getKey ());

            String whereDays = "";

            if (daysPast != 0)
            {

                whereDays = " AND end > DATEADD ('DAY', ?, CURRENT_DATE) ";
                params.add (daysPast);

            }

            ResultSet rs = this.objectManager.executeQuery ("SELECT count, end FROM wordcount WHERE chapterdbkey = ? AND projectdbkey = ? " + whereDays + " GROUP BY end, count ORDER BY end",
                                                            params,
                                                            conn);

            List<WordCount> ret = new ArrayList<> ();

            while (rs.next ())
            {

                int ind = 1;

                WordCount c = new WordCount ();
                c.setCount (rs.getInt (ind++));
                c.setEnd (rs.getTimestamp (ind++));
                c.setChapter (ch);
                c.setProject (ch.getBook ().getProject ());

                ret.add (c);

            }

            return ret;

        } catch (Exception e)
        {

            this.objectManager.throwException (conn,
                                               "Unable to load word counts for chapter: " +
                                               ch,
                                               e);

        } finally {

            this.objectManager.releaseConnection (conn);

        }

        return null;

    }

    @Override
    public void createObject (Chapter    c,
                              Connection conn)
                       throws GeneralException
    {

        List<Object> params = new ArrayList<> ();
        params.add (c.getKey ());
        params.add (c.getBook ().getKey ());

        StringWithMarkup d = c.getText ();

        String t = null;
        String m = null;

        if (d != null)
        {

            t = d.getText ();
            m = (d.getMarkup () != null ? d.getMarkup ().toString () : null);

        }

        params.add (t);
        params.add (m);
        params.add (c.getBook ().getChapterIndex (c));

        t = null;
        m = null;

        StringWithMarkup g = c.getGoals ();

        if (g != null)
        {

            t = g.getText ();
            m = (g.getMarkup () != null ? g.getMarkup ().toString () : null);

        }

        params.add (t);
        params.add (m);

        t = null;
        m = null;

        StringWithMarkup p = c.getPlan ();

        if (p != null)
        {

            t = p.getText ();
            m = (p.getMarkup () != null ? p.getMarkup ().toString () : null);

        }

        params.add (t);
        params.add (m);

        params.add (c.getEditPosition ());
        params.add (c.isEditComplete ());

        if (c.getProjectVersion () != null)
        {

            params.add (c.getProjectVersion ().getKey ());

        } else {

            params.add (null);

        }

        this.objectManager.executeStatement ("INSERT INTO chapter (dbkey, bookdbkey, text, markup, index, goals, goalsmarkup, plan, planmarkup, editposition, editcomplete, projectversiondbkey) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                             params,
                                             conn);

        BookDataHandler bdh = (BookDataHandler) this.objectManager.getHandler (Book.class);

        bdh.updateChapterIndexes (c.getBook (),
                                  conn);

        for (Scene s : c.getScenes ())
        {

            this.objectManager.saveObject (s,
                                           conn);

        }

        for (OutlineItem i : c.getOutlineItems ())
        {

            this.objectManager.saveObject (i,
                                           conn);

        }

    }

    public Set<Chapter> snapshot (Set<Chapter>   chapters,
                                  ProjectVersion pv)
                           throws GeneralException
    {

        if (pv == null)
        {

            throw new GeneralException ("Expected a project version");

        }

        Set<Chapter> newChapters = new LinkedHashSet<> ();

        Connection conn = null;

        try
        {

            conn = this.objectManager.getConnection ();

            if (pv.getKey () == null)
            {

                this.objectManager.saveObject (pv,
                                               conn);

            }

            for (Chapter c : chapters)
            {

                newChapters.add (this.snapshot (c,
                                                pv,
                                                conn));

            }

            return newChapters;

        } catch (Exception e)
        {

            this.objectManager.throwException (conn,
                                               "Unable to snapshot chapters",
                                               e);

        } finally {

            this.objectManager.releaseConnection (conn);

        }

        return null;

    }

    public Chapter getChapterById (String     id,
                                   Connection conn)
                            throws GeneralException
    {

        if (id == null)
        {

            return null;

        }

        boolean closeConn = false;

        if (conn == null)
        {

            conn = this.objectManager.getConnection ();

            closeConn = true;

        }

        try
        {

            List<Object> params = new ArrayList<> ();
            params.add (id);

            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE id = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                return this.getChapter (rs,
                                        null,
                                        false);

            }

        } catch (Exception e)
        {

            this.objectManager.throwException (conn,
                                               "Unable to get chapter with id: " +
                                               id,
                                               e);

        } finally {

            if (closeConn)
            {

                this.objectManager.releaseConnection (conn);

            }

        }

        return null;

    }

    public Set<Chapter> updateToNewVersions (ProjectVersion      pv,
                                             Collection<Chapter> chapters)
                                      throws GeneralException
    {

        Set<Chapter> newChapters = new LinkedHashSet<> ();

        Connection conn = null;

        try
        {

            conn = this.objectManager.getConnection ();

            if (pv != null)
            {

                this.objectManager.saveObject (pv,
                                               conn);

            }

            // Need to set all existing chapters to be not the latest.
            List<Object> params = new ArrayList<> ();
            params.add (Chapter.OBJECT_TYPE);

            this.objectManager.executeStatement ("UPDATE dataobject SET latest = FALSE WHERE objecttype = ? AND dbkey IN (SELECT dbkey FROM chapter)",
                                                 params,
                                                 conn);

            final List<Chapter> chaps = new ArrayList<> (chapters);

            // TODO: Handle multiple books.  This is "ok" for now, i.e. assume a single book.
            Book b = this.objectManager.getProject ().getBook (0);

            for (Chapter c : chaps)
            {

                Chapter oc = this.getChapterById (c.getId (),
                                                  conn);

                if (oc != null)
                {

                    c.setProjectVersion (pv);
                    c.setBook (b);

                    newChapters.add (this.updateToNewVersion (c,
                                                              conn));

                } else {

                    // Create an entirely new chapter.
                    Chapter newc = new Chapter (b);

                    newc.setName (c.getName ());
                    newc.setDescription (c.getDescription ());
                    newc.setId (c.getId ());
                    newc.setVersion (c.getVersion ());
                    newc.setText (c.getText ());
                    newc.setPlan (c.getPlan ());
                    newc.setGoals (c.getGoals ());
                    newc.setLatest (true);
                    newc.setProjectVersion (pv);

                    this.objectManager.saveObject (newc,
                                                   conn);

                    newChapters.add (newc);

                }

            }

            return newChapters;

        } catch (Exception e)
        {

            this.objectManager.throwException (conn,
                                               "Unable to update chapters",
                                               e);

        } finally {

            this.objectManager.releaseConnection (conn);

        }

        return null;

    }

    /**
     * Updates an existing chapter (<b>oldVer</b>) to a new version given the information in <b>newVer</b>.
     * The id and version of the chapter are taken from <b>newVer</b> but a new key is assigned thus creating a new object.
     * This method should be used when updating an editor project and the id/version need to stay the same so
     * that comments can be sent back to the author.
     *
     * @param newVer The new chapter information.
     * @param conn The connection to use.
     * @returns A new chapter object that represents the new version.
     * @throws An exception if something goes wrong.
     */
    public Chapter updateToNewVersion (final Chapter    newVer,
                                       final Connection conn)
                                throws GeneralException
    {

        if (conn == null)
        {

            throw new IllegalArgumentException ("No connection provided.");

        }

        Chapter oldVer = this.getChapterById (newVer.getId (),
                                              conn);

        if (oldVer == null)
        {

            throw new GeneralException ("Unable to find existing chapter with id: " +
                                        newVer.getId ());

        }

        Chapter newc = new Chapter (newVer.getBook ());

        newc.setName (newVer.getName ());
        newc.setDescription (newVer.getDescription ());
        newc.setId (newVer.getId ());
        newc.setVersion (newVer.getVersion ());
        newc.setText (newVer.getText ());
        newc.setPlan (newVer.getPlan ());
        newc.setGoals (newVer.getGoals ());
        newc.setProjectVersion (newVer.getProjectVersion ());
        newc.setLatest (true);

        this.objectManager.saveObject (newc,
                                       conn);

        // Make all the existing notes to be not the latest.
        ((NoteDataHandler) this.objectManager.getHandler (Note.class)).setObjectNotesToLatest (oldVer,
                                                                                               false,
                                                                                               conn);

        this.objectManager.setLatestVersion (oldVer,
                                             false,
                                             conn);

        this.objectManager.setLatestVersion (newc,
                                             true,
                                             conn);

        return newc;


    }

    public Chapter snapshot (final Chapter        c,
                             final ProjectVersion pv,
                                   Connection     conn)
                      throws GeneralException
    {

        if (conn == null)
        {

            throw new IllegalArgumentException ("No connection provided.");

        }

        Book b = new Book (c.getBook ().getProject ())
        {

            public int getChapterIndex (Chapter chap)
            {

                return c.getBook ().getChapterIndex (c);

            }

        };

        b.setKey (c.getBook ().getKey ());

        Chapter newc = new Chapter (b);
        newc.setBook (b);
        newc.setName (c.getName ());
        newc.setDescription (c.getDescription ());
        newc.setId (c.getId ());
        newc.setText (c.getText ());
        newc.setPlan (c.getPlan ());
        newc.setGoals (c.getGoals ());
        newc.setEditPosition (c.getEditPosition ());
        newc.setEditComplete (c.isEditComplete ());
        newc.setLatest (false);
        newc.setProjectVersion (pv);

        this.objectManager.saveObject (newc,
                                       conn);

        this.objectManager.setLatestVersion (newc,
                                             false,
                                             conn);

        return newc;

    }

    @Override
    public void deleteObject (Chapter    c,
                              // Always ignored here.
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        if (!c.isLatest ())
        {

            List<Object> params = new ArrayList<> ();
            params.add (c.getKey ());

            this.objectManager.executeStatement ("DELETE FROM chapter WHERE dbkey = ?",
                                                 params,
                                                 conn);

            return;

        }

        List<Object> params = new ArrayList<> ();
        params.add (c.getId ());

        try
        {

            // Delete any "other" versions.
            ResultSet rs = this.objectManager.executeQuery (STD_SELECT_PREFIX + " WHERE id = ? AND latest = FALSE",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                this.objectManager.deleteObject (this.getChapter (rs,
                                                                  null,
                                                                  false),
                                                 true,
                                                 conn);

            }

            rs.close ();

        } catch (Exception e) {

            throw new GeneralException ("Unable to delete versioned chapters",
                                        e);

        }

        // Delete the outline items.
        for (OutlineItem i : c.getOutlineItems ())
        {

            this.objectManager.deleteObject (i,
                                             true,
                                             conn);

        }

        // Delete the scenes.
        for (Scene s : c.getScenes ())
        {

            this.objectManager.deleteObject (s,
                                             true,
                                             conn);

        }

        // Delete the problem finder ignores.
        this.deleteProblemFinderIgnores (c,
                                         conn);

        params = new ArrayList<> ();
        params.add (c.getKey ());

        // Delete the word counts.
        this.objectManager.executeStatement ("DELETE FROM wordcount WHERE chapterdbkey = ?",
                                             params,
                                             conn);

        this.objectManager.executeStatement ("DELETE FROM chapter WHERE dbkey = ?",
                                             params,
                                             conn);

        BookDataHandler bdh = (BookDataHandler) this.objectManager.getHandler (Book.class);

        bdh.updateChapterIndexes (c.getBook (),
                                  conn);

    }

    @Override
    public void updateObject (Chapter    c,
                              Connection conn)
                       throws GeneralException
    {

        StringWithMarkup d = c.getText ();

        String t = null;
        String m = null;

        if (d != null)
        {

            t = d.getText ();
            m = (d.getMarkup () != null ? d.getMarkup ().toString () : null);

        }

        List<Object> params = new ArrayList<> ();
        params.add (t);
        params.add (m);
        params.add (c.getBook ().getChapterIndex (c));

        t = null;
        m = null;

        StringWithMarkup g = c.getGoals ();

        if (g != null)
        {

            t = g.getText ();
            m = (g.getMarkup () != null ? g.getMarkup ().toString () : null);

        }

        params.add (t);
        params.add (m);

        t = null;
        m = null;

        StringWithMarkup p = c.getPlan ();

        if (p != null)
        {

            t = p.getText ();
            m = (p.getMarkup () != null ? p.getMarkup ().toString () : null);

        }

        params.add (t);
        params.add (m);
        params.add (c.getEditPosition ());
        params.add (c.isEditComplete ());
        params.add (c.getKey ());

        this.objectManager.executeStatement ("UPDATE chapter SET text = ?, markup = ?, index = ?, goals = ?, goalsmarkup = ?, plan = ?, planmarkup = ?, editposition = ?, editcomplete = ? WHERE dbkey = ?",
                                             params,
                                             conn);

        BookDataHandler bdh = (BookDataHandler) this.objectManager.getHandler (Book.class);

        bdh.updateChapterIndexes (c.getBook (),
                                  conn);

        for (OutlineItem i : c.getOutlineItems ())
        {

            this.objectManager.saveObject (i,
                                           conn);

        }

        for (Scene s : c.getScenes ())
        {

            this.objectManager.saveObject (s,
                                           conn);

        }

        this.saveProblemFinderIgnores (c,
                                       conn);

    }

}
