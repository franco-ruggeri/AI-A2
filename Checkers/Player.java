import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Player {
	private static final int WEIGHT_KING = 5;
	private static final int WEIGHT_PIECE = 1;
	private static final long MARGIN_DEADLINE = (long) 1e8;	// 100 ms of margin
    private static final long TIME_TO_RETURN = (long) 1e5;  // 0.1 ms to return 1 level up in recursion
    private static final int INITIAL_DEPTH = 9;				// don't start from 1, too much time wasted
    private static final int NUMBER_OF_ROWS = 8;
	private static final int NUMBER_OF_COLUMNS = 4;
    
	private int whoAmI;			// red or white?
	
	// iterative deepening
	private Deadline deadline;
    private boolean timeout;    // set to true when deadline is almost reached
    private int currentDepth;  	// depth of current iteration
    
    // repeated state checking
    private HashMap<String, Integer> transpositionTable;
    
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
        
    	currentDepth = INITIAL_DEPTH-1;
    	timeout = false;
        while (!timeout) {
            // save result of the previous completed iteration
            finalChoice = tmpChoice;
            
            // prepare new iteration
            currentDepth++;
            transpositionTable = new HashMap<>();
            
            // move ordering
            moveOrdering(state, nextStates, currentDepth);
            
            // find action maximizing the "utility"
            for (int i=0; i<nextStates.size() && !timeout; i++) {
                GameState s = nextStates.elementAt(i);
                int tmp = alphabetaR(s, currentDepth-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (tmp > v) {
                    v = tmp;        // max
                    tmpChoice = s;  // argmax
                }
            }
        }
        
        return finalChoice;
    }
    
    private int alphabetaR(GameState state, int depth, int alpha, int beta) {
        int player = state.getNextPlayer();
        Vector<GameState> nextStates = new Vector<>();
        Integer v;
        String key;
        
        // check deadline
        if (timeout(currentDepth - depth)) {
            timeout = true;
            return 0;   // end search
        }
        
        // update stats
        nodeVisited++;
        
        // check repeated state
        key = getKey(state, depth);
        v = transpositionTable.get(key);
        if (v != null)
            return v;
        
        // fill next states
        state.findPossibleMoves(nextStates);
        
        // cutoff test
        if (depth == 0 || nextStates.isEmpty()) {
            v = evaluate(state);
            addKnownState(key, v);
            return v;
        }
        
        // move ordering
        moveOrdering(state, nextStates, depth);
        
        // it's me, I look for the maximum
        if (player == whoAmI) {
            v = Integer.MIN_VALUE;
            for (GameState s : nextStates) {
                int tmp = alphabetaR(s, depth-1, alpha, beta);
                if (timeout)
                	return 0;   // end search
                if (tmp > v)
                    v = tmp;
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
	                return 0;   // end search
                if (tmp < v)
                    v = tmp;
                if (tmp < beta)
                    beta = tmp;
                if (beta <= alpha)
                    break;
            }
        }
        
        // add known state for repeated state checking
        addKnownState(key, v);
        
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
    
    private void addKnownState(String key, int value) {
    	transpositionTable.put(key, value);
    	// add symmetric state (i.e. columns mirrored)
	    transpositionTable.put(getSymmetricState(key), value);
    }
    
    private String getKey(GameState state, int depth) {
    	String[] parts = state.toMessage().split(" ");
    	/*
    	 * It is supposed to be for transpositionTable, so it depends on:
    	 * - the position of the pieces (parts[0]).
    	 * - the player who has to move (parts[2]).
    	 * - the depth (an equal state but at a different depth should be expanded
    	 * 	 until the limit of depth because it can achieve a better estimate).
    	 */
    	return parts[0] + " " + parts[2] + " " + depth;
    }
    
    private String getSymmetricState(String key) {
    	String[] parts = key.split(" ");
    	String board = parts[0];
    	StringBuffer symmetricKey = new StringBuffer();
    	
    	// symmetric board
    	for (int i=0; i<NUMBER_OF_ROWS; i++)
    		for (int j=NUMBER_OF_COLUMNS-1; j>=0; j--)
    			symmetricKey.append(board.charAt(i*NUMBER_OF_COLUMNS + j));
    	
    	// symmetric key
    	for (int i=1; i<parts.length; i++)
    		symmetricKey.append(" ").append(parts[i]);
    	
    	return symmetricKey.toString();
    }
    
    private void moveOrdering(GameState state, Vector<GameState> nextStates, int depth) {
        Vector<GameState> jump, becomeKing, normal;
        Predicate<GameState> filterJump, filterNormal, filterKing;
        
        // filters
        filterJump = s -> s.getMove().isJump();
        filterNormal = s -> s.getMove().isNormal();
        filterKing = s -> hasBecomeKing(state, s);
        
        // first: jumps sorted by decreasing length
        jump = nextStates.stream()
                .filter(filterJump)
                .sorted((s1, s2) -> s2.getMove().length() - s1.getMove().length())
                .collect(Collectors.toCollection(Vector::new));
        // second: normal moves for which the piece becomes king
        becomeKing = nextStates.stream()
                .filter(filterNormal.and(filterKing))
                .collect(Collectors.toCollection(Vector::new));
        Collections.shuffle(becomeKing);
        // third: the rest of the normal moves
        normal = nextStates.stream()
                .filter(filterNormal.and(filterKing.negate()))
                .collect(Collectors.toCollection(Vector::new));
        Collections.shuffle(normal);
        
        nextStates.clear();
        nextStates.addAll(jump);
        nextStates.addAll(becomeKing);
        nextStates.addAll(normal);
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

