// bloom_downsample.fsh
#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform vec2 OutSize;  // must be here AND used

in vec2 texCoord;
out vec4 fragColor;
uniform float Spread;

void main() {
    vec2 texelIn = 1.0 / InSize;
    vec2 spread = vec2(Spread * 2.0, Spread); // increase this for wider spread

    vec4 color = vec4(0.0);
    color += texture(DiffuseSampler, texCoord + vec2(-texelIn.x * spread.x,  texelIn.y * spread.y)) * 0.125;
    color += texture(DiffuseSampler, texCoord + vec2( 0.0,                   texelIn.y   * spread.y)) * 0.125;
    color += texture(DiffuseSampler, texCoord + vec2( texelIn.x * spread.x,  texelIn.y * spread.y)) * 0.125;
    color += texture(DiffuseSampler, texCoord + vec2(-texelIn.x * spread.x,  0.0              )) * 0.125;
    color += texture(DiffuseSampler, texCoord                                                 ) * 0.125;
    color += texture(DiffuseSampler, texCoord + vec2( texelIn.x * spread.x,  0.0              )) * 0.125;
    color += texture(DiffuseSampler, texCoord + vec2(-texelIn.x * spread.x, -texelIn.y * spread.y)) * 0.125;
    color += texture(DiffuseSampler, texCoord + vec2( 0.0,                  -texelIn.y   * spread.y)) * 0.125;
    color += texture(DiffuseSampler, texCoord + vec2( texelIn.x * spread.x, -texelIn.y * spread.y)) * 0.125;

    fragColor = color;
}