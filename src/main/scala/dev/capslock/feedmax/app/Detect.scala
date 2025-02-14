package dev.capslock.feedmax
package app

import dev.capslock.rss4s.Feed
import dev.capslock.rss4s.ParseFeedError
import zio.*
import dev.capslock.feedmax.domain.NotifiableInfos

object Detect:
  def filterSuccessfulFeeds(
      feeds: Seq[Either[ParseFeedError, Feed]],
  ): ZIO[Any, java.io.IOException, Seq[Feed]] = // TODO: log, defer error
    val (successfulFeeds, errors) = feeds.partition(_.isRight)
    ZIO.collectAll(errors.map(e => zio.Console.printLineError(e.left.get))) *>
      ZIO.succeed(successfulFeeds.map(_.toOption.get))

  def detectUnnotifiedItems(
      feeds: Seq[Feed],
  ): ZIO[Any, Nothing, Seq[NotifiableInfos]] =
    // TODO: implement this
    ZIO.succeed(feeds.map(infra.Feed.feedToNotifiableInfos))
end Detect
