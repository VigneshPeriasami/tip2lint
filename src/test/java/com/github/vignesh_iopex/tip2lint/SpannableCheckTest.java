package com.github.vignesh_iopex.tip2lint;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Project;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.junit.Test;

import javax.lang.model.element.Modifier;
import java.util.Collections;
import java.util.List;

public class SpannableCheckTest extends LintDetectorTest {
  private static final String TEST_PACKAGE_NAME = "com.helloworld";
  private static final String TEST_FILES_LOCATION = "src/main/java/com/helloworld";

  @Override protected Detector getDetector() {
    return new SpannableCheck();
  }

  @Override protected List<Issue> getIssues() {
    return Collections.singletonList(SpannableCheck.ISSUE);
  }

  @Override protected TestConfiguration getConfiguration(LintClient client, Project project) {
    return new TestConfiguration(client, project, null) {
      @Override public boolean isEnabled(Issue issue) {
        return true;
      }
    };
  }

  @Test public void testSomething() throws Exception {
    MethodSpec main = MethodSpec.methodBuilder("main")
        .addModifiers(Modifier.PUBLIC)
        .returns(void.class)
        .addParameter(String[].class, "args")
        .addStatement("$T some = $S", int.class, 1)
        .addStatement("TextView s = new TextView()")
        .addStatement("s.setText(S.id.hello)")
        .addStatement("$T s = $S", String.class, "Hello, there")
        .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
        .build();

    TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addField(String.class, "hello", Modifier.PUBLIC)
        .addMethod(main)
        .build();

    JavaFile javaFile = JavaFile.builder(TEST_PACKAGE_NAME, helloWorld).build();
    TestFile xml = xml("res/layout/some_layout.xml",
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<FrameLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "android:id=\"@+id/sme\"\n" +
            "android:layout_width=\"match_parent\"\n" +
            "android:layout_height=\"match_parent\">\n" +
            "<TextView android:id=\"@+id/text\"/>\n" +
            "</FrameLayout>");

    String result = lintProject(xml, java(TEST_FILES_LOCATION + "Test.java", javaFile.toString()));
    assertEquals("No warnings.", result);
  }
}
