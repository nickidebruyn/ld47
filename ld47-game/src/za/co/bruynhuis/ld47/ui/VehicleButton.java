/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47.ui;

import com.bruynhuis.galago.ui.Image;
import com.bruynhuis.galago.ui.Label;
import com.bruynhuis.galago.ui.TextAlign;
import com.bruynhuis.galago.ui.button.ControlButton;
import com.bruynhuis.galago.ui.effect.TouchEffect;
import com.bruynhuis.galago.ui.listener.TouchButtonAdapter;
import com.bruynhuis.galago.ui.panel.Panel;
import com.jme3.math.ColorRGBA;

/**
 *
 * @author NideBruyn
 */
public class VehicleButton extends Panel {

    private ControlButton controlButton;
    private Label priceLabel;
    private Image image;

    public VehicleButton(Panel parent, String pictureFile, String id) {
        super(parent, "Interface/vehicle-panel.png", 128, 128, true);

        priceLabel = new Label(this, "x 0", 16, 100, 16);
        priceLabel.setAlignment(TextAlign.LEFT);
        priceLabel.setTextColor(ColorRGBA.White);
        priceLabel.centerTop(30, 0);
        
        image = new Image(this, pictureFile, 96, 96, true);
        image.centerAt(0, 0);
        
        controlButton = new ControlButton(this, id, 128, 128, true);
        controlButton.addEffect(new TouchEffect(this));

        parent.add(this);

    }
    
    public void setEnabled(boolean enabled) {
        controlButton.setEnabled(enabled);
        
    }

    public void setCost(int cost) {
        this.priceLabel.setText("x " + cost);
    }
    
    public void addButtonAdapter(TouchButtonAdapter touchButtonAdapter) {
        controlButton.addTouchButtonListener(touchButtonAdapter);
    }
}
