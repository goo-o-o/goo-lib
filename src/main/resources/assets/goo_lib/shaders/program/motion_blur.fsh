#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

uniform mat4 InvViewProjMat;
uniform mat4 PrevViewProjMat;
uniform vec3 CameraPosDelta;
uniform vec2 InSize;

in vec2 texCoord;
out vec4 fragColor;

vec2 reproject(vec2 uv, float depth) {
    vec4 currentClip = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 localWorldPos = InvViewProjMat * currentClip;
    localWorldPos /= localWorldPos.w;

    vec4 prevClip = PrevViewProjMat * vec4(localWorldPos.xyz + CameraPosDelta, 1.0);
    prevClip /= prevClip.w;

    return prevClip.xy * 0.5 + 0.5;
}

float noise(vec2 pos) {
    return fract(52.9829189 * fract(0.06711056 * pos.x + 0.00583715 * pos.y));
}

void main() {
    ivec2 texel = ivec2(gl_FragCoord.xy);
    float depth = texelFetch(DepthSampler, texel, 0).r;

    // depth guard for near items
    if (depth <= 0.9) {
        fragColor = texture(DiffuseSampler, texCoord);
        return;
    }

    float dilatedDepth = depth;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            float d = texelFetch(DepthSampler, texel + ivec2(x, y), 0).r;
            dilatedDepth = max(dilatedDepth, d);
        }
    }

    vec2 prevTexCoord = reproject(texCoord, dilatedDepth);
    vec2 velocity = texCoord - prevTexCoord;

    float maxVel = 0.14;
    float lenSq = dot(velocity, velocity);
    if (lenSq > maxVel * maxVel) {
        velocity = normalize(velocity) * maxVel;
    }

    int samples = 32;
    vec2 baseStep = velocity / float(samples);
    vec3 colorSum = vec3(0.0);
    float alphaSum = 0.0;
    vec2 seed = texCoord * InSize;
    float centerOffset = -(float(samples) * 0.5);

    for (int i = 0; i < samples; ++i) {
        float fi = float(i);
        float jitter = noise(seed + vec2(fi, fi * 1.4));
        vec2 offsetUV = texCoord + (fi + centerOffset + jitter) * baseStep;
        offsetUV = clamp(offsetUV, vec2(0.001), vec2(0.999));

        vec4 sampledColor = texture(DiffuseSampler, offsetUV);
        colorSum += sampledColor.rgb * sampledColor.rgb;
        alphaSum += sampledColor.a;
    }

    // average out both colors and the sampled alpha channels to allow transparency passing
    fragColor = vec4(sqrt(colorSum / float(samples)), alphaSum / float(samples));
//    fragColor = vec4(velocity * 10.0, 0.0, 1.0);
}