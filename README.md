# BookAdviser
This is a basic project aimed at learning about Akka Cluster

This small program starts two clusters which a Publisher publishs advises about
books and those interested subscribes to them.

The main publisher uses DistributedPubSub extension to publish advises in the
same cluster and it uses ClusterClient to publish advises to the external cluster (scluster).

The external Publisher updates a CRDT, and the external Subscribers subscribes to the
changes of the CRDT.

To run this program, type ```sbt "runMain BookAdviserApp"```
The nodes will be started on ports: ```2551```,```2552```,```2553```,```2561```,```2562```


###### runMain BookAdviserPublisherNode
###### runMain BookAdviserSubscriberNode 2552
###### runMain BookAdviserSubscriberNode 2553
###### runMain ExternalBookAdviserPublisherNode
###### runMain ExternalBookAdviserSubscriberNode 2562
###### runMain ExternalBookAdviserSubscriberNode 2563
