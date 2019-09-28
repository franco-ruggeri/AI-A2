import java.util.*;

public class Player {
	
	public final int depth = 9;
	
	private int nPlayer; 
	
	private HashMap<String, Integer> repeatedStates;
		
	public Player() {
		repeatedStates = new HashMap<String, Integer>();
	}
		
    /**
     * Performs a move
     *
     * @param pState
     *            the current state of the board
     * @param pDue
     *            time before which we must have returned
     * @return the next state the board is in after our move
     */
    public GameState play(final GameState pState, final Deadline pDue) {
    	
    	nPlayer = (compare(pState.getNextPlayer(),Constants.CELL_RED) ? Constants.CELL_WHITE : Constants.CELL_RED);

        Vector<GameState> lNextStates = new Vector<GameState>();
        pState.findPossibleMoves(lNextStates);

        if (lNextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(pState, new Move());
        }

	    /**
	     * Here you should write your algorithms to get the best next move, i.e.
	     * the best next state. This skeleton returns a random move instead.
	     */
	    
	    int state = -1;
	    int v = Integer.MIN_VALUE;
	    int aux = 0;
	    int i = 0;
	    for (GameState child : lNextStates) {
	        aux = alphabeta(child, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, pState.getNextPlayer(), pDue);
	        if (aux > v) {
	            v = aux;
	            state = i;
	        }
	        i++;
	    }	    
		return lNextStates.elementAt(state);
    }
    
    private int alphabeta (GameState state, int depth, double alpha, double beta, int player, Deadline deadline) {
    	Integer stored = repeatedStates.get(makeKey(state));
    	if (stored != null) {
    		return stored;
    	}
    	
        if (depth == 0 || state.isEOG()) {
        	int value = eval (state);
        	addToHash(state, value);
            return value;
        }
        
        Vector<GameState> nextStates = new Vector<GameState>();
        state.findPossibleMoves(nextStates);
        
        int v;
        if (compare(player,Constants.CELL_RED)) {
            v = Integer.MIN_VALUE;
            for(GameState child : nextStates) {
                v = Math.max(v, alphabeta(child, depth-1, alpha, beta, Constants.CELL_WHITE, deadline));
                alpha = Math.max(alpha, v);
                if (beta <= alpha) {
                    return v;
                }  

            }
        }
        else {
            v = Integer.MAX_VALUE;
            for (GameState child: nextStates) {
                v = Math.min(v, alphabeta(child, depth-1, alpha, beta, Constants.CELL_RED, deadline));
                beta = Math.min(beta, v);
                if (beta <= alpha) {
                    return v;
                }  

            }
        }
        addToHash(state, v);
        return v;
    }
    
    /**
     * Adds to the HashMap the given GameState and all its symmetries
     * @param state
     * @param value The value of given GameState
     */
    private void addToHash(GameState state, int value) {
    	String key = "";
    	key = makeKey(state);
    	repeatedStates.put(key, value);
    	key = getSymmetricState(key);
    	repeatedStates.put(key, value);
    	key = getOpposedState(key);
    	repeatedStates.put(key, -value);
    	key = getSymmetricState(key);
    	repeatedStates.put(key, -value);
    }
    
    /**
     * Creates the key for the HashTable of given GameState
     * Key only depends on the position of the pieces and the player
     * @param state
     * @return The key to Hash the provided GameState
     */
    private String makeKey(GameState state) {
    	String key = state.toMessage();
    	String[] parts = key.split(" ");
    	//Only depends on position of the pieces and the player
    	key = parts[0] + " " + parts[2];
    	return key;
    }
    
    /**
     * Creates the Symmetric State of given GameState message
     * @param state
     * @return
     */
    private String getSymmetricState(String state) {
    	String result = "";
    	String[] parts = state.split(" ");
    	int i, j;
    	char c;
    	for (i = 0; i < 8; i++) {
    		for (j = 3; j >= 0; j--) {
    			c = parts[0].charAt(i*4 + j);
    			result += c;
    		}
    	}
    	return result + " " + parts[1];
    }
    
    /**
     * Creates the Opposed State of given GameState message
     * @param state
     * @return
     */
    private String getOpposedState(String state) {
    	String result = "";
    	String[] parts = state.split(" ");
    	int i, j;
    	char c;
    	for (i = 7; i >= 0; i--) {
    		for (j = 3; j >= 0; j--) {
    			c = parts[0].charAt(i * 4 + j);
    			if (c == 'r') c = 'w';
    			else if (c == 'R') c = 'W';
    			else if (c == 'w') c = 'r';
    			else if (c == 'W') c = 'R';
    			result += c;
    		}
    	}
    	return result + " " + parts[1];
    }
    
    private int eval (GameState state) {
    	int globalSum = 0;
    	int partialSum;
    	int i;
    	int pos;
    	
    	partialSum = 0; 
    	for (i = 0; i < GameState.NUMBER_OF_SQUARES; i++) {
    		pos = state.get(i);
    		if (compare(pos, Constants.CELL_RED)) {
    			if (compare(pos, Constants.CELL_KING)) {
    				partialSum += 10;
    			} else {    				
    				partialSum += 1; //GameState.cellToRow(i);
    				partialSum *= Math.abs(partialSum);
    			}
    		} else if (compare(pos, Constants.CELL_WHITE)) {
    			if (compare(pos, Constants.CELL_KING)) {
    				partialSum -= 10;
    			} else {    				
    				partialSum -= 1; //7 - GameState.cellToRow(i);
    				partialSum *= Math.abs(partialSum);
    			}
    		}
    	}
    	globalSum += partialSum;
    	    	
    	globalSum *= compare(nPlayer, Constants.CELL_RED) ? -1 : 1;
    	
    	return globalSum;
    }
    
    private boolean compare(int a, int b) {
    	boolean v = (a&b) != 0;
    	return v;
    }
}
