import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Represents a game state with a 8x8 checkers board.
 *
 * Cells are numbered as follows:
 *
 *    col 0  1  2  3  4  5  6  7
 * row  -------------------------
 *  0  |     0     1     2     3 |  0
 *  1  |  4     5     6     7    |  1
 *  2  |     8     9    10    11 |  2
 *  3  | 12    13    14    15    |  3
 *  4  |    16    17    18    19 |  4
 *  5  | 20    21    22    23    |  5
 *  6  |    24    25    26    27 |  6
 *  7  | 28    29    30    31    |  7
 *      -------------------------
 *        0  1  2  3  4  5  6  7
 *
 * The starting board looks like this:
 *
 *    col 0  1  2  3  4  5  6  7
 * row  -------------------------
 *  0  |    rr    rr    rr    rr |  0
 *  1  | rr    rr    rr    rr    |  1
 *  2  |    rr    rr    rr    rr |  2
 *  3  | ..    ..    ..    ..    |  3
 *  4  |    ..    ..    ..    .. |  4
 *  5  | ww    ww    ww    ww    |  5
 *  6  |    ww    ww    ww    ww |  6
 *  7  | ww    ww    ww    ww    |  7
 *      -------------------------
 *        0  1  2  3  4  5  6  7
 *
 * The red player starts from the top of the board (row 0,1,2)
 * The white player starts from the bottom of the board (row 5,6,7),
 * Red moves first.
 * 
 * Note that there is one way of representing the cells with one number and one
 * way of representing them with two. You may use either one.
 */
public class GameState {
  public static final int NUMBER_OF_SQUARES = 32; // 32 valid squares
  public static final int PIECES_PER_PLAYER = 12; // 12 pieces per player
  public static final int MOVES_UNTIL_DRAW = 50; // 25 moves per player

  private int[] mCell = new int[GameState.NUMBER_OF_SQUARES];
  private int mMovesUntilDraw;
  private int mNextPlayer;
  private Move mLastMove;

  /**
   * Initialises the board to the starting position.
   */
  public GameState() {
    /* Initialise the board */
    for(int i = 0; i < GameState.PIECES_PER_PLAYER; i++) {
      this.mCell[i] = Constants.CELL_RED;
      this.mCell[NUMBER_OF_SQUARES-1-i] = Constants.CELL_WHITE;
    }

    for(int i = GameState.PIECES_PER_PLAYER;
        i < GameState.NUMBER_OF_SQUARES - GameState.PIECES_PER_PLAYER;
        i++) {
      this.mCell[i] = Constants.CELL_EMPTY;
    }

    // Initialise move related variables
    this.mLastMove = new Move(Move.MOVE_BOG);
    this.mMovesUntilDraw = GameState.MOVES_UNTIL_DRAW;
    this.mNextPlayer = Constants.CELL_RED;
  }

  /**
   * Constructs a board from a message string.
   *
   * @param pMessage the compact string representation of the state
   */
  public GameState(final String pMessage) {
    // Split the message with a string
    StringTokenizer st = new StringTokenizer(pMessage);

    String board, last_move, next_player;
    int moves_left;

    board = st.nextToken();
    last_move = st.nextToken();
    next_player = st.nextToken();
    moves_left = Integer.parseInt(st.nextToken());

    /* Sanity checks. If any of these fail, something has gone horribly
     * wrong. */
    assert(board.length() == GameState.NUMBER_OF_SQUARES);
    assert(next_player.length() == 1);
    assert(moves_left >= 0 && moves_left < 256);

    // Parse the board
    for (int i = 0; i < GameState.NUMBER_OF_SQUARES; i++) {
      if (board.charAt(i) == Constants.MESSAGE_SYMBOLS[Constants.CELL_EMPTY]) {
        this.mCell[i] = Constants.CELL_EMPTY;
      } else if (board.charAt(i) == Constants.MESSAGE_SYMBOLS[Constants.CELL_RED]) {
        this.mCell[i] = Constants.CELL_RED;
      } else if (board.charAt(i) == Constants.MESSAGE_SYMBOLS[Constants.CELL_WHITE]) {
        this.mCell[i] = Constants.CELL_WHITE;
      } else if (board.charAt(i) == Constants.MESSAGE_SYMBOLS[Constants.CELL_RED | Constants.CELL_KING]) {
        this.mCell[i] = Constants.CELL_RED | Constants.CELL_KING;
      } else if (board.charAt(i) == Constants.MESSAGE_SYMBOLS[Constants.CELL_WHITE | Constants.CELL_KING]) {
        this.mCell[i] = Constants.CELL_WHITE | Constants.CELL_KING;
      } else {
        // ???
        //assert("Invalid cell" && false);
      }
    }

    // Parse last move
    this.mLastMove = new Move(last_move);

    // Parse next player
    if (next_player.charAt(0) == Constants.MESSAGE_SYMBOLS[Constants.CELL_EMPTY]) {
      mNextPlayer = Constants.CELL_EMPTY;
    } else if (next_player.charAt(0) == Constants.MESSAGE_SYMBOLS[Constants.CELL_RED]) {
      mNextPlayer = Constants.CELL_RED;
    } else if (next_player.charAt(0) == Constants.MESSAGE_SYMBOLS[Constants.CELL_WHITE]) {
      mNextPlayer = Constants.CELL_WHITE;
    } else if (next_player.charAt(0) == Constants.MESSAGE_SYMBOLS[Constants.CELL_RED | Constants.CELL_KING]) {
      mNextPlayer = Constants.CELL_RED | Constants.CELL_KING;
    } else if (next_player.charAt(0) == Constants.MESSAGE_SYMBOLS[Constants.CELL_WHITE | Constants.CELL_KING]) {
      mNextPlayer = Constants.CELL_WHITE | Constants.CELL_KING;
    } else {
      // ???
      //assert("Invalid next player" && false);
    }

    // Set number of moves left until draw
    this.mMovesUntilDraw = moves_left;
  }

  /**
   * Constructs a board which is the result of applying move pMove to board 
   * pRH.
   *
   * @param pRH the starting board position
   * @param pMove the movement to perform
   * @see DoMove()
   */
  public GameState(final GameState pRH, final Move pMove) {
    /* Copy board */
    this.mCell = pRH.mCell.clone();

    /* Copy move status */
    this.mMovesUntilDraw = pRH.mMovesUntilDraw;
    this.mNextPlayer   = pRH.mNextPlayer;
    this.mLastMove     = pRH.mLastMove;

    /* Perform move */
    this.doMove(pMove);
  }

  /**
   * Constructs a state that is the result of rotating the board 180 degrees 
   * and swapping colours.
   *
   */
  GameState reversed() {
    /* Create new GameState */
    GameState result = new GameState();
    for (int i = 0; i < NUMBER_OF_SQUARES; i++) {
      if (this.mCell[31-i] == Constants.CELL_EMPTY) {
        result.mCell[i] = Constants.CELL_EMPTY;
      } else {
        result.mCell[i] = this.mCell[31-i] ^
        			(Constants.CELL_RED | Constants.CELL_WHITE);
      }
    }

    result.mLastMove = this.mLastMove.reversed();
    result.mNextPlayer = this.mNextPlayer ^
    			(Constants.CELL_RED | Constants.CELL_WHITE);
    result.mMovesUntilDraw = this.mMovesUntilDraw;
    return result;
  }

  /**
   * Gets the content of a cell in the board.
   *
   * This function returns a byte representing the contents of the cell,
   * using the enumeration values in ECell
   *
   * For example, to check if cell 23 contains a white piece, you would check if
   *
   * (lBoard.At(23)&CELL_WHITE)
   *
   * to check if it is a red piece,
   *
   *   (lBoard.At(23)&CELL_RED)
   *
   * and to check if it is a king, you would check if
   *
   *   (lBoard.At(23)&CELL_KING)
   */
  int get(int pPos) {
  	/* Sanity checks. If any of these fail, something has gone horribly
     * wrong. */
    assert(pPos >= 0);
    assert(pPos < NUMBER_OF_SQUARES);
    return mCell[pPos];
  }

  /**
   * Sets the content of a cell in the board.
   */
  void set(int pPos, int v) {
  	/* Sanity checks. If any of these fail, something has gone horribly
     * wrong. */
    assert(pPos >= 0);
    assert(pPos < NUMBER_OF_SQUARES);
    mCell[pPos] = v;
  }

  /**
   * Gets the content of a cell in the board, from row and column number.
   *
   * Rows are numbered (0 to 7) from the upper row in the board,
   * as seen by the player this program is playing.
   *
   * Columns are numbered starting from the left (also 0 to 7).
   *
   * Cells corresponding to white squares in the board, which will
   * never contain a piece, always return CELL_INVALID
   *
   * If the cell falls outside of the board, return CELL_INVALID
   *
   * You can use it in the same way as the version that requires a cell index
   */
  int get(int pR, int pC) {
    if (pR < 0 || pR > 7 || pC < 0 || pC > 7) {
      return Constants.CELL_INVALID;
    }

    if ((pR & 1) == (pC & 1)) {
      return Constants.CELL_INVALID;
    }

    return this.mCell[pR * 4 + (pC >> 1)];
  }

  /**
   * Sets the content of a cell in the board, from row and column number.
   * 
   * Note that this is a private function.
   */
  private void set(int pR, int pC, int v) {
    /* This is a bit ugly, but is useful for the implementation of
     * FindPossibleMoves. It won't affect single-threaded programs
     * and you're not allowed to use threads anyway. */
    this.mCell[pR * 4 + (pC >> 1)] = v;
  }


  /**
   * Gets the row corresponding to an index in the array representation of
   * the board.
   * 
   * @param pCell
   * @return the row corresponding to a cell index
   */
  public static int cellToRow(int pCell) {
    return ((pCell) >> 2);
  }

  /**
   * Gets the column corresponding to an index in the array representation of
   * the board.
   * 
   * @param pCell
   * @return the col corresponding to a cell index
   */
  public static int cellToCol(int pCell) {
    int lC = ((pCell) & 3) << 1;
	//int lC = (pCell) & 3;
	
    if ( 0 == ((pCell) & 4) ) {
      lC++;
    }
	
    return lC;
  }

  /**
   * Gets the index in the array representation of the board which corresponds
   * to a certain row and column number.
   * 
   * It doesn't check if it corresponds to a black square in the board,
   * or if it falls within the board.
   *
   * If it doesn't, the result is undefined, and the program is likely
   * to crash
   *
   * @param pRow
   * @param pCol
   * @return the cell corresponding to a row and col
   */
  static int rowColToCell(int pRow, int pCol) {
    return (pRow * 4 + (pCol >> 1));
  }

  private boolean tryJump(Vector<Move> pMoves, int pR, int pC, boolean pKing,
      int[] pBuffer) {
    return this.tryJump(pMoves, pR, pC, pKing, pBuffer, 0);
  }
  
  /**
   * Tries to make a jump (capture a piece) from a certain position of the
   * board.
   *
   * @param pMoves a vector where the valid moves will be inserted
   * @param pOther the ECell code corresponding to the player who is not 
   * 		making the move
   * @param pR the row of the cell we are capturing from
   * @param pC the column we are capturing from
   * @param pKing true if the capturing piece is a king
   * @param pBuffer a buffer where the list of jump positions is
   * 		inserted (for multiple jumps)
   * @param pDepth the number of multiple jumps before this attempt
   */
  private boolean tryJump(Vector<Move> pMoves, int pR, int pC, boolean pKing, 
  		int[] pBuffer, int pDepth) {
    /* Remove the capturing piece temporarily */
    int lOldSelf = this.get(pR, pC);
    this.set(pR, pC, Constants.CELL_EMPTY);

    pBuffer[pDepth] = GameState.rowColToCell(pR, pC);

    boolean lFound = false;
    int lOther = mNextPlayer ^ (Constants.CELL_WHITE | Constants.CELL_RED);

    // Try capturing downwards
    if(mNextPlayer == Constants.CELL_RED || pKing) {
      // Try capturing left
      if( 0 != (this.get(pR+1, pC-1) & lOther) &&
    		  	this.get(pR+2,pC-2) == Constants.CELL_EMPTY) {
        lFound = true;
        int lOldValue = get(pR+1, pC-1);
        this.set(pR+1, pC-1, Constants.CELL_EMPTY);
        this.tryJump(pMoves, pR+2, pC-2, pKing, pBuffer, pDepth + 1);
        this.set(pR+1, pC-1, lOldValue);
      }

      // Try capturing right
      if( 0 != (this.get(pR+1, pC+1) & lOther) &&
    		  	this.get(pR+2,pC+2) == Constants.CELL_EMPTY) {
        lFound = true;
        int lOldValue = this.get(pR+1, pC+1);
        this.set(pR+1, pC+1, Constants.CELL_EMPTY);
        this.tryJump(pMoves, pR+2, pC+2, pKing, pBuffer, pDepth + 1);
        this.set(pR+1, pC+1, lOldValue);
      }
    }

    // Try capturing upwards
    if(mNextPlayer == Constants.CELL_WHITE || pKing) {
      // Try capturing left
      if( 0 != (this.get(pR-1, pC-1) & lOther) &&
    		  	this.get(pR-2, pC-2) == Constants.CELL_EMPTY) {
        lFound = true;
        int lOldValue = this.get(pR-1, pC-1);
        this.set(pR-1, pC-1, Constants.CELL_EMPTY);
        this.tryJump(pMoves, pR-2, pC-2, pKing, pBuffer, pDepth + 1);
        this.set(pR-1, pC-1, lOldValue);
      }
      // Try capturing right
      if( 0 != (this.get(pR-1, pC+1) & lOther) &&
    		  	this.get(pR-2, pC+2) == Constants.CELL_EMPTY) {
        lFound = true;
        int lOldValue = this.get(pR-1, pC+1);
        this.set(pR-1, pC+1, Constants.CELL_EMPTY);
        this.tryJump(pMoves, pR-2, pC+2, pKing, pBuffer, pDepth + 1);
        this.set(pR-1, pC+1, lOldValue);
      }
    }

    /* Restore the capturing piece */
    this.set(pR, pC, lOldSelf);

    if(!lFound && pDepth > 0) {
      Vector<Integer> tmp = new Vector<Integer>();
      for (int z : pBuffer) {
        tmp.add(z);
      }
      pMoves.add(new Move(tmp, pDepth+1));
    }
    return lFound;
  }

  /**
   * Tries to make a move from a certain position, and inserts valid move
   * choices into a vector.
   *
   * @param pMoves vector where the valid moves will be inserted
   * @param pCell the cell where the move is tried from
   * @param pOther the ECell code corresponding to the player
   *     who is not making the move
   * @param pKing true if the piece is a king
   */
  void tryMove(Vector<Move> pMoves, int pCell, boolean pKing) {
    int lR = GameState.cellToRow(pCell);
    int lC = GameState.cellToCol(pCell);
    // Try moving downwards
    if(mNextPlayer == Constants.CELL_RED || pKing) {
      // Try moving right
      if(this.get(lR+1,lC-1) == Constants.CELL_EMPTY) {
        pMoves.add(new Move(pCell, GameState.rowColToCell(lR+1, lC-1)));
      }

      //try moving left
      if(this.get(lR+1, lC+1) == Constants.CELL_EMPTY) {
        pMoves.add(new Move(pCell, GameState.rowColToCell(lR+1, lC+1)));
      }
    }

    // Try moving upwards
    if(mNextPlayer == Constants.CELL_WHITE || pKing) {
      // Try moving right
      if(this.get(lR-1, lC-1) == Constants.CELL_EMPTY) {
        pMoves.add(new Move(pCell, GameState.rowColToCell(lR-1, lC-1)));
      }
      // Try moving left
      if(this.get(lR-1, lC+1) == Constants.CELL_EMPTY) {
        pMoves.add(new Move(pCell, GameState.rowColToCell(lR-1,lC+1)));
      }
    }
  }

  /**
   * Finds possible moves and stores these in a vector in the current game
   * state.
   *
   * @param pStates the current game state
   */
  void findPossibleMoves(Vector<GameState> pStates) {
    pStates.clear();

    if (mLastMove.isEOG()) {
      return;
    }

    if (mMovesUntilDraw <= 0) {
      pStates.add(new GameState(this, new Move(Move.MOVE_DRAW)));
      return;
    }

    // Normal moves are forbidden if any jump is possible
    boolean lFound = false;
    int[] lPieces = new int[GameState.PIECES_PER_PLAYER];
    int[] lMoveBuffer = new int[GameState.PIECES_PER_PLAYER];
    Vector<Move> lMoves = new Vector<Move>();

    int lNumPieces=0;
    for (int i = 0; i < NUMBER_OF_SQUARES; i++) {
      // Is this a piece which belongs to the player making the move?
      if (0 != (this.get(i) & mNextPlayer)) {
        boolean lIsKing = 0 != (this.get(i) & Constants.CELL_KING);

        if (this.tryJump(lMoves, GameState.cellToRow(i), GameState.cellToCol(i),
        				 lIsKing, lMoveBuffer)) {
          lFound=true;
        }
        lPieces[lNumPieces++]=i;
      }
    }

    // Try normal moves if no possible jump was found
    if (!lFound) {
      for (int k = 0; k < lNumPieces; k++) {
        int lCell = lPieces[k];
        boolean lIsKing = 0 != (this.get(lCell) & Constants.CELL_KING);
        this.tryMove(lMoves, lCell, lIsKing);
      }
    }

    // Convert moves to GameStates
    for (int i = 0; i < lMoves.size(); i++) {
      pStates.add(new GameState(this, lMoves.elementAt(i)));
    }

    // Admit loss if no moves can be found
    if (pStates.size() == 0) {
      pStates.add(new GameState(this, new Move(
          mNextPlayer == Constants.CELL_WHITE ? Move.MOVE_RW : Move.MOVE_WW)));
    }
  }

  /**
   * Transforms the board by performing a move.
   *
   * Note: This doesn't check that the move is valid, so you should only use
   * it with moves returned by FindPossibleMoves.
   * 
   * @param pMove the move to perform
   */
  public void doMove(final Move pMove) {
    if (pMove.isJump()) {
      // Row and column of source cell
      int sr = GameState.cellToRow(pMove.at(0));
      int sc = GameState.cellToCol(pMove.at(0));

      // Perform all jumps
      for(int i = 1; i < pMove.length(); i++) {
        // Destination cell
        int dr = GameState.cellToRow(pMove.at(i));
        int dc = GameState.cellToCol(pMove.at(i));

        // Move the jumping piece
        this.set(pMove.at(i), this.get(pMove.at(i-1)));
        this.set(pMove.at(i-1), Constants.CELL_EMPTY);

        // Promote to king if we should
        if (
            (dr == 7 && 0 != (this.get(pMove.at(i)) & Constants.CELL_RED) )
            ||
            (dr == 0 && 0 != (this.get(pMove.at(i)) & Constants.CELL_WHITE) )
          ) {

          this.set(pMove.at(i), this.get(pMove.at(i)) | Constants.CELL_KING);

        }

        // Remove the piece being jumped over (captured)
        this.set(
            GameState.rowColToCell((sr+dr)>>1, (sc+dc)>>1),
            Constants.CELL_EMPTY
            );

        // Prepare for next jump
        sr = dr;
        sc = dc;
      }

      // Reset number of moves left until draw
      mMovesUntilDraw = MOVES_UNTIL_DRAW;

    } else if(pMove.isNormal()) {
      // Move the piece
      this.set(pMove.at(1), this.get(pMove.at(0)));
      this.set(pMove.at(0), Constants.CELL_EMPTY);

      // Promote to king if we should
      int lDR = GameState.cellToRow(pMove.at(1));
      if (
          (lDR == 7 && 0 != (this.get(pMove.at(1)) & Constants.CELL_RED)) ||
          (lDR == 0 && 0 != (this.get(pMove.at(1)) & Constants.CELL_WHITE))
        ) {
        this.set(pMove.at(1), this.get(pMove.at(1)) | Constants.CELL_KING);

      }

      // Decrease number of moves left until draw
      --mMovesUntilDraw;
    }

    // Remember last move
    mLastMove = pMove;

    // Swap player
    mNextPlayer = mNextPlayer ^ (Constants.CELL_RED | Constants.CELL_WHITE);
  }

  /**
   * Converts the board to a human-readable string for printing purposes.
   *
   * Note: Use for debug purposes and print to System.err. Don't call it in
   * the final version.
   */
  public String toString(int pPlayer) {
    // Select preferred printing style by setting cell_text to SIMPLE_TEXT, UNICODE_TEXT or COLOR_TEXT

    final String[] cell_text = Constants.SIMPLE_TEXT;

    final String board_top = "   -----------------\r\n";
    final String board_bottom = "   -----------------\r\n";
    final String board_left = "| ";
    final String board_right = "|";

    int red_pieces = 0;
    int white_pieces = 0;

    // Count pieces
    for (int i = 0; i < NUMBER_OF_SQUARES; i++) {
      if (0 != (this.get(i) & Constants.CELL_RED)) {
        ++red_pieces;
      } else if (0 != (this.get(i) & Constants.CELL_WHITE)) {
        ++white_pieces;
      }
    }

    /* Use a StringBuffer to compose the string */
    StringBuffer ss = new StringBuffer();

    /* Draw the board with numbers around it indicating cell index and put text
     * to the right of the board.
     */
    ss.append(board_top);
    ss.append("  0 " + board_left);
    for(int c = 0; c < 8; c++) {
      ss.append(cell_text[this.get(0, c)]);
    }
    ss.append(board_right + " 3\r\n");

    ss.append("  4 " + board_left);
    for(int c = 0; c < 8; c++) {
      ss.append(cell_text[this.get(1, c)]);
    }
    ss.append(board_right + " 7\r\n");

    ss.append("  8 " + board_left);
    for(int c = 0; c < 8; c++) {
      ss.append(cell_text[this.get(2, c)]);
    }
    ss.append(board_right + " 11   Last move: " + mLastMove.toString());

    if ((pPlayer == Constants.CELL_RED && this.isRedWin()) ||
      (pPlayer == Constants.CELL_WHITE && this.isWhiteWin()) ) {
      ss.append(" (WOHO! I WON!)\r\n");
    } else if ((pPlayer == Constants.CELL_RED && this.isWhiteWin()) ||
        (pPlayer == Constants.CELL_WHITE && this.isRedWin()) ) {
      ss.append(" (Bummer! I lost!)\r\n");
    } else {
      ss.append("\r\n");
    }

    ss.append(" 12 " + board_left);
    for(int c = 0; c < 8; c++) {
      ss.append(cell_text[this.get(3, c)]);
    }
    ss.append(board_right + " 15   Next player: " + cell_text[mNextPlayer] +
    			((mNextPlayer == pPlayer) ? " (My turn)\r\n" : " (Opponents turn)\r\n"));

    ss.append(" 16 " + board_left);
    for(int c = 0; c < 8; c++) {
      ss.append(cell_text[this.get(4, c)]);
    }
    ss.append(board_right + " 19   Moves until draw: " + (int) mMovesUntilDraw + "\r\n");

    ss.append(" 20 " + board_left);
    for(int c = 0; c < 8; c++) {
      ss.append(cell_text[this.get(5, c)]);
    }
    ss.append(board_right + " 23   Red pieces:   " + red_pieces + "\r\n");

    ss.append(" 24 " + board_left);
    for(int c = 0; c < 8; c++) {
      ss.append(cell_text[this.get(6,c)]);
    }
    ss.append(board_right + " 27   White pieces: " + white_pieces + "\r\n");

    ss.append(" 28 " + board_left);
    for(int c = 0; c < 8; c++) {
      ss.append(cell_text[this.get(7,c)]);
    }
    ss.append(board_right + " 31\r\n");

    ss.append(board_bottom);

    return ss.toString();
  }

  /**
   * Converts the board to a machine-readable string ready to be printed to
   * System.out.
   *
   * Note: This is used for passing board states between clients.
   */
  public String toMessage() {
    // Use a StringBuffer to compose the message
    StringBuffer ss = new StringBuffer();

    // The board goes first
    for(int i = 0; i < NUMBER_OF_SQUARES; i++) {
      ss.append(Constants.MESSAGE_SYMBOLS[mCell[i]]);
    }

    // Then the information about moves
    assert(mNextPlayer == Constants.CELL_WHITE ||
    			mNextPlayer == Constants.CELL_RED);

    ss.append(" " + mLastMove.toMessage() + " " +
    		Constants.MESSAGE_SYMBOLS[mNextPlayer] + " " +
    		(int) mMovesUntilDraw);

    return ss.toString();
  }

  /**
   * Gets the last move made (the move that led to the current state).
   */
  public final Move getMove() {
    return this.mLastMove;
  }

  /**
   * Gets the next player (the player whose turn is after this one).
   */
  public final int getNextPlayer() {
    return this.mNextPlayer;
  }

  /**
   * Gets number of moves until draw.
   */
  final int getMovesUntilDraw() {
    return this.mMovesUntilDraw;
  }

  /**
   * Gets whether or not the current move marks the beginning of the game.
   */
  boolean isBOG() {
    return this.mLastMove.isBOG();
  }

  /**
   * Gets whether or not the current move marks the end of the game.
   */
  boolean isEOG() {
    return this.mLastMove.isEOG();
  }

  /**
   * Gets whether or not the last move ended in a win for red player.
   */
  boolean isRedWin() {
    return this.mLastMove.isRedWin();
  }

  /**
   * Gets whether or not the last move ended in a win for white player.
   */
  boolean isWhiteWin() {
    return mLastMove.isWhiteWin();
  }

  /**
   * Gets whether or not the last move ended in a draw.
   */
  boolean isDraw() {
    return mLastMove.isDraw();
  }
}
