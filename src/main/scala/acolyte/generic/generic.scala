package acolyte
package generic

import java.lang.{Integer => JInteger, Boolean => JBoolean}
import java.util.{List => JList, Map => JMap}

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

import acolyte.jdbc.Implicits._
import acolyte.jdbc.{Row, RowList}

import shapeless._
import shapeless.ops.hlist._
import shapeless.ops.record._

object GenericRowList {

  /** 
   * Create a new GenericRowList with a case class and derive the column
   * names from the field names of the case class.
   *
   * {{{
   * import acolyte.generic._
   * case class FooRow(a: Int, b: String)
   * val rowList = GenericRowList[FooRow]()
   *
   * // or add rows directly
   * val rowList = GenericRowList[FooRow](
   *   FooRow(1, "hello"), FooRow(2, "world")
   * )
   * }}}
   *
   * @tparam P the type for which we want to create a GenericRowList
   */
  def apply[P <: Product]: GenRowListBuilder[P] = new GenRowListBuilder[P]

  class GenRowListBuilder[P <: Product] {
    def apply[
      L <: HList,   // generic representation of P
      N <: Nat,     // number of columns of a row
      LG <: HList,  // generic representation of P with field names
      K <: HList    // field names 
    ](
      rows: P*
    )(implicit 
      gen: Generic.Aux[P, L],
      length: Length.Aux[L, N],
      classes: ClassList[L],
      trav: ToTraversable.Aux[L, List, Any],
      lgen: LabelledGeneric.Aux[P, LG],
      keys: Keys.Aux[LG, K],
      travKeys: ToTraversable.Aux[K, List, Symbol]
    ): GenericRowList[L] = {
      val labelsVector = Keys[LG].apply.toList.map(_.name).toVector
      val rowList = GenericRowList(
        labels = labelsVector, 
        nullable = Vector.fill(labelsVector.length)(false))
      rows.foldLeft(rowList)(_ append _)
    }
  }


  /** 
   * Create a new GenericRowList from an HList
   *
   * {{{
   * import shapeless.Sized
   * import acolyte.generic._
   * val rowList = GenericRowList.fromHList[Int :: String :: HNil](Sized("id", "name"))
   * }}}
   */
  def fromHList[L <: HList]: FromHListGenRowListBuilder[L] = new FromHListGenRowListBuilder[L]

  class FromHListGenRowListBuilder[L <: HList] {
    def apply[
      N <: Nat    // number of columns of a row
    ](
      labels: Sized[Seq[String], N]
    )(implicit 
      length: Length.Aux[L, N],
      classes: ClassList[L],
      trav: ToTraversable.Aux[L, List, Any]
    ): GenericRowList[L] = {
      val labelsVector = labels.unsized.to[Vector]
      GenericRowList(
        labels = labelsVector, 
        nullable = Vector.fill(labelsVector.length)(false))
    }
  }

}

case class GenericRowList[L <: HList] private(
  rows: Vector[GenericRow[L]] = Vector.empty[GenericRow[L]],
  labels: Vector[String] = Vector.empty[String],
  nullable: Vector[Boolean] = Vector.empty[Boolean]
)(implicit 
  trav: ToTraversable.Aux[L, List, Any],
  classes: ClassList[L]
) extends RowList[GenericRow[L]] {
  def getRows: JList[GenericRow[L]] = rows.asJava

  def append(row: GenericRow[L]): GenericRowList[L] = 
    this.copy(rows = rows :+ row)

  def append[P <: Product](p: P)(implicit gen: Generic.Aux[P, L]): GenericRowList[L] = 
    append(GenericRow(gen.to(p)))

  def :+[P <: Product](p: P)(implicit gen: Generic.Aux[P, L]): GenericRowList[L] = append(p)

  // conversion between shapeless compile time front to runtime class reflection library backend
  def getColumnClasses: JList[Class[_]] = classes.list.asJava

  def getColumnLabels: JMap[String, JInteger] = 
    labels.zipWithIndex.map { case (l, i) ⇒ (l, (i: JInteger)) }.toMap.asJava

  def getColumnNullables: JMap[JInteger, JBoolean] =
    (nullable.indices zip nullable).map { case (i, b) => ((i: JInteger), (b: JBoolean)) }.toMap.asJava

  def withLabel(index: Int, label: String) = {
    val (begin, end) = labels.splitAt(index)
    this.copy(labels = (begin :+ label) ++ end.drop(1))
  }

  def withNullable(index: Int, nullable: Boolean) = {
    val (begin, end) = this.nullable.splitAt(index)
    this.copy(nullable = (begin :+ nullable) ++ end.drop(1))
  }
}

                                      
case class GenericRow[L <: HList](values: L)(implicit trav: ToTraversable.Aux[L, List, Any]) extends Row {
  def cells: JList[AnyRef] = values.toList.map(_.asInstanceOf[AnyRef]).asJava
}