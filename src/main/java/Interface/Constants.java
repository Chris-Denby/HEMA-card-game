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
    public static final int maxHandSize = 5;
    public static final int maxResourceAmount = 5;
    public static final int defaultPlayerHealth = 5;
    public static final int turnTimeLimit = 10;
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
    public static final int numberOfLanes = 3;
    public static final int laneCombatDelay = 1000;

    public static enum WeaponType
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
    
    public static enum ActionEffect
    {
        //Stealth, //card comes in face down to opponent until used
        Mastercut,
        Cut,
        Thrust,
        Parry_Cut,
        Parry_Thrust,
        Combo;
    }
}
