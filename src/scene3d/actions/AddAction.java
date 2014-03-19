package scene3d.actions;

import scene3d.Action3d;
import scene3d.Actor3d;


public class AddAction extends Action3d {
        private Actor3d targetActor;
        private Action3d action;

        @Override
        public boolean act (float delta) {
                (targetActor != null ? targetActor : actor3d).addAction3d(action);
                return true;
        }

        public Actor3d getTargetActor() {
                return targetActor;
        }

        /** Sets the actor to add an action to. If null (the default), the {@link #getActor() actor} will be used. */
        public void setTargetActor(Actor3d actor) {
                this.targetActor = actor;
        }

        public Action3d getAction () {
                return action;
        }

        public void setAction(Action3d action) {
                this.action = action;
        }

        public void restart () {
                if (action != null) action.restart();
        }

        @Override
        public void reset () {
                super.reset();
                targetActor = null;
                action = null;
        }
}