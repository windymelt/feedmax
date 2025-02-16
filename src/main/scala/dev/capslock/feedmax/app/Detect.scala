package dev.capslock.feedmax
package app

import dev.capslock.rss4s.Feed
import dev.capslock.rss4s.ParseFeedError
import zio.*
import dev.capslock.feedmax.domain.NotifiableInfos
import dev.capslock.feedmax.infra.Fetcher.Result

object Detect:
  def filterSuccessfulFeeds(
      feeds: Seq[Result],
  ): ZIO[Any, java.io.IOException, Seq[Feed]] = // TODO: log, defer error
    val (successfulFeeds, errors) = feeds
      .collect { case Result.Fetched(feedEither, lastModified, url) =>
        feedEither
      // Ignore unmodified feeds
      // TODO: filter by last fetched (not last modified)
      }
      .partition(_.isRight)

    val showIgnoredFeeds = ZIO.collectAll(feeds.collect {
      case Result.NotModified(url) =>
        zio.Console.printLine(s"Feed not modified and ignored: ${url}")
    })

    showIgnoredFeeds *> ZIO.collectAll(
      errors.map(e => zio.Console.printLineError(e.left.get)),
    ) *>
      ZIO.succeed(successfulFeeds.map(_.toOption.get))
  end filterSuccessfulFeeds

  def detectUnnotifiedItems(
      feeds: Seq[Feed],
  ): ZIO[Any, Nothing, Seq[NotifiableInfos]] =
    // TODO: implement this
    ZIO.succeed(feeds.map(infra.Feed.feedToNotifiableInfos))
end Detect
