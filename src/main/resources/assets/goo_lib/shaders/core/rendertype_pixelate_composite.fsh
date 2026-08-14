#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    if (color.a < 0.05) discard;

    fragColor = color;
    gl_FragDepth = texture(DepthSampler, texCoord).r;
}