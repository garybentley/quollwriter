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

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.Markup;
import com.quollwriter.ui.renderers.*;


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

        return name;

    }

    public void exportProject (File dir)
                        throws GeneralException
    {

        try
        {

            // Get the Author.
            // Get the title.
            // Get the language.
            // Get the id (ISBN/URL).

            // Format of the zip should be.
            // mimetype (should contain application/epub+zip)
            // META-INF/
            // container.xml
            // OPS/
            // book.opf
            // chapters/
            // chapter[1-X].xhtml
            // appendix-a-characters.xhtml
            // appendix-b-locations.xhtml
            // appendix-c-items.xhtml
            // appendix-d-research.xhtml
            // appendix-e-notes.xhtml
            // appendix-f-outline.xhtml
            // css/
            // main.css

            // Create each chapter .xhtml

            Project p = ExportUtils.getSelectedItems (this.itemsTree,
                                                      this.proj);

            String name = this.sanitizeName (this.proj.getName ());

            this.zout = new ZipOutputStream (new PrintStream (new File (dir.getPath () + "/" + name + Constants.EPUB_FILE_EXTENSION),
                                                              "utf-8"));

            this.zout.setLevel (0);

            this.addEntry ("mimetype",
                           "application/epub+zip");

            // Create the container.xml
            this.addEntry ("META-INF/container.xml",
                           Environment.getResourceFileAsString ("/data/export/epub/container-template.xml"));

            // Create the book.opf

            // Get all the chapters.

            // Add other objects as Appendices

            String cTemp = Environment.getResourceFileAsString ("/data/export/epub/chapter-template.xml");

            Book b = this.proj.getBook (0);

            List<Chapter> chapters = b.getChapters ();

            StringBuilder manifest = new StringBuilder ();
            StringBuilder spine = new StringBuilder ();
            StringBuilder navMap = new StringBuilder ();

            String nmItem = "<navPoint class='chapter' id='[[CHAPTER_ID]]' playOrder='[[C]]'><navLabel><text>[[NAME]]</text></navLabel><content src='chapters/[[CHAPTER_ID]].xhtml' /></navPoint>";

            String mItem = "<item id='[[CHAPTER_ID]]' href='chapters/[[CHAPTER_ID]].xhtml' media-type='application/xhtml+xml' />";

            String sItem = "<itemref idref='[[CHAPTER_ID]]' />";

            int count = 1;

            for (Chapter c : chapters)
            {

                String cid = this.sanitizeName (c.getName ());

                String nm = StringUtils.replaceString (nmItem,
                                                       "[[CHAPTER_ID]]",
                                                       cid);
                nm = StringUtils.replaceString (nm,
                                                "[[C]]",
                                                count + "");
                nm = StringUtils.replaceString (nm,
                                                "[[NAME]]",
                                                c.getName ());

                count++;

                navMap.append (nm);

                manifest.append (StringUtils.replaceString (mItem,
                                                            "[[CHAPTER_ID]]",
                                                            cid));
                manifest.append ("\n");
                spine.append (StringUtils.replaceString (sItem,
                                                         "[[CHAPTER_ID]]",
                                                         cid));
                spine.append ("\n");

                // Create the chapter entry.
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

                        }

                        ct.append (st);

                        if (styled)
                        {

                            ct.append ("</span>");

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
                StringTokenizer tt = new StringTokenizer (ct.toString (),
                                                          String.valueOf ('\n'));

                ct = new StringBuilder ();

                while (tt.hasMoreTokens ())
                {

                    String tok = tt.nextToken ().trim ();

                    if (tok.length () > 0)
                    {

                        ct.append ("<p>" + tok + "</p>");

                    }

                }

                this.addEntry ("OPS/chapters/" + cid + ".xhtml",
                               StringUtils.replaceString (chapterText,
                                                          "[[CONTENT]]",
                                                          ct.toString ()));

            }

            String appendixTemp = Environment.getResourceFileAsString ("/data/export/epub/appendix-template.xml");

            // Get the characters.
            List<QCharacter> characters = this.proj.getCharacters ();

            if (characters.size () > 0)
            {

                String cid = "appendix-a-characters";

                String title = "Appendix A - Characters";

                String nm = StringUtils.replaceString (nmItem,
                                                       "[[CHAPTER_ID]]",
                                                       cid);
                nm = StringUtils.replaceString (nm,
                                                "[[C]]",
                                                count + "");
                nm = StringUtils.replaceString (nm,
                                                "[[NAME]]",
                                                title);

                count++;

                navMap.append (nm);

                manifest.append (StringUtils.replaceString (mItem,
                                                            "[[CHAPTER_ID]]",
                                                            cid));
                manifest.append ("\n");
                spine.append (StringUtils.replaceString (sItem,
                                                         "[[CHAPTER_ID]]",
                                                         cid));
                spine.append ("\n");

                String t = StringUtils.replaceString (appendixTemp,
                                                      "[[TITLE]]",
                                                      title);
                t = StringUtils.replaceString (t,
                                               "[[CONTENT]]",
                                               this.getAssetsPage (characters));

                this.addEntry ("OPS/chapters/" + cid + ".xhtml",
                               t);

            }

            // Get the locations.
            List<Location> locs = this.proj.getLocations ();

            if (locs.size () > 0)
            {

                String cid = "appendix-b-locations";

                String title = "Appendix B - Locations";

                String nm = StringUtils.replaceString (nmItem,
                                                       "[[CHAPTER_ID]]",
                                                       cid);
                nm = StringUtils.replaceString (nm,
                                                "[[C]]",
                                                count + "");
                nm = StringUtils.replaceString (nm,
                                                "[[NAME]]",
                                                title);

                count++;

                navMap.append (nm);

                manifest.append (StringUtils.replaceString (mItem,
                                                            "[[CHAPTER_ID]]",
                                                            cid));
                manifest.append ("\n");
                spine.append (StringUtils.replaceString (sItem,
                                                         "[[CHAPTER_ID]]",
                                                         cid));
                spine.append ("\n");

                String t = StringUtils.replaceString (appendixTemp,
                                                      "[[TITLE]]",
                                                      title);
                t = StringUtils.replaceString (t,
                                               "[[CONTENT]]",
                                               this.getAssetsPage (locs));

                this.addEntry ("OPS/chapters/" + cid + ".xhtml",
                               t);

            }

            // Get the objects.
            List<QObject> objs = this.proj.getQObjects ();

            if (objs.size () > 0)
            {

                String cid = "appendix-c-items";

                String title = "Appendix C - Items";

                String nm = StringUtils.replaceString (nmItem,
                                                       "[[CHAPTER_ID]]",
                                                       cid);
                nm = StringUtils.replaceString (nm,
                                                "[[C]]",
                                                count + "");
                nm = StringUtils.replaceString (nm,
                                                "[[NAME]]",
                                                title);

                count++;

                navMap.append (nm);

                manifest.append (StringUtils.replaceString (mItem,
                                                            "[[CHAPTER_ID]]",
                                                            cid));
                manifest.append ("\n");
                spine.append (StringUtils.replaceString (sItem,
                                                         "[[CHAPTER_ID]]",
                                                         cid));
                spine.append ("\n");

                String t = StringUtils.replaceString (appendixTemp,
                                                      "[[TITLE]]",
                                                      title);
                t = StringUtils.replaceString (t,
                                               "[[CONTENT]]",
                                               this.getAssetsPage (objs));

                this.addEntry ("OPS/chapters/" + cid + ".xhtml",
                               t);

            }

            // Get the research items.
            List<ResearchItem> res = this.proj.getResearchItems ();

            if (res.size () > 0)
            {

                String cid = "appendix-d-research";

                String title = "Appendix D - Research";

                String nm = StringUtils.replaceString (nmItem,
                                                       "[[CHAPTER_ID]]",
                                                       cid);
                nm = StringUtils.replaceString (nm,
                                                "[[C]]",
                                                count + "");
                nm = StringUtils.replaceString (nm,
                                                "[[NAME]]",
                                                title);

                count++;

                navMap.append (nm);

                manifest.append (StringUtils.replaceString (mItem,
                                                            "[[CHAPTER_ID]]",
                                                            cid));
                manifest.append ("\n");
                spine.append (StringUtils.replaceString (sItem,
                                                         "[[CHAPTER_ID]]",
                                                         cid));
                spine.append ("\n");

                String t = StringUtils.replaceString (appendixTemp,
                                                      "[[TITLE]]",
                                                      title);
                t = StringUtils.replaceString (t,
                                               "[[CONTENT]]",
                                               this.getAssetsPage (res));

                this.addEntry ("OPS/chapters/" + cid + ".xhtml",
                               t);

            }

            String opf = Environment.getResourceFileAsString ("/data/export/epub/opf-template.xml");

            opf = StringUtils.replaceString (opf,
                                             "[[TITLE]]",
                                             p.getName ());

            String id = this.id.getText ();
            String scheme = "ISBN";

            if ((id.indexOf (".") != -1) ||
                (id.indexOf ("/") != -1) ||
                (id.indexOf ("://") != -1))
            {

                scheme = "URL";

            }

            opf = StringUtils.replaceString (opf,
                                             "[[ID]]",
                                             id);
            opf = StringUtils.replaceString (opf,
                                             "[[SCHEME]]",
                                             scheme);
            opf = StringUtils.replaceString (opf,
                                             "[[AUTHOR]]",
                                             this.author.getText ());

            opf = StringUtils.replaceString (opf,
                                             "[[MANIFEST]]",
                                             manifest.toString ());
            opf = StringUtils.replaceString (opf,
                                             "[[SPINE]]",
                                             spine.toString ());

            this.addEntry ("OPS/book.opf",
                           opf);

            String ncx = Environment.getResourceFileAsString ("/data/export/epub/ncx-template.xml");

            ncx = StringUtils.replaceString (ncx,
                                             "[[TITLE]]",
                                             p.getName ());
            ncx = StringUtils.replaceString (ncx,
                                             "[[AUTHOR]]",
                                             this.author.getText ());
            ncx = StringUtils.replaceString (ncx,
                                             "[[ID]]",
                                             id);
            ncx = StringUtils.replaceString (ncx,
                                             "[[CHAPTERS]]",
                                             navMap.toString ());

            this.addEntry ("OPS/book.ncx",
                           ncx);

            String css = Environment.getResourceFileAsString ("/data/export/epub/css-template.xml");

            css = StringUtils.replaceString (css,
                                             "[[FONT_NAME]]",
                                             this.proj.getProperty (Constants.EDITOR_FONT_PROPERTY_NAME));
            css = StringUtils.replaceString (css,
                                             "[[FONT_SIZE]]",
                                             this.proj.getPropertyAsInt (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME) + "pt");
            css = StringUtils.replaceString (css,
                                             "[[LINE_SPACING]]",
                                             "" + this.proj.getPropertyAsFloat (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME));
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

            this.addEntry ("OPS/css/main.css",
                           css);

            zout.finish ();
            zout.close ();

            // Save the author and id values.
            this.proj.setProperty (Constants.AUTHOR_NAME_PROPERTY_NAME,
                                   this.author.getText ());
            this.proj.setProperty (Constants.BOOK_ID_PROPERTY_NAME,
                                   this.id.getText ());

        } catch (Exception e)
        {

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
