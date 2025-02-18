package dev.capslock.feedmax
package infra.notifier

import zio.*
import java.io.IOException

object StdoutPlainNotifier extends domain.repo.Notifier:
  def notify(
      notifications: Seq[domain.NotifiableInfos],
  ): ZIO[Any, IOException, Unit] =
    ZIO
      .collectAll(notifications.map { notification =>
        for
          _ <- zio.Console.printLine(s"Title: ${notification.title}")
          _ <- zio.Console.printLine(s"Link: ${notification.link}")
          _ <- ZIO.foreach(notification.infos) { info =>
            for
              _ <- zio.Console.printLine(s"Info: ${info.title}")
              _ <- zio.Console.printLine(s"Link: ${info.link}")
              _ <- info.description.fold(ZIO.unit)(description =>
                zio.Console.printLine(s"Description: $description"),
              )
              _ <- info.pubDate.fold(ZIO.unit)(pubDate =>
                zio.Console.printLine(s"PubDate: $pubDate"),
              )
            yield ()
          }
        yield ()
      })
      .unit

  val layer: ZLayer[Any, Nothing, domain.repo.Notifier] =
    ZLayer.succeed(this)
end StdoutPlainNotifier
