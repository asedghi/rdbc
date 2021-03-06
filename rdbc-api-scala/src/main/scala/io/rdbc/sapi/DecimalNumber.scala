/*
 * Copyright 2016 rdbc contributors
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
 */

package io.rdbc.sapi

/** Unbounded decimal numeric type that extends [[scala.math.BigDecimal BigDecimal]]
  * to be able to represent NaN, positive infitnity and negative infinity. */
sealed trait DecimalNumber

object DecimalNumber {

  /** Not-a-number */
  case object NaN extends DecimalNumber

  /** Positive infinity */
  case object PosInfinity extends DecimalNumber

  /** Negative infinity */
  case object NegInfinity extends DecimalNumber

  /** Decimal value representable with a [[scala.math.BigDecimal BigDecimal]] */
  case class Val(bigDecimal: BigDecimal) extends DecimalNumber {
    override def toString: String = bigDecimal.toString()
  }

  def apply(bigDecimal: BigDecimal): DecimalNumber = Val(bigDecimal)
}
