/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface.Cards;

import Interface.Constants;
import Interface.Constants.CardLocation;
import Interface.Constants.ActionEffect;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

/**
 *
 * @author chris
 */
public class ActionCard extends Card
{
    private int power = 1;
    private int toughness = 1;
    private JLabel powerLabel;
    private JLabel toughnessLabel;
    JLabel statDivider = new JLabel("/");
    private Font statFont = new Font("Courier",Font.BOLD,20);
    private boolean isBuffed = false;
    private int buffedBy = 0;
    private Constants.WeaponType weaponType = null;
    
    
    public ActionCard(String cardName, int imageID)
    {
        super(cardName,imageID);
        
        
        powerLabel = new JLabel();
        powerLabel.setFont(statFont);
        toughnessLabel = new JLabel();
        toughnessLabel.setFont(statFont);
        statDivider.setFont(statFont);
        powerLabel.setForeground(Color.WHITE);
        toughnessLabel.setForeground(Color.WHITE);
        statDivider.setForeground(Color.WHITE);
        powerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        toughnessLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statDivider.setHorizontalAlignment(SwingConstants.CENTER);
        powerLabel.setOpaque(false);
        toughnessLabel.setOpaque(false);
        statDivider.setOpaque(false);
        
        

        GridLayout gridLayout = new GridLayout(0,3,0,0);
        bodyBox.setLayout(gridLayout);
        bodyBox.setBackground(new Color(0,0,0,180));
        bodyBox.remove(textBox);
        bodyBox.add(powerLabel);
        bodyBox.add(statDivider);
        bodyBox.add(toughnessLabel);     
    }

    public Constants.WeaponType getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(Constants.WeaponType weaponType) {
        this.weaponType = weaponType;
    }

    public int getPower()
    {
        return power + buffedBy;
    }

    public void setPower(int power) 
    {
        this.power = power;
        powerLabel.setText(power+"");
        
    }
    
    public boolean getIsBuffed()
    {
        return isBuffed;
    }

    public void setBuffed(int buff) 
    {
        buffedBy = buffedBy + buff;
        powerLabel.setText(getPower()+"");
        
        if(buffedBy>0)
        {
            isBuffed = true;
            powerLabel.setForeground(Color.ORANGE);
        }
        else
        {
            isBuffed = false;
            powerLabel.setForeground(Color.WHITE);
            
        }
        System.out.println("my power is now " + power + ", and is buffed by " + buffedBy);
    }

    public int getToughness() 
    {
        return toughness;
    }

    public void setToughness(int toughness) 
    {
        this.toughness = toughness;
        toughnessLabel.setText(toughness+"");
    }
    
    public void setLocation(CardLocation l)
    {
        super.setCardLocation(l);
    }
    
    public void setFaceUp(boolean is)
    {
        super.setFaceUp(is);
        powerLabel.setVisible(is);
        toughnessLabel.setVisible(is);
    }

    public void takeDamage(int damage)
    {
        playerHand.getGameWindow().playSound("attackLand");
        if(this.toughness-damage<0)
            this.toughness = 0;
        else
            this.toughness = toughness-damage;
        
        this.toughnessLabel.setText(toughness+"");
        
        if(toughness<=0)
        {
            //if toughness is reduced to 0 or below - it dies
            this.playArea.triggerDeathFffect(this);
            playArea.removeCard(this);
            System.out.println("card - remove card");
        }
        
        toughnessLabel.setForeground(Color.red);
        repaint();
        revalidate();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);      
   
    }
    
    public ActionCard getClone(Image img)
    {
        //this method creates a deep copy of the card and returns it
        ActionCard clone = new ActionCard(getName(),getImageID());
        clone.setImage(img);
        clone.setPlayCost(getPlayCost());
        clone.setPower(power);
        clone.setToughness(toughness);
        clone.setActionEffect(getActionEffect());
        //set picture box
        return clone;
    }
    
    public void setCardValue()
    {
        //determine card value
        /**
        max card value = 
        7 power
        7 toughness
        1 ETB
        1 DE
        = 16
        **/

        int cardValue = power + toughness;
        if(getActionEffect()!= ActionEffect.NONE)
        cardValue++;
        
        int borderStroke = 1;
        this.cardValue = cardValue;
        LineBorder border;
        Color borderColor = Constants.commonColor;
        
        if(getActionEffect()!= ActionEffect.NONE)
            borderColor = Constants.uncommonColor;
        
        if((power+toughness)>Constants.maxResourceAmount-3 && getActionEffect()!= ActionEffect.NONE)
            borderColor = Constants.rareColor;
        
        if((power+toughness)>Constants.maxResourceAmount+3 && getActionEffect()!= ActionEffect.NONE)
            borderColor = Constants.mythicColor;
        
        //don't set border for common cards
        if(borderColor!=Constants.commonColor){
            border = new LineBorder(borderColor,borderStroke);
            innerPanel.setBorder(border);
        }
        
        cardNameLabel.setForeground(borderColor);
    }
    

}
