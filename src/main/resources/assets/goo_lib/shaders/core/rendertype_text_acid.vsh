#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GameTime;
uniform float Amplitude;       // overall displacement strength
uniform float Speed;           // animation/flow speed through the noise field
uniform float NoiseScale;      // scales world pos into noise space (size of bulges)
uniform float DetailStrength;  // weight of the secondary/finer noise octave (was hardcoded 0.5)
uniform float FlowStrength;    // weight of the gradient "pull" term (was hardcoded 1.0)

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 screenPos;
out float noiseVal;

#define T (GameTime * 1000.0 * Speed)

// --- Simplex-ish 2D noise (Ashima/McEwan, public domain style) ---
vec3 permute(vec3 x) { return mod(((x*34.0)+1.0)*x, 289.0); }

float snoise(vec2 v) {
    const vec4 C = vec4(0.211324865405187, 0.366025403784439,
            -0.577350269189626, 0.024390243902439);
    vec2 i  = floor(v + dot(v, C.yy));
    vec2 x0 = v - i + dot(i, C.xx);
    vec2 i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;
    i = mod(i, 289.0);
    vec3 p = permute(permute(i.y + vec3(0.0, i1.y, 1.0))
    + i.x + vec3(0.0, i1.x, 1.0));
    vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
    m = m*m; m = m*m;
    vec3 x = 2.0 * fract(p * C.www) - 1.0;
    vec3 h = abs(x) - 0.5;
    vec3 ox = floor(x + 0.5);
    vec3 a0 = x - ox;
    m *= 1.79284291400159 - 0.85373472095314 * (a0*a0 + h*h);
    vec3 g;
    g.x = a0.x * x0.x + h.x * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;
    return 130.0 * dot(m, g);
}

void main() {
    // World space so neighboring quads share one continuous field
    vec4 worldPos = ModelViewMat * vec4(Position, 1.0);
    vec2 wp = worldPos.xy * NoiseScale;

    // Two octaves of noise, offset in time so it flows/melts rather than static
    float n1 = snoise(wp + vec2(T * 0.15, T * 0.10));
    float n2 = snoise(wp * 2.3 + vec2(-T * 0.08, T * 0.12) + 17.0);

    // Use noise gradients (approx via offset samples) to get a DIRECTION
    // to push in, not just a magnitude — this is what makes it look like
    // pulling/stretching instead of just bumpy displacement
    float eps = 0.5;
    float nx1 = snoise(wp + vec2(eps, 0.0) + vec2(T * 0.15, T * 0.10));
    float ny1 = snoise(wp + vec2(0.0, eps) + vec2(T * 0.15, T * 0.10));
    vec2 grad = vec2(nx1 - n1, ny1 - n1) / eps;

    vec2 disp = grad * FlowStrength + vec2(n2) * DetailStrength;

    worldPos.xy += disp * Amplitude;

    gl_Position = ProjMat * worldPos;

    vertexColor = Color;
    texCoord0 = UV0;
    screenPos = worldPos.xy;
    noiseVal = n1 * 0.5 + n2 * 0.5;
}
