// bloom_downsample.fsh
#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform vec2 OutSize;
uniform float Spread;
uniform float DecodeIntensity;

in vec2 texCoord;
out vec4 fragColor;

vec4 sampleTap(vec2 uv) {
    vec4 c = texture(DiffuseSampler, uv);
    if (DecodeIntensity > 0.5) {
        float sourceIntensity = c.a;
        // decode intensity into rgb channels
        return vec4(c.rgb * sourceIntensity, c.a > 0.0 ? 1.0 : 0.0);
    }
    return c;
}

void main() {
    vec2 texel = (1.0 / InSize) * Spread;

    // 13-tap sample pattern (4 inner, 9 outer)
    vec4 A = sampleTap(texCoord + vec2(-2.0,  2.0) * texel);
    vec4 B = sampleTap(texCoord + vec2( 0.0,  2.0) * texel);
    vec4 C = sampleTap(texCoord + vec2( 2.0,  2.0) * texel);
    vec4 D = sampleTap(texCoord + vec2(-2.0,  0.0) * texel);
    vec4 E = sampleTap(texCoord);
    vec4 F = sampleTap(texCoord + vec2( 2.0,  0.0) * texel);
    vec4 G = sampleTap(texCoord + vec2(-2.0, -2.0) * texel);
    vec4 H = sampleTap(texCoord + vec2( 0.0, -2.0) * texel);
    vec4 I = sampleTap(texCoord + vec2( 2.0, -2.0) * texel);

    vec4 J = sampleTap(texCoord + vec2(-1.0,  1.0) * texel);
    vec4 K = sampleTap(texCoord + vec2( 1.0,  1.0) * texel);
    vec4 L = sampleTap(texCoord + vec2(-1.0, -1.0) * texel);
    vec4 M = sampleTap(texCoord + vec2( 1.0, -1.0) * texel);

    // standard weighted average
    vec4 avg = (D+E+I+H)*0.03125 + (A+B+G+F)*0.03125 + (B+C+H+I)*0.03125 + (E+F+I+H)*0.03125;
    avg += (J+K+L+M)*0.125;

    // max tap capture to keep thin trails/particles alive across wide spreads
    vec4 maxTap = max(E, max(max(J, K), max(L, M)));

    // mix weighted average with max energy preservation (80% avg / 20% max)
    fragColor = mix(avg, maxTap, 0.20);
}