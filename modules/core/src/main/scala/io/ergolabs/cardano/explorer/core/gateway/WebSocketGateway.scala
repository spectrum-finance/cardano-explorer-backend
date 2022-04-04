package io.ergolabs.cardano.explorer.core.gateway

import cats.effect.Sync
import cats.{Apply, Functor, Monad}
import io.circe.{Decoder, Encoder, Json}
import sttp.capabilities.WebSockets
import cats.syntax.either._
import derevo.derive
import io.ergolabs.cardano.explorer.core.gateway.GatewayError.{GatewayResponseDecodingFailure, UnexpectedError}
import sttp.client3.{ws, _}
import sttp.model.Uri
import sttp.ws.WebSocket
import tofu.Raise
import tofu.higherKind.Mid
import tofu.higherKind.derived.representableK
import tofu.logging.{Loggable, Logging, Logs}
import tofu.syntax.raise._
import tofu.syntax.monadic._
import tofu.syntax.logging._
import io.circe.parser.decode
import tofu.syntax.embed._

@derive(representableK)
trait WebSocketGateway[F[_]] {

  def send[Req, Res](req: Req)(implicit encoder: Encoder[Req], decoder: Decoder[Res]): F[Res]
}

object WebSocketGateway {

  def make[I[_]: Functor, F[_]: Sync: Raise[*[_], GatewayError]](
    settings: WebSocketSettings
  )(implicit backend: SttpBackend[F, WebSockets], logs: Logs[I, F]): I[WebSocketGateway[F]] =
    logs.forService[WebSocketGateway[F]].map { implicit logging =>
      (new WebSocketGatewayTracingMid[F](settings) attach (new Live[F](backend, settings): WebSocketGateway[F]))
    }

  final private class Live[F[_]: Sync: Raise[*[_], GatewayError]](
    backend: SttpBackend[F, WebSockets],
    settings: WebSocketSettings
  ) extends WebSocketGateway[F] {

    def send[Req, Res](req: Req)(implicit encoder: Encoder[Req], decoder: Decoder[Res]): F[Res] =
      basicRequest
        .response(asWebSocket[F, Res](ws => handler(req, ws)))
        .get(Uri.unsafeParse(settings.uri))
        .send(backend)
        .flatMap(_.body.leftMap(UnexpectedError).toRaise)

    def handler[Req, Res](req: Req, ws: WebSocket[F])(implicit encoder: Encoder[Req], decoder: Decoder[Res]): F[Res] =
      for {
        _            <- ws.sendBinary(encoder(req).toString().getBytes())
        responseText <- ws.receiveText()
        decodedRes <- decode[Res](responseText)
                        .leftMap(decFailure => GatewayResponseDecodingFailure(decFailure.toString))
                        .toRaise[F]
      } yield decodedRes
  }

  final private class WebSocketGatewayTracingMid[F[_]: Apply](settings: WebSocketSettings)(implicit logging: Logging[F])
    extends WebSocketGateway[Mid[F, *]] {

    def send[Req, Res](req: Req)(implicit encoder: Encoder[Req], decoder: Decoder[Res]): Mid[F, Res] =
      trace"Going to send req: ${req.toString} to ${settings.uri} by web socket gateway" *> _
  }
}
