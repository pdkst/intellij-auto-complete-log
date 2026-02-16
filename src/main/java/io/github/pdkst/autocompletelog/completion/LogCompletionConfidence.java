package io.github.pdkst.autocompletelog.completion;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ThreeState;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * 日志补全信任度
 * 告诉 IDE 在日志字符串中应该自动触发补全弹窗
 */
public class LogCompletionConfidence extends CompletionConfidence {

    // 日志方法名
    private static final String[] LOG_METHODS = {"debug", "info", "warn", "error", "trace", "fatal"};

    @NotNull
    @Override
    public ThreeState shouldSkipAutopopup(@NotNull PsiElement position, @NotNull PsiFile file, int offset) {
        // 检查是否在字符串字面量中
        PsiLiteralExpression literalExpression = PsiTreeUtil.getParentOfType(position, PsiLiteralExpression.class);
        if (literalExpression == null) {
            return ThreeState.UNSURE;
        }

        // 检查是否在日志方法调用的字符串参数中
        PsiMethodCallExpression methodCall = findLogMethodCall(literalExpression);
        if (methodCall == null) {
            return ThreeState.UNSURE;
        }

        // 检查是否是第一个字符串参数
        if (!isFirstStringParameter(methodCall, literalExpression)) {
            return ThreeState.UNSURE;
        }

        // 返回 NO 表示不跳过自动弹窗，即允许自动触发补全
        return ThreeState.NO;
    }

    /**
     * 查找包含当前位置的日志方法调用
     */
    private PsiMethodCallExpression findLogMethodCall(PsiElement element) {
        PsiMethodCallExpression methodCall = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
        if (methodCall == null) {
            return null;
        }

        PsiReferenceExpression methodExpression = methodCall.getMethodExpression();
        String methodName = methodExpression.getReferenceName();

        if (!StringUtils.equalsAny(methodName, LOG_METHODS)) {
            return null;
        }

        return methodCall;
    }

    /**
     * 检查字面量是否是日志方法的第一个字符串参数
     */
    private boolean isFirstStringParameter(PsiMethodCallExpression methodCall, PsiLiteralExpression literal) {
        PsiExpressionList argumentList = methodCall.getArgumentList();
        if (argumentList == null) {
            return false;
        }

        var expressions = argumentList.getExpressions();

        if (expressions.length == 0) {
            return false;
        }

        var firstArg = expressions[0];
        if (!PsiTreeUtil.isAncestor(firstArg, literal, false)) {
            return false;
        }

        var firstArgType = firstArg.getType();
        return firstArgType != null &&
               "java.lang.String".equals(firstArgType.getCanonicalText());
    }
}
