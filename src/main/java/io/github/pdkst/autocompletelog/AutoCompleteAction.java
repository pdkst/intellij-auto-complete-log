package io.github.pdkst.autocompletelog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

public class AutoCompleteAction extends AnAction {
    public AutoCompleteAction() {
        System.out.println("init ........");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) {
            return;
        }

        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        assert psiFile != null;
        PsiElement psiElement = psiFile.findElementAt(editor.getCaretModel().getOffset());

        PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(psiElement, PsiMethodCallExpression.class);
        if (methodCallExpression == null && !editor.getDocument().getText().contains("getText()")) {
            return;
        }

        assert methodCallExpression != null;
        PsiReferenceExpression methodExpression = methodCallExpression.getMethodExpression();
        if (!StringUtils.containsAny(methodExpression.getReferenceName(), "debug", "info", "warn", "error", "trace", "fatal")) {
            return;
        }

        PsiExpressionList parameterList = methodCallExpression.getArgumentList();
        if (parameterList.isEmpty()) {
            // 参数列表为空
            return;
        }
        PsiType[] parameterTypes = parameterList.getExpressionTypes();
        if (!Objects.equals(parameterTypes[0].getCanonicalText(), "java.lang.String")) {
            // 第一个参数不是String类型
            return;
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
            Document document = editor.getDocument();
            documentManager.doPostponedOperationsAndUnblockDocument(document);
            TextRange textRange = parameterList.getTextRange();
            try {
                // Modify the parameter text here
                String modifiedParameterText = reformat(parameterList.getExpressions());
                document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(), "(" + modifiedParameterText + ")");
            } catch (StringIndexOutOfBoundsException e1) {
                System.out.println("Error: " + e1.getMessage());
            }
            documentManager.commitDocument(document);
        });
    }

    private static String reformat(PsiExpression[] parameters) {
        if (ArrayUtils.isEmpty(parameters)) {
            return "";
        }
        String textExpression = parameters[0].getText();
        if (parameters.length == 1) {
            return textExpression;
        }
        String[] otherParameters = Arrays.stream(parameters)
                .skip(1)
                .map(PsiElement::getText)
                .toArray(String[]::new);
        String text = StringUtils.strip(textExpression, "\"");
        String reformatText = reformatContent(text, otherParameters);
        StringBuilder builder = new StringBuilder("\"").append(reformatText).append("\"");
        for (int i = 1; i < parameters.length; i++) {
            builder.append(", ")
                    .append(parameters[i].getText());
        }
        return builder.toString();
    }

    private static String reformatContent(String content, String... args) {
        if (args == null || args.length == 0) {
            return content;
        }
        for (String arg : args) {
            String logExpression = arg + "={}";
            if (content.contains(", " + logExpression)) {
                content = content.replace(", " + logExpression, "");
            } else if (content.contains("," + logExpression)) {
                content = content.replace("," + logExpression, "");
            } else {
                content = content.replace(logExpression, "");
            }
        }

        StringBuilder builder = new StringBuilder(content);
        for (String arg : args) {
            builder.append(", ").append(arg).append("={}");
        }
        return builder.toString();
    }

}
