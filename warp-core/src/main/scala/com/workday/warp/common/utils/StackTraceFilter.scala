package com.workday.warp.common.utils

/**
  * Utility functions for filtering out unneeded stack frames from exceptions.
  *
  * Created by tomas.mccandless on 5/13/16.
  */
trait StackTraceFilter {

  /**
    * Calls `filter` with a predicate of matching the class name of the calling instance.
    *
    * @param exception [[Throwable]] whose stacktrace will be modified.
    * @return modified `exception` with filtered stack trace.
    */
  def filter[ThrownType <: Throwable](exception: ThrownType): ThrownType = {

     /*
      * Function from [[StackTraceElement]] to [[Boolean]] used as a filter predicate.
      *
      * @param element [[StackTraceElement]] to be checked.
      * @return true iff the class name of the element equals the class name of the calling instance.
      */
    def predicate(element: StackTraceElement): Boolean = element.getClassName == this.getClass.getName

    StackTraceFilter.filter(exception, predicate)
  }
}


// can be imported or mixed in
object StackTraceFilter {

  /**
    * Mutates `exception` by settings its stack trace to the filtered version.
    *
    * @param exception [[Throwable]] whose stacktrace will be modified.
    * @param predicate function from [[StackTraceElement]] to [[Boolean]], used to filter elements of the stack trace.
    * @return modified `exception` with filtered stack trace.
    */
  def filter[ThrownType <: Throwable](exception: ThrownType, predicate: (StackTraceElement) => Boolean): ThrownType = {
    val filteredStackTrace: Array[StackTraceElement] = exception.getStackTrace filter predicate

    Option(exception.getCause) match {
      case Some(cause) =>
        val filteredCause: Throwable = this.filter(cause, predicate)
        val constructor = exception.getClass.getConstructor(classOf[String], classOf[Throwable])
        val newException: ThrownType = constructor.newInstance(exception.getMessage, filteredCause)
        newException.setStackTrace(filteredStackTrace)
        newException
      case None =>
        exception.setStackTrace(filteredStackTrace)
        exception
    }
  }
}
