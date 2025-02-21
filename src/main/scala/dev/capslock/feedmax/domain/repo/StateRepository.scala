package dev.capslock.feedmax.domain.repo

import zio.*
import zio.json.JsonDecoder
import zio.json.DeriveJsonDecoder
import zio.json.JsonEncoder
import zio.json.DeriveJsonEncoder

trait StateRepository:
  def loadState: IO[Throwable, State]
  def saveState(state: State): IO[Throwable, State]

object StateRepository:
  def loadState(): ZIO[StateRepository, Throwable, State] =
    ZIO.serviceWithZIO[StateRepository](_.loadState)

  def saveState(state: State): ZIO[StateRepository, Throwable, State] =
    ZIO.serviceWithZIO[StateRepository](_.saveState(state))

  def loadOrCreateState(): ZIO[StateRepository, Throwable, State] =
    loadState() <> saveState(State())
end StateRepository

case class State(
    lastFetched: Option[java.time.OffsetDateTime] = None,
    lastModifiedPerFeed: Map[String, java.time.OffsetDateTime] = Map.empty,
    lastNotified: Option[java.time.OffsetDateTime] = None,
)
