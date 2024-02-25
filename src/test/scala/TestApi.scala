import zio.{Scope, ZIO, ZLayer}
import zio.test.*
import com.mv.api.Api
import com.mv.configuration.Configuration
import com.mv.data.FilialenRepository
import com.mv.models.{Filiaal, Remark}
import zio.http.{Body, HttpApp, Path, Request, Response, Root, Status, URL}
import zio.test.Assertion.equalTo
import zio.json.EncoderOps

import java.nio.charset.Charset
import java.sql.SQLException
object TestApi extends ZIOSpecDefault {
  val mockFilialenRepo = ZLayer.succeed(new FilialenRepository:
    override def getFilialen: ZIO[Any, SQLException, List[Filiaal]] =
      ZIO.succeed(List.empty[Filiaal])

    override def getFiliaalByNumber(
        filiaalNumber: Int
    ): ZIO[Any, SQLException, Option[Filiaal]] = ???

    override def createFilialen(
        filialen: List[Filiaal]
    ): ZIO[Any, SQLException, Unit] = ???

    override def updateFiliaal(filiaal: Filiaal): ZIO[Any, SQLException, Unit] =
      ???

    override def getRemarks: ZIO[Any, SQLException, List[Remark]] = ???

    override def getRemarksByFiliaal(
        filiaalNumber: Int
    ): ZIO[Any, SQLException, List[Remark]] = ???

    override def getRemarkById(
        id: Int
    ): ZIO[Any, SQLException, Option[Remark]] = ???

    override def createRemark(
        filiaalId: Int,
        body: String
    ): ZIO[Any, SQLException, Int] = ???

    override def updateRemark(
        id: Int,
        body: String
    ): ZIO[Any, SQLException, Long] = ???
  )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("httpApp")(
      test("Get empty")(for {
        server <- ZIO.service[HttpApp[FilialenRepository]]
        conf <- ZIO.service[Configuration]
        req = Request.get(URL(Path(s"${conf.baseApiUrl}/filialen")))
        expectedStatus = Status.Ok
        expectedBody = Map("filialen" -> Map.empty[Int, Filiaal]).toJson
        result <- assertZIO(
          server
            .runZIO(req)
            .flatMap(resp => resp.body.asString.map(b => resp.status -> b))
        )(
          equalTo(expectedStatus -> expectedBody)
        )
      } yield result).provide(Configuration.live, Api.live, mockFilialenRepo)
    )
}
