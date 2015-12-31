package acolyte
package generic

import scala.collection.JavaConverters._

import org.specs2.mutable.Specification
import shapeless._
import shapeless.test.illTyped

object GenericRowListSpec extends Specification {

  "Factory (case class)" should {
   
    case class FooRow(id: Int, description: String)

    "create list with given classes" in {
      GenericRowList[FooRow]() aka "list" must beLike { case list =>
        list.getColumnClasses must_== List(classOf[Int], classOf[String]).asJava
      }
    }

    "create list with the fields of the case class as labels" in {
      GenericRowList[FooRow]() must beLike { case list =>
        list.getColumnLabels must_== List("id", "description").zipWithIndex.toMap.asJava
      }
    }
  }

  "Factory (HList)" should {

    "create list with given classes" in {
      GenericRowList.fromHList[Int :: String :: HNil](Sized("id", "description")) aka "list" must beLike { case list =>
        list.getColumnClasses must_== List(classOf[Int], classOf[String]).asJava
      }
    }

    "create list with given labels" in {
      GenericRowList.fromHList[Int :: String :: HNil](Sized("id", "description")) aka "list" must beLike { case list =>
        list.getColumnLabels must_== List("id", "description").zipWithIndex.toMap.asJava
      }
    }

    "not create a list without all labels" in {
       illTyped("""
        GenericRowList.fromHList[Int :: String :: HNil](Sized("boom"))
       """) must_== (())
    }
  }
  
}