package io.github.pdkst.autocompletelog.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import io.github.pdkst.autocompletelog.util.VariableCollector;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * 日志变量补全提供者
 * 实现位置判断和补全触发逻辑
 */
public class LogVariableCompletionProvider extends CompletionProvider<CompletionParameters> {

    // 日志方法名
    private static final String[] LOG_METHODS = {"debug", "info", "warn", "error", "trace", "fatal"};

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        
        // 1. 检查是否在字符串字面量中
        PsiLiteralExpression literalExpression = PsiTreeUtil.getParentOfType(position, PsiLiteralExpression.class);
        if (literalExpression == null) {
            return;
        }
        
        // 2. 检查是否在日志方法调用的字符串参数中
        PsiMethodCallExpression methodCall = findLogMethodCall(literalExpression);
        if (methodCall == null) {
            return;
        }
        
        // 3. 验证字符串是日志方法的第一个参数
        if (!isFirstStringParameter(methodCall, literalExpression)) {
            return;
        }
        
        // 4. 获取当前输入的前缀（变量名部分）
        String prefix = extractVariablePrefix(parameters, literalExpression);
        if (prefix == null) {
            return;
        }
        
        // 5. 收集可用的变量
        List<PsiVariable> variables = VariableCollector.collectAvailableVariables(position);
        
        // 6. 使用带前缀的结果集进行过滤
        CompletionResultSet filteredResult = result.withPrefixMatcher(prefix);
        
        // 7. 添加补全建议
        for (PsiVariable variable : variables) {
            String varName = variable.getName();
            if (varName != null) {
                LookupElementBuilder element = LookupElementBuilder.create(varName)
                        .withTypeText(variable.getType().getPresentableText(), true)
                        .withIcon(variable.getIcon(0))
                        .withInsertHandler(new LogParameterInsertHandler(variable));
                filteredResult.addElement(element);
            }
        }
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
        PsiExpression[] expressions = argumentList.getExpressions();
        
        if (expressions.length == 0) {
            return false;
        }
        
        PsiExpression firstArg = expressions[0];
        if (!PsiTreeUtil.isAncestor(firstArg, literal, false)) {
            return false;
        }

        PsiType firstArgType = firstArg.getType();
        return firstArgType != null &&
               Objects.equals(firstArgType.getCanonicalText(), "java.lang.String");
    }
    
    /**
     * 提取当前位置的变量名前缀
     */
    private String extractVariablePrefix(CompletionParameters parameters, PsiLiteralExpression literal) {
        Document document = parameters.getEditor().getDocument();
        int offset = parameters.getOffset();
        
        // 获取字符串的内容范围（不包括引号）
        TextRange literalRange = literal.getTextRange();
        int contentStart = literalRange.getStartOffset() + 1;
        int contentEnd = literalRange.getEndOffset() - 1;
        
        // 确保光标在字符串内容中
        if (offset < contentStart || offset > contentEnd) {
            return null;
        }
        
        // 获取字符串内容
        String fullContent = document.getText(new TextRange(contentStart, contentEnd));
        int relativeOffset = offset - contentStart;
        
        // 从光标位置向前查找，确定变量名的开始位置
        int varStart = relativeOffset;
        while (varStart > 0 && isValidIdentifierChar(fullContent.charAt(varStart - 1))) {
            varStart--;
        }
        
        // 提取变量名前缀
        if (varStart < relativeOffset) {
            return fullContent.substring(varStart, relativeOffset);
        }
        
        // 空前缀时也允许触发补全
        return "";
    }
    
    private boolean isValidIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
