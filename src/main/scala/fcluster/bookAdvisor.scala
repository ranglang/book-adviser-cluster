package fcluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberJoined}
import com.typesafe.config.ConfigFactory

object bookAdvisor {
  def main(args: Array[String]) = {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port")
      .withFallback(ConfigFactory.load())
    val actorSystem = ActorSystem("book-advisor-system-1", config)
    actorSystem.actorOf(Props[bookAdvisor], name = "book-advisor")
  }
}

class bookAdvisor extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  override def receive = receiveClusterEvents

  def receiveClusterEvents: Receive = {
     case event: MemberEvent => log.info(event.getClass.getSimpleName + "--->" +event.member)
  }

}
