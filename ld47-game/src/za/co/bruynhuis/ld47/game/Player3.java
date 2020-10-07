/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47.game;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.TweenCallback;
import com.bruynhuis.galago.control.effects.LineControl;
import com.bruynhuis.galago.control.effects.TrailControl;
import com.bruynhuis.galago.games.physics.PhysicsGame;
import com.bruynhuis.galago.games.physics.PhysicsPlayer;
import com.bruynhuis.galago.util.Debug;
import com.bruynhuis.galago.util.SpatialUtils;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.Stack;

/**
 *
 * @author NideBruyn
 */
public class Player3 extends PhysicsPlayer {

    private Node model;
    private Node followNode;
    private boolean accelarate = false;
    private boolean left = false;
    private boolean right = false;
    private Stack<Vector3f> waypoints;
    private float waypointReachDistance = 0.3f;
    private Vector3f direction;
    private Vector3f nextPosition = new Vector3f(0, 0, 0);
    private float speed = 0;
    private float accel = 0.1f;
    private float targetSpeed = 10;
    private float maxSpeed = 15;
    private float turnSpeed = 0.1f;
    private Node tempNode;
    private int lapCount = 1;
    private boolean gameEnded = false;
    private float playerHeight = 0.2f;
    private float sideMoveSpeed = 2;
    private boolean crashed = false;
    private boolean turning = false;
    private float turnSpacing = 1.2f;

    public Player3(PhysicsGame physicsGame) {
        super(physicsGame);
    }

    @Override
    protected void init() {

        model = (Node) game.getBaseApplication().getAssetManager().loadModel("Models/player/buggy1.j3o");
        playerNode.attachChild(model);
        model.move(0, playerHeight, 0);

        followNode = new Node("follow-node");
        playerNode.attachChild(followNode);
        followNode.move(0, 2f, -3);
        followNode.rotate(20f*FastMath.DEG_TO_RAD, 0, 0);

        tempNode = new Node();

        waypoints = ((Game) game).getCopyOfWaypoints();
        playerNode.setLocalTranslation(waypoints.peek());
        playerNode.lookAt(waypoints.get(waypoints.size() - 2), Vector3f.UNIT_Y);

        playerNode.addControl(new AbstractControl() {
            @Override
            protected void controlUpdate(float tpf) {

                if ((game.isStarted() && !game.isPaused()) || gameEnded) {

                    //Adjust the speed
                    if (speed < targetSpeed) {
                        speed += accel;

                    } else if (speed > targetSpeed) {
                        speed -= accel;

                    }

                    if (gameEnded && waypoints.isEmpty()) {
                        waypoints = ((Game) game).getCopyOfWaypoints();
                    }

                    if (!crashed) {
                        if (left) {
                            log("Turn left clicked");
                            turnLeft();
//                            model.move(tpf*2, 0, 0);
                        } else if (right) {
                            log("Turn right clicked");
                            turnRight();
//                            model.move(-tpf*2, 0, 0);
                        }

                    }

                    moveShip(tpf);

                }

            }

            @Override
            protected void controlRender(RenderManager rm, ViewPort vp) {

            }
        });

    }

    private void moveShip(float tpf) {
        if (getPosition().distance(waypoints.peek()) > waypointReachDistance) {

            tempNode.setLocalTranslation(playerNode.getLocalTranslation().clone());
            tempNode.setLocalRotation(playerNode.getLocalRotation().clone());
            tempNode.lookAt(waypoints.peek(), Vector3f.UNIT_Y);

            playerNode.getLocalRotation().slerp(tempNode.getLocalRotation(), turnSpeed);

            //Move in the direction the player is facing
            direction = playerNode.getLocalRotation().getRotationColumn(2).normalize();
            playerNode.move(direction.mult(tpf * speed));

        } else {
            Debug.log("Waypoint reached");
            waypoints.pop();

            if (waypoints.isEmpty()) {
                waypoints = ((Game) game).getCopyOfWaypoints();
                lapCount++;
                addScore(1);

            }

        }
    }

    public void increaseSpeed() {
        this.targetSpeed += 0.2f;
        if (this.targetSpeed > this.maxSpeed) {
            this.targetSpeed = this.maxSpeed;
        }
    }

    public void decreaseSpeed() {
        this.targetSpeed -= 0.2f;
        if (this.targetSpeed < 0) {
            this.targetSpeed = 0;
        }
    }

    @Override
    public Vector3f getPosition() {
        return playerNode.getWorldTranslation();
    }

    @Override
    public void doDie() {
        game.getBaseApplication().getEffectManager().doEffect("explode", model.getWorldTranslation());
        model.removeFromParent();

    }

    private void loadTrail(float offsetX) {
        Material trailMat = game.getBaseApplication().getAssetManager().loadMaterial("Materials/trail.j3m");

        Geometry trailGeometry = new Geometry();
//        trailMat.getAdditionalRenderState().setAlphaTest(true);
//        trailMat.getAdditionalRenderState().setAlphaFallOff(0.5f);
        trailGeometry.setMaterial(trailMat);
        //rootNode.attachChild(trail);  // either attach the trail geometry node to the root…
        trailGeometry.setIgnoreTransform(true); // or set ignore transform to true. this should be most useful when attaching nodes in the editor
        //trailGeometry.setQueueBucket(RenderQueue.Bucket.Translucent);

        Node trailNode = new Node("trailnode");
        trailNode.move(offsetX, -0.15f, 0);
        model.attachChild(trailNode);

        LineControl line = new LineControl(new LineControl.Algo2CamPosBBNormalized(), true);
        trailGeometry.addControl(line);
        TrailControl trailControl = new TrailControl(line);
        trailNode.addControl(trailControl);
        trailControl.setStartWidth(0.15f);
        trailControl.setEndWidth(0.15f);
        trailControl.setLifeSpan(0.1f);
        trailControl.setSegmentLength(0.1f);

        trailNode.attachChild(trailGeometry);
        trailGeometry.setQueueBucket(RenderQueue.Bucket.Transparent);
    }

    @Override
    public void start() {

        loadTrail(0.23f);
        loadTrail(-0.23f);

    }

    public boolean isAccelarate() {
        return accelarate;
    }

    public void setAccelarate(boolean accelarate) {
        this.accelarate = accelarate;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public Spatial getModel() {
        return model;
    }

    public void turnLeft() {

        if (!turning) {
            turning = true;
            SpatialUtils.moveFromToCenter(model, model.getLocalTranslation().x, model.getLocalTranslation().y, model.getLocalTranslation().z,
                    model.getLocalTranslation().x + turnSpacing, model.getLocalTranslation().y, model.getLocalTranslation().z, 1, 0, new TweenCallback() {
                @Override
                public void onEvent(int i, BaseTween<?> bt) {
                    log("left turning done");
                    turning = false;
                }
            }).start(game.getBaseApplication().getTweenManager());
            SpatialUtils.rotateFromTo(model, new Vector3f(0, 0, 0), new Vector3f(0, 20, 0), 0.4f, 0)
                    .repeatYoyo(1, 0)
                    .start(game.getBaseApplication().getTweenManager());
        }

    }

    public void turnRight() {
        if (!turning) {
            turning = true;
            SpatialUtils.moveFromToCenter(model, model.getLocalTranslation().x, model.getLocalTranslation().y, model.getLocalTranslation().z,
                    model.getLocalTranslation().x - turnSpacing, model.getLocalTranslation().y, model.getLocalTranslation().z, 1, 0, new TweenCallback() {
                @Override
                public void onEvent(int i, BaseTween<?> bt) {
                    log("right turning done");
                    turning = false;
                }
            }).start(game.getBaseApplication().getTweenManager());
            SpatialUtils.rotateFromTo(model, new Vector3f(0, 0, 0), new Vector3f(0, -20, 0), 0.4f, 0)
                    .repeatYoyo(1, 0)
                    .start(game.getBaseApplication().getTweenManager());
        }
    }

    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }

    public Node getFollowNode() {
        return followNode;
    }

}
