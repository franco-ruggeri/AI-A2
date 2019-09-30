import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Represents a game state with a 4x4 board
 *
 * Cells are numbered as follows:
 *
 *    col 0  1  2  3  
 * row  ---------------
 *  0  |  0  1  2  3  |  0
 *  1  |  4  5  6  7  |  1
 *  2  |  8  9  10 11 |  2
 *  3  | 12  13 14 15 |  3
 *      ---------------
 *        0  1  2  3
 
 *
 * The staring board looks like this:
 *
 *    col 0  1  2  3  
 * row  ---------------
 *  0  |  .  .  .  .  |  0
 *  1  |  .  .  .  .  |  1
 *  2  |  .  .  .  .  |  2
 *  3  |  .  .  .  .  |  3
 *      ---------------
 *        0  1  2  3
 * 
 * X moves first.
 */

public class GameState {
    public static final int BOARD_SIZE = 4;
    public static final int CELL_COUNT = BOARD_SIZE * BOARD_SIZE;

    private static final int LINE_NONE = 0;
    private static final int LINE_WIN  = 1;
    private static final int LINE_DRAW = 2;

    private int[] cells = new int[GameState.CELL_COUNT];
    private int nextPlayer;
    private Move lastMove;

    /**
     * Initializes the board to the starting position.
     */
    public GameState() {
      /* Initialize the board */
      for (int i = 0; i < CELL_COUNT; i++) {
          this.cells[i] = Constants.CELL_EMPTY;
      }
      /* Initialize move related variables */
      this.lastMove = new Move(Move.MOVE_BOG);
      // Player X starts
      this.nextPlayer = Constants.CELL_X;
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
      board = st.nextToken();
      last_move = st.nextToken();
      next_player = st.nextToken();

      /* Sanity checks. If any of these fail, something has gone horribly
       * wrong. */
      assert(board.length() == GameState.CELL_COUNT);
      assert(next_player.length() == 1);
      
      // Parse the board
      for (int i = 0; i < GameState.CELL_COUNT; ++i) {
        if (board.charAt(i) == Constants.MESSAGE_SYMBOLS[Constants.CELL_EMPTY]) {
          this.cells[i] = Constants.CELL_EMPTY;
        }
        else if (board.charAt(i) == Constants.MESSAGE_SYMBOLS[Constants.CELL_X]) {
          this.cells[i] = Constants.CELL_X;
        }
        else if (board.charAt(i) == Constants.MESSAGE_SYMBOLS[Constants.CELL_O]) {
          this.cells[i] = Constants.CELL_O;
        }
        else
          assert("Invalid cell" == "");
      }

      // Parse last move
      this.lastMove = new Move(last_move);

      // Parse next player
      if (next_player.charAt(0) == Constants.MESSAGE_SYMBOLS[Constants.CELL_EMPTY]) {
        this.nextPlayer = Constants.CELL_EMPTY;
      }
      else if (next_player.charAt(0) == Constants.MESSAGE_SYMBOLS[Constants.CELL_X]) {
        this.nextPlayer = Constants.CELL_X;
      }
      else if (next_player.charAt(0) == Constants.MESSAGE_SYMBOLS[Constants.CELL_O]) {
        this.nextPlayer = Constants.CELL_O;
      }
      else
      {
        assert("Invalid cell" == "");
      }
    }

    /**
     * Constructs a board which is the result of applying move move to board 
     * gameState.
     *
     * @param gameState the starting board position
     * @param move the movement to perform
     * @see DoMove()
     */
    public GameState(final GameState gameState, final Move move) {
      /* Copy board */
      this.cells = gameState.cells.clone();

      /* Copy move status */
      this.nextPlayer = gameState.nextPlayer;
      this.lastMove = gameState.lastMove;

      /* Perform move */
      this.doMove(move);
    }

    /**
     * Gets whether or not the current move marks the end of the game.
     */
    boolean isEOG() {
      return this.lastMove.isEOG();
    }

    /**
     * Gets whether or not the last move ended in a win for X player.
     */
    boolean isXWin() {
      return this.lastMove.isXWin();
    }

    /**
     * Gets whether or not the last move ended in a win for O player.
     */
    boolean isOWin() {
      return lastMove.isOWin();
    }

    /**
     * Gets the row corresponding to an index in the array representation of
     * the board.
     * 
     * @param cell
     * @return the row corresponding to a cell index
     */
    public static int cellToRow(int cell) {
      return cell / BOARD_SIZE;
    }

    /**
     * Gets the column corresponding to an index in the array representation of
     * the board.
     * 
     * @param cell
     * @return the column corresponding to a cell index
     */
    public static int cellToCol(int cell) {
      return cell % BOARD_SIZE;
    }

    /**
     * Gets the index in the array representation of the board given column,
     * row and layer indexes.
     * 
     * @param row
     * @param column
     * @return cell index corresponding to the row and column indexes
     */
    public static int rowColumnToCell(int row, int column)
      {
          return column + row * BOARD_SIZE;
      }

    /**
     * Gets the content of a cell in the board, from row and column number.
     *
     * Rows are numbered (0 to BOARD_SIZE - 1) from the upper row in the board,
     * as seen by the player this program is playing.
     *
     * Columns are numbered starting from the left (also 0 to BOARD_SIZE - 1).
     *
     * If the cell falls outside of the board, return CELL_INVALID
     */
    public int at(int row, int column) {
      if ((row < 0) || (row > BOARD_SIZE - 1) || (column < 0) || (column > BOARD_SIZE - 1))
        return Constants.CELL_INVALID;
      return cells[rowColumnToCell(row, column)];
    }

    /**
     * Returns the content of a cell in the board.
     *
     * This function returns a byte representing the contents of the cell,
     * using the integer values of cells
     *
     * For example, to check if cell 10 contains a O piece, you would check if
     *
     * (at(10) & CELL_O)
     *
     * to check if it is a X piece,
     *
     * (at(10) & CELL_X)
     *
     */
    public int at(int pos) {
      assert(pos >= 0);
      assert(pos < CELL_COUNT);
      return cells[pos];
    }
    
    private void set(int pos, int cell) {
      assert(pos >= 0);
      assert(pos < CELL_COUNT);
      cells[pos] = cell;
    }
    
    /**
     * Gets the last move made (the move that led to the current state).
     */
    public final Move getMove() {
      return this.lastMove;
    }

    /**
     * Gets the next player (the player whose turn is after this one).
     */
    public final int getNextPlayer() {
      return this.nextPlayer;
    }

    /** Win or Draw for board */
    private int checkLines(int thePlayer) {
      int result;
      int draws = 0;

      for (int row = 0; row < BOARD_SIZE; ++row) {
        result = checkLine(thePlayer, row, 0, row, BOARD_SIZE - 1);
        if (result == LINE_DRAW) { draws++; }
        if (result == LINE_WIN) { return LINE_WIN; }
      }
      for (int col = 0; col < BOARD_SIZE; ++col) {
        result = checkLine(thePlayer, 0, col, BOARD_SIZE - 1, col);
        if (result == LINE_DRAW) { draws++; }
        if (result == LINE_WIN) { return LINE_WIN; }
      }

      result = checkLine(thePlayer, 0, 0, BOARD_SIZE - 1, BOARD_SIZE - 1);
      if (result == LINE_DRAW) { draws++; }
      if (result == LINE_WIN) { return LINE_WIN; }
      result = checkLine(thePlayer, 0, BOARD_SIZE - 1, BOARD_SIZE - 1, 0);
      if (result == LINE_DRAW) { draws++; }
      if (result == LINE_WIN) { return LINE_WIN; }

      if (draws == 2 * BOARD_SIZE + 2)
      {
        return LINE_DRAW;
      }

      return LINE_NONE;
    }

    /** Win or Draw for line */
    private int checkLine(int player, int row1, int col1, int row2, int col2) {
       int dRow = (row2 - row1) / (BOARD_SIZE - 1);
       int dCol = (col2 - col1) / (BOARD_SIZE - 1);
       int opponent = (player == Constants.CELL_X) ? Constants.CELL_O : Constants.CELL_X;

       int playerCells = 0, opponentCells = 0;

       for (int i = 0; i < BOARD_SIZE; ++i) {
         if (cells[rowColumnToCell(row1 + dRow * i, col1 + dCol * i)] == player) {
           playerCells++;
         }
         if (cells[rowColumnToCell(row1 + dRow * i, col1 + dCol * i)] == opponent) {
           opponentCells++;
         }
         if ((playerCells > 0) && (opponentCells > 0))
         {
             return LINE_DRAW;
         }
       }
       
       return ((playerCells == BOARD_SIZE) ? LINE_WIN : LINE_NONE);
    }

    /**
    * Checks if the move ends up being a special move (Winning = 1, Draw = 2)
    * @param cell the cell where the move is tried
    * @param player player who is making the move
    */
    private int isSpecialMove(int cell, int player)
    {
      // The cell should be empty
      int oldCell = cells[cell];
      cells[cell] = player;
      int result = checkLines(player);
      cells[cell] = oldCell;
      return (result == LINE_WIN) ? 1 :
             (result == LINE_DRAW) ? 2 : 0;
    }

    /**
      * Tries to make a move onto a certain position
      * @param moves a vector where the valid moves will be inserted
      * @param cell the cell where the move is tried
      */
    private void tryMove(Vector<Move> moves, int cell) {
      int row = GameState.cellToRow(cell);
      int column = GameState.cellToCol(cell);
      int specialMove = Move.SPECIAL_NONE;
      // Try move X
      if (this.nextPlayer == Constants.CELL_X) {
        // Try move
        if(this.at(row, column) == Constants.CELL_EMPTY) {
          //Check if special move
          specialMove = this.isSpecialMove(cell, Constants.CELL_X);
          if(specialMove != Move.SPECIAL_NONE) {
            moves.add(new Move(cell, Constants.CELL_X, specialMove));
          }
          else {
            moves.add(new Move(cell, Constants.CELL_X)); 
          }
        }                   
      }
      // Try move O
      if (this.nextPlayer == Constants.CELL_O) {
        // Try move
        if (this.at(row, column) == Constants.CELL_EMPTY) {
          // Check if special move
          specialMove = this.isSpecialMove(cell, Constants.CELL_O);
          if (specialMove > 0) {
            moves.add(new Move(cell, Constants.CELL_O, specialMove));
          }
          else {
            moves.add(new Move(cell, Constants.CELL_O)); 
          }
        }
      }
    }

    /**
     * Finds possible moves and stores these in a vector in the current game
     * state.
     *
     * @param states the current game state
     */
    public void findPossibleMoves(Vector<GameState> states) {
      states.clear();

      if (lastMove.isEOG()) {
        return;
      }

      Vector<Move> moves = new Vector<Move>();
      
      for (int k = 0; k < CELL_COUNT; ++k)
      {
        tryMove(moves, k);        
      }
      
      // Convert moves to GameStates
      for (int i = 0; i < moves.size(); ++i) {
        states.add(new GameState(this, moves.elementAt(i)));
      }
    }

    /**
     * Transforms the board by performing a move.
     *
     * Note: This doesn't check that the move is valid, so you should only use
     * it with moves returned by findPossibleMoves.
     * 
     * @param move the move to perform
     */
    public void doMove(final Move move) {
      // Set the cell
      set(move.at(0), move.at(1));
     
      // Remember last move
      lastMove = move;

      // Swap player
      nextPlayer = nextPlayer ^ (Constants.CELL_X | Constants.CELL_O);
    }

    /**
     * Compares two game states.
     * 
     * @param gameState game state to compare to
     * @return true if game states are identical, otherwise false
     */
    public boolean isEqual(GameState gameState)
    {
  	  boolean equal = true;
  	  for (int i = 0; i < CELL_COUNT; ++i)
  		 if (cells[i] != gameState.at(i))
  			 equal = false;
  	  if (nextPlayer != gameState.getNextPlayer())
  		  equal = false;
  	  if (!lastMove.toMessage().equals(gameState.getMove().toMessage()))
  		  equal = false;
  	  return equal;
    }

    /**
     * Converts the board to a human-readable string for printing purposes.
     *
     * Note: Use for debug purposes and print to System.err. Don't call it in
     * the final version.
     */
    public String toString(int player) {
      // Select preferred printing style by setting cell_text to SIMPLE_TEXT, UNICODE_TEXT or COLOR_TEXT
      // Note: this code is intended for BOARD_SIZE = 4
      final String[] cell_text = Constants.COLOR_TEXT;
      final String board_top    = (cell_text == Constants.SIMPLE_TEXT) ? "     -----------------\n"
              : "    ╭─────────╮\n";
      final String board_bottom = (cell_text == Constants.SIMPLE_TEXT) ? "     -----------------\n"
              : "    ╰─────────╯\n";
      final String board_left   = (cell_text == Constants.SIMPLE_TEXT) ? "| " : "│ ";
      final String board_right  = (cell_text == Constants.SIMPLE_TEXT) ? "|" : "│";

      boolean is_winner = (isEOG() && ((player == Constants.CELL_X && isXWin()) || (player == Constants.CELL_O && isOWin())));
      boolean is_my_turn = (nextPlayer == player);
      int X_pieces = 0;
      int O_pieces = 0;
      
      for (int i = 0; i < CELL_COUNT; ++i) {
        if ((at(i) & Constants.CELL_X) != 0)
          ++X_pieces;
        else if ((at(i) & Constants.CELL_O) != 0)
          ++O_pieces;
      }

      /* Use a StringBuffer to compose the string */
      StringBuffer ss = new StringBuffer();

      /* Draw the board with numbers around it indicating cell index */
      ss.append(board_top);
      ss.append("  0 " + board_left);
      for(int c = 0; c < BOARD_SIZE; c++) {
        ss.append(cell_text[this.at(0, c)]);
      }
      ss.append(board_right + " 3 ");
      ss.append("  Last move: " + lastMove.toString() + (is_winner ? " (WOHO! I WON!)\n" : "\n"));
      
      ss.append("  4 " + board_left);
      for(int c = 0; c < BOARD_SIZE; c++) {
        ss.append(cell_text[this.at(1, c)]);
      }
      ss.append(board_right + " 7 ");
      ss.append("  Next player: " + cell_text[nextPlayer] + (is_my_turn ? " (My turn)\n" : " (Opponents turn)\n"));
      
      ss.append("  8 " + board_left);
      for(int c = 0; c < BOARD_SIZE; c++) {
          ss.append(cell_text[this.at(2, c)]);
      }
      ss.append(board_right + " 11");
      ss.append("  X pieces: " + String.valueOf(X_pieces) + "\n");    

      ss.append(" 12 " + board_left);
      for(int c = 0; c < BOARD_SIZE; c++) {
          ss.append(cell_text[this.at(3, c)]);
      }
      ss.append(board_right + " 15");
      ss.append("  O pieces: " + String.valueOf(O_pieces) + "\n"); 
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
      for (int i = 0; i < CELL_COUNT; i++) {
        ss.append(Constants.MESSAGE_SYMBOLS[cells[i]]);
      }

      // Then the information about moves
      assert((nextPlayer == Constants.CELL_O) || (nextPlayer == Constants.CELL_X));
      ss.append(" " + lastMove.toMessage() + " " + Constants.MESSAGE_SYMBOLS[nextPlayer]);

      return ss.toString();
    }
  }
