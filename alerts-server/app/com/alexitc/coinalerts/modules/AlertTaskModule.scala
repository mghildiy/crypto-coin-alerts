package com.alexitc.coinalerts.modules

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import com.alexitc.coinalerts.config.AlertTaskConfig
import com.alexitc.coinalerts.tasks.FixedPriceAlertsTask
import org.slf4j.LoggerFactory
import play.api.inject.{SimpleModule, _}

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class AlertTaskModule
    extends SimpleModule(bind[AlertTaskRunner].toSelf.eagerly())

/**
 * Runs the alert task frequently.
 */
@Singleton
class AlertTaskRunner @Inject() (
    actorSystem: ActorSystem,
    config: AlertTaskConfig,
    alertsTask: FixedPriceAlertsTask)(
    implicit executionContext: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  start()

  // TODO: Add shutdown hook?
  def start() = {
    logger.info("Starting fixed price alert task runner...")

    val _ = actorSystem.scheduler.schedule(
      initialDelay = config.initialDelay,
      interval = config.interval) { runTask() }
  }

  def runTask(): Unit = {
    logger.info("Running fixed price alert task...")
    val _ = alertsTask.execute()
        .recover {
          case NonFatal(ex) =>
            logger.error("Unexpected error while running FixedPriceAlertsTask", ex)
        }
  }
}
