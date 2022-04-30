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
    private PlayerBox targetPlayer;
    
    public CardEvent(Card originCard)
    {
        this.originCard = originCard;      
    }

    public CardEvent()
    {

    }

    public CardEvent(Card originCard, Card targetCard)
    {
        this.originCard = originCard;
        this.targetCard = targetCard;
    }

    public CardEvent(Card originCard, PlayerBox targetPlayer)
    {
        this.originCard = originCard;
        this.targetPlayer = targetPlayer;
    }

    public void addOriginCard(Card card)
    {
        originCard = card;
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
        return targetPlayer;
    }
    
    public void addTargetPlayerBox(PlayerBox box)
    {
        targetPlayer = box;
    }

}
