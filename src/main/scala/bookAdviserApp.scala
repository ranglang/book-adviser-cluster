import fcluster._

object bookAdviserApp {

  def main(args: Array[String]): Unit = {
    bookAdviserPublisher.main("2551")
    bookAdviserSubscriber.main("2552")
    bookAdviserSubscriber.main("2553")
  }
}
