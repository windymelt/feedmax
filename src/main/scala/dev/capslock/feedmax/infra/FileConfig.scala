package dev.capslock.feedmax.infra

import dev.capslock.feedmax.domain.repo.ConfigRepository
import dev.capslock.feedmax.{FeedMaxConfig, NotifierConfig, WebHookConfig}
import zio.*
import zio.config.*
import zio.Config.*
import zio.config.typesafe.*
import java.io.File
import java.net.URI

class FileConfig extends ConfigRepository:
  private val configFile = new File("application.conf")

  // can be auto generated using magnoria, but not doing it here because it's a bit too much
  // TODO: validate URI as HTTP/HTTPS
  private val webhookConfigDescriptor: Config[WebHookConfig] =
    (uri("url") ?? "Webhook URL" zip
      secret(
        "bearerToken",
      ).optional ?? "Bearer token for webhook authentication")
      .to[WebHookConfig]

  private val notifierConfigDescriptor: Config[NotifierConfig] =
    (string("type") ?? "Notifier type (stdout or webhook)" zip
      webhookConfigDescriptor.optional.nested("webhook"))
      .to[NotifierConfig]

  private val configDescriptor: Config[FeedMaxConfig] =
    (vectorOf("feeds", uri ?? "Feed URL") zip
      notifierConfigDescriptor.nested("notifier"))
      .to[FeedMaxConfig]

  override def getConfig: IO[Throwable, FeedMaxConfig] =
    ZIO.attempt {
      val source = ConfigProvider.fromHoconFile(configFile)
      source.load(configDescriptor).orDie
    }.flatten

  override def updateConfig(config: FeedMaxConfig): IO[Throwable, Unit] =
    ZIO.attempt {
      // TODO: implement config file update
      // This is a placeholder that needs to be implemented
      // We need to:
      // 1. Convert FeedMaxConfig to HOCON format
      // 2. Write to application.conf
      ???
    }
end FileConfig

object FileConfig:
  val layer: ULayer[ConfigRepository] = ZLayer.succeed(new FileConfig)
end FileConfig
