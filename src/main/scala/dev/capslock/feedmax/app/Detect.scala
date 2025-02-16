package dev.capslock.feedmax
package app

import dev.capslock.rss4s.Feed
import dev.capslock.rss4s.ParseFeedError
import zio.*
import dev.capslock.feedmax.domain.NotifiableInfos
import dev.capslock.feedmax.infra.Fetcher.Result
import dev.capslock.feedmax.domain.repo.StateRepository

object Detect:
  def filterSuccessfulFeeds(
      feeds: Seq[Result],
  ): ZIO[Any, java.io.IOException, Seq[Feed]] = // TODO: log, defer error
    val (successfulFeeds, errors) = feeds
      .collect { case Result.Fetched(feedEither, lastModified, url) =>
        feedEither
      // Ignore unmodified feeds
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
  ): ZIO[StateRepository, Throwable, Seq[NotifiableInfos]] = for
    stateRepo <- ZIO.service[StateRepository]
    state     <- stateRepo.loadOrCreateState()
    unnotified = feeds
      .map(infra.Feed.feedToNotifiableInfos)
      .flatMap { infos =>
        val lastNotified =
          state.lastNotified.getOrElse(java.time.OffsetDateTime.MIN)
        val newInfos = infos.infos
          .filter(
            _.pubDate
              .getOrElse(java.time.OffsetDateTime.MAX) // TODO: bloom filter
              .isAfter(lastNotified),
          )
        if newInfos.nonEmpty then Seq(infos.copy(infos = newInfos))
        else Seq.empty
      }
  yield unnotified
end Detect
