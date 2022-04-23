/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import Interface.Cards.*;
import Interface.Constants.CardLocation;
import Interface.Constants.ActionEffect;
import Interface.Constants.TurnPhase;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.Timer;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author chris
 */
public class PlayArea extends JPanel
{
    GameWindow gameWindow;
    WeaponCard weaponInPlay = null;
    boolean isOpponent = false;
    int width;
    int height;
    JPanel playerSubPanel;
    PlayerBox playerBox;
    CardSubPanelWithLanes cardSubPanel;

    private LinkedHashMap<Integer,Card> cardsInPlay = new LinkedHashMap<Integer,Card>();
    private ArrayList<Card> cardList;
    private Deque<CardEvent> cardEventStack = new ArrayDeque<CardEvent>();
    ArrayList<Card> discardPile = new ArrayList<Card>();

 
    public PlayArea(int containerWidth, int containerHeight, GameWindow window, boolean isOpponent)
    {
        gameWindow = window;
        this.isOpponent = isOpponent;
        width = containerWidth;
        this.setOpaque(false);
        //height is the container minus the who player and opponents hands
        height = (int) containerHeight-Math.round((containerHeight/16)*3); 
        this.setPreferredSize(new Dimension(width,height));
        this.setOpaque(false);

        cardSubPanel = new CardSubPanelWithLanes();
        Dimension cardSubPanelDimension = new Dimension(width,Math.round(height/10)*6);
        cardSubPanel.setPreferredSize(cardSubPanelDimension);
        cardSubPanel.setSize(cardSubPanelDimension);
        
        playerSubPanel = new JPanel();
        playerSubPanel.setOpaque(false);
        Dimension playerSubPanelDimension = new Dimension(width,Math.round(height/10)*4);
        playerSubPanel.setPreferredSize(playerSubPanelDimension);
        playerSubPanel.setSize(playerSubPanelDimension);
                
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        if(!isOpponent)
        {
            this.add(cardSubPanel);
            this.add(playerSubPanel);
        }
        else
        {
            this.add(playerSubPanel); 
            this.add(cardSubPanel);
        }   
        
        playerBox = new PlayerBox(playerSubPanel.getHeight(),this.isOpponent, this);
        playerBox.setImage(gameWindow.getImageFromCache(ThreadLocalRandom.current().nextInt(1001,1003)));
        
        playerBox.addMouseListener(new PlayerBoxMouseListener(playerBox,this));
        playerSubPanel.add(playerBox,Component.CENTER_ALIGNMENT);

        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) 
            {
                /**
                 * DISABLED MANUAL DRAW - draw happens automatically at start of turn
                if(gameWindow.getIsPlayerTurn())
                {
                    drawCard();
                }
                **/
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(gameWindow.getIsPlayerTurn())
                    gameWindow.cancelCardEvent();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }
    
    public GameWindow getGameWindow()
    {
        return gameWindow;
    }
    
    public PlayerBox getPlayerBoxPanel()
    {
        return playerBox;
    }

    public boolean addCard(Card card)
    {
        if(card instanceof ActionCard && cardSubPanel.checkIfFull())
            return false;

        gameWindow.playSound("playCard");
        //set card location
        if(isOpponent)
            card.setCardLocation(CardLocation.OPPONENT_PLAY_AREA);
        else
            card.setCardLocation(CardLocation.PLAYER_PLAY_AREA);

        card.setPlayArea(this);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setAlignmentY(Component.CENTER_ALIGNMENT);
        card.setFaceUp(true);

        //Determine which segment of hte play area to add the card to
        //based on card type
        if(card instanceof WeaponCard){
            if(weaponInPlay!=null)
            {
                this.removeCard(weaponInPlay);
            }
            weaponInPlay = (WeaponCard) card;
            playerSubPanel.add(card);
        }
        else if(card instanceof SpellCard)
        {
            playerSubPanel.add(card);
        }
        else{
            cardSubPanel.addCard(card);
        }

        //remove mouse listener assigned when card was added to players hand
        card.removeMouseListener(card.getMouseListeners()[0]);
        //add new mouse listener relevant to the play area
        card.addMouseListener(new CardMouseListener(card,this));

        cardsInPlay.put(card.getCardID(), card);
        triggerETBEffect((ActionCard) card);


        if(card instanceof SpellCard)
        {
            SpellCard scard = (SpellCard) card;

            //do activate on enter the battlefield
            Timer timer = new Timer();
            TimerTask tt = new TimerTask() {
                @Override
                public void run()
                {
                    timer.cancel();
                    selectCard(scard);
                    gameWindow.playSound("playSpell");
                }
            };
            timer.schedule(tt, 500);
        }
        return true;
    }
    
    public void removeCard(Card card)
    {
        playerSubPanel.remove(cardsInPlay.get(card.getCardID()));
        cardSubPanel.removeCard(cardsInPlay.get(card.getCardID()));
        card.setIsActivated(false);
        addToDiscardPile(card);
        cardsInPlay.remove(card.getCardID());
        revalidate();
        repaint();
    }
    
    public void addToDiscardPile(Card card)
    {
        discardPile.add(card);
    }
        
    public void selectCard(Card card)
    {
        gameWindow.playSound("selectCard");
        System.out.println("card selected");
        gameWindow.createCardEvent(card);   
    }

    public void clearCards()
    {
        cardSubPanel.clearLanes();
    }
    
    public HashMap<Integer,Card> getCardsInPlayArea()
    {
        return cardsInPlay;
    }
    
    public void triggerETBEffect(ActionCard card)
    {
        if(card.getActionEffect()==null)
            return;

        //String effectName = card.getETBeffect().toString().split("_")[0];
        switch(card.getActionEffect())
        {
            case Mastercut:
                //this is a passive ability
                //taunt works by the attacking player checking if taunt creature is in play
                //with this classes checkForTauntCreature() method
                //card.showTauntSymbol();
            break;
                
            case Combo:
                gameWindow.playSound("buffSound");

                cardList = new ArrayList<Card>(cardsInPlay.values());
                int buffValue = Math.round(card.getPlayCost()/Constants.buffModifier);
                if(buffValue<1)
                    buffValue = 1;

                for(int x=0; x<cardList.size();x++)
                {       
                    int playedCardIndex = cardList.indexOf(card);
                    Card c = cardList.get(x);
                    //buff only creatures <buff distance> to the left
                    if(x>=(playedCardIndex-Constants.buffDistance) && c.getCardID()!=card.getCardID() && c instanceof ActionCard)
                    {
                        ActionCard ccard = (ActionCard) c;
                        ccard.setBuffed(buffValue);
                        gameWindow.componentAnimateMap.put(c,"slash");
                    }
                }
                gameWindow.drawAnimations();
            break;

            /**
            case Stealth:
                if(isOpponent)
                    card.setFaceUp(false);
            break;
             **/
        }        
    }
    
    public void triggerDeathFffect(ActionCard card)
    {        
        //remove ETB buffs or trigger death effects
        if(card.getActionEffect()!=null)
        {
            //String ETBeffectName = card.getETBeffect().toString().split("_")[0];
            switch(card.getActionEffect())
            {
                case Mastercut:
                break;

                case Combo:
                    gameWindow.playSound("debuffSound");
                    cardList = new ArrayList<Card>(cardsInPlay.values());
                    int buffValue = Math.round(card.getPlayCost()/Constants.buffModifier);
                    if(buffValue<1)
                        buffValue = 1;
                    buffValue = buffValue*-1;


                    for(Card c:cardsInPlay.values())
                    {  
                        int x = cardList.indexOf(c);
                        int indexOfBuffer = cardList.indexOf(card);
                        
                        if(x>=(indexOfBuffer-Constants.buffDistance) &&c.getCardID()!=card.getCardID() && c instanceof ActionCard){
                            ActionCard ccard = (ActionCard) c;
                            if(ccard.getIsBuffed())
                                ccard.setBuffed(buffValue);
                            }
                    }
                break;  
            } 
        }
    }
        
    public class CardMouseListener implements MouseListener
    {
        private Container container;
        private Card card;

        public CardMouseListener(Card card, Container container)
        {
            this.card = card;
            this.container = container;
        }

        @Override
        public void mouseClicked(MouseEvent e){

        }
        @Override
        public void mousePressed(MouseEvent e){
        }
        @Override

        public void mouseReleased(MouseEvent e) {

            System.out.println(card.getWidth() +", " + card.getHeight());

            //only allow mouse events while:
            // its the main phase
            if(e.getButton()==MouseEvent.BUTTON1 && gameWindow.getTurnPhase()==TurnPhase.MAIN_PHASE)
            {
                //if mouse 1 clicked
                //selectCard(card);
            }
            else
            if(e.getButton()==MouseEvent.BUTTON3)
            {
                //if mouse 3 (right button) clicked
                //gameWindow.zoomInCard(card);
                //card.getCardSize();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
        
    }
    
    public class PlayerBoxMouseListener implements MouseListener
    {
        private Container container;
        private PlayerBox playerBox;
        
        public PlayerBoxMouseListener(PlayerBox box, Container container)
        {
            this.playerBox = box;
            this.container = container;
        }
        
        
        @Override
        public void mouseClicked(MouseEvent e) 
        {

        }

        @Override
        public void mousePressed(MouseEvent e) 
        {
        }

        @Override
        public void mouseReleased(MouseEvent e) { 
            //only allow mouse events while its the players
            System.out.println(playerBox.getWidth() + ", " + playerBox.getHeight());
            if(e.getButton()==MouseEvent.BUTTON1)
            {
                if(gameWindow.getIsPlayerTurn())
                {
                    gameWindow.createCardEvent(playerBox);           
                }               
            }
            if(e.getButton()==MouseEvent.BUTTON3)
            {
            }

        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
        
    }

    public boolean checkForAvailableBlockers()
    {
        //returns true if any cards in play area are available to block
        //else returns false
        for(Map.Entry<Integer,Card> entry:cardsInPlay.entrySet())
        {
            if(entry.getValue() instanceof ActionCard)
            {
                ActionCard c = (ActionCard) entry.getValue();
                if(!c.getIsActivated())
                    return true;
                else
                    return false;             
            }      
        }
        return false;        
    } 
    
    public boolean checkForTauntCreature()
    {
        cardList = new ArrayList<Card>(cardsInPlay.values());
        
        for(Card c:cardList)
        {
            //for each creature card player has in play
            //if a creature without taunt is present, mark it as not attackable
            if(c instanceof ActionCard && ((ActionCard)c).getActionEffect()== ActionEffect.Mastercut)
                return true;
        }
        return false;
    }

    public CardSubPanelWithLanes getCardPanel()
    {
        return cardSubPanel;
    }

    public class CardSubPanelWithLanes extends JPanel
    {
        GridLayout gridLayout = new GridLayout(1,3,30,30);
        JPanel innerPanel = new JPanel();
        JPanel[] cardLanes = new JPanel[Constants.numberOfLanes];
        Card[] cardsInPlayArray = new Card[Constants.numberOfLanes];

        private CardSubPanelWithLanes()
        {
            this.setOpaque(false);
            this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
            Dimension cardSubPanelDimension = new Dimension(width,Math.round(height/10)*6);
            this.setPreferredSize(cardSubPanelDimension);
            this.setSize(cardSubPanelDimension);
            innerPanel.setLayout(gridLayout);
            innerPanel.setOpaque(false);
            innerPanel.setBorder(new LineBorder(new Color(000000),10,false));
            innerPanel.setBackground(Constants.shadowColor);
            this.add(innerPanel);

            //set up placeholder panels
            for(int x = 0; x<Constants.numberOfLanes; x++)
            {
                JPanel cardSlot = new JPanel();
                innerPanel.add(cardSlot);
                cardLanes[x] = cardSlot;
                cardSlot.setOpaque(false);
                LanePlaceHolderPanel panel = new LanePlaceHolderPanel(x+1);
                cardLanes[x].add(panel);
            }
        }

        public void clearLanes()
        {
            for(int x = 0; x< cardLanes.length; x++)
            {
                cardLanes[x].removeAll();
                cardsInPlayArray[x] = null;
                LanePlaceHolderPanel panel = new LanePlaceHolderPanel(x+1);
                cardLanes[x].add(panel);
            }
        }

        public boolean checkIfFull()
        {
            int cardsInLanes = 0;
            for(JPanel c: cardLanes){
                if(c.getComponentCount()>0 && c.getComponent(0) instanceof Card)
                    cardsInLanes++;
            }
            if(cardsInLanes==Constants.numberOfLanes)
                return true;
            else
                return false;
        }

        private void addCard(Card card)
        {
            //add the card to the left most free slot
            for(int x = 0; x<Constants.numberOfLanes; x++)
            {
                if(!(cardLanes[x].getComponent(0) instanceof Card)){
                    cardLanes[x].removeAll();
                    cardLanes[x].add(card);
                    cardsInPlayArray[x] = card;
                    return;
                }
            }
        }

        private void removeCard(Card card)
        {
            System.out.println("card sub panel - remove card ("+card.getName()+") cardID: "+card.getCardID());
            for(int x = 0; x< cardLanes.length; x++)
            {
                if(cardLanes[x].getComponentCount()>0 && cardLanes[x].getComponent(0) instanceof Card && ((Card) cardLanes[x].getComponent(0)).getCardID()==card.getCardID())
                {
                    System.out.println("found card to remove");
                    cardLanes[x].removeAll();
                    cardsInPlayArray[x] = null;
                    LanePlaceHolderPanel panel = new LanePlaceHolderPanel(x+1);
                    cardLanes[x].add(panel);
                }
            }
        }

        public Card[] getCards()
        {
            return cardsInPlayArray;
        }

        private class LanePlaceHolderPanel extends JPanel
        {
            int slotNumber;
            public LanePlaceHolderPanel(int slotNumber)
            {
                super();
                this.slotNumber = slotNumber;
                this.add(new JLabel("ACTION " + slotNumber));
                this.setOpaque(false);
            }

            /**
            @Override
            public void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                Graphics2D graphics = (Graphics2D) g;

                graphics.drawString("ACTION "+ slotNumber,this.getWidth()/2,this.getHeight()/2);

                //this.setForeground(Color.black);
                //Color strokeColor = getForeground();

                //graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                //draw shadow
                /**
                if(dropShadow){

                    graphics.setColor(shadowColor);
                    graphics.fillRoundRect(shadowOffset,shadowOffset,width-strokeSize-shadowOffset,height-strokeSize-shadowOffset,arcSize,arcSize);
                }

                //draw inside
                graphics.setColor(backgroundColor);
                graphics.fillRoundRect(0,0,width-shadowGap,height-shadowGap,arcSize,arcSize);

                //draw outline
                graphics.setColor(strokeColor);
                graphics.setStroke(new BasicStroke(strokeSize));
                graphics.drawRoundRect(0,0,width-shadowGap,height-shadowGap,arcSize,arcSize);

            }
            **/

        }

    }
    
    

    
    
    
    
}
