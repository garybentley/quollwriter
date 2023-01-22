package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.scene.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.css.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.data.PropertyBinder;
import com.quollwriter.ui.fx.viewers.*;

public class ViewerSplitPane extends Pane implements Stateful
{

    private VBox mainSidebar = null;
    private VBox mainSidebarContent = null;
    private HBox toolbarBox = null;
    private VBox content = null;
    private VBox otherSidebar = null;
    private Region div1 = null;
    private Region div2 = null;
    private IPropertyBinder binder = null;
    private AbstractViewer viewer = null;
    private String layout = null;
    private SideBar mainSB = null;
    private SideBar otherSB = null;
    //private Map<Panel, QuollToolBar> toolbars = null;

    public ViewerSplitPane (StringProperty layoutProp,
                            AbstractViewer viewer)
    {

        this.viewer = viewer;
        //this.toolbars = new HashMap<> ();
        this.binder = new PropertyBinder ();
        this.layout = layoutProp.getValue ();
        this.getStyleClass ().add (StyleClassNames.VIEWERCONTENT);
        this.div1 = new Pane ();
        this.div1.getStyleClass ().add (StyleClassNames.DIVIDER);
        this.div1.setId ("div1");

        this.div1.setOnMousePressed (ev ->
        {

            ev.consume();
            this.div1.getProperties ().put ("startx", ev.getSceneX ());
            double pw = this.mainSidebar.getPrefWidth ();

            if (pw < 0)
            {

                pw = this.mainSidebar.prefWidth (this.getHeight ());

            }

            this.div1.getProperties ().put ("startw", pw);

        });

        this.div1.setOnMouseDragged(ev -> {
            double delta = 0;
            delta = ev.getSceneX();
            delta -= ((Number) this.div1.getProperties ().get ("startx")).doubleValue ();
            double pw = ((Number) this.div1.getProperties ().get ("startw")).doubleValue ();

            this.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, true);

            double diff = 1;

            String l = this.layout;

            if (l.equals (Constants.LAYOUT_CH_PS))
            {

                diff = -1;

            }

            if (l.equals (Constants.LAYOUT_OS_CH_PS))
            {

                diff = -1;

            }

            if (l.equals (Constants.LAYOUT_CH_OS_PS))
            {

                diff = -1;

            }

            this.resizeMain (pw + (diff * delta));
            ev.consume();

        });

        this.div1.setOnMouseReleased (ev ->
        {
            this.div1.getProperties ().remove ("startx");
            this.div1.getProperties ().remove ("startw");
            this.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);
            this.requestLayout ();
        });

        this.div2 = new Pane ();
        this.div2.getStyleClass ().add (StyleClassNames.DIVIDER);
        this.div2.setId ("div2");

        this.div2.setOnMousePressed (ev ->
        {

            ev.consume ();
            this.div2.getProperties ().put ("startx", ev.getSceneX ());

            double pw = this.otherSidebar.getPrefWidth ();

            if (pw < 0)
            {

                pw = this.otherSidebar.prefWidth (this.getHeight ());

            }

            this.div2.getProperties ().put ("startw", pw);

        });

        this.div2.setOnMouseDragged(ev ->
        {

            double delta = 0;
            delta = ev.getSceneX();
            delta -= ((Number) this.div2.getProperties ().get ("startx")).doubleValue ();
            double pw = ((Number) this.div2.getProperties ().get ("startw")).doubleValue ();
            this.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, true);
            //this.resizeOther (pw - delta);

            String l = this.layout;

            double diff = 1;

            if (l.equals (Constants.LAYOUT_PS_CH_OS))
            {

                diff = -1;

            }

            if (l.equals (Constants.LAYOUT_PS_OS_CH))
            {

                diff = 1;

            }

            if (l.equals (Constants.LAYOUT_CH_OS_PS))
            {

                diff = -1;

            }

            this.resizeOther (pw + (diff * delta));

            ev.consume();

        });

        this.div2.setOnMouseReleased (ev ->
        {
            this.div2.getProperties ().remove ("startx");
            this.div2.getProperties ().remove ("startw");
            this.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, false);
            this.requestLayout ();
        });

        this.mainSidebar = new VBox ();
        this.mainSidebarContent = new VBox ();
        VBox.setVgrow (this.mainSidebar,
                       Priority.ALWAYS);
        VBox.setVgrow (this.mainSidebarContent,
                       Priority.ALWAYS);
        this.toolbarBox = new HBox ();
        this.toolbarBox.getStyleClass ().add (StyleClassNames.TOOLBAR);
        this.toolbarBox.managedProperty ().bind (this.toolbarBox.visibleProperty ());
        UIUtils.addStyleSheet (this.toolbarBox,
                               Constants.SIDEBAR_STYLESHEET_TYPE,
                               "projecttoolbar");
        VBox.setVgrow (this.toolbarBox,
                       Priority.NEVER);
        this.mainSidebar.getChildren ().addAll (this.mainSidebarContent, this.toolbarBox);
        this.otherSidebar = new VBox ();
        this.content = new VBox ();

        this.getChildren ().addAll (this.div1, this.div2, this.mainSidebar, this.content, this.otherSidebar);

        this.viewer.addEventHandler (Viewer.ViewerEvent.FULL_SCREEN_EXITED_EVENT,
                                     ev ->
        {

            this.updateToolbar ();

        });

        this.binder.addChangeListener (layoutProp,
                                       (pr, oldv, newv) ->
        {

            this.layout = newv;
            this.updateLayout ();

        });

        this.viewer.addEventHandler (Panel.PanelEvent.CLOSE_EVENT,
                                     ev ->
        {

            this.updateToolbar ();

        });

        this.parentProperty ().addListener ((pr, oldv, newv) ->
        {

            this.prefHeightProperty ().unbind ();
            this.prefHeightProperty ().bind (((Region) newv).heightProperty ());
            this.prefWidthProperty ().unbind ();
            this.prefWidthProperty ().bind (((Region) newv).widthProperty ());

        });

    }

    @Override
    public void init (State s)
    {

        if (s == null)
        {

            return;

        }

        Double mw = s.getAsDouble ("main",
                                   null);

        if (mw != null)
        {

            this.mainSidebar.setPrefWidth (mw.doubleValue ());

        }

        Double ow = s.getAsDouble ("other",
                                   null);

        if (ow != null)
        {

            this.otherSidebar.setPrefWidth (ow.doubleValue ());

        }

        // Add listener to toolbar location position.
        this.binder.addChangeListener (UserProperties.toolbarLocationProperty (),
                                       (pr, oldv, newv) ->
        {

            this.toolbarBox.pseudoClassStateChanged (PseudoClass.getPseudoClass (oldv), false);

            this.updateToolbar ();

        });

        if (this.viewer instanceof PanelViewer)
        {

            this.binder.addChangeListener (((PanelViewer) this.viewer).currentPanelProperty (),
                                           (pr, oldv, newv) ->
            {

                this.updateToolbar ();

            });

        }

        this.updateToolbar ();

    }

    private void updateToolbar ()
    {

        this.toolbarBox.setVisible (false);
        this.toolbarBox.getChildren ().clear ();

        if (!(this.viewer instanceof PanelViewer))
        {

            return;

        }

        if (this.viewer.isInFullScreenMode ())
        {

            return;

        }

        PanelViewer pv = (PanelViewer) this.viewer;

        Panel p = pv.getCurrentPanel ();

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

        this.mainSidebar.getChildren ().remove (this.toolbarBox);

        if (loc.equals (Constants.TOP))
        {

            this.mainSidebar.getChildren ().add (0,
                                                 this.toolbarBox);

        } else {

            this.mainSidebar.getChildren ().add (this.toolbarBox);

        }

        this.toolbarBox.pseudoClassStateChanged (PseudoClass.getPseudoClass (loc), true);

        // Update the toolbar.
        if (p.getContent () instanceof ToolBarSupported)
        {

            QuollToolBar tb = ((ToolBarSupported) p.getContent ()).getToolBar ();

            if (tb != null)
            {

                if (tb.getParent () != null)
                {

                    ((Pane) tb.getParent ()).getChildren ().remove (tb);

                }

                HBox.setHgrow (tb,
                               Priority.ALWAYS);
                this.toolbarBox.getChildren ().add (tb);

                this.toolbarBox.setVisible (true);

            }


        }

        this.requestLayout ();

    }

    @Override
    public State getState ()
    {

        State s = new State ();
        s.set ("main",
               this.mainSidebar.getWidth ());
        s.set ("other",
               this.otherSidebar.getWidth ());

        return s;

    }

    public void setDividerPosition (double v)
    {

        this.mainSidebar.setPrefWidth (v);

    }

/*
    public void showSideBar (SideBar sb)
    {

        String l = this.layout;

        if (l.equals (Constants.LAYOUT_PS_CH))
        {

            if (sb == null)
            {

                sb = this.viewer.getMainSideBar ();

            }

            this.otherSidebar.getChildren ().clear ();
            this.mainSidebar.getChildren ().clear ();
            this.mainSidebar.getChildren ().add (sb);

        }

        if (l.equals (Constants.LAYOUT_PS_CH_OS))
        {

            this.mainSidebar.getChildren ().clear ();
            this.mainSidebar.getChildren ().add (this.viewer.getMainSideBar ());
            this.otherSidebar.getChildren ().clear ();

            if (sb != null)
            {

                this.otherSidebar.getChildren ().add (sb);

            }

        }

        this.requestLayout ();

    }
*/
    public void updateLayout ()
    {

        this.setLayout (this.layout);

    }

    public Region getChildToRight (Node n)
    {

        String l = this.layout;

        if (n == this.mainSidebar)
        {

            if (l.equals (Constants.LAYOUT_CH_PS))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_PS_CH))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_PS_CH_OS))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_PS_OS_CH))
            {

                return this.otherSidebar;

            }

            if (l.equals (Constants.LAYOUT_OS_CH_PS))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_CH_OS_PS))
            {

                return null;

            }

            return null;

        }

        if (n == this.otherSidebar)
        {

            if (l.equals (Constants.LAYOUT_CH_PS))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_PS_CH))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_PS_CH_OS))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_PS_OS_CH))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_OS_CH_PS))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_CH_OS_PS))
            {

                return this.mainSidebar;

            }

            return null;

        }

        if (n == this.content)
        {

            if (l.equals (Constants.LAYOUT_PS_CH))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_PS_CH_OS))
            {

                return this.otherSidebar;

            }

            if (l.equals (Constants.LAYOUT_PS_OS_CH))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_OS_CH_PS))
            {

                return this.content;

            }

            return null;

        }

        if (n == this.div1)
        {

            if (l.equals (Constants.LAYOUT_CH_PS))
            {

                return this.mainSidebar;

            }

            if (l.equals (Constants.LAYOUT_PS_CH))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_PS_CH_OS))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_PS_OS_CH))
            {

                return this.otherSidebar;

            }

            if (l.equals (Constants.LAYOUT_OS_CH_PS))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_CH_OS_PS))
            {

                return this.otherSidebar;

            }

            return null;

        }

        if (n == this.div2)
        {

            if (l.equals (Constants.LAYOUT_CH_PS))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_PS_CH))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_PS_CH_OS))
            {

                return this.otherSidebar;

            }

            if (l.equals (Constants.LAYOUT_PS_OS_CH))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_OS_CH_PS))
            {

                return this.mainSidebar;

            }

            if (l.equals (Constants.LAYOUT_CH_OS_PS))
            {

                return this.mainSidebar;

            }

            return null;

        }

        return null;

    }

    public Region getChildToLeft (Node n)
    {

        String l = this.layout;

        if (n == this.mainSidebar)
        {

            if (l.equals (Constants.LAYOUT_CH_PS))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_PS_CH))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_PS_CH_OS))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_PS_OS_CH))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_OS_CH_PS))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_CH_OS_PS))
            {

                return this.otherSidebar;

            }

            return null;

        }

        if (n == this.otherSidebar)
        {

            if (l.equals (Constants.LAYOUT_CH_PS))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_PS_CH))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_PS_CH_OS))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_PS_OS_CH))
            {

                return this.mainSidebar;

            }

            if (l.equals (Constants.LAYOUT_OS_CH_PS))
            {

                return null;

            }

            if (l.equals (Constants.LAYOUT_CH_OS_PS))
            {

                return this.content;

            }

            return null;

        }

        if (n == this.content)
        {

            if (l.equals (Constants.LAYOUT_PS_CH))
            {

                return this.mainSidebar;

            }

            if (l.equals (Constants.LAYOUT_PS_CH_OS))
            {

                return this.mainSidebar;

            }

            if (l.equals (Constants.LAYOUT_PS_OS_CH))
            {

                return this.otherSidebar;

            }

            if (l.equals (Constants.LAYOUT_OS_CH_PS))
            {

                return this.otherSidebar;

            }

            return null;

        }

        if (n == this.div1)
        {

            if (l.equals (Constants.LAYOUT_CH_PS))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_PS_CH))
            {

                return this.mainSidebar;

            }

            if (l.equals (Constants.LAYOUT_PS_CH_OS))
            {

                return this.mainSidebar;

            }

            if (l.equals (Constants.LAYOUT_PS_OS_CH))
            {

                return this.mainSidebar;

            }

            if (l.equals (Constants.LAYOUT_OS_CH_PS))
            {

                return this.otherSidebar;

            }

            if (l.equals (Constants.LAYOUT_CH_OS_PS))
            {

                return this.content;

            }

            return null;

        }

        if (n == this.div2)
        {

            if (l.equals (Constants.LAYOUT_CH_PS))
            {

                return this.mainSidebar;

            }

            if (l.equals (Constants.LAYOUT_PS_CH))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_PS_CH_OS))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_PS_OS_CH))
            {

                return this.otherSidebar;

            }

            if (l.equals (Constants.LAYOUT_OS_CH_PS))
            {

                return this.content;

            }

            if (l.equals (Constants.LAYOUT_CH_OS_PS))
            {

                return this.otherSidebar;

            }

            return null;

        }

        return null;

    }

    private void setLayout (String l)
    {

        this.div1.setVisible (false);
        this.div2.setVisible (false);
        if ((l.equals (Constants.LAYOUT_CH_PS))
            ||
            (l.equals (Constants.LAYOUT_PS_CH))
           )
        {

            SideBar sb = this.viewer.getCurrentOtherSideBar ();

            if (sb == null)
            {

                sb = this.viewer.getMainSideBar ();

            }

            this.mainSidebarContent.getChildren ().clear ();
            this.otherSidebar.getChildren ().clear ();

            if (sb != null)
            {

                VBox.setVgrow (sb,
                               Priority.ALWAYS);

                sb.setVisible (true);
                this.div1.setVisible (true);
                this.mainSidebarContent.getChildren ().add (sb);

            }

        }

        if ((l.equals (Constants.LAYOUT_PS_CH_OS))
            ||
            (l.equals (Constants.LAYOUT_PS_OS_CH))
            ||
            (l.equals (Constants.LAYOUT_OS_CH_PS))
            ||
            (l.equals (Constants.LAYOUT_CH_OS_PS))
           )
        {

            SideBar sb = this.viewer.getCurrentOtherSideBar ();
            this.otherSidebar.getChildren ().clear ();

            if (sb != null)
            {

                VBox.setVgrow (sb,
                               Priority.ALWAYS);

                sb.setVisible (true);
                this.div2.setVisible (true);
                this.otherSidebar.getChildren ().add (sb);

            }

            SideBar mb = this.viewer.getMainSideBar ();

            if (mb != null)
            {

                VBox.setVgrow (mb,
                               Priority.ALWAYS);

                mb.setVisible (true);
                this.div1.setVisible (true);
                this.mainSidebarContent.getChildren ().clear ();
                this.mainSidebarContent.getChildren ().add (mb);

            }

        }

        if (l.equals (Constants.LAYOUT_PS_CH_OS))
        {

            this.div1.setVisible (true);
            this.div2.setVisible (this.otherSidebar.getChildren ().size () > 0);

        }

        if (l.equals (Constants.LAYOUT_PS_OS_CH))
        {

            this.div1.setVisible (true);
            this.div2.setVisible (this.otherSidebar.getChildren ().size () > 0);

        }

        if (l.equals (Constants.LAYOUT_CH_OS_PS))
        {

            this.div1.setVisible (this.otherSidebar.getChildren ().size () > 0);
            this.div2.setVisible (true);

        }

        if (l.equals (Constants.LAYOUT_OS_CH_PS))
        {

            this.div1.setVisible (this.otherSidebar.getChildren ().size () > 0);
            this.div2.setVisible (true);

        }

        this.mainSidebarContent.setVisible (this.mainSidebarContent.getChildren ().size () > 0);
        this.otherSidebar.setVisible (this.otherSidebar.getChildren ().size () > 0);
        this.div1.setVisible (this.mainSidebar.isVisible ());
        this.div2.setVisible (this.otherSidebar.isVisible ());

        UIUtils.runLater (() ->
        {

            this.requestLayout ();

        });

    }

    private void resizeMain (double newWidth)
    {

        double height = this.getHeight ();

        double mmw = this.mainSidebar.minWidth (height);
        double mw = this.mainSidebar.getWidth ();

        if (newWidth > mmw)
        {

            if (newWidth > mw)
            {

                // Getting bigger.
                double cmw = this.content.minWidth (height);

                double cw = this.content.getWidth ();

                double diff = newWidth - mw;

                if ((cw - diff) > cmw)
                {

                    this.mainSidebar.setPrefWidth (newWidth);

                } else {

                    // Content is at the min, check other.
                    if (this.otherSidebar.isVisible ())
                    {

                        double osmw = this.otherSidebar.minWidth (height);
                        double osw = this.otherSidebar.getWidth ();

                        if ((osw - diff) > osmw)
                        {

                            this.otherSidebar.setPrefWidth (osw - diff);

                        }

                    }

                }

            } else {

                // Getting smaller.
                this.mainSidebar.setPrefWidth (newWidth);

            }

        }

    }

    private void resizeOther (double newWidth)
    {

        double height = this.getHeight ();

        double omw = this.otherSidebar.minWidth (height);
        double ocw = this.otherSidebar.getWidth ();

        if (newWidth > omw)
        {

            if (newWidth > ocw)
            {

                // Getting bigger.
                double cmw = this.content.minWidth (height);

                double cw = this.content.getWidth ();

                double diff = newWidth - ocw;

                if ((cw - diff) > cmw)
                {

                    // Remove the size from the main...
                    this.otherSidebar.setPrefWidth (newWidth);

                } else {

                    if (this.mainSidebarContent.getChildren ().size () > 0)
                    {

                        // Content is at the min, check main.
                        double msmw = this.mainSidebarContent.minWidth (height);
                        double msw = this.mainSidebarContent.getWidth ();

                        if ((msw - diff) > msmw)
                        {

                            this.mainSidebar.setPrefWidth (msw - diff);

                        }

                    }

                }

            } else {

                // Getting smaller.
                this.otherSidebar.setPrefWidth (newWidth);

            }

        }

    }

    private void resize (Region r,
                         double newWidth,
                         double dir)
    {

        double height = this.getHeight ();

        double omw = r.minWidth (height);
        double ocw = r.getWidth ();

        if (newWidth > omw)
        {

            if (newWidth > ocw)
            {

                double diff = newWidth - ocw;

                if (dir < 0)
                {

                    // Get the next component to our left.
                    Region or = r;

                    while ((or = this.getChildToLeft (or)) != null)
                    {

                        if (or.getChildrenUnmodifiable ().size () > 0)
                        {

                            double ormw = or.minWidth (height);

                            double orcw = or.getWidth ();

                            // See if we can remove the diff from the component to our left.
                            if ((orcw - diff) > ormw)
                            {

                                or.setPrefWidth (orcw - diff);
                                r.setPrefWidth (newWidth);
                                return;

                            }

                        }

                    }

                } else {

                    Region or = r;

                    while ((or = this.getChildToRight (or)) != null)
                    {

                        if (or.getChildrenUnmodifiable ().size () > 0)
                        {

                            double ormw = or.minWidth (height);

                            double orcw = or.getWidth ();

                            // See if we can remove the diff from the component to our left.
                            if ((orcw - diff) > ormw)
                            {

                                or.setPrefWidth (orcw - diff);
                                r.setPrefWidth (newWidth);
                                return;

                            }

                        }

                    }

                }

            } else {

                // Getting smaller.
                r.setPrefWidth (newWidth);

            }

        }

    }

    public void setContent (Region content)
    {

        VBox.setVgrow (content,
                       Priority.ALWAYS);

        this.content.getChildren ().clear ();
        this.content.getChildren ().add (content);

    }

    @Override
    protected double computePrefHeight (double w)
    {

        return this.content.prefHeight (w) + this.content.getInsets ().getTop () + this.content.getInsets ().getBottom ();

    }

    @Override
    protected double computeMinHeight (double w)
    {

        return this.content.minHeight (w) + this.content.getInsets ().getTop () + this.content.getInsets ().getBottom ();

    }

    @Override
    protected double computeMinWidth (double h)
    {

        double w = 0;

        if (this.layout.equals (Constants.LAYOUT_PS_CH_OS))
        {

            SideBar main = this.viewer.getMainSideBar ();

            if (main != null)
            {

                w = w + this.mainSidebar.minWidth (h) + this.mainSidebar.getInsets ().getLeft () + this.mainSidebar.getInsets ().getRight () ;
                w = w + this.div1.minWidth (h) + this.div1.getInsets ().getLeft () + this.div1.getInsets ().getRight () ;

            }

            w = w + this.content.minWidth (h) + this.content.getInsets ().getLeft () + this.content.getInsets ().getRight ();

            String other = this.viewer.getCurrentOtherSideBarId ();

            if (other != null)
            {

                w = w + this.div2.minWidth (h) + this.div2.getInsets ().getLeft () + this.div2.getInsets ().getRight () ;
                w = w + this.otherSidebar.minWidth (h) + this.otherSidebar.getInsets ().getLeft () + this.otherSidebar.getInsets ().getRight ();

            }

        }

        if (this.layout.equals (Constants.LAYOUT_PS_CH))
        {

            SideBar main = this.viewer.getMainSideBar ();

            if (main != null)
            {

                w = w + this.mainSidebar.minWidth (h) + this.mainSidebar.getInsets ().getLeft () + this.mainSidebar.getInsets ().getRight () ;
                w = w + this.div1.minWidth (h) + this.div1.getInsets ().getLeft () + this.div1.getInsets ().getRight () ;

            }

            w = w + this.content.minWidth (h) + this.content.getInsets ().getLeft () + this.content.getInsets ().getRight ();

        }

        if (this.layout.equals (Constants.LAYOUT_CH_PS))
        {

            SideBar main = this.viewer.getMainSideBar ();

            if (main != null)
            {

                w = w + this.otherSidebar.minWidth (h) + this.otherSidebar.getInsets ().getLeft () + this.otherSidebar.getInsets ().getRight () ;
                w = w + this.div2.minWidth (h) + this.div2.getInsets ().getLeft () + this.div2.getInsets ().getRight () ;

            }

            w = w + this.content.minWidth (h) + this.content.getInsets ().getLeft () + this.content.getInsets ().getRight ();

        }

        return w;

    }

    @Override
    protected double computePrefWidth (double h)
    {

        double w = ((Region) this.getParent ()).getWidth ();

        if (w == 0)
        {

            return this.computeMinWidth (h);

        }

        return w;

    }

    @Override
    protected void layoutChildren ()
    {

        //super.layoutChildren ();

        //this.mainSidebar.setVisible (false);
        //this.otherSidebar.setVisible (false);
        //this.div1.setVisible (false);
        //this.div2.setVisible (false);
        Insets insets = getInsets();
        double width = getWidth();
        double height = getHeight();
        double cw = 0;
        double mw = 0;
        double x = insets.getLeft ();
        double y = insets.getTop ();
        height = height - insets.getTop () - insets.getBottom ();
        width = width - insets.getLeft () - insets.getRight ();

        double w = 0;
        double msw = 0;
        double osw = 0;
        double osmw = 0;
        double d1w = 0;
        double d2w = 0;
        double contentw = 0;

        if (this.mainSidebarContent.getChildren ().size () > 0)
        {

            double pw = this.mainSidebar.getPrefWidth ();

            if (pw > -1)
            {

                cw = pw;

            } else {

                cw = this.mainSidebar.prefWidth (height);

            }

            mw = this.mainSidebar.minWidth (height);

            if (cw < mw)
            {

                cw = mw;

            }

            msw = cw + this.mainSidebar.getInsets ().getLeft () + this.mainSidebar.getInsets ().getRight ();

            cw = this.div1.prefWidth (height);
            mw = this.div1.minWidth (height);

            if (cw < mw)
            {

                cw = mw;

            }

            d1w = cw + this.div1.getInsets ().getLeft () + this.div1.getInsets ().getRight ();

        }

        if (this.otherSidebar.getChildren ().size () > 0)
        {

            cw = this.div2.prefWidth (height);
            mw = this.div2.minWidth (height);

            if (cw < mw)
            {

                cw = mw;

            }

            d2w = cw + this.div2.getInsets ().getLeft () + this.div2.getInsets ().getRight ();

            double pw = this.otherSidebar.getPrefWidth ();

            if (pw > -1)
            {

                cw = pw;

            } else {

                cw = this.otherSidebar.prefWidth (height);

            }

            osmw = this.otherSidebar.minWidth (height);

            if (cw < osmw)
            {

                cw = osmw;

            }

            osw = cw + this.otherSidebar.getInsets ().getLeft () + this.otherSidebar.getInsets ().getRight ();;

        }

        contentw = width - d1w - d2w - msw - osw;

        double cm = this.content.minWidth (height);

        if (contentw < cm)
        {

            contentw = cm;

        }

        double nw = contentw + d1w + d2w + msw + osw;

        if (nw > width)
        {

            contentw -= (nw - width);

            if (contentw < cm)
            {

                contentw = cm;

            }

        }

        nw = contentw + d1w + d2w + msw + osw;

        if (nw > width)
        {

            osw -= (nw - width);

            if (osw < osmw)
            {

                osw = osmw;

            }

        }

        nw = contentw + d1w + d2w + msw + osw;

        if (nw > width)
        {

            msw -= (nw - width);

            double msmw = this.mainSidebar.minWidth (height);

            if (msw < msmw)
            {

                msw = msmw;

            }

        }

        if (this.layout.equals (Constants.LAYOUT_PS_CH))
        {

            if (this.mainSidebarContent.getChildren ().size () > 0)
            {

                this.mainSidebar.setPrefSize (msw, height);
                this.layoutInArea (this.mainSidebar,
                                   x,
                                   y,
                                   msw,
                                   height,
                                   0,
                                   this.mainSidebar.getInsets (),
                                   //false,
                                   //false,
                                   HPos.LEFT,
                                   VPos.TOP);
                                   //true);

                x = x + msw;

                this.layoutInArea (this.div1,
                                   x,
                                   y,
                                   d1w,
                                   height,
                                   0,
                                   this.div1.getInsets (),
                                   HPos.LEFT,
                                   VPos.TOP);

                x = x + d1w;

            }

            this.content.setPrefSize (contentw, height);
            this.layoutInArea (this.content,
                               x,
                               y,
                               contentw,
                               height,
                               0,
                               this.content.getInsets (),
                               //true,
                              // true,
                               HPos.LEFT,
                               VPos.TOP);
                               //true);

        }

        if (this.layout.equals (Constants.LAYOUT_CH_PS))
        {

            this.content.setPrefSize (contentw, height);
            this.layoutInArea (this.content,
                               x,
                               y,
                               contentw,
                               height,
                               0,
                               this.content.getInsets (),
                               //true,
                              // true,
                               HPos.LEFT,
                               VPos.TOP);
                               //true);

            x = x + contentw;

            if (this.mainSidebarContent.getChildren ().size () > 0)
            {

                this.layoutInArea (this.div1,
                                   x,
                                   y,
                                   d1w,
                                   height,
                                   0,
                                   this.div1.getInsets (),
                                   HPos.LEFT,
                                   VPos.TOP);

                x = x + d1w;

                this.mainSidebar.setPrefSize (msw, height);
                this.layoutInArea (this.mainSidebar,
                                   x,
                                   y,
                                   msw,
                                   height,
                                   0,
                                   this.mainSidebar.getInsets (),
                                   //false,
                                   //false,
                                   HPos.LEFT,
                                   VPos.TOP);
                                   //true);

                x = x + msw;

            }

        }

        if (this.layout.equals (Constants.LAYOUT_PS_OS_CH))
        {

            if (this.mainSidebarContent.getChildren ().size () > 0)
            {

                this.mainSidebar.setPrefSize (msw, height);
                this.layoutInArea (this.mainSidebar,
                                   x,
                                   y,
                                   msw,
                                   height,
                                   0,
                                   this.mainSidebar.getInsets (),
                                   //false,
                                   //false,
                                   HPos.LEFT,
                                   VPos.TOP);
                                   //true);

                x = x + msw;

                this.layoutInArea (this.div1,
                                   x,
                                   y,
                                   d1w,
                                   height,
                                   0,
                                   this.div1.getInsets (),
                                   HPos.LEFT,
                                   VPos.TOP);

                x = x + d1w;

            }

            if (this.otherSidebar.getChildren ().size () > 0)
            {

                this.otherSidebar.setPrefSize (osw, height);
                //this.otherSidebar.setVisible (true);
                this.layoutInArea (this.otherSidebar,
                                   x,
                                   y,
                                   // Check for this filling the area?
                                   osw,
                                   height,
                                   0,
                                   this.otherSidebar.getInsets (),
                                   true,
                                   true,
                                   HPos.LEFT,
                                   VPos.TOP,
                                   true);

                x = x + osw;

                //this.div2.setVisible (true);
                this.layoutInArea (this.div2,
                                   x,
                                   y,
                                   // Check for this filling the area?
                                   d2w,
                                   height,
                                   0,
                                   this.div2.getInsets (),
                                   true,
                                   true,
                                   HPos.LEFT,
                                   VPos.TOP,
                                   true);

                x = x + d2w;

            }

            this.content.setPrefSize (contentw, height);
            this.layoutInArea (this.content,
                               x,
                               y,
                               contentw,
                               height,
                               0,
                               this.content.getInsets (),
                               //true,
                              // true,
                               HPos.LEFT,
                               VPos.TOP);
                               //true);

            x = x + contentw;

        }

        if (this.layout.equals (Constants.LAYOUT_CH_OS_PS))
        {

            this.content.setPrefSize (contentw, height);
            this.layoutInArea (this.content,
                               x,
                               y,
                               contentw,
                               height,
                               0,
                               this.content.getInsets (),
                               //true,
                              // true,
                               HPos.LEFT,
                               VPos.TOP);
                               //true);

            x = x + contentw;

            if (this.otherSidebar.getChildren ().size () > 0)
            {

                this.layoutInArea (this.div2,
                                   x,
                                   y,
                                   d2w,
                                   height,
                                   0,
                                   this.div2.getInsets (),
                                   HPos.LEFT,
                                   VPos.TOP);

                x = x + d2w;

                this.otherSidebar.setPrefSize (osw, height);
                //this.otherSidebar.setVisible (true);
                this.layoutInArea (this.otherSidebar,
                                   x,
                                   y,
                                   // Check for this filling the area?
                                   osw,
                                   height,
                                   0,
                                   this.otherSidebar.getInsets (),
                                   true,
                                   true,
                                   HPos.LEFT,
                                   VPos.TOP,
                                   true);

                x = x + osw;

            }

            if (this.mainSidebarContent.getChildren ().size () > 0)
            {

                //this.div2.setVisible (true);
                this.layoutInArea (this.div1,
                                   x,
                                   y,
                                   // Check for this filling the area?
                                   d1w,
                                   height,
                                   0,
                                   this.div1.getInsets (),
                                   true,
                                   true,
                                   HPos.LEFT,
                                   VPos.TOP,
                                   true);

                x = x + d1w;

                this.mainSidebar.setPrefSize (msw, height);
                this.layoutInArea (this.mainSidebar,
                                   x,
                                   y,
                                   msw,
                                   height,
                                   0,
                                   this.mainSidebar.getInsets (),
                                   //false,
                                   //false,
                                   HPos.LEFT,
                                   VPos.TOP);
                                   //true);

                return;
                //x = x + msw;

            }

        }

        if (this.layout.equals (Constants.LAYOUT_PS_CH_OS))
        {

            if (this.mainSidebarContent.getChildren ().size () > 0)
            {

                this.mainSidebar.setPrefSize (msw, height);
                this.layoutInArea (this.mainSidebar,
                                   x,
                                   y,
                                   msw,
                                   height,
                                   0,
                                   this.mainSidebar.getInsets (),
                                   //false,
                                   //false,
                                   HPos.LEFT,
                                   VPos.TOP);
                                   //true);

                x = x + msw;

                this.layoutInArea (this.div1,
                                   x,
                                   y,
                                   d1w,
                                   height,
                                   0,
                                   this.div1.getInsets (),
                                   HPos.LEFT,
                                   VPos.TOP);

                x = x + d1w;

            }

            this.content.setPrefSize (contentw, height);
            this.layoutInArea (this.content,
                               x,
                               y,
                               contentw,
                               height,
                               0,
                               this.content.getInsets (),
                               //true,
                              // true,
                               HPos.LEFT,
                               VPos.TOP);
                               //true);

            x = x + contentw;

            if (this.otherSidebar.getChildren ().size () > 0)
            {

                //this.div2.setVisible (true);
                this.layoutInArea (this.div2,
                                   x,
                                   y,
                                   // Check for this filling the area?
                                   d2w,
                                   height,
                                   0,
                                   this.div2.getInsets (),
                                   true,
                                   true,
                                   HPos.LEFT,
                                   VPos.TOP,
                                   true);

                x = x + d2w;

                this.otherSidebar.setPrefSize (osw, height);
                //this.otherSidebar.setVisible (true);
                this.layoutInArea (this.otherSidebar,
                                   x,
                                   y,
                                   // Check for this filling the area?
                                   osw,
                                   height,
                                   0,
                                   this.otherSidebar.getInsets (),
                                   true,
                                   true,
                                   HPos.LEFT,
                                   VPos.TOP,
                                   true);

            }

        }

        if (this.layout.equals (Constants.LAYOUT_OS_CH_PS))
        {

            if (this.otherSidebar.getChildren ().size () > 0)
            {

                this.otherSidebar.setPrefSize (osw, height);
                //this.otherSidebar.setVisible (true);
                this.layoutInArea (this.otherSidebar,
                                   x,
                                   y,
                                   // Check for this filling the area?
                                   osw,
                                   height,
                                   0,
                                   this.otherSidebar.getInsets (),
                                   true,
                                   true,
                                   HPos.LEFT,
                                   VPos.TOP,
                                   true);

                x = x + osw;

                this.layoutInArea (this.div2,
                                   x,
                                   y,
                                   d2w,
                                   height,
                                   0,
                                   this.div2.getInsets (),
                                   HPos.LEFT,
                                   VPos.TOP);

                x = x + d2w;

            }

            this.content.setPrefSize (contentw, height);
            this.layoutInArea (this.content,
                               x,
                               y,
                               contentw,
                               height,
                               0,
                               this.content.getInsets (),
                               //true,
                              // true,
                               HPos.LEFT,
                               VPos.TOP);
                               //true);

            x = x + contentw;

            if (this.mainSidebarContent.getChildren ().size () > 0)
            {

                this.layoutInArea (this.div1,
                                   x,
                                   y,
                                   // Check for this filling the area?
                                   d2w,
                                   height,
                                   0,
                                   this.div1.getInsets (),
                                   true,
                                   true,
                                   HPos.LEFT,
                                   VPos.TOP,
                                   true);

                x = x + d1w;

                this.mainSidebar.setPrefSize (msw, height);
                this.layoutInArea (this.mainSidebar,
                                   x,
                                   y,
                                   msw,
                                   height,
                                   0,
                                   this.mainSidebar.getInsets (),
                                   //false,
                                   //false,
                                   HPos.LEFT,
                                   VPos.TOP);
                                   //true);

            }

        }

        UIUtils.forceRunLater (() ->
        {

            // This causes performance issues...
            // Use with care.
            //this.div1.toFront ();
            //this.div2.toFront ();

        });

    }

    public void dispose ()
    {

        this.binder.dispose ();

    }

}
