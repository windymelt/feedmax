package dev.capslock.feedmax.infra

import zio.*
import zio.http.*
import dev.capslock.feedmax.domain.FetchRequest
import dev.capslock.rss4s.ParseFeedError

object Fetcher:
  enum Result:
    case Fetched(result: Either[ParseFeedError, dev.capslock.rss4s.Feed])
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
  end fetchFeed

  private def clientWithCache(
      cli: ZClient[Any, Scope, Body, Throwable, Response],
      lastModified: Option[java.time.OffsetDateTime],
  ) = lastModified match
    case Some(lm) =>
      cli.addHeader(Header.IfModifiedSince(lm.toZonedDateTime()))
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
        yield Result.Fetched(dev.capslock.rss4s.parseFeed(body))
end Fetcher
