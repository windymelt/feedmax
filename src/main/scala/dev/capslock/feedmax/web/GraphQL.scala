package dev.capslock.feedmax.web

import caliban.*
import caliban.schema.Schema
import caliban.schema.ArgBuilder
import caliban.schema.Schema.auto.*
import caliban.schema.ArgBuilder.auto.*
import caliban.quick.*
import zio.*
import zio.http.*
import java.net.URI

import dev.capslock.feedmax.FeedMaxConfig
import dev.capslock.feedmax.NotifierConfig
import dev.capslock.feedmax.WebHookConfig
import zio.Config.Secret

// GraphQLスキーマに対応するScalaのデータ型
object Types:
  implicit val uriSchema: Schema[Any, URI] =
    Schema.stringSchema.contramap(_.toString)
  implicit val secretSchema: Schema[Any, Secret] =
    Schema.stringSchema.contramap(_.toString)

  case class Queries(
      config: UIO[FeedMaxConfig],
  )

  case class Mutations(
      updateConfig: FeedMaxConfigInput => UIO[FeedMaxConfig],
  )

  case class FeedMaxConfigInput(
      feeds: List[String],
      notifier: NotifierConfigInput,
  )

  case class NotifierConfigInput(
      `type`: String,
      webhook: Option[WebHookConfigInput],
  )

  case class WebHookConfigInput(
      url: String,
      bearerToken: Option[String],
  )

  extension (input: FeedMaxConfigInput)
    def toDomain: FeedMaxConfig =
      FeedMaxConfig(
        feeds = input.feeds.map(URI.create).toVector,
        notifier = input.notifier.toDomain,
      )

  extension (input: NotifierConfigInput)
    def toDomain: NotifierConfig =
      NotifierConfig(
        `type` = input.`type`,
        webhook = input.webhook.map(_.toDomain),
      )

  extension (input: WebHookConfigInput)
    def toDomain: WebHookConfig =
      WebHookConfig(
        url = URI.create(input.url),
        bearerToken = input.bearerToken.map(Secret.apply),
      )
end Types

object GraphQL:
  import Types.*
  import caliban.schema.Schema.auto.*
  import caliban.schema.ArgBuilder.auto.*

  // dummy
  val queries = Queries(
    config = ZIO.succeed(
      FeedMaxConfig(
        feeds = Vector(
          URI.create("https://example.com/feed.xml"),
          URI.create("https://example.org/rss"),
        ),
        notifier = NotifierConfig(
          `type` = "webhook",
          webhook = Some(
            WebHookConfig(
              url = URI.create("https://example.com/webhook"),
              bearerToken = Some(Secret("dummy-token")),
            ),
          ),
        ),
      ),
    ),
  )

  val mutations = Mutations(
    updateConfig = input => ZIO.succeed(input.toDomain),
  )

  val api = graphQL(RootResolver(queries, mutations))

  val routes: ZIO[Any, CalibanError.ValidationError, Routes[Any, Response]] =
    for handlers <- api.handlers
    yield Routes(
      Method.POST / "api" / "graphql" -> handlers.api,
      Method.GET / "api" / "graphql" -> GraphiQLHandler.handler(
        apiPath = "/api/graphql",
        graphiqlPath = "/graphiql",
      ),
    )
end GraphQL
