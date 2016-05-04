/***************************************************************************
 * Copyright 2016 GiusaSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package giusa.parser.parameter;

/**
 * Checked exception thrown if a parameter is missing and it was required.
 *
 * @author Alessandro Giusa, alessandrogiusa@gmail.com
 * @version 1.0
 *
 */
public class MissingParameterException extends Exception {

  /** constant identifies a parameter to be named. */
  public static final int NO_POSITION = -1;

  /** serial id. */
  private static final long serialVersionUID = 1L;

  /**name of missing parameter.*/
  private final String name;

  /**position of missing parameter.*/
  private final int pos;

  /**
   * Create a missing parameter exception.
   *
   * @param missingParameterName
   *            name of the parameter
   * @param expectedPosition
   *            expected position if it were unnnamed
   * @param message
   *            the exception message
   *
   */
  public MissingParameterException(final String missingParameterName,
            final int expectedPosition, final String message) {
      super(message);
      this.name = missingParameterName;
      this.pos = expectedPosition;
  }

    /**
     * Get the name of the missing parameter.
     *
     * @return name of the parameter
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Get the expected position if this parameter was unnamed.
     *
     * @return position. If {@link #NO_POSITION} then it has to be named
     */
    public final int getExpectedPosition() {
        return this.pos;
    }
}
