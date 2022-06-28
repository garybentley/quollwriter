package com.quollwriter.ui.fx.sidebars;

import java.util.*;

import javafx.scene.*;
import javafx.css.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.components.*;

public abstract class BaseSideBar<E extends AbstractViewer & PanelViewer> extends SideBarContent<E>
{

    private HBox toolbarBox = null;
    private Pane contentWrapper = null;
    private Map<Panel, QuollToolBar> toolbars = null;

    public BaseSideBar (E viewer)
    {

        super (viewer);

        this.toolbars = new HashMap<> ();
        this.contentWrapper = new VBox ();
        VBox.setVgrow (this.contentWrapper,
                       Priority.ALWAYS);
        this.toolbarBox = new HBox ();
        this.toolbarBox.getStyleClass ().add (StyleClassNames.TOOLBAR);
        this.toolbarBox.managedProperty ().bind (this.toolbarBox.visibleProperty ());

        this.getChildren ().add (this.contentWrapper);

        this.viewer.addEventHandler (Panel.PanelEvent.CLOSE_EVENT,
                                     ev ->
        {

            this.toolbars.remove (ev.getPanel ());
            this.updateToolbar ();

        });

        this.addChangeListener (this.viewer.currentPanelProperty (),
                                (pr, oldv, newv) ->
        {

            this.updateToolbar ();

        });

    }

    public void setContent (Node n)
    {

        this.contentWrapper.getChildren ().clear ();
        this.contentWrapper.getChildren ().addAll (n);

    }

    @Override
    public void init (State s)
    {

        super.init (s);

        // Add listener to toolbar location position.
        this.addChangeListener (UserProperties.toolbarLocationProperty (),
                                (pr, oldv, newv) ->
        {

            this.toolbarBox.pseudoClassStateChanged (PseudoClass.getPseudoClass (oldv), false);

            this.updateToolbar ();

        });

        this.updateToolbar ();

    }

    private void updateToolbar ()
    {
if (true){return;}
        this.toolbarBox.setVisible (false);
        this.toolbarBox.getChildren ().clear ();

        Panel p = this.viewer.getCurrentPanel ();

        if (p == null)
        {

            return;

        }

        this.toolbarBox.pseudoClassStateChanged (PseudoClass.getPseudoClass (Constants.TOP), false);
        this.toolbarBox.pseudoClassStateChanged (PseudoClass.getPseudoClass (Constants.BOTTOM), false);

        String loc = UserProperties.toolbarLocationProperty ().getValue ();

        if (loc == null)
        {

            loc = Constants.BOTTOM;

        }

        this.contentWrapper.getChildren ().remove (this.toolbarBox);

        if (loc.equals (Constants.TOP))
        {

            this.contentWrapper.getChildren ().add (0,
                                                    this.toolbarBox);

        } else {

            this.contentWrapper.getChildren ().add (this.toolbarBox);

        }

        this.toolbarBox.pseudoClassStateChanged (PseudoClass.getPseudoClass (loc), true);

        // Update the toolbar.
        if (p.getContent () instanceof ToolBarSupported)
        {

            QuollToolBar tb = this.toolbars.get (p);

            if (tb == null)
            {

                ToolBarSupported tbs = (ToolBarSupported) p.getContent ();

                //tb.getItems ().addAll (tbs.getToolBarItems ());

                tb = QuollToolBar.builder ()
                    .styleClassName (StyleClassNames.TOOLBAR)
                    .controls (tbs.getToolBarItems ())
                    //.configurable (tbs.isToolBarConfigurable ())
                    .inViewer (this.viewer)
                    .build ();
                //tb = new ToolBar ();
                //tb.getStyleClass ().add (StyleClassNames.TOOLBAR);

                HBox.setHgrow (tb,
                               Priority.ALWAYS);

                this.toolbars.put (p,
                                   tb);

            }

            this.toolbarBox.getChildren ().add (tb);

            this.toolbarBox.setVisible (true);

        }

        this.requestLayout ();

    }

}
