package fcluster

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck, Unsubscribe}
import com.typesafe.config.ConfigFactory

object BookAdviserSubscriber {

  def main(port: String) = {
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port")
      .withFallback(ConfigFactory.parseString("akka.cluster.roles = [subscriber]"))
      .withFallback(ConfigFactory.load("fcluster"))
    val actorSystem = ActorSystem("cluster-system-1", config)
    actorSystem.actorOf(Props[BookAdviserSubscriber], name = "subscriber")
  }
}

class BookAdviserSubscriber extends Actor with ActorLogging {

  import akka.cluster.pubsub.DistributedPubSub
  import BookAdviserPublisher.Advise

  val actorSystem: ActorSystem = context.system
  val cluster = Cluster(actorSystem)
  val mediator: ActorRef = DistributedPubSub(actorSystem).mediator

  mediator ! Subscribe("book-advise", self)

  override def postStop(): Unit = {
    mediator ! Unsubscribe("book-advise", self)
  }

  override def receive: Receive = receiveSubscription

  private def receiveSubscription: Receive = {
    case SubscribeAck(Subscribe("book-advise", None, `self`)) =>
      log.info("------->Subscr¡bing...")
    case advise: Advise =>
      log.info(s"Advise ----> Book: ${advise.book} grade: ${advise.grade}")
  }


}
