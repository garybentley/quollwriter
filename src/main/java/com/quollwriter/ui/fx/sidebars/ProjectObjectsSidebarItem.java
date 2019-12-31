package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.function.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.input.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.uistrings.UILanguageStringsManager;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

public abstract class ProjectObjectsSidebarItem<E extends AbstractProjectViewer> implements Stateful, IPropertyBinder
{

    protected E viewer = null;
    protected AccordionItem item = null;
    private IPropertyBinder binder = null;

    public ProjectObjectsSidebarItem (E               pv,
                                      IPropertyBinder binder)
    {

        this.viewer = pv;
        this.binder = binder;

    }

    @Override
    public IPropertyBinder getBinder ()
    {

        return this.binder;

    }

    public abstract Node getContent ();

    public abstract String getId ();

    public abstract StringProperty getTitle ();

    public abstract IntegerProperty getItemCount ();

    public abstract Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ();

    public abstract String getStyleClassName ();

    public abstract BooleanProperty showItemCountOnHeader ();

    @Override
    public State getState ()
    {

        if (this.item == null)
        {

            return new State ();

        }

        return this.item.getState ();

    }

    @Override
    public void init (State s)
    {

        this.getAccordionItem ().init (s);

    }

    public AccordionItem getAccordionItem ()
    {

        if (this.item == null)
        {

            String tt = "%1$s (%2$s)";

            StringProperty title = this.getTitle ();
            IntegerProperty itemCount = this.getItemCount ();

            SimpleStringProperty t = new SimpleStringProperty ();
            t.bind (Bindings.createStringBinding (() ->
            {

                return String.format (tt,
                                      title.getValue (),
                                      Environment.formatNumber (itemCount.getValue ()));

            },
            title,
            itemCount,
            UILanguageStringsManager.uilangProperty ()));

            this.item = AccordionItem.builder ()
                .title (t)
                .accordionId (this.getId ())
                .openContent (this.getContent ())
                .styleClassName (this.getStyleClassName ())
                .contextMenu (this.getHeaderContextMenuItemSupplier ())
                .build ();

        }

        return this.item;

    }

    public E getViewer ()
    {

        return this.viewer;

    }

/*
    public void updateTitle ()
    {

        String title = this.getTitle ();

        if (this.showItemCountOnHeader ())
        {

            title += String.format (" (%s)",
                                    Environment.formatNumber (this.getItemCount ()));

        }

        // Set the title on the header directly.
        this.header.setTitle (title);

    }

    public void updateItemCount (int c)
    {

        String title = this.getTitle ();

        if (this.showItemCountOnHeader ())
        {

            title += " (" + c + ")";

        }

        // Set the title on the header directly.
        this.header.setTitle (title);

    }

    public abstract boolean showItemCountOnHeader ();
*/
/*
    public abstract int getItemCount ();

    public abstract void initTree ();
*/
/*
    public abstract void fillTreePopupMenu (JPopupMenu menu,
                                            MouseEvent ev);
*/
    //public abstract TreeCellEditor getTreeCellEditor (E     pv);

    //public abstract int getViewObjectClickCount (Object d);

    //public abstract boolean isTreeEditable ();

/*
    private class PopupPreviewListener extends MouseEventHandler
    {

        private NamedObjectPreviewPopup popup = null;
        private NamedObject lastObject = null;
        private ProjectObjectsAccordionItem item = null;

        // This timer is used when the user presses a button
        private Timer showDelayTimer = null;

        public PopupPreviewListener (ProjectObjectsAccordionItem item)
        {

            this.item = item;

            this.popup = new NamedObjectPreviewPopup (this.item.viewer);

        }

        @Override
        public void handlePress (MouseEvent ev)
        {

            this.popup.hidePopup ();

        }

        @Override
        public void mouseMoved (MouseEvent ev)
        {

            // Should be an achievement for having such a long var name...
            if (!this.item.viewer.getProject ().getPropertyAsBoolean (Constants.SHOW_QUICK_OBJECT_PREVIEW_IN_PROJECT_SIDEBAR_PROPERTY_NAME))
            {

                return;

            }

            final PopupPreviewListener _this = this;

            // Edit the chapter.
            TreePath tp = this.item.tree.getPathForLocation (ev.getX (),
                                                             ev.getY ());

            if (tp == null)
            {

                return;

            }

            Object d = ((DefaultMutableTreeNode) tp.getLastPathComponent ()).getUserObject ();

            if (!(d instanceof NamedObject))
            {

                return;

            }

            if ((d instanceof TreeParentNode)
                ||
                (d instanceof Note)
               )
            {

                return;

            }

            if (d == this.lastObject)
            {

                return;

            }

            if (d != this.lastObject)
            {

                // Hide the popup.
                this.popup.hidePopup ();

            }

            this.lastObject = (NamedObject) d;

            Point po = this.item.viewer.convertPoint (this.item.tree,
                                                      new Point (ev.getX () + 10,
                                                                 this.item.tree.getPathBounds (tp).y + this.item.tree.getPathBounds (tp).height - 5));

            // Show the first line of the description.
            this.popup.show ((NamedObject) d,
                             1000,
                             250,
                             po,
                             new ActionAdapter ()
                             {

                                public void actionPerformed (ActionEvent ev)
                                {

                                    _this.lastObject = null;

                                }

                             });

        }

        @Override
        public void mouseExited (MouseEvent ev)
        {

            this.lastObject = null;

            this.popup.hidePopup ();

        }

    }
*/
}
