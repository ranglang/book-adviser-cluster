package fcluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

object bookAdviserPublisher {


  def main(port: String) = {
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.role = [publisher]")).
      withFallback(ConfigFactory.load())
    val actorSystem = ActorSystem("cluster-system", config)
    actorSystem.actorOf(Props[bookAdviserSubscriber], name = "book-advisor-publisher")
  }
}

class bookAdviserPublisher extends Actor with ActorLogging {
  import bookAdviserPublisher.Book
  import bookAdviserPublisher.PublishBook

  import akka.cluster.pubsub.DistributedPubSub

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  override def receive: Receive = ???

}
