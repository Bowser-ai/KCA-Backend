import com.mv.api.Api
import com.mv.configuration.Configuration
import com.mv.data.{FiliaalRepository, RemarkRepository}
import com.mv.domain.FiliaalManager
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
      test("get filialen") {
        check(Gen.listOf(Generators.filiaalGenerator).zip(Gen.int(1, 999))) {
          (filialen, pageSize) =>
            (for {
              server <- ZIO
                .service[HttpApp[FiliaalManager & RemarkRepository]]
              conf <- ZIO.service[Configuration]
              req = Request.get(
                URL(
                  Path(s"${conf.baseApiUrl}/filialen"),
                  queryParams = QueryParams("page_size" -> pageSize.toString)
                )
              )
              // Expectations.
              expectedStatus = Status.Ok
              expectedBody = Map(
                "filialen" -> SortedMap(
                  filialen
                    .map(x => x.filiaalNumber -> x)*
                )
              ).toJson
              // Real results.
              result <- assertZIO(
                server
                  .runZIO(req)
                  .flatMap(resp =>
                    resp.body.asString.map(b => resp.status -> b)
                  )
              )(
                equalTo(expectedStatus -> expectedBody)
              )
            } yield result)
              .provide(
                Configuration.live,
                Api.live,
                FiliaalManager.live,
                (MockFilialenRepository
                  .GetFilialen(
                    assertion = Assertion.equalTo(pageSize -> 0),
                    result = Expectation.value(filialen)
                  ) &&
                  MockFilialenRepository
                    .GetTotalCount(Expectation.value(pageSize.toLong))).toLayer,
                MockRemarkRepository.empty
              )
        }
      },
      test("create filialen")(
        check(Gen.listOf(Generators.filiaalGenerator))(filialen =>
          (for {
            server <- ZIO
              .service[HttpApp[FiliaalManager & RemarkRepository]]
            conf <- ZIO.service[Configuration]
            req = Request.put(
              URL(
                Path(s"${conf.baseApiUrl}/filialen")
              ),
              Body.fromString(filialen.toJson)
            )
            expectedStatus = Status.Ok
            expectedBody = filialen.toJson
            // Real results.
            result <- assertZIO(
              server
                .runZIO(req)
                .flatMap(resp => resp.body.asString.map(b => resp.status -> b))
            )(
              equalTo(expectedStatus -> expectedBody)
            )
          } yield result).provide(
            Configuration.live,
            Api.live,
            FiliaalManager.live,
            MockFilialenRepository
              .CreateFilialen(
                Assertion.equalTo(filialen),
                Expectation.unit
              )
              .toLayer,
            MockRemarkRepository.empty
          )
        )
      ),
      test("update filiaal")(
        check(Generators.partialFiliaalGenerator.zip(Gen.int(1, 999))) {
          case (partialFiliaal, filiaalNumber) =>
            (for {
              server <- ZIO
                .service[HttpApp[FiliaalManager & RemarkRepository]]
              conf <- ZIO.service[Configuration]
              req = Request.patch(
                URL(
                  Path(
                    s"${conf.baseApiUrl}/filialen/${filiaalNumber.toString}"
                  )
                ),
                Body.fromString(partialFiliaal.toJson)
              )
              expectedStatus = Status.Ok
              expectedBody = partialFiliaal.asFiliaal(filiaalNumber).toJson
              // Real results.
              result <- assertZIO(
                server
                  .runZIO(req)
                  .flatMap(resp =>
                    resp.body.asString.map(b => resp.status -> b)
                  )
              )(
                equalTo(expectedStatus -> expectedBody)
              )
            } yield result).provide(
              Configuration.live,
              Api.live,
              FiliaalManager.live,
              (MockFilialenRepository
                .GetFiliaalByNumber(
                  Assertion.equalTo(filiaalNumber),
                  Expectation.value(
                    Option(partialFiliaal.asFiliaal(filiaalNumber))
                  )
                ) &&
                MockFilialenRepository
                  .UpdateFiliaal(
                    Assertion.equalTo(partialFiliaal.asFiliaal(filiaalNumber)),
                    Expectation.unit
                  )).toLayer,
              MockRemarkRepository.empty
            )
        }
      ),
      test("delete filiaal")(
        check(Generators.filiaalGenerator)(filiaal =>
          (for {
            server <- ZIO
              .service[HttpApp[FiliaalManager & RemarkRepository]]
            conf <- ZIO.service[Configuration]
            req = Request.delete(
              URL(
                Path(
                  s"${conf.baseApiUrl}/filialen/${filiaal.filiaalNumber.toString}"
                )
              )
            )
            expectedStatus = Status.Ok
            expectedBody = filiaal.toJson
            // Real results.
            result <- assertZIO(
              server
                .runZIO(req)
                .flatMap(resp => resp.body.asString.map(b => resp.status -> b))
            )(
              equalTo(expectedStatus -> expectedBody)
            )
          } yield result).provide(
            Configuration.live,
            Api.live,
            FiliaalManager.live,
            (MockFilialenRepository
              .GetFiliaalByNumber(
                Assertion.equalTo(filiaal.filiaalNumber),
                Expectation.value(
                  Option(filiaal)
                )
              ) &&
              MockFilialenRepository
                .DeleteFiliaal(
                  Assertion.equalTo(filiaal.filiaalNumber),
                  Expectation.unit
                )).toLayer,
            MockRemarkRepository.empty
          )
        )
      )
    )
}
