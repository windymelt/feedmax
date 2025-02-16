package dev.capslock.feedmax.infra

import zio.*
import zio.http.*
import dev.capslock.feedmax.domain.FetchRequest
import dev.capslock.rss4s.ParseFeedError

object Fetcher:
  enum Result:
    case Fetched(
        result: Either[ParseFeedError, dev.capslock.rss4s.Feed],
        lastModified: Option[java.time.OffsetDateTime],
        url: String,
    )
    case NotModified(url: String)

  // TODO: keep conn using stream client to avoid reconnecting same host
  // TODO: Last-Modified, ETag, If-Modified-Since, If-None-Match
  def fetchFeed(
      fetchRequest: FetchRequest,
  ): ZIO[ZClient[Any, Scope, Body, Throwable, Response], Throwable, Result] =
    val feedUrl =
      URL.decode(fetchRequest.url).toOption.get // TODO: handle error
    for
      c <- ZIO.service[Client]
      _ <- zio.Console.printLine(s"Fetching feed from $feedUrl")
      req = Request.get(feedUrl)
      resp <- clientWithCache(c, fetchRequest.lastModified)
        .batched(req)
        .debug
      result <- handleResult(req, resp)
    yield result
    end for
  end fetchFeed

  private def clientWithCache(
      cli: ZClient[Any, Scope, Body, Throwable, Response],
      lastModified: Option[java.time.OffsetDateTime],
  ) = lastModified match
    case Some(lm) =>
      val truncated = lm
        .toZonedDateTime()
        .truncatedTo(
          java.time.temporal.ChronoUnit.SECONDS,
        )
      // TODO: If-Modified-Since per feed. some server requires exactly same time as response's Last-Modified.
      val header = Header.IfModifiedSince(truncated)
      cli.addHeader(header)
    case None =>
      cli

  private def handleResult(
      req: Request,
      resp: Response,
  ) =
    resp.status match
      case Status.NotModified =>
        ZIO.succeed(Result.NotModified(req.url.toString))
      case _ =>
        for body <- resp.body.asString
        yield Result.Fetched(
          dev.capslock.rss4s.parseFeed(body),
          lastModified = resp
            .header(Header.LastModified)
            .map(lm => lm.value.toOffsetDateTime()),
          url = req.url.toString,
        )
end Fetcher
