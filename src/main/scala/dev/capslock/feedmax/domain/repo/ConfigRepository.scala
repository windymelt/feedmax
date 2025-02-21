package dev.capslock.feedmax.domain.repo

import dev.capslock.feedmax.FeedMaxConfig
import zio.*

/** Repository for managing FeedMaxConfig
  */
trait ConfigRepository:
  /** Retrieves the current configuration
    *
    * @return
    *   Current FeedMaxConfig wrapped in ZIO
    */
  def getConfig: IO[Throwable, FeedMaxConfig]

  /** Updates the configuration
    *
    * @param config
    *   New configuration to save
    * @return
    *   Unit wrapped in ZIO indicating success or failure
    */
  def updateConfig(config: FeedMaxConfig): IO[Throwable, Unit]
end ConfigRepository

object ConfigRepository:
  // Access methods
  def getConfig: ZIO[ConfigRepository, Throwable, FeedMaxConfig] =
    ZIO.serviceWithZIO[ConfigRepository](_.getConfig)

  def updateConfig(
      config: FeedMaxConfig,
  ): ZIO[ConfigRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[ConfigRepository](_.updateConfig(config))
end ConfigRepository
