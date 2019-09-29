import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Player {
	private int whoAmI;
	private Deadline deadline;
	private int maxDepth;		// current iteration (iterative deepening)
	private boolean timeout;	// set to true when deadline is almost reached
	private Vector<String> bestPath = new Vector<>();	// for move ordering
	
	//Hash map with the heuristics for Red Player
	private HashMap<String, Integer> redStates = new HashMap<>();
	//Hash map with the heuristics for White Player
	private HashMap<String, Integer> whiteStates = new HashMap<>();
	//Hash map with the heuristics for the current player (It will point to redStates or whiteStates)
	private HashMap<String, Integer> currentPlayerStates;
	//Hash map with the heuristics for the other player (It will point to redStates or whiteStates)
	private HashMap<String, Integer> otherPlayerStates;
	
	private static final long MARGIN_DEADLINE = (long) (5*1e7);	// 50 ms (5000000 ns) of margin
	private static final long TIME_TO_RETURN = (long) 1e5;		// 0.1 ms (100000 ns) to return 1 level up in recursion
	
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
    	currentPlayerStates = (whoAmI == Constants.CELL_RED ? redStates : whiteStates);
    	otherPlayerStates = (whoAmI == Constants.CELL_RED ? whiteStates : redStates);
    	
        deadline = pDue;
        return alphabeta(pState);
    }
    
    private GameState alphabeta(GameState state) {
    	Vector<GameState> nextStates = new Vector<>();
    	int v = Integer.MIN_VALUE;
    	GameState finalChoice = new GameState(state, new Move());
    	GameState tmpChoice = new GameState(state, new Move()); 
    	
    	// fill next states
    	state.findPossibleMoves(nextStates);
    	
    	// no possible moves, pass
    	if (state.isEOG())
    		return finalChoice;

    	// iterative deepening
    	maxDepth = 0;
    	timeout = false;
    	bestPath.clear();
    	while (!timeout) {
    		// save result of the previous completed iteration
    		finalChoice = tmpChoice;
    		
    		// prepare new iteration
    		maxDepth++;
    		bestPath.add(0, null);	// shift other elements to right, new position for the new step in depth
    		redStates = new HashMap<String, Integer>();
    		whiteStates = new HashMap<String, Integer>();
    		currentPlayerStates = (whoAmI == Constants.CELL_RED ? redStates : whiteStates);
    		otherPlayerStates = (whoAmI == Constants.CELL_RED ? whiteStates : redStates);
    		
    		// move ordering
    		moveOrdering(nextStates, maxDepth);
    		
    		// find action maximizing the "utility"
        	for (int i=0; i<nextStates.size() && !timeout; i++) {
        		GameState s = nextStates.elementAt(i);
        		int tmp = alphabetaR(s, maxDepth-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        		if (tmp > v) {
        			v = tmp;		// max
        			tmpChoice = s;	// argmax
        			// save best move for move ordering at the next iteration
        			bestPath.setElementAt(makeKey(s), maxDepth-1);
        		}
        	}
    	}
    	
    	return finalChoice;
    }
    
    private int alphabetaR(GameState state, int depth, int alpha, int beta) {
    	int player = state.getNextPlayer();
    	Vector<GameState> nextStates = new Vector<>();
    	int v;
    	
    	// check deadline
    	if (timeout(maxDepth - depth)) {
    		timeout = true;
    		return 0;	// end search
    	}
    	
    	//We retrieve the stored value for this node
    	Integer stored = currentPlayerStates.get(makeKey(state));
    	//If we have something stored, we return that value
    	if (stored != null) {
    		return stored;
    	}
    	
    	// fill next states
    	state.findPossibleMoves(nextStates);
    	
    	// cutoff test
    	if (depth == 0 || nextStates.isEmpty()) {
    		v = evaluate(state);
    		addToHash(state, v);
    		return v;
    	}
    	
    	// move ordering
    	moveOrdering(nextStates, depth);
    	
    	// it's me, I look for the maximum
    	if (player == whoAmI) {
    		v = Integer.MIN_VALUE;
    		for (GameState s : nextStates) {
    			int tmp = alphabetaR(s, depth-1, alpha, beta);
    			if (timeout)
    				return 0;	// end search
    			if (tmp > v) {
    				v = tmp;
    				// save best move for move ordering at the next iteration
    				bestPath.setElementAt(makeKey(s), depth-1);
    			}
    			if (tmp > alpha)
    				alpha = tmp;
    			if (beta <= alpha)
    				break;
    		}
    	}
    	// it's the opponent, he looks for the minimum
    	else {
    		v = Integer.MAX_VALUE;
    		for (GameState s : nextStates) {
    			int tmp = alphabetaR(s, depth-1, alpha, beta);
    			if (timeout)
    				return 0;	// end search
    			if (tmp < v) {
    				v = tmp;
    				// save best move for move ordering at the next iteration
    				bestPath.setElementAt(makeKey(s), depth-1);
    			}
    			if (tmp < beta)
    				beta = tmp;
    			if (beta <= alpha)
    				break;
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
    	//Compute the key of the symmetric state
    	key = getSymmetricState(key);
    	currentPlayerStates.put(key, value);
    	otherPlayerStates.put(key, -value);
    	//Compute the key of the opposed of the symmetric state (in which value is -value)
    	key = getOpposedState(key);
    	currentPlayerStates.put(key, -value);
    	otherPlayerStates.put(key, value);
    	//Compute the key of the opposed state (in which value is -value)
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
    		//Only the order of columns changes
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
    	//Beginning from bottom-right, we fill the contrary piece on top-left, to create the opposed state
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
    	//We also invert the player
    	player = player == "r" ? "w" : "r";
    	return result + " " + player;
    }
    
    private int evaluate(GameState state) {
    	int globalSum = 0;
    	int myPartialSum, othersPartialSum;
    	int i;
    	int piece;
    	
    	// terminal state, the result is certain
    	if (state.isEOG()) {
    		if (isWin(state))
    			return Integer.MAX_VALUE;
    		else if (isLoss(state))
    			return Integer.MIN_VALUE;
    		else
    			return 0;	// draw
    	}
    	
    	// evaluate
    	myPartialSum = othersPartialSum = 0; 
    	for (i = 0; i < GameState.NUMBER_OF_SQUARES; i++) {
    		piece = state.get(i);
    		if (piece == Constants.CELL_EMPTY)
    			continue;
    		
    		if ((piece & whoAmI) != 0) {
    			if ((piece & Constants.CELL_KING) != 0) {
    				myPartialSum += 5;
    			} else {    				
    				myPartialSum += 1; //getRelativeRow(piece, i);
    			}
    		} else {
    			if ((piece & Constants.CELL_KING) != 0) {
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
//    private int getRelativeRow(int piece, int position) {
//    	if ((piece & Constants.CELL_RED) != 0)
//    		return GameState.cellToRow(position);
//    	else
//    		return 7 - GameState.cellToRow(position);
//    }
    
    private boolean isWin(GameState state) {
    	return (whoAmI == Constants.CELL_RED && state.isRedWin()) ||
    			(whoAmI == Constants.CELL_WHITE && state.isWhiteWin());
    }
    
    private boolean isLoss(GameState state) {
    	return (whoAmI == Constants.CELL_RED && state.isWhiteWin()) ||
    			(whoAmI == Constants.CELL_WHITE && state.isRedWin());
    }
    
    private boolean timeout(int depth) {
    	return deadline.timeUntil() <= MARGIN_DEADLINE + TIME_TO_RETURN*depth;
    }
    
    private void moveOrdering(Vector<GameState> nextStates, int depth) {
    	String bestNextState = depth > 0 ? bestPath.elementAt(depth-1) : null;
    	Vector<GameState> states1, states2, states3, states4, states5;
    	Predicate<GameState> filter1, filter2, filter3;
    	
    	filter1 = s -> s.toMessage().equals(bestNextState);	// best move
    	filter2 = s -> s.getMove().isJump();	// jump move
    	filter3 = s -> isBecomingKing(s);		// become king

    	// first: best move from previous iteration
		states1 = nextStates.stream()
				.filter(filter1)
				.collect(Collectors.toCollection(Vector::new));
		Collections.shuffle(states1);
    	// second: jump moves for which the piece becomes king
    	states2 = nextStates.stream()
    			.filter(filter1.negate().and(filter2).and(filter3))
    			.collect(Collectors.toCollection(Vector::new));
    	Collections.shuffle(states2);
    	// third: the rest of the jump moves
    	states3 = nextStates.stream()
    			.filter(filter1.negate().and(filter2).and(filter3.negate()))
    			.collect(Collectors.toCollection(Vector::new));
    	Collections.shuffle(states3);
    	// fourth: normal moves for which the piece becomes king
    	states4 = nextStates.stream()
    			.filter(filter1.negate().and(filter2.negate()).and(filter3))
    			.collect(Collectors.toCollection(Vector::new));
    	Collections.shuffle(states4);
    	// fifth: the rest of the normal moves
    	states5 = nextStates.stream()
    			.filter(filter1.negate().and(filter2.negate()).and(filter3.negate()))
    			.collect(Collectors.toCollection(Vector::new));
    	Collections.shuffle(states5);
    	
    	nextStates.clear();
    	nextStates.addAll(states1);
    	nextStates.addAll(states2);
    	nextStates.addAll(states3);
    	nextStates.addAll(states4);
    	nextStates.addAll(states5);
    }
    
    private boolean isBecomingKing(GameState state) {
    	Move m = state.getMove();
    	int length = m.length();
    	if (length == 0)
    		return false;
    	return (state.get(m.at(length-1)) & Constants.CELL_KING) != 0;
    }
    
}
