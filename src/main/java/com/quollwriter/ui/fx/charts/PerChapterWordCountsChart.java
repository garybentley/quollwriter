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

public class PerChapterWordCountsChart extends VBox implements QuollChart
{

    public static final String CHART_TYPE = "per-chapter-word-counts";

    private Node controls = null;
    private VBox chartWrapper = null;
    private QuollVerticalBarChart<Number, String> chart = null;
    private CheckBox showAvg = null;
    private CheckBox showTarget = null;
    private ComboBox<StringProperty> displayB = null;
    private Set<Chapter> selected = null;

    private SimpleLongProperty totalWordsProp = new SimpleLongProperty ();

    private AbstractProjectViewer viewer = null;

    public PerChapterWordCountsChart (AbstractProjectViewer viewer)
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

        if (this.displayB.getSelectionModel ().getSelectedIndex () == 0)
        {

            this.createCurrentChart ();

            return;

        }

        this.createHistoryChart ();

    }

    private void createHistoryChart ()
    {

    }

    private void createCurrentChart ()
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

            this.chart.getData ().add (this.getData ());

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
        if (this.showAvg.isSelected ())
        {

            if (this.selected.size () > 0)
            {

                long avg = this.totalWordsProp.getValue () / this.selected.size ();

                this.chart.addValueMarker (this.chart.createMarker (QuollLabel.builder ()
                                                                                .styleClassName (StyleClassNames.AVERAGE)
                                                                                .label (new SimpleStringProperty ("Average (" + Utils.formatAsDuration (avg) + ")"))
                                                                                .build (),
                                                                              StyleClassNames.AVERAGE,
                                                                              avg));

            }

        }

        if (this.showTarget.isSelected ())
        {

            TargetsData targets = this.viewer.getProjectTargets ();

            this.chart.addValueMarker (this.chart.createMarker (QuollLabel.builder ()
                                                                            .styleClassName (StyleClassNames.TARGET)
                                                                            .label (new SimpleStringProperty ("Target (" + targets.getMaxChapterCount () + ")"))
                                                                            .build (),
                                                                          StyleClassNames.TARGET,
                                                                          targets.getMaxChapterCount ()));

        }

        this.chartWrapper.getChildren ().add (this.chart);

    }

    @Override
    public State getState ()
    {

        State s = new State ();
        s.set ("showtarget",
               this.showTarget.isSelected ());
        s.set ("showaverage",
               this.showAvg.isSelected ());
        s.set ("for",
               this.displayB.getSelectionModel ().getSelectedIndex ());
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
        this.showTarget.setSelected (s.getAsBoolean ("showtarget", false));
        this.displayB.getSelectionModel ().select (s.getAsInt ("for", 0));

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

        List<String> prefix = Arrays.asList (charts,sessionlength,labels);

        FlowPane b = new FlowPane ();
        b.getStyleClass ().add (StyleClassNames.DETAIL);

        return b;

    }

    private Node createControls ()
    {

        final PerChapterWordCountsChart _this = this;

        VBox b = new VBox ();

        FlowPane fp = new FlowPane ();
        fp.getChildren ().add (QuollLabel.builder ()
            .label (charts,perchapter,labels,_for)
            .build ());
/*
        b.getChildren ().add (Header.builder ()
            .title (charts,sessionwordcount,labels,_for)
            .build ());
*/
        List<StringProperty> displayItems = new ArrayList<> ();
        displayItems.add (getUILanguageStringProperty (times,thisweek));//"This week");
        displayItems.add (getUILanguageStringProperty (times,lastweek));//"Last week");
        displayItems.add (getUILanguageStringProperty (times,thismonth));//"This month");
        displayItems.add (getUILanguageStringProperty (times,lastmonth));//"Last month");
        displayItems.add (getUILanguageStringProperty (times,alltime));//"All time");

        this.displayB = new ComboBox<> ();
        this.displayB.getItems ().addAll (displayItems);
        this.displayB.getSelectionModel ().select (0);
        this.displayB.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            this.createChart ();

        });

        Callback<ListView<StringProperty>, ListCell<StringProperty>> cellFactory = (lv ->
        {

            return new ListCell<StringProperty> ()
            {

                @Override
                protected void updateItem (StringProperty item,
                                           boolean        empty)
                {

                    super.updateItem (item,
                                      empty);

                    if (empty || item == null)
                    {

                        this.textProperty ().unbind ();
                        setText ("");

                    } else {

                        this.textProperty ().bind (item);

                    }

                }

            };

        });

        this.displayB.setCellFactory (cellFactory);
        this.displayB.setButtonCell (cellFactory.call (null));

        QuollHyperlink forChapters = QuollHyperlink.builder ()
            .label (charts,perchapter,labels,forchapters)
            .build ();

        forChapters.setOnAction (ev ->
        {

            UIUtils.createChaptersSelectableTreePopup (this.viewer.getProject (),
                                                       this.viewer,
                                                       this.selected,
                                                       null,
                                                       getUILanguageStringProperty (charts,perchapter,labels,forchapters),
                                                       getUILanguageStringProperty (actions,select),
                                                       chapters ->
                                                       {

                                                           this.selected = chapters;
                                                           this.createChart ();

                                                       }).show (forChapters,
                                                                Side.BOTTOM);

        });

        this.showAvg = QuollCheckBox.builder ()
            .label (charts,perchapter,labels,showaverage)
            .onAction (ev -> this.createChart ())
            .build ();

        this.showTarget = QuollCheckBox.builder ()
            .label (charts,perchapter,labels,showtarget)
            .onAction (ev ->
            {

                TargetsData targets = Environment.getUserTargets ();

                if ((targets.getMySessionWriting () == 0)
                   )
                {

                    List<String> prefix = Arrays.asList (charts,perchapter,notarget,popup);

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
                                                                 getUILanguageStringProperty (charts,perchapter,notarget,actionerror));
                                                         //"Unable to show targets.");

                            }

                        })
                        .build ();
                    this.showTarget.setSelected (false);

                    return;

                }

                this.createChart ();

            })
            .build ();

        fp.getChildren ().addAll (this.displayB, forChapters, this.showAvg, this.showTarget);

        return fp;

    }

    public Node getControls ()
    {

        return this.controls;

    }

    public StringProperty getTitle ()
    {

        return getUILanguageStringProperty (charts,perchapter,title);

    }

    private XYChart.Series<Number, String> getData ()
                                             throws Exception
    {

        XYChart.Series<Number, String> series = new XYChart.Series<> ();

        int days = -1;

        Date minDate = null;
        Date maxDate = new Date ();

        int selInd = this.displayB.getSelectionModel ().getSelectedIndex ();

        // This week.
        if (selInd == 0)
        {

            // Work out how many days there have been this week.
            GregorianCalendar gc = new GregorianCalendar ();

            days = gc.get (Calendar.DAY_OF_WEEK) - gc.getFirstDayOfWeek ();

            if (days < 0)
            {

                days -= 7;

            }

            gc.add (Calendar.DATE,
                    -1 * days);

            minDate = gc.getTime ();

        }

        // Last week
        if (selInd == 1)
        {

            GregorianCalendar gc = new GregorianCalendar ();

            days = gc.get (Calendar.DAY_OF_WEEK) - gc.getFirstDayOfWeek ();

            if (days < 0)
            {

                days -= 7;

            }

            gc.add (Calendar.DATE,
                    (-1 * days) - 1);

            maxDate = gc.getTime ();

            days += 7;

            gc.add (Calendar.DATE,
                    -6);

            minDate = gc.getTime ();

        }

        // This month.
        if (selInd == 2)
        {

            GregorianCalendar gc = new GregorianCalendar ();

            days = gc.get (Calendar.DATE);

            gc.set (Calendar.DATE,
                    1);

            minDate = gc.getTime ();

        }

        // Last month.
        if (selInd == 3)
        {

            GregorianCalendar gc = new GregorianCalendar ();

            gc.add (Calendar.MONTH,
                    -1);

            days = gc.getActualMaximum (Calendar.DATE);

            gc.set (Calendar.DATE,
                    days);

            maxDate = gc.getTime ();

            gc.set (Calendar.DATE,
                    1);

            minDate = gc.getTime ();

        }

        // All time
        if (selInd == 4)
        {

            days = -1;

        }

        int chapterCount = 0;
        long totalWords = 0;
        long maxWords = 0;
        int target = Environment.getUserTargets ().getMaxChapterCount ();

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

                if (cc.getWordCount () > 0)
                {

                    chapterCount++;

                }

                totalWords += cc.getWordCount ();

                if (cc.getWordCount () > maxWords)
                {

                    maxWords = cc.getWordCount ();

                }

                XYChart.Data d = new XYChart.Data<> (cc.getWordCount (),
                                                     c.getName ());

                series.getData ().add (d);

            }

        }

        this.totalWordsProp.setValue (totalWords);

        return series;

    }

}
