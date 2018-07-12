package com.workday.warp.common

import org.pmw.tinylog.Logger

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.{Mirror, ModuleSymbol, MethodSymbolApi, Type}

/**
  * Marker trait for property singletons.
  *
  * Concrete property singletons should mix in this trait.
  *
  * See [[CoreWarpProperty]].
  *
  * Created by tomas.mccandless on 11/14/17.
  */
trait WarpPropertyLike {

  /** @return all [[PropertyEntry]] vals defined on the concrete runtime class. */
  def values: Seq[PropertyEntry] = PropertyInspector.values(this.getClass)
}

object PropertyInspector {

  // expected return type of all the members we'll analyze
  private val entryType: Type = universe.typeOf[PropertyEntry]


  /**
    * Uses reflection to read all vals with type [[PropertyEntry]] defined on `class`.
    *
    * `class` must be a scala object (must have a `MODULE$` field), and must mix in [[WarpPropertyLike]].
    *
    * For an overview of scala reflection, see https://docs.scala-lang.org/overviews/reflection/environment-universes-mirrors.html.
    *
    * A [[Mirror]] determines the set of entities that we have reflective access to.
    * The symbol hierarchy is modeled with a mirror for each symbol type (class symbol, method symbol, etc.)
    * This leads to a hierarchy of mirrors that must be traversed to obtain the correct mirror type.
    *
    * TODO investigate implementing this as a compile-time macro.
    *
    * @param `class` runtime class of the scala object containing our properties.
    * @tparam T a subtype of [[WarpPropertyLike]].
    * @return all [[PropertyEntry]] vals defined on `class`.
    */
  def values[T <: WarpPropertyLike](`class`: Class[T]): Seq[PropertyEntry] = {
    Logger.debug(s"getting property values for ${`class`.getCanonicalName}")
    val mirror: Mirror = universe.runtimeMirror(`class`.getClassLoader)

    // concrete type of the property holder class
    // TODO don't read this as a static module, this breaks nested config objects, eg those defined within a class or method.
    val module: ModuleSymbol = mirror.staticModule(`class`.getCanonicalName)
    // we reflected this as a module (singleton), so get the single instance, and obtain a mirror for that instance.
    val instanceMirror = mirror.reflect(mirror.reflectModule(module).instance)

    // the `MODULE$` field holds all the members we are really interested in.
    module.info.members.find(_.name == universe.TermName("MODULE$")) match {
      case Some(member) => member.info.members.toSeq.collect {
        // retain only public accessor methods with the correct return type.
        // recall that scala vals are private fields with generated accessor methods.
        case method: MethodSymbolApi if method.isPublic && method.isAccessor && method.returnType =:= entryType =>
          instanceMirror.reflectMethod(method.asMethod)().asInstanceOf[PropertyEntry]
      }
      case None =>
        throw new RuntimeException(s"it appears that ${`class`.getCanonicalName} is not a scala object (does not have a MODULE$$ field)")
    }
  }
}
