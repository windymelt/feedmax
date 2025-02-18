package dev.capslock.feedmax.domain.repo

import zio.*
import dev.capslock.feedmax.domain.NotifiableInfos
import java.io.IOException

trait Notifier:
  def notify(
      notifications: Seq[NotifiableInfos],
  ): ZIO[Any, IOException, Unit]
