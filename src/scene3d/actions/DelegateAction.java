package scene3d.actions;

import scene3d.Action3d;
import scene3d.Actor3d;

import com.badlogic.gdx.utils.Pool;


/** Base class for an action that wraps another action.
 * @author Nathan Sweet */
abstract public class DelegateAction extends Action3d {
        protected Action3d action;

        /** Sets the wrapped action. */
        public void setAction (Action3d action) {
                this.action = action;
        }

        public Action3d getAction () {
                return action;
        }

        abstract protected boolean delegate (float delta);

        public final boolean act (float delta) {
                Pool pool = getPool();
                setPool(null); // Ensure this action can't be returned to the pool inside the delegate action.
                try {
                        return delegate(delta);
                } finally {
                        setPool(pool);
                }
        }

        public void restart () {
                if (action != null) action.restart();
        }

        public void reset () {
                super.reset();
                action = null;
        }

        public void setActor (Actor3d actor) {
                if (action != null) action.setActor3d(actor);
                super.setActor3d(actor);
        }

        public String toString () {
                return super.toString() + (action == null ? "" : "(" + action + ")");
        }
}