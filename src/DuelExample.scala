import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.{Rectangle, Intersector, Vector2}
import org.andreschnabel.micro.Micro._
import scala.collection.immutable.HashMap

object DuelExample {
  lazy val players = Array((1, (50.0f, 400.0f)), (2, (520.0f, 400.0f))).map(p => new Player(p._1, p._2))

  def MoveSpeed = Player.MoveSpeed
  val keyActions = HashMap(
    Keys.ESCAPE -> (() => quit()),
    Keys.LEFT -> (() => players(1).move(-MoveSpeed, 0)),
    Keys.RIGHT -> (() => players(1).move(MoveSpeed, 0)),
    Keys.UP -> (() => players(1).move(0, MoveSpeed)),
    Keys.DOWN -> (() => players(1).move(0, -MoveSpeed)),
    Keys.CONTROL_LEFT -> (() => players(1).shoot()),
    Keys.A -> (() => players(0).move(-MoveSpeed, 0)),
    Keys.D -> (() => players(0).move(MoveSpeed, 0)),
    Keys.W -> (() => players(0).move(0, MoveSpeed)),
    Keys.S -> (() => players(0).move(0, -MoveSpeed)),
    Keys.CONTROL_RIGHT -> (() => players(0).shoot()),
    Keys.ALT_RIGHT -> (() => players(0).shoot()))

  def main(args : Array[String]) {
    init("DuelExample", (640, 480), initCallback, drawCallback)
  }

  def initCallback() {
    setFont("font", 30)
    playSong("loop", true)
  }

  def drawCallback(delta : Float) {
    processInput()
    renderScene()

    def processInput() {
      keyActions.keySet.filter(keyPressed).foreach(keyActions(_)())
      val mstate = mouseState()
      if(mstate.lmb) {
        players(0).pos = mstate.pos.sub(players(0).dim.mul(0.5f))
      }
    }

    def renderScene() {
      drawImage("background", 0, 0)
      players.foreach(_.draw())
      Player.checkCollisions(players)

      drawText("P1 score " + players(0).kills, 100, 100)
      drawText("P2 score " + players(1).kills, 500, 100)
    }
  }

  object Player {
    val MoveSpeed = 5.0f
    val BulletMoveSpeed = 10.0f
    val ReloadTime = 120l
    val DeathTime = 2000l

    lazy val bulletDim = getImageDim("bullet")

    def rectangleForBullet(bpos: Vector2): Rectangle = {
      new Rectangle(bpos.x, bpos.y, bulletDim._1, bulletDim._2)
    }

    def checkCollisions(players : Array[Player]) {
      val oldBullets = players.map(_.bullets)
      Array((0,1),(1,0)).foreach(pair => checkCollisionForPair(pair._1, pair._2))
      def checkCollisionForPair(p:Int, o:Int) {
        players(p).bullets = oldBullets(p).filterNot(p1b => oldBullets(o).exists(p2b => Intersector.overlapRectangles(rectangleForBullet(p1b), rectangleForBullet(p2b))))

        if(players(o).dead) {
          return
        }

        val oldLen = players(p).bullets.length
        players(p).bullets = players(p).bullets.filterNot(p1b => Intersector.overlapRectangles(players(o).rect, rectangleForBullet(p1b)))
        if(players(p).bullets.length < oldLen) {
          if(players(o).hit()) {
            players(p).score()
          }
        }
      }
    }
  }

  class Player(val num : Int, var pos : (Float,Float)) {
    import Player._

    val bulletMoveVec = new Vector2((if(num == 1) 1.0f else -1.0f) * BulletMoveSpeed, 0.0f)
    lazy val dim = getImageDim("player" + num + "hurt0")

    var (healthState, kills) = (0,0)
    var bullets = List[Vector2]()
    var (lastShotTicks,deathTicks) = (0l,0l)

    def rect = {
      new Rectangle(pos.x, pos.y, dim._1, dim._2)
    }

    def shoot() {
      if(ticks() - lastShotTicks > ReloadTime) {
        bullets = pos.add(dim.mul(0.25f)) :: bullets
        playSound("shot")
        lastShotTicks = ticks()
      }
    }

    def hit() = {
      healthState+=1
      if(dead) {
        deathTicks = ticks()
        true
      } else false
    }

    def score() {
      kills += 1
    }

    def dead = healthState >= 3

    def draw() {
      drawImage("player" + num + "hurt" + healthState, pos.x, pos.y)

      bullets = bullets.map(_.add(bulletMoveVec)).filter(pos => pos.x + Player.bulletDim._1 > 0 && pos.x < scrW)
      bullets.foreach(b => drawImage("bullet", b.x, b.y))

      if(dead && ticks() - deathTicks > DeathTime) {
        healthState = 0
      }
    }

    def move(dx : Float, dy : Float) {
      if(dead) {
        return
      }

      val npos = (pos.x+dx, pos.y+dy)
      if(((num == 1 && npos.x >= 0 && npos.x + dim._1 <= scrW / 2)
            || (num == 2 && npos.x >= scrW/2 && npos.x + dim._1 <= scrW))
          && npos.y > 0 && npos.y + dim._2 < scrH) {
        pos = npos
      }
    }
  }
}
