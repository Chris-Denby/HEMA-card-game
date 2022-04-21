/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;

/**
 *
 * @author chris
 */
public class StatLabel extends JLabel 
{

    private int cardHeight;
    public StatLabel(int height)
    {
        super();
        cardHeight = height;
    }
    
    
        @Override
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g;
        Color backgroundColor = Color.YELLOW;
        Color strokeColor = Color.BLACK;
        int strokeSize = 1;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        //draw outline
        graphics.setColor(strokeColor);
        graphics.setStroke(new BasicStroke(strokeSize));
        graphics.drawOval(0, 0, cardHeight-5, cardHeight-5);

        
    }
    
}
