package top.deeke.groupcontrol.utils

import org.json.JSONArray
import org.json.JSONObject
import java.util.Collections
import java.util.IdentityHashMap

class DeekeScriptJsonParser {

    data class DeekeScriptMethod(
        val title: String,
        val jsFile: String,
        val icon: String? = null,
        val settingPage: JSONObject? = null,
        val hidden: Boolean = false,
        val runType: String? = null,
        val packageName: String? = null,
        val columns: Int? = null,
        val autoOpen: Boolean? = null,
        val groupTitle: String? = null,
        val activity: JSONObject? = null,
        val minVersion: Int? = null
    ) {
        val displayName: String
            get() {
                val group = groupTitle?.takeIf { it.isNotBlank() }
                return if (group == null || title.isBlank()) title.ifBlank { group.orEmpty() } else "$group / $title"
            }
    }

    /**
     * 解析 deekeScript.json。
     * 新格式：顶层 groups[].methods，子功能在 method.activity.groups 或 activity.methods。
     * 旧格式：顶层 methods，分组名在 cate.data[].count。
     */
    fun parseDeekeScriptJson(jsonString: String): List<DeekeScriptMethod> {
        val methods = mutableListOf<DeekeScriptMethod>()
        val seen = Collections.newSetFromMap(IdentityHashMap<JSONObject, Boolean>())

        try {
            val root = JSONObject(jsonString)
            val groups = root.optJSONArray("groups")
            if (groups != null && groups.length() > 0) {
                collectGroups(groups, null, methods, seen)
            } else {
                collectMethodArray(root.optJSONArray("methods"), cateTitles(root.optJSONObject("cate")), methods, seen)
            }
            collectRemaining(root, methods, seen)
        } catch (e: Exception) {
            println("JSON解析出错: ${e.message}")
            e.printStackTrace()
        }

        println("总共解析到${methods.size}个methods")
        return methods
    }

    private fun collectGroups(
        groups: JSONArray,
        parentTitle: String?,
        methods: MutableList<DeekeScriptMethod>,
        seen: MutableSet<JSONObject>
    ) {
        for (i in 0 until groups.length()) {
            val group = groups.optJSONObject(i) ?: continue
            val groupTitle = joinTitle(parentTitle, group.optString("title", ""))
            collectMethodArray(group.optJSONArray("methods"), List(group.optJSONArray("methods")?.length() ?: 0) { groupTitle }, methods, seen)
        }
    }

    private fun collectMethodArray(
        array: JSONArray?,
        groupTitles: List<String?>,
        methods: MutableList<DeekeScriptMethod>,
        seen: MutableSet<JSONObject>
    ) {
        if (array == null) return
        for (i in 0 until array.length()) {
            val method = array.optJSONObject(i) ?: continue
            if (!seen.add(method)) continue
            val groupTitle = groupTitles.getOrNull(i)
            val parsed = parseMethod(method, groupTitle)
            if (parsed.jsFile.isNotBlank()) {
                methods.add(parsed)
            }
            collectActivity(method.optJSONObject("activity"), groupTitle, parsed.title, methods, seen)
        }
    }

    private fun collectActivity(
        activity: JSONObject?,
        parentGroupTitle: String?,
        methodTitle: String,
        methods: MutableList<DeekeScriptMethod>,
        seen: MutableSet<JSONObject>
    ) {
        if (activity == null) return
        val nestedGroups = activity.optJSONArray("groups")
        if (nestedGroups != null && nestedGroups.length() > 0) {
            collectGroups(nestedGroups, parentGroupTitle, methods, seen)
            return
        }
        val nestedTitle = joinTitle(parentGroupTitle, activity.optString("title", methodTitle))
        val nestedMethods = activity.optJSONArray("methods") ?: return
        collectMethodArray(nestedMethods, List(nestedMethods.length()) { nestedTitle }, methods, seen)
    }

    /**
     * 收集 groups / 顶层 methods 之外仍挂在 methods 数组上的功能，例如旧示例里的 bottomMenus。
     */
    private fun collectRemaining(node: Any?, methods: MutableList<DeekeScriptMethod>, seen: MutableSet<JSONObject>) {
        when (node) {
            is JSONObject -> {
                val keys = node.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = node.opt(key)
                    if (key == "methods" && value is JSONArray) {
                        collectMethodArray(value, emptyList(), methods, seen)
                    } else {
                        collectRemaining(value, methods, seen)
                    }
                }
            }
            is JSONArray -> {
                for (i in 0 until node.length()) {
                    collectRemaining(node.opt(i), methods, seen)
                }
            }
        }
    }

    private fun cateTitles(cate: JSONObject?): List<String?> {
        val data = cate?.optJSONArray("data") ?: return emptyList()
        val titles = mutableListOf<String?>()
        for (i in 0 until data.length()) {
            val item = data.optJSONObject(i) ?: continue
            val title = item.optString("title", "").takeIf { it.isNotBlank() }
            repeat(item.optInt("count", 0).coerceAtLeast(0)) {
                titles.add(title)
            }
        }
        return titles
    }

    private fun parseMethod(method: JSONObject, groupTitle: String?): DeekeScriptMethod {
        return DeekeScriptMethod(
            title = method.optString("title", ""),
            jsFile = method.optString("jsFile", ""),
            icon = method.optString("icon", "").takeIf { method.has("icon") && !method.isNull("icon") && it.isNotEmpty() },
            settingPage = method.optJSONObject("settingPage"),
            hidden = method.optBoolean("hidden", false),
            runType = method.optString("runType", "").takeIf { method.has("runType") && !method.isNull("runType") && it.isNotEmpty() },
            packageName = method.optString("packageName", "").takeIf { method.has("packageName") && !method.isNull("packageName") && it.isNotEmpty() },
            columns = if (method.has("columns") && !method.isNull("columns")) method.optInt("columns") else null,
            autoOpen = if (method.has("autoOpen") && !method.isNull("autoOpen")) method.optBoolean("autoOpen") else null,
            groupTitle = groupTitle?.takeIf { it.isNotBlank() },
            activity = method.optJSONObject("activity"),
            minVersion = if (method.has("minVersion") && !method.isNull("minVersion")) method.optInt("minVersion") else null
        )
    }

    private fun joinTitle(parent: String?, child: String?): String? {
        val parentTitle = parent?.takeIf { it.isNotBlank() }
        val childTitle = child?.takeIf { it.isNotBlank() }
        return when {
            parentTitle == null -> childTitle
            childTitle == null || parentTitle == childTitle -> parentTitle
            else -> "$parentTitle / $childTitle"
        }
    }

    fun DeekeScriptMethod.toJsonString(): String {
        val json = JSONObject()
        json.put("title", title)
        json.put("jsFile", jsFile)
        icon?.let { json.put("icon", it) }
        settingPage?.let { json.put("settingPage", it) }
        json.put("hidden", hidden)
        runType?.let { json.put("runType", it) }
        packageName?.let { json.put("packageName", it) }
        columns?.let { json.put("columns", it) }
        autoOpen?.let { json.put("autoOpen", it) }
        groupTitle?.let { json.put("groupTitle", it) }
        activity?.let { json.put("activity", it) }
        minVersion?.let { json.put("minVersion", it) }
        return json.toString()
    }
}
