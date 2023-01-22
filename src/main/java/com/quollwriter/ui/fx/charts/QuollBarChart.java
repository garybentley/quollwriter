package com.quollwriter.ui.fx.charts;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.beans.*;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.scene.chart.*;

// Adapted from: https://stackoverflow.com/questions/24232931/adding-a-line-in-a-javafx-chart/24403761#24403761
public class QuollBarChart extends BarChart<String, Number>
{

    private ObservableList<Marker> horizontalMarkers;

    public QuollBarChart (CategoryAxis xAxis, ValueAxis<Number> yAxis)
    {

        super(xAxis, yAxis);
          // a list that notifies on change of the yValue property
          horizontalMarkers = FXCollections.observableArrayList();
          // listen to list changes and re-plot
          horizontalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
    }

    /**
         * Add horizontal value marker. The marker's Y value is used to plot a
         * horizontal line across the plot area, its X value is ignored.
         *
         * @param marker must not be null.
         */
        public void addHorizontalValueMarker(Marker m)
        {
            Objects.requireNonNull(m, "the marker must not be null");
            if (horizontalMarkers.contains(m)) return;
            horizontalMarkers.add (m);
            getPlotChildren().add(m.line);
            getPlotChildren ().add (m.label);
            //horizontalMarkers.add(marker);
        }

        /**
         * Remove horizontal value marker.
         *
         * @param horizontalMarker must not be null
         */
        public void removeHorizontalValueMarker(Marker m) {
            Objects.requireNonNull(m, "the marker must not be null");
                getPlotChildren().remove(m.line);
                getPlotChildren ().remove (m.label);
                //.getNode());
                //horizontalMarkers.remove (marker);
                //marker.setNode(null);
            horizontalMarkers.remove(m);
        }

        /**
         * Overridden to layout the value markers.
         */
        @Override
        protected void layoutPlotChildren() {
            super.layoutPlotChildren();
            Bounds b = this.getBoundsInLocal ();
            for (Marker m : horizontalMarkers)
            {
                //double lower = ((ValueAxis) getXAxis()).getLowerBound();
                //X lowerX = getXAxis().toRealValue(lower);
                //double upper = ((ValueAxis) getXAxis()).getUpperBound();
                //X upperX = getXAxis().toRealValue(upper);
                //Line line = (Line) horizontalMarker.getNode();

                Line line = m.line;
                line.setStartX(b.getMinX ());//getXAxis().getDisplayPosition(lowerX));
                line.setEndX(b.getMaxX ());//getXAxis().getDisplayPosition(upperX));
                line.setStartY(getYAxis().getDisplayPosition(m.yValue));
                line.setEndY(line.getStartY());

                Node label = m.label;
                Bounds tb = label.getBoundsInLocal ();
                label.autosize ();
                label.relocate (tb.getMinX (),
                                line.getStartY () - tb.getHeight ());

            }
        }

    public Marker createMarker (Node    label,
                                String  styleClassName,
                                Number  yValue)
    {

        return new Marker (label,
                           styleClassName,
                           yValue);

    }

    private class Marker
    {

        public Line line = null;
        public Node label = null;
        public Number yValue = null;

        public Marker (Node   label,
                       String styleClassName,
                       Number yValue)
        {

            this.label = label;
            this.label.getStyleClass ().addAll ("marker", "label");

            this.line = new Line ();
            this.line.getStyleClass ().addAll ("marker", "line");

            if (styleClassName != null)
            {

                this.line.getStyleClass ().add (styleClassName);

            }

            this.yValue = yValue;

        }

    }

}
