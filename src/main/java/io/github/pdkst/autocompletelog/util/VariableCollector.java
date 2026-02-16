package io.github.pdkst.autocompletelog.util;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 变量收集工具类
 * 收集当前上下文中所有可访问的变量
 */
public class VariableCollector {

    /**
     * 收集当前位置可访问的所有变量
     *
     * @param position 当前 PSI 元素位置
     * @return 变量列表
     */
    @NotNull
    public static List<PsiVariable> collectAvailableVariables(@NotNull PsiElement position) {
        Set<PsiVariable> variables = new HashSet<>();
        
        // 1. 收集局部变量
        collectLocalVariables(position, variables);
        
        // 2. 收集方法参数
        collectParameters(position, variables);
        
        // 3. 收集类成员变量（包括继承的）
        collectFields(position, variables);
        
        // 4. 收集静态变量
        collectStaticFields(position, variables);
        
        return new ArrayList<>(variables);
    }

    /**
     * 收集局部变量
     */
    private static void collectLocalVariables(@NotNull PsiElement position, Set<PsiVariable> variables) {
        PsiMethod method = PsiTreeUtil.getParentOfType(position, PsiMethod.class);
        if (method == null) {
            return;
        }
        
        // 遍历方法体中的局部变量
        PsiCodeBlock body = method.getBody();
        if (body == null) {
            return;
        }
        
        PsiLocalVariable[] localVars = PsiTreeUtil.getChildrenOfType(body, PsiLocalVariable.class);
        if (localVars != null) {
            for (PsiLocalVariable var : localVars) {
                // 只收集在当前位置之前声明的变量
                if (var.getTextOffset() < position.getTextOffset()) {
                    variables.add(var);
                }
            }
        }
        
        // 递归收集代码块中的局部变量
        collectLocalVariablesRecursive(body, position, variables);
    }
    
    /**
     * 递归收集代码块中的局部变量
     */
    private static void collectLocalVariablesRecursive(PsiElement element, PsiElement position, Set<PsiVariable> variables) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PsiDeclarationStatement) {
                PsiDeclarationStatement decl = (PsiDeclarationStatement) child;
                for (PsiElement declared : decl.getDeclaredElements()) {
                    if (declared instanceof PsiLocalVariable && declared.getTextOffset() < position.getTextOffset()) {
                        variables.add((PsiLocalVariable) declared);
                    }
                }
            }
            if (child.getChildren().length > 0) {
                collectLocalVariablesRecursive(child, position, variables);
            }
        }
    }

    /**
     * 收集方法参数
     */
    private static void collectParameters(@NotNull PsiElement position, Set<PsiVariable> variables) {
        PsiMethod method = PsiTreeUtil.getParentOfType(position, PsiMethod.class);
        if (method == null) {
            return;
        }
        
        PsiParameterList parameterList = method.getParameterList();
        for (PsiParameter parameter : parameterList.getParameters()) {
            variables.add(parameter);
        }
    }

    /**
     * 收集类成员变量（包括继承的）
     */
    private static void collectFields(@NotNull PsiElement position, Set<PsiVariable> variables) {
        PsiClass containingClass = PsiTreeUtil.getParentOfType(position, PsiClass.class);
        if (containingClass == null) {
            return;
        }
        
        // 收集当前类的字段
        collectClassFields(containingClass, variables, true);
        
        // 收集继承的字段
        collectInheritedFields(containingClass, variables);
    }
    
    /**
     * 收集类的字段
     */
    private static void collectClassFields(PsiClass psiClass, Set<PsiVariable> variables, boolean includePrivate) {
        for (PsiField field : psiClass.getFields()) {
            // 如果不是私有的，或者在同一类中，则添加
            if (includePrivate || !field.hasModifierProperty(PsiModifier.PRIVATE)) {
                variables.add(field);
            }
        }
    }
    
    /**
     * 收集继承的字段
     */
    private static void collectInheritedFields(PsiClass psiClass, Set<PsiVariable> variables) {
        PsiClass superClass = psiClass.getSuperClass();
        while (superClass != null && !superClass.getQualifiedName().equals("java.lang.Object")) {
            // 只收集非私有的继承字段
            for (PsiField field : superClass.getFields()) {
                if (!field.hasModifierProperty(PsiModifier.PRIVATE)) {
                    variables.add(field);
                }
            }
            superClass = superClass.getSuperClass();
        }
        
        // 收集接口中的常量
        for (PsiClass anInterface : psiClass.getInterfaces()) {
            for (PsiField field : anInterface.getFields()) {
                variables.add(field);
            }
        }
    }

    /**
     * 收集静态变量
     */
    private static void collectStaticFields(@NotNull PsiElement position, Set<PsiVariable> variables) {
        PsiClass containingClass = PsiTreeUtil.getParentOfType(position, PsiClass.class);
        if (containingClass == null) {
            return;
        }
        
        // 收集当前类的静态字段
        for (PsiField field : containingClass.getFields()) {
            if (field.hasModifierProperty(PsiModifier.STATIC)) {
                variables.add(field);
            }
        }
        
        // 收集静态导入的变量
        PsiFile file = position.getContainingFile();
        if (file instanceof PsiJavaFile) {
            PsiImportList importList = ((PsiJavaFile) file).getImportList();
            if (importList != null) {
                for (PsiImportStaticStatement staticImport : importList.getImportStaticStatements()) {
                    PsiElement resolved = staticImport.resolve();
                    if (resolved instanceof PsiField) {
                        variables.add((PsiField) resolved);
                    }
                }
            }
        }
    }
}
