/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface.Cards;

import Interface.Constants.CardLocation;
import Interface.Constants.SpellEffect;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JLabel;

/**
 *
 * @author chris
 */
public class SpellCard extends Card
{
    private SpellEffect spellEffect;
    private JLabel effectLabel;
    
    
    public SpellCard(String cardName, int imageID) 
    {
        super(cardName, imageID);
        effectLabel = new JLabel();
        this.setFaceUp(false);

    }

    public void setLocation(CardLocation l)
    {
        super.setCardLocation(l);
        revalidate();
    }
        
    public void setSpellEffect(SpellEffect effect)
    {
        this.spellEffect =effect;
        setBodyText(this.spellEffect);
    }
    
    public void setBodyText(SpellEffect effect)
    {
        String text = effect.toString();
        if(effect.toString().contains("_"))
        {
            String partA = effect.toString().split("_")[0];
            String partB = effect.toString().split("_")[1];
            text = partA+" "+playCost+" "+partB;
        }

        
        switch(effect)
        {
            case Draw_cards:
                setBodyText(text);
                return;
                
            case Deal_damage:
                setBodyText(text);
                return;
                
            case Stun:
                setBodyText(text+" a creature");
                return;
        }
    }
    
    public SpellEffect getSpellEffect()
    {
        return spellEffect;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);   
    }
    
    public SpellCard getClone(Image img)
    {
        //this method creates a deep copy of the card and returns it
        SpellCard clone = new SpellCard(getName(), getImageID());
        clone.setPlayCost(getPlayCost());
        clone.setSpellEffect(spellEffect);
        clone.setImage(img);
        clone.setCardID(this.getCardID());
        //set picture box
        return clone;
    }
 
}
