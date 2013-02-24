package com.quollwriter.ui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;
import java.util.List;

import javax.activation.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import javax.swing.tree.*;

import com.quollwriter.*;

import com.quollwriter.data.*;


/**
 * Found at: http://forums.sun.com/thread.jspa?threadID=497065&messageID=2431829
 */
public class DataObjectTransferHandler extends TransferHandler
{

    protected static DropHandler dropHandler = new DropHandler ();

    protected static class DropHandler implements DropTargetListener,
                                                  Serializable
    {

        private boolean canImport;

        private boolean actionSupported (int action)
        {

            return (action & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK)) !=
                   DnDConstants.ACTION_NONE;
        }

        // --- DropTargetListener methods -----------------------------------

        public void dragEnter (DropTargetDragEvent e)
        {
            DataFlavor[] flavors = e.getCurrentDataFlavors ();

            JComponent      c = (JComponent) e.getDropTargetContext ().getComponent ();
            TransferHandler importer = c.getTransferHandler ();

            if ((importer != null) && importer.canImport (c,
                                                          flavors))
            {
                canImport = true;
            } else
            {
                canImport = false;
            }

            int dropAction = e.getDropAction ();

            if (canImport && actionSupported (dropAction))
            {
                e.acceptDrag (dropAction);
            } else
            {
                e.rejectDrag ();
            }
        }

        public void dragOver (DropTargetDragEvent e)
        {
            int dropAction = e.getDropAction ();

            if (canImport && actionSupported (dropAction))
            {

                Component c = e.getDropTargetContext ().getComponent ();

                if (c instanceof JTable)
                {

                    JTable table = (JTable) c;
                    int    row = table.rowAtPoint (e.getLocation ());
                    table.getSelectionModel ().setSelectionInterval (row,
                                                                     row);
                    e.acceptDrag (dropAction);

                }

                if (c instanceof JTree)
                {

                    JTree tree = (JTree) c;

                    TreePath itp = tree.getPathForLocation (e.getLocation ().x,
                                                            e.getLocation ().y);

                    tree.getSelectionModel ().setSelectionPath (itp);

                    itp = tree.getPathForRow (tree.getRowForPath (itp));

                    if (itp == null)
                    {

                        return;

                    }

                    NamedObject n = (NamedObject) ((DefaultMutableTreeNode) itp.getLastPathComponent ()).getUserObject ();

                    if (n instanceof Chapter)
                    {

                        e.acceptDrag (dropAction);

                    } else
                    {

                        e.rejectDrag ();

                    }

                }

            } else
            {

                e.rejectDrag ();
            }

        }

        public void dragExit (DropTargetEvent e)
        {
        }

        public void drop (DropTargetDropEvent e)
        {
            int dropAction = e.getDropAction ();

            JComponent                c = (JComponent) e.getDropTargetContext ().getComponent ();
            DataObjectTransferHandler importer = (DataObjectTransferHandler) c.getTransferHandler ();

            if (canImport && (importer != null) && actionSupported (dropAction))
            {
                e.acceptDrop (dropAction);


                try
                {
                    Transferable t = e.getTransferable ();
                    importer.setDropPoint (e.getLocation ());
                    importer.setDropComponent (c);
                    e.dropComplete (importer.importData (c,
                                                         t));
                } catch (RuntimeException re)
                {
                    e.dropComplete (false);
                }
            } else
            {
                e.rejectDrop ();
            }
        }

        public void dropActionChanged (DropTargetDragEvent e)
        {
            int dropAction = e.getDropAction ();

            if (canImport && actionSupported (dropAction))
            {
                e.acceptDrag (dropAction);
            } else
            {
                e.rejectDrag ();
            }
        }
    }

    public class GenericTransferable implements Transferable
    {

        private Object obj = null;

        public GenericTransferable(Object obj)
        {

            this.obj = obj;

        }

        public Object getTransferData (DataFlavor flavor)
                                throws UnsupportedFlavorException,
                                       IOException
        {

            if (this.isDataFlavorSupported (flavor))
            {

                return this.obj;

            } else
            {

                throw new UnsupportedFlavorException (flavor);

            }

        }

        public DataFlavor[] getTransferDataFlavors ()
        {

            return new DataFlavor[] { DataFlavor.stringFlavor };

        }

        public boolean isDataFlavorSupported (DataFlavor flavor)
        {

            DataFlavor[] flavors = getTransferDataFlavors ();

            for (int i = 0; i < flavors.length; i++)
            {

                if (flavors[i].equals (flavor))
                {

                    return true;

                }

            }

            return false;

        }

    }

    protected Point           dragPoint;
    protected Point           dropPoint;
    protected Component       dragComponent;
    protected Component       dropComponent;
    private AbstractProjectViewer     projectViewer = null;
    private DragActionHandler dragActionHandler = null;

    public DataObjectTransferHandler (AbstractProjectViewer pv,
                                      DragActionHandler     dah)
    {

        this.projectViewer = pv;
        this.dragActionHandler = dah;

    }

    public Point getDropPoint ()
    {
        return dropPoint;
    }

    public void setDropPoint (Point dropPoint)
    {
        this.dropPoint = dropPoint;
    }

    public Component getDragComponent ()
    {
        return dragComponent;
    }

    public void setDragComponent (Component dragComponent)
    {
        this.dragComponent = dragComponent;
    }

    public Point getDragPoint ()
    {
        return dragPoint;
    }

    public void setDragPoint (Point dragPoint)
    {
        this.dragPoint = dragPoint;
    }

    public Component getDropComponent ()
    {
        return dropComponent;
    }

    public void setDropComponent (Component dropComponent)
    {
        this.dropComponent = dropComponent;
    }

    public int getSourceActions (JComponent c)
    {
        return DnDConstants.ACTION_COPY_OR_MOVE;
    }

    public boolean canImport (JComponent   comp,
                              DataFlavor[] transferFlavors)
    {

        if (comp.isEnabled ())
        {

            for (int i = 0; i < transferFlavors.length; i++)
            {

                if (transferFlavors[i].equals (DataFlavor.stringFlavor))
                {

                    return true;

                }

            }

        }

        return false;

    }

    public void exportAsDrag (JComponent comp,
                              InputEvent e,
                              int        action)
    {
        setDragComponent (comp);
        setDragPoint (((MouseEvent) e).getPoint ());

        super.exportAsDrag (comp,
                            e,
                            action);
    }

    protected Transferable createTransferable (JComponent c)
    {
        Transferable t = null;

        if (c instanceof JTable)
        {

            JTable table = (JTable) c;
            int[]  selection = table.getSelectedRows ();
            Vector selectedRows = new Vector ();

            for (int j = 0; j < selection.length; j++)
            {
                selectedRows.add (((DefaultTableModel) table.getModel ()).getDataVector ().get (selection[j]));
            }

            t = new GenericTransferable (selectedRows);

        }

        if (c instanceof JTree)
        {
            JTree tree = (JTree) c;

            t = new GenericTransferable (tree.getSelectionPath ());

        }

        return t;
    }

    public boolean importData (JComponent   comp,
                               Transferable t)
    {

        if (canImport (comp,
                       t.getTransferDataFlavors ()))
        {

            try
            {

                if (getDragComponent () != comp)
                {
                    List list = (List) t.getTransferData (DataFlavor.stringFlavor);

                    if (comp instanceof JTable)
                    {

                        JTable            table = (JTable) comp;
                        DefaultTableModel model = ((DefaultTableModel) table.getModel ());
                        int               insertRow;

                        if (getDropPoint () != null)
                        {
                            insertRow = table.rowAtPoint (getDropPoint ());
                        } else
                        {
                            insertRow = table.getSelectedRow ();
                        }

                        for (int i = 0; i < list.size (); i++)
                        {
                            model.insertRow (insertRow + i,
                                             (Vector) list.get (i));
                        }

                        table.getSelectionModel ().clearSelection ();
                        table.getSelectionModel ().setSelectionInterval (insertRow,
                                                                         insertRow + list.size () - 1);
                        table.requestFocus ();

                    }

                    if (comp instanceof JTree)
                    {

                        JTree tree = (JTree) comp;

                        int insertRow;

                        if (getDropPoint () != null)
                        {

                            insertRow = tree.getRowForLocation (getDropPoint ().x,
                                                                getDropPoint ().y);

                        } else
                        {

                            insertRow = tree.getRowForPath (tree.getSelectionPath ());

                        }

                        DefaultTreeModel model = ((DefaultTreeModel) tree.getModel ());

                        for (int i = 0; i < list.size (); i++)
                        {

                            TreePath tp = (TreePath) list.get (i);

                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                            DefaultMutableTreeNode p = (DefaultMutableTreeNode) n.getParent ();

                            model.removeNodeFromParent (n);

                            model.insertNodeInto (n,
                                                  p,
                                                  insertRow);

                        }

                        tree.getSelectionModel ().clearSelection ();

                        tree.requestFocus ();

                    }

                }

                return true;
            } catch (Exception e)
            {

            }
        } // if

        return false;
    }

    protected void exportDone (JComponent   source,
                               Transferable data,
                               int          action)
    {

        if (action == DnDConstants.ACTION_MOVE)
        {

            try
            {

                if (source instanceof JTable)
                {

                    List list = (List) data.getTransferData (DataFlavor.stringFlavor);

                    JTable            table = (JTable) source;
                    DefaultTableModel model = ((DefaultTableModel) table.getModel ());

                    if (source != getDropComponent ())
                    {
                        int index;

                        for (int i = 0; i < list.size (); i++)
                        {
                            index = model.getDataVector ().indexOf (list.get (i));
                            model.removeRow (index);

                        }
                    } else
                    {
                        int index;
                        int insertRow = table.rowAtPoint (getDropPoint ());

                        for (int i = 0; i < list.size (); i++)
                        {
                            index = model.getDataVector ().indexOf (list.get (i));
                            model.moveRow (index,
                                           index,
                                           insertRow + i);
                        }
                    }

                }

                if (source instanceof JTree)
                {

                    TreePath removeTreePath = (TreePath) data.getTransferData (DataFlavor.stringFlavor);

                    JTree tree = (JTree) source;

                    DefaultTreeModel model = ((DefaultTreeModel) tree.getModel ());

                    if (source != getDropComponent ())
                    {

                        model.removeNodeFromParent ((DefaultMutableTreeNode) removeTreePath.getLastPathComponent ());

                    } else
                    {

                        int insertRow = tree.getRowForLocation (getDropPoint ().x,
                                                                getDropPoint ().y);

                        int removeRow = tree.getRowForPath (removeTreePath);

                        TreePath insertTreePath = tree.getPathForRow (insertRow);

                        DefaultMutableTreeNode removeNode = (DefaultMutableTreeNode) removeTreePath.getLastPathComponent ();

                        DefaultMutableTreeNode insertNode = (DefaultMutableTreeNode) insertTreePath.getLastPathComponent ();

                        NamedObject removeObject = (NamedObject) removeNode.getUserObject ();

                        NamedObject insertObject = (NamedObject) insertNode.getUserObject ();

                        try
                        {

                            if (!this.dragActionHandler.performAction (removeRow,
                                                                       removeObject,
                                                                       insertRow,
                                                                       insertObject))
                            {

                                return;

                            }

                        } catch (Exception e)
                        {

                            Environment.logError ("Unable to move: " +
                                                  removeObject +
                                                  " from row: " +
                                                  removeRow +
                                                  " to row: " +
                                                  insertRow,
                                                  e);

                            UIUtils.showErrorMessage (this.projectViewer,
                                                      "Unable to move.");

                            return;

                        }

                    }

                }

            } catch (Exception e)
            {
                e.printStackTrace ();
            }
        } // if
    }

    public static DropHandler getDropHandler ()
    {
        return dropHandler;
    }

}
