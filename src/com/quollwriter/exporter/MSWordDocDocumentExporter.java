package com.quollwriter.exporter;

import java.io.*;

import java.text.*;

import java.util.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.*;

//import org.apache.poi.pwpf.usermodel.*;
import org.apache.poi.poifs.filesystem.*;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.*;


public class MSWordDocDocumentExporter extends MSWordDocXDocumentExporter
{

    public static final String HEADING1 = "Heading1";
    public static final String TITLE = "Title";
    public static final String NORMAL = "Normal";

    private void addTo (MainDocumentPart mp,
                        NamedObject      n)
                 throws Exception
    {

        String prefix = "";
        
        // TODO: Add style.
        String text = (n.getDescription () != null ? n.getDescription ().getText () : null);

        if (n instanceof QCharacter)
        {

            QCharacter c = (QCharacter) n;

            if (text == null)
            {

                text = "";

            }

            if (c.getAliases () != null)
            {

                text = "Aliases: " + c.getAliases ();

            }

        }

        if (n instanceof Note)
        {

            Note note = (Note) n;

            if (text == null)
            {

                text = "";

            }

            if (note.getType () != null)
            {

                text = note.getType () + ": " + note.getType ();

            }

        }

        if ((!(n instanceof Chapter)) &&
            (!(n instanceof Note)))
        {

            prefix = Environment.getObjectTypeName (n.getObjectType ()) + ": ";

        } else
        {
            
            if (n instanceof Chapter)
            {

                StringWithMarkup t = ((Chapter) n).getText ();
                
                if (t.getText () != null)
                {
            
                    text = t.getText ();
                    
                }

            }
            
        }

        if (text == null)
        {

            text = "";

        }

        if (!(n instanceof OutlineItem))
        {

            mp.addStyledParagraphOfText (HEADING1,
                                         prefix + n.getName ());

        }

        StringTokenizer t = new StringTokenizer (text,
                                                 String.valueOf ('\n'));

        while (t.hasMoreTokens ())
        {

            String tok = t.nextToken ().trim ();

            if (tok.equals (""))
            {

                continue;

            }

            mp.addStyledParagraphOfText (NORMAL,
                                         tok);

        }

    }

    protected void save (WordprocessingMLPackage wordMLPackage,
                         String                  name)
                  throws Exception
    {

        // this.settings.outputDirectory.mkdirs ();

        name = name.replace ('/',
                             '_');

        name = name.replace ('\\',
                             '_');

        // File f = new File (this.settings.outputDirectory.getPath () + "/" + name + Constants.DOCX_FILE_EXTENSION);

        // wordMLPackage.save (f);

    }

    public void exportProject (File dir)
                        throws GeneralException
    {

        Project p = ExportUtils.getSelectedItems (null,
                                                  this.proj);

        ExportSettings settings = null;

        try
        {

            // HWPFDocument d = new HWPFDocument (new (POIFSFileSystem ()));

            // Range r = d.getRange ();

            if (settings.chapterExportType == ExportSettings.SINGLE_FILE)
            {

                WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage ();

                MainDocumentPart mp = wordMLPackage.getMainDocumentPart ();

                mp.addStyledParagraphOfText (TITLE,
                                             p.getName ());

                WordprocessingMLPackage notesWordMLPackage = WordprocessingMLPackage.createPackage ();

                MainDocumentPart notesmp = notesWordMLPackage.getMainDocumentPart ();

                notesmp.addStyledParagraphOfText (TITLE,
                                                  p.getName () + " - Notes");

                boolean hasNotes = false;

                WordprocessingMLPackage outlineWordMLPackage = WordprocessingMLPackage.createPackage ();

                MainDocumentPart outlinemp = outlineWordMLPackage.getMainDocumentPart ();

                outlinemp.addStyledParagraphOfText (TITLE,
                                                    p.getName () + " - Plot Outline");

                boolean hasOutline = false;

                List<Chapter> chapters = p.getBooks ().get (0).getChapters ();

                for (Chapter c : chapters)
                {

                    this.addTo (mp,
                                c);

                    notesmp.addStyledParagraphOfText (HEADING1,
                                                      c.getName ());
                    outlinemp.addStyledParagraphOfText (HEADING1,
                                                        c.getName ());

                    for (Note n : c.getNotes ())
                    {

                        hasNotes = true;

                        this.addTo (notesmp,
                                    n);

                    }

                    for (OutlineItem o : c.getOutlineItems ())
                    {

                        hasOutline = true;

                        this.addTo (outlinemp,
                                    o);

                    }

                }

                this.save (wordMLPackage,
                           p.getName ());

                if (hasNotes)
                {

                    this.save (notesWordMLPackage,
                               p.getName () + "-notes");

                }

                if (hasOutline)
                {

                    this.save (outlineWordMLPackage,
                               p.getName () + "-plot-outline");

                }

            }

            if (settings.chapterExportType == ExportSettings.INDIVIDUAL_FILE)
            {

                List<Chapter> chapters = p.getBooks ().get (0).getChapters ();

                for (Chapter c : chapters)
                {

                    WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage ();

                    MainDocumentPart mp = wordMLPackage.getMainDocumentPart ();

                    WordprocessingMLPackage notesWordMLPackage = WordprocessingMLPackage.createPackage ();

                    MainDocumentPart notesmp = notesWordMLPackage.getMainDocumentPart ();

                    notesmp.addStyledParagraphOfText (TITLE,
                                                      c.getName () + " - Notes");

                    WordprocessingMLPackage outlineWordMLPackage = WordprocessingMLPackage.createPackage ();

                    MainDocumentPart outlinemp = outlineWordMLPackage.getMainDocumentPart ();

                    outlinemp.addStyledParagraphOfText (TITLE,
                                                        c.getName () + " - Plot Outline");

                    this.addTo (mp,
                                c);

                    this.save (wordMLPackage,
                               c.getName ());

                    if (c.getNotes ().size () > 0)
                    {

                        for (Note n : c.getNotes ())
                        {

                            this.addTo (notesmp,
                                        n);

                        }

                        this.save (notesWordMLPackage,
                                   c.getName () + "-notes");

                    }

                    if (c.getOutlineItems ().size () > 0)
                    {

                        for (OutlineItem i : c.getOutlineItems ())
                        {

                            this.addTo (outlinemp,
                                        i);

                        }

                        this.save (outlineWordMLPackage,
                                   c.getName () + "-plot-outline");

                    }

                }

            }

            if (settings.otherExportType == ExportSettings.SINGLE_FILE)
            {

                WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage ();

                MainDocumentPart mp = wordMLPackage.getMainDocumentPart ();

                mp.addStyledParagraphOfText (TITLE,
                                             p.getName () + " Other Items");

                List<NamedObject> objs = new ArrayList (p.getAllNamedChildObjects ());

                Collections.sort (objs,
                                  NamedObjectSorter.getInstance ());

                for (NamedObject n : objs)
                {

                    if ((n instanceof Book) ||
                        (n instanceof Chapter))
                    {

                        continue;

                    }

                    this.addTo (mp,
                                n);

                }

                this.save (wordMLPackage,
                           p.getName () + "-other-items");

            }

            if (settings.otherExportType == ExportSettings.INDIVIDUAL_FILE)
            {

                List<NamedObject> objs = new ArrayList (p.getAllNamedChildObjects ());

                Collections.sort (objs,
                                  NamedObjectSorter.getInstance ());

                for (NamedObject n : objs)
                {

                    if ((n instanceof Book) ||
                        (n instanceof Chapter))
                    {

                        continue;

                    }

                    WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage ();

                    MainDocumentPart mp = wordMLPackage.getMainDocumentPart ();

                    this.addTo (mp,
                                n);

                    this.save (wordMLPackage,
                               Environment.getObjectTypeName (n.getObjectType ()) + " - " + n.getName ());

                }

            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to export project: " +
                                        p +
                                        " using settings: " +
                                        settings,
                                        e);

        }

    }

}
