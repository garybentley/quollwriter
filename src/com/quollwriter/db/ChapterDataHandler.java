package com.quollwriter.db;

import java.sql.*;

import java.util.*;

import javax.swing.text.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.text.*;
import com.quollwriter.text.rules.*;

public class ChapterDataHandler implements DataHandler
{

    private ObjectManager objectManager = null;
    private List<Chapter> allChapters = new ArrayList ();
    private boolean       loaded = false;

    public ChapterDataHandler(ObjectManager om)
    {

        this.objectManager = om;

    }

    public void deleteProblemFinderIgnores (Chapter    c,
                                            Connection conn)
                                     throws GeneralException
    {

        List params = new ArrayList ();
        params.add (c.getKey ());

        this.objectManager.executeStatement ("DELETE FROM problemfinderignore WHERE chapterdbkey = ?",
                                             params,
                                             conn);

    }

    public void saveProblemFinderIgnores (Chapter    c,
                                          Set<Issue> ignores)
                                   throws GeneralException
    {

        try
        {

            Connection conn = this.objectManager.getConnection ();

            // Delete everything for this chapter first.
            this.deleteProblemFinderIgnores (c,
                                             conn);

            for (Issue iss : ignores)
            {

                List params = new ArrayList ();
                params.add (c.getKey ());
                params.add (iss.getRuleId ());
                params.add (iss.getSentenceStartPosition ().getOffset ());
                params.add (iss.getStartWordPosition ());

                this.objectManager.executeStatement ("INSERT INTO problemfinderignore (chapterdbkey, ruleid, startposition, wordposition) VALUES (?, ?, ?, ?)",
                                                     params,
                                                     conn);

            }

            // Commit the changes.
            this.objectManager.releaseConnection (conn);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to update problem finder ignores for chapter: " +
                                        c,
                                        e);

        }

    }

    public Set<Issue> getProblemFinderIgnores (Chapter  c,
                                               Document doc)
                                        throws GeneralException
    {

        Set<Issue> ret = new TreeSet (new IssueSorter ());

        try
        {

            Connection conn = this.objectManager.getConnection ();

            List params = new ArrayList ();
            params.add (c.getKey ());

            ResultSet rs = this.objectManager.executeQuery ("SELECT ruleid, startposition, wordposition FROM problemfinderignore WHERE chapterdbkey = ? ORDER BY startposition, wordposition",
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

                Issue iss = new Issue (null,
                                       wordPos,
                                       -1,
                                       r);

                iss.setSentenceStartPosition (doc.createPosition (startPos));

                ret.add (iss);

            }

            try
            {

                rs.close ();

                this.objectManager.releaseConnection (conn);

            } catch (Exception e)
            {
            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load problem finder ignores for chapter: " +
                                        c,
                                        e);

        }

        return ret;

    }

    private Chapter getChapter (ResultSet rs,
                                boolean   loadChildObjects)
                         throws GeneralException
    {

        try
        {

            int ind = 1;

            int  bookKey = rs.getInt (ind++);
            long key = rs.getInt (ind++);

            // Get the book but don't load the child objects (i.e. chapters!)
            Book b = (Book) this.objectManager.getObjectByKey (Book.class,
                                                               bookKey,
                                                               rs.getStatement ().getConnection (),
                                                               false);

            Chapter c = new Chapter (b);
            c.setName (rs.getString (ind++));
            c.setKey (key);
            c.setDescription (rs.getString (ind++));

            c.setLastModified (rs.getTimestamp (ind++));
            c.setDateCreated (rs.getTimestamp (ind++));
            c.setPropertiesAsString (rs.getString (ind++));
            c.setText (rs.getString (ind++));
            c.setGoals (rs.getString (ind++));
            c.setMarkup (rs.getString (ind++));
            c.setPlan (rs.getString (ind++));
            
            /*
                    if (loadChildObjects)
                    {

                        Connection conn = rs.getStatement ().getConnection ();

                        List scenes = this.objectManager.getObjects (Scene.class,
                                                                     c,
                                                                     conn,
                                                                     loadChildObjects);

                        for (int i = 0; i < scenes.size (); i++)
                        {

                            c.addScene ((Scene) scenes.get (i));

                        }

                        // Get all the notes.
                        this.objectManager.loadNotes (c,
                                                      conn);

                        List items = this.objectManager.getObjects (OutlineItem.class,
                                                                    c,
                                                                    conn,
                                                                    loadChildObjects);

                        for (int i = 0; i < items.size (); i++)
                        {

                            c.addOutlineItem ((OutlineItem) items.get (i));

                        }

                    }
             */
            return c;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load chapter",
                                        e);

        }

    }

    public List<? extends NamedObject> getObjects (NamedObject parent,
                                                   Connection  conn,
                                                   boolean     loadChildObjects)
                                            throws GeneralException
    {

        this.loadAllChapters (conn,
                              loadChildObjects);

        List<Chapter> ret = new ArrayList ();

        for (Chapter c : this.allChapters)
        {

            // Parent should be a "book" here.
            if (c.getBook ().getKey ().equals (parent.getKey ()))
            {

                ret.add (c);

            }

        }

        return ret;
/*
        List<Chapter> ret = new ArrayList ();

        try
        {

            List params = new ArrayList ();
            params.add (parent.getKey ());

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties, text, goals FROM chapter_v WHERE bookdbkey = ? ORDER BY index",
                                                            params,
                                                            conn);

            while (rs.next ())
            {

                ret.add (this.getChapter (rs,
                                          loadChildObjects));

            }

            try
            {

                rs.close ();

            } catch (Exception e) {}

        } catch (Exception e) {

            throw new GeneralException ("Unable to load chapters for: " +
                                        parent,
                                        e);

        }

        return ret;
*/
    }

    private void loadAllChapters (Connection conn,
                                  boolean    loadChildObjects)
                           throws GeneralException
    {

        if (!this.loaded)
        {

            this.loaded = true;

            try
            {

                ResultSet rs = this.objectManager.executeQuery ("SELECT bookdbkey, dbkey, name, description, lastmodified, datecreated, properties, text, goals, markup, plan FROM chapter_v ORDER BY index",
                                                                null,
                                                                conn);

                while (rs.next ())
                {

                    this.addChapter (this.getChapter (rs,
                                                      loadChildObjects));

                }

                try
                {

                    rs.close ();

                } catch (Exception e)
                {
                }

                // Doing it this way so that if the child needs to perform the lookup then
                // we won't go into a loop.
                if (loadChildObjects)
                {

                    for (Chapter c : this.allChapters)
                    {

                        List scenes = this.objectManager.getObjects (Scene.class,
                                                                     c,
                                                                     conn,
                                                                     loadChildObjects);

                        for (int i = 0; i < scenes.size (); i++)
                        {

                            c.addScene ((Scene) scenes.get (i));

                        }

                        // Get all the notes.
                        this.objectManager.loadNotes (c,
                                                      conn);

                        List items = this.objectManager.getObjects (OutlineItem.class,
                                                                    c,
                                                                    conn,
                                                                    loadChildObjects);

                        for (int i = 0; i < items.size (); i++)
                        {

                            c.addOutlineItem ((OutlineItem) items.get (i));

                        }

                    }

                }

            } catch (Exception e)
            {

                throw new GeneralException ("Unable to load all chapters",
                                            e);

            }

        }

    }

    public NamedObject getObjectByKey (int        key,
                                       Connection conn,
                                       boolean    loadChildObjects)
                                throws GeneralException
    {

        this.loadAllChapters (conn,
                              loadChildObjects);

        for (Chapter c : this.allChapters)
        {

            if (c.getKey ().intValue () == key)
            {

                return c;

            }

        }

        return null;

/*
        Chapter i = null;

        try
        {
com.quollwriter.Environment.logError ("HERE", new Exception ());
            List params = new ArrayList ();
            params.add (key);

            ResultSet rs = this.objectManager.executeQuery ("SELECT dbkey, name, description, lastmodified, datecreated, properties, text, goals FROM chapter_v WHERE dbkey = ?",
                                                            params,
                                                            conn);

            if (rs.next ())
            {

                i = this.getChapter (rs,
                                     loadChildObjects);

            }

            try
            {

                rs.close ();

            } catch (Exception e) {}

        } catch (Exception e) {

            throw new GeneralException ("Unable to load chapter for key: " +
                                        key,
                                        e);

        }

        return i;
*/
    }

    public List<WordCount> getWordCounts (Chapter ch,
                                          int     daysPast)
                                   throws GeneralException
    {

        try
        {

            Connection conn = this.objectManager.getConnection ();

            List params = new ArrayList ();
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

            List<WordCount> ret = new ArrayList ();

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

            try
            {

                conn.close ();

            } catch (Exception e)
            {
            }

            return ret;

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to load word counts for chapter: " +
                                        ch,
                                        e);

        }

    }

    public void createObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Chapter c = (Chapter) d;

        List params = new ArrayList ();
        params.add (c.getKey ());
        params.add (c.getBook ().getKey ());
        params.add (c.getText ());
        params.add (c.getBook ().getChapterIndex (c));
        params.add (c.getGoals ());
        params.add (c.getMarkup ());
        params.add (c.getPlan ());

        this.objectManager.executeStatement ("INSERT INTO chapter (dbkey, bookdbkey, text, index, goals, markup, plan) VALUES (?, ?, ?, ?, ?, ?, ?)",
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

        this.addChapter (c);

    }

    private void addChapter (Chapter c)
    {

        this.allChapters.add (c);

    }

    public void deleteObject (DataObject d,
                              // Always ignored here.
                              boolean    deleteChildObjects,
                              Connection conn)
                       throws GeneralException
    {

        Chapter c = (Chapter) d;

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

        List params = new ArrayList ();
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

        this.allChapters.remove (c);

    }

    public void updateObject (DataObject d,
                              Connection conn)
                       throws GeneralException
    {

        Chapter c = (Chapter) d;

        List params = new ArrayList ();
        params.add (c.getText ());
        params.add (c.getBook ().getChapterIndex (c));
        params.add (c.getGoals ());
        params.add (c.getMarkup ());
        params.add (c.getPlan ());
        params.add (c.getKey ());

        this.objectManager.executeStatement ("UPDATE chapter SET text = ?, index = ?, goals = ?, markup = ?, plan = ? WHERE dbkey = ?",
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

    }

}
