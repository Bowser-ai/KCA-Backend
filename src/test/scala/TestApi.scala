import com.mv.api.Api
import com.mv.configuration.Configuration
import com.mv.data.{FilialenRepository, RemarkRepository}
import com.mv.models.Filiaal
import mocks.{MockFilialenRepository, MockRemarkRepository}
import zio.http.*
import zio.json.EncoderOps
import zio.mock.Expectation
import zio.test.*
import zio.test.Assertion.equalTo
import zio.{Scope, ZIO, ZLayer}

import scala.collection.SortedMap
object TestApi extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("httpApp")(
      test("Get filialen") {
        check(
          for {
            filialen <- Gen.listOf(Generators.filiaalGenerator)
          } yield (
            filialen,
            MockFilialenRepository
              .GetFilialen(
                Expectation.value(filialen)
              )
              .toLayer,
            MockRemarkRepository.empty
          )
        ) { case (filialen, filialenRepo, remarkRepo) =>
          (for {
            server <- ZIO
              .service[HttpApp[FilialenRepository & RemarkRepository]]
            conf <- ZIO.service[Configuration]
            req = Request.get(URL(Path(s"${conf.baseApiUrl}/filialen")))
            expectedStatus = Status.Ok
            expectedBody = Map(
              "filialen" -> SortedMap(
                filialen.map(x => x.filiaalNumber -> x)*
              )
            ).toJson
            result <- assertZIO(
              server
                .runZIO(req)
                .flatMap(resp => resp.body.asString.map(b => resp.status -> b))
            )(
              equalTo(expectedStatus -> expectedBody)
            )
          } yield result)
            .provide(
              Configuration.live,
              Api.live,
              filialenRepo,
              remarkRepo
            )
        }
      }
    )
}
