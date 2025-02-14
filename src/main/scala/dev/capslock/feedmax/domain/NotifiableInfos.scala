package dev.capslock.feedmax.domain

final case class NotifiableInfos(
    title: String,
    link: String,
    infos: Seq[NotifiableInfo],
)

final case class NotifiableInfo(
    title: String,
    link: String,
    description: Option[String],
    pubDate: Option[java.time.OffsetDateTime],
)
