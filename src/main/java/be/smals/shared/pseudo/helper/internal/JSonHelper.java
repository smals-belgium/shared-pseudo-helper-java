package be.smals.shared.pseudo.helper.internal;

import be.smals.shared.pseudo.helper.exceptions.EHealthProblemException;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParseException;
import com.nimbusds.jose.shaded.gson.JsonParser;

/**
 * JSon operations helper.
 */
public class JSonHelper {

  /**
   * Parse the given {@link String}.
   *
   * @param jsonString the {@link String} to parse
   * @return the parsed {@link String} as a {@link JsonObject}
   */
  static JsonObject parse(final String jsonString) throws EHealthProblemException {
    try {
      return (JsonObject) JsonParser.parseString(jsonString);
    } catch (final JsonParseException e) {
      throw new EHealthProblemException(EHealthProblemImpl.fromUnparseableRawResponse(jsonString), e);
    }
  }
}
