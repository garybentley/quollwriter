package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import java.io.*;

import javax.swing.*;

public class BackgroundImagePanel extends JPanel implements MouseWheelListener
{

    private Paint bg = null;
    private float opacity = 0f;
    
    private Object backgroundObject = null;
    
    public BackgroundImagePanel (LayoutManager layout)
    {
        
        super (layout);
        
        this.addMouseWheelListener (this);
                
    }
    
    public void mouseWheelMoved (MouseWheelEvent ev)
    {

        if (ev.isShiftDown ())
        {
    
            int r = ev.getWheelRotation ();
            
            this.setBackgroundOpacity (this.opacity + (-1 * r * 0.1f));

            return;
            
        }
            
        ev.getComponent ().getParent ().dispatchEvent (ev);
        
    }    
    
    public float getBackgroundOpacity ()
    {
        
        return this.opacity;
        
    }
    
    public void setBackgroundOpacity (float v)
    {
        
        this.opacity = v;

        if (this.opacity < 0f)
        {
            
            this.opacity = 0f;
            
        }
        
        if (this.opacity > 1)
        {
            
            this.opacity = 1;
            
        }
        
        this.validate ();
        this.repaint ();
        
    }
    
    public String getBackgroundObjectAsString ()
    {
        
        if (this.backgroundObject == null)
        {
            
            return "none";
            
        }
        
        if (this.backgroundObject instanceof BackgroundImage)
        {
            
            return "bg:" + ((BackgroundImage) this.backgroundObject).getName ();
            
        }
    
        if (this.backgroundObject instanceof Color)
        {
            
            return UIUtils.colorToHex ((Color) this.backgroundObject);
            
        }
    
        if (this.backgroundObject instanceof File)
        {
            
            return "file:" + this.backgroundObject;
            
        }        
        
        return "none";
        
    }
    
    public Object getBackgroundObject ()
    {
        
        return this.backgroundObject;
        
    }
    
    public void paintComponent (Graphics g)
    {
    
        Graphics2D g2d = (Graphics2D) g;

        if (this.bg != null)
        {

            g2d.setPaint (this.bg);
            g2d.fill (g2d.getClip ());

            g2d.setPaint (new Color (0,
                                     0,
                                     0,
                                     this.opacity));
            g2d.fill (g2d.getClip ());
            
        }
    
    }
    
    public void setBackgroundObject (Object o)
    {

        if (o == null)
        {
            
            this.setNoBackground ();
            
            return;
            
        }

        if (o instanceof String)
        {

            String b = o.toString ();
            
            if (b.equals ("none"))
            {
                
                this.setNoBackground ();
                
                return;
                
            }
            
            if (b.startsWith ("bg:"))
            {
                
                this.setBackgroundObject (new BackgroundImage (b.substring ("bg:".length ())));
                
                return;
                
            } 
            
            if (b.startsWith ("file:"))
            {
                
                this.setBackgroundObject (new File (b.substring ("file:".length ())));
                
                return;
                
            }
            
            if (b.startsWith ("#"))
            {
                
                this.setBackgroundObject (UIUtils.getColor (b));
                
                return;
                
            }
            
            this.setBackgroundObject ("bg:" + b);
                        
            return;
            
        }
    
        if (o instanceof BackgroundImage)
        {
            
            this.setBackgroundObject ((BackgroundImage) o);
            
            return;
            
        }

        if (o instanceof File)
        {
            
            this.setBackgroundObject ((File) o);
            
            return;
            
        }

        if (o instanceof BufferedImage)
        {
            
            this.setBackgroundObject ((BufferedImage) o);
            
            return;
            
        }
        
        if (o instanceof Color)
        {
            
            this.setBackgroundObject ((Color) o);
            
            return;
            
        }
        
    }

    private void setBackgroundObject (BackgroundImage im)
    {

        this.setBackgroundObject (UIUtils.getBufferedImage (im.getImage (),
                                                            this));

        this.backgroundObject = im;
           
    }

    private void setBackgroundObject (BufferedImage im)
    {

        int           height = im.getHeight ();
        int           width = im.getWidth ();
        
        Graphics2D    biG2d = (Graphics2D) im.getGraphics ();
        biG2d.drawImage (im,
                         0,
                         0,
                         Color.black,
                         this);
        
        this.bg = new TexturePaint (im,
                                    new Rectangle (0,
                                                   0,
                                                   width,
                                                   height));
                
        this.validate ();
        this.repaint ();
        
    }
    
    private void setNoBackground ()
    {

        this.bg = null;
    
        this.backgroundObject = "none";

        this.validate ();
        this.repaint ();
            
    }

    public void setBackgroundObject (Color c)
    {
                
        this.bg = c;
                
        this.backgroundObject = c;
        
        this.validate ();
        this.repaint ();
        
    }
    
    private void setBackgroundObject (File f)
    {    
    
        Image im = UIUtils.getImage (f);

        BufferedImage bi = new BufferedImage (im.getWidth (this),
                                              im.getHeight (this),
                                              BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.getGraphics ();
        g.drawImage (im,
                     0,
                     0,
                     this);

        g.dispose ();
        
        this.setBackgroundObject (bi);        

        this.backgroundObject = f;
                    
    }
    
}