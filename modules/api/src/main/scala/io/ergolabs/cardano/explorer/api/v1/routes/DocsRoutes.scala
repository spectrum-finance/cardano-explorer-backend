package io.ergolabs.cardano.explorer.api.v1.routes

import cats.effect.{Concurrent, ContextShift, Sync, Timer}
import io.ergolabs.cardano.explorer.api.v1.endpoints.{AssetsEndpoints, BlocksEndpoints, DocsEndpoints, OutputsEndpoints, TransactionsEndpoints}
import sttp.tapir.apispec.Tag
import cats.syntax.semigroupk._
import cats.syntax.option._
import org.http4s.HttpRoutes
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import cats.syntax.either._
import cats.syntax.applicative._
import cats.syntax.flatMap._
import io.ergolabs.cardano.explorer.api.configs.RequestConfig
import io.ergolabs.cardano.explorer.api.v1.HttpError
import sttp.tapir.redoc.http4s.RedocHttp4s
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}

final class DocsRoutes[F[_]: Concurrent: ContextShift: Timer](config: RequestConfig)(implicit
  opts: Http4sServerOptions[F, F]
) {

  private val endpoints = DocsEndpoints
  import endpoints._

  private val interpreter = Http4sServerInterpreter(opts)

  val routes: HttpRoutes[F] = openApiSpecR <+> redocApiSpecR

  val transactionsEndpoints = new TransactionsEndpoints(config)
  val outputsEndpoints = new OutputsEndpoints(config)

  private def allEndpoints =
    AssetsEndpoints.endpoints ++
    BlocksEndpoints.endpoints ++
    outputsEndpoints.endpoints ++
    transactionsEndpoints.endpoints

  private def tags =
    Tag(AssetsEndpoints.pathPrefix, "Assets methods".some) ::
    Tag(BlocksEndpoints.pathPrefix, "Blocks methods".some) ::
    Tag(outputsEndpoints.pathPrefix, "Outputs methods".some) ::
    Tag(transactionsEndpoints.pathPrefix, "Transactions methods".some) ::
    Nil

  private val docsAsYaml =
    OpenAPIDocsInterpreter()
      .toOpenAPI(allEndpoints, "Cardano Explorer API v1", "1.0")
      .tags(tags)
      .toYaml

  private def openApiSpecR: HttpRoutes[F] =
    interpreter.toRoutes(apiSpecDef) { _ =>
      docsAsYaml
        .asRight[HttpError]
        .pure[F]
    }

  private def redocApiSpecR: HttpRoutes[F] =
    new RedocHttp4s(
      "Redoc",
      docsAsYaml,
      "openapi",
      contextPath = "docs" :: Nil
    ).routes
}

object DocsRoutes {

  def make[F[_]: Concurrent: ContextShift: Timer](config: RequestConfig)(implicit opts: Http4sServerOptions[F, F]): HttpRoutes[F] =
    new DocsRoutes[F](config).routes
}
