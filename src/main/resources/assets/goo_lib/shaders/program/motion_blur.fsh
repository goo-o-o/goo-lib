#version 330 core

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform float BlendFactor;
uniform vec3 CameraPos;
uniform vec3 PrevCameraPos;
uniform vec2 ViewRes;
uniform mat4 MvInverse;
uniform mat4 ProjInverse;
uniform mat4 PrevModelInverse;
uniform mat4 PrevProjection;
uniform int BlurAlgorithm;
uniform int UseDepth;
uniform int MotionBlurSamples;

in vec2 texCoord;
layout(location = 0) out vec4 color;

#define rcp(x) (1.0 / (x))

vec3 reproject(vec3 screen_pos) {
    vec3 ndc = screen_pos * 2.0 - 1.0;
    vec4 view_pos4 = ProjInverse * vec4(ndc, 1.0);
    vec3 view_pos = view_pos4.xyz / view_pos4.w;

    vec3 world_pos = (MvInverse * vec4(view_pos, 1.0)).xyz + (CameraPos - PrevCameraPos);
    vec4 prev_proj = PrevProjection * (PrevModelInverse * vec4(world_pos, 1.0));

    return (prev_proj.xyz / prev_proj.w) * 0.5 + 0.5;
}

vec2 clampLength(vec2 velocity) {
    float lenSq = dot(velocity, velocity);
    return (lenSq > 0.16) ? velocity * (0.4 * inversesqrt(lenSq)) : velocity;
}

float noise(vec2 pos) {
    return fract(52.9829189 * fract(0.06711056 * pos.x + 0.00583715 * pos.y));
}

void main() {
    ivec2 texel = ivec2(gl_FragCoord.xy);

    float depth = texelFetch(DiffuseDepthSampler, texel, 0).x;
    // Iris Hand Fix
    if (depth < 0.56) {
        color = texture(DiffuseSampler, texCoord);
        return;
    }
    // Depth blend inconsistency fix
    float dilatedDepth = depth;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            float d = texelFetch(DiffuseDepthSampler, texel + ivec2(x, y), 0).x;
            dilatedDepth = min(dilatedDepth, d);
        }
    }
    vec2 velocity = texCoord - reproject(vec3(texCoord, UseDepth == 1 ? dilatedDepth : 1.0)).xy;
    velocity = clampLength(velocity);

    float speed = length(velocity);
    int dynamicSamples = clamp(int(ceil(speed * float(MotionBlurSamples))), 4, MotionBlurSamples);

    vec2 baseStep = (BlendFactor * velocity) / float(dynamicSamples);
    vec3 color_sum = vec3(0.0);
    vec2 seed = texCoord * ViewRes;
    float centerOffset = BlurAlgorithm == 0 ? 0.0 : -(float(dynamicSamples) * 0.5); //logic for centered blur

    for (int i = 0; i < dynamicSamples; ++i) {
        float fi = float(i);

        float jitter = noise(seed + vec2(fi, fi * 1.4));
        vec2 pos = texCoord + (fi + centerOffset + jitter) * baseStep;
        vec3 color = texture(DiffuseSampler, pos).rgb;

        color_sum += color * color;
    }
    color = vec4(sqrt(color_sum / float(dynamicSamples)), 1.0);
}