import java.util.*;

public class Player {
	private int whoAmI;
	private static final int DEPTH = 1;
	private static final int POWER = 4;
	
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
    	int[][]  parallelX, parallelY, parallelZ;
    	int[][] diagonalsXY, diagonalsXZ, diagonalsYZ;
    	int[] mainDiagonals;
    	
    	EvaluationData() {
    		parallelX = new int[GameState.BOARD_SIZE][GameState.BOARD_SIZE];
    		parallelY = new int[GameState.BOARD_SIZE][GameState.BOARD_SIZE];
    		parallelZ = new int[GameState.BOARD_SIZE][GameState.BOARD_SIZE];
    		diagonalsXY = new int[GameState.BOARD_SIZE][2];
    		diagonalsXZ = new int[GameState.BOARD_SIZE][2];
    		diagonalsYZ = new int[GameState.BOARD_SIZE][2];
    		mainDiagonals = new int[4];
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
    		int x = GameState.cellToRow(i);
    		int y = GameState.cellToCol(i);
    		int z = GameState.cellToLay(i);
    		
    		// orthogonal rows
    		current.parallelX[y][z]++;
    		current.parallelY[x][z]++;
    		current.parallelZ[x][y]++;
    		
    		// diagonals
    		if (x == y)
    			current.diagonalsXY[z][0]++;
    		if (x+y == GameState.BOARD_SIZE-1)
    			current.diagonalsXY[z][1]++;
    		if (x == z)
    			current.diagonalsXZ[y][0]++;
    		if (x+z == GameState.BOARD_SIZE-1)
    			current.diagonalsXZ[y][1]++;
    		if (y == z)
    			current.diagonalsYZ[x][0]++;
    		if (y+z == GameState.BOARD_SIZE-1)
    			current.diagonalsYZ[x][1]++;
    		
    		// main diagonals
    		if (x == y && y == z)
    			current.mainDiagonals[0]++;
    		if (x == y && z == GameState.BOARD_SIZE-1-x)
    			current.mainDiagonals[1]++;
    		if (x+y == GameState.BOARD_SIZE-1 && x == z)
    			current.mainDiagonals[2]++;
    		if (x+y == GameState.BOARD_SIZE-1 && y == z)
    			current.mainDiagonals[3]++;
    	}
    	
    	// count only lines that can lead to victory
    	int score = 0;
    	for (int i=0; i<me.parallelX.length; i++) {
    		for (int j=0; j<me.parallelX[i].length; j++) {
    			if (me.parallelX[i][j] == 0 || opponent.parallelX[i][j] == 0) {
    				int tmp = me.parallelX[i][j] - opponent.parallelX[i][j];
    				score += tmp  < 0 ? -Math.pow(tmp, POWER) : Math.pow(tmp, POWER);
    			}
    			if (me.parallelY[i][j] == 0 || opponent.parallelY[i][j] == 0) {
    				int tmp = me.parallelY[i][j] - opponent.parallelY[i][j];
    				score += tmp < 0 ? -Math.pow(tmp, POWER) : Math.pow(tmp, POWER);
    			}
    			if (me.parallelZ[i][j] == 0 || opponent.parallelZ[i][j] == 0) {
    				int tmp = me.parallelZ[i][j] - opponent.parallelZ[i][j];
    				score += tmp < 0 ? -Math.pow(tmp, POWER) : Math.pow(tmp, POWER);
    			}
    		}
    	}
    	for (int i=0; i<me.diagonalsXY.length; i++) {
    		for (int j=0; j<me.diagonalsXY[i].length; j++) {
    			if (me.diagonalsXY[i][j] == 0 || opponent.diagonalsXY[i][j] == 0) {
    				int tmp = me.diagonalsXY[i][j] - opponent.diagonalsXY[i][j];
    				score += tmp < 0 ? -Math.pow(tmp, POWER) : Math.pow(tmp, POWER);
    			}
    			if (me.diagonalsXZ[i][j] == 0 || opponent.diagonalsXZ[i][j] == 0) {
    				int tmp = me.diagonalsXZ[i][j] - opponent.diagonalsXZ[i][j];
    				score += tmp < 0 ? -Math.pow(tmp, POWER) : Math.pow(tmp, POWER);
    			}
    			if (me.diagonalsYZ[i][j] == 0 || opponent.diagonalsYZ[i][j] == 0) {
    				int tmp = me.diagonalsYZ[i][j] - opponent.diagonalsYZ[i][j];
    				score += tmp < 0 ? -Math.pow(tmp, POWER) : Math.pow(tmp, POWER);
    			}
    		}
    	}
    	for (int i=0; i<me.mainDiagonals.length; i++) {
    		if (me.mainDiagonals[i] == 0 || opponent.mainDiagonals[i] == 0) {
    			int tmp = me.mainDiagonals[i] - opponent.mainDiagonals[i];
				score += tmp < 0 ? -Math.pow(tmp, POWER) : Math.pow(tmp, POWER);
    		}
    	}

    	return score;
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
