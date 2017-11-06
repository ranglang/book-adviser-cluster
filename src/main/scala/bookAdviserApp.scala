import fcluster._
import scluster.{ExternalBookAdviserPublisher, ExternalBookAdviserSubscriber}

object BookAdviserApp {

  def main(args: Array[String]): Unit = {
//    BookAdviserPublisher.main("2551")
//    BookAdviserSubscriber.main("2552")
//    BookAdviserSubscriber.main("2553")
      ExternalBookAdviserPublisher.main("2561")
      ExternalBookAdviserSubscriber.main("2562")

  }
}
