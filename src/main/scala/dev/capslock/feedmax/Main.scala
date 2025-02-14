package dev.capslock.feedmax

import zio.*
import zio.Console.*
import dev.capslock.feedmax.infra.Fetcher.fetchFeed

object Main extends ZIOAppDefault:
  def run = for
    _    <- printLine("FeedMax")
    conf <- feedMaxConfig
    feeds <- app.Fetch
      .batchFetch(conf.feeds)
    _ <- ZIO.collectAll(
      feeds.map(f => f.fold(_ => ???, f => f.items.head)).map(i => printLine(i)),
    )
  yield ()
end Main
