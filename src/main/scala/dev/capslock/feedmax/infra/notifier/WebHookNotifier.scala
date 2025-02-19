package dev.capslock.feedmax
package infra.notifier

import java.io.IOException
import dev.capslock.feedmax.domain.NotifiableInfos
import zio.http.*
import zio.*

case class DiscordWebHookPayload(
    name: String,
    content: String,
    avatar_url: Option[String] = None,
    // TODO: embeds
)
object DiscordWebHookPayload:
  import zio.json.*
  given JsonCodec[DiscordWebHookPayload] =
    DeriveJsonCodec.gen[DiscordWebHookPayload]

class WebHookNotifier(webHookTarget: java.net.URL, client: Client)
    extends domain.repo.Notifier:

  override def notify(
      notifications: Seq[NotifiableInfos],
  ): ZIO[Any, Throwable, Unit] =
    val targetUrl = URL.fromURI(webHookTarget.toURI).get
    val requests = notifications.map { ns =>
      Request
        .post(targetUrl, Body.fromString(formatNotifications(ns)))
        .addHeader(Header.ContentType(MediaType("application", "json")))
    }

    val posts =
      for _ <- ZIO.collectAll(requests.map(client.batched))
      yield ()

    posts.unit
  end notify

  def formatNotifications(notifications: NotifiableInfos): String =
    import zio.json.*
    val infos = notifications.infos
    val string =
      infos
        .map(i => s"""
        |Title: ${i.title}
        |Link: ${i.link}
        |Description: ${i.description.getOrElse("")}
        |PubDate: ${i.pubDate.getOrElse("")}
        |
        |
      """.stripMargin)
        .mkString("\n")
    DiscordWebHookPayload(
      name = notifications.title,
      content = string,
    ).toJson
  end formatNotifications
end WebHookNotifier

object WebHookNotifier:
  def layer(webHookTarget: java.net.URL): ZLayer[
    ZClient[Any, Scope, Body, Throwable, Response],
    Nothing,
    WebHookNotifier,
  ] = ZLayer {
    for client <- ZIO.service[Client]
    yield new WebHookNotifier(webHookTarget, client)
  }
end WebHookNotifier
