package scene3d.actions;

import scene3d.Action3d;
import scene3d.Actor3d;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class Actions3d {
	
	/** Returns a new or pooled action of the specified type. */
    static public <T extends Action3d> T action (Class<T> type) {
            Pool<T> pool = Pools.get(type);
            T action = pool.obtain();
            action.setPool(pool);
            return action;
    }
    
    static public AddAction addAction (Action3d action) {
        AddAction addAction = action(AddAction.class);
        addAction.setAction(action);
        return addAction;
    }
    
    static public AddAction addAction (Action3d action, Actor3d targetActor) {
        AddAction addAction = action(AddAction.class);
        addAction.setTargetActor(targetActor);
        addAction.setAction(action);
        return addAction;
    }
    
    static public RemoveAction removeAction (Action3d action) {
        RemoveAction removeAction = action(RemoveAction.class);
        removeAction.setAction(action);
        return removeAction;
    }
    
    static public RemoveAction removeAction (Action3d action, Actor3d targetActor) {
        RemoveAction removeAction = action(RemoveAction.class);
        removeAction.setTargetActor(targetActor);
        removeAction.setAction(action);
        return removeAction;
    }

    /** Moves the actor instantly. */
    static public MoveToAction moveTo (float x, float y, float z) {
            return moveTo(x, y, z, 0, null);
    }

    static public MoveToAction moveTo (float x, float y, float z, float duration) {
            return moveTo(x, y, z, duration, null);
    }

    static public MoveToAction moveTo (float x, float y, float z ,float duration, Interpolation interpolation) {
            MoveToAction action = action(MoveToAction.class);
            action.setPosition(x, y, z);
            action.setDuration(duration);
            action.setInterpolation(interpolation);
            return action;
    }
    
    /** Moves the actor instantly. */
    static public MoveByAction moveBy (float amountX, float amountY, float amountZ) {
            return moveBy(amountX, amountY, amountZ, 0, null);
    }

    static public MoveByAction moveBy (float amountX, float amountY, float amountZ, float duration) {
            return moveBy(amountX, amountY, amountZ, duration, null);
    }

    static public MoveByAction moveBy (float amountX, float amountY, float amountZ, float duration, Interpolation interpolation) {
            MoveByAction action = action(MoveByAction.class);
            action.setAmount(amountX, amountY, amountZ);
            action.setDuration(duration);
            action.setInterpolation(interpolation);
            return action;
    }
    
    /** Scales the actor instantly. */
    static public ScaleToAction scaleTo (float x, float y, float z) {
            return scaleTo(x, y, z, 0, null);
    }

    static public ScaleToAction scaleTo (float x, float y, float z, float duration) {
            return scaleTo(x, y, z, duration, null);
    }

    static public ScaleToAction scaleTo (float x, float y, float z, float duration, Interpolation interpolation) {
            ScaleToAction action = action(ScaleToAction.class);
            action.setScale(x, y, z);
            action.setDuration(duration);
            action.setInterpolation(interpolation);
            return action;
    }

    /** Scales the actor instantly. */
    static public ScaleByAction scaleBy (float amountX, float amountY, float amountZ) {
            return scaleBy(amountX, amountY, amountZ, 0, null);
    }

    static public ScaleByAction scaleBy (float amountX, float amountY, float amountZ, float duration) {
            return scaleBy(amountX, amountY, amountZ, duration, null);
    }

    static public ScaleByAction scaleBy (float amountX, float amountY, float amountZ, float duration, Interpolation interpolation) {
            ScaleByAction action = action(ScaleByAction.class);
            action.setAmount(amountX, amountY, amountZ);
            action.setDuration(duration);
            action.setInterpolation(interpolation);
            return action;
    }
    
    /** Rotates the actor instantly. */
    static public RotateToAction rotateTo (float rotation) {
            return rotateTo(rotation, 0, null);
    }
    
    static public RotateToAction rotateTo (float rotation, float duration) {
        return rotateTo(rotation, duration, null);
    }
    
    static public RotateToAction rotateTo (float rotation, float duration, Interpolation interpolation) {
        RotateToAction action = action(RotateToAction.class);
        action.setRotation(rotation);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    /** Rotates the actor instantly. */
    static public RotateByAction rotateBy (float rotationAmount) {
            return rotateBy(rotationAmount, 0, null);
    }
    
    static public RotateByAction rotateBy (float rotationAmount, float duration) {
        return rotateBy(rotationAmount, duration, null);
    }
    
    static public RotateByAction rotateBy (float rotationAmount, float duration, Interpolation interpolation) {
        RotateByAction action = action(RotateByAction.class);
        action.setAmount(rotationAmount);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }
    
    
    
    static public VisibleAction show () {
        return visible(true);
    }

    static public VisibleAction hide () {
        return visible(false);
    }
    
    static public VisibleAction visible (boolean visible) {
        VisibleAction action = action(VisibleAction.class);
        action.setVisible(visible);
        return action;
    }
    
    static public RemoveActorAction removeActor (Actor3d removeActor) {
        RemoveActorAction action = action(RemoveActorAction.class);
        action.setRemoveActor(removeActor);
        return action;
    }

    static public DelayAction delay (float duration) {
        DelayAction action = action(DelayAction.class);
        action.setDuration(duration);
        return action;
    }

    static public DelayAction delay (float duration, Action3d delayedAction) {
        DelayAction action = action(DelayAction.class);
        action.setDuration(duration);
        action.setAction(delayedAction);
        return action;
    }
    
    static public TimeScaleAction timeScale (float scale, Action3d scaledAction) {
        TimeScaleAction action = action(TimeScaleAction.class);
        action.setScale(scale);
        action.setAction(scaledAction);
        return action;
    }

    static public SequenceAction sequence (Action3d action1) {
        SequenceAction action = action(SequenceAction.class);
        action.addAction(action1);
        return action;
    }

    static public SequenceAction sequence (Action3d action1, Action3d action2) {
        SequenceAction action = action(SequenceAction.class);
        action.addAction(action1);
        action.addAction(action2);
        return action;
    }

    static public SequenceAction sequence (Action3d action1, Action3d action2, Action3d action3) {
        SequenceAction action = action(SequenceAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        return action;
    }

    static public SequenceAction sequence (Action3d action1, Action3d action2, Action3d action3, Action3d action4) {
        SequenceAction action = action(SequenceAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        action.addAction(action4);
        return action;
    }

    static public SequenceAction sequence (Action3d action1, Action3d action2, Action3d action3, Action3d action4, Action3d action5) {
        SequenceAction action = action(SequenceAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        action.addAction(action4);
        action.addAction(action5);
        return action;
    }

    static public SequenceAction sequence (Action3d... actions) {
        SequenceAction action = action(SequenceAction.class);
        for (int i = 0, n = actions.length; i < n; i++)
                action.addAction(actions[i]);
        return action;
    }

    static public SequenceAction sequence () {
        return action(SequenceAction.class);
    }

    static public ParallelAction parallel (Action3d action1) {
        ParallelAction action = action(ParallelAction.class);
        action.addAction(action1);
        return action;
    }

    static public ParallelAction parallel (Action3d action1, Action3d action2) {
        ParallelAction action = action(ParallelAction.class);
        action.addAction(action1);
        action.addAction(action2);
        return action;
    }

    static public ParallelAction parallel (Action3d action1, Action3d action2, Action3d action3) {
        ParallelAction action = action(ParallelAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        return action;
    }

    static public ParallelAction parallel (Action3d action1, Action3d action2, Action3d action3, Action3d action4) {
        ParallelAction action = action(ParallelAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        action.addAction(action4);
        return action;
    }

    static public ParallelAction parallel (Action3d action1, Action3d action2, Action3d action3, Action3d action4, Action3d action5) {
        ParallelAction action = action(ParallelAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        action.addAction(action4);
        action.addAction(action5);
        return action;
    }

    static public ParallelAction parallel (Action3d... actions) {
        ParallelAction action = action(ParallelAction.class);
        for (int i = 0, n = actions.length; i < n; i++)
                action.addAction(actions[i]);
        return action;
    }

    static public ParallelAction parallel () {
        return action(ParallelAction.class);
    }

    static public RepeatAction repeat (int count, Action3d repeatedAction) {
        RepeatAction action = action(RepeatAction.class);
        action.setCount(count);
        action.setAction(repeatedAction);
        return action;
    }

    static public RepeatAction forever (Action3d repeatedAction) {
        RepeatAction action = action(RepeatAction.class);
        action.setCount(RepeatAction.FOREVER);
        action.setAction(repeatedAction);
        return action;
    }

    static public RunnableAction run (Runnable runnable) {
        RunnableAction action = action(RunnableAction.class);
        action.setRunnable(runnable);
        return action;
    }
    
    static public AfterAction after (Action3d action) {
        AfterAction afterAction = action(AfterAction.class);
        afterAction.setAction(action);
        return afterAction;
    }

}
