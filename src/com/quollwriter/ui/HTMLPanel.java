package com.quollwriter.ui;

import java.awt.*;
import java.util.StringTokenizer;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.net.*;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xhtmlrenderer.simple.*;
import org.xhtmlrenderer.swing.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.simple.extend.*;

import com.quollwriter.*;

public class HTMLPanel extends XHTMLPanel
{
    
    private AbstractProjectViewer projectViewer = null;
    
    public HTMLPanel (String                 t,
                      AbstractProjectViewer  projectViewer)
    {
        
        this.projectViewer = projectViewer;
        
        LinkListener ll = null;
        
        for (int i = 0; i < this.getMouseTrackingListeners ().size (); i++)
        {
            
            Object lll = this.getMouseTrackingListeners ().get (i);
            
            if (lll instanceof LinkListener)
            {
                
                ll = (LinkListener) lll;
                
            }
            
        }
        
        if (ll != null)
        {
            
            this.removeMouseTrackingListener (ll);
            
        }

        this.getSharedContext ().getTextRenderer ().setSmoothingThreshold (8f);

        final HTMLPanel _this = this;

        this.addMouseTrackingListener (new LinkListener ()
        {

            public void onMouseUp(BasicPanel panel, Box box)
            {

                String uri = null;

                Element e = box.getElement ();

                for (Node node = e;
                     node.getNodeType () == Node.ELEMENT_NODE;
                     node = node.getParentNode ())
                {
                    
                    uri = panel.getSharedContext().getNamespaceHandler().getLinkUri ((Element) node);

                    if (uri != null)
                    {
                
                        break;
                    
                    }
                
                }

                if (uri != null)
                {
                    
                    _this.handleURI (uri);
                                        
                }

            }
                                   
        });

        this.setText (t);
        
    }
    
    public void setText (String text)
    {

        if (text == null)
        {
            
            return;
            
        }

        StringBuilder buf = new StringBuilder ();

        int ind = text.indexOf ("{");
        
        while (ind > -1)
        {
            
            int end = text.indexOf ("}",
                                    ind + 1);

            if (end > ind + 1)
            {
                
                String v = text.substring (ind + 1,
                                           end);
                
                StringTokenizer st = new StringTokenizer (v,
                                                          ",;");
                
                String icon = st.nextToken ().trim ().toLowerCase ();
                String action = null;
                
                if (st.hasMoreTokens ())
                {
                    
                    action = st.nextToken ().trim ().toLowerCase ();
                    
                }
                
                v = "<img src=\"" + Environment.getIconURL (icon, Constants.ICON_MENU) + "\" />";
                                
                if (action != null)
                {
                    
                    v = "<a href=\"action:" + action + "\">" + v + "</a>";
                                
                }
                
                // Split up the value.
                text = text.substring (0,
                                       ind) + v + text.substring (end + 1);
                
                ind = text.indexOf ("{",
                                    ind + v.length ());
                
                
            }
            
        }

        StringBuilder t = new StringBuilder ();
        t.append ("<html><head>");
        t.append (UIUtils.getHTMLStyleSheet (null,
                                             null,
                                             null,
                                             // The panel scales the font size without taking the potential difference
                                             // in dpi into account.
                                             UIUtils.getPrintFontSize ()));
        t.append ("</head><body>");
        t.append (text);
        t.append ("</body></html>");

        this.setDocumentFromString (t.toString (),
                                    null,
                                    new XhtmlNamespaceHandler ());        
        
    }
    
    private void handleURI (String u)
    {
        
        URL uri = null;
        
        try
        {
            
            uri = new URL (u);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to convert string: " + u + " to a uri",
                                  e);
            
            return;
            
        }

        if (this.projectViewer != null)
        {
            
            UIUtils.openURL (this.projectViewer,
                             uri);

            return;
                             
        }
        /*
        if (uri.getScheme ().equals (Constants.HELP_PROTOCOL))
        {
            
            try
            {
            
                UIUtils.openURL (this,
                                 uri.toURL ());
                
                return;

            } catch (Exception e) {
                
                Environment.logError ("Unable to open help url: " +
                                      uri,
                                      e);
                
                return;
                
            }
            
        }
        
        if (uri.getScheme ().equals ("action"))
        {
                
            if (this.handler != null)
            {                
            
                this.handler.handleHTMLPanelAction (uri.getSchemeSpecificPart ());
                
            }

            return;
            
        }
        
        if (this.handler != null)
        {
        
            this.handler.handleHTMLPanelAction (u);
            
        }
          */      
    }
    
    public static JScrollPane createHelpPanel (String                 t,
                                               AbstractProjectViewer  projectViewer)
    {
        
        HTMLPanel p = new HTMLPanel (t,
                                     projectViewer);
        
        return HTMLPanel.createHelpPanel (p);
                
    }

    public static JScrollPane createHelpPanel (HTMLPanel p)
    {
                
        JScrollPane sp = new JScrollPane (p);
        
        sp.getVerticalScrollBar ().setUnitIncrement (20);
        sp.getViewport ().setOpaque (false);
        sp.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        /*
        sp.setMinimumSize (new Dimension (500,
                                          30));
        */
        sp.setBorder (null);
        sp.setBackground (null);
        sp.setOpaque (false);
        p.setBackground (null);
        p.setOpaque (false);
        
        return sp;
        
    }
    
}