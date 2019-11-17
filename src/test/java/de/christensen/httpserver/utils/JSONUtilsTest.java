package de.christensen.httpserver.utils;


import org.junit.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class JSONUtilsTest {

    @Test
    public void testBuildJSONObject() {
        final Path path = FileSystems.getDefault().getPath("./src/test/resources/test_dir");
        System.out.println(JSONUtils.buildJSONObject(path));
        String expectedResult = "{\"test_dir\":[\"test_file2\",{\"test_sub_dir\":[\"test_sub_file1\",\"test_sub_file2\"]},\"test_file1\"]}";
        assertThat(JSONUtils.buildJSONObject(path).toString()).isEqualTo(expectedResult);
    }

    @Test
    public void testIsDirectory() {
        final Path path = FileSystems.getDefault().getPath("./src/test/resources/test_dir");
        assertThat(JSONUtils.isDirectory(path)).isTrue();
    }

}
