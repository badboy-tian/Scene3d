package scene3d.actions;


/** Sets the actor's rotation from its current value to a specific value.
 * @author Nathan Sweet */
public class RotateToAction extends TemporalAction {
        private float start, end;

        protected void begin () {
                start = actor3d.getRotation();
        }

        protected void update (float percent) {
                actor3d.setRotation(start + (end - start) * percent);
        }

        public float getRotation () {
                return end;
        }

        public void setRotation (float rotation) {
                this.end = rotation;
        }
}