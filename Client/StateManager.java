/**
 * Inhibits a State that can be accessed by the Client and the Server by a Get
 * end Set function.
 *
 */
public class StateManager {
	private State currentState = null;

	/**
	 * Initializes the StateManger with a currentState.
	 */
	public StateManager() {
		currentState = State.NONE;
	}

	/**
	 * Returns the currentState.
	 * 
	 * @return
	 */
	public State getState() {
		return currentState;
	}

	/**
	 * Sets the currentState.
	 * 
	 * @param state
	 */
	public void setState(State state) {
		currentState = state;
	}
}
