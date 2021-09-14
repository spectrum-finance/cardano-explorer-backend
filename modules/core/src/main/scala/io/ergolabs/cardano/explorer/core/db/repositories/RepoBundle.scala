package io.ergolabs.cardano.explorer.core.db.repositories

import cats.FlatMap
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.logging.Logs
import tofu.syntax.monadic._

final case class RepoBundle[F[_]](
  assets: AssetsRepo[F],
  inputs: InputsRepo[F],
  outputs: OutputsRepo[F],
  transactions: TransactionsRepo[F],
  metadata: TxMetadataRepo[F],
  redeemer: RedeemerRepo[F],
  blocks: BlocksRepo[F],
  slots: SlotsRepo[F]
)

object RepoBundle {

  def make[I[_]: FlatMap, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[RepoBundle[D]] =
    for {
      assetsR       <- AssetsRepo.make[I, D]
      inputsR       <- InputsRepo.make[I, D]
      outputsR      <- OutputsRepo.make[I, D]
      transactionsR <- TransactionsRepo.make[I, D]
      metadata      <- TxMetadataRepo.make[I, D]
      redeemer      <- RedeemerRepo.make[I, D]
      blocks        <- BlocksRepo.make[I, D]
      slots         <- SlotsRepo.make[I, D]
    } yield RepoBundle(assetsR, inputsR, outputsR, transactionsR, metadata, redeemer, blocks, slots)
}
