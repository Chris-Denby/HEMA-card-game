/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import Database.JSONHelper;
import Interface.Constants.TurnPhase;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author chris
 */
public class GameControlPanel extends JPanel
{
    private int height;
    private int width;
    private GameWindow gameWindow;
    private JButton passTurnButton;
    private JButton resolveButton;
    private JButton resignButton;
    private JLabel turnLabel;
    private JLabel turnNumberLabel;
    private JLabel turnPhaseLabel;
    private JLabel notificationLabel;
    private JLabel turnTimeLabel;
    private boolean isPlayerTurn = false;   
    private Timer turnTimer = new Timer();
    private TimerTask turnTimerTask;
    private TimerTask discardTask;
    private int turnTimeLimit = 60;
    private int discardTimeLimit;
    
    public GameControlPanel(int containerWidth, int containerHeight, GameWindow window)
    {
        gameWindow = window;
        height = containerHeight;
        width = Math.round(containerWidth/3);


        
        this.setPreferredSize(new Dimension(width,height));
        this.setOpaque(true);
        this.setBackground(Color.LIGHT_GRAY);
        
        passTurnButton = new JButton("Pass Turn");
        resolveButton = new JButton("Resolve Event");
        resignButton = new JButton("Resign");
        passTurnButton.setEnabled(false);
        resolveButton.setEnabled(false);
        turnLabel = new JLabel();
        turnNumberLabel = new JLabel();
        turnPhaseLabel = new JLabel();
        notificationLabel = new JLabel();
        turnTimeLabel = new JLabel();
        this.add(passTurnButton);
        this.add(resolveButton); 
        this.add(turnLabel);
        this.add(turnNumberLabel);
        this.add(turnPhaseLabel);
        this.add(notificationLabel);
        this.add(turnTimeLabel);
        this.add(resignButton);
        
        resolveButton.setEnabled(false);
        
        resolveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                if(gameWindow.getTurnPhase()==TurnPhase.MAIN_PHASE)
                {
                    //do nothing on main phase                    
                }
                else
                if(gameWindow.getIsPlayerTurn() && gameWindow.getTurnPhase()==TurnPhase.COMBAT_PHASE)
                {
                    gameWindow.requestResolveCombat();  
                }
                else
                if(!gameWindow.getIsPlayerTurn() && gameWindow.getTurnPhase()==TurnPhase.DECLARE_BLOCKERS)
                {
                    gameWindow.passOnBlocking();
                }
            }
        });
        
        passTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                passTurnButtonAction();
            }
        });
        
        resignButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                gameWindow.closeGameWindow();
            }
        });
    }
       
    public void enableResolveButton(boolean enabled)
    {
        resolveButton.setEnabled(enabled);
    }

    public void setResolveButtonText(String text)
    {
        resolveButton.setText(text);
    }
    
    public void setTurnLabelText(String text)
    {
       turnLabel.setText(text);
    }  
    
    public void setIsPlayerTurn(boolean is)
    {
        this.isPlayerTurn = is;
        if(isPlayerTurn)
        {
            turnLabel.setText("YOUR\nTURN");
        }
        else
        {
            turnLabel.setText("OPPONENTS\nTURN");           
        }
        
        passTurnButton.setEnabled(isPlayerTurn);
        resolveButton.setEnabled(isPlayerTurn);
        turnNumberLabel.setText(gameWindow.getTurnNumber()+"");
    }
    
    public void setTurnPhaseLabelText(TurnPhase phase)
    {
        turnPhaseLabel.setText(phase+"");
    }
    
    public void setNotificationLabel(String text)
    {
        notificationLabel.setText(text);
    }
    
    public void endGame(boolean hasWon)
    {
        turnTimer.cancel();

        if(hasWon)
        {
            //you won
            notificationLabel.setText("YOU WON"); 
        }
        else
        {
            //you lost
             notificationLabel.setText("YOU LOST");            
        }
        
        //disable play buttons
         passTurnButton.setEnabled(false);
         resolveButton.setEnabled(false);
         
        
    }

    private void passTurnButtonAction()
    {
        if(gameWindow.getPlayerHand().checkHandSizeForEndTurn()) {
            gameWindow.passTurn();
        }
        else {
            startDiscardTimer();
        }
    }

    public void startTurnTimer()
    {
        //stop timer from previous turn
        if(turnTimerTask!=null) {
            turnTimerTask.cancel();
        }
        if(discardTask!=null){
            discardTask.cancel();
        }

        turnTimeLimit = Constants.turnTimeLimit;
        turnTimeLabel.setText(turnTimeLimit+"");
        turnTimeLabel.setForeground(Color.BLACK);

        turnTimer.purge();

        turnTimerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                turnTimeLimit--;

                if(turnTimeLimit>=0)
                    turnTimeLabel.setText(turnTimeLimit+"");

                if(turnTimeLimit<=10)
                    turnTimeLabel.setForeground(Color.RED);

                if(turnTimeLimit==0)
                {
                    if(gameWindow.getIsPlayerTurn())
                        passTurnButtonAction();
                }
            }
        };
        turnTimer.scheduleAtFixedRate(turnTimerTask,0,1000);
    }
    
    public void increaseTime()
    {
        turnTimeLimit = turnTimeLimit +3;
    }
    
    public void setTurnTimerLabelText(int sec)
    {
        turnTimeLabel.setText(sec+"");
    }
    
    public void startDiscardTimer()
    {
        if(discardTask!=null){
            discardTask.cancel();
        }
        if(turnTimerTask!=null){
            turnTimerTask.cancel();
        }

        resolveButton.setEnabled(false);
        discardTimeLimit = Constants.discardTimeLimit;

        discardTask = new TimerTask()
        {
            @Override
            public void run()
            {
                discardTimeLimit--;
                setTurnTimerLabelText(discardTimeLimit);
                if(discardTimeLimit==0)
                {
                    //if discard time limit reaches 0
                    //discard last card in hand
                    int cardsInHand = gameWindow.getPlayerHand().getCardsInHand().size();
                    int cardsToDiscard = cardsInHand - Constants.maxHandSize;
                    while(cardsToDiscard>0)
                    {
                        gameWindow.getPlayerHand().discardCard(gameWindow.getPlayerHand().getCardsInHand().get(gameWindow.getPlayerHand().getCardsInHand().size()-1).getCardID());
                        cardsToDiscard--;
                    }
                    gameWindow.passTurn();
                }
            }
        };

        turnTimer.schedule(discardTask, 0, 1000);
    }
}
