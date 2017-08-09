package net.suunto3rdparty

import java.io.File

import org.joda.time.{DateTimeZone, DateTime => ZonedDateTime}
import org.joda.time.format.DateTimeFormat

object Util {

  implicit class MinMaxOptTraversable[T](val seq: Traversable[T]) extends AnyVal {
    def minOpt(implicit ev: Ordering[T]): Option[T] = if (seq.isEmpty) None else Some(seq.min)
    def maxOpt(implicit ev: Ordering[T]): Option[T] = if (seq.isEmpty) None else Some(seq.max)
  }

  def timeToUTC(dateTime: ZonedDateTime) = {
    dateTime.withZone(DateTimeZone.UTC)
  }

  def timeDifference(beg: ZonedDateTime, end: ZonedDateTime): Double = {
    (end.getMillis - beg.getMillis) * 0.001
  }

  def kiloCaloriesFromKilojoules(kj: Double): Int = (kj / 4184).toInt

  def isWindows: Boolean = {
    val OS = System.getProperty("os.name").toLowerCase
    OS.contains("win")
  }
  def isMac: Boolean = {
    val OS = System.getProperty("os.name").toLowerCase
    OS.contains("mac")
  }
  def isUnix: Boolean = {
    val OS = System.getProperty("os.name").toLowerCase
    OS.contains("nix") || OS.contains("nux") || OS.contains("aix")
  }
  def getSuuntoHome: File = {
    if (Util.isWindows) {
      val appData = System.getenv("APPDATA")
      return new File(new File(appData), "Suunto")
    }
    if (Util.isMac) {
      val userHome = System.getProperty("user.home")
      return new File(new File(userHome), "Library/Application Support/Suunto/")
    }
    if (Util.isUnix) {
      val userHome = System.getProperty("user.home")
      return new File(new File(userHome), "Suunto")
    }
    throw new UnsupportedOperationException("Unknown operating system")
  }
}
