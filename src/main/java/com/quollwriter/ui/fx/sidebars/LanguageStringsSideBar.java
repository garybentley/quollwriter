package com.quollwriter.ui.fx.sidebars;

import java.awt.image.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.collections.*;
import javafx.beans.value.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.css.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.geometry.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.uistrings.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class LanguageStringsSideBar extends BaseSideBar<AbstractLanguageStringsEditor>
{

    public static final String SIDEBAR_ID = "mainlanguagestrings";

    private Map<String, QuollTreeView> sectionTrees = new HashMap<> ();
    private Filter<Node> nodeFilter = null;
    private QuollHyperlink                forwardLabel = null;
    private ScrollPane scroll = null;

    public LanguageStringsSideBar (AbstractLanguageStringsEditor viewer,
                                   AbstractLanguageStrings       baseStrings)
    {

        super (viewer);

        VBox content = new VBox ();
        content.getStyleClass ().add (StyleClassNames.CONTENT);

        this.forwardLabel = QuollHyperlink.builder ()
            .styleClassName (StyleClassNames.FORWARD)
            .onAction (ev ->
            {

                try
                {

                    // TODO?
                    //this.onForwardLabelClicked ();

                } catch (Exception e) {

                    Environment.logError ("Unable to update view",
                                          e);

                    ComponentUtils.showErrorMessage (this.viewer,
                                                     new SimpleStringProperty ("Unable to update view."));

                }

            })
            .build ();
        this.forwardLabel.managedProperty ().bind (this.forwardLabel.visibleProperty ());
        this.forwardLabel.setVisible (false);

        content.getChildren ().add (this.forwardLabel);

        String defSection = "General";

        Map<String, Set<Node>> sections = baseStrings.getNodesInSections (defSection);

        for (Section sect : (Set<Section>) baseStrings.getSections ())
        {

            AccordionItem it = this.createSectionTree (sect.name,
                                                       sect.icon,
                                                       sections.get (sect.id));

            if (it == null)
            {

                continue;

            }

            content.getChildren ().add (it);

        }

        this.scroll = new ScrollPane (content);

        this.setContent (this.scroll);

        this.addChangeListener (viewer.currentPanelProperty (),
                                (pr, oldv, newv) ->
        {

            LanguageStringsIdsPanel p = (LanguageStringsIdsPanel) this.viewer.getCurrentPanel ().getContent ();

            Node n = p.getParentNode ();

            this.sectionTrees.values ().stream ()
                .forEach (t ->
                {

                    t.clearSelection ();

                    t.select (n);

                });

        });

    }

    public void showForwardLabel (StringProperty t)
    {

        this.forwardLabel.textProperty ().unbind ();
        this.forwardLabel.textProperty ().bind (t);
        this.forwardLabel.setVisible (true);

    }

    private AccordionItem createSectionTree (String    title,
                                             String    styleClassName,
                                             Set<Node> sections)
    {

        final QuollTreeView tree = this.createStringsTree (sections);

        if (tree == null)
        {

            return null;

        }

        AccordionItem acc = AccordionItem.builder ()
            .title (new SimpleStringProperty (title))
            .styleClassNames (Arrays.asList (StyleClassNames.SECTION, styleClassName))
            .headerIconClassName (styleClassName)
            .openContent (tree)
            .build ();

        this.sectionTrees.put (title,
                               tree);

        return acc;

    }

    private QuollTreeView createStringsTree (Set<Node> sections)
    {

        TreeItem<Node> root = new TreeItem<> (new Node ("_strings",
                                                        null));

        for (Node k : sections)
        {

            // Find all the top level nodes.
            Set<String> tlNodes = k.getTopLevelNodes ();

            Set<Value> vals = null;

            Node n = null;

            if ((tlNodes != null)
                &&
                (tlNodes.size () > 0)
               )
            {

                // Get the nodes, pass through the filter as well.
                vals = new LinkedHashSet<> ();

                for (String tln : tlNodes)
                {

                    Node x = k.getChild (BaseStrings.getIdParts (tln));

                    if (x != null)
                    {

                        if (this.nodeFilter != null)
                        {

                            if (this.nodeFilter.accept (x))
                            {

                                n = x;

                            }

                        } else {

                            n = x;

                        }

                    }

                    vals = n.getValues (this.nodeFilter);

                    if (vals.size () == 0)
                    {

                        continue;

                    }

                    TreeItem<Node> ti = new TreeItem<> (n);
                    ti.setValue (n);

                    root.getChildren ().add (ti);

                }

                continue;

            }

            vals = k.getValues (this.nodeFilter);

            if (vals.size () == 0)
            {

                continue;

            }

            TreeItem<Node> ti = new TreeItem<> (k);
            ti.setValue (k);

            root.getChildren ().add (ti);

        }

        if (root.getChildren ().size () == 0)
        {

            // Filtered out everything.
            return null;

        }

        Function<TreeItem<Node>, javafx.scene.Node> cellProvider = (treeItem) ->
        {

            Node n = (Node) treeItem.getValue ();

            String name = this.getStringTitle (n);

            StringProperty nameP = new SimpleStringProperty ();
            nameP.setValue (name);

            QuollLabel l = QuollLabel.builder ()
                .styleClassName (StyleClassNames.NODE)
                .label (nameP)
                .build ();

            l.setIconClassName (this.getStringStyle (n));

            this.getBinder ().<Node, Number>addMapChangeListener (this.viewer.userCountsProperty (),
                                                                  ev ->
            {

                Node on = ev.getKey ();

                if (on.equals (n))
                {

                    nameP.setValue (this.getStringTitle (n));
                    //l.getStyleClass ().removeAll (Arrays.asList (StyleClassNames.ERROR, StyleClassNames.NEXT, StyleClassNames.SAVE));
                    l.setIconClassName (this.getStringStyle (n));

                    //l.getStyleClass ().add (this.getStringStyle (n));

                }

            });

            this.getBinder ().<Node, Number>addMapChangeListener (this.viewer.errorCountsProperty (),
                                                                  ev ->
            {

                Node on = ev.getKey ();

                if (on.equals (n))
                {

                    nameP.setValue (this.getStringTitle (n));
                    l.getStyleClass ().removeAll (Arrays.asList (StyleClassNames.ERROR, StyleClassNames.NEXT, StyleClassNames.SAVE));
                    l.getStyleClass ().add (this.getStringStyle (n));

                }

            });

            l.setOnMousePressed (ev ->
            {

                try
                {

                    this.viewer.showIds (new Id (BaseStrings.toId (n.getId ())));

                } catch (Exception e) {

                    Environment.logError ("Unable to show ids for: " + n,
                                          e);

                }

            });

            return l;

        };


        QuollTreeView tree = new QuollTreeView ();
        tree.setShowRoot (false);
        tree.getStyleClass ().add (StyleClassNames.LANGUAGESTRINGS);
        tree.setCellProvider (cellProvider);
        tree.setRoot (root);

        return tree;

    }

    private String getStringStyle (Node n)
    {

        int c = this.viewer.getUserValueCount (n);
        int alls = 0;
        int errCount = this.viewer.getErrorCount (n);

        String styleClassName = null;

        Set<Value> vals = this.viewer.getValuesForNode (n);

        if (vals != null)
        {

            alls = vals.size ();

        }

        if (errCount > 0)
        {

            styleClassName = StyleClassNames.ERROR;

        } else {

            if (alls == c)
            {

                styleClassName = StyleClassNames.SAVE;

            } else {

                if (c > 0)
                {

                    styleClassName = StyleClassNames.NEXT;

                } else {

                    styleClassName = StyleClassNames.NODE;

                }

            }

        }

        return styleClassName;

    }

    private String getStringTitle (Node n)
    {

        int c = this.viewer.getUserValueCount (n);
        int alls = 0;
        int errCount = this.viewer.getErrorCount (n);

        Set<Value> vals = this.viewer.getValuesForNode (n);

        if (vals != null)
        {

            alls = vals.size ();

        }

        String title = (n.getTitle () != null ? n.getTitle () : n.getNodeId ());

        String name = null;
        String styleClassName = null;

        if (errCount > 0)
        {

            name = String.format ("%s (%s/%s) [%s errors]",
                                  title,
                                  Environment.formatNumber (c),
                                  Environment.formatNumber (alls),
                                  Environment.formatNumber (errCount));

            styleClassName = StyleClassNames.ERROR;

        } else {

            name = String.format ("%s (%s/%s)",
                                  title,
                                  Environment.formatNumber (c),
                                  Environment.formatNumber (alls));

            if (alls == c)
            {

                styleClassName = StyleClassNames.SAVE;

            } else {

                if (c > 0)
                {

                    styleClassName = StyleClassNames.NEXT;

                }

            }

        }

        return name;

    }

    @Override
    public void init (State s)
    {

        super.init (s);

        if (s == null)
        {

            return;

        }

        float scroll = s.getAsFloat ("scroll",
                                     0f);

        if (scroll > 0)
        {

            this.scroll.setVvalue (scroll);

        }

    }

    @Override
    public SideBar createSideBar ()
    {

        return SideBar.builder ()
            .title (new SimpleStringProperty ("Ids"))
            .activeTitle (new SimpleStringProperty ("Ids"))
            //.contextMenu ()?
            .styleClassName (StyleClassNames.LANGUAGESTRINGS)
            .styleSheet (StyleClassNames.LANGUAGESTRINGS)
            .withScrollPane (false)
            .canClose (false)
            //.headerControls ()?
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (SIDEBAR_ID)
            .build ();

    }

}
