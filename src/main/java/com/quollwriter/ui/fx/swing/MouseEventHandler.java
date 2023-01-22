package com.quollwriter.ui.fx.swing;

import java.awt.Component;
import java.awt.event.*;

import java.util.*;
import java.util.function.*;

import javax.swing.*;

import javafx.scene.*;
import javafx.scene.control.*;

import com.quollwriter.*;
//import com.quollwriter.ui.*;
import com.quollwriter.ui.fx.*;

public class MouseEventHandler extends MouseAdapter
{

    //public boolean redispatchToParent = false;

    private Node parentNode = null;

    public MouseEventHandler (Node parent)
    {

        this.parentNode = parent;

    }

    public void redispatch (MouseEvent e)
    {

    }
    /*
        if (this.redispatchToParent)
        {

            Component c = e.getComponent ();

            if (c.getParent () != null)
            {

                e = SwingUtilities.convertMouseEvent (c,
                                                      e,
                                                      c.getParent ());

                c.getParent ().dispatchEvent (e);

            }

        }

    }
*/
    public void performAction (ActionEvent ev)
    {

        // Blank.

    }

    public JButton createButton (String         icon,
                                 int            iconType,
                                 String         toolTipText,
                                 String actionCommand)
    {

        final MouseEventHandler _this = this;
/*
TODO
        JButton but = UIUtils.createButton (icon,
                                            iconType,
                                            toolTipText,
                                            new ActionListener ()
                                            {

                                                 public void actionPerformed (ActionEvent ev)
                                                 {

                                                     _this.performAction (ev);

                                                 }

                                            });

        but.setActionCommand (actionCommand);
*/
        return null; //but;

    }

    public JMenuItem createMenuItem (String label,
                                     String icon,
                                     String         actionCommand,
                                     KeyStroke      accel)
    {

        JMenuItem mi = this.createMenuItem (label,
                                            icon,
                                            actionCommand);

        mi.setAccelerator (accel);

        return mi;


    }

    public JMenuItem createMenuItem (String label,
                                     String icon,
                                     String actionCommand)
    {

        final MouseEventHandler _this = this;
/*
TODO
        JMenuItem mi = UIUtils.createMenuItem (label,
                                               icon,
                                               new ActionListener ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.performAction (ev);

                                                    }

                                               });

        mi.setActionCommand (actionCommand);
*/
        return null; //mi;

    }

    public JMenuItem createMenuItem (String label,
                                     String icon,
                                     String         actionCommand,
                                     KeyStroke      accel,
                                     ActionListener list)
    {
/*
TODO
        JMenuItem mi = UIUtils.createMenuItem (label,
                                               icon,
                                               list);

        mi.setActionCommand (actionCommand);

        mi.setAccelerator (accel);

        return mi;
*/
        return null;

    }

    public void addItemsToPopupMenu (ContextMenu cm)
    {

    }

    public void fillPopup (JPopupMenu m,
                           MouseEvent ev)
    {

    }

    public void handleMiddlePress (MouseEvent ev)
    {

    }

    public void handlePress (MouseEvent ev)
    {

    }

    public void handleDoublePress (MouseEvent ev)
    {

    }

    private void _handlePress (final MouseEvent ev)
    {

        final MouseEventHandler _this = this;

        if (SwingUtilities.isMiddleMouseButton (ev))
        {

            this.handleMiddlePress (ev);

            return;

        }

        if (ev.isPopupTrigger ())
        {

            UIUtils.runLater (() ->
            {

                ContextMenu cm = new ContextMenu ();

                this.addItemsToPopupMenu (cm);

                if (cm.getItems ().size () == 0)
                {

                    return;

                }

                cm.setAutoHide (true);

                cm.show (_this.parentNode, ev.getXOnScreen (), ev.getYOnScreen ());

            });

        }

        if (ev.getClickCount () == 2)
        {

            this.handleDoublePress (ev);

            return;

        }

        this.handlePress (ev);

    }

    public void mouseReleased (MouseEvent ev)
    {

        this._handlePress (ev);

        this.redispatch (ev);

    }

    public void mousePressed (MouseEvent ev)
    {

        if (!Environment.isWindows)
        {

            this._handlePress (ev);

            return;

        }

        this.redispatch (ev);

    }

    public void mouseMoved (MouseEvent e)
    {

        this.redispatch (e);

    }

    public void mouseDragged (MouseEvent e)
    {

        this.redispatch (e);

    }

    public void mouseEntered (MouseEvent e)
    {

        this.redispatch (e);

    }

    public void mouseExited (MouseEvent e)
    {

        this.redispatch (e);

    }

    @Override
    public void mouseClicked (MouseEvent e)
    {

        this.redispatch (e);

    }

}
