package be.smals.shared.pseudo.helper.internal;

import be.smals.shared.pseudo.helper.EHealthProblem;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.JsonObject;

public class EHealthProblemImpl implements EHealthProblem {

  private static final Gson GSON = new Gson();

  private final String type;
  private final String title;
  private final String status;
  private final String detail;

  EHealthProblemImpl(final String type, final String title, final String status, final String detail) {
    this.type = type;
    this.title = title;
    this.status = status;
    this.detail = detail;
  }

  @Override
  public String type() {
    return type;
  }

  @Override
  public String title() {
    return title;
  }

  @Override
  public String status() {
    return status;
  }

  @Override
  public String detail() {
    return detail;
  }

  /**
   * Create a {@link EHealthProblemImpl} from the eHealth response.
   *
   * @param response the response from eHealth as a {@link JsonObject}.
   * @return a {@link EHealthProblemImpl} created from the response from eHealth
   */
  static EHealthProblemImpl fromResponse(final JsonObject response) {
    return response.has("type")
           ? new EHealthProblemImpl(response.get("type").getAsString(),
                                    response.get("title").getAsString(),
                                    response.get("status").getAsString(),
                                    response.get("detail").getAsString())
           : new EHealthProblemImpl("urn:problem-type:smals:pseudo-helper:unexpected-result",
                                    "Unexpected Result", "undefined",
                                    "Unable to convert the response from eHealth into an EHealthProblem." +
                                    "Response was: \n" + GSON.toJson(response));
  }

  static EHealthProblemImpl fromUnparseableRawResponse(final String rawResponse) {
    return new EHealthProblemImpl("urn:problem-type:smals:pseudo-helper:unparseable-result",
                                  "Unparseable Result", "undefined",
                                  "Unable to parse the response from eHealth." +
                                  "Response was: \n" + rawResponse);
  }
}
