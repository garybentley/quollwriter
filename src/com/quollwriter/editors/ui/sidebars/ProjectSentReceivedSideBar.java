package com.quollwriter.editors.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.events.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.text.*;

public abstract class ProjectSentReceivedSideBar<E extends EditorMessage, V extends ProjectSentReceivedViewer> extends AbstractSideBar<V>
{

    private ProjectCommentsChaptersAccordionItem chapters = null;
    private EditorInfoBox editorInfoBox = null;
    private EditorEditor editor = null;
    private JComponent content = null;
    protected E message = null;

    public ProjectSentReceivedSideBar (V v,
                                       E message)
    {

        super (v);

        this.editor = message.getEditor ();
        this.message = message;
        this.content = new Box (BoxLayout.Y_AXIS);

        this.content.setOpaque (false);
        this.content.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.setMinimumSize (new Dimension (200,
                                            200));

    }

    public E getMessage ()
    {

        return this.message;

    }

    public abstract String getItemsTitle ();

    public abstract int getItemCount ();

    public abstract String getItemsIconType ();

    public abstract JComponent getMessageDetails (E message);

    public JComponent getContent ()
    {

        return this.content;

    }

    public void init ()
               throws GeneralException
    {

        final ProjectSentReceivedSideBar _this = this;

        super.init ();

        this.editorInfoBox = new EditorInfoBox (this.editor,
                                                this.viewer,
                                                true).init ();
        this.editorInfoBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                          this.editorInfoBox.getPreferredSize ().height));
        this.editorInfoBox.setBorder (UIUtils.createPadding (5, 5, 5, 0));

        this.chapters = new ProjectCommentsChaptersAccordionItem (this.viewer)
        {

            @Override
            public int getItemCount ()
            {

                return _this.getItemCount ();

            }

        };

        this.chapters.setIconType (this.getItemsIconType ());
        this.chapters.setTitle (this.getItemsTitle ());

        this.chapters.init ();

        this.content.add (this.editorInfoBox);

        JComponent messageC = this.getMessageDetails (this.message);

        if (messageC != null)
        {

            messageC.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                    messageC.getPreferredSize ().height));
            messageC.setOpaque (false);
            messageC.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            messageC.setBorder (UIUtils.createPadding (5, 0, 0, 5));

            this.content.add (messageC);

        }

        final Border topLineBorder = new MatteBorder (1, 0, 0, 0,
                                                      UIUtils.getInnerBorderColor ());
        final Border noBorder = UIUtils.createPadding (1, 0, 0, 0);

        final JScrollPane sp = this.wrapInScrollPane (this.chapters);

        sp.setBorder (noBorder);

        sp.getVerticalScrollBar ().addAdjustmentListener (new AdjustmentListener ()
        {

            public void adjustmentValueChanged (AdjustmentEvent ev)
            {

                if (sp.getVerticalScrollBar ().getValue () > 0)
                {

                    sp.setBorder (topLineBorder);

                } else {

                    sp.setBorder (noBorder);

                }

            }

        });
        sp.setMinimumSize (new Dimension (200,
                                          300));

        this.content.add (sp);
        this.chapters.setBorder (UIUtils.createPadding (0, 0, 0, 0));

    }

    public void reloadTreeForObjectType (String objType)
    {

        this.chapters.update ();

    }

    public void showObjectInTree (String      treeObjType,
                                  NamedObject obj)
    {

        JTree tree = this.getTreeForObjectType (treeObjType);

        if (tree == null)
        {

            return;

        }

        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        tree.expandPath (UIUtils.getTreePathForUserObject (root,
                                                           obj));

    }

    public JTree getTreeForObjectType (String objType)
    {

        return this.chapters.getTree ();

    }

    public boolean removeOnClose ()
    {

        return false;

    }

    public void onClose ()
    {


    }

    @Override
    public String getIconType ()
    {

        return null;

    }

    public void panelShown (MainPanelEvent ev)
    {

        this.chapters.setObjectSelectedInTree (ev.getForObject ());

    }

    public boolean canClose ()
    {

        return false;

    }

    @Override
    public List<JComponent> getHeaderControls ()
    {

        return null;

    }

}
