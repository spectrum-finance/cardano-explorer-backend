package io.ergolabs.cardano.explorer.core.db.sql

import doobie._
import doobie.syntax.all._
import io.ergolabs.cardano.explorer.core.db.models.MetaData
import io.ergolabs.cardano.explorer.core.db.models.EpochParams
import io.ergolabs.cardano.explorer.core.db.models.EpochStakes

final class NetworkParamsSql(implicit lh: LogHandler) {

  def getMeta: Query0[MetaData] =
    sql"select start_time, network_name from meta".query

  def getLastEpochParams: Query0[EpochParams] =
    sql"""
      select 
        epoch_no,
        protocol_major,
        protocol_minor,
        decentralisation,
        entropy,
        max_bh_size,
        max_block_size,
        max_tx_size,
        min_fee_b,
        min_fee_a,
        min_utxo_value,
        key_deposit,
        pool_deposit,
        min_pool_cost,
        max_epoch,
        optimal_pool_count,
        influence,
        monetary_expand_rate,
        treasury_growth_rate,
        coins_per_utxo_word,
        cost_model_id,
        price_step,
        max_tx_ex_steps,
        max_block_ex_steps,
        max_val_size,
        collateral_percent,
        max_collateral_inputs
      from epoch_param order by epoch_no desc limit 1""".stripMargin.query

  def getEpochStakes(epochNo: Int): Query0[String] =
    sql"""
         |select
         |  p.view
         |from epoch_stake e
         |left join pool_hash p on e.pool_id = p.id
         |where e.epoch_no = $epochNo
         |""".stripMargin.query
  
//  def getCostModel(costModelId: Int): Query[String] =
//    sql"select costs from cost_model where id = $costModelId".query
//
}
