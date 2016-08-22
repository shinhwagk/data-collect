/**
  * Created by zhangxu on 2016/8/19.
  */
object Test2 {

  import collection.mutable.Stack
  import org.scalatest._

  class ExampleSpec extends FlatSpec with Matchers {

    "A Stack" should "pop values in last-in-first-out order" in {
      val stack = new Stack[Int]
      stack.push(1)
      stack.push(2)
      stack.pop() should be(3)
      stack.pop() should be(1)
    }

    it should "throw NoSuchElementException if an empty stack is popped" in {
      val emptyStack = new Stack[Int]
      a[NoSuchElementException] should be thrownBy {
        emptyStack.pop()
      }
    }
  }

}


