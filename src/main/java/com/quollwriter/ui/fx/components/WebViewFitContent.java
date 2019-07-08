/*
 * From: https://stackoverflow.com/questions/25838965/size-javafx-webview-to-the-minimum-size-needed-by-the-document-body
 * and: http://tech.chitgoks.com/2014/09/13/how-to-fit-webview-height-based-on-its-content-in-java-fx-2-2/
 */
package com.quollwriter.ui.fx.components;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.util.Set;

public final class WebViewFitContent extends Region {

    final WebView webview = new WebView();
    final WebEngine webEngine = webview.getEngine();

    public WebViewFitContent(String content) {
        webview.setPrefHeight(1);
/*
        widthProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                Double width = (Double)newValue;
                webview.setPrefWidth(width);
                adjustHeight();
            }
        });
*/
        webview.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> arg0, State oldState, State newState)         {
                if (newState == State.SUCCEEDED) {
                }
            }
        });

        webview.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Node> change) {
                Set<Node> scrolls = webview.lookupAll(".scroll-bar");
                for (Node scroll : scrolls) {
                    scroll.setVisible(false);
                }
            }
        });

        setContent(content);
        getChildren().add(webview);
    }

    public void setContent(final String content) {
        Platform.runLater (() ->
        {

            webEngine.loadContent (getHtml(content));
        });
/*
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                webEngine.loadContent(getHtml(content));
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        adjustHeight();
                    }
                });
            }
        });
*/
    }


    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(webview,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
    }

    private String getHtml(String content) {
        return "<html><head><style>html, body{padding: 0px; margin:0px;}</style></head><body>" +
                "<div id=\"mydiv\">" + content + "</div>" +
                "</body></html>";
    }

}
