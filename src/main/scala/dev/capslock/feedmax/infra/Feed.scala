package dev.capslock.feedmax.infra

object Feed:
  def feedToNotifiableInfos(
      feed: dev.capslock.rss4s.Feed,
  ): dev.capslock.feedmax.domain.NotifiableInfos =
    val infos = feed.items.map { item =>
      dev.capslock.feedmax.domain.NotifiableInfo(
        title = item.title,
        link = item.link.toString,
        description = item.description,
        pubDate = item.publishedAt,
      )
    }
    dev.capslock.feedmax.domain.NotifiableInfos(
      title = feed.title,
      link = feed.link.toString,
      infos = infos,
    )
  end feedToNotifiableInfos
end Feed
