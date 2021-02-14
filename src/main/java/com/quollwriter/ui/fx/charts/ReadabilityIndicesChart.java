package com.quollwriter.ui.fx.charts;

import java.util.*;
import java.text.*;

import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.shape.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.util.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ReadabilityIndicesChart extends VBox implements QuollChart
{

    public static final String CHART_TYPE = "readability-index";

    private Node controls = null;
    private VBox chartWrapper = null;
    private QuollVerticalBarChart<Number, String> chart = null;
    private CheckBox showAvg = null;
    private CheckBox showTargets = null;
    private CheckBox showGF = null;
    private CheckBox showFK = null;
    private Set<Chapter> selected = null;

    private SimpleLongProperty chapterCountProp = new SimpleLongProperty ();
    private SimpleDoubleProperty avgGFProp = new SimpleDoubleProperty ();
    private SimpleDoubleProperty avgFKProp = new SimpleDoubleProperty ();
    private SimpleLongProperty filteredCountProp = new SimpleLongProperty ();
    private SimpleFloatProperty totalGFProp = new SimpleFloatProperty ();
    private SimpleFloatProperty totalFKProp = new SimpleFloatProperty ();

    private AbstractProjectViewer viewer = null;

    public ReadabilityIndicesChart (AbstractProjectViewer viewer)
    {

        this.viewer = viewer;

        this.selected = new LinkedHashSet<> (viewer.getProject ().getBook (0).getChapters ());
        this.controls = this.createControls ();
        this.chartWrapper = new VBox ();
        VBox.setVgrow (this.chartWrapper,
                       Priority.ALWAYS);
        this.getChildren ().addAll (this.chartWrapper, this.createDetails ());

    }

    @Override
    public String getType ()
    {

        return CHART_TYPE;

    }

    @Override
    public Node getChart ()
    {

        return this;//.chart;

    }

    private void createChart ()
    {

        this.chartWrapper.getChildren ().clear ();

        CategoryAxis xAxis = new CategoryAxis ();
        NumberAxis yAxis = new NumberAxis ();

        xAxis.labelProperty ().bind (getUILanguageStringProperty (charts,perchapter,labels,xaxis));
        yAxis.labelProperty ().bind (getUILanguageStringProperty (charts,perchapter,labels,yaxis));

        this.chart = new QuollVerticalBarChart<> (yAxis, xAxis);

        this.chart.prefWidthProperty ().bind (this.widthProperty ());
        this.chart.prefHeightProperty ().bind (this.heightProperty ());

        try
        {

            this.chart.getData ().addAll (this.getData ());

        } catch (Exception e) {

            Environment.logError ("Unable to get sessions",
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (charts,actionerror));
                                      //"Unable to sessions");

        }

        final SimpleDateFormat dateFormat = new SimpleDateFormat ("hh:mm a, EEE, dd MMM yyyy");

        // Again with the stupid, have to do this AFTER the data has been added...
        /*
        for (XYChart.Data<String, Number> d : this.chart.getData ().get (0).getData ())
        {

            Session s = (Session) d.getExtraValue ();

            // TODO Maybe use a custom node?
            UIUtils.setTooltip (d.getNode (),
                                getUILanguageStringProperty (Arrays.asList (charts,sessionlength,tooltip),
                                                             // TODO Make the date format configurable...
                                                             dateFormat.format (s.getStart ()),
                                                             Utils.formatAsDuration (s.getEnd ().getTime () - s.getStart ().getTime ())));

        }
*/

        TargetsData ptargs = this.viewer.getProjectTargets ();

        int targetGF = ptargs.getReadabilityGF ();
        int targetFK = ptargs.getReadabilityFK ();

        double avgGF = 0;
        double avgFK = 0;

        long chapterCount = this.chapterCountProp.getValue ();
        float totalFK = this.totalFKProp.getValue ();
        float totalGF = this.totalGFProp.getValue ();

        if (chapterCount > 0)
        {

            avgGF = totalGF / chapterCount;
            avgFK = totalFK / chapterCount;

        }

        this.avgGFProp.setValue (avgGF);
        this.avgFKProp.setValue (avgFK);

        double diffAvgGF = avgGF - targetGF;

        boolean showGF = this.showGF.isSelected ();
        boolean showFK = this.showFK.isSelected ();

        if (this.showAvg.isSelected ())
        {

            String tgf = "";

            if ((targetGF > 0)
                &&
                (showGF)
               )
            {

                tgf = getUILanguageStringProperty (Arrays.asList (charts,readability,markers,averagegfsuffix),
                                                   (diffAvgGF < 0 ? "" : "+") + Environment.formatNumber (diffAvgGF)).getValue ();

            }

            this.chart.addValueMarker (this.chart.createMarker (QuollLabel.builder ()
                                                                            .styleClassName (StyleClassNames.AVERAGEGF)
                                                                            .label (getUILanguageStringProperty (Arrays.asList (charts,readability,markers,averagegf),
                                                                                                                 Environment.formatNumber (avgGF),
                                                                                                                 tgf))
                                                                            .build (),
                                                                          StyleClassNames.AVERAGEGF,
                                                                          avgGF));
            String tfk = "";

            if ((targetFK > 0)
                &&
                (showFK)
               )
            {

               double diffAvgFK = avgFK - targetFK;

               tfk = getUILanguageStringProperty (Arrays.asList (charts,readability,markers,averagefksuffix),
                                   //", %s%s target",
                                    (diffAvgFK < 0 ? "" : "+") + Environment.formatNumber (diffAvgFK)).getValue ();

            }

            this.chart.addValueMarker (this.chart.createMarker (QuollLabel.builder ()
                                                                            .styleClassName (StyleClassNames.AVERAGEFK)
                                                                            .label (getUILanguageStringProperty (Arrays.asList (charts,readability,markers,averagefk),
                                                                                                                 Environment.formatNumber (avgFK),
                                                                                                                 tfk))
                                                                            .build (),
                                                                          StyleClassNames.AVERAGEFK,
                                                                          avgFK));

        }

        if (this.showTargets.isSelected ())
        {

            if ((targetGF > 0)
                &&
                (showGF)
               )
            {

                this.chart.addValueMarker (this.chart.createMarker (QuollLabel.builder ()
                                                                        .styleClassName (StyleClassNames.TARGETGF)
                                                                        .label (getUILanguageStringProperty (Arrays.asList (charts,readability,markers,targetgf),
                                                                                //"GF Target %s",
                                                                                                             Environment.formatNumber (targetGF)))
                                                                        .build (),
                                                                    StyleClassNames.TARGETGF,
                                                                    targetGF));

            }

            if ((targetFK > 0)
                &&
                (showFK)
               )
            {

                this.chart.addValueMarker (this.chart.createMarker (QuollLabel.builder ()
                                                                        .styleClassName (StyleClassNames.TARGETFK)
                                                                        .label (getUILanguageStringProperty (Arrays.asList (charts,readability,markers,targetfk),
                                                                                //"GF Target %s",
                                                                                                             Environment.formatNumber (targetFK)))
                                                                        .build (),
                                                                    StyleClassNames.TARGETFK,
                                                                    targetFK));

            }

        }

        this.chartWrapper.getChildren ().add (this.chart);

    }

    @Override
    public State getState ()
    {

        State s = new State ();
        s.set ("showtargets",
               this.showTargets.isSelected ());
        s.set ("showaverage",
               this.showAvg.isSelected ());
        s.set ("showgf",
               this.showGF.isSelected ());
        s.set ("showfk",
               this.showFK.isSelected ());
        return s;

    }

    @Override
    public void init (State s)
    {

        if (s == null)
        {

            return;

        }

        this.showAvg.setSelected (s.getAsBoolean ("showaverage", false));
        this.showTargets.setSelected (s.getAsBoolean ("showtargets", false));
        this.showFK.setSelected (s.getAsBoolean ("showfk", false));
        this.showGF.setSelected (s.getAsBoolean ("showgf", false));

        this.createChart ();

    }

    private Node createDetailItem (StringProperty prop)
    {

        HBox item = new HBox ();
        item.getStyleClass ().add (StyleClassNames.ITEM);
        item.getChildren ().add (new ImageView ());
        item.getChildren ().add (BasicHtmlTextFlow.builder ()
            .text (prop)
            .build ());

        return item;

    }

    private Node createDetails ()
    {

        //VBox b = new VBox ();

        List<String> prefix = Arrays.asList (charts,readability,labels);

        FlowPane b = new FlowPane ();
        b.getStyleClass ().add (StyleClassNames.DETAIL);

        TargetsData ptargs = this.viewer.getProjectTargets ();

        int targetGF = ptargs.getReadabilityGF ();
        int targetFK = ptargs.getReadabilityFK ();

        boolean showGF = this.showGF.isSelected ();
        boolean showFK = this.showFK.isSelected ();

        int overGF = 0;
        int overFK = 0;

        for (Chapter c : selected)
        {

            ReadabilityIndices ri = this.viewer.getReadabilityIndices (c);

            float fk = ri.getFleschKincaidGradeLevel ();
            float gf = ri.getGunningFogIndex ();

            if (fk > targetFK)
            {

                overFK++;

            }

            if (gf > targetGF)
            {

                overGF++;

            }

        }

        if ((targetFK > 0)
            &&
            (overFK > 0)
            &&
            (showFK)
           )
        {

            b.getChildren ().add (this.createDetailItem (QuollHyperlink.builder ()
                .tooltip (getUILanguageStringProperty (charts,readability,labels,clicktoview))
                .label (getUILanguageStringProperty (Arrays.asList (charts,readability,labels,overtargetfk),
                                         //"%s {Chapter%s} over target Flesch-Kincaid",
                                          Environment.formatNumber (overFK)))
                .onAction (ev ->
                {

                    this.viewer.showChaptersOverReadabilityTarget ();

                })
                .build ()));

        }

        if ((targetGF > 0)
            &&
            (overGF > 0)
            &&
            (showGF)
           )
        {

            b.getChildren ().add (this.createDetailItem (QuollHyperlink.builder ()
                .tooltip (getUILanguageStringProperty (charts,readability,labels,clicktoview))
                .label (getUILanguageStringProperty (Arrays.asList (charts,readability,labels,overtargetgf),
                                         //"%s {Chapter%s} over target Flesch-Kincaid",
                                          Environment.formatNumber (overGF)))
                .onAction (ev ->
                {

                    this.viewer.showChaptersOverReadabilityTarget ();

                })
                .build ()));

        }

        if (this.filteredCountProp.getValue () > 0)
        {

            b.getChildren ().add (this.createDetailItem (getUILanguageStringProperty (Arrays.asList (charts,readability,excluded,plural,text),
                                                //"%s {chapters} have less than %s words and have been excluded",
                                                                        Environment.formatNumber (this.filteredCountProp.getValue ()))));
                                                //Environment.formatNumber (Constants.MIN_READABILITY_WORD_COUNT))));

        }

        if (showFK)
        {

            b.getChildren ().add (this.createDetailItem (getUILanguageStringProperty (Arrays.asList (charts,readability,labels,averagefk),
                                                            //"%s - Average Flesch-Kincaid",
                                                                                      Environment.formatNumber (this.avgFKProp.getValue ()))));

        }

        if (showGF)
        {

            b.getChildren ().add (this.createDetailItem (getUILanguageStringProperty (Arrays.asList (charts,readability,labels,averagegf),
                                                            //"%s - Average Gunning Fog",
                                                                    Environment.formatNumber (this.avgGFProp.getValue ()))));

        }

        return b;

    }

    private Node createControls ()
    {

        final ReadabilityIndicesChart _this = this;

        VBox b = new VBox ();

        FlowPane fp = new FlowPane ();

        QuollHyperlink forChapters = QuollHyperlink.builder ()
            .label (charts,readability,labels,forchapters)
            .build ();

        forChapters.setOnAction (ev ->
        {

            Set<Chapter> excl = new HashSet<> ();

            for (Chapter c : this.viewer.getProject ().getBook (0).getChapters ())
            {

                ChapterCounts cc = this.viewer.getChapterCounts (c);

                if (cc.getWordCount () < Constants.MIN_READABILITY_WORD_COUNT)
                {

                    excl.add (c);

                }

            }

            UIUtils.createChaptersSelectableTreePopup (this.viewer.getProject (),
                                                       this.viewer,
                                                       this.selected,
                                                       excl,
                                                       getUILanguageStringProperty (charts,readability,labels,forchapters),
                                                       getUILanguageStringProperty (actions,select),
                                                       chapters ->
                                                       {

                                                           this.selected = chapters;
                                                           this.createChart ();

                                                       }).show (forChapters,
                                                                Side.BOTTOM);

        });

        this.showGF = QuollCheckBox.builder ()
            .label (charts,readability,labels,showgf)
            .onAction (ev -> this.createChart ())
            .build ();

        this.showFK = QuollCheckBox.builder ()
            .label (charts,readability,labels,showfk)
            .onAction (ev -> this.createChart ())
            .build ();

        this.showAvg = QuollCheckBox.builder ()
            .label (charts,readability,labels,showaverage)
            .onAction (ev -> this.createChart ())
            .build ();

        this.showTargets = QuollCheckBox.builder ()
            .label (charts,readability,labels,showtargets)
            .onAction (ev ->
            {

                TargetsData targets = _this.viewer.getProjectTargets ();

                if ((targets.getReadabilityGF () == 0)
                    &&
                    (targets.getReadabilityFK () == 0)
                   )
                {

                    List<String> prefix = Arrays.asList (charts,readability,notargets,popup);

                    QuollPopup.questionBuilder ()
                        .withViewer (this.viewer)
                        .title (Utils.newList (prefix,title))
                        .styleClassName (StyleClassNames.TARGETS)
                        .message (Utils.newList (prefix,text))
                        .confirmButtonLabel (Utils.newList (prefix,buttons,confirm))
                        .cancelButtonLabel (Utils.newList (prefix,buttons,cancel))
                        .onConfirm (eev ->
                        {

                            try
                            {

                                _this.viewer.runCommand (AbstractViewer.CommandId.viewtargets);

                            } catch (Exception e) {

                                Environment.logError ("Unable to show targets",
                                                      e);

                                ComponentUtils.showErrorMessage (_this.viewer,
                                                                 getUILanguageStringProperty (charts,readability,notargets,actionerror));
                                                         //"Unable to show targets.");

                            }

                        })
                        .build ();
                    this.showTargets.setSelected (false);

                    return;

                }

                this.createChart ();

            })
            .build ();

        fp.getChildren ().addAll (this.showFK, this.showGF, this.showAvg, this.showTargets, forChapters);

        return fp;

    }

    public Node getControls ()
    {

        return this.controls;

    }

    public StringProperty getTitle ()
    {

        return getUILanguageStringProperty (charts,readability,title);

    }

    private List<XYChart.Series<Number, String>> getData ()
                                                   throws Exception
    {

        List<XYChart.Series<Number, String>> data = new ArrayList<> ();

        XYChart.Series<Number, String> gfseries = new XYChart.Series<> ();
        XYChart.Series<Number, String> fkseries = new XYChart.Series<> ();

        if (this.showFK.isSelected ())
        {

            data.add (fkseries);
            //fkseries.getNode ().getStyleClass ().add ("fk");

        }

        if (this.showGF.isSelected ())
        {

            data.add (gfseries);
            //gfseries.getNode ().getStyleClass ().add  ("gf");

        }

        int chapterCount = 0;
        float totalFK = 0;
        float totalGF = 0;
        float maxFK = 0;
        float maxGF = 0;
        float showMax = 0;
        int filteredCount = 0;

        for (Book book : this.viewer.getProject ().getBooks ())
        {

            List<Chapter> cs = new ArrayList<> (book.getChapters ());
            Collections.reverse (cs);

            for (Chapter c : cs)
            {

                if (!this.selected.contains (c))
                {

                    continue;

                }

                ChapterCounts cc = this.viewer.getChapterCounts (c);

                // Filter out chapters with less than the min readability word count.
                if (cc.getWordCount () < Constants.MIN_READABILITY_WORD_COUNT)
                {

                    filteredCount++;

                    continue;

                }

                ReadabilityIndices ri = this.viewer.getReadabilityIndices (c);

                float fk = ri.getFleschKincaidGradeLevel ();
                float gf = ri.getGunningFogIndex ();

                chapterCount++;
                totalFK += fk;
                totalGF += gf;

                maxFK = Math.max (maxFK, fk);
                maxGF = Math.max (maxGF, gf);

                if (this.showFK.isSelected ())
                {

                    XYChart.Data d = new XYChart.Data<> (fk,
                                                         c.getName ());

                    fkseries.getData ().add (d);

                }

                if (this.showGF.isSelected ())
                {

                    XYChart.Data d = new XYChart.Data<> (gf,
                                                         c.getName ());

                    gfseries.getData ().add (d);

                }

            }

        }

        this.totalFKProp.setValue (totalFK);
        this.totalGFProp.setValue (totalGF);
        this.chapterCountProp.setValue (chapterCount);
        this.filteredCountProp.setValue (filteredCount);

        return data;

    }

    private Node createDetailItem (Node n)
    {

        HBox item = new HBox ();
        item.getStyleClass ().add (StyleClassNames.ITEM);
        item.getChildren ().add (new ImageView ());
        item.getChildren ().add (n);

        return item;

    }

}
