package scene3d.actions;

import scene3d.Action3d;
import scene3d.Actor3d;

import com.badlogic.gdx.utils.Array;

/** Executes an action only after all other actions on the actor at the time this action was added have finished.
 * @author Nathan Sweet */
public class AfterAction extends DelegateAction {
        private Array<Action3d> waitForActions = new Array(false, 4);

        public void setActor (Actor3d actor) {
                if (actor != null) waitForActions.addAll(actor.getActions3d());
                super.setActor(actor);
        }

        public void restart () {
                super.restart();
                waitForActions.clear();
        }

        protected boolean delegate (float delta) {
                Array<Action3d> currentActions = actor3d.getActions3d();
                if (currentActions.size == 1) waitForActions.clear();
                for (int i = waitForActions.size - 1; i >= 0; i--) {
                        Action3d action = waitForActions.get(i);
                        int index = currentActions.indexOf(action, true);
                        if (index == -1) waitForActions.removeIndex(i);
                }
                if (waitForActions.size > 0) return false;
                return action.act(delta);
        }
}