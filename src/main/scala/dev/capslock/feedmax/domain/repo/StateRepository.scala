package dev.capslock.feedmax.domain.repo

import zio.*
import zio.json.JsonDecoder
import zio.json.DeriveJsonDecoder
import zio.json.JsonEncoder
import zio.json.DeriveJsonEncoder

trait StateRepository:
  def loadOrCreateState(): ZIO[Any, Throwable, State] =
    loadState() <> saveState(State())
  def loadState(): ZIO[Any, Throwable, State]
  def saveState(state: State): ZIO[Any, Throwable, State]

case class State(
    lastFetched: Option[java.time.OffsetDateTime] = None,
)
object State:
  implicit val decoder: JsonDecoder[State] = DeriveJsonDecoder.gen[State]
  implicit val encoder: JsonEncoder[State] = DeriveJsonEncoder.gen[State]
