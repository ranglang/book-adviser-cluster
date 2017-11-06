package scluster

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, Props}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator.{Update, UpdateResponse, UpdateSuccess, WriteAll}
import akka.cluster.ddata.{DistributedData, GSet, GSetKey}
import com.typesafe.config.ConfigFactory
import fcluster.BookAdviserPublisher.{Advise, Book, PublishAdvise}

import scala.concurrent.duration._

object ExternalBookAdviserPublisher {

  val writeConsistency = WriteAll(3.seconds)

  def main(port: String) = {
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [external-publisher]")).
      withFallback(ConfigFactory.load("scluster"))
    val actorSystem = ActorSystem("cluster-system-2", config)
    actorSystem.actorOf(Props[ExternalBookAdviserPublisher])
  }

}


class ExternalBookAdviserPublisher extends Actor with ActorLogging {

  import ExternalBookAdviserPublisher._

  val cluster = Cluster(context.system)

  val advisesKey: GSetKey[Advise] = GSetKey[Advise]("book-advises-key")

  val replicator: ActorRef = DistributedData(context.system).replicator

  val r = scala.util.Random
  val book = Book(title = "La caverna", content = "...")

  val tickTask: Cancellable = context.system.scheduler.schedule(1.seconds, 3.seconds) {
    self ! PublishAdvise(Advise(book, r.nextInt(10)))
  }(context.dispatcher)

  override def receive: Receive = receivePublishAdvise

  def receivePublishAdvise: Receive = {
    case PublishAdvise(advise: Advise) =>
      replicator ! Update(advisesKey, GSet.empty[Advise], writeConsistency) {
        data => data + advise
      }
    case _: UpdateResponse[Advise] => Unit
  }
}

