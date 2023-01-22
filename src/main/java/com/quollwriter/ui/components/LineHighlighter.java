package com.quollwriter.ui.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/*
 *  Adapted from: http://www.camick.com/java/source/LinePainter.java
 *  See: http://tips4java.wordpress.com/2008/10/29/line-painter/
 *
 *  Track the movement of the Caret by painting a background line at the
 *  current caret position.
 */
public class LineHighlighter implements Highlighter.HighlightPainter
{
	private JTextComponent component;

	private Paint paint;

	private Rectangle lastView;
    private CaretListener caretListener = null;
    private MouseAdapter mouseListener = null;
    private Object highlight = null;

	/*
	 *
	 *  @param component  text component that requires background line painting
	 *  @param color      the color of the background line
	 */
	public LineHighlighter (Paint          paint)
	{
		
        this.setPaint (paint);

		//  Add listeners so we know when to change highlighting
        final LineHighlighter _this = this;
        
        this.mouseListener = new MouseAdapter ()
        {
            
            public void mousePressed(MouseEvent e)
            {
            
                _this.resetHighlight ();
            
            }

            public void mouseDragged(MouseEvent e)
            {
	
                _this.resetHighlight ();
	
            }
            
        };
        
		this.caretListener = new CaretListener ()
        {
            
            public void caretUpdate(CaretEvent e)
            {
                
                _this.resetHighlight ();
            
            }            
            
        };
        
	}

    public void install (JTextComponent c)
    {
        
        if (this.component != null)
        {
            
            this.uninstall ();
            
        }
        
        this.component = c;
        c.addCaretListener (this.caretListener);
		c.addMouseListener (this.mouseListener);
		c.addMouseMotionListener (this.mouseListener);

		//  Turn highlighting on by adding a dummy highlight

		try
		{
			this.highlight = c.getHighlighter().addHighlight(0, 0, this);
		}
		catch(BadLocationException ble) {}
        
        c.validate ();
        c.repaint ();
                
    }
    
    public void uninstall ()
    {
        
        if (this.component == null)
        {
            
            return;
            
        }
             
        this.component.removeCaretListener (this.caretListener);
        this.component.removeMouseListener (this.mouseListener);
        this.component.removeMouseMotionListener (this.mouseListener);
        
        //this.resetHighlight ();
        this.component.getHighlighter ().removeHighlight (this.highlight);
        
        this.component.validate ();
        this.component.repaint ();
        
    }
    
	/*
	 *	You can reset the line color at any time
	 *
	 *  @param color  the color of the background line
	 */
	public void setPaint(Paint paint)
	{
		this.paint = paint;
	}

	//  Paint the background highlight

	public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c)
	{
		try
		{
            
            Graphics2D g2 = (Graphics2D) g;
            
			Rectangle r = c.modelToView(c.getCaretPosition());
			
            int h = r.height;
            
            Document doc = c.getDocument ();
            
            if (doc instanceof AbstractDocument)
            {
                            
                Element el = ((AbstractDocument) doc).getParagraphElement (c.getCaretPosition ());
                
                if (el != null)
                {                            
                
                    h *= (1 / (1 + StyleConstants.getLineSpacing (el.getAttributes ())));

                }
                
            }
            
            g2.setPaint ( this.paint );
			g2.fillRect (0,
                         r.y,
                         c.getWidth(),
                         h);

			if (lastView == null)
            {
				lastView = r;
            }
            
		}
		catch(BadLocationException ble) {}
	}

	/*
	*   Caret position has changed, remove the highlight
	*/
	private void resetHighlight()
	{
		//  Use invokeLater to make sure updates to the Document are completed,
		//  otherwise Undo processing causes the modelToView method to loop.

        if (this.lastView == null)
        {
            
            return;
            
        }
        
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					int offset =  component.getCaretPosition();
					Rectangle currentView = component.modelToView(offset);

					//  Remove the highlighting from the previously highlighted line

					if (lastView.y != currentView.y)
					{
                        
                        //component.validate ();
                        //component.repaint ();
						component.repaint(0, lastView.y, component.getWidth(), lastView.height);
						lastView = currentView;
					}
				}
				catch(BadLocationException ble) {}
			}
		});
	}

}