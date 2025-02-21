package dev.capslock.feedmax

import zio.*
import zio.Console.*
import dev.capslock.feedmax.infra.Fetcher.fetchFeed
import dev.capslock.feedmax.domain.repo.ConfigRepository

object Main extends ZIOAppDefault:
  private def notifierLayer = for
    conf <- ZIO.service[ConfigRepository].flatMap(_.getConfig)
    notifier <- conf.notifier.`type` match
      case "webhook" => ZIO.succeed(infra.notifier.WebHookNotifier.layer)
      case "stdout"  => ZIO.succeed(infra.notifier.StdoutPlainNotifier.layer)
      case other =>
        ZIO.fail(new IllegalStateException(s"Unknown notifier type: $other"))
  yield notifier

  def run = for
    layer <- notifierLayer.provide(infra.FileConfig.layer)
    _ <- program.provide(
      infra.FileState.layer,
      infra.FileConfig.layer,
      layer,
      zio.http.Client.default,
    )
  yield ()

  val program = for
    _          <- printLine("FeedMax")
    conf       <- ConfigRepository.getConfig
    feeds      <- app.Fetch.batchFetch(conf.feeds)
    okFeeds    <- app.Detect.filterSuccessfulFeeds(feeds.feeds)
    unnotified <- app.Detect.detectUnnotifiedItems(okFeeds)
    _          <- app.Notify.notify(unnotified)
  yield ()
end Main
