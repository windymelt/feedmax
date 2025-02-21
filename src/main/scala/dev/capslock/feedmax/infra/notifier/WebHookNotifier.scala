package dev.capslock.feedmax
package infra.notifier

import java.io.IOException
import dev.capslock.feedmax.domain.NotifiableInfos
import dev.capslock.feedmax.WebHookConfig
import zio.http.*
import zio.*
import dev.capslock.feedmax.domain.repo.ConfigRepository
import zio.Config.Secret

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

class WebHookNotifier(configRepo: ConfigRepository, client: Client)
    extends domain.repo.Notifier:

  override def notify(
      notifications: Seq[NotifiableInfos],
  ): ZIO[Any, Throwable, Unit] = for
    config <- configRepo.getConfig
    webhookConfig <- ZIO
      .fromOption(config.notifier.webhook)
      .orElseFail(
        new IllegalStateException("Webhook configuration is not set"),
      )
    targetUrl = URL.fromURI(webhookConfig.url).get
    requests = notifications.map { ns =>
      buildRequest(
        targetUrl,
        formatNotifications(ns),
        webhookConfig.bearerToken,
      )
    }
    _ <- ZIO.collectAll(requests.map(client.batched))
  yield ()
  end notify

  private def buildRequest(
      url: URL,
      body: String,
      bearerToken: Option[Secret],
  ): Request =
    val req = Request
      .post(url, Body.fromString(body))
      .addHeader(Header.ContentType(MediaType("application", "json")))
    bearerToken match
      case Some(token) => req.addHeader(Header.Authorization.Bearer(token))
      case None        => req
  end buildRequest

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
  def layer: ZLayer[
    ConfigRepository & ZClient[Any, Scope, Body, Throwable, Response],
    Nothing,
    WebHookNotifier,
  ] = ZLayer {
    for
      configRepo <- ZIO.service[ConfigRepository]
      client     <- ZIO.service[Client]
    yield new WebHookNotifier(configRepo, client)
  }
end WebHookNotifier
