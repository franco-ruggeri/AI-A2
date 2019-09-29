import java.util.*;

public class Player {
	
	public final int depth = 10;
	
	private int myPlayer, otherPlayer; 
	
	private HashMap<String, Integer> redStates;
	private HashMap<String, Integer> whiteStates;
	private HashMap<String, Integer> currentPlayerStates;
	private HashMap<String, Integer> otherPlayerStates;

		
	public Player() {
		redStates = new HashMap<String, Integer>();
		whiteStates = new HashMap<String, Integer>();
		System.err.println("Inicializado");
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
    	
    	myPlayer = pState.getNextPlayer();
    	otherPlayer = (compare(myPlayer,Constants.CELL_RED) ? Constants.CELL_WHITE : Constants.CELL_RED);
    	currentPlayerStates = (compare(myPlayer,Constants.CELL_RED) ? redStates : whiteStates);
    	otherPlayerStates = (compare(myPlayer,Constants.CELL_RED) ? whiteStates : redStates);

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
	        aux = alphabeta(child, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, pDue);
	        if (aux > v) {
	            v = aux;
	            state = i;
	        }
	        i++;
	    }	    
		return lNextStates.elementAt(state);
    }
    
    private int alphabeta (GameState state, int depth, double alpha, double beta, Deadline deadline) {
    	Integer stored = currentPlayerStates.get(makeKey(state));
    	if (stored != null) {
    		return stored;
    	}
    	
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
                v = Math.max(v, alphabeta(child, depth-1, alpha, beta, deadline));
                alpha = Math.max(alpha, v);
                if (beta <= alpha) {
                    return v;
                }  

            }
        }
        else {
            v = Integer.MAX_VALUE;
            for (GameState child: nextStates) {
                v = Math.min(v, alphabeta(child, depth-1, alpha, beta, deadline));
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
    	currentPlayerStates.put(key, value);
    	otherPlayerStates.put(key, -value);
    	key = getSymmetricState(key);
    	currentPlayerStates.put(key, value);
    	otherPlayerStates.put(key, -value);
    	key = getOpposedState(key);
    	currentPlayerStates.put(key, -value);
    	otherPlayerStates.put(key, value);
    	key = getSymmetricState(key);
    	currentPlayerStates.put(key, -value);
    	otherPlayerStates.put(key, value);
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
    	String[] parts = state.split(" ");
    	String result = "", player = parts[1];
    	int i, j;
    	char c;
    	for (i = 0; i < 8; i++) {
    		for (j = 3; j >= 0; j--) {
    			c = parts[0].charAt(i*4 + j);
    			result += c;
    		}
    	}
    	return result + " " + player;
    }
    
    /**
     * Creates the Opposed State of given GameState message
     * @param state
     * @return
     */
    private String getOpposedState(String state) {
    	String[] parts = state.split(" ");
    	String result = "", player = parts[1];
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
    	player = player == "r" ? "w" : "r";
    	return result + " " + player;
    }
    
    private int eval (GameState state) {
    	int globalSum = 0;
    	int myPartialSum, othersPartialSum;
    	int i;
    	int piece;
    	
    	boolean amIRed = compare(myPlayer, Constants.CELL_RED);
    	if (state.isRedWin() && amIRed)
    		return Integer.MAX_VALUE;
    	else if (state.isRedWin() && !amIRed)
    		return Integer.MIN_VALUE + 1;
    	else if (state.isWhiteWin() && amIRed)
    		return Integer.MIN_VALUE + 1;
    	else if (state.isWhiteWin() && !amIRed)
    		return Integer.MAX_VALUE;
    	else if (state.isDraw())
    		return 0;
    	
    	myPartialSum = othersPartialSum = 0; 
    	for (i = 0; i < GameState.NUMBER_OF_SQUARES; i++) {
    		piece = state.get(i);
    		if (compare(piece, myPlayer)) {
    			if (compare(piece, Constants.CELL_KING)) {
    				myPartialSum += 10;
    			} else {    				
    				myPartialSum += getRelativeRow(piece, i);
    			}
    		} else if (compare(piece, otherPlayer)) {
    			if (compare(piece, Constants.CELL_KING)) {
    				othersPartialSum += 10;
    			} else {    				
    				othersPartialSum += getRelativeRow(piece, i);
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
