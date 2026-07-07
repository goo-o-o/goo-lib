// bloom_upsample.fsh — tent filter upsample, GLSL 150
#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;

in vec2 texCoord;
out vec4 fragColor;

uniform float Spread;
uniform float Intensity;

void main() {
    vec2 texel = 1.0 / OutSize * Spread; // was hardcoded 1.0

    vec4 s = vec4(0.0);
    s += texture(DiffuseSampler, texCoord + vec2(-texel.x,  texel.y)) * 1.0;
    s += texture(DiffuseSampler, texCoord + vec2( 0.0,      texel.y)) * 2.0;
    s += texture(DiffuseSampler, texCoord + vec2( texel.x,  texel.y)) * 1.0;
    s += texture(DiffuseSampler, texCoord + vec2(-texel.x,  0.0    )) * 2.0;
    s += texture(DiffuseSampler, texCoord                            ) * 4.0;
    s += texture(DiffuseSampler, texCoord + vec2( texel.x,  0.0    )) * 2.0;
    s += texture(DiffuseSampler, texCoord + vec2(-texel.x, -texel.y)) * 1.0;
    s += texture(DiffuseSampler, texCoord + vec2( 0.0,     -texel.y)) * 2.0;
    s += texture(DiffuseSampler, texCoord + vec2( texel.x, -texel.y)) * 1.0;
    s *= 1.0 / 20.0;
    
    fragColor = vec4(s.rgb * s.a * 5.0, clamp(s.a * 5.0, 0.0, 1.0));
}