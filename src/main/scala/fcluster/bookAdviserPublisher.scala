package fcluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import akka.cluster.pubsub.DistributedPubSubMediator.Publish

object bookAdviserPublisher {

  case class Book(title: String, actorName :String,  content: String)

  case class PublishBook(Book)

  def main(args: String) = {
    val port = if (args.isEmpty) "0" else port
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port")
      .withFallback(ConfigFactory.load())
    val actorSystem = ActorSystem("book-advisor-system-1", config)
    actorSystem.actorOf(Props[bookAdviserSubscriber], name = "book-advisor-publisher")
  }
}

class bookAdviserPublisher extends Actor with ActorLogging {
  import bookAdviserPublisher.Book
  import bookAdviserPublisher.PublishBook

  import akka.cluster.pubsub.DistributedPubSub

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  override def receive: Receive = {
    case PublishBook(book: Book) => mediator ! Publish(book.actorName, book)
  }

}
