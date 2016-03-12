package net.suunto3rdparty

import java.time.ZonedDateTime
import Util._

object SuuntoMove {
  case class Header(startTime: ZonedDateTime = ZonedDateTime.now, duration: Int = 0, calories: Int = 0, distance: Int = 0)
  case class TrackPoint(latitude: Double, longitude: Double, elevation: Option[Int], time: String)
}

case class SuuntoMove(var startTime: ZonedDateTime = ZonedDateTime.now, var duration: Int = 0, var calories: Int = 0, var distance: Int = 0) {
  import SuuntoMove._

  def endTime: ZonedDateTime = startTime.plusNanos(duration * 1000L)

  def isOverlapping(that: SuuntoMove): Boolean = !(startTime > that.endTime || endTime < that.startTime)

  private var distanceSamples = Seq[Int]()
  private var heartRateSamples = Seq[Int]()
  private var trackPoints = Seq[TrackPoint]()

  def this(distSamples: Seq[Int], hrSamples: Seq[Int]) = {
    this()
    distanceSamples = distSamples
    heartRateSamples = hrSamples
  }

  def this(header: SuuntoMove.Header, distSamples: Seq[Int], hrSamples: Seq[Int], trackPointSamples: Seq[SuuntoMove.TrackPoint]) = {
    this(header.startTime, header.duration, header.calories, header.distance)
    distanceSamples = distSamples
    heartRateSamples = hrSamples
    trackPoints = trackPointSamples
  }

  def this(header: SuuntoMove.Header, distSamples: Seq[Int], hrSamples: Seq[Int]) = {
    this(header, distSamples, hrSamples, Seq())
  }

  /**
    * Concatenate two moves - non-overlapping (or mostly non-overlapping)
    */
  def concat(that: SuuntoMove): SuuntoMove = {
    ???
  }

  /**
    * this contains move with HR data (Quest)
    *
    * @param that move with GPS data (GPS Track Pod)
    * */
  def mergeGPS(that: SuuntoMove): SuuntoMove = {
    ???
  }
}

