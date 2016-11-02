import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Attributes, OverflowStrategy}

import scala.concurrent.Future
import scala.concurrent.duration._


object Text extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer(
    //    ActorMaterializerSettings(system)
    //    .withInputBuffer(
    //      initialSize = 64,
    //      maxSize = 128)
  )
  implicit val executionContext = system.dispatcher



  var c = 0
  Source.tick(0.seconds, 0.1.seconds, ())
    .map(p => {
      println(c + 1, "start");
      c += 1;
      c
    })
    .map(p => {
      println("          " + c, "zhongqian");
      c += 1;
      c
    })
    .async.buffer(32,OverflowStrategy.backpressure)
    .mapAsync(2)(p => Future {
      println("                        " + p, "zhong");
      Thread.sleep(100000)
      p
    })
    .map(p => {
      println(p);
      p
    })
    .runWith(Sink.ignore)
}