/*
 * From: https://stackoverflow.com/questions/25838965/size-javafx-webview-to-the-minimum-size-needed-by-the-document-body
 * and: http://tech.chitgoks.com/2014/09/13/how-to-fit-webview-height-based-on-its-content-in-java-fx-2-2/
 */
package com.quollwriter.ui.fx.components;

import java.nio.file.*;
import java.nio.charset.*;
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
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Hyperlink;
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

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

public class WebViewFitContent extends Region {

    final WebView webview = new WebView();
    final WebEngine webEngine = webview.getEngine();
    private double currHeight = 0;
    private Label label = new Label ();
    private Hyperlink hyperlink = new Hyperlink ();
    private String content = "";
    private String divId = UUID.randomUUID ().toString ();
    private BiConsumer<String, MouseEvent> onLinkClicked = null;
    private Function<String, String> formatter = null;

    private static ConcurrentLinkedQueue<WebViewFitContent> heightUpdates = null;
    private static ScheduledFuture adjust = null;
    private static String header = null;

    static
    {

        WebViewFitContent.heightUpdates = new ConcurrentLinkedQueue ();

        WebViewFitContent.adjust = Environment.schedule (() ->
        {

            if (WebViewFitContent.heightUpdates.size () == 0)
            {

                return;

            }
/*
            synchronized (WebViewFitContent.heightUpdates)
            {

                WebViewFitContent r = null;

                while ((r = WebViewFitContent.heightUpdates.poll ()) != null)
                {
WebViewFitContent _r = r;
System.out.println ("ADDING: " + r);
                    try
                    {
                        UIUtils.runLater (() -> _r.doAdjustHeight ());
                        //r.run ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to update height",
                                              e);

                    }

                }

            }
            */

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

    public WebViewFitContent (BiConsumer<String, MouseEvent> onLinkClicked)
    {

        if (WebViewFitContent.header == null)
        {

            try
            {

                WebViewFitContent.header = new String (Files.readAllBytes (Paths.get (Utils.getResourceUrl (Constants.WEBVIEW_HEADER_FILE).toURI ())),
                                                       StandardCharsets.UTF_8);

            } catch (Exception e) {

                Environment.logError ("Unable to load webview header",
                                      e);

                WebViewFitContent.header = "<html>";

            }

        }

        this.onLinkClicked = onLinkClicked;

        this.webview.setPrefHeight (0);
        this.webview.setMaxHeight (0);
        this.managedProperty ().bind (this.visibleProperty ());
        this.webview.setContextMenuEnabled (false);
        this.webview.setVisible (false);
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

        this.hyperlink.setVisible (false);
        this.hyperlink.fontProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setContent (this.content);

        });

        this.hyperlink.textFillProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setContent (this.content);

        });

        this.backgroundProperty ().addListener ((pr, oldv, newv) ->
        {

            //this.setContent (this.content);

        });

        this.visibleProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv)
            {

                this.adjustHeight ();

            }

        });

        this.widthProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv.doubleValue () == 0)
            {

                return;

            }

            this.webview.setPrefWidth (newv.doubleValue ());
            this.webview.setMaxWidth (newv.doubleValue ());
            this.adjustHeight ();
/*
            UIUtils.runLater (() ->
            {

                this.adjustHeight ();
            });
*/
        });

        this.heightProperty ().addListener ((pr, oldv, newv) ->
        {

            this.adjustHeight ();

        });

        this.parentProperty ().addListener ((pr, oldv, newv) ->
        {

            this.adjustHeight ();

        });

        final WebViewFitContent _this = this;
        this.sceneProperty ().addListener ((pr, oldv, newv) ->
        {

            this.adjustHeight ();

        });

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

        webview.setOnScrollStarted (ev ->
        {

            this.getParent ().fireEvent (ev);
            ev.consume ();

        });

        webview.setOnScrollFinished (ev ->
        {

            this.getParent ().fireEvent (ev);
            ev.consume ();

        });

        webview.setOnKeyPressed (ev ->
        {

            this.getParent ().fireEvent (ev);
            ev.consume ();
        });

        webview.setOnMousePressed (ev ->
        {

            this.getParent ().fireEvent (ev);
            ev.consume ();
        });

        webview.setOnMouseReleased (ev ->
        {

            this.getParent ().fireEvent (ev);
            ev.consume ();
        });

        this.getChildren ().add (this.webview);
        this.getChildren ().add (this.label);
        this.getChildren ().add (this.hyperlink);
    }

    public void setContent (String content)
    {

        if (content == null)
        {

            content = "";

        }

        this.content = content;

        StringBuilder b = new StringBuilder (WebViewFitContent.header);
        b.append (String.format ("body{background-color:%1$s;font-size:%2$spx;font-family:%3$s;color:%4$s;}",
                                 this.getBackgroundAsCssString (this.getBackground ()),
                                 this.label.getFont ().getSize () + "",
                                 this.label.getFont ().getFamily (),
                                 this.getPaintAsCssString (this.label.getTextFill ())));
        b.append (String.format ("a{color:%1$s;}",
                                 this.getPaintAsCssString (this.hyperlink.getTextFill ())));
        b.append ("li{list-style-position:outside;margin-left:1em;}");
        b.append ("ul{margin:0 0 0 0.5em;padding:0.5em;}");
        b.append (String.format ("more{color:%1$s;font-style:italic;cursor:hand;}",
                                 this.getPaintAsCssString (this.hyperlink.getTextFill ())));
        b.append ("more:hover{text-decoration:underline;}");
/*
        b.append (".b{font-weight: bold;}.i{font-style: italic;}");
        b.append (".u{text-decoration: underline;}");
        b.append ("a{text-decoration:none;font-weight: normal;}");
        b.append ("a:hover{text-decoration:underline;}");
        b.append ("ul{margin: 0;padding: 0.5em; padding-left: 2em;}");
        b.append ("ul.errors{color:red;}");
        b.append ("img.icon{display: inline-block; margin-right: 0.25em; vertical-align: middle}");
*/
        //b.append ("</style>");
/*
        b.append ("<script>");
        b.append ("function noScroll(){window.scrollTo(0,0);}window.addEventListener('scroll', noScroll);");
        //b.append ("document.addEventListener('DOMContentLoaded',function(e){window.___domready=true;});");
        b.append ("</script>");
*/
        b.append ("</style></head><body>");

        String c = Utils.replaceString (this.content,
                                              String.valueOf ('\n'),
                                              "<br />");

        if (this.formatter != null)
        {

            c = this.formatter.apply (c);

        }

        b.append (String.format ("<div id='%1$s'>",
                                 divId));
        b.append (c);

        b.append ("</div></body></html>");

        webEngine.loadContent (b.toString ());

    }

    public void setFormatter (Function<String, String> f)
    {

        this.formatter = f;

    }

    @Override
    protected double computeMaxHeight (double width)
    {

        return this.currHeight;

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

        if (!this.isVisible ())
        {

            return;

        }

        if (this.webview.getPrefWidth () == 0)
        {

            return;

        }

        try
        {

            // The document can sometimes be null, usually when the change is the result of a parent width change.
            if (this.webEngine.getDocument () == null)
            {

                return;

            }

            // Set the width of the element to match the width of the webview.
            // The initial width can be a lot smaller than it "should be".

            // ??? TODO Remove, not needed now we are setting our width in reaction to other events.

            this.webEngine.executeScript (String.format ("document.getElementById('%1$s').style.width='%2$spx'",
                                                         divId,
                                                         Environment.formatNumber (this.webview.getPrefWidth ())));


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
                    this.webview.setMaxHeight (height);
                    this.webview.setVisible (true);
                    this.webview.setMinHeight (height);

                    UIUtils.runLater (() ->
                    {

                        this.webview.requestLayout ();

                    });

                } else {

                    this.webview.setVisible (true);

                    UIUtils.runLater (() ->
                    {

                        this.webview.requestLayout ();

                    });

                }

            }

        } catch (Exception e) {

            // You should do something about this!
            e.printStackTrace ();
        }

    }

    private void adjustHeight ()
    {

        this.doAdjustHeight ();

        if (true)
        {
            return;
        }

        this.adjust = com.quollwriter.Environment.schedule (() ->
        {

        UIUtils.runLater (() ->
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
        1000,
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
