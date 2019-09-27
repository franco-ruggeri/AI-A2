import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * Starts the game for one client.
 * 
 * Note:
 *      Use the verbose flag for outputting game information.
 *      Use the fast flag for using 1s move deadline instead of 10s.
 *      Use the init flag if you want this client to initialize the game, that
 *      is, send a starting board without moving for the other client to move
 *      first.
 */
public class Main {
    public static void main(String[] args) throws IOException {
      /* Parse parameters */
      boolean init = false;
      boolean verbose = false;
      boolean fast = false;

      for (int i = 0; i < args.length; ++i) {
        String param = args[i];

        if (param.equals("init") || param.equals("i")) {
          init = true;
        } else if (param.equals("verbose") || param.equals("v")) {
          verbose = true;
        } else if (param.equals("fast") || param.equals("f")) {
          fast = true;
        } else {
          System.err.println("Unknown parameter: '" + args[i] + "'");
          return;
        }
      }

      /**
       * Start the game by sending the starting board without moves
       * if the parameter "init" is given
       */
      if (init) {
        String message = new GameState().toMessage();
        System.out.println(message);
      }

      Player player = new Player();

      String input_message;
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      while ((input_message = br.readLine()) != null) {
        /* Deadline is one second from when we receive the message */
        Deadline deadline = new Deadline(Deadline.getCpuTime() + (fast ? (long) 1e8 : (long) 2.5e9));

        /* Get game state from standard input */
        GameState input_state = new GameState(input_message);

        /* See if we would produce the same message */
        if (!input_state.toMessage().equals(input_message)) {
          System.err.println("*** ERROR! ***");
          System.err.println("Interpreted: '" + input_message + "'");
          System.err.println("As:      '" + input_state.toMessage() + "'");
          System.err.println(input_state.toString(input_state.getNextPlayer()));
          assert(false);
        }

        /* Print the input state */
        if (verbose) {
          System.err.println(input_state.toMessage());
          System.err.println(input_state.toString(input_state.getNextPlayer()));
        }

        /* Quit if this is end of game */
        if (input_state.getMove().isEOG()) {
          break;
        }

        /* Figure out the next move */
        GameState output_state = player.play(input_state, deadline);

        /* Exit if deadline has been exceeded */
        if (deadline.timeUntil() < 0) {
          System.exit(152);
        }

        /* Check if output state is correct */
        Vector<GameState> output_states = new Vector<GameState>();
        input_state.findPossibleMoves(output_states);
        boolean output_state_correct = false;
        for (int i = 0; i < output_states.size(); ++i)
        	if (output_state.isEqual(output_states.elementAt(i)))
        	{
        		output_state_correct = true;
        		break;
        	}
        if (!output_state_correct) {
        	System.exit(134);
        }

        /* Print the output state */
        if (verbose) {
          System.err.println(output_state.toMessage());
          System.err.println(output_state.toString(input_state.getNextPlayer()));
        }

        /* Send the next move */
        String output_message = output_state.toMessage();
        
        System.out.println(output_message);

        /* Quit if this is the end of the game */
        if (output_state.getMove().isEOG()) {
          break;
        }
      }
    }
}
