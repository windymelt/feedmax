package dev.capslock.feedmax.app

import dev.capslock.feedmax.domain.NotifiableInfos
import zio.*
import dev.capslock.feedmax.domain.repo.StateRepository

object Notify:
  def notify(
      notifiableInfosList: Seq[NotifiableInfos],
  ): ZIO[StateRepository, Throwable, Unit] =
    val infos = notifiableInfosList.flatMap(_.infos)
    val string =
      infos.map(_.title).mkString("\n")
    if string.isEmpty then ZIO.unit
    else
      for
        _         <- zio.Console.printLine(string) // TODO; inject notifier
        stateRepo <- ZIO.service[StateRepository]
        state     <- stateRepo.loadOrCreateState()
        _ <- stateRepo.saveState(
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
