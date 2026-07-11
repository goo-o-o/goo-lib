#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;


void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    fragColor = vec4(tex.rgb * vertexColor.rgb * vertexColor.a, tex.a * vertexColor.a);
}