import java.io.{File, PrintWriter}

/**
 * Created by harsh on 29/7/14.
 */
case class x(s:String,t:Int,u:Double)
object test {
def main(arg:Array[String]){
  val pw = new PrintWriter((new File("/home/harsh/IdeaProjects/persistentexample/example.txt" )))
  pw.write("Hello, world")
  pw.close
  val a  = new x("q",1,2.0)
  println(a.toString)
}
}
