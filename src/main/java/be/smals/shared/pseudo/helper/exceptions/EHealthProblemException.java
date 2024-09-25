package be.smals.shared.pseudo.helper.exceptions;

import be.smals.shared.pseudo.helper.EHealthProblem;

/**
 * Exception thrown when the result of a "multiple" operation contains a problem.
 */
public class EHealthProblemException extends RuntimeException {

  private final EHealthProblem problem;

  /**
   * Constructor.
   *
   * @param problem a {@link EHealthProblem}
   */
  public EHealthProblemException(final EHealthProblem problem) {
    super(problem.detail());
    this.problem = problem;
  }

  /**
   * Constructor.
   *
   * @param problem a {@link EHealthProblem}
   */
  public EHealthProblemException(final EHealthProblem problem, final Exception cause) {
    super(problem.detail(), cause);
    this.problem = problem;
  }

  /**
   * Returns the {@link EHealthProblem} from eHealth
   * or a problem of type {@code urn:problem-type:smals:pseudo-helper:unexpected-result}.
   *
   * @return the {@link EHealthProblem} from eHealth
   * or a problem of type {@code urn:problem-type:smals:pseudo-helper:unexpected-result}
   */
  public EHealthProblem getProblem() {
    return problem;
  }
}
