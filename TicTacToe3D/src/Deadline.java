import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Represents a deadline for the player to make their move.
 */
public class Deadline {
  private long deadline;

  /** 
   * Gets CPU time in nanoseconds (but likely with millisecond or microsecond
   * precision).
   */
  public static long getCpuTime() {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    return bean.isCurrentThreadCpuTimeSupported() ?
        bean.getCurrentThreadCpuTime() : 0;
  }
  
  /**
   * Constructs and sets the Deadline.
   * 
   * @param deadline the deadline expressed in nanoseconds.
   */
  public Deadline(long deadline) {
    this.deadline = deadline;
  }

  /**
   * Calculates and returns the remaining time until the Deadline must be met,
   * in nanoseconds.
   */
  long timeUntil() {
    return deadline - Deadline.getCpuTime();
  }
}
