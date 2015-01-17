package ru.ifmo.ctd.ngp.opt.listeners

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.{CodomainComparator, Evaluated}
import ru.ifmo.ctd.ngp.opt.event.{InitializationStartedEvent, BestEvaluatedUpdatedEvent, EvaluationFinishedEvent}

/**
 * A listener which stores the best `Evaluated` generated by the optimization algorithm.
 *
 * @author Maxim Buzdalov
 */
class BestEvaluated[D, C](
  implicit evaluationFinished: EvaluationFinishedEvent[D, C],
           initializationStarted: InitializationStartedEvent,
           codomainComparator: CodomainComparator[C]
) extends BestEvaluatedUpdatedEvent[D, C] {
  private var best: Option[Evaluated[D, C]] = None
  def apply() = best
  def get = best.get

  InitializationStartedEvent() addListener { _ => best = None }
  EvaluationFinishedEvent()    addListener { list =>
    val ov = best
    list.foreach { t =>
      best = best match {
        case None      => Some(t)
        case o@Some(v) => if (codomainComparator(t.output, v.output) > 0) Some(t) else o
      }
    }
    if (ov != best) bestEvaluatedUpdatedAccessor.fire(best.get)
  }
}