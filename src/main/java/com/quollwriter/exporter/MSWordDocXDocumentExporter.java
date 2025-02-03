package com.quollwriter.exporter;

import java.io.*;
import java.nio.file.*;

import java.text.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.text.*;

import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.userobjects.*;
import com.quollwriter.ui.fx.components.*;

import org.docx4j.jaxb.*;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.*;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.wml.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class MSWordDocXDocumentExporter extends AbstractDocumentExporter
{
    public static final String HOW_TO_SAVE_STAGE = "how-to-save";

    public static final String HEADING1 = "Heading1";
    public static final String HEADING2 = "Heading2";
    public static final String TITLE = "Title";
    public static final String NORMAL = "Normal";
    public static final String SUBTITLE = "Subtitle";

    protected ExportSettings settings = null;

    private QuollChoiceBox exportChaptersType2 = null;
    private QuollChoiceBox exportOthersType2 = null;

    private CTLanguage lang = null;

    public String getStartStage ()
    {

        return HOW_TO_SAVE_STAGE;

    }

    @Override
    public Wizard.Step getStage (String stage)
    {

        final MSWordDocXDocumentExporter _this = this;

        com.quollwriter.ui.fx.components.Wizard.Step ws = new com.quollwriter.ui.fx.components.Wizard.Step ();

        if (HOW_TO_SAVE_STAGE.equals (stage))
        {

            ws.title = getUILanguageStringProperty (exportproject,stages,howtosave,title);
            //"How should the selected items be saved";

            Set<StringProperty> exportChaptersTypes = new LinkedHashSet<> ();
            exportChaptersTypes.add (getUILanguageStringProperty (exportproject,stages,howtosave,types,singlefile));
            exportChaptersTypes.add (getUILanguageStringProperty (exportproject,stages,howtosave,types,onefileperchapter));

            this.exportChaptersType2 = QuollChoiceBox.builder ()
                .items (exportChaptersTypes)
                .build ();

            Set<StringProperty> exportOthersTypes = new LinkedHashSet<> ();
            exportOthersTypes.add (getUILanguageStringProperty (exportproject,stages,howtosave,types,singlefile));
            exportOthersTypes.add (getUILanguageStringProperty (exportproject,stages,howtosave,types,onefileperitemtype));

            this.exportOthersType2 = QuollChoiceBox.builder ()
                .items (exportOthersTypes)
                .build ();

            Form f = Form.builder ()
                .description (exportproject,stages,howtosave,text)
                .item (getUILanguageStringProperty (exportproject,stages,howtosave,labels,chapters),
                       this.exportChaptersType2)
                .item (getUILanguageStringProperty (exportproject,stages,howtosave,labels,otheritems),
                       this.exportOthersType2)
                .build ();

            ws.content = f;
            ws.content.getStyleClass ().add (HOW_TO_SAVE_STAGE);

        }

        return ws;

    }

    public String getNextStage (String currStage)
    {

        if (HOW_TO_SAVE_STAGE.equals (currStage))
        {

            return null;

        }

        return HOW_TO_SAVE_STAGE;
/*
        if (currStage == null)
        {

            return "select-items";

        }

        if (currStage.equals ("select-items"))
        {

            return "how-to-save";

        }

        return null;
*/
    }

    public String getPreviousStage (String currStage)
    {

        return null;
/*
        if (currStage == null)
        {

            return null;

        }

        if (currStage.equals ("how-to-save"))
        {

            return "select-items";

        }

        return null;
*/
    }

    private void addParagraph (Paragraph        para,
                               Markup           markup,
                               String           style,
                               MainDocumentPart mp,
                               Body             body)
                        throws GeneralException
    {

        PPrBase.PStyle pstyle = null;

        Styles styles = null;

        try
        {

            styles = mp.getStyleDefinitionsPart ().getContents ();

        } catch (Exception e) {

            throw new GeneralException ("Unable to get styles",
                                        e);

        }

        for (Style s : styles.getStyle ())
        {

            if (s.getStyleId ().equals (style))
            {

                pstyle = s.getPPr ().getPStyle ();

            }

        }

        this.addParagraph (para,
                           markup,
                           pstyle,
                           body);

    }

    private void addParagraph (Paragraph      para,
                               Markup         markup,
                               PPrBase.PStyle style,
                               Body           body)
    {

        ObjectFactory factory = Context.getWmlObjectFactory ();

        P p = this.createParagraph (style);

        PPr ppr = p.getPPr ();

        if (ppr == null)
        {

            ppr = factory.createPPr ();
            p.setPPr (ppr);

        }

        ParaRPr rpr = ppr.getRPr ();

        if (rpr == null)
        {

            rpr = factory.createParaRPr ();
            ppr.setRPr (rpr);

        }

        //rpr.setLang (this.lang);

        //p.getPPr ().getRPr ().setLang (this.lang);

        body.getContent ().add (p);

        Markup pm = new Markup (markup,
                                para.getAllTextStartOffset (),
                                para.getAllTextEndOffset ());

        pm.shiftBy (-1 * para.getAllTextStartOffset ());

        // Get the markup items.
        List<Markup.MarkupItem> items = pm.items;

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
                p.getContent ().add (this.createRun (ptext.substring (0, end),
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
                    p.getContent ().add (this.createRun (ptext.substring (lastEnd, Math.min(item.start, textLength)),
                                                         null));

                }

                if ((end >= textLength)
                    ||
                    (end == -1)
                   )
                {

                    end = textLength;

                }

                p.getContent ().add (this.createRun (ptext.substring (start, end),
                                                     item));

                lastEnd = end;

            }

            // Check the last item.
            Markup.MarkupItem litem = items.get (items.size () - 1);

            // Does it end before the end of the text, if so then add normal text.
            if (litem.end < textLength)
            {

                // Add the last text.
                p.getContent ().add (this.createRun (ptext.substring (litem.end),
                                                     null));

            }

        } else {

            p.getContent ().add (this.createRun (ptext,
                                                 null));

        }

    }

    private org.docx4j.wml.R createRun (String            text,
                                        Markup.MarkupItem item)
    {

        // Create the text element
        ObjectFactory factory = Context.getWmlObjectFactory ();

        org.docx4j.wml.Text tel = factory.createText ();
        tel.setSpace ("preserve");

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

        //pr.setLang (this.lang);

        run.getContent ().add (tel);

        return run;

    }

    private void addText (String            text,
                          int               start,
                          int               end,
                          Markup.MarkupItem item,
                          P                 para)
    {

        // Create the text element
        ObjectFactory factory = Context.getWmlObjectFactory ();

        org.docx4j.wml.Text tel = factory.createText ();

        String t = text;

        if ((start > -1)
            &&
            (end > -1)
           )
        {

            t = text.substring (start,
                                end);

        }

        if ((start > -1)
            &&
            (end == -1)
           )
        {

            t = text.substring (start);

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

        run.getContent ().add (tel);

        para.getContent ().add (run);

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

    private void addTo (MainDocumentPart        mp,
                        WordprocessingMLPackage pk,
                        Chapter                 c)
                 throws Exception
    {

        StringWithMarkup sm = c.getText ();

        if ((sm == null)
            ||
            (sm.getText () == null)
           )
        {

            return;

        }

        Body b = mp.getContents ().getBody ();

        ObjectFactory factory = Context.getWmlObjectFactory ();

        Br br = new Br ();
        br.setType (STBrType.PAGE);
        P brp = factory.createP ();
        brp.getContent ().add (br);

        b.getContent ().add (brp);

        mp.addStyledParagraphOfText (HEADING1,
                                     c.getName ());

        // Get the markup, if present.
        TextIterator iter = new TextIterator (sm.getText ());

        for (Paragraph p : iter.getParagraphs ())
        {

            this.addParagraph (p,
                               sm.getMarkup (),
                               NORMAL,
                               mp,
                               b);

        }

    }

    private void addTo (MainDocumentPart        mp,
                        WordprocessingMLPackage pk,
                        Asset                   a)
                 throws Exception
    {

        mp.addStyledParagraphOfText (HEADING1,
                                     a.getName ());

        Body b = mp.getContents ().getBody ();

        // Get the handlers.
        Set<UserConfigurableObjectFieldViewEditHandler> handlers = a.getViewEditHandlers (null);

        for (UserConfigurableObjectFieldViewEditHandler h : handlers)
        {

            // TODO: This has GOT to change.
            if (h instanceof ObjectNameUserConfigurableObjectFieldViewEditHandler)
            {

                continue;

            }

            Object val = h.getFieldValue ();

            if ((val == null)
                ||
                ((val instanceof StringWithMarkup) && (!((StringWithMarkup) val).hasText ()))
               )
            {

                continue;

            }

            mp.addStyledParagraphOfText (SUBTITLE,
                                         h.getTypeField ().getFormName ());

            if (h instanceof ImageUserConfigurableObjectFieldViewEditHandler)
            {

                File f = this.proj.getFile (val.toString ());

                if (!f.exists ())
                {

                    continue;

                }

                this.addImage (pk,
                               f);

                continue;

            }

            StringWithMarkup sm = null;

            if (val instanceof String)
            {

                sm = new StringWithMarkup ((String) val);

            }

            if (val instanceof Date)
            {

                sm = new StringWithMarkup (Environment.formatDate ((Date) val));

            }

            if (val instanceof Number)
            {

                sm = new StringWithMarkup (Environment.formatNumber ((Double) val));

            }

            if (val instanceof Set)
            {

                sm = new StringWithMarkup (Utils.joinStrings ((Set<String>) val, null));

            }

            if (val instanceof StringWithMarkup)
            {

                sm = (StringWithMarkup) val;

            }

            if (sm == null)
            {

                continue;

            }

            TextIterator iter = new TextIterator (sm.getText ());

            for (Paragraph p : iter.getParagraphs ())
            {

                this.addParagraph (p,
                                   sm.getMarkup (),
                                   NORMAL,
                                   mp,
                                   b);

            }

        }

    }

    private void addTo (MainDocumentPart        mp,
                        WordprocessingMLPackage pk,
                        Note                    note)
                 throws Exception
    {

        Body b = mp.getContents ().getBody ();

        mp.addStyledParagraphOfText (SUBTITLE,
                                     note.getType ());

        StringWithMarkup sm = note.getDescription ();

        TextIterator iter = new TextIterator (sm.getText ());

        for (Paragraph p : iter.getParagraphs ())
        {

            this.addParagraph (p,
                               sm.getMarkup (),
                               NORMAL,
                               mp,
                               b);

        }

    }

    private void addTo (MainDocumentPart        mp,
                        WordprocessingMLPackage pk,
                        OutlineItem             item)
                 throws Exception
    {

        Body b = mp.getContents ().getBody ();

        StringWithMarkup sm = item.getDescription ();

        TextIterator iter = new TextIterator (sm.getText ());

        for (Paragraph p : iter.getParagraphs ())
        {

            this.addParagraph (p,
                               sm.getMarkup (),
                               NORMAL,
                               mp,
                               b);

        }

    }

    private void addTo (MainDocumentPart        mp,
                        WordprocessingMLPackage pk,
                        Scene                   scene)
                 throws Exception
    {

        Body b = mp.getContents ().getBody ();

        StringWithMarkup sm = scene.getDescription ();

        TextIterator iter = new TextIterator (sm.getText ());

        for (Paragraph p : iter.getParagraphs ())
        {

            this.addParagraph (p,
                               sm.getMarkup (),
                               NORMAL,
                               mp,
                               b);

        }

    }

    private void addTo (MainDocumentPart        mp,
                        WordprocessingMLPackage pk,
                        NamedObject             n)
                 throws Exception
    {

        if (n instanceof Scene)
        {

            this.addTo (mp,
                        pk,
                        ((Scene) n));

            return;

        }

        if (n instanceof OutlineItem)
        {

            this.addTo (mp,
                        pk,
                        ((OutlineItem) n));

            return;

        }

        if (n instanceof Asset)
        {

            this.addTo (mp,
                        pk,
                        ((Asset) n));

            return;

        }

        if (n instanceof Chapter)
        {

            this.addTo (mp,
                        pk,
                        ((Chapter) n));

            return;

        }

        Body b = mp.getContents ().getBody ();

        // TODO: Export markup for asset descriptions.
        String prefix = "";
        String text = n.getDescriptionText ();

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

        name = Utils.sanitizeForFilenameKeepCase (name);

        File f = new File (this.settings.outputDirectory.getPath () + "/" + name + Constants.DOCX_FILE_EXTENSION);

        wordMLPackage.save (f);

    }

    @Override
    public void exportProject (Path    dir,
                               Project itemsToExport)
                        throws GeneralException
    {

        ExportSettings es = new ExportSettings ();
        es.outputDirectory = dir.toFile ();
        es.chapterExportType = ((this.exportChaptersType2.getSelectionModel ().getSelectedIndex () == 0) ? ExportSettings.SINGLE_FILE : ExportSettings.INDIVIDUAL_FILE);
        es.otherExportType = ((this.exportOthersType2.getSelectionModel ().getSelectedIndex () == 0) ? ExportSettings.SINGLE_FILE : ExportSettings.INDIVIDUAL_FILE);

        this.settings = es;

        ObjectFactory factory = Context.getWmlObjectFactory ();

        this.lang = factory.createCTLanguage ();

        this.lang.setVal (this.getLanguageCode (this.proj));

        Project p = itemsToExport;

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
                                                  String.format (getUIString (exportproject,sectiontitles,notes),
                                                                 p.getName ()));
                                                  //p.getName () + " - Notes");

                boolean hasNotes = false;

                WordprocessingMLPackage outlineWordMLPackage = WordprocessingMLPackage.createPackage ();

                MainDocumentPart outlinemp = outlineWordMLPackage.getMainDocumentPart ();

                this.setStyles (outlinemp,
                                true);

                outlinemp.addStyledParagraphOfText (TITLE,
                                                    String.format (getUIString (exportproject,sectiontitles,scenesoutlineitems),
                                                                   p.getName ()));
                                                    //p.getName () + " - Scenes and Plot Outline");

                boolean hasOutline = false;

                WordprocessingMLPackage chapterInfoWordMLPackage = WordprocessingMLPackage.createPackage ();

                MainDocumentPart chapinfmp = chapterInfoWordMLPackage.getMainDocumentPart ();

                this.setStyles (chapinfmp,
                                true);

                chapinfmp.addStyledParagraphOfText (TITLE,
                                                    String.format (getUIString (exportproject,sectiontitles,chapterinfo),
                                                                   p.getName ()));
                                                    //p.getName () + " - Chapter Information");

                boolean hasChapInf = false;

                if (p.getBooks ().size () > 0)
                {

                    List<Chapter> chapters = p.getBooks ().get (0).getChapters ();

                    for (Chapter c : chapters)
                    {

                        this.addTo (mp,
                                    wordMLPackage,
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
                                   String.format (getUIString (exportproject,sectiontitles,notes),
                                                  p.getName ()));
                                   //p.getName () + " - Notes");

                    }

                    if (hasOutline)
                    {

                        this.save (outlineWordMLPackage,
                                   String.format (getUIString (exportproject,sectiontitles,scenesoutlineitems),
                                                  p.getName ()));
                                   //p.getName () + " - Scenes and Plot Outline");

                    }

                    if (hasChapInf)
                    {

                        this.save (chapterInfoWordMLPackage,
                                   String.format (getUIString (exportproject,sectiontitles,chapterinfo),
                                                  p.getName ()));
                                   //p.getName () + " - Chapter Information");

                    }

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
                                                      String.format (getUIString (exportproject,sectiontitles,notes),
                                                                     c.getName ()));
                                                      //c.getName () + " - Notes");

                    WordprocessingMLPackage outlineWordMLPackage = WordprocessingMLPackage.createPackage ();

                    MainDocumentPart outlinemp = outlineWordMLPackage.getMainDocumentPart ();

                    this.setStyles (outlinemp,
                                    true);

                    outlinemp.addStyledParagraphOfText (TITLE,
                                                        String.format (getUIString (exportproject,sectiontitles,scenesoutlineitems),
                                                                       c.getName ()));
                                                        //c.getName () + " - Scenes and Plot Outline");

                    WordprocessingMLPackage chapterInfoWordMLPackage = WordprocessingMLPackage.createPackage ();

                    MainDocumentPart chapinfmp = chapterInfoWordMLPackage.getMainDocumentPart ();

                    this.setStyles (chapinfmp,
                                    true);

                    chapinfmp.addStyledParagraphOfText (TITLE,
                                                        String.format (getUIString (exportproject,sectiontitles,chapterinfo),
                                                                       c.getName ()));
                                                        //c.getName () + " - Chapter Information");

                    this.addTo (mp,
                                wordMLPackage,
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
                                   String.format (getUIString (exportproject,sectiontitles,chapterinfo),
                                                  c.getName ()));
                                   //c.getName () + " - Chapter Information");

                    }

                    if (c.getNotes ().size () > 0)
                    {

                        this.save (outlineWordMLPackage,
                                   String.format (getUIString (exportproject,sectiontitles,scenesoutlineitems),
                                                  c.getName ()));
                                   //c.getName () + " - Scenes and Plot Outline");

                    }

                    if ((c.getScenes ().size () > 0)
                        ||
                        (c.getOutlineItems ().size () > 0)
                       )
                    {

                        this.save (notesWordMLPackage,
                                   String.format (getUIString (exportproject,sectiontitles,notes),
                                                  c.getName ()));
                                   //c.getName () + " - Notes");

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
                                             String.format (getUIString (exportproject,sectiontitles,assets),
                                                            p.getName ()));
                                             //p.getName () + " - Assets");

                List<NamedObject> objs = new ArrayList (p.getAllNamedChildObjects (Asset.class));

                if (objs.size () > 0)
                {

                    Collections.sort (objs,
                                      new NamedObjectSorter (itemsToExport));

                    for (NamedObject n : objs)
                    {

                        this.addTo (mp,
                                    wordMLPackage,
                                    n);

                    }

                    this.save (wordMLPackage,
                               String.format (getUIString (exportproject,sectiontitles,assets),
                                              p.getName ()));
                               //p.getName () + " - Assets");

                }

            }

            if (settings.otherExportType == ExportSettings.INDIVIDUAL_FILE)
            {

                Set<UserConfigurableObjectType> assetTypes = p.getAssetUserConfigurableObjectTypes (true);

                for (UserConfigurableObjectType t : assetTypes)
                {

                    this.writeItemsFromProject (p,
                                                t);

                }

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

    private void writeItemsFromProject (Project                    p,
                                        UserConfigurableObjectType type)
                                        throws  GeneralException
    {

        String title = p.getName () + " - " + type.getObjectTypeNamePlural ();

        try
        {

            List<NamedObject> objs = new ArrayList (p.getAllNamedChildObjects (type));

            if (objs.size () == 0)
            {

                return;

            }

            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage ();

            MainDocumentPart mp = wordMLPackage.getMainDocumentPart ();

            this.setStyles (mp,
                            false);

            mp.addStyledParagraphOfText (TITLE,
                                         title);

            Collections.sort (objs,
                              new NamedObjectSorter (p));

            for (NamedObject n : objs)
            {

                this.addTo (mp,
                            wordMLPackage,
                            n);

            }

            this.save (wordMLPackage,
                       title);

        } catch (Exception e) {

            throw new GeneralException ("Unable to write items of type: " +
                                        type.getObjectTypeName () +
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

            String goals = (c.getGoals () != null ? c.getGoals ().getText () : null);
            String plan = (c.getPlan () != null ? c.getPlan ().getText () : null);

            if (goals != null)
            {

                chapinfmp.addStyledParagraphOfText (HEADING2,
                                                    getUIString (exportproject,sectiontitles,goals));

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
                                                    getUIString (exportproject,sectiontitles,plan));

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
                                // TODO: Fix this
                                null,
                                n);

                } else {

                    this.addTo (outlinemp,
                                null,
                                n);

                    if (n instanceof Scene)
                    {

                        Scene s = (Scene) n;

                        Set<OutlineItem> its = s.getOutlineItems ();

                        for (OutlineItem it : its)
                        {

                            this.addTo (outlinemp,
                                        null,
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
                     throws Exception
    {

        // Sort out the styles.
        Styles styles = null;

        try
        {

            styles = mp.getStyleDefinitionsPart ().getContents ();

        } catch (Exception e) {

            throw new GeneralException ("Unable to get styles",
                                        e);

        }

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

                rpr.setLang (this.lang);

                RFonts rf = rpr.getRFonts ();

                if (rf == null)
                {

                    rf = factory.createRFonts ();
                    rpr.setRFonts (rf);

                }

                rf.setAscii (UserProperties.get (Constants.EDITOR_FONT_PROPERTY_NAME));
                rf.setHAnsi (UserProperties.get (Constants.EDITOR_FONT_PROPERTY_NAME));
                rf.setCs (UserProperties.get (Constants.EDITOR_FONT_PROPERTY_NAME));

                rf.setHAnsiTheme (null);
                rf.setCstheme (null);
                rf.setAsciiTheme (null);

                // rpr.sz - font size.
                if (s.getStyleId ().equals (NORMAL))
                {

                    HpsMeasure m = factory.createHpsMeasure ();

                    // Size is 1/144 of an inch, so just double the value.

                    int fontSize = UserProperties.getAsInt (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME);

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

                    String align = UserProperties.get (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME).toUpperCase ();

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

                    float spacing = UserProperties.getAsFloat (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME);

                    spac.setAfterLines (java.math.BigInteger.valueOf ((long) (100 * spacing)));
                    // spac.setBeforeLines (java.math.BigInteger.valueOf ((long) 300));

                    // Line spacing is expressed in 240ths of a line.
                    spac.setLine (java.math.BigInteger.valueOf ((long) (240 * spacing)));

                    spac.setLineRule (STLineSpacingRule.valueOf ("AUTO"));

                    // ind - paragraph indentation.
                    if ((indent) &&
                        (UserProperties.getAsBoolean (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME)))
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

	public P addImage (WordprocessingMLPackage mp,
                       File                    file)
                throws Exception
    {

        byte[] bytes = UIUtils.getImageBytes (UIUtils.getScaledImage (UIUtils.getImage (file.toPath ()),
                                                                      250));

        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart (mp, bytes);

        Inline inline = imagePart.createImageInline (null,
                                                     null,
                                                     0,
                                                     1,
                                                     false);

        // Now add the inline in w:p/w:r/w:drawing
		ObjectFactory factory = Context.getWmlObjectFactory ();
		P  p = factory.createP ();
		R  run = factory.createR ();
		p.getContent ().add (run);
		Drawing drawing = factory.createDrawing ();
		run.getContent ().add (drawing);
		drawing.getAnchorOrInline().add (inline);

		mp.getMainDocumentPart ().addObject (p);

		return p;

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

    private String getLanguageCode (Project p)
    {

        // Ref: http://docwiki.embarcadero.com/RADStudio/Tokyo/en/Language_Culture_Names,_Codes,_and_ISO_Values
        // The first part is the language, the second part is the culture.  Not sure how this affects how
        // the document content will behave though.

        String langCode = p.getLanguageCodeForSpellCheckLanguage ();

        if (langCode.equals ("en"))
        {

            return langCode + "-US";

        }

        if (langCode.equals ("cs"))
        {

            return langCode + "-CZ";

        }

        if (langCode.equals ("nl"))
        {

            return langCode + "-NL";

        }

        if (langCode.equals ("fr"))
        {

            return langCode + "-FR";

        }

        if (langCode.equals ("de"))
        {

            return langCode + "-DE";

        }

        if (langCode.equals ("it"))
        {

            return langCode + "-IT";

        }

        if (langCode.equals ("pl"))
        {

            return langCode + "-PL";

        }

        if (langCode.equals ("ru"))
        {

            return langCode + "-RU";

        }

        if (langCode.equals ("es"))
        {

            return langCode + "-ES";

        }

        throw new IllegalArgumentException ("Language code: " + langCode + ", not supported.");

    }

}
