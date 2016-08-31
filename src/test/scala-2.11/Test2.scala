/**
  * Created by zhangxu on 2016/8/19.
  */
object Test2 extends App {
  val b = abc(1)
  println(b.copy(x = 11).hashCode())
  println(b.hashCode())
}

case class abc(x: Int) {
  def afd: abc = {
    copy(x)
  }
}


