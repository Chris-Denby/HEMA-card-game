/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author chris
 */
public class InnerCardPanel extends JPanel 
{
    private Image image;
    
    public void setImage(Image img)
    {
        image = img;
        repaint();
        revalidate();
    }
    
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g;
        
        //graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        
        if(image!=null)
        {
            //image = image.getScaledInstance(this.getWidth(), this.getHeight(), SCALE_DEFAULT);
            graphics.drawImage(image, 0, 0, this);
        }
    }
    
}
