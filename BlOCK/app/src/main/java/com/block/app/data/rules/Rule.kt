package com.block.app.data.rules

import org.json.JSONArray
import org.json.JSONObject

/**
 * Types of accessibility-node signals we can match.
 */
enum class SignalType {
    viewId,
    className,
    contentDescription
}

/**
 * A single signal that can be matched against an accessibility node.
 * [pattern] is a regex.
 */
data class Signal(
    val type: SignalType,
    val pattern: String
)

/**
 * A detection rule: when enough signals match on a node,
 * the bouncer steps in.
 */
data class DetectionRule(
    val id: String,
    val appPackage: String,       // "*" = any app (browser rules)
    val appName: String,
    val enabled: Boolean = true,
    val signals: List<Signal>,
    val minSignals: Int = 2,
    val browserRule: Boolean = false,
    val urlPatterns: List<String> = emptyList()
)

/**
 * Parses and serializes [DetectionRule] lists to/from JSON
 * using the built-in org.json classes (no Gson/Moshi).
 */
object RulesParser {

    /**
     * Parse the top-level rules.json content into a list of rules.
     * Expected format:
     * ```json
     * {
     *   "rules": [ { ... }, { ... } ]
     * }
     * ```
     */
    fun parseRules(json: String): List<DetectionRule> {
        val root = JSONObject(json)
        val rulesArray = root.getJSONArray("rules")
        return (0 until rulesArray.length()).map { i ->
            parseRule(rulesArray.getJSONObject(i))
        }
    }

    /**
     * Parse a single rule JSON object.
     */
    fun parseRule(obj: JSONObject): DetectionRule {
        val signalsArray = obj.getJSONArray("signals")
        val signals = (0 until signalsArray.length()).map { i ->
            val s = signalsArray.getJSONObject(i)
            Signal(
                type = SignalType.valueOf(s.getString("type")),
                pattern = s.getString("pattern")
            )
        }

        val urlPatterns = if (obj.has("urlPatterns")) {
            val arr = obj.getJSONArray("urlPatterns")
            (0 until arr.length()).map { arr.getString(it) }
        } else {
            emptyList()
        }

        return DetectionRule(
            id = obj.getString("id"),
            appPackage = obj.getString("appPackage"),
            appName = obj.getString("appName"),
            enabled = obj.optBoolean("enabled", true),
            signals = signals,
            minSignals = obj.optInt("minSignals", 2),
            browserRule = obj.optBoolean("browserRule", false),
            urlPatterns = urlPatterns
        )
    }

    /**
     * Serialize a list of rules back to a JSON string.
     */
    fun toJson(rules: List<DetectionRule>): String {
        val root = JSONObject()
        val arr = JSONArray()
        rules.forEach { rule -> arr.put(ruleToJson(rule)) }
        root.put("rules", arr)
        return root.toString(2) // pretty-print with 2-space indent
    }

    /**
     * Serialize a single rule to a JSONObject.
     */
    fun ruleToJson(rule: DetectionRule): JSONObject {
        val obj = JSONObject()
        obj.put("id", rule.id)
        obj.put("appPackage", rule.appPackage)
        obj.put("appName", rule.appName)
        obj.put("enabled", rule.enabled)
        obj.put("minSignals", rule.minSignals)
        obj.put("browserRule", rule.browserRule)

        val signalsArr = JSONArray()
        rule.signals.forEach { signal ->
            val s = JSONObject()
            s.put("type", signal.type.name)
            s.put("pattern", signal.pattern)
            signalsArr.put(s)
        }
        obj.put("signals", signalsArr)

        if (rule.urlPatterns.isNotEmpty()) {
            val urlArr = JSONArray()
            rule.urlPatterns.forEach { urlArr.put(it) }
            obj.put("urlPatterns", urlArr)
        }

        return obj
    }
}
