package scene3d.actions;

import scene3d.Action3d;
import scene3d.Actor3d;


/** Removes an actor from the stage.
 * @author Nathan Sweet */
public class RemoveActorAction extends Action3d {
        private Actor3d removeActor;
        private boolean removed;

        public boolean act (float delta) {
                if (!removed) {
                        removed = true;
                        (removeActor != null ? removeActor : actor3d).remove();
                }
                return true;
        }

        public void restart () {
                removed = false;
        }

        public void reset () {
                super.reset();
                removeActor = null;
        }

        public Actor3d getRemoveActor () {
                return removeActor;
        }

        /** Sets the actor to remove. If null (the default), the {@link #getActor() actor} will be used. */
        public void setRemoveActor (Actor3d removeActor) {
                this.removeActor = removeActor;
        }
}