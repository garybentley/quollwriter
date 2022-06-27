package com.quollwriter.ui.fx.viewers;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class WindowedContent extends AbstractViewer.Content<AbstractViewer>
{

    private Header header = null;
    private ViewerSplitPane parentPane = null;
    //private Region content = null;
    private VBox notifications = null;

    public WindowedContent (AbstractViewer viewer,
                            String         styleClassName,
                            String         headerIconClassName,
                            Set<Node>      headerCons,
                            Region         mainContent)
    {

        super (viewer);

        Set<Node> hc = new LinkedHashSet<> ();

        if (Environment.getQuollWriterVersion ().isBeta ())
        {

            hc.add (viewer.getTitleHeaderControl (AbstractViewer.HeaderControl.reportbug));

        }

        if (headerCons != null)
        {

            hc.addAll (headerCons);

        }

        QuollMenuButton context = viewer.createViewerMenuButton ();
        hc.add (context);

        QuollToolBar ctb = QuollToolBar.builder ()
            .controls (hc)
            .inViewer (viewer)
            .configurable (true)
            .build ();

        ctb.minWidthProperty ().bind (ctb.prefWidthProperty ());

/*
        // TODO Change or make work!
        QuollToolBar ctb = QuollToolBar.builder ()
            .controls (hc)
            //.visibleItems (hc)
            //.withViewer (this.viewer)
            .build ();
*/
        this.header = Header.builder ()
            //.controls (b.headerControlsSupplier.get ())
            .toolbar (ctb)
            .styleClassName (StyleClassNames.HEADER)
            .iconClassName (headerIconClassName)
            .build ();
        this.header.getStyleClass ().add (StyleClassNames.TITLE);
        VBox.setVgrow (this.header,
                       Priority.NEVER);
        this.notifications = new VBox ();

        this.notifications.getStyleClass ().add (StyleClassNames.NOTIFICATIONS);
        VBox.setVgrow (this.notifications,
                       Priority.NEVER);

        this.parentPane = new ViewerSplitPane (UserProperties.uiLayoutProperty (),
                                               this.viewer);
        //this.parentPane.getStyleClass ().add (StyleClassNames.CONTENT);
        VBox.setVgrow (this.parentPane,
                       Priority.ALWAYS);
        this.parentPane.setContent (mainContent);

        VBox b = new VBox ();
        VBox.setVgrow (b,
                       Priority.ALWAYS);

        UIUtils.addStyleSheet (b,
                               Constants.VIEWER_STYLESHEET_TYPE,
                               StyleClassNames.WINDOWED);
/*
        if (b.styleSheet != null)
        {

            this.getStylesheets ().add (b.styleSheet);

        }
*/
        b.getStyleClass ().add (StyleClassNames.WINDOWED);
        b.prefWidthProperty ().bind (this.widthProperty ());
        b.prefHeightProperty ().bind (this.heightProperty ());
/*
        TabPane tp = new TabPane ();
        Tab tab = new Tab ();
        VBox.setVgrow (tp,
                       Priority.ALWAYS);
        tab.setContent (new ScrollPane (AccordionItem.builder ()
            .openContent (new TextArea ())
            .title (new javafx.beans.property.SimpleStringProperty ("TEST"))
            .closedContent (new Label ("THIS IS A TEST"))
            .build ()));
        tp.getTabs ().add (tab);
//this.parentPane.setContent (tp);
*/
        this.getChildren ().add (b);
        b.getChildren ().addAll (this.header, this.notifications, this.parentPane);

    }

    public Header getHeader ()
    {

        return this.header;

    }

    @Override
    public void showSideBar (SideBar sb)
    {

        this.updateLayout ();

    }

    @Override
    public void removeAllNotifications ()
    {

        this.notifications.getChildren ().clear ();

    }

    @Override
    public void removeNotification (Notification n)
    {

        this.notifications.getChildren ().remove (n);

        if (this.notifications.getChildren ().size () == 0)
        {

            this.notifications.setVisible (false);
            this.requestLayout ();
        }

    }

    @Override
    public void addNotification (Notification n)
    {

        UIUtils.runLater (() ->
        {

            this.notifications.getChildren ().add (0,
                                                   n);

            this.notifications.setVisible (true);

        });

    }

    @Override
    public State getState ()
    {

        return this.parentPane.getState ();

    }

    @Override
    public void init (State s)
    {

        this.parentPane.init (s);

        if (s != null)
        {



        }

    }

    public void dispose ()
    {

        this.parentPane.dispose ();

    }

    @Override
    public void updateLayout ()
    {

        this.parentPane.updateLayout ();

    }
/*
    public void setContent (Region n)
    {

        this.content = n;
        this.parentPane.setContent (n);

    }
*/

    public StringProperty getTitle ()
    {

        return this.header.titleProperty ();

    }

    public void setTitle (StringProperty t)
    {

        this.header.titleProperty ().unbind ();
        this.header.titleProperty ().bind (t);

    }

}
