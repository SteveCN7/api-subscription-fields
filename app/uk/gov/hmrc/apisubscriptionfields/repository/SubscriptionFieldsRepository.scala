/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apisubscriptionfields.repository

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import play.api.Logger
import play.api.libs.json._
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//TODO: think about getting rid of trait
@ImplementedBy(classOf[SubscriptionFieldsMongoRepository])
trait SubscriptionFieldsRepository {

  //TODO consider Future[Boolean]
  def save(subscription: SubscriptionFields): Future[Unit]

  def fetchById(id: String): Future[Option[SubscriptionFields]]
  def fetchByFieldsId(fieldsId: UUID): Future[Option[SubscriptionFields]]

  def delete(id: String): Future[Boolean]
}

@Singleton
class SubscriptionFieldsMongoRepository @Inject()(mongoDbProvider: MongoDbProvider)
  extends ReactiveRepository[SubscriptionFields, BSONObjectID]("subscriptionFields", mongoDbProvider.mongo,
    MongoFormatters.SubscriptionFieldsJF, ReactiveMongoFormats.objectIdFormats)
  with SubscriptionFieldsRepository {

  private implicit val format = MongoFormatters.SubscriptionFieldsJF

  override def indexes = Seq(
    createSingleFieldAscendingIndex(
      indexFieldKey = "id",
      indexName = Some("idIndex")
    ),
    createSingleFieldAscendingIndex(
      indexFieldKey = "fieldsId",
      indexName = Some("fieldsIdIndex")
    )
  )

  private def createSingleFieldAscendingIndex(indexFieldKey: String, indexName: Option[String],
                                              isUnique: Boolean = false, isBackground: Boolean = true): Index = {
    Index(
      key = Seq(indexFieldKey -> Ascending),
      name = indexName,
      unique = isUnique,
      background = isBackground
    )
  }

  //TODO change return type to boolean
  override def save(subscription: SubscriptionFields): Future[Unit] = {
    val selector = selectorById(subscription.id)
    Logger.debug(s"[save] selector: $selector")
    collection.find(selector).one[BSONDocument].flatMap {
      case Some(document) => collection.update(selector = BSONDocument("_id" -> document.get("_id")), update = subscription)
      case _ => collection.insert(subscription)
    }.map {
      writeResult => handleError(writeResult, s"Could not save subscription fields: $subscription")
    }
  }

  override def fetchById(id: String): Future[Option[SubscriptionFields]] = {
    val selector = selectorById(id)
    Logger.debug(s"[fetchById] selector: $selector")
    collection.find(selector).one[SubscriptionFields]
  }
  override def fetchByFieldsId(fieldsId: UUID): Future[Option[SubscriptionFields]] = {
    val selector = Json.obj("fieldsId" -> fieldsId)
    Logger.debug(s"[fetchByFieldsId] selector: $selector")
    collection.find(selector).one[SubscriptionFields]
  }

  override def delete(id: String): Future[Boolean] = {
    val selector = selectorById(id)
    Logger.debug(s"[delete] selector: $selector")
    collection.remove(selector).map {
      writeResult => handleError(writeResult, s"Could not delete subscription fields for id: $id")
    }
  }

  private def handleError[T](result: WriteResult, exceptionMsg: => String): Boolean = {
    result.errmsg.fold(databaseAltered(result)) {
      errMsg => {
        val errorMsg = s"""$exceptionMsg. $errMsg"""
        logger.error(errorMsg)
        throw new RuntimeException(errorMsg)
      }
    }
  }

  private def databaseAltered(writeResult: WriteResult): Boolean = writeResult.n > 0

  private def selectorById(id: String) = Json.obj("id" -> id)

}