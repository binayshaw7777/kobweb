@file:Suppress("LeakingThis") // Following official Gradle guidance

package com.varabyte.kobwebx.gradle.markdown

import com.varabyte.kobweb.gradle.application.extensions.hasDependencyNamed
import com.varabyte.kobwebx.gradle.markdown.ext.kobwebcall.KobwebCallExtension
import org.commonmark.Extension
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.Image
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser
import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class MarkdownConfig {
    /**
     * The path to all markdown resources to process.
     *
     * This path should live in the root of the project's `resources` folder, e.g. `src/jsMain/resources`
     */
    abstract val markdownPath: Property<String>

    init {
        markdownPath.convention("markdown")
    }
}

/**
 * List feature extensions to markdown that this plugin supports.
 *
 * This block will be nested under [MarkdownConfig], e.g.
 *
 * ```
 * kobwebx {
 *   markdown {
 *     features { ... }
 *   }
 * }
 * ```
 */
abstract class MarkdownFeatures {
    /**
     * If true, convert URLs and email addresses into links automatically.
     *
     * See also: https://github.com/commonmark/commonmark-java#autolink
     */
    abstract val autolink: Property<Boolean>

    /**
     * If true, support front matter (a header YAML block at the top of your markdown file with key/value pairs)
     *
     * See also: https://github.com/commonmark/commonmark-java#yaml-front-matter
     */
    abstract val frontMatter: Property<Boolean>

    /**
     * If true, support a syntax for inserting a composable call into the final generated Kotlin source:
     *
     * ```
     * {{{ .components.widgets.VisitorCounter }}}
     * ```
     *
     * becomes:
     *
     * ```
     * org.example.myproject.components.widgets.VisitorCounter()
     * ```
     */
    abstract val kobwebCall: Property<Boolean>

    /**
     * The delimiters used to delineate code for the [kobwebCall] feature.
     *
     * By default, it is curly braces `{}` but you can change the character if this
     * causes a problem in your project for some unforeseeable reason.
     */
    abstract val kobwebCallDelimiters: Property<Pair<Char, Char>>

    /**
     * If true, support creating tables via pipe syntax.
     *
     * See also: https://github.com/commonmark/commonmark-java#tables
     * See also: https://docs.github.com/en/github/writing-on-github/working-with-advanced-formatting/organizing-information-with-tables
     */
    abstract val tables: Property<Boolean>

    /**
     * If true, support creating task list items via a convenient syntax:
     *
     * ```
     * - [ ] task #1
     * - [x] task #2
     * ```
     *
     * See also: https://github.com/commonmark/commonmark-java#task-list-items
     */
    abstract val taskList: Property<Boolean>

    init {
        autolink.convention(true)
        frontMatter.convention(true)
        kobwebCall.convention(true)
        kobwebCallDelimiters.convention('{' to '}')
        tables.convention(true)
        taskList.convention(true)
    }

    /**
     * Create a markdown parser configured based on the currently activated features.
     */
    fun createParser(): Parser {
        val extensions = mutableListOf<Extension>()
        if (autolink.get()) {
            extensions.add(AutolinkExtension.create())
        }
        if (frontMatter.get()) {
            extensions.add(YamlFrontMatterExtension.create())
        }
        if (kobwebCall.get()) {
            extensions.add(KobwebCallExtension.create(kobwebCallDelimiters.get()))
        }
        if (tables.get()) {
            extensions.add(TablesExtension.create())
        }
        if (taskList.get()) {
            extensions.add(TaskListItemsExtension.create())
        }

        return Parser.builder()
            .extensions(extensions)
            .build()
    }
}

private const val JB_DOM = "org.jetbrains.compose.web.dom"
private const val SILK = "com.varabyte.kobweb.silk.components"

class NodeScope {
    /** If set, will cause the Markdown visit to visit these nodes instead of the node's original children. */
    var childrenOverride: List<Node>? = null
}

/**
 * Specify which composable components should be used to render various html tags generated by the markdown parser.
 *
 * This block will be nested under [MarkdownConfig], e.g.
 *
 * ```
 * kobwebx {
 *   markdown {
 *     components { ... }
 *   }
 * }
 * ```
 */
abstract class MarkdownComponents @Inject constructor(project: Project) {
    /**
     * Use Silk components instead of Web Compose components when relevant.
     *
     * If the user's project doesn't have a dependency on the Silk library, this should be set to false.
     */
    abstract val useSilk: Property<Boolean>

    abstract val text: Property<NodeScope.(Text) -> String>
    abstract val img: Property<NodeScope.(Image) -> String>
    abstract val h1: Property<NodeScope.(Heading) -> String>
    abstract val h2: Property<NodeScope.(Heading) -> String>
    abstract val h3: Property<NodeScope.(Heading) -> String>
    abstract val h4: Property<NodeScope.(Heading) -> String>
    abstract val h5: Property<NodeScope.(Heading) -> String>
    abstract val h6: Property<NodeScope.(Heading) -> String>
    abstract val p: Property<NodeScope.(Paragraph) -> String>
    abstract val br: Property<NodeScope.(HardLineBreak) -> String>
    abstract val a: Property<NodeScope.(Link) -> String>
    abstract val em: Property<NodeScope.(Emphasis) -> String>
    abstract val strong: Property<NodeScope.(StrongEmphasis) -> String>
    abstract val hr: Property<NodeScope.(ThematicBreak) -> String>
    abstract val ul: Property<NodeScope.(BulletList) -> String>
    abstract val ol: Property<NodeScope.(OrderedList) -> String>
    abstract val li: Property<NodeScope.(ListItem) -> String>
    abstract val code: Property<NodeScope.(FencedCodeBlock) -> String>
    abstract val inlineCode: Property<NodeScope.(Code) -> String>
    abstract val table: Property<NodeScope.(TableBlock) -> String>
    abstract val thead: Property<NodeScope.(TableHead) -> String>
    abstract val tbody: Property<NodeScope.(TableBody) -> String>
    abstract val tr: Property<NodeScope.(TableRow) -> String>
    abstract val td: Property<NodeScope.(TableCell) -> String>
    abstract val th: Property<NodeScope.(TableCell) -> String>

    init {
        project.afterEvaluate {
            useSilk.convention(project.hasDependencyNamed("kobweb-silk"))
        }

        text.convention { text ->
            val literal = text.literal.escapeQuotes()
            if (useSilk.get()) {
                "$SILK.text.Text(\"${literal}\")"
            } else {
                "$JB_DOM.Text(\"${literal}\")"
            }
        }
        img.convention { "$JB_DOM.Img" }
        h1.convention { "$JB_DOM.H1" }
        h2.convention { "$JB_DOM.H2" }
        h3.convention { "$JB_DOM.H3" }
        h4.convention { "$JB_DOM.H4" }
        h5.convention { "$JB_DOM.H5" }
        h6.convention { "$JB_DOM.H6" }
        p.convention { "$JB_DOM.P" }
        br.convention { "$JB_DOM.Br" }
        a.convention { link ->
            if (useSilk.get()) {
                val linkText = link.children().filterIsInstance<Text>().firstOrNull()?.literal?.escapeQuotes().orEmpty()
                childrenOverride = listOf() // We "consumed" the children, no more need to visit them
                "$SILK.navigation.Link(\"${link.destination}\", \"$linkText\")"
            } else {
                "$JB_DOM.A(\"${link.destination}\")"
            }
        }
        em.convention { "$JB_DOM.Em" }
        strong.convention { "$JB_DOM.B" }
        hr.convention { "$JB_DOM.Hr" }
        ul.convention { "$JB_DOM.Ul" }
        ol.convention { "$JB_DOM.Ol" }
        li.convention { "$JB_DOM.Li" }
        code.convention { codeBlock ->
            childrenOverride = codeBlock.literal.trim().split("\n")
                .map { line -> Text("$line\\n") }
            // See also: https://stackoverflow.com/a/31775545/1299302
            "$JB_DOM.Code(attrs = { style { property(\"display\", \"block\"); property(\"white-space\", \"pre-wrap\") } })"
        }
        inlineCode.convention { code ->
            childrenOverride = listOf(Text(code.literal))
            "$JB_DOM.Code"
        }
        table.convention { "$JB_DOM.Table" }
        thead.convention { "$JB_DOM.Thead" }
        tbody.convention { "$JB_DOM.Tbody" }
        tr.convention { "$JB_DOM.Tr" }
        td.convention { "$JB_DOM.Td" }
        th.convention { "$JB_DOM.Th" }
    }
}