package com.collective.celos.ci.mode.test;

import com.collective.celos.ci.testing.fixtures.compare.RecursiveDirComparer;
import com.collective.celos.ci.testing.fixtures.compare.json.JsonContentsDirComparer;
import com.collective.celos.ci.testing.fixtures.convert.avro.AvroToJsonConverter;
import com.collective.celos.ci.testing.fixtures.create.FixDirFromResourceCreator;
import com.collective.celos.ci.testing.fixtures.create.FixDirHierarchyCreator;
import com.collective.celos.ci.testing.fixtures.create.FixFileFromResourceCreator;
import com.collective.celos.ci.testing.fixtures.create.OutputFixDirFromHdfsCreator;
import com.collective.celos.ci.testing.fixtures.deploy.HdfsInputDeployer;
import com.collective.celos.ci.testing.structure.fixobject.FixDir;
import com.collective.celos.ci.testing.structure.fixobject.FixDirTreeConverter;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by akonopko on 27.11.14.
 */
public class TestConfigurationParserTest {

    private TestRun testRun;

    @Before
    public void setUp() {
        testRun = mock(TestRun.class);
        doReturn(new File("/")).when(testRun).getTestCasesDir();
    }

    @Test
    public void testConfigurationParserWorks() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();
        String filePath = Thread.currentThread().getContextClassLoader().getResource("com/collective/celos/defaults/test.js").getFile();
        parser.evaluateTestConfig(new File(filePath));
    }

    @Test
    public void fixDirFromResource() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader("fixDirFromResource(\"stuff\")"), "string");
        FixDirFromResourceCreator creator = (FixDirFromResourceCreator) creatorObj.unwrap();
        Assert.assertEquals(new File("/stuff"), creator.getPath(testRun));
    }

    @Test(expected = JavaScriptException.class)
    public void fixDirFromResourceFails() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader("fixDirFromResource()"), "string");
        FixDirFromResourceCreator creator = (FixDirFromResourceCreator) creatorObj.unwrap();
        Assert.assertEquals(new File("/stuff"), creator.getPath(testRun));
    }


    @Test
    public void fixFileFromResource() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader("fixFileFromResource(\"stuff\")"), "string");
        FixFileFromResourceCreator creator = (FixFileFromResourceCreator) creatorObj.unwrap();
        Assert.assertEquals(new File("/stuff"), creator.getPath(testRun));
    }

    @Test(expected = JavaScriptException.class)
    public void fixFileFromResourceFails() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader("fixFileFromResource()"), "string");
        FixFileFromResourceCreator creator = (FixFileFromResourceCreator) creatorObj.unwrap();
        Assert.assertEquals(new File("stuff"), creator.getPath(testRun));
    }

    @Test(expected = JavaScriptException.class)
    public void testHdfsInputDeployerCall1() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader("hdfsInput()"), "string");
        HdfsInputDeployer creator = (HdfsInputDeployer) creatorObj.unwrap();
        Assert.assertEquals(new File("stuff"), creator.getPath());
    }

    @Test
    public void testHdfsInputDeployerCall2() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader("hdfsInput(fixFileFromResource(\"stuff\"), \"here\")"), "string");
        HdfsInputDeployer creator = (HdfsInputDeployer) creatorObj.unwrap();
        Assert.assertEquals(new Path("here"), creator.getPath());
    }

    @Test(expected = JavaScriptException.class)
    public void testRecursiveDirComparer1() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader("plainCompare()"), "string");
        RecursiveDirComparer creator = (RecursiveDirComparer) creatorObj.unwrap();
    }

    @Test
    public void testRecursiveDirComparer2() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader("plainCompare(fixDirFromResource(\"stuff\"), \"here\")"), "string");

        RecursiveDirComparer comparer = (RecursiveDirComparer) creatorObj.unwrap();

        OutputFixDirFromHdfsCreator actualCreator = (OutputFixDirFromHdfsCreator) comparer.getActualDataCreator();
        FixDirFromResourceCreator expectedDataCreator = (FixDirFromResourceCreator) comparer.getExpectedDataCreator();
        Assert.assertEquals(new Path("here"), actualCreator.getPath());
        Assert.assertEquals(new File("/stuff"), expectedDataCreator.getPath(testRun));
    }

    @Test(expected = JavaScriptException.class)
    public void testRecursiveDirComparer3() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader("plainCompare(fixFileFromResource(\"stuff\"))"), "string");
        RecursiveDirComparer creator = (RecursiveDirComparer) creatorObj.unwrap();
    }


    @Test
    public void testAddTestCase() throws IOException {

        String configJS = "addTestCase({\n" +
                "    name: \"wordcount test case 1\",\n" +
                "    sampleTimeStart: \"2013-11-20T11:00Z\",\n" +
                "    sampleTimeEnd: \"2013-11-20T18:00Z\",\n" +
                "    inputs: [\n" +
                "        hdfsInput(fixDirFromResource(\"src/test/celos-ci/test-1/input/plain/input/wordcount1\"), \"input/wordcount1\"),\n" +
                "        hdfsInput(fixDirFromResource(\"src/test/celos-ci/test-1/input/plain/input/wordcount11\"), \"input/wordcount11\")\n" +
                "    ],\n" +
                "    outputs: [\n" +
                "        plainCompare(fixDirFromResource(\"src/test/celos-ci/test-1/output/plain/output/wordcount1\"), \"output/wordcount1\")\n" +
                "    ]\n" +
                "})\n";

        TestConfigurationParser parser = new TestConfigurationParser();

        parser.evaluateTestConfig(new StringReader(configJS), "string");

    }

    @Test(expected = JavaScriptException.class)
    public void testAddTestCaseNoOutput() throws IOException {

        String configJS = "addTestCase({\n" +
                "    name: \"wordcount test case 1\",\n" +
                "    sampleTimeStart: \"2013-11-20T11:00Z\",\n" +
                "    sampleTimeEnd: \"2013-11-20T18:00Z\",\n" +
                "    inputs: [\n" +
                "        hdfsInput(fixDirFromResource(\"src/test/celos-ci/test-1/input/plain/input/wordcount1\"), \"input/wordcount1\"),\n" +
                "        hdfsInput(fixDirFromResource(\"src/test/celos-ci/test-1/input/plain/input/wordcount11\"), \"input/wordcount11\")\n" +
                "    ]\n" +
                "})\n";

        TestConfigurationParser parser = new TestConfigurationParser();

        parser.evaluateTestConfig(new StringReader(configJS), "string");

    }


    @Test(expected = JavaScriptException.class)
    public void testAddTestCaseNoInput() throws IOException {

        String configJS = "addTestCase({\n" +
                "    name: \"wordcount test case 1\",\n" +
                "    sampleTimeStart: \"2013-11-20T11:00Z\",\n" +
                "    sampleTimeEnd: \"2013-11-20T18:00Z\",\n" +
                "    outputs: [\n" +
                "        plainCompare(fixDirFromResource(\"src/test/celos-ci/test-1/output/plain/output/wordcount1\"), \"output/wordcount1\")\n" +
                "    ]\n" +
                "})\n";

        TestConfigurationParser parser = new TestConfigurationParser();

        parser.evaluateTestConfig(new StringReader(configJS), "string");

    }


    @Test(expected = JavaScriptException.class)
    public void testAddTestCaseNoSampleTimeStart() throws IOException {

        String configJS = "addTestCase({\n" +
                "    name: \"wordcount test case 1\",\n" +
                "    sampleTimeEnd: \"2013-11-20T18:00Z\",\n" +
                "    inputs: [\n" +
                "        hdfsInput(fixDirFromResource(\"src/test/celos-ci/test-1/input/plain/input/wordcount1\"), \"input/wordcount1\"),\n" +
                "        hdfsInput(fixDirFromResource(\"src/test/celos-ci/test-1/input/plain/input/wordcount11\"), \"input/wordcount11\")\n" +
                "    ],\n" +
                "    outputs: [\n" +
                "        plainCompare(fixDirFromResource(\"src/test/celos-ci/test-1/output/plain/output/wordcount1\"), \"output/wordcount1\")\n" +
                "    ]\n" +
                "})\n";

        TestConfigurationParser parser = new TestConfigurationParser();

        parser.evaluateTestConfig(new StringReader(configJS), "string");
    }

    @Test(expected = JavaScriptException.class)
    public void testAddTestCaseNoSampleTimeEnd() throws IOException {

        String configJS = "addTestCase({\n" +
                "    name: \"wordcount test case 1\",\n" +
                "    sampleTimeStart: \"2013-11-20T18:00Z\",\n" +
                "    inputs: [\n" +
                "        hdfsInput(fixDirFromResource(\"src/test/celos-ci/test-1/input/plain/input/wordcount1\"), \"input/wordcount1\"),\n" +
                "        hdfsInput(fixDirFromResource(\"src/test/celos-ci/test-1/input/plain/input/wordcount11\"), \"input/wordcount11\")\n" +
                "    ],\n" +
                "    outputs: [\n" +
                "        plainCompare(fixDirFromResource(\"src/test/celos-ci/test-1/output/plain/output/wordcount1\"), \"output/wordcount1\")\n" +
                "    ]\n" +
                "})\n";

        TestConfigurationParser parser = new TestConfigurationParser();

        parser.evaluateTestConfig(new StringReader(configJS), "string");
    }

    @Test(expected = JavaScriptException.class)
    public void testAvroToJsonFails() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        parser.evaluateTestConfig(new StringReader("avroToJson()"), "string");
    }

    @Test
    public void testAvroToJson() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader("avroToJson(\"1\")"), "string");

        FixDirTreeConverter converter = (FixDirTreeConverter) creatorObj.unwrap();
        Assert.assertTrue(converter.getFixFileConverter() instanceof AvroToJsonConverter);
        Assert.assertTrue(converter.getCreator() instanceof OutputFixDirFromHdfsCreator);
    }

    @Test
    public void testJsonCompare() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader("jsonCompare(fixDirFromResource(\"stuff\"), fixDirFromResource(\"stuff\"))"), "string");

        JsonContentsDirComparer comparer = (JsonContentsDirComparer) creatorObj.unwrap();
        Assert.assertEquals(comparer.getIgnorePaths(), new HashSet<String>());
    }

    @Test
    public void testJsonCompare2() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader("jsonCompare(fixDirFromResource(\"stuff\"), fixDirFromResource(\"stuff\"), [\"path1\", \"path2\"])"), "string");

        JsonContentsDirComparer comparer = (JsonContentsDirComparer) creatorObj.unwrap();
        Assert.assertEquals(comparer.getIgnorePaths(), new HashSet(Lists.newArrayList("path1", "path2")));
    }


    @Test(expected = JavaScriptException.class)
    public void testJsonCompareFails() throws IOException {
        TestConfigurationParser parser = new TestConfigurationParser();

        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader("jsonCompare(fixDirFromResource(\"stuff\"))"), "string");

        JsonContentsDirComparer comparer = (JsonContentsDirComparer) creatorObj.unwrap();
        Assert.assertEquals(comparer.getIgnorePaths(), new HashSet(Lists.newArrayList("path1", "path2")));
    }

    @Test
    public void testFixDir() throws Exception {
        String js = "" +
                "fixDir({" +
                "   file1: fixFile('123')," +
                "   file2: fixFile('234')" +
                "})";

        TestConfigurationParser parser = new TestConfigurationParser();
        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader(js), "string");
        FixDirHierarchyCreator creator = (FixDirHierarchyCreator) creatorObj.unwrap();
        FixDir fixDir = creator.create(null);

        Assert.assertEquals(fixDir.getChildren().size(), 2);
        Assert.assertTrue(IOUtils.contentEquals(fixDir.getChildren().get("file1").asFile().getContent(), new ByteArrayInputStream("123".getBytes())));
        Assert.assertTrue(IOUtils.contentEquals(fixDir.getChildren().get("file2").asFile().getContent(), new ByteArrayInputStream("234".getBytes())));
    }


    @Test
    public void testFixDirWithFixDir() throws Exception {
        String js = "" +
                "fixDir({" +
                "    file0: fixFile('012')," +
                "    dir1: fixDir({" +
                "        file1: fixFile('123')," +
                "        file2: fixFile('234')" +
                "    })" +
                "})";

        TestConfigurationParser parser = new TestConfigurationParser();
        NativeJavaObject creatorObj = (NativeJavaObject) parser.evaluateTestConfig(new StringReader(js), "string");
        FixDirHierarchyCreator creator = (FixDirHierarchyCreator) creatorObj.unwrap();
        FixDir fixDir = creator.create(null);

        Assert.assertEquals(fixDir.getChildren().size(), 2);
        Assert.assertTrue(IOUtils.contentEquals(fixDir.getChildren().get("file0").asFile().getContent(), new ByteArrayInputStream("012".getBytes())));

        FixDir fixDir2 = (FixDir) fixDir.getChildren().get("dir1");
        Assert.assertEquals(fixDir2.getChildren().size(), 2);
        Assert.assertTrue(IOUtils.contentEquals(fixDir2.getChildren().get("file1").asFile().getContent(), new ByteArrayInputStream("123".getBytes())));
        Assert.assertTrue(IOUtils.contentEquals(fixDir2.getChildren().get("file2").asFile().getContent(), new ByteArrayInputStream("234".getBytes())));

    }


}
