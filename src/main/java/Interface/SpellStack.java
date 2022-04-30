/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import Interface.Cards.ActionCard;
import Interface.Cards.Card;
import Interface.Cards.SpellCard;
import Interface.Constants.CardLocation;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author chris
 */
public class SpellStack extends JLayeredPane
{

    int width;
    int height;
    PlayerHand playerHand;
    Point origin;
    int offset = 0;
    List<CardEvent> cardsInStack = new ArrayList<CardEvent>();
    GameWindow gameWindow;
    Integer layer = 1;

    public SpellStack(GameWindow window,Component container)
    {
        gameWindow = window;
        origin = new Point(2,2);
        height = (int) Math.round(container.getHeight() * 0.8);
        width = height*2;
        //this.setBackground(Color.YELLOW);
        //this.setOpaque(true);
        this.setSize(new Dimension(width,height));
    }
    
    public void addCardEvent(CardEvent cardEvent)
    {
        System.out.println("SpellStack.addCardEvent()");
        /**
         * ISSUE!!
         * The first card added to the stack remotely is not visible
         * some issue exists rendering the first card received via message
         *      possible reasons?:
         *
         *
         *
         */

        SpellCard card = (SpellCard) cardEvent.getOriginCard();
        card.setVisible(true);
        card.setBounds(origin.x,origin.y+offset,card.getWidth(),card.getHeight());
        this.add(card,layer);
        offset+= card.getHeight()/2;
        cardsInStack.add(cardEvent);
        layer++;
    }
    
    public void removeCardEvent (CardEvent cardEvent)
    {
        Card card = cardEvent.getOriginCard();
        cardsInStack.remove(cardEvent);
        this.remove(card);
        layer--;
        revalidate();
        repaint();
    }

    public List<CardEvent> getCardsInStack()
    {
        return cardsInStack;
    } 
}
