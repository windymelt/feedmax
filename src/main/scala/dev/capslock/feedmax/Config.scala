package dev.capslock.feedmax

import java.net.URI
import zio.Config.Secret

/** Configuration model for FeedMax
  *
  * @param feeds
  *   List of feed URLs to monitor
  * @param notifier
  *   Notifier configuration
  */
case class FeedMaxConfig(
    feeds: Vector[URI],
    notifier: NotifierConfig,
)

case class NotifierConfig(
    `type`: String, // "stdout" or "webhook"
    webhook: Option[WebHookConfig] = None,
)

case class WebHookConfig(
    url: URI,
    bearerToken: Option[Secret] = None,
)
