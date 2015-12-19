package com.github.vignesh_iopex.tip2lint;

import com.android.SdkConstants;
import com.android.resources.ResourceFolderType;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.ResourceXmlDetector;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.Speed;
import com.android.tools.lint.detector.api.XmlContext;
import lombok.ast.AstVisitor;
import lombok.ast.ClassDeclaration;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;
import lombok.ast.VariableDeclaration;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static com.android.SdkConstants.ANDROID_NS_NAME_PREFIX;
import static com.android.SdkConstants.ATTR_ID;
import static com.android.SdkConstants.DOT_JAVA;

public class SpannableCheck extends ResourceXmlDetector implements Detector.JavaScanner {
  public static final Issue ISSUE = Issue.create("TextViewCheck",
      "Spannable conflict with textAllCaps",
      "Don't use textAllCaps when using spannable in setText",
      Category.CORRECTNESS, 1, Severity.ERROR,
      new Implementation(SpannableCheck.class,
          EnumSet.of(Scope.ALL_RESOURCE_FILES, Scope.ALL_JAVA_FILES)));

  @Override public Speed getSpeed() {
    return Speed.SLOW;
  }

  @Override public boolean appliesTo(ResourceFolderType folderType) {
    return ResourceFolderType.LAYOUT == folderType;
  }

  @Override public Collection<String> getApplicableElements() {
    return Collections.singletonList(SdkConstants.TEXT_VIEW);
  }

  @Override public void visitElement(XmlContext context, Element element) {
    String attr = element.getAttribute(ANDROID_NS_NAME_PREFIX + ATTR_ID);
    context.report(ISSUE, context.getLocation(element), "Attribute " + attr);
  }

  @Override public boolean appliesTo(Context context, File file) {
    if (LintUtils.endsWith(file.getName(), DOT_JAVA)) {
      return true;
    }
    return super.appliesTo(context, file);
  }

  @Override public List<Class<? extends Node>> getApplicableNodeTypes() {
    return Arrays.<Class <? extends Node>>asList(MethodInvocation.class, VariableDeclaration.class);
  }

  @Override public List<String> getApplicableMethodNames() {
    return Arrays.asList("findViewById", "setText");
  }

  @Override public AstVisitor createJavaVisitor(final JavaContext context) {
    return new ForwardingAstVisitor() {
      @Override public boolean visitMethodInvocation(MethodInvocation node) {
        /*context.report(ISSUE, node, context.getLocation(node), "Pending to check this rule"
            + node.astArguments().first() + "<--"
            + ((Select)node.astArguments().first()).astIdentifier().astValue());*/
        return super.visitMethodInvocation(node);
      }
    };
  }
}
