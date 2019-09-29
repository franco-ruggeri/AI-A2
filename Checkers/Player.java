import java.util.*;

public class Player {
	
	public final int depth = 10;
	
	private int myPlayer, otherPlayer; 
	
	//Hash map with the heuristics for Red Player
	private HashMap<String, Integer> redStates;
	//Hash map with the heuristics for White Player
	private HashMap<String, Integer> whiteStates;
	//Hash map with the heuristics for the current player (It will point to redStates or whiteStates)
	private HashMap<String, Integer> currentPlayerStates;
	//Hash map with the heuristics for the other player (It will point to redStates or whiteStates)
	private HashMap<String, Integer> otherPlayerStates;

		
	public Player() {
		//Initialize the HashMaps
		redStates = new HashMap<String, Integer>();
		whiteStates = new HashMap<String, Integer>();
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
    	// assign player
    	whoAmI = pState.getNextPlayer();
    	
    	// assign hash tables
    	
        deadline = pDue;
        return alphabeta(pState);
    }
    
    private GameState alphabeta(GameState state) {
    	Vector<GameState> nextStates = new Vector<>();
    	int v = Integer.MIN_VALUE;
    	GameState choice = new GameState(state, new Move());
    	
    	// fill next states
    	state.findPossibleMoves(nextStates);
    	
    	// no possible moves, pass
    	if (state.isEOG())
    		return choice;

    	// iterative deepening
    	maxDepth = 0;
    	timeout = false;
    	bestPath.clear();
    	while (!timeout) {
    		//Re-initialize HashMaps
    		redStates = new HashMap<String, Integer>();
    		whiteStates = new HashMap<String, Integer>();
    		currentPlayerStates = (whoAmI == Constants.CELL_RED ? redStates : whiteStates);
    		otherPlayerStates = (whoAmI == Constants.CELL_RED ? whiteStates : redStates);
    		// save result of the previous completed iteration
    		if (!bestPath.isEmpty())
    			choice = bestPath.lastElement();
    		
    		// prepare new iteration
    		maxDepth++;
    		bestPath.add(0, null);	// shift other elements to right, new position for the new step in depth
    		
    		// move ordering
    		moveOrdering(nextStates, maxDepth);
    		
    		// find action maximizing the "utility"
        	for (int i=0; i<nextStates.size() && !timeout; i++) {
        		GameState s = nextStates.elementAt(i);
        		int tmp = alphabetaR(s, maxDepth-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        		if (tmp > v) {
        			v = tmp;	// max
        			bestPath.setElementAt(s, maxDepth-1);	// argmax
        		}
        	}
    	}
    	
    	return choice;
    }
    
    /**
     * 
     * @param state The current GameState state
     * @param depth The remaining depth to dive in
     * @param alpha Current alpha value
     * @param beta Current beta value
     * @return The value of the current state node
     */
    private int alphabeta (GameState state, int depth, double alpha, double beta) {
    	//We retrieve the stored value for this node
    	Integer stored = currentPlayerStates.get(makeKey(state));
    	//If we have something stored, we return that value
    	if (stored != null) {
    		return stored;
    	}
    	//If we have ran out of depth or the state is End Of Game, 
    	//we compute the value for that specific node, and we add it to the HashMap
        if (depth == 0 || state.isEOG()) {
        	int value = eval (state);
        	addToHash(state, value);
            return value;
        }
        
        int player = state.getNextPlayer();
        
        Vector<GameState> nextStates = new Vector<GameState>();
        state.findPossibleMoves(nextStates);
        
        int v;
        if (compare(myPlayer,player)) {
            v = Integer.MIN_VALUE;
            for(GameState child : nextStates) {
                v = Math.max(v, alphabeta(child, depth-1, alpha, beta));
                alpha = Math.max(alpha, v);
                if (beta <= alpha) {
                    return v;
                }  

            }
        }
        else {
            v = Integer.MAX_VALUE;
            for (GameState child: nextStates) {
                v = Math.min(v, alphabeta(child, depth-1, alpha, beta));
                beta = Math.min(beta, v);
                if (beta <= alpha) {
                    return v;
                }  

            }
        }
        //We finally add to the HashMap the value of that node
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
    	//Compute the key corresponding to given state
    	key = makeKey(state);
    	currentPlayerStates.put(key, value);
    	otherPlayerStates.put(key, -value);
    	/*//Compute the key of the opposed state;
    	currentPlayerStates.put(key, value);
    	otherPlayerStates.put(key, -value);*/
    }
    
    /**
     * Creates the key for the HashTable of given GameState
     * Key only depends on the position of the pieces and the player
     * @param state
     * @return The key to Hash the provided GameState
     */
    private String makeKey(GameState state) {
    	String key = "";
    	for (int i = 0; i < 8; i++) {
    		int count = 4;
    		for (int j = 0; j < 4; j++) {
    			int piece = state.get(GameState.rowColToCell(i, j));
    			if ((piece & Constants.CELL_RED) != 0)
    				count += 1;
    			else if ((piece & Constants.CELL_WHITE) != 0)
    				count -=1;
    				
    		}
    		key += count;
    	}
    	System.err.println(key);
    	return key;
    }
    
    /**
     * Creates the Symmetric State of given GameState message
     * @param state
     * @return
     */
    private String getSymmetricState(String state) {
    	String result = "";
    	int i, j;
    	char c;
    	for (i = 0; i < 8; i++) {
    		//Only the order of columns changes
    		for (j = 3; j >= 0; j--) {
    			c = state.charAt(GameState.rowColToCell(i, j));
    			result += c;
    		}
    	}
    	return result;
    }
    
    /**
     * Creates the Opposed State of given GameState message
     * @param state
     * @return
     */
    private String getOpposedState(String state) {
    	String result = "";
    	int i, j;
    	char c;
    	//Beginning from bottom-right, we fill the contrary piece on top-left, to create the opposed state
    	for (i = 7; i >= 0; i--) {
    		for (j = 3; j >= 0; j--) {
    			c = state.charAt(GameState.rowColToCell(i, j));
    			if (c == 'r') c = 'w';
    			else if (c == 'R') c = 'W';
    			else if (c == 'w') c = 'r';
    			else if (c == 'W') c = 'R';
    			result += c;
    		}
    	}
    	return result;
    }
    
    private int eval (GameState state) {
    	int globalSum = 0;
    	int myPartialSum, othersPartialSum;
    	int i;
    	int piece;
    	
    	// terminal state, the result is certain
    	if (state.isEOG()) {
    		if (isWin(state))
    			return Integer.MAX_VALUE;
    		else if (isLoss(state))
    			return Integer.MIN_VALUE + 1;
    		else
    			return 0;	// draw
    	}
    	
    	myPartialSum = othersPartialSum = 0; 
    	for (i = 0; i < GameState.NUMBER_OF_SQUARES; i++) {
    		piece = state.get(i);
    		if (compare(piece, myPlayer)) {
    			if (compare(piece, Constants.CELL_KING)) {
    				myPartialSum += 5;
    			} else {    				
    				myPartialSum += 1; //getRelativeRow(piece, i);
    			}
    		} else if (compare(piece, otherPlayer)) {
    			if (compare(piece, Constants.CELL_KING)) {
    				othersPartialSum += 5;
    			} else {    				
    				othersPartialSum += 1; //getRelativeRow(piece, i);
				}
    		}
    	}
//    	myPartialSum *= myPartialSum;
//    	othersPartialSum *= othersPartialSum;
    	globalSum = myPartialSum - othersPartialSum;
    	
    	return globalSum;
    }
    
    /**
     * 
     * @param piece
     * @return
     */
    private int getRelativeRow(int piece, int position) {
    	if (compare(piece,Constants.CELL_RED))
    		return GameState.cellToRow(position);
    	else
    		return 7 - GameState.cellToRow(position);
    }
    
    private boolean compare(int a, int b) {
    	boolean v = (a&b) != 0;
    	return v;
    }
    
}
