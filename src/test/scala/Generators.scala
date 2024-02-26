import zio.test.Gen
import com.mv.models.Filiaal

object Generators {
  def filiaalGenerator: Gen[Any, Filiaal] = for {
    filiaalNum <- Gen.int(1, 9999)
    optionStr <- Gen.option(Gen.string)
  } yield Filiaal(filiaalNum, optionStr, optionStr, optionStr, optionStr)
}
