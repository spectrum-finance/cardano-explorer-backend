package io.ergolabs.cardano.explorer.api

import cats.Applicative
import cats.syntax.either._
import fs2.{Chunk, Stream}
import io.circe.Encoder
import io.circe.syntax._
import tofu.streams.Compile
import tofu.syntax.monadic._

object streaming {

  type CompileStream[F[_]] = Compile[Stream[F, *], F]

  def bytesStream[F[_]: Applicative, A: Encoder, E](fa: Stream[F, A]): F[Either[E, Stream[F, Byte]]] =
    fa.flatMap(entity => Stream.chunk(Chunk.array(entity.asJson.noSpaces.getBytes)))
      .pure
      .map(_.asRight[E])
}
