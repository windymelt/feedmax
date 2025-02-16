package dev.capslock.feedmax
package infra

import domain.repo.StateRepository
import dev.capslock.feedmax.domain.repo.State
import zio.*
import zio.json.*
import scala.util.Try

object FileState extends StateRepository:
  def loadState(): ZIO[Any, Throwable, State] =
    ZIO
      .fromTry(
        Try(
          scala.io.Source
            .fromFile("state.json")
            .mkString
            .fromJson[State]
            .left
            .map(e => new Throwable(e)),
        ),
      )
      .absolve
  end loadState

  def saveState(state: State): ZIO[Any, Throwable, State] =
    import java.nio.file.{Paths, Files}
    import java.nio.charset.StandardCharsets
    ZIO
      .succeed(
        Files.write(
          Paths.get("state.json"),
          state.toJson.getBytes(StandardCharsets.UTF_8),
        ),
      )
      .as(state)
  end saveState

  val layer: ZLayer[Any, Nothing, StateRepository] =
    ZLayer.succeed(this)
end FileState
