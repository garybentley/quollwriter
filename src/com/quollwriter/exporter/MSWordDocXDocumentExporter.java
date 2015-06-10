package com.quollwriter.exporter;

import java.awt.Color;
import java.awt.event.*;

import java.io.*;

import java.text.*;

import java.util.*;

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
import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.text.*;

import org.docx4j.jaxb.*;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.*;

import org.docx4j.wml.*;


public class MSWordDocXDocumentExporter extends AbstractDocumentExporter
{

    public static final String HEADING1 = "Heading1";
    public static final String HEADING2 = "Heading2";
    public static final String TITLE = "Title";
    public static final String NORMAL = "Normal";

    protected ExportSettings settings = null;
    private JComboBox        exportOthersType = null;
    private JComboBox        exportChaptersType = null;
    private JScrollPane      itemsTreeScroll = null;

    public String getStartStage ()
    {

        return "select-items";

    }

    public WizardStep getStage (String stage)
    {

        final MSWordDocXDocumentExporter _this = this;

        WizardStep ws = new WizardStep ();

        if (stage.equals ("select-items"))
        {

            ws.title = "Select the items you wish to export";

            ws.helpText = "Select the items you wish to export, if you select any {chapters} then any associated {notes} and {outlineitems} will also be exported.";

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

        if (stage.equals ("how-to-save"))
        {

            ws.title = "How should the selected items be saved";
            ws.helpText = "The items can be saved in either a single file or in one file per type of item.  Any {notes} and {outlineitems} will be saved in separate files.";

            FormLayout fl = new FormLayout ("10px, right:p, 6px, p, fill:10px",
                                            "p, 6px, p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            Vector exportChaptersTypes = new Vector ();
            exportChaptersTypes.add ("Single file");
            exportChaptersTypes.add ("One file per " + Environment.getObjectTypeName (Chapter.OBJECT_TYPE));

            this.exportChaptersType = new JComboBox (exportChaptersTypes);
            this.exportChaptersType.setOpaque (false);

            builder.addLabel (Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE),
                              cc.xy (2,
                                     1));
            builder.add (this.exportChaptersType,
                         cc.xy (4,
                                1));

            Vector exportOthersTypes = new Vector ();
            exportOthersTypes.add ("Single file");
            exportOthersTypes.add ("One file per type of item");

            this.exportOthersType = new JComboBox (exportOthersTypes);
            this.exportOthersType.setOpaque (false);

            builder.addLabel ("Other items",
                              cc.xy (2,
                                     3));
            builder.add (this.exportOthersType,
                         cc.xy (4,
                                3));

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

            return "how-to-save";

        }

        return null;

    }

    public String getPreviousStage (String currStage)
    {

        if (currStage == null)
        {

            return null;

        }

        if (currStage.equals ("how-to-save"))
        {

            return "select-items";

        }

        return null;

    }

    private void addTo (MainDocumentPart mp,
                        Chapter          c)
                 throws Exception
    {

        PPrBase.PStyle style = null;

        Styles styles = mp.getStyleDefinitionsPart ().getJaxbElement ();

        for (Style s : styles.getStyle ())
        {

            if (s.getStyleId ().equals (NORMAL))
            {

                style = s.getPPr ().getPStyle ();

            }

        }

        String m = c.getMarkup ();
        String chapterText = c.getText ();

        if (chapterText == null)
        {
            
            chapterText = "";
            
        }
        
        P para = null;//this.createParagraph (style);

        Body b = mp.getJaxbElement ().getBody ();

        mp.addStyledParagraphOfText (HEADING1,
                                     c.getName ());

        // Get the markup, if present.
        Markup mu = new Markup (m);
        Environment.logMessage ("ALL MARKUP: " + mu);
        TextIterator iter = new TextIterator (chapterText);
        
        for (Paragraph p : iter.getParagraphs ())
        {
            
            this.addParagraph (p,
                               mu,
                               style,
                               b);

/*            
            String ptext = p.getText ();
            
            para = this.createParagraph (style);

            b.getEGBlockLevelElts ().add (para);
            
            List<Markup.MarkupItem> items = mu.getMarkupBetween (p.getAllTextStartOffset (),
                                                                 p.getAllTextEndOffset ());

            String nullS = null;
            Markup pm = new Markup (nullS);
            pm.items = items;
            pm.shiftBy (-1 * p.getAllTextStartOffset ());
        
            this.addParagraph (para);
            this.addParagraph (paraText,
                               items,
                               para);
  */          
        }
        
        /*
        Environment.logMessage ("MU: " + mu);
        StringTokenizer t = new StringTokenizer (chapterText,
                                                 String.valueOf ('\n'),
                                                 true);
     
        int l = 0;
        
        while (t.hasMoreTokens ())
        {
        
            String paraText = t.nextToken ();
        
            if ((paraText.length () == 1)
                &&
                (paraText.equals (String.valueOf ('\n')))
               )
            {
               
               l++;

               continue;
               
            }
            
            para = this.createParagraph (style);

            b.getEGBlockLevelElts ().add (para);
            Environment.logMessage ("PARA: " + paraText);
            List<Markup.MarkupItem> items = mu.getMarkupBetween (l,
                                                                 l + paraText.length () + 1);
Environment.logMessage ("ITEMS: " + items);
for (Markup.MarkupItem mi : items)
{
    
    Environment.logMessage ("MIX: " + mi + ", " + chapterText.substring (mi.start, mi.end));
}
            String nullS = null;
            Markup pm = new Markup (nullS);
            pm.items = items;
            pm.shiftBy (-1 * l);
        
            this.addParagraph (paraText,
                               items,
                               para);

            l += paraText.length ();
                
        }
            */
    }

    private void addParagraph (Paragraph      para,
                               Markup         markup,
                               PPrBase.PStyle style,
                               Body           body)
    {

        P p = this.createParagraph (style);

        body.getEGBlockLevelElts ().add (p);

        // Get the markup items.
        List<Markup.MarkupItem> items = TextUtilities.getParagraphMarkup (para,
                                                                          markup);

        String ptext = para.getText ();
        int textLength = ptext.length ();
        
        if (items.size () > 0)
        {

            Markup.MarkupItem last = null;
            
            int start = -1;
            int end = -1;

            int lastEnd = 0;
            
            // Check the first item.
            Markup.MarkupItem fitem = items.get (0);
            
            if (fitem.start > 0)
            {
                
                start = 0;
                end = fitem.start;
                
                if (end > textLength)
                {
                    
                    end = textLength;
                    
                }
                
                // Add the start text.
                p.getParagraphContent ().add (this.createRun (ptext.substring (0, end),
                                                              null));
                
                lastEnd = end;
                
            }
            
            for (Markup.MarkupItem item : items)
            {

                start = item.start;

                end = item.end;

                if (start > lastEnd)
                {

                    // Add some normal text.
                    p.getParagraphContent ().add (this.createRun (ptext.substring (lastEnd, Math.min(item.start, textLength)),
                                                                  null));            
                                                                  
                }
                
                if ((end >= textLength)
                    ||
                    (end == -1)
                   )
                {
                    
                    end = textLength;
                    
                }

                p.getParagraphContent ().add (this.createRun (ptext.substring (start, end),
                                                              item));            
            
                lastEnd = end;
            
            }
            
            // Check the last item.
            Markup.MarkupItem litem = items.get (items.size () - 1);
            
            // Does it end before the end of the text, if so then add normal text.
            if (litem.end < textLength)
            {
                
                // Add the last text.
                p.getParagraphContent ().add (this.createRun (ptext.substring (litem.end),
                                                              null));                            
                
            }
                            
        } else {
            
            p.getParagraphContent ().add (this.createRun (ptext,
                                                          null));
            
        }
        
    }
            
    private void addParagraph (String                  paraText,
                               List<Markup.MarkupItem> items,
                               P                       para)
    {
        
        int ind = 0;
        
        if (items.size () > 0)
        {                                                                    
        
            //Iterator<Markup.MarkupItem> iter = mu.iterator ();

            Markup.MarkupItem last = null;
            
            int start = -1;
            int end = -1;
            
            for (Markup.MarkupItem item : items)
            {

                start = -1;
                end = -1;
            
                if (last == null)
                {
                    
                    start = 0;
                    end = item.start;
                    
                    this.addText (paraText,
                                  start,
                                  end,
                                  null,
                                  para);
                    
                }
            
                last = item;

                this.addText (paraText,
                              last.start,
                              last.end,
                              last,
                              para);

            }                

            if (last.end < paraText.length ())
            {

                this.addText (paraText,
                              last.end,
                              -1,
                              null,
                              para);

            }

        } else {
            
            this.addText (paraText,
                          -1,
                          -1,
                          null,
                          para);
            
        }
        
    }
    
    private org.docx4j.wml.R createRun (String            text,
                                        Markup.MarkupItem item)
    {
        
        // Create the text element
        ObjectFactory factory = Context.getWmlObjectFactory ();

        org.docx4j.wml.Text tel = factory.createText ();

        tel.setValue (text);

        org.docx4j.wml.R run = factory.createR ();

        org.docx4j.wml.RPr pr = factory.createRPr ();

        BooleanDefaultTrue bool = new BooleanDefaultTrue ();
        bool.setVal (true);

        if (item != null)
        {

            if (item.bold)
            {

                pr.setB (bool);

            }

            if (item.italic)
            {

                pr.setI (bool);

            }

            if (item.underline)
            {

                U u = factory.createU ();
                u.setVal (UnderlineEnumeration.SINGLE);

                pr.setU (u);

            }

        }

        run.setRPr (pr);

        run.getRunContent ().add (tel);

        return run;
        
    }
    
    private void addText (String            chapterText,
                          int               start,
                          int               end,
                          Markup.MarkupItem item,
                          P                 para)
    {

        // Create the text element
        ObjectFactory factory = Context.getWmlObjectFactory ();

        org.docx4j.wml.Text tel = factory.createText ();

        String t = chapterText;

        if ((start > -1)
            &&
            (end > -1)
           )
        {

            t = chapterText.substring (start,
                                       end);
            Environment.logMessage ("T: " + t);
        }
        
        if ((start > -1)
            &&
            (end == -1)
           )
        {
            
            t = chapterText.substring (start);
            
        }
               
        tel.setValue (t);

        org.docx4j.wml.R run = factory.createR ();

        org.docx4j.wml.RPr pr = factory.createRPr ();

        BooleanDefaultTrue bool = new BooleanDefaultTrue ();
        bool.setVal (true);

        if (item != null)
        {

            if (item.bold)
            {

                pr.setB (bool);

            }

            if (item.italic)
            {

                pr.setI (bool);

            }

            if (item.underline)
            {

                U u = factory.createU ();
                u.setVal (UnderlineEnumeration.SINGLE);

                pr.setU (u);

            }

        }

        run.setRPr (pr);

        run.getRunContent ().add (tel);

        para.getParagraphContent ().add (run);

    }

    private P createParagraph (PPrBase.PStyle style)
    {

        ObjectFactory factory = Context.getWmlObjectFactory ();

        org.docx4j.wml.P para = factory.createP ();

        org.docx4j.wml.PPr ppr = factory.createPPr ();

        ppr.setPStyle (style);

        para.setPPr (ppr);

        return para;

    }

    private void addTo (MainDocumentPart mp,
                        NamedObject      n)
                 throws Exception
    {

        String prefix = "";
        String text = n.getDescription ();

        if (n instanceof QCharacter)
        {

            QCharacter c = (QCharacter) n;

            if (text == null)
            {

                text = "";

            }

            if ((c.getAliases () != null)
                &&
                (c.getAliases ().trim ().length () > 0)
               )
            {

                List l = c.getAliasesAsList ();
        
                StringBuilder b = new StringBuilder ("Aliases: ");
        
                for (int i = 0; i < l.size (); i++)
                {
    
                    if (i > 0)
                    {
    
                        b.append (", ");
    
                    }
    
                    b.append (l.get (i));
    
                }

                text = b.toString () + "\n\n" + text;

            }

        }

        if (n instanceof QObject)
        {

            QObject o = (QObject) n;

            if (text == null)
            {

                text = "";

            }

            if (o.getType () != null)
            {

                text = "Type: " + o.getType () + "\n\n" + text;

            }

        }

        if (n instanceof ResearchItem)
        {

            ResearchItem r = (ResearchItem) n;

            if (text == null)
            {

                text = "";

            }

            String url = r.getUrl ();

            if (url != null)
            {

                if ((!url.startsWith ("http://"))
                    &&
                    (!url.startsWith ("https://"))
                   )
                {
                    
                    url = "http://" + url;
                    
                }

                text = url + "\n\n" + text;

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

                text = note.getType () + ": " + text;

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

                text = ((Chapter) n).getText ();

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

            String tok = t.nextToken ();

            mp.addStyledParagraphOfText (NORMAL,
                                         tok);

        }

    }

    protected void save (WordprocessingMLPackage wordMLPackage,
                         String                  name)
                  throws Exception
    {

        name = name.replace ('/',
                             '_');

        name = name.replace ('\\',
                             '_');

        name = Utils.sanitizeForFilename (name);
                             
        File f = new File (this.settings.outputDirectory.getPath () + "/" + name + Constants.DOCX_FILE_EXTENSION);

        wordMLPackage.save (f);

    }

    public void exportProject (File dir)
                        throws GeneralException
    {

        ExportSettings es = new ExportSettings ();
        es.outputDirectory = dir;
        es.chapterExportType = ((this.exportChaptersType.getSelectedIndex () == 0) ? ExportSettings.SINGLE_FILE : ExportSettings.INDIVIDUAL_FILE);
        es.otherExportType = ((this.exportOthersType.getSelectedIndex () == 0) ? ExportSettings.SINGLE_FILE : ExportSettings.INDIVIDUAL_FILE);

        this.settings = es;

        Project p = ExportUtils.getSelectedItems (this.itemsTree,
                                                  this.proj);

        try
        {

            if (settings.chapterExportType == ExportSettings.SINGLE_FILE)
            {

                WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage ();

                MainDocumentPart mp = wordMLPackage.getMainDocumentPart ();

                this.setStyles (mp,
                                true);

                mp.addStyledParagraphOfText (TITLE,
                                             p.getName ());

                WordprocessingMLPackage notesWordMLPackage = WordprocessingMLPackage.createPackage ();

                MainDocumentPart notesmp = notesWordMLPackage.getMainDocumentPart ();

                // ADD NOTES AS FOOTNOTES???  OTHER OPTION???  MAYBE FOR "REAL" FORMATTING ALLOW CERTAIN
                // TYPES TO BE ADDED AS FOOTNOTES???

                this.setStyles (notesmp,
                                true);

                notesmp.addStyledParagraphOfText (TITLE,
                                                  p.getName () + " - Notes");

                boolean hasNotes = false;

                WordprocessingMLPackage outlineWordMLPackage = WordprocessingMLPackage.createPackage ();

                MainDocumentPart outlinemp = outlineWordMLPackage.getMainDocumentPart ();

                this.setStyles (outlinemp,
                                true);

                outlinemp.addStyledParagraphOfText (TITLE,
                                                    p.getName () + " - Scenes and Plot Outline");

                boolean hasOutline = false;
                                                    
                WordprocessingMLPackage chapterInfoWordMLPackage = WordprocessingMLPackage.createPackage ();

                MainDocumentPart chapinfmp = chapterInfoWordMLPackage.getMainDocumentPart ();

                this.setStyles (chapinfmp,
                                true);

                chapinfmp.addStyledParagraphOfText (TITLE,
                                                    p.getName () + " - Chapter Information");

                boolean hasChapInf = false;
                                                    
                List<Chapter> chapters = p.getBooks ().get (0).getChapters ();

                for (Chapter c : chapters)
                {

                    this.addTo (mp,
                                c);

                    notesmp.addStyledParagraphOfText (HEADING1,
                                                      c.getName ());
                    outlinemp.addStyledParagraphOfText (HEADING1,
                                                        c.getName ());
                    chapinfmp.addStyledParagraphOfText (HEADING1,
                                                        c.getName ());

                    this.addChapterItems (c,
                                          notesmp,
                                          outlinemp,
                                          chapinfmp);
                    
                    if ((c.getGoals () != null)
                        ||
                        (c.getPlan () != null)
                       )
                    {
                        
                        hasChapInf = true;
                        
                    }
                    
                    if (c.getNotes ().size () > 0)
                    {
                        
                        hasNotes = true;
                        
                    }

                    if ((c.getOutlineItems ().size () > 0)
                        ||
                        (c.getScenes ().size () > 0)
                       )
                    {
                        
                        hasOutline = true;
                        
                    }

                }

                this.save (wordMLPackage,
                           p.getName ());

                if (hasNotes)
                {

                    this.save (notesWordMLPackage,
                               p.getName () + " - Notes");

                }

                if (hasOutline)
                {

                    this.save (outlineWordMLPackage,
                               p.getName () + " - Scenes and Plot Outline");

                }

                if (hasChapInf)
                {

                    this.save (chapterInfoWordMLPackage,
                               p.getName () + " - Chapter Information");

                }
                
            }

            if (settings.chapterExportType == ExportSettings.INDIVIDUAL_FILE)
            {

                List<Chapter> chapters = p.getBooks ().get (0).getChapters ();

                for (Chapter c : chapters)
                {

                    WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage ();

                    MainDocumentPart mp = wordMLPackage.getMainDocumentPart ();

                    this.setStyles (mp,
                                    true);

                    WordprocessingMLPackage notesWordMLPackage = WordprocessingMLPackage.createPackage ();

                    MainDocumentPart notesmp = notesWordMLPackage.getMainDocumentPart ();

                    this.setStyles (notesmp,
                                    true);

                    notesmp.addStyledParagraphOfText (TITLE,
                                                      c.getName () + " - Notes");
                                                      
                    WordprocessingMLPackage outlineWordMLPackage = WordprocessingMLPackage.createPackage ();

                    MainDocumentPart outlinemp = outlineWordMLPackage.getMainDocumentPart ();

                    this.setStyles (outlinemp,
                                    true);

                    outlinemp.addStyledParagraphOfText (TITLE,
                                                        c.getName () + " - Scenes and Plot Outline");

                    WordprocessingMLPackage chapterInfoWordMLPackage = WordprocessingMLPackage.createPackage ();

                    MainDocumentPart chapinfmp = chapterInfoWordMLPackage.getMainDocumentPart ();

                    this.setStyles (chapinfmp,
                                    true);

                    chapinfmp.addStyledParagraphOfText (TITLE,
                                                        c.getName () + " - Chapter Information");                        
                                                        
                    this.addTo (mp,
                                c);

                    this.save (wordMLPackage,
                               c.getName ());
                               
                    this.addChapterItems (c,
                                          notesmp,
                                          outlinemp,
                                          chapinfmp);
                          
                    if ((c.getGoals () != null)
                        ||
                        (c.getPlan () != null)
                       )
                    {
                        
                        this.save (chapterInfoWordMLPackage,
                                   c.getName () + " - Chapter Information");
                                          
                    }
                    
                    if (c.getNotes ().size () > 0)
                    {

                        this.save (outlineWordMLPackage,
                                   c.getName () + " - Scenes and Plot Outline");

                    }

                    if ((c.getScenes ().size () > 0)
                        ||
                        (c.getOutlineItems ().size () > 0)
                       )
                    {
                        
                        this.save (notesWordMLPackage,
                                   c.getName () + " - Notes");
                        
                    }

                }

            }

            if (settings.otherExportType == ExportSettings.SINGLE_FILE)
            {

                WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage ();

                MainDocumentPart mp = wordMLPackage.getMainDocumentPart ();

                this.setStyles (mp,
                                false);

                mp.addStyledParagraphOfText (TITLE,
                                             p.getName () + " - Assets");

                List<NamedObject> objs = new ArrayList (p.getAllNamedChildObjects (Asset.class));

                Collections.sort (objs,
                                  new NamedObjectSorter ());

                for (NamedObject n : objs)
                {

                    this.addTo (mp,
                                n);

                }

                this.save (wordMLPackage,
                           p.getName () + " - Assets");

            }

            if (settings.otherExportType == ExportSettings.INDIVIDUAL_FILE)
            {

                this.writeItemsFromProject (p,
                                            QCharacter.class,
                                            p.getName () + " - " + Environment.getObjectTypeNamePlural (QCharacter.OBJECT_TYPE));

                this.writeItemsFromProject (p,
                                            Location.class,
                                            p.getName () + " - " + Environment.getObjectTypeNamePlural (Location.OBJECT_TYPE));

                this.writeItemsFromProject (p,
                                            QObject.class,
                                            p.getName () + " - " + Environment.getObjectTypeNamePlural (QObject.OBJECT_TYPE));

                this.writeItemsFromProject (p,
                                            ResearchItem.class,
                                            p.getName () + " - " + Environment.getObjectTypeNamePlural (ResearchItem.OBJECT_TYPE));

            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to export project: " +
                                        this.proj +
                                        " using settings: " +
                                        settings,
                                        e);

        }

    }

    private void writeItemsFromProject (Project p,
                                        Class   cl,
                                        String  title)
                                        throws  GeneralException
    {

        try
        {

            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage ();
    
            MainDocumentPart mp = wordMLPackage.getMainDocumentPart ();
    
            this.setStyles (mp,
                            false);
    
            mp.addStyledParagraphOfText (TITLE,
                                         title);
            
            List<NamedObject> objs = new ArrayList (p.getAllNamedChildObjects (cl));
    
            Collections.sort (objs,
                              new NamedObjectSorter ());
    
            for (NamedObject n : objs)
            {
    
                this.addTo (mp,
                            n);
    
            }        
    
            this.save (wordMLPackage,
                       title);

        } catch (Exception e) {
            
            throw new GeneralException ("Unable to write items of type: " +
                                        cl.getName () +
                                        " to file with title: " +
                                        title,
                                        e);
            
        }
        
    }

    private void addChapterItems (Chapter c,
                                  MainDocumentPart notesmp,
                                  MainDocumentPart outlinemp,
                                  MainDocumentPart chapinfmp)
                                  throws           GeneralException
    {
        
        try
        {

            String goals = c.getGoals ();
            String plan = c.getPlan ();        
        
            if (goals != null)
            {
                
                chapinfmp.addStyledParagraphOfText (HEADING2,
                                                    "Goals");

                StringTokenizer t = new StringTokenizer (goals,
                                                         String.valueOf ('\n'));
        
                while (t.hasMoreTokens ())
                {
        
                    String tok = t.nextToken ();
        
                    chapinfmp.addStyledParagraphOfText (NORMAL,
                                                        "* " + tok);
        
                }
                
            }

            if (plan != null)
            {
                
                chapinfmp.addStyledParagraphOfText (HEADING1,
                                                    "Plan");

                StringTokenizer t = new StringTokenizer (plan,
                                                         String.valueOf ('\n'));
        
                while (t.hasMoreTokens ())
                {
        
                    String tok = t.nextToken ();
        
                    chapinfmp.addStyledParagraphOfText (NORMAL,
                                                        "* " + tok);
        
                }
                
            }
        
            Set<NamedObject> items = c.getAllNamedChildObjects ();
            
            for (NamedObject n : items)
            {
    
                if (n instanceof Note)
                {
    
                    this.addTo (notesmp,
                                n);
    
                } else {
                                    
                    this.addTo (outlinemp,
                                n);
                    
                    if (n instanceof Scene)
                    {
                        
                        Scene s = (Scene) n;
                        
                        Set<OutlineItem> its = s.getOutlineItems ();
                        
                        for (OutlineItem it : its)
                        {
                            
                            this.addTo (outlinemp,
                                        it);
                            
                        }
                        
                    }
                    
                }
    
            }        

        } catch (Exception e) {
            
            throw new GeneralException ("Unable to write items for chapter: " +
                                        c,
                                        e);
            
        }
        
    }

    private void setStyles (MainDocumentPart mp,
                            boolean          indent)
    {

        // Sort out the styles.
        Styles styles = mp.getStyleDefinitionsPart ().getJaxbElement ();

        ObjectFactory factory = Context.getWmlObjectFactory ();

        for (Style s : styles.getStyle ())
        {

            if ((s.getStyleId ().equals (NORMAL)) ||
                (s.getStyleId ().equals (TITLE)) ||
                (s.getStyleId ().equals (HEADING1)))
            {

                RPr rpr = s.getRPr ();

                if (rpr == null)
                {

                    rpr = factory.createRPr ();
                    s.setRPr (rpr);

                }

                RFonts rf = rpr.getRFonts ();

                if (rf == null)
                {

                    rf = factory.createRFonts ();
                    rpr.setRFonts (rf);

                }

                rf.setAscii (this.proj.getProperty (Constants.EDITOR_FONT_PROPERTY_NAME));

                rf.setAsciiTheme (null);

                // rpr.sz - font size.
                if (s.getStyleId ().equals (NORMAL))
                {

                    HpsMeasure m = factory.createHpsMeasure ();

                    // Size is 1/144 of an inch, so just double the value.

                    int fontSize = this.proj.getPropertyAsInt (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME);

                    m.setVal (java.math.BigInteger.valueOf ((long) fontSize * 2));

                    rpr.setSz (m);

                    PPr ppr = s.getPPr ();

                    if (ppr == null)
                    {

                        ppr = factory.createPPr ();
                        s.setPPr (ppr);

                    }

                    // Alignment is "jc" (so obvious!)
                    Jc jc = factory.createJc ();

                    String align = this.proj.getProperty (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME).toUpperCase ();

                    if (align.equals ("JUSTIFIED"))
                    {

                        align = "BOTH";

                    }

                    jc.setVal (JcEnumeration.valueOf (align));
                    ppr.setJc (jc);

                    // Also spacing - line spacing.
                    PPrBase.Spacing spac = ppr.getSpacing ();

                    if (spac == null)
                    {

                        spac = factory.createPPrBaseSpacing ();
                        ppr.setSpacing (spac);

                    }

                    float spacing = this.proj.getPropertyAsFloat (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME);

                    spac.setAfterLines (java.math.BigInteger.valueOf ((long) (100 * spacing)));
                    // spac.setBeforeLines (java.math.BigInteger.valueOf ((long) 300));

                    // Line spacing is expressed in 240ths of a line.
                    spac.setLine (java.math.BigInteger.valueOf ((long) (240 * spacing)));

                    spac.setLineRule (STLineSpacingRule.valueOf ("AUTO"));

                    // ind - paragraph indentation.
                    if ((indent) &&
                        (this.proj.getPropertyAsBoolean (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME)))
                    {

                        PPrBase.Ind ind = ppr.getInd ();

                        if (ind == null)
                        {

                            ind = factory.createPPrBaseInd ();
                            ppr.setInd (ind);

                        }

                        // Is in 20ths of a point.
                        ind.setFirstLine (java.math.BigInteger.valueOf ((long) 30 * 20));

                    }

                } else
                {

                    PPr ppr = s.getPPr ();

                    if (ppr == null)
                    {

                        ppr = factory.createPPr ();
                        s.setPPr (ppr);

                    }

                    PPrBase.Ind ind = ppr.getInd ();

                    if (ind == null)
                    {

                        ind = factory.createPPrBaseInd ();
                        ppr.setInd (ind);

                    }

                    // Since styles are inherited, override other styles to have a zero indent.
                    ind.setFirstLine (java.math.BigInteger.valueOf (0L));

                }

            }

        }

    }

/*
    private Project getSelectedItems ()
    {

        Project p = new Project (this.proj.getName ());

        Book b = new Book (p,
                           null);

        p.addBook (b);
        b.setName (this.proj.getName ());

        DefaultTreeModel dtm = (DefaultTreeModel) this.itemsTree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        Enumeration en = root.depthFirstEnumeration ();

        while (en.hasMoreElements ())
        {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement ();

            Object o = node.getUserObject ();

            if (o instanceof SelectableDataObject)
            {

                SelectableDataObject so = (SelectableDataObject) o;

                if (so.selected)
                {

                    if (so.obj instanceof Asset)

                    {

                        p.addAsset ((Asset) so.obj);

                    }

                    if (so.obj instanceof Chapter)
                    {

                        p.getBooks ().get (0).addChapter ((Chapter) so.obj);

                    }

                }

            }

        }

        return p;

    }
*/
}
