/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47;

import com.bruynhuis.galago.app.Base3DApplication;
import com.bruynhuis.galago.resource.EffectManager;
import com.bruynhuis.galago.resource.FontManager;
import com.bruynhuis.galago.resource.ModelManager;
import com.bruynhuis.galago.resource.ScreenManager;
import com.bruynhuis.galago.resource.SoundManager;
import com.bruynhuis.galago.resource.TextureManager;
import za.co.bruynhuis.ld47.screens.PlayScreen;

/**
 *
 * @author NideBruyn
 */
public class MainApplication extends Base3DApplication {

    public static void main(String[] args) {
        new MainApplication();
    }

    public MainApplication() {
        super("Ludum Dare 47", 1280, 720, "ld47.save", null, null, true);
    }

    @Override
    protected void preInitApp() {
    }

    @Override
    protected void postInitApp() {
        showScreen(PlayScreen.NAME);
    }

    @Override
    protected boolean isPhysicsEnabled() {
        return true;
    }

    @Override
    protected void initScreens(ScreenManager screenManager) {
        screenManager.loadScreen(PlayScreen.NAME, new PlayScreen());

    }

    @Override
    public void initModelManager(ModelManager modelManager) {
    }

    @Override
    protected void initSound(SoundManager soundManager) {
        soundManager.loadSoundFx("button", "Sounds/button.wav");
        soundManager.loadSoundFx("crash", "Sounds/crash2.wav");
        soundManager.loadSoundFx("coin", "Sounds/coin.wav");
        soundManager.loadSoundFx("blockturn", "Sounds/blockturn.wav");
        soundManager.loadSoundFx("go", "Sounds/go.wav");
        soundManager.loadSoundFx("timer", "Sounds/timer.wav");
        soundManager.loadSoundFx("upgrade", "Sounds/upgrade.wav");

        soundManager.loadMusic("car", "Sounds/car.wav");
        soundManager.loadMusic("loop", "Sounds/loop.ogg");
    }

    @Override
    protected void initEffect(EffectManager effectManager) {
        effectManager.loadEffect("explode", "Models/effects/explode.j3o");
        effectManager.loadEffect("pickup", "Models/effects/pickup.j3o");
        effectManager.loadEffect("truck1", "Models/effects/truck1.j3o");
        effectManager.loadEffect("buggy1", "Models/effects/buggy1.j3o");
        effectManager.loadEffect("ship1", "Models/effects/ship1.j3o");
        effectManager.loadEffect("car1", "Models/effects/car1.j3o");
    }

    @Override
    protected void initTextures(TextureManager textureManager) {
    }

    @Override
    protected void initFonts(FontManager fontManager) {
    }

}
