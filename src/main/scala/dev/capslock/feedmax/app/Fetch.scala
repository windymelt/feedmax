package dev.capslock.feedmax
package app

import java.net.URI
import dev.capslock.rss4s.Feed
import dev.capslock.rss4s.ParseFeedError
import infra.Fetcher
import zio.*
import domain.FetchRequest

object Fetch:
  def batchFetch(
      uris: Seq[URI],
  ): ZIO[Any, Throwable, Seq[Either[ParseFeedError, Feed]]] =
    // TODO: group same origin
    ZIO.collectAllPar(uris.map { uri =>
      val fetchRequest = FetchRequest(uri.toString)
      Fetcher.fetchFeed(fetchRequest).provide(zio.http.Client.default)
    })
end Fetch
