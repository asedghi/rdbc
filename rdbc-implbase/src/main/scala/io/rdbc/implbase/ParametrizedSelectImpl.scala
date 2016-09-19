/*
 * Copyright 2016 Krzysztof Pado
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

package io.rdbc.implbase

import io.rdbc.sapi._

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class ParametrizedSelectImpl(stmt: ParametrizedStatement) extends ParametrizedSelect {

  def executeForStream()(implicit timeout: FiniteDuration): Future[ResultStream] = stmt.executeForStream()

  def executeForSet()(implicit timeout: FiniteDuration): Future[ResultSet] = stmt.executeForSet()

  def executeForFirstRow()(implicit timeout: FiniteDuration): Future[Option[Row]] = stmt.executeForFirstRow()

  def executeForSingle[A](f: Row => A)(implicit timeout: FiniteDuration): Future[Option[A]] = stmt.executeForValue[A](f)

  def executeForSingleOpt[A](f: Row => Option[A])(implicit timeout: FiniteDuration): Future[Option[A]] = stmt.executeForValueOpt[A](f)
}