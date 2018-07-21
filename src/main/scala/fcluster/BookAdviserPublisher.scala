package fcluster

import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, ActorSystem, Cancellable, Props}
import akka.cluster.Cluster
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.cluster.pubsub.DistributedPubSubMediator
import com.typesafe.config.ConfigFactory

object BookAdviserPublisher {

  case class Book(title: String, content: String)

  case class Advise(book: Book, grade: Int)

  case class PublishAdvise(advise: Advise)

  def main(port: String) = {

    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [publisher]")).
      withFallback(ConfigFactory.load("pcluster"))

    val actorSystem = ActorSystem("publish-system-1", config)
    actorSystem.actorOf(props = Props[BookAdviserPublisher], name = "publisher")
  }
}

class BookAdviserPublisher extends Actor with ActorLogging {

  import BookAdviserPublisher._
  import scala.concurrent.duration._
  import akka.cluster.pubsub.DistributedPubSub

  val actorSystem: ActorSystem = context.system
  val cluster = Cluster(actorSystem)
  val mediator: ActorRef = DistributedPubSub(actorSystem).mediator

  val initialContacts = Set(
    ActorPath.fromString("akka.tcp://subscribe-cluster-system@127.0.0.1:2561/system/receptionist")
  )

  val clusterClient: ActorRef = actorSystem.actorOf(ClusterClient.props(
    ClusterClientSettings(actorSystem).withInitialContacts(initialContacts)))

  val r = scala.util.Random
  val book = Book(title = "La caverna", content = "...")

  val tickTask: Cancellable = context.system.scheduler.schedule(1.seconds, 6.seconds) {
    self ! PublishAdvise(Advise(book, r.nextInt(10)))
  }(context.dispatcher)

  override def postStop: Unit = {
    tickTask.cancel()
  }

  override def receive: Receive = receivePublish

  def receivePublish: Receive = {
    case PublishAdvise(advise) =>
      mediator ! DistributedPubSubMediator.Publish("book-advise", advise)
      clusterClient ! ClusterClient.Publish("book-advise", advise)
  }

}
