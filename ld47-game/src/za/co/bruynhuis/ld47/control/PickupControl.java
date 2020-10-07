/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47.control;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import za.co.bruynhuis.ld47.game.Game;

/**
 *
 * @author NideBruyn
 */
public class PickupControl extends AbstractControl {

    private Game game;
    private RigidBodyControl rbc;
    private boolean detroy;

    public PickupControl(Game game) {
        this.game = game;
    }

    @Override
    protected void controlUpdate(float tpf) {

        if (rbc == null) {
            rbc = spatial.getControl(RigidBodyControl.class);
        }

        if (detroy) {
            if (rbc != null) {
                game.getBaseApplication().getBulletAppState().getPhysicsSpace().remove(rbc);
            }

            spatial.removeFromParent();
            game.removePickup();

            game.loadPickups();
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }

    public void doPickup() {
        if (!detroy) {
            game.getPlayer().addScore(100);
            game.getBaseApplication().getMessageManager().sendMessage("score", 100);
            game.getBaseApplication().getEffectManager().doEffect("pickup", spatial.getWorldTranslation().clone());
            game.getBaseApplication().getSoundManager().playSound("coin");
            detroy = true;

        }
    }
}
