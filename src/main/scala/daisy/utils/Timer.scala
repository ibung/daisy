// Original work Copyright 2009-2016 EPFL, Lausanne
// Modified work Copyright 2017 MPI-SWS, Saarbruecken, Germany

package daisy

import scala.language.dynamics


class Timer {
  var beginning: Long = 0L
  var end: Long = 0L

  def start: this.type = {
    beginning = System.currentTimeMillis
    end       = 0L
    this
  }

  def stop: Unit = {
    end         = System.currentTimeMillis
  }

  override def toString: String = {
    "%6d ms".format((end - beginning))
  }

}


// The fancy way of doing this
class TimerStorage extends Dynamic {
  var keys    = List[String]()
  var fields  = Map[String, Timer]()

  def get(name: String): Timer = {
    fields.get(name) match {
      case Some(t) =>
        t

      case None =>
        val t = new Timer()
        fields += name -> t
        keys ::= name
        t
    }
  }

  def selectDynamic(name: String): Timer = get(name)

  override def toString: String = {
    val sb = new StringBuffer

    keys.map(key => s"$key: ${fields(key).toString}").mkString(", ")
  }
}
