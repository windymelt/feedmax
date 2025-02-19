package dev.capslock.feedmax

import zio.*
import zio.Console.*
import dev.capslock.feedmax.infra.Fetcher.fetchFeed

object Main extends ZIOAppDefault:
  def run =
    program.provide(
      infra.FileState.layer,
      infra.notifier.StdoutPlainNotifier.layer,
      zio.http.Client.default,
    )

  val program = for
    _          <- printLine("FeedMax")
    conf       <- feedMaxConfig
    feeds      <- app.Fetch.batchFetch(conf.feeds)
    okFeeds    <- app.Detect.filterSuccessfulFeeds(feeds.feeds)
    unnotified <- app.Detect.detectUnnotifiedItems(okFeeds)
    _          <- app.Notify.notify(unnotified) // TODO: webhook notifier
  yield ()
end Main
