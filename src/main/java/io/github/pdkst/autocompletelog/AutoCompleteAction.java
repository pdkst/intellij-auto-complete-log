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
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (!Objects.equals(methodExpression.getReferenceName(), "info")) {
            return;
        }

        PsiExpressionList parameterList = methodCallExpression.getArgumentList();
        String parameterText = parameterList.getText();

        // Modify the parameter text here
        String modifiedParameterText = modifyParameterText(parameterText);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
            Document document = editor.getDocument();
            documentManager.doPostponedOperationsAndUnblockDocument(document);
            TextRange textRange = parameterList.getTextRange();
            try {
                document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(), modifiedParameterText);
            } catch (StringIndexOutOfBoundsException e1) {
                System.out.println("Error: " + e1.getMessage());
            }
            documentManager.commitDocument(document);
        });
    }

    private static String modifyParameterText(String expression) {
        String stripExpression = StringUtils.strip(expression, "()").trim();

        Pattern pattern = Pattern.compile("^\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(stripExpression);
        if (!matcher.find()) {
            return expression;
        }
        String content = matcher.group(0);
        String argsString = stripExpression.substring(content.length());
        content = StringUtils.strip(content, "\"");
        String[] args = StringUtils.split(argsString, ", ");
        if (args.length == 0) {
            return expression;
        }
        for (int i = 0; i < args.length; i++) {
            int index = args.length - 1 - 1;
            String argument = args[index];
            content = content.replace(StringUtils.trim(argument) + "={}", "");
            Pattern patten = Pattern.compile(argument + "=\\{},?");
            content = patten.matcher(content).replaceAll("");
        }

        StringBuilder contentBuilder = new StringBuilder("(\"").append(content);
        for (int i = 0; i < args.length; i++) {
            String arg = StringUtils.trim(args[i]);
            contentBuilder.append(arg).append("={}");
            if (i != args.length - 1) {
                contentBuilder.append(", ");
            }
        }
        contentBuilder.append("\", ");
        for (int i = 0; i < args.length; i++) {
            contentBuilder.append(args[i]);
            if (i != args.length - 1) {
                contentBuilder.append(", ");
            }
        }

        return contentBuilder.append(")").toString();
    }

}
