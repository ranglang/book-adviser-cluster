package fcluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent}
import akka.cluster.pubsub.DistributedPubSubMediator.{SubscribeAck, Subscribe, Unsubscribe, UnsubscribeAck}
import com.typesafe.config.ConfigFactory

object bookAdviserSubscriber {
  def main(port: String) = {
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port")
      .withFallback(ConfigFactory.parseString("akka.cluster.role = [subscriber]"))
      .withFallback(ConfigFactory.load())
    val actorSystem = ActorSystem("book-advisor-systems", config)
    actorSystem.actorOf(Props[bookAdviserSubscriber], name = "book-advisor-subscriber")
  }
}

class bookAdviserSubscriber extends Actor with ActorLogging {
  import akka.cluster.pubsub.DistributedPubSub

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  override def receive = ???

}
