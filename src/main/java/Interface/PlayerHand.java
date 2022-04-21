/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import Interface.Cards.Card;
import Interface.Cards.CreatureCard;
import Interface.Constants.CardLocation;
import Interface.Constants.TurnPhase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author chris
 */
public class PlayerHand extends JLayeredPane
{
    boolean isOpponents = false;
    int layers = 0;
    int width;
    int height;
    int maxHandSize = Constants.maxHandSize;
    PlayArea playArea;
    Deck deck;
    //this is the origin of hte first card added
    Point origin;
    GameWindow gameWindow;
    ResourcePanel resourcePanel;
    private int discardTimeLimit = 10;
    private int cardOverlap=0;
    private LinkedHashMap<Integer,Card> cardsInHandMap = new LinkedHashMap<Integer,Card>();
    
    public PlayerHand(int containerWidth, int containerHeight, PlayArea area, boolean isOpponents, GameWindow window, ResourcePanel panel)
    {
        resourcePanel = panel;
        gameWindow = window;
        this.isOpponents = isOpponents;
        playArea = area;
        width = containerWidth;
        height = (int) Math.round((containerHeight/16)*5); 
        this.setPreferredSize(new Dimension(width, height));
        this.setOpaque(true);
        this.setBackground(Color.DARK_GRAY);
    }
    
    public boolean addCard(Card card)
    {
        if(!cardsInHandMap.containsKey(card.getCardID()))
        {
            gameWindow.playSound("addCard");
            card.setPlayerHand(this);
            card.applySize(height);
            card.setAlignmentX(Component.CENTER_ALIGNMENT);
            //set the position of the cards added
            card.setBounds(origin.x, origin.y,card.getWidth(),card.getHeight());
            if(!isOpponents)
                card.setFaceUp(true);
            cardsInHandMap.put(card.getCardID(),card);
            this.add(card,layers);
            layers++;
            resizeHand();
            card.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e){
                }

                @Override
                public void mousePressed(MouseEvent e) {}

                @Override
                public void mouseReleased(MouseEvent e) 
                {
                    if(e.getButton()==MouseEvent.BUTTON1)
                    {
                        if(gameWindow.getIsPlayerTurn() && card.getCardLocation()==CardLocation.PLAYER_HAND)
                        {
                            if(gameWindow.getTurnPhase()==TurnPhase.END_PHASE)
                            {
                                discardCard(card.getCardID());
                                if(checkHandSizeForEndTurn())
                                    gameWindow.passTurn();     
                            }
                            else
                                playCard(card.getCardID(),false);
                        }
                    }
                    if(e.getButton()==MouseEvent.BUTTON3)
                    {
                        
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseExited(MouseEvent e) {}
            });
            highlightPlayableCards();
            return true;
        }
        return false;
    }
    
    public List<Card> getCardsInHand()
    {
        return cardsInHandMap.values().stream().toList();
        //return cardsInHand;
    }

    
    public void discardCard(int cardID)
    {
        Card discard = cardsInHandMap.get(cardID);
        gameWindow.playSound("discardCard");
        playArea.addToDiscardPile(discard);
        System.out.println("discard card - " + discard.getCardID());
            
            if(!isOpponents)
            {
                Message message = new Message();
                message.setText("PLAYER_DISCARD_CARD");
                gameWindow.sendMessage(message,gameWindow.getJsonHelper().convertCardToJSON(discard));
            }
        //remove card after sending hte message
        //otherwise theres no reference to the card anymore
        removeCard(discard);
    }
    
    public void removeCard(Card card)
    {
        card.setIsPlayable(false);
        cardsInHandMap.remove(card.getCardID());
        this.remove(card);
        resizeHand();
        layers--;;
        highlightPlayableCards();
    }
    
    public void setDeckArea(Deck deck)
    {
        this.deck = deck;
        int spacing = Math.round((height-deck.getHeight())/2);
        deck.setBounds(spacing,spacing,deck.getWidth(),deck.getHeight());
        //set the points where cards are added relative to the deck area
        origin = new Point(deck.getWidth()+(spacing*2), deck.getY());
        this.add(deck,0);
    }
    
    public void resizeHand()
    {     
        origin.x = (deck.getWidth()+(Math.round((height-deck.getHeight())/2)*2));
        
        //@index parameter is the hard that was taken from the hand
        cardsInHandMap.forEach((cardID,card)->{
            this.remove(card);
            layers--;
            card.setBounds(origin.x, origin.y,card.getWidth(),card.getHeight());
            this.add(card,layers);
            origin.x += card.getWidth()-cardOverlap;
            layers++;
            repaint();
            revalidate();
        });
    }
    
    public void highlightPlayableCards()
    {
        cardsInHandMap.forEach((cardID,card)->{
            if(card.getPlayCost()<=resourcePanel.getAmount() && gameWindow.getIsPlayerTurn())
                card.setIsPlayable(true);
            else if(card.getPlayCost()>resourcePanel.getAmount() | !gameWindow.getIsPlayerTurn() )
                card.setIsPlayable(false);
        });
    }
    
    public void playCard(int cardID, boolean isOpponent)
    {
        Card card = cardsInHandMap.get(cardID);
        
        //if the played card is a creature
        //and the maxmimum num of cards are in play
        //dont allow the card to be played
        if(card instanceof CreatureCard && playArea.getNumCardsInPlayArea()==Constants.maxCaradsInPlayArea){
            gameWindow.playSound("playAreaFull");
            return;
        }
        
        //if the cost of hte card exceeds available resources - exit method
        if(card.getPlayCost()>resourcePanel.getAmount())
            return;

        if(playArea.addCard(card))
        {
            this.removeCard(card);
            card.removeFromPlayerHand();

            if(!isOpponent)
                card.setCardLocation(CardLocation.PLAYER_HAND);
            else
                card.setCardLocation(CardLocation.OPPONENT_HAND);

            resourcePanel.useResources(card.getPlayCost());
            this.gameWindow.getGameControlPanel().increaseTime();

            //***************
            //send message to connected server/client
            if(!isOpponents)
            {
                Message message = new Message();
                message.setText("OPPONENT_PLAY_CARD");
                gameWindow.sendMessage(message,gameWindow.getJsonHelper().convertCardToJSON(card));
            }
        }
    }
    
    public Deck getDeck()
    {
        return deck;
    }
        
    public void dealHand()
    {
        for(int x=0;x<maxHandSize;x++)
            deck.drawCard(false);
    }
    
    public void drawCards(int num)
    {
        //draw cards * the number passed as parameter
        for(int x=0;x<num;x++)
            this.deck.drawCard(true);
    }
    
    public int getNumCards()
    {
        return cardsInHandMap.size();
    }
    
    public int getMaxHandSize()
    {
        return maxHandSize;
    }
    
    public boolean checkHandSizeForEndTurn()
    {
        gameWindow.setTurnPhase(TurnPhase.END_PHASE);
        int numCardsOver = cardsInHandMap.size()-maxHandSize;
  
        if(numCardsOver>0)
        {
            gameWindow.getGameControlPanel().setNotificationLabel("Discard " + numCardsOver + " cards");
            return false;
        }
        else
        {
            gameWindow.getGameControlPanel().setNotificationLabel("");    
            return true;
        }
    }
    
    public GameWindow getGameWindow()
    {
        return gameWindow;
    }
    
    
    
}

    
    
    
    
    
    

