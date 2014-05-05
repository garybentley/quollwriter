package com.quollwriter.ui.components;

import java.awt.*;
import javax.swing.*;

public class ScrollablePanel extends JPanel implements Scrollable
{
    
    public ScrollablePanel (LayoutManager layout)
    {
        
        super (layout);
        
    }
    
    public Dimension getPreferredScrollableViewportSize() {
        return super.getPreferredSize(); //tell the JScrollPane that we want to be our 'preferredSize' - but later, we'll say that vertically, it should scroll.
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;//set to 16 because that's what you had in your code.
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 1;//set to 16 because that's what you had set in your code.
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;//track the width, and re-size as needed.
    }

    public boolean getScrollableTracksViewportHeight()
    {
        
        if (this.getParent() instanceof JViewport)
		{
		    return (((JViewport)this.getParent()).getHeight() > this.getPreferredSize().height);
		}        
        
        return false; //we don't want to track the height, because we want to scroll vertically.
    }
    
    
}