package io.github.pdkst.autocompletelog.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiLiteralExpression;

/**
 * 日志变量补全贡献者
 * 注册到 IDE 的补全系统，在日志字符串中提供变量补全
 */
public class LogVariableCompletionContributor extends CompletionContributor {

    public LogVariableCompletionContributor() {
        // 注册补全提供者，针对 Java 字符串字面量
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement().inside(PsiLiteralExpression.class),
                new LogVariableCompletionProvider()
        );
    }
}
