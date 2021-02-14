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

public class SessionWordCountChart extends VBox implements QuollChart
{

    public static final String CHART_TYPE = "session-word-count";

    private Node controls = null;
    //private Region details = null;
    private VBox chartWrapper = null;
    private QuollBarChart<String, Number> chart = null;
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

        xAxis.labelProperty ().bind (getUILanguageStringProperty (charts,sessionwordcount,labels,xaxis));
        yAxis.labelProperty ().bind (getUILanguageStringProperty (charts,sessionwordcount,labels,yaxis));

        this.chart = new QuollBarChart<> (xAxis, yAxis);

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

        if (this.showAvg.isSelected ())
        {

            //this.getChildren ().add (this.avgLine);

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

            Integer avg = (int) (this.totalWordsProp.getValue () / this.sessionsProp.getValue ());

            this.chart.addHorizontalValueMarker (this.chart.createMarker (QuollLabel.builder ()
                                                                            .styleClassName (StyleClassNames.AVERAGE)
                                                                            .label (new SimpleStringProperty ("Average (" + Environment.formatNumber (avg) + ")"))
                                                                            .build (),
                                                                          StyleClassNames.AVERAGE,
                                                                          avg));

        }

        if (this.showTarget.isSelected ())
        {

            TargetsData userTargets = Environment.getUserTargets ();

            this.chart.addHorizontalValueMarker (this.chart.createMarker (QuollLabel.builder ()
                                                                            .styleClassName (StyleClassNames.TARGET)
                                                                            .label (new SimpleStringProperty ("Target (" + userTargets.getMySessionWriting () + ")"))
                                                                            .build (),
                                                                          StyleClassNames.TARGET,
                                                                          userTargets.getMySessionWriting ()));

        }

        this.chartWrapper.getChildren ().add (this.chart);

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

        List<String> prefix = Arrays.asList (charts,sessionwordcount,labels);

        FlowPane b = new FlowPane ();
        b.getStyleClass ().add (StyleClassNames.DETAIL);

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

        return b;

    }

    private Node createControls ()
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

        this.showAvg = QuollCheckBox.builder ()
            .label (charts,sessionwordcount,labels,showaverage)
            .onAction (ev -> this.createChart ())
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

                this.createChart ();

            })
            .build ();

        fp.getChildren ().addAll (this.displayB, this.showAvg, this.showTarget);

        return fp;

    }

    public Node getControls ()
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

    }

}
