package dev.capslock.feedmax

import zio.IO

import zio.config.*
import zio.config.typesafe.*
import zio.ConfigProvider
import zio.Config, Config.*
import zio.Chunk
import java.net.URI

case class FeedMaxConfig(feeds: Vector[URI])

// can be auto generated using magnoria, but not doing it here because it's a bit too much
// TODO: validate URI as HTTP/HTTPS
val config: Config[FeedMaxConfig] =
  vectorOf("feeds", (uri ?? "Feed URL")).to[FeedMaxConfig]

private val source =
  ConfigProvider.fromHoconFile(new java.io.File("application.conf"))

lazy val feedMaxConfig = source.load(config)
