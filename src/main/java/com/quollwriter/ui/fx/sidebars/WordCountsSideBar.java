package com.quollwriter.ui.fx.sidebars;

import java.util.*;

import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.charts.*;
import com.quollwriter.ui.fx.panels.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import com.quollwriter.uistrings.UILanguageStringsManager;
import static com.quollwriter.LanguageStrings.*;

public class WordCountsSideBar extends SideBarContent<AbstractProjectViewer>
{

    public static final String SIDEBAR_ID = "wordcounts";

    private Label selectedWordCount = null;
    private Label selectedFleschKincaid = null;
    private Label selectedGunningFog = null;
    private Label chapterEditPosition = null;
    private Label chapterFleschKincaid = null;
    private Label chapterFleschReading = null;
    private Label chapterGunningFog = null;
    private Label chapterA4PageCount = null;
    private Label chapterWordCount = null;

    public WordCountsSideBar (AbstractProjectViewer viewer)
    {

        super (viewer);

        AccordionItem selText = AccordionItem.builder ()
            .title (project,LanguageStrings.sidebar,wordcount,sectiontitles,selected)
            .styleClassName (StyleClassNames.SELECTED)
            .openContent (this.createSelectedTextSection ())
            .build ();
        selText.managedProperty ().bind (selText.visibleProperty ());
        selText.setVisible (viewer.getSelectedText () != null);
        selText.visibleProperty ().bind (Bindings.createBooleanBinding (() ->
        {

            return viewer.selectedTextProperty ().getValue () != null;

        },
        viewer.selectedTextProperty ()));

        AccordionItem sess = AccordionItem.builder ()
            .title (project,LanguageStrings.sidebar,wordcount,sectiontitles,session)
            .styleClassName (StyleClassNames.SESSION)
            .openContent (this.createSessionSection ())
            .build ();

        AccordionItem chap = AccordionItem.builder ()
            .title (new SimpleStringProperty (""))
            .styleClassName (StyleClassNames.CHAPTER)
            .openContent (this.createChapterSection ())
            .build ();

        if (viewer.getCurrentPanel ().getContent () instanceof ChapterEditorPanelContent)
        {

            ChapterEditorPanelContent cp = (ChapterEditorPanelContent) viewer.getCurrentPanel ().getContent ();
            chap.getHeader ().titleProperty ().unbind ();
            chap.getHeader ().titleProperty ().bind (cp.getObject ().nameProperty ());

        }

        viewer.chapterCurrentlyEditedProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv != null)
            {

                Chapter ch = viewer.getChapterCurrentlyEdited ();
                chap.getHeader ().titleProperty ().unbind ();
                chap.getHeader ().titleProperty ().bind (ch.nameProperty ());

            }

        });

        chap.managedProperty ().bind (chap.visibleProperty ());
        chap.setVisible (viewer.getCurrentPanel ().getContent () instanceof ChapterEditorPanelContent);
        chap.visibleProperty ().bind (Bindings.createBooleanBinding (() ->
        {

            return viewer.getCurrentPanel ().getContent () instanceof ChapterEditorPanelContent;

        },
        viewer.currentPanelProperty ()));

        AccordionItem allChaps = AccordionItem.builder ()
            .title (project,LanguageStrings.sidebar,wordcount,sectiontitles,allchapters)
            .styleClassName (StyleClassNames.ALLCHAPTERS)
            .openContent (this.createAllChaptersSection ())
            .build ();

        QuollHyperlink detail = QuollHyperlink.builder ()
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,wordcount,viewdetaillink))
            .styleClassName (StyleClassNames.DETAIL)
            .onAction (ev ->
            {

/*
TODO
                viewer.runCommand (ProjectViewer.CommandId.statistics,
                                   WordCountsCharts.CHART_TYPE);
*/
            })
            .build ();

        QuollHyperlink help = QuollHyperlink.builder ()
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,wordcount,helplink))
            .styleClassName (StyleClassNames.HELP)
            .onAction (ev ->
            {

                UIUtils.openURL (this.viewer,
                                 "help://chapters/readability");

            })
            .build ();

        VBox content = new VBox ();
        content.getChildren ().addAll (selText, sess, chap, allChaps, detail, help);

        this.getChildren ().add (content);

    }

    private Node createSessionSection ()
    {

        VBox content = new VBox ();

        HBox r = new HBox ();

        StringProperty pwcp = new SimpleStringProperty ();

        pwcp.bind (Bindings.createStringBinding (() ->
        {

            return Environment.formatNumber (this.viewer.getSessionWordCount ());

        },
        this.viewer.sessionWordCountProperty ()));

        Label pwc = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .label (pwcp)
            .build ();

        r.getChildren ().add (pwc);

        r.getChildren ().add (QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUELABEL)
            .label (project,LanguageStrings.sidebar,wordcount,labels,projectwords)
            .build ());

        content.getChildren ().add (r);

        StringProperty twcp = new SimpleStringProperty ();
        twcp.bind (Bindings.createStringBinding (() ->
        {

            return Environment.formatNumber (Environment.getSessionWordCount ());

        },
        Environment.sessionWordCountProperty ()));

        Label twc = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .label (twcp)
            .build ();

        r = new HBox ();

        r.getChildren ().add (twc);

        r.getChildren ().add (QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUELABEL)
            .label (project,LanguageStrings.sidebar,wordcount,labels,LanguageStrings.totalwords)
            .build ());

        content.getChildren ().add (r);

        return content;

    }

    private Node createAllChaptersSection ()
    {

        VBox content = new VBox ();

        HBox r = new HBox ();

        Label wc = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r.getChildren ().add (wc);

        r.getChildren ().add (QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUELABEL)
            .label (project,LanguageStrings.sidebar,wordcount,labels,words)
            .build ());

        content.getChildren ().add (r);

        Label a4pages = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r = new HBox ();

        r.getChildren ().add (a4pages);

        r.getChildren ().add (QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUELABEL)
            .label (project,LanguageStrings.sidebar,wordcount,labels,LanguageStrings.a4pages)
            .build ());

        content.getChildren ().add (r);

        // TODO Add sparkline

        // Now the readability.
        VBox readB = new VBox ();
        content.getChildren ().add (readB);
        readB.managedProperty ().bind (readB.visibleProperty ());
        readB.getChildren ().add (QuollLabel.builder ()
            .styleClassName (StyleClassNames.SUBTITLE)
            .label (project,LanguageStrings.sidebar,wordcount,labels,readability)
            .build ());

        Label fk = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r = new HBox ();

        r.getChildren ().addAll (fk, QuollLabel.builder ()
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,wordcount,labels,LanguageStrings.fk))
            .styleClassName (StyleClassNames.VALUELABEL)
            .build ());

        readB.getChildren ().add (r);

        r = new HBox ();

        Label fr = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r.getChildren ().addAll (fr, QuollLabel.builder ()
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,wordcount,labels,LanguageStrings.fr))
            .styleClassName (StyleClassNames.VALUELABEL)
            .build ());

        readB.getChildren ().add (r);

        r = new HBox ();

        Label gf = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r.getChildren ().addAll (gf, QuollLabel.builder ()
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,wordcount,labels,LanguageStrings.gf))
            .styleClassName (StyleClassNames.VALUELABEL)
            .build ());

        readB.getChildren ().add (r);

        //this.updateChapterValues ();
        Runnable setbindings = () ->
        {

            readB.visibleProperty ().unbind ();
            wc.textProperty ().unbind ();
            a4pages.textProperty ().unbind ();
            fk.textProperty ().unbind ();
            fr.textProperty ().unbind ();
            gf.textProperty ().unbind ();

            ChapterCounts achc = this.viewer.getAllChapterCounts ();
            ReadabilityIndices ri = this.viewer.getAllReadabilityIndices ();

            readB.visibleProperty ().bind (Bindings.createBooleanBinding (() ->
            {

                if ((achc.getWordCount () > Constants.MIN_READABILITY_WORD_COUNT)
                    &&
                    (viewer.isProjectLanguageEnglish ())
                   )
                {

                    return true;

                }

                return false;

            },
            achc.wordCountProperty (),
            UILanguageStringsManager.uilangProperty ()));

            wc.textProperty ().bind (Bindings.createStringBinding (() ->
            {

                return Environment.formatNumber (achc.getWordCount ());

            },
            achc.wordCountProperty ()));

            a4pages.textProperty ().bind (Bindings.createStringBinding (() ->
            {

                return Environment.formatNumber (achc.getStandardPageCount ());

            },
            achc.standardPageCountProperty ()));

            fk.textProperty ().bind (Bindings.createStringBinding (() ->
            {

                return Environment.formatNumber (Math.round (ri.getFleschKincaidGradeLevel ()));

            },
            ri.fleschKindcaidGradeLevelProperty (),
            UILanguageStringsManager.uilangProperty ()));

            fr.textProperty ().bind (Bindings.createStringBinding (() ->
            {

                return Environment.formatNumber (Math.round (ri.getFleschReadingEase ()));

            },
            ri.fleschReadingEaseProperty (),
            UILanguageStringsManager.uilangProperty ()));

            gf.textProperty ().bind (Bindings.createStringBinding (() ->
            {

                return Environment.formatNumber (Math.round (ri.getGunningFogIndex ()));

            },
            ri.gunningFogIndexProperty (),
            UILanguageStringsManager.uilangProperty ()));

        };

        setbindings.run ();

        return content;

    }

    private Node createChapterSection ()
    {

        VBox content = new VBox ();

        HBox r = new HBox ();

        this.chapterWordCount = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r.getChildren ().add (this.chapterWordCount);

        r.getChildren ().add (QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUELABEL)
            .label (project,LanguageStrings.sidebar,wordcount,labels,words)
            .build ());

        content.getChildren ().add (r);

        this.chapterA4PageCount = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r = new HBox ();

        r.getChildren ().add (this.chapterA4PageCount);

        r.getChildren ().add (QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUELABEL)
            .label (project,LanguageStrings.sidebar,wordcount,labels,a4pages)
            .build ());

        content.getChildren ().add (r);

        // TODO Add sparkline

        // Now the edit position.
        VBox epB = new VBox ();
        content.getChildren ().add (epB);
        epB.managedProperty ().bind (epB.visibleProperty ());

        epB.getChildren ().add (QuollLabel.builder ()
            .styleClassName (StyleClassNames.SUBTITLE)
            .label (project,LanguageStrings.sidebar,wordcount,labels,edited)
            .build ());

        this.chapterEditPosition = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r = new HBox ();

        r.getChildren ().addAll (this.chapterEditPosition, QuollLabel.builder ()
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,wordcount,labels,words))
            .styleClassName (StyleClassNames.VALUELABEL)
            .build ());

        epB.getChildren ().add (r);

        // Now the readability.
        VBox readB = new VBox ();
        content.getChildren ().add (readB);
        readB.managedProperty ().bind (readB.visibleProperty ());

        readB.getChildren ().add (QuollLabel.builder ()
            .styleClassName (StyleClassNames.SUBTITLE)
            .label (project,LanguageStrings.sidebar,wordcount,labels,readability)
            .build ());

        this.chapterFleschKincaid = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r = new HBox ();

        r.getChildren ().addAll (this.chapterFleschKincaid, QuollLabel.builder ()
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,wordcount,labels,fk))
            .styleClassName (StyleClassNames.VALUELABEL)
            .build ());

        readB.getChildren ().add (r);

        r = new HBox ();

        this.chapterFleschReading = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r.getChildren ().addAll (this.chapterFleschReading, QuollLabel.builder ()
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,wordcount,labels,fr))
            .styleClassName (StyleClassNames.VALUELABEL)
            .build ());

        readB.getChildren ().add (r);

        r = new HBox ();

        this.chapterGunningFog = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r.getChildren ().addAll (this.chapterGunningFog, QuollLabel.builder ()
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,wordcount,labels,gf))
            .styleClassName (StyleClassNames.VALUELABEL)
            .build ());

        readB.getChildren ().add (r);

        //this.updateChapterValues ();
        Runnable setbindings = () ->
        {

            epB.visibleProperty ().unbind ();
            readB.visibleProperty ().unbind ();
            this.chapterWordCount.textProperty ().unbind ();
            this.chapterA4PageCount.textProperty ().unbind ();
            this.chapterEditPosition.textProperty ().unbind ();
            this.chapterFleschKincaid.textProperty ().unbind ();
            this.chapterFleschReading.textProperty ().unbind ();
            this.chapterGunningFog.textProperty ().unbind ();

            Chapter ch = viewer.getChapterCurrentlyEdited ();

            if (ch != null)
            {

                ChapterCounts cc = viewer.getChapterCounts (ch);
                ChapterCounts achc = this.viewer.getAllChapterCounts ();
                ReadabilityIndices ri = this.viewer.getReadabilityIndices (ch);

                readB.visibleProperty ().bind (Bindings.createBooleanBinding (() ->
                {

                    if ((cc.getWordCount () > Constants.MIN_READABILITY_WORD_COUNT)
                        &&
                        (viewer.isProjectLanguageEnglish ())
                       )
                    {

                        return true;

                    }

                    return false;

                },
                cc.wordCountProperty (),
                UILanguageStringsManager.uilangProperty ()));

                epB.visibleProperty ().bind (Bindings.createBooleanBinding (() ->
                {

                    if (ch.getEditPosition () > 0)
                    {

                        // Get the text.
                        String editText = viewer.getCurrentChapterText (ch).substring (0,
                                                                                       ch.getEditPosition ());

                        return editText.trim ().length () > 0;

                    }

                    return false;

                },
                ch.editPositionProperty (),
                UILanguageStringsManager.uilangProperty ()));

                this.chapterWordCount.textProperty ().bind (Bindings.createStringBinding (() ->
                {

                    return String.format (UILanguageStringsManager.getUIString (project,LanguageStrings.sidebar,wordcount,valuepercent),
                                          cc.getWordCount (),
                                          Utils.getPercent (cc.getWordCount (), achc.getWordCount ()));

                },
                cc.wordCountProperty (),
                achc.wordCountProperty (),
                UILanguageStringsManager.uilangProperty ()));

                this.chapterA4PageCount.textProperty ().bind (Bindings.createStringBinding (() ->
                {

                    return Environment.formatNumber (cc.getStandardPageCount ());

                },
                cc.standardPageCountProperty (),
                UILanguageStringsManager.uilangProperty ()));

                this.chapterEditPosition.textProperty ().bind (Bindings.createStringBinding (() ->
                {

                    if (ch.getEditPosition () > -1)
                    {

                        String editText = viewer.getCurrentChapterText (ch).substring (0,
                                                                                       ch.getEditPosition ());

                        ChapterCounts sc = new ChapterCounts (editText);

                        return String.format (UILanguageStringsManager.getUIString (project,LanguageStrings.sidebar,wordcount,valuepercent),
                                              sc.getWordCount (),
                                              Utils.getPercent (sc.getWordCount (), cc.getWordCount ()));

                    } else {

                        return "";

                    }

                },
                cc.wordCountProperty (),
                ch.editPositionProperty (),
                UILanguageStringsManager.uilangProperty ()));

                this.chapterFleschKincaid.textProperty ().bind (Bindings.createStringBinding (() ->
                {

                    return Environment.formatNumber (Math.round (ri.getFleschKincaidGradeLevel ()));

                },
                ri.fleschKindcaidGradeLevelProperty (),
                UILanguageStringsManager.uilangProperty ()));

                this.chapterFleschReading.textProperty ().bind (Bindings.createStringBinding (() ->
                {

                    return Environment.formatNumber (Math.round (ri.getFleschReadingEase ()));

                },
                ri.fleschReadingEaseProperty (),
                UILanguageStringsManager.uilangProperty ()));

                this.chapterGunningFog.textProperty ().bind (Bindings.createStringBinding (() ->
                {

                    return Environment.formatNumber (Math.round (ri.getGunningFogIndex ()));

                },
                ri.gunningFogIndexProperty (),
                UILanguageStringsManager.uilangProperty ()));

            } else {

                epB.setVisible (false);
                readB.setVisible (false);

            }

        };

        setbindings.run ();

        viewer.chapterCurrentlyEditedProperty ().addListener ((pr, oldv, newv) ->
        {

            setbindings.run ();

        });

        return content;

    }

    private Node createSelectedTextSection ()
    {

        VBox content = new VBox ();

        HBox r = new HBox ();

        this.viewer.selectedTextProperty ().addListener ((pr, oldv, newv) ->
        {

            this.updateSelectedTextValues ();

        });

        this.selectedWordCount = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r.getChildren ().add (this.selectedWordCount);

        r.getChildren ().add (QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUELABEL)
            .label (project,LanguageStrings.sidebar,wordcount,labels,words)
            .build ());

        content.getChildren ().add (r);

        content.getChildren ().add (QuollLabel.builder ()
            .styleClassName (StyleClassNames.SUBTITLE)
            .label (project,LanguageStrings.sidebar,wordcount,labels,readability)
            .build ());

        // Now the readability.
        VBox readB = new VBox ();
        content.getChildren ().add (readB);
        readB.managedProperty ().bind (readB.visibleProperty ());

        readB.visibleProperty ().bind (Bindings.createBooleanBinding (() ->
        {

            String sel = this.viewer.getSelectedText ();

            if (sel == null)
            {

                return false;

            }

            ChapterCounts sc = new ChapterCounts (sel);

            return ((sc.getWordCount () > Constants.MIN_READABILITY_WORD_COUNT)
                    &&
                    (this.viewer.isProjectLanguageEnglish ()));

        },
        this.viewer.selectedTextProperty (),
        UILanguageStringsManager.uilangProperty ()));

        this.selectedFleschKincaid = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r = new HBox ();

        r.getChildren ().addAll (this.selectedFleschKincaid, QuollLabel.builder ()
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,wordcount,labels,fk))
            .styleClassName (StyleClassNames.VALUELABEL)
            .build ());

        readB.getChildren ().add (r);

        r = new HBox ();

        this.selectedGunningFog = QuollLabel.builder ()
            .styleClassName (StyleClassNames.VALUE)
            .build ();

        r.getChildren ().addAll (this.selectedGunningFog, QuollLabel.builder ()
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,wordcount,labels,fk))
            .styleClassName (StyleClassNames.VALUELABEL)
            .build ());

        readB.getChildren ().add (r);

        this.updateSelectedTextValues ();

        return content;

    }

    private void updateSelectedTextValues ()
    {

        String sel = this.viewer.getSelectedText ();

        int wc = 0;
        float fk = 0;
        float gf = 0;

        if (sel != null)
        {

            ChapterCounts sc = new ChapterCounts (sel);

            ReadabilityIndices ri = new ReadabilityIndices ();
            ri.add (sel);

            wc = sc.getWordCount ();
            fk = ri.getFleschKincaidGradeLevel ();
            gf = ri.getGunningFogIndex ();

        }

        this.selectedWordCount.setText (Environment.formatNumber (wc));
        this.selectedFleschKincaid.setText (Environment.formatNumber (fk));
        this.selectedGunningFog.setText (Environment.formatNumber (gf));

    }

    @Override
    public SideBar createSideBar ()
    {

        StringProperty title = getUILanguageStringProperty (project,LanguageStrings.sidebar,wordcount,LanguageStrings.title);

        Set<Node> headerCons = new LinkedHashSet<> ();
        headerCons.add (QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (project,LanguageStrings.sidebar,wordcount,headercontrols,items,statistics,tooltip))
            .styleClassName (StyleClassNames.STATISTICS)
            .build ());

        return SideBar.builder ()
            .title (title)
            .activeTitle (title)
            //.contextMenu ()?
            .styleClassName (StyleClassNames.WORDCOUNTS)
            .withScrollPane (true)
            .canClose (true)
            .headerControls (headerCons)
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (SIDEBAR_ID)
            .build ();

    }

    @Override
    public void init (State s)
    {

        super.init (s);

    }

    @Override
    public State getState ()
    {

        return super.getState ();

    }

}
