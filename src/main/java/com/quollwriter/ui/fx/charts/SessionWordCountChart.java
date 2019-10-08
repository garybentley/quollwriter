package com.quollwriter.ui.fx.charts;

import java.util.*;
import java.text.*;

import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.*;
import javafx.util.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class SessionWordCountChart extends AnchorPane implements QuollChart
{

    public static final String CHART_TYPE = "session-word-count";

    private Region controls = null;
    private Region details = null;
    private BarChart<String, Number> chart = null;
    private LineChart<String, Number> markers = null;
    private CheckBox showAvg = null;
    private CheckBox showTarget = null;
    private ComboBox<StringProperty> displayB = null;
    private SimpleObjectProperty<Session> maxWordsSessionProp = new SimpleObjectProperty<> ();
    private SimpleObjectProperty<Session> longestSessionProp = new SimpleObjectProperty<> ();
    private SimpleLongProperty maxWordsProp = new SimpleLongProperty ();
    private SimpleLongProperty maxTimeProp = new SimpleLongProperty ();
    private SimpleLongProperty longSessionsProp = new SimpleLongProperty ();
    private SimpleLongProperty totalTimeProp = new SimpleLongProperty ();
    private SimpleLongProperty totalWordsProp = new SimpleLongProperty ();
    private SimpleLongProperty sessionsProp = new SimpleLongProperty ();
    private SimpleLongProperty sessionsAboveTargetProp = new SimpleLongProperty ();

    private AbstractViewer viewer = null;

    public SessionWordCountChart (AbstractViewer viewer)
    {

        this.viewer = viewer;

        CategoryAxis xAxis = new CategoryAxis ();
        NumberAxis yAxis = new NumberAxis ();

        xAxis.labelProperty ().bind (getUILanguageStringProperty (charts,sessionwordcount,labels,xaxis));
        yAxis.labelProperty ().bind (getUILanguageStringProperty (charts,sessionwordcount,labels,yaxis));

        this.chart = new BarChart<> (xAxis, yAxis);

        CategoryAxis mxAxis = new CategoryAxis ();
        NumberAxis myAxis = new NumberAxis ();

        this.markers = new LineChart<> (mxAxis, myAxis);
        this.markers.getStyleClass ().add ("markers");
        mxAxis.setTickLabelsVisible (false);
        myAxis.setTickLabelsVisible (false);
        myAxis.setForceZeroInRange (true);
        mxAxis.setTickMarkVisible (false);
        myAxis.setTickMarkVisible (false);
        mxAxis.managedProperty ().bind (mxAxis.visibleProperty ());
        mxAxis.setVisible (false);

        this.getChildren ().add (this.chart);
        this.getChildren ().add (this.markers);

        this.chart.prefWidthProperty ().bind (this.widthProperty ());
        this.chart.prefHeightProperty ().bind (this.heightProperty ());
        this.markers.prefWidthProperty ().bind (this.widthProperty ());
        this.markers.prefHeightProperty ().bind (this.heightProperty ());

        yAxis.lowerBoundProperty ().addListener ((pr, oldv, newv) ->
        {

            myAxis.setLowerBound (newv.doubleValue ());

        });

        yAxis.upperBoundProperty ().addListener ((pr, oldv, newv) ->
        {

            myAxis.setUpperBound (newv.doubleValue ());

        });

        //this.getChildren ().add (this.chart);
        this.createControls ();
        this.createDetails ();

        this.updateChart ();

    }

    @Override
    public String getType ()
    {

        return CHART_TYPE;

    }

    @Override
    public Region getChart ()
    {

        return this;//.chart;

    }

    private void updateChart ()
    {

        this.chart.getData ().clear ();

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
        for (XYChart.Data<String, Number> d : this.chart.getData ().get (0).getData ())
        {

            Session s = (Session) d.getExtraValue ();

            // TODO Maybe use a custom node?
            UIUtils.setTooltip (d.getNode (),
                                getUILanguageStringProperty (Arrays.asList (charts,sessionwordcount,tooltip),
                                                             // TODO Make the date format configurable...
                                                             dateFormat.format (s.getStart ()),
                                                             Utils.formatAsDuration (s.getEnd ().getTime () - s.getStart ().getTime ())));

        }

        this.markers.setVisible (false);

        if ((this.showAvg.isSelected ())
            ||
            (this.showTarget.isSelected ())
           )
        {

            List<XYChart.Data<String, Number>> data = this.chart.getData ().get (0).getData ();

            if ((data == null)
                ||
                (data.size () == 0)
               )
            {

                return;

            }

            String min = data.get (0).getXValue ();
            String max = data.get (data.size () - 1).getXValue ();

            this.markers.getData ().clear ();

            XYChart.Series<String, Number> series = new XYChart.Series<> ();

            Integer avg = (int) (this.totalWordsProp.getValue () / this.sessionsProp.getValue ());

            series.getData ().add (new XYChart.Data<String, Number> (min, avg));
            series.getData ().add (new XYChart.Data<String, Number> (max, avg));

            this.markers.getData ().add (series);

            this.markers.setVisible (true);

        }

        this.chart.requestLayout ();
        this.markers.requestLayout ();
        this.requestLayout ();

    }

    @Override
    public State getState ()
    {

        State s = new State ();
        s.set ("showaverage",
               this.showAvg.isSelected ());
        s.set ("showtarget",
               this.showTarget.isSelected ());
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

    @Override
    public Region getDetails ()
    {

        return this.details;

    }

    private void createDetails ()
    {

        //VBox b = new VBox ();

        List<String> prefix = Arrays.asList (charts,sessionwordcount,labels);

        FlowPane b = new FlowPane ();

        b.getChildren ().add (this.createDetailItem (getUILanguageStringProperty (Utils.newList (prefix,numsessions),
                                                                                  this.sessionsProp)));

/*
        items.add (this.createDetailLabel (String.format (getUIString (prefix,numsessions),//"%s - Session%s",
                                                          Environment.formatNumber (sessions))));
                                                    //(sessions == 1 ? "" : "s"))));
*/

        Node sessAboveTarget = this.createDetailItem (getUILanguageStringProperty (Utils.newList (prefix,sessionsovertarget),
                                                                                   this.sessionsAboveTargetProp));

        sessAboveTarget.managedProperty ().bind (sessAboveTarget.visibleProperty ());
        sessAboveTarget.visibleProperty ().bind (this.showTarget.selectedProperty ());

        Node item = this.createDetailItem (getUILanguageStringProperty (Utils.newList (prefix,sessionsover1hr),//"%s - Session%s over 1 hr",
                                                                        longSessionsProp));
        item.managedProperty ().bind (item.visibleProperty ());

        BooleanBinding visb = Bindings.createBooleanBinding (() ->
        {

            return this.sessionsProp.getValue () > 0;

        },
        this.sessionsProp);

        item.visibleProperty ().bind (visb);

        b.getChildren ().add (item);

        StringProperty dur = new SimpleStringProperty ();
        dur.bind (Bindings.createStringBinding (() ->
        {

            if ((this.totalTimeProp.getValue () == null)
                ||
                (this.sessionsProp.getValue () == 0)
               )
            {

                return "";

            }

            // TODO Change to use strings.
            return Utils.formatAsDuration (this.totalTimeProp.getValue () / this.sessionsProp.getValue ());

        },
        this.totalTimeProp,
        this.sessionsProp));

        StringProperty avgwords = new SimpleStringProperty ();
        avgwords.bind (Bindings.createStringBinding (() ->
        {

            if ((this.totalWordsProp.getValue () == null)
                ||
                (this.sessionsProp.getValue () == 0)
               )
            {

                return "";

            }

            return Environment.formatNumber (this.totalWordsProp.getValue () / this.sessionsProp.getValue ());

        },
        this.totalWordsProp,
        this.sessionsProp));

        item = this.createDetailItem (getUILanguageStringProperty (Utils.newList (prefix,averagesession),//"%s words, %s - Average session",
                                                                   avgwords,
                                                                   dur));
        item.managedProperty ().bind (item.visibleProperty ());
        item.visibleProperty ().bind (visb);
        b.getChildren ().add (item);

        StringProperty longsesswc = new SimpleStringProperty ();
        longsesswc.bind (Bindings.createStringBinding (() ->
        {

            if (this.longestSessionProp.getValue () == null)
            {

                return "";

            }

            return Environment.formatNumber (this.longestSessionProp.getValue ().getWordCount ());

        },
        this.longestSessionProp));

        StringProperty longsessdur = new SimpleStringProperty ();
        longsessdur.bind (Bindings.createStringBinding (() ->
        {

            if (this.longestSessionProp.getValue () == null)
            {

                return "";

            }

            // TODO Change.
            return Utils.formatAsDuration (this.longestSessionProp.getValue ().getSessionDuration ());

        },
        this.longestSessionProp));

        StringProperty longsesstime = new SimpleStringProperty ();
        longsesstime.bind (Bindings.createStringBinding (() ->
        {

            if (this.longestSessionProp.getValue () == null)
            {

                return "";

            }

            return Environment.formatDateTime (this.longestSessionProp.getValue ().getStart ());

        },
        this.longestSessionProp));

        item = this.createDetailItem (getUILanguageStringProperty (Utils.newList (prefix,longestsession),
                                                                   longsesswc,
                                                                   longsessdur,
                                                                   longsesstime));
        item.managedProperty ().bind (item.visibleProperty ());
        item.visibleProperty ().bind (visb);
        b.getChildren ().add (item);

        StringProperty maxsesswc = new SimpleStringProperty ();
        maxsesswc.bind (Bindings.createStringBinding (() ->
        {

            if (this.maxWordsSessionProp.getValue () == null)
            {

                return "";

            }

            return Environment.formatNumber (this.maxWordsSessionProp.getValue ().getWordCount ());

        },
        this.maxWordsSessionProp));

        StringProperty maxsessdur = new SimpleStringProperty ();
        maxsessdur.bind (Bindings.createStringBinding (() ->
        {

            if (this.maxWordsSessionProp.getValue () == null)
            {

                return "";

            }

            // TODO Change.
            return Utils.formatAsDuration (this.maxWordsSessionProp.getValue ().getSessionDuration ());

        },
        this.maxWordsSessionProp));

        StringProperty maxsesstime = new SimpleStringProperty ();
        maxsesstime.bind (Bindings.createStringBinding (() ->
        {

            if (this.maxWordsSessionProp.getValue () == null)
            {

                return "";

            }

            return Environment.formatDateTime (this.maxWordsSessionProp.getValue ().getStart ());

        },
        this.maxWordsSessionProp));

        item = this.createDetailItem (getUILanguageStringProperty (Utils.newList (prefix,sessionmostwords),
                                                                   maxsesswc,
                                                                   maxsessdur,
                                                                   maxsesstime));
        item.managedProperty ().bind (item.visibleProperty ());
        item.visibleProperty ().bind (visb);
        b.getChildren ().add (item);

        this.details = b;

    }

    private void createControls ()
    {

        final SessionWordCountChart _this = this;

        VBox b = new VBox ();

        FlowPane fp = new FlowPane ();
        fp.getChildren ().add (QuollLabel.builder ()
            .label (charts,sessionwordcount,labels,_for)
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

            this.updateChart ();

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

        this.showAvg = QuollCheckBox.builder ()
            .label (charts,sessionwordcount,labels,showaverage)
            .onAction (ev -> this.updateChart ())
            .build ();

        this.showTarget = QuollCheckBox.builder ()
            .label (charts,sessionwordcount,labels,showtarget)
            .onAction (ev ->
            {

                TargetsData targets = Environment.getUserTargets ();

                if ((targets.getMySessionWriting () == 0)
                   )
                {

                    List<String> prefix = Arrays.asList (charts,sessionwordcount,notarget,popup);

                    QuollPopup.questionBuilder ()
                        .withViewer (this.viewer)
                        .title (prefix,title)
                        .styleClassName (StyleClassNames.TARGETS)
                        .message (prefix,text)
                        .confirmButtonLabel (prefix,buttons,confirm)
                        .cancelButtonLabel (prefix,buttons,cancel)
                        .onConfirm (eev ->
                        {

                            try
                            {

                                _this.viewer.runCommand (AbstractViewer.CommandId.viewtargets);

                            } catch (Exception e) {

                                Environment.logError ("Unable to show targets",
                                                      e);

                                ComponentUtils.showErrorMessage (_this.viewer,
                                                                 getUILanguageStringProperty (charts,sessionwordcount,notarget,actionerror));
                                                         //"Unable to show targets.");

                            }

                        })
                        .build ();
/*
TODO Remove
                    ComponentUtils.createQuestionPopup (getUILanguageStringProperty (Utils.newList (prefix,title)),
                                                        StyleClassNames.TARGETS,
                                                        getUILanguageStringProperty (Utils.newList (prefix,text)),
                                                        //"You currently have no writing targets set up.<br /><br />Would you like to set the targets now?<br /><br />Note: Targets can be accessed at any time from the {Project} menu.",
                                                        getUILanguageStringProperty (Utils.newList (prefix,buttons,confirm)),
                                                        //"Yes, show me",
                                                        getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)),
                                                        //"No, not now",
                                                        eev ->
                                                        {

                                                            try
                                                            {

                                                                _this.viewer.runCommand (AbstractViewer.CommandId.viewtargets);

                                                            } catch (Exception e) {

                                                                Environment.logError ("Unable to show targets",
                                                                                      e);

                                                                ComponentUtils.showErrorMessage (_this.viewer,
                                                                                                 getUILanguageStringProperty (charts,sessionwordcount,notarget,actionerror));
                                                                                         //"Unable to show targets.");

                                                            }

                                                        },
                                                        _this.viewer);
*/
                    this.showTarget.setSelected (false);

                    return;

                }

                this.updateChart ();

            })
            .build ();

        fp.getChildren ().addAll (this.displayB, this.showAvg, this.showTarget);

        this.controls = fp;

    }

    public Region getControls ()
    {

        return this.controls;

    }

    public StringProperty getTitle ()
    {

        return getUILanguageStringProperty (charts,sessionwordcount,title);

    }

    private XYChart.Series<String, Number> getData ()
                                             throws Exception
    {

        XYChart.Series<String, Number> series = new XYChart.Series<> ();

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

        long sessions = 0;
        long totalWords = 0;
        long totalTime = 0;
        long sessionsAboveTarget = 0;
        long longSessions = 0;
        long maxTime = 0;
        long maxWords = 0;
        Session maxWordsSession = null;
        Session longestSession = null;
        int target = Environment.getUserTargets ().getMySessionWriting ();

        for (Session s : Environment.getSessions (days))
        {

            int wc = s.getWordCount ();

            if (wc == 0)
            {

                continue;

            }

            if (wc > target)
            {

                sessionsAboveTarget++;

            }

            if (days == -1)
            {

                if (minDate == null)
                {

                    minDate = s.getStart ();

                }

                if (s.getStart ().before (minDate))
                {

                    minDate = s.getStart ();

                }

            }

            long time = s.getEnd ().getTime () - s.getStart ().getTime ();

            totalTime += time;
            totalWords += wc;
            sessions++;

            if (time > 60 * 60 * 1000)
            {

                longSessions++;

            }

            if (time > maxTime)
            {

                maxTime = time;
                longestSession = s;

            }

            if (wc > maxWords)
            {

                maxWords = wc;
                maxWordsSession = s;

            }

            XYChart.Data d = new XYChart.Data<> (Environment.formatDate (s.getStart ()),
                                                                         wc);
            d.setExtraValue (s);

            series.getData ().add (d);

        }

        this.longestSessionProp.setValue (longestSession);
        this.maxWordsSessionProp.setValue (maxWordsSession);
        this.maxWordsProp.setValue (maxWords);
        this.maxTimeProp.setValue (maxTime);
        this.longSessionsProp.setValue (longSessions);
        this.totalTimeProp.setValue (totalTime);
        this.totalWordsProp.setValue (totalWords);
        this.sessionsProp.setValue (sessions);
        this.sessionsAboveTargetProp.setValue (sessionsAboveTarget);

        return series;

/*
        if (minDate == null)
        {

            minDate = new Date ();

        }

        this.chart = QuollChartUtils.createTimeSeriesChart (getUIString (prefix,xaxis),//"Date",
                                                            getUIString (prefix,yaxis),//"Word Count",
                                                            ds);

        this.chart.removeLegend ();

        XYPlot xyplot = (XYPlot) this.chart.getPlot ();

        PeriodAxis axis = (PeriodAxis) xyplot.getDomainAxis ();

        if (minDate != null)
        {

            axis.setLowerBound (minDate.getTime ());

            axis.setUpperBound (maxDate.getTime ());

        }

        QuollChartUtils.customizePlot (xyplot);
*/
/*

        if (this.showAvg.isSelected ())
        {

            long avgWords = (sessions > 0 ? totalWords / sessions : 0);

            String t = "";

            if (target > 0)
            {

                double diffAvg = avgWords - target;

                t = String.format (getUIString (prefix,averagesuffix),//", %s%s target",
                                   (diffAvg < 0 ? "" : "+") + Environment.formatNumber ((long) diffAvg));

            }

            xyplot.addRangeMarker (QuollChartUtils.createMarker (String.format (getUIString (prefix,average),//"Avg %s%s",
                                                                                Environment.formatNumber (avgWords),
                                                                                t),
                                                                 avgWords,
                                                                 0));

        }
*/
/*
        if (this.showTarget.isSelected ())
        {

            if (target > maxWords)
            {

                ((NumberAxis) xyplot.getRangeAxis()).setUpperBound (target + 100);

            }
            xyplot.addRangeMarker (QuollChartUtils.createMarker (String.format (getUIString (prefix, LanguageStrings.target),//"Target %s",
                                                                                Environment.formatNumber (target)),
                                                                 target,
                                                                 -1));

        }
*/
/*
        final SimpleDateFormat dateFormat = new SimpleDateFormat ("hh:mm a, EEE, dd MMM yyyy");

        XYToolTipGenerator ttgen = new StandardXYToolTipGenerator ()
        {

            public String generateToolTip (XYDataset dataset,
                                           int       series,
                                           int       item)
            {

                TimePeriodValuesCollection tsc = (TimePeriodValuesCollection) dataset;

                TimePeriodValues ts = tsc.getSeries (series);

                Number n = ts.getValue (item);

                return String.format (getUIString (charts,sessionwordcount,tooltip),
                                      dateFormat.format (ts.getTimePeriod (item).getStart ()),
                                      Utils.formatAsDuration (n.intValue ()));
            }

        };
*/
/*
        ((XYBarRenderer) xyplot.getRenderer ()).setSeriesToolTipGenerator (0,
                                                                           ttgen);

        Set<JComponent> items = new LinkedHashSet ();

        items.add (this.createDetailLabel (String.format (getUIString (prefix,numsessions),//"%s - Session%s",
                                                          Environment.formatNumber (sessions))));
                                                    //(sessions == 1 ? "" : "s"))));

        if (this.showTarget.isSelected ())
        {

            items.add (this.createDetailLabel (String.format (getUIString (prefix,sessionsovertarget),//"%s - Sessions above word target",
                                                              Environment.formatNumber (sessionsAboveTarget))));

        }

        if (sessions > 0)
        {

            // Work out number of sessions over target.

            items.add (this.createDetailLabel (String.format (getUIString (prefix,sessionsover1hr),//"%s - Session%s over 1 hr",
                                                              Environment.formatNumber (longSessions))));
                                                        //(longSessions == 1 ? "" : "s"))));

            items.add (this.createDetailLabel (String.format (getUIString (prefix,averagesession),//"%s words, %s - Average session",
                                                              Environment.formatNumber (totalWords / sessions),
                                                              Utils.formatAsDuration (totalTime / sessions))));

            items.add (this.createDetailLabel (String.format (getUIString (prefix,longestsession),//"%s words, %s, %s, - Longest session",
                                                              Environment.formatNumber (longestSession.getWordCount ()),
                                                              Utils.formatAsDuration (longestSession.getSessionDuration ()),
                                                              Environment.formatDateTime (longestSession.getStart ()))));

            items.add (this.createDetailLabel (String.format (getUIString (prefix,sessionmostwords),//"%s words, %s, %s - Session with most words",
                                                              Environment.formatNumber (maxWordsSession.getWordCount ()),
                                                              Utils.formatAsDuration (maxWordsSession.getSessionDuration ()),
                                                              Environment.formatDateTime (maxWordsSession.getStart ()))));

        }

        this.detail = QuollChartUtils.createDetailPanel (items);
*/
    }

}
