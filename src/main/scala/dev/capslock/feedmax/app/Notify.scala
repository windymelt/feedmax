package dev.capslock.feedmax.app

import dev.capslock.feedmax.domain.NotifiableInfos
import zio.*
import dev.capslock.feedmax.domain.repo.StateRepository
import dev.capslock.feedmax.domain.repo.Notifier

object Notify:
  def notify(
      notifiableInfosList: Seq[NotifiableInfos],
  ): ZIO[StateRepository & Notifier, Throwable, Unit] =
    val infos = notifiableInfosList.flatMap(_.infos)
    val string =
      infos.map(_.title).mkString("\n")
    if string.isEmpty then ZIO.unit
    else
      for
        notifier <- ZIO.service[Notifier]
        _        <- notifier.notify(notifiableInfosList)
        state    <- StateRepository.loadOrCreateState()
        _ <- StateRepository.saveState(
          state.copy(
            lastNotified = Some(
              java.time.OffsetDateTime.now(),
            ),
          ),
        )
      yield ()
      end for
    end if
  end notify
end Notify
