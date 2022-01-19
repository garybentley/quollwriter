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

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class TargetsSideBar<E extends AbstractViewer> extends SideBarContent
{

    public static final String SIDEBAR_ID = "targets";

    private CheckBox sessWarn = null;
    private CheckBox chsWarn = null;

    public TargetsSideBar (E viewer)
    {

        super (viewer);

        final TargetsData userTargets = Environment.getUserTargets ();

        QuollSpinner sessionS = QuollSpinner.builder ()
            .initialValue (userTargets.getMySessionWriting ())
            .stepBy (10)
            .build ();

        sessionS.valueProperty ().addListener ((p, oldv, newv) ->
        {

            userTargets.setMySessionWriting (newv);

            Environment.saveUserTargets ();

        });

        QuollSpinner dailyS = QuollSpinner.builder ()
            .initialValue (userTargets.getMyDailyWriting ())
            .stepBy (10)
            .build ();

        dailyS.valueProperty ().addListener ((p, oldv, newv) ->
        {

            userTargets.setMyDailyWriting (newv);

            Environment.saveUserTargets ();

        });

        QuollSpinner weeklyS = QuollSpinner.builder ()
            .initialValue (userTargets.getMyWeeklyWriting ())
            .stepBy (50)
            .build ();

        weeklyS.valueProperty ().addListener ((p, oldv, newv) ->
        {

            userTargets.setMyWeeklyWriting (newv);

            Environment.saveUserTargets ();

        });

        QuollSpinner monthlyS = QuollSpinner.builder ()
            .initialValue (userTargets.getMyMonthlyWriting ())
            .stepBy (100)
            .build ();

        monthlyS.valueProperty ().addListener ((p, oldv, newv) ->
        {

            userTargets.setMyMonthlyWriting (newv);

            Environment.saveUserTargets ();

        });

        List<String> prefix = Arrays.asList (project,LanguageStrings.sidebar,targets,labels);

        Form f = Form.builder ()
            .layoutType (Form.LayoutType.column)
            .item (getUILanguageStringProperty (Utils.newList (prefix,session)),
                  this.createWordsSpinnerLine (sessionS))
            .item (getUILanguageStringProperty (Utils.newList (prefix,daily)),
                   this.createWordsSpinnerLine (dailyS))
            .item (getUILanguageStringProperty (Utils.newList (prefix,weekly)),
                   this.createWordsSpinnerLine (weeklyS))
            .item (getUILanguageStringProperty (Utils.newList (prefix,monthly)),
                   this.createWordsSpinnerLine (monthlyS))
            .build ();

        this.sessWarn = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (Utils.newList (prefix,showmessagewhentargetreached)))
            .selected (userTargets.isShowMessageWhenSessionTargetReached ())
            .onAction (ev ->
            {

                userTargets.setShowMessageWhenSessionTargetReached (this.sessWarn.isSelected ());

                Environment.saveUserTargets ();

            })
            .build ();

        QuollHyperlink history = QuollHyperlink.builder ()
            .styleClassName (StyleClassNames.CHART)
            .label (getUILanguageStringProperty (Utils.newList (prefix,showdetail)))
            .onAction (ev ->
            {

                try
                {

                    viewer.runCommand (AbstractViewer.CommandId.charts,
                                       null,
                                       SessionWordCountChart.CHART_TYPE);

                } catch (Exception e) {

                    Environment.logError ("Unable to show session word counts",
                                          e);

                    ComponentUtils.showErrorMessage (viewer,
                                                     getUILanguageStringProperty (charts,view,actionerror));
                                              //"Unable to show chart.");

                }

            })
            .build ();

        VBox content = new VBox ();
        this.getChildren ().add (content);

        VBox wb = new VBox ();

        wb.getChildren ().addAll (f, sessWarn, history);

        AccordionItem my = AccordionItem.builder ()
            .title (project,LanguageStrings.sidebar,targets,sectiontitles,mywriting)
            .styleClassName (StyleClassNames.MYWRITING)
            .openContent (wb)
            .build ();

        content.getChildren ().add (my);

        if (this.viewer instanceof AbstractProjectViewer)
        {

            final AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

            final TargetsData projTargets = pv.getProjectTargets ();

            QuollSpinner chapterS = QuollSpinner.builder ()
                .initialValue (projTargets.getMaxChapterCount ())
                .stepBy (50)
                .build ();

            chapterS.valueProperty ().addListener ((p, oldv, newv) ->
            {

                // TODO Make a property.
                projTargets.setMaxChapterCount (newv);

                pv.saveProjectTargets ();

            });

            f = Form.builder ()
                .layoutType (Form.LayoutType.column)
                .item (getUILanguageStringProperty (Utils.newList (prefix,maximum)),
                      this.createWordsSpinnerLine (chapterS))
                .build ();

            this.chsWarn = QuollCheckBox.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,showwarningwhenchapterexceedsmax)))
                .selected (userTargets.isShowMessageWhenSessionTargetReached ())
                .onAction (ev ->
                {

                    userTargets.setShowMessageWhenMaxChapterCountExceeded (this.chsWarn.isSelected ());

                    pv.saveProjectTargets ();

                })
                .build ();

            QuollHyperlink chsOver = QuollHyperlink.builder ()
                .styleClassName (StyleClassNames.WARNING)
                .label (getUILanguageStringProperty (Arrays.asList (project,LanguageStrings.sidebar,targets,labels,chaptersovermaxtarget),
                                                     Environment.formatNumber (pv.getChaptersOverWordTarget ().size ())))
                .onAction (ev ->
                {

                    pv.runCommand (AbstractProjectViewer.CommandId.chaptersoverwordcounttarget);

                })
                .build ();

            chsOver.managedProperty ().bind (chsOver.visibleProperty ());
            chsOver.setVisible (pv.getChaptersOverWordTarget ().size () > 0);
            this.getBinder ().addSetChangeListener (pv.chaptersOverWordCountTarget (),
                                                    ev ->
            {

                UIUtils.runLater (() ->
                {

                    chsOver.setVisible (pv.chaptersOverWordCountTarget ().size () > 0);
                    chsOver.textProperty ().unbind ();
                    chsOver.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (project,LanguageStrings.sidebar,targets,labels,chaptersovermaxtarget),
                                                                               Environment.formatNumber (pv.chaptersOverWordCountTarget ().size ())));

                });

            });

            chapterS.valueProperty ().addListener ((p, oldv, newv) ->
            {

                int cc = pv.getChaptersOverWordTarget ().size ();

                chsOver.setVisible (cc > 0);

                if (cc > 0)
                {

                    chsOver.textProperty ().unbind ();
                    chsOver.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (project,LanguageStrings.sidebar,targets,labels,chaptersovermaxtarget),
                                                                               Environment.formatNumber (cc)));

                }

            });

            history = QuollHyperlink.builder ()
                .styleClassName (StyleClassNames.CHART)
                .label (getUILanguageStringProperty (Utils.newList (prefix,showdetail)))
                .onAction (ev ->
                {

                    try
                    {

                        pv.runCommand (AbstractViewer.CommandId.charts,
                                       PerChapterWordCountsChart.CHART_TYPE);

                    } catch (Exception e) {

                        Environment.logError ("Unable to show per chapter word counts",
                                              e);

                        ComponentUtils.showErrorMessage (pv,
                                                         getUILanguageStringProperty (charts,actionerror));
                                                  //"Unable to show chart.")

                    }

                })
                .build ();

            VBox chsb = new VBox ();

            chsb.getChildren ().addAll (f, this.chsWarn, chsOver, history);

            AccordionItem chs = AccordionItem.builder ()
                .title (project,LanguageStrings.sidebar,targets,sectiontitles,chapterwordcount)
                .styleClassName (StyleClassNames.CHAPTER)
                .openContent (chsb)
                .build ();

            content.getChildren ().add (chs);

            QuollSpinner fkS = QuollSpinner.builder ()
                .initialValue (projTargets.getReadabilityFK ())
                .max (30)
                .build ();

            fkS.valueProperty ().addListener ((p, oldv, newv) ->
            {

                projTargets.setReadabilityFK (newv);

                pv.saveProjectTargets ();

            });

            QuollSpinner gfS = QuollSpinner.builder ()
                .initialValue (projTargets.getReadabilityGF ())
                .max (20)
                .build ();

            gfS.valueProperty ().addListener ((p, oldv, newv) ->
            {

                projTargets.setReadabilityGF (newv);

                pv.saveProjectTargets ();

            });

            QuollHyperlink rOver = QuollHyperlink.builder ()
                .styleClassName (StyleClassNames.WARNING)
                .label (getUILanguageStringProperty (Arrays.asList (project,LanguageStrings.sidebar,targets,labels,chaptersoverreadabilitytarget),
                                                     Environment.formatNumber (pv.getChaptersOverReadabilityTarget ().size ())))
                .onAction (ev ->
                {

                    pv.runCommand (AbstractProjectViewer.CommandId.chaptersoverreadabilitytarget);

                })
                .build ();

            f = Form.builder ()
                .layoutType (Form.LayoutType.column)
                .item (getUILanguageStringProperty (Utils.newList (prefix,fk)),
                       fkS)
                .item (getUILanguageStringProperty (Utils.newList (prefix,gf)),
                       gfS)
                .build ();

            rOver.managedProperty ().bind (rOver.visibleProperty ());
            rOver.setVisible (pv.getChaptersOverReadabilityTarget ().size () > 0);

            fkS.valueProperty ().addListener ((p, oldv, newv) ->
            {

                Set<Chapter> chaps = pv.getChaptersOverReadabilityTarget ();

                final int cc = chaps.size ();

                rOver.setVisible (cc > 0);

                if (cc > 0)
                {

                    rOver.textProperty ().unbind ();
                    rOver.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (project,LanguageStrings.sidebar,targets,labels,chaptersoverreadabilitytarget),
                                                                             Environment.formatNumber (cc)));

                }

            });

            gfS.valueProperty ().addListener ((p, oldv, newv) ->
            {

                Set<Chapter> chaps = pv.getChaptersOverReadabilityTarget ();

                final int cc = chaps.size ();

                rOver.setVisible (cc > 0);

                if (cc > 0)
                {

                    rOver.textProperty ().unbind ();
                    rOver.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (project,LanguageStrings.sidebar,targets,labels,chaptersoverreadabilitytarget),
                                                                             Environment.formatNumber (cc)));

                }

            });

            history = QuollHyperlink.builder ()
                .styleClassName (StyleClassNames.CHART)
                .label (getUILanguageStringProperty (Utils.newList (prefix,showdetail)))
                .onAction (ev ->
                {

                    try
                    {

                        pv.runCommand (AbstractViewer.CommandId.charts,
                                       ReadabilityIndicesChart.CHART_TYPE);

                    } catch (Exception e) {

                        Environment.logError ("Unable to show per chapter word counts",
                                              e);

                        ComponentUtils.showErrorMessage (pv,
                                                         getUILanguageStringProperty (charts,actionerror));
                                                  //"Unable to show chart.")

                    }

                })
                .build ();

            VBox rsb = new VBox ();

            rsb.getChildren ().addAll (f, rOver, history);

            AccordionItem rs = AccordionItem.builder ()
                .title (project,LanguageStrings.sidebar,targets,sectiontitles,readability)
                .styleClassName (StyleClassNames.READABILITY)
                .openContent (rsb)
                .build ();

            content.getChildren ().add (rs);

        }

    }

/*
    public void showChaptersOverReadabilityTarget ()
    {

        AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

        Set<Chapter> chaps = pv.getChaptersOverReadabilityTarget ();

        if (chaps.size () == 0)
        {

            return;

        }

        TargetsData projTargets = pv.getProjectTargets ();

        int tcc = projTargets.getMaxChapterCount ();
        int tfk = projTargets.getReadabilityFK ();
        int tgf = projTargets.getReadabilityGF ();

        // TODO Use a TableView?

        GridPane gp = new GridPane ();

        ColumnConstraints cc1 = new ColumnConstraints ();
        cc1.setHgrow (Priority.ALWAYS);
        cc1.setFillWidth (true);
        cc1.setHalignment (HPos.LEFT);

        ColumnConstraints cc2 = new ColumnConstraints ();
        cc2.setHgrow (Priority.NEVER);
        cc2.setHalignment (HPos.RIGHT);

        ColumnConstraints cc3 = new ColumnConstraints ();
        cc3.setHgrow (Priority.NEVER);
        cc3.setHalignment (HPos.RIGHT);

        ColumnConstraints cc4 = new ColumnConstraints ();
        cc4.setHgrow (Priority.NEVER);
        cc4.setHalignment (HPos.RIGHT);

        ColumnConstraints cc5 = new ColumnConstraints ();
        cc5.setHgrow (Priority.NEVER);
        cc5.setHalignment (HPos.RIGHT);

        gp.getColumnConstraints ().addAll (cc1, cc2, cc3, cc4, cc5);

        int r = 0;

        QuollLabel colH = QuollLabel.builder ()
            .label (targets,chaptersoverreadabilitymaximum,detail,popup,labels,fk)
            .build ();

        gp.setColumnIndex (colH, 1);
        gp.setRowIndex (colH, r);

        colH = QuollLabel.builder ()
            .label (targets,chaptersoverreadabilitymaximum,detail,popup,labels,gf)
            .build ();

        gp.setColumnIndex (colH, 3);
        gp.setRowIndex (colH, r);

        r++;

        for (Chapter c : chaps)
        {

            QuollHyperlink l = QuollHyperlink.builder ()
                .styleClassName (StyleClassNames.NAME)
                .label (c.nameProperty ())
                .tooltip (actions,clicktoview)
                .onAction (ev ->
                {

                    try
                    {

                        pv.viewObject (c);

                    } catch (Exception e) {

                        Environment.logError ("Unable to view chapter: " +
                                              c,
                                              e);

                    }

                })
                .build ();
            l.getStyleClass ().add (StyleClassNames.LABEL);
            GridPane.setRowIndex (l, r);
            GridPane.setColumnIndex (l, 0);

            ReadabilityIndices ri = pv.getReadabilityIndices (c);

            float fk = ri.getFleschKincaidGradeLevel ();
            float gf = ri.getGunningFogIndex ();

            QuollLabel fkl = QuollLabel.builder ()
                .label (new SimpleStringProperty (Environment.formatNumber (fk)))
                .build ();
            GridPane.setRowIndex (fkl, r);
            GridPane.setColumnIndex (fkl, 1);

            float diffFK = fk - tfk;
            float diffGF = gf - tgf;

            String diffFKl = "-";

            if (diffFK > 0)
            {

                diffFKl = String.format ("+%s",
                                         Environment.formatNumber (diffFK));

            }

            QuollLabel fkd = QuollLabel.builder ()
                .label (new SimpleStringProperty (diffFKl))
                .build ();
            GridPane.setRowIndex (fkd, r);
            GridPane.setColumnIndex (fkd, 2);

            QuollLabel gfl = QuollLabel.builder ()
                .label (new SimpleStringProperty (Environment.formatNumber (gf)))
                .build ();
            GridPane.setRowIndex (gfl, r);
            GridPane.setColumnIndex (gfl, 3);

            String diffGFl = "-";

            if (diffGF > 0)
            {

                diffGFl = String.format ("+%s",
                                         Environment.formatNumber (diffGF));

            }

            QuollLabel gfd = QuollLabel.builder ()
                .label (new SimpleStringProperty (diffGFl))
                .build ();
            GridPane.setRowIndex (gfd, r);
            GridPane.setColumnIndex (gfd, 4);

            r++;

        }

        VBox content = new VBox ();

        content.getChildren ().add (gp);

        Set<Button> buttons = new LinkedHashSet<> ();
        QuollButton close = QuollButton.builder ()
            .label (targets,chaptersoverreadabilitymaximum,detail,popup,LanguageStrings.buttons,LanguageStrings.close)
            .build ();
        buttons.add (close);

        QuollPopup qp = ComponentUtils.showMessage (pv,
                                                    StyleClassNames.READABILITY,
                                                    getUILanguageStringProperty (targets,chaptersoverreadabilitymaximum,detail, LanguageStrings.popup,title),
                                                    content,
                                                    buttons);

    }
/*
/*
    public void showChaptersOverWordTarget ()
    {

        AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

        Set<Chapter> chaps = pv.getChaptersOverWordTarget ();

        if (chaps.size () == 0)
        {

            return;

        }

        TargetsData projTargets = pv.getProjectTargets ();

        int tcc = projTargets.getMaxChapterCount ();

        GridPane gp = new GridPane ();

        ColumnConstraints cc1 = new ColumnConstraints ();
        cc1.setHgrow (Priority.ALWAYS);
        cc1.setFillWidth (true);
        cc1.setHalignment (HPos.LEFT);

        ColumnConstraints cc2 = new ColumnConstraints ();
        cc2.setHgrow (Priority.NEVER);
        cc2.setHalignment (HPos.RIGHT);

        ColumnConstraints cc3 = new ColumnConstraints ();
        cc3.setHgrow (Priority.NEVER);
        cc3.setHalignment (HPos.RIGHT);

        gp.getColumnConstraints ().addAll (cc1, cc2, cc3);

        boolean hasOver25 = false;
        int r = 0;

        for (Chapter c : chaps)
        {

            QuollHyperlink l = QuollHyperlink.builder ()
                .styleClassName (StyleClassNames.NAME)
                .tooltip (actions,clicktoview)
                .onAction (ev ->
                {

                    try
                    {

                        pv.viewObject (c);

                    } catch (Exception e) {

                        Environment.logError ("Unable to view chapter: " +
                                              c,
                                              e);

                    }

                })
                .build ();
            l.getStyleClass ().add (StyleClassNames.LABEL);
            GridPane.setRowIndex (l, r);
            GridPane.setColumnIndex (l, 0);

            ChapterCounts count = pv.getChapterCounts (c);

            QuollLabel wl = QuollLabel.builder ()
                .styleClassName (StyleClassNames.COUNT)
                .label (new SimpleStringProperty (String.format ("%s",
                                                                 Environment.formatNumber (count.wordCount))))
                .build ();

            GridPane.setRowIndex (wl, r);
            GridPane.setColumnIndex (wl, 1);

            int diff = count.wordCount - tcc;

            int perc = Utils.getPercent (diff, tcc);

            QuollLabel ol = QuollLabel.builder ()
                .label (new SimpleStringProperty (String.format ("+%s",
                                                                 Environment.formatNumber (count.wordCount - tcc))))
                .styleClassName (StyleClassNames.WORDCOUNT)
                .build ();

            GridPane.setRowIndex (ol, r);
            GridPane.setColumnIndex (ol, 2);

            if (perc >= 25)
            {

                ol.getStyleClass ().add (StyleClassNames.OVER25PERC);

                hasOver25 = true;

            }

            r++;

        }

        VBox content = new VBox ();

        BasicHtmlTextFlow desc = BasicHtmlTextFlow.builder ()
            .text (getUILanguageStringProperty (Arrays.asList (targets,chaptersoverwcmaximum,detail,popup,text),
                                                //"Current maximum {chapter} word count <b>%s words</b>.",
                                                Environment.formatNumber (tcc)))
            .withViewer (this.viewer)
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        content.getChildren ().add (desc);

        if (hasOver25)
        {

            BasicHtmlTextFlow over = BasicHtmlTextFlow.builder ()
                .text (getUILanguageStringProperty (Arrays.asList (targets,chaptersoverwcmaximum,detail,popup,overlimit)))
                .withViewer (this.viewer)
                .styleClassName (StyleClassNames.OVER25PERC)
                .build ();

            content.getChildren ().add (over);

        }

        content.getChildren ().add (gp);

        Set<Button> buttons = new LinkedHashSet<> ();
        QuollButton close = QuollButton.builder ()
            .label (targets,chaptersoverwcmaximum,detail,popup,LanguageStrings.buttons,LanguageStrings.close)
            .build ();
        buttons.add (close);

        QuollPopup qp = ComponentUtils.showMessage (pv,
                                                    StyleClassNames.WORDCOUNTS,
                                                    getUILanguageStringProperty (targets,chaptersoverwcmaximum,detail, LanguageStrings.popup,title),
                                                    content,
                                                    buttons);

    }
*/
    private Node createWordsSpinnerLine (Spinner s)
    {

        HBox b = new HBox ();

        QuollLabel l = QuollLabel.builder ()
                            .label (getUILanguageStringProperty (Arrays.asList (project,LanguageStrings.sidebar,targets,labels,words)))
                            .build ();

        HBox.setHgrow (l, Priority.ALWAYS);

        b.getChildren ().addAll (s, l);
        return b;

    }

    @Override
    public SideBar createSideBar ()
    {

        StringProperty title = getUILanguageStringProperty (project,LanguageStrings.sidebar,targets,LanguageStrings.title);

        Set<Node> headerCons = new LinkedHashSet<> ();
        headerCons.add (UIUtils.createHelpPageButton (this.viewer,
                                                      "targets",
                                                      null));

        return SideBar.builder ()
            .title (title)
            .activeTitle (title)
            //.contextMenu ()?
            .styleClassName (StyleClassNames.TARGETS)
            .styleSheet (StyleClassNames.TARGETS)
            .headerIconClassName (StyleClassNames.TARGETS)
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
