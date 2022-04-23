/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import Interface.Cards.PlayerBox;
import Interface.Cards.Card;
import java.io.Serializable;

/**
 *
 * @author chris
 */
public class CardEvent implements Serializable
{
    private Card originCard = null;
    private Card targetCard = null;
    private Card blockingCard = null;
    private PlayerBox playerBoxPanel;
    private boolean isResolved = false;
    private int laneNumber = 0;
    
    public CardEvent(Card originCard)
    {
        this.originCard = originCard;      
    }

    public void setLaneNumber(int num)
    {
        laneNumber=num;
    }

    public int getLaneNumber()
    {
        return laneNumber;
    }

    public void addTargetCard(Card card)
    {
        targetCard = card;
    }
    
    public Card getTargetCard()
    {
        return targetCard;
    }
    
    public Card getOriginCard()
    {
        return originCard;
    }
    
    public PlayerBox getTargetPlayerBox()
    {
        return playerBoxPanel;
    }
    
    public void addTargetPlayerBox(PlayerBox box)
    {
        playerBoxPanel = box;
    }

    public void execute()
    {
        isResolved = true;
    }
    
    
    
    
}
