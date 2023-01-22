package com.quollwriter.ui.components;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;

public class MultiImagePanel extends JPanel
{

    private float alpha = 1f;
    private List<Image> images = new ArrayList ();
    private Image fromImage = null;
    private Image toImage = null;
    private Image bgImage = null;
    private Image currImage = null;
    private int current = 0;
    private Timer timer = null;

    public MultiImagePanel ()
    {

        this.setDoubleBuffered (true);

        //this.setOpaque (true);
        
        this.setLayout (null);

    }
    
    public void start (final int showFor,
                       final int transitionTime)
    {
    
        int showForX = showFor;
        int transitionTimeX = transitionTime;
    
        if (transitionTime > showFor)
        {
            
            showForX = transitionTimeX;
            
        }
    
        if (transitionTime <= 0)
        {
            
            transitionTimeX = -1;
            
        }
    
        final int _showFor = showForX;
        final int _transitionTime = transitionTimeX;
    
        final MultiImagePanel _this = this;

        if (_showFor < 1)
        {
            
            return;
            
        }

        this.timer = new Timer (_showFor,
                                null);
        
        this.timer.setRepeats (true);
        
        int split = 50;
                
        final int tick = (_transitionTime > 0 ? _transitionTime : 10000) / 50;

        this.timer.addActionListener (new ActionAdapter ()
        {
           
            private long startTime = 0;
            private boolean processing = false;
           
            public void actionPerformed (ActionEvent ev)
            {

                if (_this.images.size () == 0)
                {
                    
                    _this.timer.stop ();
                    
                    return;
                    
                }

                if (this.processing)
                {
                    
                    return;
                    
                }

                this.processing = true;

                if (startTime == 0)
                {
                    
                    _this.timer.setDelay (tick);
                    startTime = System.currentTimeMillis ();
                    
                    // Get the from.
                    _this.fromImage = _this.images.get (_this.current);
                    
                    int next = _this.current + 1;
                    
                    if (next == _this.images.size ())
                    {
                        
                        next = 0;
                        
                    }
                    
                    _this.toImage = _this.images.get (next);
                    
                    //_this.currImage = null;
                    
                    _this.currImage = _this.toImage;
                    
                    _this.current = next;

                }
                
                long now = System.currentTimeMillis ();

                if (now > (startTime + _transitionTime))
                {

                    _this.timer.stop ();
                    this.startTime = 0;
                    _this.timer.setInitialDelay (showFor);
                    
                    _this.currImage = _this.toImage;

                    _this.timer.start ();
                    
                    this.processing = false;

                    //_this.validate ();
                    _this.repaint ();

                    return;
                    
                }

                float v = 1f - ((float) (now - startTime) / _transitionTime);
                                    
                _this.setImageAlpha (v);
                                                                        
                //_this.validate ();
                _this.repaint ();
                           
                this.processing = false;
                                                                                    
            }
            
        });

        this.timer.setDelay (tick);
        
        this.timer.start ();
        
    }

    public void stop ()
    {
        
        this.timer.stop ();
        
    }
    
    public void setBackgroundImage (Image im)
    {
        
        this.bgImage = im;
        
    }
    
    public void setCurrentImage (Image im)
    {
        
        this.currImage = im;
        
        this.images.add (im);
        
        this.repaint ();
        
    }
    
    public void addImage (Image im)
    {

        if (this.currImage == null)
        {
            
            this.currImage = im;
            
        }
        
        this.images.add (im);
        
    }

    public void setImageAlpha (float f)
    {
        
        this.alpha = f;
        
    }

    public void paintComponent (Graphics g)
    {

        Graphics2D g2d = (Graphics2D) g;

        //super.paintComponent (g);
                
        Insets insets = this.getInsets ();

        if (this.bgImage != null)
        {
        
            g.drawImage (this.bgImage,
                         insets.left,
                         insets.top,
                         this);

        }

        Composite oldC = g2d.getComposite ();
        
        if (this.currImage != null)
        {

            g.drawImage (this.currImage,
                         insets.left,
                         insets.top,
                         this);

        } else {

            // From image first.
            if (this.alpha > 0.5f)
            {
            
                g2d.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER,
                                                              this.alpha));
    
                g.drawImage (this.fromImage,
                             insets.left,
                             insets.top,
                             this);

            }
                        
            g2d.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER,
                                                          1f - this.alpha));    
        
            g.drawImage (this.toImage,
                         insets.left,
                         insets.top,
                         this);
            
        }
        
        g2d.setComposite (oldC);
                
    }

}
