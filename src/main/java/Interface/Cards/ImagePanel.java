/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface.Cards;

import Interface.Constants;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import static java.awt.Image.SCALE_DEFAULT;
import java.awt.RenderingHints;
import static java.awt.RenderingHints.VALUE_RENDER_SPEED;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author chris
 */
public class ImagePanel extends JPanel
{
    private transient Image image;
    
    public ImagePanel()
    {
    }
    
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
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);
        
        if(image!=null)
        {
            //image = image.getScaledInstance(this.getWidth(), this.getHeight(), SCALE_DEFAULT);
            //graphics.drawImage(image, 0, 0, this);
        }
    }
    
}
