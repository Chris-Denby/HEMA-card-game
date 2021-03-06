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
import java.util.*;
import javax.swing.*;

import Interface.Constants.CardLocation;
import Interface.Constants.ActionEffect;
import Interface.Constants.TurnPhase;

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
    private CentrePanel playAreaCentrePanel;
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
    private SpellStack spellStack;

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
        JPanel centrePanel = new JPanel();
        spellStack = new SpellStack(this,centrePanel);
        centrePanel.setLayout(new BoxLayout(centrePanel,BoxLayout.X_AXIS));
        this.playAreaCentrePanel = new CentrePanel();
        this.playAreaCentrePanel.setOpaque(false);
        this.playAreaCentrePanel.setLayout(new BoxLayout(this.playAreaCentrePanel, BoxLayout.PAGE_AXIS));
        this.playAreaCentrePanel.add(opponentsResourcePanel);
        this.playAreaCentrePanel.add(opponentsPlayArea);
        this.playAreaCentrePanel.add(playerPlayArea);
        this.playAreaCentrePanel.add(playerResourcePanel);
        centrePanel.add(this.playAreaCentrePanel);
        centrePanel.add(spellStack);
        
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
        
        //playAmbientSound();
        //playMusic();
        this.playAreaCentrePanel.setImage(getImageFromCache(000));

        Timer timer = new Timer();

        TimerTask beginGameTask = new TimerTask() {
            @Override
            public void run()
            {
                beginGame();
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

    public SpellStack getSpellStack()
    {
        return spellStack;
    }

    public void beginGame()
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

    int stackEventIndex = 0;
    public void executeSpellStack()
    {
        combatTimer = new Timer();
        TimerTask stackEventTimerTask = null;
        stackEventIndex = spellStack.getCardsInStack().size()-1;

        //if stack is not empty
        if(spellStack.getCardsInStack().size()>0)
        {
            stackEventTimerTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    if(stackEventIndex<0){
                        combatTimer.cancel();
                        combatTimer.purge();
                        executeLaneCombat();
                        return;
                    }

                    CardEvent eventToExecute = spellStack.cardsInStack.get(stackEventIndex);
                    SpellCard originCard = (SpellCard) eventToExecute.getOriginCard();
                    ActionCard targetCard = (ActionCard) eventToExecute.getTargetCard();
                    PlayerBox targetPlayer = eventToExecute.getTargetPlayerBox();

                    eventToExecute.getOriginCard().setIsSelected(true);
                    //System.out.println("Card event ["+stackEventIndex+"]" + " spell played from "+"["+eventToExecute.getOriginCard().getCardLocation()+"]");

                    //execute card events here ------
                    if(originCard.getSpellEffect()==Constants.SpellEffect.Stun)
                    {
                        playSound("stun");
                            //execute stun effect - activated cards cant be used in combat
                            eventToExecute.getTargetCard().setIsActivated(true);
                            //draw animation
                            //componentAnimateMap.put(targetCard,"stun");
                            //drawAnimations();
                    }
                    //execute more card events here -----

                    //remove spent spell card from the stack
                    spellStack.removeCardEvent(eventToExecute);
                    //increment the stack index
                    stackEventIndex--;
                }
            };
            combatTimer.schedule(stackEventTimerTask,Constants.stackExecuteDelay,Constants.stackExecuteDelay);
        }
        else{
            executeLaneCombat();
        }
    }

    int combatIterator = 0;
    public void executeLaneCombat()
    {
        System.out.println("execute lane combat entered");
        //reset the iterator back to 0 - otherwise timer is cancelled
        combatIterator = 0;
        //Schedules the specified task for repeated fixed-delay execution (period), beginning after the specified delay (delay)
        combatTimer = new Timer();
        TimerTask laneTimerTask = new TimerTask() {
            @Override
            public void run()
            {
                //cancel timer after iterating through all lanes
                int x = combatIterator;
                if(combatIterator==Constants.numberOfLanes) {
                    combatTimer.cancel();
                    combatTimer.purge();
                    passTurn();
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

                //System.out.println("lane " +x + " --------");

                for(int y=0;y<2;y++)
                {
                    //System.out.println("    slot "+y);
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
                        playerActionCard.setIsActivated(true);
                        if(opponentActionCard!=null && !opponentActionCard.getIsActivated())
                        {
                            if(playerActionCard.getActionEffect()==ActionEffect.Cut && opponentActionCard.getActionEffect()==ActionEffect.Parry_Cut){
                                isBlocked = true;
                                //System.out.println("        Cut was blocked");
                                //add blocked animation
                            }
                            else
                            if(playerActionCard.getActionEffect()==ActionEffect.Thrust && opponentActionCard.getActionEffect()==ActionEffect.Parry_Thrust){
                                isBlocked = true;
                                //System.out.println("        Thrust was blocked");
                                //add blocked animation
                            }
                            else
                            if(opponentActionCard.getActionEffect()==ActionEffect.Mastercut){
                                isBlocked = true;
                                //System.out.println("        Blocked by mastercut");
                                //add blocked animation
                            }
                        }
                        if(!isBlocked)
                        {
                            //if player slot is not empty
                            //and the card is a cut of thrust
                            //execute the card
                            playSound("attackSwing");
                            opponentsPlayerBox.takeDamage(playerActionCard.getPower());
                            componentAnimateMap.put(opponentsPlayerBox,"slash");
                            //System.out.println("        card damage from " + playerActionCard.getPower());
                        }
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

    public TurnPhase getTurnPhase()
    {
        return turnPhase;
    }

    public void passTurn()
    {
        playSound("passTurn");
        gameControlPanel.setNotificationLabel("");

        //cancel any half created events
        playerPlayArea.cancelCardEvent();
        opponentsPlayArea.cancelCardEvent();

        //progress to next turn phase
        if(getTurnPhase()==null) {
            setTurnPhase(TurnPhase.MAIN_PHASE);
            //replenish resources back to turn amount
            playerResourcePanel.resetResources();
            opponentsResourcePanel.resetResources();
            playerHand.highlightPlayableCards();
            gameControlPanel.startTurnTimer();
        }
        else if(getTurnPhase()==TurnPhase.MAIN_PHASE) {
            setTurnPhase(TurnPhase.COMBAT_PHASE);
            playerHand.unHighlightPlayableCards();
            executeSpellStack();
        }
        else if (getTurnPhase()==TurnPhase.COMBAT_PHASE) {
            setTurnPhase(TurnPhase.END_PHASE);
            playerPlayArea.clearCards();
            opponentsPlayArea.clearCards();
            playerHand.discardAllCards();
            opponentsHand.discardAllCards();
            playerHand.dealHand();
            opponentsHand.dealHand();
            gameControlPanel.startTurnTimer();
        }
        else if (getTurnPhase()==TurnPhase.END_PHASE) {
            setTurnPhase(TurnPhase.MAIN_PHASE);
            //replenish resources back to turn amount
            playerResourcePanel.resetResources();
            opponentsResourcePanel.resetResources();
            playerHand.highlightPlayableCards();
            gameControlPanel.startTurnTimer();
        }


        gameControlPanel.setTurnPhaseLabelText(turnPhase);
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

            //REPLACE BELOW WITH CHECKING CARDS CARD LOCATION VARIABLE THEN USE GET CARD METHOD FROM PLAY AREA!!!!

            if(playerPlayArea.getCard(messageCard.getCardID())!=null){
                messageCard = playerPlayArea.getCard(messageCard.getCardID());
                messageCard.setPlayArea(playerPlayArea);
                messageCard.setPlayerHand(playerHand);

            }
            else
            if(opponentsPlayArea.getCard(messageCard.getCardID())!=null){
                messageCard = opponentsPlayArea.getCard(messageCard.getCardID());
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
        if(message.getText().equals("OPPONENT_SELECTED_CARD"))
        {
            //any incoming selections are directed to the opponents card event
            //local selections are made by mouse only
            System.out.println("createCardEvent triggered remotely");
            opponentsPlayArea.createCardEvent(messageCard);
        }
        else
        if(message.getText().equals("OPPONENT_PASS_TURN"))
        {
            passTurn();
        }
        else
        if(message.getText().equals("CANCEL_CARD_EVENT"))
        {
            opponentsPlayArea.cancelCardEvent();
        }
        else
        if(message.getText().equals("PLAYER_DISCARD_CARD"))
        {
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

    public PlayArea getOpponentsPlayArea()
    {
        return opponentsPlayArea;
    }

    public PlayArea getPlayerPlayArea(){
        return playerPlayArea;
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
        System.out.println("draw pointer method fired");
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
                //System.out.println("play " + effect);
                //System.out.println(point.toString());
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
            setSize(playAreaCentrePanel.getWidth(), playAreaCentrePanel.getHeight());
            setVisible(true);
            setBackground(overlayColor);
            card.setZoomed(true);
            //card.setImage(getImageFromCache(card.getImageID()));
            //resize clone of card to be zoomed            
            //card.applySize((int) Math.round(centrePanel.getHeight()*0.6));
            card.applySize((int) Math.round(playAreaCentrePanel.getHeight()*0.6));
            //set card location on screen
            card.setBounds((gameControlPanel.getWidth() + (int) Math.round(card.getWidth()/5)), 
                    (int) Math.round((playAreaCentrePanel.getHeight()-card.getHeight())/2) + playerHand.getHeight(),
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
