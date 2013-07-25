import com.badlogic.gdx.Input.Keys
import org.andreschnabel.micro.Micro._
import scala.collection.immutable.HashMap

object Example {
  val IMG_NAME = "alien"
  val SND_NAME = "coin"
  val SONG_NAME = "loop"
  val FONT_NAME = "font"

  var (imgPos,imgDim) = ((0.0f, 0.0f),(0,0))
  val MOV_SPEED = 5.0f

  val keyActions = HashMap(
    Keys.ESCAPE -> (() => quit()),
    Keys.S -> (() => playSound(SND_NAME)),
    Keys.M -> (() => playSong(SONG_NAME)),
    Keys.N -> (() => stopSong(SONG_NAME)),
    Keys.LEFT -> (() => imgPos=(imgPos.x-MOV_SPEED, imgPos.y)),
    Keys.RIGHT -> (() => imgPos=(imgPos.x+MOV_SPEED, imgPos.y)),
    Keys.UP -> (() => imgPos=(imgPos.x, imgPos.y+MOV_SPEED)),
    Keys.DOWN -> (() => imgPos=(imgPos.x, imgPos.y-MOV_SPEED)))

  def main(args : Array[String]) {
    init("Micro Example", (800, 480), initCallback, drawCallback)
  }

  def initCallback() {
    imgDim = getImageDim(IMG_NAME)
    setFont(FONT_NAME, 20)
  }

  def drawCallback(delta : Float) {
    processInput()
    drawImage(IMG_NAME, imgPos.x, imgPos.y)
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
