package acolyte
package generic

import scala.reflect.{ClassTag, classTag}

import shapeless._
import shapeless.ops.hlist._
import shapeless.ops.record._

/** 
 * Represents the abbility to convert from a type to a list of classes `List[Class[_]]`.
 *
 * For example:
 * {{{
 * scala> val someHlistClasses = ClassList[String :: Int :: HNil].list
 * someHlistClasses: List[Class[_]] = List(class java.lang.String, int)
 *
 * scala> case class Bar(a: Int, b: Double, c: List[String])
 * defined class Bar
 *
 * scala> val barClasses = ClassList[Bar].list
 * barClasses: List[Class[_]] = List(int, double, class scala.collection.immutable.List)
 * }}}
 *
 * @tparam T  A type that has some "child" types which can be returned as a list of `Class`.
 *            E.g. a case class, a tuple, an `HList`, ...
 */
trait ClassList[T] {
  /** Get a list of classes for type `T`. */
  def list: List[Class[_]]
}

object ClassList {
  /** Get an instance of `ClassList` for the type `T`. */
  def apply[T](implicit classes: ClassList[T]): ClassList[T] = classes 

  /** A `ClassList` instance for the empty `HList`. */
  implicit val hnilClassList: ClassList[HNil] = 
    new ClassList[HNil] {
      def list: List[Class[_]] = Nil
    }

  /** A `ClassList` instance for an `HList`. */
  implicit def hconsClassList[H: ClassTag, T <: HList: ClassList]: ClassList[H :: T] = 
    new ClassList[H :: T] {
      def list: List[Class[_]] = 
        classTag[H].runtimeClass.asInstanceOf[Class[H]] +: implicitly[ClassList[T]].list
    }

  /** A `ClassList` instance for a `Product`, e.g. a case class or a tuple. */
  implicit def productClassList[P <: Product, L <: HList](implicit 
    gen: Generic.Aux[P, L], 
    classes: ClassList[L]
  ): ClassList[P] = new ClassList[P] {
    def list: List[Class[_]] = classes.list
  }
}
