package top.deeke.groupcontrol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import top.deeke.groupcontrol.utils.DeekeScriptJsonParser

class DeekeScriptJsonParserTest {
    private val parser = DeekeScriptJsonParser()

    @Test
    fun parsesGroupsAndActivityGroups() {
        val json = """
            {
              "groups": [
                {
                  "title": "DY·万能引流",
                  "methods": [
                    {"title": "推荐营销", "jsFile": "tasks/task_dy_toker.js"},
                    {
                      "title": "工具箱",
                      "jsFile": "tasks/tool.js",
                      "activity": {
                        "title": "工具箱",
                        "groups": [
                          {
                            "title": "工具箱",
                            "methods": [
                              {"title": "一键取赞", "jsFile": "tasks/task_dy_cancel_zan.js", "hidden": false}
                            ]
                          }
                        ]
                      }
                    }
                  ]
                }
              ],
              "bottomMenus": [
                {"title": "底部", "methods": [{"title": "底部功能", "jsFile": "tasks/bottom.js"}]}
              ]
            }
        """.trimIndent()

        val methods = parser.parseDeekeScriptJson(json)
        assertEquals(
            listOf(
                "DY·万能引流 / 推荐营销",
                "DY·万能引流 / 工具箱",
                "DY·万能引流 / 工具箱 / 一键取赞",
                "底部功能"
            ),
            methods.map { it.displayName }
        )
        assertEquals("tasks/task_dy_cancel_zan.js", methods[2].jsFile)
        assertTrue(methods[1].activity != null)
    }

    @Test
    fun parsesLegacyMethodsAndCate() {
        val json = """
            {
              "cate": {"data": [{"count": 1, "title": "组A"}]},
              "methods": [
                {
                  "title": "工具箱",
                  "jsFile": "tasks/tool.js",
                  "activity": {
                    "title": "工具箱",
                    "methods": [
                      {"title": "一键取赞", "jsFile": "tasks/a.js"}
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val methods = parser.parseDeekeScriptJson(json)
        assertEquals(listOf("组A / 工具箱", "组A / 工具箱 / 一键取赞"), methods.map { it.displayName })
    }
}
