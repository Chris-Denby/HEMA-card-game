/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import java.awt.Color;

/**
 *
 * @author chris
 */
public class Constants 
{
    public static final int windowWidth = 900;
    public static final int windowHeight = 800;
    public static final int maxHandSize = 7;
    public static final int maxResourceAmount = 7;
    public static final int defaultPlayerHealth = 5;
    public static final int turnTimeLimit = 30;
    public static final int discardTimeLimit = 10;
    public static final int maxCaradsInPlayArea = 14;
    public static final int buffDistance = 2;
    public static final int buffModifier = 2;
    public static final int DECK_SIZE = 60;
    public static final double cardAspectRatio = 0.715;
    public static String imagePath = "C:\\Users\\chris\\AppData\\Local\\CardGame\\";
    public static final Color cardBaseColor = new Color(38,38,38,255);
    public static final Color shadowColor = new Color(0,0,0,80);
    public static final Color commonColor = new Color(255,255,255,255);
    public static final Color uncommonColor = new Color(0,112,221,255);
    public static final Color rareColor = new Color(163,53,238,255);
    public static final Color mythicColor = new Color(255,128,0,255);
    public static final int numCardSlots = 3;

    public static enum BannerType
    {
        Longsword,
        Rapier,
        Singlesword,
        Sabre,
    }
    
    public static enum CardLocation
    {
        PLAYER_HAND,
        PLAYER_PLAY_AREA,
        PLAYER_DECK,
        PLAYER_DISCARD_PILE,
        OPPONENT_HAND,
        OPPONENT_PLAY_AREA,
        OPPONENT_DECK,
        OPPONENT_DISCARD_PILE
    }
    
    public static enum TurnPhase
    {
        UPKEEP_PHASE,
        MAIN_PHASE,
        COMBAT_PHASE,
        DECLARE_BLOCKERS,        
        END_PHASE
    }
    
    public static enum SpellEffect
    {
        Draw_cards,
        Deal_damage,
        Stun;
    }
    
    public static enum CreatureEffect
    {
        NONE,
        Stealth, //card comes in face down to opponent until used
        Gain_Life,
        Taunt,
        Buff_Power;
    }
    
    
    
}
