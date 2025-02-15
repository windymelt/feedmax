package dev.capslock.feedmax.infra

import zio.*
import zio.http.*
import dev.capslock.feedmax.domain.FetchRequest

object Fetcher:
  // TODO: keep conn using stream client to avoid reconnecting same host
  // TODO: Last-Modified, ETag, If-Modified-Since, If-None-Match
  def fetchFeed(fetchRequest: FetchRequest) =
    val feedUrl =
      URL.decode(fetchRequest.url).toOption.get // TODO: handle error
    for
      c    <- ZIO.service[Client]
      _    <- zio.Console.printLine(s"Fetching feed from $feedUrl")
      resp <- c.batched(Request.get(feedUrl)).debug
      body <- resp.body.asString
    yield dev.capslock.rss4s.parseFeed(body)
end Fetcher
