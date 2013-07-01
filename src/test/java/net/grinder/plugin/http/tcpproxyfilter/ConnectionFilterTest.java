package net.grinder.plugin.http.tcpproxyfilter;

import static net.grinder.util.NoOp.noOp;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import net.grinder.tools.tcpproxy.ConnectionDetails;
import net.grinder.tools.tcpproxy.EndPoint;

import org.junit.Test;

/**
 * @author JunHo Yoon
 * 
 */
public class ConnectionFilterTest {
	/**
	 * Test Connection Filter.
	 */
	@Test
	public void testConnectionFilter() {
		ConnectionFilter connectionFilter = new ConnectionFilterImpl();
		connectionFilter.addConnectionDetails(new ConnectionDetails(new EndPoint("hello", 80),
						new EndPoint("world", 80), false), true);
		assertThat(connectionFilter.isFiltered(new EndPoint("world", 80))).isFalse();

		connectionFilter.setFilter(new EndPoint("world", 80), true);
		assertThat(connectionFilter.isFiltered(new EndPoint("world", 80))).isTrue();

		connectionFilter.addConnectionDetails(new ConnectionDetails(new EndPoint("hello", 80),
						new EndPoint("world", 80), true), true);
		assertThat(connectionFilter.isFiltered(new EndPoint("world", 80))).isTrue();
		assertThat(connectionFilter.isChanged()).isTrue();
		assertThat(connectionFilter.getConnectionEndPoints()).hasSize(1);

		assertThat(connectionFilter.isFiltered(new EndPoint("world", 90))).isFalse();
		assertThat(connectionFilter.getConnectionEndPoints()).hasSize(1);
		assertThat(connectionFilter.getConnectionEndPoints()).contains(new EndPoint("world", 80));
		try {
			assertThat(connectionFilter.getConnectionEndPoint(2));
			fail("Error");
		} catch (Exception d) {
			noOp();
		}

	}
}
