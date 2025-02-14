package dev.capslock.feedmax

import zio.*
import zio.Console.*
import dev.capslock.feedmax.infra.Fetcher.fetchFeed

object Main extends ZIOAppDefault:
  def run = for
    _          <- printLine("FeedMax")
    conf       <- feedMaxConfig
    feeds      <- app.Fetch.batchFetch(conf.feeds)
    okFeeds    <- app.Detect.filterSuccessfulFeeds(feeds)
    unnotified <- app.Detect.detectUnnotifiedItems(okFeeds)
    _ <- ZIO.collectAll(
      unnotified
        .map(f => printLine(f.infos.head.title)),
    )
  yield ()
end Main
