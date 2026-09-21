#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform vec4 ColorModulator;
uniform float GameTime;
uniform vec2 ScreenSize;

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 quadUv;    // local quad UV
in vec2 screenPos;

out vec4 fragColor;

#define TIME (GameTime * 1000.0)

// 3d noise functions...
float hash3D(vec3 p) {
    p = fract(p * vec3(443.8975, 397.2973, 491.1871));
    p += dot(p, p.yxz + 19.19);
    return fract((p.x + p.y) * p.z);
}

float noise3D(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    return mix(
            mix(mix(hash3D(i + vec3(0, 0, 0)), hash3D(i + vec3(1, 0, 0)), f.x),
                    mix(hash3D(i + vec3(0, 1, 0)), hash3D(i + vec3(1, 1, 0)), f.x), f.y),
            mix(mix(hash3D(i + vec3(0, 0, 1)), hash3D(i + vec3(1, 0, 1)), f.x),
                    mix(hash3D(i + vec3(0, 1, 1)), hash3D(i + vec3(1, 1, 1)), f.x), f.y),
            f.z
    );
}

vec2 curlNoise(vec2 p, float t) {
    float eps = 0.1;
    float n1 = noise3D(vec3(p.x, p.y + eps, t));
    float n2 = noise3D(vec3(p.x, p.y - eps, t));
    float n3 = noise3D(vec3(p.x + eps, p.y, t));
    float n4 = noise3D(vec3(p.x - eps, p.y, t));

    return vec2(n1 - n2, -(n3 - n4));
}

float renderCurlEmbers(vec2 sp, vec2 localUv) {
    float localY = 1.0 - localUv.y;
    float verticalEnvelope = sin(localY * 3.14159265);
    verticalEnvelope = smoothstep(0.0, 1.0, verticalEnvelope);

    if (verticalEnvelope <= 0.001) return 0.0;

    float screenAspect = ScreenSize.x / ScreenSize.y;
    vec2 pos = vec2(sp.x * screenAspect, sp.y);

    vec2 flow = curlNoise(pos * 6.0, TIME);
    vec2 particleUv = pos * 25.0 + flow * 1.5 - vec2(0.0, TIME * 3.0);

    vec2 id = floor(particleUv);
    vec2 gUv = fract(particleUv) - 0.5;

    float rnd = hash3D(vec3(id, 17.0));

    vec2 pPos = vec2(sin(rnd * 6.28 + TIME) * 0.2, cos(rnd * 3.14 + TIME) * 0.2);
    float dist = length(gUv - pPos);

    float maxSparkRadius = 0.12 + rnd * 0.08;
    float currentSparkRadius = maxSparkRadius * verticalEnvelope;

    float spark = smoothstep(currentSparkRadius, 0.0, dist);
    float flicker = 0.7 + 0.3 * sin(TIME * 5.0 + rnd * 100.0);

    return spark * flicker * verticalEnvelope;
}

void main() {
    // 1. Calculate heat distortion offset for font sampling
    // localUv.y (1.0 - quadUv.y) ensures heat refraction increases higher up the text
    float heatHeightMask = (1.0 - quadUv.y);
    vec2 distortionFlow = curlNoise(screenPos * 8.0, TIME * 0.5);
    vec2 distortedFontUV = texCoord0 + distortionFlow * 0.0015 * heatHeightMask;

    // Sample Sampler0 with heat-distorted UV coordinates
    vec4 fontColor = texture(Sampler0, distortedFontUV);

    // Dynamic threshold expansion: lowering min bound inflates the glyph mask outwards
    float shimmerWave = sin(TIME * 4.0 + quadUv.x * 3.0 + quadUv.y * 3.0) * 0.03;
    float minEdge = clamp(0.01 + shimmerWave, 0.001, 0.05);

    // Inflated soft font alpha mask
    float softFontAlpha = smoothstep(minEdge, 0.45, fontColor.a);
    float emberIntensity = renderCurlEmbers(screenPos, quadUv);

    if (softFontAlpha < 0.01 && emberIntensity < 0.01) {
        discard;
    }

    float screenAspect = ScreenSize.x / ScreenSize.y;
    vec2 aspectCorrectedScreenPos = vec2(screenPos.x * screenAspect, screenPos.y);

    // 2. Add heat distortion flow to magma texture UVs
    float textureScale = 8.0;
    vec2 scrollSpeed = vec2(0.001, -0.001);

    vec2 magmaUV = aspectCorrectedScreenPos * textureScale;
    magmaUV += TIME * scrollSpeed;
    magmaUV += distortionFlow * 0.15; // distort magma fill
    magmaUV = fract(magmaUV);

    vec4 magmaColor = texture(Sampler1, magmaUV);
    vec3 tint = length(vertexColor.rgb) < 0.01 ? vec3(1.0) : vertexColor.rgb;

    vec3 moltenText = magmaColor.rgb * tint * 1.5;

    vec3 finalColor = mix(tint, moltenText, softFontAlpha);
    float finalAlpha = max(softFontAlpha, emberIntensity);

    vec4 colMod = ColorModulator.a < 0.01 ? vec4(ColorModulator.rgb, 1.0) : ColorModulator;
    fragColor = vec4(finalColor, finalAlpha) * colMod;
}