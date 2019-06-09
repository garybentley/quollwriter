package com.quollwriter.ui.fx.panels;

import java.util.*;

import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.*;

import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.charts.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class StatisticsPanel extends PanelContent<AbstractViewer>
{

    public static final String PANEL_ID = "statistics";

    private StackPane controlPane = null;
    private StackPane chartPane = null;
    private StackPane detailPane = null;
    private List<QuollChart> charts = null;
    private Map<String, String> chartIds = new HashMap<> ();
    private QuollChart currentChart = null;
    private SimpleObjectProperty<QuollChart> chartProp = null;
    private ComboBox<QuollChart> chartSel = null;

    public StatisticsPanel (AbstractViewer viewer,
                            Set<Node>      headerCons,
                            QuollChart...  charts)
                     throws GeneralException
    {

        super (viewer);

        this.chartProp = new SimpleObjectProperty<QuollChart> ();

        SimpleStringProperty title =  new SimpleStringProperty ();
        title.bind (Bindings.createStringBinding (() ->
        {

            if (this.currentChart == null)
            {

                return "";

            }

            return String.format (UILanguageStringsManager.getUIString (statistics,LanguageStrings.title),
                                  this.currentChart.getTitle ().getValue ());

        },
        UILanguageStringsManager.uilangProperty (),
        this.chartProp));

        this.chartProp.addListener ((pr, oldv, newv) ->
        {

        });

        Header h = Header.builder ()
            .title (title)
            .controls (headerCons)
            .build ();

        //SplitPane content = new SplitPane ();
        VBox content = new VBox ();
        VBox.setVgrow (content,
                       Priority.ALWAYS);

        //VBox sidebar = new VBox ();

        this.controlPane = new StackPane ();
        this.chartPane = new StackPane ();
        this.detailPane = new StackPane ();
        this.detailPane.getStyleClass ().add (StyleClassNames.DETAIL);

        for (QuollChart qc : charts)
        {

            String uuid = UUID.randomUUID ().toString ();

            Node cons = qc.getControls ();
            cons.setId ("controls" + uuid);

            Region chart = qc.getChart ();
            chart.setId ("chart" + uuid);

            chart.prefWidthProperty ().bind (this.chartPane.widthProperty ());
            chart.prefHeightProperty ().bind (this.chartPane.heightProperty ());

            Node dets = qc.getDetails ();
            dets.setId ("details" + uuid);

            this.chartIds.put (qc.getType (),
                               uuid);

            this.controlPane.getChildren ().add (cons);
            this.chartPane.getChildren ().add (chart);
            this.detailPane.getChildren ().add (dets);

        }

        this.charts = Arrays.asList (charts);

        HBox hb = new HBox ();
        hb.getStyleClass ().add (StyleClassNames.CONTROLS);

        hb.getChildren ().add (QuollLabel.builder ()
            .label (statistics,sectiontitles,show)
            .build ());

        // Add the charts selector.
        this.chartSel = new ComboBox<> ();
        chartSel.getItems ().addAll (this.charts);
        chartSel.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            this.updateChart (newv);
            this.currentChart = newv;
            this.chartProp.setValue (newv);

        });

        Callback<ListView<QuollChart>, ListCell<QuollChart>> cellFactory = (lv ->
        {

            return new ListCell<QuollChart> ()
            {

                @Override
                protected void updateItem (QuollChart item,
                                           boolean    empty)
                {

                    super.updateItem (item,
                                      empty);

                    if (empty || item == null)
                    {

                        this.textProperty ().unbind ();
                        setText ("");

                    } else {

                        this.textProperty ().bind (item.getTitle ());

                    }

                }

            };

        });

        chartSel.setCellFactory (cellFactory);
        chartSel.setButtonCell (cellFactory.call (null));

        hb.getChildren ().add (this.chartSel);
        hb.getChildren ().add (this.controlPane);

        //sidebar.getChildren ().add (this.chartSel);
        //sidebar.getChildren ().add (this.controlPane);
/*
        sidebar.getChildren ().add (Header.builder ()
            .title (statistics,sectiontitles,detail)
            .build ());

        sidebar.getChildren ().add (this.detailPane);
*/
        //SplitPane.setResizableWithParent (chartPane, true);
        VBox.setVgrow (chartPane,
                       Priority.ALWAYS);
        content.getChildren ().addAll (hb, /*new ScrollPane (sidebar),*/ chartPane, this.detailPane);
        //content.setDividerPositions (0.2f);

        VBox b = new VBox ();
        b.getChildren ().addAll (h, content);

        this.getChildren ().add (b);

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        super.init (s);

        if (s == null)
        {

            QuollChart qc = this.charts.iterator ().next ();

            this.chartSel.getSelectionModel ().select (qc);
            this.updateChart (qc);

            return;

        }

        String t = s.getAsString ("chart");

        if (t == null)
        {

            t = "";

        }

        QuollChart show = null;

        for (QuollChart qc : this.charts)
        {

            if (show == null)
            {

                show = qc;

            }

            State qs = s.getAsState (qc.getType ());

            qc.init (qs);

            if (qc.getType ().equals (t))
            {

                show = qc;

            }

        }

        if (show == null)
        {

            show = this.charts.iterator ().next ();

        }

        this.chartSel.getSelectionModel ().select (show);
        this.updateChart (show);

    }

    @Override
    public State getState ()
    {

        State s = super.getState ();

        for (QuollChart qc : this.charts)
        {

            s.set (qc.getType (),
                   qc.getState ());

        }

        s.set ("chart",
               this.currentChart.getType ());

        return s;

    }

    private void updateChart (QuollChart chart)
    {

        String uid = this.chartIds.get (chart.getType ());

        this.controlPane.lookup ("#controls" + uid).toFront ();
        this.chartPane.lookup ("#chart" + uid).toFront ();
        this.detailPane.lookup ("#details" + uid).toFront ();

        this.currentChart = chart;
        this.chartProp.setValue (this.currentChart);

    }

    @Override
    public Panel createPanel ()
    {

        SimpleStringProperty title =  new SimpleStringProperty ();
        title.bind (Bindings.createStringBinding (() ->
        {

            if (this.currentChart == null)
            {

                return "";

            }

            return String.format (UILanguageStringsManager.getUIString (statistics,LanguageStrings.title),
                                  this.currentChart.getTitle ().getValue ());

        },
        UILanguageStringsManager.uilangProperty (),
        this.chartProp));

        Panel panel = Panel.builder ()
            .title (title)
            .content (this)
            .styleClassName (StyleClassNames.STATISTICS)
            .panelId (PANEL_ID)
            // TODO .headerControls ()
            .toolbar (() ->
            {

                return new LinkedHashSet<Node> ();

            })
            .build ();

        return panel;

    }

}
