/*******************************************************************************
 * Copyright (c) 2015 Stefan Marr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package json;

public final class JsonPureStringParser {

  private final String input;
  private int index;
  private int line;
  private int column;
  private String current;
  private String captureBuffer;
  private int captureStart;

  public JsonPureStringParser(final String string) {
    this.input = string;
    index = -1;
    line = 1;
    captureStart = -1;
    column = 0;
    current = null;
    captureBuffer = "";
  }

  public JsonValue parse() {
    read();
    skipWhiteSpace();
    JsonValue result = readValue();
    skipWhiteSpace();
    if (!isEndOfText()) {
      throw error("Unexpected character");
    }
    return result;
  }

  private JsonValue readValue() {
    switch(current) {
    case "n":
      return readNull();
    case "t":
      return readTrue();
    case "f":
      return readFalse();
    case "\"":
      return readString();
    case "[":
      return readArray();
    case "{":
      return readObject();
    case "-":
    case "0":
    case "1":
    case "2":
    case "3":
    case "4":
    case "5":
    case "6":
    case "7":
    case "8":
    case "9":
      return readNumber();
    default:
      throw expected("value");
    }
  }

  private JsonArray readArray() {
    read();
    JsonArray array = new JsonArray();
    skipWhiteSpace();
    if (readChar("]")) {
      return array;
    }
    do {
      skipWhiteSpace();
      array.add(readValue());
      skipWhiteSpace();
    } while (readChar(","));
    if (!readChar("]")) {
      throw expected("',' or ']'");
    }
    return array;
  }

  private JsonObject readObject() {
    read();
    JsonObject object = new JsonObject();
    skipWhiteSpace();
    if (readChar("}")) {
      return object;
    }
    do {
      skipWhiteSpace();
      String name = readName();
      skipWhiteSpace();
      if (!readChar(":")) {
        throw expected("':'");
      }
      skipWhiteSpace();
      object.add(name, readValue());
      skipWhiteSpace();
    } while (readChar(","));

    if (!readChar("}")) {
      throw expected("',' or '}'");
    }
    return object;
  }

  private String readName() {
    if (!current.equals("\"")) {
      throw expected("name");
    }
    return readStringInternal();
  }

  private JsonValue readNull() {
    read();
    readRequiredChar("u");
    readRequiredChar("l");
    readRequiredChar("l");
    return JsonLiteral.NULL;
  }

  private JsonValue readTrue() {
    read();
    readRequiredChar("r");
    readRequiredChar("u");
    readRequiredChar("e");
    return JsonLiteral.TRUE;
  }

  private JsonValue readFalse() {
    read();
    readRequiredChar("a");
    readRequiredChar("l");
    readRequiredChar("s");
    readRequiredChar("e");
    return JsonLiteral.FALSE;
  }

  private void readRequiredChar(final String ch) {
    if (!readChar(ch)) {
      throw expected("'" + ch + "'");
    }
  }

  private JsonValue readString() {
    return new JsonString(readStringInternal());
  }

  private String readStringInternal() {
    read();
    startCapture();
    while (!current.equals("\"")) {
      if (current.equals("\\")) {
        pauseCapture();
        readEscape();
        startCapture();
      } else {
        read();
      }
    }
    String string = endCapture();
    read();
    return string;
  }

  private void readEscape() {
    read();
    switch(current) {
    case "\"":
    case "/":
    case "\\":
      captureBuffer += current;
      break;
    case "b":
      captureBuffer += "\b";
      break;
    case "f":
      captureBuffer += "\f";
      break;
    case "n":
      captureBuffer += "\n";
      break;
    case "r":
      captureBuffer += "\r";
      break;
    case "t":
      captureBuffer += "\t";
      break;
    default:
      throw expected("valid escape sequence");
    }
    read();
  }

  private JsonValue readNumber() {
    startCapture();
    readChar("-");
    String firstDigit = current;
    if (!readDigit()) {
      throw expected("digit");
    }
    if (!firstDigit.equals("0")) {
      // Checkstyle: stop
      while (readDigit()) { }
     // Checkstyle: resume
    }
    readFraction();
    readExponent();
    return new JsonNumber(endCapture());
  }

  private boolean readFraction() {
    if (!readChar(".")) {
      return false;
    }
    if (!readDigit()) {
      throw expected("digit");
    }
    // Checkstyle: stop
    while (readDigit()) { }
    // Checkstyle: resume
    return true;
  }

  private boolean readExponent() {
    if (!readChar("e") && !readChar("E")) {
      return false;
    }
    if (!readChar("+")) {
      readChar("-");
    }
    if (!readDigit()) {
      throw expected("digit");
    }

    // Checkstyle: stop
    while (readDigit()) { }
    // Checkstyle: resume
    return true;
  }

  private boolean readChar(final String ch) {
    if (!current.equals(ch)) {
      return false;
    }
    read();
    return true;
  }

  private boolean readDigit() {
    if (!isDigit()) {
      return false;
    }
    read();
    return true;
  }

  private void skipWhiteSpace() {
    while (isWhiteSpace()) {
      read();
    }
  }

  private void read() {
    if ("\n".equals(current)) {
      line++;
      column = 0;
    }
    index++;
    if (index < input.length()) {
      current = input.substring(index, index + 1);
    } else {
      current = null;
    }
  }

  private void startCapture() {
    captureStart = index;
  }

  private void pauseCapture() {
    int end = current == null ? index : index - 1;
    captureBuffer += input.substring(captureStart, end + 1);
    captureStart = -1;
  }

  private String endCapture() {
    int end = current == null ? index : index - 1;
    String captured;
    if ("".equals(captureBuffer)) {
      captured = input.substring(captureStart, end + 1);
    } else {
      captureBuffer += input.substring(captureStart, end + 1);
      captured = captureBuffer;
      captureBuffer = "";
    }
    captureStart = -1;
    return captured;
  }

  private ParseException expected(final String expected) {
    if (isEndOfText()) {
      return error("Unexpected end of input");
    }
    return error("Expected " + expected);
  }

  private ParseException error(final String message) {
    return new ParseException(message, index, line, column - 1);
  }

  private boolean isWhiteSpace() {
    return " ".equals(current) || "\t".equals(current) || "\n".equals(current) || "\r".equals(current);
  }

  private boolean isDigit() {
    return "0".equals(current) ||
        "1".equals(current) ||
        "2".equals(current) ||
        "3".equals(current) ||
        "4".equals(current) ||
        "5".equals(current) ||
        "6".equals(current) ||
        "7".equals(current) ||
        "8".equals(current) ||
        "9".equals(current);
  }

  private boolean isEndOfText() {
    return current == null;
  }
}
