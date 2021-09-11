package io.ergolabs.cardano.explorer.core.db.repositories

import cats.FlatMap
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.logging.Logs
import tofu.syntax.monadic._

final case class TxRepoBundle[F[_]](
  assets: AssetsR[F],
  inputs: InputsR[F],
  outputs: OutputsR[F],
  transactions: TransactionsR[F]
)

object TxRepoBundle {

  def make[I[_]: FlatMap, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[TxRepoBundle[D]] =
    for {
      assetsR       <- AssetsR.make[I, D]
      inputsR       <- InputsR.make[I, D]
      outputsR      <- OutputsR.make[I, D]
      transactionsR <- TransactionsR.make[I, D]
    } yield TxRepoBundle(assetsR, inputsR, outputsR, transactionsR)
}
