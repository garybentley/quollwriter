/*
 * From: https://stackoverflow.com/questions/25838965/size-javafx-webview-to-the-minimum-size-needed-by-the-document-body
 * and: http://tech.chitgoks.com/2014/09/13/how-to-fit-webview-height-based-on-its-content-in-java-fx-2-2/
 */
package com.quollwriter.ui.fx.components;

import java.net.*;
import java.util.*;
import java.util.function.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.*;
import javafx.scene.input.*;
import netscape.javascript.*;
import org.w3c.dom.*;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

public class WebViewFitContent extends Region {

    final WebView webview = new WebView();
    final WebEngine webEngine = webview.getEngine();
    private double currHeight = 0;
    private Label label = new Label ();
    private String content = "";
    private String divId = UUID.randomUUID ().toString ();
    private BiConsumer<String, MouseEvent> onLinkClicked = null;
    private Function<String, String> formatter = null;

    private static ConcurrentLinkedQueue<WebViewFitContent> heightUpdates = null;
    private static ScheduledFuture adjust = null;

    static
    {

        WebViewFitContent.heightUpdates = new ConcurrentLinkedQueue ();

    }

    public WebViewFitContent (BiConsumer<String, MouseEvent> onLinkClicked)
    {

        if (WebViewFitContent.adjust == null)
        {

            WebViewFitContent.adjust = Environment.schedule (() ->
            {

                if (WebViewFitContent.heightUpdates.size () == 0)
                {

                    return;

                }

                UIUtils.runLater (() ->
                {

                    synchronized (WebViewFitContent.heightUpdates)
                    {

                        WebViewFitContent r = null;

                        while ((r = WebViewFitContent.heightUpdates.poll ()) != null)
                        {

                            try
                            {
                                r.doAdjustHeight ();
                                //r.run ();

                            } catch (Exception e) {

                                Environment.logError ("Unable to update height",
                                                      e);

                            }

                        }

                    }

                });

            },
            5,
            50);

        }

        this.onLinkClicked = onLinkClicked;

        this.webview.setPrefHeight (0);
        this.managedProperty ().bind (this.visibleProperty ());
        //this.setVisible (false);
        this.webview.setVisible (false);
        this.webview.setContextMenuEnabled (false);
        this.getStyleClass ().add ("qwebview");
        this.label.setVisible (false);
        this.label.fontProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setContent (this.content);

        });

        this.label.textFillProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setContent (this.content);

        });

        this.backgroundProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setContent (this.content);

        });

        this.widthProperty ().addListener ((pr, oldv, newv) ->
        {

            this.webview.setPrefWidth (newv.doubleValue ());
            this.adjustHeight ();

        });

        this.heightProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setContent (this.content);
            this.adjustHeight ();

        });

        this.parentProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setContent (this.content);
            //this.adjustHeight ();

        });

        final WebViewFitContent _this = this;
        this.sceneProperty ().addListener ((pr, oldv, newv) ->
        {

            _this.setContent (_this.content);
/*
            com.quollwriter.ui.fx.UIUtils.runLater (() ->
            {


            });
*/
        });

        //final WebViewFitContent _this = this;

        this.webEngine.getLoadWorker ().stateProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv == State.SUCCEEDED)
            {

                adjustHeight ();

                // Make the background transparent.
                com.sun.javafx.webkit.Accessor.getPageFor (webEngine).setBackgroundColor (0);

                Document doc = this.webEngine.getDocument ();
                NodeList lista = doc.getElementsByTagName ("a");

                for (int i = 0; i < lista.getLength (); i++)
                {

                    Element el = (Element) lista.item (i);

                    // An element in w3c isn't an event target... this is an offically supported API... dafuq.
                    ((EventTarget) lista.item (i)).addEventListener ("click", ev ->
                    {

                        if (this.onLinkClicked != null)
                        {

                            String href = el.getAttribute ("href");

                            Platform.runLater (() ->
                            {

                                org.w3c.dom.events.MouseEvent mev = (org.w3c.dom.events.MouseEvent) ev;

                                short but = mev.getButton ();

                                this.onLinkClicked.accept (href,
                                                           // Synthesize a mouse event
                                                           new MouseEvent (_this,
                                                                           _this,
                                                                           MouseEvent.MOUSE_CLICKED,
                                                                           mev.getClientX (),
                                                                           mev.getClientY (),
                                                                           mev.getScreenX (),
                                                                           mev.getScreenY (),
                                                                           but == 0 ? MouseButton.PRIMARY : but == 1 ? MouseButton.MIDDLE : MouseButton.SECONDARY,
                                                                           1,
                                                                           mev.getShiftKey (),
                                                                           mev.getCtrlKey (),
                                                                           mev.getAltKey (),
                                                                           mev.getMetaKey (),
                                                                           but == 0,
                                                                           but == 1,
                                                                           but == 2,
                                                                           false,
                                                                           but == 2, // Is popup trigger?
                                                                           true,
                                                                           null));

                            });

                        }

                        ev.preventDefault ();

                    }, false);

                }

            }

        });

        this.webview.getChildrenUnmodifiable ().addListener ((ListChangeListener<Node>) ev ->
        {

            this.webview.lookupAll (".scroll-bar").stream ()
                .forEach (n -> n.setVisible (false));

        });

        webview.setOnScroll (ev ->
        {

            this.getParent ().fireEvent (ev);
            ev.consume ();
        });

        this.getChildren ().add (this.webview);
        this.getChildren ().add (this.label);
    }

    public void setContent (String content)
    {

        if (content == null)
        {

            content = "";

        }

        this.content = content;

        StringBuilder b = new StringBuilder ();
        b.append ("<html><head>");

        b.append ("<style>");
        b.append ("html, body{padding: 0px; margin: 0px; offset-x: hidden; offset-y: hidden;}");
        b.append (String.format ("body{background-color: %1$s;}",
                                 this.getBackgroundAsCssString (this.getBackground ())));
        b.append (String.format ("body{font-size: %1$spx; font-family: %2$s; color: %3$s;}",
                                 this.label.getFont ().getSize () + "",
                                 this.label.getFont ().getFamily (),
                                 this.getPaintAsCssString (this.label.getTextFill ())));
        b.append (".b{font-weight: bold;}");
        b.append (".i{font-style: italic;}");
        b.append (".u{text-decoration: underline;}");
        b.append ("a{text-decoration:none;}");
        b.append ("a:hover{text-decoration:underline;}");
        b.append ("ul{margin: 0;padding: 0.5em; padding-left: 2em;}");
        b.append ("ul.errors{color:red;}");
        b.append ("img.icon{display: inline-block; margin-right: 0.25em; vertical-align: middle}");
        b.append ("</style>");

        b.append ("<script>");
        b.append ("function noScroll(){window.scrollTo(0,0);}window.addEventListener('scroll', noScroll);");
        b.append ("</script>");

        b.append ("</head");

        String c = StringUtils.replaceString (this.content,
                                              String.valueOf ('\n'),
                                              "<br />");

        if (this.formatter != null)
        {

            c = this.formatter.apply (c);

        }

        b.append (String.format ("<body><div id='%1$s'>",
                                 divId));
        b.append (c);

        b.append ("</div></body></html>");

        this.webview.setVisible (false);

        webEngine.loadContent (b.toString ());

    }

    public void setFormatter (Function<String, String> f)
    {

        this.formatter = f;

    }

    @Override
    protected double computeMinHeight (double width)
    {

        return this.currHeight;

    }

    @Override
    protected double computePrefHeight (double width)
    {

        return this.currHeight;

    }

    @Override
    protected void layoutChildren()
    {

        this.layoutInArea (this.webview,0,0,this.getWidth (), this.getHeight (),0, HPos.CENTER, VPos.CENTER);
        this.layoutInArea (this.label, 0,0,0,0,0,HPos.CENTER,VPos.CENTER);
    }

    private void doAdjustHeight ()
    {

        try
        {

            // The document can sometimes be null, usually when the change is the result of a parent width change.
            if (this.webEngine.getDocument () == null)
            {

                return;

            }

            Object result = this.webEngine.executeScript (String.format ("document.getElementById('%1$s').scrollHeight",
                                                                         divId));

            if (result instanceof Integer)
            {

                Integer i = (Integer) result;
                double height = i.doubleValue ();

                // This check ensures that we don't get into a weird loop where the view is constantly resizing.
                if (height != this.currHeight)
                {

                    this.currHeight = height;
                    this.webview.setPrefHeight (height);
                    this.webview.setVisible (true);
                    this.webview.requestLayout ();

                } else {

                    this.webview.setVisible (true);
                    this.webview.requestLayout ();

                }

            }

        } catch (Exception e) {

            // You should do something about this!
            e.printStackTrace ();
        }

    }

    private void adjustHeight ()
    {
/*
        if (this.adjust != null)
        {

            this.adjust.cancel (true);

        }
*/

        Runnable r = () ->
        {

            try
            {

                // The document can sometimes be null, usually when the change is the result of a parent width change.
                if (this.webEngine.getDocument () == null)
                {

                    return;

                }

                Object result = this.webEngine.executeScript (String.format ("document.getElementById('%1$s').scrollHeight",
                                                                             divId));

                if (result instanceof Integer)
                {

                    Integer i = (Integer) result;
                    double height = i.doubleValue ();

                    // This check ensures that we don't get into a weird loop where the view is constantly resizing.
                    if (height != this.currHeight)
                    {

                        this.currHeight = height;
                        this.webview.setPrefHeight (height);
                        this.webview.setVisible (true);
                        this.webview.requestLayout ();

                    } else {

                        this.webview.setVisible (true);
                        this.webview.requestLayout ();

                    }

                }

            } catch (Exception e) {

                // You should do something about this!
                e.printStackTrace ();
            }

        };

        if (!WebViewFitContent.heightUpdates.contains (this))
        {

            WebViewFitContent.heightUpdates.add (this);

        }

        if (true)
        {
            return;
        }

        this.adjust = com.quollwriter.Environment.schedule (() ->
        {

        Platform.runLater (() ->
        {

            try
            {

                // The document can sometimes be null, usually when the change is the result of a parent width change.
                if (this.webEngine.getDocument () == null)
                {

                    return;

                }

                Object result = this.webEngine.executeScript (String.format ("document.getElementById('%1$s').scrollHeight",
                                                                             divId));

                if (result instanceof Integer)
                {

                    Integer i = (Integer) result;
                    double height = i.doubleValue ();

                    // This check ensures that we don't get into a weird loop where the view is constantly resizing.
                    if (height != this.currHeight)
                    {

                        this.currHeight = height;
                        this.webview.setPrefHeight (height);
                        this.webview.setVisible (true);
                        this.webview.requestLayout ();

                    } else {

                        this.webview.setVisible (true);
                        this.webview.requestLayout ();

                    }

                }

            } catch (Exception e) {

                // You should do something about this!
                e.printStackTrace ();
            }

        });

        },
        10,
        -1);

    }

    private String getPaintAsCssString (Paint p)
    {

        if (p instanceof Color)
        {

            Color c = (Color) p;

            return String.format( "#%02X%02X%02X",
                        (int)( c.getRed () * 255 ),
                        (int)( c.getGreen () * 255 ),
                        (int)( c.getBlue () * 255 ) );

        }

        return "#000000";

    }

    private String getBackgroundAsCssString (Background bg)
    {

        if (bg != null)
        {

            List<BackgroundFill> fills = bg.getFills ();

            if (fills != null)
            {

                return this.getPaintAsCssString (fills.get (0).getFill ());

            }

        }

        return "transparent"; //#ffffff";

    }

}
