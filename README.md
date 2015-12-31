# acolyte-shapeless
Generic row lists for [acolyte](https://github.com/cchantep/acolyte) with [shapeless](https://github.com/milessabin/shapeless).

## Example

Using acolyte-shapeless we can write :

```scala
import java.sql.Date

import acolyte.generic._

case class PersonRow(name: String, hoursWorked: Float , dateBirth: Date)
val rowList = GenericRowList[PersonRow](
  PersonRow("str", 1.2f, new Date(1l)),
  PersonRow("val", 2.34f, null))
```

If we compare this with how we would write the same without acolyte-shapeless :

```scala
import java.sql.Date

import acolyte.jdbc.RowLists.rowList3
import acolyte.jdbc.AcolyteDSL
import acolyte.jdbc.Implicits._

val rowList = rowList3(classOf[String], classOf[Float], classOf[Date])
  .withLabels(1 -> "name", 2 -> "hoursWorked", 3 -> "dateBirth") :+ 
  ("str", 1.2f, new Date(1l)) :+ 
  ("val", 2.34f, null)
```