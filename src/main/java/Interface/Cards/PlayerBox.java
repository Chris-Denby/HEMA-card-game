/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface.Cards;

import Interface.Constants;
import Interface.PlayArea;
import Interface.PlayerHand;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author chris
 */
public class PlayerBox extends JPanel
{
    private int playerHealth = 0;
    private int width;
    private int height;
    private JLabel playerNameLabel;
    private JLabel playerHealthLabel;
    private boolean isOpponent;
    PlayArea playArea;
    
    Image image = null;
    
    private int arcSize;
    private int strokeSize = 1;
    private Color shadowColor = Constants.shadowColor;
    private int shadowGap = 4;
    private int shadowOffset = 4;
    private Color backgroundColor = Constants.cardBaseColor;
    private boolean isSelected = false;
    
    public PlayerBox(int containerHeight, boolean isOpponent, PlayArea playArea)
    {
        
        this.isOpponent = isOpponent;
        this.height = (int) Math.round(containerHeight*0.75);
        this.width = this.height;
        this.setMinimumSize(new Dimension(width,height));
        this.setPreferredSize(new Dimension(width,height));
        this.setSize(new Dimension(width,height));
        arcSize = (int) Math.round(height/10);
        playerNameLabel = new JLabel();
        playerHealthLabel = new JLabel();
        playerHealthLabel.setForeground(Color.WHITE);
        playerHealthLabel.setBackground(new Color(0,0,0,200));
        this.add(playerHealthLabel);
        setPlayerHealth(Constants.defaultPlayerHealth);
        setOpaque(false);
        this.playArea = playArea;
    }
    
    public void setImage (Image img)
    {
        image = img;
        repaint();
        revalidate();
    }
    
    
    public boolean getIsOpponent()
    {
        return isOpponent;
    }
    
    public void setPlayerHealth(int health)
    {
        this.playerHealth = health;
        this.playerHealthLabel.setText(playerHealth+"");
        //repaint();
        //revalidate();
    }
    
    public void setIsSelected(boolean selected)
    {
        this.isSelected = selected;
        this.repaint();
        this.revalidate();
    }
    
    public boolean getIsSelected()       
    {
        return isSelected;
    }
    
    public void takeDamage(int damage)
    {
        setPlayerHealth(playerHealth-damage);
        playArea.getGameWindow().playSound("playerDamaged");  
               
    }
    
    public int getPlayerHealth()
    {
        return playerHealth;
    }
    
    public void gainLife(int life)
    {
        setPlayerHealth(getPlayerHealth()+life);
    }
       
    @Override
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g;
        
        //draw shap of JPanel
        
        //draw shadow
        this.setForeground(Color.black);
        Color strokeColor = getForeground();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(shadowColor);
        graphics.fillRoundRect(shadowOffset,shadowOffset,width-strokeSize-shadowOffset,height-strokeSize-shadowOffset,arcSize,arcSize);     
                
        //draw fill
        if(isSelected)
        {   
            strokeColor = Color.RED;
            strokeSize = 5;
        }
        else
            strokeSize = 1;
        
        graphics.setColor(backgroundColor);
        graphics.fillRoundRect(0,0,width-shadowGap,height-shadowGap,arcSize,arcSize);
        graphics.setColor(strokeColor);
        graphics.setStroke(new BasicStroke(strokeSize));
        graphics.drawRoundRect(0,0,width-shadowGap,height-shadowGap,arcSize,arcSize);
        graphics.setStroke(new BasicStroke()); 
        if(image!=null)
        {
            int gap = 5;
            int roundRectWidthHeight = width-shadowGap;
            //image = image.getScaledInstance(roundRectWidthHeight-(gap*2), roundRectWidthHeight-(gap*2), Image.SCALE_DEFAULT);
            graphics.drawImage(image, gap+strokeSize,gap+strokeSize, this);
        }
    }
    
    
    
}
