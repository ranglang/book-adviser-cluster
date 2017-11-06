package fcluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSubMediator
import com.typesafe.config.ConfigFactory

object bookAdviserPublisher {

  case class Book(title: String, content: String)
  case class Advise(book: Book, grade: Int)
  case class PublishAdvise(advise: Advise)

  def props = Props(new bookAdviserPublisher)

  def main(port: String) = {
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [publisher]")).
      withFallback(ConfigFactory.load())
    val actorSystem = ActorSystem("cluster-system", config)
    actorSystem.actorOf(bookAdviserPublisher.props, name = "publisher")
  }
}

class bookAdviserPublisher extends Actor with ActorLogging {
  import bookAdviserPublisher._
  import scala.concurrent.duration._
  import akka.cluster.pubsub.DistributedPubSub

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  val r = scala.util.Random
  val book = Book(title = "La caverna", content = "...")

  val tickTask = context.system.scheduler.schedule(1.seconds, 3.seconds){
    self ! PublishAdvise(Advise(book, r.nextInt(10)))
  }(context.dispatcher)

  override def postStop: Unit = {
    tickTask.cancel()
  }

  override def receive: Receive = receivePublish

  def receivePublish: Receive= {
    case PublishAdvise(advise) =>
      mediator ! DistributedPubSubMediator.Publish("book-advise", advise)
  }

}
