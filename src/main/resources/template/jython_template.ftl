# -*- coding:utf-8 -*-
# HTTP script recorded by nGrinder Recorder at ${meta.time}
######################################################################

from net.grinder.script import Test
from net.grinder.script.Grinder import grinder
from net.grinder.plugin.http import HTTPPluginControl, HTTPRequest
from HTTPClient import NVPair, Cookie, CookieModule, CookiePolicyHandler


connectionDefaults = HTTPPluginControl.getConnectionDefaults()
httpUtilities = HTTPPluginControl.getHTTPUtilities()

log = grinder.logger.info
err = grinder.logger.error

# Set up a cookie handler to log all cookies that are sent and received.
class MyCookiePolicyHandler(CookiePolicyHandler):
	def acceptCookie(self, cookie, request, response):
		# log("accept cookie: %s" % cookie)
		return 1

	def sendCookie(self, cookie, request):
		# log("send cookie: %s" % cookie)
		return 1
 
def createRequest(url, headers=None):
	"""Create an instrumented HTTPRequest."""
	request = HTTPRequest(url=url)
	if headers: request.headers=headers
	return request


CookieModule.setCookiePolicyHandler(MyCookiePolicyHandler())
 
# These definitions at the top level of the file are evaluated once,
# when the worker process is started.
<#-- Generate test wide common header -->
<#if commonHeaders?has_content>
	<#list commonHeaders?keys as eachKey>
		<#if eachKey?has_content>
		<#assign eachHeader = commonHeaders[eachKey]>
		<#if eachHeader.headersId == "defaultHeaders">
connectionDefaults.defaultHeaders = [
			<#list eachHeader.getHeaderArray() as eachNV>
	NVPair(${f.escapeQuote(eachNV.name)}, ${f.escapeQuote(eachNV.value)})<#if eachNV_has_next>,</#if>
			</#list>
]
		</#if>
		</#if>
	</#list>
</#if>

<#-- Generate request common header -->
<#list commonHeaders?keys as eachKey>

	<#if eachKey?has_content>
	<#assign eachHeader = commonHeaders[eachKey]>
	<#if eachHeader.headersId != "defaultHeaders">
${eachHeader.headersId} = [
		<#list eachHeader.getHeaderArray() as eachNV>
	NVPair(${f.escapeQuote(eachNV.name)}, ${f.escapeQuote(eachNV.value)})<#if eachNV_has_next>,</#if >
		</#list>
]
	</#if>
	</#if>
</#list>

<#-- Generate request common header -->
<#list uris?keys as eachKey>
	<#assign eachUri = uris[eachKey]>
request_${eachUri.uriId} = createRequest("${f.generateURLString(eachUri)}");
</#list>


<#if f.hasOption("FollowRedirection")>
connectionDefaults.followRedirects = True
</#if>

class TestRunner:
	"""A TestRunner instance is created for each worker thread."""
	def __init__(self) :
		grinder.statistics.delayReports=True
		pass
	
	def __call__(self) :
<#if pages?size == 0> 
		pass
<#else>	
		try :
	<#list pages as each_page>		
			self.page${each_page_index+1}()
	</#list>
			grinder.statistics.forLastTest.success = 1
		except Exception, e:
			err(e.message)
			grinder.statistics.forLastTest.success = 0
</#if>

<#list pages as each_page>
	def page${each_page_index+1}(self) :
	<#assign request_array = each_page.getRequestArray()>
	<#list request_array as each_request>
		##########################################################################################
		# ${uris[each_request.uri.extends].scheme}://${uris[each_request.uri.extends].host}${each_request.uri.path.getStringValue()}
		##########################################################################################
		<#assign token_arrays = []>
		<#if f.hasOption("AddSleep") && each_request.sleepTime != 0>
		self.sleep(${each_request.sleepTime?c})
		</#if>	
		<#if each_request.uri.queryString?has_content>
			<#assign token_arrays = each_request.uri.queryString.getTokenReferenceArray()>
		</#if>
		<#list token_arrays as each_token>
			<#if each_token.newValue??>
		self.${each_token.tokenId} = ${f.escapeQuote(each_token.newValue)}  <#if each_token.source?has_content># ${each_token.source}</#if>
			</#if>
		</#list>
		<#if each_request.method == "GET">
		result = request_${each_request.uri.extends}.${each_request.method}(
				${f.generatePathString(each_request.uri)},
				None,	
		<#else>
		result = request_${each_request.uri.extends}.${each_request.method}(
				${f.generatePathString(each_request.uri)},
				${f.generatePostParameter(token_array, each_request.getBody())},
		</#if>
				${f.generateHeaderParameter(each_request.headers)}
			) 
		<#if each_request.response.getTokenReferenceArray()?has_content>
			<#list each_request.response.getTokenReferenceArray() as each_token>
				<#if each_token.newValue?has_content>
		${f.generateTokenReference(each_token)}
				</#if>
			</#list>
		</#if>
		<#if f.hasOption("FollowRedirection")>
		self.checkResponse(result, 200,  "${uris[each_request.uri.extends].scheme}://${uris[each_request.uri.extends].host}${each_request.uri.path.getStringValue()}")
		# The followed request might be useless because the redirection is handled above.
		<#else>
		self.checkResponse(result, ${each_request.response.statusCode},  "${uris[each_request.uri.extends].scheme}://${uris[each_request.uri.extends].host}${each_request.uri.path.getStringValue()}")
		</#if>
		</#list>
</#list>
	def checkResponse(self, result, expectedValue, url) :
		if result.getStatusCode() != expectedValue :
			raise Exception(("%s, should return %s, . But returned value was %s" % (url, expectedValue, str(result.statusCode))))

	def sleep(self, millisecond):
		""" sleep during the given millisecond. """
		grinder.sleep(millisecond)		
		
<#if pages?has_content>
<#list pages as each_page>
Test(${each_page_index+1}, "Test${each_page_index+1}").record(TestRunner.page${each_page_index+1})
</#list>
</#if>
