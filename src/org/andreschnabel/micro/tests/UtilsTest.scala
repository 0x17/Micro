package org.andreschnabel.micro.tests

import org.andreschnabel.micro.Micro.Utils
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.File

class UtilsTest extends FlatSpec with ShouldMatchers {

  "Filename w/out extension " should "return the filename excluding the part after the last dot" in {
    Utils.filenameWithoutExt(new File("/some/long/path/test.mp3")) should equal ("test")
  }

  "List image files " should "return all .png files in a given directory" in {
    Utils.listImageFiles(new File("src/data/")).map(f => f.getName) should equal (Array("alien.png"))
  }

}
