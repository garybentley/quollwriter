package com.quollwriter.exporter;

import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.renderers.*;


public abstract class AbstractDocumentExporter implements DocumentExporter
{

    protected Project proj = null;

    protected JTree itemsTree = null;

    public void setProject (Project p)
    {

        this.proj = p;

    }

    public void initItemsTree (List<String> excludeTypes)
    {

        if (this.itemsTree != null)
        {

            return;

        }

        final AbstractDocumentExporter _this = this;

        this.itemsTree = UIUtils.createTree ();
        this.itemsTree.setModel (null);

        this.itemsTree.addMouseListener (new MouseAdapter ()
            {

                private void selectAllChildren (DefaultTreeModel       model,
                                                DefaultMutableTreeNode n,
                                                boolean                v)
                {

                    Enumeration<DefaultMutableTreeNode> en = n.children ();

                    while (en.hasMoreElements ())
                    {

                        DefaultMutableTreeNode c = en.nextElement ();

                        Object uo = c.getUserObject ();

                        if (uo instanceof SelectableDataObject)
                        {

                            SelectableDataObject s = (SelectableDataObject) uo;

                            s.selected = v;

                            // Tell the model that something has changed.
                            model.nodeChanged (c);

                            // Iterate.
                            this.selectAllChildren (model,
                                                    c,
                                                    v);

                        }

                    }

                }

                public void mousePressed (MouseEvent ev)
                {

                    TreePath tp = _this.itemsTree.getPathForLocation (ev.getX (),
                                                                      ev.getY ());

                    if (tp != null)
                    {

                        DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                        // Tell the model that something has changed.
                        DefaultTreeModel model = (DefaultTreeModel) _this.itemsTree.getModel ();

                        SelectableDataObject s = (SelectableDataObject) n.getUserObject ();

                        s.selected = !s.selected;

                        model.nodeChanged (n);

                        this.selectAllChildren (model,
                                                n,
                                                s.selected);

                    }

                }

            });

        this.itemsTree.setCellRenderer (new SelectableProjectTreeCellRenderer ());

        this.itemsTree.setOpaque (false);
        this.itemsTree.setBorder (new EmptyBorder (5,
                                                   5,
                                                   5,
                                                   5));

        if (excludeTypes == null)
        {

            excludeTypes = new ArrayList ();

        }

        this.itemsTree.setModel (new DefaultTreeModel (this.createTree (this.proj,
                                                                        excludeTypes)));

    }

    private DefaultMutableTreeNode createTree (Project      p,
                                               List<String> excludeTypes)
    {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode (new SelectableDataObject (p));

        DefaultMutableTreeNode n = null;
        DefaultMutableTreeNode tn = null;

        if (!excludeTypes.contains (Chapter.OBJECT_TYPE))
        {

            if (p.getBooks ().size () > 0)
            {

                Book b = p.getBooks ().get (0);

                if (b.getChapters ().size () > 0)
                {

                    TreeParentNode c = new TreeParentNode (Chapter.OBJECT_TYPE,
                                                           Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE));

                    SelectableDataObject sd = new SelectableDataObject (c);

                    sd.selected = true;

                    tn = new DefaultMutableTreeNode (sd);

                    root.add (tn);

                    Collections.sort (b.getChapters (),
                                      NamedObjectSorter.getInstance ());

                    for (Chapter ch : b.getChapters ())
                    {

                        sd = new SelectableDataObject (ch);

                        sd.selected = true;

                        n = new DefaultMutableTreeNode (sd);

                        tn.add (n);

                    }

                }

            }

        }

        Set<UserConfigurableObjectType> assetTypes = Environment.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType t : assetTypes)
        {
            
            Set<Asset> as = p.getAssets (t);
        
            if (excludeTypes.contains (t.getObjectTypeId ()))
            {
                
                continue;
                
            }
        
            if (as == null)
            {
                
                continue;
                
            }
        
            this.addAssetsToTree (root,
                                  t,
                                  as);
            
        }
        /*
        if (!excludeTypes.contains (QCharacter.OBJECT_TYPE))
        {

            this.addAssetsToTree (root,
                                  p.getCharacters ());

        }

        if (!excludeTypes.contains (Location.OBJECT_TYPE))
        {

            this.addAssetsToTree (root,
                                  p.getLocations ());

        }

        if (!excludeTypes.contains (QObject.OBJECT_TYPE))
        {

            this.addAssetsToTree (root,
                                  p.getQObjects ());

        }

        if (!excludeTypes.contains (ResearchItem.OBJECT_TYPE))
        {

            this.addAssetsToTree (root,
                                  p.getResearchItems ());

        }
*/
        return root;

    }

    private void addAssetsToTree (DefaultMutableTreeNode     root,
                                  UserConfigurableObjectType type,
                                  Set<Asset>                 assets)
    {

        if (assets.size () > 0)
        {

            TreeParentNode c = new TreeParentNode (type.getObjectTypeId (),
                                                   type.getObjectTypeNamePlural ());

            SelectableDataObject sd = new SelectableDataObject (c);

            sd.selected = true;

            DefaultMutableTreeNode tn = new DefaultMutableTreeNode (sd);

            root.add (tn);

            java.util.List<Asset> lassets = new ArrayList (assets);
            
            Collections.sort (lassets,
                              NamedObjectSorter.getInstance ());

            for (Asset a : lassets)
            {

                sd = new SelectableDataObject (a);

                sd.selected = true;

                DefaultMutableTreeNode n = new DefaultMutableTreeNode (sd);

                tn.add (n);

            }

        }

    }

}
