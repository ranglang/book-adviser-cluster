package fcluster

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSubMediator
import com.typesafe.config.ConfigFactory

object BookAdviserPublisher {

  case class Book(title: String, content: String)

  case class Advise(book: Book, grade: Int)

  case class PublishAdvise(advise: Advise)

  def main(port: String) = {
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [publisher]")).
      withFallback(ConfigFactory.load("fcluster"))
    val actorSystem = ActorSystem("cluster-system-1", config)
    actorSystem.actorOf(Props[BookAdviserPublisher], name = "publisher")
  }
}

class BookAdviserPublisher extends Actor with ActorLogging {

  import BookAdviserPublisher._
  import scala.concurrent.duration._
  import akka.cluster.pubsub.DistributedPubSub

  val cluster = Cluster(context.system)
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  val r = scala.util.Random
  val book = Book(title = "La caverna", content = "...")

  val tickTask: Cancellable = context.system.scheduler.schedule(1.seconds, 3.seconds) {
    self ! PublishAdvise(Advise(book, r.nextInt(10)))
  }(context.dispatcher)

  override def postStop: Unit = {
    tickTask.cancel()
  }

  override def receive: Receive = receivePublish

  def receivePublish: Receive = {
    case PublishAdvise(advise) =>
      mediator ! DistributedPubSubMediator.Publish("book-advise", advise)
  }

}
