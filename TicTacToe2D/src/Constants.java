/**
 * Contains all the constants needed among all classes.
 */
public class Constants {
  /**
   * These definitions are used in the contents of squares in TTTBoard.
   * the CELL_X and CELL_O constants are also used to refer
   * to this and the other player
   */
  public static final int CELL_EMPTY   = 0;      ///< the cell is empty
  public static final int CELL_X       = 1;      ///< the cell belongs to the X player
  public static final int CELL_O       = 2;      ///< the cell belongs to the O player
  public static final int CELL_INVALID = 3;      ///< the cell is invalid

  /**
   * A simple way of representing the pieces of the board.
   */
  public static final String[] SIMPLE_TEXT = {
      ". ", // CELL_EMPTY
      "x ", // CELL_X
      "o ", // CELL_O
      "  ", // CELL_INVALID
  };

  /**
   * A more sophisticated way of representing the pieces of the board, using
   * the Unicode character set.
   */
  public static final String[] UNICODE_TEXT = {
      "― ", // CELL_EMPTY
      "✗ ", // CELL_X
      "𝑂 ", // CELL_O
      "  ", // CELL_INVALID
  };

  /**
   * A quite sophisticated way of representing the pieces of the board, using
   * the Unicode character set with colors.
   */
  public
  static final String[] COLOR_TEXT = {
      "\u001B[30m― \u001B[0m", // CELL_EMPTY
      "✗ ", // CELL_X
      "𝑂 ", // CELL_O
      "  ", // CELL_INVALID
  };

  /**
   * Symbols used for messages between clients.
   */
  public static final char[] MESSAGE_SYMBOLS = {
      '.', // CELL_EMPTY
      'x', // CELL_X
      'o', // CELL_O
      '_', // CELL_INVALID
  };
}