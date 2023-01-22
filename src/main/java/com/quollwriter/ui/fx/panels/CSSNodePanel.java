package com.quollwriter.ui.fx.panels;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.net.*;

import javafx.geometry.*;
import javafx.css.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.text.*;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Document;

import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class CSSNodePanel extends PanelContent<CSSViewer>
{

    public static final String PANEL_ID = "cssnode";

    private Node node = null;

    public CSSNodePanel (CSSViewer viewer,
                         Node      n)
    {

        super (viewer);

        this.node = n;
/*
        for (CssMetaData<? extends Node, ?> md : (CssMetaData<? extends Node, ?>) this.node.getCssMetaData ())
        {

            props.put (md.getProperty (),
                       md.getStyleableProperty (this.node));

        }
*/

        SplitPane sp = new SplitPane ();

        sp.getItems ().add (this.createCSSPanel ());
        sp.getItems ().add (this.createPropertiesPanel ());

        this.getChildren ().add (sp);

    }

    private Node createPropertiesPanel ()
    {

        Map<String, StyleableProperty<? extends Styleable>> props = new TreeMap<> ();

        this.node.getCssMetaData ().stream ()
            .forEach (md ->
            {

                CssMetaData cmd = (CssMetaData) md;

                props.put (cmd.getProperty (),
                           cmd.getStyleableProperty (this.node));

            });

        VBox b = new VBox ();
        b.getStyleClass ().add (StyleClassNames.PROPERTIES);

        Form.Builder f = Form.builder ()
            .layoutType (Form.LayoutType.column);

        f.item (new SimpleStringProperty ("Id"),
                new SimpleStringProperty (this.node.getId () + ""));
        f.item (new SimpleStringProperty ("Inline style"),
                new SimpleStringProperty (this.node.getStyle () + ""));
        f.item (new SimpleStringProperty ("Pseudo classes"),
                new SimpleStringProperty (this.node.getPseudoClassStates ().stream ()
                    .map (p -> ":" + p.getPseudoClassName ())
                    .collect (Collectors.joining (" ")).trim ()));
        f.item (new SimpleStringProperty ("Local bounds"),
                this.format (this.node.boundsInLocalProperty ().getValue ()));
        f.item (new SimpleStringProperty ("Bounds in parent"),
                this.format (this.node.boundsInParentProperty ().getValue ()));

        b.getChildren ().add (AccordionItem.builder ()
            .title (new SimpleStringProperty ("Node Properties"))
            .openContent (f.build ())
            .build ());

        Form.Builder ff = Form.builder ()
            .layoutType (Form.LayoutType.column);

        props.keySet ().stream ()
            .forEach (p ->
            {

                StyleableProperty sp = (StyleableProperty) props.get (p);

                if (sp.getValue () != null)
                {

                    Property prop = ((Property) sp);

                    QuollLabel l = QuollLabel.builder ()
                        .label (prop.getName ())
                        .tooltip (new SimpleStringProperty (p))
                        .build ();

                    ff.item (l,
                             this.format (prop.getValue ()));
                             //new SimpleStringProperty (prop.getValue ().toString ()));

                }

            });

        b.getChildren ().add (AccordionItem.builder ()
            .title (new SimpleStringProperty ("Styleable Properties"))
            .openContent (ff.build ())
            .build ());

        ScrollPane sp = new ScrollPane (b);

        VBox.setVgrow (sp,
                       Priority.ALWAYS);

        return sp;

    }

    private Node format (Object o)
    {

        if (o == null)
        {

            return new TextFlow (new Text (o + ""));

        }

        if (o instanceof Insets)
        {

            Insets i = (Insets) o;

            return new TextFlow (new Text (String.format ("t: %1$s, r: %2$s, b: %3$s, l: %4$s",
                                                          Environment.formatNumber (i.getTop ()),
                                                          Environment.formatNumber (i.getRight ()),
                                                          Environment.formatNumber (i.getBottom ()),
                                                          Environment.formatNumber (i.getLeft ()))));

        }

        if (o instanceof Font)
        {

            Font f = (Font) o;

            TextFlow tf = new TextFlow ();

            Text t = new Text ();
            t.setText (String.format ("%1$s, %2$s, %3$spt - ",
                                      f.getName (),
                                      f.getStyle (),
                                      Environment.formatNumber (f.getSize ())));
            Text et = new Text ();
            et.setText ("Example text");
            et.setStyle (String.format ("-fx-font-family: %1$s; -fx-font-style: %2$s; -fx-font-size: %3$s",
                                       f.getName (),
                                       f.getStyle (),
                                       Environment.formatNumber (f.getSize ())));

            return new TextFlow (t, et);

        }

        if (o instanceof BoundingBox)
        {

            BoundingBox bb = (BoundingBox) o;

            return new TextFlow (new Text (String.format ("x: %1$s - %4$s, y: %2$s - %5$s, z: %3$s - %6$s, width: %7$s, height: %8$s",
                                  Environment.formatNumber (bb.getMinX ()),
                                  Environment.formatNumber (bb.getMinY ()),
                                  Environment.formatNumber (bb.getMinZ ()),
                                  Environment.formatNumber (bb.getMaxX ()),
                                  Environment.formatNumber (bb.getMaxY ()),
                                  Environment.formatNumber (bb.getMaxZ ()),
                                  Environment.formatNumber (bb.getWidth ()),
                                  Environment.formatNumber (bb.getHeight ()))));

        }

        return new TextFlow (new Text (o.toString ()));

    }

    private Node createCSSPanel ()
    {

        VBox b = new VBox ();
        b.getStyleClass ().add (StyleClassNames.STYLESHEETS);

        b.getChildren ().add (Header.builder ()
            .title (new SimpleStringProperty ("Stylesheets"))
            .build ());

        Map<Stylesheet, List<Rule>> stylesheets = this.getStylesheets (this.node);

        for (Stylesheet ss : stylesheets.keySet ())
        {

            VBox sb = new VBox ();

            for (Rule r : stylesheets.get (ss))
            {

                sb.getChildren ().add (this.getSelectors (r,
                                                          this.node));

                for (Declaration d : r.getDeclarations ())
                {

                    TextFlow tf = new TextFlow ();
                    tf.getStyleClass ().add ("declaration");

                    Text tp = new Text (d.getProperty ());
                    tp.getStyleClass ().add ("name");

                    String pv = "";

                    try
                    {

                        Document doc = DocumentHelper.parseText (d.getParsedValue () + "");

                        Element root = doc.getRootElement ();

                        pv = this.getValue (root);

                    } catch (Exception e) {

                        Environment.logError ("Unable to parse value: " + d.getParsedValue (),
                                              e);

                    }

                    Text dv = new Text (": ");
                    dv.getStyleClass ().add ("divider");

                    Text tv = new Text (pv);
                    tv.getStyleClass ().add ("value");

                    Text cv = new Text (";");
                    cv.getStyleClass ().add ("close");

                    tf.getChildren ().addAll (tp, dv, tv, cv);

                    sb.getChildren ().add (tf);

                }

                QuollLabel l = QuollLabel.builder ()
                    .styleClassName ("selectorclose")
                    .label (new SimpleStringProperty ("}"))
                    .build ();

                sb.getChildren ().add (l);

            }

            try
            {

                URI su = new URI (ss.getUrl ());

                Path sp = Paths.get (su);

                AccordionItem it = AccordionItem.builder ()
                    .title (new SimpleStringProperty (sp.getFileName ().toString ()))
                    .styleClassName ("stylesheetfile")
                    .openContent (sb)
                    .build ();

                UIUtils.setTooltip (it.getHeader (),
                                    new SimpleStringProperty (sp.toString ()));

                it.managedProperty ().bind (it.visibleProperty ());

                b.getChildren ().add (it);

            } catch (Exception e) {

                Environment.logError ("Unable to display stylesheet: " + ss,
                                      e);

                continue;

            }

        }

        if (b.getChildren ().size () > 0)
        {

            b.getChildren ().get (b.getChildren ().size () -1).pseudoClassStateChanged (StyleClassNames.LAST_PSEUDO_CLASS, true);

        }

        return new ScrollPane (b);

    }

    private String getValue (Element el)
    {

        StringBuilder b = new StringBuilder ();

        if (el != null)
        {

            Element pvel = el.element ("value");

            if (pvel != null)
            {

                if (pvel.attribute ("layer") != null)
                {

                    Element lel = pvel.element ("layer");

                    if (b.length () > 0)
                    {

                        b.append (" ");

                    }

                    b.append (lel.elements ("Value").stream ()
                        .map (_el -> this.getValue (_el))
                        .collect (Collectors.joining (" ")));

                    return b.toString ();

                }

                if (pvel.attribute ("values") != null)
                {

                    if (b.length () > 0)
                    {

                        b.append (" ");

                    }

                    b.append (pvel.elements ("Value").stream ()
                        .map (_el -> this.getValue (_el))
                        .collect (Collectors.joining (" ")));

                    return b.toString ();

                }

                Element vvel = pvel.element ("Value");

                if (vvel != null)
                {

                    b.append (this.getValue (vvel));

                    return b.toString ();

                }

                if (b.length () > 0)
                {

                    b.append (" ");

                }

                b.append (pvel.getText ());

            }

        }

        return b.toString ().trim ();

    }

    private Node getSelectors (Rule r,
                               Node n)
    {

        TextFlow f = new TextFlow ();

        f.getStyleClass ().add ("selectors");

        for (Selector s : r.getSelectors ())
        {

            if (f.getChildren ().size () > 0)
            {

                f.getChildren ().add (new Text (", "));

            }

            String sel = s.toString ();

            if (sel.startsWith ("*."))
            {

                sel = sel.substring (1);

            }

            Text t = new Text (sel);
            t.getStyleClass ().add ("name");

            if (s.applies (n))
            {

                if (sel.indexOf (":") > -1)
                {

                    if (s.stateMatches (n,
                                        n.getPseudoClassStates ()))
                    {

                        t.getStyleClass ().add ("match");

                    }

                } else {

                    t.getStyleClass ().add ("match");

                }

            }

            f.getChildren ().add (t);

        }

        Text b = new Text (" {");
        b.getStyleClass ().addAll ("openbracket", "bracket");

        f.getChildren ().add (b);

        return f;

    }

    private Map<Stylesheet, List<Rule>> getStylesheets (Node n)
    {

        Map<Stylesheet, List<Rule>> ss = new LinkedHashMap<> ();

        if (n == null)
        {

            return ss;

        }

        this.getStylesheets (n,
                             n,
                             ss);

        List<String> ssurls = new ArrayList<> (n.getScene ().getStylesheets ());

        Collections.reverse (ssurls);

        for (String surl : ssurls)
        {

            try
            {

                Stylesheet s = this.getStylesheet (surl);

                List<Rule> mrules = this.getMatchingRules (n,
                                                           s);

                if (mrules.size () > 0)
                {

                    ss.put (s,
                            mrules);

                }

            } catch (Exception e) {

                Environment.logError ("Unable to get stylesheet: " + surl,
                                      e);

            }

        }

        return ss;

    }

    private void getStylesheets (Node                        n,
                                 Node                        node,
                                 Map<Stylesheet, List<Rule>> stylesheets)
    {

        if (n instanceof Parent)
        {

            this.getStylesheets ((Parent) n,
                                 node,
                                 stylesheets);

        } else {

            this.getStylesheets (n.getParent (),
                                 node,
                                 stylesheets);

        }

    }

    private Stylesheet getStylesheet (String url)
                               throws Exception
    {

        try
        {

            Stylesheet sheet = null;

            CssParser ccsp = new CssParser ();
            return ccsp.parse (new URL (url));

        } catch (Exception e) {

            throw new GeneralException ("Unable to convert: " + url + " to a stylesheet.",
                                        e);

        }

    }
/*
    private List<Rule> getStyles (Stylesheet sheet,
                                  Node   node)
    {

        List<Rule> mrules = new ArrayList<> ();

        List<Rule> rules = sheet.getRules ();

        for (Rule r : rules)
        {

            List<Selector> sels = r.getSelectors ();

            for (Selector sel : sels)
            {

                if (sel.applies (n))
                {

                    mrules.add (r);

                }

            }

        }

        return mrules;

    }
*/

    private List<Rule> getMatchingRules (Node       n,
                                         Stylesheet sheet)
    {

        List<Rule> mrules = new ArrayList<> ();

        List<Rule> rules = sheet.getRules ();

        for (Rule r : rules)
        {

            List<Selector> sels = r.getSelectors ();

            for (Selector sel : sels)
            {

                if (sel.applies (n))
                {

                    mrules.add (r);

                }

            }

        }

        return mrules;

    }

    private void getStylesheets (Parent                      p,
                                 Node                        node,
                                 Map<Stylesheet, List<Rule>> stylesheets)
    {

        if (p == null)
        {

            return;

        }

        List<String> surls = new ArrayList<> (p.getStylesheets ());

        Collections.reverse (surls);

        for (String s : surls)
        {

            try
            {

                Stylesheet sheet = this.getStylesheet (s);

                List<Rule> mrules = this.getMatchingRules (node,
                                                           sheet);

                if (mrules.size () > 0)
                {

                    stylesheets.put (sheet,
                                     mrules);

                }

            } catch (Exception e) {

                Environment.logError ("Unable to get stylesheet: " + s,
                                      e);

            }

        }

        p = p.getParent ();

        this.getStylesheets (p,
                             node,
                             stylesheets);

    }

    private void outputApplicableCSSRules (Parent n,
                                                  List<String> ss)
    {

        for (String s : ss)
        {

            CssParser ccsp = new CssParser ();
            Stylesheet sheet = null;

            try
            {

                sheet = ccsp.parse (new URL (s));

            } catch (Exception e) {

                new Exception ("Unable to convert to a stylesheet: " + s,
                               e).printStackTrace ();

                continue;

            }

            System.out.println ("STYLESHEET: " + s);

            List<Rule> rules = sheet.getRules ();

            for (Rule r : rules)
            {

                List<Selector> sels = r.getSelectors ();

                for (Selector sel : sels)
                {

                    if (sel.applies (n))
                    {

                        System.out.println ("RULE: " + r);
                        System.out.println ("RULE1: " + r.getOrigin ());
                        System.out.println ("RULE2: " + r.getDeclarations ());
                        System.out.println ("RULE3: " + r.getSelectors ());

                    }

                }

            }

        }

    }

    public void outputApplicableCSSRules (Parent n)
    {

        Parent p = n;

        while (p != null)
        {

            this.outputApplicableCSSRules (n,
                                              p.getStylesheets ());

            p = p.getParent ();

        }

        this.outputApplicableCSSRules (n,
                                          n.getScene ().getStylesheets ());

        //List<String> ss = n.getScene ().getStylesheets ();
/*
        if (n instanceof Skinnable)
        {

            n = ((Skinnable) n.getStyleableNode ()).getSkin ().getNode ();

        }
*/

        if (n.getParent () == null)
        {



        }

    }

    public Node getNode ()
    {

        return this.node;

    }

    @Override
    public Panel createPanel ()
    {

        Panel panel = Panel.builder ()
            .title (new SimpleStringProperty (CSSViewer.getNodeLabelName (this.node)))
            .content (this)
            .styleClassName (StyleClassNames.CSSNODE)
            .styleSheet (StyleClassNames.CSSNODE)
            .panelId (PANEL_ID)
            // TODO .headerControls ()
            .toolbar (() ->
            {

                return new LinkedHashSet<Node> ();

            })
            .build ();

        return panel;

    }

    private String getNameAndStyle (Node n)
    {

        StringBuilder b = new StringBuilder ();

        if ((n.getStyleClass () != null)
            &&
            (n.getStyleClass ().size () > 0)
           )
        {

            b.append (".");
            b.append (n.getStyleClass ().stream ().collect (Collectors.joining (".")));
            b.append (" - ");

        }

        b.append (n.getClass ().getName ());

        return b.toString ();

    }

    private class CSSNode extends VBox
    {

        private QuollLabel label = null;
        private Map<String, AccordionItem> styles = new HashMap<> ();
        private CSSNodePanel panel = null;

        public CSSNode (StringProperty label,
                        Node           node,
                        List<String>   styles,
                        CSSNodePanel   panel)
        {

            this.panel = panel;

            this.label = QuollLabel.builder ()
                .label (label)
                .styleClassName ("nodename")
                .build ();

            this.getChildren ().addAll (this.label);

            this.addStylesheets (styles,
                                 node);

        }

        public CSSNode (Scene        s,
                        Node         n,
                        CSSNodePanel panel)
        {

            this (new SimpleStringProperty ("Base Scene Stylesheets"),
                  node,
                  s.getStylesheets (),
                  panel);

        }

        public CSSNode (StringProperty label,
                        CSSNodePanel   panel)
        {

            this (label,
                  (Node) null,
                  null,
                  panel);

        }

        public CSSNode (StringProperty label,
                        Parent         p,
                        Node           node,
                        CSSNodePanel   panel)
        {

            this (label,
                  node,
                  p.getStylesheets (),
                  panel);

            this.label.setOnMouseClicked (ev ->
            {

                try
                {

                    this.panel.viewer.updateForNode (p);

                } catch (Exception e) {

                    Environment.logError ("Unable to display node: " + p,
                                          e);

                    ComponentUtils.showErrorMessage (new SimpleStringProperty ("Unable to display node."));

                }

            });

        }

        private void addStylesheets (List<String> ss,
                                     Node         n)
        {

            if (ss == null)
            {

                ss = new ArrayList<> ();

            }

            this.pseudoClassStateChanged (StyleClassNames.NOSTYLES_PSEUDO_CLASS, true);

            ss = new ArrayList<> (ss);
            Collections.reverse (ss);

            for (String s : ss)
            {



                CssParser ccsp = new CssParser ();
                Stylesheet sheet = null;

                try
                {

                    sheet = ccsp.parse (new URL (s));

                } catch (Exception e) {

                    this.getChildren ().add (QuollLabel.builder ()
                        .styleClassName (StyleClassNames.ERROR)
                        .label (new SimpleStringProperty ("Unable to convert: " + s + " to a stylesheet."))
                        .build ());

                    continue;

                }

                VBox sb = new VBox ();

                List<Rule> rules = sheet.getRules ();

                for (Rule r : rules)
                {

                    List<Selector> sels = r.getSelectors ();

                    for (Selector sel : sels)
                    {

                        if (sel.applies (n))
                        {

                            sb.getChildren ().add (QuollLabel.builder ()
                                .label (new SimpleStringProperty (r.toString ()))
                                .build ());
                            this.pseudoClassStateChanged (StyleClassNames.NOSTYLES_PSEUDO_CLASS, false);
                            this.pseudoClassStateChanged (StyleClassNames.HASSTYLES_PSEUDO_CLASS, true);

                        }

                    }

                }


                try
                {

                    URI su = new URI (s);

                    Path sp = Paths.get (su);
System.out.println ("FN: " + sp.getFileName ());
                    AccordionItem it = AccordionItem.builder ()
                        .title (new SimpleStringProperty (sp.getFileName ().toString ()))
                        .styleClassName ("stylesheetfile")
                        .openContent (sb)
                        .build ();

                    if (sb.getChildren ().size () > 0)
                    {

                        it.pseudoClassStateChanged (StyleClassNames.HASSTYLES_PSEUDO_CLASS, true);

                    } else {

                        it.pseudoClassStateChanged (StyleClassNames.NOSTYLES_PSEUDO_CLASS, true);

                    }

                    it.managedProperty ().bind (it.visibleProperty ());

                    this.styles.put (s,
                                     it);

                    this.getChildren ().add (it);

                } catch (Exception e) {

                    Environment.logError ("Unable to add stylesheet: " + s,
                                          e);

                    continue;

                }

            }

        }

    }

}
