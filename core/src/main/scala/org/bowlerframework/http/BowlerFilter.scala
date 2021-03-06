package org.bowlerframework.http

import org.scalatra.ScalatraFilter
import javax.servlet.FilterConfig
import org.scalatra.fileupload.FileUploadSupport
import util.matching.Regex
import org.bowlerframework._
import controller.Controller

class BowlerFilter extends ScalatraFilter with FileUploadSupport with BowlerHttpApplicationRouter{

  var bootstrap: AnyRef = null

  override def initialize(config: FilterConfig): Unit = {
    super.initialize(config)
    BowlerConfigurator.setApplicationRouter(this)
    BowlerConfigurator.isServletApp = false
    println(config.getServletContext.getRealPath("WEB-INF"))

    if(config.getInitParameter("bootstrapClass") != null) {
      bootstrap = Class.forName(config.getInitParameter("bootstrapClass")).newInstance.asInstanceOf[AnyRef]
    }
  }

  def addApplicationRoute(protocol: HTTP.Method, routeMatchers: String, routeExecutor: RouteExecutor) = {
      protocol.toString match {
        case "GET" => this.get(routeMatchers){mapExecutor(routeExecutor)}
        case "PUT" => this.put(routeMatchers){mapExecutor(routeExecutor)}
        case "POST" => this.post(routeMatchers){mapExecutor(routeExecutor)}
        case "DELETE" => this.delete(routeMatchers){mapExecutor(routeExecutor)}
      }
  }


  def addApplicationRoute(protocol: HTTP.Method, routeMatchers: Regex, routeExecutor: RouteExecutor) = {
      protocol.toString match {
        case "GET" => this.get(routeMatchers){mapExecutor(routeExecutor)}
        case "PUT" => this.put(routeMatchers){mapExecutor(routeExecutor)}
        case "POST" => this.post(routeMatchers){mapExecutor(routeExecutor)}
        case "DELETE" => this.delete(routeMatchers){mapExecutor(routeExecutor)}
      }
  }

  override def requestPath = if (request.getPathInfo != null) request.getPathInfo else request.getServletPath

  private def mapExecutor(routeExecutor: RouteExecutor): Any = {
    val bowlerRequest = new BowlerHttpRequest(this.requestPath, this.request, this.flattenParameters(this.request, this.params, this.multiParams, this.fileParams, this.fileMultiParams))
    val scope = RequestScope(bowlerRequest, new BowlerHttpResponse(this.response))
    routeExecutor.executeRoute(scope)
  }

}
