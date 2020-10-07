/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47.ui;

import com.bruynhuis.galago.ui.TextAlign;
import com.bruynhuis.galago.ui.button.TouchButton;
import com.bruynhuis.galago.ui.effect.TouchEffect;
import com.bruynhuis.galago.ui.panel.Panel;

/**
 *
 * @author NideBruyn
 */
public class ButtonExit extends TouchButton {
    
    public ButtonExit(Panel panel, String id, String text) {
        super(panel, id, "Interface/button-exit.png", 300, 80, true);
        setText(text);
        setFontSize(32);
        setTextAlignment(TextAlign.CENTER);
        addEffect(new TouchEffect(this));
    }
    
}
