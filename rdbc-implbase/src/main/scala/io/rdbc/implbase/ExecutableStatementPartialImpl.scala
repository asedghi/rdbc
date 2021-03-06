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

package io.rdbc.implbase

import io.rdbc.sapi._
import io.rdbc.sapi.exceptions.{ColumnIndexOutOfBoundsException, NoKeysReturnedException}
import io.rdbc.util.Preconditions.checkNotNull

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait ExecutableStatementPartialImpl extends ExecutableStatement {
  implicit protected def ec: ExecutionContext

  override def executeForSet()(implicit timeout: Timeout): Future[ResultSet] = {
    checkNotNull(timeout)
    val resultStream = stream()
    val subscriber = new HeadSubscriber(None)
    resultStream.subscribe(subscriber)
    for {
      rowsAffected <- resultStream.rowsAffected
      warnings <- resultStream.warnings
      metadata <- resultStream.metadata
      rows <- subscriber.rows
    } yield {
      new ResultSet(
        rowsAffected = rowsAffected,
        warnings = warnings,
        metadata = metadata,
        rows = rows
      )
    }
  }

  override def execute()(implicit timeout: Timeout): Future[Unit] = {
    checkNotNull(timeout)
    executeForRowsAffected().map(_ => ())
  }

  override def executeForRowsAffected()(implicit timeout: Timeout): Future[Long] = {
    checkNotNull(timeout)
    val resultStream = stream()
    resultStream.subscribe(new IgnoringSubscriber)
    resultStream.rowsAffected
  }

  override def executeForFirstRow()(implicit timeout: Timeout): Future[Option[Row]] = {
    /* The method is optimized for result sets that are small.
       It's faster to fetch all rows instead of fetching
       just one and cancelling the subscription. */
    checkNotNull(timeout)
    executeForSet().map(_.rows.headOption)
  }

  override def executeForValue[A](valExtractor: Row => A)
                                 (implicit timeout: Timeout): Future[Option[A]] = {
    checkNotNull(valExtractor)
    checkNotNull(timeout)
    executeForFirstRow().map(maybeRow => maybeRow.map(valExtractor))
  }

  override def executeForKey[K: ClassTag]()(implicit timeout: Timeout): Future[K] = {
    checkNotNull(timeout)
    executeForValue[Option[K]](_.colOpt(0)).flatMap {
      case Some(Some(key)) => Future.successful(key)
      case Some(None) => Future.failed(new NoKeysReturnedException("Row's first column is NULL"))
      case None => Future.failed(new NoKeysReturnedException("No rows were returned"))
    }.recoverWith {
      case ex: ColumnIndexOutOfBoundsException =>
        Future.failed(new NoKeysReturnedException(ex.getMessage, Some(ex)))
    }
  }
}
