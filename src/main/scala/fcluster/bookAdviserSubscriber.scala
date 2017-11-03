package fcluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent}
import akka.cluster.pubsub.DistributedPubSubMediator.{SubscribeAck, Subscribe, Unsubscribe, UnsubscribeAck}
import com.typesafe.config.ConfigFactory

object bookAdviserSubscriber {
  def main(args: Array[String]) = {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port")
      .withFallback(ConfigFactory.load())
    val actorSystem = ActorSystem("book-advisor-system-1", config)
    actorSystem.actorOf(Props[bookAdviserSubscriber], name = "book-advisor-subscriber")
  }
}

class bookAdviserSubscriber extends Actor with ActorLogging {
  import akka.cluster.pubsub.DistributedPubSub

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    mediator ! Subscribe("readers", self)
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent])
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe("readers", self)
    cluster.unsubscribe(self)
  }

  override def receive = receiveClusterEvents
    .orElse[Any, Unit](receiveSubscription)

  def receiveClusterEvents: Receive = {
     case event: MemberEvent => log.info(event.getClass.getSimpleName + "--->" +event.member)
  }

  def receiveSubscription: Receive = {
    case SubscribeAck(subscribe) =>
      log.info("Actor: " + subscribe.ref + " subscribed to: " +  subscribe.topic)
    case UnsubscribeAck(unsubscribe) =>
      log.info("Actor: " +  unsubscribe.ref " unsubscribed to: " +  unsubscribe.topic)
  }

}
