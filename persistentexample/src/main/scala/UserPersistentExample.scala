import java.io.{File, PrintWriter}

import akka.actor.{Props, ActorSystem}
import akka.persistence.PersistentActor

/**
 * Created by harsh on 29/7/14.
 */
case class User(var username: String = "", var password: String = "", var name: String = "", var email: String = "", var isActive: Boolean = false)


sealed trait Command

sealed trait Event

case class CreateUser(user: User) extends Command

case class UpdateUserName(username: String) extends Command

case class UpdatePassWord(password: String) extends Command

case class UpdateName(name: String) extends Command

case class UpdateEmail(mail: String) extends Command

case object SetActiveStatus extends Command

case class UserCreated(user: User) extends Event

case class UsernameUpdated(username: String) extends Event

case class PasswordUpdated(password: String) extends Event

case class NameUpdated(name: String) extends Event

case class EmailUpdated(mail: String) extends Event

case object UserActivated extends Event


case class UserState(usr: User) {
  def updateChangedUserState(u: User): UserState = {
    copy(usr = u)
  }
  override def toString:String = "Current user state :"+usr.username+" "+usr.password+" "+usr.name+" "+usr.email+" "+usr.isActive
}

class UserPersistentActor extends PersistentActor {

  override def persistenceId = "persistent-id-1"

  var writer =  new PrintWriter((new File("/home/harsh/IdeaProjects/persistentexample/example.txt" )))

  var userstate = UserState(new User)

  def changeUserState(event: Event): Unit = {
    event match {
      case UserCreated(u: User) => userstate.updateChangedUserState(u)
      case UsernameUpdated(username: String) => {
        userstate.usr.username = username
        userstate.updateChangedUserState(userstate.usr)
      }
      case PasswordUpdated(password: String) => {
        userstate.usr.password = password
        userstate.updateChangedUserState(userstate.usr)
      }
      case NameUpdated(name: String) => {
        userstate.usr.name = name
        userstate.updateChangedUserState(userstate.usr)
      }
      case EmailUpdated(mail: String) => {
        userstate.usr.email = mail
        userstate.updateChangedUserState(userstate.usr)
      }
      case UserActivated => {
        userstate.usr.isActive = true
        userstate.updateChangedUserState(userstate.usr)
      }

    }
  }

  val receiveRecover: Receive = {
    case event: Event => {println("Recovery started !");changeUserState(event)}
  }

  try{
    val receiveCommand: Receive = {
      case CreateUser(new_user) => persist(UserCreated(new_user)){event =>
        changeUserState(event)
      }
      case UpdateUserName(username) => persist(UsernameUpdated(username)){event =>
        changeUserState(event)
      }
      case UpdatePassWord(password) => persist(PasswordUpdated(password)){event =>
        changeUserState(event)
      }
      case UpdateName(name) => persist(NameUpdated(name)){event =>
        changeUserState(event)
      }
      case UpdateEmail(mail) => persist(EmailUpdated(mail)){event =>
        changeUserState(event)
      }
      case SetActiveStatus => persist(UserActivated){event =>
        changeUserState(event)
      }
      case "dump_state_to_file" => {writer.write(userstate.toString);;writer.flush();writer.close()}
    }
  }catch{
    case e:Exception => e.printStackTrace()
  }

}

object UserPersistentExample{
  var a_user = new User("User1","secret","Tom","tom@mot.com",true)
  def main (args: Array[String]) {
    val system = ActorSystem("userExample")
    val userPersistentActor = system.actorOf(Props[UserPersistentActor], "persistent-user-example")
    userPersistentActor ! CreateUser(a_user)
    userPersistentActor ! UpdateUserName("updatedUser1")
    userPersistentActor ! UpdatePassWord("updatedSecret")
    userPersistentActor ! UpdateName("updatedTom")
    userPersistentActor ! UpdateEmail("updatedtom@mot.com")
    userPersistentActor ! SetActiveStatus
    userPersistentActor ! "dump_state_to_file"
    Thread.sleep(1000)

    system.shutdown()
  }
}
