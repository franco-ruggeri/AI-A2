import java.util.*;

public class Player {
	private int whoAmI;
	private static final int DEPTH = 2;
	private EvaluationData me, opponent;
	
    private class EvaluationData {
    	int[][]  parallelX, parallelY, parallelZ;
    	int[][] diagonalsXY, diagonalsXZ, diagonalsYZ;
    	int[] mainDiagonals;
    	int sum;
    	
    	EvaluationData() {
    		// init
    		parallelX = new int[GameState.BOARD_SIZE][GameState.BOARD_SIZE];
    		parallelY = new int[GameState.BOARD_SIZE][GameState.BOARD_SIZE];
    		parallelZ = new int[GameState.BOARD_SIZE][GameState.BOARD_SIZE];
    		diagonalsXY = new int[GameState.BOARD_SIZE][2];
    		diagonalsXZ = new int[GameState.BOARD_SIZE][2];
    		diagonalsYZ = new int[GameState.BOARD_SIZE][2];
    		mainDiagonals = new int[4];
    		
    		// clear
    		for (int i=0; i<parallelX.length; i++) {
    			Arrays.fill(parallelX[i], 0);
    			Arrays.fill(parallelY[i], 0);
    			Arrays.fill(parallelZ[i], 0);
    		}
    		for (int i=0; i<diagonalsXY.length; i++) {
    			Arrays.fill(diagonalsXY[i], 0);
    			Arrays.fill(diagonalsXZ[i], 0);
    			Arrays.fill(diagonalsYZ[i], 0);
    		}
    		Arrays.fill(mainDiagonals, 0);
    		sum = 0;
    	}
    }
	
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

    	// init evaluation data
    	me = new EvaluationData();
    	opponent = new EvaluationData();
    	for (int i=0; i<GameState.CELL_COUNT; i++)
    		updateEvaluationData(state, i);
    	
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
    			updateEvaluationData(state, s.getMove().at(0));
    			int tmp = alphabetaR(s, depth-1, alpha, beta);
    			backtrackEvaluationData(state, s.getMove().at(0));
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
    			updateEvaluationData(state, s.getMove().at(0));
    			int tmp = alphabetaR(s, depth-1, alpha, beta);
    			backtrackEvaluationData(state, s.getMove().at(0));
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
    
    private int evaluate(GameState state) {
    	// terminal state, the result is certain
    	if (state.isEOG()) {
    		if (isWin(state))
    			return Integer.MAX_VALUE;
    		else if (isLoss(state))
    			return Integer.MIN_VALUE;
    		else
    			return 0;	// draw
    	}
    	
    	// estimate
    	return me.sum - opponent.sum;
    }
    
    private void updateEvaluationData(GameState state, int cell) {
    	int mark = state.at(cell);
		
		int x = GameState.cellToRow(cell);
		int y = GameState.cellToCol(cell);
		int z = GameState.cellToLay(cell);
		
		EvaluationData current = (mark==whoAmI ? me : opponent);
		EvaluationData other = (mark==whoAmI ? opponent : me);
		
		// orthogonal rows
		if (other.parallelX[y][z] > 0) {
			other.sum -= other.parallelX[y][z];
			other.parallelX[y][z] = 0;
		} else {
			current.parallelX[y][z]++;
			current.sum++;
		}
		if (other.parallelY[x][z] > 0) {
			other.sum -= other.parallelY[x][z];
			other.parallelY[x][z] = 0;
		} else {
			current.parallelY[y][z]++;
			current.sum++;
		}
		if (other.parallelZ[x][y] > 0) {
			other.sum -= other.parallelZ[x][y];
			other.parallelZ[x][y] = 0;
		} else {
			current.parallelZ[x][y]++;
			current.sum++;
		}
		
		// diagonals
		if (x-y == 0) {
			if (other.diagonalsXY[z][0] > 0) {
				other.sum -= other.diagonalsXY[z][0];
				other.diagonalsXY[z][0] = 0;
			} else {
				current.diagonalsXY[z][0]++;
				current.sum++;
			}
			
			// main diagonals
			if (x == z) {
				if (other.mainDiagonals[0] > 0) {
					other.sum -= other.mainDiagonals[0];
					other.mainDiagonals[0] = 0;
				} else {
					current.mainDiagonals[0]++;
					current.sum++;
				}
			} else if (x+z == GameState.BOARD_SIZE-1) {
				if (other.mainDiagonals[1] > 0) {
					other.sum -= other.mainDiagonals[1];
					other.mainDiagonals[1] = 0;
				} else {
					current.mainDiagonals[1]++;
					current.sum++;
				}
			}
		} else if (x+y == GameState.BOARD_SIZE-1) {
			if (other.diagonalsXY[z][1] > 0) {
				other.sum -= other.diagonalsXY[z][1];
				other.diagonalsXY[z][1] = 0;
			} else {
				current.diagonalsXY[z][1]++;
				current.sum++;
			}
			
			// main diagonals
			if (x == z) {
				if (other.mainDiagonals[2] > 0) {
					other.sum -= other.mainDiagonals[2];
					other.mainDiagonals[2] = 0;
				} else {
					current.mainDiagonals[2]++;
					current.sum++;
				}
			} else if (x+z == GameState.BOARD_SIZE-1) {
				if (other.mainDiagonals[3] > 0) {
					other.sum -= other.mainDiagonals[3];
					other.mainDiagonals[3] = 0;
				} else {
					current.mainDiagonals[3]++;
					current.sum++;
				}
			}
		}
		if (x-z == 0) {
			if (other.diagonalsXZ[y][0] > 0) {
				other.sum -= other.diagonalsXZ[y][0];
				other.diagonalsXZ[y][0] = 0;
			} else {
				current.sum++;
				current.diagonalsXZ[y][0]++;
			}
		} else if (x+z == GameState.BOARD_SIZE-1) {
			if (other.diagonalsXZ[y][1] > 0) {
				other.sum -= other.diagonalsXZ[y][1];
				other.diagonalsXZ[y][1] = 0;
			} else {
				current.diagonalsXZ[y][1]++;
				current.sum++;
			}
		}
		if (y-z == 0) {
			if (other.diagonalsYZ[x][0] > 0) {
				other.sum -= other.diagonalsYZ[x][0];
				other.diagonalsYZ[x][0] = 0;
			} else {
				current.diagonalsYZ[x][0]++;
				current.sum++;
			}
		} else if (y+z == GameState.BOARD_SIZE-1) {
			if (other.diagonalsYZ[x][1] > 0) {
				other.sum -= other.diagonalsYZ[x][1];
				other.diagonalsYZ[x][1] = 0;
			} else {
				current.diagonalsYZ[x][1]++;
				current.sum++;
			}
		}
    }
    
    private void backtrackEvaluationData(GameState state, int cell) {
    	// TODO: aggiungere main diagonals
    	
    	int mark = state.at(cell);
    	int otherMark = (mark==Constants.CELL_X ? Constants.CELL_O : Constants.CELL_X);
		
		int x = GameState.cellToRow(cell);
		int y = GameState.cellToCol(cell);
		int z = GameState.cellToLay(cell);
		
		EvaluationData current = (mark==whoAmI ? me : opponent);
		EvaluationData other = (mark==whoAmI ? opponent : me);
		
		// the cell has become empty
		if (state.at(cell) != Constants.CELL_EMPTY)
			throw new RuntimeException("Non-empty cell during backtrack.");
		
		// orthogonal rows
		if (current.parallelX[y][z] > 0) {
			current.parallelX[y][z]--;
			current.sum--;
		} else if (other.parallelX[y][z] == 0) {	// it may have been cleared, re-update
			int tmpSum = 0;
			boolean foundOther = false;
			for (int i=0; i<GameState.BOARD_SIZE && !foundOther; i++) {
				if (state.at(i, y, z) == otherMark) {
					other.parallelX[y][z]++;
					tmpSum++;
				} else {
					foundOther = true;
				}
			}
			if (!foundOther)
				other.sum += tmpSum;
			else
				other.parallelX[y][z] = 0;
		}
		if (current.parallelY[x][z] > 0) {
			current.parallelY[x][z]--;
			current.sum--;
		} else if (other.parallelY[x][z] == 0) {
			int tmpSum = 0;
			boolean foundOther = false;
			for (int i=0; i<GameState.BOARD_SIZE && !foundOther; i++) {
				if (state.at(x, i, z) == otherMark) {
					other.parallelY[x][z]++;
					tmpSum++;
				} else {
					foundOther = true;
				}
			}
			if (!foundOther)
				other.sum += tmpSum;
			else
				other.parallelY[x][z] = 0;
		}
		if (current.parallelZ[x][y] > 0) {
			current.parallelZ[x][y]--;
			current.sum--;
		} else if (other.parallelZ[x][y] == 0) {
			int tmpSum = 0;
			boolean foundOther = false;
			for (int i=0; i<GameState.BOARD_SIZE && !foundOther; i++) {
				if (state.at(x, y, i) == otherMark) {
					other.parallelZ[x][y]++;
					tmpSum++;
				} else {
					foundOther = true;
				}
			}
			if (!foundOther)
				other.sum += tmpSum;
			else
				other.parallelY[x][y] = 0;
		}
		
		// diagonals
		if (x-y == 0) {
			if (current.diagonalsXY[z][0] > 0) {
				current.diagonalsXY[z][0]--;
				current.sum--;
			} else if (other.diagonalsXY[z][0] == 0) {
				int tmpSum = 0;
				boolean foundOther = false;
				for (int i=0; i<GameState.BOARD_SIZE && !foundOther; i++) {
					if (state.at(i, i, z) == otherMark) {
						other.diagonalsXY[z][0]++;
						tmpSum++;
					}
				}
				if (!foundOther)
					other.sum += tmpSum;
				else
					other.diagonalsXY[z][0] = 0;
			}
		} else if (x+y == GameState.BOARD_SIZE-1) {
			if (current.diagonalsXY[z][1] > 0) {
				current.diagonalsXY[z][1]--;
				current.sum--;
			} else if (other.diagonalsXY[z][1] == 0) {
				int tmpSum = 0;
				boolean foundOther = false;
				for (int i=0; i<GameState.BOARD_SIZE && !foundOther; i++) {
					if (state.at(i, GameState.BOARD_SIZE-1-i, z) == otherMark) {
						other.diagonalsXY[z][1]++;
						tmpSum++;
					}
				}
				if (!foundOther)
					other.sum += tmpSum;
				else
					other.diagonalsXY[z][1] = 0;
			}
		}
		if (x-z == 0) {
			if (current.diagonalsXZ[y][0] > 0) {
				current.diagonalsXZ[y][0]--;
				current.sum--;
			} else if (other.diagonalsXZ[y][0] == 0) {
				int tmpSum = 0;
				boolean foundOther = false;
				for (int i=0; i<GameState.BOARD_SIZE && !foundOther; i++) {
					if (state.at(i, y, i) == otherMark) {
						other.diagonalsXZ[y][0]++;
						tmpSum++;
					}
				}
				if (!foundOther)
					other.sum += tmpSum;
				else
					other.diagonalsXZ[y][0] = 0;
			}
		} else if (x+z == GameState.BOARD_SIZE-1) {
			if (current.diagonalsXZ[y][1] > 0) {
				current.diagonalsXZ[y][1]--;
				current.sum--;
			} else if (other.diagonalsXZ[y][1] == 0) {
				int tmpSum = 0;
				boolean foundOther = false;
				for (int i=0; i<GameState.BOARD_SIZE && !foundOther; i++) {
					if (state.at(i, y, GameState.BOARD_SIZE-1-i) == otherMark) {
						other.diagonalsXZ[y][1]++;
						tmpSum++;
					}
				}
				if (!foundOther)
					other.sum += tmpSum;
				else
					other.diagonalsXZ[y][1] = 0;
			}
		}
		if (y-z == 0) {
			if (current.diagonalsYZ[y][0] > 0) {
				current.diagonalsYZ[y][0]--;
				current.sum--;
			} else if (other.diagonalsYZ[y][0] == 0) {
				int tmpSum = 0;
				boolean foundOther = false;
				for (int i=0; i<GameState.BOARD_SIZE && !foundOther; i++) {
					if (state.at(x, i, i) == otherMark) {
						other.diagonalsYZ[x][0]++;
						tmpSum++;
					}
				}
				if (!foundOther)
					other.sum += tmpSum;
				else
					other.diagonalsYZ[x][0] = 0;
			}
		} else if (y+z == GameState.BOARD_SIZE-1) {
			if (current.diagonalsYZ[y][1] > 0) {
				current.diagonalsYZ[y][1]--;
				current.sum--;
			} else if (other.diagonalsYZ[y][1] == 0) {
				int tmpSum = 0;
				boolean foundOther = false;
				for (int i=0; i<GameState.BOARD_SIZE && !foundOther; i++) {
					if (state.at(x, i, GameState.BOARD_SIZE-1-i) == otherMark) {
						other.diagonalsYZ[x][1]++;
						tmpSum++;
					}
				}
				if (!foundOther)
					other.sum += tmpSum;
				else
					other.diagonalsYZ[x][1] = 0;
			}
		}
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
