package net.grinder.plugin.http.tcpproxyfilter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import net.grinder.plugin.http.tcpproxyfilter.options.FileTypeCategory;

import org.junit.Test;

/**
 * @author JunHo Yoon
 *
 */
public class FileTypeFilterImplementationTest {
	/**
	 * Test.
	 */
	@Test
	public void testFileFiltering() {
		// Given
		FileTypeFilterImpl fileTypeFilterImplementation = new FileTypeFilterImpl();
		// When
		fileTypeFilterImplementation.addFilteredCategory(FileTypeCategory.image);
		// Then
		assertThat(fileTypeFilterImplementation.isFiltered("/hello/world/hello.png?wow=ewewe"), is(true));
		assertThat(fileTypeFilterImplementation.isFiltered("/hello/world/hello.png"), is(true));
		assertThat(fileTypeFilterImplementation.isFiltered("/hello/world/hello.png?"), is(true));
		assertThat(fileTypeFilterImplementation.isFiltered("/hello/world/hello.json"), is(false));
	}
}
