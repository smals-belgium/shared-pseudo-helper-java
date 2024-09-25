package be.smals.shared.pseudo.helper;

public interface EHealthProblem {

  /**
   * Returns the type of problem.
   * <p>
   * For example: {@code https://www.gcloud.belgium.be/rest/problems/badRequest}
   *
   * @return the type of problem
   */
  String type();

  /**
   * Returns the title of the problem.
   * <p>
   * Basically, it is the HTTP reason phrase (that is associated to the HTTP status) that you would receive if you submitted a non-multiple request.
   * <p>
   * For example: {@code Bad Request}
   *
   * @return the title of the problem
   */
  String title();

  /**
   * Returns the status of the problem.
   * <p>
   * Basically, it is the HTTP status that you would receive if you submitted a non-multiple request.
   * <p>
   * For example: {@code 400}
   *
   * @return the title of the problem
   */
  String status();

  /**
   * Returns more details about the problem.
   * <p>
   * For example: ""Invalid point [ECPoint{curve='P-521', x=AM5X+xq5OWJsxIXR+20/c5WOtOxGg32b0CKzFyOTWSJluozbfjSiYewsEMiTic7uVsuy4CQzr/xMiPPs8qvQ/A8F, y=AaZzuuB427oxpuoTWDIBhrHDiAaVQF+j5TwjW7UZdVQB5qzLelFNieVVxcrFBc6thTWVSxW+8FOjXEeznjiiWVLE}]""
   *
   * @return more details about the problem
   */
  String detail();
}
