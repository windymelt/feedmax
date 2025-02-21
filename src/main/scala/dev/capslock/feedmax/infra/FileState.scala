package dev.capslock.feedmax
package infra

import domain.repo.StateRepository
import dev.capslock.feedmax.domain.repo.State
import zio.*
import zio.json.*
import scala.util.Try
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets

class FileState extends StateRepository:
  given JsonDecoder[State] = DeriveJsonDecoder.gen[State]
  given JsonEncoder[State] = DeriveJsonEncoder.gen[State]

  def loadState: IO[Throwable, State] =
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

  def saveState(state: State): IO[Throwable, State] =
    ZIO
      .succeed(
        Files.write(
          Paths.get("state.json"),
          state.toJson.getBytes(StandardCharsets.UTF_8),
        ),
      )
      .as(state)
  end saveState
end FileState

object FileState:
  val layer: ULayer[StateRepository] = ZLayer.succeed(new FileState)
end FileState
