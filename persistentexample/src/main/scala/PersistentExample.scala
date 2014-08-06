/**
 * Created by harsh on 28/7/14.
 */

import java.io.{FileWriter, PrintWriter, File}

import akka.actor._
import akka.persistence._
import scala.io._




case class Cmd(op: String)
case class Evt(op: String)

case class UserStateOps(usrOps:List[String] = Nil){
  def usrOp(e:Evt):UserStateOps = copy(e.op::usrOps)
  override def toString: String = usrOps.reverse.toString

}
class PersistentExample extends PersistentActor{

  override def persistenceId = "example-id-1"
  var writer =  new PrintWriter((new File("/home/harsh/IdeaProjects/persistentexample/example.txt" )))

  var uo = UserStateOps()

  def executeUsrOp(evt : Evt):Unit = {
    println(s"executing op $evt")
    uo = uo.usrOp(evt)
  }
  val receiveRecover: Receive = {
    case _ => println("Recovery started.Nothing to recover")
  }
  val receiveCommand: Receive = {
    case Cmd(op) => persist(Evt(s"Event generated for user operation : $op")) { e =>
      executeUsrOp(e)
    }
    case "write_to_file" => {val str = uo.toString;println(s"writing to file ${str}");writer.write(str);writer.flush();writer.close()}
  }
}

object PersistentExample {


  def main(args:Array[String]):Unit = {
    val system = ActorSystem("example")
    val persistentActor = system.actorOf(Props[PersistentExample], "persistent-user-example")
    persistentActor ! Cmd("Create User")
    persistentActor ! Cmd("Update User")
    persistentActor ! Cmd("Create User")
    persistentActor ! "write_to_file"

  }

}
