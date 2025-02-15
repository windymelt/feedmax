package dev.capslock.feedmax
package app

import java.net.URI
import dev.capslock.rss4s.Feed
import dev.capslock.rss4s.ParseFeedError
import infra.Fetcher
import zio.*
import domain.FetchRequest
import dev.capslock.feedmax.infra.Fetcher.Result

object Fetch:
  def batchFetch(
      uris: Seq[URI],
  ): ZIO[Any, Throwable, Seq[Result]] =
    // TODO: group same origin
    ZIO.collectAllPar(uris.map { uri =>
      val fetchRequest = FetchRequest(
        uri.toString,
        lastModified =
          Some(java.time.OffsetDateTime.parse("2025-02-14T00:30:44Z")), // TODO
      )
      Fetcher.fetchFeed(fetchRequest).provide(zio.http.Client.default)
    })
end Fetch
