package be.maartenballiauw.rider.reversepackagesearch

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKeys

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/*
Code in this class is heavily based on https://code.google.com/archive/p/ideia-google-search/
 */

class ReversePackageSearchAction : AnAction() {
    override fun actionPerformed(ae: AnActionEvent) {
        val editor = ae.getData(DataKeys.EDITOR)
        val project = ae.getData(DataKeys.PROJECT)
        val word = getWordAtCaret(editor!!.document.charsSequence, editor.caretModel.offset)

        if (word != null) {
            val query: String
            try {
                query = URLEncoder.encode(word, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                return
            }

            BrowserUtil.browse("https://packagesearch.azurewebsites.net/?q=" + query, project)
        }
    }

    override fun update(e: AnActionEvent?) {
        // In this method, we decide whether our action is shown in the current context or not.
        // We should only be visible when:
        // - A project is loaded
        // - An editor is open
        // - A PsiElement is available
        //      (current PsiElement is a full File instead of a syntax tree we can reason about, but good to check)

        val project = e!!.getData(DataKeys.PROJECT)

        if (project == null || project.isDefault) {
            e.presentation.isVisible = false
            e.presentation.isEnabled = false
            return
        }

        val editor = e.getData(DataKeys.EDITOR)
        val psiElement = e.getData(DataKeys.PSI_ELEMENT)

        if (editor == null || psiElement == null || editor.document.textLength == 0) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = true
    }


    private fun getWordAtCaret(editorText: CharSequence, caretOffset: Int): String? {
        var caretOffset = caretOffset

        if (editorText.isEmpty()) return null

        if (caretOffset > 0 && !Character.isJavaIdentifierPart(editorText[caretOffset])
                && Character.isJavaIdentifierPart(editorText[caretOffset - 1])) {
            caretOffset--
        }

        if (Character.isJavaIdentifierPart(editorText[caretOffset])) {
            var start = caretOffset
            var end = caretOffset

            while (start > 0 && Character.isJavaIdentifierPart(editorText[start - 1])) {
                start--
            }

            while (end < editorText.length && Character.isJavaIdentifierPart(editorText[end])) {
                end++
            }

            return editorText.subSequence(start, end).toString()
        }

        return null
    }
}