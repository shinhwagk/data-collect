//import java.util.{Calendar, Date}
//
//import akka.stream.scaladsl.Source
//import org.quartz.CronExpression
//import scala.concurrent.duration._
//import scala.util.Random
//
///**
//  * Created by zhangxu on 2016/8/19.
//  */
//object Test2 extends App {
//
//  import org.wex.services.ActorSystemServices._
//
//  def abc: Seq[Int] = {
//    val max = new Random().nextInt(10)
//    println(11111,max)
//    (1 to max)
//  }
//
////  Source.cycle(() => abc.toIterator).map(p => {
////    Thread.sleep(1000);
////    p
////  }).runForeach(println(_))
//
//  Source.tick(0.seconds, 1.seconds, Test2).map(_)
//  while (true) {
//    Thread.sleep(1111)
//  }
//}
//
