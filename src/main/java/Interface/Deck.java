/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import Database.JSONHelper;
import Interface.Cards.Card;
import Interface.Cards.CreatureCard;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JLayeredPane;
import Interface.Constants.CardLocation;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author chris
 */
public class Deck extends JLayeredPane
{
    
    int width;
    int height;
    PlayerHand playerHand;
    PlayArea playArea;
    Point origin;
    int offset = 0;
    List<Card> cardsInDeck = new ArrayList<Card>(); 
    GameWindow gameWindow;
    boolean isOpponents = false;
    Integer layer = 0;  
    
    public Deck(PlayerHand hand, PlayArea area, GameWindow window, boolean isOpponents)
    {
        gameWindow = window;
        this.isOpponents = isOpponents;
        origin = new Point(2,2);
        playerHand = hand;
        playArea = area;
        height = (int) Math.round(hand.height * 0.8);
        width = height*2;
        //this.setBackground(Color.LIGHT_GRAY);
        this.setOpaque(true);
        this.setSize(new Dimension(width,height));
        
            

    }
    
    public void addCard(Card card)
    { 
        card.applySize(playerHand.getHeight());
        card.setBounds(origin.x+offset,origin.y,card.getWidth()-1,card.getHeight()-1);  
        offset+=1;
        //card.setImage(gameWindow.getImageFromCache(c.getImageID()));
        //card.setCardBack((gameWindow.getImageFromCache(999)));
        
        //***************
        //send message to connected server/client
        if(!isOpponents)
        {
            card.setCardLocation(CardLocation.PLAYER_HAND);
        }
        else
        if(isOpponents)
            card.setCardLocation(CardLocation.OPPONENT_HAND);
            card.setFaceUp(false);
        
        if(card instanceof CreatureCard)
            ((CreatureCard)card).setCardValue();
        
        cardsInDeck.add(card);
        this.add(card,layer);
        layer++;
    }
    
    public void removeCard (Card card)
    {
        cardsInDeck.remove(card);
        this.remove(card);
        layer--;
    }
    
    public void drawCard(boolean sendMessage)
    {
        //sendMessage parameter is so that a message isnt sent on the first deal - so as not to clog up TCP messages
        
        //add new card to play area from the top of the deck
        if(playerHand.addCard(cardsInDeck.get(cardsInDeck.size()-1)))
        {
            removeCard(cardsInDeck.get(cardsInDeck.size()-1)); 
            if(!isOpponents && sendMessage)
            {
                Message m = new Message();
                m.setText("OPPONENT_DRAW_CARD");
                gameWindow.sendMessage(m,null);
            }
        }       
    }
        
    public void populateDeckAndDeal(List<Card> list)
    {
        //add cards to deck
        for(Card c:list)
        {
            
            c.setImage(gameWindow.getImageFromCache(c.getImageID()));
            addCard(c);
        }
        
        //if not opponent
        //send the deck to the opponent
        //and deal out your first hand
        if(!this.isOpponents)
        {

            JSONObject obj = gameWindow.getJsonHelper().getCardListJSON(list);
            gameWindow.sendMessage(new Message("OPPONENTS_DECKLIST"),obj);
        }
        
        //deal out first hand
        playerHand.dealHand();
    }
    
    public void shuffleDeck()
    {
        layer = 0;
        this.removeAll();
        Collections.shuffle(cardsInDeck);
        for(Card c:cardsInDeck)
        {
            add(c,layer);
            layer++;
        }
    }

    
    public List<Card> getCardsInDeck()
    {
        return cardsInDeck;
    } 
}
