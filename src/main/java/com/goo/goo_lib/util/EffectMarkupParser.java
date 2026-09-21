package com.goo.goo_lib.util;

import com.goo.goo_lib.client.text.EffectType;
import com.goo.goo_lib.client.text.effect.ShakeEffect;
import com.goo.goo_lib.client.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.common.registry.TextEffects;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EffectMarkupParser {

    private static final Pattern TAG = Pattern.compile("<(/?)([^>]*)>");
    private static final Pattern EFFECT_SPLIT = Pattern.compile("\\s*\\|\\s*");
    private static final Pattern NAME_TOKEN = Pattern.compile("([A-Za-z0-9_\\-.:]+)\\s*(.*)");
    private static final Pattern ATTR = Pattern.compile("([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|\\[[^\\]]*\\]|[^\\s>]+)");
    private static final Pattern NUMBER_PREFIX = Pattern.compile("^\\s*([-+]?\\d*\\.?\\d+)");

    private static final Map<String, Component> CACHE = new ConcurrentHashMap<>();

    public static void clearCache() {
        CACHE.clear();
    }

    public static Component parseCached(String raw) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        return CACHE.computeIfAbsent(raw, EffectMarkupParser::parse);
    }

    public static Component parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Component.empty();
        }

        if (raw.indexOf('<') < 0 && raw.indexOf('§') < 0) {
            return Component.literal(raw);
        }

        MutableComponent root = Component.empty();
        Matcher matcher = TAG.matcher(raw);
        int cursor = 0;
        Style activeStyle = Style.EMPTY;

        while (matcher.find()) {
            // Process any text preceding the tag
            if (matcher.start() > cursor) {
                String segment = raw.substring(cursor, matcher.start());
                activeStyle = processSegment(root, segment, activeStyle);
            }

            boolean isClose = "/".equals(matcher.group(1));
            String body = matcher.group(2) != null ? matcher.group(2).trim() : "";

            if (!isClose && !body.isEmpty()) {
                // 1. Intercept <percent> tags
                if (body.startsWith("percent")) {
                    String afterTag = raw.substring(matcher.end());

                    Float percentValue = extractPercentValue(body, afterTag);

                    if (percentValue != null) {
                        // Append formatted percent component
                        root.append(percent(percentValue));

                        // If value was pulled from trailing text (e.g. <percent>90), advance cursor past numbers
                        if (body.equalsIgnoreCase("percent")) {
                            Matcher numMatcher = NUMBER_PREFIX.matcher(afterTag);
                            if (numMatcher.find()) {
                                cursor = matcher.end() + numMatcher.end();
                                continue; // consumed tag + inline number
                            }
                        }

                        cursor = matcher.end();
                        continue; // CONSUME TAG: prevents falling through to fallback processSegment
                    }
                }

                // 2. Fallback to standard effect decoding
                List<ConfiguredEffect<?>> effects = parseEffects(body);
                if (effects != null && !effects.isEmpty()) {
                    activeStyle = StyleEffectUtil.createStyleWithEffects(activeStyle, effects);
                } else {
                    activeStyle = processSegment(root, matcher.group(0), activeStyle);
                }
            }
            cursor = matcher.end();
        }

        // Process remaining trailing text
        if (cursor < raw.length()) {
            String remaining = raw.substring(cursor);
            processSegment(root, remaining, activeStyle);
        }

        return root;
    }

    private static Float extractPercentValue(String tagBody, String afterTag) {
        // Try extracting from tag attributes first: <percent value=90>
        Map<String, JsonElement> attrs = parseAttrs(tagBody.substring("percent".length()));
        if (attrs.containsKey("value")) {
            try {
                return attrs.get("value").getAsFloat();
            } catch (Exception ignored) {
            }
        }
        if (attrs.containsKey("val")) {
            try {
                return attrs.get("val").getAsFloat();
            } catch (Exception ignored) {
            }
        }

        // Fallback: extract number directly following <percent> in raw string
        Matcher numMatcher = NUMBER_PREFIX.matcher(afterTag);
        if (numMatcher.find()) {
            try {
                return Float.parseFloat(numMatcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }

        return null;
    }

    public static MutableComponent percent(float percent) {
        float clamped = Mth.clamp(percent, 0, 100);
        int color;

        if (clamped < 66F) {
            float delta = clamped / 66F;
            color = FastColor.ARGB32.lerp(delta, 0xFFE9B115, 0xFFFAD64A);
        } else {
            float delta = (clamped - 66F) / (100F - 66F);
            color = FastColor.ARGB32.lerp(delta, 0xFFFAD64A, 0xFF99FFFD);
        }

        String value = percent % 1 == 0 ? String.valueOf((int) percent) : String.valueOf(percent);
        Style style;
        if (percent > 100) {
            Supplier<ConfiguredEffect<ShakeEffect.Config>> SHAKE_SHR = () -> new ConfiguredEffect<>(
                    TextEffects.SHAKE_TYPE.get(), new ShakeEffect(), ShakeEffect.Config.builder().speed(percent * 0.01F - 1).intensity(0.5F).build()
            );
            style = StyleEffectUtil.createStyleWithEffects(Style.EMPTY.withBold(true).withColor(color), List.of(SHAKE_SHR.get()));
        } else {
            style = Style.EMPTY.withBold(true).withColor(color);
        }

        return Component.literal(value + "%").withStyle(style);
    }

    private static Style processSegment(MutableComponent root, String segment, Style currentStyle) {
        if (segment.isEmpty()) return currentStyle;

        StringBuilder cleanText = new StringBuilder();
        Style style = currentStyle;

        for (int i = 0; i < segment.length(); i++) {
            char c = segment.charAt(i);

            if (c == '§' && i + 1 < segment.length()) {
                char code = Character.toLowerCase(segment.charAt(i + 1));
                ChatFormatting formatting = ChatFormatting.getByCode(code);

                if (!cleanText.isEmpty()) {
                    root.append(Component.literal(cleanText.toString()).setStyle(style));
                    cleanText.setLength(0);
                }

                if (formatting != null) {
                    if (formatting == ChatFormatting.RESET) {
                        style = Style.EMPTY;
                    } else {
                        style = style.applyLegacyFormat(formatting);
                    }
                }
                i++;
            } else {
                cleanText.append(c);
            }
        }

        if (!cleanText.isEmpty()) {
            root.append(Component.literal(cleanText.toString()).setStyle(style));
        }

        return style;
    }

    private static List<ConfiguredEffect<?>> parseEffects(String body) {
        List<ConfiguredEffect<?>> effects = new ArrayList<>();
        for (String piece : EFFECT_SPLIT.split(body)) {
            ConfiguredEffect<?> effect = decodeEffect(piece.trim());
            if (effect == null) return null;
            effects.add(effect);
        }
        return effects;
    }

    private static ConfiguredEffect<?> decodeEffect(String piece) {
        Matcher nameM = NAME_TOKEN.matcher(piece);
        if (!nameM.matches()) return null;

        ResourceLocation id = resolveId(nameM.group(1));
        if (id == null) return null;

        EffectType<?> type = TextEffects.REGISTRY.getRegistry().get().get(id);
        if (type == null) return null;

        return decodeAttrs(type, nameM.group(2));
    }

    private static ResourceLocation resolveId(String s) {
        // if namespace specified try that
        if (s.indexOf(':') >= 0) return ResourceLocation.tryParse(s);

        // if not get the registry
        List<ResourceLocation> matches = new ArrayList<>();
        TextEffects.REGISTRY.getEntries().forEach(entry -> {
            if (entry.getKey().location().getPath().equalsIgnoreCase(s.toLowerCase(Locale.ROOT))) {
                matches.add(entry.getKey().location());
            }
        });

        if (matches.isEmpty()) {
            return GooLib.loc(s.toLowerCase(Locale.ROOT));
        }

        // if only 1 match return
        if (matches.size() == 1) {
            return matches.getFirst();
        }

        for (ResourceLocation loc : matches) {
            if (loc.getNamespace().equals(GooLib.MOD_ID)) {
                // prefer goolib
                return loc;
            }
        }

        return matches.getFirst();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ConfiguredEffect<?> decodeAttrs(EffectType<?> type, String attrs) {
        JsonObject obj = new JsonObject();
        parseAttrs(attrs).forEach(obj::add);

        DataResult<?> result = type.codec().codec().parse(JsonOps.INSTANCE, obj);
        return result.result()
                .map(cfg -> ((EffectType) type).configure(cfg))
                .orElse(null);
    }

    private static Map<String, JsonElement> parseAttrs(String tagBody) {
        Map<String, JsonElement> out = new LinkedHashMap<>();
        if (tagBody == null || tagBody.isBlank()) return out;

        Matcher m = ATTR.matcher(tagBody);
        while (m.find()) {
            String key = m.group(1);
            String raw = m.group(2).trim();

            if ((raw.startsWith("\"") && raw.endsWith("\"")) || (raw.startsWith("'") && raw.endsWith("'"))) {
                raw = raw.substring(1, raw.length() - 1).replace("\\\"", "\"").replace("\\'", "'");
                out.put(key, new JsonPrimitive(raw));
                continue;
            }

            if (raw.startsWith("[") && raw.endsWith("]")) {
                com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
                String inner = raw.substring(1, raw.length() - 1);
                if (!inner.isBlank()) {
                    for (String part : inner.split(",")) {
                        arr.add(parseValueToken(part.trim()));
                    }
                }
                out.put(key, arr);
                continue;
            }

            out.put(key, parseValueToken(raw));
        }
        return out;
    }

    private static JsonElement parseValueToken(String val) {
        if (val.startsWith("#")) {
            try {
                return new JsonPrimitive(Long.parseLong(val.substring(1), 16));
            } catch (NumberFormatException ignored) {
            }
        }
        try {
            return JsonParser.parseString(val);
        } catch (Exception e) {
            return new JsonPrimitive(val);
        }
    }
}