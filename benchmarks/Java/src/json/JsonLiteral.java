/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource.
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

final class JsonLiteral extends JsonValue {

  static final JsonValue NULL = new JsonLiteral("null");
  static final JsonValue TRUE = new JsonLiteral("true");
  static final JsonValue FALSE = new JsonLiteral("false");

  private final String value;
  private final boolean isNull;
  private final boolean isTrue;
  private final boolean isFalse;

  private JsonLiteral(final String value) {
    this.value = value;
    isNull  = "null".equals(value);
    isTrue  = "true".equals(value);
    isFalse = "false".equals(value);
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public boolean isNull() {
    return isNull;
  }

  @Override
  public boolean isTrue() {
    return isTrue;
  }

  @Override
  public boolean isFalse() {
    return isFalse;
  }

  @Override
  public boolean isBoolean() {
    return isTrue || isFalse;
  }
}
