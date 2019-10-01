import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Player {
	private static final int WEIGHT_KING = 5;
	private static final int WEIGHT_PIECE = 1;
	private int whoAmI;		// red or white?
	
	// optimizations (on/off)
    private static final boolean ITERATIVE_DEEPENING = true;
    private static final boolean REPEATED_STATE_CHECKING = true;
    private static final boolean SYMMETRY_BREAKING = true;	// only with repeated state checking
    private static final boolean MOVE_ORDERING = true;
	
    // iterative deepening
    private static final long MARGIN_DEADLINE = (long) 1e8; // 100 ms of margin
    private static final long TIME_TO_RETURN = (long) 1e5;  // 0.1 ms to return 1 level up in recursion
    private static final int INITIAL_DEPTH = 8;				// don't start from 1, too much time wasted
    private Deadline deadline;
    private boolean timeout;    // set to true when deadline is almost reached
    private int currentDepth;  	// depth of current iteration
    
    // repeated state checking
    private static final int NUMBER_OF_ROWS = 8;
	private static final int NUMBER_OF_COLUMNS = 4;
    private HashMap<String, Integer> transpositionTable = new HashMap<>();
    
    // move ordering
    private Vector<String> bestPath = new Vector<>();
    
    // stats
    private long nodeVisited;
    
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
    	GameState choice;
    	
    	// init stats
    	nodeVisited = 0;
    	
    	// search move
        whoAmI = pState.getNextPlayer();
        deadline = pDue;
        choice = alphabeta(pState);
        
        // print stats
        System.err.println("Node visited: " + nodeVisited);
        System.err.println("Max depth reached: " + currentDepth);
        
        return choice;
    }
    
    private GameState alphabeta(GameState state) {
        Vector<GameState> nextStates = new Vector<>();
        int v = Integer.MIN_VALUE;
        GameState finalChoice = null;
        GameState tmpChoice = null;
        
        // fill next states
        state.findPossibleMoves(nextStates);
        
        // empty => pass
        if (nextStates.isEmpty())
            return new GameState(state, new Move());

        // no alternatives => useless search
        if (nextStates.size() == 1)
        	return nextStates.firstElement();
        
        if (ITERATIVE_DEEPENING) {
        	currentDepth = INITIAL_DEPTH-1;
        	timeout = false;
        	if (MOVE_ORDERING) {
        		bestPath.clear();
        		bestPath.setSize(INITIAL_DEPTH-1);
        	}
            while (!timeout) {
                // save result of the previous completed iteration
                finalChoice = tmpChoice;
                
                // prepare new iteration
                currentDepth++;
                if (MOVE_ORDERING)
                	bestPath.add(0, null);	// shift others to the right, i.e. element 0 is filled by the second-last deepest level
                if (REPEATED_STATE_CHECKING)
                	transpositionTable.clear();
                
                // move ordering
                if (MOVE_ORDERING)
                	moveOrdering(state, nextStates, currentDepth);
                
                // find action maximizing the "utility"
                for (int i=0; i<nextStates.size() && !timeout; i++) {
                    GameState s = nextStates.elementAt(i);
                    int tmp = alphabetaR(s, currentDepth-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    if (tmp > v) {
                        v = tmp;        // max
                        tmpChoice = s;  // argmax
                     // save best move for move ordering at the next iteration
                        if (MOVE_ORDERING)
                        	bestPath.setElementAt(getKey(s, currentDepth), currentDepth-1);
                    }
                }
            }
        } else {
        	currentDepth = INITIAL_DEPTH;
        	
        	// move ordering
        	if (MOVE_ORDERING) {
        		bestPath.clear();	// it is used in moveOrdering(), so fill it with null 
        		bestPath.setSize(INITIAL_DEPTH);
        		moveOrdering(state, nextStates, INITIAL_DEPTH);
        	}
        		
        	// find action maximizing the "utility"
        	for (GameState s : nextStates) {
        		int tmp = alphabetaR(s, currentDepth-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        		if (tmp > v) {
        			v = tmp;			// max
        			finalChoice = s;	// argmax
        		}
        	}
        }
        
        return finalChoice;
    }
    
    private int alphabetaR(GameState state, int depth, int alpha, int beta) {
        int player = state.getNextPlayer();
        Vector<GameState> nextStates = new Vector<>();
        Integer v;
        
        // check deadline
        if (ITERATIVE_DEEPENING) {
	        if (timeout(currentDepth - depth)) {
	            timeout = true;
	            return 0;   // end search
	        }
        }
        
        // update stats
        nodeVisited++;
        
        // check repeated state
        if (REPEATED_STATE_CHECKING) {
	        v = checkRepeatedState(state, depth);
	        if (v != null)
	            return v;
        }
        
        // fill next states
        state.findPossibleMoves(nextStates);
        
        // cutoff test
        if (depth == 0 || nextStates.isEmpty()) {
            v = evaluate(state);
            if (REPEATED_STATE_CHECKING)
            	addKnownState(state, depth, v);
            return v;
        }
        
        // move ordering
        if (MOVE_ORDERING)
        	moveOrdering(state, nextStates, depth);
        
        // it's me, I look for the maximum
        if (player == whoAmI) {
            v = Integer.MIN_VALUE;
            for (GameState s : nextStates) {
                int tmp = alphabetaR(s, depth-1, alpha, beta);
                if (ITERATIVE_DEEPENING)
                	if (timeout)
                		return 0;   // end search
                if (tmp > v) {
                    v = tmp;
                    // save best move for move ordering at the next iteration
                    if (MOVE_ORDERING)
                    	bestPath.setElementAt(getKey(s, depth), depth-1);
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
                if (ITERATIVE_DEEPENING)
		            if (timeout)
		                return 0;   // end search
                if (tmp < v) {
                    v = tmp;
                    // save best move for move ordering at the next iteration
                    if (MOVE_ORDERING)
                    	bestPath.setElementAt(getKey(s, depth), depth-1);
                }
                if (tmp < beta)
                    beta = tmp;
                if (beta <= alpha)
                    break;
            }
        }
        
        // add known state for repeated state checking
        if (REPEATED_STATE_CHECKING)
        	addKnownState(state, depth, v);
        
        return v;
    }
    
    private int evaluate(GameState state) {
    	int scoreMe = 0;
    	int scoreOpponent = 0;
    	int cell;
    	
    	// terminal state, the result is certain
        if (state.isEOG()) {
            if (isWin(state))
                return Integer.MAX_VALUE;
            else if (isLoss(state))
    			return Integer.MIN_VALUE + 1; // +1 because -(Integer.MIN_VALUE + 1) = Integer.MAX_VALUE (MIN_VALUE is 1 unit smaller than -MAX_VALUE)
            else
                return 0;   // draw
        }
    	
    	// pieces and kings
    	for (int i = 0; i < GameState.NUMBER_OF_SQUARES; i++) {
    		cell = state.get(i);
    		if (cell == Constants.CELL_EMPTY)
    			continue;
    		
    		if ((cell & whoAmI) != 0) {
    			if ((cell & Constants.CELL_KING) != 0)
    				scoreMe += WEIGHT_KING;
    			else
    				scoreMe += WEIGHT_PIECE;
    		} else {
    			if ((cell & Constants.CELL_KING) != 0)
    				scoreOpponent += WEIGHT_KING;
    			else
    				scoreOpponent += WEIGHT_PIECE;
    		}
    	}
    	
    	return scoreMe - scoreOpponent;
    }
    
    private boolean timeout(int depth) {
    	return deadline.timeUntil() <= MARGIN_DEADLINE + TIME_TO_RETURN*depth;
    }
    
    private void addKnownState(GameState state, int depth, int value) {
    	transpositionTable.put(getKey(state, depth), value);
    	// add symmetric state (i.e. columns mirrored)
    	if (SYMMETRY_BREAKING)
	    	transpositionTable.put(getKey(getSymmetricState(state), depth), value);
    }
    
    private Integer checkRepeatedState(GameState state, int depth) {
		return transpositionTable.get(getKey(state, depth));
	}
    
    private String getKey(GameState state, int depth) {
    	String[] parts = state.toMessage().split(" ");
    	/*
    	 * It depends on:
    	 * - the position of the pieces (parts[0])
    	 * - the player who has to move (parts[2])
    	 * - the depth (an equal state but at a different depth should be expanded
    	 * 	 until the limit of depth because it can achieve a better estimate)
    	 */
    	return parts[0] + " " + parts[2] + " " + depth;
    }
    
    private GameState getSymmetricState(GameState state) {
    	String[] parts = state.toMessage().split(" ");
    	String board = parts[0], newBoard = "";
    	StringBuffer newState = new StringBuffer();
    	
    	// prepare string of the symmetric board
    	for (int i=0; i<NUMBER_OF_ROWS; i++)
    		for (int j=NUMBER_OF_COLUMNS-1; j>=0; j--)
    			newBoard += board.charAt(i*NUMBER_OF_COLUMNS + j);
    	
    	// prepare complete string of the symmetric state
    	newState.append(newBoard);
    	for (int i=1; i<parts.length; i++)
    		newState.append(" ").append(parts[i]);
    	
    	return new GameState(newState.toString());
    }
    
    private void moveOrdering(GameState state, Vector<GameState> nextStates, int depth) {
        String bestNextState = depth > 0 ? bestPath.elementAt(depth-1) : null;
        Vector<GameState> states1, states2, states3, states4, states5;
        Predicate<GameState> filter1, filter2, filter3;
        
        filter1 = s -> getKey(s, depth).equals(bestNextState); 	// best move
        filter2 = s -> s.getMove().isJump();    				// jump move
        filter3 = s -> hasBecomeKing(state, s);       			// become king
        
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
    
    private boolean hasBecomeKing(GameState oldState, GameState newState) {
        Move oldMove = oldState.getMove(), newMove = newState.getMove();
        int oldLength = oldMove.length(), newLength = newMove.length();
        if (oldLength == 0 || newLength == 0)
            return false;
        
        boolean wasPiece = (oldState.get(oldMove.at(oldLength-1)) & Constants.CELL_KING) == 0;
        boolean isKing = (newState.get(newMove.at(newLength-1)) & Constants.CELL_KING) != 0;
        return wasPiece && isKing;
    }
    
    private boolean isWin(GameState state) {
        return (whoAmI == Constants.CELL_RED && state.isRedWin()) ||
                (whoAmI == Constants.CELL_WHITE && state.isWhiteWin());
    }
    
    private boolean isLoss(GameState state) {
        return (whoAmI == Constants.CELL_RED && state.isWhiteWin()) ||
                (whoAmI == Constants.CELL_WHITE && state.isRedWin());
    }
    
}

