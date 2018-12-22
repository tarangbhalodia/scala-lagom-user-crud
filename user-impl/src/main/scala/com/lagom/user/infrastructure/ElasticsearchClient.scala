package com.lagom.user.infrastructure

import java.net.URL

import com.sksamuel.elastic4s.http.{ElasticClient, ElasticNodeEndpoint, ElasticProperties, HttpClient}
import javax.inject.Singleton
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import play.api.Configuration

@Singleton
private[infrastructure] class ElasticsearchClient(configuration: Configuration) extends ElasticClient {

  private val clientUrl = new URL(configuration.get[String]("elasticsearch.url"))

  private val httpClient = {
    ElasticClient(
      ElasticProperties(Seq(ElasticNodeEndpoint(clientUrl.getProtocol, clientUrl.getHost, clientUrl.getPort, clientUrl.getPath match {
        case "" | "/" => None
        case prefix   => Some(prefix)
      }))),
      (requestConfigBuilder: RequestConfig.Builder) => {
        requestConfigBuilder.setConnectionRequestTimeout(6000)
      },
      (httpClientBuilder: HttpAsyncClientBuilder) => {
        if (clientUrl.getUserInfo != null) {
          val Array(user, password) = clientUrl.getUserInfo.split(':')
          val provider = {
            val provider = new BasicCredentialsProvider
            val credentials = new UsernamePasswordCredentials(user, password)
            provider.setCredentials(AuthScope.ANY, credentials)
            provider
          }

          httpClientBuilder.setDefaultCredentialsProvider(provider)
        } else httpClientBuilder
      }
    )
  }

  override def close(): Unit = httpClient.close()

  override def client: HttpClient = httpClient.client

}
