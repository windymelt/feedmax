package dev.capslock.feedmax

import zio.*
import zio.Console.*
import dev.capslock.feedmax.infra.Fetcher.fetchFeed

object Main extends ZIOAppDefault:
  def run = for
    _       <- printLine("FeedMax")
    conf    <- feedMaxConfig
    feeds   <- app.Fetch.batchFetch(conf.feeds)
    okFeeds <- app.Detect.filterSuccessfulFeeds(feeds)
    _ <- ZIO.collectAll(
      okFeeds
        .map(f => printLine(f.items.head.title)),
    )
  yield ()
end Main
