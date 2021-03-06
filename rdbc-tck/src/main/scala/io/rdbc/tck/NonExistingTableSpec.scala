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

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import io.rdbc.sapi._
import io.rdbc.sapi.exceptions.InvalidQueryException
import io.rdbc.tck.util.Subscribers

import scala.concurrent.Future

trait NonExistingTableSpec extends RdbcSpec {

  protected implicit def system: ActorSystem
  protected implicit def materializer: Materializer

  "Error should be returned when referencing a non-existent table when" - {
    stmtTest("Select", _.statement(sql"select * from nonexistent"), errPos = 15)
    stmtTest("Insert", _.statement(sql"insert into nonexistent values (1)"), errPos = 13)
    stmtTest("Returning insert",
      _.statement(sql"insert into nonexistent values (1)", StatementOptions.ReturnGenKeys),
      errPos = 13
    )
    stmtTest("Delete", _.statement(sql"delete from nonexistent"), errPos = 13)
    stmtTest("Update", _.statement(sql"update nonexistent set x = 1"), errPos = 8)
    stmtTest("DDL", _.statement(sql"drop table nonexistent"), errPos = 12)
  }

  "Streaming arguments should" - {
    "fail with an InvalidQueryException" - {
      "when statement references a non-existing table" in { c =>
          val stmt = c.statement("insert into nonexistent values (:x)")
          val src = Source(Vector(Vector(1), Vector(2))).runWith(Sink.asPublisher(fanout = false))

          assertInvalidQueryThrown(errPos = 13) {
            stmt.streamArgsByIdx(src).get
        }
      }
    }
  }

  private def stmtTest(stmtType: String, stmt: Connection => ExecutableStatement, errPos: Int): Unit = {
    s"executing a $stmtType for" - {
      executedFor("nothing", _.execute())
      executedFor("set", _.executeForSet())
      executedFor("value", _.executeForValue(_.int(1)))
      executedFor("first row", _.executeForFirstRow())
      executedFor("generated key", _.executeForKey[String])
      executedFor("stream", stmt => {
        val rs = stmt.stream()
        val subscriber = Subscribers.eager()
        rs.subscribe(subscriber)
        subscriber.rows
      })

      def executedFor[A](executorName: String, executor: ExecutableStatement => Future[A]): Unit = {
        s"executed for $executorName" in { c =>
          assertInvalidQueryThrown(errPos) {
            executor(stmt(c)).get
          }
        }
      }
    }
  }


  private def assertInvalidQueryThrown(errPos: Int)(body: => Any): Unit = {
    val e = intercept[InvalidQueryException] {
      body
    }
    e.errorPosition.fold(alert("non-fatal: no error position reported")) {
      pos =>
        pos shouldBe errPos
    }
  }
}
