package scluster

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator.{Changed, Subscribe}
import akka.cluster.ddata.{DistributedData, GSetKey}
import com.typesafe.config.ConfigFactory
import fcluster.BookAdviserPublisher.Advise

object ExternalBookAdviserSubscriber {

  def main(port: String) = {
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [external-subscriber]")).
      withFallback(ConfigFactory.load("scluster"))
    val actorSystem = ActorSystem("cluster-system-2", config)
    actorSystem.actorOf(Props[ExternalBookAdviserSubscriber])
  }
}


class ExternalBookAdviserSubscriber extends Actor with ActorLogging {
  val actorSystem: ActorSystem = context.system
  val cluster = Cluster(actorSystem)

  val replicator: ActorRef = DistributedData(actorSystem).replicator

  val advisesKey: GSetKey[Advise] = GSetKey[Advise]("book-advises-key")

  replicator ! Subscribe(advisesKey, self)

  override def receive: Receive = receiveSubscription

  def receiveSubscription: Receive = {
    case dataChanged@Changed(`advisesKey`) =>
      log.info(s"Data changed --> ${dataChanged.get(advisesKey).elements}")
  }

}
