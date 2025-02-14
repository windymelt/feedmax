package dev.capslock.feedmax

import zio.*
import zio.Console.*
import dev.capslock.feedmax.infra.Fetcher.fetchFeed

object Main extends ZIOAppDefault:
  def run = for
    _    <- printLine("FeedMax")
    conf <- feedMaxConfig
    _ <- fetchFeed(
      FetchRequest(
        conf.feeds.head.toString(),
      ),
    )
      .provide(zio.http.Client.default)
      .map(f => f.fold(_ => ???, f => f.items.head))
      .debug
  yield ()
end Main
