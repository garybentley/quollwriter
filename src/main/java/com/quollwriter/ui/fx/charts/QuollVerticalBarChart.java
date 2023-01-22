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
public class QuollVerticalBarChart<Number, X> extends BarChart<Number, X>
{

    private ObservableList<Marker> markers;

    public QuollVerticalBarChart (Axis<Number> xAxis, Axis<X> yAxis)
    {

        super(xAxis, yAxis);
          // a list that notifies on change of the yValue property
          this.markers = FXCollections.observableArrayList();
          // listen to list changes and re-plot
          this.markers.addListener((InvalidationListener)observable -> layoutPlotChildren());
    }

    /**
         * Add horizontal value marker. The marker's Y value is used to plot a
         * horizontal line across the plot area, its X value is ignored.
         *
         * @param marker must not be null.
         */
        public void addValueMarker(Marker m)
        {
            Objects.requireNonNull(m, "the marker must not be null");
            if (this.markers.contains(m)) return;
            this.markers.add (m);
            getPlotChildren().add(m.line);
            getPlotChildren ().add (m.label);
            //horizontalMarkers.add(marker);
        }

        /**
         * Remove horizontal value marker.
         *
         * @param horizontalMarker must not be null
         */
        public void removeValueMarker(Marker m) {
            Objects.requireNonNull(m, "the marker must not be null");
                getPlotChildren().remove(m.line);
                getPlotChildren ().remove (m.label);
                //.getNode());
                //horizontalMarkers.remove (marker);
                //marker.setNode(null);
            this.markers.remove(m);
        }

        /**
         * Overridden to layout the value markers.
         */
        @Override
        protected void layoutPlotChildren() {
            super.layoutPlotChildren();
            Bounds b = this.getBoundsInLocal ();
            for (Marker m : this.markers)
            {

                Line line = m.line;
                line.setStartY(b.getMinY ());//getXAxis().getDisplayPosition(lowerX));
                line.setEndY(b.getMaxY ());//getXAxis().getDisplayPosition(upperX));
                line.setStartX(getXAxis().getDisplayPosition(m.value));
                line.setEndX(line.getStartX());

                Node label = m.label;
                Bounds tb = label.getBoundsInLocal ();
                label.autosize ();
                label.relocate (line.getStartX () + line.getBoundsInLocal ().getWidth () - 1,
                                line.getStartY ());

            }
        }

    public Marker createMarker (Node    label,
                                String  styleClassName,
                                Number  value)
    {

        return new Marker (label,
                           styleClassName,
                           value);

    }

    private class Marker
    {

        public Line line = null;
        public Node label = null;
        public Number value = null;

        public Marker (Node   label,
                       String styleClassName,
                       Number value)
        {

            this.label = label;
            this.label.getStyleClass ().addAll ("marker", "label");

            this.line = new Line ();
            this.line.getStyleClass ().addAll ("marker", "line");

            if (styleClassName != null)
            {

                this.line.getStyleClass ().add (styleClassName);

            }

            this.value = value;

        }

    }

}
