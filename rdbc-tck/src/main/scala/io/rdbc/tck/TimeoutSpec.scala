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

package io.rdbc.tck

import io.rdbc.sapi._
import io.rdbc.sapi.exceptions.TimeoutException
import io.rdbc.tck.util.Subscribers

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect.ClassTag

trait TimeoutSpec extends RdbcSpec {

  private val testTimeout = 1.second.timeout

  "Any statement should" - {
    "return an error when max time is exceeded" - {
      executedFor("nothing", _.execute()(testTimeout))
      executedFor("set", _.executeForSet()(testTimeout))
      executedFor("value", _.executeForValue(_.int(1))(testTimeout))
      executedFor("first row", _.executeForFirstRow()(testTimeout))
      executedFor("generated key", _.executeForKey[String]()(ClassTag(classOf[String]), testTimeout))
      executedFor("stream", stmt => {
        val rs = stmt.stream()(testTimeout)
        val subscriber = Subscribers.eager()
        rs.subscribe(subscriber)
        subscriber.rows
      })

      def executedFor[A](executorName: String, executor: ExecutableStatement => Future[A]): Unit = {
        executedForTempl(
          executorName,
          _.statement(slowStatement).noArgs,
          executor
        )
      }
    }
  }

  private def assertTimeoutThrown(body: => Future[Any]): Unit = {
    assertThrows[TimeoutException] {
      body.get
    }
  }

  private def executedForTempl[S, R](name: String,
                                     creator: Connection => S,
                                     executor: S => Future[R]): Unit = {
    s"executed for $name" in { c =>
      assertTimeoutThrown {
        executor(creator(c))
      }
    }
  }

  private val slowStatement: String = slowStatement(5.seconds) //TODO hardcoded
  protected def slowStatement(time: FiniteDuration): String
}
