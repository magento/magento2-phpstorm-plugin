/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.util.indexing.FileBasedIndex
import com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex
import org.json.JSONArray
import org.json.JSONObject

/** Native indexed module declarations and XML/JSON PSI; no PHP parsing or Composer execution. */
internal object MagentoModuleDependencyQueries {
    private const val MAX_MODULE_FILES = 2000
    private const val MAX_FILE_BYTES = 262_144
    private const val MAX_EDGES = 20_000
    private val CACHE = Key.create<CachedValue<Snapshot>>("magento.mcp.moduleDependencies")

    private data class Source(val path: String, val start: Int, val end: Int, val line: Int, val column: Int) {
        fun json() = JSONObject().put("path", path).put("line", line).put("column", column)
            .put("startOffset", start).put("endOffset", end)
    }
    private data class Module(val name: String, val source: Source, val root: String,
                              val composerStatus: String, val packageName: String?) {
        fun json() = JSONObject().put("name", name).put("source", source.json()).put("root", root)
            .put("composerStatus", composerStatus).put("packageName", packageName ?: JSONObject.NULL)
    }
    private data class Edge(val kind: String, val from: String, val target: String, val source: Source,
                            val fromPackage: String? = null, val constraint: String? = null, val constraintSource: Source? = null)
    private data class Problem(val code: String, val path: String, val message: String) {
        fun json() = JSONObject().put("code", code).put("path", path).put("message", message)
    }
    private data class Requirement(val kind: String, val target: String, val constraint: String,
                                   val source: Source, val constraintSource: Source)
    private data class Composer(val status: String, val name: String?, val requirements: List<Requirement>)
    private data class Snapshot(val modules: List<Module>, val edges: List<Edge>, val problems: List<Problem>,
                                val scannedModules: Int, val scannedManifests: Int, val bounded: Boolean)

    fun query(project: Project, moduleName: String, direction: String, limit: Any?): String {
        ProgressManager.checkCanceled()
        if (direction !in setOf("dependencies", "dependents", "both")) return invalid("direction must be dependencies, dependents, or both.")
        if (limit !is Int || limit !in 1..100) return invalid("limit must be a JSON integer between 1 and 100.")
        if (moduleName.length > 256 || moduleName.any { it.isISOControl() }) return invalid("moduleName must be an exact module declaration name of at most 256 characters.")
        val snapshot = CachedValuesManager.getManager(project).getCachedValue(project, CACHE, {
            CachedValueProvider.Result.create(build(project), PsiModificationTracker.MODIFICATION_COUNT,
                ProjectRootManager.getInstance(project))
        }, false)
        val declarations = snapshot.modules.filter { it.name == moduleName }
        val packages = declarations.mapNotNull { it.packageName }.toSet()
        val packageModules = snapshot.modules.filter { it.packageName != null }.groupBy { it.packageName }
        val names = snapshot.modules.map { it.name }.toSet()
        val matches = snapshot.edges.mapNotNull { edge ->
            ProgressManager.checkCanceled()
            val outgoing = edge.from == moduleName
            val incoming = if (edge.kind == "sequence_after") edge.target == moduleName else edge.target in packages
            if (!(when (direction) { "dependencies" -> outgoing; "dependents" -> incoming; else -> outgoing || incoming })) return@mapNotNull null
            edge to if (outgoing && incoming) "both" else if (outgoing) "dependency" else "dependent"
        }.sortedWith(compareBy({ it.first.from }, { it.first.kind }, { it.first.target }, { it.first.source.path }, { it.first.source.start }))
        val truncated = matches.size > limit || declarations.size > 20
        val complete = !snapshot.bounded && snapshot.problems.isEmpty() && !truncated
        val status = if (!complete) "partial" else if (declarations.isEmpty()) "module_not_found" else "ok"
        val edges = matches.take(limit).map { (edge, role) ->
            JSONObject().put("kind", edge.kind).put("direction", role).put("fromModule", edge.from).put("source", edge.source.json()).apply {
                if (edge.kind == "sequence_after") {
                    put("toModule", edge.target).put("targetDeclared", edge.target in names)
                } else {
                    put("fromPackage", edge.fromPackage ?: JSONObject.NULL).put("toPackage", edge.target)
                        .put("versionConstraint", edge.constraint).put("constraintSource", edge.constraintSource!!.json())
                        .put("targetModules", JSONArray(packageModules[edge.target].orEmpty().map { it.name }.distinct().sorted()))
                }
            }
        }
        return JSONObject().put("queryType", "module_dependencies").put("status", status).put("successful", status == "ok")
            .put("complete", complete).put("moduleName", moduleName).put("direction", direction)
            .put("moduleFound", declarations.isNotEmpty()).put("declarationCount", declarations.size)
            .put("declarations", JSONArray(declarations.take(20).map { it.json() }))
            .put("totalEdges", matches.size).put("returnedEdges", edges.size).put("truncated", truncated)
            .put("edges", JSONArray(edges)).put("problemCount", snapshot.problems.size)
            .put("problems", JSONArray(snapshot.problems.take(20).map { it.json() })).put("problemsTruncated", snapshot.problems.size > 20)
            .put("scan", JSONObject().put("moduleFiles", snapshot.scannedModules).put("composerFiles", snapshot.scannedManifests)
                .put("bounded", snapshot.bounded).put("maxModuleFiles", MAX_MODULE_FILES).put("maxFileBytes", MAX_FILE_BYTES).put("maxEdges", MAX_EDGES))
            .put("coverage", "Indexed module declarations and composer.json directly at each module root. sequence_after is ordering, not a hard dependency. Composer require and require-dev are distinct package declarations; targetModules maps only explicit module-root package names. No parent/multi-module package ownership inference, Composer solving, installed/enabled-state checks, transitive closure, effective load order, PHP or runtime validation. Source line/column are 1-based; offsets are UTF-16 with exclusive end. Missing declarations can still have inbound sequence references.")
            .toString()
    }

    private fun invalid(message: String) = JSONObject().put("queryType", "module_dependencies").put("status", "invalid_request")
        .put("successful", false).put("complete", false).put("message", message).toString()

    private fun build(project: Project): Snapshot {
        val modules = mutableListOf<Module>()
        val edges = mutableListOf<Edge>()
        val problems = mutableListOf<Problem>()
        val composers = mutableMapOf<String, Composer>()
        val manager = PsiManager.getInstance(project)
        val index = FileBasedIndex.getInstance()
        val files = FilenameIndex.getVirtualFilesByName("module.xml", GlobalSearchScope.allScope(project))
            .filter { it.parent?.name == "etc" }.sortedBy { it.path }
        var bounded = files.size > MAX_MODULE_FILES
        var processed = 0
        for (virtual in files.take(MAX_MODULE_FILES)) {
            ProgressManager.checkCanceled()
            if (edges.size >= MAX_EDGES) { bounded = true; break }
            processed++
            val path = MagentoMcpSupport.relativePath(project, virtual)
            if (virtual.length > MAX_FILE_BYTES) {
                problems += Problem("file_too_large", path, "Module declaration exceeds $MAX_FILE_BYTES bytes."); continue
            }
            val file = manager.findFile(virtual) as? XmlFile
            if (file == null || PsiTreeUtil.findChildOfType(file, PsiErrorElement::class.java) != null) {
                problems += Problem("invalid_xml", path, "Module XML PSI is unavailable or contains syntax errors."); continue
            }
            val root = file.rootTag ?: continue
            if (root.name != "config") continue
            val keys = index.getFileData(ModuleXmlIndex.KEY, virtual, project).keys
            val moduleRoot = virtual.parent?.parent ?: continue
            val composerFile = moduleRoot.findChild("composer.json")
            val composer = if (composerFile == null) Composer("absent", null, emptyList()) else composers.getOrPut(composerFile.path) {
                val composerPath = MagentoMcpSupport.relativePath(project, composerFile)
                if (composerFile.length > MAX_FILE_BYTES) {
                    problems += Problem("file_too_large", composerPath, "Composer metadata exceeds $MAX_FILE_BYTES bytes.")
                    Composer("invalid", null, emptyList())
                } else {
                    val psi = manager.findFile(composerFile) as? JsonFile
                    if (psi == null || PsiTreeUtil.findChildOfType(psi, PsiErrorElement::class.java) != null) {
                        problems += Problem("invalid_json", composerPath, "Composer JSON PSI is unavailable or contains syntax errors.")
                        Composer("invalid", null, emptyList())
                    } else readComposer(psi, problems)
                }
            }
            for (tag in root.findSubTags("module")) {
                ProgressManager.checkCanceled()
                val nameElement = tag.getAttribute("name")?.valueElement
                if (nameElement == null) {
                    problems += Problem("invalid_module_name", path, "Module declaration has no name.")
                    continue
                }
                val name = nameElement.value
                if (name.isBlank()) { problems += Problem("invalid_module_name", path, "Blank module declaration name."); continue }
                if (name !in keys) {
                    problems += Problem("index_snapshot_mismatch", path, "Module PSI and indexed names disagree; retry after indexing."); continue
                }
                modules += Module(name, source(nameElement), MagentoMcpSupport.relativePath(project, moduleRoot), composer.status, composer.name)
                for (sequence in tag.findSubTags("sequence")) for (dependency in sequence.findSubTags("module")) {
                    ProgressManager.checkCanceled()
                    val target = dependency.getAttribute("name")?.valueElement
                    if (target == null || target.value.isBlank()) {
                        problems += Problem("invalid_sequence", path, "Sequence declaration has no module name.")
                    } else if (edges.size < MAX_EDGES) edges += Edge("sequence_after", name, target.value, source(target))
                    else bounded = true
                }
                for (requirement in composer.requirements) {
                    ProgressManager.checkCanceled()
                    if (edges.size >= MAX_EDGES) { bounded = true; break }
                    edges += Edge(requirement.kind, name, requirement.target, requirement.source, composer.name, requirement.constraint, requirement.constraintSource)
                }
            }
        }
        return Snapshot(modules.toList(), edges.toList(), problems.toList(), processed, composers.size, bounded)
    }

    private fun readComposer(file: JsonFile, problems: MutableList<Problem>): Composer {
        val path = MagentoMcpSupport.relativePath(file.project, file.virtualFile)
        val root = file.topLevelValue as? JsonObject
        if (root == null) {
            problems += Problem("invalid_composer", path, "Composer metadata must be a JSON object.")
            return Composer("invalid", null, emptyList())
        }
        if (root.propertyList.groupBy { it.name }.any { it.value.size > 1 }) {
            problems += Problem("ambiguous_composer", path, "Duplicate top-level Composer keys.")
            return Composer("invalid", null, emptyList())
        }
        val initialProblems = problems.size
        val name = (root.findProperty("name")?.value as? JsonStringLiteral)?.value
        if (name.isNullOrBlank()) problems += Problem("missing_package_name", path, "Composer package name is missing; reverse package mapping is incomplete.")
        val requirements = mutableListOf<Requirement>()
        for (section in listOf("require", "require-dev")) {
            val property = root.findProperty(section) ?: continue
            val values = property.value as? JsonObject
            if (values == null || values.propertyList.groupBy { it.name }.any { it.value.size > 1 }) {
                problems += Problem("invalid_requirements", path, "$section must contain unique package keys and string constraints."); continue
            }
            for (entry in values.propertyList) {
                ProgressManager.checkCanceled()
                val constraint = entry.value as? JsonStringLiteral
                if (constraint == null || entry.name.isBlank()) {
                    problems += Problem("invalid_requirement", path, "$section requirement must have a package name and string constraint."); continue
                }
                if (requirements.size >= MAX_EDGES) {
                    problems += Problem("requirement_limit", path, "Manifest exceeds $MAX_EDGES requirements."); break
                }
                requirements += Requirement(if (section == "require") "composer_require" else "composer_require_dev",
                    entry.name, constraint.value, source(entry.nameElement), source(constraint))
            }
        }
        return Composer(if (problems.size > initialProblems) "partial" else "ok", name, requirements)
    }

    private fun source(element: PsiElement): Source {
        val file = element.containingFile
        val range = element.textRange
        val document = file.viewProvider.document
        val line = document?.getLineNumber(range.startOffset) ?: 0
        return Source(MagentoMcpSupport.relativePath(file.project, file.virtualFile), range.startOffset, range.endOffset,
            line + 1, range.startOffset - (document?.getLineStartOffset(line) ?: 0) + 1)
    }
}
