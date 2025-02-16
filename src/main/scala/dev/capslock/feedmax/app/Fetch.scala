package dev.capslock.feedmax
package app

import java.net.URI
import dev.capslock.rss4s.Feed
import dev.capslock.rss4s.ParseFeedError
import infra.Fetcher
import zio.*
import domain.FetchRequest
import dev.capslock.feedmax.infra.Fetcher.Result
import dev.capslock.feedmax.domain.repo.StateRepository

object Fetch:
  def batchFetch(
      uris: Seq[URI],
  ): ZIO[StateRepository, Throwable, Seq[Result]] =
    // TODO: group same origin
    for
      stateRepo <- ZIO.service[StateRepository]
      state     <- stateRepo.loadOrCreateState()
      fetchedAt = java.time.OffsetDateTime.now()
      result <- ZIO.collectAllPar(uris.map { uri =>
        val fetchRequest = FetchRequest(
          uri.toString,
          lastModified = state.lastModifiedPerFeed.get(uri.toString),
        )
        Fetcher.fetchFeed(fetchRequest).provide(zio.http.Client.default)
      })
      _ <- stateRepo.saveState(
        state.copy(
          lastFetched = Some(fetchedAt),
          lastModifiedPerFeed = state.lastModifiedPerFeed ++ result.flatMap {
            case Result.Fetched(_, lastModified, url) =>
              lastModified.map(url -> _)
            case _ => None
          }.toMap,
        ),
      )
    yield result
end Fetch
