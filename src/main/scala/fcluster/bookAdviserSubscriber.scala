package fcluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSubMediator.{Unsubscribe, Subscribe, SubscribeAck}
import com.typesafe.config.ConfigFactory

object bookAdviserSubscriber {

  def main(port: String) = {
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port")
      .withFallback(ConfigFactory.parseString("akka.cluster.roles = [subscriber]"))
      .withFallback(ConfigFactory.load())
    val actorSystem = ActorSystem("cluster-system", config)
    actorSystem.actorOf(Props[bookAdviserSubscriber], name = "subscriber")
  }
}

class bookAdviserSubscriber extends Actor with ActorLogging {
  import akka.cluster.pubsub.DistributedPubSub
  import bookAdviserPublisher.Advise

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe("book-advise", self)

  override def postStop(): Unit = {
    mediator ! Unsubscribe("book-advise", self)
  }

  override def receive = receiveSubscription

  def receiveSubscription: Receive = {
   case SubscribeAck(Subscribe("book-advise", None, `self`))  =>
     log.info("------->Subscr¡bing...")
   case advise: Advise =>
      log.info(s"Advise ----> Book: ${advise.book} grade: ${advise.grade}")
  }


}
