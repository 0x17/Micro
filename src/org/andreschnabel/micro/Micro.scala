package org.andreschnabel.micro

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.{Gdx, ApplicationListener}
import com.badlogic.gdx.audio.{Music, Sound}
import com.badlogic.gdx.graphics.g2d.{BitmapFont, TextureAtlas, SpriteBatch}
import com.badlogic.gdx.graphics.GL10
import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.tools.imagepacker.TexturePacker2
import scala.collection.mutable
import java.io.File
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Disposable

/**
 * Minimalistic framework for 2D games built on top of libgdx.
 */
object Micro {

  /**
   * Horizontal dimension of screen in pixels.
   * @return screen width.
   */
  def scrW = scrDims._1

  /**
   * Vertical dimension of screen in pixels.
   * @return screen height.
   */
  def scrH = scrDims._2

  /**
   * Initialize micro framework.
   * @param drawCallback called each frame.
   */
  def init(caption : String, scrSize : (Int,Int), initCallback : () => Unit, drawCallback : (Float) => Unit) {
    scrDims = scrSize
    Utils.updateAtlas()
    new LwjglApplication(new AppListener(initCallback, drawCallback), caption, scrW, scrH, false)
  }

  /**
   * Draw image w/ filename in data name on screen. Origin is bottom left.
   * @param name name of image file (w/out extension) in data name.
   * @param x horizontal offset rightwards from origin on the left.
   * @param y vertical offset upwards from origin on the bottom.
   * @param rz rotation around z-axis in degrees. Counter clockwise.
   * @param sx scaling factor on x-axis. 1.0f means no horizontal stretching.
   * @param sy scaling factor on y-axis. 1.0f means no vertical stretching.
   */
  def drawImage(name : String, x : Float = 0, y : Float = 0, rz : Float = 0.0f, sx : Float = 1.0f, sy : Float = 1.0f) {
    val region = atlas.findRegion(name)
    if(region != null) {
      val w = region.getRegionWidth.toFloat
      val h = region.getRegionHeight.toFloat
      sb.draw(region, x, y, x+w/2.0f, y+h/2.0f, w, h, sx, sy, rz)
    }
  }

  /**
   * Get size of image with given name.
   * @param name name of image file (w/out extension) in data name.
   * @return (width,height)-tuple.
   */
  def getImageDim(name : String) : (Int, Int) = {
    val region = atlas.findRegion(name)
    if(region != null) {
      (region.getRegionWidth, region.getRegionHeight)
    } else {
      (0,0)
    }
  }

  var font : BitmapFont = _

  /**
   * Set a font for subsequent drawText calls.
   * @param name name of font file in data name (w/out extension)
   * @param size size for glyphs
   */
  def setFont(name : String, size : Int) {
    if(font != null) {
      font.dispose()
    }
    val generator = new FreeTypeFontGenerator(Utils.loadRes(name, "ttf"))
    font = generator.generateFont(size)
    generator.dispose()
  }

  /**
   * Draw given string at given position. Origin as usual bottom left.
   * @param text text to be drawn on screen with currently selected font.
   * @param x horizontal offset rightwards from origin on the left.
   * @param y vertical offset upwards from origin on the bottom.
   */
  def drawText(text : String, x : Float, y : Float) {
    font.draw(sb, text, x, y)
  }

  /**
   * Play sound w/ filename in data name.
   * @param name name of sound file (w/out extension) in data name.
   */
  def playSound(name : String) {
    Utils.putOrKeep(sounds, name, () => Gdx.audio.newSound(Utils.loadRes(name, "wav"))).play()
  }

  /**
   * Play song w/ filename in data name.
   * @param name name of music file (w/out extension) in data name.
   * @param loop true iff. playback of song should be loop infinitely.
   */
  def playSong(name : String, loop : Boolean = false) {
    val song = Utils.putOrKeep(songs, name, () => Gdx.audio.newMusic(Utils.loadRes(name, "mp3")))
    song.setLooping(loop)
    if(songPlaying != null) {
      songPlaying.stop()
    }
    song.play()
    songPlaying = song
  }

  /**
   * Stop playback of song w/ filename in data name.
   * @param name name of music file (w/out extension) in data name.
   */
  def stopSong(name : String) {
    songs(name).stop()
  }

  /**
   * Quit execution.
   */
  def quit() {
    Gdx.app.exit()
  }

  /**
   * Query if a given key is pressed.
   * @param key key code
   * @return true, iff. key w/ key code is pressed right now.
   */
  def keyPressed(key : Int) : Boolean = Gdx.input.isKeyPressed(key)

  /**
   * Represents state of the mouse.
   * @param pos position of mouse pointer (x,y) with bottom left origin.
   * @param lmb true, iff. left mouse button is pressed down.
   * @param mmb true, iff. middle mouse button is pressed down.
   * @param rmb true, iff. right mouse button is pressed down.
   */
  class MouseState(val pos : (Int,Int), val lmb : Boolean, val mmb : Boolean, val rmb : Boolean) {
    def x = pos._1
    def y = pos._2
  }

  /**
   * Query current state of mouse.
   * @return mouse state.
   */
  def mouseState() : MouseState = {
    val lmb = Gdx.input.isButtonPressed(Buttons.LEFT)
    val mmb = Gdx.input.isButtonPressed(Buttons.MIDDLE)
    val rmb = Gdx.input.isButtonPressed(Buttons.RIGHT)
    new MouseState((Gdx.input.getX, scrH-Gdx.input.getY), lmb, mmb, rmb)
  }

  implicit def intPairToVec2(pair: (Int,Int)) = new Vector2(pair._1, pair._2)
  implicit def floatPairToVec2(pair: (Float,Float)) = new Vector2(pair._1, pair._2)
  implicit def vec2ToIntPair(vec : Vector2) = (vec.x.toInt, vec.y.toInt)
  implicit def vec2ToFloatPair(vec : Vector2) = (vec.x, vec.y)

  //====================================================================================================================

  private var sb : SpriteBatch = _
  private var atlas : TextureAtlas = _

  private val sounds = mutable.HashMap[String, Sound]()
  private val songs = mutable.HashMap[String, Music]()
  private var songPlaying : Music = _

  private var scrDims = (0,0)

  private val ATLAS_NAME = "atlas"
  private val DATA_PATH = "data/"

  private def dispose() {
    sounds.values.foreach(Utils.safeDispose)
    songs.values.foreach(Utils.safeDispose)
    Utils.safeDispose(font)
    Utils.safeDispose(atlas)
    Utils.safeDispose(sb)
  }

  private class AppListener(initCallback : () => Unit, drawCallback : (Float) => Unit) extends ApplicationListener {
    def create() {
      sb = new SpriteBatch
      Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
      atlas = new TextureAtlas(Utils.loadRes(ATLAS_NAME, "pack"))
      initCallback()
    }
    def resize(width: Int, height: Int) {
      scrDims = (width, height)
    }
    def render() {
      Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
      sb.begin()
      drawCallback(Gdx.graphics.getDeltaTime)
      sb.end()
    }
    def pause() {}
    def resume() {}
    def dispose() {
      Micro.dispose()
    }
  }

  private object Utils {
    def updateAtlas(forceRepack : Boolean = false) {
      val BASE_PATH = "src/data/"
      val lastAtlasUpdate = new File(BASE_PATH + ATLAS_NAME + ".pack").lastModified()
      val lastPathUpdate = new File(BASE_PATH).lastModified()
      val imgFiles = listImageFiles(BASE_PATH)
      if(forceRepack || imgFiles.exists(_.lastModified() > lastAtlasUpdate) || lastPathUpdate > lastAtlasUpdate) {
        new File(BASE_PATH + ATLAS_NAME + ".png").delete()
        TexturePacker2.process(BASE_PATH, BASE_PATH, ATLAS_NAME + ".pack")
      }
    }

    def listImageFiles(path: String) : Array[File] = {
      new File(path).listFiles().filter(_.getName.endsWith(".png"))
    }

    def loadRes(path : String, ext : String) = Gdx.files.internal(DATA_PATH + path + "." + ext)

    def putOrKeep[T](m : mutable.HashMap[String, T], key : String, genFunc : () => T) : T = {
      if(!m.contains(key)) {
        m.put(key, genFunc())
      }
      m(key)
    }

    def safeDispose(obj : Disposable) {
      if(obj != null) {
        obj.dispose()
      }
    }
  }
}
