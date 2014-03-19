package scene3d.actions;

import scene3d.Action3d;

import com.badlogic.gdx.utils.Pool;

/** An action that runs a {@link Runnable}. Alternatively, the {@link #run()} method can be overridden instead of setting a
 * runnable.
 * @author Nathan Sweet */
public class RunnableAction extends Action3d {
        private Runnable runnable;
        private boolean ran;

        @Override
        public boolean act (float delta) {
                if (!ran) {
                        ran = true;
                        run();
                }
                return true;
        }

        /** Called to run the runnable. */
        public void run () {
                Pool pool = getPool();
                setPool(null); // Ensure this action can't be returned to the pool inside the runnable.
                try {
                        runnable.run();
                } finally {
                        setPool(pool);
                }
        }

        @Override
        public void restart () {
                ran = false;
        }

        @Override
        public void reset () {
                super.reset();
                runnable = null;
        }

        public Runnable getRunnable () {
                return runnable;
        }

        public void setRunnable (Runnable runnable) {
                this.runnable = runnable;
        }
}