/**
 * Contains all the constants needed among all classes.
 */
public class Constants {
  /**
   * These definitions are used in the contents of squares in CBoard.
   * the CELL_WHITE and CELL_RED definitions are also used to refer
   * to this and the other player.
   * 
   * Note that 1<<0 refers to the first bit of an integer being set, 1<<1 the 
   * second, and so on.
   */
  public static final int CELL_EMPTY   = 0;  // The cell is empty
  public static final int CELL_RED   = 1<<0; // The cell belongs to the red player
  public static final int CELL_WHITE   = 1<<1; // The cell belongs to the white player
  public static final int CELL_KING  = 1<<2; // The cell is a king
  public static final int CELL_INVALID = 1<<3; // The cell is invalid

  /**
   * A simple way of representing the pieces of the board.
   */
  public static final String[] SIMPLE_TEXT = {
      ". ", // CELL_EMPTY
      "r ", // CELL_RED
      "w ", // CELL_WHITE
      "? ", // Unused
      "? ", // Unused
      "R ", // CELL_RED | CELL_KING
      "W ", // CELL_WHITE | CELL_KING
      "? ", // Unused
      "  ", // CELL_INVALID
  };

  /**
   * Symbols used for messages between clients.
   */
  public static final char[] MESSAGE_SYMBOLS = {
    '.', // CELL_EMPTY
    'r', // CELL_RED
    'w', // CELL_WHITE
    '?', // Unused
    '?', // Unused
    'R', // CELL_RED | CELL_KING
    'W', // CELL_WHITE | CELL_KING
    '?', // Unused
    '_', // CELL_INVALID
  };
}
