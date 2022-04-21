package Interface.Cards;

import Interface.Constants.WeaponType;

import javax.swing.*;
import java.awt.*;

public class WeaponCard extends Card
{

    private WeaponType weaponType;
    private int durability = 5;
    private JLabel durabilityLabel = new JLabel(durability+"");

    public WeaponCard(String cardName, int imageId) {
        super(cardName, imageId);

        cardNameLabel.setText("Weapon");
        GridLayout gridLayout = new GridLayout(0,3,0,0);
        bodyBox.setLayout(gridLayout);
        bodyBox.setBackground(new Color(0,0,0,180));
        bodyBox.remove(textBox);
        bodyBox.add(durabilityLabel);
    }

    public void setWeaponType(WeaponType type)
    {
        weaponType = type;
        abilityLabel.setText(weaponType.name());
    }

    public WeaponType getWeaponType()
    {
        return weaponType;
    }

    public void decrementDurability()
    {
        durability--;
        durabilityLabel.setText(durability+"");
    }




}
