import java.util.*;

public class Player {
	private int whoAmI;
	private static final int DEPTH = 5;
	
    /**
     * Performs a move
     *
     * @param gameState
     *            the current state of the board
     * @param deadline
     *            time before which we must have returned
     * @return the next state the board is in after our move
     */
    public GameState play(final GameState gameState, final Deadline deadline) {
        whoAmI = gameState.getNextPlayer();
        return alphabeta(gameState, DEPTH);
    }
    
    private GameState alphabeta(GameState state, int depth) {
    	Vector<GameState> nextStates = new Vector<>();
    	int v = Integer.MIN_VALUE;
    	GameState choice = null;
    	
    	// fill next states
    	state.findPossibleMoves(nextStates);

    	// invalid depth
    	if (depth==0)
    		return null;
    	
    	// no possible moves, pass
    	if (nextStates.isEmpty())
    		return new GameState(state, new Move());

    	// find action maximizing the "utility"
    	for (GameState s : nextStates) {
    		int tmp = alphabetaR(s, depth-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
    		if (tmp > v) {
    			v = tmp;		// max
    			choice = s;		// argmax
    		}
    	}
    	
    	return choice;
    }
    
    private int alphabetaR(GameState state, int depth, int alpha, int beta) {
    	int player = state.getNextPlayer();
    	Vector<GameState> nextStates = new Vector<>();
    	int v;
    	
    	// fill next states
    	state.findPossibleMoves(nextStates);
    	
    	// cutoff test
    	if (depth == 0 || nextStates.isEmpty()) {
    		v = evaluate(state);
    	}
    	// it's me, I look for the maximum
    	else if (player == whoAmI) {
    		v = Integer.MIN_VALUE;
    		for (GameState s : nextStates) {
    			int tmp = alphabetaR(s, depth-1, alpha, beta);
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
    			if (tmp < v)
    				v = tmp;
    			if (tmp < beta)
    				beta = tmp;
    			if (beta <= alpha)
    				break;
    		}
    	}
    	
    	return v;
    }
    
    private class EvaluationData {
    	int[] rows, cols, diags;
    	
    	EvaluationData() {
    		rows = new int[GameState.BOARD_SIZE];
    		cols = new int[GameState.BOARD_SIZE];
        	diags = new int[2];
        	Arrays.fill(rows, 0);
        	Arrays.fill(cols, 0);
        	Arrays.fill(diags, 0);
    	}
    }
    
    private int evaluate(GameState state) {
    	EvaluationData me = new EvaluationData();
    	EvaluationData opponent = new EvaluationData();
    	
    	// terminal state, the result is certain
    	if (state.isEOG()) {
    		if (isWin(state))
    			return Integer.MAX_VALUE;
    		else if (isLoss(state))
    			return Integer.MIN_VALUE + 1; // +1 because -(Integer.MIN_VALUE + 1) = Integer.MAX_VALUE (MIN_VALUE is 1 unit smaller than -MAX_VALUE)
    		else
    			return 0;	// draw
    	}
    	
    	// fill evaluation data
    	for (int i=0; i<GameState.CELL_COUNT; i++) {
    		int mark = state.at(i);
    		if (mark == Constants.CELL_EMPTY)
    			continue;
    		
    		EvaluationData current = (mark==whoAmI ? me : opponent);
			int r = GameState.cellToRow(i);
    		int c = GameState.cellToCol(i);
    		current.rows[r]++;
    		current.cols[c]++;
    		if (r-c == 0)						// main diagonal
    			current.diags[0]++;
    		if (r+c == GameState.BOARD_SIZE-1)	// anti diagonal
    			current.diags[1]++;
    	}
    	
    	// count only lines that can lead to victory
    	int scoreMe = 0;
    	int scoreOpponent = 0;
    	for (int i=0; i<me.rows.length; i++) {
    		if (me.rows[i] == 0 || opponent.rows[i] == 0) {
    			scoreMe += me.rows[i];
    			scoreOpponent += opponent.rows[i];
    		}
    		if (me.cols[i] == 0 || opponent.cols[i] == 0) {
    			scoreMe += me.cols[i];
    			scoreOpponent += opponent.cols[i];
    		}
    	}
    	for (int i=0; i<me.diags.length; i++) {
    		if (me.diags[i] == 0 || opponent.diags[i] == 0) {
    			scoreMe += me.diags[i];
    			scoreOpponent += opponent.diags[i];
    		}
    	}
    	
    	return scoreMe - scoreOpponent;
    }
    
    private boolean isWin(GameState state) {
    	return (whoAmI == Constants.CELL_X && state.isXWin()) ||
    			(whoAmI == Constants.CELL_O && state.isOWin());
    }
    
    private boolean isLoss(GameState state) {
    	return (whoAmI == Constants.CELL_X && state.isOWin()) ||
    			(whoAmI == Constants.CELL_O && state.isXWin());
    }
    
}
