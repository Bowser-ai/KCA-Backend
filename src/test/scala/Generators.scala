import zio.test.Gen
import com.mv.models.{Filiaal, PartialFiliaal}

object Generators {
  def filiaalGenerator: Gen[Any, Filiaal] = for {
    filiaalNum <- Gen.int(1, 9999)
    optionStr <- Gen.option(Gen.string)
  } yield Filiaal(filiaalNum, optionStr, optionStr, optionStr, optionStr)

  def partialFiliaalGenerator: Gen[Any, PartialFiliaal] = for {
    filiaalNum <- Gen.int(1, 9999)
    optionStr <- Gen.option(Gen.string)
  } yield PartialFiliaal(optionStr, optionStr, optionStr, optionStr)
}
