package io.github.pdkst.autocompletelog.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参数插入处理器
 * 选中变量后自动将变量添加到日志方法的参数列表中
 */
public class LogParameterInsertHandler implements InsertHandler<LookupElement> {

    private final PsiVariable variable;

    public LogParameterInsertHandler(@NotNull PsiVariable variable) {
        this.variable = variable;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
        Editor editor = context.getEditor();
        Project project = context.getProject();
        Document document = editor.getDocument();
        int offset = context.getTailOffset();

        // 获取当前位置的 PSI 元素
        PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
        PsiElement element = file.findElementAt(offset);

        // 查找日志方法调用
        PsiMethodCallExpression methodCall = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
        if (methodCall == null) {
            // 尝试向前查找
            element = file.findElementAt(offset - 1);
            methodCall = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
        }

        if (methodCall == null) {
            return;
        }

        // 检查光标后面是否有 ={}，如果没有则添加
        String textAfter = document.getText().substring(offset, Math.min(offset + 5, document.getTextLength()));
        boolean needAddPlaceholder = !textAfter.startsWith("={}");

        PsiExpressionList argumentList = methodCall.getArgumentList();
        PsiExpression[] arguments = argumentList.getExpressions();

        // 检查变量是否已经在参数列表中
        String varName = variable.getName();
        boolean alreadyExists = false;
        for (PsiExpression arg : arguments) {
            if (arg.getText().equals(varName)) {
                alreadyExists = true;
                break;
            }
        }

        if (alreadyExists) {
            return;  // 变量已存在，无需添加
        }

        // 执行插入操作
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
            documentManager.doPostponedOperationsAndUnblockDocument(document);

            int insertOffset = argumentList.getTextRange().getEndOffset() - 1;

            if (needAddPlaceholder) {
                // 插入 ={} 占位符
                document.insertString(offset, "={}");
                insertOffset += 3;  // 偏移量调整
            }

            // 构建要插入的参数字符串
            StringBuilder paramBuilder = new StringBuilder();

            if (arguments.length == 1) {
                // 只有字符串参数，直接追加
                paramBuilder.append(", ").append(varName);
            } else {
                // 已有其他参数，追加到最后
                paramBuilder.append(", ").append(varName);
            }

            // 插入参数
            document.insertString(insertOffset, paramBuilder.toString());

            documentManager.commitDocument(document);
        });
    }
}
