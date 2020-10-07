/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47.game;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Linear;
import com.bruynhuis.galago.control.tween.SpatialAccessor;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author NideBruyn
 */
public class PlayerPlayback extends AbstractControl {

    private Game game;
    private RecordingSnapshot recordingSnapshot;
    private boolean active;
    private boolean processing;
    private RecordEntry currentEntry;
    private int recordingEntryIndex = 0;
    private Quaternion targetRotation;

    public PlayerPlayback(Game game, RecordingSnapshot recordingSnapshot) {
        this.game = game;
        this.recordingSnapshot = recordingSnapshot;
    }

    @Override
    protected void controlUpdate(float tpf) {

        if (active) {
            
            if (targetRotation != null) {
                spatial.getLocalRotation().slerp(targetRotation, 0.1f);
                
            }

            if (!processing) {
                currentEntry = recordingSnapshot.getEntries().get(recordingEntryIndex);
                processing = true;
                targetRotation = currentEntry.getRotation();
//                float targetAngle = targetRotation.toAngles(null)[1] * FastMath.RAD_TO_DEG;
                
                Timeline.createParallel()
                        .push(Tween.to(spatial, SpatialAccessor.POS_XYZ, currentEntry.getEllapsedTime())
                                .target(currentEntry.getLocation().x, currentEntry.getLocation().y, currentEntry.getLocation().z)
                                .ease(Linear.INOUT))
//                        .push(
//                                Tween.to(spatial, SpatialAccessor.ROTATION_Y, currentEntry.getEllapsedTime())
//                                .target(targetAngle)
//                                .ease(Linear.INOUT)
//                        )
                        .setCallback(new TweenCallback() {
                            @Override
                            public void onEvent(int i, BaseTween<?> bt) {
                                recordingEntryIndex++;
                                processing = false;

                                if (recordingEntryIndex >= recordingSnapshot.getEntries().size()) {
                                    active = false;

                                }

                            }
                        })
                        .start(game.getBaseApplication().getTweenManager());

//                Tween.to(spatial, SpatialAccessor.POS_XYZ, currentEntry.getEllapsedTime())
//                        .target(currentEntry.getLocation().x, currentEntry.getLocation().y, currentEntry.getLocation().z)
//                        .ease(Linear.INOUT)
//                        .setCallback(new TweenCallback() {
//                            @Override
//                            public void onEvent(int i, BaseTween<?> bt) {
//                                recordingEntryIndex ++;
//                                processing = false;
//                                
//                                if (recordingEntryIndex >= recordingSnapshot.getEntries().size()) {
//                                    active = false;
//                                    
//                                }
//
//                            }
//                        })
//                        .start(game.getBaseApplication().getTweenManager());
//                
//                Quaternion q = currentEntry.getRotation();
//                float targetAngle = q.toAngles(null)[1] * FastMath.RAD_TO_DEG;
//                Tween.to(spatial, SpatialAccessor.ROTATION_Y, currentEntry.getEllapsedTime())
//                        .target(targetAngle)
//                        .ease(Linear.INOUT)
//                        .start(game.getBaseApplication().getTweenManager());
            }

        }

    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }

    public void start() {
        currentEntry = null;
        recordingEntryIndex = 0;
        processing = false;
        active = true;
    }
}
