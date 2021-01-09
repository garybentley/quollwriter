package com.quollwriter.ui.fx.charts;

import java.util.*;
import java.text.*;
import javafx.util.*;

import javafx.scene.*;
import javafx.beans.property.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class AllWordCountsChart extends Pane implements QuollChart
{

    public static final String CHART_TYPE = "all-word-count";

    private Region controls = null;
    private ComboBox<StringProperty> displayB = null;
    private AbstractProjectViewer viewer = null;

    public AllWordCountsChart (AbstractProjectViewer viewer)
    {

        this.viewer = viewer;

        this.createControls ();

    }

    @Override
    public Node getControls ()
    {

        return this.controls;

    }

    @Override
    public StringProperty getTitle ()
    {

        return getUILanguageStringProperty (charts,allwordcounts,title);

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

    private void createControls ()
    {

        VBox b = new VBox ();

        FlowPane fp = new FlowPane ();
        fp.getChildren ().add (QuollLabel.builder ()
            .label (charts,allwordcounts,labels,_for)
            .build ());

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

        fp.getChildren ().addAll (this.displayB);

        this.controls = fp;

    }

    private void createChart ()
    {

        this.getChildren ().clear ();

        CategoryAxis xAxis = new CategoryAxis ();
        NumberAxis yAxis = new NumberAxis ();

        xAxis.labelProperty ().bind (getUILanguageStringProperty (charts,allwordcounts,labels,xaxis));
        yAxis.labelProperty ().bind (getUILanguageStringProperty (charts,allwordcounts,labels,yaxis));

        QuollBarChart<String, Number> chart = new QuollBarChart<> (xAxis, yAxis);

        this.getChildren ().add (chart);

        chart.prefWidthProperty ().bind (this.widthProperty ());
        chart.prefHeightProperty ().bind (this.heightProperty ());

        chart.getData ().clear ();

        try
        {

            chart.getData ().add (this.getData ());

        } catch (Exception e) {

            Environment.logError ("Unable to get data",
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (charts,actionerror));
                                      //"Unable to sessions");

        }

        final SimpleDateFormat dateFormat = new SimpleDateFormat ("hh:mm a, EEE, dd MMM yyyy");

        // Again with the stupid, have to do this AFTER the data has been added...
        for (XYChart.Data<String, Number> d : chart.getData ().get (0).getData ())
        {

            Session s = (Session) d.getExtraValue ();

            // TODO Maybe use a custom node?
            /*
            UIUtils.setTooltip (d.getNode (),
                                getUILanguageStringProperty (Arrays.asList (charts,sessionwordcount,tooltip),
                                                             // TODO Make the date format configurable...
                                                             dateFormat.format (s.getEnd ()),
                                                             Utils.formatAsDuration (s.getEnd ().getTime () - s.getStart ().getTime ())));
*/
        }

    }

    private XYChart.Series<String, Number> getData ()
                                             throws Exception
    {

        XYChart.Series<String, Number> series = new XYChart.Series<> ();

        int days = -1;

        Date minDate = new Date (0);
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

        ProjectDataHandler pdh = (ProjectDataHandler) this.viewer.getDataHandler (Project.class);

        // Get all the word counts for the project.
        List<WordCount> wordCounts = pdh.getWordCounts (this.viewer.getProject (),
                                                        0);

        for (WordCount wc : wordCounts)
        {

            if ((wc.getEnd ().getTime () >= minDate.getTime ())
                &&
                (wc.getEnd ().getTime () <= maxDate.getTime ())
               )
            {

                XYChart.Data d = new XYChart.Data<> (Environment.formatDate (wc.getEnd ()),
                                                                             wc.getCount ());
                //d.setExtraValue (s);

                series.getData ().add (d);

            }

        }

        return series;

    }

    @Override
    public State getState ()
    {

        State s = new State ();
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

        this.displayB.getSelectionModel ().select (s.getAsInt ("for", 0));

        this.createChart ();

    }

}
