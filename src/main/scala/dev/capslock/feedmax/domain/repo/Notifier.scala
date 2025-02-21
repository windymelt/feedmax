package dev.capslock.feedmax.domain.repo

import zio.*
import dev.capslock.feedmax.domain.NotifiableInfos
import java.io.IOException

trait Notifier:
  def notify(notifications: Seq[NotifiableInfos]): IO[Throwable, Unit]

object Notifier:
  def notify(
      notifications: Seq[NotifiableInfos],
  ): ZIO[Notifier, Throwable, Unit] =
    ZIO.serviceWithZIO[Notifier](_.notify(notifications))
end Notifier
