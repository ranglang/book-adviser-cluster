import scluster.ExternalBookAdviserPublisher

object ExternalBookAdviserSubscriberNode {

  def main(args: Array[String]): Unit = {
    ExternalBookAdviserPublisher.main(args(0))
  }
}
