package com.quollwriter.ui.fx.viewers;

import java.net.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.geometry.*;
import javafx.beans.property.*;
import javafx.css.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.sidebars.*;

public class CSSViewer extends AbstractViewer
{

    private TabPane tabs = null;
    private WindowedContent windowedContent = null;
    private Map<Node, Panel> panels = new HashMap<> ();
    private ObjectProperty<Panel> currentPanelProp = null;
    private Stage viewer = null;
    private CSSSideBar sidebar = null;

    public CSSViewer (AbstractViewer forViewer)
    {

        this (forViewer.getViewer ());

        forViewer.getViewer ().addEventHandler (Viewer.ViewerEvent.CLOSE_EVENT,
                                                ev ->
        {

            this.close (null);

        });

    }

    public CSSViewer (Stage forViewer)
    {

        this.viewer = forViewer;

        this.currentPanelProp = new SimpleObjectProperty<> ();

        this.sidebar = new CSSSideBar (this);

        this.setMainSideBar (this.sidebar.getSideBar ());

        this.tabs = new TabPane ();
        this.tabs.setTabClosingPolicy (TabPane.TabClosingPolicy.ALL_TABS);
        this.tabs.setSide (UserProperties.tabsLocationProperty ().getValue ().equals (Constants.TOP) ? Side.TOP : Side.BOTTOM);
        this.tabs.setTabDragPolicy (TabPane.TabDragPolicy.REORDER);

        this.tabs.getSelectionModel ().selectedItemProperty ().addListener ((pr, oldi, newi) ->
        {

            if (newi != null)
            {

                Panel qp = (Panel) newi.getContent ();

                this.currentPanelProp.setValue (qp);

                qp.fireEvent (new Panel.PanelEvent (qp,
                                                    Panel.PanelEvent.SHOW_EVENT));

            } else {

                this.currentPanelProp.setValue (null);

            }

        });

        UIUtils.doOnKeyReleased (this.tabs,
                                 KeyCode.F4,
                                 () ->
                                 {

                                     this.removeCurrentPanel (null);

                                 });

    }

    public Panel getCurrentPanel ()
    {

        return this.currentPanelProp.getValue ();

    }

    public ObjectProperty<Panel> currentPanelProperty ()
    {

        return this.currentPanelProp;

    }

    public Tab addPanel (CSSNodePanel qp)
                  throws GeneralException
    {

        if (this.panels.containsKey (qp.getNode ()))
        {

            throw new GeneralException ("Already have a panel for node: " + qp.getNode ());

        }

        Panel p = qp.getPanel ();

        Tab tab = new Tab ();
        tab.textProperty ().bind (p.titleProperty ());
        tab.getStyleClass ().add (p.getStyleClassName ());
        tab.setContent (p);

        IconBox h = IconBox.builder ()
            .iconName (p.getStyleClassName ())
            .build ();

        tab.setGraphic (h);
/*
        if (this.getViewer ().isShowing ())
        {

            qp.getContent ().init (state);

        } else {
            final State _state = state;

            this.getViewer ().showingProperty ().addListener ((pr, oldv, newv) ->
            {

                if (newv)
                {

                    UIUtils.runLater (() ->
                    {

                        try
                        {

                            qp.getContent ().init (_state);

                        } catch (Exception e) {

                            Environment.logError ("Unable to init panel: " +
                                                  qp +
                                                  " with state: " +
                                                  _state,
                                                  e);

                        }

                    });

                }

            });

        }
*/
        this.tabs.getTabs ().add (0,
                                  tab);

        this.panels.put (qp.getNode (),
                         p);
/*
        tab.setOnCloseRequest (ev ->
        {

            ev.consume ();

            this.closePanel (qp.getNode (),
                             null);

        });
*/
        return tab;

    }

    public void closePanel (Node     n,
                            Runnable onClose)
    {

        CSSNodePanel c = (CSSNodePanel) this.panels.get (n).getContent ();

        if (c == null)
        {

            return;

        }

        this.closePanel (c,
                         onClose);

    }

    public void closePanel (Panel qp,
                            Runnable   onClose)
    {

        // Get the state.
        this.removePanel (qp,
                          onClose);

        Panel.PanelEvent pe = new Panel.PanelEvent (qp,
                                                    Panel.PanelEvent.CLOSE_EVENT);

        qp.fireEvent (pe);

        this.fireEvent (pe);

    }

    public void removeCurrentPanel (Runnable onRemove)
    {

        this.removePanel (this.currentPanelProp.getValue (),
                          onRemove);

    }

    private void removePanel (Node     node,
                              Runnable onRemove)
    {

        CSSNodePanel p = (CSSNodePanel) this.panels.get (node).getContent ();

        if (p == null)
        {

            return;

        }

        this.removePanel (p,
                          onRemove);

    }

    public void removePanel (Panel    p,
                             Runnable onRemove)
    {

		int tInd = this.getTabIndexForPanel (p);

		if (tInd > -1)
		{

			this.tabs.getTabs ().remove (tInd);

		}

        p.fireEvent (new Panel.PanelEvent (p,
                                           Panel.PanelEvent.CLOSE_EVENT));

        CSSNodePanel cp = (CSSNodePanel) p.getContent ();

		this.panels.remove (cp.getNode ());

        if (onRemove != null)
        {

            UIUtils.runLater (onRemove);

        }

	}

    @Override
    public Content getWindowedContent ()
    {

        if (this.windowedContent == null)
        {

            Supplier<Set<Node>> hcsupp = this.getTitleHeaderControlsSupplier ();

            Set<Node> headerCons = new LinkedHashSet<> ();

            if (hcsupp != null)
            {

                headerCons.addAll (hcsupp.get ());

            }

            this.windowedContent = new WindowedContent (this,
                                                        this.getStyleClassName (),
                                                        this.getStyleClassName (),
                                                        headerCons,
                                                        //new TextArea ());
                                                        this.tabs);

            this.windowedContent.setTitle (this.viewer.titleProperty ());


        }

        return this.windowedContent;

    }

    @Override
    public Content getFullScreenContent ()
    {

        return null;

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.CSS;

    }

    @Override
    public Supplier<Set<MenuItem>> getSettingsMenuSupplier ()
    {

        return () -> new LinkedHashSet<> ();

    }

    @Override
    public StringProperty titleProperty ()
    {

        return this.viewer.titleProperty ();

    }

    @Override
    public void showOptions (String sect)
                      throws GeneralException
    {

    }

    @Override
    public void deleteAllObjectsForType (UserConfigurableObjectType t)
                                  throws GeneralException
    {

        throw new UnsupportedOperationException ("Not supported.");

    }

    @Override
    public Supplier<Set<Node>> getTitleHeaderControlsSupplier ()
    {

        return () ->
        {

            Set<Node> controls = new LinkedHashSet<> ();

            return controls;

        };

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        s = new State ();
        s.set (Constants.WINDOW_WIDTH_PROPERTY_NAME,
               1200);
        s.set (Constants.WINDOW_HEIGHT_PROPERTY_NAME,
               800);

        super.init (s);

    }

    public void showNode (Node n)
                   throws GeneralException
    {

        Panel p = this.panels.get (n);

        if (p == null)
        {

            CSSNodePanel np = new CSSNodePanel (this,
                                                n);
            this.addPanel (np);

        }

        p = this.panels.get (n);

        for (Tab t : this.tabs.getTabs ())
        {

            Panel tp = (Panel) t.getContent ();

            if (tp.equals (p))
            {

                this.tabs.getSelectionModel ().select (t);

            }

        }

    }

    public void updateForNode (Node n)
                        throws GeneralException
    {

        this.sidebar.setNode (n);

        this.showNode (n);

        //n.setStyle (n.getStyle () + " -fx-background-color: yellow; -fx-border-color: blue; -fx-border-insets: 1px; -fx-border-width: 1px; -fx-border-style: dashed;");

    }

    protected int getTabIndexForPanel (Panel panel)
    {

        for (int i = 0; i < this.tabs.getTabs ().size (); i++)
        {

            Tab t = this.tabs.getTabs ().get (i);

            Node n = t.getContent ();

            if (n instanceof Panel)
            {

                Panel p = (Panel) n;

                if (p.equals (panel))
                {

                    return i;

                }

            }

        }

        return -1;

    }

    public static String getNodeLabelName (Node n)
    {

        String cn = n.getClass ().getName ();

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

        b.append (Arrays.asList (cn.split ("\\.")).stream ()
            .reduce ((f, s) -> s)
            .orElse (null));

        return b.toString ();

    }

    public Set<FindResultsBox> findText (String t)
    {

        return null;

    }

}
