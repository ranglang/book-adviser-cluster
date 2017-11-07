package scluster

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, Props}
import akka.cluster.Cluster
import akka.cluster.client.ClusterClientReceptionist
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{DistributedData, GSet, GSetKey}
import com.typesafe.config.ConfigFactory
import fcluster.BookAdviserPublisher.{Advise, Book, PublishAdvise}

import scala.concurrent.duration._

object ExternalBookAdviserPublisher {

  val writeConsistency = WriteAll(5.seconds)

  def main(port: String) = {
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [external-publisher]")).
      withFallback(ConfigFactory.load("scluster"))
    val actorSystem = ActorSystem("cluster-system-2", config)
    actorSystem.actorOf(props = Props[ExternalBookAdviserPublisher])
  }

}


class ExternalBookAdviserPublisher extends Actor with ActorLogging {

  import ExternalBookAdviserPublisher._

  val actorSystem: ActorSystem = context.system
  val cluster = Cluster(actorSystem)
  val advisesKey: GSetKey[Advise] = GSetKey[Advise]("book-advises-key")

  ClusterClientReceptionist(actorSystem).registerSubscriber("book-advise", self)

  val replicator: ActorRef = DistributedData(actorSystem).replicator

  override def postStop(): Unit = {
    ClusterClientReceptionist(context.system).unregisterSubscriber("book-advise", self)
  }

  override def receive: Receive = receivePublishAdvise

  private def receivePublishAdvise: Receive = {
    case advise: Advise =>
      replicator ! Update(advisesKey, GSet.empty[Advise], writeConsistency)(
        data => data + advise)
      replicator ! FlushChanges
    case _: UpdateResponse[Advise] => Unit
  }
}

