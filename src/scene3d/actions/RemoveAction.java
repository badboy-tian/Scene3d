

package scene3d.actions;

import scene3d.Action3d;
import scene3d.Actor3d;

/** Removes an action from an actor.
 * @author Nathan Sweet */
public class RemoveAction extends Action3d {
        private Actor3d targetActor;
        private Action3d action;

        public boolean act (float delta) {
                (targetActor != null ? targetActor : actor3d).removeAction3d(action);
                return true;
        }

        public Actor3d getTargetActor () {
                return targetActor;
        }

        /** Sets the actor to remove an action from. If null (the default), the {@link #getActor() actor} will be used. */
        public void setTargetActor (Actor3d actor) {
                this.targetActor = actor;
        }

        public Action3d getAction () {
                return action;
        }

        public void setAction (Action3d action) {
                this.action = action;
        }

        public void reset () {
                super.reset();
                targetActor = null;
                action = null;
        }
}