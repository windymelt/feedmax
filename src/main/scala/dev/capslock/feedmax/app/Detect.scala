package dev.capslock.feedmax.app

import dev.capslock.rss4s.Feed
import dev.capslock.rss4s.ParseFeedError
import zio.*

object Detect:
  def filterSuccessfulFeeds(
      feeds: Seq[Either[ParseFeedError, Feed]],
  ): ZIO[Any, java.io.IOException, Seq[Feed]] = // TODO: log, defer error
    val (successfulFeeds, errors) = feeds.partition(_.isRight)
    ZIO.collectAll(errors.map(e => zio.Console.printLineError(e.left.get))) *>
      ZIO.succeed(successfulFeeds.map(_.toOption.get))
end Detect
