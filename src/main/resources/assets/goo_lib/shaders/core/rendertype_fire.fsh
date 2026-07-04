#version 150

#define DISTORTION_STRENGTH 0.1
#define NORMAL_STRENGTH     40.0
#define TIMESCALE           (GameTime * 500)

// Scaler constant to map screen-space units nicely into noise sizes
#define FIRE_SCALE          0.005

#define MOVEMENT_DISTORTION vec2(0.01, 0.3)
#define MOVEMENT_FIRE       vec2(0.01, -0.5)

uniform vec4 ColorModulator;
uniform float GameTime;

in vec4 vertexColor;
out vec4 fragColor;

vec2 hash(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)),
            dot(p, vec2(269.5, 183.3)));
    return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
}

float noise(in vec2 p) {
    const float K1 = 0.366025404;
    const float K2 = 0.211324865;

    vec2 i = floor(p + (p.x + p.y) * K1);
    vec2 a = p - i + (i.x + i.y) * K2;
    vec2 o = step(a.yx, a.xy);
    vec2 b = a - o + K2;
    vec2 c = a - 1.0 + 2.0 * K2;
    vec3 h = max(0.5 - vec3(dot(a, a), dot(b, b), dot(c, c)), 0.0);
    vec3 n = h * h * h * h * vec3(dot(a, hash(i + 0.0)), dot(b, hash(i + o)), dot(c, hash(i + 1.0)));
    return dot(n, vec3(70.0));
}

float fbm(in vec2 p) {
    float f = 0.0;
    mat2 m = mat2(1.6, 1.2, -1.2, 1.6);
    f  = 0.5000 * noise(p); p = m * p;
    f += 0.2500 * noise(p); p = m * p;
    f += 0.1250 * noise(p); p = m * p;
    f += 0.0625 * noise(p); p = m * p;
    return 0.5 + 0.5 * f;
}

vec3 bumpMap(vec2 uv) {
    vec2 s = vec2(0.005, 0.005);
    float p = fbm(uv);
    float h1 = fbm(uv + s * vec2(1.0, 0.0));
    float v1 = fbm(uv + s * vec2(0.0, 1.0));
    vec2 xy = (p - vec2(h1, v1)) * NORMAL_STRENGTH;
    return vec3(xy + 0.5, 1.0);
}

void main() {
    // Normalize coordinates using flat screen space position with scale modifiers
    vec2 fireUV = gl_FragCoord.xy * FIRE_SCALE;

    // Compute fire distortion (Removed raw currentTexSize dependency)
    vec3 normal = bumpMap(fireUV * vec2(1.0, 0.3) + MOVEMENT_DISTORTION * TIMESCALE);
    vec2 displacement = clamp((normal.xy - 0.5) * DISTORTION_STRENGTH, -1.0, 1.0);

    // FIX: Removed the floor() repixelation filtering step to ensure smooth continuous paths
    vec2 finalUV = fireUV + displacement;

    // Compute fire noise
    vec2 uvT = (finalUV * vec2(1.0, 0.5)) + MOVEMENT_FIRE * TIMESCALE;
    float n = pow(fbm(8.0 * uvT), 1.0);

    // Compute height gradient
    float finalNoise = n;

    // Compute fire colors
    vec3 fireColor = finalNoise * vec3(2.0 * n, 2.0 * n * n * n, n * n * n * n);
    vec4 baseFire = vec4(fireColor, finalNoise);

    // Multiply colors by java vertex colors then only multiply with java vertex alpha
    fragColor = (baseFire * vec4(vertexColor.rgb, 1.0)) * vertexColor.a * ColorModulator;
}