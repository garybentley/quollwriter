package com.quollwriter.exporter;

import java.awt.Color;
import java.awt.event.*;

import java.io.*;

import java.text.*;

import java.util.*;
import java.util.zip.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import nl.siegmann.epublib.epub.*;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Resource;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.Markup;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.text.*;


public class EPUBDocumentExporter extends AbstractDocumentExporter
{

    // private ExportSettings settings = null;
    private ZipOutputStream zout = null;

    private JTextField author = null;
    private JTextField id = null;

    public EPUBDocumentExporter()
    {

    }

    public String getStartStage ()
    {

        return "select-items";

    }

    public WizardStep getStage (String stage)
    {

        final EPUBDocumentExporter _this = this;

        WizardStep ws = new WizardStep ();

        if (stage.equals ("select-items"))
        {

            ws.title = "Select the items you wish to export";

            ws.helpText = "Select the items you wish to export, if you select any {chapters} then any associated {notes} and {outlineitems} will also be exported.  {Locations}, {characters}, {objects} and {researchitems} will be added as appendices.";

            this.initItemsTree (null);

            JScrollPane sp = new JScrollPane (this.itemsTree);

            sp.setOpaque (false);
            sp.getViewport ().setOpaque (false);
            sp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            sp.setBorder (new LineBorder (new Color (127,
                                                     127,
                                                     127),
                                          1));

            ws.panel = sp;

        }

        if (stage.equals ("details"))
        {

            ws.title = "Enter the details about the book";
            ws.helpText = "You should provide either the ISBN of your book or a unique url for the book as the ID.";

            FormLayout fl = new FormLayout ("10px, right:p, 6px, 200px, fill:10px",
                                            "p, 6px, p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            this.author = UIUtils.createTextField ();

            builder.addLabel ("Author Name",
                              cc.xy (2,
                                     1));
            builder.add (this.author,
                         cc.xy (4,
                                1));

            this.author.setText (this.proj.getProperty (Constants.AUTHOR_NAME_PROPERTY_NAME));

            builder.addLabel ("ID (ISBN/URL)",
                              cc.xy (2,
                                     3));

            this.id = UIUtils.createTextField ();

            builder.add (this.id,
                         cc.xy (4,
                                3));

            this.id.setText (this.proj.getProperty (Constants.BOOK_ID_PROPERTY_NAME));

            ws.panel = builder.getPanel ();

        }

        return ws;

    }

    public String getNextStage (String currStage)
    {

        if (currStage == null)
        {

            return "select-items";

        }

        if (currStage.equals ("select-items"))
        {

            return "details";

        }

        return null;

    }

    public String getPreviousStage (String currStage)
    {

        if (currStage == null)
        {

            return null;

        }

        if (currStage.equals ("details"))
        {

            return "select-items";

        }

        return null;

    }

    private void addEntry (String name,
                           String content)
                    throws Exception
    {

        byte[] bytes = content.getBytes ("utf-8");

        ZipEntry ze = new ZipEntry (name);

        if (name.equals ("mimetype"))
        {

            this.zout.setLevel (ZipEntry.STORED);

        } else
        {

            this.zout.setLevel (ZipEntry.DEFLATED);

        }

        this.zout.putNextEntry (ze);

        this.zout.write (bytes,
                         0,
                         bytes.length);

    }

    private String sanitizeName (String name)
    {

        name = Utils.sanitizeForFilename (name);
    /*
        name = name.replace ('/',
                             '_').replace ('\\',
                                           '_').replace ('*',
                                                         '_').replace ('"',
                                                                       '_').replace (':',
                                                                                     '_').replace ('<',
                                                                                                   '_').replace ('>',
                                                                                                                 '_').replace ('?',
                                                                                                                               '_').replace (' ',
                                                                                                                                             '_');

        if (name.endsWith ("."))
        {

            name = name.substring (0,
                                   name.length () - 1);

        }
*/
        return name;

    }

    public void exportProject (File dir)
                        throws GeneralException
    {

        try
        {
    
            Project p = ExportUtils.getSelectedItems (this.itemsTree,
                                                      this.proj);
        
            // Create new Book
            nl.siegmann.epublib.domain.Book book = new nl.siegmann.epublib.domain.Book ();
             
            // Set the title
            book.getMetadata ().addTitle (this.proj.getName ());
             
            // Add an Author
            book.getMetadata ().addAuthor (new Author (this.author.getText ()));
             
            // Set cover image
            //book.getMetadata().setCoverImage(new Resource(Simple1.class.getResourceAsStream("/book1/test_cover.png"), "cover.png"));
    
            String css = Environment.getResourceFileAsString ("/data/export/epub/css-template.xml");
    
            css = StringUtils.replaceString (css,
                                             "[[FONT_NAME]]",
                                             this.proj.getProperty (Constants.EDITOR_FONT_PROPERTY_NAME));
            css = StringUtils.replaceString (css,
                                             "[[FONT_SIZE]]",
                                             this.proj.getPropertyAsInt (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME) + "pt");
            css = StringUtils.replaceString (css,
                                             "[[LINE_SPACING]]",
                                             (100 * this.proj.getPropertyAsFloat (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME)) + "%");
            css = StringUtils.replaceString (css,
                                             "[[ALIGN]]",
                                             this.proj.getProperty (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME).toLowerCase ());
    
            String indent = "0px";
    
            if (this.proj.getPropertyAsBoolean (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME))
            {
    
                indent = "5em";
    
            }
    
            css = StringUtils.replaceString (css,
                                             "[[INDENT]]",
                                             indent);
    
            book.getResources ().add (new Resource (new ByteArrayInputStream (css.getBytes ()),
                                                    "main.css"));        
            
            Book b = this.proj.getBook (0);

            String cTemp = Environment.getResourceFileAsString ("/data/export/epub/chapter-template.xml");
    
            List<Chapter> chapters = b.getChapters ();
    
            int count = 0;
            
            for (Chapter c : chapters)
            {
    
                count++;

                String chapterText = StringUtils.replaceString (cTemp,
                                                                "[[TITLE]]",
                                                                c.getName ());
                
                StringBuilder ct = new StringBuilder ();

                String t = c.getText ();

                String m = c.getMarkup ();

                if (m != null)
                {

                    // Get the markup, if present.
                    Markup mu = new Markup (m);

                    Iterator<Markup.MarkupItem> iter = mu.iterator ();

                    Markup.MarkupItem last = null;

                    while (iter.hasNext ())
                    {

                        last = iter.next ();

                        String st = t.substring (last.start,
                                                 last.end);

                        boolean styled = last.isStyled ();
                        
                        if (styled)
                        {

                            ct.append ("<span class=\"");
                            ct.append (last.getStyles (" "));
                            ct.append ("\">");

                            ct.append (StringUtils.replaceString (st,
                                                                  String.valueOf ('\n'),
                                                                  "<br />"));

                            ct.append ("</span>");

                        } else {

                            ct.append (st);
                        
                        }
                            
                    }

                    if (last.end < t.length ())
                    {

                        ct.append (t.substring (last.end));

                    }

                } else
                {

                    ct.append (t);

                }

                // Split the text on new line, for each one output a p tag if not empty.

                // Get the text and split it.
                ParagraphIterator it = new ParagraphIterator (ct.toString ());
                it.init (0);

                ct = new StringBuilder ();

                boolean lastWasText = false;
                
                String tok = null;
                
                while ((tok = it.next ()) != null)
                {

                    //String tok = it.next ();

                    if ((tok.equals (String.valueOf ('\n')))
                        ||
                        (tok.trim ().length () == 0)
                       )
                    {
                                            
                        ct.append ("<p>&nbsp;</p>");
                        
                    } else {
                                        
                        ct.append ("<p>" + tok.trim () + "</p>");
                        
                    }

                }
                
                chapterText = StringUtils.replaceString (chapterText,
                                                         "[[CONTENT]]",
                                                         ct.toString ());

                book.addSection (c.getName (),
                                 new Resource (new ByteArrayInputStream (chapterText.getBytes ()),
                                               "chapter" + count + ".html"));
            
            }

            String appendixTemp = Environment.getResourceFileAsString ("/data/export/epub/appendix-template.xml");

            // Get the characters.
            List<QCharacter> characters = this.proj.getCharacters ();

            if (characters.size () > 0)
            {

                String cid = "appendix-a-characters";

                String title = "Appendix A - Characters";

                String t = StringUtils.replaceString (appendixTemp,
                                                      "[[TITLE]]",
                                                      title);
                t = StringUtils.replaceString (t,
                                               "[[CONTENT]]",
                                               this.getAssetsPage (characters));

                book.addSection (title,
                                 new Resource (new ByteArrayInputStream (t.getBytes ()),
                                               cid + ".html"));
                                               
            }

            // Get the locations.
            List<Location> locs = this.proj.getLocations ();

            if (locs.size () > 0)
            {

                String cid = "appendix-b-locations";

                String title = "Appendix B - Locations";

                String t = StringUtils.replaceString (appendixTemp,
                                                      "[[TITLE]]",
                                                      title);
                t = StringUtils.replaceString (t,
                                               "[[CONTENT]]",
                                               this.getAssetsPage (locs));

                book.addSection (title,
                                 new Resource (new ByteArrayInputStream (t.getBytes ()),
                                               cid + ".html"));

            }

            // Get the objects.
            List<QObject> objs = this.proj.getQObjects ();

            if (objs.size () > 0)
            {

                String cid = "appendix-c-items";

                String title = "Appendix C - Items";

                String t = StringUtils.replaceString (appendixTemp,
                                                      "[[TITLE]]",
                                                      title);
                t = StringUtils.replaceString (t,
                                               "[[CONTENT]]",
                                               this.getAssetsPage (objs));

                book.addSection (title,
                                 new Resource (new ByteArrayInputStream (t.getBytes ()),
                                               cid + ".html"));

            }

            // Get the research items.
            List<ResearchItem> res = this.proj.getResearchItems ();

            if (res.size () > 0)
            {

                String cid = "appendix-d-research";

                String title = "Appendix D - Research";

                String t = StringUtils.replaceString (appendixTemp,
                                                      "[[TITLE]]",
                                                      title);
                t = StringUtils.replaceString (t,
                                               "[[CONTENT]]",
                                               this.getAssetsPage (res));

                book.addSection (title,
                                 new Resource (new ByteArrayInputStream (t.getBytes ()),
                                               cid + ".html"));

            }
                      
            // Create EpubWriter
            EpubWriter epubWriter = new EpubWriter ();
             
            // Write the Book as Epub
            epubWriter.write (book, new FileOutputStream (new File (dir.getPath () + "/" + this.sanitizeName (this.proj.getName ()) + Constants.EPUB_FILE_EXTENSION)));

        } catch (Exception e) {
            
            throw new GeneralException ("Unable to export project: " +
                                        this.proj,
                                        e);            
            
        }
        
    }
    
    private String getAssetsPage (List<? extends Asset> assets)
    {

        StringBuilder buf = new StringBuilder ();

        for (Asset a : assets)
        {

            buf.append ("<h2>");
            buf.append (a.getName ());
            buf.append ("</h2>");

            if (a instanceof QObject)
            {

                QObject q = (QObject) a;

                buf.append ("<h3>Type: " + q.getType () + "</h3>");

            }

            if (a instanceof ResearchItem)
            {

                ResearchItem r = (ResearchItem) a;

                if (r.getUrl () != null)
                {

                    buf.append ("<h3>Web page: <a href='" + r.getUrl () + "'>" + r.getUrl () + "</a></h3>");

                }

            }

            // Get the text and split it.
            StringTokenizer t = new StringTokenizer (a.getDescription (),
                                                     String.valueOf ('\n') + String.valueOf ('\n'));

            while (t.hasMoreTokens ())
            {

                buf.append ("<p>" + t.nextToken ().trim () + "</p>");

            }

        }

        return buf.toString ();

    }
}
