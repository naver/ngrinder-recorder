// HTTP script recorded by nGrinder Recorder at ${meta.time}
import net.grinder.script.GTest
import static net.grinder.script.Grinder.grinder
import net.grinder.plugin.http.*
import HTTPClient.*
import net.grinder.scriptengine.groovy.junit.*
import net.grinder.scriptengine.groovy.junit.annotation.*
import net.grinder.script.GTest
import net.grinder.script.Test.InstrumentationFilter
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import org.junit.*
import org.junit.runner.*


@RunWith(GrinderRunner)
public class TestRunner {
	static def connectionDefaults = HTTPPluginControl.getConnectionDefaults()
	static def httpUtilities = HTTPPluginControl.getHTTPUtilities()
	static def logger = grinder.logger
	def result
	/** Requests */
	<#list uris?keys as eachKey>
		<#assign eachUri = uris[eachKey]>
	static def request_${eachUri.uriId} = createRequest("${f.generateURLString(eachUri)}")
	</#list>
	
	/** Tokens */
	<#list token?keys as eachKey>
		<#assign eachToken = token[eachKey]>
	def ${eachKey}
	</#list>

	/** Common Headers */
	<#list commonHeaders?keys as eachKey>
		<#if eachKey?has_content>
			<#assign eachHeader = commonHeaders[eachKey]>
			<#if eachHeader.headersId != "defaultHeaders">
	static NVPair[] ${eachHeader.headersId} = [
				<#list eachHeader.getHeaderArray() as eachNV>
		new NVPair(${f.escapeQuote(eachNV.name)}, ${f.escapeQuote(eachNV.value)})<#if eachNV_has_next>,</#if >
				</#list>
	]
	
			</#if>
		</#if>
	</#list> 

	<#if pages?has_content>
		<#list pages as each_page>
	def pageTest${each_page_index+1} = new GTest(${each_page_index+1}, "Test${each_page_index+1}")
		</#list>
	</#if>
		
	@BeforeProcess
	static void beforeProcess() {
		CookieModule.setCookiePolicyHandler(new MyCookiePolicyHandler())
		connectionDefaults.timeout = 600
		<#if f.hasOption("FollowRedirection")>
		connectionDefaults.followRedirects = true
		</#if>
	}
	
	
	@BeforeThread
	void beforeThread() {
		grinder.statistics.delayReports=true
		// Instrument the pages
		<#if pages?has_content>
		<#list pages as each_page>
		pageTest${each_page_index+1}.record(this, new MethodNameFilter("page${each_page_index+1}"))
		</#list>
		</#if>
	}

	@Test
	void test() {
	<#if pages?size != 0> 
	<#list pages as each_page>		
		page${each_page_index+1}()
	</#list>
	</#if>
	}

<#list pages as each_page>
	def page${each_page_index+1}() {
	<#assign request_array = each_page.getRequestArray()>
	<#list request_array as each_request>
		/***********************************************************************************************
		 * ${uris[each_request.uri.extends].scheme}://${uris[each_request.uri.extends].host}${each_request.uri.path.getStringValue()}
		 ***********************************************************************************************/
		<#assign token_arrays = []>
		<#if f.hasOption("AddSleep") && each_request.sleepTime != 0>
		sleepFor(${each_request.sleepTime?c})
		</#if>	
		<#if each_request.uri.queryString?has_content>
			<#assign token_arrays = each_request.uri.queryString.getTokenReferenceArray()>
		</#if>
		<#list token_arrays as each_token>
			<#if each_token.newValue??>
		${each_token.tokenId} = ${f.escapeQuote(each_token.newValue)}  <#if each_token.source?has_content>// ${each_token.source}</#if>
			</#if>
		</#list>
		<#if each_request.method == "GET">
		result = request_${each_request.uri.extends}.${each_request.method}(
			${f.generatePathString(each_request.uri)},
			null,	
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
		assertThat("${uris[each_request.uri.extends].scheme}://${uris[each_request.uri.extends].host} should returns ${each_request.response.statusCode}", result.statusCode, is(200))
		<#else>
		assertThat("${uris[each_request.uri.extends].scheme}://${uris[each_request.uri.extends].host} should returns ${each_request.response.statusCode}", result.statusCode, is(${each_request.response.statusCode}))
		/** The followed request might be useless because the redirection is handled above. */
		</#if>
		</#list>
	}
</#list>


	def sleepFor(millisecond) {
		// While validating script, comment out following line to speed up.
		grinder.sleep(millisecond)
	}
	

	def static HTTPRequest createRequest(url, headers=null) {
		def request = new HTTPRequest()
		request.url = url
		if (headers != null) {
			request.headers=headers
		}
		return request
	}
	
	static class MethodNameFilter implements InstrumentationFilter {
		def name
		MethodNameFilter(name) { this.name = name }
		boolean matches(Object item) { item.name == name }
	}
	
	/**
	 * Set up a cookie handler to log all cookies that are sent and received.
	 */
	static class MyCookiePolicyHandler extends CookiePolicyHandler {
		boolean acceptCookie(Cookie cookie, RoRequest request, RoResponse response) {
			//logger.log("accept cookie: {}", cookie)
			return true
		}
		boolean sendCookie(Cookie cookie, RoRequest request) {
			//logger.log("send cookie: {}", cookie)
			return true
		}
	}

}



