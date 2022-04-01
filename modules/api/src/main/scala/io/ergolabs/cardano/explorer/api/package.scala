package io.ergolabs.cardano.explorer

import cats.{~>, Applicative, Defer}
import cats.effect.{Concurrent, ContextShift, Resource}
import io.ergolabs.cardano.explorer.api.configs.ClientTimeouts
import monix.eval.Task
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import sttp.capabilities.WebSockets
import sttp.client3._
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.monix.AsyncHttpClientMonixBackend
import sttp.client3.impl.cats.implicits._
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import tofu.WithRun

package object api {

  def makeSttpBackend[I[_]: Defer: Applicative, F[_]: Concurrent: ContextShift](
    ctx: AppContext,
    httpClientTimeouts: Option[ClientTimeouts] = None
  )(implicit
    WR: WithRun[F, I, AppContext],
    fromTaskI: Task ~> I,
    fromTaskF: Task ~> F,
    toTaskF: F ~> Task
  ): Resource[I, SttpBackend[F, WebSockets]] =
    AsyncHttpClientMonixBackend.resource().mapK(fromTaskI).map(_.mapK(fromTaskF, toTaskF))

  def setupTimeouts(builder: DefaultAsyncHttpClientConfig.Builder, config: ClientTimeouts) = {
    import config._
    builder
      .setConnectTimeout(connectTimeout.toMillis.toInt)
      .setReadTimeout(timeout.toMillis.toInt)
      .setRequestTimeout(
        timeout.toMillis.toInt + connectTimeout.toMillis.toInt * 2
      )
  }

  def wrapWithLogs[F[_], S](delegate: SttpBackend[F, S]): SttpBackend[F, S] =
    Slf4jLoggingBackend(delegate, logRequestBody = true, logResponseBody = true)
}
