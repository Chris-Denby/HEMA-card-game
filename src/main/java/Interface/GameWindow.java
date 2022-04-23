/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import Database.JSONHelper;
import Interface.Cards.Card;
import Interface.Cards.ActionCard;
import Interface.Cards.PlayerBox;
import Interface.Cards.SpellCard;
import NetCode.TCPClient;
import NetCode.TCPServer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;

import Interface.Constants.CardLocation;
import Interface.Constants.ActionEffect;
import Interface.Constants.SpellEffect;
import Interface.Constants.TurnPhase;
import Interface.Constants.ActionEffect;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.json.simple.JSONObject;


/**
 *
 * @author chris
 */
public class GameWindow extends JPanel
{
    private JTabbedPane parentTabbedPane;
    private TCPServer netServer = null;
    private TCPClient netClient = null;
    private PlayArea opponentsPlayArea;
    private PlayArea playerPlayArea;
    private PlayerHand playerHand;
    private PlayerHand opponentsHand;
    private Deck playerDeck;
    private Deck opponentsDeck;
    private GameControlPanel gameControlPanel;
    private ResourcePanel playerResourcePanel;
    private ResourcePanel opponentsResourcePanel;
    private CentrePanel centrePanel;
    private JRootPane rootPane;
    private DrawLineGlassPane drawLineGlassPane;
    private CardZoomGlassPane cardZoomGlassPane;
    private EndGameGlassPane endGameGlassPane;
    private boolean isPlayerTurn = false;
    private int turnPasses = 0;
    private int turnNumber = 1;
    private TurnPhase turnPhase = null;
    private int turnCycleIncrementor = 0;
    private Color overlayColor = new Color(223,223,223,200);
    private StartGameWindow startGameWindow;
    private Timer combatTimer;
    private JSONHelper jsonHelper;
    private Clip musicClip;
    private Clip ambientSoundClip;
    private AnimationGlassPane animationGlassPane;
    public Map<Component,String> componentAnimateMap = new HashMap<Component,String>();

    //private Deque<CardEvent> actionCardStack = new ArrayDeque<CardEvent>();
    private ArrayList<CardEvent> actionCardStack = new ArrayList<CardEvent>();
    private CardEvent cardEvent = null;
    
    //constructor
    public GameWindow(JTabbedPane pane, StartGameWindow startGameWindow)
    {
        combatTimer = new Timer();
        this.startGameWindow = startGameWindow;
        parentTabbedPane = pane;
        BorderLayout borderLayout = new BorderLayout();
        //SET JFRAME PARAMETERS
        int width = 400;
        int height = 400;
        Dimension dimensions = new Dimension(width,height);
        this.setSize(width, height);
        this.setMinimumSize(dimensions);
        this.setLayout(borderLayout);
        jsonHelper = new JSONHelper();
        
                
        //INITIALISE COMPONENTS
        playerResourcePanel = new ResourcePanel(getWidth(),getHeight(),this);
        //playerResourcePanel.setOpaque(true);
        playerResourcePanel.setPreferredSize(new Dimension(getWidth(),Math.round(getHeight()/10)));
        opponentsResourcePanel = new ResourcePanel(getWidth(),getHeight(),this);
        //opponentsResourcePanel.setOpaque(true);
        opponentsResourcePanel.setPreferredSize(new Dimension(getWidth(),Math.round(getHeight()/10)));


        playerPlayArea = new PlayArea(getWidth(),getHeight(),this, false);
        opponentsPlayArea = new PlayArea(getWidth(),getHeight(),this, true);
        playerHand = new PlayerHand(getWidth(),getHeight(), playerPlayArea, false,this,playerResourcePanel);
        opponentsHand = new PlayerHand(getWidth(),getHeight(), opponentsPlayArea, true,this,opponentsResourcePanel);
        opponentsHand.setEnabled(false);
        playerDeck = new Deck(playerHand, playerPlayArea, this,false); 
        opponentsDeck = new Deck(opponentsHand, opponentsPlayArea, this,true);  
        opponentsDeck.setEnabled(false);
        gameControlPanel = new GameControlPanel(this.getHeight(), this.getWidth(),this);

        //ADD COMPONENTS        
        this.add(opponentsHand, BorderLayout.PAGE_START);
        
        centrePanel = new CentrePanel();
        centrePanel.setOpaque(false);
        centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.PAGE_AXIS));
        centrePanel.add(opponentsResourcePanel);
        centrePanel.add(opponentsPlayArea, BorderLayout.CENTER);
        centrePanel.add(playerPlayArea);
        centrePanel.add(playerResourcePanel);
        
        JScrollPane scrollPane = new JScrollPane(playerHand);

        
        this.add(gameControlPanel, BorderLayout.WEST);
        this.add(centrePanel, BorderLayout.CENTER);
        this.add(scrollPane, BorderLayout.PAGE_END);
        
        playerHand.setDeckArea(playerDeck);    
        opponentsHand.setDeckArea(opponentsDeck);
        //set decks disabled until after each player is dealt
        playerDeck.setEnabled(false);
        opponentsDeck.setEnabled(false);
        //MAKE THE JFRAME VISIBLE
        setVisible(true); 
        
        playAmbientSound();
        playMusic();
        centrePanel.setImage(getImageFromCache(000));

        Timer timer = new Timer();

        TimerTask beginGameTask = new TimerTask() {
            @Override
            public void run()
            {
                beingGame();
            }
        };
        TimerTask dealTask = new TimerTask() {
            @Override
            public void run() 
            {    
                //populate players deck
                playerDeck.populateDeckAndDeal(jsonHelper.createCardLists());
                timer.schedule(beginGameTask, 1500);
            }
        };
        timer.schedule(dealTask, 1500);
    }
    
    public GameControlPanel getGameControlPanel()
    {
        return gameControlPanel;
    }

    public JSONHelper getJsonHelper() {
        return jsonHelper;
    }

    public void beingGame()
    {
        while(true)
        {
            if(opponentsHand.getCardsInHand().size()==Constants.maxHandSize && playerHand.getCardsInHand().size()==Constants.maxHandSize)
                break;
        }

        if(isNetServer())
        {
            //if application is the server - begin turn
            passTurn();
            Message message = new Message();
            message.setText("OPPONENT_PASS_TURN");
            sendMessage(message,null);
        }
    }

    public boolean isNetServer()
    {
        if(netServer==null)
            return false;
        else
            return true;
    }

    public void createCardEvent(Card card)
    {        
        //@@ Parameter originCarnd - the player hand which fired the method
        
        //if card is clicked - create a new event for that card
        //then if another card is clicked as a target - trigger the source cards event on that card
        //if the cost of the selected card is greater than the available resources - exit the method       

        //select a card as event source only if it is not yet selected
        //dont allow origin card to be selected from opponents hand
        if(cardEvent == null && !card.getIsSelected() && !card.getIsActivated())
        {
            //if origin card is in opponents play area, do nothing
            if(this.isPlayerTurn & card.getCardLocation()==CardLocation.OPPONENT_PLAY_AREA)
                return;

            cardEvent = new CardEvent(card);
            //activate cards in the play areas
            card.setIsSelected(true);
            
            if(card instanceof SpellCard)
            {
                SpellCard scard = (SpellCard) card;

                if(scard.getSpellEffect()==SpellEffect.Draw_cards)
                {
                    //if a draw card spell, set self as the target and execute immediately
                    cardEvent.addTargetPlayerBox(playerPlayArea.getPlayerBoxPanel());
                    executeCardEvent();
                }
                else
                if(scard.getSpellEffect()==SpellEffect.Deal_damage)
                {
                    //let method continue to allow a second card, or player, to be targeted
                }
            }

            //***************
            //send message to connected server/client
            if(isPlayerTurn)
            {
                Message message = new Message();
                message.setText("OPPONENT_ACTIVATE_CARD");
                sendMessage(message,jsonHelper.convertCardToJSON(card));
            }
        }
        else
        //if a card event exists and target card has not yet been set
        if(cardEvent != null && cardEvent.getTargetCard()==null && cardEvent.getTargetPlayerBox()==null && turnPhase!=TurnPhase.DECLARE_BLOCKERS)
        {
            //set the target card location if its the opponents turn
            //because location information is lost when sending over the stream
            if(getPlayerLocalCard(card.getCardID())==null)
                card.setCardLocation(CardLocation.OPPONENT_PLAY_AREA);
            else
                card.setCardLocation(CardLocation.PLAYER_PLAY_AREA);

            //if target card is the same as the origin - abandon method
            if(cardEvent.getOriginCard().getCardID()==card.getCardID())
                return;

            //if the target card is a spell - abandon method
            if(card instanceof SpellCard)
                return;

            if(cardEvent.getOriginCard() instanceof ActionCard && card instanceof ActionCard)
            {
                //>> PREVENT IF TAUNT PREVENTS ATTACK
                //if its the players turn
                //and the creature being attacked doesnt have taunt
                //and there is a taunt creature in play
                //RETURN!!

                if(this.isPlayerTurn
                        && ((ActionCard) card).getActionEffect()!= ActionEffect.Mastercut
                        && opponentsPlayArea.checkForTauntCreature()){
                    return;
                }
            }

            //add card event to the stack
            //cardEventStack.addFirst((CardEvent)cardEvent);

            //set selected card as the target card
            cardEvent.addTargetCard(card);
            cardEvent.getTargetCard().setIsSelected(true);

            //show glass pane so the arrow can be drawn
            drawPointer(cardEvent.getOriginCard(), cardEvent.getTargetCard());

            executeCardEvent();

            //***************
            //send message to connected server/client
            if(isPlayerTurn)
            {
                Message message = new Message();
                message.setText("OPPONENT_ACTIVATE_CARD");
                sendMessage(message,jsonHelper.convertCardToJSON(card));
            }
        }
        //if turn phase is delcare blockers
        //the card added is to be the blocker
    }

    public void createCardEvent(PlayerBox playerBox)
    {
        //if the event already created
        //if a target card has not been set in the event
        //if a target player has not been set in th event

        if(cardEvent != null && cardEvent.getTargetCard()==null & cardEvent.getTargetPlayerBox()==null)
        {
            //exit method if targeting own player
            if(this.isPlayerTurn && !playerBox.getIsOpponent())
                return;

            //exit method if a taunt creature is present
            if(this.isPlayerTurn && opponentsPlayArea.checkForTauntCreature())
                return;

            //exit method if a stun spell is being played on a player
            if(cardEvent.getOriginCard() instanceof SpellCard && ((SpellCard)cardEvent.getOriginCard()).getSpellEffect()==SpellEffect.Stun)
                return;

            playerBox.setIsSelected(true);
            cardEvent.addTargetPlayerBox(playerBox);

            //add card event to the stack
            //cardEventStack.addFirst((CardEvent)cardEvent);

            //show glass pane so the arrow can be drawn
            drawPointer(cardEvent.getOriginCard(),cardEvent.getTargetPlayerBox());

            //if its the opponent who recieved the card event targeting their player box
            //and they have no available blockers
            //skip blocking without their input


            //if origin is a spell, execute
            if(cardEvent.getOriginCard() instanceof SpellCard)
            {
                executeCardEvent();
            }
            //if the origin is a creature
            else if(cardEvent.getOriginCard() instanceof ActionCard)
            {
                //and it isnt the players turn
                //allow opponent to block
                requestResolveCombat();
                if(!isPlayerTurn)
                    //and there is no creatures available to block
                    if(!playerPlayArea.checkForAvailableBlockers())
                        passOnBlocking();
            }





            //***************
            //send message to connected server/client
            if(isPlayerTurn)
            {
                Message message = new Message();

                if(playerBox.getIsOpponent())
                    message.setText("CARD_EVENT_ON_OPPONENT");
                else
                    message.setText("CARD_EVENT_ON_PLAYER");
                sendMessage(message,null);
            }
        }
    }

    public void cancelCardEvent()
    {   if(cardEvent!=null)
        {
            Card originCard = cardEvent.getOriginCard();
            Card targetCard = cardEvent.getTargetCard();
            PlayerBox targetPlayer = cardEvent.getTargetPlayerBox();

            //if target card objects dont match whats in players hand due to sending over stream
            //match by ID instead
            if(targetCard!=null && getPlayerLocalCard(targetCard.getCardID())!=null)
            {
                targetCard = getPlayerLocalCard(targetCard.getCardID());
            }

            //unselect any selected cards or opponent
            originCard.setIsSelected(false);
            if(targetPlayer!=null)
                targetPlayer.setIsSelected(false);
            if(targetCard!=null)
                targetCard.setIsSelected(false);



            //if spell is pending a target, remove the spell from play
            if(originCard instanceof SpellCard && targetCard==null)
            {
                System.out.println("removing myself from play area");
                originCard.removeFromPlayArea();
            }



            cardEvent = null;

            hideDrawLineGlassPane();


            drawLineGlassPane=null;

            if(isPlayerTurn)
            {
                Message message = new Message();
                message.setText("CANCEL_CARD_EVENT");
                sendMessage(message,null);
            }
        }
    }

    public Card getPlayerLocalCard(int id)
    {
        if(playerPlayArea.getCardsInPlayArea().containsKey(id))
            return playerPlayArea.getCardsInPlayArea().get(id);
        else
            return null;
    }

    public Card getOpponentLocalCard(int id)
    {
        if(opponentsPlayArea.getCardsInPlayArea().containsKey(id))
            return opponentsPlayArea.getCardsInPlayArea().get(id);
        else
            return null;
    }

    public void requestResolveCombat()
    {
        if(cardEvent.getOriginCard() instanceof ActionCard && cardEvent.getTargetCard() instanceof ActionCard)
            executeCardEvent();
        else
        if(cardEvent.getOriginCard() instanceof ActionCard && cardEvent.getTargetPlayerBox()!=null)
        {
            if(!isPlayerTurn)
            {
                gameControlPanel.enableResolveButton(true);
                gameControlPanel.setResolveButtonText("NO BLOCKERS");
            }
        }
        //***************
        //send message to connected server/client
        if(isPlayerTurn)
        {
            Message message = new Message();
            message.setText("REQUEST_RESOLVE_COMBAT");
            sendMessage(message,null);
        }
    }

    public void executeCardEvent(CardEvent event)
    {
        TimerTask combatTask = null;
        Timer timer = new Timer();

        Card originCard = event.getOriginCard();
        Card targetCard = event.getTargetCard();
        PlayerBox targetPlayer = event.getTargetPlayerBox();

        //force the cards to be face up, if it is face down for some reason
        //such as Stealth effect...
        originCard.setFaceUp(true);
        if(targetCard!=null)
            targetCard.setFaceUp(true);

        //this.gameControlPanel.increaseTime();

        //if target card objects dont match whats in players hand due to sending over stream
        //match by ID instead
        if(targetCard!=null && getPlayerLocalCard(targetCard.getCardID())!=null)
            targetCard = getPlayerLocalCard(targetCard.getCardID());

        if(event.getOriginCard() instanceof SpellCard)
        {
            //do action
            SpellCard spellCard = (SpellCard) event.getOriginCard();

            if(spellCard.getSpellEffect()==SpellEffect.Draw_cards)
            {
                combatTask = new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        hideDrawLineGlassPane();


                        componentAnimateMap.put(originCard,"slash");
                        drawAnimations();

                        playSound("drawCardSpell");
                        if(isPlayerTurn)
                            playerHand.drawCards(spellCard.getPlayCost());

                        originCard.removeFromPlayArea();
                        //release current card event
                        cardEvent = null;
                    }
                };
            }
            else
            if(spellCard.getSpellEffect()==SpellEffect.Deal_damage)
            {
                combatTask = new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        playSound("fireball");

                        componentAnimateMap.put(originCard,"slash");
                        drawAnimations();

                        if(cardEvent.getTargetCard() instanceof ActionCard)
                        {
                            ActionCard ccard;
                            if(getPlayerLocalCard(cardEvent.getTargetCard().getCardID())!=null){
                                ccard = (ActionCard) getPlayerLocalCard(cardEvent.getTargetCard().getCardID());
                            }
                            else{
                                ccard = (ActionCard) cardEvent.getTargetCard();
                            }
                            ccard.takeDamage(cardEvent.getOriginCard().getPlayCost());
                            componentAnimateMap.put(ccard,"explode");
                            drawAnimations();
                        }
                        if(cardEvent.getTargetPlayerBox()!=null)
                        {
                            cardEvent.getTargetPlayerBox().takeDamage(cardEvent.getOriginCard().getPlayCost());
                            componentAnimateMap.put(cardEvent.getTargetPlayerBox(),"explode");
                            drawAnimations();
                        }


                        event.getOriginCard().removeFromPlayArea();
                        //release current card event
                        cardEvent = null;
                        //hideDrawLineGlassPane();
                    }
                };
            }
            else
            if(spellCard.getSpellEffect()==SpellEffect.Stun)
            {
                combatTask = new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        playSound("stun");

                        componentAnimateMap.put(originCard,"slash");
                        drawAnimations();

                        if(cardEvent.getTargetCard() instanceof ActionCard)
                        {
                            //set targets
                            ActionCard ccard;
                            if(getPlayerLocalCard(cardEvent.getTargetCard().getCardID())!=null){
                                ccard = (ActionCard) getPlayerLocalCard(cardEvent.getTargetCard().getCardID());
                            }
                            else{
                                ccard = (ActionCard) cardEvent.getTargetCard();
                            }

                            //execute effect
                            ((Card)ccard).setIsActivated(true);

                            //draw animation
                            //drawAnimation("stun",null,ccard);
                        }

                        //remove spent spell card from play area
                        event.getOriginCard().removeFromPlayArea();
                        //release current card event
                        cardEvent = null;
                        hideDrawLineGlassPane();
                    }
                };
            }

            if(combatTask!=null)
                timer.schedule(combatTask, 500);
        }

        //AFTER EVENT RESOLVED
        //return card state to normal
        event.execute();

        if(originCard!=null)
        {
            originCard.setIsSelected(false);
            originCard.setIsActivated(true);
        }
        if(targetCard!=null)
        {
            targetCard.setIsSelected(false);
            targetCard.setIsActivated(true);
        }
        if(targetPlayer!=null)
        {
            targetPlayer.setIsSelected(false);
        }
    }

    public void addCardToActionCardStack(ActionCard card, int lane){
        CardEvent event = new CardEvent(card);
        event.setLaneNumber(lane);
        actionCardStack.add(event);
    }


    int combatIterator = 0;
    public void executeLaneCombat()
    {
        //reset the iterator back to 0 - otherwise timer is cancelled
        combatIterator = 0;
        //Schedules the specified task for repeated fixed-delay execution (period), beginning after the specified delay (delay)
        combatTimer = new Timer();
        TimerTask laneTimerTask = new TimerTask() {
            @Override
            public void run()
            {
                System.out.println("timer entered");
                //cancel timer after iterating through all lanes
                int x = combatIterator;
                if(combatIterator==Constants.numberOfLanes) {
                    combatTimer.cancel();
                    combatTimer.purge();
                    return;
                }

                ActionCard[] playerActionCardArray = new ActionCard[2];
                ActionCard[] opponentActionCardArray = new ActionCard[2];
                PlayerBox[] playerBoxArray = new PlayerBox[2];
                playerActionCardArray[0] = (ActionCard) playerPlayArea.getCardPanel().getCards()[x];
                playerActionCardArray[1] = (ActionCard) opponentsPlayArea.getCardPanel().getCards()[x];
                opponentActionCardArray[0] = (ActionCard) opponentsPlayArea.getCardPanel().getCards()[x];
                opponentActionCardArray[1] = (ActionCard) playerPlayArea.getCardPanel().getCards()[x];
                playerBoxArray[0] = opponentsPlayArea.getPlayerBoxPanel();
                playerBoxArray[1] = playerPlayArea.getPlayerBoxPanel();

                System.out.println("lane " +x + " --------");

                for(int y=0;y<2;y++)
                {
                    System.out.println("    slot "+y);
                    //determine which index is opponent or player
                    PlayerBox opponentsPlayerBox = playerBoxArray[y];
                    ActionCard playerActionCard = playerActionCardArray[y];
                    ActionCard opponentActionCard = opponentActionCardArray[y];

                    // INSERT CODE TO DETERMINE IF THIS CARD ACTION IS BLOCKED HERE!!!
                    boolean isBlocked = false;
                    if(playerActionCard!=null && (
                        playerActionCard.getActionEffect()==ActionEffect.Mastercut ||
                        playerActionCard.getActionEffect()==ActionEffect.Cut ||
                        playerActionCard.getActionEffect()==ActionEffect.Thrust
                            ))
                    {
                        if(opponentActionCard!=null)
                        {
                            System.out.println(opponentActionCard.getPower());

                            if(playerActionCard.getActionEffect()==ActionEffect.Cut && opponentActionCard.getActionEffect()==ActionEffect.Parry_Cut){
                                isBlocked = true;
                                System.out.println("        Cut was blocked");
                                //add blocked animation
                            }
                            else
                            if(playerActionCard.getActionEffect()==ActionEffect.Thrust && opponentActionCard.getActionEffect()==ActionEffect.Parry_Thrust){
                                isBlocked = true;
                                System.out.println("        Thrust was blocked");
                                //add blocked animation
                            }
                            else
                            if(opponentActionCard.getActionEffect()==ActionEffect.Mastercut){
                                isBlocked = true;
                                System.out.println("        Blocked by mastercut");
                                //add blocked animation
                            }
                        }
                        if(!isBlocked)
                        {
                            //if player slot is not empty
                            //and the card is a cut of thrust
                            //execute the card
                            playSound("attackSwing");
                            playerActionCard.setIsActivated(true);
                            opponentsPlayerBox.takeDamage(playerActionCard.getPower());
                            componentAnimateMap.put(opponentsPlayerBox,"slash");
                            System.out.println("        card damage from " + playerActionCard.getPower());
                        }
                        else
                            System.out.println("        Blocked!");

                    }
                }
                drawAnimations();
                combatIterator++;
            }
        };
        combatTimer.schedule(laneTimerTask,Constants.laneCombatDelay,Constants.laneCombatDelay);
    }

    public void hideDrawLineGlassPane()
    {
        if(drawLineGlassPane!=null)
        {
            drawLineGlassPane.setVisible(false);
            drawLineGlassPane = null;
        }
    }

    public void executeCardEvent()
    {
        //if no parameters given
        //execute card event in memory
        if(cardEvent!=null)
        executeCardEvent(cardEvent);
    }

    public boolean getIsPlayerTurn()
    {
        return isPlayerTurn;
    }

    public TurnPhase getTurnPhase()
    {
        return turnPhase;
    }

    public void passTurn()
    {
        playSound("passTurn");
        gameControlPanel.startTurnTimer();
        gameControlPanel.setNotificationLabel("");

        //cancel any half created events
        if(cardEvent!=null)
            cancelCardEvent();

        //progress to next turn phase
        if(getTurnPhase()==null) {
            setTurnPhase(TurnPhase.MAIN_PHASE);
            //replenish resources back to turn amount
            playerResourcePanel.resetResources();
            opponentsResourcePanel.resetResources();
            playerHand.highlightPlayableCards();
        }
        else if(getTurnPhase()==TurnPhase.MAIN_PHASE) {
            setTurnPhase(TurnPhase.COMBAT_PHASE);
            playerHand.unHighlightPlayableCards();
            //executeActionCardStack();
            executeLaneCombat();
        }
        else if (getTurnPhase()==TurnPhase.COMBAT_PHASE) {
            setTurnPhase(TurnPhase.END_PHASE);
            playerPlayArea.clearCards();
            opponentsPlayArea.clearCards();
            playerHand.discardAllCards();
            opponentsHand.discardAllCards();
            playerHand.dealHand();
            opponentsHand.dealHand();
        }
        else if (getTurnPhase()==TurnPhase.END_PHASE) {
            setTurnPhase(TurnPhase.MAIN_PHASE);
            //replenish resources back to turn amount
            playerResourcePanel.resetResources();
            opponentsResourcePanel.resetResources();
            playerHand.highlightPlayableCards();
        }

        gameControlPanel.setTurnPhaseLabelText(turnPhase);
    }

    public void passOnBlocking()
    {
        executeCardEvent();

        Message message = new Message();
        message.setText("OPPONENT_PASS_ON_BLOCKING");
        sendMessage(message,null);
    }

    public void setOpponentInteractable(boolean enabled)
    {
        opponentsHand.setEnabled(enabled);
        opponentsPlayArea.setEnabled(enabled);
        opponentsDeck.setEnabled(enabled);
    }

    public GameWindow(JTabbedPane pane,TCPServer server, StartGameWindow startGamePanel)
    {
        this(pane, startGamePanel);
        netServer = server;
        server.setGameWindow(this);
        //setOpponentInteractable(false);
    }

    public PlayerHand getPlayerHand()
    {
        return playerHand;
    }

    public PlayerHand getOpponentHand()
    {
        return opponentsHand;
    }

    public Image getImageFromCache(int id)
    {
        return startGameWindow.getImageFromCache(id);
    }

    public GameWindow(JTabbedPane pane,TCPClient client, StartGameWindow startGamePanel)
    {
        this(pane, startGamePanel);
        netClient = client;
        client.setGameWindow(this);
    }

    public void sendMessage(Message message, JSONObject o)
    {
        if(o!=null){
            message.setJsonCard(o);
        }

        if(netServer!=null)
        {
            netServer.sendMessage(message);
        }
        else if (netClient!=null)
        {
            netClient.sendMessage(message);
        }
    }

    public void receiveMessage(Message message)
    {
        Card messageCard = null;
        if(message.getJsonCard()!=null && !message.getText().equals("OPPONENTS_DECKLIST"))
        {
            //convert from JSON to card
            messageCard = this.jsonHelper.convertJSONtoCard(message.getJsonCard());

            if(this.getPlayerLocalCard(messageCard.getCardID())!=null){
                messageCard = this.getPlayerLocalCard(messageCard.getCardID());
                messageCard.setPlayArea(playerPlayArea);
                messageCard.setPlayerHand(playerHand);

            }
            else
            if(this.getOpponentLocalCard(messageCard.getCardID())!=null){
                messageCard = this.getOpponentLocalCard(messageCard.getCardID());
                messageCard.setPlayArea(opponentsPlayArea);
                messageCard.setPlayerHand(opponentsHand);
            }
        }
        if(message.getText().equals("OPPONENTS_DECKLIST"))
        {
            this.opponentsDeck.populateDeckAndDeal(jsonHelper.readCardListJSON(message.getJsonCard()));
        }
        else
        if(message.getText().equals("OPPONENT_DRAW_CARD"))
        {
            opponentsDeck.drawCard(true);
        }
        else
        if(message.getText().equals("OPPONENT_ADD_CARD_TO_DECK"))
        {
            opponentsDeck.addCard(messageCard);
        }
        else
        if(message.getText().equals("OPPONENT_PLAY_CARD"))
        {
            this.opponentsHand.playCard(messageCard.getCardID(),true);
        }
        else
        if(message.getText().equals("OPPONENT_ACTIVATE_CARD"))
        {
            createCardEvent(messageCard);
        }
        else
        if(message.getText().equals("OPPONENT_PASS_TURN"))
        {
            passTurn();
        }
        else
        if(message.getText().equals("CANCEL_CARD_EVENT"))
        {
            if(cardEvent!=null)
                cancelCardEvent();
        }
        else
        if(message.getText().equals("CARD_EVENT_ON_PLAYER"))
        {
            //"player" meaning the opponents (who sent the message) self
            this.createCardEvent(opponentsPlayArea.getPlayerBoxPanel());
        }
        else
        if(message.getText().equals("CARD_EVENT_ON_OPPONENT"))
        {
            //"opponent" meaning this player (who recieved the message) self
            this.createCardEvent(playerPlayArea.getPlayerBoxPanel());
        }
        else
        if(message.getText().equals("PLAYER_DISCARD_CARD"))
        {
            System.out.println("I am instructed to discard card - " + messageCard.getCardID());
            //System.out.println("which is in hand? - "+ getOpponentLocalCard(messageCard.getCardID()));
            System.out.println("cards in opponents hand:");
            for(Card c:opponentsHand.getCardsInHand())
                System.out.println(c.getCardID());

            //"player" meaning the active player who's turn it is
            //therefore on receipt, the player would be this applications opponent
            opponentsHand.discardCard(messageCard.getCardID());
        }
        else
        if(message.getText().equals("OPPONENT_LOSE_GAME"))
        {
            this.winGame();
        }
        else
        if(message.getText().equals("REQUEST_RESOLVE_COMBAT"))
        {
            requestResolveCombat();
        }
        else
        if(message.getText().equals("OPPONENT_DECLARED_BLOCKER"))
        {
            //send received blocking card to the create card event method
            //this method asigns the card as a blocker in the card event
            createCardEvent(messageCard);
        }
        else
        if(message.getText().equals("OPPONENT_PASS_ON_BLOCKING"))
        {
            executeCardEvent();
        }
        else
        if(message.getText().equals("OPPONENT_RESIGNED"))
        {
            winGame();
        }
    }

    public void disablePlay()
    {
        //disable interaction with play area
        //disable interaction with players game

        //player
        playerPlayArea.setEnabled(false);
        playerHand.setEnabled(false);
        playerDeck.setEnabled(false);

        //opponent
        opponentsPlayArea.setEnabled(false);
        opponentsHand.setEnabled(false);
        opponentsDeck.setEnabled(false);


    }

    public void setTurnPhase(TurnPhase phase)
    {
        turnPhase = phase;
        gameControlPanel.setTurnPhaseLabelText(turnPhase);
    }

    public void loseGame()
    {
        //you lose the game
        this.getRootPane().setGlassPane(new EndGameGlassPane(false));
        playSound("lost");
        //disable interaction
        this.disablePlay();
        gameControlPanel.endGame(false);


        //send message to connected server/client
        Message message = new Message();
        message.setText("OPPONENT_LOSE_GAME");
        sendMessage(message,null);
    }

    public void winGame()
    {
        //you win the games
        this.getRootPane().setGlassPane(new EndGameGlassPane(true));
        playSound("won");
        //disable interaction
        this.disablePlay();
        gameControlPanel.endGame(true);
    }

    public void zoomInCard(Card card)
    {
        if(cardZoomGlassPane==null)
        {
            ActionCard cc = (ActionCard) card;
            ActionCard zoomedCard = cc.getClone(getImageFromCache(card.getImageID()));
            //if glass pane is currently hidden
            //show zoomed in card glass pane
            rootPane = this.getRootPane();
            cardZoomGlassPane = new CardZoomGlassPane(zoomedCard);
            rootPane.setGlassPane(cardZoomGlassPane);
            cardZoomGlassPane.setVisible(true);
        }
        else
        if(cardZoomGlassPane!=null)
        {
            //remove zoomed in card glass pane
            //restore draw line glass pane
            cardZoomGlassPane.setVisible(false);
            cardZoomGlassPane = null;
            if(drawLineGlassPane!=null)
            {
                rootPane.setGlassPane(drawLineGlassPane);
                drawLineGlassPane.setVisible(true);
            }
        }
    }

    public void closeGameWindow()
    {
        sendMessage(new Message("OPPONENT_RESIGNED"),null);
        //remove game window
        parentTabbedPane.remove(this);
        musicClip.close();

        //close network connections
        if(netServer!=null)
            startGameWindow.stopNetworkGame(1);
        else if(netClient!=null)
            startGameWindow.stopNetworkGame(0);
    }

    public void playSound(String soundFileName)
    {

        AudioInputStream audioInputStream = null;
        final Clip clip;
        try {
            String soundName = "sounds/"+soundFileName+".wav";
            audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

            clip.addLineListener(new LineListener() {
                public void update(LineEvent myLineEvent) {
                    if(myLineEvent.getType() == LineEvent.Type.STOP){
                        clip.close();
                    }
                }
            });

        }
        catch (UnsupportedAudioFileException ex) {
            Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LineUnavailableException ex) {
            Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                audioInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void playAmbientSound()
    {

        AudioInputStream audioInputStream = null;
        try {
            String soundName = "sounds/ambientSound1.wav";
            audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile());
            ambientSoundClip = AudioSystem.getClip();
            ambientSoundClip.open(audioInputStream);
            ambientSoundClip.loop(100);

            ambientSoundClip.addLineListener(new LineListener() {
                public void update(LineEvent myLineEvent) {
                    if(myLineEvent.getType() == LineEvent.Type.STOP)
                        ambientSoundClip.close();
                }
            });
        }
        catch (UnsupportedAudioFileException ex) {
            Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LineUnavailableException ex) {
            Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                audioInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void playMusic()
    {

        int songNum = ThreadLocalRandom.current().nextInt(1,4);
        String soundName = "sounds/music_" + songNum +".wav";
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile());
            musicClip = AudioSystem.getClip();
            musicClip.open(audioInputStream);
            setVolume(musicClip,0.4f);
            musicClip.loop(100);

            musicClip.addLineListener(new LineListener() {
                public void update(LineEvent myLineEvent) {
                    if(myLineEvent.getType() == LineEvent.Type.STOP)
                        musicClip.close();
                }
            });

        }
        catch (UnsupportedAudioFileException ex) {
            Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LineUnavailableException ex) {
            Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                audioInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public float getVolume(Clip clip)
    {
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        return (float) Math.pow(10f, gainControl.getValue() / 20f);
    }

    public void setVolume(Clip clip, float volume)
    {
        if (volume < 0f || volume > 1f)
            throw new IllegalArgumentException("Volume not valid: " + volume);
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(20f * (float) Math.log10(volume));
    }

    public void drawPointer(Component origin, Component target)
    {
        rootPane = this.getRootPane();

        int horizontalSpacing = gameControlPanel.getWidth();
        int originVerticalSpacing = 0;
        int targetVerticalSpacing = 0;

        if(target instanceof PlayerBox)
        {
            PlayerBox targetPlayer = (PlayerBox) target;

            if(isPlayerTurn)
                //on the players turn, origin is always from players hand
                originVerticalSpacing = opponentsHand.getHeight()
                        + opponentsPlayArea.getHeight()
                        + target.getHeight()/2;
            else if(!isPlayerTurn)
                //if its the opponents turn, origin is always from the opponents hand
                originVerticalSpacing = opponentsHand.getHeight()
                        + (opponentsPlayArea.getHeight()/2)
                        + target.getHeight()/2;


            //if the target player is the player (me)
            if(!targetPlayer.getIsOpponent())
                //spacing includes opponents hand
                targetVerticalSpacing =
                        opponentsHand.getHeight()
                                + opponentsPlayArea.getHeight()
                                + playerPlayArea.getHeight()/2
                                + target.getHeight()/2;


            //if the target player is the opponent
            if(targetPlayer.getIsOpponent())
                //spacing includes opponents hand
                targetVerticalSpacing =
                        opponentsHand.getHeight()
                                + target.getHeight()/2;
        }


        if(target instanceof Card)
        {
            Card originCard = (Card) origin;
            Card targetCard = (Card) target;
            if(isPlayerTurn)
            {
                //on the players turn, origin is always from players hand
                originVerticalSpacing = opponentsHand.getHeight() + opponentsPlayArea.getHeight();


                //if target is in player hand, spacing includes opponents hand + play area
                if(targetCard.getCardLocation()==CardLocation.PLAYER_PLAY_AREA)
                    targetVerticalSpacing =
                            opponentsHand.getHeight()
                                    + opponentsPlayArea.getHeight();

                //if target is in opponents hand, spacing includes opponents hand only
                if(targetCard.getCardLocation()==CardLocation.OPPONENT_PLAY_AREA)
                    targetVerticalSpacing = opponentsHand.getHeight()
                            + (opponentsPlayArea.getHeight()/2);
            }
            else if(!isPlayerTurn)
            {
                //if its the opponents turn, origin is always from the opponents hand
                originVerticalSpacing = opponentsHand.getHeight()
                        + (opponentsPlayArea.getHeight()/2);


                //if the target is in the opponents hand, spacing includes opponents hand only
                if(targetCard.getCardLocation()==CardLocation.OPPONENT_PLAY_AREA)
                    targetVerticalSpacing = opponentsHand.getHeight()
                            + (opponentsPlayArea.getHeight()/2);


                //if the target is in the players hand, spacing includes
                if(targetCard.getCardLocation()==CardLocation.PLAYER_PLAY_AREA)
                    targetVerticalSpacing =
                            opponentsHand.getHeight()
                                    + opponentsPlayArea.getHeight();
            }
        }


        //create points for origin and target cards
        //x = middle of card + horizontal spacing
        //y = middle of card + vertical spacing determined above
        Point originPoint = new Point(origin.getX()+(origin.getWidth()/2)+horizontalSpacing,
                origin.getY()+(origin.getHeight()/2)+originVerticalSpacing);

        Point targetPoint = new Point(target.getX()+(target.getWidth()/2)+horizontalSpacing,
                target.getY()+(target.getHeight()/2)+targetVerticalSpacing);

        drawLineGlassPane = new DrawLineGlassPane(originPoint,targetPoint);

        if(cardZoomGlassPane==null)
        {
            rootPane.setGlassPane(drawLineGlassPane);
            drawLineGlassPane.setVisible(true);
        }
    }

    public void drawAnimations()
    {
        rootPane = this.getRootPane();
        Map<Point,String> animations = new HashMap<Point,String>();

        /**
         *  component width/2 = compCentre
         *  animation width/2 = animCentre
         *
         *  where to draw
         *  horizontal UI spacing + componenet.width/2
         *
         *  centre the animation
         *  where to draw *minus* animation.width/2
         *
         */

        componentAnimateMap.forEach((component,name)->{
            int horizontalSpacing = component.getWidth()/2;
            int verticalSpacing = 20 + component.getHeight()/2;

            if (component instanceof PlayerBox) {
                PlayerBox targetPlayer = (PlayerBox) component;

                if (targetPlayer.getIsOpponent()) {
                    //verticalSpacing = verticalSpacing + opponentsHand.getHeight();
                } else if (!targetPlayer.getIsOpponent()) {
                    verticalSpacing = verticalSpacing
                            //+ opponentsHand.getHeight()
                            + opponentsPlayArea.getHeight()
                            + playerPlayArea.getHeight() / 2;
                }
            }

            if (component instanceof Card) {
                Card targetCard = (Card) component;

                if(targetCard.getCardLocation() == CardLocation.PLAYER_PLAY_AREA){
                    verticalSpacing = verticalSpacing
                            //+ opponentsHand.getHeight()
                            + opponentsPlayArea.getHeight();
                }
                else
                if(targetCard.getCardLocation() == CardLocation.OPPONENT_PLAY_AREA){
                    verticalSpacing = verticalSpacing
                            //+ opponentsHand.getHeight()
                            + opponentsPlayArea.getHeight()/2;
                }
                else
                if(targetCard.getCardLocation() == CardLocation.PLAYER_HAND){
                    verticalSpacing = verticalSpacing
                            //+ opponentsHand.getHeight()
                            + opponentsPlayArea.getHeight()
                            + playerPlayArea.getHeight();
                }
                if(targetCard.getCardLocation() == CardLocation.OPPONENT_HAND){
                    //no spacing required
                }
            }

            //create points for origin and target cards
            //x = middle of card + horizontal spacing
            //y = middle of card + vertical spacing determined above
            Point targetPoint = new Point(component.getX()+horizontalSpacing,
                    component.getY()+verticalSpacing);

            animations.put(targetPoint,name);
        });

        animationGlassPane = new AnimationGlassPane(animations);
        rootPane.setGlassPane(animationGlassPane);
        Timer t = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                if(animationGlassPane!=null) {
                    animationGlassPane.setVisible(false);
                    animationGlassPane = null;
                    componentAnimateMap.clear();
                }
            }
        };
        t.schedule(task, 500);
    }

    public class AnimationGlassPane extends JComponent
    {
        public AnimationGlassPane(Map<Point,String> animations)
        {
            animations.forEach((point,effect)->{
                System.out.println("play " + effect);
                System.out.println(point.toString());
                ImageIcon animation = new ImageIcon(effect+".gif");
                JLabel label = new JLabel(animation);
                label.setBounds((point.x),point.y,animation.getIconWidth(),animation.getIconHeight());
                this.add(label);
                animation.getImage().flush();
            });
            //this.setOpaque(false);
            setVisible(true);
        }
    }

    public class DrawLineGlassPane extends JComponent
    {
        Point originCardPoint;
        Point targetCardPoint;

        public DrawLineGlassPane(Point origin, Point target)
        {
            originCardPoint = origin;
            targetCardPoint = target; 
            setVisible(true);
        }
        
        @Override
        protected void paintComponent(Graphics g) 
        {
            super.paintComponent(g);
            setForeground(Color.red);
            Graphics2D graphics = (Graphics2D) g;
            graphics.setStroke(new BasicStroke(5));
            graphics.drawLine(originCardPoint.x,originCardPoint.y,targetCardPoint.x,targetCardPoint.y);
        } 
    }

    public class CardZoomGlassPane extends JComponent
    {
        public CardZoomGlassPane(Card card)
        { 
            //make size of glass pane the same as the game window
            setSize(centrePanel.getWidth(), centrePanel.getHeight());
            setVisible(true);
            setBackground(overlayColor);
            card.setZoomed(true);
            //card.setImage(getImageFromCache(card.getImageID()));
            //resize clone of card to be zoomed            
            //card.applySize((int) Math.round(centrePanel.getHeight()*0.6));
            card.applySize((int) Math.round(centrePanel.getHeight()*0.6));
            //set card location on screen
            card.setBounds((gameControlPanel.getWidth() + (int) Math.round(card.getWidth()/5)), 
                    (int) Math.round((centrePanel.getHeight()-card.getHeight())/2) + playerHand.getHeight(), 
                    card.getWidth(), card.getHeight());
            
            //add card to glass pane
            add(card);
            card.setFaceUp(true);
            card.setIsActivated(false);
            card.setIsSelected(false);
            card.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {}

                @Override
                public void mousePressed(MouseEvent e) {}

                @Override
                public void mouseReleased(MouseEvent e) {
                    zoomInCard(card);
                }

                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseExited(MouseEvent e) {}
            });
        }
        
    }
    
    public class EndGameGlassPane extends JComponent
    {       
        public EndGameGlassPane(boolean win)
        {    
            JLabel resultLabel = new JLabel();
            Font font = new Font("Arial",Font.BOLD,64);
            resultLabel.setFont(font);
            
            if(win)
            {
                resultLabel.setText("VICTORY");
                
            }
            else
            {
                resultLabel.setText("DEFEATED");
            }
                
               
            this.add(resultLabel);
            this.setOpaque(false);
            setVisible(true);
        }  
        
        @Override
        protected void paintComponent(Graphics g) 
        {
            super.paintComponent(g);
            setForeground(Color.BLACK);
            this.setBackground(new Color(0,0,0,100));
        } 
    } 
    
}
