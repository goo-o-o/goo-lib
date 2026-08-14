// bloom_upsample.fsh
#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform float Spread;
uniform float Intensity;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 texel = (1.0 / OutSize) * Spread;

    // 3x3 bilinear tent filter (eliminates graininess on wide spreads)
    vec4 s = vec4(0.0);
    s += texture(DiffuseSampler, texCoord + vec2(-texel.x,  texel.y)) * 1.0;
    s += texture(DiffuseSampler, texCoord + vec2( 0.0,      texel.y)) * 2.0;
    s += texture(DiffuseSampler, texCoord + vec2( texel.x,  texel.y)) * 1.0;
    s += texture(DiffuseSampler, texCoord + vec2(-texel.x,  0.0))     * 2.0;
    s += texture(DiffuseSampler, texCoord + vec2( 0.0,      0.0))     * 4.0;
    s += texture(DiffuseSampler, texCoord + vec2( texel.x,  0.0))     * 2.0;
    s += texture(DiffuseSampler, texCoord + vec2(-texel.x, -texel.y)) * 1.0;
    s += texture(DiffuseSampler, texCoord + vec2( 0.0,     -texel.y)) * 2.0;
    s += texture(DiffuseSampler, texCoord + vec2( texel.x, -texel.y)) * 1.0;
    s *= (1.0 / 16.0);

    fragColor = vec4(s.rgb * Intensity, clamp(s.a * Intensity, 0.0, 1.0));
}