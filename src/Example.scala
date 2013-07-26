import com.badlogic.gdx.Input.Keys
import org.andreschnabel.micro.Micro._
import scala.collection.immutable.HashMap

object Example {
  val ImgName = "alien"
  val SndName = "coin"
  val SongName = "loop"
  val FontName = "font"
  val MoveSpeed = 5.0f

  lazy val imgDim = getImageDim(ImgName)
  var imgPos = (0.0f, 0.0f)

  val keyActions = HashMap(
    Keys.ESCAPE -> (() => quit()),
    Keys.S -> (() => playSound(SndName)),
    Keys.M -> (() => playSong(SongName)),
    Keys.N -> (() => stopSong(SongName)),
    Keys.LEFT -> (() => imgPos=(imgPos.x-MoveSpeed, imgPos.y)),
    Keys.RIGHT -> (() => imgPos=(imgPos.x+MoveSpeed, imgPos.y)),
    Keys.UP -> (() => imgPos=(imgPos.x, imgPos.y+MoveSpeed)),
    Keys.DOWN -> (() => imgPos=(imgPos.x, imgPos.y-MoveSpeed)))

  def main(args : Array[String]) {
    init("Micro Example", (800, 480), initCallback, drawCallback)
  }

  def initCallback() {
    setFont(FontName, 20)
  }

  def drawCallback(delta : Float) {
    processInput()
    drawImage(ImgName, imgPos.x, imgPos.y)
    drawText("This is a minimal framework", 100, 100)

    def processInput() {
      keyActions.keySet.filter(keyPressed).foreach(keyActions(_)())
      val mstate = mouseState()
      if(mstate.lmb) {
        imgPos = mstate.pos.sub(imgDim.mul(0.5f))
      }
    }
  }
}
